package algorithms;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class StartTree {
    public static treeNode tree;
    public static String tree_move;

    public static boolean phase1 = false;

    public StartTree() {
        tree = new treeNode("", Arrays.asList(
                new treeNode("e2e4", List.of(
                        new treeNode("c7c5", Arrays.asList(
                                new treeNode("g1f3", List.of(
                                        new treeNode("d7d6", Arrays.asList(
                                                new treeNode("d2d4", List.of(
                                                        new treeNode("c5d4", null)
                                                )),
                                                new treeNode("f1b5", List.of(
                                                        new treeNode("c8d7", null)
                                                )),
                                                new treeNode("c2c3", List.of(
                                                        new treeNode("g8f6", null)
                                                ))
                                        ))
                                )),
                                new treeNode("c2c3", List.of(
                                        new treeNode("g8f6", Arrays.asList(
                                                new treeNode("e4e5", List.of(
                                                        new treeNode("f6d5", null)
                                                )),
                                                new treeNode("d2d3", List.of(
                                                        new treeNode("b8c6", null)
                                                ))
                                        ))
                                )),
                                new treeNode("b1c3", List.of(
                                        new treeNode("b8c6", Arrays.asList(
                                                new treeNode("f1b5", List.of(
                                                        new treeNode("c6d4", null)
                                                )),
                                                new treeNode("g2g3", List.of(
                                                        new treeNode("g7g56", null)
                                                )),
                                                new treeNode("f2f4", List.of(
                                                        new treeNode("g7g6", null)
                                                )),
                                                new treeNode("g1f3", List.of(
                                                        new treeNode("g7g6", null)
                                                ))
                                        ))
                                ))
                        ))
                )),
                new treeNode("d2d4", List.of(
                        new treeNode("g8f6", Arrays.asList(
                                new treeNode("c2c4", List.of(
                                        new treeNode("e7e6", Arrays.asList(
                                                new treeNode("b1f3", List.of(
                                                        new treeNode("f8b4", null)
                                                )),
                                                new treeNode("g1f3", List.of(
                                                        new treeNode("b7b6", null)
                                                )),
                                                new treeNode("g2g3", List.of(
                                                        new treeNode("d7d5", null)
                                                ))
                                        ))
                                )),
                                new treeNode("g1f3", List.of(
                                        new treeNode("g7g6", Arrays.asList(
                                                new treeNode("c2c4", List.of(
                                                        new treeNode("f8g7", null)
                                                )),
                                                new treeNode("g2g3", List.of(
                                                        new treeNode("f8g7", null)
                                                )),
                                                new treeNode("c1g5", List.of(
                                                        new treeNode("f8g7", null)
                                                ))
                                        ))
                                )),
                                new treeNode("c1g5", List.of(
                                        new treeNode("e7e6", Arrays.asList(
                                                new treeNode("e2e4", List.of(
                                                        new treeNode("h7h6", null)
                                                )),
                                                new treeNode("b1d2", List.of(
                                                        new treeNode("h7h6", null)
                                                ))
                                        ))
                                ))
                        ))
                )),
                new treeNode("g1f3", List.of(
                        new treeNode("g8f6", Arrays.asList(
                                new treeNode("c2c4", List.of(
                                        new treeNode("g7g6", Arrays.asList(
                                                new treeNode("b1c3", List.of(
                                                        new treeNode("f8g7", null)
                                                )),
                                                new treeNode("g2g3", List.of(
                                                        new treeNode("f8g7", null)
                                                ))
                                        ))
                                )),
                                new treeNode("g2g3", List.of(
                                        new treeNode("g7g6", Arrays.asList(
                                                new treeNode("f1g2", List.of(
                                                        new treeNode("f8g7", null)
                                                )),
                                                new treeNode("b2b3", List.of(
                                                        new treeNode("f8g7", null)
                                                ))
                                        ))
                                )),
                                new treeNode("d2d4", List.of(
                                        new treeNode("g7g6", Arrays.asList(
                                                new treeNode("c2c4", List.of(
                                                        new treeNode("f8g7", null)
                                                )),
                                                new treeNode("g2g3", List.of(
                                                        new treeNode("f8g7", null)
                                                )),
                                                new treeNode("c1g5", List.of(
                                                        new treeNode("f8g7", null)
                                                ))
                                        ))
                                ))
                        ))
                )),
                new treeNode("c2c4", List.of(
                        new treeNode("g8f6", Arrays.asList(
                                new treeNode("b1c3", List.of(
                                        new treeNode("g7g6", Arrays.asList(
                                                new treeNode("g2g3", List.of(
                                                        new treeNode("f8g7", null)
                                                )),
                                                new treeNode("e2e4", List.of(
                                                        new treeNode("d7d6", null)
                                                ))
                                        ))
                                )),
                                new treeNode("g2g3", List.of(
                                        new treeNode("g7g6", Arrays.asList(
                                                new treeNode("f1g2", List.of(
                                                        new treeNode("f8g7", null)
                                                )),
                                                new treeNode("b1c3", List.of(
                                                        new treeNode("f8g7", null)
                                                ))
                                        ))
                                )),
                                new treeNode("g1f3", List.of(
                                        new treeNode("g7g6", Arrays.asList(
                                                new treeNode("b1c3", List.of(
                                                        new treeNode("f8g7", null)
                                                )),
                                                new treeNode("g2g3", List.of(
                                                        new treeNode("f8g7", null)
                                                ))
                                        ))
                                ))
                        ))
                ))
        ));

    }

    public static void search(treeNode node, List<String> movelist) {
        tree_move = null;
        String mv = movelist.get(0);
        List<String> new_mlist = movelist.subList(1, movelist.size());
        for (treeNode child : node.children) {
            if (Objects.equals(mv, child.move)) {
                if (new_mlist.size() == 0) {
                    tree_move = child.children.get(0).move;
                    if (child.children.get(0).children == null)
                        phase1 = true;
                }
                else
                    search(child, new_mlist);
            }
        }
    }
}

class treeNode {
    String move = "";
    List<treeNode> children = null;

    treeNode(String mv, List<treeNode> cld) {
        move = mv;
        children = cld;
    }
}