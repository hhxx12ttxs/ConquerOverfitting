<<<<<<< HEAD
/*
 * Copyright 1994-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package java.lang;

import sun.misc.FloatingDecimal;
import sun.misc.FpUtils;
import sun.misc.DoubleConsts;

/**
 * The {@code Double} class wraps a value of the primitive type
 * {@code double} in an object. An object of type
 * {@code Double} contains a single field whose type is
 * {@code double}.
 *
 * <p>In addition, this class provides several methods for converting a
 * {@code double} to a {@code String} and a
 * {@code String} to a {@code double}, as well as other
 * constants and methods useful when dealing with a
 * {@code double}.
 *
 * @author  Lee Boynton
 * @author  Arthur van Hoff
 * @author  Joseph D. Darcy
 * @since JDK1.0
 */
public final class Double extends Number implements Comparable<Double> {
    /**
     * A constant holding the positive infinity of type
     * {@code double}. It is equal to the value returned by
     * {@code Double.longBitsToDouble(0x7ff0000000000000L)}.
     */
    public static final double POSITIVE_INFINITY = 1.0 / 0.0;

    /**
     * A constant holding the negative infinity of type
     * {@code double}. It is equal to the value returned by
     * {@code Double.longBitsToDouble(0xfff0000000000000L)}.
     */
    public static final double NEGATIVE_INFINITY = -1.0 / 0.0;

    /**
     * A constant holding a Not-a-Number (NaN) value of type
     * {@code double}. It is equivalent to the value returned by
     * {@code Double.longBitsToDouble(0x7ff8000000000000L)}.
     */
    public static final double NaN = 0.0d / 0.0;

    /**
     * A constant holding the largest positive finite value of type
     * {@code double},
     * (2-2<sup>-52</sup>)&middot;2<sup>1023</sup>.  It is equal to
     * the hexadecimal floating-point literal
     * {@code 0x1.fffffffffffffP+1023} and also equal to
     * {@code Double.longBitsToDouble(0x7fefffffffffffffL)}.
     */
    public static final double MAX_VALUE = 0x1.fffffffffffffP+1023; // 1.7976931348623157e+308

    /**
     * A constant holding the smallest positive normal value of type
     * {@code double}, 2<sup>-1022</sup>.  It is equal to the
     * hexadecimal floating-point literal {@code 0x1.0p-1022} and also
     * equal to {@code Double.longBitsToDouble(0x0010000000000000L)}.
     *
     * @since 1.6
     */
    public static final double MIN_NORMAL = 0x1.0p-1022; // 2.2250738585072014E-308

    /**
     * A constant holding the smallest positive nonzero value of type
     * {@code double}, 2<sup>-1074</sup>. It is equal to the
     * hexadecimal floating-point literal
     * {@code 0x0.0000000000001P-1022} and also equal to
     * {@code Double.longBitsToDouble(0x1L)}.
     */
    public static final double MIN_VALUE = 0x0.0000000000001P-1022; // 4.9e-324

    /**
     * Maximum exponent a finite {@code double} variable may have.
     * It is equal to the value returned by
     * {@code Math.getExponent(Double.MAX_VALUE)}.
     *
     * @since 1.6
     */
    public static final int MAX_EXPONENT = 1023;

    /**
     * Minimum exponent a normalized {@code double} variable may
     * have.  It is equal to the value returned by
     * {@code Math.getExponent(Double.MIN_NORMAL)}.
     *
     * @since 1.6
     */
    public static final int MIN_EXPONENT = -1022;

    /**
     * The number of bits used to represent a {@code double} value.
     *
     * @since 1.5
     */
    public static final int SIZE = 64;

    /**
     * The {@code Class} instance representing the primitive type
     * {@code double}.
     *
     * @since JDK1.1
     */
    public static final Class<Double>   TYPE = (Class<Double>) Class.getPrimitiveClass("double");

    /**
     * Returns a string representation of the {@code double}
     * argument. All characters mentioned below are ASCII characters.
     * <ul>
     * <li>If the argument is NaN, the result is the string
     *     "{@code NaN}".
     * <li>Otherwise, the result is a string that represents the sign and
     * magnitude (absolute value) of the argument. If the sign is negative,
     * the first character of the result is '{@code -}'
     * (<code>'&#92;u002D'</code>); if the sign is positive, no sign character
     * appears in the result. As for the magnitude <i>m</i>:
     * <ul>
     * <li>If <i>m</i> is infinity, it is represented by the characters
     * {@code "Infinity"}; thus, positive infinity produces the result
     * {@code "Infinity"} and negative infinity produces the result
     * {@code "-Infinity"}.
     *
     * <li>If <i>m</i> is zero, it is represented by the characters
     * {@code "0.0"}; thus, negative zero produces the result
     * {@code "-0.0"} and positive zero produces the result
     * {@code "0.0"}.
     *
     * <li>If <i>m</i> is greater than or equal to 10<sup>-3</sup> but less
     * than 10<sup>7</sup>, then it is represented as the integer part of
     * <i>m</i>, in decimal form with no leading zeroes, followed by
     * '{@code .}' (<code>'&#92;u002E'</code>), followed by one or
     * more decimal digits representing the fractional part of <i>m</i>.
     *
     * <li>If <i>m</i> is less than 10<sup>-3</sup> or greater than or
     * equal to 10<sup>7</sup>, then it is represented in so-called
     * "computerized scientific notation." Let <i>n</i> be the unique
     * integer such that 10<sup><i>n</i></sup> &le; <i>m</i> {@literal <}
     * 10<sup><i>n</i>+1</sup>; then let <i>a</i> be the
     * mathematically exact quotient of <i>m</i> and
     * 10<sup><i>n</i></sup> so that 1 &le; <i>a</i> {@literal <} 10. The
     * magnitude is then represented as the integer part of <i>a</i>,
     * as a single decimal digit, followed by '{@code .}'
     * (<code>'&#92;u002E'</code>), followed by decimal digits
     * representing the fractional part of <i>a</i>, followed by the
     * letter '{@code E}' (<code>'&#92;u0045'</code>), followed
     * by a representation of <i>n</i> as a decimal integer, as
     * produced by the method {@link Integer#toString(int)}.
     * </ul>
     * </ul>
     * How many digits must be printed for the fractional part of
     * <i>m</i> or <i>a</i>? There must be at least one digit to represent
     * the fractional part, and beyond that as many, but only as many, more
     * digits as are needed to uniquely distinguish the argument value from
     * adjacent values of type {@code double}. That is, suppose that
     * <i>x</i> is the exact mathematical value represented by the decimal
     * representation produced by this method for a finite nonzero argument
     * <i>d</i>. Then <i>d</i> must be the {@code double} value nearest
     * to <i>x</i>; or if two {@code double} values are equally close
     * to <i>x</i>, then <i>d</i> must be one of them and the least
     * significant bit of the significand of <i>d</i> must be {@code 0}.
     *
     * <p>To create localized string representations of a floating-point
     * value, use subclasses of {@link java.text.NumberFormat}.
     *
     * @param   d   the {@code double} to be converted.
     * @return a string representation of the argument.
     */
    public static String toString(double d) {
        return new FloatingDecimal(d).toJavaFormatString();
    }

