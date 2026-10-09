package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class UserDAOMemory implements UserDAO {
    private final Map<String, UserData> userData = new HashMap<>();

    public void createUser(UserData user) throws DataAccessException{
        userData.put(user.username(), user);
    }

    public UserData getUser(String username) throws DataAccessException{
        return userData.get(username);
    }

    public void clear() throws DataAccessException{
        userData.clear();
    }
}
