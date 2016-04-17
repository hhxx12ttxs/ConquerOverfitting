package ba.bitcamp.w06d01.exercises.interfacee.task03;

public class Clock implements WriteableClock {

	private int hours;
	private int minutes;
	private int seconds;

	/**
	 * Constructor
	 * @param hours
	 * @param minutes
	 * @param seconds
	 */
	public Clock(int hours, int minutes, int seconds) {
		super();
		if (hours >= 0 && hours < 24) {
			this.hours = hours;
		} else {
			throw new NumberFormatException();
		}
		if (minutes >= 0 && minutes < 60) {
			this.minutes = minutes;
		} else {
			throw new NumberFormatException();
		}
		if (seconds >= 0 && seconds < 60) {
			this.seconds = seconds;
		} else {
			throw new NumberFormatException();
		}
	}

	@Override
	public void addToFile(String filename, int format) {
		String s = "";
		if (format == AMPM_FORMAT) {
			if (hours > 12) {
				s = (hours - 12) + ":" + minutes + ":" + seconds + " PM";
			} else {
				s = hours + ":" + minutes + ":" + seconds + " AM";
			}
			writeToFile(filename, s);
		} else if (format == AMPM_FORMAT_NO_SECONDS) {
			if (hours > 12) {
				s = (hours - 12) + ":" + minutes + " PM";
			} else {
				s = hours + ":" + minutes + " AM";
			}
			writeToFile(filename, s);
		} else if (format == MILITARY_FORMAT) {
			s = hours + ":" + minutes + ":" + seconds;
			writeToFile(filename, s);
		} else if (format == MILITARY_FORMAT_NO_SECONDS) {
			s = hours + ":" + minutes;
			writeToFile(filename, s);
		} else {
			throw new NumberFormatException();
		}

	}

}

