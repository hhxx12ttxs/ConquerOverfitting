package net.euler.problem019;

import net.euler.problem019.Problem.Months;
import net.euler.problem019.Problem.Weekdays;

/**
 * You are given the following information, but you may prefer to do some
 * research for yourself.
 * 
 * 1 Jan 1900 was a Monday. Thirty days has September, April, June and November.
 * All the rest have thirty-one, Saving February alone, Which has twenty-eight,
 * rain or shine. And on leap years, twenty-nine. A leap year occurs on any year
 * evenly divisible by 4, but not on a century unless it is divisible by 400.
 * How many Sundays fell on the first of the month during the twentieth century
 * (1 Jan 1901 to 31 Dec 2000)?
 */
public class Problem {

	enum Months {
		JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER
	}

	enum Weekdays {
		MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
	}

	public static void main(String[] args) {
		System.out.println(countFirstSundays(1901, 2000));
	}

	static int countFirstSundays(int firstYear, int lastYear) {
		Month currMonth = new Month(Months.JANUARY, Weekdays.TUESDAY, firstYear);
		int count = 0;

		while (currMonth.year <= lastYear) {
			if (currMonth.isSundayFirst())
				count++;
			currMonth = currMonth.getNextMonth();
		}

		return count;
	}
}

class Month {

	Months month;
	Weekdays firstDay;
	int year;

	Month(Months month, Weekdays firstDay, int year) {
		this.month = month;
		this.firstDay = firstDay;
		this.year = year;
	}

	Month getNextMonth() {
		Months month;
		int days;
		switch (this.month) {
		case JANUARY:
			month = Months.FEBRUARY;
			days = 31;
			break;
		case FEBRUARY:
			month = Months.MARCH;
			days = isLeapYear(this.year) ? 29 : 28;
			break;
		case MARCH:
			month = Months.APRIL;
			days = 31;
			break;
		case APRIL:
			month = Months.MAY;
			days = 30;
			break;
		case MAY:
			month = Months.JUNE;
			days = 31;
			break;
		case JUNE:
			month = Months.JULY;
			days = 30;
			break;
		case JULY:
			month = Months.AUGUST;
			days = 31;
			break;
		case AUGUST:
			month = Months.SEPTEMBER;
			days = 31;
			break;
		case SEPTEMBER:
			month = Months.OCTOBER;
			days = 30;
			break;
		case OCTOBER:
			month = Months.NOVEMBER;
			days = 31;
			break;
		case NOVEMBER:
			month = Months.DECEMBER;
			days = 30;
			break;
		case DECEMBER:
			month = Months.JANUARY;
			days = 31;
			break;
		default:
			month = null;
			days = 0;
		}

		Weekdays firstDay;			
		firstDay = Weekdays.values()[(this.firstDay.ordinal() + days) % 7];

		int year = month == Months.JANUARY ? this.year + 1 : this.year;
		return new Month(month, firstDay, year);
	}

	boolean isSundayFirst() {
		return firstDay == Weekdays.SUNDAY;
	}

	private boolean isLeapYear(int year) {
		if (year % 4 != 0)
			return false;
		if (year % 100 != 0)
			return true;
		if (year % 400 != 0)
			return false;
		return true;
	}
}

