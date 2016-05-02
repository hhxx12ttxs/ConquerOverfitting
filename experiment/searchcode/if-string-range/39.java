/* ************************************************************************
 * Hanoi - A typestate specification language & tools for the JVM
 * Copyright (C) 2010 Iain McGinniss
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 ************************************************************************ */
package uk.ac.gla.hanoi.model.conditions.ranges;

/**
 * Represents a set of values defined by interval notation, i.e. [0, 5) is the
 * set of all numbers from 0 (inclusive) to 5 (exclusive). If this example range
 * applies to the integer domain, then it represents the set {0,1,2,3,4}.
 */
public class Range implements Comparable<Range> {

    /**
     * The range including all possible values. See other methods in this class
     * for the expected behaviour when the "all" range is involved.
     */
    public static final Range ALL = new Range(RangeBoundary.MIN_LOWER, RangeBoundary.MAX_UPPER);

    /*
     * definition of the empty range, which is (\u221e,\u221e]. This odd range
     * definition is chosen so that all methods in this class do not require
     * special code to handle the empty range, and that the empty range produces
     * a consistent, defined result for each operation.
     */
    private static final RangeBoundary EMPTY_LWR = new RangeBoundary(PositiveInfinityRangeValue.INSTANCE, true, false);
    private static final RangeBoundary EMPTY_UPR = new RangeBoundary(PositiveInfinityRangeValue.INSTANCE, false, true);

    /**
     * The empty range. Any range which is logically empty, such as (0,0), is
     * equal to this range. See the other methods in this class for the expected
     * behaviour when the empty range is involved.
     */
    public static final Range EMPTY = new Range(EMPTY_LWR, EMPTY_UPR);

    private final RangeBoundary lower;
    private final RangeBoundary upper;

    public Range(RangeValue lowerBound, boolean lowerInclusive, RangeValue upperBound, boolean upperInclusive) {
        this(new RangeBoundary(lowerBound, true, lowerInclusive), new RangeBoundary(upperBound, false, upperInclusive));
    }

    protected Range(RangeBoundary lower, RangeBoundary upper) {
        if (lower.compareTo(upper) > 0 && lower.getBound().compareTo(upper.getBound()) > 0) {
            throw new IllegalArgumentException("lower bound is greater than upper bound");
        } else if (lower.getBound().equals(upper.getBound()) && (!lower.isInclusive() || !upper.isInclusive())) {
            this.lower = EMPTY_LWR;
            this.upper = EMPTY_UPR;
        } else {
            this.lower = lower;
            this.upper = upper;
        }
    }

    /**
     * @return whether or not the specified value is within the set of the
     *         values defined by this range.
     */
    public boolean contains(RangeValue value) {
        return lower.matches(value) && upper.matches(value);
    }

    /**
     * @return whether the provided range overlaps with this range, i.e. there
     *         exists at least one value where contains() will return true on
     *         both ranges.
     */
    public boolean overlapsWith(Range r) {
        return !intersect(r).isEmpty();
    }

    /**
     * Returns the range defining the space between this range and the provided
     * range, if such a gap exists. For instance, the gap between [0, 1) and [2,
     * 4] is [1, 2). There is no gap between [0,2] and (1, 3].
     * 
     * @return the range that defines the gap.
     */
    public Range gapTo(Range r) {
        if (this.compareTo(r) == 1) {
            return r.gapTo(this);
        }

        // we are to the left of r
        // the gap is the space between our upper bound and r's lower bound
        RangeBoundary gapLower = this.upper.adjacent();
        RangeBoundary gapUpper = r.lower.adjacent();

        if (gapLower.compareTo(gapUpper) > 0) {
            // the range contains no values, as the lower bound is greater than
            // the upper
            return Range.EMPTY;
        }

        return new Range(gapLower, gapUpper);
    }

