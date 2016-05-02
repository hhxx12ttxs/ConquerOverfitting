package sdp.vision;
import java.awt.Point;
import java.util.ArrayList;

import sdp.vision.NoAngleException;

public class Position {
	
	/**
	 * Returns the mean of all the points in arraylist ps
	 * @param ps A list of all the points
	 * @return The mean of these points
	 * @throws Exception 
	 */
	public static Point findMean(ArrayList<Point> ps) throws Exception {
		int x=0;
		int y=0;
		int numPos=0;
        for (int i = 0; i < ps.size(); i++) {
            x = (int) (x + ps.get(i).getX());
            y = (int) (y + ps.get(i).getY());
            numPos = numPos + 1;
    	}
        if (numPos > 0) {
        	x /= numPos;
        	y /= numPos;
        }
        else throw new Exception("No points passed to findMean");
		return new Point(x,y);
	}
	
	/**
	 * Removes from ps all those points that are within circle with radius of point 'centre'
	 * 
	 * @param ps The points to be filtered. ps will be changed by this method
	 * @param centre The centre of the circle to be cut out of the points ps
	 * @param radius The radius of the circle to be cut out of points ps
	 */
	public static void filterOutCircle(ArrayList<Point> ps, Point centre, int radius) {                 
        for (int i = 0; i < ps.size(); i++) {
        	if ((Math.pow(ps.get(i).getX() - centre.getX(), 2) + Math.pow(ps.get(i).getY() - centre.getY(), 2)) < Math.pow(radius, 2)) {
                ps.remove(i);
                i=i-1;
        	}
        }
	}
	
	/**
	 * Updates the centre point of the object, given a list of new points
	 * to compare it to. Any points too far away from the current centre are
	 * removed.
	 * 
	 * @param ps		The set of points. These will be updated by filterPoints
	 * @param centroid The mean of ps
	 */
    public static void filterPoints(ArrayList<Point> ps, Point centroid) {
    	
    	if (ps.size() > 0) {
    		
	    	int stdev = 0;
	    	
	    	/* Standard deviation */
	    	for (int i = 0; i < ps.size(); i++) {
	    		int x = (int) ps.get(i).getX();
	    		int y = (int) ps.get(i).getY();
	    		
	    		stdev += Math.pow(Math.sqrt(sqrdEuclidDist(x, y, (int) centroid.getX(), (int) centroid.getY())), 2);
	    	}
	    	stdev  = (int) Math.sqrt(stdev / ps.size());
	    	
	    	int count = 0;
	    	int newX = 0;
	    	int newY = 0;
	    	
	    	if (stdev<18) {
	    		stdev=18;
	    	}
	    	
	    	/* Remove points further than standard deviation */
	    	for (int i = 0; i < ps.size(); i++) {
	    		int x = (int) ps.get(i).getX();
	    		int y = (int) ps.get(i).getY();
	    		if (Point.distance(x, y, centroid.getX(), centroid.getY()) < stdev) {
	    			newX += x;
	    			newY += y;
	    			count++;
	    		}
	    		else {
	    			ps.remove(i);
	    			i=i-1;
	    		}
	    	}
    	}
    }
	
	/**
	 * Eliminates points from the list that are in the ball
	 * 
	 * @param xs		The new set of x points.
	 * @param ys		The new set of y points.
	 */
    public static void ballFilterPoints(ArrayList<Point> ps, Point centroid) {
    	
    	if (ps.size() > 0) {
    		
	    	int stdev = 150;
	    	
	    	int count = 0;
	    	int newX = 0;
	    	int newY = 0;
	    	
	    	/* Remove points further than standard deviation */
	    	for (int i = 0; i < ps.size(); i++) {
	    		int x = (int) ps.get(i).getX();
	    		int y = (int) ps.get(i).getY();
	    		if (Math.abs(x - centroid.getX()) < stdev && Math.abs(y - centroid.getY()) < stdev) {
	    			newX += x;
	    			newY += y;
	    			count++;
	    		}
	    		else {
	    			ps.remove(i);
	    			i=i-1;
	    		}
	    	}
    	}
    }
    
    
    public static ArrayList<Point> removeOutliers(ArrayList<Integer> xs, ArrayList<Integer> ys, Point centroid){
    	ArrayList<Point> goodPoints = new ArrayList<Point>();
	if (xs.size() > 0) {
    		
	    	int stdev = 0;
	    	
	    	/* Standard deviation */
	    	for (int i = 0; i < xs.size(); i++) {
	    		int x = xs.get(i);
	    		int y = ys.get(i);
	    		
	    		stdev += Math.pow(Math.sqrt(sqrdEuclidDist(x, y, (int) centroid.getX(), (int)centroid.getY())), 2);
	    	}
	    	stdev  = (int) Math.sqrt(stdev / xs.size());
	    		    	
	    	/* Remove points further than standard deviation */
	    	for (int i = 0; i < xs.size(); i++) {
	    		int x = xs.get(i);
	    		int y = ys.get(i);
	    		if (Math.abs(x - centroid.getX()) < stdev*1.17 && Math.abs(y - centroid.getY()) < stdev*1.17) {
	    			Point p = new Point(x, y);
	    			goodPoints.add(p);
	    		}
	    	}
	    	
    	}
    	
		return goodPoints;
    	
    }
    
