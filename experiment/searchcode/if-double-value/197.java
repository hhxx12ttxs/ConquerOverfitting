<<<<<<< HEAD
/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2011, IBM Corporation
 */

package com.phonegap.json4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;

import com.phonegap.json4j.internal.JSON4JStringReader;
import com.phonegap.json4j.internal.JSON4JStringWriter;
import com.phonegap.json4j.internal.NumberUtil;
import com.phonegap.json4j.internal.Parser;
import com.phonegap.json4j.internal.Serializer;
import com.phonegap.json4j.internal.SerializerVerbose;

/**
 * Models a JSON Object.
 * 
 * Extension of Hashtable that only allows String keys, and values which are JSON-able (such as a Java Bean). 
 * <BR><BR>
 * JSON-able values are: null, and instances of String, Boolean, Number, JSONObject and JSONArray.
 * <BR><BR>
 * Instances of this class are not thread-safe.
 */
public class JSONObject extends Hashtable implements JSONArtifact {

    private static final long serialVersionUID = -3269263069889337298L;

    /**
     * A constant definition reference to Java null.  
     * Provided for API compatibility with other JSON parsers.
     */
    public static final Object NULL = new Null();

    /**
     * Return whether the object is a valid value for a property.
     * @param object The object to check for validity as a JSON property value.
     * @return boolean indicating if the provided object is directly convertable to JSON.
     */
    public static boolean isValidObject(Object object) {
        if (null == object) return true;
        return isValidType(object.getClass());
    }

    /**
     * Return whether the class is a valid type of value for a property.
     * @param clazz The class type to check for validity as a JSON object type.
     * @return boolean indicating if the provided class is directly convertable to JSON.
     */
    public static boolean isValidType(Class clazz) {
        if (null == clazz) throw new IllegalArgumentException();

        if (String.class  == clazz) return true;
        if (Boolean.class == clazz) return true;
        if (JSONObject.class.isAssignableFrom(clazz)) return true;
        if (JSONArray.class == clazz) return true;
        if (NumberUtil.isNumber(clazz)) return true;
        if (JSONObject.NULL == clazz) return true;
        if (JSONString.class.isAssignableFrom(clazz)) return true;

        return false;
    }

    /**
     * Create a new instance of this class. 
     */
    public JSONObject() {
        super();
    }

