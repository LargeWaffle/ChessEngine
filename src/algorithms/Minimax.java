package algorithms;

import chesslib.*;
import chesslib.game.Game;
import chesslib.move.*;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.lang.reflect.Array;
import java.util.*;

import static chesslib.Bitboard.bitScanForward;
import static chesslib.Bitboard.extractLsb;
import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;
import static java.lang.Long.bitCount;
import static java.lang.Long.min;

public class Minimax {

    public static final int MINIMAX_DEPTH = 5;
    public static final int QUIESCENCE_DEPTH = 3;
    public static int transpSize = 100000;

    public static int toc = 0;

    public static String theFEN = "3r4/P5n1/3p1PPk/4p3/P7/RPK3PN/3N3P/8 w - - 0 1";
    public static String repFEN = "b7/b3k3/7R/3n4/4B1n1/3N3N/4K3/8 w - - 0 1";
    public static String endFEN = "5k2/p2r1p2/1p1bq2p/7Q/2PR3P/6P1/PP3PK1/R7 w - - 1 29 ";
    public static String promotionFEN = "8/7P/3k4/8/2P1K3/8/1b3PR1/8 w - - 2 69";

    public static double lowerBound = -10000.0, higherBound = 10000.0;

    public static Map<Character, Integer> values = new HashMap<>(Map.of(
            'Q', 9, 'R', 5, 'N', 3, 'B', 3, 'P', 1,
            'q', -9, 'r', -5, 'n', -3, 'b', -3, 'p', -1));

    public static int[][] vic_atk_val = {
            {60, 61, 62, 63, 64, 65, 0}, // victim K, attacker K, Q, R, B, N, P, None
            {50, 51, 52, 53, 54, 55, 0}, // victim Q, attacker K, Q, R, B, N, P, None
            {40, 41, 42, 43, 44, 45, 0}, // victim R, attacker K, Q, R, B, N, P, None
            {30, 31, 32, 33, 34, 35, 0}, // victim B, attacker K, Q, R, B, N, P, None
            {20, 21, 22, 23, 24, 25, 0}, // victim N, attacker K, Q, R, B, N, P, None
            {10, 11, 12, 13, 14, 15, 0}, // victim P, attacker K, Q, R, B, N, P, None
            {0, 0, 0, 0, 0, 0, 0},      // no victim
    };
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

    public static Move[][] killerMoves = new Move[MINIMAX_DEPTH][2];

    public static double frontierFutility = 100;
    public static double extendedFutility = 500;
    public static double rasorFutility = 900;
    public static double cpt = 0;
    public static double cpt2 = 0;
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

    public static Map<PieceType, List<Integer>> midgamePST = new HashMap<>(Map.of(
            PieceType.PAWN, pawnMiddleTable,
            PieceType.KNIGHT, knightMiddleTable,
            PieceType.BISHOP, bishopMiddleTable,
            PieceType.ROOK, rookMiddleTable,
            PieceType.QUEEN, queenMiddleTable,
            PieceType.KING, kingMiddleTable));

    public static Map<PieceType, List<Integer>> endgamePST = new HashMap<>(Map.of(
            PieceType.PAWN, pawnEndingTable,
            PieceType.KNIGHT, knightEndingTable,
            PieceType.BISHOP, bishopEndingTable,
            PieceType.ROOK, rookEndingTable,
            PieceType.QUEEN, queenEndingTable,
            PieceType.KING, kingEndingTable));

    public Minimax() {
    }

