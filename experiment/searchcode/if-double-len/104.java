/*
 * point.java
 *
 * Created on December 20, 2004, 3:01 PM
 */

package kuhnlab.coordinates;
import kuhnlab.xml.*;

import java.beans.*;
import java.util.*;
import java.math.*;
import java.io.Serializable;
import java.awt.geom.*;
	
/** A class to represent 2-dimensional points and points.
 * @author Jeffrey R. Kuhn (jeffrey.kuhn@yale.edu)
*/
public class KPoint2D implements Serializable, XDomTranslate {
    public static final String tagTPoint = "point2d";
    public static final String tagX = "x";
    public static final String tagY = "y";
    
    public double x;
    public double y;
    
    /** Default constructor. Set's coordinates to zero. */
    public KPoint2D() { 
    }
    
    /** Construct a point from specified coordinates.
     * @param x new x coordinate.
     * @param y new y coordinate. */
    public KPoint2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /** Construct a point from an array of 2 values.
     * @param array array containing {x,y} */
    public KPoint2D(double[] array) {
        this(array[0], array[1]);
    }
    
    /** Construct a point from another point.
     * @param t point containing new x and y coordinates. */
    public KPoint2D(KPoint2D t) {
        this(t.x, t.y);
    }
    
    /** Construct a point from this point. */
    @Override public Object clone() {
        return new KPoint2D(this.x, this.y);
    }
    
    /** Returns the hash code for the point. */
    @Override public int hashCode() {
        long hash = java.lang.Double.doubleToLongBits(x);
        hash = (hash<<5) - hash + java.lang.Double.doubleToLongBits(y);
        return (int) (hash ^ (hash >>> 32));
    }
    
    /** Convert point to string. */
    public String toString() {
        return "(" + x + "," + y + ")";
    }
    
    /************************************************************************
     *** XML input/output                                                 ***
     ************************************************************************/

    /** Create a new Point from an XML element */
    public static KPoint2D TPoint2D_fromXDomItem(XDomItem item) {
        KPoint2D t = new KPoint2D();
        return (t.fromXDomItem(item)) ? t : null;
    }

    /** Fill in this object from an XML element. */
    public boolean fromXDomItem(XDomItem item) {
        if (item == null) return false;
        if (!item.isElementNamed(tagTPoint)) return false;
        x = item.getDouble(tagX, 0);
        y = item.getDouble(tagY, 0);
        return true;
    }
    
    /** Create an XML Element from this object. */
    public XDomItem toXDomItem(XDomItem.Factory factory) {
        if (factory == null) return null;
        XDomItem item = factory.createElement(tagTPoint);
        if (item == null) return null;
        item.setDouble(tagX, x);
        item.setDouble(tagY, y);
        return item;
    }

    /************************************************************************
     *** OPERATIONS ON COORDINATES                                        ***
     ************************************************************************/
    
    /** Set all coordinates to zero. */
    public void clear() {
        this.x = 0;  this.y = 0;
    }
    
    /** Sets all coordinates to NaN. */
    public void toNaN() {
        this.x = java.lang.Double.NaN;
        this.y = java.lang.Double.NaN;
    }
    
    /**
     * Check if this point is NaN.
     * @return true if one of the coordinates is NaN
     *  and false if all coordinates are valid. */
    public boolean isNaN() {
        if (java.lang.Double.isNaN(this.x)) return true;
        if (java.lang.Double.isNaN(this.y)) return true;
        return false;
    }
    
    /** Sets all coordinates to Infinity. */
    public void toInfinite() {
        this.x = java.lang.Double.POSITIVE_INFINITY;
        this.y = java.lang.Double.POSITIVE_INFINITY;
    }
    
    /** Check if this point is infinite.
     * @return true if one of the coordinates is infinite
     *  and false if all coordinates are valid. */
    public boolean isInfinite() {
        if (java.lang.Double.isInfinite(this.x)) return true;
        if (java.lang.Double.isInfinite(this.y)) return true;
        return false;
    }
    
    /** Get the x coordinate.
     * @return Value of x coordinate. */
    public double getX() { return this.x; }
    /** Get the y coordinate.
     * @return Value of y coordinate. */
    public double getY() { return this.y; }
    
    
    /** Set the x coordinate.
     * @param x New value of x coordinate. */
    public void setX(double x) { this.x = x; }
    
