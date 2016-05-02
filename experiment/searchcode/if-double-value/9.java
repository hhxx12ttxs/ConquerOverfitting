/*
<<<<<<< HEAD
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.wink.json4j;

import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Stack;

import org.apache.wink.json4j.internal.BeanSerializer;

/**
 * This class implements a JSONWrier, a convenience function for writing out JSON
 * to a writer or underlying stream.
 */
public class JSONWriter {

    /**
     * The writer to use to output JSON in a semi-streaming fashion.
     */
    protected Writer writer = null;

    /**
     * Flag to denote that the writer is in an object.  
     */
    private boolean inObject = false;

    /**
     * Flag to denote that the writer is in an array.  
     */
    private boolean inArray = false;

    /**
     * Flag for state checking that a key was placed (if inside an object)
     * Required to be true for a value to be placed in that situation
     */
    private boolean keyPlaced = false;

    /**
     * Flag denoting if in an array or object, if the first entry has been placed or not.
     */
    private boolean firstEntry = false;

    /**
     * A stack to keep track of all the closures.
     */
    private Stack closures = null;

    /** 
     * Flag used to check the state of this writer, if it has been closed, all
     * operations will throw an IllegalStateException.
     */
    private boolean closed = false;

    /**
     * Constructor.
     * @param writer The writer to use to do 'streaming' JSON writing.
     * @throws NullPointerException Thrown if writer is null.
     */
    public JSONWriter(Writer writer) throws NullPointerException {
        //Try to avoid double-buffering or buffering in-memory writers.
        Class writerClass = writer.getClass();
        if (!StringWriter.class.isAssignableFrom(writerClass) &&
            !CharArrayWriter.class.isAssignableFrom(writerClass) &&
            !BufferedWriter.class.isAssignableFrom(writerClass)) {
            writer = new BufferedWriter(writer);
        }
        this.writer = writer;
        this.closures = new Stack();
    }

    /**
     * Open a new JSON Array in the output stream.
     * @throws IOException Thrown if an error occurs on the underlying writer.
     * @throws IllegalstateException Thrown if the current writer position does not permit an array.
     * @return A reference to this writer.
     */
    public JSONWriter array() throws IOException, IllegalStateException {
        if (closed) {
            throw new IllegalStateException("The writer has been closed.  No further operations allowed.");
        }
        if (inObject) {
            if (!keyPlaced) {
                throw new IllegalStateException("Current containment is a JSONObject, but a key has not been specified to contain a new array");
            }
        } else if (inArray) {
            if (!firstEntry) {
                writer.write(",");
            }
        }
        writer.write("[");
        inArray = true;
        inObject = false;
        keyPlaced = false;
        firstEntry = true;
        closures.push("]");
        return this;
    }

    /**
     * Method to close the current JSON Array in the stream.  
     * @throws IOException Thrown if an IO error occurs on the underlying writer.
     * @throws IllegalStateException Thrown if the writer position is not inside an array.
     * @return A reference to this writer.
     */
    public JSONWriter endArray() throws IOException {
        if (closed) {
            throw new IllegalStateException("The writer has been closed.  No further operations allowed.");
        }
        if (!inArray) {
            throw new IllegalStateException("Current writer position is not within a JSON array");
        } else {
            writer.write(((String)closures.pop()));
            // Set our current positional/control state.
            if (!closures.isEmpty()) {
                String nextClosure = (String)closures.peek();
                if (nextClosure.equals("}")) {
                    inObject = true;
                    inArray = false;
                } else {
                    inObject = false;
                    inArray = true;
                }
                firstEntry = false;
            } else {
                inArray = false;
                inObject = false;
                firstEntry = true;
            }
        }
        return this;
    }

    /**
     * Method to close a current JSON object in the stream.  
     * @throws IOException Thrown if an IO error occurs on the underlying writer.
     * @throws IllegalStateException Thrown if the writer position is not inside an object, or if the object has a key placed, but no value.
     * @return A reference to this writer.
     */
    public JSONWriter endObject() throws IOException, IllegalStateException {
        if (closed) {
            throw new IllegalStateException("The writer has been closed.  No further operations allowed.");
        }
        if (!inObject) {
            throw new IllegalStateException("Current writer position is not within a JSON object");
        } else {
            if (keyPlaced) {
                throw new IllegalStateException("Current writer position in an object and has a key placed, but no value has been assigned to the key.  Cannot end.");
            } else {
                writer.write((String)closures.pop());
                // Set our current positional/control state.
                if (!closures.isEmpty()) {
                    String nextClosure = (String)closures.peek();
                    if (nextClosure.equals("}")) {
                        inObject = true;
                        inArray = false;
                    } else {
                        inObject = false;
                        inArray = true;
                    }
                    firstEntry = false;
                } else {
                    inArray = false;
                    inObject = false;
                    firstEntry = true;
                }
            }
        }
        return this;
    }

