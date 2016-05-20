package com.jeasonzhao.commons.parser.expression.library;

import java.util.Date;

public class DateOperations
{
    public DateOperations()
    {
        super();
    }

    public String formatDate(Date date,String str)
    {
        if(null == date)
        {
            return null;
        }
        if(null == str)
        {
            return(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(date);
        }
        else
        {
            return(new java.text.SimpleDateFormat(str)).format(date);
        }
    }

    public String dateString(Date date)
    {
        return formatDate(date,"yyyy-MM-dd");
    }

    public String timeString(Date date)
    {
        return formatDate(date,"HH:mm:ss");
    }

    public int year()
    {
        return year(new Date());
    }

    public int year(Date date)
    {
        java.util.Calendar ca = java.util.Calendar.getInstance();
        ca.setTime(date);
        return ca.get(java.util.Calendar.YEAR);
    }

    public int month()
    {
        return month(new Date());
    }

    public int month(Date date)
    {
        java.util.Calendar ca = java.util.Calendar.getInstance();
        ca.setTime(date);
        return ca.get(java.util.Calendar.MONTH);
    }

    public Date firstMonthDate()
    {
        return firstMonthDate(null);
    }

    public Date firstYearDate()
    {
        return firstYearDate(null);
    }

    public Date firstYearDate(Date d)
    {
        if(d == null)
        {
            d = now();
        }
        java.util.Calendar ca = java.util.Calendar.getInstance();
        ca.setTime(d);
        ca.set(java.util.Calendar.DAY_OF_MONTH,1);
        ca.set(java.util.Calendar.MONTH,0);
        return ca.getTime();
    }

    public Date lastYearDate()
    {
        return lastYearDate(null);
    }

    public Date lastYearDate(Date d)
    {
        if(d == null)
        {
            d = now();
        }
        java.util.Calendar ca = java.util.Calendar.getInstance();
        ca.setTime(d);
        ca.set(java.util.Calendar.DAY_OF_MONTH,1);
        ca.set(java.util.Calendar.MONTH,12);
        ca.add(java.util.Calendar.DATE, -1);
        return ca.getTime();
    }

    public Date firstMonthDate(Date d)
    {
        if(d == null)
        {
            d = now();
        }
        java.util.Calendar ca = java.util.Calendar.getInstance();
        ca.setTime(d);
        ca.set(java.util.Calendar.DAY_OF_MONTH,1);
        return ca.getTime();
    }

    public Date lastMonthDate()
    {
        return lastMonthDate(null);
    }

    public Date lastMonthDate(Date d)
    {
        if(d == null)
        {
            d = now();
        }
        java.util.Calendar ca = java.util.Calendar.getInstance();
        ca.setTime(d);
        ca.add(java.util.Calendar.MONTH,1);
        ca.set(java.util.Calendar.DAY_OF_MONTH,1);
        ca.add(java.util.Calendar.DATE, -1);
        return ca.getTime();
    }

    public Date now()
    {
        return new Date();
    }

    public int date()
    {
        return date(new Date());
    }

    public int date(Date date)
    {
        java.util.Calendar ca = java.util.Calendar.getInstance();
        ca.setTime(date);
        return ca.get(java.util.Calendar.DATE);
    }

    public int hour()
    {
        return hour(new Date());
    }

    public int hour(Date date)
    {
        java.util.Calendar ca = java.util.Calendar.getInstance();
        ca.setTime(date);
        return ca.get(java.util.Calendar.HOUR);
    }

    public int minutes()
    {
        return minutes(new Date());
    }

    public int minutes(Date date)
    {
        java.util.Calendar ca = java.util.Calendar.getInstance();
        ca.setTime(date);
        return ca.get(java.util.Calendar.MINUTE);
    }

    public int seconds()
    {
        return seconds(new Date());
    }

    public int seconds(Date date)
    {
        java.util.Calendar ca = java.util.Calendar.getInstance();
        ca.setTime(date);
        return ca.get(java.util.Calendar.SECOND);
    }

    public int dayOfWeek()
    {
        return dayOfWeek(new Date());
    }

    public int dayOfWeek(Date date)
    {
        java.util.Calendar ca = java.util.Calendar.getInstance();
        ca.setTime(date);
        return ca.get(java.util.Calendar.DAY_OF_WEEK);
    }

    public Date addYear(int n)
    {
        return addYear(new Date(),n);
    }

    public Date addYear(Date date,int n)
    {
        java.util.Calendar ca = java.util.Calendar.getInstance();
        ca.setTime(date);
        ca.add(java.util.Calendar.YEAR,n);
        return ca.getTime();
    }

    public Date addMonth(int n)
    {
        return addMonth(new Date(),n);
    }

    public Date addMonth(Date date,int n)
    {
        java.util.Calendar ca = java.util.Calendar.getInstance();
        ca.setTime(date);
        ca.add(java.util.Calendar.MONTH,n);
        return ca.getTime();
    }

    public Date addDate(int n)
    {
        return addDate(new Date(),n);
    }

    public Date addDate(Date date,int n)
    {
        java.util.Calendar ca = java.util.Calendar.getInstance();
        ca.setTime(date);
        ca.add(java.util.Calendar.DATE,n);
        return ca.getTime();
    }

    public Date addHour(int n)
    {
        return addHour(new Date(),n);
    }

    public Date addHour(Date date,int n)
    {
        java.util.Calendar ca = java.util.Calendar.getInstance();
        ca.setTime(date);
        ca.add(java.util.Calendar.HOUR,n);
        return ca.getTime();
    }

    public Date addMinute(int n)
    {
        return addMinute(new Date(),n);
    }

    public Date addMinute(Date date,int n)
    {
        java.util.Calendar ca = java.util.Calendar.getInstance();
        ca.setTime(date);
        ca.add(java.util.Calendar.MINUTE,n);
        return ca.getTime();
    }

    public Date addSecond(int n)
    {
        return addSecond(new Date(),n);
    }

    public Date addSecond(Date date,int n)
    {
        java.util.Calendar ca = java.util.Calendar.getInstance();
        ca.setTime(date);
        ca.add(java.util.Calendar.SECOND,n);
        return ca.getTime();
    }

    public java.util.Date sysdate()
    {
        return new Date();
    }

    public Date nextYear()
    {
        return addYear(1);
    }

    public Date prevYear()
    {
        return addYear( -1);
    }

    public java.util.Date getDate()
    {
        return new Date();
    }

}

