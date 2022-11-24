package algorithms;

import chesslib.*;
import chesslib.move.*;

import java.util.*;

import static chesslib.Bitboard.bitScanForward;
import static chesslib.Bitboard.extractLsb;
import static java.lang.Long.bitCount;

public class Minimax {

    // minimax depth for the very late game
    public static int MINIMAX_MAX_DEPTH = 9;

    // minimax depth throughout the game
    public static int MINIMAX_DEPTH = 5;

    // quiescence search depth
    public static int QUIESCENCE_DEPTH = 3;

    // maximum size of the transposition table
    public static int transpSize = 100000;

    // mate evaluation values
    public static double lowerBound = -10000.0, higherBound = 10000.0;

    // MVV/LVA table
    public static int[][] vic_atk_val = {
            {60, 61, 62, 63, 64, 65, 0}, // victim K, attacker K, Q, R, B, N, P, None
            {50, 51, 52, 53, 54, 55, 0}, // victim Q, attacker K, Q, R, B, N, P, None
            {40, 41, 42, 43, 44, 45, 0}, // victim R, attacker K, Q, R, B, N, P, None
            {30, 31, 32, 33, 34, 35, 0}, // victim B, attacker K, Q, R, B, N, P, None
            {20, 21, 22, 23, 24, 25, 0}, // victim N, attacker K, Q, R, B, N, P, None
            {10, 11, 12, 13, 14, 15, 0}, // victim P, attacker K, Q, R, B, N, P, None
            {0, 0, 0, 0, 0, 0, 0},      // no victim
    };

    // values of pieces in our engine
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

    // transposition table
    public static Map<Long, HashEntry> transposition = new HashMap<>(transpSize);

    // history moves table
    public static Map<Integer, Integer> historyMoves = new HashMap<>();

    // killer moves table
    public static int[][] killerMoves = new int[MINIMAX_MAX_DEPTH][2];

    // margins for futility pruning
    public static double frontierFutility = 100;
    public static double extendedFutility = 500;
    public static double rasorFutility = 900;

    // explored node counter
    public static double cpt = 0;

    // Piece Square Tables
    public static List<Integer> pawnMiddleTable = Arrays.asList(
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            5, 5, 10, 25, 25, 10, 5, 5,
            0, 0, 0, 20, 20, 0, 0, 0,
            5, -5, -10, 0, 0, -10, -5, 5,
            5, 10, 10, -20, -20, 10, 10, 5,
            0, 0, 0, 0, 0, 0, 0, 0);

    public static List<Integer> pawnEndingTable = Arrays.asList(
            0, 0, 0, 0, 0, 0, 0, 0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            5, 5, 10, 25, 25, 10, 5, 5,
            0, 0, 0, 20, 20, 0, 0, 0,
            5, -5, -10, 0, 0, -10, -5, 5,
            5, 10, 10, -20, -20, 10, 10, 5,
            0, 0, 0, 0, 0, 0, 0, 0);

    public static List<Integer> knightMiddleTable = Arrays.asList(
            -50, -40, -30, -30, -30, -30, -40, -50,
            -40, -20, 0, 0, 0, 0, -20, -40,
            -30, 0, 10, 15, 15, 10, 0, -30,
            -30, 5, 15, 20, 20, 15, 5, -30,
            -30, 0, 15, 20, 20, 15, 0, -30,
            -30, 5, 10, 15, 15, 10, 5, -30,
            -40, -20, 0, 5, 5, 0, -20, -40,
            -50, -40, -30, -30, -30, -30, -40, -50);

    public static List<Integer> knightEndingTable = Arrays.asList(
            -20, -20, -20, -20, -20, -20, -20, -20,
            -20, -20, -20, -20, -20, -20, -20, -20,
            -20, -20, -20, -20, -20, -20, -20, -20,
            -20, -20, -20, -20, -20, -20, -20, -20,
            -20, -20, -20, -20, -20, -20, -20, -20,
            -20, -20, -20, -20, -20, -20, -20, -20,
            -20, -20, -20, -20, -20, -20, -20, -20,
            -20, -20, -20, -20, -20, -20, -20, -20);

