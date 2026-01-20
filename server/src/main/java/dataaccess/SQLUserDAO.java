package dataaccess;

import model.UserData;

public class SQLUserDAO implements UserDAO {

    private final static String[] TABLE_CONFIG = {
            """
                    CREATE TABLE IF NOT EXISTS user (
                    `username` VARCHAR(256) NOT NULL,
                    `password` VARCHAR(256) NOT NULL,
                    `email` VARCHAR(256) NOT NULL
                    )
                            """
    };

    public SQLUserDAO() {
        configureTable();
    }

    @Override
    public void createUser(UserData user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createUser'");
    }

    @Override
    public UserData getUser(String username) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUser'");
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clear'");
    }

    private void configureTable() {

    }

}
