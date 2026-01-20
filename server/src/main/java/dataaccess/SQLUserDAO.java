package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;

import model.UserData;

public class SQLUserDAO extends AbstractSQLDAO implements UserDAO {

    private final static String[] TABLE_CONFIG = {
            """
                    CREATE TABLE IF NOT EXISTS user (
                    `username` VARCHAR(256) NOT NULL PRIMARY KEY,
                    `password` VARCHAR(256) NOT NULL,
                    `email` VARCHAR(256) NOT NULL
                    )
                            """
    };

    public SQLUserDAO() {
        super();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn
                    .prepareStatement("INSERT INTO user (username, password, email) VALUES(?, ?, ?)")) {
                preparedStatement.setString(1, user.username());
                preparedStatement.setString(2, user.password());
                preparedStatement.setString(3, user.email());

                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn
                    .prepareStatement("SELECT username, password, email FROM user WHERE username=?")) {
                preparedStatement.setString(1, username);

                try (var resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return new UserData(resultSet.getString("username"), resultSet.getString("password"),
                                resultSet.getString("email"));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(ex.getMessage());
        }

        return null;
    }

    @Override
    public void clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("TRUNCATE user")) {
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
