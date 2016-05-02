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
    
//#if CLDC=="1.0"
    public static final Boolean TRUE = new Boolean(true);
    public static final Boolean FALSE = new Boolean(false);
//#endif    

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


//#ifdef PRODUCER
//#     /**
//#      * Construct a JSONObject from a subset of another JSONObject.
//#      * An array of strings is used to identify the keys that should be copied.
//#      * Missing keys are ignored.
//#      * @param jo A JSONObject.
//#      * @param sa An array of strings.
//#      * @exception JSONException If a value is a non-finite number.
//#      */
//#     public JSONObject(JSONObject jo, String[] sa) throws JSONException {
//#         this();
//#         for (int i = 0; i < sa.length; i += 1) {
//#             putOpt(sa[i], jo.opt(sa[i]));
//#         }
//#     }
//#endif

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


//#ifdef PRODUCER
//#     /**
//#      * Construct a JSONObject from a Map.
//#      * @param map A map object that can be used to initialize the contents of
//#      *  the JSONObject.
//#      */
//#     public JSONObject(Hashtable map) {
//#         if (map == null) {
//#             this.myHashMap = new Hashtable();
//#         } else {
//#             this.myHashMap = new Hashtable(map.size());
//#             Enumeration keys = map.keys();
//#             while (keys.hasMoreElements()) {
//#                 Object key = keys.nextElement();
//#                 this.myHashMap.put(key, map.get(key));
//#             }
//#         }
//#     }
//#endif    
    
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

//#ifdef PRODUCER
//#     /**
//#      * Append values to the array under a key. If the key does not exist in the
//#      * JSONObject, then the key is put in the JSONObject with its value being a
//#      * JSONArray containing the value parameter. If the key was already
//#      * associated with a JSONArray, then the value parameter is appended to it.
//#      * @param key   A key string.
//#      * @param value An object to be accumulated under the key.
//#      * @return this.
//#      * @throws JSONException If the key is null or if the current value 
//#      * 	associated with the key is not a JSONArray.
//#      */
//#     public JSONObject append(String key, Object value)
//#             throws JSONException {
//#         testValidity(value);
//#         Object o = opt(key);
//#         if (o == null) {
//#             put(key, new JSONArray().put(value));
//#         } else if (o instanceof JSONArray) {
//#             throw new JSONException("JSONObject[" + key + 
//#             		"] is not a JSONArray.");
//#         } else {
//#             put(key, new JSONArray().put(o).put(value));
//#         }
//#         return this;
//#     }
//#endif

//#if CLDC!="1.0"
//#     /**
//#      * Produce a string from a double. The string "null" will be returned if
//#      * the number is not finite.
//#      * @param  d A double.
//#      * @return A String.
//#      */
//#     static public String doubleToString(double d) {
//#         if (Double.isInfinite(d) || Double.isNaN(d)) {
//#         	return "null";
//#         }
//# 
//# // Shave off trailing zeros and decimal point, if possible.
//# 
//#         String s = Double.toString(d);
//#         if (s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0) {
//#             while (s.endsWith("0")) {
//#                 s = s.substring(0, s.length() - 1);
//#             }
//#             if (s.endsWith(".")) {
//#                 s = s.substring(0, s.length() - 1);
//#             }
//#         }
//#         return s;
//#     }
//#endif

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
//#if CLDC!="1.0"
//#         if (o.equals(Boolean.FALSE) ||
//#else
        if (o.equals(FALSE) ||
//#endif
                (o instanceof String &&
                ((String)o).toLowerCase().equals("false"))) {
            return false;
//#if CLDC!="1.0"
//#         } else if (o.equals(Boolean.TRUE) ||
//#else
        } else if (o.equals(TRUE) ||
//#endif
                (o instanceof String &&
                ((String)o).toLowerCase().equals("true"))) {
            return true;
        }
        throw new JSONException("JSONObject[" + quote(key) +
                "] is not a Boolean.");
    }