    /** Set the y coordinate.
     * @param y New value of y coordinate. */
    public void setY(double y) { this.y = y; }
    
    /** Specified the coordinates.
     * @param x new x coordinate.
     * @param y new y coordinate. */
    public void set(double x, double y) {
        this.x = x; this.y = y;
    }
    
    /** Specify the coordinates from an array of 2 values.
     * @param array array containing {x,y} */
    public void set(double[] array) {
        this.x = array[0]; this.y = array[1];
    }
    
    /** Specify the coordinates from another point.
     * @param t point containing new x and y coordinates. */
    public void set(KPoint2D t) {
        this.x = t.x; this.y = t.y;
    }
    
    /************************************************************************
     *** UNITARY OPERATIONS RETURNING point VALUES                       ***
     ************************************************************************/
    
    /*** Polar coordinates ***/
    
    /** Sets the coordinates of this point from polar coordinates.
     *  @param length length of the point.
     *  @param angle angle (in radians) from the x-axis.
     */
    public KPoint2D toPolar(double length, double angle) {
        this.x = length*Math.cos(angle);
        this.y = length*Math.sin(angle);
        return this;
    }
    
    /** Returns a new point from polar coordinates.
     *  @param length length of the point.
     *  @param angle angle (in radians) from the x-axis.
     */
    static public KPoint2D newPolar(double length, double angle) {
        return new KPoint2D().toPolar(length, angle);
    }
    
    /*** Rounding ***/
    
    /** Sets the coordinates of this point to the rounded
     *  values of coordinates in p1. */
    public KPoint2D toRoundOf(KPoint2D p1, double place) {
        BigDecimal bplace = new BigDecimal(place);
        BigDecimal bx = new BigDecimal(p1.x);
        BigDecimal bxr = bx.divide(bplace, 0, BigDecimal.ROUND_HALF_UP);
        BigDecimal bxs = bxr.multiply(bplace);
        this.x = bxs.doubleValue();
        BigDecimal by = new BigDecimal(p1.y);
        BigDecimal byr = by.divide(bplace, 0, BigDecimal.ROUND_HALF_UP);
        BigDecimal bys = byr.multiply(bplace);
        this.y = bys.doubleValue();
        return this;
    }
    
    /** Returns a new point containing the rounded value of the coordinates
     *  of p1. */
    static public KPoint2D newRoundOf(KPoint2D p1, double place) {
        return new KPoint2D().toRoundOf(p1, place);
    }
    
    /** Set coordinates to absolute values. */
    public KPoint2D round(double place) {
        return toRoundOf(this, place);
    }
    
    /*** Absolute values ***/
    
    /** Sets the coordinates of this point to the absolute
     *  value of coordinates in p1. */
    public KPoint2D toAbsOf(KPoint2D p1) {
        this.x = Math.abs(p1.x);  this.y = Math.abs(p1.y);
        return this;
    }
    
    /** Returns a new point containing the absolute value of the coordinates
     *  of p1. */
    static public KPoint2D newAbsOf(KPoint2D p1) {
        return new KPoint2D().toAbsOf(p1);
    }
    
    /** Set coordinates to absolute values. */
    public KPoint2D abs() {
        return toAbsOf(this);
    }
    
    /*** Negative values ***/
    
    /** Sets the coordinates of this point to the negative
     *  value of coordinates in p1. */
    public KPoint2D toNegOf(KPoint2D p1) {
        this.x = -p1.x;
        this.y = -p1.y;
        return this;
    }
    
    /** Returns a new point containing the negative value of the coordinates
     *  of p1. */
    static public KPoint2D newNegOf(KPoint2D p1) {
        return new KPoint2D().toNegOf(p1);
    }
    
    /** Set coordinates to negative values. */
    public KPoint2D neg() {
        return toNegOf(this);
    }
    
    /*** Normalization ***/
    
    /** Sets this point to the normal of p1. Sets the
     * coordinates to Double.POSITIVE_INFINITY if the
     * length of p1 was zero.
     * @param p1
     */
    public KPoint2D toNormOf(KPoint2D p1) {
        double len = Math.sqrt(p1.x*p1.x + p1.y*p1.y);
        if (len == 0.0) {
            this.toInfinite();
        } else {
            this.x = p1.x / len;
            this.y = p1.y / len;
        }
        return this;
    }
    
