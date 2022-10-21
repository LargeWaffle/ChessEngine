import chesslib.Board;

import java.util.*;

import static chesslib.Side.*;

public class UCI {
    static String ENGINENAME="Tomart1";
    public static void uciCommunication() {
        Scanner input = new Scanner(System.in);
        while (true)
        {
            String inputString=input.nextLine();
            if ("uci".equals(inputString))
            {
                inputUCI();
            }
            else if (inputString.startsWith("setoption"))
            {
                inputSetOption(inputString);
            }
            else if ("isready".equals(inputString))
            {
                inputIsReady();
            }
            else if ("ucinewgame".equals(inputString))
            {
                inputUCINewGame();
            }
            else if (inputString.startsWith("position"))
            {
                inputPosition(inputString);
            }
            else if (inputString.startsWith("go"))
            {
                inputGo();
            }
            else if (inputString.equals("quit"))
            {
                inputQuit();
            }
            else if ("print".equals(inputString))
            {
                inputPrint();
            }
        }
    }
    public static void inputUCI() {
        System.out.println("id name "+ENGINENAME);
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
        Board board = new Board(); // TO REMOVE
    }
    public static void inputPosition(String input) {
        Board board = new Board(); // TO REMOVE
        input=input.substring(9).concat(" ");
        if (input.contains("startpos ")) {
            input=input.substring(9);
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        }
        else if (input.contains("fen")) {
            input=input.substring(4);
            board.loadFromFen(input);
        }
        if (input.contains("moves")) {
            input=input.substring(input.indexOf("moves")+6);
            while (input.length()>0)
            {
                board.legalMoves();
                /*
                for (Move move : board.legalMoves()) {
                    board.doMove(move);
                    //do something
                    board.undoMove();
                }
                 */
                input=input.substring(input.indexOf(' ')+1);
            }
        }
    }
    public static void inputGo() {
        Board board = new Board(); // TO REMOVE
        board.legalMoves();
        int index=(int)(Math.floor(Math.random()*(board.legalMoves().toArray().length/4))*4);
        System.out.println("bestmove "+ board.legalMoves().get(index));
    }

    public static void inputQuit() {
        System.exit(0);
    }
    public static void inputPrint() {

    }
}