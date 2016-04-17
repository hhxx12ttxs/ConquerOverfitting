package com.telus.training.ex1;

public class ClockNeedles {

	public double calculateAngleBetween(int hours, int minutes) {
		double hoursAngle = convertHoursToAngle(hours);
		double minutesAngle = convertMinutesToAngle(minutes);
		
		double angle = Math.abs(hoursAngle - minutesAngle);
		return (angle > 180.0) ? (angle - 180.0) : angle;
	}

	private int convertMinutesToAngle(int minutes) {
		if (minutes < 0 || minutes > 60) {
			throw new IllegalArgumentException("Minutes must be between 0 and 60");
		}
		return minutes*6;
	}

	private int convertHoursToAngle(int hours) {
		if(hours < 0 || hours > 12) {
			throw new IllegalArgumentException("Hours must be between 0 and 12");
		}
		return hours*30;
	}
}

