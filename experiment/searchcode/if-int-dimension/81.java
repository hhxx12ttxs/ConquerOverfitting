import java.util.HashSet;
import java.util.Set;

public class Board {
    // current blocks
    private int[][] blocks = null;
    
    private int manhattan = -1;
    private int hamming = -1;
    
    private int dimension = -1;

    // construct a board from an N-by-N array of blocks
    // (where blocks[i][j] = block in row i, column j)
    public Board(int[][] blocks) {
        this.blocks = copy(blocks);
        //this.blocks = blocks;
        this.dimension = blocks.length;
    }

    // board dimension N
    public int dimension() {
        return dimension;
    }

    // number of blocks out of place
    public int hamming() {
        if (hamming == -1) {
            hamming = calcHamming();
        }
        return hamming;
    }
    
    private int calcHamming() {
        int wrong = 0;
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                if (blocks[i][j] != 0) {
                    int shouldbe = i * dimension + j + 1;
                    if (blocks[i][j] != shouldbe) {
                        wrong++;
                    }
                }
            }
        }
        return wrong;
    }

    // sum of Manhattan distances between blocks and goal
    public int manhattan() {
        if (manhattan == -1) {
            manhattan = calcManhattan();
        }
        return manhattan;
    }
    
    private int calcManhattan() {
        int distance = 0;
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                if (blocks[i][j] != 0) {
                    int shouldbe = i * dimension + j + 1;
                    if (blocks[i][j] != shouldbe) {
                        int y = (blocks[i][j] - 1) / dimension;
                        int x = (blocks[i][j] - 1) % dimension;
                        distance += Math.abs(i - y);
                        distance += Math.abs(j - x);
                    }
                }
            }
        }
        return distance;
    }
    // is this board the goal board?
    public boolean isGoal() {
        int boardsize = dimension * dimension;
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                if (blocks[i][j] != ((i * dimension + j + 1) % (boardsize))) {
                    return false;
                }
            }
        }
        return true;
    }

    // a board obtained by exchanging two adjacent blocks in the same row
    public Board twin() {
        int[][] c = copy();
        for (int i = 0; i < dimension; i++) {
            if (c[i][0] != 0 && c[i][1] != 0) {
                swap(c, i, 0, i, 1);
                break;
            }
        }
        return new Board(c);
    }

    // does this board equal y?
    public boolean equals(Object y) {
        if (y == null)
            return false;
        if (y == this)
            return true;
        if (y.getClass() != getClass())
            return false;
        Board that = (Board) y;
        if (that.dimension != dimension)
            return false;
        //return Arrays.deepEquals(that.blocks, blocks);
        
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                if (that.blocks[i][j] != blocks[i][j])
                    return false;
            }
        }
        return true;
        
    }

    // all neighboring boards
    public Iterable<Board> neighbors() {
        Set<Board> set = new HashSet<Board>();
        int i = -1, j = -1;
        search: {
            for (i = 0; i < dimension; i++) {
                for (j = 0; j < dimension; j++) {
                    if (blocks[i][j] == 0) {
                        break search;
                    }
                }
            }
        }
        // not top
        if (i != 0) {
            int[][] c = copy();
            swap(c, i, j, i - 1, j);
            set.add(new Board(c));
        }
        // not top
        if (i != dimension - 1) {
            int[][] c = copy();
            swap(c, i, j, i + 1, j);
            set.add(new Board(c));
        }
        // not left
        if (j != 0) {
            int[][] c = copy();
            swap(c, i, j, i, j - 1);
            set.add(new Board(c));
        }
        // not right
        if (j != dimension - 1) {
            int[][] c = copy();
            swap(c, i, j, i, j + 1);
            set.add(new Board(c));
        }
        return set;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(dimension + "\n");
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                s.append(String.format("%2d ", blocks[i][j]));
                //s.append(blocks[i][j]);
                //s.append(" ");
            }
            s.append("\n");
        }
        return s.toString();
    }

    private static void swap(int[][] b, int i1, int j1, int i2, int j2) {
        int tmp = b[i1][j1];
        b[i1][j1] = b[i2][j2];
        b[i2][j2] = tmp;
    }

    private static int[][] copy(int[][] src) {
        int rows = src.length;
        int cols = src[0].length;
        int[][] copyBlocks = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                copyBlocks[i][j] = src[i][j];
            }
        }
        return copyBlocks;
    }

    private int[][] copy() {
        return copy(blocks);
    }
}
