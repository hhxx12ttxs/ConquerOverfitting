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
package javax.time;

import static javax.time.DateTimes.HOURS_PER_DAY;
import static javax.time.DateTimes.MICROS_PER_DAY;
import static javax.time.DateTimes.MILLIS_PER_DAY;
import static javax.time.DateTimes.MINUTES_PER_DAY;
import static javax.time.DateTimes.NANOS_PER_DAY;
import static javax.time.DateTimes.NANOS_PER_HOUR;
import static javax.time.DateTimes.NANOS_PER_MINUTE;
import static javax.time.DateTimes.NANOS_PER_SECOND;
import static javax.time.DateTimes.SECONDS_PER_DAY;

import java.io.Serializable;

import javax.time.calendrical.CalendricalAdjuster;
import javax.time.calendrical.CalendricalFormatter;
import javax.time.calendrical.CalendricalObject;
import javax.time.calendrical.DateAdjuster;
import javax.time.calendrical.DateTimeBuilder;
import javax.time.calendrical.DateTimeField;
import javax.time.calendrical.DateTimeObject;
import javax.time.calendrical.LocalDateTimeField;
import javax.time.calendrical.LocalDateTimeUnit;
import javax.time.calendrical.PeriodUnit;
import javax.time.calendrical.TimeAdjuster;
import javax.time.calendrical.ZoneResolver;
import javax.time.calendrical.ZoneResolvers;

/**
 * A date-time without a time-zone in the ISO-8601 calendar system,
 * such as {@code 2007-12-03T10:15:30}.
 * <p>
 * {@code LocalDateTime} is an immutable calendrical that represents a date-time, often
 * viewed as year-month-day-hour-minute-second. This object can also access other
 * fields such as day-of-year, day-of-week and week-of-year.
 * <p>
 * This class stores all date and time fields, to a precision of nanoseconds.
 * It does not store or represent a time-zone. For example, the value
 * "2nd October 2007 at 13:45.30.123456789" can be stored in an {@code LocalDateTime}.
 * 
 * <h4>Implementation notes</h4>
 * This class is immutable and thread-safe.
 */
