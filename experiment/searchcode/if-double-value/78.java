<<<<<<< HEAD
/*
 * Copyright 2004-2013 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.test.synth.sql;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * Represents a simple value.
 */
public class Value {
    private final int type;
    private final Object data;
    private final TestSynth config;

    private Value(TestSynth config, int type, Object data) {
        this.config = config;
        this.type = type;
        this.data = data;
    }

    /**
     * Convert the value to a SQL string.
     *
     * @return the SQL string
     */
    String getSQL() {
        if (data == null) {
            return "NULL";
        }
        switch (type) {
        case Types.DECIMAL:
        case Types.NUMERIC:
        case Types.BIGINT:
        case Types.INTEGER:
        case Types.DOUBLE:
        case Types.REAL:
            return data.toString();
        case Types.CLOB:
        case Types.VARCHAR:
        case Types.CHAR:
        case Types.OTHER:
        case Types.LONGVARCHAR:
            return "'" + data.toString() + "'";
        case Types.BLOB:
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
            return getBlobSQL();
        case Types.DATE:
            return getDateSQL((Date) data);
        case Types.TIME:
            return getTimeSQL((Time) data);
        case Types.TIMESTAMP:
            return getTimestampSQL((Timestamp) data);
        case Types.BOOLEAN:
        case Types.BIT:
            return (String) data;
        default:
            throw new AssertionError("type=" + type);
        }
    }

    private static Date randomDate(TestSynth config) {
        return config.random().randomDate();

    }

    private static Double randomDouble(TestSynth config) {
        return config.random().getInt(100) / 10.;
    }

    private static Long randomLong(TestSynth config) {
        return Long.valueOf(config.random().getInt(1000));
    }

    private static Time randomTime(TestSynth config) {
        return config.random().randomTime();
    }

    private static Timestamp randomTimestamp(TestSynth config) {
        return config.random().randomTimestamp();
    }

    private String getTimestampSQL(Timestamp ts) {
        String s = "'" + ts.toString() + "'";
        if (config.getMode() != TestSynth.HSQLDB) {
            s = "TIMESTAMP " + s;
        }
        return s;
    }

    private String getDateSQL(Date date) {
        String s = "'" + date.toString() + "'";
        if (config.getMode() != TestSynth.HSQLDB) {
            s = "DATE " + s;
        }
        return s;
    }

    private String getTimeSQL(Time time) {
        String s = "'" + time.toString() + "'";
        if (config.getMode() != TestSynth.HSQLDB) {
            s = "TIME " + s;
        }
        return s;
    }

    private String getBlobSQL() {
        byte[] bytes = (byte[]) data;
        // StringBuilder buff = new StringBuilder("X'");
        StringBuilder buff = new StringBuilder("'");
        for (byte b : bytes) {
            int c = b & 0xff;
            buff.append(Integer.toHexString(c >> 4 & 0xf));
            buff.append(Integer.toHexString(c & 0xf));

        }
        buff.append("'");
        return buff.toString();
    }

