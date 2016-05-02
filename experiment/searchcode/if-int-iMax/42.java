package util;

import common.math.Vec2;

/**
 * A Heightmap is basically an array of heights at certain x-z-values, with functions for calculating the height at
 * certain positions.
 *
 * @author jeroen
 */
public class Heightmap
{
	private float[][] heightmap;
	private Vec2 upperLeft;		// Extreme upper left point in world coordinates
	private Vec2 lowerRight;	// Extreme lower right point in world coordinates
	private double lengthX;		// The length of the total height map along the x-axis in world coordinates
	private double lengthZ;		// The length of the total height map along the z-axis in world coordinates
	private double leftX;		// The lowest x-value
	private double rightX;		// The highest x-value
	private double topZ;			// The lowest z-value (look down from positive y, left-handed)
	private double bottomZ;		// The highest z-value (look down from positive y, left-handed)
	private double tileLengthX;	// The length in world coordinates of one tile in x-direction
	private double tileLengthZ;	// The length in world coordinates of one tile in z-direction
	private int numTilesX;		// The number of tiles in x-direction
	private int numTilesZ;		// The number of tiles in z-directions


	/**
	 * Create a new Heightmap
	 *
	 * @param heightMap	Array containing the heights
	 * @param upperLeft	The upper left point in world coordinates
	 * @param lowerRightThe lower right point in world coordinates
	 */
	public Heightmap(float[][] heightMap, Vec2 upperLeft, Vec2 lowerRight)
	{
		this.heightmap = heightMap;
		this.upperLeft = upperLeft;
		this.lowerRight = lowerRight;
		lengthZ = lowerRight.get(1) - upperLeft.get(1);
		lengthX = lowerRight.get(0) - upperLeft.get(0);
		leftX = upperLeft.get(0);
		rightX = lowerRight.get(0);
		topZ = upperLeft.get(1);
		bottomZ = lowerRight.get(1);
		numTilesX = heightMap.length - 1;
		numTilesZ = heightMap[0].length - 1;
		tileLengthX = lengthX / numTilesX;
		tileLengthZ = lengthZ / numTilesZ;
	}


	/**
	 * Get the interpolated height (y-value) at point (x, z)
	 *
	 * @param x	The x-value of the point for which the height is requested
	 * @param z The z-value of the point for which the height is requested
	 *
	 * @return The interpolated height (y-value) at point (x, z)
	 */
	public float getHeight(float x, float z, boolean debug) throws IndexOutOfBoundsException
	{
		/* Find out if this point is withiin the heightmap */
		if(!isWithinHeightmap(x, z)) throw new IndexOutOfBoundsException("Point (" + x + "," + z + ") is outside this heightmap");

		/* Get the relative position within this heightmap */
		double relX = (x - leftX) / lengthX;
		double relZ = (z - topZ) / lengthZ;

		if(debug) System.out.println("At relative positions " + relX + " and " + relZ);

		/* Get the accompanying array upper left indices */
		int i = MathHelper.fastFloor(relX * numTilesX);
		if(relX == 1) i--;	// Right edge (dx will compensate)
		int j = MathHelper.fastFloor(relZ * numTilesZ);
		if(relZ == 1) j--;	// Bottom edge (dz will compensate)

		if(debug) System.out.println("Getting height using indices " + i + " and " + j);

		/* Get the relative position within this tile */
		double dx = relX * numTilesX - i;
		double dz = relZ * numTilesZ - j;

		if(debug) System.out.println("Relative position within this small rectangle: dx = " + dx + ", dz = " + dz);

		/* Get the height at the upper right and lower left point, because these will be required either way */
		float heightUpperRight = heightmap[i + 1][j];
		float heightLowerLeft = heightmap[i][j + 1];

		/* Find out if we're in the upper left or lower right triangle */
		if(dx + dz > 1)
		{
			if(debug) System.out.println("Lower right triangle");
			/* Lower right triangle; get the lower right height */
			float heightLowerRight = heightmap[i + 1][j + 1];

			/* Calculate the height differences along the axes */
			float heightDiffX = heightLowerRight - heightLowerLeft;
			float heightDiffZ = heightUpperRight - heightLowerRight;

			/* Calculate the height */
			return (float)(heightLowerLeft + heightDiffX * dx + heightDiffZ * (1 - dz));
		}
		else
		{
			if(debug) System.out.println("Upper left triangle");
			/* Upper left triangle; get the upper left height */
			float heightUpperLeft = heightmap[i][j];

			/* Calculate the height differences along the axes */
			float heightDiffX = heightUpperRight - heightUpperLeft;
			float heightDiffZ = heightLowerLeft - heightUpperLeft;

			/* Calculate the height */
			return (float)(heightUpperLeft + heightDiffX * dx + heightDiffZ * dz);
		}
	}


