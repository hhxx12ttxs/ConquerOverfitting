package com.listic.smarthome.model;


/**
 * The Schedule class if used to represent a schedule
 * @author Vincent Chenal
 *
 */
public class Schedule{
	private int hours;
	private int minutes;

	public Schedule(){
		this.hours = 0;
		this.minutes = 0;
	}

	public Schedule(int hours,int minutes){
		this.hours = hours;
		this.minutes = minutes;
	}

	public Schedule(Schedule toCopy){
		this.hours = toCopy.getHours();
		this.minutes = toCopy.getMinutes();
	}

	/**
	 * @return
	 * 			a String which represent the Schedule as a clock
	 */
	public String getClockRepresentation(){
		if(this.minutes<10)
			return ""+this.hours+" : 0"+this.minutes;
		else
			return ""+this.hours+" : "+this.minutes;
	}

	////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////
	////							ACCESSORS							////
	////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////

	public int getHours() {
		return hours;
	}

	public void setHours(int startHours) {
		this.hours = startHours;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int startMinutes) {
		this.minutes = startMinutes;
	}

	/**
	 * @return
	 * 			The Json String representing this object
	 */
	public String toJson(){
		return "{\"hours\":"+this.hours+","+
				"\"minutes\":"+this.minutes+"}";
	}
} 

