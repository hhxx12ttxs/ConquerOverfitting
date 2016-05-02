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

}

=======
package org.json;

/*
Copyright (c) 2002 JSON.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

The Software shall be used for Good, not Evil.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

/**
 * A JSONObject is an unordered collection of name/value pairs. Its
 * external form is a string wrapped in curly braces with colons between the
 * names and values, and commas between the values and names. The internal form
 * is an object having <code>get</code> and <code>opt</code> methods for
 * accessing the values by name, and <code>put</code> methods for adding or
 * replacing values by name. The values can be any of these types:
 * <code>Boolean</code>, <code>JSONArray</code>, <code>JSONObject</code>,
 * <code>Number</code>, <code>String</code>, or the <code>JSONObject.NULL</code>
 * object. A JSONObject constructor can be used to convert an external form
 * JSON text into an internal form whose values can be retrieved with the
 * <code>get</code> and <code>opt</code> methods, or to convert values into a
 * JSON text using the <code>put</code> and <code>toString</code> methods.
 * A <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object, which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you.
 * <p>
 * The <code>put</code> methods adds values to an object. For example, <pre>
 *     myString = new JSONObject().put("JSON", "Hello, World!").toString();</pre>
 * produces the string <code>{"JSON": "Hello, World"}</code>.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * the JSON syntax rules.
 * The constructors are more forgiving in the texts they will accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 *     before the closing brace.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 *     quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 *     or single quote, and if they do not contain leading or trailing spaces,
 *     and if they do not contain any of these characters:
 *     <code>{ } [ ] / \ : , = ; #</code> and if they do not look like numbers
 *     and if they are not the reserved words <code>true</code>,
 *     <code>false</code>, or <code>null</code>.</li>
 * <li>Keys can be followed by <code>=</code> or <code>=></code> as well as
 *     by <code>:</code>.</li>
 * <li>Values can be followed by <code>;</code> <small>(semicolon)</small> as
 *     well as by <code>,</code> <small>(comma)</small>.</li>
 * <li>Numbers may have the <code>0-</code> <small>(octal)</small> or
 *     <code>0x-</code> <small>(hex)</small> prefix.</li>
 * </ul>
 * @author JSON.org
 * @version 2009-03-06
 */
public class JSONObject {

    /**
     * JSONObject.NULL is equivalent to the value that JavaScript calls null,
     * whilst Java's null is equivalent to the value that JavaScript calls
     * undefined.
     */
     static final class Null {

        /**
         * There is only intended to be a single instance of the NULL object,
         * so the clone method returns itself.
         * @return     NULL.
         */
    	@Override
        protected final Object clone() {
            return this;
        }


        /**
         * A Null object is equal to the null value and to itself.
         * @param object    An object to test for nullness.
         * @return true if the object parameter is the JSONObject.NULL object
         *  or null.
         */
    	@Override
        public boolean equals(Object object) {
            return object == null || object == this;
        }


        /**
         * Get the "null" string value.
         * @return The string "null".
         */
        @Override
		public String toString() {
            return "null";
        }
    }


    /**
     * The map where the JSONObject's properties are kept.
     */
    private Map<Object, Object> map;


    /**
     * It is sometimes more convenient and less ambiguous to have a
     * <code>NULL</code> object than to use Java's <code>null</code> value.
     * <code>JSONObject.NULL.equals(null)</code> returns <code>true</code>.
     * <code>JSONObject.NULL.toString()</code> returns <code>"null"</code>.
     */
    public static final Object NULL = new Null();


    /**
     * Construct an empty JSONObject.
     */
    public JSONObject() {
        this.map = new HashMap<Object, Object>();
    }


    /**
     * Construct a JSONObject from a subset of another JSONObject.
     * An array of strings is used to identify the keys that should be copied.
     * Missing keys are ignored.
     * @param jo A JSONObject.
     * @param names An array of strings.
     * @exception JSONException If a value is a non-finite number or if a name is duplicated.
     */
    public JSONObject(JSONObject jo, String[] names) throws JSONException {
        this();
        for (int i = 0; i < names.length; i += 1) {
            putOnce(names[i], jo.opt(names[i]));
        }
    }


    /**
     * Construct a JSONObject from a JSONTokener.
     * @param x A JSONTokener object containing the source string.
     * @throws JSONException If there is a syntax error in the source string
     *  or a duplicated key.
     */
    public JSONObject(JSONTokener x) throws JSONException {
        this();
        char c;
        String key;

        if (x.nextClean() != '{') {
            throw x.syntaxError("A JSONObject text must begin with '{'");
        }
        for (;;) {
            c = x.nextClean();
            switch (c) {
            case 0:
                throw x.syntaxError("A JSONObject text must end with '}'");
            case '}':
                return;
            default:
                x.back();
                key = x.nextValue().toString();
            }

            /*
             * The key is followed by ':'. We will also tolerate '=' or '=>'.
             */

            c = x.nextClean();
            if (c == '=') {
                if (x.next() != '>') {
                    x.back();
                }
            } else if (c != ':') {
                throw x.syntaxError("Expected a ':' after a key");
            }
            putOnce(key, x.nextValue());

            /*
             * Pairs are separated by ','. We will also tolerate ';'.
             */

            switch (x.nextClean()) {
            case ';':
            case ',':
                if (x.nextClean() == '}') {
                    return;
                }
                x.back();
                break;
            case '}':
                return;
            default:
                throw x.syntaxError("Expected a ',' or '}'");
            }
        }
    }