	/**
	 * Returns the highest point within a rectangle between the two extreme values.
	 *
	 * @param rectMinX	Minimum x-value of the rectangle to check
	 * @param rectMaxX	Maximum x-value of the rectangle to check
	 * @param rectMinZ	Minimum z-value of the rectangle to check
	 * @param rectMaxZ	Maximum z-value of the rectangle to check
	 * @param debug Print debug information?
	 * @return
	 * @throws IndexOutOfBoundsException
	 */
	public float getHighestPoint(double rectMinX, double rectMaxX, double rectMinZ, double rectMaxZ, boolean debug) throws IndexOutOfBoundsException
	{
		/* Check if this rectangle is completely outside this heightmap and, if so, quit */
		if(!partlyCovers(rectMinX, rectMaxX, rectMinZ, rectMaxZ))
		{
			throw new IndexOutOfBoundsException("The rectangle spun by (" + rectMinX + ", " + rectMinZ + ") and (" +
					rectMaxX + ", " + rectMaxZ + ") falls completely outside this heightmap");
		}

		float maxHeight = Float.NEGATIVE_INFINITY;

		/* The rectangle could still be partly outside the heightmap. Adjust the extreme points if necessary */
		rectMinX = Math.max(rectMinX, leftX);
		rectMaxX = Math.min(rectMaxX, rightX);
		rectMinZ = Math.max(rectMinZ, topZ);
		rectMaxZ = Math.min(rectMaxZ, bottomZ);

		if(debug) System.out.println("Finding highest point between x from " + rectMinX + " to " + rectMaxX + " and z from " + rectMinZ + " to " + rectMaxZ);

		/* Check all points of the heightmap within or on the edge of the rectangle. This could be zero or more points */
		// The minimum and maximum indices in the heightmap array that fall within or on the edge of the rectangle
		int jMin = MathHelper.fastCeil((rectMinZ - topZ) / tileLengthZ);
		int jMax = MathHelper.fastFloor((rectMaxZ - topZ) / tileLengthZ);
		int iMin = MathHelper.fastCeil((rectMinX - leftX) / tileLengthX);
		int iMax = MathHelper.fastFloor((rectMaxX - leftX) / tileLengthX);

		if(debug) System.out.println("This map is " + numTilesX + " by " + numTilesZ + ", spanning from " + upperLeft.toString() +
				" to " + lowerRight.toString() + " and x from " + iMin + " to " + iMax + " and z from " + jMin +
				" to " + jMax + " fall completely within the rectangle");
		for(int j = jMin; j <= jMax ; j++)
		{
			for(int i = iMin; i <= iMax; i++)
			{
				maxHeight = Math.max(maxHeight, heightmap[i][j]);
			}
		}

		if(debug) System.out.println("The maximum height within this easy part of the rectangle is " + maxHeight);

		maxHeight = Math.max(getHeight((float)rectMinX, (float)rectMinZ, debug), maxHeight);
		if(debug) System.out.println("After checking the upper left corner of the rectangle, the maximum height now is " + maxHeight);
		maxHeight = Math.max(getHeight((float)rectMaxX, (float)rectMinZ, debug), maxHeight);
		if(debug) System.out.println("After checking the upper right corner of the rectangle, the maximum height now is " + maxHeight);
		maxHeight = Math.max(getHeight((float)rectMinX, (float)rectMaxZ, debug), maxHeight);
		if(debug) System.out.println("After checking the lower left corner of the rectangle, the maximum height now is " + maxHeight);
		maxHeight = Math.max(getHeight((float)rectMaxX, (float)rectMaxZ, debug), maxHeight);
		if(debug) System.out.println("After checking the lower right corner of the rectangle, the maximum height now is " + maxHeight);


		/* There's a lot of code here in order to optimize for speed a bit but mostly to keep things understandable.
		 * Check the points outside the edges that don't coincide with a heightmap line. Do this by calculating the
		 * height at the crossing between the rectangle edges and the two lines of each triangle of the heightmap that
		 * is crossed. One of these two heights is always the heighest on that part of the line. Taken in the direction
		 * of the line, these heights are called y1 and y2. The heights of the four points surrounding the triangle in
		 * question are called from left top, clockwise, p1, p2 and p3. These calculations will cause out of bounds
		 * exceptions if done on a edge that coincides with the edge of a heightmap. That's what the surrounding checks
		 * are for. */
		// Make sure we don't overflow calculating the borders by moving the max i and j values into the rectangle if they are on the line
		if(rectMaxZ == bottomZ) jMax--;
		if(rectMaxX == rightX) iMax--;
		float y1, y2;
		float p1, p2, p3;
		if(rectMinX != leftX && jMax - jMin >= 1)
		{
			/* Left edge, from top to bottom */
			int iLeft = iMin - 1;
			double dx = (rectMinX - (leftX + tileLengthX * iLeft)) / tileLengthX;
			p1 = heightmap[iLeft][jMin];
			p2 = heightmap[iMin][jMin];
			p3 = heightmap[iLeft][jMin + 1];
			y1 = (float)(p1 + (p2 - p1) * dx);
			y2 = (float)(p3 + (p2 - p3) * dx);
			maxHeight = Math.max(y1, maxHeight);
			maxHeight = Math.max(y2, maxHeight);
			for(int j = jMin + 1; j <= jMax; j++) // yes, <= (diagonal moves into rectangle)
			{
				// This next line is the reason for all the code up there
				p1 = p3;
				p2 = heightmap[iMin][j];
				p3 = heightmap[iLeft][j + 1];
				y1 = (float)(p1 + (p2 - p1) * dx);
				y2 = (float)(p3 + (p2 - p3) * dx);
				maxHeight = Math.max(y1, maxHeight);
				maxHeight = Math.max(y2, maxHeight);
			}
		}
		if(debug) System.out.println("After checking the left edge of the rectangle, the maximum height now is " + maxHeight);
		if(rectMaxX != rightX && jMax - jMin >= 1)
		{
			/* Right edge, from top to bottom */
			int iRight = iMax + 1;
			double dx = (rectMaxX - (leftX + tileLengthX * iMax)) / tileLengthX;
			p1 = heightmap[iMax][jMin];
			p2 = heightmap[iRight][jMin];
			p3 = heightmap[iMax][jMin + 1];
			y1 = (float)(p1 + (p2 - p1) * dx);
			y2 = (float)(p3 + (p2 - p3) * dx);
			maxHeight = Math.max(y1, maxHeight);
			maxHeight = Math.max(y2, maxHeight);
			for(int j = jMin + 1; j <= jMax; j++) // yes, <= (diagonal moves into rectangle)
			{
				p1 = p3;
				p2 = heightmap[iRight][j];
				p3 = heightmap[iMax][j + 1];
				y1 = (float)(p1 + (p2 - p1) * dx);
				y2 = (float)(p3 + (p2 - p3) * dx);
				maxHeight = Math.max(y1, maxHeight);
				maxHeight = Math.max(y2, maxHeight);
			}
		}
		if(debug) System.out.println("After checking the right edge of the rectangle, the maximum height now is " + maxHeight);
		if(rectMinZ != topZ && iMax - iMin >= 1)
		{
			/* Top edge, from left to right */
			int jTop = jMin - 1;
			double dz = (rectMinZ - (topZ + tileLengthZ * jTop)) / tileLengthZ;
			p1 = heightmap[iMin][jTop];
			p2 = heightmap[iMin + 1][jTop];
			p3 = heightmap[iMin][jMin];
			y1 = (float)(p1 + (p3 - p1) * dz);
			y2 = (float)(p2 + (p3 - p2) * dz);
			maxHeight = Math.max(y1, maxHeight);
			maxHeight = Math.max(y2, maxHeight);
			for(int i = iMin + 1; i < iMax; i++) // yes, < (diagonal moves out of rectangle)
			{
				p1 = p2;
				p2 = heightmap[i + 1][jTop];
				p3 = heightmap[i][jMin];
				y1 = (float)(p1 + (p3 - p1) * dz);
				y2 = (float)(p2 + (p3 - p2) * dz);
				maxHeight = Math.max(y1, maxHeight);
				maxHeight = Math.max(y2, maxHeight);
			}
		}
		if(debug) System.out.println("After checking the top edge of the rectangle, the maximum height now is " + maxHeight);
		if(rectMaxZ != bottomZ && iMax - iMin >= 1)
		{
			/* Bottom edge, from left to right */
			int jBottom = jMax + 1;
			double dz = (rectMaxZ - (topZ + jMax * tileLengthZ)) / tileLengthZ;
			p1 = heightmap[iMin][jMax];
			p2 = heightmap[iMin + 1][jMax];
			p3 = heightmap[iMin][jBottom];
			y1 = (float)(p1 + (p3 - p1) * dz);
			y2 = (float)(p2 + (p3 - p2) * dz);
			maxHeight = Math.max(y1, maxHeight);
			maxHeight = Math.max(y2, maxHeight);
			for(int i = iMin + 1; i < iMax; i++) // yes, < (diagonal moves out of rectangle)
			{
				p1 = p2;
				p2 = heightmap[i + 1][jMax];
				p3 = heightmap[i][jBottom];
				y1 = (float)(p1 + (p3 - p1) * dz);
				y2 = (float)(p2 + (p3 - p2) * dz);
				maxHeight = Math.max(y1, maxHeight);
				maxHeight = Math.max(y2, maxHeight);
			}
		}
		if(debug) System.out.println("After checking the bottom edge of the rectangle, the maximum height now is " + maxHeight);
		/* Wouldn't it be great if that were all? Sadly there's the four corner points that were ignored before. Every
		 * corner point could potentially be the heighest point, as could the zero, one or two points on the diagonal
		 * between it and the rest of the rectangle if it is in the far triangle. Time to calculate at most ten points!
		 * Don't check them if they coincide with a heightmap point. That's not just to save time; it could also cause
		 * out of bounds exceptions. The heights are again named, for consistency, p2 at the upper right end of the
		 * diagonal and p3 at the lower left end */
		if(!(rectMinX == leftX && rectMinZ == topZ))
		{
			/* Upper left corner */
			int iLeft = rectMinX == leftX ? iMin : iMin - 1;
			int jTop = rectMinZ == topZ ? jMin : jMin - 1;
			int iRight = iLeft + 1;
			int jBottom = jTop + 1;
			double dx = (rectMinX - (leftX + iLeft * tileLengthX)) / tileLengthX;
			double dz = (rectMinZ - (topZ + jTop * tileLengthZ)) / tileLengthZ;
			if(dx + dz < 1)
			{
				/* Upper left triangle, means we must calculate two crossings, but not for corners which are between
				 * this corner and the diagonal */
				double diagonalZ = topZ + jBottom * tileLengthZ - dx * tileLengthZ;
				double diagonalX = leftX + iRight * tileLengthX - tileLengthX * dz;
				p2 = heightmap[iRight][jTop];
				p3 = heightmap[iLeft][jBottom];
				if(rectMaxX > diagonalX)
				{
					y2 = (float)(p2 + (p3 - p2) * dz);
					maxHeight = Math.max(y2, maxHeight);
				}
				if(rectMaxZ > diagonalZ)
				{
					y1 = (float)(p3 + (p2 - p3) * dx);
					maxHeight = Math.max(y1, maxHeight);
				}
			}
			// Corner is in lower right triangle, no further calculations needed
		}
		if(debug) System.out.println("After checking the little lines connecting the upper left corner to the top and left edge of the rectangle, the maximum height now is " + maxHeight);
		if(!(rectMaxX == rightX && rectMinZ == topZ))
		{
			/* Upper right corner */
			int iRight = rectMaxX == rightX ? iMax : iMax + 1;
			int jTop = rectMinZ == topZ ? jMin : jMin - 1;
			int iLeft = iRight - 1;
			int jBottom = jTop + 1;
			double dx = (rectMaxX - (leftX + iLeft * tileLengthX)) / tileLengthX;
			double dz = (rectMinZ - (topZ + jTop * tileLengthZ)) / tileLengthZ;
			p2 = heightmap[iRight][jTop];
			p3 = heightmap[iLeft][jBottom];
			if(dx + dz < 1)
			{
				/* Upper left triangle, means we must calculate the crossing below, unless the lower right corner is
				 * between this corner and the diagonal */
				double diagonalZ = topZ + jBottom * tileLengthZ - dx * tileLengthZ;
				if(rectMaxZ > diagonalZ)
				{
					y1 = (float)(p3 + (p2 - p3) * dx);
					maxHeight = Math.max(y1, maxHeight);
				}
			}
			else
			{
				/* Lower right triangle, means we must calculate the crossing to the left, unless the upper left corner
				 * is between this corner and the diagonal */
				double diagonalX = leftX + iRight * tileLengthX - tileLengthX * dz;
				if(rectMinX < diagonalX)
				{
					y1 = (float)(p2 + (p3 - p2) * dz);
					maxHeight = Math.max(y1, maxHeight);
				}
			}
		}
		if(debug) System.out.println("After checking the little lines connecting the upper right corner to the top and right edge of the rectangle, the maximum height now is " + maxHeight);
		if(!(rectMinX == leftX && rectMaxZ == bottomZ))
		{
			/* Lower left corner */
			int iLeft = rectMinX == leftX ? iMin : iMin - 1;
			int jBottom = rectMaxZ == bottomZ ? jMax : jMax + 1;
			int iRight = iLeft + 1;
			int jTop = jBottom - 1;
			double dx = (rectMinX - (leftX + iLeft * tileLengthX)) / tileLengthX;
			double dz = (rectMaxZ - (topZ + jTop * tileLengthZ)) / tileLengthZ;
			p2 = heightmap[iRight][jTop];
			p3 = heightmap[iLeft][jBottom];
			if(dx + dz < 1)
			{
				/* Upper left triangle, means we must calculate the crossing to the right, unless the lower right corner
				 * is between the lower left corner and that diagonal */
				double diagonalX = leftX + iRight * tileLengthX - tileLengthX * dz;
				if(rectMaxX > diagonalX)
				{
					y1 = (float)(p2 + (p3 - p2) * dz);
					maxHeight = Math.max(y1, maxHeight);
				}
			}
			else
			{
				/* Lower right triangle, means we must calculate the crossing above, unless the upper left corner is
				 * between the lower left corner and that diagonal */
				double diagonalZ = topZ + jBottom * tileLengthZ - dx * tileLengthZ;
				if(rectMinZ < diagonalZ)
				{
					y1 = (float)(p3 + (p2 - p3) * dx);
					maxHeight = Math.max(y1, maxHeight);
				}
			}
		}
		if(debug) System.out.println("After checking the little lines connecting the lower left corner to the bottom and left edge of the rectangle, the maximum height now is " + maxHeight);
		if(!(rectMaxX == rightX && rectMaxZ == bottomZ))
		{
			/* Lower right corner */
			int iRight = rectMaxX == rightX ? iMax : iMax + 1;
			int jBottom = rectMaxZ == bottomZ ? jMax : jMax + 1;
			int iLeft = iRight - 1;
			int jTop = jBottom - 1;
			double dx = (rectMaxX - (leftX + iLeft * tileLengthX)) / tileLengthX;
			double dz = (rectMaxZ - (topZ + jTop * tileLengthZ)) / tileLengthZ;
			if(dx + dz > 1)
			{
				/* Lower right triangle, means we must calculate two crossings, but only if the diagonal is actually
				 * crossed between here and the other corners */
				double diagonalZ = topZ + jBottom * tileLengthZ - dx * tileLengthZ;
				if(debug) System.out.println("Diagonal Z: " + diagonalZ);
				if(debug) System.out.println("rectMinZ: " + rectMinZ);
				double diagonalX = leftX + iRight * tileLengthX - tileLengthX * dz;
				p2 = heightmap[iRight][jTop];
				p3 = heightmap[iLeft][jBottom];
				if(rectMinX < diagonalX)
				{
					if(debug) System.out.println("rectMinX < diagonalX");
					y2 = (float)(p2 + (p3 - p2) * dz);
					maxHeight = Math.max(y2, maxHeight);
					if(debug) System.out.println("maxHeight is now " + maxHeight);
				}
				if(rectMinZ < diagonalZ)
				{
					if(debug) System.out.println("rectMinZ < diagonalZ");
					y1 = (float)(p3 + (p2 - p3) * dx);
					maxHeight = Math.max(y1, maxHeight);
					if(debug) System.out.println("maxHeight is now " + maxHeight);
				}
			}
			// Corner is in upper left triangle; no further calculations needed
		}
		if(debug) System.out.println("After checking the little lines connecting the lower right corner to the bottom and right edge of the rectangle, the maximum height now is " + maxHeight);

		return maxHeight;
	}


