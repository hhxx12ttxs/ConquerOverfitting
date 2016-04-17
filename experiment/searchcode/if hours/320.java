package misc.questions.t.clock;

public class ClockMirror {

    public Clock findMirror(Clock c) {
        int newMinutes = 60 - c.minutes;
        int newHours = 11 - c.hours;
        if (newMinutes == 60) {
            newMinutes = 0;
            newHours += 1;
        }
        if (newHours == 12) {
            newHours = 0;
        }
        return new Clock(newHours, newMinutes);
    }

    public SecondClock findMirrorWithSeconds(SecondClock c) {
        int newSeconds = 60 - c.seconds;
        int newMinutes = 59 - c.minutes;
        int newHours = 11 - c.hours;
        if (newSeconds == 60) {
            newSeconds = 0;
            newMinutes += 1;
        }
        if (newMinutes == 60) {
            newMinutes = 0;
            newHours += 1;
        }
        if (newHours == 12) {
            newHours = 0;
        }
        return new SecondClock(newHours, newMinutes, newSeconds);
    }
}

