package org.sk.maskedpicker;

import java.text.DateFormatSymbols;
import java.util.Locale;

/**
 * A <code>BasicCalendar</code> implements a more natural use of a
 * <code>GregorianCalendar</code>.
 * 
 * @author Mario C??novas
 */
public class Calendar extends java.util.GregorianCalendar {
    /**
     * Serial version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Singleton calendar.
     */
    private static Calendar tmpCalendar = new Calendar();

    /**
     * Creates a calendar based on a date and a time.
     * 
     * @param date
     * @param time
     * @return The calendar
     */
    public static Calendar createCalendar(final Date date, final Time time) {
        Calendar d = new Calendar(date);
        Calendar t = new Calendar(time);
        return new Calendar(d.getYear(), d.getMonth(), d.getDay(),
            t.getHour(), t.getMinute(), t.getSecond(), t.getMilliSecond());
    }

    /**
     * Creates a date.
     * 
     * @return java.sql.Date
     * @param year
     *            int
     * @param month
     *            int
     * @param day
     *            int
     */
    public static Date createDate(final int year, final int month,
            final int day) {
        return new Date(new Calendar(year, month, day).getTimeInMillis());
    }

    /**
     * Creates a <code>Time</code> from hour, minute and second.
     * 
     * @return The <code>Time</code>
     * @param hour
     *            The hour
     * @param minute
     *            The minute
     * @param second
     *            The second
     */
    public static Time createTime(final int hour, final int minute,
            final int second) {
        return createTime(hour, minute, second, 0);
    }

    /**
     * Creates a <code>Time</code> from hour, minute, second and millisecond.
     * 
     * @return The <code>Time</code>
     * @param hour
     *            The hour
     * @param minute
     *            The minute
     * @param second
     *            The second
     * @param millis
     *            The millisecond
     */
    public static Time createTime(final int hour, final int minute,
            final int second, final int millis) {
        tmpCalendar.setHour(hour);
        tmpCalendar.setMinute(minute);
        tmpCalendar.setSecond(second);
        tmpCalendar.setMilliSecond(millis);
        return tmpCalendar.toTime();
    }

    /**
     * Creates a <code>Timestamp</code> from year, month, day, hour, minute and
     * second.
     * 
     * @return The <code>Timestamp</code>
     * @param year
     *            The year
     * @param month
     *            The month
     * @param day
     *            The day
     * @param hour
     *            The hour
     * @param minute
     *            The minute
     * @param second
     *            The second
     */
    public static Timestamp createTimestamp(final int year,
            final int month, final int day, final int hour,
            final int minute, final int second) {
        return createTimestamp(year, month, day, hour, minute, second, 0);
    }

    /**
     * Creates a <code>Timestamp</code> from year, month, day, hour, minute,
     * second and millisecond.
     * 
     * @return The <code>Timestamp</code>
     * @param year
     *            The year
     * @param month
     *            The month
     * @param day
     *            The day
     * @param hour
     *            The hour
     * @param minute
     *            The minute
     * @param second
     *            The second
     * @param millis
     *            The millisecond
     */
    public static Timestamp createTimestamp(final int year,
            final int month, final int day, final int hour,
            final int minute, final int second, final int millis) {
        tmpCalendar.setYear(year);
        tmpCalendar.setMonth(month);
        tmpCalendar.setDay(day);
        tmpCalendar.setHour(hour);
        tmpCalendar.setMinute(minute);
        tmpCalendar.setSecond(second);
        tmpCalendar.setMilliSecond(millis);
        return tmpCalendar.toTimestamp();
    }

    /**
     * Creates a timestamp based on a date and a time.
     * 
     * @param date
     * @param time
     * @return the timestamp.
     */
    public static Timestamp crateTimestamp(final Date date, final Time time) {
        return createCalendar(date, time).toTimestamp();
    }

    /**
     * Returns the number of days of a month.
     * 
     * @param year
     *            int
     * @param month
     *            int
     * @return int
     */
    public static int getDaysOfMonth(final int year, final int month) {
        if (month == 1) {
            return 31;
        }
        if (month == 2) {
            return (tmpCalendar.isLeapYear(year) ? 29 : 28);
        }
        if (month == 3) {
            return 31;
        }
        if (month == 4) {
            return 30;
        }
        if (month == 5) {
            return 31;
        }
        if (month == 6) {
            return 30;
        }
        if (month == 7) {
            return 31;
        }
        if (month == 8) {
            return 31;
        }
        if (month == 9) {
            return 30;
        }
        if (month == 10) {
            return 31;
        }
        if (month == 11) {
            return 30;
        }
        if (month == 12) {
            return 31;
        }
        return 0;
    }