    /**
     * Place a key in the current JSON Object.
     * @throws IOException Thrown if an IO error occurs on the underlying writer.
     * @throws IllegalStateException Thrown if the current writer position is not within an object.
     * @return A reference to this writer.
     */
    public JSONWriter key(String s) throws IOException, IllegalStateException, NullPointerException {
        if (closed) {
            throw new IllegalStateException("The writer has been closed.  No further operations allowed.");
        }
        if (s == null) {
            throw new NullPointerException("Key cannot be null");
        } else {
            if (!inObject) {
                throw new IllegalStateException("Current writer position is not inside a JSON Object, a key cannot be placed.");
            } else {
                if (!keyPlaced) {
                    if (firstEntry) {
                        firstEntry = false;
                    } else {
                        writer.write(",");
                    }
                    keyPlaced = true;
                    writeString(s);
                    writer.write(":");
                } else {
                    throw new IllegalStateException("Current writer position is inside a JSON Object an with an open key waiting for a value.  Another key cannot be placed.");
                }
            }
        }
        return this;
    }

    /**
     * Open a new JSON Object in the output stream.
     * @throws IllegalStateException Thrown if an object cannot currently be created in the stream.
     * @throws IOException Thrown if an IO error occurs in the underlying writer.
     * @return A reference to this writer.
     */
    public JSONWriter object() throws IOException, IllegalStateException {
        if (closed) {
            throw new IllegalStateException("The writer has been closed.  No further operations allowed.");
        }
        if (inObject) {
            if (!keyPlaced) {
                throw new IllegalStateException("Current containment is a JSONObject, but a key has not been specified to contain a new object");
            }
        } else if (inArray) {
            if (!firstEntry) {
                writer.write(",");
            }
        }
        writer.write("{");
        inObject = true;
        inArray = false;
        keyPlaced = false;
        firstEntry = true;
        closures.push("}");
        return this;
    }

    /**
     * Method to write a boolean to the current writer position.
     * @throws IOException Thrown if an IO error occurs on the underlying writer.
     * @throws IllegalStateException Thrown if the current writer position will not accept a boolean value.
     * @return A reference to this writer.
     */
    public JSONWriter value(boolean b) throws IOException, IllegalStateException {
        if (closed) {
            throw new IllegalStateException("The writer has been closed.  No further operations allowed.");
        }
        if (inArray) {
            if (firstEntry) {
                firstEntry = false;
            } else {
                writer.write(",");
            }
            writer.write(Boolean.toString(b));
        } else if (inObject) {
            if (keyPlaced) {
                writer.write(Boolean.toString(b));
                keyPlaced = false;
            } else {
                throw new IllegalStateException("Current containment is a JSONObject, but a key has not been specified for the boolean value.");
            }
        } else {
            throw new IllegalStateException("Writer is currently not in an array or object, cannot write value");
        }
        return this;
    }

    /**
     * Method to write a double to the current writer position.
     * @param d The Double to write.
     * @throws IOException Thrown if an IO error occurs on the underlying writer.
     * @throws IllegalStateException Thrown if the current writer position will not accept a double value.
     * @return A reference to this writer.
     */
    public JSONWriter value(double d) throws IOException, IllegalStateException {
        if (closed) {
            throw new IllegalStateException("The writer has been closed.  No further operations allowed.");
        }
        if (inArray) {
            if (firstEntry) {
                firstEntry = false;
            } else {
                writer.write(",");
            }
            writer.write(Double.toString(d));
        } else if (inObject) {
            if (keyPlaced) {
                writer.write(Double.toString(d));
                keyPlaced = false;
            } else {
                throw new IllegalStateException("Current containment is a JSONObject, but a key has not been specified for the double value.");
            }
        } else {
            throw new IllegalStateException("Writer is currently not in an array or object, cannot write value");
        }
        return this;
    }

