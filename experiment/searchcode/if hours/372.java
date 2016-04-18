package java_essential_training;

public class Timespan {
	private int hours;
	private int minutes;
	
	// Constructs a time span with the given interval.
	// pre: hours >= 0 && minutes >= 0
	public Timespan(){
		this(0,0);
	}
	public Timespan(int hours, int minutes){
		add(hours, minutes);
	}
	
	// Adds the given interval to this time span.
	// pre: hours >= 0 && minutes >= 0
	public void add(int hours, int minutes){
		if (hours < 0 || minutes <0){
			throw new IllegalArgumentException();
		}
		this.hours = 0; // this is like self in python
		this.minutes = 0;
		
		this.hours += hours;
		this.minutes += minutes;
		// converts each 60 minutes into one hour
		this.hours += this.minutes / 60;
		this.minutes = this.minutes % 60;
	}
	
	// returns a String for this time span, such as "6h 15m"
	public String toString() {
		return hours + "h " + minutes + "m";
	}
}

