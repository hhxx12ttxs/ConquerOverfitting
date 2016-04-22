package recursion;

public class Factorial {
	private static int ID = 0;
	
	public static int factorial(int n) {
		if (n == 1) return 1;
		
		return n * factorial(n - 1);
	}
	
	public static int factorialIterative(int n) {
		int result = 1;
		for (int i = n; i > 0; --i) {
			result *= i;
		}
		
		return result;
	}
	
	public static void main(String[] args) {
		System.out.println(factorial(500000000));
		//System.out.println(factorialIterative(5));
	}
	
	
}


