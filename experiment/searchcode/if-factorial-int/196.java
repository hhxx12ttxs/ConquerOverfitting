
public class Factorial {

	public static void main(String[] args) {
		System.out.println(factorial(6));
	}

	public static int factorial(int n) {

		// Suponemos que n es mayor o igual a 1
		if (n > 1) {
			return n * factorial(n - 1);
		} else {
			return 1;
		}

	}

}


















