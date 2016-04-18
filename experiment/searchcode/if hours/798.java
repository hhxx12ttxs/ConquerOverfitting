package net.ikaconsulting.TaskTimer.app.models;

import org.joda.time.DateTime;

public class DateTimePretty {
    private static final int MINUTES = 60 * 1000;
    private static final int HOURS = 60 * MINUTES;
    private static final int DAYS = 24 * HOURS;
    public static final String DATE_FORMAT = "yyyy-MMM-dd";

    //Refactor code to use datetimepretty (rename this too) to read and convert dates
    public static String formatDate(DateTime date) {
        return date.toString(DATE_FORMAT);
    }

    public static String formatTime(long time) {
        //Used by anything that converts the time into something pretty
        long days;
        long hours;
        long minutes;

        if (time < MINUTES) {
            return "<1m";
        } else if (time < HOURS) {
            minutes = time / MINUTES;
            return minutes + "m";
        } else if (time < DAYS) {
            hours = time / HOURS;
            minutes = (time - (hours * HOURS)) / MINUTES;
            return hours + "h:" + minutes + "m";
        } else {
            days = time / DAYS;
            hours = (time - (days * DAYS)) / HOURS;
            minutes = (time - (days * DAYS) - (hours * HOURS)) / MINUTES;
            return days + "d:" + hours + "h:" + minutes + "m";
        }
    }
}

