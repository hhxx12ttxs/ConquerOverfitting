<<<<<<< HEAD
/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2011, IBM Corporation
 */

package com.phonegap.json4j.internal;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;

import com.phonegap.json4j.JSONArray;
import com.phonegap.json4j.JSONObject;
import com.phonegap.json4j.JSONString;

/**
 * Class to handle serialization of a JSON object to a JSON string.
 */
public class Serializer {

    /**
     * The writer to use when writing this JSON object.
     */
    private Writer writer;

    /**
     * Create a serializer on the specified output stream writer.
     */
    public Serializer(Writer writer) {
        super();
        this.writer = writer;
    }

    /**
     * Method to flush the current writer.
     * @throws IOException Thrown if an error occurs during writer flush.
     */
    public void flush() throws IOException {
        writer.flush();
    }

    /**
     * Method to close the current writer.
     * @throws IOException Thrown if an error occurs during writer close.
     */
    public void close() throws IOException {
        writer.close();
    }

    /**
     * Method to write a raw string to the writer.
     * @param s The String to write.
     * @throws IOException Thrown if an error occurs during write.
     */
    public Serializer writeRawString(String s) throws IOException {
        writer.write(s);
        return this;
    }

    /**
     * Method to write the text string 'null' to the output stream (null JSON object).
     * @throws IOException Thrown if an error occurs during write.
     */
    public Serializer writeNull() throws IOException {
        writeRawString("null");
        return this;
    }

    /**
     * Method to write a number to the current writer.
     * @param value The number to write to the JSON output string.
     * @throws IOException Thrown if an error occurs during write.
     */
    public Serializer writeNumber(Object value) throws IOException {
        if (null == value) return writeNull();

        if (value instanceof Float) {
            if (((Float)value).isNaN()) return writeNull();
            if (Float.NEGATIVE_INFINITY == ((Float)value).floatValue()) return writeNull();
            if (Float.POSITIVE_INFINITY == ((Float)value).floatValue()) return writeNull();
        }

        if (value instanceof Double) {
            if (((Double)value).isNaN()) return writeNull();
            if (Double.NEGATIVE_INFINITY == ((Double)value).doubleValue()) return writeNull();
            if (Double.POSITIVE_INFINITY == ((Double)value).doubleValue()) return writeNull();
        }

        writeRawString(value.toString());

        return this;
    }

    /**
     * Method to write a boolean value to the output stream.
     * @param value The Boolean object to write out as a JSON boolean.
     * @throws IOException Thrown if an error occurs during write.
     */
    public Serializer writeBoolean(Boolean value) throws IOException {
        if (null == value) return writeNull();

        writeRawString(value.toString());

        return this;
    }

    /**
     * Method to generate a string with a particular width.  Alignment is done using zeroes if it does not meet the width requirements.
     * @param s The string to write
     * @param len The minimum length it should be, and to align with zeroes if length is smaller.
     * @return A string properly aligned/correct width.
     */
    private static String rightAlignedZero(String s, int len) {
        if (len == s.length()) return s;

        StringBuffer sb = new StringBuffer(s);

        while (sb.length() < len) {
            sb.insert(0, '0');
        }

        return sb.toString();
    }

    /**
     * Method to write a String out to the writer, encoding special characters and unicode characters properly.
     * @param value The string to write out.
     * @throws IOException Thrown if an error occurs during write.
     */
    public Serializer writeString(String value) throws IOException {
        if (null == value) return writeNull();

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

        return this;
    }

    /**
     * Method to write out a generic JSON type.
     * @param object The JSON compatible object to serialize.
     * @throws IOException Thrown if an error occurs during write, or if a nonJSON compatible Java object is passed..
     */
    private Serializer write(Object object) throws IOException {
        if (null == object) return writeNull();
        
        // Serialize the various types!
        Class clazz = object.getClass();
        if (NumberUtil.isNumber(clazz)) return writeNumber(object);
        if (Boolean.class.isAssignableFrom(clazz)) return writeBoolean((Boolean) object);
        if (JSONObject.class.isAssignableFrom(clazz)) return writeObject((JSONObject) object);
        if (JSONArray.class.isAssignableFrom(clazz)) return writeArray((JSONArray) object);
        if (JSONString.class.isAssignableFrom(clazz)) return writeRawString(((JSONString) object).toJSONString());
        if (String.class.isAssignableFrom(clazz)) return writeString((String) object);

        throw new IOException("Attempting to serialize unserializable object: '" + object + "'");
    }

