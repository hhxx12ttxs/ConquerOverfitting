package Chapter12;

public class Time {
	
	
	// Here are the Data Members:
	private int hours, minutes, seconds;
	
	
	
	
	
	
	// Here are the constructors:
    public Time (){
		hours = 0;
		minutes = 0;
		seconds = 0;
	}
	
    // Parameterised constructors:
	public Time(int h, int m, int s){
		
		if (h<0 || h>23)
			hours = 0;
		else
			hours = h;
		
		if(m<0 || m>59)
			minutes = 0;
		else
			minutes = m;
		
		if(s<0 || s>59)
			seconds = 0;
		else 
			seconds = s;
	}
	
	Time (int h, int m){
		
		if (h<0 || h>23)
			hours = 0;
		else
			hours = h;
		
		if(m<0 || m>59)
			minutes = 0;
		else
			minutes = m;
		
		seconds = 0;
	
	}
	
	Time (int h){
		
		if (h<0 || h>23)
			hours = 0;
		else
			hours = h;
			
		minutes = 0;
		seconds = 0;
	}
	
	
	
	
	// These down here are Methods!!!
	
	public String displayTime(){
		String hrs, mins, secs;
		
		if(hours < 10)
			hrs = "0" + hours;
		else
			hrs = hours + "";
		
		if(minutes < 10)
			mins = "0" + minutes;
		else
			mins = minutes + "";
		
		if(seconds < 10)
			secs = "0" + seconds;
		else
			secs = seconds + "";
		
		
		return hrs + ":" + mins + ":" + secs;
	}
	
	
	public String toString(){
		return "Hours:" + hours + "\nMinutes:" + minutes + "\nSeconds:" + seconds;
	}
	
	// Accessor Methods:
	// setMethods:
	public void setTime(int h, int m, int s){
		if (h<0 || h>23)
			hours = 0;
		else
			hours = h;
		
		if(m<0 || m>59)
			minutes = 0;
		else
			minutes = m;
		
		if(s<0 || s>59)
			seconds = 0;
		else 
			seconds = s;
	}
	
	
	public void setTime(int h, int m){
		if (h<0 || h>23)
			hours = 0;
		else
			hours = h;
		
		if(m<0 || m>59)
			minutes = 0;
		else
			minutes = m;

			seconds = 0;
	}
	
	
	public void setTime(int h){
		if (h<0 || h>23)
			hours = 0;
		else
			hours = h;

			minutes = 0;
			seconds = 0;
	}
	
	
	
	public void setHours(int h){
		if (h<0 || h>23)
			hours = 0;
		else
			hours = h;
	}
	
	
	public void setMinutes(int m){
		if(m<0 || m>59)
			minutes = 0;
		else
			minutes = m;
	}
	
	
	public void setSeconds(int s){
		if(s<0 || s>59)
			seconds = 0;
		else 
			seconds = s;
	}
	
	
	
	// getMethods:
	
	public int getHours(){
		return hours;
	}
	
	
	public int getMinutes(){
		return minutes;
	}
	
	public int getSeconds(){
		return seconds;
	}
	
}

