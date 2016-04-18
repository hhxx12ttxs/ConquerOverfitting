public class Time
{
	public int hours;
	public int minutes;
	
	public Time (int hours, int minutes)
	{
		this.hours=hours;
		this.minutes=minutes;
	}
	
	//returns -1 if time1 is earlier than time2, 1 if time1 is later than time 2, and 0 if they are the same time
	public static int compare (Time time1, Time time2)
	{
		if(time1.hours<time2.hours)
			return -1;
		if(time2.hours>time1.hours)
			return 1;
		if(time1.minutes<time2.minutes)
			return -1;
		if(time1.minutes>time2.minutes)
			return 1;
		return 0;
	}
}

