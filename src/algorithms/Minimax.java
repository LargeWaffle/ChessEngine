package algorithms;

import chesslib.Board;
import chesslib.Side;
import chesslib.move.*;

import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;

public class Minimax {
    public Move bestMove;

    public Minimax(String fen, int depth){

        Node source_node = new Node(fen, null);

        boolean isMax = source_node.board.getSideToMove() == Side.WHITE;

        minimax(source_node, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, isMax);
    }
    public void getBestMove(Node src){

        while(src.parent.parent != null){
            src = src.parent;
        }

        bestMove = src.nodeMove;
    }
    public double minimax(Node source, int depth, double alpha, double beta, boolean max){


        //System.out.println("My best move " + bestMove);
        //System.out.println("Im at depth " + (3-depth));
        boolean terminal = source.board.isDraw() || source.board.isMated();

        if (depth == 0 || terminal)
        {
            return source.score;
        }

        source.generateChildren();

        double bestVal;
        if (max){

            bestVal = Double.NEGATIVE_INFINITY;

            for (Node child: source.children) {

                double value = minimax(child, depth - 1, alpha, beta, false);
                if (value > bestVal) {
                    bestVal = value;
                    bestMove = child.nodeMove;
                }

                //bestVal = Math.max(bestVal, value);


                alpha = Math.max(alpha, bestVal);

                if (beta <= alpha)
                    break;

            }

        } else {
            bestVal = Double.POSITIVE_INFINITY;

            for (Node child: source.children) {

                double value = minimax(child, depth - 1, alpha, beta, true);
                //bestVal = Math.min(bestVal, value);

                if (value < bestVal) {
                    bestVal = value;
                    bestMove = child.nodeMove;
                }

                beta = Math.min(beta, bestVal);

                if (beta <= alpha)
                    break;

            }
        }

        /*if (depth == 3)
            System.out.println("At depth " + (4-depth) + ", my best move is " + bestMove + " with a score of " + bestVal);
        if (depth == 4)
            System.out.println("At depth " + (4-depth) + ", my best move is " + bestMove + " with a score of " + bestVal);*/
        return bestVal;
    }

}
