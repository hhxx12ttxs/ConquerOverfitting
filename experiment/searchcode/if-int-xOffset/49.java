import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Point;
/**
 * @author Alex Beach
 *
 */
public class Maze {
	private String fileName; // name of maze bmp
	private BufferedImage image; // stores the image
	private int height; // height of the image
	private int width; // width of the image
	private Node[][] openList; // indexed array, square i,j on grid of maze
	public Node[][] closedList; // always goes in spot i,j in the array
	public static int startx; // starting location on grid
	public static int starty; // starting location on grid
	public static int targetx; // target x on grid
	public static int targety; // target y on grid
	
	public static int colorTolerance = 0;
	
	public static int downHillScale = 5;
	public static int upHillScale = 100;
	
	private boolean startSet = false;
	private boolean targetSet = false;
	
	public int[] restrictedColors; 	
	
	
	public int gridWidth;
	public int gridHeight;
	
	public int heuristic; 

	int SQUARE; // defines the size of each square in the grid
	static final int BLACK = Color.BLACK.getRGB();
	static final int WHITE = Color.WHITE.getRGB();
	static final int RED = Color.RED.getRGB();
	static final int BLUE = Color.BLUE.getRGB();
	static final int GREEN = Color.green.getRGB();
	static final int CYAN  = Color.cyan.getRGB();

	public static int MANHATTAN = 0;
	public static int DIAGONAL = 1;
	public static int EUCLIDEAN = 2;

	public Maze(int x, int y, int xt, int yt, int size, String file)
			throws Exception {
		this.startx = x;
		this.starty = y;
		this.targetx = xt;
		this.targety = yt;

		SQUARE = size;

		loadImage(file);

		gridWidth = width / SQUARE;
		gridHeight = height / SQUARE;

		openList = new Node[gridWidth][gridHeight];
		closedList = new Node[gridWidth][gridHeight];
	}
	//Sets the heuristic mode
	public void setHeuristic(int h)
	{
		heuristic = h;
	}
	

	//sets the start of the maze
	public void setStart(Point start)
	{
		startx = (int) start.getX();
		starty = (int) start.getY();
		startSet = true;
	}
	
	//sets the end of the maze	
	public void setTarget(Point end)
	{
		targetx = (int) end.getX();
		targety = (int) end.getY();
		targetSet = true;
	}

	/**
	 * Takes a current node and an neighbor node that is not in the openlist
	 * then it determines which heuristic distance to use based on the location 
	 * of the neighbor.  i and j enumerates which neigher based on  a 3x3 grid
	 * where 0,0 is the upper left most neighbor
	 * @param neightborx
	 * @param neightbory
	 * @param currentx
	 * @param currenty
	 * @param xoffset
	 * @param yoffsetj
	 */
	public void addNeighborToSet(int neightborx, int neightbory, int currentx,
			int currenty, int xoffset, int yoffsetj) {

		//If 2 divides i+j or i + j == 0 then it is a diagonal
		if (xoffset + yoffsetj == 0 || (xoffset + yoffsetj) % 2 == 0)
			addToOpenList(neightborx, neightbory, currentx, currenty, Node.DIAGONAL_COST);
		else
			addToOpenList(neightborx, neightbory, currentx, currenty, Node.FORWARD_COST);
	}

	/**
	 * Takes a position
	 * 
	 * @param xloc
	 * @param yloc
	 *            Whenever a node is removed from openList is need to be put
	 *            into the closed list.
	 */
	public void addToClosedList(int xloc, int yloc) 
	{
		closedList[xloc][yloc] = openList[xloc][yloc];
		openList[xloc][yloc] = null;

	}
	
	/**
	 * Resets the maze so that it can be solved again
	 * @param clearTargets
	 * @throws Exception
	 */
	public void reset(boolean clearTargets) throws Exception
	{
		closedList = new Node[width][height];
		openList = new Node[width][height];
		loadImage(fileName);	
	}
	
