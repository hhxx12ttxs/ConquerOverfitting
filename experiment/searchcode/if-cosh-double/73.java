//#preprocessor

//---------------------------------------------------------------------------------
//
// BlackBerry Extensions
// Copyright (c) 2008-2012 Vincent Simonetti
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
// documentation files (the "Software"), to deal in the Software without restriction, including without limitation 
// the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
// to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
// INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
// PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
// OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.
//
//---------------------------------------------------------------------------------
//
package rebuild.util;

import rebuild.BBXResource;
import rebuild.Resources;
import rebuild.util.ref.RefLong;

//asin, acos, atan2 are from J4ME and use the Apache License 2.0

/**
 * Provides some basic numeric operations.
 * @since BBX 1.0.0
 */
public final class MathUtilities
{
	private static final long POSITIVEINFINITY = 0x7FF0000000000000L;
	private static final long SIGN = 0x8000000000000000L;
	
//#ifdef BlackBerrySDK4.5.0
	private static final double LOG2 = 0.69314718055994530941723212145818;
//#endif
	
	/**
	 * Returns the arc cosine of the value x.
	 * @param x the value.
	 * @return the arc cosine of the argument in radians.
	 * @since BBX 1.0.0
	 */
	public static double acos(double x)
	{
//#ifdef BlackBerrySDK4.5.0
		//return (Math.PI / 2) - asin(x);
		
		// Special case.
		if (Double.isNaN(x) || Math.abs(x) > 1.0)
		{
			return Double.NaN;
		}
		
		// Calculate the arc cosine.
		double aSquared = x * x;
		double arcCosine = atan2(Math.sqrt(1 - aSquared), x);
		return arcCosine;
//#else
		return net.rim.device.api.util.MathUtilities.acos(x);
//#endif
	}
	
	/**
	 * Returns the arc sine of the value x.
	 * @param x the value.
	 * @return the arc sine of the argument in radians.
	 * @since BBX 1.0.0
	 */
	public static double asin(double x)
	{
//#ifdef BlackBerrySDK4.5.0
		//Code from J4ME
		// Special cases.
		if (Double.isNaN(x) || Math.abs(x) > 1.0)
		{
			return Double.NaN;
		}
		
		if (x == 0.0)
		{
			return x;
		}
		
		// Calculate the arc sine.
		double aSquared = x * x;
		double arcSine = atan2(x, Math.sqrt(1 - aSquared));
		return arcSine;
//#else
		return net.rim.device.api.util.MathUtilities.asin(x);
//#endif
	}
	
//#ifdef BlackBerrySDK4.5.0
	//atan code and constants taken from cmath.tgz (http://www.netlib.org/cephes/)
	
	/*
	Cephes Math Library Release 2.8:  June, 2000
	Copyright 1984, 1995, 2000 by Stephen L. Moshier
	*/
	/* arctan(x)  = x + x^3 P(x^2)/Q(x^2)
	   0 <= x <= 0.66
	   Peak relative error = 2.6e-18  */
	private static final double[] P = {
	    -8.750608600031904122785E-1,
	    -1.615753718733365076637E1,
	    -7.500855792314704667340E1,
	    -1.228866684490136173410E2,
	    -6.485021904942025371773E1
    };
    private static final double[] Q = {
	    /* 1.000000000000000000000E0, */
	     2.485846490142306297962E1,
	     1.650270098316988542046E2,
	     4.328810604912902668951E2,
	     4.853903996359136964868E2,
	     1.945506571482613964425E2
    };
    
    /* tan( 3*pi/8 ) */
    private static final double T3P8 = 2.41421356237309504880;
    /* pi/2 = PIO2 + MOREBITS.  */
    private static final double MOREBITS = 6.123233995736765886130E-17;
    
    //polevl and p1evl taken from Java-ML 0.1.6 (Java Machine Learning Library)(http://java-ml.sourceforge.net/)
    /*
    Copyright (C) 1999 CERN - European Organization for Nuclear Research.
    Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
    is hereby granted without fee, provided that the above copyright notice appear in all copies and 
    that both that copyright notice and this permission notice appear in supporting documentation. 
    CERN makes no representations about the suitability of this software for any purpose. 
    It is provided "as is" without expressed or implied warranty.
    */
    private static double p1evl(double x, double[] coef, int N)
    {
        double ans;
        
        ans = x + coef[0];
        
        for (int i = 1; i < N; i++)
        {
            ans = ans * x + coef[i];
        }
        
        return ans;
    }
    
    private static double polevl(double x, double[] coef, int N)
    {
        double ans;
        ans = coef[0];
        
        for (int i = 1; i <= N; i++)
        {
        	ans = ans * x + coef[i];
        }
        
        return ans;
    }
//#endif
	
