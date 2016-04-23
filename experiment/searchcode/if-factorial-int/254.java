package ds.learning.list.misc;

public class Factorial {

	// Using Loop
	public int printFactorialValue(int n) {
		int factorialValue = 1;
		if (n == 0)
			return factorialValue;
		for (int i = 1; i <= n; i++) {
			factorialValue = factorialValue * i;
		}
		return factorialValue;
	}

	// Recursive Example
	public int factorialUsingRecursive(int n) {
		if (n == 1)
			return n;
		else
			return n * factorialUsingRecursive(n - 1);
	}

	public static void main(String[] args) {
		Factorial factorialTest = new Factorial();
		int n = 10;

		System.out.println("Using ForLoop");
		System.out.println(factorialTest.printFactorialValue(n));

		System.out.println("Using Recursive");
		System.out.println(factorialTest.factorialUsingRecursive(n));
	}
}