    /**
     * @return the range of all values beneath the lower bound of this range. If
     *         this range extends to negative infinity (-&#x221e;), then we
     *         return {@link #EMPTY}. If this is the empty range, we return
     *         {@link #ALL}. If this is the {@link #ALL} range, we return
     *         {@link #EMPTY}.
     */
    public Range below() {
        return new Range(RangeBoundary.MIN_LOWER, this.lower.adjacent());
    }

    /**
     * Subtraction of one range to another can result in two ranges as the
     * result. This method returns the result range which is closer to the
     * negative infinity end of the number line.
     * <p>
     * Some examples: <br/>
     * [0,4].subtractLower([1,5]) = [0,1) <br/>
     * [0,4].subtractLower([0,3]) = &#x2205; (empty set)
     * [0,4].subtractLower([5,6]) = [0,4]
     * 
     * <p>
     * Represented pictorially:
     * 
     * <pre>
     * this:                         [------------------]
     * other:                            [---------]
     * this.subtractLower(other):    [---)
     * this.subtractUpper(other):                  (----]
     * </pre>
     * 
     * @param other
     *            the other range to be subtracted from this one, to determine
     *            the lower remainder.
     */
    public Range remainderLower(Range other) {
        if (this.lower.compareTo(other.lower) >= 0) {
            return Range.EMPTY;
        }

        RangeBoundary newUpper = this.upper.compareTo(other.lower.adjacent()) <= 0 ? this.upper : other.lower
                .adjacent();

        return new Range(this.lower, newUpper);
    }

    /**
     * Subtraction of one range to another can result in two ranges as the
     * result. This method returns the result range which is closer to the
     * positive infinity end of the number line.
     * <p>
     * Some examples: <br/>
     * [0,4].subtractUpper([1,3]) = (3,4] <br/>
     * [0,4].subtractUpper([2,4]) = &#x2205; (empty set)
     * [0,4].subtractUpper([-3,-1]) = [0,4]
     * 
     * <p>
     * Represented pictorially:
     * 
     * <pre>
     * this:                         [------------------]
     * other:                            [---------]
     * this.subtractLower(other):    [---)
     * this.subtractUpper(other):                  (----]
     * </pre>
     * 
     * @param other
     *            the other range to be subtracted from this one, to determine
     *            the upper remainder.
     */
    public Range remainderUpper(Range other) {
        if (this.upper.compareTo(other.upper) <= 0) {
            return Range.EMPTY;
        }

        RangeBoundary newLower =
                this.lower.compareTo(other.upper.adjacent()) >= 0
                        ? this.lower
                        : other.upper.adjacent();

        return new Range(newLower, this.upper);
    }

    /**
     * @return the range of all values above the upper bound of this range. If
     *         this range extends to positive infinity (&#x221e;), then we
     *         return {@link #empty}. If this is the empty range, we return
     *         {@link #EMPTY}.
     */
    public Range above() {
        return new Range(this.upper.adjacent(), RangeBoundary.MAX_UPPER);
    }

    public void toString(StringBuilder builder) {
        if (isEmpty()) {
            builder.append("\u2205");
            return;
        }

        lower.toString(builder);
        builder.append(", ");
        upper.toString(builder);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        toString(builder);
        return builder.toString();
    }

    @Override
    public int compareTo(Range o) {
        return this.lower.compareTo(o.lower);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Range) {
            Range other = (Range) obj;

            return lower.equals(other.lower)
                    && upper.equals(other.upper);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return lower.hashCode() ^ upper.hashCode();
    }

    public Range intersect(Range other) {
        RangeBoundary intersectLower = this.lower.compareTo(other.lower) >= 0 ? this.lower : other.lower;
        RangeBoundary intersectUpper = this.upper.compareTo(other.upper) <= 0 ? this.upper : other.upper;

        if (intersectLower.compareTo(intersectUpper) > 0) {
            return EMPTY;
        } else {
            return new Range(intersectLower, intersectUpper);
        }
    }

    public boolean isEmpty() {
        return this.equals(EMPTY);
    }
}

