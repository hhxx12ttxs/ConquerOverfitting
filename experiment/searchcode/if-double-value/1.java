<<<<<<< HEAD
/**
 * Copyright (c) 2013, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.controlsfx.control.spreadsheet;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

/**
 * When instantiating a {@link SpreadsheetCell}, its SpreadsheetCellType will
 * specify which values the cell can accept as user input, and which
 * {@link SpreadsheetCellEditor} it will use to receive user input. <br>
 * Different static methods are provided in order to give you access to basic
 * types, and to create {@link SpreadsheetCell} easily:
 * <ul>
 * <li><b>String</b>: Accessible with
 * {@link SpreadsheetCellType.StringType#createCell(int, int, int, int, String)}
 * .</li>
 * <li><b>List</b>: Accessible with
 * {@link SpreadsheetCellType.ListType#createCell(int, int, int, int, String)}.</li>
 * <li><b>Double</b>: Accessible with
 * {@link SpreadsheetCellType.DoubleType#createCell(int, int, int, int, Double)}
 * .</li>
 * <li><b>Integer</b>: Accessible with
 * {@link SpreadsheetCellType.IntegerType#createCell(int, int, int, int, Integer)}
 * .</li>
 * <li><b>Date</b>: Accessible with
 * {@link SpreadsheetCellType.DateType#createCell(int, int, int, int, LocalDate)}
 * .</li>
 * </ul>
 * 
 * <h3>Value verification</h3> You can specify two levels of verification in your
 * types. <br>
 * <ul>
 * <li>The first one is defined by {@link #match(Object)}. It is the first level
 * that tells whether or not the given value should be accepted or not. Trying
 * to set a String into a Double will return false for example. This method will
 * be use by the {@link SpreadsheetView} when trying to set values for example.
 * <br>
 * </li>
 * <li>The second level is defined by {@link #isError(Object)}. This is more
 * subtle and allow you to tell whether the given value is coherent or not
 * regarding the policy you gave. You can just make a {@link SpreadsheetCell}
 * call this method when its value has changed in order to react accordingly if
 * the value is in error. (see example below).</li>
 * </ul>
 * <h3>Converter</h3> You will have to specify a converter for your type. It
 * will handle all the conversion between your real value type (Double, Integer,
 * LocalDate etc) and its string representation for the cell. <br>
 * You can either use a pre-built {@link StringConverter} or our
 * {@link StringConverterWithFormat}. This one just add one method (
 * {@link StringConverterWithFormat#toStringFormat(Object, String)} which will
 * convert your value with a String format (found in
 * {@link SpreadsheetCell#getFormat()}).
 * 
 * <h3>Example</h3> You can create several types which are using the same
 * editor. Suppose you want to handle Double values. You will implement the
 * {@link #createEditor(SpreadsheetView)} method and use the
 * {@link SpreadsheetCellEditor.DoubleEditor}. <br/>
 * 
 * Then for each type you will provide your own policy in {@link #match(Object)}
 * and in {@link #isError(Object)}, which most of the time will use your
 * {@link #converter}. <br>
 * 
 * Here is an example of how to create a {@link StringConverterWithFormat} :
 * 
 * 
 * 
 * <pre>
 * 
 * StringConverterWithFormat specialConverter = new StringConverterWithFormat&lt;Double&gt;(new DoubleStringConverter()) {
 *            &#64;Override
 *             public String toString(Double item) {
 *                  //We just redirect to the other method.
 *                 return toStringFormat(item, "");
 *             }
 * 
 *             &#64;Override
 *             public String toStringFormat(Double item, String format) {
 *                 if (item == null || Double.isNaN(item)) {
 *                     return missingLabel; // For example return something else that an empty cell.
 *                 } else{
 *                     if (!("").equals(format) && !Double.isNaN(item)) {
 *                     //We format here the value
 *                         return new DecimalFormat(format).format(item);
 *                     } else {
 *                     //We call the DoubleStringConverter that we gave in argument
 *                         return myConverter.toString(item);
 *                     }
 *                 }
 *             }
 * 
 *            &#64;Override
 *             public Double fromString(String str) {
 *                 if (str == null || str.isEmpty()) {
 *                     return Double.NaN;
 *                 } else {
 *                     try {
 *                         //Just returning the value
 *                         Double myDouble = Double.parseDouble(str);
 *                         return myDouble;
 * 
 *                     } catch (NumberFormatException e) {
 *                         return myConverter.fromString(str);
 *                     }
 *                 }
 *             }
 *         }
 * 
 * </pre>
 * 
 * And then suppose you only want to accept double values between 0 and 100, and
 * that a value superior to 10 is abnormal. <br>
 * 
 * <pre>
 * &#064;Override
 * public boolean isError(Object value) {
 *     if (value instanceof Double) {
 *         if ((Double) value &gt; 0 &amp;&amp; (Double) value &lt; 10) {
 *             return false;
 *         }
 *         return true;
 *     }
 *     return true;
 * }
 * 
 * &#064;Override
 * public boolean match(Object value) {
 *     if (value instanceof Double) {
 *         return true;
 *     } else {
 *         try {
 *             Double convertedValue = converter.fromString(value == null ? null : value.toString());
 *             if (convertedValue &gt;= 0 &amp;&amp; convertedValue &lt;= 100)
 *                 return true;
 *             else
 *                 return false;
 *         } catch (Exception e) {
 *             return false;
 *         }
 *     }
 * }
 * </pre>
 * 
 * @see SpreadsheetView
 * @see SpreadsheetCellEditor
 * @see SpreadsheetCell
 */
