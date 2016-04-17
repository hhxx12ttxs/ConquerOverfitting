package com.sadik.teacher;

import android.util.Log;

public class TimeInMinutes {

	public static int getTime(String time) // HH:MM AM
	{
	
		int hours = Integer.parseInt(time.substring(0, 2));
		int minutes = Integer.parseInt(time.substring(3, 5));

		Log.i("Hour Minutes "+time.charAt(6), +hours+" : "+minutes);
		
		if(time.charAt(6) == 'A')
		{
			if(hours ==12)
			{
				hours = 0;
			}
		}
		else if(time.charAt(6) == 'P')
		{
			if(hours !=12)
			{
				hours += 12;
			}
		}

		Log.i("Hour Minutes", +hours+" : "+minutes);
		
		minutes = hours*60 + minutes;
		
		return minutes;
	}
}

