//cbasurto: Hw 1 problem 2 defining time data 
import java.util.Calendar;

public class Time
{
	//fields
	private int hours;
	private int minutes;
	public static final Time NOON = new Time(12,0);

	//constructors
	public Time(){
		hours = 4;
		minutes = 9;	    
	}

	//standard constructor
	public Time (int h, int m){
		hours = (h+m/60)%24;
		minutes = m%60;
	}

	public Time(String tstring) {
		String[] strsplit = tstring.split(":");
		hours = Integer.parseInt(strsplit[0]);
		minutes = Integer.parseInt(strsplit[1]);
	}

	//getters
	public int getHours(){
		return hours;
	}
	public int getMinutes(){
		return minutes;	
	}
	public Time getNOON(){
		return NOON;
	}

	//mutator to hrs later
	public  int addHours(int hrs){
		hours = hours + hrs;
		hours = hours%24;
		return hours;
	}
	//toString method for 24 hour notation
	public String toString (){
		if (minutes <10)
			if (hours <12)
				return "" +hours+ ":0" + +minutes+  "AM";
			else
				return "" +hours+ ":" + +minutes+  "PM";
		else
			if (hours <12)
				return "" +hours+  ":" + +minutes+ "AM";
			else
				return "" +hours+  ":" + +minutes+ "PM";

	}

	//method for am/pm string
	public String amPM () {
		if (hours <12)
			return "AM";
		else
			return "PM";  
	}

	//boolean method for lessthan noon
	public boolean lessThan(Time t) {
		if(hours < t.getHours())
			return true;
		else if(hours > t.getHours())
			return false;
		
		if(minutes < t.getMinutes())
			return true;
		else if(minutes > t.getMinutes())
			return false;
		
		return false;
	}

	//boolean method for equal noon
	public boolean equals(Time t) {
		if(hours == t.getHours() && minutes == t.getMinutes())
			return true;
		else
			return false;
	}
}


