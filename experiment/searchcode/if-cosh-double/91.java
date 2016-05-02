/*
 * This file is part of Cadmium.
 * Copyright (C) 2007-2010 Xavier Clerc.
 *
 * Cadmium is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cadmium is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.x9c.cadmium.primitives.stdlib;

import java.util.Formatter;
import java.util.Locale;

import fr.x9c.cadmium.kernel.Block;
import fr.x9c.cadmium.kernel.CodeRunner;
import fr.x9c.cadmium.kernel.Fail;
import fr.x9c.cadmium.kernel.Primitive;
import fr.x9c.cadmium.kernel.PrimitiveProvider;
import fr.x9c.cadmium.kernel.Value;

/**
 * Implements all primitives from 'floats.c'.
 *
 * @author <a href="mailto:cadmium@x9c.fr">Xavier Clerc</a>
 * @version 1.0
 * @since 1.0
 */
@PrimitiveProvider
public final class Floats {

    /** Value returned for <i>normal</i> float. */
    private static final Value FP_NORMAL = Value.ZERO;

    /** Value returned for a float very close to zero, with reduced precision. */
    private static final Value FP_SUBNORMAL = Value.ONE;

    /** Value returned for positive or negative zero. */
    private static final Value FP_ZERO = Value.TWO;

    /** Value returned for positive or negative infinity. */
    private static final Value FP_INFINITE = Value.createFromLong(3);

    /** Value returned for <i>not a number</i>. */
    private static final Value FP_NAN = Value.createFromLong(4);

    /**
     * No instance of this class.
     */
    private Floats() {
    } // end empty constructor

    /**
     * Converts a float value into a string value.
     * @param ctxt context
     * @param f format string
     * @param v float value to convert
     * @return the result of the conversion of <tt>v</tt>,
     *         using <tt>f</tt> format
     */
    @Primitive
    public static Value caml_format_float(final CodeRunner ctxt,
                                          final Value f,
                                          final Value v) {
        final double d = v.asBlock().asDouble();
        if (Double.isNaN(d)) {
            return Value.createFromBlock(Block.createString("nan"));
        } else if (Double.isInfinite(d)) {
            if (d < 0) {
                return Value.createFromBlock(Block.createString("-inf"));
            } else {
                return Value.createFromBlock(Block.createString("inf"));
            } // end if/else
        } // end if/elsif

        final StringBuilder sb = new StringBuilder();
        final Formatter fmt = new Formatter(sb, Locale.US);
        final String format = f.asBlock().asString();
        fmt.format(format, d);
        final boolean exp = (sb.indexOf("e") >= 0) || (sb.indexOf("E") >= 0);
        switch (format.charAt(format.length() - 1)) {
        case 'F':
            if (!exp) {
                while (sb.charAt(sb.length() - 1) == '0') {
                    sb.deleteCharAt(sb.length() - 1);
                } // end while
            } // end if
            break;
        case 'g':
        case 'G':
            if (!exp) {
                while (sb.charAt(sb.length() - 1) == '0') {
                    sb.deleteCharAt(sb.length() - 1);
                } // end while
            } // end if
            if (sb.charAt(sb.length() - 1) == '.') {
                sb.deleteCharAt(sb.length() - 1);
            } // end if
            break;
        default:
        } // end switch
        return Value.createFromBlock(Block.createString(fmt.toString()));
    } // end method 'caml_format_float(CodeRunner, Value, Value)'

    /**
     * Converts a string value into a float value.
     * @param ctxt context
     * @param v string value to convert
     * @return the float value corresponding to <tt>v</tt>
     * @throws Fail.Exception if a parsing error occurs
     */
    @Primitive
    public static Value caml_float_of_string(final CodeRunner ctxt,
                                             final Value v)
        throws Fail.Exception {
        final String s = v.asBlock().asString();
        final StringBuilder sb = new StringBuilder(s.length());
        final int len = s.length();
        for (int i = 0; i < len; i++) {
            final char ch = s.charAt(i);
            if (ch != '_') {
                sb.append(ch);
            } // end if
        } // end for
        if (sb.length() == 0) {
            Fail.failWith("float_of_string");
            return Value.UNIT; // never reached
        } // end if
        try {
            return Value.createFromBlock(Block.createDouble(Double.parseDouble(sb.toString())));
        } catch (final NumberFormatException nfe) {
            Fail.failWith("float_of_string");
            return Value.UNIT; // never reached
        } // end try/catch
    } // end method 'caml_float_of_string(CodeRunner, Value)'