public abstract class SpreadsheetCellType<T> {
    /** An instance of converter from string to cell type. */
    protected StringConverter<T> converter;

    /**
     * Default constructor.
     */
    public SpreadsheetCellType() {

    }

    /**
     * Constructor with the StringConverter directly provided.
     * 
     * @param converter
     *            The converter to use
     */
    public SpreadsheetCellType(StringConverter<T> converter) {
        this.converter = converter;
    }

    /**
     * Creates an editor for this type of cells.
     * 
     * @param view
     *            the spreadsheet that will own this editor
     * @return the editor instance
     */
    public abstract SpreadsheetCellEditor createEditor(SpreadsheetView view);

    /**
     * Return a string representation of the given item for the
     * {@link SpreadsheetView} to display using the inner
     * {@link SpreadsheetCellType#converter} and the specified format.
     * 
     * @param object
     * @param format
     * @return a string representation of the given item.
     */
    public String toString(T object, String format) {
        return toString(object);
    }

    /**
     * Return a string representation of the given item for the
     * {@link SpreadsheetView} to display using the inner
     * {@link SpreadsheetCellType#converter}.
     * 
     * @param object
     * @return a string representation of the given item.
     */
    public abstract String toString(T object);

    /**
     * Verify that the upcoming value can be set to the current cell. This is
     * the first level of verification to prevent affecting a text to a double
     * or a double to a date. For closer verification, use
     * {@link #isError(Object)}.
     * 
     * @param value
     *            the value to test
     * @return true if it matches.
     */
    public abstract boolean match(Object value);

    /**
     * Returns true if the value is an error regarding the specification of its
     * type.
     * 
     * @param value
     * @return true if the value is an error.
     */
    public boolean isError(Object value) {
        return false;
    }

    /**
     * This method will be called when a commit is happening.<br/>
     * This method will try to convert the value, be sure to call
     * {@link #match(Object)} before to see if this method will succeed.
     * 
     * @param value
     * @return null if not valid or the correct value otherwise.
     */
    public abstract T convertValue(Object value);

    /**
     * The {@link SpreadsheetCell} {@link Object} type instance.
     */
    public static final SpreadsheetCellType<Object> OBJECT = new ObjectType();

    /**
     * The {@link SpreadsheetCell} {@link Object} type base class.
     */
    public static class ObjectType extends SpreadsheetCellType<Object> {

