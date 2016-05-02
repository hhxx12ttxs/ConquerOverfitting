import java.awt.geom.Point2D;
import java.util.Random;
import java.util.Stack;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This class generates a maze for the game. The player will have to go from the
 * entry to the exit.
 * @author Pokemon_Mike
 */
public class MazeGenerator {

	// #########################################################
	// #                	CONSTRUCTOR                        #
	// #########################################################
	
	/**
	 * This function generates a randomised maze. The algorithm used is the
	 * Recursive Backtrack method. A 2D array was used to store a code which
	 * identifies what kind of tile should be placed.
	 */
	public MazeGenerator() {
		this.entryRow = 0;
		this.entryCol = 0;
		this.exitRow = 0;
		this.exitCol = 0;
		this.height = 1;
		this.width = 1;
	}

	// #########################################################
	// #                	PUBLIC METHODS                     #
	// #########################################################
	
	/**
	 * Generates a maze represented as a 2D array of the specified difficulty
	 * level. The level affects the size of the maze generated.
	 * 
	 * @param difficulty
	 *            The difficulty level.
	 * @return The maze represented as a 2D array.
	 */
	public int[][] generateMaze(int difficulty) {
		setHeight(difficulty * 10 + 1);
		setWidth(difficulty * 16 - 1);

		// INITILISE MAZE WITH UNVISITED TILES, AND WALL TILES AROUND THE EDGES
		// FOR THE BORDER. THE HEIGHT OF THE MAZE WILL ALWAYS BE AN ODD INDEX. 
		setMaze();
		int row = 0;
		int col = 0;
		int choose = 0;
		int obstacles = height * width / 100;
		int loops = obstacles / 2;

		Random random = new Random();
		Stack<Integer> stackRow = new Stack<Integer>();
		Stack<Integer> stackCol = new Stack<Integer>();

		// MARK A NODE AS VISITED. THE NODE MUST BE SET ON AN ODD ROW AND COL.
		// THE MAZE HEIGHT IS USED AS A SEED FOR RANDOM, AND MULTIPLED BY 2 
		// TO GUARANTEE AN EVEN NUMBER. SINCE ODD INDICES ARE REQUIRED,
		// WE THEN ADD 1. 
		row = (random.nextInt((height) / 2)) * 2 + 1;
		col = (random.nextInt((width) / 2)) * 2 + 1;

		// MAKE THE INITAL CELL THE CURRENT CELL AND MARK IT AS VISITED
		this.maze[row][col] = PATH_TILE;

		// WHILE THERE ARE INVISITED CELLS
		while (hasUnvisitedCell()) {

			// IF THE CURRENT CELL HAS ANY NEIGHBOURS WHICH HAVE NOT BEEN VISITED
			if ((row - 2 >= 0 && getContent(row - 2, col) == UNVISITED)
					|| (row + 2 < height && getContent(row + 2, col) == UNVISITED)
					|| (col - 2 >= 0 && getContent(row, col - 2) == UNVISITED)
					|| (col + 2 < width && getContent(row, col + 2) == UNVISITED)) {

				int tempRow = row;
				int tempCol = col;
				boolean appropriateCell = false;

				// CHOOSE RANDOMLY ONE OF THE UNVISITED NEIGHBOURS
				while (appropriateCell == false) {
					choose = random.nextInt(4);

					if (choose == 0) {
						if (row - 2 >= 0
								&& getContent(row - 2, col) == UNVISITED) {
							row = row - 2;
							appropriateCell = true;
						}
					} else if (choose == 1) {
						if (row + 2 < height
								&& getContent(row + 2, col) == UNVISITED) {
							row = row + 2;
							appropriateCell = true;
						}
					} else if (choose == 2) {
						if (col - 2 >= 0
								&& getContent(row, col - 2) == UNVISITED) {
							col = col - 2;
							appropriateCell = true;
						}
					} else if (choose == 3) {
						if (col + 2 < width
								&& getContent(row, col + 2) == UNVISITED) {
							col = col + 2;
							appropriateCell = true;
						}
					}
				}
				// PUSH THE CURRENT CELL TO THE STACK
				stackRow.push(tempRow);
				stackCol.push(tempCol);

				// REMOVE THE WALL BETWEEN THE CURRENT CELL AND THE CHOSEN CELL
				if (choose == 0) {
					this.maze[tempRow - 1][tempCol] = PATH_TILE;
				} else if (choose == 1) {
					this.maze[tempRow + 1][tempCol] = PATH_TILE;
				} else if (choose == 2) {
					this.maze[tempRow][tempCol - 1] = PATH_TILE;
				} else if (choose == 3) {
					this.maze[tempRow][tempCol + 1] = PATH_TILE;
				}

				// MAKE THE CHOSEN CELL THE CURRENT CELL AND MARK IT AS VISITED
				this.maze[row][col] = PATH_TILE;

				// ELSE IF THE STACK IS NOT EMPTY
			} else if (!stackRow.isEmpty()) {

				// POP A CELL FROM THE STACK 
				// MAKE IT THE CURRENT CELL
				row = stackRow.pop();
				col = stackCol.pop();
				this.maze[row][col] = PATH_TILE;

			} else {
				// PICK A RADNOM UNVISTED CELL, MAKE IT THE CURRENT CELL AND
				// MARK IT AS VISITED
				while (this.maze[row][col] != UNVISITED) {
					row = (random.nextInt((height) / 2)) * 2 + 1;
					col = (random.nextInt((width) / 2)) * 2 + 1;
				}
			}
		}

		// PICK AN ENTRY
		setEntry();

		// LOOPS AND PATHS
		setLoops(loops);
		showBFS();

		// PICK AN EXIT
		boolean appropriateEntry = false;
		int retry = 0;
		while (appropriateEntry == false && retry < 10) {
			setExit();
			if (appropriateExit()) {
				appropriateEntry = true;
			}
			retry++;

		}

		// GENERATE MAZE IF appropriateEntry IS TRUE
		if (appropriateEntry == false) {
			this.maze[0][0] = UNVISITED;
		} else {
			// SET UP OBSTACLES
			setObstacles(obstacles);
			hideBFS();
		}
		return this.maze;
	}


