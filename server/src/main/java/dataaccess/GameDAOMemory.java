package dataaccess;

import model.GameData;
import model.UserData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GameDAOMemory implements GameDAO {
    private final Map<Integer, GameData> games = new HashMap<>();

    public void createGame(GameData gameData) {
        games.put(gameData.gameID(), gameData);
    }

    public GameData getGame(int gameID) throws DataAccessException{
        return games.get(gameID);
    }

    public Collection<GameData> listGames() throws DataAccessException {
        return games.values();
    }

    public void updateGame(GameData gameData) throws DataAccessException {
        // Overwrite the existing entry with updated GameData (e.g. player joined)
        games.put(gameData.gameID(), gameData);
    }

    public void clear() throws DataAccessException{
        try {
            games.clear();
        } catch (Exception e) {
            System.out.println("A data access exception occurred.");
        }
    }
}
