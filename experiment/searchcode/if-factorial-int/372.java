package recursion;

public class FactorialGenerator {
	public long nthFactorial(int n) {
		return computeFactorialRecurse(n);
	}
	
	private long computeFactorialRecurse(int n) {
		assert n >= 0 : "n must be nonnegative";
		if (n == 0) 		return 1;
		return n * computeFactorialRecurse(n-1);
	}
	
	public static void main(String[] args) {
		FactorialGenerator g = new FactorialGenerator();
		for (int i = 0; i < 22; i++) {
			long result = g.nthFactorial(i);
			System.out.println(result);
		}			
	}
}

