// $Id: MicroDouble.java,v 1.2 2004/08/03 04:57:42 Dave Exp $
/*
 * Double.java
 * Copyright (C) 2003, 2004 David Clausen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Portions of this software are derived from FDLIBM, which contained the
 * following notice:
 *
 * ====================================================
 * Copyright (C) 1993 by Sun Microsystems, Inc. All rights reserved.
 *
 * Developed at SunSoft, a Sun Microsystems, Inc. business.
 * Permission to use, copy, modify, and distribute this
 * software is freely granted, provided that this notice 
 * is preserved.
 * ====================================================
 *
 * For mor information on FDLIBM see:
 * http://netlib.bell-labs.com/netlib/fdlibm/index.html
 *
 */
package net.dclausen.microfloat;

import java.util.Random;

/**
 * A software implementation of IEEE-754 double precision math which does not
 * rely on the <code>double</code> data type. 
 * This class overloads the <code>long</code> data type by storing 
 * <code>double</code> data in it.
 * See the 
 * <a href="package-summary.html#package_description">package description</a> 
 * for more information.
 * <p>
 * @author David Clausen
 * @see <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html">Double</a>
 * @see <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html">Math</a>
 * @see Float
 * @version $Revision: 1.2 $
 */
public class MicroDouble {
  
  /////////////////////////////////////////////////////////////////////////////
  // General-purpose constants
  /////////////////////////////////////////////////////////////////////////////

  /**
   * A constant holding the same value as <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#POSITIVE_INFINITY">Double.POSITIVE_INFINITY</a>
   */
  public  static final long POSITIVE_INFINITY = 0x7ff0000000000000L;
  
  /**
   * A constant holding the same value as <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#NEGATIVE_INFINITY">Double.NEGATIVE_INFINITY</a>
   */
  public  static final long NEGATIVE_INFINITY = 0xfff0000000000000L;

  /**
   * A constant holding the same value as <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#NaN">Double.NaN</a>
   */
  public  static final long NaN               = 0x7ff8000000000000L;
  
  /**
   * A constant holding the same value as <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#MAX_VALUE">Double.MAX_VALUE</a>
   */
  public  static final long MAX_VALUE         = 0x7fefffffffffffffL;
  
  /**
   * A constant holding the same value as <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#MIN_VALUE">Double.MIN_VALUE</a>
   */
  public  static final long MIN_VALUE         = 0x0000000000000001L; 
  
  /**
   * A constant holding the same value as <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#E">Math.E</a>
   */
  public  static final long E                 = 0x4005bf0a8b145769L;
  
  /**
   * A constant holding the same value as <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#PI">Math.PI</a>
   */
  public  static final long PI                = 0x400921fb54442d18L;

  // Other constants needed internally, and exposed as a convenience.

  /** A constant holding the value of 0.0d */
  public static final long ZERO              = 0x0000000000000000L;

  /** A constant holding the value of -0.0d */
  public static final long NEGATIVE_ZERO     = 0x8000000000000000L;
  
  /** A constant holding the value of 1.0d */
  public static final long ONE               = 0x3ff0000000000000L;
  
  /** A constant holding the value of -1.0d */
  public static final long NEGATIVE_ONE      = 0xbff0000000000000L;
  
  /** A constant holding the value of 2.0d */
  public static final long TWO               = 0x4000000000000000L;
  
  /** A constant holding the value of 3.0d */
  public static final long THREE             = 0x4008000000000000L;
  
  /** A constant holding the value of 4.0d */
  public static final long FOUR              = 0x4010000000000000L;
  
  /** A constant holding the value of 5.0d */
  public static final long FIVE              = 0x4014000000000000L;
  
  /** A constant holding the value of 6.0d */
  public static final long SIX               = 0x4018000000000000L;
  
  /** A constant holding the value of 8.0d */
  public static final long EIGHT             = 0x4020000000000000L;
  
  /** A constant holding the value of 10.0d */
  public static final long TEN               = 0x4024000000000000L;
  
  /** A constant holding the value of 100.0d */
  public static final long ONE_HUNDRED       = 0x4059000000000000L;
  
  /** A constant holding the value of 1.5d */
  public static final long THREE_HALVES      = 0x3ff8000000000000L;
  
  /** A constant holding the value of 0.5d */
  public static final long ONE_HALF          = 0x3fe0000000000000L;
  
  /** A constant holding the value of (1.0d / 3.0d) */
  public static final long ONE_THIRD         = 0x3fd5555555555555L;
  
  /** A constant holding the value of 0.25d */
  public static final long ONE_FOURTH        = 0x3fd0000000000000L;
  
  /** A constant holding the value of 0.125d */
  public static final long ONE_EIGHTH        = 0x3fc0000000000000L;
  
  /** A constant holding the natural logarithm of 2 */
  public static final long LN2               = 0x3fe62e42fefa39efL;

  
  /////////////////////////////////////////////////////////////////////////////
  // Packing and unpacking the IEEE-754 double precision format
  /////////////////////////////////////////////////////////////////////////////

  private static final long ABS_MASK          = 0x7fffffffffffffffL;
  private static final long SIGN_MASK         = 0x8000000000000000L; // 1 bit
  private static final long EXPONENT_MASK     = 0x7ff0000000000000L; // 11 bits
  private static final long FRACTION_MASK     = 0x000fffffffffffffL; // 52 bits
  private static final long IMPLIED_ONE       = 0x0010000000000000L; // 53rd bit

  /** @return true iff d is negative */
  static boolean unpackSign(long d) {
    return (d < 0L);
  }

  /** @return an integer in the range [-1075, 972] */
  static int unpackExponent(long d) {
    return (((int) (d >> 52)) & 0x7ff) - 1075;
  }

  /** @return a long in the range [0, 0x001fffffffffffffL] */
  static long unpackMantissa(long d) {
    if ((d & EXPONENT_MASK) == 0) {
      return ((d & FRACTION_MASK) << 1);
    } else {
      return ((d & FRACTION_MASK) | IMPLIED_ONE);
    }
  }

  /** 
   * @return the double which most closely represents the given base-2 mantissa
   *         and exponent
   */
  static long pack(boolean negative, int exponent, long mantissa) {
    // reduce precision of mantissa, rounding if necessary
    if (mantissa != 0) {
      // left align mantissa
      int shift = BitUtils.countLeadingZeros(mantissa);
      mantissa <<= shift;
      exponent -= shift;

      if (exponent < -1085) {
        // subnormal
        mantissa = BitUtils.roundingRightShift(mantissa, -1074 - exponent);
      } else {
        // normal
        mantissa = BitUtils.roundingRightShift(mantissa, 11);
        if (mantissa == 0x20000000000000L) {
          // oops, rounding carried into the 54th bit
          mantissa = 0x10000000000000L;
          exponent++;
        }
        // pack the exponent
        if (exponent > 960) {
          mantissa = POSITIVE_INFINITY;
        } else {
          mantissa ^= IMPLIED_ONE;
          mantissa |= ((long) (exponent + 1086)) << 52;
        }
      }
    }
    
    // pack the sign bit
    if (negative) {
      mantissa |= SIGN_MASK;
    }
    
    return mantissa;
  }

  
  /////////////////////////////////////////////////////////////////////////////
  // Simple tests 
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#isNaN(double)">Double.isNaN(double)</a>
   */
  public static boolean isNaN(long d) {
    return ((d & ABS_MASK) > POSITIVE_INFINITY);
  }

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#isInfinite(double)">Double.isInfinite(double)</a>
   */
  public static boolean isInfinite(long d) {
    return ((d & ABS_MASK) == POSITIVE_INFINITY);
  }
  
  /**
   * Returns <code>true</code> if the specified number has zero
   * magnitude, <code>false</code> otherwise.
   *
   * @param   d   the <code>double</code> value to be tested.
   * @return  <code>true</code> if the value of the argument is positive
   *          zero or negative zero; <code>false</code> otherwise.
   */
  public static boolean isZero(long d) {
    return ((d & ABS_MASK) == ZERO);
  }

  
  /////////////////////////////////////////////////////////////////////////////
  // Sign changes
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#abs(double)">Math.abs(double)</a>
   */
  public static long abs(long d) {
    //if (isNaN(d)) {
    //  return NaN;
    //}
    return (d & ABS_MASK);
  }

