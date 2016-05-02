<<<<<<< HEAD
package skylight1.opengl.files;

/**
 * Parses strings to numbers quickly.
 * Arguments not checked or trimmed.
 * Limited formats accepted.
 * 
 */
public class QuickParseUtil {

	/**
	 * Parse string containing a float.
	 * Assumes input trimmed, not null, not infinity, not NaN, not using exponent representation.
	 * Handles only: [-+]?\d+(.\d+)?
	 * <p>
	 * Current implementation took 12ms to parse 1000 test strings,
	 * Float.parseFloat took 465ms to parse the same strings.
	 * 
	 * @param aInput
	 *            String representation of a float number
	 * @return parsed float
	 */
	public static float parseFloat(final String aInput) {
		// XXX Might be cleaner to use Float.intBitsToFloat with a researched algorithm to find inputs:
		// http://portal.acm.org/citation.cfm?doid=93542.93557

		// Read sign.
		int index = 0;
		boolean isNegative = false;
		switch (aInput.charAt(0)) {
			case '-':
				isNegative = true;
				// Fall through.
			case '+':
				index++;
				// No default action.
		}

		// Read integer before the decimal.
		final int length = aInput.length();
		int integer = 0;
		for (; index < length; index++) {
			char character = aInput.charAt(index);
			if (character == '.') {
				index++;
				break;
			}

			// Pre-calculated power values in an array doesn't seem to help speed
			// when using integer math. It did previously when using float math.
			// Maybe Java's array bounds checking slows it down.
			integer *= 10;
			integer += character - '0';
		}

		// Read fraction after the decimal.
		int numerator = 0;
		int denominator = 1;
		for (; index < length; index++) {
			char character = aInput.charAt(index);

			denominator *= 10;
			numerator *= 10;
			numerator += character - '0';
		}

		// Calculate and return result.
		float result = integer + numerator / (float) denominator;
		return isNegative ? -result : result;
	}
	

	private final static int[][] INTEGER_DECIMAL_VALUES = new int[4][10];

	static {
		for (int decimalPlace = 0; decimalPlace < INTEGER_DECIMAL_VALUES.length; decimalPlace++) {
			for (int decimalValue = 0; decimalValue < 10; decimalValue++) {
				INTEGER_DECIMAL_VALUES[decimalPlace][decimalValue] = (int) (Math.pow(10d, decimalPlace) * decimalValue);
			}
		}
	}

	public static int parseInteger(final String aStringRepresentationOfAnInteger) {
		final int startOfDigits;
		final int sign;
		if (aStringRepresentationOfAnInteger.charAt(0) == '-') {
			startOfDigits = 1;
			sign = -1;
		} else {
			startOfDigits = 0;
			sign = 1;
		}
		int result = 0;
		int decimalPlace = -1;
		final int stringLength = aStringRepresentationOfAnInteger.length();
		for (int i = stringLength - 1; i >= startOfDigits; i--) {
			decimalPlace++;
			result += INTEGER_DECIMAL_VALUES[decimalPlace][aStringRepresentationOfAnInteger.charAt(i) - '0'];
		}
		return sign * result;
	}
	
}

=======
/*
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
}
>>>>>>> 76aa07461566a5976980e6696204781271955163
