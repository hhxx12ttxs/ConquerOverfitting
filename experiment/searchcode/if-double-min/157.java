<<<<<<< HEAD
/**
 * Copyright (c) 2002, Raben Systems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the Raben Systems, Inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.ecommerce.utils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Routines for calculating and setting Julian day number
 * based on algorithms from Jean Meeus,
 * "Astronomical Algorithms", 2nd Edition, Willmann-Bell, Inc.,
 * 1998.
 *
 * @author Vern Raben (mailto:vern@raben.com)
 * @version $Revision: 1.16 $ $Date: 2002/07/22 14:24:17 $
 */
public final class JulianDay implements java.io.Serializable, Cloneable {
    public final static int JD = 100;
    public final static int MJD = 101;
    public final static int YEAR = Calendar.YEAR;
    public final static int MONTH = Calendar.MONTH;
    public final static int DATE = Calendar.DATE;
    public final static int HOUR = Calendar.HOUR;
    public final static int HOUR_OF_DAY = Calendar.HOUR_OF_DAY;
    public final static int MINUTE = Calendar.MINUTE;
    public final static int SECOND = Calendar.SECOND;
    public final static int DAY_OF_YEAR = Calendar.DAY_OF_YEAR;
    public final static int DAY_OF_WEEK = Calendar.DAY_OF_WEEK;
    public final static int DAY_OF_MONTH = Calendar.DAY_OF_MONTH;
    public final static int JANUARY = Calendar.JANUARY;
    public final static int FEBRUARY = Calendar.FEBRUARY;
    public final static int MARCH = Calendar.MARCH;
    public final static int APRIL = Calendar.APRIL;
    public final static int MAY = Calendar.MAY;
    public final static int JUNE = Calendar.JUNE;
    public final static int JULY = Calendar.JULY;
    public final static int AUGUST = Calendar.AUGUST;
    public final static int SEPTEMBER = Calendar.SEPTEMBER;
    public final static int OCTOBER = Calendar.OCTOBER;
    public final static int NOVEMBER = Calendar.NOVEMBER;
    public final static int DECEMBER = Calendar.DECEMBER;
    public final static String[] MONTHS = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
    public final static String[] TIME_UNIT = {"unk", "yr", "mo", "unk", "unk", "day", "unk", "unk", "unk", "unk", "unk", "hr", "min", "sec"};
    public final static double EPOCH_1970 = 2440587.5;
    public final static String SQL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private DateFormat dateFormat = null;
    private Integer year = new Integer(0);
    private Integer month = new Integer(0);
    private Integer date = new Integer(0);
    private Integer hour = new Integer(0);
    private Integer minute = new Integer(0);
    private Integer second = new Integer(0);
    private Double jd;
    private Double mjd;
    private Integer dayOfWeek;
    private Integer dayOfYear;
    private final static DecimalFormat fmt4Dig = new DecimalFormat("0000");
    private final static DecimalFormat fmt2Dig = new DecimalFormat("00");
    private final static TimeZone tz = TimeZone.getTimeZone("UTC");

    /**
     * JulianCalendar constructor - sets JD for current time
     */
    public JulianDay() {
        Calendar cal = new GregorianCalendar(tz);
        setTime(cal.getTime());
    }

    /**
     * JulianCalendar constructor - sets JD passed as double
     *
     * @param jd double The Julian date
     */
    public JulianDay(double jd) {
        set(JulianDay.JD, jd);
        calcCalDate();
    }

    /**
     * Constructor to create Julian day given year, month, and decimal day
     *
     * @param yr int
     * @param mo int
     * @param da double
     */
    public JulianDay(int yr, int mo, double da) {
        int day = (int) da;
        int hr = 0;
        int min = 0;
        int sec = 0;
        double dhr = (da - day) * 24.0;
        hr = (int) dhr;
        double dmin = (dhr - hr) * 60.0;
        min = (int) (dmin);
        sec = (int) ((dmin - min) * 60.0);
        set(yr, mo, day, hr, min, sec);
        calcJD();
    }

    /**
     * Construct JulianDate given year, month, and date
     *
     * @param yr int
     * @param mo int
     * @param da int
     */
    public JulianDay(int yr, int mo, int da) {
        int hr = 0;
        int min = 0;
        int sec = 0;

        if (da < 1) {
            da = 1;
        }

        if (mo < 0) {
            mo = 0;
        }

        if (hr < 0) {
            hr = 0;
        }

        if (min < 0) {
            min = 0;
        }

        if (sec < 0) {
            sec = 0;
        }

        set(yr, mo, da, hr, min, sec);
        calcJD();
    }

    /**
     * Construct JulianDate given year, month, date, hour and minute
     *
     * @param yr int
     * @param mo int
     * @param da int
     */
    public JulianDay(int yr, int mo, int da, int hr, int min) {

        int sec = 0;

        if (da < 1) {
            da = 1;
        }

        if (mo < 0) {
            mo = 0;
        }

        if (hr < 0) {
            hr = 0;
        }

        if (min < 0) {
            min = 0;
        }

        if (sec < 0) {
            sec = 0;
        }

        set(yr, mo, da, hr, min, sec);
        calcJD();
    }

    /**
     * Construct JulianDate given year, month, day, hour, minute, and second
     *
     * @param yr  int
     * @param mo  int
     * @param da  int
     * @param hr  int
     * @param min int
     * @param sec int
     */
    public JulianDay(int yr, int mo, int da, int hr, int min, int sec) {

        if (da < 1) {
            da = 1;
        }

        if (mo < 0) {
            mo = 0;
        }

        if (hr < 0) {
            hr = 0;
        }

        if (min < 0) {
            min = 0;
        }

        if (sec < 0) {
            sec = 0;
        }

        set(yr, mo, da, hr, min, sec);
        calcJD();
    }

    /**
     * Construct JulianDay from system time in milli-seconds since Jan 1, 1970
     *
     * @param timeInMilliSec long
     */
    public JulianDay(long timeInMilliSec) {
        setDateTime("1970-01-01 0:00");
        add(JulianDay.DATE, ((double) timeInMilliSec / 86400000.0));
    }

    /**
     * Copy constructor for JulianDate
     *
     * @param cal com.raben.util.JulianDate
     */
    public JulianDay(JulianDay cal) {
        if (cal != null) {
            set(Calendar.YEAR, cal.get(Calendar.YEAR));
            set(Calendar.MONTH, cal.get(Calendar.MONTH));
            set(Calendar.DATE, cal.get(Calendar.DATE));
            set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
            set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
            set(Calendar.SECOND, cal.get(Calendar.SECOND));
            calcJD();
        } else {
            Calendar calendar = new GregorianCalendar(tz);
            setTime(calendar.getTime());
        }
    }

    /**
     * Set JulianDay from sql database compatible date/time string (yyyy-mm-dd hh:mm:ss)
     *
     * @param str java.lang.String
     */
    public JulianDay(String str) {
        setDateTime(str);
        calcJD();
    }

    /**
     * Construct JulianDate given Calendar as a parameter
     *
     * @param cal java.util.Calendar
     */
    public JulianDay(Calendar cal) {
        set(YEAR, cal.get(YEAR));
        set(MONTH, cal.get(MONTH));
        set(DATE, cal.get(DATE));
        set(HOUR_OF_DAY, cal.get(HOUR_OF_DAY));
        set(MINUTE, cal.get(MINUTE));
        set(SECOND, cal.get(SECOND));
        calcJD();
        calcCalDate();
    }

