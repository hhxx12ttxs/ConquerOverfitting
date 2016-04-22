
public class Factorial {

	public static void main(String[] args) {
		System.out.println(factorialOf(10));

	}
	
	public static int factorialOf(int number) {
		
		if ( number == 0 || number ==1)
			return 1;
		
		int factorial = number * factorialOf(number -1);
		
		return factorial;
	}

}

