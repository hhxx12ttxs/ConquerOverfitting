/*
 * (ConfigurationXML) copyright BP Pensions 2003
 */
package com.bp.pensionline.util;

// JDK Imports
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;
//import org.htmlparser.tags.FormTag;

// J2EE Imports 

/**
 *  A generic date utility class that provides convenience methods to common 
 *  date related tasks.  eg day/month/year-->Date or munging oracle date strings
 *  
 *  Created by: mcobby, last updated by $Author: dom_the_bom $
 *
 *  @author dom_the_bom
 *  @author $Author: dom_the_bom $
 *  @version $Revision: 1.1 $
 * 
 *  $Id: DateUtil.java,v 1.1 2007/02/11 00:38:06 dom_the_bom Exp $
 */
public class DateUtil {
	static Logger log = Logger.getLogger(DateUtil.class.getName());

	private static SimpleDateFormat shortMonthNameFormatter = null;

	private static SimpleDateFormat longMonthNameFormatter = null;

	private static boolean inititialsed = false;

	/**
	 * 
	 */
	public DateUtil() {
		super();
	}

	private static void init() {
		synchronized (shortMonthNameFormatter) {
			shortMonthNameFormatter = new SimpleDateFormat("MM");
			longMonthNameFormatter = new SimpleDateFormat("MMMM");
			inititialsed = true;
		}
	}

	/**
	 * @param day
	 * @param month
	 * @param year
	 * @return
	 */
	public static Date toDate(int day, int month, int year) {
		GregorianCalendar cal = new GregorianCalendar(year, month - 1, day);
		return cal.getTime();
	}

	/**
	 * Gives you back a date object that has been reset to the first day of the current
	 * month.  The time ascepct is left at it so if you call this method at 16:07 on the 
	 * 21st October 2003 then the retruned date object will be "Wed Oct 01 16:07:41 BST 2003"
	 * @return The Date object for the first day of this month.
	 */
	public static Date getFirstDayOfMonth() {
		GregorianCalendar cal = new GregorianCalendar(); // now
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}

	/**
	 * Gives you back a date object that has been reset to the first day of the next
	 * month.  The time ascepct is left at it so if you call this method at 16:07 on the 
	 * 21st October 2003 then the retruned date object will be "Wed Oct 01 16:07:41 BST 2003"
	 * @return The Date object for the first day of this month.
	 */
	public static Date getFirstDayOfNextMonth() {
		GregorianCalendar cal = new GregorianCalendar(); // now
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
		return cal.getTime();
	}

