package com.intexsoft.smarthouse.time;


public class Time {
	
	
	
	private int hours;
	
	private int minutes;
	
	private int seconds;
	
	

	public Time(int hours, int minutes, int seconds) {
		
		
		this.hours = hours;
		this.minutes = minutes;
		this.seconds = seconds;
	}
	
	

	public int getHours() {
		return hours;
	}

	public void setHours(int hours) {
		if ( hours < 0 || hours > 23 ){
			try {
				throw new TimeException("Hours bounds are incorrect");
			} catch (TimeException e) {
				e.printStackTrace();
			}
		}
		this.hours = hours;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		if (minutes < 0 || minutes > 59){
			try {
				throw new TimeException("Minutes bounds are incorrect");
			} catch (TimeException e) {
				e.printStackTrace();
			}
		}
		this.minutes = minutes;
	}

	public int getSeconds() {
		return seconds;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}
	
	@Override
	public String toString() {
		return hours + ":" + minutes + ":" + seconds;
	}
	
	public void addHour(){
		if (hours == 23){
			hours = 0;
		}
		else{
		hours += 1;
		}
		
	}
	
	public void addMinute(){
		if (minutes == 59){
			minutes = 0;
			addHour();
		}
		else{
		minutes += 1;
		}
		
	}
	
	public void addSecond(){
		if (seconds == 59){
			seconds = 0;
			addMinute();
		}
		else{
		
		seconds += 1;
		}
	}
	

}

