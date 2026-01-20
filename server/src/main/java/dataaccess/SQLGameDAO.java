package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import com.google.gson.Gson;

import model.GameData;

public class SQLGameDAO extends AbstractSQLDAO implements GameDAO {

    private final static String[] TABLE_CONFIG = {
            """
                    CREATE TABLE IF NOT EXISTS game (
                    `id` INT AUTO_INCREMENT PRIMARY KEY,
                    `white` VARCHAR(256) DEFAULT NULL,
                    `black` VARCHAR(256) DEFAULT NULL,
                    `name` VARCHAR(256) NOT NULL,
                    `game` TEXT NOT NULL
                    )
                            """
    };

    private final static Gson gson = new Gson();

    public SQLGameDAO() {
        super();
    }

    @Override
    public int createGame(GameData gameData) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(
                    "INSERT INTO game (white, black, name, game) VALUES(?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, gameData.whiteUsername());
                preparedStatement.setString(2, gameData.blackUsername());
                preparedStatement.setString(3, gameData.gameName());
                preparedStatement.setString(4, gson.toJson(gameData.game()));

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
    public GameData getGame(int gameID) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getGame'");
    }

    @Override
    public Collection<GameData> listGames() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listGames'");
    }

    @Override
    public void updateGame(GameData updatedGameData) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateGame'");
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clear'");
    }

    @Override
    protected String[] getTableConfig() {
        return TABLE_CONFIG;
    }

}
