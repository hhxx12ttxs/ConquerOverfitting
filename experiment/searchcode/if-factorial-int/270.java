package codingbatjava;

/**
 * Given n of 1 or more, return the factorial of n, 
 * which is n * (n-1) * (n-2) ... 1. Compute the result recursively (without loops). 
 * 
 * factorial(1) 1
 * factorial(2) 2
 * factorial(3) 6
 * 
 */

public class Factorial {

	public static void main(String[] args) {

		int number = 4;
		
		System.out.println("functionFactorial: " + functionFactorial(number));
	}
	
	public static int functionFactorial(int number) {

		if (number <= 1) {
			return 1;
		}
		
		else {
			return number * functionFactorial(number - 1);
		}
	}
}
