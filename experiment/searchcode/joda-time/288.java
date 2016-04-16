package de.minestar.diehard.core;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Time implements Comparable<Time> {
    private int hours;
    private int minutes;

    public Time() {
        // init Time with 0 hours and 0 minutes
        this.setTime(0, 0);
    }

    public Time(int h, int m) {
        // init Time with given hours and minutes
        this.setTime(h, m);
    }

    public Time(int m) {
        // init time from given minutes
        int h = m / 60;
        m %= 60;
        this.setTime(h, m);
    }

    public Time(String timeText) {
        // init Time from text in format HH:mm
        this.setTime(timeText);
    }

    public Time(Date date) {
        // init Time from Java's Date class
        DateFormat df = DateFormat.getTimeInstance();
        this.setTime(df.format(date));
    }

    @Override
    public int compareTo(Time t) {
        // compare different Time instances
        if (this.hours == t.hours) {
            if (this.minutes == t.minutes) {
                return 0;
            } else {
                return this.minutes - t.minutes;
            }
        } else {
            return this.hours - t.hours;
        }
    }

    @Override
    public String toString() {
        // convert Time to text formatted as HH:mm
        return String.format("%02d:%02d", this.hours, this.minutes);
    }

    public Time substract(Time t) {
        // substract Time instances from each other
        int h = this.hours - t.hours;
        int m = this.minutes - t.minutes;
        if (m < 0) {
            m += 60;
            h -= 1;
        }
        return new Time(h, m);
    }

    public Time add(Time t) {
        // add Time instances to another
        int h = this.hours + t.hours;
        int m = this.minutes + t.minutes;
        if (m > 59) {
            m -= 60;
            h += 1;
        }
        if (h > 23) {
            h -= 24;
        }
        return new Time(h, m);
    }

    public int toMinutes() {
        // convert Time to only minutes
        return this.hours * 60 + this.minutes;
    }

    private void setTime(int h, int m) {
        // set hours and minutes and check if they are valid
        if (this.isValid(h, m)) {
            this.hours = h;
            this.minutes = m;
        } else {
            this.hours = -1;
            this.minutes = -1;
        }
    }

    private void setTime(String timeText) {
        // set time from text in format HH:mm
        String[] timeFragments = timeText.split(":");
        int hours, minutes;
        try {
            hours = Integer.parseInt(timeFragments[0]);
            minutes = Integer.parseInt(timeFragments[1]);

            setTime(hours, minutes);
        } catch (NumberFormatException e) {
            this.hours = -2;
            this.minutes = -2;
        }
    }

    private boolean isValid(int hours, int minutes) {
        // check if time values are valid
        if ((hours >= 0) && (hours <= 23) && (minutes >= 0) && (minutes <= 59)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isValid() {
        return this.isValid(this.hours, this.minutes);
    }

    public boolean isAfter(Time t) {
        if (this.hours > t.hours) {
            return true;
        } else if (this.hours < t.hours) {
            return false;
        } else {
            if (this.minutes > t.minutes) {
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean isBefore(Time t) {
        if (this.hours < t.hours) {
            return true;
        } else if (this.hours > t.hours) {
            return false;
        } else {
            if (this.minutes < t.minutes) {
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean equals(Time t) {
        if ((this.hours == t.hours) && (this.minutes == t.minutes)) {
            return true;
        } else {
            return false;
        }
    }

    public Time difference(Time t) {
        int result;
        if (t.isAfter(this)) {
            Time diff = t.substract(this);
            result = diff.toMinutes();
        } else {
            result = t.toMinutes() + (int) TimeUnit.DAYS.toMinutes(1) - this.toMinutes();
        }
        return new Time(result);
    }
}

