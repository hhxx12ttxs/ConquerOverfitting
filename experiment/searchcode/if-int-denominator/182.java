/*
<<<<<<< HEAD
 *   
 *
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package java.util;

/**
 * <code>Calendar</code> is an abstract class for getting and setting dates
 * using a set of integer fields such as
 * <code>YEAR</code>, <code>MONTH</code>, <code>DAY</code>,
 * and so on. (A <code>Date</code> object represents a specific instant in
 * time with millisecond precision. See
 * {@link Date}
 * for information about the <code>Date</code> class.)
 *
 * <p>
 * Subclasses of <code>Calendar</code> interpret a <code>Date</code>
 * according to the rules of a specific calendar system.
 *
 * <p>
 * Like other locale-sensitive classes, <code>Calendar</code> provides a
 * class method, <code>getInstance</code>, for getting a generally useful
 * object of this type.
 * <blockquote>
 * <pre>
 * Calendar rightNow = Calendar.getInstance();
 * </pre>
 * </blockquote>
 *
 * <p>
 * A <code>Calendar</code> object can produce all the time field values
 * needed to implement the date-time formatting for a particular language
 * and calendar style (for example, Japanese-Gregorian, Japanese-Traditional).
 *
 * <p>
 * When computing a <code>Date</code> from time fields,
 * there may be insufficient information to compute the
 * <code>Date</code> (such as only year and month but no day in the month).
 *
 * <p>
 * <strong>Insufficient information.</strong> The calendar will use default
 * information to specify the missing fields. This may vary by calendar; for
 * the Gregorian calendar, the default for a field is the same as that of the
 * start of the epoch: i.e., YEAR = 1970, MONTH = JANUARY, DATE = 1, etc.
 *
 * <p>
 * <strong>Inconsistent information.</strong> In the J2SE calendar, it is
 * possible to set fields inconsistently. However, in this subset, the
 * DAY_OF_WEEK field cannot be set, and only a subset of the other J2SE
 * Calendar fields are included. So it is not possible to set
 * inconsistent data.
 * <p>
 *
 * <strong>Note:</strong> The ambiguity in interpretation of what day midnight
 * belongs to, is resolved as so: midnight "belongs" to the following day.<br>
 * 23:59 on Dec 31, 1969 &lt; 00:00 on Jan 1, 1970.<br>
 * 12:00 PM is midday, and 12:00 AM is midnight.<br>
 * 11:59 PM on Jan 1 &lt; 12:00 AM on Jan 2 &lt; 12:01 AM on Jan 2.<br>
 * 11:59 AM on Mar 10 &lt; 12:00 PM on Mar 10 &lt; 12:01 PM on Mar 10.<br>
 * 24:00 or greater are invalid.
 * Hours greater than 12 are invalid in AM/PM mode.
 * Setting the time will never change the date.
 * <p>
 * If equivalent times are entered in AM/PM or 24 hour mode, equality will be
 * determined by the actual time rather than the entered time.
 * <p>
 *
 * This class is a subset for J2ME of the J2SE Calendar class.
 * Many methods and variables have been
 * pruned, and other methods simplified, in an effort to reduce the size
 * of this class.
 *
 *
 * @see          TimeZone
 * @version      1.0 (J2ME MIDP)
 */
public abstract class Calendar {
  /**
   * Field number for <code>get</code> and <code>set</code> indicating the
   * year. This is a calendar-specific value.
   */
  public final static int YEAR = 1;
  /**
   * Field number for <code>get</code> and <code>set</code> indicating the
   * month. This is a calendar-specific value.
   */
  public final static int MONTH = 2;
  /**
   * Field number for <code>get</code> and <code>set</code> indicating the
   * day of the month. This is a synonym for <code>DAY_OF_MONTH</code>.
   * @see #DAY_OF_MONTH
   */
  public final static int DATE = 5;
  /**
   * Field number for <code>get</code> and <code>set</code> indicating the
   * day of the month. This is a synonym for <code>DATE</code>.
   * @see #DATE
   */
  public final static int DAY_OF_MONTH = 5;
  /**
   * Field number for <code>get</code> and <code>set</code> indicating the
   * day of the week.
   */
  public final static int DAY_OF_WEEK = 7;
  /**
   * Field number for <code>get</code> and <code>set</code> indicating
   * whether the <code>HOUR</code> is before or after noon.
   * E.g., at 10:04:15.250 PM the <code>AM_PM</code> is <code>PM</code>.
   * @see #AM
   * @see #PM
   * @see #HOUR
   */
  public final static int AM_PM = 9;
  /**
   * Field number for <code>get</code> and <code>set</code> indicating the
   * hour of the morning or afternoon. <code>HOUR</code> is used for the
   * 12-hour clock.
   * E.g., at 10:04:15.250 PM the <code>HOUR</code> is 10.
   * @see #AM_PM
   * @see #HOUR_OF_DAY
   */
  public final static int HOUR = 10;
  /**
   * Field number for <code>get</code> and <code>set</code> indicating the
   * hour of the day. <code>HOUR_OF_DAY</code> is used for the 24-hour clock.
   * E.g., at 10:04:15.250 PM the <code>HOUR_OF_DAY</code> is 22.
   */
  public final static int HOUR_OF_DAY = 11;
  /**
   * Field number for <code>get</code> and <code>set</code> indicating the
   * minute within the hour.
   * E.g., at 10:04:15.250 PM the <code>MINUTE</code> is 4.
   */
  public final static int MINUTE = 12;
  /**
   * Field number for <code>get</code> and <code>set</code> indicating the
   * second within the minute.
   * E.g., at 10:04:15.250 PM the <code>SECOND</code> is 15.
   */
  public final static int SECOND = 13;
  /**
   * Field number for <code>get</code> and <code>set</code> indicating the
   * millisecond within the second.
   * E.g., at 10:04:15.250 PM the <code>MILLISECOND</code> is 250.
   */
  public final static int MILLISECOND = 14;

  /**
   * Value of the <code>DAY_OF_WEEK</code> field indicating
   * Sunday.
   */
  public final static int SUNDAY = 1;
  /**
   * Value of the <code>DAY_OF_WEEK</code> field indicating
   * Monday.
   */
  public final static int MONDAY = 2;
  /**
   * Value of the <code>DAY_OF_WEEK</code> field indicating
   * Tuesday.
   */
  public final static int TUESDAY = 3;
  /**
   * Value of the <code>DAY_OF_WEEK</code> field indicating
   * Wednesday.
   */
  public final static int WEDNESDAY = 4;
  /**
   * Value of the <code>DAY_OF_WEEK</code> field indicating
   * Thursday.
   */
  public final static int THURSDAY = 5;
  /**
   * Value of the <code>DAY_OF_WEEK</code> field indicating
   * Friday.
   */
  public final static int FRIDAY = 6;
  /**
   * Value of the <code>DAY_OF_WEEK</code> field indicating
   * Saturday.
   */
  public final static int SATURDAY = 7;

