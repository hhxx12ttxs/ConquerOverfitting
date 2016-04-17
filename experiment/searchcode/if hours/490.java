package ba.bitcamp.medina.banjic;

public class Converter {

	public static void main(String[] args) {

		int hours = 0;
		int minutes = 34;
		String midnight = "AM";
		String noon = "PM";
		
		// I declared variables for hours, minutes and two periods AM and PM
		
		// IF statements below are converting military time format to 12 hours clock format
		// If the hours are greater or equal zero and less than 12 it will just add AM
		// If the hours are greater or equal to 12 it will first check weather it is greater than
		// 13, if it is then it will subtract hours and 12, when finished print out and add PM
		// If the hours are 12 it will just add PM

		if (hours >= 0 && hours < 12) {

			System.out.printf("It is %d : %d %s ", hours, minutes, midnight);
		}

		else if (hours >= 12 && hours < 25) {

			if (hours >= 13) {

				hours -= 12;
			}

			System.out.printf("It is %d : %d %s ", hours, minutes, noon);
		}
		
		else 
			
			System.out.println("Wrong time.");
	}

}

