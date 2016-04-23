package j2.classesAndObjects;

public class Factorial {
	int factorial(int n) {
		int product = 1;
		for (int i = 2; i <= n; i++)
			product *= i;
		return product;
	}

	// Advantage of Recursive approach is that it is able to express a problem
	// in simpler terms of itself
	public int factorialRecursive(int n) {
		if (n == 1)
			return 1;
		else
			return n * factorialRecursive(n - 1);
	}

	/*
	 * The stack of expression is called if factorialRecursive(4) is called
	 * 4*factorial(3) 
	 * 3*factorial(2) 
	 * 2*factorial(1)
	 */

	public static void main(String[] args) {
		Factorial x = new Factorial();
		System.out.println(x.factorialRecursive(15));
		System.out.println(x.factorial(3));
	}
}

