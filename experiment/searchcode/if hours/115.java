package ba.bitcamp.exercises.day1.task3;

public class Clock implements WriteableClock {
	
	
	private int hours;
	private int minutes;
	private int seconds;
	
	

	public Clock(int hours, int minutes, int seconds) {
		this.hours = hours;
		this.minutes = minutes;
		this.seconds = seconds;
	}


	

	@Override
	public void addToFile(String filename, int format) {
		String s = "";
		if (format == AMPM_FORMAT) {
			if (hours > 12) {
				hours -= 12;
				s += hours + ":" + minutes + ":" + seconds + " PM";
			} else {
				s += hours + ":" + minutes + ":" + seconds + " AM";
			}
		} else if (format == AMPM_FORMAT_NO_SECONDS) {
			if (hours > 12) {
				hours -= 12;
				s += hours + ":" + minutes + " PM";
			} else {
				s += hours + ":" + minutes + " AM";
			}
		} else if (format == MILITARY_FORMAT) {
			s += hours + ":" + minutes + ":" + seconds;
		} else if (format == MILITARY_FORMAT_NO_SECONDS) {
			s += hours + ":" + minutes;
		}
		writeToFile(filename, s);
	}

}

