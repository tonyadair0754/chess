package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PawnMovesCalculator implements PieceMovesCalculator {

    /**
     * Defines where a pawn can move
     *
     * @param board
     * @param position
     * @return A list of ChessMove objects
     */
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        ChessPiece pawn = board.getPiece(position);
        if (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) {
            if (position.getRow() == 2) {
                checkDirection(2, 0, moves, board, position);
            }
            checkDirection(0, -1, moves, board, position);
            checkDirection(1, -1, moves, board, position);
            checkDirection(0, 1, moves, board, position);
            checkDirection(1, 1, moves, board, position);
            checkDirection(1, 0, moves, board, position);
        } else {
            if (position.getRow() == 7) {
                checkDirection(-2, 0, moves, board, position);
            }
            checkDirection(0, -1, moves, board, position);
            checkDirection(-1, -1, moves, board, position);
            checkDirection(-1, 0, moves, board, position);
            checkDirection(-1, 1, moves, board, position);
            checkDirection(0, 1, moves, board, position);
        }
        return moves;
    }

    /**
     * Helper method that checks where the pawn can move in one direction
     *
     * @param rowChange
     * @param colChange
     * @param moves
     * @param board
     * @param position
     */
    private void checkDirection(int rowChange, int colChange, List<ChessMove> moves, ChessBoard board, ChessPosition position) {
        ChessPiece pawn = board.getPiece(position);
        int row = position.getRow();
        int col = position.getColumn();

        if ((row + rowChange < 9 && row + rowChange > 0) && (col + colChange < 9 && col + colChange > 0)) {
            row += rowChange;
            col += colChange;
            ChessPosition newPos = new ChessPosition(row, col);
            ChessPiece piece = board.getPiece(newPos);

            // If the space the pawn moves to is empty
            if (piece == null) {
                if (row == 8 || row == 1) {
                    moves.add(new ChessMove(position, newPos, ChessPiece.PieceType.ROOK));
                    moves.add(new ChessMove(position, newPos, ChessPiece.PieceType.BISHOP));
                    moves.add(new ChessMove(position, newPos, ChessPiece.PieceType.KNIGHT));
                    moves.add(new ChessMove(position, newPos, ChessPiece.PieceType.QUEEN));
                } else {
                    moves.add(new ChessMove(position, newPos, null));
                }
            } else {

                // If capturing
                if (piece.getTeamColor() != pawn.getTeamColor()) {
                    if (rowChange == 1 && (colChange == -1 || colChange == 1)) {
                        if (row == 8) {
                            moves.add(new ChessMove(position, newPos, ChessPiece.PieceType.ROOK));
                            moves.add(new ChessMove(position, newPos, ChessPiece.PieceType.BISHOP));
                            moves.add(new ChessMove(position, newPos, ChessPiece.PieceType.KNIGHT));
                            moves.add(new ChessMove(position, newPos, ChessPiece.PieceType.QUEEN));
                        } else {
                            moves.add(new ChessMove(position, newPos, null));
                        }
                    } else if (rowChange == -1 && (colChange == -1 || colChange == 1)) {
                        if (row == 1) {
                            moves.add(new ChessMove(position, newPos, ChessPiece.PieceType.ROOK));
                            moves.add(new ChessMove(position, newPos, ChessPiece.PieceType.BISHOP));
                            moves.add(new ChessMove(position, newPos, ChessPiece.PieceType.KNIGHT));
                            moves.add(new ChessMove(position, newPos, ChessPiece.PieceType.QUEEN));
                        } else {
                            moves.add(new ChessMove(position, newPos, null));
                        }
                    }
                }
            }
        }
    }
}
