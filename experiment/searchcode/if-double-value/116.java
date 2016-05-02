<<<<<<< HEAD
/*
 * Copyright 2004-2013 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.value;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import org.h2.api.ErrorCode;
import org.h2.engine.Constants;
import org.h2.engine.SysProperties;
import org.h2.message.DbException;
import org.h2.store.DataHandler;
import org.h2.tools.SimpleResultSet;
import org.h2.util.DateTimeUtils;
import org.h2.util.MathUtils;
import org.h2.util.StringUtils;
import org.h2.util.Utils;

/**
 * This is the base class for all value classes.
 * It provides conversion and comparison methods.
 *
 * @author Thomas Mueller
 * @author Noel Grandin
 * @author Nicolas Fortin, Atelier SIG, IRSTV FR CNRS 24888
 */
public abstract class Value {

    /**
     * The data type is unknown at this time.
     */
    public static final int UNKNOWN = -1;

    /**
     * The value type for NULL.
     */
    public static final int NULL = 0;

    /**
     * The value type for BOOLEAN values.
     */
    public static final int BOOLEAN = 1;

    /**
     * The value type for BYTE values.
     */
    public static final int BYTE = 2;

    /**
     * The value type for SHORT values.
     */
    public static final int SHORT = 3;

    /**
     * The value type for INT values.
     */
    public static final int INT = 4;

    /**
     * The value type for LONG values.
     */
    public static final int LONG = 5;

    /**
     * The value type for DECIMAL values.
     */
    public static final int DECIMAL = 6;

    /**
     * The value type for DOUBLE values.
     */
    public static final int DOUBLE = 7;

    /**
     * The value type for FLOAT values.
     */
    public static final int FLOAT = 8;

    /**
     * The value type for TIME values.
     */
    public static final int TIME = 9;

    /**
     * The value type for DATE values.
     */
    public static final int DATE = 10;

    /**
     * The value type for TIMESTAMP values.
     */
    public static final int TIMESTAMP = 11;

    /**
     * The value type for BYTES values.
     */
    public static final int BYTES = 12;

    /**
     * The value type for STRING values.
     */
    public static final int STRING = 13;

    /**
     * The value type for case insensitive STRING values.
     */
    public static final int STRING_IGNORECASE = 14;

    /**
     * The value type for BLOB values.
     */
    public static final int BLOB = 15;

    /**
     * The value type for CLOB values.
     */
    public static final int CLOB = 16;

    /**
     * The value type for ARRAY values.
     */
    public static final int ARRAY = 17;

    /**
     * The value type for RESULT_SET values.
     */
    public static final int RESULT_SET = 18;
    /**
     * The value type for JAVA_OBJECT values.
     */
    public static final int JAVA_OBJECT = 19;

    /**
     * The value type for UUID values.
     */
    public static final int UUID = 20;

    /**
     * The value type for string values with a fixed size.
     */
    public static final int STRING_FIXED = 21;

    /**
     * The value type for string values with a fixed size.
     */
    public static final int GEOMETRY = 22;

    /**
     * The number of value types.
     */
    public static final int TYPE_COUNT = GEOMETRY + 1;

    private static SoftReference<Value[]> softCache =
            new SoftReference<Value[]>(null);
    private static final BigDecimal MAX_LONG_DECIMAL =
            BigDecimal.valueOf(Long.MAX_VALUE);
    private static final BigDecimal MIN_LONG_DECIMAL =
            BigDecimal.valueOf(Long.MIN_VALUE);

    /**
     * Get the SQL expression for this value.
     *
     * @return the SQL expression
     */
    public abstract String getSQL();

    /**
     * Get the value type.
     *
     * @return the type
     */
    public abstract int getType();

    /**
     * Get the precision.
     *
     * @return the precision
     */
    public abstract long getPrecision();

    /**
     * Get the display size in characters.
     *
     * @return the display size
     */
    public abstract int getDisplaySize();

    /**
     * Get the memory used by this object.
     *
     * @return the memory used in bytes
     */
    public int getMemory() {
        return DataType.getDataType(getType()).memory;
    }

    /**
     * Get the value as a string.
     *
     * @return the string
     */
    public abstract String getString();

    /**
     * Get the value as an object.
     *
     * @return the object
     */
    public abstract Object getObject();

    /**
     * Set the value as a parameter in a prepared statement.
     *
     * @param prep the prepared statement
     * @param parameterIndex the parameter index
     */
    public abstract void set(PreparedStatement prep, int parameterIndex)
            throws SQLException;

    /**
     * Compare the value with another value of the same type.
     *
     * @param v the other value
     * @param mode the compare mode
     * @return 0 if both values are equal, -1 if the other value is smaller, and
     *         1 otherwise
     */
    protected abstract int compareSecure(Value v, CompareMode mode);

    @Override
    public abstract int hashCode();

    /**
     * Check if the two values have the same hash code. No data conversion is
     * made; this method returns false if the other object is not of the same
     * class. For some values, compareTo may return 0 even if equals return
     * false. Example: ValueDecimal 0.0 and 0.00.
     *
     * @param other the other value
     * @return true if they are equal
     */
    @Override
    public abstract boolean equals(Object other);

    /**
     * Get the order of this value type.
     *
     * @param type the value type
     * @return the order number
     */
    static int getOrder(int type) {
        switch(type) {
        case UNKNOWN:
            return 1;
        case NULL:
            return 2;
        case STRING:
            return 10;
        case CLOB:
            return 11;
        case STRING_FIXED:
            return 12;
        case STRING_IGNORECASE:
            return 13;
        case BOOLEAN:
            return 20;
        case BYTE:
            return 21;
        case SHORT:
            return 22;
        case INT:
            return 23;
        case LONG:
            return 24;
        case DECIMAL:
            return 25;
        case FLOAT:
            return 26;
        case DOUBLE:
            return 27;
        case TIME:
            return 30;
        case DATE:
            return 31;
        case TIMESTAMP:
            return 32;
        case BYTES:
            return 40;
        case BLOB:
            return 41;
        case UUID:
            return 42;
        case JAVA_OBJECT:
            return 43;
        case GEOMETRY:
            return 44;
        case ARRAY:
            return 50;
        case RESULT_SET:
            return 51;
        default:
            throw DbException.throwInternalError("type:"+type);
        }
    }

    /**
     * Get the higher value order type of two value types. If values need to be
     * converted to match the other operands value type, the value with the
     * lower order is converted to the value with the higher order.
     *
     * @param t1 the first value type
     * @param t2 the second value type
     * @return the higher value type of the two
     */
    public static int getHigherOrder(int t1, int t2) {
        if (t1 == Value.UNKNOWN || t2 == Value.UNKNOWN) {
            if (t1 == t2) {
                throw DbException.get(
                        ErrorCode.UNKNOWN_DATA_TYPE_1, "?, ?");
            } else if (t1 == Value.NULL) {
                throw DbException.get(
                        ErrorCode.UNKNOWN_DATA_TYPE_1, "NULL, ?");
            } else if (t2 == Value.NULL) {
                throw DbException.get(
                        ErrorCode.UNKNOWN_DATA_TYPE_1, "?, NULL");
            }
        }
        if (t1 == t2) {
            return t1;
        }
        int o1 = getOrder(t1);
        int o2 = getOrder(t2);
        return o1 > o2 ? t1 : t2;
    }