    /**
     * Converts a float value into a long value.
     * @param ctxt context
     * @param f float value to convert
     * @return <tt>f</tt> as a long value
     */
    @Primitive
    public static Value caml_int_of_float(final CodeRunner ctxt,
                                          final Value f) {
        return Value.createFromLong((int) f.asBlock().asDouble());
    } // end method 'caml_int_of_float(CodeRunner, Value)'

    /**
     * Converts a long value into a float value.
     * @param ctxt context
     * @param n long value to convert
     * @return <tt>n</tt> as a float value
     */
    @Primitive
    public static Value caml_float_of_int(final CodeRunner ctxt,
                                          final Value n) {
        return Value.createFromBlock(Block.createDouble(n.asLong()));
    } // end method 'caml_float_of_int(CodeRunner, Value)'

    /**
     * Computes the opposite of a float value.
     * @param ctxt context
     * @param f float value
     * @return <tt>-f</tt>
     */
    @Primitive
    public static Value caml_neg_float(final CodeRunner ctxt,
                                       final Value f) {
        return Value.createFromBlock(Block.createDouble(-f.asBlock().asDouble()));
    } // end method 'caml_neg_float(CodeRunner, Value)'

    /**
     * Computes the absolute value of a float value.
     * @param ctxt context
     * @param f float value
     * @return the absolute value of <tt>f</tt>
     */
    @Primitive
    public static Value caml_abs_float(final CodeRunner ctxt,
                                       final Value f) {
        return Value.createFromBlock(Block.createDouble(Math.abs(f.asBlock().asDouble())));
    } // end method 'caml_abs_float(CodeRunner, Value)'

    /**
     * Computes the sum of two float values.
     * @param ctxt context
     * @param f first float value
     * @param g second float value
     * @return <tt>f + g</tt>
     */
    @Primitive
    public static Value caml_add_float(final CodeRunner ctxt,
                                       final Value f,
                                       final Value g) {
        return Value.createFromBlock(Block.createDouble(f.asBlock().asDouble() + g.asBlock().asDouble()));
    } // end method 'caml_add_float(CodeRunner, Value, Value)'

    /**
     * Computes the difference between two float values.
     * @param ctxt context
     * @param f first float value
     * @param g second float value
     * @return <tt>f - g</tt>
     */
    @Primitive
    public static Value caml_sub_float(final CodeRunner ctxt,
                                       final Value f,
                                       final Value g) {
        return Value.createFromBlock(Block.createDouble(f.asBlock().asDouble() - g.asBlock().asDouble()));
    } // end method 'caml_sub_float(CodeRunner, Value, Value)'

    /**
     * Computes the product of two float values.
     * @param ctxt context
     * @param f first float value
     * @param g second float value
     * @return <tt>f * g</tt>
     */
    @Primitive
    public static Value caml_mul_float(final CodeRunner ctxt,
                                       final Value f,
                                       final Value g) {
        return Value.createFromBlock(Block.createDouble(f.asBlock().asDouble() * g.asBlock().asDouble()));
    } // end method 'caml_mul_float(CodeRunner, Value, Value')

    /**
     * Computes the division of two float values.
     * @param ctxt context
     * @param f first float value
     * @param g second float value
     * @return <tt>f / g</tt>
     */
    @Primitive
    public static Value caml_div_float(final CodeRunner ctxt,
                                       final Value f,
                                       final Value g) {
        return Value.createFromBlock(Block.createDouble(f.asBlock().asDouble() / g.asBlock().asDouble()));
    } // end method 'caml_div_float(CodeRunner, Value, Value)'

    /**
     * Computes <i>e ** f</i>, e being Euler number and f being the passed
     * parameter.
     * @param ctxt context
     * @param f float value
     * @return <i>e ** f</i>, e being Euler number
     *
     */
    @Primitive
    public static Value caml_exp_float(final CodeRunner ctxt,
                                       final Value f) {
        return Value.createFromBlock(Block.createDouble(Math.exp(f.asBlock().asDouble())));
    } // end method 'caml_exp_float(CodeRunner, Value)'

