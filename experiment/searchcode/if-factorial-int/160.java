public class Factorial{
	public static int factorial(n){
		if(n <= 1){
			return 1;
		}
		return n * factorial(n-1);
	}
}

