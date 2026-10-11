package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {

    // Successful
    @Test
    public void registerSuccess() throws Exception {

        // 1. Arrange (Create objects and set up test data)
        UserDAO userDao = new UserDAOMemory();
        AuthDAO authDao = new AuthDAOMemory();

        UserService userService = new UserService(userDao, authDao);

        // Create sample data records
        RegisterRequest request = new RegisterRequest("mrMe", "password123", "mrMe@example.com");

        // 2. Act (Call the method you want to test)
        RegisterResult result = userService.register(request);

        // 3. Assert: verify result
        assertEquals("mrMe", result.username());
        assertNotNull(result.authToken());
        assertFalse(result.authToken().isBlank());

        // Assert: verify the user was stored
        UserData storedUser = userDao.getUser("mrMe");
        assertNotNull(storedUser);
        assertEquals("mrMe", storedUser.username());
        assertEquals("password123", storedUser.password());
        assertEquals("mrMe@example.com", storedUser.email());

        // Assert: verify the auth token was stored
        AuthData storedAuth = authDao.getAuth(result.authToken());
        assertNotNull(storedAuth);
        assertEquals("mrMe", storedAuth.username());
    }

    // Already Taken Exception
    @Test
    void usernameAlreadyTaken() throws Exception {
        // Arrange
        UserDAO userDao = new UserDAOMemory();
        AuthDAO authDao = new AuthDAOMemory();

        UserService userService = new UserService(userDao, authDao);

        RegisterRequest first = new RegisterRequest("mrMe", "password123", "mrMe@example.com");

        // Act
        userService.register(first);

        RegisterRequest duplicate = new RegisterRequest("mrMe", "differentPassword", "differentEmail@example.com");

        // Assert
        assertThrows(dataaccess.AlreadyTakenException.class, () -> userService.register(duplicate));
    }

    // Bad Request Exception
    @Test
    void registerMissingUsername() throws Exception {
        // Arrange
        UserDAO userDao = new UserDAOMemory();
        AuthDAO authDao = new AuthDAOMemory();

        UserService userService = new UserService(userDao, authDao);

        // Act
        RegisterRequest request = new RegisterRequest(null, "password123", "mrMe@example.com"
        );

        // Assert
        assertThrows(dataaccess.BadRequestException.class, () -> userService.register(request)
        );
    }
}