  /**
   * Returns the negation of a <code>double</code> value.
   * Special cases:
   * <ul>
   * <li>If the argument is negative zero, the result is positive zero.
   * <li>If the argument is positive zero, the result is negative zero.
   * <li>If the argument is negative infinity, the result is positive infinity.
   * <li>If the argument is positive infinity, the result is negative infinity.
   * <li>If the argument is NaN, the result is NaN.</ul>
   * <p>
   * This method takes the place of the unary <code>-</code> operator.
   *
   * @param   d   the <code>double</code> value whose negated value is to be 
   *              determined
   * @return  the negation of the argument.
   */
  public static long negate(long d) {
    if (isNaN(d)) {
      return NaN;
    }
    return (d ^ SIGN_MASK);
  }
  

  /////////////////////////////////////////////////////////////////////////////
  // Comparison
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Returns <code>true</code> if the specified numbers are considered equal
   * according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#5198">section 15.21.1
   * of the JLS</a>.  Special cases:
   * <ul>
   * <li>If either operand is NaN, then the result is false
   * <li>Positive zero and negative zero are considered equal
   * </ul>
   * <p>
   * This method takes the place of the <code>==</code> operator.
   *
   * @param   d1   the first <code>double</code> value to be compared.
   * @param   d2   the second <code>double</code> value to be compared.
   * @return  <code>true</code> if the two values are considered equal;
   *          <code>false</code> otherwise.
   */
  public static boolean eq(long d1, long d2) {
    return (((d1 == d2) && (! isNaN(d1))) || (isZero(d1) && isZero(d2)));
  }

  /**
   * Returns <code>true</code> if the specified numbers are considered unequal
   * according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#5198">section
   * 15.21.1 of the JLS</a>.  Special cases:
   * <ul>
   * <li>If either operand is NaN, then the result is true
   * <li>Positive zero and negative zero are considered equal
   * </ul>
   * The value returned by <code>ne</code> is always the opposite of the value
   * returned by <code>eq</code> for the same arguments.
   * <p>
   * This method takes the place of the <code>!=</code> operator.
   *
   * @param   d1   the first <code>double</code> value to be compared.
   * @param   d2   the second <code>double</code> value to be compared.
   * @return  <code>true</code> if the two values are considered equal;
   *          <code>false</code> otherwise.
   */
  public static boolean ne(long d1, long d2) {
    return (! eq(d1, d2));
  }

  /**
   * Returns <code>true</code> if the first argument is considered less than
   * the second argument according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#153654">section
   * 15.20.1 of the JLS</a>.  Special cases:
   * <ul>
   * <li>If either operand is NaN, then the result is false
   * <li>Positive zero and negative zero are considered equal
   * <li>Negative infinity is conisdered less than all other values except NaN
   * <li>Positive infinity is conisdered greater than all other values except NaN
   * </ul>
   * <p>
   * This method takes the place of the <code>&lt;</code> operator.
   *
   * @param   d1   the first <code>double</code> value to be compared.
   * @param   d2   the second <code>double</code> value to be compared.
   * @return  <code>true</code> if the first value is less than the second value;
   *          <code>false</code> otherwise.
   */
  public static boolean lt(long d1, long d2) {
    if (isNaN(d1) || isNaN(d2)) {
      return false;
    } else if (d2 == ZERO) {
      d2 = NEGATIVE_ZERO;
    }
    return (cmp(d1, d2) < 0);
  }
  
  /**
   * Returns <code>true</code> if the first argument is considered less than
   * or equal to the second argument according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#153654">section
   * 15.20.1 of the JLS</a>.  Special cases:
   * <ul>
   * <li>If either operand is NaN, then the result is false
   * <li>Positive zero and negative zero are considered equal
   * <li>Negative infinity is conisdered less than all other values except NaN
   * <li>Positive infinity is conisdered greater than all other values except NaN
   * </ul>
   * <p>
   * This method takes the place of the <code>&lt;=</code> operator.
   *
   * @param   d1   the first <code>double</code> value to be compared.
   * @param   d2   the second <code>double</code> value to be compared.
   * @return  <code>true</code> if the first value is less than or equal to 
   *          the second value; <code>false</code> otherwise.
   */
  public static boolean le(long d1, long d2) {
    if (isNaN(d1) || isNaN(d2)) {
      return false;
    } else if (d2 == NEGATIVE_ZERO) {
      d2 = ZERO;
    }
    return (cmp(d1, d2) <= 0);
  }

  /**
   * Returns <code>true</code> if the first argument is considered greater than
   * the second argument according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#153654">section
   * 15.20.1 of the JLS</a>.  Special cases:
   * <ul>
   * <li>If either operand is NaN, then the result is false
   * <li>Positive zero and negative zero are considered equal
   * <li>Negative infinity is conisdered less than all other values except NaN
   * <li>Positive infinity is conisdered greater than all other values except NaN
   * </ul>
   * <p>
   * This method takes the place of the <code>&gt;</code> operator.
   *
   * @param   d1   the first <code>double</code> value to be compared.
   * @param   d2   the second <code>double</code> value to be compared.
   * @return  <code>true</code> if the first value is greater than the second value;
   *          <code>false</code> otherwise.
   */
  public static boolean gt(long d1, long d2) {
    if (isNaN(d1) || isNaN(d2)) {
      return false;
    } else if (d1 == ZERO) {
      d1 = NEGATIVE_ZERO;
    }
    return (cmp(d1, d2) > 0);
  }
  
  /**
   * Returns <code>true</code> if the first argument is considered greater than
   * or equal to the second argument according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#153654">section
   * 15.20.1 of the JLS</a>.  Special cases:
   * <ul>
   * <li>If either operand is NaN, then the result is false
   * <li>Positive zero and negative zero are considered equal
   * <li>Negative infinity is conisdered less than all other values except NaN
   * <li>Positive infinity is conisdered greater than all other values except NaN
   * </ul>
   * <p>
   * This method takes the place of the <code>&gt;=</code> operator.
   *
   * @param   d1   the first <code>double</code> value to be compared.
   * @param   d2   the second <code>double</code> value to be compared.
   * @return  <code>true</code> if the first value is greater than or equal to 
   *          the second value; <code>false</code> otherwise.
   */
  public static boolean ge(long d1, long d2) {
    if (isNaN(d1) || isNaN(d2)) {
      return false;
    } else if (d1 == NEGATIVE_ZERO) {
      d1 = ZERO;
    }
    return (cmp(d1, d2) >= 0);
  }

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#compare(double, double)">Double.compare(double, double)</a>.
   * <p>
   * Note that when using this method (as well as <code>Double.compare</code>),
   * the following rules apply:
   * <ul><li>
   *		<code>NaN</code> is considered 
   *		to be equal to itself and greater than all other
   *		<code>double</code> values (including
   *		<code>POSITIVE_INFINITY</code>).
   * <li>
   *		<code>0.0</code> is considered to be greater
   *		than <code>-0.0</code>.
   * </ul>
   */
  public static int compare(long d1, long d2) {
    boolean n1 = isNaN(d1);
    boolean n2 = isNaN(d2);
    if (n1 || n2) {
      if (n1 && n2) {
        return 0;
      }
      return (n1 ? 1 : -1);
    }
    return cmp(d1, d2);
  }

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#max(double, double)">Math.max(double, double)</a>
   */
  public static long max(long d1, long d2) {
    if (isNaN(d1) || isNaN(d2)) {
      return NaN;
    }
    return ((cmp(d1, d2) >= 0) ? d1 : d2);
  }

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#min(double, double)">Math.min(double, double)</a>
   */
  public static long min(long d1, long d2) {
    if (isNaN(d1) || isNaN(d2)) {
      return NaN;
    }
    return ((cmp(d1, d2) < 0) ? d1 : d2);
  }

  private static int cmp(long d1, long d2) {
    if (d1 == d2) {
      return 0;
    } else if (d1 < 0L) {
      if (d2 < 0L) {
        return ((d1 < d2) ? 1 : -1);
      }
      return -1;
    } else if (d2 < 0) {
      return 1;
    }
    return ((d1 < d2) ? -1 : 1);
  }
  
  /////////////////////////////////////////////////////////////////////////////
  // Type conversion
  /////////////////////////////////////////////////////////////////////////////
  
  /** 
   * Convert the given <code>int</code> to a <code>double</code> as would happen
   * in a casting operation specified by 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25214">section
   * 5.1.2 of the JLS</a>.  This is a widening primitive conversion which 
   * will result in neither a loss of magnitude nor precision.
   *
   * @param x the <code>int</code> to be converted
   * @return the <code>double</code> representation of the argument
   */
  public static long intToDouble(int x) {
    return longToDouble(x);
  }
  
