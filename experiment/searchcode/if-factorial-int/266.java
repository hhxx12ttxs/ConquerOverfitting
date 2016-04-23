
public class Factorial {

//	public int factorial(int n) {
//		int foo = n;
//		while (n > 1) {
//			n--;
//			foo = foo * n;
//		}
//		return foo;
//	}
	public int factorial(int n) {
		if (n == 1) return 1;
		return n * factorial(n-1);
	}
	 
}