    /** Returns a new point containing normalized coordinates of p1.
     *  Sets the coordinates to Double.POSITIVE_INFINITY
     *  if the length was zero. */
    static public KPoint2D newNormOf(KPoint2D p1) {
        return new KPoint2D().toNormOf(p1);
    }
    
    /** Sets the length of this point to 1.
     *  Sets the coordinates to Double.POSITIVE_INFINITY
     *  if the length was zero. */
    public KPoint2D norm() {
        return toNormOf(this);
    }
    
    /************************************************************************
     *** BINARY OPERATIONS RETURNING point VALUES                        ***
     ************************************************************************/
    
    /*** Addition ***/
    
    /** Sets the coordinates of this point to the
     * sum of the coordiantes of p1 and p2. */
    public KPoint2D toSumOf(KPoint2D p1, KPoint2D p2) {
        this.x = p1.x + p2.x;
        this.y = p1.y + p2.y;
        return this;
    }
    
    /** Returns a new point containing the sum of the coordinates
     *  of p1 and p2. */
    static public KPoint2D newSumOf(KPoint2D p1, KPoint2D p2) {
        return new KPoint2D().toSumOf(p1, p2);
    }
    
    /** Adds the coordinates of p2 to this point. */
    public KPoint2D add(KPoint2D p2) {
        return toSumOf(this, p2);
    }
    
    /*** Subtraction ***/
    
    /** Sets the coordinates of this point to p1 - p2. */
    public KPoint2D toDiffOf(KPoint2D p1, KPoint2D p2) {
        this.x = p1.x - p2.x;
        this.y = p1.y - p2.y;
        return this;
    }
    
    /** Returns a new point containing the difference between the coordinates
     *  of p1 and p2. */
    static public KPoint2D newDiffOf(KPoint2D p1, KPoint2D p2) {
        return new KPoint2D().toDiffOf(p1, p2);
    }
    
    /** Subtract the coordinates of p2 from this point. */
    public KPoint2D sub(KPoint2D p2) {
        return toDiffOf(this, p2);
    }
    
    /*** Scaling ***/
    
    /** Sets these coordinates to coordinates of p1 multiplied by a constant
     *  scale factor. */
    public KPoint2D toProdOf(double scale, KPoint2D p1) {
        this.x = scale * p1.x;  this.y = scale * p1.y;
        return this;
    }
    
    /** Return a new point having coordinates of p1 multiplied by a constant
     *  scale factor. */
    static public KPoint2D newProdOf(double scale, KPoint2D p1) {
        return new KPoint2D().toProdOf(scale, p1);
    }
    
    /** Multiply coordinates by a constant scale factor. */
    public KPoint2D scale(double scale) {
        return toProdOf(scale, this);
    }
    
    /** Sets these coordinates to coordinates of p1 multiplied by constant
     *  scale factors. */
    public KPoint2D toProdOf(double xscale, double yscale, KPoint2D p1) {
        this.x = xscale * p1.x;  this.y = yscale * p1.y;
        return this;
    }
    
    /** Return a new point having coordinates of p1 multiplied by constant
     *  scale factors. */
    static public KPoint2D newProdOf(double xscale, double yscale, KPoint2D p1) {
        return new KPoint2D().toProdOf(xscale, yscale, p1);
    }
    
    /** Multiply coordinates by a constant scale factor. */
    public KPoint2D scale(double xscale, double yscale) {
        return toProdOf(xscale, yscale, this);
    }
    
    /************************************************************************
     *** MISCELANEOUS point OPERATIONS                                   ***
     ************************************************************************/
    
    /** Sets the coordinates of this point to the linear interpolation
     *  between p1 and p2. Alpha should be in the range [0,1]. An alpha
     *  of 0 would give p1 and an alpha of 1 would give p2. */
    public KPoint2D toInterpolateBetween(double alpha, KPoint2D p1, KPoint2D p2) {
        this.x = (1 - alpha) * p1.x + alpha * p2.x;
        this.y = (1 - alpha) * p1.y + alpha * p2.y;
        return this;
    }
    
