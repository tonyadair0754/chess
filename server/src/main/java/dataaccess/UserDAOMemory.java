package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class UserDAOMemory implements UserDAO {
    private final Map<UserData, String> UserData = new HashMap<>();

    public void clear() throws DataAccessException{
        try {
            UserData.clear();
        } catch (Exception e) {
            System.out.println("A data access exception occurred.");
        }
    }
}
