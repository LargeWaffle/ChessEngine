package algorithms;

import chesslib.*;
import chesslib.move.*;

import java.util.*;

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
            {0,  0,  0,  0,  0,  0,  0},      // no victim
    };
    public static Map<Character, Double> toto_values = new HashMap<>(Map.of(
            'K', 200.0, 'Q', 9.0, 'R', 5.0, 'N', 3.0, 'B', 3.0, 'P', 1.0));
    public static Map<PieceType, Integer> pieceValues = new HashMap<>(Map.of(
            PieceType.KING, 20000,
            PieceType.QUEEN, 900,
            PieceType.ROOK, 500,
            PieceType.KNIGHT, 330,
            PieceType.BISHOP, 320,
            PieceType.PAWN, 120,
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

    public static List<Integer> pawnTable = Arrays.asList(
            0,  0,  0,  0,  0,  0,  0,  0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            5,  5, 10, 25, 25, 10,  5,  5,
            0,  0,  0, 20, 20,  0,  0,  0,
            5, -5,-10,  0,  0,-10, -5,  5,
            5, 10, 10,-20,-20, 10, 10,  5,
            0,  0,  0,  0,  0,  0,  0,  0);

    public static List<Integer> knightTable = Arrays.asList(
            -50,-40,-30,-30,-30,-30,-40,-50,
            -40,-20,  0,  0,  0,  0,-20,-40,
            -30,  0, 10, 15, 15, 10,  0,-30,
            -30,  5, 15, 20, 20, 15,  5,-30,
            -30,  0, 15, 20, 20, 15,  0,-30,
            -30,  5, 10, 15, 15, 10,  5,-30,
            -40,-20,  0,  5,  5,  0,-20,-40,
            -50,-40,-30,-30,-30,-30,-40,-50);

    public static List<Integer> bishopTable = Arrays.asList(
            -20,-10,-10,-10,-10,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5, 10, 10,  5,  0,-10,
            -10,  5,  5, 10, 10,  5,  5,-10,
            -10,  0, 10, 10, 10, 10,  0,-10,
            -10, 10, 10, 10, 10, 10, 10,-10,
            -10,  5,  0,  0,  0,  0,  5,-10,
            -20,-10,-10,-10,-10,-10,-10,-20);

    public static List<Integer> rookTable = Arrays.asList(
            0,  0,  0,  0,  0,  0,  0,  0,
            5, 10, 10, 10, 10, 10, 10,  5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            0,  0,  0,  5,  5,  0,  0,  0);

    public static List<Integer> queenTable = Arrays.asList(
            -20,-10,-10, -5, -5,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5,  5,  5,  5,  0,-10,
            -5,  0,  5,  5,  5,  5,  0, -5,
            0,  0,  5,  5,  5,  5,  0, -5,
            -10,  5,  5,  5,  5,  5,  0,-10,
            -10,  0,  5,  0,  0,  0,  0,-10,
            -20,-10,-10, -5, -5,-10,-10,-20);

    public static List<Integer> kingMiddleTable = Arrays.asList(
            -20,-10,-10, -5, -5,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5,  5,  5,  5,  0,-10,
            -5,  0,  5,  5,  5,  5,  0, -5,
            0,  0,  5,  5,  5,  5,  0, -5,
            -10,  5,  5,  5,  5,  5,  0,-10,
            -10,  0,  5,  0,  0,  0,  0,-10,
            -20,-10,-10, -5, -5,-10,-10,-20);

    public static List<Integer> kingEndingTable = Arrays.asList(
            -50,-40,-30,-20,-20,-30,-40,-50,
            -30,-20,-10,  0,  0,-10,-20,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-30,  0,  0,  0,  0,-30,-30,
            -50,-30,-30,-30,-30,-30,-30,-50);

    public static Map<PieceType, List<Integer>> midgamePST = new HashMap<>(Map.of(
            PieceType.PAWN, pawnTable,
            PieceType.KNIGHT, knightTable,
            PieceType.BISHOP, bishopTable,
            PieceType.ROOK, rookTable,
            PieceType.QUEEN, queenTable,
            PieceType.KING, kingMiddleTable));

    public static Map<PieceType, List<Integer>> endgamePST = new HashMap<>(Map.of(
            PieceType.PAWN, pawnTable,
            PieceType.KNIGHT, knightTable,
            PieceType.BISHOP, bishopTable,
            PieceType.ROOK, rookTable,
            PieceType.QUEEN, queenTable,
            PieceType.KING, kingEndingTable));

    public Minimax() {

    }

    public static Node minimax(Board board, int depth, double alpha, double beta, boolean max) {

        cpt++;

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

        boolean terminal = board.isDraw() || board.isMated();

        if (depth == 0 || terminal)
            return new Node(null, evaluate(board, max));

        Move bestMove = board.legalMoves().get(0);
        if (max) {

            int hashf = 1;
            double maxEval = -Double.MAX_VALUE;

            // generate children
            List<Move> moveList = board.legalMoves();

            // order the list by capturing moves first to maximize alpha beta cutoff
            moveList.sort(Comparator.comparingInt((Move m) -> (int) moveValue(m, board.getFen())));
            Collections.reverse(moveList);

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

        double score = 0.0, materialScore = 0.0, controlScore = 0.0, mobilityScore = 0.0;

        if (board.isMated())
            return max ? lowerBound : higherBound;

        if (board.isDraw())
            return 0.0;

        int matW = 0, contW = 0, mobW = 0;
        // BOARD ANALYSIS
        if (board.gamePhase == 0) { // opening phase
            matW = 1;
            contW = 40;
            mobW = 2;
        }
        else if (board.gamePhase == 1) { // middle phase
            matW = 1;
            contW = 40;
            mobW = 10;
        }
        else if (board.gamePhase == 2) { // end phase
            matW = 1;
            contW = 10;
            mobW = 2;
        }

        // Material evaluation
        long bboard = board.getBitboard();
        for (int i = 0; i < 64; i++) {
            if (((1L << i) & bboard) != 0L) {
                Piece p = board.getPiece(Square.squareAt(i));
                if (p.getPieceSide() == Side.WHITE)
                    materialScore += pieceValues.get(p.getPieceType()) + (board.gamePhase == 1 ? midgamePST.get(p.getPieceType()).get(i) : endgamePST.get(p.getPieceType()).get(i));
                else
                    materialScore -= pieceValues.get(p.getPieceType()) - (board.gamePhase == 1 ? midgamePST.get(p.getPieceType()).get(63-i) : endgamePST.get(p.getPieceType()).get(63-i));
            }
        }

        // Control evaluation
        long white_bit, black_bit;
        int count = 0;
        for (int i = 0; i < 64; i++) {
            white_bit = board.squareAttackedBy(Square.squareAt(i), Side.WHITE);
            black_bit = board.squareAttackedBy(Square.squareAt(i), Side.BLACK);
            for (int j = 0; j < 64; j++) {
                if (((1L << j) & white_bit) != 0L)
                    count += 1;
                if (((1L << j) & black_bit) != 0L)
                    count -= 1;
            }
            if (count > 0)
                controlScore += 1;
            else if (count < 0)
                controlScore -= 1;
            count = 0;
        }

        // Mobility evaluation
        // w à 10 trop pour start : il yonk sa dame vers l'avant comme un dégénéré
        mobilityScore += MoveGenerator.getLegalMovesSize(board, Side.WHITE) - MoveGenerator.getLegalMovesSize(board, Side.BLACK);

        // Tapered evaluation
        score = matW * materialScore + contW * controlScore + mobW * mobilityScore;

        // scale between lowerBound and higherBound
        score = (score - lowerBound) / (higherBound - lowerBound);
        return score;
    }

    public static float moveValue(Move move, String fen) {

        Board simBoard = new Board();
        simBoard.loadFromFen(fen);
        PieceType vic = simBoard.getPiece(move.getTo()).getPieceType();
        simBoard.doMove(move);
        if (simBoard.isMated()) // Mate : highest
            return 80;
        if (vic == null)
            if (transposition.containsKey(simBoard.getZobristKey() % transpSize)) // TT-move ordering : third highest
                return 1;
            else
                return 0;
        else { // victim/attacker capture : second highest
            simBoard.undoMove();
            return vic_atk_val[pieceIndex.get(vic)][pieceIndex.get(simBoard.getPiece(move.getFrom()).getPieceType())];
        }
    }
}