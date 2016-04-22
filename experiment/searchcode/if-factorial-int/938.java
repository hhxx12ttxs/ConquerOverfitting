package info._7chapters.test;
class Factorial {
	int fact(int n) {
		int result;
		if(n==1) return 1;
		result = fact(n-1) * n;
		return result;
	}
}

public class Recursion {
	/**
	 * @param args
	 */
	public static void main(String... args) {
		Factorial f = new Factorial();
		System.out.println("Factorial of 3 is " + f.fact(3));
	}
}
