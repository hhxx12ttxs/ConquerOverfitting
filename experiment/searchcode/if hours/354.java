import java.util.Scanner;

/*
 * Rahul Shah
 * This application allows a user to choose among converting hours to minutes, 
 * days to hours, minutes to hours, or hours to days.
 */
public class TimeConverter {

	
	public static void main(String[] args) {
		//variables
		int choice, answer;
		Scanner scan = new Scanner (System.in);
		
		
		displayOptions();
		
		System.out.print("Enter your choice: ");
		choice = scan.nextInt();
		
		if (choice == 1){
			answer = hoursToMinutes();
			System.out.println("There are " + answer + " minutes");
		}else if (choice == 2){
			answer = daysToHours();
			System.out.println("There are " + answer + " hours");
		}else if (choice == 3){
			answer = minutesToHours();
			System.out.println("There are " + answer + " hours");
		}else if (choice == 4){
			answer = hoursToDays();
			System.out.println("There are " + answer + " days");
		}else
			System.out.println("invalid choice ");
		

	}//end of main
	
	public static void displayOptions (){
		System.out.println("Enter the number for your choice");
		System.out.println("1. to convert hours to minutes");
		System.out.println("2. to convert days to hours");
		System.out.println("3. to convert minutes to hours");
		System.out.println("4. to convert hours to days");
		
		
	}//end of displayOptions
	public static int hoursToMinutes (){
		
		Scanner scan = new Scanner (System.in);
		int hours, minutes;
		System.out.print("Enter the number of hours: ");
		hours = scan.nextInt();
		
		minutes = hours * 60;
		return minutes;
	}//end of hoursToMinutes
	
	public static int daysToHours(){
		Scanner scan = new Scanner (System.in);
		int days, hours;
		System.out.print("Enter the number of days: ");
		days = scan.nextInt();
		
		hours = days * 24;
		return hours;
	}//end of daysToHours
	public static int minutesToHours(){
		Scanner scan = new Scanner (System.in);
		int minutes, hours;
		System.out.println("Enter the number of minutes: ");
		minutes = scan.nextInt();
		
		hours = minutes / 60;
		return hours;
	}//end of minutesToHours
	public static int hoursToDays(){
		Scanner scan = new Scanner (System.in);
		int hours, days;
		System.out.println("Enter number of hours: ");
		hours = scan.nextInt();
		
		days = hours / 24;
		return days;
	}//end of hoursToDays

}//end of Time Converter

