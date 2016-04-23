package combination;

public class CountCombination {
	private static int combination(int m, int n) {

		return factorial(m) / (factorial(n) * factorial(m - n));
	}
	public static int factorial(int n) {
		if (n == 0 || n == 1)
			return 1;
		else
			return n * factorial(n - 1);
	}
	
	public static void main (String[] args) {
		System.out.println(combination(6, 4));
	}
}