  /**
   * Value of the <code>MONTH</code> field indicating the
   * first month of the year.
   */
  public final static int JANUARY = 0;
  /**
   * Value of the <code>MONTH</code> field indicating the
   * second month of the year.
   */
  public final static int FEBRUARY = 1;
  /**
   * Value of the <code>MONTH</code> field indicating the
   * third month of the year.
   */
  public final static int MARCH = 2;
  /**
   * Value of the <code>MONTH</code> field indicating the
   * fourth month of the year.
   */
  public final static int APRIL = 3;
  /**
   * Value of the <code>MONTH</code> field indicating the
   * fifth month of the year.
   */
  public final static int MAY = 4;
  /**
   * Value of the <code>MONTH</code> field indicating the
   * sixth month of the year.
   */
  public final static int JUNE = 5;
  /**
   * Value of the <code>MONTH</code> field indicating the
   * seventh month of the year.
   */
  public final static int JULY = 6;
  /**
   * Value of the <code>MONTH</code> field indicating the
   * eighth month of the year.
   */
  public final static int AUGUST = 7;
  /**
   * Value of the <code>MONTH</code> field indicating the
   * ninth month of the year.
   */
  public final static int SEPTEMBER = 8;
  /**
   * Value of the <code>MONTH</code> field indicating the
   * tenth month of the year.
   */
  public final static int OCTOBER = 9;
  /**
   * Value of the <code>MONTH</code> field indicating the
   * eleventh month of the year.
   */
  public final static int NOVEMBER = 10;
  /**
   * Value of the <code>MONTH</code> field indicating the
   * twelfth month of the year.
   */
  public final static int DECEMBER = 11;
  /**
   * Value of the <code>AM_PM</code> field indicating the
   * period of the day from midnight to just before noon.
   */
  public final static int AM = 0;
  /**
   * Value of the <code>AM_PM</code> field indicating the
   * period of the day from noon to just before midnight.
   */
  public final static int PM = 1;

  // Internal notes:
  // Calendar contains two kinds of time representations: current "time" in
  // milliseconds, and a set of time "fields" representing the current time.
  // The two representations are usually in sync, but can get out of sync
  // as follows.
  // 1. Initially, no fields are set, and the time is invalid.
  // 2. If the time is set, all fields are computed and in sync.
  // 3. If a single field is set, the time is invalid.
  // Recomputation of the time and fields happens when the object needs
  // to return a result to the user, or use a result for a computation.

  private int packed_time = 0;
  private int packed_date = 0;
  private int day_field = 0;
  private int dstOffset = 0;
  private boolean dstSet = false;

  /**
   * The currently set time for this calendar, expressed in milliseconds after
   * January 1, 1970, 0:00:00 GMT.
   */
  private long          time;

  /**
   * True if then the value of <code>time</code> is valid.
   * The time is made invalid by a change to an item of <code>field[]</code>.
   * @see #time
   */
  private boolean       millisSet; // NOTE: Make transient when possible

  /**
   * The <code>TimeZone</code> used by this calendar. </code>Calendar</code>
   * uses the time zone data to translate between locale and GMT time.
   */
  private TimeZone        zone;

  //////////////////
  // Class Variables
  //////////////////

  private static final int JAN_1_1_JULIAN_DAY = 1721426; // January 1, year 1 (Gregorian)
  private static final int EPOCH_JULIAN_DAY   = 2440588; // Jaunary 1, 1970 (Gregorian)
  private static final int EPOCH_YEAR = 1970;

  private static final int NUM_DAYS[]
    = {0,31,59,90,120,151,181,212,243,273,304,334}; // 0-based, for day-in-year
  private static final int LEAP_NUM_DAYS[]
    = {0,31,60,91,121,152,182,213,244,274,305,335}; // 0-based, for day-in-year

  // Useful millisecond constants.  Although ONE_DAY and ONE_WEEK can fit
  // into ints, they must be longs in order to prevent arithmetic overflow
  // when performing (CR 4173516).
  private static final int  ONE_SECOND = 1000;
  private static final int  ONE_MINUTE = 60*ONE_SECOND;
  private static final int  ONE_HOUR   = 60*ONE_MINUTE;
  private static final long ONE_DAY    = 24*ONE_HOUR;
  private static final long ONE_WEEK   = 7*ONE_DAY;

  /////////////////////
  // Instance Variables
  /////////////////////

  /**
   * The point at which the Gregorian calendar rules are used, measured in
   * milliseconds from the standard epoch.  Default is October 15, 1582
   * (Gregorian) 00:00:00 UTC or -12219292800000L.  For this value, October 4,
   * 1582 (Julian) is followed by October 15, 1582 (Gregorian).  This
   * corresponds to Julian day number 2299161.
   */
  private static final long gregorianCutover = -12219292800000L;

  /**
   * The year of the gregorianCutover, with 0 representing
   * 1 BC, -1 representing 2 BC, etc.
   */
  private static final int gregorianCutoverYear = 1582;

  private Date date = null;

  /** if both of these are set, the set() method will recalculate
   * the HOUR_OF_DAY using 12hr time.
   */
  private int hour_12hr = -1;
  private int am_pm_12hr = -1;

  /**
   * The platform name
   */
  private static String platform = null;

  /**
   * The root of the classes
   */
  private static String classRoot = null;

