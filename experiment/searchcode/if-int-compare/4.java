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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.x9c.cadmium.kernel.Block;
import fr.x9c.cadmium.kernel.CodeRunner;
import fr.x9c.cadmium.kernel.Custom;
import fr.x9c.cadmium.kernel.Fail;
import fr.x9c.cadmium.kernel.Primitive;
import fr.x9c.cadmium.kernel.PrimitiveProvider;
import fr.x9c.cadmium.kernel.Value;

/**
 * Implements all primitives from 'compare.c'.
 *
 * @author <a href="mailto:cadmium@x9c.fr">Xavier Clerc</a>
 * @version 1.0
 * @since 1.0
 */
@PrimitiveProvider
public final class Compare {

    /** Returned when first value is inferior to second one. */
    public static final int LESS = -1;

    /** Returned when first value is inferior to second one. */
    public static final Value LESS_VALUE = Value.createFromLong(Compare.LESS);

    /** Returned when the two values are equal. */
    public static final int EQUAL = 0;

    /** Returned when the two values are equal. */
    public static final Value EQUAL_VALUE = Value.createFromLong(Compare.EQUAL);

    /** Returned when first value is superior to second one. */
    public static final int GREATER = 1;

    /** Returned when first value is superior to second one. */
    public static final Value GREATER_VALUE = Value.createFromLong(Compare.GREATER);

    /** Returned when the two values are unordered. */
    public static final int UNORDERED = 1 << 31;

    /** Returned when the two values are unordered. */
    public static final Value UNORDERED_VALUE = Value.createFromLong(Compare.UNORDERED);

    /**
     * No instance of this class.
     */
    private Compare() {
    } // end empty constructor

    /**
     * Compares two values.
     * @param d1 first value to compare
     * @param d2 second value to compare
     * @param total whether total ordering is wanted
     * @return {@link #LESS} if the first value is less than the second one,
     *         {@link #GREATER} if the first value is greater than the second one,
     *         {@link #EQUAL} if the two values are equal,
     *         {@link #UNORDERED} otherwise
     */
    private static int compareDoubles(final double d1,
                                      final double d2,
                                      final boolean total) {
        if (!Double.isNaN(d1) && !Double.isNaN(d2)) {
            if (d1 < d2) {
                return Compare.LESS;
            } else if (d1 > d2) {
                return Compare.GREATER;
            } else {
                return Compare.EQUAL;
            } // end if/elsif/else
        } else {
            if (!total) {
                return Compare.UNORDERED;
            } // end if
            if (!Double.isNaN(d1) && Double.isNaN(d2)) {
                return Compare.GREATER;
            } else if (Double.isNaN(d1) && !Double.isNaN(d2)) {
                return Compare.LESS;
            } else {
                return Compare.EQUAL;
            } // end if/elsif/else
        } // end if/else
    } // end method 'compareDoubles(double, double, boolean)'

