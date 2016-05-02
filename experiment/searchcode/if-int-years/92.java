/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>, Artjom Kochtchi
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package ilarkesto.base.time;

import ilarkesto.base.Str;
import ilarkesto.base.Tm;
import ilarkesto.base.Utl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

public final class Date extends ilarkesto.core.time.Date {

	public static final transient SimpleDateFormat FORMAT_DAY_MONTH_SHORTYEAR = new SimpleDateFormat("dd.MM.yy");
	public static final transient SimpleDateFormat FORMAT_DAY_MONTH_YEAR = new SimpleDateFormat("dd.MM.yyyy");
	public static final transient SimpleDateFormat FORMAT_LONGMONTH_DAY_YEAR = new SimpleDateFormat("MMMM d, yyyy");
	public static final transient SimpleDateFormat FORMAT_DAY_MONTH = new SimpleDateFormat("dd.MM.");
	public static final transient SimpleDateFormat FORMAT_WEEKDAY_DAY_MONTH = new SimpleDateFormat("EEEE, dd.MM.");
	public static final transient SimpleDateFormat FORMAT_DAY_LONGMONTH_YEAR = new SimpleDateFormat("dd. MMMM yyyy");
	public static final transient SimpleDateFormat FORMAT_WEEKDAY_DAY_LONGMONTH_YEAR = new SimpleDateFormat(
			"EEEE, dd. MMMM yyyy");
	public static final transient SimpleDateFormat FORMAT_SHORTWEEKDAY_DAY_MONTH_YEAR = new SimpleDateFormat(
			"EE, dd.MM.yyyy");
	public static final transient SimpleDateFormat FORMAT_LONGMONTH = new SimpleDateFormat("MMMM");
	public static final transient SimpleDateFormat FORMAT_LONGMONTH_YEAR = new SimpleDateFormat("MMMM yyyy");

	public static final transient SimpleDateFormat FORMAT_YEAR_MONTH_DAY = new SimpleDateFormat("yyyy-MM-dd");
	public static final transient SimpleDateFormat FORMAT_YEAR_MONTH_DAY_NOSEP = new SimpleDateFormat("yyyyMMdd");

	public static final transient SimpleDateFormat FORMAT_WEEKDAY = new SimpleDateFormat("EEEE");

	public Date() {
		super();
	}

	public Date(java.util.Date javaDate) {
		super(javaDate);
	}

	public Date(long millis) {
		super(millis);
	}

	public Date(int year, int month, int day) {
		super(year, month, day);
	}

	public Date(String date) {
		super(date);
	}

	public Date(GregorianCalendar calendar) {
		this(calendar.getTime());
	}

	@Override
	protected Date newDate(java.util.Date javaDate) {
		return new Date(javaDate);
	}

	public GregorianCalendar getGregorianCalendar() {
		return new GregorianCalendar(year, month - 1, day);
	}

	public Date getFirstDateOfMonth() {
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(toJavaDate());
		c.set(GregorianCalendar.DAY_OF_MONTH, 1);
		return new Date(c);
	}