//#if CLDC!="1.0"
//#     /**
//#      * Get the double value associated with a key.
//#      * @param key   A key string.
//#      * @return      The numeric value.
//#      * @throws JSONException if the key is not found or
//#      *  if the value is not a Number object and cannot be converted to a number.
//#      */
//#     public double getDouble(String key) throws JSONException {
//#         Object o = get(key);
//#         if (o instanceof Byte) {
//#             return (double) ((Byte)o).byteValue();
//#         } else if (o instanceof Short) {
//#             return (double) ((Short)o).shortValue();
//#         } else if (o instanceof Integer) {
//#             return (double) ((Integer)o).intValue();
//#         } else if (o instanceof Long) {
//#             return (double) ((Long)o).longValue();
//#         } else if (o instanceof Float) {
//#             return (double) ((Float)o).floatValue();
//#         } else if (o instanceof Double) {
//#             return ((Double)o).doubleValue();
//#         } else if (o instanceof String) {
//#             try {
//#                 return Double.valueOf((String)o).doubleValue();
//#             } catch (Exception e) {
//#                 throw new JSONException("JSONObject[" + quote(key) +
//#                     "] is not a number.");
//#             }
//#         } 
//#         throw new JSONException("JSONObject[" + quote(key) +
//#             "] is not a number.");
//#     }
//#endif


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
//#if CLDC!="1.0"
//#         } else if (o instanceof Float) {
//#             return (int) ((Float)o).floatValue();
//#         } else if (o instanceof Double) {
//#             return (int) ((Double)o).doubleValue();
//#         } else if (o instanceof String) {
//#             return (int) getDouble(key);
//#endif
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
//#if CLDC!="1.0"
//#         } else if (o instanceof Float) {
//#             return (long) ((Float)o).floatValue();
//#         } else if (o instanceof Double) {
//#             return (long) ((Double)o).doubleValue();
//#         } else if (o instanceof String) {
//#             return (long) getDouble(key);
//#endif
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

    
//#if CLDC!="1.0"
//#     /**
//#      * Get an optional double associated with a key,
//#      * or NaN if there is no such key or if its value is not a number.
//#      * If the value is a string, an attempt will be made to evaluate it as
//#      * a number.
//#      *
//#      * @param key   A string which is the key.
//#      * @return      An object which is the value.
//#      */
//#     public double optDouble(String key) {
//#         return optDouble(key, Double.NaN);
//#     }
//#endif

//#if CLDC!="1.0"
//#     /**
//#      * Get an optional double associated with a key, or the
//#      * defaultValue if there is no such key or if its value is not a number.
//#      * If the value is a string, an attempt will be made to evaluate it as
//#      * a number.
//#      *
//#      * @param key   A key string.
//#      * @param defaultValue     The default.
//#      * @return      An object which is the value.
//#      */
//#     public double optDouble(String key, double defaultValue) {
//#         try {
//#             Object o = opt(key);
//#             return Double.parseDouble((String)o);
//#         } catch (Exception e) {
//#             return defaultValue;
//#         }
//#     }
//#endif

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
//#if CLDC!="1.0"
//#         put(key, value ? Boolean.TRUE : Boolean.FALSE);
//#else
        put(key, value ? TRUE : FALSE);
//#endif
        return this;
    }


