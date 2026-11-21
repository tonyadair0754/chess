package websocket;

import com.google.gson.Gson;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final Gson gson = new Gson();

    // Map gameID to active client connections for that game
    private final Map<Integer, Set<ClientConnection>> gameConnections = new ConcurrentHashMap<>();

    // Map underlying WebSocket Session to connection metadata & context for sending
    private final Map<Session, ClientConnection> connections = new ConcurrentHashMap<>();

    public record ClientConnection(
            Session session,
            WsContext ctx,
            String username,
            String authToken,
            int gameID
    ) {
    }

    public void add(WsMessageContext ctx, String username, String authToken, int gameID) {
        Session session = ctx.session;
        ClientConnection conn = new ClientConnection(session, ctx, username, authToken, gameID);

        connections.put(session, conn);
        gameConnections.computeIfAbsent(gameID, k -> ConcurrentHashMap.newKeySet()).add(conn);
    }

    public void remove(Session session) {
        ClientConnection conn = connections.remove(session);
        if (conn == null) {
            return;
        }
        Set<ClientConnection> gameConns = gameConnections.get(conn.gameID());
        if (gameConns != null) {
            gameConns.remove(conn);
            if (gameConns.isEmpty()) {
                gameConnections.remove(conn.gameID());
            }
        }
    }

    public ClientConnection getConnection(Session session) {
        return connections.get(session);
    }

    /*
    Send a message to a single registered session
     */
    public void sendToSession(Session session, ServerMessage message) {
        ClientConnection conn = getConnection(session);
        if (conn != null) {
            send(conn, message);
        }
    }

    /*
    Send a message directly to WebSocket before a connection is registered
    (For error messages on failed authentication or invalid commands)
     */
    public void sendToContext(WsContext ctx, ServerMessage message) {
        if (ctx == null) {
            return;
        }
        if (ctx.session.isOpen()) {
            ctx.send(gson.toJson(message));
        }
    }

    /*
    Broadcasts a message to everyone in the game except for the originating session
     */
    public void broadcastToOthers(int gameID, Session excludeSession, ServerMessage message) {
        Set<ClientConnection> clients = gameConnections.get(gameID);
        if (clients == null) {
            return;
        }
        for (ClientConnection client : clients) {
            if (!client.session().equals(excludeSession)) {
                send(client, message);
            }
        }
    }

    /*
    Broadcast a message to every connected client in a game
     */
    public void broadcastToAll(int gameID, ServerMessage message) {
        Set<ClientConnection> clients = gameConnections.get(gameID);
        if (clients == null) {
            return;
        }
        for (ClientConnection client : clients) {
            send(client, message);
        }
    }

    public void send(ClientConnection client, ServerMessage message) {
        if (client.session().isOpen()) {
            client.ctx.send(gson.toJson(message));
        }
    }
}
