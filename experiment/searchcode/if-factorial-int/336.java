package activity1;

public class PizzaProject {
	private static final int s = 5;
	private static final int k = 3;
	public static void main(String[] args) {
		System.out.println(factorial(s) / (factorial(k) * factorial(s-k)));
	}
	public static int factorial(int input) {
		if (input == 0) {
			return 1;
		} 
		return input * factorial(input-1);
	}

}

