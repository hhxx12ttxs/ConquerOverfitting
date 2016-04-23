package com.company.target.collections.ch03;

/**
 * �򵥵ݹ�:
 * 		ʵ�ֽ׳�
 */
public class Factorial {

	public static void main(String[] args) {
		Factorial factorial = new Factorial();
		System.out.println(factorial.factorial(3));
	}
	
	public long factorial(int n) {
		if (n < 0) {
			throw new IllegalArgumentException();
		}
		
		if (n <= 1) {
			return 1;
		}
		
		return n * factorial(n - 1);
	}
	
}

