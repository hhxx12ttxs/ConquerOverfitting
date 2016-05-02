import java.util.LinkedList;

/**
 * <h3> Class Description</h3>
 * Checks if a Sudoku has a unique solution by attempting to solve the puzzle with logical techniques,
 * which will only solve the puzzle if a unique solution exits.This is specifically why backtracking 
 * was not used since it would require comparison of all solutions in order to ensure that only one 
 * solution existed.<br /><br />
 *  
 * Holds the current board and evaluation state required to solve a puzzle.
 * Also contains the following algorithms which are used for solving the particular state:- <br/>
 * Fill in rows, columns or sections missing a single value. <br/>
 * Fill in values which are only possible in a single cell of a section <br/>
 * Pointing pairs and triples algorithm - Check if there are two or three possible 
 * values in any section which all fall in the same row or column. The other instances of 
 * this possible value can be removed from cells in the respective row and column that are not 
 * contained within the relative section. <br/>
 * The following terminology will be used throughout this documentation:- <br/>
 * <h5>Row</h5>
 * A particular horizontal row from 0 to 8 which contains cells referenced as follows; <br/>
 * 0 x x x|x x x|x x x| <br/> 
 * 1 x x x|x x x|x x x| <br/> 
 * 2<u>  x x x|x x x|x x x| </u> <br/>
 * 3 x x x|x x x|x x x| <br/>
 * 4 x x x|x x x|x x x| <br/>
 * 5<u> x x x|x x x|x x x| </u><br/>
 * 6 x x x|x x x|x x x| <br/>
 * 7 x x x|x x x|x x x| <br/>
 * 8 x x x|x x x|x x x| <br/>
 * <h5>Column</h5>
 * A particular vertical column from 0 to 8 which contains cells referenced as follows; <br/>
 * 0 | 1 | 2| 3 | 4 | 5| 6 | 7 | 8 | <br/>
 * x | x | x | x | x | x | x | x | x | <br/>
 * <u> x | x | x | x | x | x | x | x | x | </u> <br/>
 * x | x | x | x | x | x | x | x | x | <br/>
 * x | x | x | x | x | x | x | x | x | <br/>
 * x | x | x | x | x | x | x | x | x | <br/>
 * <u> x | x | x | x | x | x | x | x | x | </u><br/>
 * x | x | x | x | x | x | x | x | x | <br/>
 * x | x | x | x | x | x | x | x | x | <br/>
 * x | x | x | x | x | x | x | x | x | <br/>
 * <h5>Section</h5>
 * A particular section consisting of 3 column and 3 rows labeled from 1 to 9 referenced as follows; <br/>
 *  x  x  x | x   x   x | x  x  x | <br/>
 *  x 1 x | x 2 x | x 3 x| <br/> 
 * <u> x x x | x x x | x x x | </u><br/>
 *  x  x  x | x   x   x | x  x  x | <br/>
 *  x 4 x | x 5 x | x 6 x| <br/>
 *  <u> x x x | x x x | x x x | </u><br/>
 *  x  x  x | x   x   x | x  x  x | <br/>
 *  x 7 x | x 8 x | x 9 x| <br/>
 *  x  x  x | x   x   x | x  x  x | <br/>
 * 
 * @author Hayden Smith, Laura Hodges, Jerome Robins, Steven Falconieri
 */

public class SudokuSolver {
		
		/** 
		 * Constructs and initializes the board state given a 2D integer array of values with empty cells.
		 * @param inputBoard 2D integer array of board state.
		 */
		public SudokuSolver()
		{
			
		}
		
		/**
		 * Since implementation only uses logical solving techniques, the puzzle
		 * 	can only be solved if there is a unique solution. This is specifically
		 * 	why backtracking was not used
		 * @param boardToSolve Board that needs to be solved
		 * @return Whether the board has a unique solution
		 */
		public boolean isUniqueSolution(Board boardToSolve) {
			int board[][] = new int[boardToSolve.getBoardSize()][boardToSolve.getBoardSize()];
			for(int row = 1; row <= boardToSolve.getBoardSize(); row++) {
				for(int col = 1; col <= boardToSolve.getBoardSize(); col++) {
					if(boardToSolve.isInitiallySet(row, col)) {
						board[row-1][col-1] = boardToSolve.getCellValue(row, col);	
					} else {
						board[row-1][col-1] = SudokuSolver.EMPTY;
					}
				}
			}
			firstRun(board);
			if(!solved()) {
				solve();
			}
			return solved();
		}

