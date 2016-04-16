
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Utils {

	public static boolean validateFeedURL(String feedURL){
		
		URLReader urlreader;
		try {
			urlreader = new URLReader(new URL(feedURL));
			return true;
		} catch (MalformedURLException e) {
			return false;
		} catch (Exception e) {
			return false;
		}
	}
	

	public static String[] months = { "January", "February", "March", "April",
			"May", "June", "July", "August", "September", "October",
			"November", "December" };
	public static String[] weekdays = { "Saturday", "Sunday", "Monday",
			"Tuesday", "Wednesday", "Thursday", "Friday" };


	public static String getDate() {
		String tmp = "";
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		int dayOfMonth = c.get(Calendar.DAY_OF_MONTH) %31;
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) %7;
		String year = "" + c.get(Calendar.YEAR);
		int monthOfYear = c.get(Calendar.MONTH) %12;
		int century = Integer.parseInt("" + year.charAt(0) + year.charAt(1));
		int decade = Integer.parseInt("" + year.charAt(2) + year.charAt(3));

		tmp += weekdays[dayOfWeek] + " " + convertNumberToWords(dayOfMonth)
				+ " " + months[monthOfYear];
		if (year.charAt(2) == '0') {
			tmp += convertNumberToWords(century) + " oh "
					+ convertNumberToWords(decade);
		} else {
			tmp += convertNumberToWords(century) + convertNumberToWords(decade);
		}

		return tmp;

	}

	public static String getTime() {
		String number = "";
		Date d = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		int hours = c.get(Calendar.HOUR_OF_DAY);
		int mins = c.get(Calendar.MINUTE);
		if (mins < 10) {
			number = convertNumberToWords(hours) + " oh "
					+ convertNumberToWords(mins);
		} else {
			number = convertNumberToWords(hours) + " "
					+ convertNumberToWords(mins);
		}
		return number;

	}
	
	public static String stripMarkupTags(String markup){
		String tmp = markup;
		//System.out.println(tmp);
		while (tmp.contains("<")){
			int startLoc = tmp.indexOf("<");
			int endLoc = tmp.indexOf(">")+1;
			String substr = tmp.substring(startLoc, endLoc);
			tmp = tmp.replaceFirst(substr, " ");
			//System.out.println(tmp);
		}
		return tmp.trim();
		
		
	}
	

	public static String convertNumberToWords(int number) {
		BritishEnglish be = new BritishEnglish();
		return be.toWords(number);
	}

}