	/**
	 * Returns the arc tangent of the value x.
	 * @param x the value.
	 * @return the arc tangent of the argument in radians.
	 * @since BBX 1.0.0
	 */
	public static double atan(double x)
	{
//#ifdef BlackBerrySDK4.5.0
		// Special cases.
        if (Double.isNaN(x))
        {
            return Double.NaN;
        }
        
        if(Double.isInfinite(x))
		{
			if(x < 0)
			{
				return -Math.PI / 2;
			}
			else
			{
				return Math.PI / 2;
			}
		}
        
        if (x == 0.0)
        {
            return x;
        }
        
        // Compute the arc tangent.
        double y, z;
        short sign, flag;
        
        //make argument positive and save the sign
        sign = 1;
        if (x < 0.0)
        {
            sign = -1;
            x = -x;
        }
        //range reduction
        flag = 0;
        if (x > T3P8)
        {
            y = Math.PI / 2;
            flag = 1;
            x = -(1.0 / x);
        }
        else if (x <= 0.66)
        {
            y = 0.0;
        }
        else
        {
            y = Math.PI / 4;
            flag = 2;
            x = (x - 1.0) / (x + 1.0);
        }
        z = x * x;
        z = z * polevl(z, P, 4) / p1evl(z, Q, 5);
        z = x * z + x;
        if (flag == 2)
        {
        	z += 0.5 * MOREBITS;
        }
        else if (flag == 1)
        {
        	z += MOREBITS;
        }
        y = y + z;
        if (sign < 0)
        {
        	y = -y;
        }
        return y;
//#else
		return net.rim.device.api.util.MathUtilities.atan(x);
//#endif
	}
	
	/**
	 * Converts rectangular coordinates (x,y) to polar coordinates (r,theta).
	 * This method computes the phase theta by computing the arc tangent of y/x in the range of -pi to pi.
	 * @param y the abscissa coordinate.
	 * @param x the ordinate coordinate.
	 * @return the theta component of the point (r,theta) in polar coordinates that corresponds to the point (x,y) in Cartesian coordinates.
	 * @since BBX 1.0.0
	 */
	public static double atan2(double y, double x)
	{
//#ifdef BlackBerrySDK4.5.0
		//Code from J4ME
		if(Double.isNaN(y) || Double.isNaN(x))
		{
			return Double.NaN;
		}
		else if (Double.isInfinite(y))
        {
            if (y > 0.0) // Positive infinity
            {
                if (Double.isInfinite(x))
                {
                    if (x > 0.0)
                    {
                        return Math.PI / 4;
                    }
                    else
                    {
                        return 3.0 * Math.PI / 4;
                    }
                }
                else if (x != 0.0)
                {
                    return Math.PI / 2;
                }
            }
            else  // Negative infinity
            {
                if (Double.isInfinite(x))
                {
                    if (x > 0.0)
                    {
                        return -Math.PI / 4;
                    }
                    else
                    {
                        return -3.0 * Math.PI / 4;
                    }
                }
                else if (x != 0.0)
                {
                    return -Math.PI / 2;
                }
            }
        }
        else if (y == 0.0)
        {
            if (x > 0.0)
            {
                return y;
            }
            else if (x < 0.0)
            {
                return Math.PI;
            }
        }
        else if (Double.isInfinite(x))
        {
            if (x > 0.0)  // Positive infinity
            {
                if (y > 0.0)
                {
                    return 0.0;
                }
                else if (y < 0.0)
                {
                    return -0.0;
                }
            }
            else  // Negative infinity
            {
                if (y > 0.0)
                {
                    return Math.PI;
                }
                else if (y < 0.0)
                {
                    return -Math.PI;
                }
            }
        }
        else if (x == 0.0)
        {
            if (y > 0.0)
            {
                return Math.PI / 2;
            }
            else if (y < 0.0)
            {
                return -Math.PI / 2;
            }
        }
		
		// Implementation a simple version ported from a PASCAL implementation:
        //   http://everything2.com/index.pl?node_id=1008481
		
        double arcTangent;
        
        // Use arctan() avoiding division by zero.
        if (Math.abs(x) > Math.abs(y))
        {
            arcTangent = atan(y / x);
        }
        else
        {
            arcTangent = atan(x / y); // -PI/4 <= a <= PI/4

            if (arcTangent < 0)
            {
                arcTangent = -(Math.PI / 2) - arcTangent; // a is negative, so we're adding
            }
            else
            {
                arcTangent = (Math.PI / 2) - arcTangent;
            }
        }
        
        // Adjust result to be from [-PI, PI]
        if (x < 0)
        {
            if (y < 0)
            {
                arcTangent = arcTangent - Math.PI;
            }
            else
            {
                arcTangent = arcTangent + Math.PI;
            }
        }
        
        return arcTangent;
//#else
		return net.rim.device.api.util.MathUtilities.atan2(y, x);
//#endif
	}
	
	/**
	 * Clamps provided integer value between a lower and upper bound.
	 * @param low Lower bound value; must be less than your upper bound parameter.
	 * @param value Value to clamp.
	 * @param high Upper bound value.
	 * @return If the value is lower than the lower bound, this method returns the lower bound; if the value is higher than the higher bound, this method returns 
	 * the higher bound; otherwise, this method returns the value itself.
	 * @throws IllegalArgumentException If you provide a low parameter value that is greater than your high parameter value.
	 * @since BBX 1.0.0
	 */
	public static int clamp(int low, int value, int high)
	{
		return net.rim.device.api.util.MathUtilities.clamp(low, value, high);
	}
	
