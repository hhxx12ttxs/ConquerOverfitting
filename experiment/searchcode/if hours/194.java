package ba.BITCamp.ajla.weekend1;

public class Task6 {

	public static void main(String[] args) {
		
		int hours = 0, minutes = 17, zeroHour = 0;
		if (hours >= 0 && hours <= 11) {
			System.out.printf("%d:%d AM", hours, minutes);
		}
		else if (hours == 12) {
			System.out.printf("%d:%d PM", hours, minutes);
		}
		else {
			zeroHour = hours - 12;
			System.out.printf("%d:%d PM", zeroHour, minutes);
		}
	}

}