    /**
     * Read a value from a result set.
     *
     * @param config the configuration
     * @param rs the result set
     * @param index the column index
     * @return the value
     */
    static Value read(TestSynth config, ResultSet rs, int index)
            throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        Object data;
        int type = meta.getColumnType(index);
        switch (type) {
        case Types.REAL:
        case Types.DOUBLE:
            data = rs.getDouble(index);
            break;
        case Types.BIGINT:
            data = rs.getLong(index);
            break;
        case Types.DECIMAL:
        case Types.NUMERIC:
            data = rs.getBigDecimal(index);
            break;
        case Types.BLOB:
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
            data = rs.getBytes(index);
            break;
        case Types.OTHER:
        case Types.CLOB:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
        case Types.CHAR:
            data = rs.getString(index);
            break;
        case Types.DATE:
            data = rs.getDate(index);
            break;
        case Types.TIME:
            data = rs.getTime(index);
            break;
        case Types.TIMESTAMP:
            data = rs.getTimestamp(index);
            break;
        case Types.INTEGER:
            data = rs.getInt(index);
            break;
        case Types.NULL:
            data = null;
            break;
        case Types.BOOLEAN:
        case Types.BIT:
            data = rs.getBoolean(index) ? "TRUE" : "FALSE";
            break;
        default:
            throw new AssertionError("type=" + type);
        }
        if (rs.wasNull()) {
            data = null;
        }
        return new Value(config, type, data);
    }

    /**
     * Generate a random value.
     *
     * @param config the configuration
     * @param type the value type
     * @param precision the precision
     * @param scale the scale
     * @param mayBeNull if the value may be null or not
     * @return the value
     */
    static Value getRandom(TestSynth config, int type, int precision,
            int scale, boolean mayBeNull) {
        Object data;
        if (mayBeNull && config.random().getBoolean(20)) {
            return new Value(config, type, null);
        }
        switch (type) {
        case Types.BIGINT:
            data = randomLong(config);
            break;
        case Types.DOUBLE:
            data = randomDouble(config);
            break;
        case Types.DECIMAL:
            data = randomDecimal(config, precision, scale);
            break;
        case Types.VARBINARY:
        case Types.BINARY:
        case Types.BLOB:
            data = randomBytes(config, precision);
            break;
        case Types.CLOB:
        case Types.VARCHAR:
            data = config.random().randomString(config.random().getInt(precision));
            break;
        case Types.DATE:
            data = randomDate(config);
            break;
        case Types.TIME:
            data = randomTime(config);
            break;
        case Types.TIMESTAMP:
            data = randomTimestamp(config);
            break;
        case Types.INTEGER:
            data = randomInt(config);
            break;
        case Types.BOOLEAN:
        case Types.BIT:
            data = config.random().getBoolean(50) ? "TRUE" : "FALSE";
            break;
        default:
            throw new AssertionError("type=" + type);
        }
        return new Value(config, type, data);
    }

    private static Object randomInt(TestSynth config) {
        int value;
        if (config.is(TestSynth.POSTGRESQL)) {
            value = config.random().getInt(1000000);
        } else {
            value = config.random().getRandomInt();
        }
        return value;
    }

    private static byte[] randomBytes(TestSynth config, int max) {
        int len = config.random().getLog(max);
        byte[] data = new byte[len];
        config.random().getBytes(data);
        return data;
    }

    private static BigDecimal randomDecimal(TestSynth config, int precision,
            int scale) {
        int len = config.random().getLog(precision - scale) + scale;
        if (len == 0) {
            len++;
        }
        StringBuilder buff = new StringBuilder();
        for (int i = 0; i < len; i++) {
            buff.append((char) ('0' + config.random().getInt(10)));
        }
        buff.insert(len - scale, '.');
        if (config.random().getBoolean(20)) {
            buff.insert(0, '-');
        }
        return new BigDecimal(buff.toString());
    }

//    private int compareTo(Object o) {
//        Value v = (Value) o;
//        if (type != v.type) {
//            throw new AssertionError("compare " + type +
//                    " " + v.type + " " + data + " " + v.data);
//        }
//        if (data == null) {
//            return (v.data == null) ? 0 : -1;
//        } else if (v.data == null) {
//            return 1;
//        }
//        switch (type) {
//        case Types.DECIMAL:
//            return ((BigDecimal) data).compareTo((BigDecimal) v.data);
//        case Types.BLOB:
//        case Types.VARBINARY:
//        case Types.BINARY:
//            return compareBytes((byte[]) data, (byte[]) v.data);
//        case Types.CLOB:
//        case Types.VARCHAR:
//            return data.toString().compareTo(v.data.toString());
//        case Types.DATE:
//            return ((Date) data).compareTo((Date) v.data);
//        case Types.INTEGER:
//            return ((Integer) data).compareTo((Integer) v.data);
//        default:
//            throw new AssertionError("type=" + type);
//        }
//    }

//    private static int compareBytes(byte[] a, byte[] b) {
//        int al = a.length, bl = b.length;
//        int len = Math.min(al, bl);
//        for (int i = 0; i < len; i++) {
//            int x = a[i] & 0xff;
//            int y = b[i] & 0xff;
//            if (x == y) {
//                continue;
//            }
//            return x > y ? 1 : -1;
//        }
//        return al == bl ? 0 : al > bl ? 1 : -1;
//    }

    @Override
    public String toString() {
        return getSQL();
    }

}

