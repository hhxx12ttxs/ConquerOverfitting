/*
 * Copyright (c) 2007-2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package javax.time.extra;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReferenceArray;

import javax.time.CalendricalException;
import javax.time.DateTimes;
import javax.time.LocalDate;
import javax.time.calendrical.CalendricalObject;
import javax.time.calendrical.DateAdjuster;
import javax.time.calendrical.DateTimeField;
import javax.time.calendrical.LocalDateTimeField;

/**
 * A representation of a day-of-month in the ISO-8601 calendar system.
 * <p>
 * DayOfMonth is an immutable time field that can only store a day-of-month.
 * It is a type-safe way of representing a day-of-month in an application.
 * <p>
 * Static factory methods allow you to construct instances.
 * The day-of-month may be queried using getValue().
 * 
 * <h4>Implementation notes</h4>
 * This class is immutable and thread-safe.
 */
public final class DayOfMonth
        implements Comparable<DayOfMonth>, DateAdjuster, Serializable {

    /**
     * A serialization identifier for this instance.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Cache of singleton instances.
     */
    private static final AtomicReferenceArray<DayOfMonth> CACHE = new AtomicReferenceArray<DayOfMonth>(31);

    /**
     * The day-of-month being represented, from 1 to 31.
     */
    private final int dayOfMonth;

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code DayOfMonth}.
     * <p>
     * A day-of-month object represents one of the 31 days of the month, from
     * 1 to 31.
     *
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @return the day-of-month, not null
     * @throws CalendricalException if the day-of-month is invalid
     */
    public static DayOfMonth of(int dayOfMonth) {
        try {
            DayOfMonth result = CACHE.get(--dayOfMonth);
            if (result == null) {
                DayOfMonth temp = new DayOfMonth(dayOfMonth + 1);
                CACHE.compareAndSet(dayOfMonth, null, temp);
                result = CACHE.get(dayOfMonth);
            }
            return result;
        } catch (IndexOutOfBoundsException ex) {
            throw new CalendricalException("Invalid value for DayOfYear: " + ++dayOfMonth);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code DayOfMonth} from a calendrical.
     * <p>
     * A calendrical represents some form of date and time information.
     * This factory converts the arbitrary calendrical to an instance of {@code DayOfMonth}.
     * 
     * @param calendrical  the calendrical to convert, not null
     * @return the day-of-month, not null
     * @throws CalendricalException if unable to convert to a {@code DayOfMonth}
     */
    public static DayOfMonth from(CalendricalObject calendrical) {
        LocalDate date = LocalDate.from(calendrical);
        return DayOfMonth.of(date.getDayOfMonth());
    }

    //-----------------------------------------------------------------------
    /**
     * Constructs an instance with the specified day-of-month.
     *
     * @param dayOfMonth  the day-of-month to represent
     */
    private DayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    /**
     * Resolve the singleton.
     *
     * @return the singleton, never null
     */
    private Object readResolve() {
        return of(dayOfMonth);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the field that defines how the day-of-month field operates.
     * <p>
     * The field provides access to the minimum and maximum values, and a
     * generic way to access values within a calendrical.
     *
     * @return the day-of-month field, never null
     */
    public DateTimeField getField() {
        return LocalDateTimeField.DAY_OF_MONTH;
    }

    /**
     * Gets the day-of-month value.
     *
     * @return the day-of-month, from 1 to 31
     */
    public int getValue() {
        return dayOfMonth;
    }

    //-----------------------------------------------------------------------
    /**
     * Adjusts a date to have the value of this day-of-month, returning a new date.
     * <p>
     * If the day-of-month is invalid for the year and month then it will be changed
     * to the last valid date for the month.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param date  the date to be adjusted, not null
     * @return the adjusted date, never null
     */
    public LocalDate adjustDate(LocalDate date) {
        return date.withDayOfMonth(dayOfMonth);
    }

    //-----------------------------------------------------------------------
    /**
     * Compares this day-of-month instance to another.
     *
     * @param otherDayOfMonth  the other day-of-month instance, not null
     * @return the comparator value, negative if less, positive if greater
     * @throws NullPointerException if otherDayOfMonth is null
     */
    public int compareTo(DayOfMonth otherDayOfMonth) {
        return DateTimes.safeCompare(dayOfMonth, otherDayOfMonth.dayOfMonth);
    }

    //-----------------------------------------------------------------------
    /**
     * Is this instance equal to that specified, evaluating the day-of-month.
     *
     * @param otherDayOfMonth  the other day-of-month instance, null returns false
     * @return true if the day-of-month is the same
     */
    @Override
    public boolean equals(Object otherDayOfMonth) {
        return this == otherDayOfMonth;
    }

    /**
     * A hash code for the day-of-month object.
     *
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
        return dayOfMonth;
    }

    /**
     * A string describing the day-of-month object.
     *
     * @return a string describing this object
     */
    @Override
    public String toString() {
        return "DayOfMonth=" + getValue();
    }

//  /**
//  * A map holding the maximum number of days per month.
//  */
// private static final Map<MonthOfYear, Days> STANDARD_DAYS_IN_MONTH = new EnumMap<MonthOfYear, Days>(MonthOfYear.class);
// /**
//  * A map holding the maximum number of days per month.
//  */
// private static final Map<MonthOfYear, Days> LEAP_YEAR_DAYS_IN_MONTH = new EnumMap<MonthOfYear, Days>(MonthOfYear.class);
// static {
//     STANDARD_DAYS_IN_MONTH.put(MonthOfYear.JANUARY, days(31));
//     STANDARD_DAYS_IN_MONTH.put(MonthOfYear.FEBRUARY, days(28));
//     STANDARD_DAYS_IN_MONTH.put(MonthOfYear.MARCH, days(31));
//     STANDARD_DAYS_IN_MONTH.put(MonthOfYear.APRIL, days(30));
//     STANDARD_DAYS_IN_MONTH.put(MonthOfYear.MAY, days(31));
//     STANDARD_DAYS_IN_MONTH.put(MonthOfYear.JUNE, days(30));
//     STANDARD_DAYS_IN_MONTH.put(MonthOfYear.JULY, days(31));
//     STANDARD_DAYS_IN_MONTH.put(MonthOfYear.AUGUST, days(31));
//     STANDARD_DAYS_IN_MONTH.put(MonthOfYear.SEPTEMBER, days(30));
//     STANDARD_DAYS_IN_MONTH.put(MonthOfYear.OCTOBER, days(31));
//     STANDARD_DAYS_IN_MONTH.put(MonthOfYear.NOVEMBER, days(30));
//     STANDARD_DAYS_IN_MONTH.put(MonthOfYear.DECEMBER, days(31));
//     LEAP_YEAR_DAYS_IN_MONTH.putAll(STANDARD_DAYS_IN_MONTH);
//     LEAP_YEAR_DAYS_IN_MONTH.put(MonthOfYear.FEBRUARY, days(29));
// }

}

