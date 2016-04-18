package ba.bitcamp.vjezbe.clock;

public class Clock implements WriteableClock {
	private int hours;
	private int minutes;
	private int seconds;

	public Clock(int hours, int minutes, int seconds) {
		super();
		this.hours = hours;
		this.minutes = minutes;
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

	public int getSeconds() {
		return seconds;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}

	@Override
	public void addToFile(String filename, int format) {
		String s = "";
		if (format == AMPM_FORMAT) {
			if (hours > 12) {
				hours -= 12;
				s += "\n" + hours + ":" + minutes + ":" + seconds + " PM";
			} else {
				s += hours + ":" + minutes + ":" + seconds + " AM";
			}
		} else if (format == AMPM_FORMAT_NO_SECONDS) {
			if (hours > 12) {
				hours -= 12;
				s += hours + ":" + minutes + ":" + " PM";
			} else {
				s += hours + ":" + minutes + ":" + " AM";
			}

		} else if (format == MILITARY_FORMAT) {
			s += hours + ":" + minutes + ":" + seconds;
		} else if (format == MILITARY_FORMAT_NO_SECONDS) {
			s += hours + ":" + minutes;
		}

		writeToFile(filename, s);
	}

}

