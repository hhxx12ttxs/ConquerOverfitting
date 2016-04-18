package com.ds.code;

public class Clock {
	private int hours;
	private int minutes;

	public Clock (int i, int j) {
		if(i > 24){
			throw new IllegalArgumentException();
		}
		if(j > 59){
			throw new IllegalArgumentException();
		}
		this.hours = i;
		this.minutes = j;
	}
	
	public void addHours(int hours) {
		hours = hours % 24;
		this.hours += hours;
		if (this.hours > 23){
			this.hours -= 24;
		}
		/*
		this.hours += hours;
		while(this.hours > 24){
			this.hours -= 24;
		}
		*/
	}

	public void addMinutes(int minutes) {
		while(minutes >= 60){
			addHours(1);
			minutes -= 60;
		}
		this.minutes += minutes;
		if(this.minutes >= 60){
			addHours(1);
			this.minutes -= 60;
		}
	}
	
	public void subHours(int i) {
		this.hours = (this.hours - i) % 24;	
		if(this.hours < 0){
			this.hours = this.hours * (-1);
			}
		}

	@Override
	public String toString() {
		return hours + ":" + minutes;
	}
}