    /**
     * Check if a value is in the cache that is equal to this value. If yes,
     * this value should be used to save memory. If the value is not in the
     * cache yet, it is added.
     *
     * @param v the value to look for
     * @return the value in the cache or the value passed
     */
    static Value cache(Value v) {
        if (SysProperties.OBJECT_CACHE) {
            int hash = v.hashCode();
            if (softCache == null) {
                softCache = new SoftReference<Value[]>(null);
            }
            Value[] cache = softCache.get();
            if (cache == null) {
                cache = new Value[SysProperties.OBJECT_CACHE_SIZE];
                softCache = new SoftReference<Value[]>(cache);
            }
            int index = hash & (SysProperties.OBJECT_CACHE_SIZE - 1);
            Value cached = cache[index];
            if (cached != null) {
                if (cached.getType() == v.getType() && v.equals(cached)) {
                    // cacheHit++;
                    return cached;
                }
            }
            // cacheMiss++;
            // cache[cacheCleaner] = null;
            // cacheCleaner = (cacheCleaner + 1) &
            //     (Constants.OBJECT_CACHE_SIZE - 1);
            cache[index] = v;
        }
        return v;
    }

    /**
     * Clear the value cache. Used for testing.
     */
    public static void clearCache() {
        softCache = null;
    }

    public Boolean getBoolean() {
        return ((ValueBoolean) convertTo(Value.BOOLEAN)).getBoolean();
    }

    public Date getDate() {
        return ((ValueDate) convertTo(Value.DATE)).getDate();
    }

    public Time getTime() {
        return ((ValueTime) convertTo(Value.TIME)).getTime();
    }

    public Timestamp getTimestamp() {
        return ((ValueTimestamp) convertTo(Value.TIMESTAMP)).getTimestamp();
    }

    public byte[] getBytes() {
        return ((ValueBytes) convertTo(Value.BYTES)).getBytes();
    }

    public byte[] getBytesNoCopy() {
        return ((ValueBytes) convertTo(Value.BYTES)).getBytesNoCopy();
    }

    public byte getByte() {
        return ((ValueByte) convertTo(Value.BYTE)).getByte();
    }

    public short getShort() {
        return ((ValueShort) convertTo(Value.SHORT)).getShort();
    }

    public BigDecimal getBigDecimal() {
        return ((ValueDecimal) convertTo(Value.DECIMAL)).getBigDecimal();
    }

    public double getDouble() {
        return ((ValueDouble) convertTo(Value.DOUBLE)).getDouble();
    }

    public float getFloat() {
        return ((ValueFloat) convertTo(Value.FLOAT)).getFloat();
    }

    public int getInt() {
        return ((ValueInt) convertTo(Value.INT)).getInt();
    }

    public long getLong() {
        return ((ValueLong) convertTo(Value.LONG)).getLong();
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(getBytesNoCopy());
    }

    public Reader getReader() {
        return new StringReader(getString());
    }

    /**
     * Add a value and return the result.
     *
     * @param v the value to add
     * @return the result
     */
    public Value add(Value v) {
        throw throwUnsupportedExceptionForType("+");
    }

    public int getSignum() {
        throw throwUnsupportedExceptionForType("SIGNUM");
    }

    /**
     * Return -value if this value support arithmetic operations.
     *
     * @return the negative
     */
    public Value negate() {
        throw throwUnsupportedExceptionForType("NEG");
    }

    /**
     * Subtract a value and return the result.
     *
     * @param v the value to subtract
     * @return the result
     */
    public Value subtract(Value v) {
        throw throwUnsupportedExceptionForType("-");
    }

    /**
     * Divide by a value and return the result.
     *
     * @param v the value to divide by
     * @return the result
     */
    public Value divide(Value v) {
        throw throwUnsupportedExceptionForType("/");
    }

    /**
     * Multiply with a value and return the result.
     *
     * @param v the value to multiply with
     * @return the result
     */
    public Value multiply(Value v) {
        throw throwUnsupportedExceptionForType("*");
    }

    /**
     * Take the modulus with a value and return the result.
     *
     * @param v the value to take the modulus with
     * @return the result
     */
    public Value modulus(Value v) {
        throw throwUnsupportedExceptionForType("%");
    }

