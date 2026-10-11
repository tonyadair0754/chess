package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {

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

    @Test
    void registerUsernameAlreadyTaken() throws Exception {
        // Arrange
        UserDAO userDao = new UserDAOMemory();
        AuthDAO authDao = new AuthDAOMemory();

        UserService userService = new UserService(userDao, authDao);

        RegisterRequest first = new RegisterRequest("mrMe", "password123", "mrMe@example.com");
        userService.register(first);

        RegisterRequest duplicate = new RegisterRequest("mrMe", "differentPassword", "differentEmail@example.com");

        // Act and assert
        assertThrows(dataaccess.AlreadyTakenException.class, () -> userService.register(duplicate));
    }

    @Test
    void registerMissingUsername() throws Exception {
        // Arrange
        UserDAO userDao = new UserDAOMemory();
        AuthDAO authDao = new AuthDAOMemory();

        UserService userService = new UserService(userDao, authDao);
        RegisterRequest request = new RegisterRequest(null, "password123", "mrMe@example.com"
        );

        // Act and assert
        assertThrows(dataaccess.BadRequestException.class, () -> userService.register(request)
        );
    }

    @Test
    public void loginSuccess() throws Exception {

        // Arrange
        UserDAO userDao = new UserDAOMemory();
        AuthDAO authDao = new AuthDAOMemory();
        UserService userService = new UserService(userDao, authDao);

        // Register a user and prepare login request
        userService.register(new RegisterRequest("mrMe", "password123", "mrMe@example.com"));
        LoginRequest request = new LoginRequest("mrMe", "password123");

        // Act
        LoginResult result = userService.login(request);

        // Assert: verify the result
        assertEquals("mrMe", result.username());
        assertNotNull(result.authToken());
        assertFalse(result.authToken().isBlank());

        // Assert: verify the auth token was stored
        AuthData storedAuth = authDao.getAuth(result.authToken());
        assertNotNull(storedAuth);
        assertEquals("mrMe", storedAuth.username());
    }

    @Test
    void loginIncorrectPassword() throws Exception {
        // Arrange
        UserDAO userDao = new UserDAOMemory();
        AuthDAO authDao = new AuthDAOMemory();
        UserService userService = new UserService(userDao, authDao);

        userService.register(new RegisterRequest("mrMe", "password123", "mrMe@example.com"));
        LoginRequest request = new LoginRequest("mrMe", "incorrectPassword");

        // Act and assert
        assertThrows(UnauthorizedException.class, () -> userService.login(request));
    }

    @Test
    void loginUnknownUsername() throws Exception {
        // Arrange
        UserDAO userDao = new UserDAOMemory();
        AuthDAO authDao = new AuthDAOMemory();
        UserService userService = new UserService(userDao, authDao);

        LoginRequest request = new LoginRequest("unknownUser", "password123");

        // Act and assert
        assertThrows(UnauthorizedException.class, () -> userService.login(request));
    }

    @Test
    void loginMissingPassword() throws Exception {
        UserDAO userDao = new UserDAOMemory();
        AuthDAO authDao = new AuthDAOMemory();
        UserService userService = new UserService(userDao, authDao);

        LoginRequest request = new LoginRequest("mrMe", null);

        assertThrows(BadRequestException.class, () -> userService.login(request));
    }

    @Test
    public void logoutSuccess() throws Exception {

        // Arrange
        UserDAO userDao = new UserDAOMemory();
        AuthDAO authDao = new AuthDAOMemory();
        UserService userService = new UserService(userDao, authDao);

        // Register and login a user to obtain an auth token
        userService.register(new RegisterRequest("mrMe", "password123", "mrMe@example.com"));
        LoginRequest loginRequest = new LoginRequest("mrMe", "password123");
        LoginResult loginResult = userService.login(loginRequest);

        String token = loginResult.authToken();
        LogoutRequest logoutRequest = new LogoutRequest(token);

        // Verify token exists before trying to logout
        assertNotNull(authDao.getAuth(token));

        // Act
        userService.logout(logoutRequest);

        // Assert: the token should no longer exist
        assertNull(authDao.getAuth(token));
    }

    @Test
    void logoutInvalidToken() throws Exception {
        // Arrange
        UserDAO userDao = new UserDAOMemory();
        AuthDAO authDao = new AuthDAOMemory();
        UserService userService = new UserService(userDao, authDao);

        String token = "not-a-real-token";
        LogoutRequest logoutRequest = new LogoutRequest(token);

        // Act and assert
        assertThrows(UnauthorizedException.class, () -> userService.logout(logoutRequest));
    }

    @Test
    void logoutTwice() throws Exception {
        // Arrange
        UserDAO userDao = new UserDAOMemory();
        AuthDAO authDao = new AuthDAOMemory();
        UserService userService = new UserService(userDao, authDao);

        userService.register(new RegisterRequest("mrMe", "password123", "mrMe@example.com"));

        LoginResult loginResult = userService.login(new LoginRequest("mrMe", "password123"));

        String token = loginResult.authToken();
        LogoutRequest logoutRequest = new LogoutRequest(token);

        // Act: first logout succeeds
        userService.logout(logoutRequest);

        // Assert: second logout should fail
        assertThrows(UnauthorizedException.class, () -> userService.logout(logoutRequest));
    }
}