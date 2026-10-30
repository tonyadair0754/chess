package client;

import model.AuthData;

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
                    // Postlogin commands will eventually go here
                    if (cmd.equals("logout")) {
                        logout();
                    } else {
                        System.out.println("Unknown command. Type 'help' for help.");
                    }
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private String register(String[] args) {
        
    }

    private String login(String[] args) {

    }

    private String logout() {

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