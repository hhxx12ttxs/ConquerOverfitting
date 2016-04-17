package it.arpav.mobile.apparpav.utils;


public class Date {

	private String year=null;
	private String month=null;
	private String day=null;
	private String hours=null;
	private String minutes=null;
	
	Date(String _year, String _month, String _day, String _hours, String _minutes){
		year=_year;
		month=_month;
		day=_day;
		
		// delite the "0" character in hours "01", "02", "03", .. "09"
		if(_hours.startsWith("0"))
			hours=_hours.substring(1, 2);
		else
			hours=_hours;
		
		minutes=_minutes;
	}
	
	
	
	public String getYear(){ return year;}
	public String getMonth(){ return month;}
	public String getDay(){ return day;}
	public String getHours(){ return hours;}
	public String getMinutes(){ return minutes;}
	
}