    /**
     * Default constructor.
     */
    public Calendar() {
        super();
    }

    /**
     * Constructor assigning the time in millis.
     * 
     * @param timeInMillis
     */
    public Calendar(final long timeInMillis) {
        super();
        setTimeInMillis(timeInMillis);
    }

    /**
     * Constructor assigning year, month and day.
     * 
     * @param year
     *            The year.
     * @param month
     *            The month from 1 to 12.
     * @param day
     *            The day.
     */
    public Calendar(final int year, final int month, final int day) {
        super(year, month - 1, day, 0, 0);
    }

    /**
     * Constructor assigning year, month, day, hour and minute.
     * 
     * @param year
     *            The year
     * @param month
     *            The month, from 1 to 12
     * @param day
     *            The day
     * @param hour
     *            The hour
     * @param minute
     *            The minute
     */
    public Calendar(final int year, final int month, final int day,
            final int hour, final int minute) {
        super(year, month - 1, day, hour, minute);
    }

    /**
     * Constructor assigning year, month, day, hour and minute.
     * 
     * @param year
     *            The year
     * @param month
     *            The month, from 1 to 12
     * @param day
     *            The day
     * @param hour
     *            The hour
     * @param minute
     *            The minute
     * @param second
     *            The second
     */
    public Calendar(final int year, final int month, final int day,
            final int hour, final int minute, final int second) {
        super(year, month - 1, day, hour, minute, second);
    }

    /**
     * Constructor assigning year, month, day, hour and minute.
     * 
     * @param year
     *            The year
     * @param month
     *            The month, from 1 to 12
     * @param day
     *            The day
     * @param hour
     *            The hour
     * @param minute
     *            The minute
     * @param second
     *            The second
     * @param millis
     *            The millisecond
     */
    public Calendar(final int year, final int month, final int day,
            final int hour, final int minute, final int second,
            final int millis) {
        super(year, month - 1, day, hour, minute, second);
        add(MILLISECOND, millis);
    }

    /**
     * Constructor assigning a <code>Date</code>.
     * 
     * @param date
     *            The <code>Date</code>.
     */
    public Calendar(final java.sql.Date date) {
        setLong(date.getTime());
    }

    /**
     * Constructor assigning a <code>Time</code>.
     * 
     * @param time
     *            The <code>Time</code>.
     */
    public Calendar(final java.sql.Time time) {
        setLong(time.getTime());
    }

    /**
     * Constructor assigning a <code>Timestamp</code>.
     * 
     * @param time
     *            The <code>Timestamp</code>.
     */
    public Calendar(final java.sql.Timestamp time) {
        setLong(time.getTime());
    }

    /**
     * Add days to a date.
     * 
     * @return The new date.
     * @param date
     *            The origin date.
     * @param days
     *            The number of days to add.
     */
    public static Date add(final Date date, final int days) {
        Calendar calendar = new Calendar(date);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.toDate();
    }

    /**
     * Add months to a date.
     * 
     * @param d
     * @param months
     * @return The new date.
     */
    public static Date goMonth(final Date d, final int months) {
        Calendar c = new Calendar(d);
        int day = c.getDay();
        int month = c.getMonth();
        int year = c.getYear();
        int count = Math.abs(months);
        int dif = count % 12;
        if (months >= 0) {
            year =
                ((dif + month) <= 12 ? year + count / 12
                        : (year + count / 12) + 1);
            month =
                ((month + dif) <= 12 ? month + dif : (month + dif) - 12);
        } else {
            year =
                ((dif < month) ? year - count / 12
                        : (year - count / 12) - 1);
            month = ((month - dif) > 0 ? month - dif : (month - dif) + 12);
        }

        // Comprueba que el dia exista para ese mes/a???o
        day =
            (Calendar.getDaysOfMonth(year, month) >= day ? day : Calendar
                .getDaysOfMonth(year, month));

        return Calendar.createDate(year, month, day);
    }

