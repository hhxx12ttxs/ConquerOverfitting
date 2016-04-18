package com.Agentleader1.DarkThunder.Managers;

public class TimeConversionManager {

	public static Object[] convert(double hours){
		if(hours >= 8640){
			double years = (hours / 8640D);
			String year = (years > 1d) ? "years" : "year";
			Object[] obj = {years, year};
			return obj;
		}else if(hours >= 720){
			double months = (hours / 720D);
			String month = (months > 1d) ? "months" : "month";
			Object[] obj = {months, month};
			return obj;
		}else if(hours >= 168){
			double weeks = (hours / 168D);
			String week = (weeks > 1d) ? "weeks" : "week";
			Object[] obj = {weeks, week};
			return obj;
		}else if(hours >= 24){
			double days = (hours / 24D);
			String day = (days > 1d) ? "days" : "day";
			Object[] obj = {days, day};
			return obj;
		}else{
			String hour = (hours > 1d) ? "hours" : "hour";
			Object[] obj = {hours, hour};
			return obj; 
		}
	}
}