    /**
     * Compare a value to the specified type.
     *
     * @param targetType the type of the returned value
     * @return the converted value
     */
    public Value convertTo(int targetType) {
        // converting NULL is done in ValueNull
        // converting BLOB to CLOB and vice versa is done in ValueLob
        if (getType() == targetType) {
            return this;
        }
        try {
            // decimal conversion
            switch (targetType) {
            case BOOLEAN: {
                switch (getType()) {
                case BYTE:
                case SHORT:
                case INT:
                case LONG:
                case DECIMAL:
                case DOUBLE:
                case FLOAT:
                    return ValueBoolean.get(getSignum() != 0);
                case TIME:
                case DATE:
                case TIMESTAMP:
                case BYTES:
                case JAVA_OBJECT:
                case UUID:
                    throw DbException.get(
                            ErrorCode.DATA_CONVERSION_ERROR_1, getString());
                }
                break;
            }
            case BYTE: {
                switch (getType()) {
                case BOOLEAN:
                    return ValueByte.get(getBoolean().booleanValue() ? (byte) 1 : (byte) 0);
                case SHORT:
                    return ValueByte.get(convertToByte(getShort()));
                case INT:
                    return ValueByte.get(convertToByte(getInt()));
                case LONG:
                    return ValueByte.get(convertToByte(getLong()));
                case DECIMAL:
                    return ValueByte.get(convertToByte(convertToLong(getBigDecimal())));
                case DOUBLE:
                    return ValueByte.get(convertToByte(convertToLong(getDouble())));
                case FLOAT:
                    return ValueByte.get(convertToByte(convertToLong(getFloat())));
                case BYTES:
                    return ValueByte.get((byte) Integer.parseInt(getString(), 16));
                }
                break;
            }
            case SHORT: {
                switch (getType()) {
                case BOOLEAN:
                    return ValueShort.get(getBoolean().booleanValue() ? (short) 1 : (short) 0);
                case BYTE:
                    return ValueShort.get(getByte());
                case INT:
                    return ValueShort.get(convertToShort(getInt()));
                case LONG:
                    return ValueShort.get(convertToShort(getLong()));
                case DECIMAL:
                    return ValueShort.get(convertToShort(convertToLong(getBigDecimal())));
                case DOUBLE:
                    return ValueShort.get(convertToShort(convertToLong(getDouble())));
                case FLOAT:
                    return ValueShort.get(convertToShort(convertToLong(getFloat())));
                case BYTES:
                    return ValueShort.get((short) Integer.parseInt(getString(), 16));
                }
                break;
            }
            case INT: {
                switch (getType()) {
                case BOOLEAN:
                    return ValueInt.get(getBoolean().booleanValue() ? 1 : 0);
                case BYTE:
                    return ValueInt.get(getByte());
                case SHORT:
                    return ValueInt.get(getShort());
                case LONG:
                    return ValueInt.get(convertToInt(getLong()));
                case DECIMAL:
                    return ValueInt.get(convertToInt(convertToLong(getBigDecimal())));
                case DOUBLE:
                    return ValueInt.get(convertToInt(convertToLong(getDouble())));
                case FLOAT:
                    return ValueInt.get(convertToInt(convertToLong(getFloat())));
                case BYTES:
                    return ValueInt.get((int) Long.parseLong(getString(), 16));
                }
                break;
            }
            case LONG: {
                switch (getType()) {
                case BOOLEAN:
                    return ValueLong.get(getBoolean().booleanValue() ? 1 : 0);
                case BYTE:
                    return ValueLong.get(getByte());
                case SHORT:
                    return ValueLong.get(getShort());
                case INT:
                    return ValueLong.get(getInt());
                case DECIMAL:
                    return ValueLong.get(convertToLong(getBigDecimal()));
                case DOUBLE:
                    return ValueLong.get(convertToLong(getDouble()));
                case FLOAT:
                    return ValueLong.get(convertToLong(getFloat()));
                case BYTES: {
                    // parseLong doesn't work for ffffffffffffffff
                    byte[] d = getBytes();
                    if (d.length == 8) {
                        return ValueLong.get(Utils.readLong(d, 0));
                    }
                    return ValueLong.get(Long.parseLong(getString(), 16));
                }
                }
                break;
            }
            case DECIMAL: {
                switch (getType()) {
                case BOOLEAN:
                    return ValueDecimal.get(BigDecimal.valueOf(
                            getBoolean().booleanValue() ? 1 : 0));
                case BYTE:
                    return ValueDecimal.get(BigDecimal.valueOf(getByte()));
                case SHORT:
                    return ValueDecimal.get(BigDecimal.valueOf(getShort()));
                case INT:
                    return ValueDecimal.get(BigDecimal.valueOf(getInt()));
                case LONG:
                    return ValueDecimal.get(BigDecimal.valueOf(getLong()));
                case DOUBLE: {
                    double d = getDouble();
                    if (Double.isInfinite(d) || Double.isNaN(d)) {
                        throw DbException.get(
                                ErrorCode.DATA_CONVERSION_ERROR_1, "" + d);
                    }
                    return ValueDecimal.get(BigDecimal.valueOf(d));
                }
                case FLOAT: {
                    float f = getFloat();
                    if (Float.isInfinite(f) || Float.isNaN(f)) {
                        throw DbException.get(
                                ErrorCode.DATA_CONVERSION_ERROR_1, "" + f);
                    }
                    // better rounding behavior than BigDecimal.valueOf(f)
                    return ValueDecimal.get(new BigDecimal(Float.toString(f)));
                }
                }
                break;
            }
            case DOUBLE: {
                switch (getType()) {
                case BOOLEAN:
                    return ValueDouble.get(getBoolean().booleanValue() ? 1 : 0);
                case BYTE:
                    return ValueDouble.get(getByte());
                case SHORT:
                    return ValueDouble.get(getShort());
                case INT:
                    return ValueDouble.get(getInt());
                case LONG:
                    return ValueDouble.get(getLong());
                case DECIMAL:
                    return ValueDouble.get(getBigDecimal().doubleValue());
                case FLOAT:
                    return ValueDouble.get(getFloat());
                }
                break;
            }
            case FLOAT: {
                switch (getType()) {
                case BOOLEAN:
                    return ValueFloat.get(getBoolean().booleanValue() ? 1 : 0);
                case BYTE:
                    return ValueFloat.get(getByte());
                case SHORT:
                    return ValueFloat.get(getShort());
                case INT:
                    return ValueFloat.get(getInt());
                case LONG:
                    return ValueFloat.get(getLong());
                case DECIMAL:
                    return ValueFloat.get(getBigDecimal().floatValue());
                case DOUBLE:
                    return ValueFloat.get((float) getDouble());
                }
                break;
            }
            case DATE: {
                switch (getType()) {
                case TIME:
                    // because the time has set the date to 1970-01-01,
                    // this will be the result
                    return ValueDate.fromDateValue(
                            DateTimeUtils.dateValue(1970, 1, 1));
                case TIMESTAMP:
                    return ValueDate.fromDateValue(
                            ((ValueTimestamp) this).getDateValue());
                }
                break;
            }
            case TIME: {
                switch (getType()) {
                case DATE:
                    // need to normalize the year, month and day because a date
                    // has the time set to 0, the result will be 0
                    return ValueTime.fromNanos(0);
                case TIMESTAMP:
                    return ValueTime.fromNanos(
                            ((ValueTimestamp) this).getNanos());
                }
                break;
            }
            case TIMESTAMP: {
                switch (getType()) {
                case TIME:
                    return DateTimeUtils.normalizeTimestamp(
                            0, ((ValueTime) this).getNanos());
                case DATE:
                    return ValueTimestamp.fromDateValueAndNanos(
                            ((ValueDate) this).getDateValue(), 0);
                }
                break;
            }
            case BYTES: {
                switch(getType()) {
                case JAVA_OBJECT:
                case BLOB:
                    return ValueBytes.getNoCopy(getBytesNoCopy());
                case UUID:
                case GEOMETRY:
                    return ValueBytes.getNoCopy(getBytes());
                case BYTE:
                    return ValueBytes.getNoCopy(new byte[]{getByte()});
                case SHORT: {
                    int x = getShort();
                    return ValueBytes.getNoCopy(new byte[]{
                            (byte) (x >> 8),
                            (byte) x
                    });
                }
                case INT: {
                    int x = getInt();
                    return ValueBytes.getNoCopy(new byte[]{
                            (byte) (x >> 24),
                            (byte) (x >> 16),
                            (byte) (x >> 8),
                            (byte) x
                    });
                }
                case LONG: {
                    long x = getLong();
                    return ValueBytes.getNoCopy(new byte[]{
                            (byte) (x >> 56),
                            (byte) (x >> 48),
                            (byte) (x >> 40),
                            (byte) (x >> 32),
                            (byte) (x >> 24),
                            (byte) (x >> 16),
                            (byte) (x >> 8),
                            (byte) x
                    });
                }
                }
                break;
            }
            case JAVA_OBJECT: {
                switch(getType()) {
                case BYTES:
                case BLOB:
                    return ValueJavaObject.getNoCopy(
                            null, getBytesNoCopy(), getDataHandler());
                }
                break;
            }
            case BLOB: {
                switch(getType()) {
                case BYTES:
                    return ValueLobDb.createSmallLob(
                            Value.BLOB, getBytesNoCopy());
                }
                break;
            }
            case UUID: {
                switch(getType()) {
                case BYTES:
                    return ValueUuid.get(getBytesNoCopy());
                }
            }
            case GEOMETRY:
                switch(getType()) {
                case BYTES:
                    return ValueGeometry.get(getBytesNoCopy());
                case JAVA_OBJECT:
                    Object object = Utils.deserialize(getBytesNoCopy(), getDataHandler());
                    if (DataType.isGeometry(object)) {
                        return ValueGeometry.getFromGeometry(object);
                    }
                }
            }
            // conversion by parsing the string value
            String s = getString();
            switch (targetType) {
            case NULL:
                return ValueNull.INSTANCE;
            case BOOLEAN: {
                if (s.equalsIgnoreCase("true") ||
                        s.equalsIgnoreCase("t") ||
                        s.equalsIgnoreCase("yes") ||
                        s.equalsIgnoreCase("y")) {
                    return ValueBoolean.get(true);
                } else if (s.equalsIgnoreCase("false") ||
                        s.equalsIgnoreCase("f") ||
                        s.equalsIgnoreCase("no") ||
                        s.equalsIgnoreCase("n")) {
                    return ValueBoolean.get(false);
                } else {
                    // convert to a number, and if it is not 0 then it is true
                    return ValueBoolean.get(new BigDecimal(s).signum() != 0);
                }
            }
            case BYTE:
                return ValueByte.get(Byte.parseByte(s.trim()));
            case SHORT:
                return ValueShort.get(Short.parseShort(s.trim()));
            case INT:
                return ValueInt.get(Integer.parseInt(s.trim()));
            case LONG:
                return ValueLong.get(Long.parseLong(s.trim()));
            case DECIMAL:
                return ValueDecimal.get(new BigDecimal(s.trim()));
            case TIME:
                return ValueTime.parse(s.trim());
            case DATE:
                return ValueDate.parse(s.trim());
            case TIMESTAMP:
                return ValueTimestamp.parse(s.trim());
            case BYTES:
                return ValueBytes.getNoCopy(
                        StringUtils.convertHexToBytes(s.trim()));
            case JAVA_OBJECT:
                return ValueJavaObject.getNoCopy(null,
                        StringUtils.convertHexToBytes(s.trim()), getDataHandler());
            case STRING:
                return ValueString.get(s);
            case STRING_IGNORECASE:
                return ValueStringIgnoreCase.get(s);
            case STRING_FIXED:
                return ValueStringFixed.get(s);
            case DOUBLE:
                return ValueDouble.get(Double.parseDouble(s.trim()));
            case FLOAT:
                return ValueFloat.get(Float.parseFloat(s.trim()));
            case CLOB:
                return ValueLobDb.createSmallLob(
                        CLOB, s.getBytes(Constants.UTF8));
            case BLOB:
                return ValueLobDb.createSmallLob(
                        BLOB, StringUtils.convertHexToBytes(s.trim()));
            case ARRAY:
                return ValueArray.get(new Value[]{ValueString.get(s)});
            case RESULT_SET: {
                SimpleResultSet rs = new SimpleResultSet();
                rs.setAutoClose(false);
                rs.addColumn("X", Types.VARCHAR, s.length(), 0);
                rs.addRow(s);
                return ValueResultSet.get(rs);
            }
            case UUID:
                return ValueUuid.get(s);
            case GEOMETRY:
                return ValueGeometry.get(s);
            default:
                throw DbException.throwInternalError("type=" + targetType);
            }
        } catch (NumberFormatException e) {
            throw DbException.get(
                    ErrorCode.DATA_CONVERSION_ERROR_1, e, getString());
        }
    }

