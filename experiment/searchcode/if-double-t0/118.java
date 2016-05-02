package org.arclib.math;

public class Collision
{

	// / whether 2 boxes collide with each other
	public static boolean boxBoxCollision(Point pos1, Size size1, Point pos2,
			Size size2)
	{
		// box's within x range
		if ((pos1.x > pos2.x || pos1.x + size1.w > pos2.x)
				&& (pos1.x < pos2.x + size2.w))
			// box's within y range
			if ((pos1.y > pos2.y || pos1.y + size1.h > pos2.y)
					&& (pos1.y < pos2.y + size2.h))
				return true;

		return false;
	}

	// / whether a box and circle collide with each other
	public static boolean boxCircleCollision(Point boxPos, Size boxSize,
			Point circlePos, double radius)
	{
		// first we do a simple test to see if center of circle is inside of box
		if (boxXYCollision(circlePos, boxPos, boxSize))
			return true;

		// this next algoritm will determine the distance from given edge
		// of rectangle to the circle.
		// draw line from radframe (x,y) to gx, gy for a better
		// understanding of what this algorithm does

		// destination x and y values this algorithms calculates
		double gx = 0, gy = 0;

		// box center point
		double x2 = boxPos.x + boxSize.w / 2;
		double y2 = boxPos.y + boxSize.h / 2;

		// box bottom right corner
		double nw2 = boxPos.x + boxSize.w;
		double nh2 = boxPos.y + boxSize.h;

		// box frame is above radius frame
		if (y2 <= circlePos.y)
		{
			// middle
			if (circlePos.x >= boxPos.x && circlePos.x <= nw2)
			{
				gx = circlePos.x;
				gy = nh2;
				// debug writefln("top middle");
			}
			// left
			if (circlePos.x > nw2)
			{
				gx = nw2;
				gy = nh2;
				// debug writefln("top left");
			}
			// right
			if (circlePos.x < boxPos.x)
			{
				gx = boxPos.x;
				gy = nh2;
				// debug writefln("top right");
			}
		}
		// box frame below radius frame
		else if (y2 > circlePos.y)
		{
			// middle
			if (circlePos.x >= boxPos.x && circlePos.x <= nw2)
			{
				gx = circlePos.x;
				gy = boxPos.y;
				// debug writefln("bottom middle");
			}
			// left
			if (circlePos.x > nw2)
			{
				gx = nw2;
				gy = boxPos.y;
				// debug writefln("bottom left");
			}
			// right
			if (circlePos.x < boxPos.x)
			{
				gx = boxPos.x;
				gy = boxPos.y;
				// debug writefln("bottom right");
			}
		}

		// boxframe is on right middle of radframe
		if (circlePos.x < x2 && nh2 > circlePos.y && boxPos.y < circlePos.y)
		{
			gx = boxPos.x;
			gy = circlePos.y;
			// debug writefln("right middle");
		}
		// boxframe is on left middle of radframe
		else if (circlePos.x > x2 && nh2 > circlePos.y
				&& boxPos.y < circlePos.y)
		{
			gx = nw2;
			gy = circlePos.y;
			// debug writefln("left middle");
		}

		if (Common.distance(circlePos.x, circlePos.y, gx, gy) <= radius)
			return true;

		return false;
	}

	// / determine whether x and y are within given box
	public static boolean boxXYCollision(Point point, Point boxPos, Size boxSize)
	{
		if (point.x > boxPos.x && point.x < boxPos.x + boxSize.w)
			if (point.y > boxPos.y && point.y < boxPos.y + boxSize.h)
				return true;

		return false;
	}

	// CIRCLES ///////////////////////////////////////////////////////////

	// / determines whether or not 2 circles have collided
	public static boolean circleCircleCollision(Point c1, double rad1,
			Point c2, double rad2)
	{
		if (Common.distance(c1.x, c1.y, c2.x, c2.y) <= (rad1 + rad2))
			return true;

		return false;
	}

	// / determine whether point x, y is within circle
	public static boolean circleXYCollision(Point pos, Point c, double rad)
	{
		if (Common.distance(c.x, c.y, pos.x, pos.y) <= rad)
			return true;

		return false;
	}

	// LINE ///////////////////////////////////////////////////////////

	/**
	 * // Line Line Collision /////////////////////////////////
	 * http://www.geometryalgorithms
	 * .com/Archive/algorithm_0104/algorithm_0104B.htm#intersect2D_SegSeg()
	 * intersect2D_2Segments(): the intersection of 2 finite 2D segments Input:
	 * two finite segments S1 and S2 Output: *I0 = intersect point (when it
	 * exists) Return: 0=disjoint (no intersect) 1=intersect in unique point I0
	 * 2=overlap in segment from I0 to I1
	 */

