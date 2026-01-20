package dataaccess;

import java.util.Collection;

import model.GameData;

public interface GameDAO {

    int createGame(GameData gameData);

    GameData getGame(int gameID);

    Collection<GameData> listGames();

    void updateGame(GameData updatedGameData) throws DataAccessException;

    void clear();
}
