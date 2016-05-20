package uk.ac.lkl.migen.mockup.shapebuilder.model.shape;

import java.awt.Color;
import java.awt.Graphics2D;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.*;

import org.apache.log4j.Logger;

import uk.ac.lkl.common.ui.NotifyingLine;
import uk.ac.lkl.common.ui.NotifyingPoint;

import uk.ac.lkl.migen.mockup.shapebuilder.*;
import uk.ac.lkl.migen.mockup.shapebuilder.model.*;

import uk.ac.lkl.common.util.value.DoubleValue;

import uk.ac.lkl.common.util.event.UpdateEvent;
import uk.ac.lkl.common.util.event.UpdateListener;

/**
 * A shape that consist of one or more connected lines.
 * 
 * The length of each of these lines is defined by an expression.
 * 
 * todo: clean up use of DoubleValue. Using 'double' a lot when probably better
 * to stay in 'realm' of DoubleValue and convert only when needed.
 * 
 * @author $Author: darren.pearce $
 * @version $Revision: 1348 $
 * @version $Date: 2008-10-22 15:53:53 +0200 (Wed, 22 Oct 2008) $
 * 
 */
public class ExpressedShape extends Expressed {

    static Logger logger = Logger.getLogger(ExpressedShape.class);

    static Logger dplogger = Logger.getLogger("Darren");

    public static final Color DEFAULT_LINE_COLOR = Color.BLACK;

    public static final Color DEFAULT_FILL_COLOR = Color.YELLOW;

    private ID id;

    /**
     * The location of this shape.
     * 
     */
    private NotifyingPoint<DoubleValue> location;

    /**
     * The angle of the shape
     */
    private int angle;

    private List<PathSegment> pathSegmentList;

    /**
     * The expressions that this shape uses to construct itself. Each path
     * segment may use zero or more of these expression to define its length.
     * 
     * Note that each of these is an expression element which wraps up an
     * expression with an id.
     * 
     */
    private HashMap<String, Expression> parameterMap;

    private ArrayList<String> parameterNames;

    /**
     * The lines generated from the underlying path segments.
     * 
     */
    private ArrayList<NotifyingLine<DoubleValue>> lines;

    /**
     * The points generated from the underlying path segments.
     * 
     */
    private ArrayList<NotifyingPoint<DoubleValue>> points;

    /**
     * The map from each line to its corresponding expression.
     * 
     */
    private HashMap<NotifyingLine<DoubleValue>, Expression> lineMap;

    // map from String to shape expression. Is so can
    // re-use properly.
    private HashMap<String, ShapeExpression> shapeExpressionMap;

    /**
     * The fill colour of this shape.
     * 
     */
    private Color fillColor;

    private Color transparentFillColor;

    /**
     * The line colour of this shape.
     * 
     */
    private Color lineColor;

    private boolean linesSelectable;

    /**
     * The change listener used to monitor various data about this shape.
     * 
     * This is attached to expressions.
     * 
     */
    private UpdateListener<Expression> expressionUpdateListener =
	    new UpdateListener<Expression>() {

		public void objectUpdated(UpdateEvent<Expression> e) {
		    calculatePoints();
		    fireObjectUpdated();
		}
	    };

    /**
     * This is attached to points. Note that it does not need to be attached to
     * lines since they are based on points and listen to these themselves.
     * 
     */
    private UpdateListener<NotifyingPoint<DoubleValue>> pointUpdateListener =
	    new UpdateListener<NotifyingPoint<DoubleValue>>() {

		public void objectUpdated(
			// repeated code
			UpdateEvent<NotifyingPoint<DoubleValue>> e) {
		    calculatePoints();
		    fireObjectUpdated();
		}
	    };

    /**
     * Indicates whether or not all expressions are defined.
     * 
     * 
     * 
     */
    private boolean allParametersDefined;

    private boolean settingPoint = false;

