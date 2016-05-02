package greendroid.util;

import greendroid.app.GDApplication;

import java.util.Date;

import com.cyrilmottier.android.greendroid.R;

import android.util.Log;

/**
 * Various String utils
 * @author kennydude
 *
 */
public class StringUtils {
	
	/**
	 * Gets a resource by the Id
	 * @param resource Integer resource. Generally in the format R.string.x
	 * @return String
	 */
	public static String getResource(int resource){
		return GDApplication.getAppResources().getString(resource);
	}
	
	/**
	 * Parses a date to be be pretty like "x ago"
	 * Based on http://leaverou.me/2009/04/java-pretty-dates/ (@kennydude just added localization to it)
	 * @param input Date to parse
	 * @return Pretty Date
	 */
	public static String prettyDate(Date date){
		long	current = (new Date()).getTime(),
		timestamp = date.getTime(),
		diff = (current - timestamp)/1000;
		int	amount = 0;
		String what = "";
		/**
		 * Second counts
		 * 3600: hour
		 * 86400: day
		 * 604800: week
		 * 2592000: month
		 * 31536000: year
		 */
		if(diff > 31536000) {
			amount = (int)(diff/31536000);
			what = "year";
		}
		else if(diff > 31536000) {
			amount = (int)(diff/31536000);
			what = "month";
		}
		else if(diff > 604800) {
			amount = (int)(diff/604800);
			what = "week";
		}
		else if(diff > 86400) {
			amount = (int)(diff/86400);
			what = "day";
		}
		else if(diff > 3600) {
			amount = (int)(diff/3600);
			what = "hour";
		}
		else if(diff > 60) {
			amount = (int)(diff/60);
			what = "minute";
		}
		else {
			amount = (int)diff;
			what = "second";
			if(amount < 6) {
				return getResource(R.string.gd_pretty_now);
			}
		}

		if(amount == 1) {
			if(what.equals("day")) {
				return getResource(R.string.gd_pretty_yesterday);
			}
			else if(what.equals("week")){
				return getResource(R.string.gd_pretty_last_week);
			} else if( what.equals("month") ){
				return getResource(R.string.gd_pretty_last_month);
			} else if(what.equals("year")) {
				return getResource(R.string.gd_pretty_last_year);
			}
		}
		else {
			what += "s";
		}
		int res = 0;
		// Now select resource

		if(what.equals("seconds"))
			res = R.string.gd_pretty_seconds_ago;
		else if(what.equals("minute"))
			res = R.string.gd_pretty_minute_ago;
		else if(what.equals("minutes"))
			res = R.string.gd_pretty_minutes_ago;
		else if(what.equals("hour"))
			res = R.string.gd_pretty_hour_ago;
		else if(what.equals("hours"))
			res = R.string.gd_pretty_hours_ago;
		else if(what.equals("days"))
			res = R.string.gd_pretty_days_ago;
		else if(what.equals("months"))
			res = R.string.gd_pretty_months_ago;
		else if(what.equals("years"))
			res = R.string.gd_pretty_years_ago;
		else if(what.equals("weeks"))
			res = R.string.gd_pretty_weeks_ago;
		if(res == 0)
			return date.toLocaleString();
		return getResource(res).replace("{x}", ""+amount);
	}
}