    /**
     * Add specified value in specified time unit to current Julian Date
     * increments next higher field
     * ISSUE - meaning of incrementing YEAR and MONTH by fractional value is not clear since
     * period of a month and year varies, that is ignored. Year is assumed to be 365 days and
     * month is assumed to be 30 days for computing the fractional increment.
     * ISSUE - not thoroughly tested, typically 1-2 second errors may occur
     * due to round-off. Will be refactored
     * "real soon  now" :) to utilize BigDecimal internal representation
     * of Julian Day.
     *
     * @param unit int Time unit
     * @param val  int Time increment
     */
    public void add(int unit, double val) {
        double da;

        switch (unit) {
            case YEAR:
                // issue - what this means if its not whole year
                int yr = year.intValue() + (int) val;
                set(YEAR, yr);
                da = (val - (int) val) * 365.0;
                set(DATE, da);
                break;
            case MONTH:
                int mo = month.intValue() + (int) val;
                set(MONTH, mo);
                da = (val - (int) val) * 30.0;
                set(DATE, da);
                break;

            case DATE:
                set(JD, getJDN() + val);
                break;
            case HOUR:
            case HOUR_OF_DAY:
                set(JD, getJDN() + (double) val / 24.0);
                break;
            case MINUTE:
                double min = minute.doubleValue() + val;
                set(JD, getJDN() + (double) val / 1440.0);
                break;
            case SECOND:
                double sec = second.doubleValue() + val;
                set(JD, getJDN() + (double) val / 86400.0);
                break;
            default:
                System.out.println("Error: JulianDate.add: The 'unit' parameter is not recognized=" + unit);
                set(JD, getJDN() + val);
                break;
        }

        calcJD();

    }

    /**
     * Add specified value in specified time unit to current Julian Date
     * increments next higher field
     * <p/>
     * ISSUE - meaning of incrementing YEAR and MONTH by fractional value is not clear since
     * period of a month and year varies, that is ignored. Year is assumed to be 365 days and
     * month is assumed to be 30 days for computing the fractional increment.
     * ISSUE - not thoroughly tested, typically 1-2 second errors may occur
     * due to round-off. Will be refactored
     * "real soon  now" :) to utilize BigDecimal internal representation
     * of Julian Day.
     *
     * @param unit int Time unit
     * @param val  int Time increment
     */
    public void add(int unit, int val) {
        int yr;
        int mo;
        switch (unit) {
            case YEAR:
                yr = year.intValue() + val;
                set(YEAR, yr);
                break;
            case MONTH:
                mo = month.intValue() + val;

                while (mo >= 12) {
                    mo -= 12;
                    yr = year.intValue() + 1;
                    set(YEAR, yr);
                }

                while (mo < 0) {
                    mo += 12;
                    yr = year.intValue() - 1;
                    set(YEAR, yr);
                }

                set(MONTH, mo);
                break;

            case DATE:
                set(JD, getJDN() + val);
                break;
            case HOUR:
            case HOUR_OF_DAY:
                set(JD, getJDN() + val * 0.041667);
                break;

            case MINUTE:
                set(JD, getJDN() + (double) val / 1440.0);
                break;

            case SECOND:
                set(JD, getJDN() + (double) val / 86400.0);
                break;
            default:
                System.out.println("Error: JulianDate.add: The 'unit' parameter is not recognized=" + unit);
                set(JD, getJDN() + val); // default to adding days
                break;
        }

        calcJD();

    }

    /**
     * Calculate calendar date for Julian date field this.jd
     */
    private void calcCalDate() {

        Double jd2 = new Double(jd.doubleValue() + 0.5);
        long I = jd2.longValue();
        double F = jd2.doubleValue() - (double) I;
        long A = 0;
        long B = 0;

        if (I > 2299160) {
            Double a1 = new Double(((double) I - 1867216.25) / 36524.25);
            A = a1.longValue();
            Double a3 = new Double((double) A / 4.0);
            B = I + 1 + A - a3.longValue();
        } else {
            B = I;
        }

        double C = (double) B + 1524;
        Double d1 = new Double((C - 122.1) / 365.25);
        long D = d1.longValue();
        Double e1 = new Double(365.25 * (double) D);
        long E = e1.longValue();
        Double g1 = new Double((double) (C - E) / 30.6001);
        long G = g1.longValue();
        Double h = new Double((double) G * 30.6001);
        long da = (long) C - E - h.longValue();
        date = new Integer((int) da);

        if (G < 14L) {
            month = new Integer((int) (G - 2L));
        } else {
            month = new Integer((int) (G - 14L));
        }

        if (month.intValue() > 1) {
            year = new Integer((int) (D - 4716L));
        } else {
            year = new Integer((int) (D - 4715L));
        }

        // Calculate fractional part as hours, minutes, and seconds
        Double dhr = new Double(24.0 * F);
        hour = new Integer(dhr.intValue());
        Double dmin = new Double((dhr.doubleValue() - (double) dhr.longValue()) * 60.0);
        minute = new Integer(dmin.intValue());
        Double dsec = new Double((dmin.doubleValue() - (double) dmin.longValue()) * 60.0);
        second = new Integer(dsec.intValue());

    }

    /**
     * Calculate day of week class attribute for class attribute jd
     */
    private void calcDayOfWeek() {
        JulianDay nJd = new JulianDay(getJDN());
        nJd.setStartOfDay();
        double nJdn = nJd.getJDN() + 1.5;
        int dow = (int) (nJdn % 7);
        dayOfWeek = new Integer(dow);
    }

    /**
     * Calculate day of year for jd (jd is a class attribute)
     */
    private void calcDayOfYear() {
        JulianDay julCal = new JulianDay();
        julCal.set(year.intValue(), 0, 1);
        double doy = jd.doubleValue() - julCal.getJDN();
        int idoy = (int) doy;
        dayOfYear = new Integer(idoy);
    }

    /**
     * Calculate Julian Date class attribute for class attributes year, month,
     * date, hour, minute, and second
     */
    private void calcJD() {
        int mo = month.intValue() + 1;
        int da = date.intValue();
        int yr = year.intValue();
        int A = 0;
        int B = 0;
        int C = 0;
        int D = 0;

        if (mo <= 2) {
            yr--;
            mo += 12;
        } else {
            mo = month.intValue() + 1;
        }

        if ((year.intValue() > 1582) || ((year.intValue() == 1582) && (month.intValue() >= 10) && (date.intValue() >= 15))) {
            Double a1 = new Double((double) yr / 100.0);
            A = a1.intValue();
            Double b1 = new Double((double) A / 4.0);
            B = 2 - A + b1.intValue();
        } else {
            B = 0;
        }

        Double c1 = new Double(365.25 * (double) yr);
        if (yr < 0) {
            c1 = new Double(365.25 * (double) yr - 0.75);
        }

        C = c1.intValue();
        Double d1 = new Double(30.6001 * (mo + 1));
        D = d1.intValue();

        double jdd = B + C + D + da + (hour.doubleValue() / 24.0) +
                (minute.doubleValue() / 1440.0) + (second.doubleValue() / 86400.0) +
                1720994.5;
        jd = new Double(jdd);

    }

    /**
     * Returns time difference in days between date specified and the JulianDay of this object
     * (parameter date-this date)
     *
     * @param date com.raben.util.JulianDate
     * @return double
     */
    public double diff(JulianDay date) {
        return date != null ? date.getJDN() - getJDN() : Double.NaN;
    }

    /**
     * Returns true if Julian day number is within 0.001 of parameter jd
     *
     * @param jd double
     * @return boolean
     */
    public boolean equals(double jd) {
        return Math.abs(jd - getJDN()) < 0.001 ? true : false;
    }

    /**
     * Return true if JulianDates are equal, false otherwise
     *
     * @param date com.raben.util.JulianDate
     * @return boolean
     */
    public boolean equals(JulianDay date) {
        boolean retVal = false;

        if (date != null) {
            retVal = equals(date.getJDN());
        }

        return retVal;

    }

    /**
     * Returns the specified field
     *
     * @param field int The specified field
     * @return int The field value
     */
    public final int get(int field) {

        switch (field) {
            case YEAR:
                return year.intValue();
            case MONTH:
                return month.intValue();
            case DAY_OF_MONTH:
                return date.intValue();
            case HOUR:
                int hr = hour.intValue();
                hr = hr > 12 ? hr -= 12 : hr;
                return hr;
            case HOUR_OF_DAY:
                return hour.intValue();
            case MINUTE:
                return minute.intValue();
            case SECOND:
                return second.intValue();
            case DAY_OF_WEEK:
                calcDayOfWeek();
                return dayOfWeek.intValue();
            case DAY_OF_YEAR:
                calcDayOfYear();
                return dayOfYear.intValue();
            default:
                return -1; // ISSUE - should throw exception? - what does Calendar do?
        }

    }

