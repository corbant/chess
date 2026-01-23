package client;

import org.junit.jupiter.api.*;

import chess.ChessGame.TeamColor;
import server.Server;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("localhost", port);
    }

    @BeforeEach
    public void reset() throws Exception {
        facade.clearDB();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    public void registerSuccess() throws Exception {
        var res = facade.register("player1", "password", "p1@email.com");
        Assertions.assertNotNull(res.authToken());
    }

    @Test
    public void registerFailure() throws Exception {
        facade.register("player1", "password", "p1@email.com");
        Assertions.assertThrows(AlreadyTakenException.class,
                () -> facade.register("player1", "password", "p1@email.com"));
    }

    @Test
    public void loginSuccess() throws Exception {
        var res = facade.register("player1", "password", "p1@email.com");
        facade.logout(res.authToken());
        res = facade.login("player1", "password");
        Assertions.assertNotNull(res.authToken());
    }

    @Test
    public void loginFailure() throws Exception {
        var res = facade.register("player1", "password", "p1@email.com");
        facade.logout(res.authToken());
        Assertions.assertThrows(UnauthorizedException.class, () -> facade.login("asdf", "asdf"));
    }

    @Test
    public void logoutSuccess() throws Exception {
        var res = facade.register("player1", "password", "p1@email.com");
        Assertions.assertDoesNotThrow(() -> facade.logout(res.authToken()));
    }

    @Test
    public void logoutFailure() throws Exception {
        Assertions.assertThrows(UnauthorizedException.class, () -> facade.logout("sdfjabljasdlkfjas"));
    }

    @Test
    public void createGameSuccess() throws Exception {
        var res = facade.register("player1", "password", "p1@email.com");
        var gameRes = facade.createGame(res.authToken(), "test");
        Assertions.assertTrue(gameRes.gameID() > 0);
    }

    @Test
    public void createGameFailure() throws Exception {
        var res = facade.register("player1", "password", "p1@email.com");
        Assertions.assertThrows(BadRequestException.class, () -> facade.createGame(res.authToken(), ""));
    }

    @Test
    public void listGamesSuccess() throws Exception {
        var res = facade.register("player1", "password", "p1@email.com");
        facade.createGame(res.authToken(), "test1");
        facade.createGame(res.authToken(), "test2");
        var gamesListRes = facade.listGames(res.authToken());
        Assertions.assertTrue(gamesListRes.games().length > 0);
    }

    @Test
    public void listGamesFailure() throws Exception {
        Assertions.assertThrows(UnauthorizedException.class, () -> facade.listGames("asdfasdf"));
    }

    @Test
    public void playGameSuccess() throws Exception {
        var res = facade.register("player1", "password", "p1@email.com");
        var createGameRes = facade.createGame(res.authToken(), "test1");
        Assertions.assertDoesNotThrow(() -> facade.playGame(res.authToken(), createGameRes.gameID(), TeamColor.WHITE));
    }

    @Test
    public void playGameFailure() throws Exception {
        var res = facade.register("player1", "password", "p1@email.com");
        var createGameRes = facade.createGame(res.authToken(), "test1");
        facade.playGame(res.authToken(), createGameRes.gameID(), TeamColor.WHITE);
        Assertions.assertThrows(AlreadyTakenException.class,
                () -> facade.playGame(res.authToken(), createGameRes.gameID(), TeamColor.WHITE));
    }

}
