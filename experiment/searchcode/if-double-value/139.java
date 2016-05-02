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
package ncsa.d2k.modules.core.datatype.table.sparse;

//import ncsa.d2k.modules.projects.vered.sparse.example.SparsePredictionExample;
import ncsa.d2k.modules.core.datatype.table.sparse.examples.
    SparsePredictionExample;
import ncsa.d2k.modules.core.datatype.table.Example;
import ncsa.d2k.modules.core.datatype.table.*;
//import ncsa.d2k.modules.projects.vered.sparse.primitivehash.*;
//import ncsa.d2k.modules.projects.vered.sparse.column.*;
import ncsa.d2k.modules.core.datatype.table.sparse.primitivehash.*;
import ncsa.d2k.modules.core.datatype.table.sparse.columns.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * SparseTestTable is the same as SparsePredictionTable with limited access to
 * the test set rows only.
 */

public class SparseTestTable
    extends SparsePredictionTable
    implements TestTable {

  public SparseTestTable() {
    super();
  }

  public SparseTestTable(SparseExampleTable table) {
    super(table);
  }

  protected SparseTestTable(SparseExampleTable table, boolean flag) {
    super(table, flag);
  }

  public ExampleTable getExampleTable() {
    return new SparseExampleTable(this);
  }

  /*
    public Example getExample(int index){
       return new SparsePredictionExample( (SparseTestTable) getSubset(index, 1));
    }
   */

  public Table getSubset(int pos, int len) {
    //Table t = super.getSubset(pos, len);
    //ExampleTable et  = t.toExampleTable();
    SparseExampleTable temp = (SparseExampleTable)super.getSubset(pos, len);

    //PredictionTableImpl pt = new PredictionTableImpl(et.getNumColumns());
    SparsePredictionTable retVal = new SparseTestTable(temp, true);

    //retVal.setPredictionSet(newpred);
    retVal.predictionColumns = (SparseMutableTable) predictionColumns.getSubset(
        pos, len);
    //copying the prediction indices.
    retVal.predictions = copyArray(predictions);
    retVal.numPredictions = numPredictions;
    //retVal.columns = ((SparseExampleTable)temp).columns;
    //pt.setInputFeatures(newin);
    //pt.setOutputFeatures(newout);
    //pt.setTrainingSet(newtrain);
    //pt.setTestingSet(newtest);
    return retVal;
  }

  public Table getSubset(int[] rows) {
    //Table t = super.getSubset(pos, len);
    //ExampleTable et  = t.toExampleTable();
    SparseExampleTable temp = (SparseExampleTable)super.getSubset(rows);

    //PredictionTableImpl pt = new PredictionTableImpl(et.getNumColumns());
    SparsePredictionTable retVal = new SparseTestTable(temp, true);

    //retVal.setPredictionSet(newpred);
    retVal.predictionColumns = (SparseMutableTable) predictionColumns.getSubset(
        rows);
    //copying the prediction indices.
    retVal.predictions = copyArray(predictions);
    retVal.numPredictions = numPredictions;
    //retVal.columns = ((SparseExampleTable)temp).columns;
    //pt.setInputFeatures(newin);
    //pt.setOutputFeatures(newout);
    //pt.setTrainingSet(newtrain);
    //pt.setTestingSet(newtest);
    return retVal;
  }

  public Table getSubsetByReference(int pos, int len) {
//Table t = super.getSubset(pos, len);
//ExampleTable et  = t.toExampleTable();
    SparseExampleTable temp = (SparseExampleTable)super.getSubsetByReference(
        pos, len);

//PredictionTableImpl pt = new PredictionTableImpl(et.getNumColumns());
    SparsePredictionTable retVal = new SparseTestTable(temp, true);

//retVal.setPredictionSet(newpred);
    retVal.predictionColumns = (SparseMutableTable) predictionColumns.
        getSubsetByReference(
        pos, len);
//copying the prediction indices.
    retVal.predictions = copyArray(predictions);
    retVal.numPredictions = numPredictions;
//retVal.columns = ((SparseExampleTable)temp).columns;
//pt.setInputFeatures(newin);
//pt.setOutputFeatures(newout);
//pt.setTrainingSet(newtrain);
//pt.setTestingSet(newtest);
    return retVal;
  }

  public Table getSubsetByReference(int[] rows) {
    SparseExampleTable temp = (SparseExampleTable)super.getSubsetByReference(
        rows);

//PredictionTableImpl pt = new PredictionTableImpl(et.getNumColumns());
    SparsePredictionTable retVal = new SparseTestTable(temp, true);

//retVal.setPredictionSet(newpred);
    retVal.predictionColumns = (SparseMutableTable) predictionColumns.
        getSubsetByReference(rows);
//copying the prediction indices.
    retVal.predictions = copyArray(predictions);
    retVal.numPredictions = numPredictions;
    return retVal;

  }

  /**
       * Returns an int representation of the data at row no. <codE>row</code> in the
   * test set and column no. <code>column</codE> .
   *
   * This method is the same as getInt(testSet[row], column).
   *
   * @param row       the row index in the test set
   * @param column    the column index
       * @return          an int representation of the data at the specified position.
   */
  public int getInt(int row, int column) {
    return super.getInt(testSet[row], column);
  }

  /**
       * Returns a short representation of the data at row no. <codE>row</code> in the
   * test set and column no. <code>column</codE> .
   *
   * This method is the same as getShort(testSet[row], column).
   *
   * @param row       the row index in the test set
   * @param column    the column index
       * @return          a short representation of the data at the specified position.
   */
  public short getShort(int row, int column) {
    return super.getShort(testSet[row], column);
  }

  /**
       * Returns a long representation of the data at row no. <codE>row</code> in the
   * test set and column no. <code>column</codE> ..
   *
   * This method is the same as getLong(testSet[row], column).
   *
   * @param row       the row index in the test set
   * @param column    the column index
       * @return          a long representation of the data at the specified position.
   */
  public long getLong(int row, int column) {
    return super.getLong(testSet[row], column);
  }

  /**
       * Returns a float representation of the data at row no. <codE>row</code> in the
   * test set and column no. <code>column</codE> .
   *
   * This method is the same as getFloat(testSet[row], column).
   *
   * @param row       the row index in the test set
   * @param column    the column index
       * @return          a float representation of the data at the specified position.
   */
  public float getFloat(int row, int column) {
    return super.getFloat(testSet[row], column);
  }

  /**
       * Returns a double representation of the data at row no. <codE>row</code> in the
   * test set and column no. <code>column</codE> .
   *
   * This method is the same as getDouble(testSet[row], column).
   *
   * @param row       the row index in the test set
   * @param column    the column index
   * @return          a double representation of the data at the specified position.
   */
  public double getDouble(int row, int column) {
    return super.getDouble(testSet[row], column);
  }

  /**
       * Returns a String representation of the data at row no. <codE>row</code> in the
   * test set and column no. <code>column</codE> .
   *
   * This method is the same as getString(testSet[row], column).
   *
   * @param row       the row index in the test set
   * @param column    the column index
   * @return          a String representation of the data at the specified position.
   */
  public String getString(int row, int column) {
    return super.getString(testSet[row], column);
  }

  /**
   * Returns a byte array representation of the data at row no. <codE>row</code> in the
   * test set and column no. <code>column</codE> .
   *
   * This method is the same as getBytes(testSet[row], column).
   *
   * @param row       the row index in the test set
   * @param column    the column index
   * @return          a byte array representation of the data at the specified position.
   */
  public byte[] getBytes(int row, int column) {
    return super.getBytes(testSet[row], column);
  }

  /**
   * Returns a char array representation of the data at row no. <codE>row</code> in the
   * test set and column no. <code>column</codE> .
   *
   * This method is the same as getChars(testSet[row], column).
   *
   * @param row       the row index in the test set
   * @param column    the column index
   * @return          a char array representation of the data at the specified position.
   */
  public char[] getChars(int row, int column) {
    return super.getChars(testSet[row], column);
  }

  /**
       * Returns  boolean representation of the data at row no. <codE>row</code> in the
   * test set and column no. <code>column</codE> .
   *
   * This method is the same as getInt(testSet[row], column).
   *
   * @param row       the row index in the test set
   * @param column    the column index
   * @return          a boolean representation of the data at the specified position.
   */
  public boolean getBoolean(int row, int column) {
    return super.getBoolean(testSet[row], column);
  }

  /**
       * Returns a byte representation of the data at row no. <codE>row</code> in the
   * test set and column no. <code>column</codE> .
   *
   * This method is the same as getByte(testSet[row], column).
   *
   * @param row       the row index in the test set
   * @param column    the column index
       * @return          a byte representation of the data at the specified position.
   */
  public byte getByte(int row, int column) {
    return super.getByte(testSet[row], column);
  }

  /**
       * Returns a char representation of the data at row no. <codE>row</code> in the
   * test set and column no. <code>column</codE> .
   *
   * This method is the same as getChar(testSet[row], column).
   *
   * @param row       the row index in the test set
   * @param column    the column index
       * @return          a char representation of the data at the specified position.
   */
  public char getChar(int row, int column) {
    return super.getChar(testSet[row], column);
  }

  /* returns the actual number of rows in the test set. */
  public int getNumRows() {
    return testSet.length;
  }

  public int getNumExamples() {
    return testSet.length;
  }

  /**
   * Returns an Object representation of the data at row no. <codE>row</code> in the
   * test set and column no. <code>column</codE> .
   *
   * This method is the same as getObject(testSet[row], column).
   *
   * @param row       the row index in the test set
   * @param column    the column index
   * @return          an Object representation of the data at the specified position.
   */
  public Object getObject(int row, int column) {
    return super.getObject(testSet[row], column);
  }

  /* Returns the number od entries in the test set.*/
  public int getNumEntries() {

    int numEntries = 0;
    for (int i = 0; i < testSet.length; i++) {
      numEntries += this.getRowNumEntries(testSet[i]);

    }
    return numEntries;
  }

  /**
   * Copies the content of row index <code>position</codE> in the test set into
   * <code>buffer</code>, and copies the valid columns of this row into <code>
   * indices</cdoe>.
   *
   * @param buffer    an array of some type into which the data is copied.
   * @param position  the row index into the test set, to retrieve its data
   * @param indices   an int array to hold the valid columns of row <codE>position</code>
   */
/* Deprecated
  public void getRow(Object buffer, int position, int[] indices) {
    super.getRow(buffer, testSet[position], indices);
  }
*/

  /**
       * Returns a TestTable with data from row index no. <code> start</code> in the
   * test set through row index no. <code>start+len</code> in the test set.
   *
       * @param start       index number into the test set of the row at which begins
   *                    the subset.
   * @param len         number of consequetive rows to include in the subset.
   /
     public Table getSubset(int start, int len) {
     return getSubset(start, len, true);
     }*/

  /**
   * Returns true if there is a value at row with index  <codE>row</code> and column
   * no. <code>column</code> in the test set.
   *
   * @param row   row index into the test set.
   * @param column  the iinspected column index
   * @return        true if there is a value at row index <code> row</codE> in the test
   *                set and column no. <code>column</code>.
   */
  public boolean doesValueExist(int row, int column) {
    return super.doesValueExist(testSet[row], column);
  }

  /**
   * Returns a deep copy of this table.
   */
  public Table copy() {
    return new SparseTestTable( (SparsePredictionTable)super.copy());
  }

  /**
   * Return the total number of entries in row index <code>position</code> in
   * the testing set.
   */
  public int getRowNumEntries(int position) {
    return super.getRowNumEntries(testSet[position]);

  }

  /**
   * Returns the valid column numbers of row index <code>position</code> in
   * the testing set.
   */
  public int[] getRowIndices(int position) {
    return super.getRowIndices(testSet[position]);
  }

  public int[] getRowIndicesUnsorted(int position) {
    return super.getRowIndicesUnsorted(testSet[position]);
  }

  /* **************************************************
     OVERRIDING SET TYPE PREDICTION METHODS
   **************************************************
   */

  /**
       * sets the value <codE>prediction</code> to row index <codE>row</code> in the
   * testing set.
       * <codE>predictionColIdx</codE> is an index into <code>predictionColumns</code>
   * and not as seen externally of this table.
   *
   * @param prediction    the value to be set.
   * @param row           the row index into the testing set
   * @param predictionColIdx    the index of the prediction column
   *                            in <code>predictionColumns</code>.
   */
  public void setStringPrediction(String prediction, int row,
                                  int predictionColIdx) {
    super.setStringPrediction(prediction, testSet[row], predictionColIdx);
  }

  /**
       * sets the value <codE>prediction</code> to row index <codE>row</code> in the
   * testing set.
       * <codE>predictionColIdx</codE> is an index into <code>predictionColumns</code>
   * and not as seen externally of this table.
   *
   * @param prediction    the value to be set.
   * @param row           the row index into the testing set
   * @param predictionColIdx    the index of the prediction column
   *                            in <code>predictionColumns</code>.
   */
  public void setBooleanPrediction(boolean prediction, int row,
                                   int predictionColIdx) {
    super.setBooleanPrediction(prediction, testSet[row], predictionColIdx);
  }

  /**
       * sets the value <codE>prediction</code> to row index <codE>row</code> in the
   * testing set.
       * <codE>predictionColIdx</codE> is an index into <code>predictionColumns</code>
   * and not as seen externally of this table.
   *
   * @param prediction    the value to be set.
   * @param row           the row index into the testing set
   * @param predictionColIdx    the index of the prediction column
   *                            in <code>predictionColumns</code>.
   */
  public void setCharPrediction(char prediction, int row, int predictionColIdx) {
    super.setCharPrediction(prediction, testSet[row], predictionColIdx);
  }

  /**
       * sets the value <codE>prediction</code> to row index <codE>row</code> in the
   * testing set.
       * <codE>predictionColIdx</codE> is an index into <code>predictionColumns</code>
   * and not as seen externally of this table.
   *
   * @param prediction    the value to be set.
   * @param row           the row index into the testing set
   * @param predictionColIdx    the index of the prediction column
   *                            in <code>predictionColumns</code>.
   */
  public void setBytePrediction(byte prediction, int row, int predictionColIdx) {
    super.setBytePrediction(prediction, testSet[row], predictionColIdx);
  }

  /**
       * sets the value <codE>prediction</code> to row index <codE>row</code> in the
   * testing set.
       * <codE>predictionColIdx</codE> is an index into <code>predictionColumns</code>
   * and not as seen externally of this table.
   *
   * @param prediction    the value to be set.
   * @param row           the row index into the testing set
   * @param predictionColIdx    the index of the prediction column
   *                            in <code>predictionColumns</code>.
   */
  public void setFloatPrediction(float prediction, int row,
                                 int predictionColIdx) {
    super.setFloatPrediction(prediction, testSet[row], predictionColIdx);
  }

  /**
       * sets the value <codE>prediction</code> to row index <codE>row</code> in the
   * testing set.
       * <codE>predictionColIdx</codE> is an index into <code>predictionColumns</code>
   * and not as seen externally of this table.
   *
   * @param prediction    the value to be set.
   * @param row           the row index into the testing set
   * @param predictionColIdx    the index of the prediction column
   *                            in <code>predictionColumns</code>.
   */
  public void setDoublePrediction(double prediction, int row,
                                  int predictionColIdx) {
    super.setDoublePrediction(prediction, testSet[row], predictionColIdx);
  }

  /**
       * sets the value <codE>prediction</code> to row index <codE>row</code> in the
   * testing set.
       * <codE>predictionColIdx</codE> is an index into <code>predictionColumns</code>
   * and not as seen externally of this table.
   *
   * @param prediction    the value to be set.
   * @param row           the row index into the testing set
   * @param predictionColIdx    the index of the prediction column
   *                            in <code>predictionColumns</code>.
   */
  public void setCharsPrediction(char[] prediction, int row,
                                 int predictionColIdx) {
    super.setCharsPrediction(prediction, testSet[row], predictionColIdx);
  }

  /**
       * sets the value <codE>prediction</code> to row index <codE>row</code> in the
   * testing set.
       * <codE>predictionColIdx</codE> is an index into <code>predictionColumns</code>
   * and not as seen externally of this table.
   *
   * @param prediction    the value to be set.
   * @param row           the row index into the testing set
   * @param predictionColIdx    the index of the prediction column
   *                            in <code>predictionColumns</code>.
   */
  public void setBytesPrediction(byte[] prediction, int row,
                                 int predictionColIdx) {
    super.setBytesPrediction(prediction, testSet[row], predictionColIdx);
  }

  /**
       * sets the value <codE>prediction</code> to row index <codE>row</code> in the
   * testing set.
       * <codE>predictionColIdx</codE> is an index into <code>predictionColumns</code>
   * and not as seen externally of this table.
   *
   * @param prediction    the value to be set.
   * @param row           the row index into the testing set
   * @param predictionColIdx    the index of the prediction column
   *                            in <code>predictionColumns</code>.
   */
  public void setObjectPrediction(Object prediction, int row,
                                  int predictionColIdx) {
    super.setObjectPrediction(prediction, testSet[row], predictionColIdx);
  }

  /**
       * sets the value <codE>prediction</code> to row index <codE>row</code> in the
   * testing set.
       * <codE>predictionColIdx</codE> is an index into <code>predictionColumns</code>
   * and not as seen externally of this table.
   *
   * @param prediction    the value to be set.
   * @param row           the row index into the testing set
   * @param predictionColIdx    the index of the prediction column
   *                            in <code>predictionColumns</code>.
   */
  public void setShortPrediction(short prediction, int row,
                                 int predictionColIdx) {
    super.setShortPrediction(prediction, testSet[row], predictionColIdx);
  }

  /**
       * sets the value <codE>prediction</code> to row index <codE>row</code> in the
   * testing set.
       * <codE>predictionColIdx</codE> is an index into <code>predictionColumns</code>
   * and not as seen externally of this table.
   *
   * @param prediction    the value to be set.
   * @param row           the row index into the testing set
   * @param predictionColIdx    the index of the prediction column
   *                            in <code>predictionColumns</code>.
   */
  public void setLongPrediction(long prediction, int row, int predictionColIdx) {
    super.setLongPrediction(prediction, testSet[row], predictionColIdx);
  }

  /**
       * sets the value <codE>prediction</code> to row index <codE>row</code> in the
   * testing set.
       * <codE>predictionColIdx</codE> is an index into <code>predictionColumns</code>
   * and not as seen externally of this table.
   *
   * @param prediction    the value to be set.
   * @param row           the row index into the testing set
   * @param predictionColIdx    the index of the prediction column
   *                            in <code>predictionColumns</code>.
   */
  public void setIntPrediction(int prediction, int row, int predictionColIdx) {
    super.setIntPrediction(prediction, testSet[row], predictionColIdx);
  }

  /**
   * Set an int value in the table.
   * @param data the value to set
   * @param row the row of the table
   * @param column the column of the table
   */
  public void setInt(int data, int row, int column) {
    super.setInt(data, this.testSet[row], column);
  }

  /**
   * Set a short value in the table.
   * @param data the value to set
   * @param row the row of the table
   * @param column the column of the table
   */
  public void setShort(short data, int row, int column) {
    super.setShort(data, this.testSet[row], column);
  }

  /**
   * Set a long value in the table.
   * @param data the value to set
   * @param row the row of the table
   * @param column the column of the table
   */
  public void setLong(long data, int row, int column) {
    super.setLong(data, this.testSet[row], column);
  }

  /**
   * Set a float value in the table.
   * @param data the value to set
   * @param row the row of the table
   * @param column the column of the table
   */
  public void setFloat(float data, int row, int column) {
    super.setFloat(data, this.testSet[row], column);
  }

  /**
   * Set a double value in the table.
   * @param data the value to set
   * @param row the row of the table
   * @param column the column of the table
   */
  public void setDouble(double data, int row, int column) {
    super.setDouble(data, this.testSet[row], column);
  }

  /**
   * Set a String value in the table.
   * @param data the value to set
   * @param row the row of the table
   * @param column the column of the table
   */
  public void setString(String data, int row, int column) {
    super.setString(data, this.testSet[row], column);
  }

  /**
   * Set a byte[] value in the table.
   * @param data the value to set
   * @param row the row of the table
   * @param column the column of the table
   */
  public void setBytes(byte[] data, int row, int column) {
    super.setBytes(data, this.testSet[row], column);
  }

  /**
   * Set a char[] value in the table.
   * @param data the value to set
   * @param row the row of the table
   * @param column the column of the table
   */
  public void setChars(char[] data, int row, int column) {
    super.setChars(data, this.testSet[row], column);
  }

  /**
   * Set a boolean value in the table.
   * @param data the value to set
   * @param row the row of the table
   * @param column the column of the table
   */
  public void setBoolean(boolean data, int row, int column) {
    super.setBoolean(data, this.testSet[row], column);
  }

  /*public PredictionTable toPredictionTable() {
     return this;
    }*/

  ////////////////////////////////////
  // now, TestTableImpl must override all methods that add, insert, and
  // remove rows in order to correctly keep track of its test set.

  public void addRow(int[] newEntry) {
    addTesting();
    super.addRow(newEntry);
  }

  public void addRow(float[] newEntry) {
    addTesting();
    super.addRow(newEntry);
  }

  public void addRow(double[] newEntry) {
    addTesting();
    super.addRow(newEntry);
  }

  public void addRow(long[] newEntry) {
    addTesting();
    super.addRow(newEntry);
  }

  public void addRow(short[] newEntry) {
    addTesting();
    super.addRow(newEntry);
  }

  public void addRow(boolean[] newEntry) {
    addTesting();
    super.addRow(newEntry);
  }

  public void addRow(String[] newEntry) {
    addTesting();
    super.addRow(newEntry);
  }

  public void addRow(char[][] newEntry) {
    addTesting();
    super.addRow(newEntry);
  }

  public void addRow(byte[][] newEntry) {
    addTesting();
    super.addRow(newEntry);
  }

  public void addRow(Object[] newEntry) {
    addTesting();
    super.addRow(newEntry);
  }

  public void addRow(byte[] newEntry) {
    addTesting();
    super.addRow(newEntry);
  }

  public void addRow(char[] newEntry) {
    addTesting();
    super.addRow(newEntry);
  }

  public void insertRow(int[] newEntry, int position) {
    insertTesting(testSet[position]);
    super.insertRow(newEntry, testSet[position]);
  }

  public void insertRow(float[] newEntry, int position) {
    insertTesting(testSet[position]);
    super.insertRow(newEntry, testSet[position]);
  }

  public void insertRow(double[] newEntry, int position) {
    insertTesting(testSet[position]);
    super.insertRow(newEntry, testSet[position]);
  }

  public void insertRow(long[] newEntry, int position) {
    insertTesting(testSet[position]);
    super.insertRow(newEntry, testSet[position]);
  }

  public void insertRow(short[] newEntry, int position) {
    insertTesting(testSet[position]);
    super.insertRow(newEntry, testSet[position]);
  }

  public void insertRow(boolean[] newEntry, int position) {
    insertTesting(testSet[position]);
    super.insertRow(newEntry, testSet[position]);
  }

  public void insertRow(String[] newEntry, int position) {
    insertTesting(testSet[position]);
    super.insertRow(newEntry, testSet[position]);
  }

  public void insertRow(char[][] newEntry, int position) {
    insertTesting(testSet[position]);
    super.insertRow(newEntry, testSet[position]);
  }

  public void insertRow(byte[][] newEntry, int position) {
    insertTesting(testSet[position]);
    super.insertRow(newEntry, testSet[position]);
  }

  public void insertRow(Object[] newEntry, int position) {
    insertTesting(testSet[position]);
    super.insertRow(newEntry, testSet[position]);
  }

  public void insertRow(byte[] newEntry, int position) {
    insertTesting(testSet[position]);
    super.insertRow(newEntry, testSet[position]);
  }

  public void insertRow(char[] newEntry, int position) {
    insertTesting(testSet[position]);
    super.insertRow(newEntry, testSet[position]);
  }

  public void removeRow(int row) {
    super.removeRow(testSet[row]);
    removeTesting(row);
  }

  public void removeRows(int start, int len) {
    for (int i = 0; i < len; i++) {
      removeRow(start);
    }
  }

  public void removeRowsByFlag(boolean[] flags) {
    int offset = 0;
    for (int i = 0; i < flags.length; i++) {
      if (flags[i]) {
        removeRow(i - offset++);
      }
    }
  }

  public void removeRowsByIndex(int[] indices) {
    int offset = 0;
    for (int i = 0; i < indices.length; i++) {
      removeRow(indices[i] - offset++);
    }
  }

  /**
   * Set the row at the given position to an array of int data.
   *	@param newEntry the new values of the row.
   *	@param position the position to set
   */
  public void setRow(int[] newEntry, int position) {
    super.setRow(newEntry, testSet[position]);
  }

  /**
   * Set the row at the given position to an array of float data.
   *	@param newEntry the new values of the row.
   *	@param position the position to set
   */
  public void setRow(float[] newEntry, int position) {
    super.setRow(newEntry, testSet[position]);
  }

  /**
   * Set the row at the given position to an array of double data.
   *	@param newEntry the new values of the row.
   *	@param position the position to set
   */
  public void setRow(double[] newEntry, int position) {
    super.setRow(newEntry, testSet[position]);
  }

  /**
   * Set the row at the given position to an array of long data.
   *	@param newEntry the new values of the row.
   *	@param position the position to set
   */
  public void setRow(long[] newEntry, int position) {
    super.setRow(newEntry, testSet[position]);
  }

  /**
   * Set the row at the given position to an array of short data.
   *	@param newEntry the new values of the row.
   *	@param position the position to set
   */
  public void setRow(short[] newEntry, int position) {
    super.setRow(newEntry, testSet[position]);
  }

  /**
   * Set the row at the given position to an array of boolean data.
   *	@param newEntry the new values of the row.
   *	@param position the position to set
   */
  public void setRow(boolean[] newEntry, int position) {
    super.setRow(newEntry, testSet[position]);
  }

  /**
   * Set the row at the given position to an array of String data.
   *	@param newEntry the new values of the row.
   *	@param position the position to set
   */
  public void setRow(String[] newEntry, int position) {
    super.setRow(newEntry, testSet[position]);
  }

  /**
   * Set the row at the given position to an array of char[] data.
   *	@param newEntry the new values of the row.
   *	@param position the position to set
   */
  public void setRow(char[][] newEntry, int position) {
    super.setRow(newEntry, testSet[position]);
  }

  /**
   * Set the row at the given position to an array of byte[] data.
   *	@param newEntry the new values of the row.
   *	@param position the position to set
   */
  public void setRow(byte[][] newEntry, int position) {
    super.setRow(newEntry, testSet[position]);
  }

  /**
   * Set the row at the given position to an array of Object data.
   *	@param newEntry the new values of the row.
   *	@param position the position to set
   */
  public void setRow(Object[] newEntry, int position) {
    super.setRow(newEntry, testSet[position]);
  }

  /**
   * Set the row at the given position to an array of byte data.
   *	@param newEntry the new values of the row.
   *	@param position the position to set
   */
  public void setRow(byte[] newEntry, int position) {
    super.setRow(newEntry, testSet[position]);
  }

  /**
   * Set the row at the given position to an array of char data.
   *	@param newEntry the new values of the row.
   *	@param position the position to set
   */
  public void setRow(char[] newEntry, int position) {
    super.setRow(newEntry, testSet[position]);
  }

  /**
   Swap the positions of two rows.
   @param pos1 the first row to swap
   @param pos2 the second row to swap
   */
  public void swapRows(int pos1, int pos2) {
    super.swapRows(trainSet[pos1], trainSet[pos2]);
    this.swapTestTrain(pos1, pos2);
  }

  // maintenance methods:

  private void addTesting() {

    int[] newTestingSet = new int[testSet.length + 1];
    for (int i = 0; i < testSet.length; i++) {
      newTestingSet[i] = testSet[i];
    }
    newTestingSet[testSet.length] = super.getNumRows();

    testSet = newTestingSet;

  }

  private void insertTesting(int tablePosition) {

    for (int i = 0; i < testSet.length; i++) {
      if (testSet[i] >= tablePosition) {
        testSet[i]++;

      }
    }
    int[] newTestingSet = new int[testSet.length + 1];
    for (int i = 0; i < testSet.length; i++) {
      newTestingSet[i] = testSet[i];
    }
    newTestingSet[testSet.length] = tablePosition;

    Arrays.sort(newTestingSet);

    testSet = newTestingSet;

  }

  private void removeTesting(int testingPosition) {

    int[] newTestingSet = new int[testSet.length - 1];
    for (int i = 0; i < testingPosition; i++) {
      newTestingSet[i] = testSet[i];

    }
    for (int i = testingPosition + 1; i < testSet.length; i++) {
      newTestingSet[i - 1] = testSet[i] - 1;

    }
    testSet = newTestingSet;

  }

  public void setColumn(char[] data, int pos) {
     //CharColumn cc = new CharColumn(data);
     //setColumn(cc, pos);
     for(int i = 0; i < data.length; i++) {
       setChar(data[i], i,  pos);
     }
  }
  public void setColumn(byte[] data, int pos) {
     //ByteColumn bc = new ByteColumn(data);
     //setColumn(bc, pos);
     for(int i = 0; i < data.length; i++) {
       setByte(data[i], i,  pos);
     }
  }

  public void setColumn(int[] data, int pos) {
     //IntColumn ic = new IntColumn(data);
     //setColumn(ic, pos);
     for(int i = 0; i < data.length; i++) {
       setInt(data[i], i,  pos);
     }
  }
  public void setColumn(float[] data, int pos) {
     //FloatColumn ic = new FloatColumn(data);
     //setColumn(ic, pos);
     for(int i = 0; i < data.length; i++) {
       setFloat(data[i], i,  pos);
     }
  }
  public void setColumn(double[] data, int pos) {
     //DoubleColumn ic = new DoubleColumn(data);
     //setColumn(ic, pos);
     for(int i = 0; i < data.length; i++) {
       setDouble(data[i], i,  pos);
     }
  }
  public void setColumn(long[] data, int pos) {
     //LongColumn ic = new LongColumn(data);
     //setColumn(ic, pos);
     for(int i = 0; i < data.length; i++) {
       setDouble(data[i], i,  pos);
     }
  }
  public void setColumn(short[] data, int pos) {
     //ShortColumn ic = new ShortColumn(data);
     //setColumn(ic, pos);
     for(int i = 0; i < data.length; i++) {
       setShort(data[i], i,  pos);
     }
  }
  public void setColumn(String[] data, int pos) {
     //StringColumn ic = new StringColumn(data);
     //setColumn(ic, pos);
     for(int i = 0; i < data.length; i++) {
       setString(data[i], i,  pos);
     }
  }
  public void setColumn(byte[][] data, int pos) {
     //ContinuousByteArrayColumn bc = new ContinuousByteArrayColumn(data);
     //setColumn(bc, pos);
     for(int i = 0; i < data.length; i++) {
       setBytes(data[i], i,  pos);
     }
  }
  public void setColumn(char[][] data, int pos) {
     //ContinuousCharArrayColumn cc = new ContinuousCharArrayColumn(data);
     //setColumn(cc, pos);
     for(int i = 0; i < data.length; i++) {
       setChars(data[i], i,  pos);
     }
  }
  public void setColumn(Object[] data, int pos) {
     //ObjectColumn ic = new ObjectColumn(data);
     //setColumn(ic, pos);
     for(int i = 0; i < data.length; i++) {
       setObject(data[i], i,  pos);
     }
  }
  public void setColumn(boolean[] data, int pos) {
     //BooleanColumn ic = new BooleanColumn(data);
     //ic.setLabel(getColumnLabel(pos));
     //ic.setComment(getColumnComment(pos));
     //setColumn(ic, pos);
     for(int i = 0; i < data.length; i++) {
       setBoolean(data[i], i,  pos);
     }
  }


} //SparseTestTable
/*overriding the addRow(type[] arr, int[] indices) methods*/
/*
    public void addRow(int[] newEntry, int[] indices) {
      testSet.add(super.getNumRows());
      super.addRow(newEntry, indices);
   }
   public void addRow(float[] newEntry, int[] indices) {
      testSet.add(super.getNumRows());
      super.addRow(newEntry, indices);
   }
   public void addRow(double[] newEntry, int[] indices) {
      testSet.add(super.getNumRows());
      super.addRow(newEntry, indices);
   }
   public void addRow(long[] newEntry, int[] indices) {
      testSet.add(super.getNumRows());
      super.addRow(newEntry, indices);
   }
   public void addRow(short[] newEntry, int[] indices) {
      testSet.add(super.getNumRows());
      super.addRow(newEntry, indices);
   }
   public void addRow(boolean[] newEntry, int[] indices) {
       testSet.add(super.getNumRows());
      super.addRow(newEntry, indices);
   }
   public void addRow(String[] newEntry, int[] indices) {
      testSet.add(super.getNumRows());
      super.addRow(newEntry, indices);
   }
   public void addRow(char[][] newEntry, int[] indices) {
      testSet.add(super.getNumRows());
      super.addRow(newEntry, indices);
   }
   public void addRow(byte[][] newEntry, int[] indices) {
      testSet.add(super.getNumRows());
      super.addRow(newEntry, indices);
   }
   public void addRow(Object[] newEntry, int[] indices) {
      testSet.add(super.getNumRows());
      super.addRow(newEntry, indices);
   }
   public void addRow(byte[] newEntry, int[] indices) {
       testSet.add(super.getNumRows());
      super.addRow(newEntry, indices);
   }
   public void addRow(char[] newEntry, int[] indices) {
      testSet.add(super.getNumRows());
      super.addRow(newEntry, indices);
   }
 */