		public void firstRun(int inputBoard[][]) {
			// Initialize Board
			this.board = new int[BOARD_SIZE][BOARD_SIZE];
			this.temp = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
			this.board = inputBoard;
			for(int row = 0; row < BOARD_SIZE; row++) {
				for(int col = 0; col < BOARD_SIZE; col++) {
					// Initialize all temporary values to be possible;
					for(int i = 0; i < BOARD_SIZE; i++) {
						this.temp[row][col][i] = POSSIBLE;	
					}
					if(this.board[row][col] != EMPTY) {
					}
					debug(this.board[row][col]);
				}
				
			}
			this.updateTempVals();
			this.updateCellsFilled();
		}
		
		/**
		 * Checks if current board is solved. <br/>
		 * Solved is defined to be both complete(All values inserted) 
		 * and valid (all values comply with the conditions of the game).
		 * @return True if the current BoardState is solved.
		 */
		public boolean solved() {
			if(this.updateCellsFilled() == BOARD_SIZE * BOARD_SIZE && this.valid()) {
				return true;
			}
			return false;
		}

		/**
		 * Applies various solving algorithms in order to solve the board using logical reasoning. <br/>
		 * Will only ever solve the board if there is a unique solution for the current Sudoku state.
		 */
		public void solve() {
			debug("Updating Temp Vals");
			this.updateTempVals();
			debug("_______________");
			
			if(this.solved()) {
				debug("Is Solved and Valid");
			} else {
				debug("Is Not Solved or Valid");
			}
			while(this.solveMediumSteps() && this.pointingPairsAndTriplesAlgorithm()) {
			}
			debug("This is Valid = " + this.valid());
			debug("This is Solved = " + this.solved());
			if(this.solved()) {
				debug("Is Solved and Valid");
				//this.print2DBoard(this.board);
			} else {
				debug("Is Not Solved or Valid");
			}	
		}

		/**
		 * Solves for cells which have all but 1 number missing in a column, row or section.
		 */
		private boolean solveSimpleSteps()
		{
			boolean stepsSolved = false;
			boolean cellsFilled = false;
			for(int row = 0; row < BOARD_SIZE; row++){
				for(int col = 0; col < BOARD_SIZE; col++) {
					if(this.board[row][col] == EMPTY) {
						int cellVal = this.getTempFinalVal(row, col);
						if(cellVal != NOT_POSSIBLE) {
							this.board[row][col] = cellVal;
							this.updateCellsFilled();
							this.updateTempVals();
							cellsFilled = true;
						} 
					}
				}
			}
			if(cellsFilled) {
				this.solveSimpleSteps();
				stepsSolved = true;
			}
			return stepsSolved;
		}

		/**
		 * Checks all other rows and columns for possible values in order to evaluate if a cell can be filled.
		 * The example below illustrates <br/>
		 * E.g.<br/>
		 * | 1  - -| 3  -  - | -  -  - |  <br/>
		 * | 2  - -| -  -  - | -  3  - |  <br/>
		 * <u>| x  |          |         |</u>  <br/>
		 * |-  3 | | <br/>
		 * | -  | | | <br/>
		 * <u> | -   | | | </u><br/>
		 * | - | 3| <br/>
		 *  x = 3 since all other rows and columns already have a 3. <br/>
		 *  This is achieved logically by considering all possible values for a section and checking if there is
		 *  only one valid location with the specific section for a particular possible value.
		 *  @return True if any cells were solved.
		 */
		private boolean solveMediumSteps() {
			boolean stepsSolved = false;
			boolean cellsFilled = false;
			int sectionRow = 0;
			int sectionCol = 0;
			LinkedList<Integer> possibleCellVal = null;
			LinkedList<Integer> allOtherPossibleCellVals = new LinkedList<Integer>();
			boolean sameCell = false;
			// Solve all simple steps first in order to provide as much information as 
			// possible about the current state.
			this.solveSimpleSteps();
			// For each row and column
			for(int row = 0; row < BOARD_SIZE; row++) {
				for(int col = 0; col < BOARD_SIZE; col++) {
					// Get all possible values for the current cell
					possibleCellVal = this.possibleVals(row, col);
					if(this.getTempFinalVal(row, col) == NOT_POSSIBLE) {
						// For each possible value of this cell.
						for(Integer num : possibleCellVal) {
							sectionRow = 3*(row/3);
							sectionCol = 3*(col/3);
							allOtherPossibleCellVals.clear();
							// Get all the possible values to be filled in this section.
							for(int rowTranslate = 0;
								rowTranslate < SECTION_SIZE; rowTranslate++) {				
									for(int colTranslate = 0; colTranslate < SECTION_SIZE; colTranslate++) {
									int currentRow = sectionRow + rowTranslate;
									int currentCol = sectionCol + colTranslate;
									sameCell = ((currentRow == row) && (currentCol == col));
									if(!sameCell) {
										allOtherPossibleCellVals.addAll(this.possibleVals(currentRow, currentCol));
									}
								}
							}
							this.removeDuplicates(allOtherPossibleCellVals);
							debug("Num = " + num + " (" + row + ", " + col + ") " + allOtherPossibleCellVals + !allOtherPossibleCellVals.contains(num));
							// If this is the only possible location for this value then this value must be in this cell location.
							if(!allOtherPossibleCellVals.contains(num)) {
								this.board
								[row][col] = num;
								this.updateCellsFilled();
								this.updateTempVals();
								cellsFilled = true;
							}
						}
					}
				}
			}
			// Check if further cells can be solved as consequence of finding a solution for this cell.
			if(cellsFilled) {
				this.solveMediumSteps();
				stepsSolved = true;
			}
			this.solveSimpleSteps();
			return stepsSolved;
		}
		