  /**
   * Constructs a Calendar with the default time zone
   * and default locale.
   *
   * @see     TimeZone#getDefault
   */
  protected Calendar() {
    zone = TimeZone.getDefault();
    if (zone == null) {
        throw new RuntimeException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                   "Could not find default timezone"
/* #endif */
        );
    }
    setTimeInMillis(0);
  }

  /**
   * Gets this Calendar's current time.
   *
   * @return the current time.
   *
   * @see #setTime
   */
  public final Date getTime() {
    if (date == null)
      return date = new Date( getTimeInMillis() );
    else {
      synchronized (date) {
        date.setTime( getTimeInMillis() );
        return date;
      }
    }
  }

  /**
   * Sets this Calendar's current time with the given Date.
   * <p>
   * Note: Calling <code>setTime()</code> with
   * <code>Date(Long.MAX_VALUE)</code> or <code>Date(Long.MIN_VALUE)</code>
   * may yield incorrect field values from <code>get()</code>.
   *
   * @param date the given Date.
   *
   * @see #getTime
   */
  public final void setTime(Date date) {
    setTimeInMillis( date.getTime() );
  }

  /**
   * Gets a calendar using the default time zone and default locale.
   *
   * @return a Calendar.
   */

  /* <p>
   * The following is information for implementers. Applications should
   * not need to be aware of this or rely on it, because each 
   * implementation may do it differently:
   * <p>
   * The Calendar will look up a class the name of which includes 
   * the platform name. The class name will take the form: <p>
   *
   * <code>{classRoot}.util.{platform}.CalendarImpl</code>
   *
   * <p>
   * The classRoot is derived from the system by looking up the system
   * property "microedition.implpath".  If this property key is not
   * found or the associated class is not present then "com.sun.cldc"
   * is used.
   */
  public static synchronized Calendar getInstance() {
    if (platform == null) {
      /* Setup the platform name */
      platform = "j2me";

      /* See if there is an alternate protocol class root */
      classRoot = System.getProperty("microedition.implpath");
      if (classRoot == null) {
        classRoot = "com.sun.cldc";
      }
    }

    try {
      /* Using the platform and protocol names lookup a class to implement the connection */
      Class clazz = Class.forName(classRoot+".util."+platform+".CalendarImpl");

      /* Construct a new instance */
      return (Calendar)clazz.newInstance();
    }
    catch (Exception x) {}

    return null;
  }

  /**
   * Gets a calendar using the specified time zone and default locale.
   * @param zone the time zone to use
   * @return a Calendar.
   */
  public static synchronized Calendar getInstance(TimeZone zone) {
    Calendar cal = getInstance();
    cal.setTimeZone(zone);
    return cal;
  }

  /**
   * Gets this Calendar's current time as a long expressed in milliseconds
   * after January 1, 1970, 0:00:00 GMT (the epoch).
   *
   * @return the current time as UTC milliseconds from the epoch.
   *
   * @see #setTimeInMillis
   */
  protected long getTimeInMillis() {
    if (!millisSet) {
      calculateTime();
      millisSet = true;
    }
    return time;
  }

  /**
   * Sets this Calendar's current time from the given long value.
   * @param millis the new time in UTC milliseconds from the epoch.
   *
   * @see #getTimeInMillis
   */
  protected void setTimeInMillis( long millis ) {
    millisSet = true;
    day_field = 0;
    time = millis;
    calculateFields();
  }

  /**
   * Gets the value for a given time field.
   * @param field the given time field (either YEAR, MONTH, DATE, DAY_OF_WEEK,
   *                                    HOUR_OF_DAY, HOUR, AM_PM, MINUTE, 
   *                                    SECOND, or MILLISECOND
   * @return the value for the given time field.
   * @exception ArrayIndexOutOfBoundsException if the parameter is not
   * one of the above.
   */
  public final int get(int field) {
    switch (field) {
    case YEAR:          return packed_date >> 9;
    case MONTH:         return (packed_date >> 5) & 15;
    case DATE:          return packed_date & 31;
    case DAY_OF_WEEK: {
      if (day_field == 0) {
        getTimeInMillis();
        calculateFields();
      }
      return day_field;
    }
    case HOUR_OF_DAY:   return packed_time >> 22;
    case HOUR: {
      int hr = (packed_time >> 22) % 12;
      return hr == 0? 12 : hr;
    }
    case AM_PM:         return (packed_time >> 22) < 12? AM: PM;
    case MINUTE:        return (packed_time >> 16) & 63;
    case SECOND:        return (packed_time >> 10) & 63;
    case MILLISECOND:   return packed_time & 1023;
    }
    throw new ArrayIndexOutOfBoundsException();
  }

  /**
   * Sets the time field with the given value.
   *
   * @param field the given time field.
   * Note that the DAY_OF_WEEK field cannot be set.
   * @param value the value to be set for the given time field.
   *
   * @exception ArrayIndexOutOfBoundsException if an illegal field
   * parameter is received.
   */
  public final void set(int field, int value) {
    millisSet = false;
    day_field = 0;
    switch (field) {
    case YEAR: // 16383 = (2 to the power 14) - 1
      packed_date = (packed_date & 511) | (value << 9);
      break;
    case MONTH:
      packed_date = (packed_date & (~(15<<5))) | ((value & 15) << 5);
      break;
    case DATE:
      packed_date = (packed_date & (~31)) | (value & 31);
      break;
    case HOUR_OF_DAY:
      value = value % 24;
      packed_time = (packed_time & 4194303) | (value << 22);
      hour_12hr = am_pm_12hr = -1;
      break;
    case HOUR: {
      if (value > 12)
        value = 12;
      if (am_pm_12hr != -1) {
        if (am_pm_12hr == PM) {
          if (value != 12)
            value += 12;
        }
        else if (value == 12)
          value = 0;
        hour_12hr = am_pm_12hr = -1;
      }
      else
        hour_12hr = value;
      packed_time = (packed_time & 4194303) | (value << 22);
      break;
    }
    case AM_PM: {
      if (hour_12hr != -1) {
        am_pm_12hr = -1;
      } else {
        am_pm_12hr = value;
        hour_12hr = (packed_time >> 22) % 12;
      }

      if (value == PM) {
        if (hour_12hr != 12) {
          hour_12hr += 12;
        }
      } else if (hour_12hr == 12) {
          hour_12hr = 0;
      }
      packed_time = (packed_time & 4194303) | (hour_12hr << 22);
      hour_12hr = -1;
      break;
    }
    case MINUTE:
      packed_time = (packed_time & (~(63<<16))) | ((value & 63) << 16);
      break;
    case SECOND:
      packed_time = (packed_time & (~(63<<10))) | ((value & 63) << 10);
      break;
    case MILLISECOND:
      packed_time = (packed_time & (~1023)) | (value & 1023);
      break;
    default: throw new ArrayIndexOutOfBoundsException();
    }
    dstSet = false;
  }

  /**
   * Compares this calendar to the specified object.
   * The result is <code>true</code> if and only if the argument is
   * not <code>null</code> and is a <code>Calendar</code> object that
   * represents the same calendar as this object.
   * @param obj the object to compare with.
   * @return <code>true</code> if the objects are the same;
   * <code>false</code> otherwise.
   */
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof Calendar))
      return false;

    Calendar that = (Calendar)obj;

    return getTimeInMillis() == that.getTimeInMillis() && zone.equals(that.zone);
  }

  /**
   * Compares the time field records.
   * Equivalent to comparing result of conversion to UTC.
   * @param when the Calendar to be compared with this Calendar.
   * @return true if the current time of this Calendar is before
   * the time of Calendar when; false otherwise.
   */
  public boolean before(Object when) {
    return (when instanceof Calendar
            && getTimeInMillis() < ((Calendar)when).getTimeInMillis());
  }

  /**
   * Compares the time field records.
   * Equivalent to comparing result of conversion to UTC.
   * @param when the Calendar to be compared with this Calendar.
   * @return true if the current time of this Calendar is after
   * the time of Calendar when; false otherwise.
   */
  public boolean after(Object when) {
    return (when instanceof Calendar
            && getTimeInMillis() > ((Calendar)when).getTimeInMillis());
  }

  /**
   * Sets the time zone with the given time zone value.
   * @param value the given time zone.
   *
   * @see #getTimeZone
   */
  public void setTimeZone(TimeZone value) {
    zone = value;
    getTimeInMillis();
    calculateFields();
  }

  /**
   * Gets the time zone.
   * @return the time zone object associated with this calendar.
   *
   * @see #setTimeZone
   */
  public TimeZone getTimeZone() {
    return zone;
  }

  /////////////////////////////
  // Time => Fields computation
  /////////////////////////////

  private void calculateDstOffset() {
    if (day_field == 0) {
      getTimeInMillis();
      calculateFields();
    }
    int rawOffset = zone.getRawOffset();
    long localMillis = time + rawOffset;
    long days = (long) (localMillis / ONE_DAY);
    int millisInDay = (int) (localMillis - (days * ONE_DAY));
    if (millisInDay < 0)
      millisInDay += ONE_DAY;
    dstOffset = zone.getOffset(1,
                               packed_date >> 9,
                               (packed_date >> 5) & 15,
                               packed_date & 31,
                               day_field,
                               millisInDay) - rawOffset;
    dstSet = true;
  }

  /**
   * Converts UTC as milliseconds to time field values.
   */
  private void calculateFields() {
    int rawOffset = zone.getRawOffset();
    long localMillis = time + rawOffset;

    /* Check for very extreme values -- millis near Long.MIN_VALUE or
     * Long.MAX_VALUE.  For these values, adding the zone offset can push
     * the millis past MAX_VALUE to MIN_VALUE, or vice versa.  This produces
     * the undesirable effect that the time can wrap around at the ends,
     * yielding, for example, a Date(Long.MAX_VALUE) with a big BC year
     * (should be AD).  Handle this by pinning such values to Long.MIN_VALUE
     * or Long.MAX_VALUE. - liu 8/11/98 CR 4149677 */
    if (time > 0 && localMillis < 0 && rawOffset > 0) {
      localMillis = Long.MAX_VALUE;
    } else if (time < 0 && localMillis > 0 && rawOffset < 0) {
      localMillis = Long.MIN_VALUE;
    }

    // Time to fields takes the wall millis (Standard or DST).
    timeToFields(localMillis);

    long days = (long) (localMillis / ONE_DAY);
    int millisInDay = (int) (localMillis - (days * ONE_DAY));
    if (millisInDay < 0) millisInDay += ONE_DAY;

    // Call getOffset() to get the TimeZone offset.  The millisInDay value must
    // be standard local millis.
    dstOffset = zone.getOffset(1,
                               packed_date >> 9,
                               (packed_date >> 5) & 15,
                               packed_date & 31,
                               day_field,
                               millisInDay) - rawOffset;
    dstSet = true;

    // Adjust our millisInDay for DST, if necessary.
    millisInDay += dstOffset;

    // If DST has pushed us into the next day, we must call timeToFields() again.
    // This happens in DST between 12:00 am and 1:00 am every day.  The call to
    // timeToFields() will give the wrong day, since the Standard time is in the
    // previous day.
    if (millisInDay >= ONE_DAY) {
      long dstMillis = localMillis + dstOffset;
      millisInDay -= ONE_DAY;
      // As above, check for and pin extreme values
      if (localMillis > 0 && dstMillis < 0 && dstOffset > 0) {
        dstMillis = Long.MAX_VALUE;
      } else if (localMillis < 0 && dstMillis > 0 && dstOffset < 0) {
        dstMillis = Long.MIN_VALUE;
      }
      timeToFields(dstMillis);
    }

    // Fill in all time-related fields based on millisInDay.
    // so as not to perturb flags.
    packed_time = (packed_time & (~1023)) | (millisInDay % 1000);
    millisInDay /= 1000;
    packed_time = (packed_time & (~(63<<10))) | ((millisInDay % 60) << 10);
    millisInDay /= 60;
    packed_time = (packed_time & (~(63<<16))) | ((millisInDay % 60) << 16);
    millisInDay /= 60;
    packed_time = (packed_time & (~(31<<22))) | ((millisInDay & 31) << 22);
  }

  /**
   * Convert the time as milliseconds to the date fields.  Millis must be
   * given as local wall millis to get the correct local day.  For example,
   * if it is 11:30 pm Standard, and DST is in effect, the correct DST millis
   * must be passed in to get the right date.
   * <p>
   * Fields that are completed by this method: YEAR, MONTH, DATE, DAY_OF_WEEK.
   * @param theTime the time in wall millis (either Standard or DST),
   * whichever is in effect
   * @param quick if true, only compute the YEAR, MONTH, DATE, and DAY_OF_WEEK.
   */
  private final void timeToFields(long theTime) {
    int dayOfYear, weekCount, year_field;
    boolean isLeap;

    // Compute the year, month, and day of month from the given millis
    if (theTime >= gregorianCutover) {
      // The Gregorian epoch day is zero for Monday January 1, year 1.
      long gregorianEpochDay = millisToJulianDay(theTime) - JAN_1_1_JULIAN_DAY;
      // Here we convert from the day number to the multiple radix
      // representation.  We use 400-year, 100-year, and 4-year cycles.
      // For example, the 4-year cycle has 4 years + 1 leap day; giving
      // 1461 == 365*4 + 1 days.
      int[] rem = new int[1];
      int n400 = floorDivide(gregorianEpochDay, 146097, rem); // 400-year cycle length
      int n100 = floorDivide(rem[0], 36524, rem); // 100-year cycle length
      int n4 = floorDivide(rem[0], 1461, rem); // 4-year cycle length
      int n1 = floorDivide(rem[0], 365, rem);
      year_field = 400*n400 + 100*n100 + 4*n4 + n1;
      dayOfYear = rem[0]; // zero-based day of year
      if (n100 == 4 || n1 == 4) dayOfYear = 365; // Dec 31 at end of 4- or 400-yr cycle
      else ++year_field;

      isLeap = ((year_field&0x3) == 0) && // equiv. to (year_field%4 == 0)
        (year_field%100 != 0 || year_field%400 == 0);

      // Gregorian day zero is a Monday
      day_field = (int)((gregorianEpochDay+1) % 7);
    }
    else {
      // The Julian epoch day (not the same as Julian Day)
      // is zero on Saturday December 30, 0 (Gregorian).
      long julianEpochDay = millisToJulianDay(theTime) - (JAN_1_1_JULIAN_DAY - 2);
      year_field = (int) floorDivide(4*julianEpochDay + 1464, 1461);

      // Compute the Julian calendar day number for January 1, year
      long january1 = 365*(year_field-1) + floorDivide(year_field-1, 4);
      dayOfYear = (int)(julianEpochDay - january1); // 0-based

      // Julian leap years occurred historically every 4 years starting
      // with 8 AD.  Before 8 AD the spacing is irregular; every 3 years
      // from 45 BC to 9 BC, and then none until 8 AD.  However, we don't
      // implement this historical detail; instead, we implement the
      // computationally cleaner proleptic calendar, which assumes
      // consistent 4-year cycles throughout time.
      isLeap = ((year_field&0x3) == 0); // equiv. to (year_field%4 == 0)

      // Julian calendar day zero is a Saturday
      day_field = (int)((julianEpochDay-1) % 7);
    }

    // Common Julian/Gregorian calculation
    int correction = 0;
    int march1 = isLeap ? 60 : 59; // zero-based DOY for March 1
    if (dayOfYear >= march1) correction = isLeap ? 1 : 2;
    int month_field = (12 * (dayOfYear + correction) + 6) / 367; // zero-based month
    int date_field = dayOfYear -
      (isLeap ? LEAP_NUM_DAYS[month_field] : NUM_DAYS[month_field]) + 1; // one-based DOM

    // Normalize day of week
    day_field += (day_field < 0) ? (SUNDAY+7) : SUNDAY;

    month_field += JANUARY; // 0-based

    packed_date =  year_field << 9;
    packed_date |= (month_field & 15) << 5;
    packed_date |= date_field & 31;
  }

  /////////////////////////////
  // Fields => Time computation
  /////////////////////////////

  /**
   * Converts time field values to UTC as milliseconds.
   * @exception IllegalArgumentException if any fields are invalid.
   */
  private void calculateTime() {

    // This function takes advantage of the fact that unset fields in
    // the time field list have a value of zero.

    // First, use the year to determine whether to use the Gregorian or the
    // Julian calendar. If the year is not the year of the cutover, this
    // computation will be correct. But if the year is the cutover year,
    // this may be incorrect. In that case, assume the Gregorian calendar,
    // make the computation, and then recompute if the resultant millis
    // indicate the wrong calendar has been assumed.

    // A date such as Oct. 10, 1582 does not exist in a Gregorian calendar
    // with the default changeover of Oct. 15, 1582, since in such a
    // calendar Oct. 4 (Julian) is followed by Oct. 15 (Gregorian).  This
    // algorithm will interpret such a date using the Julian calendar,
    // yielding Oct. 20, 1582 (Gregorian).
    int year_field = packed_date >> 9;
    boolean isGregorian = year_field >= gregorianCutoverYear;
    long julianDay = calculateJulianDay(isGregorian, year_field);
    long millis = julianDayToMillis(julianDay);

    // The following check handles portions of the cutover year BEFORE the
    // cutover itself happens. The check for the julianDate number is for a
    // rare case; it's a hardcoded number, but it's efficient.  The given
    // Julian day number corresponds to Dec 3, 292269055 BC, which
    // corresponds to millis near Long.MIN_VALUE.  The need for the check
    // arises because for extremely negative Julian day numbers, the millis
    // actually overflow to be positive values. Without the check, the
    // initial date is interpreted with the Gregorian calendar, even when
    // the cutover doesn't warrant it.
    if (isGregorian != (millis >= gregorianCutover) &&
        julianDay != -106749550580L) { // See above
      julianDay = calculateJulianDay(!isGregorian, year_field);
      millis = julianDayToMillis(julianDay);
    }

    // Do the time portion of the conversion.

    int millisInDay = 0;

    // Hours
    // Don't normalize here; let overflow bump into the next period.
    // This is consistent with how we handle other fields.
    millisInDay += (packed_time >> 22) & 31;

    millisInDay *= 60;
    millisInDay += (packed_time >> 16) & 63; // now have minutes
    millisInDay *= 60;
    millisInDay += (packed_time >> 10) & 63; // now have seconds
    millisInDay *= 1000;
    millisInDay += packed_time & 1023; // now have millis

    // Compute the time zone offset and DST offset.  There are two potential
    // ambiguities here.  We'll assume a 2:00 am (wall time) switchover time
    // for discussion purposes here.
    // 1. The transition into DST.  Here, a designated time of 2:00 am - 2:59 am
    //    can be in standard or in DST depending.  However, 2:00 am is an invalid
    //    representation (the representation jumps from 1:59:59 am Std to 3:00:00 am DST).
    //    We assume standard time.
    // 2. The transition out of DST.  Here, a designated time of 1:00 am - 1:59 am
    //    can be in standard or DST.  Both are valid representations (the rep
    //    jumps from 1:59:59 DST to 1:00:00 Std).
    //    Again, we assume standard time.
    // We use the TimeZone object to get the zone offset
    int zoneOffset = zone.getRawOffset();

    // Now add date and millisInDay together, to make millis contain local wall
    // millis, with no zone or DST adjustments
    millis += millisInDay;

    dstOffset = 0;
    /* Normalize the millisInDay to 0..ONE_DAY-1.  If the millis is out
     * of range, then we must call timeToFields() to recompute our
     * fields. */
    int[] normalizedMillisInDay = new int[1];
    floorDivide(millis, (int)ONE_DAY, normalizedMillisInDay);

    // We need to have the month, the day, and the day of the week.
    // Calling timeToFields will compute the MONTH and DATE fields.
    //
    // It's tempting to try to use DAY_OF_WEEK here, if it
    // is set, but we CAN'T.  Even if it's set, it might have
    // been set wrong by the user.  We should rely only on
    // the Julian day number, which has been computed correctly
    // using the disambiguation algorithm above. [LIU]
    int dow = julianDayToDayOfWeek(julianDay);

    // It's tempting to try to use DAY_OF_WEEK here, if it
    // is set, but we CAN'T.  Even if it's set, it might have
    // been set wrong by the user.  We should rely only on
    // the Julian day number, which has been computed correctly
    // using the disambiguation algorithm above. [LIU]
    dstOffset = zone.getOffset(1,
                               packed_date >> 9,
                               (packed_date >> 5) & 15,
                               packed_date & 31,
                               dow,
                               normalizedMillisInDay[0]) -
      zoneOffset;
    dstSet = true;
    // Note: Because we pass in wall millisInDay, rather than
    // standard millisInDay, we interpret "1:00 am" on the day
    // of cessation of DST as "1:00 am Std" (assuming the time
    // of cessation is 2:00 am).

    // Store our final computed GMT time, with timezone adjustments.
    time = millis - zoneOffset - dstOffset;
  }

  /**
   * Compute the Julian day number under either the Gregorian or the
   * Julian calendar, using the given year and the remaining fields.
   * @param isGregorian if true, use the Gregorian calendar
   * @param year the adjusted year number, with 0 indicating the
   * year 1 BC, -1 indicating 2 BC, etc.
   * @return the Julian day number
   */
  private final long calculateJulianDay(boolean isGregorian, int year) {
    int month = 0, y;
    long millis = 0;

    month = (packed_date >> 5) & 15 - JANUARY;

    // If the month is out of range, adjust it into range
    if (month < 0 || month > 11) {
      int[] rem = new int[1];
      year += floorDivide(month, 12, rem);
      month = rem[0];
    }

    boolean isLeap = year%4 == 0;
    y = year - 1;
    long julianDay = 365L*y + floorDivide(y, 4) + (JAN_1_1_JULIAN_DAY - 3);

    if (isGregorian) {
      isLeap = isLeap && ((year%100 != 0) || (year%400 == 0));
      // Add 2 because Gregorian calendar starts 2 days after Julian calendar
      julianDay += floorDivide(y, 400) - floorDivide(y, 100) + 2;
    }

    // At this point julianDay is the 0-based day BEFORE the first day of
    // January 1, year 1 of the given calendar.  If julianDay == 0, it
    // specifies (Jan. 1, 1) - 1, in whatever calendar we are using (Julian
    // or Gregorian).

    julianDay += isLeap ? LEAP_NUM_DAYS[month] : NUM_DAYS[month];
    julianDay += packed_date & 31;
    return julianDay;
  }

  /////////////////
  // Implementation
  /////////////////

  /**
   * Converts time as milliseconds to Julian day.
   * @param millis the given milliseconds.
   * @return the Julian day number.
   */
  private static final long millisToJulianDay(long millis) {
    return EPOCH_JULIAN_DAY + floorDivide(millis, ONE_DAY);
  }

  /**
   * Converts Julian day to time as milliseconds.
   * @param julian the given Julian day number.
   * @return time as milliseconds.
   */
  private static final long julianDayToMillis(long julian) {
    return (julian - EPOCH_JULIAN_DAY) * ONE_DAY;
  }

  private static final int julianDayToDayOfWeek(long julian) {
    // If julian is negative, then julian%7 will be negative, so we adjust
    // accordingly.  We add 1 because Julian day 0 is Monday.
    int dayOfWeek = (int)((julian + 1) % 7);
    return dayOfWeek + ((dayOfWeek < 0) ? (7 + SUNDAY) : SUNDAY);
  }

  /**
   * Divide two long integers, returning the floor of the quotient.
   * <p>
   * Unlike the built-in division, this is mathematically well-behaved.
   * E.g., <code>-1/4</code> => 0
   * but <code>floorDivide(-1,4)</code> => -1.
   * @param numerator the numerator
   * @param denominator a divisor which must be > 0
   * @return the floor of the quotient.
   */
  private static final long floorDivide(long numerator, long denominator) {
    // We do this computation in order to handle
    // a numerator of Long.MIN_VALUE correctly
    return (numerator >= 0) ?
      numerator / denominator :
      ((numerator + 1) / denominator) - 1;
  }

  /**
   * Divide two integers, returning the floor of the quotient.
   * <p>
   * Unlike the built-in division, this is mathematically well-behaved.
   * E.g., <code>-1/4</code> => 0
   * but <code>floorDivide(-1,4)</code> => -1.
   * @param numerator the numerator
   * @param denominator a divisor which must be > 0
   * @return the floor of the quotient.
   */
  private static final int floorDivide(int numerator, int denominator) {
    // We do this computation in order to handle
    // a numerator of Integer.MIN_VALUE correctly
    return (numerator >= 0) ?
      numerator / denominator :
      ((numerator + 1) / denominator) - 1;
  }

  /**
   * Divide two integers, returning the floor of the quotient, and
   * the modulus remainder.
   * <p>
   * Unlike the built-in division, this is mathematically well-behaved.
   * E.g., <code>-1/4</code> => 0 and <code>-1%4</code> => -1,
   * but <code>floorDivide(-1,4)</code> => -1 with <code>remainder[0]</code> => 3.
   * @param numerator the numerator
   * @param denominator a divisor which must be > 0
   * @param remainder an array of at least one element in which the value
   * <code>numerator mod denominator</code> is returned. Unlike <code>numerator
   * % denominator</code>, this will always be non-negative.
   * @return the floor of the quotient.
   */
  private static final int floorDivide(int numerator, int denominator, int[] remainder) {
    if (numerator >= 0) {
      remainder[0] = numerator % denominator;
      return numerator / denominator;
    }
    int quotient = ((numerator + 1) / denominator) - 1;
    remainder[0] = numerator - (quotient * denominator);
    return quotient;
  }

  /**
   * Divide two integers, returning the floor of the quotient, and
   * the modulus remainder.
   * <p>
   * Unlike the built-in division, this is mathematically well-behaved.
   * E.g., <code>-1/4</code> => 0 and <code>-1%4</code> => -1,
   * but <code>floorDivide(-1,4)</code> => -1 with <code>remainder[0]</code> => 3.
   * @param numerator the numerator
   * @param denominator a divisor which must be > 0
   * @param remainder an array of at least one element in which the value
   * <code>numerator mod denominator</code> is returned. Unlike <code>numerator
   * % denominator</code>, this will always be non-negative.
   * @return the floor of the quotient.
   */
  private static final int floorDivide(long numerator, int denominator, int[] remainder) {
    if (numerator >= 0) {
      remainder[0] = (int)(numerator % denominator);
      return (int)(numerator / denominator);
    }
    int quotient = (int)(((numerator + 1) / denominator) - 1);
    remainder[0] = (int)(numerator - (quotient * denominator));
    return quotient;
  }
