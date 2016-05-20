package fission;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @authors okey, Bobby
 */
public class FissionPlayerMCTS extends FissionPlayer {
	//private static final int MAX_SIMS = 100;
	//private static final int STEP = 10;
	static int MAX_SIMS = 100;
	static int STEP = 100;
	private OBMCTSGame mcts = new OBMCTSGame();
	private boolean type; // Because MA made the datafield that does this private, and I don't want to use an accessor all the time...
	private ArrayList<FissionMove> moves = new ArrayList<FissionMove>();
	int total = 0;
	//private final long WAIT = 5900000000l;//490000000l;//5900000000l;
	static long WAIT = 5900000000l;
	private long startTime;
	
	public FissionPlayerMCTS(String name, boolean moveType) {
		super(name, moveType);
		mcts.setPlayer(moveType);
		type = moveType;
	}
	
	@Override
	public void generateMove(FissionGame g) throws InterruptedException {
		startTime = System.nanoTime();
		/*if (null == pieces) { // can probably use this condition to assume !(1 < moves.size())
			mcts.loadPieces(g);
		} else {
			moves = g.getMoves();
			if (null != move) {
				// make our previous move
			}
			// make the opponent's previous move
		}*/
		//mcts.asCopyOf(g);
		moves = g.getMoves();
		// assume the game is never in a messed up state where we end up playing twice sequentially or something
		//mcts.setPlayer(type);
		if (1 < moves.size()) {
			synchronized (mcts.board) {
			FissionMove umove = moves.get(moves.size()-2);
			mcts.update(umove); // make our previous move
			//mcts.setPlayer(!type);
			FissionMove omove = moves.get(moves.size()-1);
			//if (!mcts.isValidMove(omove)) System.out.println(omove);
			assert(mcts.isValidMove(omove));
			mcts.update(omove); // make the opponent's previous move
			}
		} else { mcts.asCopyOf(g); } // initialise
		//mcts.setPlayer(type);

		//System.out.println("S:" + mcts.root);
		move = null;
		total = 0;
		//System.out.println(move);
		FissionMove maybe = null;
		long currentDelta = 0;
		while (true) {
		//for (int i = STEP; i < MAX_SIMS+1; i += STEP) {
			//System.out.println("!");
			maybe = mcts.search(STEP);
			//total += MAX_SIMS;
			//total = i;
			currentDelta = System.nanoTime() - startTime;
			//if (g.isValidMove(maybe)) { move = maybe; }
			//else { throw new InterruptedException();}
			if (WAIT > currentDelta) { move = maybe; }
			else { throw new InterruptedException();}
		}
		//}
		//System.out.println("B:" + mcts.root);
		//}
		//throw new InterruptedException();
    }
	
	@Override
	public String makeComment() { return "..."; }

	@Override
    public int getEvaluation() { return 0; }
	
	public int getT() {return total;}
	
	private Integer hash(int r, int c) { return new Integer((r << 19) + c); } // FissionMove.hashCode() >>> 1
	
	
	/*public void loadPieces(FissionGame g) {
		pieces = new HashMap<Integer, Integer[]>();
		boolean[][] board = g.getBoard();
		for (int r = 0; r < board.length; ++r) {
			for (int c = 0; c < board[0].length; ++c) {
				Integer[] p = {new Integer(r), new Integer(c)};
				pieces.put(hash(r, c), p);
			}
		}
	}*/
}