=======
/*
 * @(#)$Id: XDouble.java 3619 2008-03-26 07:23:03Z yui $
 *
 * Copyright 2006-2008 Makoto YUI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributors:
 *     Makoto YUI - initial implementation
 */
package xbird.xquery.dm.value.literal;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import xbird.util.lang.ObjectUtils;
import xbird.xquery.DynamicError;
import xbird.xquery.XQueryException;
import xbird.xquery.dm.value.AtomicValue;
import xbird.xquery.dm.value.Item;
import xbird.xquery.dm.value.xsi.BooleanValue;
import xbird.xquery.meta.DynamicContext;
import xbird.xquery.type.AtomicType;
import xbird.xquery.type.TypeTable;
import xbird.xquery.type.xs.DoubleType;

/**
 * 
 * <DIV lang="en"></DIV>
 * <DIV lang="ja"></DIV>
 * 
 * @author yui (yuin405+xbird@gmail.com)
 * @link http://www.w3.org/TR/xmlschema-2/#double
 */
public final class XDouble extends XNumber {
    private static final long serialVersionUID = -8779305902783571768L;
    public static final int ID = 3;

    private static final DecimalFormat decFormat;
    private static final DecimalFormat decSciFormat;
    static {
        final DecimalFormatSymbols symbol = new DecimalFormatSymbols(Locale.US);
        decFormat = new DecimalFormat("#####0.#################", symbol);
        decSciFormat = new DecimalFormat("0.0################E0##", symbol);
    }

    public static final XDouble COMPARABLE_NaN = new XDouble("NaN", Double.NaN);

    private double value;
    private transient int hashcode = -1;
    private transient String _canonical = null;

    public XDouble() {
        super();
    }

    public XDouble(String literal) {
        this(literal, parseDouble(literal));
    }

    private XDouble(String literal, double value) {
        super(literal, DoubleType.DOUBLE);
        this.value = value;
    }

    public XDouble(double value) {
        super(Double.toString(value), DoubleType.DOUBLE);
        this.value = value;
    }

    private static double parseDouble(final String literal) {
        assert (literal != null);
        if("INF".equals(literal)) {
            return Double.POSITIVE_INFINITY;
        } else if("-INF".equals(literal)) {
            return Double.NEGATIVE_INFINITY;
        } else if("NaN".equals(literal)) {
            return Double.NaN;
        } else {
            return Double.parseDouble(literal);
        }
    }

    public double getValue() {
        return value;
    }

    public Number getNumber() {
        return value;
    }

    public Double toJavaObject() throws XQueryException {
        return value;
    }

    @Override
    public int compareTo(Item trg) {
        if(this == trg && trg == COMPARABLE_NaN) {
            return 0;
        }
        if(trg instanceof XDouble) {
            if(Double.isNaN(value) && ((XDouble) trg).isNaN()) {
                return -1; // incomparable: this object set to be smaller than trg.
            }
            final double trgValue = ((XDouble) trg).value;
            return Double.compare(value, trgValue);
        }
        return super.compareTo(trg);
    }

    public static XDouble valueOf(double value) {
        return new XDouble(value);
    }

    public boolean isNaN() {
        return Double.isNaN(value);
    }

    public XDouble negate() {
        this.value = (-value);
        onUpdate();
        return this;
    }

    public XNumber ceil() {
        this.value = Math.ceil(value);
        onUpdate();
        return this;
    }

    public XNumber floor() {
        this.value = Math.floor(value);
        onUpdate();
        return this;
    }

    public XNumber round() {
        this.value = Math.round(value);
        onUpdate();
        return this;
    }

    public XNumber roundHalfToEven(int precision) {
        final BigDecimal rounded = BigDecimal.valueOf(value).setScale(precision, RoundingMode.HALF_EVEN);
        this.value = rounded.doubleValue();
        onUpdate();
        return this;
    }