    /**
     * Create a new instance at the given location that uses the specified
     * expressions.
     * 
     * @param location
     * @param expressions
     */
    public ExpressedShape() {
	this.id = IDFactory.newId();
	setFillColor(DEFAULT_FILL_COLOR);
	setLineColor(DEFAULT_LINE_COLOR);
	this.lines = new ArrayList<NotifyingLine<DoubleValue>>();
	this.location =
		new NotifyingPoint<DoubleValue>(new DoubleValue(0.0),
			new DoubleValue(0.0));
	this.angle = 0;

	this.pathSegmentList = new ArrayList<PathSegment>();
	this.parameterMap = new HashMap<String, Expression>();
	this.parameterNames = new ArrayList<String>();

	this.points = new ArrayList<NotifyingPoint<DoubleValue>>();
	this.points.add(new NotifyingPoint<DoubleValue>(new DoubleValue(0.0),
		new DoubleValue(0.0)));

	this.lineMap = new HashMap<NotifyingLine<DoubleValue>, Expression>();

	this.shapeExpressionMap = new HashMap<String, ShapeExpression>();
	this.linesSelectable = true;
    }

    public void setLinesSelectable(boolean linesSelectable) {
	// todo: should fire event so shape plotter, etc, can change their set
	// of detected lines
	this.linesSelectable = linesSelectable;
    }

    public boolean areLinesSelectable() {
	return linesSelectable;
    }

    public ID getId() {
	return id;
    }

    public int getAngle() {
	return this.angle;
    }

    public void setAngle(int angle) {
	this.angle = angle % 360;
    }

    // hack: uses same ref to path segment set. This is immutable at the moment
    // but may not be in the future...
    public ExpressedShape createCopy(NotifyingPoint<DoubleValue> location) {
	ExpressedShape copy = new ExpressedShape();
	copy.setLocation(location.getX().doubleValue(), location.getY()
		.doubleValue());
	for (PathSegment pathSegment : pathSegmentList)
	    copy.addPathSegment(pathSegment);

	Collection<String> handles = getParameterNames();
	for (String handle : handles) {
	    Expression expression = getParameterExpression(handle);
	    copy.addNamedExpression(expression, handle);
	}

	copy.setLineColor(lineColor);
	copy.setFillColor(fillColor);
	logger.info("Shape ID:" + copy.getId() + " is copy of ID:"
		+ this.getId() + ".");
	return copy;
    }

    protected void addNamedExpression(Expression expression, String name) {
	if (parameterMap.containsKey(name))
	    throw new IllegalArgumentException("Name '" + name
		    + "' already exists");

	Expression currentExpression = parameterMap.get(name);
	if (currentExpression != null)
	    currentExpression.removeUpdateListener(expressionUpdateListener);

	parameterMap.put(name, expression);
	parameterNames.add(name);

	if (expression != null)
	    expression.addUpdateListener(expressionUpdateListener);

	updateAllParametersDefined();
    }

    // protected void addExpressionElement(ExpressionElement expressionElement)
    // {
    // expressionElements.add(expressionElement);
    // if (expressionElement != null)
    // expressionElement.getExpression().addChangeListener(changeListener);
    //
    // updateAllExpressionElementsDefined();
    // }

    public ShapeExpression getShapeExpression(String name) {
	ShapeExpression shapeExpression = shapeExpressionMap.get(name);
	if (shapeExpression == null) {
	    shapeExpression = new ShapeExpression(this, name);
	    shapeExpressionMap.put(name, shapeExpression);
	}
	return shapeExpression;
    }

    // indirect through shape expression so evaluates to current each time
    public void addPathSegment(int turnAngle, String expressionName) {
	addPathSegment(turnAngle, getShapeExpression(expressionName));
    }

    public void addPathSegment(int turnAngle, Expression expression) {
	addPathSegment(new PathSegment(turnAngle, expression));
    }

