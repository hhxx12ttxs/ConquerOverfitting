package uk.ac.lkl.common.ui;

import java.awt.geom.Line2D;

import uk.ac.lkl.common.util.value.NumericValue;

/**
 * An abstract typed line.
 * 
 * This class is parameterised on the Number type (N) used for its definition
 * and the point type (P) used to define the two points that constitute the
 * line. The point type (P) extends AbstractTypedPoint which is also
 * parameterised on N as well as some sub-class of TypedOrdinate<N>.
 * 
 * @param N
 *                the Number type
 * @param P
 *                the Point type
 * 
 * @author $Author: darren.pearce $
 * @version $Revision: 1345 $
 * @version $Date: 2008-10-22 15:01:33 +0200 (Wed, 22 Oct 2008) $
 * 
 */
public class AbstractTypedLine<N extends NumericValue<N>, P extends AbstractTypedPoint<N, ? extends TypedOrdinate<N>>>
	implements TypedLineObject<N> {

    /**
     * The first point of this line.
     * 
     */
    protected P point1;

    /**
     * The second point of this line.
     * 
     */
    protected P point2;

    /**
     * Create a new instance using the given points.
     * 
     * @param point1
     *                the first point
     * @param point2
     *                the second point
     * 
     */
    public AbstractTypedLine(P point1, P point2) {
	this.point1 = point1;
	this.point2 = point2;
    }

    public void translate(N xDiff, N yDiff) {
	N newX1 = point1.getX().add(xDiff);
	N newY1 = point1.getY().add(yDiff);
	N newX2 = point2.getX().add(xDiff);
	N newY2 = point2.getY().add(yDiff);
	setLine(newX1, newY1, newX2, newY2);
    }

    /**
     * Set the values of the ordinates used to define this line.
     * 
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * 
     */
    public void setLine(N x1, N y1, N x2, N y2) {
	point1.setLocation(x1, y1);
	point2.setLocation(x2, y2);
    }

    /**
     * Get a Line2D.Double instance corresponding to this class.
     * 
     * Note that the returned Line2D.Double instance can be changed
     * independently of this class.
     * 
     * @return a line corresponding to this class
     * 
     */
    public Line2D.Double getLine() {
	Line2D.Double line = new Line2D.Double();
	updateLine(line);
	return line;
    }

    /**
     * Update the specified line so that it correspond to this class.
     * 
     * This method is used within this class on each new line created in
     * getLine() as well as in NotifyingLine to update its cached instance.
     * 
     * @param line
     *                the line to update
     * 
     */
    protected final void updateLine(Line2D.Double line) {
	N x1 = point1.getX();
	N y1 = point1.getY();
	N x2 = point2.getX();
	N y2 = point2.getY();

	if (x1 == null || y1 == null || x2 == null || y2 == null)
	    return;

	double x1d = x1.doubleValue();
	double y1d = y1.doubleValue();
	double x2d = x2.doubleValue();
	double y2d = y2.doubleValue();

	// todo: set some sort of error status that line is not valid
	if (Double.isNaN(x1d) || Double.isNaN(y1d) || Double.isNaN(x2d)
		|| Double.isNaN(y2d))
	    return;

	line.setLine(x1d, y1d, x2d, y2d);
    }

    public N getX1() {
	return point1.getX();
    }

    public N getY1() {
	return point1.getY();
    }

    public N getX2() {
	return point2.getX();
    }

    public N getY2() {
	return point2.getY();
    }

    /**
     * Get the first point of this line.
     * 
     * @return the first point
     * 
     */
    public P getPoint1() {
	return point1;
    }

    /**
     * Get the second point of this line.
     * 
     * @return the second point
     * 
     */
    public P getPoint2() {
	return point2;
    }

    // todo: make more efficient - creates line again (in base class)
    public double getLength() {
	Line2D.Double line = getLine();
	double x1 = line.getX1();
	double x2 = line.getX2();
	double xDiff = x2 - x1;
	double y1 = line.getY1();
	double y2 = line.getY2();
	double yDiff = y2 - y1;
	double xDiff2 = xDiff * xDiff;
	double yDiff2 = yDiff * yDiff;
	double length2 = xDiff2 + yDiff2;
	double length = Math.sqrt(length2);
	return length;
    }

    /**
     * Convert this instance to a string representation.
     * 
     * @return a string representation
     * 
     */
    @Override
    public String toString() {
	return getClass().getName() + "[" + point1.toString() + ", "
		+ point2.toString() + "]";
    }

    /**
     * Determine whether this instance is equal to another object.
     * 
     * 
     * 
     * @param object
     *                the object to compare to
     * 
     * @return <code>true</code> if object is an instance of
     *         <code>AbstractTypedLine</code> with identical points under
     *         <code>.equals()</code>; <code>false</code> otherwise
     * 
     */
    @SuppressWarnings("unchecked")
    public boolean equals(Object object) {
	if (!(object instanceof AbstractTypedLine))
	    return false;

	AbstractTypedLine<N, P> other = (AbstractTypedLine<N, P>) object;

	return this.point1.equals(other.point1)
		&& this.point2.equals(other.point2);
    }

}