	/**
	 * Clamps provided long integer value between a lower and upper bound.
	 * @param low Lower bound value; must be less than your upper bound parameter.
	 * @param value Value to clamp.
	 * @param high Upper bound value.
	 * @return If the value is lower than the lower bound, this method returns the lower bound; if the value is higher than the higher bound, this method returns 
	 * the higher bound; otherwise, this method returns the value itself.
	 * @throws IllegalArgumentException If you provide a low parameter value that is greater than your high parameter value.
	 * @since BBX 1.0.1
	 */
	public static long clamp(long low, long value, long high)
	{
//#ifdef BlackBerrySDK4.5.0 | BlackBerrySDK4.6.0 | BlackBerrySDK4.6.1 | BlackBerrySDK4.7.0 | BlackBerrySDK4.7.1 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0
		if(low > high)
		{
			throw new IllegalArgumentException("low > high");
		}
		return Math.max(low, Math.min(high, value));
//#else
		return net.rim.device.api.util.MathUtilities.clamp(low, value, high);
//#endif
	}
	
	/**
	 * Clamps provided float value between a lower and upper bound.
	 * @param low Lower bound value; must be less than your upper bound parameter.
	 * @param value Value to clamp.
	 * @param high Upper bound value.
	 * @return If the value is lower than the lower bound, this method returns the lower bound; if the value is higher than the higher bound, this method returns 
	 * the higher bound; otherwise, this method returns the value itself.
	 * @throws IllegalArgumentException If you provide a low parameter value that is greater than your high parameter value.
	 * @since BBX 1.0.1
	 */
	public static float clamp(float low, float value, float high)
	{
//#ifdef BlackBerrySDK4.5.0 | BlackBerrySDK4.6.0 | BlackBerrySDK4.6.1 | BlackBerrySDK4.7.0 | BlackBerrySDK4.7.1 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0
		if(low > high)
		{
			throw new IllegalArgumentException("low > high");
		}
		return Math.max(low, Math.min(high, value));
//#else
		return net.rim.device.api.util.MathUtilities.clamp(low, value, high);
//#endif
	}
	
	/**
	 * Clamps provided double value between a lower and upper bound.
	 * @param low Lower bound value; must be less than your upper bound parameter.
	 * @param value Value to clamp.
	 * @param high Upper bound value.
	 * @return If the value is lower than the lower bound, this method returns the lower bound; if the value is higher than the higher bound, this method returns 
	 * the higher bound; otherwise, this method returns the value itself.
	 * @throws IllegalArgumentException If you provide a low parameter value that is greater than your high parameter value.
	 * @since BBX 1.0.1
	 */
	public static double clamp(double low, double value, double high)
	{
//#ifdef BlackBerrySDK4.5.0 | BlackBerrySDK4.6.0 | BlackBerrySDK4.6.1 | BlackBerrySDK4.7.0 | BlackBerrySDK4.7.1 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0
		if(low > high)
		{
			throw new IllegalArgumentException("low > high");
		}
		return Math.max(low, Math.min(high, value));
//#else
		return net.rim.device.api.util.MathUtilities.clamp(low, value, high);
//#endif
	}
	
	/**
	 * Return the exponential (base e) of x.
	 * Interesting cases:
	 * <ul>
	 * 	<li>If the argument is negative infinity, the result is zero.</li>
	 * 	<li>If the argument is positive infinity, the result is positive infinity.</li>
	 * 	<li>If the argument is NaN, the result is NaN.</li>
	 * 	<li>If the argument is greater than 7.09e+02, the result is positive infinity.</li>
	 * 	<li>If the argument is less than -7.45e+02, the result is zero.</li>
	 * </ul>
	 * @param x the power to raise e to.
	 * @return e raised to the power x.
	 * @since BBX 1.0.0
	 */
	public static double exp(double x)
	{
		return net.rim.device.api.util.MathUtilities.exp(x);
	}
	
	/**
	 * Returns the result of multiplying x (the significand) by 2 raised to the power of exp (the exponent).
	 * @param x the significand.
	 * @param exp the exponent.
	 * @return x * 2exp.
	 * @since BBX 1.0.0
	 */
	public static double ldexp(double x, int exp)
	{
//#ifdef BlackBerrySDK4.5.0
		//"x * e^(exp * log(2.0))" AKA "x * 2^exp"
		return x * net.rim.device.api.util.MathUtilities.exp(exp * LOG2);
//#else
		return net.rim.device.api.util.MathUtilities.ldexp(x, exp);
//#endif
	}
	
	/**
	 * Return the natural logarithm (base e) of x.
	 * Interesting cases:
	 * <ul>
	 * 	<li>If the argument is less than zero (including negative infinity), the result is NaN.</li>
	 * 	<li>If the argument is zero, the result is negative infinity.</li>
	 * 	<li>If the argument is positive infinity, the result is positive infinity.</li>
	 * 	<li>If the argument is NaN, the result is NaN.</li>
	 * </ul>
	 * @param x a number greater than zero.
	 * @return the natural logarithm (base e) of the argument.
	 * @since BBX 1.0.0
	 */
	public static double log(double x)
	{
		return net.rim.device.api.util.MathUtilities.log(x);
	}
	
	/**
	 * Returns the log base 2 of the unsigned value rounded down.
	 * <p>
	 * The value zero and one both return zero.
	 * @param value The unsigned value to calculate log2 on.
	 * @return The calculation of log2(value) (range: 0-31).
	 * @since BBX 1.0.0
	 */
	public static int log2(int value)
	{
		return net.rim.device.api.util.MathUtilities.log2(value);
	}
	
