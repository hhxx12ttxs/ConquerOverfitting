<<<<<<< HEAD
/*
 * Copyright 2004-2010 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.value;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import org.h2.constant.ErrorCode;
import org.h2.constant.SysProperties;
import org.h2.message.DbException;
import org.h2.store.DataHandler;
import org.h2.store.LobStorage;
import org.h2.tools.SimpleResultSet;
import org.h2.util.IOUtils;
import org.h2.util.StringUtils;
import org.h2.util.Utils;

/**
 * This is the base class for all value classes.
 * It provides conversion and comparison methods.
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
     * The value type for INT values.
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
     * The number of value types.
     */
    public static final int TYPE_COUNT = STRING_FIXED + 1;

    private static SoftReference<Value[]> softCache = new SoftReference<Value[]>(null);
    private static final BigDecimal MAX_LONG_DECIMAL = BigDecimal.valueOf(Long.MAX_VALUE);
    private static final BigDecimal MIN_LONG_DECIMAL = BigDecimal.valueOf(Long.MIN_VALUE);

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
    public abstract void set(PreparedStatement prep, int parameterIndex) throws SQLException;

    /**
     * Compare the value with another value of the same type.
     *
     * @param v the other value
     * @param mode the compare mode
     * @return 0 if both values are equal, -1 if the other value is smaller, and
     *         1 otherwise
     */
    protected abstract int compareSecure(Value v, CompareMode mode);

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
        if (t1 == t2) {
            if (t1 == Value.UNKNOWN) {
                throw DbException.get(ErrorCode.UNKNOWN_DATA_TYPE_1, "?, ?");
            }
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
            // cacheCleaner = (cacheCleaner + 1) & (Constants.OBJECT_CACHE_SIZE - 1);
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

    public Date getDateNoCopy() {
        return ((ValueDate) convertTo(Value.DATE)).getDateNoCopy();
    }

    public Time getTime() {
        return ((ValueTime) convertTo(Value.TIME)).getTime();
    }

    public Time getTimeNoCopy() {
        return ((ValueTime) convertTo(Value.TIME)).getTimeNoCopy();
    }

    public Timestamp getTimestamp() {
        return ((ValueTimestamp) convertTo(Value.TIMESTAMP)).getTimestamp();
    }

    public Timestamp getTimestampNoCopy() {
        return ((ValueTimestamp) convertTo(Value.TIMESTAMP)).getTimestampNoCopy();
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
        return IOUtils.getReader(getString());
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
                    throw DbException.get(ErrorCode.DATA_CONVERSION_ERROR_1, getString());
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
                // convert to string is required for JDK 1.4
                switch (getType()) {
                case BOOLEAN:
                    return ValueDecimal.get(BigDecimal.valueOf(getBoolean().booleanValue() ? 1 : 0));
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
                        throw DbException.get(ErrorCode.DATA_CONVERSION_ERROR_1, "" + d);
                    }
                    return ValueDecimal.get(BigDecimal.valueOf(d));
                }
                case FLOAT: {
                    float f = getFloat();
                    if (Float.isInfinite(f) || Float.isNaN(f)) {
                        throw DbException.get(ErrorCode.DATA_CONVERSION_ERROR_1, "" + f);
                    }
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
                    return ValueDate.get(new Date(getTimeNoCopy().getTime()));
                case TIMESTAMP:
                    return ValueDate.get(new Date(getTimestampNoCopy().getTime()));
                }
                break;
            }
            case TIME: {
                switch (getType()) {
                case DATE:
                    // need to normalize the year, month and day
                    return ValueTime.get(new Time(getDateNoCopy().getTime()));
                case TIMESTAMP:
                    // need to normalize the year, month and day
                    return ValueTime.get(new Time(getTimestampNoCopy().getTime()));
                }
                break;
            }
            case TIMESTAMP: {
                switch (getType()) {
                case TIME:
                    return ValueTimestamp.getNoCopy(new Timestamp(getTimeNoCopy().getTime()));
                case DATE:
                    return ValueTimestamp.getNoCopy(new Timestamp(getDateNoCopy().getTime()));
                }
                break;
            }
            case BYTES: {
                switch(getType()) {
                case JAVA_OBJECT:
                case BLOB:
                    return ValueBytes.getNoCopy(getBytesNoCopy());
                case UUID:
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
                    return ValueJavaObject.getNoCopy(getBytesNoCopy());
                }
                break;
            }
            case BLOB: {
                switch(getType()) {
                case BYTES:

                    return LobStorage.createSmallLob(Value.BLOB, getBytesNoCopy());
                }
                break;
            }
            case UUID: {
                switch(getType()) {
                case BYTES:
                    return ValueUuid.get(getBytesNoCopy());
                }
            }
            }
            // conversion by parsing the string value
            String s = getString();
            switch (targetType) {
            case NULL:
                return ValueNull.INSTANCE;
            case BOOLEAN: {
                if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("t") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("y")) {
                    return ValueBoolean.get(true);
                } else if (s.equalsIgnoreCase("false") || s.equalsIgnoreCase("f") || s.equalsIgnoreCase("no") || s.equalsIgnoreCase("n")) {
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
                return ValueTime.getNoCopy(ValueTime.parseTime(s.trim()));
            case DATE:
                return ValueDate.getNoCopy(ValueDate.parseDate(s.trim()));
            case TIMESTAMP:
                return ValueTimestamp.getNoCopy(ValueTimestamp.parseTimestamp(s.trim()));
            case BYTES:
                return ValueBytes.getNoCopy(StringUtils.convertStringToBytes(s.trim()));
            case JAVA_OBJECT:
                return ValueJavaObject.getNoCopy(StringUtils.convertStringToBytes(s.trim()));
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
                return LobStorage.createSmallLob(CLOB, StringUtils.utf8Encode(s));
            case BLOB:
                return LobStorage.createSmallLob(BLOB, StringUtils.convertStringToBytes(s.trim()));
            case ARRAY:
                return ValueArray.get(new Value[]{ValueString.get(s)});
            case RESULT_SET: {
                SimpleResultSet rs = new SimpleResultSet();
                rs.addColumn("X", Types.VARCHAR, s.length(), 0);
                rs.addRow(s);
                return ValueResultSet.get(rs);
            }
            case UUID:
                return ValueUuid.get(s);
            default:
                throw DbException.throwInternalError("type=" + targetType);
            }
        } catch (NumberFormatException e) {
            throw DbException.get(ErrorCode.DATA_CONVERSION_ERROR_1, e, getString());
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
     * @throws SQLException
     */
    public Value convertScale(boolean onlyToSmallerScale, int targetScale) {
        return this;
    }

    /**
     * Convert the precision to the requested value.
     *
     * @param precision the new precision
     * @return the new value
     * @throws SQLException
     */
    public Value convertPrecision(long precision) {
        return this;
    }

    private byte convertToByte(long x) {
        if (x > Byte.MAX_VALUE || x < Byte.MIN_VALUE) {
            throw DbException.get(ErrorCode.NUMERIC_VALUE_OUT_OF_RANGE);
        }
        return (byte) x;
    }

    private short convertToShort(long x) {
        if (x > Short.MAX_VALUE || x < Short.MIN_VALUE) {
            throw DbException.get(ErrorCode.NUMERIC_VALUE_OUT_OF_RANGE);
        }
        return (short) x;
    }

    private int convertToInt(long x) {
        if (x > Integer.MAX_VALUE || x < Integer.MIN_VALUE) {
            throw DbException.get(ErrorCode.NUMERIC_VALUE_OUT_OF_RANGE);
        }
        return (int) x;
    }

    private long convertToLong(double x) {
        if (x > Long.MAX_VALUE || x < Long.MIN_VALUE) {
            // TODO document that +Infinity, -Infinity throw an exception and NaN returns 0
            throw DbException.get(ErrorCode.NUMERIC_VALUE_OUT_OF_RANGE);
        }
        return Math.round(x);
    }

    private long convertToLong(BigDecimal x) {
        if (x.compareTo(MAX_LONG_DECIMAL) > 0 || x.compareTo(Value.MIN_LONG_DECIMAL) < 0) {
            throw DbException.get(ErrorCode.NUMERIC_VALUE_OUT_OF_RANGE);
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
     * @throws SQLException
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
     * @throws SQLException
     */
    public void unlink() {
        // nothing to do
    }

    /**
     * Check if this value is stored in it's own file. For values that are
     * kept fully in memory, this method returns false.
     *
     * @return true if it is
     */
    public boolean isFileBased() {
        return false;
    }

    /**
     * Close the underlying resource, if any. For values that are kept fully in
     * memory this method has no effect.
     *
     * @throws SQLException
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
     * Get a medium size SQL expression for debugging or tracing. If the precision is
     * too large, only a subset of the value is returned.
     *
     * @return the SQL expression
     */
    public String getTraceSQL() {
        return getSQL();
    }

    public String toString() {
        return getTraceSQL();
    }

    /**
     * Throw the exception that the feature is not support for the given data type.
     *
     * @return never returns normally
     * @throws the exception
     */
    protected DbException throwUnsupportedExceptionForType(String op) {
        throw DbException.getUnsupportedException(DataType.getDataType(getType()).name + " " + op);
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
package ncsa.d2k.modules.core.vis.widgets;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.*;
import java.util.*;
import java.text.*;
import java.awt.image.*;
import javax.swing.table.*;
import java.io.*;

import ncsa.d2k.userviews.swing.*;
import ncsa.d2k.modules.core.datatype.table.*;
import ncsa.d2k.modules.core.datatype.table.basic.Column;
import ncsa.d2k.core.modules.*;
import ncsa.d2k.gui.*;
import ncsa.gui.*;

/**
 *
 */
public class PCView extends JUserPane implements ActionListener, Printable {
	/**
	 * Generate an image of the text label in the given font rotated 90degrees.
	 * @param font the text font.
	 * @param text the text to rotate.
	 * @param fg the foreground color.
	 * @param bg the background color.
	 * @param c the component.
	 */
	static final public Image generateRotatedTextImage (Font font, String text, Color fg, Color bg, Component c) {
		
		// get a buffered image, draw the text into it.
		BufferedImage bi = new BufferedImage (1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics ();
		((Graphics2D) g).setRenderingHint (RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
				
		g.setFont (font);
		FontMetrics fm = g.getFontMetrics (font);
		Rectangle2D bounds = fm.getStringBounds (text, g);
		bi.flush ();
		int width = (int) Math.ceil (bounds.getWidth ());

		//int width = fm.stringWidth (title);
		int height = fm.getHeight ();

		// Get tthe tranformation and create the image to rotate.
		Image origImg = new BufferedImage (width, height, BufferedImage.TYPE_INT_RGB);		
		Graphics2D g2 = (Graphics2D) origImg.getGraphics ();
		g2.setFont (font);

		// make the background brighter if this is selected or active
		g2.setColor (bg);
		g2.fillRect (0, 0, width, height);
		g2.setColor (fg);
		g2.setRenderingHint (RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.drawString (text, 0, fm.getAscent ());

		// rotate the image.
		Image timage = new BufferedImage (height, width, BufferedImage.TYPE_INT_RGB);
		AffineTransform af = AffineTransform.getRotateInstance ((3.0*Math.PI) / 2.0);
		af.translate (-width, 0);//-height);
		g2 = (Graphics2D) timage.getGraphics ();
		g2.setColor (bg);
		g2.fillRect (0, 0, height, width);
		g2.drawImage (origImg, af, c);
		origImg.flush ();
		return timage;
	}
	/** The length of the line used for the gradient paint */
	private static final int LINE_LENGTH = 250;

	/** color wheel */
	private static final Color[] colors =
		{
			new Color(71, 74, 98),
			new Color(191, 191, 115),
			new Color(111, 142, 116),
			new Color(178, 198, 181),
			new Color(153, 185, 216),
			new Color(96, 93, 71),
			new Color(146, 205, 163),
			new Color(203, 84, 84),
			new Color(217, 183, 170),
			new Color(140, 54, 57),
			new Color(203, 136, 76)};

	private static final String GREATER_THAN = ">";
	private static final String LESS_THAN = "<";
	private static final String GREATER_THAN_EQUAL_TO = ">=";
	private static final String LESS_THAN_EQUAL_TO = "<=";
	private static final String NOT_EQUAL_TO = "!=";
	private static final String EQUAL_TO = "==";

	private static final int MAX_MENU_ITEMS = 15;
	private static final String MORE = "More..";
	private static final int BAR_WIDTH = 20;
	private static final int HALF_WIDTH = 10;
	private static final String HIGH = "High";
	private static final String LOW = "Low";

	private static final Color defaultHighColor = Color.red; //colors[0];
	private static final Color defaultLowColor = Color.yellow;
	//colors[colors.length-1];
	private static final Color yellowish = new Color(255, 255, 240);
	private static final Color grayish = new Color(219, 217, 206);
	private static final float TOLERANCE = 2.005f;

	private static final String zoomicon =
		File.separator + "images" + File.separator + "zoom.gif";
	private static final String refreshicon =
		File.separator + "images" + File.separator + "home.gif";
	private static final String printicon =
		File.separator + "images" + File.separator + "printit.gif";
	private static final String tableicon =
		File.separator + "images" + File.separator + "table.gif";
	private static final String filtericon =
		File.separator + "images" + File.separator + "filter.gif";
	private static final String helpicon =
		File.separator + "images" + File.separator + "help.gif";
	private static final String EMPTY_STRING = " ";

	private static final Dimension buttonsize = new Dimension(22, 22);

	private static final HashMap uniqueValues(Table t, int i) {
		HashMap hm = new HashMap();
		for (int k = 0; k < t.getNumRows(); k++) {
			String s = t.getString(k, i);
			if (!hm.containsKey(s)) {
				int sz = hm.size();
				sz++;
				hm.put(s, new Integer(sz));
			}
		}
		return hm;
	}

	/** the indices of the columns to show */
	private volatile int[] columnorder;
	/** the locations of the columns */
	private volatile float[] columnlocations;

	private int imagewidth;

	private boolean zoomin;

	/** the table with the data */
	private Table table;
	private JMenuBar menuBar;

	/** the choose colors menu */
	private JMenu chooseColors;
	/** the area that shows the lines */
	private MainArea ma;

	private JButton refreshButton;
	private JButton filterButton;
	private JToggleButton zoom;
	private JButton showTable;
	private JButton printButton;
	private JButton helpButton;
	private JCheckBoxMenuItem useAntialias;
	private JMenuItem miClearSelected;
	private ColorMenuItem miSelectedColor;
	private Color selectedColor = Color.red;
	private JMenuItem helpItem;

	private int currentKeyColumn;

	private Filter filter;
	private JMenuItem miFilter;
	private JMenuItem miShowTable;
	private JMenuItem miPrint;

	private Color highColor;
	private Color lowColor;
	private HashMap[] colorsLookup;
	/** true if the row should be drawn */
	private boolean[] linemap;
	private boolean[] selectedlines;
	private InfoTableModel infoModel;
	private JTable infoTable;
	private Legend legend;

	private double[] mins;
	private double[] maxes;
	private ViewModule module;
	private JLabel llabel;
	
	public PCView() {
		menuBar = new JMenuBar();
	}

	public PCView(ViewModule mod, Table tbl) {
		menuBar = new JMenuBar();
		initView(mod);
		setInput(tbl, 0);
	}

	public void initView(ViewModule m) {
		module = m;
	}

	private NumberFormat nf;
	private Dimension defaultSize;
	private JScrollPane jsp;

	private HelpWindow helpWindow;

	/**
	 * Input arrived
	 */
	public void setInput(Object o, int i) {
		table = (Table) o;
		selectedlines = new boolean[table.getNumRows()];
		legend = new Legend();
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);

		// create the menus
		JMenu opt = new JMenu("Options");
		JMenu helpMenu = new JMenu("Help");
		helpItem = new JMenuItem("About ParallelCoordinateVis..");
		helpItem.addActionListener(this);
		helpMenu.add(helpItem);
		menuBar.add(opt);
		menuBar.add(helpMenu);
		JMenu displaycols = new JMenu("Display Columns");

		HashSet displayedcols = new HashSet();
		if (table instanceof ExampleTable) {
			int[] inputs = ((ExampleTable) table).getInputFeatures();
			int[] outputs = ((ExampleTable) table).getOutputFeatures();

			for (int j = 0; j < inputs.length; j++)
				displayedcols.add(new Integer(inputs[j]));
			for (int j = 0; j < outputs.length; j++)
				displayedcols.add(new Integer(outputs[j]));
		}

		int numItems = 0;
		JMenu curMenu = displaycols;
		for (int j = 0; j < table.getNumColumns(); j++) {
			DisplayColumnMenuItem dcmi =
				new DisplayColumnMenuItem(table.getColumnLabel(j), j);
			dcmi.addActionListener(this);
			dcmi.setSelected(true);
			if (table instanceof ExampleTable) {
				if (!displayedcols.contains(new Integer(j)))
					dcmi.setSelected(false);
			}
			if (numItems == MAX_MENU_ITEMS) {
				JMenu nextMenu = new JMenu(MORE);
				curMenu.insert(nextMenu, 0);
				nextMenu.add(dcmi);
				curMenu = nextMenu;
				numItems = 1;
			} else {
				curMenu.add(dcmi);
				numItems++;
			}
		}
		opt.add(displaycols);

		int keycol = table.getNumColumns() - 1;
		JMenu keyMenu = new JMenu("Key Column");
		curMenu = keyMenu;
		numItems = 0;
		ButtonGroup bg = new ButtonGroup();
		for (int j = 0; j < table.getNumColumns(); j++) {
			KeyColumnMenuItem dcmi =
				new KeyColumnMenuItem(table.getColumnLabel(j), j);
			if (j == keycol)
				dcmi.setSelected(true);
			bg.add(dcmi);
			dcmi.addActionListener(this);
			if (numItems == MAX_MENU_ITEMS) {
				JMenu nextMenu = new JMenu(MORE);
				curMenu.insert(nextMenu, 0);
				nextMenu.add(dcmi);
				curMenu = nextMenu;
				numItems = 1;
			} else {
				curMenu.add(dcmi);
				numItems++;
			}
		}
		opt.add(keyMenu);

		chooseColors = new JMenu("Choose Colors");
		opt.add(chooseColors);
		ImageIcon hi =
			new ImageIcon(new ColorComponent(selectedColor).getImage());
		miSelectedColor =
			new ColorMenuItem("Selected Line Color", hi, selectedColor);
		miSelectedColor.addActionListener(this);
		opt.add(miSelectedColor);
		opt.addSeparator();
		useAntialias = new JCheckBoxMenuItem("Use Antialiasing", false);
		useAntialias.addActionListener(this);
		opt.add(useAntialias);
		//miFilter = new JMenuItem("Filters..");
		//miFilter.addActionListener(this);
		//opt.add(miFilter);
		miClearSelected = new JMenuItem("Clear Selected Lines");
		miClearSelected.addActionListener(this);
		opt.add(miClearSelected);
		//miShowTable = new JMenuItem("Show Table");
		//miShowTable.addActionListener(this);
		//opt.add(miShowTable);
		miPrint = new JMenuItem("Print..");
		miPrint.addActionListener(this);
		opt.add(miPrint);

		ma = new MainArea(this);
		ma.setKeyColumn(keycol);
		ma.setPreferredSize(new Dimension(500, 400));
		jsp = new JScrollPane(ma);
		//jsp.setPreferredSize(new Dimension(500, 400));

		Image im = module.getImage(filtericon);
		ImageIcon icon = null;
		if (im != null)
			icon = new ImageIcon(im);
		if (icon != null) {
			filterButton = new JButton(icon);
			filterButton.setMaximumSize(buttonsize);
			filterButton.setPreferredSize(buttonsize);
		} else
			filterButton = new JButton("F");
		filterButton.addActionListener(this);
		filterButton.setToolTipText("Filter");

		im = null;
		icon = null;
		im = module.getImage(refreshicon);
		if (im != null)
			icon = new ImageIcon(im);
		if (icon != null) {
			refreshButton = new JButton(icon);
			refreshButton.setMaximumSize(buttonsize);
			refreshButton.setPreferredSize(buttonsize);
		} else
			refreshButton = new JButton("R");
		refreshButton.addActionListener(this);
		refreshButton.setToolTipText("Reset View");

		im = null;
		icon = null;
		im = module.getImage(zoomicon);
		if (im != null)
			icon = new ImageIcon(im);
		if (icon != null) {
			zoom = new JToggleButton(icon);
			zoom.setMaximumSize(buttonsize);
			zoom.setPreferredSize(buttonsize);
		} else
			zoom = new JToggleButton("Z");

		zoom.addActionListener(this);
		zoom.setToolTipText("Zoom");

		im = null;
		icon = null;
		im = module.getImage(tableicon);
		if (im != null)
			icon = new ImageIcon(im);
		if (icon != null) {
			showTable = new JButton(icon);
			showTable.setMaximumSize(buttonsize);
			showTable.setPreferredSize(buttonsize);
		} else
			showTable = new JButton("T");
		showTable.addActionListener(this);
		showTable.setToolTipText("Show Table");

		im = null;
		icon = null;
		im = module.getImage(printicon);
		if (im != null)
			icon = new ImageIcon(im);
		if (icon != null) {
			printButton = new JButton(icon);
			printButton.setMaximumSize(buttonsize);
			printButton.setPreferredSize(buttonsize);
		} else
			printButton = new JButton("P");
		printButton.addActionListener(this);
		printButton.setToolTipText("Print");

		im = null;
		icon = null;
		im = module.getImage(helpicon);

		if (im != null)
			icon = new ImageIcon(im);
		if (icon != null) {
			helpButton = new JButton(icon);
			helpButton.setMaximumSize(buttonsize);
			helpButton.setPreferredSize(buttonsize);
		} else
			helpButton = new JButton("H");
		helpButton.addActionListener(this);
		helpButton.setToolTipText("Help");

		JPanel bp = new JPanel();
		bp.setLayout(new GridLayout(1, 6));
		bp.add(refreshButton);
		bp.add(filterButton);
		bp.add(printButton);
		bp.add(zoom);
		bp.add(showTable);
		bp.add(helpButton);

		JPanel bq = new JPanel();
		bq.setLayout(new BoxLayout(bq, BoxLayout.Y_AXIS));
		bq.add(Box.createGlue());
		bq.add(bp);
		bq.add(Box.createGlue());
		JPanel header = new JPanel();
		header.setLayout(new BorderLayout());
		header.add(new JPanel(), BorderLayout.CENTER);
		header.add(bq, BorderLayout.EAST);

		setLayout(new BorderLayout());
		//add(jsp, BorderLayout.CENTER);
		//add(bp, BorderLayout.NORTH);
		infoModel = new InfoTableModel();
		infoTable = new JTable(infoModel);
		JScrollPane tableScroll = new JScrollPane(infoTable);
		tableScroll.setPreferredSize(new Dimension(350, 75));
		//legend.setMinimumSize(new Dimension(100, 75));
		JScrollPane legendScroll = new JScrollPane(legend);
		llabel = new JLabel();
		this.setLegendLabel(table, table.getNumColumns() - 1);
		llabel.setFont(infoTable.getFont());
		JViewport jv = new JViewport();
		jv.setView(llabel);
		legendScroll.setColumnHeader(jv);
		//legendScroll.setPreferredSize(new Dimension(100, 75));

		/*JPanel bk = new JPanel();
		bk.setLayout(new BorderLayout());
		bk.add(tableScroll, BorderLayout.CENTER);
		bk.add(legendScroll, BorderLayout.EAST);
		*/
		JSplitPane split2 =
			new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT,
				tableScroll,
				legendScroll);
		split2.setDividerSize(4);
		//split2.setDividerLocation(.6d);

		JSplitPane split =
			new JSplitPane(JSplitPane.VERTICAL_SPLIT, jsp, split2);
		split.setOneTouchExpandable(true);
		split.setDividerSize(8);
		split.setResizeWeight(1);
		//split.setDividerLocation(.9d);
		//add(ma, BorderLayout.CENTER);
		add(header, BorderLayout.NORTH);
		add(split, BorderLayout.CENTER);
		filter = new Filter(ma);
		helpWindow = new HelpWindow();
	}
	
	/**
	 * Set the legend title to include the name of the key column.
	 * @param table the table with the key column.
	 */
	private void setLegendLabel (Table table, int which) {
		Column kc = table.getColumn(which);
		llabel.setText("Legend : "+kc.getLabel());
	}
	public Object getMenu() {
		return menuBar;
	}

	/**
	   Add a line to the selected set.
	*/
	private final void addSelection(int row) {
		infoModel.addInfoRow(row);
	}

	/**
	   Remove a line from the selected set.
	*/
	private final void removeSelection(int row) {
		infoModel.removeInfoRow(row);
		infoTable.validate();
		//scrollPane.validate();
	}

	/**
	 * Identifies which columns to display
	 */
	private final class DisplayColumnMenuItem extends JCheckBoxMenuItem {
		int id;
		DisplayColumnMenuItem(String s, int i) {
			super(s);
			id = i;
		}
	}

	/**
	 * Identifies which is the key column
	 */
	private final class KeyColumnMenuItem extends JCheckBoxMenuItem {
		int id;
		KeyColumnMenuItem(String s, int i) {
			super(s);
			id = i;
		}
	}

	/**
	 * Identifies which colors to use
	 */
	private final class ColorMenuItem extends JMenuItem {
		Color c;
		ColorMenuItem(String s, Icon i, Color c) {
			super(s, i);
		}
	}

	/**
	 * Action listener for menu items
	 */
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		// update the key column
		if (src instanceof KeyColumnMenuItem) {
			int id = ((KeyColumnMenuItem) src).id;
			this.setLegendLabel(table,id);
			ma.setKeyColumn(id);
			ma.updateImage();
		}
		// update the columns to display
		else if (src instanceof DisplayColumnMenuItem) {
			DisplayColumnMenuItem dcmi = (DisplayColumnMenuItem) src;
			int id = dcmi.id;
			// if selected, add it to the list of columns
			if (dcmi.getState()) {
				int[] newcolorder = new int[columnorder.length + 1];
				float[] newcolloc = new float[columnlocations.length + 1];
				for (int i = 0; i < columnorder.length; i++) {
					newcolorder[i] = columnorder[i];
					newcolloc[i] = columnlocations[i];
				}
				newcolorder[newcolorder.length - 1] = id;
				newcolloc[newcolorder.length - 1] = imagewidth;

				// find a better way to set column locations
				for (int i = 0; i < newcolloc.length - 1; i++) {
					newcolloc[i] *= ((float) columnlocations.length)
						/ ((float) newcolloc.length);
				}
				columnorder = newcolorder;
				columnlocations = newcolloc;
			}

			// if not selected, remove it from the list
			else {
				int[] newcolorder = new int[columnorder.length - 1];
				float[] newcolloc = new float[columnlocations.length - 1];
				int idx = 0;
				for (int i = 0; i < columnorder.length; i++) {
					if (columnorder[i] != id) {
						newcolorder[idx] = columnorder[i];
						newcolloc[idx] = columnlocations[i];
						idx++;
					}
				}
				columnorder = newcolorder;
				columnlocations = newcolloc;
			}
			ma.updateImage();
		}
		// zooming
		else if (src == zoom) {
			if (zoomin)
				zoomin = false;
			else
				zoomin = true;
		}
		// change the colors
		else if (src instanceof ColorMenuItem) {
			ColorMenuItem cmi = (ColorMenuItem) src;
			String text = cmi.getText();
			Color oldColor = cmi.c;
			Color newColor = JColorChooser.showDialog(this, "Choose", oldColor);

			if (cmi == miSelectedColor) {
				ImageIcon hi =
					new ImageIcon(new ColorComponent(newColor).getImage());
				cmi.setIcon(hi);
				selectedColor = newColor;
				ma.updateImage();
				return;
			}
			if (newColor != null) {
				colorsLookup[currentKeyColumn].put(text, newColor);
				ma.setKeyColumn(currentKeyColumn);
				ma.updateImage();
			}
		}
		// antialias the lines
		else if (src == useAntialias) {
			ma.image = null;
			ma.repaint();
			ma.updateImage();
		}
		// show the filter
		else if (src == miFilter || src == filterButton) {
			filter.setState(Frame.NORMAL);
			filter.setVisible(true);
			//ma.updateImage();
		} else if (src == miClearSelected) {
			boolean found = false;
			for (int i = 0; i < selectedlines.length; i++) {
				if (selectedlines[i]) {
					found = true;
					removeSelection(i);
				}
				selectedlines[i] = false;
			}
			// only redraw buffer if we actually cleared any lines
			if (found)
				ma.updateImage();
		} else if (src == miShowTable || src == showTable) {
			JD2KFrame frame = new JD2KFrame("Table");
			TableMatrix vtm = new TableMatrix(table);
			frame.getContentPane().add(vtm);
			frame.addWindowListener(new DisposeOnCloseListener(frame));
			frame.pack();
			frame.show();
		}
		// print
		else if (src == miPrint || src == printButton) {
			PrinterJob pj = PrinterJob.getPrinterJob();
			pj.setPrintable(this);
			if (pj.printDialog()) {
				try {
					pj.print();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} else if (src == refreshButton) {
			int wid = jsp.getWidth();
			int hei = jsp.getHeight();
			ma.setPreferredSize(new Dimension(wid, hei));
			jsp.revalidate();
		} else if (src == helpItem || src == helpButton)
			helpWindow.setVisible(true);
	}

	/**
	 * Print this component.
	 */
	public int print(Graphics g, PageFormat pf, int pi)
		throws PrinterException {

		double pageHeight = pf.getImageableHeight();
		double pageWidth = pf.getImageableWidth();

		double cWidth = getWidth();
		double cHeight = getHeight();

		double scale = 1;
		if (cWidth >= pageWidth)
			scale = pageWidth / cWidth;
		if (cHeight >= pageHeight)
			scale = Math.min(scale, pageHeight / cHeight);

		double cWidthOnPage = cWidth * scale;
		double cHeightOnPage = cHeight * scale;

		if (pi >= 1)
			return Printable.NO_SUCH_PAGE;

		Graphics2D g2 = (Graphics2D) g;
		g2.translate(pf.getImageableX(), pf.getImageableY());
		g2.scale(scale, scale);
		print(g2);
		return Printable.PAGE_EXISTS;
	}

	/**
	 * Draw the lines as a buffered image.  Everything else is painted
	 * directly on this.
	 */
	private final class MainArea
		extends JPanel
		implements MouseListener, MouseMotionListener {
		private PCView app;
		private int selectedcol;
		private boolean mousedrag;
		private int mousex;
		private int mousey;
		private boolean firsttime;

		// the weights of the lines, scaled to between the
		// max and min for a column
		private float[][] weights;
		// the actual heights of the lines
		private float[][] heights;

		// the color of a row
		private Color[] linecolor;

		private int leftoffset;
		private int topoffset;
		private int imageheight;

		private int imagebottom;
		private int barheight;
		private int imagetop;

		private BufferedImage image;

		MainArea(PCView a) {
			app = a;
			firsttime = true;
			mousedrag = false;

			if (!(table instanceof ExampleTable)) {
				columnorder = new int[table.getNumColumns()];
				columnlocations = new float[table.getNumColumns()];
				weights = new float[table.getNumColumns()][table.getNumRows()];
				heights = new float[table.getNumColumns()][table.getNumRows()];
				mins = new double[table.getNumColumns()];
				maxes = new double[table.getNumColumns()];
				for (int j = 0; j < columnorder.length; j++) {
					columnorder[j] = j;
					//Column c = table.getColumn(j);
					//if(c instanceof NumericColumn) {
					if (table.isColumnNumeric(j)) {
						//NumericColumn nc = (NumericColumn)c;
						MaxMin mm = getMaxMin(j);
						//float max = (float)nc.getMax();
						//float min = (float)nc.getMin();
						mins[j] = (float) mm.min;
						maxes[j] = (float) mm.max;
						for (int k = 0; k < table.getNumRows(); k++)
							weights[j][k] =
								((float) maxes[j] - table.getFloat(k, j))
									/ ((float) maxes[j] - (float) mins[j]);
					} else {
						//Column sc = (Column)c;
						HashMap hm = uniqueValues(table, j);
						for (int k = 0; k < table.getNumRows(); k++) {
							Integer ii =
								(Integer) hm.get(table.getString(k, j));
							weights[j][k] = ii.floatValue() / (hm.size() + 1);
						}
					}
				}
			} else {
				ExampleTable et = (ExampleTable) table;
				int[] inputs = et.getInputFeatures();
				int[] outputs = et.getOutputFeatures();

				int idx = 0;
				int[] all = new int[inputs.length + outputs.length];
				for (int i = 0; i < inputs.length; i++) {
					all[idx] = inputs[i];
					idx++;
				}
				for (int i = 0; i < outputs.length; i++) {
					all[idx] = outputs[i];
					idx++;
				}

				columnorder = new int[all.length];
				columnlocations = new float[all.length];
				weights = new float[table.getNumColumns()][table.getNumRows()];
				heights = new float[table.getNumColumns()][table.getNumRows()];
				mins = new double[table.getNumColumns()];
				maxes = new double[table.getNumColumns()];
				for (int j = 0; j < columnorder.length; j++) {
					columnorder[j] = all[j];
				}
				for (int j = 0; j < table.getNumColumns(); j++) {

					//Column c = table.getColumn(j);
					//if(c instanceof NumericColumn) {
					if (table.isColumnNumeric(j)) {
						//NumericColumn nc = (NumericColumn)c;
						MaxMin mm = getMaxMin(j);
						//float max = (float)nc.getMax();
						//float min = (float)nc.getMin();
						mins[j] = (float) mm.min;
						maxes[j] = (float) mm.max;
						for (int k = 0; k < table.getNumRows(); k++)
							weights[j][k] =
								((float) maxes[j] - table.getFloat(k, j))
									/ ((float) maxes[j] - (float) mins[j]);
					} else {
						//Column sc = (Column)c;
						HashMap hm = uniqueValues(table, j);
						for (int k = 0; k < table.getNumRows(); k++) {
							Integer ii =
								(Integer) hm.get(table.getString(k, j));
							weights[j][k] = ii.floatValue() / (hm.size() + 1);
						}
					}
				}

			}
			linemap = new boolean[table.getNumRows()];
			for (int j = 0; j < linemap.length; j++)
				linemap[j] = true;
			linecolor = new Color[table.getNumRows()];
			colorsLookup = new HashMap[table.getNumColumns()];
			addMouseListener(this);
			addMouseMotionListener(this);
		}

		private int oldwidth;
		private int oldheight;
		private int sidebuffer;

		/**
		 * Set the size of this.
		 */
		public void setBounds(int x, int y, int w, int h) {
			int oldimagewidth = imagewidth;

			if (w != oldwidth || h != oldheight) {
				oldwidth = w;
				oldheight = h;
				sidebuffer = (int) (.025 * w);
				leftoffset = (int) (.075 * w);
				topoffset = (int) (.05 * h);
				imagetop = (int) (.1 * h);
				imageheight = (int) (.55 * h);
				imagewidth = (int) (.85 * w);
				imagebottom = imagetop + imageheight + topoffset;
				barheight = (int) (.25 * h);

				float newratio = (float) imagewidth / (float) oldimagewidth;

				if (firsttime) {
					int wid = (int) (imagewidth / (columnlocations.length - 1));
					for (int i = 0; i < columnlocations.length; i++) {
						columnlocations[i] = (float) i * wid;
					}
					firsttime = false;
				}
				// scale the current columnlocations
				else {
					for (int i = 0; i < columnlocations.length; i++)
						columnlocations[i] *= newratio;
				}

				// Recompute all the heights.
				for (int j = 0; j < weights.length; j++) {
					for (int k = 0; k < table.getNumRows(); k++) {
						heights[j][k] = weights[j][k] * imageheight;
					}
				}
				updateImage();
			}

			super.setBounds(x, y, w, h);
		}

		/**
		 * Create a buffered image used for the gradient paint.
		 */
		private final BufferedImage getGradientImage(Color low, Color high) {
			BufferedImage bi =
				new BufferedImage(2, 101, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = (Graphics2D) bi.getGraphics();
			g2.setPaint(new GradientPaint(0, 0, low, 2, 101, high));
			g2.fill(new Rectangle(0, 0, 2, 101));
			return bi;
		}

		/**
		 * Set the column that determines the line colors.
		 */
		private final void setKeyColumn(int i) {
			currentKeyColumn = i;
			// update the linecolors
			//Column c = table.getColumn(i);

			HashMap lookup = colorsLookup[i];
			if (lookup == null)
				lookup = new HashMap();

			//if(c instanceof NumericColumn) {
			if (table.isColumnNumeric(i)) {
				//NumericColumn nc = (NumericColumn)c;
				MaxMin mm = getMaxMin(i);
				double max = mm.max; //nc.getMax();
				double min = mm.min; //nc.getMin();

				Color h = (Color) lookup.get(HIGH);
				if (h == null)
					h = defaultHighColor;
				Color l = (Color) lookup.get(LOW);
				if (l == null)
					l = defaultLowColor;

				// get the high and low color here
				BufferedImage gradient = getGradientImage(l, h);
				for (int j = 0; j < table.getNumRows(); j++) {
					// now create a new GradientPaint
					// based on the shadecolumn get the shader value
					double shaderVal = table.getDouble(j, i);
					// find where the shader value lies between min and max
					double percent = (max - shaderVal) / (max - min);
					Color col =
						new Color(
							gradient.getRGB(
								1,
								100 - (int) Math.abs(percent * 100)));
					linecolor[j] = col;
				}
				chooseColors.removeAll();
				lookup.put(HIGH, h);
				ImageIcon hi = new ImageIcon(new ColorComponent(h).getImage());
				ColorMenuItem hm = new ColorMenuItem(HIGH, hi, h);
				hm.addActionListener(app);
				lookup.put(LOW, l);
				ImageIcon li = new ImageIcon(new ColorComponent(l).getImage());
				ColorMenuItem lm = new ColorMenuItem(LOW, li, l);
				lm.addActionListener(app);
				chooseColors.add(hm);
				chooseColors.add(lm);
			} else {
				//Column sc = (Column)c;

				int idx = 0;
				//linecolor = new Color[table.getNumRows()];
				for (int j = 0; j < table.getNumRows(); j++) {
					String s = table.getString(j, i);
					if (lookup.containsKey(s)) {
						Color col = (Color) lookup.get(s);
						linecolor[j] = col;
					} else {
						lookup.put(s, colors[idx % colors.length]);
						linecolor[j] = colors[idx % colors.length];
						idx++;
					}
				}
				chooseColors.removeAll();
				int numItems = 0;
				JMenu curMenu = chooseColors;
				Iterator iter = lookup.keySet().iterator();
				while (iter.hasNext()) {
					String text = (String) iter.next();
					Color col = (Color) lookup.get(text);
					ImageIcon li =
						new ImageIcon(new ColorComponent(col).getImage());
					ColorMenuItem cmi = new ColorMenuItem(text, li, col);
					cmi.addActionListener(app);
					if (numItems == MAX_MENU_ITEMS) {
						JMenu nextMenu = new JMenu(MORE);
						curMenu.insert(nextMenu, 0);
						nextMenu.add(cmi);
						curMenu = nextMenu;
						numItems = 1;
					} else {
						curMenu.add(cmi);
						numItems++;
					}
				}
			}
			legend.updateLegend(lookup);
			colorsLookup[i] = lookup;
		}

		/**
		 * Redraw the lines.
		 */
		private final void updateImage() {
			image = null;
			new CreateImageThread(this).start();
		}

		/**
		 * Draw the lines on a buffered image in a separate thread
		 * and then paint the image on the screen.
		 */
		private final class CreateImageThread extends Thread {
			private MainArea app;

			CreateImageThread(MainArea a) {
				app = a;
			}

			public void run() {
				BufferedImage img =
					new BufferedImage(
						imagewidth,
						imageheight,
						BufferedImage.TYPE_INT_RGB);
				Graphics2D g2 = (Graphics2D) img.getGraphics();
				if (useAntialias.getState())
					g2.setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setPaint(yellowish);
				g2.fill(new Rectangle(0, 0, imagewidth, imageheight));

				// draw the lines
				for (int i = 0; i < columnorder.length - 1; i++) {
					for (int j = 0; j < table.getNumRows(); j++) {
						if (linemap[j] && !selectedlines[j]) {
							g2.setPaint(linecolor[j]);
							g2.draw(
								new Line2D.Float(
									columnlocations[i],
									heights[columnorder[i]][j],
									columnlocations[i + 1],
									heights[columnorder[i + 1]][j]));
						}
						g2.setPaint(Color.black);
						g2.draw(
							new Line2D.Float(
								columnlocations[i],
								0,
								columnlocations[i],
								imageheight));
					}
				}

				// loop through again to draw the selected lines
				// this is done so that the selected lines will be
				// drawn on top of the other lines
				g2.setPaint(selectedColor);
				for (int i = 0; i < columnorder.length - 1; i++) {
					for (int j = 0; j < table.getNumRows(); j++) {
						if (linemap[j] && selectedlines[j]) {
							g2.draw(
								new Line2D.Float(
									columnlocations[i],
									heights[columnorder[i]][j],
									columnlocations[i + 1],
									heights[columnorder[i + 1]][j]));
						}
					}
				}

				g2.setPaint(Color.black);
				// get the last one
				g2.draw(new Line2D.Float(
						columnlocations[columnlocations.length - 1], 0,
						columnlocations[columnlocations.length - 1], imageheight));
				image = img;
				app.repaint();
			}
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

			g2.setPaint(yellowish);
			g2.fill(new Rectangle(leftoffset - sidebuffer, topoffset, 
					imagewidth + 2 * sidebuffer, imageheight + 2 * topoffset));

			g2.setPaint(Color.black);
			if (image != null)
				g2.drawImage(image, leftoffset, imagetop, this);
			// draw something else while the image is being drawn
			else {
				g2.setPaint(yellowish);
				g2.fill(new Rectangle(leftoffset, imagetop, imagewidth,
						imageheight));
			}

			g2.setPaint(Color.black);
			Font f = g2.getFont();
			Paint oldPaint = g2.getPaint();
			
			for (int i = 0; i < columnlocations.length; i++) {
				drawBar(
					g2,
					columnlocations[i],
					table.getColumnLabel(columnorder[i]));
			}
			g2.setFont(f);
			g2.setPaint(oldPaint);

			if (mousedrag) {
				g2.setPaint(Color.green);
				g2.draw(
					new Line2D.Float(
						columnlocations[selectedcol] + leftoffset,
						topoffset,
						columnlocations[selectedcol] + leftoffset,
						imagebottom));
			}

			g2.setPaint(Color.black);
			FontMetrics fm = g2.getFontMetrics();
			int ascent = fm.getAscent();
			for (int i = 0; i < columnorder.length; i++) {
				//if(table.getColumn(columnorder[i]) instanceof NumericColumn) {
				if (table.isColumnNumeric(columnorder[i])) {
					String mx = nf.format(maxes[columnorder[i]]);
					g2.drawString(
						mx,
						columnlocations[i]
							+ leftoffset
							- fm.stringWidth(mx) / 2,
						2 * topoffset - (topoffset - ascent) / 2);
				}
			}
			for (int i = 0; i < columnorder.length; i++) {
				//if(table.getColumn(columnorder[i]) instanceof NumericColumn) {
				if (table.isColumnNumeric(columnorder[i])) {
					String mx = nf.format(mins[columnorder[i]]);
					g2.drawString(
						mx,
						columnlocations[i]
							+ leftoffset
							- fm.stringWidth(mx) / 2,
						imagebottom - (topoffset - ascent) / 2);
				}
			}
			g2.draw(
				new Rectangle(
					leftoffset - sidebuffer,
					topoffset,
					imagewidth + 2 * sidebuffer,
					imageheight + 2 * topoffset));
			g2.draw(
				new Line2D.Double(
					leftoffset - sidebuffer,
					2 * topoffset,
					leftoffset + imagewidth + sidebuffer,
					2 * topoffset));
			g2.draw(
				new Line2D.Double(
					leftoffset - sidebuffer,
					imagebottom - topoffset,
					leftoffset + imagewidth + sidebuffer,
					imagebottom - topoffset));
		}

		private final void drawBar(
			Graphics2D g2,
			float location,
			String name) {
			int buffer = (int) (.1 * barheight);

			Rectangle r =
				new Rectangle(
					leftoffset + ((int) location - HALF_WIDTH),
					imagebottom + buffer,
					BAR_WIDTH,
					barheight - buffer);
			g2.setPaint(grayish);
			g2.fill(r);
			g2.setPaint(Color.black);
			g2.draw(r);
			Image img = PCView.generateRotatedTextImage(this.getFont(), name, this.getForeground(),
					this.getBackground(), this);
			int y = r.y + 3;
			int x = r.x + ((r.width - img.getWidth(this))/2);
			g2.drawImage(img, x, y, this);
		}

		public void mousePressed(MouseEvent e) {
			int cx = e.getX();
			int cy = e.getY();

			// if it is inside the image, calc location
			if (cx >= leftoffset
				&& cx <= (leftoffset + imagewidth)
				&& cy >= 2 * topoffset
				&& cy <= imagebottom - topoffset)
				calcLocation(cx - leftoffset, cy - 2 * topoffset);

			boolean found = false;
			for (int i = 0; i < columnlocations.length; i++) {

				if (cx >= (columnlocations[i] - HALF_WIDTH + leftoffset)
					&& cx <= (columnlocations[i] + HALF_WIDTH + leftoffset)) {

					if (cy >= imagebottom && cy <= imagebottom + barheight) {
						found = true;
						selectedcol = i;
						break;
					}
				}
			}
			if (found) {
				mousedrag = true;
				mousex = cx;
				mousey = cy;
			} else {
				if (zoomin) {
					if (!e.isMetaDown()) {
						Dimension d = this.getPreferredSize();
						this.setPreferredSize(
							new Dimension(
								(int) (d.width * 1.1),
								(int) (d.height * 1.1)));
						this.revalidate();
					} else {
						Dimension d = this.getPreferredSize();
						this.setPreferredSize(
							new Dimension(
								(int) (d.width * .9),
								(int) (d.height * .9)));
						this.revalidate();
					}
				}
			}
		}

		public void mouseReleased(MouseEvent e) {
			if (mousedrag) {
				updateImage();
				mousedrag = false;
			}
		}
		public void mouseClicked(MouseEvent e) {
		}
		public void mouseEntered(MouseEvent e) {
		}
		public void mouseExited(MouseEvent e) {
		}
		public void mouseMoved(MouseEvent e) {
		}
		public void mouseDragged(MouseEvent e) {
			if (mousedrag) {
				float newloc = e.getX() - leftoffset;
				//if(newloc < leftoffset)
				if (newloc < 0)
					newloc = 0;
				if (newloc + leftoffset > getWidth() - leftoffset)
					newloc = imagewidth - 2;

				// now loop through the columnlocations and swap if necessary

				//moving right
				if (newloc > columnlocations[selectedcol]) {
					for (int i = selectedcol + 1;
						i < columnlocations.length;
						i++) {
						if (newloc > columnlocations[i]) {
							swap(selectedcol, i);
							selectedcol = i;
						}
					}
				}

				// moving left
				else {
					for (int i = selectedcol - 1; i >= 0; i--) {
						if (newloc < columnlocations[i]) {
							swap(selectedcol, i);
							selectedcol = i;
						}
					}
				}

				columnlocations[selectedcol] = newloc;
				repaint();
			}
		}

		private final void swap(int one, int two) {
			int temp = columnorder[one];
			float temploc = columnlocations[one];
			columnorder[one] = columnorder[two];
			columnlocations[one] = columnlocations[two];
			columnorder[two] = temp;
			columnlocations[two] = temploc;
		}

		/**
		 * Calculate the lines that contain the point (x1, y1).
		 * If a line is found, the lines are redrawn and the selected
		 * lines are painted the selected color.
		 */
		private final void calcLocation(float x1, float y1) {
			// find the two columns that this point
			// lies between

			int col1 = -1;
			int col2 = -1;
			// find the two columns that the point lies between
			for (int i = 0; i < columnlocations.length - 1; i++) {
				if (columnlocations[i] <= x1 && columnlocations[i + 1] >= x1) {
					col1 = i;
					col2 = i + 1;
					break;
				}
			}

			boolean found = false;
			if (col1 != -1 && col2 != -1) {
				float xl = columnlocations[col1];
				float xr = columnlocations[col2];
				for (int i = 0; i < table.getNumRows(); i++) {
					float yl = heights[columnorder[col1]][i];
					float yr = heights[columnorder[col2]][i];

					if ((y1 < (yl + TOLERANCE))
						&& (y1 > (yr - TOLERANCE))
						|| (y1 < (yr + TOLERANCE))
						&& (y1 > (yl - TOLERANCE))) {

						float m = (yr - yl) / (xr - xl);
						float b = yr - m * xr;
						if (Math.abs((y1 - (m * x1) - b)) <= TOLERANCE
							&& linemap[i]) {
							selectedlines[i] = !selectedlines[i];
							if (selectedlines[i])
								addSelection(i);
							else
								removeSelection(i);
							found = true;
						}
					}
				}
			}
			if (found)
				ma.updateImage();
		}
	}

	/**
	 * A small square with a black outline.  The color of the
	 * square is given in the constructor.
	 */
	private final class ColorComponent extends JComponent {
		private final int DIM = 12;
		Color bkgrd;

		ColorComponent(Color c) {
			super();
			setOpaque(true);
			bkgrd = c;
		}

		public Dimension getPreferredSize() {
			return new Dimension(DIM, DIM);
		}

		public Dimension getMinimumSize() {
			return new Dimension(DIM, DIM);
		}

		public void paint(Graphics g) {
			g.setColor(bkgrd);
			g.fillRect(0, 0, DIM - 1, DIM - 1);
			g.setColor(Color.black);
			g.drawRect(0, 0, DIM - 1, DIM - 1);
		}

		void setBkgrd(Color c) {
			bkgrd = c;
		}

		BufferedImage getImage() {
			BufferedImage image =
				new BufferedImage(DIM, DIM, BufferedImage.TYPE_INT_RGB);
			Graphics g = image.getGraphics();
			paint(g);
			return image;
		}
	}

	/**
	 * Filtering lines.
	 */
	private final class Filter extends JD2KFrame implements ActionListener {
		private HashMap numericColumnLookup;
		private HashMap stringColumnLookup;

		private JComboBox numColumns;
		private JComboBox strColumns;
		private JComboBox numOps;
		private JComboBox strOps;
		private JTextField numValue;
		private JTextField strValue;
		private JButton numAdd;
		private JButton strAdd;

		private JButton cancel;
		private JButton done;
		private JButton update;

		private JList filterList;
		private JButton remove;

		private DefaultListModel listModel;

		private MainArea ma;

		Filter(MainArea m) {
			super("Filter");
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			ma = m;
			//setSize(300, 200);
			//JPanel bg = new JPanel();
			//showLines = new boolean[table.getNumRows()];
			//for(int i = 0; i < linemap.length; i++)
			//	showLines[i] = true;

			numericColumnLookup = new HashMap();
			stringColumnLookup = new HashMap();

			LinkedList numCols = new LinkedList();
			LinkedList strCols = new LinkedList();

			for (int i = 0; i < table.getNumColumns(); i++) {
				//Column c = table.getColumn(i);
				//if(c instanceof NumericColumn) {
				if (table.isColumnNumeric(i)) {
					numericColumnLookup.put(
						table.getColumnLabel(i),
						new Integer(i));
					numCols.add(table.getColumnLabel(i));
				} else {
					stringColumnLookup.put(
						table.getColumnLabel(i),
						new Integer(i));
					strCols.add(table.getColumnLabel(i));
				}
			}

			JOutlinePanel num = new JOutlinePanel("Numeric");
			//num.setLayout(new GridLayout(2, 3));
			num.setLayout(new GridBagLayout());

			numColumns = new JComboBox();
			Iterator i = numCols.iterator();
			while (i.hasNext())
				numColumns.addItem(i.next());
			numCols.clear();
			numOps = new JComboBox();
			numOps.addItem(GREATER_THAN);
			numOps.addItem(LESS_THAN);
			numOps.addItem(GREATER_THAN_EQUAL_TO);
			numOps.addItem(LESS_THAN_EQUAL_TO);
			numOps.addItem(EQUAL_TO);
			numOps.addItem(NOT_EQUAL_TO);
			numValue = new JTextField(5);
			numAdd = new JButton("Add");
			numAdd.addActionListener(this);

			/*num.add(numColumns);
			num.add(numOps);
			num.add(numValue);
			num.add(new JPanel());
			num.add(numAdd);
			num.add(new JPanel());
			*/
			Constrain.setConstraints(
				num,
				numColumns,
				0,
				0,
				1,
				1,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.WEST,
				1,
				1);
			Constrain.setConstraints(
				num,
				numOps,
				1,
				0,
				1,
				1,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.WEST,
				1,
				1);
			Constrain.setConstraints(
				num,
				numValue,
				2,
				0,
				1,
				1,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.WEST,
				1,
				1);
			Constrain.setConstraints(
				num,
				numAdd,
				1,
				1,
				1,
				1,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.WEST,
				1,
				1);

			JPanel str = new JOutlinePanel("String");
			//str.setLayout(new GridLayout(2, 3));
			str.setLayout(new GridBagLayout());

			strColumns = new JComboBox();
			i = strCols.iterator();
			while (i.hasNext())
				strColumns.addItem(i.next());
			strCols.clear();
			strOps = new JComboBox();
			strOps.addItem(EQUAL_TO);
			strOps.addItem(NOT_EQUAL_TO);
			strValue = new JTextField(5);
			strAdd = new JButton("Add");
			strAdd.addActionListener(this);

			/*str.add(strColumns);
			str.add(strOps);
			str.add(strValue);
			str.add(new JPanel());
			str.add(strAdd);
			str.add(new JPanel());
			*/
			Constrain.setConstraints(
				str,
				strColumns,
				0,
				0,
				1,
				1,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.WEST,
				1,
				1);
			Constrain.setConstraints(
				str,
				strOps,
				1,
				0,
				1,
				1,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.WEST,
				1,
				1);
			Constrain.setConstraints(
				str,
				strValue,
				2,
				0,
				1,
				1,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.WEST,
				1,
				1);
			Constrain.setConstraints(
				str,
				strAdd,
				1,
				1,
				1,
				1,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.WEST,
				1,
				1);

			JPanel one = new JPanel();
			//one.setLayout(new GridLayout(2, 1));
			/*one.setLayout(new BoxLayout(one, BoxLayout.Y_AXIS));
			one.add(num);
			one.add(Box.createGlue());
			one.add(str);
			*/
			one.setLayout(new GridBagLayout());
			Constrain.setConstraints(
				one,
				num,
				0,
				0,
				1,
				1,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.WEST,
				1,
				1);
			Constrain.setConstraints(
				one,
				new JPanel(),
				0,
				1,
				1,
				1,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.WEST,
				1,
				1);
			Constrain.setConstraints(
				one,
				str,
				0,
				2,
				1,
				1,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.WEST,
				1,
				1);
			JScrollPane jsp = new JScrollPane(one);
			jsp.setMinimumSize(jsp.getPreferredSize());
			//bg.setLayout(new BorderLayout());
			//bg.add(one, BorderLayout.CENTER);

			filterList = new JList();
			listModel = new DefaultListModel();
			JLabel lbl = new JLabel("Current Filters");
			Dimension d = lbl.getPreferredSize();
			//filterList.setFixedCellWidth(d.width);
			filterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			filterList.setModel(listModel);
			JScrollPane jsp1 = new JScrollPane(filterList);
			JViewport jview = new JViewport();
			jview.setView(lbl);
			jsp1.setColumnHeader(jview);
			JPanel two = new JPanel();
			two.setLayout(new BorderLayout());
			two.add(jsp1, BorderLayout.CENTER);
			remove = new JButton("Remove");
			remove.addActionListener(this);
			JPanel rp = new JPanel();
			rp.add(remove);
			two.add(rp, BorderLayout.SOUTH);
			//bg.add(two, BorderLayout.EAST);
			JSplitPane bg = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
			/*one*/
			jsp, two);

			cancel = new JButton("Cancel");
			cancel.addActionListener(this);
			done = new JButton("Done");
			done.addActionListener(this);
			update = new JButton("Update");
			update.addActionListener(this);

			JPanel buttonPanel = new JPanel();
			buttonPanel.add(cancel);
			buttonPanel.add(done);
			buttonPanel.add(update);

			JPanel whole = new JPanel();
			whole.setLayout(new BorderLayout());
			whole.add(bg, BorderLayout.CENTER);
			whole.add(buttonPanel, BorderLayout.SOUTH);

			getContentPane().add(whole);
			pack();
			setVisible(false);
		}

		/**
		 * Update the lines to show based on the current filteritems.
		 */
		private final void updateLineMap() {
			Object[] filters = listModel.toArray();
			if (filters.length == 0) {
				for (int i = 0; i < table.getNumRows(); i++)
					linemap[i] = true;
				return;
			}
			for (int i = 0; i < table.getNumRows(); i++) {
				boolean start = true;
				for (int j = 0; j < filters.length; j++) {
					FilterItem fi = (FilterItem) filters[j];
					int col = fi.colNum;
					boolean retVal;
					//if(table.getColumn(col) instanceof NumericColumn)
					if (table.isColumnNumeric(col))
						retVal = fi.evaluate(table.getDouble(i, col));
					else
						retVal = fi.evaluate(table.getString(i, col));
					start = start && retVal;
				}
				linemap[i] = start;
			}
		}

		/**
		 * Listen for button presses.
		 */
		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if (src == strAdd) {
				String colLabel = strColumns.getSelectedItem().toString();
				String op = strOps.getSelectedItem().toString();
				String value = strValue.getText();
				int colNum =
					((Integer) stringColumnLookup.get(colLabel)).intValue();
				FilterItem fi =
					new StringFilterItem(colLabel, colNum, op, value);
				listModel.addElement(fi);
				strValue.setText(EMPTY_STRING);
			} else if (src == numAdd) {
				String colLabel = numColumns.getSelectedItem().toString();
				String op = numOps.getSelectedItem().toString();
				String value = numValue.getText();
				int colNum =
					((Integer) numericColumnLookup.get(colLabel)).intValue();
				double d = 0;
				try {
					d = Double.parseDouble(value);
				} catch (Exception ex) {
				}
				FilterItem fi = new NumericFilterItem(colLabel, colNum, op, d);
				listModel.addElement(fi);
				numValue.setText(EMPTY_STRING);
			} else if (src == remove) {
				int selected = filterList.getSelectedIndex();
				if (selected != -1)
					listModel.remove(selected);
			} else if (src == cancel) {
				listModel.removeAllElements();
				for (int i = 0; i < oldFilters.length; i++) {
					listModel.addElement(oldFilters[i]);
				}
				oldFilters = null;
				setVisible(false);
			} else if (src == done) {
				updateLineMap();
				ma.updateImage();
				oldFilters = null;
				setVisible(false);
			} else if (src == update) {
				updateLineMap();
				ma.updateImage();
				oldFilters = listModel.toArray();
			}
		}

		private Object[] oldFilters;

		public void setVisible(boolean f) {
			if (f)
				oldFilters = listModel.toArray();
			super.setVisible(f);
		}

		/**
		 * Base class for filters.
		 */
		abstract private class FilterItem {
			String label;
			int colNum;
			String op;

			abstract boolean evaluate(String s);
			abstract boolean evaluate(double d);
		}

		/**
		 * Filter out items for numeric columns
		 */
		private final class NumericFilterItem extends FilterItem {
			private double value;

			NumericFilterItem(String l, int c, String o, double v) {
				label = l;
				colNum = c;
				op = o;
				value = v;
			}

			final boolean evaluate(String s) {
				double d = 0;
				try {
					d = Double.parseDouble(s);
				} catch (Exception e) {
					return false;
				}
				return evaluate(d);
			}

			final boolean evaluate(double d) {
				if (op == GREATER_THAN)
					//return value > d;
					return value < d;
				else if (op == GREATER_THAN_EQUAL_TO)
					//return value >= d;
					return value <= d;
				else if (op == LESS_THAN)
					//return value < d;
					return value > d;
				else if (op == LESS_THAN_EQUAL_TO)
					//return value <= d;
					return value >= d;
				else if (op == EQUAL_TO)
					//return value == d;
					return value != d;
				else if (op == NOT_EQUAL_TO)
					//return value != d;
					return value == d;
				return false;
			}

			/**
			*/
			public String toString() {
				StringBuffer sb = new StringBuffer(label);
				sb.append(EMPTY_STRING);
				sb.append(op);
				sb.append(EMPTY_STRING);
				sb.append(value);
				return sb.toString();
			}
		}

		/**
		 * Filter out items for non-numeric columns.
		 * String equality and inequality is used
		 */
		private final class StringFilterItem extends FilterItem {
			private String value;

			StringFilterItem(String l, int c, String o, String v) {
				label = l;
				colNum = c;
				op = o;
				value = v;
			}

			/**
			   Return true if the item should be shown
			*/
			final boolean evaluate(String s) {
				if (op == EQUAL_TO)
					return value.trim().equals(s.trim());
				else if (op == NOT_EQUAL_TO)
					return !value.trim().equals(s.trim());
				return false;
			}

			final boolean evaluate(double d) {
				String s;
				try {
					s = Double.toString(d);
				} catch (Exception e) {
					return false;
				}
				return evaluate(s);
			}

			public String toString() {
				StringBuffer sb = new StringBuffer(label);
				sb.append(EMPTY_STRING);
				sb.append(op);
				sb.append(EMPTY_STRING);
				sb.append(value);
				return sb.toString();
			}
		}
	}

	/**
	   A small window into the table we are visualizing.
	   This only shows the rows that are selected.
	*/
	private final class InfoTableModel extends DefaultTableModel {
		private ArrayList rows;

		InfoTableModel() {
			rows = new ArrayList();
		}

		private final void addInfoRow(int i) {
			rows.add(new Integer(i));
			fireTableDataChanged();
		}

		private final void removeInfoRow(int i) {
			rows.remove(new Integer(i));
			fireTableDataChanged();
		}

		public int getColumnCount() {
			return table.getNumColumns();
		}

		public int getRowCount() {
			if (rows == null)
				return 0;
			return rows.size();
		}

		public String getColumnName(int i) {
			return table.getColumnLabel(i);
		}

		public Object getValueAt(int row, int col) {
			Integer i = (Integer) rows.get(row);
			return table.getString(i.intValue(), col);
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}
	}

	/**
	 * Show the colors for each class name and its percentage of
	 * the composite
	 */
	private final class Legend extends JPanel {
		Legend() {
			setLayout(new GridBagLayout());
			setBackground(yellowish);
		}

		private final void updateLegend(HashMap lk) {
			removeAll();
			/*JLabel leg = new JLabel("LEGEND");//new AALabel("LEGEND");
			leg.setBackground(yellowish);
			Constrain.setConstraints (this, leg, 1, 0, 1, 1,
			   GridBagConstraints.HORIZONTAL,
			   GridBagConstraints.NORTH, 1.0, 0.0,
			   new Insets(2, 4, 2, 0));
			*/
			Iterator it = lk.keySet().iterator();

			int i = 0;
			while (it.hasNext()) {
				String text = (String) it.next();
				Color c = (Color) lk.get(text);

				Insets ii = new Insets(4, 8, 4, 0);
				Insets i2 = new Insets(4, 8, 4, 0);

				JLabel ll = new JLabel(text);
				ColorComponent cc = new ColorComponent(c);
				Constrain.setConstraints(
					this,
					cc,
					0,
					i,
					1,
					1,
					GridBagConstraints.NONE,
					GridBagConstraints.NORTH,
					0.0,
					0.0,
					ii);
				Constrain.setConstraints(
					this,
					ll,
					1,
					i,
					1,
					1,
					GridBagConstraints.HORIZONTAL,
					GridBagConstraints.NORTH,
					1.0,
					0.0,
					i2);
				i++;
			}
			revalidate();
			repaint();
		}
	}
	private final class HelpWindow extends JD2KFrame {
		HelpWindow() {
			super("About ParallelCoordinateVis");
			JEditorPane jep = new JEditorPane("text/html", getHelpString());
			jep.setBackground(yellowish);
			getContentPane().add(new JScrollPane(jep));
			setSize(400, 400);
		}
	}

	private static final String getHelpString() {
		StringBuffer s = new StringBuffer("<html>");
		s.append("<h2>ParallelCoordinateVis</h2>");
		s.append("ParallelCoordinateVis shows the data contained in a Table ");
		s.append(
			"on parallel axes.  For numeric columns, the maximum will be at ");
		s.append(
			"the top of the graph and the minimum will be at the bottom. ");
		s.append(
			"For any other column, each unique value will be mapped to a ");
		s.append(
			"point on its axis.  These points will be spaced equally over ");
		s.append("the axis.<br><br>");
		s.append(
			"The lines in the graph are colored according to the key column, ");
		s.append(
			"which is set to be the last column by default.  The key column ");
		s.append(
			"can be changed by using the menu.  When a numeric column is chosen ");
		s.append(
			"as the key column, the lines will be shaded by a smooth gradient ");
		s.append(
			"based on the maximum and minimum values of the key column.  For ");
		s.append(
			"other columns, each unique item in the column will be assigned ");
		s.append(
			"a unique value and the lines will be colored according to this ");
		s.append("value.  The colors can be changed using the menu.<br><br>");
		s.append(
			"A user can select a line in the graph to highlight by clicking on ");
		s.append(
			"it.  The data for the row of the table that this line corresponds ");
		s.append(
			"to will be displayed in the lower left corner.  If multiple lines pass ");
		s.append(
			"through this point, each one will be highlighted.  Lines can be ");
		s.append(
			"unselected by clicking on them again or using an item in the menu ");
		s.append("to clear all selected lines.<br><br>");
		s.append(
			"The columns can be rearranged by clicking on the box that contains ");
		s.append("their names and dragging the box to a new location.");
		s.append("<h3>Menu Options</h3>");
		s.append(
			"<ul><li>Display Columns: Choose which columns to show or hide.");
		s.append("<li>Key Column: Choose the key column.");
		s.append(
			"<li>Choose Colors: Choose the colors for this key column.  When ");
		s.append(
			"the key column is numeric, choose the high and low colors for the ");
		s.append(
			"gradient.  Otherwise, a color can be chosen for each unique item ");
		s.append("in the column.");
		s.append(
			"<li>Selected Line Color: Choose the color for the highlighted lines.");
		s.append(
			"<li>Use Antialiasing: Toggle antialiasing on and off.  When ");
		s.append(
			"antialiasing is on, the lines will appear much smoother.  This ");
		s.append("will slow the rendering speed, however.");
		s.append("<li>Clear Selected Lines: Clear all the selected lines.");
		s.append("<li>Print: Print this visualization.</ul>");
		s.append("<h3>Toolbar Buttons</h3>");
		s.append("<ul><li>Reset View: Reset the view to the default size.");
		s.append(
			"<li>Filter: Display the filter window, which allows the user ");
		s.append("to determine which lines are displayed in the graph.");
		s.append("<li>Print: Print this visualization.");
		s.append("<li>Zoom: When this button is pressed, left-click the ");
		s.append("graph to zoom in, or right-click the graph to zoom out.");
		s.append("<li>Show Table: Show the Table that is being graphed.");
		s.append("</ul></html>");
		return s.toString();
	}

	private class MaxMin {
		double min;
		double max;
	}

	private MaxMin getMaxMin(int colIdx) {
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < table.getNumRows(); i++) {
			double d = table.getDouble(i, colIdx);
			if (d < min)
				min = d;
			if (d > max)
				max = d;
		}
		MaxMin mm = new MaxMin();
		mm.min = min;
		mm.max = max;
		return mm;
	}
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

