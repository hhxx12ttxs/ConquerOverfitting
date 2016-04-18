public class Clock {

	private int hours,minutes,seconds;
	private String name;

	public Clock()
	{
		hours=minutes=seconds=0;
		name="Local time zone";
	}

	public Clock(int hour,int minute,int second)
	{
		this.hours=hour % 24;
		this.minutes=minute % 60;
		this.seconds=second % 60;
		name="Local Time Zone";
		
	}
	public  Clock(Clock clock)
	{
		this.hours=clock.getHours();
		this.minutes=clock.getMinutes();
		this.seconds=clock.getSeconds();
		this.name=clock.getName();
	}

	public int getHours() 
	{
		return hours;
	}

	public void setHours(int hours)
	{
		this.hours = hours;
	}

	public int getMinutes()
	{
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	public int getSeconds()
	{
		return seconds;
	}

	public void setSeconds(int seconds)
	{
		this.seconds = seconds;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
	public String displayTime()
	{
		return name+"==>"+hours+":"+minutes+":"+seconds;
	}
	public void incrementHours(int hours)
	{
		this.hours+=hours;
		if (this.hours>=24)
		{
			this.hours=this.hours%24;
		}
		else if (this.hours<0)
		{
			this.hours+=24;
		}
	}
	public void incrementMinutes(int minutes )
	{
		this.minutes+=minutes;
		if (this.minutes>=60)
		{
			this.hours+=this.minutes/60;
			this.minutes=this.minutes%60;
	
			if (hours>=24)
			{
				this.hours=this.hours%24;
			}
			else if (this.hours<0)
			{
				this.hours+=24;	
			}
		}
		else if (this.minutes<0)
		{
			this.minutes+=this.minutes%24;
		}

	}
	public void incrementSeconds(int seconds)
	{
		this.seconds+=seconds;
		if (this.seconds>=60) 
		{
			
			this.minutes+=this.seconds/60;
			this.seconds=this.seconds%60;
			if (this.minutes>=60)
			{
				
				this.hours+=this.minutes/60;
				this.minutes=this.minutes%60;
				if (hours>=24)
				{
					this.hours=this.hours%24;
				}
				else if (this.hours<0)
				{
					this.hours+=24;	
				}
			}
		}
		else if (this.seconds<0)
		{
			this.seconds+=this.seconds%60;
		}
	}


}