    /**
     * Compare this value against another value given that the values are of the
     * same data type.
     *
     * @param v the other value
     * @param mode the compare mode
     * @return 0 if both values are equal, -1 if the other value is smaller, and
     *         1 otherwise
     */
    public final int compareTypeSave(Value v, CompareMode mode) {
        if (this == v) {
            return 0;
        } else if (this == ValueNull.INSTANCE) {
            return -1;
        } else if (v == ValueNull.INSTANCE) {
            return 1;
        }
        return compareSecure(v, mode);
    }

    /**
     * Compare this value against another value using the specified compare
     * mode.
     *
     * @param v the other value
     * @param mode the compare mode
     * @return 0 if both values are equal, -1 if the other value is smaller, and
     *         1 otherwise
     */
    public final int compareTo(Value v, CompareMode mode) {
        if (this == v) {
            return 0;
        }
        if (this == ValueNull.INSTANCE) {
            return v == ValueNull.INSTANCE ? 0 : -1;
        } else if (v == ValueNull.INSTANCE) {
            return 1;
        }
        if (getType() == v.getType()) {
            return compareSecure(v, mode);
        }
        int t2 = Value.getHigherOrder(getType(), v.getType());
        return convertTo(t2).compareSecure(v.convertTo(t2), mode);
    }

    public int getScale() {
        return 0;
    }

    /**
     * Convert the scale.
     *
     * @param onlyToSmallerScale if the scale should not reduced
     * @param targetScale the requested scale
     * @return the value
     */
    public Value convertScale(boolean onlyToSmallerScale, int targetScale) {
        return this;
    }

    /**
     * Convert the precision to the requested value. The precision of the
     * returned value may be somewhat larger than requested, because values with
     * a fixed precision are not truncated.
     *
     * @param precision the new precision
     * @param force true if losing numeric precision is allowed
     * @return the new value
     */
    public Value convertPrecision(long precision, boolean force) {
        return this;
    }

    private static byte convertToByte(long x) {
        if (x > Byte.MAX_VALUE || x < Byte.MIN_VALUE) {
            throw DbException.get(
                    ErrorCode.NUMERIC_VALUE_OUT_OF_RANGE_1, Long.toString(x));
        }
        return (byte) x;
    }

    private static short convertToShort(long x) {
        if (x > Short.MAX_VALUE || x < Short.MIN_VALUE) {
            throw DbException.get(
                    ErrorCode.NUMERIC_VALUE_OUT_OF_RANGE_1, Long.toString(x));
        }
        return (short) x;
    }

    private static int convertToInt(long x) {
        if (x > Integer.MAX_VALUE || x < Integer.MIN_VALUE) {
            throw DbException.get(
                    ErrorCode.NUMERIC_VALUE_OUT_OF_RANGE_1, Long.toString(x));
        }
        return (int) x;
    }

    private static long convertToLong(double x) {
        if (x > Long.MAX_VALUE || x < Long.MIN_VALUE) {
            // TODO document that +Infinity, -Infinity throw an exception and
            // NaN returns 0
            throw DbException.get(
                    ErrorCode.NUMERIC_VALUE_OUT_OF_RANGE_1, Double.toString(x));
        }
        return Math.round(x);
    }

