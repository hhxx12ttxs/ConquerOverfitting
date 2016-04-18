package ba.bitcamp.senadin.homework.weekend.one;

public class Task6 {

	public static void main(String[] args) {
		/*Make a program that contains two variables that 
		 * represent the time in the format 24: 00h (military time).
		 * Print the screen in the format AM / PM.*/

		int hours = 18;
		int minutes = 12;
		int Gsat = 0 ;
		
		if ( hours >= 0 && hours <= 11 ) {
			System.out.println( hours + ":" + minutes + "AM");
		} else if ( hours == 12 ) {
			System.out.println( hours + ":" + minutes + "PM");
		} else {
			Gsat = hours - 12;
			System.out.println( Gsat + ":" + minutes + "PM");
		}
	}

}