    /**
     * Calculates the squared euclidean distance between two 2D points.
     * 
     * @param x1		The x-coordinate of the first point.
     * @param y1		The y-coordinate of the first point.
     * @param x2		The x-coordinate of the second point.
     * @param y2		The y-coordinate of the second point.
     * 
     * @return			The squared euclidean distance between the two points.
     */
	public static float sqrdEuclidDist(int x1, int y1, int x2, int y2) {
		return (float) (Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}public double mean(ArrayList<Integer> points){
		double mean=0.0;
		for(int i=0; i<points.size(); i++)
			mean = mean + points.get(i);
		mean = mean/(double) points.size();		
		return mean;
	}
	
	
	public double stDev(ArrayList<Integer> points, double mean){
		double stDev = 0;
		for(int i=0; i<points.size(); i++)
			stDev = stDev + Math.pow(points.get(i)-mean, 2);
		stDev = stDev/(double) points.size();
		
		return Math.sqrt(stDev);
	}
	
	
	public Point[] findFurthest(Point centroid,
			ArrayList<Integer> xpoints, ArrayList<Integer> ypoints, int distT,
			int distM) throws NoAngleException {
		if (xpoints.size() < 5) {
			throw new NoAngleException("");
		}

		Point[] points = new Point[4];
		for (int i = 0; i < points.length; i++) {
			points[i] = new Point(0, 0);
			points[i] = new Point(0, 0);
		}

		double dist = 0;
		int index = 0;

		//find furthest point from the centroid
		for (int i = 0; i < xpoints.size(); i++) {

			double currentDist = Point.distance(centroid.getX(), centroid.getY(), xpoints.get(i), ypoints.get(i));

			if (currentDist > dist && currentDist < distM) {
				dist = currentDist;
				index = i;
			}

		}
		points[0] = new Point(xpoints.get(index), ypoints.get(index));

		index = 0;

		dist = 0;

		//find furthest point from the previous point
		for (int i = 0; i < xpoints.size(); i++) {
			double dc = Point.distance(centroid.getX(),	centroid.getY(), xpoints.get(i), ypoints.get(i));
			double currentDist = Point.distance(points[0].getX(), points[0].getY(), xpoints.get(i), ypoints.get(i));
			if (currentDist > dist && dc < distM) {
				dist = currentDist;
				index = i;
			}

		}
		points[1] = new Point(xpoints.get(index), ypoints.get(index));

		index = 0;

		dist = 0;

		if (points[0].getX() == points[1].getX()) {
			throw new NoAngleException("");
		}
		//find a line line between the two points, which should cross the T
		double m1 = (points[0].getY() - points[1].getY())
				/ ((points[0].getX() - points[1].getX()) * 1.0);
		double b1 = points[0].getY() - m1 * points[0].getX();

		//find the point furthest from the line
		for (int i = 0; i < xpoints.size(); i++) {
			double d = Math.abs(m1 * xpoints.get(i) - ypoints.get(i) + b1)
					/ (Math.sqrt(m1 * m1 + 1));	//unnecessary line			REMOVE

			double dc = Point.distance(centroid.getX(),	centroid.getY(), xpoints.get(i), ypoints.get(i));
			if (d > dist && dc < distM) {
				dist = d;
				index = i;
			}
		}

		points[2] = new Point(xpoints.get(index), ypoints.get(index));

		index = 0;
		dist = 0;
		//find the point furthest from the previous point
		for (int i = 0; i < xpoints.size(); i++) {
			double dc = Point.distance(centroid.getX(),	centroid.getY(), xpoints.get(i), ypoints.get(i));
			double d3 = Point.distance(points[2].getX(), points[2].getY(), xpoints.get(i), ypoints.get(i));
			if (d3 > dist && dc < distM) {
				dist = d3;
				index = i;
			}

		}
		points[3] = new Point(xpoints.get(index), ypoints.get(index));

		for(int i=0; i<points.length; i++) {
			//frameImage.getGraphics().drawOval(points[i].getX(), points[i].getY(), 2, 2);
		}
		return points;
	}	
	
	/**
	 * Returns the angle between the vector (a-b) and the vector (1,0)
	 * @param a a point
	 * @param b a point
	 * @return angle
	 * @throws NoAngleException
	 */
	public static double angleTo(Point a, Point b) throws NoAngleException {
        double length = Math.sqrt(Math.pow((a.getX()-b.getX()),2)+Math.pow((a.getY()-b.getY()),2));
		double angle=0;
        if (length==0) {throw new NoAngleException("Attempted to divide by zero with 'length'");}
        double ay = ((a.getY() - b.getY()) / length);
        angle = -Math.acos(ay);
        if ((a.getX() - b.getX())<0) {angle=-angle;}
        angle=angle-Math.PI/2;
        
        if (angle<0) {
        	angle=angle+Math.PI*2;
        }
        return angle;
	}
}