    /**
     * Computes the floor function (<i>i.e.</i> largest integer value that is
     * less than or equal to a given float).
     * @param ctxt context
     * @param f float value
     * @return <tt>floor(f)</tt>
     *
     */
    @Primitive
    public static Value caml_floor_float(final CodeRunner ctxt,
                                         final Value f) {
        return Value.createFromBlock(Block.createDouble(Math.floor(f.asBlock().asDouble())));
    } // end method 'caml_floor_float(CodeRunner, Value)'

    /**
     * Computes the remainder of <tt>f / g</tt>.
     * @param ctxt context
     * @param f dividend
     * @param g divisor
     * @return the remainder of <tt>f / g</tt>
     */
    @Primitive
    public static Value caml_fmod_float(final CodeRunner ctxt,
                                        final Value f,
                                        final Value g) {
        return Value.createFromBlock(Block.createDouble(f.asBlock().asDouble() % g.asBlock().asDouble()));
    } // end method 'caml_fmod_float(CodeRunner, Value, Value)'

    /**
     * Decomposes a float into two parts: a mantissa (between 0.5 and 1)
     * and an exponent.
     * @param ctxt context
     * @param f float value to decompose
     * @return <i>(mantissa, exp)</i>
     *         such that <i>f = mantissa * (2 ** exp)</i>
     */
    @Primitive
    public static Value caml_frexp_float(final CodeRunner ctxt,
                                         final Value f) {
        double dbl = f.asBlock().asDouble();
        if (Double.isNaN(dbl) || Double.isInfinite(dbl) || (dbl == 0.0)) {
            final Block b =
                Block.createBlock(0,
                                  Value.createFromBlock(Block.createDouble(dbl)),
                                  Value.ZERO);
            return Value.createFromBlock(b);
        } // end if
        final double sign = Math.signum(dbl);
        dbl = Math.abs(dbl);
        int n = 0;
        if (dbl >= 1.0) {
            while (dbl >= 1.0) {
                dbl = dbl / 2.0;
                n++;
            } // end while
        } else if (dbl < 0.5) {
            while (dbl < 0.5) {
                dbl = dbl * 2.0;
                n--;
            } // end while
        } // end if/elsif
        dbl = dbl * sign;
        final Block b =
            Block.createBlock(0,
                              Value.createFromBlock(Block.createDouble(dbl)),
                              Value.createFromLong(n));
        return Value.createFromBlock(b);
    } // end method 'caml_frexp_float(CodeRunner, Value)'

    /**
     * Computes <tt>f * (2 ** n)</tt>.
     * @param ctxt context
     * @param f base
     * @param n exponent
     * @return <tt>f * (2 ** n)</tt>
     */
    @Primitive
    public static Value caml_ldexp_float(final CodeRunner ctxt,
                                         final Value f,
                                         final Value n) {
        return Value.createFromBlock(Block.createDouble(f.asBlock().asDouble() * Math.pow(2, n.asLong())));
    } // end method 'caml_ldexp_float(CodeRunner, Value, Value)'

    /**
     * Computes the natural logarithm of a float value.
     * @param ctxt context
     * @param f float value
     * @return <tt>ln(f)</tt>
     */
    @Primitive
    public static Value caml_log_float(final CodeRunner ctxt,
                                       final Value f) {
        return Value.createFromBlock(Block.createDouble(Math.log(f.asBlock().asDouble())));
    } // end method 'caml_log_float(CodeRunner, Value)'

    /**
     * Computes the base 10 logarithm of a float value.
     * @param ctxt context
     * @param f float value
     * @return <tt>log10(f)</tt>
     */
    @Primitive
    public static Value caml_log10_float(final CodeRunner ctxt,
                                         final Value f) {
        return Value.createFromBlock(Block.createDouble(Math.log10(f.asBlock().asDouble())));
    } // end method 'caml_log10_float(CodeRunner, Value)'

    /**
     * Splits a float into integer and decimals parts.
     * @param ctxt context
     * @param f float value to split
     * @return <i>a couple: (integer_part(f), decimal_part(f))</i>
     */
    @Primitive
    public static Value caml_modf_float(final CodeRunner ctxt,
                                        final Value f) {
        final double d = f.asBlock().asDouble();
        final double dbl = Math.abs(d);
        final boolean neg = d < 0.0;
        final double v = Math.floor(dbl);
        final double n = dbl - v;
        final Block b =
            Block.createBlock(0,
                              Value.createFromBlock(Block.createDouble(neg ? -n : n)),
                              Value.createFromBlock(Block.createDouble(neg ? -v : v)));
        return Value.createFromBlock(b);
    } // end method 'caml_modf_float(CodeRunner, Value)'

