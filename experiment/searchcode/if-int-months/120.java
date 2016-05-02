/**
 * Copyright (C) 2009-2010 eHealth Systems Ltd. All Rights Reserved.
 *
 * Reproduction or distribution of this source code is prohibited.
 */
package org.openmrs.module.visitscheduler.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import java.util.Locale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;

/**
 * A utility class for common date operations
 */
public class DateUtil {
		
	protected static Log log = LogFactory.getLog(DateUtil.class);
	
	// Common periods of time
	final static int DAILY = 0;
	final static int WEEKLY = 1;
	final static int MONTHLY = 2;
	final static int QUARTERLY = 3;
	final static int ANNUALLY = 4;
	
	
	// Added for readability (see below)
	final static int MILLISECOND = 1000;
	final static int SECOND = 1;
	final static int MINUTE = 60 * SECOND;
	final static int HOUR = 60 * MINUTE;
	final static int DAY = 24 * HOUR;
	final static int MONTH = 30 * DAY;

	/**
	 * Returns the passed date, at the specified time
	 */
	public static Date getDateTime(int year, int mon, int day, int hr, int min,
			int sec, int ms) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, mon);
		c.set(Calendar.DATE, day);
		c.set(Calendar.HOUR_OF_DAY, hr);
		c.set(Calendar.MINUTE, min);
		c.set(Calendar.SECOND, sec);
		c.set(Calendar.MILLISECOND, ms);
		return c.getTime();
	}

	/**
	 * Returns the passed date, at the specified time
	 */
	public static Date getDateTime(Date d, int hour, int minute, int second,
			int millisecond) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, second);
		c.set(Calendar.MILLISECOND, millisecond);
		return c.getTime();
	}

	/**
	 * Returns the last second of the day
	 */
	public static Date getEndOfDay(Date d) {
		return getDateTime(d, 23, 59, 59, 999);
	}

	/**
	 * Returns a date that represents the very beginning of the passed date
	 */
	public static Date getStartOfDay(Date d) {
		return getDateTime(d, 0, 0, 0, 0);
	}
	
	/**
	 * Returns a date that is the very beginning of the first of the month,
	 * given the passed date and adjustment
	 */
	public static Date getStartOfMonth(Date d, int monthAdjustment) {
		Calendar c = Calendar.getInstance();
		c.setTime(getStartOfDay(d));
		c.set(Calendar.DATE, 1);
		c.add(Calendar.MONTH, monthAdjustment);
		return c.getTime();
	}

	/**
	 * Returns a date that is the very end of the last of the month, given the
	 * passed date and adjustment
	 */
	public static Date getEndOfMonth(Date d, int monthAdjustment) {
		Calendar c = Calendar.getInstance();
		c.setTime(getEndOfDay(d));
		c.set(Calendar.DATE, c.getActualMaximum(Calendar.DATE));
		c.add(Calendar.MONTH, monthAdjustment);
		return c.getTime();
	}

	
	/**
	 * Get a string that represents the time span that has elapsed 
	 * between now and the given date.
	 * 
	 * @param then
	 * @return	a string that represents the timespan between two dates
	 */
	public static String getTimespan(Date then) { 
		return getTimespan(new Date(), then);
	}
	
	public static String getTimespan(Date now, Date then) {
		return getTimespan(now, then, true);
	}
	
	/**
	 * Returns a string that represents the time span that has elapsed 
	 * between the given dates (e.g. one hour ago, 5 weeks ago).  
	 * 
	 * @param now
	 * @param then
	 * @return	a string that represents the timespan between two dates
	 */
	public static String getTimespan(Date now, Date then, boolean showAgoWord) {

		if (now == null || then == null) { 
			return "";
		}
		
		// Time span between two dates (in seconds)
		long delta = (now.getTime() - then.getTime()) / MILLISECOND;
		
		String suffix = showAgoWord ? " ago" : "";

		if (delta < 0) { 
			return "(in the future?)";
		}
		if (delta < 1 * MINUTE) {
			return (delta / SECOND) == 1 ? "one second" + suffix : (delta / SECOND) + " seconds" + suffix;
		}
		if (delta < 2 * MINUTE) {
			return "a minute" + suffix;
		}
		if (delta < 45 * MINUTE) {
			return (delta / MINUTE) + " minutes" + suffix;
		}
		if (delta < 90 * MINUTE) {
			return "an hour" + suffix;
		}
		if (delta < 24 * HOUR) {
			return (delta / HOUR) + " hours" + suffix;
		}
		if (delta < 48 * HOUR && showAgoWord) {
			return "yesterday";
		}
		if (delta < 30 * DAY) {
			return (delta / DAY) + " days" + suffix;
		}
		if (delta < 12 * MONTH) {
			int months = (int) (delta / (DAY * 30));
			return months <= 1 ? "one month" + suffix : months + " months" + suffix;
		} else {
			int years = (int) (delta / (DAY * 365));
			return years <= 1 ? "one year" + suffix : years + " years" + suffix;
		}

	}

	/**
	 * Utility method to format a date in the given format
	 * @param d the date to format
	 * @return a String representing the date in the passed format
	 */
	public static String formatDate(Date d, String format) {
		DateFormat df = new SimpleDateFormat(format);
		return df.format(d);
	}
	
	/**
	 * Utility method to format a date in the given format and locale
	 * @param d the date to format
	 * @param locale the locale to use
	 * @return a String representing the date in the passed format
	 */
	public static String formatDate(Date d, String format, Locale locale) {
		DateFormat df = new SimpleDateFormat(format, locale);
		return df.format(d);
	}

	/**
	 * Utility method to determine the number of hours between two dates (rounding down)
	 * 
	 * @param a
	 * @param b
	 * @return the number of hours between a and b
	 */
	public static int getHoursBetween(Date a, Date b) {
		long diff = (b.getTime() - a.getTime()) / MILLISECOND;
		if (diff < 0)
			diff = -diff;
		diff /= HOUR;
		return (int) diff;
	}

	
	/**
	 * 
	 * @param currentDate
	 * @param periodType
	 * @return
	 */
	public static Date getStartOfPeriod(Date currentDate, int period) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		switch (period) { 		
			case DAILY: 
				return getStartOfDay(currentDate);
			case WEEKLY:								
				return getStartOfWeek(currentDate);
			case MONTHLY: 
				return getStartOfMonth(currentDate);
			case QUARTERLY:				
				return getStartOfQuarter(currentDate);
			case ANNUALLY:
				return getStartOfYear(currentDate);
		}
		return currentDate;
		
	}
	
	
	public static Date getStartOfWeek(Date currentDate) { 		
		return getStartOfCalendarPeriod(currentDate, Calendar.DAY_OF_WEEK);
	}
		
	public static Date getStartOfMonth(Date currentDate) { 
		return getStartOfCalendarPeriod(currentDate, Calendar.DAY_OF_MONTH);
	}

	public static Date getStartOfQuarter(Date currentDate) { 
		throw new APIException("Not implemented yet");
	}	

	public static Date getStartOfYear(Date currentDate) { 
		return getStartOfCalendarPeriod(currentDate, Calendar.DAY_OF_YEAR);
	}
	
	public static Date getStartOfCalendarPeriod(Date currentDate, int field) { 
		if (currentDate == null) 
			throw new APIException("Please specify a date");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.set(field, calendar.getActualMinimum(field));
		return calendar.getTime();
	}
	
	/**
	 * 
	 * @param currentDate
	 * @param periodType
	 * @return
	 */
	public static Date getEndOfPeriod(Date currentDate, int period) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		switch (period) { 		
			case DAILY: 
				return getEndOfDay(currentDate);
			case WEEKLY:								
				return getEndOfWeek(currentDate);
			case MONTHLY: 
				return getEndOfMonth(currentDate);
			case QUARTERLY:				
				return getEndOfQuarter(currentDate);
			case ANNUALLY:
				return getEndOfYear(currentDate);
		}
		return currentDate;
		
	}
	
	
	public static Date getEndOfWeek(Date currentDate) { 		
		return getEndOfCalendarPeriod(currentDate, Calendar.DAY_OF_WEEK);
	}
		
	public static Date getEndOfMonth(Date currentDate) { 
		return getEndOfCalendarPeriod(currentDate, Calendar.DAY_OF_MONTH);
	}

	public static Date getEndOfQuarter(Date currentDate) { 
		throw new APIException("Not implemented yet");
	}	

	public static Date getEndOfYear(Date currentDate) { 
		return getEndOfCalendarPeriod(currentDate, Calendar.DAY_OF_YEAR);
	}
	
	public static Date getEndOfCalendarPeriod(Date currentDate, int field) { 
		if (currentDate == null) 
			throw new APIException("Please specify a date");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.set(field, calendar.getActualMaximum(field));
		return calendar.getTime();
	}	
	
	
}

