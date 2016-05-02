/*
 * Handling of weeks are not good in most Calendar and date classes.
 * This class helps with that and other Date related stuff
 */

//TODO expand so that it Uses YearWeek instead of an integer for the Week
//For now the week methods only works for the current year

package se.chalmers.tda367.group6.studytimeproject.utils;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

public class DateUtils {

	/**
	 * 
	 * @param week
	 * @return date of the first day of the specified week
	 */
	public static String firstDayOfWeek(int week) {
		return getDayOfWeek(week, DateTimeConstants.MONDAY);
	}
	
	/**
	 * 
	 * @param week
	 * @return date of the last day of the specified week
	 */
	public static String lastDayOfWeek(int week) {
		return getDayOfWeek(week, DateTimeConstants.SUNDAY);
	}
	
	/** 
	 * private method that calculates first and last day of week
	 */
	private static String getDayOfWeek(int week, int day) {
		int thisWeek = currentWeek();
		
		LocalDate date = new LocalDate();

		if(week==thisWeek) {
			return date.withDayOfWeek(day).toString();
		} else if(week > thisWeek) {
			int weekDifference = week-thisWeek;
			date.minusWeeks(weekDifference);
			return date.withDayOfWeek(day).toString();
		} else {
			int weekDifference = thisWeek-week;
			date.plusWeeks(weekDifference);
			return date.withDayOfWeek(day).toString();
		}
	}
	
	/**
	 * 
	 * @return current week as an int
	 */
	public static int currentWeek() {
		LocalDate date = new LocalDate();
		return date.getWeekOfWeekyear();
	}
	
	/**
	 * 
	 * @return current year as an int
	 */
	public static int currentYear() {
		LocalDate date = new LocalDate();
		return date.getYear();
	}
	
	/**
	 * 
	 * @return todays date
	 */
	public static LocalDate todaysDate () {
		return LocalDate.now();
	}
}

