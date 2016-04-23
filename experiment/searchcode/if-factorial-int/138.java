package com.main.recursive;

public class Factorial {

	public int factorial(int i) {
		if (i == 0) return 1;
		else if (i == 1)	return 1;
		else
			return i * factorial(i - 1);
	}

}

