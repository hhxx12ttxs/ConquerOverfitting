class Factorial {
	private int i;
	private int sum;
	public Factorial(int n) { 
		sum = n;
		System.out.print(n+"! = "+n);
		for (i=1; i<n; i++ ) {
			if (i < n-1) {
				System.out.print(""+"x"+(n-(i)));
			} else {
				System.out.print("x"+"1");
			}
			sum = sum*(n-i);
		}
		System.out.println(" = "+sum);
	}
}

public class SumFactorialV1_0 {
	public static void main(String[] args) {
		new Factorial(1);
		new Factorial(2);
		new Factorial(3);
		new Factorial(4);
		new Factorial(5);
	}
}