    /**
     * Adds hours minutes and seconds to a time.
     * 
     * @param time
     *            Time starting time
     * @param hours
     *            Number of hours to add
     * @param minutes
     *            Number of minutes to add
     * @param seconds
     *            Number of seconds to add
     * @return The new time
     */
    public static Time add(final Time time, final int hours,
            final int minutes, final int seconds) {
        Calendar calendar = new Calendar(time);
        calendar.add(Calendar.HOUR, hours);
        calendar.add(Calendar.MINUTE, minutes);
        calendar.add(Calendar.SECOND, seconds);
        return calendar.toTime();
    }

    /**
     * Returns the number of days elapsed between two dates.
     * 
     * @return The number of days.
     * @param fromDate
     *            The date from.
     * @param toDate
     *            The date to.
     */
    public static long daysElapsed(final Date fromDate, final Date toDate) {
        return (toDate.getTime() - fromDate.getTime())
            / (1000 * 60 * 60 * 24);
    }

    /**
     * Returns the number of weeks elapsed.
     * 
     * @param date0
     *            Start date
     * @param date1
     *            End date
     * @return The number of weeks elapsed.
     */
    public static int weeksElapsed(final Date date0, final Date date1) {
        Calendar c0 = new Calendar(date0);
        Calendar c1 = new Calendar(date1);
        int year0 = c0.getYear();
        int week0 = c0.getWeek();
        int year1 = c1.getYear();
        int week1 = c1.getWeek();
        return weeksElapsed(year0, week0, year1, week1);
    }

    /**
     * Returns the number of weeks elapsed.
     * 
     * @param year0
     *            Start year
     * @param week0
     *            Start week
     * @param year1
     *            End year
     * @param week1
     *            End week
     * @return The number of weeks
     */
    public static int weeksElapsed(final int year0, final int week0,
            final int year1, final int week1) {
        int yearStart = 0;
        int weekStart = 0;
        int yearEnd = 0;
        int weekEnd = 0;
        if (year0 == year1) {
            if (week0 <= week1) {
                yearStart = year0;
                weekStart = week0;
                yearEnd = year1;
                weekEnd = week1;
            } else {
                yearStart = year1;
                weekStart = week1;
                yearEnd = year0;
                weekEnd = week0;
            }
        } else if (year0 < year1) {
            yearStart = year0;
            weekStart = week0;
            yearEnd = year1;
            weekEnd = week1;
        } else {
            yearStart = year1;
            weekStart = week1;
            yearEnd = year0;
            weekEnd = week0;
        }
        int weeks = 0;
        if (yearStart == yearEnd) {
            weeks = weekEnd - weekStart;
        } else {
            int lastWeek = getLastWeekOfYear(yearStart);
            weeks = (lastWeek - weekStart) + weekEnd;
        }
        return weeks;
    }

    /**
     * Get the day.
     * 
     * @return The day
     */
    public int getDay() {
        return get(DAY_OF_MONTH);
    }

    /**
     * Get the day of the week.
     * 
     * @return The day of the week.
     */
    public int getDayOfWeek() {
        return get(DAY_OF_WEEK);
    }

    /**
     * Get the hour.
     * 
     * @return The hour.
     */
    public int getHour() {
        return get(HOUR_OF_DAY);
    }

    /**
     * Returns an array of localized names of week days.
     * 
     * @return An array of names.
     * @param capitalized
     *            A boolean to capitalize the name.
     * @param day
     *            The day of the week, use Calendar.MONDAY ...
     */
    public static String getLongDay(final boolean capitalized,
            final int day) {
        return getLongDay(Locale.getDefault(), capitalized, day);
    }

    /**
     * Returns an array of localized names of week days.
     * 
     * @return An array of names.
     * @param locale
     *            The desired locale.
     * @param capitalized
     *            A boolean to capitalize the name.
     * @param day
     *            The day of the week, use Calendar.MONDAY ...
     */
    public static String getLongDay(final Locale locale,
            final boolean capitalized, final int day) {
        return getLongDays(locale, capitalized)[day];
    }

    /**
     * Returns an array of localized names of week days.
     * 
     * @return An array of names.
     * @param capitalized
     *            A boolean to capitalize the name.
     */
    public static String[] getLongDays(final boolean capitalized) {
        return getLongDays(Locale.getDefault(), capitalized);
    }