    public BigDecimal asDecimal() {
        return BigDecimal.valueOf(value);
    }

    @Override
    public double asDouble() {
        return value;
    }

    @Override
    public int hashCode() {
        if(hashcode != -1) {
            return hashcode;
        }
        final long bits = Double.doubleToLongBits(value);
        final int h = (int) (bits ^ (bits >>> 32));
        this.hashcode = h;
        return h;
    }

    @Override
    public String stringValue() {
        return this.toString();
    }

    @Override
    protected void onUpdate() {
        this._canonical = null;
        this.hashcode = -1;
        String sv = toString();
        setStringValue(sv);
    }

    @Override
    public synchronized String toString() {
        if(_canonical == null) {
            final String c;
            if(value == Double.POSITIVE_INFINITY) {
                c = "INF";
            } else if(value == Double.NEGATIVE_INFINITY) {
                c = "-INF";
            } else if(value != value) {
                c = "NaN";
            } else if(value == 0) {
                c = ((1.0 / value) == Double.POSITIVE_INFINITY) ? "0" : "-0";
            } else {
                final double abs = Math.abs(value);
                if(abs >= 1e-6 && abs < 1e6) {
                    synchronized(decFormat) {
                        c = decFormat.format(value);
                    }
                } else {
                    c = decSciFormat.format(value);
                }
            }
            this._canonical = c;
            return c;
        }
        return _canonical;
    }

    /**
     * @link http://www.w3.org/TR/xpath-functions/#casting-to-numerics
     */
    @Override
    public <T extends AtomicValue> T castAs(AtomicType trgType, DynamicContext dynEnv)
            throws XQueryException {
        final int ttid = trgType.getTypeId();
        final AtomicValue v;
        switch(ttid) {
            case TypeTable.BOOLEAN_TID:
                final boolean ebv = (value != 0.0 && !Double.isNaN(value));
                v = new BooleanValue(ebv);
                break;
            case TypeTable.DOUBLE_TID:
            case TypeTable.NUMERIC_TID:
                v = this;
                break;
            case TypeTable.INTEGER_TID:
                if(Double.isNaN(value)) {
                    throw new DynamicError("err:FOCA0002", "Can't convert xs:double(" + toString()
                            + ") to xs:integer");
                }
                if(Double.isInfinite(value)) {
                    throw new DynamicError("err:FOCA0002", "Can't convert xs:double(" + toString()
                            + ") to xs:integer");
                }
                v = XInteger.valueOf(asLong());
                break;
            case TypeTable.FLOAT_TID:
                if(Double.isNaN(value)) {
                    v = XFloat.valueOf(Float.NaN);
                } else if(value == Double.POSITIVE_INFINITY) {
                    v = XFloat.valueOf(Float.POSITIVE_INFINITY);
                } else if(value == Double.NEGATIVE_INFINITY) {
                    v = XFloat.valueOf(Float.NEGATIVE_INFINITY);
                } else {
                    v = XFloat.valueOf((float) value);
                }
                break;
            case TypeTable.DECIMAL_TID:
                if(Double.isNaN(value)) {
                    throw new DynamicError("err:FORG0001", "Can't convert xs:double(" + toString()
                            + ") to xs:decimal");
                }
                if(Double.isInfinite(value)) {
                    throw new DynamicError("err:FOCA0002", "Can't convert xs:double(" + toString()
                            + ") to xs:decimal");
                }
                v = XDecimal.valueOf(asDecimal());
                break;
            default:
                v = super.castAs(trgType, dynEnv);
                break;
        }
        return (T) v;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this._lexicalValue = ObjectUtils.readString(in);
        this._type = DoubleType.DOUBLE;
        this.value = in.readDouble();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        ObjectUtils.writeString(out, _lexicalValue);
        out.writeDouble(value);
    }

    @Override
    public int getIdentifier() {
        return ID;
    }

    @Override
    public AtomicValue asGroupingValue() {
        return isNaN() ? COMPARABLE_NaN : this;
    }

}
>>>>>>> 76aa07461566a5976980e6696204781271955163
