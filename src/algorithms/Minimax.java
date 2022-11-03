package algorithms;

import chesslib.*;
import chesslib.move.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Character.*;

public class Minimax {

    public static int transpSize = 100000;

    public static Map<Character, Integer> values = new HashMap<>(Map.of(
            'Q', 9, 'R', 5, 'N', 3, 'B', 3, 'P', 1,
            'q', -9, 'r', -5, 'n', -3, 'b', -3, 'p', -1));

    public static int[][] vic_atk_val = {
            {60, 61, 62, 63, 64, 65, 0},       // victim K, attacker K, Q, R, B, N, P, None
            {50, 51, 52, 53, 54, 55, 0}, // victim Q, attacker K, Q, R, B, N, P, None
            {40, 41, 42, 43, 44, 45, 0}, // victim R, attacker K, Q, R, B, N, P, None
            {30, 31, 32, 33, 34, 35, 0}, // victim B, attacker K, Q, R, B, N, P, None
            {20, 21, 22, 23, 24, 25, 0}, // victim N, attacker K, Q, R, B, N, P, None
            {10, 11, 12, 13, 14, 15, 0}, // victim P, attacker K, Q, R, B, N, P, None
            {0, 0, 0, 0, 0, 0, 0},      // no victim
    };
    public static Map<Character, Double> toto_values = new HashMap<>(Map.of(
            'K', 200.0, 'Q', 9.0, 'R', 5.0, 'N', 3.0, 'B', 3.0, 'P', 1.0));
    public static Map<PieceType, Integer> pieceValues = new HashMap<>(Map.of(
            PieceType.KING, 15,
            PieceType.QUEEN, 9,
            PieceType.ROOK, 5,
            PieceType.KNIGHT, 3,
            PieceType.BISHOP, 3,
            PieceType.PAWN, 1,
            PieceType.NONE, 0));

    public static Map<PieceType, Integer> pieceIndex = new HashMap<>(Map.of(
            PieceType.KING, 0,
            PieceType.QUEEN, 1,
            PieceType.ROOK, 2,
            PieceType.KNIGHT, 3,
            PieceType.BISHOP, 4,
            PieceType.PAWN, 5,
            PieceType.NONE, 6));

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
            Node node = new Node(null, evaluate(board, max));
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

    public static double evaluate(Board board, boolean max) {

        // EDGE CASES
        double lowerBound = -10000.0, higherBound = 10000.0;
        boolean mate = board.isMated(), draw = board.isDraw();

        double score = 0.0;

        if (mate)
            return max ? lowerBound : higherBound;

        if (draw)
            return 0.0;

        // BOARD ANALYSIS
        int w;
        if (board.gamePhase == 0) // opening phase
            w = 0;
        else if  (board.gamePhase == 1) // middle phase
            w = 1;
        else if  (board.gamePhase == 2) // end phase
            w = 2;

        int index = board.getFen().indexOf(" ");
        String subfen = "";

        if (index != -1)
            subfen = board.getFen().substring(0, index);

        char[] fen_char = subfen.toCharArray();

        int[][] files = new int[8][8];

        // KNIGHT VALUE UPDATE AND PAWN COUNT
        if (toto_values.get('N') == 3.0) {

            double change = 0.0;
            double pieceRatio = 0.0;

            int level = 0, line = 0;

            for (char fc : fen_char) {

                if (max && isUpperCase(fc))
                    pieceRatio++;

                if (!max && isLowerCase(fc))
                    pieceRatio++;

                if (fc == 'p') {
                    files[level][line] = 2;
                } else if (fc == 'P') {
                    files[level][line] = 1;
                }

                line++;

                if (fc == '/') {
                    level++;
                    line = 0;
                }
            }

            // Update knights starting from mid game
            if ((pieceRatio / 16.0) < 0.5)
                change = 1.0;

            toto_values.replace('N', toto_values.get('N') - change);
        }

        int w_doubled = 0, w_blocked = 0, w_isolated = 0;
        int b_doubled = 0, b_blocked = 0, b_isolated = 0;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {

                int square = files[i][j];
                int frontpos;
                int leftpos = j - 1;
                int rightpos = j + 1;

                if (square == 1) {
                    frontpos = i - 1;

                    if (frontpos >= 0)
                        if (files[frontpos][j] == 2)
                            w_blocked++;
                        else if (files[frontpos][j] == 1)
                            w_doubled++;

                    if (leftpos >= 0 && rightpos <= 7)
                        if (files[i][leftpos] != 1 && files[i][rightpos] != 1)
                            w_isolated++;


                } else if (square == 2) {
                    frontpos = i + 1;

                    if (frontpos <= 7)
                        if (files[frontpos][j] == 1)
                            b_blocked++;
                        else if (files[frontpos][j] == 2)
                            b_doubled++;


                    if ((leftpos >= 0 && rightpos <= 7))
                        if (files[i][leftpos] != 2 && files[i][rightpos] != 2)
                            b_isolated++;
                }
            }
        }

        StringBuilder visited = new StringBuilder();

        for (char fc : fen_char) {
            if (isUpperCase(fc)) {

                boolean isVisited = visited.toString().indexOf(fc) == -1 ||
                        visited.toString().indexOf(toLowerCase(fc)) == -1;

                if (!isVisited) {

                    int found = subfen.indexOf(toLowerCase(fc)) != -1 ? 1 : 0;

                    score += (1 - found) * toto_values.get(fc);
                    visited.append(fc);
                }
            }
        }

        int whiteMoves = 0, blackMoves = 0;

        if (max) {
            whiteMoves = board.legalMoves().size();
            board.setSideToMove(Side.BLACK);
            blackMoves = board.legalMoves().size();
        } else {
            blackMoves = board.legalMoves().size();
            board.setSideToMove(Side.WHITE);
            whiteMoves = board.legalMoves().size();
        }

        score -= 0.5 * (w_doubled - b_doubled + w_blocked - b_blocked + w_isolated - b_isolated);
        score += 0.1 * (whiteMoves - blackMoves);

        // scale between lowerBound:higherBound
        // score = ((higherBound - lowerBound) * (score + 39) / (39 + 39)) + lowerBound;

        return score;
    }

    public static float moveCaptureValue(Move move, String fen) {

        Board simBoard = new Board();
        simBoard.loadFromFen(fen);

        simBoard.doMove(move);
        if (simBoard.isMated()) // Mate : highest
            return 80;
        if (transposition.containsKey(simBoard.getZobristKey() % transpSize)) { // TT-move ordering : second highest
            return 70;
        } else { // victim/attacker capture : third highest
            simBoard.undoMove();
            PieceType vic = simBoard.getPiece(move.getTo()).getPieceType();
            if (vic == null)
                vic = PieceType.NONE;
            return vic_atk_val[pieceIndex.get(vic)][pieceIndex.get(simBoard.getPiece(move.getFrom()).getPieceType())];
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
