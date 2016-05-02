<<<<<<< HEAD
/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.lang;

/**
 * The wrapper for the primitive type {@code double}.
 *
 * @see java.lang.Number
 * @since 1.0
 */
public final class Double extends Number implements Comparable<Double> {
    static final int EXPONENT_BIAS = 1023;

    static final int EXPONENT_BITS = 12;
    static final int MANTISSA_BITS = 52;
    static final int NON_MANTISSA_BITS = 12;

    static final long SIGN_MASK     = 0x8000000000000000L;
    static final long EXPONENT_MASK = 0x7ff0000000000000L;
    static final long MANTISSA_MASK = 0x000fffffffffffffL;

    private static final long serialVersionUID = -9172774392245257468L;

    /**
     * The value which the receiver represents.
     */
    private final double value;

    /**
     * Constant for the maximum {@code double} value, (2 - 2<sup>-52</sup>) *
     * 2<sup>1023</sup>.
     */
    public static final double MAX_VALUE = 1.79769313486231570e+308;

    /**
     * Constant for the minimum {@code double} value, 2<sup>-1074</sup>.
     */
    public static final double MIN_VALUE = 5e-324;

    /* 4.94065645841246544e-324 gets rounded to 9.88131e-324 */

    /**
     * Constant for the Not-a-Number (NaN) value of the {@code double} type.
     */
    public static final double NaN = 0.0 / 0.0;

    /**
     * Constant for the positive infinity value of the {@code double} type.
     */
    public static final double POSITIVE_INFINITY = 1.0 / 0.0;

    /**
     * Constant for the negative infinity value of the {@code double} type.
     */
    public static final double NEGATIVE_INFINITY = -1.0 / 0.0;

    /**
     * Constant for the smallest positive normal value of the {@code double} type.
     *
     * @since 1.6
     */
    public static final double MIN_NORMAL = 2.2250738585072014E-308;

    /**
     * Maximum base-2 exponent that a finite value of the {@code double} type may have.
     * Equal to {@code Math.getExponent(Double.MAX_VALUE)}.
     *
     * @since 1.6
     */
    public static final int MAX_EXPONENT = 1023;

    /**
     * Minimum base-2 exponent that a normal value of the {@code double} type may have.
     * Equal to {@code Math.getExponent(Double.MIN_NORMAL)}.
     *
     * @since 1.6
     */
    public static final int MIN_EXPONENT = -1022;

    /**
     * The {@link Class} object that represents the primitive type {@code
     * double}.
     *
     * @since 1.1
     */
    @SuppressWarnings("unchecked")
    public static final Class<Double> TYPE
            = (Class<Double>) double[].class.getComponentType();
    // Note: Double.TYPE can't be set to "double.class", since *that* is
    // defined to be "java.lang.Double.TYPE";

    /**
     * Constant for the number of bits needed to represent a {@code double} in
     * two's complement form.
     *
     * @since 1.5
     */
    public static final int SIZE = 64;

    /**
     * Constructs a new {@code Double} with the specified primitive double
     * value.
     *
     * @param value
     *            the primitive double value to store in the new instance.
     */
    public Double(double value) {
        this.value = value;
    }

    /**
     * Constructs a new {@code Double} from the specified string.
     *
     * @param string
     *            the string representation of a double value.
     * @throws NumberFormatException
     *             if {@code string} cannot be parsed as a double value.
     * @see #parseDouble(String)
     */
    public Double(String string) throws NumberFormatException {
        this(parseDouble(string));
    }

    /**
     * Compares this object to the specified double object to determine their
     * relative order. There are two special cases:
     * <ul>
     * <li>{@code Double.NaN} is equal to {@code Double.NaN} and it is greater
     * than any other double value, including {@code Double.POSITIVE_INFINITY};</li>
     * <li>+0.0d is greater than -0.0d</li>
     * </ul>
     *
     * @param object
     *            the double object to compare this object to.
     * @return a negative value if the value of this double is less than the
     *         value of {@code object}; 0 if the value of this double and the
     *         value of {@code object} are equal; a positive value if the value
     *         of this double is greater than the value of {@code object}.
     * @throws NullPointerException
     *             if {@code object} is {@code null}.
     * @see java.lang.Comparable
     * @since 1.2
     */
    public int compareTo(Double object) {
        return compare(value, object.value);
    }

