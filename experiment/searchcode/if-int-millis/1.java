package time;

public class Time {

private int millis;

public Time() {
millis = 0;
}

public Time(int millis) {
this.millis = millis;
}

public void update(int delta) {
millis += delta;

