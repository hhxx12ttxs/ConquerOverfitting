/**
 * This class gets copy-and-pasted between the eu.future.earth.gwt.emul.java.util (used for
 * development and testing) and com.google.gwt.emul.java.util packages (used for gwt
 * compilation).  Change the comments on the package declaration appropriately.
 */
package eu.future.earth.gwt.emul.java.util;
//package java.util;

import java.util.Date;

public class GregorianCalendar extends Calendar implements Cloneable {

	private Date cached = null;
	private int year = 0;
	private int month = 0;
	private int day = 0;
	private int hour = 0;
	private int minute = 0;
	private int second = 0;

	public GregorianCalendar() {
		super();
		setTime(new Date());
	}
	
	public Object clone() {
		final GregorianCalendar clone = new GregorianCalendar();
		clone.setFirstDayOfWeek(getFirstDayOfWeek());
		clone.setMinimalDaysInFirstWeek(getMinimalDaysInFirstWeek());
		clone.setTime(getTime());
		return clone;
	}
	
	@SuppressWarnings("deprecation")
	public void clear() {
		cached = new Date(1970 - 1900, 0, 1, 0, 0, 0);
		computeFields();
	}
	
	@SuppressWarnings("deprecation")
	public void computeFields() {
		year = cached.getYear() + 1900;
		month = cached.getMonth();
		day = cached.getDate();
		hour = cached.getHours();
		minute = cached.getMinutes();
		second = cached.getSeconds();
	}
	
	@SuppressWarnings("deprecation")
	public void computeTime() {
		cached = new Date(year - 1900, month, day, hour, minute, second);		
		computeFields();  // recompute the fields because they may have been rolled
	}
		
	public Date getTime() {
		computeTime();
		return new Date(cached.getTime());
	}

    public void setTime(Date date) {
    	cached = new Date(date.getTime());
    	computeFields();
    }

	public void setTimeInMillis(long millis) {
		cached = new Date(millis);
		computeFields();
	}

	public long getTimeInMillis() {
		computeTime();
		return cached.getTime();
	}

	@SuppressWarnings("deprecation")
	public int get(int field) {
		computeTime();
		switch (field) {
		case YEAR: {
			return year;
		}
		case MONTH: {
			return month;
		}
		case DAY_OF_MONTH: {
			return day;
		}
		case HOUR: {
			return (hour > 11) ? hour - 12 : hour;
		}
		case AM_PM: {
			if (hour < 12) return AM;
			else return PM;
		}
		case HOUR_OF_DAY: {
			return hour;
		}
		case MINUTE: {
			return minute;
		}
		case SECOND: {
			return second;
		}
		case DAY_OF_WEEK: {
			return cached.getDay() + 1;
		}
		case DAY_OF_YEAR: {
			return (compareDate(new Date(cached.getYear(), 0, 1), cached) + 1);
		}
		case WEEK_OF_YEAR: {
			Date weekOne = getWeekOne(cached.getYear());
			if (weekOne.after(cached)) {
				weekOne = getWeekOne(cached.getYear() - 1);
			}
			int days = compareDate(weekOne, cached);
			int week = ((days / 7) + 1);
			if (week < 1) {
				return 1;
			} else if (week < 53) {
				return week;
			} else {
				Date weekOneNextYear = getWeekOne(cached.getYear() + 1);
				if (cached.before(weekOneNextYear)) {
					return 53;
				} else {
					return 1;
				}
			}
		}
		}
		return 0;
	}

	public void set(int field, int value) {
		switch (field) {
		case YEAR: {
			year = value;
			break;
		}
		case MONTH: {
			month = value;
			break;
		}
		case DAY_OF_MONTH: {
			day = value;
			break;
		}
		case HOUR: {
			hour = (hour < 12) ? value : (value + 12);
			break;
		}
		case HOUR_OF_DAY: {
			hour = value;
			break;
		}
		case AM_PM: {
			if ((value == AM) && (hour > 11)) hour -= 12;
			else if ((value == PM) && (hour < 12)) hour += 12;
			break;
		}		
		case MINUTE: {
			minute = value;
			break;
		}
		case SECOND: {
			second = value;
			break;
		}
		case DAY_OF_WEEK: {
			computeTime();
			if (value < getFirstDayOfWeek()) value += 7;
			int dayOfWeek = get(DAY_OF_WEEK);
			if (dayOfWeek < getFirstDayOfWeek()) dayOfWeek += 7;
			day += (value - dayOfWeek);
			computeTime();
			break;
		}
		case DAY_OF_YEAR: {
			computeTime();
			int dayOfYear = get(DAY_OF_YEAR);
			day += (value - dayOfYear);
			computeTime();
			break;
		}
		case WEEK_OF_YEAR: {
			computeTime();
			int currentWeekOfYear = get(WEEK_OF_YEAR);
			add(WEEK_OF_YEAR, (value - currentWeekOfYear));
			computeTime();
			break;
		}
		}
	}

	public void add(int field, int value) {
		switch (field) {
		case YEAR: {
			year += value;
			break;
		}
		case MONTH: {
			month += value;
			break;
		}
		case DAY_OF_MONTH: {
			day += value;
			break;
		}
		case HOUR:
		case HOUR_OF_DAY: {
			hour += value;
			break;
		}
		case MINUTE: {
			minute += value;
			break;
		}
		case SECOND: {
			second += value;
			break;
		}
		case DAY_OF_WEEK: {
			day += value;
			break;
		}
		case DAY_OF_YEAR: {
			day += value;
			break;
		}
		case WEEK_OF_YEAR: {
			day += (value * 7);
			break;
		}
		}
	}

	/**
	 * Calculate the number of days between two dates
	 * 
	 * @param a Date
	 * @param b Date
	 * @return the difference in days between b and a (b - a)
	 */
	private int compareDate(Date a, Date b) {
		final Date ta = new Date(a.getTime());
		final Date tb = new Date(b.getTime());
		final long d1 = setHourToZero(ta).getTime();
		final long d2 = setHourToZero(tb).getTime();
		return (int) Math.round((d2 - d1) / 1000.0 / 60.0 / 60.0 / 24.0);
	}

	/**
	 * Set hour, minutes, second and milliseconds to zero.
	 * 
	 * @param d Date
	 * @return Modified date
	 */
	@SuppressWarnings("deprecation")
	private Date setHourToZero(Date in) {
		final Date d = new Date(in.getTime());
		d.setHours(0);
		d.setMinutes(0);
		d.setSeconds(0);
		// a trick to set milliseconds to zero
		long t = d.getTime() / 1000;
		t = t * 1000;
		return new Date(t);
	}
	
	/**
	 * Gets the date of the first week for the specified year.
	 * @param year This is year - 1900 as returned by Date.getYear()
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private Date getWeekOne(int year) {
		GregorianCalendar weekOne = new GregorianCalendar();
		weekOne.setFirstDayOfWeek(getFirstDayOfWeek());
		weekOne.setMinimalDaysInFirstWeek(getMinimalDaysInFirstWeek());
		weekOne.setTime(new Date(year, 0, 1));
		// can we use the week of 1/1/year as week one?
		int dow = weekOne.get(DAY_OF_WEEK);
		if (dow < weekOne.getFirstDayOfWeek()) dow += 7;
		int eow = weekOne.getFirstDayOfWeek() + 7;
		if ((eow - dow) < weekOne.getMinimalDaysInFirstWeek()) {
			// nope, week one is the following week
			weekOne.add(DATE, 7);
		}
		weekOne.set(DAY_OF_WEEK, weekOne.getFirstDayOfWeek());
		return weekOne.getTime();
	}

}

