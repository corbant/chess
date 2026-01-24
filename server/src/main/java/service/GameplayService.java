package service;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessGame.TeamColor;
import chess.InvalidMoveException;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import model.GameResult;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

public class GameplayService {

    private GameDAO gameDAO;
    private AuthDAO authDAO;

    public GameplayService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
    }

    public ChessGame makeMove(MakeMoveCommand command, GameData gameData, AuthData authSession)
            throws ServerErrorException, InvalidMoveException, GameEndedException {
        if (gameData.result() != null) {
            throw new GameEndedException("this game has ended");
        }
        ChessGame game = gameData.game();
        ChessMove move = command.getMove();
        if (getUserTeamColor(authSession, gameData) != game.getTeamTurn()) {
            throw new InvalidMoveException("Not your turn");
        }
        game.makeMove(move);
        if (game.isInStalemate(TeamColor.WHITE)) {
            gameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(),
                    gameData.gameName(), game, GameResult.DRAW);
        } else if (game.isInCheckmate(TeamColor.WHITE)) {
            gameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(),
                    gameData.gameName(), game, GameResult.BLACK);
        } else if (game.isInCheckmate(TeamColor.BLACK)) {
            gameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(),
                    gameData.gameName(), game, GameResult.WHITE);
        }
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

    public TeamColor getUserTeamColor(AuthData authSession, GameData gameData) {
        String whiteUsername = gameData.whiteUsername();
        String blackUsername = gameData.blackUsername();
        String userUsername = authSession.username();
        return whiteUsername.equals(userUsername) ? TeamColor.WHITE
                : blackUsername.equals(userUsername) ? TeamColor.BLACK : null;
    }

}
