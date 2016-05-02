import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Float;

import java.util.ArrayList;

/*
 *  This class holds the values for the two point sets and is able to calculate
 * the Hausdorff distance between the two point sets.
 */
public class Hausdorff {
    /* Arraylists for the two point sets. */
    public ArrayList<Point> set1 = new ArrayList<Point>();
    public ArrayList<Point> set2 = new ArrayList<Point>();
    /* The right and lower boundaries of the allowed space (left and upper are assumed to be 0) */
    public int xbound = 0;
    public int ybound = 0;
    /* The lines representing the minimal distances between point patterns,
     * for visual feedback. */
    public ArrayList<Line2D.Float> l1 = new ArrayList<Line2D.Float>();
    public ArrayList<Line2D.Float> l2 = new ArrayList<Line2D.Float>();
    public Line2D.Float hdLine = null;
    
    /*
     * Standard constructor.
     */
    public Hausdorff() {
        /* No work required on initialization. */
    }
    
    /*
     * Calculate the standard Hausdorff distance, based on the information in
     * the two point sets.
     */
    public double calculateHausdorff(ArrayList<Point> set1) {
        if(set1.size() == 0 || set1.size() == 0) {
            System.out.println("Can not calculate distance with empty set(s).");
            return -1;
        }
        /* Clear previous lines. */
        l1.clear();
        l2.clear();
        hdLine = null;
    
        double maxMinDist = 0.0;
        /* Determine lines of minimal distance from set 1 to set 2. */
        for(int i = 0; i < set1.size(); i++) {
            double minDist = L2Dist(set1.get(i), set2.get(0));
            /* Determine the line connecting the segments. */
            Line2D.Float line = new Line2D.Float(set1.get(i), set2.get(0));
            
            /* Check for new closest point for each point from set 2. */
            for(int j = 1; j < set2.size(); j++) {
                double d = L2Dist(set1.get(i), set2.get(j));
                if(d < minDist) {
                    minDist = d;
                    line.x2 = set2.get(j).x;
                    line.y2 = set2.get(j).y;
                }
            }
            /* Append the new line to the collection of lines. */
            l1.add(line);
            
            /* Calculate the max of the min distances from set 1 to 2.  */
            if(minDist > maxMinDist) {
                maxMinDist = minDist;
                hdLine = line;
            }
        }
        
        /* Determine lines of minimal distance from set 2 to set 1. */
        for(int i = 0; i < set2.size(); i++) {
            double minDist = L2Dist(set2.get(i), set1.get(0));
            /* Determine the line connecting the segments. */
            Line2D.Float line = new Line2D.Float(set2.get(i), set1.get(0));
            
            for(int j = 1; j < set1.size(); j++) {
                /* Check for new closest point. */
                double d = L2Dist(set2.get(i), set1.get(j));
                if(d < minDist) {
                    minDist = d;
                    line.x2 = set1.get(j).x;
                    line.y2 = set1.get(j).y;
                }
            }
            /* Append the new line to the collection of lines. */
            l2.add(line);
            
            /* Calculate the max of the min distances from set 2 to 1. */
            if(minDist > maxMinDist) {
                maxMinDist = minDist;
                hdLine = line;
            }
        }
        
        /* Return the max of both max-values. */
        return maxMinDist;
    }
    
    /*
     * The standard hausdorff distance calculation is between the two original point sets.
     */
    public double calculateHausdorff()
    {
    	return calculateHausdorff(set1);
    }
    
    /*
     * Calculate the minimal Hausdorff distance under translation, based on the
     * information in the two point sets.
     */
    public double calculateHausdorffTranslation() {
    	if(set1.size() == 0 || set1.size() == 0) {
            System.out.println("Can not calculate distance with empty set(s).");
            return -1;
        }
        /* Clear previous lines. */
        l1.clear();
        l2.clear();
        
        double epsilon = calculateHausdorff();
        
        /* Find initial translation space for point set1 */
        int xmin = xbound;	int xmax = 0;
        int ymin = ybound;	int ymax = 0;
        for(Point p : set1)
        {
        	if(p.x < xmin)
        		xmin = p.x;
        	if(p.x > xmax)
        		xmax = p.x;
        	if(p.y < ymin)
        		ymin = p.y;
        	if(p.y > ymax)
        		ymax = p.y;
        }
        Point trafo = trafoSpaceSubdivision(-xmin, xbound - xmax, -ymin, ybound - ymax, epsilon);
        double dist;
        if(trafo != null)
        {
        	/* Translate points  */
        	ArrayList<Point> translatedSet = new ArrayList<Point>();
        	for(Point p : set1)
        	{
        		translatedSet.add(new Point(p.x + trafo.x, p.y + trafo.y));
        	}
        	set1 = translatedSet;
        	dist = calculateHausdorff();
        }
        else
        	dist = -1;
        return dist;
    }
    
