<<<<<<< HEAD
package org.json.me;

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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

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
 * coersion for you.
 * <p>
 * The <code>put</code> methods adds values to an object. For example, <pre>
 *     myString = new JSONObject().put("JSON", "Hello, World!").toString();</pre>
 * produces the string <code>{"JSON": "Hello, World"}</code>.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * the JSON sysntax rules.
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
 * <li>Comments written in the slashshlash, slashstar, and hash conventions
 *     will be ignored.</li>
 * </ul>
 * @author JSON.org
 * @version 2
 */
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
     * The hash map where the JSONObject's properties are kept.
     */
    private Hashtable myHashMap;


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
        this.myHashMap = new Hashtable();
    }


    /**
     * Construct a JSONObject from a subset of another JSONObject.
     * An array of strings is used to identify the keys that should be copied.
     * Missing keys are ignored.
     * @param jo A JSONObject.
     * @param sa An array of strings.
     * @exception JSONException If a value is a non-finite number.
     */
    public JSONObject(JSONObject jo, String[] sa) throws JSONException {
        this();
        for (int i = 0; i < sa.length; i += 1) {
            putOpt(sa[i], jo.opt(sa[i]));
        }
    }


    /**
     * Construct a JSONObject from a JSONTokener.
     * @param x A JSONTokener object containing the source string.
     * @throws JSONException If there is a syntax error in the source string.
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
            put(key, x.nextValue());

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
     * @param map A map object that can be used to initialize the contents of
     *  the JSONObject.
     */
    public JSONObject(Hashtable map) {
        if (map == null) {
            this.myHashMap = new Hashtable();
        } else {
            this.myHashMap = new Hashtable(map.size());
            Enumeration keys = map.keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                this.myHashMap.put(key, map.get(key));
            }
        }
    }
    
    
    /**
     * Construct a JSONObject from a string.
     * This is the most commonly used JSONObject constructor.
     * @param string    A string beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @exception JSONException If there is a syntax error in the source string.
     */
    public JSONObject(String string) throws JSONException {
        this(new JSONTokener(string));
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
            put(key, value);
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
     * 	associated with the key is not a JSONArray.
     */
    public JSONObject append(String key, Object value)
            throws JSONException {
        testValidity(value);
        Object o = opt(key);
        if (o == null) {
            put(key, new JSONArray().put(value));
        } else if (o instanceof JSONArray) {
            throw new JSONException("JSONObject[" + key + 
            		"] is not a JSONArray.");
        } else {
            put(key, new JSONArray().put(o).put(value));
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
        if (o instanceof Byte) {
            return (double) ((Byte)o).byteValue();
        } else if (o instanceof Short) {
            return (double) ((Short)o).shortValue();
        } else if (o instanceof Integer) {
            return (double) ((Integer)o).intValue();
        } else if (o instanceof Long) {
            return (double) ((Long)o).longValue();
        } else if (o instanceof Float) {
            return (double) ((Float)o).floatValue();
        } else if (o instanceof Double) {
            return ((Double)o).doubleValue();
        } else if (o instanceof String) {
            try {
                return Double.valueOf((String)o).doubleValue();
            } catch (Exception e) {
                throw new JSONException("JSONObject[" + quote(key) +
                    "] is not a number.");
            }
        } 
        throw new JSONException("JSONObject[" + quote(key) +
            "] is not a number.");
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
        if (o instanceof Byte) {
            return ((Byte)o).byteValue();
        } else if (o instanceof Short) {
            return ((Short)o).shortValue();
        } else if (o instanceof Integer) {
            return ((Integer)o).intValue();
        } else if (o instanceof Long) {
            return (int) ((Long)o).longValue();
        } else if (o instanceof Float) {
            return (int) ((Float)o).floatValue();
        } else if (o instanceof Double) {
            return (int) ((Double)o).doubleValue();
        } else if (o instanceof String) {
            return (int) getDouble(key);
        } 
        throw new JSONException("JSONObject[" + quote(key) +
            "] is not a number.");
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
        if (o instanceof Byte) {
            return ((Byte)o).byteValue();
        } else if (o instanceof Short) {
            return ((Short)o).shortValue();
        } else if (o instanceof Integer) {
            return ((Integer)o).intValue();
        } else if (o instanceof Long) {
            return ((Long)o).longValue();
        } else if (o instanceof Float) {
            return (long) ((Float)o).floatValue();
        } else if (o instanceof Double) {
            return (long) ((Double)o).doubleValue();
        } else if (o instanceof String) {
            return (long) getDouble(key);
        } 
        throw new JSONException("JSONObject[" + quote(key) +
            "] is not a number.");
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
        return this.myHashMap.containsKey(key);
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
    public Enumeration keys() {
        return this.myHashMap.keys();
    }


    /**
     * Get the number of keys stored in the JSONObject.
     *
     * @return The number of keys in the JSONObject.
     */
    public int length() {
        return this.myHashMap.size();
    }


    /**
     * Produce a JSONArray containing the names of the elements of this
     * JSONObject.
     * @return A JSONArray containing the key strings, or null if the JSONObject
     * is empty.
     */
    public JSONArray names() {
        JSONArray ja = new JSONArray();
        Enumeration  keys = keys();
        while (keys.hasMoreElements()) {
            ja.put(keys.nextElement());
        }
        return ja.length() == 0 ? null : ja;
    }

    
    /**
     * Shave off trailing zeros and decimal point, if possible.
     */
    static public String trimNumber(String s) {
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
     * Produce a string from a Number.
     * @param  n A Number
     * @return A String.
     * @throws JSONException If n is a non-finite number.
     */
    static public String numberToString(Object n)
            throws JSONException {
        if (n == null) {
            throw new JSONException("Null pointer");
        }
        testValidity(n);
        return trimNumber(n.toString());
    }

    /**
     * Get an optional value associated with a key.
     * @param key   A key string.
     * @return      An object which is the value, or null if there is no value.
     */
    public Object opt(String key) {
        return key == null ? null : this.myHashMap.get(key);
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
     * @param key 	A key string.
     * @param value	A Collection value.
     * @return		this.
     * @throws JSONException
     */
    public JSONObject put(String key, Vector value) throws JSONException {
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
            return Double.parseDouble((String)o);
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
     * @param key 	A key string.
     * @param value	A Map value.
     * @return		this.
     * @throws JSONException
     */
    public JSONObject put(String key, Hashtable value) throws JSONException {
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
            this.myHashMap.put(key, value);
        } else {
            remove(key);
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
                if (c < ' ') {
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
        return this.myHashMap.remove(key);
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
                        "JSON does not allow non-finite numbers");
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
            Enumeration keys = keys();
            StringBuffer sb = new StringBuffer("{");

            while (keys.hasMoreElements()) {
                if (sb.length() > 1) {
                    sb.append(',');
                }
                Object o = keys.nextElement();
                sb.append(quote(o.toString()));
                sb.append(':');
                sb.append(valueToString(this.myHashMap.get(o)));
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
        int          i;
        int          n = length();
        if (n == 0) {
            return "{}";
        }
        Enumeration keys = keys();
        StringBuffer sb = new StringBuffer("{");
        int          newindent = indent + indentFactor;
        Object       o;
        if (n == 1) {
            o = keys.nextElement();
            sb.append(quote(o.toString()));
            sb.append(": ");
            sb.append(valueToString(this.myHashMap.get(o), indentFactor,
                    indent));
        } else {
            while (keys.hasMoreElements()) {
                o = keys.nextElement();
                if (sb.length() > 1) {
                    sb.append(",\n");
                } else {
                    sb.append('\n');
                }
                for (i = 0; i < newindent; i += 1) {
                    sb.append(' ');
                }
                sb.append(quote(o.toString()));
                sb.append(": ");
                sb.append(valueToString(this.myHashMap.get(o), indentFactor,
                        newindent));
            }
            if (sb.length() > 1) {
                sb.append('\n');
                for (i = 0; i < indent; i += 1) {
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
     * produced by the rules.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     * @param value The value to be serialized.
     * @return a printable, displayable, transmittable
     *  representation of the object, beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws JSONException If the value is or contains an invalid number.
     */
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
        if (value instanceof Float || value instanceof Double ||
            value instanceof Byte || value instanceof Short || 
            value instanceof Integer || value instanceof Long) {
            return numberToString(value);
        }
        if (value instanceof Boolean || value instanceof JSONObject ||
                value instanceof JSONArray) {
            return value.toString();
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
        if (value instanceof Float || value instanceof Double ||
            value instanceof Byte || value instanceof Short || 
            value instanceof Integer || value instanceof Long) {
            return numberToString(value);
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
            Enumeration keys = keys();
            writer.write('{');

            while (keys.hasMoreElements()) {
                if (b) {
                    writer.write(',');
                }
                Object k = keys.nextElement();
                writer.write(quote(k.toString()));
                writer.write(':');
                Object v = this.myHashMap.get(k);
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
=======
/*
 * Copyright 2004 The Closure Compiler Authors.
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
 */

package com.google.javascript.jscomp;

import com.google.common.base.Preconditions;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.head.ScriptRuntime;
import com.google.javascript.rhino.jstype.TernaryValue;

/**
 * Peephole optimization to fold constants (e.g. x + 1 + 7 --> x + 8).
 *
 */
class PeepholeFoldConstants extends AbstractPeepholeOptimization {

  // TODO(johnlenz): optimizations should not be emiting errors. Move these to
  // a check pass.
  static final DiagnosticType INVALID_GETELEM_INDEX_ERROR =
      DiagnosticType.warning(
          "JSC_INVALID_GETELEM_INDEX_ERROR",
          "Array index not integer: {0}");

  static final DiagnosticType INDEX_OUT_OF_BOUNDS_ERROR =
      DiagnosticType.warning(
          "JSC_INDEX_OUT_OF_BOUNDS_ERROR",
          "Array index out of bounds: {0}");

  static final DiagnosticType NEGATING_A_NON_NUMBER_ERROR =
      DiagnosticType.warning(
          "JSC_NEGATING_A_NON_NUMBER_ERROR",
          "Can't negate non-numeric value: {0}");

  static final DiagnosticType BITWISE_OPERAND_OUT_OF_RANGE =
      DiagnosticType.warning(
          "JSC_BITWISE_OPERAND_OUT_OF_RANGE",
          "Operand out of range, bitwise operation will lose information: {0}");

  static final DiagnosticType SHIFT_AMOUNT_OUT_OF_BOUNDS =
      DiagnosticType.warning(
          "JSC_SHIFT_AMOUNT_OUT_OF_BOUNDS",
          "Shift amount out of bounds: {0}");

  static final DiagnosticType FRACTIONAL_BITWISE_OPERAND =
      DiagnosticType.warning(
          "JSC_FRACTIONAL_BITWISE_OPERAND",
          "Fractional bitwise operand: {0}");

  private static final double MAX_FOLD_NUMBER = Math.pow(2, 53);

  private final boolean late;

  /**
   * @param late When late is false, this mean we are currently running before
   * most of the other optimizations. In this case we would avoid optimizations
   * that would make the code harder to analyze. When this is true, we would
   * do anything to minimize for size.
   */
  PeepholeFoldConstants(boolean late) {
    this.late = late;
  }

  @Override
  Node optimizeSubtree(Node subtree) {
    switch(subtree.getType()) {
      case Token.NEW:
        return tryFoldCtorCall(subtree);

      case Token.TYPEOF:
        return tryFoldTypeof(subtree);

      case Token.NOT:
      case Token.POS:
      case Token.NEG:
      case Token.BITNOT:
        tryReduceOperandsForOp(subtree);
        return tryFoldUnaryOperator(subtree);

      case Token.VOID:
        return tryReduceVoid(subtree);

      default:
        tryReduceOperandsForOp(subtree);
        return tryFoldBinaryOperator(subtree);
    }
  }

  private Node tryFoldBinaryOperator(Node subtree) {
    Node left = subtree.getFirstChild();

    if (left == null) {
      return subtree;
    }

    Node right = left.getNext();

    if (right == null) {
      return subtree;
    }

    // If we've reached here, node is truly a binary operator.
    switch(subtree.getType()) {
      case Token.GETPROP:
        return tryFoldGetProp(subtree, left, right);

      case Token.GETELEM:
        return tryFoldGetElem(subtree, left, right);

      case Token.INSTANCEOF:
        return tryFoldInstanceof(subtree, left, right);

      case Token.AND:
      case Token.OR:
        return tryFoldAndOr(subtree, left, right);

      case Token.LSH:
      case Token.RSH:
      case Token.URSH:
        return tryFoldShift(subtree, left, right);

      case Token.ASSIGN:
        return tryFoldAssign(subtree, left, right);

      case Token.ASSIGN_BITOR:
      case Token.ASSIGN_BITXOR:
      case Token.ASSIGN_BITAND:
      case Token.ASSIGN_LSH:
      case Token.ASSIGN_RSH:
      case Token.ASSIGN_URSH:
      case Token.ASSIGN_ADD:
      case Token.ASSIGN_SUB:
      case Token.ASSIGN_MUL:
      case Token.ASSIGN_DIV:
      case Token.ASSIGN_MOD:
        return tryUnfoldAssignOp(subtree, left, right);

      case Token.ADD:
        return tryFoldAdd(subtree, left, right);

      case Token.SUB:
      case Token.DIV:
      case Token.MOD:
        return tryFoldArithmeticOp(subtree, left, right);

      case Token.MUL:
      case Token.BITAND:
      case Token.BITOR:
      case Token.BITXOR:
        Node result = tryFoldArithmeticOp(subtree, left, right);
        if (result != subtree) {
          return result;
        }
        return tryFoldLeftChildOp(subtree, left, right);

      case Token.LT:
      case Token.GT:
      case Token.LE:
      case Token.GE:
      case Token.EQ:
      case Token.NE:
      case Token.SHEQ:
      case Token.SHNE:
        return tryFoldComparison(subtree, left, right);

      default:
        return subtree;
    }
  }

  private Node tryReduceVoid(Node n) {
    Node child = n.getFirstChild();
    if (!child.isNumber() || child.getDouble() != 0.0) {
      if (!mayHaveSideEffects(n)) {
        n.replaceChild(child, IR.number(0));
        reportCodeChange();
      }
    }
    return n;
  }

  private void tryReduceOperandsForOp(Node n) {
    switch (n.getType()) {
      case Token.ADD:
        Node left = n.getFirstChild();
        Node right = n.getLastChild();
        if (!NodeUtil.mayBeString(left) && !NodeUtil.mayBeString(right)) {
          tryConvertOperandsToNumber(n);
        }
        break;
      case Token.ASSIGN_BITOR:
      case Token.ASSIGN_BITXOR:
      case Token.ASSIGN_BITAND:
        // TODO(johnlenz): convert these to integers.
      case Token.ASSIGN_LSH:
      case Token.ASSIGN_RSH:
      case Token.ASSIGN_URSH:
      case Token.ASSIGN_SUB:
      case Token.ASSIGN_MUL:
      case Token.ASSIGN_MOD:
      case Token.ASSIGN_DIV:
        tryConvertToNumber(n.getLastChild());
        break;
      case Token.BITNOT:
      case Token.BITOR:
      case Token.BITXOR:
      case Token.BITAND:
      case Token.LSH:
      case Token.RSH:
      case Token.URSH:
      case Token.SUB:
      case Token.MUL:
      case Token.MOD:
      case Token.DIV:
      case Token.POS:
      case Token.NEG:
        tryConvertOperandsToNumber(n);
        break;
    }
  }

  private void tryConvertOperandsToNumber(Node n) {
    Node next;
    for (Node c = n.getFirstChild(); c != null; c = next) {
      next = c.getNext();
      tryConvertToNumber(c);
    }
  }

  private void tryConvertToNumber(Node n) {
    switch (n.getType()) {
      case Token.NUMBER:
        // Nothing to do
        return;
      case Token.AND:
      case Token.OR:
      case Token.COMMA:
        tryConvertToNumber(n.getLastChild());
        return;
      case Token.HOOK:
        tryConvertToNumber(n.getChildAtIndex(1));
        tryConvertToNumber(n.getLastChild());
        return;
      case Token.NAME:
        if (!NodeUtil.isUndefined(n)) {
          return;
        }
        break;
    }

    Double result = NodeUtil.getNumberValue(n);
    if (result == null) {
      return;
    }

    double value = result;

    Node replacement = NodeUtil.numberNode(value, n);
    if (replacement.isEquivalentTo(n)) {
      return;
    }

    n.getParent().replaceChild(n, replacement);
    reportCodeChange();
  }

  /**
   * Folds 'typeof(foo)' if foo is a literal, e.g.
   * typeof("bar") --> "string"
   * typeof(6) --> "number"
   */
  private Node tryFoldTypeof(Node originalTypeofNode) {
    Preconditions.checkArgument(originalTypeofNode.isTypeOf());

    Node argumentNode = originalTypeofNode.getFirstChild();
    if (argumentNode == null || !NodeUtil.isLiteralValue(argumentNode, true)) {
      return originalTypeofNode;
    }

    String typeNameString = null;

    switch (argumentNode.getType()) {
      case Token.FUNCTION:
        typeNameString = "function";
        break;
      case Token.STRING:
        typeNameString = "string";
        break;
      case Token.NUMBER:
        typeNameString = "number";
        break;
      case Token.TRUE:
      case Token.FALSE:
        typeNameString = "boolean";
        break;
      case Token.NULL:
      case Token.OBJECTLIT:
      case Token.ARRAYLIT:
        typeNameString = "object";
        break;
      case Token.VOID:
        typeNameString = "undefined";
        break;
      case Token.NAME:
        // We assume here that programs don't change the value of the
        // keyword undefined to something other than the value undefined.
        if ("undefined".equals(argumentNode.getString())) {
          typeNameString = "undefined";
        }
        break;
    }

    if (typeNameString != null) {
      Node newNode = IR.string(typeNameString);
      originalTypeofNode.getParent().replaceChild(originalTypeofNode, newNode);
      reportCodeChange();

      return newNode;
    }

    return originalTypeofNode;
  }

  private Node tryFoldUnaryOperator(Node n) {
    Preconditions.checkState(n.hasOneChild());

    Node left = n.getFirstChild();
    Node parent = n.getParent();

    if (left == null) {
      return n;
    }

    TernaryValue leftVal = NodeUtil.getPureBooleanValue(left);
    if (leftVal == TernaryValue.UNKNOWN) {
      return n;
    }

    switch (n.getType()) {
      case Token.NOT:
        // Don't fold !0 and !1 back to false.
        if (late && left.isNumber()) {
          double numValue = left.getDouble();
          if (numValue == 0 || numValue == 1) {
            return n;
          }
        }
        Node replacementNode = NodeUtil.booleanNode(!leftVal.toBoolean(true));
        parent.replaceChild(n, replacementNode);
        reportCodeChange();
        return replacementNode;
      case Token.POS:
        if (NodeUtil.isNumericResult(left)) {
          // POS does nothing to numeric values.
          parent.replaceChild(n, left.detachFromParent());
          reportCodeChange();
          return left;
        }
        return n;
      case Token.NEG:
        if (left.isName()) {
          if (left.getString().equals("Infinity")) {
            // "-Infinity" is valid and a literal, don't modify it.
            return n;
          } else if (left.getString().equals("NaN")) {
            // "-NaN" is "NaN".
            n.removeChild(left);
            parent.replaceChild(n, left);
            reportCodeChange();
            return left;
          }
        }

        if (left.isNumber()) {
          double negNum = -left.getDouble();

          Node negNumNode = IR.number(negNum);
          parent.replaceChild(n, negNumNode);
          reportCodeChange();
          return negNumNode;
        } else {
          // left is not a number node, so do not replace, but warn the
          // user because they can't be doing anything good
          report(NEGATING_A_NON_NUMBER_ERROR, left);
          return n;
        }
      case Token.BITNOT:
        try {
          double val = left.getDouble();
          if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
            int intVal = (int) val;
            if (intVal == val) {
              Node notIntValNode = IR.number(~intVal);
              parent.replaceChild(n, notIntValNode);
              reportCodeChange();
              return notIntValNode;
            } else {
              report(FRACTIONAL_BITWISE_OPERAND, left);
              return n;
            }
          } else {
            report(BITWISE_OPERAND_OUT_OF_RANGE, left);
            return n;
          }
        } catch (UnsupportedOperationException ex) {
          // left is not a number node, so do not replace, but warn the
          // user because they can't be doing anything good
          report(NEGATING_A_NON_NUMBER_ERROR, left);
          return n;
        }
        default:
          return n;
    }
  }

  /**
   * Try to fold {@code left instanceof right} into {@code true}
   * or {@code false}.
   */
  private Node tryFoldInstanceof(Node n, Node left, Node right) {
    Preconditions.checkArgument(n.isInstanceOf());

    // TODO(johnlenz) Use type information if available to fold
    // instanceof.
    if (NodeUtil.isLiteralValue(left, true)
        && !mayHaveSideEffects(right)) {

      Node replacementNode = null;

      if (NodeUtil.isImmutableValue(left)) {
        // Non-object types are never instances.
        replacementNode = IR.falseNode();
      } else if (right.isName()
          && "Object".equals(right.getString())) {
        replacementNode = IR.trueNode();
      }

      if (replacementNode != null) {
        n.getParent().replaceChild(n, replacementNode);
        reportCodeChange();
        return replacementNode;
      }
    }

    return n;
  }

  private Node tryFoldAssign(Node n, Node left, Node right) {
    Preconditions.checkArgument(n.isAssign());

    if (!late) {
      return n;
    }

    // Tries to convert x = x + y -> x += y;
    if (!right.hasChildren() ||
        right.getFirstChild().getNext() != right.getLastChild()) {
      // RHS must have two children.
      return n;
    }

    if (mayHaveSideEffects(left)) {
      return n;
    }

    Node newRight;
    if (areNodesEqualForInlining(left, right.getFirstChild())) {
      newRight = right.getLastChild();
    } else if (NodeUtil.isCommutative(right.getType()) &&
          areNodesEqualForInlining(left, right.getLastChild())) {
      newRight = right.getFirstChild();
    } else {
      return n;
    }

    int newType = -1;
    switch (right.getType()) {
      case Token.ADD:
        newType = Token.ASSIGN_ADD;
        break;
      case Token.BITAND:
        newType = Token.ASSIGN_BITAND;
        break;
      case Token.BITOR:
        newType = Token.ASSIGN_BITOR;
        break;
      case Token.BITXOR:
        newType = Token.ASSIGN_BITXOR;
        break;
      case Token.DIV:
        newType = Token.ASSIGN_DIV;
        break;
      case Token.LSH:
        newType = Token.ASSIGN_LSH;
        break;
      case Token.MOD:
        newType = Token.ASSIGN_MOD;
        break;
      case Token.MUL:
        newType = Token.ASSIGN_MUL;
        break;
      case Token.RSH:
        newType = Token.ASSIGN_RSH;
        break;
      case Token.SUB:
        newType = Token.ASSIGN_SUB;
        break;
      case Token.URSH:
        newType = Token.ASSIGN_URSH;
        break;
      default:
        return n;
    }

    Node newNode = new Node(newType,
        left.detachFromParent(), newRight.detachFromParent());
    n.getParent().replaceChild(n, newNode);

    reportCodeChange();

    return newNode;
  }

  private Node tryUnfoldAssignOp(Node n, Node left, Node right) {
    if (late) {
      return n;
    }

    if (!n.hasChildren() ||
        n.getFirstChild().getNext() != n.getLastChild()) {
      return n;
    }

    if (mayHaveSideEffects(left)) {
      return n;
    }

    // Tries to convert x += y -> x = x + y;
    int op = NodeUtil.getOpFromAssignmentOp(n);
    Node replacement = IR.assign(left.detachFromParent(),
        new Node(op, left.cloneTree(), right.detachFromParent())
            .srcref(n));
    n.getParent().replaceChild(n, replacement);
    reportCodeChange();

    return replacement;
  }

  /**
   * Try to fold a AND/OR node.
   */
  private Node tryFoldAndOr(Node n, Node left, Node right) {
    Node parent = n.getParent();

    Node result = null;

    int type = n.getType();

    TernaryValue leftVal = NodeUtil.getImpureBooleanValue(left);

    if (leftVal != TernaryValue.UNKNOWN) {
      boolean lval = leftVal.toBoolean(true);

      // (TRUE || x) => TRUE (also, (3 || x) => 3)
      // (FALSE && x) => FALSE
      if (lval && type == Token.OR ||
          !lval && type == Token.AND) {
        result = left;

      } else if (!mayHaveSideEffects(left)) {
        // (FALSE || x) => x
        // (TRUE && x) => x
        result = right;
      }
    }

    // Note: Right hand side folding is handled by
    // PeepholeSubstituteAlternateSyntax#tryMinimizeCondition

    if (result != null) {
      // Fold it!
      n.removeChild(result);
      parent.replaceChild(n, result);
      reportCodeChange();

      return result;
    } else {
      return n;
    }
  }

  /**
   * Expressions such as [foo() + 'a' + 'b'] generate parse trees
   * where no node has two const children ((foo() + 'a') + 'b'), so
   * tryFoldAdd() won't fold it -- tryFoldLeftChildAdd() will (for Strings).
   * Specifically, it folds Add expressions where:
   *  - The left child is also and add expression
   *  - The right child is a constant value
   *  - The left child's right child is a STRING constant.
   */
  private Node tryFoldChildAddString(Node n, Node left, Node right) {

    if (NodeUtil.isLiteralValue(right, false) &&
        left.isAdd()) {

      Node ll = left.getFirstChild();
      Node lr = ll.getNext();

      // Left's right child MUST be a string. We would not want to fold
      // foo() + 2 + 'a' because we don't know what foo() will return, and
      // therefore we don't know if left is a string concat, or a numeric add.
      if (lr.isString()) {
        String leftString = NodeUtil.getStringValue(lr);
        String rightString = NodeUtil.getStringValue(right);
        if (leftString != null && rightString != null) {
          left.removeChild(ll);
          String result = leftString + rightString;
          n.replaceChild(left, ll);
          n.replaceChild(right, IR.string(result));
          reportCodeChange();
          return n;
        }
      }
    }

    if (NodeUtil.isLiteralValue(left, false) &&
        right.isAdd()) {

      Node rl = right.getFirstChild();
      Node rr = right.getLastChild();

      // Left's right child MUST be a string. We would not want to fold
      // foo() + 2 + 'a' because we don't know what foo() will return, and
      // therefore we don't know if left is a string concat, or a numeric add.
      if (rl.isString()) {
        String leftString = NodeUtil.getStringValue(left);
        String rightString = NodeUtil.getStringValue(rl);
        if (leftString != null && rightString != null) {
          right.removeChild(rr);
          String result = leftString + rightString;
          n.replaceChild(right, rr);
          n.replaceChild(left, IR.string(result));
          reportCodeChange();
          return n;
        }
      }
    }

    return n;
  }

  /**
   * Try to fold an ADD node with constant operands
   */
  private Node tryFoldAddConstantString(Node n, Node left, Node right) {
    if (left.isString() ||
        right.isString()) {
      // Add strings.
      String leftString = NodeUtil.getStringValue(left);
      String rightString = NodeUtil.getStringValue(right);
      if (leftString != null && rightString != null) {
        Node newStringNode = IR.string(leftString + rightString);
        n.getParent().replaceChild(n, newStringNode);
        reportCodeChange();
        return newStringNode;
      }
    }



    return n;
  }

  /**
   * Try to fold arithmetic binary operators
   */
  private Node tryFoldArithmeticOp(Node n, Node left, Node right) {
    Node result = performArithmeticOp(n.getType(), left, right);
    if (result != null) {
      result.copyInformationFromForTree(n);
      n.getParent().replaceChild(n, result);
      reportCodeChange();
      return result;
    }
    return n;
  }

  /**
   * Try to fold arithmetic binary operators
   */
  private Node performArithmeticOp(int opType, Node left, Node right) {
    // Unlike other operations, ADD operands are not always converted
    // to Number.
    if (opType == Token.ADD
        && (NodeUtil.mayBeString(left, false)
            || NodeUtil.mayBeString(right, false))) {
      return null;
    }

    double result;

    // TODO(johnlenz): Handle NaN with unknown value. BIT ops convert NaN
    // to zero so this is a little awkward here.

    Double lValObj = NodeUtil.getNumberValue(left);
    if (lValObj == null) {
      return null;
    }
    Double rValObj = NodeUtil.getNumberValue(right);
    if (rValObj == null) {
      return null;
    }

    double lval = lValObj;
    double rval = rValObj;

    switch (opType) {
      case Token.BITAND:
        result = ScriptRuntime.toInt32(lval) & ScriptRuntime.toInt32(rval);
        break;
      case Token.BITOR:
        result = ScriptRuntime.toInt32(lval) | ScriptRuntime.toInt32(rval);
        break;
      case Token.BITXOR:
        result = ScriptRuntime.toInt32(lval) ^ ScriptRuntime.toInt32(rval);
        break;
      case Token.ADD:
        result = lval + rval;
        break;
      case Token.SUB:
        result = lval - rval;
        break;
      case Token.MUL:
        result = lval * rval;
        break;
      case Token.MOD:
        if (rval == 0) {
          return null;
        }
        result = lval % rval;
        break;
      case Token.DIV:
        if (rval == 0) {
          return null;
        }
        result = lval / rval;
        break;
      default:
        throw new Error("Unexpected arithmetic operator");
    }

    // TODO(johnlenz): consider removing the result length check.
    // length of the left and right value plus 1 byte for the operator.
    if ((String.valueOf(result).length() <=
        String.valueOf(lval).length() + String.valueOf(rval).length() + 1

        // Do not try to fold arithmetic for numbers > 2^53. After that
        // point, fixed-point math starts to break down and become inaccurate.
        && Math.abs(result) <= MAX_FOLD_NUMBER)
        || Double.isNaN(result)
        || result == Double.POSITIVE_INFINITY
        || result == Double.NEGATIVE_INFINITY) {
      return NodeUtil.numberNode(result, null);
    }
    return null;
  }

  /**
   * Expressions such as [foo() * 10 * 20] generate parse trees
   * where no node has two const children ((foo() * 10) * 20), so
   * performArithmeticOp() won't fold it -- tryFoldLeftChildOp() will.
   * Specifically, it folds associative expressions where:
   *  - The left child is also an associative expression of the same time.
   *  - The right child is a constant NUMBER constant.
   *  - The left child's right child is a NUMBER constant.
   */
  private Node tryFoldLeftChildOp(Node n, Node left, Node right) {
    int opType = n.getType();
    Preconditions.checkState(
        (NodeUtil.isAssociative(opType) && NodeUtil.isCommutative(opType))
        || n.isAdd());

    Preconditions.checkState(
        !n.isAdd()|| !NodeUtil.mayBeString(n));

    // Use getNumberValue to handle constants like "NaN" and "Infinity"
    // other values are converted to numbers elsewhere.
    Double rightValObj = NodeUtil.getNumberValue(right);
    if (rightValObj != null && left.getType() == opType) {
      Preconditions.checkState(left.getChildCount() == 2);

      Node ll = left.getFirstChild();
      Node lr = ll.getNext();

      Node valueToCombine = ll;
      Node replacement = performArithmeticOp(opType, valueToCombine, right);
      if (replacement == null) {
        valueToCombine = lr;
        replacement = performArithmeticOp(opType, valueToCombine, right);
      }
      if (replacement != null) {
        // Remove the child that has been combined
        left.removeChild(valueToCombine);
        // Replace the left op with the remaining child.
        n.replaceChild(left, left.removeFirstChild());
        // New "-Infinity" node need location info explicitly
        // added.
        replacement.copyInformationFromForTree(right);
        n.replaceChild(right, replacement);
        reportCodeChange();
      }
    }

    return n;
  }

  private Node tryFoldAdd(Node node, Node left, Node right) {
    Preconditions.checkArgument(node.isAdd());

    if (NodeUtil.mayBeString(node, true)) {
      if (NodeUtil.isLiteralValue(left, false) &&
          NodeUtil.isLiteralValue(right, false)) {
        // '6' + 7
        return tryFoldAddConstantString(node, left, right);
      } else {
        // a + 7 or 6 + a
        return tryFoldChildAddString(node, left, right);
      }
    } else {
      // Try arithmetic add
      Node result = tryFoldArithmeticOp(node, left, right);
      if (result != node) {
        return result;
      }
      return tryFoldLeftChildOp(node, left, right);
    }
  }

  /**
   * Try to fold shift operations
   */
  private Node tryFoldShift(Node n, Node left, Node right) {
    if (left.isNumber() &&
        right.isNumber()) {

      double result;
      double lval = left.getDouble();
      double rval = right.getDouble();

      // check ranges.  We do not do anything that would clip the double to
      // a 32-bit range, since the user likely does not intend that.
      if (!(lval >= Integer.MIN_VALUE && lval <= Integer.MAX_VALUE)) {
        report(BITWISE_OPERAND_OUT_OF_RANGE, left);
        return n;
      }

      // only the lower 5 bits are used when shifting, so don't do anything
      // if the shift amount is outside [0,32)
      if (!(rval >= 0 && rval < 32)) {
        report(SHIFT_AMOUNT_OUT_OF_BOUNDS, right);
        return n;
      }

      // Convert the numbers to ints
      int lvalInt = (int) lval;
      if (lvalInt != lval) {
        report(FRACTIONAL_BITWISE_OPERAND, left);
        return n;
      }

      int rvalInt = (int) rval;
      if (rvalInt != rval) {
        report(FRACTIONAL_BITWISE_OPERAND, right);
        return n;
      }

      switch (n.getType()) {
        case Token.LSH:
          result = lvalInt << rvalInt;
          break;
        case Token.RSH:
          result = lvalInt >> rvalInt;
          break;
        case Token.URSH:
          // JavaScript handles zero shifts on signed numbers differently than
          // Java as an Java int can not represent the unsigned 32-bit number
          // where JavaScript can so use a long here.
          long lvalLong = lvalInt & 0xffffffffL;
          result = lvalLong >>> rvalInt;
          break;
        default:
          throw new AssertionError("Unknown shift operator: " +
              Token.name(n.getType()));
      }

      Node newNumber = IR.number(result);
      n.getParent().replaceChild(n, newNumber);
      reportCodeChange();

      return newNumber;
    }

    return n;
  }

  /**
   * Try to fold comparison nodes, e.g ==
   */
  @SuppressWarnings("fallthrough")
  private Node tryFoldComparison(Node n, Node left, Node right) {
    TernaryValue result = evaluateComparison(n.getType(), left, right);
    if (result == TernaryValue.UNKNOWN) {
      return n;
    }

    Node newNode = NodeUtil.booleanNode(result.toBoolean(true));
    n.getParent().replaceChild(n, newNode);
    reportCodeChange();

    return newNode;
  }

  static TernaryValue evaluateComparison(int op, Node left, Node right) {
    boolean leftLiteral = NodeUtil.isLiteralValue(left, true);
    boolean rightLiteral = NodeUtil.isLiteralValue(right, true);

    if (!leftLiteral || !rightLiteral) {
      // We only handle literal operands for LT and GT.
      if (op != Token.GT && op != Token.LT) {
        return TernaryValue.UNKNOWN;
      }
    }

    boolean undefinedRight = NodeUtil.isUndefined(right) && rightLiteral;
    boolean nullRight = right.isNull();
    int lhType = getNormalizedNodeType(left);
    int rhType = getNormalizedNodeType(right);
    switch (lhType) {
      case Token.VOID:
        if (!leftLiteral) {
          return TernaryValue.UNKNOWN;
        } else if (!rightLiteral) {
          return TernaryValue.UNKNOWN;
        } else {
          return TernaryValue.forBoolean(compareToUndefined(right, op));
        }

      case Token.NULL:
        if (rightLiteral && isEqualityOp(op)) {
          return TernaryValue.forBoolean(compareToNull(right, op));
        }
        // fallthrough
      case Token.TRUE:
      case Token.FALSE:
        if (undefinedRight) {
          return TernaryValue.forBoolean(compareToUndefined(left, op));
        }
        if (rhType != Token.TRUE &&
            rhType != Token.FALSE &&
            rhType != Token.NULL) {
          return TernaryValue.UNKNOWN;
        }
        switch (op) {
          case Token.SHEQ:
          case Token.EQ:
            return TernaryValue.forBoolean(lhType == rhType);

          case Token.SHNE:
          case Token.NE:
            return TernaryValue.forBoolean(lhType != rhType);

          case Token.GE:
          case Token.LE:
          case Token.GT:
          case Token.LT:
            return compareAsNumbers(op, left, right);
        }
        return TernaryValue.UNKNOWN;

      case Token.THIS:
        if (!right.isThis()) {
          return TernaryValue.UNKNOWN;
        }
        switch (op) {
          case Token.SHEQ:
          case Token.EQ:
            return TernaryValue.TRUE;

          case Token.SHNE:
          case Token.NE:
            return TernaryValue.FALSE;
        }

        // We can only handle == and != here.
        // GT, LT, GE, LE depend on the type of "this" and how it will
        // be converted to number.  The results are different depending on
        // whether it is a string, NaN or other number value.
        return TernaryValue.UNKNOWN;

      case Token.STRING:
        if (undefinedRight) {
          return TernaryValue.forBoolean(compareToUndefined(left, op));
        }
        if (nullRight && isEqualityOp(op)) {
          return TernaryValue.forBoolean(compareToNull(left, op));
        }
        if (Token.STRING != right.getType()) {
          return TernaryValue.UNKNOWN;  // Only eval if they are the same type
        }

        switch (op) {
          case Token.SHEQ:
          case Token.EQ:
            return areStringsEqual(left.getString(), right.getString());

          case Token.SHNE:
          case Token.NE:
            return areStringsEqual(left.getString(), right.getString()).not();
        }

        return TernaryValue.UNKNOWN;

      case Token.NUMBER:
        if (undefinedRight) {
          return TernaryValue.forBoolean(compareToUndefined(left, op));
        }
        if (nullRight && isEqualityOp(op)) {
          return TernaryValue.forBoolean(compareToNull(left, op));
        }
        if (Token.NUMBER != right.getType()) {
          return TernaryValue.UNKNOWN;  // Only eval if they are the same type
        }
        return compareAsNumbers(op, left, right);

      case Token.NAME:
        if (leftLiteral && undefinedRight) {
          return TernaryValue.forBoolean(compareToUndefined(left, op));
        }

        if (rightLiteral) {
          boolean undefinedLeft = (left.getString().equals("undefined"));
          if (undefinedLeft) {
            return TernaryValue.forBoolean(compareToUndefined(right, op));
          }
          if (leftLiteral && nullRight && isEqualityOp(op)) {
            return TernaryValue.forBoolean(compareToNull(left, op));
          }
        }

        if (Token.NAME != right.getType()) {
          return TernaryValue.UNKNOWN;  // Only eval if they are the same type
        }
        String ln = left.getString();
        String rn = right.getString();
        if (!ln.equals(rn)) {
          return TernaryValue.UNKNOWN;  // Not the same value name.
        }

        switch (op) {
          // If we knew the named value wouldn't be NaN, it would be nice
          // to handle EQ,NE,LE,GE,SHEQ, and SHNE.
          case Token.LT:
          case Token.GT:
            return TernaryValue.FALSE;
        }

        return TernaryValue.UNKNOWN;  // don't handle that op

      case Token.NEG:
        if (leftLiteral) {
          if (undefinedRight) {
            return TernaryValue.forBoolean(compareToUndefined(left, op));
          }
          if (nullRight && isEqualityOp(op)) {
            return TernaryValue.forBoolean(compareToNull(left, op));
          }
        }
        // Nothing else for now.
        return TernaryValue.UNKNOWN;

      case Token.ARRAYLIT:
      case Token.OBJECTLIT:
      case Token.REGEXP:
      case Token.FUNCTION:
        if (leftLiteral) {
          if (undefinedRight) {
            return TernaryValue.forBoolean(compareToUndefined(left, op));
          }
          if (nullRight && isEqualityOp(op)) {
            return TernaryValue.forBoolean(compareToNull(left, op));
          }
        }
        // ignore the rest for now.
        return TernaryValue.UNKNOWN;

      default:
        // assert, this should cover all consts
        return TernaryValue.UNKNOWN;
    }
  }

  /** Returns whether two JS strings are equal. */
  private static TernaryValue areStringsEqual(String a, String b) {
    // In JS, browsers parse \v differently. So do not consider strings
    // equal if one contains \v.
    if (a.indexOf('\u000B') != -1 ||
        b.indexOf('\u000B') != -1) {
      return TernaryValue.UNKNOWN;
    } else {
      return a.equals(b) ? TernaryValue.TRUE : TernaryValue.FALSE;
    }
  }

  /**
   * @return Translate NOT expressions into TRUE or FALSE when possible.
   */
  private static int getNormalizedNodeType(Node n) {
    int type = n.getType();
    if (type == Token.NOT) {
      TernaryValue value = NodeUtil.getPureBooleanValue(n);
      switch (value) {
        case TRUE:
          return Token.TRUE;
        case FALSE:
          return Token.FALSE;
        case UNKNOWN:
          return type;
      }
    }
    return type;
  }

  /**
   * The result of the comparison, or UNKNOWN if the
   * result could not be determined.
   */
  private static TernaryValue compareAsNumbers(int op, Node left, Node right) {
    Double leftValue = NodeUtil.getNumberValue(left);
    if (leftValue == null) {
      return TernaryValue.UNKNOWN;
    }
    Double rightValue = NodeUtil.getNumberValue(right);
    if (rightValue == null) {
      return TernaryValue.UNKNOWN;
    }

    double lv = leftValue;
    double rv = rightValue;

    switch (op) {
      case Token.SHEQ:
      case Token.EQ:
        Preconditions.checkState(
            left.isNumber() && right.isNumber());
        return TernaryValue.forBoolean(lv == rv);
      case Token.SHNE:
      case Token.NE:
        Preconditions.checkState(
            left.isNumber() && right.isNumber());
        return TernaryValue.forBoolean(lv != rv);
      case Token.LE:
        return TernaryValue.forBoolean(lv <= rv);
      case Token.LT:
        return TernaryValue.forBoolean(lv <  rv);
      case Token.GE:
        return TernaryValue.forBoolean(lv >= rv);
      case Token.GT:
        return TernaryValue.forBoolean(lv >  rv);
      default:
        return TernaryValue.UNKNOWN;  // don't handle that op
    }
  }

  /**
   * @param value The value to compare to "undefined"
   * @param op The boolean op to compare with
   * @return Whether the boolean op is true or false
   */
  private static boolean compareToUndefined(Node value, int op) {
    Preconditions.checkState(NodeUtil.isLiteralValue(value, true));
    boolean valueUndefined = NodeUtil.isUndefined(value);
    boolean valueNull = (Token.NULL == value.getType());
    boolean equivalent = valueUndefined || valueNull;
    switch (op) {
      case Token.EQ:
        // undefined is only equal to null or an undefined value
        return equivalent;
      case Token.NE:
        return !equivalent;
      case Token.SHEQ:
        return valueUndefined;
      case Token.SHNE:
        return !valueUndefined;
      case Token.LT:
      case Token.GT:
      case Token.LE:
      case Token.GE:
        return false;
      default:
        throw new IllegalStateException("unexpected.");
    }
  }

  private static boolean isEqualityOp(int op) {
    switch (op) {
      case Token.EQ:
      case Token.NE:
      case Token.SHEQ:
      case Token.SHNE:
        return true;
    }
    return false;
  }

  /**
   * @param value The value to compare to "null"
   * @param op The boolean op to compare with
   * @return Whether the boolean op is true or false
   */
  private static boolean compareToNull(Node value, int op) {
    boolean valueUndefined = NodeUtil.isUndefined(value);
    boolean valueNull = (Token.NULL == value.getType());
    boolean equivalent = valueUndefined || valueNull;
    switch (op) {
      case Token.EQ:
        // undefined is only equal to null or an undefined value
        return equivalent;
      case Token.NE:
        return !equivalent;
      case Token.SHEQ:
        return valueNull;
      case Token.SHNE:
        return !valueNull;
      default:
        throw new IllegalStateException("unexpected.");
    }
  }

  /**
   * Try to fold away unnecessary object instantiation.
   * e.g. this[new String('eval')] -> this.eval
   */
  private Node tryFoldCtorCall(Node n) {
    Preconditions.checkArgument(n.isNew());

    // we can remove this for GETELEM calls (anywhere else?)
    if (inForcedStringContext(n)) {
      return tryFoldInForcedStringContext(n);
    }
    return n;
  }

  /** Returns whether this node must be coerced to a string. */
  private boolean inForcedStringContext(Node n) {
    if (n.getParent().isGetElem() &&
        n.getParent().getLastChild() == n) {
      return true;
    }

    // we can fold in the case "" + new String("")
    if (n.getParent().isAdd()) {
      return true;
    }
    return false;
  }

  private Node tryFoldInForcedStringContext(Node n) {
    // For now, we only know how to fold ctors.
    Preconditions.checkArgument(n.isNew());

    Node objectType = n.getFirstChild();
    if (!objectType.isName()) {
      return n;
    }

    if (objectType.getString().equals("String")) {
      Node value = objectType.getNext();
      String stringValue = null;
      if (value == null) {
        stringValue = "";
      } else {
        if (!NodeUtil.isImmutableValue(value)) {
          return n;
        }

        stringValue = NodeUtil.getStringValue(value);
      }

      if (stringValue == null) {
        return n;
      }

      Node parent = n.getParent();
      Node newString = IR.string(stringValue);

      parent.replaceChild(n, newString);
      newString.copyInformationFrom(parent);
      reportCodeChange();

      return newString;
    }
    return n;
  }

  /**
   * Try to fold array-element. e.g [1, 2, 3][10];
   */
  private Node tryFoldGetElem(Node n, Node left, Node right) {
    Preconditions.checkArgument(n.isGetElem());

    if (left.isObjectLit()) {
      return tryFoldObjectPropAccess(n, left, right);
    }

    if (left.isArrayLit()) {
      return tryFoldArrayAccess(n, left, right);
    }
    return n;
  }

  /**
   * Try to fold array-length. e.g [1, 2, 3].length ==> 3, [x, y].length ==> 2
   */
  private Node tryFoldGetProp(Node n, Node left, Node right) {
    Preconditions.checkArgument(n.isGetProp());

    if (left.isObjectLit()) {
      return tryFoldObjectPropAccess(n, left, right);
    }

    if (right.isString() &&
        right.getString().equals("length")) {
      int knownLength = -1;
      switch (left.getType()) {
        case Token.ARRAYLIT:
          if (mayHaveSideEffects(left)) {
            // Nope, can't fold this, without handling the side-effects.
            return n;
          }
          knownLength = left.getChildCount();
          break;
        case Token.STRING:
          knownLength = left.getString().length();
          break;
        default:
          // Not a foldable case, forget it.
          return n;
      }

      Preconditions.checkState(knownLength != -1);
      Node lengthNode = IR.number(knownLength);
      n.getParent().replaceChild(n, lengthNode);
      reportCodeChange();

      return lengthNode;
    }

    return n;
  }

  private boolean isAssignmentTarget(Node n) {
    Node parent = n.getParent();
    if ((NodeUtil.isAssignmentOp(parent) && parent.getFirstChild() == n)
        || parent.isInc()
        || parent.isDec()) {
      // If GETPROP/GETELEM is used as assignment target the object literal is
      // acting as a temporary we can't fold it here:
      //    "{a:x}.a += 1" is not "x += 1"
      return true;
    }
    return false;
  }

  private Node tryFoldArrayAccess(Node n, Node left, Node right) {
    Node parent = n.getParent();
    // If GETPROP/GETELEM is used as assignment target the array literal is
    // acting as a temporary we can't fold it here:
    //    "[][0] += 1"
    if (isAssignmentTarget(n)) {
      return n;
    }

    if (!right.isNumber()) {
      // Sometimes people like to use complex expressions to index into
      // arrays, or strings to index into array methods.
      return n;
    }

    double index = right.getDouble();
    int intIndex = (int) index;
    if (intIndex != index) {
      report(INVALID_GETELEM_INDEX_ERROR, right);
      return n;
    }

    if (intIndex < 0) {
      report(INDEX_OUT_OF_BOUNDS_ERROR, right);
      return n;
    }

    Node current = left.getFirstChild();
    Node elem = null;
    for (int i = 0; current != null; i++) {
      if (i != intIndex) {
        if (mayHaveSideEffects(current)) {
          return n;
        }
      } else {
        elem = current;
      }

      current = current.getNext();
    }

    if (elem == null) {
      report(INDEX_OUT_OF_BOUNDS_ERROR, right);
      return n;
    }

    if (elem.isEmpty()) {
      elem = NodeUtil.newUndefinedNode(elem);
    } else {
      left.removeChild(elem);
    }

    // Replace the entire GETELEM with the value
    n.getParent().replaceChild(n, elem);
    reportCodeChange();
    return elem;
  }

  private Node tryFoldObjectPropAccess(Node n, Node left, Node right) {
    Preconditions.checkArgument(NodeUtil.isGet(n));

    if (!left.isObjectLit() || !right.isString()) {
      return n;
    }

    if (isAssignmentTarget(n)) {
      // If GETPROP/GETELEM is used as assignment target the object literal is
      // acting as a temporary we can't fold it here:
      //    "{a:x}.a += 1" is not "x += 1"
      return n;
    }

    // find the last definition in the object literal
    Node key = null;
    Node value = null;
    for (Node c = left.getFirstChild(); c != null; c = c.getNext()) {
      if (c.getString().equals(right.getString())) {
        switch (c.getType()) {
          case Token.SETTER_DEF:
            continue;
          case Token.GETTER_DEF:
          case Token.STRING_KEY:
            if (value != null && mayHaveSideEffects(value)) {
              // The previously found value had side-effects
              return n;
            }
            key = c;
            value = key.getFirstChild();
            break;
          default:
            throw new IllegalStateException();
        }
      } else if (mayHaveSideEffects(c.getFirstChild())) {
        // We don't handle the side-effects here as they might need a temporary
        // or need to be reordered.
        return n;
      }
    }

    // Didn't find a definition of the name in the object literal, it might
    // be coming from the Object prototype
    if (value == null) {
      return n;
    }

    if (value.isFunction() && NodeUtil.referencesThis(value)) {
      // 'this' may refer to the object we are trying to remove
      return n;
    }

    Node replacement = value.detachFromParent();
    if (key.isGetterDef()){
      replacement = IR.call(replacement);
      replacement.putBooleanProp(Node.FREE_CALL, true);
    }

    n.getParent().replaceChild(n, replacement);
    reportCodeChange();
    return n;
  }
}

>>>>>>> 76aa07461566a5976980e6696204781271955163