    // todo: write removePathSegment
    public void addPathSegment(PathSegment pathSegment) {
	pathSegmentList.add(pathSegment);
	NotifyingPoint<DoubleValue> point =
		new NotifyingPoint<DoubleValue>(new DoubleValue(0.0),
			new DoubleValue(0.0));
	point.addUpdateListener(pointUpdateListener);
	points.add(point);

	int numPoints = points.size();
	int startIndex = numPoints - 2;
	int endIndex = numPoints - 1;

	NotifyingPoint<DoubleValue> start = points.get(startIndex);
	NotifyingPoint<DoubleValue> end = points.get(endIndex);
	Expression expression = pathSegment.getExpression();
	NotifyingLine<DoubleValue> line =
		new NotifyingLine<DoubleValue>(start, end);
	// hack to have both these data structures really
	lines.add(line);
	lineMap.put(line, expression);

	updateAllParametersDefined();
	calculatePoints();
    }

    /**
     * Calculate the points given the location and the current values of the
     * expressions.
     * 
     * Note that this uses the existing NotifyingPoint instances.
     * 
     */
    private void calculatePoints() {
	updateAllParametersDefined();

	if (!allParametersDefined || settingPoint)
	    return;

	Point2D.Double point = new Point2D.Double();
	point.setLocation(location.getX().doubleValue(), location.getY()
		.doubleValue());

	points.get(0).setLocation(location.getX(), location.getY());
	int shapeAngle = this.getAngle();
	for (int i = 0; i < pathSegmentList.size(); i++) {
	    PathSegment pathSegment = pathSegmentList.get(i);
	    int angle = pathSegment.getAngle() + shapeAngle;
	    Expression expression = pathSegment.getExpression();
	    double value = expression.getValue();
	    double x =
		    Math.round(point.x + value
			    * Math.cos(Math.toRadians(angle)));
	    double y =
		    Math.round(point.y + value
			    * Math.sin(Math.toRadians(angle)));
	    point.setLocation(x, y);
	    // note: i + 1
	    points.get(i + 1).setLocation(new DoubleValue(point.getX()),
		    new DoubleValue(point.getY()));
	}
    }

    public void update() {
	calculatePoints();
    }

    private void refreshLineMap() {
	for (int i = 0; i < points.size() - 1; i++)
	    refreshLineMap(i);
    }

    private void refreshLineMap(int index) {
	NotifyingLine<DoubleValue> line = lines.get(index);
	PathSegment pathSegment = pathSegmentList.get(index);
	Expression expression = pathSegment.getExpression();
	lineMap.put(line, expression);
    }

    /**
     * Updates the boolean indicating whether all expressions are defined.
     * 
     */
    private void updateAllParametersDefined() {
	allParametersDefined = calculateAllParametersDefined();
    }

    /**
     * Determines whether all expressions are non-<code>null</code> or not.
     * 
     * @return <code>true</code> if all expressions are non-<code>null</code>
     *         and evaluate to non-<code>null</code>; <code>false</code>
     *         otherwise
     * 
     */
    private boolean calculateAllParametersDefined() {
	for (Expression expression : parameterMap.values())
	    if (expression == null || expression.getValue() == null)
		return false;
	return true;
    }

    /**
     * Returns whether all expressions are defined or not.
     * 
     * @return <code>true</code> if all expressions are defined;
     *         <code>false</code> otherwise
     * 
     */
    public boolean areAllParametersDefined() {
	return allParametersDefined;
    }

    public boolean isImmutable() {
	// hack
	if (!allParametersDefined)
	    return true;

	for (Expression expression : parameterMap.values())
	    if (expression.isMutable())
		return false;

	return true;
    }

    public List<String> getParameterNames() {
	return Collections.unmodifiableList(parameterNames);
    }

    public Expression getParameterExpression(String name) {
	return parameterMap.get(name);
    }

    /**
     * Return the number of parameters exposed to clients as defining this
     * instance.
     * 
     * @return the number of parameters
     * 
     */
    public int getNumParameters() {
	return parameterMap.size();
    }

    public Color getLineColor() {
	return lineColor;
    }

    public Color getFillColor() {
	return fillColor;
    }

    public void setLineColor(Color lineColor) {
	if (this.lineColor != null && this.lineColor.equals(lineColor))
	    return;

	this.lineColor = lineColor;
	fireObjectUpdated();
    }

