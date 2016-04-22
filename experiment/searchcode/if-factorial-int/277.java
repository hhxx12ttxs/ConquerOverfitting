package mywork;

public class Factorial {
	public static void main(String[] args) {
		System.out.println(getFactorial(100));
	}

	private static double getFactorial(int n) {
		if (n == 1) {
			return 1;
		}
		return n * getFactorial(n - 1);
	}
}

