package com.wifislam.sample.data;

/**
 * 
 * 
 * @author Giovanni Soldi
 * 
 */
public class Rectangle2D {

	private final double startPointX;
	private final double endPointX;
	private final double startPointY;
	private final double endPointY;
	public int countPointsInside;
	public double meanX;
	public double meanY;
	public double sumX;
	public double sumY;

	private static Rectangle2D intervalWithBiggerNumberOfPoints;

	/**
	 * Constructor
	 * 
	 * @param startPointX
	 *            the start point of the rectangle on the x-axis
	 * @param endPointX
	 *            the end point of the rectangle on the x-axis
	 * @param startPointY
	 *            the start point of the rectangle on the y-axis
	 * @param endPointY
	 *            the end point of the rectangle on the y-axis
	 */
	public Rectangle2D(double startPointX, double endPointX,
			double startPointY, double endPointY) {
		super();
		this.startPointX = startPointX;
		this.endPointX = endPointX;
		this.startPointY = startPointY;
		this.endPointY = endPointY;
	}

	/**
	 * 
	 * @param point
	 * @return true if the point is inside the rectangle, false if the point is
	 *         outside
	 */
	public boolean isPointInTheInterval(Coordinates point) {
		return (point.getX() <= endPointX && point.getX() >= startPointX
				&& point.getY() <= endPointY && point.getY() >= startPointY);
	}

	/**
	 * 
	 * @return the estimated mean point in the rectangle
	 */
	public Coordinates calculateMeanPoint() {
		meanX = sumX / countPointsInside;
		meanY = sumY / countPointsInside;
		return new Coordinates(meanX, meanY);
	}

	/**
	 * 
	 * @return the interval that contains the biggest number of points
	 */
	public static Rectangle2D getIntervalWithBiggerNumberOfPoints() {
		return intervalWithBiggerNumberOfPoints;
	}

	/**
	 * 
	 * @param intervalWithBiggerNumberOfPoints
	 */
	public static void setIntervalWithBiggerNumberOfPoints(
			Rectangle2D intervalWithBiggerNumberOfPoints) {
		Rectangle2D.intervalWithBiggerNumberOfPoints = intervalWithBiggerNumberOfPoints;
	}

	@Override
	public String toString() {
		return "Interval2D [startPointX=" + startPointX + ", endPointX="
				+ endPointX + ", startPointY=" + startPointY + ", endPointY="
				+ endPointY + ", countPointsInside=" + countPointsInside
				+ ", meanX=" + meanX + ", meanY=" + meanY + ", sumX=" + sumX
				+ ", sumY=" + sumY + "]";
	}

}