	/**
	 * retrusn the image with a square in CYAN at the startpoint
	 * @return
	 */
	public BufferedImage drawStart()
	{
		
		for(int i = -1; i <= 1; i++)
		{
			for(int j = -1; j <= 1; j++)
			{
				if(startSet && !pixelOutofBounds(startx + i, starty + j))
				image.setRGB(startx + i, starty + j, CYAN);
				
				
			}
		}
		return image;
		
	}
	/**
	 * returns the image with a square in GREEN at the targetpoint
	 * @return
	 */
	public BufferedImage drawTarget()
	{
		
		for(int i = -1; i <= 1; i++)
		{
			for(int j = -1; j <= 1; j++)
			{
				if(targetSet && !pixelOutofBounds(targetx + i, targetx + j))				
				image.setRGB(targetx + i, targety + j, GREEN);
			}
		}
		return image;
		
	}
	
	
	/**
	 * Gets the move cost between two nodes, if they are the same it returns 0
	 * @param currentx
	 * @param currenty
	 * @param x
	 * @param y
	 * @return
	 */
	public int moveCost(int currentx, int currenty, int x, int y)
	{
		int difference =  image.getRGB(x, y) - image.getRGB(currentx, currenty) ;
		double temp = difference*.0001;
		//Temp holds a factor of the difference
		
		//if down hill
		if(temp < 0)
		{
			difference = (int) Math.abs((temp / Node.DIAGONAL_COST )) * downHillScale;
		}
		else if(temp > 0)
		difference = (int) ((temp /  Node.DIAGONAL_COST) * upHillScale);		
		
		
		//returns the elevation cost
		return difference;		
	}
	

	/**
	 * Creates a new node object and adds it to the l.  The fscore is then claculated
	 * for each node being added by this function
	 * @param x        x coord of node
	 * @param y        y coord of node
	 * @param currentx x coord of selected node
	 * @param currenty y coord of selected node
	 * @param movemenType  either diagonal(14) or horizonal/vertical(10)
	 */
	public void addToOpenList(int x, int y, int currentx, int currenty,
			int movemenType) {
		openList[x][y] = new Node(x, y, heuristic);
		openList[x][y].setParents(currentx, currenty);
		openList[x][y].moveCost = getMoveCost(currentx, currenty) + movemenType+		
		moveCost(currentx, currenty, x,y);
		openList[x][y].setFscore();
	}

