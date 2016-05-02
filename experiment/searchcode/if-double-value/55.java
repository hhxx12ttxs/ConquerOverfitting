<<<<<<< HEAD
package net.sf.saxon.value;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.SequenceReceiver;
import net.sf.saxon.expr.*;
import net.sf.saxon.functions.Aggregate;
import net.sf.saxon.om.*;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.*;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
* A value is the result of an expression but it is also an expression in its own right.
* Note that every value can be regarded as a sequence - in many cases, a sequence of
* length one.
*/

public abstract class Value
        implements Serializable, SequenceIterable, ValueRepresentation /*, Comparable */ {

    /**
     * Static method to make a Value from a given Item (which may be either an AtomicValue
     * or a NodeInfo
     * @param val       The supplied value, or null, indicating the empty sequence.
     * @return          The supplied value, if it is a value, or a SingletonNode that
     *                  wraps the item, if it is a node. If the supplied value was null,
     *                  return an EmptySequence
     */

    public static Value asValue(ValueRepresentation val) {
        if (val instanceof Value) {
            return (Value)val;
        } else if (val == null) {
            return EmptySequence.getInstance();
        } else {
            return new SingletonNode((NodeInfo)val);
        }
    }

    /**
     * Static method to make an Item from a Value
     * @param value the value to be converted
     * @return null if the value is an empty sequence; or the only item in the value
     * if it is a singleton sequence
     * @throws XPathException if the Value contains multiple items
     */

    public static Item asItem(ValueRepresentation value) throws XPathException {
        if (value instanceof Item) {
            return (Item)value;
        } else {
            return ((Value)value).asItem();
        }
    }

    /**
     * Return the value in the form of an Item
     * @return the value in the form of an Item
     */

    public Item asItem() throws XPathException {
        SequenceIterator iter = iterate();
        Item item = iter.next();
        if (item == null) {
            return null;
        } else if (iter.next() != null) {
            throw new XPathException("Attempting to access a sequence as a singleton item");
        } else {
            return item;
        }
    }

    /**
     * Static method to get a Value from an Item
     * @param item the supplied item
     * @return the item expressed as a Value
     */

    public static Value fromItem(Item item) {
        if (item == null) {
            return EmptySequence.getInstance();
        } else if (item instanceof AtomicValue) {
            return (AtomicValue)item;
        } else {
            return new SingletonNode((NodeInfo)item);
=======
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
import java.util.Hashtable;
import java.util.Vector;

/**
 * A JSONArray is an ordered sequence of values. Its external text form is a
 * string wrapped in square brackets with commas separating the values. The
 * internal form is an object having <code>get</code> and <code>opt</code>
 * methods for accessing the values by index, and <code>put</code> methods for
 * adding or replacing values. The values can be any of these types:
 * <code>Boolean</code>, <code>JSONArray</code>, <code>JSONObject</code>,
 * <code>Number</code>, <code>String</code>, or the
 * <code>JSONObject.NULL object</code>.
 * <p>
 * The constructor can convert a JSON text into a Java object. The
 * <code>toString</code> method converts to JSON text.
 * <p>
 * A <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coersion for you.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * JSON syntax rules. The constructors are more forgiving in the texts they will
 * accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 *     before the closing bracket.</li>
 * <li>The <code>null</code> value will be inserted when there
 *     is <code>,</code>&nbsp;<small>(comma)</small> elision.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 *     quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 *     or single quote, and if they do not contain leading or trailing spaces,
 *     and if they do not contain any of these characters:
 *     <code>{ } [ ] / \ : , = ; #</code> and if they do not look like numbers
 *     and if they are not the reserved words <code>true</code>,
 *     <code>false</code>, or <code>null</code>.</li>
 * <li>Values can be separated by <code>;</code> <small>(semicolon)</small> as
 *     well as by <code>,</code> <small>(comma)</small>.</li>
 * <li>Numbers may have the <code>0-</code> <small>(octal)</small> or
 *     <code>0x-</code> <small>(hex)</small> prefix.</li>
 * <li>Comments written in the slashshlash, slashstar, and hash conventions
 *     will be ignored.</li>
 * </ul>

 * @author JSON.org
 * @version 2
 */
public class JSONArray {


    /**
     * The Vector where the JSONArray's properties are kept.
     */
    private Vector myArrayList;


    /**
     * Construct an empty JSONArray.
     */
    public JSONArray() {
        this.myArrayList = new Vector();
    }

    /**
     * Construct a JSONArray from a JSONTokener.
     * @param x A JSONTokener
     * @throws JSONException If there is a syntax error.
     */
    public JSONArray(JSONTokener x) throws JSONException {
        this();
        if (x.nextClean() != '[') {
            throw x.syntaxError("A JSONArray text must start with '['");
        }
        if (x.nextClean() == ']') {
            return;
        }
        x.back();
        for (;;) {
            if (x.nextClean() == ',') {
                x.back();
                this.myArrayList.addElement(null);
            } else {
                x.back();
                this.myArrayList.addElement(x.nextValue());
            }
            switch (x.nextClean()) {
            case ';':
            case ',':
                if (x.nextClean() == ']') {
                    return;
                }
                x.back();
                break;
            case ']':
                return;
            default:
                throw x.syntaxError("Expected a ',' or ']'");
            }
        }
    }


    /**
     * Construct a JSONArray from a source sJSON text.
     * @param string     A string that begins with
     * <code>[</code>&nbsp;<small>(left bracket)</small>
     *  and ends with <code>]</code>&nbsp;<small>(right bracket)</small>.
     *  @throws JSONException If there is a syntax error.
     */
    public JSONArray(String string) throws JSONException {
        this(new JSONTokener(string));
    }


    /**
     * Construct a JSONArray from a Collection.
     * @param collection     A Collection.
     */
    public JSONArray(Vector collection) {
        if (collection == null) {
            this.myArrayList = new Vector();
        } else {
            int size = collection.size();
            this.myArrayList = new Vector(size);
            for (int i=0; i < size; i++) {
                this.myArrayList.addElement(collection.elementAt(i));
            }
>>>>>>> 76aa07461566a5976980e6696204781271955163
        }
    }

    /**
<<<<<<< HEAD
     * Static method to get an Iterator over any ValueRepresentation (which may be either a Value
     * or a NodeInfo
     * @param val       The supplied value, or null, indicating the empty sequence.
     * @return          The supplied value, if it is a value, or a SingletonNode that
     *                  wraps the item, if it is a node. If the supplied value was null,
     *                  return an EmptySequence
     */

    public static SequenceIterator asIterator(ValueRepresentation val) throws XPathException {
        if (val instanceof Value) {
            return ((Value)val).iterate();
        } else if (val == null) {
            return EmptyIterator.getInstance();
        } else {
            return SingletonIterator.makeIterator((NodeInfo)val);
        }
    }

    /**
     * Static method to convert strings to doubles.
     * @param s the String to be converted
     * @return a double representing the value of the String
     * @throws NumberFormatException if the value cannot be converted
    */

    public static double stringToNumber(CharSequence s) throws NumberFormatException {
        // first try to parse simple numbers by hand (it's cheaper)
        int len = s.length();
        if (len < 9) {
            boolean useJava = false;
            long num = 0;
            int dot = -1;
            int lastDigit = -1;
            boolean onlySpaceAllowed = false;
            loop: for (int i=0; i<s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                    case ' ':
                    case '\n':
                    case '\t':
                    case '\r':
                        if (lastDigit != -1) {
                            onlySpaceAllowed = true;
                        }
                        break;
                    case '0': case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        if (onlySpaceAllowed) {
                            throw new NumberFormatException("Numeric value contains embedded whitespace");
                        }
                        lastDigit = i;
                        num = num*10 + (c - '0');
                        break;
                    case '.':
                        if (onlySpaceAllowed) {
                            throw new NumberFormatException("Numeric value contains embedded whitespace");
                        }
                        if (dot != -1) {
                            throw new NumberFormatException("Only one decimal point allowed");
                        }
                        dot = i;
                        break;
                    default:
                        // there's something like a sign or an exponent: take the slow train instead
                        useJava = true;
                        break loop;
                }
            }
            if (!useJava) {
                if (lastDigit == -1) {
                    throw new NumberFormatException("No digits found");
                } else if (dot == -1 || dot > lastDigit) {
                    return (double)num;
                } else {
                    int afterPoint = lastDigit - dot;
                    return ((double)num)/powers[afterPoint];
                }
            }
        }
        String n = Whitespace.trimWhitespace(s).toString();
        if ("INF".equals(n)) {
            return Double.POSITIVE_INFINITY;
        } else if ("-INF".equals(n)) {
            return Double.NEGATIVE_INFINITY;
        } else if ("NaN".equals(n)) {
            return Double.NaN;
        } else if (!doublePattern.matcher(n).matches()) {
            // Need to disallow values that are OK in Java but not in XPath, specifically
            // - special values like +NaN or -Infinity
            // - hex digits
            // - binary exponents
            // TODO: this checking incurs a performance hit. Perhaps we should do the whole conversion in-house
            throw new NumberFormatException("Invalid characters in float/double value");
        } else {
            return Double.parseDouble(n);
        }
    }

    private static double[] powers = new double[]{1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};
    private static Pattern doublePattern = Pattern.compile("^[0-9.eE+-]+$");

    /**
     * Get a SequenceIterator over a ValueRepresentation
     * @param val the value to iterate over
     * @return the iterator
     */

    public static SequenceIterator getIterator(ValueRepresentation val) throws XPathException {
        if (val instanceof Value) {
            return ((Value)val).iterate();
        } else if (val instanceof NodeInfo) {
            return SingletonIterator.makeIterator((NodeInfo)val);
        } else if (val == null) {
            throw new AssertionError("Value of variable is undefined (null)");
        } else {
            throw new AssertionError("Unknown value representation " + val.getClass());
        }
    }

    /**
     * Iterate over the items contained in this value.
     * @return an iterator over the sequence of items
     * @throws XPathException if a dynamic error occurs. This is possible only in the case of values
     * that are materialized lazily, that is, where the iterate() method leads to computation of an
     * expression that delivers the values.
     */

    public abstract SequenceIterator iterate() throws XPathException;

    /**
     * Return an iterator over the results of evaluating an expression
     * @param context the dynamic evaluation context (not used in this implementation)
     * @return an iterator over the items delivered by the expression
     */

    public final SequenceIterator iterate(XPathContext context) throws XPathException {
        // Note, this method, and the SequenceIterable interface, are used from XQuery compiled code
        return iterate();
    }

    /**
     * Get the value of the item as a CharSequence. This is in some cases more efficient than
     * the version of the method that returns a String.
     */

    public CharSequence getStringValueCS() throws XPathException {
        return getStringValue();
    }

    /**
     * Get the canonical lexical representation as defined in XML Schema. This is not always the same
     * as the result of casting to a string according to the XPath rules.
     * @return the canonical lexical representation if defined in XML Schema; otherwise, the result
     * of casting to string according to the XPath 2.0 rules
     */

    public CharSequence getCanonicalLexicalRepresentation() {
        try {
            return getStringValueCS();
        } catch (XPathException err) {
            throw new IllegalStateException("Failed to get canonical lexical representation: " + err.getMessage());
        }
    }

    /**
     * Determine the data type of the items in the expression, if possible
     * @return for the default implementation: AnyItemType (not known)
     * @param th The TypeHierarchy. Can be null if the target is an AtomicValue.
     */

    public ItemType getItemType(TypeHierarchy th) {
        return AnyItemType.getInstance();
    }

    /**
     * Determine the cardinality
     * @return the cardinality
     */

    public int getCardinality() {
        try {
            SequenceIterator iter = iterate();
            Item next = iter.next();
            if (next == null) {
                return StaticProperty.EMPTY;
            } else {
                if (iter.next() != null) {
                    return StaticProperty.ALLOWS_ONE_OR_MORE;
                } else {
                    return StaticProperty.EXACTLY_ONE;
                }
            }
        } catch (XPathException err) {
            // can't actually happen
            return StaticProperty.ALLOWS_ZERO_OR_MORE;
        }
    }

    /**
     * Get the n'th item in the sequence (starting from 0). This is defined for all
     * Values, but its real benefits come for a sequence Value stored extensionally
     * (or for a MemoClosure, once all the values have been read)
     * @param n position of the required item, counting from zero.
     * @return the n'th item in the sequence, where the first item in the sequence is
     * numbered zero. If n is negative or >= the length of the sequence, returns null.
     */

    public Item itemAt(int n) throws XPathException {
        if (n < 0) {
            return null;
        }
        int i = 0;        // indexing is zero-based
        SequenceIterator iter = iterate();
        while (true) {
            Item item = iter.next();
            if (item == null) {
                return null;
            }
            if (i++ == n) {
                return item;
            }
        }
    }

    /**
     * Get the length of the sequence
     * @return the number of items in the sequence
     */

    public int getLength() throws XPathException {
        return Aggregate.count(iterate());
    }

    /**
      * Process the value as an instruction, without returning any tail calls
      * @param context The dynamic context, giving access to the current node,
      * the current variables, etc.
      */

    public void process(XPathContext context) throws XPathException {
        SequenceIterator iter = iterate();
        SequenceReceiver out = context.getReceiver();
        while (true) {
            Item it = iter.next();
            if (it==null) break;
            out.append(it, 0, NodeInfo.ALL_NAMESPACES);
        }
    }


    /**
     * Convert the value to a string, using the serialization rules.
     * For atomic values this is the same as a cast; for sequence values
     * it gives a space-separated list.
     * @throws XPathException The method can fail if evaluation of the value
     * has been deferred, and if a failure occurs during the deferred evaluation.
     * No failure is possible in the case of an AtomicValue.
     */

    public String getStringValue() throws XPathException {
        FastStringBuffer sb = new FastStringBuffer(1024);
        SequenceIterator iter = iterate();
        Item item = iter.next();
        if (item != null) {
            while (true) {
                sb.append(item.getStringValueCS());
                item = iter.next();
                if (item == null) {
                    break;
                }
                sb.append(' ');
            }
        }
        return sb.toString();
=======
     * Get the object value associated with an index.
     * @param index
     *  The index must be between 0 and length() - 1.
     * @return An object value.
     * @throws JSONException If there is no value for the index.
     */
    public Object get(int index) throws JSONException {
        Object o = opt(index);
        if (o == null) {
            throw new JSONException("JSONArray[" + index + "] not found.");
        }
        return o;
    }

    /**
     * Get the boolean value associated with an index.
     * The string values "true" and "false" are converted to boolean.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return      The truth.
     * @throws JSONException If there is no value for the index or if the
     *  value is not convertable to boolean.
     */
    public boolean getBoolean(int index) throws JSONException {
        Object o = get(index);
//#if CLDC!="1.0"
//#         if (o.equals(Boolean.FALSE) ||
//#else
            if (o.equals(JSONObject.FALSE) ||
//#endif
                (o instanceof String &&
                ((String)o).toLowerCase().equals("false"))) {
            return false;
//#if CLDC!="1.0"
//#         } else if (o.equals(Boolean.TRUE) ||
//#else
        } else if (o.equals(JSONObject.TRUE) ||
//#endif
                (o instanceof String &&
                ((String)o).toLowerCase().equals("true"))) {
            return true;
        }
        throw new JSONException("JSONArray[" + index + "] is not a Boolean.");
    }

//#if CLDC!="1.0"
//#     /**
//#      * Get the double value associated with an index.
//#      *
//#      * @param index The index must be between 0 and length() - 1.
//#      * @return      The value.
//#      * @throws   JSONException If the key is not found or if the value cannot
//#      *  be converted to a number.
//#      */
//#     public double getDouble(int index) throws JSONException {
//#         Object o = get(index);
//#         try {
//#             return Double.valueOf((String)o).doubleValue();
//#         } catch (Exception e) {
//#             throw new JSONException("JSONArray[" + index +
//#                 "] is not a number.");
//#         }
//#     }
//#endif

//#if CLDC!="1.0"
//#     /**
//#      * Get the int value associated with an index.
//#      *
//#      * @param index The index must be between 0 and length() - 1.
//#      * @return      The value.
//#      * @throws   JSONException If the key is not found or if the value cannot
//#      *  be converted to a number.
//#      *  if the value cannot be converted to a number.
//#      */
//#     public int getInt(int index) throws JSONException {
//#         Object o = get(index);
//#         return (int)getDouble(index);
//#     }
//#endif

    /**
     * Get the JSONArray associated with an index.
     * @param index The index must be between 0 and length() - 1.
     * @return      A JSONArray value.
     * @throws JSONException If there is no value for the index. or if the
     * value is not a JSONArray
     */
    public JSONArray getJSONArray(int index) throws JSONException {
        Object o = get(index);
        if (o instanceof JSONArray) {
            return (JSONArray)o;
        }
        throw new JSONException("JSONArray[" + index +
                "] is not a JSONArray.");
    }


    /**
     * Get the JSONObject associated with an index.
     * @param index subscript
     * @return      A JSONObject value.
     * @throws JSONException If there is no value for the index or if the
     * value is not a JSONObject
     */
    public JSONObject getJSONObject(int index) throws JSONException {
        Object o = get(index);
        if (o instanceof JSONObject) {
            return (JSONObject)o;
        }
        throw new JSONException("JSONArray[" + index +
            "] is not a JSONObject.");
    }


//#if CLDC!="1.0"
//#     /**
//#      * Get the long value associated with an index.
//#      *
//#      * @param index The index must be between 0 and length() - 1.
//#      * @return      The value.
//#      * @throws   JSONException If the key is not found or if the value cannot
//#      *  be converted to a number.
//#      */
//#     public long getLong(int index) throws JSONException {
//#         Object o = get(index);
//#         return (long)getDouble(index);
//#     }
//#endif

    /**
     * Get the string associated with an index.
     * @param index The index must be between 0 and length() - 1.
     * @return      A string value.
     * @throws JSONException If there is no value for the index.
     */
    public String getString(int index) throws JSONException {
        return get(index).toString();
    }


    /**
     * Determine if the value is null.
     * @param index The index must be between 0 and length() - 1.
     * @return true if the value at the index is null, or if there is no value.
     */
    public boolean isNull(int index) {
        return JSONObject.NULL.equals(opt(index));
    }


    /**
     * Make a string from the contents of this JSONArray. The
     * <code>separator</code> string is inserted between each element.
     * Warning: This method assumes that the data structure is acyclical.
     * @param separator A string that will be inserted between the elements.
     * @return a string.
     * @throws JSONException If the array contains an invalid number.
     */
    public String join(String separator) throws JSONException {
        int len = length();
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < len; i += 1) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(JSONObject.valueToString(this.myArrayList.elementAt(i)));
        }
        return sb.toString();
    }


    /**
     * Get the number of elements in the JSONArray, included nulls.
     *
     * @return The length (or size).
     */
    public int length() {
        return this.myArrayList.size();
    }


    /**
     * Get the optional object value associated with an index.
     * @param index The index must be between 0 and length() - 1.
     * @return      An object value, or null if there is no
     *              object at that index.
     */
    public Object opt(int index) {
        return (index < 0 || index >= length()) ?
            null : this.myArrayList.elementAt(index);
    }

    /**
     * Get the optional boolean value associated with an index.
     * It returns false if there is no value at that index,
     * or if the value is not Boolean.TRUE or the String "true".
     *
     * @param index The index must be between 0 and length() - 1.
     * @return      The truth.
     */
    public boolean optBoolean(int index)  {
        return optBoolean(index, false);
    }
    
    /**
     * Get the optional boolean value associated with an index.
     * It returns the defaultValue if there is no value at that index or if
     * it is not a Boolean or the String "true" or "false" (case insensitive).
     *
     * @param index The index must be between 0 and length() - 1.
     * @param defaultValue     A boolean default.
     * @return      The truth.
     */
    public boolean optBoolean(int index, boolean defaultValue)  {
        try {
            return getBoolean(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }

//#if CLDC!="1.0"
//#     /**
//#      * Get the optional double value associated with an index.
//#      * NaN is returned if there is no value for the index,
//#      * or if the value is not a number and cannot be converted to a number.
//#      *
//#      * @param index The index must be between 0 and length() - 1.
//#      * @return      The value.
//#      */
//#     public double optDouble(int index) {
//#         return optDouble(index, Double.NaN);
//#     }
//#endif

//#if CLDC!="1.0"
//#     /**
//#      * Get the optional double value associated with an index.
//#      * The defaultValue is returned if there is no value for the index,
//#      * or if the value is not a number and cannot be converted to a number.
//#      *
//#      * @param index subscript
//#      * @param defaultValue     The default value.
//#      * @return      The value.
//#      */
//#     public double optDouble(int index, double defaultValue) {
//#         try {
//#             return getDouble(index);
//#         } catch (Exception e) {
//#             return defaultValue;
//#         }
//#     }
//#endif

//#if CLDC!="1.0"
//#     /**
//#      * Get the optional int value associated with an index.
//#      * Zero is returned if there is no value for the index,
//#      * or if the value is not a number and cannot be converted to a number.
//#      *
//#      * @param index The index must be between 0 and length() - 1.
//#      * @return      The value.
//#      */
//#     public int optInt(int index) {
//#         return optInt(index, 0);
//#     }
//#endif

//#if CLDC!="1.0"
//#     /**
//#      * Get the optional int value associated with an index.
//#      * The defaultValue is returned if there is no value for the index,
//#      * or if the value is not a number and cannot be converted to a number.
//#      * @param index The index must be between 0 and length() - 1.
//#      * @param defaultValue     The default value.
//#      * @return      The value.
//#      */
//#     public int optInt(int index, int defaultValue) {
//#         try {
//#             return getInt(index);
//#         } catch (Exception e) {
//#             return defaultValue;
//#         }
//#     }
//#endif

    /**
     * Get the optional JSONArray associated with an index.
     * @param index subscript
     * @return      A JSONArray value, or null if the index has no value,
     * or if the value is not a JSONArray.
     */
    public JSONArray optJSONArray(int index) {
        Object o = opt(index);
        return o instanceof JSONArray ? (JSONArray)o : null;
    }


    /**
     * Get the optional JSONObject associated with an index.
     * Null is returned if the key is not found, or null if the index has
     * no value, or if the value is not a JSONObject.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return      A JSONObject value.
     */
    public JSONObject optJSONObject(int index) {
        Object o = opt(index);
        return o instanceof JSONObject ? (JSONObject)o : null;
    }

//#if CLDC!="1.0"
//#     /**
//#      * Get the optional long value associated with an index.
//#      * Zero is returned if there is no value for the index,
//#      * or if the value is not a number and cannot be converted to a number.
//#      *
//#      * @param index The index must be between 0 and length() - 1.
//#      * @return      The value.
//#      */
//#     public long optLong(int index) {
//#         return optLong(index, 0);
//#     }
//#endif

//#if CLDC!="1.0"
//#     /**
//#      * Get the optional long value associated with an index.
//#      * The defaultValue is returned if there is no value for the index,
//#      * or if the value is not a number and cannot be converted to a number.
//#      * @param index The index must be between 0 and length() - 1.
//#      * @param defaultValue     The default value.
//#      * @return      The value.
//#      */
//#     public long optLong(int index, long defaultValue) {
//#         try {
//#             return getLong(index);
//#         } catch (Exception e) {
//#             return defaultValue;
//#         }
//#     }
//#endif

    /**
     * Get the optional string value associated with an index. It returns an
     * empty string if there is no value at that index. If the value
     * is not a string and is not null, then it is coverted to a string.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return      A String value.
     */
    public String optString(int index) {
        return optString(index, "");
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }


    /**
<<<<<<< HEAD
     * Get the effective boolean value of the expression. This returns false if the value
     * is the empty sequence, a zero-length string, a number equal to zero, or the boolean
     * false. Otherwise it returns true.
     *
     * @exception XPathException if any dynamic error occurs evaluating the
     *     expression
     * @return the effective boolean value
     */

    public boolean effectiveBooleanValue() throws XPathException {
        return ExpressionTool.effectiveBooleanValue(iterate());
    }

    /**
     * Get a Comparable value that implements the XML Schema ordering comparison semantics for this value.
     * The default implementation is written to compare sequences of atomic values.
     * This method is overridden for AtomicValue and its subclasses.
     *
     * <p>In the case of data types that are partially ordered, the returned Comparable extends the standard
     * semantics of the compareTo() method by returning the value {@link #INDETERMINATE_ORDERING} when there
     * is no defined order relationship between two given values.</p>
     *
     * @return a Comparable that follows XML Schema comparison rules
     */

    public Comparable getSchemaComparable() {
        return new ValueSchemaComparable();
    }

    private class ValueSchemaComparable implements Comparable {
        public Value getValue() {
            return Value.this;
        }
        public int compareTo(Object obj) {
            try {
                if (obj instanceof ValueSchemaComparable) {
                    SequenceIterator iter1 = getValue().iterate();
                    SequenceIterator iter2 = ((ValueSchemaComparable)obj).getValue().iterate();
                    while (true) {
                        Item item1 = iter1.next();
                        Item item2 = iter2.next();
                        if (item1 == null && item2 == null) {
                            return 0;
                        }
                        if (item1 == null) {
                            return -1;
                        } else if (item2 == null) {
                            return +1;
                        }
                        if (item1 instanceof NodeInfo || item2 instanceof NodeInfo) {
                            throw new UnsupportedOperationException("Sequences containing nodes are not schema-comparable");
                        }
                        int c = ((AtomicValue)item1).getSchemaComparable().compareTo(
                                    ((AtomicValue)item2).getSchemaComparable());
                        if (c != 0) {
                            return c;
                        }
                    }
                } else {
                    return INDETERMINATE_ORDERING;
                }
            } catch (XPathException e) {
                throw new AssertionError("Failure comparing schema values: " + e.getMessage());
            }
        }

        public boolean equals(Object obj) {
            return compareTo(obj) == 0;
        }

        public int hashCode() {
            try {
                int hash = 0x06639662;  // arbitrary seed
                SequenceIterator iter = getValue().iterate();
                while (true) {
                    Item item = iter.next();
                    if (item == null) {
                        return hash;
                    }
                    hash ^= ((AtomicValue)item).getSchemaComparable().hashCode();
                }
            } catch (XPathException e) {
                return 0;
            }
        }
    }

    /**
     * Constant returned by compareTo() method to indicate an indeterminate ordering between two values
     */

    public static final int INDETERMINATE_ORDERING = Integer.MIN_VALUE;

    /**
     * Compare two (sequence) values for equality. This method implements the XPath eq operator, for cases
     * where it is defined. For values containing nodes, nodes are compared for identity.
     * In cases where eq is not defined, it throws ClassCastException. In cases
     * where the result of eq is an empty sequence, this function returns false, except that two empty
     * sequences compare equal. The method also returns a ClassCastException
     * if any failure occurs evaluating either of the values.
     */

    public boolean equals(Object obj) {
        throw new UnsupportedOperationException("Value.equals()");
//        try {
//            if (obj instanceof Value) {
//                SequenceIterator iter1 = iterate();
//                SequenceIterator iter2 = ((Value)obj).iterate();
//                while (true) {
//                    Item item1 = iter1.next();
//                    Item item2 = iter2.next();
//                    if (item1 == null || item2 == null) {
//                        return item1 == null && item2 == null;
//                    }
//                    if (!item1.equals(item2)) {
//                        return false;
//                    }
//                }
//            } else {
//                return false;
//            }
//        } catch (XPathException e) {
//            throw new ClassCastException(e.getMessage());
//        }
=======
     * Get the optional string associated with an index.
     * The defaultValue is returned if the key is not found.
     *
     * @param index The index must be between 0 and length() - 1.
     * @param defaultValue     The default value.
     * @return      A String value.
     */
    public String optString(int index, String defaultValue) {
        Object o = opt(index);
        return o != null ? o.toString() : defaultValue;
    }


    /**
     * Append a boolean value. This increases the array's length by one.
     *
     * @param value A boolean value.
     * @return this.
     */
    public JSONArray put(boolean value) {
//#if CLDC!="1.0"
//#         put(value ? Boolean.TRUE : Boolean.FALSE);
//#else
        put(value ? JSONObject.TRUE : JSONObject.FALSE);
//#endif
        return this;
    }

    /**
     * Put a value in the JSONArray, where the value will be a
     * JSONArray which is produced from a Collection.
     * @param value	A Collection value.
     * @return		this.
     */
    public JSONArray put(Vector value) {
        put(new JSONArray(value));
        return this;
    }
    

//#if CLDC!="1.0"
//#     /**
//#      * Append a double value. This increases the array's length by one.
//#      *
//#      * @param value A double value.
//#      * @throws JSONException if the value is not finite.
//#      * @return this.
//#      */
//#     public JSONArray put(double value) throws JSONException {
//#         Double d = new Double(value);
//#         JSONObject.testValidity(d);
//#         put(d);
//#         return this;
//#     }
//#endif

    /**
     * Append an int value. This increases the array's length by one.
     *
     * @param value An int value.
     * @return this.
     */
    public JSONArray put(int value) {
        put(new Integer(value));
        return this;
    }


    /**
     * Append an long value. This increases the array's length by one.
     *
     * @param value A long value.
     * @return this.
     */
    public JSONArray put(long value) {
        put(new Long(value));
        return this;
    }


//#ifdef PRODUCER
//#     /**
//#      * Put a value in the JSONArray, where the value will be a
//#      * JSONObject which is produced from a Map.
//#      * @param value	A Map value.
//#      * @return		this.
//#      */
//#     public JSONArray put(Hashtable value) {
//#         put(new JSONObject(value));
//#         return this;
//#     }
//#endif    
    
    /**
     * Append an object value. This increases the array's length by one.
     * @param value An object value.  The value should be a
     *  Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the
     *  JSONObject.NULL object.
     * @return this.
     */
    public JSONArray put(Object value) {
        this.myArrayList.addElement(value);
        return this;
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }


    /**
<<<<<<< HEAD
     * Check statically that the results of the expression are capable of constructing the content
     * of a given schema type.
     * @param parentType The schema type
     * @param env the static context
     * @param whole true if this value accounts for the entire content of the containing node
     * @throws XPathException if the expression doesn't match the required content type
     */

    public void checkPermittedContents(SchemaType parentType, StaticContext env, boolean whole) throws XPathException {
        //return;
    }

    /**
     * Reduce a value to its simplest form. If the value is a closure or some other form of deferred value
     * such as a FunctionCallPackage, then it is reduced to a SequenceExtent. If it is a SequenceExtent containing
     * a single item, then it is reduced to that item. One consequence that is exploited by class FilterExpression
     * is that if the value is a singleton numeric value, then the result will be an instance of NumericValue
     * @return the value in simplified form
     */

    public Value reduce() throws XPathException {
        return this;
    }

    /**
     * Convert to Java object (for passing to external functions)
     * @param target the required target class
     * @param context the XPath dynamic evaluation context
     * @return the (boxed) Java object that best represents the XPath value. This is guaranteed
     * to be an instance of the target class
     */

//    public Object convertToJava(Class target, XPathContext context) throws XPathException {
//        // TODO: delete this method and its implementations
//        // This is overridden in subclasses that handle singleton objects
//        return convertSequenceToJava(target, context);
//    }

    /**
     * Convert this value to a Java object
     * @param target the required class of the resulting Java object
     * @param context the XPath evaluation context
     * @return the Java object, which will always be an instance of the target class
     * @throws XPathException if conversion is not possible
     */

//    public final Object convertSequenceToJava(Class target, XPathContext context) throws XPathException {
//        // TODO: delete this method and its implementations
//        if (target == Object.class) {
//            List list = new ArrayList(20);
//            return convertToJavaList(list, context);
//        }
//
//        // See if the extension function is written to accept native Saxon objects
//
//        if (target.isAssignableFrom(getClass())) {
//            return this;
//        } else if (target.isAssignableFrom(SequenceIterator.class)) {
//            return iterate();
//        }
//
//        // Offer the object to registered external object models
//
//        if ((this instanceof ObjectValue || !(this instanceof AtomicValue)) && !(this instanceof EmptySequence)) {
//            List externalObjectModels = context.getConfiguration().getExternalObjectModels();
//            for (int m=0; m<externalObjectModels.size(); m++) {
//                ExternalObjectModel model = (ExternalObjectModel)externalObjectModels.get(m);
//                Object object = model.convertXPathValueToObject(this, target, context);
//                if (object != null) {
//                    return object;
//                }
//            }
//        }
//
//        if (Collection.class.isAssignableFrom(target)) {
//            Collection list;
//            if (target.isAssignableFrom(ArrayList.class)) {
//                list = new ArrayList(100);
//            } else {
//                try {
//                    list = (Collection)target.newInstance();
//                } catch (InstantiationException e) {
//                    XPathException de = new XPathException("Cannot instantiate collection class " + target);
//                    de.setXPathContext(context);
//                    throw de;
//                } catch (IllegalAccessException e) {
//                    XPathException de = new XPathException("Cannot access collection class " + target);
//                    de.setXPathContext(context);
//                    throw de;
//                }
//            }
//            return convertToJavaList(list, context);
//        } else if (target.isArray()) {
//            Class component = target.getComponentType();
//            if (component.isAssignableFrom(Item.class) ||
//                    component.isAssignableFrom(NodeInfo.class) ||
//                    component.isAssignableFrom(DocumentInfo.class)) {
//                Value extent = this;
//                if (extent instanceof Closure) {
//                    extent = Value.asValue(SequenceExtent.makeSequenceExtent(extent.iterate()));
//                }
//                int length = extent.getLength();
//                Object array = Array.newInstance(component, length);
//                SequenceIterator iter = extent.iterate();
//                for (int i=0; i<length; i++) {
//                    Item item = iter.next();
//                    try {
//                        Array.set(array, i, item);
//                    } catch (IllegalArgumentException err) {
//                        XPathException d = new XPathException("Item " + i + " in supplied sequence cannot be converted " +
//                                "to the component type of the Java array (" + component + ')', err);
//                        d.setErrorCode(SaxonErrorCode.SXJE0023);
//                        d.setXPathContext(context);
//                        throw d;
//                    }
//                }
//                return array;
//            } else /* if (!(this instanceof AtomicValue)) */ {
//                // try atomizing the sequence, unless this is a single atomic value, in which case we've already
//                // tried that.
//                SequenceIterator it = Atomizer.getAtomizingIterator(iterate());
//                int length;
//                if ((it.getProperties() & SequenceIterator.LAST_POSITION_FINDER) == 0) {
//                    SequenceExtent extent = new SequenceExtent(it);
//                    length = extent.getLength();
//                    it = extent.iterate();
//                } else {
//                    length = ((LastPositionFinder)it).getLastPosition();
//                }
//                Object array = Array.newInstance(component, length);
//                for (int i=0; i<length; i++) {
//                    try {
//                        AtomicValue val = (AtomicValue)it.next();
//                        Object jval = val.convertToJava(component, context);
//                        Array.set(array, i, jval);
//                    } catch (XPathException err) {
//                        XPathException d = new XPathException("Cannot convert item in atomized sequence to the component type of the Java array", err);
//                        d.setErrorCode(SaxonErrorCode.SXJE0023);
//                        d.setXPathContext(context);
//                        throw d;
//                    }
//                }
//                return array;
//            }
//
//        } else if (target.isAssignableFrom(Item.class) ||
//                target.isAssignableFrom(NodeInfo.class) ||
//                target.isAssignableFrom(DocumentInfo.class)) {
//
//            // try passing the first item in the sequence provided it is the only one
//            SequenceIterator iter = iterate();
//            Item first = null;
//            while (true) {
//                Item next = iter.next();
//                if (next == null) {
//                    break;
//                }
//                if (first != null) {
//                    XPathException err = new XPathException("Sequence contains more than one value; Java method expects only one", SaxonErrorCode.SXJE0022);
//                    err.setXPathContext(context);
//                    throw err;
//                }
//                first = next;
//            }
//            if (first == null) {
//                // sequence is empty; pass a Java null
//                return null;
//            }
//            if (target.isAssignableFrom(first.getClass())) {
//                // covers Item and NodeInfo
//                return first;
//            }
//
//            Object n = first;
//            while (n instanceof VirtualNode) {
//                // If we've got a wrapper around a DOM or JDOM node, and the user wants a DOM
//                // or JDOM node, we unwrap it
//                Object vn = ((VirtualNode) n).getUnderlyingNode();
//                if (target.isAssignableFrom(vn.getClass())) {
//                    return vn;
//                } else {
//                    n = vn;
//                }
//            }
//
//            throw new XPathException(
//                    "Cannot convert supplied XPath value to the required type for the extension function",
//                    SaxonErrorCode.SXJE0021);
//        } else if (!(this instanceof AtomicValue)) {
//            // try atomizing the value, unless this is an atomic value, in which case we've already tried that
//            SequenceIterator it = Atomizer.getAtomizingIterator(iterate());
//            Item first = null;
//            while (true) {
//                Item next = it.next();
//                if (next == null) {
//                    break;
//                }
//                if (first != null) {
//                    XPathException err = new XPathException("Sequence contains more than one value; Java method expects only one", SaxonErrorCode.SXJE0022);
//                    err.setXPathContext(context);
//                    throw err;
//                }
//                first = next;
//            }
//            if (first == null) {
//                // sequence is empty; pass a Java null
//                return null;
//            }
//            if (target.isAssignableFrom(first.getClass())) {
//                return first;
//            } else {
//                return ((AtomicValue)first).convertToJava(target, context);
//            }
//        } else {
//            throw new XPathException("Cannot convert supplied XPath value to the required type for the extension function",
//                    SaxonErrorCode.SXJE0021);
//        }
//    }
    
    /**
     * Convert this XPath value to a Java collection
     * @param list an empty Collection, to which the relevant values will be added
     * @param context the evaluation context
     * @return the supplied list, with relevant values added
     * @throws XPathException
     */

//    public Collection convertToJavaList(Collection list, XPathContext context) throws XPathException {
//        // TODO: with JDK 1.5, check to see if the item type of the list is constrained
//        SequenceIterator iter = iterate();
//        while (true) {
//            Item it = iter.next();
//            if (it == null) {
//                return list;
//            }
//            if (it instanceof AtomicValue) {
//                list.add(((AtomicValue)it).convertToJava(Object.class, context));
//            } else if (it instanceof VirtualNode) {
//                list.add(((VirtualNode)it).getUnderlyingNode());
//            } else {
//                list.add(it);
//            }
//        }
//    }

    /**
    * Convert a Java object to an XPath value. This method is called to handle the result
    * of an external function call, and also to process global parameters passed to the stylesheet or query.
    * @param object The Java object to be converted
    * @param requiredType The required type of the result (if known)
    * @param context The XPath dynamic context
     * @return the result of converting the value. If the value is null, returns null.
    */

//    public static Value convertJavaObjectToXPath(Object object, SequenceType requiredType, XPathContext context)
//                                          throws XPathException {
//
//        Configuration config= context.getConfiguration();
//        ItemType requiredItemType = requiredType.getPrimaryType();
//
//        // TODO: make more use of the requiredType, e.g. to decide what to convert a Date into.
//
//        if (object==null) {
//            return EmptySequence.getInstance();
//        }
//
//        if (object instanceof Value && requiredType.matches((Value)object, config)) {
//            return (Value)object;
//        }
//
//        // Offer the object to all the registered external object models
//
//        List externalObjectModels = config.getExternalObjectModels();
//        for (int m=0; m<externalObjectModels.size(); m++) {
//            ExternalObjectModel model = (ExternalObjectModel)externalObjectModels.get(m);
//            Value val = model.convertObjectToXPathValue(object, config);
//            if (val != null && TypeChecker.testConformance(val, requiredType, context) == null) {
//                return val;
//            }
//        }
//
//        if (requiredItemType instanceof ExternalObjectType) {
//            Class theClass = ((ExternalObjectType)requiredItemType).getJavaClass();
//            if (theClass.isAssignableFrom(object.getClass())) {
//                return new ObjectValue(object, (ExternalObjectType)requiredItemType);
//            } else {
//                throw new XPathException("Supplied parameter value is of class " + object.getClass().getName() +
//                        " - it needs to be of class " + theClass.getName());
//            }
//        }
//
//        return convertToBestFit(object, config);
//
//    }

//    public static Value convertToBestFit(Object object, Configuration config) throws XPathException {
//        if (object instanceof String) {
//            return StringValue.makeStringValue((String)object);
//
//        } else if (object instanceof Character) {
//            return new StringValue(object.toString());
//
//        } else if (object instanceof Boolean) {
//            return BooleanValue.get(((Boolean)object).booleanValue());
//
//        } else if (object instanceof Double) {
//            return new DoubleValue(((Double)object).doubleValue());
//
//        } else if (object instanceof Float) {
//            return new FloatValue(((Float)object).floatValue());
//
//        } else if (object instanceof Short) {
//            return new Int64Value(((Short)object).shortValue(), BuiltInAtomicType.SHORT, false);
//        } else if (object instanceof Integer) {
//            return new Int64Value(((Integer)object).intValue(), BuiltInAtomicType.INT, false);
//        } else if (object instanceof Long) {
//            return new Int64Value(((Long)object).longValue(), BuiltInAtomicType.LONG, false);
//        } else if (object instanceof Byte) {
//            return new Int64Value(((Byte)object).byteValue(), BuiltInAtomicType.BYTE, false);
//
//        } else if (object instanceof BigInteger) {
//            return BigIntegerValue.makeIntegerValue(((BigInteger)object));
//
//        } else if (object instanceof BigDecimal) {
//            return new DecimalValue(((BigDecimal)object));
//
////        } else if (object instanceof QName) {
////            return new QNameValue((QName)object);
//            // TODO: reinstate above lines in JDK 1.5
//        } else if (object.getClass().getName().equals("javax.xml.namespace.QName")) {
//            return makeQNameValue(object, config);
//
//        } else if (object instanceof URI) {
//            return new AnyURIValue(object.toString());
//
//        } else if (object instanceof URL) {
//            return new AnyURIValue(object.toString());
//
//        } else if (object instanceof Date) {
//            return DateTimeValue.fromJavaDate((Date)object);
//
//        // TODO: recognize GregorianCalendar...
//
//        } else if (object instanceof Closure) {
//            // Force eager evaluation, because of problems with side-effects.
//            // (The value might depend on data that is mutable.)
//            //return Value.asValue(ExpressionTool.evaluate((Closure)object, ExpressionTool.ITERATE_AND_MATERIALIZE, config.getConversionContext(), 10));
//            return Value.asValue(
//                    SequenceExtent.makeSequenceExtent(((Closure)object).iterate()));
//        } else if (object instanceof Value) {
//            return (Value)object;
//
//        } else if (object instanceof NodeInfo) {
//            if (!((NodeInfo)object).getConfiguration().isCompatible(config)) {
//                throw new XPathException(
//                        "Externally-supplied NodeInfo belongs to a different and incompatible Configuration",
//                        SaxonErrorCode.SXXP0004);
//            }
//            return new SingletonNode((NodeInfo)object);
//
//        } else if (object instanceof SequenceIterator) {
//            return Closure.makeIteratorClosure((SequenceIterator)object);
//
//        } else if (object instanceof List) {
//            Item[] array = new Item[((List)object).size()];
//            int a = 0;
//            for (Iterator i=((List)object).iterator(); i.hasNext(); ) {
//                Object obj = i.next();
//                if (obj instanceof NodeInfo) {
//                    if (!((NodeInfo)obj).getConfiguration().isCompatible(config)) {
//                        throw new XPathException("Externally-supplied NodeInfo belongs to wrong Configuration",
//                        SaxonErrorCode.SXXP0004);
//                    }
//                    array[a++] = (NodeInfo)obj;
//                } else {
//                    Value v = convertToBestFit(obj, config);
//                    if (v!=null) {
//                        if (v instanceof Item) {
//                            array[a++] = (Item)v;
//                        } else if (v instanceof EmptySequence) {
//                            // no action
//                        } else if (v instanceof SingletonNode) {
//                            NodeInfo node = ((SingletonNode)v).getNode();
//                            if (node != null) {
//                                array[a++] = node;
//                            }
//                        } else {
//                            throw new XPathException(
//                                    "Returned List contains an object that cannot be converted to an Item (" + obj.getClass() + ')',
//                                    SaxonErrorCode.SXJE0051);
//                        }
//                    }
//                }
//            }
//
//            return new SequenceExtent(array);
//
//        } else if (object instanceof Object[]) {
//            Object[] arrayObject = (Object[])object;
//            Item[] newArray = new Item[arrayObject.length];
//             int a = 0;
//             for (int i = 0; i < arrayObject.length; i++){
//                 Object itemObject = arrayObject[i];
//                 if (itemObject instanceof NodeInfo) {
//                     if (!((NodeInfo)itemObject).getConfiguration().isCompatible(config)) {
//                         throw new XPathException(
//                                 "Externally-supplied NodeInfo belongs to a different and incompatible Configuration",
//                                 SaxonErrorCode.SXXP0004);
//                     }
//                     newArray[a++] = (NodeInfo)itemObject;
//                 } else if (itemObject != null) {
//                     Value v = convertToBestFit(itemObject, config);
//                     if (v!=null) {
//                         if (v instanceof Item) {
//                             newArray[a++] = (Item)v;
//                         } else {
//                             throw new XPathException(
//                                     "Returned array contains an object that cannot be converted to an Item (" +
//                                            itemObject.getClass() + ')',
//                                     SaxonErrorCode.SXJE0051);
//                         }
//                     }
//                 }
//             }
//             return new SequenceExtent(newArray, 0, a);
//
//        } else if (object instanceof long[]) {
//             Item[] array = new Item[((long[])object).length];
//             for (int i = 0; i < array.length; i++){
//                 array[i] = Int64Value.makeIntegerValue(((long[])object)[i]);
//             }
//             return new SequenceExtent(array);
//
//        } else if (object instanceof int[]) {
//             Item[] array = new Item[((int[])object).length];
//             for (int i = 0; i < array.length; i++){
//                 array[i] = Int64Value.makeIntegerValue(((int[])object)[i]);
//             }
//             return new SequenceExtent(array);
//
//        } else if (object instanceof short[]) {
//             Item[] array = new Item[((short[])object).length];
//             for (int i = 0; i < array.length; i++){
//                 array[i] = Int64Value.makeIntegerValue(((short[])object)[i]);
//             }
//             return new SequenceExtent(array);
//
//        } else if (object instanceof byte[]) {  // interpret this as unsigned bytes
//             Item[] array = new Item[((byte[])object).length];
//             for (int i = 0; i < array.length; i++){
//                 array[i] = Int64Value.makeIntegerValue(255 & (int)((byte[])object)[i]);
//             }
//             return new SequenceExtent(array);
//
//        } else if (object instanceof char[]) {
//             return StringValue.makeStringValue(new String((char[])object));
//
//        } else if (object instanceof boolean[]) {
//             Item[] array = new Item[((boolean[])object).length];
//             for (int i = 0; i < array.length; i++){
//                 array[i] = BooleanValue.get(((boolean[])object)[i]);
//             }
//             return new SequenceExtent(array);
//
//        } else if (object instanceof double[]) {
//             Item[] array = new Item[((double[])object).length];
//             for (int i = 0; i < array.length; i++){
//                 array[i] = new DoubleValue(((double[])object)[i]);
//             }
//             return new SequenceExtent(array);
//
//        } else if (object instanceof float[]) {
//             Item[] array = new Item[((float[])object).length];
//             for (int i = 0; i < array.length; i++){
//                 array[i] = new FloatValue(((float[])object)[i]);
//             }
//             return new SequenceExtent(array);
//
//        } else if (object instanceof Source && config != null) {
//            if (object instanceof DOMSource) {
//                NodeInfo node = config.unravel((Source)object);
//                if (!node.getConfiguration().isCompatible(config)) {
//                    throw new XPathException(
//                            "Externally-supplied DOM Node belongs to a different and incompatible Configuration",
//                            SaxonErrorCode.SXXP0004);
//                }
//                return new SingletonNode(node);
//            }
//            try {
//                Builder b = (config.getTreeModel() == Builder.TINY_TREE ?
//                        (Builder)new TinyBuilder() : (Builder)new TreeBuilder());
//                PipelineConfiguration pipe = config.makePipelineConfiguration();
//                b.setPipelineConfiguration(pipe);
//                new Sender(pipe).send((Source)object, b);
//                if (object instanceof AugmentedSource && ((AugmentedSource)object).isPleaseCloseAfterUse()) {
//                     ((AugmentedSource)object).close();
//                }
//                return new SingletonNode(b.getCurrentRoot());
//            } catch (XPathException err) {
//                throw new XPathException(err);
//            }
//        } else {
//            // See whether this is an object representing a Node in some recognized object model
//            ExternalObjectModel model = config.findExternalObjectModel(object);
//            if (model != null) {
//                DocumentInfo doc = model.wrapDocument(object, "", config);
//                NodeInfo node = model.wrapNode(doc, object);
//                return Value.asValue(node);
//            }
//        }
//        return new ObjectValue(object);
//    }
//
    /**
     * Temporary method to make a QNameValue from a JAXP 1.3 QName, without creating a compile-time link
     * to the JDK 1.5 QName class
     * @param object an instance of javax.xml.namespace.QName
     * @param config the Saxon configuration (used for dynamic loading)
     * @return a corresponding Saxon QNameValue, or null if any error occurs performing the conversion
     */

    public static QNameValue makeQNameValue(Object object, Configuration config) {
        try {
            Class qnameClass = config.getClass("javax.xml.namespace.QName", false, null);
            Class[] args = EMPTY_CLASS_ARRAY;
            Method getPrefix = qnameClass.getMethod("getPrefix", args);
            Method getLocalPart = qnameClass.getMethod("getLocalPart", args);
            Method getNamespaceURI = qnameClass.getMethod("getNamespaceURI", args);
            String prefix = (String)getPrefix.invoke(object, (Object[])args);
            String localPart = (String)getLocalPart.invoke(object, (Object[])args);
            String uri = (String)getNamespaceURI.invoke(object, (Object[])args);
            return new QNameValue(prefix, uri, localPart, BuiltInAtomicType.QNAME, config.getNameChecker());
        } catch (XPathException e) {
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        } catch (InvocationTargetException e) {
            return null;
        }
    }

    public static final Class[] EMPTY_CLASS_ARRAY = new Class[0];

    /**
     * Convert to a string for diagnostic output
     */

    public String toString() {
        try {
            return getStringValue();
        } catch (XPathException err) {
            return super.toString();
        }
    }

    /**
     * Convert an XPath value to a Java object.
     * An atomic value is returned as an instance
     * of the best available Java class. If the item is a node, the node is "unwrapped",
     * to return the underlying node in the original model (which might be, for example,
     * a DOM or JDOM node).
     * @param item the item to be converted
     * @return the value after conversion
    */

    public static Object convertToJava(Item item) throws XPathException {
        if (item instanceof NodeInfo) {
            Object node = item;
            while (node instanceof VirtualNode) {
                // strip off any layers of wrapping
                node = ((VirtualNode)node).getUnderlyingNode();
            }
            return node;
        } else if (item instanceof ObjectValue) {
            return ((ObjectValue)item).getObject();
        } else {
            AtomicValue value = (AtomicValue)item;
            switch (value.getItemType(null).getPrimitiveType()) {
                case StandardNames.XS_STRING:
                case StandardNames.XS_UNTYPED_ATOMIC:
                case StandardNames.XS_ANY_URI:
                case StandardNames.XS_DURATION:
                    return value.getStringValue();
                case StandardNames.XS_BOOLEAN:
                    return (((BooleanValue)value).getBooleanValue() ? Boolean.TRUE : Boolean.FALSE );
                case StandardNames.XS_DECIMAL:
                    return ((DecimalValue)value).getDecimalValue();
                case StandardNames.XS_INTEGER:
                    return new Long(((NumericValue)value).longValue());
                case StandardNames.XS_DOUBLE:
                    return new Double(((DoubleValue)value).getDoubleValue());
                case StandardNames.XS_FLOAT:
                    return new Float(((FloatValue)value).getFloatValue());
                case StandardNames.XS_DATE_TIME:
                    return ((DateTimeValue)value).getCalendar().getTime();
                case StandardNames.XS_DATE:
                    return ((DateValue)value).getCalendar().getTime();
                case StandardNames.XS_TIME:
                    return value.getStringValue();
                case StandardNames.XS_BASE64_BINARY:
                    return ((Base64BinaryValue)value).getBinaryValue();
                case StandardNames.XS_HEX_BINARY:
                    return ((HexBinaryValue)value).getBinaryValue();
                default:
                    return item;
            }
        }
    }

}

//
// The contents of this file are subject to the Mozilla Public License Version 1.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is Michael H. Kay.
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
// Contributor(s): none.
//

=======
     * Put or replace a boolean value in the JSONArray. If the index is greater
     * than the length of the JSONArray, then null elements will be added as
     * necessary to pad it out.
     * @param index The subscript.
     * @param value A boolean value.
     * @return this.
     * @throws JSONException If the index is negative.
     */
    public JSONArray put(int index, boolean value) throws JSONException {
//#if CLDC!="1.0"
//#         put(index, value ? Boolean.TRUE : Boolean.FALSE);
//#else
        put(index, value ? JSONObject.TRUE : JSONObject.FALSE);
//#endif
        return this;
    }
    
    /**
     * Put a value in the JSONArray, where the value will be a
     * JSONArray which is produced from a Collection.
     * @param index The subscript.
     * @param value	A Collection value.
     * @return		this.
     * @throws JSONException If the index is negative or if the value is
     * not finite.
     */
    public JSONArray put(int index, Vector value) throws JSONException {
        put(index, new JSONArray(value));
        return this;
    }

    
//#if CLDC!="1.0"
//#     /**
//#      * Put or replace a double value. If the index is greater than the length of
//#      *  the JSONArray, then null elements will be added as necessary to pad
//#      *  it out.
//#      * @param index The subscript.
//#      * @param value A double value.
//#      * @return this.
//#      * @throws JSONException If the index is negative or if the value is
//#      * not finite.
//#      */
//#     public JSONArray put(int index, double value) throws JSONException {
//#         put(index, new Double(value));
//#         return this;
//#     }
//#endif

    /**
     * Put or replace an int value. If the index is greater than the length of
     *  the JSONArray, then null elements will be added as necessary to pad
     *  it out.
     * @param index The subscript.
     * @param value An int value.
     * @return this.
     * @throws JSONException If the index is negative.
     */
    public JSONArray put(int index, int value) throws JSONException {
        put(index, new Integer(value));
        return this;
    }


    /**
     * Put or replace a long value. If the index is greater than the length of
     *  the JSONArray, then null elements will be added as necessary to pad
     *  it out.
     * @param index The subscript.
     * @param value A long value.
     * @return this.
     * @throws JSONException If the index is negative.
     */
    public JSONArray put(int index, long value) throws JSONException {
        put(index, new Long(value));
        return this;
    }


//#ifdef PRODUCER
//#     /**
//#      * Put a value in the JSONArray, where the value will be a
//#      * JSONObject which is produced from a Map.
//#      * @param index The subscript.
//#      * @param value	The Map value.
//#      * @return		this.
//#      * @throws JSONException If the index is negative or if the the value is
//#      *  an invalid number.
//#      */
//#     public JSONArray put(int index, Hashtable value) throws JSONException {
//#         put(index, new JSONObject(value));
//#         return this;
//#     }
//#endif    
    
    /**
     * Put or replace an object value in the JSONArray. If the index is greater
     *  than the length of the JSONArray, then null elements will be added as
     *  necessary to pad it out.
     * @param index The subscript.
     * @param value The value to put into the array. The value should be a
     *  Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the
     *  JSONObject.NULL object.
     * @return this.
     * @throws JSONException If the index is negative or if the the value is
     *  an invalid number.
     */
    public JSONArray put(int index, Object value) throws JSONException {
        JSONObject.testValidity(value);
        if (index < 0) {
            throw new JSONException("JSONArray[" + index + "] not found.");
        }
        if (index < length()) {
            this.myArrayList.setElementAt(value, index);
        } else {
            while (index != length()) {
                put(JSONObject.NULL);
            }
            put(value);
        }
        return this;
    }


    /**
     * Produce a JSONObject by combining a JSONArray of names with the values
     * of this JSONArray.
     * @param names A JSONArray containing a list of key strings. These will be
     * paired with the values.
     * @return A JSONObject, or null if there are no names or if this JSONArray
     * has no values.
     * @throws JSONException If any of the names are null.
     */
    public JSONObject toJSONObject(JSONArray names) throws JSONException {
        if (names == null || names.length() == 0 || length() == 0) {
            return null;
        }
        JSONObject jo = new JSONObject();
        for (int i = 0; i < names.length(); i += 1) {
            jo.put(names.getString(i), this.opt(i));
        }
        return jo;
    }


    /**
     * Make a JSON text of this JSONArray. For compactness, no
     * unnecessary whitespace is added. If it is not possible to produce a
     * syntactically correct JSON text then null will be returned instead. This
     * could occur if the array contains an invalid number.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @return a printable, displayable, transmittable
     *  representation of the array.
     */
    public String toString() {
        try {
            return '[' + join(",") + ']';
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Make a prettyprinted JSON text of this JSONArray.
     * Warning: This method assumes that the data structure is acyclical.
     * @param indentFactor The number of spaces to add to each level of
     *  indentation.
     * @return a printable, displayable, transmittable
     *  representation of the object, beginning
     *  with <code>[</code>&nbsp;<small>(left bracket)</small> and ending
     *  with <code>]</code>&nbsp;<small>(right bracket)</small>.
     * @throws JSONException
     */
    public String toString(int indentFactor) throws JSONException {
        return toString(indentFactor, 0);
    }


    /**
     * Make a prettyprinted JSON text of this JSONArray.
     * Warning: This method assumes that the data structure is acyclical.
     * @param indentFactor The number of spaces to add to each level of
     *  indentation.
     * @param indent The indention of the top level.
     * @return a printable, displayable, transmittable
     *  representation of the array.
     * @throws JSONException
     */
    String toString(int indentFactor, int indent) throws JSONException {
        int len = length();
        if (len == 0) {
            return "[]";
        }
        int i;
        StringBuffer sb = new StringBuffer("[");
        if (len == 1) {
            sb.append(JSONObject.valueToString(this.myArrayList.elementAt(0),
                    indentFactor, indent));
        } else {
            int newindent = indent + indentFactor;
            sb.append('\n');
            for (i = 0; i < len; i += 1) {
                if (i > 0) {
                    sb.append(",\n");
                }
                for (int j = 0; j < newindent; j += 1) {
                    sb.append(' ');
                }
                sb.append(JSONObject.valueToString(this.myArrayList.elementAt(i),
                        indentFactor, newindent));
            }
            sb.append('\n');
            for (i = 0; i < indent; i += 1) {
                sb.append(' ');
            }
        }
        sb.append(']');
        return sb.toString();
    }


    /**
     * Write the contents of the JSONArray as JSON text to a writer.
     * For compactness, no whitespace is added.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @return The writer.
     * @throws JSONException
     */
    public Writer write(Writer writer) throws JSONException {
        try {
            boolean b = false;
            int     len = length();

            writer.write('[');

            for (int i = 0; i < len; i += 1) {
                if (b) {
                    writer.write(',');
                }
                Object v = this.myArrayList.elementAt(i);
                if (v instanceof JSONObject) {
                    ((JSONObject)v).write(writer);
                } else if (v instanceof JSONArray) {
                    ((JSONArray)v).write(writer);
                } else {
                    writer.write(JSONObject.valueToString(v));
                }
                b = true;
            }
            writer.write(']');
            return writer;
        } catch (IOException e) {
           throw new JSONException(e);
        }
    }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163
