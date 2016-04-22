package recursion;

import java.util.Scanner;

public class Factorial {

	public int Factorial(int n) {
		if (n > 0) {
			return Factorial(n - 1) * n;
		} else {
			return 1;
		}
	}

	public static void main(String args[]) {
		System.out.println("��������Ҫ�ݹ��������:");
		Scanner sc = new Scanner(System.in);
		int number = sc.nextInt();
		Factorial factorial = new Factorial();
		int result = factorial.Factorial(number);
		System.out.println("�ݹ���"+result);
	}

}
