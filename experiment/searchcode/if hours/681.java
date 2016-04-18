package org.coursera.androidcapstone.symptomapp.client.settings;

import java.util.Calendar;

public class CheckInAlarm  implements Comparable<Object> {


	private int hours;
	private int minutes;
	private int alarmCode;
	
	public CheckInAlarm() {
		super();
	}

	public CheckInAlarm(int hours, int minutes, int alarmCode) {
		super();
		this.hours = hours;
		this.minutes = minutes;
		this.alarmCode = alarmCode;
	}

	public int getHours() {
		return hours;
	}

	public void setHours(int hours) {
		this.hours = hours;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	public int getAlarmCode() {
		return alarmCode;
	}

	public void setAlarmCode(int alarmCode) {
		this.alarmCode = alarmCode;
	}
	
	public long getTimeStamp() { 
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, hours);
		cal.set(Calendar.MINUTE, minutes);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}
	
	@Override
	public String toString(){
		if(hours == 0)
			return String.format("%01d:%02d", 12, minutes)+"am";	
		else if(hours == 12)
			return String.format("%01d:%02d", 12, minutes)+"pm";	
		else if(hours > 12)
			return String.format("%01d:%02d", (hours-12), minutes)+"pm";	
		else
			return String.format("%01d:%02d", hours, minutes)+"am";	
	}
	
	@Override
    public int compareTo(Object o) {

		CheckInAlarm other = (CheckInAlarm) o;

        if (hours > other.hours) {
            return 1;
        }
        else if (hours == other.hours) {
            if (minutes > other.minutes) {
                return 1;
            }
            else if(minutes < other.minutes) {
            	return -1;
            }
            else {
                return 0;
            }
        }
        else if (hours < other.hours) {
            return -1;
        }
        else {
            return 0;
        }
    }
	
}

