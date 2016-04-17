package JavaBooklet1;

import java.util.Scanner;

public class JourneyPlanner {
	public static void main(String[] args){
		Scanner sc = new Scanner(System.in);
		System.out.print("Enter a start time (HH:MM): " );
		String x = sc.nextLine();
		int hours = Integer.valueOf(x.split("\\D")[0]);
		int mins = Integer.valueOf(x.split("\\D")[1]);
		
		System.out.print("Enter a journey duration (HH:MM): ");
		x = sc.nextLine();
		hours += Integer.valueOf(x.split("\\D")[0]);
		mins += Integer.valueOf(x.split("\\D")[1]);
		if(mins >= 60){
			mins %= 60;
			hours++;
		} // if
		String m = " AM";
		if(hours == 12){
			m = " PM";
		}else if(hours == 24){
			hours = 0;
		}else if(hours > 12 && hours < 24){
			hours %= 12;
			m = " PM";
		} // if
		String eta = (mins<10)?hours+":0"+mins+ m:hours+":"+mins+m;
		System.out.println("ETA: " + eta);
		sc.close();
	} // Main	
} // Class