	/**
	 * Returns the log base 2 of the unsigned value rounded down.
	 * <p>
	 * The value zero and one both return a log2 result of zero.
	 * @param value The unsigned value to calculate log2 on.
	 * @return The calculation of log2(value) (range: 0-63).
	 * @since BBX 1.0.0
	 */
	public static int log2(long value)
	{
		return net.rim.device.api.util.MathUtilities.log2(value);
	}
	
	/**
	 * Return x raised to the power of y.
	 * @param x the base value.
	 * @param y the exponent.
	 * @return xy.
	 * @since BBX 1.0.0
	 */
	public static double pow(double x, double y)
	{
//#ifdef BlackBerrySDK4.5.0
		//e^(y * log(x))
		return net.rim.device.api.util.MathUtilities.exp(y * net.rim.device.api.util.MathUtilities.log(x));
//#else
		return net.rim.device.api.util.MathUtilities.pow(x, y);
//#endif
	}
	
	/**
	 * Returns the closest long to the argument.
	 * The result is rounded to an integer by adding 1/2 if positive or subtracting 1/2 if negative, taking the floor of the result, and casting the result to type long.
	 * <p>
	 * Special cases:
	 * <ul>
	 * 	<li>If the argument is NaN, the result is 0.</li>
	 * 	<li>If the argument is negative infinity or any value less than or equal to the value of Long.MIN_VALUE, the result is equal to the value of Long.MIN_VALUE.</li>
	 * 	<li>If the argument is positive infinity or any value greater than or equal to the value of Long.MAX_VALUE, the result is equal to the value of Long.MAX_VALUE.</li>
	 * 	<li>If the argument is an exact negative half (-x.5), the value is rounded to the closest value to zero.</li>
	 * </ul>
	 * @param a a floating-point value to be rounded to a long.
	 * @return the value of the argument rounded to the nearest long value.
	 * @see Long#MAX_VALUE
	 * @see Long#MIN_VALUE
	 * @since BBX 1.0.0
	 */
	public static long round(double a)
	{
//#ifdef BlackBerrySDK4.5.0
		if(a == 0)
		{
			return 0;
		}
		else if(Double.isNaN(a))
		{
			return 0;
		}
		else if(Double.isInfinite(a))
		{
			if(a == Double.NEGATIVE_INFINITY)
			{
				return Long.MIN_VALUE;
			}
			else
			{
				return Long.MAX_VALUE;
			}
		}
		else if(a <= Long.MIN_VALUE)
		{
			return Long.MIN_VALUE;
		}
		else if(a >= Long.MAX_VALUE)
		{
			return Long.MAX_VALUE;
		}
		else
		{
			if(a < 0)
			{
				a -= 0.5;
			}
			else
			{
				a += 0.5;
			}
			return (long)Math.floor(a);
		}
//#else
		return net.rim.device.api.util.MathUtilities.round(a);
//#endif
	}
	
	/**
	 * Returns the closest int to the argument.
	 * The result is rounded to an integer by adding 1/2 if positive or subtracting 1/2 if negative, taking the floor of the result, and casting the result to type int.
	 * <p>
	 * Special cases:
	 * <ul>
	 * 	<li>If the argument is NaN, the result is 0.</li>
	 * 	<li>If the argument is negative infinity or any value less than or equal to the value of Integer.MIN_VALUE, the result is equal to the value of Integer.MIN_VALUE.</li>
	 * 	<li>If the argument is positive infinity or any value greater than or equal to the value of Integer.MAX_VALUE, the result is equal to the value of Integer.MAX_VALUE.</li>
	 * 	<li>If the argument is an exact negative half (-x.5), the value is rounded to the closes value to zero.</li>
	 * </ul>
	 * @param a a floating-point value to be rounded to an integer.
	 * @return the value of the argument rounded to the nearest int value.
	 * @see Integer#MAX_VALUE
	 * @see Integer#MIN_VALUE
	 * @since BBX 1.0.0
	 */
	public static int round(float a)
	{
//#ifdef BlackBerrySDK4.5.0
		if(a == 0)
		{
			return 0;
		}
		else if(Float.isNaN(a))
		{
			return 0;
		}
		else if(Float.isInfinite(a))
		{
			if(a == Float.NEGATIVE_INFINITY)
			{
				return Integer.MIN_VALUE;
			}
			else
			{
				return Integer.MAX_VALUE;
			}
		}
		else if(a <= Integer.MIN_VALUE)
		{
			return Integer.MIN_VALUE;
		}
		else if(a >= Integer.MAX_VALUE)
		{
			return Integer.MAX_VALUE;
		}
		else
		{
			if(a < 0)
			{
				a -= 0.5f;
			}
			else
			{
				a += 0.5f;
			}
			return (int)Math.floor(a);
		}
//#else
		return net.rim.device.api.util.MathUtilities.round(a);
//#endif
	}
	
	/**
	 * Wraps provided integer value around a lower and upper bound.
	 * <p>
	 * This method does the opposite of {@link MathUtilities#clamp(int, int, int)}.
	 * @param low Lower bound value; must be less than your upper bound parameter.
	 * @param value Value to wrap.
	 * @param high Upper bound value.
	 * @return If the value is lower than the lower bound, this method returns the higher bound; if the value is higher than the higher bound, this method returns 
	 * the lower bound; otherwise, this method returns the value itself.
	 * @throws IllegalArgumentException If you provide a low parameter value that is greater than your high parameter value.
	 * @since BBX 1.0.0
	 */
	public static int wrap(int low, int value, int high)
	{
		return net.rim.device.api.util.MathUtilities.wrap(low, value, high);
	}
	
