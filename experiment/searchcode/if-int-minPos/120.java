/* -*- tab-width: 4 -*-
 *
 * Electric(tm) VLSI Design System
 *
 * File: MinAreaPolygon.java
 * Written by: Daniel Schmidt
 * 
 * This code has been developed at the Karlsruhe Institute of Technology (KIT), Germany, 
 * as part of the course "Multicore Programming in Practice: Tools, Models, and Languages".
 * Contact instructor: Dr. Victor Pankratius (pankratius@ipd.uka.de)
 *
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 *
 * Electric(tm) is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Electric(tm) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Electric(tm); see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, Mass 02111-1307, USA.
 */
package com.sun.electric.plugins.minarea.mk01.datatypes;

import java.util.ArrayList;
import java.util.Collection;

import com.sun.electric.api.minarea.geometry.Point;

/**
 * MinAreaPolygon stores all relevant informations of a particular polygon
 * that are needed in phase 3 of the algorithm.
 * 
 * @author Daniel Schmidt
 *
 */
public class MinAreaPolygon {
	
	/* The current area of this polygon */
	private long area;
	/* The current most upper right point of this point */
	private Point upperRight;
	/* Collection of all set bitmap ranges on the most left of this polygon */
	private Collection<Pair<Integer, Integer>> rangesLeft = new ArrayList<Pair<Integer, Integer>>();
	/* Collection of all set bitmap ranges on the most right of this polygon */
	private Collection<Pair<Integer, Integer>> rangesRight = new ArrayList<Pair<Integer, Integer>>();

	/* The most left x index of this polygon */ 
	private Integer minPos = Integer.MAX_VALUE;
	/* The most right x index of this polygon */
	private Integer maxPos = Integer.MIN_VALUE;
	/* Indicating during merge whether this is the left or right polygon */
	private Side side;
	
	/**
	 * Sets the current area of this polygon with the given value.
	 * 
	 * @param area
	 */
	public void setArea(long area) {
		this.area = area;
	}
	
	/**
	 * Adds the given value to the current area of this polygon.
	 * 
	 * @param area
	 */
	public void addArea(long area) {
		this.area += area;
	}
	
	/**
	 * Gets the current area of this polygon.
	 * 
	 * @return the current area of this polygon.
	 */
	public long getArea() {
		return area;
	}
	
	/**
	 * Sets the most upper right point of this polygon. Therefore it checks
	 * whether the current or the given point is the most upper right one.
	 * @param upperRight
	 */
	public void setUpperRight(Point upperRight) {
		this.upperRight = this.upperRight != null ? 
				determineUpperRight(this.upperRight, upperRight) : upperRight;
	}

	/**
	 * Gets the most right upper point of this polygon.
	 * 
	 * @return the most right upper point of this polygon.
	 */
	public Point getUpperRight() {
		return upperRight;
	}
	
	/**
	 * Adds all given ranges to this ranges on the most left.
	 * 
	 * @param rangesLeft
	 */
	public void addRangesLeft(Collection<Pair<Integer, Integer>> rangesLeft) {
		this.rangesLeft.addAll(rangesLeft);
	}
	
	/**
	 * Adds the range specified by the start and end indexes to this
	 * ranges on the most left if the given pos is really the most left one.
	 * If pos represents an index further left as the position of the current
	 * ranges, the ranges are cleared and set by the specified range. 
	 * 
	 * @param pos
	 * @param start
	 * @param end
	 */
	public void addRangeLeft(int pos, int start, int end) {
		if(pos < 0)
			return;
		if(pos == minPos) {
			rangesLeft.add(new Pair<Integer, Integer>(start, end));
		} else if(pos >= 0 && pos < minPos) {
			rangesLeft.clear();
			rangesLeft.add(new Pair<Integer, Integer>(start, end));
			minPos = pos;
		}
	}
	
	/**
	 * Gets all ranges on the most left of this polygon.
	 * 
	 * @return all ranges on the most left of this polygon.
	 */
	public Collection<Pair<Integer, Integer>> getRangesLeft() {
		return rangesLeft;
	}
	
