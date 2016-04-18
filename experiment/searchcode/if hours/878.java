import java.util.Arrays;
import java.util.Scanner;


public class _01_VideoDurations {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		int hours = 0;
		int minutes = 0;
		
		while (true) {
			String line = sc.nextLine();
			if (line.equals("End")) {
				break;
			}
			String[] data = line.split(":");
			int currentHours = Integer.parseInt(data[0]);
			int currentMinutes = Integer.parseInt(data[1]);
			hours += currentHours;
			minutes += currentMinutes;
			
		}
		sc.close();
		hours = hours + (minutes/60);
		minutes = minutes % 60;
		
		System.out.printf("%d:%02d",hours,minutes);
	}

}

