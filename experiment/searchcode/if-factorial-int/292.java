package edu.wbqa.ut;

public class Factorial {

	public static int getFactorial(int i) {
		if (i <= 1) {
			return i;
		}
		return i * getFactorial(i - 1);

	}

}

