package dataaccess;

import java.util.ArrayList;
import java.util.Collection;

import model.GameData;

public class MemoryGameDAO implements GameDAO {
    private Collection<GameData> games;

    public MemoryGameDAO() {
        games = new ArrayList<>();
    }

    @Override
    public GameData createGame() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createGame'");
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
    public void updateGame(int gameID) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateGame'");
    }

    @Override
    public void clear() {
        games = new ArrayList<>();
    }

}
