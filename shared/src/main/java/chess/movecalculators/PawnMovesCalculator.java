package chess.movecalculators;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PawnMovesCalculator implements PieceMovesCalculator{

    /**
     * Defines where a pawn can move
     *
     * @param board
     * @param myPosition
     * @return A list of ChessMove objects
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();

        if (board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE) {
            checkMove(1, -1, board, myPosition, moves);
            checkMove(1, 0, board, myPosition, moves);
            checkMove(1, 1, board, myPosition, moves);
            if (myPosition.getRow() == 2
                    && board.getPiece(new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn())) == null) {
                checkMove(2, 0, board, myPosition, moves);
            }
        } else {
            checkMove(-1, -1, board, myPosition, moves);
            checkMove(-1, 0, board, myPosition, moves);
            checkMove(-1, 1, board, myPosition, moves);
            if (myPosition.getRow() == 7
                    && board.getPiece(new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn())) == null) {
                checkMove(-2, 0, board, myPosition, moves);
            }
        }

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
        ChessPiece thisPiece = board.getPiece(myPosition);

        if (!ChessPosition.inOnBoard(newRow + rowChange, newCol + colChange)) {
            return;
        }

        ChessPosition newPos = new ChessPosition(newRow + rowChange, newCol + colChange);
        ChessPiece thatPiece = board.getPiece(newPos);

        if (colChange == 0) {
            if (thatPiece == null) {
                addMove(myPosition, newPos, moves);
            }
        } else if (thatPiece != null) {
            if (thatPiece.getTeamColor() != thisPiece.getTeamColor()) {
                addMove(myPosition, newPos, moves);
            }
        }
    }

    /**
     * Add move helper
     *
     * @param myPosition
     * @param newPos
     * @param moves
     */
    private void addMove(ChessPosition myPosition, ChessPosition newPos, Collection<ChessMove> moves) {
        if (newPos.getRow() == 1 || newPos.getRow() == 8) {
            moves.add(new ChessMove(myPosition, newPos, ChessPiece.PieceType.ROOK));
            moves.add(new ChessMove(myPosition, newPos, ChessPiece.PieceType.KNIGHT));
            moves.add(new ChessMove(myPosition, newPos, ChessPiece.PieceType.BISHOP));
            moves.add(new ChessMove(myPosition, newPos, ChessPiece.PieceType.QUEEN));
        } else {
            moves.add(new ChessMove(myPosition, newPos, null));
        }
    }
}
