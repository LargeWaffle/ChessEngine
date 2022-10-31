package algorithms;

import chesslib.*;
import chesslib.move.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Character.*;

public class Minimax {

    public static int transpSize = 10000000;

    public static Map<Character, Integer> values = new HashMap<>(Map.of(
            'Q', 9, 'R', 5, 'N', 3, 'B', 3, 'P', 1,
            'q', -9, 'r', -5, 'n', -3, 'b', -3, 'p', -1));

    public static Map<Character, Double> toto_values = new HashMap<>(Map.of(
            'Q', 9.0, 'R', 5.0, 'N', 3.0, 'B', 3.0, 'P', 1.0));
    public static Map<PieceType, Integer> pieceValues = new HashMap<>(Map.of(
            PieceType.KING, 15,
            PieceType.QUEEN, 9,
            PieceType.ROOK, 5,
            PieceType.KNIGHT, 3,
            PieceType.BISHOP, 3,
            PieceType.PAWN, 1,
            PieceType.NONE, 0));

    public static Map<Long, HashEntry> transposition = new HashMap<>(transpSize);
    public static double cpt = 0;

    public Minimax() {

    }

    public static Node minimax(Board board, int depth, double alpha, double beta, boolean max) {

        if (transposition.containsKey(board.getZobristKey() % transpSize)) {
            HashEntry rec = transposition.get(board.getZobristKey() % transpSize);
            if (rec.zobrist == board.getZobristKey()) {
                if (rec.depth >= depth) {
                    if (rec.flag == 0)
                        return rec.node;
                    if (rec.flag == 1 && rec.node.score <= alpha)
                        return new Node(rec.node.move, alpha);
                    if (rec.flag == 2 && rec.node.score >= beta)
                        return new Node(rec.node.move, beta);
                }
            }
        }

        cpt++;
        boolean terminal = board.isDraw() || board.isMated();

        if (depth == 0 || terminal) {
            Node node = new Node(null, evaluate(board));
            transposition.put(board.getZobristKey() % transpSize, new HashEntry(board.getZobristKey(), depth, 0, node));
            return node;
        }

        Move bestMove = board.legalMoves().get(0);
        if (max) {

            int hashf = 1;
            double maxEval = -Double.MAX_VALUE;

            // generate children
            List<Move> moveList = board.legalMoves();

            // order the list by capturing moves first to maximize alpha beta cutoff
            quickSort(moveList, board.getFen(), 0, moveList.size() - 1);

            for (Move move : moveList) {

                board.doMove(move);
                double value = minimax(board, depth - 1, alpha, beta, false).score;
                board.undoMove();

                if (value > maxEval) {
                    hashf = 0;
                    maxEval = value;
                    bestMove = move;
                }

                alpha = Math.max(alpha, maxEval);

                if (beta <= alpha)
                    break;
            }

            Node node = new Node(bestMove, maxEval);
            transposition.put(board.getZobristKey() % transpSize, new HashEntry(board.getZobristKey(), depth, hashf, node));
            return node;

        } else {

            int hashf = 2;
            double minEval = Double.MAX_VALUE;

            List<Move> moveList = board.legalMoves();

            for (Move move : moveList) {

                board.doMove(move);
                double value = minimax(board, depth - 1, alpha, beta, true).score;
                board.undoMove();

                if (value < minEval) {
                    hashf = 0;
                    minEval = value;
                    bestMove = move;
                }

                beta = Math.min(beta, minEval);

                if (beta <= alpha)
                    break;

            }

            Node node = new Node(bestMove, minEval);
            transposition.put(board.getZobristKey() % transpSize, new HashEntry(board.getZobristKey(), depth, hashf, node));
            return node;
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

        if (toto_values.get('N') == 3.0) {

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

            toto_values.replace('N', toto_values.get('N') - change);
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
                        blackCount += toto_values.get(toUpperCase(fc));
                    else
                        whiteCount += toto_values.get(fc);
            }

            score = whiteCount - blackCount;

            // scale between lowerBound:higherBound
            score = ((higherBound - lowerBound) * (score + 39) / (39 + 39)) + lowerBound;
        }

        return score;
    }

    public static float moveCaptureValue(Move move, String fen) {

        Board simBoard = new Board();
        simBoard.loadFromFen(fen);

        simBoard.doMove(move);
        if (simBoard.isMated())
            return 100;
        else if (simBoard.isKingAttacked())
            return 10;
        else {
            simBoard.undoMove();

            Piece cap = simBoard.getPiece(move.getTo());
            if (cap == Piece.NONE)
                return 0;
            else {
                PieceType captured = cap.getPieceType();
                PieceType movingPiece = simBoard.getPiece(move.getFrom()).getPieceType();
                return ((float) pieceValues.get(captured) / pieceValues.get(movingPiece));
            }
        }
    }

    public static void quickSort(List<Move> moveList, String fen, int begin, int end) {
        if (begin < end) {
            int partitionIndex = partition(moveList, fen, begin, end);

            quickSort(moveList, fen, begin, partitionIndex - 1);
            quickSort(moveList, fen, partitionIndex + 1, end);
        }
    }

    private static int partition(List<Move> moveList, String fen, int begin, int end) {
        Move pivot = moveList.get(end);
        int i = (begin - 1);

        for (int j = begin; j < end; j++) {
            if (moveCaptureValue(moveList.get(j), fen) >= moveCaptureValue(pivot, fen)) {
                i++;

                Move swapTemp = moveList.get(i);
                moveList.set(i, moveList.get(j));
                moveList.set(j, swapTemp);
            }
        }

        Move swapTemp = moveList.get(i + 1);
        moveList.set(i + 1, moveList.get(end));
        moveList.set(end, swapTemp);

        return i + 1;
    }

}
