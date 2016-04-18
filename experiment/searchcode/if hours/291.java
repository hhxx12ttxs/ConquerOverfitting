package se.iths;

public class TimeConverter {
	
	public String getMeridiem(int hours) throws IllegalArgumentException{
		if(hours < 0 || hours > 23)
			throw new IllegalArgumentException();
		
		if (hours < 12)
			return "AM";
		return "PM";
	}

}

