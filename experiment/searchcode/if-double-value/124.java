<<<<<<< HEAD
/*
 *   
 *
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package java.lang;

/**
 * The Double class wraps a value of the primitive type
 * <code>double</code> in an object. An object of type
 * <code>Double</code> contains a single field whose type is
 * <code>double</code>.
 * <p>
 * In addition, this class provides several methods for converting a
 * <code>double</code> to a <code>String</code> and a
 * <code>String</code> to a <code>double</code>, as well as other
 * constants and methods useful when dealing with a
 * <code>double</code>.
 *
 * @version 12/17/01 (CLDC 1.1)
 * @since   JDK1.0, CLDC 1.1
 */
public final class Double {

    /**
     * The positive infinity of type <code>double</code>.
     * It is equal to the value returned by
     * <code>Double.longBitsToDouble(0x7ff0000000000000L)</code>.
     */
    public static final double POSITIVE_INFINITY = 1.0 / 0.0;

    /**
     * The negative infinity of type <code>double</code>.
     * It is equal to the value returned by
     * <code>Double.longBitsToDouble(0xfff0000000000000L)</code>.
     */
    public static final double NEGATIVE_INFINITY = -1.0 / 0.0;

    /**
     * A Not-a-Number (NaN) value of type <code>double</code>.
     * It is equal to the value returned by
     * <code>Double.longBitsToDouble(0x7ff8000000000000L)</code>.
     */
    public static final double NaN = 0.0d / 0.0;

    /**
     * The largest positive finite value of type <code>double</code>.
     * It is equal to the value returned by
     * <blockquote><pre>
     * <code>Double.longBitsToDouble(0x7fefffffffffffffL)</code>
     * </pre></blockquote>
     */
    public static final double MAX_VALUE = 1.79769313486231570e+308;

    /**
     * The smallest positive value of type <code>double</code>.
     * It is equal to the value returned by
     * <code>Double.longBitsToDouble(0x1L)</code>.
     */
    public static final double MIN_VALUE = 4.94065645841246544e-324;

    /**
     * Creates a string representation of the <code>double</code>
     * argument. All characters mentioned below are ASCII characters.
     * <ul>
     * <li>If the argument is NaN, the result is the string "NaN".
     * <li>Otherwise, the result is a string that represents the sign and
     * magnitude (absolute value) of the argument. If the sign is negative,
     * the first character of the result is '<code>-</code>'
     * ('<code>\u002d</code>'); if the sign is positive, no sign character
     * appears in the result. As for the magnitude <i>m</i>:
     * <li>If <i>m</i> is infinity, it is represented by the characters
     * <code>"Infinity"</code>; thus, positive infinity produces the result
     * <code>"Infinity"</code> and negative infinity produces the result
     * <code>"-Infinity"</code>.
     * <li>If <i>m</i> is zero, it is represented by the characters
     * <code>"0.0"</code>; thus, negative zero produces the result
     * <code>"-0.0"</code> and positive zero produces the result
     * <code>"0.0"</code>.
     * <li>If <i>m</i> is greater than or equal to 10<sup>-3</sup> but less
     * than 10<sup>7</sup>, then it is represented as the integer part of
     * <i>m</i>, in decimal form with no leading zeroes, followed by
     * <code>'.'</code> (<code>\u002E</code>), followed by one or more decimal
     * digits representing the fractional part of <i>m</i>.
     * <li>If <i>m</i> is less than 10<sup>-3</sup> or not less than
     * 10<sup>7</sup>, then it is represented in so-called "computerized
     * scientific notation." Let <i>n</i> be the unique integer such that
     * 10<sup>n</sup>&lt;=<i>m</i>&lt;10<sup>n+1</sup>; then let <i>a</i> be
     * the mathematically exact quotient of <i>m</i> and 10<sup>n</sup> so
     * that 1&lt;=<i>a</i>&lt;10. The magnitude is then represented as the
     * integer part of <i>a</i>, as a single decimal digit, followed
     * by <code>'.'</code> (<code>\u002E</code>), followed by decimal digits
     * representing the fractional part of <i>a</i>, followed by the letter
     * <code>'E'</code> (<code>\u0045</code>), followed by a representation
     * of <i>n</i> as a decimal integer, as produced by the method
     * {@link Integer#toString(int)}.
     * </ul><p>
     * How many digits must be printed for the fractional part of
     * <i>m</i> or <i>a</i>? There must be at least one digit to represent
     * the fractional part, and beyond that as many, but only as many, more
     * digits as are needed to uniquely distinguish the argument value from
     * adjacent values of type <code>double</code>. That is, suppose that
     * <i>x</i> is the exact mathematical value represented by the decimal
     * representation produced by this method for a finite nonzero argument
     * <i>d</i>. Then <i>d</i> must be the <code>double</code> value nearest
     * to <i>x</i>; or if two <code>double</code> values are equally close
     * to <i>x</i>, then <i>d</i> must be one of them and the least
     * significant bit of the significand of <i>d</i> must be <code>0</code>.
     *
     * @param   d   the <code>double</code> to be converted.
     * @return  a string representation of the argument.
     */
    public static String toString(double d){
        return new FloatingDecimal(d).toJavaFormatString();
    }

    /**
     * Returns a new <code>Double</code> object initialized to the value
     * represented by the specified string. The string <code>s</code> is
     * interpreted as the representation of a floating-point value and a
     * <code>Double</code> object representing that value is created and
     * returned.
     * <p>
     * If <code>s</code> is <code>null</code>, then a
     * <code>NullPointerException</code> is thrown.
     * <p>
     * Leading and trailing whitespace characters in s are ignored. The rest
     * of <code>s</code> should constitute a <i>FloatValue</i> as described
     * by the lexical rule:
     * <blockquote><pre><i>
     * FloatValue:
     *
     *        Sign<sub>opt</sub> FloatingPointLiteral
     * </i></pre></blockquote>
     * where <i>Sign</i> and <i>FloatingPointLiteral</i> are as defined in
     * Section 3.10.2 of the <a href="http://java.sun.com/docs/books/jls/html/">Java
     * Language Specification</a>. If it does not have the form of a
     * <i>FloatValue</i>, then a <code>NumberFormatException</code> is
     * thrown. Otherwise, it is regarded as representing an exact decimal
     * value in the usual "computerized scientific notation"; this exact
     * decimal value is then conceptually converted to an "infinitely
     * precise" binary value that is then rounded to type <code>double</code>
     * by the usual round-to-nearest rule of IEEE 754 floating-point
     * arithmetic. Finally, a new object of class <code>Double</code> is
     * created to represent the <code>double</code> value.
     *
     * @param      s   the string to be parsed.
     * @return     a newly constructed <code>Double</code> initialized to the
     *             value represented by the string argument.
     * @exception  NumberFormatException  if the string does not contain a
     *               parsable number.
     */
    public static Double valueOf(String s) throws NumberFormatException {
        return new Double(FloatingDecimal.readJavaFormatString(s).doubleValue());
    }

