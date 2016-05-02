/*
<<<<<<< HEAD
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sanselan.common;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class RationalNumber extends Number
{
	private static final long serialVersionUID = -1;

	public final int numerator;
	public final int divisor;

	public RationalNumber(int numerator, int divisor)
	{
		this.numerator = numerator;
		this.divisor = divisor;
	}

	public static final RationalNumber factoryMethod(long n, long d)
	{
		// safer than constructor - handles values outside min/max range.
		// also does some simple finding of common denominators.

		if (n > Integer.MAX_VALUE || n < Integer.MIN_VALUE
				|| d > Integer.MAX_VALUE || d < Integer.MIN_VALUE)
		{
			while ((n > Integer.MAX_VALUE || n < Integer.MIN_VALUE
					|| d > Integer.MAX_VALUE || d < Integer.MIN_VALUE)
					&& (Math.abs(n) > 1) && (Math.abs(d) > 1))
			{
				// brutal, inprecise truncation =(
				// use the sign-preserving right shift operator.
				n >>= 1;
				d >>= 1;
			}

			if (d == 0)
				throw new NumberFormatException("Invalid value, numerator: "
						+ n + ", divisor: " + d);
		}

		long gcd = gcd(n, d);
		d = d / gcd;
		n = n / gcd;

		return new RationalNumber((int) n, (int) d);
	}

	/**
	 * Return the greatest common divisor
	 */
	private static long gcd(long a, long b)
	{

		if (b == 0)
			return a;
		else
			return gcd(b, a % b);
	}

	public RationalNumber negate()
	{
		return new RationalNumber(-numerator, divisor);
	}

	public double doubleValue()
	{
		return (double) numerator / (double) divisor;
	}

	public float floatValue()
	{
		return (float) numerator / (float) divisor;
	}

	public int intValue()
	{
		return (int) numerator / (int) divisor;
	}

	public long longValue()
	{
		return (long) numerator / (long) divisor;
	}

	public boolean isValid()
	{
		return divisor != 0;
	}

	private static final NumberFormat nf = DecimalFormat.getInstance();

	public String toString()
	{
		if (divisor == 0)
			return "Invalid rational (" + numerator + "/" + divisor + ")";
		if ((numerator % divisor) == 0)
			return nf.format(numerator / divisor);
		return numerator + "/" + divisor + " ("
				+ nf.format((double) numerator / divisor) + ")";
	}

	public String toDisplayString()
	{
		if ((numerator % divisor) == 0)
			return "" + (numerator / divisor);
		NumberFormat nf = DecimalFormat.getInstance();
		nf.setMaximumFractionDigits(3);
		return nf.format((double) numerator / (double) divisor);
	}
=======
 * Rational.java
 *
 * This class is public domain software - that is, you can do whatever you want
 * with it, and include it software that is licensed under the GNU or the
 * BSD license, or whatever other licence you choose, including proprietary
 * closed source licenses.  Similarly, I release this Java version under the
 * same license, though I do ask that you leave this header in tact.
 *
 * If you make modifications to this code that you think would benefit the
 * wider community, please send me a copy and I'll post it on my site.
 *
 * If you make use of this code, I'd appreciate hearing about it.
 *   drew.noakes@drewnoakes.com
 * Latest version of this software kept at
 *   http://drewnoakes.com/
 *
 * Created on 6 May 2002, 18:06
 * Updated 26 Aug 2002 by Drew
 * - Added toSimpleString() method, which returns a simplified and hopefully more
 *   readable version of the Rational.  i.e. 2/10 -> 1/5, and 10/2 -> 5
 * Modified 29 Oct 2002 (v1.2)
 * - Improved toSimpleString() to factor more complex rational numbers into
 *   a simpler form
 *     i.e.
 *       10/15 -> 2/3
 * - toSimpleString() now accepts a boolean flag, 'allowDecimals' which will
 *   display the rational number in decimal form if it fits within 5 digits
 *     i.e.
 *       3/4 -> 0.75 when allowDecimal == true
 */

package com.drew.lang;

import java.io.Serializable;

/**
 * Immutable class for holding a rational number without loss of precision.  Provides
 * a familiar representation via toString() in form <code>numerator/denominator</code>.
 * <p>
 * @author  Drew Noakes http://drewnoakes.com
 */
public class Rational extends java.lang.Number implements Serializable
{
    /**
     * Holds the numerator.
     */
    private final int numerator;

    /**
     * Holds the denominator.
     */
    private final int denominator;

    private int maxSimplificationCalculations = 1000;