  /** 
   * Convert the given <code>long</code> to a <code>double</code> as would happen
   * in a casting operation specified by 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25214">section
   * 5.1.2 of the JLS</a>.  This is a widening primitive conversion which 
   * will not result in a loss of magnitude, but might result in a loss of
   * precision.
   *
   * @param x the <code>long</code> to be converted
   * @return the <code>double</code> representation of the argument
   */
  public static long longToDouble(long x) {
    if (x < 0) {
      return pack(true, 0, -x);
    }
    return pack(false, 0, x);
  }

  /** 
   * Convert the given <code>float</code> to a <code>double</code> as would happen
   * in a casting operation specified by 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25214">section
   * 5.1.2 of the JLS</a>.  This is a widening primitive conversion which 
   * will result in neither a loss of magnitude nor precision.
   *
   * @param f the <code>float</code> to be converted
   * @return the <code>double</code> representation of the argument
   */
  public static long floatToDouble(int f) {
    if (MicroFloat.isNaN(f)) {
      return NaN;
    }
    boolean n = MicroFloat.unpackSign(f);
    if (MicroFloat.isZero(f)) {
      return (n ? NEGATIVE_ZERO : ZERO);
    } else if (MicroFloat.isInfinite(f)) {
      return (n ? NEGATIVE_INFINITY : POSITIVE_INFINITY);
    }
    int x = MicroFloat.unpackExponent(f);
    long m = MicroFloat.unpackMantissa(f);
    return pack(n, x, m);
  }
  
  /** 
   * Convert the given <code>double</code> to a <code>byte</code> as would happen
   * in a casting operation specified by 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">section
   * 5.1.3 of the JLS</a>.  This is a narrowing primitive conversion which 
   * may result in a loss of magnitude and/or precision.
   * <p>
   * Note that this is a non-intuitive conversion.  If the argument is outside
   * of the range of the byte type, the result is basically meaningless.
   *
   * @param d the <code>double</code> to be converted
   * @return the <code>byte</code> representation of the argument
   */
  public static byte byteValue(long d) {
    return (byte) intValue(d);
  }

  /** 
   * Convert the given <code>double</code> to a <code>short</code> as would happen
   * in a casting operation specified by 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">section
   * 5.1.3 of the JLS</a>.  This is a narrowing primitive conversion which 
   * may result in a loss of magnitude and/or precision.
   * <p>
   * Note that this is a non-intuitive conversion.  If the argument is outside
   * of the range of the short type, the result is basically meaningless.
   *
   * @param d the <code>double</code> to be converted
   * @return the <code>short</code> representation of the argument
   */
  public static short shortValue(long d) {
    return (short) intValue(d);
  }

  /** 
   * Convert the given <code>double</code> to an <code>int</code> as would happen
   * in a casting operation specified by 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">section
   * 5.1.3 of the JLS</a>.  This is a narrowing primitive conversion which 
   * may result in a loss of magnitude and/or precision.
   *
   * @param d the <code>double</code> to be converted
   * @return the <code>int</code> representation of the argument
   */
  public static int intValue(long d) {
    long x = longValue(d);
    if (x >= Integer.MAX_VALUE) {
      return Integer.MAX_VALUE;
    } else if (x <= Integer.MIN_VALUE) {
      return Integer.MIN_VALUE;
    }
    return (int) x;
  }

  /** 
   * Convert the given <code>double</code> to a <code>long</code> as would happen
   * in a casting operation specified by 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">section
   * 5.1.3 of the JLS</a>.  This is a narrowing primitive conversion which 
   * may result in a loss of magnitude and/or precision.
   *
   * @param d the <code>double</code> to be converted
   * @return the <code>long</code> representation of the argument
   */
  public static long longValue(long d) {
    if (isNaN(d)) {
      return 0;
    }
    boolean n = unpackSign(d);
    int x = unpackExponent(d);
    long m = unpackMantissa(d);
    if (x > 0) {
      if ((x >= 63) || ((m >> (63 - x)) != 0))  {
        return (n ? Long.MIN_VALUE : Long.MAX_VALUE);
      }
      m <<= x;
    } else if (x <= -53) {
      return 0;
    } else {
      m >>>= -x;
    }
    return (n ? -m : m);
  }

  /** 
   * Convert the given <code>double</code> to a <code>float</code> as would happen
   * in a casting operation specified by 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">section
   * 5.1.3 of the JLS</a>.  This is a narrowing primitive conversion which 
   * may result in a loss of magnitude and/or precision.
   *
   * @param d the <code>double</code> to be converted
   * @return the <code>float</code> representation of the argument
   */
  public static int floatValue(long d) {
    return MicroFloat.doubleToFloat(d);
  }
  
  /////////////////////////////////////////////////////////////////////////////
  // Random number generation
  /////////////////////////////////////////////////////////////////////////////

  private static Random random;

  private static synchronized Random getRandom() {
    if (random == null) {
      random = new java.util.Random();
    }
    return random;
  }
  
  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#random()">Math.random()</a>
   */
  public static long random() {
    return pack(false, -64, getRandom().nextLong() << 11);
  }

  
  /////////////////////////////////////////////////////////////////////////////
  // Basic arithmetic
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Returns the sum of the two <code>double</code> arguments according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#13510">section
   * 15.18.2 of the JLS</a>.
   * <p>
   * This method takes the place of the <code>+</code> operator.
   *
   * @param   d1   the first <code>double</code> value to be summed.
   * @param   d2   the second <code>double</code> value to be summed.
   * @return  the sum of the two arguments
   */
  public static long add(long d1, long d2) {
    if (isNaN(d1) || isNaN(d2)) {
      return NaN;
    }

    boolean n1 = unpackSign(d1);
    boolean n2 = unpackSign(d2);
    
    // special handling of infinity
    boolean i1 = isInfinite(d1);
    boolean i2 = isInfinite(d2);
    if (i1 || i2) {
      if (i1 && i2) {
        if (n1 != n2) {
          // infinites of opposite sign -> NaN
          return NaN;
        } else {
          // infinites of same sign -> infinity the same sign
          return d1;
        }
      } else if (i1) {
        return d1; // infinite + finite = infinite
      } else {
        return d2; // finite + infinite = infinite
      }
    }
    
    // special handling of zero
    boolean z1 = isZero(d1);
    boolean z2 = isZero(d2);
    if (z1 || z2) {
      if (z1 && z2) {
        if (n1 != n2) {
          // zeros of opposite sign -> positive zero
          return ZERO;
        } else {
          return d1; // zeros of same sign -> zero of the same sign
        }
      } else if (z1) {
        return d2; // zero + nonzero = nonzero
      } else {
        return d1; // nonzero + zero = nonzero
      }
    }
    
    // unpack, and add 3 guard digits
    long m1 = unpackMantissa(d1) << 3;
    int x1 = unpackExponent(d1) - 3;
    long m2 = unpackMantissa(d2) << 3;
    int x2 = unpackExponent(d2) - 3;
    
    // make exponents equal
    int dx = x1 - x2;
    if (dx > 0) {
      m2 = BitUtils.stickyRightShift(m2, dx);
      x2 = x1;
    } else if (dx < 0) {
      m1 = BitUtils.stickyRightShift(m1, -dx);
      x1 = x2;
    }

    // if the signs are different, negate the smaller mantissa and choose
    // the sign of the larger
    if (n1 ^ n2) { 
      if (m1 > m2) {
        m2 = -m2;
      } else {
        m1 = -m1;
        n1 = n2;
      }
    }
    
    // add (or subtract) mantissas
    m1 += m2;

    // pack result, and handle special case of zero (which always returns +0.0) 
    long d = pack(n1, x1, m1);
    if (d == NEGATIVE_ZERO) {
      return ZERO;
    }
    return d;
  }
  
  /**
   * Returns the difference of the two <code>double</code> arguments according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#13510">section
   * 15.18.2 of the JLS</a>.
   * <p>
   * This method takes the place of the binary <code>-</code> operator.
   *
   * @param   d1   the first <code>double</code> value 
   * @param   d2   the second <code>double</code> value
   * @return  the difference of the two arguments
   */
  public static long sub(long d1, long d2) {
    return add(d1, negate(d2));
  }

