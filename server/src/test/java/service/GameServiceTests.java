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
        try {
            if (gameDAO != null) {
                gameDAO.clear();
            }
            if (authDAO != null) {
                authDAO.clear();
            }
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void listGamesSuccess() {
        try {
            int id1 = gameDAO.createGame(new GameData(0, "username", "username", "game 1", new ChessGame(), null));
            int id2 = gameDAO.createGame(new GameData(0, "username", "username", "game 2", new ChessGame(), null));
            int id3 = gameDAO.createGame(new GameData(0, "username", "username", "game 3", new ChessGame(), null));

            GameData[] expected = new GameData[] {
                    new GameData(id1, "username", "username", "game 1", new ChessGame(), null),
                    new GameData(id2, "username", "username", "game 2", new ChessGame(), null),
                    new GameData(id3, "username", "username", "game 3", new ChessGame(), null)
            };

            Assertions.assertArrayEquals(expected, gameService.list().games());
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void listGamesFailure() {
        try {
            GameData[] games = gameService.list().games();
            Assertions.assertNotNull(games);
            Assertions.assertEquals(0, games.length);
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void createGameSuccess() {
        try {
            GameCreateRequest req = new GameCreateRequest("game");
            var res = gameService.create(req);
            GameData game = gameDAO.getGame(res.gameID());
            Assertions.assertEquals(req.gameName(), game.gameName());
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void createGameFailure() {
        Assertions.assertThrows(NullPointerException.class, () -> gameService.create(null));
    }

    @Test
    public void joinGameSuccess() {
        try {
            int gameID = gameDAO.createGame(new GameData(0, null, null, "test game", new ChessGame(), null));
            AuthData authSession = new AuthData(UUID.randomUUID().toString(), "testuser");
            authDAO.createAuth(authSession);
            gameService.join(new GameJoinRequest("WHITE", gameID), authSession.authToken());
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void joinGameFailure() {
        try {
            int gameID = gameDAO.createGame(new GameData(0, "whiteuser", null, "test game", new ChessGame(), null));
            AuthData authSession = new AuthData(UUID.randomUUID().toString(), "testuser");
            authDAO.createAuth(authSession);
            Assertions.assertThrows(AlreadyTakenException.class, () -> {
                gameService.join(new GameJoinRequest("WHITE", gameID), authSession.authToken());
            });
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

}
