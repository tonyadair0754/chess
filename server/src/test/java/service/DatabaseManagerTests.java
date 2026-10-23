package service;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.UserDAO;
import dataaccess.UserDAOMySQL;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTests {
    private static UserDAO userDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        userDAO = new UserDAOMySQL();
        userDAO.clear();
    }

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
        userDAO.createUser(user);

        UserData retrieved = userDAO.getUser("mrMe");

        assertNotNull(retrieved);
        assertEquals("mrMe", retrieved.username());
        assertEquals("password123", retrieved.password());
        assertEquals("mrMe@example.com", retrieved.email());
    }

    @Test
    void retrieveNonexistentUser() throws Exception {
        UserData retrieved = userDAO.getUser("invalidUser");
        assertNull(retrieved);
    }

    @Test
    void clearTable() throws Exception {
        UserData user = new UserData("mrMe", "password123", "mrMe@example.com");
        userDAO.createUser(user);

        assertNotNull(userDAO.getUser("mrMe"));

        userDAO.clear();

        assertNull(userDAO.getUser("mrMe"));
    }

    @Test
    void duplicateUsernames() throws Exception {
        UserData user1 = new UserData("mrMe", "password123", "mrMe@example.com");
        UserData user2 = new UserData("mrMe", "password123", "mrMe@example.com");

        assertDoesNotThrow(() -> userDAO.createUser(user1));

        assertThrows(DataAccessException.class, () -> {
            userDAO.createUser(user2);
        });
    }
}