  /**
   * Returns the product of the two <code>double</code> arguments according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#5036">section
   * 15.17.1 of the JLS</a>.
   * <p>
   * This method takes the place of the <code>*</code> operator.
   *
   * @param   d1   the first <code>double</code> value
   * @param   d2   the second <code>double</code> value
   * @return  the product of the two arguments
   */
  public static long mul(long d1, long d2) {
    if (isNaN(d1) || isNaN(d2)) {
      return NaN;
    }

    boolean negative = unpackSign(d1) ^ unpackSign(d2);
    
    // special handling of infinity
    if (isInfinite(d1) || isInfinite(d2)) {
      if (isZero(d1) || isZero(d2)) {
        return NaN;
      } else {
        return (negative ? NEGATIVE_INFINITY : POSITIVE_INFINITY);
      }
    }
    
    // unpack
    long m1 = unpackMantissa(d1);
    int x1 = unpackExponent(d1);
    long m2 = unpackMantissa(d2);
    int x2 = unpackExponent(d2);
    
    // compute the resultant exponent
    x1 += x2;
    
    // compute the resultant mantissa using double-precision integer 
    // multiplication with 28 bit words
    long m11 = m1 & 0x0fffffff;
    long m12 = m1 >> 28;
    long m21 = m2 & 0x0fffffff;
    long m22 = m2 >> 28;
    
    long t1 = m11 * m21;
    long t2 = (m11 * m22) + (m12 * m21);
    long t3 = m12 * m22;
    
    t1 += (t2 & 0x0fffffff) << 28;
    t3 += t2 >>> 28;
    t3 += t1 >>> 56;
    t1 <<= 8;
    // the 128 bit result is now in t3t1

    if (t3 == 0) {
      // the high 64 bits are zero and can be ignored.
      return pack(negative, x1, t1);
    }
    
    // shift the result left into t3 and discard excess precision
    int s = BitUtils.countLeadingZeros(t3);
    x1 += 56 - s;
    t3 <<= s;
    t3 |= t1 >>> (64 - s);
    if ((t1 << s) != 0) {
      // discarded low bits go into the sticky bit
      t3 |= 1;
    }
    
    // round and pack the result
    return pack(negative, x1, t3);
  }
  
  /**
   * Returns the quotient of the two <code>double</code> arguments according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#5047">section
   * 15.17.2 of the JLS</a>.
   * <p>
   * This method takes the place of the <code>/</code> operator.
   *
   * @param   d1   the <code>double</code> dividend 
   * @param   d2   the <code>double</code> divisor
   * @return  the quotient of the two arguments
   */
  public static long div(long d1, long d2) {
    if (isNaN(d1) || isNaN(d2)) {
      return NaN;
    }

    boolean negative = unpackSign(d1) ^ unpackSign(d2);
    
    // special handling of infinity
    boolean n1 = isInfinite(d1);
    boolean n2 = isInfinite(d2);
    if (n1 || n2) {
      if (n1 && n2) {
        return NaN;
      } else if (n1) {
        return (negative ? NEGATIVE_INFINITY : POSITIVE_INFINITY);
      } else {
        return (negative ? NEGATIVE_ZERO : ZERO);
      }
    }
    // neither value is infinite
    
    // special handling of zero
    n1 = isZero(d1);
    n2 = isZero(d2);
    if (n1 || n2) {
      if (n1 && n2) {
        return NaN;
      } else if (n1) {
        return (negative ? NEGATIVE_ZERO : ZERO);
      } else {
        return (negative ? NEGATIVE_INFINITY : POSITIVE_INFINITY);
      }
    }
    // neither value is zero
    
    // unpack
    long m1 = unpackMantissa(d1);
    int x1 = unpackExponent(d1);
    long m2 = unpackMantissa(d2);
    int x2 = unpackExponent(d2);

    // shift, divide, mod, repeat
    long m = 0;
    x1 -= x2;
    while (true) {
      int s = Math.min(BitUtils.countLeadingZeros(m1) - 1, 
              BitUtils.countLeadingZeros(m));
      if (s <= 8) {
        if (m1 != 0) {
          m |= 1;
        }
        break;
      }
      m1 <<= s;
      m <<= s;
      x1 -= s;
      m |= m1 / m2;
      m1 %= m2;
    }
    return pack(negative, x1, m);
  }
  
  /**
   * Returns the remainder of the two <code>double</code> arguments according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#24956">section
   * 15.17.3 of the JLS</a>.
   * <p>
   * This method takes the place of the <code>%</code> operator.
   *
   * @param   d1   the <code>double</code> dividend 
   * @param   d2   the <code>double</code> divisor
   * @return  the remainder of the two arguments
   * @see #IEEEremainder(long, long)
   */
  public static long mod(long d1, long d2) {
    if (isNaN(d1) || isNaN(d2) || isInfinite(d1) || isZero(d2)) {
      return NaN;
    } else if (isZero(d1) || isInfinite(d2)) {
      return d1;
    }
    
    // unpack
    int x1 = unpackExponent(d1);
    int x2 = unpackExponent(d2);
    if (x1 < x2) {
      return d1;
    }
    boolean n = unpackSign(d1);
    long m1 = unpackMantissa(d1);
    long m2 = unpackMantissa(d2);
    if (x1 == x2) {
      m1 %= m2;
    } else {
      // reduce m1 by left shifting and modding until the exponents x1 and x2 are 
      // equal
      while (x1 != x2) {
        int s = Math.min(BitUtils.countLeadingZeros(m1) - 1, x1 - x2);
        x1 -= s;
        m1 = (m1 << s) % m2;
      }
    }
    return pack(n, x1, m1);
  }

  
  /////////////////////////////////////////////////////////////////////////////
  // Rounding
  /////////////////////////////////////////////////////////////////////////////

  
  /**
   * Returns the <code>double</code> of greatest magnitude (furthest from zero)
   * that is equal to a mathematical integer and which has a mignitude not
   * greater than the argument's magnitude.  Special cases:
   * <ul><li>If the argument value is already equal to a mathematical 
   * integer, then the result is the same as the argument. 
   * <li>If the argument is NaN or an infinity or positive zero or 
   * negative zero, then the result is the same as the argument.</ul>
   *
   * @param   d   a <code>double</code> value.
   * @return the <code>double</code> of greatest magnitude (furthest from zero)
   *         whose magnitude is not greater than the argument's and which 
   *         is equal to a mathematical integer.
   */
  public static long truncate(long d) {
    return round(d, false, unpackSign(d));
  }

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#rint(double)">Math.rint(double)</a>.
   */
  public static long rint(long d) {
    return round(d, true, false);
  }
  
  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#floor(double)">Math.floor(double)</a>.
   */
  public static long floor(long d) {
    return round(d, false, false);
  }
  
  /**
   * Mimcs <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#ceil(double)">Math.ceil(double)</a>.
   */
  public static long ceil(long d) {
    return round(d, false, true);
  }

  /**
   * Mimcs <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#round(double)">Math.round(double)</a>.
   */
  public static long round(long d) {
    return longValue(floor(add(d, ONE_HALF)));
  }

  private static long round(long d, boolean round, boolean ceil) {
    if (isNaN(d)) {
      return NaN;
    } else if (isZero(d) || isInfinite(d)) {
      return d;
    }
    int x = unpackExponent(d);
    if (x >= 0) {
      return d;
    }
    boolean n = unpackSign(d);
    long m = unpackMantissa(d);
    if (round) {
      m = BitUtils.roundingRightShift(m, -x);
    } else {
      long r;
      if (x <= -64) {
        r = m;
        m = 0;
      } else {
        r = m << (64 + x);
        m >>>= -x;
      }
      if ((n ^ ceil) && (r != 0)) {
        m++;
      }
    }
    return pack(n, 0, m);
  }

  
  /////////////////////////////////////////////////////////////////////////////
  // String Conversion
  /////////////////////////////////////////////////////////////////////////////
  
  // decimal -> binary 
  
