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

/*-{
// From apache-harmony/classlib/modules/luni/src/main/native/luni/shared/floatbits.c,
// apache-harmony/classlib/modules/portlib/src/main/native/include/shared/hycomp.h
#define HYCONST64(x)            x##LL
#define DOUBLE_EXPONENT_MASK    HYCONST64(0x7FF0000000000000)
#define DOUBLE_MANTISSA_MASK    HYCONST64(0x000FFFFFFFFFFFFF)
#define DOUBLE_NAN_BITS         (DOUBLE_EXPONENT_MASK | HYCONST64(0x0008000000000000))

}-*/

/**
 * The wrapper for the primitive type {@code double}.
 *
 * @see java.lang.Number
 * @since 1.0
 */
public final class Double extends Number implements Comparable<Double> {

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
    public static final Class<Double> TYPE = (Class<Double>) new double[0]
            .getClass().getComponentType();

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
        if (object == null) {
          // When object is nil, Obj-C ignores messages sent to it.
          throw new NullPointerException();
        }
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
    public static native long doubleToLongBits(double value) /*-{
      // Modified from Harmony JNI implementation.
      long long longValue = *(long long *) &value;
      if ((longValue & DOUBLE_EXPONENT_MASK) == DOUBLE_EXPONENT_MASK) {
        if (longValue & DOUBLE_MANTISSA_MASK) {
          return DOUBLE_NAN_BITS;
        }
      }
      return longValue;
    }-*/;

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
    public static native long doubleToRawLongBits(double value) /*-{
        return *(long long *) &value;
    }-*/;

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
     * Compares this object with the specified object and indicates if they are
     * equal. In order to be equal, {@code object} must be an instance of
     * {@code Double} and the bit pattern of its double value is the same as
     * this object's.
     *
     * @param object
     *            the object to compare this double with.
     * @return {@code true} if the specified object is equal to this
     *         {@code Double}; {@code false} otherwise.
     */
    @Override
    public native boolean equals(Object object) /*-{
        NSComparisonResult result = [self compare:object];
        return result == NSOrderedSame;
    }-*/;

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
    public static native boolean isInfinite(double d) /*-{
        return isinf(d);
    }-*/;

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
    public native static boolean isNaN(double d) /*-{
        return isnan(d);
    }-*/;

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
    public static native double longBitsToDouble(long bits) /*-{
        return *(double *) &bits;
    }-*/;

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
    public native static double parseDouble(String string)
            throws NumberFormatException /*-{
        return [string doubleValue];
    }-*/;

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
    public native static String toString(double d) /*-{
        return [NSString stringWithFormat:@"%01.1f", d];
    }-*/;

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
        return new Double(parseDouble(string));
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
    public static native String toHexString(double d) /*-{
        return [NSString stringWithFormat:@"%A", d];
    }-*/;
}
=======
package org.rrd4j.core;

import org.rrd4j.ConsolFun;

import java.io.IOException;

/**
 * Class to represent single RRD archive in a RRD with its internal state.
 * Normally, you don't need methods to manipulate archive objects directly
 * because Rrd4j framework does it automatically for you.<p>
 * <p/>
 * Each archive object consists of three parts: archive definition, archive state objects
 * (one state object for each datasource) and round robin archives (one round robin for
 * each datasource). API (read-only) is provided to access each of these parts.<p>
 *
 * @author Sasa Markovic
 */
public class Archive implements RrdUpdater {
    private final RrdDb parentDb;

    // definition
    protected final RrdString consolFun;
    protected final RrdDouble xff;
    protected final RrdInt steps;
    protected final RrdInt rows;

    // state
    private final Robin[] robins;
    private final ArcState[] states;

