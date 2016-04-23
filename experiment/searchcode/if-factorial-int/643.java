package org.wittydev.math;

public class MiscMathUtil {
	
	/**
	 *  Calucaltes the factorial of a given positive number n
	 *  factorial(n) = 1*2*3*4*...*n
	 *  factorial(0) = 1
	 * @exception IllegalArgumentException if n is not negative 
	 * @param n   
	 * @return the factorial of n 
	 */
	
	public static long factorial ( int n ){
		if (n<0) throw new IllegalArgumentException("factorial can't be calculated for negative numbers");
		if (n==0) return 1;
		return n*(factorial(n-1));
	}

}

