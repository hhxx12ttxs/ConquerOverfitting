/**
 * Copyright (C) 2009 Mathieu Carbou <mathieu.carbou@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mycila.math.number;

import com.mycila.math.Factorial;
import com.mycila.math.concurrent.ConcurrentOperation;
import com.mycila.math.concurrent.Result;
import com.mycila.math.distribution.Distribution;
import com.mycila.math.list.ByteProcedure;
import com.mycila.math.prime.PrimaltyTest;
import com.mycila.math.prime.Primes;
import com.mycila.math.prime.Sieve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public abstract class BigInt<T> implements Comparable<BigInt> {

    public static final BigInt ZERO;
    public static final BigInt ONE;
    public static final BigInt TWO;
    public static final BigInt THREE;
    public static final BigInt FOUR;
    public static final BigInt FIVE;
    public static final BigInt SIX;
    public static final BigInt SEVEN;
    public static final BigInt EIGHT;
    public static final BigInt NINE;
    public static final BigInt TEN;
    public static final BigInt INT_MAX;
    public static final BigInt LONG_MAX;

    private static final BigIntFactory FACTORY;

    static {
        try {
            String cname = System.getProperty("mycila.bigint.factory");
            if (cname == null) cname = "com.mycila.math.number.jdk.JDKBigIntFactory";
            @SuppressWarnings({"unchecked"})
            Class<BigIntFactory> fClass = (Class<BigIntFactory>) Thread.currentThread().getContextClassLoader().loadClass(cname);
            if (!BigIntFactory.class.isAssignableFrom(fClass))
                throw new RuntimeException(cname + " does not implement " + BigIntFactory.class.getName());
            FACTORY = fClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        ZERO = big(0);
        ONE = big(1);
        TWO = big(2);
        THREE = big(3);
        FOUR = big(4);
        FIVE = big(5);
        SIX = big(6);
        SEVEN = big(7);
        EIGHT = big(8);
        NINE = big(9);
        TEN = big(10);
        INT_MAX = big(Integer.MAX_VALUE);
        LONG_MAX = big(Long.MAX_VALUE);
    }

    @SuppressWarnings({"unchecked"})
    public static <T> BigInt wrapBig(T number, int radix) {
        return FACTORY.wrap(number, radix);
    }

    public static BigInt big(long number) {
        return FACTORY.create(number);
    }

    public static BigInt big(String number) {
        return FACTORY.create(number);
    }

    public static BigInt big(String number, int radix) {
        return FACTORY.create(number, radix);
    }

    public static BigInt randomBig(int length) {
        return FACTORY.random(length);
    }

    public static BigInt randomPrime(int length) {
        return FACTORY.randomPrime(length);
    }

    public final T internal;
    protected byte[] digits;

    protected BigInt(T internal) {
        this.internal = internal;
    }

    @Override
    public final int hashCode() {
        return internal.hashCode();
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o.getClass() == internal.getClass()) return internal.equals(o);
        return o.getClass() == getClass() && internal.equals(((BigInt) o).internal);
    }

    public abstract String toString(int radix);

    /**
     * Converts this BigInteger to an {@code int}.  This
     * conversion is analogous to a <a
     * href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363"><i>narrowing
     * primitive conversion</i></a> from {@code long} to
     * {@code int} as defined in the <a
     * href="http://java.sun.com/docs/books/jls/html/">Java Language
     * Specification</a>: if this BigInteger is too big to fit in an
     * {@code int}, only the low-order 32 bits are returned.
     * Note that this conversion can lose information about the
     * overall magnitude of the BigInteger value as well as return a
     * result with the opposite sign.
     *
     * @return this BigInteger converted to an {@code int}.
     */
    public abstract int toInt();

    /**
     * Converts this BigInteger to a {@code long}.  This
     * conversion is analogous to a <a
     * href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363"><i>narrowing
     * primitive conversion</i></a> from {@code long} to
     * {@code int} as defined in the <a
     * href="http://java.sun.com/docs/books/jls/html/">Java Language
     * Specification</a>: if this BigInteger is too big to fit in a
     * {@code long}, only the low-order 64 bits are returned.
     * Note that this conversion can lose information about the
     * overall magnitude of the BigInteger value as well as return a
     * result with the opposite sign.
     *
     * @return this BigInteger converted to a {@code long}.
     */
    public abstract long toLong();

    /**
     * Returns the number of bits in the minimal two's-complement
     * representation of this BigInteger, <i>excluding</i> a sign bit.
     * For positive BigIntegers, this is equivalent to the number of bits in
     * the ordinary binary representation.  (Computes
     * {@code (ceil(log2(this < 0 ? -this : this+1)))}.)
     *
     * @return number of bits in the minimal two's-complement
     *         representation of this BigInteger, <i>excluding</i> a sign bit.
     */
    public abstract int bitLength();

    /**
     * Returns the number of bits in the two's complement representation
     * of this BigInteger that differ from its sign bit.  This method is
     * useful when implementing bit-vector style sets atop BigIntegers.
     *
     * @return number of bits in the two's complement representation
     *         of this BigInteger that differ from its sign bit.
     */
    public abstract int bitCount();

    /**
     * Returns the index of the rightmost (lowest-order) one bit in this
     * BigInteger (the number of zero bits to the right of the rightmost
     * one bit).  Returns -1 if this BigInteger contains no one bits.
     * (Computes {@code (this==0? -1 : log2(this & -this))}.)
     *
     * @return index of the rightmost one bit in this BigInteger.
     */
    public abstract int lowestSetBit();

    /**
     * Returns a BigInteger whose value is {@code (this & val)}.  (This
     * method returns a negative BigInteger if and only if this and val are
     * both negative.)
     *
     * @param n value to be AND'ed with this BigInteger.
     * @return {@code this & val}
     */
    public abstract BigInt and(BigInt n);

    /**
     * Returns a BigInteger whose value is {@code (~this)}.  (This method
     * returns a negative value if and only if this BigInteger is
     * non-negative.)
     *
     * @return {@code ~this}
     */
    public abstract BigInt not();

    /**
     * Returns a BigInteger whose value is {@code (this | val)}.  (This method
     * returns a negative BigInteger if and only if either this or val is
     * negative.)
     *
     * @param val value to be OR'ed with this BigInteger.
     * @return {@code this | val}
     */
    public abstract BigInt or(BigInt val);

    /**
     * Returns a BigInteger whose value is {@code (this ^ val)}.  (This method
     * returns a negative BigInteger if and only if exactly one of this and
     * val are negative.)
     *
     * @param val value to be XOR'ed with this BigInteger.
     * @return {@code this ^ val}
     */
    public abstract BigInt xor(BigInt val);

    /**
     * Returns a BigInteger whose value is <tt>(this<sup>exponent</sup>)</tt>.
     *
     * @param exponent exponent to which this BigInteger is to be raised.
     * @return <tt>this<sup>exponent</sup></tt>
     * @throws ArithmeticException {@code exponent} is negative.  (This would
     *                             cause the operation to yield a non-integer value.)
     */
    public abstract BigInt pow(int exponent);

    /**
     * Returns a BigInteger whose value is
     * <tt>(this<sup>exponent</sup> mod m)</tt>.  (Unlike {@code pow}, this
     * method permits negative exponents.)
     *
     * @param exponent the exponent.
     * @param m        the modulus.
     * @return <tt>this<sup>exponent</sup> mod m</tt>
     * @throws ArithmeticException {@code m <= 0}
     * @see #modInverse
     */
    public abstract BigInt modPow(BigInt exponent, BigInt m);

    /**
     * Returns a BigInteger whose value is {@code (this + val)}.
     *
     * @param val value to be added to this BigInteger.
     * @return {@code this + val}
     */
    public abstract BigInt add(BigInt val);

    /**
     * Returns a BigInteger whose value is {@code (this - val)}.
     *
     * @param val value to be subtracted from this BigInteger.
     * @return {@code this - val}
     */
    public abstract BigInt subtract(BigInt val);

    /**
     * Returns a BigInteger whose value is {@code (this * val)}.
     *
     * @param val value to be multiplied by this BigInteger.
     * @return {@code this * val}
     */
    public abstract BigInt multiply(BigInt val);

    /**
     * Returns a BigInteger whose value is {@code (this / val)}.
     *
     * @param val value by which this BigInteger is to be divided.
     * @return {@code this / val}
     * @throws ArithmeticException {@code val==0}
     */
    public abstract BigInt divide(BigInt val);

    /**
     * Returns a BigInteger whose value is {@code (-this)}.
     *
     * @return {@code -this}
     */
    public abstract BigInt opposite();

    /**
     * Compares this BigInteger with the specified BigInteger.  This
     * method is provided in preference to individual methods for each
     * of the six boolean comparison operators ({@literal <}, ==,
     * {@literal >}, {@literal >=}, !=, {@literal <=}).  The suggested
     * idiom for performing these comparisons is: {@code
     * (x.compareTo(y)} &lt;<i>op</i>&gt; {@code 0)}, where
     * &lt;<i>op</i>&gt; is one of the six comparison operators.
     *
     * @param val BigInteger to which this BigInteger is to be compared.
     * @return -1, 0 or 1 as this BigInteger is numerically less than, equal
     *         to, or greater than {@code val}.
     */
    public abstract int compareTo(BigInt val);

    /**
     * Convert a number to the given radix.
     *
     * @param radix new radix
     * @return this number is the new radix
     */
    public abstract BigInt toRadix(int radix);

    /**
     * Get the current radix used to display this number
     *
     * @return the current radix
     */
    public abstract int radix();

    /**
     * Returns {@code true} if this BigInteger is prime,
     * {@code false} if it's definitely composite.
     *
     * @return the probabilty it is prime. O if the BigInteger is determined to be composite, 1 if it is prime
     */
    public abstract boolean isPrime();

    /**
     * Returns the first integer greater than this {@code BigInteger} that
     * is prime.  This method will
     * never skip over a prime when searching: if it returns {@code p}, there
     * is no prime {@code q} such that {@code this < q < p}.
     *
     * @return the first integer greater than this {@code BigInteger} that
     *         is prime.
     * @throws ArithmeticException {@code this < 0}.
     */
    public abstract BigInt nextPrime();

    @Override
    public String toString() {
        return toString(radix());
    }

    /**
     * Returns the signum function of this BigInteger.
     *
     * @return -1, 0 or 1 as the value of this BigInteger is negative, zero or
     *         positive.
     */
    public int signum() {
        int comp = this.compareTo(ZERO);
        return comp < 0 ? -1 : comp == 0 ? 0 : 1;
    }

    /**
     * Returns {@code true} if and only if the designated bit is set.
     * (Computes {@code ((this & (1<<n)) != 0)}.)
     *
     * @param n index of bit to test.
     * @return {@code true} if and only if the designated bit is set.
     * @throws ArithmeticException {@code n} is negative.
     */
    public boolean testBit(int n) {
        return !ZERO.equals(this.and(ONE.shiftLeft(n)));
    }

    /**
     * Returns a BigInteger whose value is {@code (this << n)}.
     * The shift distance, {@code n}, may be negative, in which case
     * this method performs a right shift.
     * (Computes <tt>floor(this * 2<sup>n</sup>)</tt>.)
     *
     * @param n shift distance, in bits.
     * @return {@code this << n}
     * @see #shiftRight
     */
    public BigInt shiftLeft(int n) {
        return this.multiply(TWO.pow(n));
    }

    /**
     * Returns a BigInteger whose value is {@code (this >> n)}.  Sign
     * extension is performed.  The shift distance, {@code n}, may be
     * negative, in which case this method performs a left shift.
     * (Computes <tt>floor(this / 2<sup>n</sup>)</tt>.)
     *
     * @param n shift distance, in bits.
     * @return {@code this >> n}
     * @see #shiftLeft
     */
    public BigInt shiftRight(int n) {
        return this.divide(TWO.pow(n));
    }

    /**
     * Returns a BigInteger whose value is {@code (this & ~val)}.  This
     * method, which is equivalent to {@code and(val.not())}, is provided as
     * a convenience for masking operations.  (This method returns a negative
     * BigInteger if and only if {@code this} is negative and {@code val} is
     * positive.)
     *
     * @param val value to be complemented and AND'ed with this BigInteger.
     * @return {@code this & ~val}
     */
    public BigInt andNot(BigInt val) {
        return this.and(val.not());
    }

    /**
     * Returns a BigInteger whose value is equivalent to this BigInteger
     * with the designated bit cleared.
     * (Computes {@code (this & ~(1<<n))}.)
     *
     * @param n index of bit to clear.
     * @return {@code this & ~(1<<n)}
     * @throws ArithmeticException {@code n} is negative.
     */
    public BigInt clearBit(int n) {
        return this.and(ONE.shiftLeft(n).not());
    }

    /**
     * Returns a BigInteger whose value is equivalent to this BigInteger
     * with the designated bit flipped.
     * (Computes {@code (this ^ (1<<n))}.)
     *
     * @param n index of bit to flip.
     * @return {@code this ^ (1<<n)}
     * @throws ArithmeticException {@code n} is negative.
     */
    public BigInt flipBit(int n) {
        return this.xor(ONE.shiftLeft(n).not());
    }

    /**
     * Returns a BigInteger whose value is equivalent to this BigInteger
     * with the designated bit set.  (Computes {@code (this | (1<<n))}.)
     *
     * @param n index of bit to set.
     * @return {@code this | (1<<n)}
     * @throws ArithmeticException {@code n} is negative.
     */
    public BigInt setBit(int n) {
        return this.or(ONE.shiftLeft(n).not());
    }

    /**
     * Returns this^2
     *
     * @return this^2
     */
    public BigInt square() {
        return pow(2);
    }

    /**
     * Returns a BigInteger whose value is {@code (this + val)}.
     *
     * @param val value to be added to this BigInteger.
     * @return {@code this + val}
     */
    public BigInt add(long val) {
        return add(big(val));
    }

    /**
     * Returns a BigInteger whose value is {@code (this - val)}.
     *
     * @param val value to be subtracted from this BigInteger.
     * @return {@code this - val}
     */
    public BigInt subtract(long val) {
        return subtract(big(val));
    }

    /**
     * Returns a BigInteger whose value is {@code (this * val)}.
     *
     * @param val value to be multiplied by this BigInteger.
     * @return {@code this * val}
     */
    public BigInt multiply(long val) {
        return multiply(big(val));
    }

    /**
     * Returns a BigInteger whose value is {@code (this / val)}.
     *
     * @param val value by which this BigInteger is to be divided.
     * @return {@code this / val}
     * @throws ArithmeticException {@code val==0}
     */
    public BigInt divide(long val) {
        return divide(big(val));
    }

    /**
     * Returns an array of two BigIntegers containing {@code (this / val)}
     * followed by {@code (this % val)}.
     *
     * @param val value by which this BigInteger is to be divided, and the
     *            remainder computed.
     * @return an array of two BigIntegers: the quotient {@code (this / val)}
     *         is the initial element, and the remainder {@code (this % val)}
     *         is the final element.
     * @throws ArithmeticException {@code val==0}
     */
    public BigInt[] divideAndRemainder(BigInt val) {
        BigInt[] res = new BigInt[2];
        res[0] = divide(val);
        res[1] = subtract(val.multiply(res[0]));
        return res;
    }

    /**
     * Returns an array of two BigIntegers containing {@code (this / val)}
     * followed by {@code (this % val)}.
     *
     * @param val value by which this BigInteger is to be divided, and the
     *            remainder computed.
     * @return an array of two BigIntegers: the quotient {@code (this / val)}
     *         is the initial element, and the remainder {@code (this % val)}
     *         is the final element.
     * @throws ArithmeticException {@code val==0}
     */
    public BigInt[] divideAndRemainder(long val) {
        return divideAndRemainder(big(val));
    }

    /**
     * Returns a BigInteger whose value is the absolute value of this
     * BigInteger.
     *
     * @return {@code abs(this)}
     */
    public BigInt abs() {
        return signum() == -1 ? opposite() : this;
    }

    /**
     * Returns the maximum of this BigInteger and {@code val}.
     *
     * @param val value with which the maximum is to be computed.
     * @return the BigInteger whose value is the greater of this and
     *         {@code val}.  If they are equal, either may be returned.
     */
    public BigInt max(BigInt val) {
        return compareTo(val) > 0 ? this : val;
    }

    /**
     * Returns the minimum of this BigInteger and {@code val}.
     *
     * @param val value with which the minimum is to be computed.
     * @return the BigInteger whose value is the lesser of this BigInteger and
     *         {@code val}.  If they are equal, either may be returned.
     */
    public BigInt min(BigInt val) {
        return compareTo(val) < 0 ? this : val;
    }

    /**
     * Returns a BigInteger whose value is {@code (thismodm)}.  This method
     * differs from {@code remainder} in that it always returns a
     * <i>non-negative</i> BigInteger.
     *
     * @param m the modulus.
     * @return {@code this mod m}
     * @throws ArithmeticException {@code m <= 0}
     */
    public BigInt mod(BigInt m) {
        BigInt result = remainder(m);
        return result.signum() >= 0 ? result : result.add(m);
    }

    /**
     * Returns a BigInteger whose value is {@code (thismodm)}.  This method
     * differs from {@code remainder} in that it always returns a
     * <i>non-negative</i> BigInteger.
     *
     * @param m the modulus.
     * @return {@code this mod m}
     * @throws ArithmeticException {@code m <= 0}
     */
    public BigInt mod(long m) {
        if (bitLength() <= 63) return big(toLong() % m);
        return mod(big(m));
    }

    /**
     * Returns a BigInteger whose value is {@code (this % val)}.
     *
     * @param val value by which this BigInteger is to be divided, and the
     *            remainder computed.
     * @return {@code this % val}
     * @throws ArithmeticException {@code val==0}
     */
    public BigInt remainder(BigInt val) {
        return subtract(val.multiply(divide(val)));
    }

    /**
     * Returns a BigInteger whose value is {@code (this % val)}.
     *
     * @param val value by which this BigInteger is to be divided, and the
     *            remainder computed.
     * @return {@code this % val}
     * @throws ArithmeticException {@code val==0}
     */
    public BigInt remainder(long val) {
        return remainder(big(val));
    }

    /**
     * Compute the <a href="http://en.wikipedia.org/wiki/Extended_Euclidean_algorithm">Extended Euclidean algorithm</a>.
     * <p/>
     * <b>Notes:</b>
     * <p/>
     * For 'this' and 'val', this algorithm finds (a, b, c) such that <code>this * a + val * b = c = this.gcd(val)</code>.
     *
     * @param val a number
     * @return An array of length 3 containing the values a, b, c at respective positions 0, 1, 2
     */
    public BigInt[] euclidExtended(BigInt val) {
        BigInt p = ONE,
                q = ZERO,
                a = this,
                r = ZERO,
                s = ONE,
                b = val;
        while (b.signum() != 0) {
            BigInt quot = a.divide(b);
            BigInt t1 = p.subtract(quot.multiply(r));
            BigInt t2 = q.subtract(quot.multiply(s));
            BigInt t3 = a.subtract(quot.multiply(b));
            p = r;
            q = s;
            a = b;
            r = t1;
            s = t2;
            b = t3;
        }
        return new BigInt[]{p, q, a};
    }

    /**
     * Returns a BigInteger whose value is <tt>x = this<sup>-1</sup> mod m, this*x mod m = 1</tt>.
     *
     * @param m the modulus.
     * @return <tt>this<sup>-1</sup> mod m</tt>.
     * @throws ArithmeticException <tt> m &lt;= 0</tt>, or this BigInteger
     *                             has no multiplicative inverse mod m (that is, this BigInteger
     *                             is not <i>relatively prime</i> to m).
     */
    public BigInt modInverse(BigInt m) {
        BigInt[] euclid = euclidExtended(m);
        if (euclid[2].equals(ONE))
            return euclid[0].signum() < 0 ? euclid[0].add(m) : euclid[0];
        if (euclid[2].equals(ONE.opposite()))
            return m.subtract(euclid[0]);
        throw new ArithmeticException("BigInt not invertible: gcd(" + this + ", " + m + ") = " + euclid[2]);
    }

    /**
     * Returns a BigInteger whose value is <tt>x = this<sup>-1</sup> mod m, this*x mod m = 1</tt>.
     *
     * @param m the modulus.
     * @return <tt>this<sup>-1</sup> mod m</tt>.
     * @throws ArithmeticException <tt> m &lt;= 0</tt>, or this BigInteger
     *                             has no multiplicative inverse mod m (that is, this BigInteger
     *                             is not <i>relatively prime</i> to m).
     */
    public BigInt modInverse(long m) {
        return modInverse(big(m));
    }

    /**
     * Returns a BigInteger whose value is
     * <tt>(this<sup>exponent</sup> mod m)</tt>.  (Unlike {@code pow}, this
     * method permits negative exponents.)
     *
     * @param exponent the exponent.
     * @param m        the modulus.
     * @return <tt>this<sup>exponent</sup> mod m</tt>
     * @throws ArithmeticException {@code m <= 0}
     * @see #modInverse
     */
    public BigInt modPow(long exponent, long m) {
        return modPow(big(exponent), big(m));
    }

    /**
     * Returns a BigInteger whose value is {@code (this * val) % m}.
     *
     * @param val value by which this BigInteger is to be multiplied
     * @param m   the modulus.
     * @return {@code (this * val) % m}
     */
    public BigInt modMultiply(BigInt val, BigInt m) {
        return mod(m).multiply(val.mod(m)).mod(m);
    }

    /**
     * Returns a BigInteger whose value is {@code (this * val) % m}.
     *
     * @param val value by which this BigInteger is to be multiplied
     * @param m   the modulus.
     * @return {@code (this * val) % m}
     */
    public BigInt modMultiply(long val, long m) {
        return modMultiply(big(val), big(m));
    }

    /**
     * Returns a BigInteger whose value is {@code (this + val) % m}.
     *
     * @param val value by which this BigInteger is to be added
     * @param m   the modulus.
     * @return {@code (this + val) % m}
     */
    public BigInt modAdd(BigInt val, BigInt m) {
        return mod(m).add(val.mod(m)).mod(m);
    }

    /**
     * Returns a BigInteger whose value is {@code (this + val) % m}.
     *
     * @param val value by which this BigInteger is to be added
     * @param m   the modulus.
     * @return {@code (this + val) % m}
     */
    public BigInt modAdd(long val, long m) {
        return modAdd(big(val), big(m));
    }

    /**
     * Returns a BigInteger whose value is the greatest common divisor of
     * given numbers, using {@link #gcd(BigInt, BigInt[])}
     *
     * @param val value with which the GCD is to be computed.
     * @return {@code GCD(abs(this), abs(val))}
     */
    public BigInt gcd(BigInt val) {
        if (signum() == 0) return val;
        if (val.signum() == 0) return this;
        return abs().euclidExtended(val.abs())[2];
    }

    /**
     * Returns a BigInteger whose value is the greatest common divisor of
     * {@code abs(this)} and {@code abs(val)}.  Returns 0 if
     * {@code this==0 && val==0}.
     *
     * @param val    value with which the GCD is to be computed.
     * @param others Additional numbers to include in GCD computation
     * @return {@code GCD(abs(this), abs(val), ...)}
     */
    public BigInt gcd(BigInt val, BigInt... others) {
        BigInt gcd = gcd(val);
        for (int i = others.length - 1; i >= 0; i--)
            gcd = gcd.gcd(others[i]);
        return gcd;
    }

    /**
     * Returns a BigInteger whose value is the least common multiple of
     * {@code abs(this)} and {@code abs(val)}.  Returns 0 if
     * {@code this==0 && val==0}.
     *
     * @param val value with which the GCD is to be computed.
     * @return {@code GCD(abs(this), abs(val))}
     */
    public BigInt lcm(BigInt val) {
        return divide(gcd(val)).multiply(val);
    }

    /**
     * Returns a BigInteger whose value is the least common multiple of
     * {@code abs(this)} and {@code abs(val)}.  Returns 0 if
     * {@code this==0 && val==0}.
     *
     * @param val    value with which the GCD is to be computed.
     * @param others Other numbers
     * @return {@code GCD(abs(this), abs(val), ...)}
     */
    public BigInt lcm(BigInt val, BigInt... others) {
        BigInt lcm = lcm(val);
        for (int i = others.length - 1; i >= 0; i--)
            lcm = lcm.lcm(others[i]);
        return lcm;
    }

    /**
     * Concatenate positive numbers
     *
     * @param numbers Numbers to concatenate to this number
     * @return The concatenated number
     */
    public BigInt concat(long... numbers) {
        if (numbers.length == 0) return this;
        StringBuilder sb = new StringBuilder().append(numbers[0]);
        for (int i = 1, max = numbers.length; i < max; i++)
            sb.append(numbers[i]);
        return concat(big(sb.toString(), 10));
    }

    /**
     * Concatenate positive numbers
     *
     * @param numbers Numbers to concatenate to this number
     * @return The concatenated number
     */
    public BigInt concat(BigInt... numbers) {
        if (numbers.length == 0) return this;
        StringBuilder sb = new StringBuilder().append(toString());
        for (int i = 0, max = numbers.length; i < max; i++)
            sb.append(numbers[i].internal.toString());
        return big(sb.toString(), radix());
    }

    /**
     * Get the number of digits. Note: 0 as a length of 1.
     *
     * @return Its length
     */
    public int digitsCount() {
        return digitsInternal().length;
    }

    /**
     * Sort the digits of a number
     *
     * @return Another number with the same digits, sorted
     */
    public BigInt digitsSorted() {
        byte[] digits = digitsInternal();
        char[] chars = new char[digits.length];
        for (int i = digits.length - 1; i >= 0; i--)
            chars[i] = (char) (digits[i] + 48);
        Arrays.sort(chars);
        return big(String.valueOf(chars), radix());
    }

    /**
     * List all digits of a number in this base, in descending order of powers.
     * <p/>
     * I.e. 123 gives {1, 2, 3}
     *
     * @return the list of digits
     */
    public byte[] digits() {
        byte[] digits = digitsInternal();
        return Arrays.copyOf(digits, digits.length);
    }

    private byte[] digitsInternal() {
        if (digits != null) return digits;
        String s = signum() < 0 ? opposite().toString() : toString();
        byte[] digits = new byte[s.length()];
        for (int i = s.length() - 1; i >= 0; i--)
            digits[i] = (byte) (s.charAt(i) - 48);
        return this.digits = digits;
    }

    protected int digitsSum = -1;

    /**
     * Returns the digit sum of a number in this base
     *
     * @return the digit sum
     */
    public int digitsSum() {
        if (digitsSum != -1) return digitsSum;
        int sum = 0;
        byte[] digits = digitsInternal();
        for (int i = digits.length - 1; i >= 0; i--)
            sum += digits[i];
        return this.digitsSum = sum;
    }

    protected Distribution<Byte> digitsMap;

    /**
     * Returns the digit distribution for this BigInteger
     *
     * @return the digit sum
     */
    public Distribution<Byte> digitsMap() {
        if (digitsMap != null) return digitsMap;
        Distribution<Byte> distribution = Distribution.of(Byte.class);
        byte[] digits = digitsInternal();
        for (int i = digits.length - 1; i >= 0; i--)
            distribution.add(digits[i]);
        return digitsMap = distribution;
    }

    /**
     * Executes a callback for each digit of the number
     *
     * @param procedure The callback to run for each digit
     * @return True if all digits have been processed.
     *         The callback can return false at any time to stop processing.
     */
    public boolean eachDigit(ByteProcedure procedure) {
        byte[] digits = digitsInternal();
        for (int i = 0, max = digits.length; i < max; i++)
            if (!procedure.execute(digits[i]))
                return false;
        return true;
    }

    /**
     * Computes the <a href="http://en.wikipedia.org/wiki/Digital_root">Digital Root</a> of this number
     *
     * @return The <a href="http://en.wikipedia.org/wiki/Digital_root">Digital Root</a> of this number
     */
    public int digitalRoot() {
        if (signum() == 0) return 0;
        return ONE.add(this.subtract(ONE).mod(NINE)).toInt();
    }


    /**
     * Reverse the digits of a number
     *
     * @return the reversed number
     */
    public BigInt digitsReversed() {
        byte[] digits = digitsInternal();
        char chars[] = new char[digits.length];
        for (int i = digits.length - 1, max = i; i >= 0; i--)
            chars[i] = (char) (digits[max - i] + 48);
        return big(String.valueOf(chars), radix());
    }

    /**
     * Rotate digits of a number.<br>
     * - The rotation direction is specified by the sign of offset<br>
     * - The rotation length is determined by the value of the offset
     * <p/>
     * I.e., rotate(1234, 3) and rotate(1234, -1) will both give 2341
     *
     * @param offset The direction and length of the rotation
     * @return The rotated number
     */
    public BigInt digitsRotated(int offset) {
        byte[] digits = digits();
        if (digits.length < 2) return this;
        offset %= digits.length;
        if (offset < 0) offset += digits.length;
        if (offset == 0) return this;
        int end = digits.length - 1;
        while (offset-- > 0) {
            byte tmp = digits[end];
            System.arraycopy(digits, 0, digits, 1, end);
            digits[0] = tmp;
        }
        for (int i = end; i >= 0; i--)
            digits[i] += 48;
        return big(new String(digits), radix());
    }

    /**
     * Returns the signature of a number. The signature is composed of all the digit of the number, sorted.
     *
     * @return Its digit list
     */
    public byte[] digitsSignature() {
        byte[] digits = digits();
        Arrays.sort(digits);
        return digits;
    }

    protected Boolean isPalindromic;

    /**
     * Check if the number is a palyndrom
     *
     * @return true if it is
     */
    public boolean isPalindromic() {
        if (isPalindromic != null) return isPalindromic;
        byte[] digits = digitsInternal();
        int len = digits.length - 1;
        for (int i = (digits.length - 1) >>> 1; i >= 0; i--)
            if (digits[i] != digits[len - i])
                return isPalindromic = false;
        return isPalindromic = true;
    }

    /**
     * Check wheter the given numbers is a permutations of the digits of this number
     *
     * @param val The  number to check
     * @return True if the numbers is a permutation of number
     */
    public boolean isDigitsPermutation(BigInt val) {
        return Arrays.equals(this.digitsSignature(), val.digitsSignature());
    }

    /**
     * Check if this number is <a href="http://en.wikipedia.org/wiki/Pandigital_number">pandigital</a>, meaning
     * that it contains at least once each digit of its base.
     * <p/>
     * In example, 1223334444555567890 is pandigital in base 10.
     *
     * @return true if this number is pandigital for its radix (base)
     */
    public boolean isPandigital() {
        return isPandigital(0, radix() - 1);
    }

    /**
     * Check if this number is <a href="http://en.wikipedia.org/wiki/Pandigital_number">from-to pandigital</a>, meaning
     * that it contains at least once each digit from 'from' to 'to' all inclusive.
     * <p/>
     * In example, 1223334444555567890 is 0-9 pandigital.
     *
     * @param from The lower digit
     * @param to   The higher digit
     * @return true if this number is from-to pandigital
     */
    public boolean isPandigital(int from, int to) {
        int length = to - from + 1;
        int bitset = 0;
        byte[] digits = digits();
        for (byte digit : digits) {
            if (digit < from || digit > to) return false;
            int bit = 1 << digit - from;
            bitset |= bit;
        }
        int mask = (1 << length) - 1;
        return (bitset & mask) == mask;
    }

    /**
     * Check wheter this number is pandigital and if yes returns its range: the number is then a-b pandigital, where
     * a and b are the values returned
     *
     * @return An array of tewo values a (position 0) and b (position b) if the number is a to b pandigital. Otherwise, returns null.
     */
    public byte[] pandigitalRange() {
        if (signum() == 0) return new byte[]{0, 0};
        int bitset = 0;
        byte[] digits = digits();
        for (byte digit : digits) bitset |= 1 << digit;
        byte from = 0;
        int mask = 1;
        for (; mask <= 512 && (bitset & mask) == 0; mask <<= 1) from++;
        byte to = from;
        for (; mask <= 512 && (bitset & mask) == mask; mask <<= 1) to++;
        return (bitset >>> to) == 0 ? new byte[]{from, (byte) (to - 1)} : null;
    }

    /**
     * Returns the integer square root of this integer.
     *
     * @return <code>k<code> such as <code>k^2 <= this < (k + 1)^2</code>
     * @throws ArithmeticException if this integer is negative.
     */
    public BigInt isqrt() {
        return isqrtAndRemainder()[0];
    }

    /**
     * Compute the <a href="http://en.wikipedia.org/wiki/Integer_square_root">integer square root</a> of a number.
     *
     * @return An array of two BigIntegers: <code>[q, r]</code>, where <code>q<sup>2</sup> + r = number</code>.
     */
    public BigInt[] isqrtAndRemainder() {
        // Newton's algorithm (http://en.wikipedia.org/wiki/Nth_root_algorithm)
        if (signum() < 0)
            throw new ArithmeticException("Square root of negative integer");
        if (signum() == 0 || equals(ONE))
            return new BigInt[]{this, ZERO};
        BigInt guess = ONE.shiftLeft(bitLength() >>> 1);
        while (true) {
            BigInt newGuess = guess.add(divide(guess)).shiftRight(1);
            if (newGuess.compareTo(guess) >= 0) break;
            guess = newGuess;
        }
        return new BigInt[]{guess, subtract(guess.square())};
    }

    /**
     * Compute the integer root q of a number so that q^root + r = number
     *
     * @param root The root to compute
     * @return q such that q is the maximum number so that <code>q<sup>root</sup> <= number</code>.
     */
    public BigInt iroot(int root) {
        return irootAndRemainder(root)[0];
    }

    /**
     * Compute the integer root q of a number so that q^root + r = number
     *
     * @param root The root to compute
     * @return An array of two BigIntegers: <code>[q, r]</code>, where <code>q<sup>root</sup> + r = this</code>.
     */
    public BigInt[] irootAndRemainder(int root) {
        // Newton's algorithm (http://en.wikipedia.org/wiki/Nth_root_algorithm)
        if (root <= 0 || signum() < 0)
            throw new ArithmeticException("Root and this number must be strictly positive");
        if (signum() == 0 || equals(ONE))
            return new BigInt[]{this, ZERO};
        BigInt guess = ONE.shiftLeft(bitLength() / root + 1);
        int rootMin1 = root - 1;
        BigInt bigRootMin1 = big(rootMin1);
        while (true) {
            BigInt newGuess = bigRootMin1.multiply(guess).add(this.divide(guess.pow(rootMin1))).divide(root);
            if (newGuess.compareTo(guess) >= 0) break;
            guess = newGuess;
        }
        return new BigInt[]{guess, subtract(guess.pow(root))};
    }

    /**
     * Get the recurring cycle of the inverse of this number 1/this. The recurring cycle is the length of
     * the period of the floating part of the decimal 1/this.
     * <p/>
     * We find the least number l that satisfy 10^l mod this = 1
     * <p/>
     * In example, 1/7 = 0.142857142857142857142 has a period of 142857, length 6
     *
     * @return An array containing the period at position 0 and its length at position 1
     */
    public BigInt[] recuringCycle() {
        // We check the least number that satisfy 10^l mod p = 1
        BigInt l = ZERO, pow = ONE;
        do {
            l = l.add(ONE);
            pow = TEN.multiply(pow);
            BigInt[] qr = pow.divideAndRemainder(this);
            if (qr[1].equals(ONE))
                return new BigInt[]{qr[0], l};
        } while (l.compareTo(this) < 0);
        return new BigInt[]{ZERO, ZERO};
    }

    /**
     * Computes the sum of the consecutive numbers from this to n
     * <code>S = this + (this+1) + ... + n
     *
     * @param n The limit which will determine
     *          the direction of the sum, from this to n or n to this
     * @return the consecutive sum
     */
    public BigInt sumTo(BigInt n) {
        BigInt start = this;
        if (compareTo(n) > 0) {
            start = n;
            n = this;
        }
        return n.subtract(start).add(ONE).multiply(start.add(n)).shiftRight(1);
    }

    /**
     * Computes the sum of the consecutive numbers from this to n
     * <code>S = this + (this+1) + ... + n
     *
     * @param n The limit which will determine
     *          the direction of the sum, from this to n or n to this
     * @return the consecutive sum
     */
    public BigInt sumTo(long n) {
        return sumTo(big(n));
    }

    /**
     * Computes the product of the consecutive numbers from this to n
     * <code>S = this * (this+1) * ... * n
     *
     * @param n The limit which will determine
     *          the direction of the product, from this to n or n to this
     * @return the consecutive product
     */
    public BigInt productTo(BigInt n) {
        BigInt start = this;
        if (compareTo(n) > 0) {
            start = n;
            n = this;
        }
        BigInt res = ONE;
        int shifts = 0;
        for (; start.compareTo(n) <= 0; start = start.add(ONE)) {
            int s = start.lowestSetBit();
            shifts += s;
            res = res.multiply(start.shiftRight(s));
        }
        return res.shiftLeft(shifts);
    }

    /**
     * Computes the product of the consecutive numbers from this to n
     * <code>S = this * (this+1) * ... * n
     *
     * @param n The limit which will determine
     *          the direction of the product, from this to n or n to this
     * @return the consecutive product
     */
    public BigInt productTo(long n) {
        return productTo(big(n));
    }

    /**
     * Computes the <a href="http://en.wikipedia.org/wiki/Jacobi_symbol">Jacobi symbol</a> (a/this)
     *
     * @param a Positive odd number
     * @return The Jacobi symbol (a/this)
     */
    public BigInt jacobiSymbol(BigInt a) {
        // Using BigInteger implementation
        if (a.signum() == 0) return ZERO; // (0/n) = 0
        BigInt j = ONE;
        BigInt u = this;
        // Make p positive
        if (a.signum() < 0) {
            a = a.opposite(); // (a/n) = (-a/n)*(-1/n)
            BigInt n8 = u.and(SEVEN);
            if (n8.equals(THREE) || n8.equals(SEVEN))
                j = j.opposite(); // 3 (011) or 7 (111) mod 8
        }
        // Get rid of factors of 2 in p
        while (a.and(THREE).signum() == 0)
            a = a.shiftRight(2);
        if (a.and(ONE).signum() == 0) {
            a = a.shiftRight(1);
            if (u.xor(u.shiftRight(1)).and(TWO).signum() != 0)
                j = j.opposite(); // 3 (011) or 5 (101) mod 8
        }
        if (a.equals(ONE)) return j;
        // Then, apply quadratic reciprocity
        if (TWO.and(a).and(u).signum() != 0)
            j = j.opposite(); // p = u = 3 (mod 4)
        // And reduce u mod p
        u = mod(a);
        // Get rid of factors of 2 in p
        while (a.and(THREE).signum() == 0)
            a = a.shiftRight(2);
        // Now compute Jacobi(u,p), u < p
        while (u.signum() != 0) {
            while (u.and(THREE).signum() == 0)
                u = u.shiftRight(2);
            if (u.and(ONE).signum() == 0) {
                u = u.shiftRight(1);
                if (a.xor(a.shiftRight(1)).and(TWO).signum() != 0)
                    j = j.opposite(); // 3 (011) or 5 (101) mod 8
            }
            if (u.equals(ONE)) return j;
            // Now both u and p are odd, so use quadratic reciprocity
            BigInt t = u;
            u = a;
            a = t;
            if (TWO.and(a).and(u).signum() != 0)
                j = j.opposite(); // p = u = 3 (mod 4)
            // Now u >= p, so it can be reduced
            u = u.mod(a);
        }
        return ZERO;
    }

    /**
     * Primalty test using <a href="http://en.wikipedia.org/wiki/Miller%E2%80%93Rabin_primality_test">Miller-Rabin primality test<a/>.
     * <p/>
     * If the BigInteger passes all tests, returns the probabilty it is prime as a double. Number of passes is set to maximum 50 depending on the number's length.
     *
     * @return true if the BigInteger is determined to be prime for 50 iterations
     */
    public boolean isPrimeMillerRabin() {
        return isPrimeMillerRabin(50);
    }

    /**
     * Primalty test using <a href="http://en.wikipedia.org/wiki/Miller%E2%80%93Rabin_primality_test">Miller-Rabin primality test<a/>.
     * <p/>
     * If the BigInteger passes all tests, returns the probabilty it is prime as a double.
     *
     * @param certainty Number of different bases to try is passed in as an int
     * @return true if the BigInteger is determined to be prime for given number of iterations
     */
    public boolean isPrimeMillerRabin(int certainty) {
        if (equals(TWO)) return true;
        if (!testBit(0) || equals(ONE)) return false;
        if (bitLength() <= 31)
            return PrimaltyTest.millerRabin(toInt());
        // Find a and m such that m is odd and this == 1 + 2**a * m
        BigInt thisMinusOne = subtract(ONE);
        int a = thisMinusOne.lowestSetBit();
        BigInt m = thisMinusOne.shiftRight(a);
        int len = bitLength();
        while (certainty-- > 0) {
            // Generate a uniform random on (1, this)
            BigInt b;
            do b = randomBig(len);
            while (b.compareTo(ONE) <= 0 || b.compareTo(this) >= 0);
            int j = 0;
            BigInt z = b.modPow(m, this);
            while (!(j == 0 && z.equals(ONE) || z.equals(thisMinusOne))) {
                if (j > 0 && z.equals(ONE) || ++j == a)
                    return false;
                z = z.modPow(TWO, this);
            }
        }
        return true;
    }

    /**
     * Check wheter this number is a Mersenne Prime <code>Mp = 2^this - 1</code>
     * <p/>
     * Using <a href="http://en.wikipedia.org/wiki/Lucas%E2%80%93Lehmer_test_for_Mersenne_numbers">Lucas-Lehmer primality test<a/>
     *
     * @return True if it is a Mersenne prime
     */
    public boolean isPrimeMersenne() {
        if (bitLength() > 63)
            throw new ArithmeticException("Number too big to be an exponent. Must be <= Integer.MAX_VALUE");
        return equals(TWO) || !(!testBit(0) || equals(ONE))
                && ONE.shiftLeft(toInt()).subtract(ONE).isPrimeLucasLehmer();
    }

    /**
     * Returns true iff this BigInteger is a Lucas-Lehmer prime.
     * The following assumptions are made:
     * This BigInteger is a positive, odd number.
     *
     * @return True is the number is prime
     */
    public boolean isPrimeLucasLehmer() {
        // Using JDK's implementation
        if (equals(TWO)) return true;
        if (!testBit(0) || equals(ONE)) return false;
        BigInt thisPlusOne = this.add(ONE);
        BigInt d = FIVE;
        while (jacobiSymbol(d).signum() >= 0)
            d = (d.signum() < 0) ? d.abs().add(TWO) : d.add(TWO).opposite(); // 5, -7, 9, -11, ...
        // Lucas-Lehmer sequence
        BigInt u = ONE;
        BigInt u2;
        BigInt v = ONE;
        BigInt v2;
        for (int i = thisPlusOne.bitLength() - 2; i >= 0; i--) {
            u2 = u.multiply(v).mod(this);
            v2 = v.square().add(d.multiply(u.square())).mod(this);
            if (v2.testBit(0)) v2 = v2.subtract(this);
            v2 = v2.shiftRight(1);
            u = u2;
            v = v2;
            if (thisPlusOne.testBit(i)) {
                u2 = u.add(v).mod(this);
                if (u2.testBit(0)) u2 = u2.subtract(this);
                u2 = u2.shiftRight(1);
                v2 = v.add(d.multiply(u)).mod(this);
                if (v2.testBit(0)) v2 = v2.subtract(this);
                v2 = v2.shiftRight(1);
                u = u2;
                v = v2;
            }
        }
        return u.mod(this).equals(ZERO);
    }

    /**
     * check if this number is a <a href="http://en.wikipedia.org/wiki/Euler-Jacobi_pseudoprime">Euler-Jacobi pseudoprime</a> for base a
     *
     * @param a The base
     * @return True if a^[(n?1)/2] = (a/this) mod n, (a/this) beeing the Jacobi Symbol.
     */
    public boolean isPseudoprimeEulerJacobi(BigInt a) {
        return a.modPow(subtract(ONE).shiftRight(1), this).equals(jacobiSymbol(a));
    }

    /**
     * <p>Checks <a href="http://en.wikipedia.org/wiki/Fermat_primality_test">Fermat's Little Theorem</a> for base <i>b</i>; i.e.
     * <code><i>b</i>^(this-1) == 1 (mod this)</code>.</p>
     * The number of passes is set to 50 by default.
     *
     * @return <code>true</code> iff <code><i>b</i>^(this-1) == 1 (mod this)</code>,
     *         for some <i>b</i>.
     */
    public boolean isPrimeFermatLittle() {
        return isPrimeFermatLittle(50);
    }

    /**
     * <p>Checks Fermat's Little Theorem for base <i>b</i>; i.e.
     * <code><i>b</i>**(w-1) == 1 (mod w)</code>.</p>
     *
     * @param certainty the number of random bases to test.
     * @return <code>true</code> iff <code><i>b</i>**(w-1) == 1 (mod w)</code>,
     *         for some <i>b</i>.
     */
    public boolean isPrimeFermatLittle(int certainty) {
        if (equals(TWO)) return true;
        if (!testBit(0) || equals(ONE)) return false;
        BigInt minusOne = subtract(ONE);
        // Test for base 2
        if (!TWO.modPow(minusOne, this).equals(ONE))
            return false;
        int length = minusOne.bitLength();
        while (certainty-- > 0) {
            // Generate a uniform random on (3, minusOne)
            BigInt b;
            do b = randomBig(length);
            while (b.compareTo(TWO) <= 0 || b.compareTo(minusOne) > 0);
            // test for base b
            if (!b.modPow(minusOne, this).equals(ONE))
                return false;
        }
        return true;
    }

    /**
     * <p>Checks <a href="http://en.wikipedia.org/wiki/Solovay-Strassen_primality_test">SolovayStrassen primality test</a></p>
     * The number of passes is set to 50 by default.
     *
     * @return <code>true</code> if the number is a probable prime
     */
    public boolean isPrimeSolovayStrassen() {
        return isPrimeSolovayStrassen(50);
    }

    /**
     * <p>Checks <a href="http://en.wikipedia.org/wiki/Solovay-Strassen_primality_test">SolovayStrassen primality test</a></p>
     * The number of passes is set to 50 by default.
     *
     * @param certainty the number of random bases to test.
     * @return <code>true</code> if the number is a probable prime
     */
    public boolean isPrimeSolovayStrassen(int certainty) {
        if (equals(TWO)) return true;
        if (!testBit(0) || equals(ONE)) return false;
        BigInt minusOne = subtract(ONE);
        int length = minusOne.bitLength();
        BigInt exp = minusOne.shiftRight(1);
        while (certainty-- > 0) {
            BigInt a;
            BigInt x;
            do a = randomBig(length);
            while (a.signum() == 0 || a.compareTo(minusOne) > 0 || (x = jacobiSymbol(a)).signum() < 0);
            if (x.signum() == 0 || !x.equals(a.modPow(exp, this)))
                return false;
        }
        return true;
    }

    public boolean isPrimeEulerCriterion() {
        return isPrimeEulerCriterion(20);
    }

    /**
     * <p>Java port of Colin Plumb primality test (Euler Criterion)
     * implementation for a base of 2 --from bnlib-1.1 release, function
     * primeTest() in prime.c. this is his comments; (bn is our w).</p>
     * <p/>
     * <p>"Now, check that bn is prime. If it passes to the base 2, it's prime
     * beyond all reasonable doubt, and everything else is just gravy, but it
     * gives people warm fuzzies to do it.</p>
     * <p/>
     * <p>This starts with verifying Euler's criterion for a base of 2. This is
     * the fastest pseudoprimality test that I know of, saving a modular squaring
     * over a Fermat test, as well as being stronger. 7/8 of the time, it's as
     * strong as a strong pseudoprimality test, too. (The exception being when
     * <code>bn == 1 mod 8</code> and <code>2</code> is a quartic residue, i.e.
     * <code>bn</code> is of the form <code>a^2 + (8*b)^2</code>.) The precise
     * series of tricks used here is not documented anywhere, so here's an
     * explanation. Euler's criterion states that if <code>p</code> is prime
     * then <code>a^((p-1)/2)</code> is congruent to <code>Jacobi(a,p)</code>,
     * modulo <code>p</code>. <code>Jacobi(a, p)</code> is a function which is
     * <code>+1</code> if a is a square modulo <code>p</code>, and <code>-1</code>
     * if it is not. For <code>a = 2</code>, this is particularly simple. It's
     * <code>+1</code> if <code>p == +/-1 (mod 8)</code>, and <code>-1</code> if
     * <code>m == +/-3 (mod 8)</code>. If <code>p == 3 (mod 4)</code>, then all
     * a strong test does is compute <code>2^((p-1)/2)</code>. and see if it's
     * <code>+1</code> or <code>-1</code>. (Euler's criterion says <i>which</i>
     * it should be.) If <code>p == 5 (mod 8)</code>, then <code>2^((p-1)/2)</code>
     * is <code>-1</code>, so the initial step in a strong test, looking at
     * <code>2^((p-1)/4)</code>, is wasted --you're not going to find a
     * <code>+/-1</code> before then if it <b>is</b> prime, and it shouldn't
     * have either of those values if it isn't. So don't bother.</p>
     * <p/>
     * <p>The remaining case is <code>p == 1 (mod 8)</code>. In this case, we
     * expect <code>2^((p-1)/2) == 1 (mod p)</code>, so we expect that the
     * square root of this, <code>2^((p-1)/4)</code>, will be <code>+/-1 (mod p)
     * </code>. Evaluating this saves us a modular squaring 1/4 of the time. If
     * it's <code>-1</code>, a strong pseudoprimality test would call <code>p</code>
     * prime as well. Only if the result is <code>+1</code>, indicating that
     * <code>2</code> is not only a quadratic residue, but a quartic one as well,
     * does a strong pseudoprimality test verify more things than this test does.
     * Good enough.</p>
     * <p/>
     * <p>We could back that down another step, looking at <code>2^((p-1)/8)</code>
     * if there was a cheap way to determine if <code>2</code> were expected to
     * be a quartic residue or not. Dirichlet proved that <code>2</code> is a
     * quadratic residue iff <code>p</code> is of the form <code>a^2 + (8*b^2)</code>.
     * All primes <code>== 1 (mod 4)</code> can be expressed as <code>a^2 +
     * (2*b)^2</code>, but I see no cheap way to evaluate this condition."</p>
     *
     * @param numTests Number of primes to verify against
     * @return <code>true</code> iff the designated number passes Euler criterion
     *         as implemented by Colin Plumb in his <i>bnlib</i> version 1.1.
     */
    public boolean isPrimeEulerCriterion(int numTests) {
        if (equals(TWO)) return true;
        if (!testBit(0) || equals(ONE)) return false;
        // From http://www.gnu.org/software/gnu-crypto/
        // first check if it's already a known prime
        BigInt minusOne = subtract(ONE);
        BigInt e = minusOne;
        // l is the 3 least-significant bits of e
        int l = e.and(SEVEN).toInt();
        int j = 1; // Where to start in prime array for strong prime tests
        BigInt A;
        int k;
        if ((l & 7) != 0) {
            e = e.shiftRight(1);
            A = TWO.modPow(e, this);
            if ((l & 7) == 6) { // bn == 7 mod 8, expect +1
                if (A.bitCount() != 1)
                    return false; // Not prime
                k = 1;
            } else { // bn == 3 or 5 mod 8, expect -1 == bn-1
                A = A.add(ONE);
                if (!A.equals(this))
                    return false; // Not prime
                k = 1;
                if ((l & 4) != 0) { // bn == 5 mod 8, make odd for strong tests
                    e = e.shiftRight(1);
                    k = 2;
                }
            }
        } else { // bn == 1 mod 8, expect 2^((bn-1)/4) == +/-1 mod bn
            e = e.shiftRight(2);
            A = TWO.modPow(e, this);
            if (A.bitCount() == 1) {
                j = 0; // Re-do strong prime test to base 2
            } else {
                A = A.add(ONE);
                if (!A.equals(this))
                    return false; // Not prime
            }
            // bnMakeOdd(n) = d * 2^s. Replaces n with d and returns s.
            k = e.lowestSetBit();
            e = e.shiftRight(k);
            k += 2;
        }
        // It's prime!  Now go on to confirmation tests
        // Now, e = (bn-1)/2^k is odd.  k >= 1, and has a given value with
        // probability 2^-k, so its expected value is 2.  j = 1 in the usual case
        // when the previous test was as good as a strong prime test, but 1/8 of
        // the time, j = 0 because the strong prime test to the base 2 needs to
        // be re-done.
        Sieve sieve = Sieve.to(numTests);
        for (int i = j, max = sieve.size(); i < max; i++) {
            A = big(sieve.get(i));
            A = A.modPow(e, this);
            if (A.bitCount() == 1)
                continue; // Passed this test
            l = k;
            while (true) {
                // A = A.add(ONE);
                // if (A.equals(w)) { // Was result bn-1?
                if (A.equals(minusOne)) // Was result bn-1?
                    break; // Prime
                if (--l == 0) // Reached end, not -1? luck?
                    return false; // Failed, not prime
                // This portion is executed, on average, once
                // A = A.subtract(ONE); // Put a back where it was
                A = A.modPow(TWO, this);
                if (A.bitCount() == 1)
                    return false; // Failed, not prime
            }
            // It worked (to the base primes[i])
        }
        return true;
    }

    /**
     * Returns the product of this BigInt by another BigInt using the
     * <a href="http://en.wikipedia.org/wiki/Karatsuba_algorithm">Karatsuba algorithm</a>
     *
     * @param val Multiplicator
     * @return the product
     */
    public BigInt multiplyKaratsuba(BigInt val) {
        int n = Math.max(bitLength(), val.bitLength());
        n = (n >>> 1) + (n & 1);
        // x = a + 2^N b, y = c + 2^N d
        BigInt b = shiftRight(n);
        BigInt a = subtract(b.shiftLeft(n));
        BigInt d = val.shiftRight(n);
        BigInt c = val.subtract(d.shiftLeft(n));
        // compute sub-expressions
        BigInt ac = a.multiply(c);
        BigInt bd = b.multiply(d);
        BigInt abcd = a.add(b).multiply(c.add(d));
        a = b = c = d = null;
        // a*c + 2^n * ((a+b)*(c+d)-(a*c+b*d)) + b*d*2^2n
        return ac.add(abcd.subtract(ac).subtract(bd).shiftLeft(n)).add(bd.shiftLeft(n << 1));
    }

    /**
     * Multiplies two BigIntegers using a 3-way Toom-Cook multiplication
     * algorithm.  This is a recursive divide-and-conquer algorithm which is
     * more efficient for large numbers than what is commonly called the
     * "grade-school" algorithm used in multiplyToLen.  If the numbers to be
     * multiplied have length n, the "grade-school" algorithm has an
     * asymptotic complexity of O(n^2).  In contrast, 3-way Toom-Cook has a
     * complexity of about O(n^1.465).  It achieves this increased asymptotic
     * performance by breaking each number into three parts and by doing 5
     * multiplies instead of 9 when evaluating the product.  Due to overhead
     * (additions, shifts, and one division) in the Toom-Cook algorithm, it
     * should only be used when both numbers are larger than a certain
     * threshold (found experimentally).  This threshold is generally larger
     * than that for Karatsuba multiplication, so this algorithm is generally
     * only used when numbers become significantly larger.
     * <p/>
     * The algorithm used is the "optimal" 3-way Toom-Cook algorithm outlined
     * by Marco Bodrato.
     * <p/>
     * See: http://bodrato.it/toom-cook/
     * http://bodrato.it/papers/#WAIFI2007
     * <p/>
     * "Towards Optimal Toom-Cook Multiplication for Univariate and
     * Multivariate Polynomials in Characteristic 2 and 0." by Marco BODRATO;
     * In C.Carlet and B.Sunar, Eds., "WAIFI'07 proceedings", p. 116-133,
     * LNCS #4547. Springer, Madrid, Spain, June 21-22, 2007.
     *
     * @param val Multiplicator
     * @return the product
     */
    public BigInt multiplyToomCook3(BigInt val) {
        int len = (Math.max(bitLength(), val.bitLength()) + 31) >>> 5;
        // k is the size (in ints) of the lower-order slices.
        len = (len + 2) / 3 << 5;   // Equal to ceil(largest/3)*32
        ConcurrentOperation.Multiply multiply = ConcurrentOperation.multiply();
        ConcurrentOperation.Slice slice = ConcurrentOperation.slice(len);
        Result<BigInt> a0 = slice.result(this, 0);
        Result<BigInt> b0 = slice.result(val, 0);
        Result<BigInt> a2 = slice.result(this, 2);
        Result<BigInt> b2 = slice.result(val, 2);
        Result<BigInt> a1 = slice.result(this, 1);
        Result<BigInt> b1 = slice.result(val, 1);
        slice = null;

        Result<BigInt> v0 = multiply.result(a0.get(), b0.get());
        Result<BigInt> vinf = multiply.result(a2.get(), b2.get());
        BigInt da = a2.get().add(a0.get());
        BigInt db = b2.get().add(b0.get());
        Result<BigInt> vm1 = multiply.result(da.subtract(a1.get()), db.subtract(b1.get()));

        da = da.add(a1.get());
        a1 = null;
        db = db.add(b1.get());
        b1 = null;
        BigInt v1 = da.multiply(db);
        BigInt t1 = v1.subtract(v0.get());
        BigInt t5 = da.add(a2.get()).shiftLeft(1).subtract(a0.get())
                .multiply(db.add(b2.get()).shiftLeft(1).subtract(b0.get()))
                .subtract(vm1.get()).divide(THREE).subtract(t1).shiftRight(1).subtract(vinf.get().shiftLeft(1));
        multiply = null;
        a0 = a2 = b0 = b2 = null;

        da = v1.subtract(vm1.get()).shiftRight(1);
        vm1 = null;
        v1 = null;
        db = vinf.get().shiftLeft(len)
                .add(t5).shiftLeft(len)
                .add(t1.subtract(da).subtract(vinf.get())).shiftLeft(len)
                .add(da.subtract(t5)).shiftLeft(len)
                .add(v0.get());
        v0 = vinf = null;
        t1 = t5 = da = null;

        return signum() != val.signum() ? db.opposite() : db;

        // NON-PARALLEL VERSION, 4-5 times slower
        /*BigInt v0, v1, v2, vm1, vinf, t1, t2, tm1, da1, db1;
        v0 = a0.get().multiply(b0.get());
        da1 = a2.get().add(a0.get());
        db1 = b2.get().add(b0.get());
        vm1 = da1.subtract(a1.get()).multiply(db1.subtract(b1.get()));
        da1 = da1.add(a1.get());
        db1 = db1.add(b1.get());
        v1 = da1.multiply(db1);
        v2 = da1.add(a2.get()).shiftLeft(1).subtract(a0.get()).multiply(db1.add(b2.get()).shiftLeft(1).subtract(b0.get()));
        vinf = a2.get().multiply(b2.get());
        t2 = v2.subtract(vm1).divide(THREE);
        tm1 = v1.subtract(vm1).shiftRight(1);
        t1 = v1.subtract(v0);
        t2 = t2.subtract(t1).shiftRight(1);
        t1 = t1.subtract(tm1).subtract(vinf);
        t2 = t2.subtract(vinf.shiftLeft(1));
        tm1 = tm1.subtract(t2);
        BigInt result = vinf.shiftLeft(len).add(t2).shiftLeft(len).add(t1).shiftLeft(len).add(tm1).shiftLeft(len).add(v0);
        return signum() != val.signum() ? result.opposite() : result;*/
    }

    /**
     * Split the number in slices of given lenth.
     *
     * @param len Length in bit
     * @return An array of bigInt wherer the concatenation of a[i]...a[1]a[0] = this number
     */
    public BigInt[] slice(int len) {
        BigInt mask = ONE.shiftLeft(len).subtract(ONE);
        BigInt num = this;
        BigInt[] slices = new BigInt[(bitLength() + len - 1) / len];
        for (int i = 0; num.signum() != 0; i++) {
            slices[i] = num.and(mask).abs();
            num = num.shiftRight(len);
        }
        return slices;
    }

    /**
     * Split the number in slices of given lenth.
     *
     * @param len   Length in bit
     * @param index The index of the slice to get: from 0 to Ceil(bitLength / n)
     * @return A bigInt representing the wanted slice of the number, so that the concatenation of all slices a[i]...a[1]a[0] = this number
     */
    public BigInt slice(int len, int index) {
        return abs().shiftRight(len * index).and(ONE.shiftLeft(len).subtract(ONE));
    }

    /**
     * Squares a BigInteger using the Karatsuba squaring algorithm.  It should
     * be used when both numbers are larger than a certain threshold (found
     * experimentally).  It is a recursive divide-and-conquer algorithm that
     * has better asymptotic performance than the algorithm used in
     * squareToLen.
     */
    public BigInt squareKaratsuba() {
        int len = (bitLength() + 31) >>> 5;
        len = (len + 1) / 2 << 5;
        BigInt xl = slice(len, 0);
        BigInt xh = slice(len, 1);
        BigInt xhs = xh.square();
        BigInt xls = xl.square();
        // xh^2 << 64  +  (((xl+xh)^2 - (xh^2 + xl^2)) << 32) + xl^2
        return xhs.shiftLeft(len).add(xl.add(xh).square().subtract(xhs.add(xls))).shiftLeft(len).add(xls);
    }

    /**
     * Squares a BigInteger using the 3-way Toom-Cook squaring algorithm.  It
     * should be used when both numbers are larger than a certain threshold
     * (found experimentally).  It is a recursive divide-and-conquer algorithm
     * that has better asymptotic performance than the algorithm used in
     * squareToLen or squareKaratsuba.
     */
    public BigInt squareToomCook3() {
        int len = bitLength() + 31 >>> 5;
        len = (len + 2) / 3 << 5;   // Equal to ceil(largest/3)*32
        ConcurrentOperation.Square square = ConcurrentOperation.square();
        ConcurrentOperation.Slice slice = ConcurrentOperation.slice(len);
        Result<BigInt> a2 = slice.result(this, 2);
        Result<BigInt> a0 = slice.result(this, 0);
        Result<BigInt> a1 = slice.result(this, 1);
        slice = null;

        BigInt da1 = a2.get().add(a0.get());
        BigInt da2 = da1.add(a1.get());

        Result<BigInt> v0 = square.result(a0.get());
        Result<BigInt> v1 = square.result(da2);
        Result<BigInt> vm1 = square.result(da1.subtract(a1.get()));
        Result<BigInt> vinf = square.result(a2.get());
        a1 = null;

        BigInt t1 = v1.get().subtract(v0.get());
        da2 = da2.add(a2.get()).shiftLeft(1).subtract(a0.get()).square().subtract(vm1.get()).divide(THREE).subtract(t1).shiftRight(1).subtract(vinf.get().shiftLeft(1));
        a2 = null;
        a0 = null;
        da1 = v1.get().subtract(vm1.get()).shiftRight(1);
        v1 = null;
        vm1 = null;

        return vinf.get().shiftLeft(len)
                .add(da2).shiftLeft(len)
                .add(t1.subtract(da1).subtract(vinf.get())).shiftLeft(len)
                .add(da1.subtract(da2)).shiftLeft(len)
                .add(v0.get());


        // NON PARALLEL VERSION
        /*BigInt v0, v1, v2, vm1, vinf, t1, t2, tm1, da1;
        v0 = a0.get().square();
        da1 = a2.get().add(a0.get());
        vm1 = da1.subtract(a1.get()).square();
        da1 = da1.add(a1.get());
        v1 = da1.square();
        vinf = a2.get().square();
        v2 = da1.add(a2.get()).shiftLeft(1).subtract(a0.get()).square();
        t2 = v2.subtract(vm1).divide(THREE);
        tm1 = v1.subtract(vm1).shiftRight(1);
        t1 = v1.subtract(v0);
        t2 = t2.subtract(t1).shiftRight(1);
        t1 = t1.subtract(tm1).subtract(vinf);
        t2 = t2.subtract(vinf.shiftLeft(1));
        tm1 = tm1.subtract(t2);
        return vinf.shiftLeft(len).add(t2).shiftLeft(len).add(t1).shiftLeft(len).add(tm1).shiftLeft(len).add(v0);*/
    }

    /**
     * Computes the <a href="http://en.wikipedia.org/wiki/Pochhammer_symbol">Falling Factorial</a>
     * <code>this! / (this-n)!</code>
     *
     * @param n The falling factor to substract from this number
     * @return this! / (this-n)!
     */
    public BigInt factorialFalling(BigInt n) {
        if (n.signum() == 0) return ONE;
        return subtract(n).add(ONE).productTo(this);
    }

    /**
     * Computes the <a href="http://en.wikipedia.org/wiki/Pochhammer_symbol">Falling Factorial</a>
     * <code>this! / (this-n)!</code>
     *
     * @param n The falling factor to substract from this number
     * @return this! / (this-n)!
     */
    public BigInt factorialFalling(long n) {
        return factorialFalling(big(n));
    }

    /**
     * Computes the <a href="http://en.wikipedia.org/wiki/Factorial">Factorial of this number</a>
     * <code>this!</code>
     *
     * @return this!
     */
    public BigInt factorial() {
        //TODO: FACT - switch to prime swing parallel
        return factorialPrimeSwing();
    }

    /**
     * Computes the <a href="http://en.wikipedia.org/wiki/Factorial">Factorial of this number</a>
     * <code>this!</code> by using <a href="http://www.luschny.de/math/factorial/index.html">Luschny's PrimeSwing</a>
     *
     * @return this!
     */
    public BigInt factorialPrimeSwing() {
        if (bitLength() > 31)
            throw new ArithmeticException("Number too big");
        int number = toInt();
        if (number <= 20) return big(Factorial.lookup(number));
        Sieve sieve = Sieve.to(number);
        double pow2Count = Math.log(number) * 1.4426950408889634D;
        int[] primeList = new int[(int) (2.0 * ((int) Math.sqrt(number) + number / (pow2Count - 1)))];
        int[] toStwing = new int[(int) pow2Count];
        for (int i = toStwing.length - 1, n = number; i >= 0; i--, n >>>= 1)
            toStwing[i] = n;
        BigInt recFactorial = ONE;
        for (int i = 0, max = toStwing.length; i < max; i++)
            recFactorial = recFactorial.square().multiply(swing(toStwing[i], sieve, primeList));
        return recFactorial.shiftLeft(number - Integer.bitCount(number));
    }

    private static final int[] smallOddSwing = {1, 1, 1, 3, 3, 15, 5, 35, 35, 315, 63, 693, 231, 3003, 429, 6435, 6435, 109395, 12155, 230945, 46189, 969969, 88179, 2028117, 676039, 16900975, 1300075, 35102025, 5014575, 145422675, 9694845, 300540195, 300540195};

    private static BigInt swing(final int number, Sieve sieve, int[] primeList) {
        if (number < 33) return big(smallOddSwing[number]);
        final int sqrtN = (int) Math.sqrt(number);
        final int[] pIter0 = sieve.asArray(3, sqrtN);
        final int[] pIter1 = sieve.asArray(sqrtN + 1, number / 3);
        int count = 0;
        for (int prime : pIter0) {
            int q = number, p = 1;
            while ((q /= prime) > 0) if ((q & 1) == 1) p *= prime;
            if (p > 1) primeList[count++] = p;
        }
        for (int prime : pIter1)
            if (((number / prime) & 1) == 1)
                primeList[count++] = prime;
        BigInt primorial = sieve.primorial((number >>> 1) + 1, number);
        return primorial.multiply(Primes.product(primeList, 0, count));
    }

    /**
     * Computes the <a href="http://en.wikipedia.org/wiki/Factorial">Factorial of this number</a>
     * <code>this!</code> by using <a href="http://www.luschny.de/math/factorial/index.html">Luschny's PrimeSwingParallel</a>
     *
     * @return this!
     */
    public BigInt factorialPrimeSwingParallel() {
        if (bitLength() > 31)
            throw new ArithmeticException("Number too big");
        int number = toInt();
        if (number <= 20) return big(Factorial.lookup(number));
        int proc = Runtime.getRuntime().availableProcessors();
        ExecutorService poolExe = Executors.newFixedThreadPool(proc);
        Sieve sieve = Sieve.to(number);
        int log2n = 31 - Integer.numberOfLeadingZeros(number);
        ArrayList<Callable<BigInt>> swingTasks = new ArrayList<Callable<BigInt>>(log2n);
        AtomicInteger taskCounter = new AtomicInteger(0);
        // -- It is more efficient to add the big swings
        // -- first and the small ones later!
        for (int n = number; n > 32; n >>= 1) {
            swingTasks.add(new Swing(sieve, n));
            taskCounter.incrementAndGet();
        }
        BigInt fact = null;
        List<Future<BigInt>> swings;
        try {
            swings = poolExe.invokeAll(swingTasks);
            fact = recFactorial(number, swings, taskCounter).shiftLeft(number - Integer.bitCount(number));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        poolExe.shutdownNow();
        return fact;
    }

    private static BigInt recFactorial(int n, List<Future<BigInt>> swings, AtomicInteger taskCounter)
            throws ExecutionException, InterruptedException {
        if (n < 2) return ONE;
        BigInt recFact = recFactorial(n / 2, swings, taskCounter).square();
        BigInt swing;
        if (n <= 32)
            swing = big(smallOddSwing[n]);
        else
            swing = swings.get(taskCounter.decrementAndGet()).get();
        return recFact.multiply(swing);
    }

    private static final class Swing implements Callable<BigInt> {
        private final Sieve sieve;
        private final int n;

        public Swing(Sieve sieve, int n) {
            this.sieve = sieve;
            this.n = n;
        }

        public BigInt call() throws Exception {
            FutureTask<BigInt> primorial = new FutureTask<BigInt>(new Callable<BigInt>() {
                public BigInt call() {
                    return sieve.primorial(n / 2 + 1, n);
                }
            });
            new Thread(primorial).start();
            BigInt primeProduct = lowSwing();
            return primeProduct.multiply(primorial.get());
        }

        private BigInt lowSwing() {
            int sqrtN = (int) Math.floor(Math.sqrt(n));
            final int[] pIter0 = sieve.asArray(3, sqrtN);
            final int[] pIter1 = sieve.asArray(sqrtN + 1, n / 3);
            int piN = pIter0.length + pIter1.length;
            final int[] primeList = new int[piN];
            int count = 0;
            for (int prime : pIter0) {
                int q = n, p = 1;
                while ((q /= prime) > 0)
                    if ((q & 1) == 1)
                        p *= prime;
                if (p > 1)
                    primeList[count++] = p;
            }
            for (int prime : pIter1)
                if (((n / prime) & 1) == 1)
                    primeList[count++] = prime;
            return Primes.product(primeList, 0, count);
        }
    }

    /**
     * @return Returns a prime sieve from 2 up to this number
     */
    public Sieve primeSieve() {
        if (bitLength() > 31) throw new ArithmeticException("Number too big");
        return Sieve.to(toInt());
    }

    /**
     * Computes the <a href="http://en.wikipedia.org/wiki/Binomial_coefficient">Binomial Coefficient</a>
     * <code>C(this, k)</code>
     * Algorithm from http://www.luschny.de/math/factorial/FastBinomialFunction.html
     *
     * @param k Coefficient
     * @return The binomial coefficient
     */
    public BigInt binomial(int k) {
        if (bitLength() > 31)
            throw new ArithmeticException("Number too big");
        final int n = toInt();
        if (0 > k || k > n)
            throw new ArithmeticException("Binomial: 0 <= k and k <= n required, but n was " + n + " and k was " + k);
        final int n2 = n >>> 1;
        if (k > n2) k = n - k;
        if(k == 0) return ONE;
        if(k == 1) return big(n);
        final int rootN = (int) Math.floor(Math.sqrt(n));
        final int[] primes = primeSieve().asArray();
        final int[] factors = new int[primes.length];
        BigInt binom = ONE;
        int pos = 0;
        // equivalent to a nextPrime() function.
        // prime runs through the prime numbers 1 < prime <= n
        for (int prime : primes) {
            if (prime > n - k) {
                factors[pos++] = prime;
                continue;
            }
            if (prime > n2)
                continue;
            if (prime > rootN) {
                if (n % prime < k % prime)
                    factors[pos++] = prime;
                continue;
            }
            int exp = 0, r = 0, N = n, K = k;
            while (N > 0) {
                r = (N % prime) < (K % prime + r) ? 1 : 0;
                exp += r;
                N /= prime;
                K /= prime;
            }
            if (exp > 0)
                binom = binom.multiply(big(prime).pow(exp));
        }
        return binom.multiply(Primes.product(factors, 0, pos));
    }

    public BigInt binomial(BigInt k) {
        if (k.signum() == -1 || k.compareTo(this) > 0)
            throw new ArithmeticException("Binomial: 0 <= k and k <= n required");
        if (bitLength() <= 31 && k.bitLength() <= 31)
            return binomial(k.toInt());
        final BigInt n2 = this.shiftLeft(1);
        if (k.compareTo(n2) > 0) k = this.subtract(k);
        if(k.signum() == 0) return ONE;
        if(k.equals(ONE)) return this;
        return this.factorialFalling(k).divide(k.factorial());
    }

}