    /**
     * Compares two values.
     * @param val1 first value to compare
     * @param val2 second value to compare
     * @param total whether total ordering is wanted
     * @return {@link #LESS} if the first value is less than the second one,
     *         {@link #GREATER} if the first value is greater than the second one,
     *         {@link #EQUAL} if the two values are equal,
     *         {@link #UNORDERED} otherwise
     * @throws Fail.Exception if any of the values is either abstract or
     *                        functional (and hence not comparable)
     */
    private static int compare(final Value val1,
                               final Value val2,
                               final boolean total)
        throws Fail.Exception {
        Value v1 = val1;
        Value v2 = val2;
        final List<Value> stack = new LinkedList<Value>();
        while (true) {
            if (((v1 == v2)
                 || (v1.isLong() && v2.isLong() && (v1.asLong() == v2.asLong()))
                 || (v1.isBlock() && v2.isBlock() && (v1.asBlock() == v2.asBlock()))) && total) { // next item
                if (stack.isEmpty()) {
                    return Compare.EQUAL;
                } else {
                    v2 = stack.remove(0);
                    v1 = stack.remove(0);
                } // end if/else
                continue;
            } // end if

            if (v1.isLong()) {
                if ((v1 == v2) || (v2.isLong() && (v1.asLong() == v2.asLong()))) { // next item
                    if (stack.isEmpty()) {
                        return Compare.EQUAL;
                    } else {
                        v2 = stack.remove(0);
                        v1 = stack.remove(0);
                    } // end if/else
                    continue;
                } // end if
                if (v2.isLong()) {
                    return v1.asLong() - v2.asLong();
                } // end if
                if (v2.asBlock().getTag() == Block.FORWARD_TAG) {
                    v2 = v2.asBlock().get(0);
                    continue;
                } // end if
                return Compare.LESS;
            } // end if

            if (v2.isLong()) {
                if (v1.asBlock().getTag() == Block.FORWARD_TAG) {
                    v1 = v1.asBlock().get(0);
                    continue;
                } // end if
                return Compare.GREATER;
            } // end if

            final Block b1 = v1.asBlock();
            final Block b2 = v2.asBlock();
            final int t1 = b1.getTag();
            final int t2 = b2.getTag();
            if (t1 == Block.FORWARD_TAG) {
                v1 = b1.get(0);
                continue;
            } // end if
            if (t2 == Block.FORWARD_TAG) {
                v2 = b2.get(0);
                continue;
            } // end if
            if (t1 != t2) {
                return t1 - t2;
            } // end if

            switch (t1) {
            case Block.STRING_TAG:
                if (v1 == v2) { // never true as already tested above
                    break;
                } // end if
                final String s1 = b1.asString();
                final String s2 = b2.asString();
                final int cmpStr = s1.compareTo(s2);
                if (cmpStr != 0) {
                    return cmpStr;
                } // end if
                break;
            case Block.DOUBLE_TAG:
                final int cmpDbl =
                    compareDoubles(b1.asDouble(), b2.asDouble(), total);
                if (cmpDbl != 0) {
                    return cmpDbl;
                } // end if
                break;
            case Block.DOUBLE_ARRAY_TAG:
                final int sz1 = b1.sizeDoubles();
                final int sz2 = b2.sizeDoubles();
                if (sz1 != sz2) {
                    return sz1 - sz2;
                } // end if
                for (int i = 0; i < sz1; i++) {
                    final int cmpDblElem =
                        compareDoubles(b1.getDouble(i),
                                       b2.getDouble(i),
                                       total);
                    if (cmpDblElem != 0) {
                        return cmpDblElem;
                    } // end if
                } // end for
                break;
            case Block.ABSTRACT_TAG:
                Fail.invalidArgument("equal: abstract value");
                break;
            case Block.CLOSURE_TAG:
            case Block.INFIX_TAG:
                Fail.invalidArgument("equal: functional value");
                break;
            case Block.OBJECT_TAG:
                final int oid1 = b1.get(1).asLong();
                final int oid2 = b2.get(1).asLong();
                if (oid1 != oid2) {
                    return oid1 - oid2;
                } // end if
                break;
            case Block.CUSTOM_TAG:
                final Custom.Operations ops = b1.getCustomOperations();
                if (!ops.isComparable()) {
                    Fail.invalidArgument("equal: abstract value");
                } // end if
                final AtomicBoolean unordered = new AtomicBoolean();
                final int cmpCust = ops.compare(v1, v2, unordered);
                if (unordered.get() && !total) {
                    return Compare.UNORDERED;
                } // end if
                if (cmpCust != 0) {
                    return cmpCust;
                } // end if
                break;
            default:
                final int wosz1 = b1.getWoSize();
                final int wosz2 = b2.getWoSize();
                if (wosz1 != wosz2) {
                    return wosz1 - wosz2;
                } // end if
                if (wosz1 == 0) {
                    break;
                } // end if
                for (int i = wosz1 - 1; i >= 1; i--) {
                    stack.add(0, b1.get(i));
                    stack.add(0, b2.get(i));
                } // end for
                v1 = b1.get(0);
                v2 = b2.get(0);
                continue;
            } // end switch

            // next item
            if (stack.isEmpty()) {
                return Compare.EQUAL;
            } else {
                v2 = stack.remove(0);
                v1 = stack.remove(0);
            } // end if/else
        } // end while
    } // end method 'compare(Value, Value, boolean)'

    /**
     * Compares two values.
     * @param ctxt context
     * @param v1 first value to compare
     * @param v2 second value to compare
     * @return -1 if the first value is less than the second one <br/>
     *          0 if the two values are equal <br/>
     *          1 if the first value is greater than the second one
     * @throws Fail.Exception if any of the values is either abstract or
     *                        functional (and hence not comparable)
     */
    @Primitive
    public static Value caml_compare(final CodeRunner ctxt,
                                     final Value v1,
                                     final Value v2)
        throws Fail.Exception {
        return Value.createFromLong(Integer.signum(compare(v1, v2, true)));
    } // end method 'caml_compare(CodeRunner, Value, Value)'

