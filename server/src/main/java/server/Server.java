package server;

import com.google.gson.Gson;

import chess.ChessGame.TeamColor;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import dataaccess.SQLAuthDAO;
import dataaccess.SQLGameDAO;
import dataaccess.SQLUserDAO;
import io.javalin.*;
import io.javalin.json.JavalinGson;
import io.javalin.validation.ValidationException;
import io.javalin.websocket.WsMessageContext;
import service.*;
import service.request.*;
import service.result.*;
import websocket.commands.ConnectCommand;
import websocket.commands.LeaveCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ResignCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;

public class Server {

    private final Javalin javalin;
    private final UserService userService;
    private final GameService gameService;
    private final DBService dbService;
    private final GameplayService gameplayService;
    private final AuthDAO authDAO;
    private final UserDAO userDAO;
    private final GameDAO gameDAO;

    private final WebsocketConnectionManager connectionManager;

    private static final String ERROR_MESSAGE_FORMAT = "Error: %s";

    public Server() {
        initializeDatabase();
        // DAOs
        authDAO = new SQLAuthDAO();
        userDAO = new SQLUserDAO();
        gameDAO = new SQLGameDAO();
        // Services
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
        dbService = new DBService(authDAO, userDAO, gameDAO);
        gameplayService = new GameplayService(gameDAO, authDAO);

        connectionManager = new WebsocketConnectionManager();
        javalin = createJavalin();
        registerHttpRoutes();
        registerWebsocketRoutes();
        addExceptionHandlers(javalin);
    }

