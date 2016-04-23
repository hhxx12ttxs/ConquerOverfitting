public class TailRecursion {

	public static int factorial(int n) {

		return _factorial(n, 1);
	}

	private static int _factorial(int n, int aggregator) {
		if (n == 1) {
			return aggregator;
		}

		return _factorial(n-1, aggregator * n);
	}

	public static void main(String[] args) {
		int aggregator = 0;
		System.out.println("factorial is: " + factorial(10));
	}
}

