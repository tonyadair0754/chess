package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

public class BoardPrinter {
    public static void printBoard(ChessBoard board, ChessGame.TeamColor perspective) {
        if (perspective == ChessGame.TeamColor.WHITE) {
            printWhitePerspective(board);
        } else {
            printBlackPerspective(board);
        }
    }

    public static void printWhitePerspective(ChessBoard board) {
        // Print top file header (a to h)
        System.out.println("    a  b   c   d   e  f   g   h  ");

        // Ranks run from 8 down to 1 from top to bottom
        for (int row = 8; row >= 1; row--) {
            System.out.print(" " + row + " "); // Left rank label
            for (int col = 1; col <= 8; col++) {
                printSquare(board, row, col);
            }
            System.out.print(" " + row + " "); // Right rank label
            System.out.println();
        }

        // Print bottom file header (a to h)
        System.out.println("    a  b   c   d   e  f   g   h  ");
    }

    public static void printBlackPerspective(ChessBoard board) {
        // Print top file header (h to a)
        System.out.println("    h  g   f   e   d  c   b   a  ");

        // Ranks run from 1 up to 8 from top to bottom
        for (int row = 1; row <= 8; row++) {
            System.out.print(" " + row + " "); // Left rank label
            for (int col = 8; col >= 1; col--) {
                printSquare(board, row, col);
            }
            System.out.print(" " + row + " "); // Right rank label
            System.out.println();
        }

        // Print bottom file header (h to a)
        System.out.println("    h  g   f   e   d  c   b   a  ");
    }

    public static void printSquare(ChessBoard board, int row, int col) {
        // Alternate background square color light and dark
        boolean isLightSquare = (row + col) % 2 != 0;
        String bgColor = isLightSquare ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY : EscapeSequences.SET_BG_COLOR_DARK_GREY;

        // Retrieve piece
        ChessPosition pos = new ChessPosition(row, col);
        ChessPiece piece = board.getPiece(pos);

        String pieceSymbol = getPieceSymbol(piece);

        System.out.print(bgColor + pieceSymbol + EscapeSequences.RESET_BG_COLOR);
    }

    private static String getPieceSymbol(ChessPiece piece) {
        if (piece == null) {
            return EscapeSequences.EMPTY;
        }

        boolean isWhite = (piece.getTeamColor() == ChessGame.TeamColor.WHITE);

        return switch (piece.getPieceType()) {
            case KING -> isWhite ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
            case QUEEN -> isWhite ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
            case BISHOP -> isWhite ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
            case KNIGHT -> isWhite ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
            case ROOK -> isWhite ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
            case PAWN -> isWhite ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
        };
    }

    /**
     * Method for testing the board
     **/
    public static void main(String[] args) {
        ChessBoard board = new ChessBoard();
        board.resetBoard();

        System.out.println("=== WHITE PERSPECTIVE ===");
        BoardPrinter.printBoard(board, ChessGame.TeamColor.WHITE);

        System.out.println("\n=== BLACK PERSPECTIVE ===");
        BoardPrinter.printBoard(board, ChessGame.TeamColor.BLACK);
    }
}
