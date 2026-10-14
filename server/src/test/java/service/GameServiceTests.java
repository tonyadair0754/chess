package service;

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
    void setUp() {
        userDao = new UserDAOMemory();
        authDao = new AuthDAOMemory();
        gameDao = new GameDAOMemory();

        userService = new UserService(userDao, authDao);
        gameService = new GameService(authDao, gameDao);
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

//    @Test
//    void listGamesWithMultipleGames() throws Exception {
//        CreateGameResult first = gameService.createGame(token, new CreateGameRequest("First Game"));
//
//        CreateGameResult second = gameService.createGame(token, new CreateGameRequest("Second Game"));
//
//        // Act
//        ListGamesResult result = gameService.listGames(token);
//
//        // Assert
//        assertEquals(2, result.games().size());
//
//        assertTrue(result.games().stream().anyMatch(game -> game.gameID() == first.gameID()
//                && game.gameName().equals("First Game")));
//
//        assertTrue(result.games().stream().anyMatch(game -> game.gameID() == second.gameID()
//                && game.gameName().equals("Second Game")));
//    }
//
//    @Test
//    void createGameSuccess() throws Exception {
//        String token = loginTestUser();
//
//        // Act
//        CreateGameResult result = gameService.createGame(
//                token, new CreateGameRequest("My Chess Game")
//        );
//
//        // Assert
//        assertTrue(result.gameID() > 0);
//
//        // Retrieve the stored game
//        GameData storedGame = gameDao.getGame(result.gameID());
//
//        assertNotNull(storedGame);
//        assertEquals("My Chess Game", storedGame.gameName());
//        assertNull(storedGame.whiteUsername());
//        assertNull(storedGame.blackUsername());
//    }
//
//    @Test
//    void createGameInvalidAuthToken() throws Exception {
//        CreateGameRequest request = new CreateGameRequest("My Chess Game");
//
//        // Act and assert
//        assertThrows(UnauthorizedException.class, () -> gameService.createGame("invalid-token", request));
//    }
//
//    @Test
//    void createGameMissingName() throws Exception {
//        String token = loginTestUser();
//
//        CreateGameRequest request = new CreateGameRequest(null);
//
//        // Act and assert
//        assertThrows(BadRequestException.class, () -> gameService.createGame(token, request));
//    }
}
