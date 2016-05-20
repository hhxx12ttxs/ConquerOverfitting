package fission;

import java.util.Random;

public class TestMain {
	public static Random R = new Random();
	
	public static void main(String[] args) {
		double[] vals = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
		int ngames = 100; // # of random boards to play games on - actual number of games played will be twice this
		boolean[][][] boards = genBoards(20, 2, ngames);
		String ap = "Random", bp = "OB1";
		int expand = 10, step = 1, depth = 5;
		long timeout = 50000000l;
		
		// NOTE: you can use R.nextInt(number) to randomise the number of starting pieces if you want to.
		
		for (int index = 0; index < vals.length; ++index) {
			int a = 0, b = 0, error = 0;
			for (int count = 0; count < ngames; ++count) {
				Pair res = test(boards[count], ap, bp, vals[index], expand, step, depth, timeout);//500l);
				//int res = test(5, 2, "Random", "OB1", 0.5, 10, 100, 5, false, 6000000000l);
				if (0 == res.a) { ++a; }
				else if (1 == res.a) { ++b; }
				else if (-1 == res.a) { ++error; }
				else { System.err.println("Bogus return code - wth???"); }
				if (1 == res.b) { ++b; }
				else if (0 == res.b) { ++a; }
				else if (-1 == res.b) { ++error; }
				else { System.err.println("Bogus return code - wth???"); }
				System.out.print(count + ", ");
			}
			System.out.println("\n" + ap + ":" + bp + ":Error " + a + ":" + b + ":" + error + ", C=" + vals[index] + ", EX=" + expand);
		}
	}
	
	public static Pair test(boolean[][] board, String playerA, String playerB, double cval, int expand, int step, int depth, long wait) {
		Pair result = new Pair();
		result.a = play(board, playerA, playerB, true, cval, expand, step, depth, wait);
		result.b = play(board, playerA, playerB, false, cval, expand, step, depth, wait);
		return result;
	}
	
	public static long toMilliSeconds(long nano) {
		return (long) (nano / 1000000.0d);
	}
	
	public static long toNanoSeconds(long ms) {
		return ms * 1000000l;
	}
	
	public static int play(boolean[][] board, String playerA, String playerB, boolean aStarts, double cval, int expand, int step, int depth, long wait) {
		FissionGame g = new FissionGame(copyBoard(board));
		FissionPlayer a, b, currentPlayer;
		
		//System.out.println(g);
		//System.out.println();
		
		if (aStarts) {
			a = initPlayer(playerA, Fission.VERTICAL);
			b = initPlayer(playerB, Fission.HORIZONTAL);
			currentPlayer = a;
		} else {
			a = initPlayer(playerA, Fission.HORIZONTAL);
			b = initPlayer(playerB, Fission.VERTICAL);
			currentPlayer = b;
		}
		
		FissionPlayerOB.MAXDEPTH = depth;
		
		FissionPlayerMCTS.STEP = step;
		FissionPlayerMCTS.WAIT = (long)(wait * 0.9d); // only for manual timer
		OBMCTSGame.C = cval; // will need changing if we want to play it against itself with different values
		OBMCTSGame.EXPAND = expand; // will need changing if we want to play it against itself with different values

		try {
			while (true) {
				Thread t = new Thread(new FissionMoveGenerator(g, currentPlayer));
				t.start();
				t.join(toMilliSeconds(wait));
				t.interrupt();
				FissionMove m = currentPlayer.getMove();
				if (!g.isValidMove(m)) {
					//System.out.println("The winner is: " + ((currentPlayer == a) ? b : a));
					return (currentPlayer == b) ? 0 : 1; // 0 if a won, 1 if b won, -1 if something broke
				}
				//System.out.println(m);
				g.doMove(m);
				//System.out.println(m);
				//System.out.println(g);
				//System.out.println();
				currentPlayer = (currentPlayer == a) ? b : a; 
			}
		} catch (InterruptedException ie) {
			System.out.println("Main thread interrupted!");
		}
		return -1;
	}
	
	public static FissionPlayer initPlayer(String name, boolean type) {
		if ("OB1".equals(name)) return new FissionPlayerMCTS(name, type);
		if ("OB-N".equals(name)) return new FissionPlayerOB(name, type);
		if ("Random".equals(name)) return new RandomPlayer(name, type);
		return new RandomPlayer(name + "default", type);
	}
	
	public static boolean[][] copyBoard(boolean[][] board) {
    		boolean copy[][] = new boolean[board.length][board[0].length];
    		for(int r = 0; r < board.length; r++)
    			System.arraycopy(board[r], 0, copy[r], 0, board[0].length);
    		return copy;
	}
	
	public static boolean[][][] genBoards(int boardSize, int pieceCount, int nGames) {
		boolean[][][] boards = new boolean[nGames][][];
		
		for (int count = 0; count < nGames; ++count) {
			boards[count] = randomBoard(boardSize, pieceCount);
		}
		
		return boards;
	}
	
	public static boolean[][] randomBoard(int boardSize, int pieceCount) { // won't * and P games with only 1 or two moves, e.g. edges, cause problems?
		if (pieceCount > (boardSize * boardSize)) { System.err.println("Too many pieces!"); System.exit(0);}
		boolean[][] board = new boolean[boardSize][boardSize];
		int count = 0;
		while (count < pieceCount) {
			//int a = R.nextInt(boardSize);
			//int b = R.nextInt(boardSize);
			int a = 1 + R.nextInt(boardSize - 2);
			int b = 1 + R.nextInt(boardSize - 2);
			if (board[a][b] != Fission.STONE) {
				if (a == b || ((a + b + 1) == boardSize)) { // diagonals
					//System.out.println("Diag: (" + a + "," + b + ")");
					board[a][b] = Fission.STONE;
					++count;
				} else { // keep it symmetric
					//System.out.println("Sym: (" + a + "," + b + ")");
					board[a][b] = Fission.STONE;
					board[b][a] = Fission.STONE;
					count += 2;
				}
			}
		}
		return board;
	}
	
	public static class Pair {
		int a, b;
		public Pair() { a = -1; b = -1; }
	}
}
