import chesslib.Board;
import chesslib.move.Move;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        Board board = new Board();
        List<Move> moves = board.legalMoves();
        System.out.println("Legal moves: " + moves);
        System.out.println("Legal moves: " + board.getSideToMove());
    }
}