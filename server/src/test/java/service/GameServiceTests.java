package service;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import model.AuthData;
import model.GameData;
import service.request.GameCreateRequest;
import service.request.GameJoinRequest;

public class GameServiceTests {

    private static GameDAO gameDAO;
    private static AuthDAO authDAO;

    private static GameService gameService;

    @BeforeAll
    public static void init() {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();

        gameService = new GameService(gameDAO, authDAO);
    }

    @BeforeEach
    public void reset() {
        if (gameDAO != null) {
            gameDAO.clear();
        }
        if (authDAO != null) {
            authDAO.clear();
        }
    }

    @Test
    public void listGamesSuccess() {
        GameData[] games = new GameData[] {
                new GameData(1, "username", "username", "game 1", new ChessGame()),
                new GameData(2, "username", "username", "game 2", new ChessGame()),
                new GameData(3, "username", "username", "game 3", new ChessGame())
        };
        for (GameData game : games) {
            gameDAO.createGame(game);
        }

        Assertions.assertArrayEquals(games, gameService.list().games());
    }

    @Test
    public void listGamesFailure() {
        GameData[] games = gameService.list().games();
        Assertions.assertNotNull(games);
        Assertions.assertEquals(0, games.length);
    }

    @Test
    public void createGameSuccess() {
        GameCreateRequest req = new GameCreateRequest("game");
        var res = gameService.create(req);
        GameData game = gameDAO.getGame(res.gameID());
        Assertions.assertEquals(req.gameName(), game.gameName());
    }

    @Test
    public void createGameFailure() {
        Assertions.assertThrows(NullPointerException.class, () -> gameService.create(null));
    }

    @Test
    public void joinGameSuccess() {
        GameData game = new GameData(5, null, null, "test game", new ChessGame());
        gameDAO.createGame(game);
        AuthData authSession = new AuthData(UUID.randomUUID().toString(), "testuser");
        authDAO.createAuth(authSession);
        try {
            gameService.join(new GameJoinRequest("WHITE", game.gameID()), authSession.authToken());
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void joinGameFailure() {
        GameData game = new GameData(10, "whiteuser", null, "test game", new ChessGame());
        gameDAO.createGame(game);
        AuthData authSession = new AuthData(UUID.randomUUID().toString(), "testuser");
        authDAO.createAuth(authSession);
        Assertions.assertThrows(AlreadyTakenException.class, () -> {
            gameService.join(new GameJoinRequest("WHITE", game.gameID()), authSession.authToken());
        });
    }

}
