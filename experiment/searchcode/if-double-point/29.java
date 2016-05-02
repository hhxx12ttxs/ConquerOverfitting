import static java.lang.Math.sqrt;

/**
 * A two-dimensional point
 * @author David Pick
 *
 */
public class Point {
	private double x, y;
	
	/**
	 * Construct a point with these coordinates
	 * @param x the x coordainte
	 * @param y tye y coordinate
	 */
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	/**
	 * Returns a String representation of this Point
	 * @return a String representation of this Point
	 */
	@Override
	public String toString() {
		return String.format("[Point: %.2f, %.2f]", this.x, this.y);
	}
	/**
	 * Returns the x-coordinate of this point
	 * @return the x-coordinate of this point
	 */
	public double getX() {
		return this.x;
	}
	
	/**
	 * Returns the y-coordinate of this point
	 * @return the y-coordinate of this point
	 */
	public double getY() {
		return this.y;
	}
	/**
	 * Moves this point by dx in the x-direction <br /> 
	 * and by dy in the y-direction
	 * @param dx amount to move in the x-direction
	 * @param dy amount to move in the y-direction
	 */
	public void translate (double dx, double dy) {
		this.x = this.x + dx;
		this.y = this.y + dy;
	}
	
	/**
	 * Is this point equivalent to the other one?
	 * @param other the other point
	 * @return true if and only if this Point is equivalent to other
	 */
	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (!(other instanceof Point))
			return false;
		Point p = (Point) other;
		
		return p.x == this.x && p.y == this.y;
	}
	/**
	 * Returns the distance from this Point to another point
	 * @param other
	 * @return the distance from this Point to the other point
	 */
	public double distance (Point other) {
		double xDist = this.x - other.x;
		double yDist = this.y - other.y;
		
		return sqrt(xDist*xDist + yDist*yDist);
	}
	
	/**
	 * Returns the distance between two points
	 * @param p1 1st point
	 * @param p2 2nd point
	 * @return the distance between the points
	 */
	public static double distance (Point p1, Point p2) {
		return p1.distance(p2);
	}
	
	
}

