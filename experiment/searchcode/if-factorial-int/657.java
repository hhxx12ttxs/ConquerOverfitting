// �����f�wӋ���A��

public class p78_01_RecursiveFactorial {
	public static int factorial(int n) {
		int result;

		if (n == 0) result = 1;
		else result = n * factorial(n - 1);
		return result;
	}
	public static void main(String[] args) {
		// 1~6 ���A��

		for (int ix = 1; ix <= 6; ++ix) {
			System.out.println(ix + "! = " + factorial(ix));
		}
	}
}