    Archive(RrdDb parentDb, ArcDef arcDef) throws IOException {
        this.parentDb = parentDb;
        consolFun = new RrdString(this, true);     // constant, may be cached
        xff = new RrdDouble(this);
        steps = new RrdInt(this, true);            // constant, may be cached
        rows = new RrdInt(this, true);             // constant, may be cached
        boolean shouldInitialize = arcDef != null;
        if (shouldInitialize) {
            consolFun.set(arcDef.getConsolFun().name());
            xff.set(arcDef.getXff());
            steps.set(arcDef.getSteps());
            rows.set(arcDef.getRows());
        }
        int n = parentDb.getHeader().getDsCount();
        int numRows = rows.get();
        states = new ArcState[n];
        int version = parentDb.getHeader().getVersion();
        if (version == 1) {
            robins = new RobinArray[n];
            for (int i = 0; i < n; i++) {
                states[i] = new ArcState(this, shouldInitialize);
                robins[i] = new RobinArray(this, numRows, shouldInitialize);
            }
        } else {
            RrdInt[] pointers = new RrdInt[n];
            robins = new RobinMatrix[n];
            for (int i = 0; i < n; i++) {
                pointers[i] = new RrdInt(this);
                states[i] = new ArcState(this, shouldInitialize);
            }
            RrdDoubleMatrix values = new RrdDoubleMatrix(this, numRows, n, shouldInitialize);
            for (int i = 0; i < n; i++) {
                robins[i] = new RobinMatrix(this, values, pointers[i], i);
            }
        }
    }

    // read from XML
    Archive(RrdDb parentDb, DataImporter reader, int arcIndex) throws IOException {
        this(parentDb, new ArcDef(
                reader.getConsolFun(arcIndex), reader.getXff(arcIndex),
                reader.getSteps(arcIndex), reader.getRows(arcIndex)));
        int n = parentDb.getHeader().getDsCount();
        for (int i = 0; i < n; i++) {
            // restore state
            states[i].setAccumValue(reader.getStateAccumValue(arcIndex, i));
            states[i].setNanSteps(reader.getStateNanSteps(arcIndex, i));
            // restore robins
            double[] values = reader.getValues(arcIndex, i);
            robins[i].update(values);
        }
    }

    /**
     * Returns archive time step in seconds. Archive step is equal to RRD step
     * multiplied with the number of archive steps.
     *
     * @return Archive time step in seconds
     * @throws IOException Thrown in case of I/O error.
     */
    public long getArcStep() throws IOException {
        return parentDb.getHeader().getStep() * steps.get();
    }

    String dump() throws IOException {
        StringBuilder sb = new StringBuilder("== ARCHIVE ==\n");
        sb.append("RRA:").append(consolFun.get()).append(":").append(xff.get()).append(":").append(steps.get()).append(":").append(rows.get()).append("\n");
        sb.append("interval [").append(getStartTime()).append(", ").append(getEndTime()).append("]" + "\n");
        for (int i = 0; i < robins.length; i++) {
            sb.append(states[i].dump());
            sb.append(robins[i].dump());
        }
        return sb.toString();
    }

    RrdDb getParentDb() {
        return parentDb;
    }

    void archive(int dsIndex, double value, long numUpdates) throws IOException {
        Robin robin = robins[dsIndex];
        ArcState state = states[dsIndex];
        long step = parentDb.getHeader().getStep();
        long lastUpdateTime = parentDb.getHeader().getLastUpdateTime();
        long updateTime = Util.normalize(lastUpdateTime, step) + step;
        long arcStep = getArcStep();
        // finish current step
        while (numUpdates > 0) {
            accumulate(state, value);
            numUpdates--;
            if (updateTime % arcStep == 0) {
                finalizeStep(state, robin);
                break;
            } else {
                updateTime += step;
            }
        }
        // update robin in bulk
        int bulkUpdateCount = (int) Math.min(numUpdates / steps.get(), (long) rows.get());
        robin.bulkStore(value, bulkUpdateCount);
        // update remaining steps
        long remainingUpdates = numUpdates % steps.get();
        for (long i = 0; i < remainingUpdates; i++) {
            accumulate(state, value);
        }
    }

