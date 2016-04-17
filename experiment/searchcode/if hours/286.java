package fr.nesqwik.vlcremote.bean;

public class Time {
	private int seconds;
	private int minutes;
	private int hours;
	
	public Time(int seconds, int minutes, int hours) {
		this.seconds = seconds;
		this.minutes = minutes;
		this.hours = hours;
	}
	
	public int getSeconds() {
		return seconds;
	}
	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}
	public int getMinutes() {
		return minutes;
	}
	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}
	public int getHours() {
		return hours;
	}
	public void setHours(int hours) {
		this.hours = hours;
	}
	
	public String toString() {
		String res = "";
		if(hours <= 0) {
			if(minutes <= 0) {
				return seconds + "";
			}
			return minutes + ":" + seconds;
		}
		return hours + ":" + minutes + ":" + seconds;
	}
	
	
}

