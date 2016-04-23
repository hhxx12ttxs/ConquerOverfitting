package Recursion;
import java.util.Scanner;


public class RecursiveFactorial {

	public static void main(String[] args) {

		System.out.println("Enter number: ");
		Scanner input = new Scanner(System.in);
		int n = input.nextInt();
		long factorial = factorial(n);
		long factorialIterative = factorialItarative(n);
		System.out.printf("%d! = %d", n, factorial);
		System.out.println();
		System.out.printf("%d! = %d", n, factorialIterative);
		input.close();
	}

	static long factorial(int n){
		if (n == 0) {
			return 1;
		}
		else {
			return n * factorial(n-1);
		}
	}
	
	static long factorialItarative(int n){
		long result = 1;
		for (int i = 1; i <= n; i++) {
			result = result * i;
		}
		
		return result;
	}
}

