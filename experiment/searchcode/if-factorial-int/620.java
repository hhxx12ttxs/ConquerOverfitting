package com.sumon.homework;

public class Factorial {
	public int getFactorial(int num) {
		if (num==1) {
			return 1;
		}
		else {
			return num*getFactorial(num-1);
		}
	}
}