    private static long convertToLong(BigDecimal x) {
        if (x.compareTo(MAX_LONG_DECIMAL) > 0 ||
                x.compareTo(Value.MIN_LONG_DECIMAL) < 0) {
            throw DbException.get(
                    ErrorCode.NUMERIC_VALUE_OUT_OF_RANGE_1, x.toString());
        }
        return x.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
    }

    /**
     * Link a large value to a given table. For values that are kept fully in
     * memory this method has no effect.
     *
     * @param handler the data handler
     * @param tableId the table to link to
     * @return the new value or itself
     */
    public Value link(DataHandler handler, int tableId) {
        return this;
    }

    /**
     * Check if this value is linked to a specific table. For values that are
     * kept fully in memory, this method returns false.
     *
     * @return true if it is
     */
    public boolean isLinked() {
        return false;
    }

    /**
     * Mark any underlying resource as 'not linked to any table'. For values
     * that are kept fully in memory this method has no effect.
     *
     * @param handler the data handler
     */
    public void unlink(DataHandler handler) {
        // nothing to do
    }

    /**
     * Close the underlying resource, if any. For values that are kept fully in
     * memory this method has no effect.
     */
    public void close() {
        // nothing to do
    }

    /**
     * Check if the precision is smaller or equal than the given precision.
     *
     * @param precision the maximum precision
     * @return true if the precision of this value is smaller or equal to the
     *         given precision
     */
    public boolean checkPrecision(long precision) {
        return getPrecision() <= precision;
    }

    /**
     * Get a medium size SQL expression for debugging or tracing. If the
     * precision is too large, only a subset of the value is returned.
     *
     * @return the SQL expression
     */
    public String getTraceSQL() {
        return getSQL();
    }

    @Override
    public String toString() {
        return getTraceSQL();
    }

    /**
     * Throw the exception that the feature is not support for the given data
     * type.
     *
     * @param op the operation
     * @return never returns normally
     * @throws DbException the exception
     */
    protected DbException throwUnsupportedExceptionForType(String op) {
        throw DbException.getUnsupportedException(
                DataType.getDataType(getType()).name + " " + op);
    }

    /**
     * Get the table (only for LOB object).
     *
     * @return the table id
     */
    public int getTableId() {
        return 0;
    }

    /**
     * Get the byte array.
     *
     * @return the byte array
     */
    public byte[] getSmall() {
        return null;
    }

    /**
     * Copy this value to a temporary file if necessary.
     *
     * @return the new value
     */
    public Value copyToTemp() {
        return this;
    }

    public ResultSet getResultSet() {
        SimpleResultSet rs = new SimpleResultSet();
        rs.setAutoClose(false);
        rs.addColumn("X", DataType.convertTypeToSQLType(getType()),
                MathUtils.convertLongToInt(getPrecision()), getScale());
        rs.addRow(getObject());
        return rs;
    }

    /**
     * Return the data handler for the values that support it
     * (actually only Java objects).
     * @return the data handler
     */
    protected DataHandler getDataHandler() {
        return null;
    }

    /**
     * A "binary large object".
     */
    public interface ValueClob {
        // this is a marker interface
    }

    /**
     * A "character large object".
     */
    public interface ValueBlob {
        // this is a marker interface
    }
=======
/*******************************************************************************
 * Copyright (c) 2009 Lifeform Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ernan Hughes - initial API and implementation
 *******************************************************************************/
package org.lifeform.math;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.lifeform.core.Pair;
import org.lifeform.util.ArrayUtils;

public class Statistics {

	public static double[] bollinger(final int n, final int deviations,
			final double[] vals, final int skipdays) {
		final double[] value = new double[3];

		final double centerband = SMA(n, vals, skipdays);

		final double t2 = deviation(n, vals, skipdays);

		final double upper = centerband + (deviations * t2);
		final double lower = centerband - (deviations * t2);

		value[2] = upper;
		value[1] = centerband;
		value[0] = lower;

		return value;
	}

	/**
	 * method to compute the compound interest.
	 * 
	 * @param cash
	 *            i.e. 5000
	 * @param unitsToComputeFor
	 *            i.e. 12 days
	 * @param unitsPerAnnum
	 *            i.e. 360 days
	 * @param interestRatePerAnnum
	 *            i.e. 0.04 for 4%.
	 * @return the interest result, according to ((
	 *         Math.pow((1.0+(interestRatePerAnnum/365.0)),daysToComputeFor) -
	 *         1) * cash; )
	 */
	public static double compoundInterest(final double cash,
			final double unitsToComputeFor, final double unitsPerAnnum,
			final double interestRatePerAnnum) {
		final double interest = (Math.pow(
				(1.0 + (interestRatePerAnnum / unitsPerAnnum)),
				unitsToComputeFor) - 1)
				* cash;
		return interest;
	}

	/**
	 * checks if the values 0 and 1 of two double series cross over each other.
	 * Example:<br>
	 * series1: 10,9,8,7 <br>
	 * series2: 9,10,11<br>
	 * This would yield yes. <br>
	 * 
	 * @param series1
	 * @param series2
	 * @return
	 */
	public static boolean cross(final double[] series1, final double[] series2) {
		if ((series1[0] < series2[0]) && (series1[1] > series2[1])) {
			return true;
		}
		if ((series1[0] > series2[0]) && (series1[1] < series2[1])) {
			return true;
		}
		return false;
	}

	public static double deviation(final int n, final double[] vals,
			final int skipdays) {
		final double centerband = SMA(n, vals, skipdays);

		double t1 = 0.0;

		for (int i = 0; i < n; i++) {
			t1 += ((vals[i + skipdays] - centerband) * (vals[i + skipdays] - centerband));
		}

		final double t2 = Math.sqrt(t1 / n);

		return t2;
	}

	/**
	 * returns the exponential moving average <br/>
	 * see http://www.quotelinks.com/technical/ema.html
	 * 
	 * @param n
	 * @param candles
	 * @param skipdays
	 * @return the exponential moving average
	 */
	public static double EMA(final int n, final double[] vals,
			final int skipdays) {
		double value = 0;

		final double exponent = 2 / (double) (n + 1);

		value = vals[vals.length - 1];

		for (int i = vals.length - 1; i > skipdays - 1; i--) {

			value = (vals[i] * exponent) + (value * (1 - exponent));

		}

		return value;
	}

	/**
	 * Calculates Pivot Points, contributed by Dan O'Rourke.
	 * 
	 * @param open
	 * @param high
	 * @param low
	 * @param close
	 * @param positionInTime
	 * @return
	 */
	public static double[] getPivotPoints(final double[] open,
			final double[] high, final double[] low, final double[] close,
			final int pos) {
		final double[] ret = new double[7];

		ret[3] = (high[pos] + low[pos] + close[pos]) / 3;
		// r1
		ret[2] = (ret[2] * 2) - low[pos];
		// r2
		ret[1] = (ret[2] + high[pos] - low[pos]);
		// r3
		ret[0] = (ret[1] + high[pos] - low[pos]);

		// s1
		ret[4] = (ret[3] * 2) - high[pos];
		// s2
		ret[5] = (ret[4] - high[pos] + low[pos]);
		// s3
		ret[6] = (ret[5] - high[pos] + low[pos]);

		return ret;
	}

