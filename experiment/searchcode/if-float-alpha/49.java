package Quoridor;

	/**
	 * Time limited Smart AI search class using Alpha-Beta NegaMax search algorithm
	 * @author Daniel Murphy, Samuel Swiss & Sahil Kapoor
	 */

public class TimeLimitedAIPlayer extends AIPlayer {
	final long maxMs;
	
	long startTime;
	
	/**
	 * A time limited version of the NegaMax algorithm, looks ahead at future moves on 
	 * the search tree, starting at depth 1 and increasing until the time limit is reached.
	 * This way, every immediate possible move will be evaluated at similar depth, returning 
	 * a better move the longer the algorithm is allowed to run. 
	 * @param maxSeconds
	 */
	public TimeLimitedAIPlayer(int maxSeconds) {
		if (maxSeconds > 0) {
			this.maxMs = maxSeconds * 1000;
		} else {
			this.maxMs = 0;
		}
	}
	
	/**
	 * For time limited SmartAI searches, the AI will only search for 
	 * the specified amount of time.
	 * @return Run Time of SmartAI
	 */
	private boolean outOfTime() {
		return maxMs != 0 && System.currentTimeMillis() - startTime >= maxMs;
	}
	
	
	/**
	 * Implements an iterative version of the NegaMax search algorithm which traverses
	 * the search tree by running NegaMax at increasing depths until the time limit is reached.
	 * @param board
	 * @param playerState
	 * @param otherState
	 * @return bestMove
	 */
	@Override
	public Move generateMove(BoardState board, PlayerState playerState, PlayerState otherState) {
			float alpha = Float.NEGATIVE_INFINITY;
			float beta = Float.POSITIVE_INFINITY;
			Move bestMove = null;
			int maxDepth = 1;
			startTime = System.currentTimeMillis();

			while (!outOfTime()) {
				for (Move m: board.allPossibleMoves(playerState)) {
					board.applyMove(m, playerState);
					float score = -iterativeNegaMax(board, otherState, playerState, -beta, -alpha, maxDepth);
					board.undoMove(1, playerState, otherState);
					
					if (score > alpha) {
						alpha = score;
						bestMove = m;
					}
				}
				maxDepth++;
			}
		
		return bestMove;
	}

	/**
	 * The NegaMax algorithm, slightly modified to look at future possible moves 
	 * until the time limit is reached.
	 * @param currState
	 * @param thisPlayer
	 * @param otherPlayer
	 * @param alpha
	 * @param beta
	 * @param maxDepth
	 * @return
	 */
	private float iterativeNegaMax(BoardState currState, PlayerState thisPlayer, PlayerState otherPlayer, float alpha, float beta, int maxDepth) {
		float score;
		
		for (Move m : currState.allPossibleMoves(thisPlayer)) {
			if (maxDepth == 0 || currState.isOver() || outOfTime()) {
				return -currState.getCost(thisPlayer, otherPlayer);
				
			}
			
			currState.applyMove(m, thisPlayer);
			score = -iterativeNegaMax(currState, otherPlayer, thisPlayer, -beta, -alpha, maxDepth - 1);
			currState.undoMove(1, thisPlayer, otherPlayer);
			
			if (score > alpha) {
				alpha = score;
				
				if (alpha >= beta) {
					break;
				}
			}
		}
		
		return alpha;
	}
	
	/**
	 * Returns what the SmartAI search is limited by; 
	 * in this case time
	 * @return Name of AI
	 */
	@Override
	public String getName() {
		return String.format("smartai time %d", maxMs);
	}

}

