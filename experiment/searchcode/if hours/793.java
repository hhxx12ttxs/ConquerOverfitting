package com.example.time;

import android.util.Log;

public class Time {
	public int hours;
	public int minutes;
	public String AM_PM;

	public Time (int hours, int minutes, String AM_PM)
	{
		this.hours = hours;
		this.minutes = minutes;
		this.AM_PM = AM_PM;
		if(this.hours > 12) {
			this.hours -=12;
		}
	}

	public String toString()
	{
		String time = String.format("%02d:%02d %s",hours,minutes,AM_PM);
		Log.e ("time_class",time);
		return time;
	}

	public int compare (Time t1,Time t2)
	{
		if (t2.AM_PM.trim().equals("AM")&& t1.AM_PM.trim().equals("PM"))
			return -1;
		else if (t2.AM_PM.trim().equals("PM")&& t1.AM_PM.trim().equals("AM"))
			return 1;
		else 
		{
			if (t1.hours==12 || t2.hours == 12)
			{
				if (t1.hours==12) return 1;
				if (t1.hours==12) return -1;
			}
			else
			{
			if (t1.hours > t2.hours)
				return -1;
			else if (t1.hours < t2.hours)
				return 1;
			else if (t1.minutes > t2.minutes)
				return -1;
			else if (t1.minutes < t2.minutes)
				return 1;
			else if (t1.minutes == t2.minutes && t1.hours == t2.hours && t1.AM_PM == t2.AM_PM)
				return 0;
			}
		}
		return 2;
	}

	public Time add (int minutes)
	{
		this.minutes += minutes;
		if (this.minutes >= 60)
		{
			this.minutes -= 60;
			this.hours += 1;
		}
		if (this.hours >12)
		{
			this.hours -=12;
			if (this.AM_PM == "AM")
				this.AM_PM = "PM";
			else this.AM_PM= "AM";

		}
		return this;
	}
}

