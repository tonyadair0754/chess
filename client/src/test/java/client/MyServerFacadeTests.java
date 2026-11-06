package client;

import chess.ChessGame;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;

public class MyServerFacadeTests {
    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    static void init() {
        server = new Server();
        var port = server.run(0); // Starts server on a random available port
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clear() throws Exception {
        facade.clearDatabase();
    }

    @Test
    void registerSuccess() throws Exception {
        var authData = facade.register("testUser", "securePass", "test@example.com");
        assertNotNull(authData);
        assertNotNull(authData.authToken());
        assertEquals("testUser", authData.username());
    }

    @Test
    void registerMissingFields() {
        assertThrows(Exception.class, () -> facade.register(null, null, null));
    }

    @Test
    void loginSuccess() throws Exception {
        facade.register("loginUser", "password123", "login@example.com");
        var authData = facade.login("loginUser", "password123");
        assertNotNull(authData.authToken());
        assertEquals("loginUser", authData.username());
    }

    @Test
    void loginFakeUser() {
        assertThrows(Exception.class, () -> facade.login("nonexistent", "wrongpass"));
    }

    @Test
    void logoutSuccess() throws Exception {
        var authData = facade.register("logoutUser", "pass", "logout@example.com");
        assertDoesNotThrow(() -> facade.logout(authData.authToken()));
    }

    // --- Game workflow tests ---

    @Test
    void createGameSuccess() throws Exception {
        var auth = facade.register("gameCreator", "password", "creator@example.com");
        var game = facade.createGame(auth.authToken(), "My Game");

        assertNotNull(game);
        assertTrue(game.gameID() > 0);
        assertEquals("My Game", game.gameName());
        assertNotNull(game.game()); // Verifies the ChessGame object is instantiated
    }

    @Test
    void createGameUnauthorized() {
        assertThrows(Exception.class, () -> facade.createGame("invalid-token-12345", "Unauthorized Game"));
    }

    @Test
    void listGamesEmptyAndMultiple() throws Exception {
        var auth = facade.register("lister", "password", "list@example.com");

        // Initially empty
        var gamesBefore = facade.listGames(auth.authToken());
        assertTrue(gamesBefore.isEmpty());

        // Create two games
        facade.createGame(auth.authToken(), "Game One");
        facade.createGame(auth.authToken(), "Game Two");

        // Verify listing returns both
        var gamesAfter = facade.listGames(auth.authToken());
        assertEquals(2, gamesAfter.size());
    }

    @Test
    void listGamesUnauthorized() {
        assertThrows(Exception.class, () -> facade.listGames("bad-auth-token"));
    }

    @Test
    void joinGameSuccess() throws Exception {
        var auth1 = facade.register("playerWhite", "pass", "w@example.com");
        var auth2 = facade.register("playerBlack", "pass", "b@example.com");
        var game = facade.createGame(auth1.authToken(), "My Game");
        
        System.out.println("Created game ID: " + game.gameID());

        var games = facade.listGames(auth1.authToken());
        games.forEach(g -> System.out.println(
                "Listed game ID: " + g.gameID() + ", name: " + g.gameName()
        ));

        // Player 1 joins as white
        assertDoesNotThrow(() -> {
            facade.joinGame(chess.ChessGame.TeamColor.WHITE, game.gameID(), auth1.authToken());
        });

        // Player 2 joins as black in the same game
        assertDoesNotThrow(() -> {
            facade.joinGame(chess.ChessGame.TeamColor.BLACK, game.gameID(), auth2.authToken());
        });
    }

    @Test
    void joinGameAlreadyTaken() throws Exception {
        var auth1 = facade.register("userOne", "pass", "one@example.com");
        var auth2 = facade.register("userTwo", "pass", "two@example.com");
        var game = facade.createGame(auth1.authToken(), "My Game");

        // First user takes white
        facade.joinGame(chess.ChessGame.TeamColor.WHITE, game.gameID(), auth1.authToken());

        // Second user attempts to take white
        assertThrows(Exception.class, () -> {
            facade.joinGame(chess.ChessGame.TeamColor.WHITE, game.gameID(), auth2.authToken());
        });
    }

    @Test
    void joinGameInvalidId() throws Exception {
        var auth = facade.register("user", "pass", "user@example.com");

        // Attempting to join a game ID that doesn't exist (e.g., 99999)
        assertThrows(Exception.class, () -> {
            facade.joinGame(chess.ChessGame.TeamColor.WHITE, 99999, auth.authToken());
        });
    }
}