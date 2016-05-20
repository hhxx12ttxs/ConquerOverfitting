package uk.ac.lkl.common.ui;

import java.awt.geom.Rectangle2D;

import uk.ac.lkl.common.util.value.NumericValue;

/**
 * An abstract typed rectangle.
 * 
 * This class is parameterised on the Number type that this rectangle will use
 * as well as the sub-classes of AbstractTypedPoint and TypedOrdinate that are
 * in turn used to define the rectangle. By parameterising them in this way,
 * methods such as <code>getLocation()</code> or <code>getWidth()</code> can
 * return the appropriate sub-class type rather than the base-class.
 * 
 * <p>
 * Note that, in contrast, to <code>AbstractTypedLine</code>, this class has
 * three generic parameters. This is because
 * 
 * <pre>
 * P extends AbstractTypedPoint&lt;N, ? extends TypedOrdinate&lt;N&gt;&gt;
 * </pre>
 * 
 * is now specified using a separate parameter, O:
 * 
 * <pre>
 * P extends AbstractTypedPoint&lt;N, O&gt;, O extends TypedOrdinate&lt;N&gt;
 * </pre>
 * 
 * This is because this class needs access to O in its class definition whereas
 * <code>AbstractTypedLine</code> did not.
 * </p>
 * 
 * @param N
 *            the number type
 * @param P
 *            the point type
 * @param O
 *            the ordinate type
 * 
 * @author $Author: darren.pearce $
 * @version $Revision: 1345 $
 * @version $Date: 2008-10-22 15:01:33 +0200 (Wed, 22 Oct 2008) $
 * 
 */
public class AbstractTypedRectangle<N extends NumericValue<N>, O extends TypedOrdinate<N>>
	implements TypedRectangleObject<N> {

    /**
     * The x ordinate of this rectangle.
     * 
     */
    private O x;

    /**
     * The y ordinate of this rectangle.
     * 
     */
    private O y;

    /**
     * The width of this rectangle.
     * 
     */
    private O width;

    /**
     * The height of this rectangle.
     * 
     */
    private O height;

    /**
     * Create a new instance using the given ordinates.
     * 
     * @param x
     *            the x ordinate
     * @param y
     *            the y ordinate
     * @param width
     *            the width
     * @param height
     *            the height
     * 
     */
    public AbstractTypedRectangle(O x, O y, O width, O height) {
	this.x = x;
	this.y = y;
	this.width = width;
	this.height = height;
    }

    public void setRectangle(TypedRectangleObject<N> rectangle) {
	setRectangle(rectangle.getX(), rectangle.getY(), rectangle.getWidth(),
		rectangle.getHeight());
    }

    /**
     * Set the ordinates used to define this instance.
     * 
     * @param x
     *            the x ordinate of its location
     * @param y
     *            the y ordinate of its location
     * @param width
     *            the width
     * @param height
     *            the height
     * 
     */
    public void setRectangle(N x, N y, N width, N height) {
	this.x.setValue(x);
	this.y.setValue(y);
	this.width.setValue(width);
	this.height.setValue(height);
    }

    /**
     * Get a Rectangle2D.Double instance corresponding to this class.
     * 
     * Note that the returned Rectangle2D.Double instance can be changed
     * independently of this class.
     * 
     * @return a rectangle corresponding to this class
     * 
     */
    public Rectangle2D.Double getRectangle() {
	Rectangle2D.Double rectangle = new Rectangle2D.Double();
	updateRectangle(rectangle);
	return rectangle;
    }

    public boolean contains(AbstractTypedPoint<N, O> point) {
	return contains(point.getX(), point.getY());
    }

    // is strictly *within* rect. i.e. doesn't include edge.
    // hack?
    public boolean contains(N x, N y) {
	N x1 = getX();
	N y1 = getY();
	N x2 = x1.add(getWidth());
	N y2 = y1.add(getHeight());

	return x.isGreaterThanOrEqual(x1) && x.isLessThanOrEqual(x2)
		&& y.isGreaterThanOrEqual(y1) && y.isLessThanOrEqual(y2);
    }

    /**
     * Update the specified rectangle so that it correspond to this class.
     * 
     * This method is used within this class on each new rectangle created in
     * getRectangle() as well as in NotifyingRectangle to update its cached
     * instance.
     * 
     * @param rectangle
     *            the rectangle to update
     * 
     */
    protected final void updateRectangle(Rectangle2D.Double rectangle) {
	N x = getX();
	N y = getY();
	N w = width.getValue();
	N h = height.getValue();

	if (x == null || y == null || w == null || h == null)
	    return;

	double xd = x.doubleValue();
	double yd = y.doubleValue();
	double wd = w.doubleValue();
	double hd = h.doubleValue();

	// todo: set some sort of error status that line is not valid
	if (Double.isNaN(xd) || Double.isNaN(yd) || Double.isNaN(wd)
		|| Double.isNaN(hd))
	    return;

	rectangle.setRect(xd, yd, wd, hd);
    }

    public N getX() {
	return x.getValue();
    }

    public N getY() {
	return y.getValue();
    }

    public O getXOrdinate() {
	return x;
    }

    public O getYOrdinate() {
	return y;
    }

    /**
     * Get the width ordinate of this rectangle.
     * 
     * @return the width ordinate
     * 
     */
    public O getWidthOrdinate() {
	return width;
    }

    /**
     * Get the width of this rectangle.
     * 
     * @return the width
     * 
     */
    public N getWidth() {
	return width.getValue();
    }

    /**
     * Get the height ordinate of this rectangle.
     * 
     * @return the height ordinate
     * 
     */
    public O getHeightOrdinate() {
	return height;
    }

    /**
     * Get the height of this rectangle.
     * 
     * @return the height
     * 
     */
    public N getHeight() {
	return height.getValue();
    }

    /**
     * Convert this instance to a string representation.
     * 
     * @return a string representation
     * 
     */
    @Override
    public String toString() {
	return "AbstractTypedRectangle[" + x.getValue() + ", " + y.getValue()
		+ ", " + width.getValue() + ", " + height.getValue() + "]";
    }

    /**
     * Determine whether this instance is equal to another object.
     * 
     * @param object
     *            the object to compare to
     * 
     * @return <code>true</code> if object is an instance of
     *         <code>AbstractTypedRectangle</code> with identical points under
     *         <code>.equals()</code>; <code>false</code> otherwise
     * 
     */
    @SuppressWarnings("unchecked")
    public boolean equals(Object object) {
	if (!(object instanceof AbstractTypedRectangle))
	    return false;

	AbstractTypedRectangle<N, O> other =
		(AbstractTypedRectangle<N, O>) object;

	return this.x.equals(other.x) && this.y.equals(other.y)
		&& this.width.equals(other.width)
		&& this.height.equals(other.height);
    }

}

