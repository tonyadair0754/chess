package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KnightMovesCalculator implements PieceMovesCalculator {

    /**
     * Defines where a knight can move
     *
     * @param board
     * @param position
     * @return A list of ChessMove objects
     */
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        checkDirection(1, -2, moves, board, position);
        checkDirection(2, -1, moves, board, position);
        checkDirection(2, 1, moves, board, position);
        checkDirection(1, 2, moves, board, position);
        checkDirection(-1, 2, moves, board, position);
        checkDirection(-2, 1, moves, board, position);
        checkDirection(-2, -1, moves, board, position);
        checkDirection(-1, -2, moves, board, position);
        return moves;
    }

    /**
     * Helper method that checks where the knight can move in one direction
     *
     * @param rowChange
     * @param colChange
     * @param moves
     * @param board
     * @param position
     */
    private void checkDirection(int rowChange, int colChange, List<ChessMove> moves, ChessBoard board, ChessPosition position) {
        ChessPiece knight = board.getPiece(position);
        int row = position.getRow();
        int col = position.getColumn();

        if ((row + rowChange < 9 && row + rowChange > 0) && (col + colChange < 9 && col + colChange > 0)) {
            row += rowChange;
            col += colChange;
            ChessPosition newPos = new ChessPosition(row, col);
            ChessPiece piece = board.getPiece(newPos);

            if (piece == null) {
                moves.add(new ChessMove(position, newPos, null));
            } else {
                if (piece.getTeamColor() != knight.getTeamColor()) {
                    moves.add(new ChessMove(position, newPos, null));
                }
            }
        }
    }
}
