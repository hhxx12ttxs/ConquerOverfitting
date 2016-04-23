package factorial;

/*
 * Find factorial of a given number.
 */
public class FactorialFinder {

	/*
	 * Return factorial of a given number using recursion.
	 */
	public long findFactorialRecursively(final int num) {

		if (num == 1) {
			return 1;
		}
		long factorial = num * findFactorialRecursively(num - 1);

		return factorial;

	}

	/*
	 * Return factorial of a given number without using recursion.
	 */
	public long findFactorial(final int num) {
		long factorial = 1;
		for (int i = num; i > 1; i--) {
			factorial = factorial * i;
		}

		return factorial;

	}

}

