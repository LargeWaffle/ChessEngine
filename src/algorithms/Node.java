package algorithms;

import chesslib.Board;
import chesslib.move.*;

public class Node {

    public Move move;
    public Board board;
    public double score;

    public Node(Move mv, double sc) {
        move = mv;
        score = sc;
    }
}
