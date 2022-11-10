import algorithms.Minimax;
import algorithms.StartTree;
import chesslib.Board;
import chesslib.move.Move;

import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        StartTree st = new StartTree();
        UCI uci = new UCI();
        UCI.uciCommunication();
        /*Board board = new Board();
        board.doMove("e4");
        board.doMove("e6");
        board.doMove("f1c4");
        board.doMove("d8g5");
        while (true) {
            Minimax algo = new Minimax(board, 4);
            board.doMove(algo.bestMove);
            System.out.println(board.toString());
            System.out.println("\n\n");
            Thread.sleep(3000);
        }*/
    }
}