import java.util.Arrays;


/**
 * A program for solving the n-queens problem, using backtracking.
 * 
 * This works smoothly up until around n = 29 at which point the combinatorial
 * explosion takes over.
 * 
 * @author paulthoren
 * 
 */
public class NQueenSolver {
	
	public static void main(String[] args) {
		NQueenSolver.findSolution(28);
	}

	/**
	 * Find a solution to the n-queens problem for a given n, if it
	 * exists.
	 * 
	 * @param dimension
	 */
	public static void findSolution(int dimension) {
		int[][] board = NQueenSolver.initBoard(dimension);

		if (placeQueen(0, board)) {
			NQueenSolver.printBoard(board);
		} else {
			System.out.println("no solution");
		}
	}
	
	private static int[][] initBoard(int dimension) {
		int[][] board = new int[dimension][dimension];
		
		for (int[] i : board) {
			Arrays.fill(i, 0);
		}
		
		return board;
	}

	private static boolean placeQueen(int column, int[][] board) {		
		for (int row = 0; row < board.length; row++) {
			if (NQueenSolver.canPlace(row, column, board)) {
				board[row][column] = 1;

				if (column == board[0].length - 1) {
					// done!
					return true;
				} else {
					// place the next queen over
					if (placeQueen(column + 1, board)) {
						return true;
					} else {
						// rollback
						board[row][column] = 0;
					}
				}
			}
		}

		//couldn't place anywhere in the column.
		return false;
	}

	private static boolean canPlace(int row, int column, int[][] board) {
		// check column
		for (int i = 0; i < board[0].length; i++) {
			if (board[row][i] == 1)
				return false;
		}

		// check bottom left diagonal
		int i = row;
		int j = column;
		while (i > 0 && j > 0) {
			if (board[--i][--j] == 1)
				return false;
		}

		// check top left diagonal
		i = row;
		j = column;
		while (i < board.length - 1 && j > 0) {
			if (board[++i][--j] == 1)
				return false;
		}

		return true;
	}

	private static void printBoard(int[][] board) {
		StringBuilder builder = new StringBuilder();

		for (int row = 0; row < board.length; row++) {
			for (int column = 0; column < board[0].length; column++) {
				builder.append(board[row][column] + " ");
			}
			builder.append("\n");
		}

		System.out.println(builder.toString());
	}
}