public final class LocalDateTime
        implements DateTimeObject, Comparable<LocalDateTime>, Serializable {

    /**
     * Constant for the local date-time of midnight at the start of the minimum date.
     * This combines {@link LocalDate#MIN_DATE} and {@link LocalTime#MIN_TIME}.
     * This could be used by an application as a "far past" date-time.
     */
    public static final LocalDateTime MIN_DATE_TIME = LocalDateTime.of(LocalDate.MIN_DATE, LocalTime.MIN_TIME);
    /**
     * Constant for the local date-time just before midnight at the end of the maximum date.
     * This combines {@link LocalDate#MAX_DATE} and {@link LocalTime#MAX_TIME}.
     * This could be used by an application as a "far future" date-time.
     */
    public static final LocalDateTime MAX_DATE_TIME = LocalDateTime.of(LocalDate.MAX_DATE, LocalTime.MAX_TIME);

    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The date part.
     */
    private final LocalDate date;
    /**
     * The time part.
     */
    private final LocalTime time;

    //-----------------------------------------------------------------------
    /**
     * Obtains the current date-time from the system clock in the default time-zone.
     * <p>
     * This will query the {@link Clock#systemDefaultZone() system clock} in the default
     * time-zone to obtain the current date-time.
     * <p>
     * Using this method will prevent the ability to use an alternate clock for testing
     * because the clock is hard-coded.
     *
     * @return the current date-time using the system clock, not null
     */
    public static LocalDateTime now() {
        return now(Clock.systemDefaultZone());
    }

    /**
     * Obtains the current date-time from the specified clock.
     * <p>
     * This will query the specified clock to obtain the current date-time.
     * Using this method allows the use of an alternate clock for testing.
     * The alternate clock may be introduced using {@link Clock dependency injection}.
     *
     * @param clock  the clock to use, not null
     * @return the current date-time, not null
     */
    public static LocalDateTime now(Clock clock) {
        DateTimes.checkNotNull(clock, "Clock must not be null");
        // inline OffsetDateTime factory to avoid creating object and InstantProvider checks
        final Instant now = clock.instant();  // called once
        ZoneOffset offset = clock.getZone().getRules().getOffset(now);
        long localSeconds = now.getEpochSecond() + offset.getTotalSeconds();  // overflow caught later
        return create(localSeconds, now.getNanoOfSecond());
    }

    /**
     * Obtains an instance of {@code LocalDateTime} using seconds from the
     * local epoch of 1970-01-01T00:00:00.
     * <p>
     * The nanosecond field is set to zero.
     *
     * @param localSeconds  the number of seconds from the local epoch of 1970-01-01T00:00:00
     * @param nanoOfSecond  the nanosecond within the second, from 0 to 999,999,999
     * @return the local date-time, not null
     * @throws CalendricalException if the instant exceeds the supported date range
     */
    static LocalDateTime create(long localSeconds, int nanoOfSecond) {
        long epochDays = DateTimes.floorDiv(localSeconds, SECONDS_PER_DAY);
        int secsOfDay = DateTimes.floorMod(localSeconds, SECONDS_PER_DAY);
        LocalDate date = LocalDate.ofEpochDay(epochDays);
        LocalTime time = LocalTime.ofSecondOfDay(secsOfDay, nanoOfSecond);
        return LocalDateTime.of(date, time);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code LocalDateTime} from year, month and
     * day with the time set to midnight at the start of day.
     * <p>
     * The day must be valid for the year and month or an exception will be thrown.
     * <p>
     * The time fields will be set to zero by this factory method.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, not null
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @return the local date-time, not null
     * @throws CalendricalException if the value of any field is out of range
     * @throws CalendricalException if the day-of-month is invalid for the month-year
     */
    public static LocalDateTime ofMidnight(int year, MonthOfYear monthOfYear, int dayOfMonth) {
        LocalDate date = LocalDate.of(year, monthOfYear, dayOfMonth);
        return new LocalDateTime(date, LocalTime.MIDNIGHT);
    }

    /**
     * Obtains an instance of {@code LocalDateTime} from year, month and
     * day with the time set to midnight at the start of day.
     * <p>
     * The day must be valid for the year and month or an exception will be thrown.
     * <p>
     * The time fields will be set to zero by this factory method.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, from 1 (January) to 12 (December)
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @return the local date-time, not null
     * @throws CalendricalException if the value of any field is out of range
     * @throws CalendricalException if the day-of-month is invalid for the month-year
     */
    public static LocalDateTime ofMidnight(int year, int monthOfYear, int dayOfMonth) {
        LocalDate date = LocalDate.of(year, monthOfYear, dayOfMonth);
        return new LocalDateTime(date, LocalTime.MIDNIGHT);
    }

    /**
     * Obtains an instance of {@code LocalDateTime} from a date with the
     * time set to midnight at the start of day.
     * <p>
     * The day must be valid for the year and month or an exception will be thrown.
     * <p>
     * The time fields will be set to zero by this factory method.
     *
     * @param date  the local date, not null
     * @return the local date-time, not null
     */
    public static LocalDateTime ofMidnight(LocalDate date) {
        DateTimes.checkNotNull(date, "LocalDate must not be null");
        return new LocalDateTime(date, LocalTime.MIDNIGHT);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code LocalDateTime} from year, month,
     * day, hour and minute, setting the second and nanosecond to zero.
     * <p>
     * The day must be valid for the year and month, otherwise an exception will be thrown.
     * The second and nanosecond fields will be set to zero.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, not null
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @param hourOfDay  the hour-of-day to represent, from 0 to 23
     * @param minuteOfHour  the minute-of-hour to represent, from 0 to 59
     * @return the local date-time, not null
     * @throws CalendricalException if the value of any field is out of range
     * @throws CalendricalException if the day-of-month is invalid for the month-year
     */
    public static LocalDateTime of(
            int year, MonthOfYear monthOfYear, int dayOfMonth,
            int hourOfDay, int minuteOfHour) {
        LocalDate date = LocalDate.of(year, monthOfYear, dayOfMonth);
        LocalTime time = LocalTime.of(hourOfDay, minuteOfHour);
        return new LocalDateTime(date, time);
    }

    /**
     * Obtains an instance of {@code LocalDateTime} from year, month,
     * day, hour, minute and second, setting the nanosecond to zero.
     * <p>
     * The day must be valid for the year and month, otherwise an exception will be thrown.
     * The nanosecond field will be set to zero.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, not null
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @param hourOfDay  the hour-of-day to represent, from 0 to 23
     * @param minuteOfHour  the minute-of-hour to represent, from 0 to 59
     * @param secondOfMinute  the second-of-minute to represent, from 0 to 59
     * @return the local date-time, not null
     * @throws CalendricalException if the value of any field is out of range
     * @throws CalendricalException if the day-of-month is invalid for the month-year
     */
    public static LocalDateTime of(
            int year, MonthOfYear monthOfYear, int dayOfMonth,
            int hourOfDay, int minuteOfHour, int secondOfMinute) {
        LocalDate date = LocalDate.of(year, monthOfYear, dayOfMonth);
        LocalTime time = LocalTime.of(hourOfDay, minuteOfHour, secondOfMinute);
        return new LocalDateTime(date, time);
    }

    /**
     * Obtains an instance of {@code LocalDateTime} from year, month,
     * day, hour, minute, second and nanosecond.
     * <p>
     * The day must be valid for the year and month, otherwise an exception will be thrown.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, not null
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @param hourOfDay  the hour-of-day to represent, from 0 to 23
     * @param minuteOfHour  the minute-of-hour to represent, from 0 to 59
     * @param secondOfMinute  the second-of-minute to represent, from 0 to 59
     * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
     * @return the local date-time, not null
     * @throws CalendricalException if the value of any field is out of range
     * @throws CalendricalException if the day-of-month is invalid for the month-year
     */
    public static LocalDateTime of(
            int year, MonthOfYear monthOfYear, int dayOfMonth,
            int hourOfDay, int minuteOfHour, int secondOfMinute, int nanoOfSecond) {
        LocalDate date = LocalDate.of(year, monthOfYear, dayOfMonth);
        LocalTime time = LocalTime.of(hourOfDay, minuteOfHour, secondOfMinute, nanoOfSecond);
        return new LocalDateTime(date, time);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code LocalDateTime} from year, month,
     * day, hour and minute, setting the second and nanosecond to zero.
     * <p>
     * The day must be valid for the year and month, otherwise an exception will be thrown.
     * The second and nanosecond fields will be set to zero.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, from 1 (January) to 12 (December)
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @param hourOfDay  the hour-of-day to represent, from 0 to 23
     * @param minuteOfHour  the minute-of-hour to represent, from 0 to 59
     * @return the local date-time, not null
     * @throws CalendricalException if the value of any field is out of range
     * @throws CalendricalException if the day-of-month is invalid for the month-year
     */
    public static LocalDateTime of(
            int year, int monthOfYear, int dayOfMonth,
            int hourOfDay, int minuteOfHour) {
        LocalDate date = LocalDate.of(year, monthOfYear, dayOfMonth);
        LocalTime time = LocalTime.of(hourOfDay, minuteOfHour);
        return new LocalDateTime(date, time);
    }

    /**
     * Obtains an instance of {@code LocalDateTime} from year, month,
     * day, hour, minute and second, setting the nanosecond to zero.
     * <p>
     * The day must be valid for the year and month, otherwise an exception will be thrown.
     * The nanosecond field will be set to zero.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, from 1 (January) to 12 (December)
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @param hourOfDay  the hour-of-day to represent, from 0 to 23
     * @param minuteOfHour  the minute-of-hour to represent, from 0 to 59
     * @param secondOfMinute  the second-of-minute to represent, from 0 to 59
     * @return the local date-time, not null
     * @throws CalendricalException if the value of any field is out of range
     * @throws CalendricalException if the day-of-month is invalid for the month-year
     */
    public static LocalDateTime of(
            int year, int monthOfYear, int dayOfMonth,
            int hourOfDay, int minuteOfHour, int secondOfMinute) {
        LocalDate date = LocalDate.of(year, monthOfYear, dayOfMonth);
        LocalTime time = LocalTime.of(hourOfDay, minuteOfHour, secondOfMinute);
        return new LocalDateTime(date, time);
    }

    /**
     * Obtains an instance of {@code LocalDateTime} from year, month,
     * day, hour, minute, second and nanosecond.
     * <p>
     * The day must be valid for the year and month, otherwise an exception will be thrown.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, from 1 (January) to 12 (December)
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @param hourOfDay  the hour-of-day to represent, from 0 to 23
     * @param minuteOfHour  the minute-of-hour to represent, from 0 to 59
     * @param secondOfMinute  the second-of-minute to represent, from 0 to 59
     * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
     * @return the local date-time, not null
     * @throws CalendricalException if the value of any field is out of range
     * @throws CalendricalException if the day-of-month is invalid for the month-year
     */
    public static LocalDateTime of(
            int year, int monthOfYear, int dayOfMonth,
            int hourOfDay, int minuteOfHour, int secondOfMinute, int nanoOfSecond) {
        LocalDate date = LocalDate.of(year, monthOfYear, dayOfMonth);
        LocalTime time = LocalTime.of(hourOfDay, minuteOfHour, secondOfMinute, nanoOfSecond);
        return new LocalDateTime(date, time);
    }

    /**
     * Obtains an instance of {@code LocalDateTime} from a date and time.
     *
     * @param date  the local date, not null
     * @param time  the local time, not null
     * @return the local date-time, not null
     */
    public static LocalDateTime of(LocalDate date, LocalTime time) {
        DateTimes.checkNotNull(date, "LocalDate must not be null");
        DateTimes.checkNotNull(time, "LocalTime must not be null");
        return new LocalDateTime(date, time);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code LocalDateTime} from a calendrical.
     * <p>
     * A calendrical represents some form of date and time information.
     * This factory converts the arbitrary calendrical to an instance of {@code LocalDateTime}.
     * 
     * @param calendrical  the calendrical to convert, not null
     * @return the local date-time, not null
     * @throws CalendricalException if unable to convert to a {@code LocalDateTime}
     */
    public static LocalDateTime from(CalendricalObject calendrical) {
        LocalDateTime obj = calendrical.extract(LocalDateTime.class);
        return DateTimes.ensureNotNull(obj, "Unable to convert calendrical to LocalDateTime: ", calendrical.getClass());
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code LocalDateTime} from a text string such as {@code 2007-12-03T10:15:30}.
     * <p>
     * The string must represent a valid date-time and is parsed using
     * {@link javax.time.format.DateTimeFormatters#isoLocalDateTime()}.
     * Year, month, day-of-month, hour and minute are required.
     * Seconds and fractional seconds are optional.
     * Years outside the range 0000 to 9999 must be prefixed by the plus or minus symbol.
     *
     * @param text  the text to parse such as "2007-12-03T10:15:30", not null
     * @return the parsed local date-time, not null
     * @throws CalendricalParseException if the text cannot be parsed
     */
    public static LocalDateTime parse(CharSequence text) {
        throw new UnsupportedOperationException();
//        return DateTimeFormatters.isoLocalDateTime().parse(text, rule());
    }

    /**
     * Obtains an instance of {@code LocalDateTime} from a text string using a specific formatter.
     * <p>
     * The text is parsed using the formatter, returning a date-time.
     *
     * @param text  the text to parse, not null
     * @param formatter  the formatter to use, not null
     * @return the parsed local date-time, not null
     * @throws UnsupportedOperationException if the formatter cannot parse
     * @throws CalendricalParseException if the text cannot be parsed
     */
    public static LocalDateTime parse(String text, CalendricalFormatter formatter) {
        DateTimes.checkNotNull(formatter, "CalendricalFormatter must not be null");
        return formatter.parse(text, LocalDateTime.class);
    }

    //-----------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param date  the date part of the date-time, not null
     * @param time  the time part of the date-time, not null
     */
    private LocalDateTime(LocalDate date, LocalTime time) {
        this.date = date;
        this.time = time;
    }

    /**
     * Returns a copy of this date-time with the new date and time, checking
     * to see if a new object is in fact required.
     *
     * @param newDate  the date of the new date-time, not null
     * @param newTime  the time of the new date-time, not null
     * @return the date-time, not null
     */
    private LocalDateTime with(LocalDate newDate, LocalTime newTime) {
        if (date == newDate && time == newTime) {
            return this;
        }
        return new LocalDateTime(newDate, newTime);
    }

    //-----------------------------------------------------------------------
    @Override
    public long get(DateTimeField field) {
        if (field instanceof LocalDateTimeField) {
            if (((LocalDateTimeField) field).isDateField()) {
                return date.get(field);
            } else {
                return time.get(field);
            }
        }
        return field.get(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the year field.
     * <p>
     * This method returns the primitive {@code int} value for the year.
     *
     * @return the year, from MIN_YEAR to MAX_YEAR
     */
    public int getYear() {
        return date.getYear();
    }

    /**
     * Gets the month-of-year field from 1 to 12.
     * <p>
     * This method returns the month as an {@code int} from 1 to 12.
     * Application code is frequently clearer if the enum {@link MonthOfYear}
     * is used by calling {@link #getMonthOfYear()}.
     *
     * @return the month-of-year, from 1 to 12
     * @see #getMonthOfYear()
     */
    public int getMonth() {
        return date.getMonth();
    }

    /**
     * Gets the month-of-year field, which is an enum {@code MonthOfYear}.
     * <p>
     * This method returns the enum {@link MonthOfYear} for the month.
     * This avoids confusion as to what {@code int} values mean.
     * If you need access to the primitive {@code int} value then the enum
     * provides the {@link MonthOfYear#getValue() int value}.
     *
     * @return the month-of-year, not null
     * @see #getMonth()
     */
    public MonthOfYear getMonthOfYear() {
        return date.getMonthOfYear();
    }

    /**
     * Gets the day-of-month field.
     * <p>
     * This method returns the primitive {@code int} value for the day-of-month.
     *
     * @return the day-of-month, from 1 to 31
     */
    public int getDayOfMonth() {
        return date.getDayOfMonth();
    }

    /**
     * Gets the day-of-year field.
     * <p>
     * This method returns the primitive {@code int} value for the day-of-year.
     *
     * @return the day-of-year, from 1 to 365, or 366 in a leap year
     */
    public int getDayOfYear() {
        return date.getDayOfYear();
    }

    /**
     * Gets the day-of-week field, which is an enum {@code DayOfWeek}.
     * <p>
     * This method returns the enum {@link DayOfWeek} for the day-of-week.
     * This avoids confusion as to what {@code int} values mean.
     * If you need access to the primitive {@code int} value then the enum
     * provides the {@link DayOfWeek#getValue() int value}.
     * <p>
     * Additional information can be obtained from the {@code DayOfWeek}.
     * This includes textual names of the values.
     *
     * @return the day-of-week, not null
     */
    public DayOfWeek getDayOfWeek() {
        return date.getDayOfWeek();
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the hour-of-day field.
     *
     * @return the hour-of-day, from 0 to 23
     */
    public int getHourOfDay() {
        return time.getHourOfDay();
    }

    /**
     * Gets the minute-of-hour field.
     *
     * @return the minute-of-hour, from 0 to 59
     */
    public int getMinuteOfHour() {
        return time.getMinuteOfHour();
    }

    /**
     * Gets the second-of-minute field.
     *
     * @return the second-of-minute, from 0 to 59
     */
    public int getSecondOfMinute() {
        return time.getSecondOfMinute();
    }

    /**
     * Gets the nano-of-second field.
     *
     * @return the nano-of-second, from 0 to 999,999,999
     */
    public int getNanoOfSecond() {
        return time.getNanoOfSecond();
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the year is a leap year, according to the ISO proleptic
     * calendar system rules.
     * <p>
     * This method applies the current rules for leap years across the whole time-line.
     * In general, a year is a leap year if it is divisible by four without
     * remainder. However, years divisible by 100, are not leap years, with
     * the exception of years divisible by 400 which are.
     * <p>
     * For example, 1904 is a leap year it is divisible by 4.
     * 1900 was not a leap year as it is divisible by 100, however 2000 was a
     * leap year as it is divisible by 400.
     * <p>
     * The calculation is proleptic - applying the same rules into the far future and far past.
     * This is historically inaccurate, but is correct for the ISO-8601 standard.
     *
     * @return true if the year is leap, false otherwise
     */
    public boolean isLeapYear() {
        return date.isLeapYear();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this date-time with the specified field altered.
     * <p>
     * This method returns a new date-time based on this date-time with a new value for the specified field.
     * This can be used to change any field, for example to set the year, month of day-of-month.
     * <p>
     * In some cases, changing the specified field can cause the resulting date-time to become invalid,
     * such as changing the month from January to February would make the day-of-month 31 invalid.
     * In cases like this, the field is responsible for resolving the date. Typically it will choose
     * the previous valid date, which would be the last valid day of February in this example.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param field  the field to set in the returned date-time, not null
     * @param newValue  the new value of the field in the returned date-time, not null
     * @return a {@code LocalDateTime} based on this date-time with the specified field set, not null
     * @throws CalendricalException if the value is invalid
     */
    public LocalDateTime with(DateTimeField field, long newValue) {
        if (field instanceof LocalDateTimeField) {
            if (((LocalDateTimeField) field).isDateField()) {
                return with(date.with(field, newValue), time);
            } else {
                return with(date, time.with(field, newValue));
            }
        }
        return field.set(this, newValue);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code LocalDateTime} with the year altered.
     * The time does not affect the calculation and will be the same in the result.
     * If the day-of-month is invalid for the year, it will be changed to the last valid day of the month.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param year  the year to set in the returned date, from MIN_YEAR to MAX_YEAR
     * @return a {@code LocalDateTime} based on this date-time with the requested year, not null
     * @throws CalendricalException if the year value is invalid
     */
    public LocalDateTime withYear(int year) {
        return with(date.withYear(year), time);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the month-of-year altered.
     * The time does not affect the calculation and will be the same in the result.
     * If the day-of-month is invalid for the year, it will be changed to the last valid day of the month.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param monthOfYear  the month-of-year to set in the returned date, from 1 (January) to 12 (December)
     * @return a {@code LocalDateTime} based on this date-time with the requested month, not null
     * @throws CalendricalException if the month-of-year value is invalid
     */
    public LocalDateTime withMonthOfYear(int monthOfYear) {
        return with(date.withMonthOfYear(monthOfYear), time);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the month-of-year altered.
     * The time does not affect the calculation and will be the same in the result.
     * If the day-of-month is invalid for the year, it will be changed to the last valid day of the month.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param monthOfYear  the month-of-year to set in the returned date, not null
     * @return a {@code LocalDateTime} based on this date-time with the requested month, not null
     */
    public LocalDateTime with(MonthOfYear monthOfYear) {
        return with(date.with(monthOfYear), time);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the day-of-month altered.
     * If the resulting {@code LocalDateTime} is invalid, an exception is thrown.
     * The time does not affect the calculation and will be the same in the result.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfMonth  the day-of-month to set in the returned date, from 1 to 28-31
     * @return a {@code LocalDateTime} based on this date-time with the requested day, not null
     * @throws CalendricalException if the day-of-month value is invalid
     * @throws CalendricalException if the day-of-month is invalid for the month-year
     */
    public LocalDateTime withDayOfMonth(int dayOfMonth) {
        return with(date.withDayOfMonth(dayOfMonth), time);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the day-of-year altered.
     * If the resulting {@code LocalDateTime} is invalid, an exception is thrown.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfYear  the day-of-year to set in the returned date, from 1 to 365-366
     * @return a {@code LocalDateTime} based on this date with the requested day, not null
     * @throws CalendricalException if the day-of-year value is invalid
     * @throws CalendricalException if the day-of-year is invalid for the year
     */
    public LocalDateTime withDayOfYear(int dayOfYear) {
        return with(date.withDayOfYear(dayOfYear), time);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code LocalDateTime} with the date values altered.
     * <p>
     * This method will return a new instance with the same time fields,
     * but altered date fields.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, not null
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @return a {@code LocalDateTime} based on this date-time with the requested date, not null
     * @throws CalendricalException if any field value is invalid
     * @throws CalendricalException if the day-of-month is invalid for the month-year
     */
    public LocalDateTime withDate(int year, MonthOfYear monthOfYear, int dayOfMonth) {
        if (year == getYear() &&
                monthOfYear == getMonthOfYear() &&
                dayOfMonth == getDayOfMonth()) {
            return this;
        }
        LocalDate newDate = LocalDate.of(year, monthOfYear, dayOfMonth);
        return with(newDate, time);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the date values altered.
     * <p>
     * This method will return a new instance with the same time fields,
     * but altered date fields.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param monthOfYear  the month-of-year to represent, from 1 (January) to 12 (December)
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @return a {@code LocalDateTime} based on this date-time with the requested date, not null
     * @throws CalendricalException if any field value is invalid
     * @throws CalendricalException if the day-of-month is invalid for the month-year
     */
    public LocalDateTime withDate(int year, int monthOfYear, int dayOfMonth) {
        if (year == getYear() &&
                monthOfYear == getMonthOfYear().getValue() &&
                dayOfMonth == getDayOfMonth()) {
            return this;
        }
        LocalDate newDate = LocalDate.of(year, monthOfYear, dayOfMonth);
        return with(newDate, time);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code LocalDateTime} with the hour-of-day value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hourOfDay  the hour-of-day to represent, from 0 to 23
     * @return a {@code LocalDateTime} based on this date-time with the requested hour, not null
     * @throws CalendricalException if the hour value is invalid
     */
    public LocalDateTime withHourOfDay(int hourOfDay) {
        LocalTime newTime = time.withHourOfDay(hourOfDay);
        return with(date, newTime);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the minute-of-hour value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minuteOfHour  the minute-of-hour to represent, from 0 to 59
     * @return a {@code LocalDateTime} based on this date-time with the requested minute, not null
     * @throws CalendricalException if the minute value is invalid
     */
    public LocalDateTime withMinuteOfHour(int minuteOfHour) {
        LocalTime newTime = time.withMinuteOfHour(minuteOfHour);
        return with(date, newTime);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the second-of-minute value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param secondOfMinute  the second-of-minute to represent, from 0 to 59
     * @return a {@code LocalDateTime} based on this date-time with the requested second, not null
     * @throws CalendricalException if the second value is invalid
     */
    public LocalDateTime withSecondOfMinute(int secondOfMinute) {
        LocalTime newTime = time.withSecondOfMinute(secondOfMinute);
        return with(date, newTime);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the nano-of-second value altered.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
     * @return a {@code LocalDateTime} based on this date-time with the requested nanosecond, not null
     * @throws CalendricalException if the nano value is invalid
     */
    public LocalDateTime withNanoOfSecond(int nanoOfSecond) {
        LocalTime newTime = time.withNanoOfSecond(nanoOfSecond);
        return with(date, newTime);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the time values altered.
     * <p>
     * This method will return a new instance with the same date fields,
     * but altered time fields.
     * This is a shorthand for {@link #withTime(int,int,int,int)} and sets
     * the second and nanosecond fields to zero.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hourOfDay  the hour-of-day to represent, from 0 to 23
     * @param minuteOfHour  the minute-of-hour to represent, from 0 to 59
     * @return a {@code LocalDateTime} based on this date-time with the requested time, not null
     * @throws CalendricalException if any field value is invalid
     */
    public LocalDateTime withTime(int hourOfDay, int minuteOfHour) {
        return withTime(hourOfDay, minuteOfHour, 0, 0);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the time values altered.
     * <p>
     * This method will return a new instance with the same date fields,
     * but altered time fields.
     * This is a shorthand for {@link #withTime(int,int,int,int)} and sets
     * the nanosecond fields to zero.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hourOfDay  the hour-of-day to represent, from 0 to 23
     * @param minuteOfHour  the minute-of-hour to represent, from 0 to 59
     * @param secondOfMinute  the second-of-minute to represent, from 0 to 59
     * @return a {@code LocalDateTime} based on this date-time with the requested time, not null
     * @throws CalendricalException if any field value is invalid
     */
    public LocalDateTime withTime(int hourOfDay, int minuteOfHour, int secondOfMinute) {
        return withTime(hourOfDay, minuteOfHour, secondOfMinute, 0);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the time values altered.
     * <p>
     * This method will return a new instance with the same date fields,
     * but altered time fields.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hourOfDay  the hour-of-day to represent, from 0 to 23
     * @param minuteOfHour  the minute-of-hour to represent, from 0 to 59
     * @param secondOfMinute  the second-of-minute to represent, from 0 to 59
     * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
     * @return a {@code LocalDateTime} based on this date-time with the requested time, not null
     * @throws CalendricalException if any field value is invalid
     */
    public LocalDateTime withTime(int hourOfDay, int minuteOfHour, int secondOfMinute, int nanoOfSecond) {
        if (hourOfDay == getHourOfDay() && minuteOfHour == getMinuteOfHour() &&
                secondOfMinute == getSecondOfMinute() && nanoOfSecond == getNanoOfSecond()) {
            return this;
        }
        LocalTime newTime = LocalTime.of(hourOfDay, minuteOfHour, secondOfMinute, nanoOfSecond);
        return with(date, newTime);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this date-time with the specified duration added.
     * <p>
     * This adds the specified duration to this date-time, returning a new date-time.
     * <p>
     * The calculation is equivalent to using {@link #plusSeconds(long)} and
     * {@link #plusNanos(long)} on the two parts of the duration.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param duration  the duration to add, not null
     * @return a {@code LocalDateTime} based on this date-time with the duration added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDateTime plus(Duration duration) {
        return plusSeconds(duration.getSeconds()).plusNanos(duration.getNanoOfSecond());
    }

    /**
     * Returns a copy of this date-time with the specified period added.
     * <p>
     * This method returns a new date-time based on this time with the specified period added.
     * The calculation is delegated to the unit within the period.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param period  the period to add, not null
     * @return a {@code LocalDateTime} based on this date-time with the period added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDateTime plus(Period period) {
        return plus(period.getAmount(), period.getUnit());
    }

    /**
     * Returns a copy of this date-time with the specified period added.
     * <p>
     * This method returns a new date-time based on this date-time with the specified period added.
     * This can be used to add any period that is defined by a unit, for example to add years, months or days.
     * The unit is responsible for the details of the calculation, including the resolution
     * of any edge cases in the calculation.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param period  the amount of the unit to add to the returned date-time, not null
     * @param unit  the unit of the period to add, not null
     * @return a {@code LocalDateTime} based on this date-time with the specified period added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDateTime plus(long period, PeriodUnit unit) {
        if (unit instanceof LocalDateTimeUnit) {
            LocalDateTimeUnit f = (LocalDateTimeUnit) unit;
            switch (f) {
                case NANOS: return plusNanos(period);
                case MICROS: return plusDays(period / MICROS_PER_DAY).plusNanos((period % MICROS_PER_DAY) * 1000);
                case MILLIS: return plusDays(period / MILLIS_PER_DAY).plusNanos((period % MILLIS_PER_DAY) * 1000000);
                case SECONDS: return plusSeconds(period);
                case MINUTES: return plusMinutes(period);
                case HOURS: return plusHours(period);
                case HALF_DAYS: return plusDays(period / 256).plusHours((period % 256) * 12);  // no overflow (256 is multiple of 2)
            }
            return with(date.plus(period, unit), time);
        }
        return unit.add(this, period);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code LocalDateTime} with the specified period in years added.
     * <p>
     * This method adds the specified amount to the years field in three steps:
     * <ol>
     * <li>Add the input years to the year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * </ol>
     * <p>
     * For example, 2008-02-29 (leap year) plus one year would result in the
     * invalid date 2009-02-29 (standard year). Instead of returning an invalid
     * result, the last valid day of the month, 2009-02-28, is selected instead.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to add, may be negative
     * @return a {@code LocalDateTime} based on this date-time with the years added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDateTime plusYears(long years) {
        LocalDate newDate = date.plusYears(years);
        return with(newDate, time);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the specified period in months added.
     * <p>
     * This method adds the specified amount to the months field in three steps:
     * <ol>
     * <li>Add the input months to the month-of-year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * </ol>
     * <p>
     * For example, 2007-03-31 plus one month would result in the invalid date
     * 2007-04-31. Instead of returning an invalid result, the last valid day
     * of the month, 2007-04-30, is selected instead.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to add, may be negative
     * @return a {@code LocalDateTime} based on this date-time with the months added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDateTime plusMonths(long months) {
        LocalDate newDate = date.plusMonths(months);
        return with(newDate, time);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the specified period in weeks added.
     * <p>
     * This method adds the specified amount in weeks to the days field incrementing
     * the month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     * <p>
     * For example, 2008-12-31 plus one week would result in the 2009-01-07.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param weeks  the weeks to add, may be negative
     * @return a {@code LocalDateTime} based on this date-time with the weeks added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDateTime plusWeeks(long weeks) {
        LocalDate newDate = date.plusWeeks(weeks);
        return with(newDate, time);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the specified period in days added.
     * <p>
     * This method adds the specified amount to the days field incrementing the
     * month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     * <p>
     * For example, 2008-12-31 plus one day would result in the 2009-01-01.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to add, may be negative
     * @return a {@code LocalDateTime} based on this date-time with the days added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDateTime plusDays(long days) {
        LocalDate newDate = date.plusDays(days);
        return with(newDate, time);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the specified period in hours added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to add, may be negative
     * @return a {@code LocalDateTime} based on this date-time with the hours added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDateTime plusHours(long hours) {
        return plusWithOverflow(date, hours, 0, 0, 0, 1);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the specified period in minutes added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutes  the minutes to add, may be negative
     * @return a {@code LocalDateTime} based on this date-time with the minutes added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDateTime plusMinutes(long minutes) {
        return plusWithOverflow(date, 0, minutes, 0, 0, 1);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the specified period in seconds added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param seconds  the seconds to add, may be negative
     * @return a {@code LocalDateTime} based on this date-time with the seconds added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDateTime plusSeconds(long seconds) {
        return plusWithOverflow(date, 0, 0, seconds, 0, 1);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the specified period in nanoseconds added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanos  the nanos to add, may be negative
     * @return a {@code LocalDateTime} based on this date-time with the nanoseconds added, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDateTime plusNanos(long nanos) {
        return plusWithOverflow(date, 0, 0, 0, nanos, 1);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this date-time with the specified duration subtracted.
     * <p>
     * This subtracts the specified duration from this date-time, returning a new date-time.
     * <p>
     * The calculation is equivalent to using {@link #minusSeconds(long)} and
     * {@link #minusNanos(long)} on the two parts of the duration.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param duration  the duration to subtract, not null
     * @return a {@code LocalDateTime} based on this date-time with the duration subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDateTime minus(Duration duration) {
        return minusSeconds(duration.getSeconds()).minusNanos(duration.getNanoOfSecond());
    }

    /**
     * Returns a copy of this date-time with the specified period subtracted.
     * <p>
     * This method returns a new date-time based on this time with the specified period subtracted.
     * The calculation is delegated to the unit within the period.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param period  the period to subtract, not null
     * @return a {@code LocalDateTime} based on this date-time with the period subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDateTime minus(Period period) {
        return minus(period.getAmount(), period.getUnit());
    }

    /**
     * Returns a copy of this date-time with the specified period subtracted.
     * <p>
     * This method returns a new date-time based on this date-time with the specified period subtracted.
     * This can be used to subtract any period that is defined by a unit, for example to subtract years, months or days.
     * The unit is responsible for the details of the calculation, including the resolution
     * of any edge cases in the calculation.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param period  the amount of the unit to subtract from the returned date-time, not null
     * @param unit  the unit of the period to subtract, not null
     * @return a {@code LocalDateTime} based on this date-time with the specified period subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDateTime minus(long period, PeriodUnit unit) {
        return unit.add(this, DateTimes.safeNegate(period));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code LocalDateTime} with the specified period in years subtracted.
     * <p>
     * This method subtracts the specified amount from the years field in three steps:
     * <ol>
     * <li>Subtract the input years to the year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * </ol>
     * <p>
     * For example, 2008-02-29 (leap year) minus one year would result in the
     * invalid date 2009-02-29 (standard year). Instead of returning an invalid
     * result, the last valid day of the month, 2009-02-28, is selected instead.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to subtract, may be negative
     * @return a {@code LocalDateTime} based on this date-time with the years subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDateTime minusYears(long years) {
        LocalDate newDate = date.minusYears(years);
        return with(newDate, time);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the specified period in months subtracted.
     * <p>
     * This method subtracts the specified amount from the months field in three steps:
     * <ol>
     * <li>Subtract the input months to the month-of-year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * </ol>
     * <p>
     * For example, 2007-03-31 minus one month would result in the invalid date
     * 2007-04-31. Instead of returning an invalid result, the last valid day
     * of the month, 2007-04-30, is selected instead.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to subtract, may be negative
     * @return a {@code LocalDateTime} based on this date-time with the months subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDateTime minusMonths(long months) {
        LocalDate newDate = date.minusMonths(months);
        return with(newDate, time);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the specified period in weeks subtracted.
     * <p>
     * This method subtracts the specified amount in weeks from the days field decrementing
     * the month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     * <p>
     * For example, 2008-12-31 minus one week would result in the 2009-01-07.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param weeks  the weeks to subtract, may be negative
     * @return a {@code LocalDateTime} based on this date-time with the weeks subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDateTime minusWeeks(long weeks) {
        LocalDate newDate = date.minusWeeks(weeks);
        return with(newDate, time);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the specified period in days subtracted.
     * <p>
     * This method subtracts the specified amount from the days field incrementing the
     * month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     * <p>
     * For example, 2008-12-31 minus one day would result in the 2009-01-01.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to subtract, may be negative
     * @return a {@code LocalDateTime} based on this date-time with the days subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDateTime minusDays(long days) {
        LocalDate newDate = date.minusDays(days);
        return with(newDate, time);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the specified period in hours subtracted.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to subtract, may be negative
     * @return a {@code LocalDateTime} based on this date-time with the hours subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDateTime minusHours(long hours) {
        return plusWithOverflow(date, hours, 0, 0, 0, -1);
   }

    /**
     * Returns a copy of this {@code LocalDateTime} with the specified period in minutes subtracted.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutes  the minutes to subtract, may be negative
     * @return a {@code LocalDateTime} based on this date-time with the minutes subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDateTime minusMinutes(long minutes) {
        return plusWithOverflow(date, 0, minutes, 0, 0, -1);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the specified period in seconds subtracted.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param seconds  the seconds to subtract, may be negative
     * @return a {@code LocalDateTime} based on this date-time with the seconds subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDateTime minusSeconds(long seconds) {
        return plusWithOverflow(date, 0, 0, seconds, 0, -1);
    }

    /**
     * Returns a copy of this {@code LocalDateTime} with the specified period in nanoseconds subtracted.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanos  the nanos to subtract, may be negative
     * @return a {@code LocalDateTime} based on this date-time with the nanoseconds subtracted, not null
     * @throws CalendricalException if the result exceeds the supported date range
     */
    public LocalDateTime minusNanos(long nanos) {
        return plusWithOverflow(date, 0, 0, 0, nanos, -1);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code LocalDateTime} with the specified period added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param newDate  the new date to base the calculation on, not null
     * @param hours  the hours to add, may be negative
     * @param minutes the minutes to add, may be negative
     * @param seconds the seconds to add, may be negative
     * @param nanos the nanos to add, may be negative
     * @param sign  the sign to determine add or subtract
     * @return a long nanos-from-midnight value, holding the nano-of-day time and days overflow
     */
    private LocalDateTime plusWithOverflow(LocalDate newDate, long hours, long minutes, long seconds, long nanos, int sign) {
        // 9223372036854775808 long, 2147483648 int
        if ((hours | minutes | seconds | nanos) == 0) {
            return with(newDate, time);
        }
        long totDays = nanos / NANOS_PER_DAY +             //   max/24*60*60*1B
                seconds / SECONDS_PER_DAY +                //   max/24*60*60
                minutes / MINUTES_PER_DAY +                //   max/24*60
                hours / HOURS_PER_DAY;                     //   max/24
        totDays *= sign;                                   // total max*0.4237...
        long totNanos = nanos % NANOS_PER_DAY +                    //   max  86400000000000
                (seconds % SECONDS_PER_DAY) * NANOS_PER_SECOND +   //   max  86400000000000
                (minutes % MINUTES_PER_DAY) * NANOS_PER_MINUTE +   //   max  86400000000000
                (hours % HOURS_PER_DAY) * NANOS_PER_HOUR;          //   max  86400000000000
        long curNoD = time.toNanoOfDay();                       //   max  86400000000000
        totNanos = totNanos * sign + curNoD;                    // total 432000000000000
        totDays += DateTimes.floorDiv(totNanos, NANOS_PER_DAY);
        long newNoD = DateTimes.floorMod(totNanos, NANOS_PER_DAY);
        LocalTime newTime = (newNoD == curNoD ? time : LocalTime.ofNanoOfDay(newNoD));
        return with(newDate.plusDays(totDays), newTime);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns an offset date-time formed from this date-time and the specified offset.
     * <p>
     * This merges the two objects - {@code this} and the specified offset -
     * to form an instance of {@code OffsetDateTime}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param offset  the offset to use, not null
     * @return the offset date-time formed from this date-time and the specified offset, not null
     */
    public OffsetDateTime atOffset(ZoneOffset offset) {
        return OffsetDateTime.of(this, offset);
    }

    /**
     * Returns a zoned date-time formed from this date-time and the specified time-zone.
     * <p>
     * Time-zone rules, such as daylight savings, mean that not every time on the
     * local time-line exists. If the local date-time is in a gap or overlap according to
     * the rules then a resolver is used to determine the resultant local time and offset.
     * This method uses the {@link ZoneResolvers#postGapPreOverlap() post-gap pre-overlap} resolver.
     * This selects the date-time immediately after a gap and the earlier offset in overlaps.
     * <p>
     * Finer control over gaps and overlaps is available in two ways.
     * If you simply want to use the later offset at overlaps then call
     * {@link ZonedDateTime#withLaterOffsetAtOverlap()} immediately after this method.
     * Alternately, pass a specific resolver to {@link #atZone(ZoneId, ZoneResolver)}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param zone  the time-zone to use, not null
     * @return the zoned date-time formed from this date-time, not null
     */
    public ZonedDateTime atZone(ZoneId zone) {
        return ZonedDateTime.of(this, zone, ZoneResolvers.postGapPreOverlap());
    }

    /**
     * Returns a zoned date-time formed from this date-time and the specified time-zone
     * taking control of what occurs in time-line gaps and overlaps.
     * <p>
     * Time-zone rules, such as daylight savings, mean that not every time on the
     * local time-line exists. If the local date-time is in a gap or overlap according to
     * the rules then the resolver is used to determine the resultant local time and offset.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param zone  the time-zone to use, not null
     * @param resolver  the zone resolver to use for gaps and overlaps, not null
     * @return the zoned date-time formed from this date-time, not null
     * @throws CalendricalException if the date-time cannot be resolved
     */
    public ZonedDateTime atZone(ZoneId zone, ZoneResolver resolver) {
        return ZonedDateTime.of(this, zone, resolver);
    }

    //-----------------------------------------------------------------------
    /**
     * Extracts date-time information in a generic way.
     * <p>
     * This method exists to fulfill the {@link CalendricalObject} interface.
     * This implementation returns the following types:
     * <ul>
     * <li>LocalDate
     * <li>LocalTime
     * <li>LocalDateTime
     * <li>DateTimeBuilder
     * <li>Class, returning {@code LocalDateTime}
     * </ul>
     * 
     * @param <R> the type to extract
     * @param type  the type to extract, null returns null
     * @return the extracted object, null if unable to extract
     */
    @SuppressWarnings("unchecked")
    @Override
    public <R> R extract(Class<R> type) {
        if (type == LocalDateTime.class) {
            return (R) this;
        } else if (type == LocalDate.class) {
            return (R) date;
        } else if (type == LocalTime.class) {
            return (R) time;
        } else if (type == Class.class) {
            return (R) LocalDateTime.class;
        } else if (type == DateTimeBuilder.class) {
            return (R) new DateTimeBuilder(this);
        }
        return null;
    }

    @Override
    public LocalDateTime with(CalendricalAdjuster adjuster) {
        if (adjuster instanceof DateAdjuster) {
            return with(((DateAdjuster) adjuster).adjustDate(date), time);
        } else if (adjuster instanceof TimeAdjuster) {
            return with(date, ((TimeAdjuster) adjuster).adjustTime(time));
        } else if (adjuster instanceof LocalDate) {
            return with((LocalDate) adjuster, time);
        } else if (adjuster instanceof LocalTime) {
            return with(date, (LocalTime) adjuster);
        } else if (adjuster instanceof LocalDateTime) {
            return ((LocalDateTime) adjuster);
        }
        DateTimes.checkNotNull(adjuster, "Adjuster m
