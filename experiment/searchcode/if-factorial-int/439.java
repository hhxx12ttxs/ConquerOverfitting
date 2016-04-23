package factorial;

public class Factorial {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println( factorial(6) );
	}

	private static int factorial(int numero) {
		if (numero < 2) return 1;
		else return numero * factorial(--numero);
	}
	
}

