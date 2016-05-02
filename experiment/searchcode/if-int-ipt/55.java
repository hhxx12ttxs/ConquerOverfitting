package util;

import java.awt.Point;

/**
 * Calculate line intersections 
 */
public class Intersection {
	
	/**
	 * Check if two segments intersect
	 */
	static public boolean segmentsIntersect(int xa, // line 1 point 1 x
            				  int ya, // line 1 point 1 y
					          int xb, // line 1 point 2 x
					          int yb, // line 1 point 2 y
					          int xc, // line 2 point 1 x
					          int yc, // line 2 point 1 y
					          int xd, // line 2 point 2 x
					          int yd) // line 2 point 2 y
	{
		Point ipt = intersect(xa, ya, xb, yb, xc, yc, xd, yd);
		
		if (ipt == null) {
			return false;
		}
		
		if (Math.abs(xa - xb) > Math.abs(ya - yb)) {
			if (ipt.getX() > Math.max(xa, xb) || ipt.getX() < Math.min(xa, xb))
				return false;
		}
		else {
			if (ipt.getY() > Math.max(ya, yb) || ipt.getY() < Math.min(ya, yb))
				return false;
		}
		
		if (Math.abs(xc - xd) > Math.abs(yc - yd)) {
			if (ipt.getX() > Math.max(xc, xd) || ipt.getX() < Math.min(xc, xd))
				return false;
		}
		else {
			if (ipt.getY() > Math.max(yc, yd) || ipt.getY() < Math.min(yc, yd))
				return false;
		}
				
		return true;
	}	
	
	/**
     * Standard line intersection algorithm
   	 * Return the point of intersection if it exists, else null
   	**/
	// from Doug Lea's PolygonFigure
	static public Point intersect(int xa, // line 1 point 1 x
                                  int ya, // line 1 point 1 y
                                  int xb, // line 1 point 2 x
                                  int yb, // line 1 point 2 y
                                  int xc, // line 2 point 1 x
                                  int yc, // line 2 point 1 y
                                  int xd, // line 2 point 2 x
                                  int yd) // line 2 point 2 y

    // source: http://vision.dai.ed.ac.uk/andrewfg/c-g-a-faq.html
    // eq: for lines AB and CD
    //     (YA-YC)(XD-XC)-(XA-XC)(YD-YC)
    // r = -----------------------------  (eqn 1)
    //     (XB-XA)(YD-YC)-(YB-YA)(XD-XC)
    //
    //     (YA-YC)(XB-XA)-(XA-XC)(YB-YA)
    // s = -----------------------------  (eqn 2)
    //     (XB-XA)(YD-YC)-(YB-YA)(XD-XC)
    //  XI = XA + r(XB-XA)
    //  YI = YA + r(YB-YA)
	{
	    double denom = ((xb - xa) * (yd - yc) - (yb - ya) * (xd - xc));
	
	    double rnum = ((ya - yc) * (xd - xc) - (xa - xc) * (yd - yc));
	
	    if (denom == 0.0) { // parallel
	    	if (rnum == 0.0) { // coincident; pick one end of first line
	    		if ((xa < xb && (xb < xc || xb < xd)) ||
	    			(xa > xb && (xb > xc || xb > xd)))
	    		return new Point(xb, yb);
	        else
	        	return new Point(xa, ya);
	      }
	      else
	      		return null;
	    }
	
	    double r = rnum / denom;
	
	    double snum = ((ya - yc) * (xb - xa) - (xa - xc) * (yb - ya));
	    double s = snum / denom;
	
	    if (0.0 <= r && r <= 1.0 && 0.0 <= s && s <= 1.0) {
	    	int px = (int)(xa + (xb - xa) * r);
	    	int py = (int)(ya + (yb - ya) * r);
	    	return new Point(px, py);
	    }
	    else
	    	return null;
	}
}