    public void setFillColor(Color fillColor) {
	if (this.fillColor != null && this.fillColor.equals(fillColor))
	    return;

	this.fillColor = fillColor;
	this.transparentFillColor =
		new Color(fillColor.getRed(), fillColor.getGreen(), fillColor
			.getBlue(), 100);
	fireObjectUpdated();
    }

    // for debugging
    public void printPoints() {
	for (int i = 0; i < points.size(); i++) {
	    NotifyingPoint<DoubleValue> point = points.get(i);
	    System.out.println("Point " + i + ": " + point.getX() + ", "
		    + point.getY());
	}
    }

    /**
     * Get the expression corresponding to the given line.
     * 
     * @param line
     *            the line
     * 
     * @return the expression
     * 
     */
    public Expression getLineExpression(NotifyingLine<DoubleValue> line) {
	return lineMap.get(line);
    }

    /**
     * Get the location of this instance.
     * 
     * @return the location
     * 
     */
    public NotifyingPoint<DoubleValue> getLocation() {
	return location;
    }

    /**
     * Set the location of this shape to the given coordinate.
     * 
     * @param x
     *            the x-ordinate
     * @param y
     *            the y-ordinate
     * 
     */
    public void setLocation(double x, double y) {
	if (x == location.getX().doubleValue()
		&& y == location.getY().doubleValue())
	    return;

	logShapeMovement(x, y);

	double xDiff = location.getX().doubleValue() - x;
	double yDiff = location.getY().doubleValue() - y;

	location.setLocation(new DoubleValue(x), new DoubleValue(y));

	for (NotifyingLine<DoubleValue> line : lines)
	    line.translate(new DoubleValue(xDiff), new DoubleValue(yDiff));

	// calculatePoints();
    }

    private void logShapeMovement(double x, double y) {
	String result = new String();
	result += "Shape ID:" + this.getId() + " (";
	for (Iterator<String> itr = parameterMap.keySet().iterator(); itr
		.hasNext();) {
	    String parameterName = itr.next();
	    Expression parameter = parameterMap.get(parameterName);

	    Double value = parameter == null ? null : parameter.getValue();

	    ID id = parameter == null ? null : parameter.getId();

	    result += parameterName + ":" + value + "[ID:" + id + "]";
	    result += ", ";
	}
	result += "Color:" + this.fillColor;
	result += ") is now at " + location + ".";
	logger.info(result);
    }

    /**
     * Change the expression for the given index.
     * 
     * This allows the dynamic editing of the expressions that define this
     * shape. The change listener is detached from the old expression and
     * attached to the new one.
     * 
     * <p>
     * <b>Improvement:</b>Need to be more sensible about listeners since a shape
     * may use the same expression more than once (for example, a square).
     * </p>
     * 
     * @param index
     *            the index to set
     * @param expression
     *            the expression to set it to
     * 
     */
    public void setExpression(String name, Expression expression) {
	Expression currentExpression = parameterMap.get(name);
	if (currentExpression != null)
	    currentExpression.removeUpdateListener(expressionUpdateListener);

	parameterMap.put(name, expression);

	if (expression != null)
	    expression.addUpdateListener(expressionUpdateListener);

	updateAllParametersDefined();
	calculatePoints();

	if (expression == null) {
	    logger.info("Shape ID:" + this.getId() + " lost its expression at "
		    + "parameter '" + name + "'.");
	} else {
	    logger.info("Expression ID:" + expression.getId() + " (name: "
		    + expression.getName() + "," + " value: "
		    + expression.getValue() + ")" + " is attached to shape ID:"
		    + this.getId() + ", parameter `" + name + "'.");
	}

	// for moment, do all. Eventually do only relevant indices.
	refreshLineMap();
	fireObjectUpdated();
    }

    /**
     * Get the lines representing this instance.
     * 
     * @return the list of lines
     * 
     */
    public List<NotifyingLine<DoubleValue>> getLines() {
	return Collections.unmodifiableList(lines);
    }

