import java.util.*;

public class Factorial{

	public static double factorial (int n) {
		
		if (n==1)
			return 1;
		else {
			return n * factorial(n-1);
		}
	}
	
	public static void main (String args[]) {

		Scanner s = new Scanner (System.in);
		int input;
		
		System.out.println("Factorial Number Calculator");
		System.out.print("Enter the number whose factorial you would like to calculate: ");
			input = s.nextInt();

		System.out.println("\nThe factorial is " + factorial(input));
	}
}
