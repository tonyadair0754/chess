package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KingMovesCalculator extends SingleStepMovesCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();

        checkMove(1, 0, board, myPosition, moves);
        checkMove(-1, 0, board, myPosition, moves);
        checkMove(0, 1, board, myPosition, moves);
        checkMove(0, -1, board, myPosition, moves);
        checkMove(1, 1, board, myPosition, moves);
        checkMove(-1, 1, board, myPosition, moves);
        checkMove(-1, -1, board, myPosition, moves);
        checkMove(1, -1, board, myPosition, moves);

        return moves;
    }
}
