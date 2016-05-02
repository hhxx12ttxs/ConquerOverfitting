/*
 * Copyright (c) 2009 Christian Riekoff <info@texone.org>
 * 
 * This file is free software: you may copy, redistribute and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This file is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.

 */
package cc.creativecomputing.math;

import cc.creativecomputing.math.random.CCFarbrauschNoise;
import cc.creativecomputing.math.random.CCRandom;

/**
 * <p>
 * This class contains methods for performing basic numeric operations such as the elementary exponential, 
 * logarithm, square root, and trigonometric functions.
 * </p> 
 * @author christianriekoff
 *
 */
public class CCMath {
	/**
     * Square root of 2
     */
    public static final float SQRT2 = (float) Math.sqrt(2);

    /**
     * Square root of 3
     */
    public static final float SQRT3 = (float) Math.sqrt(3);

    
	/** A "close to zero" double epsilon value for use*/
    public static final double DBL_EPSILON = 2.220446049250313E-16d;

    /** A "close to zero" float epsilon value for use*/
    public static final float FLT_EPSILON = 1.1920928955078125E-7f;

    /** A "close to zero" float epsilon value for use*/
    public static final float ZERO_TOLERANCE = 0.0001f;
    
	public static final float PI = (float) Math.PI;
	public static final float HALF_PI = PI / 2.0f;
	public static final float THIRD_PI = PI / 3.0f;
	public static final float QUARTER_PI = PI / 4.0f;
	public static final float TWO_PI = PI * 2.0f;

	public static final float DEG_TO_RAD = PI / 180.0f;
	public static final float RAD_TO_DEG = 180.0f / PI;

	public static final CCRandom RANDOM = new CCRandom();

	public static final CCFarbrauschNoise NOISE = new CCFarbrauschNoise();
	
	public static float random(){
		return RANDOM.random();
	}

	public static float random(final float theMax) {
		return RANDOM.random(theMax);
	}

	public static double random(final double theMax) {
		return RANDOM.random(theMax);
	}

	public static float random(final float theMin, final float theMax) {
		return RANDOM.random(theMin, theMax);
	}

	public static double random(final double theMin, final double theMax) {
		return RANDOM.random(theMin, theMax);
	}

	public static float gaussianRandom() {
		return RANDOM.gaussianRandom();
	}
	
	public static float gaussianRandom(final float theMax) {
		return RANDOM.gaussianRandom(theMax);
	}
	
	public static float gaussianRandom(final float theMin, final float theMax) {
		return RANDOM.gaussianRandom(theMin, theMax);
	}
	
	public static void randomSeed(final long theSeed) {
		RANDOM.randomSeed(theSeed);
	}

	public static void noiseDetail(final int theOctaves) {
		NOISE.noiseDetail(theOctaves);
	}

	public static void noiseDetail(final int theOctaves, final float theFallOff) {
		NOISE.noiseDetail(theOctaves, theFallOff);
	}

	public static float noise(final float theX) {
		return NOISE.noise(theX);
	}

	public static float noise(final float theX, final float theY) {
		return NOISE.noise(theX, theY);
	}

	public static float noise(final CCVector2f theVector) {
		return NOISE.noise(theVector.x(), theVector.y());
	}

	public static float noise(final float theX, final float theY,final float theZ) {
		return NOISE.noise(theX, theY, theZ);
	}

	public static float noise(final CCVector3f theVector) {
		return NOISE.noise(theVector.x(), theVector.y(), theVector.z());
	}

	/**
	 * returns the bitwise representation of a floating point number
	 */
	static public int floatToBits(float theValue) {
		return Float.floatToIntBits(theValue);
	}

	/**
	 * returns the absolute value of a floating point number in bitwise form
	 */
	static public int floatToAbsluteBits(float theValue) {
		return (floatToBits(theValue) & 0x7FFFFFFF);
	}

	/**
	 * returns the signal bit of a floating point number
	 */
	static public int signalBit(float theValue) {
		return floatToBits(theValue) & 0x80000000;
	};

	/**
	 * returns the value of 1.0f in bitwise form
	 */
	static public int oneInBits() {
		return 0x3F800000;
	}

