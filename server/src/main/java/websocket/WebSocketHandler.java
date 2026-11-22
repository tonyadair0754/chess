package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.InvalidMoveException;
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

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler {
    private final AuthDAO authDao;
    private final GameDAO gameDao;
    private final Gson gson = new Gson();
    private final ConnectionManager connectionManager = new ConnectionManager();

    private record AuthenticatedGame(AuthData auth, GameData game) {
    }

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

    private void handleConnect(
            WsMessageContext ctx,
            UserGameCommand command) {

        try {
            AuthenticatedGame sessionData = authenticateAndLoadGame(ctx, command);
            if (sessionData == null) {
                return;
            }

            String username = sessionData.auth.username();

            if (connectionManager.getConnection(ctx.session) != null) {
                sendError(ctx, "Error: already connected");
                return;
            }
            connectionManager.add(ctx, username, command.getAuthToken(), command.getGameID());

            // Send current game state back to the connecting client
            GameData game = sessionData.game();
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

    private void handleMakeMove(WsMessageContext ctx, UserGameCommand command) throws DataAccessException {
        AuthenticatedGame sessionData = authenticateAndLoadGame(ctx, command);
        if (sessionData == null) {
            return;
        }

        ChessMove move = command.getMove();
        if (move == null) {
            sendError(ctx, "Error: missing move");
            return;
        }

        String username = sessionData.auth().username();

        // Verify the WebSocket session is registered for this gameID and user
        ConnectionManager.ClientConnection conn = connectionManager.getConnection(ctx.session);
        if (conn == null) {
            sendError(ctx, "Error: connection not registered for a game");
            return;
        }
        if (conn.gameID() != command.getGameID()) {
            sendError(ctx, "Error: connection does not match game ID");
            return;
        }
        if (!conn.username().equals(username)) {
            sendError(ctx, "Error: authenticated user does not match connection");
            return;
        }
        if (!Objects.equals(conn.authToken(), command.getAuthToken())) {
            sendError(ctx, "Error: auth token does not match connection");
            return;
        }

        GameData gameData = sessionData.game();
        ChessGame chessGame = gameData.game();

        // Reject moves if the game is already over (e.g. resigned or finished)
        if (chessGame == null || isGameOver(chessGame)) {
            sendError(ctx, "Error: game is over");
            return;
        }

        // Confirm the user is an assigned player and it is their turn
        ChessGame.TeamColor playerColor;
        if (username.equals(gameData.whiteUsername())) {
            playerColor = ChessGame.TeamColor.WHITE;
        } else if (username.equals(gameData.blackUsername())) {
            playerColor = ChessGame.TeamColor.BLACK;
        } else {
            sendError(ctx, "Error: observers cannot make moves");
            return;
        }

        if (chessGame.getTeamTurn() != playerColor) {
            sendError(ctx, "Error: not your turn");
            return;
        }

        // Check piece ownership and legal moves before changing the game
        ChessPiece piece = chessGame.getBoard().getPiece(move.getStartPosition());
        if (piece == null || piece.getTeamColor() != playerColor) {
            sendError(ctx, "Error: invalid piece selection");
            return;
        }

        Collection<ChessMove> legalMoves = chessGame.validMoves(move.getStartPosition());
        if (legalMoves == null || !legalMoves.contains(move)) {
            sendError(ctx, "Error: illegal move");
            return;
        }

        // Execute move and save updated game
        try {
            chessGame.makeMove(move);
        } catch (InvalidMoveException e) {
            sendError(ctx, "Error: " + e.getMessage());
            return;
        }

        GameData updatedGame = new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                chessGame
        );
        gameDao.updateGame(updatedGame);

        // 7. Broadcast updated board to everyone; notify everyone else of the move
        ServerMessage loadGame = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, chessGame);
        connectionManager.broadcastToAll(command.getGameID(), loadGame);

        String note = username + " moved from " + formatSquare(move) + ".";
        ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, note);
        connectionManager.broadcastToOthers(command.getGameID(), ctx.session, notification);

        // Broadcast checkmate or stalemate notification to all connected participants
        ChessGame.TeamColor nextTurn = chessGame.getTeamTurn();
        if (chessGame.isInCheckmate(nextTurn)) {
            String msg = nextTurn + " is in checkmate!";
            connectionManager.broadcastToAll(
                    command.getGameID(),
                    new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg)
            );
        } else if (chessGame.isInStalemate(nextTurn)) {
            String msg = "Game ended in stalemate.";
            connectionManager.broadcastToAll(
                    command.getGameID(),
                    new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg)
            );
        }
    }

    private void handleLeave(WsMessageContext ctx, UserGameCommand command) throws DataAccessException {
        AuthenticatedGame sessionData = authenticateAndLoadGame(ctx, command);
        if (sessionData == null) {
            return;
        }

        String username = sessionData.auth().username();
        GameData gameData = sessionData.game();

        ConnectionManager.ClientConnection conn = connectionManager.getConnection(ctx.session);
        if (conn == null || conn.gameID() != command.getGameID() || !conn.username().equals(username)) {
            sendError(ctx, "Error: connection not registered for this game");
            return;
        }

        // Vacate player slot if the client leaving is assigned as white or black
        String newWhite = username.equals(gameData.whiteUsername()) ? null : gameData.whiteUsername();
        String newBlack = username.equals(gameData.blackUsername()) ? null : gameData.blackUsername();

        if (!Objects.equals(newWhite, gameData.whiteUsername()) || !Objects.equals(newBlack, gameData.blackUsername())) {
            GameData updatedGame = new GameData(
                    gameData.gameID(),
                    newWhite,
                    newBlack,
                    gameData.gameName(),
                    gameData.game()
            );
            gameDao.updateGame(updatedGame);
        }

        connectionManager.remove(ctx.session);

        String note = username + " left the game.";
        ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, note);
        connectionManager.broadcastToOthers(command.getGameID(), ctx.session, notification);
    }

    private void handleResign(WsMessageContext ctx, UserGameCommand command) throws DataAccessException {
        AuthenticatedGame sessionData = authenticateAndLoadGame(ctx, command);
        if (sessionData == null) {
            return;
        }

        String username = sessionData.auth().username();
        GameData gameData = sessionData.game();

        ConnectionManager.ClientConnection conn = connectionManager.getConnection(ctx.session);
        if (conn == null || conn.gameID() != command.getGameID() || !conn.username().equals(username)) {
            sendError(ctx, "Error: connection not registered for this game");
            return;
        }

        boolean isWhite = username.equals(gameData.whiteUsername());
        boolean isBlack = username.equals(gameData.blackUsername());
        if (!isWhite && !isBlack) {
            sendError(ctx, "Error: observers cannot resign");
            return;
        }

        ChessGame chessGame = gameData.game();
        if (chessGame == null || isGameOver(chessGame)) {
            sendError(ctx, "Error: game is already over");
            return;
        }

        chessGame.setGameOver(true);
        GameData updatedGame = new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                chessGame
        );
        gameDao.updateGame(updatedGame);

        String note = username + " resigned from the game.";
        ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, note);
        connectionManager.broadcastToAll(command.getGameID(), notification);
    }

    // --- Helper methods ---

    private void sendError(WsContext ctx, String message) {
        ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, message);
        connectionManager.sendToContext(ctx, errorMessage);
    }

    /*
    Authenticates users and retrieves a game
     */
    private AuthenticatedGame authenticateAndLoadGame(WsMessageContext ctx, UserGameCommand command)
            throws DataAccessException {
        AuthData auth = authDao.getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(ctx, "Error: unauthorized");
            return null;
        }

        GameData game = gameDao.getGame(command.getGameID());
        if (game == null) {
            sendError(ctx, "Error: game not found");
            return null;
        }

        return new AuthenticatedGame(auth, game);
    }

    private boolean isGameOver(ChessGame game) {
        if (game.isGameOver()) {
            return true;
        }
        ChessGame.TeamColor turn = game.getTeamTurn();
        return game.isInCheckmate(turn) || game.isInStalemate(turn);
    }

    private String formatSquare(ChessMove move) {
        char startCol = (char) ('a' + move.getStartPosition().getColumn() - 1);
        int startRow = move.getStartPosition().getRow();
        char endCol = (char) ('a' + move.getEndPosition().getColumn() - 1);
        int endRow = move.getEndPosition().getRow();
        return "" + startCol + startRow + " to " + endCol + endRow;
    }
}