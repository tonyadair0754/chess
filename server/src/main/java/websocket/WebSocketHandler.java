package websocket;

import com.google.gson.Gson;
import dataaccess.sql.AuthDAO;
import dataaccess.sql.GameDAO;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
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
        try {
            UserGameCommand command = new Gson().fromJson(ctx.message(), UserGameCommand.class);

            if (command == null
                    || command.getCommandType() == null
                    || command.getAuthToken() == null
                    || command.getGameID() == null) {
                sendErrorMsg(ctx, "Error: invalid command");
                return;
            }

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(ctx, command);
                case MAKE_MOVE -> handleMakeMove(ctx, command);
                case LEAVE -> handleLeave(ctx, command);
                case RESIGN -> handleResign(ctx, command);
            }

        } catch (Exception e) {
            sendErrorMsg(ctx, "Error: invalid command");
        }
    }

    public void onClose(WsCloseContext ctx) {
        connectionManager.remove(ctx);
    }

    private void sendErrorMsg(WsContext ctx, String errorMessage) {
        ServerMessage errorMsg = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
        ctx.send(gson.toJson(errorMsg));
    }

    private void handleConnect(WsContext ctx, UserGameCommand command) {
        /* TODO */
    }

    private void handleMakeMove(WsContext ctx, UserGameCommand command) {

    }

    private void handleLeave(WsContext ctx, UserGameCommand command) {

    }

    private void handleResign(WsContext ctx, UserGameCommand command) {

    }
}