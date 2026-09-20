package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KnightMovesCalculator extends SingleStepMovesCalculator {

    /**
     * Defines where a knight can move
     *
     * @param board
     * @param myPosition
     * @return A list of ChessMove objects
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();

        checkMove(2, 1, board, myPosition, moves);
        checkMove(2, -1, board, myPosition, moves);
        checkMove(1, 2, board, myPosition, moves);
        checkMove(-1, 2, board, myPosition, moves);
        checkMove(-1, -2, board, myPosition, moves);
        checkMove(1, -2, board, myPosition, moves);
        checkMove(-2, 1, board, myPosition, moves);
        checkMove(-2, -1, board, myPosition, moves);

        return moves;
    }
}
