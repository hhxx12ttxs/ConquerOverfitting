/*
 * BEGIN_HEADER - DO NOT EDIT
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */

/*
 * @(#)DateTime.java 
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */

package com.sun.jbi.engine.bpel.core.bpel.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * DOCUMENT ME!
 *
 * @author Sun Microsystems
 * @version 
 */
public class DateTime {
	
    /**
     * DOCUMENT ME!
     */
    public static final TimeZone UTC = TimeZone.getTimeZone("UTC"); // NO I18N

    /** DOCUMENT ME! */
    GregorianCalendar cal;

    /**
     * Creates a new DateTime object.
     */
    public DateTime() {
        cal = new GregorianCalendar(UTC);
        cal.setLenient(true);
    }

    /**
     * Creates a new DateTime object.
     *
     * @param cal DOCUMENT ME!
     */
    public DateTime(GregorianCalendar cal) {
        this.cal = (GregorianCalendar) cal.clone();
        this.cal.setLenient(true);
    }

    /**
     * Creates a new DateTime object.
     *
     * @param year DOCUMENT ME!
     * @param month DOCUMENT ME!
     * @param day DOCUMENT ME!
     */
    public DateTime(int year, int month, int day) {
        this(year, month, day, 0, 0, 0, 0);
    }

    /**
     * Creates a new DateTime object.
     *
     * @param year DOCUMENT ME!
     * @param month DOCUMENT ME!
     * @param day DOCUMENT ME!
     * @param hour DOCUMENT ME!
     * @param minute DOCUMENT ME!
     * @param second DOCUMENT ME!
     * @param millisecond DOCUMENT ME!
     */
    public DateTime(int year, int month, int day, int hour, int minute,
        int second, int millisecond) {
        cal = new GregorianCalendar(year, month, day, hour, minute, second);
        cal.setLenient(true);
        cal.set(Calendar.MILLISECOND, millisecond);
    }

