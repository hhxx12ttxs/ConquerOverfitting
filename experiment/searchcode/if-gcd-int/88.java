package e1.s100502515;

import java.util.Scanner;

public class E12 {
	public static void main(String[] args) {

		Scanner scanner = new Scanner(System.in);
		System.out.print("a = ");
		int a = scanner.nextInt();
		System.out.print("b = ");
		int b = scanner.nextInt();
		if (a >= b)
			System.out.println("gcd(" + a + ", " + b + ") = " + gCD(a, b));
		else {
			System.out.println("gcd(" + a + ", " + b + ") = " + gCD(b, a));
		}
		scanner.close();

	}

	private static int gCD(int a, int b) {
		int result = 0;
		for (int i = b; i > 0; i--) {
			if (a % i == 0 && b % i == 0) {
				result = i;
				break;// if a result is found, then the progress is over.
			}
		}
		return result;
	}
}