//#if CLDC!="1.0"
//#     /**
//#      * Put a key/double pair in the JSONObject.
//#      *
//#      * @param key   A key string.
//#      * @param value A double which is the value.
//#      * @return this.
//#      * @throws JSONException If the key is null or if the number is invalid.
//#      */
//#     public JSONObject put(String key, double value) throws JSONException {
//#         put(key, new Double(value));
//#         return this;
//#     }
//#endif

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

     
//#ifdef PRODUCER
//#     /**
//#      * Put a key/value pair in the JSONObject, where the value will be a
//#      * JSONObject which is produced from a Map.
//#      * @param key 	A key string.
//#      * @param value	A Map value.
//#      * @return		this.
//#      * @throws JSONException
//#      */
//#     public JSONObject put(String key, Hashtable value) throws JSONException {
//#         put(key, new JSONObject(value));
//#         return this;
//#     }
//#endif    
    
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
//#if CLDC!="1.0"
//#             if (o instanceof Double) {
//#                 if (((Double)o).isInfinite() || ((Double)o).isNaN()) {
//#                     throw new JSONException(
//#                         "JSON does not allow non-finite numbers");
//#                 }
//#             } else if (o instanceof Float) {
//#                 if (((Float)o).isInfinite() || ((Float)o).isNaN()) {
//#                     throw new JSONException(
//#                         "JSON does not allow non-finite numbers.");
//#                 }
//#             }
//#endif
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
//#if CLDC!="1.0"
//#         if (value instanceof Float || value instanceof Double ||
//#else
        if (
//#endif
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
//#if CLDC!="1.0"
//#         if (value instanceof Float || value instanceof Double ||
//#else
        if (
//#endif
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
 * Copyright 2010-2012 Luca Garulli (l.garulli--at--orientechnologies.com)
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
package com.orientechnologies.orient.core.metadata.schema;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.common.types.OBinary;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.serialization.OSerializableStream;
import com.orientechnologies.orient.core.serialization.serializer.OStringSerializerHelper;

/**
 * Generic representation of a type.<br/>
 * allowAssignmentFrom accepts any class, but Array.class means that the type accepts generic Arrays.
 * 
 * @author Luca Garulli
 * 
 */
public enum OType {
  BOOLEAN("Boolean", 0, new Class<?>[] { Boolean.class, Boolean.TYPE }, new Class<?>[] { Boolean.class, Number.class }) {
  },
  INTEGER("Integer", 1, new Class<?>[] { Integer.class, Integer.TYPE }, new Class<?>[] { Integer.class, Number.class }) {
  },
  SHORT("Short", 2, new Class<?>[] { Short.class, Short.TYPE }, new Class<?>[] { Short.class, Number.class }) {
  },
  LONG("Long", 3, new Class<?>[] { Long.class, Long.TYPE }, new Class<?>[] { Long.class, Number.class, }) {
  },
  FLOAT("Float", 4, new Class<?>[] { Float.class, Float.TYPE }, new Class<?>[] { Float.class, Number.class }) {
  },
  DOUBLE("Double", 5, new Class<?>[] { Double.class, Double.TYPE }, new Class<?>[] { Double.class, Number.class }) {
  },
  DATETIME("Datetime", 6, new Class<?>[] { Date.class }, new Class<?>[] { Date.class, Number.class }) {
  },
  STRING("String", 7, new Class<?>[] { String.class }, new Class<?>[] { String.class }) {
  },
  BINARY("Binary", 8, new Class<?>[] { byte[].class }, new Class<?>[] { byte[].class }) {
  },
  EMBEDDED("Embedded", 9, new Class<?>[] { Object.class }, new Class<?>[] { OSerializableStream.class }) {
  },
  EMBEDDEDLIST("EmbeddedList", 10, new Class<?>[] { List.class }, new Class<?>[] { List.class }) {
  },
  EMBEDDEDSET("EmbeddedSet", 11, new Class<?>[] { Set.class }, new Class<?>[] { Set.class }) {
  },
  EMBEDDEDMAP("EmbeddedMap", 12, new Class<?>[] { Map.class }, new Class<?>[] { Map.class }) {
  },
  LINK("Link", 13, new Class<?>[] { Object.class, ORecordId.class }, new Class<?>[] { ORecord.class, ORID.class }) {
  },
  LINKLIST("LinkList", 14, new Class<?>[] { List.class }, new Class<?>[] { List.class }) {
  },
  LINKSET("LinkSet", 15, new Class<?>[] { Set.class }, new Class<?>[] { Set.class }) {
  },
  LINKMAP("LinkMap", 16, new Class<?>[] { Map.class }, new Class<?>[] { Map.class }) {
  },
  BYTE("Byte", 17, new Class<?>[] { Byte.class, Byte.TYPE }, new Class<?>[] { Byte.class, Number.class }) {
  },
  TRANSIENT("Transient", 18, new Class<?>[] {}, new Class<?>[] {}) {
  },
  DATE("Date", 19, new Class<?>[] { Date.class }, new Class<?>[] { Date.class, Number.class }) {
  },
  CUSTOM("Custom", 20, new Class<?>[] { OSerializableStream.class }, new Class<?>[] { OSerializableStream.class }) {
  },
  DECIMAL("Decimal", 21, new Class<?>[] { BigDecimal.class }, new Class<?>[] { BigDecimal.class, Number.class }) {
  };

  protected static final OType[] TYPES = new OType[] { STRING, BOOLEAN, BYTE, INTEGER, SHORT, LONG, FLOAT, DOUBLE, DATE, DATETIME,
      BINARY, EMBEDDEDLIST, EMBEDDEDSET, EMBEDDEDMAP, LINK, LINKLIST, LINKSET, LINKMAP, EMBEDDED, CUSTOM, TRANSIENT, DECIMAL };

  protected String               name;
  protected int                  id;
  protected Class<?>[]           javaTypes;
  protected Class<?>[]           allowAssignmentFrom;

  private OType(final String iName, final int iId, final Class<?>[] iJavaTypes, final Class<?>[] iAllowAssignmentBy) {
    name = iName;
    id = iId;
    javaTypes = iJavaTypes;
    allowAssignmentFrom = iAllowAssignmentBy;
  }

  /**
   * Return the type by ID.
   * 
   * @param iId
   *          The id to search
   * @return The type if any, otherwise null
   */
  public static OType getById(final byte iId) {
    for (OType t : TYPES) {
      if (iId == t.id)
        return t;
    }
    return null;
  }

  /**
   * Return the correspondent type by checking the "assignability" of the class received as parameter.
   * 
   * @param iClass
   *          Class to check
   * @return OType instance if found, otherwise null
   */
  public static OType getTypeByClass(final Class<?> iClass) {
    if (iClass == null)
      return null;

    for (final OType type : TYPES)
      for (int i = 0; i < type.javaTypes.length; ++i) {
        if (type.javaTypes[i] == iClass)
          return type;
        if (type.javaTypes[i] == Array.class && iClass.isArray())
          return type;
      }

    int priority = 0;
    boolean comparedAtLeastOnce;
    do {
      comparedAtLeastOnce = false;
      for (final OType type : TYPES) {
        if (type.allowAssignmentFrom.length > priority) {
          if (type.allowAssignmentFrom[priority].isAssignableFrom(iClass))
            return type;
          if (type.allowAssignmentFrom[priority].isArray() && iClass.isArray())
            return type;
          comparedAtLeastOnce = true;
        }
      }

      priority++;
    } while (comparedAtLeastOnce);

    return null;
  }

  /**
   * Convert the input object to an integer.
   * 
   * @param iValue
   *          Any type supported
   * @return The integer value if the conversion succeed, otherwise the IllegalArgumentException exception
   */
  public int asInt(final Object iValue) {
    if (iValue instanceof Number)
      return ((Number) iValue).intValue();
    else if (iValue instanceof String)
      return Integer.valueOf((String) iValue);
    else if (iValue instanceof Boolean)
      return ((Boolean) iValue) ? 1 : 0;

    throw new IllegalArgumentException("Cannot convert value " + iValue + " to int for type: " + name);
  }

  /**
   * Convert the input object to a long.
   * 
   * @param iValue
   *          Any type supported
   * @return The long value if the conversion succeed, otherwise the IllegalArgumentException exception
   */
  public long asLong(final Object iValue) {
    if (iValue instanceof Number)
      return ((Number) iValue).longValue();
    else if (iValue instanceof String)
      return Long.valueOf((String) iValue);
    else if (iValue instanceof Boolean)
      return ((Boolean) iValue) ? 1 : 0;

    throw new IllegalArgumentException("Cannot convert value " + iValue + " to long for type: " + name);
  }

  /**
   * Convert the input object to a float.
   * 
   * @param iValue
   *          Any type supported
   * @return The float value if the conversion succeed, otherwise the IllegalArgumentException exception
   */
  public float asFloat(final Object iValue) {
    if (iValue instanceof Number)
      return ((Number) iValue).floatValue();
    else if (iValue instanceof String)
      return Float.valueOf((String) iValue);

    throw new IllegalArgumentException("Cannot convert value " + iValue + " to float for type: " + name);
  }

  /**
   * Convert the input object to a double.
   * 
   * @param iValue
   *          Any type supported
   * @return The double value if the conversion succeed, otherwise the IllegalArgumentException exception
   */
  public double asDouble(final Object iValue) {
    if (iValue instanceof Number)
      return ((Number) iValue).doubleValue();
    else if (iValue instanceof String)
      return Double.valueOf((String) iValue);

    throw new IllegalArgumentException("Cannot convert value " + iValue + " to double for type: " + name);
  }

  /**
   * Convert the input object to a string.
   * 
   * @param iValue
   *          Any type supported
   * @return The string if the conversion succeed, otherwise the IllegalArgumentException exception
   */
  public String asString(final Object iValue) {
    return iValue.toString();
  }

  public static boolean isSimpleType(final Object iObject) {
    if (iObject == null)
      return false;

    final Class<? extends Object> iType = iObject.getClass();

    if (iType.isPrimitive()
        || Number.class.isAssignableFrom(iType)
        || String.class.isAssignableFrom(iType)
        || Boolean.class.isAssignableFrom(iType)
        || Date.class.isAssignableFrom(iType)
        || (iType.isArray() && (iType.equals(byte[].class) || iType.equals(char[].class) || iType.equals(int[].class)
            || iType.equals(long[].class) || iType.equals(double[].class) || iType.equals(float[].class)
            || iType.equals(short[].class) || iType.equals(Integer[].class) || iType.equals(String[].class)
            || iType.equals(Long[].class) || iType.equals(Short[].class) || iType.equals(Double[].class))))
      return true;

    return false;
  }

  /**
   * Convert types between numbers based on the iTargetClass parameter.
   * 
   * @param iValue
   *          Value to convert
   * @param iTargetClass
   *          Expected class
   * @return The converted value or the original if no conversion was applied
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static Object convert(final Object iValue, final Class<?> iTargetClass) {
    if (iValue == null)
      return null;

    if (iValue.getClass().equals(iTargetClass))
      // SAME TYPE: DON'T CONVERT IT
      return iValue;

    if (iTargetClass.isAssignableFrom(iValue.getClass()))
      // COMPATIBLE TYPES: DON'T CONVERT IT
      return iValue;

    try {
      if (iValue instanceof OBinary && iTargetClass.isAssignableFrom(byte[].class))
        return ((OBinary) iValue).toByteArray();
      else if (byte[].class.isAssignableFrom(iTargetClass)) {
        return OStringSerializerHelper.getBinaryContent(iValue);
      } else if (byte[].class.isAssignableFrom(iValue.getClass())) {
        return iValue;
      } else if (iTargetClass.isEnum()) {
        if (iValue instanceof Number)
          return ((Class<Enum>) iTargetClass).getEnumConstants()[((Number) iValue).intValue()];
        return Enum.valueOf((Class<Enum>) iTargetClass, iValue.toString());
      } else if (iTargetClass.equals(Byte.TYPE) || iTargetClass.equals(Byte.class)) {
        if (iValue instanceof Byte)
          return iValue;
        else if (iValue instanceof String)
          return Byte.parseByte((String) iValue);
        else
          return ((Number) iValue).byteValue();

      } else if (iTargetClass.equals(Short.TYPE) || iTargetClass.equals(Short.class)) {
        if (iValue instanceof Short)
          return iValue;
        else if (iValue instanceof String)
          return Short.parseShort((String) iValue);
        else
          return ((Number) iValue).shortValue();

      } else if (iTargetClass.equals(Integer.TYPE) || iTargetClass.equals(Integer.class)) {
        if (iValue instanceof Integer)
          return iValue;
        else if (iValue instanceof String)
          return Integer.parseInt((String) iValue);
        else
          return ((Number) iValue).intValue();

      } else if (iTargetClass.equals(Long.TYPE) || iTargetClass.equals(Long.class)) {
        if (iValue instanceof Long)
          return iValue;
        else if (iValue instanceof String)
          return Long.parseLong((String) iValue);
        else
          return ((Number) iValue).longValue();

      } else if (iTargetClass.equals(Float.TYPE) || iTargetClass.equals(Float.class)) {
        if (iValue instanceof Float)
          return iValue;
        else if (iValue instanceof String)
          return Float.parseFloat((String) iValue);
        else
          return ((Number) iValue).floatValue();

      } else if (iTargetClass.equals(BigDecimal.class)) {
        if (iValue instanceof BigDecimal)
          return iValue;
        else if (iValue instanceof String)
          return new BigDecimal((String) iValue);
        else if (iValue instanceof Number)
          return new BigDecimal(iValue.toString());

      } else if (iTargetClass.equals(Double.TYPE) || iTargetClass.equals(Double.class)) {
        if (iValue instanceof Double)
          return iValue;
        else if (iValue instanceof String)
          return Double.parseDouble((String) iValue);
        else if (iValue instanceof Float)
          // THIS IS NECESSARY DUE TO A BUG/STRANGE BEHAVIOR OF JAVA BY LOSSING PRECISION
          return Double.parseDouble((String) iValue.toString());
        else
          return ((Number) iValue).doubleValue();

      } else if (iTargetClass.equals(Boolean.TYPE) || iTargetClass.equals(Boolean.class)) {
        if (iValue instanceof Boolean)
          return ((Boolean) iValue).booleanValue();
        else if (iValue instanceof String) {
          if (((String) iValue).equalsIgnoreCase("true"))
            return Boolean.TRUE;
          else if (((String) iValue).equalsIgnoreCase("false"))
            return Boolean.FALSE;
          throw new IllegalArgumentException("Value is not boolean. Expected true or false but received '" + iValue + "'");
        } else if (iValue instanceof Number)
          return ((Number) iValue).intValue() != 0;

      } else if (iValue instanceof Collection<?> && Set.class.isAssignableFrom(iTargetClass)) {
        final Set<Object> set = new HashSet<Object>();
        set.addAll((Collection<? extends Object>) iValue);
        return set;

      } else if (iTargetClass.equals(Date.class)) {
        if (iValue instanceof Number)
          return new Date(((Number) iValue).longValue());
        if (iValue instanceof String) {
          try {
            return ODatabaseRecordThreadLocal.INSTANCE.get().getStorage().getConfiguration().getDateTimeFormatInstance()
                .parse((String) iValue);
          } catch (ParseException e) {
            return ODatabaseRecordThreadLocal.INSTANCE.get().getStorage().getConfiguration().getDateFormatInstance()
                .parse((String) iValue);
          }
        }
      } else if (iTargetClass.equals(String.class))
        return iValue.toString();
    } catch (IllegalArgumentException e) {
      // PASS THROUGH
      throw e;
    } catch (Exception e) {
      OLogManager.instance().debug(OType.class, "Error in conversion of value '%s' to type '%s'", iValue, iTargetClass);
      return null;
    }

    return iValue;
  }

  public Class<?> getDefaultJavaType() {
    return javaTypes.length > 0 ? javaTypes[0] : null;
  }

  public Class<?>[] getJavaTypes() {
    return javaTypes;
  }

  public static Number increment(final Number a, final Number b) {
    if (a == null || b == null)
      throw new IllegalArgumentException("Cannot increment a null value");

    if (a instanceof Integer) {
      if (b instanceof Integer) {
        final int sum = a.intValue() + b.intValue();
        if (sum < 0 && a.intValue() > 0 && b.intValue() > 0)
          // SPECIAL CASE: UPGRADE TO LONG
          return new Long(a.intValue() + b.intValue());
        return sum;
      } else if (b instanceof Long)
        return new Long(a.intValue() + b.longValue());
      else if (b instanceof Short) {
        final int sum = a.intValue() + b.shortValue();
        if (sum < 0 && a.intValue() > 0 && b.shortValue() > 0)
          // SPECIAL CASE: UPGRADE TO LONG
          return new Long(a.intValue() + b.shortValue());
        return sum;
      } else if (b instanceof Float)
        return new Float(a.intValue() + b.floatValue());
      else if (b instanceof Double)
        return new Double(a.intValue() + b.doubleValue());
      else if (b instanceof BigDecimal)
        return new BigDecimal(a.intValue()).add((BigDecimal) b);

    } else if (a instanceof Long) {
      if (b instanceof Integer)
        return new Long(a.longValue() + b.intValue());
      else if (b instanceof Long)
        return new Long(a.longValue() + b.longValue());
      else if (b instanceof Short)
        return new Long(a.longValue() + b.shortValue());
      else if (b instanceof Float)
        return new Float(a.longValue() + b.floatValue());
      else if (b instanceof Double)
        return new Double(a.longValue() + b.doubleValue());
      else if (b instanceof BigDecimal)
        return new BigDecimal(a.longValue()).add((BigDecimal) b);

    } else if (a instanceof Short) {
      if (b instanceof Integer) {
        final int sum = a.shortValue() + b.intValue();
        if (sum < 0 && a.shortValue() > 0 && b.intValue() > 0)
          // SPECIAL CASE: UPGRADE TO LONG
          return new Long(a.shortValue() + b.intValue());
        return sum;
      } else if (b instanceof Long)
        return new Long(a.shortValue() + b.longValue());
      else if (b instanceof Short) {
        final int sum = a.shortValue() + b.shortValue();
        if (sum < 0 && a.shortValue() > 0 && b.shortValue() > 0)
          // SPECIAL CASE: UPGRADE TO INTEGER
          return new Integer(a.intValue() + b.intValue());
        return sum;
      } else if (b instanceof Float)
        return new Float(a.shortValue() + b.floatValue());
      else if (b instanceof Double)
        return new Double(a.shortValue() + b.doubleValue());
      else if (b instanceof BigDecimal)
        return new BigDecimal(a.shortValue()).add((BigDecimal) b);

    } else if (a instanceof Float) {
      if (b instanceof Integer)
        return new Float(a.floatValue() + b.intValue());
      else if (b instanceof Long)
        return new Float(a.floatValue() + b.longValue());
      else if (b instanceof Short)
        return new Float(a.floatValue() + b.shortValue());
      else if (b instanceof Float)
        return new Float(a.floatValue() + b.floatValue());
      else if (b instanceof Double)
        return new Double(a.floatValue() + b.doubleValue());
      else if (b instanceof BigDecimal)
        return new BigDecimal(a.floatValue()).add((BigDecimal) b);

    } else if (a instanceof Double) {
      if (b instanceof Integer)
        return new Double(a.doubleValue() + b.intValue());
      else if (b instanceof Long)
        return new Double(a.doubleValue() + b.longValue());
      else if (b instanceof Short)
        return new Double(a.doubleValue() + b.shortValue());
      else if (b instanceof Float)
        return new Double(a.doubleValue() + b.floatValue());
      else if (b instanceof Double)
        return new Double(a.doubleValue() + b.doubleValue());
      else if (b instanceof BigDecimal)
        return new BigDecimal(a.doubleValue()).add((BigDecimal) b);

    } else if (a instanceof BigDecimal) {
      if (b instanceof Integer)
        return ((BigDecimal) a).add(new BigDecimal(b.intValue()));
      else if (b instanceof Long)
        return ((BigDecimal) a).add(new BigDecimal(b.longValue()));
      else if (b instanceof Short)
        return ((BigDecimal) a).add(new BigDecimal(b.shortValue()));
      else if (b instanceof Float)
        return ((BigDecimal) a).add(new BigDecimal(b.floatValue()));
      else if (b instanceof Double)
        return ((BigDecimal) a).add(new BigDecimal(b.doubleValue()));
      else if (b instanceof BigDecimal)
        return ((BigDecimal) a).add((BigDecimal) b);

    }

    throw new IllegalArgumentException("Cannot increment value '" + a + "' (" + a.getClass() + ") with '" + b + "' ("
        + b.getClass() + ")");
  }

  public boolean isMultiValue() {
    return this == EMBEDDEDLIST || this == EMBEDDEDMAP || this == EMBEDDEDSET || this == LINKLIST || this == LINKMAP
        || this == LINKSET;
  }

  public boolean isLink() {
    return this == LINK || this == LINKSET || this == LINKLIST || this == LINKMAP;
  }
}

>>>>>>> 76aa07461566a5976980e6696204781271955163
