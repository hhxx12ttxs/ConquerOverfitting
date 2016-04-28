package com.pixplit.android.util.time;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.pixplit.android.PixApp;
import com.pixplit.android.R;
import com.pixplit.android.util.Utils;

public class PixDateFormat extends SimpleDateFormat{
	private static PixDateFormat instance = null;
	
	public static PixDateFormat getPixDateInstance() {
		if (instance == null) {
			instance = new PixDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZ");
		}
		return instance;
	}
	
	private PixDateFormat(String string) {
		super(string);
	}

	public String getTimestamp() {
		String timestamp = "";
		if (instance != null) {
			timestamp = instance.format(new Date());
		}
		return timestamp;
	}
	
	public String humanFormat(Date date) {
		int minutes;
		int hours;
		int days;
		int months;
		int years;
				
		if (date != null) {
			long diffInMs = Utils.getTimeInMillis() - date.getTime();
	
			int diffInSeconds = (int)TimeUnit.MILLISECONDS.toSeconds(diffInMs);
			
			String humanFormat = null;
			
		    minutes = diffInSeconds/60;
		    
		    if (minutes <= 0) {
		    	humanFormat = PixApp.getContext().getString(R.string.just_now);
		    	return humanFormat;
		    } 
		    if (minutes > 0 && minutes < 60) {
		    	if (minutes == 1) {
		    		humanFormat = PixApp.getContext().getString(R.string._1_minute_ago);
		    	} else {
		    		humanFormat = String.format(PixApp.getContext().getString(R.string._d_minutes_ago), minutes);
		    	}
		    	return humanFormat;
		    } 
		    
	    	// hours
	    	hours = minutes / 60;
		    if (hours > 0 && hours < 24) {
		    	if (hours == 1) {
		    		humanFormat = PixApp.getContext().getString(R.string.about_an_hour_ago);
		    	} else {
		    		humanFormat = String.format(PixApp.getContext().getString(R.string._d_hours_ago), hours);
		    	}
		    	return humanFormat;
		    }

    		// days
    		days = hours / 24;
    		if (days > 0 && days < 30) {
    			if (days == 1) {
        			humanFormat = PixApp.getContext().getString(R.string.about_a_day_ago);
        		} else {
        			humanFormat = String.format(PixApp.getContext().getString(R.string._d_days_ago), days);
        		}
    			return humanFormat;
		    }
    		
			// months
			months = days / 30;
			if (months > 0 && months < 12) {
				if (months == 1) {
	    			humanFormat = PixApp.getContext().getString(R.string.about_a_month_ago);
				} else {
					humanFormat = String.format(PixApp.getContext().getString(R.string._d_months_ago), months);
				}
				return humanFormat;
			}
			
			// years
			years = months / 12;
			if (years == 1) {
				humanFormat = PixApp.getContext().getString(R.string.about_a_year_ago);
			} else {
				if (years > 0 && years < 3) {
					humanFormat = String.format(PixApp.getContext().getString(R.string._d_years_ago), years);
				} else {
					// sometime server calculate time inaccurate for 'just now' time.
					humanFormat = PixApp.getContext().getString(R.string.just_now);
				}
			}
			return humanFormat;
		}
		return null;
	}
	
}

