package sdp.vision;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import sdp.vision.Position;
/**
 * Finding rectangles around the green plate
 * 
 * @author Dale Myers
 */
public class Plate{
	
	/**
	 * Taken from http://stackoverflow.com/questions/6989100/sort-points-in-clockwise-order
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private static Boolean less(Point a, Point b, Point centre)
	{
	    if (a.x >= 0 && b.x < 0)
	        return true;
	    if (a.x == 0 && b.x == 0)
	        return a.y > b.y;

	    // compute the cross product of vectors (centre -> a) x (centre -> b)
	    int det = (a.x-centre.x) * (b.y-centre.y) - (b.x - centre.x) * (a.y - centre.y);
	    if (det < 0)
	        return true;
	    if (det > 0)
	        return false;

	    // points a and b are on the same line from the centre
	    // check which point is closer to the centre
	    int d1 = (a.x-centre.x) * (a.x-centre.x) + (a.y-centre.y) * (a.y-centre.y);
	    int d2 = (b.x-centre.x) * (b.x-centre.x) + (b.y-centre.y) * (b.y-centre.y);
	    return d1 > d2;
	}
	
	/**
	 * Get the corners of a plate
	 * If the two robots are too close to each other, find the outside 2 corners, and reflect these inside
	 * Use Dale's triangle method to get the corners of a plate
	 * 
	 * @param points The list of points that make up a plate
	 * @param c Corners will be returned for the robot that this centroid is associated with
	 * @param otherCentroid Corners associated with this robot will be ignored
	 * @param img The camera image
	 * @return The four corners of the plate
	 */
	public static Point[] getCorners(ArrayList<Point> points, Point c, Point otherCentroid) {
		Point[] corners = getCorners(points);
		//Graphics g = img.getGraphics();
		//g.setColor(new Color(0xFF000000));
		//g.drawOval((int) c.getX()-4, (int) c.getY()-4, 8, 8);
		//if (false) {
		if (Position.sqrdEuclidDist((int) c.getX(), (int) c.getY(), (int) otherCentroid.getX(), (int) otherCentroid.getY())>(3025)) {//(55 squared)
			//System.out.println("dist="+Math.sqrt(Position.sqrdEuclidDist((int) c.getX(), (int) c.getY(), (int) otherCentroid.getX(), (int) otherCentroid.getY())));
			return corners;
		}
		else {
			try {
				//System.out.println("Using modified getCorners");
				//find the two corners c1 and c2 most distant from otherCentroid. These should be correct
		        int furthestIndex=-1;
		        double furthestDistance=-1;
		        for (int i=0; (i<corners.length); i++) {
		        	if ((Math.pow((corners[i].getX()-otherCentroid.getX()),2)+Math.pow((corners[i].getY()-otherCentroid.getY()),2))>furthestDistance) {
		        		furthestIndex=i;
		        		furthestDistance=(Math.pow((corners[i].getX()-otherCentroid.getX()),2)+Math.pow((corners[i].getY()-otherCentroid.getY()),2));
		        	}
		        }
		        int furthestIndex2=-1;
		        furthestDistance=-1;
		        for (int i=0; (i<corners.length); i++) {
		        	if (((Math.pow((corners[i].getX()-otherCentroid.getX()),2)+Math.pow((corners[i].getY()-otherCentroid.getY()),2))>furthestDistance)
		        			&&(i!=furthestIndex)) {
		        		furthestIndex2=i;
		        		furthestDistance=(Math.pow((corners[i].getX()-otherCentroid.getX()),2)+Math.pow((corners[i].getY()-otherCentroid.getY()),2));
		        	}
		        }
				Point p1 = corners[furthestIndex];
				Point p2 = corners[furthestIndex2];
				
				//there is a line l between these two corners
				Point l = new Point((int) (p2.getX()-p1.getX()), (int) (p2.getY()-p1.getY())); //line vector for l
				
				/*
				for (int i=0; i<151; i++) {
					int x=(int) (i*0.01*l.getX()+c.getX());
					int y=(int) (i*0.01*l.getY()+c.getY());
					if (x>0 && y>0 && x<img.getWidth() && y<img.getHeight()) {
						img.setRGB(x, y, 0xFF000000);
					}
				}*/
				
				double[][] r = new double[2][2]; //rotation matrix
				
				Point pTemp;
				Point pTemp2;
				double theta;
				double theta2;
				
				pTemp = new Point((int) (p1.getX()-c.getX()), (int) (p1.getY()-c.getY()));
		        theta = Math.acos((pTemp.getX()*l.getX() + pTemp.getY()*l.getY()) 
		        		/ (Math.sqrt(pTemp.getX()*pTemp.getX()+pTemp.getY()*pTemp.getY()) * Math.sqrt(l.getX()*l.getX()+l.getY()*l.getY())));
		        
				pTemp2 = new Point((int) (p2.getX()-c.getX()), (int) (p2.getY()-c.getY()));
		        theta2 = Math.acos((pTemp2.getX()*l.getX() + pTemp2.getY()*l.getY()) 
		        		/ (Math.sqrt(pTemp2.getX()*pTemp2.getX()+pTemp2.getY()*pTemp2.getY()) * Math.sqrt(l.getX()*l.getX()+l.getY()*l.getY())));

				//System.out.println("p1:Theta="+Math.toDegrees(theta));
				//System.out.println("p2:Theta2="+Math.toDegrees(theta2));
				if (less(p1, p2, c)) {
					theta=-theta;
					theta2=-theta2;
					//System.out.println("Negating!");
				}
				else {
					//System.out.println();
				}
				//System.out.println("Fix them!");
				//System.out.println("p1:Theta="+Math.toDegrees(theta));
				//System.out.println("p2:Theta2="+Math.toDegrees(theta2));
				
				theta=2*theta;
				r[0][0]=Math.cos(theta);
				r[1][0]=-Math.sin(theta);
				r[0][1]=Math.sin(theta);
				r[1][1]=Math.cos(theta);
				Point p3 = new Point((int) (pTemp.getX()*r[0][0] + pTemp.getY()*r[1][0] + c.getX()), (int) (pTemp.getX()*r[0][1] + pTemp.getY()*r[1][1] + c.getY()));
				
				theta2=2*theta2;
				r[0][0]=Math.cos(theta2);
				r[1][0]=-Math.sin(theta2);
				r[0][1]=Math.sin(theta2);
				r[1][1]=Math.cos(theta2);
				Point p4 = new Point((int) (pTemp2.getX()*r[0][0] + pTemp2.getY()*r[1][0] + c.getX()), (int) (pTemp2.getX()*r[0][1] + pTemp2.getY()*r[1][1] + c.getY()));

				corners = new Point[4];
				corners[0]=p1;
				corners[1]=p4;
				corners[2]=p3;
				corners[3]=p2;
			} catch (Exception e) {
				e.printStackTrace();
				return getCorners(points);
			}/*
			g.setColor(new Color(0xFF000000));
			g.drawOval((int) corners[0].getX()-2, (int) corners[0].getY()-2, 4, 4);
			g.setColor(new Color(0xFFFF0000));
			g.drawOval((int) corners[3].getX()-2, (int) corners[3].getY()-2, 4, 4);
			g.setColor(new Color(0xFF0000FF));
			g.drawOval((int) corners[1].getX()-2, (int) corners[1].getY()-2, 4, 4);
			g.setColor(new Color(0xFF00FF00));
			g.drawOval((int) corners[2].getX()-2, (int) corners[2].getY()-2, 4, 4);
			
			g.setColor(Color.magenta);
			g.drawLine(corners[0].x, corners[0].y, c.x, c.y);
			g.drawLine(corners[2].x, corners[2].y, c.x, c.y);
			g.setColor(Color.cyan);
			g.drawLine(corners[1].x, corners[1].y, c.x, c.y);
			g.drawLine(corners[3].x, corners[3].y, c.x, c.y);*/
			//return getCorners(points, img);
			return corners;
		}
	}
	
	
	
