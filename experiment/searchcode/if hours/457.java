package com.uladzislau.madelaine;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Greeter {

	public Greeter() {
	}

	public String getGreeting() {
		Calendar cal = Calendar.getInstance();
		cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$
		String s = sdf.format(cal.getTime());
		String current_hour = s.substring(0, 2);
		int hours = Integer.parseInt(current_hour);
		if (hours >= 5 && hours <= 12) {
			return "Good morning."; //$NON-NLS-1$
		} else if (hours >= 12 && hours <= 13) {
			return "It is noon."; //$NON-NLS-1$
		} else if (hours >= 13 && hours <= 18) {
			return "Good afternoon."; //$NON-NLS-1$
		} else if (hours >= 18 && hours <= 20) {
			return "Good evening."; //$NON-NLS-1$
		} else if (hours > 20 && hours < 24) {
			return "It is night."; //$NON-NLS-1$
		} else if (hours == 24) {
			return "It is midnight."; //$NON-NLS-1$
		} else if (hours > 24 || hours < 5) {
			return "It is very late at night."; //$NON-NLS-1$
		}
		System.out.println(hours);
		return "Hello. Right now it is " + sdf.format(cal.getTime()); //$NON-NLS-1$
	}

}

