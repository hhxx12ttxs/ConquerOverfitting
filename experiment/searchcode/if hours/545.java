package de.htwg.scala.time;

public class JavaTime {

	private int hours;
	private int minutes;

	private static final int HOURS_PER_DAY = 24;
	private static final int MINUTES_PER_HOUR = 60;

	public JavaTime(int hours, int minutes) {
		this.hours = hours % HOURS_PER_DAY + minutes / MINUTES_PER_HOUR;
		this.minutes = minutes % MINUTES_PER_HOUR;
	}

	public int getHours() {
		return hours;
	}

	public int getMinutes() {
		return minutes;
	}

	public int getAsMinutes() {
		return hours * MINUTES_PER_HOUR + minutes;
	}

	public void plus(JavaTime that) {
		int newMinutes = (this.minutes + that.minutes) % MINUTES_PER_HOUR;
		int newHours = (this.hours + that.hours) % HOURS_PER_DAY + (this.minutes + that.minutes) / MINUTES_PER_HOUR;
		this.minutes = newMinutes;
		this.hours = newHours;
	}

	public void minus(JavaTime that) {
		while (this.getAsMinutes() < that.getAsMinutes())
			this.hours = this.hours + HOURS_PER_DAY;
		int newMinutes = (this.getAsMinutes() - that.getAsMinutes()) % MINUTES_PER_HOUR;
		int newHours = (this.getAsMinutes() - that.getAsMinutes()) / MINUTES_PER_HOUR;
		this.minutes = newMinutes;
		this.hours = newHours;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (getClass() == obj.getClass()) {
			JavaTime that = (JavaTime) obj;
			if (hours == that.hours && minutes == that.minutes)
				return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + hours;
		result = prime * result + minutes;
		return result;
	}

	@Override
	public String toString() {
		return String.format("%02d:%02d", hours, minutes);
	}
}

