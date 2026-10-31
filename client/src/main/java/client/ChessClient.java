package client;

import model.AuthData;
import model.GameData;

import java.util.Arrays;
import java.util.Scanner;

/**
 * Our Chess Client's REPL
 */
public class ChessClient {
    private final ServerFacade facade;
    private State state;
    private String authToken = null;

    public ChessClient(int port) {
        facade = new ServerFacade(port);
        this.state = State.SIGNEDOUT;
    }

    public void run() {
        // Display a welcome message and available commands.
        System.out.println("♕ Welcome to the best chess program! Type 'help' to get started. ♕");
        help();

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (true) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                var tokens = line.toLowerCase().split("\\s+");
                var cmd = tokens.length > 0 ? tokens[0] : "";

                if (state == State.SIGNEDOUT) {
                    switch (cmd) {
                        case "help" -> help();
                        case "quit" -> {
                            return;
                        }
                        case "register" -> register(tokens, scanner);
                        case "login" -> login(tokens, scanner);
                        default -> System.out.println("Unknown command. Type 'help' for help.");
                    }
                } else {
                    switch (cmd) {
                        case "help" -> help();
                        case "logout" -> logout();
                        case "create" -> createGame(tokens, scanner);
                        case "list" -> listGames();
                        case "play" -> playGame(tokens, scanner);
                        case "observe" -> observeGame(tokens, scanner);
                        default -> System.out.println("Unknown command. Type 'help' for help.");
                    }
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
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

    private void createGame(String[] tokens, Scanner scanner) throws Exception) {

    }

    private void listGames() throws Exception {

    }

    private GameData selectGame(String[] tokens, Scanner scanner) throws Exception {

    }

    private void playGame(String[] tokens, Scanner scanner) throws Exception {

    }

    private void observeGame(String[] tokens, Scanner scanner) throws Exception) {

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