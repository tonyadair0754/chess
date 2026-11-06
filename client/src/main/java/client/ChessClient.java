package client;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import ui.BoardPrinter;

import java.util.*;

/**
 * Our Chess Client's REPL
 */
public class ChessClient {
    private final ServerFacade facade;
    private State state;
    private String authToken = null;
    private List<GameData> cachedGames;

    public ChessClient(int port) {
        facade = new ServerFacade(port);
        this.state = State.SIGNEDOUT;
        this.cachedGames = new ArrayList<>();
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
                    // Parse JSON so users see a clean message {"message":"Error: unauthorized"} -> Error: unauthorized
                    message = message.replaceAll(".*\"message\"\\s*:\\s*\"([^\"]+)\".*", "$1");
                }
                System.out.println(message);
            }
        }
    }

    private boolean processCommand(String cmd, String[] tokens, Scanner scanner) throws Exception {
        if (state == State.SIGNEDOUT) {
            return processSignedOutCommand(cmd, tokens, scanner);
        } else {
            return processSignedInCommand(cmd, tokens, scanner);
        }
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

        // Render the board from the chosen player's perspective
        if (selectedGame.game() != null) {
            chess.ChessBoard board = selectedGame.game().getBoard();
            BoardPrinter.printBoard(board, color);
        } else {
            System.out.println("Error: Game board state is missing.");
        }
    }

    private void observeGame(String[] tokens, Scanner scanner) throws Exception {
        GameData selectedGame = selectGame(tokens, scanner);
        if (selectedGame == null) {
            return;
        }

        System.out.println("Now observing game '" + selectedGame.gameName() + "'.");

        // Render the board from white's perspective by default
        if (selectedGame.game() != null) {
            chess.ChessBoard board = selectedGame.game().getBoard();
            BoardPrinter.printBoard(board, ChessGame.TeamColor.WHITE);
        } else {
            System.out.println("Error: Game board state is missing.");
        }
    }

    // Handle help by showing the available commands.
    private void help() {
        if (state == State.SIGNEDOUT) {
            System.out.println("  register <USERNAME> <PASSWORD> <EMAIL> - to create an account");
            System.out.println("  login <USERNAME> <PASSWORD> - to log in");
            System.out.println("  quit - exit the program");
            System.out.println("  help - view available commands");
        } else {
            System.out.println("  create <GAMENAME> - create a game");
            System.out.println("  list - list games");
            System.out.println("  play <ID> [WHITE|BLACK] - play a game");
            System.out.println("  observe <ID> - observe a game");
            System.out.println("  logout - log out of your account");
            System.out.println("  help - view available commands");
        }
    }

    private void printPrompt() {
        if (state == State.SIGNEDOUT) {
            System.out.print("\n[LOGGED_OUT] >>> ");
        } else {
            System.out.print("\n[LOGGED_IN] >>> ");
        }
    }
}