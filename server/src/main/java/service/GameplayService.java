package service;

import chess.ChessGame;
import chess.InvalidMoveException;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

public class GameplayService {

    private GameDAO gameDAO;
    private AuthDAO authDAO;

    public GameplayService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public ChessGame makeMove(MakeMoveCommand command, GameData gameData)
            throws ServerErrorException, InvalidMoveException {
        ChessGame game = gameData.game();
        game.makeMove(command.getMove());
        try {
            gameDAO.updateGame(gameData);
        } catch (DataAccessException e) {
            throw new ServerErrorException(e.getMessage());
        }
        return game;
    }

    public ChessGame getChessGame(int gameID) throws ServerErrorException, DoesNotExistException {
        GameData gameData;
        try {
            gameData = gameDAO.getGame(gameID);
        } catch (DataAccessException e) {
            throw new ServerErrorException(e.getMessage());
        }
        if (gameData == null) {
            throw new DoesNotExistException("invalid game");
        }
        ChessGame game = gameData.game();
        return game;
    }

    public GameData getGameData(UserGameCommand command) throws ServerErrorException, DoesNotExistException {
        int gameID = command.getGameID();
        GameData gameData = null;
        try {
            gameData = gameDAO.getGame(gameID);
        } catch (DataAccessException e) {
            throw new ServerErrorException(e.getMessage());
        }
        if (gameData == null) {
            throw new DoesNotExistException("game does not exist");
        }
        return gameData;
    }

    public AuthData validateAuthSession(UserGameCommand command) throws ServerErrorException, UnauthorizedException {
        AuthData authSession = null;
        if (command.getAuthToken() == null) {
            throw new UnauthorizedException("unauthorized");
        }
        try {
            authSession = authDAO.getAuth(command.getAuthToken());
        } catch (DataAccessException e) {
            throw new ServerErrorException(e.getMessage());
        }
        if (authSession == null) {
            throw new UnauthorizedException("unauthorized");
        }
        return authSession;
    }

}