    public static List<Integer> bishopMiddleTable = Arrays.asList(
            -20, -10, -10, -10, -10, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 5, 5, 10, 10, 5, 5, -10,
            -10, 0, 10, 10, 10, 10, 0, -10,
            -10, 10, 10, 10, 10, 10, 10, -10,
            -10, 5, 0, 0, 0, 0, 5, -10,
            -20, -10, -10, -10, -10, -10, -10, -20);

    public static List<Integer> bishopEndingTable = Arrays.asList(
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0);

    public static List<Integer> rookMiddleTable = Arrays.asList(
            0, 0, 0, 0, 0, 0, 0, 0,
            5, 10, 10, 10, 10, 10, 10, 5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            0, 0, 0, 5, 5, 0, 0, 0);

    public static List<Integer> rookEndingTable = Arrays.asList(
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0);

    public static List<Integer> queenMiddleTable = Arrays.asList(
            -20, -10, -10, -5, -5, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -5, 0, 5, 5, 5, 5, 0, -5,
            0, 0, 5, 5, 5, 5, 0, -5,
            -10, 5, 5, 5, 5, 5, 0, -10,
            -10, 0, 5, 0, 0, 0, 0, -10,
            -20, -10, -10, -5, -5, -10, -10, -20);

    public static List<Integer> queenEndingTable = Arrays.asList(
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0);

    public static List<Integer> kingMiddleTable = Arrays.asList(
            -20, -10, -10, -5, -5, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -5, 0, 5, 5, 5, 5, 0, -5,
            0, 0, 5, 5, 5, 5, 0, -5,
            -10, 5, 5, 5, 5, 5, 0, -10,
            -10, 0, 5, 0, 0, 0, 0, -10,
            -20, -10, -10, -5, -5, -10, -10, -20);

    public static List<Integer> kingEndingTable = Arrays.asList(
            -50, -40, -30, -20, -20, -30, -40, -50,
            -30, -20, -10, 0, 0, -10, -20, -30,
            -30, -10, 20, 30, 30, 20, -10, -30,
            -30, -10, 30, 40, 40, 30, -10, -30,
            -30, -10, 30, 40, 40, 30, -10, -30,
            -30, -10, 20, 30, 30, 20, -10, -30,
            -30, -30, 0, 0, 0, 0, -30, -30,
            -50, -30, -30, -30, -30, -30, -30, -50);

    public static List<Integer> kingSoloTable = Arrays.asList(
            -100, -50, -50, -50, -50, -50, -50, -100,
            -50, -25, 0, 0, 0, 0, -25, -50,
            -50, 0, 5, 5, 5, 5, 0, -50,
            -50, 0, 5, 5, 5, 5, 0, -50,
            -50, 0, 5, 5, 5, 5, 0, -50,
            -50, 5, 5, 5, 5, 5, 0, -50,
            -50, -25, 5, 0, 0, 0, -25, -50,
            -100, -50, -50, -50, -50, -50, -50, -100);

    // link the PST to the according pieces in midgame
    public static Map<PieceType, List<Integer>> midgamePST = new HashMap<>(Map.of(
            PieceType.PAWN, pawnMiddleTable,
            PieceType.KNIGHT, knightMiddleTable,
            PieceType.BISHOP, bishopMiddleTable,
            PieceType.ROOK, rookMiddleTable,
            PieceType.QUEEN, queenMiddleTable,
            PieceType.KING, kingMiddleTable));

    // link the PST to the according pieces in endgame
    public static Map<PieceType, List<Integer>> endgamePST = new HashMap<>(Map.of(
            PieceType.PAWN, pawnEndingTable,
            PieceType.KNIGHT, knightEndingTable,
            PieceType.BISHOP, bishopEndingTable,
            PieceType.ROOK, rookEndingTable,
            PieceType.QUEEN, queenEndingTable,
            PieceType.KING, kingEndingTable));

    // link the PST to the according pieces when there is only a king left
    public static Map<PieceType, List<Integer>> soloKingPST = new HashMap<>(Map.of(
            PieceType.PAWN, pawnEndingTable,
            PieceType.KNIGHT, knightEndingTable,
            PieceType.BISHOP, bishopEndingTable,
            PieceType.ROOK, rookEndingTable,
            PieceType.QUEEN, queenEndingTable,
            PieceType.KING, kingSoloTable));