    /**
     * Returns an array of localized names of week days.
     * 
     * @return An array of names.
     * @param locale
     *            The desired locale.
     * @param capitalized
     *            A boolean to capitalize the name.
     */
    public static String[] getLongDays(final Locale locale,
            final boolean capitalized) {

        DateFormatSymbols sysd = new DateFormatSymbols(locale);
        String[] dsc = sysd.getWeekdays();
        if (capitalized) {
            for (int i = 0; i < dsc.length; i++) {
                dsc[i] = StringUtils.capitalize(dsc[i]);
            }
        }
        return dsc;
    }

    /**
     * Returns an array of localized names of months.
     * 
     * @return An array of names.
     * @param capitalized
     *            A boolean to capitalize the name.
     * @param month
     *            The month, use Calendar.JANUARY ...
     */
    public static String getLongMonth(final boolean capitalized,
            final int month) {
        return getLongMonth(Locale.getDefault(), capitalized, month);
    }

    /**
     * Returns an array of localized names of months.
     * 
     * @return An array of names.
     * @param locale
     *            The desired locale.
     * @param capitalized
     *            A boolean to capitalize the name.
     * @param month
     *            The month, use Calendar.JANUARY ...
     */
    public static String getLongMonth(final Locale locale,
            final boolean capitalized, final int month) {
        return getLongMonths(locale, capitalized)[month - 1];
    }

    /**
     * Returns an array of localized names of months.
     * 
     * @return An array of names.
     * @param capitalized
     *            A boolean to capitalize the name.
     */
    public static String[] getLongMonths(final boolean capitalized) {
        return getLongMonths(Locale.getDefault(), capitalized);
    }

    /**
     * Returns an array of localized names of months.
     * 
     * @return An array of names.
     * @param locale
     *            The desired locale.
     * @param capitalized
     *            A boolean to capitalize the name.
     */
    public static String[] getLongMonths(final Locale locale,
            final boolean capitalized) {

        DateFormatSymbols sysd = new DateFormatSymbols(locale);
        String[] dsc = sysd.getMonths();
        if (capitalized) {
            for (int i = 0; i < dsc.length; i++) {
                dsc[i] = StringUtils.capitalize(dsc[i]);
            }
        }
        return dsc;
    }

    /**
     * Get the number of milliseconds.
     * 
     * @return The number of milliseconds.
     */
    public int getMilliSecond() {
        return get(MILLISECOND);
    }

    /**
     * Get the minutes.
     * 
     * @return The minutes.
     */
    public int getMinute() {
        return get(MINUTE);
    }

    /**
     * Get the month.
     * 
     * @return he month.
     */
    public int getMonth() {
        return get(MONTH) + 1;
    }

    /**
     * Get The seconds.
     * 
     * @return The seconds.
     */
    public int getSecond() {
        return get(SECOND);
    }

    /**
     * Get The week of the year.
     * 
     * @return The week of the year.
     */
    public int getWeek() {
        return get(WEEK_OF_YEAR);
    }

    /**
     * Returns an array of localized names of week days.
     * 
     * @return An array of names.
     * @param capitalized
     *            A boolean to capitalize the name.
     * @param day
     *            The day of the week, use Calendar.MONDAY ...
     */
    public static String getShortDay(final boolean capitalized,
            final int day) {
        return getShortDay(Locale.getDefault(), capitalized, day);
    }

    /**
     * Returns an array of localized names of week days.
     * 
     * @return An array of names.
     * @param locale
     *            The desired locale.
     * @param capitalized
     *            A boolean to capitalize the name.
     * @param day
     *            The day of the week, use Calendar.MONDAY ...
     */
    public static String getShortDay(final Locale locale,
            final boolean capitalized, final int day) {
        return getShortDays(locale, capitalized)[day];
    }

    /**
     * Returns an array of localized names of week days.
     * 
     * @return An array of names.
     * @param capitalized
     *            A boolean to capitalize the name.
     */
    public static String[] getShortDays(final boolean capitalized) {
        return getShortDays(Locale.getDefault(), capitalized);
    }

    /**
     * Returns an array of localized names of week days.
     * 
     * @return An array of names.
     * @param locale
     *            The desired locale.
     * @param capitalized
     *            A boolean to capitalize the name.
     */
    public static String[] getShortDays(final Locale locale,
            final boolean capitalized) {

        DateFormatSymbols sysd = new DateFormatSymbols(locale);
        String[] dsc = sysd.getShortWeekdays();
        if (capitalized) {
            for (int i = 0; i < dsc.length; i++) {
                dsc[i] = StringUtils.capitalize(dsc[i]);
            }
        }
        return dsc;
    }

