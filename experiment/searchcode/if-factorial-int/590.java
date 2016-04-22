package com.ram.home;

public class FactorialTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FactorialTest ft = new FactorialTest();
		
		for (int i=0;i< 10;i++) {
			System.out.println("Factorial for " + i + " is " + ft.findFactorial(i));
		}

	}
	
	public int findFactorial(int n) {
		if (n == 0 || n == 1) {
			return n;
		} else {
			return n * findFactorial(n-1);
		}
	}

}