    /** Sets the coordinates of this point to the intersection
     *  between line segment AB and line segment CD
     *	or NaN if these two lines do not intersect */
    public KPoint2D toIntersectionOf(KPoint2D A, KPoint2D B, KPoint2D C, KPoint2D D) {
        double p1, p2, denom;
        // The equations for any point along either line are give by
        //	P1 = A + p1 (B - A)
        //	P2 = C + p2 (D - C)
        // Look for the intersection by setting P1==P2 and solving for p1 and p2
        //
        //      (Dx - Cx)(Ay - Cy) - (Dy - Cy)(Ax - Cx)
        // p1 = ---------------------------------------
        //      (Dy - Cy)(Bx - Ax) - (Dx - Cx)(By - Ay)
        //
        //      (Bx - Ax)(Ay - Cy) - (By - Ay)(Ax - Cx)
        // p2 = ---------------------------------------
        //      (Dy - Cy)(Bx - Ax) - (Dx - Cx)(By - Ay)
        //
        // The lines intersect if 0<=p1<=1 and 0<=p2<=1
        //
        denom = (D.y - C.y)*(B.x - A.x) - (D.x - C.x)*(B.y - A.y);
        if (denom == 0) {
            // the lines are parallel
            this.toNaN();
            return this;
        }
        p1 = ((D.x - C.x)*(A.y - C.y) - (D.y - C.y)*(A.x - C.x)) / denom;
        p2 = ((B.x - A.x)*(A.y - C.y) - (B.y - A.y)*(A.x - C.x)) / denom;
        
        if ((p1 < 0 || p1 > 1.0) || (p2 < 0 || p2 > 1.0)) {
            this.toNaN();
            return this;
        }
        
        // the lines intersect. set this to the intersection point
        this.x = A.x + p1*(B.x - A.x);
        this.y = A.y + p1*(B.y - A.y);
        return this;
    }
    
    /** find the shortest distance between the line segment AB and the
     *  point C by dropping a perpendicular from C to AB. The coordinates of
     *  this point are set to a point along AB and CP which perpendicular
     *  to AB. If this point is outside of AB, then it is set to NaN.
     */
    public KPoint2D toPerpProjOf(KPoint2D A, KPoint2D B, KPoint2D C) {
        double lensq, r, dx, dy;
        // The value r represents the distance from A along AB at
        // which P is located. It is given by the equation
        //       AC dot AB
        //   r = ---------
        //        |AB|^2
        //
        //  If 0 <= r <= 1 Then P lies on the segement AB and
        //  P is given by Px = Ax + r(Bx-Ax); Py = Ay + r(By-Ay)
        dx = B.x - A.x;
        dy = B.y - A.y;
        lensq = dx*dx + dy*dy;
        if (lensq == 0.0) {
            this.toNaN();
            return this;
        }
        r = ((C.x - A.x)*dx + (C.y - A.y)*dy)/lensq;
        
        if (r < 0 || r > 1) {
            this.toNaN();
        } else {
            this.x = A.x + r*dx;
            this.y = A.y + r*dy;
        }
        return this;
    }

    /** Sets this point perpendicular to p1 by rotating p1
     *  90 degrees counter-clockwise. */
    public KPoint2D toPerpOf(KPoint2D p1) {
        this.x = -p1.y;
        this.y = p1.x;
        return this;
    }
    
    /** Rotates this point by angle (specified in radians). */
    public KPoint2D rotateBy(double dAngle) {
        double xold = this.x;
        double yold = this.y;
        //  /  \   /                          \   /  \
        //  |Bx|   |  cos(theta)  -sin(theta) |   |Ax|
        //  |  | = |                          | x |  |
        //  |By|   |  sin(theta)   cos(theta) |   |Ay|
        //  \  /   \                          /   \  /
        double dCos=Math.cos(dAngle), dSin=Math.sin(dAngle);
        this.x = xold*dCos - yold*dSin;
        this.y = xold*dSin + yold*dCos;
        return this;
    }
    
    /************************************************************************
     *** OPERATIONS RETURNING SCALAR VALUES                               ***
     ************************************************************************/
    
    /** Returns the distance between p1 and p2. */
    static public double distBetween(KPoint2D p1, KPoint2D p2) {
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        return Math.sqrt(dx*dx + dy*dy);
    }
    
    /** Returns the square of the distance between p1 and p2. */
    static public double distSqBetween(KPoint2D p1, KPoint2D p2) {
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        return dx*dx + dy*dy;
    }
    