    /**
     * Computes the square root of a float value.
     * @param ctxt context
     * @param f float value
     * @return <tt>f ** 0.5</tt>
     */
    @Primitive
    public static Value caml_sqrt_float(final CodeRunner ctxt,
                                        final Value f) {
        return Value.createFromBlock(Block.createDouble(Math.sqrt(f.asBlock().asDouble())));
    } // end method 'caml_sqrt_float(CodeRunner, Value)'

    /**
     * Computes the power of two floats values.
     * @param ctxt context
     * @param f first float value
     * @param g second float value
     * @return <tt>f ** g</tt>
     */
    @Primitive
    public static Value caml_power_float(final CodeRunner ctxt,
                                         final Value f,
                                         final Value g) {
        return Value.createFromBlock(Block.createDouble(Math.pow(f.asBlock().asDouble(), g.asBlock().asDouble())));
    } // end method 'caml_power_float(CodeRunner, Value, Value)'

    /**
     * Computes the sine of an angle.
     * @param ctxt context
     * @param f angle value
     * @return <tt>sin(f)</tt>
     */
    @Primitive
    public static Value caml_sin_float(final CodeRunner ctxt,
                                       final Value f) {
        return Value.createFromBlock(Block.createDouble(Math.sin(f.asBlock().asDouble())));
    } // end method 'caml_sin_float(CodeRunner, Value)'

    /**
     * Computes the hyperbolic sine of an angle.
     * @param ctxt context
     * @param f angle value
     * @return <tt>sinh(f)</tt>
     */
    @Primitive
    public static Value caml_sinh_float(final CodeRunner ctxt,
                                        final Value f) {
        return Value.createFromBlock(Block.createDouble(Math.sinh(f.asBlock().asDouble())));
    } // end method 'caml_sinh_float(CodeRunner, Value)'

    /**
     * Computes the cosine of an angle.
     * @param ctxt context
     * @param f angle value
     * @return <tt>cos(f)</tt>
     */
    @Primitive
    public static Value caml_cos_float(final CodeRunner ctxt,
                                       final Value f) {
        return Value.createFromBlock(Block.createDouble(Math.cos(f.asBlock().asDouble())));
    } // end method 'caml_cos_float(CodeRunner, Value)'

    /**
     * Computes the hyperbolic cosine of an angle.
     * @param ctxt context
     * @param f angle value
     * @return <tt>cosh(f)</tt>
     */
    @Primitive
    public static Value caml_cosh_float(final CodeRunner ctxt,
                                        final Value f) {
        return Value.createFromBlock(Block.createDouble(Math.cosh(f.asBlock().asDouble())));
    } // end method 'caml_cosh_float(CodeRunner, Value)'

    /**
     * Computes the tangent of an angle.
     * @param ctxt context
     * @param f angle value
     * @return <tt>tan(f)</tt>
     */
    @Primitive
    public static Value caml_tan_float(final CodeRunner ctxt,
                                       final Value f) {
        return Value.createFromBlock(Block.createDouble(Math.tan(f.asBlock().asDouble())));
    } // end method 'caml_tan_float(CodeRunner, Value)'

    /**
     * Computes the hyperbolic tangent of an angle.
     * @param ctxt context
     * @param f angle value
     * @return <tt>tanh(f)</tt>
     */
    @Primitive
    public static Value caml_tanh_float(final CodeRunner ctxt,
                                        final Value f) {
        return Value.createFromBlock(Block.createDouble(Math.tanh(f.asBlock().asDouble())));
    } // end method 'caml_tanh_float(CodeRunner, Value)'

    /**
     * Computes the arc sine of an angle.
     * @param ctxt context
     * @param f angle value (should be in -pi/2 .. pi/2)
     * @return <tt>asin(f)</tt>
     */
    @Primitive
    public static Value caml_asin_float(final CodeRunner ctxt,
                                        final Value f) {
        return Value.createFromBlock(Block.createDouble(Math.asin(f.asBlock().asDouble())));
    } // end method 'caml_asin_float(CodeRunner, Value)'