    /**
     * Creates a new instance of Rational.  Rational objects are immutable, so
     * once you've set your numerator and denominator values here, you're stuck
     * with them!
     */
    public Rational(int numerator, int denominator)
    {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    /**
     * Returns the value of the specified number as a <code>double</code>.
     * This may involve rounding.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>double</code>.
     */
    public double doubleValue()
    {
        return (double)numerator / (double)denominator;
    }

    /**
     * Returns the value of the specified number as a <code>float</code>.
     * This may involve rounding.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>float</code>.
     */
    public float floatValue()
    {
        return (float)numerator / (float)denominator;
    }

    /**
     * Returns the value of the specified number as a <code>byte</code>.
     * This may involve rounding or truncation.  This implementation simply
     * casts the result of <code>doubleValue()</code> to <code>byte</code>.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>byte</code>.
     */
    public final byte byteValue()
    {
        return (byte)doubleValue();
    }

    /**
     * Returns the value of the specified number as an <code>int</code>.
     * This may involve rounding or truncation.  This implementation simply
     * casts the result of <code>doubleValue()</code> to <code>int</code>.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>int</code>.
     */
    public final int intValue()
    {
        return (int)doubleValue();
    }

    /**
     * Returns the value of the specified number as a <code>long</code>.
     * This may involve rounding or truncation.  This implementation simply
     * casts the result of <code>doubleValue()</code> to <code>long</code>.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>long</code>.
     */
    public final long longValue()
    {
        return (long)doubleValue();
    }

    /**
     * Returns the value of the specified number as a <code>short</code>.
     * This may involve rounding or truncation.  This implementation simply
     * casts the result of <code>doubleValue()</code> to <code>short</code>.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>short</code>.
     */
    public final short shortValue()
    {
        return (short)doubleValue();
    }


    /**
     * Returns the denominator.
     */
    public final int getDenominator()
    {
        return this.denominator;
    }

    /**
     * Returns the numerator.
     */
    public final int getNumerator()
    {
        return this.numerator;
    }

    /**
     * Returns the reciprocal value of this obejct as a new Rational.
     * @return the reciprocal in a new object
     */
    public Rational getReciprocal()
    {
        return new Rational(this.denominator, this.numerator);
    }

    /**
     * Checks if this rational number is an Integer, either positive or negative.
     */
    public boolean isInteger()
    {
        if (denominator == 1 ||
                (denominator != 0 && (numerator % denominator == 0)) ||
                (denominator == 0 && numerator == 0)
        ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns a string representation of the object of form <code>numerator/denominator</code>.
     * @return  a string representation of the object.
     */
    public String toString()
    {
        return numerator + "/" + denominator;
    }

    /**
     * Returns the simplest represenation of this Rational's value possible.
     */
    public String toSimpleString(boolean allowDecimal)
    {
        if (denominator == 0 && numerator != 0) {
            return toString();
        } else if (isInteger()) {
            return Integer.toString(intValue());
        } else if (numerator != 1 && denominator % numerator == 0) {
            // common factor between denominator and numerator
            int newDenominator = denominator / numerator;
            return new Rational(1, newDenominator).toSimpleString(allowDecimal);
        } else {
            Rational simplifiedInstance = getSimplifiedInstance();
            if (allowDecimal) {
                String doubleString = Double.toString(simplifiedInstance.doubleValue());
                if (doubleString.length() < 5) {
                    return doubleString;
                }
            }
            return simplifiedInstance.toString();
        }
    }

    /**
     * Decides whether a brute-force simplification calculation should be avoided
     * by comparing the maximum number of possible calculations with some threshold.
     * @return true if the simplification should be performed, otherwise false
     */
    private boolean tooComplexForSimplification()
    {
        double maxPossibleCalculations = (((double)(Math.min(denominator, numerator) - 1) / 5d) + 2);
        return maxPossibleCalculations > maxSimplificationCalculations;
    }

    /**
     * Compares two <code>Rational</code> instances, returning true if they are mathematically
     * equivalent.
     * @param obj the Rational to compare this instance to.
     * @return true if instances are mathematically equivalent, otherwise false.  Will also
     *         return false if <code>obj</code> is not an instance of <code>Rational</code>.
     */
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Rational)) {
            return false;
        }
        Rational that = (Rational)obj;
        return this.doubleValue() == that.doubleValue();
    }

    /**
     * <p>
     * Simplifies the Rational number.</p>
     * <p>
     * Prime number series: 1, 2, 3, 5, 7, 9, 11, 13, 17</p>
     * <p>
     * To reduce a rational, need to see if both numerator and denominator are divisible
     * by a common factor.  Using the prime number series in ascending order guarantees
     * the minimun number of checks required.</p>
     * <p>
     * However, generating the prime number series seems to be a hefty task.  Perhaps
     * it's simpler to check if both d & n are divisible by all numbers from 2 ->
     * (Math.min(denominator, numerator) / 2).  In doing this, one can check for 2
     * and 5 once, then ignore all even numbers, and all numbers ending in 0 or 5.
     * This leaves four numbers from every ten to check.</p>
     * <p>
     * Therefore, the max number of pairs of modulus divisions required will be:</p>
     * <code><pre>
     *    4   Math.min(denominator, numerator) - 1
     *   -- * ------------------------------------ + 2
     *   10                    2
     *
     *   Math.min(denominator, numerator) - 1
     * = ------------------------------------ + 2
     *                  5
     * </pre></code>
     * @return a simplified instance, or if the Rational could not be simpliffied,
     *         returns itself (unchanged)
     */
    public Rational getSimplifiedInstance()
    {
        if (tooComplexForSimplification()) {
            return this;
        }
        for (int factor = 2; factor <= Math.min(denominator, numerator); factor++) {
            if ((factor % 2 == 0 && factor > 2) || (factor % 5 == 0 && factor > 5)) {
                continue;
            }
            if (denominator % factor == 0 && numerator % factor == 0) {
                // found a common factor
                return new Rational(numerator / factor, denominator / factor);
            }
        }
        return this;
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}