    public Minimax() {}

    // minimax alpha beta search
    public static Node minimax(Board board, int depth, double alpha, double beta, boolean max, boolean isCap, boolean allowNull) {

        cpt++;
        // if we have explored 400 000 nodes prune more aggressively
        if (cpt == 400000) {
            QUIESCENCE_DEPTH = 2;
            frontierFutility = 50;
            extendedFutility = 300;
            rasorFutility = 600;
        }

        // if we have explored 800 000 nodes stop the search
        if (cpt > 800000) {
            depth = 0;
            QUIESCENCE_DEPTH = 0;
        }

        int gamePhase = board.gamePhase;

        // if there is a solitary king set next game phase
        if (gamePhase == 2 && (board.isKingSolo(Side.WHITE) || board.isKingSolo(Side.BLACK)))
            gamePhase = 3;

        if (board.isMated())
            return new Node(null, max ? lowerBound : higherBound);
        else if (board.isDraw())
            return new Node(null, 0);
        else if (board.isRepetition(2))
            return new Node(null, 0);

        long zKey = board.getIncrementalHashKey(); // or zobrist but this seems faster
        int hashf = 0;

        // retrieve position info from transposition table if existing
        if (gamePhase != 3 && transposition.containsKey(zKey % transpSize)) {
            HashEntry rec = transposition.get(zKey % transpSize);
            if (rec.zobrist == zKey) {
                if (rec.depth >= depth) {
                    if (rec.flag == 0)
                        return rec.node;
                    if (rec.flag == 1 && rec.node.score <= alpha) // real score >= node score
                        return new Node(rec.node.move, alpha);
                    if (rec.flag == 2 && rec.node.score >= beta)
                        return new Node(rec.node.move, beta);
                }
            }
        }

        //  evaluate leaf nodes
        if (depth <= 0) {
            Node node;
            if (isCap && gamePhase != 3) // if last move was capturing launch a quiescence search to stabilize
                node = new Node(null, quiescenceSearch(board, alpha, beta, QUIESCENCE_DEPTH, max));
            else
                node = new Node(null, evaluate(board, gamePhase, alpha, beta, false));
            transposition.put(zKey % transpSize, new HashEntry(zKey, depth, hashf, node));

            return node;
        }

        // null move pruning
        if (gamePhase < 2 && allowNull && depth >= 3 && !board.isKingAttacked()) {

            double eval = evaluate(board, gamePhase, alpha, beta, true);
            if (eval >= beta) {
                board.doNullMove();
                eval = minimax(board, depth - 2 - 1, -beta, -beta + 1, !max, false, false).score;
                board.undoMove();

                if (eval >= beta)
                    return new Node(null, eval);
            }
        }

        // generate children
        List<Move> moveList = board.legalMoves();

        // order the list to maximize alpha beta cutoff
        int finalDepth = depth;
        moveList.sort(Comparator.comparingInt((Move m) ->
                moveValue(m, m.getPromotion(), m.isAdvancing(max ? Side.WHITE : Side.BLACK),
                        board.getPiece(m.getTo()).getPieceType(), board.getPiece(m.getFrom()).getPieceType(),
                        false, finalDepth)).reversed());

        Move bestMove = null;

        if (max) {
            double maxEval = -Double.MAX_VALUE;

            for (Move move : moveList) {

                Piece atkPiece = board.getPiece(move.getTo());
                int hc = move.hashCode();

                // defines if the move is capturing
                boolean capMove = !Piece.NONE.equals(atkPiece);

                board.doMove(move);

                // get a estimation of the value to perform futility pruning at depth <= 3
                double fastValue = -evaluate(board, gamePhase, alpha, beta, true) + alpha;

                boolean boardCanPrune = fastValue > (lowerBound + alpha) && Piece.NONE.equals(move.getPromotion())
                        && !board.isKingAttacked();

                // futility pruning
                if (depth <= 3 && boardCanPrune && gamePhase < 2) {
                    if (depth == 3 && (fastValue >= rasorFutility)) {
                        board.undoMove();
                        continue;
                    }

                    if (depth == 2 && (fastValue >= extendedFutility)) {
                        board.undoMove();
                        continue;
                    }

                    if (depth == 1 && (fastValue >= frontierFutility)) {
                        board.undoMove();
                        continue;
                    }
                }

                double value = minimax(board, depth - 1, alpha, beta, false, capMove, true).score;

                // gives more importance to mates that are seen early in the search --> less moves required to mate
                if (value == higherBound)
                    value += depth;

                if (value > maxEval) {
                    maxEval = value;
                    bestMove = move;

                    // history move heuristic keeps a counter of the moves that were best moves
                    if (historyMoves.containsKey(hc)) {
                        historyMoves.put(hc, historyMoves.get(hc) + 1);
                    } else
                        historyMoves.put(hc, 1);
                }

                alpha = Math.max(alpha, maxEval);

                board.undoMove();

                if (beta <= alpha) {
                    // killer move heuristic - store a non capturing killer move that is different as the one stored
                    if (!capMove && killerMoves[depth - 1][0] != hc) {
                        killerMoves[depth - 1][1] = killerMoves[depth - 1][0];
                        killerMoves[depth - 1][0] = hc;
                    }

                    // set the flag to alpha cutoff for transposition table
                    hashf = 1;
                    break;
                }

            }

            Node node = new Node(bestMove, maxEval);

            // add this position to the transposition table with accurate flag
            transposition.put(zKey % transpSize, new HashEntry(zKey, depth, hashf, node));
            return node;

        } else {
            double minEval = Double.MAX_VALUE;

            for (Move move : moveList) {

                Piece atkPiece = board.getPiece(move.getTo());
                int hc = move.hashCode();

                // defines if the move is capturing
                boolean capMove = !Piece.NONE.equals(atkPiece);

                board.doMove(move);

                // get a estimation of the value to perform futility pruning at depth <= 3
                double fastValue = evaluate(board, gamePhase, alpha, beta, true) - beta;

                boolean boardCanPrune = fastValue > (lowerBound - beta) && Piece.NONE.equals(move.getPromotion())
                        && !board.isKingAttacked();

                // futility pruning
                if (depth <= 3 && boardCanPrune && gamePhase < 2) {
                    if (depth == 3 && (fastValue >= rasorFutility)) {
                        board.undoMove();
                        continue;
                    }

                    if (depth == 2 && (fastValue >= extendedFutility)) {
                        board.undoMove();
                        continue;
                    }

                    if (depth == 1 && (fastValue >= frontierFutility)) {
                        board.undoMove();
                        continue;
                    }
                }

                double value = minimax(board, depth - 1, alpha, beta, true, capMove, true).score;

                // gives more importance to mates that are seen early in the search --> less moves required to mate
                if (value == lowerBound)
                    value -= depth;

                if (value < minEval) {
                    minEval = value;
                    bestMove = move;

                    // history move heuristic keeps a counter of the moves that were best moves
                    if (historyMoves.containsKey(hc)) {
                        historyMoves.put(hc, historyMoves.get(hc) + 1);
                    } else
                        historyMoves.put(hc, 1);
                }

                beta = Math.min(beta, minEval);

                board.undoMove();

                if (beta <= alpha) {
                    // killer move heuristic - store a non capturing killer move that is different as the one stored
                    if (!capMove && killerMoves[depth - 1][0] != hc) {
                        killerMoves[depth - 1][1] = killerMoves[depth - 1][0];
                        killerMoves[depth - 1][0] = hc;
                    }

                    // set the flag to beta cutoff for transposition table
                    hashf = 2;
                    break;
                }

            }

            Node node = new Node(bestMove, minEval);

            // add this position to the transposition table with accurate flag
            transposition.put(zKey % transpSize, new HashEntry(zKey, depth, hashf, node));
            return node;
        }
    }

