<<<<<<< HEAD
package trussoptimizater.Truss.Optimize;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;


/**
 * This class extends line2D functality and is used for defining the symmetry axis when optimizing a truss.
 *
 * @author Chris
 */
public class MirrorLine extends Line2D.Double {

    /**
     * A tolerance is used when comparing Point2D to escape floating points issues
     */
    private double tolerance = 0.0001;


    public MirrorLine(double x1, double y1, double x2, double y2) {
        super(x1, y1, x2, y2);
    }

    /**
     *
     * @param origPoint
     * @return true if the point in question is left or below this line
     */
    public boolean isLeftorBelowLine(Point2D origPoint){
        if(relativeCCW(origPoint) == 1 || relativeCCW(origPoint) == 0){
            return true;
        }else{
            return false;
        }
    }

    /**
     * This method calculates whether this line is vertical by finding the difference between
     * the x cordinates of both its points. If the difference is less than the specified tolerance,
     * then this line is vertical.
     * @return true if this line is vertical
     */
    public boolean isVertical() {
        double xdiff = getX2() - getX1();
        if (xdiff <= tolerance && xdiff >= -tolerance) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method calculates whether this line is horizntaol by finding the difference between
     * the y cordinates of both its points. If the difference is less than the specified tolerance,
     * then this line is horizontal.
     * @return true if this line is horizontal
     */
    public boolean isHorizotal() {
        double ydiff = getY2() - getY1();
        if (ydiff <= tolerance && ydiff >= -tolerance) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method checks whether this line is horizontal or vertical. If it is netiher it must be angled.
     * @return true if the line is not horizontal and not vertical
     */
    public boolean isAngled() {
        if (isHorizotal() || isVertical()) {
            return false;
        } else {
            return true;
        }
    }


 
    /**
     * Uses y = mx + c to work out y. Note this method assumes the line is infinite and will calculate a y value
     * even if the x cordinate you specifiy is not within p1.x and p2.x
     * @param x The horizontal distance at which you wish to find the corresponding y value
     * @return the y cordinate of the line at specified x cordinate
     * @throws Exception if the line is vertical as there is no way to tell what y ias at x
     */
    private double getYatX(double x) throws Exception {
        if (isHorizotal()) {
            return getY1();
        }

        if (this.isVertical()) {
            throw new Exception("Line is Vertical, therefore no way to tell what y is at " + x + "!");
        }


        double lineGradient = getGradient();
        double lineIntercept = getYIntercept();
        return lineGradient * x + lineIntercept;
    }

    /**
     * Uses y = mx + c to work out x. Note this method assumes the line is infinite and will calculate an x value
     * even if the y cordinate you specifiy is not within p1.y and p2.y
     * @param y The vertical distance at which you wish to find the corresponding x value
     * @return the x cordinate of the line at specified y cordinate
     * @throws Exception If the line is horzintal there is no way to tell what x is at y.
     */
    private double getXatY(double y) throws Exception {
        double lineGradient = getGradient();
        double lineIntercept = getYIntercept();

        if (isHorizotal()) {
            throw new Exception("Line is Horizontal, therefore no way to tell what x is at " + y + "!");
        }

        if (isVertical()) {
            return y;
        }

        if (lineIntercept > 0) {
            return (y - lineIntercept) / lineGradient;
        } else {
            return (y + lineIntercept) / lineGradient;
        }
    }

    /**
     * The super method always returns false, therefore this method has been overriden to include
     * @param p
     * @return true if point lies on line
     */
    @Override
    public boolean contains(Point2D p) {
        if (this.isVertical()) {
            if (p.getX() - getX1() <= tolerance && p.getX() - getX1() >= -tolerance ) {
                return true;
            } else {
                return false;
            }
        }
        if (this.isHorizotal()) {
            if (p.getY() - this.getY1() <= tolerance && p.getY() - this.getY1() >= -tolerance) {
                return true;
            } else {
                return false;
            }
        }
        try {
            if (p.getY() - getYatX(p.getX()) <= tolerance && p.getY() - getYatX(p.getX()) >= -tolerance ) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            System.out.println("Error in contains method " + ex);
            return false;
        }
    }



    /**
     * If this line is horizontal, then the y intercept will just be p1.y or p2.y. If this
     * line is angled then y intercept can be found by rearanging the straight line equation ie
     * c = y - mx.
     * @return the y cordinate where this line crosses the y axis
     * @throws Exception If this line is vertical, then there is no y intercept
     */
    private double getYIntercept() throws Exception {
        if (isHorizotal()) {
            return getY1();
        }

        if (this.isVertical()) {
            throw new Exception("Line is Vertical, therefore no y intercept");
        }

        double gradient = getGradient();
        if (gradient * getX1() > 0) {
            return getY1() - gradient * getX1();
        } else {
            return getY1() + gradient * getX1();
        }

    }

    /**
     * The gradient of this line is the (m) used in the y = mx+ c equation. Note that if this
     * line is horizontal gradient will be 0. And if this line is vertical graident will be infinity.
     * @return the gradient of the line
     */
    private double getGradient() {
        double xdiff = getX2() - getX1();
        double ydiff = getY2() - getY1();
        return ydiff / xdiff;
    }

    /**
     * Uses projections to find a the cordinates of a mirror Point.
     * <p>
     * For example if the mirror is vertical and starts at the cordinates (0,0).
     * If you use getReflectedPoint(new Point2D.Double(-5,0) it will return the mirror point with the
     * cordinates (5,0)
     * <p>
     * @param p The point that you want to obtain the mirror point of
     * @return a point mirror by this line
     */
    public Point2D getReflectedPoint(Point2D p) {
        //vector y (the point)
        double y1 = p.getX() - getX1();
        double y2 = p.getY() - getY1();

        //vector u (the line)
        double u1 = getX2() - getX1();
        double u2 = getY2() - getY1();

        //orthogonal projection of y onto u
        double scale = (y1 * u1 + y2 * u2) / (u1 * u1 + u2 * u2);
        double projX = scale * u1 + getX1();
        double projY = scale * u2 + getY1();

        return new Point2D.Double(2 * projX - p.getX(), 2 * projY - p.getY());
    }


=======
package optimization.projections;


import java.util.Random;

import optimization.util.MathUtils;
import optimization.util.MatrixOutput;

/**
 * Implements a projection into a box set defined by a and b.
 * If either a or b are infinity then that bound is ignored.
 * @author javg
 *
 */
public class BoundsProjection extends Projection{

	double a,b;
	boolean ignoreA = false;
	boolean ignoreB = false;
	public BoundsProjection(double lowerBound, double upperBound) {
		if(Double.isInfinite(lowerBound)){
			this.ignoreA = true;
		}else{
			this.a =lowerBound;
		}
		if(Double.isInfinite(upperBound)){
			this.ignoreB = true;
		}else{
			this.b =upperBound;
		}
	}
	
	
	
	/**
	* Projects into the bounds
	* a <= x_i <=b
	 */
	public void project(double[] original){
		for (int i = 0; i < original.length; i++) {
			if(!ignoreA && original[i] < a){
				original[i] = a;
			}else if(!ignoreB && original[i]>b){
				original[i]=b;
			}
		}
	}
	
	/**
	 * Generates a random number between a and b.
	 */

	Random r = new Random();
	
	public double[] samplePoint(int numParams) {
		double[] point = new double[numParams];
		for (int i = 0; i < point.length; i++) {
			double rand = r.nextDouble();
			if(ignoreA && ignoreB){
				//Use const to avoid number near overflow
				point[i] = rand*(1.E100+1.E100)-1.E100;
			}else if(ignoreA){
				point[i] = rand*(b-1.E100)-1.E100;
			}else if(ignoreB){
				point[i] = rand*(1.E100-a)-a;
			}else{
				point[i] = rand*(b-a)-a;
			}
		}
		return point;
	}
	
	public static void main(String[] args) {
		BoundsProjection sp = new BoundsProjection(0,Double.POSITIVE_INFINITY);
		
		
		MatrixOutput.printDoubleArray(sp.samplePoint(3), "random 1");
		MatrixOutput.printDoubleArray(sp.samplePoint(3), "random 2");
		MatrixOutput.printDoubleArray(sp.samplePoint(3), "random 3");
		
		double[] d = {-1.1,1.2,1.4};
		double[] original = d.clone();
		MatrixOutput.printDoubleArray(d, "before");
		
		sp.project(d);
		MatrixOutput.printDoubleArray(d, "after");
		System.out.println("Test projection: " + sp.testProjection(original, d));
	}
	
	double epsilon = 1.E-10;
	public double[] perturbePoint(double[] point, int parameter){
		double[] newPoint = point.clone();
		if(!ignoreA && MathUtils.almost(point[parameter], a)){
			newPoint[parameter]+=epsilon;
		}else if(!ignoreB && MathUtils.almost(point[parameter], b)){
			newPoint[parameter]-=epsilon;
		}else{
			newPoint[parameter]-=epsilon;
		}
		return newPoint;
	}

	
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

