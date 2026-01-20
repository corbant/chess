package service;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import dataaccess.AuthDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import service.request.LoginRequest;
import service.request.RegisterRequest;

public class UserServiceTests {

    private static UserService userService;
    private static UserDAO userDAO;
    private static AuthDAO authDAO;

    @BeforeAll
    public static void init() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    @BeforeEach
    public void reset() {
        try {
            if (userDAO != null) {
                userDAO.clear();
            }
            if (authDAO != null) {
                authDAO.clear();
            }
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void registerSuccess() {
        try {
            userService.register(new RegisterRequest("username", "password", "user@test.com"));
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void registerFailure() {
        try {
            userDAO.createUser(new UserData("username", "password", "user@test.com"));
            Assertions.assertThrows(AlreadyTakenException.class, () -> {
                userService.register(new RegisterRequest("username", "password", "user@test.com"));
            });
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void loginSuccess() {
        try {
            userDAO.createUser(new UserData("username", BCrypt.hashpw("password", BCrypt.gensalt()), "user@test.com"));
            userService.login(new LoginRequest("username", "password"));
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void loginFailure() {
        Assertions.assertThrows(UnauthorizedException.class, () -> {
            userService.login(new LoginRequest("username", "password"));
        });
    }

    @Test
    public void logoutSuccess() {
        try {
            AuthData authSession = new AuthData(UUID.randomUUID().toString(), "username");
            authDAO.createAuth(authSession);
            userService.logout(authSession.authToken());
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void logoutFailure() {
        Assertions.assertThrows(UnauthorizedException.class, () -> {
            userService.logout(UUID.randomUUID().toString());
        });
    }
}