    /*
    * Get the UTC date/time string in the format yyyy-mm-dd hh:mm:ss
    * If the dateFormat is set, the date must be more recent than Jan 1, 1970
    * otherwise the empty string "" will be returned.)
    * @return java.lang.String
    */
    public String getDateTimeStr() {
        String retStr = "";

        if ((dateFormat != null) && (getJDN() >= EPOCH_1970)) {
            dateFormat.setTimeZone(tz);
            retStr = dateFormat.format(getTime());
        } else {
            StringBuffer strBuf = new StringBuffer(fmt4Dig.format(get(JulianDay.YEAR)));
            strBuf.append("-");
            strBuf.append(fmt2Dig.format(get(JulianDay.MONTH) + 1));
            strBuf.append("-");
            strBuf.append(fmt2Dig.format(get(JulianDay.DATE)));
            strBuf.append(" ");
            strBuf.append(fmt2Dig.format(get(JulianDay.HOUR_OF_DAY)));
            strBuf.append(":");
            strBuf.append(fmt2Dig.format(get(JulianDay.MINUTE)));
            strBuf.append(":");
            strBuf.append(fmt2Dig.format(get(JulianDay.SECOND)));
            retStr = strBuf.toString();
        }
        return retStr;
    }

    /**
     * Returns the Julian Date Number as a double
     *
     * @return double
     */
    public final double getJDN() {
        if (jd == null) {
            calcJD();
        }

        calcJD();

        return jd.doubleValue();
    }

    /**
     * Returns milli-seconds since Jan 1, 1970
     *
     * @return long
     */
    public long getMilliSeconds() {
        //JulianDay jd1970=new JulianDay("1970-01-01 0:00");
        //double diff=getJDN()-jd1970.getJDN();
        double diff = getJDN() - EPOCH_1970;
        return (long) (diff * 86400000.0);
    }

    /**
     * Return the modified Julian date
     *
     * @return double
     */
    public final double getMJD() {

        return (getJDN() - 2400000.5);
    }

    /**
     * Return date as YYYYMMDDHHSS string with the least unit to be returned specified
     * For example to to return YYYYMMDD specify least unit as JulianDay.DATE
     *
     * @param leastUnit int least unit to be returned
     */
    public String getYMD(int leastUnit) {

        StringBuffer retBuf = new StringBuffer();
        int yr = get(JulianDay.YEAR);
        int mo = get(JulianDay.MONTH) + 1;
        int da = get(JulianDay.DATE);
        int hr = get(JulianDay.HOUR_OF_DAY);
        int min = get(JulianDay.MINUTE);
        int sec = get(JulianDay.SECOND);

        String yrStr = fmt4Dig.format(yr);

        String moStr = fmt2Dig.format(mo);
        String daStr = fmt2Dig.format(da);
        String hrStr = fmt2Dig.format(hr);
        String minStr = fmt2Dig.format(min);
        String secStr = fmt2Dig.format(sec);

        switch (leastUnit) {
            case JulianDay.YEAR:
                retBuf.append(yrStr);
                break;

            case JulianDay.MONTH:
                retBuf.append(yrStr);
                retBuf.append(moStr);
                break;

            case JulianDay.DATE:
                retBuf.append(yrStr);
                retBuf.append(moStr);
                retBuf.append(daStr);
                break;

            case JulianDay.HOUR_OF_DAY:
            case JulianDay.HOUR:
                retBuf.append(yrStr);
                retBuf.append(moStr);
                retBuf.append(daStr);
                retBuf.append(hrStr);
                break;

            case JulianDay.MINUTE:
                retBuf.append(yrStr);
                retBuf.append(moStr);
                retBuf.append(daStr);
                retBuf.append(hrStr);
                retBuf.append(minStr);
                break;

            case JulianDay.SECOND:
                retBuf.append(yrStr);
                retBuf.append(moStr);
                retBuf.append(daStr);
                retBuf.append(hrStr);
                retBuf.append(minStr);
                retBuf.append(secStr);
                break;
        }

        return retBuf.toString();

    }

    /**
     * This method sets Julian day or modified Julian day
     *
     * @param field int Field to be changed
     * @param value double The value the field is set to
     *              ISSUE - double values are truncated when setting
     *              YEAR, MONTH<DATE, HOUR,MINUTE, and SECOND - this is not
     *              what should happen. (Should be able to set date to 1.5 to be
     *              the 1st day of month plus 12 hours).
     */
    public void set(int field, double value) {
        int ivalue = (int) value;

        switch (field) {

            case JD:
                jd = new Double(value);
                calcCalDate();
                break;

            case MJD:
                jd = new Double(value + 2400000.5);
                calcCalDate();
                break;

            case YEAR:
                year = new Integer(ivalue);
                calcJD();
                break;

            case MONTH:
                if (ivalue > 11) {
                    int yr = year.intValue() + 1;
                    set(YEAR, ivalue);
                    ivalue -= 11;
                }
                month = new Integer(ivalue);
                calcJD();
                break;

            case DATE:
                date = new Integer(ivalue);
                calcJD();
                break;

            case HOUR_OF_DAY:
            case HOUR:
                hour = new Integer(ivalue);
                while (hour.intValue() >= 24) {
                    add(DATE, 1);
                    hour = new Integer(hour.intValue() - 24);
                }
                calcJD();
                break;

            case MINUTE:
                minute = new Integer(ivalue);
                while (minute.intValue() >= 60) {
                    add(HOUR, 1);
                    minute = new Integer(minute.intValue() - 60);
                }
                calcJD();
                break;

            case SECOND:
                second = new Integer(ivalue);
                while (second.intValue() >= 60) {
                    add(MINUTE, 1);
                    second = new Integer(second.intValue() - 60);
                }
                calcJD();
                break;

        }

    }

    /**
     * Set various JulianCalendar fields
     * Example:
     * JulianDay jd=new JulianDay();
     * jd.set(Calendar.YEAR,1999);
     *
     * @param field int The field to be set
     * @param value int The field value
     */
    public final void set(int field, int value) {

        switch (field) {
            case YEAR:
                year = new Integer(value);
                break;

            case MONTH:
                month = new Integer(value);
                break;

            case DATE:
                date = new Integer(value);
                break;

            case HOUR_OF_DAY:
            case HOUR:
                hour = new Integer(value);
                break;

            case MINUTE:
                minute = new Integer(value);
                break;

            case SECOND:
                second = new Integer(value);
                break;
        }
        calcJD();

    }

    /**
     * Set year, month, and day
     *
     * @param year  int
     * @param month int Note - January is 0, December is 11
     * @param date  int
     */
    public final void set(int year, int month, int date) {
        this.year = new Integer(year);
        this.month = new Integer(month);
        this.date = new Integer(date);
        this.hour = new Integer(0);
        this.minute = new Integer(0);
        this.second = new Integer(0);
        calcJD();
    }

    /**
     * Set year, month,day, hour and minute
     *
     * @param year   int
     * @param month  int January is 0, Dec is 11
     * @param date   int
     * @param hour   int
     * @param minute int
     */
    public final void set(int year, int month, int date, int hour, int minute) {
        this.year = new Integer(year);
        this.month = new Integer(month);
        this.date = new Integer(date);
        this.hour = new Integer(hour);
        this.minute = new Integer(minute);
        this.second = new Integer(0);
        calcJD();
    }

    /**
     * Set year month, day, hour, minute and second
     *
     * @param year   int
     * @param month  int January is 0, December is 11
     * @param date   int
     * @param hour   int
     * @param minute int
     * @param second int
     */
    public final void set(int year, int month, int date, int hour, int minute, int second) {
        this.year = new Integer(year);
        this.month = new Integer(month);
        this.date = new Integer(date);
        this.hour = new Integer(hour);
        this.minute = new Integer(minute);
        this.second = new Integer(second);
        calcJD();
    }

    /**
     * Set date/time from string
     *
     * @param str java.lang.String
     */
    public void setDateTime(String str) {
        try {
            int vals[] = {0, 0, 0, 0, 0, 0};
            str = str.replace('T', ' ');
            StringTokenizer tok = new StringTokenizer(str, "/:- ");

            if (tok.countTokens() > 0) {

                // Check if its not a database time format yyyy-mm-dd
                int j = str.indexOf("-");

                if ((j == -1) && (tok.countTokens() == 1)) {
                    setYMD(str);
                } else {
                    int i = 0;

                    while (tok.hasMoreTokens()) {
                        vals[i++] = Integer.parseInt(tok.nextToken());
                    }

                    set(vals[0], vals[1] - 1, vals[2], vals[3], vals[4], vals[5]);

                }

            }

        } catch (NumberFormatException e) {
            throw new Error(e.toString());
        }

        calcJD();


    }