	/**
	 * Get the corners of a plate
	 * 
	 * Use Dale's triangle method to get the corners of a plate
	 * 
	 * @param points The list of points that make up a plate
	 * @return The four corners of the plate
	 */
	public static Point[] getCorners(ArrayList<Point> points){
		Point centroid = getCentroid(points);
		Point furthest = new Point(0,0);
		Point opposite = new Point(0,0);
		Point adjacent = new Point(0,0);
		Point adjacent2 = new Point(0,0);
		double dist = 0;
		for(Point p : points){
			if(Point.distance(p.x, p.y, centroid.x, centroid.y) > dist){
				furthest = p;
				dist = Point.distance(p.x, p.y, centroid.x, centroid.y);
			}
		}
		
		dist = 0;
		for(Point p : points){
			if(Point.distance(p.x, p.y, furthest.x, furthest.y) > dist){
				opposite = p;
				dist = Point.distance(p.x, p.y, furthest.x, furthest.y);
			}
		}
		
		dist = 0;
		for(Point p : points){
			if(Line2D.ptSegDist(furthest.x, furthest.y, opposite.x, opposite.y, p.x, p.y) > dist){
				adjacent = p;
				dist = Line2D.ptSegDist(furthest.x, furthest.y, opposite.x, opposite.y, p.x, p.y);
			}
		}

		dist = 0;
		ArrayList<Point> outside = new ArrayList<Point>();
		for(Point p : points){
			if(!isPointInTriangle(furthest,opposite,adjacent,p)){
				outside.add(p);
			}
		}
		for(Point p : outside){
			//if (Line2D.ptLineDist(furthest.x, furthest.y, opposite.x, opposite.y, p.x, p.y)>dist){
			if 		(Point.distance(furthest.x, furthest.y, p.x, p.y)
					+Point.distance(opposite.x, opposite.y, p.x, p.y)
					+Point.distance(adjacent.x, adjacent.y, p.x, p.y) > dist){
				adjacent2 = p;
				dist=Point.distance(furthest.x, furthest.y, p.x, p.y)
				+Point.distance(opposite.x, opposite.y, p.x, p.y)
				+Point.distance(adjacent.x, adjacent.y, p.x, p.y);
				//dist = Line2D.ptLineDist(furthest.x, furthest.y, opposite.x, opposite.y, p.x, p.y);
			}
		}
		/*
		Graphics g = img.getGraphics();
		g.setColor(new Color(0xFF000000));
		g.drawOval((int) furthest.getY()-2, (int) furthest.getX()-2, 4, 4);
		g.setColor(new Color(0xFFFFFFFF));
		g.drawOval((int) opposite.getY()-2, (int) opposite.getX()-2, 4, 4);
		g.setColor(new Color(0xFFFF0000));
		g.drawOval((int) adjacent.getY()-2, (int) adjacent.getX()-2, 4, 4);
		g.setColor(new Color(0xFF0000FF));
		g.drawOval((int) adjacent2.getY()-2, (int) adjacent2.getX()-2, 4, 4);*/

		return new Point[]	{	
								furthest,
								opposite,
								adjacent,
								adjacent2
							};
	}
	