	/**
	 *  Convert's an angle in radians to one in degrees.
	 *
	 *  @param  rad  The angle in radians to be converted.
	 *  @return The angle in degrees.
	 **/
	public static final float radiansToDegrees(float rad) {
		return rad * 180 / PI;
	}

	/**
	 *  Convert's an angle in degrees to one in radians.
	 *
	 *  @param  deg  The angle in degrees to be converted.
	 *  @return The angle in radians.
	 **/
	public static final float degreesToRadians(float deg) {
		return deg * PI / 180;
	}

	public static final float mag(float abc[]) {
		return (float) Math.sqrt(abc[0] * abc[0] + abc[1] * abc[1] + abc[2]
				* abc[2]);
	}

	//////////////////////////////////////////////////////////////

	// MATH

	// lots of convenience methods for math with floats.
	
	public static final int abs(int x) {
		int y = x >> 31;
		return (x ^ y) - y;
	}

	public static final float abs(float x) {
		return x < 0 ? -x : x;
	}

	public static final double abs(double x) {
		return x < 0 ? -x : x;
	}

	static public final float sq(float a) {
		return a * a;
	}

	static public final double sq(double a) {
		return a * a;
	}

	static public final float sqrt(float a) {
		return (float) Math.sqrt(a);
	}

	static public final double sqrt(double a) {
		return Math.sqrt(a);
	}
	
	/**
	 * Returns 1/sqrt(fValue)
	 * 
	 * @param theValue The value to process.
	 * @return 1/sqrt(fValue)
	 * @see java.lang.Math#sqrt(double)
	 */
	public static float invSqrt(final float theValue) {
		return 1.0f / sqrt(theValue);
	}

	static public final float log(float a) {
		return (float) Math.log(a);
	}
	
	static public final float log10(float a){
		return (float) Math.log10(a);
	}

	static public final float exp(float a) {
		return (float) Math.exp(a);
	}

	static public final float pow(float a, float b) {
		return (float) Math.pow(a, b);
	}

	static public final double pow(double a, double b) {
		return Math.pow(a, b);
	}

	static public final int pow(int a, int b) {
		return (int)Math.pow(a, b);
	}

	static public final float max(float a, float b) {
		return Math.max(a, b);
//		return (a > b) ? a : b;
	}

	static public final double max(double a, double b) {
		//return Math.max(a, b);
		return (a > b) ? a : b;
	}

	static public final float max(float a, float b, float c) {
		//return Math.max(a, Math.max(b, c));
		return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
	}

	static public final double max(double a, double b, double c) {
		//return Math.max(a, Math.max(b, c));
		return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
	}
	
	static public final float max(float...theValues) {
		float result = Float.MIN_VALUE;
		for(float myValue:theValues) {
			result = max(result,myValue);
		}
		return result;
	}

	static public final float min(float a, float b) {
//		return Math.min(a, b);
		return (a < b) ? a : b;
	}

	static public final double min(double a, double b) {
		//return Math.min(a, b);
		return (a < b) ? a : b;
	}

	static public final float min(float a, float b, float c) {
		//return Math.min(a, Math.min(b, c));
		return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
	}
	/**
	 * constrains a value to a range between two floats.
	 * @param value
	 * @param theMin minimum output value
	 * @param theMax maximum output value
	 * @return
	 */
	static public float clamp(float value, float theMin, float theMax){
		return CCMath.max(theMin,CCMath.min(value,theMax));
	}
	static public final float min(float...theValues) {
		float result = Float.MAX_VALUE;
		for(float myValue:theValues) {
			result = min(result,myValue);
		}
		return result;
	}

	/**
	 * Blends between a start and an end value according to the given blend.
	 * The blend parameter is the amount to interpolate between the two values 
	 * where 0.0 equal to the first point, 0.1 is very near the first point, 
	 * 0.5 is half-way in between, etc. The blend function is convenient for 
	 * creating motion along a straight path and for drawing dotted lines.
	 * 
	 * @param theStart first value
	 * @param theStop second value
	 * @param theBlend between 0.0 and 1.0
	 * @return
	 */
	static public final float blend(final float theStart, final float theStop, final float theBlend) {
		return theStart + (theStop - theStart) * theBlend;
	}
	
