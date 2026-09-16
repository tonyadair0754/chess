package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BishopMovesCalculator implements PieceMovesCalculator{

    /**
     * Defines where a bishop can move
     *
     * @param board
     * @param myPosition
     * @return A list of ChessMove objects
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();

        checkDirection(1, 1, board, myPosition, moves);
        checkDirection(-1, 1, board, myPosition, moves);
        checkDirection(-1, -1, board, myPosition, moves);
        checkDirection(1, -1, board, myPosition, moves);

        return moves;
    }

    /**
     * Helper method that checks where the piece can move in one direction
     *
     * @param rowChange
     * @param colChange
     * @param board
     * @param myPosition
     * @param moves
     */
    private void checkDirection(int rowChange, int colChange, ChessBoard board, ChessPosition myPosition, List<ChessMove> moves) {
        int newRow = myPosition.getRow();
        int newCol = myPosition.getColumn();
        ChessPiece myPiece = board.getPiece(myPosition);

        while (newRow + rowChange >= 1 && newRow +rowChange <= 8 && newCol + colChange >= 1 && newCol + colChange <= 8) {
            newRow += rowChange;
            newCol += colChange;

            ChessPosition newPos = new ChessPosition(newRow, newCol);
            ChessPiece thatPiece = board.getPiece(newPos);

            if (thatPiece == null) {
                moves.add(new ChessMove(myPosition, newPos, null));
                continue;
            }

            if (thatPiece.getTeamColor() != myPiece.getTeamColor()) {
                moves.add(new ChessMove(myPosition, newPos, null));
            }
            break;
        }
    }
}
