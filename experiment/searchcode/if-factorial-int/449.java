/* 
 * HDC - Data Structure and Algorithms
 * Exercise # 1
 * CodingBat.com - Recusion exercise
 * Factorial:
 * Given n of 1 or more, return the factorial of n,
 * which is n * (n-1) * (n-2) ... 1.
 * Compute the result recursively (without loops).
 * factorial(1) → 1
 * factorial(2) → 2
 * factorial(3) → 6
 *
 * Michael Twohig
 * 2016-02-05
 */


public class recursion {
	public static void main (String [] args) {
		print(factorial(1));
		print(factorial(2));
		print(factorial(3));
	}

	private static int factorial(int n){
		if (n==1)
			return 1;
		else
			return n*factorial(n-1);
	}

	private static void print(String s){
		System.out.println(s);
	}

	private static void print(int n){
		System.out.println(n);
	}
}

