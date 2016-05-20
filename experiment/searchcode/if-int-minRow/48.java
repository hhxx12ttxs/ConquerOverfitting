package abstracts;

import model.*;
import view.*;

public abstract class ControllerBoard {

	protected Game game;
	protected Board board;
	protected BoardView boardView;
	
	public final static int MIN_SCORE = -1000;

	/**
	 * constructor: link to the game model, create a new board
	 * @param game		(Game) game model to link
	 * @param height	(int) height of new board
	 * @param width		(int) width of new board
	 */
	public ControllerBoard(Game game, int height, int width) {
		this.game = game;
		this.board = new Board(height, width);
		this.boardView = new BoardView(this);
		this.board.addView(boardView);
	} // public ControllerBoard(Game game, int height, int width)
	
	/**
	 * Get handle of game model
	 * @return	(Game) handle of game model
	 */
	public Game getGameModel() {
		return this.game;
	} // public Game getGameModel()
	
	/**
	 * Get handle of board view model
	 * @return	(BoardView) handle of game model
	 */
	public BoardView getBoardView() {
		return this.boardView;
	} // public BoardView getBoardView()
	
	/**
	 * Get height of the board
	 * @return	(int) height of the board
	 */
	public int getBoardHeight() {
		return this.board.getHeight();
	} // public int getBoardHeight()
	
	/**
	 * Get width of the board
	 * @return	(int) width of the board
	 */
	public int getBoardWidth() {
		return this.board.getWidth();
	} // public int getBoardWidth()
	
	/**
	 * Get status of a grid
	 * @param	row		(int) row number of the grid
	 * @param	column	(int) column number of the grid
	 * @return			(int) current status of the grid
	 */
	public int getGridStatus(int row, int column) {
		return this.board.getGridStatus(row, column);
	} // public int getGridStatus(int row, int column)
	
	/**
	 * Add a new drop to the board
	 * @param	playerID	(int) current player's ID
	 * @param	column		(int) column number to drop
	 */
	public boolean addNewDrop(int playerID, int column) {
		if ((column < 0) || (column > this.board.getWidth() - 1))
			return false;
		if (playerID == 0)
			return false;
		boolean temp = this.board.setNewDropTo(column, playerID);
		return temp;
	} // public void addNewDrop(int playerID, int column)
	
	/**
	 * Set the board to a specific size
	 * @param	boardHeight	(int) new height of the board
	 * @param	boardWidth	(int) new widht of the board
	 */
	public void setBoardSize(int boardHeight, int boardWidth) {
		this.board.setBoardSize(boardHeight, boardWidth);
		
		// maybe this line is useless if we don't change 
		// board size after the game window is displayed
		this.boardView.update(this.board);
	} // public void setBoardSize(int boardHeight, int boardWidth)
	
	/**
	 * Check the chess result
	 * @param player1ID	(int) ID of player 1
	 * @param player2ID	(int) ID of player 2
	 * @return	(String) capital sting result of the game
	 * 					  - "WIN": current player wins;
	 * 					  - "TIE": no winner after board is full;
	 * 					  - null:  game not finished.
	 */
	public abstract String getChessResult(int player1ID, int player2ID);
	
	/**
	 * Find out if match the win scenario
	 * @param	win1	(int) 1st piece for win scenario
	 * @param	win2	(int) 2nd piece for win scenario
	 * @param	win3	(int) 3rd piece for win scenario
	 * @param	win4	(int) 4th piece for win scenario
	 * @return			(boolean) whether match win scenario
	 */
	public boolean matchWinScenario(int win1, int win2, int win3, int win4) {
		int height = this.board.getHeight();
		int width = this.board.getWidth();
	
		// check for the width direction
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width - 3; j++) {
				if ((board.getGridStatus(i, j) == win1) &&
					(board.getGridStatus(i, j+1) == win2) &&
					(board.getGridStatus(i, j+2) == win3) &&
					(board.getGridStatus(i, j+3) == win4))
					return true;
			} // for - for

		// check for winner: the height direction
		for (int i = 0; i < height - 3; i++)
			for (int j = 0; j < width; j++) {
				if ((board.getGridStatus(i, j) == win1) &&
					(board.getGridStatus(i+1, j) == win2) &&
					(board.getGridStatus(i+2, j) == win3) &&
					(board.getGridStatus(i+3, j) == win4))
					return true;
			} // for - for

		// check for winner: the diagonal direction (left-top to right-bottom)
		for (int i = 0; i < height - 3; i++)
			for (int j = 0; j < width - 3; j++) {
				if ((board.getGridStatus(i, j) == win1) &&
					(board.getGridStatus(i+1, j+1) == win2) &&
					(board.getGridStatus(i+2, j+2) == win3) &&
					(board.getGridStatus(i+3, j+3) == win4))
					return true;
			} // for - for

		// check for winner: the diagonal direction (left-bottom to right-top)
		for (int i = 3; i < height; i++)
			for (int j = 0; j < width - 3; j++) {
				if ((board.getGridStatus(i, j) == win1) &&
					(board.getGridStatus(i-1, j+1) == win2) &&
					(board.getGridStatus(i-2, j+2) == win3) &&
					(board.getGridStatus(i-3, j+3) == win4))
					return true;
			} // for - for
		
