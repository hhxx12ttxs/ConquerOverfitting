public class FactorialPrac {

	public static void main(String... a) {
		System.out.println("factorial" + factorial(7));
	}
	
	public static int factorial(int n) {
		if(n==1) return 1;
		else return n*factorial(n-1);
	}
}
