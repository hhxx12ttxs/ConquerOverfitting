package com.github.joey11111000111.EasyPlan.core.util;

import java.io.Serializable;

/**
 * Created by joey on 2015.11.06..
 */
public class DayTime implements Serializable {
    static final long serialVersionUID = 0L;

    private int hours;
    private int minutes;

    public DayTime(int minutes, boolean canOverflow) {
        if (!canOverflow)
            if (minutes < 0 || minutes > (23 * 60 + 59))
                throw new IllegalArgumentException("time day-overflow is not allowed, must represent one day from "
                        + "0:0 to 23:59");
        this.hours = minutes / 60;
        this.minutes = minutes - hours * 60;
        this.hours %= 24;
    }
    public DayTime(int minutes) {
        this(minutes, false);
    }

    public DayTime(int hours, int minutes) {
        setHours(hours);
        setMinutes(minutes);
    }

    public DayTime(DayTime rhs) {
        hours = rhs.getHours();
        minutes = rhs.getMinutes();
    }


    public int getHours() {
        return hours;
    }
    public int getMinutes() {
        return minutes;
    }

    public int getTimeAsMinutes() {
        return hours * 60 + minutes;
    }

    public boolean setHours(int hours) {
        if (hours < 0 || hours > 23)
            throw new IllegalArgumentException("hours must be in range [0, 23]");
        if (this.hours == hours)
            return false;
        this.hours = hours;
        return true;
    }

    public boolean setMinutes(int minutes) {
        if (minutes < 0 || minutes > 59)
            throw new IllegalArgumentException("minutes must be in range [0, 59]");
        if (this.minutes == minutes)
            return false;
        this.minutes = minutes;
        return true;
    }

    public String toString() {
        return "" + hours + ':' + minutes;
    }
}//class

