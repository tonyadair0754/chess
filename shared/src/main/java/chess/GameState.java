package chess;

import java.util.Objects;

/**
 * Represents what the board looks like at a given point in the game.
 * It is a record of what the game looked like; it is not responsible for modifying the game.
 */
public class GameState {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GameState gameState = (GameState) o;
        return Objects.equals(board, gameState.board) && turn == gameState.turn && Objects.equals(lastMove, gameState.lastMove);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, turn, lastMove);
    }

    private final ChessBoard board;
    private final ChessGame.TeamColor turn;
    private final ChessMove lastMove;

    public GameState(ChessBoard board, ChessGame.TeamColor turn, ChessMove lastMove) {
        this.board = board;
        this.turn = turn;
        this.lastMove = lastMove;
    }

    public ChessBoard getBoard() {
        return board;
    }

    public ChessGame.TeamColor getTurn() {
        return turn;
    }

    public ChessMove getLastMove() {
        return lastMove;
    }
}
