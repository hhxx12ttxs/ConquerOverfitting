package org.saba.tutorial;

/**
 * @author ShabbuDhee
 *
 */
public class Factorial {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(tailFactorial(10));

	}
	
	//head recursion
	public static int factorial(int n) {
		
		if(n==0) {
			return 1;
			
		}
		else {
			return n*factorial(n-1);
		}
		
		
	}
	
	public static int tailFactorial(int n, int sum) {
		System.out.println("sum"+sum);
		System.out.println("n"+n);
		if(n==1) {
			return sum;
		} else {
			return tailFactorial(n-1, sum*n);
		}
	}
	
	public static int tailFactorial(int n) {
		System.out.println("n"+n);
		if(n==1) {
			return 1;
		} else {
			return n*tailFactorial(n-1);
		}
	}

}

