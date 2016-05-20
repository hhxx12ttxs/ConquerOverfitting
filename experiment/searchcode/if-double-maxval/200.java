package cs424.windblows.application;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/** 
 * Created with an intention to define all the utiliy methods
 * @author Gaurav
 *
 */
public class Utils {
	
	
	/**
	 * Scales float value to appropriate level based on screen size.
	 * I hope jvm inlines this function.
	 * @param val
	 * @return scaled value
	 */
	public static final float scale(float val){
		return val * Constants.SCALE;
	}
	
	/**
	 * Returns true if the value if present between min and max values
	 * @param minVal
	 * @param maxVal
	 * @param val
	 * @return
	 */
	public static boolean isPresent(float minVal, float maxVal, float val){
		if(val < maxVal && val > minVal) return true;
		else return false;
	}
	
	/**
	 * Implementation from floatTable
	 * @param array
	 */
	public static void scrubQuotes(String[] array) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].length() > 2) {
				// remove quotes at start and end, if present
				if (array[i].startsWith("\"") && array[i].endsWith("\"")) {
					array[i] = array[i].substring(1, array[i].length() - 1);
				}
			}
			// make double quotes into single quotes
			array[i] = array[i].replaceAll("\"\"", "\"");
		}
	}
	
	/**
	 * Adds days to the given date
	 * @param date
	 * @param days
	 * @return
	 */
	public static Date addDays(Date date, int days){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
       
        return cal.getTime();
    }
	
	
	/**
	 * Returns date form of string in format mm/dd/yyyy
	 * which is stored in database
	 * @param date
	 * @return
	 */
	public static Date getDate(String date){
		Date d = null;
		try {
			d = new SimpleDateFormat("M/dd/yyyy", Locale.ENGLISH)
							.parse(date);
		} catch (ParseException e) {
			System.out.println("ParseException - tried to parse " + date + ". Handle this ASAP. Exit 0.");
			e.printStackTrace();
			System.exit(0);
		}
		return d;
	}
	
	/**
	 * this is exact reverse of getDate
	 * @param date
	 * @return
	 */
	public static String getFormattedDate(Date date){
		SimpleDateFormat df = new SimpleDateFormat("M/d/yyyy", Locale.ENGLISH);
		return df.format(date);
	}
	
	/**
	 * this is exact reverse of getDate
	 * @param date
	 * @return
	 */
	public static String getFormattedDateMonth(Date date){
		SimpleDateFormat df = new SimpleDateFormat("M/d", Locale.ENGLISH);
		return df.format(date);
	}
	
	/**
	 * Gets the serial number of the date starting from first
	 * @param date
	 * @return
	 */
	public static int getInt(Date min, Date max, Date date){
		int count = 1;
		for(Date d = min; !d.after(max); addDays(d, 1)){
			if(d.compareTo(date) == 0) return count;
		}
		return count;
	}
	
	public static Date getDate(Date min, Date max, int val){
		int count = 1;
		for(Date d = min; !d.after(max); d = addDays(d, 1), count++){
			if(count == val) return d;
		}
		return min;
	}
	
	public static String getProjectPath() {
		
		String dir = "";
		
		try {
			dir = new File(".").getCanonicalPath();
			
			if (dir.substring(dir.length() - 4, dir.length()).equalsIgnoreCase(File.separator +"lib")
					|| dir.substring(dir.length() - 4, dir.length()).equalsIgnoreCase(File.separator +"bin")){
				dir = dir.substring(0, dir.length() - 4);
			}
		} 
		catch (IOException e) {
			System.out.println("Utils.getProjectPath() " + e.getMessage());
		}
		
		return dir;
	}
}