	/**
	 * Wraps provided long integer value around a lower and upper bound.
	 * <p>
	 * This method does the opposite of {@link MathUtilities#clamp(long, long, long)}.
	 * @param low Lower bound value; must be less than your upper bound parameter.
	 * @param value Value to wrap.
	 * @param high Upper bound value.
	 * @return If the value is lower than the lower bound, this method returns the higher bound; if the value is higher than the higher bound, this method returns 
	 * the lower bound; otherwise, this method returns the value itself.
	 * @throws IllegalArgumentException If you provide a low parameter value that is greater than your high parameter value.
	 * @since BBX 1.0.1
	 */
	public static long wrap(long low, long value, long high)
	{
//#ifdef BlackBerrySDK4.5.0 | BlackBerrySDK4.6.0 | BlackBerrySDK4.6.1 | BlackBerrySDK4.7.0 | BlackBerrySDK4.7.1 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0
		if(low > high)
		{
			throw new IllegalArgumentException("low > high");
		}
		if(value < low)
		{
			return high;
		}
		else if(value > high)
		{
			return low;
		}
		return value;
//#else
		return net.rim.device.api.util.MathUtilities.wrap(low, value, high);
//#endif
	}
	
	/**
	 * Wraps provided float value around a lower and upper bound.
	 * <p>
	 * This method does the opposite of {@link MathUtilities#clamp(float, float, float)}.
	 * @param low Lower bound value; must be less than your upper bound parameter.
	 * @param value Value to wrap.
	 * @param high Upper bound value.
	 * @return If the value is lower than the lower bound, this method returns the higher bound; if the value is higher than the higher bound, this method returns 
	 * the lower bound; otherwise, this method returns the value itself.
	 * @throws IllegalArgumentException If you provide a low parameter value that is greater than your high parameter value.
	 * @since BBX 1.0.1
	 */
	public static float wrap(float low, float value, float high)
	{
//#ifdef BlackBerrySDK4.5.0 | BlackBerrySDK4.6.0 | BlackBerrySDK4.6.1 | BlackBerrySDK4.7.0 | BlackBerrySDK4.7.1 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0
		if(low > high)
		{
			throw new IllegalArgumentException("low > high");
		}
		if(value < low)
		{
			return high;
		}
		else if(value > high)
		{
			return low;
		}
		return value;
//#else
		return net.rim.device.api.util.MathUtilities.wrap(low, value, high);
//#endif
	}
	
	/**
	 * Wraps provided double value around a lower and upper bound.
	 * <p>
	 * This method does the opposite of {@link MathUtilities#clamp(double, double, double)}.
	 * @param low Lower bound value; must be less than your upper bound parameter.
	 * @param value Value to wrap.
	 * @param high Upper bound value.
	 * @return If the value is lower than the lower bound, this method returns the higher bound; if the value is higher than the higher bound, this method returns 
	 * the lower bound; otherwise, this method returns the value itself.
	 * @throws IllegalArgumentException If you provide a low parameter value that is greater than your high parameter value.
	 * @since BBX 1.0.1
	 */
	public static double wrap(double low, double value, double high)
	{
		if(low > high)
		{
			throw new IllegalArgumentException("low > high");
		}
		if(value < low)
		{
			return high;
		}
		else if(value > high)
		{
			return low;
		}
		return value;
	}
	
	/**
	 * Returns a value indicating whether the specified number evaluates to negative infinity.
	 * @param d A single-precision floating point number.
	 * @return true if d evaluates to {@link Float.NEGATIVE_INFINITY}; otherwise, false.
	 * @since BBX 1.1.0
	 */
	public static boolean isNegativeInfinity(float d)
	{
	    return (d == Float.NEGATIVE_INFINITY);
	}
	
	/**
	 * Returns a value indicating whether the specified number evaluates to positive infinity.
	 * @param d A single-precision floating point number.
	 * @return true if d evaluates to {@link Float.POSITIVE_INFINITY}; otherwise, false.
	 * @since BBX 1.1.0
	 */
	public static boolean isPositiveInfinity(float d)
	{
	    return (d == Float.POSITIVE_INFINITY);
	}
	
	/**
	 * Returns a value indicating whether the specified number evaluates to negative infinity.
	 * @param d A double-precision floating point number.
	 * @return true if d evaluates to {@link Double.NEGATIVE_INFINITY}; otherwise, false.
	 * @since BBX 1.1.0
	 */
	public static boolean isNegativeInfinity(double d)
	{
	    return (d == Double.NEGATIVE_INFINITY);
	}
	
	/**
	 * Returns a value indicating whether the specified number evaluates to positive infinity.
	 * @param d A double-precision floating point number.
	 * @return true if d evaluates to {@link Double.POSITIVE_INFINITY}; otherwise, false.
	 * @since BBX 1.1.0
	 */
	public static boolean isPositiveInfinity(double d)
	{
	    return (d == Double.POSITIVE_INFINITY);
	}
	
