import java.util.TimerTask;

public class Task extends TimerTask{

	private int hours;
	private int minutes;
	private int seconds;
	public Clock c;
	public Alarm a;

	public Task() {
		this.hours = 0;
		this.minutes = 0;
		this.seconds = 0;
	}

	public Task(int hours, int minutes, int seconds) {
		this.hours = hours;
		this.minutes = minutes;
		this.seconds = seconds;
	}

	public Task(Clock c, Alarm a) {
		this.c = c;
		this.a = a;
	}

	public void run() {
		this.seconds++;
		if (this.seconds == 60) {
			this.seconds = 0;
			this.minutes++;
		}

		if (this.minutes == 60) {
			this.minutes = 0;
			this.hours++;
		}

		if (this.hours == 24) {
			this.hours = 0;
		}
	}

	public int getHours() {
		return this.hours;
	}

	public int getMinutes() {
		return this.minutes;
	}

}

