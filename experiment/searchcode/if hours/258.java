package time_tools;

public class TimeConverter {
	
	public static String convertTime(int time) {
		String newTime;
		int hours;
		int minutes;
		int holder;
		
		minutes = time % 60;
		holder = time - minutes;
		hours = holder / 60;
		if(hours > 12) {
			hours = hours - 12;
		}
		
		if(minutes < 10) {
			newTime = hours + ":0" + minutes;
		} else {
			newTime = hours + ":" + minutes;
		}
		return newTime;
	}

}