	/**
	 * Produces the full product of two 32-bit numbers.
	 * @param a The first int to multiply.
	 * @param b The second int to multiply.
	 * @return The long containing the product of the specified numbers.
	 * @since BBX 1.1.0
	 */
	public static long bigMul(int a, int b)
	{
	    return ((long)a * (long)b);
	}
	
	/**
	 * Calculates the quotient of two 64-bit signed integers and also returns the remainder in an output parameter.
	 * @param a The long that contains the dividend.
	 * @param b The long that contains the divisor.
	 * @param result The {@link RefLong} that receives the remainder.
	 * @return The long containing the quotient of the specified numbers.
	 * @throws ArithmeticException b is zero.
	 * @since BBX 1.1.0
	 */
	public static int divRem(int a, int b, RefLong result)
	{
		result.setValue(a % b);
	    return (a / b);
	}
	
	/**
	 * Returns the logarithm of a specified number in a specified base.
	 * @param a A number whose logarithm is to be found.
	 * @param newBase The base of the logarithm.
	 * @return In the following table +Infinity denotes {@link Double.POSITIVE_INFINITY}, -Infinity denotes {@link Double.NEGATIVE_INFINITY}, and {@link Double.NaN} denotes {@link Double.NaN}.
	 * <p><table><tr><th><p><span>a</span></p></th><th><p><span>newBase</span></p></th><th><p>Return Value</p>
	 * </th></tr><tr><td><p><span>a</span><span>&gt;</span> 0</p></td><td><p>(0 <span>&lt;</span><span>newBase</span>
	 * <span>&lt;</span> 1) -or-(<span>newBase</span><span>&gt;</span> 1)</p></td><td><p>log<sub>newBase</sub>(a)</p>\
	 * </td></tr><tr><td><p><span>a</span><span>&lt;</span> 0</p></td><td><p>(any value)</p></td><td><p>NaN</p></td></tr>
	 * <tr><td><p>(any value)</p></td><td><p><span>newBase</span><span>&lt;</span> 0</p></td><td><p>NaN</p></td></tr><tr>
	 * <td><p><span>a</span> != 1</p></td><td><p><span>newBase</span> = 0</p></td><td><p>NaN</p></td></tr><tr><td><p>
	 * <span>a</span> != 1</p></td><td><p><span>newBase</span> = +Infinity</p></td><td><p>NaN</p></td></tr><tr><td><p>
	 * <span>a</span> = NaN</p></td><td><p>(any value)</p></td><td><p>NaN</p></td></tr><tr><td><p>(any value)</p></td>
	 * <td><p><span>newBase</span> = NaN</p></td><td><p>NaN</p></td></tr><tr><td><p>(any value)</p></td><td><p>
	 * <span>newBase</span> = 1</p></td><td><p>NaN</p></td></tr><tr><td><p><span>a</span> = 0</p></td><td>
	 * <p>0 <span>&lt;</span><span>newBase</span><span>&lt;</span> 1 </p></td><td><p>+Infinity</p></td></tr><tr><td><p>
	 * <span>a</span> = 0</p></td><td><p><span>newBase</span><span>&gt;</span> 1</p></td><td><p>-Infinity</p></td></tr>
	 * <tr><td><p><span>a</span> =  +Infinity</p></td><td>
	 * <p>0 <span>&lt;</span><span>newBase</span><span>&lt;</span> 1</p></td><td><p>-Infinity</p></td></tr><tr><td><p>
	 * <span>a</span> =  +Infinity</p></td><td><p><span>newBase</span><span>&gt;</span> 1</p></td><td><p>+Infinity</p>
	 * </td></tr><tr><td><p><span>a</span> = 1</p></td><td><p><span>newBase</span> = 0</p></td><td><p>0</p></td></tr>
	 * <tr><td><p><span>a</span> = 1</p></td><td><p><span>newBase</span> = +Infinity</p></td><td><p>0</p></td></tr>
	 * </table></p>
	 * @since BBX 1.1.0
	 */
	public static double log(double a, double newBase)
	{
		if ((newBase != 1.0) && ((a == 1.0) || ((newBase != 0.0) && !isPositiveInfinity(newBase))))
	    {
	        return (net.rim.device.api.util.MathUtilities.log(a) / net.rim.device.api.util.MathUtilities.log(newBase));
	    }
	    return Double.NaN;
	}
	
	/**
	 * Returns a value indicating the sign of a double-precision floating-point number.
	 * @param value A signed number.
	 * @return A number indicating the sign of value.Number Description -1 value is less than zero. 0 value is equal to zero. 1 value is greater than zero.
	 * @throws ArithmeticException value is equal to {@link Double.NaN}.
	 * @since BBX 1.1.0
	 */
	public static int sign(double value)
	{
	    if (value < 0.0)
	    {
	        return -1;
	    }
	    if (value > 0.0)
	    {
	        return 1;
	    }
	    if (value != 0.0)
	    {
	        throw new ArithmeticException(Resources.getString(BBXResource.MATH_NaN));
	    }
	    return 0;
	}
	
	/**
	 * Returns a value indicating the sign of a 8-bit signed integer.
	 * @param value A signed number.
	 * @return A number indicating the sign of value.Number Description -1 value is less than zero. 0 value is equal to zero. 1 value is greater than zero.
	 * @since BBX 1.1.0
	 */
	public static int sign(byte value)
	{
	    if (value < 0)
	    {
	        return -1;
	    }
	    if (value > 0)
	    {
	        return 1;
	    }
	    return 0;
	}
	
