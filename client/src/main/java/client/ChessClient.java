package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;
import ui.BoardPrinter;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.util.*;

/**
 * Our Chess Client's REPL
 */
public class ChessClient {
    private final ServerFacade facade;
    private State state;
    private String authToken = null;
    private List<GameData> cachedGames;
    private int port;
    private WebsocketCommunicator wsCommunicator = null;
    private ChessGame currentGame = null;
    private Integer activeGameID = null;
    private ChessGame.TeamColor playerColor = null; // null when observing

    public ChessClient(int port) {
        facade = new ServerFacade(port);
        this.state = State.SIGNEDOUT;
        this.cachedGames = new ArrayList<>();
        this.port = port;
    }

    public void run() {
        // Display a welcome message and available commands.
        System.out.println("♕ Welcome to the best chess program! Type 'help' to get started. ♕");
        ClientHelper.printHelp(state);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            ClientHelper.printPrompt(state);
            String line = scanner.nextLine();

            try {
                var tokens = line.toLowerCase().split("\\s+");
                var cmd = tokens.length > 0 ? tokens[0] : "";

                if (processCommand(cmd, tokens, scanner)) {
                    return; // Exit loop if quit was entered
                }
            } catch (Exception e) {
                String message = e.getMessage();
                // If the error message is a JSON string containing a message field, extract it
                if (message != null && message.contains("\"message\":")) {
                    // Parse JSON so users see a clean message (e.g. {"message":"Error: unauthorized"} -> Error: unauthorized)
                    message = message.replaceAll(".*\"message\"\\s*:\\s*\"([^\"]+)\".*", "$1");
                }
                System.out.println(message);
            }
        }
    }

    private boolean processCommand(String cmd, String[] tokens, Scanner scanner) throws Exception {
        return switch (state) {
            case SIGNEDOUT -> processSignedOutCommand(cmd, tokens, scanner);
            case SIGNEDIN -> processSignedInCommand(cmd, tokens, scanner);
            case IN_GAME -> processInGameCommand(cmd, tokens, scanner);
        };
    }

    private boolean processSignedOutCommand(String cmd, String[] tokens, Scanner scanner) throws Exception {
        switch (cmd) {
            case "help" -> ClientHelper.printHelp(state);
            case "quit" -> {
                return true;
            }
            case "register" -> register(tokens, scanner);
            case "login" -> login(tokens, scanner);
            default -> System.out.println("Unknown command. Type 'help' for help.");
        }
        return false;
    }

    private boolean processSignedInCommand(String cmd, String[] tokens, Scanner scanner) throws Exception {
        switch (cmd) {
            case "help" -> ClientHelper.printHelp(state);
            case "quit" -> {
                return true;
            }
            case "logout" -> logout();
            case "create" -> createGame(tokens, scanner);
            case "list" -> listGames();
            case "play" -> playGame(tokens, scanner);
            case "observe" -> observeGame(tokens, scanner);
            default -> System.out.println("Unknown command. Type 'help' for help.");
        }
        return false;
    }

    private boolean processInGameCommand(String cmd, String[] tokens, Scanner scanner) throws Exception {
        switch (cmd) {
            case "help" -> ClientHelper.printHelp(state);
            case "redraw" -> redrawBoard(null);
            case "move" -> makeMove(tokens, scanner);
            case "highlight" -> highlightMoves(tokens, scanner);
            case "resign" -> resign(scanner);
            case "leave" -> {
                leaveGame();
                return false;
            }
            case "quit" -> {
                leaveGame();
                return true;
            }
            default -> System.out.println("Unknown command. Type 'help' for help.");
        }
        return false;
    }

    public void notify(ServerMessage message) {
        if (message == null || message.getServerMessageType() == null) {
            return;
        }

        System.out.println();
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                currentGame = message.getGame();
                redrawBoard(null);
            }
            case NOTIFICATION -> System.out.println(message.getMessage());
            case ERROR -> System.out.println(message.getErrorMessage());
        }
        ClientHelper.printPrompt(state);
    }

    private void redrawBoard(Set<ChessPosition> highlights) {
        if (currentGame == null || currentGame.getBoard() == null) {
            System.out.println("Board not available yet.");
            return;
        }
        ChessGame.TeamColor perspective = (playerColor != null) ? playerColor : ChessGame.TeamColor.WHITE;
        if (highlights != null && !highlights.isEmpty()) {
            BoardPrinter.printBoard(currentGame.getBoard(), perspective, highlights);
        } else {
            highlights = Set.of();
            BoardPrinter.printBoard(currentGame.getBoard(), perspective, highlights);
        }
    }

    private void register(String[] tokens, Scanner scanner) throws Exception {
        // If the user provides arguments all at once
        if (tokens.length >= 4) {
            var authData = facade.register(tokens[1], tokens[2], tokens[3]);
            authToken = authData.authToken();
            state = State.SIGNEDIN;
            System.out.println("Successfully registered and logged in as " + authData.username());
        } else {  // If the user enters in the arguments one at a time
            System.out.print("Username: ");
            String username = scanner.next();
            System.out.print("Password: ");
            String password = scanner.next();
            System.out.print("Email: ");
            String email = scanner.next();
            scanner.nextLine();

            var authData = facade.register(username, password, email);
            authToken = authData.authToken();
            state = State.SIGNEDIN;
            System.out.println("Successfully registered and logged in as " + authData.username());
        }
    }

    private void login(String[] tokens, Scanner scanner) throws Exception {
        if (tokens.length >= 3) {
            var authData = facade.login(tokens[1], tokens[2]);
            authToken = authData.authToken();
            state = State.SIGNEDIN;
            System.out.println("Logged in as " + authData.username());
        } else {
            System.out.print("Username: ");
            String username = scanner.next();
            System.out.print("Password: ");
            String password = scanner.next();
            scanner.nextLine();

            var authData = facade.login(username, password);
            authToken = authData.authToken();
            state = State.SIGNEDIN;
            System.out.println("Logged in as " + authData.username());
        }
    }

    private void logout() throws Exception {
        facade.logout(authToken);
        authToken = null;
        state = State.SIGNEDOUT;
        System.out.println("Logged out successfully.");
    }

    private void createGame(String[] tokens, Scanner scanner) throws Exception {
        String gameName;
        if (tokens.length > 1) {
            gameName = tokens[1];
        } else {
            System.out.print("Enter game name: ");
            gameName = scanner.next();
            scanner.nextLine();
        }
        facade.createGame(authToken, gameName);
        System.out.println("Game '" + gameName + "' created successfully.");
    }

    private void listGames() throws Exception {
        Collection<GameData> games = facade.listGames(authToken);
        cachedGames.clear();
        cachedGames.addAll(games);

        if (cachedGames.isEmpty()) {
            System.out.println("No games available.");
            return;
        }

        System.out.println("\n--- Available Games ---");
        int i = 1;
        for (var game : cachedGames) {
            String white = (game.whiteUsername() != null) ? game.whiteUsername() : "[Empty]";
            String black = (game.blackUsername() != null) ? game.blackUsername() : "[Empty]";
            System.out.println(i + ". " + game.gameName() + " | White: " + white + " | Black: " + black);
            i++;
        }
    }

    private GameData selectGame(String[] tokens, Scanner scanner) throws Exception {
        // Ensure we have cached games. Get cached games if the cache is empty.
        if (cachedGames.isEmpty()) {
            Collection<model.GameData> games = facade.listGames(authToken);
            cachedGames.clear();
            cachedGames.addAll(games);
        }

        if (cachedGames.isEmpty()) {
            return null;
        }

        int listNumber;
        if (tokens.length >= 2) {
            try {
                listNumber = Integer.parseInt(tokens[1]);
            } catch (NumberFormatException e) {
                System.out.println("Error: Game number must be an integer.");
                return null;
            }
        } else {
            System.out.print("Enter game number: ");
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input. Enter game number: ");
                scanner.next();
            }
            listNumber = scanner.nextInt();
            scanner.nextLine();
        }

        // Validate list number bounds
        if (listNumber < 1 || listNumber > cachedGames.size()) {
            System.out.println("Error: Invalid game number.");
            return null;
        }

        // UI index is 1-based, but gameID is 0-based
        return cachedGames.get(listNumber - 1);
    }

    private void playGame(String[] tokens, Scanner scanner) throws Exception {
        GameData selectedGame = selectGame(tokens, scanner);
        if (selectedGame == null) {
            return;
        }

        String colorStr;
        // Check if color was passed as a token (e.g. "play 1 black")
        if (tokens.length >= 3) {
            colorStr = tokens[2].toUpperCase();
        } else {
            System.out.print("Choose team color (WHITE/BLACK): ");
            colorStr = scanner.next().toUpperCase();
            scanner.nextLine();
        }

        chess.ChessGame.TeamColor color;
        if (colorStr.equals("WHITE") || colorStr.equals("W")) {
            color = chess.ChessGame.TeamColor.WHITE;
        } else if (colorStr.equals("BLACK") || colorStr.equals("B")) {
            color = chess.ChessGame.TeamColor.BLACK;
        } else {
            System.out.println("Error: Invalid color. Must be WHITE or BLACK.");
            return;
        }

        // Call the server facade to join the game
        facade.joinGame(color, selectedGame.gameID(), authToken);
        System.out.println("Successfully joined game '" + selectedGame.gameName() + "' as " + color + ".");

        // Open WebSocket, enter IN_GAME state, and send CONNECT
        enterGameSession(selectedGame.gameID(), color);
    }

    private void observeGame(String[] tokens, Scanner scanner) throws Exception {
        GameData selectedGame = selectGame(tokens, scanner);
        if (selectedGame == null) {
            return;
        }

        System.out.println("Now observing game '" + selectedGame.gameName() + "'.");
        enterGameSession(selectedGame.gameID(), null);
    }

    private void makeMove(String[] tokens, Scanner scanner) throws Exception {
        if (wsCommunicator == null || activeGameID == null) {
            System.out.println("Not connected to a game.");
            return;
        }

        String fromStr;
        String toStr;
        String promoStr = null;

        if (tokens.length >= 3) {
            fromStr = tokens[1];
            toStr = tokens[2];
            if (tokens.length >= 4) {
                promoStr = tokens[3];
            }
        } else {
            System.out.print("From square (e.g. e2): ");
            fromStr = scanner.nextLine().trim().toLowerCase();
            System.out.print("To square (e.g. e4): ");
            toStr = scanner.nextLine().trim().toLowerCase();
            System.out.print("Promotion piece (Q/R/B/N or blank): ");
            String inputPromo = scanner.nextLine().trim();
            if (!inputPromo.isEmpty()) {
                promoStr = inputPromo;
            }
        }

        ChessPosition start = ClientHelper.parsePosition(fromStr);
        ChessPosition end = ClientHelper.parsePosition(toStr);
        if (start == null || end == null) {
            System.out.println("Invalid square(s). Use a-h followed by 1-8 (e.g. move e2 e4).");
            return;
        }

        ChessPiece.PieceType promotion = ClientHelper.parsePromotion(promoStr);
        if (promoStr != null && promotion == null) {
            System.out.println("Invalid promotion piece. Use Q, R, B, or N.");
            return;
        }

        ChessMove move = new ChessMove(start, end, promotion);
        UserGameCommand cmd = new UserGameCommand(
                UserGameCommand.CommandType.MAKE_MOVE,
                authToken,
                activeGameID,
                move
        );
        wsCommunicator.sendCommand(cmd);
    }

    private void highlightMoves(String[] tokens, Scanner scanner) {
        if (currentGame == null || currentGame.getBoard() == null) {
            System.out.println("Board not available yet.");
            return;
        }

        String squareStr;
        if (tokens.length >= 2) {
            squareStr = tokens[1];
        } else {
            System.out.print("Enter square to highlight (e.g. e2): ");
            squareStr = scanner.nextLine().trim().toLowerCase();
        }

        ChessPosition position = ClientHelper.parsePosition(squareStr);
        if (position == null) {
            System.out.println("Invalid square. Use a-h followed by 1-8 (e.g. e2).");
            return;
        }

        ChessPiece piece = currentGame.getBoard().getPiece(position);
        if (piece == null) {
            System.out.println("No piece at " + squareStr + ".");
            return;
        }

        Collection<ChessMove> validMoves = currentGame.validMoves(position);
        Set<ChessPosition> highlights = new HashSet<>();
        if (validMoves != null) {
            for (ChessMove move : validMoves) {
                highlights.add(move.getEndPosition());
            }
        }

        if (highlights.isEmpty()) {
            System.out.println("No legal moves for piece at " + squareStr + ".");
        }

        redrawBoard(highlights);
    }

    private void resign(Scanner scanner) {
        if (wsCommunicator == null || activeGameID == null) {
            System.out.println("Not connected to a game.");
            return;
        }

        if (playerColor == null) {
            System.out.println("Observers cannot resign from a game.");
            return;
        }

        System.out.print("Are you sure you want to resign? (y/n): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        if (!confirmation.equals("y") && !confirmation.equals("yes")) {
            System.out.println("Resignation cancelled.");
            return;
        }

        try {
            UserGameCommand resignCmd = new UserGameCommand(
                    UserGameCommand.CommandType.RESIGN,
                    authToken,
                    activeGameID
            );
            wsCommunicator.sendCommand(resignCmd);
        } catch (Exception e) {
            System.out.println("Failed to send resign command: " + e.getMessage());
        }
    }

    private void leaveGame() {
        if (wsCommunicator != null && activeGameID != null) {
            try {
                UserGameCommand leaveCmd = new UserGameCommand(
                        UserGameCommand.CommandType.LEAVE,
                        authToken,
                        activeGameID
                );
                wsCommunicator.sendCommand(leaveCmd);
            } catch (Exception e) {
                System.err.println("Failed to send leave command: " + e.getMessage());
            }
        }

        cleanupGameSession();
        if (state == State.IN_GAME) {
            state = State.SIGNEDIN;
        }
        System.out.println("Left the game.");
    }

    private void enterGameSession(int gameID, ChessGame.TeamColor color) throws Exception {
        this.activeGameID = gameID;
        this.playerColor = color;
        this.currentGame = null;
        this.state = State.IN_GAME;

        this.wsCommunicator = new WebsocketCommunicator(port, this::notify);
        UserGameCommand connectCmd = new UserGameCommand(
                UserGameCommand.CommandType.CONNECT,
                authToken,
                gameID
        );
        wsCommunicator.sendCommand(connectCmd);
    }

    private void cleanupGameSession() {
        if (wsCommunicator != null) {
            try {
                wsCommunicator.close();
            } catch (Exception ignored) {
            }
            wsCommunicator = null;
        }
        activeGameID = null;
        playerColor = null;
        currentGame = null;
    }
}