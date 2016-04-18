package Homework4;

public class Task6 {

	public static void main(String[] args) {
		
		//Variables for input hours and minutes.//
		int hours = 15;
		int min = 30;
		
		/*If we want to make a 12 format clock, we just needed to subtract hours from 12, when input hour
		is larger then 12*/
		int hoursAmPm= hours - 12;
		
		if (hours > 12 && hours!=24) {
			
			System.out.println(hoursAmPm+":"+ min +" PM");
		}
		//When hours are <=12 result will be AM for sure.//
		else if (hours <= 12) { 
			
			System.out.println(hours+":"+min+" AM"); 
			}
		//Midnight hour//
		else if (hours == 24) {
			System.out.printf("%d:%d AM",hoursAmPm,min);
			
		

		}
	}

}

