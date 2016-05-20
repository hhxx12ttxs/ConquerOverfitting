package ai;

import game.Board;
import game.Movement;
import game.Player;
import game.Strategy;

public class TBIDLMinimax implements Minimax {

	/*
	 * Time Bound, Iterative Deepening, Alpha-Beta Pruning MiniMax We've created
	 * a monster.
	 */

	private Movement solution;
	private Strategy strategy;
	private Board board;
	private Player maximizer;
	private Player minimizer;
	private boolean hasTime = true;
	private int millis;
	private int states = 0;
	private long runtime = -3;
	private int maxlvl = 0;
	

	private Thread worker;
	private Thread clock;
	private LMMWorker task;

	public TBIDLMinimax(int millis, Strategy strategy, Board board,
			Player maximizer, Player minimizer) {
		this.millis = millis;
		this.strategy = strategy;
		this.board = board;
		this.maximizer = maximizer;
		this.minimizer = minimizer;
	}

	private synchronized void postSolution(Movement move) {
		if (move != null) {
			this.solution = move;
			states += task.exploredStates();
			if (maxlvl < task.getHeight()){
				maxlvl = task.getHeight();
			}
		}
		this.notify();
	}

	private synchronized void stopTime() {
		this.hasTime = false;
		task.poison();
		worker.interrupt();
		this.notify();
	}

	private class LMMWorker extends LMinimax implements Runnable {

		public LMMWorker(int levels, Strategy strategy, Board board,
				Player maximizer, Player minimizer) {
			super(levels, strategy, board, maximizer, minimizer);
		}

		public void run() {
			postSolution(getBestMove());
		}

	}

	private class Clock extends Thread implements Runnable {

		private int millis;

		public Clock(int millis) {
			this.millis = millis;
		}

		@Override
		public void run() {
			try {
				Clock.sleep(millis);
				stopTime();
			} catch (InterruptedException e) {
			}
		}

	}

	public synchronized Movement getBestMove() {
		/*
		 * We spawn two threads: - Ye Olde ABPMinimax (with a custom height
		 * progression). - An alarm thread.
		 * 
		 * When the alarm thread rings it signals this thread, which promptly
		 * kills the ABPMinimax worker (which is a service thread, unjoined).
		 * 
		 * Every time the worker returns it sets a solution (Movement) in this
		 * thread. This happens inside a while(flag) loop that increments the
		 * height for each run.
		 */
		int level = 1;

		hasTime = true;
		solution = null;
		clock = new Clock(millis);
		clock.start();
		long start = System.currentTimeMillis();
		
		try {
			while (hasTime) {
				task = new LMMWorker(level, strategy, board, maximizer,
						minimizer);
				worker = new Thread(task);
				worker.setDaemon(true);
				worker.start();
				this.wait();
				level++;
			}
			clock.join();
		} catch (InterruptedException e) {
		}
		if (solution == null && board.hasAvailableMoves(maximizer)) {
			throw new MinimaxTimeoutException(millis, board, level);
		}
		runtime = (System.currentTimeMillis() - start);
		return solution;
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	public int getHeight() {
		return maxlvl;
	}

	public int exploredStates() {
		return states;
	}

	public long runTime() {
		return runtime;
	}
	
	public String getName() {
		return "Time Bound Iterative Deepening Level Minimax";
	}
}

