package ce1002.s101502512;

import java.util.Scanner;

public class Q1 {
	public static void main(String[] args) {
		// ????
		while (true) {
			System.out.println("Please choose the method you want to use:");
			System.out.println("1.toFahrenheit");
			System.out.println("2.toCelcius");
			System.out.println("3.Exit");
			// ????
			Scanner input = new Scanner(System.in);
			int i = input.nextInt();

			if (i == 1) {
				System.out.println("Please input the temperature:");
				double x = input.nextDouble();
				System.out.println(x + " in Celcius is equal to "
						+ toFahrenheit(x) + " in Fahrenheit.\n");
			}
			if (i == 2) {
				System.out.println("Please input the temperature:");
				double x = input.nextDouble();
				System.out.println(x + " in Fahrenheit is equal to "
						+ toCelcius(x) + " in Celcius.\n");
			}
			if (i == 3) {
				System.out.println("Good Bye");
				break;
			}
		}// ????
	}

	// ?????
	public static double toFahrenheit(double x) {
		x = x * 9 / 5+ 32;
		return x;
	}

	// ?????
	public static double toCelcius(double x) {
		x = (x - 32) * 5 / 9;
		return x;
	}
}