    /**
     * Returns a hexadecimal string representation of the
     * {@code double} argument. All characters mentioned below
     * are ASCII characters.
     *
     * <ul>
     * <li>If the argument is NaN, the result is the string
     *     "{@code NaN}".
     * <li>Otherwise, the result is a string that represents the sign
     * and magnitude of the argument. If the sign is negative, the
     * first character of the result is '{@code -}'
     * (<code>'&#92;u002D'</code>); if the sign is positive, no sign
     * character appears in the result. As for the magnitude <i>m</i>:
     *
     * <ul>
     * <li>If <i>m</i> is infinity, it is represented by the string
     * {@code "Infinity"}; thus, positive infinity produces the
     * result {@code "Infinity"} and negative infinity produces
     * the result {@code "-Infinity"}.
     *
     * <li>If <i>m</i> is zero, it is represented by the string
     * {@code "0x0.0p0"}; thus, negative zero produces the result
     * {@code "-0x0.0p0"} and positive zero produces the result
     * {@code "0x0.0p0"}.
     *
     * <li>If <i>m</i> is a {@code double} value with a
     * normalized representation, substrings are used to represent the
     * significand and exponent fields.  The significand is
     * represented by the characters {@code "0x1."}
     * followed by a lowercase hexadecimal representation of the rest
     * of the significand as a fraction.  Trailing zeros in the
     * hexadecimal representation are removed unless all the digits
     * are zero, in which case a single zero is used. Next, the
     * exponent is represented by {@code "p"} followed
     * by a decimal string of the unbiased exponent as if produced by
     * a call to {@link Integer#toString(int) Integer.toString} on the
     * exponent value.
     *
     * <li>If <i>m</i> is a {@code double} value with a subnormal
     * representation, the significand is represented by the
     * characters {@code "0x0."} followed by a
     * hexadecimal representation of the rest of the significand as a
     * fraction.  Trailing zeros in the hexadecimal representation are
     * removed. Next, the exponent is represented by
     * {@code "p-1022"}.  Note that there must be at
     * least one nonzero digit in a subnormal significand.
     *
     * </ul>
     *
     * </ul>
     *
     * <table border>
     * <caption><h3>Examples</h3></caption>
     * <tr><th>Floating-point Value</th><th>Hexadecimal String</th>
     * <tr><td>{@code 1.0}</td> <td>{@code 0x1.0p0}</td>
     * <tr><td>{@code -1.0}</td>        <td>{@code -0x1.0p0}</td>
     * <tr><td>{@code 2.0}</td> <td>{@code 0x1.0p1}</td>
     * <tr><td>{@code 3.0}</td> <td>{@code 0x1.8p1}</td>
     * <tr><td>{@code 0.5}</td> <td>{@code 0x1.0p-1}</td>
     * <tr><td>{@code 0.25}</td>        <td>{@code 0x1.0p-2}</td>
     * <tr><td>{@code Double.MAX_VALUE}</td>
     *     <td>{@code 0x1.fffffffffffffp1023}</td>
     * <tr><td>{@code Minimum Normal Value}</td>
     *     <td>{@code 0x1.0p-1022}</td>
     * <tr><td>{@code Maximum Subnormal Value}</td>
     *     <td>{@code 0x0.fffffffffffffp-1022}</td>
     * <tr><td>{@code Double.MIN_VALUE}</td>
     *     <td>{@code 0x0.0000000000001p-1022}</td>
     * </table>
     * @param   d   the {@code double} to be converted.
     * @return a hex string representation of the argument.
     * @since 1.5
     * @author Joseph D. Darcy
     */
    public static String toHexString(double d) {
        /*
         * Modeled after the "a" conversion specifier in C99, section
         * 7.19.6.1; however, the output of this method is more
         * tightly specified.
         */
        if (!FpUtils.isFinite(d) )
            // For infinity and NaN, use the decimal output.
            return Double.toString(d);
        else {
            // Initialized to maximum size of output.
            StringBuffer answer = new StringBuffer(24);

            if (FpUtils.rawCopySign(1.0, d) == -1.0) // value is negative,
                answer.append("-");                  // so append sign info

            answer.append("0x");

            d = Math.abs(d);

            if(d == 0.0) {
                answer.append("0.0p0");
            }
            else {
                boolean subnormal = (d < DoubleConsts.MIN_NORMAL);

                // Isolate significand bits and OR in a high-order bit
                // so that the string representation has a known
                // length.
                long signifBits = (Double.doubleToLongBits(d)
                                   & DoubleConsts.SIGNIF_BIT_MASK) |
                    0x1000000000000000L;

                // Subnormal values have a 0 implicit bit; normal
                // values have a 1 implicit bit.
                answer.append(subnormal ? "0." : "1.");

                // Isolate the low-order 13 digits of the hex
                // representation.  If all the digits are zero,
                // replace with a single 0; otherwise, remove all
                // trailing zeros.
                String signif = Long.toHexString(signifBits).substring(3,16);
                answer.append(signif.equals("0000000000000") ? // 13 zeros
                              "0":
                              signif.replaceFirst("0{1,12}$", ""));

                // If the value is subnormal, use the E_min exponent
                // value for double; otherwise, extract and report d's
                // exponent (the representation of a subnormal uses
                // E_min -1).
                answer.append("p" + (subnormal ?
                               DoubleConsts.MIN_EXPONENT:
                               FpUtils.getExponent(d) ));
            }
            return answer.toString();
        }
    }

    /**
     * Returns a {@code Double} object holding the
     * {@code double} value represented by the argument string
     * {@code s}.
     *
     * <p>If {@code s} is {@code null}, then a
     * {@code NullPointerException} is thrown.
     *
     * <p>Leading and trailing whitespace characters in {@code s}
     * are ignored.  Whitespace is removed as if by the {@link
     * String#trim} method; that is, both ASCII space and control
     * characters are removed. The rest of {@code s} should
     * constitute a <i>FloatValue</i> as described by the lexical
     * syntax rules:
     *
     * <blockquote>
     * <dl>
     * <dt><i>FloatValue:</i>
     * <dd><i>Sign<sub>opt</sub></i> {@code NaN}
     * <dd><i>Sign<sub>opt</sub></i> {@code Infinity}
     * <dd><i>Sign<sub>opt</sub> FloatingPointLiteral</i>
     * <dd><i>Sign<sub>opt</sub> HexFloatingPointLiteral</i>
     * <dd><i>SignedInteger</i>
     * </dl>
     *
     * <p>
     *
     * <dl>
     * <dt><i>HexFloatingPointLiteral</i>:
     * <dd> <i>HexSignificand BinaryExponent FloatTypeSuffix<sub>opt</sub></i>
     * </dl>
     *
     * <p>
     *
     * <dl>
     * <dt><i>HexSignificand:</i>
     * <dd><i>HexNumeral</i>
     * <dd><i>HexNumeral</i> {@code .}
     * <dd>{@code 0x} <i>HexDigits<sub>opt</sub>
     *     </i>{@code .}<i> HexDigits</i>
     * <dd>{@code 0X}<i> HexDigits<sub>opt</sub>
     *     </i>{@code .} <i>HexDigits</i>
     * </dl>
     *
     * <p>
     *
     * <dl>
     * <dt><i>BinaryExponent:</i>
     * <dd><i>BinaryExponentIndicator SignedInteger</i>
     * </dl>
     *
     * <p>
     *
     * <dl>
     * <dt><i>BinaryExponentIndicator:</i>
     * <dd>{@code p}
     * <dd>{@code P}
     * </dl>
     *
     * </blockquote>
     *
     * where <i>Sign</i>, <i>FloatingPointLiteral</i>,
     * <i>HexNumeral</i>, <i>HexDigits</i>, <i>SignedInteger</i> and
     * <i>FloatTypeSuffix</i> are as defined in the lexical structure
     * sections of the <a
     * href="http://java.sun.com/docs/books/jls/html/">Java Language
     * Specification</a>. If {@code s} does not have the form of
     * a <i>FloatValue</i>, then a {@code NumberFormatException}
     * is thrown. Otherwise, {@code s} is regarded as
     * representing an exact decimal value in the usual
     * "computerized scientific notation" or as an exact
     * hexadecimal value; this exact numerical value is then
     * conceptually converted to an "infinitely precise"
     * binary value that is then rounded to type {@code double}
     * by the usual round-to-nearest rule of IEEE 754 floating-point
     * arithmetic, which includes preserving the sign of a zero
     * value. Finally, a {@code Double} object representing this
     * {@code double} value is returned.
     *
     * <p> To interpret localized string representations of a
     * floating-point value, use subclasses of {@link
     * java.text.NumberFormat}.
     *
     * <p>Note that trailing format specifiers, specifiers that
     * determine the type of a floating-point literal
     * ({@code 1.0f} is a {@code float} value;
     * {@code 1.0d} is a {@code double} value), do
     * <em>not</em> influence the results of this method.  In other
     * words, the numerical value of the input string is converted
     * directly to the target floating-point type.  The two-step
     * sequence of conversions, string to {@code float} followed
     * by {@code float} to {@code double}, is <em>not</em>
     * equivalent to converting a string directly to
     * {@code double}. For example, the {@code float}
     * literal {@code 0.1f} is equal to the {@code double}
     * value {@code 0.10000000149011612}; the {@code float}
     * literal {@code 0.1f} represents a different numerical
     * value than the {@code double} literal
     * {@code 0.1}. (The numerical value 0.1 cannot be exactly
     * represented in a binary floating-point number.)
     *
     * <p>To avoid calling this method on an invalid string and having
     * a {@code NumberFormatException} be thrown, the regular
     * expression below can be used to screen the input string:
     *
     * <code>
     * <pre>
     *  final String Digits     = "(\\p{Digit}+)";
     *  final String HexDigits  = "(\\p{XDigit}+)";
     *  // an exponent is 'e' or 'E' followed by an optionally
     *  // signed decimal integer.
     *  final String Exp        = "[eE][+-]?"+Digits;
     *  final String fpRegex    =
     *      ("[\\x00-\\x20]*"+  // Optional leading "whitespace"
     *       "[+-]?(" + // Optional sign character
     *       "NaN|" +           // "NaN" string
     *       "Infinity|" +      // "Infinity" string
     *
     *       // A decimal floating-point string representing a finite positive
     *       // number without a leading sign has at most five basic pieces:
     *       // Digits . Digits ExponentPart FloatTypeSuffix
     *       //
     *       // Since this method allows integer-only strings as input
     *       // in addition to strings of floating-point literals, the
     *       // two sub-patterns below are simplifications of the grammar
     *       // productions from the Java Language Specification, 2nd
     *       // edition, section 3.10.2.
     *
     *       // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
     *       "((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+
     *
     *       // . Digits ExponentPart_opt FloatTypeSuffix_opt
     *       "(\\.("+Digits+")("+Exp+")?)|"+
     *
     *       // Hexadecimal strings
     *       "((" +
     *        // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
     *        "(0[xX]" + HexDigits + "(\\.)?)|" +
     *
     *        // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
     *        "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +
     *
     *        ")[pP][+-]?" + Digits + "))" +
     *       "[fFdD]?))" +
     *       "[\\x00-\\x20]*");// Optional trailing "whitespace"
     *
     *  if (Pattern.matches(fpRegex, myString))
     *      Double.valueOf(myString); // Will not throw NumberFormatException
     *  else {
     *      // Perform suitable alternative action
     *  }
     * </pre>
     * </code>
     *
     * @param      s   the string to be parsed.
     * @return     a {@code Double} object holding the value
     *             represented by the {@code String} argument.
     * @throws     NumberFormatException  if the string does not contain a
     *             parsable number.
     */
    public static Double valueOf(String s) throws NumberFormatException {
        return new Double(FloatingDecimal.readJavaFormatString(s).doubleValue());
    }