    @Override
    public byte byteValue() {
        return (byte) value;
    }

    /**
     * Returns an integer corresponding to the bits of the given
     * <a href="http://en.wikipedia.org/wiki/IEEE_754-1985">IEEE 754</a> double precision
     * {@code value}. All <em>Not-a-Number (NaN)</em> values are converted to a single NaN
     * representation ({@code 0x7ff8000000000000L}) (compare to {@link #doubleToRawLongBits}).
     */
    public static native long doubleToLongBits(double value);

    /**
     * Returns an integer corresponding to the bits of the given
     * <a href="http://en.wikipedia.org/wiki/IEEE_754-1985">IEEE 754</a> double precision
     * {@code value}. <em>Not-a-Number (NaN)</em> values are preserved (compare
     * to {@link #doubleToLongBits}).
     */
    public static native long doubleToRawLongBits(double value);

    /**
     * Gets the primitive value of this double.
     *
     * @return this object's primitive value.
     */
    @Override
    public double doubleValue() {
        return value;
    }

    /**
     * Tests this double for equality with {@code object}.
     * To be equal, {@code object} must be an instance of {@code Double} and
     * {@code doubleToLongBits} must give the same value for both objects.
     *
     * <p>Note that, unlike {@code ==}, {@code -0.0} and {@code +0.0} compare
     * unequal, and {@code NaN}s compare equal by this method.
     *
     * @param object
     *            the object to compare this double with.
     * @return {@code true} if the specified object is equal to this
     *         {@code Double}; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object object) {
        return (object instanceof Double) &&
                (doubleToLongBits(this.value) == doubleToLongBits(((Double) object).value));
    }

    @Override
    public float floatValue() {
        return (float) value;
    }

    @Override
    public int hashCode() {
        long v = doubleToLongBits(value);
        return (int) (v ^ (v >>> 32));
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    /**
     * Indicates whether this object represents an infinite value.
     *
     * @return {@code true} if the value of this double is positive or negative
     *         infinity; {@code false} otherwise.
     */
    public boolean isInfinite() {
        return isInfinite(value);
    }

    /**
     * Indicates whether the specified double represents an infinite value.
     *
     * @param d
     *            the double to check.
     * @return {@code true} if the value of {@code d} is positive or negative
     *         infinity; {@code false} otherwise.
     */
    public static boolean isInfinite(double d) {
        return (d == POSITIVE_INFINITY) || (d == NEGATIVE_INFINITY);
    }

    /**
     * Indicates whether this object is a <em>Not-a-Number (NaN)</em> value.
     *
     * @return {@code true} if this double is <em>Not-a-Number</em>;
     *         {@code false} if it is a (potentially infinite) double number.
     */
    public boolean isNaN() {
        return isNaN(value);
    }

    /**
     * Indicates whether the specified double is a <em>Not-a-Number (NaN)</em>
     * value.
     *
     * @param d
     *            the double value to check.
     * @return {@code true} if {@code d} is <em>Not-a-Number</em>;
     *         {@code false} if it is a (potentially infinite) double number.
     */
    public static boolean isNaN(double d) {
        return d != d;
    }

    /**
     * Returns the <a href="http://en.wikipedia.org/wiki/IEEE_754-1985">IEEE 754</a>
     * double precision float corresponding to the given {@code bits}.
     */
    public static native double longBitsToDouble(long bits);

    @Override
    public long longValue() {
        return (long) value;
    }

    /**
     * Parses the specified string as a double value.
     *
     * @param string
     *            the string representation of a double value.
     * @return the primitive double value represented by {@code string}.
     * @throws NumberFormatException
     *             if {@code string} cannot be parsed as a double value.
     */
    public static double parseDouble(String string) throws NumberFormatException {
        return StringToReal.parseDouble(string);
    }