		return false;
	} // public boolean matchWinScenario(int win1, int win2, int win3, int win4)
	
	/**
	 * Find out if match the tie scenario.
	 * Result is correct only if matchWinScenario(...) is false
	 * @return			(boolean) whether match tie scenario
	 */
	public boolean matchTieScenario() {
		for (int i = this.board.getHeight() - 1; i >= 0; i--)
			for (int j = this.board.getWidth() - 1; j >= 0 ; j--)
				if (board.getGridStatus(i, j) == 0)
					return false;			// indicates game not finished
		return true;
	} // public boolean matchTieScenario()
	
	/**
	 * This is the scoring for AI checking a specific
	 * column for it's value towards winning
	 * @return (int) Score for this position
	 */
	public int check(int column, int playerId) {
		int row = 0;
		while(row < this.board.getHeight()) {
			if(board.getGridStatus(row, column) == 0) {
				break;
			}
			row++;
		}
		if(row == this.board.getHeight()) {
			return MIN_SCORE - 1;
		} else {
			int temp   = this.board.setPosition(row, column, -1);
			int result = checkPos(column,row,playerId);
			this.board.setPosition(row, column, temp);
			return result;
		}
	}
	
	/**
	 * this is the abstract for the unique board's to call all
	 * checks from
	 * @return (int) sum of score for this potision
	 */
	protected abstract int checkPos(int column, int row, int playerId);
	
	/**
	 * this is the list of checks for the different directions
	 * for the board based on the win scenarios being 4 long
	 * @return (int) score for direction
	 */
	protected int checkVertical(int column, int row, int win1, int win2, int win3, int win4, int playerId) {
		int minRow = Math.max(0, row - 3);
		int maxRow = Math.min(this.board.getHeight()-1, row + 3);
		// check if enough values to have 4 in a row
		if(maxRow - minRow < 3) {
			return 0;
		}
		int Score = 0;
		for(int i=minRow;i<=maxRow - 3;i++) {
			int temp = scorePoints(board.getGridStatus(i, column),
					board.getGridStatus(i+1, column),board.getGridStatus(i+2, column),
					board.getGridStatus(i+3, column),win1, win2, win3, win4, playerId);
			if(Math.abs(Score) < Math.abs(temp)) {
				Score = temp;
			}
			/*Score = Math.max(Score, scorePoints(board.getGridStatus(i, column),
					board.getGridStatus(i+1, column),board.getGridStatus(i+2, column),
					board.getGridStatus(i+3, column),win1, win2, win3, win4, playerId));*/
			/*Score += scorePoints(board.getGridStatus(i, column),
					 board.getGridStatus(i+1, column),board.getGridStatus(i+2, column),
					 board.getGridStatus(i+3, column),win1, win2, win3, win4, playerId);*/
		}
		return Score;
	}
	protected int checkHorizontal(int column, int row, int win1, int win2, int win3, int win4, int playerId) {
		int minColumn = Math.max(0, column - 3);
		int maxColumn = Math.min(this.board.getWidth()-1, column + 3);
		// check if enough values to have 4 in a row
		if((maxColumn - minColumn < 3)||(row > this.board.getHeight()-1)) {
			return 0;
		}
		int Score = 0;
		for(int i=minColumn;i<=maxColumn - 3;i++) {
			int temp = scorePoints(board.getGridStatus(row,i),
					board.getGridStatus(row,i+1),board.getGridStatus(row,i+2),
					board.getGridStatus(row,i+3),win1, win2, win3, win4, playerId);
			if(Math.abs(Score) < Math.abs(temp)) {
				Score = temp;
			}
			/*Score = Math.max(Score, scorePoints(board.getGridStatus(row,i),
					board.getGridStatus(row,i+1),board.getGridStatus(row,i+2),
					board.getGridStatus(row,i+3),win1, win2, win3, win4, playerId));*/
			/*Score += scorePoints(board.getGridStatus(row,i),
					 board.getGridStatus(row,i+1),board.getGridStatus(row,i+2),
					 board.getGridStatus(row,i+3),win1, win2, win3, win4, playerId);*/
		}
		return Score;
	}
	protected int checkPosSlope(int column, int row, int win1, int win2, int win3, int win4, int playerId) {
		// correct to bottom left corner diagonal to (column,row)
		int rMin,cMin;
		if((column < 3)||(row < 3)) {
			if(column-row<0) {
				rMin = row - column;
				cMin = 0;
			} else {
				cMin = column - row;
				rMin = 0;
			}
		} else {
			cMin = column - 3;
			rMin = row    - 3;
		}
		//get steps available
		int steps = 0;
		if((column + 3 > this.board.getWidth())||(row + 3 > this.board.getHeight())) {
			//will go out of bounds
			if(column - this.board.getWidth() > row - this.board.getHeight()) {
				//column is limiting factor
				steps = this.board.getWidth() - 1 - 3 - cMin;
			} else {
				//row is limiting factor
				steps = this.board.getHeight() - 1 - 3 - rMin;
			}
		} else {
			//neither limiting cap
			steps = column - cMin;
		}
		if(steps < 0) {
			return 0;
		}
		//get Score now
		int Score = 0;
		for(int i=0;i<steps;i++) {
			int temp = scorePoints(board.getGridStatus(rMin+i,cMin+i),
					board.getGridStatus(rMin+i+1,cMin+i+1),board.getGridStatus(rMin+i+2,cMin+i+2),
					board.getGridStatus(rMin+i+3,cMin+i+3),win1, win2, win3, win4, playerId);
			if(Math.abs(Score) < Math.abs(temp)) {
				Score = temp;
			}
			/*Score = Math.max(Score, scorePoints(board.getGridStatus(rMin+i,cMin+i),
					board.getGridStatus(rMin+i+1,cMin+i+1),board.getGridStatus(rMin+i+2,cMin+i+2),
					board.getGridStatus(rMin+i+3,cMin+i+3),win1, win2, win3, win4, playerId));*/
			/*Score += scorePoints(board.getGridStatus(rMin+i,cMin+i),
					 board.getGridStatus(rMin+i+1,cMin+i+1),board.getGridStatus(rMin+i+2,cMin+i+2),
					 board.getGridStatus(rMin+i+3,cMin+i+3),win1, win2, win3, win4, playerId);*/
		}
		return Score;
	}
	protected int checkNegSlope(int column, int row, int win1, int win2, int win3, int win4, int playerId) {
		// correct to bottom right corner diagonal to (column,row)
		int rMin,rMax,cMin,cMax;
		//figure out line
		rMax = row    + 3;
		cMin = column - 3;
		rMin = row    - 3;
		cMax = column + 3;
		if((column < 3)||(row < 3)) {
			if(column-row<0) {
				rMin = row - column;
				cMin = 0;
			} else {
				cMin = column - row;
				rMin = 0;
			}
		} else {
			cMin = column - 3;
			rMin = row    - 3;
		}
		//get steps available
		int steps = 0;
		if((column + 3 > this.board.getWidth())||(row + 3 > this.board.getHeight())) {
			//will go out of bounds
			if(column - this.board.getWidth() > row - this.board.getHeight()) {
				//column is limiting factor
				steps = this.board.getWidth() - 1 - 3 - cMin;
			} else {
				//row is limiting factor
				steps = this.board.getHeight() - 1 - 3 - rMin;
			}
		} else {
			//neither limiting cap
			steps = column - cMin;
		}
		if(steps < 0) {
			return 0;
		}
		rMax = rMin + steps + 2;
		cMax = cMin + steps + 2;
		//get Score now
		int Score = 0;
		for(int i=0;i<steps;i++) {
			try{
				int temp = scorePoints(board.getGridStatus(rMax-i,cMin+i),
						board.getGridStatus(rMax-i-1,cMin+i+1),board.getGridStatus(rMax-i-2,cMin+i+2),
						board.getGridStatus(rMax-i-3,cMin+i+3),win1, win2, win3, win4, playerId);
				if(Math.abs(Score) < Math.abs(temp)) {
					Score = temp;
				}
				/*Score = Math.max(Score, scorePoints(board.getGridStatus(rMax-i,cMin+i),
						board.getGridStatus(rMax-i-1,cMin+i+1),board.getGridStatus(rMax-i-2,cMin+i+2),
						board.getGridStatus(rMax-i-3,cMin+i+3),win1, win2, win3, win4, playerId));*/
				/*Score += scorePoints(board.getGridStatus(rMax-i,cMin+i),
						 board.getGridStatus(rMax-i-1,cMin+i+1),board.getGridStatus(rMax-i-2,cMin+i+2),
						 board.getGridStatus(rMax-i-3,cMin+i+3),win1, win2, win3, win4, playerId);*/
			} catch(ArrayIndexOutOfBoundsException e) {
				System.out.println("Error Negative: ");
				System.out.print("row : " + new Integer(row).toString() + "|");
				System.out.print("col : " + new Integer(column).toString() + "|");
				System.out.print("rMin: " + new Integer(rMin).toString() + "|");
				System.out.print("rMax: " + new Integer(rMax).toString() + "|");
				System.out.print("cMin: " + new Integer(cMin).toString() + "|");
				System.out.print("cMax: " + new Integer(cMax).toString() + "|");
				System.out.print("step: " + new Integer(steps).toString() + "|");
				System.out.print("i: " + new Integer(i).toString() + "\n");
			}
		}
		return Score;
	}
	
	/**
	 * Scores the lines passed
	 * @return (int) score for line
	 */
	protected abstract int scorePoints(int a,int b,int c,int d,int w1,int w2,int w3,int w4, int playerId);
	
	/**
	 * Returns the max absolute value from the inputed values
	 * @param a (int) value 1
	 * @param b (int) value 2
	 * @return int lerger absolute value between the two
	 */
	protected int absMax(int a, int b) {
		if (Math.abs(a) > Math.abs(b)) {
			return a;
		}
		return b;
	}

} // public abstract class ControllerBoard