    private void accumulate(ArcState state, double value) throws IOException {
        if (Double.isNaN(value)) {
            state.setNanSteps(state.getNanSteps() + 1);
        } else {
            switch (ConsolFun.valueOf(consolFun.get())) {
                case MIN:
                    state.setAccumValue(Util.min(state.getAccumValue(), value));
                    break;
                case MAX:
                    state.setAccumValue(Util.max(state.getAccumValue(), value));
                    break;
                case FIRST:
                    if (Double.isNaN(state.getAccumValue())) {
                        state.setAccumValue(value);
                    }
                    break;
                case LAST:
                    state.setAccumValue(value);
                    break;
                case AVERAGE:
                case TOTAL:
                    state.setAccumValue(Util.sum(state.getAccumValue(), value));
                    break;
            }
        }
    }

    private void finalizeStep(ArcState state, Robin robin) throws IOException {
        // should store
        long arcSteps = steps.get();
        double arcXff = xff.get();
        long nanSteps = state.getNanSteps();
        //double nanPct = (double) nanSteps / (double) arcSteps;
        double accumValue = state.getAccumValue();
        if (nanSteps <= arcXff * arcSteps && !Double.isNaN(accumValue)) {
            if (getConsolFun() == ConsolFun.AVERAGE) {
                accumValue /= (arcSteps - nanSteps);
            }
            robin.store(accumValue);
        } else {
            robin.store(Double.NaN);
        }
        state.setAccumValue(Double.NaN);
        state.setNanSteps(0);
    }

    /**
     * Returns archive consolidation function ("AVERAGE", "MIN", "MAX", "FIRST", "LAST" or "TOTAL").
     *
     * @return Archive consolidation function.
     * @throws IOException Thrown in case of I/O error.
     */
    public ConsolFun getConsolFun() throws IOException {
        return ConsolFun.valueOf(consolFun.get());
    }

    /**
     * Returns archive X-files factor.
     *
     * @return Archive X-files factor (between 0 and 1).
     * @throws IOException Thrown in case of I/O error.
     */
    public double getXff() throws IOException {
        return xff.get();
    }

    /**
     * Returns the number of archive steps.
     *
     * @return Number of archive steps.
     * @throws IOException Thrown in case of I/O error.
     */
    public int getSteps() throws IOException {
        return steps.get();
    }

    /**
     * Returns the number of archive rows.
     *
     * @return Number of archive rows.
     * @throws IOException Thrown in case of I/O error.
     */
    public int getRows() throws IOException {
        return rows.get();
    }

    /**
     * Returns current starting timestamp. This value is not constant.
     *
     * @return Timestamp corresponding to the first archive row
     * @throws IOException Thrown in case of I/O error.
     */
    public long getStartTime() throws IOException {
        long endTime = getEndTime();
        long arcStep = getArcStep();
        long numRows = rows.get();
        return endTime - (numRows - 1) * arcStep;
    }

    /**
     * Returns current ending timestamp. This value is not constant.
     *
     * @return Timestamp corresponding to the last archive row
     * @throws IOException Thrown in case of I/O error.
     */
    public long getEndTime() throws IOException {
        long arcStep = getArcStep();
        long lastUpdateTime = parentDb.getHeader().getLastUpdateTime();
        return Util.normalize(lastUpdateTime, arcStep);
    }

    /**
     * Returns the underlying archive state object. Each datasource has its
     * corresponding ArcState object (archive states are managed independently
     * for each RRD datasource).
     *
     * @param dsIndex Datasource index
     * @return Underlying archive state object
     */
    public ArcState getArcState(int dsIndex) {
        return states[dsIndex];
    }

    /**
     * Returns the underlying round robin archive. Robins are used to store actual
     * archive values on a per-datasource basis.
     *
     * @param dsIndex Index of the datasource in the RRD.
     * @return Underlying round robin archive for the given datasource.
     */
    public Robin getRobin(int dsIndex) {
        return robins[dsIndex];
    }

