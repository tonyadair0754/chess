package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueenMovesCalculator extends SlidingMovesCalculator {

    /**
     * Defines where a queen can move
     *
     * @param board
     * @param myPosition
     * @return A list of ChessMove objects
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();

        checkDirection(1, 0, board, myPosition, moves);
        checkDirection(-1, 0, board, myPosition, moves);
        checkDirection(0, 1, board, myPosition, moves);
        checkDirection(0, -1, board, myPosition, moves);
        checkDirection(1, 1, board, myPosition, moves);
        checkDirection(-1, 1, board, myPosition, moves);
        checkDirection(-1, -1, board, myPosition, moves);
        checkDirection(1, -1, board, myPosition, moves);

        return moves;
    }
}