    /*
     * Calculate the minimal Hausdorff distance with Transformation Space Subdivision
     */
    public Point trafoSpaceSubdivision(int left, int right, int upper, int lower, double epsilon)
    {
    	
    	if(lower < upper || right < left)
    		return null;
    	if(epsilon < 0)
    		epsilon = 0;
    	/* Compute the center trafo */
    	Point tc = new Point((int)Math.floor((right+left)/2.0), (int) Math.floor((lower+upper)/2.0));
    	double radius = Math.max(
    			Math.max(L2Dist(tc, new Point(right, lower)), L2Dist(tc, new Point(left, lower)))
    			,Math.max(L2Dist(tc, new Point(right, upper)), L2Dist(tc, new Point(left, upper))));
    	double hdist = hausdorffUnderTranslation(tc);

   	 	/* If the Hausdorff distance of the center trafo is too large, discard this region */
	   	if(hdist > epsilon + radius)
	   		return null;
	   	else if(hdist <= epsilon)
	   	{
	   		/* If possible, refine epsilon */
	   		if(epsilon > 0)
	   		{
	   			Point tFine = trafoSpaceSubdivision(left, right, upper, lower, epsilon - 0.5);
	   			if(tFine != null)
	   				return tFine;
	   			else
	   				return tc;
	   		}
	   		else
	   		{
   				return tc;
	   		}
	   		
	   	}
	   	else
	   	{
	   		if(lower  <= upper && right  <= left)
	    		return null;

	   		/* Determine new epsilon: 
	   		 * it should be the minimum of the Hausdorf distances of the center trafo's 
	   		 * of the four subregions.
	   		 */
	   		Point tc1 = new Point((left + tc.x)/2, (upper + tc.y)/2);
    		Point tc2 = new Point((left + tc.x)/2, (tc.y + 1 + lower)/2);
    		Point tc3 = new Point((tc.x + 1 + right)/2, (upper + tc.y)/2);
    		Point tc4 = new Point((tc.x + 1 + right)/2, (tc.y + 1 + lower)/2);
    		double hd1 = hausdorffUnderTranslation(tc1);
    		double hd2 = hausdorffUnderTranslation(tc2);
    		double hd3 = hausdorffUnderTranslation(tc3);
    		double hd4 = hausdorffUnderTranslation(tc4);
    		double epsNew = Math.min(epsilon, 
    							Math.min(
    									Math.min(hd1, hd2), 
    									Math.min(hd3, hd4)));

    		/* Compute the optimal trafo's of the four subregions */
	   		Point t1 = trafoSpaceSubdivision(left, tc.x, upper, tc.y, epsNew);
	   		Point t2 = trafoSpaceSubdivision(left, tc.x, tc.y +1, lower, epsNew);
	   		Point t3 = trafoSpaceSubdivision(tc.x + 1, right, upper, tc.y, epsNew);
	   		Point t4 = trafoSpaceSubdivision(tc.x + 1, right, tc.y + 1, lower, epsNew);
	   		
	   		/* Compute the Hausdorff distances under these translations */
	   		hd1 = hausdorffUnderTranslation(t1);
    		hd2 = hausdorffUnderTranslation(t2);
    		hd3 = hausdorffUnderTranslation(t3);
    		hd4 = hausdorffUnderTranslation(t4);
    		
    		/* Return the best translation */
    		if(hd1 < hd2 && hd1 < hd3 && hd1 < hd4)
    		{
    			return t1;
    		}
    		else if(hd2 < hd3 && hd2 < hd4) 
    		{
    			return t2;
    		}
    		else if(hd3 < hd4)
    		{
    			return t3;
    		}
    		else
    		{
    			return t4;
    		}
	   	}
    }
    
    public double hausdorffUnderTranslation(Point t)
    {
    	if(t==null)
    		return Double.MAX_VALUE;
    	
    	/* Translate set1 by t */
    	ArrayList<Point> translatedSet = new ArrayList<Point>();
    	for(Point p : set1)
    	{
    		translatedSet.add(new Point(p.x + t.x, p.y + t.y));
    	}
    	return calculateHausdorff(translatedSet);
    }
    
    /*
     * Calculate the L2 distance between two 2D points.
     */
    private double L2Dist(Point a, Point b) {
        return Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2));
    }
}

