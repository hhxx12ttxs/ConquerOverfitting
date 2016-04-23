/*
Factorial Challenge
http://www.cprogramming.com/challenges/factorial.html

Here's a challenge that's a bit more mathematical in nature. Write a program that determines the number of
trailing zeros at the end of X! (X factorial), where X is an arbitrary number. For instance, 5! is 120, so
it has one trailing zero. (How can you handle extremely values, such as 100!?) The input format should be
that the program asks the user to enter a number, minus the !.
 */

package com.lucaspate.factorial;

import java.math.BigInteger;

public class Factorial {
	private int n;
	private String nFactString;

	public Factorial() {
		setN(0);
	}
	
	public Factorial(int n) {
		setN(n);
	}
	
	private void findFactorial() {
		BigInteger nFact;
		
		if (n < 0)
			nFactString = "undefined";
		else if (n == 0)
			nFactString = "1";
		else {
			nFact = BigInteger.valueOf(1);
		
			for (int i = 2; i <= n; i++)
				nFact = nFact.multiply(BigInteger.valueOf(i));
			
			nFactString = nFact.toString();
		}
	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		nFactString = "";
		this.n = n;
		findFactorial();
	}

	public String getnFact() {
		return nFactString;
	}
	
	public int getTrailingZeros() {
		int numZeros = 0;
		int index = nFactString.length() - 1;
		
		while (nFactString.charAt(index) == '0') {
			numZeros++;
			index--;
		}
		
		return numZeros;
	}
	
	

}

