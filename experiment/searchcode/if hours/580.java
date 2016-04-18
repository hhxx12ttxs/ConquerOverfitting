package se1app.praktikum.datatypes;

public class Time {
    private final int hours;
    private final int minutes;
    private final int seconds;

    private Time(int seconds, int minutes, int hours) {
        this.seconds = seconds;
        this.minutes = minutes;
        this.hours = hours;
    }

    public static Time of(int seconds, int minutes, int hours) {
        conditionCheck(seconds, minutes, hours);
        return new Time(seconds, minutes, hours);
    }

    private static void conditionCheck(int seconds, int minutes, int hours) {
        if (seconds < 0 || minutes < 0 ||hours < 0)
            throw new IllegalArgumentException();
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }
}