    // quiescence search to stabilize capture nodes and limit horizon effect
    public static double quiescenceSearch(Board board, double alpha, double beta, int depth, boolean max) {

        // delta cutoff value
        double DELTA = 1500;

        if (board.isMated())
            return max ? lowerBound : higherBound;
        else if (board.isDraw())
            return 0;

        // get the evaluation of the board
        double stand_pat = evaluate(board, board.gamePhase, alpha, beta, false);

        if (depth <= 0)
            return stand_pat;

        if (max) {
            if (stand_pat >= beta)
                return stand_pat;

            alpha = Math.max(stand_pat, alpha);

        } else {
            if (stand_pat <= alpha)
                return stand_pat;

            beta = Math.min(stand_pat, beta);
        }

        // generate all possible captures
        List<Move> capList = board.pseudoLegalCaptures();

        if (capList.isEmpty())
            return stand_pat;

        // order the moves to maximise cutoff
        capList.sort(Comparator.comparingInt((Move m) ->
                moveValue(m, m.getPromotion(), m.isAdvancing(max ? Side.WHITE : Side.BLACK),
                        board.getPiece(m.getTo()).getPieceType(), board.getPiece(m.getFrom()).getPieceType(),
                        true, depth)).reversed());

        if (max) {

            double maxEval = -Double.MAX_VALUE;

            for (Move cap : capList) {

                // check if the capture is legal
                if (!board.isMoveLegal(cap, true))
                    continue;

                // delta pruning
                if (stand_pat + DELTA < alpha)
                    continue;

                board.doMove(cap);
                double value = quiescenceSearch(board, alpha, beta, depth - 1, false);
                board.undoMove();

                maxEval = Math.max(maxEval, value);

                if (maxEval >= beta)
                    return maxEval;

                alpha = Math.max(maxEval, alpha);
            }

            if (maxEval == -Double.MAX_VALUE)
                return stand_pat;
            else
                return maxEval;

        } else {
            double minEval = Double.MAX_VALUE;

            for (Move cap : capList) {

                if (!board.isMoveLegal(cap, true))
                    continue;

                if (stand_pat + DELTA < alpha)
                    continue;

                board.doMove(cap);
                double value = quiescenceSearch(board, alpha, beta, depth - 1, true);

                board.undoMove();

                minEval = Math.min(minEval, value);

                if (minEval <= alpha)
                    return minEval;

                beta = Math.min(minEval, beta);
            }

            if (minEval == Double.MAX_VALUE)
                return stand_pat;
            else
                return minEval;
        }
    }

