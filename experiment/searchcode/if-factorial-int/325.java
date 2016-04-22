package Algorithm;

public class Factorial {
	
	public static int factorialS(int n) {
		int product = 1;
		for(int i = 2; i <= n; ++i) {
			product *= i;
		}
		return product;
	}
	public int factorialR(int n) {
		return (n==1) ? 1 : n * factorialR(n-1);
	}
	
	public int factorialN(int n) {
		if(n < 0) {
			throw new IllegalArgumentException("Cant have a negative factorial!");
		}
		int total = 1;
		while(n > 1) {
			total *= n;
			--n;
		}
		
		return total;
	}
	
	public int factorialN2(int n) {
		if(n < 0) {
			throw new IllegalArgumentException("Cant have a negative factorial!");
		}
		
		int total = 1;
		for(int i = 2; i <= n;  ++i) {
			total *= i;
		}
		return total;
	}
	public static void main(String args[]) {
		Factorial f = new Factorial();
		for(int i = 0; i < 10; ++i) {
			System.out.println(f.factorialS(i));
			//System.out.println(f.factorialN2(i));
			//System.out.println(f.factorialR(i));
		}

		//System.out.println(f.factorialR(20));
	}
}

