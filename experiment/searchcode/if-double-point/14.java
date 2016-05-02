/**
 *	Copyright (C) 2014 Marco A Asteriti
 *
 *	Permission is hereby granted, free of charge, to any person obtaining a copy 
 *	of this software and associated documentation files (the "Software"), to deal in 
 *	the Software without restriction, including without limitation the rights to use, 
 *	copy, modify, merge, publish, distribute, sublicense, and/or sell copies of 
 *	the Software, and to permit persons to whom the Software is furnished to do so, 
 *	subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in all copies 
 *  or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR 
 *  A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF 
 *  OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


package com.masteriti.geometry;

/**
 * Represents a point in Euclidean 3D space of coordinates x, y, z. 
 * @author Marco
 */
public final class Point implements Comparable<Point> {
    public static final Point ORIGIN = new Point(0,0);
	public final double x;
	public final double y;
	public final double z;

	
	
	/////////////////////////
	// Constructors
	/////////////////////////
	public Point  (double x, double y) {
		this(x, y, 0.0);
	}
	public Point(double x, double y, double z) {
      if((Double.isNaN(x)) || (Double.isNaN(y)) || (Double.isNaN(z))) {
        throw new IllegalArgumentException("Point Constructor received Double.NaN as a parameter");
      }
        // convert any negative zero values to 0.0 (messe up sorting somehow in containers
		this.x = x == -0.0 ? 0.0 : x;
		this.y = y == -0.0 ? 0.0 : y;
		this.z = z == -0.0 ? 0.0 : z;
	}
        
    /**
     * Natural ordering sorts points right to left on X axis (higher X to lower),
     * then top to bottom on Y axis (higher Y to lower), and finally from higher
     * z to lower) in that order of priority.
     * @param p
     * @return 
     */
	@Override
	public int compareTo(Point p) {
		if (this.x > p.x)
			return -1;            // -1 means this point to the right of p (higher x value)
		else if (this.x == p.x) {
			if (this.y > p.y)
				return -1;
			else if (this.y == p.y) {
              if (this.z > p.z)
                return -1;
              else if(this.z == p.z)
                return 0;         // 0 means this point is in same xyz coordinate position as p.
              else
                return 1;
            }
			else
				return 1;         // 1 means this point is the the left of p (lower x value)
		} else 
			return 1;
	}
    
    @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this)
      return true;
    
    if (obj instanceof Point) {
      Point other = (Point) obj;
      if ((       (Math.abs(this.x - other.x)) < Calc.EPSILON)
              && ((Math.abs(this.y - other.y)) < Calc.EPSILON)
              && (Math.abs((this.z - other.z)) < Calc.EPSILON)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 67 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
    hash = 67 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
    hash = 67 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
    return hash;
  }

  public Point copy() {
    return new Point(x, y, z);
  }
  public String print3DLoc() {
    String s = String.format("(%4.1f,%4.1f,%4.1f)", x, y, z);
    return String.format("%24s",s);
  }
  
  public String print2DLoc() {
    String s = String.format("(%4.1f,%4.1f)", x, y);
    return String.format("%17s",s);
  }

  @Override
  public String toString() {
    if(z == 0.0)
      return print2DLoc();
    else
      return print3DLoc();
  }
  
  public Vector2d getPosVector() {
    return new Vector2d(this);
  }
  
} // End of Coordinates class

