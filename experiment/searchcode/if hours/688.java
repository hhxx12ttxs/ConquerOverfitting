package com.frontapp.openhours.model;

/**
 * @author Alexandre LACROUX
 *
 * Open hours of the day when the team can reply to customers' requests 
 */
public class OpenHoursDay {

	private String day;
	private boolean open;
	private int openHours;
	private int openMinutes;
	private int closeHours;
	private int closeMinutes;
	
	/**
	 * Constructor
	 */
	public OpenHoursDay(String day, boolean open, int openHours, int openMinutes, int closeHours,	int closeMinutes) {
		super();
		this.day = day;
		this.open = open;
		this.openHours = openHours;
		this.openMinutes = openMinutes;
		this.closeHours = closeHours;
		this.closeMinutes = closeMinutes;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if(isOpen()) 
			return String.format("%s : %02d:%02d to %02d:%02d", getDay() , getOpenHours(), getOpenMinutes(), getCloseHours(), getCloseMinutes());
		return String.format("%s : %s", getDay() , "closed");
		
	}

	// GETTERS & SETTERS

	/**
	 * @return the day
	 */
	public String getDay() {
		return day;
	}

	/**
	 * @param day the day to set
	 */
	public void setDay(String day) {
		this.day = day;
	}
	
	/**
	 * @return the open
	 */
	public boolean isOpen() {
		return open;
	}

	/**
	 * @param open the open to set
	 */
	public void setOpen(boolean open) {
		this.open = open;
	}
	
	/**
	 * @return the openHours
	 */
	public int getOpenHours() {
		return openHours;
	}

	/**
	 * @param openHours the openHours to set
	 */
	public void setOpenHours(int openHours) {
		this.openHours = openHours;
	}

	/**
	 * @return the openMinutes
	 */
	public int getOpenMinutes() {
		return openMinutes;
	}

	/**
	 * @param openMinutes the openMinutes to set
	 */
	public void setOpenMinutes(int openMinutes) {
		this.openMinutes = openMinutes;
	}

	/**
	 * @return the closeHours
	 */
	public int getCloseHours() {
		return closeHours;
	}

	/**
	 * @param closeHours the closeHours to set
	 */
	public void setCloseHours(int closeHours) {
		this.closeHours = closeHours;
	}

	/**
	 * @return the closeMinutes
	 */
	public int getCloseMinutes() {
		return closeMinutes;
	}

	/**
	 * @param closeMinutes the closeMinutes to set
	 */
	public void setCloseMinutes(int closeMinutes) {
		this.closeMinutes = closeMinutes;
	}

}

