/**
 *
 * Factorual of 5 is 5x4x3x2x1 = 120
 *
 */
public class Factorial {

	public static void main(String[] args) {

		System.out.println(factorial(4));
	}
	
	private static int factorial(int a){
		
		if(a==1) {
			return 1;
		}
		
		return a * factorial(a-1);
	}

}

