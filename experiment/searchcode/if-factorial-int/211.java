
public class Factorial {

	// factorial
	public static void main(String[] args) {
		Factorial f = new Factorial();
		int number = f.factorial(5);
		System.out.println(number);
	}
	
	public int factorial(int number) {
		
		if( number <= 1 ) {
			return 1;
		}
		else {
			return number * factorial(number-1);
		}
	}
}