    /**
     * Computes the arc cosine of an angle.
     * @param ctxt context
     * @param f angle value (should be in 0 .. pi)
     * @return <tt>acos(f)</tt>
     */
    @Primitive
    public static Value caml_acos_float(final CodeRunner ctxt,
                                        final Value f) {
        return Value.createFromBlock(Block.createDouble(Math.acos(f.asBlock().asDouble())));
    } // end method 'caml_acos_float(CodeRunner, Value)'

    /**
     * Computes the arc tangent of an angle.
     * @param ctxt context
     * @param f angle value (should be in -pi/2 .. pi/2)
     * @return <tt>atan(f)</tt>
     */
    @Primitive
    public static Value caml_atan_float(final CodeRunner ctxt,
                                        final Value f) {
        return Value.createFromBlock(Block.createDouble(Math.atan(f.asBlock().asDouble())));
    } // end method 'caml_atan_float(CodeRunner, Value)'

    /**
     * Computes the distance between the origin and a given point in Cartesian
     * coordinates.
     * @param ctxt context
     * @param f ordinate of point
     * @param g abscissa of point
     * @return the distance between the origin and (g, f)
     */
    @Primitive
    public static Value caml_atan2_float(final CodeRunner ctxt,
                                         final Value f,
                                         final Value g) {
        return Value.createFromBlock(Block.createDouble(Math.atan2(f.asBlock().asDouble(), g.asBlock().asDouble())));
    } // end method 'caml_atan2_float(CodeRunner, Value, Value)'

    /**
     * Computes the ceil function (<i>i.e.</i> smallest integer value that is
     * greater than or equal to a givan float).
     * @param ctxt context
     * @param f float value
     * @return <tt>ceil(f)</tt>
     */
    @Primitive
    public static Value caml_ceil_float(final CodeRunner ctxt,
                                        final Value f) {
        return Value.createFromBlock(Block.createDouble(Math.ceil(f.asBlock().asDouble())));
    } // end method 'caml_ceil_float(CodeRunner, Value)'

    /**
     * Compares two floats.
     * @param ctxt context
     * @param f first float to compare
     * @param g second float to compare
     * @return {@link fr.x9c.cadmium.kernel.Value#TRUE} if the two values are
     *         equal,
     *         {@link fr.x9c.cadmium.kernel.Value#FALSE} otherwise
     */
    @Primitive
    public static Value caml_eq_float(final CodeRunner ctxt,
                                      final Value f,
                                      final Value g) {
        return f.asBlock().asDouble() == g.asBlock().asDouble()
            ? Value.TRUE
            : Value.FALSE;
    } // end method 'caml_eq_float(CodeRunner, Value, Value)'

    /**
     * Compares two floats.
     * @param ctxt context
     * @param f first float to compare
     * @param g second float to compare
     * @return {@link fr.x9c.cadmium.kernel.Value#TRUE} if the two values are
     *         different,
     *         {@link fr.x9c.cadmium.kernel.Value#FALSE} otherwise
     */
    @Primitive
    public static Value caml_neq_float(final CodeRunner ctxt,
                                       final Value f,
                                       final Value g) {
        return f.asBlock().asDouble() != g.asBlock().asDouble()
            ? Value.TRUE
            : Value.FALSE;
    } // end method 'caml_neq_float(CodeRunner, Value, Value)'

    /**
     * Compares two floats.
     * @param ctxt context
     * @param f first float to compare
     * @param g second float to compare
     * @return {@link fr.x9c.cadmium.kernel.Value#TRUE} if the first value is
     *         less than or equal to the second one,
     *         {@link fr.x9c.cadmium.kernel.Value#FALSE} otherwise
     */
    @Primitive
    public static Value caml_le_float(final CodeRunner ctxt,
                                      final Value f,
                                      final Value g) {
        return f.asBlock().asDouble() <= g.asBlock().asDouble()
            ? Value.TRUE
            : Value.FALSE;
    } // end method 'caml_le_float(CodeRunner, Value, Value)'