    /**
     * Returns a new double initialized to the value represented by the
     * specified <code>String</code>, as performed by the <code>valueOf</code>
     * method of class <code>Double</code>.
     *
     * @param      s   the string to be parsed.
     * @return     the double value represented by the string argument.
     * @exception  NumberFormatException  if the string does not contain a
     *               parsable double.
     * @see        java.lang.Double#valueOf(String)
     * @since      JDK1.2
     */
    public static double parseDouble(String s) throws NumberFormatException {
        return FloatingDecimal.readJavaFormatString(s).doubleValue();
    }

    /**
     * Returns true if the specified number is the special Not-a-Number (NaN)
     * value.
     *
     * @param   v   the value to be tested.
     * @return  <code>true</code> if the value of the argument is NaN;
     *          <code>false</code> otherwise.
     */
    static public boolean isNaN(double v) {
        return (v != v);
    }

    /**
     * Returns true if the specified number is infinitely large in magnitude.
     *
     * @param   v   the value to be tested.
     * @return  <code>true</code> if the value of the argument is positive
     *          infinity or negative infinity; <code>false</code> otherwise.
     */
    static public boolean isInfinite(double v) {
        return (v == POSITIVE_INFINITY) || (v == NEGATIVE_INFINITY);
    }

    /**
     * The value of the Double.
     */
    private double value;

    /**
     * Constructs a newly allocated <code>Double</code> object that
     * represents the primitive <code>double</code> argument.
     *
     * @param   value   the value to be represented by the <code>Double</code>.
     */
    public Double(double value) {
        this.value = value;
    }

    /**
     * Constructs a newly allocated <code>Double</code> object that
     * represents the floating- point value of type <code>double</code>
     * represented by the string. The string is converted to a
     * <code>double</code> value as if by the <code>valueOf</code> method.
     *
     * @param      s   a string to be converted to a <code>Double</code>.
     * @exception  NumberFormatException  if the string does not contain a
     *               parsable number.
     * @see        java.lang.Double#valueOf(java.lang.String)
     */
/* REMOVED from CLDC
    public Double(String s) throws NumberFormatException {
        // IMPL_NOTE: this is inefficient
        this(valueOf(s).doubleValue());
    }
*/

    /**
     * Returns true if this Double value is the special Not-a-Number (NaN)
     * value.
     *
     * @return  <code>true</code> if the value represented by this object is
     *          NaN; <code>false</code> otherwise.
     */
    public boolean isNaN() {
        return isNaN(value);
    }

    /**
     * Returns true if this Double value is infinitely large in magnitude.
     *
     * @return  <code>true</code> if the value represented by this object is
     *          positive infinity or negative infinity;
     *          <code>false</code> otherwise.
     */
    public boolean isInfinite() {
        return isInfinite(value);
    }

    /**
     * Returns a String representation of this Double object.
     * The primitive <code>double</code> value represented by this
     * object is converted to a string exactly as if by the method
     * <code>toString</code> of one argument.
     *
     * @return  a <code>String</code> representation of this object.
     * @see     java.lang.Double#toString(double)
     */
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Returns the value of this Double as a byte (by casting to a byte).
     *
     * @since   JDK1.1
     */
    public byte byteValue() {
        return (byte)value;
    }

    /**
     * Returns the value of this Double as a short (by casting to a short).
     *
     * @since   JDK1.1
     */
    public short shortValue() {
        return (short)value;
    }

    /**
     * Returns the integer value of this Double (by casting to an int).
     *
     * @return  the <code>double</code> value represented by this object is
     *          converted to type <code>int</code> and the result of the
     *          conversion is returned.
     */
    public int intValue() {
        return (int)value;
    }

    /**
     * Returns the long value of this Double (by casting to a long).
     *
     * @return  the <code>double</code> value represented by this object is
     *          converted to type <code>long</code> and the result of the
     *          conversion is returned.
     */
    public long longValue() {
        return (long)value;
    }

    /**
     * Returns the float value of this Double.
     *
     * @return  the <code>double</code> value represented by this object is
     *          converted to type <code>float</code> and the result of the
     *          conversion is returned.
     * @since   JDK1.0
     */
    public float floatValue() {
        return (float)value;
    }

    /**
     * Returns the double value of this Double.
     *
     * @return  the <code>double</code> value represented by this object.
     */
    public double doubleValue() {
        return (double)value;
    }

    /**
     * Returns a hashcode for this <code>Double</code> object. The result
     * is the exclusive OR of the two halves of the long integer bit
     * representation, exactly as produced by the method
     * {@link #doubleToLongBits(double)}, of the primitive
     * <code>double</code> value represented by this <code>Double</code>
     * object. That is, the hashcode is the value of the expression:
     * <blockquote><pre>
     * (int)(v^(v>>>32))
     * </pre></blockquote>
     * where <code>v</code> is defined by:
     * <blockquote><pre>
     * long v = Double.doubleToLongBits(this.doubleValue());
     * </pre></blockquote>
     *
     * @return  a <code>hash code</code> value for this object.
     */
    public int hashCode() {
        long bits = doubleToLongBits(value);
        return (int)(bits ^ (bits >>> 32));
    }