    private void initializeDatabase() {
        // Configure DB
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            // can't access the db
            System.exit(-1);
        }
    }

    private Javalin createJavalin() {
        return Javalin.create(config -> {
            config.staticFiles.add("web");
            var serializer = new Gson();
            config.jsonMapper(new JavalinGson(serializer, false));
        });
    }

    private void registerHttpRoutes() {
        javalin.post("/user", ctx -> {
            RegisterRequest registerRequest = ctx.bodyValidator(RegisterRequest.class)
                    .check(req -> req.username() != null && !req.username().isBlank(), "username required")
                    .check(req -> req.password() != null && !req.password().isBlank(), "password required")
                    .check(req -> req.email() != null && !req.email().isBlank(), "email required").get();
            RegisterResult registerResult = userService.register(registerRequest);
            ctx.status(200).json(registerResult);
        });

        javalin.post("/session", ctx -> {
            LoginRequest loginRequest = ctx.bodyValidator(LoginRequest.class)
                    .check(req -> req.username() != null && !req.username().isBlank(), "username required")
                    .check(req -> req.password() != null && !req.password().isBlank(), "password required").get();
            LoginResult loginResult = userService.login(loginRequest);
            ctx.status(200).json(loginResult);
        });

        javalin.delete("/session", ctx -> {
            new AuthenticateHandler(authDAO).handle(ctx);
            String authToken = ctx.header("authorization");
            userService.logout(authToken);
            ctx.status(200);
        });

        javalin.before("/game", new AuthenticateHandler(authDAO));
        javalin.get("/game", ctx -> {
            GameListResponse gameListResponse = gameService.list();
            ctx.status(200).json(gameListResponse);
        });

        javalin.post("/game", ctx -> {
            GameCreateRequest gameCreateRequest = ctx.bodyValidator(GameCreateRequest.class)
                    .check(req -> req.gameName() != null && !req.gameName().isBlank(), "gameName required").get();
            GameCreateResponse gameCreateResponse = gameService.create(gameCreateRequest);
            ctx.status(200).json(gameCreateResponse);
        });

        javalin.put("/game", ctx -> {
            GameJoinRequest gameJoinRequest = ctx.bodyValidator(GameJoinRequest.class).check(req -> {
                if (req.playerColor() == null || req.playerColor().isBlank()) {
                    return false;
                }
                try {
                    TeamColor.valueOf(req.playerColor());
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }, "invalid player color").check(req -> req.gameID() > 0, "invalid game ID").get();
            String authToken = ctx.header("authorization");
            gameService.join(gameJoinRequest, authToken);
            ctx.status(200);
        });

        javalin.delete("/db", ctx -> {
            dbService.clear();
            ctx.status(200);
        });
    }

    private void registerWebsocketRoutes() {
        javalin.ws("/ws", ws -> {
            ws.onConnect(ctx -> ctx.enableAutomaticPings());
            ws.onMessage(ctx -> {
                UserGameCommand command = ctx.messageAsClass(UserGameCommand.class);
                CommandResult commandResult = handleWebsocketCommand(ctx, command);
                if (commandResult != null) {
                    dispatchOutboundMessages(ctx, commandResult);
                }
            });
        });
    }

    private CommandResult handleWebsocketCommand(WsMessageContext ctx, UserGameCommand command) {
        try {
            return executeWebsocketCommand(ctx, command);
        } catch (UnauthorizedException e) {
            sendWsError(ctx, "unauthorized");
        } catch (DoesNotExistException e) {
            sendWsError(ctx, "game not found");
        } catch (ServerErrorException e) {
            sendWsError(ctx, "server error, please try again");
        }
        return null;
    }

    private CommandResult executeWebsocketCommand(WsMessageContext ctx, UserGameCommand command)
            throws UnauthorizedException, DoesNotExistException, ServerErrorException {
        return switch (command.getCommandType()) {
            case CONNECT -> handleConnect(ctx);
            case MAKE_MOVE -> handleMakeMove(ctx);
            case LEAVE -> handleLeave(ctx);
            case RESIGN -> handleResign(ctx);
        };
    }

    private CommandResult handleConnect(WsMessageContext ctx)
            throws UnauthorizedException, DoesNotExistException, ServerErrorException {
        ConnectCommand command = ctx.messageAsClass(ConnectCommand.class);
        CommandResult result = gameplayService.connect(command);
        connectionManager.addSession(command.getGameID(), ctx);
        return result;
    }

    private CommandResult handleMakeMove(WsMessageContext ctx)
            throws UnauthorizedException, DoesNotExistException, ServerErrorException {
        MakeMoveCommand command = ctx.messageAsClass(MakeMoveCommand.class);
        return gameplayService.makeMove(command);
    }

    private CommandResult handleLeave(WsMessageContext ctx)
            throws UnauthorizedException, DoesNotExistException, ServerErrorException {
        LeaveCommand command = ctx.messageAsClass(LeaveCommand.class);
        CommandResult result = gameplayService.leaveGame(command);
        connectionManager.removeSession(command.getGameID(), ctx);
        return result;
    }

    private CommandResult handleResign(WsMessageContext ctx)
            throws UnauthorizedException, DoesNotExistException, ServerErrorException {
        ResignCommand command = ctx.messageAsClass(ResignCommand.class);
        return gameplayService.resign(command);
    }

    private void dispatchOutboundMessages(WsMessageContext ctx, CommandResult commandResult) {
        for (var outbound : commandResult.outbound()) {
            switch (outbound.target()) {
                case SELF -> ctx.sendAsClass(outbound.message(), outbound.message().getClass());
                case OTHERS -> connectionManager.broadcast(commandResult.gameID(), outbound.message(), ctx);
                case ALL -> connectionManager.broadcastAll(commandResult.gameID(), outbound.message());
            }
        }
    }

    private void sendWsError(WsMessageContext ctx, String errorText) {
        ctx.sendAsClass(new ErrorMessage(String.format(ERROR_MESSAGE_FORMAT, errorText)), ErrorMessage.class);
    }

    private void addExceptionHandlers(Javalin javalin) {
        javalin.exception(ServerErrorException.class, (e, ctx) -> {
            String error = String.format(ERROR_MESSAGE_FORMAT, e.getMessage());
            ctx.status(500).json(new FailureResponse(error));
        });

        javalin.exception(AlreadyTakenException.class, (e, ctx) -> {
            String error = String.format(ERROR_MESSAGE_FORMAT, "already taken");
            ctx.status(403).json(new FailureResponse(error));
        });

        javalin.exception(DoesNotExistException.class, (e, ctx) -> {
            String error = String.format(ERROR_MESSAGE_FORMAT, "bad request");
            ctx.status(400).json(new FailureResponse(error));
        });

        javalin.exception(UnauthorizedException.class, (e, ctx) -> {
            String error = String.format(ERROR_MESSAGE_FORMAT, "unauthorized");
            ctx.status(401).json(new FailureResponse(error));
        });

        javalin.exception(ValidationException.class, (e, ctx) -> {
            String error = String.format(ERROR_MESSAGE_FORMAT, "bad request");
            ctx.status(400).json(new FailureResponse(error));
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
