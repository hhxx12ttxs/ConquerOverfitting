<<<<<<< HEAD
/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.common.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * Some useful additions to the built-in functions in {@link Math}.
 *
 * @version $Revision: 927249 $ $Date: 2010-03-24 21:06:51 -0400 (Wed, 24 Mar 2010) $
 */
public final class MathUtils {


    /**
     * Smallest positive number such that 1 - EPSILON is not numerically equal to 1.
     */
    public static final double EPSILON = 0x1.0p-53;

    /**
     * Safe minimum, such that 1 / SAFE_MIN does not overflow.
     * <p>In IEEE 754 arithmetic, this is also the smallest normalized
     * number 2<sup>-1022</sup>.</p>
     */
    public static final double SAFE_MIN = 0x1.0p-1022;

    /**
     * 2 &pi;.
     *
     * @since 2.1
     */
    public static final double TWO_PI = 2 * Math.PI;

    /**
     * -1.0 cast as a byte.
     */
    private static final byte NB = (byte) -1;

    /**
     * -1.0 cast as a short.
     */
    private static final short NS = (short) -1;

    /**
     * 1.0 cast as a byte.
     */
    private static final byte PB = (byte) 1;

    /**
     * 1.0 cast as a short.
     */
    private static final short PS = (short) 1;

    /**
     * 0.0 cast as a byte.
     */
    private static final byte ZB = (byte) 0;

    /**
     * 0.0 cast as a short.
     */
    private static final short ZS = (short) 0;

    /**
     * Gap between NaN and regular numbers.
     */
    private static final int NAN_GAP = 4 * 1024 * 1024;

    /**
     * Offset to order signed double numbers lexicographically.
     */
    private static final long SGN_MASK = 0x8000000000000000L;

    /**
     * All long-representable factorials
     */
    private static final long[] FACTORIALS = new long[]{
            1l, 1l, 2l,
            6l, 24l, 120l,
            720l, 5040l, 40320l,
            362880l, 3628800l, 39916800l,
            479001600l, 6227020800l, 87178291200l,
            1307674368000l, 20922789888000l, 355687428096000l,
            6402373705728000l, 121645100408832000l, 2432902008176640000l};

    /**
     * Private Constructor
     */
    private MathUtils() {
        super();
    }

    /**
     * Add two integers, checking for overflow.
     *
     * @param x an addend
     * @param y an addend
     * @return the sum <code>x+y</code>
     * @throws ArithmeticException if the result can not be represented as an
     *                             int
     * @since 1.1
     */
    public static int addAndCheck(int x, int y) {
        long s = (long) x + (long) y;
        if (s < Integer.MIN_VALUE || s > Integer.MAX_VALUE) {
            throw new ArithmeticException("overflow: add");
        }
        return (int) s;
    }

    /**
     * Add two long integers, checking for overflow.
     *
     * @param a an addend
     * @param b an addend
     * @return the sum <code>a+b</code>
     * @throws ArithmeticException if the result can not be represented as an
     *                             long
     * @since 1.2
     */
    public static long addAndCheck(long a, long b) {
        return addAndCheck(a, b, "overflow: add");
    }

    /**
     * Add two long integers, checking for overflow.
     *
     * @param a   an addend
     * @param b   an addend
     * @param msg the message to use for any thrown exception.
     * @return the sum <code>a+b</code>
     * @throws ArithmeticException if the result can not be represented as an
     *                             long
     * @since 1.2
     */
    private static long addAndCheck(long a, long b, String msg) {
        long ret;
        if (a > b) {
            // use symmetry to reduce boundary cases
            ret = addAndCheck(b, a, msg);
        } else {
            // assert a <= b

            if (a < 0) {
                if (b < 0) {
                    // check for negative overflow
                    if (Long.MIN_VALUE - b <= a) {
                        ret = a + b;
                    } else {
                        throw new ArithmeticException(msg);
                    }
                } else {
                    // opposite sign addition is always safe
                    ret = a + b;
                }
            } else {
                // assert a >= 0
                // assert b >= 0

                // check for positive overflow
                if (a <= Long.MAX_VALUE - b) {
                    ret = a + b;
                } else {
                    throw new ArithmeticException(msg);
                }
            }
        }
        return ret;
    }

    /**
     * Returns an exact representation of the <a
     * href="http://mathworld.wolfram.com/BinomialCoefficient.html"> Binomial
     * Coefficient</a>, "<code>n choose k</code>", the number of
     * <code>k</code>-element subsets that can be selected from an
     * <code>n</code>-element set.
     * <p>
     * <Strong>Preconditions</strong>:
     * <ul>
     * <li> <code>0 <= k <= n </code> (otherwise
     * <code>IllegalArgumentException</code> is thrown)</li>
     * <li> The result is small enough to fit into a <code>long</code>. The
     * largest value of <code>n</code> for which all coefficients are
     * <code> < Long.MAX_VALUE</code> is 66. If the computed value exceeds
     * <code>Long.MAX_VALUE</code> an <code>ArithMeticException</code> is
     * thrown.</li>
     * </ul></p>
     *
     * @param n the size of the set
     * @param k the size of the subsets to be counted
     * @return <code>n choose k</code>
     * @throws IllegalArgumentException if preconditions are not met.
     * @throws ArithmeticException      if the result is too large to be represented
     *                                  by a long integer.
     */
    public static long binomialCoefficient(final int n, final int k) {
        checkBinomial(n, k);
        if ((n == k) || (k == 0)) {
            return 1;
        }
        if ((k == 1) || (k == n - 1)) {
            return n;
        }
        // Use symmetry for large k
        if (k > n / 2)
            return binomialCoefficient(n, n - k);

        // We use the formula
        // (n choose k) = n! / (n-k)! / k!
        // (n choose k) == ((n-k+1)*...*n) / (1*...*k)
        // which could be written
        // (n choose k) == (n-1 choose k-1) * n / k
        long result = 1;
        if (n <= 61) {
            // For n <= 61, the naive implementation cannot overflow.
            int i = n - k + 1;
            for (int j = 1; j <= k; j++) {
                result = result * i / j;
                i++;
            }
        } else if (n <= 66) {
            // For n > 61 but n <= 66, the result cannot overflow,
            // but we must take care not to overflow intermediate values.
            int i = n - k + 1;
            for (int j = 1; j <= k; j++) {
                // We know that (result * i) is divisible by j,
                // but (result * i) may overflow, so we split j:
                // Filter out the gcd, d, so j/d and i/d are integer.
                // result is divisible by (j/d) because (j/d)
                // is relative prime to (i/d) and is a divisor of
                // result * (i/d).
                final long d = gcd(i, j);
                result = (result / (j / d)) * (i / d);
                i++;
            }
        } else {
            // For n > 66, a result overflow might occur, so we check
            // the multiplication, taking care to not overflow
            // unnecessary.
            int i = n - k + 1;
            for (int j = 1; j <= k; j++) {
                final long d = gcd(i, j);
                result = mulAndCheck(result / (j / d), i / d);
                i++;
            }
        }
        return result;
    }

    /**
     * Returns a <code>double</code> representation of the <a
     * href="http://mathworld.wolfram.com/BinomialCoefficient.html"> Binomial
     * Coefficient</a>, "<code>n choose k</code>", the number of
     * <code>k</code>-element subsets that can be selected from an
     * <code>n</code>-element set.
     * <p>
     * <Strong>Preconditions</strong>:
     * <ul>
     * <li> <code>0 <= k <= n </code> (otherwise
     * <code>IllegalArgumentException</code> is thrown)</li>
     * <li> The result is small enough to fit into a <code>double</code>. The
     * largest value of <code>n</code> for which all coefficients are <
     * Double.MAX_VALUE is 1029. If the computed value exceeds Double.MAX_VALUE,
     * Double.POSITIVE_INFINITY is returned</li>
     * </ul></p>
     *
     * @param n the size of the set
     * @param k the size of the subsets to be counted
     * @return <code>n choose k</code>
     * @throws IllegalArgumentException if preconditions are not met.
     */
    public static double binomialCoefficientDouble(final int n, final int k) {
        checkBinomial(n, k);
        if ((n == k) || (k == 0)) {
            return 1d;
        }
        if ((k == 1) || (k == n - 1)) {
            return n;
        }
        if (k > n / 2) {
            return binomialCoefficientDouble(n, n - k);
        }
        if (n < 67) {
            return binomialCoefficient(n, k);
        }

        double result = 1d;
        for (int i = 1; i <= k; i++) {
            result *= (double) (n - k + i) / (double) i;
        }

        return Math.floor(result + 0.5);
    }

    /**
     * Returns the natural <code>log</code> of the <a
     * href="http://mathworld.wolfram.com/BinomialCoefficient.html"> Binomial
     * Coefficient</a>, "<code>n choose k</code>", the number of
     * <code>k</code>-element subsets that can be selected from an
     * <code>n</code>-element set.
     * <p>
     * <Strong>Preconditions</strong>:
     * <ul>
     * <li> <code>0 <= k <= n </code> (otherwise
     * <code>IllegalArgumentException</code> is thrown)</li>
     * </ul></p>
     *
     * @param n the size of the set
     * @param k the size of the subsets to be counted
     * @return <code>n choose k</code>
     * @throws IllegalArgumentException if preconditions are not met.
     */
    public static double binomialCoefficientLog(final int n, final int k) {
        checkBinomial(n, k);
        if ((n == k) || (k == 0)) {
            return 0;
        }
        if ((k == 1) || (k == n - 1)) {
            return Math.log(n);
        }

        /*
         * For values small enough to do exact integer computation,
         * return the log of the exact value
         */
        if (n < 67) {
            return Math.log(binomialCoefficient(n, k));
        }

        /*
         * Return the log of binomialCoefficientDouble for values that will not
         * overflow binomialCoefficientDouble
         */
        if (n < 1030) {
            return Math.log(binomialCoefficientDouble(n, k));
        }

        if (k > n / 2) {
            return binomialCoefficientLog(n, n - k);
        }

        /*
         * Sum logs for values that could overflow
         */
        double logSum = 0;

        // n!/(n-k)!
        for (int i = n - k + 1; i <= n; i++) {
            logSum += Math.log(i);
        }

        // divide by k!
        for (int i = 2; i <= k; i++) {
            logSum -= Math.log(i);
        }

        return logSum;
    }

