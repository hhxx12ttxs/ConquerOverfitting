package eu.future.earth.date;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class WeekCounter {

	@SuppressWarnings("deprecation")
	public static int getWeekOfYear(final Date date, int firstDayOfWeek, int minDaysOfFirstWeek) {
//		log("De dag is" , date);
		final GregorianCalendar tester = new GregorianCalendar();
		tester.setFirstDayOfWeek(firstDayOfWeek);
		tester.setTime(date);
		
		tester.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
		Date firstOfWeekForDate = tester.getTime();
//		log("First Day of week" , firstOfWeekForDate);
		tester.add(Calendar.DATE, 7);
		Date laatsteDagOfWeekForDate = tester.getTime();
//		log("Laatste Dag of week" , laatsteDagOfWeekForDate);
		if(laatsteDagOfWeekForDate.getYear() != firstOfWeekForDate.getYear()){
			final Date firstDayOfNextYear = getFirstDateOfYear(laatsteDagOfWeekForDate.getYear());
			int weekOneDuration = compareDate(firstOfWeekForDate, firstDayOfNextYear);
			if((7 - minDaysOfFirstWeek) >= weekOneDuration){
				return 1;
			}
		}
		if (firstOfWeekForDate.getYear() != date.getYear()) {
			return getWeekOfYearNoPrev(firstOfWeekForDate, firstDayOfWeek, minDaysOfFirstWeek);
		}
		
		final Date firstDayOfYear = getFirstDateOfYear(date.getYear());
		tester.setTime(firstDayOfYear);
		tester.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
		// tester.set(Calendar.DATE, 7);
		final Date startOfWeekForFirstDayInYear = tester.getTime();
//		log("Start of first day in week of year", startOfWeekForFirstDayInYear);

		// Check the first week
		// if its shorter that the min skip it.

		
		int count = 1;
		Date startOfWeekForFirstWeekInYear = startOfWeekForFirstDayInYear;
		if (startOfWeekForFirstDayInYear.getYear() != firstDayOfYear.getYear()) {
			tester.setTime(startOfWeekForFirstWeekInYear);
//			log("Start of First Week",  startOfWeekForFirstWeekInYear);
			int weekOneDuration = compareDate(startOfWeekForFirstWeekInYear, firstDayOfYear);
			if((minDaysOfFirstWeek) <= weekOneDuration){
				tester.add(Calendar.DATE, 7);	
				startOfWeekForFirstWeekInYear = tester.getTime();
			}
			
//			log("Start of First Week",  startOfWeekForFirstWeekInYear);
		}
//		log("Start of First Week",  startOfWeekForFirstWeekInYear);
//		if (weekOneDuration <= minDaysOfFirstWeek) {
//			count--;
//		}

		int days = compareDate(startOfWeekForFirstWeekInYear, firstOfWeekForDate);
		System.out.println(days);
		while (days >= 7) {
			days = days - 7;
			count++;
		}
		if (count == 54) {
			count = 1;
		}

		if (count == 0) {
			count = 53;
		}
		return count;
	}

	@SuppressWarnings("deprecation")
	private static Date getFirstDateOfYear(int year){
		final Date firstDayOfYear = new Date(year, Calendar.JANUARY, 1);
		return firstDayOfYear;
	}
	
	@SuppressWarnings("deprecation")
	private static int getWeekOfYearNoPrev(final Date date, int firstDayOfWeek, int minDaysOfFirstWeek) {
		final Date firstDayOfYear = getFirstDateOfYear(date.getYear());

		final GregorianCalendar tester = new GregorianCalendar();
		tester.setFirstDayOfWeek(firstDayOfWeek);
		tester.setTime(firstDayOfYear);
		tester.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);

		tester.setTime(firstDayOfYear);
		tester.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
		// tester.set(Calendar.DATE, 7);
		final Date startOfWeekForFirstDayInYear = tester.getTime();
//		log("Start of first day in week of year", startOfWeekForFirstDayInYear);

		// Check the first week
		// if its shorter that the min skip it.

		
		int count = 1;
		Date startOfWeekForFirstWeekInYear = startOfWeekForFirstDayInYear;
		if (startOfWeekForFirstDayInYear.getYear() != firstDayOfYear.getYear()) {
			tester.setTime(startOfWeekForFirstWeekInYear);
//			log("Start of First Week",  startOfWeekForFirstWeekInYear);
			int weekOneDuration = compareDate(startOfWeekForFirstWeekInYear, firstDayOfYear);
			if((minDaysOfFirstWeek) <= weekOneDuration){
				tester.add(Calendar.DATE, 7);	
				startOfWeekForFirstWeekInYear = tester.getTime();
			}
			
//			log("Start of First Week",  startOfWeekForFirstWeekInYear);
		}
//		log("Start of First Week",  startOfWeekForFirstWeekInYear);
//		if (weekOneDuration <= minDaysOfFirstWeek) {
//			count--;
//		}
		tester.setTime(date);
		tester.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
		Date firstOfWeekForDate = tester.getTime();
		int days = compareDate(startOfWeekForFirstWeekInYear, firstOfWeekForDate);
		System.out.println(days);
		while (days >= 7) {
			days = days - 7;
			count++;
		}
		if (count == 54) {
			count = 1;
		}

		if (count == 0) {
			count = 53;
		}
		return count;
	}

//	private static void log(String comment, Date theDate) {
//		SimpleDateFormat formatter = new SimpleDateFormat("EEEEE dd/MM/yyyy");
//		System.out.println(comment + " " + formatter.format(theDate));
//	}

	/**
	 * Set hour, minutes, second and milliseconds to zero.
	 * 
	 * @param d
	 *            Date
	 * @return Modified date
	 */
	@SuppressWarnings("deprecation")
	public static Date setHourToZero(Date in) {
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
	 * Calculate the number of days betwen two dates
	 * 
	 * @param a
	 *            Date
	 * @param b
	 *            Date
	 * @return the difference in days betwen b and a (b - a)
	 */
	public static int compareDate(Date a, Date b) {
		final Date ta = new Date(a.getTime());
		final Date tb = new Date(b.getTime());
		final long d1 = setHourToZero(ta).getTime();
		final long d2 = setHourToTen(tb).getTime();
		return (int) ((d2 - d1) / 1000 / 60 / 60 / 24);
	}

	
	
	/**
	 * Set hour to 10, minutes, second and milliseconds to zero.
	 * 
	 * @param d
	 *            Date
	 * @return Modified date
	 */
	@SuppressWarnings("deprecation")
	public static Date setHourToTen(Date in) {
		final Date d = new Date(in.getTime());
		d.setHours(10);
		d.setMinutes(0);
		d.setSeconds(0);
		// a trick to set milliseconds to zero
		long t = d.getTime() / 1000;
		t = t * 1000;
		return new Date(t);
	}

}

