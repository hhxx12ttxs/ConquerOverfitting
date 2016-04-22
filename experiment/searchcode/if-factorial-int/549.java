package numberproblems;

import java.util.Scanner;

/*
 * Given a positive integer returns it's factorial value
 */
public class Factorial {

	public static void main(String[] args) {
		System.out.println("Enter a positive integer:");
		Scanner in = new Scanner(System.in);
		int number;
		number = in.nextInt();
		if (number < 0) {
			System.out.println("No negative numbers please!");
		} else {
			System.out.println("Factorial of " + number + " is "
					+ findFactorial(number));
		}
		in.close();
	}

	private static int findFactorial(int number) {
		int factorial = 1;
		for (int i = 1; i <= number; i++) {
			factorial = factorial * i;
		}
		return factorial;
	}
}