    /**
     * Compares two values.
     * @param ctxt context
     * @param v1 first value to compare
     * @param v2 second value to compare
     * @return {@link fr.x9c.cadmium.kernel.Value#TRUE} if the values are
     *         equal, {@link fr.x9c.cadmium.kernel.Value#FALSE} otherwise
     * @throws Fail.Exception if any of the values is either abstract or
     *                        functional (and hence not comparable)
     */
    @Primitive
    public static Value caml_equal(final CodeRunner ctxt,
                                   final Value v1,
                                   final Value v2)
        throws Fail.Exception {
        return compare(v1, v2, false) == 0 ? Value.TRUE : Value.FALSE;
    } // end method 'caml_equal(CodeRunner, Value, Value)'

    /**
     * Compares two values.
     * @param ctxt context
     * @param v1 first value to compare
     * @param v2 second value to compare
     * @return {@link fr.x9c.cadmium.kernel.Value#TRUE} if the values are
     *         different, {@link fr.x9c.cadmium.kernel.Value#FALSE} otherwise
     * @throws Fail.Exception if any of the values is either abstract or
     *                        functional (and hence not comparable)
     */
    @Primitive
    public static Value caml_notequal(final CodeRunner ctxt,
                                      final Value v1,
                                      final Value v2)
        throws Fail.Exception {
        return compare(v1, v2, false) != 0 ? Value.TRUE : Value.FALSE;
    } // end method 'caml_notequal(CodeRunner, Value, Value)'

    /**
     * Compares two values.
     * @param ctxt context
     * @param v1 first value to compare
     * @param v2 second value to compare
     * @return {@link fr.x9c.cadmium.kernel.Value#TRUE} if the first value is
     *         less than the second one,
     *         {@link fr.x9c.cadmium.kernel.Value#FALSE} otherwise
     * @throws Fail.Exception if any of the values is either abstract or
     *                        functional (and hence not comparable)
     */
    @Primitive
    public static Value caml_lessthan(final CodeRunner ctxt,
                                      final Value v1,
                                      final Value v2)
        throws Fail.Exception {
        return compare(v1, v2, false) < 0 ? Value.TRUE : Value.FALSE;
    } // end method 'caml_lessthan(CodeRunner, Value, Value)'

    /**
     * Compares two values.
     * @param ctxt context
     * @param v1 first value to compare
     * @param v2 second value to compare
     * @return {@link fr.x9c.cadmium.kernel.Value#TRUE} if the first value is
     *         less than or equal to the second one,
     *         {@link fr.x9c.cadmium.kernel.Value#FALSE} otherwise
     * @throws Fail.Exception if any of the values is either abstract or
     *                        functional (and hence not comparable)
     */
    @Primitive
    public static Value caml_lessequal(final CodeRunner ctxt,
                                       final Value v1,
                                       final Value v2)
        throws Fail.Exception {
        return compare(v1, v2, false) <= 0 ? Value.TRUE : Value.FALSE;
    } // end method 'caml_lessequal(CodeRunner, Value, Value)'

    /**
     * Compares two values.
     * @param ctxt context
     * @param v1 first value to compare
     * @param v2 second value to compare
     * @return {@link fr.x9c.cadmium.kernel.Value#TRUE} if the first value is
     *         greater than the second one,
     *         {@link fr.x9c.cadmium.kernel.Value#FALSE} otherwise
     * @throws Fail.Exception if any of the values is either abstract or
     *                        functional (and hence not comparable)
     */
    @Primitive
    public static Value caml_greaterthan(final CodeRunner ctxt,
                                         final Value v1,
                                         final Value v2)
        throws Fail.Exception {
        return compare(v1, v2, false) > 0 ? Value.TRUE : Value.FALSE;
    } // end method 'caml_greaterthan(CodeRunner, Value, Value)'

    /**
     * Compares two values.
     * @param ctxt context
     * @param v1 first value to compare
     * @param v2 second value to compare
     * @return {@link fr.x9c.cadmium.kernel.Value#TRUE} if the first value is
     *         greater than or equal to the second one,
     *         {@link fr.x9c.cadmium.kernel.Value#FALSE} otherwise
     * @throws Fail.Exception if any of the values is either abstract or
     *                        functional (and hence not comparable)
     */
    @Primitive
    public static Value caml_greaterequal(final CodeRunner ctxt,
                                          final Value v1,
                                          final Value v2)
        throws Fail.Exception {
        return compare(v1, v2, false) >= 0 ? Value.TRUE : Value.FALSE;
    } // end method 'caml_greaterequal(CodeRunner, Value, Value)'

} // end class 'Compare'