    @Override
    public short shortValue() {
        return (short) value;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * specified double value.
     *
     * @param d
     *             the double to convert to a string.
     * @return a printable representation of {@code d}.
     */
    public static String toString(double d) {
        return RealToString.getInstance().doubleToString(d);
    }

    /**
     * Parses the specified string as a double value.
     *
     * @param string
     *            the string representation of a double value.
     * @return a {@code Double} instance containing the double value represented
     *         by {@code string}.
     * @throws NumberFormatException
     *             if {@code string} cannot be parsed as a double value.
     * @see #parseDouble(String)
     */
    public static Double valueOf(String string) throws NumberFormatException {
        return parseDouble(string);
    }

    /**
     * Compares the two specified double values. There are two special cases:
     * <ul>
     * <li>{@code Double.NaN} is equal to {@code Double.NaN} and it is greater
     * than any other double value, including {@code Double.POSITIVE_INFINITY};</li>
     * <li>+0.0d is greater than -0.0d</li>
     * </ul>
     *
     * @param double1
     *            the first value to compare.
     * @param double2
     *            the second value to compare.
     * @return a negative value if {@code double1} is less than {@code double2};
     *         0 if {@code double1} and {@code double2} are equal; a positive
     *         value if {@code double1} is greater than {@code double2}.
     */
    public static int compare(double double1, double double2) {
        // Non-zero, non-NaN checking.
        if (double1 > double2) {
            return 1;
        }
        if (double2 > double1) {
            return -1;
        }
        if (double1 == double2 && 0.0d != double1) {
            return 0;
        }

        // NaNs are equal to other NaNs and larger than any other double
        if (isNaN(double1)) {
            if (isNaN(double2)) {
                return 0;
            }
            return 1;
        } else if (isNaN(double2)) {
            return -1;
        }

        // Deal with +0.0 and -0.0
        long d1 = doubleToRawLongBits(double1);
        long d2 = doubleToRawLongBits(double2);
        // The below expression is equivalent to:
        // (d1 == d2) ? 0 : (d1 < d2) ? -1 : 1
        return (int) ((d1 >> 63) - (d2 >> 63));
    }

    /**
     * Returns a {@code Double} instance for the specified double value.
     *
     * @param d
     *            the double value to store in the instance.
     * @return a {@code Double} instance containing {@code d}.
     * @since 1.5
     */
    public static Double valueOf(double d) {
        return new Double(d);
    }

    /**
     * Converts the specified double into its hexadecimal string representation.
     *
     * @param d
     *            the double to convert.
     * @return the hexadecimal string representation of {@code d}.
     * @since 1.5
     */
    public static String toHexString(double d) {
        /*
         * Reference: http://en.wikipedia.org/wiki/IEEE_754-1985
         */
        if (d != d) {
            return "NaN";
        }
        if (d == POSITIVE_INFINITY) {
            return "Infinity";
        }
        if (d == NEGATIVE_INFINITY) {
            return "-Infinity";
        }

        long bitValue = doubleToLongBits(d);

        boolean negative = (bitValue & 0x8000000000000000L) != 0;
        // mask exponent bits and shift down
        long exponent = (bitValue & 0x7FF0000000000000L) >>> 52;
        // mask significand bits and shift up
        long significand = bitValue & 0x000FFFFFFFFFFFFFL;

        if (exponent == 0 && significand == 0) {
            return (negative ? "-0x0.0p0" : "0x0.0p0");
        }

        StringBuilder hexString = new StringBuilder(10);
        if (negative) {
            hexString.append("-0x");
        } else {
            hexString.append("0x");
        }

        if (exponent == 0) { // denormal (subnormal) value
            hexString.append("0.");
            // significand is 52-bits, so there can be 13 hex digits
            int fractionDigits = 13;
            // remove trailing hex zeros, so Integer.toHexString() won't print
            // them
            while ((significand != 0) && ((significand & 0xF) == 0)) {
                significand >>>= 4;
                fractionDigits--;
            }
            // this assumes Integer.toHexString() returns lowercase characters
            String hexSignificand = Long.toHexString(significand);

            // if there are digits left, then insert some '0' chars first
            if (significand != 0 && fractionDigits > hexSignificand.length()) {
                int digitDiff = fractionDigits - hexSignificand.length();
                while (digitDiff-- != 0) {
                    hexString.append('0');
                }
            }
            hexString.append(hexSignificand);
            hexString.append("p-1022");
        } else { // normal value
            hexString.append("1.");
            // significand is 52-bits, so there can be 13 hex digits
            int fractionDigits = 13;
            // remove trailing hex zeros, so Integer.toHexString() won't print
            // them
            while ((significand != 0) && ((significand & 0xF) == 0)) {
                significand >>>= 4;
                fractionDigits--;
            }
            // this assumes Integer.toHexString() returns lowercase characters
            String hexSignificand = Long.toHexString(significand);

            // if there are digits left, then insert some '0' chars first
            if (significand != 0 && fractionDigits > hexSignificand.length()) {
                int digitDiff = fractionDigits - hexSignificand.length();
                while (digitDiff-- != 0) {
                    hexString.append('0');
                }
            }

            hexString.append(hexSignificand);
            hexString.append('p');
            // remove exponent's 'bias' and convert to a string
            hexString.append(Long.toString(exponent - 1023));
        }
        return hexString.toString();
    }
}
=======
package org.rrd4j.core;

import java.io.IOException;

/**
 * Class to represent archive values for a single datasource. Robin class is the heart of
 * the so-called "round robin database" concept. Basically, each Robin object is a
 * fixed length array of double values. Each double value represents consolidated, archived
 * value for the specific timestamp. When the underlying array of double values gets completely
 * filled, new values will replace the oldest ones.<p>
 * <p/>
 * Robin object does not hold values in memory - such object could be quite large.
 * Instead of it, Robin reads them from the backend I/O only when necessary.
 *
 * @author Sasa Markovic
 */
class RobinArray implements Robin {
    private final Archive parentArc;
    private final RrdInt pointer;
    private final RrdDoubleArray values;
    private int rows;

