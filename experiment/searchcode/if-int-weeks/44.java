package com.adams.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.beanutils.BeanComparator;

public class DateUtil {

	// ?????? ???????????????

	private static String pattern = "yyyy-MM-dd";

	private static String time_pattern = "yyMMddHHmmss";

	/**
	 * ?????????????????
	 * 
	 * @param sdate
	 * @return
	 */
	public static String getWeek(String sdate) {
		// ??????
		Date date = DateUtil.strToDate(sdate);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return new SimpleDateFormat("EEEE").format(c.getTime());
	}

	/**
	 * @Description: ???string
	 * @param date
	 *            ??
	 * @param format
	 *            ????
	 * @return String
	 * @date Dec 4, 2009 10:06:16 AM
	 */
	public static String dateToString(Date date, String format) {

		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(date);
	}

	/**
	 * @Description: ???? yyyy-MM-dd??
	 * @param
	 * @return String
	 * @date Dec 28, 2009 9:07:42 PM
	 */
	public static String dateToString(Date date) {

		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		return formatter.format(date);
	}

	/**
	 * ?????????????? yyyy-MM-dd
	 * 
	 * @param strDate
	 * @return
	 */
	public static Date strToDate(String strDate) {
		return strToDate(strDate, pattern);
	}

	/**
	 * ????????????????
	 * 
	 * @author hub
	 * @create 2009-4-21 ??09:34:47
	 * @param strDate
	 * @param format
	 * @return
	 */
	public static Date strToDate(String strDate, String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}