=======
 * Copyright (c) 2009 Stephan Schloepke and innoQ Deutschland GmbH
 *
 * Stephan Schloepke: http://www.schloepke.de/
 * innoQ Deutschland GmbH: http://www.innoq.com/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jbasics.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

/**
 * An Immutable representation of ratio based on two {@link BigInteger} for numerator and
 * denominator.
 * <p>
 * {@link BigRational} is the extension to the {@link BigDecimal} and {@link BigInteger} classes found in Java. Since
 * Java does not have any support for real ratios this class is supposed to fill this gap.
 * </p>
 * 
 * @author Stephan Schloepke
 */
public final class BigRational extends Number implements Comparable<BigRational> {
	/**
	 * The serial version UID required as a derive from number
	 */
	private static final long serialVersionUID = -1773220741262593011L;
	/**
	 * Constant {@link BigRational} for the value of zero (0/1)
	 */
	public static final BigRational ZERO = new BigRational(BigInteger.valueOf(0L), BigInteger.valueOf(1L));
	/**
	 * Constant {@link BigRational} for the value of one (1/1)
	 */
	public static final BigRational ONE = new BigRational(BigInteger.valueOf(1L), BigInteger.valueOf(1L));

	private final BigInteger numerator;
	private final BigInteger denomintar;
	private transient BigInteger cachedGCD;

