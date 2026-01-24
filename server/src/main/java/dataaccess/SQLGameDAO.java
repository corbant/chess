package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import com.google.gson.Gson;

import chess.ChessGame;
import model.GameData;
import model.GameResult;

public class SQLGameDAO extends AbstractSQLDAO implements GameDAO {

    private final static String[] TABLE_CONFIG = {
            """
                    CREATE TABLE IF NOT EXISTS game (
                    `id` INT AUTO_INCREMENT PRIMARY KEY,
                    `white` VARCHAR(256) DEFAULT NULL,
                    `black` VARCHAR(256) DEFAULT NULL,
                    `name` VARCHAR(256) NOT NULL,
                    `game` TEXT NOT NULL,
                    `result` VARCHAR(10) DEFAULT NULL
                    )
                            """
    };

    private final static Gson GSON = new Gson();

    public SQLGameDAO() {
        super();
    }

    @Override
    public int createGame(GameData gameData) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(
                    "INSERT INTO game (white, black, name, game, result) VALUES(?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, gameData.whiteUsername());
                preparedStatement.setString(2, gameData.blackUsername());
                preparedStatement.setString(3, gameData.gameName());
                preparedStatement.setString(4, GSON.toJson(gameData.game()));
                preparedStatement.setString(5, gameData.result() != null ? gameData.result().toString() : null);

                preparedStatement.executeUpdate();

                var resultSet = preparedStatement.getGeneratedKeys();
                int id = 0;
                if (resultSet.next()) {
                    id = resultSet.getInt(1);
                }

                return id;
            }
        } catch (SQLException ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn
                    .prepareStatement("SELECT id, white, black, name, game, result FROM game WHERE id=?")) {
                preparedStatement.setInt(1, gameID);

                try (var resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String gameResultValue = resultSet.getString("result");
                        GameResult gameResult = gameResultValue != null ? GameResult.valueOf(gameResultValue) : null;
                        return new GameData(resultSet.getInt("id"), resultSet.getString("white"),
                                resultSet.getString("black"), resultSet.getString("name"), GSON.fromJson(
                                        resultSet.getString("game"), ChessGame.class),
                                gameResult);
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(ex.getMessage());
        }

        return null;
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        Collection<GameData> games = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn
                    .prepareStatement("SELECT id, white, black, name, game, result FROM game")) {

                try (var resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String gameResultValue = resultSet.getString("result");
                        GameResult gameResult = gameResultValue != null ? GameResult.valueOf(gameResultValue) : null;
                        games.add(new GameData(resultSet.getInt("id"), resultSet.getString("white"),
                                resultSet.getString("black"), resultSet.getString("name"), GSON.fromJson(
                                        resultSet.getString("game"), ChessGame.class),
                                gameResult));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(ex.getMessage());
        }

        return games;
    }

    @Override
    public void updateGame(GameData updatedGameData) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(
                    "UPDATE game SET white=?, black=?, name=?, game=?, result=? WHERE id=?")) {
                preparedStatement.setString(1, updatedGameData.whiteUsername());
                preparedStatement.setString(2, updatedGameData.blackUsername());
                preparedStatement.setString(3, updatedGameData.gameName());
                preparedStatement.setString(4, GSON.toJson(updatedGameData.game()));
                preparedStatement.setString(5,
                        updatedGameData.result() != null ? updatedGameData.result().toString() : null);
                preparedStatement.setInt(6, updatedGameData.gameID());

                int updated = preparedStatement.executeUpdate();
                if (updated == 0) {
                    throw new DataAccessException("Game not found");
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("TRUNCATE game")) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    @Override
    protected String[] getTableConfig() {
        return TABLE_CONFIG;
    }

}
