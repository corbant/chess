package dataaccess;

import model.AuthData;

public class SQLAuthDAO implements AuthDAO {

    private final static String[] TABLE_CONFIG = {
            """
                    CREATE TABLE IF NOT EXISTS auth (
                    `token` VARCHAR(32) NOT NULL,
                    `username` VARCHAR(256) NOT NULL
                    )
                            """
    };

    public SQLAuthDAO() {
        configureTable();
    }

    @Override
    public void createAuth(AuthData authData) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createAuth'");
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

    private void configureTable() {

    }
}
