package algorithms;

import java.util.*;

import chesslib.Board;
import chesslib.Side;
import chesslib.move.*;

import static java.lang.Character.*;

public class Node {

    public Move nodeMove;
    public Board board;
    public String fen;
    public double score;
    public Node parent = null;
    public List<Node> children;

    public Node(String fenstring, Move mv) {
        board = new Board();
        nodeMove = mv;
        fen = fenstring;
        board.loadFromFen(fen);
        children = new ArrayList<>();
        score = getFenScore();
    }

    public void generateChildren() {

        List<Move> moveList = board.legalMoves();

        for (Move move : moveList) {

            board.doMove(move);
            String current_fen = board.getFen();
            board.undoMove();

            children.add(new Node(current_fen, move));
        }

        for (Node child : children) {
            child.parent = this;
        }
    }

    public double getFenScore() {
        int index = fen.indexOf(" ");
        String subfen = "";
        if (index != -1)
        {
            subfen= fen.substring(0 , index);
        }

        Side side = board.getSideToMove();

        Map <Character, Integer> values = Map.of('q', 9, 'r', 5, 'n', 3, 'b', 3, 'p', 1,
                'Q', 9, 'R', 5, 'N', 3, 'B', 3, 'P', 1);

        char[] fen_char = subfen.toCharArray();

        int score = 0;

        for (char fc : fen_char) {
            if (fc != 'k' && fc != 'K') {
                score -= isLowerCase(fc) ? values.get(fc) : 0;

                score += isUpperCase(fc) ? values.get(fc) : 0;
            }
        }

        if (board.isKingAttacked())
            score += 100000;


        return score;
    }
}
