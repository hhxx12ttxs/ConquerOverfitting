package lab22;

public class Period {
    private int seconds;
    private int hours;
    private int minutes;

    public Period() {
    }

    public Period(int seconds) {
        this.seconds = seconds;
    }

    public Period(int hours, int minutes, int seconds) {
        this.seconds = seconds;
        this.minutes = minutes;
        this.hours = hours;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int sumOfHoursMinutesSeconds() {
        return (hours * 60 * 60) + (minutes * 60) + seconds;
    }

    public String timeCompare(Period obj) {
        int comparedPeriod = obj.hours * 60 * 60 + obj.minutes * 60 + obj.seconds;
        if (sumOfHoursMinutesSeconds() > comparedPeriod) {
            return "первый промежуток больше второго";
        } else if (sumOfHoursMinutesSeconds() == comparedPeriod) {
            return "равны";
        } else {
            return "первый промежуток меньше второго";
        }
    }
}
