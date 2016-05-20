package com.zukov.QuickOrganizer.Calculators;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.Days;

import com.zukov.QuickOrganizer.State;


public class TimeCalculator {
	
	private int dayOfMonth;
	private int month;
	private int year;
	
	public static final int MINUTES_IN_HOUR = 60;
	
	
	private static final String[] monthNames = {"none", "January", "February", "March", "April", "May", 
			"June", "July", "August", "September", "October", "November", "December"};
	
	
	
	public TimeCalculator() {
		
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.get(Calendar.DAY_OF_MONTH);
		calendar.get(Calendar.MONTH);
		calendar.get(Calendar.YEAR);	
		
		if(calendar.getActualMaximum(Calendar.DAY_OF_YEAR) == 366) {
		}
	}
	
	
	
	public void calculateDate(int horizontalPosition){
				
		DateTime dateTime = new DateTime(State.getTodayYear(), State.getTodayMonth(), State.getTodayDayOfMonth(), 12 , 1);
		dateTime = dateTime.minusDays(horizontalPosition);
		dayOfMonth = dateTime.getDayOfMonth();
		month = dateTime.getMonthOfYear();
		year = dateTime.getYear();
		
	}
	
	
	public int calculatePosition(int dayOfMonth, int month, int year) {
		
		int zeroDayOfMonth = State.getTodayDayOfMonth();
		int zeroMonth = State.getTodayMonth();
		int zeroYear = State.getTodayYear();
		
		DateTime endDateTime = new DateTime(year, month, dayOfMonth, 12, 1);
		DateTime startDateTime = new DateTime(zeroYear, zeroMonth, zeroDayOfMonth, 12, 1);
		Days daysBetween = Days.daysBetween(startDateTime, endDateTime);
		
		return  - (daysBetween.getDays());
	}

		
	
	public static String addZeroInFrontIfNeeded(String date){
		
		if((date.length() == 1) && (date.charAt(0) >= '0' &&  date.charAt(0) <= '9'))
			date = "0" + date;
		
		return date;
	}
	
	/**
	 * Interprets integer month value and returns name of month in string
	 * 
	 * @param month integer to be converted
	 * @return month name in string
	 */
	public static String monthToString(int monthInt){
		return monthNames[monthInt];
	}
	
	public int getDayOfMonth() {
		return dayOfMonth;
	}

	public int getMonth() {
		return month;
	}

	public int getYear() {
		return year;
	}
}

