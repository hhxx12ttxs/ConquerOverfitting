/*
 * Pedersen, Jonathan
 * Pd. 2
 */
public class Clock {
	private int hours;
	private int minutes;
	
	public Clock() {
		hours = 12;
		minutes = 0;
	}//end constructor Clock()
	
	public Clock(int h, int m) {
		hours = h;
		minutes = m;
	}//end constructor Clock(int, int)
	
	public int getHours() {
		return hours;
	}//end method getHours()
	
	public void setHours(int h) {
		hours = h;
	}//end method setHours(int)
	
	public int getMinutes() {
		return minutes;
	}//end method getMinutes()
	
	public void setMinutes(int m) {
		minutes = m;
	}//end method setMinutes(int)
	
	public void incMinutes() {
		minutes++;
		if(minutes > 59) {
			minutes = 0;
			hours++;
			if(hours > 23)
				hours = 0;
		}//end if
	}//end method incMinutes()
	
	public void makeNoise() {
		System.out.println("Tick tock tick tock");
	}//end method makeNoise
	
	public String toString() {
		String s = "";
		if(hours<10)
			s = "0";
		s += hours + ":";
		if(minutes < 10)
			s += "0";
		s += minutes;
		return s;
	}//end method toString()
	
	public boolean equals(Object o) {
		Clock c = (Clock) o;
		return hours == c.getHours() && minutes == c.getMinutes();
	}//end method eqauls(Object)
	
}//end class Clock

