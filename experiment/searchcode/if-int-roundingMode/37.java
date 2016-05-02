/*
 * Portions Copyright 1996-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

/*
 * Portions Copyright IBM Corporation, 2001. All Rights Reserved.
 */

package java.math;

/**
 * Immutable, arbitrary-precision signed decimal numbers.  A
 * {@code BigDecimal} consists of an arbitrary precision integer
 * <i>unscaled value</i> and a 32-bit integer <i>scale</i>.  If zero
 * or positive, the scale is the number of digits to the right of the
 * decimal point.  If negative, the unscaled value of the number is
 * multiplied by ten to the power of the negation of the scale.  The
 * value of the number represented by the {@code BigDecimal} is
 * therefore <tt>(unscaledValue &times; 10<sup>-scale</sup>)</tt>.
 *
 * <p>The {@code BigDecimal} class provides operations for
 * arithmetic, scale manipulation, rounding, comparison, hashing, and
 * format conversion.  The {@link #toString} method provides a
 * canonical representation of a {@code BigDecimal}.
 *
 * <p>The {@code BigDecimal} class gives its user complete control
 * over rounding behavior.  If no rounding mode is specified and the
 * exact result cannot be represented, an exception is thrown;
 * otherwise, calculations can be carried out to a chosen precision
 * and rounding mode by supplying an appropriate {@link MathContext}
 * object to the operation.  In either case, eight <em>rounding
 * modes</em> are provided for the control of rounding.  Using the
 * integer fields in this class (such as {@link #ROUND_HALF_UP}) to
 * represent rounding mode is largely obsolete; the enumeration values
 * of the {@code RoundingMode} {@code enum}, (such as {@link
 * RoundingMode#HALF_UP}) should be used instead.
 *
 * <p>When a {@code MathContext} object is supplied with a precision
 * setting of 0 (for example, {@link MathContext#UNLIMITED}),
 * arithmetic operations are exact, as are the arithmetic methods
 * which take no {@code MathContext} object.  (This is the only
 * behavior that was supported in releases prior to 5.)  As a
 * corollary of computing the exact result, the rounding mode setting
 * of a {@code MathContext} object with a precision setting of 0 is
 * not used and thus irrelevant.  In the case of divide, the exact
 * quotient could have an infinitely long decimal expansion; for
 * example, 1 divided by 3.  If the quotient has a nonterminating
 * decimal expansion and the operation is specified to return an exact
 * result, an {@code ArithmeticException} is thrown.  Otherwise, the
 * exact result of the division is returned, as done for other
 * operations.
 *
 * <p>When the precision setting is not 0, the rules of
 * {@code BigDecimal} arithmetic are broadly compatible with selected
 * modes of operation of the arithmetic defined in ANSI X3.274-1996
 * and ANSI X3.274-1996/AM 1-2000 (section 7.4).  Unlike those
 * standards, {@code BigDecimal} includes many rounding modes, which
 * were mandatory for division in {@code BigDecimal} releases prior
 * to 5.  Any conflicts between these ANSI standards and the
 * {@code BigDecimal} specification are resolved in favor of
 * {@code BigDecimal}.
 *
 * <p>Since the same numerical value can have different
 * representations (with different scales), the rules of arithmetic
 * and rounding must specify both the numerical result and the scale
 * used in the result's representation.
 *
 *
 * <p>In general the rounding modes and precision setting determine
 * how operations return results with a limited number of digits when
 * the exact result has more digits (perhaps infinitely many in the
 * case of division) than the number of digits returned.
 *
 * First, the
 * total number of digits to return is specified by the
 * {@code MathContext}'s {@code precision} setting; this determines
 * the result's <i>precision</i>.  The digit count starts from the
 * leftmost nonzero digit of the exact result.  The rounding mode
 * determines how any discarded trailing digits affect the returned
 * result.
 *
 * <p>For all arithmetic operators , the operation is carried out as
 * though an exact intermediate result were first calculated and then
 * rounded to the number of digits specified by the precision setting
 * (if necessary), using the selected rounding mode.  If the exact
 * result is not returned, some digit positions of the exact result
 * are discarded.  When rounding increases the magnitude of the
 * returned result, it is possible for a new digit position to be
 * created by a carry propagating to a leading {@literal "9"} digit.
 * For example, rounding the value 999.9 to three digits rounding up
 * would be numerically equal to one thousand, represented as
 * 100&times;10<sup>1</sup>.  In such cases, the new {@literal "1"} is
 * the leading digit position of the returned result.
 *
 * <p>Besides a logical exact result, each arithmetic operation has a
 * preferred scale for representing a result.  The preferred
 * scale for each operation is listed in the table below.
 *
 * <table border>
 * <caption top><h3>Preferred Scales for Results of Arithmetic Operations
 * </h3></caption>
 * <tr><th>Operation</th><th>Preferred Scale of Result</th></tr>
 * <tr><td>Add</td><td>max(addend.scale(), augend.scale())</td>
 * <tr><td>Subtract</td><td>max(minuend.scale(), subtrahend.scale())</td>
 * <tr><td>Multiply</td><td>multiplier.scale() + multiplicand.scale()</td>
 * <tr><td>Divide</td><td>dividend.scale() - divisor.scale()</td>
 * </table>
 *
 * These scales are the ones used by the methods which return exact
 * arithmetic results; except that an exact divide may have to use a
 * larger scale since the exact result may have more digits.  For
 * example, {@code 1/32} is {@code 0.03125}.
 *
 * <p>Before rounding, the scale of the logical exact intermediate
 * result is the preferred scale for that operation.  If the exact
 * numerical result cannot be represented in {@code precision}
 * digits, rounding selects the set of digits to return and the scale
 * of the result is reduced from the scale of the intermediate result
 * to the least scale which can represent the {@code precision}
 * digits actually returned.  If the exact result can be represented
 * with at most {@code precision} digits, the representation
 * of the result with the scale closest to the preferred scale is
 * returned.  In particular, an exactly representable quotient may be
 * represented in fewer than {@code precision} digits by removing
 * trailing zeros and decreasing the scale.  For example, rounding to
 * three digits using the {@linkplain RoundingMode#FLOOR floor}
 * rounding mode, <br>
 *
 * {@code 19/100 = 0.19   // integer=19,  scale=2} <br>
 *
 * but<br>
 *
 * {@code 21/110 = 0.190  // integer=190, scale=3} <br>
 *
 * <p>Note that for add, subtract, and multiply, the reduction in
 * scale will equal the number of digit positions of the exact result
 * which are discarded. If the rounding causes a carry propagation to
 * create a new high-order digit position, an additional digit of the
 * result is discarded than when no new digit position is created.
 *
 * <p>Other methods may have slightly different rounding semantics.
 * For example, the result of the {@code pow} method using the
 * {@linkplain #pow(int, MathContext) specified algorithm} can
 * occasionally differ from the rounded mathematical result by more
 * than one unit in the last place, one <i>{@linkplain #ulp() ulp}</i>.
 *
 * <p>Two types of operations are provided for manipulating the scale
 * of a {@code BigDecimal}: scaling/rounding operations and decimal
 * point motion operations.  Scaling/rounding operations ({@link
 * #setScale setScale} and {@link #round round}) return a
 * {@code BigDecimal} whose value is approximately (or exactly) equal
 * to that of the operand, but whose scale or precision is the
 * specified value; that is, they increase or decrease the precision
 * of the stored number with minimal effect on its value.  Decimal
 * point motion operations ({@link #movePointLeft movePointLeft} and
 * {@link #movePointRight movePointRight}) return a
 * {@code BigDecimal} created from the operand by moving the decimal
 * point a specified distance in the specified direction.
 *
 * <p>For the sake of brevity and clarity, pseudo-code is used
 * throughout the descriptions of {@code BigDecimal} methods.  The
 * pseudo-code expression {@code (i + j)} is shorthand for "a
 * {@code BigDecimal} whose value is that of the {@code BigDecimal}
 * {@code i} added to that of the {@code BigDecimal}
 * {@code j}." The pseudo-code expression {@code (i == j)} is
 * shorthand for "{@code true} if and only if the
 * {@code BigDecimal} {@code i} represents the same value as the
 * {@code BigDecimal} {@code j}." Other pseudo-code expressions
 * are interpreted similarly.  Square brackets are used to represent
 * the particular {@code BigInteger} and scale pair defining a
 * {@code BigDecimal} value; for example [19, 2] is the
 * {@code BigDecimal} numerically equal to 0.19 having a scale of 2.
 *
 * <p>Note: care should be exercised if {@code BigDecimal} objects
 * are used as keys in a {@link java.util.SortedMap SortedMap} or
 * elements in a {@link java.util.SortedSet SortedSet} since
 * {@code BigDecimal}'s <i>natural ordering</i> is <i>inconsistent
 * with equals</i>.  See {@link Comparable}, {@link
 * java.util.SortedMap} or {@link java.util.SortedSet} for more
 * information.
 *
 * <p>All methods and constructors for this class throw
 * {@code NullPointerException} when passed a {@code null} object
 * reference for any input parameter.
 *
 * @see     BigInteger
 * @see     MathContext
 * @see     RoundingMode
 * @see     java.util.SortedMap
 * @see     java.util.SortedSet
 * @author  Josh Bloch
 * @author  Mike Cowlishaw
 * @author  Joseph D. Darcy
 */
