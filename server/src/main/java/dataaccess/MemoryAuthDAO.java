package dataaccess;

import java.util.ArrayList;

import model.AuthData;

public class MemoryAuthDAO implements AuthDAO {
    private ArrayList<AuthData> authSessions;

    public MemoryAuthDAO() {
        authSessions = new ArrayList<>();
    }

    @Override
    public void createAuth(AuthData auth) {
        authSessions.add(auth);
    }

    @Override
    public AuthData getAuth(String authToken) {
        for (AuthData authSession : authSessions) {
            if (authSession.authToken() == authToken) {
                return authSession;
            }
        }
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        boolean removed = authSessions.removeIf(authSession -> authSession.authToken() == authToken);
        if (!removed) {
            throw new DataAccessException("Auth not found");
        }
    }

    @Override
    public void clear() {
        authSessions.clear();
    }

}
