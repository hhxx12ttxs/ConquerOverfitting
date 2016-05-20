/*
 * Dates.java
 * 
 * Copyright (c) 2001-2002 Labfire, Inc. All rights reserved.
 * Visit http://labfire.com/ for more information. 
 * 
 * This software is the confidential and proprietary information of
 * Labfire, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Labfire.
 */


package com.labfire.fe.util;

/**
 * Dates
 * 
 * @author <a href="http://labfire.com/">Labfire, Inc.</a>
 */
public class Dates {
	public static final long SECONDS = 1000;
	public static final long MINUTES = SECONDS * 60;
	public static final long HOURS = MINUTES * 60;
	public static final long DAYS = HOURS * 24;
	public static final long YEARS = DAYS * 365;
	
	public static final int SECONDS_PER_YEAR = 60*60*24*365;
	
	public static String formatTime(long millis) {
		long remainder;
		long multiplier;
		String amount;
		if (millis < SECONDS) {
			return millis + " ms.";
		} else if (millis < MINUTES) {
			multiplier = millis/SECONDS;
			remainder = millis - (SECONDS * multiplier);
			if (multiplier != 1) {
				amount = " seconds, ";
			} else {
				amount = " second, ";
			}
			return multiplier + amount + formatTime(remainder);
		} else if (millis < HOURS) {
			multiplier = millis/MINUTES;
			remainder = millis - (MINUTES * multiplier);
			if (multiplier != 1) {
				amount = " minutes, ";
			} else {
				amount = " minute, ";
			}
			return multiplier + amount + formatTime(remainder);
		} else if (millis < DAYS) {
			multiplier = millis/HOURS;
			remainder = millis - (HOURS * multiplier);
			if (multiplier != 1) {
				amount = " hours, ";
			} else {
				amount = " hour, ";
			}
			return multiplier + amount + formatTime(remainder);
		} else if (millis < YEARS) {
			multiplier = millis/DAYS;
			remainder = millis - (DAYS * multiplier);
			if (multiplier != 1) {
				amount = " days, ";
			} else {
				amount = " day, ";
			}
			return multiplier + amount + formatTime(remainder);
		} else {
			multiplier = millis/YEARS;
			remainder = millis - (YEARS * multiplier);
			if (multiplier != 1) {
				amount = " years, ";
			} else {
				amount = " years, ";
			}
			return multiplier + amount + formatTime(remainder);
		}
	}
}
