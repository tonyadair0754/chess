package service;

import dataaccess.*;
import dataaccess.sql.AuthDAO;
import dataaccess.sql.AuthDAOMySQL;
import dataaccess.sql.UserDAO;
import dataaccess.sql.UserDAOMySQL;
import model.AuthData;
import model.UserData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {
    private UserDAO userDao;
    private AuthDAO authDao;
    private UserService userService;

    @BeforeEach
    void setUp() throws DataAccessException {
        userDao = new UserDAOMySQL();
        authDao = new AuthDAOMySQL();
        userService = new UserService(userDao, authDao);

        userDao.clear();
        authDao.clear();
    }

    private void registerTestUser() throws Exception {
        userService.register(new RegisterRequest("mrMe", "password123", "mrMe@example.com"));
    }

    private String loginTestUser() throws Exception {
        registerTestUser();

        LoginResult result = userService.login(new LoginRequest("mrMe", "password123"));

        return result.authToken();
    }

    @Test
    public void registerSuccess() throws Exception {
        // Create sample data records
        RegisterRequest request = new RegisterRequest("mrMe", "password123", "mrMe@example.com");

        // Call the method we want to test
        RegisterResult result = userService.register(request);

        // Verify result
        assertEquals("mrMe", result.username());
        assertNotNull(result.authToken());
        assertFalse(result.authToken().isBlank());

        // Verify the user was stored
        UserData storedUser = userDao.getUser("mrMe");
        assertNotNull(storedUser);
        assertEquals("mrMe", storedUser.username());
        assertTrue(BCrypt.checkpw("password123", storedUser.password()));
        assertEquals("mrMe@example.com", storedUser.email());

        // Verify the auth token was stored
        AuthData storedAuth = authDao.getAuth(result.authToken());
        assertNotNull(storedAuth);
        assertEquals("mrMe", storedAuth.username());
    }

    @Test
    void registerUsernameAlreadyTaken() throws Exception {
        RegisterRequest first = new RegisterRequest("mrMe", "password123", "mrMe@example.com");
        userService.register(first);

        RegisterRequest duplicate = new RegisterRequest("mrMe", "differentPassword", "differentEmail@example.com");

        // Act and assert
        assertThrows(dataaccess.AlreadyTakenException.class, () -> userService.register(duplicate));
    }

    @Test
    void registerMissingUsername() throws Exception {
        RegisterRequest request = new RegisterRequest(null, "password123", "mrMe@example.com"
        );

        // Act and assert
        assertThrows(dataaccess.BadRequestException.class, () -> userService.register(request)
        );
    }

    @Test
    public void loginSuccess() throws Exception {
        // Register a user and prepare login request
        registerTestUser();
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
        registerTestUser();
        LoginRequest request = new LoginRequest("mrMe", "incorrectPassword");

        // Act and assert
        assertThrows(UnauthorizedException.class, () -> userService.login(request));
    }

    @Test
    void loginUnknownUsername() throws Exception {
        LoginRequest request = new LoginRequest("unknownUser", "password123");

        // Act and assert
        assertThrows(UnauthorizedException.class, () -> userService.login(request));
    }

    @Test
    void loginMissingPassword() throws Exception {
        LoginRequest request = new LoginRequest("mrMe", null);

        assertThrows(BadRequestException.class, () -> userService.login(request));
    }

    @Test
    public void logoutSuccess() throws Exception {
        String token = loginTestUser();
        LogoutRequest logoutRequest = new LogoutRequest(token);

        // Verify token exists before trying to logout
        assertNotNull(authDao.getAuth(token));

        // Logout
        userService.logout(logoutRequest);

        // Verify that the token no longer exists
        assertNull(authDao.getAuth(token));
    }

    @Test
    void logoutInvalidToken() throws Exception {
        String token = "not-a-real-token";
        LogoutRequest logoutRequest = new LogoutRequest(token);

        // Act and assert
        assertThrows(UnauthorizedException.class, () -> userService.logout(logoutRequest));
    }

    @Test
    void logoutTwice() throws Exception {
        String token = loginTestUser();
        LogoutRequest logoutRequest = new LogoutRequest(token);

        // First logout succeeds
        userService.logout(logoutRequest);

        // Second logout should fail
        assertThrows(UnauthorizedException.class, () -> userService.logout(logoutRequest));
    }
}