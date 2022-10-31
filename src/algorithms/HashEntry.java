package algorithms;

import chesslib.move.Move;

public class HashEntry {
    public long zobrist;
    public int depth;
    public int flag;
    public Node node;

    public HashEntry (long zobrist, int depth, int flag, Node node) {
        this.zobrist = zobrist;
        this.depth = depth;
        this.flag = flag;
        this.node = node;
    }
}
