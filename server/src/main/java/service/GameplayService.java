package service;

import java.util.List;

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
import service.result.CommandResult;
import service.result.OutboundWSMessage;
import service.result.OutboundWSMessage.Target;
import websocket.commands.ConnectCommand;
import websocket.commands.LeaveCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ResignCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public class GameplayService {

    private GameDAO gameDAO;
    private AuthDAO authDAO;

    public GameplayService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public CommandResult makeMove(MakeMoveCommand command)
            throws ServerErrorException, UnauthorizedException,
            DoesNotExistException {
        AuthData authSession = validateAuthSession(command);
        GameData gameData = getGameData(command);
        if (gameData.result() != null) {
            return new CommandResult(gameData.gameID(),
                    List.of(new OutboundWSMessage(Target.SELF, new ErrorMessage("Error: game ended"))));
        }
        ChessGame game = gameData.game();
        ChessMove move = command.getMove();
        if (getUserTeamColor(authSession, gameData) != game.getTeamTurn()) {
            return new CommandResult(gameData.gameID(),
                    List.of(new OutboundWSMessage(Target.SELF, new ErrorMessage("Error: invalid move"))));
        }
        try {
            game.makeMove(move);
        } catch (InvalidMoveException e) {
            return new CommandResult(gameData.gameID(),
                    List.of(new OutboundWSMessage(Target.SELF, new ErrorMessage("Error: invalid move"))));
        }

        GameResult gameResult = null;
        TeamColor inCheck = null;
        if (game.isInStalemate(TeamColor.WHITE)) {
            gameResult = GameResult.DRAW;
            gameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(),
                    gameData.gameName(), game, gameResult);
        } else if (game.isInCheckmate(TeamColor.WHITE)) {
            gameResult = GameResult.BLACK;
            gameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(),
                    gameData.gameName(), game, gameResult);
        } else if (game.isInCheckmate(TeamColor.BLACK)) {
            gameResult = GameResult.WHITE;
            gameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(),
                    gameData.gameName(), game, GameResult.BLACK);
        } else if (game.isInCheck(TeamColor.WHITE)) {
            inCheck = TeamColor.WHITE;
        } else if (game.isInCheck(TeamColor.BLACK)) {
            inCheck = TeamColor.BLACK;
        }

        try {
            gameDAO.updateGame(gameData);
        } catch (DataAccessException e) {
            throw new ServerErrorException(e.getMessage());
        }

        OutboundWSMessage checkMessage = null;
        if (gameResult != null) {
            switch (gameResult) {
                case DRAW:
                    checkMessage = new OutboundWSMessage(Target.ALL, new NotificationMessage("stalemate detected"));
                    break;
                case BLACK:
                    checkMessage = new OutboundWSMessage(Target.ALL, new NotificationMessage("black wins!"));
                    break;
                case WHITE:
                    checkMessage = new OutboundWSMessage(Target.ALL, new NotificationMessage("white wins!"));
                    break;
            }
        } else if (inCheck != null) {
            switch (inCheck) {
                case WHITE:
                    checkMessage = new OutboundWSMessage(Target.ALL, new NotificationMessage("white is in check"));
                case BLACK:
                    checkMessage = new OutboundWSMessage(Target.ALL, new NotificationMessage("black is in check"));
            }
        }
        return new CommandResult(gameData.gameID(),
                List.of(new OutboundWSMessage(Target.ALL, new LoadGameMessage(game)),
                        new OutboundWSMessage(Target.OTHERS, new NotificationMessage("move " + move.toString())),
                        checkMessage));
    }

    public CommandResult connect(ConnectCommand command)
            throws UnauthorizedException, DoesNotExistException, ServerErrorException {
        var authSession = validateAuthSession(command);
        GameData gameData = getGameData(command);

        TeamColor userTeamColor = getUserTeamColor(authSession, gameData);
        String joiningAs = userTeamColor != null ? userTeamColor.toString() : "OBSERVER";

        return new CommandResult(gameData.gameID(),
                List.of(new OutboundWSMessage(OutboundWSMessage.Target.SELF, new LoadGameMessage(gameData.game())),
                        new OutboundWSMessage(OutboundWSMessage.Target.OTHERS,
                                "join " + authSession.username() + " " + joiningAs)));
    }

    public CommandResult leaveGame(LeaveCommand command)
            throws UnauthorizedException, DoesNotExistException, ServerErrorException {
        AuthData authSession = validateAuthSession(command);
        GameData gameData = getGameData(command);

        TeamColor teamColor = getUserTeamColor(authSession, gameData);

        if (teamColor != null) {
            GameData updatedGameData;
            if (teamColor == TeamColor.WHITE) {
                updatedGameData = new GameData(gameData.gameID(), null, gameData.blackUsername(),
                        gameData.gameName(), gameData.game(), gameData.result());
            } else {
                updatedGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), null,
                        gameData.gameName(), gameData.game(), gameData.result());
            }

            try {
                gameDAO.updateGame(updatedGameData);
            } catch (DataAccessException e) {
                throw new ServerErrorException(e.getMessage());
            }
        }

        return new CommandResult(gameData.gameID(),
                List.of(new OutboundWSMessage(Target.OTHERS, "leave " + authSession.username())));
    }

    public CommandResult resign(ResignCommand command)
            throws UnauthorizedException, DoesNotExistException, ServerErrorException {
        AuthData authSession = validateAuthSession(command);
        GameData gameData = getGameData(command);

        TeamColor teamColor = getUserTeamColor(authSession, gameData);
        if (teamColor != null) {
            GameData updatedGameData;
            if (teamColor == TeamColor.WHITE) {
                updatedGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(),
                        gameData.gameName(),
                        gameData.game(), GameResult.BLACK);
            } else {
                updatedGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(),
                        gameData.gameName(),
                        gameData.game(), GameResult.WHITE);
            }
            try {
                gameDAO.updateGame(updatedGameData);
            } catch (DataAccessException e) {
                throw new ServerErrorException(e.getMessage());
            }
        } else {
            throw new UnauthorizedException("you cannot resign");
        }

        return new CommandResult(gameData.gameID(),
                List.of(new OutboundWSMessage(Target.OTHERS,
                        new NotificationMessage("resign " + authSession.username()))));
    }

    private GameData getGameData(UserGameCommand command) throws ServerErrorException, DoesNotExistException {
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

    private AuthData validateAuthSession(UserGameCommand command) throws ServerErrorException, UnauthorizedException {
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

    private TeamColor getUserTeamColor(AuthData authSession, GameData gameData) {
        String whiteUsername = gameData.whiteUsername();
        String blackUsername = gameData.blackUsername();
        String userUsername = authSession.username();
        return whiteUsername.equals(userUsername) ? TeamColor.WHITE
                : blackUsername.equals(userUsername) ? TeamColor.BLACK : null;
    }

}
