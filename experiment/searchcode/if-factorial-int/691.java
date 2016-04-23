package in.co.sunrays.corejava;

public class FactorialTest {

	public static void main(String[] args) {
		int n = 5;
		int fact = factorial(n);
		System.out.println(fact);
	}

	public static int factorial(int n) {
		if (n == 1) {
			return 1;
		} else {
			return factorial(n - 1) * n;
		}
	}

}