    /**
     * Check binomial preconditions.
     *
     * @param n the size of the set
     * @param k the size of the subsets to be counted
     * @throws IllegalArgumentException if preconditions are not met.
     */
    private static void checkBinomial(final int n, final int k)
            throws IllegalArgumentException {
        if (n < k) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "must have n >= k for binomial coefficient (n,k), got n = {0}, k = {1}",
                    n, k);
        }
        if (n < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "must have n >= 0 for binomial coefficient (n,k), got n = {0}",
                    n);
        }
    }

    /**
     * Compares two numbers given some amount of allowed error.
     *
     * @param x   the first number
     * @param y   the second number
     * @param eps the amount of error to allow when checking for equality
     * @return <ul><li>0 if  {@link #equals(double, double, double) equals(x, y, eps)}</li>
     *         <li>&lt; 0 if !{@link #equals(double, double, double) equals(x, y, eps)} &amp;&amp; x &lt; y</li>
     *         <li>> 0 if !{@link #equals(double, double, double) equals(x, y, eps)} &amp;&amp; x > y</li></ul>
     */
    public static int compareTo(double x, double y, double eps) {
        if (equals(x, y, eps)) {
            return 0;
        } else if (x < y) {
            return -1;
        }
        return 1;
    }

    /**
     * Returns the <a href="http://mathworld.wolfram.com/HyperbolicCosine.html">
     * hyperbolic cosine</a> of x.
     *
     * @param x double value for which to find the hyperbolic cosine
     * @return hyperbolic cosine of x
     */
    public static double cosh(double x) {
        return (Math.exp(x) + Math.exp(-x)) / 2.0;
    }

    /**
     * Returns true iff both arguments are NaN or neither is NaN and they are
     * equal
     *
     * @param x first value
     * @param y second value
     * @return true if the values are equal or both are NaN
     */
    public static boolean equals(double x, double y) {
        return (Double.isNaN(x) && Double.isNaN(y)) || x == y;
    }

    /**
     * Returns true iff both arguments are equal or within the range of allowed
     * error (inclusive).
     * <p>
     * Two NaNs are considered equals, as are two infinities with same sign.
     * </p>
     *
     * @param x   first value
     * @param y   second value
     * @param eps the amount of absolute error to allow
     * @return true if the values are equal or within range of each other
     */
    public static boolean equals(double x, double y, double eps) {
        return equals(x, y) || (Math.abs(y - x) <= eps);
    }

    /**
     * Returns true iff both arguments are equal or within the range of allowed
     * error (inclusive).
     * Adapted from <a
     * href="http://www.cygnus-software.com/papers/comparingfloats/comparingfloats.htm">
     * Bruce Dawson</a>
     *
     * @param x       first value
     * @param y       second value
     * @param maxUlps {@code (maxUlps - 1)} is the number of floating point
     *                values between {@code x} and {@code y}.
     * @return {@code true} if there are less than {@code maxUlps} floating
     *         point values between {@code x} and {@code y}
     */
    public static boolean equals(double x, double y, int maxUlps) {
        // Check that "maxUlps" is non-negative and small enough so that the
        // default NAN won't compare as equal to anything.
        assert maxUlps > 0 && maxUlps < NAN_GAP;

        long xInt = Double.doubleToLongBits(x);
        long yInt = Double.doubleToLongBits(y);

        // Make lexicographically ordered as a two's-complement integer.
        if (xInt < 0) {
            xInt = SGN_MASK - xInt;
        }
        if (yInt < 0) {
            yInt = SGN_MASK - yInt;
        }

        return Math.abs(xInt - yInt) <= maxUlps;
    }

    /**
     * Returns true iff both arguments are null or have same dimensions
     * and all their elements are {@link #equals(double,double) equals}
     *
     * @param x first array
     * @param y second array
     * @return true if the values are both null or have same dimension
     *         and equal elements
     * @since 1.2
     */
    public static boolean equals(double[] x, double[] y) {
        if ((x == null) || (y == null)) {
            return !((x == null) ^ (y == null));
        }
        if (x.length != y.length) {
            return false;
        }
        for (int i = 0; i < x.length; ++i) {
            if (!equals(x[i], y[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns n!. Shorthand for <code>n</code> <a
     * href="http://mathworld.wolfram.com/Factorial.html"> Factorial</a>, the
     * product of the numbers <code>1,...,n</code>.
     * <p>
     * <Strong>Preconditions</strong>:
     * <ul>
     * <li> <code>n >= 0</code> (otherwise
     * <code>IllegalArgumentException</code> is thrown)</li>
     * <li> The result is small enough to fit into a <code>long</code>. The
     * largest value of <code>n</code> for which <code>n!</code> <
     * Long.MAX_VALUE</code> is 20. If the computed value exceeds <code>Long.MAX_VALUE</code>
     * an <code>ArithMeticException </code> is thrown.</li>
     * </ul>
     * </p>
     *
     * @param n argument
     * @return <code>n!</code>
     * @throws ArithmeticException      if the result is too large to be represented
     *                                  by a long integer.
     * @throws IllegalArgumentException if n < 0
     */
    public static long factorial(final int n) {
        if (n < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "must have n >= 0 for n!, got n = {0}",
                    n);
        }
        if (n > 20) {
            throw new ArithmeticException(
                    "factorial value is too large to fit in a long");
        }
        return FACTORIALS[n];
    }

    /**
     * Returns n!. Shorthand for <code>n</code> <a
     * href="http://mathworld.wolfram.com/Factorial.html"> Factorial</a>, the
     * product of the numbers <code>1,...,n</code> as a <code>double</code>.
     * <p>
     * <Strong>Preconditions</strong>:
     * <ul>
     * <li> <code>n >= 0</code> (otherwise
     * <code>IllegalArgumentException</code> is thrown)</li>
     * <li> The result is small enough to fit into a <code>double</code>. The
     * largest value of <code>n</code> for which <code>n!</code> <
     * Double.MAX_VALUE</code> is 170. If the computed value exceeds
     * Double.MAX_VALUE, Double.POSITIVE_INFINITY is returned</li>
     * </ul>
     * </p>
     *
     * @param n argument
     * @return <code>n!</code>
     * @throws IllegalArgumentException if n < 0
     */
    public static double factorialDouble(final int n) {
        if (n < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "must have n >= 0 for n!, got n = {0}",
                    n);
        }
        if (n < 21) {
            return factorial(n);
        }
        return Math.floor(Math.exp(factorialLog(n)) + 0.5);
    }

    /**
     * Returns the natural logarithm of n!.
     * <p>
     * <Strong>Preconditions</strong>:
     * <ul>
     * <li> <code>n >= 0</code> (otherwise
     * <code>IllegalArgumentException</code> is thrown)</li>
     * </ul></p>
     *
     * @param n argument
     * @return <code>n!</code>
     * @throws IllegalArgumentException if preconditions are not met.
     */
    public static double factorialLog(final int n) {
        if (n < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "must have n >= 0 for n!, got n = {0}",
                    n);
        }
        if (n < 21) {
            return Math.log(factorial(n));
        }
        double logSum = 0;
        for (int i = 2; i <= n; i++) {
            logSum += Math.log(i);
        }
        return logSum;
    }

    /**
     * <p>
     * Gets the greatest common divisor of the absolute value of two numbers,
     * using the "binary gcd" method which avoids division and modulo
     * operations. See Knuth 4.5.2 algorithm B. This algorithm is due to Josef
     * Stein (1961).
     * </p>
     * Special cases:
     * <ul>
     * <li>The invocations
     * <code>gcd(Integer.MIN_VALUE, Integer.MIN_VALUE)</code>,
     * <code>gcd(Integer.MIN_VALUE, 0)</code> and
     * <code>gcd(0, Integer.MIN_VALUE)</code> throw an
     * <code>ArithmeticException</code>, because the result would be 2^31, which
     * is too large for an int value.</li>
     * <li>The result of <code>gcd(x, x)</code>, <code>gcd(0, x)</code> and
     * <code>gcd(x, 0)</code> is the absolute value of <code>x</code>, except
     * for the special cases above.
     * <li>The invocation <code>gcd(0, 0)</code> is the only one which returns
     * <code>0</code>.</li>
     * </ul>
     *
     * @param p any number
     * @param q any number
     * @return the greatest common divisor, never negative
     * @throws ArithmeticException if the result cannot be represented as a
     *                             nonnegative int value
     * @since 1.1
     */
    public static int gcd(final int p, final int q) {
        int u = p;
        int v = q;
        if ((u == 0) || (v == 0)) {
            if ((u == Integer.MIN_VALUE) || (v == Integer.MIN_VALUE)) {
                throw MathRuntimeException.createArithmeticException(
                        "overflow: gcd({0}, {1}) is 2^31",
                        p, q);
            }
            return Math.abs(u) + Math.abs(v);
        }
        // keep u and v negative, as negative integers range down to
        // -2^31, while positive numbers can only be as large as 2^31-1
        // (i.e. we can't necessarily negate a negative number without
        // overflow)
        /* assert u!=0 && v!=0; */
        if (u > 0) {
            u = -u;
        } // make u negative
        if (v > 0) {
            v = -v;
        } // make v negative
        // B1. [Find power of 2]
        int k = 0;
        while ((u & 1) == 0 && (v & 1) == 0 && k < 31) { // while u and v are
            // both even...
            u /= 2;
            v /= 2;
            k++; // cast out twos.
        }
        if (k == 31) {
            throw MathRuntimeException.createArithmeticException(
                    "overflow: gcd({0}, {1}) is 2^31",
                    p, q);
        }
        // B2. Initialize: u and v have been divided by 2^k and at least
        // one is odd.
        int t = ((u & 1) == 1) ? v : -(u / 2)/* B3 */;
        // t negative: u was odd, v may be even (t replaces v)
        // t positive: u was even, v is odd (t replaces u)
        do {
            /* assert u<0 && v<0; */
            // B4/B3: cast out twos from t.
            while ((t & 1) == 0) { // while t is even..
                t /= 2; // cast out twos
            }
            // B5 [reset max(u,v)]
            if (t > 0) {
                u = -t;
            } else {
                v = t;
            }
            // B6/B3. at this point both u and v should be odd.
            t = (v - u) / 2;
            // |u| larger: t positive (replace u)
            // |v| larger: t negative (replace v)
        } while (t != 0);
        return -u * (1 << k); // gcd is u*2^k
    }

    /**
     * <p>
     * Gets the greatest common divisor of the absolute value of two numbers,
     * using the "binary gcd" method which avoids division and modulo
     * operations. See Knuth 4.5.2 algorithm B. This algorithm is due to Josef
     * Stein (1961).
     * </p>
     * Special cases:
     * <ul>
     * <li>The invocations
     * <code>gcd(Long.MIN_VALUE, Long.MIN_VALUE)</code>,
     * <code>gcd(Long.MIN_VALUE, 0L)</code> and
     * <code>gcd(0L, Long.MIN_VALUE)</code> throw an
     * <code>ArithmeticException</code>, because the result would be 2^63, which
     * is too large for a long value.</li>
     * <li>The result of <code>gcd(x, x)</code>, <code>gcd(0L, x)</code> and
     * <code>gcd(x, 0L)</code> is the absolute value of <code>x</code>, except
     * for the special cases above.
     * <li>The invocation <code>gcd(0L, 0L)</code> is the only one which returns
     * <code>0L</code>.</li>
     * </ul>
     *
     * @param p any number
     * @param q any number
     * @return the greatest common divisor, never negative
     * @throws ArithmeticException if the result cannot be represented as a nonnegative long
     *                             value
     * @since 2.1
     */
    public static long gcd(final long p, final long q) {
        long u = p;
        long v = q;
        if ((u == 0) || (v == 0)) {
            if ((u == Long.MIN_VALUE) || (v == Long.MIN_VALUE)) {
                throw MathRuntimeException.createArithmeticException(
                        "overflow: gcd({0}, {1}) is 2^63",
                        p, q);
            }
            return Math.abs(u) + Math.abs(v);
        }
        // keep u and v negative, as negative integers range down to
        // -2^63, while positive numbers can only be as large as 2^63-1
        // (i.e. we can't necessarily negate a negative number without
        // overflow)
        /* assert u!=0 && v!=0; */
        if (u > 0) {
            u = -u;
        } // make u negative
        if (v > 0) {
            v = -v;
        } // make v negative
        // B1. [Find power of 2]
        int k = 0;
        while ((u & 1) == 0 && (v & 1) == 0 && k < 63) { // while u and v are
            // both even...
            u /= 2;
            v /= 2;
            k++; // cast out twos.
        }
        if (k == 63) {
            throw MathRuntimeException.createArithmeticException(
                    "overflow: gcd({0}, {1}) is 2^63",
                    p, q);
        }
        // B2. Initialize: u and v have been divided by 2^k and at least
        // one is odd.
        long t = ((u & 1) == 1) ? v : -(u / 2)/* B3 */;
        // t negative: u was odd, v may be even (t replaces v)
        // t positive: u was even, v is odd (t replaces u)
        do {
            /* assert u<0 && v<0; */
            // B4/B3: cast out twos from t.
            while ((t & 1) == 0) { // while t is even..
                t /= 2; // cast out twos
            }
            // B5 [reset max(u,v)]
            if (t > 0) {
                u = -t;
            } else {
                v = t;
            }
            // B6/B3. at this point both u and v should be odd.
            t = (v - u) / 2;
            // |u| larger: t positive (replace u)
            // |v| larger: t negative (replace v)
        } while (t != 0);
        return -u * (1L << k); // gcd is u*2^k
    }

    /**
     * Returns an integer hash code representing the given double value.
     *
     * @param value the value to be hashed
     * @return the hash code
     */
    public static int hash(double value) {
        return new Double(value).hashCode();
    }

    /**
     * Returns an integer hash code representing the given double array.
     *
     * @param value the value to be hashed (may be null)
     * @return the hash code
     * @since 1.2
     */
    public static int hash(double[] value) {
        return Arrays.hashCode(value);
    }

    /**
     * For a byte value x, this method returns (byte)(+1) if x >= 0 and
     * (byte)(-1) if x < 0.
     *
     * @param x the value, a byte
     * @return (byte)(+1) or (byte)(-1), depending on the sign of x
     */
    public static byte indicator(final byte x) {
        return (x >= ZB) ? PB : NB;
    }

    /**
     * For a double precision value x, this method returns +1.0 if x >= 0 and
     * -1.0 if x < 0. Returns <code>NaN</code> if <code>x</code> is
     * <code>NaN</code>.
     *
     * @param x the value, a double
     * @return +1.0 or -1.0, depending on the sign of x
     */
    public static double indicator(final double x) {
        if (Double.isNaN(x)) {
            return Double.NaN;
        }
        return (x >= 0.0) ? 1.0 : -1.0;
    }

    /**
     * For a float value x, this method returns +1.0F if x >= 0 and -1.0F if x <
     * 0. Returns <code>NaN</code> if <code>x</code> is <code>NaN</code>.
     *
     * @param x the value, a float
     * @return +1.0F or -1.0F, depending on the sign of x
     */
    public static float indicator(final float x) {
        if (Float.isNaN(x)) {
            return Float.NaN;
        }
        return (x >= 0.0F) ? 1.0F : -1.0F;
    }

    /**
     * For an int value x, this method returns +1 if x >= 0 and -1 if x < 0.
     *
     * @param x the value, an int
     * @return +1 or -1, depending on the sign of x
     */
    public static int indicator(final int x) {
        return (x >= 0) ? 1 : -1;
    }

    /**
     * For a long value x, this method returns +1L if x >= 0 and -1L if x < 0.
     *
     * @param x the value, a long
     * @return +1L or -1L, depending on the sign of x
     */
    public static long indicator(final long x) {
        return (x >= 0L) ? 1L : -1L;
    }

    /**
     * For a short value x, this method returns (short)(+1) if x >= 0 and
     * (short)(-1) if x < 0.
     *
     * @param x the value, a short
     * @return (short)(+1) or (short)(-1), depending on the sign of x
     */
    public static short indicator(final short x) {
        return (x >= ZS) ? PS : NS;
    }

    /**
     * <p>
     * Returns the least common multiple of the absolute value of two numbers,
     * using the formula <code>lcm(a,b) = (a / gcd(a,b)) * b</code>.
     * </p>
     * Special cases:
     * <ul>
     * <li>The invocations <code>lcm(Integer.MIN_VALUE, n)</code> and
     * <code>lcm(n, Integer.MIN_VALUE)</code>, where <code>abs(n)</code> is a
     * power of 2, throw an <code>ArithmeticException</code>, because the result
     * would be 2^31, which is too large for an int value.</li>
     * <li>The result of <code>lcm(0, x)</code> and <code>lcm(x, 0)</code> is
     * <code>0</code> for any <code>x</code>.
     * </ul>
     *
     * @param a any number
     * @param b any number
     * @return the least common multiple, never negative
     * @throws ArithmeticException if the result cannot be represented as a nonnegative int
     *                             value
     * @since 1.1
     */
    public static int lcm(int a, int b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        int lcm = Math.abs(mulAndCheck(a / gcd(a, b), b));
        if (lcm == Integer.MIN_VALUE) {
            throw MathRuntimeException.createArithmeticException(
                    "overflow: lcm({0}, {1}) is 2^31",
                    a, b);
        }
        return lcm;
    }

    /**
     * <p>
     * Returns the least common multiple of the absolute value of two numbers,
     * using the formula <code>lcm(a,b) = (a / gcd(a,b)) * b</code>.
     * </p>
     * Special cases:
     * <ul>
     * <li>The invocations <code>lcm(Long.MIN_VALUE, n)</code> and
     * <code>lcm(n, Long.MIN_VALUE)</code>, where <code>abs(n)</code> is a
     * power of 2, throw an <code>ArithmeticException</code>, because the result
     * would be 2^63, which is too large for an int value.</li>
     * <li>The result of <code>lcm(0L, x)</code> and <code>lcm(x, 0L)</code> is
     * <code>0L</code> for any <code>x</code>.
     * </ul>
     *
     * @param a any number
     * @param b any number
     * @return the least common multiple, never negative
     * @throws ArithmeticException if the result cannot be represented as a nonnegative long
     *                             value
     * @since 2.1
     */
    public static long lcm(long a, long b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        long lcm = Math.abs(mulAndCheck(a / gcd(a, b), b));
        if (lcm == Long.MIN_VALUE) {
            throw MathRuntimeException.createArithmeticException(
                    "overflow: lcm({0}, {1}) is 2^63",
                    a, b);
        }
        return lcm;
    }

    /**
     * <p>Returns the
     * <a href="http://mathworld.wolfram.com/Logarithm.html">logarithm</a>
     * for base <code>b</code> of <code>x</code>.
     * </p>
     * <p>Returns <code>NaN<code> if either argument is negative.  If
     * <code>base</code> is 0 and <code>x</code> is positive, 0 is returned.
     * If <code>base</code> is positive and <code>x</code> is 0,
     * <code>Double.NEGATIVE_INFINITY</code> is returned.  If both arguments
     * are 0, the result is <code>NaN</code>.</p>
     *
     * @param base the base of the logarithm, must be greater than 0
     * @param x    argument, must be greater than 0
     * @return the value of the logarithm - the number y such that base^y = x.
     * @since 1.2
     */
    public static double log(double base, double x) {
        return Math.log(x) / Math.log(base);
    }

    /**
     * Multiply two integers, checking for overflow.
     *
     * @param x a factor
     * @param y a factor
     * @return the product <code>x*y</code>
     * @throws ArithmeticException if the result can not be represented as an
     *                             int
     * @since 1.1
     */
    public static int mulAndCheck(int x, int y) {
        long m = ((long) x) * ((long) y);
        if (m < Integer.MIN_VALUE || m > Integer.MAX_VALUE) {
            throw new ArithmeticException("overflow: mul");
        }
        return (int) m;
    }

    /**
     * Multiply two long integers, checking for overflow.
     *
     * @param a first value
     * @param b second value
     * @return the product <code>a * b</code>
     * @throws ArithmeticException if the result can not be represented as an
     *                             long
     * @since 1.2
     */
    public static long mulAndCheck(long a, long b) {
        long ret;
        String msg = "overflow: multiply";
        if (a > b) {
            // use symmetry to reduce boundary cases
            ret = mulAndCheck(b, a);
        } else {
            if (a < 0) {
                if (b < 0) {
                    // check for positive overflow with negative a, negative b
                    if (a >= Long.MAX_VALUE / b) {
                        ret = a * b;
                    } else {
                        throw new ArithmeticException(msg);
                    }
                } else if (b > 0) {
                    // check for negative overflow with negative a, positive b
                    if (Long.MIN_VALUE / b <= a) {
                        ret = a * b;
                    } else {
                        throw new ArithmeticException(msg);

                    }
                } else {
                    // assert b == 0
                    ret = 0;
                }
            } else if (a > 0) {
                // assert a > 0
                // assert b > 0

                // check for positive overflow with positive a, positive b
                if (a <= Long.MAX_VALUE / b) {
                    ret = a * b;
                } else {
                    throw new ArithmeticException(msg);
                }
            } else {
                // assert a == 0
                ret = 0;
            }
        }
        return ret;
    }

    /**
     * Get the next machine representable number after a number, moving
     * in the direction of another number.
     * <p>
     * If <code>direction</code> is greater than or equal to<code>d</code>,
     * the smallest machine representable number strictly greater than
     * <code>d</code> is returned; otherwise the largest representable number
     * strictly less than <code>d</code> is returned.</p>
     * <p>
     * If <code>d</code> is NaN or Infinite, it is returned unchanged.</p>
     *
     * @param d         base number
     * @param direction (the only important thing is whether
     *                  direction is greater or smaller than d)
     * @return the next machine representable number in the specified direction
     * @since 1.2
     */
    public static double nextAfter(double d, double direction) {

        // handling of some important special cases
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            return d;
        } else if (d == 0) {
            return (direction < 0) ? -Double.MIN_VALUE : Double.MIN_VALUE;
        }
        // special cases MAX_VALUE to infinity and  MIN_VALUE to 0
        // are handled just as normal numbers

        // split the double in raw components
        long bits = Double.doubleToLongBits(d);
        long sign = bits & 0x8000000000000000L;
        long exponent = bits & 0x7ff0000000000000L;
        long mantissa = bits & 0x000fffffffffffffL;

        if (d * (direction - d) >= 0) {
            // we should increase the mantissa
            if (mantissa == 0x000fffffffffffffL) {
                return Double.longBitsToDouble(sign |
                        (exponent + 0x0010000000000000L));
            } else {
                return Double.longBitsToDouble(sign |
                        exponent | (mantissa + 1));
            }
        } else {
            // we should decrease the mantissa
            if (mantissa == 0L) {
                return Double.longBitsToDouble(sign |
                        (exponent - 0x0010000000000000L) |
                        0x000fffffffffffffL);
            } else {
                return Double.longBitsToDouble(sign |
                        exponent | (mantissa - 1));
            }
        }

    }

    /**
     * Scale a number by 2<sup>scaleFactor</sup>.
     * <p>If <code>d</code> is 0 or NaN or Infinite, it is returned unchanged.</p>
     *
     * @param d           base number
     * @param scaleFactor power of two by which d sould be multiplied
     * @return d &times; 2<sup>scaleFactor</sup>
     * @since 2.0
     */
    public static double scalb(final double d, final int scaleFactor) {

        // handling of some important special cases
        if ((d == 0) || Double.isNaN(d) || Double.isInfinite(d)) {
            return d;
        }

        // split the double in raw components
        final long bits = Double.doubleToLongBits(d);
        final long exponent = bits & 0x7ff0000000000000L;
        final long rest = bits & 0x800fffffffffffffL;

        // shift the exponent
        final long newBits = rest | (exponent + (((long) scaleFactor) << 52));
        return Double.longBitsToDouble(newBits);

    }

    /**
     * Normalize an angle in a 2&pi wide interval around a center value.
     * <p>This method has three main uses:</p>
     * <ul>
     * <li>normalize an angle between 0 and 2&pi;:<br/>
     * <code>a = MathUtils.normalizeAngle(a, Math.PI);</code></li>
     * <li>normalize an angle between -&pi; and +&pi;<br/>
     * <code>a = MathUtils.normalizeAngle(a, 0.0);</code></li>
     * <li>compute the angle between two defining angular positions:<br>
     * <code>angle = MathUtils.normalizeAngle(end, start) - start;</code></li>
     * </ul>
     * <p>Note that due to numerical accuracy and since &pi; cannot be represented
     * exactly, the result interval is <em>closed</em>, it cannot be half-closed
     * as would be more satisfactory in a purely mathematical view.</p>
     *
     * @param a      angle to normalize
     * @param center center of the desired 2&pi; interval for the result
     * @return a-2k&pi; with integer k and center-&pi; &lt;= a-2k&pi; &lt;= center+&pi;
     * @since 1.2
     */
    public static double normalizeAngle(double a, double center) {
        return a - TWO_PI * Math.floor((a + Math.PI - center) / TWO_PI);
    }

    /**
     * <p>Normalizes an array to make it sum to a specified value.
     * Returns the result of the transformation <pre>
     *    x |-> x * normalizedSum / sum
     * </pre>
     * applied to each non-NaN element x of the input array, where sum is the
     * sum of the non-NaN entries in the input array.</p>
     *
     * <p>Throws IllegalArgumentException if <code>normalizedSum</code> is infinite
     * or NaN and ArithmeticException if the input array contains any infinite elements
     * or sums to 0</p>
     *
     * <p>Ignores (i.e., copies unchanged to the output array) NaNs in the input array.</p>
     *
     * @param values        input array to be normalized
     * @param normalizedSum target sum for the normalized array
     * @return normalized array
     * @throws ArithmeticException      if the input array contains infinite elements or sums to zero
     * @throws IllegalArgumentException if the target sum is infinite or NaN
     * @since 2.1
     */
    public static double[] normalizeArray(double[] values, double normalizedSum)
            throws ArithmeticException, IllegalArgumentException {
        if (Double.isInfinite(normalizedSum)) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "Cannot normalize to an infinite value");
        }
        if (Double.isNaN(normalizedSum)) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "Cannot normalize to NaN");
        }
        double sum = 0d;
        final int len = values.length;
        double[] out = new double[len];
        for (int i = 0; i < len; i++) {
            if (Double.isInfinite(values[i])) {
                throw MathRuntimeException.createArithmeticException(
                        "Array contains an infinite element, {0} at index {1}", values[i], i);
            }
            if (!Double.isNaN(values[i])) {
                sum += values[i];
            }
        }
        if (sum == 0) {
            throw MathRuntimeException.createArithmeticException(
                    "Array sums to zero");
        }
        for (int i = 0; i < len; i++) {
            if (Double.isNaN(values[i])) {
                out[i] = Double.NaN;
            } else {
                out[i] = values[i] * normalizedSum / sum;
            }
        }
        return out;
    }

    /**
     * Round the given value to the specified number of decimal places. The
     * value is rounded using the {@link BigDecimal#ROUND_HALF_UP} method.
     *
     * @param x     the value to round.
     * @param scale the number of digits to the right of the decimal point.
     * @return the rounded value.
     * @since 1.1
     */
    public static double round(double x, int scale) {
        return round(x, scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Round the given value to the specified number of decimal places. The
     * value is rounded using the given method which is any method defined in
     * {@link BigDecimal}.
     *
     * @param x              the value to round.
     * @param scale          the number of digits to the right of the decimal point.
     * @param roundingMethod the rounding method as defined in
     *                       {@link BigDecimal}.
     * @return the rounded value.
     * @since 1.1
     */
    public static double round(double x, int scale, int roundingMethod) {
        try {
            return (new BigDecimal
                    (Double.toString(x))
                    .setScale(scale, roundingMethod))
                    .doubleValue();
        } catch (NumberFormatException ex) {
            if (Double.isInfinite(x)) {
                return x;
            } else {
                return Double.NaN;
            }
        }
    }

    /**
     * Round the given value to the specified number of decimal places. The
     * value is rounding using the {@link BigDecimal#ROUND_HALF_UP} method.
     *
     * @param x     the value to round.
     * @param scale the number of digits to the right of the decimal point.
     * @return the rounded value.
     * @since 1.1
     */
    public static float round(float x, int scale) {
        return round(x, scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Round the given value to the specified number of decimal places. The
     * value is rounded using the given method which is any method defined in
     * {@link BigDecimal}.
     *
     * @param x              the value to round.
     * @param scale          the number of digits to the right of the decimal point.
     * @param roundingMethod the rounding method as defined in
     *                       {@link BigDecimal}.
     * @return the rounded value.
     * @since 1.1
     */
    public static float round(float x, int scale, int roundingMethod) {
        float sign = indicator(x);
        float factor = (float) Math.pow(10.0f, scale) * sign;
        return (float) roundUnscaled(x * factor, sign, roundingMethod) / factor;
    }

    /**
     * Round the given non-negative, value to the "nearest" integer. Nearest is
     * determined by the rounding method specified. Rounding methods are defined
     * in {@link BigDecimal}.
     *
     * @param unscaled       the value to round.
     * @param sign           the sign of the original, scaled value.
     * @param roundingMethod the rounding method as defined in
     *                       {@link BigDecimal}.
     * @return the rounded value.
     * @since 1.1
     */
    private static double roundUnscaled(double unscaled, double sign,
                                        int roundingMethod) {
        switch (roundingMethod) {
            case BigDecimal.ROUND_CEILING:
                if (sign == -1) {
                    unscaled = Math.floor(nextAfter(unscaled, Double.NEGATIVE_INFINITY));
                } else {
                    unscaled = Math.ceil(nextAfter(unscaled, Double.POSITIVE_INFINITY));
                }
                break;
            case BigDecimal.ROUND_DOWN:
                unscaled = Math.floor(nextAfter(unscaled, Double.NEGATIVE_INFINITY));
                break;
            case BigDecimal.ROUND_FLOOR:
                if (sign == -1) {
                    unscaled = Math.ceil(nextAfter(unscaled, Double.POSITIVE_INFINITY));
                } else {
                    unscaled = Math.floor(nextAfter(unscaled, Double.NEGATIVE_INFINITY));
                }
                break;
            case BigDecimal.ROUND_HALF_DOWN: {
                unscaled = nextAfter(unscaled, Double.NEGATIVE_INFINITY);
                double fraction = unscaled - Math.floor(unscaled);
                if (fraction > 0.5) {
                    unscaled = Math.ceil(unscaled);
                } else {
                    unscaled = Math.floor(unscaled);
                }
                break;
            }
            case BigDecimal.ROUND_HALF_EVEN: {
                double fraction = unscaled - Math.floor(unscaled);
                if (fraction > 0.5) {
                    unscaled = Math.ceil(unscaled);
                } else if (fraction < 0.5) {
                    unscaled = Math.floor(unscaled);
                } else {
                    // The following equality test is intentional and needed for rounding purposes
                    if (Math.floor(unscaled) / 2.0 == Math.floor(Math
                            .floor(unscaled) / 2.0)) { // even
                        unscaled = Math.floor(unscaled);
                    } else { // odd
                        unscaled = Math.ceil(unscaled);
                    }
                }
                break;
            }
            case BigDecimal.ROUND_HALF_UP: {
                unscaled = nextAfter(unscaled, Double.POSITIVE_INFINITY);
                double fraction = unscaled - Math.floor(unscaled);
                if (fraction >= 0.5) {
                    unscaled = Math.ceil(unscaled);
                } else {
                    unscaled = Math.floor(unscaled);
                }
                break;
            }
            case BigDecimal.ROUND_UNNECESSARY:
                if (unscaled != Math.floor(unscaled)) {
                    throw new ArithmeticException("Inexact result from rounding");
                }
                break;
            case BigDecimal.ROUND_UP:
                unscaled = Math.ceil(nextAfter(unscaled, Double.POSITIVE_INFINITY));
                break;
            default:
                throw MathRuntimeException.createIllegalArgumentException(
                        "invalid rounding method {0}, valid methods: {1} ({2}), {3} ({4})," +
                                " {5} ({6}), {7} ({8}), {9} ({10}), {11} ({12}), {13} ({14}), {15} ({16})",
                        roundingMethod,
                        "ROUND_CEILING", BigDecimal.ROUND_CEILING,
                        "ROUND_DOWN", BigDecimal.ROUND_DOWN,
                        "ROUND_FLOOR", BigDecimal.ROUND_FLOOR,
                        "ROUND_HALF_DOWN", BigDecimal.ROUND_HALF_DOWN,
                        "ROUND_HALF_EVEN", BigDecimal.ROUND_HALF_EVEN,
                        "ROUND_HALF_UP", BigDecimal.ROUND_HALF_UP,
                        "ROUND_UNNECESSARY", BigDecimal.ROUND_UNNECESSARY,
                        "ROUND_UP", BigDecimal.ROUND_UP);
        }
        return unscaled;
    }

    /**
     * Returns the <a href="http://mathworld.wolfram.com/Sign.html"> sign</a>
     * for byte value <code>x</code>.
     * <p>
     * For a byte value x, this method returns (byte)(+1) if x > 0, (byte)(0) if
     * x = 0, and (byte)(-1) if x < 0.</p>
     *
     * @param x the value, a byte
     * @return (byte)(+1), (byte)(0), or (byte)(-1), depending on the sign of x
     */
    public static byte sign(final byte x) {
        return (x == ZB) ? ZB : (x > ZB) ? PB : NB;
    }

    /**
     * Returns the <a href="http://mathworld.wolfram.com/Sign.html"> sign</a>
     * for double precision <code>x</code>.
     * <p>
     * For a double value <code>x</code>, this method returns
     * <code>+1.0</code> if <code>x > 0</code>, <code>0.0</code> if
     * <code>x = 0.0</code>, and <code>-1.0</code> if <code>x < 0</code>.
     * Returns <code>NaN</code> if <code>x</code> is <code>NaN</code>.</p>
     *
     * @param x the value, a double
     * @return +1.0, 0.0, or -1.0, depending on the sign of x
     */
    public static double sign(final double x) {
        if (Double.isNaN(x)) {
            return Double.NaN;
        }
        return (x == 0.0) ? 0.0 : (x > 0.0) ? 1.0 : -1.0;
    }

    /**
     * Returns the <a href="http://mathworld.wolfram.com/Sign.html"> sign</a>
     * for float value <code>x</code>.
     * <p>
     * For a float value x, this method returns +1.0F if x > 0, 0.0F if x =
     * 0.0F, and -1.0F if x < 0. Returns <code>NaN</code> if <code>x</code>
     * is <code>NaN</code>.</p>
     *
     * @param x the value, a float
     * @return +1.0F, 0.0F, or -1.0F, depending on the sign of x
     */
    public static float sign(final float x) {
        if (Float.isNaN(x)) {
            return Float.NaN;
        }
        return (x == 0.0F) ? 0.0F : (x > 0.0F) ? 1.0F : -1.0F;
    }

    /**
     * Returns the <a href="http://mathworld.wolfram.com/Sign.html"> sign</a>
     * for int value <code>x</code>.
     * <p>
     * For an int value x, this method returns +1 if x > 0, 0 if x = 0, and -1
     * if x < 0.</p>
     *
     * @param x the value, an int
     * @return +1, 0, or -1, depending on the sign of x
     */
    public static int sign(final int x) {
        return (x == 0) ? 0 : (x > 0) ? 1 : -1;
    }

    /**
     * Returns the <a href="http://mathworld.wolfram.com/Sign.html"> sign</a>
     * for long value <code>x</code>.
     * <p>
     * For a long value x, this method returns +1L if x > 0, 0L if x = 0, and
     * -1L if x < 0.</p>
     *
     * @param x the value, a long
     * @return +1L, 0L, or -1L, depending on the sign of x
     */
    public static long sign(final long x) {
        return (x == 0L) ? 0L : (x > 0L) ? 1L : -1L;
    }

    /**
     * Returns the <a href="http://mathworld.wolfram.com/Sign.html"> sign</a>
     * for short value <code>x</code>.
     * <p>
     * For a short value x, this method returns (short)(+1) if x > 0, (short)(0)
     * if x = 0, and (short)(-1) if x < 0.</p>
     *
     * @param x the value, a short
     * @return (short)(+1), (short)(0), or (short)(-1), depending on the sign of
     *         x
     */
    public static short sign(final short x) {
        return (x == ZS) ? ZS : (x > ZS) ? PS : NS;
    }

    /**
     * Returns the <a href="http://mathworld.wolfram.com/HyperbolicSine.html">
     * hyperbolic sine</a> of x.
     *
     * @param x double value for which to find the hyperbolic sine
     * @return hyperbolic sine of x
     */
    public static double sinh(double x) {
        return (Math.exp(x) - Math.exp(-x)) / 2.0;
    }

    /**
     * Subtract two integers, checking for overflow.
     *
     * @param x the minuend
     * @param y the subtrahend
     * @return the difference <code>x-y</code>
     * @throws ArithmeticException if the result can not be represented as an
     *                             int
     * @since 1.1
     */
    public static int subAndCheck(int x, int y) {
        long s = (long) x - (long) y;
        if (s < Integer.MIN_VALUE || s > Integer.MAX_VALUE) {
            throw new ArithmeticException("overflow: subtract");
        }
        return (int) s;
    }

    /**
     * Subtract two long integers, checking for overflow.
     *
     * @param a first value
     * @param b second value
     * @return the difference <code>a-b</code>
     * @throws ArithmeticException if the result can not be represented as an
     *                             long
     * @since 1.2
     */
    public static long subAndCheck(long a, long b) {
        long ret;
        String msg = "overflow: subtract";
        if (b == Long.MIN_VALUE) {
            if (a < 0) {
                ret = a - b;
            } else {
                throw new ArithmeticException(msg);
            }
        } else {
            // use additive inverse
            ret = addAndCheck(a, -b, msg);
        }
        return ret;
    }

    /**
     * Raise an int to an int power.
     *
     * @param k number to raise
     * @param e exponent (must be positive or null)
     * @return k<sup>e</sup>
     * @throws IllegalArgumentException if e is negative
     */
    public static int pow(final int k, int e)
            throws IllegalArgumentException {

        if (e < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "cannot raise an integral value to a negative power ({0}^{1})",
                    k, e);
        }

        int result = 1;
        int k2p = k;
        while (e != 0) {
            if ((e & 0x1) != 0) {
                result *= k2p;
            }
            k2p *= k2p;
            e = e >> 1;
        }

        return result;

    }

    /**
     * Raise an int to a long power.
     *
     * @param k number to raise
     * @param e exponent (must be positive or null)
     * @return k<sup>e</sup>
     * @throws IllegalArgumentException if e is negative
     */
    public static int pow(final int k, long e)
            throws IllegalArgumentException {

        if (e < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "cannot raise an integral value to a negative power ({0}^{1})",
                    k, e);
        }

        int result = 1;
        int k2p = k;
        while (e != 0) {
            if ((e & 0x1) != 0) {
                result *= k2p;
            }
            k2p *= k2p;
            e = e >> 1;
        }

        return result;

    }

    /**
     * Raise a long to an int power.
     *
     * @param k number to raise
     * @param e exponent (must be positive or null)
     * @return k<sup>e</sup>
     * @throws IllegalArgumentException if e is negative
     */
    public static long pow(final long k, int e)
            throws IllegalArgumentException {

        if (e < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "cannot raise an integral value to a negative power ({0}^{1})",
                    k, e);
        }

        long result = 1l;
        long k2p = k;
        while (e != 0) {
            if ((e & 0x1) != 0) {
                result *= k2p;
            }
            k2p *= k2p;
            e = e >> 1;
        }

        return result;

    }

    /**
     * Raise a long to a long power.
     *
     * @param k number to raise
     * @param e exponent (must be positive or null)
     * @return k<sup>e</sup>
     * @throws IllegalArgumentException if e is negative
     */
    public static long pow(final long k, long e)
            throws IllegalArgumentException {

        if (e < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "cannot raise an integral value to a negative power ({0}^{1})",
                    k, e);
        }

        long result = 1l;
        long k2p = k;
        while (e != 0) {
            if ((e & 0x1) != 0) {
                result *= k2p;
            }
            k2p *= k2p;
            e = e >> 1;
        }

        return result;

    }

    /**
     * Raise a BigInteger to an int power.
     *
     * @param k number to raise
     * @param e exponent (must be positive or null)
     * @return k<sup>e</sup>
     * @throws IllegalArgumentException if e is negative
     */
    public static BigInteger pow(final BigInteger k, int e)
            throws IllegalArgumentException {

        if (e < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "cannot raise an integral value to a negative power ({0}^{1})",
                    k, e);
        }

        return k.pow(e);

    }

    /**
     * Raise a BigInteger to a long power.
     *
     * @param k number to raise
     * @param e exponent (must be positive or null)
     * @return k<sup>e</sup>
     * @throws IllegalArgumentException if e is negative
     */
    public static BigInteger pow(final BigInteger k, long e)
            throws IllegalArgumentException {

        if (e < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "cannot raise an integral value to a negative power ({0}^{1})",
                    k, e);
        }

        BigInteger result = BigInteger.ONE;
        BigInteger k2p = k;
        while (e != 0) {
            if ((e & 0x1) != 0) {
                result = result.multiply(k2p);
            }
            k2p = k2p.multiply(k2p);
            e = e >> 1;
        }

        return result;

    }

    /**
     * Raise a BigInteger to a BigInteger power.
     *
     * @param k number to raise
     * @param e exponent (must be positive or null)
     * @return k<sup>e</sup>
     * @throws IllegalArgumentException if e is negative
     */
    public static BigInteger pow(final BigInteger k, BigInteger e)
            throws IllegalArgumentException {

        if (e.compareTo(BigInteger.ZERO) < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "cannot raise an integral value to a negative power ({0}^{1})",
                    k, e);
        }

        BigInteger result = BigInteger.ONE;
        BigInteger k2p = k;
        while (!BigInteger.ZERO.equals(e)) {
            if (e.testBit(0)) {
                result = result.multiply(k2p);
            }
            k2p = k2p.multiply(k2p);
            e = e.shiftRight(1);
        }

        return result;

    }

    /**
     * Calculates the L<sub>1</sub> (sum of abs) distance between two points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the L<sub>1</sub> distance between the two points
     */
    public static double distance1(double[] p1, double[] p2) {
        double sum = 0;
        for (int i = 0; i < p1.length; i++) {
            sum += Math.abs(p1[i] - p2[i]);
        }
        return sum;
    }

    /**
     * Calculates the L<sub>1</sub> (sum of abs) distance between two points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the L<sub>1</sub> distance between the two points
     */
    public static int distance1(int[] p1, int[] p2) {
        int sum = 0;
        for (int i = 0; i < p1.length; i++) {
            sum += Math.abs(p1[i] - p2[i]);
        }
        return sum;
    }

    /**
     * Calculates the L<sub>2</sub> (Euclidean) distance between two points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the L<sub>2</sub> distance between the two points
     */
    public static double distance(double[] p1, double[] p2) {
        double sum = 0;
        for (int i = 0; i < p1.length; i++) {
            final double dp = p1[i] - p2[i];
            sum += dp * dp;
        }
        return Math.sqrt(sum);
    }

    /**
     * Calculates the L<sub>2</sub> (Euclidean) distance between two points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the L<sub>2</sub> distance between the two points
     */
    public static double distance(int[] p1, int[] p2) {
        double sum = 0;
        for (int i = 0; i < p1.length; i++) {
            final double dp = p1[i] - p2[i];
            sum += dp * dp;
        }
        return Math.sqrt(sum);
    }

    /**
     * Calculates the L<sub>&infin;</sub> (max of abs) distance between two points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the L<sub>&infin;</sub> distance between the two points
     */
    public static double distanceInf(double[] p1, double[] p2) {
        double max = 0;
        for (int i = 0; i < p1.length; i++) {
            max = Math.max(max, Math.abs(p1[i] - p2[i]));
        }
        return max;
    }

    /**
     * Calculates the L<sub>&infin;</sub> (max of abs) distance between two points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the L<sub>&infin;</sub> distance between the two points
     */
    public static int distanceInf(int[] p1, int[] p2) {
        int max = 0;
        for (int i = 0; i < p1.length; i++) {
            max = Math.max(max, Math.abs(p1[i] - p2[i]));
        }
        return max;
    }

    /**
     * Checks that the given array is sorted.
     *
     * @param val    Values
     * @param dir    Order direction (-1 for decreasing, 1 for increasing)
     * @param strict Whether the order should be strict
     * @throws IllegalArgumentException if the array is not sorted.
     */
    public static void checkOrder(double[] val, int dir, boolean strict) {
        double previous = val[0];

        int max = val.length;
        for (int i = 1; i < max; i++) {
            if (dir > 0) {
                if (strict) {
                    if (val[i] <= previous) {
                        throw MathRuntimeException.createIllegalArgumentException("points {0} and {1} are not strictly increasing ({2} >= {3})",
                                i - 1, i, previous, val[i]);
                    }
                } else {
                    if (val[i] < previous) {
                        throw MathRuntimeException.createIllegalArgumentException("points {0} and {1} are not increasing ({2} > {3})",
                                i - 1, i, previous, val[i]);
                    }
                }
            } else {
                if (strict) {
                    if (val[i] >= previous) {
                        throw MathRuntimeException.createIllegalArgumentException("points {0} and {1} are not strictly decreasing ({2} <= {3})",
                                i - 1, i, previous, val[i]);
                    }
                } else {
                    if (val[i] > previous) {
                        throw MathRuntimeException.createIllegalArgumentException("points {0} and {1} are not decreasing ({2} < {3})",
                                i - 1, i, previous, val[i]);
                    }
                }
            }

            previous = val[i];
        }
    }
}
=======