    /**
     * Construct a JSONObject from a Map.
     *
     * @param map A map object that can be used to initialize the contents of
     *  the JSONObject.
     */
    public JSONObject(Map<Object, Object> map) {
        this.map = (map == null) ? new HashMap<Object, Object>() : map;
    }


    /**
     * Construct a JSONObject from a Map.
     *
     * Note: Use this constructor when the map contains <key,bean>.
     *
     * @param map - A map with Key-Bean data.
     * @param includeSuperClass - Tell whether to include the super class properties.
     */
    public JSONObject(Map<?, ?> map, boolean includeSuperClass) {
        this.map = new HashMap<Object, Object>();
        if (map != null) {
            Iterator<?> i = map.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) i.next();
                if (isStandardProperty(e.getValue().getClass())) {
                    this.map.put(e.getKey(), e.getValue());
                } else {
                    this.map.put(e.getKey(), new JSONObject(e.getValue(),
                            includeSuperClass));
                }
            }
        }
    }


    /**
     * Construct a JSONObject from an Object using bean getters.
     * It reflects on all of the public methods of the object.
     * For each of the methods with no parameters and a name starting
     * with <code>"get"</code> or <code>"is"</code> followed by an uppercase letter,
     * the method is invoked, and a key and the value returned from the getter method
     * are put into the new JSONObject.
     *
     * The key is formed by removing the <code>"get"</code> or <code>"is"</code> prefix.
     * If the second remaining character is not upper case, then the first
     * character is converted to lower case.
     *
     * For example, if an object has a method named <code>"getName"</code>, and
     * if the result of calling <code>object.getName()</code> is <code>"Larry Fine"</code>,
     * then the JSONObject will contain <code>"name": "Larry Fine"</code>.
     *
     * @param bean An object that has getter methods that should be used
     * to make a JSONObject.
     */
    public JSONObject(Object bean) {
        this();
        populateInternalMap(bean, false);
    }


    /**
     * Construct a JSONObject from an Object using bean getters.
     * It reflects on all of the public methods of the object.
     * For each of the methods with no parameters and a name starting
     * with <code>"get"</code> or <code>"is"</code> followed by an uppercase letter,
     * the method is invoked, and a key and the value returned from the getter method
     * are put into the new JSONObject.
     *
     * The key is formed by removing the <code>"get"</code> or <code>"is"</code> prefix.
     * If the second remaining character is not upper case, then the first
     * character is converted to lower case.
     *
     * @param bean An object that has getter methods that should be used
     * to make a JSONObject.
     * @param includeSuperClass If true, include the super class properties.
     */
    public JSONObject(Object bean, boolean includeSuperClass) {
        this();
        populateInternalMap(bean, includeSuperClass);
    }

    @SuppressWarnings("cast")
	private void populateInternalMap(Object bean, boolean includeSuperClassParam){
    	boolean includeSuperClass = includeSuperClassParam;
        Class<?> klass = bean.getClass();

        /* If klass.getSuperClass is System class then force includeSuperClass to false. */

        if (klass.getClassLoader() == null) {
            includeSuperClass = false;
        }

        Method[] methods = (includeSuperClass) ?
                klass.getMethods() : klass.getDeclaredMethods();
        for (int i = 0; i < methods.length; i += 1) {
            try {
                Method method = methods[i];
                if (Modifier.isPublic(method.getModifiers())) {
                    String name = method.getName();
                    String key = "";
                    if (name.startsWith("get")) {
                        key = name.substring(3);
                    } else if (name.startsWith("is")) {
                        key = name.substring(2);
                    }
                    if (key.length() > 0 &&
                            Character.isUpperCase(key.charAt(0)) &&
                            method.getParameterTypes().length == 0) {
                        if (key.length() == 1) {
                            key = key.toLowerCase();
                        } else if (!Character.isUpperCase(key.charAt(1))) {
                            key = key.substring(0, 1).toLowerCase() +
                                key.substring(1);
                        }

                        Object result = method.invoke(bean, (Object[])null);
                        if (result == null) {
                            map.put(key, NULL);
                        } else if (result.getClass().isArray()) {
                            map.put(key, new JSONArray(result, includeSuperClass));
                        } else if (result instanceof Collection<?>) { // List or Set
                            map.put(key, new JSONArray((Collection<?>) result, includeSuperClass));
                        } else if (result instanceof Map<?, ?>) {
                            map.put(key, new JSONObject((Map<?, ?>)result, includeSuperClass));
                        } else if (isStandardProperty(result.getClass())) { // Primitives, String and Wrapper
                            map.put(key, result);
                        } else {
                            if (result.getClass().getPackage().getName().startsWith("java") ||
                                    result.getClass().getClassLoader() == null) {
                                map.put(key, result.toString());
                            } else { // User defined Objects
                                map.put(key, new JSONObject(result, includeSuperClass));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    static boolean isStandardProperty(Class<?> clazz) {
        return clazz.isPrimitive()                  ||
            clazz.isAssignableFrom(Byte.class)      ||
            clazz.isAssignableFrom(Short.class)     ||
            clazz.isAssignableFrom(Integer.class)   ||
            clazz.isAssignableFrom(Long.class)      ||
            clazz.isAssignableFrom(Float.class)     ||
            clazz.isAssignableFrom(Double.class)    ||
            clazz.isAssignableFrom(Character.class) ||
            clazz.isAssignableFrom(String.class)    ||
            clazz.isAssignableFrom(Boolean.class);
    }


    /**
     * Construct a JSONObject from an Object, using reflection to find the
     * public members. The resulting JSONObject's keys will be the strings
     * from the names array, and the values will be the field values associated
     * with those keys in the object. If a key is not found or not visible,
     * then it will not be copied into the new JSONObject.
     * @param object An object that has fields that should be used to make a
     * JSONObject.
     * @param names An array of strings, the names of the fields to be obtained
     * from the object.
     */
    public JSONObject(Object object, String names[]) {
        this();
        Class<?> c = object.getClass();
        for (int i = 0; i < names.length; i += 1) {
            String name = names[i];
            try {
                putOpt(name, c.getField(name).get(object));
            } catch (Exception e) {
                /* forget about it */
            }
        }
    }


    /**
     * Construct a JSONObject from a source JSON text string.
     * This is the most commonly used JSONObject constructor.
     * @param source    A string beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @exception JSONException If there is a syntax error in the source
     *  string or a duplicated key.
     */
    public JSONObject(String source) throws JSONException {
        this(new JSONTokener(source));
    }


    /**
     * Accumulate values under a key. It is similar to the put method except
     * that if there is already an object stored under the key then a
     * JSONArray is stored under the key to hold all of the accumulated values.
     * If there is already a JSONArray, then the new value is appended to it.
     * In contrast, the put method replaces the previous value.
     * @param key   A key string.
     * @param value An object to be accumulated under the key.
     * @return this.
     * @throws JSONException If the value is an invalid number
     *  or if the key is null.
     */
    public JSONObject accumulate(String key, Object value)
            throws JSONException {
        testValidity(value);
        Object o = opt(key);
        if (o == null) {
            put(key, value instanceof JSONArray ?
                    new JSONArray().put(value) :
                    value);
        } else if (o instanceof JSONArray) {
            ((JSONArray)o).put(value);
        } else {
            put(key, new JSONArray().put(o).put(value));
        }
        return this;
    }


    /**
     * Append values to the array under a key. If the key does not exist in the
     * JSONObject, then the key is put in the JSONObject with its value being a
     * JSONArray containing the value parameter. If the key was already
     * associated with a JSONArray, then the value parameter is appended to it.
     * @param key   A key string.
     * @param value An object to be accumulated under the key.
     * @return this.
     * @throws JSONException If the key is null or if the current value
     *  associated with the key is not a JSONArray.
     */
    public JSONObject append(String key, Object value)
            throws JSONException {
        testValidity(value);
        Object o = opt(key);
        if (o == null) {
            put(key, new JSONArray().put(value));
        } else if (o instanceof JSONArray) {
            put(key, ((JSONArray)o).put(value));
        } else {
            throw new JSONException("JSONObject[" + key +
                    "] is not a JSONArray.");
        }
        return this;
    }


    /**
     * Produce a string from a double. The string "null" will be returned if
     * the number is not finite.
     * @param  d A double.
     * @return A String.
     */
    static public String doubleToString(double d) {
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            return "null";
        }

// Shave off trailing zeros and decimal point, if possible.

        String s = Double.toString(d);
        if (s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0) {
            while (s.endsWith("0")) {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith(".")) {
                s = s.substring(0, s.length() - 1);
            }
        }
        return s;
    }


    /**
     * Get the value object associated with a key.
     *
     * @param key   A key string.
     * @return      The object associated with the key.
     * @throws   JSONException if the key is not found.
     */
    public Object get(String key) throws JSONException {
        Object o = opt(key);
        if (o == null) {
            throw new JSONException("JSONObject[" + quote(key) +
                    "] not found.");
        }
        return o;
    }


    /**
     * Get the boolean value associated with a key.
     *
     * @param key   A key string.
     * @return      The truth.
     * @throws   JSONException
     *  if the value is not a Boolean or the String "true" or "false".
     */
    public boolean getBoolean(String key) throws JSONException {
        Object o = get(key);
        if (o.equals(Boolean.FALSE) ||
                (o instanceof String &&
                ((String)o).equalsIgnoreCase("false"))) {
            return false;
        } else if (o.equals(Boolean.TRUE) ||
                (o instanceof String &&
                ((String)o).equalsIgnoreCase("true"))) {
            return true;
        }
        throw new JSONException("JSONObject[" + quote(key) +
                "] is not a Boolean.");
    }


    /**
     * Get the double value associated with a key.
     * @param key   A key string.
     * @return      The numeric value.
     * @throws JSONException if the key is not found or
     *  if the value is not a Number object and cannot be converted to a number.
     */
    public double getDouble(String key) throws JSONException {
        Object o = get(key);
        try {
            return o instanceof Number ?
                ((Number)o).doubleValue() :
                Double.valueOf((String)o).doubleValue();
        } catch (Exception e) {
            throw new JSONException("JSONObject[" + quote(key) +
                "] is not a number.");
        }
    }


    /**
     * Get the int value associated with a key. If the number value is too
     * large for an int, it will be clipped.
     *
     * @param key   A key string.
     * @return      The integer value.
     * @throws   JSONException if the key is not found or if the value cannot
     *  be converted to an integer.
     */
    public int getInt(String key) throws JSONException {
        Object o = get(key);
        return o instanceof Number ?
                ((Number)o).intValue() : (int)getDouble(key);
    }


    /**
     * Get the JSONArray value associated with a key.
     *
     * @param key   A key string.
     * @return      A JSONArray which is the value.
     * @throws   JSONException if the key is not found or
     *  if the value is not a JSONArray.
     */
    public JSONArray getJSONArray(String key) throws JSONException {
        Object o = get(key);
        if (o instanceof JSONArray) {
            return (JSONArray)o;
        }
        throw new JSONException("JSONObject[" + quote(key) +
                "] is not a JSONArray.");
    }


    /**
     * Get the JSONObject value associated with a key.
     *
     * @param key   A key string.
     * @return      A JSONObject which is the value.
     * @throws   JSONException if the key is not found or
     *  if the value is not a JSONObject.
     */
    public JSONObject getJSONObject(String key) throws JSONException {
        Object o = get(key);
        if (o instanceof JSONObject) {
            return (JSONObject)o;
        }
        throw new JSONException("JSONObject[" + quote(key) +
                "] is not a JSONObject.");
    }


    /**
     * Get the long value associated with a key. If the number value is too
     * long for a long, it will be clipped.
     *
     * @param key   A key string.
     * @return      The long value.
     * @throws   JSONException if the key is not found or if the value cannot
     *  be converted to a long.
     */
    public long getLong(String key) throws JSONException {
        Object o = get(key);
        return o instanceof Number ?
                ((Number)o).longValue() : (long)getDouble(key);
    }


    /**
     * Get an array of field names from a JSONObject.
     *
     * @return An array of field names, or null if there are no names.
     */
    public static String[] getNames(JSONObject jo) {
        int length = jo.length();
        if (length == 0) {
            return null;
        }
        Iterator<?> i = jo.keys();
        String[] names = new String[length];
        int j = 0;
        while (i.hasNext()) {
            names[j] = (String)i.next();
            j += 1;
        }
        return names;
    }


    /**
     * Get an array of field names from an Object.
     *
     * @return An array of field names, or null if there are no names.
     */
    public static String[] getNames(Object object) {
        if (object == null) {
            return null;
        }
        Class<?> klass = object.getClass();
        Field[] fields = klass.getFields();
        int length = fields.length;
        if (length == 0) {
            return null;
        }
        String[] names = new String[length];
        for (int i = 0; i < length; i += 1) {
            names[i] = fields[i].getName();
        }
        return names;
    }


    /**
     * Get the string associated with a key.
     *
     * @param key   A key string.
     * @return      A string which is the value.
     * @throws   JSONException if the key is not found.
     */
    public String getString(String key) throws JSONException {
        return get(key).toString();
    }


    /**
     * Determine if the JSONObject contains a specific key.
     * @param key   A key string.
     * @return      true if the key exists in the JSONObject.
     */
    public boolean has(String key) {
        return this.map.containsKey(key);
    }


    /**
     * Determine if the value associated with the key is null or if there is
     *  no value.
     * @param key   A key string.
     * @return      true if there is no value associated with the key or if
     *  the value is the JSONObject.NULL object.
     */
    public boolean isNull(String key) {
        return JSONObject.NULL.equals(opt(key));
    }


    /**
     * Get an enumeration of the keys of the JSONObject.
     *
     * @return An iterator of the keys.
     */
    public Iterator<?> keys() {
        return this.map.keySet().iterator();
    }


    /**
     * Get the number of keys stored in the JSONObject.
     *
     * @return The number of keys in the JSONObject.
     */
    public int length() {
        return this.map.size();
    }


    /**
     * Produce a JSONArray containing the names of the elements of this
     * JSONObject.
     * @return A JSONArray containing the key strings, or null if the JSONObject
     * is empty.
     */
    public JSONArray names() {
        JSONArray ja = new JSONArray();
        Iterator<?>  keys = keys();
        while (keys.hasNext()) {
            ja.put(keys.next());
        }
        return ja.length() == 0 ? null : ja;
    }

    /**
     * Produce a string from a Number.
     * @param  n A Number
     * @return A String.
     * @throws JSONException If n is a non-finite number.
     */
    static public String numberToString(Number n)
            throws JSONException {
        if (n == null) {
            throw new JSONException("Null pointer");
        }
        testValidity(n);

// Shave off trailing zeros and decimal point, if possible.

        String s = n.toString();
        if (s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0) {
            while (s.endsWith("0")) {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith(".")) {
                s = s.substring(0, s.length() - 1);
            }
        }
        return s;
    }


    /**
     * Get an optional value associated with a key.
     * @param key   A key string.
     * @return      An object which is the value, or null if there is no value.
     */
    public Object opt(String key) {
        return key == null ? null : this.map.get(key);
    }


    /**
     * Get an optional boolean associated with a key.
     * It returns false if there is no such key, or if the value is not
     * Boolean.TRUE or the String "true".
     *
     * @param key   A key string.
     * @return      The truth.
     */
    public boolean optBoolean(String key) {
        return optBoolean(key, false);
    }


    /**
     * Get an optional boolean associated with a key.
     * It returns the defaultValue if there is no such key, or if it is not
     * a Boolean or the String "true" or "false" (case insensitive).
     *
     * @param key              A key string.
     * @param defaultValue     The default.
     * @return      The truth.
     */
    public boolean optBoolean(String key, boolean defaultValue) {
        try {
            return getBoolean(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Put a key/value pair in the JSONObject, where the value will be a
     * JSONArray which is produced from a Collection.
     * @param key   A key string.
     * @param value A Collection value.
     * @return      this.
     * @throws JSONException
     */
    public JSONObject put(String key, Collection<Object> value) throws JSONException {
        put(key, new JSONArray(value));
        return this;
    }


    /**
     * Get an optional double associated with a key,
     * or NaN if there is no such key or if its value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key   A string which is the key.
     * @return      An object which is the value.
     */
    public double optDouble(String key) {
        return optDouble(key, Double.NaN);
    }


    /**
     * Get an optional double associated with a key, or the
     * defaultValue if there is no such key or if its value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key   A key string.
     * @param defaultValue     The default.
     * @return      An object which is the value.
     */
    public double optDouble(String key, double defaultValue) {
        try {
            Object o = opt(key);
            return o instanceof Number ? ((Number)o).doubleValue() :
                new Double((String)o).doubleValue();
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get an optional int value associated with a key,
     * or zero if there is no such key or if the value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key   A key string.
     * @return      An object which is the value.
     */
    public int optInt(String key) {
        return optInt(key, 0);
    }


    /**
     * Get an optional int value associated with a key,
     * or the default if there is no such key or if the value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key   A key string.
     * @param defaultValue     The default.
     * @return      An object which is the value.
     */
    public int optInt(String key, int defaultValue) {
        try {
            return getInt(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get an optional JSONArray associated with a key.
     * It returns null if there is no such key, or if its value is not a
     * JSONArray.
     *
     * @param key   A key string.
     * @return      A JSONArray which is the value.
     */
    public JSONArray optJSONArray(String key) {
        Object o = opt(key);
        return o instanceof JSONArray ? (JSONArray)o : null;
    }


    /**
     * Get an optional JSONObject associated with a key.
     * It returns null if there is no such key, or if its value is not a
     * JSONObject.
     *
     * @param key   A key string.
     * @return      A JSONObject which is the value.
     */
    public JSONObject optJSONObject(String key) {
        Object o = opt(key);
        return o instanceof JSONObject ? (JSONObject)o : null;
    }


    /**
     * Get an optional long value associated with a key,
     * or zero if there is no such key or if the value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key   A key string.
     * @return      An object which is the value.
     */
    public long optLong(String key) {
        return optLong(key, 0);
    }


    /**
     * Get an optional long value associated with a key,
     * or the default if there is no such key or if the value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key   A key string.
     * @param defaultValue     The default.
     * @return      An object which is the value.
     */
    public long optLong(String key, long defaultValue) {
        try {
            return getLong(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get an optional string associated with a key.
     * It returns an empty string if there is no such key. If the value is not
     * a string and is not null, then it is coverted to a string.
     *
     * @param key   A key string.
     * @return      A string which is the value.
     */
    public String optString(String key) {
        return optString(key, "");
    }


    /**
     * Get an optional string associated with a key.
     * It returns the defaultValue if there is no such key.
     *
     * @param key   A key string.
     * @param defaultValue     The default.
     * @return      A string which is the value.
     */
    public String optString(String key, String defaultValue) {
        Object o = opt(key);
        return o != null ? o.toString() : defaultValue;
    }


    /**
     * Put a key/boolean pair in the JSONObject.
     *
     * @param key   A key string.
     * @param value A boolean which is the value.
     * @return this.
     * @throws JSONException If the key is null.
     */
    public JSONObject put(String key, boolean value) throws JSONException {
        put(key, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }


    /**
     * Put a key/double pair in the JSONObject.
     *
     * @param key   A key string.
     * @param value A double which is the value.
     * @return this.
     * @throws JSONException If the key is null or if the number is invalid.
     */
    public JSONObject put(String key, double value) throws JSONException {
        put(key, new Double(value));
        return this;
    }


    /**
     * Put a key/int pair in the JSONObject.
     *
     * @param key   A key string.
     * @param value An int which is the value.
     * @return this.
     * @throws JSONException If the key is null.
     */
    public JSONObject put(String key, int value) throws JSONException {
        put(key, new Integer(value));
        return this;
    }


    /**
     * Put a key/long pair in the JSONObject.
     *
     * @param key   A key string.
     * @param value A long which is the value.
     * @return this.
     * @throws JSONException If the key is null.
     */
    public JSONObject put(String key, long value) throws JSONException {
        put(key, new Long(value));
        return this;
    }


    /**
     * Put a key/value pair in the JSONObject, where the value will be a
     * JSONObject which is produced from a Map.
     * @param key   A key string.
     * @param value A Map value.
     * @return      this.
     * @throws JSONException
     */
    public JSONObject put(String key, Map<?, ?> value) throws JSONException {
        put(key, new JSONObject(value));
        return this;
    }


    /**
     * Put a key/value pair in the JSONObject. If the value is null,
     * then the key will be removed from the JSONObject if it is present.
     * @param key   A key string.
     * @param value An object which is the value. It should be of one of these
     *  types: Boolean, Double, Integer, JSONArray, JSONObject, Long, String,
     *  or the JSONObject.NULL object.
     * @return this.
     * @throws JSONException If the value is non-finite number
     *  or if the key is null.
     */
    public JSONObject put(String key, Object value) throws JSONException {
        if (key == null) {
            throw new JSONException("Null key.");
        }
        if (value != null) {
            testValidity(value);
            this.map.put(key, value);
        } else {
            remove(key);
        }
        return this;
    }


    /**
     * Put a key/value pair in the JSONObject, but only if the key and the
     * value are both non-null, and only if there is not already a member
     * with that name.
     * @param key
     * @param value
     * @return his.
     * @throws JSONException if the key is a duplicate
     */
    public JSONObject putOnce(String key, Object value) throws JSONException {
        if (key != null && value != null) {
            if (opt(key) != null) {
                throw new JSONException("Duplicate key \"" + key + "\"");
            }
            put(key, value);
        }
        return this;
    }


    /**
     * Put a key/value pair in the JSONObject, but only if the
     * key and the value are both non-null.
     * @param key   A key string.
     * @param value An object which is the value. It should be of one of these
     *  types: Boolean, Double, Integer, JSONArray, JSONObject, Long, String,
     *  or the JSONObject.NULL object.
     * @return this.
     * @throws JSONException If the value is a non-finite number.
     */
    public JSONObject putOpt(String key, Object value) throws JSONException {
        if (key != null && value != null) {
            put(key, value);
        }
        return this;
    }


    /**
     * Produce a string in double quotes with backslash sequences in all the
     * right places. A backslash will be inserted within </, allowing JSON
     * text to be delivered in HTML. In JSON text, a string cannot contain a
     * control character or an unescaped quote or backslash.
     * @param string A String
     * @return  A String correctly formatted for insertion in a JSON text.
     */
    public static String quote(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char         b;
        char         c = 0;
        int          i;
        int          len = string.length();
        StringBuffer sb = new StringBuffer(len + 4);
        String       t;

        sb.append('"');
        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
            case '\\':
            case '"':
                sb.append('\\');
                sb.append(c);
                break;
            case '/':
                if (b == '<') {
                    sb.append('\\');
                }
                sb.append(c);
                break;
            case '\b':
                sb.append("\\b");
                break;
            case '\t':
                sb.append("\\t");
                break;
            case '\n':
                sb.append("\\n");
                break;
            case '\f':
                sb.append("\\f");
                break;
            case '\r':
                sb.append("\\r");
                break;
            default:
                if (c < ' ' || (c >= '\u0080' && c < '\u00a0') ||
                               (c >= '\u2000' && c < '\u2100')) {
                    t = "000" + Integer.toHexString(c);
                    sb.append("\\u" + t.substring(t.length() - 4));
                } else {
                    sb.append(c);
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    /**
     * Remove a name and its value, if present.
     * @param key The name to be removed.
     * @return The value that was associated with the name,
     * or null if there was no value.
     */
    public Object remove(String key) {
        return this.map.remove(key);
    }

    /**
     * Get an enumeration of the keys of the JSONObject.
     * The keys will be sorted alphabetically.
     *
     * @return An iterator of the keys.
     */
    public Iterator<?> sortedKeys() {
      return new TreeSet<Object>(this.map.keySet()).iterator();
    }

    /**
     * Try to convert a string into a number, boolean, or null. If the string
     * can't be converted, return the string.
     * @param s A String.
     * @return A simple JSON value.
     */
    static public Object stringToValue(String s) {
        if (s.equals("")) {
            return s;
        }
        if (s.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (s.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        if (s.equalsIgnoreCase("null")) {
            return JSONObject.NULL;
        }

        /*
         * If it might be a number, try converting it. We support the 0- and 0x-
         * conventions. If a number cannot be produced, then the value will just
         * be a string. Note that the 0-, 0x-, plus, and implied string
         * conventions are non-standard. A JSON parser is free to accept
         * non-JSON forms as long as it accepts all correct JSON forms.
         */

        char b = s.charAt(0);
        if ((b >= '0' && b <= '9') || b == '.' || b == '-' || b == '+') {
            if (b == '0') {
                if (s.length() > 2 &&
                        (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
                    try {
                        return new Integer(Integer.parseInt(s.substring(2),
                                16));
                    } catch (Exception e) {
                        /* Ignore the error */
                    }
                } else {
                    try {
                        return new Integer(Integer.parseInt(s, 8));
                    } catch (Exception e) {
                        /* Ignore the error */
                    }
                }
            }
            try {
                if (s.indexOf('.') > -1 || s.indexOf('e') > -1 || s.indexOf('E') > -1) {
                    return Double.valueOf(s);
                }
				Long myLong = new Long(s);
				if (myLong.longValue() == myLong.intValue()) {
				    return new Integer(myLong.intValue());
				}
				return myLong;
            }  catch (Exception f) {
                /* Ignore the error */
            }
        }
        return s;
    }


    /**
     * Throw an exception if the object is an NaN or infinite number.
     * @param o The object to test.
     * @throws JSONException If o is a non-finite number.
     */
    static void testValidity(Object o) throws JSONException {
        if (o != null) {
            if (o instanceof Double) {
                if (((Double)o).isInfinite() || ((Double)o).isNaN()) {
                    throw new JSONException(
                        "JSON does not allow non-finite numbers.");
                }
            } else if (o instanceof Float) {
                if (((Float)o).isInfinite() || ((Float)o).isNaN()) {
                    throw new JSONException(
                        "JSON does not allow non-finite numbers.");
                }
            }
        }
    }


    /**
     * Produce a JSONArray containing the values of the members of this
     * JSONObject.
     * @param names A JSONArray containing a list of key strings. This
     * determines the sequence of the values in the result.
     * @return A JSONArray of values.
     * @throws JSONException If any of the values are non-finite numbers.
     */
    public JSONArray toJSONArray(JSONArray names) throws JSONException {
        if (names == null || names.length() == 0) {
            return null;
        }
        JSONArray ja = new JSONArray();
        for (int i = 0; i < names.length(); i += 1) {
            ja.put(this.opt(names.getString(i)));
        }
        return ja;
    }

    /**
     * Make a JSON text of this JSONObject. For compactness, no whitespace
     * is added. If this would not result in a syntactically correct JSON text,
     * then null will be returned instead.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @return a printable, displayable, portable, transmittable
     *  representation of the object, beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>.
     */
    @Override
	public String toString() {
        try {
            Iterator<?>     keys = keys();
            StringBuffer sb = new StringBuffer("{");

            while (keys.hasNext()) {
                if (sb.length() > 1) {
                    sb.append(',');
                }
                Object o = keys.next();
                sb.append(quote(o.toString()));
                sb.append(':');
                sb.append(valueToString(this.map.get(o)));
            }
            sb.append('}');
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Make a prettyprinted JSON text of this JSONObject.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     * @param indentFactor The number of spaces to add to each level of
     *  indentation.
     * @return a printable, displayable, portable, transmittable
     *  representation of the object, beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws JSONException If the object contains an invalid number.
     */
    public String toString(int indentFactor) throws JSONException {
        return toString(indentFactor, 0);
    }


    /**
     * Make a prettyprinted JSON text of this JSONObject.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     * @param indentFactor The number of spaces to add to each level of
     *  indentation.
     * @param indent The indentation of the top level.
     * @return a printable, displayable, transmittable
     *  representation of the object, beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws JSONException If the object contains an invalid number.
     */
    String toString(int indentFactor, int indent) throws JSONException {
        int j;
        int n = length();
        if (n == 0) {
            return "{}";
        }
        Iterator<?>     keys = sortedKeys();
        StringBuffer sb = new StringBuffer("{");
        int          newindent = indent + indentFactor;
        Object       o;
        if (n == 1) {
            o = keys.next();
            sb.append(quote(o.toString()));
            sb.append(": ");
            sb.append(valueToString(this.map.get(o), indentFactor,
                    indent));
        } else {
            while (keys.hasNext()) {
                o = keys.next();
                if (sb.length() > 1) {
                    sb.append(",\n");
                } else {
                    sb.append('\n');
                }
                for (j = 0; j < newindent; j += 1) {
                    sb.append(' ');
                }
                sb.append(quote(o.toString()));
                sb.append(": ");
                sb.append(valueToString(this.map.get(o), indentFactor,
                        newindent));
            }
            if (sb.length() > 1) {
                sb.append('\n');
                for (j = 0; j < indent; j += 1) {
                    sb.append(' ');
                }
            }
        }
        sb.append('}');
        return sb.toString();
    }


    /**
     * Make a JSON text of an Object value. If the object has an
     * value.toJSONString() method, then that method will be used to produce
     * the JSON text. The method is required to produce a strictly
     * conforming text. If the object does not contain a toJSONString
     * method (which is the most common case), then a text will be
     * produced by other means. If the value is an array or Collection,
     * then a JSONArray will be made from it and its toJSONString method
     * will be called. If the value is a MAP, then a JSONObject will be made
     * from it and its toJSONString method will be called. Otherwise, the
     * value's toString method will be called, and the result will be quoted.
     *
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     * @param value The value to be serialized.
     * @return a printable, displayable, transmittable
     *  representation of the object, beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws JSONException If the value is or contains an invalid number.
     */
    @SuppressWarnings("cast")
	static String valueToString(Object value) throws JSONException {
        if (value == null || value.equals(null)) {
            return "null";
        }
        if (value instanceof JSONString) {
            Object o;
            try {
                o = ((JSONString)value).toJSONString();
            } catch (Exception e) {
                throw new JSONException(e);
            }
            if (o instanceof String) {
                return (String)o;
            }
            throw new JSONException("Bad value from toJSONString: " + o);
        }
        if (value instanceof Number) {
            return numberToString((Number) value);
        }
        if (value instanceof Boolean || value instanceof JSONObject ||
                value instanceof JSONArray) {
            return value.toString();
        }
        if (value instanceof Map<?, ?>) {
            return new JSONObject((Map<?, ?>)value).toString();
        }
        if (value instanceof Collection<?>) {
            return new JSONArray((Collection<?>) value).toString();
        }
        if (value.getClass().isArray()) {
            return new JSONArray(value).toString();
        }
        return quote(value.toString());
    }


    /**
     * Make a prettyprinted JSON text of an object value.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     * @param value The value to be serialized.
     * @param indentFactor The number of spaces to add to each level of
     *  indentation.
     * @param indent The indentation of the top level.
     * @return a printable, displayable, transmittable
     *  representation of the object, beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws JSONException If the object contains an invalid number.
     */
     @SuppressWarnings("cast")
	static String valueToString(Object value, int indentFactor, int indent)
            throws JSONException {
        if (value == null || value.equals(null)) {
            return "null";
        }
        try {
            if (value instanceof JSONString) {
                Object o = ((JSONString)value).toJSONString();
                if (o instanceof String) {
                    return (String)o;
                }
            }
        } catch (Exception e) {
            /* forget about it */
        }
        if (value instanceof Number) {
            return numberToString((Number) value);
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof JSONObject) {
            return ((JSONObject)value).toString(indentFactor, indent);
        }
        if (value instanceof JSONArray) {
            return ((JSONArray)value).toString(indentFactor, indent);
        }
        if (value instanceof Map<?, ?>) {
            return new JSONObject((Map<?, ?>)value).toString(indentFactor, indent);
        }
        if (value instanceof Collection<?>) {
            return new JSONArray((Collection<?>) value).toString(indentFactor, indent);
        }
        if (value.getClass().isArray()) {
            return new JSONArray(value).toString(indentFactor, indent);
        }
        return quote(value.toString());
    }


     /**
      * Write the contents of the JSONObject as JSON text to a writer.
      * For compactness, no whitespace is added.
      * <p>
      * Warning: This method assumes that the data structure is acyclical.
      *
      * @return The writer.
      * @throws JSONException
      */
     public Writer write(Writer writer) throws JSONException {
        try {
            boolean  b = false;
            Iterator<?> keys = keys();
            writer.write('{');

            while (keys.hasNext()) {
                if (b) {
                    writer.write(',');
                }
                Object k = keys.next();
                writer.write(quote(k.toString()));
                writer.write(':');
                Object v = this.map.get(k);
                if (v instanceof JSONObject) {
                    ((JSONObject)v).write(writer);
                } else if (v instanceof JSONArray) {
                    ((JSONArray)v).write(writer);
                } else {
                    writer.write(valueToString(v));
                }
                b = true;
            }
            writer.write('}');
            return writer;
        } catch (IOException e) {
            throw new JSONException(e);
        }
     }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163