        public ObjectType() {
            this(new StringConverterWithFormat<Object>() {
                @Override
                public Object fromString(String arg0) {
                    return arg0;
                }

                @Override
                public String toString(Object arg0) {
                    return arg0 == null ? "" : arg0.toString(); //$NON-NLS-1$
                }
            });
        }

        public ObjectType(StringConverterWithFormat<Object> converter) {
            super(converter);
        }

        @Override
        public String toString() {
            return "object"; //$NON-NLS-1$
        }

        @Override
        public boolean match(Object value) {
            return true;
        }

        /**
        * Creates a cell that hold an Object at the specified position, with the
        * specified row/column span.
        * 
        * @param row
        *            row number
        * @param column
        *            column number
        * @param rowSpan
        *            rowSpan (1 is normal)
        * @param columnSpan
        *            ColumnSpan (1 is normal)
        * @param value
        *            the value to display
        * @return a {@link SpreadsheetCell}
        */
        public SpreadsheetCell createCell(final int row, final int column, final int rowSpan, final int columnSpan,
                final Object value) {
            SpreadsheetCell cell = new SpreadsheetCellBase(row, column, rowSpan, columnSpan, this);
            cell.setItem(value);
            return cell;
        }

        @Override
        public SpreadsheetCellEditor createEditor(SpreadsheetView view) {
            return new SpreadsheetCellEditor.ObjectEditor(view);
        }

        @Override
        public Object convertValue(Object value) {
            return value;
        }

        @Override
        public String toString(Object item) {
            return converter.toString(item);
        }

    };

    /**
     * The {@link SpreadsheetCell} {@link String} type instance.
     */
    public static final StringType STRING = new StringType();

    /**
     * The {@link SpreadsheetCell} {@link String} type base class.
     */
    public static class StringType extends SpreadsheetCellType<String> {

        public StringType() {
            this(new DefaultStringConverter());
        }

        public StringType(StringConverter<String> converter) {
            super(converter);
        }

        @Override
        public String toString() {
            return "string"; //$NON-NLS-1$
        }

        @Override
        public boolean match(Object value) {
            return true;
        }

        /**
        * Creates a cell that hold a String at the specified position, with the
        * specified row/column span.
        * 
        * @param row
        *            row number
        * @param column
        *            column number
        * @param rowSpan
        *            rowSpan (1 is normal)
        * @param columnSpan
        *            ColumnSpan (1 is normal)
        * @param value
        *            the value to display
        * @return a {@link SpreadsheetCell}
        */
        public SpreadsheetCell createCell(final int row, final int column, final int rowSpan, final int columnSpan,
                final String value) {
            SpreadsheetCell cell = new SpreadsheetCellBase(row, column, rowSpan, columnSpan, this);
            cell.setItem(value);
            return cell;
        }

        @Override
        public SpreadsheetCellEditor createEditor(SpreadsheetView view) {
            return new SpreadsheetCellEditor.StringEditor(view);
        }

        @Override
        public String convertValue(Object value) {
            String convertedValue = converter.fromString(value == null ? null : value.toString());
            if (convertedValue == null || convertedValue.equals("")) { //$NON-NLS-1$
                return null;
            }
            return convertedValue;
        }

        @Override
        public String toString(String item) {
            return converter.toString(item);
        }

    };

    /**
     * The {@link SpreadsheetCell} {@link Double} type instance.
     */
    public static final DoubleType DOUBLE = new DoubleType();

    /**
     * The {@link SpreadsheetCell} {@link Double} type base class.
     */
    public static class DoubleType extends SpreadsheetCellType<Double> {

        public DoubleType() {

            this(new StringConverterWithFormat<Double>(new DoubleStringConverter()) {
                @Override
                public String toString(Double item) {
                    return toStringFormat(item, ""); //$NON-NLS-1$
                }

                @Override
                public Double fromString(String str) {
                    if (str == null || str.isEmpty() || "NaN".equals(str)) { //$NON-NLS-1$
                        return Double.NaN;
                    } else {
                        return myConverter.fromString(str);
                    }
                }

                @Override
                public String toStringFormat(Double item, String format) {
                    try {
                        if (item == null || Double.isNaN(item)) {
                            return ""; //$NON-NLS-1$
                        } else {
                            return new DecimalFormat(format).format(item);
                        }
                    } catch (Exception ex) {
                        return myConverter.toString(item);
                    }
                }
            });
        }

