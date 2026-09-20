package chess;

import java.util.List;

public abstract class SingleStepMovesCalculator implements PieceMovesCalculator {

    /**
     * Helper method that checks where a piece that moves in a single step (a knight or king) can move
     *
     * @param rowChange
     * @param colChange
     * @param board
     * @param myPosition
     * @param moves
     */
    protected void checkMove(
            int rowChange,
            int colChange,
            ChessBoard board,
            ChessPosition myPosition,
            List<ChessMove> moves) {

        int newRow = myPosition.getRow();
        int newCol = myPosition.getColumn();
        ChessPiece myPiece = board.getPiece(myPosition);

        if (newRow + rowChange >= 1 && newRow + rowChange <= 8 && newCol + colChange >= 1 && newCol + colChange <= 8) {
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