public class BigDecimal extends Number implements Comparable<BigDecimal> {
    /**
     * The unscaled value of this BigDecimal, as returned by {@link
     * #unscaledValue}.
     *
     * @serial
     * @see #unscaledValue
     */
    private volatile BigInteger intVal;

    /**
     * The scale of this BigDecimal, as returned by {@link #scale}.
     *
     * @serial
     * @see #scale
     */
    private int scale = 0;  // Note: this may have any value, so
                            // calculations must be done in longs
    /**
     * The number of decimal digits in this BigDecimal, or 0 if the
     * number of digits are not known (lookaside information).  If
     * nonzero, the value is guaranteed correct.  Use the precision()
     * method to obtain and set the value if it might be 0.  This
     * field is mutable until set nonzero.
     *
     * @since  1.5
     */
    private volatile transient int precision = 0;

    /**
     * Used to store the canonical string representation, if computed.
     */
    private volatile transient String stringCache = null;

    /**
     * Sentinel value for {@link #intCompact} indicating the
     * significand information is only available from {@code intVal}.
     */
    private static final long INFLATED = Long.MIN_VALUE;

    /**
     * If the absolute value of the significand of this BigDecimal is
     * less than or equal to {@code Long.MAX_VALUE}, the value can be
     * compactly stored in this field and used in computations.
     */
    private transient long intCompact = INFLATED;

    // All 18-digit base ten strings fit into a long; not all 19-digit
    // strings will
    private static final int MAX_COMPACT_DIGITS = 18;

    private static final int MAX_BIGINT_BITS = 62;

    /* Appease the serialization gods */
    private static final long serialVersionUID = 6108874887143696463L;

    // Cache of common small BigDecimal values.
    private static final BigDecimal zeroThroughTen[] = {
        new BigDecimal(BigInteger.ZERO,         0,  0),
        new BigDecimal(BigInteger.ONE,          1,  0),
        new BigDecimal(BigInteger.valueOf(2),   2,  0),
        new BigDecimal(BigInteger.valueOf(3),   3,  0),
        new BigDecimal(BigInteger.valueOf(4),   4,  0),
        new BigDecimal(BigInteger.valueOf(5),   5,  0),
        new BigDecimal(BigInteger.valueOf(6),   6,  0),
        new BigDecimal(BigInteger.valueOf(7),   7,  0),
        new BigDecimal(BigInteger.valueOf(8),   8,  0),
        new BigDecimal(BigInteger.valueOf(9),   9,  0),
        new BigDecimal(BigInteger.TEN,          10, 0),
    };

    // Constants
    /**
     * The value 0, with a scale of 0.
     *
     * @since  1.5
     */
    public static final BigDecimal ZERO =
        zeroThroughTen[0];

    /**
     * The value 1, with a scale of 0.
     *
     * @since  1.5
     */
    public static final BigDecimal ONE =
        zeroThroughTen[1];

    /**
     * The value 10, with a scale of 0.
     *
     * @since  1.5
     */
    public static final BigDecimal TEN =
        zeroThroughTen[10];

    // Constructors

