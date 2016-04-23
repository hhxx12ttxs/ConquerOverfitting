package com.naironics.factorial;

import java.util.Scanner;

public class Factorial {

	public static int getFactorial(int number) {

		if (number == 0) {
			return 1;
		}

		return number * getFactorial(number - 1);
	}

	public static void main(String args[]) {

		System.out.println("Enter any number to get its factorial :");
		int number = new Scanner(System.in).nextInt();

		System.out.println("Factorial of " + number + " is "
				+ getFactorial(number));

	}

}

