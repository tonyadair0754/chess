package service;

import chess.ChessGame;
import dataaccess.*;
import dataaccess.sql.AuthDAOMySQL;
import dataaccess.sql.GameDAOMySQL;
import dataaccess.sql.UserDAOMySQL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ServiceEdgeCaseTests {

    private UserService userService;
    private GameService gameService;
    private String validToken;

    @BeforeEach
    public void setUp() throws Exception {
        UserDAOMySQL userDao = new UserDAOMySQL();
        AuthDAOMySQL authDao = new AuthDAOMySQL();
        GameDAOMySQL gameDao = new GameDAOMySQL();

        userService = new UserService(userDao, authDao);
        gameService = new GameService(authDao, gameDao);

        userDao.clear();
        authDao.clear();
        gameDao.clear();

        validToken = userService
                .register(new RegisterRequest("mrMe", "password123", "mrMe@example.com"))
                .authToken();
    }

    // --- UserService.register ---

    @Test
    public void registerNullRequest() {
        assertThrows(BadRequestException.class, () -> userService.register(null));
    }

    @Test
    public void registerBlankUsername() {
        assertThrows(BadRequestException.class,
                () -> userService.register(new RegisterRequest("", "password123", "mrMe@example.com")));
    }

    @Test
    public void registerNullPassword() {
        assertThrows(BadRequestException.class,
                () -> userService.register(new RegisterRequest("mrMe", null, "mrMe@example.com")));
    }

    @Test
    public void registerBlankPassword() {
        assertThrows(BadRequestException.class,
                () -> userService.register(new RegisterRequest("mrMe", "", "mrMe@example.com")));
    }

    @Test
    public void registerNullEmail() {
        assertThrows(BadRequestException.class,
                () -> userService.register(new RegisterRequest("mrMe", "password123", null)));
    }

    @Test
    public void registerBlankEmail() {
        assertThrows(BadRequestException.class,
                () -> userService.register(new RegisterRequest("mrMe", "password123", "")));
    }

    // --- UserService.login ---

    @Test
    public void loginNullRequest() {
        assertThrows(BadRequestException.class, () -> userService.login(null));
    }

    @Test
    public void loginNullUsername() {
        assertThrows(BadRequestException.class,
                () -> userService.login(new LoginRequest(null, "password123")));
    }

    @Test
    public void loginBlankUsername() {
        assertThrows(BadRequestException.class,
                () -> userService.login(new LoginRequest("", "password123")));
    }

    @Test
    public void loginBlankPassword() {
        assertThrows(BadRequestException.class,
                () -> userService.login(new LoginRequest("mrMe", "")));
    }

    // --- UserService.logout ---

    @Test
    public void logoutNullRequest() {
        assertThrows(UnauthorizedException.class, () -> userService.logout(null));
    }

    @Test
    public void logoutNullToken() {
        assertThrows(UnauthorizedException.class,
                () -> userService.logout(new LogoutRequest(null)));
    }

    @Test
    public void logoutBlankToken() {
        assertThrows(UnauthorizedException.class,
                () -> userService.logout(new LogoutRequest("")));
    }

    // ---------------------------------------------------------------- GameService.listGames

    @Test
    public void listGamesNullRequest() {
        assertThrows(UnauthorizedException.class, () -> gameService.listGames(null));
    }

    @Test
    public void listGamesNullToken() {
        assertThrows(UnauthorizedException.class,
                () -> gameService.listGames(new ListGamesRequest(null)));
    }

    @Test
    public void listGamesBlankToken() {
        assertThrows(UnauthorizedException.class,
                () -> gameService.listGames(new ListGamesRequest("")));
    }

    // --- GameService.createGame ---

    @Test
    public void createGameNullRequest() {
        assertThrows(UnauthorizedException.class, () -> gameService.createGame(null));
    }

    @Test
    public void createGameBlankToken() {
        assertThrows(UnauthorizedException.class,
                () -> gameService.createGame(new CreateGameRequest("", "My Game")));
    }

    @Test
    public void createGameBlankName() {
        assertThrows(BadRequestException.class,
                () -> gameService.createGame(new CreateGameRequest(validToken, "")));
    }

    @Test
    public void createGameInvalidToken() {
        assertThrows(UnauthorizedException.class,
                () -> gameService.createGame(new CreateGameRequest("invalid-token", "My Game")));
    }

    // --- GameService.joinGame ---

    @Test
    public void joinGameNullRequest() {
        assertThrows(UnauthorizedException.class, () -> gameService.joinGame(null));
    }

    @Test
    public void joinGameNullToken() {
        assertThrows(UnauthorizedException.class,
                () -> gameService.joinGame(new JoinGameRequest(null, ChessGame.TeamColor.WHITE, 1)));
    }

    @Test
    public void joinGameBlankToken() {
        assertThrows(UnauthorizedException.class,
                () -> gameService.joinGame(new JoinGameRequest("", ChessGame.TeamColor.WHITE, 1)));
    }

    // --- exception constructors

    @Test
    public void dataAccessExceptionKeepsMessageAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        DataAccessException ex = new DataAccessException("db failed", cause);

        assertEquals("db failed", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    public void dataAccessExceptionKeepsMessage() {
        assertEquals("db failed", new DataAccessException("db failed").getMessage());
    }

    @Test
    public void badRequestExceptionKeepsMessageAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        BadRequestException ex = new BadRequestException("bad", cause);

        assertEquals("bad", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    public void alreadyTakenExceptionKeepsMessageAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        AlreadyTakenException ex = new AlreadyTakenException("taken", cause);

        assertEquals("taken", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    public void unauthorizedExceptionKeepsMessageAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        UnauthorizedException ex = new UnauthorizedException("nope", cause);

        assertEquals("nope", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}