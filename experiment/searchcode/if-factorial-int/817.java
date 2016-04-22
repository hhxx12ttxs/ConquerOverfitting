package algorithm.etc;

/**
 * 5! = 5*4*3*2*1 
 * n! = n*(n-1)*(n-2)*...
 */
public class Factorial {

	public static int factorial(int n) {
		if (n <= 1) {
			return 1;
		} else {
			return n * factorial(n - 1);
		}
	}

	public static void main(String[] args) {
		int n = 5;
		System.out.println(factorial(n));
	}
}