	/**
	 * ?????????
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static long getDays(String date1, String date2) {
		if (date1 == null || date1.equals(""))
			return 0;
		if (date2 == null || date2.equals(""))
			return 0;
		// ???????
		SimpleDateFormat myFormatter = new SimpleDateFormat(pattern);
		java.util.Date date = null;
		java.util.Date mydate = null;
		try {
			date = myFormatter.parse(date1);
			mydate = myFormatter.parse(date2);
		} catch (Exception e) {
		}
		long day = (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);
		return day;
	}

	// ????????,?????
	public static Date getDefaultDay() {

		Calendar lastDate = Calendar.getInstance();
		lastDate.set(Calendar.DATE, 1);// ??????1?
		lastDate.add(Calendar.MONTH, 1);// ??????????1?
		lastDate.add(Calendar.DATE, -1);// ?????????????

		return lastDate.getTime();
	}

	// ?????
	public static Date getPreviousMonthFirst() {

		Calendar lastDate = Calendar.getInstance();
		lastDate.set(Calendar.DATE, 1);// ??????1?
		lastDate.add(Calendar.MONTH, -1);// ??????????1?

		return lastDate.getTime();
	}

	// ???????
	public static Date getFirstDayOfMonth() {

		Calendar lastDate = Calendar.getInstance();
		lastDate.set(Calendar.DATE, 1);// ??????1?

		return lastDate.getTime();
	}

	// ??????????
	public static Date getCurrentWeekday() {
		int mondayPlus = getMondayPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, mondayPlus + 6);
		Date monday = currentDate.getTime();

		return monday;
	}

	// ??????
	public static String getNowTime(String dateformat) {
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);// ???????????
		String hehe = dateFormat.format(now);
		return hehe;
	}

	// ???????????????
	public static int getMondayPlus() {
		Calendar cd = Calendar.getInstance();
		// ???????????????????????????......
		int dayOfWeek = cd.get(Calendar.DAY_OF_WEEK) - 1; // ??????????????????1
		if (dayOfWeek == 1) {
			return 0;
		} else {
			return 1 - dayOfWeek;
		}
	}

	// ????????
	public static Date getMondayOFWeek() {
		int mondayPlus = getMondayPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, mondayPlus);
		return currentDate.getTime();
	}

	// ???????????
	public static Date getSaturday() {
		int weeks = 0;
		int mondayPlus = getMondayPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 * weeks + 6);
		return currentDate.getTime();
	}

	// ??????????
	public static Date getPreviousWeekSunday() {
		int weeks = 0;
		weeks--;
		int mondayPlus = getMondayPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, mondayPlus + weeks);
		return currentDate.getTime();
	}

	// ??????????
	public static Date getPreviousWeekday() {
		int weeks = 0;
		weeks--;
		int mondayPlus = getMondayPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 * weeks);
		return currentDate.getTime();
	}

	// ??????????
	public static Date getNextMonday() {
		int mondayPlus = getMondayPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, mondayPlus + 7);
		return currentDate.getTime();
	}

	// ??????????
	public static Date getNextSunday() {
		int mondayPlus = getMondayPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 + 6);
		return currentDate.getTime();
	}

	public static int getMonthPlus() {
		Calendar cd = Calendar.getInstance();
		int monthOfNumber = cd.get(Calendar.DAY_OF_MONTH);
		cd.set(Calendar.DATE, 1);// ???????????
		cd.roll(Calendar.DATE, -1);// ??????????????
		int MaxDate = cd.get(Calendar.DATE);
		if (monthOfNumber == 1) {
			return -MaxDate;
		} else {
			return 1 - monthOfNumber;
		}
	}

	// ???????????
	public static Date getPreviousMonthEnd() {
		Calendar lastDate = Calendar.getInstance();
		lastDate.add(Calendar.MONTH, -1);// ????
		lastDate.set(Calendar.DATE, 1);// ???????????
		lastDate.roll(Calendar.DATE, -1);// ????????????????
		return lastDate.getTime();
	}

	// ???????????
	public static Date getNextMonthFirst() {
		Calendar lastDate = Calendar.getInstance();
		lastDate.add(Calendar.MONTH, 1);// ????
		lastDate.set(Calendar.DATE, 1);// ???????????
		return lastDate.getTime();
	}

	// ????????????
	public static Date getNextMonthEnd() {

		Calendar lastDate = Calendar.getInstance();
		lastDate.add(Calendar.MONTH, 1);// ????
		lastDate.set(Calendar.DATE, 1);// ???????????
		lastDate.roll(Calendar.DATE, -1);// ????????????????

		return lastDate.getTime();
	}

	// ??????????
	public static Date getNextYearFirst() {
		Calendar lastDate = Calendar.getInstance();
		lastDate.add(Calendar.YEAR, 1);// ???
		lastDate.set(Calendar.MONTH, 0);// ???????????
		lastDate.set(Calendar.DATE, 1);// ???????????

		return lastDate.getTime();
	}

	public static int getYearPlus() {
		Calendar cd = Calendar.getInstance();
		int yearOfNumber = cd.get(Calendar.DAY_OF_YEAR);// ????????????
		cd.set(Calendar.DAY_OF_YEAR, 1);// ??????????
		cd.roll(Calendar.DAY_OF_YEAR, -1);// ????????
		int MaxYear = cd.get(Calendar.DAY_OF_YEAR);
		if (yearOfNumber == 1) {
			return -MaxYear;
		} else {
			return 1 - yearOfNumber;
		}
	}

	// ??????????
	public static Date getCurrentYearFirst() {
		int yearPlus = getYearPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, yearPlus);
		return currentDate.getTime();
	}

	// ??????????? *
	public static String getCurrentYearEnd() {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");// ???????????
		String years = dateFormat.format(date);
		return years + "-12-31";
	}

	// ?????????? *
	public static String getPreviousYearFirst() {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");// ???????????
		String years = dateFormat.format(date);
		int years_value = Integer.parseInt(years);
		years_value--;
		return years_value + "-1-1";
	}

	// ???????????
	public static String getPreviousYearEnd() {
		int weeks = 0, MaxYear = 0;
		weeks--;
		int yearPlus = getYearPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, yearPlus + MaxYear * weeks
				+ (MaxYear - 1));
		Date yearDay = currentDate.getTime();
		DateFormat df = DateFormat.getDateInstance();
		String preYearDay = df.format(yearDay);
		getThisSeasonTime(11);
		return preYearDay;
	}

	// ?????
	public static String getThisSeasonTime(int month) {
		int array[][] = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 }, { 10, 11, 12 } };
		int season = 1;
		if (month >= 1 && month <= 3) {
			season = 1;
		}
		if (month >= 4 && month <= 6) {
			season = 2;
		}
		if (month >= 7 && month <= 9) {
			season = 3;
		}
		if (month >= 10 && month <= 12) {
			season = 4;
		}
		int start_month = array[season - 1][0];
		int end_month = array[season - 1][2];

		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");// ???????????
		String years = dateFormat.format(date);
		int years_value = Integer.parseInt(years);

		int start_days = 1;// years+"-"+String.valueOf(start_month)+"-1";//getLastDayOfMonth(years_value,start_month);
		int end_days = getLastDayOfMonth(years_value, end_month);
		String seasonDate = years_value + "-" + start_month + "-" + start_days
				+ ";" + years_value + "-" + end_month + "-" + end_days;
		return seasonDate;

	}

	/**
	 * ???????????
	 * 
	 * @param year
	 *            ?
	 * @param month
	 *            ?
	 * @return ????
	 */
	public static int getLastDayOfMonth(int year, int month) {
		if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8
				|| month == 10 || month == 12) {
			return 31;
		}
		if (month == 4 || month == 6 || month == 9 || month == 11) {
			return 30;
		}
		if (month == 2) {
			if (isLeapYear(year)) {
				return 29;
			} else {
				return 28;
			}
		}
		return 0;
	}

	/**
	 * ????
	 * 
	 * @param year
	 *            ?
	 * @return
	 */
	public static boolean isLeapYear(int year) {
		return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
	}

	/**
	 * @Description: ??????
	 * @param
	 * @return String
	 * @date Nov 30, 2009 2:11:38 PM
	 */
	public static Date lastDay(Date today) {
		return nextNDay(today, -1);
	}

	/**
	 * @Description: ??????
	 * @param
	 * @return String
	 * @date Nov 30, 2009 2:11:57 PM
	 */
	public static Date nextDay(Date today) {
		return nextNDay(today, 1);
	}

	/**
	 * @Description: ??n????
	 * @param period
	 *            (n ?)
	 * @return String
	 * @date Nov 30, 2009 2:11:57 PM
	 */
	public static Date nextNDay(Date today, int period) {

		long millSecond = 3600000 * 24;
		long lastDayLong = today.getTime() + period * millSecond;
		Date nextDay = new Date(lastDayLong);

		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		ParsePosition pos = new ParsePosition(0);
		String str = formatter.format(nextDay);
		return formatter.parse(str, pos);
	}

	public static Date stringToDate(String timeStr, String pattern) {
		if (timeStr == null || timeStr.equals("")) {
			return null;
		}
		Date date = new Date();
		SimpleDateFormat apf = new SimpleDateFormat(pattern);
		try {
			date = apf.parse(timeStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;

	}

	public static boolean isToday(String today, String dateformat) {
		if (today == null)
			today = "";
		Date date = new Date();
		String str = (new SimpleDateFormat(dateformat)).format(date);
		if (today.equals(str)) {
			return true;
		}
		return false;
	}

	/**
	 * @Description: string???date??????
	 * @return boolean 1????,0????,-1????
	 * @date Dec 4, 2009 10:11:40 AM
	 */
	public static int stringCompareToDate(String strDate, Date date) {

		String str = dateToString(date, time_pattern);
		BeanComparator beanComparator = new BeanComparator();
		return beanComparator.compare(strDate, str);

	}

	/**
	 * @Description: ????????
	 * @param day
	 *            ??
	 * @return Date
	 * @date Dec 5, 2009 7:39:13 AM
	 */
	public static Date getDateBefore(int day) {
		Calendar now = Calendar.getInstance();
		now.set(Calendar.DATE, now.get(Calendar.DATE) - day);
		return now.getTime();
	}

	/**
	 * @Description: ????????
	 * @param day
	 *            ??
	 * @return Date
	 * @date Dec 5, 2009 7:40:41 AM
	 */
	public static Date getDateAfter(int day) {
		Calendar now = Calendar.getInstance();
		now.set(Calendar.DATE, now.get(Calendar.DATE) + day);
		return now.getTime();
	}

	public static Date getCurrentDate() {
		Calendar cal = Calendar.getInstance();
		Date currDate = cal.getTime();
		return currDate;
	}

	/**
	 * java.util.Date ????? XMLGregorianCalendar
	 * 
	 * @param date
	 * @return XMLGregorianCalendar
	 * @throws DatatypeConfigurationException
	 */
	public static XMLGregorianCalendar convertToXMLGregorianCalendar(Date date)
			throws DatatypeConfigurationException {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		XMLGregorianCalendar gc = null;
		try {
			gc = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
		} catch (DatatypeConfigurationException e) {
			throw e;
		}
		return gc;
	}

	/**
	 * XMLGregorianCalendar ????? java.util.Date
	 * 
	 * @param XMLGregorianCalendar
	 * @return Date
	 */
	public static Date convertToDate(XMLGregorianCalendar xmlCalendar) {

		if (null != xmlCalendar) {
			Calendar c = xmlCalendar.toGregorianCalendar();
			Date d = c.getTime();
			return d;
		} else {
			return null;
		}

	}
}

