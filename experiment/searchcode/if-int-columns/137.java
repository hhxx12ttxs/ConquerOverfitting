import java.util.*;

/**
 * Contains all the cells that will be used.
 * @author Brook
 *
 */
public class CellWorld 
{
	private Cell cellWorld[][];
	private int rows; 
	private int columns;
	private int numAlive;	
	
	
	private static Shape DEFAULT = new Shape("Default", new int[][]{{5,6},{5,7},{5,8},{6,7},{4,7}});
	private static Shape GLIDER = new Shape("Glider", new int[][] {{1,0}, {2,1}, {2,2}, {1,2}, {0,2}});
	
	private static Shape GLIDERGUN = new Shape("Gosper Glider Gun", 
			new int[][] {{0,2}, {0,3}, {1,2}, {1,3}, {8,3}, {8,4}, {9,2}, {9,4}, {10,2},
			{10,3}, {16,4}, {16,5}, {16,6}, {17,4}, {18,5}, {22,1}, {22,2}, {23,0}, {23,2},
			{24,0}, {24,1}, {24,12}, {24,13}, {25,12}, {25,14}, {26,12}, {34,0}, {34,1}, {35,0},
			{35,1}, {35,7}, {35,8}, {35,9}, {36,7}, {37,8}});
	private static Shape SMALLEXPLODER = new Shape("Small Exploder", new int[][] {{0,1}, {0,2}, {1,0}, {1,1}, 
			{1,3}, {2,1}, {2,2}});
	private static Shape EXPLODER = new Shape("Exploder", new int[][] {{0,0}, {0,1}, {0,2}, {0,3}, 
			{0,4}, {2,0}, {2,4}, {4,0}, {4,1}, {4,2}, {4,3}, {4,4}});
	private static Shape CELL10 = new Shape("10CellRow", new int[][] {{0,0}, {1,0}, {2,0}, {3,0}, {4,0}, 
			{5,0}, {6,0}, {7,0}, {8,0}, {9,0}});
	private static Shape SHIP = new Shape("Ship", new int[][] {{0,1}, {0,3}, {1,0}, {2,0}, {3,0},
			{3,3}, {4,0}, {4,1}, {4,2}});
	private static Shape PUMP = new Shape("Pump", new int[][] {{0,3}, {0,4}, {0,5}, {1,0}, {1,1}, {1,5}, {2,0},
			{2,1}, {2,2}, {2,3}, {2,4}, {4,0}, {4,1}, {4,2}, {4,3}, {4,4}, {5,0}, {5,1}, {5,5}, {6,3}, {6,4}, {6,5}});
	private static final Shape[] SHAPES = new Shape[]{SHIP, PUMP, EXPLODER, SMALLEXPLODER, CELL10};
	
	public CellWorld(int rows, int columns)
	{
		cellWorld = new Cell[rows][columns];
		this.rows = rows;
		this.columns = columns;
		clearWorld();
		initLiveCells();
	}
	
	/**
	 * Clear the world of all live cells
	 */
	public void clearWorld()
	{
		for(int i = 0; i < rows; i++){
			for(int j = 0; j < columns; j++){
				cellWorld[i][j] = new Cell(false, i, j);				
			}
		}
		numAlive = 0; 
	}
	
	/**
	 * Default setup
	 */
	public void initLiveCells()
	{
		//test
		cellWorld[5][6].setIsAlive(true);
		cellWorld[5][7].setIsAlive(true);
		cellWorld[5][8].setIsAlive(true);
		cellWorld[6][7].setIsAlive(true);
		cellWorld[4][7].setIsAlive(true);
		numAlive = 5;
	}
	
	/**
	 * Adds some cells to the frame.
	 * @param cells The cells to add.
	 */
	public void addLiveCells(ArrayList<Cell>cells)
	{
		for(Cell cell : cells){
			int row = cell.getRow();
			int column = cell.getColumn();
			cellWorld[row][column].setIsAlive(true);
			numAlive++;
		}		
	}
	
	/**
	 * Removes some cells
	 * @param cells Cells to remove.
	 */
	public void removeCells(ArrayList<Cell>cells)
	{
		for(Cell cell : cells){
			int row = cell.getRow();
			int column = cell.getColumn();
			cellWorld[row][column].setIsAlive(false);
			numAlive--;
		}
		
	}
	
	/**
	 * Returns if the cell at the row and column location is alive.
	 * @param row Row location.
	 * @param column Column location.
	 * @return True if alive, false otherwise.
	 */
	public boolean isCellAlive(int row, int column)
	{
		return cellWorld[row][column].getIsAlive();
	}
	
	/** 
	 * Make the cell at the given location alive.
	 * @param row
	 * @param column
	 */
	public void setCellAlive(int row, int column)
	{
		if(row < 0 || row >= rows) return;
		if(column < 0 || column >= columns) return;
		cellWorld[row][column].setIsAlive(true);
		numAlive++;
	}
	
	/**
	 * Set the cell as dead.
	 * @param row 
	 * @param column
	 */
	public void setCellDead(int row, int column)
	{
		if(row < 0 || row >= rows) return;
		if(column < 0 || column >= columns) return;
		cellWorld[row][column].setIsAlive(false);
		numAlive--;
	}
	
	/**
	 * Get the number of live cells.
	 * @return
	 */
	public int getNumAlive()
	{
		return numAlive;
	}
	
	/**
	 * Add the glider shape.
	 */
	public void loadGlider()
	{
		clearWorld();
		//GLIDER.printShape();
		loadShape(GLIDER);
		
	}
	
	/**
	 * Load the glider gun
	 */
	public void loadGliderGun()
	{
		clearWorld();
		loadShape(GLIDERGUN);	
		//GLIDERGUN.printShape();
	}
	
	/**
	 * Load a random shape 
	 */
	public void loadRandomShape()
	{
		clearWorld();
		Random random = new Random();
		int index = random.nextInt(SHAPES.length);
		loadShape(SHAPES[index]);		
		
	}
	
	private void loadShape(Shape shape)
	{
		ArrayList<Cell>configuration = shape.getShape();		
		addLiveCells(configuration);
	}
}

