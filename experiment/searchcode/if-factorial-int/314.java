

import java.util.Scanner; 

public class ComputeFactorial {
	/** Main method */
	public static void main(String[] args) {
		// Create a Scanner
		Scanner input = new Scanner(System.in);
		System.out.print("Enter a non-negative integer: ");
		int n = input.nextInt();

		// Display factorial
		factorial(n);
		System.out.println("Factorial of " + n + " is " + factorial(n));
	}

	/** Return the factorial for a specified number */
	public static long factorial(int n) {
		if (n == 0) // Base case
			return 1;
		else
			return n * factorial(n - 1); // Recursive call
	}

	/*public static int factorial(int n) {
		if (n == 1) {
			// Base case 
			//System.out.println("This is the base case: " + n + " == 1");
			return 1;
		}

		else {
			System.out.println(n + " + factorial(" + n + " - 1)" + " = " + factorial(n - 1));
			System.out.println("factorial(" + n + " - 1)" + " = " + factorial(n - 1));
			System.out.println("n = " + n);
			System.out.println(n + factorial(n - 1) + "\n");
			return n + factorial(n - 1); // Recursive call
		}
	}*/
}

