package handler;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import io.javalin.http.Context;
import service.*;

import java.util.Map;

/**
 * The server handler classes serve as a translator between HTTP and Java.
 * Your handlers will convert an HTTP request into Java usable objects & data.
 * The handler then calls the appropriate service.
 * When the service responds, the handler converts the response object back to JSON and sends the HTTP response.
 * This could include converting thrown exception types into the appropriate HTTP status codes if necessary.
 */
public class GameHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void listGames(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            ListGamesRequest request = new ListGamesRequest(authToken);
            ListGamesResult result = gameService.listGames(request);
            sendJsonResponse(ctx, 200, result);

        } catch (UnauthorizedException e) {
            sendJsonResponse(ctx, 401, Map.of("message", e.getMessage()));
        } catch (DataAccessException e) {
            sendJsonResponse(ctx, 500, Map.of("message", e.getMessage()));
        }
    }

    public void createGame(Context ctx) {
        try {
            // Get auth from header
            String authToken = ctx.header("authorization");

            // Extract gameName from Body
            CreateGameBody body = gson.fromJson(ctx.body(), CreateGameBody.class);

            // Handle body or game name being null
            String gameName = (body != null) ? body.gameName() : null;

            CreateGameRequest request = new CreateGameRequest(authToken, gameName);
            CreateGameResult result = gameService.createGame(request);

            sendJsonResponse(ctx, 200, result);

        } catch (BadRequestException e) {
            sendJsonResponse(ctx, 400, Map.of("message", e.getMessage()));
        } catch (UnauthorizedException e) {
            sendJsonResponse(ctx, 401, Map.of("message", e.getMessage()));
        } catch (DataAccessException e) {
            sendJsonResponse(ctx, 500, Map.of("message", e.getMessage()));
        }
    }

    public void joinGame(Context ctx) {
        try {
            // Get auth from header
            String authToken = ctx.header("authorization");

            JoinGameBody body = gson.fromJson(ctx.body(), JoinGameBody.class);

            ChessGame.TeamColor playerColor = (body != null) ? body.playerColor() : null;
            Integer gameID = (body != null) ? body.gameID() : null;

            int finalGameID = (gameID != null) ? gameID : 0;

            JoinGameRequest request = new JoinGameRequest(authToken, playerColor, finalGameID);
            gameService.joinGame(request);

            sendJsonResponse(ctx, 200, Map.of());

        } catch (BadRequestException e) {
            sendJsonResponse(ctx, 400, Map.of("message", e.getMessage()));
        } catch (UnauthorizedException e) {
            sendJsonResponse(ctx, 401, Map.of("message", e.getMessage()));
        } catch (AlreadyTakenException e) {
            sendJsonResponse(ctx, 403, Map.of("message", e.getMessage()));
        } catch (DataAccessException e) {
            sendJsonResponse(ctx, 500, Map.of("message", e.getMessage()));
        }
    }

    private void sendJsonResponse(Context ctx, int status, Object body) {
        ctx.status(status);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(body));
    }

    private record CreateGameBody(String gameName) {
    }

    private record JoinGameBody(ChessGame.TeamColor playerColor, Integer gameID) {
    }
}