    /**
     * Translates a character array representation of a
     * {@code BigDecimal} into a {@code BigDecimal}, accepting the
     * same sequence of characters as the {@link #BigDecimal(String)}
     * constructor, while allowing a sub-array to be specified.
     *
     * <p>Note that if the sequence of characters is already available
     * within a character array, using this constructor is faster than
     * converting the {@code char} array to string and using the
     * {@code BigDecimal(String)} constructor .
     *
     * @param  in {@code char} array that is the source of characters.
     * @param  offset first character in the array to inspect.
     * @param  len number of characters to consider.
     * @throws NumberFormatException if {@code in} is not a valid
     *         representation of a {@code BigDecimal} or the defined subarray
     *         is not wholly within {@code in}.
     * @since  1.5
     */
    public BigDecimal(char[] in, int offset, int len) {
        // This is the primary string to BigDecimal constructor; all
        // incoming strings end up here; it uses explicit (inline)
        // parsing for speed and generates at most one intermediate
        // (temporary) object (a char[] array).

        // use array bounds checking to handle too-long, len == 0,
        // bad offset, etc.
        try {
            // handle the sign
            boolean isneg = false;          // assume positive
            if (in[offset] == '-') {
                isneg = true;               // leading minus means negative
                offset++;
                len--;
            } else if (in[offset] == '+') { // leading + allowed
                offset++;
                len--;
            }

            // should now be at numeric part of the significand
            int dotoff = -1;                 // '.' offset, -1 if none
            int cfirst = offset;             // record start of integer
            long exp = 0;                    // exponent
            if (len > in.length)             // protect against huge length
                throw new NumberFormatException();
            char coeff[] = new char[len];    // integer significand array
            char c;                          // work

            for (; len > 0; offset++, len--) {
                c = in[offset];
                if ((c >= '0' && c <= '9') || Character.isDigit(c)) {
                    // have digit
                    coeff[precision] = c;
                    precision++;             // count of digits
                    continue;
                }
                if (c == '.') {
                    // have dot
                    if (dotoff >= 0)         // two dots
                        throw new NumberFormatException();
                    dotoff = offset;
                    continue;
                }
                // exponent expected
                if ((c != 'e') && (c != 'E'))
                    throw new NumberFormatException();
                offset++;
                c = in[offset];
                len--;
                boolean negexp = false;
                // optional sign
                if (c == '-' || c == '+') {
                    negexp = (c == '-');
                    offset++;
                    c = in[offset];
                    len--;
                }
                if (len <= 0)    // no exponent digits
                    throw new NumberFormatException();
                // skip leading zeros in the exponent
                while (len > 10 && Character.digit(c, 10) == 0) {
                        offset++;
                        c = in[offset];
                        len--;
                }
                if (len > 10)  // too many nonzero exponent digits
                    throw new NumberFormatException();
                // c now holds first digit of exponent
                for (;; len--) {
                    int v;
                    if (c >= '0' && c <= '9') {
                        v = c - '0';
                    } else {
                        v = Character.digit(c, 10);
                        if (v < 0)            // not a digit
                            throw new NumberFormatException();
                    }
                    exp = exp * 10 + v;
                    if (len == 1)
                        break;               // that was final character
                    offset++;
                    c = in[offset];
                }
                if (negexp)                  // apply sign
                    exp = -exp;
                // Next test is required for backwards compatibility
                if ((int)exp != exp)         // overflow
                    throw new NumberFormatException();
                break;                       // [saves a test]
                }
            // here when no characters left
            if (precision == 0)              // no digits found
                throw new NumberFormatException();

            if (dotoff >= 0) {               // had dot; set scale
                scale = precision - (dotoff - cfirst);
                // [cannot overflow]
            }
            if (exp != 0) {                  // had significant exponent
                try {
                    scale = checkScale(-exp + scale); // adjust
                } catch (ArithmeticException e) {
                    throw new NumberFormatException("Scale out of range.");
                }
            }

            // Remove leading zeros from precision (digits count)
            int first = 0;
            for (; (coeff[first] == '0' || Character.digit(coeff[first], 10) == 0) &&
                     precision > 1;
                 first++)
                precision--;

            // Set the significand ..
            // Copy significand to exact-sized array, with sign if
            // negative
            // Later use: BigInteger(coeff, first, precision) for
            //   both cases, by allowing an extra char at the front of
            //   coeff.
            char quick[];
            if (!isneg) {
                quick = new char[precision];
                System.arraycopy(coeff, first, quick, 0, precision);
            } else {
                quick = new char[precision+1];
                quick[0] = '-';
                System.arraycopy(coeff, first, quick, 1, precision);
            }
            if (precision <= MAX_COMPACT_DIGITS)
                intCompact = Long.parseLong(new String(quick));
            else
                intVal = new BigInteger(quick);
            // System.out.println(" new: " +intVal+" ["+scale+"] "+precision);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NumberFormatException();
        } catch (NegativeArraySizeException e) {
            throw new NumberFormatException();
        }
    }

    /**
     * Translates a character array representation of a
     * {@code BigDecimal} into a {@code BigDecimal}, accepting the
     * same sequence of characters as the {@link #BigDecimal(String)}
     * constructor, while allowing a sub-array to be specified and
     * with rounding according to the context settings.
     *
     * <p>Note that if the sequence of characters is already available
     * within a character array, using this constructor is faster than
     * converting the {@code char} array to string and using the
     * {@code BigDecimal(String)} constructor .
     *
     * @param  in {@code char} array that is the source of characters.
     * @param  offset first character in the array to inspect.
     * @param  len number of characters to consider..
     * @param  mc the context to use.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}.
     * @throws NumberFormatException if {@code in} is not a valid
     *         representation of a {@code BigDecimal} or the defined subarray
     *         is not wholly within {@code in}.
     * @since  1.5
     */
    public BigDecimal(char[] in, int offset, int len, MathContext mc) {
        this(in, offset, len);
        if (mc.precision > 0)
            roundThis(mc);
    }

    /**
     * Translates a character array representation of a
     * {@code BigDecimal} into a {@code BigDecimal}, accepting the
     * same sequence of characters as the {@link #BigDecimal(String)}
     * constructor.
     *
     * <p>Note that if the sequence of characters is already available
     * as a character array, using this constructor is faster than
     * converting the {@code char} array to string and using the
     * {@code BigDecimal(String)} constructor .
     *
     * @param in {@code char} array that is the source of characters.
     * @throws NumberFormatException if {@code in} is not a valid
     *         representation of a {@code BigDecimal}.
     * @since  1.5
     */
    public BigDecimal(char[] in) {
        this(in, 0, in.length);
    }

    /**
     * Translates a character array representation of a
     * {@code BigDecimal} into a {@code BigDecimal}, accepting the
     * same sequence of characters as the {@link #BigDecimal(String)}
     * constructor and with rounding according to the context
     * settings.
     *
     * <p>Note that if the sequence of characters is already available
     * as a character array, using this constructor is faster than
     * converting the {@code char} array to string and using the
     * {@code BigDecimal(String)} constructor .
     *
     * @param  in {@code char} array that is the source of characters.
     * @param  mc the context to use.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}.
     * @throws NumberFormatException if {@code in} is not a valid
     *         representation of a {@code BigDecimal}.
     * @since  1.5
     */
    public BigDecimal(char[] in, MathContext mc) {
        this(in, 0, in.length, mc);
    }

