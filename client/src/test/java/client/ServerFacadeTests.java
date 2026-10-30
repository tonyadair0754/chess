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

    @Test
    void logoutSuccess() throws Exception {
        AuthData authData = facade.register("mrMe", "password123", "mrMe@example.com");

        assertDoesNotThrow(() -> {
            facade.logout(authData.authToken());
        });
    }

    // --- Game Management Facade Tests ---

//    @Test
//    void listGamesSuccess() throws Exception {
//        var auth = facade.register("mrMe", "password123", "mrMe@example.com");
//        facade.createGame("Game One", auth.authToken());
//        facade.createGame("Game Two", auth.authToken());
//
//        GameData[] games = facade.listGames(auth.authToken());
//        assertNotNull(games);
//        assertEquals(2, games.length);
//    }

    @Test
    void createGameSuccess() throws Exception {
        var auth = facade.register("mrMe", "password123", "mrMe@example.com");
        var game = facade.createGame("Championship Match", auth.authToken());

        assertNotNull(game);
        assertTrue(game.gameID() > 0);
        assertEquals("Championship Match", game.gameName());
    }

    @Test
    void createGameUnauthorized() {
        assertThrows(Exception.class, () -> {
            facade.createGame("Unauthorized Game", "invalid-auth-token");
        });
    }

//    @Test
//    void joinGameSuccess() throws Exception {
//        var auth = facade.register("mrMe", "password123", "mrMe@example.com");
//        var game = facade.createGame("Joinable Game", auth.authToken());
//
//        assertDoesNotThrow(() -> {
//            facade.joinGame(chess.ChessGame.TeamColor.WHITE, game.gameID(), auth.authToken());
//        });
//    }
//
//    @Test
//    void joinGameAlreadyTaken() throws Exception {
//        var auth1 = facade.register("player1", "pass1", "p1@example.com");
//        var auth2 = facade.register("player2", "pass2", "p2@example.com");
//        var game = facade.createGame("Contested Game", auth1.authToken());
//
//        // Player 1 joins white
//        facade.joinGame(chess.ChessGame.TeamColor.WHITE, game.gameID(), auth1.authToken());
//
//        // Player 2 attempts to join white (should fail with an AlreadyTakenException exception)
//        assertThrows(Exception.class, () -> {
//            facade.joinGame(chess.ChessGame.TeamColor.WHITE, game.gameID(), auth2.authToken());
//        });
//    }
}