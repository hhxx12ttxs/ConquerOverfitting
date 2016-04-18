package com.tsystems.javaschool.gurylev.utils;


public class TimePeriod {

    private Integer hours;
    private Integer mins;

    public TimePeriod(Integer hours, Integer mins) {
        this.hours = hours;
        this.mins = mins;
    }

    public TimePeriod add(TimePeriod timePeriod) {

        Integer hoursLocal = timePeriod.hours + hours;
        Integer minsLocal = timePeriod.mins + mins;

        if (minsLocal >= 60) {
            hoursLocal++;
            minsLocal -= 60;
        }

        return new TimePeriod(hoursLocal, minsLocal);
    }

    public Integer getMins() {
        return mins;
    }

    public void setMins(Integer mins) {
        this.mins = mins;
    }

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }

    @Override
    public String toString() {
        return "TimePeriod{" +
                "hours=" + hours +
                ", mins=" + mins +
                '}';
    }
}

