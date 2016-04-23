package code.Ravi.CodingBat.Recursion;

/**
 * Given n of 1 or more, return the factorial of n
 * 
 * @author ravikson
 * 
 */
public class Factorial {
	public static void main(String[] args) {
		System.out.println(factorial(6));
	}

	public static int factorial(int n) {
		if (n == 1)
			return 1;
		return n * factorial(n - 1);
	}

}

