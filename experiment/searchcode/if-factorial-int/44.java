package factorial;

public class Factorial {

	public static long factorial(int n) {
		if (n <= 1) {
			return 1;
		} else {
			return n * factorial(n - 1);
		}
	}

	public static long factorialLoop(int n) {
		long factorial = 1;
		for (int i = 1; i <= n; i++)
			factorial *= i;
		return factorial;
	}
}


