
public class Clock {
	private int hours;
	private int minutes;

	public Clock (int i, int j) {
		if(i > 24){
			throw new IllegalArgumentException();
		}
		if(j > 59){
			throw new IllegalArgumentException();
		}
		this.hours = i;
		this.minutes = j;
	}
	
	public void addHours(int hours) {
		hours = hours % 24;
		this.hours += hours;
		if (this.hours > 23){
			this.hours -= 24;
		}
	}

	public void addMinutes(int minutes) {
		while(minutes >= 60){
			addHours(1);
			minutes -= 60;
		}
		this.minutes += minutes;
		if(this.minutes >= 60){
			addHours(1);
			this.minutes -= 60;
		}
	}

	@Override
	public String toString() {
		return hours + ":" + minutes;
	}
}