    /**
     * Method to write a double to the current writer position.
     * @param l The long to write.
     * @throws IOException Thrown if an IO error occurs on the underlying writer.
     * @throws IllegalStateException Thrown if the current writer position will not accept a double value.
     * @return A reference to this writer.
     */
    public JSONWriter value(long l) throws IOException, IllegalStateException {
        if (closed) {
            throw new IllegalStateException("The writer has been closed.  No further operations allowed.");
        }
        if (inArray) {
            if (firstEntry) {
                firstEntry = false;
            } else {
                writer.write(",");
            }
            writer.write(Long.toString(l));
        } else if (inObject) {
            if (keyPlaced) {
                writer.write(Long.toString(l));
                keyPlaced = false;
            } else {
                throw new IllegalStateException("Current containment is a JSONObject, but a key has not been specified for the long value.");
            }
        } else {
            throw new IllegalStateException("Writer is currently not in an array or object, cannot write value");
        }
        return this;
    }

    /**
     * Method to write an int to the current writer position.
     * @param i The int to write.
     * @throws IOException Thrown if an IO error occurs on the underlying writer.
     * @throws IllegalStateException Thrown if the current writer position will not accept a double value.
     * @return A reference to this writer.
     */
    public JSONWriter value(int i) throws IOException, IllegalStateException {
        if (closed) {
            throw new IllegalStateException("The writer has been closed.  No further operations allowed.");
        }
        if (inArray) {
            if (firstEntry) {
                firstEntry = false;
            } else {
                writer.write(",");
            }
            writer.write(Integer.toString(i));
        } else if (inObject) {
            if (keyPlaced) {
                writer.write(Integer.toString(i));
                keyPlaced = false;
            } else {
                throw new IllegalStateException("Current containment is a JSONObject, but a key has not been specified for the int value.");
            }
        } else {
            throw new IllegalStateException("Writer is currently not in an array or object, cannot write value");
        }
        return this;
    }

    /**
     * Method to write a short to the current writer position.
     * @param s The short to write.
     * @throws IOException Thrown if an IO error occurs on the underlying writer.
     * @throws IllegalStateException Thrown if the current writer position will not accept a double value.
     * @return A reference to this writer.
     */
    public JSONWriter value(short s) throws IOException, IllegalStateException {
        if (closed) {
            throw new IllegalStateException("The writer has been closed.  No further operations allowed.");
        }
        if (inArray) {
            if (firstEntry) {
                firstEntry = false;
            } else {
                writer.write(",");
            }
            writer.write(Short.toString(s));
        } else if (inObject) {
            if (keyPlaced) {
                writer.write(Short.toString(s));
                keyPlaced = false;
            } else {
                throw new IllegalStateException("Current containment is a JSONObject, but a key has not been specified for the short value.");
            }
        } else {
            throw new IllegalStateException("Writer is currently not in an array or object, cannot write value");
        }
        return this;
    }

    /**
     * Method to write an Object to the current writer position.
     * @param o The object to write.
     * @throws IOException Thrown if an IO error occurs on the underlying writer.
     * @throws JSONException Thrown if the object is not JSONAble.
     * @return A reference to this writer.
     */
    public JSONWriter value(Object o) throws IOException, IllegalStateException, JSONException {
        if (closed) {
            throw new IllegalStateException("The writer has been closed.  No further operations allowed.");
        }
        if (inArray) {
            if (firstEntry) {
                firstEntry = false;
            } else {
                writer.write(",");
            }
            writeObject(o);
        } else if (inObject) {
            if (keyPlaced) {
                writeObject(o);
                keyPlaced = false;
            } else {
                throw new IllegalStateException("Current containment is a JSONObject, but a key has not been specified for the boolean value.");
            }
        } else {
            throw new IllegalStateException("Writer is currently not in an array or object, cannot write value");
        }
        return this;
    }


    /**
     * Method to close the JSON Writer.  All current object depths will be closed out and the writer closed.
     * @throws IOException Thrown if an IO error occurs on the underlying writer.
     * @throws IllegalStateException Thrown if the writer position is in an object and a key has been placed, but a value has not been assigned or if the writer was already closed.
     */
    public void close() throws IOException, IllegalStateException {
        if (!closed) {
            if (inObject && keyPlaced) {
                throw new IllegalStateException("Object has key without value.  Cannot close.");
            } else {
                while (!closures.isEmpty()) {
                    writer.write((String)closures.pop());
                }
                writer.flush();
                writer.close();
                closed = true;
            }
        }
    }

