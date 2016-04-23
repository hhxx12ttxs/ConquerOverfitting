/**
 * created by Maxim Orlov on 18 Nov 2014
 */
package recursion1;

public class Factorial {
	public int factorial(int n) {
		if (n == 1) {
			return n;
		}

		return n * factorial(n - 1);
	}
}

