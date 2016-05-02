package navigation;



import odometry.Odometer;

/**
 * A map has been written using the wavefront technique, which consists of mapping the entire field in a 2D array: a "wavefront grid".
 * Each number on the grid represents a 30' by 30' tile on the field. The system inserts numbers into the grid, in order to link the 
 * robot to its target locations, thereafter the robot will travel block per block following a pattern of number until it reaches the
 * goal. For more information, the method has been entirely based on the following URL and open source information.
 * 
 * 
 * http://www.societyofrobots.com/programming_wavefront.shtml
 * http://www.robotc.net/blog/2011/08/08/robotc-advanced-training/
 * http://www.mcs.alma.edu/LMICSE/LabMaterials/AlgoComp/Lab4/AlgCoL4.htm
 * https://code.google.com/p/mindstormsproject/source/browse/trunk/LejosProject/src/it/uniba/wavefront/GridWalker.java?r=5
 * https://code.google.com/p/mindstormsproject/source/browse/trunk/LejosProject/src/it/uniba/wavefront/Grid.java?r=6
 * 
 * @author Michael
 * 
 */
public class Map {
	
	private Odometer myOdometer;
	double tileWidth;
	public static final int NOTHING = 0;
	public static final int OBSTACLE = 1;
	public static final int GOAL = 1;
	public static final int ROBOT = 99;
	private double [] coordsX = new double[10];
	private double [] coordsY = new double[10];

	private int[][] grid = new int[][] {{1,0,0,1,1,0,0,0,0,1}, 
			   			       	{1,0,0,0,1,1,0,0,0,1}, 
			   			       	{1,0,0,0,0,0,0,0,0,1}, 
			   			       	{1,0,0,0,0,0,0,0,0,1}, 
			   			       	{1,0,0,0,0,0,0,0,0,1}, 
			   			       	{1,0,0,0,0,0,0,0,0,1}, 
			   			       	{1,0,0,0,0,0,0,0,0,1}, 
			   			       	{1,0,0,0,0,0,0,0,0,1}, 
			   			       	{1,0,0,0,0,0,0,0,0,1}, 
			   			       	{1,0,0,0,0,0,0,0,0,1}};
	
	/**
	 * Constructor which builds a wavefront grid
	 * @param odo The Odometer used to determine the robots displacement and position
	 * @param tileWidth The width of a tile in centimeters
	 */
	public Map(Odometer odo, double tileWidth){
		this.myOdometer = odo;
		this.tileWidth = tileWidth;
		
		this.tileWidth = tileWidth;
		double sumX = tileWidth/2;
		double sumY = tileWidth/2 + tileWidth*9;
		
		// build x coordinates of map
		for(int i=0;i<this.getCoordsX().length;i++){
			this.getCoordsX()[i] = sumX;
			sumX = sumX + this.tileWidth;
		}
		
		//build y coordinates of map
		for(int j=0;j<this.getCoordsY().length;j++){
			this.getCoordsY()[j] = sumY;
			sumY = sumY - this.tileWidth;
		}
	}
	
	/**
	 * @return The vertical coordinate I of the tile on which the robot currently reposes on according to the wavefront grid
	 */
	public int currentI(){
		double currentY = getClosest(getCoordsY(), myOdometer.getY());
		int currentI = 0;
		for(int j=0; j<getCoordsY().length; j++){
			double delta = 3.00;
			if(Math.abs(currentY-getCoordsY()[j]) < delta){
				currentI=j;
				break;
			}
		}
		return currentI;
	}
	
	/**
	 * @return The horizontal coordinate J of the tile on which the robot currently reposes on according to the wavefront grid
	 */
	public int currentJ(){
		double currentX = getClosest(getCoordsX(), myOdometer.getX());
		int currentJ = 0;
		for(int i=0; i<getCoordsX().length; i++){
			double delta = 3.00;
			if(Math.abs(currentX-getCoordsX()[i]) < delta){
				currentJ=i;
				break;
			}
		}
		return currentJ;
	}
	
	/**
	 * Convert an X coordinate to the corresponding horizontal component of the closest tile according to the wavefront grid
	 * @param destX The X coordinate of the target destination
	 * @return The horizontal J component of the closest tile to the input X coordinate according to the wavefront grid
	 */
	public int destJ(double destX){
		int destJ = 0;
		for(int i=0; i<getCoordsX().length; i++){
			double delta = 2.00; 
			if(Math.abs(destX-getCoordsX()[i]) <= delta){
				destJ = i;
				break;
			}
		}
		return destJ;
	}
	
