package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;


import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTests {
    private static UserDAO userDao;
    private static AuthDAO authDao;

    @BeforeEach
    void setUp() throws DataAccessException {
        userDao = new UserDAOMySQL();
        authDao = new AuthDAOMySQL();

        userDao.clear();
        authDao.clear();
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

    void createAuthSuccess() throws Exception {
        AuthData auth = new AuthData("token-1", "mrMe");

        assertDoesNotThrow(() -> authDao.createAuth(auth));

        // Verify it was actually saved
        AuthData retrieved = authDao.getAuth("token-1");
        assertNotNull(retrieved);
        assertEquals("token-1", retrieved.authToken());
        assertEquals("mrMe", retrieved.username());
    }

    void getAuthSuccess() throws Exception {
        AuthData auth = new AuthData("token-1", "mrMe");
        authDao.createAuth(auth);

        AuthData retrieved = authDao.getAuth("token-1");
        assertNotNull(retrieved);
        assertEquals("mrMe", retrieved.username());
    }

    void getAuthNonexistent() throws Exception {
        AuthData retrieved = authDao.getAuth("invalid-token");
        assertNull(retrieved);
    }

    void deleteAuthSuccess() throws Exception {
        AuthData auth = new AuthData("token-1", "mrMe");
        authDao.createAuth(auth);

        assertNotNull(authDao.getAuth("token-1"));

        authDao.deleteAuth("token-1");

        assertNull(authDao.getAuth("token-1"));
    }

    void clearAuthTable() throws Exception {
        authDao.createAuth(new AuthData("token-1", "user1"));
        authDao.createAuth(new AuthData("token-2", "user2"));

        assertNotNull(authDao.getAuth("token-1"));
        assertNotNull(authDao.getAuth("token-2"));

        authDao.clear();

        assertNull(authDao.getAuth("token-1"));
        assertNull(authDao.getAuth("token-2"));
    }
}