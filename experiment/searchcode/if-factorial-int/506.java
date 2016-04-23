import java.util.Scanner;
/**
 * This program takes in a number and prints the factorial of 
 * that number. If the number is less than or equal to 0, the
 * program will print out 1.
 * @author a00882367
 * @version 1
 */
public class Factorial {
/**
 * The main method runs the program and does the calculations for the
 * factorial using a for loop.
 * @param args unused
 */
	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		int initialFactorial;
		int factorial;
		int finalFactorial = 1;
		
		System.out.println("Enter the number you want the factorial of: ");
		initialFactorial = scan.nextInt();
		factorial = initialFactorial;
		if (initialFactorial > 0) {
			for (int i = 0; factorial > i; factorial--) {
				finalFactorial *= factorial;
			}
		} else {
			finalFactorial = 1;
		}
		System.out.println("The factorial of " + initialFactorial 
				+ " is: " + finalFactorial);
		scan.close();
	}
}

