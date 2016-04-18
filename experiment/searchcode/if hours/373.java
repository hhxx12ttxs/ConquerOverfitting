
//Reads a number from the console, that represents the hour of the day and 
//makes an according response

import java.util.Scanner;

public class Problem15 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int hours;
		Scanner sc = new Scanner(System.in);
		System.out.println("Please enter an hour of the day");
		hours = sc.nextInt();
		if (hours < 0 || hours > 24) {
			System.out.println("Please enter a number between 0 and 24");
			hours = sc.nextInt();
		}
		if (hours < 4) {
			System.out.println("Good evening");
		}
		if (hours >= 18) {
			System.out.println("Good evening");
		}
		if (hours >= 4 && hours <= 9) {
			System.out.println("Good morning");
		}
		if (hours > 9 && hours < 18) {
			System.out.println("Good day");

		}
	}

}