// $Id$

package net.sf.persist.tests.framework;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

/**
 * Set of helpers to build and manipulate beans at runtime.
 */
public class DynamicBean {
	
	public static Class createBeanClass(BeanMap beanMap, boolean noTable) {

		ClassPool pool = ClassPool.getDefault();
		CtClass cc = pool.makeClass("net.sf.persist.tests.generated." 
				+ beanMap.getClassName() + (noTable ? "NoTable" : ""));

		if (noTable) {	
			ClassFile cf = cc.getClassFile();
			ConstPool cp = cf.getConstPool();
			AnnotationsAttribute attr = new AnnotationsAttribute(cp, AnnotationsAttribute.visibleTag);
			Annotation a;
			try {
				a = new Annotation(cp, pool.get("net.sf.persist.annotations.NoTable"));
			} catch (NotFoundException e) {
				throw new RuntimeException(e);
			}
			attr.setAnnotation(a);
			cf.addAttribute(attr);
			cf.setVersionToJava5();
		}
		
		try {

			for (FieldMap fieldMap : beanMap.getFields()) {

				String fieldName = fieldMap.getFieldName();
				Class fieldType = fieldMap.getTypes().get(0);

				String fieldNameU = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
				String fieldTypeName = fieldType.getCanonicalName();

				String getterCode = "public " + fieldTypeName + " get" + fieldNameU + "() { return " + fieldName + "; }";
				String setterCode = "public void set" + fieldNameU + "(" + fieldTypeName + " " + fieldName + ") { this." + fieldName + "=" + fieldName + "; }";

				CtField cf = new CtField(pool.get(fieldTypeName), fieldName, cc);
				cc.addField(cf);

				CtMethod cm = CtNewMethod.make(getterCode, cc);
				cc.addMethod(cm);

				cm = CtNewMethod.make(setterCode, cc);
				cc.addMethod(cm);
			}

			String toStringCode = "public String toString() { return net.sf.persist.tests.framework.DynamicBean.toString(this); }";
			CtMethod cm = CtNewMethod.make(toStringCode, cc);
			cc.addMethod(cm);

			String equalsCode = "public boolean equals(Object obj) { return net.sf.persist.tests.framework.DynamicBean.compareBeans(this,obj); }";
			cm = CtNewMethod.make(equalsCode, cc);
			cc.addMethod(cm);

			return cc.toClass();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Object createInstance(Class cls, BeanMap beanMap, boolean useNulls) {
		Object obj = null;
		try {
			obj = cls.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		for (Field field : cls.getDeclaredFields()) {
			FieldMap fieldMap = beanMap.getField(field.getName());
			setRandomValue(obj, field, fieldMap, useNulls);
		}
		return obj;
	}

	private static void setRandomValue(Object obj, Field field, FieldMap fieldMap, boolean useNull) {

		Class fieldType = field.getType();
		int size = fieldMap.getSize();
		double min = fieldMap.getMin();
		double max = fieldMap.getMax();

		Object value = null;

		if (fieldType == Boolean.class)
			value = useNull ? null : new Boolean(randomBoolean());
		else if (fieldType == boolean.class)
			value = useNull ? false : randomBoolean();
		else if (fieldType == Byte.class)
			value = useNull ? null : new Byte(randomByte((byte)min, (byte)max));
		else if (fieldType == byte.class)
			value = useNull ? (byte) 0 : randomByte((byte)min, (byte)max);
		else if (fieldType == Byte[].class)
			value = useNull ? null : randomByteObjArray(size);
		else if (fieldType == byte[].class)
			value = useNull ? null : randomByteArray(size);
		else if (fieldType == Short.class)
			value = useNull ? null : new Short(randomShort((short)min, (short)max));
		else if (fieldType == short.class)
			value = useNull ? (short) 0 : randomShort((short)min, (short)max);
		else if (fieldType == Integer.class)
			value = useNull ? null : new Integer(randomInt((int)min, (int)max));
		else if (fieldType == int.class)
			value = useNull ? (int) 0 : randomInt((int)min, (int)max);
		else if (fieldType == Long.class)
			value = useNull ? null : new Long(randomLong((long)min, (long)max));
		else if (fieldType == long.class)
			value = useNull ? (long) 0 : randomLong((long)min, (long)max);
		else if (fieldType == Float.class)
			value = useNull ? null : new Float(randomFloat((float)min, (float)max));
		else if (fieldType == float.class)
			value = useNull ? (float) 0 : randomFloat((float)min, (float)max);
		else if (fieldType == Double.class)
			value = useNull ? null : new Double(randomDouble(min, max));
		else if (fieldType == double.class)
			value = useNull ? (double) 0 : randomDouble(min, max);
		else if (fieldType == Character.class)
			value = useNull ? null : new Character(randomChar());
		else if (fieldType == char.class)
			value = useNull ? ' ' : randomChar();
		else if (fieldType == Character[].class)
			value = useNull ? null : randomCharObjArray(size);
		else if (fieldType == char[].class)
			value = useNull ? null : randomCharArray(size);
		else if (fieldType == String.class)
			value = useNull ? null : randomString(size);
		else if (fieldType == BigDecimal.class)
			value = useNull ? null : new BigDecimal(randomLong((long)min, (long)max));
		else if (fieldType == java.io.Reader.class)
			value = useNull ? null : new StringReader(randomString(size));
		else if (fieldType == java.io.InputStream.class)
			value = useNull ? null : new ByteArrayInputStream(randomByteArray(size));
		else if (fieldType == java.util.Date.class)
			value = useNull ? null : new java.util.Date(randomTimestamp());
		else if (fieldType == java.sql.Date.class)
			value = useNull ? null : new java.sql.Date(randomTimestamp());
		else if (fieldType == java.sql.Time.class)
			value = useNull ? null : new java.sql.Time(randomTimestamp());
		else if (fieldType == java.sql.Timestamp.class)
			value = useNull ? null : new java.sql.Timestamp(randomTimestamp());
		else if (fieldType == java.sql.Blob.class)
			value = useNull ? null : new BytesBlob(randomByteArray(size));
		else if (fieldType == java.sql.Clob.class)
			value = useNull ? null : new StringClob(randomString(size));
		else {
			if (useNull)
				value = null;
			else {
				Map m = new HashMap();
				m.put(randomString(3), randomString(32));
				m.put(randomString(3), randomString(32));
				m.put(randomString(3), randomString(32));
				value = m;
			}
		}

		try {
			String fieldName = field.getName();
			Field f = obj.getClass().getDeclaredField(fieldName);
			f.setAccessible(true);
			f.set(obj, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public static Object getFieldValue(Object obj, String fieldName) {
		try {
			Field f = obj.getClass().getDeclaredField(fieldName);
			f.setAccessible(true);
			return f.get(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String toString(Object obj) {
		if (obj == null)
			return "null";
		StringBuffer sb = new StringBuffer();
		sb.append("{ ");
		for (Field field : obj.getClass().getDeclaredFields()) {
			String fieldName = field.getName();

			Object value = getFieldValue(obj, fieldName);
			String s = value == null ? "null" : value.toString();
			if (s.length() > 32)
				s = s.substring(0, 32) + "...";

			sb.append(fieldName + "=" + s + ", ");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.deleteCharAt(sb.length() - 1);
		sb.append(" }");
		return sb.toString();
	}
	
	/**
	 * Returns true if the field is a primitive number type (byte, short, int, etc.) and its value is zero,
	 * or if the field is an object and its value is null
	 */
	public static boolean isNull(Class cls, Object obj) {
		
		if (obj==null) return true;
		
		if (cls==boolean.class || cls==Boolean.class)
			return ((Boolean)obj).booleanValue()==false;
		else if (cls==byte.class || cls==Byte.class || cls==short.class || cls==Short.class 
				|| cls==int.class || cls==Integer.class || cls==long.class || cls==Long.class 
				|| cls==float.class || cls==Float.class  || cls==double.class || cls==Double.class 
				|| cls==BigDecimal.class) {
			
			// first cast to Number
			Number n = (Number) obj;
			return n.longValue()==0;
		}
		else 
			return false;
	}

	public static boolean compareBeans(Object o1, Object o2) {

		if (o1 == null && o2 == null)
			return true;
		if (o1 == o2)
			return true;
		if (o1 == null && o2 != null)
			return false;
		if (o1 != null && o2 == null)
			return false;
		if (o1.getClass() != o2.getClass())
			return false;

		try {

			for (Field field : o1.getClass().getDeclaredFields()) {
				field.setAccessible(true);
				Object v1 = field.get(o1);
				Object v2 = field.get(o2);
				if (!compareValues(v1, v2))
					return false;
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return true;
	}
	
	
	public static boolean compareBeansFromDifferentClasses(Object o1, Object o2) {

		if (o1 == null && o2 == null)
			return true;
		if (o1 == o2)
			return true;
		if (o1 == null && o2 != null)
			return false;
		if (o1 != null && o2 == null)
			return false;

		try {

			for (Field f1 : o1.getClass().getDeclaredFields()) {
				f1.setAccessible(true);
				Object v1 = f1.get(o1);
				
				Field f2;
				try {
					f2 = o2.getClass().getDeclaredField(f1.getName());
				} catch (NoSuchFieldException e) {
					return false;
				}
				f2.setAccessible(true);
				Object v2 = f2.get(o2);
				
				if (!compareValues(v1, v2))
					return false;
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return true;
	}

	
	

	/**
	 * Compare values trying to convert types if they are found to be compatible
	 */
	public static boolean compareValues(Object v1, Object v2) {

		if (v1 == null && v2 == null)
			return true;
		if (v1 == v2)
			return true;
		if (v1 == null && v2 != null)
			return false;
		if (v1 != null && v2 == null)
			return false;

		// try to convert v2 into v1 type
		v2 = convertToType(v1.getClass(), v2);

		if (v1.getClass() != v2.getClass())
			return false;

		Class type = v1.getClass();

		try {

			if (type == Boolean.class || type == boolean.class) {
				if (!((Boolean) v1).equals((Boolean) v2))
					return false;
			} else if (type == Byte.class || type == byte.class) {
				if (!((Byte) v1).equals((Byte) v2))
					return false;
			} else if (type == Byte[].class) {
				if (!Arrays.equals((Byte[]) v1, (Byte[]) v2))
					return false;
			} else if (type == byte[].class) {
				if (!Arrays.equals((byte[]) v1, (byte[]) v2))
					return false;
			} else if (type == Short.class || type == short.class) {
				if (!((Short) v1).equals((Short) v2))
					return false;
			} else if (type == Integer.class || type == int.class) {
				if (!((Integer) v1).equals((Integer) v2))
					return false;
			} else if (type == Long.class || type == long.class) {
				if (!((Long) v1).equals((Long) v2))
					return false;
			} else if (type == Float.class || type == float.class) {
				Float v1f = (Float) v1;
				Float v2f = (Float) v2;
				if (Float.floatToIntBits(v1f) != Float.floatToIntBits(v2f))
					return false;
			} else if (type == Double.class || type == double.class) {
				Double v1d = (Double) v1;
				Double v2d = (Double) v2;
				if (Double.doubleToLongBits(v1d) != Double.doubleToLongBits(v2d))
					return false;
			} else if (type == Character.class || type == char.class) {
				if (!((Character) v1).equals((Character) v2))
					return false;
			} else if (type == Character[].class) {
				if (!Arrays.equals((Character[]) v1, (Character[]) v2))
					return false;
			} else if (type == char[].class) {
				if (!Arrays.equals((char[]) v1, (char[]) v2))
					return false;
			} else if (type == String.class) {
				if (!((String) v1).equals((String) v2))
					return false;
			} else if (type == BigDecimal.class) {
				if (!((BigDecimal) v1).equals((BigDecimal) v2))
					return false;
			} else if (type == Reader.class) {
				Reader r1 = (Reader) v1;
				Reader r2 = (Reader) v2;
				if (!compareReaders(r1,r2))
					return false;
			} else if (v1  == InputStream.class) {
				InputStream i1 = (InputStream) v1;
				InputStream i2 = (InputStream) v2;
				if (!compareInputStreams(i1,i2))
					return false;
			} else if (v1 instanceof Clob) {
				Clob c1 = (Clob) v1;
				Clob c2 = (Clob) v2;
				if (!compareReaders(c1.getCharacterStream(), c2.getCharacterStream()))
					return false;
			} else if (v1 instanceof Blob) {
				Blob b1 = (Blob) v1;
				Blob b2 = (Blob) v2;
				if (!compareInputStreams(b1.getBinaryStream(), b2.getBinaryStream()))
					return false;
			} else if (type == java.util.Date.class) {
				java.util.Date d1 = (java.util.Date) v1;
				java.util.Date d2 = (java.util.Date) v2;
				if (!d1.toString().substring(0,19).equals(d2.toString().substring(0,19)))
					return false;
			} else if (type == java.sql.Date.class) {
				java.sql.Date d1 = (java.sql.Date) v1;
				java.sql.Date d2 = (java.sql.Date) v2;
				if (!d1.toString().equals(d2.toString()))
					return false;
			} else if (type == java.sql.Time.class) {
				java.sql.Time d1 = (java.sql.Time) v1;
				java.sql.Time d2 = (java.sql.Time) v2;
				if (!d1.toString().equals(d2.toString()))
					return false;
			} else if (type == java.sql.Timestamp.class) {
				java.sql.Timestamp d1 = (java.sql.Timestamp) v1;
				java.sql.Timestamp d2 = (java.sql.Timestamp) v2;
				// quick fix for smalldatetimes is to compare up to 15, instead of 19
				if (!d1.toString().substring(0,15).equals(d2.toString().substring(0,15)))
					return false;
			} else if (!v1.equals(v2))
				return false;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return true;
	}
	
	/**
	 * Try to convert the provided object to the provided class. 
	 * The following groups of types allow for conversion among their types:
	 * { String, char, Character, char[], Character[], Reader }
	 * { byte[], Byte[], InputStream }
	 * { boolean, Boolean }
	 * { byte, Byte, short, Short, int, Integer, long, Long, float, Float, double, Double, BigDecimal }
	 * { java.sql.Timestamp, java.sql.Date, java.sql.Time, java.util.Date }
	 */
	public static Object convertToType(Class cls, Object value) {
		
		if (cls == value.getClass()) return value;
		
		// if cls implements Clob or Blob, upcast
		for (Class iface : cls.getInterfaces()) {
			if (iface==Clob.class) {
				cls = Clob.class;
				break;
			}
			else if (iface==Blob.class) {
				cls = Blob.class;
				break;
			}
		}
		
		Class clsValue = value.getClass();

		if (cls==String.class || cls==char.class || cls==Character.class 
				|| cls==char[].class || cls==Character[].class || cls==Reader.class || cls==Clob.class) {
			
			// first convert it to string
			
			String s = null;
			
			if (clsValue==String.class) {
				s = (String) value;
			}
			else if (clsValue==char.class || clsValue==Character.class) {
				s = "" + value;
			}
			else if (clsValue==char[].class) {
				s = String.copyValueOf((char[])value);
			}
			else if (clsValue==Character[].class) { 
				Character[] a = (Character[])value; 
				char[] ac = new char[a.length];
				for (int i=0; i<a.length; i++) ac[i] = a[i];
				s = String.copyValueOf(ac);
			}
			else if (value instanceof Reader) {
				Reader r2 = (Reader)value;
				s = readReader(r2);
			}
			else if (value instanceof Clob) {
				Clob c2 = (Clob)value;
				try {
					s = readReader(c2.getCharacterStream());
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
			else {
				return value;
			}
			
			// now convert it to the target type
			
			if (cls==String.class) {
				return s;
			}
			else if (cls==char.class) {
				if (s.length()==1) return s.charAt(0);
				else return value;
			}
			else if (cls==Character.class) {
				if (s.length()==1) return new Character(s.charAt(0));
				else return value;
			}
			else if (cls==char[].class) {
				return s.toCharArray();
			}
			else if (cls==Character[].class) {
				Character[] ret = new Character[s.length()];
				for (int i=0; i<s.length(); i++) ret[i] = new Character(s.charAt(i));
				return ret;
			}
			else if (cls==Reader.class) {
				return new StringReader(s);
			}
			else if (cls==Clob.class) {
				return new StringClob(s);
			}
		}
		
		else if (cls==byte[].class || cls==Byte[].class || cls==InputStream.class || cls==Blob.class) {
			
			// first convert to byte[]
			
			byte[] a = null;
			if (clsValue==byte[].class) {
				a = (byte[]) value;
			}
			else if (clsValue==Byte[].class) {
				Byte[] ba = (Byte[]) value;
				a = new byte[ba.length];
				for (int i=0; i<ba.length; i++) a[i]=ba[i];
			}
			else if (value instanceof InputStream) {
				InputStream is = (InputStream) value;
				a = readInputStream(is);
			}
			else if (value instanceof Blob) {
				Blob b = (Blob) value;
				try {
					a = readInputStream(b.getBinaryStream());
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
			else return value;
			
			// now convert to target class
			
			if (cls==byte[].class) {
				return a;
			}
			else if (cls==Byte[].class) {
				Byte[] ba = new Byte[a.length];
				for (int i=0; i<a.length; i++) ba[i]=a[i];
				return ba;
			}
			else if (cls==InputStream.class) {
				return new ByteArrayInputStream(a);
			}
			else if (cls==Blob.class) {
				return new BytesBlob(a);
			}
		}
		
		else if (clsValue==java.util.Date.class) {
			java.util.Date d = (java.util.Date)value;
			if (cls==java.sql.Date.class) {
				return new java.sql.Date(d.getTime());
			}
			if (cls==java.sql.Time.class) {
				return new java.sql.Time(d.getTime());
			}
			if (cls==java.sql.Timestamp.class) {
				return new java.sql.Timestamp(d.getTime());
			}
			else return value;
		}
		
		else if (cls==java.util.Date.class) {
			if (clsValue==java.sql.Date.class) {
				return new java.util.Date(((java.sql.Date)value).getTime());
			}
			if (clsValue==java.sql.Time.class) {
				return new java.util.Date(((java.util.Date)value).getTime());
			}
			if (clsValue==java.sql.Timestamp.class) {
				return new java.util.Date(((java.util.Date)value).getTime());
			}
			else return value;
		}
		
		else if (clsValue==boolean.class || clsValue==Boolean.class) {
			Boolean b = (Boolean) value;
			if (cls==boolean.class) return b.booleanValue();
			else if (cls==Boolean.class) return b;
			else return value;
		}
		
		else if (clsValue==byte.class || clsValue==Byte.class || clsValue==short.class || clsValue==Short.class 
				|| clsValue==int.class || clsValue==Integer.class || clsValue==long.class || clsValue==Long.class 
				|| clsValue==float.class || clsValue==Float.class  || clsValue==double.class || clsValue==Double.class 
				|| clsValue==BigDecimal.class) {
			
			// first cast to Number
			Number n = (Number) value;
			
			if (cls==byte.class) return n.byteValue();
			else if (cls==Byte.class) return new Byte(n.byteValue());
			else if (cls==short.class) return n.shortValue();
			else if (cls==Short.class) return new Short(n.shortValue());
			else if (cls==int.class) return n.intValue();
			else if (cls==Integer.class) return new Integer(n.intValue());
			else if (cls==long.class) return n.longValue();
			else if (cls==Long.class) return new Long(n.longValue());
			else if (cls==float.class) return n.floatValue();
			else if (cls==Float.class) return new Float(n.floatValue());
			else if (cls==double.class) return n.doubleValue();
			else if (cls==Double.class) return new Double(n.doubleValue());
			else if (cls==BigDecimal.class) return BigDecimal.valueOf(n.doubleValue());
			else return value;
		}
		
		return value;
	}
	
	public static byte[] readInputStream(InputStream is) {
		// assumes no more than 64KB of data
		try {
			byte[] buf = new byte[65535];
			int n = is.read(buf);
			if (n<0) n=0;
			byte[] ret = new byte[n];
			for (int i=0; i<n; i++) ret[i] = buf[i];
			return ret;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String readReader(Reader r) {
		char[] buf = new char[65535];
		try {
			int n = r.read(buf);
			return new String(buf, 0, n);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static boolean compareReaders(Reader r1, Reader r2) {
		char[] buf1 = new char[65535];
		char[] buf2 = new char[65535];
		try {
			int n1 = r1.read(buf1);
			int n2 = r2.read(buf2);
			return (n1 == n2 && Arrays.equals(buf1, buf2));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static boolean compareInputStreams(InputStream i1, InputStream i2) {
		byte[] buf1 = new byte[65535];
		byte[] buf2 = new byte[65535];
		try {
			int n1 = i1.read(buf1);
			int n2 = i2.read(buf2);
			return (n1 == n2 && Arrays.equals(buf1, buf2));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// ---------- create random values ----------

	public static boolean randomBoolean() {
		return Math.random() > 0.5;
	}

	public static byte randomByte(byte min, byte max) {
		if (min==-1 && max==-1) {
			min = Byte.MIN_VALUE;
			max = Byte.MAX_VALUE;
		}
		return (byte) (min+Math.random()*(max-min));
	}

	public static byte[] randomByteArray(int size) {
		byte[] a = new byte[size];
		for (int i = 0; i < size; i++)
			a[i] = randomByte(Byte.MIN_VALUE, Byte.MAX_VALUE);
		return a;
	}

	public static Byte[] randomByteObjArray(int size) {
		Byte[] a = new Byte[size];
		for (int i = 0; i < size; i++)
			a[i] = new Byte(randomByte(Byte.MIN_VALUE, Byte.MAX_VALUE));
		return a;
	}

	public static short randomShort(short min, short max) {
		if (min==-1 && max==-1) {
			min = Short.MIN_VALUE;
			max = Short.MAX_VALUE;
		}
		return (short) (min+Math.random()*(max-min));
	}

	public static int randomInt(int min, int max) {
		if (min==-1 && max==-1) {
			min = Integer.MIN_VALUE;
			max = Integer.MAX_VALUE;
		}
		return (int) (min+Math.random()*(max-min));
	}

	public static long randomLong(long min, long max) {
		if (min==-1 && max==-1) {
			min = Long.MIN_VALUE;
			max = Long.MAX_VALUE;
		}
		return (long) (min+Math.random()*(max-min));
	}

	public static float randomFloat(float min, float max) {
		if (min==-1 && max==-1) {
			min = Float.MIN_VALUE;
			max = Float.MAX_VALUE;
		}
		//double f = (int) (Math.random() * 10);
		double f = 0;
		return (float) ( ((long)(min+Math.random()*(max-min))) + f/10 );
	}

	public static double randomDouble(double min, double max) {
		if (min==-1 && max==-1) {
			min = Double.MIN_VALUE;
			max = Double.MAX_VALUE;
		}
		double f = (int) (Math.random() * 10);
		return ((long)(min+Math.random()*(max-min))) + f/10;
	}

	public static char randomChar() {
		byte[] b = new byte[] { (byte) (97 + (byte) (Math.random() * 26)) };
		return new String(b).charAt(0);
	}

	public static char[] randomCharArray(int size) {
		return randomString(size).toCharArray();
	}

	public static Character[] randomCharObjArray(int size) {
		Character[] a = new Character[size];
		int i = 0;
		for (char c : randomString(size).toCharArray()) {
			a[i] = new Character(c);
			i++;
		}
		return a;
	}

	public static String randomString(int size) {
		byte b[] = new byte[size];
		for (int i = 0; i < size; i++) {
			b[i] = (byte) (97 + (byte) (Math.random() * 26));
		}
		return new String(b);
	}
	
	public static long randomTimestamp() {
		return (long) (Math.random() * System.currentTimeMillis());
	}

}
>>>>>>> 76aa07461566a5976980e6696204781271955163

