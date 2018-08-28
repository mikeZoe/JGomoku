package ai;

import gui.constant.GuiConst;

/**
 * This class is an AI agent using miniMax search with alpha beta pruning
 *
 * @author Chang ta'z jun
 * @version Version 1.1
 */
public class DemoAgent {
    static int count = 0;

    private DemoAgent() {
    }

    /**
     * Starts miniMax search
     *
     * @param chess 2-dimension array represents the chessboard
     * @return Coordinates of the best next move for the computer
     */
    public static int[] startMiniMax(int[][] chess) {
        Node root = new Node(-1, -1, -1, chess);
        Node result = miniMax(root, 0, -1, true);
        System.out.println("x " + result.getX() + "y " + result.getY() + "score " + result.getScore());
        return new int[] {result.getX(), result.getY()};
    }

    /**
     * Provides depth first miniMax search for the game tree and returns
     * the most valuable node
     *
     * @param root      Current tree node
     * @param depth     Current Depth of the node
     * @param pieceType Identification of players, 1 for black piece and -1 white piece
     * @param isMax     Identification of max nodes or min nodes
     * @return The most valuable node
     */
    private static Node miniMax(Node root, int depth, int pieceType, boolean isMax) {
        count++;
        if (depth >= 2) {
            //@Todo
            root.setScore(HeuristicAgent.heuristic(root.getChess()));
            return root;
        }

        int[][] chess = root.getChess();
        int bestScore = isMax ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        Node bestChild = null;

        for (int i = 0; i < GuiConst.TILE_NUM_PER_ROW; i++) {

            for (int j = 0; j < GuiConst.TILE_NUM_PER_ROW; j++) {
                //if the tile is empty
                if (chess[i][j] == 0) {
                    //create child node and search it
                    int[][] dummy = ToolKit.copyArray(chess);
                    dummy[i][j] = pieceType;
                    Node child = new Node(i, j, 0, dummy);

                    int score = miniMax(child, depth + 1, pieceType * -1, !isMax).getScore();

                    if (isMax) {
                        if (score > bestScore) {
                            bestScore = score;
                            bestChild = child;
                        }
                    } else {
                        if (score < bestScore) {
                            bestScore = score;
                            bestChild = child;
                        }
                    }
                }
            }
        }
        root.setScore(bestScore);

        if (depth == 0) {
            System.out.println("Minimax total nodes: " + count);
            count = 0;
            return bestChild;
        }

        return root;
    }

    /**
     * Starts depth first miniMax search with alpha beta pruning
     *
     * @param chess 2-dimension array represents the chessboard
     * @return Coordinates of the best next move for the computer
     */
    public static int[] startAlphaBetaPruning(int[][] chess) {
        Node root = new Node(-1, -1, -1, chess);
        Node bestMove = alphaBetaPruning_Maximizer(root, 0, -1, Integer.MIN_VALUE, Integer.MAX_VALUE);
        int[] result = new int[2];
        result[0] = bestMove.getX();
        result[1] = bestMove.getY();
        System.out.println("x " + bestMove.getX() + "y " + bestMove.getY() + "score " + bestMove.getScore());

        return result;
    }

    /**
     * This methods is the maximizer of alpha beta pruning, it prunes the current node
     * when the alpha value of current node is greater than or equal to the beta value
     * of its ancient node
     *
     * @param root      Current tree node
     * @param depth     Current depth of the node
     * @param pieceType Identification of players, 1 for black piece and -1 white piece
     * @param alpha     alpha value for Max node
     * @param beta      beta value for Min node
     * @return The most valuable node
     */
    private static Node alphaBetaPruning_Maximizer(Node root, int depth, int pieceType, int alpha, int beta) {
        count++;
        //base case
        if (depth >= 2) {
            root.setScore(HeuristicAgent.heuristic(root.getChess()));
            return root;
        }

        int[][] chess = root.getChess();
        int bestScore = Integer.MIN_VALUE;
        Node bestChild = null;

        for (int i = 0; i < GuiConst.TILE_NUM_PER_ROW; i++) {
            for (int j = 0; j < GuiConst.TILE_NUM_PER_ROW; j++) {
                // for each possible moves, do depth first search
                if (chess[i][j] == 0) {
                    int[][] nextMove = ToolKit.nextMoveChessboard(chess, i, j, pieceType);
                    Node child = new Node(i, j, -1, nextMove);

                    int score = alphaBetaPruning_Minimizer(child, depth + 1, pieceType * -1, alpha, beta).getScore();
                    if (score > bestScore) {
                        bestScore = score;
                        bestChild = child;
                        alpha = score;
                    }
                    //beta pruning
                    if (score >= beta) {
                        root.setScore(bestScore);
                        return bestChild;
                    }
                }
            }
        }

        root.setScore(bestScore);

        if (depth == 0) {
            System.out.println("total nodes: " + count);
            count = 0;
            return bestChild;
        }

        return root;
    }