		/**
		 * Applies a method of removing possible values from rows, columns and sections based upon the number 
		 * and location of particular cells. Specific details about this algorithm are explained here
		 * <a src="http://www.sudokuwiki.org/Intersection_Removal"> http://www.sudokuwiki.org/Intersection_Removal </a>.
		 * @return True if any possible values were removed. 
		 */
		private boolean pointingPairsAndTriplesAlgorithm() {
			debug("Pointing pairs algorithm ************");
			boolean cellsFilled = true;
			this.updateBoardVals();
			this.updateTempVals();
			debug("Pointing pairs algorithm ************");
			this.solveMediumSteps();
			cellsFilled = false;
			for(int row = 0; row < this.BOARD_SIZE; row++) {
				for(int col = 0; col < this.BOARD_SIZE; col++) {
					int section = this.getSection(row, col);
					int sectionFirstRow = this.getSectionFirstRow(section); 
					int sectionFirstCol = this.getSectionFirstCol(section);
					// Current Cell is First Cell in a Section
					LinkedList<Integer> sectionRowPossibleVals = this.getSectionRowPossibleVals(row, col);
					LinkedList<Integer> sectionColPossibleVals = this.getSectionColPossibleVals(row, col);
					// Get all possible values of this section except this cell.
					LinkedList<Integer> sectionPossibleVals = this.getSectionPossibleVals(row, col);
					LinkedList<Integer> rowPossibleVals = this.getRowPossibleVals(row);
					LinkedList<Integer> colPossibleVals = this.getColPossibleVals(col);
					for(int num = 1; num <= this.BOARD_SIZE; num++) {
						int numOccurrencesInSection = this.numberOccurances(num, sectionPossibleVals);
						int numOccurrencesInSectionRow = this.numberOccurances(num, sectionRowPossibleVals);
						int numOccurrencesInSectionCol = this.numberOccurances(num, sectionColPossibleVals);
						int numOccurrencesInRow = this.numberOccurances(num, rowPossibleVals);
						int numOccurrencesInCol = this.numberOccurances(num, colPossibleVals);
						boolean pairOrTripleInSection = (numOccurrencesInSection == 2 || numOccurrencesInSection == 3); 
						boolean pairOrTripleInSectionRow = (numOccurrencesInSectionRow == 2 || numOccurrencesInSectionRow == 3);
						boolean pairOrTripleInSectionCol = (numOccurrencesInSectionCol == 2 || numOccurrencesInSectionCol == 3);
						boolean pairOrTripleInRow = (numOccurrencesInRow == 2 || numOccurrencesInRow == 3);
						boolean pairOrTripleInCol = (numOccurrencesInCol == 2 || numOccurrencesInCol == 3);
						// A Pair or Triple in a box - if they are aligned on a row, n can be removed from the rest of the row.
						//                           - if they are aligned on a column, n can be removed from the rest of the column.
						//debug("# of " + num + " in section " + section + " is " + numOccurrencesInSection + " with " + numOccurrencesInSectionRow + " in section row " + row + " and " + numOccurrencesInSectionCol + " in section col " + col);
						if(row == sectionFirstRow && col == sectionFirstCol) {
							// A Pair or Triple in box
							if(pairOrTripleInSection) {
								//debug("# of " + num + " in section " + section + " is " + numOccurrencesInSection + " with " + numOccurrencesInSectionRow + " in row " + row + " and " + numOccurrencesInSectionCol + " in col " + col);
								if(numOccurrencesInSectionRow == numOccurrencesInSection) {			
									debug("# in section = # in row");
									debug("pairOrTriple in section row " + row);
									debug("pair Or Triple in Section = " + pairOrTripleInSection + " in sectionRow = " + pairOrTripleInSectionRow + " in sectionCol = " + pairOrTripleInSectionCol);
									debug("Num is " + num + " vals in section " + sectionPossibleVals + " vals in section row " + sectionRowPossibleVals + " vals in section col " + sectionColPossibleVals);
									for(int currentCol = 0; currentCol < this.BOARD_SIZE; currentCol++) {
										// Check if currentCol is not contained within this section of the board.
										if(this.getSection(row, currentCol) != section && this.possibleVals(row, currentCol).contains(num)) {
											// Remove occurrences of this number from all columns in this row that are not in the same section.
											this.removeTempVal(num, row, currentCol);
											if(!this.valid()) {
												this.updateTempVals();
											} else {
												cellsFilled = true;
												debug("Removed possibility of " + num + " from (" + currentCol + ", " + row + ") From Section " + this.getSection(row, currentCol) + " due to section " + section);
												this.updateBoardVals();
											}
										}
									}
								}
								if(numOccurrencesInSectionCol == numOccurrencesInSection) {
									debug("pairOrTriple in section col " + col);
									debug("pair Or Triple in Section = " + pairOrTripleInSection + " in sectionRow = " + pairOrTripleInSectionRow + " in sectionCol = " + pairOrTripleInSectionCol);
									debug("Num is " + num + " vals in section " + sectionPossibleVals + " vals in section row " + sectionRowPossibleVals + " vals in section col " + sectionColPossibleVals);
									debug("# in section = # in col");
									for(int currentRow = 0; currentRow < this.BOARD_SIZE; currentRow++) {
										// Check if currentRow is not contained within this section of the board.
										if(this.getSection(currentRow, col) != this.getSection(row, col) && this.possibleVals(currentRow, col).contains(num)) {
											// Remove occurrences of this number from all rows in this column that are not in the same section.
											this.removeTempVal(num, currentRow, col);
											if(!this.valid()) {
												this.updateTempVals();
											} else {
												cellsFilled = true;
												debug("Removed possibility of " + num + " from (" + col + ", " + currentRow + ") From Section " + this.getSection(currentRow, col) + " due to Section " + section);
												this.updateBoardVals();
											}
										}	
									}
								}
							}
						} 
						// A Pair or Triple on a row - if they are all in the same box, n can be removed from the rest of the box.
						if(numOccurrencesInSectionRow == numOccurrencesInRow && pairOrTripleInRow) {
							debug("A Pair or Triple on a row - if they are all in the same box = " + section + " row = " + row + " col = " + col);
							for(int rowTranslate = 0; rowTranslate < this.SECTION_SIZE; rowTranslate++) {
								for(int colTranslate = 0; colTranslate < this.SECTION_SIZE; colTranslate++) {
									int currentRow = sectionFirstRow + rowTranslate;
									int currentCol = sectionFirstCol + colTranslate;
									if(currentRow != row && this.possibleVals(currentRow, currentCol).contains(num)) 
										debug("# of " + num + " in row " + row + " is " + numOccurrencesInRow + " with " + numOccurrencesInSectionRow + " in section row " + row + " and " + numOccurrencesInSectionCol + " in section col " + col);
										debug("pair Or Triple in Section = " + pairOrTripleInSection + " in sectionRow = " + pairOrTripleInSectionRow + " in sectionCol = " + pairOrTripleInSectionCol);
										debug("Num is " + num + " vals in section " + sectionPossibleVals + " vals in section row " + sectionRowPossibleVals + " vals in section col " + sectionColPossibleVals);
										this.removeTempVal(num, currentRow, currentCol);
										if(!this.valid()) {
											this.updateTempVals();
										} else {
											debug("Removed possibility of " + num + " from (" + currentCol + ", " + currentRow + ") ");
											cellsFilled = true;
											this.updateBoardVals();
										}
									}
								}
							}
					
						// A Pair or Triple on a column - if they are all in the same box, n can be removed from the rest of the box.
						if(numOccurrencesInSectionCol == numOccurrencesInCol && pairOrTripleInCol) {
							debug("# of " + num + " in section " + section + " is " + numOccurrencesInSection + " with " + numOccurrencesInSectionRow + " in row " + row + " and " + numOccurrencesInSectionCol + " in col " + col);
							debug("A Pair or Triple on a col - if they are all in the same box = " + section + " row = " + row + " col = " + col);
							for(int rowTranslate = 0; rowTranslate < this.SECTION_SIZE; rowTranslate++) {
								for(int colTranslate = 0; colTranslate < this.SECTION_SIZE; colTranslate++) {
									int currentRow = sectionFirstRow + rowTranslate;
									int currentCol = sectionFirstCol + colTranslate;
									if(currentCol != col && this.possibleVals(currentRow, currentCol).contains(num)) { 
										debug("# of " + num + " in col " + col + " is " + numOccurrencesInCol + " with " + numOccurrencesInSectionRow + " in section row " + row + " and " + numOccurrencesInSectionCol + " in section col " + col);
										debug("pair Or Triple in Section = " + pairOrTripleInSection + " in sectionRow = " + pairOrTripleInSectionRow + " in sectionCol = " + pairOrTripleInSectionCol);
										debug("Num is " + num + " vals in section " + sectionPossibleVals + " vals in section row " + sectionRowPossibleVals + " vals in section col " + sectionColPossibleVals);
														
										this.removeTempVal(num, currentRow, currentCol);
										if(!this.valid()) {
											this.updateTempVals();
										} else {
											debug("Removed possibility of " + num + " from (" + currentCol + ", " + currentRow + ") ");
											cellsFilled = true;
											this.updateBoardVals();
										}
									}
								}
							}
						}
						this.updateBoardVals();
						this.updateTempVals();
					}
				}
			}
			
			this.solveMediumSteps();
			return cellsFilled;
		}

