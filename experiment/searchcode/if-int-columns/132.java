package fission;

public class OBMCTSGame {
	
	//private final double C = 0.5;
	//private final int EXPAND = 10;
	static double C = 0.5;
	static int EXPAND = 10;
	private boolean oplayer;
	private OBNode root = null;
	
	volatile boolean[][] board;
    private int rows;
    private int columns;
	
    public OBMCTSGame() {}
    
	public FissionMove search(int maxSimulations) {
		OBMCTSGame copy = new OBMCTSGame();
		for (int count = 0; count < maxSimulations; ++count) {
			copy.asCopyOf(this);
			//System.out.println("+" + root);
			copy.simulate(root);
		}
		OBNode n = getBest(root);
		
		//System.out.println(root);
		if (null == n) return null;
		return new FissionMove(n.row, n.col, n.player);
	}
	
	private OBNode select(OBNode n) {
		OBNode result = null;
		OBNode next = n.child;
		double bestUCT = -1.0;
		
		while (null != next) {
			double uct = 0.0;
			if (0 < next.visitCount) {
				uct = next.averagePayoff() + C * Math.sqrt((2 * Math.log(n.visitCount)) / next.visitCount);
			} else {
				uct = 10000 + 1000 * Math.random(); // WTF? why such a large random value? // Seems like it would heavily bias us towards expanding nodes below the count... is this appropriate?
			}
			if (bestUCT < uct) {
				bestUCT = uct;
				result = next;
			}
			next = next.sibling;
		}
		//System.out.println("-" + n);
		//System.out.println("!" + result);
		return result;
	}
	
	private int simulate(OBNode n) {
		int random = 0;
		//System.out.println("simFission...");
		if (null == n.child && EXPAND > n.visitCount) {
			random = randomGame(!n.player);
		} else {
			if (null == n.child) { expand(n); }
			OBNode next = select(n);
			//System.out.println("trying: " + new FissionMove(next.row, next.col, player));
			if (null == next) {
				/*
				System.out.println("player:"+player);
				System.out.println(this);
				System.out.println("null expansion:" + root);
				System.out.println(n);
				*/
				return 0;
			}
			assert(null != next); /* Should never happen - remove for optimisation purposes later ## */
			doMove(new FissionMove(next.row, next.col, next.player));
			int result = simulate(next);
			random = 1 - result;
		}
		
		n.visit(1 - random);
		return random;
	}
	
	private int randomGame(boolean player) {
		Boolean winner = null; // NULL hack
		boolean currentPlayer = player;
		
		//System.out.println("Beginning simulation for " + player);
		while (null == winner) {
			winner = randomMove(currentPlayer);
			currentPlayer = !currentPlayer;
		}
		return (player == winner) ? 1 : 0; // loss
	}
	
	private Boolean randomMove(boolean player) {
		int possibleMoves = 0;
        FissionMove result = null;
        
        for(int r = 0; r < rows; r++) { /* Definitely not the best way to do this... */
            for(int c = 0; c < columns; c++) {
                FissionMove m = new FissionMove(r, c, player);
                if (isValidMove(m)) {
                    possibleMoves++;
                    if (Math.random() * possibleMoves < 1.0) { result = m; break; }
                }
            }
        }
        if (null != result) {
        	doMove(result);
        	return null;
        } else { return new Boolean(!player); }
        	
	}
	
	private void expand(OBNode n) {
		OBNode last = n;

		for (int r = 0; r < rows; ++r) { /* WE CAN DO THIS FASTER IF WE KEEP TRACK OF PIECES!! */
			for (int c = 0; c < columns; ++c) {
				FissionMove move = new FissionMove(r, c, !n.player);
				if (isValidMove(move)) {
					//System.out.println("creating child for player " + player + ":[" + r + ", " + c + "]");
					OBNode child = new OBNode(r, c, !n.player);
					if (last == n) { last.child = child; }
					else { last.sibling = child; }
					last = child;
				}
			}
		}
	}

	private OBNode getBest(OBNode node) {
		OBNode child = node.child;
		OBNode best = null;
		int bestC = -1;
		
		while (child != null) {
			if (bestC < child.visitCount) {
				best = child;
				bestC = child.visitCount;
			}
			child = child.sibling;
		}
		
		return best;
	}
	
	public boolean isValidMove(FissionMove move) {
		synchronized (board) {
			return
			move != null &&
			move.getRow() - move.dRow() >= 0 &&
			move.getRow() + move.dRow()  < rows &&
			move.getColumn() - move.dColumn() >= 0 &&
			move.getColumn() + move.dColumn() < columns &&
			board[move.getRow()][move.getColumn()] == Fission.STONE &&
			board[move.getRow() + move.dRow()][move.getColumn() + move.dColumn()] == Fission.SPACE &&
			board[move.getRow() - move.dRow()][move.getColumn() - move.dColumn()] == Fission.SPACE
			;
		}
    }
    
    private boolean doMove(FissionMove move)  {
    	synchronized (board) {
    		if (!isValidMove(move)) {System.out.println("invalid:" + move);}
    		assert(isValidMove(move)); // unnecessary

    		int r = move.getRow(); int dr = move.dRow();
    		int c = move.getColumn(); int dc = move.dColumn();
    		board[r][c] = Fission.SPACE;
    		board[r+dr][c+dc] = Fission.STONE;
    		board[r-dr][c-dc] = Fission.STONE;
    	}
        return true;
    }
	
    private boolean[][] copyBoard() {
    	synchronized (board) {
    		boolean copy[][] = new boolean[rows][columns];
    		for(int r = 0; r < rows; r++)
    			System.arraycopy(board[r], 0, copy[r], 0, columns);
    		return copy;
    	}
	}
    
	private void asCopyOf(OBMCTSGame g) {
		synchronized (g.board) {
			this.board = g.copyBoard();
			rows = board.length;
			columns = board[0].length;
			this.oplayer = g.oplayer;
		}
	}

	public void asCopyOf(FissionGame g) {
		this.board = g.getBoard();
		rows = board.length;
		columns = board[0].length;
		root = new OBNode(-1, -1, oplayer);
		expand(root);
	}

    public void setPlayer(boolean player) { this.oplayer = !player; } // Internal root is !US because means the doMove calls make more sense
    
    public void update(FissionMove move) {
    	assert(null != root);
    	assert(null != move);
    	// Update the board state; do this first because we'll never make a move that we haven't expanded, but they might, which would trigger re-initialising the root, which must be done from our POV or things will break
    	doMove(move);
    	// If we already have the nodes for the move, keep them, else start from scratch
    	OBNode newState = findChild(root, move.getRow(), move.getColumn());
    	if (null == newState) {
    		root = new OBNode(-1, -1, oplayer);
    		expand(root);
    	} else {
    		root = newState;
    		root.sibling = null;
    		root.row = -1; root.col = -1;
    		if (null == root.child) { expand(root); }
    	}
    }
    
    private OBNode findChild(OBNode parent, int r, int c) {
    	OBNode next = parent.child;
    	OBNode target = null;
    	while (null != next) {
    		if (r == next.row && c == next.col) { target = next; return target; }
    		next = next.sibling;
    	}
    	return target;
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for(int r = board.length-1; r >= 0; r--) {
            for(int c = 0; c < board[r].length; c++) {
                if (board[r][c] == Fission.STONE) {
                    result.append('x');
                } else {
                    result.append('.');
                }
            }
            result.append('\n');
        }
        return result.toString();
    }
}

