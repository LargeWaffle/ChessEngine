package algorithms;

import chesslib.Board;
import chesslib.Side;
import chesslib.move.*;

public class Minimax {
    public Move bestMove;

    public Minimax(String fen, int depth){

        Node source_node = new Node(fen, null);
        double bestVal = minimax(source_node, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
    }
    public void getBestMove(Node src){

        while(src.parent.parent != null){
            src = src.parent;
        }

        bestMove = src.nodeMove;
    }
    public double minimax(Node source, int depth, double alpha, double beta, boolean max){

        boolean terminal = source.board.isDraw() || source.board.isMated();

        if (depth == 0 || terminal)
        {
            bestMove = source.nodeMove;
            return source.score;
        }

        source.generateChildren();

        double bestVal;
        if (max){

            bestVal = Double.NEGATIVE_INFINITY;

            for (Node child: source.children) {

                double value = minimax(child, depth - 1, alpha, beta, false);
                bestVal = Math.max(bestVal, value);

                if (bestVal == value)
                    bestMove = child.nodeMove;

                alpha = Math.max(alpha, bestVal);

                if (beta <= alpha)
                    break;

            }

        } else {
            bestVal = Double.POSITIVE_INFINITY;

            for (Node child: source.children) {

                double value = minimax(child, depth - 1, alpha, beta, true);
                bestVal = Math.min(bestVal, value);

                if (bestVal == value)
                    bestMove = child.nodeMove;

                beta = Math.min(beta, bestVal);

                if (beta <= alpha)
                    break;

            }
        }
        return bestVal;
    }

}
