package Lesson3;

public class Factorial {

	public static void main(String[] args) {
		long factorial = 1;
		int N = 5;
		if (N > 0) {
			for (int f = 1; f <= N; f++) {
				factorial *= f;
			}
		}
		System.out.println("Factorial of number " + N + " is:");
		System.out.println(N <= 0? "undefined" : Long.toString(factorial));
	}
}