		/**
		 * Returns section number from 1 to 9 as illustrated below and described in the SudokuState Class Description:- <br/>
		 * | 1 | 2 | 3 | <br/>
		 * ---------- <br/>
		 * | 4 | 5 | 6 | <br/>
		 * ---------- <br/>
		 * | 7 | 8 | 9 | <br/>
		 * ---------- <br/>
		 * @param row Value from 0 to 8 representing the row number from top to bottom
		 * @param col Value from 0 to 8 representing the column number from left to right
		 * @return Section number from 1 to 9 as shown above.
		 */
		private int getSection(int row, int col) {
			int sectionRow = 0;
			int sectionCol = 0;
			if(row < 3) {
				sectionRow = 1;
			} else if(row < 6 ) {
				sectionRow = 2; 
			} else if(row < 9) {
				sectionRow = 3;
			}
			if(col < 3) {
				sectionCol = 1;
			} else if(col < 6) {
				sectionCol = 2;
			} else if(col < 9) {
				sectionCol = 3;
			}
			return 3*(sectionRow-1) + sectionCol;
		}

		/** Returns the first column for a given section parsed
		 * @param section Square of 3 by 3 cells contained on a Sudoku board labeled from 1-9 as shown below. <br/>
		 * |1|2|3| <br/>
		 * |4|5|6| <br/>
		 * |7|8|9| <br/>
		 * @return First column in a given section.
		 */
		private int getSectionFirstCol(int section) {
			return (3 * ((section - 1) % 3));
		}

