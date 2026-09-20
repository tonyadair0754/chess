package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BishopMovesCalculator extends SlidingMovesCalculator {

    /**
     * Defines where a bishop can move
     *
     * @param board
     * @param myPosition
     * @return A list of ChessMove objects
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();

        checkDirection(1, 1, board, myPosition, moves);
        checkDirection(-1, 1, board, myPosition, moves);
        checkDirection(-1, -1, board, myPosition, moves);
        checkDirection(1, -1, board, myPosition, moves);

        return moves;
    }
}