    public static Node minimax(Board board, int depth, double alpha, double beta, boolean max, boolean isCap) {

        cpt++;
        int gamePhase = board.gamePhase;

        if (board.isMated())
            return new Node(null, max ? lowerBound : higherBound);
        else if (board.isDraw())
            return new Node(null, 0);
        else if (board.isRepetition(2))
            return new Node(null, 0);

        long zKey = board.getZobristKey();
        int hashf = 0;

        if (transposition.containsKey(zKey % transpSize)) {
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

        if (depth <= 0) {
            Node node;
            if (isCap) // if last move was capturing launch a quiescence search to stabilize
                node = new Node(null, quiescenceSearch(board, alpha, beta, QUIESCENCE_DEPTH, max));
            else
                node = new Node(null, evaluate(board, gamePhase, alpha, beta, false));
            transposition.put(zKey % transpSize, new HashEntry(zKey, depth, hashf, node));

            /*if (gamePhase == 2 && (max && node.score >= 1500) || (!max && node.score <= -1500)) // not smart, low score variations in endGame
                depth = 2;
            else*/
            return node;
        }

        // this kinda works ... but not with quiescence (add allowNull in minimax)
   /*   if (gamePhase != 2 && allowNull && depth >= 3 && !board.isKingAttacked()) { // put here all conditions to check if its ok to nullmove

            double eval = evaluate(board, gamePhase);
            if (eval >= beta) {
                board.doNullMove();
                eval = minimax(board, depth - 2 - 1, -beta, -beta + 1, !max, false).score;
                board.undoMove();

                if (eval >= beta)
                    return new Node(null, eval);
            }
        }*/

        // generate children
        List<Move> moveList = board.legalMoves();

        // order the list to maximize alpha beta cutoff
        moveList.sort(Comparator.comparingInt((Move m) ->
                moveValue(m, m.getPromotion(), m.isAdvancing(max ? Side.WHITE : Side.BLACK), board.getPiece(m.getTo()).getPieceType(), board.getPiece(m.getFrom()).getPieceType(), false, depth)).reversed());

        Move bestMove = moveList.get(0);

        if (max) {
            double maxEval = -Double.MAX_VALUE;

            for (Move move : moveList) {

                Piece atkPiece = board.getPiece(move.getTo());
                board.doMove(move);

                boolean capMove = !Piece.NONE.equals(atkPiece);

                double fastValue = -evaluate(board, gamePhase, alpha, beta, true) + alpha;

                boolean boardCanPrune = fastValue > (lowerBound + alpha) && Piece.NONE.equals(move.getPromotion())
                        && !board.isKingAttacked();

                if (depth <= 3 && boardCanPrune) {
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

                double value = minimax(board, depth - 1, alpha, beta, false, capMove).score;

                if (value == higherBound)
                    value += depth;

                if (value > maxEval) {
                    maxEval = value;
                    bestMove = move;
                }

                alpha = Math.max(alpha, maxEval);

                board.undoMove();

                if (beta <= alpha) {
                    // store a non capturing killer move that is different as the one stored
                    if (!capMove && killerMoves[depth-1][1] != move) {
                        killerMoves[depth-1][1] = killerMoves[depth-1][0];
                        killerMoves[depth-1][0] = move;
                    }

                    hashf = 1;
                    break;
                }

            }

            Node node = new Node(bestMove, maxEval);
            transposition.put(zKey % transpSize, new HashEntry(zKey, depth, hashf, node));
            return node;

        } else {
            double minEval = Double.MAX_VALUE;

            for (Move move : moveList) {

                Piece atkPiece = board.getPiece(move.getTo());
                board.doMove(move);

                boolean capMove = !Piece.NONE.equals(atkPiece);

                double fastValue = evaluate(board, gamePhase, alpha, beta, true) - beta;

                boolean boardCanPrune = fastValue > (lowerBound - beta) && Piece.NONE.equals(move.getPromotion())
                        && !board.isKingAttacked();

                if (depth <= 3 && boardCanPrune) {
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

                double value = minimax(board, depth - 1, alpha, beta, true, capMove).score;

                if (value == lowerBound)
                    value -= depth;

                if (value < minEval) {
                    minEval = value;
                    bestMove = move;
                }

                beta = Math.min(beta, minEval);

                board.undoMove();

                if (beta <= alpha) {
                    // store a non capturing killer move that is different as the one stored
                    if (!capMove && killerMoves[depth-1][1] != move) {
                        killerMoves[depth-1][1] = killerMoves[depth-1][0];
                        killerMoves[depth-1][0] = move;
                    }

                    hashf = 2;
                    break;
                }

            }

            Node node = new Node(bestMove, minEval);
            transposition.put(zKey % transpSize, new HashEntry(zKey, depth, hashf, node));
            return node;
        }
    }

    public static double quiescenceSearch(Board board, double alpha, double beta, int depth, boolean max) {

        cpt2++;
        double DELTA = 1500; // delta cutoff

        if (board.isMated())
            return max ? lowerBound : higherBound;
        else if (board.isDraw())
            return 0;

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

        List<Move> capList = board.pseudoLegalCaptures();

        if (capList.isEmpty())
            return stand_pat;

        capList.sort(Comparator.comparingInt((Move m) ->
                moveValue(m, m.getPromotion(), m.isAdvancing(max ? Side.WHITE : Side.BLACK), board.getPiece(m.getTo()).getPieceType(), board.getPiece(m.getFrom()).getPieceType(), true, depth)).reversed());

        if (max) {

            double maxEval = -Double.MAX_VALUE;

            for (Move cap : capList) {

                if (!board.isMoveLegal(cap, true))
                    continue;

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

        int lazyMargin = 150;

        // Material evaluation
        long bboard = board.getBitboard();
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

        if (lazy)
            return materialScore;

        if ((materialScore - lazyMargin > beta) || (materialScore + lazyMargin < alpha))
            return materialScore;

        // BOARD ANALYSIS
        if (phase == 0) { // opening phase
            matW = 1;
            contW = 10;
            pawnW = 2;
        } else if (phase == 1) { // middle phase
            matW = 1;
            contW = 10;
            pawnW = 1;
        } else if (phase == 2) { // end phase
            matW = 1;
            contW = 1;
            pawnW = 1;
        }

        // Pawn eval
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


        // Control evaluation
        for (int i = 0; i < 64; i++) {
            int wCount = bitCount(board.squareAttackedBy(Square.squareAt(i), Side.WHITE));
            int bCount = bitCount(board.squareAttackedBy(Square.squareAt(i), Side.BLACK));
            if (wCount > bCount)
                controlScore += 1;
            else if (wCount < bCount)
                controlScore -= 1;
        }

        // Tapered evaluation
        score = matW * materialScore + contW * controlScore - pawnW * pawnScore;

        return score;
    }


    public static int moveValue(Move m, Piece prom, boolean advancing, PieceType vic, PieceType atk, boolean isCap, int depth) {

        if (!isCap && vic == null) {

            if (!Piece.NONE.equals(prom))
                return 40;

            if (m == killerMoves[depth-1][0] || m == killerMoves[depth-1][1])
                return 2;

            // maybe only for pawns
            if (advancing)
                return 1;
        }
        if (vic != null)// MVV/LVA ordering : highest
            return vic_atk_val[pieceIndex.get(vic)][pieceIndex.get(atk)];

        return 0;
    }
}