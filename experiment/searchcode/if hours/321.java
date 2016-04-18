package edu.avans.hartigehap.a1.timepicker;

public class Time {
    private int hours;
    private int minutes;

    public Time(int hours, int minutes) {
        setTime(hours, minutes);
    }

    public void setTime(int hours, int minutes) {
        if (hours < 0) {
            this.hours = 0;
        } else {
            this.hours = hours % 24;
        }

        if (minutes < 0) {
            this.minutes = 0;
        } else if (minutes >= 60) {
            this.hours += Math.floor(minutes / 60);
            this.minutes = minutes % 60;
        } else {
            this.minutes = minutes;
        }
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public String toString() {
        return String.format("%1$d:%2$02d", hours, minutes);
    }
}