    /**
     * Method to flush the underlying writer so that all buffered content, if any, is written out.
     * @return A reference to this writer.
     */
    public JSONWriter flush() throws IOException {
        writer.flush();
        return this;
    }

    /**
     * Method to write a String out to the writer, encoding special characters and unicode characters properly.
     * @param value The string to write out.
     * @throws IOException Thrown if an error occurs during write.
     */
    private void writeString(String value) throws IOException {
        writer.write('"');
        char[] chars = value.toCharArray();
        for (int i=0; i<chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case  '"': writer.write("\\\""); break;
                case '\\': writer.write("\\\\"); break;
                case    0: writer.write("\\0"); break;
                case '\b': writer.write("\\b"); break;
                case '\t': writer.write("\\t"); break;
                case '\n': writer.write("\\n"); break;
                case '\f': writer.write("\\f"); break;
                case '\r': writer.write("\\r"); break;
                case '/': writer.write("\\/"); break;
                default:
                    if ((c >= 32) && (c <= 126)) {
                        writer.write(c);
                    } else {
                        writer.write("\\u");
                        writer.write(rightAlignedZero(Integer.toHexString(c),4));
                    }
            }
        }
        writer.write('"');
    }

    /**
     * Method to generate a string with a particular width.  Alignment is done using zeroes if it does not meet the width requirements.
     * @param s The string to write
     * @param len The minimum length it should be, and to align with zeroes if length is smaller.
     * @return A string properly aligned/correct width.
     */
    private String rightAlignedZero(String s, int len) {
        if (len == s.length()) return s;
        StringBuffer sb = new StringBuffer(s);
        while (sb.length() < len) {
            sb.insert(0, '0');
        }
        return sb.toString();
    }

    /**
     * Method to write a number to the current writer.
     * @param value The number to write to the JSON output string.
     * @throws IOException Thrown if an error occurs during write.
     */
    private void writeNumber(Number value) throws IOException {
        if (null == value) {
            writeNull();
        }
        if (value instanceof Float) {
            if (((Float)value).isNaN()) {
                writeNull();
            }
            if (Float.NEGATIVE_INFINITY == value.floatValue()) {
                writeNull();
            }
            if (Float.POSITIVE_INFINITY == value.floatValue()) {
                writeNull();
            }
        }
        if (value instanceof Double) {
            if (((Double)value).isNaN()) {
                writeNull();
            }
            if (Double.NEGATIVE_INFINITY == value.doubleValue()) {
                writeNull();
            }
            if (Double.POSITIVE_INFINITY == value.doubleValue()) {
                writeNull();
            }
        }
        writer.write(value.toString());
    }

    /**
     * Method to write an object to the current writer.
     * @param o The object to write.
     * @throws IOException Thrown if an IO error occurs on the underlying writer.
     * @throws JSONException Thrown if the specified object is not JSONAble.
     */
    private void writeObject(Object o) throws IOException, JSONException {
        // Handle the object!
        if (o == null) {
            writeNull();
        } else {
            Class clazz = o.getClass();
            if (JSONArtifact.class.isAssignableFrom(clazz)) {
                writer.write(((JSONArtifact)o).toString());
            } else if (Number.class.isAssignableFrom(clazz)) {
                writeNumber((Number)o);
            } else if (Boolean.class.isAssignableFrom(clazz)) {
                writer.write(((Boolean)o).toString());
            } else if (String.class.isAssignableFrom(clazz)) {
                writeString((String)o);
            } else if (JSONString.class.isAssignableFrom(clazz)) {
                writer.write(((JSONString)o).toJSONString());
            } else {
                // Unknown type, we'll just try to serialize it like a Java Bean.
                writer.write(BeanSerializer.toJson(o, true).write());
            }
        }
    }

    /**
     * Method to write the text string 'null' to the output stream (null JSON object).
     * @throws IOException Thrown if an error occurs during write.
     */
    private void writeNull() throws IOException {
        writer.write("null");
    }
=======
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

>>>>>>> 76aa07461566a5976980e6696204781271955163
}

