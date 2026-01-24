package server;

import com.google.gson.Gson;

import chess.ChessGame;
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
import service.*;
import service.request.*;
import service.result.*;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;

public class Server {

    private final Javalin javalin;
    private final UserService userService;
    private final GameService gameService;
    private final DBService dbService;
    private final GameplayService gameplayService;
    private final AuthDAO authDAO;
    private final UserDAO userDAO;
    private final GameDAO gameDAO;

    private static final String ERROR_MESSAGE_FORMAT = "Error: %s";

    public Server() {
        // Configure DB
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            // can't access the db
            System.exit(-1);
        }
        // DAOs
        authDAO = new SQLAuthDAO();
        userDAO = new SQLUserDAO();
        gameDAO = new SQLGameDAO();
        // Services
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
        dbService = new DBService(authDAO, userDAO, gameDAO);
        gameplayService = new GameplayService(gameDAO);
        // web server
        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
            var serializer = new Gson();
            config.jsonMapper(new JavalinGson(serializer, false));
        });
        // handlers
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

        javalin.ws("/ws", ws -> {

            ws.onConnect(ctx -> {
                ctx.enableAutomaticPings();
            });

            ws.onMessage(ctx -> {
                UserGameCommand command = ctx.messageAsClass(null);
                // validate
                if (command.getAuthToken() == null || authDAO.getAuth(command.getAuthToken()) == null) {
                    ctx.sendAsClass(new ErrorMessage(String.format(ERROR_MESSAGE_FORMAT, "invalid authToken")),
                            ErrorMessage.class);
                    return;
                }
                if (command.getGameID() == 0 || gameDAO.getGame(command.getGameID()) == null) {
                    ctx.sendAsClass(new ErrorMessage(String.format(ERROR_MESSAGE_FORMAT, "invalid gameID")),
                            ErrorMessage.class);
                }

                ChessGame game = null;
                switch (command.getCommandType()) {
                    case CONNECT:
                        game = gameplayService.getChessGame(command.getGameID());
                        break;
                    case MAKE_MOVE:
                        game = gameplayService.makeMove((MakeMoveCommand) command);
                        break;
                    case LEAVE:
                        break;
                    case RESIGN:
                        break;
                }

                if (game != null) {
                    ctx.sendAsClass(new LoadGameMessage(game), getClass());
                }
            });
        });
        // exception handlers
        addExceptionHandlers(javalin);
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