    /**
     * Returns a {@code Double} instance representing the specified
     * {@code double} value.
     * If a new {@code Double} instance is not required, this method
     * should generally be used in preference to the constructor
     * {@link #Double(double)}, as this method is likely to yield
     * significantly better space and time performance by caching
     * frequently requested values.
     *
     * @param  d a double value.
     * @return a {@code Double} instance representing {@code d}.
     * @since  1.5
     */
    public static Double valueOf(double d) {
        return new Double(d);
    }

    /**
     * Returns a new {@code double} initialized to the value
     * represented by the specified {@code String}, as performed
     * by the {@code valueOf} method of class
     * {@code Double}.
     *
     * @param  s   the string to be parsed.
     * @return the {@code double} value represented by the string
     *         argument.
     * @throws NumberFormatException if the string does not contain
     *         a parsable {@code double}.
     * @see    java.lang.Double#valueOf(String)
     * @since 1.2
     */
    public static double parseDouble(String s) throws NumberFormatException {
        return FloatingDecimal.readJavaFormatString(s).doubleValue();
    }

    /**
     * Returns {@code true} if the specified number is a
     * Not-a-Number (NaN) value, {@code false} otherwise.
     *
     * @param   v   the value to be tested.
     * @return  {@code true} if the value of the argument is NaN;
     *          {@code false} otherwise.
     */
    static public boolean isNaN(double v) {
        return (v != v);
    }

    /**
     * Returns {@code true} if the specified number is infinitely
     * large in magnitude, {@code false} otherwise.
     *
     * @param   v   the value to be tested.
     * @return  {@code true} if the value of the argument is positive
     *          infinity or negative infinity; {@code false} otherwise.
     */
    static public boolean isInfinite(double v) {
        return (v == POSITIVE_INFINITY) || (v == NEGATIVE_INFINITY);
    }

    /**
     * The value of the Double.
     *
     * @serial
     */
    private final double value;

    /**
     * Constructs a newly allocated {@code Double} object that
     * represents the primitive {@code double} argument.
     *
     * @param   value   the value to be represented by the {@code Double}.
     */
    public Double(double value) {
        this.value = value;
    }

    /**
     * Constructs a newly allocated {@code Double} object that
     * represents the floating-point value of type {@code double}
     * represented by the string. The string is converted to a
     * {@code double} value as if by the {@code valueOf} method.
     *
     * @param  s  a string to be converted to a {@code Double}.
     * @throws    NumberFormatException  if the string does not contain a
     *            parsable number.
     * @see       java.lang.Double#valueOf(java.lang.String)
     */
    public Double(String s) throws NumberFormatException {
        // REMIND: this is inefficient
        this(valueOf(s).doubleValue());
    }

    /**
     * Returns {@code true} if this {@code Double} value is
     * a Not-a-Number (NaN), {@code false} otherwise.
     *
     * @return  {@code true} if the value represented by this object is
     *          NaN; {@code false} otherwise.
     */
    public boolean isNaN() {
        return isNaN(value);
    }

    /**
     * Returns {@code true} if this {@code Double} value is
     * infinitely large in magnitude, {@code false} otherwise.
     *
     * @return  {@code true} if the value represented by this object is
     *          positive infinity or negative infinity;
     *          {@code false} otherwise.
     */
    public boolean isInfinite() {
        return isInfinite(value);
    }

    /**
     * Returns a string representation of this {@code Double} object.
     * The primitive {@code double} value represented by this
     * object is converted to a string exactly as if by the method
     * {@code toString} of one argument.
     *
     * @return  a {@code String} representation of this object.
     * @see java.lang.Double#toString(double)
     */
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Returns the value of this {@code Double} as a {@code byte} (by
     * casting to a {@code byte}).
     *
     * @return  the {@code double} value represented by this object
     *          converted to type {@code byte}
     * @since JDK1.1
     */
    public byte byteValue() {
        return (byte)value;
    }

    /**
     * Returns the value of this {@code Double} as a
     * {@code short} (by casting to a {@code short}).
     *
     * @return  the {@code double} value represented by this object
     *          converted to type {@code short}
     * @since JDK1.1
     */
    public short shortValue() {
        return (short)value;
    }

    /**
     * Returns the value of this {@code Double} as an
     * {@code int} (by casting to type {@code int}).
     *
     * @return  the {@code double} value represented by this object
     *          converted to type {@code int}
     */
    public int intValue() {
        return (int)value;
    }

    /**
     * Returns the value of this {@code Double} as a
     * {@code long} (by casting to type {@code long}).
     *
     * @return  the {@code double} value represented by this object
     *          converted to type {@code long}
     */
    public long longValue() {
        return (long)value;
    }

    /**
     * Returns the {@code float} value of this
     * {@code Double} object.
     *
     * @return  the {@code double} value represented by this object
     *          converted to type {@code float}
     * @since JDK1.0
     */
    public float floatValue() {
        return (float)value;
    }

    /**
     * Returns the {@code double} value of this
     * {@code Double} object.
     *
     * @return the {@code double} value represented by this object
     */
    public double doubleValue() {
        return (double)value;
    }

    /**
     * Returns a hash code for this {@code Double} object. The
     * result is the exclusive OR of the two halves of the
     * {@code long} integer bit representation, exactly as
     * produced by the method {@link #doubleToLongBits(double)}, of
     * the primitive {@code double} value represented by this
     * {@code Double} object. That is, the hash code is the value
     * of the expression:
     *
     * <blockquote>
     *  {@code (int)(v^(v>>>32))}
     * </blockquote>
     *
     * where {@code v} is defined by:
     *
     * <blockquote>
     *  {@code long v = Double.doubleToLongBits(this.doubleValue());}
     * </blockquote>
     *
     * @return  a {@code hash code} value for this object.
     */
    public int hashCode() {
        long bits = doubleToLongBits(value);
        return (int)(bits ^ (bits >>> 32));
    }

