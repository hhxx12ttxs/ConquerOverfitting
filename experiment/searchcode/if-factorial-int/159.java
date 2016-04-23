package it.tech.skal.algorithms.recursion;

public class Factorial {
	
	public static void main(String a[]){
		Factorial factorial = new Factorial();
		System.out.println(factorial.getFactorial(6));
	}

	private int getFactorial(int i) {
		if (i == 1) {
			return 1;
		} else {
			return i * getFactorial(i - 1);
		}

	}

}


