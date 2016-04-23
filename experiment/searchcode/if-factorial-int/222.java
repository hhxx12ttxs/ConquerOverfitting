package com.codingbat.recursion1;

import static org.junit.Assert.*;

import org.junit.Test;

/*
 Given n of 1 or more, return the factorial of n, which is n * (n-1) * (n-2) ... 1. 
 Compute the result recursively (without loops).

factorial(1) → 1
factorial(2) → 2
factorial(3) → 6
 */

public class Factorial {
	
	public int factorial(int n) {
		if(n == 0) {
			return 1;
		}
		return factorial(n - 1) * n;
	}
	
	@Test
	public void testFactorial() {
		assertEquals(1, factorial(1));
		assertEquals(2, factorial(2));
		assertEquals(6, factorial(3));
	}


}