    /**
     * Method to write a complete JSON object to the stream.
     * @param object The JSON object to write out.
     * @throws IOException Thrown if an error occurs during write.
     */
    public Serializer writeObject(JSONObject object) throws IOException {
        if (null == object) return writeNull();

        // write header
        writeRawString("{");
        indentPush();

        Enumeration iter = getPropertyNames(object);

        while ( iter.hasMoreElements() ) {
            Object key = iter.nextElement();
            if (!(key instanceof String)) throw new IOException("attempting to serialize object with an invalid property name: '" + key + "'" );

            Object value = object.get(key);
            if (!JSONObject.isValidObject(value)) throw new IOException("attempting to serialize object with an invalid property value: '" + value + "'");

            newLine();
            indent();
            writeString((String)key);
            writeRawString(":");
            space();
            write(value);

            if (iter.hasMoreElements()) writeRawString(",");
        }

        // write trailer
        indentPop();
        newLine();
        indent();
        writeRawString("}");

        return this;
    }

    /**
     * Method to write a JSON array out to the stream.
     * @param value The JSON array to write out.
     * @throws IOException Thrown if an error occurs during write.
     */
    public Serializer writeArray(JSONArray value) throws IOException {
        if (null == value) return writeNull();

        // write header
        writeRawString("[");
        indentPush();

        for (Enumeration iter=value.elements(); iter.hasMoreElements(); ) {
            Object element = iter.nextElement();
            if (!JSONObject.isValidObject(element)) throw new IOException("attempting to serialize array with an invalid element: '" + value + "'");

            newLine();
            indent();
            write(element);

            if (iter.hasMoreElements()) writeRawString(",");
        }

        // write trailer
        indentPop();
        newLine();
        indent();
        writeRawString("]");

        return this;
    }

    //---------------------------------------------------------------
    // pretty printing overridables
    //---------------------------------------------------------------

    /**
     * Method to write a space to the output writer.
     * @throws IOException Thrown if an error occurs during write.
     */
    public void space() throws IOException {
    }

    /**
     * Method to write a newline to the output writer.
     * @throws IOException Thrown if an error occurs during write.
     */
    public void newLine() throws IOException {
    }

    /**
     * Method to write an indent to the output writer.
     * @throws IOException Thrown if an error occurs during write.
     */
    public void indent() throws IOException {
    }

    /**
     * Method to increase the indent depth of the output writer.
     * @throws IOException Thrown if an error occurs during write.
     */
    public void indentPush() {
    }

    /**
     * Method to reduce the indent depth of the output writer.
     */
    public void indentPop() {
    }

    /**
     * Method to get a list of all the property names stored in a map.
     */
    public Enumeration getPropertyNames(Hashtable map) {
        return map.keys();
    }
    
    /**
     * Method to write a String out to the writer, encoding special characters and unicode characters properly.
     * @param value The string to write out.
     */
    public static String quote(String value) {
        if (value == null || value.length() == 0) {
            return "\"\"";
        }      

        StringBuffer buf = new StringBuffer();
        char[] chars = value.toCharArray();

        buf.append('"');        
        for (int i=0; i<chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case  '"': buf.append("\\\""); break;
                case '\\': buf.append("\\\\"); break;
                case    0: buf.append("\\0"); break;
                case '\b': buf.append("\\b"); break;
                case '\t': buf.append("\\t"); break;
                case '\n': buf.append("\\n"); break;
                case '\f': buf.append("\\f"); break;
                case '\r': buf.append("\\r"); break;
                case '/': buf.append("\\/"); break;
                default:
                    if ((c >= 32) && (c <= 126)) {
                    	buf.append(c);
                    } else {
                    	buf.append("\\u");
                    	buf.append(rightAlignedZero(Integer.toHexString(c),4));
                    }
            }
        }
        buf.append('"');        