	/**
	 * Dale's triangle
	 * 
	 * Looks slightly further than it should to ensure it finds the farthest point
	 * 
	 * @param a One of the corners of triangle
	 * @param b One of the corners of triangle
	 * @param c One of the corners of triangle
	 * @param p The point to check if its in the triangle
	 * @return True if the points are in the triangle
	 */
	public static boolean isPointInTriangle(Point a, Point b, Point c, Point p){
		Point v0 = new Point(c.x - a.x, c.y - a.y);
		Point v1 = new Point(b.x - a.x, b.y - a.y);
		Point v2 = new Point(p.x - a.x, p.y - a.y);
		
		int dot00 = dot(v0, v0);
		int dot01 = dot(v0, v1);
		int dot02 = dot(v0, v2);
		int dot11 = dot(v1, v1);
		int dot12 = dot(v1, v2);

		double invDenom = 1.0 / (double)(dot00 * dot11 - dot01 * dot01);
		double u = (dot11 * dot02 - dot01 * dot12) * invDenom;
		double v = (dot00 * dot12 - dot01 * dot02) * invDenom;

		// Check if point is in triangle
		return (u >= -0.1) && (v >= -0.1) && (u + v < 1.1);

	}
	
	/**
	 * Like Dale's but doesn't look too far
	 * 
	 * Used for orientation finding so that we don't look off of green plate
	 * 
	 * @param a One of the corners of triangle
	 * @param b One of the corners of triangle
	 * @param c One of the corners of triangle
	 * @param p The point to check if its in the triangle
	 * @return True if in triangle
	 */
	public static boolean isPointInNotShitTriangle(Point a, Point b, Point c, Point p){
		Point v0 = new Point(c.x - a.x, c.y - a.y);
		Point v1 = new Point(b.x - a.x, b.y - a.y);
		Point v2 = new Point(p.x - a.x, p.y - a.y);
		
		int dot00 = dot(v0, v0);
		int dot01 = dot(v0, v1);
		int dot02 = dot(v0, v2);
		int dot11 = dot(v1, v1);
		int dot12 = dot(v1, v2);

		double invDenom = 1.0 / (double)(dot00 * dot11 - dot01 * dot01);
		double u = (dot11 * dot02 - dot01 * dot12) * invDenom;
		double v = (dot00 * dot12 - dot01 * dot02) * invDenom;

		// Check if point is in triangle
		return (u >= 0) && (v >= 0) && (u + v < 1);

	}
	
	/**
	 * Dot product?
	 * 
	 * @param a One Point
	 * @param b One Point
	 * @return The value of the dot product
	 */
	public static int dot(Point a, Point b){
		return (a.x * b.x) + (a.y *b.y);
	}
	
	/**
	 * Get centroid of plate?
	 * 
	 * @param points The points that make up the plate
	 * @return The position of the centroid (Point)
	 */
	public static Point getCentroid(ArrayList<Point> points){
		Point centroid = new Point(0,0);
		try{
			for(Point p : points){
				centroid.translate(p.x, p.y);
			}
			return new Point(centroid.x / points.size(), centroid.y / points.size());
		} catch (Exception e){
			return new Point(0,0);
		}
	}
	
	/**
	 * Use the triangle stuff to generate rectangle
	 * 
	 * @param a point p
	 * @param array of four points, forming a rectangle
	 * @return whether p is in the rectangle formed from the four points
	 */
	public static boolean isInRectangle(Point p, Point[] points){
		if( p == new Point(0,0) ){
			return false;
		}
		
		boolean a; 
		boolean b; 
		
		a = isPointInNotShitTriangle(points[0], points[2], points[3], p);
		b = isPointInNotShitTriangle(points[1], points[2], points[3], p);
		
		return a || b;
	}
}

