package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public class GameService {
    private final AuthDAO authDao;
    private final GameDAO gameDao;
    private static int nextGameID = 1;

    public GameService(AuthDAO authDao, GameDAO gameDao) {
        this.authDao = authDao;
        this.gameDao = gameDao;
    }

    public ListGamesResult listGames(ListGamesRequest request)
            throws DataAccessException, UnauthorizedException {

        // Validate inputs
        if (request == null ||
                request.authToken() == null || request.authToken().isBlank()) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        // Verify token exists in database/memory
        AuthData auth = authDao.getAuth(request.authToken());
        if (auth == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        // Retrieve games from GameDAO
        Collection<GameData> games = gameDao.listGames();

        return new ListGamesResult(games);
    }

    public CreateGameResult createGame(CreateGameRequest request)
            throws DataAccessException, BadRequestException, UnauthorizedException {

        // Validate auth token exists
        if (request == null ||
                request.authToken() == null || request.authToken().isBlank()) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        // Verify token exists in database/memory
        AuthData auth = authDao.getAuth(request.authToken());
        if (auth == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        // Validate gameName exists
        if (request.gameName() == null || request.gameName().isBlank()) {
            throw new BadRequestException("Error: bad request");
        }

        // Generate new gameID
        int gameID = generateGameID();

        // Create a new GameData object
        // Parameters: (gameID, whiteUsername, blackUsername, gameName, chessGame)
        GameData newGame = new GameData(gameID, null, null, request.gameName(), new ChessGame());

        // Save in DAO
        gameDao.createGame(newGame);

        return new CreateGameResult(gameID);
    }

    // joinGame();

    private int generateGameID() {
        return nextGameID++;
    }
}