package uk.ac.qub.recursion;

/**
 * Factorial calculator through use of recursion
 * @author Peter
 *
 */
public class FactorialRecursion {

	public FactorialRecursion() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		
		FactorialRecursion factorialRecursion = new FactorialRecursion();
		System.out.println("Factorial 5! : " + factorialRecursion.factorial(5));

	}
	
	public int factorial(int n) {
		
		int result;
		if(n == 1) {
			return 1;
		}
		result = factorial(n - 1) * n;
		return result;
	}

}