  // base 2 mantissas for 10**-345 through 10**309, at intervals of 1000
  private static final long[] pow10m = {
          0xf4b0769e47eb5a79L, 0xeef453d6923bd65aL, 0xe95a99df8ace6f54L, 
          0xe3e27a444d8d98b8L, 0xde8b2b66b3bc4724L, 0xd953e8624b85dd79L, 
          0xd43bf0effdc0ba48L, 0xcf42894a5dce35eaL, 0xca66fa129f9b60a7L, 
          0xc5a890362fddbc63L, 0xc1069cd4eabe89f9L, 0xbc807527ed3e12bdL, 
          0xb8157268fdae9e4cL, 0xb3c4f1ba87bc8697L, 0xaf8e5410288e1b6fL, 
          0xab70fe17c79ac6caL, 0xa76c582338ed2622L, 0xa37fce126597973dL, 
          0x9faacf3df73609b1L, 0x9becce62836ac577L, 0x9845418c345644d7L, 
          0x94b3a202eb1c3f39L, 0x91376c36d99995beL, 0x8dd01fad907ffc3cL, 
          0x8a7d3eef7f1cfc52L, 0x873e4f75e2224e68L, 0x8412d9991ed58092L, 
          0x80fa687f881c7f8eL, 0xfbe9141915d7a922L, 0xf6019da07f549b2bL, 
          0xf03d93eebc589f88L, 0xea9c227723ee8bcbL, 0xe51c79a85916f485L, 
          0xdfbdcece67006ac9L, 0xda7f5bf590966849L, 0xd5605fcdcf32e1d7L, 
          0xd0601d8efc57b08cL, 0xcb7ddcdda26da269L, 0xc6b8e9b0709f109aL, 
          0xc21094364dfb5637L, 0xbd8430bd08277231L, 0xb913179899f68584L, 
          0xb4bca50b065abe63L, 0xb080392cc4349dedL, 0xac5d37d5b79b6239L, 
          0xa8530886b54dbdecL, 0xa46116538d0deb78L, 0xa086cfcd97bf97f4L, 
          0x9cc3a6eec6311a64L, 0x991711052d8bf3c5L, 0x9580869f0e7aac0fL, 
          0x91ff83775423cc06L, 0x8e938662882af53eL, 0x8b3c113c38f9f37fL, 
          0x87f8a8d4cfa417caL, 0x84c8d4dfd2c63f3bL, 0x81ac1fe293d599c0L, 
          0xfd442e4688bd304bL, 0xf7549530e188c129L, 0xf18899b1bc3f8ca2L, 
          0xebdf661791d60f56L, 0xe65829b3046b0afaL, 0xe0f218b8d25088b8L, 
          0xdbac6c247d62a584L, 0xd686619ba27255a3L, 0xd17f3b51fca3a7a1L, 
          0xcc963fee10b7d1b3L, 0xc7caba6e7c5382c9L, 0xc31bfa0fe5698db8L, 
          0xbe89523386091466L, 0xba121a4650e4ddecL, 0xb5b5ada8aaff80b8L, 
          0xb1736b96b6fd83b4L, 0xad4ab7112eb3929eL, 0xa93af6c6c79b5d2eL, 
          0xa54394fe1eedb8ffL, 0xa163ff802a3426a9L, 0x9d9ba7832936edc1L, 
          0x99ea0196163fa42eL, 0x964e858c91ba2655L, 0x92c8ae6b464fc96fL, 
          0x8f57fa54c2a9eab7L, 0x8bfbea76c619ef36L, 0x88b402f7fd75539bL, 
          0x857fcae62d8493a5L, 0x825ecc24c8737830L, 0xfea126b7d78186bdL, 
          0xf8a95fcf88747d94L, 0xf2d56790ab41c2a3L, 0xed246723473e3813L, 
          0xe7958cb87392c2c3L, 0xe2280b6c20dd5232L, 0xdcdb1b2798182245L, 
          0xd7adf884aa879177L, 0xd29fe4b18e88640fL, 0xcdb02555653131b6L, 
          0xc8de047564d20a8cL, 0xc428d05aa4751e4dL, 0xbf8fdb78849a5f97L, 
          0xbb127c53b17ec159L, 0xb6b00d69bb55c8d1L, 0xb267ed1940f1c61cL, 
          0xae397d8aa96c1b78L, 0xaa242499697392d3L, 0xa6274bbdd0fadd62L, 
          0xa2425ff75e14fc32L, 0x9e74d1b791e07e48L, 0x9abe14cd44753b53L, 
          0x971da05074da7befL, 0x9392ee8e921d5d07L, 0x901d7cf73ab0acd9L, 
          0x8cbccc096f5088ccL, 0x89705f4136b4a597L, 0x8637bd05af6c69b6L, 
          0x83126e978d4fdf3bL, 0x8000000000000000L, 0xfa00000000000000L, 
          0xf424000000000000L, 0xee6b280000000000L, 0xe8d4a51000000000L, 
          0xe35fa931a0000000L, 0xde0b6b3a76400000L, 0xd8d726b7177a8000L, 
          0xd3c21bcecceda100L, 0xcecb8f27f4200f3aL, 0xc9f2c9cd04674edfL, 
          0xc5371912364ce305L, 0xc097ce7bc90715b3L, 0xbc143fa4e250eb31L, 
          0xb7abc627050305aeL, 0xb35dbf821ae4f38cL, 0xaf298d050e4395d7L, 
          0xab0e93b6efee0054L, 0xa70c3c40a64e6c52L, 0xa321f2d7226895c8L, 
          0x9f4f2726179a2245L, 0x9b934c3b330c8577L, 0x97edd871cfda3a57L, 
          0x945e455f24fb1cf9L, 0x90e40fbeea1d3a4bL, 0x8d7eb76070a08aedL, 
          0x8a2dbf142dfcc7abL, 0x86f0ac99b4e8dafdL, 0x83c7088e1aab65dbL, 
          0x80b05e5ac60b6178L, 0xfb5878494ace3a5fL, 0xf5746577930d6501L, 
          0xefb3ab16c59b14a3L, 0xea1575143cf97227L, 0xe498f455c38b997aL, 
          0xdf3d5e9bc0f653e1L, 0xda01ee641a708deaL, 0xd4e5e2cdc1d1ea96L, 
          0xcfe87f7cef46ff17L, 0xcb090c8001ab551cL, 0xc646d63501a1511eL, 
          0xc1a12d2fc3978937L, 0xbd176620a501fc00L, 0xb8a8d9bbe123f018L, 
          0xb454e4a179dd1877L, 0xb01ae745b101e9e4L, 0xabfa45da0edbde69L, 
          0xa7f26836f282b733L, 0xa402b9c5a8d3a6e7L, 0xa02aa96b06deb0feL, 
          0x9c69a97284b578d8L, 0x98bf2f79d5993803L, 0x952ab45cfa97a0b3L, 
          0x91abb422ccb812efL, 0x8e41ade9fbebc27dL, 0x8aec23d680043beeL, 
          0x87aa9aff79042287L, 0x847c9b5d7c2e09b7L, 0x8161afb94b44f57dL, 
          0xfcb2cb35e702af78L, 0xf6c69a72a3989f5cL, 0xf0fdf2d3f3c30b9fL, 
          0xeb57ff22fc0c795aL, 0xe5d3ef282a242e82L, 0xe070f78d3927556bL, 
          0xdb2e51bfe9d0696aL, 0xd60b3bd56a5586f2L, 0xd106f86e69d785c8L, 
          0xcc20ce9bd35c78a5L, 0xc75809c42c684dd1L, 0xc2abf989935ddbfeL, 
          0xbe1bf1b059e9a8d6L, 0xb9a74a0637ce2ee1L, 0xb54d5e4a127f59c8L, 
          0xb10d8e1456105dadL, 0xace73cbfdc0bfb7bL, 0xa8d9d1535ce3b396L, 
          0xa4e4b66b68b65d61L, 0xa1075a24e4421731L, 0x9d412e0806e88aa6L, 
          0x9991a6f3d6bf1766L, 0x95f83d0a1fb69cd9L, 0x92746b9be2f8552cL, 
          0x8f05b1163ba6832dL, 0x8bab8eefb6409c1aL, 0x8865899617fb1871L, 
          0x8533285c936b35dfL, 0x8213f56a67f6b29cL, 0xfe0efb53d30dd4d8L, 
          0xf81aa16fdc1b81dbL, 0xf24a01a73cf2dcd0L, 0xec9c459d51852ba3L, 
          0xe7109bfba19c0c9dL, 0xe1a63853bbd26451L, 0xdc5c5301c56b75f7L, 
          0xd732290fbacaf134L, 0xd226fc195c6a2f8cL, 0xcd3a1230c43fb26fL, 
          0xc86ab5c39fa63441L, 0xc3b8358109e84f07L, 0xbf21e44003acdd2dL, 
          0xbaa718e68396cffeL, 0xb6472e511c81471eL, 0xb201833b35d63f73L, 
  };
  
