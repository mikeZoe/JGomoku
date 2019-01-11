/**
 * @author Cirun Zhang
 * @Date 2019.1.8
 */
package ai;

import ai.constant.AiConst;
import ai.utility.AiUtils;
import gui.constant.GuiConst;
import observer.GameStatuChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MonteCalro extends Agent {
    private static int iteration;

    public static void tester(int[][] chess) {
        iteration = 0;
        TreeNode root = new TreeNode(true, aiPieceType * -1, -1, -1, chess, null);
        while (iteration < 30000) {
            selection(root);
        }

        List<TreeNode> children = root.getChildren();
        int maxVisits = Integer.MIN_VALUE;
        int max_x = -1;
        int max_y = -1;
        for (TreeNode child : children) {
            if (child.getVisitsCount() > maxVisits) {
                maxVisits = child.getVisitsCount();
                max_x = child.getX();
                max_y = child.getY();
            }
        }

        System.out.println(max_x + "===" + max_y);

    }

    /**
     * Entrance of MCTS
     *
     * @param chess 2-dimensional array represents the chessboard
     * @return Desired next move
     */
    public static int[] monteCalroTreeSearch(int[][] chess) {
        iteration = 0;

        TreeNode root = new TreeNode(true, aiPieceType * -1, -1, -1, chess, null);

        while (iteration < 50000) {
            selection(root);
        }

        List<TreeNode> children = root.getChildren();
        int maxVisits = Integer.MIN_VALUE;
        int max_x = -1;
        int max_y = -1;
        for (TreeNode child : children) {
            if (child.getVisitsCount() > maxVisits) {
                maxVisits = child.getVisitsCount();
                max_x = child.getX();
                max_y = child.getY();
            }
        }

        System.out.println(root.getReward() + "-" + root.getVisitsCount());
        System.out.println(max_x + "===" + max_y);
        System.out.println(maxVisits);
        return new int[] {max_x, max_y, aiPieceType};
    }

    /**
     * Selection process of MCTS
     *
     * @param root The node for process selection, initially the node is set to the node
     */
    private static void selection(TreeNode root) {
        //System.out.println("selection");
        if (root.isLeaf()) {
            if (root.getVisitsCount() == 0) {
                rollout(root);
            } else {
                expansion(root);
            }
        } else {
            List<TreeNode> children = root.getChildren();
            TreeNode best = ucbSelection(children);
            if (best != null) {
                selection(best);
            } else {
                System.out.println("null");
            }

        }
    }

    /**
     * Expansion process of MCTS
     *
     * @param node The leaf node need to be expanded
     */
    private static void expansion(TreeNode node) {
        //System.out.println("expansion");
        List<TreeNode> children = generatesChildren(node);
        node.setChildren(children);
        node.setLeaf(false);
        selection(node);
    }

    //Todo check the validity of this function.

    /**
     * Rollout process of MCTS. The rollout only stops when the simulated game is terminated
     *
     * @param node The node need to be simulated
     */
    private static void rollout(TreeNode node) {
        iteration++;
        int numOfMoves = 0;
        int[][] chess = AiUtils.copyArray(node.getChess());
        int lastTurnPlayer = node.getThisTurnPlayer();
        PossibleMove randomMove;

        do {
            lastTurnPlayer *= -1;
            numOfMoves++;
            randomMove = getRandomMove(chess);
            if (randomMove == null) {
                System.out.println("randomMove == null");
                break;
            }
            placePiece(chess, randomMove, lastTurnPlayer);
        } while (!GameStatuChecker.isFiveInLine(chess, randomMove.getX(), randomMove.getY()));

        //back propagation
        backPropagation(node, 1, lastTurnPlayer);
    }

    @Deprecated private static void rolloutCheat(TreeNode node) {
        iteration++;
        int numOfMoves = 0;
        int[][] chess = AiUtils.copyArray(node.getChess());
        int lastTurnPlayer = node.getThisTurnPlayer();
        PossibleMove randomMove;

        do {
            lastTurnPlayer *= -1;
            numOfMoves++;
            randomMove = getRandomMoveCheat(chess);
            if (randomMove == null) {
                System.out.println("randomMove == null");
                break;
            }
            placePiece(chess, randomMove, lastTurnPlayer);
        } while (!GameStatuChecker.isFiveInLine(chess, randomMove.getX(), randomMove.getY()));

        //back propagation
        backPropagation(node, 1, lastTurnPlayer);

    }

    /**
     * Back propagation process of MCTS
     *
     * @param node         The back propagated node
     * @param reward       The reward for winning nodes
     * @param winningPiece Indicates which player wins
     */
    private static void backPropagation(TreeNode node, int reward, int winningPiece) {
        //System.out.println("back prop");
        if (node != null) {
            if (node.getThisTurnPlayer() == winningPiece) {
                node.increaseReward(reward);
            } else {
                node.increaseReward(-1);
            }
            node.increaseVisitCount();
            backPropagation(node.getParent(), reward, winningPiece);
        }
    }

    /**
     * UCB-1 function of MCTS
     *
     * @param node Calculates the UCB value for this particular node
     * @return UCB value
     */
    private static double ucb1(TreeNode node) {
        int reward = node.getReward();
        int visitCount = node.getVisitsCount();
        int parentVisitCount = node.getParent().getVisitsCount();
        double c = 1.1; //0.1;//1.414;
        return AiUtils.safeDivide(reward, visitCount) + c * Math
            .sqrt(AiUtils.safeDivide(Math.log(parentVisitCount), visitCount));
    }

    /**
     * Selects the child node with the highest UCB value
     *
     * @param children The child nodes
     * @return The best node
     */
    private static TreeNode ucbSelection(List<TreeNode> children) {
        double max = Double.NEGATIVE_INFINITY;
        TreeNode best = null;

        for (TreeNode child : children) {
            double ucbVal = ucb1(child);
            if (ucbVal > max) {
                max = ucbVal;
                best = child;
            }

            if (max == Double.POSITIVE_INFINITY) {
                return best;
            }
        }

        if (best == null) {
            System.out.println(ucb1(children.get(0)));
        }
        return best;
    }

    private static List<TreeNode> generatesChildren(TreeNode node) {
        List<TreeNode> children = new ArrayList<>();

        int nextTurnPlayer = node.getThisTurnPlayer() * -1;
        int[][] chess = node.getChess();

        //Generates 10 child nodes
        List<int[]> moves = AiUtils.moveGeneratorTop10(chess);

        for (int[] move : moves) {
            int x = move[0];
            int y = move[1];
            int[][] nextChess = AiUtils.nextMoveChessboard(chess, x, y, nextTurnPlayer);
            boolean isTerminal = GameStatuChecker.isFiveInLine(nextChess, x, y);

            if(!isTerminal){
                children.add(new TreeNode(true, nextTurnPlayer, x, y, nextChess, node));
            }else{
                //System.out.println("is terminal node");
                backPropagation(node, 1, nextTurnPlayer);
            }
        }

        return children;
    }

    private static PossibleMove getRandomMove(int[][] chess) {
        List<PossibleMove> possibleMoves = generatesMoves(chess);
        int size = possibleMoves.size();

        if (size == 0) {
            System.out.println("Chess board full");
            return null;
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(0, size);
        return possibleMoves.get(randomIndex);
    }

    @Deprecated private static PossibleMove getRandomMoveCheat(int[][] chess) {
        List<int[]> possibleMoves = AiUtils.moveGeneratorTop100(chess);
        int size = possibleMoves.size();

        if (size == 0) {
            System.out.println("Chess board full");
            return null;
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(0, size);
        int[] nextMove = possibleMoves.get(randomIndex);
        return new PossibleMove(nextMove[0], nextMove[1]);
    }

    private static List<PossibleMove> generatesMoves(int[][] chess) {
        List<PossibleMove> possibleMoves = new ArrayList<>();
        for (int i = 0; i < GuiConst.TILE_NUM_PER_ROW; i++) {
            for (int j = 0; j < GuiConst.TILE_NUM_PER_ROW; j++) {
                if (chess[i][j] == AiConst.EMPTY_STONE) {
                    possibleMoves.add(new PossibleMove(i, j));
                }
            }
        }
        return possibleMoves;
    }

    private static void placePiece(int[][] chess, PossibleMove move, int pieceType) {
        chess[move.getX()][move.getY()] = pieceType;
    }

}

class PossibleMove {
    private int x;
    private int y;

    public PossibleMove(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}

class TreeNode {
    private boolean isLeaf;

    private boolean isTerminal;

    private int thisTurnPlayer;

    private int x;

    private int y;

    private int[][] chess;

    private int reward = 0;

    private int visitsCount = 0;

    private TreeNode parent;

    private List<TreeNode> children;

    public TreeNode(int[][] chess) {
        this.chess = chess;
    }

    public TreeNode(boolean isLeaf, int[][] chess) {
        this.isLeaf = isLeaf;
        this.chess = chess;
    }

    public TreeNode(boolean isLeaf, int thisTurnPlayer, int x, int y, int[][] chess, TreeNode parent) {
        this.isLeaf = isLeaf;
        this.thisTurnPlayer = thisTurnPlayer;
        this.x = x;
        this.y = y;
        this.chess = chess;
        this.parent = parent;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }

    public int[][] getChess() {
        return chess;
    }

    public void setChess(int[][] chess) {
        this.chess = chess;
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public int getVisitsCount() {
        return visitsCount;
    }

    public void setVisitsCount(int visitsCount) {
        this.visitsCount = visitsCount;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    public void setTerminal(boolean terminal) {
        isTerminal = terminal;
    }

    public int getThisTurnPlayer() {
        return thisTurnPlayer;
    }

    public void setThisTurnPlayer(int thisTurnPlayer) {
        this.thisTurnPlayer = thisTurnPlayer;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void increaseReward(int reward) {
        this.reward += reward;
    }

    public void increaseVisitCount() {
        this.visitsCount += 1;
    }
}