    RobinArray(Archive parentArc, int rows, boolean shouldInitialize) throws IOException {
        this.parentArc = parentArc;
        this.pointer = new RrdInt(this);
        this.values = new RrdDoubleArray(this, rows);
        this.rows = rows;
        if (shouldInitialize) {
            pointer.set(0);
            values.set(0, Double.NaN, rows);
        }
    }

    /* (non-Javadoc)
     * @see org.rrd4j.core.Robin#getValues()
     */
    public double[] getValues() throws IOException {
        return getValues(0, rows);
    }

    // stores single value
    public void store(double newValue) throws IOException {
        int position = pointer.get();
        values.set(position, newValue);
        pointer.set((position + 1) % rows);
    }

    // stores the same value several times
    public void bulkStore(double newValue, int bulkCount) throws IOException {
        assert bulkCount <= rows: "Invalid number of bulk updates: " + bulkCount + " rows=" + rows;

        int position = pointer.get();

        // update tail
        int tailUpdateCount = Math.min(rows - position, bulkCount);

        values.set(position, newValue, tailUpdateCount);
        pointer.set((position + tailUpdateCount) % rows);

        // do we need to update from the start?
        int headUpdateCount = bulkCount - tailUpdateCount;
        if (headUpdateCount > 0) {
            values.set(0, newValue, headUpdateCount);
            pointer.set(headUpdateCount);
        }
    }

    public void update(double[] newValues) throws IOException {
        assert rows == newValues.length: "Invalid number of robin values supplied (" + newValues.length +
        "), exactly " + rows + " needed";
        pointer.set(0);
        values.writeDouble(0, newValues);
    }