  // base 2 exponents for 10**-345 through 10**309, at intervals of 1000
  private static final short[] pow10x = {
          -1146, -1136, -1126, -1116, -1106, -1096, -1086, -1076, 
          -1066, -1056, -1046, -1036, -1026, -1016, -1006, -996, 
          -986, -976, -966, -956, -946, -936, -926, -916, 
          -906, -896, -886, -876, -867, -857, -847, -837, 
          -827, -817, -807, -797, -787, -777, -767, -757, 
          -747, -737, -727, -717, -707, -697, -687, -677, 
          -667, -657, -647, -637, -627, -617, -607, -597, 
          -587, -578, -568, -558, -548, -538, -528, -518, 
          -508, -498, -488, -478, -468, -458, -448, -438, 
          -428, -418, -408, -398, -388, -378, -368, -358, 
          -348, -338, -328, -318, -308, -298, -289, -279, 
          -269, -259, -249, -239, -229, -219, -209, -199, 
          -189, -179, -169, -159, -149, -139, -129, -119, 
          -109, -99, -89, -79, -69, -59, -49, -39, 
          -29, -19, -9, 1, 10, 20, 30, 40, 
          50, 60, 70, 80, 90, 100, 110, 120, 
          130, 140, 150, 160, 170, 180, 190, 200, 
          210, 220, 230, 240, 250, 260, 270, 280, 
          290, 299, 309, 319, 329, 339, 349, 359, 
          369, 379, 389, 399, 409, 419, 429, 439, 
          449, 459, 469, 479, 489, 499, 509, 519, 
          529, 539, 549, 559, 569, 579, 588, 598, 
          608, 618, 628, 638, 648, 658, 668, 678, 
          688, 698, 708, 718, 728, 738, 748, 758, 
          768, 778, 788, 798, 808, 818, 828, 838, 
          848, 858, 868, 877, 887, 897, 907, 917, 
          927, 937, 947, 957, 967, 977, 987, 997, 
          1007, 1017, 1027, 
  };

  private static long decToDouble(boolean negative, int base10x, long base10m) {
    if (base10m == 0) {
      return (negative ? NEGATIVE_ZERO : ZERO);
    }
    // maximize base10m to ensure consistency between toString and parseDouble
    while ((base10m > 0) && (base10m <= 0x1999999999999999L)) { // (Long.MAX_VALUE / 5))) {
      base10m = (base10m << 3) + (base10m << 1);
      base10x--;
    }
    // base10x needs to be a multiple of 3, because the tables are
    // spaced at intervals of 1000 (not 10).
    base10x += 345;
    int mod = base10x % 3;
    base10x /= 3;
    if (base10x < 0) { // -345
      return (negative ? NEGATIVE_ZERO : ZERO);
    } else if (base10x > 218) { // 309
      return (negative ? NEGATIVE_INFINITY : POSITIVE_INFINITY);
    }
    int base2x = pow10x[base10x];
    int s = BitUtils.countLeadingZeros(base10m);
    base10m <<= s;
    base2x -= s;
    long base2m = dpMul(base10m, pow10m[base10x]);
    while (mod > 0) {
      if (base2m < 0) {
        base2m >>>= 1;
        base2x++;
      }
      base2m += base2m >>> 2;
      base2x += 3;
      mod--;
    }
    return pack(negative, base2x, base2m);
  }

  /**
   * Double-precision integer multiplication of x1 and x2.
   */
  private static final long dpMul(long x1, long x2) {
    long v1 = (x1 >>> 32)        * (x2 >>> 32);
    long v2 = (x1 & 0xffffffffL) * (x2 >>> 32);
    long v3 = (x1 >>> 32)        * (x2 & 0xffffffffL);
    v1 += v2 >>> 32;
    v1 += v3 >>> 32;
    if (((v2 + v3) << 32) < 0) {
      v1++;
    }
    return v1;
  }
  
  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#parseDouble(String)">Double.parseDouble(String)</a>.
   * <p>
   * See the notes on <code>toString</code> for some caveats on String 
   * conversion.
   *
   * @see #toString
   * @exception  NumberFormatException  if the string does not contain a
   *               parsable number.
   */
  public static long parseDouble(String s) {
    // remove leading & trailing whitespace
    s = s.trim().toUpperCase();
    
    // check length
    int len = s.length();
    if (len == 0) {
      throw new NumberFormatException(s);
    }
    
    // check for NaN
    if ("NAN".equals(s)) {
      return NaN;
    }
    
    // begin parsing, one character at a time
    int idx = 0;
    
    // read sign
    boolean negative = false;
    char c = s.charAt(0);
    negative = (c == '-');
    if (negative || (c == '+')) {
      idx = 1;
    }

    // check for "Infinity"
    if (idx < len) {
      c = s.charAt(idx);
      if ((c == 'I') || (c == 'i')) {
        if ("INFINITY".equals(s.substring(idx))) {
          return (negative ? NEGATIVE_INFINITY : POSITIVE_INFINITY);
        }
      }
    }

    // read Digits.Digits
    long mantissa = 0;
    int exponent = 0;
    int fractionChars = 0;
    boolean sticky = false;
    boolean readingFraction = false;
    while (idx < len) {
      c = s.charAt(idx);
      if (c == '.') {
        if (readingFraction) {
          throw new NumberFormatException(s);
        }
        readingFraction = true;
      } else if ((c < '0') || (c > '9')) {
        break;
      } else {
        fractionChars++;
        if (mantissa <= 0x1999999999999998L) { // ((Long.MAX_VALUE / 5) - 1)
          mantissa = (mantissa << 3) + (mantissa << 1) + (c - '0');
          if (readingFraction) {
            exponent--;
          }
        } else {
          if (! readingFraction) {
            exponent++;
          }
          sticky |= (c != '0');
        }
      }
      idx++;
    }
    if (fractionChars == 0) {
      throw new NumberFormatException(s);
    }
    
    // read exponent
    if (((idx + 1) < len) && ((s.charAt(idx) == 'E') || (s.charAt(idx) == 'e'))) {
      try {
        exponent += Integer.parseInt(s.substring(idx + 1));
      } catch (NumberFormatException e) {
        throw new NumberFormatException(s);
      }
      idx = len;
    } else if (idx != len) {
      // check that we parsed the entire string
      throw new NumberFormatException(s);
    }

    // convert the decimal to a float
    return decToDouble(negative, exponent, mantissa);
  }

  // binary -> decimal
  
