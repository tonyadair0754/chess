package websocket;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import dataaccess.DataAccessException;
import dataaccess.sql.AuthDAO;
import dataaccess.sql.GameDAO;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import model.AuthData;
import model.GameData;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler {
    private final AuthDAO authDao;
    private final GameDAO gameDao;
    private final Gson gson = new Gson();
    private final ConnectionManager connectionManager = new ConnectionManager();

    public WebSocketHandler(AuthDAO authDao, GameDAO gameDao) {
        this.authDao = authDao;
        this.gameDao = gameDao;
    }

    public void onMessage(WsMessageContext ctx) {
        UserGameCommand command;
        try {
            command = gson.fromJson(ctx.message(), UserGameCommand.class);
        } catch (JsonParseException e) {
            sendError(ctx, "Error: invalid JSON command");
            return;
        }

        if (command == null
                || command.getCommandType() == null
                || command.getAuthToken() == null
                || command.getGameID() == null) {
            sendError(ctx, "Error: invalid command");
            return;
        }

        try {
            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(ctx, command);
                case MAKE_MOVE -> handleMakeMove(ctx, command);
                case LEAVE -> handleLeave(ctx, command);
                case RESIGN -> handleResign(ctx, command);
            }
        } catch (Exception e) {
            // Log unexpected failures separately so we don't mask them as general "bad user commands"
            System.err.println("Unexpected error handling " + command.getCommandType() + ": " + e.getMessage());
            e.printStackTrace();
            sendError(ctx, "Error: internal server error");
        }
    }

    public void onClose(WsCloseContext ctx) {
        connectionManager.remove(ctx.session);
    }

    private void sendError(WsContext ctx, String message) {
        ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, message);
        connectionManager.sendToContext(ctx, errorMessage);
    }

    private void handleConnect(
            WsMessageContext ctx,
            UserGameCommand command) {

        try {
            // Authenticate
            AuthData auth = authDao.getAuth(command.getAuthToken());
            if (auth == null) {
                sendError(ctx, "Error: unauthorized");
                return;
            }

            // Retrieve the game
            GameData game = gameDao.getGame(command.getGameID());
            if (game == null) {
                sendError(ctx, "Error: game not found");
                return;
            }

            // Determine identity and register connection
            String username = auth.username();
            connectionManager.add(ctx, username, command.getAuthToken(), command.getGameID());

            // Send current game state back to the connecting client
            ServerMessage loadGameMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game.game());
            connectionManager.sendToSession(ctx.session, loadGameMessage);

            // Notify other connected players & observers in the same game
            String role = username.equals(game.whiteUsername()) ? "white"
                    : username.equals(game.blackUsername()) ? "black"
                    : "an observer";
            String noteText = username + " joined the game as " + role + ".";
            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, noteText);
            connectionManager.broadcastToOthers(command.getGameID(), ctx.session, notification);
        } catch (DataAccessException e) {
            System.err.println("Database error during CONNECT: " + e.getMessage());
            e.printStackTrace();
            sendError(ctx, "Error: database failure");
        }
    }

    private void handleMakeMove(WsContext ctx, UserGameCommand command) {

    }

    private void handleLeave(WsContext ctx, UserGameCommand command) {

    }

    private void handleResign(WsContext ctx, UserGameCommand command) {

    }
}