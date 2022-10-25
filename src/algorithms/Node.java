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

    public Node(String fenstring){
        board = new Board();

        fen = fenstring;
        board.loadFromFen(fen);

        score = getFenScore();

        children = new ArrayList<>();
    }

    public void generateChildren(){

        List<Move> moveList = board.legalMoves();

        for (Move move: moveList) {

            nodeMove = move;
            board.doMove(nodeMove);
            String current_fen = board.getFen();
            board.undoMove();

            children.add(new Node(current_fen));

            for (Node child: children) {
                child.parent = this;
            }
        }
    }

    public double getFenScore(){
        int index = fen.indexOf(" ");
        String subfen = "";
        if (index != -1)
        {
            subfen= fen.substring(0 , index);
        }

        Side side = board.getSideToMove();

        //int pieceCount[] = {8, 2, 2, 2, 1, 1};

        char[] fen_char = subfen.toCharArray();

        int score = 16;

        for(char fc : fen_char){
            int myInt;

            if (side == Side.WHITE) {
                myInt = isLowerCase(fc) ? 1 : 0;

            }else{
                myInt = isUpperCase(fc) ? 1 : 0;
            }

            score -= myInt;
        }

        return score;
    }
}
