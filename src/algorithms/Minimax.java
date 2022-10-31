package algorithms;

import chesslib.Board;
import chesslib.Side;
import chesslib.move.*;

import java.util.List;
import java.util.Map;

import static java.lang.Character.*;

public class Minimax {

    public static Map<Character, Double> values = new java.util.HashMap<>(Map.of(
            'Q', 9.0, 'R', 5.0, 'N', 3.0, 'B', 3.0, 'P', 1.0));

    public Minimax() {
    }

    public static Node minimax(Board board, int depth, double alpha, double beta, boolean max) {

        boolean terminal = board.isDraw() || board.isMated();

        if (depth == 0 || terminal) {
            return new Node(null, evaluate(board));
        }

        Move bestMove = board.legalMoves().get(0);
        if (max) {

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

        char[] fen_char = subfen.toCharArray();

        Side side = board.getSideToMove();
        double score = 0.0;

        if (values.get('N') == 3.0) {

            double change = 0.0;
            double pieceRatio = 0.0;

            for (char fc : fen_char) {
                if (side == Side.WHITE && isUpperCase(fc))
                    pieceRatio++;

                if (side == Side.BLACK && isLowerCase(fc))
                    pieceRatio++;
            }

            // Update knights starting from mid game
            if ((pieceRatio / 16.0) < 0.5)
                change = 1.0;

            values.replace('N', values.get('N') - change);
        }

        double lowerBound = -10.0, higherBound = 10.0;
        double whiteCount = 0.0, blackCount = 0.0;

        boolean mate = board.isMated(), draw = board.isDraw();

        if (mate)
            score = side == Side.WHITE ? lowerBound : higherBound;

        if (draw)
            score = 0.0;

        if (!mate && !draw) {
            for (char fc : fen_char) {
                if (Character.isLetter(fc) && (fc != 'k' && fc != 'K'))
                    if (isLowerCase(fc))
                        blackCount += values.get(toUpperCase(fc));
                    else
                        whiteCount += values.get(fc);
            }

            score = whiteCount - blackCount;

            // scale between lowerBound:higherBound
            score = ((higherBound - lowerBound) * (score + 39) / (39 + 39)) + lowerBound;
        }

        return score;
    }

}