		/** Returns the first row for a given section parsed
		 * @param section Square of 3 by 3 cells contained on a Sudoku board labeled from 1-9 as shown below. <br/>
		 * |1|2|3| <br/>
		 * |4|5|6| <br/>
		 * |7|8|9| <br/>
		 * @return First row in a given section.
		 */
		private int getSectionFirstRow(int section) {
			return 3 * ((section - 1)/ 3);
		}
		
		/**
		 * Returns list of all possible values for cells in this section of the row parsed.
		 * @param row Value from 0 to 8 representing the row number from top to bottom
		 * @param col Value from 0 to 8 representing the column number from left to right
		 * @return List of possible values for cells in the section and row defined by the row and columns parsed.
		 */
		private LinkedList<Integer> getSectionRowPossibleVals(int row, int col) {
			int section = this.getSection(row, col);
			int sectionFirstCol = this.getSectionFirstCol(section);
			LinkedList<Integer> sectionRowTemps = new LinkedList<Integer>();
			for(int colTranslate = 0; colTranslate < this.SECTION_SIZE; colTranslate++) {
				int currentCol = sectionFirstCol + colTranslate;
				if(this.getTempFinalVal(row, currentCol) == this.NOT_POSSIBLE) {
					sectionRowTemps.addAll(this.possibleVals(row, currentCol));
				}
			}
			return sectionRowTemps;
		}
		/**
		 * Returns list of all possible values for cells in this section of the column parsed.
		 * @param row Value from 0 to 8 representing the row number from top to bottom
		 * @param col Value from 0 to 8 representing the column number from left to right
		 * @return List of possible values for a cells in the section and column defined by the row and columns parsed.
		 */
		private LinkedList<Integer> getSectionColPossibleVals(int row, int col) {
			int section = this.getSection(row, col);
			int sectionFirstRow = this.getSectionFirstRow(section);
			LinkedList<Integer> sectionColTemps = new LinkedList<Integer>();
			for(int rowTranslate = 0; rowTranslate < this.SECTION_SIZE; rowTranslate++) {
				int currentRow = sectionFirstRow + rowTranslate;
				if(this.getTempFinalVal(currentRow, col) == this.NOT_POSSIBLE) {
					sectionColTemps.addAll(this.possibleVals(currentRow, col));
				}
			}
			return sectionColTemps;
		}
		
