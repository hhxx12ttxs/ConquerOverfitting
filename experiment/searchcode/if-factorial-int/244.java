package recursive.factorial;

public class Factorial {
	public static int runFactorial(int number) {
		if (number == 0) {
			return 1;
		} else {
			return (number * runFactorial(number - 1));
		}
	}
	
}

