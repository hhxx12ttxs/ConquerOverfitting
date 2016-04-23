package learn.tutorials.simpleprograms;

/**
 * Program to give Factorial of a number with and without recursion
 * 
 * @author KD
 * 
 */
public class Factorial {

	public static void main(String[] args) {
		int input = 5, factorial = 1;
		System.out.println("Without Reccursion");
		for (int i = input; i > 0; i--)
			factorial = factorial * i;
		System.out.println(factorial);
		System.out.println("With Recursion");
		System.out.println(factorialWithRecursion(input));
	}

	private static int factorialWithRecursion(int input) {
		if (input == 1)
			return input;
		return input * factorialWithRecursion(input - 1);
	}

}