    /**
     * Compares this object against the specified object.
     * The result is <code>true</code> if and only if the argument is
     * not <code>null</code> and is a <code>Double</code> object that
     * represents a double that has the identical bit pattern to the bit
     * pattern of the double represented by this object. For this purpose,
     * two <code>double</code> values are considered to be the same if and
     * only if the method {@link #doubleToLongBits(double)} returns the same
     * long value when applied to each.
     * <p>
     * Note that in most cases, for two instances of class
     * <code>Double</code>, <code>d1</code> and <code>d2</code>, the
     * value of <code>d1.equals(d2)</code> is <code>true</code> if and
     * only if
     * <blockquote><pre>
     *   d1.doubleValue()&nbsp;== d2.doubleValue()
     * </pre></blockquote>
     * <p>
     * also has the value <code>true</code>. However, there are two
     * exceptions:
     * <ul>
     * <li>If <code>d1</code> and <code>d2</code> both represent
     *     <code>Double.NaN</code>, then the <code>equals</code> method
     *     returns <code>true</code>, even though
     *     <code>Double.NaN==Double.NaN</code> has the value
     *     <code>false</code>.
     * <li>If <code>d1</code> represents <code>+0.0</code> while
     *     <code>d2</code> represents <code>-0.0</code>, or vice versa,
     *     the <code>equals</code> test has the value <code>false</code>,
     *     even though <code>+0.0==-0.0</code> has the value <code>true</code>.
     *     This allows hashtables to operate properly.
     * </ul>
     *
     * @param   obj   the object to compare with.
     * @return  <code>true</code> if the objects are the same;
     *          <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        return (obj instanceof Double)
               && (doubleToLongBits(((Double)obj).value) ==
                   doubleToLongBits(value));
    }

    /**
     * Returns a representation of the specified floating-point value
     * according to the IEEE 754 floating-point "double
     * format" bit layout.
     * <p>
     * Bit 63 (the bit that is selected by the mask
     * <code>0x8000000000000000L</code>) represents the sign of the
     * floating-point number. Bits
     * 62-52 (the bits that are selected by the mask
     * <code>0x7ff0000000000000L</code>) represent the exponent. Bits 51-0
     * (the bits that are selected by the mask
     * <code>0x000fffffffffffffL</code>) represent the significand
     * (sometimes called the mantissa) of the floating-point number.
     * <p>
     * If the argument is positive infinity, the result is
     * <code>0x7ff0000000000000L</code>.
     * <p>
     * If the argument is negative infinity, the result is
     * <code>0xfff0000000000000L</code>.
     * <p>
     * If the argument is NaN, the result is
     * <code>0x7ff8000000000000L</code>.
     * <p>
     * In all cases, the result is a <code>long</code> integer that, when
     * given to the {@link #longBitsToDouble(long)} method, will produce a
     * floating-point value equal to the argument to
     * <code>doubleToLongBits</code>.
     *
     * @param   value   a double precision floating-point number.
     * @return  the bits that represent the floating-point number.
     */
    public static native long doubleToLongBits(double value);

    /**
     * Returns a representation of the specified floating-point value
     * according to the IEEE 754 floating-point "double
     * format" bit layout.
     * <p>
     * Bit 63 (the bit that is selected by the mask
     * <code>0x8000000000000000L</code>) represents the sign of the
     * floating-point number. Bits
     * 62-52 (the bits that are selected by the mask
     * <code>0x7ff0000000000000L</code>) represent the exponent. Bits 51-0
     * (the bits that are selected by the mask
     * <code>0x000fffffffffffffL</code>) represent the significand
     * (sometimes called the mantissa) of the floating-point number.
     * <p>
     * If the argument is positive infinity, the result is
     * <code>0x7ff0000000000000L</code>.
     * <p>
     * If the argument is negative infinity, the result is
     * <code>0xfff0000000000000L</code>.
     * <p>
     * If the argument is NaN, the result is the <code>long</code> integer
     * representing the actual NaN value.  Unlike the <code>doubleToLongBits</code>
     * method, <code>doubleToRawLongBits</code> does not collapse NaN values.
     * <p>
     * In all cases, the result is a <code>long</code> integer that, when
     * given to the {@link #longBitsToDouble(long)} method, will produce a
     * floating-point value equal to the argument to
     * <code>doubleToRawLongBits</code>.
     *
     * @param   value   a double precision floating-point number.
     * @return  the bits that represent the floating-point number.
     */
/* REMOVED from CLDC
    public static native long doubleToRawLongBits(double value);
*/

    /**
     * Returns the double-float corresponding to a given bit representation.
     * The argument is considered to be a representation of a
     * floating-point value according to the IEEE 754 floating-point
     * "double precision" bit layout. That floating-point
     * value is returned as the result.
     * <p>
     * If the argument is <code>0x7ff0000000000000L</code>, the result
     * is positive infinity.
     * <p>
     * If the argument is <code>0xfff0000000000000L</code>, the result
     * is negative infinity.
     * <p>
     * If the argument is any value in the range
     * <code>0x7ff0000000000001L</code> through
     * <code>0x7fffffffffffffffL</code> or in the range
     * <code>0xfff0000000000001L</code> through
     * <code>0xffffffffffffffffL</code>, the result is NaN. All IEEE 754
     * NaN values of type <code>double</code> are, in effect, lumped together
     * by the Java programming language into a single value called NaN.
     * <p>
     * In all other cases, let <i>s</i>, <i>e</i>, and <i>m</i> be three
     * values that can be computed from the argument:
     * <blockquote><pre>
     * int s = ((bits >> 63) == 0) ? 1 : -1;
     * int e = (int)((bits >> 52) & 0x7ffL);
     * long m = (e == 0) ?
     *                 (bits & 0xfffffffffffffL) << 1 :
     *                 (bits & 0xfffffffffffffL) | 0x10000000000000L;
     * </pre></blockquote>
     * Then the floating-point result equals the value of the mathematical
     * expression <i>s</i>&#183;<i>m</i>&#183;2<sup>e-1075</sup>.
     *
     * @param   bits   any <code>long</code> integer.
     * @return  the <code>double</code> floating-point value with the same
     *          bit pattern.
     */
    public static native double longBitsToDouble(long bits);

    /**
     * Compares two Doubles numerically.  There are two ways in which
     * comparisons performed by this method differ from those performed
     * by the Java language numerical comparison operators (<code>&lt;, &lt;=,
     * ==, &gt;= &gt;</code>) when applied to primitive doubles:
     * <ul><li>
     *      <code>Double.NaN</code> is considered by this method to be
     *      equal to itself and greater than all other double values
     *      (including <code>Double.POSITIVE_INFINITY</code>).
     * <li>
     *      <code>0.0d</code> is considered by this method to be greater
     *      than <code>-0.0d</code>.
     * </ul>
     * This ensures that Double.compareTo(Object) (which inherits its behavior
     * from this method) obeys the general contract for Comparable.compareTo,
     * and that the <i>natural order</i> on Doubles is <i>total</i>.
     *
     * @param   anotherDouble   the <code>Double</code> to be compared.
     * @return  the value <code>0</code> if <code>anotherDouble</code> is
     *      numerically equal to this Double; a value less than
     *          <code>0</code> if this Double is numerically less than
     *      <code>anotherDouble</code>; and a value greater than
     *      <code>0</code> if this Double is numerically greater than
     *      <code>anotherDouble</code>.
     *
     * @since   JDK1.2
     * @see     Comparable#compareTo(Object)
     */
/* REMOVED from CLDC
    public int compareTo(Double anotherDouble) {
        double thisVal = value;
        double anotherVal = anotherDouble.value;

        if (thisVal < anotherVal)
            return -1;       // Neither val is NaN, thisVal is smaller
        if (thisVal > anotherVal)
            return 1;        // Neither val is NaN, thisVal is larger

        long thisBits = Double.doubleToLongBits(thisVal);
        long anotherBits = Double.doubleToLongBits(anotherVal);

        return (thisBits == anotherBits ?  0 : // Values are equal
                (thisBits < anotherBits ? -1 : // (-0.0, 0.0) or (!NaN, NaN)
                 1));                          // (0.0, -0.0) or (NaN, !NaN)
    }
*/

