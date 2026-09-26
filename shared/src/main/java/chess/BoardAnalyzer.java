package chess;

/**
 * Contains utility methods for analyzing a chess board
 */
public class BoardAnalyzer {

    public BoardAnalyzer() {

    }

    /**
     * Finds the position of a piece, given that piece's type and color (primarily used to find the king, as there is only one king per team)
     *
     * @param pieceType
     * @param teamColor
     * @return The position of the matching piece, or null if it isn't on the board
     */
    public static ChessPosition findPiecePos(ChessBoard board, ChessPiece.PieceType pieceType, ChessGame.TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null
                        && piece.getPieceType() == pieceType
                        && piece.getTeamColor() == teamColor) {
                    return pos;
                }
            }
        }
        return null;
    }

    /**
     * Checks whether a piece at piecePosition can attack a piece at targetPosition
     *
     * @param board
     * @param piecePosition
     * @param targetPosition
     * @param defendingTeam
     * @return True if the piece at the starting position can attack the piece at the target position
     */
    private static boolean canAttackPosition(ChessBoard board, ChessPosition piecePosition, ChessPosition targetPosition, ChessGame.TeamColor defendingTeam) {
        ChessPiece piece = board.getPiece(piecePosition);

        if (piece == null || piece.getTeamColor() == defendingTeam) {
            return false;
        }

        // Pawns attack diagonally, regardless of whether the target square is empty
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int direction = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
            int rowDiff = targetPosition.getRow() - piecePosition.getRow();
            int colDiff = Math.abs(targetPosition.getColumn() - piecePosition.getColumn());

            return rowDiff == direction && colDiff == 1;
        }

        // Standard piece attacks
        for (ChessMove move : piece.pieceMoves(board, piecePosition)) {
            if (move.getEndPosition().equals(targetPosition)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Evaluates whether targetPosition is under attack by any opposing piece
     *
     * @param targetPosition
     * @param defendingTeam
     * @return
     */
    public static boolean isPositionAttacked(ChessBoard board, ChessPosition targetPosition, ChessGame.TeamColor defendingTeam) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);

                if (canAttackPosition(board, pos, targetPosition, defendingTeam)) {
                    return true;
                }
            }
        }

        return false;
    }
}