    /**
     * Translates the string representation of a {@code BigDecimal}
     * into a {@code BigDecimal}.  The string representation consists
     * of an optional sign, {@code '+'} (<tt> '&#92;u002B'</tt>) or
     * {@code '-'} (<tt>'&#92;u002D'</tt>), followed by a sequence of
     * zero or more decimal digits ("the integer"), optionally
     * followed by a fraction, optionally followed by an exponent.
     *
     * <p>The fraction consists of a decimal point followed by zero
     * or more decimal digits.  The string must contain at least one
     * digit in either the integer or the fraction.  The number formed
     * by the sign, the integer and the fraction is referred to as the
     * <i>significand</i>.
     *
     * <p>The exponent consists of the character {@code 'e'}
     * (<tt>'&#92;u0065'</tt>) or {@code 'E'} (<tt>'&#92;u0045'</tt>)
     * followed by one or more decimal digits.  The value of the
     * exponent must lie between -{@link Integer#MAX_VALUE} ({@link
     * Integer#MIN_VALUE}+1) and {@link Integer#MAX_VALUE}, inclusive.
     *
     * <p>More formally, the strings this constructor accepts are
     * described by the following grammar:
     * <blockquote>
     * <dl>
     * <dt><i>BigDecimalString:</i>
     * <dd><i>Sign<sub>opt</sub> Significand Exponent<sub>opt</sub></i>
     * <p>
     * <dt><i>Sign:</i>
     * <dd>{@code +}
     * <dd>{@code -}
     * <p>
     * <dt><i>Significand:</i>
     * <dd><i>IntegerPart</i> {@code .} <i>FractionPart<sub>opt</sub></i>
     * <dd>{@code .} <i>FractionPart</i>
     * <dd><i>IntegerPart</i>
     * <p>
     * <dt><i>IntegerPart:
     * <dd>Digits</i>
     * <p>
     * <dt><i>FractionPart:
     * <dd>Digits</i>
     * <p>
     * <dt><i>Exponent:
     * <dd>ExponentIndicator SignedInteger</i>
     * <p>
     * <dt><i>ExponentIndicator:</i>
     * <dd>{@code e}
     * <dd>{@code E}
     * <p>
     * <dt><i>SignedInteger:
     * <dd>Sign<sub>opt</sub> Digits</i>
     * <p>
     * <dt><i>Digits:
     * <dd>Digit
     * <dd>Digits Digit</i>
     * <p>
     * <dt><i>Digit:</i>
     * <dd>any character for which {@link Character#isDigit}
     * returns {@code true}, including 0, 1, 2 ...
     * </dl>
     * </blockquote>
     *
     * <p>The scale of the returned {@code BigDecimal} will be the
     * number of digits in the fraction, or zero if the string
     * contains no decimal point, subject to adjustment for any
     * exponent; if the string contains an exponent, the exponent is
     * subtracted from the scale.  The value of the resulting scale
     * must lie between {@code Integer.MIN_VALUE} and
     * {@code Integer.MAX_VALUE}, inclusive.
     *
     * <p>The character-to-digit mapping is provided by {@link
     * java.lang.Character#digit} set to convert to radix 10.  The
     * String may not contain any extraneous characters (whitespace,
     * for example).
     *
     * <p><b>Examples:</b><br>
     * The value of the returned {@code BigDecimal} is equal to
     * <i>significand</i> &times; 10<sup>&nbsp;<i>exponent</i></sup>.
     * For each string on the left, the resulting representation
     * [{@code BigInteger}, {@code scale}] is shown on the right.
     * <pre>
     * "0"            [0,0]
     * "0.00"         [0,2]
     * "123"          [123,0]
     * "-123"         [-123,0]
     * "1.23E3"       [123,-1]
     * "1.23E+3"      [123,-1]
     * "12.3E+7"      [123,-6]
     * "12.0"         [120,1]
     * "12.3"         [123,1]
     * "0.00123"      [123,5]
     * "-1.23E-12"    [-123,14]
     * "1234.5E-4"    [12345,5]
     * "0E+7"         [0,-7]
     * "-0"           [0,0]
     * </pre>
     *
     * <p>Note: For values other than {@code float} and
     * {@code double} NaN and &plusmn;Infinity, this constructor is
     * compatible with the values returned by {@link Float#toString}
     * and {@link Double#toString}.  This is generally the preferred
     * way to convert a {@code float} or {@code double} into a
     * BigDecimal, as it doesn't suffer from the unpredictability of
     * the {@link #BigDecimal(double)} constructor.
     *
     * @param val String representation of {@code BigDecimal}.
     *
     * @throws NumberFormatException if {@code val} is not a valid
     *         representation of a {@code BigDecimal}.
     */
    public BigDecimal(String val) {
        this(val.toCharArray(), 0, val.length());
    }

    /**
     * Translates the string representation of a {@code BigDecimal}
     * into a {@code BigDecimal}, accepting the same strings as the
     * {@link #BigDecimal(String)} constructor, with rounding
     * according to the context settings.
     *
     * @param  val string representation of a {@code BigDecimal}.
     * @param  mc the context to use.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}.
     * @throws NumberFormatException if {@code val} is not a valid
     *         representation of a BigDecimal.
     * @since  1.5
     */
    public BigDecimal(String val, MathContext mc) {
        this(val.toCharArray(), 0, val.length());
        if (mc.precision > 0)
            roundThis(mc);
    }

    /**
     * Translates a {@code double} into a {@code BigDecimal} which
     * is the exact decimal representation of the {@code double}'s
     * binary floating-point value.  The scale of the returned
     * {@code BigDecimal} is the smallest value such that
     * <tt>(10<sup>scale</sup> &times; val)</tt> is an integer.
     * <p>
     * <b>Notes:</b>
     * <ol>
     * <li>
     * The results of this constructor can be somewhat unpredictable.
     * One might assume that writing {@code new BigDecimal(0.1)} in
     * Java creates a {@code BigDecimal} which is exactly equal to
     * 0.1 (an unscaled value of 1, with a scale of 1), but it is
     * actually equal to
     * 0.1000000000000000055511151231257827021181583404541015625.
     * This is because 0.1 cannot be represented exactly as a
     * {@code double} (or, for that matter, as a binary fraction of
     * any finite length).  Thus, the value that is being passed
     * <i>in</i> to the constructor is not exactly equal to 0.1,
     * appearances notwithstanding.
     *
     * <li>
     * The {@code String} constructor, on the other hand, is
     * perfectly predictable: writing {@code new BigDecimal("0.1")}
     * creates a {@code BigDecimal} which is <i>exactly</i> equal to
     * 0.1, as one would expect.  Therefore, it is generally
     * recommended that the {@linkplain #BigDecimal(String)
     * <tt>String</tt> constructor} be used in preference to this one.
     *
     * <li>
     * When a {@code double} must be used as a source for a
     * {@code BigDecimal}, note that this constructor provides an
     * exact conversion; it does not give the same result as
     * converting the {@code double} to a {@code String} using the
     * {@link Double#toString(double)} method and then using the
     * {@link #BigDecimal(String)} constructor.  To get that result,
     * use the {@code static} {@link #valueOf(double)} method.
     * </ol>
     *
     * @param val {@code double} value to be converted to
     *        {@code BigDecimal}.
     * @throws NumberFormatException if {@code val} is infinite or NaN.
     */
    public BigDecimal(double val) {
        if (Double.isInfinite(val) || Double.isNaN(val))
            throw new NumberFormatException("Infinite or NaN");

        // Translate the double into sign, exponent and significand, according
        // to the formulae in JLS, Section 20.10.22.
        long valBits = Double.doubleToLongBits(val);
        int sign = ((valBits >> 63)==0 ? 1 : -1);
        int exponent = (int) ((valBits >> 52) & 0x7ffL);
        long significand = (exponent==0 ? (valBits & ((1L<<52) - 1)) << 1
                            : (valBits & ((1L<<52) - 1)) | (1L<<52));
        exponent -= 1075;
        // At this point, val == sign * significand * 2**exponent.

        /*
         * Special case zero to supress nonterminating normalization
         * and bogus scale calculation.
         */
        if (significand == 0) {
            intVal = BigInteger.ZERO;
            intCompact = 0;
            precision = 1;
            return;
        }

        // Normalize
        while((significand & 1) == 0) {    //  i.e., significand is even
            significand >>= 1;
            exponent++;
        }

        // Calculate intVal and scale
        intVal = BigInteger.valueOf(sign*significand);
        if (exponent < 0) {
            intVal = intVal.multiply(BigInteger.valueOf(5).pow(-exponent));
            scale = -exponent;
        } else if (exponent > 0) {
            intVal = intVal.multiply(BigInteger.valueOf(2).pow(exponent));
        }
        if (intVal.bitLength() <= MAX_BIGINT_BITS) {
            intCompact = intVal.longValue();
        }
    }

