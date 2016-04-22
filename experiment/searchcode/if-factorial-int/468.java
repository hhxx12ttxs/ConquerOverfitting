import java.math.BigInteger;

/**
 * Created with IntelliJ IDEA.
 * User: Oleg
 * Date: 27.03.2014
 * Time: 14:24
 */
public class RecursionFactorial {
	public static void main(String[] args) {
		Factorial myFactorial = new Factorial();

		for (int i = 0; i <= 10; i++) {
			System.out.println("Factorial of " + i + " is: " + myFactorial.factorial(i));
		}

	}
}

class Factorial {
	BigInteger factorial(int i) {
		BigInteger result;
		if (i <= 1) {
			return new BigInteger("1");
		}
		result = factorial(i - 1).multiply(new BigInteger(String.valueOf(i)));
		return result;
	}
}
