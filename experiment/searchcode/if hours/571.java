package FoolProofTimeClass;
public class Time {

    private int hours;
    private int minutes;
    private int seconds;

    public Time(int hours, int minutes, int seconds) throws IllegalArgumentException {
        if (hours < 0 || hours > 24 || minutes < 0 || minutes > 59 || seconds < 0 || seconds > 59) {
            throw new IllegalArgumentException();
        }
        else{
        this.setHours(hours);
        this.setMinutes(minutes);
        this.setSeconds(seconds);
        }

    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) throws IllegalArgumentException {
        if (hours < 0 || hours > 24) {
            throw new IllegalArgumentException();
        }
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) throws IllegalArgumentException {
        if (minutes < 0 || minutes > 59) {
            throw new IllegalArgumentException();
        }
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) throws IllegalArgumentException {
        if (seconds < 0 || seconds > 59) {
            throw new IllegalArgumentException();
        }
        this.seconds = seconds;
    }

    @Override
    public String toString() {
        String result = "";
        result = hours + ":" + minutes + ":" + seconds;
        return result;
    }
}

