//contains methos to claculate factorials and n choose k
public class Perm {	
	private Perm() {
		
	}
	
	public static int choose(int n, int k) {
		return factorial(n)/(factorial(k) * factorial(n - k));
	}
	
	public static int factorial(int x) {
		if (x == 0) {
			return 1;
		}
		int num = 1;
		for (int i = 2; i <= x; i++) {
			num *= i;
		}
		return num;
	}	
}
