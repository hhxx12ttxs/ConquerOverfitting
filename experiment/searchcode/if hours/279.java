package utils;

public class Time implements Comparable<Time> {
	private int hours;
	private int minutes;
	private int seconds;

	public Time(int hours, int minutes, int seconds) {
		this.hours = hours;
		this.minutes = minutes;
		this.seconds = seconds;
		nomalize();
	}
	
	public Time(double time) {
		hours = (int) Math.floor(time);
		time = time - hours;
		time *= 60;
		minutes = (int) Math.floor(time);
		time = time - minutes;
		time *= 60;
		seconds = (int) Math.ceil(time);
		
		if (seconds != 0) {
			seconds = 0;
			minutes++;
			nomalize();
		}
	}

	private void nomalize() {
		minutes = minutes + seconds / 60;
		hours = hours + minutes / 60;
		
		seconds %= 60;
		minutes %= 60;
		hours %= 24;
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
	
	public Time add(Time t) {
		return new Time(hours + t.hours, minutes + t.minutes, seconds + t.seconds);
	}

	@Override
	public int compareTo(Time t) {
		if (hours != t.hours)
			return Integer.compare(hours, t.hours);
		if (minutes != t.minutes)
			return Integer.compare(minutes, t.minutes);
		return Integer.compare(seconds, t.seconds);
	}
	
	@Override
	public String toString() {
		return String.format("%d:%02d:%02d", hours, minutes, seconds);
	}
	
}

