package dataaccess;

import model.AuthData;

public interface AuthDAO {
    void createAuth(AuthData authData);

    AuthData getAuth(String authToken);

    void deleteAuth(String authToken) throws DataAccessException;

    void clear();
}
