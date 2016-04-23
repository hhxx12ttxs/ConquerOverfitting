package euler;

public class Problem034 extends ProblemBase {
	
	private long sum;
	
	public Problem034() {
		super(34, "Digit Factorials");
	}

	@Override
	protected String getSolution() {
		return Long.toString(sum);
	}

	@Override
	protected void solve() {
		final int[] factorials = {
			factorial(0),
			factorial(1),
			factorial(2),
			factorial(3),
			factorial(4),
			factorial(5),
			factorial(6),
			factorial(7),
			factorial(8),
			factorial(9),
		};
		
		for (int i = 10; i < 9_999_999; i++) {
			String intString = Integer.toString(i);
			int factorialSum = intString.chars().map(c -> factorials[c - 48]).sum();
			
			if (factorialSum == i) {
				sum += i;
			}
		}
	}
	
	private int factorial(int n) {
		int factorial = 1;
		for (int i = 1; i <= n; i++) {
			factorial *= i;
		}
		return factorial;
	}

}

