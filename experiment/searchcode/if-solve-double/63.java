import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Random;

/** A simple n-sudoku solver, hard to find solutions for > 16 
*	We don't care about the testability etc, it's just a performance exercise
*/
final class Sudoku {

	private static final int MinSize = 9;
	
	private final AtomicBoolean solutionFound;
	private final int size;
	private final int rootSize;
	private final int threadCount;
	
	/** Create a Sudoku object for the given size and number of threads */
	Sudoku(final int size, final int threadCount) {
		if (threadCount < 1 || size < MinSize)
			throw new IllegalArgumentException("Bad arguments to Sudoku");
			
		this.size = size;
		this.rootSize = (int)Math.sqrt(size);
		this.threadCount = threadCount;
		this.solutionFound = new AtomicBoolean(false);
	}

	/** The main solver, to be run by multiple threads */
	private final class Solver implements Runnable {
		final int[][] values;
		final int threadId;
		final Random rand;
	
		Solver(final int[][] values, final int threadId) {
			this.values = values;
			this.threadId = threadId;
			this.rand = new Random();
		}
		
		public void run() {
			solve(0, 0);
		}
		
		private boolean solve(int row, int col) {
			if (col == size) {
				++row;
				col = 0;
			}
			if (row == size)
				return true;

			final boolean[] valids = calcValidNumbers(row, col);
			final int[] order = calcOrder(valids);
			for (int i = 0; i < order[size] && !solutionFound.get(); ++i) {
				if (!valids[order[i]]) {
					values[row][col] = order[i]+1;
					if (solve(row, col+1)) {
						if (!solutionFound.getAndSet(true))
							printSolution();
						return true;
					}					
				}
			} 
			values[row][col] = 0;
			return false;
		}
		
		//get the valid numbers for this position
		private boolean[] calcValidNumbers(final int row, final int col) {
			boolean[] valids = new boolean[size];
		
			for (int i = 0; i < size; ++i) {
				if (values[row][i] > 0)
					valids[values[row][i]-1] = true; //default init is false
				if (values[i][col] > 0)
					valids[values[i][col]-1] = true;
			}
			
			int blockStartRow = (row/rootSize) * rootSize;
			int blockStartCol = (col/rootSize) * rootSize;
			for (int r = blockStartRow; r < blockStartRow+rootSize; ++r) {
				for (int c = blockStartCol; c < blockStartCol+rootSize; ++c) {
					if (values[r][c] > 0)
						valids[values[r][c]-1] = true;
				}
			}		
			return valids;
		}
		
		//randomizing order makes it less likely to get stuck
		private int[] calcOrder(final boolean[] valids) {
			final int[] order = new int[size+1];
		
			int validCount = 0;
			for (int i = 0; i < size; ++i)
				if (!valids[i])
					order[validCount++] = i;
			
			order[size] = validCount;
			for (int i = 0; i < validCount; ++i) {
				int pos1 = rand.nextInt(validCount);
				int pos2 = rand.nextInt(validCount);
				int tmp = order[pos1];
				order[pos1] = order[pos1];
				order[pos2] = tmp;
			}
			return order;				
		}
		
		private void printSolution() {
			final int padBase = 1+size/10;
			for (int r = 0; r < size; ++r) {
				for (int c = 0; c < size; ++c) {
					System.out.print(values[r][c]);
					for (int i = 0, spaces = padBase-values[r][c]/10; i < spaces; ++i)
						System.out.print(" "); //align nicely, tabs are too big
				}
				System.out.println();
			}
		}
	}
	
	static void printUsageMessage() {
		System.out.println("Usage: java Sudoku [integer > " + MinSize + " with integer square root]");
	}
	
	public static void main(final String[] args) {
		int size = MinSize;
		int threadCount = Runtime.getRuntime().availableProcessors()-1; //leave one core for garbage collector;
		boolean error = false;
		if (args.length > 0) {
			try {
				size = Integer.parseInt(args[0]);
				final double root = Math.sqrt(size);
				if (Math.ceil(root) != Math.floor(root) || size < MinSize)
					error = true;
				if (args.length > 1) {
					threadCount = Integer.parseInt(args[1]);
					if (threadCount < 1)
						error = true;
				}
			}
			catch (NumberFormatException e) {
				error = true;
			}
		}
		
		if (error) {
			Sudoku.printUsageMessage();
			System.exit(1);
			return; //just in case exit doesn't work
		}

		final Sudoku sudoku = new Sudoku(size, threadCount);
		final Thread[] threads = new Thread[sudoku.threadCount];
		for (int t = 0; t < threadCount; ++t) {
			threads[t] = new Thread(sudoku.new Solver(new int[size][size], t));
			threads[t].start();
		}
		
		try {
			for (int t = 0; t < threadCount; ++t)
				threads[t].join();
		}
		catch (InterruptedException e) {} //we don't care
		
		if (!sudoku.solutionFound.get())
			System.out.println("No solution found");
	}
}
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
