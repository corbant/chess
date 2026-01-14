package service;

import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.UserData;
import model.AuthData;
import service.request.*;
import service.result.*;

public class UserService {
    private UserDAO userDAO;
    private AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws AlreadyTakenException {
        UserData existingUser = userDAO.getUser(registerRequest.username());
        if (existingUser != null) {
            throw new AlreadyTakenException("Username already taken");
        }
        String hashedPassword = BCrypt.hashpw(registerRequest.password(), BCrypt.gensalt());
        UserData newUser = new UserData(registerRequest.username(), hashedPassword, registerRequest.email());
        userDAO.createUser(newUser);

        AuthData authSession = createAuthSession(newUser.username());
        return new RegisterResult(authSession.username(), authSession.authToken());
    }

    public LoginResult login(LoginRequest loginRequest) throws UnauthorizedException {
        UserData user = userDAO.getUser(loginRequest.username());
        if (user == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        boolean validPassword = BCrypt.checkpw(loginRequest.password(), user.password());
        if (!validPassword) {
            throw new UnauthorizedException("Unauthorized");
        }

        AuthData authSession = createAuthSession(user.username());
        return new LoginResult(authSession.username(), authSession.authToken());
    }

    public void logout(LogoutRequest logoutRequest) throws UnauthorizedException, ServerErrorException {
        AuthData authSession = authDAO.getAuth(logoutRequest.authToken());
        if (authSession == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        try {
            authDAO.deleteAuth(authSession.authToken());
        } catch (DataAccessException e) {
            throw new ServerErrorException(e.getMessage());
        }
    }

    private AuthData createAuthSession(String username) {
        String authToken = UUID.randomUUID().toString();
        return new AuthData(authToken, username);
    }
}
