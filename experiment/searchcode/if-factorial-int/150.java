package com.taro;

public class Factorial {

	public int factorial(int num) {
		if (num > 1) {
			return num * factorial(num - 1);
		} else {
			return 1;
		}
	}
}
