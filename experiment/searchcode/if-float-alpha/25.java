package Quoridor;

	/**
	 * Depth limited Smart AI search class using Alpha-Beta NegaMax search algorithm
	 * @author Daniel Murphy, Samuel Swiss & Sahil Kapoor
	 */
public class DepthLimitedAIPlayer extends AIPlayer {
	final long maxDepth;
	
	/**
	 * The SmartAI player uses a special search algorithm called NegaMax, 
	 * a modified version of the Alpha-Beta MiniMax search algorithm which 
	 * looks at future possible moves to determine the maximum benefiting move
	 * to make in a game. The algorithm returns the negative maximum of the opposing
	 * players moves to find which of the opponents moves will benefit the current 
	 * player's moves. By comparing all of these potential moves the best move can 
	 * be found.
	 * @param maxDepth
	 */
	public DepthLimitedAIPlayer (int maxDepth) {
		
		if (maxDepth > 0) {
			this.maxDepth = maxDepth;
		} else {
			this.maxDepth = 3;
		}
	}
	
	/**
	 * Calculates the best move using the NegaMax search algorithm, 
	 * a type of MiniMax search algorithm which allows the AI to 
	 * "look ahead" a certain number of moves to determine which 
	 * move it should take. This search can be limited by how far 
	 * ahead it's allowed to look.
	 * @param BoardState
	 * @param PlayerState 
	 * @param PlayerState
	 * @return bestMove
	 */
	public Move generateMove (BoardState board, PlayerState playerState, PlayerState otherState) {
		
		Move bestMove = null;
		float alpha = Float.NEGATIVE_INFINITY;
		float beta = Float.POSITIVE_INFINITY;
		
		for(Move m : board.allPossibleMoves(playerState)) {
			
			//System.err.format("a:%s m:%s best:%s\n", alpha, m, bestMove);
			
			board.applyMove(m, playerState);
			float score = -negaMax(otherState, playerState, board, 1, -beta, -alpha);
			board.undoMove(1, playerState, otherState);
			
			if (score > alpha || alpha == Float.NEGATIVE_INFINITY) {
				alpha = score;
				bestMove = m;
			}
		}

		//System.err.format("picked %s->%s\n", board.playerPosition(playerState.playerNum()), bestMove);
		//System.err.flush();
		
		return bestMove;
	}
	
	/**
	 * The recursively repeating NegaMax search algorithm which 
	 * repeatedly looks at possible future moves and evaluates 
	 * the best move which will give the maximum benefit.
	 * @param thisPlayer
	 * @param otherPlayer
	 * @param currState
	 * @param currDepth
	 * @param alpha
	 * @param beta
	 * @return alpha
	 */
	public float negaMax(PlayerState thisPlayer, PlayerState otherPlayer, BoardState currState, int currDepth, float alpha, float beta) {
		float score;
		
		if (currState.isOver() || currDepth >= maxDepth) {
			return -currState.getCost(thisPlayer, otherPlayer);
		}
		
		for (Move m : currState.allPossibleMoves(thisPlayer)) {
			
			currState.applyMove(m, thisPlayer);
			score = -negaMax(otherPlayer, thisPlayer, currState, currDepth + 1, -beta, -alpha);
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
	 * in this case depth
	 * @return Name of AI
	 */
	public String getName() {
		return String.format("smartai depth %d", maxDepth); 
	}
}

