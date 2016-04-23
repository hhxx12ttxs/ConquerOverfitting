package kalkulaator;

public class Factorial {
	public static int factorial(int number) {
		int factorial = 1;
		if (number < 0) {
			System.out.println("Cannot calculate for negative number!");
			return -1;
		}
		if (number == 0) {
			return 1;
		}
		for (int f = 1; f <= number; f++) {
			factorial = factorial * f;
		}
		return factorial;
	}

	public static void main(String[] args) {
		System.out.println(factorial(-2));
		System.out.println(factorial(0));
		System.out.println(factorial(1));
		System.out.println(factorial(2));
		System.out.println(factorial(3));
		System.out.println(factorial(4));
		System.out.println(factorial(5));
		System.out.println(factorial(6));
		System.out.println(factorial(7));
		System.out.println(factorial(8));
		System.out.println(factorial(9));
		System.out.println(factorial(10));
	}
}
