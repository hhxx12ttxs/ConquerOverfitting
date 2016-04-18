package com.stark.assignments.Time;

public class Time 
{

    private int hours, minutes;
    private String mtimeString;
    
    public Time()
    {
        mtimeString = "1200";
        
        hours = 12;
        minutes = 0;
    }
    
    public Time(int h, int m)
    {
        if(h <= 23 && h >= 1)
            hours = h;
        else
            hours = 0;
        
        if(m >= 0 && m <= 59)
            minutes = m;
        else 
            minutes = 0;
        
    }
    
    public void increment()
    {
        minutes += 1;
        
        if(minutes > 59)
        {
            minutes = 00;
            
            hours += 1;
            
                if(hours > 23)
                {
                    hours = 00;
                }
        } 
        
    }
    
    public String convert()
    {
        int locHours = hours;
        String ap = null;
        
        if(locHours > 12)
        {
           locHours -= 12;
           ap = " pm";
        } else {
            ap = " am";
        }
        
        if(locHours < 1)
        {
            locHours = 12;
            ap = " am";
        }
        
        return locHours + ":" + (minutes < 10 ? "0" + minutes : minutes) + ap;
    }
    
    @Override
    public String toString()
    {
        if(hours >= 10)
        {
            mtimeString = Integer.toString(hours) + (minutes < 10 ? "0" + minutes : minutes);
        } else {
            mtimeString = "0" + Integer.toString(hours) +  (minutes < 10 ? "0" + minutes : minutes);
        }
        
        return mtimeString;
    }
}

