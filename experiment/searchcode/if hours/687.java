package ba.bitcamp.interfacevjezbe1;

public class Clock implements WriteableClock {

	int hours;
	int minutes;
	int seconds;
	
	public Clock( int hours, int minutes, int seconds){
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
	public String toString() {
		return "Clock [hours=" + hours + ", minutes=" + minutes + ", seconds="
				+ seconds + "]";
	}

	@Override
	public void addToFile(String filename, int format) {
		String formatedTime = "";
		if( format == AMPM_FORMAT ){
			String time = "AM";
			if( hours > 12 ){
				hours = hours % 12;
				time = "PM";
			}
			formatedTime = hours + ":" + minutes + ":" + seconds;
			writeToFile(filename, formatedTime);
			
		} else if( format == AMPM_FORMAT_NO_SECONDS ){
			String time = "AM";
			if( hours > 12 ){
				hours = hours % 12;
				time = "PM";
			}
			formatedTime = hours + ":" + minutes;
			writeToFile(filename, formatedTime);
			
		} else if( format == MILITARY_FORMAT){
			formatedTime = hours + ":" + minutes + ":" + seconds;
			writeToFile(filename, formatedTime);
		} else if( format == MILITARY_FORMAT_NO_SECONDS ){
			formatedTime = hours + ":" + minutes;
			writeToFile(filename, formatedTime);
		}
	}
	
	
	
}

