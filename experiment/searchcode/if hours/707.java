package org.effectivejava.examples.ch02.item14;

// Item 14: In public classes, use accessors instead of public fields
public class Time {
    public static final int HOURS_PER_DAY = 24;
    public static final int MINUTES_PER_HOUR = 60;

    public final int hours;
    public final int minutes;

    // public class with immutable fields - questionable.
    public Time (int hours, int minutes){
        if (hours < 0 && hours >= HOURS_PER_DAY)
            throw new IllegalArgumentException("Hours : " + hours);
        if (minutes < 0 && minutes >= MINUTES_PER_HOUR)
            throw new IllegalArgumentException("Minutes : " + minutes);
        this.hours = hours;
        this.minutes = minutes;
    }

}

