package service;

import chess.ChessGame;
import dataaccess.*;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTests {
    private UserDAO userDao;
    private GameDAO gameDao;
    private AuthDAO authDao;

    private UserService userService;
    private GameService gameService;

    @BeforeEach
    void setUp() throws DataAccessException {
        userDao = new UserDAOMySQL();
        authDao = new AuthDAOMySQL();
        gameDao = new GameDAOMySQL();

        userService = new UserService(userDao, authDao);
        gameService = new GameService(authDao, gameDao);

        userDao.clear();
        authDao.clear();
        gameDao.clear();
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
    void listGamesEmpty() throws Exception {
        String token = loginTestUser();

        // Act
        ListGamesResult result = gameService.listGames(new ListGamesRequest(token));

        // Assert
        assertNotNull(result);
        assertTrue(result.games().isEmpty());
    }

    @Test
    void listGamesInvalidAuthToken() throws Exception {
        // Act and assert
        assertThrows(UnauthorizedException.class, () -> gameService.listGames(new ListGamesRequest("invalid-token")));
    }

    @Test
    void listGamesWithMultipleGames() throws Exception {
        String token = loginTestUser();

        CreateGameResult first = gameService.createGame(new CreateGameRequest(token, "First Game"));

        CreateGameResult second = gameService.createGame(new CreateGameRequest(token, "Second Game"));

        // Act
        ListGamesResult result = gameService.listGames(new ListGamesRequest(token));

        // Assert
        assertEquals(2, result.games().size());

        assertTrue(result.games().stream().anyMatch(game -> game.gameID() == first.gameID()
                && game.gameName().equals("First Game")));

        assertTrue(result.games().stream().anyMatch(game -> game.gameID() == second.gameID()
                && game.gameName().equals("Second Game")));
    }

    @Test
    void createGameSuccess() throws Exception {
        String token = loginTestUser();

        // Act
        CreateGameResult result = gameService.createGame(new CreateGameRequest(token, "My Chess Game"));

        // Assert
        assertTrue(result.gameID() > 0);

        // Retrieve the stored game
        GameData storedGame = gameDao.getGame(result.gameID());

        assertNotNull(storedGame);
        assertEquals("My Chess Game", storedGame.gameName());
        assertNull(storedGame.whiteUsername());
        assertNull(storedGame.blackUsername());
    }

    @Test
    void createGameInvalidAuthToken() throws Exception {
        CreateGameRequest request = new CreateGameRequest(null, "My Chess Game");

        // Act and assert
        assertThrows(UnauthorizedException.class, () -> gameService.createGame(request));
    }

    @Test
    void createGameMissingName() throws Exception {
        String token = loginTestUser();

        CreateGameRequest request = new CreateGameRequest(token, null);

        // Act and assert
        assertThrows(BadRequestException.class, () -> gameService.createGame(request));
    }

    @Test
    void joinGameAsWhite() throws Exception {
        String token = loginTestUser();

        // Create a game
        CreateGameResult created = gameService.createGame(new CreateGameRequest(token, "Chess Match"));

        // Act: join as white
        gameService.joinGame(new JoinGameRequest(token, ChessGame.TeamColor.WHITE, created.gameID()));

        // Assert: retrieve the updated game
        GameData game = gameDao.getGame(created.gameID());

        assertNotNull(game);
        assertEquals("mrMe", game.whiteUsername());
        assertNull(game.blackUsername());
    }

    @Test
    void joinGameAsBlack() throws Exception {
        String token = loginTestUser();

        // Create a game
        CreateGameResult created = gameService.createGame(new CreateGameRequest(token, "Chess Match"));

        // Act: join as black
        gameService.joinGame(new JoinGameRequest(token, ChessGame.TeamColor.BLACK, created.gameID()));

        // Assert: retrieve the updated game
        GameData game = gameDao.getGame(created.gameID());

        assertNotNull(game);
        assertEquals("mrMe", game.blackUsername());
        assertNull(game.whiteUsername());
    }

    @Test
    void joinGameInvalidAuthToken() {
        JoinGameRequest request = new JoinGameRequest("invalid-token", ChessGame.TeamColor.WHITE, 1234);

        assertThrows(UnauthorizedException.class, () -> {
            gameService.joinGame(request);
        });
    }

    @Test
    void joinGameGameNotFound() throws Exception {
        String token = loginTestUser();

        // Attempt to join a game ID that doesn't exist
        JoinGameRequest request = new JoinGameRequest(token, ChessGame.TeamColor.WHITE, 999999);

        assertThrows(BadRequestException.class, () -> {
            gameService.joinGame(request);
        });
    }

    @Test
    void joinGameWhiteAlreadyTaken() throws Exception {
        String token1 = loginTestUser();

        // Register & login a second user
        userService.register(new RegisterRequest("mrMe_theSequel", "password456", "theSequel@example.com"));
        LoginResult login2 = userService.login(new LoginRequest("mrMe_theSequel", "password456"));
        String token2 = login2.authToken();

        // Player 1 creates and joins as white
        CreateGameResult created = gameService.createGame(new CreateGameRequest(token1, "Match"));
        gameService.joinGame(new JoinGameRequest(token1, ChessGame.TeamColor.WHITE, created.gameID()));

        // Player 2 attempts to join as white
        JoinGameRequest request2 = new JoinGameRequest(token2, ChessGame.TeamColor.WHITE, created.gameID());

        assertThrows(AlreadyTakenException.class, () -> {
            gameService.joinGame(request2);
        });
    }

    @Test
    void joinGameBlackAlreadyTaken() throws Exception {
        String token1 = loginTestUser();

        userService.register(new RegisterRequest("mrMe_theSequel", "password456", "p2@example.com"));
        LoginResult login2 = userService.login(new LoginRequest("mrMe_theSequel", "password456"));
        String token2 = login2.authToken();

        // Player 1 creates and joins as black
        CreateGameResult created = gameService.createGame(new CreateGameRequest(token1, "Match"));
        gameService.joinGame(new JoinGameRequest(token1, ChessGame.TeamColor.BLACK, created.gameID()));

        // Player 2 attempts to join as black
        JoinGameRequest request2 = new JoinGameRequest(token2, ChessGame.TeamColor.BLACK, created.gameID());

        assertThrows(AlreadyTakenException.class, () -> {
            gameService.joinGame(request2);
        });
    }

    @Test
    void joinGameInvalidColor() throws Exception {
        String token = loginTestUser();
        CreateGameResult created = gameService.createGame(new CreateGameRequest(token, "Match"));

        JoinGameRequest request = new JoinGameRequest(token, null, created.gameID());

        assertThrows(BadRequestException.class, () -> {
            gameService.joinGame(request);
        });
    }
}
