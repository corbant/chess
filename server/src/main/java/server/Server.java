package server;

import chess.ChessGame.TeamColor;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import io.javalin.*;
import service.*;
import service.request.*;
import service.result.*;

public class Server {

    private final Javalin javalin;
    private final UserService userService;
    private final GameService gameService;
    private final DBService dbService;
    private final AuthDAO authDAO;
    private final UserDAO userDAO;
    private final GameDAO gameDAO;

    private static final String ERROR_MESSAGE_FORMAT = "Error: %s";

    public Server() {
        // DAOs
        authDAO = new MemoryAuthDAO();
        userDAO = new MemoryUserDAO();
        gameDAO = new MemoryGameDAO();

        // Services
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
        dbService = new DBService(authDAO, userDAO, gameDAO);

        javalin = Javalin.create(config -> config.staticFiles.add("resources/web"));

        // Register your endpoints and exception handlers here.
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
                if (req.playerColor() == null || req.playerColor().isBlank())
                    return false;
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

        // exception handlers
        javalin.exception(ServerErrorException.class, (e, ctx) -> {
            String error = String.format(ERROR_MESSAGE_FORMAT, e.getMessage());
            ctx.status(500).json(new FailureResponse(error));
        });

        javalin.exception(AlreadyTakenException.class, (e, ctx) -> {
            String error = String.format(ERROR_MESSAGE_FORMAT, "already taken");
            ctx.status(403).json(new FailureResponse(error));
        });

        javalin.exception(DoesNotExistException.class, (e, ctx) -> {
            String error = String.format(ERROR_MESSAGE_FORMAT, "not found");
            ctx.status(404).json(new FailureResponse(error));
        });

        javalin.exception(UnauthorizedException.class, (e, ctx) -> {
            String error = String.format(ERROR_MESSAGE_FORMAT, "unauthorized");
            ctx.status(401).json(new FailureResponse(error));
        });

        javalin.error(400, ctx -> {
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
