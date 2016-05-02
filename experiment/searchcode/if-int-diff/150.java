package org.hxzon.util;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

public class DateUtil {
    public static Calendar toCalendar(Date orig) {
        Calendar c = Calendar.getInstance();
        c.setLenient(false);
        c.setTime(orig);
        return c;
    }

    //month is 1-12
    public static Date setDate(Date orig, int year, int month, int date) {
        return setDateTime(orig, year, month, date, -1, -1, -1);
    }

    public static Date setTime(Date orig, int hourOfDay, int minute, int second) {
        return setDateTime(orig, -1, -1, -1, hourOfDay, minute, second);
    }

    public static Date setDateTime(Date orig, int year, int month, int date, int hourOfDay, int minute, int second) {
        if (orig == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        // getInstance() returns a new object, so this method is thread safe.
        Calendar c = toCalendar(orig);
        if (year > 0) {
            c.set(Calendar.YEAR, year);
        }
        if (month > 0) {
            c.set(Calendar.MONTH, month - 1);
        }
        if (date > 0) {
            c.set(Calendar.DATE, date);
        }
        if (hourOfDay > -1) {
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        }
        if (minute > -1) {
            c.set(Calendar.MINUTE, minute);
        }
        if (second > -1) {
            c.set(Calendar.SECOND, second);
        }
        return c.getTime();
    }

    //--------------------------------
    public static Date getFirstDateInWeek(Date orig, int firstDayInWeek) {
        Calendar c = toCalendar(orig);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        int diff = Math.abs(firstDayInWeek - dayOfWeek);
        c.add(Calendar.DATE, -diff);
        return c.getTime();
    }

    public static Date getLastDateInWeek(Date orig, int firstDayInWeek) {
        Calendar c = toCalendar(orig);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        int diff = Math.abs(firstDayInWeek - dayOfWeek);
        c.add(Calendar.DATE, -diff + 6);
        return c.getTime();
    }

    //----------------
    public static Date[] getRangeInMonthWeek(Date orig, int firstDayInWeek) {
        Date[] result = new Date[6];
        result[0] = getFirstDateInMonthWeek(orig, firstDayInWeek);
        result[1] = getLastDateInMonth(result[0]);
        result[2] = getFirstDateInMonth(orig);
        result[3] = getLastDateInMonth(orig);
        result[5] = getLastDateInMonthWeek(orig, firstDayInWeek);
        result[4] = getFirstDateInMonth(result[5]);
        if (DateUtils.isSameDay(result[0], result[2])) {
            result[0] = null;
            result[1] = null;
        }
        if (DateUtils.isSameDay(result[3], result[5])) {
            result[4] = null;
            result[5] = null;
        }
        return result;
    }

    public static Date getFirstDateInMonthWeek(Date orig, int firstDayInWeek) {
        Calendar c = toCalendar(orig);
        c.set(Calendar.DATE, 1);
        //getFirstDateInWeek
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        int diff = Math.abs(firstDayInWeek - dayOfWeek);
        c.add(Calendar.DATE, -diff);
        return c.getTime();
    }

    public static Date getLastDateInMonthWeek(Date orig, int firstDayInWeek) {
        Calendar c = toCalendar(orig);
        //getLastDateInMonth
        int origMonth = c.get(Calendar.MONTH);
        int count = 29;
        c.set(Calendar.DATE, count);
        for (int i = count; i <= 32; i++) {
            c.add(Calendar.DATE, 1);
//            System.out.println(c.getTime());
            if (origMonth != c.get(Calendar.MONTH)) {
                c.add(Calendar.DATE, -1);
                break;
            }
        }
        //getLastDateInWeek
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        int diff = Math.abs(firstDayInWeek - dayOfWeek);
        c.add(Calendar.DATE, -diff + 6);
        return c.getTime();
    }

    //----------
    public static Date getFirstDateInMonth(Date orig) {
        Calendar c = toCalendar(orig);
        c.set(Calendar.DATE, 1);
        return c.getTime();
    }

    public static Date getLastDateInMonth(Date orig) {
        Calendar c = toCalendar(orig);
        int origMonth = c.get(Calendar.MONTH);
        int count = 29;
        c.set(Calendar.DATE, count);
        for (int i = count; i <= 32; i++) {
            c.add(Calendar.DATE, 1);
//            System.out.println(c.getTime());
            if (origMonth != c.get(Calendar.MONTH)) {
                c.add(Calendar.DATE, -1);
                return c.getTime();
            }
        }
        return null;
    }

    public static int getDateCountInMonth(Date orig) {
        Calendar c = toCalendar(orig);
        int origMonth = c.get(Calendar.MONTH);
        int count = 29;
        c.set(Calendar.DATE, count);
        for (int i = count; i <= 32; i++) {
            c.add(Calendar.DATE, 1);
//            System.out.println(c.getTime());
            if (origMonth != c.get(Calendar.MONTH)) {
                return i - 1;
            }
        }
        return 0;
    }

    public static void main(String args[]) {
        Date orig = new Date();
        Date result;
        orig = setDate(orig, 2012, 2, 10);
        print("orig:", orig);
        result = getFirstDateInWeek(orig, Calendar.SUNDAY);
        print("first date in week(sunday):", result);
        result = getFirstDateInWeek(orig, Calendar.MONDAY);
        print("first date in week(monday):", result);
        result = getFirstDateInMonthWeek(orig, Calendar.SUNDAY);
        print("first date in month week:", result);
        result = getLastDateInMonth(orig);
        print("last date in month:", result);
        result = getLastDateInMonthWeek(orig, Calendar.SUNDAY);
        print("last date in month week:", result);
        System.out.println("==============");
        //----
        orig = setDate(orig, 2012, 3, 12);
        print("orig:", orig);
        result = getFirstDateInWeek(orig, Calendar.SUNDAY);
        print("first date in week(sunday):", result);
        result = getFirstDateInWeek(orig, Calendar.MONDAY);
        print("first date in week(monday):", result);
        result = getFirstDateInMonthWeek(orig, Calendar.SUNDAY);
        print("first date in month week:", result);
        result = getLastDateInMonth(orig);
        print("last date in month:", result);
        result = getLastDateInMonthWeek(orig, Calendar.SUNDAY);
        print("last date in month week:", result);
        System.out.println("==============");
        //----
        orig = setDate(orig, 2012, 5, 2);
        print("orig:", orig);
        result = getFirstDateInWeek(orig, Calendar.SUNDAY);
        print("first date in week(sunday):", result);
        result = getFirstDateInWeek(orig, Calendar.MONDAY);
        print("first date in week(monday):", result);
        result = getFirstDateInMonthWeek(orig, Calendar.SUNDAY);
        print("first date in month week:", result);
        result = getLastDateInMonth(orig);
        print("last date in month:", result);
        result = getLastDateInMonthWeek(orig, Calendar.SUNDAY);
        print("last date in month week:", result);
        System.out.println("==============");
        orig = setDate(orig, 2013, 1, 2);
        print("orig:", orig);
        result = getFirstDateInWeek(orig, Calendar.SUNDAY);
        print("first date in week(sunday):", result);
        result = getFirstDateInWeek(orig, Calendar.MONDAY);
        print("first date in week(monday):", result);
        result = getFirstDateInMonthWeek(orig, Calendar.SUNDAY);
        print("first date in month week:", result);
        result = getLastDateInMonth(orig);
        print("last date in month:", result);
        result = getLastDateInMonthWeek(orig, Calendar.SUNDAY);
        print("last date in month week:", result);
        System.out.println("==============");
    }

    private static void print(String title, Date date) {
        System.out.println(title + DateFormatUtil.formatToDay(date));
    }
}