    /**
     * DOCUMENT ME!
     *
     * @param other DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean after(DateTime other) {
        return cal.after(other.cal);
    }

    /**
     * DOCUMENT ME!
     *
     * @param other DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean before(DateTime other) {
        return cal.before(other.cal);
    }

    /**
     * DOCUMENT ME!
     *
     * @param cal DOCUMENT ME!
     */
    public void setCalendar(GregorianCalendar cal) {
        cal = (GregorianCalendar) cal.clone();
        cal.setLenient(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param dur DOCUMENT ME!
     */
    void roll(Duration dur) {
        int m = (dur.isNegative() ? (-1) : 1);
        rollYear(m * dur.getYears());
        rollMonth(m * dur.getMonths());
        rollDay(m * dur.getDays());
        rollHours(m * dur.getHours());
        rollMinutes(m * dur.getMinutes());
        rollSeconds(m * dur.getSeconds());
        rollMilliseconds(m * dur.getMilliseconds());
    }

    /**
     * DOCUMENT ME!
     *
     * @param dur DOCUMENT ME!
     */
    public void add(Duration dur) {
        int m = (dur.isNegative() ? (-1) : 1);
        addYear(m * dur.getYears());
        addMonth(m * dur.getMonths());
        addDay(m * dur.getDays());
        addHours(m * dur.getHours());
        addMinutes(m * dur.getMinutes());
        addSeconds(m * dur.getSeconds());
        addMilliseconds(m * dur.getMilliseconds());
    }

    /**
     * DOCUMENT ME!
     *
     * @param dur DOCUMENT ME!
     */
    void subtract(Duration dur) {
        int m = (dur.isNegative() ? 1 : (-1));
        addYear(m * dur.getYears());
        addMonth(m * dur.getMonths());
        addDay(m * dur.getDays());
        addHours(m * dur.getHours());
        addMinutes(m * dur.getMinutes());
        addSeconds(m * dur.getSeconds());
        addMilliseconds(m * dur.getMilliseconds());
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public GregorianCalendar toCalendar() {
        return cal;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getYear() {
        return cal.get(Calendar.YEAR);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getMonth() {
        return cal.get(Calendar.MONTH) + 1;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getDay() {
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * DOCUMENT ME!
     *
     * @param hours DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getHours(int hours) {
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getMinutes() {
        return cal.get(Calendar.MINUTE);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getSeconds() {
        return cal.get(Calendar.SECOND);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getMilliseconds() {
        return cal.get(Calendar.MILLISECOND);
    }

    /**
     * DOCUMENT ME!
     *
     * @param year DOCUMENT ME!
     */
    public void setYear(int year) {
        cal.set(Calendar.YEAR, year);
    }

    /**
     * DOCUMENT ME!
     *
     * @param month DOCUMENT ME!
     */
    public void setMonth(int month) {
        cal.set(Calendar.MONTH, month - 1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param day DOCUMENT ME!
     */
    public void setDay(int day) {
        cal.set(Calendar.DAY_OF_MONTH, day);
    }

    /**
     * DOCUMENT ME!
     *
     * @param hours DOCUMENT ME!
     */
    public void setHours(int hours) {
        cal.set(Calendar.HOUR_OF_DAY, hours);
    }

    /**
     * DOCUMENT ME!
     *
     * @param minutes DOCUMENT ME!
     */
    public void setMinutes(int minutes) {
        cal.set(Calendar.MINUTE, minutes);
    }

    /**
     * DOCUMENT ME!
     *
     * @param seconds DOCUMENT ME!
     */
    public void setSeconds(int seconds) {
        cal.set(Calendar.SECOND, seconds);
    }

    /**
     * DOCUMENT ME!
     *
     * @param millis DOCUMENT ME!
     */
    public void setMilliseconds(int millis) {
        cal.set(Calendar.MILLISECOND, millis);
    }

    /**
     * DOCUMENT ME!
     *
     * @param years DOCUMENT ME!
     */
    public void rollYear(int years) {
        cal.roll(Calendar.YEAR, years);
    }

    /**
     * DOCUMENT ME!
     *
     * @param months DOCUMENT ME!
     */
    public void rollMonth(int months) {
        cal.roll(Calendar.MONTH, months);
    }

    /**
     * DOCUMENT ME!
     *
     * @param days DOCUMENT ME!
     */
    public void rollDay(int days) {
        cal.roll(Calendar.DAY_OF_MONTH, days);
    }

    /**
     * DOCUMENT ME!
     *
     * @param hours DOCUMENT ME!
     */
    public void rollHours(int hours) {
        cal.roll(Calendar.HOUR_OF_DAY, hours);
    }

    /**
     * DOCUMENT ME!
     *
     * @param minutes DOCUMENT ME!
     */
    public void rollMinutes(int minutes) {
        cal.roll(Calendar.MINUTE, minutes);
    }

    /**
     * DOCUMENT ME!
     *
     * @param seconds DOCUMENT ME!
     */
    public void rollSeconds(int seconds) {
        cal.roll(Calendar.SECOND, seconds);
    }

    /**
     * DOCUMENT ME!
     *
     * @param millis DOCUMENT ME!
     */
    public void rollMilliseconds(int millis) {
        cal.roll(Calendar.MILLISECOND, millis);
    }

    /**
     * DOCUMENT ME!
     *
     * @param years DOCUMENT ME!
     */
    public void addYear(int years) {
        cal.add(Calendar.YEAR, years);
    }

    /**
     * DOCUMENT ME!
     *
     * @param months DOCUMENT ME!
     */
    public void addMonth(int months) {
        cal.add(Calendar.MONTH, months);
    }

    /**
     * DOCUMENT ME!
     *
     * @param days DOCUMENT ME!
     */
    public void addDay(int days) {
        cal.add(Calendar.DAY_OF_MONTH, days);
    }

    /**
     * DOCUMENT ME!
     *
     * @param hours DOCUMENT ME!
     */
    public void addHours(int hours) {
        cal.add(Calendar.HOUR_OF_DAY, hours);
    }

    /**
     * DOCUMENT ME!
     *
     * @param minutes DOCUMENT ME!
     */
    public void addMinutes(int minutes) {
        cal.add(Calendar.MINUTE, minutes);
    }

    /**
     * DOCUMENT ME!
     *
     * @param seconds DOCUMENT ME!
     */
    public void addSeconds(int seconds) {
        cal.add(Calendar.SECOND, seconds);
    }

    /**
     * DOCUMENT ME!
     *
     * @param millis DOCUMENT ME!
     */
    public void addMilliseconds(int millis) {
        cal.add(Calendar.MILLISECOND, millis);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        return DateParser.getIsoDate(cal.getTime());
    }

    /**
     * DOCUMENT ME!
     *
     * @param value DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static DateTime parse(char[] value) {
        return parse(new String(value));
    }

    /**
     * DOCUMENT ME!
     *
     * @param value DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static DateTime parse(String value) {
        return new DateTime(DateParser.getCalendar(value));
    }
}

