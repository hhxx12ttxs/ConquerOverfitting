package ge;

public class FactorialTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(factorial(10));

	}

	public static int factorial(int n) {
		if (n <= 0) {
			return 1;
		}
		return n * factorial(n - 1);

	}
}