	/**
	 * Adds all given ranges to this ranges on the most right.
	 * 
	 * @param rangesRight
	 */
	public void addRangesRight(Collection<Pair<Integer, Integer>> rangesRight) {
		this.rangesRight.addAll(rangesRight);
	}

	/**
	 * Adds the range specified by the start and end indexes to this
	 * ranges on the most right if the given pos is really the most right one.
	 * If pos represents an index further right as the position of the current
	 * ranges, the ranges are cleared and set by the specified range. 
	 * 
	 * @param pos
	 * @param start
	 * @param end
	 */
	public void addRangeRight(int pos, int start, int end) {
		if(pos < 0)
			return;
		if(pos == maxPos) {
			rangesRight.add(new Pair<Integer, Integer>(start, end));
		} else if(pos >= 0 && pos > maxPos) {
			rangesRight.clear();
			rangesRight.add(new Pair<Integer, Integer>(start, end));
			maxPos = pos;
		}
	}
	
	/**
	 * Gets all ranges on the most right of this polygon.
	 * 
	 * @return all ranges on the most right of this polygon.
	 */
	public Collection<Pair<Integer, Integer>> getRangesRight() {
		return rangesRight;
	}

	/**
	 * Determines whether two polygons in two different stripes of the bitmap
	 * are connected with each other.
	 * 
	 * @param polygon
	 * @return true if there is at least one connection point between 
	 * both polygons, false otherwise.
	 */
	public boolean overlap(MinAreaPolygon polygon) {
		if(this.maxPos + 1 != polygon.minPos)
			return false;
		boolean overlap = false;
		for(Pair<Integer, Integer> innerRangeOnLeft : this.rangesRight) {
			for(Pair<Integer, Integer> innerRangeOnRight : polygon.rangesLeft) {
				if(innerRangeOnLeft.getFirst() <= innerRangeOnRight.getSecond()
						&& innerRangeOnLeft.getSecond() >= innerRangeOnRight.getFirst()) {
					overlap = true;
					break;
				}
			}
			if(overlap)
				break;
		}
		return overlap;
	}
	
	/**
	 * Determines which point is the more upper right one and returns it.
	 * 
	 * @return the more upper right point
	 */
	public static Point determineUpperRight(Point p1, Point p2) {
		Point upperRight;
		if (p1.getX() > p2.getX()) {
			upperRight = p1;
		} else if (p1.getX() == p2.getX()) {
			if (p1.getY() > p2.getY()) {
				upperRight = p1;
			} else {
				upperRight = p2;
			}
		} else {
			upperRight = p2;
		}
		return upperRight;
	}

	/**
	 * Indicates whether this polygon has at least one connection to the
	 * left stripe of the bitmap.
	 * 
	 * @param left
	 * @return true if there is at least one point to the left vertical border,
	 * false otherwise.
	 */
	public boolean hasConnectionToLeft(int left) {
		return minPos == left;
	}

	/**
	 * Indicates whether this polygon has at least one connection to the
	 * right stripe of the bitmap.
	 * 
	 * @param right
	 * @return true if there is at least one point to the right vertical border,
	 * false otherwise.
	 */
	public boolean hasConnectionToRight(int right) {
		return maxPos == right;
	}

	/**
	 * Get the most left position of this polygon
	 * 
	 * @return the most left position of this polygon
	 */
	public Integer getMinPos() {
		return minPos;
	}

	/**
	 * Sets the most left position of this polygon.
	 * 
	 * @param minPos
	 */
	public void setMinPos(Integer minPos) {
		this.minPos = Math.min(this.minPos, minPos);
	}

	/**
	 * Get the most right position of this polygon
	 * 
	 * @return the most right position of this polygon
	 */
	public Integer getMaxPos() {
		return maxPos;
	}

	/**
	 * Sets the most right position of this polygon.
	 * 
	 * @param maxPos
	 */
	public void setMaxPos(Integer maxPos) {
		this.maxPos = Math.max(this.maxPos, maxPos);
	}
	
	/**
	 * Sets the side of this polygon during merge.
	 * 
	 * @param side
	 */
	public void setSide(Side side) {
		this.side = side;
	}
	
	/**
	 * Gets the side of this polygon during merge.
	 * @return the side of this polygon during merge.
	 */
	public Side getSide() {
		return this.side;
	}
}

