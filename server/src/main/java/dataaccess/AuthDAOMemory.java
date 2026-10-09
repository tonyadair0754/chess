package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;

public class AuthDAOMemory implements AuthDAO {
    private final Map<String, AuthData> authTokens = new HashMap<>();

    public void createAuth(AuthData auth) {
        authTokens.put(auth.authToken(), auth);
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        return authTokens.get(authToken);
    }

    public void deleteAuth(String authToken) throws DataAccessException {
        authTokens.remove(authToken);
    }

    public void clear() throws DataAccessException {
        try {
            authTokens.clear();
        } catch (Exception e) {
            System.out.println("A data access exception occurred.");
        }
    }
}
