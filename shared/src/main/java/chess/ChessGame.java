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
     * @param currentPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * currentPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition currentPosition) {
        ChessPiece movingPiece = board.getPiece(currentPosition);

        if (movingPiece == null) {
            return null;
        }

        TeamColor movingPieceColor = movingPiece.getTeamColor();
        Collection<ChessMove> candidateMoves = movingPiece.pieceMoves(board, currentPosition);

        // --- En Passant ---
        GameState lastState = gameHistory.get(gameHistory.size() - 1);
        ChessMove lastMove = gameHistory.get(gameHistory.size() - 1).getLastMove();

        if (lastMove != null && movingPiece.getPieceType() == ChessPiece.PieceType.PAWN) {
            ChessPiece lastMovedPiece = board.getPiece(lastMove.getEndPosition());

            if (lastMovedPiece != null && lastMovedPiece.getPieceType() == ChessPiece.PieceType.PAWN && lastMovedPiece.getTeamColor() != movingPieceColor) {
                int startRow = lastMove.getStartPosition().getRow();
                int endRow = lastMove.getEndPosition().getRow();

                boolean oppMovedTwoRows = Math.abs(startRow - endRow) == 2;
                boolean isAdjacentColumn = Math.abs(lastMove.getEndPosition().getColumn() - currentPosition.getColumn()) == 1 && endRow == currentPosition.getRow();
                boolean isSameRow = currentPosition.getRow() == endRow;

                if (oppMovedTwoRows && isAdjacentColumn && isSameRow) {
                    // Calculate the target en passant square halfway between start and end
                    int epRow = (startRow + endRow) / 2;
                    int epCol = lastMove.getEndPosition().getColumn();

                    ChessPosition epDestination = new ChessPosition(epRow, epCol);

                    // Add the valid en passant move
                    candidateMoves.add(new ChessMove(currentPosition, epDestination, null, ChessMove.MoveType.EN_PASSANT));
                }
            }
        }

        // --- Castling ---
        if (movingPiece.getPieceType() == ChessPiece.PieceType.KING && !hasPieceMoved(currentPosition)) {
            int row = (movingPieceColor == TeamColor.WHITE ? 1 : 8);

            // Kingside castling
            ChessPosition kingsideRookPos = new ChessPosition(row, 8);
            if (!hasPieceMoved(kingsideRookPos)) {
                ChessPosition colSix = new ChessPosition(row, 6);
                ChessPosition colSeven = new ChessPosition(row, 7);

                if (board.getPiece(colSix) == null && board.getPiece(colSeven) == null) {
                    if (!isPositionAttacked(currentPosition, movingPieceColor) &&
                            !isPositionAttacked(colSix, movingPieceColor) &&
                            !isPositionAttacked(colSeven, movingPieceColor)) {
                        candidateMoves.add(new ChessMove(currentPosition, colSeven, null, ChessMove.MoveType.CASTLING));
                    }
                }
            }

            // Queenside castling
            ChessPosition queensideRookPos = new ChessPosition(row, 1);
            if (!hasPieceMoved(queensideRookPos)) {
                ChessPosition colTwo = new ChessPosition(row, 2);
                ChessPosition colThree = new ChessPosition(row, 3);
                ChessPosition colFour = new ChessPosition(row, 4);

                if (board.getPiece(colTwo) == null && board.getPiece(colThree) == null && board.getPiece(colFour) == null) {
                    if (!isPositionAttacked(currentPosition, movingPieceColor) &&
                            !isPositionAttacked(colThree, movingPieceColor) &&
                            !isPositionAttacked(colFour, movingPieceColor)) {
                        candidateMoves.add(new ChessMove(currentPosition, colThree, null, ChessMove.MoveType.CASTLING));
                    }
                }
            }
        }

        // --- Temporarily perform the move to see if it places the king in check, then restore the board ---
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : candidateMoves) {
            ChessPosition startPos = move.getStartPosition();
            ChessPosition endPos = move.getEndPosition();
            ChessPiece capturedPiece = board.getPiece(move.getEndPosition());

            ChessPosition epCapturedPos = null;
            ChessPiece epCapturedPiece = null;

            ChessPosition rookStartPos = null;
            ChessPosition rookEndPos = null;
            ChessPiece castleRook = null;

            if (move.getMoveType() == ChessMove.MoveType.EN_PASSANT) {
                epCapturedPos = new ChessPosition(startPos.getRow(), endPos.getColumn());
                epCapturedPiece = board.getPiece(epCapturedPos);
                board.removePiece(epCapturedPos);
            }

            if (move.getMoveType() == ChessMove.MoveType.CASTLING) {
                int row = startPos.getRow();
                if (endPos.getColumn() == 7) { // Kingside
                    rookStartPos = new ChessPosition(row, 8);
                    rookEndPos = new ChessPosition(row, 6);
                } else if (endPos.getColumn() == 3) { // Queenside
                    rookStartPos = new ChessPosition(row, 1);
                    rookEndPos = new ChessPosition(row, 4);
                }

                castleRook = board.getPiece(rookStartPos);
                board.addPiece(rookEndPos, castleRook);
                board.removePiece(rookStartPos);
            }

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

            if (move.getMoveType() == ChessMove.MoveType.EN_PASSANT) {
                board.addPiece(epCapturedPos, epCapturedPiece);
            }

            if (move.getMoveType() == ChessMove.MoveType.CASTLING) {
                board.addPiece(rookStartPos, castleRook);
                board.removePiece(rookEndPos);
            }
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

        if (move.getMoveType() == ChessMove.MoveType.NORMAL) {
            // Add piece to new position
            if (move.getPromotionPiece() != null) {
                board.addPiece(endPos, new ChessPiece(pieceColor, move.getPromotionPiece()));
            } else {
                board.addPiece(endPos, piece);
            }

            board.removePiece(startPos);

        } else if (move.getMoveType() == ChessMove.MoveType.EN_PASSANT) {
            // Move pawn
            board.addPiece(endPos, piece);
            board.removePiece(startPos);

            // Remove captured pawn
            int capturedPawnRow = move.getStartPosition().getRow();
            int capturedPawnCol = move.getEndPosition().getColumn();
            ChessPosition capturedPawnPos = new ChessPosition(capturedPawnRow, capturedPawnCol);
            board.removePiece(capturedPawnPos);
        } else if (move.getMoveType() == ChessMove.MoveType.CASTLING) {
            int row = startPos.getRow();

            // Rook is kingside
            if (endPos.getColumn() == 7) {
                ChessPiece king = board.getPiece(new ChessPosition(row, 5));
                board.addPiece(new ChessPosition(row, 7), king);
                board.removePiece(new ChessPosition(row, 5));

                ChessPiece rook = board.getPiece(new ChessPosition(row, 8));
                board.addPiece(new ChessPosition(row, 6), rook);
                board.removePiece(new ChessPosition(row, 8));

            // Rook is queenside
            } else if (endPos.getColumn() == 3) {
                ChessPiece king = board.getPiece(new ChessPosition(row, 5));
                board.addPiece(new ChessPosition(row, 3), king);
                board.removePiece(new ChessPosition(row, 5));

                ChessPiece rook = board.getPiece(new ChessPosition(row, 1));
                board.addPiece(new ChessPosition(row, 4), rook);
                board.removePiece(new ChessPosition(row, 1));
            }
        }

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
        if (isPositionAttacked(kingPosition, teamColor)) {
            return true;
        } else {
            return false;
        }
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
     * @param defendingTeam
     * @return True if the piece at the starting position can attack the piece at the target position
     */
    private boolean canAttackPosition(ChessPosition piecePosition, ChessPosition targetPosition, TeamColor defendingTeam) {
        ChessPiece piece = board.getPiece(piecePosition);

        if (piece == null || piece.getTeamColor() == defendingTeam) {
            return false;
        }

        // Pawns attack diagonally, regardless of whether the target square is empty
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int direction = (piece.getTeamColor() == TeamColor.WHITE) ? 1 : -1;
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
    private boolean isPositionAttacked(ChessPosition targetPosition, TeamColor defendingTeam) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);

                if (canAttackPosition(pos, targetPosition, defendingTeam)) {
                    return true;
                }
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
     * Used for castling
     * position should be the piece's original starting position
     *
     * @param position
     * @return True if the specified piece has ever moved before during the game
     */
    public boolean hasPieceMoved(ChessPosition position) {
        for (GameState state : gameHistory) {
            ChessMove move = state.getLastMove();

            if (move != null && move.getStartPosition().equals(position)) {
                return true;
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
