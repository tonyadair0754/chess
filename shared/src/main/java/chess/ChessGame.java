package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessGame.TeamColor turn;
    private ChessBoard board;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return turn == chessGame.turn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(turn, board);
    }

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        turn = ChessGame.TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece movingPiece = board.getPiece(startPosition);

        if (movingPiece == null) {
            return null;
        }

        TeamColor movingPieceColor = movingPiece.getTeamColor();
        Collection<ChessMove> candidateMoves = movingPiece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : candidateMoves) {
            ChessPosition startPos = move.getStartPosition();
            ChessPosition endPos = move.getEndPosition();
            ChessPiece capturedPiece = board.getPiece(move.getEndPosition());

            // Temporarily add piece to new position
            if (move.getPromotionPiece() != null) {
                board.addPiece(endPos, new ChessPiece(movingPieceColor, move.getPromotionPiece()));
            } else {
                board.addPiece(endPos, movingPiece);
            }

            // Temporarily clear piece from old position
            board.removePiece(startPos);

            // Add the move if it doesn't put that team's king in check
            if (!isInCheck(movingPieceColor)) {
                validMoves.add(move);
            }

            // Restore the board
            // The move hasn't actually been made yet. That's makeMove()'s job
            board.addPiece(startPos, movingPiece);
            board.addPiece(endPos, capturedPiece);
        }

        return validMoves;
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException("Invalid move - there is no piece at the specified location.");
        }

        TeamColor pieceColor = piece.getTeamColor();
        if (turn != pieceColor) {
            throw new InvalidMoveException("Invalid move - it is not that team's turn.");
        }

        ChessPosition startPos = move.getStartPosition();
        ChessPosition endPos = move.getEndPosition();
        Collection<ChessMove> validMoves = validMoves(startPos);

        if (!validMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move - that location is an invalid move for the specified piece.");
        }

        // Add piece to new position
        if (move.getPromotionPiece() != null) {
            board.addPiece(endPos, new ChessPiece(pieceColor, move.getPromotionPiece()));
        } else {
            board.addPiece(endPos, piece);
        }

        // Clear piece from old position
        board.removePiece(startPos);

        // Switch to other team's turn
        if (pieceColor == TeamColor.WHITE) {
            setTeamTurn(TeamColor.BLACK);
        } else {
            setTeamTurn(TeamColor.WHITE);
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team’s king could be captured by an opposing piece.
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = null;

        // Find the king's position
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null) {
                    if (piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                        kingPosition = pos;
                    }
                }
            }
        }

        // Check all moves of the opposing team's pieces
        // Return true if any of their moves are equal to the king's position
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                if (piece != null && piece.getTeamColor() != teamColor) {
                    for (ChessMove move : piece.pieceMoves(board, pos)) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the given team has no way to protect their king from being captured.
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }

        return !hasValidMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the given team has no legal moves but their king is not in immediate danger.
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        return !hasValidMoves(teamColor);
    }

    private boolean hasValidMoves(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    if (!validMoves(pos).isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
