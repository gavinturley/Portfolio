package dataaccess;

import model.GameData;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryGameDAO implements GameDAO{
    private final Map<Integer, GameData> games = new HashMap<>();
    private final AtomicInteger nextID = new AtomicInteger(1);

    @Override
    public int createGame(GameData game){
        int id = nextID.getAndIncrement();
        GameData withID = game.withGameID(id);

        games.put(id, withID);
        return id;
    }

    @Override
    public GameData getGame(int id){
        return games.get(id);
    }

    @Override
    public Collection<GameData> listGames(){
        return games.values();
    }

    @Override
    public void updateGame(GameData game){
        games.put(game.gameID(), game);
    }

    @Override
    public void clear(){
        games.clear();
    }
}