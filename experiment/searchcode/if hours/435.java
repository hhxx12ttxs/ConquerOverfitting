package com.calsched.src;

public class Util {

	public static String getTimeString(int time) {
		String retString;
		
		if (time > (60 * 24)) {
			time -= (60 * 24);
		}
		int hours = time / 60;
		int mins  = time % 60;
		String ampm = "am";
		
		if (hours >= 12) {
			ampm = "pm";
			hours -= 12;
		}
		
		retString = ((hours < 10)? ("0" + hours):hours) + ":" + 
					((mins < 10)? ("0" + mins):mins) + " " + ampm;		
		return retString;
	}
}

