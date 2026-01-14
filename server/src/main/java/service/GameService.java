package service;

import java.util.Collection;

import chess.ChessGame;
import chess.ChessGame.TeamColor;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import service.request.GameCreateRequest;
import service.request.GameJoinRequest;
import service.request.GameListRequest;
import service.result.GameCreateResponse;
import service.result.GameListResponse;

public class GameService {
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private int nextGameID = 1;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public void join(GameJoinRequest gameJoinRequest)
            throws DoesNotExistException, BadRequestException, AlreadyTakenException, ServerErrorException {
        GameData gameData = gameDAO.getGame(gameJoinRequest.gameID());
        if (gameData == null) {
            throw new DoesNotExistException("Game does not exist");
        }
        TeamColor requestedColor;
        try {
            requestedColor = TeamColor.valueOf(gameJoinRequest.playerColor());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("playerColor is not a valid color");
        }
        String playerUsername = authDAO.getAuth(gameJoinRequest.authToken()).username();
        GameData updatedGameData;
        if (requestedColor == TeamColor.WHITE) {
            if (gameData.whiteUsername() != null) {
                throw new AlreadyTakenException("playerColor is already taken");
            }
            updatedGameData = new GameData(gameData.gameID(), playerUsername, gameData.blackUsername(),
                    gameData.gameName(), gameData.game());

        } else {
            if (gameData.blackUsername() != null) {
                throw new AlreadyTakenException("playerColor is already taken");
            }
            updatedGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), playerUsername,
                    gameData.gameName(), gameData.game());
        }
        try {
            gameDAO.updateGame(updatedGameData);
        } catch (DataAccessException e) {
            throw new ServerErrorException(e.getMessage());
        }
    }

    public GameCreateResponse create(GameCreateRequest gameCreateRequest) {
        GameData gameData = new GameData(nextGameID++, null, null, gameCreateRequest.gameName(), new ChessGame());
        gameDAO.createGame(gameData);
        return new GameCreateResponse(gameData.gameID());
    }

    public GameListResponse list(GameListRequest gameListRequest) {
        Collection<GameData> games = gameDAO.listGames();
        return new GameListResponse((GameData[]) games.toArray());
    }
}
