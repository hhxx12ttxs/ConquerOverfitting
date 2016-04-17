import java.util.Scanner;


public class Program01 {

	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		int hoursCount = 0;
		int minutesCount = 0;
		while (true) {
			String str = scan.nextLine();
			if (str.equals("End")) {
				break;
			}
			String[] split = str.split("[ :]+");
			int hours = Integer.parseInt(split[0]);
			int minutes = Integer.parseInt(split[1]);
			minutesCount += minutes;
			if (minutesCount >= 60) {
				minutesCount = minutesCount -60;
				hoursCount = hoursCount + 1;
			}
			hoursCount += hours;
			
		}
		System.out.printf("%d:%02d",hoursCount,minutesCount);

	}
	

}

