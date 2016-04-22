public class FactorialRecursion {
	public static int Factorial(int n) {
		//Base case
		if (n<=1) {
			return 1;
		}
		else {
			return n * Factorial(n-1);
		}
	}
	public static void main(String[] args) {
		System.out.println("4 factorial is: " + Factorial(4));
		System.out.println("10 factorial is: " + Factorial(10));
		System.out.println("1 factorial is: " + Factorial(1));
		System.out.println("0 factorial is: " + Factorial(0));
	}
}
