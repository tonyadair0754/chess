package dataaccess.sql;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTests {
    private static UserDAO userDao;
    private static AuthDAO authDao;
    private static GameDAO gameDao;

    @BeforeEach
    void setUp() throws DataAccessException {
        userDao = new UserDAOMySQL();
        authDao = new AuthDAOMySQL();
        gameDao = new GameDAOMySQL();

        userDao.clear();
        authDao.clear();
        gameDao.clear();
    }

    // --- UserDAO database tests ---

    @Test
    void connectionWorks() throws Exception {
        DatabaseManager.createDatabase();

        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT 1+1")) {
                try (var rs = preparedStatement.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(2, rs.getInt(1));
                }
            }
        }
    }

    @Test
    void retrieveUser() throws Exception {
        UserData user = new UserData("mrMe", "password123", "mrMe@example.com");
        userDao.createUser(user);

        UserData retrieved = userDao.getUser("mrMe");

        assertNotNull(retrieved);
        assertEquals("mrMe", retrieved.username());
        assertEquals("password123", retrieved.password());
        assertEquals("mrMe@example.com", retrieved.email());
    }

    @Test
    void retrieveNonexistentUser() throws Exception {
        UserData retrieved = userDao.getUser("invalidUser");
        assertNull(retrieved);
    }

    @Test
    void clearUserTable() throws Exception {
        UserData user = new UserData("mrMe", "password123", "mrMe@example.com");
        userDao.createUser(user);

        assertNotNull(userDao.getUser("mrMe"));

        userDao.clear();

        assertNull(userDao.getUser("mrMe"));
    }

    @Test
    void duplicateUsernames() throws Exception {
        UserData user1 = new UserData("mrMe", "password123", "mrMe@example.com");
        UserData user2 = new UserData("mrMe", "password123", "mrMe@example.com");

        assertDoesNotThrow(() -> userDao.createUser(user1));

        assertThrows(DataAccessException.class, () -> {
            userDao.createUser(user2);
        });
    }

    // --- AuthDAO database tests ---

    @Test
    void createAuthSuccess() throws Exception {
        AuthData auth = new AuthData("token-1", "mrMe");

        assertDoesNotThrow(() -> authDao.createAuth(auth));

        // Verify it was actually saved
        AuthData retrieved = authDao.getAuth("token-1");
        assertNotNull(retrieved);
        assertEquals("token-1", retrieved.authToken());
        assertEquals("mrMe", retrieved.username());
    }

    @Test
    void getAuthSuccess() throws Exception {
        AuthData auth = new AuthData("token-1", "mrMe");
        authDao.createAuth(auth);

        AuthData retrieved = authDao.getAuth("token-1");
        assertNotNull(retrieved);
        assertEquals("mrMe", retrieved.username());
    }

    @Test
    void getAuthNonexistent() throws Exception {
        AuthData retrieved = authDao.getAuth("invalid-token");
        assertNull(retrieved);
    }

    @Test
    void deleteAuthSuccess() throws Exception {
        AuthData auth = new AuthData("token-1", "mrMe");
        authDao.createAuth(auth);

        assertNotNull(authDao.getAuth("token-1"));

        authDao.deleteAuth("token-1");

        assertNull(authDao.getAuth("token-1"));
    }

    @Test
    void clearAuthTable() throws Exception {
        authDao.createAuth(new AuthData("token-1", "user1"));
        authDao.createAuth(new AuthData("token-2", "user2"));

        assertNotNull(authDao.getAuth("token-1"));
        assertNotNull(authDao.getAuth("token-2"));

        authDao.clear();

        assertNull(authDao.getAuth("token-1"));
        assertNull(authDao.getAuth("token-2"));
    }

    // --- Game DAO tests ---

    @Test
    void createGameSuccess() throws Exception {
        GameData newGame = new GameData(0, "", "", "New Game", new chess.ChessGame());
        gameDao.createGame(newGame);

        Collection<GameData> games = gameDao.listGames();
        assertEquals(1, games.size());
        GameData game = games.iterator().next();

        assertNotNull(game);
        assertEquals("New Game", game.gameName());
        assertNotNull(game.game()); // Verify ChessGame object deserialized properly
    }

    @Test
    void getGameNonexistent() throws Exception {
        GameData game = gameDao.getGame(999999);
        assertNull(game);
    }

    @Test
    void listGamesSuccess() throws Exception {
        gameDao.createGame(new GameData(0, "", "", "Game One", new chess.ChessGame()));
        gameDao.createGame(new GameData(0, "", "", "Game Two", new chess.ChessGame()));

        Collection<GameData> games = gameDao.listGames();
        assertEquals(2, games.size());
    }

    @Test
    void updateGameSuccess() throws Exception {
        GameData newGame = new GameData(0, "", "", "Original Name", new chess.ChessGame());
        gameDao.createGame(newGame);

        // Retrieve the GameData object to get the gameID assigned by the database
        GameData original = gameDao.listGames().iterator().next();

        // Create updated game data (e.g. adding a white player and changing name)
        GameData updated = new GameData(
                original.gameID(),
                "mrMe",
                null,
                "Updated Name",
                original.game()
        );

        gameDao.updateGame(updated);

        GameData retrieved = gameDao.getGame(original.gameID());
        assertEquals("mrMe", retrieved.whiteUsername());
        assertEquals("Updated Name", retrieved.gameName());
    }

    @Test
    void clearGames() throws Exception {
        gameDao.createGame(new GameData(0, "", "", "New Game", new chess.ChessGame()));
        assertFalse(gameDao.listGames().isEmpty());

        gameDao.clear();
        assertTrue(gameDao.listGames().isEmpty());
    }
}