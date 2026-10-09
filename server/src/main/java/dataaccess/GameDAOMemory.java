package dataaccess;

import model.GameData;

public class GameDAOMemory implements GameDAO {
    public void clear() throws DataAccessException{
        try {
            GameData.clear();
        } catch (Exception e) {
            System.out.println("A data access exception occurred.");
        }
    }
}
