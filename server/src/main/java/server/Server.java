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
            RegisterRequest registerRequest = ctx.bodyValidator(RegisterRequest.class).get();
            RegisterResult registerResult = userService.register(registerRequest);
            ctx.status(200).json(registerResult);
        });

        javalin.post("/session", ctx -> {
            LoginRequest loginRequest = ctx.bodyValidator(LoginRequest.class).get();
            LoginResult loginResult = userService.login(loginRequest);
            ctx.status(201).json(loginResult);
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
            GameCreateRequest gameCreateRequest = ctx.bodyValidator(GameCreateRequest.class).get();
            GameCreateResponse gameCreateResponse = gameService.create(gameCreateRequest);
            ctx.status(200).json(gameCreateResponse);
        });

        javalin.put("/game", ctx -> {
            GameJoinRequest gameJoinRequest = ctx.bodyValidator(GameJoinRequest.class).check(req -> {
                try {
                    TeamColor.valueOf(req.playerColor());
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }, "invalid player color").get();
            String authToken = ctx.header("authorization");
            gameService.join(gameJoinRequest, authToken);
            ctx.status(200);
        });

        javalin.delete("/db", ctx -> {
            dbService.clear();
            ctx.status(200);
        });

        // TODO: add exception handlers
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
