package dataaccess;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.AuthData;

public class AuthDAOTests {

    private static AuthDAO authDAO;

    @BeforeAll
    public static void setup() throws DataAccessException {
        DatabaseManager.createDatabase();
        authDAO = new SQLAuthDAO();
    }

    @BeforeEach
    public void reset() throws DataAccessException {
        authDAO.clear();
    }

    @Test
    public void createAuthSuccess() {
        AuthData firstAuth = new AuthData(UUID.randomUUID().toString(), "user1");
        AuthData secondAuth = new AuthData(UUID.randomUUID().toString(), "user2");
        try {
            authDAO.createAuth(firstAuth);
            authDAO.createAuth(secondAuth);
            Assertions.assertAll(() -> Assertions.assertEquals(firstAuth, authDAO.getAuth(firstAuth.authToken())),
                    () -> Assertions.assertEquals(secondAuth, authDAO.getAuth(secondAuth.authToken())));
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void createAuthFailure() {
        // invalid auth token
        AuthData authSession = new AuthData(UUID.randomUUID().toString() + "asdljbfkjnjsadfkj", "username");
        Assertions.assertThrows(DataAccessException.class, () -> authDAO.createAuth(authSession));
    }

    @Test
    public void getAuthSuccess() {
        AuthData authSession = new AuthData(UUID.randomUUID().toString(), "username");
        try {
            authDAO.createAuth(authSession);
            AuthData result = authDAO.getAuth(authSession.authToken());
            Assertions.assertNotNull(result);
            Assertions.assertEquals(authSession, result);
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void getAuthFailure() {
        AuthData authSession = new AuthData(UUID.randomUUID().toString(), "username");
        try {
            authDAO.createAuth(authSession);
            AuthData result = authDAO.getAuth(UUID.randomUUID().toString());
            Assertions.assertNull(result);
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void deleteAuthSuccess() {
        AuthData authSession = new AuthData(UUID.randomUUID().toString(), "username");
        try {
            authDAO.createAuth(authSession);
            AuthData result = authDAO.getAuth(authSession.authToken());
            Assertions.assertNotNull(result);
            authDAO.deleteAuth(authSession.authToken());
            result = authDAO.getAuth(authSession.authToken());
            Assertions.assertNull(result);
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void deleteAuthFailure() {
        String nonExistantUUID = UUID.randomUUID().toString();
        Assertions.assertThrows(DataAccessException.class, () -> authDAO.deleteAuth(nonExistantUUID));
    }

    @Test
    public void clear() {
        AuthData firstAuth = new AuthData(UUID.randomUUID().toString(), "user1");
        AuthData secondAuth = new AuthData(UUID.randomUUID().toString(), "user2");

        try {
            authDAO.createAuth(firstAuth);
            authDAO.createAuth(secondAuth);

            Assertions.assertNotNull(authDAO.getAuth(firstAuth.authToken()));
            Assertions.assertNotNull(authDAO.getAuth(secondAuth.authToken()));

            authDAO.clear();

            Assertions.assertNull(authDAO.getAuth(firstAuth.authToken()));
            Assertions.assertNull(authDAO.getAuth(secondAuth.authToken()));
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }
    }
}
