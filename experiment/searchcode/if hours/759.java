package fan.ringtone.datastructure;

public class DateNode {

	private int hours;
	private int minutes;

	public DateNode(String time) {
		String[] token = time.split(":");
		hours = Integer.parseInt(token[0]);
		minutes = Integer.parseInt(token[1]);
	}

	public DateNode(long time) {
		minutes = (int) (time % 60);
		hours = (int) (time / 60);
	}

	public DateNode(int hours, int minutes) {
		this.hours = hours;
		this.minutes = minutes;
	}

	public long getTime() {
		return hours * 60 + minutes;
	}

	public long getHour() {
		return hours;
	}

	public int getMinutes() {
		return minutes;
	}

	public String toString() {
		if (minutes >= 10)
			return hours + ":" + minutes;
		else
			return hours + ":0" + minutes;
	}

}