/* overrding insertRow(type[] arr, int pos, int[] indices) methods */
/*
  public void insertRow(int[] newEntry, int position, int[] indices) {
      incrementTrainTest(position);
      testSet.add(position);
      super.insertRow(newEntry, getTestingSet()[position], indices);
   }
   public void insertRow(float[] newEntry, int position, int[] indices) {
      incrementTrainTest(position);
      testSet.add(position);
      super.insertRow(newEntry, getTestingSet()[position], indices);
   }
   public void insertRow(double[] newEntry, int position, int[] indices) {
      incrementTrainTest(position);
      testSet.add(position);
      super.insertRow(newEntry, getTestingSet()[position], indices);
   }
   public void insertRow(long[] newEntry, int position, int[] indices) {
      incrementTrainTest(position);
      testSet.add(position);
      super.insertRow(newEntry, getTestingSet()[position], indices);
   }
   public void insertRow(short[] newEntry, int position, int[] indices) {
      incrementTrainTest(position);
      testSet.add(position);
      super.insertRow(newEntry, getTestingSet()[position], indices);
   }
   public void insertRow(boolean[] newEntry, int position, int[] indices) {
      incrementTrainTest(position);
      testSet.add(position);
      super.insertRow(newEntry, getTestingSet()[position], indices);
   }
   public void insertRow(String[] newEntry, int position, int[] indices) {
      incrementTrainTest(position);
      testSet.add(position);
      super.insertRow(newEntry, getTestingSet()[position], indices);
   }
   public void insertRow(char[][] newEntry, int position, int[] indices) {
      incrementTrainTest(position);
      testSet.add(position);
      super.insertRow(newEntry, getTestingSet()[position], indices);
   }
   public void insertRow(byte[][] newEntry, int position, int[] indices) {
      incrementTrainTest(position);
      testSet.add(position);
      super.insertRow(newEntry, getTestingSet()[position], indices);
   }
   public void insertRow(Object[] newEntry, int position, int[] indices) {
      incrementTrainTest(position);
      testSet.add(position);
      super.insertRow(newEntry, getTestingSet()[position], indices);
   }
   public void insertRow(byte[] newEntry, int position, int[] indices) {
      incrementTrainTest(position);
      testSet.add(position);
      super.insertRow(newEntry, getTestingSet()[position], indices);
   }
   public void insertRow(char[] newEntry, int position, int[] indices) {
      incrementTrainTest(position);
      testSet.add(position);
      super.insertRow(newEntry, getTestingSet()[position], indices);
   }
 */
/* overriding removeRow methods */
/*
    public void removeRow(int row) {
      super.removeRow(getTestingSet()[row]);
      testSet.remove(row);
   }
   public void removeRows(int start, int len) {
      for (int i=0; i<len; i++)
 super.removeRow(start+i);
   }
   public void removeRowsByFlag(boolean[] flags) {
      for (int i = 0; i < flags.length; i++)
         if (flags[i])
            removeRow(i);
   }
   public void removeRowsByIndex(int[] indices) {
      for (int i = 0; i < indices.length; i++)
         removeRow(indices[i]);
   }
 */
>>>>>>> 76aa07461566a5976980e6696204781271955163