    /**
     * Compares this Double to another Object.  If the Object is a Double,
     * this function behaves like <code>compareTo(Double)</code>.  Otherwise,
     * it throws a <code>ClassCastException</code> (as Doubles are comparable
     * only to other Doubles).
     *
     * @param   o the <code>Object</code> to be compared.
     * @return  the value <code>0</code> if the argument is a Double
     *      numerically equal to this Double; a value less than
     *      <code>0</code> if the argument is a Double numerically
     *      greater than this Double; and a value greater than
     *      <code>0</code> if the argument is a Double numerically
     *      less than this Double.
     * @exception <code>ClassCastException</code> if the argument is not a
     *        <code>Double</code>.
     * @see     java.lang.Comparable
     * @since   JDK1.2
     */
/* REMOVED from CLDC
    public int compareTo(Object o) {
        return compareTo((Double)o);
    }
*/

=======
package sh.calaba.org.codehaus.jackson.map.deser.std;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import sh.calaba.org.codehaus.jackson.JsonParser;
import sh.calaba.org.codehaus.jackson.JsonProcessingException;
import sh.calaba.org.codehaus.jackson.JsonToken;
import sh.calaba.org.codehaus.jackson.JsonParser.NumberType;
import sh.calaba.org.codehaus.jackson.io.NumberInput;
import sh.calaba.org.codehaus.jackson.map.*;
import sh.calaba.org.codehaus.jackson.map.annotate.JacksonStdImpl;
import sh.calaba.org.codehaus.jackson.type.JavaType;

/**
 * Base class for common deserializers. Contains shared
 * base functionality for dealing with primitive values, such
 * as (re)parsing from String.
 * 
 * @since 1.9 (moved from higher-level package)
 */
public abstract class StdDeserializer<T>
    extends JsonDeserializer<T>
{
    /**
     * Type of values this deserializer handles: sometimes
     * exact types, other time most specific supertype of
     * types deserializer handles (which may be as generic
     * as {@link Object} in some case)
     */
    final protected Class<?> _valueClass;

    protected StdDeserializer(Class<?> vc) {
        _valueClass = vc;
    }

    protected StdDeserializer(JavaType valueType) {
        _valueClass = (valueType == null) ? null : valueType.getRawClass();
    }
    
    /*
    /**********************************************************
    /* Extended API
    /**********************************************************
     */

    public Class<?> getValueClass() { return _valueClass; }

    /**
     * Exact structured type deserializer handles, if known.
     *<p>
     * Default implementation just returns null.
     */
    public JavaType getValueType() { return null; }

    /**
     * Method that can be called to determine if given deserializer is the default
     * deserializer Jackson uses; as opposed to a custom deserializer installed by
     * a module or calling application. Determination is done using
     * {@link JacksonStdImpl} annotation on deserializer class.
     * 
     * @since 1.7
     */
    protected boolean isDefaultSerializer(JsonDeserializer<?> deserializer)
    {
        return (deserializer != null && deserializer.getClass().getAnnotation(JacksonStdImpl.class) != null);
    }
    
    /*
    /**********************************************************
    /* Partial JsonDeserializer implementation 
    /**********************************************************
     */
    
    /**
     * Base implementation that does not assume specific type
     * inclusion mechanism. Sub-classes are expected to override
     * this method if they are to handle type information.
     */
    @Override
    public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt,
            TypeDeserializer typeDeserializer)
        throws IOException, JsonProcessingException
    {
        return typeDeserializer.deserializeTypedFromAny(jp, ctxt);
    }
    
    /*
    /**********************************************************
    /* Helper methods for sub-classes, parsing
    /**********************************************************
     */