    /**
     * Compares this object against the specified object.  The result
     * is {@code true} if and only if the argument is not
     * {@code null} and is a {@code Double} object that
     * represents a {@code double} that has the same value as the
     * {@code double} represented by this object. For this
     * purpose, two {@code double} values are considered to be
     * the same if and only if the method {@link
     * #doubleToLongBits(double)} returns the identical
     * {@code long} value when applied to each.
     *
     * <p>Note that in most cases, for two instances of class
     * {@code Double}, {@code d1} and {@code d2}, the
     * value of {@code d1.equals(d2)} is {@code true} if and
     * only if
     *
     * <blockquote>
     *  {@code d1.doubleValue() == d2.doubleValue()}
     * </blockquote>
     *
     * <p>also has the value {@code true}. However, there are two
     * exceptions:
     * <ul>
     * <li>If {@code d1} and {@code d2} both represent
     *     {@code Double.NaN}, then the {@code equals} method
     *     returns {@code true}, even though
     *     {@code Double.NaN==Double.NaN} has the value
     *     {@code false}.
     * <li>If {@code d1} represents {@code +0.0} while
     *     {@code d2} represents {@code -0.0}, or vice versa,
     *     the {@code equal} test has the value {@code false},
     *     even though {@code +0.0==-0.0} has the value {@code true}.
     * </ul>
     * This definition allows hash tables to operate properly.
     * @param   obj   the object to compare with.
     * @return  {@code true} if the objects are the same;
     *          {@code false} otherwise.
     * @see java.lang.Double#doubleToLongBits(double)
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
     *
     * <p>Bit 63 (the bit that is selected by the mask
     * {@code 0x8000000000000000L}) represents the sign of the
     * floating-point number. Bits
     * 62-52 (the bits that are selected by the mask
     * {@code 0x7ff0000000000000L}) represent the exponent. Bits 51-0
     * (the bits that are selected by the mask
     * {@code 0x000fffffffffffffL}) represent the significand
     * (sometimes called the mantissa) of the floating-point number.
     *
     * <p>If the argument is positive infinity, the result is
     * {@code 0x7ff0000000000000L}.
     *
     * <p>If the argument is negative infinity, the result is
     * {@code 0xfff0000000000000L}.
     *
     * <p>If the argument is NaN, the result is
     * {@code 0x7ff8000000000000L}.
     *
     * <p>In all cases, the result is a {@code long} integer that, when
     * given to the {@link #longBitsToDouble(long)} method, will produce a
     * floating-point value the same as the argument to
     * {@code doubleToLongBits} (except all NaN values are
     * collapsed to a single "canonical" NaN value).
     *
     * @param   value   a {@code double} precision floating-point number.
     * @return the bits that represent the floating-point number.
     */
    public static long doubleToLongBits(double value) {
        long result = doubleToRawLongBits(value);
        // Check for NaN based on values of bit fields, maximum
        // exponent and nonzero significand.
        if ( ((result & DoubleConsts.EXP_BIT_MASK) ==
              DoubleConsts.EXP_BIT_MASK) &&
             (result & DoubleConsts.SIGNIF_BIT_MASK) != 0L)
            result = 0x7ff8000000000000L;
        return result;
    }

    /**
     * Returns a representation of the specified floating-point value
     * according to the IEEE 754 floating-point "double
     * format" bit layout, preserving Not-a-Number (NaN) values.
     *
     * <p>Bit 63 (the bit that is selected by the mask
     * {@code 0x8000000000000000L}) represents the sign of the
     * floating-point number. Bits
     * 62-52 (the bits that are selected by the mask
     * {@code 0x7ff0000000000000L}) represent the exponent. Bits 51-0
     * (the bits that are selected by the mask
     * {@code 0x000fffffffffffffL}) represent the significand
     * (sometimes called the mantissa) of the floating-point number.
     *
     * <p>If the argument is positive infinity, the result is
     * {@code 0x7ff0000000000000L}.
     *
     * <p>If the argument is negative infinity, the result is
     * {@code 0xfff0000000000000L}.
     *
     * <p>If the argument is NaN, the result is the {@code long}
     * integer representing the actual NaN value.  Unlike the
     * {@code doubleToLongBits} method,
     * {@code doubleToRawLongBits} does not collapse all the bit
     * patterns encoding a NaN to a single "canonical" NaN
     * value.
     *
     * <p>In all cases, the result is a {@code long} integer that,
     * when given to the {@link #longBitsToDouble(long)} method, will
     * produce a floating-point value the same as the argument to
     * {@code doubleToRawLongBits}.
     *
     * @param   value   a {@code double} precision floating-point number.
     * @return the bits that represent the floating-point number.
     * @since 1.3
     */
    public static native long doubleToRawLongBits(double value);

    /**
     * Returns the {@code double} value corresponding to a given
     * bit representation.
     * The argument is considered to be a representation of a
     * floating-point value according to the IEEE 754 floating-point
     * "double format" bit layout.
     *
     * <p>If the argument is {@code 0x7ff0000000000000L}, the result
     * is positive infinity.
     *
     * <p>If the argument is {@code 0xfff0000000000000L}, the result
     * is negative infinity.
     *
     * <p>If the argument is any value in the range
     * {@code 0x7ff0000000000001L} through
     * {@code 0x7fffffffffffffffL} or in the range
     * {@code 0xfff0000000000001L} through
     * {@code 0xffffffffffffffffL}, the result is a NaN.  No IEEE
     * 754 floating-point operation provided by Java can distinguish
     * between two NaN values of the same type with different bit
     * patterns.  Distinct values of NaN are only distinguishable by
     * use of the {@code Double.doubleToRawLongBits} method.
     *
     * <p>In all other cases, let <i>s</i>, <i>e</i>, and <i>m</i> be three
     * values that can be computed from the argument:
     *
     * <blockquote><pre>
     * int s = ((bits &gt;&gt; 63) == 0) ? 1 : -1;
     * int e = (int)((bits &gt;&gt; 52) & 0x7ffL);
     * long m = (e == 0) ?
     *                 (bits & 0xfffffffffffffL) &lt;&lt; 1 :
     *                 (bits & 0xfffffffffffffL) | 0x10000000000000L;
     * </pre></blockquote>
     *
     * Then the floating-point result equals the value of the mathematical
     * expression <i>s</i>&middot;<i>m</i>&middot;2<sup><i>e</i>-1075</sup>.
     *
     * <p>Note that this method may not be able to return a
     * {@code double} NaN with exactly same bit pattern as the
     * {@code long} argument.  IEEE 754 distinguishes between two
     * kinds of NaNs, quiet NaNs and <i>signaling NaNs</i>.  The
     * differences between the two kinds of NaN are generally not
     * visible in Java.  Arithmetic operations on signaling NaNs turn
     * them into quiet NaNs with a different, but often similar, bit
     * pattern.  However, on some processors merely copying a
     * signaling NaN also performs that conversion.  In particular,
     * copying a signaling NaN to return it to the calling method
     * may perform this conversion.  So {@code longBitsToDouble}
     * may not be able to return a {@code double} with a
     * signaling NaN bit pattern.  Consequently, for some
     * {@code long} values,
     * {@code doubleToRawLongBits(longBitsToDouble(start))} may
     * <i>not</i> equal {@code start}.  Moreover, which
     * particular bit patterns represent signaling NaNs is platform
     * dependent; although all NaN bit patterns, quiet or signaling,
     * must be in the NaN range identified above.
     *
     * @param   bits   any {@code long} integer.
     * @return  the {@code double} floating-point value with the same
     *          bit pattern.
     */
    public static native double longBitsToDouble(long bits);

    /**
     * Compares two {@code Double} objects numerically.  There
     * are two ways in which comparisons performed by this method
     * differ from those performed by the Java language numerical
     * comparison operators ({@code <, <=, ==, >=, >})
     * when applied to primitive {@code double} values:
     * <ul><li>
     *          {@code Double.NaN} is considered by this method
     *          to be equal to itself and greater than all other
     *          {@code double} values (including
     *          {@code Double.POSITIVE_INFINITY}).
     * <li>
     *          {@code 0.0d} is considered by this method to be greater
     *          than {@code -0.0d}.
     * </ul>
     * This ensures that the <i>natural ordering</i> of
     * {@code Double} objects imposed by this method is <i>consistent
     * with equals</i>.
     *
     * @param   anotherDouble   the {@code Double} to be compared.
     * @return  the value {@code 0} if {@code anotherDouble} is
     *          numerically equal to this {@code Double}; a value
     *          less than {@code 0} if this {@code Double}
     *          is numerically less than {@code anotherDouble};
     *          and a value greater than {@code 0} if this
     *          {@code Double} is numerically greater than
     *          {@code anotherDouble}.
     *
     * @since   1.2
     */
    public int compareTo(Double anotherDouble) {
        return Double.compare(value, anotherDouble.value);
    }

