package client;

import chess.ChessBoard;
import chess.ChessGame;

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
        System.out.println("    a  b  c  d  e  f  g  h  ");

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
        System.out.println("    a  b  c  d  e  f  g  h  ");
    }

    public static void printBlackPerspective(ChessBoard board) {
        // Print top file header (h to a)
        System.out.println("    h  g  f  e  d  c  b  a  ");

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
        System.out.println("    h  g  f  e  d  c  b  a  ");
    }

    public static void printSquare(ChessBoard board, int row, int col) {

    }
}