    /**
     * Create a new instance of this class taking selected values from the underlying one.
     * @param obj The JSONObject to extract values from.
     * @param keys The keys to take from the JSONObject and apply to this instance.
     * @throws JSONException Thrown if a key is duplicated in the string[] keys
     */
    public JSONObject(JSONObject obj, String[] keys) throws JSONException{
        super();
        if (keys != null && keys.length > 0) {
            for (int i = 0; i < keys.length; i++) {
                if (this.containsKey(keys[i])) {
                    throw new JSONException("Duplicate key: " + keys[i]);
                }
                try {
                    this.put(keys[i], obj.get(keys[i]));
                } catch (Exception ex) {
                    JSONException jex = new JSONException("Error occurred during JSONObject creation");
                    jex.setCause(ex);
                    throw jex;
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
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A JSONObject is an unordered collection of name/value pairs. Its external
 * form is a string wrapped in curly braces with colons between the names and
 * values, and commas between the values and names. The internal form is an
 * object having <code>get</code> and <code>opt</code> methods for accessing the
 * values by name, and <code>put</code> methods for adding or replacing values
 * by name. The values can be any of these types: <code>Boolean</code>,
 * <code>JSONArray</code>, <code>JSONObject</code>, <code>Number</code>,
 * <code>String</code>, or the <code>JSONObject.NULL</code> object. A JSONObject
 * constructor can be used to convert an external form JSON text into an
 * internal form whose values can be retrieved with the <code>get</code> and
 * <code>opt</code> methods, or to convert values into a JSON text using the
 * <code>put</code> and <code>toString</code> methods. A <code>get</code> method
 * returns a value if one can be found, and throws an exception if one cannot be
 * found. An <code>opt</code> method returns a default value instead of throwing
 * an exception, and so is useful for obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object, which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you. The opt methods differ from the get methods in that they do
 * not throw. Instead, they return a specified value, such as null.
 * <p>
 * The <code>put</code> methods add or replace values in an object. For example,
 *
 * <pre>
 * myString = new JSONObject().put(&quot;JSON&quot;, &quot;Hello, World!&quot;).toString();
 * </pre>
 *
 * produces the string <code>{"JSON": "Hello, World"}</code>.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * the JSON syntax rules. The constructors are more forgiving in the texts they
 * will accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 * before the closing brace.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 * quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 * or single quote, and if they do not contain leading or trailing spaces, and
 * if they do not contain any of these characters:
 * <code>{ } [ ] / \ : , = ; #</code> and if they do not look like numbers and
 * if they are not the reserved words <code>true</code>, <code>false</code>, or
 * <code>null</code>.</li>
 * <li>Keys can be followed by <code>=</code> or <code>=></code> as well as by
 * <code>:</code>.</li>
 * <li>Values can be followed by <code>;</code> <small>(semicolon)</small> as
 * well as by <code>,</code> <small>(comma)</small>.</li>
 * </ul>
 *
 * @author JSON.org
 * @version 2012-05-29
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class JSONObject {

    /**
     * JSONObject.NULL is equivalent to the value that JavaScript calls null,
     * whilst Java's null is equivalent to the value that JavaScript calls
     * undefined.
     */
     private static final class Null {

        /**
         * There is only intended to be a single instance of the NULL object,
         * so the clone method returns itself.
         * @return     NULL.
         */
        protected final Object clone() {
            return this;
        }

        /**
         * A Null object is equal to the null value and to itself.
         * @param object    An object to test for nullness.
         * @return true if the object parameter is the JSONObject.NULL object
         *  or null.
         */
        public boolean equals(Object object) {
            return object == null || object == this;
        }

        /**
         * Get the "null" string value.
         * @return The string "null".
         */
        public String toString() {
            return "null";
        }
    }


    /**
     * The map where the JSONObject's properties are kept.
     */
    private final Map map;


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
        this.map = new HashMap();
    }


    /**
     * Construct a JSONObject from a subset of another JSONObject.
     * An array of strings is used to identify the keys that should be copied.
     * Missing keys are ignored.
     * @param jo A JSONObject.
     * @param names An array of strings.
     * @throws JSONException
     * @exception JSONException If a value is a non-finite number or if a name is duplicated.
     */
    public JSONObject(JSONObject jo, String[] names) {
        this();
        for (int i = 0; i < names.length; i += 1) {
            try {
                this.putOnce(names[i], jo.opt(names[i]));
            } catch (Exception ignore) {
            }
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

// The key is followed by ':'. We will also tolerate '=' or '=>'.

            c = x.nextClean();
            if (c == '=') {
                if (x.next() != '>') {
                    x.back();
                }
            } else if (c != ':') {
                throw x.syntaxError("Expected a ':' after a key");
            }
            this.putOnce(key, x.nextValue());

// Pairs are separated by ','. We will also tolerate ';'.

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
>>>>>>> 76aa07461566a5976980e6696204781271955163
            }
        }
    }

<<<<<<< HEAD
    /**
     * Create a new instance of this class from the provided JSON object string.
     * Note:  This is the same as new JSONObject(str, false);  Parsing in non-strict mode.
     * @param str The JSON string to parse.  
     * @throws JSONException Thrown when the string passed is null, or malformed JSON.. 
     */
    public JSONObject(String str) throws JSONException {
        super();
        JSON4JStringReader reader = new JSON4JStringReader(str);
        (new Parser(reader)).parse(this);
    }

    /**
     * Create a new instance of this class from the provided JSON object string.
     * @param str The JSON string to parse.  
     * @param strict Whether or not to parse in 'strict' mode, meaning all strings must be quoted (including identifiers), and comments are not allowed.
     * @throws JSONException Thrown when the string passed is null, or malformed JSON.. 
     */
    public JSONObject(String str, boolean strict) throws JSONException {
        super();
        JSON4JStringReader reader = new JSON4JStringReader(str);
        (new Parser(reader, strict)).parse(this);
    }

    /**
     * Create a new instance of this class from the data provided from the reader.  The reader content must be a JSON object string.
     * Note:  The reader will not be closed, that is left to the caller.
     * Note:  This is the same as new JSONObject(rdr, false);  Parsing in non-strict mode.
     * @throws JSONException Thrown when the string passed is null, or malformed JSON.. 
     */
    public JSONObject(Reader rdr) throws JSONException {
        (new Parser(rdr)).parse(this);
    }

    /**
     * Create a new instance of this class from the data provided from the reader.  The reader content must be a JSON object string.
     * Note:  The reader will not be closed, that is left to the caller.
     * @param rdr The reader from which to read the JSON.
     * @param strict Whether or not to parse in 'strict' mode, meaning all strings must be quoted (including identifiers), and comments are not allowed.
     * @throws JSONException Thrown when the string passed is null, or malformed JSON.. 
     */
    public JSONObject(Reader rdr, boolean strict) throws JSONException {
        (new Parser(rdr, strict)).parse(this);
    }

    /**
     * Create a new instance of this class from the data provided from the input stream.  The stream content must be a JSON object string.
     * Note:  The input stream content is assumed to be UTF-8 encoded.
     * Note:  The InputStream will not be closed, that is left to the caller.
     * Note:  This is the same as new JSONObject(is, false);  Parsing in non-strict mode.
     * @param is The InputStream from which to read the JSON.
     * @throws JSONException Thrown when the string passed is null, or malformed JSON.. 
     */
    public JSONObject (InputStream is) throws JSONException {
        InputStreamReader isr = null;
        if (is != null) {
            try {
                isr = new InputStreamReader(is, "UTF-8");
            } catch (Exception ex) {
                isr = new InputStreamReader(is);
            }
        } else {
            throw new JSONException("InputStream cannot be null");
        }
        (new Parser(isr)).parse(true, this);
    }

    /**
     * Create a new instance of this class from the data provided from the input stream.  The stream content must be a JSON object string.
     * Note:  The input stream content is assumed to be UTF-8 encoded.
     * Note:  The InputStream will not be closed, that is left to the caller.
     * @param is The InputStream from which to read the JSON.
     * @param strict Whether or not to parse in 'strict' mode, meaning all strings must be quoted (including identifiers), and comments are not allowed.
     * @throws JSONException Thrown when the string passed is null, or malformed JSON.. 
     */
    public JSONObject (InputStream is, boolean strict) throws JSONException {
        InputStreamReader isr = null;
        if (is != null) {
            try {
                isr = new InputStreamReader(is, "UTF-8");
            } catch (Exception ex) {
                isr = new InputStreamReader(is);
            }
        } else {
            throw new JSONException("InputStream cannot be null");
        }
        (new Parser(isr, strict)).parse(true, this);
    }

    /**
     * Write this object to the stream as JSON text in UTF-8 encoding.  Same as calling write(os,false);
     * Note that encoding is always written as UTF-8, as per JSON spec.
     * @param os The output stream to write data to.
     *
     * @throws JSONException Thrown on IO errors during serialization.
     */
    public OutputStream write(OutputStream os) throws JSONException {
        write(os,false);
        return os;
    }

    /**
     * Convert this object into a stream of JSON text.  Same as calling write(writer,false);
     * Note that encoding is always written as UTF-8, as per JSON spec.
     * @param os The output stream to write data to.
     * @param verbose Whether or not to write the JSON text in a verbose format.
     *
     * @throws JSONException Thrown on IO errors during serialization.
     */
    public OutputStream write(OutputStream os, boolean verbose) throws JSONException {
        Writer writer = null;
        try {
        	//MSN BUFFERED
            writer = new OutputStreamWriter(os, "UTF-8");
        } catch (UnsupportedEncodingException uex) {
            JSONException jex = new JSONException(uex.toString());
            jex.setCause(uex);
            throw jex;
        }
        write(writer, verbose);
        try {
            writer.flush();
        } catch (Exception ex) { 
            JSONException jex = new JSONException("Error during buffer flush");
            jex.setCause(ex);
            throw jex;
        }
        return os;
    }

    /**
     * Write this object to the stream as JSON text in UTF-8 encoding, specifying how many spaces should be used for each indent.
     * @param indentDepth How many spaces to use for each indent level.  Should be one to eight.  
     * Less than one means no intending, greater than 8 and it will just use tab.
     *
     * @throws JSONException Thrown on IO errors during serialization.
     */
    public OutputStream write(OutputStream os, int indentDepth) throws JSONException {
        Writer writer = null;
        try {
        	//MSN BUFFERED
            writer = new OutputStreamWriter(os, "UTF-8");
        } catch (UnsupportedEncodingException uex) {
            JSONException jex = new JSONException(uex.toString());
            jex.setCause(uex);
            throw jex;
        }
        write(writer, indentDepth);
        try {
            writer.flush();
        } catch (Exception ex) { 
            JSONException jex = new JSONException("Error during buffer flush");
            jex.setCause(ex);
            throw jex;
        }
        return os;
    }

    /**
     * Write this object to the writer as JSON text. Same as calling write(writer,false);
     * @param writer The writer which to write the JSON text to.
     *
     * @throws JSONException Thrown on IO errors during serialization.
     */
    public Writer write(Writer writer) throws JSONException {
        write(writer, false);
        return writer;
    }

    /**
     * Write this object to the writer as JSON text in UTF-8 encoding, specifying whether to use verbose (tab-indented) output or not.
     * @param writer The writer which to write the JSON text to.
     *
     * @throws JSONException Thrown on IO errors during serialization.
     */
    public Writer write(Writer writer, boolean verbose) throws JSONException {
        Serializer serializer;

        //Try to avoid double-buffering or buffering in-memory
        //writers.
        Class writerClass = writer.getClass();
        boolean flushIt = false;
//        if (!StringWriter.class.isAssignableFrom(writerClass) &&
//            !CharArrayWriter.class.isAssignableFrom(writerClass) &&
//            !BufferedWriter.class.isAssignableFrom(writerClass)) {
//            writer = new BufferedWriter(writer);
//            flushIt = true;
//        }

        if (verbose) {
            serializer = new SerializerVerbose(writer);
        } else {
            serializer = new Serializer(writer);
        }

        try {
            serializer.writeObject(this);
        } catch (IOException iox) {
            JSONException jex = new JSONException("Error occurred during input read.");
            jex.setCause(iox);
            throw jex;
        }
        if (flushIt) {
            try {
                writer.flush();
            } catch (Exception ex) { 
                JSONException jex = new JSONException("Error during buffer flush");
                jex.setCause(ex);
                throw jex;
            }
        }
        return writer;
    }

    /**
     * Write this object to the writer as JSON text, specifying how many spaces should be used for each indent.  
     * This is an alternate indent style to using tabs.
     * @param writer The writer which to write the JSON text to.
     * @param indentDepth How many spaces to use for each indent.  The value should be between one to eight.
     */
    public Writer write(Writer writer, int indentDepth) throws JSONException {
        Serializer serializer;

        if (indentDepth < 1) {
            indentDepth = 0;
        } else if (indentDepth > 8) {
            indentDepth = 9;
        }

        //Try to avoid double-buffering or buffering in-memory
        //writers.
        Class writerClass = writer.getClass();
        boolean flushIt = false;
//        if (!StringWriter.class.isAssignableFrom(writerClass) &&
//            !CharArrayWriter.class.isAssignableFrom(writerClass) &&
//            !BufferedWriter.class.isAssignableFrom(writerClass)) {
//            writer = new BufferedWriter(writer);
//            flushIt = true;
//        }

        if (indentDepth > 0) {
            serializer = new SerializerVerbose(writer, indentDepth);
        } else {
            serializer = new Serializer(writer);
        }
        try {
            serializer.writeObject(this);
        } catch (IOException iox) {
            JSONException jex = new JSONException("Error occurred during input read.");
            jex.setCause(iox);
            throw jex;
        }
        if (flushIt) {
            try {
                writer.flush();
            } catch (Exception ex) { 
                JSONException jex = new JSONException("Error during buffer flush");
                jex.setCause(ex);
                throw jex;
            }
        }
        return writer;
    }

    /**
     * Convert this object into a String of JSON text, specifying how many spaces should be used for each indent.  
     * This is an alternate indent style to using tabs.
     * @param indentDepth How many spaces to use for each indent.  The value should be between one to eight.  
     * Less than one means no indenting, greater than 8 and it will just use tab.
     *
     * @throws JSONException Thrown on errors during serialization.
     */
    public String write(int indentDepth) throws JSONException {
        Serializer serializer;
        JSON4JStringWriter writer = new JSON4JStringWriter();

        if (indentDepth < 1) {
            indentDepth = 0;
        } else if (indentDepth > 8) {
            indentDepth = 9;
        }

        if (indentDepth > 0) {
            serializer = new SerializerVerbose(writer, indentDepth);
        } else {
            serializer = new Serializer(writer);
        }
        try {
            serializer.writeObject(this).flush();
        } catch (IOException iox) {
            JSONException jex = new JSONException("Error occurred during write.");
            jex.setCause(iox);
            throw jex;
        }
        return writer.toString();
    }

    /**
     * Convert this object into a String of JSON text, specifying whether to use verbose (tab-indented) output or not.
     * @param verbose Whether or not to write in compressed format.
     *
     * @throws JSONException Thrown on errors during serialization.
     */
    public String write(boolean verbose) throws JSONException {
        Serializer serializer;
        JSON4JStringWriter writer = new JSON4JStringWriter();

        if (verbose) {
            serializer = new SerializerVerbose(writer);
        } else {
            serializer = new Serializer(writer);
        }
        try {
            serializer.writeObject(this).flush();
        } catch (IOException iox) {
            JSONException jex = new JSONException("Error occurred during write.");
            jex.setCause(iox);
            throw jex;
        }
        return writer.toString();
    }

    /**
     * Convert this object into a String of JSON text.  Same as write(false);
     *
     * @throws JSONException Thrown on IO errors during serialization.
     */
    public String write() throws JSONException {
        return write(false);
    }

    /**
     * Method to obtain the object value for a key.  
     * This string-based method is provided for API compatibility to other JSON models.
     * @param key The key  (attribute) name to obtain the value for.
     * @throws JSONException Thrown if the noted key is not in the map of key/value pairs.
     */
    public Object get(String key) throws JSONException {
        Object val = this.get((Object)key);
        if (val == null) {
            if (!this.containsKey(key)) {
                throw new JSONException("The key [" + key + "] was not in the map");
            }
        }
        return val;
    }

    /**
     * Method to obtain the object value for a key.  If the key is not in the map, null is returned.  
     * This string-based method is provided for API compatibility to other JSON models.
     * @param key The key  (attribute) name to obtain the value for.
     */
    public Object opt(String key) {
        return this.get((Object)key);
    }

    /**
     * (non-Javadoc)
     * @see java.util.HashMap#put(java.lang.Object, java.lang.Object)
     * @param key The key to put in the JSONObject
     * @param value The value to put in the JSONObject
     * @param includeSuperclass Boolean indicating that if the object is a JavaBean, include superclass getter properties.
     * @throws JSONException.  Thrown if key is null, not a string, or the value could not be converted.
     */
    public Object put(Object key, Object value, boolean includeSuperclass) throws JSONException{
        if (null == key) throw new JSONException("key must not be null");
        if (!(key instanceof String)) throw new JSONException("key must be a String");
        if (!isValidObject(value)) {
            throw new JSONException("Invalid type of value.  Could not convert type: [" + value.getClass().getName() + "]");
        }
        if (null == value) {
            value = NULL;
        }
        return super.put(key, value);
    }

    /**
     * (non-Javadoc)
     * @see java.util.HashMap#put(java.lang.Object, java.lang.Object)
     * This is the same as calling put(key, value, true);
     */
    public Object put(Object key, Object value) {
        try {
            return put(key, value, true);
        } catch (Exception e) {
            IllegalArgumentException iae = new IllegalArgumentException("Error occurred during JSON conversion");
           //MSN iae.setCause(e);
            throw iae;
        }
    }

    /**
     * Convenience functions, to help map from other JSON parsers.
     */

    /**
     * Similar to default HashMap put, except it returns JSONObject instead of Object.
     * @see java.util.HashMap#put(java.lang.Object, java.lang.Object)
     * @return A reference to this object instance.
     * @throws JSONException.  Thrown if key is null, not a string, or the value could not be converted to JSON.
     */
    public JSONObject put(String key, Object value) throws JSONException{
        this.put((Object)key, value);
        return this;
    }

    /**
     * Method to add an atomic boolean to the JSONObject.
     * param key The key/attribute name to set the boolean at.
     * @param value The boolean value.
     * @throws JSONException.  Thrown if key is null or not a string.
     * @return A reference to this object instance.
     */
    public JSONObject put(String key, boolean value) throws JSONException{
        this.put(key,new Boolean(value));
        return this;
    }

    /**
     * Method to add an atomic double to the JSONObject.
     * param key The key/attribute name to set the double at.
     * @param value The double value.
     * @throws JSONException.  Thrown if key is null or not a string.
     * @return A reference to this object instance.
     */
    public JSONObject put(String key, double value) throws JSONException{
        this.put(key, new Double(value));
        return this;
    }

    /**
     * Method to add an atomic integer to the JSONObject.
     * param key The key/attribute name to set the integer at.
     * @param value The integer value.
     * @throws JSONException.  Thrown if key is null or not a string.
     * @return A reference to this object instance.
     */
    public JSONObject put(String key, int value) throws JSONException{
        this.put(key, new Integer(value));
        return this;
    }

    /**
     * Method to add an atomic short to the JSONObject.
     * param key The key/attribute name to set the integer at.
     * @param value The integer value.
     * @throws JSONException.  Thrown if key is null or not a string.
     * @return A reference to this object instance.
     */
    public JSONObject put(String key, short value) throws JSONException{
        this.put(key, new Short(value));
        return this;
    }

    /**
     * Method to add an atomic long to the JSONObject.
     * @param key The key/attribute name to set the long to.
     * @param value The long value.
     * @throws JSONException.  Thrown if key is null or not a string.
     * @return A reference to this object instance.
     */
    public JSONObject put(String key, long value) throws JSONException{
        this.put(key, new Long(value));
        return this;
    }

    /**
     * Method to add an Object array as a new JSONArray contained in this JSONObject
     * @param key The key/attribute name to set the collection to.
     * @param value The Object array to convert to a JSONArray and store.
     * @throws JSONException Thrown when contents in the Collection cannot be converted to something JSONable.
     * @return A reference to this object instance.
     */
     public JSONObject put(String key, Object[] value) throws JSONException {
     	 return put (key, new JSONArray(value));
     }
     
     /**
      * Method to add an Object array as a new JSONArray contained in this JSONObject
      * @param key The key/attribute name to set the collection to.
      * @param value The Object array to convert to a JSONArray and store.
      * @param includeSuperclass For values of the Object array which are JavaBeans and are converted, include superclass getter properties.
      * @throws JSONException Thrown when contents in the Collection cannot be converted to something JSONable.
      * @return A reference to this object instance.
      *
      public JSONObject put(String key, Object[] value, boolean includeSuperclass) throws JSONException {
   	      return put (key, new JSONArray(value), includeSuperclass);
      } */

    /**
     * Utility method to obtain the specified key as a 'boolean' value
     * Only boolean true, false, and the String true and false will return.  In this case null, the number 0, and empty string should be treated as false.
     * everything else is true.
     * Provided for compatibility to other JSON models.
     * @param key The key to look up.
     * throws JSONException Thrown when the type returned by get(key) is not a boolean instance, or the strings 'true' or 'false'.
     * @return A boolean value (true or false), if the value stored for key is a Boolean, or the strings 'true' or 'false'.
     */ 
    public boolean getBoolean(String key) throws JSONException {
        Object val = this.opt(key);
        if (val != null) {
            if (val instanceof Boolean) {
                return((Boolean)val).booleanValue();
            } else if (NumberUtil.isNumber(val.getClass())) {
                throw new JSONException("Value at key: [" + key + "] was not a boolean or string value of 'true' or 'false'.");
            } else if (String.class.isAssignableFrom(val.getClass())) {
                String str = (String)val;
                if (str.equals("true")) {
                    return true;
                } else if (str.equals("false")) {
                    return false;
                } else {
                    throw new JSONException("The value for key: [" + key + "]: [" + str + "] was not 'true' or 'false'");    
                }
            } else {
                throw new JSONException("The value for key: [" + key + "] was not a type that can be converted to boolean");
            }
        } else {
            throw new JSONException("The value for key: [" + key + "] was null");
        }
    }

    /**
     * Utility method to obtain the specified key as a 'boolean' value
     * Only returns true if the value is boolean true or the string 'true'.  All other values return false.
     * Provided for compatibility to other JSON models.
     * @param key The key to look up.
     * @return A boolean value (true or false), if the value stored for key is a Boolean, or the strings 'true' or 'false'.
     */ 
    public boolean optBoolean(String key) {
        Object val = this.opt(key);
        if (val != null) {
            if (val instanceof Boolean) {
                return((Boolean)val).booleanValue();
            } else if (NumberUtil.isNumber(val.getClass())) {
                return false;
            } else if (String.class.isAssignableFrom(val.getClass())) {
                String str = (String)val;
                if (str.equals("true")) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Utility method to obtain the specified key as a 'boolean' value
     * Only returns true if the value is boolean true or the string 'true'.  All other values return false.
     * Provided for compatibility to other JSON models.
     * @param key The key to look up.
     * @param defaultValue The default value to return.
     * @return A boolean value (true or false), if the value stored for key is a Boolean, or the strings 'true' or 'false'.
     */ 
    public boolean optBoolean(String key, boolean defaultValue) {
        Object val = this.opt(key);
        if (val != null) {
            if (val instanceof Boolean) {
                return((Boolean)val).booleanValue();
            } else if (NumberUtil.isNumber(val.getClass())) {
                return false;
            } else if (String.class.isAssignableFrom(val.getClass())) {
                String str = (String)val;
                if (str.equals("true")) {
                    return true;
                } else if (str.equals("false")) {
                    return false;
                } else {
                    return defaultValue;
                }
            } else {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    /**
     * Utility method to obtain the specified key as a 'double' value
     * Only values of Number will be converted to double, all other types will generate an exception
     * Provided for compatibility to other JSON models.
     * @param key The key to look up.
     * throws JSONException Thrown when the type returned by get(key) is not a Double instance, or cannot be converted to a double.
     * @return A double value if the value stored for key is an instance of Number.
     */ 
    public double getDouble(String key) throws JSONException {
        Object val = this.opt(key);
        if (val != null) {
            if (NumberUtil.isNumber(val.getClass())) {
                return NumberUtil.getDouble(val);
            }
            else {
                throw new JSONException("The value for key: [" + key + "] was not a type that can be converted to double");
            }
        } else {
            throw new JSONException("The value for key: [" + key + "] was null.  Number required.");
        }
    }

    /**
     * Utility method to obtain the specified key as a 'double' value
     * Only values of Number will be converted to double.  all other values will return Double.NaN.
     * Provided for compatibility to other JSON models.
     * @param key The key to look up.
     * @return A double value if the value stored for key is an instance of Number.
     */ 
    public double optDouble(String key) {
        Object val = this.opt(key);
        if (val != null) {
            if (NumberUtil.isNumber(val.getClass())) {
                return NumberUtil.getDouble(val);
            }
        }
        return Double.NaN;
    }

    /**
     * Utility method to obtain the specified key as a 'double' value
     * Only values of Number will be converted to double.  all other values will return Double.NaN.
     * Provided for compatibility to other JSON models.
     * @param key The key to look up.
     * @param defaultValue The default double value to return in case of NaN/null values in map.
     * @return A double value if the value stored for key is an instance of Number.
     */ 
    public double optDouble(String key, double defaultValue) {
        Object val = this.opt(key);
        if (val != null) {
            if (NumberUtil.isNumber(val.getClass())) {
                return NumberUtil.getDouble(val);
            }
        }
        return defaultValue;
    }

    /**
     * Utility method to obtain the specified key as a 'short' value
     * Only values of Number will be converted to short, all other types will generate an exception.
     * Provided for compatibility to other JSON models.
     * @param key The key to look up.
     * throws JSONException Thrown when the type returned by get(key) is not a Short instance, or cannot be converted to a short.
     * @return A short value if the value stored for key is an instance of Number.
     */ 
    public short getShort(String key) throws JSONException {
        Object val = this.opt(key);
        if (val != null) {
            if (NumberUtil.isNumber(val.getClass())) {
                return NumberUtil.getShort(val);
            } else {
                throw new JSONException("The value for key: [" + key + "] was not a type that can be converted to short");
            }
        } else {
            throw new JSONException("The value for key: [" + key + "] was null.  Number required.");
        }
    }

    /**
     * Utility method to obtain the specified key as a 'short' value
     * Only values of Number will be converted to short.  All other types return 0.
     * Provided for compatibility to other JSON models.
     * @param key The key to look up.
     * @return A short value if the value stored for key is an instance of Number.  0 otherwise.
     */ 
    public short optShort(String key) {
        Object val = this.opt(key);
        if (val != null) {
            if (NumberUtil.isNumber(val.getClass())) {
                return NumberUtil.getShort(val);
            } 
        }
        return(short)0;
    }

    /**
     * Utility method to obtain the specified key as a 'short' value
     * Only values of Number will be converted to short.  
     * Provided for compatibility to other JSON models.
     * @param key The key to look up.
     * @param defaultValue The default value to return in the case of null/nonNumber values in the map.
     * @return A short value if the value stored for key is an instance of Number.  0 otherwise.
     */ 
    public short optShort(String key, short defaultValue) {
        Object val = this.opt(key);
        if (val != null) {
            if (NumberUtil.isNumber(val.getClass())) {
                return NumberUtil.getShort(val);
            } 
        }
        return defaultValue;
    }

    /**
     * Utility method to obtain the specified key as a 'int' value
     * Only values of Number will be converted to integer, all other types will generate an exception.
     * Provided for compatibility to other JSON models.
     * @param key The key to look up.
     * throws JSONException Thrown when the type returned by get(key) is not a Double instance, or cannot be converted to a double.
     * @return A int value if the value stored for key is an instance of Number.
     */ 
    public int getInt(String key) throws JSONException {
        Object val = this.opt(key);
        if (val != null) {
            if (NumberUtil.isNumber(val.getClass())) {
                return NumberUtil.getInt(val);
            } else {
                throw new JSONException("The value for key: [" + key + "] was not a type that can be converted to integer");
            }
        } else {
            throw new JSONException("The value for key: [" + key + "] was null.  Number required.");
        }
    }

    /**
     * Utility method to obtain the specified key as a 'int' value
     * Provided for compatibility to other JSON models.
     * Only values of Number will be converted to integer, all other types will return 0.
     * @param key The key to look up.
     * @return A int value if the value stored for key is an instance of Number.  0 otherwise.
     */ 
    public int optInt(String key) {
        Object val = this.opt(key);
        if (val != null) {
            if (NumberUtil.isNumber(val.getClass())) {
                return NumberUtil.getInt(val);
            } 
        }
        return 0;
    }

    /**
     * Utility method to obtain the specified key as a 'int' value
     * Provided for compatibility to other JSON models.
     * Only values of Number will be converted to integer
     * @param key The key to look up.
     * @param defaultValue The default int value to return in case of null/non-number values in the map.
     * @return A int value if the value stored for key is an instance of Number.
     */ 
    public int optInt(String key, int defaultValue) {
        Object val = this.opt(key);
        if (val != null) {
            if (NumberUtil.isNumber(val.getClass())) {
                return NumberUtil.getInt(val);
            } 
        }
        return defaultValue;
    }

    /**
     * Utility method to obtain the specified key as a 'long' value
     * Only values of Number will be converted to long, all other types will generate an exception.
     * Provided for compatibility to other JSON models.
     * @param key The key to look up.
     * throws JSONException Thrown when the type returned by get(key) is not a Long instance, or cannot be converted to a long..
     * @return A long value if the value stored for key is an instance of Number.
     */ 
    public long getLong(String key) throws JSONException {
        Object val = this.opt(key);
        if (val != null) {
            if (NumberUtil.isNumber(val.getClass())) {
                return NumberUtil.getLong(val);
            } else {
                throw new JSONException("The value for key: [" + key + "] was not a type that can be converted to long");
            }
        } else {
            throw new JSONException("The value for key: [" + key + "] was null.  Number required.");
        }
    }

    /**
     * Utility method to obtain the specified key as a 'long' value
     * Only values of Number will be converted to long.  all other types return 0.
     * Provided for compatibility to other JSON models.
     * @param key The key to look up.
     * @return A long value if the value stored for key is an instance of Number, 0 otherwise.
     */ 
    public long optLong(String key) throws JSONException {
        Object val = this.opt(key);
        if (val != null) {
            if (NumberUtil.isNumber(val.getClass())) {
                return NumberUtil.getLong(val);
            }
        }
        return(long)0;
    }

    /**
     * Utility method to obtain the specified key as a 'long' value
     * Only values of Number will be converted to long.  all other types return 0.
     * Provided for compatibility to other JSON models.
     * @param key The key to look up.
     * @param defaultValue The default long value to return in case of null/non Number values in the map.
     * @return A long value if the value stored for key is an instance of Number, defaultValue otherwise.
     */ 
    public long optLong(String key, long defaultValue) throws JSONException {
        Object val = this.opt(key);
        if (val != null) {
            if (NumberUtil.isNumber(val.getClass())) {
                return NumberUtil.getLong(val);
            }
        }
        return defaultValue;
    }

    /**
     * Utility method to obtain the specified key as a 'string' value
     * Only values that can be easily converted to string will be returned.  A null will generate an exception.
     * Provided for compatibility to other JSON models.
     * @param key The key to look up.
     * throws JSONException Thrown when the type returned by get(key) is null.
     * @return A string value if the value if the value stored for key is not null.
     */ 
    public String getString(String key) throws JSONException {
        Object val = this.opt(key);
        if (val != null) {
            return val.toString();
        } else {
            throw new JSONException("The value for key: [" + key + "] was null.  Object required.");
        }
    }

    /**
     * Utility method to obtain the specified key as a 'string' value
     * Only values that can be easily converted to string will be returned.  A null will generate an exception.
     * Provided for compatibility to other JSON models.
     * @param key The key to look up.
     * throws JSONException Thrown when the type returned by get(key) is null.
     * @return A string value if the value if the value stored for key is not null.
     */ 
    public String optString(String key) {
        Object val = this.opt(key);
        if (val != null) {
            return val.toString();
        }
        return null;
    }

    /**
     * Utility method to obtain the specified key as a 'string' value
     * Only values that can be easily converted to string will be returned.  A null will generate an exception.
     * Provided for compatibility to other JSON models.
     * @param key The key to look up.
     * @param defaultValue The default String value to return in the case of null values in the map.
     * @return A string value if the value if the value stored for key is not null, defaultValue otherwise.
     */ 
    public String optString(String key, String defaultValue) {
        Object val = this.opt(key);
        if (val != null) {
            return val.toString();
        }
        return defaultValue;
    }

    /**
     * Utility method to obtain the specified key as a JSONObject
     * Only values that are instances of JSONObject will be returned.  A null will generate an exception.
     * Provided for compatibility to other JSON models.
     * @param key The key to look up.
     * throws JSONException Thrown when the type returned by get(key) is not a JSONObject instance.
     * @return A JSONObject value if the value stored for key is an instance or subclass of JSONObject.
     */ 
    public JSONObject getJSONObject(String key) throws JSONException {
        Object val = this.opt(key);
        if (val != null) {
            if (JSONObject.class.isAssignableFrom(val.getClass())) {
                return(JSONObject)val;
            } else {
                throw new JSONException("The value for key: [" + key + "] was not a JSONObject");
            }
        } else {
            throw new JSONException("The value for key: [" + key + "] was null.  Object required.");
        }
    }

    /**
     * Utility method to obtain the specified key as a JSONObject
     * Only values that are instances of JSONObject will be returned.  A null will generate an exception.
     * Provided for compatibility to other JSON models.
     * @param key The key to look up.
     * @return A JSONObject value if the value stored for key is an instance or subclass of JSONObject, null otherwise.
     */ 
    public JSONObject optJSONObject(String key) {
        Object val = this.opt(key);
        if (val != null) {
            if (JSONObject.class.isAssignableFrom(val.getClass())) {
                return(JSONObject)val;
            }
        }
        return null;
    }

    /**
     * Utility method to obtain the specified key as a JSONObject
     * Only values that are instances of JSONObject will be returned.  A null will generate an exception.
     * Provided for compatibility to other JSON models.
     * @param key The key to look up.
     * @param defaultValue The default JSONObject to return in the case of null/non JSONObject values in the map.
     * @return A JSONObject value if the value stored for key is an instance or subclass of JSONObject, defaultValue otherwise.
     */ 
    public JSONObject optJSONObject(String key, JSONObject defaultValue) {
        Object val = this.opt(key);
        if (val != null) {
            if (JSONObject.class.isAssignableFrom(val.getClass())) {
                return(JSONObject)val;
            }
        }
        return defaultValue;
    }

    /**
     * Utility method to obtain the specified key as a JSONArray
     * Only values that are instances of JSONArray will be returned.  A null will generate an exception.
     * Provided for compatibility to other JSON models.
     * @param key The key to look up.
     * throws JSONException Thrown when the type returned by get(key) is not a Long instance, or cannot be converted to a long..
     * @return A JSONArray value if the value stored for key is an instance or subclass of JSONArray.
     */ 
    public JSONArray getJSONArray(String key) throws JSONException {
        Object val = this.opt(key);
        if (val != null) {
            if (JSONArray.class.isAssignableFrom(val.getClass())) {
                return(JSONArray)val;
            } else {
                throw new JSONException("The value for key: [" + key + "] was not a JSONObject");
            }
        } else {
            throw new JSONException("The value for key: [" + key + "] was null.  Object required.");
        }
    }

    /**
     * Utility method to obtain the specified key as a JSONArray
     * Only values that are instances of JSONArray will be returned.  A null will generate an exception.
     * Provided for compatibility to other JSON models.
     * @param key The key to look up.
     * @return A JSONArray value if the value stored for key is an instance or subclass of JSONArray, null otherwise.
     */ 
    public JSONArray optJSONArray(String key) throws JSONException {
        Object val = this.opt(key);
        if (val != null) {
            if (JSONArray.class.isAssignableFrom(val.getClass())) {
                return(JSONArray)val;
            }
        }
        return null;
    }

    /**
     * Utility method to obtain the specified key as a JSONArray
     * Only values that are instances of JSONArray will be returned.  A null will generate an exception.
     * Provided for compatibility to other JSON models.
     * @param key The key to look up.
     * @param defaultValue The default value to return if the value in the map is null/not a JSONArray.
     * @return A JSONArray value if the value stored for key is an instance or subclass of JSONArray, defaultValue otherwise.
     */ 
    public JSONArray optJSONArray(String key, JSONArray defaultValue) throws JSONException {
        Object val = this.opt(key);
        if (val != null) {
            if (JSONArray.class.isAssignableFrom(val.getClass())) {
                return(JSONArray)val;
            }
        }
        return defaultValue;
    }

    /**
     * Put a key/value pair into the JSONObject, but only if key/value are both not null, and only if the key is not present already.
     * Provided for compatibility to existing models.
     * @param key The ket to place in the array
     * @param value The value to place in the array
     * @return Reference to the current JSONObject.
     * @throws JSONException - Thrown if the key already exists or if key or value is null
     */
    public JSONObject putOnce(String key, Object value) throws JSONException {
        if (key == null) {
            throw new JSONException("Key cannot be null");
        }
        if (value == null) {
            throw new JSONException("Value cannot be null");
        }
        if (this.containsKey(key)) {
            throw new JSONException("Key [" + key + "] already exists in the map");
        }
        this.put(key,value);
        return this;
    }

    /**
     * Put a key/value pair into the JSONObject, but only if the key and value are non-null.
     * @param key The keey (attribute) name to assign to the value.
     * @param value The value to put into the JSONObject.
     * @return Reference to the current JSONObject.
     * @throws JSONException - if the value is a non-finite number 
     */
    public JSONObject putOpt(String key, Object value) throws JSONException {
        if (key == null) {
            throw new JSONException("Key cannot be null");
        }
        if (value == null) {
            throw new JSONException("Value cannot be null");
        }
        this.put(key,value);
        return this;
    }

    /** 
     * Method to return the number of keys in this JSONObject.
     * This function merely maps to HashMap.size().  Provided for API compatibility.
     * @return returns the number of keys in the JSONObject.
     */
    public int length() {
        return this.size();
    }

    /**
     * Method to append the 'value' object to the element at entry 'key'.
     * If JSONObject.has(key) returns false, a new array is created and the value is appended to it.
     * If the object as position key is not an array, then a new JSONArray is created
     * and both current and new values are appended to it, then the value of the attribute is set to the new 
     * array.  If the current value is already an array, then 'value' is added to it.
     *
     * @param key The key/attribute name to append to.
     * @param value The value to append to it.
     *
     * @throws JSONException Thrown if the value to append is not JSONAble.
     * @return A reference to this object instance.
     */
    public JSONObject append(String key, Object value) throws JSONException {
        JSONArray array = null;
        if (!this.has(key)) {
            array = new JSONArray();
        }
        else {
            Object oldVal = this.get(key);
            array = new JSONArray();   
            if (oldVal == null) {
                // Add a null if the key was actually there, but just
                // had value of null.
                array.add(null);
            }
            else  if (JSONArray.class.isAssignableFrom(oldVal.getClass())) {
                array = (JSONArray)oldVal;
            } else {
                array = new JSONArray();
                array.add(oldVal);
            }
        }
        array.add(value);
        return put(key,array);
    }

    /**
     * Produce a JSONArray containing the values of the members of this JSONObject
     * @param names - A JSONArray containing the a list of key strings.  This determines the sequence of values in the result.
     * @return A JSONArray of the values found for the names provided.
     * @throws JSONException - if errors occur during storing the values in a JSONArray
     */
    public JSONArray toJSONArray(JSONArray names) throws JSONException {
        Enumeration itr = names.elements();
        JSONArray array = new JSONArray();
        //MSN WAS IF, SHOULD BE WHILE?
        while (itr != null && itr.hasMoreElements()) {
            array.put(this.get(itr.nextElement()));
        }
        return array;
    }


    /** 
     * Method to test if a key exists in the JSONObject.
     * @param key The key to test.
     * @return true if the key is defined in the JSONObject (regardless of value), or false if the key is not in the JSONObject
     */
    public boolean has(String key) {
        if (key != null) {
            return this.containsKey(key);
        }
        return false;
    }

  /**
   * Method to test if a key is mapped to null. This method will also return
   * true if the key has not been put in the JSONObject yet,
   * 
   * @param key   The key to test for null.
   * @return true if the key is not in the map or if the value referenced by the
   *         key is null, or if the value is the JSONObject.NULL object.
   */
    public boolean isNull(String key) {
        Object obj = this.opt(key);
        return JSONObject.NULL.equals(obj);
    }

    /** 
     * Utility function that returns an iterator of all the keys (attributes) of this JSONObject
     * @return An iterator of all keys in the object.
     *
    public Enumeration keys() {
        return this.keys();
        
    } */

    /** 
     * Utility function that returns a JSONArray of all the names of the keys (attributes) of this JSONObject
     * @return All the keys in the JSONObject as a JSONArray.
     */
    public JSONArray names() {
        Enumeration itr = this.keys();
        if (itr != null) {
            JSONArray array = new JSONArray();
            while (itr.hasMoreElements()) {
                array.add(itr.nextElement());
            }
            return array;
        }
        return null;
    }

    /** 
     * Utility function that returns a String[] of all the names of the keys (attributes) of this JSONObject
     * @return All the keys in the JSONObject as a String[].
     */
    public static String[] getNames(JSONObject obj) {
        String[] array = null;
        if (obj != null) {
            if (obj.size() > 0) {
                array = new String[obj.size()];
                int pos = 0;
                Enumeration itr = obj.keys();
                if (itr != null) {
                    while (itr.hasMoreElements()) {
                        array[pos] = (String)itr.nextElement();
                        pos++;
                    }
                }
            }
        }
        return array;
    }

//    /** 
//     * Utility function that returns an iterator of all the keys (attributes) of this JSONObject sorted in lexicographic manner (String.compareTo).
//     * @return An iterator of all keys in the object in lexicographic (character code) sorted order.
//     */
//    public Enumeration sortedKeys() {
//        Enumeration itr = this.keys();
//        if (itr != null && itr.hasMoreElements()) {
//            Vector vect = new Vector();
//            while (itr.hasMoreElements()) {
//                vect.addElement(itr.nextElement());
//            }
//            String[] strs = new String[vect.size()];
//            vect.copyInto(strs);
//           // java.util.Arrays.sort(strs);
//            vect.clear();
//            for (int i = 0; i < strs.length; i++) {
//                vect.add(strs[i]);
//            }
//            return vect.iterator();
//        }
//        return null;
//    }

    /**
     * End of convenience methods.
     */

    /**
     * Over-ridden toString() method.  Returns the same value as write(), which is a compact JSON String.
     * If an error occurs in the serialization, the return will be of format: JSON Generation Error: [<some error>]
     * @return A string of JSON text, if possible.
     */
    public String toString() {
        return toString(false);
    }

    /**
     * Verbose capable toString method.
     * If an error occurs in the serialization, the return will be of format: JSON Generation Error: [<some error>]
     * @param verbose Whether or not to tab-indent the output.
     * @return A string of JSON text, if possible.
     */
    public String toString(boolean verbose) {
        String str = null;
        try {
            str = write(verbose);    
        } catch (JSONException jex) {
            str = "JSON Generation Error: [" + jex.toString() + "]";
        }
        return str;
    }

    /**
     * Function to return a string of JSON text with specified indention.  Returns the same value as write(indentDepth).
     * If an error occurs in the serialization, a JSONException is thrown.
     * @return A string of JSON text, if possible.
     */
    public String toString(int indentDepth) throws JSONException {
        return write(indentDepth);
    }

    public static String quote(String string) {
        return Serializer.quote(string);
    }

    /**
     * An simple class provided for API compatibility to other JSON models that 'represents' 
     * 'null' in an actual object.
     */
    private static class Null implements JSONString {

        /**
         * Equals function that returns true for comparisons to null.
         */
        public boolean equals(Object obj) {
            if (obj == null || obj == this) {
                return true;
            }
            else {
                return false;
            }
        }

        /**
         * Ensure only one Null object.
         */
        protected Object clone() {
            return this;
        }
        
        /**
         * toString method that just returns 'null' as the string.
         */
        public String toString() {
            return "null";
        }

        /**
         * Method to return a JSON compliant representation of this object.
         * @return a JSON formatted string.
         */
        public String toJSONString() {
            return this.toString();
        }
    }
=======

    /**
     * Construct a JSONObject from a Map.
     *
     * @param map A map object that can be used to initialize the contents of
     *  the JSONObject.
     * @throws JSONException
     */
    public JSONObject(Map map) {
        this.map = new HashMap();
        if (map != null) {
            Iterator i = map.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry)i.next();
                Object value = e.getValue();
                if (value != null) {
                    this.map.put(e.getKey(), wrap(value));
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
        this.populateMap(bean);
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
        Class c = object.getClass();
        for (int i = 0; i < names.length; i += 1) {
            String name = names[i];
            try {
                this.putOpt(name, c.getField(name).get(object));
            } catch (Exception ignore) {
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
     * Construct a JSONObject from a ResourceBundle.
     * @param baseName The ResourceBundle base name.
     * @param locale The Locale to load the ResourceBundle for.
     * @throws JSONException If any JSONExceptions are detected.
     */
    public JSONObject(String baseName, Locale locale) throws JSONException {
        this();
        ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale,
                Thread.currentThread().getContextClassLoader());

// Iterate through the keys in the bundle.

        Enumeration keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (key instanceof String) {

// Go through the path, ensuring that there is a nested JSONObject for each
// segment except the last. Add the value using the last segment's name into
// the deepest nested JSONObject.

                String[] path = ((String)key).split("\\.");
                int last = path.length - 1;
                JSONObject target = this;
                for (int i = 0; i < last; i += 1) {
                    String segment = path[i];
                    JSONObject nextTarget = target.optJSONObject(segment);
                    if (nextTarget == null) {
                        nextTarget = new JSONObject();
                        target.put(segment, nextTarget);
                    }
                    target = nextTarget;
                }
                target.put(path[last], bundle.getString((String)key));
            }
        }
    }


    /**
     * Accumulate values under a key. It is similar to the put method except
     * that if there is already an object stored under the key then a
     * JSONArray is stored under the key to hold all of the accumulated values.
     * If there is already a JSONArray, then the new value is appended to it.
     * In contrast, the put method replaces the previous value.
     *
     * If only one value is accumulated that is not a JSONArray, then the
     * result will be the same as using put. But if multiple values are
     * accumulated, then the result will be like append.
     * @param key   A key string.
     * @param value An object to be accumulated under the key.
     * @return this.
     * @throws JSONException If the value is an invalid number
     *  or if the key is null.
     */
    public JSONObject accumulate(
        String key,
        Object value
    ) throws JSONException {
        testValidity(value);
        Object object = this.opt(key);
        if (object == null) {
            this.put(key, value instanceof JSONArray
                    ? new JSONArray().put(value)
                    : value);
        } else if (object instanceof JSONArray) {
            ((JSONArray)object).put(value);
        } else {
            this.put(key, new JSONArray().put(object).put(value));
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
    public JSONObject append(String key, Object value) throws JSONException {
        testValidity(value);
        Object object = this.opt(key);
        if (object == null) {
            this.put(key, new JSONArray().put(value));
        } else if (object instanceof JSONArray) {
            this.put(key, ((JSONArray)object).put(value));
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
    public static String doubleToString(double d) {
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            return "null";
        }

// Shave off trailing zeros and decimal point, if possible.

        String string = Double.toString(d);
        if (string.indexOf('.') > 0 && string.indexOf('e') < 0 &&
                string.indexOf('E') < 0) {
            while (string.endsWith("0")) {
                string = string.substring(0, string.length() - 1);
            }
            if (string.endsWith(".")) {
                string = string.substring(0, string.length() - 1);
            }
        }
        return string;
    }


    /**
     * Get the value object associated with a key.
     *
     * @param key   A key string.
     * @return      The object associated with the key.
     * @throws      JSONException if the key is not found.
     */
    public Object get(String key) throws JSONException {
        if (key == null) {
            throw new JSONException("Null key.");
        }
        Object object = this.opt(key);
        if (object == null) {
            throw new JSONException("JSONObject[" + quote(key) +
                    "] not found.");
        }
        return object;
    }


    /**
     * Get the boolean value associated with a key.
     *
     * @param key   A key string.
     * @return      The truth.
     * @throws      JSONException
     *  if the value is not a Boolean or the String "true" or "false".
     */
    public boolean getBoolean(String key) throws JSONException {
        Object object = this.get(key);
        if (object.equals(Boolean.FALSE) ||
                (object instanceof String &&
                ((String)object).equalsIgnoreCase("false"))) {
            return false;
        } else if (object.equals(Boolean.TRUE) ||
                (object instanceof String &&
                ((String)object).equalsIgnoreCase("true"))) {
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
        Object object = this.get(key);
        try {
            return object instanceof Number
                ? ((Number)object).doubleValue()
                : Double.parseDouble((String)object);
        } catch (Exception e) {
            throw new JSONException("JSONObject[" + quote(key) +
                "] is not a number.");
        }
    }


    /**
     * Get the int value associated with a key.
     *
     * @param key   A key string.
     * @return      The integer value.
     * @throws   JSONException if the key is not found or if the value cannot
     *  be converted to an integer.
     */
    public int getInt(String key) throws JSONException {
        Object object = this.get(key);
        try {
            return object instanceof Number
                ? ((Number)object).intValue()
                : Integer.parseInt((String)object);
        } catch (Exception e) {
            throw new JSONException("JSONObject[" + quote(key) +
                "] is not an int.");
        }
    }


    /**
     * Get the JSONArray value associated with a key.
     *
     * @param key   A key string.
     * @return      A JSONArray which is the value.
     * @throws      JSONException if the key is not found or
     *  if the value is not a JSONArray.
     */
    public JSONArray getJSONArray(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof JSONArray) {
            return (JSONArray)object;
        }
        throw new JSONException("JSONObject[" + quote(key) +
                "] is not a JSONArray.");
    }


    /**
     * Get the JSONObject value associated with a key.
     *
     * @param key   A key string.
     * @return      A JSONObject which is the value.
     * @throws      JSONException if the key is not found or
     *  if the value is not a JSONObject.
     */
    public JSONObject getJSONObject(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof JSONObject) {
            return (JSONObject)object;
        }
        throw new JSONException("JSONObject[" + quote(key) +
                "] is not a JSONObject.");
    }


    /**
     * Get the long value associated with a key.
     *
     * @param key   A key string.
     * @return      The long value.
     * @throws   JSONException if the key is not found or if the value cannot
     *  be converted to a long.
     */
    public long getLong(String key) throws JSONException {
        Object object = this.get(key);
        try {
            return object instanceof Number
                ? ((Number)object).longValue()
                : Long.parseLong((String)object);
        } catch (Exception e) {
            throw new JSONException("JSONObject[" + quote(key) +
                "] is not a long.");
        }
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
        Iterator iterator = jo.keys();
        String[] names = new String[length];
        int i = 0;
        while (iterator.hasNext()) {
            names[i] = (String)iterator.next();
            i += 1;
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
        Class klass = object.getClass();
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
     * @throws   JSONException if there is no string value for the key.
     */
    public String getString(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof String) {
            return (String)object;
        }
        throw new JSONException("JSONObject[" + quote(key) +
            "] not a string.");
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
     * Increment a property of a JSONObject. If there is no such property,
     * create one with a value of 1. If there is such a property, and if
     * it is an Integer, Long, Double, or Float, then add one to it.
     * @param key  A key string.
     * @return this.
     * @throws JSONException If there is already a property with this name
     * that is not an Integer, Long, Double, or Float.
     */
    public JSONObject increment(String key) throws JSONException {
        Object value = this.opt(key);
        if (value == null) {
            this.put(key, 1);
        } else if (value instanceof Integer) {
            this.put(key, ((Integer)value).intValue() + 1);
        } else if (value instanceof Long) {
            this.put(key, ((Long)value).longValue() + 1);
        } else if (value instanceof Double) {
            this.put(key, ((Double)value).doubleValue() + 1);
        } else if (value instanceof Float) {
            this.put(key, ((Float)value).floatValue() + 1);
        } else {
            throw new JSONException("Unable to increment [" + quote(key) + "].");
        }
        return this;
    }


    /**
     * Determine if the value associated with the key is null or if there is
     *  no value.
     * @param key   A key string.
     * @return      true if there is no value associated with the key or if
     *  the value is the JSONObject.NULL object.
     */
    public boolean isNull(String key) {
        return JSONObject.NULL.equals(this.opt(key));
    }


    /**
     * Get an enumeration of the keys of the JSONObject.
     *
     * @return An iterator of the keys.
     */
    public Iterator keys() {
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
        Iterator  keys = this.keys();
        while (keys.hasNext()) {
            ja.put(keys.next());
        }
        return ja.length() == 0 ? null : ja;
    }

    /**
     * Produce a string from a Number.
     * @param  number A Number
     * @return A String.
     * @throws JSONException If n is a non-finite number.
     */
    public static String numberToString(Number number)
            throws JSONException {
        if (number == null) {
            throw new JSONException("Null pointer");
        }
        testValidity(number);

// Shave off trailing zeros and decimal point, if possible.

        String string = number.toString();
        if (string.indexOf('.') > 0 && string.indexOf('e') < 0 &&
                string.indexOf('E') < 0) {
            while (string.endsWith("0")) {
                string = string.substring(0, string.length() - 1);
            }
            if (string.endsWith(".")) {
                string = string.substring(0, string.length() - 1);
            }
        }
        return string;
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
        return this.optBoolean(key, false);
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
            return this.getBoolean(key);
        } catch (Exception e) {
            return defaultValue;
        }
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
        return this.optDouble(key, Double.NaN);
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
            return this.getDouble(key);
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
        return this.optInt(key, 0);
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
            return this.getInt(key);
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
        Object o = this.opt(key);
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
        Object object = this.opt(key);
        return object instanceof JSONObject ? (JSONObject)object : null;
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
        return this.optLong(key, 0);
    }


    /**
     * Get an optional long value associated with a key,
     * or the default if there is no such key or if the value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key          A key string.
     * @param defaultValue The default.
     * @return             An object which is the value.
     */
    public long optLong(String key, long defaultValue) {
        try {
            return this.getLong(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get an optional string associated with a key.
     * It returns an empty string if there is no such key. If the value is not
     * a string and is not null, then it is converted to a string.
     *
     * @param key   A key string.
     * @return      A string which is the value.
     */
    public String optString(String key) {
        return this.optString(key, "");
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
        Object object = this.opt(key);
        return NULL.equals(object) ? defaultValue : object.toString();
    }


    private void populateMap(Object bean) {
        Class klass = bean.getClass();

// If klass is a System class then set includeSuperClass to false.

        boolean includeSuperClass = klass.getClassLoader() != null;

        Method[] methods = includeSuperClass
                ? klass.getMethods()
                : klass.getDeclaredMethods();
        for (int i = 0; i < methods.length; i += 1) {
            try {
                Method method = methods[i];
                if (Modifier.isPublic(method.getModifiers())) {
                    String name = method.getName();
                    String key = "";
                    if (name.startsWith("get")) {
                        if ("getClass".equals(name) ||
                                "getDeclaringClass".equals(name)) {
                            key = "";
                        } else {
                            key = name.substring(3);
                        }
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
                        if (result != null) {
                            this.map.put(key, wrap(result));
                        }
                    }
                }
            } catch (Exception ignore) {
            }
        }
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
        this.put(key, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }


    /**
     * Put a key/value pair in the JSONObject, where the value will be a
     * JSONArray which is produced from a Collection.
     * @param key   A key string.
     * @param value A Collection value.
     * @return      this.
     * @throws JSONException
     */
    public JSONObject put(String key, Collection value) throws JSONException {
        this.put(key, new JSONArray(value));
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
        this.put(key, new Double(value));
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
        this.put(key, new Integer(value));
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
        this.put(key, new Long(value));
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
    public JSONObject put(String key, Map value) throws JSONException {
        this.put(key, new JSONObject(value));
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
            this.remove(key);
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
            if (this.opt(key) != null) {
                throw new JSONException("Duplicate key \"" + key + "\"");
            }
            this.put(key, value);
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
            this.put(key, value);
        }
        return this;
    }


    /**
     * Produce a string in double quotes with backslash sequences in all the
     * right places. A backslash will be inserted within </, producing <\/,
     * allowing JSON text to be delivered in HTML. In JSON text, a string
     * cannot contain a control character or an unescaped quote or backslash.
     * @param string A String
     * @return  A String correctly formatted for insertion in a JSON text.
     */
    public static String quote(String string) {
        StringWriter sw = new StringWriter();
        synchronized (sw.getBuffer()) {
            try {
                return quote(string, sw).toString();
            } catch (IOException ignored) {
                // will never happen - we are writing to a string writer
                return "";
            }
        }
    }

    public static Writer quote(String string, Writer w) throws IOException {
        if (string == null || string.length() == 0) {
            w.write("\"\"");
            return w;
        }

        char b;
        char c = 0;
        String hhhh;
        int i;
        int len = string.length();

        w.write('"');
        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
            case '\\':
            case '"':
                w.write('\\');
                w.write(c);
                break;
            case '/':
                if (b == '<') {
                    w.write('\\');
                }
                w.write(c);
                break;
            case '\b':
                w.write("\\b");
                break;
            case '\t':
                w.write("\\t");
                break;
            case '\n':
                w.write("\\n");
                break;
            case '\f':
                w.write("\\f");
                break;
            case '\r':
                w.write("\\r");
                break;
            default:
                if (c < ' ' || (c >= '\u0080' && c < '\u00a0')
                        || (c >= '\u2000' && c < '\u2100')) {
                    hhhh = "000" + Integer.toHexString(c);
                    w.write("\\u" + hhhh.substring(hhhh.length() - 4));
                } else {
                    w.write(c);
                }
            }
        }
        w.write('"');
        return w;
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
     * Try to convert a string into a number, boolean, or null. If the string
     * can't be converted, return the string.
     * @param string A String.
     * @return A simple JSON value.
     */
    public static Object stringToValue(String string) {
        Double d;
        if (string.equals("")) {
            return string;
        }
        if (string.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (string.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        if (string.equalsIgnoreCase("null")) {
            return JSONObject.NULL;
        }

        /*
         * If it might be a number, try converting it.
         * If a number cannot be produced, then the value will just
         * be a string. Note that the plus and implied string
         * conventions are non-standard. A JSON parser may accept
         * non-JSON forms as long as it accepts all correct JSON forms.
         */

        char b = string.charAt(0);
        if ((b >= '0' && b <= '9') || b == '.' || b == '-' || b == '+') {
            try {
                if (string.indexOf('.') > -1 ||
                        string.indexOf('e') > -1 || string.indexOf('E') > -1) {
                    d = Double.valueOf(string);
                    if (!d.isInfinite() && !d.isNaN()) {
                        return d;
                    }
                } else {
                    Long myLong = new Long(string);
                    if (myLong.longValue() == myLong.intValue()) {
                        return new Integer(myLong.intValue());
                    } else {
                        return myLong;
                    }
                }
            }  catch (Exception ignore) {
            }
        }
        return string;
    }


    /**
     * Throw an exception if the object is a NaN or infinite number.
     * @param o The object to test.
     * @throws JSONException If o is a non-finite number.
     */
    public static void testValidity(Object o) throws JSONException {
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
    public String toString() {
        try {
            return this.toString(0);
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
        StringWriter w = new StringWriter();
        synchronized (w.getBuffer()) {
            return this.write(w, indentFactor, 0).toString();
        }
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
    public static String valueToString(Object value) throws JSONException {
        if (value == null || value.equals(null)) {
            return "null";
        }
        if (value instanceof JSONString) {
            Object object;
            try {
                object = ((JSONString)value).toJSONString();
            } catch (Exception e) {
                throw new JSONException(e);
            }
            if (object instanceof String) {
                return (String)object;
            }
            throw new JSONException("Bad value from toJSONString: " + object);
        }
        if (value instanceof Number) {
            return numberToString((Number) value);
        }
        if (value instanceof Boolean || value instanceof JSONObject ||
                value instanceof JSONArray) {
            return value.toString();
        }
        if (value instanceof Map) {
            return new JSONObject((Map)value).toString();
        }
        if (value instanceof Collection) {
            return new JSONArray((Collection)value).toString();
        }
        if (value.getClass().isArray()) {
            return new JSONArray(value).toString();
        }
        return quote(value.toString());
    }

     /**
      * Wrap an object, if necessary. If the object is null, return the NULL
      * object. If it is an array or collection, wrap it in a JSONArray. If
      * it is a map, wrap it in a JSONObject. If it is a standard property
      * (Double, String, et al) then it is already wrapped. Otherwise, if it
      * comes from one of the java packages, turn it into a string. And if
      * it doesn't, try to wrap it in a JSONObject. If the wrapping fails,
      * then null is returned.
      *
      * @param object The object to wrap
      * @return The wrapped value
      */
     public static Object wrap(Object object) {
         try {
             if (object == null) {
                 return NULL;
             }
             if (object instanceof JSONObject || object instanceof JSONArray  ||
                     NULL.equals(object)      || object instanceof JSONString ||
                     object instanceof Byte   || object instanceof Character  ||
                     object instanceof Short  || object instanceof Integer    ||
                     object instanceof Long   || object instanceof Boolean    ||
                     object instanceof Float  || object instanceof Double     ||
                     object instanceof String) {
                 return object;
             }

             if (object instanceof Collection) {
                 return new JSONArray((Collection)object);
             }
             if (object.getClass().isArray()) {
                 return new JSONArray(object);
             }
             if (object instanceof Map) {
                 return new JSONObject((Map)object);
             }
             Package objectPackage = object.getClass().getPackage();
             String objectPackageName = objectPackage != null
                 ? objectPackage.getName()
                 : "";
             if (
                 objectPackageName.startsWith("java.") ||
                 objectPackageName.startsWith("javax.") ||
                 object.getClass().getClassLoader() == null
             ) {
                 return object.toString();
             }
             return new JSONObject(object);
         } catch(Exception exception) {
             return null;
         }
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
        return this.write(writer, 0, 0);
    }


    static final Writer writeValue(Writer writer, Object value,
            int indentFactor, int indent) throws JSONException, IOException {
        if (value == null || value.equals(null)) {
            writer.write("null");
        } else if (value instanceof JSONObject) {
            ((JSONObject) value).write(writer, indentFactor, indent);
        } else if (value instanceof JSONArray) {
            ((JSONArray) value).write(writer, indentFactor, indent);
        } else if (value instanceof Map) {
            new JSONObject((Map) value).write(writer, indentFactor, indent);
        } else if (value instanceof Collection) {
            new JSONArray((Collection) value).write(writer, indentFactor,
                    indent);
        } else if (value.getClass().isArray()) {
            new JSONArray(value).write(writer, indentFactor, indent);
        } else if (value instanceof Number) {
            writer.write(numberToString((Number) value));
        } else if (value instanceof Boolean) {
            writer.write(value.toString());
        } else if (value instanceof JSONString) {
            Object o;
            try {
                o = ((JSONString) value).toJSONString();
            } catch (Exception e) {
                throw new JSONException(e);
            }
            writer.write(o != null ? o.toString() : quote(value.toString()));
        } else {
            quote(value.toString(), writer);
        }
        return writer;
    }

    static final void indent(Writer writer, int indent) throws IOException {
        for (int i = 0; i < indent; i += 1) {
            writer.write(' ');
        }
    }

    /**
     * Write the contents of the JSONObject as JSON text to a writer. For
     * compactness, no whitespace is added.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @return The writer.
     * @throws JSONException
     */
    Writer write(Writer writer, int indentFactor, int indent)
            throws JSONException {
        try {
            boolean commanate = false;
            final int length = this.length();
            Iterator keys = this.keys();
            writer.write('{');

            if (length == 1) {
                Object key = keys.next();
                writer.write(quote(key.toString()));
                writer.write(':');
                if (indentFactor > 0) {
                    writer.write(' ');
                }
                writeValue(writer, this.map.get(key), indentFactor, indent);
            } else if (length != 0) {
                final int newindent = indent + indentFactor;
                while (keys.hasNext()) {
                    Object key = keys.next();
                    if (commanate) {
                        writer.write(',');
                    }
                    if (indentFactor > 0) {
                        writer.write('\n');
                    }
                    indent(writer, newindent);
                    writer.write(quote(key.toString()));
                    writer.write(':');
                    if (indentFactor > 0) {
                        writer.write(' ');
                    }
                    writeValue(writer, this.map.get(key), indentFactor,
                            newindent);
                    commanate = true;
                }
                if (indentFactor > 0) {
                    writer.write('\n');
                }
                indent(writer, indent);
            }
            writer.write('}');
            return writer;
        } catch (IOException exception) {
            throw new JSONException(exception);
        }
     }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

