
public class checkSpendtime {
	private int timeInHours;
	private String inTimes;
	private String data[];

	public checkSpendtime(String time) {
		inTimes = time;
	}

	public int getTimeInHours() {

		if (inTimes.contains(":")) {
			data = inTimes.split(":");
			timeInHours = Integer.parseInt(data[0]);
		} else if (inTimes.contains(":")) {
			timeInHours = 1;
		} else {
			timeInHours = 0;
		}

		return timeInHours;
	}
}

