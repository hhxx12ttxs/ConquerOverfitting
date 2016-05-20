package com.wifislam.sample;

import java.util.ArrayList;
import java.util.List;

import com.wifislam.sample.data.Coordinates;
import com.wifislam.sample.data.InputData;
import com.wifislam.sample.data.Rectangle2D;

/**
 * This class contains methods in order to generate the subdivision of the (x,y)
 * space in rectangles, count the point that fall into each rectangle, find the
 * rectangle that contains the biggest amount of points
 * 
 * @author giovannisoldi
 * 
 */
public class BestLocationEstimator {

	/**
	 * This method is used to find the rectangle that contains the biggest
	 * amount of points.
	 * 
	 * @param rectangles
	 *            the list of the generated rectangles
	 */
	public void findIntervalWithMorePoints(List<Rectangle2D> rectangles) {
		int maximum = 0;
		for (Rectangle2D rectangle : rectangles) {
			if (rectangle.countPointsInside > maximum) {
				maximum = rectangle.countPointsInside;
				rectangle.calculateMeanPoint();
				Rectangle2D.setIntervalWithBiggerNumberOfPoints(rectangle);
			}
		}
	}

	/**
	 * For each rectangle count how many points fall in it.
	 * 
	 * @param data
	 *            the parsed input data
	 * @param rectangles
	 *            the list of the generated rectangles
	 */
	public void countPoints(InputData data, List<Rectangle2D> rectangles) {
		for (Coordinates point : data.getCoordinates()) {
			for (Rectangle2D rectangle : rectangles) {
				if (rectangle.isPointInTheInterval(point)) {
					rectangle.countPointsInside += 1;
					rectangle.sumX += point.getX();
					rectangle.sumY += point.getY();
					continue;
				}
			}
		}
	}

	/**
	 * This methods generates the subdivision of the (x,y) space in rectangles.
	 * 
	 * @param data
	 *            the parsed input data
	 * @param nX
	 *            the number of intervals that one wants to use to divide the
	 *            x-axis
	 * @param nY
	 *            the number of intervals that one wants to use to divide the
	 *            y-axis
	 * @return the list of the generated rectangles
	 */
	public List<Rectangle2D> generateRectangles(InputData data, int nX, int nY) {
		List<Rectangle2D> rectangles = new ArrayList<Rectangle2D>();
		List<Double> Xdata = new ArrayList<Double>(data.getCoordinates().size());
		List<Double> Ydata = new ArrayList<Double>(data.getCoordinates().size());

		for (Coordinates point : data.getCoordinates()) {
			Xdata.add(point.getX());
			Ydata.add(point.getY());
		}

		double Xminimum = findMinimum(Xdata);
		double Xmaximum = findMaximum(Xdata);
		double Yminimum = findMinimum(Ydata);
		double Ymaximum = findMaximum(Ydata);
		double stepX = Math.abs((Xmaximum - Xminimum)) / nX;
		double stepY = Math.abs((Ymaximum - Yminimum)) / nY;
		double[] intraPointsX = new double[nX + 1];
		double[] intraPointsY = new double[nY + 1];
		intraPointsX[0] = Xminimum;
		intraPointsX[nX] = Xmaximum;
		intraPointsY[0] = Yminimum;
		intraPointsY[nY] = Ymaximum;
		for (int i = 1; i < intraPointsX.length - 1; i++) {
			intraPointsX[i] = intraPointsX[i - 1] + stepX;
		}
		for (int j = 1; j < intraPointsY.length - 1; j++) {
			intraPointsY[j] = intraPointsY[j - 1] + stepY;
		}
		for (int i = 0; i < intraPointsX.length - 1; i++) {
			for (int j = 0; j < intraPointsY.length - 1; j++) {
				Rectangle2D rectangle = new Rectangle2D(intraPointsX[i],
						intraPointsX[i + 1], intraPointsY[j],
						intraPointsY[j + 1]);
				rectangles.add(rectangle);
			}
		}
		return rectangles;
	}

	/**
	 * Find the maximum number in a list of double numbers
	 * 
	 * @param list
	 *            the list of double numbers
	 * @return the biggest double number in the list
	 */
	private double findMaximum(List<Double> list) {
		double maximum = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) > maximum)
				maximum = list.get(i);
		}
		return maximum;
	}

	/**
	 * Find the minimum number in a list of double numbers
	 * 
	 * @param list
	 *            the list of double numbers
	 * @return the smallest double number in the list
	 */

	private double findMinimum(List<Double> list) {
		double minimum = Double.POSITIVE_INFINITY;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) < minimum)
				minimum = list.get(i);
		}
		return minimum;
	}
}

