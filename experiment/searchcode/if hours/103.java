package ca.tonsaker.workschedu.employee;

import com.google.gson.annotations.Expose;

public class Week {
	
	@Expose public String DATE;
	
	@Expose public double TOTAL_HOURS_MONDAY;
	@Expose public double TOTAL_HOURS_TUESDAY;
	@Expose public double TOTAL_HOURS_WEDNESDAY;
	@Expose public double TOTAL_HOURS_THURSDAY;
	@Expose public double TOTAL_HOURS_FRIDAY;
	@Expose public double TOTAL_HOURS_SATURDAY;
	@Expose public double TOTAL_HOURS_SUNDAY;
	@Expose public double TOTAL_HOURS_WEEK;
	
	//From and To hours.  Example: 7:00am-3:30pm
	@Expose public String MONDAY_HOURS;
	@Expose public String TUESDAY_HOURS;
	@Expose public String WEDNESDAY_HOURS;
	@Expose public String THURSDAY_HOURS;
	@Expose public String FRIDAY_HOURS;
	@Expose public String SATURDAY_HOURS;
	@Expose public String SUNDAY_HOURS;

	public Week(String date){
		if(DATE == null){
			MONDAY_HOURS = "12:00pm-12:00pm";
			TUESDAY_HOURS = "12:00pm-12:00pm";
			WEDNESDAY_HOURS = "12:00pm-12:00pm";
			THURSDAY_HOURS = "12:00pm-12:00pm";
			FRIDAY_HOURS = "12:00pm-12:00pm";
			SATURDAY_HOURS = "12:00pm-12:00pm";
			SUNDAY_HOURS = "12:00pm-12:00pm";
			DATE = date;
		}
	}
}

