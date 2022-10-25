import algorithms.Minimax;
import chesslib.Board;
import chesslib.move.*;

import java.util.*;
import algorithms.Minimax.*;
import static chesslib.Side.*;

public class UCI {
    static String actualFen = "";
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
            MoveList list = new MoveList();
            list.loadFromSan(input);
            System.out.println("FEN of final position: " + list.getFen());
            actualFen = list.getFen();
        }
    }
    public static void inputGo() {
        Board board = new Board(); // TO REMOVE
        board.loadFromFen(actualFen);

        Minimax algo = new Minimax(actualFen, 2);
        Move bestMove = algo.bestMove;

        System.out.println("bestmove "+ bestMove);
    }

    public static void inputQuit() {
        System.exit(0);
    }
    public static void inputPrint() {

    }
}