package ce1002.E1.s100502203;

import java.util.Scanner;

public class E12 {
	public static int GCD(int a, int b) { // ?????
		if(b == 0)
			return a;
		else
			return E12.GCD(b, a % b);
	}

	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		int a, b;
		System.out.print("a=");
		a = input.nextInt();
		System.out.print("b=");
		b = input.nextInt();
		System.out.printf("gcd(124,36)=%d", E12.GCD(a, b));
	}
}