    /**
     * Returns an array of localized names of months.
     * 
     * @return An array of names.
     * @param locale
     *            The desired locale.
     * @param capitalized
     *            A boolean to capitalize the name.
     * @param month
     *            The month, use Calendar.JANUARY ...
     */
    public static String getShortMonth(final Locale locale,
            final boolean capitalized, final int month) {
        return getShortMonths(locale, capitalized)[month];
    }

    /**
     * Returns an array of localized names of months.
     * 
     * @return An array of names.
     * @param locale
     *            The desired locale.
     * @param capitalized
     *            A boolean to capitalize the name.
     */
    public static String[] getShortMonths(final Locale locale,
            final boolean capitalized) {

        DateFormatSymbols sysd = new DateFormatSymbols(locale);
        String[] dsc = sysd.getShortMonths();
        if (capitalized) {
            for (int i = 0; i < dsc.length; i++) {
                dsc[i] = StringUtils.capitalize(dsc[i]);
            }
        }
        return dsc;
    }

    /**
     * Returns the last week of a year.
     * 
     * @param year
     *            The year
     * @return The last week
     */
    public static int getLastWeekOfYear(final int year) {
        Calendar c = new Calendar(year, 12, 20);
        int lastWeek = c.getWeek();
        while (c.getYear() == year) {
            c.add(WEEK_OF_YEAR, 1);
            int week = c.getWeek();
            if (week > lastWeek) {
                lastWeek = week;
            }
        }
        return lastWeek;
    }

    /**
     * Get the year.
     * 
     * @return The year.
     */
    public int getYear() {
        return get(YEAR);
    }

    /**
     * Set the day.
     * 
     * @param day
     *            The day.
     */
    public void setDay(final int day) {
        set(DAY_OF_MONTH, day);
    }

    /**
     * Set the hour.
     * 
     * @param hour
     *            The hour.
     */
    public void setHour(final int hour) {
        set(HOUR_OF_DAY, hour);
    }

    /**
     * Set the time as a <b>long</b>.
     * 
     * @param timeInMillis
     *            The time as a <b>long</b>.
     */
    public void setLong(final long timeInMillis) {
        setTimeInMillis(timeInMillis);
    }

    /**
     * Set the milliseconds.
     * 
     * @param milliSecond
     *            The milliseconds.
     */
    public void setMilliSecond(final int milliSecond) {
        set(MILLISECOND, milliSecond);
    }

    /**
     * Set the minute.
     * 
     * @param minute
     *            he minute.
     */
    public void setMinute(final int minute) {
        set(MINUTE, minute);
    }

    /**
     * Set the month.
     * 
     * @param month
     *            The month.
     */
    public void setMonth(final int month) {
        set(MONTH, month - 1);
    }

    /**
     * Set the seconds.
     * 
     * @param second
     *            The seconds.
     */
    public void setSecond(final int second) {
        set(SECOND, second);
    }

    /**
     * Set the year.
     * 
     * @param year
     *            The year.
     */
    public void setYear(final int year) {
        set(YEAR, year);
    }

    /**
     * Set the week of the year.
     * 
     * @param week
     *            The week of the year.
     */
    public void setWeek(final int week) {
        set(WEEK_OF_YEAR, week);
    }

    /**
     * Add some centesimal time <code>CTime</code>.
     * 
     * @param time
     *            The CTime to add.
     * @param amount
     *            The number of times to add it.
     */
    public void add(final CTime time, final int amount) {
        add(Calendar.HOUR, time.getHours() * amount);
        add(Calendar.MINUTE, time.getMinutes() * amount);
        add(Calendar.SECOND, time.getSeconds() * amount);
    }

    /**
     * Convert this <code>Calendar</code> to a clean <code>Date</code>.
     * 
     * @return The clean <code>Date</code>, that is, with hours, minutes,
     *         seconds and milliseconds set to zero.
     */
    public Date toDate() {
        return new Date(getTimeInMillis());
    }

    /**
     * Get this calendar as a time <code>Time</code>.
     * 
     * @return The <code>Time</code>.
     */
    public Time toTime() {
        return new Time(getTimeInMillis());
    }

    /**
     * Get this calendar as a <code>Timestamp</code>.
     * 
     * @return The <code>Timestamp</code>.
     */
    public Timestamp toTimestamp() {
        return new Timestamp(getTimeInMillis());
    }
}

