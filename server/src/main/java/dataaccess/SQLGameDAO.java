package dataaccess;

import java.util.Collection;

import model.GameData;

public class SQLGameDAO implements GameDAO {

    private final static String[] TABLE_CONFIG = {
            """
                    CREATE TABLE IF NOT EXISTS game (
                    `id` INT AUTO_INCREMENT PRIMARY KEY,
                    `white` VARCHAR(256) DEFAULT NULL,
                    `black` VARCHAR(256) DEFAULT NULL,
                    `name` VARCHAR(256) NOT NULL,
                    `game` TEXT NOT NULL
                    )
                    AUTO_INCREMENT = 1
                            """
    };

    public SQLGameDAO() {
        configureTable();
    }

    @Override
    public void createGame(GameData gameData) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createGame'");
    }

    @Override
    public GameData getGame(int gameID) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getGame'");
    }

    @Override
    public Collection<GameData> listGames() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listGames'");
    }

    @Override
    public void updateGame(GameData updatedGameData) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateGame'");
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clear'");
    }

    private void configureTable() {

    }

}
