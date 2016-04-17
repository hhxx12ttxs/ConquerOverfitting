package com.herguan.cs562.model;

import java.io.Serializable;

public class TimesheetAllocationVO implements Serializable{

	private String projectDisplay;

	private TimesheetStatusEnum timesheet_status;

	// concatenation of year and week number
	private String weekId;

	private String sundayHours;
	private String mondayHours;
	private String tuesdayHours;
	private String wednesdayHours;
	private String thursdayHours;
	private String fridayHours;
	private String saturdayHours;
	private String totalWeekHours;

	private String projectName;

	public String getProjectDisplay() {
		return projectDisplay;
	}

	public void setProjectDisplay(String projectDisplay) {
		this.projectDisplay = projectDisplay;
	}

	public TimesheetStatusEnum getTimesheet_status() {
		return timesheet_status;
	}

	public void setTimesheet_status(TimesheetStatusEnum timesheetStatus) {
		timesheet_status = timesheetStatus;
	}

	public String getWeekId() {
		return weekId;
	}

	public void setWeekId(String weekId) {
		this.weekId = weekId;
	}

	public String getSundayHours() {
		return sundayHours;
	}

	public void setSundayHours(String sundayHours) {
		if (!sundayHours.trim().equals("")) {
			this.sundayHours = sundayHours;
		}
	}

	public String getMondayHours() {
		return mondayHours;
	}

	public void setMondayHours(String mondayHours) {
		if (!mondayHours.trim().equals("")) {
			this.mondayHours = mondayHours;
		}
	}

	public String getTuesdayHours() {
		return tuesdayHours;
	}

	public void setTuesdayHours(String tuesdayHours) {
		if (!tuesdayHours.trim().equals("")) {
			this.tuesdayHours = tuesdayHours;
		}
	}

	public String getWednesdayHours() {
		return wednesdayHours;
	}

	public void setWednesdayHours(String wednesdayHours) {
		if (!wednesdayHours.trim().equals("")) {
			this.wednesdayHours = wednesdayHours;
		}
	}

	public String getThursdayHours() {
		return thursdayHours;
	}

	public void setThursdayHours(String thursdayHours) {
		if (!thursdayHours.trim().equals("")) {
			this.thursdayHours = thursdayHours;
		}
		
	}

	public String getFridayHours() {
		return fridayHours;
	}

	public void setFridayHours(String fridayHours) {
		if (!fridayHours.trim().equals("")) {
			this.fridayHours = fridayHours;
		}

	}

	public String getSaturdayHours() {
		return saturdayHours;
	}

	public void setSaturdayHours(String saturdayHours) {
		if (!saturdayHours.trim().equals("")) {
			this.saturdayHours = saturdayHours;
		}
	}

	public String getTotalWeekHours() {
		return totalWeekHours;
	}

	public void setTotalWeekHours(String totalWeekHours) {
		if (!totalWeekHours.trim().equals("")) {
			this.totalWeekHours = totalWeekHours;
		}
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

}

