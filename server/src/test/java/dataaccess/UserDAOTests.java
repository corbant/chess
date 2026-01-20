package dataaccess;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.UserData;

public class UserDAOTests {

    private static UserDAO userDAO;

    @BeforeAll
    public static void setup() throws DataAccessException {
        DatabaseManager.createDatabase();
        userDAO = new SQLUserDAO();
    }

    @BeforeEach
    public void reset() throws DataAccessException {
        userDAO.clear();
    }

    @Test
    public void createUserSuccess() {
        UserData user = new UserData("username", "password", "user@test.com");

        try {
            userDAO.createUser(user);

            UserData dbUser = userDAO.getUser(user.username());

            Assertions.assertNotNull(dbUser);
            Assertions.assertEquals(user, dbUser);
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void createUserFailure() {
        UserData user = new UserData(null, null, null);

        Assertions.assertThrows(DataAccessException.class, () -> userDAO.createUser(user));

        UserData validUser = new UserData("username", "password", "user@test.com");

        try {
            userDAO.createUser(validUser);

            Assertions.assertThrows(DataAccessException.class, () -> userDAO.createUser(validUser));
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }

    }

    @Test
    public void getUserSuccess() {
        UserData user1 = new UserData("username1", "password", "user@test.com");
        UserData user2 = new UserData("username2", "password", "user@test.com");

        try {
            userDAO.createUser(user1);
            userDAO.createUser(user2);

            UserData dbUser1 = userDAO.getUser(user1.username());
            UserData dbUser2 = userDAO.getUser(user2.username());

            Assertions.assertEquals(user1, dbUser1);
            Assertions.assertEquals(user2, dbUser2);
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void getUserFailure() {
        UserData user = new UserData("username", "password", "user@test.com");

        try {
            userDAO.createUser(user);

            UserData nonexistentUser = userDAO.getUser("nonexistentusername");

            Assertions.assertNull(nonexistentUser);
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }

    }

    @Test
    public void clear() {
        UserData[] users = {
                new UserData("username1", "password", "user@test.com"),
                new UserData("username2", "password", "user@test.com"),
                new UserData("username3", "password", "user@test.com"),
        };

        try {
            for (UserData user : users) {
                userDAO.createUser(user);
            }

            for (UserData user : users) {
                Assertions.assertNotNull(userDAO.getUser(user.username()));
            }

            userDAO.clear();

            for (UserData user : users) {
                Assertions.assertNull(userDAO.getUser(user.username()));
            }
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }

    }

}