	public Date getLastDateOfMonth() {
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(toJavaDate());
		c.set(GregorianCalendar.DAY_OF_MONTH, c.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
		return new Date(c);
	}

	public Date addMonths(int count) {
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(toJavaDate());
		c.add(GregorianCalendar.MONTH, count);
		return new Date(c);
	}

	public Date addYears(int count) {
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(toJavaDate());
		c.add(GregorianCalendar.YEAR, count);
		return new Date(c);
	}

	public int getDaysInMonth() {
		return getGregorianCalendar().getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
	}

	public TimePeriod getPeriodTo(Date other) {
		return new TimePeriod(other.toMillis() - toMillis());
	}

	public TimePeriod getPeriodToNow() {
		return getPeriodTo(today());
	}

	public int getPeriodToInYears(Date other) {
		int years = other.year - year;
		if (month > other.month) {
			years--;
		} else if (month == other.month && day > other.day) {
			years--;
		}
		return years;
	}

	public int getPeriodToInMonths(Date other) {
		int years = other.year - year;
		int months = other.month - month;
		return (years * 12) + months;
	}

	public int getPeriodToNowInMonths() {
		return getPeriodToInMonths(today());
	}

	private String toDe() {
		StringBuilder sb = new StringBuilder();
		if (day < 10) sb.append('0');
		sb.append(day);
		sb.append(".");
		if (month < 10) sb.append('0');
		sb.append(month);
		sb.append(".");
		sb.append(year);
		return sb.toString();
	}

	public String toLongDe() {
		StringBuilder sb = new StringBuilder();
		sb.append(Tm.WEEKDAYS_DE[getGregorianCalendar().get(GregorianCalendar.DAY_OF_WEEK) - 1]);
		sb.append(", der ");
		sb.append(toDe());
		return sb.toString();
	}

	private String toInt() {
		return toString();
	}

	public String toLongInt() {
		StringBuilder sb = new StringBuilder();
		sb.append(Tm.WEEKDAYS[getGregorianCalendar().get(GregorianCalendar.DAY_OF_WEEK) - 1]);
		sb.append(", the ");
		sb.append(toInt());
		return sb.toString();
	}

	public String toString(DateFormat format) {
		return format.format(toJavaDate());
	}

	public String toString(Locale locale) {
		if (locale.equals(Locale.GERMANY)) return toDe();
		return toInt();
	}

	public String toLongString(Locale locale) {
		if (locale.equals(Locale.GERMANY)) return toLongDe();
		return toLongInt();
	}

	@Override
	public Date addDays(int days) {
		return (Date) super.addDays(days);
	}

	@Override
	public Date prevDay() {
		return addDays(-1);
	}

	@Override
	public Date nextDay() {
		return addDays(1);
	}

	// --- static ---

	private static transient Date today;

	private static transient long todayInvalidTime;

	public static Date latest(Date... dates) {
		Date latest = null;
		for (Date date : dates) {
			if (latest == null || date.isAfter(latest)) latest = date;
		}
		return latest;
	}

	public static Date earliest(Date... dates) {
		Date earliest = null;
		for (Date date : dates) {
			if (earliest == null || date.isBefore(earliest)) earliest = date;
		}
		return earliest;
	}

	public static Date today() {
		if (today == null || System.currentTimeMillis() > todayInvalidTime) {
			today = new Date();
			todayInvalidTime = tomorrow().toJavaDate().getTime() - 1;
		}
		return today;
	}

	public static Date tomorrow() {
		return new Date(System.currentTimeMillis() + Tm.DAY);
	}

	public static Date inDays(int numberOfDays) {
		return new Date(System.currentTimeMillis() + (Tm.DAY * numberOfDays));
	}

	public static Date beforeDays(int numberOfDays) {
		return new Date(System.currentTimeMillis() - (Tm.DAY * numberOfDays));
	}

	public static Date randomPast(int beforeMaxDays) {
		return Date.beforeDays(Utl.randomInt(0, beforeMaxDays));
	}

	public static Date parseTolerant(String s) throws ParseException {
		s = s.trim();
		String[] sa = Str.tokenize(s, ".,- ");
		if (sa.length == 0) throw new ParseException("Not a Date: " + s, -1);
		if (sa.length > 3) throw new ParseException("Not a Date: " + s, -1);
		int[] ia = new int[sa.length];
		for (int i = 0; i < ia.length; i++) {
			try {
				ia[i] = Integer.parseInt(sa[i]);
			} catch (NumberFormatException e) {
				throw new ParseException("Not a Date: " + s, -1);
			}
		}

		if (ia.length == 3) return new Date(Tm.year(ia[2]), ia[1], ia[0]);

		Date today = today();
		if (ia.length == 2) {
			if (ia[1] > 12) return new Date(Tm.year(ia[1]), ia[0], today.day);
			return new Date(today.year, ia[1], ia[0]);
		}

		if (ia[0] > 31) return new Date(Tm.year(ia[0]), today.month, today.day);
		return new Date(today.year, today.month, ia[0]);
	}

	// --- Object ---

	public boolean equalsIgnoreYear(Date d) {
		if (d == null) return false;
		return d.day == day && d.month == month;
	}

	public boolean equalsIgnoreDay(Date d) {
		if (d == null) return false;
		return d.year == year && d.month == month;
	}

}

