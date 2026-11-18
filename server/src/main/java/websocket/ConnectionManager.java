package websocket;

import com.google.gson.Gson;
import io.javalin.websocket.WsMessageContext;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final Gson gson = new Gson();

    // Map gameID to all active WsMessageContexts in that game
    private final Map<Integer, Set<WsMessageContext>> gameConnections = new ConcurrentHashMap<>();

    // Map a specific WsMessageContext to its connection metadata
    private final Map<WsMessageContext, ConnectionInfo> connections = new ConcurrentHashMap<>();

    public record ConnectionInfo(
            String username,
            String authToken,
            int gameID
    ) {
    }

    public void add(WsMessageContext ctx, String username, String authToken, int gameID) {
        connections.put(ctx, new ConnectionInfo(username, authToken, gameID));
        gameConnections.computeIfAbsent(gameID, k -> ConcurrentHashMap.newKeySet()).add(ctx);
    }

    public void remove(WsMessageContext ctx) {
        ConnectionInfo info = connections.remove(ctx);
        if (info == null) {
            return;
        }
        Set<WsMessageContext> gameConns = gameConnections.get(info.gameID());
        if (gameConns != null) {
            gameConns.remove(ctx);
            if (gameConns.isEmpty()) {
                gameConnections.remove(info.gameID());
            }
        }
    }

    public ConnectionInfo getConnectionInfo(WsMessageContext ctx) {
        return connections.get(ctx);
    }

    public void broadcast(int gameID, WsMessageContext senderCtx, ServerMessage message) {
        Set<WsMessageContext> sessions = gameConnections.get(gameID);
        if (sessions == null) {
            return;
        }
        String jsonMessage = gson.toJson(message);
        for (WsMessageContext clientSession : sessions) {
            if (!clientSession.equals(senderCtx)) {
                try {
                    if (clientSession.session.isOpen()) {
                        clientSession.send(jsonMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