	/**
	 * maps to the metastock hhvbars method.
	 * 
	 * @param n
	 * @param data
	 * @return
	 */
	public static int hhvbars(int n, double[] data) {
		int periodsSinceHigh = 0;
		double max = -10000000;
		if (n > data.length - 1) {
			n = data.length - 1;
		}
		for (int i = 0; i < n; i++) {
			if (data[i] > max) {
				periodsSinceHigh = i;
				max = data[i];
			}
		}
		return periodsSinceHigh;
	}

	/**
	 * checks if something is a hammer or not.
	 * 
	 * @param series
	 * @param filterPeriod
	 * @param multiplier
	 * @param position
	 * @return
	 */
	// public static boolean isHammer(
	// final org.activequant.core.domainmodel.data.CandleSeries series,
	// final int filterPeriod, final double multiplier, final int position) {
	// //
	//
	// //
	// final double xaverage = EMA(filterPeriod, series.getCloses(), position);
	//
	// if (series.get(position).getClosePrice() < xaverage) {
	//
	// final double open = series.get(position).getOpenPrice();
	// final double high = series.get(position).getHighPrice();
	// final double low = series.get(position).getLowPrice();
	// final double close = series.get(position).getClosePrice();
	//
	// final double bodyMin = minOf(close, open);
	// final double bodyMax = maxOf(close, open);
	// final double candleBody = bodyMax - bodyMin;
	// final double rangeMedian = (high + low) * 0.5;
	// final double upShadow = high - bodyMax;
	// final double downShadow = bodyMin - low;
	//
	// final boolean isHammer = (open != close) && (bodyMin > rangeMedian)
	// && (downShadow > (candleBody * multiplier))
	// && (upShadow < candleBody);
	//
	// return isHammer;
	//
	// } else {
	// return false;
	// }
	//
	// }

	/**
	 * checks if something is a hammer or not by another algorithm
	 * 
	 * @param series
	 * @param filterPeriod
	 * @param multiplier
	 * @param position
	 * @return
	 */
	// public static boolean isHammer2(
	// final org.activequant.core.domainmodel.data.CandleSeries series,
	// final int filterPeriod, final double multiplier, final int position) {
	// //
	// int ups = 0;
	// int downs = 0;
	// for (int i = position + 1; i < position + filterPeriod + 1; i++) {
	// if (series.get(i).isRising()) {
	// ups++;
	// } else {
	// downs++;
	// }
	// }
	// if (ups > downs) {
	// return false;
	// }
	//
	// if (series.get(position + 1).isRising()) {
	// return false;
	// }
	// if (series.get(position).getClosePrice() > series.get(position + 1)
	// .getClosePrice()) {
	// return false;
	// }
	//
	// final double open = series.get(position).getOpenPrice();
	// final double high = series.get(position).getHighPrice();
	// final double low = series.get(position).getLowPrice();
	// final double close = series.get(position).getClosePrice();
	//
	// final double bodyMin = minOf(close, open);
	// final double bodyMax = maxOf(close, open);
	// final double candleBody = bodyMax - bodyMin;
	// final double rangeMedian = (high + low) * 0.5;
	// final double upShadow = high - bodyMax;
	// final double downShadow = bodyMin - low;
	//
	// final boolean isHammer = (open != close) && (bodyMin > rangeMedian)
	// && (downShadow > (candleBody * multiplier))
	// && (upShadow < downShadow);
	//
	// return isHammer;
	//
	// }

	/**
	 * maps to the metastock llvbars method.
	 * 
	 * @param n
	 * @param data
	 * @return
	 */
	public static int llvbars(int n, double[] data) {
		int periodsSinceHigh = 0;
		double min = 1000000000;
		if (n > data.length - 1) {
			n = data.length - 1;
		}
		for (int i = 0; i < n; i++) {
			if (data[i] < min) {
				periodsSinceHigh = i;
				min = data[i];
			}
		}
		return periodsSinceHigh;
	}

	/**
	 * returns the logarithmic change value for double[0] and double[1].
	 * double[0] must be the more recent value.
	 * 
	 * @param in
	 * @return
	 * @throws NotEnoughDataException
	 */
	public static double logChange(final double... in) {
		assert in.length == 2 : "Too few inputs.";
		return Math.log(in[0] / in[1]);
	}

	public static double max(final double... values) {
		double max = Double.MIN_VALUE;
		for (double value : values) {
			if (value > max) {
				max = value;
			}
		}
		return max;
	}

	public static double max(final int start, final int end,
			final double... values) {
		final double[] sublist = new double[end - start + 1];
		System.arraycopy(values, start, sublist, 0, sublist.length);
		return max(sublist);
	}

	public static double maxOf(final double v1, final double v2) {
		if (v1 >= v2) {
			return v1;
		} else {
			return v2;
		}
	}

	/**
	 * returns the max value out of three.
	 */
	public static double maxOf(final double v1, final double v2, final double v3) {
		if ((v1 >= v2) && (v1 >= v3)) {
			return v1;
		}
		if ((v2 >= v1) && (v2 >= v3)) {
			return v2;
		}
		if ((v3 >= v1) && (v3 >= v2)) {
			return v3;
		}
		return v1;
	}

	/**
	 * calculates the plain mean of an array of doubles
	 * 
	 * @param vals
	 * @return plain mean of an array of doubles
	 */
	public static double mean(final double... vals) {
		double v = 0;
		for (final double val : vals) {
			v += val;
		}
		v /= vals.length;
		return v;
	}

	/**
	 * calculates the MEMA
	 * 
	 * @param period
	 * @param candles
	 * @param skipdays
	 * @return the MEMA
	 */
	public static double MEMA(final int period, final double[] values,
			final int skipdays) {
		double mema = 0.0;
		double smoothing = 1;

		if (period != 0) {
			smoothing = 1 / (double) period;
		}

		int max = values.length;
		if (max > 600 + skipdays + 2 + period) {
			max = 500 + skipdays + 2 + period;
		} else {
			max = values.length - skipdays - 1 - period;
		}
		for (int i = max; i >= skipdays; i--) {
			final double value = values[i];
			if (i == max) {
				// ok, beginning of calculation
				mema = SMA(period, values, i);
			} else {
				mema = (smoothing * value) + ((1 - smoothing) * mema);
			}
		}
		return mema;
	}

	public static double min(final double... values) {
		double min = Double.MAX_VALUE;
		for (final double value : values) {
			if (value < min) {
				min = value;
			}
		}
		return min;
	}

	public static double min(final int start, final int end,
			final double... values) {
		final double[] sublist = new double[end - start + 1];
		System.arraycopy(values, start, sublist, 0, sublist.length);
		return min(sublist);
	}

