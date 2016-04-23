package WeekOne;

public class factorial_oddeven {

	public static void main(String[] args) {
		long n = 11, factorial = 1;
		if (n <= 0) {
			factorial=1;
		} else {
			for (int i = 1; i <= n; i++) {
				factorial = factorial * i;
			}
		}
		System.out.println("factorial by iterative : "+factorial);
		long fact=recursive_factorial(n);
		
		System.out.println("factorial by recursive : "+fact);
		oddeven(n);
	}

	private static long recursive_factorial(long n) {
		if(n<=1){
			return 1;
		}else {
			return n * recursive_factorial(n-1);
		}
		
	}
	private static void oddeven(long n){
		if(n<0){
			System.out.println("Negative number");
		}else if(n%2==0){
			System.out.println("Even number");
		}else{
			System.out.println("Odd number");
		}
	}

}