        return buf.toString();
    }
}

=======
/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package alice.tuprolog;

import java.util.List;

/**
 *
 * Double class represents the double prolog data type
 *
 */
public class Double extends Number {
	
	private double value;
	
	public Double(double v) {
		value = v;
	}
	
	/**
	 *  Returns the value of the Double as int
	 */
	final public int intValue() {
		return (int) value;
	}
	
	/**
	 *  Returns the value of the Double as float
	 *
	 */
	final public float floatValue() {
		return (float) value;
	}
	
	/**
	 *  Returns the value of the Double as double
	 *
	 */
	final public double doubleValue() {
		return value;
	}
	
	/**
	 *  Returns the value of the Double as long
	 */
	final public long longValue() {
		return (long) value;
	}
	
	
	/** is this term a prolog integer term? */
	final public boolean isInteger() {
		return false;
	}
	
	/** is this term a prolog real term? */
	final public boolean isReal() {
		return true;
	}
	
	/** is an int Integer number? 
     * @deprecated Use <tt>instanceof Int</tt> instead. */
    final public boolean isTypeInt() {
        return false;
    }

    /** is an int Integer number?
     * @deprecated Use <tt>instanceof Int</tt> instead. */
	final public boolean isInt() {
		return false;
	}
	
	/** is a float Real number? 
     * @deprecated Use <tt>instanceof alice.tuprolog.Float</tt> instead. */
    final public boolean isTypeFloat() {
        return false;
    }

    /** is a float Real number?
     * @deprecated Use <tt>instanceof alice.tuprolog.Float</tt> instead. */
	final public boolean isFloat() {
		return false;
	}
	
	/** is a double Real number? 
     * @deprecated Use <tt>instanceof alice.tuprolog.Double</tt> instead. */
    final public boolean isTypeDouble() {
        return true;
    }

    /** is a double Real number?
     * @deprecated Use <tt>instanceof alice.tuprolog.Double</tt> instead. */
	final public boolean isDouble() {
		return true;
	}
	
	/** is a long Integer number? 
     * @deprecated Use <tt>instanceof alice.tuprolog.Long</tt> instead. */
    final public boolean isTypeLong() {
        return false;
    }

    /** is a long Integer number?
     * @deprecated Use <tt>instanceof alice.tuprolog.Long</tt> instead. */
	final public boolean isLong() {
		return false;
	}
	
	/**
	 * Returns true if this Double term is grater that the term provided.
	 * For number term argument, the int value is considered.
	 */
	public boolean isGreater(Prolog mediator, Term t) {
		t = t.getTerm();
		if (t instanceof Number) {
			return value>((Number)t).doubleValue();
		} else if (t instanceof Struct) {
			return false;
		} else if (t instanceof Var) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns true if this Double term is equal to the term provided.
	 */
	public boolean isEqual(Prolog mediator, Term t) {
		t = t.getTerm();
		if (t instanceof Number) {
			Number n = (Number) t;
			if (!n.isReal())
				return false;
			return value == n.doubleValue();
		} else {
			return false;
		}
	}
	
	/**
	 * Tries to unify a term with the provided term argument.
	 * This service is to be used in demonstration context.
	 */
	public boolean unify(Prolog mediator, List vl1, List vl2, Term t) {
		t = t.getTerm();
		if (t instanceof Var) {
			return t.unify(mediator, vl2, vl1, this);
		} else if (t instanceof Number) {
			return value==((Number)t).doubleValue();
		} else {
			return false;
		}
	}
	
	public String toString() {
		return java.lang.Double.toString(value);
	}
	
	public int resolveVariables(int count) {
		return count;
	}
	
}
>>>>>>> 76aa07461566a5976980e6696204781271955163