	/**
	 * This function looks at all the pixels that fall on the lines between
	 * the center Node and its neighbors.  If a white pixel is encountered 
	 * along on of those lines, then that neighbor can't be traveled to.
	 * 
	 * @param x current node x coordinate
	 * @param y current node y coordinate
	 * @return
	 * @throws IOException
	 */
	public boolean[][] cantTravel(int x, int y)  {
		//center is the current, the other parts of this boolean array
		//are test to see if if that grid space can't be reached without
		//touching a white pixel
		boolean[][] cantTravel = new boolean[3][3];
		//converts from grid x and y to pixel x and y
		int currentx = (x * SQUARE) + SQUARE / 2;
		int currenty = (y * SQUARE) + SQUARE / 2;
		//temp. variables for storing the center pixel locations
		//of all neighbors
		int x1, y1;
		int x2, y2;
		//searches every neighbor
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {

				x1 = currentx;
				y1 = currenty;
				
				if( heuristic == 0 )
				if(i + j == 0 || (i + j) % 2 == 0)
					cantTravel[i][j] = true;

				//converts form i and j to x and y grid coordinates
				int[] Neighbors = getNeighborCoord(x1, y1, i, j, SQUARE);

				x2 = Neighbors[0];
				y2 = Neighbors[1];

				if (pixelOutofBounds(x2, y2))
					cantTravel[i][j] = true;
				else {
					//looks at all pixels from one center to another
					//and if there is a white one, then that position can;t
					//be traveled to
					
					endloop:
					while (x1 != x2 || y1 != y2) {

						
						for(int p = 0; p < restrictedColors.length; p++)
						{
							int difference = Math.abs(image.getRGB(x1, y1) - restrictedColors[p]);
							
							if (difference <= colorTolerance ) 
							{
								cantTravel[i][j] = true;
								break endloop;
							}
						}

						if (x1 != x2)
							if (x1 > x2)
								x1--;
							else
								x1++;

						if (y1 != y2)
							if (y1 > y2)
								y1--;
							else
								y1++;
					}
					
				}
			}
		}
		return cantTravel;
	}

	
	/**
	 * Sets the restricted colors from int [] of RGB 
	 * @param colors
	 */
	public void restrictedColors(int[] colors)
	{
		restrictedColors = colors.clone();
	}
	
	//Returns the current state of the maze
	public BufferedImage getImage()
	{
		return image;
	}

	/**
	 * This function draws the solution by travering all of the parents from the
	 * target location
	 * @throws IOException for call to writetoImage which writes to the file
	 * 
	 */
	public void drawSolution(boolean writeToFile) throws IOException {

		int x = targetx;
		int y = targety;
		//temp variables for comparing pixel locations
		int x1, y1;
		int x2, y2;

		int parents[];

		for (; x != startx || y != starty;) {

			parents = getParent(x, y);

			//convert from grid coord. to pixel coord
			x1 = (x * SQUARE) + SQUARE / 2;
			y1 = (y * SQUARE) + SQUARE / 2;
			
			//getting the distance to the parent node
			x2 = (parents[0] * SQUARE) + SQUARE / 2;
			y2 = (parents[1] * SQUARE) + SQUARE / 2;

			//traversing all of the pixels and drawing the path in red
			while (x1 != x2 || y1 != y2) {	

				image.setRGB(x1, y1, RED);

				if (x1 != x2)
					if (x1 > x2)
						x1--;
					else
						x1++;

				if (y1 != y2)
					if (y1 > y2)
						y1--;
					else
						y1++;
			}

			//updates parents
			x = parents[0];
			y = parents[1];

		}

		this.drawStart();
		this.drawTarget();
		if(writeToFile)
		writeToImage("NewMaze.bmp");

	}

	/**
	 * returns the fscore for an element of the openlist
	 * @param x
	 * @param y
	 * @return
	 */
	public double getFscore(int x, int y) 
	{
		return openList[x][y].Fscore;

	}

	
	/**
	 * returns the accumlated move cost of each the specified node
	 * @param x
	 * @param y
	 * @return
	 */
	public double getMoveCost(int x, int y) {
		if (closedList[x][y] != null)
			return closedList[x][y].moveCost;
		if (openList[x][y] != null)
			return openList[x][y].moveCost;
		return 0;

	}

	/**
	 * the Node at currentx, and currenty is surrounded by 8 nodes and is seen
	 * as a  3x3 array[i][j] where 0,0 is at the top left. This function
	 * returns the x and y coord of the neighbor based on the current node, ints
	 * xoffset and yoffset. sizeOfBlock determines how to represent each block, ie, as a Node
	 * or pixels depending on the size of each square
	 * 
	 * @param currentx
	 * @param currenty
	 * @param xoffset
	 * @param yoffset
	 * @param sizeOfBlock
	 * @return vector of coord x, y
	 */
	public int[] getNeighborCoord(int currentx, int currenty, int xoffset, int yoffset,
			int sizeOfBlock) {
		int[] NeighborCoord = { 0, 0 };

		//For x coordinate of neighbor
		if ((xoffset - 1) < 0)
			NeighborCoord[0] = currentx - sizeOfBlock;
		else if ((xoffset - 1) > 0)
			NeighborCoord[0] = currentx + sizeOfBlock;
		else
			NeighborCoord[0] = currentx;

		//For y coordinate of neighbor
		if ((yoffset - 1) < 0)
			NeighborCoord[1] = currenty - sizeOfBlock;
		else if ((yoffset - 1) > 0)
			NeighborCoord[1] = currenty + sizeOfBlock;
		else
			NeighborCoord[1] = currenty;

		return NeighborCoord;

	}

	/**
	 * Returns the parent of a node that can either be
	 * in the closed list or open list
	 * @param x
	 * @param y
	 * @return
	 */
	public int[] getParent(int x, int y) {
		int[] toreturn = { 0, 0 };
		if (closedList[x][y] != null) {
			toreturn[0] = closedList[x][y].parentX;
			toreturn[1] = closedList[x][y].parentY;
		}
		if (openList[x][y] != null) {
			toreturn[0] = openList[x][y].parentX;
			toreturn[1] = openList[x][y].parentY;
		}
		return toreturn;
	}

	/**
	 * This function looks at a given location on the square grid and determines
	 * if if the square has white in the center, otherwise, it could be part of
	 * a valid path
	 * 
	 * @param xloc
	 *            is the horizontal square on grid
	 * @param yloc
	 *            is the vertical square on grid
	 * @return returns true if there is no obstruction in the middle of the
	 *         square other wise it returns false
	 */
	public boolean isWalkable(int xloc, int yloc) {

		
		for(int p = 0; p < restrictedColors.length; p++)
		{
			int difference = Math.abs(image.getRGB(xloc, yloc) - restrictedColors[p]);
			
			if (difference > colorTolerance ) 
			{
				System.out.println(image.getRGB(xloc, yloc));
				System.out.println(restrictedColors[p]);
				return true;
				
			}
		}
		return false;
	}

	/**
	 * This function takes an .bmp image file name and loads the image into the
	 * global BufferedImage image
	 * 
	 * @param file
	 * @throws Exception
	 */
	public boolean loadImage(String file) throws Exception {
		// The image is read, but it is in greyScale, we want a TYP_INT_RGB
		// image
		fileName = file;
		File input = new File(file);

		if (!input.exists())
			return false;

		BufferedImage greyScale = ImageIO.read(input);
		height = greyScale.getHeight();
		width = greyScale.getWidth();

		// A new image is created that is type RGB
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		// All pixels are copied
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				image.setRGB(i, j, greyScale.getRGB(i, j));

		System.out.println("The width is: " + width + " pixels" + '\n'
				+ "The height is: " + height + " pixels");
		return true;
	}

	/**
	 * This function takes and x and y grid cord and a boolean array of invalid moves
	 * and returns the neighbor with the lowest f score.  If no neighbor can be found, 
	 * The openlist is then traversed by a method call for the smallest fscore.
	 * @param x
	 * @param y
	 * @param notValid
	 * @return
	 */
	public int[] lowestNeighbor(int x, int y, boolean[][] notValid) {
		int[] toreturn = new int[4];
		double lowest = Integer.MAX_VALUE;

		//for all neighbors
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				{
					//get neighbors in grid coord
					int[] Neighbors = getNeighborCoord(x, y, i, j, 1);

					//if it is valid, then compare fscores
					if (!notValid[i][j] && (i != 1 || j != 1)) {

						if (closedList[Neighbors[0]][Neighbors[1]] == null
								&& openList[Neighbors[0]][Neighbors[1]].Fscore < lowest) {

							lowest = openList[Neighbors[0]][Neighbors[1]].Fscore;
							toreturn[0] = Neighbors[0];
							toreturn[1] = Neighbors[1];
						}
					}
				}
			}
		}

		//if the lowest is not found, then the openlist must be traversed
		if (toreturn[0] == 0 && toreturn[1] == 0) {
			int search[] = searchAll();
			toreturn[0] = search[0];
			toreturn[1] = search[1];
		}
		//Also retursn parent information
		toreturn[2] = getParent(toreturn[0], toreturn[1])[0];
		toreturn[3] = getParent(toreturn[0], toreturn[1])[1];
		return toreturn;
	}

	/**
	 * Takes a maze.bmp file and saves it as a new .bmp file with a grid
	 * 
	 * @throws IOException because of writeToImage
	 */
	public void makeGrid() throws IOException {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {

				if (i % SQUARE == 0 || j % SQUARE == 0) {
					Color temp = new Color(0, 120, 255);
					int rgb = temp.getRGB();
					image.setRGB(i, j, rgb);
				}
			}
		}

		writeToImage("grid.bmp");
	}



	/**
	 * returns true when the specified pixel is out of bounds
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean pixelOutofBounds(int x, int y) {
		return (x < 0 || y < 0 || x >= width || y >= height);
	}

	
	/**
	 * Calls printNode() on everyNode on the closedlist
	 */
	public void printClosed() {
		System.out.println("ClosedList======");
		for (int i = 0; i < gridWidth; i++) {
			for (int j = 0; j < gridHeight; j++) {
				if (closedList[i][j] != null)
					closedList[i][j].printNode();
			}
		}
		System.out.println("ClosedList======");
	}

	/**
	 * Calls printNode() on everyNode on the openList
	 */
	public void printOpen() {

		System.out.println("OpenList======");
		for (int i = 0; i < gridWidth; i++) {
			for (int j = 0; j < gridHeight; j++) {
				if (openList[i][j] != null)
					openList[i][j].printNode();
			}
			System.out.println("OpenList======");
		}
	}

	/**
	 * Searches every element in the openlist to find the lowest.
	 * This is only called by lowestNeighbor() if a neighbor can't be found
	 * retursn x and y loction as a 2d array 
	 * @return
	 */
	public int[] searchAll() {
		int[] toreturn = new int[2];
		double lowest = Integer.MAX_VALUE;
		int tempx = 0, tempy = 0;

		for (int i = 0; i < gridWidth; i++) {
			for (int j = 0; j < gridHeight; j++) {
				//only looks at valid nodes
				if (openList[i][j] != null && openList[i][j].Fscore < lowest) {
					lowest = openList[i][j].Fscore;
					tempx = i;
					tempy = j;
				}
			}
		}

		toreturn[0] = tempx;
		toreturn[1] = tempy;

		return toreturn;

	}

	/**
	 * Checks to see if the movement cost for a neighbor would be less
	 * if its parent was changed to the current node.  If so the parents
	 * are changed accordingly. ints i and j indicated which neighbor
	 * @param currentx
	 * @param currenty
	 * @param neighborx
	 * @param neighbory
	 * @param i enumerations of neighbor
	 * @param j
	 */
	public void setBetterPath(int currentx, int currenty, int neighborx, int neighbory,
			int i, int j) {
		double newG = openList[currentx][currenty].moveCost;

		if (i + j == 0 || (i + j) % 2 == 0)
			newG += Node.DIAGONAL_COST;
		else
			newG += Node.FORWARD_COST;
		
		newG += moveCost(currentx, currenty, neighborx,neighbory);

		if (newG < getMoveCost(neighborx, neighbory)) {
			openList[neighborx][neighbory].parentX = currentx;
			openList[neighborx][neighbory].parentY = currenty;
			openList[neighborx][neighbory].moveCost = newG;
			openList[neighborx][neighbory].setFscore();
		}
	}

	/**
	 * This fucntion adds and removes elements from the openlist and adds elements to the
	 * closed list based on the node and all its neighbors.  this function terminates when
	 * the openlist is empty or when the target is in the closed list
	 * 
	 */
	public boolean solzeMaze() throws IOException {
		boolean[][] notValidMoves;

		
		int currentx = startx;
		int currenty = starty;

		//adds start node to the openlist
		addToOpenList(currentx, currenty, currentx, currenty, 0);
	
		int count = 0;
		while (closedList[targetx][targety] == null) {

			if(count > 500000)
				return false;
			
		
			count++;
			
			notValidMoves = cantTravel(currentx, currenty);
			//checks all neighbors enumerated by i and j
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {

					//gets grid coord of neighbors
					int[] Neighbors = getNeighborCoord(currentx, currenty, i,
							j, 1);

					
					if (notValidMoves[i][j] == false) {
						//If the neighbor is empty, then it is added to the openlist
						if (openList[Neighbors[0]][Neighbors[1]] == null
								&& closedList[Neighbors[0]][Neighbors[1]] == null) {

							addNeighborToSet(Neighbors[0], Neighbors[1],
									currentx, currenty, i, j);

							//if the neighbor is in the openlist, then a call is made to determin if there is abetter path
						} else if (openList[Neighbors[0]][Neighbors[1]] != null) {
							if (Neighbors[0] != currentx
									|| Neighbors[1] != currenty) {

								setBetterPath(currentx, currenty, Neighbors[0],
										Neighbors[1], i, j);
							}
						}
					}
				}
			}

			//current node is added to closedList which also removes it from openlist
			addToClosedList(currentx, currenty);
			int[] lowest = lowestNeighbor(currentx, currenty, notValidMoves);			
		
			//current node is updated
			currentx = lowest[0];
			currenty = lowest[1];

		}
		return true;
	}

	/**
	 * Writes image to specified file
	 * @param File
	 * @throws IOException
	 */
	public void writeToImage(String File) throws IOException {
		File output = new File(File);
		boolean t = ImageIO.write(image, "bmp", output);
	}

}
