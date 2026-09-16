package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KnightMovesCalculator implements PieceMovesCalculator{

    /**
     * Defines where a knight can move
     *
     * @param board
     * @param myPosition
     * @return A list of ChessMove objects
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();

        checkMove(2, 1, board, myPosition, moves);
        checkMove(2, -1, board, myPosition, moves);
        checkMove(1, 2, board, myPosition, moves);
        checkMove(-1, 2, board, myPosition, moves);
        checkMove(-1, -2, board, myPosition, moves);
        checkMove(1, -2, board, myPosition, moves);
        checkMove(-2, 1, board, myPosition, moves);
        checkMove(-2, -1, board, myPosition, moves);

        return moves;
    }

    /**
     * Helper method that checks whether the piece can move to a specified space
     *
     * @param rowChange
     * @param colChange
     * @param board
     * @param myPosition
     * @param moves
     */
    private void checkMove(int rowChange, int colChange, ChessBoard board, ChessPosition myPosition, List<ChessMove> moves) {
        int newRow = myPosition.getRow();
        int newCol = myPosition.getColumn();
        ChessPiece myPiece = board.getPiece(myPosition);

        if (newRow + rowChange >= 1 && newRow +rowChange <= 8 && newCol + colChange >= 1 && newCol + colChange <= 8) {
            ChessPosition newPos = new ChessPosition(newRow + rowChange, newCol + colChange);
            ChessPiece thatPiece = board.getPiece(newPos);

            if (thatPiece == null) {
                moves.add(new ChessMove(myPosition, newPos, null));
            } else if (thatPiece.getTeamColor() != myPiece.getTeamColor()) {
                moves.add(new ChessMove(myPosition, newPos, null));
            }
        }
    }
}
