package dataaccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import model.AuthData;

public class MemoryAuthDAO implements AuthDAO {
    private Collection<AuthData> authSessions;

    public MemoryAuthDAO() {
        authSessions = new ArrayList<>();
    }

    @Override
    public AuthData createAuth(String username) {
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, username);
        authSessions.add(authData);
        return authData;
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
    public void deleteAuth(String authToken) {
        authSessions.removeIf(authSession -> authSession.authToken() == authToken);
    }

    @Override
    public void clear() {
        authSessions = new ArrayList<>();
    }

}
