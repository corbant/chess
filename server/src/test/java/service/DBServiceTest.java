package service;

import java.util.UUID;

import org.junit.jupiter.api.*;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.GameData;
import model.UserData;

public class DBServiceTest {
    private static AuthDAO authDAO;
    private static UserDAO userDAO;
    private static GameDAO gameDAO;

    private static DBService dbService;

    @BeforeAll
    public static void init() {
        authDAO = new MemoryAuthDAO();
        userDAO = new MemoryUserDAO();
        gameDAO = new MemoryGameDAO();

        dbService = new DBService(authDAO, userDAO, gameDAO);
    }

    @Test
    public void clear() {
        try {
            UserData user = new UserData("username", "password", "email");
            userDAO.createUser(user);

            AuthData auth = new AuthData(UUID.randomUUID().toString(), "username");
            authDAO.createAuth(auth);

            GameData game = new GameData(1, "username", "username", "game name", new ChessGame(), null);
            gameDAO.createGame(game);

            dbService.clear();

            Assertions.assertNull(userDAO.getUser(user.username()));
            Assertions.assertNull(authDAO.getAuth(auth.authToken()));
            Assertions.assertNull(gameDAO.getGame(game.gameID()));
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }
}