	/**
	 * Gets the height of the maze
	 * 
	 * @return The height of the maze
	 */
	public int getHeight() {
		return this.height;
	}

	/**
	 * Gets the width of the maze
	 * 
	 * @return The width of the maze
	 */
	public int getWidth() {
		return this.width;
	}

	/**
	 * Gets the row of the ENTRY_TILE
	 * 
	 * @return The row of the ENTRY_TILE
	 */
	public int getEntryRow() {
		return this.entryRow;
	}

	/**
	 * Gets the column of the ENTRY_TILE
	 * 
	 * @return The column of the ENTRY_TILE
	 */
	public int getEntryCol() {
		return this.entryCol;
	}

	/**
	 * Gets the row of the EXIT_TILE
	 * 
	 * @return The row of the EXIT_TILE
	 */
	public int getExitRow() {
		return this.exitRow;
	}

	/**
	 * Gets the column of the EXIT_TILE
	 * 
	 * @return The column of the EXIT_TILE
	 */
	public int getExitCol() {
		return this.exitCol;
	}

	/**
	 * Gets the content of a specific cell in the 2D array (maze)
	 * 
	 * @param row
	 *            The row of the 2D array
	 * @param col
	 *            The column of the 2D array
	 * @return The content of the cell
	 */
	public int getContent(int row, int col) {
		return this.maze[row][col];
	}

	/**
	 * Gets the coordinates of the exit cell in the 2D array.
	 * 
	 * @return The exit cell coordinates.
	 */
	public Point2D getExitCoords() {
		return new Point2D.Double(this.exitCol, this.exitRow);
	}
	
	// #########################################################
	// #                	PRIVATE METHODS            		   #
	// #########################################################
	
