package websocket.messages;

import chess.ChessGame;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * <p>
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    private final ServerMessageType serverMessageType;
    private final ChessGame game;
    private final String errorMessage;
    private final String message;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    // Constructor for messages that only need the type
    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
        this.game = null;
        this.errorMessage = null;
        this.message = null;
    }

    // Constructor for LOAD_GAME messages
    public ServerMessage(ServerMessageType type, ChessGame game) {
        this.serverMessageType = type;
        this.game = game;
        this.errorMessage = null;
        this.message = null;
    }

    // Constructor for ERROR or NOTIFICATION text messages
    public ServerMessage(ServerMessageType type, String text) {
        this.serverMessageType = type;
        this.game = null;
        if (type == ServerMessageType.ERROR) {
            this.errorMessage = text;
            this.message = null;
        } else if (type == ServerMessageType.NOTIFICATION) {
            this.errorMessage = null;
            this.message = text;
        } else {
            this.errorMessage = null;
            this.message = null;
        }
    }

    // Full constructor
    public ServerMessage(ServerMessageType type, ChessGame game, String errorMessage, String message) {
        this.serverMessageType = type;
        this.game = game;
        this.errorMessage = errorMessage;
        this.message = message;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    public ChessGame getGame() {
        return this.game;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage that)) {
            return false;
        }
        return getServerMessageType() == that.getServerMessageType() &&
                Objects.equals(getGame(), that.getGame()) &&
                Objects.equals(getErrorMessage(), that.getErrorMessage()) &&
                Objects.equals(getMessage(), that.getMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType(), getGame(), getErrorMessage(), getMessage());
    }
}
