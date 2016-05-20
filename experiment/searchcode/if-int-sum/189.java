package com.nickblomberg.projecteuler.solutions;

/**
 * Problem: 1
 * 
 * If we list all the natural numbers below 10 that are multiples of 3 or 5,
 * we get 3, 5, 6 and 9. The sum of these multiples is 23.
 * 
 * Find the sum of all the multiples of 3 or 5 below 1000.
 * 
 * @author Nick Blomberg
 *
 */
public class Problem1 {
	
	public int solve(int target) {
		int sum = 0;
		
		for(int i = 0; i < target; i++) {
			
			if(i % 5 == 0 || i % 3 == 0) {
				sum += i;
			}
			
		}
		return sum;
	}
	
	public static void main(String[] args) {
		Problem1 p = new Problem1();
		int sum = p.solve(1000);
		
		System.out.println("Total sum: " + sum);
	}
	
}