    protected final boolean _parseBooleanPrimitive(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_TRUE) {
            return true;
        }
        if (t == JsonToken.VALUE_FALSE) {
            return false;
        }
        if (t == JsonToken.VALUE_NULL) {
            return false;
        }
        // [JACKSON-78]: should accept ints too, (0 == false, otherwise true)
        if (t == JsonToken.VALUE_NUMBER_INT) {
            // 11-Jan-2012, tatus: May be outside of int...
            if (jp.getNumberType() == NumberType.INT) {
                return (jp.getIntValue() != 0);
            }
            return _parseBooleanFromNumber(jp, ctxt);
        }
        // And finally, let's allow Strings to be converted too
        if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();
            if ("true".equals(text)) {
                return true;
            }
            if ("false".equals(text) || text.length() == 0) {
                return Boolean.FALSE;
            }
            throw ctxt.weirdStringException(_valueClass, "only \"true\" or \"false\" recognized");
        }
        // Otherwise, no can do:
        throw ctxt.mappingException(_valueClass, t);
    }

    protected final Boolean _parseBoolean(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_TRUE) {
            return Boolean.TRUE;
        }
        if (t == JsonToken.VALUE_FALSE) {
            return Boolean.FALSE;
        }
        // [JACKSON-78]: should accept ints too, (0 == false, otherwise true)
        if (t == JsonToken.VALUE_NUMBER_INT) {
            // 11-Jan-2012, tatus: May be outside of int...
            if (jp.getNumberType() == NumberType.INT) {
                return (jp.getIntValue() == 0) ? Boolean.FALSE : Boolean.TRUE;
            }
            return Boolean.valueOf(_parseBooleanFromNumber(jp, ctxt));
        }
        if (t == JsonToken.VALUE_NULL) {
            return (Boolean) getNullValue();
        }
        // And finally, let's allow Strings to be converted too
        if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();
            if ("true".equals(text)) {
                return Boolean.TRUE;
            }
            if ("false".equals(text)) {
                return Boolean.FALSE;
            }
            if (text.length() == 0) {
                return (Boolean) getEmptyValue();
            }
            throw ctxt.weirdStringException(_valueClass, "only \"true\" or \"false\" recognized");
        }
        // Otherwise, no can do:
        throw ctxt.mappingException(_valueClass, t);
    }

    protected final boolean _parseBooleanFromNumber(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
    {
        if (jp.getNumberType() == NumberType.LONG) {
            return (jp.getLongValue() == 0L) ? Boolean.FALSE : Boolean.TRUE;
        }
        // no really good logic; let's actually resort to textual comparison
        String str = jp.getText();
        if ("0.0".equals(str) || "0".equals(str)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
    
    protected Byte _parseByte(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) { // coercing should work too
            return jp.getByteValue();
        }
        if (t == JsonToken.VALUE_STRING) { // let's do implicit re-parse
            String text = jp.getText().trim();
            int value;
            try {
                int len = text.length();
                if (len == 0) {
                    return (Byte) getEmptyValue();
                }
                value = NumberInput.parseInt(text);
            } catch (IllegalArgumentException iae) {
                throw ctxt.weirdStringException(_valueClass, "not a valid Byte value");
            }
            // So far so good: but does it fit?
            if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                throw ctxt.weirdStringException(_valueClass, "overflow, value can not be represented as 8-bit value");
            }
            return Byte.valueOf((byte) value);
        }
        if (t == JsonToken.VALUE_NULL) {
            return (Byte) getNullValue();
        }
        throw ctxt.mappingException(_valueClass, t);
    }
    
    protected Short _parseShort(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) { // coercing should work too
            return jp.getShortValue();
        }
        if (t == JsonToken.VALUE_STRING) { // let's do implicit re-parse
            String text = jp.getText().trim();
            int value;
            try {
                int len = text.length();
                if (len == 0) {
                    return (Short) getEmptyValue();
                }
                value = NumberInput.parseInt(text);
            } catch (IllegalArgumentException iae) {
                throw ctxt.weirdStringException(_valueClass, "not a valid Short value");
            }
            // So far so good: but does it fit?
            if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                throw ctxt.weirdStringException(_valueClass, "overflow, value can not be represented as 16-bit value");
            }
            return Short.valueOf((short) value);
        }
        if (t == JsonToken.VALUE_NULL) {
            return (Short) getNullValue();
        }
        throw ctxt.mappingException(_valueClass, t);
    }

    protected final short _parseShortPrimitive(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        int value = _parseIntPrimitive(jp, ctxt);
        // So far so good: but does it fit?
        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            throw ctxt.weirdStringException(_valueClass, "overflow, value can not be represented as 16-bit value");
        }
        return (short) value;
    }
    
    protected final int _parseIntPrimitive(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();

        // Int works as is, coercing fine as well
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) { // coercing should work too
            return jp.getIntValue();
        }
        if (t == JsonToken.VALUE_STRING) { // let's do implicit re-parse
            /* 31-Dec-2009, tatus: Should improve handling of overflow
             *   values... but this'll have to do for now
             */
            String text = jp.getText().trim();
            try {
                int len = text.length();
                if (len > 9) {
                    long l = Long.parseLong(text);
                    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
                        throw ctxt.weirdStringException(_valueClass,
                            "Overflow: numeric value ("+text+") out of range of int ("+Integer.MIN_VALUE+" - "+Integer.MAX_VALUE+")");
                    }
                    return (int) l;
                }
                if (len == 0) {
                    return 0;
                }
                return NumberInput.parseInt(text);
            } catch (IllegalArgumentException iae) {
                throw ctxt.weirdStringException(_valueClass, "not a valid int value");
            }
        }
        if (t == JsonToken.VALUE_NULL) {
            return 0;
        }
        // Otherwise, no can do:
        throw ctxt.mappingException(_valueClass, t);
    }

    protected final Integer _parseInteger(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) { // coercing should work too
            return Integer.valueOf(jp.getIntValue());
        }
        if (t == JsonToken.VALUE_STRING) { // let's do implicit re-parse
            String text = jp.getText().trim();
            try {
                int len = text.length();
                if (len > 9) {
                    long l = Long.parseLong(text);
                    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
                        throw ctxt.weirdStringException(_valueClass,
                            "Overflow: numeric value ("+text+") out of range of Integer ("+Integer.MIN_VALUE+" - "+Integer.MAX_VALUE+")");
                    }
                    return Integer.valueOf((int) l);
                }
                if (len == 0) {
                    return (Integer) getEmptyValue();
                }
                return Integer.valueOf(NumberInput.parseInt(text));
            } catch (IllegalArgumentException iae) {
                throw ctxt.weirdStringException(_valueClass, "not a valid Integer value");
            }
        }
        if (t == JsonToken.VALUE_NULL) {
            return (Integer) getNullValue();
        }
        // Otherwise, no can do:
        throw ctxt.mappingException(_valueClass, t);
    }

    protected final Long _parseLong(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
    
        // it should be ok to coerce (although may fail, too)
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
            return jp.getLongValue();
        }
        // let's allow Strings to be converted too
        if (t == JsonToken.VALUE_STRING) {
            // !!! 05-Jan-2009, tatu: Should we try to limit value space, JDK is too lenient?
            String text = jp.getText().trim();
            if (text.length() == 0) {
                return (Long) getEmptyValue();
            }
            try {
                return Long.valueOf(NumberInput.parseLong(text));
            } catch (IllegalArgumentException iae) { }
            throw ctxt.weirdStringException(_valueClass, "not a valid Long value");
        }
        if (t == JsonToken.VALUE_NULL) {
            return (Long) getNullValue();
        }
        // Otherwise, no can do:
        throw ctxt.mappingException(_valueClass, t);
    }

    protected final long _parseLongPrimitive(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
            return jp.getLongValue();
        }
        if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();
            if (text.length() == 0) {
                return 0L;
            }
            try {
                return NumberInput.parseLong(text);
            } catch (IllegalArgumentException iae) { }
            throw ctxt.weirdStringException(_valueClass, "not a valid long value");
        }
        if (t == JsonToken.VALUE_NULL) {
            return 0L;
        }
        throw ctxt.mappingException(_valueClass, t);
    }
    
    protected final Float _parseFloat(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        // We accept couple of different types; obvious ones first:
        JsonToken t = jp.getCurrentToken();
        
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) { // coercing should work too
            return jp.getFloatValue();
        }
        // And finally, let's allow Strings to be converted too
        if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();
            if (text.length() == 0) {
                return (Float) getEmptyValue();
            }
            switch (text.charAt(0)) {
            case 'I':
                if ("Infinity".equals(text) || "INF".equals(text)) {
                    return Float.POSITIVE_INFINITY;
                }
                break;
            case 'N':
                if ("NaN".equals(text)) {
                    return Float.NaN;
                }
                break;
            case '-':
                if ("-Infinity".equals(text) || "-INF".equals(text)) {
                    return Float.NEGATIVE_INFINITY;
                }
                break;
            }
            try {
                return Float.parseFloat(text);
            } catch (IllegalArgumentException iae) { }
            throw ctxt.weirdStringException(_valueClass, "not a valid Float value");
        }
        if (t == JsonToken.VALUE_NULL) {
            return (Float) getNullValue();
        }
        // Otherwise, no can do:
        throw ctxt.mappingException(_valueClass, t);
    }

    protected final float _parseFloatPrimitive(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
        
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) { // coercing should work too
            return jp.getFloatValue();
        }
        if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();
            if (text.length() == 0) {
                return 0.0f;
            }
            switch (text.charAt(0)) {
            case 'I':
                if ("Infinity".equals(text) || "INF".equals(text)) {
                    return Float.POSITIVE_INFINITY;
                }
                break;
            case 'N':
                if ("NaN".equals(text)) {
                    return Float.NaN;
                }
                break;
            case '-':
                if ("-Infinity".equals(text) || "-INF".equals(text)) {
                    return Float.NEGATIVE_INFINITY;
                }
                break;
            }
            try {
                return Float.parseFloat(text);
            } catch (IllegalArgumentException iae) { }
            throw ctxt.weirdStringException(_valueClass, "not a valid float value");
        }
        if (t == JsonToken.VALUE_NULL) {
            return 0.0f;
        }
        // Otherwise, no can do:
        throw ctxt.mappingException(_valueClass, t);
    }

    protected final Double _parseDouble(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
        
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) { // coercing should work too
            return jp.getDoubleValue();
        }
        if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();
            if (text.length() == 0) {
                return (Double) getEmptyValue();
            }
            switch (text.charAt(0)) {
            case 'I':
                if ("Infinity".equals(text) || "INF".equals(text)) {
                    return Double.POSITIVE_INFINITY;
                }
                break;
            case 'N':
                if ("NaN".equals(text)) {
                    return Double.NaN;
                }
                break;
            case '-':
                if ("-Infinity".equals(text) || "-INF".equals(text)) {
                    return Double.NEGATIVE_INFINITY;
                }
                break;
            }
            try {
                return parseDouble(text);
            } catch (IllegalArgumentException iae) { }
            throw ctxt.weirdStringException(_valueClass, "not a valid Double value");
        }
        if (t == JsonToken.VALUE_NULL) {
            return (Double) getNullValue();
        }
            // Otherwise, no can do:
        throw ctxt.mappingException(_valueClass, t);
    }

    protected final double _parseDoublePrimitive(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        // We accept couple of different types; obvious ones first:
        JsonToken t = jp.getCurrentToken();
        
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) { // coercing should work too
            return jp.getDoubleValue();
        }
        // And finally, let's allow Strings to be converted too
        if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();
            if (text.length() == 0) {
                return 0.0;
            }
            switch (text.charAt(0)) {
            case 'I':
                if ("Infinity".equals(text) || "INF".equals(text)) {
                    return Double.POSITIVE_INFINITY;
                }
                break;
            case 'N':
                if ("NaN".equals(text)) {
                    return Double.NaN;
                }
                break;
            case '-':
                if ("-Infinity".equals(text) || "-INF".equals(text)) {
                    return Double.NEGATIVE_INFINITY;
                }
                break;
            }
            try {
                return parseDouble(text);
            } catch (IllegalArgumentException iae) { }
            throw ctxt.weirdStringException(_valueClass, "not a valid double value");
        }
        if (t == JsonToken.VALUE_NULL) {
            return 0.0;
        }
            // Otherwise, no can do:
        throw ctxt.mappingException(_valueClass, t);
    }

    
    protected java.util.Date _parseDate(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT) {
            return new java.util.Date(jp.getLongValue());
        }
        if (t == JsonToken.VALUE_NULL) {
            return (java.util.Date) getNullValue();
        }
        if (t == JsonToken.VALUE_STRING) {
            try {
                /* As per [JACKSON-203], take empty Strings to mean
                 * null
                 */
                String str = jp.getText().trim();
                if (str.length() == 0) {
                    return (Date) getEmptyValue();
                }
                return ctxt.parseDate(str);
            } catch (IllegalArgumentException iae) {
                throw ctxt.weirdStringException(_valueClass, "not a valid representation (error: "+iae.getMessage()+")");
            }
        }
        throw ctxt.mappingException(_valueClass, t);
    }

    /**
     * Helper method for encapsulating calls to low-level double value parsing; single place
     * just because we need a work-around that must be applied to all calls.
     *<p>
     * Note: copied from <code>sh.calaba.org.codehaus.jackson.io.NumberUtil</code> (to avoid dependency to
     * version 1.8; except for String constants, but that gets compiled in bytecode here)
     */
    protected final static double parseDouble(String numStr) throws NumberFormatException
    {
        // [JACKSON-486]: avoid some nasty float representations... but should it be MIN_NORMAL or MIN_VALUE?
        if (NumberInput.NASTY_SMALL_DOUBLE.equals(numStr)) {
            return 0x1.0p-1022; //Double.MIN_NORMAL; Hardcoded since MIN_NORMAL is not available until SDK level 10
        }
        return Double.parseDouble(numStr);
    }
    
    /*
    /****************************************************
    /* Helper methods for sub-classes, resolving dependencies
    /****************************************************
    */

    /**
     * Helper method used to locate deserializers for properties the
     * type this deserializer handles contains (usually for properties of
     * bean types)
     * 
     * @param config Active deserialization configuration 
     * @param provider Deserializer provider to use for actually finding deserializer(s)
     * @param type Type of property to deserialize
     * @param property Actual property object (field, method, constuctor parameter) used
     *     for passing deserialized values; provided so deserializer can be contextualized if necessary (since 1.7)
     */
    protected JsonDeserializer<Object> findDeserializer(DeserializationConfig config, DeserializerProvider provider,
                                                        JavaType type, BeanProperty property)
        throws JsonMappingException
    {
        JsonDeserializer<Object> deser = provider.findValueDeserializer(config, type, property);
        return deser;
    }

    /*
    /**********************************************************
    /* Helper methods for sub-classes, problem reporting
    /**********************************************************
     */

    /**
     * Method called to deal with a property that did not map to a known
     * Bean property. Method can deal with the problem as it sees fit (ignore,
     * throw exception); but if it does return, it has to skip the matching
     * Json content parser has.
     *<p>
     * NOTE: method signature was changed in version 1.5; explicit JsonParser
     * <b>must</b> be passed since it may be something other than what
     * context has. Prior versions did not include the first parameter.
     *
     * @param jp Parser that points to value of the unknown property
     * @param ctxt Context for deserialization; allows access to the parser,
     *    error reporting functionality
     * @param instanceOrClass Instance that is being populated by this
     *   deserializer, or if not known, Class that would be instantiated.
     *   If null, will assume type is what {@link #getValueClass} returns.
     * @param propName Name of the property that can not be mapped
     */
    protected void handleUnknownProperty(JsonParser jp, DeserializationContext ctxt, Object instanceOrClass, String propName)
        throws IOException, JsonProcessingException
    {
        if (instanceOrClass == null) {
            instanceOrClass = getValueClass();
        }
        // Maybe we have configured handler(s) to take care of it?
        if (ctxt.handleUnknownProperty(jp, this, instanceOrClass, propName)) {
            return;
        }
        // Nope, not handled. Potentially that's a problem...
        reportUnknownProperty(ctxt, instanceOrClass, propName);

        /* If we get this far, need to skip now; we point to first token of
         * value (START_xxx for structured, or the value token for others)
         */
        jp.skipChildren();
    }
        
    protected void reportUnknownProperty(DeserializationContext ctxt,
                                         Object instanceOrClass, String fieldName)
        throws IOException, JsonProcessingException
    {
        // throw exception if that's what we are expected to do
        if (ctxt.isEnabled(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES)) {
            throw ctxt.unknownFieldException(instanceOrClass, fieldName);
        }
        // ... or if not, just ignore
    }


    /*
    /**********************************************************
    /* Then one intermediate base class for things that have
    /* both primitive and wrapper types
    /**********************************************************
     */

    protected abstract static class PrimitiveOrWrapperDeserializer<T>
        extends StdScalarDeserializer<T>
    {
        final T _nullValue;
        
        protected PrimitiveOrWrapperDeserializer(Class<T> vc, T nvl)
        {
            super(vc);
            _nullValue = nvl;
        }
        
        @Override
        public final T getNullValue() {
            return _nullValue;
        }
    }
    
    /*
    /**********************************************************
    /* Then primitive/wrapper types
    /**********************************************************
     */

    @JacksonStdImpl
    public final static class BooleanDeserializer
        extends PrimitiveOrWrapperDeserializer<Boolean>
    {
        public BooleanDeserializer(Class<Boolean> cls, Boolean nvl)
        {
            super(cls, nvl);
        }
        
        @Override
	public Boolean deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            return _parseBoolean(jp, ctxt);
        }

        // 1.6: since we can never have type info ("natural type"; String, Boolean, Integer, Double):
        // (is it an error to even call this version?)
        @Override
        public Boolean deserializeWithType(JsonParser jp, DeserializationContext ctxt,
                TypeDeserializer typeDeserializer)
            throws IOException, JsonProcessingException
        {
            return _parseBoolean(jp, ctxt);
        }
    }

    @JacksonStdImpl
    public final static class ByteDeserializer
        extends PrimitiveOrWrapperDeserializer<Byte>
    {
        public ByteDeserializer(Class<Byte> cls, Byte nvl)
        {
            super(cls, nvl);
        }

        @Override
        public Byte deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            return _parseByte(jp, ctxt);
        }
    }

    @JacksonStdImpl
    public final static class ShortDeserializer
        extends PrimitiveOrWrapperDeserializer<Short>
    {
        public ShortDeserializer(Class<Short> cls, Short nvl)
        {
            super(cls, nvl);
        }

        @Override
        public Short deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            return _parseShort(jp, ctxt);
        }
    }

    @JacksonStdImpl
    public final static class CharacterDeserializer
        extends PrimitiveOrWrapperDeserializer<Character>
    {
        public CharacterDeserializer(Class<Character> cls, Character nvl)
        {
            super(cls, nvl);
        }

        @Override
        public Character deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            JsonToken t = jp.getCurrentToken();
            int value;

            if (t == JsonToken.VALUE_NUMBER_INT) { // ok iff ascii value
                value = jp.getIntValue();
                if (value >= 0 && value <= 0xFFFF) {
                    return Character.valueOf((char) value);
                }
            } else if (t == JsonToken.VALUE_STRING) { // this is the usual type
                // But does it have to be exactly one char?
                String text = jp.getText();
                if (text.length() == 1) {
                    return Character.valueOf(text.charAt(0));
                }
                // actually, empty should become null?
                if (text.length() == 0) {
                    return (Character) getEmptyValue();
                }
            }
            throw ctxt.mappingException(_valueClass, t);
        }
    }

    @JacksonStdImpl
    public final static class IntegerDeserializer
        extends PrimitiveOrWrapperDeserializer<Integer>
    {
        public IntegerDeserializer(Class<Integer> cls, Integer nvl)
        {
            super(cls, nvl);
        }

        @Override
        public Integer deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            return _parseInteger(jp, ctxt);
        }

        // 1.6: since we can never have type info ("natural type"; String, Boolean, Integer, Double):
        // (is it an error to even call this version?)
        @Override
        public Integer deserializeWithType(JsonParser jp, DeserializationContext ctxt,
                TypeDeserializer typeDeserializer)
            throws IOException, JsonProcessingException
        {
            return _parseInteger(jp, ctxt);
        }
    }

    @JacksonStdImpl
    public final static class LongDeserializer
        extends PrimitiveOrWrapperDeserializer<Long>
    {
        public LongDeserializer(Class<Long> cls, Long nvl)
        {
            super(cls, nvl);
        }

        @Override
        public Long deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            return _parseLong(jp, ctxt);
        }
    }

    @JacksonStdImpl
    public final static class FloatDeserializer
        extends PrimitiveOrWrapperDeserializer<Float>
    {
        public FloatDeserializer(Class<Float> cls, Float nvl)
        {
            super(cls, nvl);
        }

        @Override
        public Float deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            /* 22-Jan-2009, tatu: Bounds/range checks would be tricky
             *   here, so let's not bother even trying...
             */
            return _parseFloat(jp, ctxt);
        }
    }

    @JacksonStdImpl
    public final static class DoubleDeserializer
        extends PrimitiveOrWrapperDeserializer<Double>
    {
        public DoubleDeserializer(Class<Double> cls, Double nvl)
        {
            super(cls, nvl);
        }

        @Override
        public Double deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            return _parseDouble(jp, ctxt);
        }

        // 1.6: since we can never have type info ("natural type"; String, Boolean, Integer, Double):
        // (is it an error to even call this version?)
        @Override
        public Double deserializeWithType(JsonParser jp, DeserializationContext ctxt,
                TypeDeserializer typeDeserializer)
            throws IOException, JsonProcessingException
        {
            return _parseDouble(jp, ctxt);
        }
    }

    /**
     * For type <code>Number.class</code>, we can just rely on type
     * mappings that plain {@link JsonParser#getNumberValue} returns.
     *<p>
     * Since 1.5, there is one additional complication: some numeric
     * types (specifically, int/Integer and double/Double) are "non-typed";
     * meaning that they will NEVER be output with type information.
     * But other numeric types may need such type information.
     * This is why {@link #deserializeWithType} must be overridden.
     */
    @JacksonStdImpl
    public final static class NumberDeserializer
        extends StdScalarDeserializer<Number>
    {
        public NumberDeserializer() { super(Number.class); }

        @Override
        public Number deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            JsonToken t = jp.getCurrentToken();
            if (t == JsonToken.VALUE_NUMBER_INT) {
                if (ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_INTEGER_FOR_INTS)) {
                    return jp.getBigIntegerValue();
                }
                return jp.getNumberValue();
            } else if (t == JsonToken.VALUE_NUMBER_FLOAT) {
                /* [JACKSON-72]: need to allow overriding the behavior
                 * regarding which type to use
                 */
                if (ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_DECIMAL_FOR_FLOATS)) {
                    return jp.getDecimalValue();
                }
                return Double.valueOf(jp.getDoubleValue());
            }

            /* Textual values are more difficult... not parsing itself, but figuring
             * out 'minimal' type to use 
             */
            if (t == JsonToken.VALUE_STRING) { // let's do implicit re-parse
                String text = jp.getText().trim();
                try {
                    if (text.indexOf('.') >= 0) { // floating point
                        // as per [JACKSON-72]:
                        if (ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_DECIMAL_FOR_FLOATS)) {
                            return new BigDecimal(text);
                        }
                        return new Double(text);
                    }
                    // as per [JACKSON-100]:
                    if (ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_INTEGER_FOR_INTS)) {
                        return new BigInteger(text);
                    }
                    long value = Long.parseLong(text);
                    if (value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE) {
                        return Integer.valueOf((int) value);
                    }
                    return Long.valueOf(value);
                } catch (IllegalArgumentException iae) {
                    throw ctxt.weirdStringException(_valueClass, "not a valid number");
                }
            }
            // Otherwise, no can do:
            throw ctxt.mappingException(_valueClass, t);
        }

        /**
         * As mentioned in class Javadoc, there is additional complexity in
         * handling potentially mixed type information here. Because of this,
         * we must actually check for "raw" integers and doubles first, before
         * calling type deserializer.
         */
        @Override
        public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt,
                                          TypeDeserializer typeDeserializer)
            throws IOException, JsonProcessingException
        {
            switch (jp.getCurrentToken()) {
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
            case VALUE_STRING:
                // can not point to type information: hence must be non-typed (int/double)
                return deserialize(jp, ctxt);
            }
            return typeDeserializer.deserializeTypedFromScalar(jp, ctxt);
        }
    }
    
    /*
    /**********************************************************
    /* And then bit more complicated (but non-structured) number
    /* types
    /**********************************************************
     */

    @JacksonStdImpl
    public static class BigDecimalDeserializer
        extends StdScalarDeserializer<BigDecimal>
    {
        public BigDecimalDeserializer() { super(BigDecimal.class); }

        @Override
	public BigDecimal deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            JsonToken t = jp.getCurrentToken();
            if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
                return jp.getDecimalValue();
            }
            // String is ok too, can easily convert
            if (t == JsonToken.VALUE_STRING) { // let's do implicit re-parse
                String text = jp.getText().trim();
                if (text.length() == 0) {
                    return null;
                }
                try {
                    return new BigDecimal(text);
                } catch (IllegalArgumentException iae) {
                    throw ctxt.weirdStringException(_valueClass, "not a valid representation");
                }
            }
            // Otherwise, no can do:
            throw ctxt.mappingException(_valueClass, t);
        }
    }

    /**
     * This is bit trickier to implement efficiently, while avoiding
     * overflow problems.
     */
    @JacksonStdImpl
    public static class BigIntegerDeserializer
        extends StdScalarDeserializer<BigInteger>
    {
        public BigIntegerDeserializer() { super(BigInteger.class); }

        @Override
		public BigInteger deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            JsonToken t = jp.getCurrentToken();
            String text;

            if (t == JsonToken.VALUE_NUMBER_INT) {
                switch (jp.getNumberType()) {
                case INT:
                case LONG:
                    return BigInteger.valueOf(jp.getLongValue());
                }
            } else if (t == JsonToken.VALUE_NUMBER_FLOAT) {
                /* Whether to fail if there's non-integer part?
                 * Could do by calling BigDecimal.toBigIntegerExact()
                 */
                return jp.getDecimalValue().toBigInteger();
            } else if (t != JsonToken.VALUE_STRING) { // let's do implicit re-parse
                // String is ok too, can easily convert; otherwise, no can do:
                throw ctxt.mappingException(_valueClass, t);
            }
            text = jp.getText().trim();
            if (text.length() == 0) {
                return null;
            }
            try {
                return new BigInteger(text);
            } catch (IllegalArgumentException iae) {
                throw ctxt.weirdStringException(_valueClass, "not a valid representation");
            }
        }
    }

    /*
    /****************************************************
    /* Then trickier things: Date/Calendar types
    /****************************************************
     */

    /**
     * Compared to plain old {@link java.util.Date}, SQL version is easier
     * to deal with: mostly because it is more limited.
     */
    public static class SqlDateDeserializer
        extends StdScalarDeserializer<java.sql.Date>
    {
        public SqlDateDeserializer() { super(java.sql.Date.class); }

        @Override
        public java.sql.Date deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            Date d = _parseDate(jp, ctxt);
            return (d == null) ? null : new java.sql.Date(d.getTime());
        }
    }

    /*
    /****************************************************
    /* And other oddities
    /****************************************************
    */

    public static class StackTraceElementDeserializer
        extends StdScalarDeserializer<StackTraceElement>
    {
        public StackTraceElementDeserializer() { super(StackTraceElement.class); }

        @Override
        public StackTraceElement deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            JsonToken t = jp.getCurrentToken();
            // Must get an Object
            if (t == JsonToken.START_OBJECT) {
                String className = "", methodName = "", fileName = "";
                int lineNumber = -1;

                while ((t = jp.nextValue()) != JsonToken.END_OBJECT) {
                    String propName = jp.getCurrentName();
                    if ("className".equals(propName)) {
                        className = jp.getText();
                    } else if ("fileName".equals(propName)) {
                        fileName = jp.getText();
                    } else if ("lineNumber".equals(propName)) {
                        if (t.isNumeric()) {
                            lineNumber = jp.getIntValue();
                        } else {
                            throw JsonMappingException.from(jp, "Non-numeric token ("+t+") for property 'lineNumber'");
                        }
                    } else if ("methodName".equals(propName)) {
                        methodName = jp.getText();
                    } else if ("nativeMethod".equals(propName)) {
                        // no setter, not passed via constructor: ignore
                    } else {
                        handleUnknownProperty(jp, ctxt, _valueClass, propName);
                    }
                }
                return new StackTraceElement(className, methodName, fileName, lineNumber);
            }
            throw ctxt.mappingException(_valueClass, t);
        }
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

