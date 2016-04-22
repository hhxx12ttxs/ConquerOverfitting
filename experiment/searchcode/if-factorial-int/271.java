package me.sumartha.factorial;

public class Factorial {

	public float getFactorialOf(int number) {	
		if (number < 2) return 1; 
		return number * getFactorialOf(number-1);
	}
}

