package edu.ycp.cs.cs496.collegeplanner.models;

/**
 * @author dholtzap
 * Time manipulation/parsing is a pain in the butt. Record a time as hours 
 * and minutes in 24-hour format. toString will properly format the 
 * time to 12-hour format.
 */
public class Time {
	
	private int hours;
	private int minutes;	
	
	public Time() {
		
	}
	
	public int getHours() {
		return hours;
	}
	
	public void setHours(int hours) {
		this.hours = hours;
	}
	
	public int getMinutes() {
		return minutes;
	}
	
	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}
	
	@Override
	public String toString() {
		int formatted_hour = getHours() % 12;
		if(this.getHours() > 12) {			
				return formatted_hour + ":" + getMinutes() + "pm";			
		}
		return formatted_hour + ":" + getMinutes() + "am";
	}
}

