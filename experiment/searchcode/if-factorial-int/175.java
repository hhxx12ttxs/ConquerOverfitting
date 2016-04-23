package coderbytejava;

/**
 * Write the function FirstFactorial(number) to take the number parameter being passed 
 * and return the factorial of it. 
 *
 */

public class FirstFactorial {

	public static void main(String[] args) {

		int number = 0;
		
		System.out.println("functionFirstFactorial: " + functionFirstFactorial(number));
	}
	
	public static int functionFirstFactorial(int number) {
		int factorial = 1;
		
		for (int i = 1; i <= number; i++) {
			factorial = factorial * i;
		}
		
		return factorial;
	}
	
	/*public static int functionFirstFactorial(int number) {
		
		if (number <= 1) {
			return 1;
		}
		
		else {
			return number * functionFirstFactorial(number-1);
		}
	}*/
}
