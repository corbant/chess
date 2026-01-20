package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;

import model.AuthData;

public class SQLAuthDAO extends AbstractSQLDAO implements AuthDAO {

    private final static String[] TABLE_CONFIG = {
            """
                    CREATE TABLE IF NOT EXISTS auth (
                    `token` VARCHAR(36) NOT NULL PRIMARY KEY,
                    `username` VARCHAR(256) NOT NULL
                    )
                            """
    };

    public SQLAuthDAO() {
        super();
    }

    @Override
    public void createAuth(AuthData authData) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("INSERT INTO auth (token, username) VALUES(?, ?)")) {
                preparedStatement.setString(1, authData.authToken());
                preparedStatement.setString(2, authData.username());

                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT token, username FROM auth WHERE token=?")) {
                preparedStatement.setString(1, authToken);

                try (var resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return new AuthData(resultSet.getString("token"), resultSet.getString("username"));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(ex.getMessage());
        }

        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("DELETE FROM auth WHERE token=?")) {
                preparedStatement.setString(1, authToken);

                int deleted = preparedStatement.executeUpdate();
                if (deleted == 0) {
                    throw new DataAccessException("Auth not found");
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("TRUNCATE auth")) {
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