		/**
		 * Returns list of all possible values for cells in the column parsed.
		 * @param col Value from 0 to 8 representing the column number from left to right
		 * @return List of possible values for all cells in the column parsed. 
		 */
		private LinkedList<Integer> getColPossibleVals(int col) {
			LinkedList<Integer> sectionColTemps = new LinkedList<Integer>();
			for(int currentRow = 0; currentRow < this.BOARD_SIZE; currentRow++) {
				if(this.getTempFinalVal(currentRow, col) == this.NOT_POSSIBLE) {
					sectionColTemps.addAll(this.possibleVals(currentRow, col));
				}
			}
			return sectionColTemps;
		}
		
		/**
		 * Returns list of all possible values for cells in the row parsed.
		 * @param row Value from 0 to 8 representing the row number from top to bottom
		 * @return List of possible values for all cells in the row parsed.  
		 */
		private LinkedList<Integer> getRowPossibleVals(int row) {
			LinkedList<Integer> sectionRowTemps = new LinkedList<Integer>();
			for(int currentCol = 0; currentCol < this.BOARD_SIZE; currentCol++) {
				if(this.getTempFinalVal(row, currentCol) == this.NOT_POSSIBLE) {
					sectionRowTemps.addAll(this.possibleVals(row, currentCol));
				}
			}
			return sectionRowTemps;
		}
		
		/**
		 * Returns list of all possible values of cells contained within the section that row and col are contained in.
		 * @param row Value from 0 to 8 representing the row number from top to bottom
		 * @param col Value from 0 to 8 representing the column number from left to right
		 * @return List of all possible values in the section defined by the row and column parsed.
		 */
		private LinkedList<Integer> getSectionPossibleVals(int row, int col) {
			int sectionFirstRow = 3*(row/3);
			int sectionFirstCol = 3*(col/3);
			int currentRow = 0;
			int currentCol = 0;
			LinkedList<Integer> sectionTemps = new LinkedList<Integer>();
			for(int rowTranslate = 0; rowTranslate < this.SECTION_SIZE; rowTranslate++) {
				for(int colTranslate = 0; colTranslate < this.SECTION_SIZE; colTranslate++) {
					currentRow = sectionFirstRow + rowTranslate;
					currentCol = sectionFirstCol + colTranslate;
					// Exclude Final Values
					if(this.getTempFinalVal(currentRow,currentCol) == this.NOT_POSSIBLE) {
						sectionTemps.addAll(this.possibleVals(currentRow, currentCol));
					}
				}
			}
			return sectionTemps;
		}
		
		/**
		 * Return the only possible value for a given cell given all other possibilities have been exhausted. 
		 * @param row Value from 0 to 8 representing the row number from top to bottom
		 * @param col Value from 0 to 8 representing the column number from left to right
		 * @return The only possible value for a given cell given all other possibilities have been exhausted. <br/>
		 * this.NOT_POSSIBLE otherwise.
		 */
		private int getTempFinalVal(int row, int col) {
			LinkedList<Integer> possibleCellVals = this.possibleVals(row, col);
			if(possibleCellVals.size() == 1) {
				return possibleCellVals.peek();
			} 
			return NOT_POSSIBLE;		
		}

