/**
 * 
 */
package com.frontapp.openhours.model;

import java.util.ArrayList;
import java.util.List;

import com.frontapp.openhours.model.utils.DaysOfWeekEnum;


/**
 * @author Alexandre LACROUX
 *
 * Open hours of the week when the team can reply to customers' requests
 */
public class OpenHours {

	private List<OpenHoursDay> openHours = new ArrayList<OpenHoursDay>();

	/**
	 * Returns the open hours of a day
	 * @param day The day from 0 (Sunday) to 6 (saturday)
	 * @return The open hours of a day
	 */
	public OpenHoursDay getOpenHourDay(Integer day) {
		for(OpenHoursDay openHoursDay : openHours) {
			if(day.equals(DaysOfWeekEnum.valueOf(openHoursDay.getDay()).getValue()))
				return openHoursDay;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String toString = "Open Hours : \n";
		for(OpenHoursDay openHoursDay : getOpenHours()) {
			toString += openHoursDay.toString();
			toString += "\n";
		}
		return toString;
	}

	// GETTERS & SETTERS
	
	/**
	 * @return the openHours
	 */
	public List<OpenHoursDay> getOpenHours() {
		return openHours;
	}

	/**
	 * @param openHours the openHours to set
	 */
	public void setOpenHours(List<OpenHoursDay> openHours) {
		this.openHours = openHours;
	}
	
}

