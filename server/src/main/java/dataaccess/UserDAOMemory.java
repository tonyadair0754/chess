package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class UserDAOMemory implements UserDAO {
    private final Map<String, UserData> UserData = new HashMap<>();

    public void createUser(UserData userData) throws DataAccessException{
        UserData.put(userData.username(), userData);
        UserData.put(userData.password(), userData);
        UserData.put(userData.email(), userData);
    }

    public UserData getUser(String authToken) throws DataAccessException{
        return UserData.get(authToken);
    }

    public void clear() throws DataAccessException{
        try {
            UserData.clear();
        } catch (Exception e) {
            System.out.println("A data access exception occurred.");
        }
    }
}