    public static double evaluate(Board board, int phase, double alpha, double beta, boolean lazy) {

        double score = 0, materialScore = 0, controlScore = 0, pawnScore = 0;

        int matW = 0, contW = 0, pawnW = 0;

        // lazy margin value
        int lazyMargin = 150;

        long bboard = board.getBitboard();

        // if there is a side with only a king left, evaluate material with the "soloKingPST" piece square table
        if (phase == 3) {
            for (int i = 0; i < 64; i++) {
                if (((1L << i) & bboard) != 0L) {
                    Piece p = board.getPiece(Square.squareAt(i));
                    PieceType type = p.getPieceType();
                    if (p.getPieceSide() == Side.WHITE) {
                        if (board.isKingSolo(Side.WHITE))
                            materialScore += pieceValues.get(type) + soloKingPST.get(type).get(i);
                        else
                            materialScore += pieceValues.get(type) + endgamePST.get(type).get(i);
                    } else {
                        if (board.isKingSolo(Side.BLACK))
                            materialScore -= pieceValues.get(type) - soloKingPST.get(type).get(i);
                        else
                            materialScore -= pieceValues.get(type) - endgamePST.get(type).get(i);
                    }
                }
            }
            return materialScore;
        }

        // Material evaluation
        for (int i = 0; i < 64; i++) {
            if (((1L << i) & bboard) != 0L) {
                Piece p = board.getPiece(Square.squareAt(i));
                PieceType type = p.getPieceType();
                if (p.getPieceSide() == Side.WHITE)
                    materialScore += pieceValues.get(type) + (phase == 1 ? midgamePST.get(type).get(i) : endgamePST.get(type).get(i));
                else
                    materialScore -= pieceValues.get(type) - (phase == 1 ? midgamePST.get(type).get(63 - i) : endgamePST.get(type).get(63 - i));
            }
        }

        // if we wanted a lazy evaluation (i.e. an idea of the actual score), return only the material score
        if (lazy)
            return materialScore;

        // if the estimate score is far off beta or alpha, no need to compute it's actual value
        if ((materialScore - lazyMargin > beta) || (materialScore + lazyMargin < alpha))
            return materialScore;

        // set the weights of different evaluations according to the game phase
        if (phase == 0) { // opening phase - values are not used
            matW = 0;
            contW = 0;
            pawnW = 0;
        } else if (phase == 1) { // middle phase
            matW = 1;
            contW = 10;
            pawnW = 1;
        } else if (phase == 2) { // end phase
            matW = 1;
            contW = 1;
            pawnW = 1;
        }

        // Pawn evaluation - adds malus to doubled, blocked and isolated pawns
        int w_doubled = 0, b_doubled = 0, w_blocked = 0, b_blocked = 0, w_isolated = 0, b_isolated = 0;
        int sq, p_d, p_b, p_i;
        long copyBoard = bboard;

        while (copyBoard != 0L) {

            p_d = 0;
            p_b = 0;
            p_i = 0;

            sq = bitScanForward(copyBoard);
            copyBoard = extractLsb(copyBoard);

            Square currentSquare = Square.squareAt(sq);
            Square[] sideSquares = currentSquare.getSideSquares();
            Piece p = board.getPiece(currentSquare);

            boolean pieceSideWhite = p.getPieceSide() == Side.WHITE;
            Piece controlPiece = pieceSideWhite ? Piece.WHITE_PAWN : Piece.BLACK_PAWN;

            int frontIndex = pieceSideWhite ? sq + 8 : sq - 8;
            Square frontSquare = Square.squareAt(frontIndex);
            Piece frontPiece = board.getPiece(frontSquare);

            if (frontIndex >= 0 && frontIndex < 64) {
                if (frontPiece == controlPiece)
                    p_d++;

                if (frontPiece != Piece.NONE)
                    p_b++;
            }

            boolean isIsolated = true;

            for (Square square : sideSquares) {
                if (board.getPiece(square) == controlPiece) {
                    isIsolated = false;
                    break;
                }
            }

            if (isIsolated)
                p_i++;

            if (pieceSideWhite) {
                w_doubled += p_d;
                w_blocked += p_b;
                w_isolated += p_i;
            } else {
                b_doubled += p_d;
                b_blocked += p_b;
                b_isolated += p_i;
            }
        }

        pawnScore = w_doubled - b_doubled + w_blocked - b_blocked + w_isolated - b_isolated;

        // Control evaluation - checks if a square is controlled (i.e. has more pieces covering it) by a side
        for (int i = 0; i < 64; i++) {
            int wCount = bitCount(board.squareAttackedBy(Square.squareAt(i), Side.WHITE));
            int bCount = bitCount(board.squareAttackedBy(Square.squareAt(i), Side.BLACK));
            if (wCount > bCount)
                controlScore += 1;
            else if (wCount < bCount)
                controlScore -= 1;
        }

        // Sort of tapered evaluation with the weights set earlier
        score = matW * materialScore + contW * controlScore - pawnW * pawnScore;

        return score;
    }

