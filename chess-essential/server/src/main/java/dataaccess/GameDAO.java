package dataaccess;

import model.GameData;
import java.util.Collection;

// Data Access Object for game data
public interface GameDAO {
    int createGame(GameData g) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    Collection<GameData> listGames() throws DataAccessException;
    void updateGame(GameData g) throws DataAccessException;
    void clear() throws DataAccessException;
}