	static public final double blend(final double theStart, final double theStop, final double theBlend) {
		return theStart + (theStop - theStart) * theBlend;
	}

	/**
	 * <p>Normalizes a number from another range into a value between 0 and 1.</p>
	 * <p>Identical to map(value, low, high, 0, 1);</p>
	 * <p>Numbers outside the range are not clamped to 0 and 1, because out-of-range 
	 * values are often intentional and useful.</p>
	 * 
	 * @param theValue The incoming value to be converted
	 * @param theMin Lower bound of the value's current range
	 * @param theMax Upper bound of the value's current range
	 * @return
	 */
	static public final float norm(final float theValue, final float theMin, final float theMax) {
		return (theValue - theMin) / (theMax - theMin);
	}
	
	static public final double norm(final double theValue, final double theMin, final double theMax) {
		return (theValue - theMin) / (theMax - theMin);
	}

	/**
	 * <p>
	 * Re-maps a number from one range to another. In the example above, the number '25' 
	 * is converted from a value in the range 0..100 into a value that ranges from the 
	 * left edge (0) to the right edge (width) of the screen.</p>
	 * <p>
	 * Numbers outside the range are not clamped to 0 and 1, because out-of-range values 
	 * are often intentional and useful.</p>
	 * @param theValue The incoming value to be converted
	 * @param theMinSrc Lower bound of the value's current range
	 * @param theMaxSrc Upper bound of the value's current range
	 * @param theMinDst Lower bound of the value's target range
	 * @param theMaxDst Upper bound of the value's target range
	 * @return
	 */
	static public float map(final float theValue, final float theMinSrc, final float theMaxSrc, final float theMinDst, final float theMaxDst) {
		return blend(theMinDst, theMaxDst, norm(theValue, theMinSrc, theMaxSrc));
	}

	static public double map(final double theValue, final double theMinSrc, final double theMaxSrc, final double theMinDst, final double theMaxDst) {
		return blend(theMinDst, theMaxDst, norm(theValue, theMinSrc, theMaxSrc));
	}
	
	/**
	 * For values of x between min and max, returns a smoothly varying value 
	 * that ranges from 0 at x = min to 1 at x = max. x is clamped to the 
	 * range [min, max] and then the interpolation formula is evaluated:
	 * -2*((x-min)/(max-min))3 + 3*((x-min)/(max-min))2
	 * @param theA
	 * @param theB
	 * @param theValue
	 * @return
	 */
	static public final float smoothStep(final float theMin, final float theMax, final float theValue) {
		if (theValue <= theMin)
			return 0;
		if (theValue >= theMax)
			return 1;
		
		return 3 * pow((theValue-theMin)/(theMax-theMin), 2) - 2 * pow((theValue-theMin)/(theMax-theMin), 3);
	}

	/**
	 * Constrains a value to not exceed a maximum and minimum value.
	 * @param theValue the value to constrain
	 * @param theMin minimum limit
	 * @param theMax maximum limit
	 * @return the constrained value
	 */
	static public final float constrain(final float theValue, final float theMin, final float theMax) {
		return (theValue < theMin) ? theMin : ((theValue > theMax) ? theMax : theValue);
	}

	/**
	 * Constrains a value to not exceed a maximum and minimum value.
	 * @param theValue the value to constrain
	 * @param theMin minimum limit
	 * @param theMax maximum limit
	 * @return the constrained value
	 */
	static public final double constrain(final double theValue, final double theMin, final double theMax) {
		return (theValue < theMin) ? theMin : ((theValue > theMax) ? theMax : theValue);
	}

	/**
	 * Constrains a value to not exceed a maximum and minimum value.
	 * @param theValue the value to constrain
	 * @param theMin minimum limit
	 * @param theMax maximum limit
	 * @return the constrained value
	 */
	static public final int constrain(final int theValue, final int theMinimum, final int theMaximum) {
		return (theValue < theMinimum) ? theMinimum : ((theValue > theMaximum) ? theMaximum : theValue);
	}
	
	/**
	 * Constrains the given value to be between 0 and 1
	 * @param theValue the value to constrain
	 * @return the saturated value
	 */
	public static double saturate(double theValue) {
		return constrain(theValue, 0, 1);
	}
	
