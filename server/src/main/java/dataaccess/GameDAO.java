package dataaccess;

import java.util.Collection;

import model.GameData;

public interface GameDAO {

    GameData createGame();

    GameData getGame(int gameID);

    Collection<GameData> listGames();

    void updateGame(int gameID);
}
