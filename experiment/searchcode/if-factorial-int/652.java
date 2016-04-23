package vjezbe;

import java.util.Scanner;

public class Zadaca3 {

	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);

		// Asking user to enter desired no
		System.out.println("Enter the number ");
		System.out.println();
		int n = input.nextInt();

		// Calculating factorial of entered number
		long factorial = 1;
		String factorialString = factorial + "";
		for (int i = 1; i <= n; i++) {
			factorial *= i;
			factorialString = factorial + "";
			
			for (int j = factorialString.length() - 1; j >= 0; j--) {
				if (factorialString.charAt(j) == '0')  {
					factorial = factorial / 10;
				}
			}
		}

		factorialString = factorial + "";
		

		// Printing only two last digit of driven number
		int digit = factorialString.length();
		System.out.println(factorialString);
		int counter = 0;
		String resultRev = "", result = "";
		for (int k = digit-1; k >= 0; k--) {
			if (factorialString.charAt(k) != '0')  {
				resultRev += factorialString.charAt(k);
				counter++;
			}
			if (counter > 2) {
				break;
			}
		}
		
		for (int l = 1; l >= 0; l--) {
			result += resultRev.charAt(l);
		}
		
		System.out.println(result);
		input.close();

	}

}

