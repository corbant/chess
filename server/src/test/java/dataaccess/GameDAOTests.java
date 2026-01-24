package dataaccess;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import model.GameData;

public class GameDAOTests {

    private static GameDAO gameDAO;

    @BeforeAll
    public static void setup() throws DataAccessException {
        DatabaseManager.createDatabase();
        gameDAO = new SQLGameDAO();
    }

    @BeforeEach
    public void reset() throws DataAccessException {
        gameDAO.clear();
    }

    @Test
    public void createGameSuccess() {
        GameData game = new GameData(0, null, null, "game", new ChessGame(), null);

        try {
            int gameID = gameDAO.createGame(game);

            game = new GameData(gameID, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game(),
                    game.result());

            GameData dbGame = gameDAO.getGame(gameID);

            Assertions.assertNotNull(dbGame);
            Assertions.assertEquals(game, dbGame);
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void createGameFailure() {
        // game name can't be null
        GameData invalidGame = new GameData(0, null, null, null, new ChessGame(), null);
        Assertions.assertThrows(DataAccessException.class, () -> gameDAO.createGame(invalidGame));
    }

    @Test
    public void getGameSuccess() {
        GameData game1 = new GameData(0, "white1", "black1", "firstgame", new ChessGame(), null);
        GameData game2 = new GameData(0, "white2", "black2", "firstgame", new ChessGame(), null);
        try {
            int gameID1 = gameDAO.createGame(game1);
            int gameID2 = gameDAO.createGame(game2);

            game1 = new GameData(gameID1, game1.whiteUsername(), game1.blackUsername(), game1.gameName(), game1.game(),
                    game1.result());
            game2 = new GameData(gameID2, game2.whiteUsername(), game2.blackUsername(), game2.gameName(), game2.game(),
                    game2.result());

            GameData dbGame1 = gameDAO.getGame(gameID1);
            GameData dbGame2 = gameDAO.getGame(gameID2);

            Assertions.assertNotNull(dbGame1);
            Assertions.assertNotNull(dbGame2);

            Assertions.assertEquals(game1, dbGame1);
            Assertions.assertEquals(game2, dbGame2);
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void getGameFailure() {
        GameData game = new GameData(0, null, null, "game", new ChessGame(), null);

        try {
            gameDAO.createGame(game);

            Assertions.assertNull(gameDAO.getGame(-1));
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void listGamesSuccess() {
        GameData[] games = {
                new GameData(0, "white1", "black1", "firstgame", new ChessGame(), null),
                new GameData(0, "white2", "black2", "firstgame", new ChessGame(), null),
                new GameData(0, "white3", "black3", "firstgame", new ChessGame(), null)
        };

        try {
            for (int i = 0; i < games.length; i++) {
                int gameID = gameDAO.createGame(games[i]);
                games[i] = new GameData(gameID, games[i].whiteUsername(), games[i].blackUsername(), games[i].gameName(),
                        games[i].game(), games[i].result());
            }

            var dbGames = gameDAO.listGames();

            Assertions.assertArrayEquals(games, dbGames.toArray());
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void listGamesFailure() {
        try {
            var games = gameDAO.listGames();

            Assertions.assertTrue(games.isEmpty());
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void updateGameSuccess() {
        ChessGame gameState = new ChessGame();
        GameData game = new GameData(0, null, null, "game", gameState, null);

        try {
            int gameID = gameDAO.createGame(game);
            GameData updatedGame = new GameData(gameID, "username", game.blackUsername(), game.gameName(), game.game(),
                    game.result());

            gameDAO.updateGame(updatedGame);

            GameData dbUpdatedGame = gameDAO.getGame(gameID);

            Assertions.assertEquals(updatedGame, dbUpdatedGame);

            // move pawn
            try {
                gameState.makeMove(new ChessMove(new ChessPosition(2, 1), new ChessPosition(3, 1)));
            } catch (InvalidMoveException e) {

            }

            Assertions.assertNotEquals(gameState, dbUpdatedGame.game());

            GameData gameWithChessMove = new GameData(gameID, updatedGame.whiteUsername(), updatedGame.blackUsername(),
                    updatedGame.gameName(), gameState, updatedGame.result());
            gameDAO.updateGame(gameWithChessMove);

            GameData dbGameWithChessMove = gameDAO.getGame(gameID);
            Assertions.assertEquals(gameState, dbGameWithChessMove.game());
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void updateGameFailure() {
        GameData gameToUpdate = new GameData(-1, null, null, "game", new ChessGame(), null);

        Assertions.assertThrows(DataAccessException.class, () -> gameDAO.updateGame(gameToUpdate));

        try {
            int gameID = gameDAO.createGame(gameToUpdate);

            GameData updatedGame = new GameData(gameID, gameToUpdate.whiteUsername(), gameToUpdate.blackUsername(),
                    null,
                    gameToUpdate.game(), gameToUpdate.result());
            Assertions.assertThrows(DataAccessException.class, () -> gameDAO.updateGame(updatedGame));
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void clear() {
        GameData[] games = {
                new GameData(0, "white1", "black1", "firstgame", new ChessGame(), null),
                new GameData(0, "white2", "black2", "firstgame", new ChessGame(), null),
                new GameData(0, "white3", "black3", "firstgame", new ChessGame(), null)
        };

        try {
            for (GameData game : games) {
                gameDAO.createGame(game);
            }
            Assertions.assertFalse(gameDAO.listGames().isEmpty());

            gameDAO.clear();

            Assertions.assertTrue(gameDAO.listGames().isEmpty());
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }
    }

}
