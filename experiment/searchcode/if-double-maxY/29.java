package carsproject.general;

/**
 * This class contains methods used to store and manage coordinates.
 * This store X and Y values in Double variables but return Integer values.
 * This because they are wrote thinking to pixel coordinates
 * that can't be double but only integer, otherwise they are stored as double
 * to have more precision using addX() and addY() methods
 * and then round values when using getX() and getY().
 * This class got also two max values for both X and Y than can be set and all
 * "get" methods will throw IllegalArgumentException if values that should be
 * returned are higher than those max values or lower than zero.
 * 
 * @author Masini Gioele
 *
 */
public class Coordinates {
	
	/*		 VARIABLES 		*/
	private Double x;
	private Double y;
	private static Double maxX = Double.MAX_VALUE;
	private static Double maxY = Double.MAX_VALUE;
	
	/** Value of a coordinates' value out of map. */
	public static final Double OUT_OF_MAP = -1.0;

	
	/*		 CONSTRUCTORS 		*/
	/**
	 * Coordinates base Integer constructor. It accept also values not legal that means that the point is out of map.
	 * 
	 * @param x coordinate x
	 * @param y coordinate y
	 */
	public Coordinates(final int x, final int y) {
		this.x = (double) x;
		this.y = (double) y;
	}
	
	/**
	 * Coordinates base Double constructor. It accept also values not legal that means that the point is out of map.
	 * 
	 * @param x coordinate x
	 * @param y coordinate y
	 */
	public Coordinates(final double x, final double y) {
		this.x = x;
		this.y = y;
	}
	
	/*			   X    		*/
	/**
	 * Return value of coordinate X.
	 * 
	 * @return value of coordinate X
	 * @throws IllegalArgumentException if value is more than maxX or less than zero
	 */
	public int getX() throws IllegalArgumentException {
		Coordinates.checkX(this.x);
		return Coordinates.doubleToInteger(this.x);
	}
	
	/**
	 * Set Integer value of coordinate X. It accept also values not legal that means that the point is out of map.
	 * 
	 * @param x value to be set
	 */
	public void setX(final int x) {
		this.x = (double) x;
	}
	
	/**
	 * Set Double value of coordinate X. It accept also values not legal that means that the point is out of map.
	 * 
	 * @param x value to be set
	 */
	public void setX(final double x) {
		this.x = x;
	}
	
	/**
	 * Add value to coordinate X. It accept also values not legal that means that the point is out of map.
	 * 
	 * @param adder value to be added
	 */
	public void addX(final double adder) {
		this.x += adder;
	}
	
	/*			   Y    			*/
	/**
	 * Return value of coordinate Y
	 * 
	 * @return value of coordinate Y
	 * @throws IllegalArgumentException if value is more than maxY or less than zero
	 */
	public int getY() throws IllegalArgumentException {
		Coordinates.checkY(this.y);
		return Coordinates.doubleToInteger(this.y);
	}
	
	/**
	 * Set Integer value to coordinate Y. It accept also values not legal that means that the point is out of map.
	 * 
	 * @param y value to be set
	 */
	public void setY(final int y) {
		this.y = (double) y;
	}
	
	/**
	 * Set Double value to coordinate Y. It accept also values not legal that means that the point is out of map.
	 * 
	 * @param y value to be set
	 */
	public void setY(final double y) {
		this.y = y;
	}
	
	/**
	 * Add value to coordinate Y. It accept also values not legal that means that the point is out of map.
	 * 
	 * @param adder value to be added
	 */
	public void addY(final double adder) {
		this.y += adder;
	}
	
	
	/*		 MAXIMUMS 		*/
	/**
	 * Set maximum value for coordinate X
	 * 
	 * @param maxX maximum value to be set
	 */
	public static void setMaxX(final double maxX) {
		Coordinates.maxX = maxX;
	}
	
	/**
	 * Return maximum value for coordinate X. If not set will return Double.MAX_VALUE.
	 * 
	 * @return value of maxX
	 */
	public static Double getMaxX() {
		return Coordinates.maxX;
	}
	
	/**
	 * Set maximum value for coordinate Y.
	 * 
	 * @param maxY maximum value to be set
	 */
	public static void setMaxY(final double maxY) {
		Coordinates.maxY = maxY;
	}
	
	/**
	 * Return maximum value for coordinate X. If not set will return Double.MAX_VALUE.
	 * 
	 * @return value of maxY
	 */
	public static Double getMaxY() {
		return Coordinates.maxY;
	}
	
	
	/*			 VALUE CONTROLS 			*/
	
	/**
	 * Check if value is legal (inferior than maxX value or less than zero)
	 * 
	 * @param value value to check
	 * @throws IllegalArgumentException if value is more than maxY or less than zero
	 */
	private static void checkX(final double value) throws IllegalArgumentException {
		if (value > Coordinates.maxX || value < 0) {
			//System.err.println("Error X value: " + value);
			throw new IllegalArgumentException() {
				private static final long serialVersionUID = 1L;
				public String getMessage() {
					return "X value off limits.";
				}
			};
		}
	}
	
	/**
	 * Check if value is legal (inferior than maxY value or less than zero).
	 * 
	 * @param value value to check
	 * @throws IllegalArgumentException if value is more than maxY or less than zero
	 */
	private static void checkY(final double value) throws IllegalArgumentException {
		if (value > Coordinates.maxY || value < 0) {
			//System.err.println("Error Y value: " + value);
			throw new IllegalArgumentException() {
				private static final long serialVersionUID = 1L;
				public String getMessage() {
					return "Y value off limits.";
				}
			};
		}
	}
	
	/**
	 * Check if a coordinate is legal (inside the map). If it is this return true, false otherwise.
	 * 
	 * @param point point to be checked
	 * @return true if legal, false otherwise
	 */
	public static boolean isLegal(final Coordinates point) {
		try {
			Coordinates.checkX(point.getX());
			Coordinates.checkY(point.getY());
		} catch (IllegalArgumentException e) {
			return false;
		}
		return true;
		
	}
	
	
	/*			 OTHER METHODS 			*/
	
	/**
	 * Round from Double to Integer and return it. Method wrote to have code more readable.
	 * 
	 * @param value double to be rounded
	 * @return value rounded to integer
	 */
	public static Integer doubleToInteger(final double value) {
		return Math.round(Math.round(value));
	}
	
	/**
	 * Calculate and return distance from two given points of type Coordinates.
	 * 
	 * @param p1 first point
	 * @param p2 second point
	 * @return distance between p1 and p2
	 * @throws NullPointerException if p1 or p2 are null
	 */
	public static Double distanceFromTwoPoints(final Coordinates p1, final Coordinates p2) throws NullPointerException {
		if (p1 != null && p2 != null) {
			return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
		} else {
			throw new NullPointerException();
		}
	}
	
	/**
	 * Returns a personalized representation of this class.
	 * It use the form "(x, y)".
	 * 
	 * @return String representing the class
	 */
	public String toString() {
		try {
			return "(" + this.getX() + "," + this.getY() + ")";
		} catch (Exception e) {
			return "(Not valid values.)";
		}
	}
}