	/**
	 * returns the minimum value of two doubles.
	 * 
	 * @param v1
	 * @param v2
	 * @return minimum of a value of two doubles.
	 */
	public static double minOf(final double v1, final double v2) {
		if (v1 <= v2) {
			return v1;
		}
		if (v2 <= v1) {
			return v2;
		}
		return v1;
	}

	/**
	 * returns the minimum value of three doubles
	 * 
	 * @param v1
	 * @param v2
	 * @param v3
	 * @return the minimum value of three doubles
	 */

	/**
	 * computes the average price from a non discrete signal using a simple
	 * integral. List must be in order t(0) is oldest and t(X) is newest.
	 * 
	 * @param values
	 * @return
	 */
	public static double nonDiscreteAverage(
			final List<Pair<Calendar, Double>> values) {

		Pair<Calendar, Double> current = values.get(0);
		double wholeArea = 0.0;
		for (final Pair<Calendar, Double> val : values) {
			final double baseArea = (val.getSecond())
					* (val.getFirst().getTimeInMillis() - current.getFirst()
							.getTimeInMillis());
			final double upperArea = ((current.getSecond() - val.getSecond()) * (val
					.getFirst().getTimeInMillis() - current.getFirst()
					.getTimeInMillis())) / 2.0;
			wholeArea = baseArea + upperArea;
			current = val;
		}

		return wholeArea;
	}

	/**
	 * returns a normalized copy of the input array
	 */
	public static double[] normalizeArray(final double... in) {
		final double min = min(in);
		final double max = max(in);
		final double[] ret = new double[in.length];

		for (int i = 0; i < in.length; i++) {
			ret[i] = (in[i] - min) / (max - min);
		}
		return ret;
	}

	/**
	 * returns the price range R as an array of doubles.
	 * 
	 * @return the price range
	 */
	public static double[] priceRange(final double[] opens,
			final double[] highs, final double[] lows, final double[] closes,
			final int skipdays) {
		final List<Double> results = new ArrayList<Double>();
		boolean first_run_range = true;
		int max = opens.length - 1;
		if (max > 200) {
			max = 200;
		}
		for (int i = max; i > skipdays - 1; i--) {
			double result = 0.0;
			if (first_run_range) {
				first_run_range = false;
				result = highs[i] - lows[i];
			} else {
				final double v1 = highs[i] - lows[i];
				final double v2 = highs[i] - closes[i + 1];
				final double v3 = closes[i + 1] - lows[i];

				if ((v1 >= v2) && (v1 >= v3)) {
					result = v1;
				} else if ((v2 >= v1) && (v2 >= v3)) {
					result = v2;
				} else if ((v3 >= v1) && (v3 >= v2)) {
					result = v3;
				}
			}
			results.add(0, result);
		}
		return ArrayUtils.unboxDoubles(results);
	}

	/**
	 * calculates the slope, relative to the price and scales it by 100.
	 * 
	 * @param n
	 * @param values
	 * @param skipdays
	 * @return
	 */
	public static double priceSlope(final int n, final double[] values,
			final int skipdays) {
		double value = 0.0;
		value = (values[skipdays] - values[n + skipdays]) / values[skipdays]
				* 100;
		return value;
	}

	/**
	 * returns the rate of change
	 * 
	 * @param n
	 * @param candles
	 * @param skipdays
	 * @return the rate of change
	 */
	public static double ROC(final int n, final double[] vals,
			final int skipdays) {
		double value = 0.0;
		final double v0 = vals[skipdays];
		final double v1 = vals[skipdays + n];

		value = (v0 - v1) / v0 * 100;

		return value;
	}

	/**
	 * returns the RSI
	 * 
	 * @param n
	 * @param values
	 * @param skipdays
	 * @return the RSI
	 */
	public static double RSI(final int n, final double[] vals,
			final int skipdays) {
		double U = 0.0;
		double D = 0.0;

		for (int i = 0; i < n; i++) {
			final double v0 = vals[skipdays + i];
			final double v1 = vals[skipdays + i + 1];

			final double change = v0 - v1;

			if (change > 0) {
				U += change;
			} else {
				D += Math.abs(change);
			}
		}

		// catch division by zero
		if ((D == 0) || ((1 + (U / D)) == 0)) {
			assert false : "Division by zero";
			return 0.0;
		}

		return 100 - (100 / (1 + (U / D)));
	}

	/**
	 * returns the parabolic SAR - double check with a reference implementation
	 * !
	 * 
	 * @param initialValue
	 *            TODO
	 * @param candles
	 * @param skipdays
	 * @param n
	 * 
	 * @return the parabolic SAR
	 */
	public static double SAR(final double af, final double max,
			final double[] lows, final double[] highs, final int skipdays) {

		final List<Double> l1 = new ArrayList<Double>();
		final List<Double> l2 = new ArrayList<Double>();
		for (final Double d : lows) {
			l1.add(d);
		}
		for (final Double d : highs) {
			l2.add(d);
		}

		Collections.reverse(l1);
		Collections.reverse(l2);

		// need to reverse from activequant norm for ta lib.
		final double[] lowsReversed = new double[lows.length - skipdays];
		final double[] highsReversed = new double[highs.length - skipdays];

		for (int i = 0; i < lowsReversed.length; i++) {
			lowsReversed[i] = l1.get(i);
			highsReversed[i] = l2.get(i);
		}

		Integer outBegIdx = new Integer(0);
		final double[] outArray = new double[highsReversed.length];
		final double value = outArray[outArray.length - 1 - outBegIdx];
		return value;
	}

	/**
	 * this function does scale the input parameters into the values -1...1. Can
	 * be useful for various aspects.
	 * 
	 * @param in
	 *            the input values
	 * @return the input values in the range -1 .. 1
	 */
	public static double[] scale(final double... in) {
		final double[] ret = new double[in.length];

		for (int i = 0; i < in.length; i++) {
			ret[i] = -1 + 2 * in[i];
		}
		return ret;
	}

	/**
	 * calculates the sharpe Ratio, you need to pipe in returns in PERCENT!!!!
	 * this sharpe ratio calculation calculates based on a periodically series
	 * of returns and the std deviation of these returns Example of use: create
	 * an array of weekly returns, i.e. {0.1, 0.2, 0.01, 0.04} know the weekly
	 * interest rate, for example 0.04/52
	 * 
	 * @param returns
	 *            - this double[] must contain the return in percents for a
	 *            given period (i.e. 0.1)
	 * @param interest
	 *            - the interest rate in percent for this period (i.e. 0.035)
	 * @return the sharpe ratio
	 */
	public static double sharpeRatio(final double interest,
			final double... returns) {
		if (returns.length > 1) {
			double ret = 0.0, avg = 0.0, stddev = 0.0;
			avg = mean(returns);
			stddev = deviation(returns.length, returns, 0);
			ret = (avg - interest) / stddev;
			return ret;
		} else if (returns.length == 1) {
			return returns[0] - interest;
		} else {
			return 0;
		}
	}

