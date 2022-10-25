package algorithms;

import chesslib.Board;
import chesslib.Side;
import chesslib.move.*;

public class Minimax {
    public Move bestMove;

    public Minimax(String fen, int depth){

        Node source_node = new Node(fen);
        minimax(source_node, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
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
            getBestMove(source);
            return source.score;
        }

        source.generateChildren();

        if (max){

            double bestVal = Double.NEGATIVE_INFINITY;

            for (Node child: source.children) {

                double value = minimax(child, depth - 1, alpha, beta, false);
                bestVal = Math.max(bestVal, value);

                alpha = Math.max(alpha, bestVal);

                if (beta <= alpha)
                    break;

                return bestVal;
            }

        } else {
            double bestVal = Double.POSITIVE_INFINITY;

            for (Node child: source.children) {

                double value = minimax(child, depth - 1, alpha, beta, true);
                bestVal = Math.min(bestVal, value);

                beta = Math.min(beta, bestVal);

                if (beta <= alpha)
                    break;

                return bestVal;
            }
        }
        return 0;
    }

}