    /**
     * This methods is the minimizer of alpha beta pruning, it prunes the current node
     * when the beta value of current node is less than or equal to the alpha value
     * of its ancient node
     *
     * @param root      Current tree node
     * @param depth     Current depth of the node
     * @param pieceType Identification of players, 1 for black piece and -1 white piece
     * @param alpha     Alpha value for Max node
     * @param beta      Beta value for Min node
     * @return The most valuable node
     */
    private static Node alphaBetaPruning_Minimizer(Node root, int depth, int pieceType, int alpha, int beta) {
        count++;
        //base case
        if (depth >= 2) {
            root.setScore(HeuristicAgent.heuristic(root.getChess()));
            return root;
        }

        int[][] chess = root.getChess();
        int bestScore = Integer.MAX_VALUE;
        Node bestChild = null;

        for (int i = 0; i < GuiConst.TILE_NUM_PER_ROW; i++) {
            for (int j = 0; j < GuiConst.TILE_NUM_PER_ROW; j++) {
                // for each possible moves, do depth first search
                if (chess[i][j] == 0) {
                    int[][] nextMove = ToolKit.nextMoveChessboard(chess, i, j, pieceType);
                    Node child = new Node(i, j, -1, nextMove);

                    int score = alphaBetaPruning_Maximizer(child, depth + 1, pieceType * -1, alpha, beta).getScore();
                    if (score < bestScore) {
                        bestScore = score;
                        bestChild = child;
                        beta = score;
                    }
                    //alpha pruning
                    if (score <= alpha) {
                        root.setScore(bestScore);
                        return bestChild;
                    }
                }
            }
        }

        root.setScore(bestScore);

        if (depth == 0) {
            return bestChild;
        }

        return root;
    }

    /**
     * Provides a heuristic function for the game
     *
     * @param chess     2-dimension array represents the chessboard
     * @param pieceType 1 for black piece and -1 white piece
     * @return Score of the chessboard
     */
    public static int evaluation(int[][] chess, int pieceType) {
        int scoreAlly = 0;
        int scoreOppo = 0;
        for (int i = 0; i < GuiConst.TILE_NUM_PER_ROW; i++) {
            for (int j = 0; j < GuiConst.TILE_NUM_PER_ROW; j++) {
                if (chess[i][j] == pieceType) {
                    //scoreAlly += BasicAgent.markPiece(chess, i, j, pieceType);
                    scoreAlly += EvalAgent.eval(chess, i, j, pieceType);
                }
                if (chess[i][j] == pieceType * -1) {
                    //scoreOppo += BasicAgent.markPiece(chess, i, j, pieceType * -1);
                    scoreOppo += EvalAgent.eval(chess, i, j, pieceType * -1);
                }
            }
        }
        return scoreAlly - scoreOppo;
    }
}

/**
 * This class represents the node of the adversarial search tree,
 * each node is a possible move of the game
 *
 * @author chang ta'z jun
 * @version 1.1
 */
class Node {
    /**
     * X coordinate of the last move
     */
    private int x;

    /**
     * Y coordinate of the last move
     */
    private int y;

    /**
     * Score of the node
     */
    private int score;

    /**
     * Chessboard of the board
     */
    private int[][] chess;

    Node(int x, int y, int score, int[][] chess) {
        this.x = x;
        this.y = y;
        this.score = score;
        this.chess = chess;
    }

    int getX() {
        return this.x;
    }

    int getY() {
        return this.y;
    }

    int getScore() {
        return this.score;
    }

    int[][] getChess() {
        return this.chess;
    }

    void setScore(int score) {
        this.score = score;
    }
}
