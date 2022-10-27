package algorithms;

import java.util.*;

import chesslib.Board;
import chesslib.Side;
import chesslib.move.*;

import static java.lang.Character.*;

public class Node {

    public Move move;
    public Board board;
    public double score;

    public Node(Move mv, double sc) {
        move = mv;
        score = sc;
    }
}
