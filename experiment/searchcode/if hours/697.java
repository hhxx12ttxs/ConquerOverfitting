package misc.questions.t.clock;

public class Clock {
    int hours;
    int minutes;
    public Clock(int hours, int minutes) {
        this.hours = hours;
        this.minutes = minutes;
    }

    @Override
    public boolean equals(Object other) {
        if (! (other instanceof Clock)) return false;
        Clock c = (Clock) other;
        return this.hours == c.hours && this.minutes == c.minutes;
    }
}