	/**
	 * Returns a value indicating the sign of a 16-bit signed integer.
	 * @param value A signed number.
	 * @return A number indicating the sign of value.Number Description -1 value is less than zero. 0 value is equal to zero. 1 value is greater than zero.
	 * @since BBX 1.1.0
	 */
	public static int sign(short value)
	{
	    if (value < 0)
	    {
	        return -1;
	    }
	    if (value > 0)
	    {
	        return 1;
	    }
	    return 0;
	}
	
	/**
	 * Returns a value indicating the sign of a 32-bit signed integer.
	 * @param value A signed number.
	 * @return A number indicating the sign of value.Number Description -1 value is less than zero. 0 value is equal to zero. 1 value is greater than zero.
	 * @since BBX 1.1.0
	 */
	public static int sign(int value)
	{
	    if (value < 0)
	    {
	        return -1;
	    }
	    if (value > 0)
	    {
	        return 1;
	    }
	    return 0;
	}
	
	/**
	 * Returns a value indicating the sign of a 64-bit signed integer.
	 * @param value A signed number.
	 * @return A number indicating the sign of value.Number Description -1 value is less than zero. 0 value is equal to zero. 1 value is greater than zero.
	 * @since BBX 1.1.0
	 */
	public static int sign(long value)
	{
	    if (value < 0)
	    {
	        return -1;
	    }
	    if (value > 0)
	    {
	        return 1;
	    }
	    return 0;
	}
	
	/**
	 * Returns a value indicating the sign of a single-precision floating-point number.
	 * @param value A signed number.
	 * @return A number indicating the sign of value.Number Description -1 value is less than zero. 0 value is equal to zero. 1 value is greater than zero.
	 * @throws ArithmeticException value is equal to {@link Float.NaN}.
	 * @since BBX 1.1.0
	 */
	public static int sign(float value)
	{
	    if (value < 0f)
	    {
	        return -1;
	    }
	    if (value > 0f)
	    {
	        return 1;
	    }
	    if (value != 0f)
	    {
	    	throw new ArithmeticException(Resources.getString(BBXResource.MATH_NaN));
	    }
	    return 0;
	}
	
	//Similar to the "nextDouble" functions, I am not to sure where these hyperbolic functions came from. OpenJDK, J4ME, Google/Bing don't turn up anything. They seem to work and
	//match the Wikipedia definitions. But, knowing me, I probably got them from some open source library back in 2009 and don't remember which.
	
	/**
	 * Returns the hyperbolic sine of the specified angle.
	 * @param value An angle, measured in radians.
	 * @return The hyperbolic sine of value. If value is equal to {@link Double.NEGATIVE_INFINITY}, {@link Double.POSITIVE_INFINITY}, or {@link Double.NaN}, this method returns a {@link Double} equal to value.
	 * @since BBX 1.1.0
	 */
	public static double sinh(double value)
	{
		if(Double.isInfinite(value) || Double.isNaN(value))
		{
			return value;
		}
		if (value >= 0.0)
        {
            double epos = MathUtilities.exp(value);
            return (epos - 1.0 / epos) / 2.0;
        }
        else
        {
            double eneg = MathUtilities.exp(-value);
            return (1.0 / eneg - eneg) / 2.0;
        }
	}
	
	/**
	 * Returns the hyperbolic cosine of the specified angle.
	 * @param value An angle, measured in radians.
	 * @return The hyperbolic cosine of value.
	 * @since BBX 1.1.0
	 */
	public static double cosh(double value)
	{
		if(Double.isNaN(value))
		{
			return Double.NaN;
		}
		if(Double.isInfinite(value))
		{
			return Double.POSITIVE_INFINITY;
		}
		double retval = MathUtilities.exp(Math.abs(value));
		return (retval + 1.0 / retval) / 2.0;
	}
	
	/**
	 * Returns the hyperbolic tangent of the specified angle.
	 * @param value An angle, measured in radians.
	 * @return The hyperbolic tangent of value. If value is equal to {@link Double.NEGATIVE_INFINITY}, this method returns -1. If value is equal to {@link Double.POSITIVE_INFINITY}, this method returns 1. If value is equal to {@link Double.NaN}, this method returns {@link Double.NaN}.
	 * @since BBX 1.1.0
	 */
	public static double tanh(double value)
	{
		if(Double.isNaN(value))
		{
			return Double.NaN;
		}
		if(isNegativeInfinity(value) || (value < -50.0))
		{
			return -1.0;
		}
		else if(isPositiveInfinity(value) || (value > 50.0))
		{
			return 1.0;
		}
		double ebig = MathUtilities.exp(value);
        double esmall = 1.0 / ebig;
        return (ebig - esmall) / (ebig + esmall);
	}
	
	/**
	 * Finds the least double greater than d.
	 * @param d The double to get the next positive value of.
	 * @return The next positive double.
	 * @since BBX 1.1.0
	 */
	public static double nextDouble(double d)
    {
        return nextDouble(d, true);
    }
	
	/**
	 * Finds the greatest double less than d.
	 * @param d The double to get the next negative value of.
	 * @return The next negative double.
	 * @since BBX 1.1.0
	 */
    public static double previousDouble(double d)
    {
        return nextDouble(d, false);
    }
    