    /**
     * Translates a {@code double} into a {@code BigDecimal}, with
     * rounding according to the context settings.  The scale of the
     * {@code BigDecimal} is the smallest value such that
     * <tt>(10<sup>scale</sup> &times; val)</tt> is an integer.
     *
     * <p>The results of this constructor can be somewhat unpredictable
     * and its use is generally not recommended; see the notes under
     * the {@link #BigDecimal(double)} constructor.
     *
     * @param  val {@code double} value to be converted to
     *         {@code BigDecimal}.
     * @param  mc the context to use.
     * @throws ArithmeticException if the result is inexact but the
     *         RoundingMode is UNNECESSARY.
     * @throws NumberFormatException if {@code val} is infinite or NaN.
     * @since  1.5
     */
    public BigDecimal(double val, MathContext mc) {
        this(val);
        if (mc.precision > 0)
            roundThis(mc);
    }

    /**
     * Translates a {@code BigInteger} into a {@code BigDecimal}.
     * The scale of the {@code BigDecimal} is zero.
     *
     * @param val {@code BigInteger} value to be converted to
     *            {@code BigDecimal}.
     */
    public BigDecimal(BigInteger val) {
        intVal = val;
        if (val.bitLength() <= MAX_BIGINT_BITS) {
            intCompact = val.longValue();
        }
    }

    /**
     * Translates a {@code BigInteger} into a {@code BigDecimal}
     * rounding according to the context settings.  The scale of the
     * {@code BigDecimal} is zero.
     *
     * @param val {@code BigInteger} value to be converted to
     *            {@code BigDecimal}.
     * @param  mc the context to use.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}.
     * @since  1.5
     */
    public BigDecimal(BigInteger val, MathContext mc) {
        intVal = val;
        if (mc.precision > 0)
            roundThis(mc);
    }

    /**
     * Translates a {@code BigInteger} unscaled value and an
     * {@code int} scale into a {@code BigDecimal}.  The value of
     * the {@code BigDecimal} is
     * <tt>(unscaledVal &times; 10<sup>-scale</sup>)</tt>.
     *
     * @param unscaledVal unscaled value of the {@code BigDecimal}.
     * @param scale scale of the {@code BigDecimal}.
     */
    public BigDecimal(BigInteger unscaledVal, int scale) {
        // Negative scales are now allowed
        intVal = unscaledVal;
        this.scale = scale;
        if (unscaledVal.bitLength() <= MAX_BIGINT_BITS) {
            intCompact = unscaledVal.longValue();
        }
    }

    /**
     * Translates a {@code BigInteger} unscaled value and an
     * {@code int} scale into a {@code BigDecimal}, with rounding
     * according to the context settings.  The value of the
     * {@code BigDecimal} is <tt>(unscaledVal &times;
     * 10<sup>-scale</sup>)</tt>, rounded according to the
     * {@code precision} and rounding mode settings.
     *
     * @param  unscaledVal unscaled value of the {@code BigDecimal}.
     * @param  scale scale of the {@code BigDecimal}.
     * @param  mc the context to use.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}.
     * @since  1.5
     */
    public BigDecimal(BigInteger unscaledVal, int scale, MathContext mc) {
        intVal = unscaledVal;
        this.scale = scale;
        if (mc.precision > 0)
            roundThis(mc);
    }

    /**
     * Translates an {@code int} into a {@code BigDecimal}.  The
     * scale of the {@code BigDecimal} is zero.
     *
     * @param val {@code int} value to be converted to
     *            {@code BigDecimal}.
     * @since  1.5
     */
    public BigDecimal(int val) {
        intCompact = val;
    }

    /**
     * Translates an {@code int} into a {@code BigDecimal}, with
     * rounding according to the context settings.  The scale of the
     * {@code BigDecimal}, before any rounding, is zero.
     *
     * @param  val {@code int} value to be converted to {@code BigDecimal}.
     * @param  mc the context to use.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}.
     * @since  1.5
     */
    public BigDecimal(int val, MathContext mc) {
        intCompact = val;
        if (mc.precision > 0)
            roundThis(mc);
    }

    /**
     * Translates a {@code long} into a {@code BigDecimal}.  The
     * scale of the {@code BigDecimal} is zero.
     *
     * @param val {@code long} value to be converted to {@code BigDecimal}.
     * @since  1.5
     */
    public BigDecimal(long val) {
        if (compactLong(val))
            intCompact = val;
        else
            intVal = BigInteger.valueOf(val);
    }

    /**
     * Translates a {@code long} into a {@code BigDecimal}, with
     * rounding according to the context settings.  The scale of the
     * {@code BigDecimal}, before any rounding, is zero.
     *
     * @param  val {@code long} value to be converted to {@code BigDecimal}.
     * @param  mc the context to use.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}.
     * @since  1.5
     */
    public BigDecimal(long val, MathContext mc) {
        if (compactLong(val))
            intCompact = val;
        else
            intVal = BigInteger.valueOf(val);
        if (mc.precision > 0)
            roundThis(mc);
    }

    /**
     * Trusted internal constructor
     */
    private BigDecimal(long val, int scale) {
        this.intCompact = val;
        this.scale = scale;
    }

    /**
     * Trusted internal constructor
     */
    private BigDecimal(BigInteger intVal, long val, int scale) {
        this.intVal = intVal;
        this.intCompact = val;
        this.scale = scale;
    }

    // Static Factory Methods

    /**
     * Translates a {@code long} unscaled value and an
     * {@code int} scale into a {@code BigDecimal}.  This
     * {@literal "static factory method"} is provided in preference to
     * a ({@code long}, {@code int}) constructor because it
     * allows for reuse of frequently used {@code BigDecimal} values..
     *
     * @param unscaledVal unscaled value of the {@code BigDecimal}.
     * @param scale scale of the {@code BigDecimal}.
     * @return a {@code BigDecimal} whose value is
     *         <tt>(unscaledVal &times; 10<sup>-scale</sup>)</tt>.
     */
    public static BigDecimal valueOf(long unscaledVal, int scale) {
        if (scale == 0 && unscaledVal >= 0 && unscaledVal <= 10) {
            return zeroThroughTen[(int)unscaledVal];
        }
        if (compactLong(unscaledVal))
            return new BigDecimal(unscaledVal, scale);
        return new BigDecimal(BigInteger.valueOf(unscaledVal), scale);
    }

    /**
     * Translates a {@code long} value into a {@code BigDecimal}
     * with a scale of zero.  This {@literal "static factory method"}
     * is provided in preference to a ({@code long}) constructor
     * because it allows for reuse of frequently used
     * {@code BigDecimal} values.
     *
     * @param val value of the {@code BigDecimal}.
     * @return a {@code BigDecimal} whose value is {@code val}.
     */
    public static BigDecimal valueOf(long val) {
        return valueOf(val, 0);
    }

