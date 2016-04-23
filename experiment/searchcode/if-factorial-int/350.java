package com.taijia.chapter2;

/**
 * 阶乘
 * 
 * @author taijia
 * @since 2014年9月27日
 * 
 */
public class Factorial {
	
	public static void main(String[] args) {
		System.out.println(factorialPlus(10));
	}

	public static int factorial(int n) {
		if (1 == n) {
			return n;
		} else {
			return n * factorial(n - 1);
		}
	}
	
	public static int factorialPlus(int n) {
		if(1 == n) {
			return factorial(n);
		} else {
			return factorial(n)+factorialPlus(n-1);
		}
	}
	
}

