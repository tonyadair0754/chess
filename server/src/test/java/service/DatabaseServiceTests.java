package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import chess.ChessGame;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseServiceTests {

    @Test
    public void clearSuccess() throws DataAccessException {

        // 1. Arrange (Create objects and set up test data)
        UserDAO userDao = new UserDAOMySQL();
        AuthDAO authDao = new AuthDAOMemory();
        GameDAO gameDao = new GameDAOMemory();

        DatabaseService databaseService = new DatabaseService(userDao, authDao, gameDao);

        // Create sample data records
        UserData testUser = new UserData("mrMe", "password123", "mrMe@example.com");
        AuthData testAuth = new AuthData("auth-token", "mrMe");
        GameData testGame = new GameData(1, "mrMe", "opponent", "My Game", new ChessGame());

        // Insert sample data into the DAOs
        userDao.createUser(testUser);
        authDao.createAuth(testAuth);
        gameDao.createGame(testGame);

        // Verify that the data was actually inserted before clearing
        assertNotNull(userDao.getUser("mrMe"));
        assertNotNull(authDao.getAuth("auth-token"));
        assertFalse(gameDao.listGames().isEmpty());

        // 2. Act (Call the method you want to test)
        databaseService.clear();

        // 3. Assert (Verify that the result matches what you expect)
        assertNull(userDao.getUser("mrMe"), "User should be null after clearing database");
        assertNull(authDao.getAuth("auth-token"), "Auth token should be null after clearing database");
        assertTrue(gameDao.listGames().isEmpty(), "Game list should be empty after clearing database");
    }
}