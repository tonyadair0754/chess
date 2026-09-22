package chess.movesCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.List;

public abstract class SlidingMovesCalculator implements PieceMovesCalculator {

    /**
     * Helper method that checks where a sliding piece (a rook, bishop, or queen) can move in one direction
     *
     * @param rowChange
     * @param colChange
     * @param board
     * @param myPosition
     * @param moves
     */
    protected void checkDirection(
            int rowChange,
            int colChange,
            ChessBoard board,
            ChessPosition myPosition,
            List<ChessMove> moves) {

        int newRow = myPosition.getRow();
        int newCol = myPosition.getColumn();
        ChessPiece myPiece = board.getPiece(myPosition);

        while (newRow + rowChange >= 1 && newRow + rowChange <= 8 && newCol + colChange >= 1 && newCol + colChange <= 8) {
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