	/**
	 * returns the slope between two timepoints
	 * 
	 * @param n
	 * @param candles
	 * @param skipdays
	 * @return the slope
	 */
	public static double slope(final int n, final double[] values,
			final int skipdays) {
		double value = 0.0;
		value = (values[skipdays] - values[n + skipdays]) / n;
		return value;
	}

	public static double SMA(final int period, final double[] vals,
			final int skipdays) {

		double value = 0.0;
		// debugPrint("SMA("+period+") for "+candles.size()+ " skipd:
		// "+skipdays);

		for (int i = skipdays; i < (period + skipdays); i++) {
			value += vals[i];
		}

		value /= period;

		return value;
	}

	/**
	 * returns a SMA smoothed slope
	 * 
	 * @param n
	 * @param smoothingfactor
	 * @param candles
	 * @param skipdays
	 * @return smoother slope
	 */
	public static double smoothedSlope(final int n, final int smoothingunits,
			final double[] vals, final int skipdays) {
		double value = 0.0;
		final double[] values = new double[smoothingunits];
		for (int i = 0; i < (smoothingunits); i++) {
			values[i] = slope(n, vals, skipdays + i);
		}
		value = SMA(smoothingunits, values, 0);
		return value;
	}

	public static double standardDeviation(final double... values) {
		return standardDeviation(values);
	}

	public static double standardGradient(final double... values) {
		double sum = 0.0;
		for (int i = 0; i < values.length - 1; i++) {
			final double difference = values[i + 1] - values[i];
			sum += Math.abs(difference);
		}
		return sum / (values.length - 1);
	}

	public static double standardGradientDeviation(final double... values) {
		final double standardGradient = standardGradient(values);
		final double[] newValues = new double[values.length - 1];
		for (int i = 0; i < newValues.length; i++) {
			final double difference = Math.abs(values[i + 1] - values[i]);
			newValues[i] = Math.pow((difference - standardGradient), 2);
		}
		return Math.sqrt(mean(newValues));
	}

	/**
	 * returns the units / bars since the last change in the array.<br>
	 * Example:<br>
	 * series data: 10,10,10,10,3,4,5,4,4,4,4<br>
	 * would return 4<br>
	 * This function tries to work like the metastock barsSince(..) function.<br>
	 * 
	 * @param array
	 * @return
	 */
	public static int unitsSinceChange(final double... array) {
		int i = 0;
		for (i = 0; i < array.length - 1; i++) {
			if (array[i] != array[i + 1]) {
				return i;
			}
		}
		return i;
	}

	/**
	 * 
	 * This methods returns the the value of the array data at the n'th
	 * occurance of a true in the boolean array.<br>
	 * Example:<br>
	 * occurance : 2<br>
	 * boolean array: 0,1,0,0,0,1<br>
	 * data: 2,1,3,4,1,2<br>
	 * would return 2.
	 * 
	 * @param occurance
	 * @param booleanArray
	 * @param data
	 * @return the last (oldest) element in data if the criterias are never
	 *         matched, otherwise see above.
	 */
	public static double valueWhen(final int occurance,
			final boolean[] booleanArray, final double[] data) {
		int n = 0;
		for (int i = 0; i < booleanArray.length; i++) {
			if (booleanArray[i]) {
				n++;
				if (n == occurance) {
					return data[i];
				}
			}
		}
		return data[data.length - 1];
	}

	/**
	 * calculates the volatility Index, returns the trend following system
	 * working with SAR points. indicator requires at least 100 candles !
	 * 
	 * @param p1
	 *            the factor
	 * @param p2
	 *            the periods to work on.
	 * @param candles
	 *            this are the input candles.
	 * @param skipdays
	 *            this parameter specifies how many days to skip.
	 * @return the volatility Index
	 */
	public static double volatilityIndex(final int p1, final int p2,
			final double[] opens, final double[] highs, final double[] lows,
			final double[] closes, final int skipdays) {

		boolean first_run_vlx = true;
		boolean position_long = true;
		double sip = 0, sar = 0, next_sar = 0, smoothed_range = 0;

		int max = closes.length - skipdays - 2;
		if (max > 200) {
			max = 200;
		}

		for (int i = max; i > skipdays - 1; i--) {

			final double value = closes[i];
			smoothed_range = MEMA(p2,
					priceRange(opens, highs, lows, closes, i), 0);
			final double atr = smoothed_range * p1;

			if (first_run_vlx && (smoothed_range != 0)) {
				first_run_vlx = false;
				sip = max(i, i + p2, highs);
				next_sar = sip - atr;
				sar = next_sar;
			} else {
				sar = next_sar;
				if (position_long) {
					if (value < sar) {
						position_long = false;
						sip = value;
						next_sar = sip + (smoothed_range * p1);
					} else {
						position_long = true;
						sip = (value > sip) ? value : sip;
						next_sar = sip - (smoothed_range * p1);
					}
				} else {
					if (value > sar) {
						position_long = true;
						sip = value;
						next_sar = sip - (smoothed_range * p1);
					} else {
						position_long = false;
						sip = (value < sip) ? value : sip;
						next_sar = sip + (smoothed_range * p1);
					}
				}
			}
		}
		return sar;
	}

	public static double volatilityIndex(final int p1, final int p2,
			final double[][] ohlc, final int skipdays) {
		return volatilityIndex(p1, p2, ohlc[0], ohlc[1], ohlc[2], ohlc[3],
				skipdays);
	}

	/**
	 * returns the linearly weighted moving average.
	 * 
	 * @param period
	 * @param candles
	 * @param skipdays
	 * @return the wma
	 */
	public static double WMA(final int period, final double[] vals,
			final int skipdays) {

		double numerator = 0.0;

		int weight = period;
		for (int i = skipdays; i < (period + skipdays); i++) {
			numerator += vals[i] * weight;
			weight--;
		}

		final int denominator = period * (period + 1) / 2;
		final double value = numerator / denominator;

		return value;
	}

	/**
	 * returns the normalized yield between in1[0] and in2[lag].
	 * 
	 * @param in1
	 * @param in2
	 * @return double yield
	 * @throws NotEnoughDataException
	 */
	public static double[] yield(final double[] in2, final int lag,
			final double... in1) {
		final double[] ret = new double[in1.length];
		assert (in1.length < lag) && (in1.length != in2.length) : "Too few inputs.";
		for (int i = 0; i < in1.length - lag; i++) {
			ret[i] = Math.log(in1[i] / in2[i + lag]) * 100;
		}
		return ret;
	}

	/**
	 * returns the yield with a given lag.
	 * 
	 * @param in
	 * @return double
	 * @throws NotEnoughDataException
	 */
	public static double[] yield(final int lag, final double... in) {
		final double[] ret = new double[in.length];
		assert (in.length < lag) : "Too few inputs.";
		for (int i = 0; i < in.length - lag; i++) {
			ret[i] = Math.log(in[i] / in[i + lag]) * 100;
		}
		return ret;
	}

	private Statistics() {

	}
>>>>>>> 76aa07461566a5976980e6696204781271955163

}

