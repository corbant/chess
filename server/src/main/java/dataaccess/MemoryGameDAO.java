package dataaccess;

import java.util.ArrayList;
import java.util.Collection;

import model.GameData;

public class MemoryGameDAO implements GameDAO {
    private ArrayList<GameData> games;

    public MemoryGameDAO() {
        games = new ArrayList<>();
    }

    @Override
    public void createGame(GameData gameData) {
        games.add(gameData);
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
