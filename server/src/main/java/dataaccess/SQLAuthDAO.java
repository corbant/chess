package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;

import model.AuthData;

public class SQLAuthDAO extends AbstractSQLDAO implements AuthDAO {

    private final static String[] TABLE_CONFIG = {
            """
                    CREATE TABLE IF NOT EXISTS auth (
                    `token` VARCHAR(32) NOT NULL PRIMARY KEY,
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
    public AuthData getAuth(String authToken) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAuth'");
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAuth'");
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