	/**
	 * Create a {@link BigRational} with the given numerator and denominator
	 * (numerator/denominator).
	 * <p>
	 * The create {@link BigRational} is not reduced but the sign is correct in the manner that if both numbers are
	 * negative that non becomes negative and if the denominator is negative than the numerator becomes negative and the
	 * denominator positive. The result is always a Ratio in the form <code>[-]numerator/denominator</code>.
	 * </p>
	 * <p>
	 * Since a division by zero is undefined the denominator must not be zero and neither parts can be null or an
	 * {@link IllegalArgumentException} is raised.
	 * </p>
	 * 
	 * @param numerator The numerator (must not be null)
	 * @param denominator The denominator (must not be null or zero).
	 */
	public BigRational(final BigInteger numerator, final BigInteger denominator) {
		if (numerator == null || denominator == null) {
			throw new IllegalArgumentException("Null parameter: numerator and denominator must not be null");
		}
		if (denominator.signum() == 0) {
			throw new ArithmeticException("Denominator cannot be zero => Division by zero");
		}
		if (denominator.signum() < 0) {
			this.numerator = numerator.negate();
			this.denomintar = denominator.negate();
		} else {
			this.numerator = numerator;
			this.denomintar = denominator;
		}
	}

	/**
	 * Create a {@link BigRational} with the given numerator and denominator.
	 * 
	 * @param numerator The numerator
	 * @param denominator The denominator
	 */
	public BigRational(final long numerator, final long denominator) {
		this(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
	}

	/**
	 * Returns the numerator part of this {@link BigRational}.
	 * 
	 * @return The numerator part.
	 */
	public BigInteger numerator() {
		return this.numerator;
	}

	/**
	 * Returns the denominator part of this {@link BigRational}.
	 * 
	 * @return The denominator part.
	 */
	public BigInteger denominator() {
		return this.denomintar;
	}

	/**
	 * Calculates the greatest common divider of the numerator and denominator.
	 * 
	 * @return The greatest common divider.
	 */
	public BigInteger gcd() {
		if (this.cachedGCD == null) {
			this.cachedGCD = this.numerator.gcd(this.denomintar);
		}
		return this.cachedGCD;
	}

	/**
	 * Calculates the least common multiple of the denominator and the denominator of the given {@link BigRational}.
	 * 
	 * @param x The {@link BigRational} to get the least common multiple of.
	 * @return The least common multiple of this {@link BigRational} and the given {@link BigRational}s denominator.
	 */
	public BigInteger lcm(final BigRational x) {
		if (x == null) {
			throw new IllegalArgumentException("Null parameter: x");
		}
		return this.denomintar.multiply(x.denomintar).abs().divide(this.denomintar.gcd(x.denomintar));
	}

	/**
	 * Returns a {@link BigRational} representation for the given long value.
	 * 
	 * @param value The long value
	 * @return The {@link BigRational} representation.
	 */
	public static BigRational valueOf(final long value) {
		return new BigRational(BigInteger.valueOf(value), BigInteger.ONE);
	}

	/**
	 * Returns a {@link BigRational} representation for the given double value.
	 * 
	 * @param value The double value
	 * @return The {@link BigRational} representation.
	 */
	public static BigRational valueOf(final double value) {
		return BigRational.valueOf(BigDecimal.valueOf(value));
	}

	/**
	 * Returns a {@link BigRational} representation for the given {@link BigDecimal} value.
	 * 
	 * @param value The {@link BigDecimal} value (must not be null).
	 * @return The {@link BigRational} representation.
	 * @throws IllegalArgumentException If the given value is null.
	 */
	public static BigRational valueOf(final BigDecimal value) {
		if (value == null) {
			throw new IllegalArgumentException("Null parameter: value");
		}
		if (BigDecimal.ZERO.compareTo(value) == 0) {
			return BigRational.ZERO;
		} else if (BigDecimal.ONE.compareTo(value) == 0) {
			return BigRational.ONE;
		}
		BigInteger nominator = value.unscaledValue();
		BigInteger denominator = BigInteger.TEN.pow(value.scale());
		BigInteger gcd = nominator.gcd(denominator);
		return new BigRational(nominator.divide(gcd), denominator.divide(gcd));
	}

	/**
	 * Parses the given String and returns a {@link BigRational} representation.
	 * <p>
	 * The value is parsed according to the pattern "<em><code>^-?[0-9]+(/-?[0-9]+)?$</code></em>". If String does not
	 * apply to the pattern an {@link IllegalArgumentException} is thrown.
	 * </p>
	 * 
	 * @param value The String to parse (must not be null or zero length)
	 * @return The {@link BigRational} representation.
	 * @throws IllegalArgumentException Thrown if the String does not comply to the pattern (see
	 *             above).
	 */
	public static BigRational valueOf(final String value) {
		if (value == null || value.length() == 0) {
			throw new IllegalArgumentException("Zero length (or null) BigRatio");
		}
		String[] parts = value.split("/");
		switch (parts.length) {
			case 1:
				if (parts[0].indexOf('.') >= 0) {
					return BigRational.valueOf(new BigDecimal(parts[0]));
				} else {
					return new BigRational(new BigInteger(parts[0]), BigInteger.ONE);
				}
			case 2:
				return new BigRational(new BigInteger(parts[0]), new BigInteger(parts[1]));
			default:
				throw new IllegalArgumentException("Illegal BigRational value " + value);
		}
	}

	/**
	 * Returns a {@link BigRational} representation for the given {@link Number} value.
	 * 
	 * @param value The {@link Number} value (if null BigRational.ZERO will be returned).
	 * @return The {@link BigRational} representation.
	 * @throws IllegalArgumentException If the given value is null.
	 */
	public static BigRational valueOf(final Number value) {
		if (value == null) {
			return BigRational.ZERO;
		} else if (value instanceof BigRational) {
			return (BigRational) value;
		} else if (value instanceof BigInteger) {
			return new BigRational((BigInteger) value, BigInteger.ONE);
		} else if (value instanceof BigDecimal) {
			return BigRational.valueOf((BigDecimal) value);
		} else if (value instanceof Integer || value instanceof Long || value instanceof Short || value instanceof Byte) {
			return new BigRational(value.longValue(), 1L);
		} else {
			return BigRational.valueOf(value.doubleValue());
		}
	}

	/**
	 * Reduces this {@link BigRational} to a more simple fraction.
	 * <p>
	 * Returns a new {@link BigRational} with the numerator and denominator divided by the greatest common divider. In
	 * case that the GCD is one the {@link BigRational} cannot be reduced and this is returned instead of a new
	 * {@link BigRational}.
	 * </p>
	 * 
	 * @return The {@link BigRational} with the numerator and denominator reduced by the greatest
	 *         common divider.
	 */
	public BigRational reduce() {
		BigInteger gcd = gcd();
		if (BigInteger.ONE.equals(gcd)) {
			return this;
		}
		return new BigRational(this.numerator.divide(gcd), this.denomintar.divide(gcd));
	}

	/**
	 * Extend this {@link BigRational} by a given factor.
	 * <p>
	 * The result is a {@link BigRational} with its numerator and denominator multiplied by the factor. If the given
	 * factor is one than this is returned (no extension when multiplying numerator and denominator with one). Howere
	 * the factor of zero is not allowed and yields an {@link ArithmeticException} since it would lead to a division by
	 * zero (x*0/y*0 = 0/0).
	 * <p>
	 * 
	 * @param factor The factor to multiply numerator and denominator with (must not be null or
	 *            zero).
	 * @return The extended form of this {@link BigRational} or this {@link BigRational} if the
	 *         factor is one.
	 * @throws IllegalArgumentException If the given factor is null.
	 * @throws ArithmeticException if the given factor is zero.
	 */
	public BigRational extend(final BigInteger factor) {
		if (factor == null) {
			throw new IllegalArgumentException("Null parameter: factor");
		}
		if (BigInteger.ZERO.equals(factor)) {
			throw new ArithmeticException("Extension with zero would lead to a division by zero");
		}
		if (BigInteger.ONE.equals(factor)) {
			return this;
		}
		return new BigRational(BigInteger.ONE.equals(this.numerator) ? factor : this.numerator.multiply(factor),
				BigInteger.ONE.equals(this.denomintar) ? factor : this.denomintar.multiply(factor));
	}

	/**
	 * Extend this {@link BigRational} by a given factor.
	 * <p>
	 * This method is a convenient wrapper for {@link BigRational#extend(BigInteger)} and calls it with
	 * {@link BigInteger#valueOf(long)}.
	 * <p>
	 * 
	 * @param factor The factor to multiply numerator and denominator with (must not be null or
	 *            zero).
	 * @return The extended form of this {@link BigRational} or this {@link BigRational} if the
	 *         factor is one.
	 * @throws ArithmeticException if the given factor is zero.
	 * @see BigRational#extend(BigInteger)
	 */
	public BigRational extend(final long factor) {
		return extend(BigInteger.valueOf(factor));
	}

	/**
	 * Extends this BigRational to a new BigRational where the denominator is equal to the given
	 * multiple.
	 * <p>
	 * In order to extend this {@link BigRational} so that the denominator is equal to the multiple it is required that
	 * the multiple really is a multiple of the denominator. In case that the given multiple is not a multiple of the
	 * denominator an {@link ArithmeticException} is raised. The multiple must be greater than one since
	 * {@link BigRational} guarantees that the denominator is never negative and a negative multiple would lead to a
	 * negative denominator and change the value rather than extending the representation (1/2 is 2/4 so still the same
	 * value if extended to a multiple of 4. However 1/2 to 2/-4 would be a different value and therefore not an
	 * extension. If such a case is required than multiply the with -2/2 or use negate().extendToMultiple(4) instead).
	 * </p>
	 * 
	 * @param multiple The multiple to extend this {@link BigRational} to.
	 * @return A BigRational with the denominator equal to the multiple.
	 * @throws IllegalArgumentException If the multiple is less than or equal to one.
	 * @throws ArithmeticException If the multiple is not a m multiple of the denominator.
	 */
	public BigRational extendToMultiple(final BigInteger multiple) {
		if (multiple == null) {
			throw new IllegalArgumentException("Null parameter: multiple");
		}
		if (BigInteger.ONE.compareTo(multiple) >= 0) {
			throw new IllegalArgumentException("multiple must be greater than one");
		}
		BigInteger[] temp = multiple.divideAndRemainder(this.denomintar);
		if (temp[1] != null && BigInteger.ZERO.compareTo(temp[1]) != 0) {
			throw new ArithmeticException("Not a multiple of the denominator " + multiple + "/" + this.denomintar
					+ " = " + temp[0] + " rest " + temp[1]);
		}
		return extend(temp[0]);
	}

	/**
	 * Returns the reciprocal value of this {@link BigRational} (x/y => y/x).
	 * 
	 * @return The reciprocal {@link BigRational} of this {@link BigRational}.
	 */
	public BigRational reciprocal() {
		return new BigRational(this.denomintar, this.numerator);
	}

	/**
	 * Returns the negation of this {@link BigRational} (x/y => -x/y).
	 * 
	 * @return The negated {@link BigRational}.
	 */
	public BigRational negate() {
		return new BigRational(this.numerator.negate(), this.denomintar);
	}

	/**
	 * Returns the absolute value of this BigRational ( |-x/y| => x/y ). If this {@link BigRational} is already positive
	 * this is returned.
	 * 
	 * @return The absolute {@link BigRational}.
	 */
	public BigRational abs() {
		if (this.numerator.signum() > 0) {
			return this;
		}
		return new BigRational(this.numerator.abs(), this.denomintar);
	}

	/**
	 * Adds a value to the numerator and returns a new BigRational with the value added.
	 * <p>
	 * This is a useful method especially for programming certain algorithms more effectively. Consider the constant E
	 * which can be represented by the Taylor series <code>E = 1/0! + 1/1! + 1/2! + 1/3! + 1/4! + ... + 1/n!</code>. Now
	 * the algorithm could do an extend and addNumerator in the following way:
	 * 
	 * <pre>
	 * BigRational[] results = new BigRational[100];
	 * results[0] = BigRational.ONE; // 1/0! is 1/1
	 * for (int i = 1; i &lt; results.length; i++) {
	 * 	results[i] = results[i - 1].extend(i).addNumerator(BigInteger.ONE);
	 * }
	 * </pre>
	 * 
	 * as you see it would be faster due to the fact that the denominator extends by the iteration step each time. So
	 * the current numerator must advance in the same way. Those the effect is that we have two multiplications and one
	 * addition. If we would always add 1/(n+1)! there is the multiplication to extend the faculty to the next step and
	 * two multiplications to bring the rational numbers to match the same denominator. The number would raise very
	 * quickly as long as you do not reduce the fraction each step. If you reduce the fraction each step an additional
	 * amount of calculating the GCD is added as overhead. The result still would be quite close to the algorithm
	 * outlined above. With other words the algorithm above is quite a bit faster (even not linear to n because the
	 * multiplication is quadric). However it only works if you do not reduce in each step so the number raises quite a
	 * bit. In the typical exp function this does not matter a lot though since the reduction dosn't save much.
	 * </p>
	 * 
	 * @param x The value to add to the numerator of this {@link BigRational}
	 * @return The new {@link BigRational} with its numerator advanced by x.
	 */
	public BigRational addNumerator(final BigInteger x) {
		if (x == null) {
			throw new IllegalArgumentException("Null parameter: x");
		}
		return new BigRational(this.numerator.add(x), this.denomintar);
	}

	public BigRational subtractNumerator(final BigInteger x) {
		if (x == null) {
			throw new IllegalArgumentException("Null parameter: x");
		}
		return new BigRational(this.numerator.subtract(x), this.denomintar);
	}

	public BigRational add(final BigRational x) {
		if (x == null) {
			throw new IllegalArgumentException("Null parameter: x");
		}
		if (this.denomintar.equals(x.denomintar)) {
			return addNumerator(x.numerator);
		}
		BigInteger lcm = lcm(x);
		BigInteger thisFactor = lcm.divide(this.denomintar);
		BigInteger thatFactor = lcm.divide(x.denomintar);
		return new BigRational(this.numerator.multiply(thisFactor).add(x.numerator.multiply(thatFactor)), lcm);
	}

	public BigRational add(final BigInteger x) {
		if (x == null) {
			throw new IllegalArgumentException("Null parameter: x");
		}
		return new BigRational(this.numerator.add(x.multiply(this.denomintar)), this.denomintar);
	}

	public BigRational add(final BigDecimal x) {
		return add(BigRational.valueOf(x));
	}

	public BigRational subtract(final BigRational x) {
		if (x == null) {
			throw new IllegalArgumentException("Null parameter: x");
		}
		if (this.denomintar.equals(x.denomintar)) {
			return subtractNumerator(x.numerator);
		}
		BigInteger lcm = lcm(x);
		BigInteger thisFactor = lcm.divide(this.denomintar);
		BigInteger thatFactor = lcm.divide(x.denomintar);
		return new BigRational(this.numerator.multiply(thisFactor).subtract(x.numerator.multiply(thatFactor)), lcm);
	}

	public BigRational subtract(final BigInteger x) {
		if (x == null) {
			throw new IllegalArgumentException("Null parameter: x");
		}
		return new BigRational(this.numerator.subtract(x.multiply(this.denomintar)), this.denomintar);
	}

	public BigRational subtract(final BigDecimal x) {
		return subtract(BigRational.valueOf(x));
	}

	public BigRational multiply(final BigRational x) {
		if (x == null) {
			throw new IllegalArgumentException("Null parameter: x");
		}
		return new BigRational(this.numerator.multiply(x.numerator), this.denomintar.multiply(x.denomintar));
	}

	public BigRational multiply(final BigInteger x) {
		if (x == null) {
			throw new IllegalArgumentException("Null parameter: x");
		}
		return new BigRational(this.numerator.multiply(x), this.denomintar);
	}

	public BigRational multiply(final BigDecimal x) {
		return multiply(BigRational.valueOf(x));
	}

	public BigRational divide(final BigRational x) {
		if (x == null) {
			throw new IllegalArgumentException("Null parameter: x");
		}
		return new BigRational(this.numerator.multiply(x.denomintar), this.denomintar.multiply(x.numerator));
	}

	public BigRational divide(final BigInteger x) {
		if (x == null) {
			throw new IllegalArgumentException("Null parameter: x");
		}
		return new BigRational(this.numerator, this.denomintar.multiply(x));
	}

	public BigRational divide(final BigDecimal x) {
		return divide(BigRational.valueOf(x));
	}

	public BigRational pow(final int x) {
		if (x == 0 || this.numerator.signum() == 0) {
			return BigRational.ONE;
		}
		return new BigRational(this.numerator.pow(x), this.denomintar.pow(x));
	}

	// --- Convert to other Big types

	public BigDecimal decimalValue() {
		return decimalValue(MathContext.UNLIMITED);
	}

	public BigDecimal decimalValue(final MathContext mc) {
		if (mc == null) {
			throw new IllegalArgumentException("Null parameter: mc");
		}
		return new BigDecimal(this.numerator).divide(new BigDecimal(this.denomintar), mc);
	}

	public BigInteger toBigInteger() {
		return this.numerator.divide(this.denomintar);
	}

	/**
	 * Returns an exact {@link BigInteger} value of this {@link BigRational} or throws an {@link ArithmeticException} if
	 * the integer division has a remainder.
	 * 
	 * @return The exact BigInteger value of this BigRatio.
	 * @throws ArithmeticException If the division has a remainder.
	 */
	public BigInteger toBigIntegerExact() {
		if (remainder().signum() != 0) {
			throw new ArithmeticException("Ratio has a remainder");
		}
		return toBigInteger();
	}

	/**
	 * Returns the remainder of the division if the division would be executed.
	 * 
	 * @return The remainder of the division from numerator and denominator (numerator mod
	 *         denominator).
	 */
	public BigRational remainder() {
		return new BigRational(this.numerator.remainder(this.denomintar), this.denomintar);
	}

	public int signum() {
		return this.numerator.signum();
	}

	// --- Number implementation

	@Override
	public double doubleValue() {
		return new BigDecimal(this.numerator).divide(new BigDecimal(this.denomintar), MathContext.DECIMAL64)
				.doubleValue();
	}

	@Override
	public float floatValue() {
		return new BigDecimal(this.numerator).divide(new BigDecimal(this.denomintar), MathContext.DECIMAL32)
				.floatValue();
	}

	@Override
	public int intValue() {
		return this.numerator.divide(this.denomintar).intValue();
	}

	@Override
	public long longValue() {
		return this.numerator.divide(this.denomintar).longValue();
	}

	// --- Standard java toString, hashCode and equals.

	@Override
	public String toString() {
		StringBuilder temp = new StringBuilder().append(this.numerator);
		if (!BigInteger.ONE.equals(this.denomintar)) {
			temp.append("/").append(this.denomintar); //$NON-NLS-1$
		}
		return temp.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.numerator.hashCode();
		result = prime * result + this.denomintar.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof BigRational)) {
			return false;
		}
		BigRational other = (BigRational) obj;
		if (!this.denomintar.equals(other.denomintar)) {
			return false;
		} else if (!this.numerator.equals(other.numerator)) {
			return false;
		}
		return true;
	}

	// --- Comparable interface

	public int compareTo(final BigRational that) {
		if (that == null) {
			throw new IllegalArgumentException("Null parameter: that");
		}
		BigInteger lcm = this.denomintar.multiply(that.denomintar).abs().divide(this.denomintar.gcd(that.denomintar));
		return this.numerator.multiply(lcm.divide(this.denomintar)).compareTo(
				that.numerator.multiply(lcm.divide(that.denomintar)));
	}

>>>>>>> 76aa07461566a5976980e6696204781271955163
}

