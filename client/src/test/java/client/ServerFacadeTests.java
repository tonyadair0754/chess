package client;

import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void setUp() throws Exception {
        facade.clearDatabase();
    }

    // --- User & Auth Facade Tests ---

//    @Test
//    void clearSuccess() throws Exception {
//        // Add some data to the server (create a game)
//        // Verify data exists (list games)
//        // Clear the database
//        // Verify data is gone
//    }

    @Test
    void registerSuccess() throws Exception {
        AuthData authData = facade.register("mrMe", "password123", "mrMe@example.com");
        assertNotNull(authData);
        assertEquals("mrMe", authData.username());
        assertTrue(authData.authToken().length() > 10);
    }

    @Test
    void registerDuplicateUsername() throws Exception {
        facade.register("mrMe", "password123", "mrMe@example.com");

        // Attempting to register the same username should throw an exception (HTTP 403/500 from server)
        assertThrows(Exception.class, () -> {
            facade.register("mrMe", "otherpassword", "other@example.com");
        });
    }

    @Test
    void loginSuccess() throws Exception {
        facade.register("mrMe", "password123", "mrMe@example.com");

        AuthData authData = facade.login("mrMe", "password123");
        assertNotNull(authData);
        assertEquals("mrMe", authData.username());
    }

    @Test
    void loginFailureWrongPassword() throws Exception {
        facade.register("mrMe", "password123", "mrMe@example.com");

        assertThrows(Exception.class, () -> {
            facade.login("mrMe", "wrongpassword");
        });
    }
//
//    @Test
//    void logoutSuccess() throws Exception {
//        AuthData authData = facade.register("mrMe", "password123", "mrMe@example.com");
//
//        assertDoesNotThrow(() -> {
//            facade.logout(authData.authToken());
//        });
//    }

    // --- Game Management Facade Tests ---
}