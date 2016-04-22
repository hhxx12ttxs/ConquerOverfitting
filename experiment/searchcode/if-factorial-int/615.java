package recursion;

public class Factorial {

	public static int factorial(int n) {
		int total = 1;
		for (int i = 1; i <= n; i++)
			total *= i;
		return total;
	}

	public static int factorialRecursion(int n) {
		if (n <= 1)
			return 1;
		else
			return n * factorialRecursion(n - 1);
	}

}