	/**
	 * Constrains the given value to be between 0 and 1
	 * @param theValue the value to constrain
	 * @return the saturated value
	 */
	public static float saturate(float theValue) {
		return constrain(theValue, 0, 1);
	}

	static public final int max(int a, int b) {
		return (a > b) ? a : b;
	}

	static public final int max(int a, int b, int c) {
		return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
	}

	static public final int min(int a, int b) {
		return (a < b) ? a : b;
	}

	static public final float min(int...theValues) {
		float result = Integer.MAX_VALUE;
		for(float myValue:theValues) {
			result = min(result,myValue);
		}
		return result;
	}

	static public final int min(int a, int b, int c) {
		return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
	}

	public final static float sin(float angle) {
		return (float) Math.sin(angle);
	}

	public final static double sin(double angle) {
		return Math.sin(angle);
	}

	public final static float cos(float angle) {
		return (float) Math.cos(angle);
	}

	public final static double cos(double angle) {
		return Math.cos(angle);
	}

	public static final float tan(float angle) {
		return (float) Math.tan(angle);
	}

	public static final float asin(float value) {
		return (float) Math.asin(value);
	}

	public static final double asin(double value) {
		return Math.asin(value);
	}

	public static final float acos(float value) {
		return (float) Math.acos(value);
	}

	public static final double acos(double value) {
		return Math.acos(value);
	}

	public static final float atan(float value) {
		return (float) Math.atan(value);
	}

	public static final double atan(double value) {
		return Math.atan(value);
	}

	public static final float atan2(float a, float b) {
		return (float) Math.atan2(a, b);
	}

	public static final double atan2(double a, double b) {
		return Math.atan2(a, b);
	}

	static public final float degrees(float radians) {
		return radians * RAD_TO_DEG;
	}

	static public final double degrees(double radians) {
		return radians * RAD_TO_DEG;
	}

	static public final float radians(float degrees) {
		return degrees * CCMath.DEG_TO_RAD;
	}

	static public final double radians(double degrees) {
		return degrees * CCMath.DEG_TO_RAD;
	}

	static public final int ceil(float what) {
		return (int)Math.ceil(what);
	}

	static public final int ceil(double what) {
		return (int)Math.ceil(what);
	}

	static public final int floor(float what) {
		return (int)Math.floor(what);
	}

	static public final int floor(double what) {
		return (int)Math.floor(what);
	}

	static public final int round(float what) {
		return (int) Math.round(what);
	}

	static public final int round(double what) {
		return (int) Math.round(what);
	}

	/**
	 * Round a double value to a specified number of decimal places. 
	 * @param val the value to be rounded.
	 * @param places the number of decimal places to round to.
	 * @return val rounded to places decimal places.
	 */
	public static double round(double val, int places) {
		long factor = (long) Math.pow(10, places);

		// Shift the decimal the correct number of places
		// to the right.
		val = val * factor;

		// Round to the nearest integer.
		long tmp = Math.round(val);

		// Shift the decimal the correct number of places
		// back to the left.
		return (double) tmp / factor;
	}

	/**
	 * Round a float value to a specified number of decimal places.
	 * @param val the value to be rounded.
	 * @param places the number of decimal places to round to.
	 * @return val rounded to places decimal places.
	 */
	public static float round(float val, int places) {
		return (float) round((double) val, places);
	}

	static public final float mag(float a, float b) {
		return (float) Math.sqrt(a * a + b * b);
	}

	static public final float mag(float a, float b, float c) {
		return (float) Math.sqrt(a * a + b * b + c * c);
	}

	static public final float dist(float x1, float y1, float x2, float y2) {
		return sqrt(sq(x2 - x1) + sq(y2 - y1));
	}

	static public final float dist(float x1, float y1, float z1, float x2,
			float y2, float z2) {
		return sqrt(sq(x2 - x1) + sq(y2 - y1) + sq(z2 - z1));
	}

	/**
	 * Returns the sign of the given number so 1 if the value
	 * is bigger or equal than zero otherwise -1;
	 * @param theValue value to check for the sign
	 * @return sign of the given value
	 */
	static public final byte sign(final float theValue) {
		if (theValue < 0)
			return -1;
		return 1;
	}