        public DoubleType(StringConverter<Double> converter) {
            super(converter);
        }

        @Override
        public String toString() {
            return "double"; //$NON-NLS-1$
        }

        /**
        * Creates a cell that hold a Double at the specified position, with the
        * specified row/column span.
        * 
        * @param row
        *            row number
        * @param column
        *            column number
        * @param rowSpan
        *            rowSpan (1 is normal)
        * @param columnSpan
        *            ColumnSpan (1 is normal)
        * @param value
        *            the value to display
        * @return a {@link SpreadsheetCell}
        */
        public SpreadsheetCell createCell(final int row, final int column, final int rowSpan, final int columnSpan,
                final Double value) {
            SpreadsheetCell cell = new SpreadsheetCellBase(row, column, rowSpan, columnSpan, this);
            cell.setItem(value);
            return cell;
        }

        @Override
        public SpreadsheetCellEditor createEditor(SpreadsheetView view) {
            return new SpreadsheetCellEditor.DoubleEditor(view);
        }

        @Override
        public boolean match(Object value) {
            if (value instanceof Double)
                return true;
            else {
                try {
                    converter.fromString(value == null ? null : value.toString());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }

        @Override
        public Double convertValue(Object value) {
            if (value instanceof Double)
                return (Double) value;
            else {
                try {
                    return converter.fromString(value == null ? null : value.toString());
                } catch (Exception e) {
                    return null;
                }
            }
        }

        @Override
        public String toString(Double item) {
            return converter.toString(item);
        }

        @Override
        public String toString(Double item, String format) {
            return ((StringConverterWithFormat<Double>) converter).toStringFormat(item, format);
        }
    };

    /**
     * The {@link SpreadsheetCell} {@link Integer} type instance.
     */
    public static final IntegerType INTEGER = new IntegerType();

    /**
     * The {@link SpreadsheetCell} {@link Integer} type base class.
     */
    public static class IntegerType extends SpreadsheetCellType<Integer> {

        public IntegerType() {
            this(new IntegerStringConverter() {
                @Override
                public String toString(Integer item) {
                    if (item == null || Double.isNaN(item)) {
                        return ""; //$NON-NLS-1$
                    } else {
                        return super.toString(item);
                    }
                }

                @Override
                public Integer fromString(String str) {
                    if (str == null || str.isEmpty() || "NaN".equals(str)) { //$NON-NLS-1$
                        return null;
                    } else {
                        // We try to integrate Double if possible by truncating
                        // them
                        try {
                            Double temp = Double.parseDouble(str);
                            return temp.intValue();
                        } catch (Exception e) {
                            return super.fromString(str);
                        }
                    }
                }
            });
        }

        public IntegerType(IntegerStringConverter converter) {
            super(converter);
        }

        @Override
        public String toString() {
            return "Integer"; //$NON-NLS-1$
        }

        /**
        * Creates a cell that hold a Integer at the specified position, with the
        * specified row/column span.
        * 
        * @param row
        *            row number
        * @param column
        *            column number
        * @param rowSpan
        *            rowSpan (1 is normal)
        * @param columnSpan
        *            ColumnSpan (1 is normal)
        * @param value
        *            the value to display
        * @return a {@link SpreadsheetCell}
        */
        public SpreadsheetCell createCell(final int row, final int column, final int rowSpan, final int columnSpan,
                final Integer value) {
            SpreadsheetCell cell = new SpreadsheetCellBase(row, column, rowSpan, columnSpan, this);
            cell.setItem(value);
            return cell;
        }

        @Override
        public SpreadsheetCellEditor createEditor(SpreadsheetView view) {
            return new SpreadsheetCellEditor.IntegerEditor(view);
        }

        @Override
        public boolean match(Object value) {
            if (value instanceof Integer)
                return true;
            else {
                try {
                    converter.fromString(value == null ? null : value.toString());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }

        @Override
        public Integer convertValue(Object value) {
            if (value instanceof Integer)
                return (Integer) value;
            else {
                try {
                    return converter.fromString(value == null ? null : value.toString());
                } catch (Exception e) {
                    return null;
                }
            }
        }

        @Override
        public String toString(Integer item) {
            return converter.toString(item);
        }
    };

    /**
     * Creates a {@link ListType}.
     * 
     * @param items
     *            the list items
     * @return the instance
     */
    public static final ListType LIST(final List<String> items) {
        return new ListType(items);
    }

    /**
     * The {@link SpreadsheetCell} {@link List} type base class.
     */
    public static class ListType extends SpreadsheetCellType<String> {
        protected final List<String> items;

        public ListType(final List<String> items) {
            super(new DefaultStringConverter() {
                @Override
                public String fromString(String str) {
                    if (str != null && items.contains(str)) {
                        return str;
                    } else {
                        return null;
                    }
                }

            });
            this.items = items;
        }

        @Override
        public String toString() {
            return "list"; //$NON-NLS-1$
        }

        /**
        * Creates a cell that hold a String at the specified position, with the
        * specified row/column span.
        * 
        * @param row
        *            row number
        * @param column
        *            column number
        * @param rowSpan
        *            rowSpan (1 is normal)
        * @param columnSpan
        *            ColumnSpan (1 is normal)
        * @param value
        *            the value to display
        * @return a {@link SpreadsheetCell}
        */
        public SpreadsheetCell createCell(final int row, final int column, final int rowSpan, final int columnSpan,
                String value) {
            SpreadsheetCell cell = new SpreadsheetCellBase(row, column, rowSpan, columnSpan, this);
            if (items != null && items.size() > 0) {
                if (value != null && items.contains(value)) {
                    cell.setItem(value);
                } else {
                    cell.setItem(items.get(0));
                }
            }
            return cell;
        }

        @Override
        public SpreadsheetCellEditor createEditor(SpreadsheetView view) {
            return new SpreadsheetCellEditor.ListEditor<>(view, items);
        }

        @Override
        public boolean match(Object value) {
            if (value instanceof String && items.contains(value.toString()))
                return true;
            else
                return items.contains(value == null ? null : value.toString());
        }

        @Override
        public String convertValue(Object value) {
            return converter.fromString(value == null ? null : value.toString());
        }

        @Override
        public String toString(String item) {
            return converter.toString(item);
=======
/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://www.phonegap.com/about/license/ for full text.
 *
 * Copyright (c) 2011, IBM Corporation
 */

package blackberry.common.util.json4j;

import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

import blackberry.common.util.json4j.internal.NumberUtil;

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
//        if (!StringWriter.class.isAssignableFrom(writerClass) &&
//            !CharArrayWriter.class.isAssignableFrom(writerClass) &&
//            !BufferedWriter.class.isAssignableFrom(writerClass)) {
//            writer = new BufferedWriter(writer);
//        }
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
            if (b) 
                writer.write("true");
            else 
                writer.write("false");

        } else if (inObject) {
            if (keyPlaced) {
                if (b) 
                    writer.write("true");
                else 
                    writer.write("false");
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
            writer.write(Integer.toString(s));
        } else if (inObject) {
            if (keyPlaced) {
                writer.write(Integer.toString(s));
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
>>>>>>> 76aa07461566a5976980e6696204781271955163
        }
    }

    /**
<<<<<<< HEAD
     * The {@link SpreadsheetCell} {@link LocalDate} type instance.
     */
    public static final DateType DATE = new DateType();

    /**
     * The {@link SpreadsheetCell} {@link LocalDate} type base class.
     */
    public static class DateType extends SpreadsheetCellType<LocalDate> {

        /**
         * Creates a new DateType.
         */
        public DateType() {
            this(new StringConverterWithFormat<LocalDate>() {
                @Override
                public String toString(LocalDate item) {
                    return toStringFormat(item, ""); //$NON-NLS-1$
                }

                @Override
                public LocalDate fromString(String str) {
                    try {
                        return LocalDate.parse(str);
                    } catch (Exception e) {
                        return null;
                    }
                }

                @Override
                public String toStringFormat(LocalDate item, String format) {
                    if (("").equals(format)) { //$NON-NLS-1$
                        return item.toString();
                    } else if (item != null) {
                        return item.format(DateTimeFormatter.ofPattern(format));
                    } else {
                        return ""; //$NON-NLS-1$
                    }
                }
            });
        }

        public DateType(StringConverter<LocalDate> converter) {
            super(converter);
        }

        @Override
        public String toString() {
            return "date"; //$NON-NLS-1$
        }

        /**
        * Creates a cell that hold a LocalDate at the specified position, with the
        * specified row/column span.
        * 
        * @param row
        *            row number
        * @param column
        *            column number
        * @param rowSpan
        *            rowSpan (1 is normal)
        * @param columnSpan
        *            ColumnSpan (1 is normal)
        * @param value
        *            the value to display
        * @return a {@link SpreadsheetCell}
        */
        public SpreadsheetCell createCell(final int row, final int column, final int rowSpan, final int columnSpan,
                final LocalDate value) {
            SpreadsheetCell cell = new SpreadsheetCellBase(row, column, rowSpan, columnSpan, this);
            cell.setItem(value);
            return cell;
        }

        @Override
        public SpreadsheetCellEditor createEditor(SpreadsheetView view) {
            return new SpreadsheetCellEditor.DateEditor(view, converter);
        }

        @Override
        public boolean match(Object value) {
            if (value instanceof LocalDate)
                return true;
            else {
                try {
                    LocalDate temp = converter.fromString(value == null ? null : value.toString());
                    return temp != null;
                } catch (Exception e) {
                    return false;
                }
            }
        }

        @Override
        public LocalDate convertValue(Object value) {
            if (value instanceof LocalDate)
                return (LocalDate) value;
            else {
                try {
                    return converter.fromString(value == null ? null : value.toString());
                } catch (Exception e) {
                    return null;
                }
            }
        }

        @Override
        public String toString(LocalDate item) {
            return converter.toString(item);
        }

        @Override
        public String toString(LocalDate item, String format) {
            return ((StringConverterWithFormat<LocalDate>) converter).toStringFormat(item, format);
        }
    }
}
=======
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
    private void writeNumber(Object value) throws IOException {
        if (null == value) {
            writeNull();
        }
        if (value instanceof Float) {
            if (((Float)value).isNaN()) {
                writeNull();
            }
            if (Float.NEGATIVE_INFINITY == ((Float)value).floatValue()) {
                writeNull();
            }
            if (Float.POSITIVE_INFINITY == ((Float)value).floatValue()) {
                writeNull();
            }
        }
        if (value instanceof Double) {
            if (((Double)value).isNaN()) {
                writeNull();
            }
            if (Double.NEGATIVE_INFINITY == ((Double)value).doubleValue()) {
                writeNull();
            }
            if (Double.POSITIVE_INFINITY == ((Double)value).doubleValue()) {
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
            } else if (NumberUtil.isNumber(clazz)) {
                writeNumber(o);
            } else if (Boolean.class.isAssignableFrom(clazz)) {
                writer.write(((Boolean)o).toString());
            } else if (String.class.isAssignableFrom(clazz)) {
                writeString((String)o);
            } else if (JSONString.class.isAssignableFrom(clazz)) {
                writer.write(((JSONString)o).toJSONString());
            }// else {
             //   // Unknown type, we'll just try to serialize it like a Java Bean.
             //   writer.write(BeanSerializer.toJson(o, true).write());
           // }
        }
    }

    /**
     * Method to write the text string 'null' to the output stream (null JSON object).
     * @throws IOException Thrown if an error occurs during write.
     */
    private void writeNull() throws IOException {
        writer.write("null");
    }
}

>>>>>>> 76aa07461566a5976980e6696204781271955163
