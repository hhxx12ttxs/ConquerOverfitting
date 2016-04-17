package com.drukido.vrun.utils;

public class Duration {
    private static final String SPLIT_CHAR = ":";
    private long hours;
    private long minutes;
    private long seconds;

    public long getHours() {
        return hours;
    }

    public void setHours(long hours) {
        this.hours = hours;
    }

    public long getMinutes() {
        return minutes;
    }

    public void setMinutes(long minutes) {
        this.minutes = minutes;
    }

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    public String toPresentableString(){
        String strHours = String.valueOf(hours);
        String strMinutes = String.valueOf(minutes);
        String strSeconds = String.valueOf(seconds);

        if(hours < 10){
            strHours = "0" + String.valueOf(hours);
        }
        if(minutes < 10){
            strMinutes = "0" + String.valueOf(minutes);
        }
        if(seconds < 10){
            strSeconds = "0" + String.valueOf(seconds);
        }

        if (hours == 0) {
            return strMinutes + ":" + strSeconds;
        } else {
            return strHours + ":" + strMinutes + ":" + strSeconds;
        }
    }

    @Override
    public String toString() {
        String strHours = String.valueOf(hours);
        String strMinutes = String.valueOf(minutes);
        String strSeconds = String.valueOf(seconds);

        if(hours < 10){
            strHours = "0" + String.valueOf(hours);
        }
        if(minutes < 10){
            strMinutes = "0" + String.valueOf(minutes);
        }
        if(seconds < 10){
            strSeconds = "0" + String.valueOf(seconds);
        }

        return strHours + ":" + strMinutes + ":" + strSeconds;
    }

    public static Duration fromString(String durationString) {
        String args[] = durationString.split(SPLIT_CHAR);

        Duration duration = new Duration();
        duration.setHours(Long.valueOf(args[0]));
        duration.setMinutes(Long.valueOf(args[1]));
        duration.setSeconds(Long.valueOf(args[2]));

        return duration;
    }
}