		/**
		 * Removes the possibility of num being a solution at row and col. <br/>
		 * <u>Note:-</U> Performing the updateTempVals method will override removals made by this method.
		 * @param num 
		 * @param row Value from 0 to 8 representing the row number from top to bottom
		 * @param col Value from 0 to 8 representing the column number from left to right
		 * @return
		 */
		private boolean removeTempVal(int num, int row, int col) {
			if(this.getTempFinalVal(row, col) == this.NOT_POSSIBLE) {
				this.temp[row][col][num - 1] = this.NOT_POSSIBLE;
				return true;
			}
			return false;
		}

		/**
		 * Removes duplicate numbers from list.
		 * @param list The list to remove duplicates from.
		 */
		private void removeDuplicates(LinkedList<Integer> list) {
			LinkedList<Integer> duplicateFreeList = new LinkedList<Integer>();
			while(!list.isEmpty()) {
				Integer currentElement = list.remove();
				if(!duplicateFreeList.contains(currentElement)) {
					duplicateFreeList.add(currentElement);
				}
			}
			while(!duplicateFreeList.isEmpty()) {
				list.add(duplicateFreeList.remove());
			}
		}
		
		/**
		 * Returns the number of occurrences of a single number within a list of integers.
		 * @param number The number to count the occurrences of within the given list.
		 * @param list The list of integers to search through.
		 * @return The number of occurrences of a single number within a list of integers. 
		 */
		private int numberOccurances(int number, LinkedList<Integer> list) {
			int numCounter = 0;
			for(Integer currentNum : list) {
				if(currentNum == number) {
					numCounter++;
				}
			}
			return numCounter;
		}

		/**
		 * Eliminate the possibility for all temp values except that assigned by number.
		 * @param number
		 * @param row Value from 0 to 8 representing the row number from top to bottom
		 * @param col Value from 0 to 8 representing the column number from left to right
		 */
		private void setTempFinalVal(int number, int row, int col) {
			for(int num = 0; num < BOARD_SIZE; num++) {
				if(number != (num + 1)) {
					this.temp[row][col][num] = NOT_POSSIBLE;
				} else {
					this.temp[row][col][num] = POSSIBLE;
				}
			}
		}
	
		/** 
		 * Prints debugging to console.
		 * @param obj Object to convert to string before outputting to the console.
		 */
		private void debug(Object obj) {
			//System.out.println(obj.toString());
		}
		/**
		 * Provides linked list of possible values for a given cell at (row, col)
		 * @param row Value from 0 to 8 representing the row number from top to bottom
		 * @param col Value from 0 to 8 representing the column number from left to right
		 * @return List of Integers of possible values for this cell.
		 */
		private LinkedList<Integer> possibleVals(int row, int col) {
			LinkedList<Integer> possibleVals = new LinkedList<Integer>();
			for(int number = 0; number < BOARD_SIZE; number++) {
				if(this.temp[row][col][number] == POSSIBLE) {
					possibleVals.add(number+1);
				}
			}
			return possibleVals;
		}
		
		
		/**
		 * Checks if a the current board state (all rows, columns and squares) have valid cell values.
		 * @return True if all cells in the current board state are compliant to the rules of Sudoku. 
		 */
		private boolean valid() {
			for(int row = 0; row < BOARD_SIZE; row++) {
				for(int col = 0; col < BOARD_SIZE; col++) {
					for(int number = 1; number <= BOARD_SIZE; number++) {
						if(this.inColumn(number, col) > 1 || this.inRow(number, row) > 1 || this.inSection(number, row, col) > 1) {
							debug("row = " + row + " col = " + col);
							return false;
						}
					}
				}
			}
			debug("Is Valid");
			return true;
		}
		
		
		/**
		 * Sets all temp values in a given cell to be empty
		 * @param row Value from 0 to 8 representing the row number from top to bottom
		 * @param col Value from 0 to 8 representing the column number from left to right
		 */
		private void clearTemp(int row, int col) {
			for(int i = 0; i < BOARD_SIZE; i++) {
				this.temp[row][col][i] = POSSIBLE;
			}
		}
		
