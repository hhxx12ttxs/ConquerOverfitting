package recursion;

public class FactorialTester {

	public static void main(String[] args) {
		int f = FactorialTester.factorial(5);
		System.out.println("5! = " + f);

	}

	public static int factorial(int n) {
		if (n == 0) // base case
			return 1;
		else
			return n * factorial(n - 1);
	}

}

