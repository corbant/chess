package dataaccess;

import java.util.ArrayList;
import java.util.Collection;

import model.GameData;

public class MemoryGameDAO implements GameDAO {
    private ArrayList<GameData> games;
    private int nextGameID;

    public MemoryGameDAO() {
        games = new ArrayList<>();
        nextGameID = 1;
    }

    @Override
    public int createGame(GameData gameData) {
        games.add(new GameData(nextGameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(),
                gameData.game(), null));
        return nextGameID++;
    }

    @Override
    public GameData getGame(int gameID) {
        for (GameData game : games) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        return null;
    }

    @Override
    public Collection<GameData> listGames() {
        return games;
    }

    @Override
    public void updateGame(GameData updatedGameData) throws DataAccessException {
        boolean removed = games.removeIf(gameData -> gameData.gameID() == updatedGameData.gameID());
        if (!removed) {
            throw new DataAccessException("Game not found");
        }
        games.add(updatedGameData);
    }

    @Override
    public void clear() {
        games.clear();
    }

}