    /**
     * Translates a {@code double} into a {@code BigDecimal}, using
     * the {@code double}'s canonical string representation provided
     * by the {@link Double#toString(double)} method.
     *
     * <p><b>Note:</b> This is generally the preferred way to convert
     * a {@code double} (or {@code float}) into a
     * {@code BigDecimal}, as the value returned is equal to that
     * resulting from constructing a {@code BigDecimal} from the
     * result of using {@link Double#toString(double)}.
     *
     * @param  val {@code double} to convert to a {@code BigDecimal}.
     * @return a {@code BigDecimal} whose value is equal to or approximately
     *         equal to the value of {@code val}.
     * @throws NumberFormatException if {@code val} is infinite or NaN.
     * @since  1.5
     */
    public static BigDecimal valueOf(double val) {
        // Reminder: a zero double returns '0.0', so we cannot fastpath
        // to use the constant ZERO.  This might be important enough to
        // justify a factory approach, a cache, or a few private
        // constants, later.
        return new BigDecimal(Double.toString(val));
    }

    // Arithmetic Operations
    /**
     * Returns a {@code BigDecimal} whose value is {@code (this +
     * augend)}, and whose scale is {@code max(this.scale(),
     * augend.scale())}.
     *
     * @param  augend value to be added to this {@code BigDecimal}.
     * @return {@code this + augend}
     */
    public BigDecimal add(BigDecimal augend) {
        BigDecimal arg[] = {this, augend};
        matchScale(arg);

        long x = arg[0].intCompact;
        long y = arg[1].intCompact;

        // Might be able to do a more clever check incorporating the
        // inflated check into the overflow computation.
        if (x != INFLATED && y != INFLATED) {
            long sum = x + y;
            /*
             * If the sum is not an overflowed value, continue to use
             * the compact representation.  if either of x or y is
             * INFLATED, the sum should also be regarded as an
             * overflow.  See "Hacker's Delight" section 2-12 for
             * explanation of the overflow test.
             */
            if ( (((sum ^ x) & (sum ^ y)) >> 63) == 0L )        // not overflowed
                return BigDecimal.valueOf(sum, arg[0].scale);
        }
        return new BigDecimal(arg[0].inflate().intVal.add(arg[1].inflate().intVal), arg[0].scale);
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (this + augend)},
     * with rounding according to the context settings.
     *
     * If either number is zero and the precision setting is nonzero then
     * the other number, rounded if necessary, is used as the result.
     *
     * @param  augend value to be added to this {@code BigDecimal}.
     * @param  mc the context to use.
     * @return {@code this + augend}, rounded as necessary.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}.
     * @since  1.5
     */
    public BigDecimal add(BigDecimal augend, MathContext mc) {
        if (mc.precision == 0)
            return add(augend);
        BigDecimal lhs = this;

        // Could optimize if values are compact
        this.inflate();
        augend.inflate();

        // If either number is zero then the other number, rounded and
        // scaled if necessary, is used as the result.
        {
            boolean lhsIsZero = lhs.signum() == 0;
            boolean augendIsZero = augend.signum() == 0;

            if (lhsIsZero || augendIsZero) {
                int preferredScale = Math.max(lhs.scale(), augend.scale());
                BigDecimal result;

                // Could use a factory for zero instead of a new object
                if (lhsIsZero && augendIsZero)
                    return new BigDecimal(BigInteger.ZERO, 0, preferredScale);


                result = lhsIsZero ? augend.doRound(mc) : lhs.doRound(mc);

                if (result.scale() == preferredScale)
                    return result;
                else if (result.scale() > preferredScale)
                    return new BigDecimal(result.intVal, result.intCompact, result.scale).
                        stripZerosToMatchScale(preferredScale);
                else { // result.scale < preferredScale
                    int precisionDiff = mc.precision - result.precision();
                    int scaleDiff     = preferredScale - result.scale();

                    if (precisionDiff >= scaleDiff)
                        return result.setScale(preferredScale); // can achieve target scale
                    else
                        return result.setScale(result.scale() + precisionDiff);
                }
            }
        }

        long padding = (long)lhs.scale - augend.scale;
        if (padding != 0) {        // scales differ; alignment needed
            BigDecimal arg[] = preAlign(lhs, augend, padding, mc);
            matchScale(arg);
            lhs    = arg[0];
            augend = arg[1];
        }

        return new BigDecimal(lhs.inflate().intVal.add(augend.inflate().intVal),
                              lhs.scale).doRound(mc);
    }

    /**
     * Returns an array of length two, the sum of whose entries is
     * equal to the rounded sum of the {@code BigDecimal} arguments.
     *
     * <p>If the digit positions of the arguments have a sufficient
     * gap between them, the value smaller in magnitude can be
     * condensed into a {@literal "sticky bit"} and the end result will
     * round the same way <em>if</em> the precision of the final
     * result does not include the high order digit of the small
     * magnitude operand.
     *
     * <p>Note that while strictly speaking this is an optimization,
     * it makes a much wider range of additions practical.
     *
     * <p>This corresponds to a pre-shift operation in a fixed
     * precision floating-point adder; this method is complicated by
     * variable precision of the result as determined by the
     * MathContext.  A more nuanced operation could implement a
     * {@literal "right shift"} on the smaller magnitude operand so
     * that the number of digits of the smaller operand could be
     * reduced even though the significands partially overlapped.
     */
    private BigDecimal[] preAlign(BigDecimal lhs, BigDecimal augend,
                                  long padding, MathContext mc) {
        assert padding != 0;
        BigDecimal big;
        BigDecimal small;

        if (padding < 0) {     // lhs is big;   augend is small
            big   = lhs;
            small = augend;
        } else {               // lhs is small; augend is big
            big   = augend;
            small = lhs;
        }

        /*
         * This is the estimated scale of an ulp of the result; it
         * assumes that the result doesn't have a carry-out on a true
         * add (e.g. 999 + 1 => 1000) or any subtractive cancellation
         * on borrowing (e.g. 100 - 1.2 => 98.8)
         */
        long estResultUlpScale = (long)big.scale - big.precision() + mc.precision;

        /*
         * The low-order digit position of big is big.scale().  This
         * is true regardless of whether big has a positive or
         * negative scale.  The high-order digit position of small is
         * small.scale - (small.precision() - 1).  To do the full
         * condensation, the digit positions of big and small must be
         * disjoint *and* the digit positions of small should not be
         * directly visible in the result.
         */
        long smallHighDigitPos = (long)small.scale - small.precision() + 1;
        if (smallHighDigitPos > big.scale + 2 &&         // big and small disjoint
            smallHighDigitPos > estResultUlpScale + 2) { // small digits not visible
            small = BigDecimal.valueOf(small.signum(),
                                       this.checkScale(Math.max(big.scale, estResultUlpScale) + 3));
        }

        // Since addition is symmetric, preserving input order in
        // returned operands doesn't matter
        BigDecimal[] result = {big, small};
        return result;
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (this -
     * subtrahend)}, and whose scale is {@code max(this.scale(),
     * subtrahend.scale())}.
     *
     * @param  subtrahend value to be subtracted from this {@code BigDecimal}.
     * @return {@code this - subtrahend}
     */
    public BigDecimal subtract(BigDecimal subtrahend) {
        BigDecimal arg[] = {this, subtrahend};
        matchScale(arg);

        long x = arg[0].intCompact;
        long y = arg[1].intCompact;

        // Might be able to do a more clever check incorporating the
        // inflated check into the overflow computation.
        if (x != INFLATED && y != INFLATED) {
            long difference = x - y;
            /*
             * If the difference is not an overflowed value, continue
             * to use the compact representation.  if either of x or y
             * is INFLATED, the difference should also be regarded as
             * an overflow.  See "Hacker's Delight" section 2-12 for
             * explanation of the overflow test.
             */
            if ( ((x ^ y) & (difference ^ x) ) >> 63 == 0L )    // not overflowed
                return BigDecimal.valueOf(difference, arg[0].scale);
        }
        return new BigDecimal(arg[0].inflate().intVal.subtract(arg[1].inflate().intVal),
                              arg[0].scale);
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (this - subtrahend)},
     * with rounding according to the context settings.
     *
     * If {@code subtrahend} is zero then this, rounded if necessary, is used as the
     * result.  If this is zero then the result is {@code subtrahend.negate(mc)}.
     *
     * @param  subtrahend value to be subtracted from this {@code BigDecimal}.
     * @param  mc the context to use.
     * @return {@code this - subtrahend}, rounded as necessary.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}.
     * @since  1.5
     */
    public BigDecimal subtract(BigDecimal subtrahend, MathContext mc) {
        if (mc.precision == 0)
            return subtract(subtrahend);
        // share the special rounding code in add()
        this.inflate();
        subtrahend.inflate();
        BigDecimal rhs = new BigDecimal(subtrahend.intVal.negate(), subtrahend.scale);
        rhs.precision = subtrahend.precision;
        return add(rhs, mc);
    }

    /**
     * Returns a {@code BigDecimal} whose value is <tt>(this &times;
     * multiplicand)</tt>, and whose scale is {@code (this.scale() +
     * multiplicand.scale())}.
     *
     * @param  multiplicand value to be multiplied by this {@code BigDecimal}.
     * @return {@code this * multiplicand}
     */
    public BigDecimal multiply(BigDecimal multiplicand) {
        long x = this.intCompact;
        long y = multiplicand.intCompact;
        int productScale = checkScale((long)scale+multiplicand.scale);

        // Might be able to do a more clever check incorporating the
        // inflated check into the overflow computation.
        if (x != INFLATED && y != INFLATED) {
            /*
             * If the product is not an overflowed value, continue
             * to use the compact representation.  if either of x or y
             * is INFLATED, the product should also be regarded as
             * an overflow.  See "Hacker's Delight" section 2-12 for
             * explanation of the overflow test.
             */
            long product = x * y;
            if ( !(y != 0L && product/y != x)  )        // not overflowed
                return BigDecimal.valueOf(product, productScale);
        }

        BigDecimal result = new BigDecimal(this.inflate().intVal.multiply(multiplicand.inflate().intVal), productScale);
        return result;
    }

    /**
     * Returns a {@code BigDecimal} whose value is <tt>(this &times;
     * multiplicand)</tt>, with rounding according to the context settings.
     *
     * @param  multiplicand value to be multiplied by this {@code BigDecimal}.
     * @param  mc the context to use.
     * @return {@code this * multiplicand}, rounded as necessary.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}.
     * @since  1.5
     */
    public BigDecimal multiply(BigDecimal multiplicand, MathContext mc) {
        if (mc.precision == 0)
            return multiply(multiplicand);
        BigDecimal lhs = this;
        return lhs.inflate().multiply(multiplicand.inflate()).doRound(mc);
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (this /
     * divisor)}, and whose scale is as specified.  If rounding must
     * be performed to generate a result with the specified scale, the
     * specified rounding mode is applied.
     *
     * <p>The new {@link #divide(BigDecimal, int, RoundingMode)} method
     * should be used in preference to this legacy method.
     *
     * @param  divisor value by which this {@code BigDecimal} is to be divided.
     * @param  scale scale of the {@code BigDecimal} quotient to be returned.
     * @param  roundingMode rounding mode to apply.
     * @return {@code this / divisor}
     * @throws ArithmeticException if {@code divisor} is zero,
     *         {@code roundingMode==ROUND_UNNECESSARY} and
     *         the specified scale is insufficient to represent the result
     *         of the division exactly.
     * @throws IllegalArgumentException if {@code roundingMode} does not
     *         represent a valid rounding mode.
     * @see    #ROUND_UP
     * @see    #ROUND_DOWN
     * @see    #ROUND_CEILING
     * @see    #ROUND_FLOOR
     * @see    #ROUND_HALF_UP
     * @see    #ROUND_HALF_DOWN
     * @see    #ROUND_HALF_EVEN
     * @see    #ROUND_UNNECESSARY
     */
    public BigDecimal divide(BigDecimal divisor, int scale, int roundingMode) {
        /*
         * IMPLEMENTATION NOTE: This method *must* return a new object
         * since dropDigits uses divide to generate a value whose
         * scale is then modified.
         */
        if (roundingMode < ROUND_UP || roundingMode > ROUND_UNNECESSARY)
            throw new IllegalArgumentException("Invalid rounding mode");
        /*
         * Rescale dividend or divisor (whichever can be "upscaled" to
         * produce correctly scaled quotient).
         * Take care to detect out-of-range scales
         */
        BigDecimal dividend;
        if (checkScale((long)scale + divisor.scale) >= this.scale) {
            dividend = this.setScale(scale + divisor.scale);
        } else {
            dividend = this;
            divisor = divisor.setScale(checkScale((long)this.scale - scale));
        }

        boolean compact = dividend.intCompact != INFLATED && divisor.intCompact != INFLATED;
        long div = INFLATED;
        long rem = INFLATED;;
        BigInteger q=null, r=null;

        if (compact) {
            div = dividend.intCompact / divisor.intCompact;
            rem = dividend.intCompact % divisor.intCompact;
        } else {
            // Do the division and return result if it's exact.
            BigInteger i[] = dividend.inflate().intVal.divideAndRemainder(divisor.inflate().intVal);
            q = i[0];
            r = i[1];
        }

        // Check for exact result
        if (compact) {
            if (rem == 0)
                return new BigDecimal(div, scale);
        } else {
            if (r.signum() == 0)
                return new BigDecimal(q, scale);
        }

        if (roundingMode == ROUND_UNNECESSARY)      // Rounding prohibited
            throw new ArithmeticException("Rounding necessary");

        /* Round as appropriate */
        int signum = dividend.signum() * divisor.signum(); // Sign of result
        boolean increment;
        if (roundingMode == ROUND_UP) {             // Away from zero
            increment = true;
        } else if (roundingMode == ROUND_DOWN) {    // Towards zero
            increment = false;
        } else if (roundingMode == ROUND_CEILING) { // Towards +infinity
            increment = (signum > 0);
        } else if (roundingMode == ROUND_FLOOR) {   // Towards -infinity
            increment = (signum < 0);
        } else { // Remaining modes based on nearest-neighbor determination
            int cmpFracHalf;
            if (compact) {
                 cmpFracHalf = longCompareTo(Math.abs(2*rem), Math.abs(divisor.intCompact));
            } else {
                // add(r) here is faster than multiply(2) or shiftLeft(1)
                cmpFracHalf= r.add(r).abs().compareTo(divisor.intVal.abs());
            }
            if (cmpFracHalf < 0) {         // We're closer to higher digit
                increment = false;
            } else if (cmpFracHalf > 0) {  // We're closer to lower digit
                increment = true;
            } else {                       // We're dead-center
                if (roundingMode == ROUND_HALF_UP)
                    increment = true;
                else if (roundingMode == ROUND_HALF_DOWN)
                    increment = false;
                else { // roundingMode == ROUND_HALF_EVEN
                    if (compact)
                        increment = (div & 1L) != 0L;
                    else
                        increment = q.testBit(0);   // true iff q is odd
                }
            }
        }

        if (compact) {
            if (increment)
                div += signum; // guaranteed not to overflow
            return new BigDecimal(div, scale);
        } else {
            return (increment
                    ? new BigDecimal(q.add(BigInteger.valueOf(signum)), scale)
                    : new BigDecimal(q, scale));
        }
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (this /
     * divisor)}, and whose scale is as specified.  If rounding must
     * be performed to generate a result with the specified scale, the
     * specified rounding mode is applied.
     *
     * @param  divisor value by which this {@code BigDecimal} is to be divided.
     * @param  scale scale of the {@code BigDecimal} quotient to be returned.
     * @param  roundingMode rounding mode to apply.
     * @return {@code this / divisor}
     * @throws ArithmeticException if {@code divisor} is zero,
     *         {@code roundingMode==RoundingMode.UNNECESSARY} and
     *         the specified scale is insufficient to represent the result
     *         of the division exactly.
     * @since 1.5
     */
    public BigDecimal divide(BigDecimal divisor, int scale, RoundingMode roundingMode) {
        return divide(divisor, scale, roundingMode.oldMode);
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (this /
     * divisor)}, and whose scale is {@code this.scale()}.  If
     * rounding must be performed to generate a result with the given
     * scale, the specified rounding mode is applied.
     *
     * <p>The new {@link #divide(BigDecimal, RoundingMode)} method
     * should be used in preference to this legacy method.
     *
     * @param  divisor value by which this {@code BigDecimal} is to be divided.
     * @param  roundingMode rounding mode to apply.
     * @return {@code this / divisor}
     * @throws ArithmeticException if {@code divisor==0}, or
     *         {@code roundingMode==ROUND_UNNECESSARY} and
     *         {@code this.scale()} is insufficient to represent the result
     *         of the division exactly.
     * @throws IllegalArgumentException if {@code roundingMode} does not
     *         represent a valid rounding mode.
     * @see    #ROUND_UP
     * @see    #ROUND_DOWN
     * @see    #ROUND_CEILING
     * @see    #ROUND_FLOOR
     * @see    #ROUND_HALF_UP
     * @see    #ROUND_HALF_DOWN
     * @see    #ROUND_HALF_EVEN
     * @see    #ROUND_UNNECESSARY
     */
    public BigDecimal divide(BigDecimal divisor, int roundingMode) {
            return this.divide(divisor, scale, roundingMode);
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (this /
     * divisor)}, and whose scale is {@code this.scale()}.  If
     * rounding must be performed to generate a result with the given
     * scale, the specified rounding mode is applied.
     *
     * @param  divisor value by which this {@code BigDecimal} is to be divided.
     * @param  roundingMode rounding mode to apply.
     * @return {@code this / divisor}
     * @throws ArithmeticException if {@code divisor==0}, or
     *         {@code roundingMode==RoundingMode.UNNECESSARY} and
     *         {@code this.scale()} is insufficient to represent the result
     *         of the division exactly.
     * @since 1.5
     */
    public BigDecimal divide(BigDecimal divisor, RoundingMode roundingMode) {
        return this.divide(divisor, scale, roundingMode.oldMode);
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (this /
     * divisor)}, and whose preferred scale is {@code (this.scale() -
     * divisor.scale())}; if the exact quotient cannot be
     * represented (because it has a non-terminating decimal
     * expansion) an {@code ArithmeticException} is thrown.
     *
     * @param  divisor value by which this {@code BigDecimal} is to be divided.
     * @throws ArithmeticException if the exact quotient does not have a
     *         terminating decimal expansion
     * @return {@code this / divisor}
     * @since 1.5
     * @author Joseph D. Darcy
     */
    public BigDecimal divide(BigDecimal divisor) {
        /*
         * Handle zero cases first.
         */
        if (divisor.signum() == 0) {   // x/0
            if (this.signum() == 0)    // 0/0
                throw new ArithmeticException("Division undefined");  // NaN
            throw new ArithmeticException("Division by zero");
        }

        // Calculate preferred scale
        int preferredScale = (int)Math.max(Math.min((long)this.scale() - divisor.scale(),
                                                    Integer.MAX_VALUE), Integer.MIN_VALUE);
        if (this.signum() == 0)        // 0/y
            return new BigDecimal(0, preferredScale);
        else {
            this.inflate();
            divisor.inflate();
            /*
             * If the quotient this/divisor has a terminating decimal
             * expansion, the expansion can have no more than
             * (a.precision() + ceil(10*b.precision)/3) digits.
             * Therefore, create a MathContext object with this
             * precision and do a divide with the UNNECESSARY rounding
             * mode.
             */
            MathContext mc = new MathContext( (int)Math.min(this.precision() +
                                                            (long)Math.ceil(10.0*divisor.precision()/3.0),
                                                            Integer.MAX_VALUE),
                                              RoundingMode.UNNECESSARY);
            BigDecimal quotient;
            try {
                quotient = this.divide(divisor, mc);
            } catch (ArithmeticException e) {
                throw new ArithmeticException("Non-terminating decimal expansion; " +
                                              "no exact representable decimal result.");
            }

            int quotientScale = quotient.scale();

            // divide(BigDecimal, mc) tries to adjust the quotient to
            // the desired one by removing trailing zeros; since the
            // exact divide method does not have an explicit digit
            // limit, we can add zeros too.

            if (preferredScale > quotientScale)
                return quotient.setScale(preferredScale);

            return quotient;
        }
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (this /
     * divisor)}, with rounding according to the context settings.
     *
     * @param  divisor value by which this {@code BigDecimal} is to be divided.
     * @param  mc the context to use.
     * @return {@code this / divisor}, rounded as necessary.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY} or
     *         {@code mc.precision == 0} and the quotient has a
     *         non-terminating decimal expansion.
     * @since  1.5
     */
    public BigDecimal divide(BigDecimal divisor, MathContext mc) {
        if (mc.precision == 0)
            return divide(divisor);
        BigDecimal lhs = this.inflate();     // left-hand-side
        BigDecimal rhs = divisor.inflate();  // right-hand-side
        BigDecimal result;                   // work

        long preferredScale = (long)lhs.scale() - rhs.scale();

        // Now calculate the answer.  We use the existing
        // divide-and-round method, but as this rounds to scale we have
        // to normalize the values here to achieve the desired result.
        // For x/y we first handle y=0 and x=0, and then normalize x and
        // y to give x' and y'
