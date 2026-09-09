package chess;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] board = new ChessPiece[8][8];

    public ChessBoard() {
        
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow()-1][position.getColumn()-1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow()-1][position.getColumn()-1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        board = new ChessPiece[8][8];

        // One row of pawns on rows 2 and 7. Assume that values() predicts WHITE first
        int row = 2;
        for (ChessGame.TeamColor color : ChessGame.TeamColor.values()) {
            for (int col = 1; col < 9; col++) {
                ChessPiece piece = new ChessPiece(color, ChessPiece.PieceType.PAWN);
                ChessPosition pos = new ChessPosition(row, col);
                addPiece(pos, piece);
            }
            row = 7;
        }

        // One row of r, n, b, q, k, b, n, r on rows 1 and 8
        row = 1;
        for (ChessGame.TeamColor color : ChessGame.TeamColor.values()) {
            ChessPiece piece = new ChessPiece(color, ChessPiece.PieceType.ROOK);
            ChessPosition pos = new ChessPosition(row, 1);
            addPiece(pos, piece);

            piece = new ChessPiece(color, ChessPiece.PieceType.BISHOP);
            pos = new ChessPosition(row, 2);
            addPiece(pos, piece);

            piece = new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
            pos = new ChessPosition(row, 3);
            addPiece(pos, piece);

            piece = new ChessPiece(color, ChessPiece.PieceType.QUEEN);
            pos = new ChessPosition(row, 4);
            addPiece(pos, piece);

            piece = new ChessPiece(color, ChessPiece.PieceType.KING);
            pos = new ChessPosition(row, 5);
            addPiece(pos, piece);

            piece = new ChessPiece(color, ChessPiece.PieceType.BISHOP);
            pos = new ChessPosition(row, 6);
            addPiece(pos, piece);

            piece = new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
            pos = new ChessPosition(row, 7);
            addPiece(pos, piece);

            piece = new ChessPiece(color, ChessPiece.PieceType.ROOK);
            pos = new ChessPosition(row, 8);
            addPiece(pos, piece);

            row = 8;
        }
    }
}
