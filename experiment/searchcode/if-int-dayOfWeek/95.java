package com.gdpcons.tpl.rfi.periodicity.timetable;
import static org.joda.time.DateTimeConstants.APRIL;
import static org.joda.time.DateTimeConstants.AUGUST;
import static org.joda.time.DateTimeConstants.DECEMBER;
import static org.joda.time.DateTimeConstants.FRIDAY;
import static org.joda.time.DateTimeConstants.JANUARY;
import static org.joda.time.DateTimeConstants.JUNE;
import static org.joda.time.DateTimeConstants.MAY;
import static org.joda.time.DateTimeConstants.MONDAY;
import static org.joda.time.DateTimeConstants.NOVEMBER;
import static org.joda.time.DateTimeConstants.SATURDAY;
import static org.joda.time.DateTimeConstants.SUNDAY;
import static org.joda.time.DateTimeConstants.THURSDAY;
import static org.joda.time.DateTimeConstants.TUESDAY;
import static org.joda.time.DateTimeConstants.WEDNESDAY;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import org.joda.time.LocalDate;
import org.joda.time.MonthDay;
import org.joda.time.ReadablePartial;

public class JodaTimeUtils {
	/**
	 * Calculates the nth occurrence of a day of the week, for a given month and
	 * year.
	 * 
	 * @param dayOfWeek
	 *            The day of the week to calculate the day for (In the range of
	 *            [1,7], where 1 is Monday.
	 * @param month
	 *            The month to calculate the day for.
	 * @param year
	 *            The year to calculate the day for.
	 * @param n
	 *            The occurrence of the weekday to calculate. (ie. 1st, 2nd,
	 *            3rd)
	 * @return A {@link LocalDate} with the nth occurrence of the day of week,
	 *         for the given month and year.
	 */
	public static LocalDate nthWeekdayOfMonth(int n, int dayOfWeek, int month, int year) {
		LocalDate start = new LocalDate(year, month, 1);
		LocalDate date = start.withDayOfWeek(dayOfWeek);
		return (date.isBefore(start)) ? date.plusWeeks(n) : date.plusWeeks(n - 1);
	}
	
	public static MonthDay localDateToMonthDay(final LocalDate date) {
		return new MonthDay(date.getMonthOfYear(), date.getDayOfMonth());
	}
	
	public static SortedSet<LocalDate> getAllWeekDaysInYear(final int year, final int weekDay) {
		final SortedSet<LocalDate> allWeekDays = new TreeSet<LocalDate>();
		
		LocalDate date = nthWeekdayOfMonth(1, weekDay, JANUARY, year);
		final LocalDate end = new LocalDate(year, DECEMBER, 31);
		for (; !date.isAfter(end); date = date.plusDays(7)) {
			allWeekDays.add(date);
		}
				
		return Collections.unmodifiableSortedSet(allWeekDays);
	}
	
	public static SortedSet<LocalDate> getItalianRecurrencies(final int year) {
		final SortedSet<LocalDate> dates = new TreeSet<LocalDate>();
		
		dates.add(new LocalDate(year, JANUARY, 1));
		dates.add(new LocalDate(year, JANUARY, 6));
		dates.add(getEasterMonday(year));
		dates.add(new LocalDate(year, APRIL, 25));
		dates.add(new LocalDate(year, MAY, 1));
		dates.add(new LocalDate(year, JUNE, 2));
		dates.add(new LocalDate(year, AUGUST, 15));
		dates.add(new LocalDate(year, NOVEMBER, 1));
		dates.add(new LocalDate(year, DECEMBER, 8));
		dates.add(new LocalDate(year, DECEMBER, 25));
		dates.add(new LocalDate(year, DECEMBER, 26));
		
		return Collections.unmodifiableSortedSet(dates);
	}
	
	public static SortedSet<LocalDate> getItalianHolidays(final int year) {
		return getItalianHolidays(year, 0);
	}
	
	public static SortedSet<LocalDate> getItalianHolidays(final int year, final int dayOffset)
	{
		final SortedSet<LocalDate> recurrencies = getItalianRecurrencies(year);
		final SortedSet<LocalDate> sundays = getAllWeekDaysInYear(year, SUNDAY);
		SortedSet<LocalDate> holidays = new TreeSet<LocalDate>(recurrencies);
		holidays.addAll(sundays);
		if (dayOffset != 0) {
			final SortedSet<LocalDate> shiftedHolidays = new TreeSet<LocalDate>();
			for (final LocalDate date: holidays) {
				shiftedHolidays.add(date.plusDays(dayOffset));
			}
			holidays = shiftedHolidays;
		}
		return Collections.unmodifiableSortedSet(holidays);
	}
	
	public static LocalDate getEasterSunday(final int year) {
		int a = year % 19;
		int b = year / 100;
		int c = year % 100;
		int d = b / 4;
		int e = b % 4;
		int f = (b + 8) / 25;
		int g = (b - f + 1) / 3;
		int h = (19 * a + b - d - g + 15) % 30;
		int i = c / 4;
		int k = c % 4;
		int L = (32 + 2 * e + 2 * i - h - k) % 7;
		int m = (a + 11 * h + 22 * L) / 451;

		int month = (h + L - 7 * m + 114) / 31;
		int day = ((h + L - 7 * m + 114) % 31) + 1;

		return new LocalDate(year, month, day);
	}
	
	public static LocalDate getEasterMonday(final int year) {
		return getEasterSunday(year).plusDays(1);
	}
	
	public static void printPartials(final PrintStream ps, final Collection<? extends ReadablePartial> c) {
		for (final ReadablePartial p: c) {
			ps.print(p);
			ps.print(" ");
		}
	}
	
	public static String weekDayToString(int weekday) {
		switch(weekday) {
		case MONDAY:
			return "monday";
		case TUESDAY:
			return "tuesday";
		case WEDNESDAY:
			return "wednesday";
		case THURSDAY:
			return "thursday";
		case FRIDAY:
			return "friday";
		case SATURDAY:
			return "saturday";
		case SUNDAY:
			return "sunday";
		default:
			throw new IllegalStateException("Illegal weekday");
		}
	}

}

