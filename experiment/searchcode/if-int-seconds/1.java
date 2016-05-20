/* Chris Cummins - 10 Mar 2012
 *
 * This file is part of Kummins Library.
 *
 * Kummins Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Kummins Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Kummins Library.  If not, see <http://www.gnu.org/licenses/>.
 */

package jcummins.maths;

/**
 * @author Chris Cummins
 * 
 */
public class MathsTools
{
	/**
	 * 
	 * @param v
	 * @return
	 */
	public static double average (int[] v)
	{
		int total = 0;
		for (int i = 0; i < v.length; i++)
			total += v[i];
		return (total / v.length);
	}

	/**
	 * 
	 * @param v
	 * @return
	 */
	public static double average (float[] v)
	{
		int total = 0;
		for (int i = 0; i < v.length; i++)
			total += v[i];
		return (total / v.length);
	}

	/**
	 * 
	 * @param v
	 * @return
	 */
	public static double average (double[] v)
	{
		int total = 0;
		for (int i = 0; i < v.length; i++)
			total += v[i];
		return (total / v.length);
	}

	/**
	 * 
	 * @param x
	 *            Value 1.
	 * @param max
	 *            Value 2.
	 * @return (<code>x</code>/(<code>x</code>*<code>y</code>))*100
	 */
	public static double percent (int x, int max)
	{
		if (max > 0 || x > 0)
		{
			return ((double) x / (x + max)) * 100;
		}
		else
		{
			System.err.println (MathsTools.class.toString ()
					+ ": attempting to divide by zero.");
			return 0.0;
		}
	}

	/**
	 * 
	 * @param x
	 *            Value 1.
	 * @param max
	 *            Value 2.
	 * @return (<code>x</code>/(<code>x</code>*<code>y</code>))*100
	 */
	public static double percent (int x, int max, int decimalPlaces)
	{
		DecimalRounder d = new DecimalRounder (decimalPlaces);
		return d.round (percent (x, max));
	}

	/**
	 * Method overloading that allows for integer values of steps to be
	 * converted into hh:mm:ss.
	 * 
	 * @param seconds
	 *            Value to be displayed in hh:mm:ss format.
	 * @see MathsTools#stepsToTime(double)
	 */
	public static String stepsToTime (int seconds)
	{
		String output = "";
		if ((int) ((seconds / 840) % 24) < 1
				&& (int) ((seconds / 120) % 24) < 1
				&& (int) ((seconds / 2) % 60) < 1)
		{ // Seconds.
			output = (int) ((seconds * 30) % 60) + "s";
		}
		else if ((int) ((seconds / 2880) % 24) < 1
				&& (int) ((seconds / 120) % 24) < 1)
		{ // Minutes.
			output = (int) ((seconds / 2) % 60) + "m "
					+ (int) ((seconds * 30) % 60) + "s";
		}
		else if ((int) ((seconds / 2880) % 24) < 1)
		{ // Hours.
			output = (int) ((seconds / 120) % 24) + "h "
					+ (int) ((seconds / 2) % 60) + "m "
					+ (int) ((seconds * 30) % 60) + "s";
		}
		else if ((int) ((seconds / 2880) % 7) < 2)
		{ // One day.
			output = (int) ((seconds / 2880) % 7) + " day, "
					+ (int) ((seconds / 120) % 24) + "h "
					+ (int) ((seconds / 2) % 60) + "m "
					+ (int) ((seconds * 30) % 60) + "s";
		}
		else
		{ // Multiple days.
			output = (int) ((seconds / 2880) % 7) + " days, "
					+ (int) ((seconds / 120) % 24) + "h "
					+ (int) ((seconds / 2) % 60) + "m "
					+ (int) ((seconds * 30) % 60) + "s";
		}
		return output;
	}

}

