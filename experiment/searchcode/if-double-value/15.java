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
package org.loon.framework.android.game.action.map.shapes;

import java.io.Serializable;

/**
 * Copyright 2008 - 2010
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 *
 * @project loonframework
 * @author chenpeng  
 * @email??&#x161;ceponline@yahoo.com.cn 
 * @version 0.1
 */
public class Vector3D implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7026354578113311982L;

	public double x, y, z;

	public Vector3D(double value) {
		this(value, value, value);
	}

	public Vector3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3D(Vector3D vector3D) {
		this.x = vector3D.x;
		this.y = vector3D.y;
		this.z = vector3D.z;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return y;
	}

	public int x() {
		return (int) x;
	}

	public int y() {
		return (int) y;
	}

	public int z() {
		return (int) z;
	}

	public Object clone() {
		return new Vector3D(x, y, z);
	}

	public void move(Vector3D vector3D) {
		this.x += vector3D.x;
		this.y += vector3D.y;
		this.z += vector3D.z;
	}

	public void move(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
	}

	public double[] getCoords() {
		return (new double[] { x, y,z });
	}

	public boolean equals(Object o) {
		if (o instanceof Vector3D) {
			Vector3D p = (Vector3D) o;
			return p.x == x && p.y == y && p.z == z;
		}
		return false;
	}

	public int hashCode() {
		return (int) (x + y + z);
	}

	public Vector3D add(Vector3D other) {
		double x = this.x + other.x;
		double y = this.y + other.y;
		double z = this.z + other.z;
		return new Vector3D(x, y, z);
	}

	public Vector3D subtract(Vector3D other) {
		double x = this.x - other.x;
		double y = this.y - other.y;
		double z = this.z - other.z;
		return new Vector3D(x, y, z);
	}

	public Vector3D multiply(double value) {
		return new Vector3D(value * x, value * y, value * z);
	}

	public Vector3D crossProduct(Vector3D other) {
		double x = this.y * other.z - other.y * this.z;
		double y = this.z * other.x - other.z * this.x;
		double z = this.x * other.y - other.x * this.y;
		return new Vector3D(x, y, z);
	}

	public double dotProduct(Vector3D other) {
		return other.x * x + other.y * y + other.z * z;
	}

	public Vector3D normalize() {
		double magnitude = Math.sqrt(dotProduct(this));
		return new Vector3D(x / magnitude, y / magnitude, z / magnitude);
	}

	public double level() {
		return Math.sqrt(dotProduct(this));
	}

	public Vector3D modulate(Vector3D other) {
		double x = this.x * other.x;
		double y = this.y * other.y;
		double z = this.z * other.z;
		return new Vector3D(x, y, z);
	}

	public String toString() {
		return (new StringBuffer("[Vector3D x:")).append(x).append(" y:")
				.append(y).append(" z:").append(z).append("]").toString();
	}

}

>>>>>>> 76aa07461566a5976980e6696204781271955163

