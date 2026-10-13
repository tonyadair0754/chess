package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public class GameService {
    private final AuthDAO authDao;
    private final GameDAO gameDao;

    public GameService(AuthDAO authDao, GameDAO gameDao) {
        this.authDao = authDao;
        this.gameDao = gameDao;
    }

    public ListGamesResult listGames(ListGamesRequest request)
            throws DataAccessException, UnauthorizedException {

        // Validate inputs
        if (request == null ||
                request.authToken() == null || request.authToken().isBlank()){
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

    // createGame();
    // joinGame();
}