  // base 10 mantissas for 2**-1075 through 2**972, at intervals of 2**11
  private static final long[] pow2m = {
          0x3f3d8b077b8e0b11L, 0x81842f29f2cce376L, 0x1a8662f3b3919708L, 
          0x3652b62c71ce021dL, 0x6f40f20501a5e7a8L, 0xe3d8f9e563a198e5L, 
          0x2ea9c639a0e5b3ffL, 0x5f90f22001d66e96L, 0xc3b8358109e84f07L, 
          0x2815578d865470daL, 0x52173a79e8197a93L, 0xa81f301449ee8c70L, 
          0x226e6cf846d8ca6fL, 0x4683f19a2ab1bf59L, 0x906a617d450187e2L, 
          0x1d9388b3aa30a574L, 0x3c928069cf3cb734L, 0x7c0d50b7ee0dc0edL, 
          0xfe0efb53d30dd4d8L, 0x3407fbc42995e10bL, 0x6a8f537d42bc2b19L, 
          0xda3c0f568cc4f3e9L, 0x2cb1c756f2a408feL, 0x5b88c3416ddb353cL, 
          0xbb764c4ca7a44410L, 0x266469bcf5afc5d9L, 0x4ea0970403744553L, 
          0xa1075a24e4421731L, 0x20fa8ae248247913L, 0x438a53baf1f4ae3cL, 
          0x8a5296ffe33cc930L, 0x1c5416bb92e3e607L, 0x3a044721f1706ea6L, 
          0x76d1770e38320986L, 0xf356f7ebf83552feL, 0x31d602710b1a1374L, 
          0x6610674de9ae3c53L, 0xd106f86e69d785c8L, 0x2acf0bf77baab497L, 
          0x57ac20b32a535d5eL, 0xb38d92d760ec4455L, 0x24c5bfdd7761f2f6L, 
          0x4b4f5be23c2cf3a2L, 0x9a3c2087a63f6399L, 0x1f965966bce055efL, 
          0x40b0d7dca5a27abfL, 0x847c9b5d7c2e09b7L, 0x1b221effe500d3b5L, 
          0x3791a7ef666817f9L, 0x71ce24bb2fefcecaL, 0xe912b9d1478ceb17L, 
          0x2fbbbed612bfe181L, 0x61c209e792f16b87L, 0xc83553c5c8965d3dL, 
          0x2900ae716a34e9baL, 0x53f9341b79415b99L, 0xabfa45da0edbde69L, 
          0x233894a789cd2ec7L, 0x4821f50d63f209c9L, 0x93ba47c980e98ce0L, 
          0x1e412f0f768fad71L, 0x3df622f090826959L, 0x7ee5a7d0010b1532L, 
          0x19fd0fef9de8dfe3L, 0x353978b370747aa6L, 0x6d00f7320d3846f5L, 
          0xdf3d5e9bc0f653e1L, 0x2db830ddf3e8b84cL, 0x5da22ed4e5309410L, 
          0xbfc2ef456ae276e9L, 0x2745d2cb73b0391fL, 0x506e3af8bbc71cebL, 
          0xa4b8cab1a1563f52L, 0x21bc2b266d3a36bfL, 0x4516df8a16fe63d6L, 
          0x8d7eb76070a08aedL, 0x1cfa698c95390ba9L, 0x3b58e88c75313ecaL, 
          0x798b138e3fe1c845L, 0xf8ebad2b84e0d58cL, 0x32fa9be33ac0aeceL, 
          0x6867a5a867f103b3L, 0xd5d238a4abe98068L, 0x2bca63414390e576L, 
          0x59aedfc10d7279c6L, 0xb7abc627050305aeL, 0x259da6542d43623dL, 
          0x4d0985cb1d3608aeL, 0x9dc5ada82b70b59eL, 0x204fce5e3e250261L, 
          0x422ca8b0a00a4250L, 0x878678326eac9000L, 0x1bc16d674ec80000L, 
          0x38d7ea4c68000000L, 0x746a528800000000L, 0xee6b280000000000L, 
          0x30d4000000000000L, 0x6400000000000000L, 0xcccccccccccccccdL, 
          0x29f16b11c6d1e109L, 0x55e63b88c230e77eL, 0xafebff0bcb24aaffL, 
          0x24075f3dceac2b36L, 0x49c97747490eae84L, 0x971da05074da7befL, 
          0x1ef2d0f5da7dd8aaL, 0x3f61ed7ca0c03283L, 0x81ceb32c4b43fcf5L, 
          0x1a95a5b7f87a0ef1L, 0x3671f73b54f1c895L, 0x6f80f42fc8971bd2L, 
          0xe45c10c42a2b3b06L, 0x2ec49f14ec5fb056L, 0x5fc7edbc424d2fcbL, 
          0xc428d05aa4751e4dL, 0x282c674aadc39bb6L, 0x524675555bad4716L, 
          0xa87fea27a539e9a5L, 0x22823c3e2fc3c55bL, 0x46ac8391ca4529b0L, 
          0x90bd77f3483bb9baL, 0x1da48ce468e7c702L, 0x3cb559e42ad070a9L, 
          0x7c54afe7c43a3ecaL, 0xfea126b7d78186bdL, 0x3425eb41e9c7c9adL, 
          0x6acca251be03a951L, 0xdab99e59958885c5L, 0x2ccb7e3a7cd51959L, 
          0x5bbd6d030bf1dde6L, 0xbbe226efb628afebL, 0x267a8065858fe90cL, 
          0x4ecdd3c1949b76e0L, 0xa163ff802a3426a9L, 0x210d8432d2fc5833L, 
          0x43b12f82b63e2546L, 0x8aa22c0dbef60ee4L, 0x1c6463225ab7ec1dL, 
          0x3a25a835f947855aL, 0x7715d36033c5acc0L, 0xf3e2f893dec3f126L, 
          0x31f2ae9b9f14e0b2L, 0x664b1ff7085be8daL, 0xd17f3b51fca3a7a1L, 
          0x2ae7ad1f207d4454L, 0x57de91a832277568L, 0xb3f4e093db73a093L, 
          0x24dae7f3aec97265L, 0x4b7ab0078ad3dbf3L, 0x9a94dd3e8cf578baL, 
          0x1fa885c8d117a609L, 0x40d60ff149eacce0L, 0x84c8d4dfd2c63f3bL, 
          0x1b31bb5dc320d18fL, 0x37b1a07e7d30c7ccL, 0x720f9eb539bbf765L, 
          0xe998d258869facd7L, 0x2fd735519e3bbc2eL, 0x61fa48553bdeb07eL, 
          0xc8a883c0fdaf7df0L, 0x29184594e3437adeL, 0x542984435aa6def6L, 
          0xac5d37d5b79b6239L, 0x234cd83c273db92fL, 0x484b75379c244c28L, 
          0x940f4613ae5ed137L, 0x1e5297287c2f4579L, 0x3e19c9072331b530L, 
          0x7f2eaa0a85848581L, 0x1a0c03b1df8af611L, 0x355817f373ccb876L, 
          0x6d3fadfac84b3424L, 0xdfbdcece67006ac9L, 0x2dd27ebb4504974eL, 
          0x5dd80dc941929e51L, 0xc0314325637a193aL, 0x275c6b23eb69b26dL, 
          0x509c814fb511cfb9L, 0xa5178fff668ae0b6L, 0x21cf93dd7888939aL, 
          0x453e9f77bf8e7e29L, 0x8dd01fad907ffc3cL, 0x1d0b15a491eb8459L, 
          0x3b7b0d9ac471b2e4L, 0x79d1013cf6ab6a45L, 0xf97ae3d0d2446f25L, 
          0x3317f065bfbf5f43L
 };
          
