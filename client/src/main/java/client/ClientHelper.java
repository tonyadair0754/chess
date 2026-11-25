package client;

import chess.ChessPiece;
import chess.ChessPosition;

final class ClientHelper {
    private ClientHelper() {
    }

    static ChessPosition parsePosition(String square) {
        if (square == null || square.length() != 2) {
            return null;
        }
        char colChar = square.charAt(0);
        char rowChar = square.charAt(1);
        if (colChar < 'a' || colChar > 'h' || rowChar < '1' || rowChar > '8') {
            return null;
        }
        return new ChessPosition(rowChar - '1' + 1, colChar - 'a' + 1);
    }

    static ChessPiece.PieceType parsePromotion(String promo) {
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

    static void printHelp(State state) {
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

    static void printPrompt(State state) {
        switch (state) {
            case SIGNEDOUT -> System.out.print("\n[LOGGED_OUT] >>> ");
            case SIGNEDIN -> System.out.print("\n[LOGGED_IN] >>> ");
            case IN_GAME -> System.out.print("\n[GAME] >>> ");
        }
    }
}