    public NotifyingLine<DoubleValue> getLine(int index) {
	return lines.get(index);
    }

    public NotifyingPoint<DoubleValue> getPoint(int index) {
	return points.get(index);
    }

    /**
     * Get the points representing this instance.
     * 
     * @return the list of points
     * 
     */
    public List<NotifyingPoint<DoubleValue>> getPoints() {
	return Collections.unmodifiableList(points);
    }

    // hack convenience method for creating a polygon based on the points
    private GeneralPath createPath(int gridSize) {
	int numPoints = points.size();

	GeneralPath path = new GeneralPath();
	path.moveTo(points.get(0).getX().floatValue() * gridSize, points.get(0)
		.getY().floatValue()
		* gridSize);

	for (int i = 1; i < numPoints; i++) {
	    NotifyingPoint<DoubleValue> point = points.get(i);
	    double x = point.getX().doubleValue() * gridSize;
	    double y = point.getY().doubleValue() * gridSize;
	    path.lineTo((float) x, (float) y);
	}

	// path.closePath();

	return path;
    }

    // hack - creates a polygon each time since points may have changed
    // assumes that x, y, is in model coord space
    public boolean contains(double x, double y) {
	GeneralPath path = createPath(1);
	return path.contains(x, y);
    }

    /**
     * Move the location of this shape by the given amounts.
     * 
     * @param xDelta
     *            the x movement
     * @param yDelta
     *            the y movement
     * 
     */
    public void translate(double xDelta, double yDelta) {
	setLocation(location.getX().doubleValue() + xDelta, location.getY()
		.doubleValue()
		+ yDelta);
    }