    /**
     * set hour to 23, minute and second to 59
     */
    public void setEndOfDay() {
        int yr = get(YEAR);
        int mo = get(MONTH);
        int da = get(DATE);
        set(yr, mo, da, 23, 59, 59);
    }

    /**
     * Set hour,minute, and second to 0
     */
    public void setStartOfDay() {
        int yr = get(YEAR);
        int mo = get(MONTH);
        int da = get(DATE);
        set(yr, mo, da, 0, 0, 0);
    }

    /**
     * Set date from Java Date
     *
     * @param dat java.util.Date
     */
    public final void setTime(Date dat) {
        Calendar cal = new GregorianCalendar(tz);
        cal.setTime(dat);
        year = new Integer(cal.get(Calendar.YEAR));
        month = new Integer(cal.get(Calendar.MONTH));
        date = new Integer(cal.get(Calendar.DATE));
        hour = new Integer(cal.get(Calendar.HOUR_OF_DAY));
        minute = new Integer(cal.get(Calendar.MINUTE));
        second = new Integer(cal.get(Calendar.SECOND));
        //System.out.println("JulianCalendar.setTime: year="+year+" month="+month+" date="+date+" hour="+hour+" minute="+minute+" second="+second);
        calcJD();
        //System.out.println("jd="+jd);
    }

    /**
     * Set date from sting in the form YYYYMMDDhhmmss (YYYY=year MM=month DD=day hh=hr mm=min ss=sec)
     *
     * @param str java.lang.String
     */
    public void setYMD(String str) {

        int vals[] = {0, 0, 0, 0, 0, 0};

        if (str.length() >= 4) {
            vals[0] = Integer.parseInt(str.substring(0, 4));
        }
        if (str.length() >= 6) {
            vals[1] = Integer.parseInt(str.substring(4, 6));
        }

        if (str.length() >= 8) {
            vals[2] = Integer.parseInt(str.substring(6, 8));
        }

        if (str.length() >= 10) {
            vals[3] = Integer.parseInt(str.substring(8, 10));
        }
        if (str.length() >= 12) {
            vals[4] = Integer.parseInt(str.substring(10, 12));
        }

        if (str.length() >= 14) {
            vals[5] = Integer.parseInt(str.substring(12, 14));
        }

        set(YEAR, vals[0]);
        set(MONTH, vals[1] - 1);
        set(DATE, vals[2]);
        set(HOUR_OF_DAY, vals[3]);
        set(MINUTE, vals[4]);
        set(SECOND, vals[5]);
    }

    public final String toString() {

        StringBuffer buf = new StringBuffer("JulianDay[jdn=");
        buf.append(getJDN());
        buf.append(",yr=");
        buf.append(get(Calendar.YEAR));
        buf.append(",mo=");
        buf.append(get(Calendar.MONTH));
        buf.append(",da=");
        buf.append(get(Calendar.DATE));
        buf.append(",hr=");
        buf.append(get(Calendar.HOUR_OF_DAY));
        buf.append(",min=");
        buf.append(get(Calendar.MINUTE));
        buf.append(",sec=");
        buf.append(get(Calendar.SECOND));
        buf.append(",dayOfWeek=");
        buf.append(get(DAY_OF_WEEK));
        buf.append(",dayOfYear=");
        buf.append(get(DAY_OF_YEAR));
        buf.append("]");

        return buf.toString();
    }

