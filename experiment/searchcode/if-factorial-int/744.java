package Practice3;

public class Factorial {

	// Factorial using for loop
	public static int factorial(int x) {
		int sum = 1;
		for (int i = x; i > 0; i--) {
			System.out.println("sum:" + sum + " x:" + x);
			sum *= i;
		}
		return sum;
	}
	
	// Factorial using recursion
	public static int factorialRecursion(int x) {
		if (x == 1) {
			return 1;
		}
		int sum = x;
		sum = sum * factorialRecursion(x - 1);
		return sum;
	}
	
	// Factorial using compact recursion
	public static int fR(int x) {
		if (x == 1) {
			return 1;
		}
		return x * fR(x - 1);
	}
	
	
	public static void main(String[] args) {
		int x = 4;
		System.out.println(factorialRecursion(x));
	}
}

