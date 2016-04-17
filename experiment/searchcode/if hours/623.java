
public class ParkingTime 
{
	private int hours;
	private int minutes;
	private String AMorPM;
	
	public ParkingTime()
	{
		hours = 0;
		minutes = 0;
		AMorPM = "AM";
	}
	
	public ParkingTime(int hours, int minutes, String AMorPM)
	{
		this.hours = hours;
		this.minutes = minutes;
		this.AMorPM = AMorPM;
	}
	
	public int getHours()
	{
		return hours;
	}
	
	public int getMinutes()
	{
		return minutes;
	}
	
	public void setHours(int hours)
	{
		this.hours = hours;
	}
	
	public void setMinutes(int minutes)
	{
		this.minutes = minutes;
	}
	public void setAMorPM(String AMorPM)
	{
		this.AMorPM = AMorPM;
	}
	
	public String getAMorPM()
	{
		return AMorPM;
	}
	
	public void addTime(double duration)
	{
		double hoursToAdd = duration/60;
		double doubleMinutesToAdd = hoursToAdd - (int)hoursToAdd;
		doubleMinutesToAdd = doubleMinutesToAdd * 60;
		int intMinutesToAdd = (int) doubleMinutesToAdd;
		
		
		if(intMinutesToAdd!=0)
		{
			minutes = minutes + intMinutesToAdd;
			if(minutes>=60)
			{
				hours++;
				minutes = 60-minutes;
			}
		}
		
		hours = hours + (int)hoursToAdd;
		
		while(hours>=12)
		{
			
			if(AMorPM.equalsIgnoreCase("AM")&&(hours>=11))
			{
				AMorPM = "PM";
			}
			else if(AMorPM.equalsIgnoreCase("PM"))
			{
				AMorPM = "AM";
			}
			hours = hours - 12;
			if(hours == 0)
			{
				hours = 12;
				return;
			}
		}
		
		
		
	}
	
	public void addTime(double duration, ParkingDate parkingDate)
	{
		double hoursToAdd = duration/60;
		double doubleMinutesToAdd = hoursToAdd - (int)hoursToAdd;
		doubleMinutesToAdd = doubleMinutesToAdd * 60;
		int intMinutesToAdd = (int) doubleMinutesToAdd;
		
		if(intMinutesToAdd!=0)
		{
			minutes = minutes + intMinutesToAdd;
			if(minutes>=60)
			{
				hours++;
				minutes = 60-minutes;
			}
		}
		
		
		hours = hours + (int)hoursToAdd;
		while(hours>=12)
		{
			hours = hours - 12;
			if(AMorPM.equalsIgnoreCase("AM")&&(hours>=12))
			{
				AMorPM = "PM";
			}
			else
			{
				AMorPM = "AM";
				parkingDate.setDay(parkingDate.getDay()+1);
			}
				
			if(hours == 0)
			{
				hours = 12;
			}
		}
		
		
	}
	
	public String toString()
	{
		String timeString;
		String tempHours = Integer.toString(hours);
		String tempMinutes = Integer.toString(minutes);
		if(minutes<10)
			timeString = tempHours+":"+"0"+tempMinutes+":00"+AMorPM;
		else
			timeString = tempHours+":"+tempMinutes+":00"+AMorPM;
		return timeString; 
	}
	
}

