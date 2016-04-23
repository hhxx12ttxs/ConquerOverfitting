package yg0r2.examples.recursive;

public class Factorial {

	public static void main(String[] args) {
		System.out.println(factorial(5));
	}

	public static int factorial(int n) {
		if (n <= 1) {
			return 1;
		}

		return n * factorial(n - 1);
	}

}