    FetchData fetchData(FetchRequest request) throws IOException {
        long arcStep = getArcStep();
        long fetchStart = Util.normalize(request.getFetchStart(), arcStep);
        long fetchEnd = Util.normalize(request.getFetchEnd(), arcStep);
        if (fetchEnd < request.getFetchEnd()) {
            fetchEnd += arcStep;
        }
        long startTime = getStartTime();
        long endTime = getEndTime();
        String[] dsToFetch = request.getFilter();
        if (dsToFetch == null) {
            dsToFetch = parentDb.getDsNames();
        }
        int dsCount = dsToFetch.length;
        int ptsCount = (int) ((fetchEnd - fetchStart) / arcStep + 1);
        long[] timestamps = new long[ptsCount];
        double[][] values = new double[dsCount][ptsCount];
        long matchStartTime = Math.max(fetchStart, startTime);
        long matchEndTime = Math.min(fetchEnd, endTime);
        double[][] robinValues = null;
        if (matchStartTime <= matchEndTime) {
            // preload robin values
            int matchCount = (int) ((matchEndTime - matchStartTime) / arcStep + 1);
            int matchStartIndex = (int) ((matchStartTime - startTime) / arcStep);
            robinValues = new double[dsCount][];
            for (int i = 0; i < dsCount; i++) {
                int dsIndex = parentDb.getDsIndex(dsToFetch[i]);
                robinValues[i] = robins[dsIndex].getValues(matchStartIndex, matchCount);
            }
        }
        for (int ptIndex = 0; ptIndex < ptsCount; ptIndex++) {
            long time = fetchStart + ptIndex * arcStep;
            timestamps[ptIndex] = time;
            for (int i = 0; i < dsCount; i++) {
                double value = Double.NaN;
                if (time >= matchStartTime && time <= matchEndTime) {
                    // inbound time
                    int robinValueIndex = (int) ((time - matchStartTime) / arcStep);
                    assert robinValues != null;
                    value = robinValues[i][robinValueIndex];
                }
                values[i][ptIndex] = value;
            }
        }
        FetchData fetchData = new FetchData(this, request);
        fetchData.setTimestamps(timestamps);
        fetchData.setValues(values);
        return fetchData;
    }

    void appendXml(XmlWriter writer) throws IOException {
        writer.startTag("rra");
        writer.writeTag("cf", consolFun.get());
        writer.writeComment(getArcStep() + " seconds");
        writer.writeTag("pdp_per_row", steps.get());
        writer.writeTag("xff", xff.get());
        writer.startTag("cdp_prep");
        for (ArcState state : states) {
            state.appendXml(writer);
        }
        writer.closeTag(); // cdp_prep
        writer.startTag("database");
        long startTime = getStartTime();
        for (int i = 0; i < rows.get(); i++) {
            long time = startTime + i * getArcStep();
            writer.writeComment(Util.getDate(time) + " / " + time);
            writer.startTag("row");
            for (Robin robin : robins) {
                writer.writeTag("v", robin.getValue(i));
            }
            writer.closeTag(); // row
        }
        writer.closeTag(); // database
        writer.closeTag(); // rra
    }

    /**
     * Copies object's internal state to another Archive object.
     *
     * @param other New Archive object to copy state to
     * @throws IOException Thrown in case of I/O error
     */
    public void copyStateTo(RrdUpdater other) throws IOException {
        if (!(other instanceof Archive)) {
            throw new IllegalArgumentException(
                    "Cannot copy Archive object to " + other.getClass().getName());
        }
        Archive arc = (Archive) other;
        if (!arc.consolFun.get().equals(consolFun.get())) {
            throw new IllegalArgumentException("Incompatible consolidation functions");
        }
        if (arc.steps.get() != steps.get()) {
            throw new IllegalArgumentException("Incompatible number of steps");
        }
        int count = parentDb.getHeader().getDsCount();
        for (int i = 0; i < count; i++) {
            int j = Util.getMatchingDatasourceIndex(parentDb, i, arc.parentDb);
            if (j >= 0) {
                states[i].copyStateTo(arc.states[j]);
                robins[i].copyStateTo(arc.robins[j]);
            }
        }
    }

    /**
     * Sets X-files factor to a new value.
     *
     * @param xff New X-files factor value. Must be >= 0 and < 1.
     * @throws IOException Thrown in case of I/O error
     */
    public void setXff(double xff) throws IOException {
        if (xff < 0D || xff >= 1D) {
            throw new IllegalArgumentException("Invalid xff supplied (" + xff + "), must be >= 0 and < 1");
        }
        this.xff.set(xff);
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

