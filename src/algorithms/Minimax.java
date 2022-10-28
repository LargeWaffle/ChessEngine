package algorithms;

import chesslib.Board;
import chesslib.Side;
import chesslib.move.*;

import java.util.List;
import java.util.Map;

import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;

public class Minimax {

    public Minimax() {

    }

    public static Node minimax(Board board, int depth, double alpha, double beta, boolean max){

        boolean terminal = board.isDraw() || board.isMated();

        if (depth == 0 || terminal)
        {
            return new Node(null, evaluate(board));
        }

        Move bestMove = board.legalMoves().get(0);
        if (max){

            double maxEval = -Double.MAX_VALUE;

            // generate children
            List<Move> moveList = board.legalMoves();

            for (Move move : moveList) {

                board.doMove(move);
                double value = minimax(board, depth - 1, alpha, beta, false).score;
                board.undoMove();

                if (value > maxEval) {
                    maxEval = value;
                    bestMove = move;
                }

                alpha = Math.max(alpha, maxEval);

                if (beta <= alpha)
                    break;
            }

            return new Node(bestMove, maxEval);

        } else {

            double minEval = Double.MAX_VALUE;

            List<Move> moveList = board.legalMoves();

            for (Move move : moveList) {

                board.doMove(move);
                double value = minimax(board, depth - 1, alpha, beta, true).score;
                board.undoMove();

                if (value < minEval) {
                    minEval = value;
                    bestMove = move;
                }

                beta = Math.min(beta, minEval);

                if (beta <= alpha)
                    break;

            }
            return new Node(bestMove, minEval);
        }
    }

    public static double evaluate(Board board) {
        int index = board.getFen().indexOf(" ");
        String subfen = "";

        if (index != -1)
            subfen = board.getFen().substring(0, index);

        Map<Character, Integer> values = new java.util.HashMap<>(Map.of(
                'Q', 9, 'R', 5, 'N', 3, 'B', 3, 'P', 1,
                'q', -9, 'r', -5, 'n', -3, 'b', -3, 'p', -1));

        char[] fen_char = subfen.toCharArray();

        double score = 0;
        double pieceRatio = 0;

        for (char fc : fen_char) {
            if (isLowerCase(fc) || isUpperCase(fc)) {
                pieceRatio++;
            }
        }

        // Update knights starting from mid game
        pieceRatio = pieceRatio / 16.0 < 5 ? 1 : 0;
        values.put('n', values.get('n') + (int)pieceRatio);
        values.put('N', values.get('N') - (int)pieceRatio);

        for (char fc : fen_char) {
            if (fc != 'k' && fc != 'K') {
                /*score += isLowerCase(fc) ? values.get(fc) : 0;
                score += isUpperCase(fc) ? values.get(fc) : 0;*/
                score += values.get(fc);
            }
        }

        Side side = board.getSideToMove();

        if(board.isKingAttacked())
            if (side == Side.WHITE)
                score = - 100;
            else
                score = 100;

        if(board.isMated())
            if (side == Side.WHITE)
                score = - 10000;
            else
                score = 10000;

        return score;
    }

}
