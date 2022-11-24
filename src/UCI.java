import algorithms.Minimax;
import algorithms.Node;
import chesslib.*;
import chesslib.move.*;

import algorithms.StartTree;

import java.util.*;

import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;

public class UCI {
    static Board board = new Board();

    static boolean fromFen = false;
    static double totalNodes = 0;
    static double totalQNodes = 0;

    public static String moves = "";
    static String ENGINENAME = "Garry Kasparough loss";

    public static StartTree st = new StartTree();

    public static void uciCommunication() {

        Scanner input = new Scanner(System.in);
        while (true) {
            String inputString = input.nextLine();
            if ("uci".equals(inputString)) {
                inputUCI();
            } else if (inputString.startsWith("setoption")) {
                inputSetOption(inputString);
            } else if ("isready".equals(inputString)) {
                inputIsReady();
            } else if ("ucinewgame".equals(inputString)) {
                inputUCINewGame();
            } else if (inputString.startsWith("position")) {
                inputPosition(inputString);
            } else if (inputString.startsWith("go")) {
                inputGo();
            } else if (inputString.equals("quit")) {
                inputQuit();
            } else if ("print".equals(inputString)) {
                inputPrint();
            }
        }
    }

    public static void inputUCI() {
        System.out.println("id name " + ENGINENAME);
        System.out.println("id author Thomartin");
        //options go here
        System.out.println("uciok");
    }

    public static void inputSetOption(String inputString) {
        //set options
    }

    public static void inputIsReady() {
        System.out.println("readyok");
    }

    public static void inputUCINewGame() {
        board = new Board();
        st = new StartTree();
        new Minimax();
        Minimax.transposition.clear();
        board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    public static void inputPosition(String input) {
        String fen = "";
        input = input.substring(9).concat(" "); // get rid of "position "

        if (input.contains("startpos ")) {
            input = input.substring(9); // get rid of "startpos "
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

        } else if (input.contains("fen")) {
            input = input.substring(4); // get rid of "fen "
            fen = input;
            board.loadFromFen(input);
            fromFen = true;
        }

        if (input.contains("moves")) {
            input = input.substring(input.indexOf("moves") + 6); // get rid of "moves"
            moves = input;
            MoveList list;
            if (fromFen)
                list = new MoveList(fen);
            else
                list = new MoveList();

            list.loadFromSan(input);
            for (Move mv : list)
                board.doMove(mv);
        }
    }

        public static void inputGo () {

            if (fromFen)
                board.updateGamePhase(1);

            if (board.gamePhase == 0) {
                List<String> mlist = List.of(moves.split(" "));
                st.search(StartTree.tree, mlist);
                if (st.tree_move == null) {
                    board.updateGamePhase(1);
                    inputGo();
                } else if (board.legalMoves().toString().contains(st.tree_move)) {
                    System.out.println("bestmove " + st.tree_move);
                } else {
                    int m_count = 0;
                    while (board.getPiece(board.legalMoves().get(m_count).getFrom()).getPieceType() != PieceType.PAWN)
                        m_count++;
                    System.out.println("bestmove " + board.legalMoves().get(m_count));
                }

                if (st.phase1) // try to anticipate the exit of start tree
                    board.updateGamePhase(1);

            } else {
                boolean isMax = board.getSideToMove() == Side.WHITE;
                Node bestNode;
                if (board.gamePhase == 3)
                    bestNode = Minimax.minimax(board, Minimax.MINIMAX_MAX_DEPTH, -Double.MAX_VALUE, Double.MAX_VALUE, isMax, false, true);
                else
                    bestNode = Minimax.minimax(board, Minimax.MINIMAX_DEPTH, -Double.MAX_VALUE, Double.MAX_VALUE, isMax, false, true);

                if (bestNode.move == null)
                    System.out.println("bestmove " + board.legalMoves().get(0));
                else
                    System.out.println("bestmove " + bestNode.move);
                Minimax.cpt = 0;
                Arrays.stream(Minimax.killerMoves).forEach(x -> Arrays.fill(x, 0));
                Minimax.historyMoves.clear();

                Minimax.QUIESCENCE_DEPTH = 3;
                Minimax.frontierFutility = 100;
                Minimax.extendedFutility = 500;
                Minimax.rasorFutility = 900;
                board.doMove(bestNode.move);
                if (board.gamePhase == 2 && (board.isKingSolo(Side.WHITE) || board.isKingSolo(Side.BLACK)))
                    board.gamePhase = 3;

                if (board.getMoveCounter() > 20)
                    if (board.isSetEndPhase()) // transition to end phase
                        board.updateGamePhase(2);
            }
        }

        public static void inputQuit () {
            System.exit(0);
        }

        public static void inputPrint () {

        }
    }