package com.homeCenter.common;

public class Time {
	protected int hours = 0;
	protected int minutes = 0;
	protected int seconds = 0;
	
	public Time() {
		
	}
	
	public Time(int hours, int minutes) {
		this.hours = hours;
		this.minutes = minutes;
	}
	
	public Time(String str) {
		String[] parts = str.split(":");
		
		if(parts.length == 2) {
			this.hours = Integer.valueOf(parts[0]);
			this.minutes = Integer.valueOf(parts[1]);
		} else if(parts.length == 3) {
			this.hours = Integer.valueOf(parts[0]);
			this.minutes = Integer.valueOf(parts[1]);
			this.seconds = Integer.valueOf(parts[2]);
		}
	}
	
	public int getHours() {
		return this.hours;
	}
	
	public void setHours(int hours) {
		this.hours = hours;
	}
	
	public int getMinutes() {
		return this.minutes;
	}
	
	public void setMInutes(int minutes) {
		this.minutes = minutes;
	}
	
	public int getSeconds() {
		return this.seconds;
	}
	
	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}
	
	public boolean equal(Time time) {
		return (this.getHours() == time.getHours() && this.getMinutes() == time.getMinutes() && this.getSeconds() == time.getSeconds());
	}
	
	@Override
	public String toString() {
		return this.hours + ":" + this.minutes + ":" + this.seconds;
	}
}

