/*******************************************************************************
 * Copyright (c) 2005 Koji Hisano <hisano@users.sourceforge.net>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Koji Hisano - initial API and implementation
 *******************************************************************************/
package jp.sf.mapswidgets;

/**
 * Instances of this class represent a single, 2-dimensional coordinate.
 * <p>
 * If a Point represents a latitude/longitude, then x is the longitude and y is the latitude, in decimal notation.
 * </p>
 * <p>
 * Application code does <em>not</em> need to explicitly release the
 * resources managed by each instance when those instances are no longer
 * required, and thus no <code>dispose()</code> method is provided.
 * </p>
 * <p>
 * See Google Maps API documentation [<a href="http://www.google.com/apis/maps/documentation/#GPoint_code_">Class Reference &gt; GPoint</a>].
 * </p>
 *
 * @see Bounds
 * @see Size
 */
public final class Point implements Cloneable {
	private double x;
	private double y;

	/**
	 * Construct a new instance of this class given the x and y values.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public Point(double x, double y) {
		setX(x);
		setY(y);
	}

	@Override
	public String toString() {
		return getX() + "," + getY();
	}
	
	@Override
	public Point clone() {
		try {
			return (Point)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.getMessage());
		}
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object tested) {
		if (tested instanceof Point) {
			Point point = (Point)tested;
		    return x == point.x && y == point.y;
		}
		return false;
	}

	/**
	 * Get the x coordinate.
	 * <p>
	 * See Google Maps API documentation [<a href="http://www.google.com/apis/maps/documentation/#GPoint_code_">Class Reference &gt; GPoint &gt; x</a>].
	 * </p>
	 * 
	 * @return the x coordinate
	 */
	public double getX() {
		return x;
	}

	/**
	 * Set the x coordinate.
	 * <p>
	 * See Google Maps API documentation [<a href="http://www.google.com/apis/maps/documentation/#GPoint_code_">Class Reference &gt; GPoint &gt; x</a>].
	 * </p>
	 * 
	 * @param x the x coordinate
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * Get the y coordinate.
	 * <p>
	 * See Google Maps API documentation [<a href="http://www.google.com/apis/maps/documentation/#GPoint_code_">Class Reference &gt; GPoint &gt; y</a>].
	 * </p>
	 * 
	 * @return the y coordinate
	 */
	public double getY() {
		return y;
	}

	/**
	 * Set the y coordinate.
	 * <p>
	 * See Google Maps API documentation [<a href="http://www.google.com/apis/maps/documentation/#GPoint_code_">Class Reference &gt; GPoint &gt; y</a>].
	 * </p>
	 * 
	 * @param y the y coordinate
	 */
	public void setY(double y) {
		this.y = y;
	}

	String getExpression() {
		return "new GLatLng(" + getX() + ", " + getY() + ")";
	}
}

