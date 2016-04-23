package algorithms;

public class Factorial {

	public static void factorial(int num) {
		int fact = 1;
		for (int i = 1; i <= num; i++) {
			fact = fact * i;
		}
		System.out
				.println("Factorial.factorial() for " + num + " is = " + fact);
	}

	public static int factorialRec(int num) {
		if (num == 0) {
			return 1;
		}
		return num * factorialRec(num - 1);
	}

	public static void main(String[] args) {
		Factorial.factorial(5);
		Factorial.factorial(6);

		System.out.println("Factorial Test for 5 = "
				+ Factorial.factorialRec(5));
		System.out.println("Factorial Test for 6 = "
				+ Factorial.factorialRec(6));

	}

}

