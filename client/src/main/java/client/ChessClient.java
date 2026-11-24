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
        help();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            printPrompt();
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
            case "help" -> help();
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
            case "help" -> help();
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
            case "help" -> help();
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
        printPrompt();
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

        ChessPosition start = parsePosition(fromStr);
        ChessPosition end = parsePosition(toStr);
        if (start == null || end == null) {
            System.out.println("Invalid square(s). Use a-h followed by 1-8 (e.g. move e2 e4).");
            return;
        }

        ChessPiece.PieceType promotion = parsePromotion(promoStr);
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

    private ChessPiece.PieceType parsePromotion(String promo) {
        if (promo == null) {
            return null;
        }
        return switch (promo.toUpperCase()) {
            case "Q", "QUEEN" -> ChessPiece.PieceType.QUEEN;
            case "R", "ROOK" -> ChessPiece.PieceType.ROOK;
            case "B", "BISHOP" -> ChessPiece.PieceType.BISHOP;
            case "N", "KNIGHT" -> ChessPiece.PieceType.KNIGHT;
            default -> null;
        };
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

        ChessPosition position = parsePosition(squareStr);
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

    private ChessPosition parsePosition(String square) {
        if (square == null || square.length() != 2) {
            return null;
        }
        char colChar = square.charAt(0);
        char rowChar = square.charAt(1);

        if (colChar < 'a' || colChar > 'h' || rowChar < '1' || rowChar > '8') {
            return null;
        }

        int col = colChar - 'a' + 1; // a -> 1, h -> 8
        int row = rowChar - '1' + 1; // 1 -> 1, 8 -> 8
        return new ChessPosition(row, col);
    }

    private void resign(Scanner scanner) {

    }

    private void leaveGame() {

    }

    // Handle help by showing the available commands.
    private void help() {
        switch (state) {
            case SIGNEDOUT -> {
                System.out.println("  register <USERNAME> <PASSWORD> <EMAIL> - to create an account");
                System.out.println("  login <USERNAME> <PASSWORD> - to log in");
                System.out.println("  quit - exit the program");
                System.out.println("  help - view available commands");
            }
            case SIGNEDIN -> {
                System.out.println("  create <GAMENAME> - create a game");
                System.out.println("  list - list games");
                System.out.println("  play <ID> [WHITE|BLACK] - play a game");
                System.out.println("  observe <ID> - observe a game");
                System.out.println("  logout - log out of your account");
                System.out.println("  quit - exit the program");
                System.out.println("  help - view available commands");
            }
            case IN_GAME -> {
                System.out.println("  redraw - redraw the chess board");
                System.out.println("  leave - leave the game and return to menu");
                System.out.println("  move <FROM> <TO> [Q|R|B|N] - make a move (e.g. move e2 e4)");
                System.out.println("  resign - forfeit the game");
                System.out.println("  highlight <SQUARE> - highlight legal moves for a piece (e.g. highlight e2)");
                System.out.println("  quit - exit the program");
                System.out.println("  help - show gameplay commands");
            }
        }
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

    private void printPrompt() {
        switch (state) {
            case SIGNEDOUT -> System.out.print("\n[LOGGED_OUT] >>> ");
            case SIGNEDIN -> System.out.print("\n[LOGGED_IN] >>> ");
            case IN_GAME -> System.out.print("\n[GAME] >>> ");
        }
    }
}