    // hack: inefficient and incompatible with Shape interface. Needs cleanup
    // and
    // optimisation.
    public Rectangle2D.Double getBounds2D() {
	Double minX = null;
	Double maxX = null;
	Double minY = null;
	Double maxY = null;
	for (NotifyingPoint<DoubleValue> point : points) {
	    double x = point.getX().doubleValue();
	    double y = point.getY().doubleValue();
	    if (minX == null || x < minX)
		minX = x;
	    if (minY == null || y < minY)
		minY = y;
	    if (maxX == null || x > maxX)
		maxX = x;
	    if (maxY == null || y > maxY)
		maxY = y;
	}
	return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Paint the shape on the given graphics context using the specified grid
     * size.
     * 
     * This method only has an effect if all expressions are defined.
     * 
     * <p>
     * <b>Improvement</b>This sort of functionality shouldn't be necessary.
     * Instead, it should return a GeneralPath which can be appropriately
     * transformed by interested painters.
     * </p>
     * 
     * @param g2
     *            the graphics context
     * @param gridSize
     *            the grid size
     * 
     */
    public void paint(Graphics2D g2, int gridSize) {
	fill(g2, gridSize);
	draw(g2, gridSize);
    }

    /**
     * Fill the shape on the given graphics context using the specified grid
     * size.
     * 
     * This method only has an effect if all expressions are defined.
     * 
     * <p>
     * <b>Improvement</b>This sort of functionality shouldn't be necessary.
     * Instead, it should return a GeneralPath which can be appropriately
     * transformed by interested painters.
     * </p>
     * 
     * @param g2
     *            the graphics context
     * @param gridSize
     *            the grid size
     * 
     */
    public void fill(Graphics2D g2, int gridSize) {
	if (!allParametersDefined)
	    return;

	// hack - should keep a shape cached for this
	GeneralPath path = createPath(gridSize);
	g2.setColor(transparentFillColor);
	g2.fill(path);
    }

    /**
     * Draw the shape on the given graphics context using the specified grid
     * size.
     * 
     * This method only has an effect if all expressions are defined.
     * 
     * <p>
     * <b>Improvement</b>This sort of functionality shouldn't be necessary.
     * Instead, it should return a GeneralPath which can be appropriately
     * transformed by interested painters.
     * </p>
     * 
     * @param g2
     *            the graphics context
     * @param gridSize
     *            the grid size
     * 
     */
    public void draw(Graphics2D g2, int gridSize) {
	// if don't have resize tool then no need to do this
	// Color color = isImmutable() ? Color.LIGHT_GRAY : lineColor;
	Color color = lineColor;
	draw(g2, color, gridSize);
    }

    public void draw(Graphics2D g2, Color color, int gridSize) {
	if (!allParametersDefined)
	    return;

	// hack - should keep a shape cached for this
	GeneralPath path = createPath(gridSize);
	g2.setColor(color);
	g2.draw(path);
    }

    /**
     * Set the given point to new coordinates for this shape.
     * 
     * This updates the expressions underlying this shape.
     * 
     * <p>
     * <b>Improvement:</b>This method is hacky since it chains the call to the
     * indexed version of it by searching through the points array.
     * </p>
     * 
     * @param point
     *            the point to update
     * @param newX
     *            its new x-ordinate
     * @param newY
     *            its new y-ordinate
     * 
     */
    public void setPoint(NotifyingPoint<DoubleValue> point, double newX,
	    double newY) {
	int index = points.indexOf(point);
	if (index == -1)
	    return;

	setPoint(index, newX, newY);
    }

    /**
     * Set the point with the given index to a new position.
     * 
     * This updates the expressions underlying this shape.
     * 
     * @param index
     *            the index of the point to update
     * @param newX
     *            its new x-ordinate
     * @param newY
     *            its new y-ordinate
     * 
     */
    public void setPoint(int index, double newX, double newY) {
	if (!allParametersDefined)
	    return;

	if (newX == points.get(index).getX().doubleValue()
		&& newY == points.get(index).getY().doubleValue())
	    return;

	settingPoint = true;

	double origX = getLocation().getX().doubleValue();
	double origY = getLocation().getY().doubleValue();

	// System.out.println("Original location: " + origX + ", " + origY);

	// System.out.println("Before segment processing");
	// printPoints();

	// System.out.println("Setting point at index " + index + " to " + newX
	// + ", " + newY);
	boolean precedingOk = processPrecedingSegment(index, newX, newY);
	boolean followingOk = processFollowingSegment(index, newX, newY);

	// do nothing if update did nothing
	if (!precedingOk && !followingOk) {
	    settingPoint = false;
	    // hack in case the location has changed in the meantime
	    calculatePoints();
	    fireObjectUpdated();
	    return;
	}

	// todo: change newX and newY depending on which expressions were set
	// successfully.

	// this is the same as calculatePoints generalised to work from a
	// particular offset
	processPreviousPathSegments(index, newX, newY);

	// System.out.println("In between segment processing (" + index + "):");
	// printPoints();

	processSubsequentPathSegments(index, newX, newY);

	// System.out.println("After segment processing (" + index + "):");
	// printPoints();

	points.get(index).setLocation(new DoubleValue(newX),
		new DoubleValue(newY));

	double newLocationX = getLocation().getX().doubleValue();
	double newLocationY = getLocation().getY().doubleValue();

	// System.out.println("New location x: " + newLocationX);
	// System.out.println("New location y: " + newLocationY);

	// HACK - only make it the same as the first point
	if (newLocationX == origX && newLocationY == origY)
	    setLocation(points.get(0).getX().doubleValue(), points.get(0)
		    .getY().doubleValue());

	// System.out.println("Location NOW x: " + getLocation().getX());
	// System.out.println("Location NOW y: " + getLocation().getY());

	settingPoint = false;
	// hack in case the location has changed in the meantime
	// e.g. through a tie
	calculatePoints();
	fireObjectUpdated();
    }

    // find out change in expression
    private boolean processPrecedingSegment(int index, double newX, double newY) {
	// hack to process the first point - automatically closes the shape
	if (index == 0)
	    index = pathSegmentList.size();

	PathSegment segment = pathSegmentList.get(index - 1);
	NotifyingPoint<DoubleValue> currentPoint = points.get(index);
	double xDiff = newX - currentPoint.getX().doubleValue();
	double yDiff = newY - currentPoint.getY().doubleValue();

	// System.out.println("xDiff, yDiff: " + xDiff + ", " + yDiff);
	int segmentAngle = segment.getAngle();
	// System.out.println("Segment angle: " + segmentAngle);

	double alpha = Math.toDegrees(Math.atan2(yDiff, xDiff));
	double angleDifference = (double) segmentAngle - alpha;
	double changeLength = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
	double change =
		changeLength * Math.cos(Math.toRadians(angleDifference));
	double currentExpressionValue = segment.getExpression().getValue();
	change = Math.round(change);
	// System.out.println("Preceding segment change (" + index + "): " +
	// change);
	currentExpressionValue += change;
	boolean expressionSet =
		segment.getExpression().setValue(currentExpressionValue);
	return expressionSet;
    }

    // find out change in expression
    private boolean processFollowingSegment(int index, double newX, double newY) {
	// should do this depending on isClosed or not
	if (index >= pathSegmentList.size())
	    index = 0;

	PathSegment segment = pathSegmentList.get(index);
	NotifyingPoint<DoubleValue> currentPoint = points.get(index);
	double xDiff = newX - currentPoint.getX().doubleValue();
	double yDiff = newY - currentPoint.getY().doubleValue();

	double alpha = Math.toDegrees(Math.atan2(yDiff, xDiff));
	int segmentAngle = segment.getAngle();
	double angleDifference = (double) segmentAngle - alpha;
	double changeLength = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
	double change =
		changeLength * Math.cos(Math.toRadians(angleDifference));
	double currentExpressionValue = segment.getExpression().getValue();
	change = Math.round(change);
	// System.out.println("Following segment change (" + index + "): " +
	// change);
	currentExpressionValue -= change;
	boolean expressionSet =
		segment.getExpression().setValue(currentExpressionValue);
	return expressionSet;
    }

    /**
     * 
     * 
     * @param index
     * @param newX
     * @param newY
     * 
     */
    private void processPreviousPathSegments(int index, double newX, double newY) {
	// do backward from current point to beginning
	// System.out.println("Processing previous points from index " + index);
	// System.out.println("New position: " + newX + ", " + newY);

	Point2D.Double point = new Point2D.Double(newX, newY);
	for (int i = index - 1; i >= 0; i--) {
	    PathSegment pathSegment = pathSegmentList.get(i);
	    int angle = pathSegment.getAngle();
	    Expression expression = pathSegment.getExpression();

	    double value = expression.getValue();
	    double x =
		    Math.round(point.x - value
			    * Math.cos(Math.toRadians(angle)));
	    double y =
		    Math.round(point.y - value
			    * Math.sin(Math.toRadians(angle)));

	    // System.out.println("Previous point " + i + ": " + x + ", " + y);

	    point = new Point2D.Double(x, y);
	    points.get(i).setLocation(new DoubleValue(x), new DoubleValue(y));

	    // System.out.println("Point " + i + " is now " + points.get(i));
	}

    }

    /**
     * 
     * 
     * @param index
     * @param newX
     * @param newY
     * 
     */
    private void processSubsequentPathSegments(int index, double newX,
	    double newY) {
	Point2D.Double point = new Point2D.Double(newX, newY);
	// System.out.println("Processing previous points from index " + index);
	// System.out.println("New position: " + newX + ", " + newY);

	// do forward from current point to end
	for (int i = index; i < pathSegmentList.size(); i++) {
	    PathSegment pathSegment = pathSegmentList.get(i);
	    int angle = pathSegment.getAngle();
	    Expression expression = pathSegment.getExpression();

	    double value = expression.getValue();
	    double x =
		    Math.round(point.x + value
			    * Math.cos(Math.toRadians(angle)));
	    double y =
		    Math.round(point.y + value
			    * Math.sin(Math.toRadians(angle)));
	    point = new Point2D.Double(x, y);

	    // System.out.println("Subsequent point " + (i + 1) + ": " + x + ",
	    // "
	    // + y);

	    points.get(i + 1).setLocation(new DoubleValue(x),
		    new DoubleValue(y));
	}
    }

}