	/**
	 * Sets an entry point in the maze
	 */
	private void setEntry() {
		// STARTING POINT IS AT THE CENTRE OF THE LEFT MOST COLUMN
		this.entryRow = this.height / 2;
		this.entryCol = 0;

		this.maze[this.entryRow][this.entryCol] = ENTRY_TILE;
		this.maze[this.entryRow][this.entryCol + 1] = PATH_TILE;
	}
	
	/**
	 * Checks whether the maze still has an unvisited place.
	 * 
	 * @return The result of whether the maze has an unvisited place or not.
	 */
	private boolean hasUnvisitedCell() {
		boolean empty = false;
		for (int row = 0; row < this.height && empty == false; row++) {
			for (int col = 0; col < this.width && empty == false; col++) {
				if (getContent(row, col) == UNVISITED) {
					empty = true;
				}
			}
		}
		return empty;
	}

	/**
	 * Checks whether the maze still has an unvisited place for the BFS path
	 * @return The result of whether the maze has an unvisited place or not.
	 */
	private boolean hasUnvisitedBFSCell() {
		boolean empty = false;
		for (int row = 0; row < this.height && empty == false; row++) {
			for (int col = 0; col < this.width && empty == false; col++) {
				if (getContent(row, col) == PATH_TILE) {
					empty = true;
				}
			}
		}
		return empty;
	}

	/**
	 * Shows the BFS Path from the Entry Point. The steps start with 
	 * 10 to differentiate from the other Tile such as Wall (2)
	 * 
	 */
	private void showBFS() {
		int steps = 10;
		int row = getEntryRow();
		int col = getEntryCol();
		Queue<MazeNode> q = new LinkedList<MazeNode>();

		while (hasUnvisitedBFSCell() || q.peek() != null) {
			if (row - 1 >= 0 && getContent(row - 1, col) == PATH_TILE) {
				MazeNode node = new MazeNode(row, col, row - 1, col);
				q.add(node);
			}
			if (row + 1 < height - 1 && getContent(row + 1, col) == PATH_TILE) {
				MazeNode node = new MazeNode(row, col, row + 1, col);
				q.add(node);
			}
			if (col - 1 >= 0 && getContent(row, col - 1) == PATH_TILE) {
				MazeNode node = new MazeNode(row, col, row, col - 1);
				q.add(node);
			}
			if (col + 1 < width - 1 && getContent(row, col + 1) == PATH_TILE) {
				MazeNode node = new MazeNode(row, col, row, col + 1);
				q.add(node);
			}

			if (q.peek() != null) {
				MazeNode retrieved = q.poll();
				row = retrieved.getCurrentRow();
				col = retrieved.getCurrentCol();
				if (getContent(retrieved.getParentRow(),
						retrieved.getParentCol()) == ENTRY_TILE) {
					this.maze[row][col] = steps + 1;
				} else {

					this.maze[row][col] = getContent(retrieved.getParentRow(),
							retrieved.getParentCol()) + 1;
				}
			} else {
				break;
			}
		}

		if (hasUnvisitedBFSCell()) {
			this.maze[0][0] = 0;
		}
	}

	/**
	 * Hides the BFS Path from the Entry Point
	 */
	private void hideBFS() {
		for (int row = 0; row < this.height; row++) {
			for (int col = 0; col < this.width; col++) {
				if (getContent(row, col) > 10) {
					this.maze[row][col] = PATH_TILE;
				}
			}
		}
	}

	/**
	 * Checks if the number of steps between the entry point and exit point
	 * is appropriate to prevent a very easy maze generated.
	 * @return if the exit is appropriately placed or not
	 */
	private boolean appropriateExit() {
		int row = getExitRow();
		int col = getExitCol();
		boolean appropriate = false;
		if (row == 0) {
			row++;
		} else if (row == (height - 1)) {
			row--;
		}
		if (col == 0) {
			col++;
		} else if (col == (width - 1)) {
			col--;
		}
		if (getContent(row, col) > (Math.sqrt(this.height * this.width) * 3)) {
			appropriate = true;
		}
		return appropriate;
	}

