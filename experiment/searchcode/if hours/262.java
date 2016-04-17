package hospital.time;

public class DayTime {

    public static final int HOURS_IN_DAY = 24;
    public static final int MINUTES_IN_HOUR = 60;

    private int hours;
    private int minutes;

    public DayTime() {
    }

    public DayTime(int hours, int minutes) {
        setHours(hours);
        setMinutes(minutes);
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        if (minutes < MINUTES_IN_HOUR || minutes >= 0) this.minutes = minutes;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        if (hours < HOURS_IN_DAY || hours >= 0) this.hours = hours;
    }

    public DayTime deduct(int hours, int minutes) {
        return new DayTime(
                (hours + this.getHours()) % HOURS_IN_DAY,
                (minutes + this.getMinutes()) % MINUTES_IN_HOUR
        );
    }

    public String getDescription() {
        return Integer.toString(hours) + ":" + Integer.toString(minutes);
    }
}