	/**
	 * Gives you back a date object that has been reset to the first day of the next
	 * month on from the date/time given as a parameter.  The time ascepct is left at it so if you call this method at 16:07 on the 
	 * 21st October 2003 then the retruned date object will be "Wed Oct 01 16:07:41 BST 2003"
	 * @return The Date object for the first day of this month.
	 */
	public static Date getFirstDayOfNextMonth(Date myDate) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(myDate);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
		return cal.getTime();
	}

	/**
	 * Gives you back a date object that has been reset to the first day of the next
	 * month.  The time ascepct is left at it so if you call this method at 16:07 on the 
	 * 21st October 2003 then the retruned date object will be "Wed Oct 01 16:07:41 BST 2003"
	 * @return The Date object for the first day of this month.
	 */
	public static Date getFirstDayOfLastMonth() {
		GregorianCalendar cal = new GregorianCalendar(); // now
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
		return cal.getTime();
	}

	/**
	 * Returns the short english version of the month for the given date.  e.g. Jan, Feb, Mar ...
	 * @param date
	 * @return
	 */
	public static String getShortMonthName(Date date) {
		String result = null;
		if (date == null) {
			result = "NO DATE";
		} else {
			if (!inititialsed) {
				init();
			}
			result = shortMonthNameFormatter.format(date);
		}
		return result;
	}

	/**
	 * A niave approach to getting the english version of a month.  there will 
	 * be a better way of doing this that uses locale information but this will do for now.
	 * @param month
	 * @return
	 */
	public static String getShortMonthName(int month) {
		String result = null;

		switch (month) {
		case Calendar.JANUARY:
			result = "Jan";
			break;
		case Calendar.FEBRUARY:
			result = "Feb";
			break;
		case Calendar.MARCH:
			result = "Mar";
			break;
		case Calendar.APRIL:
			result = "Apr";
			break;
		case Calendar.MAY:
			result = "May";
			break;
		case Calendar.JUNE:
			result = "June";
			break;
		case Calendar.JULY:
			result = "Jul";
			break;
		case Calendar.AUGUST:
			result = "Aug";
			break;
		case Calendar.SEPTEMBER:
			result = "Sep";
			break;
		case Calendar.OCTOBER:
			result = "Oct";
			break;
		case Calendar.NOVEMBER:
			result = "Nov";
			break;
		case Calendar.DECEMBER:
			result = "Dec";
			break;
		default:
			result = "INVALID MONTH";
			break;
		}

		return result;
	}

	/**
	 * Returns the long/full english version of the month for the given date.  e.g. January, February, March
	 * @param date
	 * @return
	 */
	public static String getLongMonthName(Date date) {
		String result = null;
		if (date == null) {
			result = "NO DATE";
		} else {
			if (!inititialsed) {
				init();
			}
			result = longMonthNameFormatter.format(date);
		}
		return result;
	}

	/**
	 * A niave approach to getting the english version of a month.  there will 
	 * be a better way of doing this that uses locale information but this will do for now.
	 * @param month
	 * @return
	 */
	public static String getLongMonthName(int month) {
		String result = null;

		switch (month) {
		case Calendar.JANUARY:
			result = "January";
			break;
		case Calendar.FEBRUARY:
			result = "February";
			break;
		case Calendar.MARCH:
			result = "March";
			break;
		case Calendar.APRIL:
			result = "April";
			break;
		case Calendar.MAY:
			result = "May";
			break;
		case Calendar.JUNE:
			result = "June";
			break;
		case Calendar.JULY:
			result = "July";
			break;
		case Calendar.AUGUST:
			result = "August";
			break;
		case Calendar.SEPTEMBER:
			result = "September";
			break;
		case Calendar.OCTOBER:
			result = "October";
			break;
		case Calendar.NOVEMBER:
			result = "November";
			break;
		case Calendar.DECEMBER:
			result = "December";
			break;
		default:
			result = "INVALID MONTH";
			break;
		}

		return result;
	}

	public static String toStringMMDDYYYY(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		return formatter.format(date);
	}
	public static String toStringDDMMMYYYY(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
		return formatter.format(date);
	}
	public static String toStringDDMMMYYYYwithDash(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
		return formatter.format(date);
	}
	
	/**
	 * @param date_1 The left hand side of the compare.
	 * @param date_2 The right hand side of the compare.
	 * @return Less than zero if d1 is less than d2, zero if the same, and greater that zerof d1 greater that d2.
	 */
	public static int compareDates(Date date_1, Date date_2) {
		return date_1.compareTo(date_2);

	}

	/**
	 * <p>Returns the number of years difference between two dates Rounded down for fractions of years.</p> 
	 
	 */
	public static int getDateDiff(Date date1, Date date2) {
		log.debug("getDateDiff entry, Date1: " + date1 + ", Date2: " + date1);
		long diff = 0;
		long dateDiff = date2.getTime() - date1.getTime();

		diff = (dateDiff / ((long) 1000 * 60 * 60 * 24 * 365));

		log.debug("getDateDiff exit, difference in years: " + diff
				+ ", difference .getTime(): " + dateDiff);
		return (int) diff;
	}

	/**
	 * @param date_str The date to be reformatted, in Oracle format.
	 * @return A string representing the date in the format "d Mmm yyyy"
	 *
	 public static String rationaliseOracleDate (String date_str) {
	 // returned string -> "1973-10-08 00:00:00.0"
	 return toString(toDate(date_str, "yyyy-MM-dd hh:mm:ss.S"), "d MMM yyyy");

	 } 


	 /**
	 * Returns the tax year end which last passed
	 * @return
	 */
	public static Date mostRecentTaxYearEndDate() {
		Date now = new Date();
		Calendar cnow = new GregorianCalendar();
		cnow.setTime(now);
		// Tax Year End is always April 6th..
		Date tye = toDate(6, 4, cnow.get(Calendar.YEAR));
		Calendar ctye = new GregorianCalendar();
		ctye.setTime(tye);

		// if we are before the tax yera end in this calendar year,
		// then we must be in Jan, Feb, or March.. so subtract a year.
		if (compareDates(now, tye) < 0)
			ctye.add(Calendar.YEAR, -1);

		return ctye.getTime();
	}

	/**
	 * Returns the next tax year end- it's approaching fast! Have you filed YOUR tax return yet? 
	 * @return
	 */
	public static Date getThisTaxYearEndDate() {
		Date now = new Date();
		Calendar cnow = new GregorianCalendar();
		cnow.setTime(now);
		// Tax Year End is always April 5th, take this year as starting point
		Date tye = toDate(5, 4, cnow.get(Calendar.YEAR));
		Calendar ctye = new GregorianCalendar();
		ctye.setTime(tye);

		// if TYE is before current date, we have to add a year to get the 
		// correct year.
		if (tye.compareTo(now) < 0) { // < 0 if tye is before now
			ctye.add(Calendar.YEAR, 1);
		}

		return ctye.getTime();
	}

	/**
	 * Adds specified number of days to the date.
	 * @param date
	 * @param days
	 * @return
	 * @throws Exception
	 */
	public static Date addDays(Date date, int days) throws Exception {
		if (date == null) {
			throw new Exception("addDays: no date specified");
		}

		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}

	/**
	 * Adds specified number of ms to the date.
	 * @param date
	 * @param ms
	 * @return
	 * @throws Exception
	 */
	public static Date addMs(Date date, int ms) throws Exception {
		if (date == null) {
			throw new Exception("addDays: no date specified");
		}

		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.MILLISECOND, ms);
		return cal.getTime();
	}

	/**
	 * Adds specified number of years to the date.
	 * @param date
	 * @param years
	 * @return
	 * @throws Exception
	 */
	public static Date addYears(Date date, int years) throws Exception {
		if (date == null) {
			throw new Exception("addYears: no date specified");
		}

		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.YEAR, years);
		return cal.getTime();
	}

	/**
	 * Subtracts specified number of days to the date. USes the addDays method to actually do teh work.
	 * @param date
	 * @param days
	 * @return
	 * @throws DateUtilException
	 */
	public static Date subtractDays(Date date, int days) throws Exception {
		// simply use the add days but turn the number negative
		return addDays(date, days * -1);
	}

	/**
	 * Returns the number of days difference between two dates - works using milliseconds.
	 * will give you an absolute number of 24hr chunks
	 * Rounded down for fractions of years
	 */
	public static long getDayDiff(Date date1, Date date2) {
		log.debug("getDateDiff entry, Date1: " + date1 + ", Date2: " + date1);
		long diff = 0;
		long dateDiff = date2.getTime() - date1.getTime();

		diff = (dateDiff / ((long) 1000 * 60 * 60 * 24));

		log.debug("getDateDiff exit, difference in years: " + diff
				+ ", difference .getTime(): " + dateDiff);
		return diff;
	}

	/**
	 * Returns the number of years difference between two dates uses milliseconds and has no concept
	 * of leap years - will not be totally accurate but ok for a quick guess
	 * Rounded down for fractions of years
	 */
	public static float getApproxYearDiffAsFloat(Date date1, Date date2) {
		log.debug("getDateDiff entry, Date1: " + date1 + ", Date2: " + date1);
		float diff = 0;
		long dateDiff = date2.getTime() - date1.getTime();
		diff = ((float) dateDiff / ((long) 1000 * 60 * 60 * 24 * 365));
		log.debug("getDateDiff exit, difference in years: " + diff
				+ ", difference .getTime(): " + dateDiff);
		return diff;
	}

	/**
	 * The definition of time unit change is relatively simple: If you are counting days, you simply 
	 * count the number of times the date has changed. For example, if something starts on the 15th and 
	 * ends on the 17th, 2 days have passed. (The date changed first to the 16th, then to the 17th.) 
	 * Similarly, if a process starts at 3:25 in the afternoon and finishes at 4:10 p.m., 1 hour has 
	 * passed because the hour has changed once (from 3 to 4).
	 * Libraries often calculate time in this manner. For example, if I borrow a book from my library, 
	 * I don't need to have the book in my possession for a minimum of 24 hours for the library to consider 
	 * it borrowed for one day. Instead, the day that I borrow the book is recorded on my account. As soon 
	 * as the date switches to the next day, I have borrowed the book for one day, even though the amount 
	 * of time is often less than 24 hours. 
	 * @param g1
	 * @param g2
	 * @return
	 */
	public static long getElapsedDays(GregorianCalendar g1, GregorianCalendar g2) {
		long elapsed = 0;
		GregorianCalendar gc1, gc2;

		if (g2.after(g1)) {
			gc2 = (GregorianCalendar) g2.clone();
			gc1 = (GregorianCalendar) g1.clone();
		} else {
			gc2 = (GregorianCalendar) g1.clone();
			gc1 = (GregorianCalendar) g2.clone();
		}

		gc1.clear(Calendar.MILLISECOND);
		gc1.clear(Calendar.SECOND);
		gc1.clear(Calendar.MINUTE);
		gc1.clear(Calendar.HOUR_OF_DAY);

		gc2.clear(Calendar.MILLISECOND);
		gc2.clear(Calendar.SECOND);
		gc2.clear(Calendar.MINUTE);
		gc2.clear(Calendar.HOUR_OF_DAY);

		while (gc1.before(gc2)) {
			gc1.add(Calendar.DATE, 1);
			elapsed++;
		}
		
		return elapsed;
	}

	public static long getElapsedMonths(GregorianCalendar g1,
			GregorianCalendar g2) {
		long elapsed = 0;
		GregorianCalendar gc1, gc2;

		if (g2.after(g1)) {
			gc2 = (GregorianCalendar) g2.clone();
			gc1 = (GregorianCalendar) g1.clone();
		} else {
			gc2 = (GregorianCalendar) g1.clone();
			gc1 = (GregorianCalendar) g2.clone();
		}

		gc1.clear(Calendar.MILLISECOND);
		gc1.clear(Calendar.SECOND);
		gc1.clear(Calendar.MINUTE);
		gc1.clear(Calendar.HOUR_OF_DAY);
		gc1.clear(Calendar.DATE);

		gc2.clear(Calendar.MILLISECOND);
		gc2.clear(Calendar.SECOND);
		gc2.clear(Calendar.MINUTE);
		gc2.clear(Calendar.HOUR_OF_DAY);
		gc2.clear(Calendar.DATE);

		while (gc1.before(gc2)) {
			gc1.add(Calendar.MONTH, 1);
			elapsed++;
		}
		return elapsed;
	}

	public static long getElapsedYears(GregorianCalendar g1,
			GregorianCalendar g2) {
		long elapsed = 0;
		GregorianCalendar gc1, gc2;

		if (g2.after(g1)) {
			gc2 = (GregorianCalendar) g2.clone();
			gc1 = (GregorianCalendar) g1.clone();
		} else {
			gc2 = (GregorianCalendar) g1.clone();
			gc1 = (GregorianCalendar) g2.clone();
		}

		gc1.clear(Calendar.MILLISECOND);
		gc1.clear(Calendar.SECOND);
		gc1.clear(Calendar.MINUTE);
		gc1.clear(Calendar.HOUR_OF_DAY);
		gc1.clear(Calendar.DATE);
		gc1.clear(Calendar.MONTH);

		gc2.clear(Calendar.MILLISECOND);
		gc2.clear(Calendar.SECOND);
		gc2.clear(Calendar.MINUTE);
		gc2.clear(Calendar.HOUR_OF_DAY);
		gc2.clear(Calendar.DATE);
		gc2.clear(Calendar.MONTH);

		while (gc1.before(gc2)) {
			gc1.add(Calendar.YEAR, 1);
			elapsed++;
		}
		return elapsed;
	}

	/**
	 * Returns the age given their date of birth
	 * @param dob
	 * @return
	 */
	public static int getAge(Date dob) throws Exception {
		return getAgeAtDate(dob, new Date());
	}

	/**
	 * Returns the age at a specified date
	 * @param dob
	 * @return
	 */
	public static int getAgeAtDate(Date dob, Date doc) throws Exception {
		// simply use the number of years between DOB and NOW
		if (dob == null || doc == null) {
			return 0;
		}
		else{

			Calendar dukesBirthday = new GregorianCalendar(); //(1995, Calendar.MAY, 23);
			dukesBirthday.setTime(dob);
	
			Calendar now = new GregorianCalendar(); //Calendar.getInstance();
			now.setTime(doc);
	
			int dukesAge = now.get(Calendar.YEAR)
					- dukesBirthday.get(Calendar.YEAR);
			dukesBirthday.add(Calendar.YEAR, dukesAge);
	
			if (now.before(dukesBirthday)) {
				dukesAge--;
			}
			return dukesAge;
		}
	}

	/** Below does not handle birthday.....!!!!!
	 * Returns the age at a specified date
	 * @param dob
	 * @return
	 *
	 public static int getAgeAtDate(Date dob, Date ageNow) throws DateUtilException{
	 if (dob==null || ageNow==null) {
	 throw new DateUtilException("A NULL date has been supplied to getAge()");
	 }

	 // simply use the number of years between DOB and NOW
	 int age = getYearsBetween(dob, ageNow);            
	 return age;
	 }   
	 
	 
	 /**
	 * Returns the age given their date of birth
	 * @param dob
	 * @return
	 */
	public static int getYearsBetween(Date startDate, Date endDate)
			throws Exception {
		if (startDate == null || endDate == null) {
			String msg = "Can't work with a NULL date!";
			log.error(msg);
			throw new Exception(msg);
		}

		if (startDate.getTime() > endDate.getTime()) {
			String msg = "DateUtil.getYearsBetween: the end date seems to be before the start date!";
			log.error(msg);
			throw new Exception(msg);
		}

		GregorianCalendar start = new GregorianCalendar();
		start.setTime(startDate);
		GregorianCalendar end = new GregorianCalendar();
		end.setTime(endDate);
		// Get number of years between these two dates
		int diff = end.get(Calendar.YEAR) - start.get(Calendar.YEAR);

		// Add the tentative num of years to the start date to get this year's anniversary
		start.add(Calendar.YEAR, diff);

		// If this year's anniversary has not happened yet, subtract one from num of years
		if (end.before(start)) {
			diff--;
		}

		return diff;
	}

	/**
	 * Returns the age given their date of birth
	 * @param dob
	 * @return
	 */
	public static float getYearsBetweenAsFloat(Date startDate, Date endDate)
			throws Exception {
		if (startDate == null || endDate == null) {
			String msg = "Can't work with a NULL date!";
			log.error(msg);
			throw new Exception(msg);
		}

		if (startDate.getTime() > endDate.getTime()) {
			String msg = "DateUtil.getYearsBetween: the end date seems to be before the start date!";
			log.error(msg);
			throw new Exception(msg);
		}

		GregorianCalendar start = new GregorianCalendar();
		start.setTime(startDate);
		GregorianCalendar end = new GregorianCalendar();
		end.setTime(endDate);
		// Get number of years between these two dates
		int diff = end.get(Calendar.YEAR) - start.get(Calendar.YEAR);

		// Add the tentative num of years to the start date to get this year's anniversary
		start.add(Calendar.YEAR, diff);

		// If this year's anniversary has not happened yet, subtract one from num of years
		if (end.before(start)) {
			diff--;
			start.add(Calendar.YEAR, -1);
		}

		// diff is the number of total years, get days left over and convert to % of year
		float days = (float) (DateUtil.getElapsedDays(start, end) / 365.25);

		// add together total amount and return
		return (diff) + days;
	}

	public static java.sql.Date getDate(String value) {
		//java.sql.Date date=null;
		java.util.Date d = null;
		java.sql.Date date=null;
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
		try {
			if(value != null){
				d = formatter.parse(value);				
				date=new java.sql.Date(d.getTime());
			}
			return date;
		} catch (Exception e) {
			// TODO: handle exception
			date=new java.sql.Date(System.currentTimeMillis());
			log.debug("getNormalDate error value to convert : "+value); 
			return date;
			//throw e;
		}
	}
	public static java.util.Date getNormalDate(String value){
		java.util.Date d = null;
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
		try {				
			if(value != null && value.length() > 0){
				d = formatter.parse(value);
			}
			else{
				d = new Date();				
			}

			return d;
		} catch (Exception e) {
			// TODO: handle exception
			d=new Date();
			e.printStackTrace();
			return d;
			//throw e;
		}
		
	}

}

