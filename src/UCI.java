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

    public static String[] whiteOpen = {"g1f3", "c2c4", "g2g3", "f1g2", "e1g1"};

    public static String moves = "";
    static String ENGINENAME = "Tomart1";

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
        input = input.substring(9).concat(" ");

        if (input.contains("startpos ")) {
            input = input.substring(9);
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

        } else if (input.contains("fen")) {
            input = input.substring(4);
            fen = input;
            board.loadFromFen(input);
            fromFen = true;
        }

        if (input.contains("moves")) {

            input = input.substring(input.indexOf("moves") + 6);
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

            if (board.gamePhase != 1 && board.getMoveCounter() == 6) { // transition to middle phase
                board.updateGamePhase(1);
            }

            if (fromFen)
                board.updateGamePhase(1);

            if (board.isSetEndPhase()) { // transition to end phase
                board.updateGamePhase(2);
            }


            if (board.gamePhase == 0) {
                if (board.getSideToMove() == Side.WHITE) {
                    if (board.getMoveCounter() != 2) {
                        System.out.println("bestmove " + whiteOpen[board.getMoveCounter() - 1]);
                    } else {
                        boolean hasPiece = false;
                        for (Square sq : new Square[]{Square.E5, Square.G5}) {
                            if (board.getPiece(sq) != Piece.NONE)
                                hasPiece = true;
                        }
                        if (!hasPiece) {
                            System.out.println("bestmove " + whiteOpen[board.getMoveCounter() - 1]);
                        } else { // GERE LE CAS OU L'AUTRE DONNE PIECE
                            board.updateGamePhase(1);
                            inputGo();
                        }
                    }
                } else {
                    List<String> mlist = List.of(moves.split(" "));
                    st.search(StartTree.tree, mlist);
                    if (st.tree_move == null) { // GERE LE CAS OU L'AUTRE DONNE PIECE
                        board.updateGamePhase(1);
                        inputGo();
                    } else if (board.legalMoves().contains(st.tree_move)) {
                        System.out.println("bestmove " + st.tree_move);
                    } else {
                        int m_count = 0;
                        while (board.getPiece(board.legalMoves().get(m_count).getFrom()).getPieceType() != PieceType.PAWN)
                            m_count++;
                        System.out.println("bestmove " + board.legalMoves().get(m_count));
                    }


                    if (st.phase1)
                        board.updateGamePhase(1);
                }
            } else {
                boolean isMax = board.getSideToMove() == Side.WHITE;
                long start = System.currentTimeMillis();
                Node bestNode;
                /*if (board.gamePhase == 2 && pieceCount)
                    bestNode = Minimax.minimax(board, Minimax.MINIMAX_MAX_DEPTH, -Double.MAX_VALUE, Double.MAX_VALUE, isMax, false);
                else*/
                    bestNode = Minimax.minimax(board, Minimax.MINIMAX_DEPTH, -Double.MAX_VALUE, Double.MAX_VALUE, isMax, false);

                System.out.println(System.currentTimeMillis() - start);
                System.out.println("Nodes explored " + Minimax.cpt);
                System.out.println("Q nodes explored " + Minimax.cpt2);
                System.out.println("bestscore " + bestNode.score);
                if (bestNode.move == null)
                    System.out.println("bestmove " + board.legalMoves().get(0));
                else
                    System.out.println("bestmove " + bestNode.move);
                totalNodes += Minimax.cpt;
                System.out.println("total nodes explored " + totalNodes);
                totalQNodes += Minimax.cpt2;
                System.out.println("total Q nodes explored " + totalQNodes);
                System.out.println("total toc " + Minimax.toc);
                System.out.println("Gamephase is " + board.gamePhase);
                Minimax.cpt = 0;
                Minimax.cpt2 = 0;
                Arrays.stream(Minimax.killerMoves).forEach(x -> Arrays.fill(x, 0));
                Minimax.historyMoves.clear();
            }
        }

        public static void inputQuit () {
            System.exit(0);
        }

        public static void inputPrint () {

        }
    }