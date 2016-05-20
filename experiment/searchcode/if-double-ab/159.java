/**
 * Copyright (C) 2010 Mycila <mathieu.carbou@gmail.com>
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

package com.mycila.jdbc.query;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Generic utility methods for working with JDBC. Mainly for internal use
 * within the framework, but also useful for custom JDBC access code.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 */
final class JdbcUtils {

    private JdbcUtils() {
    }

    /**
     * Close the given JDBC Connection and ignore any thrown exception.
     * This is useful for typical finally blocks in manual JDBC code.
     *
     * @param con the JDBC Connection to close (may be <code>null</code>)
     */
    static void closeConnection(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Close the given JDBC Statement and ignore any thrown exception.
     * This is useful for typical finally blocks in manual JDBC code.
     *
     * @param stmt the JDBC Statement to close (may be <code>null</code>)
     */
    static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Retrieve a JDBC column value from a ResultSet, using the specified value type.
     * <p>Uses the specifically typed ResultSet accessor methods, falling back to
     * {@link #getResultSetValue(java.sql.ResultSet, int)} for unknown types.
     * <p>Note that the returned value may not be assignable to the specified
     * required type, in case of an unknown type. Calling code needs to deal
     * with this case appropriately, e.g. throwing a corresponding exception.
     *
     * @param rs           is the ResultSet holding the data
     * @param index        is the column index
     * @param requiredType the required value type (may be <code>null</code>)
     * @return the value object
     * @throws java.sql.SQLException if thrown by the JDBC API
     */
    @SuppressWarnings({"unchecked"})
    static <T> T getResultSetValue(ResultSet rs, int index, Class<T> requiredType) throws SQLException {
        if (requiredType == null) {
            return JdbcUtils.<T>getResultSetValue(rs, index);
        }

        Object value;
        boolean wasNullCheck = false;

        // Explicitly extract typed value, as far as possible.
        if (String.class.equals(requiredType)) {
            value = rs.getString(index);
        } else if (boolean.class.equals(requiredType) || Boolean.class.equals(requiredType)) {
            value = rs.getBoolean(index);
            wasNullCheck = true;
        } else if (byte.class.equals(requiredType) || Byte.class.equals(requiredType)) {
            value = rs.getByte(index);
            wasNullCheck = true;
        } else if (short.class.equals(requiredType) || Short.class.equals(requiredType)) {
            value = rs.getShort(index);
            wasNullCheck = true;
        } else if (int.class.equals(requiredType) || Integer.class.equals(requiredType)) {
            value = rs.getInt(index);
            wasNullCheck = true;
        } else if (long.class.equals(requiredType) || Long.class.equals(requiredType)) {
            value = rs.getLong(index);
            wasNullCheck = true;
        } else if (float.class.equals(requiredType) || Float.class.equals(requiredType)) {
            value = rs.getFloat(index);
            wasNullCheck = true;
        } else if (double.class.equals(requiredType) || Double.class.equals(requiredType) ||
                Number.class.equals(requiredType)) {
            value = rs.getDouble(index);
            wasNullCheck = true;
        } else if (byte[].class.equals(requiredType)) {
            value = rs.getBytes(index);
        } else if (java.sql.Date.class.equals(requiredType)) {
            value = rs.getDate(index);
        } else if (java.sql.Time.class.equals(requiredType)) {
            value = rs.getTime(index);
        } else if (java.sql.Timestamp.class.equals(requiredType) || java.util.Date.class.equals(requiredType)) {
            value = rs.getTimestamp(index);
        } else if (BigDecimal.class.equals(requiredType)) {
            value = rs.getBigDecimal(index);
        } else if (Blob.class.equals(requiredType)) {
            value = rs.getBlob(index);
        } else if (Clob.class.equals(requiredType)) {
            value = rs.getClob(index);
        } else if (URL.class.equals(requiredType)) {
            value = rs.getURL(index);
        } else {
            // Some unknown type desired -> rely on getObject.
            value = getResultSetValue(rs, index);
        }

        // Perform was-null check if demanded (for results that the
        // JDBC driver returns as primitives).
        if (wasNullCheck && value != null && rs.wasNull()) {
            value = null;
        }
        return (T) value;
    }

    /**
     * Retrieve a JDBC column value from a ResultSet, using the most appropriate
     * value type. The returned value should be a detached value object, not having
     * any ties to the active ResultSet: in particular, it should not be a Blob or
     * Clob object but rather a byte array respectively String representation.
     * <p>Uses the <code>getObject(index)</code> method, but includes additional "hacks"
     * to get around Oracle 10g returning a non-standard object for its TIMESTAMP
     * datatype and a <code>java.sql.Date</code> for DATE columns leaving out the
     * time portion: These columns will explicitly be extracted as standard
     * <code>java.sql.Timestamp</code> object.
     *
     * @param rs    is the ResultSet holding the data
     * @param index is the column index
     * @return the value object
     * @throws java.sql.SQLException if thrown by the JDBC API
     * @see java.sql.Blob
     * @see java.sql.Clob
     * @see java.sql.Timestamp
     */
    @SuppressWarnings({"unchecked"})
    static <T> T getResultSetValue(ResultSet rs, int index) throws SQLException {
        Object obj = rs.getObject(index);
        String className = null;
        if (obj != null) {
            className = obj.getClass().getName();
        }
        if (obj instanceof Blob) {
            obj = rs.getBytes(index);
        } else if (obj instanceof Clob) {
            obj = rs.getString(index);
        } else if (className != null &&
                ("oracle.sql.TIMESTAMP".equals(className) ||
                        "oracle.sql.TIMESTAMPTZ".equals(className))) {
            obj = rs.getTimestamp(index);
        } else if (className != null && className.startsWith("oracle.sql.DATE")) {
            String metaDataClassName = rs.getMetaData().getColumnClassName(index);
            if ("java.sql.Timestamp".equals(metaDataClassName) ||
                    "oracle.sql.TIMESTAMP".equals(metaDataClassName)) {
                obj = rs.getTimestamp(index);
            } else {
                obj = rs.getDate(index);
            }
        } else if (obj instanceof java.sql.Date) {
            if ("java.sql.Timestamp".equals(rs.getMetaData().getColumnClassName(index))) {
                obj = rs.getTimestamp(index);
            }
        }
        return (T) obj;
    }

    /**
     * Determine the column name to use. The column name is determined based on a
     * lookup using ResultSetMetaData.
     * <p>This method implementation takes into account recent clarifications
     * expressed in the JDBC 4.0 specification:
     * <p><i>columnLabel - the label for the column specified with the SQL AS clause.
     * If the SQL AS clause was not specified, then the label is the name of the column</i>.
     *
     * @param resultSetMetaData the current meta data to use
     * @param columnIndex       the index of the column for the look up
     * @return the column name to use
     * @throws java.sql.SQLException in case of lookup failure
     */
    static String lookupColumnName(ResultSetMetaData resultSetMetaData, int columnIndex) throws SQLException {
        String name = resultSetMetaData.getColumnLabel(columnIndex);
        if (name == null || name.length() < 1) {
            name = resultSetMetaData.getColumnName(columnIndex);
        }
        return name;
    }

    private static final byte[] Hexhars = {
            '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f'
    };

    static String toHexString(byte[] b) {
        StringBuilder s = new StringBuilder(2 * b.length);
        for (byte aB : b) {
            int v = aB & 0xff;
            s.append((char) Hexhars[v >> 4]);
            s.append((char) Hexhars[v & 0xf]);
        }
        return s.toString();
    }

    static byte[] readBlob(Blob blob) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            copy(new BufferedInputStream(blob.getBinaryStream()), baos);
            return baos.toByteArray();
        } catch (SQLException e) {
            throw new SqlException(e.getMessage(), e);
        }
    }

    static String readClob(Clob clob) {
        try {
            StringWriter sw = new StringWriter();
            copy(new BufferedReader(clob.getCharacterStream()), sw);
            return sw.toString();
        } catch (SQLException e) {
            throw new SqlException(e.getMessage(), e);
        }
    }

    static byte[] readFully(InputStream is) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copy(new BufferedInputStream(is), baos);
        return baos.toByteArray();
    }

    static String readFully(Reader r) {
        StringWriter sw = new StringWriter();
        copy(new BufferedReader(r), sw);
        return sw.toString();
    }

    private static void copy(InputStream is, OutputStream os) {
        byte[] buffer = new byte[4096];
        int count;
        try {
            while ((count = is.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }
        } catch (IOException e) {
            throw new SqlException(e.getMessage(), e);
        } finally {
            close(is);
            close(os);
        }
    }

    private static void copy(Reader is, Writer os) {
        char[] buffer = new char[4096];
        int count;
        try {
            while ((count = is.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }
        } catch (IOException e) {
            throw new SqlException(e.getMessage(), e);
        } finally {
            close(is);
            close(os);
        }
    }

    private static void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (Exception ignored) {
        }
    }
}