	/**
	 * Convert an Y coordinate to the corresponding coordinate of the closest tile according to the wavefront grid
	 * @param destY The Y coordinate of the target destination
	 * @return vertical I component of the closest tile to the input Y coordinate according to the wavefront grid
	 */
	public int destI(double destY){
		int destI = 0;
		for(int j=0; j<getCoordsY().length; j++){
			double delta = 2.00; 
			if(Math.abs(destY-getCoordsY()[j]) <= delta){
				destI = j;
				break;
			}
		}
		return destI;
	}
	
	
	/**
	 * Convert the J component of a tile into the X coordiante of the center of the tile
	 * @param destJ The J component of the destination tile
	 * @return The X coordinate of the center of the destination tile
	 */
	public double destX(int destJ){
		return this.getCoordsX()[destJ];
	}
	
	/**
	 * Convert the I component of a tile into the Y coordinate of the center of the tile
	 * @param destI The I component of the destination tile
	 * @return The Y coordinate of the center of the destination tile
	 */
	public double destY(int destI){
		return this.getCoordsY()[destI];
	}
	
	/**
	 * Insert a value to a tile in the wavefront grid (e.g. 1 for obstacle, 99 for robot,  2 for destination, etc.)
     * @param i I component in the wavefront grid
     * @param j J component in the wavefront grid
	 * @param value
	 */
    private void insertValue(int i, int j, int value){
        getGrid()[i][j]= value;
    }

    /**
     * Check a value the value of a tile in the wavefront grid (e.g. 1 for obstacle, 99 for robot,  2 for destination, etc.)
     * @param i I component in the wavefront grid
     * @param j J component in the wavefront grid
     * @return
     */
    private int getValue(int i, int j) {
        if(i>=0 && i<10 && j>=0 && j<10)
                return getGrid()[i][j];
        else return -3;
    }
    
    /**
     * Compare a double with every value value in an array and return the closest one
     * @param array The array containing all the values we want to get closest
     * @param position The value from which we want to change to the closest
     * @return The closest value
     */
	private double getClosest(double[] array, double position) {
	    double lowestDiff = Double.MAX_VALUE;
	    double result = 0.0;
	    for (double i : array) {
	        double diff = Math.abs(position - i);
	        if (diff < lowestDiff) {
	            lowestDiff = diff;
	            result = i;
	        }
	    }
	    return result;
	}
	
	/**
	 * Check all the values of a two dimensional array and return the highest
	 * @param grid The two dimensional array we want to check
	 * @return The highest values
	 */
	private int getHighest(int[][] grid){
		int highest= 1;
		for(int i = 0; i < grid.length; i++) {
		    for(int j = 0; j < grid.length; j++){
		    	if(highest < grid[i][j] && grid[i][j]!= 99) {
			    	highest = grid[i][j];
		    	}
		    }
		}
		return highest;
	}
	
	/**
	 * Filter the wavefront grid for all impossible paths due to obstacles creating U shape
	 * @param grid The wavefront grid containing values of paths
	 * @return The filtered wavefront grid
	 */
	private int[][] filterMap(int[][] grid){
		return grid;
	}

	/**
	 * Accessor
	 * @return 2D array of integers representing the values in the wavefront grid
	 */
	public int[][] getGrid() {
		return grid;
	}

	/**
	 * Mutator
	 * @param grid The new grid which will be used by Navigator
	 */
	public void setGrid(int[][] grid) {
		this.grid = grid;
	}

	
	/**
	 * Accessor
	 * @return coordsX Array containing all the X coordinates of the center of every tile
	 */
	double [] getCoordsX() {
		return coordsX;
	}

	/**
	 * Mutator
	 * @param coordsX Array containing all the X coordinates of the center of every tile
	 */
	void setCoordsX(double [] coordsX) {
		this.coordsX = coordsX;
	}

	
	/**
	 * Accessor
	 * @return coordsY Array containing all the Y coordinates of the center of every tile
	 */
	double [] getCoordsY() {
		return coordsY;
	}

	/**
	 * Mutator
	 * @param coordsY Array containing all the Y coordinates of the center of every tile
	 */
	void setCoordsY(double [] coordsY) {
		this.coordsY = coordsY;
	}
}

