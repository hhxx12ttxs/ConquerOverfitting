package nl.openedge.util;

import java.util.Calendar;
import java.util.Date;

/**
 * Struct class for working with dates disregarding the time fields.
 * 
 * @author Peter Veenendaal (Levob)
 * @author Eelco Hillenius
 */
public final class YearMonthDay
{
	/**
	 * Constant for month January; value == 1.
	 */
	public static final int JANUARY = 1;

	/**
	 * Constant voor month February; value == 2.
	 */
	public static final int FEBRUARY = 2;

	/**
	 * Constant voor month March; value == 3.
	 */
	public static final int MARCH = 3;

	/**
	 * Constant voor month April; value == 4.
	 */
	public static final int APRIL = 4;

	/**
	 * Constant voor month May; value == 5.
	 */
	public static final int MAY = 5;

	/**
	 * Constant voor month June; value == 6.
	 */
	public static final int JUNE = 6;

	/**
	 * Constant voor month July; value == 7.
	 */
	public static final int JULY = 7;

	/**
	 * Constant voor month August; value == 8.
	 */
	public static final int AUGUST = 8;

	/**
	 * Constant voor month September; value == 9.
	 */
	public static final int SEPTEMBER = 9;

	/**
	 * Constant voor month October; value == 10.
	 */
	public static final int OCTOBER = 10;

	/**
	 * Constant voor month November; value == 11.
	 */
	public static final int NOVEMBER = 11;

	/**
	 * Constant voor month December; value == 12.
	 */
	public static final int DECEMBER = 12;

	/**
	 * Number of months in a year; value = 12.
	 */
	public static final int NUMBER_OF_MONTHS_IN_A_YEAR = 12;

	/**
	 * Number of quarters in a year; value = 4.
	 */
	public static final int NUMBER_OF_QUARTERS_IN_A_YEAR = 4;

	/**
	 * Number of half years in a year; value = 2.
	 */
	public static final int NUMBER_OF_HALF_YEARS_IN_A_YEAR = 2;

	/** Factor year; value = 10000. */
	private static final int YEAR_FACTOR = 10000;

	/** Factor month; value = 100. */
	private static final int MONTH_FACTOR = 100;

	/** year. */
	private int year;

	/** month (from 1 - 12). */
	private int month;

	/** day in month. */
	private int dayInMonth;

	/**
	 * Construct with date object.
	 * 
	 * @param date
	 *            date object
	 */
	public YearMonthDay(Date date)
	{
		Calendar calendar = getCalendar(date);
		year = calendar.get(Calendar.YEAR);
		month = YearMonthDayHelper.getYearMonthDayMonth(calendar.get(Calendar.MONTH));
		dayInMonth = calendar.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Construct with year and month, dayInMonth will be 1.
	 * 
	 * @param theYear
	 *            year
	 * @param theMonth
	 *            month
	 */
	public YearMonthDay(int theYear, int theMonth)
	{
		year = theYear;
		month = theMonth;
		while (month > NUMBER_OF_MONTHS_IN_A_YEAR)
		{
			month -= NUMBER_OF_MONTHS_IN_A_YEAR;
			year++;
		}
		dayInMonth = 1;
	}

	/**
	 * Increment date with years, months and days.
	 * 
	 * @param date
	 *            date to increment
	 * @param years
	 *            year
	 * @param months
	 *            month
	 * @param days
	 *            dayInMonth
	 * @return Date verhoogde datum.
	 */
	public static Date incrementDate(Date date, int years, int months, int days)
	{
		return incrementDate(date, years, months, days, true);
	}

	/**
	 * Increment date with years, months and days.
	 * 
	 * @param date
	 *            date to increment
	 * @param years
	 *            years
	 * @param months
	 *            months
	 * @param days
	 *            days
	 * @param lenient
	 *            is lenient
	 * @return Date verhoogde datum.
	 */
	public static Date incrementDate(Date date, int years, int months, int days, boolean lenient)
	{
		if (date == null)
		{
			throw new IllegalArgumentException("datum is leeg.");
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setLenient(lenient);
		calendar.setTime(date);
		calendar.add(Calendar.YEAR, years);
		calendar.add(Calendar.MONTH, months);
		calendar.add(Calendar.DATE, days);
		return calendar.getTime();
	}

	/**
	 * Create calendar object based on date.
	 * 
	 * @param date
	 *            date
	 * @return Calendar calendar object based on date
	 */
	private Calendar getCalendar(Date date)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}

	/**
	 * Geeft special number.
	 * 
	 * @return int (year * 10000 + month * 100 + dayInMonth)
	 */
	public int getYearMonthDayNumber()
	{
		return (year * YEAR_FACTOR + month * MONTH_FACTOR + dayInMonth);
	}

	/**
	 * Get day in month.
	 * 
	 * @return int day in month.
	 */
	public int getDayInMonth()
	{
		return dayInMonth;
	}

	/**
	 * Get year.
	 * 
	 * @return int year
	 */
	public int getYear()
	{
		return year;
	}

	/**
	 * Get month (1 - 12).
	 * 
	 * @return int month
	 */
	public int getMonth()
	{
		return month;
	}

	/**
	 * Get this as a Date object.
	 * 
	 * @return Date this as a Date object
	 */
	public Date toDate()
	{
		return YearMonthDayHelper.getDate(year, month, dayInMonth);
	}

}