    /**
     * Compares the two specified {@code double} values. The sign
     * of the integer value returned is the same as that of the
     * integer that would be returned by the call:
     * <pre>
     *    new Double(d1).compareTo(new Double(d2))
     * </pre>
     *
     * @param   d1        the first {@code double} to compare
     * @param   d2        the second {@code double} to compare
     * @return  the value {@code 0} if {@code d1} is
     *          numerically equal to {@code d2}; a value less than
     *          {@code 0} if {@code d1} is numerically less than
     *          {@code d2}; and a value greater than {@code 0}
     *          if {@code d1} is numerically greater than
     *          {@code d2}.
     * @since 1.4
     */
    public static int compare(double d1, double d2) {
        if (d1 < d2)
            return -1;           // Neither val is NaN, thisVal is smaller
        if (d1 > d2)
            return 1;            // Neither val is NaN, thisVal is larger

        long thisBits = Double.doubleToLongBits(d1);
        long anotherBits = Double.doubleToLongBits(d2);

        return (thisBits == anotherBits ?  0 : // Values are equal
                (thisBits < anotherBits ? -1 : // (-0.0, 0.0) or (!NaN, NaN)
                 1));                          // (0.0, -0.0) or (NaN, !NaN)
    }

    /** use serialVersionUID from JDK 1.0.2 for interoperability */
    private static final long serialVersionUID = -9172774392245257468L;
=======
package sh.calaba.org.codehaus.jackson.impl;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;

import sh.calaba.org.codehaus.jackson.*;
import sh.calaba.org.codehaus.jackson.io.IOContext;
import sh.calaba.org.codehaus.jackson.io.NumberInput;
import sh.calaba.org.codehaus.jackson.util.ByteArrayBuilder;
import sh.calaba.org.codehaus.jackson.util.TextBuffer;
import sh.calaba.org.codehaus.jackson.util.VersionUtil;

/**
 * Intermediate base class used by all Jackson {@link JsonParser}
 * implementations. Contains most common things that are independent
 * of actual underlying input source
 *
 * @author Tatu Saloranta
 */
public abstract class JsonParserBase
    extends JsonParserMinimalBase
{
    /*
    /**********************************************************
    /* Generic I/O state
    /**********************************************************
     */

    /**
     * I/O context for this reader. It handles buffer allocation
     * for the reader.
     */
    final protected IOContext _ioContext;

    /**
     * Flag that indicates whether parser is closed or not. Gets
     * set when parser is either closed by explicit call
     * ({@link #close}) or when end-of-input is reached.
     */
    protected boolean _closed;

    /*
    /**********************************************************
    /* Current input data
    /**********************************************************
     */

    // Note: type of actual buffer depends on sub-class, can't include

    /**
     * Pointer to next available character in buffer
     */
    protected int _inputPtr = 0;

    /**
     * Index of character after last available one in the buffer.
     */
    protected int _inputEnd = 0;

    /*
    /**********************************************************
    /* Current input location information
    /**********************************************************
     */

    /**
     * Number of characters/bytes that were contained in previous blocks
     * (blocks that were already processed prior to the current buffer).
     */
    protected long _currInputProcessed = 0L;

    /**
     * Current row location of current point in input buffer, starting
     * from 1, if available.
     */
    protected int _currInputRow = 1;

    /**
     * Current index of the first character of the current row in input
     * buffer. Needed to calculate column position, if necessary; benefit
     * of not having column itself is that this only has to be updated
     * once per line.
     */
    protected int _currInputRowStart = 0;

    /*
    /**********************************************************
    /* Information about starting location of event
    /* Reader is pointing to; updated on-demand
    /**********************************************************
     */

    // // // Location info at point when current token was started

    /**
     * Total number of bytes/characters read before start of current token.
     * For big (gigabyte-sized) sizes are possible, needs to be long,
     * unlike pointers and sizes related to in-memory buffers.
     */
    protected long _tokenInputTotal = 0; 

    /**
     * Input row on which current token starts, 1-based
     */
    protected int _tokenInputRow = 1;

    /**
     * Column on input row that current token starts; 0-based (although
     * in the end it'll be converted to 1-based)
     */
    protected int _tokenInputCol = 0;

    /*
    /**********************************************************
    /* Parsing state
    /**********************************************************
     */

    /**
     * Information about parser context, context in which
     * the next token is to be parsed (root, array, object).
     */
    protected JsonReadContext _parsingContext;

    /**
     * Secondary token related to the next token after current one;
     * used if its type is known. This may be value token that
     * follows FIELD_NAME, for example.
     */
    protected JsonToken _nextToken;

    /*
    /**********************************************************
    /* Buffer(s) for local name(s) and text content
    /**********************************************************
     */

    /**
     * Buffer that contains contents of String values, including
     * field names if necessary (name split across boundary,
     * contains escape sequence, or access needed to char array)
     */
    protected final TextBuffer _textBuffer;

    /**
     * Temporary buffer that is needed if field name is accessed
     * using {@link #getTextCharacters} method (instead of String
     * returning alternatives)
     */
    protected char[] _nameCopyBuffer = null;

    /**
     * Flag set to indicate whether the field name is available
     * from the name copy buffer or not (in addition to its String
     * representation  being available via read context)
     */
    protected boolean _nameCopied = false;

    /**
     * ByteArrayBuilder is needed if 'getBinaryValue' is called. If so,
     * we better reuse it for remainder of content.
     */
    protected ByteArrayBuilder _byteArrayBuilder = null;

    /**
     * We will hold on to decoded binary data, for duration of
     * current event, so that multiple calls to
     * {@link #getBinaryValue} will not need to decode data more
     * than once.
     */
    protected byte[] _binaryValue;

    /*
    /**********************************************************
    /* Constants and fields of former 'JsonNumericParserBase'
    /**********************************************************
     */

    final protected static int NR_UNKNOWN = 0;

    // First, integer types

    final protected static int NR_INT = 0x0001;
    final protected static int NR_LONG = 0x0002;
    final protected static int NR_BIGINT = 0x0004;

    // And then floating point types

    final protected static int NR_DOUBLE = 0x008;
    final protected static int NR_BIGDECIMAL = 0x0010;

    // Also, we need some numeric constants

    final static BigDecimal BD_MIN_LONG = new BigDecimal(Long.MIN_VALUE);
    final static BigDecimal BD_MAX_LONG = new BigDecimal(Long.MAX_VALUE);

    final static BigDecimal BD_MIN_INT = new BigDecimal(Long.MIN_VALUE);
    final static BigDecimal BD_MAX_INT = new BigDecimal(Long.MAX_VALUE);

    final static long MIN_INT_L = (long) Integer.MIN_VALUE;
    final static long MAX_INT_L = (long) Integer.MAX_VALUE;

    // These are not very accurate, but have to do... (for bounds checks)

    final static double MIN_LONG_D = (double) Long.MIN_VALUE;
    final static double MAX_LONG_D = (double) Long.MAX_VALUE;

    final static double MIN_INT_D = (double) Integer.MIN_VALUE;
    final static double MAX_INT_D = (double) Integer.MAX_VALUE;
    
    
    // Digits, numeric
    final protected static int INT_0 = '0';
    final protected static int INT_1 = '1';
    final protected static int INT_2 = '2';
    final protected static int INT_3 = '3';
    final protected static int INT_4 = '4';
    final protected static int INT_5 = '5';
    final protected static int INT_6 = '6';
    final protected static int INT_7 = '7';
    final protected static int INT_8 = '8';
    final protected static int INT_9 = '9';

    final protected static int INT_MINUS = '-';
    final protected static int INT_PLUS = '+';
    final protected static int INT_DECIMAL_POINT = '.';

    final protected static int INT_e = 'e';
    final protected static int INT_E = 'E';

    final protected static char CHAR_NULL = '\0';
    
    // Numeric value holders: multiple fields used for
    // for efficiency

    /**
     * Bitfield that indicates which numeric representations
     * have been calculated for the current type
     */
    protected int _numTypesValid = NR_UNKNOWN;

    // First primitives

    protected int _numberInt;

    protected long _numberLong;

    protected double _numberDouble;

    // And then object types

    protected BigInteger _numberBigInt;

    protected BigDecimal _numberBigDecimal;

    // And then other information about value itself

    /**
     * Flag that indicates whether numeric value has a negative
     * value. That is, whether its textual representation starts
     * with minus character.
     */
    protected boolean _numberNegative;

    /**
     * Length of integer part of the number, in characters
     */
    protected int _intLength;

    /**
     * Length of the fractional part (not including decimal
     * point or exponent), in characters.
     * Not used for  pure integer values.
     */
    protected int _fractLength;

    /**
     * Length of the exponent part of the number, if any, not
     * including 'e' marker or sign, just digits. 
     * Not used for  pure integer values.
     */
    protected int _expLength;
    
    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    protected JsonParserBase(IOContext ctxt, int features)
    {
        super();
        _features = features;
        _ioContext = ctxt;
        _textBuffer = ctxt.constructTextBuffer();
        _parsingContext = JsonReadContext.createRootContext();
    }
    
    @Override
    public Version version() {
        return VersionUtil.versionFor(getClass());
    }
    
    /*
    /**********************************************************
    /* JsonParser impl
    /**********************************************************
     */
    
    /**
     * Method that can be called to get the name associated with
     * the current event.
     */
    @Override
    public String getCurrentName()
        throws IOException, JsonParseException
    {
        // [JACKSON-395]: start markers require information from parent
        if (_currToken == JsonToken.START_OBJECT || _currToken == JsonToken.START_ARRAY) {
            JsonReadContext parent = _parsingContext.getParent();
            return parent.getCurrentName();
        }
        return _parsingContext.getCurrentName();
    }

    @Override
    public void close() throws IOException
    {
        if (!_closed) {
            _closed = true;
            try {
                _closeInput();
            } finally {
                // as per [JACKSON-324], do in finally block
                // Also, internal buffer(s) can now be released as well
                _releaseBuffers();
            }
        }
    }

    @Override
    public boolean isClosed() { return _closed; }

    @Override
    public JsonReadContext getParsingContext()
    {
        return _parsingContext;
    }

    /**
     * Method that return the <b>starting</b> location of the current
     * token; that is, position of the first character from input
     * that starts the current token.
     */
    @Override
    public JsonLocation getTokenLocation()
    {
        return new JsonLocation(_ioContext.getSourceReference(),
                                getTokenCharacterOffset(),
                                getTokenLineNr(),
                                getTokenColumnNr());
    }

    /**
     * Method that returns location of the last processed character;
     * usually for error reporting purposes
     */
    @Override
    public JsonLocation getCurrentLocation()
    {
        int col = _inputPtr - _currInputRowStart + 1; // 1-based
        return new JsonLocation(_ioContext.getSourceReference(),
                                _currInputProcessed + _inputPtr - 1,
                                _currInputRow, col);
    }

    /*
    /**********************************************************
    /* Public API, access to token information, text
    /**********************************************************
     */

    @Override
    public boolean hasTextCharacters()
    {
        if (_currToken == JsonToken.VALUE_STRING) {
            return true; // usually true
        }        
        if (_currToken == JsonToken.FIELD_NAME) {
            return _nameCopied;
        }
        return false;
    }
    
    /*
    /**********************************************************
    /* Public low-level accessors
    /**********************************************************
     */

    public final long getTokenCharacterOffset() { return _tokenInputTotal; }
    public final int getTokenLineNr() { return _tokenInputRow; }
    public final int getTokenColumnNr() {
        // note: value of -1 means "not available"; otherwise convert from 0-based to 1-based
        int col = _tokenInputCol;
        return (col < 0) ? col : (col + 1);
    }

    /*
    /**********************************************************
    /* Low-level reading, other
    /**********************************************************
     */

    protected final void loadMoreGuaranteed()
        throws IOException
    {
        if (!loadMore()) {
            _reportInvalidEOF();
        }
    }
    
    /*
    /**********************************************************
    /* Abstract methods needed from sub-classes
    /**********************************************************
     */

    protected abstract boolean loadMore() throws IOException;
    
    protected abstract void _finishString() throws IOException, JsonParseException;

    protected abstract void _closeInput() throws IOException;
    
    /*
    /**********************************************************
    /* Low-level reading, other
    /**********************************************************
     */

    /**
     * Method called to release internal buffers owned by the base
     * reader. This may be called along with {@link #_closeInput} (for
     * example, when explicitly closing this reader instance), or
     * separately (if need be).
     */
    protected void _releaseBuffers() throws IOException
    {
        _textBuffer.releaseBuffers();
        char[] buf = _nameCopyBuffer;
        if (buf != null) {
            _nameCopyBuffer = null;
            _ioContext.releaseNameCopyBuffer(buf);
        }
    }
    
    /**
     * Method called when an EOF is encountered between tokens.
     * If so, it may be a legitimate EOF, but only iff there
     * is no open non-root context.
     */
    @Override
    protected void _handleEOF() throws JsonParseException
    {
        if (!_parsingContext.inRoot()) {
            _reportInvalidEOF(": expected close marker for "+_parsingContext.getTypeDesc()+" (from "+_parsingContext.getStartLocation(_ioContext.getSourceReference())+")");
        }
    }

    /*
    /**********************************************************
    /* Internal/package methods: Error reporting
    /**********************************************************
     */
    
    protected void _reportMismatchedEndMarker(int actCh, char expCh)
        throws JsonParseException
    {
        String startDesc = ""+_parsingContext.getStartLocation(_ioContext.getSourceReference());
        _reportError("Unexpected close marker '"+((char) actCh)+"': expected '"+expCh+"' (for "+_parsingContext.getTypeDesc()+" starting at "+startDesc+")");
    }

    /*
    /**********************************************************
    /* Internal/package methods: shared/reusable builders
    /**********************************************************
     */
    
    public ByteArrayBuilder _getByteArrayBuilder()
    {
        if (_byteArrayBuilder == null) {
            _byteArrayBuilder = new ByteArrayBuilder();
        } else {
            _byteArrayBuilder.reset();
        }
        return _byteArrayBuilder;
    }

    /*
    /**********************************************************
    /* Methods from former JsonNumericParserBase
    /**********************************************************
     */

    // // // Life-cycle of number-parsing
    
    protected final JsonToken reset(boolean negative, int intLen, int fractLen, int expLen)
    {
        if (fractLen < 1 && expLen < 1) { // integer
            return resetInt(negative, intLen);
        }
        return resetFloat(negative, intLen, fractLen, expLen);
    }
        
    protected final JsonToken resetInt(boolean negative, int intLen)
    {
        _numberNegative = negative;
        _intLength = intLen;
        _fractLength = 0;
        _expLength = 0;
        _numTypesValid = NR_UNKNOWN; // to force parsing
        return JsonToken.VALUE_NUMBER_INT;
    }
    
    protected final JsonToken resetFloat(boolean negative, int intLen, int fractLen, int expLen)
    {
        _numberNegative = negative;
        _intLength = intLen;
        _fractLength = fractLen;
        _expLength = expLen;
        _numTypesValid = NR_UNKNOWN; // to force parsing
        return JsonToken.VALUE_NUMBER_FLOAT;
    }
    
    protected final JsonToken resetAsNaN(String valueStr, double value)
    {
        _textBuffer.resetWithString(valueStr);
        _numberDouble = value;
        _numTypesValid = NR_DOUBLE;
        return JsonToken.VALUE_NUMBER_FLOAT;
    }
    
    /*
    /**********************************************************
    /* Numeric accessors of public API
    /**********************************************************
     */
    
    @Override
    public Number getNumberValue() throws IOException, JsonParseException
    {
        if (_numTypesValid == NR_UNKNOWN) {
            _parseNumericValue(NR_UNKNOWN); // will also check event type
        }
        // Separate types for int types
        if (_currToken == JsonToken.VALUE_NUMBER_INT) {
            if ((_numTypesValid & NR_INT) != 0) {
                return Integer.valueOf(_numberInt);
            }
            if ((_numTypesValid & NR_LONG) != 0) {
                return Long.valueOf(_numberLong);
            }
            if ((_numTypesValid & NR_BIGINT) != 0) {
                return _numberBigInt;
            }
            // Shouldn't get this far but if we do
            return _numberBigDecimal;
        }
    
        /* And then floating point types. But here optimal type
         * needs to be big decimal, to avoid losing any data?
         */
        if ((_numTypesValid & NR_BIGDECIMAL) != 0) {
            return _numberBigDecimal;
        }
        if ((_numTypesValid & NR_DOUBLE) == 0) { // sanity check
            _throwInternal();
        }
        return Double.valueOf(_numberDouble);
    }
    
    @Override
    public NumberType getNumberType() throws IOException, JsonParseException
    {
        if (_numTypesValid == NR_UNKNOWN) {
            _parseNumericValue(NR_UNKNOWN); // will also check event type
        }
        if (_currToken == JsonToken.VALUE_NUMBER_INT) {
            if ((_numTypesValid & NR_INT) != 0) {
                return NumberType.INT;
            }
            if ((_numTypesValid & NR_LONG) != 0) {
                return NumberType.LONG;
            }
            return NumberType.BIG_INTEGER;
        }
    
        /* And then floating point types. Here optimal type
         * needs to be big decimal, to avoid losing any data?
         * However... using BD is slow, so let's allow returning
         * double as type if no explicit call has been made to access
         * data as BD?
         */
        if ((_numTypesValid & NR_BIGDECIMAL) != 0) {
            return NumberType.BIG_DECIMAL;
        }
        return NumberType.DOUBLE;
    }
    
    @Override
    public int getIntValue() throws IOException, JsonParseException
    {
        if ((_numTypesValid & NR_INT) == 0) {
            if (_numTypesValid == NR_UNKNOWN) { // not parsed at all
                _parseNumericValue(NR_INT); // will also check event type
            }
            if ((_numTypesValid & NR_INT) == 0) { // wasn't an int natively?
                convertNumberToInt(); // let's make it so, if possible
            }
        }
        return _numberInt;
    }
    
    @Override
    public long getLongValue() throws IOException, JsonParseException
    {
        if ((_numTypesValid & NR_LONG) == 0) {
            if (_numTypesValid == NR_UNKNOWN) {
                _parseNumericValue(NR_LONG);
            }
            if ((_numTypesValid & NR_LONG) == 0) {
                convertNumberToLong();
            }
        }
        return _numberLong;
    }
    
    @Override
    public BigInteger getBigIntegerValue() throws IOException, JsonParseException
    {
        if ((_numTypesValid & NR_BIGINT) == 0) {
            if (_numTypesValid == NR_UNKNOWN) {
                _parseNumericValue(NR_BIGINT);
            }
            if ((_numTypesValid & NR_BIGINT) == 0) {
                convertNumberToBigInteger();
            }
        }
        return _numberBigInt;
    }
    
    @Override
    public float getFloatValue() throws IOException, JsonParseException
    {
        double value = getDoubleValue();
        /* 22-Jan-2009, tatu: Bounds/range checks would be tricky
         *   here, so let's not bother even trying...
         */
        /*
        if (value < -Float.MAX_VALUE || value > MAX_FLOAT_D) {
            _reportError("Numeric value ("+getText()+") out of range of Java float");
        }
        */
        return (float) value;
    }
    
    @Override
    public double getDoubleValue() throws IOException, JsonParseException
    {
        if ((_numTypesValid & NR_DOUBLE) == 0) {
            if (_numTypesValid == NR_UNKNOWN) {
                _parseNumericValue(NR_DOUBLE);
            }
            if ((_numTypesValid & NR_DOUBLE) == 0) {
                convertNumberToDouble();
            }
        }
        return _numberDouble;
    }
    
    @Override
    public BigDecimal getDecimalValue() throws IOException, JsonParseException
    {
        if ((_numTypesValid & NR_BIGDECIMAL) == 0) {
            if (_numTypesValid == NR_UNKNOWN) {
                _parseNumericValue(NR_BIGDECIMAL);
            }
            if ((_numTypesValid & NR_BIGDECIMAL) == 0) {
                convertNumberToBigDecimal();
            }
        }
        return _numberBigDecimal;
    }

    /*
    /**********************************************************
    /* Conversion from textual to numeric representation
    /**********************************************************
     */
    
    /**
     * Method that will parse actual numeric value out of a syntactically
     * valid number value. Type it will parse into depends on whether
     * it is a floating point number, as well as its magnitude: smallest
     * legal type (of ones available) is used for efficiency.
     *
     * @param expType Numeric type that we will immediately need, if any;
     *   mostly necessary to optimize handling of floating point numbers
     */
    protected void _parseNumericValue(int expType)
        throws IOException, JsonParseException
    {
        // Int or float?
        if (_currToken == JsonToken.VALUE_NUMBER_INT) {
            char[] buf = _textBuffer.getTextBuffer();
            int offset = _textBuffer.getTextOffset();
            int len = _intLength;
            if (_numberNegative) {
                ++offset;
            }
            if (len <= 9) { // definitely fits in int
                int i = NumberInput.parseInt(buf, offset, len);
                _numberInt = _numberNegative ? -i : i;
                _numTypesValid = NR_INT;
                return;
            }
            if (len <= 18) { // definitely fits AND is easy to parse using 2 int parse calls
                long l = NumberInput.parseLong(buf, offset, len);
                if (_numberNegative) {
                    l = -l;
                }
                // [JACKSON-230] Could still fit in int, need to check
                if (len == 10) {
                    if (_numberNegative) {
                        if (l >= MIN_INT_L) {
                            _numberInt = (int) l;
                            _numTypesValid = NR_INT;
                            return;
                        }
                    } else {
                        if (l <= MAX_INT_L) {
                            _numberInt = (int) l;
                            _numTypesValid = NR_INT;
                            return;
                        }
                    }
                }
                _numberLong = l;
                _numTypesValid = NR_LONG;
                return;
            }
            _parseSlowIntValue(expType, buf, offset, len);
            return;
        }
        if (_currToken == JsonToken.VALUE_NUMBER_FLOAT) {
            _parseSlowFloatValue(expType);
            return;
        }
        _reportError("Current token ("+_currToken+") not numeric, can not use numeric value accessors");
    }
    
    private final void _parseSlowFloatValue(int expType)
        throws IOException, JsonParseException
    {
        /* Nope: floating point. Here we need to be careful to get
         * optimal parsing strategy: choice is between accurate but
         * slow (BigDecimal) and lossy but fast (Double). For now
         * let's only use BD when explicitly requested -- it can
         * still be constructed correctly at any point since we do
         * retain textual representation
         */
        try {
            if (expType == NR_BIGDECIMAL) {
                _numberBigDecimal = _textBuffer.contentsAsDecimal();
                _numTypesValid = NR_BIGDECIMAL;
            } else {
                // Otherwise double has to do
                _numberDouble = _textBuffer.contentsAsDouble();
                _numTypesValid = NR_DOUBLE;
            }
        } catch (NumberFormatException nex) {
            // Can this ever occur? Due to overflow, maybe?
            _wrapError("Malformed numeric value '"+_textBuffer.contentsAsString()+"'", nex);
        }
    }
    
    private final void _parseSlowIntValue(int expType, char[] buf, int offset, int len)
        throws IOException, JsonParseException
    {
        String numStr = _textBuffer.contentsAsString();
        try {
            // [JACKSON-230] Some long cases still...
            if (NumberInput.inLongRange(buf, offset, len, _numberNegative)) {
                // Probably faster to construct a String, call parse, than to use BigInteger
                _numberLong = Long.parseLong(numStr);
                _numTypesValid = NR_LONG;
            } else {
                // nope, need the heavy guns... (rare case)
                _numberBigInt = new BigInteger(numStr);
                _numTypesValid = NR_BIGINT;
            }
        } catch (NumberFormatException nex) {
            // Can this ever occur? Due to overflow, maybe?
            _wrapError("Malformed numeric value '"+numStr+"'", nex);
        }
    }
    
    /*
    /**********************************************************
    /* Numeric conversions
    /**********************************************************
     */    
    
    protected void convertNumberToInt()
        throws IOException, JsonParseException
    {
        // First, converting from long ought to be easy
        if ((_numTypesValid & NR_LONG) != 0) {
            // Let's verify it's lossless conversion by simple roundtrip
            int result = (int) _numberLong;
            if (((long) result) != _numberLong) {
                _reportError("Numeric value ("+getText()+") out of range of int");
            }
            _numberInt = result;
        } else if ((_numTypesValid & NR_BIGINT) != 0) {
            // !!! Should check for range...
            _numberInt = _numberBigInt.intValue();
        } else if ((_numTypesValid & NR_DOUBLE) != 0) {
            // Need to check boundaries
            if (_numberDouble < MIN_INT_D || _numberDouble > MAX_INT_D) {
                reportOverflowInt();
            }
            _numberInt = (int) _numberDouble;
        } else if ((_numTypesValid & NR_BIGDECIMAL) != 0) {
            if (BD_MIN_INT.compareTo(_numberBigDecimal) > 0 
                || BD_MAX_INT.compareTo(_numberBigDecimal) < 0) {
                reportOverflowInt();
            }
            _numberInt = _numberBigDecimal.intValue();
        } else {
            _throwInternal(); // should never get here
        }
    
        _numTypesValid |= NR_INT;
    }
    
    protected void convertNumberToLong()
        throws IOException, JsonParseException
    {
        if ((_numTypesValid & NR_INT) != 0) {
            _numberLong = (long) _numberInt;
        } else if ((_numTypesValid & NR_BIGINT) != 0) {
            // !!! Should check for range...
            _numberLong = _numberBigInt.longValue();
        } else if ((_numTypesValid & NR_DOUBLE) != 0) {
            // Need to check boundaries
            if (_numberDouble < MIN_LONG_D || _numberDouble > MAX_LONG_D) {
                reportOverflowLong();
            }
            _numberLong = (long) _numberDouble;
        } else if ((_numTypesValid & NR_BIGDECIMAL) != 0) {
            if (BD_MIN_LONG.compareTo(_numberBigDecimal) > 0 
                || BD_MAX_LONG.compareTo(_numberBigDecimal) < 0) {
                reportOverflowLong();
            }
            _numberLong = _numberBigDecimal.longValue();
        } else {
            _throwInternal(); // should never get here
        }
    
        _numTypesValid |= NR_LONG;
    }
    
    protected void convertNumberToBigInteger()
        throws IOException, JsonParseException
    {
        if ((_numTypesValid & NR_BIGDECIMAL) != 0) {
            // here it'll just get truncated, no exceptions thrown
            _numberBigInt = _numberBigDecimal.toBigInteger();
        } else if ((_numTypesValid & NR_LONG) != 0) {
            _numberBigInt = BigInteger.valueOf(_numberLong);
        } else if ((_numTypesValid & NR_INT) != 0) {
            _numberBigInt = BigInteger.valueOf(_numberInt);
        } else if ((_numTypesValid & NR_DOUBLE) != 0) {
            _numberBigInt = BigDecimal.valueOf(_numberDouble).toBigInteger();
        } else {
            _throwInternal(); // should never get here
        }
        _numTypesValid |= NR_BIGINT;
    }
    
    protected void convertNumberToDouble()
        throws IOException, JsonParseException
    {
        /* 05-Aug-2008, tatus: Important note: this MUST start with
         *   more accurate representations, since we don't know which
         *   value is the original one (others get generated when
         *   requested)
         */
    
        if ((_numTypesValid & NR_BIGDECIMAL) != 0) {
            _numberDouble = _numberBigDecimal.doubleValue();
        } else if ((_numTypesValid & NR_BIGINT) != 0) {
            _numberDouble = _numberBigInt.doubleValue();
        } else if ((_numTypesValid & NR_LONG) != 0) {
            _numberDouble = (double) _numberLong;
        } else if ((_numTypesValid & NR_INT) != 0) {
            _numberDouble = (double) _numberInt;
        } else {
            _throwInternal(); // should never get here
        }
    
        _numTypesValid |= NR_DOUBLE;
    }
    
    protected void convertNumberToBigDecimal()
        throws IOException, JsonParseException
    {
        /* 05-Aug-2008, tatus: Important note: this MUST start with
         *   more accurate representations, since we don't know which
         *   value is the original one (others get generated when
         *   requested)
         */
    
        if ((_numTypesValid & NR_DOUBLE) != 0) {
            /* Let's actually parse from String representation,
             * to avoid rounding errors that non-decimal floating operations
             * would incur
             */
            _numberBigDecimal = new BigDecimal(getText());
        } else if ((_numTypesValid & NR_BIGINT) != 0) {
            _numberBigDecimal = new BigDecimal(_numberBigInt);
        } else if ((_numTypesValid & NR_LONG) != 0) {
            _numberBigDecimal = BigDecimal.valueOf(_numberLong);
        } else if ((_numTypesValid & NR_INT) != 0) {
            _numberBigDecimal = BigDecimal.valueOf((long) _numberInt);
        } else {
            _throwInternal(); // should never get here
        }
        _numTypesValid |= NR_BIGDECIMAL;
    }
    
    /*
    /**********************************************************
    /* Number handling exceptions
    /**********************************************************
     */    
    
    protected void reportUnexpectedNumberChar(int ch, String comment)
        throws JsonParseException
    {
        String msg = "Unexpected character ("+_getCharDesc(ch)+") in numeric value";
        if (comment != null) {
            msg += ": "+comment;
        }
        _reportError(msg);
    }
    
    protected void reportInvalidNumber(String msg)
        throws JsonParseException
    {
        _reportError("Invalid numeric value: "+msg);
    }
    
    protected void reportOverflowInt()
        throws IOException, JsonParseException
    {
        _reportError("Numeric value ("+getText()+") out of range of int ("+Integer.MIN_VALUE+" - "+Integer.MAX_VALUE+")");
    }
    
    protected void reportOverflowLong()
        throws IOException, JsonParseException
    {
        _reportError("Numeric value ("+getText()+") out of range of long ("+Long.MIN_VALUE+" - "+Long.MAX_VALUE+")");
    }    

    /*
    /**********************************************************
    /* Base64 handling support
    /**********************************************************
     */

    /**
     * Method that sub-classes must implement to support escaped sequences
     * in base64-encoded sections.
     * Sub-classes that do not need base64 support can leave this as is
     */
    protected char _decodeEscaped()
        throws IOException, JsonParseException {
        throw new UnsupportedOperationException();
    }
    
    protected final int _decodeBase64Escape(Base64Variant b64variant, int ch, int index)
        throws IOException, JsonParseException
    {
        // 17-May-2011, tatu: As per [JACKSON-xxx], need to handle escaped chars
        if (ch != '\\') {
            throw reportInvalidBase64Char(b64variant, ch, index);
        }
        int unescaped = _decodeEscaped();
        // if white space, skip if first triplet; otherwise errors
        if (unescaped <= INT_SPACE) {
            if (index == 0) { // whitespace only allowed to be skipped between triplets
                return -1;
            }
        }
        // otherwise try to find actual triplet value
        int bits = b64variant.decodeBase64Char(unescaped);
        if (bits < 0) {
            throw reportInvalidBase64Char(b64variant, unescaped, index);
        }
        return bits;
    }
    
    protected final int _decodeBase64Escape(Base64Variant b64variant, char ch, int index)
        throws IOException, JsonParseException
    {
        // 17-May-2011, tatu: As per [JACKSON-xxx], need to handle escaped chars
        if (ch != '\\') {
            throw reportInvalidBase64Char(b64variant, ch, index);
        }
        char unescaped = _decodeEscaped();
        // if white space, skip if first triplet; otherwise errors
        if (unescaped <= INT_SPACE) {
            if (index == 0) { // whitespace only allowed to be skipped between triplets
                return -1;
            }
        }
        // otherwise try to find actual triplet value
        int bits = b64variant.decodeBase64Char(unescaped);
        if (bits < 0) {
            throw reportInvalidBase64Char(b64variant, unescaped, index);
        }
        return bits;
    }
    
    protected IllegalArgumentException reportInvalidBase64Char(Base64Variant b64variant, int ch, int bindex)
        throws IllegalArgumentException
    {
        return reportInvalidBase64Char(b64variant, ch, bindex, null);
    }

    /**
     * @param bindex Relative index within base64 character unit; between 0
     *   and 3 (as unit has exactly 4 characters)
     */
    protected IllegalArgumentException reportInvalidBase64Char(Base64Variant b64variant, int ch, int bindex, String msg)
        throws IllegalArgumentException
    {
        String base;
        if (ch <= INT_SPACE) {
            base = "Illegal white space character (code 0x"+Integer.toHexString(ch)+") as character #"+(bindex+1)+" of 4-char base64 unit: can only used between units";
        } else if (b64variant.usesPaddingChar(ch)) {
            base = "Unexpected padding character ('"+b64variant.getPaddingChar()+"') as character #"+(bindex+1)+" of 4-char base64 unit: padding only legal as 3rd or 4th character";
        } else if (!Character.isDefined(ch) || Character.isISOControl(ch)) {
            // Not sure if we can really get here... ? (most illegal xml chars are caught at lower level)
            base = "Illegal character (code 0x"+Integer.toHexString(ch)+") in base64 content";
        } else {
            base = "Illegal character '"+((char)ch)+"' (code 0x"+Integer.toHexString(ch)+") in base64 content";
        }
        if (msg != null) {
            base = base + ": " + msg;
        }
        return new IllegalArgumentException(base);
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