    //Hmm, after thinking about it. I don't remember where this function came from. It looks extremely similar to the one included in OpenJDK (java.text.ChoiceFormat) but that uses the 
    //significant portion of the double, and this one does not. I don't know if I based mine off it, used an older version, or what.
    
	/**
	 * Finds the least double greater than d (if positive == true), or the greatest double less than d (if positive == false).
	 * @param d The number to get the next double of.
	 * @param positive Should the next value be the next positive value (adding) or negative value (subtraction).
	 * @return The next double.
	 * @since BBX 1.1.0
	 */
	public static double nextDouble(double d, boolean positive)
    {
        //filter out NaN's
        if (Double.isNaN(d))
        {
            return d;
        }
        
        //zero's are also a special case
        if (d == 0.0)
        {
            double smallestPositiveDouble = Double.longBitsToDouble(1L);
            if (positive)
            {
                return smallestPositiveDouble;
            }
            else
            {
                return -smallestPositiveDouble;
            }
        }
        
        //if entering here, d is a nonzero value
        
        //hold all bits in a long for later use
        long bits = Double.doubleToLongBits(d);
        
        /* strip off the sign bit */
        long magnitude = bits & ~SIGN;
        
        //if next double away from zero, increase magnitude
        if ((bits > 0) == positive)
        {
            if (magnitude != POSITIVEINFINITY)
            {
                magnitude += 1;
            }
        }
        //else decrease magnitude
        else
        {
            magnitude -= 1;
        }
        
        //restore sign bit and return
        long signbit = bits & SIGN;
        return Double.longBitsToDouble(magnitude | signbit);
    }
	
	//Fixed point stuff from "http://jet.ro/files/The_neglected_art_of_Fixed_Point_arithmetic_20060913.pdf"
	
	/**
	 * Convert a 16.16 fixed-point number to a {@link Float}. Certain predefined values are returned with calculation.
	 * @param fp A 16.16 fixed-point number.
	 * @return A {@link Float} that represents a fixed-point number.
	 * @since BBX 1.1.0
	 */
	public static float fpToFloat(int fp)
	{
		return ((float)fp) * (1f / 65536f);
	}
	
	/**
	 * Convert a 16.16 fixed-point number to a {@link Double}. Certain predefined values are returned with calculation.
	 * @param fp A 16.16 fixed-point number.
	 * @return A {@link Double} that represents a fixed-point number.
	 * @since BBX 1.1.0
	 */
	public static double fpToDouble(int fp)
	{
		/*
		switch(fp)
		{
			case Fixed32.E:
				return Math.E;
			case Fixed32.MAX_VALUE:
				return 32767.9999847412109375;
			case Fixed32.MIN_VALUE:
				return -32768.0;
			case Fixed32.PI:
				return Math.PI;
			case Fixed32.PI_OVER_2:
				return PIOVER2;
			case Fixed32.RAD2DEG:
				return 180.0 / Math.PI;
			case Fixed32.TWOPI:
				return TWOPI;
			case Fixed32.FP090:
				return 90.0;
			case Fixed32.FP180:
				return 180.0;
			case Fixed32.FP270:
				return 270.0;
			case Fixed32.FP360:
				return 360.0;
			case Fixed32.HALF:
				return 0.5;
			case Fixed32.ONE:
				return 1.0;
			case Fixed32.QUARTER:
				return 0.25;
			default:
				double val = 0.0;
				//Get the fraction
				int i = Fixed32.toIntTenThou(fp & 0x0000FFFF);
				val += ((double)i) * 0.0001;
				//Get the whole number
				i = Fixed32.toInt(fp & 0x7FFF0000);
				val += (double)i;
				//Get sign
				if((fp & 0x80000000) == 80000000)
				{
					val = -val;
				}
				return val;
		}
		*/
		return ((double)fp) * (1.0 / 65536.0);
	}
	
	/**
	 * Convert a {@link Float} to a 16.16 fixed-point number. Certain predefined values are returned with calculation.
	 * @param value A {@link Float} number.
	 * @return A 16.16 fixed-point number that represents a {@link Float}.
	 * @since BBX 1.1.0
	 */
	public static int doubleToFP(float value)
	{
		return round((value * 65536f) + (value >= 0 ? 0.5f : -0.5f));
	}
	
	/**
	 * Convert a {@link Double} to a 16.16 fixed-point number. Certain predefined values are returned with calculation.
	 * @param value A {@link Double} number.
	 * @return A 16.16 fixed-point number that represents a {@link Double}.
	 * @since BBX 1.1.0
	 */
	public static int doubleToFP(double value)
	{
		/*
		rebuild.system.ref.RefInteger i = new rebuild.system.ref.RefInteger(0);
		if(predefinedFP(value, i))
		{
			return i.intValue();
		}
		int fp = 0;
		double abs = Math.abs(value);
		//Get the whole number
		double whole = truncate(abs);
		fp |= Fixed32.toFP((int)whole);
		abs -= whole;
		//Get the fraction
		abs *= 10000.0;
		whole = truncate(abs);
		fp |= Fixed32.tenThouToFP((int)whole);
		//Get sign
		if(value < 0.0)
		{
			fp = -fp;
		}
		return fp;
		*/
		return (int)round((value * 65536.0) + (value >= 0 ? 0.5 : -0.5));
	}
}

