package org.arclib.math;

/**
 * 
 * @author Clay Smith
 * 
 *         Common math routines
 * 
 */
public final class Common
{
	/**
	 * See if number is in range of target, range is variable, use with caution
	 * 
	 * @param argNum
	 *            Number to test
	 * @param argTarget
	 *            Target to reach
	 * @param argRange
	 *            Proximity that's considered in reach
	 * @return True if in range, False otherwise
	 */
	public static boolean withinRange(double argNum, double argTarget,
			double argRange)
	{
		if (argNum <= (argTarget + argRange)
				&& argNum >= (argTarget - argRange))
			return true;

		return false;
	}

	/**
	 * Calculate the distance between 2 points
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return Distance between 2 points
	 */
	public static double distance(double x1, double y1, double x2, double y2)
	{
		return java.lang.Math.sqrt(java.lang.Math.pow(x2 - x1, 2)
				+ java.lang.Math.pow(y2 - y1, 2));
	}

	/**
	 * Returns the next power of 2
	 * 
	 * @param a
	 *            Find the next power of 2 to this number
	 * @return Next power of 2
	 */
	public static int nextPowerOfTwo(int a)
	{
		int rval = 1;
		while (rval < a)
			rval <<= 1;
		return rval;
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static int randomRange(double a, double b)
	{
		return (int) (a + (java.lang.Math.random() % (b + 1 - a)));
	}

	/**
	 * 
	 * Find the roots of the function
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * 
	 * @return An array of size 2 with the roots found
	 */
	public static double[] findRoots(double a, double b, double c)
	{
		double[] results = new double[2];
		double t1, t0;

		double d = b * b - (4.0f * a * c);

		if (d < 0.0f)
			return results;

		d = (double) java.lang.Math.sqrt(d);

		double one_over_two_a = 1.0f / (2.0f * a);

		t0 = (-b - d) * one_over_two_a;
		t1 = (-b + d) * one_over_two_a;

		if (t1 < t0)
		{
			double t = t0;

			results[0] = t1;
			results[1] = t;
		}

		return results;
	}

	/**
	 * Finds the area of a polygon
	 * 
	 * 
	 */
	public static double area(Point[] contour)
	{
		int n = contour.length;

		double A = 0.0f;

		for (int p = n - 1, q = 0; q < n; p = q++)
		{
			A += contour[p].getX() * contour[q].getY() - contour[q].getX()
					* contour[p].getY();
		}

		return A * 0.5f;
	}

	/**
	 * Max distance of a given point from a given set of points
	 */
	public static double maxDistance(Point given, Point[] set)
	{
		double max = 0;
		double tmp = 0;

		for (Point p : set)
		{
			// measure distance
			tmp = given.distance(p);

			// if greater than current max point, then make this distance the
			// maximum
			if (tmp > max)
				max = tmp;
		}

		return max;
	}

	/**
	 * Returns sign of a double
	 */
	public static double sign(double x)
	{
		return x < 0.0f ? -1.0f : 1.0f;
	}

	/**
	 * Clamp value to high or low end
	 */
	public static double clamp(double a, double low, double high)
	{
		return java.lang.Math.max(low, java.lang.Math.min(a, high));
	}

	/**
	 * Random number in range [-1,1]
	 * 
	 * @return Float between -1 and 1
	 */
	public static double random()
	{
		double r = java.lang.Math.random();

		r = 2.0f * r - 1.0f;
		return r;
	}

	/**
	 * Random number with given range between high and low
	 * 
	 * @param lo
	 *            Low range
	 * @param hi
	 *            High range
	 * @return r Random number between high and low
	 */
	public static double random(double lo, double hi)
	{
		double r = java.lang.Math.random();

		r = (hi - lo) * r + lo;
		return r;
	}

	public static double degreesToRadians(double argDegree)
	{
		return argDegree * DEGREE_TO_RADIAN;
	}

	public static double radiansToDegrees(double argAngle)
	{
		return argAngle * RADIAN_TO_DEGREE;
	}

	public static double restricDegree(double argDegree)
	{
		return argDegree % 360;
	}

	public static double restrictRadian(double argRadian)
	{
		return argRadian % 360;
	}

	public static double TWOPI = java.lang.Math.PI * 2;
	public static double DEGREE_TO_RADIAN = java.lang.Math.PI / 180;
	public static double RADIAN_TO_DEGREE = 180 / java.lang.Math.PI;

}

