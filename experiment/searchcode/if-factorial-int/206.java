package sudas.study.example.zero;

public class CalculateFactorial {

	public static void main(String[] args) {
		System.out.println(new Facto().factorial(4));

	}

}

/*
 * calculate the factorial of a number factorial theory factorial of 4 --
 * 4*3*2*1 i.e. if n=4 Formulae is n!=n*(n-1)! therefore 4!=4*(4-1)(3-1)(2-1)
 * [in mathematics factorial of 0 is 1]
 */

class Facto {
	public int factorial(int n) {
		int factorialOf;
		if (n == 1)
			return 1;
		factorialOf = factorial(n - 1) * n;

		return factorialOf;

	}

}

