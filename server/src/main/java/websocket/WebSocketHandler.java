package websocket;

import com.google.gson.Gson;
import dataaccess.sql.AuthDAO;
import dataaccess.sql.GameDAO;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import websocket.commands.UserGameCommand;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler {
    private final AuthDAO authDao;
    private final GameDAO gameDao;

    private final Gson gson = new Gson();

    private final Map<Integer, Set<WsContext>> gameConnections = new ConcurrentHashMap<>();

    private final Map<WsContext, ConnectionInfo> connections = new ConcurrentHashMap<>();

    private record ConnectionInfo(
            String username,
            String authToken,
            int gameID
    ) {
    }

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
                sendError(ctx, "Error: invalid command");
                return;
            }

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(ctx, command);
                case MAKE_MOVE -> handleMakeMove(ctx, command);
                case LEAVE -> handleLeave(ctx, command);
                case RESIGN -> handleResign(ctx, command);
            }

        } catch (Exception e) {
            sendError(ctx, "Error: invalid command");
        }
    }

    private void sendError(WsContext ctx, String str) {
    }

    private void handleConnect(WsContext ctx, UserGameCommand command) {

    }

    private void handleMakeMove(WsContext ctx, UserGameCommand command) {

    }

    private void handleLeave(WsContext ctx, UserGameCommand command) {

    }

    private void handleResign(WsContext ctx, UserGameCommand command) {

    }
}