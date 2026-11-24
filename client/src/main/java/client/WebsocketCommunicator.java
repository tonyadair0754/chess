
package client;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class WebsocketCommunicator implements WebSocket.Listener {
    private final Consumer<ServerMessage> observer;
    private final Gson gson = new Gson();
    private final StringBuilder textBuffer = new StringBuilder();

    private WebSocket webSocket;

    public WebsocketCommunicator(int port, Consumer<ServerMessage> observer) throws Exception {
        this.observer = observer;

        URI uri = URI.create("ws://localhost:" + port + "/ws");

        this.webSocket = HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(uri, this)
                .get(5, TimeUnit.SECONDS);
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        startKeepAlive();
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        stopKeepAlive();
        scheduler.shutdownNow();
        webSocket.request(1);
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        textBuffer.append(data);

        if (last) {
            try {
                ServerMessage message = gson.fromJson(textBuffer.toString(), ServerMessage.class);
                observer.accept(message);
            } catch (RuntimeException e) {
                System.err.println("Failed to process server message: " + e.getMessage());
            } finally {
                textBuffer.setLength(0);
            }
        }

        webSocket.request(1);
        return null;
    }

    public void close() {
        stopKeepAlive();
        scheduler.shutdownNow();
        if (webSocket != null && !webSocket.isOutputClosed()) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Leaving game").join();
        }
    }

    public void sendCommand(UserGameCommand command) {
        if (webSocket == null || webSocket.isOutputClosed()) {
            throw new IllegalStateException("WebSocket connection is not open");
        }

        String json = gson.toJson(command);
        webSocket.sendText(json, true).join();
    }

    // --- Keep-alive pings ---
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ws-keepalive");
        t.setDaemon(true);
        return t;
    });
    private ScheduledFuture<?> pingTask;

    private void startKeepAlive() {
        stopKeepAlive();
        if (scheduler.isShutdown()) {
            return;
        }
        pingTask = scheduler.scheduleAtFixedRate(() -> {
            if (webSocket != null && !webSocket.isOutputClosed()) {
                webSocket.sendPing(ByteBuffer.wrap(new byte[]{1}))
                        .exceptionally(ex -> null);  // Avoid unhandled async errors if closed mid-send
            }
        }, 20, 20, TimeUnit.SECONDS);
    }

    private void stopKeepAlive() {
        if (pingTask != null) {
            pingTask.cancel(false);
            pingTask = null;
        }
    }
}