    /**
     * Return clone of JulianDay object
     *
     * @return Object;
     */
    public Object clone() {
        JulianDay clone = null;
        try {
            clone = (JulianDay) super.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return clone;
    }

    /**
     * Set SimpleDateFormat string
     * ISSUE - only valid after Jan 1, 1970
     */
    public void setDateFormat(java.lang.String formatStr) {
        if ((formatStr != null) && (formatStr.length() > 0)) {
            dateFormat = new SimpleDateFormat(formatStr);
        }
    }

    /**
     * Set SimpleDateFormat for displaying date/time string
     *
     * @param dateFormat SimpleDateFormat
     */
    public void setDateFormat(SimpleDateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * Return Java Date
     *
     * @return Date
     */
    public Date getTime() {
        return new Date(getMilliSeconds());
    }

    /**
     * Update JulianDay to current time
     */
    public void update() {
        Calendar cal = new GregorianCalendar(tz);
        setTime(cal.getTime());
    }

    /**
     * Get increment in days given time unit and increment
     *
     * @param unit Time unit (DATE,HOUR,HOUR_OF_DAY,MINUTE, or SECOND
     * @param incr Time increment in unit specified
     * @return double Increment in days
     * @throws unit is not Julian.DATE, HOUR, HOUR_OF_DAY, MINUTE or SECOND
     */
    public static double getIncrement(int unit, int incr) {
        double retVal = 0.0;

        switch (unit) {
            case DATE:
                retVal = incr;
                break;
            case HOUR:
            case HOUR_OF_DAY:
                retVal = incr / 24.0;
                break;
            case MINUTE:
                retVal = incr / 1440.0;
                break;
            case SECOND:
                retVal = incr / 86400.0;
                break;
            default:
                StringBuffer errMsg = new StringBuffer("JulianDay.getIncrement unit=");
                errMsg.append(unit);

                if ((unit > 0) && (unit < TIME_UNIT.length)) {
                    errMsg.append(" (");
                    errMsg.append(TIME_UNIT[unit]);
                    errMsg.append(" )");
                }

                throw new IllegalArgumentException(errMsg.toString());

        }

        return retVal;
    }

    /**
     * Get java Calendar equivalent of Julian Day
     *
     * @return Calendar
     */
    public java.util.Calendar getCalendar() {
        Calendar cal = GregorianCalendar.getInstance(tz);
        cal.set(get(YEAR), get(MONTH), get(DATE), get(HOUR_OF_DAY),
                get(MINUTE), get(SECOND));
        return cal;
    }

=======
/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.javafx;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

import com.sun.javafx.stage.StageHelper;
import javafx.geometry.NodeOrientation;

/**
 * Some basic utilities which need to be in java (for shifting operations or
 * other reasons), which are not toolkit dependent.
 *
 */
public class Utils {

    /***************************************************************************
     *                                                                         *
     * Math-related utilities                                                  *
     *                                                                         *
     **************************************************************************/

    /**
     * Simple utility function which clamps the given value to be strictly
     * between the min and max values.
     */
    public static float clamp(float min, float value, float max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    /**
     * Simple utility function which clamps the given value to be strictly
     * between the min and max values.
     */
    public static int clamp(int min, int value, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    /**
     * Simple utility function which clamps the given value to be strictly
     * between the min and max values.
     */
    public static double clamp(double min, double value, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    /**
     * Simple utility function which clamps the given value to be strictly
     * above the min value.
     */
    public static double clampMin(double value, double min) {
        if (value < min) return min;
        return value;
    }

    /**
     * Simple utility function which clamps the given value to be strictly
     * above the min value.
     */
    public static int clampMin(int value, int min) {
        if (value < min) return min;
        return value;
    }

    /**
     * Simple utility function which clamps the given value to be strictly
     * under the max value.
     */
    public static float clampMax(float value, float max) {
        if (value > max) return max;
        return value;
    }

    /**
     * Simple utility function which clamps the given value to be strictly
     * under the max value.
     */
    public static int clampMax(int value, int max) {
        if (value > max) return max;
        return value;
    }

    /**
     * Utility function which returns either {@code less} or {@code more}
     * depending on which {@code value} is closer to. If {@code value}
     * is perfectly between them, then either may be returned.
     */
    public static double nearest(double less, double value, double more) {
        double lessDiff = value - less;
        double moreDiff = more - value;
        if (lessDiff < moreDiff) return less;
        return more;
    }

    /***************************************************************************
     *                                                                         *
     * String-related utilities                                                *
     *                                                                         *
     **************************************************************************/

    /**
     * Simple helper function which works on both desktop and mobile for
     * stripping newlines. The problem we encountered when attempting this in
     * FX was that there is no character literal in FX and no way that I could
     * see to efficiently create characters representing newline and so forth.
     */
    public static String stripNewlines(String s) {
        if (s == null) return null;
        return s.replace('\n', ' ');
    }

    /**
     * Helper to remove leading and trailing quotes from a string.
     * Works with single or double quotes. 
     */
    public static String stripQuotes(String str) {
        if (str == null) return str;
        if (str.length() == 0) return str;

        int beginIndex = 0;
        final char openQuote = str.charAt(beginIndex);        
        if ( openQuote == '\"' || openQuote=='\'' ) beginIndex += 1;

        int endIndex = str.length();
        final char closeQuote = str.charAt(endIndex - 1);
        if ( closeQuote == '\"' || closeQuote=='\'' ) endIndex -= 1;

        if ((endIndex - beginIndex) < 0) return str;

        // note that String.substring returns "this" if beginIndex == 0 && endIndex == count
        // or a new string that shares the character buffer with the original string.
        return str.substring(beginIndex, endIndex);
    }

    /**
     * Because mobile doesn't have string.split(s) function, this function
     * was written.
     */
    public static String[] split(String str, String separator) {
        if (str == null || str.length() == 0) return new String[] { };
        if (separator == null || separator.length() == 0) return new String[] { };
        if (separator.length() > str.length()) return new String[] { };

        java.util.List<String> result = new java.util.ArrayList<String>();

        int index = str.indexOf(separator);
        while (index >= 0) {
            String newStr = str.substring(0, index);
            if (newStr != null && newStr.length() > 0) {
                result.add(newStr);
            }
            str = str.substring(index + separator.length());
            index = str.indexOf(separator);
        }

        if (str != null && str.length() > 0) {
            result.add(str);
        }

        return result.toArray(new String[] { });
    }

    /**
     * Because mobile doesn't have string.contains(s) function, this function
     * was written.
     */
    public static boolean contains(String src, String s) {
        if (src == null || src.length() == 0) return false;
        if (s == null || s.length() == 0) return false;
        if (s.length() > src.length()) return false;

        return src.indexOf(s) > -1;
    }

    /***************************************************************************
     *                                                                         *
     * Color-related utilities                                                 *
     *                                                                         *
     **************************************************************************/

    /**
     * Calculates a perceptual brightness for a color between 0.0 black and 1.0 while
     */
    public static double calculateBrightness(Color color) {
          return  (0.3*color.getRed()) + (0.59*color.getGreen()) + (0.11*color.getBlue());
    }

    /**
     * Derives a lighter or darker of a given color.
     *
     * @param c           The color to derive from
     * @param brightness  The brightness difference for the new color -1.0 being 100% dark which is always black, 0.0 being
     *                    no change and 1.0 being 100% lighter which is always white
     */
    public static Color deriveColor(Color c, double brightness) {
        double baseBrightness = calculateBrightness(c);
        double calcBrightness = brightness;
        // Fine adjustments to colors in ranges of brightness to adjust the contrast for them
        if (brightness > 0) {
            if (baseBrightness > 0.85) {
                calcBrightness = calcBrightness * 1.6;
            } else if (baseBrightness > 0.6) {
                // no change
            } else if (baseBrightness > 0.5) {
                calcBrightness = calcBrightness * 0.9;
            } else if (baseBrightness > 0.4) {
                calcBrightness = calcBrightness * 0.8;
            } else if (baseBrightness > 0.3) {
                calcBrightness = calcBrightness * 0.7;
            } else {
                calcBrightness = calcBrightness * 0.6;
            }
        } else {
            if (baseBrightness < 0.2) {
                calcBrightness = calcBrightness * 0.6;
            }
        }
        // clamp brightness
        if (calcBrightness < -1) { calcBrightness = -1; } else if (calcBrightness > 1) {calcBrightness = 1;}
        // window two take the calculated brightness multiplyer and derive color based on source color
        double[] hsb = RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue());
        // change brightness
        if (calcBrightness > 0) { // brighter
            hsb[1] *= 1 - calcBrightness;
            hsb[2] += (1 - hsb[2]) * calcBrightness;
        } else { // darker
            hsb[2] *=  calcBrightness + 1;
        }
        // clip saturation and brightness
        if (hsb[1] < 0) { hsb[1] = 0;} else if (hsb[1] > 1) {hsb[1] = 1;}
        if (hsb[2] < 0) { hsb[2] = 0;} else if (hsb[2] > 1) {hsb[2] = 1;}
        // convert back to color
        Color c2 = Color.hsb((int)hsb[0], hsb[1], hsb[2],c.getOpacity());
        return Color.hsb((int)hsb[0], hsb[1], hsb[2],c.getOpacity());

     /*   var hsb:Number[] = RGBtoHSB(c.red,c.green,c.blue);
        // change brightness
        if (brightness > 0) {
            //var bright:Number = brightness * (1-calculateBrightness(c));
            var bright:Number = if (calculateBrightness(c)<0.65 and brightness > 0.5) {
                    if (calculateBrightness(c)<0.2) then brightness * 0.55 else brightness * 0.7
            } else brightness;
            // brighter
            hsb[1] *= 1 - bright;
            hsb[2] += (1 - hsb[2]) * bright;
        } else {
            // darker
            hsb[2] *= brightness+1;
        }
        // clip saturation and brightness
        if (hsb[1] < 0) { hsb[1] = 0;} else if (hsb[1] > 1) {hsb[1] = 1}
        if (hsb[2] < 0) { hsb[2] = 0;} else if (hsb[2] > 1) {hsb[2] = 1}
        // convert back to color
        return Color.hsb(hsb[0],hsb[1],hsb[2]) */
    }

    /**
     * interpolate at a set {@code position} between two colors {@code color1} and {@code color2}.
     * The interpolation is done is linear RGB color space not the default sRGB color space.
     */
    private static Color interpolateLinear(double position, Color color1, Color color2) {
        Color c1Linear = convertSRGBtoLinearRGB(color1);
        Color c2Linear = convertSRGBtoLinearRGB(color2);
        return convertLinearRGBtoSRGB(Color.color(
            c1Linear.getRed()     + (c2Linear.getRed()     - c1Linear.getRed())     * position,
            c1Linear.getGreen()   + (c2Linear.getGreen()   - c1Linear.getGreen())   * position,
            c1Linear.getBlue()    + (c2Linear.getBlue()    - c1Linear.getBlue())    * position,
            c1Linear.getOpacity() + (c2Linear.getOpacity() - c1Linear.getOpacity()) * position
        ));
    }

    /**
     * Get the color at the give {@code position} in the ladder of color stops
     */
    private static Color ladder(final double position, final Stop[] stops) {
        Stop prevStop = null;
        for (int i=0; i<stops.length; i++) {
            Stop stop = stops[i];
            if(position <= stop.getOffset()){
                if (prevStop == null) {
                    return stop.getColor();
                } else {
                    return interpolateLinear((position-prevStop.getOffset())/(stop.getOffset()-prevStop.getOffset()), prevStop.getColor(), stop.getColor());
                }
            }
            prevStop = stop;
        }
        // position is greater than biggest stop, so will we biggest stop's color
        return prevStop.getColor();
    }

    /**
     * Get the color at the give {@code position} in the ladder of color stops
     */
    public static Color ladder(final Color color, final Stop[] stops) {
        return ladder(calculateBrightness(color), stops);
    }

    public static double[] HSBtoRGB(double hue, double saturation, double brightness) {
        // normalize the hue
        double normalizedHue = ((hue % 360) + 360) % 360;
        hue = normalizedHue/360;

        double r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = brightness;
        } else {
            double h = (hue - Math.floor(hue)) * 6.0;
            double f = h - java.lang.Math.floor(h);
            double p = brightness * (1.0 - saturation);
            double q = brightness * (1.0 - saturation * f);
            double t = brightness * (1.0 - (saturation * (1.0 - f)));
            switch ((int) h) {
                case 0:
                    r = brightness;
                    g = t;
                    b = p;
                    break;
                case 1:
                    r = q;
                    g = brightness;
                    b = p;
                    break;
                case 2:
                    r = p;
                    g = brightness;
                    b = t;
                    break;
                case 3:
                    r = p;
                    g = q;
                    b = brightness;
                    break;
                case 4:
                    r = t;
                    g = p;
                    b = brightness;
                    break;
                case 5:
                    r = brightness;
                    g = p;
                    b = q;
                    break;
            }
        }
        double[] f = new double[3];
        f[0] = r;
        f[1] = g;
        f[2] = b;
        return f;
    }

    public static double[] RGBtoHSB(double r, double g, double b) {
        double hue, saturation, brightness;
        double[] hsbvals = new double[3];
        double cmax = (r > g) ? r : g;
        if (b > cmax) cmax = b;
        double cmin = (r < g) ? r : g;
        if (b < cmin) cmin = b;

        brightness = cmax;
        if (cmax != 0)
            saturation = (double) (cmax - cmin) / cmax;
        else
            saturation = 0;

        if (saturation == 0) {
            hue = 0;
        } else {
            double redc = (cmax - r) / (cmax - cmin);
            double greenc = (cmax - g) / (cmax - cmin);
            double bluec = (cmax - b) / (cmax - cmin);
            if (r == cmax)
                hue = bluec - greenc;
            else if (g == cmax)
                hue = 2.0 + redc - bluec;
            else
                hue = 4.0 + greenc - redc;
            hue = hue / 6.0;
            if (hue < 0)
                hue = hue + 1.0;
        }
        hsbvals[0] = hue * 360;
        hsbvals[1] = saturation;
        hsbvals[2] = brightness;
        return hsbvals;
    }

    /**
     * Helper function to convert a color in sRGB space to linear RGB space.
     */
    public static Color convertSRGBtoLinearRGB(Color color) {
        double[] colors = new double[] { color.getRed(), color.getGreen(), color.getBlue() };
        for (int i=0; i<colors.length; i++) {
            if (colors[i] <= 0.04045) {
                colors[i] = colors[i] / 12.92;
            } else {
                colors[i] = Math.pow((colors[i] + 0.055) / 1.055, 2.4);
            }
        }
        return Color.color(colors[0], colors[1], colors[2], color.getOpacity());
    }

    /**
     * Helper function to convert a color in linear RGB space to SRGB space.
     */
    public static Color convertLinearRGBtoSRGB(Color color) {
        double[] colors = new double[] { color.getRed(), color.getGreen(), color.getBlue() };
        for (int i=0; i<colors.length; i++) {
            if (colors[i] <= 0.0031308) {
                colors[i] = colors[i] * 12.92;
            } else {
                colors[i] = (1.055 * Math.pow(colors[i], (1.0 / 2.4))) - 0.055;
            }
        }
        return Color.color(colors[0], colors[1], colors[2], color.getOpacity());
    }

    public static <E extends Node> List<E> getManaged(List<E>nodes) {
        List<E> managed = new ArrayList<E>();
        for (E e : nodes) {
            if (e != null && e.isManaged()) {
                managed.add(e);
            }
        }
        return managed;
    }

    /** helper function for calculating the sum of a series of numbers */
    public static double sum(double[] values) {
   	double sum = 0;
    	for (double v : values) sum = sum+v;
    	return sum / values.length;
}

    /**
     * Returns a Point2D that represents an x,y location that should safely position
     * the given node relative to the given parent node.
     *
     * If reposition is set to be false, then the node will be positioned with no
     * regard to it's position being offscreen. Conversely, setting reposition to be
     * true will result in the point being shifted such that the entire node is onscreen.
     *
     * How this works is largely based on the provided hpos and vpos parameters, with
     * the repositioned node trying not to overlap the parent unless absolutely necessary.
     */
    public static Point2D pointRelativeTo(Node parent, Node node, HPos hpos, VPos vpos, boolean reposition) {
        final double nodeWidth = node.getLayoutBounds().getWidth();
        final double nodeHeight = node.getLayoutBounds().getHeight();
        return pointRelativeTo(parent, nodeWidth, nodeHeight, hpos, vpos, 0, 0, reposition);
    }

    public static Point2D pointRelativeTo(Node parent, double anchorWidth, double anchorHeight,
             HPos hpos, VPos vpos, boolean reposition)
    {
        return pointRelativeTo(parent, anchorWidth, anchorHeight, hpos, vpos, 0, 0, reposition);
    }

    public static Point2D pointRelativeTo(Node parent, Node node, HPos hpos,
            VPos vpos, double dx, double dy, boolean reposition)
    {
        final double nodeWidth = node.getLayoutBounds().getWidth();
        final double nodeHeight = node.getLayoutBounds().getHeight();
        return pointRelativeTo(parent, nodeWidth, nodeHeight, hpos, vpos, dx, dy, reposition);
    }

    public static Point2D pointRelativeTo(Node parent, double anchorWidth,
            double anchorHeight, HPos hpos, VPos vpos, double dx, double dy,
            boolean reposition)
    {
        double parentXOffset = getOffsetX(parent);
        final double parentYOffset = getOffsetY(parent);
        final Bounds parentBounds = getBounds(parent);
        Scene scene = parent.getScene();
        NodeOrientation orientation = parent.getEffectiveNodeOrientation();

        if (orientation == NodeOrientation.RIGHT_TO_LEFT) {
            if (hpos == HPos.LEFT) {
                hpos = HPos.RIGHT;
            } else if (hpos == HPos.RIGHT) {
                hpos = HPos.LEFT;
            }
        }

        double layoutX = positionX(parentXOffset, parentBounds, anchorWidth, hpos) + dx;
        final double layoutY = positionY(parentYOffset, parentBounds, anchorHeight, vpos) + dy;

        if (orientation == NodeOrientation.RIGHT_TO_LEFT && hpos == HPos.CENTER) {
            //TODO - testing for an instance of Stage seems wrong but works for menus
            if (scene.getWindow() instanceof Stage) {
                layoutX = layoutX + parentBounds.getWidth() - anchorWidth + (dx * 2);
            } else {
                layoutX = layoutX - parentBounds.getWidth() - anchorWidth;
            }
        }

        if (reposition) {
            return pointRelativeTo(parent, anchorWidth, anchorHeight, layoutX, layoutY, hpos, vpos);
        } else {
            return new Point2D(layoutX, layoutY);
        }
    }

    /**
     * Returns a Point2D that represents an x,y location that should safely position
     * the given node relative to the given parent node.
     *
     * The provided x and y values are offsets from the parent node. This allows for
     * the node to be positioned relative to the parent using exact coordinates.
     *
     * If reposition is set to be false, then the node will be positioned with no
     * regard to it's position being offscreen. Conversely, setting reposition to be
     * true will result in the point being shifted such that the entire node is onscreen.
     */
    public static Point2D pointRelativeTo(Node parent, Node node, double x, double y, boolean reposition) {
        final Bounds bounds = parent.localToScreen(parent.getBoundsInLocal());
        final double layoutX = x + bounds.getMinX();
        final double layoutY = y + bounds.getMinY();

        if (reposition) {
            return pointRelativeTo(parent, node, layoutX, layoutY, null, null);
        } else {
            return new Point2D(layoutX, layoutY);
        }
    }

    /**
     * Returns a Point2D that represents an x,y location that should safely position
     * the given node relative to the given parent node.
     *
     * <b>Note</b>: Unlike other functions provided in this class, the provided x
     * and y values are <b>not</b> offsets from the parent node - they are relative
     * to the screen. This reduces the utility of this function, and in many cases
     * you're better off using the more specific functions provided.
     *
     * How this works is largely based on the provided hpos and vpos parameters, with
     * the repositioned node trying not to overlap the parent unless absolutely necessary.
     *
     * This function implicitly has the reposition argument set to true, which means
     * that the returned Point2D be such that the node will be fully on screen.
     *
     * Don't use the BASELINE vpos, it doesn't make sense and would produce wrong result.
     */
    public static Point2D pointRelativeTo(Node parent, Node node, double screenX,
            double screenY, HPos hpos, VPos vpos)
    {
        final double nodeWidth = node.getLayoutBounds().getWidth();
        final double nodeHeight = node.getLayoutBounds().getHeight();

        return pointRelativeTo(parent, nodeWidth, nodeHeight, screenX, screenY, hpos, vpos);
    }

    /**
     * This is the fallthrough function that most other functions fall into. It takes
     * care specifically of the repositioning of the item such that it remains onscreen
     * as best it can, given it's unique qualities.
     *
     * As will all other functions, this one returns a Point2D that represents an x,y
     * location that should safely position the item onscreen as best as possible.
     *
     * Note that <code>width</code> and <height> refer to the width and height of the
     * node/popup that is needing to be repositioned, not of the parent.
     *
     * Don't use the BASELINE vpos, it doesn't make sense and would produce wrong result.
     */
    public static Point2D pointRelativeTo(Object parent, double width,
            double height, double screenX, double screenY, HPos hpos, VPos vpos)
    {
        double finalScreenX = screenX;
        double finalScreenY = screenY;
        final double parentOffsetX = getOffsetX(parent);
        final double parentOffsetY = getOffsetY(parent);
        final Bounds parentBounds = getBounds(parent);

        // ...and then we get the bounds of this screen
        final Screen currentScreen = getScreen(parent);
        final Rectangle2D screenBounds =
                hasFullScreenStage(currentScreen)
                        ? currentScreen.getBounds()
                        : currentScreen.getVisualBounds();

        // test if this layout will force the node to appear outside
        // of the screens bounds. If so, we must reposition the item to a better position.
        // We firstly try to do this intelligently, so as to not overlap the parent if
        // at all possible.
        if (hpos != null) {
            // Firstly we consider going off the right hand side
            if ((finalScreenX + width) > screenBounds.getMaxX()) {
                finalScreenX = positionX(parentOffsetX, parentBounds, width, getHPosOpposite(hpos, vpos));
            }

            // don't let the node go off to the left of the current screen
            if (finalScreenX < screenBounds.getMinX()) {
                finalScreenX = positionX(parentOffsetX, parentBounds, width, getHPosOpposite(hpos, vpos));
            }
        }

        if (vpos != null) {
            // don't let the node go off the bottom of the current screen
            if ((finalScreenY + height) > screenBounds.getMaxY()) {
                finalScreenY = positionY(parentOffsetY, parentBounds, height, getVPosOpposite(hpos,vpos));
            }

            // don't let the node out of the top of the current screen
            if (finalScreenY < screenBounds.getMinY()) {
                finalScreenY = positionY(parentOffsetY, parentBounds, height, getVPosOpposite(hpos,vpos));
            }
        }

        // --- after all the moving around, we do one last check / rearrange.
        // Unlike the check above, this time we are just fully committed to keeping
        // the item on screen at all costs, regardless of whether or not that results
        /// in overlapping the parent object.
        if ((finalScreenX + width) > screenBounds.getMaxX()) {
            finalScreenX -= (finalScreenX + width - screenBounds.getMaxX());
        }
        if (finalScreenX < screenBounds.getMinX()) {
            finalScreenX = screenBounds.getMinX();
        }
        if ((finalScreenY + height) > screenBounds.getMaxY()) {
            finalScreenY -= (finalScreenY + height - screenBounds.getMaxY());
        }
        if (finalScreenY < screenBounds.getMinY()) {
            finalScreenY = screenBounds.getMinY();
        }

        return new Point2D(finalScreenX, finalScreenY);
    }

    /**
     * Returns a Point2D that represents an x,y location that should safely position
     * a node on screen assuming its width and height values are equal to the arguments given
     * to this function.
     *
     * In this situation, the provided screenX and screenY values are in screen coordinates, so
     * the reposition value is implicitly set to true. This means that after calling
     * this function you'll have a Point2D object representing new screen coordinates.
     */
    public static Point2D pointRelativeTo(Window parent, double width, double height, double screenX, double screenY) {
        return pointRelativeTo(parent, width, height, screenX, screenY, null, null);
    }

    /**
     * To facilitate multiple types of parent object, we unfortunately must allow for
     * Objects to be passed in. This method handles determining the x-axis offset of the
     * given Object from the screens (0,0) position. If the Object type is not supported,
     * 0 will be returned.
     */
    private static double getOffsetX(Object obj) {
        if (obj instanceof Node) {
            Scene scene = ((Node)obj).getScene();
            if ((scene == null) || (scene.getWindow() == null)) {
                return 0;
            }
            return scene.getX() + scene.getWindow().getX();
        } else if (obj instanceof Window) {
            return ((Window)obj).getX();
        } else {
            return 0;
        }
    }

    /**
     * To facilitate multiple types of parent object, we unfortunately must allow for
     * Objects to be passed in. This method handles determining the y-axis offset of the
     * given Object from the screens (0,0) position. If the Object type is not supported,
     * 0 will be returned.
     */
    private static double getOffsetY(Object obj) {
        if (obj instanceof Node) {
            Scene scene = ((Node)obj).getScene();
            if ((scene == null) || (scene.getWindow() == null)) {
                return 0;
            }
            return scene.getY() + scene.getWindow().getY();
        } else if (obj instanceof Window) {
            return ((Window)obj).getY();
        } else {
            return 0;
        }
    }

    /**
     * Utility function that returns the x-axis position that an object should be positioned at,
     * given the parent x-axis offset, the parents bounds, the width of the object, and
     * the required HPos.
     */
    private static double positionX(double parentXOffset, Bounds parentBounds, double width, HPos hpos) {
        if (hpos == HPos.CENTER) {
            // this isn't right, but it is needed for root menus to show properly
            return parentXOffset + parentBounds.getMinX();
        } else if (hpos == HPos.RIGHT) {
            return parentXOffset + parentBounds.getMaxX();
        } else if (hpos == HPos.LEFT) {
            return parentXOffset + parentBounds.getMinX() - width;
        } else {
            return 0;
        }
    }

    /**
     * Utility function that returns the y-axis position that an object should be positioned at,
     * given the parent y-axis offset, the parents bounds, the height of the object, and
     * the required VPos.
     *
     * The BASELINE vpos doesn't make sense here, 0 is returned for it.
     */
    private static double positionY(double parentYOffset, Bounds parentBounds, double height, VPos vpos) {
        if (vpos == VPos.BOTTOM) {
            return parentYOffset + parentBounds.getMaxY();
        } else if (vpos == VPos.CENTER) {
            return parentYOffset + parentBounds.getMinY();
        } else if (vpos == VPos.TOP) {
            return parentYOffset + parentBounds.getMinY() - height;
        } else {
            return 0;
        }
    }

    /**
     * To facilitate multiple types of parent object, we unfortunately must allow for
     * Objects to be passed in. This method handles determining the bounds of the
     * given Object. If the Object type is not supported, a default Bounds will be returned.
     */
    private static Bounds getBounds(Object obj) {
        if (obj instanceof Node) {
            return ((Node)obj).localToScene(((Node)obj).getBoundsInLocal());
        } else if (obj instanceof Window) {
            final Window window = (Window)obj;
            return new BoundingBox(0, 0, window.getWidth(), window.getHeight());
        } else {
            return new BoundingBox(0, 0, 0, 0);
        }
    }

    /*
     * Simple utitilty function to return the 'opposite' value of a given HPos, taking
     * into account the current VPos value. This is used to try and avoid overlapping.
     */
    private static HPos getHPosOpposite(HPos hpos, VPos vpos) {
        if (vpos == VPos.CENTER) {
            if (hpos == HPos.LEFT){
                return HPos.RIGHT;
            } else if (hpos == HPos.RIGHT){
                return HPos.LEFT;
            } else if (hpos == HPos.CENTER){
                return HPos.CENTER;
            } else {
                // by default center for now
                return HPos.CENTER;
            }
        } else {
            return HPos.CENTER;
        }
    }

    /*
     * Simple utitilty function to return the 'opposite' value of a given VPos, taking
     * into account the current HPos value. This is used to try and avoid overlapping.
     */
    private static VPos getVPosOpposite(HPos hpos, VPos vpos) {
        if (hpos == HPos.CENTER) {
            if (vpos == VPos.BASELINE){
                return VPos.BASELINE;
            } else if (vpos == VPos.BOTTOM){
                return VPos.TOP;
            } else if (vpos == VPos.CENTER){
                return VPos.CENTER;
            } else if (vpos == VPos.TOP){
                return VPos.BOTTOM;
            } else {
                // by default center for now
                return VPos.CENTER;
            }
        } else {
            return VPos.CENTER;
        }
    }

    public static boolean hasFullScreenStage(final Screen screen) {
        final List<Stage> allStages = StageHelper.getStages();

        for (final Stage stage: allStages) {
            if (stage.isFullScreen() && (getScreen(stage) == screen)) {
                return true;
            }
        }

        return false;
    }

    /*
     * Returns true if the primary Screen has VGA dimensions, in landscape or portrait mode.
     */
    public static boolean isVGAScreen() {
        Rectangle2D bounds = Screen.getPrimary().getBounds();
        return ((bounds.getWidth() == 640 && bounds.getHeight() == 480) ||
                (bounds.getWidth() == 480 && bounds.getHeight() == 640));
    }

    /*
     * Returns true if the primary Screen has QVGA dimensions, in landscape or portrait mode.
     */
    public static boolean isQVGAScreen() {
        Rectangle2D bounds = Screen.getPrimary().getBounds();
        return ((bounds.getWidth() == 320 && bounds.getHeight() == 240) ||
                (bounds.getWidth() == 240 && bounds.getHeight() == 320));
    }

    /**
     * This function attempts to determine the best screen given the parent object
     * from which we are wanting to position another item relative to. This is particularly
     * important when we want to keep items from going off screen, and for handling
     * multiple monitor support.
     */
    public static Screen getScreen(Object obj) {
        // handle dual monitors (be careful of minX/minY vs width/height).
        // we create a rectangle representing the menubar menu item...
        final double offsetX = getOffsetX(obj);
        final double offsetY = getOffsetY(obj);
        final Bounds parentBounds = getBounds(obj);

        final Rectangle2D rect = new Rectangle2D(
                offsetX + parentBounds.getMinX(),
                offsetY + parentBounds.getMinY(),
                parentBounds.getWidth(),
                parentBounds.getHeight());

        return getScreenForRectangle(rect);
    }

    public static Screen getScreenForRectangle(final Rectangle2D rect) {
        final List<Screen> screens = Screen.getScreens();

        final double rectX0 = rect.getMinX();
        final double rectX1 = rect.getMaxX();
        final double rectY0 = rect.getMinY();
        final double rectY1 = rect.getMaxY();

        Screen selectedScreen;

        selectedScreen = null;
        double maxIntersection = 0;
        for (final Screen screen: screens) {
            final Rectangle2D screenBounds = screen.getBounds();
            final double intersection =
                    getIntersectionLength(rectX0, rectX1,
                                          screenBounds.getMinX(),
                                          screenBounds.getMaxX())
                        * getIntersectionLength(rectY0, rectY1,
                                                screenBounds.getMinY(),
                                                screenBounds.getMaxY());

            if (maxIntersection < intersection) {
                maxIntersection = intersection;
                selectedScreen = screen;
            }
        }

        if (selectedScreen != null) {
            return selectedScreen;
        }

        selectedScreen = Screen.getPrimary();
        double minDistance = Double.MAX_VALUE;
        for (final Screen screen: screens) {
            final Rectangle2D screenBounds = screen.getBounds();
            final double dx = getOuterDistance(rectX0, rectX1,
                                               screenBounds.getMinX(),
                                               screenBounds.getMaxX());
            final double dy = getOuterDistance(rectY0, rectY1,
                                               screenBounds.getMinY(),
                                               screenBounds.getMaxY());
            final double distance = dx * dx + dy * dy;

            if (minDistance > distance) {
                minDistance = distance;
                selectedScreen = screen;
            }
        }

        return selectedScreen;
    }

    public static Screen getScreenForPoint(final double x, final double y) {
        final List<Screen> screens = Screen.getScreens();

        // first check whether the point is inside some screen
        for (final Screen screen: screens) {
            // can't use screen.bounds.contains, because it returns true for
            // the min + width point
            final Rectangle2D screenBounds = screen.getBounds();
            if ((x >= screenBounds.getMinX())
                    && (x < screenBounds.getMaxX())
                    && (y >= screenBounds.getMinY())
                    && (y < screenBounds.getMaxY())) {
                return screen;
            }
        }

        // the point is not inside any screen, find the closest screen now
        Screen selectedScreen = Screen.getPrimary();
        double minDistance = Double.MAX_VALUE;
        for (final Screen screen: screens) {
            final Rectangle2D screenBounds = screen.getBounds();
            final double dx = getOuterDistance(screenBounds.getMinX(),
                                               screenBounds.getMaxX(),
                                               x);
            final double dy = getOuterDistance(screenBounds.getMinY(),
                                               screenBounds.getMaxY(),
                                               y);
            final double distance = dx * dx + dy * dy;
            if (minDistance >= distance) {
                minDistance = distance;
                selectedScreen = screen;
            }
        }

        return selectedScreen;
    }

    private static double getIntersectionLength(
            final double a0, final double a1,
            final double b0, final double b1) {
        // (a0 <= a1) && (b0 <= b1)
        return (a0 <= b0) ? getIntersectionLengthImpl(b0, b1, a1)
                          : getIntersectionLengthImpl(a0, a1, b1);
    }

    private static double getIntersectionLengthImpl(
            final double v0, final double v1, final double v) {
        // (v0 <= v1)
        if (v <= v0) {
            return 0;
        }

        return (v <= v1) ? v - v0 : v1 - v0;
    }

    private static double getOuterDistance(
            final double a0, final double a1,
            final double b0, final double b1) {
        // (a0 <= a1) && (b0 <= b1)
        if (a1 <= b0) {
            return b0 - a1;
        }

        if (b1 <= a0) {
            return b1 - a0;
        }

        return 0;
    }

    private static double getOuterDistance(final double v0,
                                           final double v1,
                                           final double v) {
        // (v0 <= v1)
        if (v <= v0) {
            return v0 - v;
        }

        if (v >= v1) {
            return v - v1;
        }

        return 0;
    }

    /***************************************************************************
     *                                                                         *
     * Miscellaneous utilities                                                 *
     *                                                                         *
     **************************************************************************/

    public static boolean assertionEnabled() {
        boolean assertsEnabled = false;
        assert assertsEnabled = true;  // Intentional side-effect !!!

        return assertsEnabled;
    }

    /**
     * Returns true if the operating system is a form of Windows.
     */
    public static boolean isWindows(){
        return PlatformUtil.isWindows();
    }

    /**
     * Returns true if the operating system is a form of Mac OS.
     */
    public static boolean isMac(){
        return PlatformUtil.isMac();
    }

    /**
     * Returns true if the operating system is a form of Unix, including Linux.
     */
    public static boolean isUnix(){
        return PlatformUtil.isUnix();
    }

    /***************************************************************************
     *                                                                         *
     * Unicode-related utilities                                               *
     *                                                                         *
     **************************************************************************/

    public static String convertUnicode(String src) {
        /** The input buffer, index of next character to be read,
         *  index of one past last character in buffer.
         */
        char[] buf;
        int bp;
        int buflen;

        /** The current character.
         */
        char ch;

        /** The buffer index of the last converted unicode character
         */
        int unicodeConversionBp = -1;
        
        buf = src.toCharArray();
        buflen = buf.length;
        bp = -1;

        char[] dst = new char[buflen];
        int dstIndex = 0;

        while (bp < buflen - 1) {
            ch = buf[++bp];
            if (ch == '\\') {
                if (unicodeConversionBp != bp) {
                    bp++; ch = buf[bp];
                    if (ch == 'u') {
                        do {
                            bp++; ch = buf[bp];
                        } while (ch == 'u');
                        int limit = bp + 3;
                        if (limit < buflen) {
                            char c = ch;
                            int result = Character.digit(c, 16);
                            if (result >= 0 && c > 0x7f) {
                                //lexError(pos+1, "illegal.nonascii.digit");
                                ch = "0123456789abcdef".charAt(result);
                            }
                            int d = result;
                            int code = d;
                            while (bp < limit && d >= 0) {
                                bp++; ch = buf[bp];
                                char c1 = ch;
                                int result1 = Character.digit(c1, 16);
                                if (result1 >= 0 && c1 > 0x7f) {
                                    //lexError(pos+1, "illegal.nonascii.digit");
                                    ch = "0123456789abcdef".charAt(result1);
                                }
                                d = result1;
                                code = (code << 4) + d;
                            }
                            if (d >= 0) {
                                ch = (char)code;
                                unicodeConversionBp = bp;
                            }
                        }
                        //lexError(bp, "illegal.unicode.esc");
                    } else {
                        bp--;
                        ch = '\\';
                    }
                }
            }
            dst[dstIndex++] = ch;
        }
        
        return new String(dst, 0, dstIndex);
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