	/**
	 * Check if point (x, z) is covered by this heightmap
	 *
	 * @param x	The x-value of the point for which to check
	 * @param z	The z-value of the point for which to check
	 *
	 * @return True if the point falls within or on the bounds of this heightmap, false otherwise
	 */
	public boolean isWithinHeightmap(float x, float z)
	{
		/* Use AND for quicker check */
		if(x >= leftX && x <= rightX && z >= topZ && z <= bottomZ) return true;
		else return false;
	}


	/**
	 * Checks if the rectangle spun along the x- and z-axes between (rectMinX, rectMinZ) and (rectMaxX, rectMaxZ) is at
	 * least partly above this heightmap.
	 *
	 * @param rectMinX	Left x-value
	 * @param rectMaxX	Right x-value
	 * @param rectMinZ	Top z-value
	 * @param rectMaxZ	Bottom z-value
	 *
	 * @return true if so, false if not
	 */
	public boolean partlyCovers(double rectMinX, double rectMaxX, double rectMinZ, double rectMaxZ)
	{
		if(rectMaxX < leftX || rectMinX > rightX || rectMaxZ < topZ || rectMinZ > bottomZ) return false;
		else return true;
	}


	/**
	 * Checks if the rectangle spun along the x- and z-axes between (rectMinX, rectMinZ) and (rectMaxX, rectMaxZ) is
	 * completely above this heightmap.
	 *
	 * @param rectMinX	Left x-value
	 * @param rectMaxX	Right x-value
	 * @param rectMinZ	Top z-value
	 * @param rectMaxZ	Bottom z-value
	 *
	 * @return true if so, false if not
	 */
	public boolean completelyCovers(double rectMinX, double rectMaxX, double rectMinZ, double rectMaxZ)
	{
		if(rectMaxX <= rightX && rectMinX >= leftX && rectMaxZ <= bottomZ && rectMinZ >= topZ) return true;
		else return false;
	}


	/**
	 * @return The array containing the heigts
	 */
	public float[][] getData()
	{
		return heightmap;
	}


	/**
	 * @return The upper left point of this heightmap
	 */
	public Vec2 getUpperLeft()
	{
		return upperLeft.clone();
	}


	/**
	 * @return The lower right point of this heightmap
	 */
	public Vec2 getLowerRight()
	{
		return lowerRight.clone();
	}
}

