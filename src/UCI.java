import algorithms.Minimax;
import algorithms.Node;
import chesslib.Board;
import chesslib.Piece;
import chesslib.Side;
import chesslib.Square;
import chesslib.move.*;

import algorithms.StartTree;

import java.util.*;

public class UCI {
    static Board board = new Board();

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
        board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    public static void inputPosition(String input) {
        input = input.substring(9).concat(" ");

        if (input.contains("startpos ")) {
            input = input.substring(9);
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        } else if (input.contains("fen")) {
            input = input.substring(4);
            board.loadFromFen(input);
        }
        if (input.contains("moves")) {
            input = input.substring(input.indexOf("moves") + 6);
            moves = input;
            MoveList list = new MoveList();
            list.loadFromSan(input);
            board.loadFromFen(list.getFen());
        }
    }

    public static void inputGo() {

        if (board.gamePhase != 1 && board.getMoveCounter() == 6) { // transition to middle phase
            board.updateGamePhase(1);
        }

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
                } else
                    System.out.println("bestmove " + st.tree_move);

                if (st.phase1)
                    board.updateGamePhase(1);
            }

        } else {
            boolean isMax = board.getSideToMove() == Side.WHITE;
            long start = System.currentTimeMillis();
            Node bestNode = Minimax.minimax(board, 5, -Double.MAX_VALUE, Double.MAX_VALUE, isMax);
            System.out.println(System.currentTimeMillis() - start);
            System.out.println("Nodes explored " + Minimax.cpt);
            System.out.println("bestmove " + bestNode.move);
            Minimax.cpt = 0;
        }
    }

    public static void inputQuit() {
        System.exit(0);
    }

    public static void inputPrint() {

    }
}