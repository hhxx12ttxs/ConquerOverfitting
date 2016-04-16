package com.jitcaforwin.extended.api.track;


/**
 * This class represents the duration of a track, a collection of tracks, or a
 * playlist.
 * 
 * @author Niklas Albers
 * 
 */
public class Duration {
	private final static String SEPARATOR = ":";
	private final static float SECONDS_IN_MINUTE = 60.0f;
	private final static float MINUTES_IN_HOUR = 60.0f;
	private final static float HOURS_IN_DAY = 24.0f;
	private final static float SECONDS_IN_HOUR = SECONDS_IN_MINUTE * MINUTES_IN_HOUR;
	private final static float SECONDS_IN_DAY = SECONDS_IN_HOUR * HOURS_IN_DAY;
	
	private final long seconds;

	/**
	 * Standard constructor.
	 * 
	 * @param seconds
	 *            Duration of the track(s)/playlist in seconds.
	 */
	public Duration(long seconds) {
		this.seconds = seconds;
	}

	/**
	 * Returns the duration as string, like D:HH:MM:SS. 
	 * @return Duration as string, like D:HH:MM:SS. 
	 */
	@Override
	public String toString() {
		int days = (int) calculateDays(this.seconds);
		int hours = (int) calculateHours(this.seconds, days);
		int minutes = (int) calculateMinutes(this.seconds, days, hours);
		int seconds = calculateSeconds(this.seconds, days, hours, minutes);
		return getStringRepresentation(days, hours, minutes, seconds);
	}
	
	/**
	 * Returns the number of days as float with one decimal place.
	 * @return The number of days as float with one decimal place.
	 */
	public float getDays(){
		return round(calculateDays(this.seconds), 1);
	}
	
	/**
	 * Returns the number of hours as float with one decimal place.
	 * @return The number of hours as float with one decimal place.
	 */
	public float getHours(){
		return round(calculateHours(this.seconds, 0), 1);
	}
	
	/**
	 * Returns the number of minutes as float with two decimal places.
	 * @return The number of minutes as float with two decimal places.
	 */
	public float getMinutes(){
		return round(calculateMinutes(this.seconds, 0, 0), 2);
	}
	
	private static float round(float f, int n){
		return Math.round(f * n * 10.0f) / (n * 10.0f);
	}
	
	private static float calculateDays(long seconds){
		return (float) (seconds / SECONDS_IN_DAY);
	}
	
	private static float calculateHours(long seconds, int days){
		return (float) ((seconds - (days * SECONDS_IN_DAY)) / SECONDS_IN_HOUR);
	}
	
	private static float calculateMinutes(long seconds, int days, int hours){
		return (float) ((seconds - (days * SECONDS_IN_DAY + hours * SECONDS_IN_HOUR)) / SECONDS_IN_MINUTE);
	}
	
	private static int calculateSeconds(long seconds, int days, int hours, int minutes){
		return (int) (seconds - (days * SECONDS_IN_DAY + hours * SECONDS_IN_HOUR + minutes * SECONDS_IN_MINUTE));
	}

	private static String getStringRepresentation(int days, int hours, int minutes,
			int seconds) {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append(days);
		strBuffer.append(SEPARATOR);
		if (hours < 10)
			strBuffer.append(0);
		strBuffer.append(hours);
		strBuffer.append(SEPARATOR);
		if (minutes < 10)
			strBuffer.append(0);
		strBuffer.append(minutes);
		strBuffer.append(SEPARATOR);
		if (seconds < 10)
			strBuffer.append(0);
		strBuffer.append(seconds);
		return strBuffer.toString();
	}

}

