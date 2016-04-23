
public class Problem02 {

	public static void main(String[] args) {
		System.out.println(factorial(6));
	}
	
	public static int factorial(int n) {
		return factorial(n,1);
	}
	
	public static int factorial(int n, int counter) {
		if (n == 0)
			return counter;
		else {
			counter = counter * n;
			n--;
			return factorial(n,counter);
		}
	}
}
