
package client;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
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
        webSocket.request(1);
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

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
    }

    public void sendCommand(UserGameCommand command) {
        if (webSocket == null || webSocket.isOutputClosed()) {
            throw new IllegalStateException("WebSocket connection is not open");
        }

        String json = gson.toJson(command);
        webSocket.sendText(json, true).join();
    }

    public void close() {
        if (webSocket != null && !webSocket.isOutputClosed()) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Leaving game").join();
        }
    }
}
