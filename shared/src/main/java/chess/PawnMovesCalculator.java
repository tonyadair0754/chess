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
            if (position.getRow() == 2
                    && board.getPiece(new ChessPosition(position.getRow() + 1, position.getColumn())) == null) {
                checkMove(2, 0, moves, board, position);
            }
            checkMove(1, -1, moves, board, position);
            checkMove(1, 1, moves, board, position);
            checkMove(1, 0, moves, board, position);
        } else {
            if (position.getRow() == 7
                    && board.getPiece(new ChessPosition(position.getRow() - 1, position.getColumn())) == null) {
                checkMove(-2, 0, moves, board, position);
            }
            checkMove(-1, -1, moves, board, position);
            checkMove(-1, 0, moves, board, position);
            checkMove(-1, 1, moves, board, position);
        }
        return moves;
    }

    /**
     * Helper method that checks whether the pawn can move to a specified space
     *
     * @param rowChange
     * @param colChange
     * @param moves
     * @param board
     * @param position
     */
    private void checkMove(int rowChange, int colChange, List<ChessMove> moves, ChessBoard board, ChessPosition position) {
        int newRow = position.getRow() + rowChange;
        int newCol = position.getColumn() + colChange;

        // If destination is off board, return
        if (newRow < 1 || newRow > 8 || newCol < 1 || newCol > 8) {
            return;
        }

        ChessPosition newPos = new ChessPosition(newRow, newCol);
        ChessPiece pawn = board.getPiece(position);
        ChessPiece piece = board.getPiece(newPos);

        /*
        If moving forward,
            if destination is empty,
                add move
            return

        if moving diagonally,
            if destination contains enemy,
                add move
         */
        if (colChange == 0) {
            if (piece == null) {
                addMove(position, newPos, moves);
            }
            return;
        }
        if (piece != null) {
            if (piece.getTeamColor() != pawn.getTeamColor()) {
                addMove(position, newPos, moves);
            }
        }
    }

    /**
     * Add move helper
     *
     * @param start
     * @param end
     * @param moves
     */
    private void addMove(ChessPosition start, ChessPosition end, List<ChessMove> moves) {
        if (end.getRow() == 8 || end.getRow() == 1) {
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.ROOK));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.BISHOP));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.KNIGHT));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.QUEEN));
        } else {
            moves.add(new ChessMove(start, end, null));
        }
    }
}