	public boolean lineLineCollision(Point S1P0, Point S1P1, Point S2P0,
			Point S2P1, Point I0)
	{
		Point u = S1P1.sub(S1P0);
		Point v = S2P1.sub(S2P0);
		Point w = S1P0.sub(S2P0);
		double D = u.cross(v);
		final double tolerance = 0.0000000001f;

		// test if they are parallel (includes either being a point)
		if (Math.abs(D) < tolerance)
		{ // S1 and S2 are parallel
			if (u.cross(w) != 0 || v.cross(w) != 0)
			{
				return false; // they are NOT collinear
			}
			// they are co-linear or degenerate
			// check if they are degenerate points
			double du = u.dot(u);
			double dv = v.dot(v);

			if (du == 0 && dv == 0)
			{ // both segments are points
				if (S1P0 != S2P0) // they are distinct points
					return false;
				I0 = S1P0; // they are the same point
				return true;
			}
			if (du == 0)
			{ // S1 is a single point
				if (inSegment(S1P0, S2P0, S2P1) == false) // but is not in S2
					return false;
				I0 = S1P0;
				return true;
			}
			if (dv == 0)
			{ // S2 a single point
				if (inSegment(S2P0, S1P0, S1P1) == false) // but is not in S1
					return false;
				I0 = S2P0;
				return true;
			}

			// they are collinear segments - get overlap (or not)
			double t0, t1; // endpoints of S1 in eqn for S2
			Point w2 = S1P1.sub(S2P0);
			if (v.x != 0)
			{
				t0 = w.x / v.x;
				t1 = w2.x / v.x;
			} else
			{
				t0 = w.y / v.y;
				t1 = w2.y / v.y;
			}
			if (t0 > t1)
			{ // must have t0 smaller than t1
				double t = t0;
				t0 = t1;
				t1 = t; // swap if not
			}
			if (t0 > 1 || t1 < 0)
			{
				return false; // NO overlap
			}
			t0 = t0 < 0 ? 0 : t0; // clip to min 0
			t1 = t1 > 1 ? 1 : t1; // clip to max 1
			if (t0 == t1)
			{ // intersect is a point
				Point tmp = v.mult(t0);
				I0 = S2P0.add(tmp);
				return true;
			}

			// they overlap in a valid subsegment
			Point tmp1 = v.mult(t0);
			// Point tmp2 = v.mult(t1);

			I0 = S2P0.add(tmp1);
			// I1 = S2P0 + tmp2;
			return true;
		}

		// the segments are skew and may intersect in a point
		// get the intersect parameter for S1
		double sI = v.cross(w) / D;
		if (sI < 0 || sI > 1) // no intersect with S1
			return false;

		// get the intersect parameter for S2
		double tI = u.cross(w) / D;
		if (tI < 0 || tI > 1) // no intersect with S2
			return false;

		Point tmp = u.mult(sI);
		I0 = S1P0.add(tmp); // compute S1 intersect point
		return true;
	}

	// ===================================================================

	/**
	 * inSegment(): determine if a point is inside a segment Input: a point P,
	 * and a collinear segment S Return: true = P is inside S false = P is not
	 * inside S
	 */
	public static boolean inSegment(Point P, Point S0, Point S1)
	{
		if (S0.x != S1.x)
		{ // S is not vertical
			if (S0.x <= P.x && P.x <= S1.x)
				return true;
			if (S0.x >= P.x && P.x >= S1.x)
				return true;
		} else
		{ // S is vertical, so test y coordinate
			if (S0.y <= P.y && P.y <= S1.y)
				return true;
			if (S0.y >= P.y && P.y >= S1.y)
				return true;
		}
		return false;
	}

	// POINT //////////////////////////////////////////////////////////

	// / returns true if this point is inside of this polygon
	public static boolean polygonXYCollision(Point p, Point[] points)
	{
		boolean c = false;
		int i, j;

		for (i = 0, j = points.length - 1; i < points.length; j = i++)
		{
			if ((((points[i].y <= p.y) && (p.y < points[j].y)) || ((points[j].y <= p.y) && (p.y < points[i].y)))
					&& (p.x < (points[j].x - points[i].x) * (p.y - points[i].y)
							/ (points[j].y - points[i].y) + points[i].x))
			{
				c = !c;
			}
		}

		return c;
	}

}

