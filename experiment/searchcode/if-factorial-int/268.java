public class Factorial {

	public static int factorial(int number) {
		
		int factorial = 1;
		
		for(int i = 1; i <= number; i++) {
			factorial = factorial * i;
		}
		
		return factorial;
		
	}
	
	public static int recursiveFactorial(int number) {
		if (number == 0) {
			return 1;
		}

		else {
			return (number * recursiveFactorial(number - 1));
		}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(recursiveFactorial(10));
		
		System.out.println(factorial(10));
	}

}