    // move ordering function
    public static int moveValue(Move m, Piece prom, boolean advancing, PieceType vic, PieceType atk, boolean isCap, int depth) {

        // if the sorting is in minimax (and not quiescence) and there is no piece captured for that move
        if (!isCap && vic == null) {

            // if it is a promotion return the value of the piece promoted
            if (!Piece.NONE.equals(prom))
                return pieceValues.get(prom.getPieceType());

            int hc = m.hashCode();
            // if it is a killer move return 3
            if (hc == killerMoves[depth - 1][0])
                return 3;
            else if (hc == killerMoves[depth - 1][1])
                return 2;
                // if it is a history move return its value
            else if (historyMoves.containsKey(hc))
                return historyMoves.get(hc);

            // if the piece moving is the king
            if (atk == PieceType.KING) {
                int o = Math.abs(m.getTo().ordinal() - m.getFrom().ordinal());
                // if it is to castle put that forward
                if (o == 2 || o == 3)
                    return 4;
                // if not send it at the back
                else
                    return -1;
            }

            // if the move is advancing (i.e. going up for white and down for black), return 1
            if (advancing)
                return 1;
        }
        // if the move is capturing, retrieve value to return with the MVV/LVA table
        else if (vic != null)
            return vic_atk_val[pieceIndex.get(vic)][pieceIndex.get(atk)];

        return 0;
    }
}