	/**
	 * Sets an exit point in the maze
	 */
	private void setExit() {
		this.maze[this.exitRow][this.exitCol] = WALL_TILE;
		this.maze[this.entryRow][this.entryCol] = ENTRY_TILE;
		Random random = new Random();
		int side = random.nextInt(4);
		if (side == 0) {
			this.exitRow = (random.nextInt(this.height / 2 - 1) * 2) + 1;
			this.exitCol = this.width - 1;
		} else if (side == 1) {
			this.exitRow = (random.nextInt(this.height / 2 - 1) * 2) + 1;
			this.exitCol = 0;
		} else if (side == 2) {
			this.exitRow = 0;
			this.exitCol = (random.nextInt(this.width / 2 - 1) * 2) + 1;
		} else {
			this.exitRow = this.height - 1;
			this.exitCol = (random.nextInt(this.width / 2 - 1) * 2) + 1;
		}

		this.maze[this.exitRow][this.exitCol] = EXIT_TILE;
	}

	/**
	 * Sets the height of the maze
	 * 
	 * @param height
	 *            The height of the maze
	 */
	private void setHeight(int height) {
		this.height = height;
	}

	/**
	 * Sets the width of the maze
	 * 
	 * @param width
	 *            The width of the maze
	 */
	private void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Initialises the maze with walls on index which has an even number or
	 * unvisited cell for other indexes
	 */
	private void setMaze() {
		this.maze = new int[this.height][this.width];
		assert ((this.height % 2) != 0);
		assert ((this.width % 2) != 0);
		for (int row = 0; row < this.height; row++) {
			for (int col = 0; col < this.width; col++) {
				if (row % 2 == 0 || col % 2 == 0) {
					this.maze[row][col] = WALL_TILE;
				} else {
					this.maze[row][col] = UNVISITED;
				}
			}
		}
	}

	/**
	 * Sets the obstacles in the maze
	 * 
	 * @param obstacles
	 *            The number of obstacles in the maze
	 */
	private void setObstacles(int obstacles) {
		Random random = new Random();
		int counter = 0;
		int row = 0;
		int col = 0;

		while (counter < obstacles) {
			row = random.nextInt(this.height);
			col = random.nextInt(this.width);

			if (getContent(row, col) == WALL_TILE && row != 0
					&& row != this.height - 1 && col != 0
					&& col != this.width - 1) {
				this.maze[row][col] = OBSTACLE;
				counter++;
			}
		}
	}

	/**
	 * Sets the loops in the maze
	 * 
	 * @param loops
	 *            The number of loops in the maze
	 */
	private void setLoops(int loops) {
		Random random = new Random();
		int counter = 0;
		int row = 0;
		int col = 0;

		while (counter < loops) {
			row = random.nextInt(this.height);
			col = random.nextInt(this.width);

			if (getContent(row, col) == WALL_TILE && row != 0
					&& row != this.height - 1 && col != 0
					&& col != this.width - 1) {
				this.maze[row][col] = PATH_TILE;
				counter++;
			}
		}
	}

	// #########################################################
	// #                	PRIVATE FIELDS            		   #
	// #########################################################
	
	private int entryRow;
	private int entryCol;
	private int exitRow;
	private int exitCol;
	private int height;
	private int width;
	private int[][] maze;

	// #########################################################
	// #                		CONSTANTS                      #
	// #########################################################
	
	private final static int UNVISITED = Tile.UNVISITED.getValue();
	private final static int PATH_TILE = Tile.PATH_TILE.getValue();
	private final static int WALL_TILE = Tile.WALL_TILE.getValue();
	private final static int ENTRY_TILE = Tile.ENTRY_TILE.getValue();
	private final static int EXIT_TILE = Tile.EXIT_TILE.getValue();
	private final static int OBSTACLE = Tile.OBSTACLE_TILE.getValue();
}

