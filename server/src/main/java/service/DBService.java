package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;

public class DBService {
    private AuthDAO authDAO;
    private UserDAO userDAO;
    private GameDAO gameDAO;

    public DBService(AuthDAO authDAO, UserDAO userDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.userDAO = userDAO;
        this.gameDAO = gameDAO;
    }

    public void clear() throws ServerErrorException {
        try {
            authDAO.clear();
            userDAO.clear();
            gameDAO.clear();
        } catch (DataAccessException ex) {
            throw new ServerErrorException(ex.getMessage());
        }

    }
}
