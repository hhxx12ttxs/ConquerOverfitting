package com.ctrmksw.nashobaschedule.ScheduleUtils.time;

/**
 * Created by Colin on 11/30/2014.
 */
public class Time
{
    private int hours, minutes;

    public Time (int hours, int minutes)
    {
        this.hours = hours;
        this.minutes = minutes;
    }

    public Time(String time)
    {
        String[] split = time.split(":");
        hours = Integer.parseInt(split[0]);
        minutes = Integer.parseInt(split[1]);
    }

    public int compareTo(Time t)
    {
        if(hours > t.hours)
            return 1;
        else if(hours < t.hours)
            return -1;
        else
        {
            if(minutes > t.minutes)
                return 1;
            else if(minutes < t.minutes)
                return -1;
            else
                return 0;
        }
    }

    public String get12HrString(boolean includeAmPm)
    {
        boolean am = true;
        int tempHours;
        if(hours == 0)
        {
            tempHours = 12;
        }
        else if(hours > 12)
        {
            tempHours = hours - 12;
            am = false;
        }
        else
        {
            tempHours = hours;
        }
        return tempHours + ":" + String.format("%02d", minutes) + ((includeAmPm) ? (" " + ((am)?"AM":"PM")) : "");
    }

    public String get24HrString()
    {
        return hours + ":" + String.format("%02d", minutes);
    }

    public int getMinutesUntil(Time otherTime)
    {
        int mins = (otherTime.getHours() - getHours()) * 60;
        mins += (otherTime.getMinutes() - getMinutes());
        return mins;
    }

    public Time clone()
    {
        return new Time(this.hours, this.minutes);
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }
}