  // base 10 exponents for 2**-1075 through 2**972, at intervals of 2**11
  private static final short[] pow2x = {
          -323, -320, -316, -313, -310, -307, -303, -300, 
          -297, -293, -290, -287, -283, -280, -277, -273, 
          -270, -267, -264, -260, -257, -254, -250, -247, 
          -244, -240, -237, -234, -230, -227, -224, -220, 
          -217, -214, -211, -207, -204, -201, -197, -194, 
          -191, -187, -184, -181, -177, -174, -171, -167, 
          -164, -161, -158, -154, -151, -148, -144, -141, 
          -138, -134, -131, -128, -124, -121, -118, -114, 
          -111, -108, -105, -101, -98, -95, -91, -88, 
          -85, -81, -78, -75, -71, -68, -65, -62, 
          -58, -55, -52, -48, -45, -42, -38, -35, 
          -32, -28, -25, -22, -18, -15, -12, -9, 
          -5, -2, 1, 5, 8, 11, 15, 18, 
          21, 25, 28, 31, 35, 38, 41, 44, 
          48, 51, 54, 58, 61, 64, 68, 71, 
          74, 78, 81, 84, 87, 91, 94, 97, 
          101, 104, 107, 111, 114, 117, 121, 124, 
          127, 131, 134, 137, 140, 144, 147, 150, 
          154, 157, 160, 164, 167, 170, 174, 177, 
          180, 184, 187, 190, 193, 197, 200, 203, 
          207, 210, 213, 217, 220, 223, 227, 230, 
          233, 237, 240, 243, 246, 250, 253, 256, 
          260, 263, 266, 270, 273, 276, 280, 283, 
          286, 289, 293 
  } ;

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#toString(double)">Double.toString(double)</a>.
   * <p>
   * String conversion is a bit of a gray area.  The J2SE implementation of
   * this function (<code>Double.toString(double)</code> has some problems.  
   * Often times it does not return the shortest valid String, even though it 
   * claims to do so, and it has a few
   * corner cases where it behaves oddly (e.g. 0.001 gets converted to 
   * the String "0.0010").
   * <p>
   * The implementation in MicroDouble uses a much simpler table-based 
   * algorithm.  It frequently returns slightly different results than 
   * <code>Double.toString(double)</code>.  Sometimes the results are better,
   * and sometimes worse.  Ususally the difference is confined to the last
   * character, which may be different or missing in one of the results.
   */
  public static String toString(long d) {
    return toString(d, 100);
  }
  
  /**
   * Returns a string representation of the double argument, rounded so that
   * the returned <code>String</code> is no longer than
   * <code>maxStringLength</code> characters (or 9 characters, if 
   * <code>maxStringLength</code> is less than 9).  
   *
   * @param      d   the <code>double</code> to be converted.
   * @param      maxStringLength the maximum length of the returned string 
   * @return     a string representation of the argument.
   *
   * @see #toString(long)
   */
  public static String toString(long d, int maxStringLength) {
    if (isNaN(d)) {
      return "NaN";
    }
    boolean n = unpackSign(d);
    if (isZero(d)) {
      return (n ? "-0.0" : "0.0");
    } else if (isInfinite(d)) {
      return (n ? "-Infinity" : "Infinity");
    }
    if (maxStringLength < 9) {
      maxStringLength = 9;
    }
    // convert from base 2 to base 10
    int base2x = unpackExponent(d);
    long base2m = unpackMantissa(d);
    int idx = base2x + 1075;
    int dx = idx % 11;
    base2m <<= dx;
    idx /= 11;
    int base10x = pow2x[idx];
    while (base2m <= 0xcccccccccccccccL) {
      base2m = (base2m << 3) + (base2m << 1); // base2m *= 10;
      base10x--;
    }
    long base10m = dpMul(base2m, pow2m[idx]);
    boolean roundedUp = false;
    while (true) {
      int r = (int) (base10m % 10);
      long mt = base10m / 10;
      int xt = base10x + 1;
      if (r != 0) {
        boolean rut;
        if ((r > 5) || ((r == 5) && (! roundedUp))) {
          rut = true;
          mt++;
        } else {
          rut = false;
        }
        long dt = decToDouble(n, xt, mt);
        if (dt != d) {
          if (rut) {
            mt--;
          } else {
            mt++;
          }
          rut ^= true;
          dt = decToDouble(n, xt, mt);
          if (dt != d) {
            break;
          }
        }
        roundedUp = rut;
      }
      base10m = mt;
      base10x = xt;
    }
    
    while (true) {
      String s = toString(n, base10x, base10m);
      if (s.length() <= maxStringLength) {
        return s;
      }
      int r = (int) (base10m % 10);
      base10m /= 10;
      base10x++;
      if ((r > 5) || ((r == 5) && (! roundedUp))) {
        roundedUp = true;
        base10m++;
      } else {
        roundedUp = false;
      }      
      while ((base10m % 10) == 0) {
        base10m /= 10;
        base10x++;
      }
    }
  }
  
  private static String toString(boolean negative, int base10x, long base10m) {
    StringBuffer sb = new StringBuffer(26);
    if (negative) {
      sb.append('-');
    }
    String s = Long.toString(base10m);
    base10x += s.length() - 1;
    boolean scientific = ((base10x < -3) || (base10x >= 7));
    int dp; // index of decimal point in final string
    if (scientific) {
      dp = 1;
    } else {
      dp = base10x + 1;
      if (dp < 1) {
        sb.append('0');
      }
    }
    for (int i=0; i<dp; i++) {
      if (i < s.length()) {
        sb.append(s.charAt(i));
      } else {
        sb.append('0');
      }
    }
    sb.append('.');
    if (dp >= s.length()) {
      sb.append('0');
    } else {
      for (int i=dp; i<s.length(); i++) {
        if (i < 0) {
          sb.append('0');
        } else {
          sb.append(s.charAt(i));
        }
      }
    }
    if (scientific) {
      sb.append('E');
      sb.append(Integer.toString(base10x));
    }
    return sb.toString();
  }

  private static final long ONE_EIGHTY =           0x4066800000000000L;
  private static final long TWO_HUNDRED =          0x4069000000000000L;
  
  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#toDegrees(double)">Math.toDegrees(double)</a>.
   */
  public static long toDegrees(long angrad) {
    return div(mul(angrad, ONE_EIGHTY), PI);
  }
  
  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#toRadians(double)">Math.toRadians(double)</a>.
   */
  public static long toRadians(long angdeg) {
    return mul(div(angdeg, ONE_EIGHTY), PI);
  }

  public static long toGradians(long angrad) {
    return div(mul(angrad, TWO_HUNDRED), PI);
  }

  public static long gradiansToRadians(long anggrad) {
    return mul(div(anggrad, TWO_HUNDRED), PI);
  }
  
  /////////////////////////////////////////////////////////////////////////////
  // Elementary functions.  Most are ported directly from fdlibm.
  /////////////////////////////////////////////////////////////////////////////

  private static long set(int newHiPart, int newLowPart) {
    return ((((long) newHiPart) << 32) | newLowPart);
  }

  private static long setLO(long d, int newLowPart) {
    return ((d & 0xFFFFFFFF00000000L) | newLowPart);
  }

  private static long setHI(long d, int newHiPart) {
    return ((d & 0x00000000FFFFFFFFL) | (((long) newHiPart) << 32));
  }

  private static int getHI(long d) {
    return ((int) (d >> 32));
  }
  
  private static int getLO(long d) {
    return ((int) d);
  }

  private static int ilogb(long d) {
    if (isZero(d)) {
      return 0x80000001;
    } else if (isNaN(d) || (isInfinite(d))) {
      return 0x7fffffff;
    }
    int x = (((int) (d >> 52)) & 0x7ff);
    if (x == 0) {
      long m = (d & FRACTION_MASK);
      while (m < IMPLIED_ONE) {
        m <<= 1;
        x--;
      }
    }
    return x - 1023;
  }
  
  /**
   * @return the magnitude of x with the sign of y
   */
  private static long copySign(long x, long y) {
    return (x & 0x7fffffffffffffffL) | (y & 0x8000000000000000L);
  }

  /** 
   * Returns the value of the first argument, multiplied by 2 raised to the
   * power of the second argument.  Note that the second argument is really
   * an <code>int</code>, not a <code>float</code> or <code>double</code>.
   *
   * @param d   a <code>double</code> value.
   * @param n   an <code>int</code> value.
   * @return  the value <code>d * 2<sup>n</sup></code>.
   */
  public static long scalbn(long d, int n) {
    if (isNaN(d)) {
      return NaN;
    } else if ((n == 0) || isInfinite(d) || isZero(d)) {
      return d;
    } else if (n >= 2098) {
      return copySign(POSITIVE_INFINITY, d);
    } else if (n <= -2099) {
      return copySign(ZERO, d);
    }
    int x = ((int) (d >> 52) & 0x7ff);
    int x2 = x + n;
    if ((x == 0) || (x2 <= 0)) { // argument and/or return value are subnormal 
      return pack(unpackSign(d), x2 - 1075, unpackMantissa(d));
    } else if (x2 >= 0x7ff) { // overflow
      return copySign(POSITIVE_INFINITY, d);
    }
    return ((d & 0x800fffffffffffffL) | (((long) x2) << 52));
  }

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#IEEEremainder(double, double)">Math.IEEEremainder(double, double)</a>.
   */
  public static long IEEEremainder(long d1, long d2) {
    if (isNaN(d1) || isNaN(d2) || isInfinite(d1) || isZero(d2)) {
      return NaN;
    } else if (isZero(d1) || isInfinite(d2)) {
      return d1;
    }
    int hx = getHI(d1); // high word of x 
    int lx = getLO(d1); // low  word of x 
    int hp = getHI(d2); // high word of p 
    int lp = getLO(d2); // low  word of p 
    boolean negative = unpackSign(d1);
    hp &= 0x7fffffff;
    hx &= 0x7fffffff;

    if (hp<=0x7fdfffff) d1 = mod(d1,scalbn(d2, 1)); // now x < 2p 
    if (((hx-hp)|(lx-lp))==0) return ZERO; //zero*x;
    d1  = abs(d1);
    d2  = abs(d2);
    if (hp<0x00200000) {
      if(gt(scalbn(d1, 1), d2)) {
        d1 = sub(d1, d2);
        if (ge(scalbn(d1, 1), d2)) d1 = sub(d1, d2);
      }
    } else {
      long p_half = scalbn(d2, -1);
      if (gt(d1, p_half)) {
        d1 = sub(d1, d2);
        if (ge(d1, p_half)) d1 = sub(d1, d2);
      }
    }
    if (negative) {
      return negate(d1);
    }
    return d1;
  }

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#sqrt(double)">Math.sqrt(double)</a>.
   */
  public static long sqrt(long d) {
    if (isZero(d)) {
      return d;
    } else if (unpackSign(d) || isNaN(d)) {
      return NaN;
    } else if (d == POSITIVE_INFINITY) {
      return d;
    }
    // f is positive, nonzero, and finite

    // unpack
    int x = unpackExponent(d);
    long m = unpackMantissa(d);
    // normalize 
    while (m < IMPLIED_ONE) {
      m <<= 1;
      x--;
    }
    // make exponent even
    if ((x & 1) != 0) {
      m <<= 1;
    }
    // compute final exponent
    x = (x >> 1) - 26;
    
    // generate sqrt(x) bit by bit
    m <<= 1;
    long q = 0L; // q = sqrt(x)
    long s = 0L;
    long r = 0x0020000000000000L;
    while (r != 0) {
      long t = s + r;
      if (t < m) {
        s = t + r;
        m -= t;
        q |= r;
      }
