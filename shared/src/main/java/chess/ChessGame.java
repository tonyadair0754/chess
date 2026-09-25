package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    private final List<GameState> gameHistory;

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

        gameHistory = new ArrayList<>();
        gameHistory.add(new GameState(board.getBoardSnapshot(), turn, null));
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

        // Normal candidate moves
        Collection<ChessMove> candidateMoves = movingPiece.pieceMoves(board, startPosition);

        TeamColor movingPieceColor = movingPiece.getTeamColor();
        Collection<ChessMove> validMoves = new ArrayList<>();

        // Add special moves
        boolean previousMoveExists = gameHistory.get(gameHistory.size() - 1).getLastMove() != null;
        if (previousMoveExists) {
            ChessMove lastMove = gameHistory.get(gameHistory.size() - 1).getLastMove();
            ChessPiece lastMovedPiece = board.getPiece(lastMove.getEndPosition());

            // En passant
            boolean movedPieceWasPawn = lastMovedPiece.getPieceType() == ChessPiece.PieceType.PAWN;
            boolean movedPieceWasOpp = lastMovedPiece.getTeamColor() != movingPieceColor;
            boolean movedTwoRows = Math.abs(lastMove.getStartPosition().getRow() - lastMove.getEndPosition().getRow()) == 2;
            boolean isBesideMovingPiece = Math.abs(lastMove.getEndPosition().getColumn() - startPosition.getColumn()) == 1
                    && lastMove.getEndPosition().getRow() == startPosition.getRow();

            if (movedPieceWasPawn && movedPieceWasOpp && movedTwoRows && isBesideMovingPiece) {
                candidateMoves.add(new ChessMove(startPosition, lastMove.getStartPosition(), null));
            }

            // Castling
            // 1. Has the king moved yet?
            // 2. Has the chosen rook moved yet?
            // 3. Are the squares between the king and rook empty?
            // 4. Is the king in check?
            // 5. Is the square the king passes through attacked?
            // 6. Is the square the king ends on attacked?
        }


        // Temporarily perform the move to see if it places the king in check, then restore the board
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
     * Determines if a given team has any valid moves
     *
     * @param teamColor
     * @return True if the given team has any possible valid moves
     */
    private boolean hasValidMoves(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                if (piece != null
                        && piece.getTeamColor() == teamColor
                        && !validMoves(pos).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        // Validate the move
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

        // Add history entry once the move has been successfully completed
        gameHistory.add(new GameState(board.getBoardSnapshot(), turn, move));
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team’s king could be captured by an opposing piece.
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findPiecePos(ChessPiece.PieceType.KING, teamColor);

        // Check whether any opposing piece can attack the king
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);

                if (canAttackPosition(pos, kingPosition, teamColor)) {
                    return true;
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

    /**
     * Checks whether a piece at piecePosition can attack a piece at targetPosition
     *
     * @param piecePosition
     * @param targetPosition
     * @param targetTeam
     * @return True if the piece at the starting position can attack the piece at the target position
     */
    private boolean canAttackPosition(ChessPosition piecePosition, ChessPosition targetPosition, TeamColor targetTeam) {
        ChessPiece piece = board.getPiece(piecePosition);

        if (piece == null || piece.getTeamColor() == targetTeam) {
            return false;
        }

        for (ChessMove move : piece.pieceMoves(board, piecePosition)) {
            // Filter out a pawn's forward moves when determining whether that pawn is attacking
            if (piece.getPieceType() == ChessPiece.PieceType.PAWN
                    && move.getEndPosition().getColumn() - move.getStartPosition().getColumn() == 0) {
                continue;
            }

            if (move.getEndPosition().equals(targetPosition)) {
                return true;
            }
        }

        return false;
    }

    private ChessPosition findPiecePos(ChessPiece.PieceType pieceType, TeamColor teamColor) {
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

    public List<GameState> getGameHistory() {
        return gameHistory;
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