	/**
	 * Returns the sign of the given number so 1 if the value
	 * is bigger or equal than zero otherwise -1;
	 * @param theValue value to check for the sign
	 * @return sign of the given value
	 */
	static public final byte sign(final double theValue) {
		if (theValue < 0)
			return -1;
		return 1;
	}
	
	static public boolean sameSign(final float theValue1, final float theValue2){
		return sign(theValue1) == sign(theValue2);
	}

	/**
	 * @param theLastX
	 * @param theF
	 * @param theF2
	 * @param theF3
	 * @param theT
	 * @return
	 */
	static public float bezierPoint(float a, float b, float c, float d, float t) {
		float t1 = 1.0f - t;
		return a * t1 * t1 * t1 + 3 * b * t * t1 * t1 + 3 * c * t * t * t1 + d * t * t * t;
	}
	

	
	//////////////////////////////////////////////////////////////////
	//
	// function to shape value ranges between 0 and 1 or do easing
	//
	//////////////////////////////////////////////////////////////////
	
	/**
	 * Use this function to shape a linear increase from 0 to 1 to a curved one.
	 * Higher exponents result in steeper curves.
	 * @param theValue the value to shape
	 * @param theExponent the exponent for shaping the output
	 * @return the shaped value
	 */
	public static float shapeExponential(final float theValue, final float theExponent) {
		return shapeExponential(theValue, 0.5f, theExponent);
	}
	
	/**
	 * Use this function to shape a linear increase from 0 to 1 to a curved one.
	 * Higher exponents result in steeper curves.
	 * @param theValue the value to shape
	 * @param theExponent the exponent for shaping the output
	 * @return the shaped value
	 */
	public static float shapeExponential(final float theValue, final float theBreakPoint, final float theExponent) {
		if(theValue < 0) return 0;
		if(theValue > 1) return 1;
		
		if(theValue < 0.5f) {
			return theBreakPoint * CCMath.pow(2 * theValue,theExponent);
		}
		
		return (1 - (1 - theBreakPoint) * CCMath.pow(2 * (1 - theValue),theExponent)) ;
	}
	
	/**
	 * Use this function to shape a linear increase from 0 to 1 to a curved one.
	 * Higher exponents result in steeper curves.
	 * @param theValue the value to shape
	 * @param theExponent the exponent for shaping the output
	 * @return the shaped value
	 */
	public static double shapeExponential(final double theValue, final double theExponent) {
		return shapeExponential(theValue, 0.5f, theExponent);
	}
	
	/**
	 * Use this function to shape a linear increase from 0 to 1 to a curved one.
	 * Higher exponents result in steeper curves.
	 * @param theValue the value to shape
	 * @param theExponent the exponent for shaping the output
	 * @return the shaped value
	 */
	public static double shapeExponential(final double theValue, final double theBreakPoint, final double theExponent) {
		if(theValue < 0) return 0;
		if(theValue > 1) return 1;
		
		if(theValue < 0.5f) {
			return theBreakPoint * CCMath.pow(2 * theValue,theExponent);
		}
		
		return (1 - (1 - theBreakPoint) * CCMath.pow(2 * (1 - theValue),theExponent)) ;
	}

	/**
	 * Use this method to average a value. This is useful if you want to buffer
	 * value changes. The smaller the factor is the slower the value reacts to 
	 * changes. 
	 * @param theOldValue value you had so far
	 * @param theNewValue new value
	 * @param theFactor influence of the new value to the average value
	 * @return averaged value based on the two given values and the factor
	 */
	public static float average(float theOldValue, float theNewValue, float theFactor) {
		return theOldValue * (1f - theFactor) + theNewValue * theFactor;
	}

	/**
	 * Use this method to average a value. This is useful if you want to buffer
	 * value changes. The smaller the factor is the slower the value reacts to 
	 * changes. 
	 * @param theOldValue value you had so far
	 * @param theNewValue new value
	 * @param theFactor influence of the new value to the average value
	 * @return averaged value based on the two given values and the factor
	 */
	public static double average(double theOldValue, double theNewValue, double theFactor) {
		return theOldValue * (1f - theFactor) + theNewValue * theFactor;
	}
}

