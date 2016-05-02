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
     * Constant for the Positive Infinity value of the {@code double} type.
     */
    public static final double POSITIVE_INFINITY = 1.0 / 0.0;

    /**
     * Constant for the Negative Infinity value of the {@code double} type.
     */
    public static final double NEGATIVE_INFINITY = -1.0 / 0.0;

    /**
     * The {@link Class} object that represents the primitive type {@code
     * double}.
     *
     * @since 1.1
     */
    @SuppressWarnings("unchecked")
    public static final Class<Double> TYPE
            = (Class<Double>) double[].class.getComponentType();

    // Note: This can't be set to "double.class", since *that* is
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
     *             if {@code string} can not be decoded into a double value.
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
     * Converts the specified double value to a binary representation conforming
     * to the IEEE 754 floating-point double precision bit layout. All
     * <em>Not-a-Number (NaN)</em> values are converted to a single NaN
     * representation ({@code 0x7ff8000000000000L}).
     *
     * @param value
     *            the double value to convert.
     * @return the IEEE 754 floating-point double precision representation of
     *         {@code value}.
     * @see #doubleToRawLongBits(double)
     * @see #longBitsToDouble(long)
     */
    public static native long doubleToLongBits(double value);

    /**
     * Converts the specified double value to a binary representation conforming
     * to the IEEE 754 floating-point double precision bit layout.
     * <em>Not-a-Number (NaN)</em> values are preserved.
     *
     * @param value
     *            the double value to convert.
     * @return the IEEE 754 floating-point double precision representation of
     *         {@code value}.
     * @see #doubleToLongBits(double)
     * @see #longBitsToDouble(long)
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
        return (object == this)
                || (object instanceof Double)
                && (doubleToLongBits(this.value) == doubleToLongBits(((Double) object).value));
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
     * Converts the specified IEEE 754 floating-point double precision bit
     * pattern to a Java double value.
     *
     * @param bits
     *            the IEEE 754 floating-point double precision representation of
     *            a double value.
     * @return the double value converted from {@code bits}.
     * @see #doubleToLongBits(double)
     * @see #doubleToRawLongBits(double)
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
     *             if {@code string} is {@code null}, has a length of zero or
     *             can not be parsed as a double value.
     */
    public static double parseDouble(String string)
            throws NumberFormatException {
        return org.apache.harmony.luni.util.FloatingPointParser
                .parseDouble(string);
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
        return org.apache.harmony.luni.util.NumberConverter.convert(d);
    }

    /**
     * Parses the specified string as a double value.
     *
     * @param string
     *            the string representation of a double value.
     * @return a {@code Double} instance containing the double value represented
     *         by {@code string}.
     * @throws NumberFormatException
     *             if {@code string} is {@code null}, has a length of zero or
     *             can not be parsed as a double value.
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
         * Reference: http://en.wikipedia.org/wiki/IEEE_754
         */
        if (d != d) {
            return "NaN"; //$NON-NLS-1$
        }
        if (d == POSITIVE_INFINITY) {
            return "Infinity"; //$NON-NLS-1$
        }
        if (d == NEGATIVE_INFINITY) {
            return "-Infinity"; //$NON-NLS-1$
        }

        long bitValue = doubleToLongBits(d);

        boolean negative = (bitValue & 0x8000000000000000L) != 0;
        // mask exponent bits and shift down
        long exponent = (bitValue & 0x7FF0000000000000L) >>> 52;
        // mask significand bits and shift up
        long significand = bitValue & 0x000FFFFFFFFFFFFFL;

        if (exponent == 0 && significand == 0) {
            return (negative ? "-0x0.0p0" : "0x0.0p0"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        StringBuilder hexString = new StringBuilder(10);
        if (negative) {
            hexString.append("-0x"); //$NON-NLS-1$
        } else {
            hexString.append("0x"); //$NON-NLS-1$
        }

        if (exponent == 0) { // denormal (subnormal) value
            hexString.append("0."); //$NON-NLS-1$
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
            hexString.append("p-1022"); //$NON-NLS-1$
        } else { // normal value
            hexString.append("1."); //$NON-NLS-1$
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

import org.rrd4j.DsType;

import java.io.IOException;

/**
 * Class to represent single datasource within RRD. Each datasource object holds the
 * following information: datasource definition (once set, never changed) and
 * datasource state variables (changed whenever RRD gets updated).<p>
 * <p/>
 * Normally, you don't need to manipulate Datasource objects directly, it's up to
 * Rrd4j framework to do it for you.
 *
 * @author Sasa Markovic
 */
public class Datasource implements RrdUpdater {
    private static final double MAX_32_BIT = Math.pow(2, 32);
    private static final double MAX_64_BIT = Math.pow(2, 64);

    private final RrdDb parentDb;

    // definition
    private final RrdString dsName, dsType;
    private final RrdLong heartbeat;
    private final RrdDouble minValue, maxValue;

    // state variables
    private RrdDouble lastValue;
    private RrdLong nanSeconds;
    private RrdDouble accumValue;

    Datasource(RrdDb parentDb, DsDef dsDef) throws IOException {
        boolean shouldInitialize = dsDef != null;
        this.parentDb = parentDb;
        dsName = new RrdString(this);
        dsType = new RrdString(this);
        heartbeat = new RrdLong(this);
        minValue = new RrdDouble(this);
        maxValue = new RrdDouble(this);
        lastValue = new RrdDouble(this);
        accumValue = new RrdDouble(this);
        nanSeconds = new RrdLong(this);
        if (shouldInitialize) {
            dsName.set(dsDef.getDsName());
            dsType.set(dsDef.getDsType().name());
            heartbeat.set(dsDef.getHeartbeat());
            minValue.set(dsDef.getMinValue());
            maxValue.set(dsDef.getMaxValue());
            lastValue.set(Double.NaN);
            accumValue.set(0.0);
            Header header = parentDb.getHeader();
            nanSeconds.set(header.getLastUpdateTime() % header.getStep());
        }
    }

    Datasource(RrdDb parentDb, DataImporter reader, int dsIndex) throws IOException {
        this(parentDb, null);
        dsName.set(reader.getDsName(dsIndex));
        dsType.set(reader.getDsType(dsIndex));
        heartbeat.set(reader.getHeartbeat(dsIndex));
        minValue.set(reader.getMinValue(dsIndex));
        maxValue.set(reader.getMaxValue(dsIndex));
        lastValue.set(reader.getLastValue(dsIndex));
        accumValue.set(reader.getAccumValue(dsIndex));
        nanSeconds.set(reader.getNanSeconds(dsIndex));
    }

    String dump() throws IOException {
        return "== DATASOURCE ==\n" +
                "DS:" + dsName.get() + ":" + dsType.get() + ":" +
                heartbeat.get() + ":" + minValue.get() + ":" +
                maxValue.get() + "\nlastValue:" + lastValue.get() +
                " nanSeconds:" + nanSeconds.get() +
                " accumValue:" + accumValue.get() + "\n";
    }

    /**
     * Returns datasource name.
     *
     * @return Datasource name
     * @throws IOException Thrown in case of I/O error
     */
    public String getName() throws IOException {
        return dsName.get();
    }

    /**
     * Returns datasource type (GAUGE, COUNTER, DERIVE, ABSOLUTE).
     *
     * @return Datasource type.
     * @throws IOException Thrown in case of I/O error
     */
    public DsType getType() throws IOException {
        return DsType.valueOf(dsType.get());
    }

    /**
     * Returns datasource heartbeat
     *
     * @return Datasource heartbeat
     * @throws IOException Thrown in case of I/O error
     */

    public long getHeartbeat() throws IOException {
        return heartbeat.get();
    }

    /**
     * Returns minimal allowed value for this datasource.
     *
     * @return Minimal value allowed.
     * @throws IOException Thrown in case of I/O error
     */
    public double getMinValue() throws IOException {
        return minValue.get();
    }

    /**
     * Returns maximal allowed value for this datasource.
     *
     * @return Maximal value allowed.
     * @throws IOException Thrown in case of I/O error
     */
    public double getMaxValue() throws IOException {
        return maxValue.get();
    }

    /**
     * Returns last known value of the datasource.
     *
     * @return Last datasource value.
     * @throws IOException Thrown in case of I/O error
     */
    public double getLastValue() throws IOException {
        return lastValue.get();
    }

    /**
     * Returns value this datasource accumulated so far.
     *
     * @return Accumulated datasource value.
     * @throws IOException Thrown in case of I/O error
     */
    public double getAccumValue() throws IOException {
        return accumValue.get();
    }

    /**
     * Returns the number of accumulated NaN seconds.
     *
     * @return Accumulated NaN seconds.
     * @throws IOException Thrown in case of I/O error
     */
    public long getNanSeconds() throws IOException {
        return nanSeconds.get();
    }

    final void process(long newTime, double newValue) throws IOException {
        Header header = parentDb.getHeader();
        long step = header.getStep();
        long oldTime = header.getLastUpdateTime();
        long startTime = Util.normalize(oldTime, step);
        long endTime = startTime + step;
        double oldValue = lastValue.get();
        double updateValue = calculateUpdateValue(oldTime, oldValue, newTime, newValue);
        if (newTime < endTime) {
            accumulate(oldTime, newTime, updateValue);
        }
        else {
            // should store something
            long boundaryTime = Util.normalize(newTime, step);
            accumulate(oldTime, boundaryTime, updateValue);
            double value = calculateTotal(startTime, boundaryTime);

            // how many updates?
            long numSteps = (boundaryTime - endTime) / step + 1L;

            // ACTION!
            parentDb.archive(this, value, numSteps);

            // cleanup
            nanSeconds.set(0);
            accumValue.set(0.0);

            accumulate(boundaryTime, newTime, updateValue);
        }
    }

    private double calculateUpdateValue(long oldTime, double oldValue,
                                        long newTime, double newValue) throws IOException {
        double updateValue = Double.NaN;
        if (newTime - oldTime <= heartbeat.get()) {
            DsType type = DsType.valueOf(dsType.get());

            if (type == DsType.GAUGE) {
                updateValue = newValue;
            }
            else if (type == DsType.COUNTER) {
                if (!Double.isNaN(newValue) && !Double.isNaN(oldValue)) {
                    double diff = newValue - oldValue;
                    if (diff < 0) {
                        diff += MAX_32_BIT;
                    }
                    if (diff < 0) {
                        diff += MAX_64_BIT - MAX_32_BIT;
                    }
                    if (diff >= 0) {
                        updateValue = diff / (newTime - oldTime);
                    }
                }
            }
            else if (type == DsType.ABSOLUTE) {
                if (!Double.isNaN(newValue)) {
                    updateValue = newValue / (newTime - oldTime);
                }
            }
            else if (type == DsType.DERIVE) {
                if (!Double.isNaN(newValue) && !Double.isNaN(oldValue)) {
                    updateValue = (newValue - oldValue) / (newTime - oldTime);
                }
            }

            if (!Double.isNaN(updateValue)) {
                double minVal = minValue.get();
                double maxVal = maxValue.get();
                if (!Double.isNaN(minVal) && updateValue < minVal) {
                    updateValue = Double.NaN;
                }
                if (!Double.isNaN(maxVal) && updateValue > maxVal) {
                    updateValue = Double.NaN;
                }
            }
        }
        lastValue.set(newValue);
        return updateValue;
    }

    private void accumulate(long oldTime, long newTime, double updateValue) throws IOException {
        if (Double.isNaN(updateValue)) {
            nanSeconds.set(nanSeconds.get() + (newTime - oldTime));
        }
        else {
            accumValue.set(accumValue.get() + updateValue * (newTime - oldTime));
        }
    }

    private double calculateTotal(long startTime, long boundaryTime) throws IOException {
        double totalValue = Double.NaN;
        long validSeconds = boundaryTime - startTime - nanSeconds.get();
        if (nanSeconds.get() <= heartbeat.get() && validSeconds > 0) {
            totalValue = accumValue.get() / validSeconds;
        }
        // IMPORTANT:
        // if datasource name ends with "!", we'll send zeros instead of NaNs
        // this might be handy from time to time
        if (Double.isNaN(totalValue) && dsName.get().endsWith(DsDef.FORCE_ZEROS_FOR_NANS_SUFFIX)) {
            totalValue = 0D;
        }
        return totalValue;
    }

    void appendXml(XmlWriter writer) throws IOException {
        writer.startTag("ds");
        writer.writeTag("name", dsName.get());
        writer.writeTag("type", dsType.get());
        writer.writeTag("minimal_heartbeat", heartbeat.get());
        writer.writeTag("min", minValue.get());
        writer.writeTag("max", maxValue.get());
        writer.writeComment("PDP Status");
        writer.writeTag("last_ds", lastValue.get(), "UNKN");
        writer.writeTag("value", accumValue.get());
        writer.writeTag("unknown_sec", nanSeconds.get());
        writer.closeTag();  // ds
    }

    /**
     * Copies object's internal state to another Datasource object.
     *
     * @param other New Datasource object to copy state to
     * @throws IOException Thrown in case of I/O error
     */
    public void copyStateTo(RrdUpdater other) throws IOException {
        if (!(other instanceof Datasource)) {
            throw new IllegalArgumentException(
                    "Cannot copy Datasource object to " + other.getClass().getName());
        }
        Datasource datasource = (Datasource) other;
        if (!datasource.dsName.get().equals(dsName.get())) {
            throw new IllegalArgumentException("Incompatible datasource names");
        }
        if (!datasource.dsType.get().equals(dsType.get())) {
            throw new IllegalArgumentException("Incompatible datasource types");
        }
        datasource.lastValue.set(lastValue.get());
        datasource.nanSeconds.set(nanSeconds.get());
        datasource.accumValue.set(accumValue.get());
    }

    /**
     * Returns index of this Datasource object in the RRD.
     *
     * @return Datasource index in the RRD.
     * @throws IOException Thrown in case of I/O error
     */
    public int getDsIndex() throws IOException {
        try {
            return parentDb.getDsIndex(dsName.get());
        }
        catch (IllegalArgumentException e) {
            return -1;
        }
    }

    /**
     * Sets datasource heartbeat to a new value.
     *
     * @param heartbeat New heartbeat value
     * @throws IOException              Thrown in case of I/O error
     * @throws IllegalArgumentException Thrown if invalid (non-positive) heartbeat value is specified.
     */
    public void setHeartbeat(long heartbeat) throws IOException {
        if (heartbeat < 1L) {
            throw new IllegalArgumentException("Invalid heartbeat specified: " + heartbeat);
        }
        this.heartbeat.set(heartbeat);
    }

    /**
     * Sets datasource name to a new value
     *
     * @param newDsName New datasource name
     * @throws IOException Thrown in case of I/O error
     */
    public void setDsName(String newDsName) throws IOException {
        if (newDsName != null && newDsName.length() > RrdString.STRING_LENGTH) {
            throw new IllegalArgumentException("Invalid datasource name specified: " + newDsName);
        }
        if (parentDb.containsDs(newDsName)) {
            throw new IllegalArgumentException("Datasource already defined in this RRD: " + newDsName);
        }

        this.dsName.set(newDsName);
    }

    public void setDsType(DsType newDsType) throws IOException {
        // set datasource type
        this.dsType.set(newDsType.name());
        // reset datasource status
        lastValue.set(Double.NaN);
        accumValue.set(0.0);
        // reset archive status
        int dsIndex = parentDb.getDsIndex(dsName.get());
        Archive[] archives = parentDb.getArchives();
        for (Archive archive : archives) {
            archive.getArcState(dsIndex).setAccumValue(Double.NaN);
        }
    }

    /**
     * Sets minimum allowed value for this datasource. If <code>filterArchivedValues</code>
     * argument is set to true, all archived values less then <code>minValue</code> will
     * be fixed to NaN.
     *
     * @param minValue             New minimal value. Specify <code>Double.NaN</code> if no minimal
     *                             value should be set
     * @param filterArchivedValues true, if archived datasource values should be fixed;
     *                             false, otherwise.
     * @throws IOException              Thrown in case of I/O error
     * @throws IllegalArgumentException Thrown if invalid minValue was supplied (not less then maxValue)
     */
    public void setMinValue(double minValue, boolean filterArchivedValues) throws IOException {
        double maxValue = this.maxValue.get();
        if (!Double.isNaN(minValue) && !Double.isNaN(maxValue) && minValue >= maxValue) {
            throw new IllegalArgumentException("Invalid min/max values: " + minValue + "/" + maxValue);
        }

        this.minValue.set(minValue);
        if (!Double.isNaN(minValue) && filterArchivedValues) {
            int dsIndex = getDsIndex();
            Archive[] archives = parentDb.getArchives();
            for (Archive archive : archives) {
                archive.getRobin(dsIndex).filterValues(minValue, Double.NaN);
            }
        }
    }

    /**
     * Sets maximum allowed value for this datasource. If <code>filterArchivedValues</code>
     * argument is set to true, all archived values greater then <code>maxValue</code> will
     * be fixed to NaN.
     *
     * @param maxValue             New maximal value. Specify <code>Double.NaN</code> if no max
     *                             value should be set.
     * @param filterArchivedValues true, if archived datasource values should be fixed;
     *                             false, otherwise.
     * @throws IOException              Thrown in case of I/O error
     * @throws IllegalArgumentException Thrown if invalid maxValue was supplied (not greater then minValue)
     */
    public void setMaxValue(double maxValue, boolean filterArchivedValues) throws IOException {
        double minValue = this.minValue.get();
        if (!Double.isNaN(minValue) && !Double.isNaN(maxValue) && minValue >= maxValue) {
            throw new IllegalArgumentException("Invalid min/max values: " + minValue + "/" + maxValue);
        }

        this.maxValue.set(maxValue);
        if (!Double.isNaN(maxValue) && filterArchivedValues) {
            int dsIndex = getDsIndex();
            Archive[] archives = parentDb.getArchives();
            for (Archive archive : archives) {
                archive.getRobin(dsIndex).filterValues(Double.NaN, maxValue);
            }
        }
    }

    /**
     * Sets min/max values allowed for this datasource. If <code>filterArchivedValues</code>
     * argument is set to true, all archived values less then <code>minValue</code> or
     * greater then <code>maxValue</code> will be fixed to NaN.
     *
     * @param minValue             New minimal value. Specify <code>Double.NaN</code> if no min
     *                             value should be set.
     * @param maxValue             New maximal value. Specify <code>Double.NaN</code> if no max
     *                             value should be set.
     * @param filterArchivedValues true, if archived datasource values should be fixed;
     *                             false, otherwise.
     * @throws IOException              Thrown in case of I/O error
     * @throws IllegalArgumentException Thrown if invalid min/max values were supplied
     */
    public void setMinMaxValue(double minValue, double maxValue, boolean filterArchivedValues) throws IOException {
        if (!Double.isNaN(minValue) && !Double.isNaN(maxValue) && minValue >= maxValue) {
            throw new IllegalArgumentException("Invalid min/max values: " + minValue + "/" + maxValue);
        }
        this.minValue.set(minValue);
        this.maxValue.set(maxValue);
        if (!(Double.isNaN(minValue) && Double.isNaN(maxValue)) && filterArchivedValues) {
            int dsIndex = getDsIndex();
            Archive[] archives = parentDb.getArchives();
            for (Archive archive : archives) {
                archive.getRobin(dsIndex).filterValues(minValue, maxValue);
            }
        }
    }

    /**
     * Returns the underlying storage (backend) object which actually performs all
     * I/O operations.
     *
     * @return I/O backend object
     */
    public RrdBackend getRrdBackend() {
        return parentDb.getRrdBackend();
    }

    /**
     * Required to implement RrdUpdater interface. You should never call this method directly.
     *
     * @return Allocator object
     */
    public RrdAllocator getRrdAllocator() {
        return parentDb.getRrdAllocator();
    }
}

>>>>>>> 76aa07461566a5976980e6696204781271955163

