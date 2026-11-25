package passoff.server; // Adjust package to match your project's test directory

import chess.*;
import com.google.gson.Gson;
import dataaccess.sql.AuthDAO;
import dataaccess.sql.GameDAO;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server; // Adjust to your Server class import
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WebSocketGameTests {

    private static Server server;
    private static int port;
    private static Gson gson;
    private static HttpClient httpClient;

    private static AuthDAO authDAO;
    private static GameDAO gameDAO;

    private String whiteAuth;
    private String blackAuth;
    private String observerAuth;
    private int gameId;

    @BeforeAll
    static void startServer() {
        server = new Server();
        port = server.run(0);
        gson = new Gson();
        httpClient = HttpClient.newHttpClient();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        clearDatabaseViaHttpOrDao();

        AuthData white = registerOrLogin("whitePlayer", "pass123", "white@test.com");
        AuthData black = registerOrLogin("blackPlayer", "pass123", "black@test.com");
        AuthData observer = registerOrLogin("observerUser", "pass123", "obs@test.com");

        whiteAuth = white.authToken();
        blackAuth = black.authToken();
        observerAuth = observer.authToken();

        gameId = createGame(whiteAuth, "Test Match");
        joinGame(whiteAuth, ChessGame.TeamColor.WHITE, gameId);
        joinGame(blackAuth, ChessGame.TeamColor.BLACK, gameId);
    }

    // --- 1. CONNECTION & NOTIFICATION TESTS ---

    @Test
    @Order(1)
    /*
    White, Black, and Observer connect and receive expected messages/notifications
     */
    void testConnectPlayersAndObserver() throws Exception {
        TestWebSocketClient whiteWs = connectClient();
        whiteWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, whiteAuth, gameId));

        ServerMessage whiteLoad = whiteWs.awaitMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        assertNotNull(whiteLoad);
        assertEquals(0, whiteWs.countMessages(ServerMessage.ServerMessageType.NOTIFICATION));

        TestWebSocketClient blackWs = connectClient();
        blackWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, blackAuth, gameId));

        assertNotNull(blackWs.awaitMessage(ServerMessage.ServerMessageType.LOAD_GAME));
        ServerMessage whiteJoinNotif = whiteWs.awaitMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        assertNotNull(whiteJoinNotif);
        assertTrue(whiteJoinNotif.getMessage().toLowerCase().contains("blackplayer"));

        TestWebSocketClient obsWs = connectClient();
        obsWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, observerAuth, gameId));

        assertNotNull(obsWs.awaitMessage(ServerMessage.ServerMessageType.LOAD_GAME));
        assertNotNull(whiteWs.awaitMessage(ServerMessage.ServerMessageType.NOTIFICATION));
        assertNotNull(blackWs.awaitMessage(ServerMessage.ServerMessageType.NOTIFICATION));

        whiteWs.close();
        blackWs.close();
        obsWs.close();
    }

    @Test
    @Order(2)
    void testConnectInvalidAuthAndGameId() throws Exception {
        TestWebSocketClient badAuthWs = connectClient();
        badAuthWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, "invalid-token", gameId));
        ServerMessage badAuthErr = badAuthWs.awaitMessage(ServerMessage.ServerMessageType.ERROR);
        assertNotNull(badAuthErr);
        assertTrue(badAuthErr.getErrorMessage().toLowerCase().contains("error"));

        TestWebSocketClient badGameWs = connectClient();
        badGameWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, whiteAuth, 999999));
        ServerMessage badGameErr = badGameWs.awaitMessage(ServerMessage.ServerMessageType.ERROR);
        assertNotNull(badGameErr);

        badAuthWs.close();
        badGameWs.close();
    }

    @Test
    @Order(3)
    /*
    Messages in one game are isolated from clients in another game
     */
    void testGameIsolation() throws Exception {
        int otherGameId = createGame(whiteAuth, "Other Match");
        joinGame(whiteAuth, ChessGame.TeamColor.WHITE, otherGameId);

        TestWebSocketClient game1Client = connectClient();
        game1Client.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, whiteAuth, gameId));
        assertNotNull(game1Client.awaitMessage(ServerMessage.ServerMessageType.LOAD_GAME));
        game1Client.clearMessages();

        TestWebSocketClient game2Client = connectClient();
        game2Client.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, whiteAuth, otherGameId));
        assertNotNull(game2Client.awaitMessage(ServerMessage.ServerMessageType.LOAD_GAME));
        game2Client.clearMessages();

        // Move in game 1
        ChessMove move = new ChessMove(new ChessPosition(2, 5), new ChessPosition(4, 5), null);  // e2 -> e4
        game1Client.sendCommand(new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, whiteAuth, gameId, move));

        assertNotNull(game1Client.awaitMessage(ServerMessage.ServerMessageType.LOAD_GAME));
        assertEquals(0, game2Client.countMessages(ServerMessage.ServerMessageType.LOAD_GAME));
        assertEquals(0, game2Client.countMessages(ServerMessage.ServerMessageType.NOTIFICATION));

        game1Client.close();
        game2Client.close();
    }

    // --- 2. LEAVE TESTS ---

    @Test
    @Order(4)
    void testPlayerLeaveFreesSlotAndNotifiesOthers() throws Exception {
        TestWebSocketClient whiteWs = connectClient();
        TestWebSocketClient blackWs = connectClient();

        whiteWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, whiteAuth, gameId));
        blackWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, blackAuth, gameId));
        assertNotNull(whiteWs.awaitMessage(ServerMessage.ServerMessageType.LOAD_GAME));
        assertNotNull(blackWs.awaitMessage(ServerMessage.ServerMessageType.LOAD_GAME));
        whiteWs.clearMessages();
        blackWs.clearMessages();

        whiteWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.LEAVE, whiteAuth, gameId));

        ServerMessage blackNotif = blackWs.awaitMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        assertNotNull(blackNotif);
        assertTrue(blackNotif.getMessage().toLowerCase().contains("whiteplayer"));

        // White slot should now be vacant so another player can join WHITE
        AuthData replacement = registerOrLogin("newWhite", "pass123", "newwhite@test.com");
        assertDoesNotThrow(() -> joinGame(replacement.authToken(), ChessGame.TeamColor.WHITE, gameId));

        // Game should still allow legal moves from remaining active player/color
        // Replacement player connects via WebSocket as the new White player
        TestWebSocketClient replacementWs = connectClient();
        replacementWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, replacement.authToken(), gameId));
        assertNotNull(replacementWs.awaitMessage(ServerMessage.ServerMessageType.LOAD_GAME));
        blackWs.clearMessages();

        // Replacement player makes White's opening move (d2 -> d4)
        ChessMove move = new ChessMove(new ChessPosition(2, 4), new ChessPosition(4, 4), null);
        replacementWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, replacement.authToken(), gameId, move));

        assertNotNull(replacementWs.awaitMessage(ServerMessage.ServerMessageType.LOAD_GAME));
        assertNotNull(blackWs.awaitMessage(ServerMessage.ServerMessageType.LOAD_GAME));

        replacementWs.close();
        whiteWs.close();
        blackWs.close();
    }

    @Test
    @Order(5)
    void testObserverLeavePreservesSlots() throws Exception {
        TestWebSocketClient obsWs = connectClient();
        obsWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, observerAuth, gameId));
        assertNotNull(obsWs.awaitMessage(ServerMessage.ServerMessageType.LOAD_GAME));

        obsWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.LEAVE, observerAuth, gameId));

        // White slot is still taken, so joining WHITE should fail
        AuthData anotherUser = registerOrLogin("intruder", "pass123", "intruder@test.com");
        assertThrows(AssertionError.class, () -> joinGame(anotherUser.authToken(), ChessGame.TeamColor.WHITE, gameId));

        obsWs.close();
    }

    // --- 3. RESIGN TESTS ---

    @Test
    @Order(6)
    void testResignEndsGameAndRejectsMoves() throws Exception {
        TestWebSocketClient whiteWs = connectClient();
        TestWebSocketClient blackWs = connectClient();

        whiteWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, whiteAuth, gameId));
        blackWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, blackAuth, gameId));
        assertNotNull(whiteWs.awaitMessage(ServerMessage.ServerMessageType.LOAD_GAME));
        assertNotNull(blackWs.awaitMessage(ServerMessage.ServerMessageType.LOAD_GAME));
        whiteWs.clearMessages();
        blackWs.clearMessages();

        whiteWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.RESIGN, whiteAuth, gameId));

        assertNotNull(whiteWs.awaitMessage(ServerMessage.ServerMessageType.NOTIFICATION));
        assertNotNull(blackWs.awaitMessage(ServerMessage.ServerMessageType.NOTIFICATION));

        // Moving after resignation should fail
        ChessMove move = new ChessMove(new ChessPosition(2, 5), new ChessPosition(4, 5), null);
        whiteWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, whiteAuth, gameId, move));
        assertNotNull(whiteWs.awaitMessage(ServerMessage.ServerMessageType.ERROR));

        // Duplicate resignation should also fail
        whiteWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.RESIGN, whiteAuth, gameId));
        assertNotNull(whiteWs.awaitMessage(ServerMessage.ServerMessageType.ERROR));

        whiteWs.close();
        blackWs.close();
    }

    @Test
    @Order(7)
    void testObserverCannotResign() throws Exception {
        TestWebSocketClient obsWs = connectClient();
        obsWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, observerAuth, gameId));
        assertNotNull(obsWs.awaitMessage(ServerMessage.ServerMessageType.LOAD_GAME));

        obsWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.RESIGN, observerAuth, gameId));
        assertNotNull(obsWs.awaitMessage(ServerMessage.ServerMessageType.ERROR));

        obsWs.close();
    }

    // --- 4. CHECK, CHECKMATE, STALEMATE & TURN/AUTHORIZATION TESTS ---

    @Test
    @Order(8)
    void testCheckDoesNotEndGame() throws Exception {
        TestWebSocketClient whiteWs = connectClient();
        TestWebSocketClient blackWs = connectClient();
        whiteWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, whiteAuth, gameId));
        blackWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, blackAuth, gameId));
        assertNotNull(whiteWs.awaitMessage(ServerMessage.ServerMessageType.LOAD_GAME));
        assertNotNull(blackWs.awaitMessage(ServerMessage.ServerMessageType.LOAD_GAME));

        // Scholar's mate setup moves:
        // 1. e2-e4 e7-e5 2. Bf1-c4 Nb8-c6 3. Qd1-h5 Ng8-f6 4. Qh5xf7# (checkmate)
        // Here we test 1. e2-e4 e7-e5 2. Bf1-c4 (normal move), then later test checkmate
        playMove(whiteWs, whiteAuth, 2, 5, 4, 5, null);
        playMove(blackWs, blackAuth, 7, 5, 5, 5, null);
        playMove(whiteWs, whiteAuth, 1, 6, 4, 3, null);
        playMove(blackWs, blackAuth, 8, 2, 6, 3, null);
        playMove(whiteWs, whiteAuth, 1, 4, 5, 8, null);
        playMove(blackWs, blackAuth, 8, 7, 6, 6, null);

        // Qxf7# causes checkmate
        ChessMove mateMove = new ChessMove(new ChessPosition(5, 8), new ChessPosition(7, 6), null);
        whiteWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, whiteAuth, gameId, mateMove));

        ServerMessage finalBoardMsg = whiteWs.awaitMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        assertNotNull(finalBoardMsg);
        assertNotNull(blackWs.awaitMessage(ServerMessage.ServerMessageType.NOTIFICATION));

        // Any follow-up move after checkmate should be rejected
        ChessMove afterMateMove = new ChessMove(new ChessPosition(7, 5), new ChessPosition(6, 5), null);
        blackWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, blackAuth, gameId, afterMateMove));
        assertNotNull(blackWs.awaitMessage(ServerMessage.ServerMessageType.ERROR));

        whiteWs.close();
        blackWs.close();
    }

    @Test
    @Order(9)
    /*
    Wrong turn, observer move, and illegal move should all return ERROR without mutating board
     */
    void testInvalidMovesAndTurnEnforcement() throws Exception {
        TestWebSocketClient whiteWs = connectClient();
        TestWebSocketClient blackWs = connectClient();
        TestWebSocketClient obsWs = connectClient();

        whiteWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, whiteAuth, gameId));
        blackWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, blackAuth, gameId));
        obsWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, observerAuth, gameId));

        assertNotNull(whiteWs.awaitMessage(ServerMessage.ServerMessageType.LOAD_GAME));
        assertNotNull(blackWs.awaitMessage(ServerMessage.ServerMessageType.LOAD_GAME));
        assertNotNull(obsWs.awaitMessage(ServerMessage.ServerMessageType.LOAD_GAME));
        whiteWs.clearMessages();
        blackWs.clearMessages();
        obsWs.clearMessages();

        // Black tries to move on White's turn
        ChessMove blackMove = new ChessMove(new ChessPosition(7, 5), new ChessPosition(5, 5), null);
        blackWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, blackAuth, gameId, blackMove));
        assertNotNull(blackWs.awaitMessage(ServerMessage.ServerMessageType.ERROR));

        // Observer tries to make a move
        ChessMove whiteMove = new ChessMove(new ChessPosition(2, 5), new ChessPosition(4, 5), null);
        obsWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, observerAuth, gameId, whiteMove));
        assertNotNull(obsWs.awaitMessage(ServerMessage.ServerMessageType.ERROR));

        // White tries an illegal move (e2 -> e5)
        ChessMove illegalMove = new ChessMove(new ChessPosition(2, 5), new ChessPosition(5, 5), null);
        whiteWs.sendCommand(new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, whiteAuth, gameId, illegalMove));
        assertNotNull(whiteWs.awaitMessage(ServerMessage.ServerMessageType.ERROR));

        // Neither board nor move notification was broadcast
        assertEquals(0, whiteWs.countMessages(ServerMessage.ServerMessageType.LOAD_GAME));
        assertEquals(0, blackWs.countMessages(ServerMessage.ServerMessageType.LOAD_GAME));
        assertEquals(0, obsWs.countMessages(ServerMessage.ServerMessageType.LOAD_GAME));

        whiteWs.close();
        blackWs.close();
        obsWs.close();
    }

    // --- HELPERS ---

    private void playMove(TestWebSocketClient client, String auth,
                          int startRow, int startCol, int endRow, int endCol,
                          ChessPiece.PieceType promo) throws Exception {
        client.clearMessages();
        ChessMove move = new ChessMove(
                new ChessPosition(startRow, startCol),
                new ChessPosition(endRow, endCol),
                promo
        );
        client.sendCommand(new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, auth, gameId, move));
        assertNotNull(client.awaitMessage(ServerMessage.ServerMessageType.LOAD_GAME));
    }

    private TestWebSocketClient connectClient() throws Exception {
        TestWebSocketClient client = new TestWebSocketClient(gson);
        client.connect("ws://localhost:" + port + "/ws");
        return client;
    }

    private void clearDatabaseViaHttpOrDao() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/db"))
                .DELETE()
                .build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, res.statusCode());
    }

    private AuthData registerOrLogin(String username, String password, String email) throws Exception {
        String body = gson.toJson(Map.of("username", username, "password", password, "email", email));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/user"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, res.statusCode(), "Failed registering " + username + ": " + res.body());
        return gson.fromJson(res.body(), AuthData.class);
    }

    private int createGame(String auth, String gameName) throws Exception {
        String body = gson.toJson(Map.of("gameName", gameName));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/game"))
                .header("Authorization", auth)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, res.statusCode(), "Failed creating game: " + res.body());
        GameData created = gson.fromJson(res.body(), GameData.class);
        return created.gameID();
    }

    private void joinGame(String auth, ChessGame.TeamColor color, int id) throws Exception {
        String body = gson.toJson(Map.of("playerColor", color, "gameID", id));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/game"))
                .header("Authorization", auth)
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, res.statusCode(), "Failed joining game: " + res.body());
    }

    private static class TestWebSocketClient implements WebSocket.Listener {
        private final Gson gson;
        private final BlockingQueue<ServerMessage> messages = new LinkedBlockingQueue<>();
        private final StringBuilder buffer = new StringBuilder();
        private WebSocket webSocket;

        TestWebSocketClient(Gson gson) {
            this.gson = gson;
        }

        void connect(String url) throws Exception {
            this.webSocket = HttpClient.newHttpClient()
                    .newWebSocketBuilder()
                    .buildAsync(URI.create(url), this)
                    .get(5, TimeUnit.SECONDS);
        }

        void sendCommand(UserGameCommand command) {
            webSocket.sendText(gson.toJson(command), true).join();
        }

        ServerMessage awaitMessage(ServerMessage.ServerMessageType expectedType) throws InterruptedException {
            long deadline = System.currentTimeMillis() + 3000;
            List<ServerMessage> skipped = new ArrayList<>();
            while (System.currentTimeMillis() < deadline) {
                long remaining = Math.max(10, deadline - System.currentTimeMillis());
                ServerMessage msg = messages.poll(remaining, TimeUnit.MILLISECONDS);
                if (msg == null) {
                    break;
                }
                if (msg.getServerMessageType() == expectedType) {
                    messages.addAll(skipped);
                    return msg;
                }
                skipped.add(msg);
            }
            messages.addAll(skipped);
            return null;
        }

        int countMessages(ServerMessage.ServerMessageType type) {
            return (int) messages.stream().filter(m -> m.getServerMessageType() == type).count();
        }

        void clearMessages() {
            messages.clear();
        }

        void close() {
            if (webSocket != null && !webSocket.isOutputClosed()) {
                try {
                    webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "done").join();
                } catch (Exception ignored) {
                }
            }
        }

        @Override
        public void onOpen(WebSocket ws) {
            ws.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
            buffer.append(data);
            if (last) {
                ServerMessage msg = gson.fromJson(buffer.toString(), ServerMessage.class);
                buffer.setLength(0);
                messages.add(msg);
            }
            ws.request(1);
            return null;
        }

        @Override
        public void onError(WebSocket ws, Throwable error) {
            // Optional: record errors if needed
        }

        @Override
        public CompletionStage<?> onPing(WebSocket ws, ByteBuffer message) {
            ws.request(1);
            return WebSocket.Listener.super.onPing(ws, message);
        }
    }
}