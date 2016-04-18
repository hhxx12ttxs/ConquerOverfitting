package com.gitgud.hackathon.database;

/**
 * Created by Graham on 12/6/2015.
 */
public final class formatTime {
    private formatTime(){

    }

    public static String convertFromMilitary(String time) {
        String ampm = null;
        String[] hms = time.split(":");

        hms[0].replaceFirst("^0+(?!$)", "");

        int hours = Integer.parseInt(hms[0]);
        int minutes = Integer.parseInt(hms[1]);
        if (hours > 12 || (hours == 12 && minutes > 0)){
            ampm = "p.m.";
            hours = hours - 12;

            if (hours == 0){
                hours = 12;

            }
        } else {
            ampm = "a.m.";
        }

        String newTime = hours + ":" + hms[1] + " " + ampm;
        return newTime;

    }


}

