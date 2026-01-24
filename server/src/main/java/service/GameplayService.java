package service;

import chess.ChessGame;
import chess.InvalidMoveException;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import websocket.commands.MakeMoveCommand;

public class GameplayService {

    private GameDAO gameDAO;

    public GameplayService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public ChessGame makeMove(MakeMoveCommand command)
            throws ServerErrorException, DoesNotExistException, InvalidMoveException {
        GameData gameData;
        try {
            gameData = gameDAO.getGame(command.getGameID());
        } catch (DataAccessException e) {
            throw new ServerErrorException(e.getMessage());
        }
        if (gameData == null) {
            throw new DoesNotExistException("invalid game");
        }
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

}