    /* (non-Javadoc)
     * @see org.rrd4j.core.Robin#setValues(double)
     */
    public void setValues(double... newValues) throws IOException {
        if (rows != newValues.length) {
            throw new IllegalArgumentException("Invalid number of robin values supplied (" + newValues.length +
                    "), exactly " + rows + " needed");
        }
        update(newValues);
    }

    /* (non-Javadoc)
     * @see org.rrd4j.core.Robin#setValues(double)
     */
    public void setValues(double newValue) throws IOException {
        double[] values = new double[rows];
        for (int i = 0; i < values.length; i++) {
            values[i] = newValue;
        }
        update(values);
    }

    public String dump() throws IOException {
        StringBuilder buffer = new StringBuilder("Robin " + pointer.get() + "/" + rows + ": ");
        double[] values = getValues();
        for (double value : values) {
            buffer.append(Util.formatDouble(value, true)).append(" ");
        }
        buffer.append("\n");
        return buffer.toString();
    }

    /* (non-Javadoc)
     * @see org.rrd4j.core.Robin#getValue(int)
     */
    public double getValue(int index) throws IOException {
        int arrayIndex = (pointer.get() + index) % rows;
        return values.get(arrayIndex);
    }

    /* (non-Javadoc)
     * @see org.rrd4j.core.Robin#setValue(int, double)
     */
    public void setValue(int index, double value) throws IOException {
        int arrayIndex = (pointer.get() + index) % rows;
        values.set(arrayIndex, value);
    }

    public double[] getValues(int index, int count) throws IOException {
        assert count <= rows: "Too many values requested: " + count + " rows=" + rows;

        int startIndex = (pointer.get() + index) % rows;
        int tailReadCount = Math.min(rows - startIndex, count);
        double[] tailValues = values.get(startIndex, tailReadCount);
        if (tailReadCount < count) {
            int headReadCount = count - tailReadCount;
            double[] headValues = values.get(0, headReadCount);
            double[] values = new double[count];
            int k = 0;
            for (double tailValue : tailValues) {
                values[k++] = tailValue;
            }
            for (double headValue : headValues) {
                values[k++] = headValue;
            }
            return values;
        }
        else {
            return tailValues;
        }
    }

    /* (non-Javadoc)
     * @see org.rrd4j.core.Robin#getParent()
     */
    public Archive getParent() {
        return parentArc;
    }

    /* (non-Javadoc)
     * @see org.rrd4j.core.Robin#getSize()
     */
    public int getSize() {
        return rows;
    }

    /* (non-Javadoc)
     * @see org.rrd4j.core.Robin#copyStateTo(org.rrd4j.core.RrdUpdater)
     */
    public void copyStateTo(RrdUpdater other) throws IOException {
        if (!(other instanceof Robin)) {
            throw new IllegalArgumentException(
                    "Cannot copy Robin object to " + other.getClass().getName());
        }
        Robin robin = (Robin) other;
        int rowsDiff = rows - robin.getSize();
        for (int i = 0; i < robin.getSize(); i++) {
            int j = i + rowsDiff;
            robin.store(j >= 0 ? getValue(j) : Double.NaN);
        }
    }

    /* (non-Javadoc)
     * @see org.rrd4j.core.Robin#filterValues(double, double)
     */
    public void filterValues(double minValue, double maxValue) throws IOException {
        for (int i = 0; i < rows; i++) {
            double value = values.get(i);
            if (!Double.isNaN(minValue) && !Double.isNaN(value) && minValue > value) {
                values.set(i, Double.NaN);
            }
            if (!Double.isNaN(maxValue) && !Double.isNaN(value) && maxValue < value) {
                values.set(i, Double.NaN);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.rrd4j.core.Robin#getRrdBackend()
     */
    public RrdBackend getRrdBackend() {
        return parentArc.getRrdBackend();
    }

    /* (non-Javadoc)
     * @see org.rrd4j.core.Robin#getRrdAllocator()
     */
    public RrdAllocator getRrdAllocator() {
        return parentArc.getRrdAllocator();
    }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