    /** Computes the L1 distance, |Dx|+|Dy|, between p1 and p2. */
    static public double distL1Between(KPoint2D p1, KPoint2D p2) {
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        if (dx < 0) dx = -dx;
        if (dy < 0) dy = -dy;
        return dx + dy;
    }
    
    /** Computes the L-infinite distance, max(|Dx|,|Dy|),
     * between p1 and p2. */
    static public double distLinfBetween(KPoint2D p1, KPoint2D p2) {
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        if (dx < 0) dx = -dx;
        if (dy < 0) dy = -dy;
        return (dx > dy) ? dx : dy;
    }
    
    /** Computes the distance between this and p2. */
    public double distTo(KPoint2D p2) {
        return distBetween(this, p2);
    }
    
    /** Computes the square of the distance between this and p2. */
    public double distSqTo(KPoint2D p2) {
        return distSqBetween(this, p2);
    }
    
    /** Computes the L1 distance, |Dx|+|Dy|, between this and p2. */
    public double distL1To(KPoint2D p2) {
        return distL1Between(this, p2);
    }
    
    /** Computes the L-infinite distance, max(|Dx|,|Dy|),
     * between this and p2. */
    public double distLinfTo(KPoint2D p2) {
        return distLinfBetween(this, p2);
    }
    
    /** Returns the length of p1.*/
    static public double lengthOf(KPoint2D p1) {
        return Math.sqrt(p1.x*p1.x + p1.y*p1.y);
    }
    
    /** Returns the square of the length of p1. */
    static public double lengthSqOf(KPoint2D p1) {
        return p1.x*p1.x + p1.y*p1.y;
    }
    
    /** Returns the length of this point.*/
    public double length() {
        return lengthOf(this);
    }
    
    /** Returns the square of the length of this point. */
    public double lengthSq() {
        return lengthSqOf(this);
    }
    
    /** Returns true if all of the coordinates
     *  of p1 and p2 are equal. */
    static public boolean equals(KPoint2D p1, KPoint2D p2) {
        return p1.x == p2.x && p1.y == p2.y;
    }
    
    /** Returns true if all of the coordinates
     *  of p1 are within 'epsilon' of the coordinates
     *  of p2. */
    static public boolean epsilonEquals(KPoint2D p1, KPoint2D p2, double epsilon) {
        double d;
        d = p2.x - p1.x;
        if (d < 0)
            d = -d;
        if (d > epsilon)
            return false;
        d = p2.y - p1.y;
        if (d < 0)
            d = -d;
        if (d > epsilon)
            return false;
        return true;
    }
    
    /** Returns true if all of the coordinates
     *  of this and p2 are equal. */
    public boolean equals(KPoint2D p2) {
        return equals(this, p2);
    }
    
    /** Returns true if all of the coordinates
     *  of this are within 'epsilon' of the coordinates
     *  of p2. */
    public boolean epsilonEquals(KPoint2D p2, double epsilon) {
        return epsilonEquals(this, p2, epsilon);
    }
    
    /** Find the angle (in radians) between the points A and B. */
    static public double angleBetween(KPoint2D A, KPoint2D B) {
        double dot = A.x*B.x + A.y*B.y;     // = |A||B|cos(theta)
        double cross = A.x*B.y - A.y*B.x;   // = |A||B|sin(theta)
        return Math.atan2(cross, dot);
    }
    
    /** Find the angle (in radians) between this and B. */
    public double angleTo(KPoint2D B) {
        return angleBetween(this, B);
    }
    
    /** Returns the dot product between p1 and p2. */
    static public double dotOf(KPoint2D p1, KPoint2D p2) {
        return p1.x*p2.x + p1.y*p2.y;
    }
    
    /** Returns the dot product between this and p2. */
    public double dot(KPoint2D p2) {
        return dotOf(this, p2);
    }

    /** Convert a point into a java.awt.geom.Rectangle2D.Double centered on a point for display */
    public Rectangle2D.Double toRectangleShape(double width, double height) {
	return new Rectangle2D.Double(x-width/2, y-height/2, width, height);
    }
    
    /** Convert a polygon into a java.awt.geom.Ellipse2D.Double centered on a point for display */
    public Ellipse2D.Double toCircleShape(double radius) {
	return new Ellipse2D.Double(x-radius/2, y-radius/2, radius, radius);
    }
}

