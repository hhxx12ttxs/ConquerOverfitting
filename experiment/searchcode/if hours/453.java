
import java.util.Scanner;

public class PayCheck {
	public static void main (String[] args) {
		final int overtime = 40;
		
		int hours;
		double rate;
		int overtimeHours = 0;
		boolean itIsOver = false;
		
		Scanner scan = new Scanner(System.in);

		System.out.print ("How many hours did you work? ");
		hours = scan.nextInt();
		
		System.out.print ("How much do you get paid per hour? ");
		rate = scan.nextDouble();
		
		if (hours > overtime){
			overtimeHours = hours - 40;
			hours = 40;
			itIsOver = true;
		}
		
		if (itIsOver){
			System.out.println("Pay for overtime: " + (overtimeHours*1.5*rate));
		}
		else
			System.out.println("There is no overtime payment");
		
		System.out.println("The total gross payment is of: $" + ((hours*rate) + (overtimeHours*1.5*rate)));

	}
}