    /**
     * Compares two floats.
     * @param ctxt context
     * @param f first float to compare
     * @param g second float to compare
     * @return {@link fr.x9c.cadmium.kernel.Value#TRUE} if the first value is
     *         less than the second one,
     *         {@link fr.x9c.cadmium.kernel.Value#FALSE} otherwise
     */
    @Primitive
    public static Value caml_lt_float(final CodeRunner ctxt,
                                      final Value f,
                                      final Value g) {
        return f.asBlock().asDouble() < g.asBlock().asDouble()
            ? Value.TRUE
            : Value.FALSE;
    } // end method 'caml_lt_float(CodeRunner, Value, Value)'

    /**
     * Compares two floats.
     * @param ctxt context
     * @param f first float to compare
     * @param g second float to compare
     * @return {@link fr.x9c.cadmium.kernel.Value#TRUE} if the first value is
     *         greater than or equal to the second one,
     *         {@link fr.x9c.cadmium.kernel.Value#FALSE} otherwise
     */
    @Primitive
    public static Value caml_ge_float(final CodeRunner ctxt,
                                      final Value f,
                                      final Value g) {
        return f.asBlock().asDouble() >= g.asBlock().asDouble()
            ? Value.TRUE
            : Value.FALSE;
    } // end method 'caml_ge_float(CodeRunner, Value, Value)'

    /**
     * Compares two floats.
     * @param ctxt context
     * @param f first float to compare
     * @param g second float to compare
     * @return {@link fr.x9c.cadmium.kernel.Value#TRUE} if the first value is
     *         greater than the second one,
     *         {@link fr.x9c.cadmium.kernel.Value#FALSE} otherwise
     */
    @Primitive
    public static Value caml_gt_float(final CodeRunner ctxt,
                                      final Value f,
                                      final Value g) {
        return f.asBlock().asDouble() > g.asBlock().asDouble()
            ? Value.TRUE
            : Value.FALSE;
    } // end method 'caml_gt_float(CodeRunner, Value, Value)'

    /**
     * Compares two float values.
     * @param ctxt context
     * @param f first float value to compare
     * @param g second float value to compare
     * @return <tt>-1</tt>, <tt>0</tt>, <tt>1</tt> whether the first value is
     *         less than, equal to or greater than the second one
     */
    @Primitive
    public static Value caml_float_compare(final CodeRunner ctxt,
                                           final Value f,
                                           final Value g) {
        final double a = f.asBlock().asDouble();
        final double b = g.asBlock().asDouble();
        if (Double.isNaN(a)) {
            if (Double.isNaN(b)) {
                return Compare.EQUAL_VALUE;
            } else {
                return Compare.LESS_VALUE;
            } // end if/else
        } else {
            if (Double.isNaN(b)) {
                return Compare.GREATER_VALUE;
            } else {
                if (a < b) {
                    return Compare.LESS_VALUE;
                } else if (a > b) {
                    return Compare.GREATER_VALUE;
                } else {
                    return Compare.EQUAL_VALUE;
                } // end if/elsif/else
            } // end if/else
        } // end if/else
    } // end method 'caml_float_compare(CodeRunner, Value, Value)'

    /**
     * Classify a float value.
     * @param ctxt context
     * @param v value to classify
     * @return a long value, according to the followinf convention: <br/>
     *         <ul>
     *           <li><tt>0: </tt> <i>normal</i> float;</li>
     *           <li><tt>1: </tt> very close to zero, with reduced precision;</li>
     *           <li><tt>2: </tt> positive or negative zero;</li>
     *           <li><tt>3: </tt> positive or negative infinity;</li>
     *           <li><tt>4: </tt> <i>not a number</i>.</li>
     *         </ul>
     */
    @Primitive
    public static Value caml_classify_float(final CodeRunner ctxt,
                                            final Value v) {
        final long bits = Double.doubleToRawLongBits(v.asBlock().asDouble());
        int h = (int) ((bits >> 32) & 0xFFFFFFFFL);
        int l = (int) (bits & 0xFFFFFFFFL);
        l = l | (h & 0xFFFFF);
        h = h & 0x7FF00000;
        if ((h | l) == 0) {
            return Floats.FP_ZERO;
        } else if (h == 0) {
            return Floats.FP_SUBNORMAL;
        } else if (h == 0x7FF00000) {
            if (l == 0) {
                return Floats.FP_INFINITE;
            } else {
                return Floats.FP_NAN;
            } // end if/else
        } else {
            return Floats.FP_NORMAL;
        } // end if/elsif/else
    } // end method 'caml_classify_float(CodeRunner, Value)'

} // end class 'Floats'

