package server;

import io.javalin.*;
import service.*;
import service.request.*;
import service.result.*;

public class Server {

    private final Javalin javalin;
    private final UserService userService;
    private final GameService gameService;
    private final DBService dbService;

    public Server() {
        userService = new UserService();
        gameService = new GameService();
        dbService = new DBService();

        javalin = Javalin.create(config -> config.staticFiles.add("resources/web"));

        // Register your endpoints and exception handlers here.
        javalin.post("/user", ctx -> {
            RegisterRequest registerRequest = ctx.bodyAsClass(RegisterRequest.class);
            RegisterResult registerResult = userService.register(registerRequest);
            ctx.status(200).json(registerResult);
        });

        javalin.post("/session", ctx -> {
            LoginRequest loginRequest = ctx.bodyAsClass(LoginRequest.class);
            LoginResult loginResult = userService.login(loginRequest);
            ctx.status(201).json(loginResult);
        });

        javalin.delete("/session", ctx -> {
            LogoutRequest logoutRequest = ctx.bodyAsClass(LogoutRequest.class);
            userService.logout(logoutRequest);
            ctx.status(200);
        });

        javalin.get("/game", ctx -> {
            GameListRequest gameListRequest = ctx.bodyAsClass(GameListRequest.class);
            GameListResponse gameListResponse = gameService.list(gameListRequest);
            ctx.status(200).json(gameListResponse);
        });

        javalin.post("/game", ctx -> {
            GameCreateRequest gameCreateRequest = ctx.bodyAsClass(GameCreateRequest.class);
            GameCreateResponse gameCreateResponse = gameService.create(gameCreateRequest);
            ctx.status(200).json(gameCreateResponse);
        });

        javalin.put("/game", ctx -> {
            GameJoinRequest gameJoinRequest = ctx.bodyAsClass(GameJoinRequest.class);
            gameService.join(gameJoinRequest);
            ctx.status(200);
        });

        javalin.delete("/db", ctx -> {
            dbService.clear();
            ctx.status(200);
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