//TODO: ADD - add methods: factorize() + polar rho, fibonacci(), parallel fibonacci, isFibonacci(), quadratic residue (http://primes.utm.edu/glossary/xpage/QuadraticResidue.html)
//TODO: ADD - BigIntegerMath.java: CRT, Quadrati, pMinusOneFactor (Pollard p-1)

//TODO: PRIME - http://en.wikipedia.org/wiki/AKS_primality_test + ZIP AKS
//TODO: PRIME - http://en.wikipedia.org/wiki/Adleman%E2%80%93Pomerance%E2%80%93Rumely_primality_test + ECM pour APR-CL (ECM is a highly efficient method of finding prime factors in the 35-55 digit range.)
//TODO: PRIME - http://en.wikipedia.org/wiki/Elliptic_curve_primality_proving + ECM applet

//TODO: PERF - productTo: find an algorithm to multiply n..m consecutive numbers
//TODO: PERF - make wrapper for optimized BigInteger + BigIntegerMath.java, apflot, jscience, ... + Javolution contexts and factories + impl. paralell computing (factorial, products, ...)
//TODO: PERF - make wrapper for GMP java avec https://jna.dev.java.net/ + http://code.google.com/p/jnaerator/
//TODO: PERF - optimize GCD for mersenne numbers: http://www.garlic.com/~wedgingt/mersenne.html (Lemma 2: Knuth's GCD Lemma)

//TODO: FACT - refactor FactorialPrimeParallelSwingLuschny
//TODO: FACT - improve perf to match those from FactorialPrimeParallelSwingLuschny

