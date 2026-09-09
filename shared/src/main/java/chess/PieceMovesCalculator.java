package chess;

import java.util.Collection;

/**
 * Calculates piece moves. Each piece type implements this interface.
 */
public interface PieceMovesCalculator {
    Collection<ChessMove> pieceMoves (ChessBoard board, ChessPosition position);
}