		/**
		 * Updates and returns the number of filled cells in the current board state.
		 * @return The number of cells which are filled in the current board state.
		 */
		private int updateCellsFilled() {
			int count = 0;
			for(int row = 0; row < BOARD_SIZE; row++) {
				for(int col = 0; col < BOARD_SIZE; col++) {
					if(this.board[row][col] != EMPTY) {
						count++;
					}
				}
			}
			return count;
		}
		
		/**
		 * Updates the value of temporary (possible) cells based upon the filled cells in its given row, column and square.
		 */
		private void updateTempVals() {
			for(int row = 0; row < BOARD_SIZE; row++) {
				for(int col = 0; col < BOARD_SIZE; col++) {
					int cellVal = this.board[row][col];
					this.clearTemp(row, col);
					if(cellVal != EMPTY && this.getTempFinalVal(row, col) == NOT_POSSIBLE) {
						this.setTempFinalVal(cellVal, row, col);
					} else {
						for(cellVal = 1; cellVal <= BOARD_SIZE; cellVal++) {
							if(!this.canBe(cellVal, row, col)) {
								// Update possible temp vals
								this.temp[row][col][cellVal-1] = NOT_POSSIBLE;
							}
						}
					}
				}
			}
		}
		/**
		 * Updates the value of board values based upon changes made to temporary cells which have a final value set.
		 * @return True if board values have been updated due to this method call.
		 */
		private boolean updateBoardVals() {
			boolean updated = false;
			for(int row = 0; row < BOARD_SIZE; row++) {
				for(int col = 0; col < BOARD_SIZE; col++) {
					if(this.getTempFinalVal(row, col) != NOT_POSSIBLE && this.board[row][col] == EMPTY) {
						this.board[row][col] = this.getTempFinalVal(row, col);
						updated = true;
					}
				}
			}
			return updated;
		}
		
		/**
		 * Returns the number of occurrences of a number in a given row.
		 * @param number Number looked for in given row.
		 * @param row Value from 0 to 8 representing the row number from top to bottom
		 * @return The amount of occurrences of a specific number within the row specified.
		 */
		private int inRow(int number, int row) {
			int answer = 0;
			for(int tempCol = 0; tempCol < BOARD_SIZE; tempCol++) {
				if(this.board[row][tempCol] == number) {
					answer++;
				}
			}
			return answer;
		}
		/**
		 * Returns the number of occurrences of a number in a given column.
		 * @param number Number looked for in given row.
		 * @param col Value from 0 to 8 representing the column number from left to right
		 * @return The amount of occurrences of a specific number within the column specified.
		 */		
		private int inColumn(int number, int col) {
			int answer = 0;
			for(int tempRow = 0; tempRow < BOARD_SIZE; tempRow++) {
				if(this.board[tempRow][col] == number) {
					answer++;
				}
			}
			return answer;
		}
		
		/**
		 * Returns number of occurrences of number within a section determined by the row 
		 * and column of the number cell.
		 * @param number
		 * @param row Value from 0 to 8 representing the row number from top to bottom
		 * @param col Value from 0 to 8 representing the column number from left to right
		 * @return The amount of occurrences of a specific number within the section specified by row and column. 
		 */
		private int inSection(int number, int row, int col) {
			int sectionRow = 3*(row /3);
			int sectionCol = 3*(col /3);
			int answer = 0;
			for(int i = 0; i < SECTION_SIZE; i++) {
				for(int j = 0; j < SECTION_SIZE; j++) {
					if(this.board[sectionRow+i][sectionCol+j] == number) {
						answer++;
					}
				}
			}
			return answer;
		}

		/**
		 * Returns true if a number can be assigned to a cell without conflict
		 * @param number
		 * @param row Value from 0 to 8 representing the row number from top to bottom
		 * @param col Value from 0 to 8 representing the column number from left to right
		 * @return True if number is a possible value for the cell located at row, column.
		 */
		private boolean canBe(int number, int row, int col) {
			if(this.inColumn(number, col) > 0 
					|| this.inRow(number, row) > 0 
						|| this.inSection(number, row, col) > 0) {
				return false;
			}
			return true;
		}
		/**
		 * @param EMPTY Static integer representation of unknown cell values used to solve and construct Sudoku board states.
		 */
		public static final int EMPTY = -1;	
		private int board[][];
		private int temp[][][];
		private final int BOARD_SIZE = 9;
		private final int SECTION_SIZE = 3;
		private final int NOT_POSSIBLE = -10;
		private final int POSSIBLE = -100;
}


