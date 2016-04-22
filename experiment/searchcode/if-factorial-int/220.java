package com.interview.algorithms;

public class Factorial {

	public void generateFactorial(int kth) {
		System.out.println("Factoial for " + kth + "  no is : "
				+ recFactorial(kth));
		System.out.println("Factoial for " + kth + "  no is : "
				+ itertaiveFactorial(kth));

	}

	private int recFactorial(int n) {
		if (n <= 0) {
			return 1;

		} else {
			return n * recFactorial(n - 1);
			// System.out.print(fac + ",");
			// return fac;
		}

	}

	private long itertaiveFactorial(int num) {
		long result = 1;
		if (num == 0) {
			return 1;
		} else {
			for (int i = 2; i <= num; i++) {
				result *= i;
			}
			return result;
		}

	}



	public static void main(String[] args) {
		Factorial factorial = new Factorial();
		factorial.generateFactorial(5);		

	}

}

