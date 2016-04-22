package recursion;

public class Factorial {

	public static void main(String[] args) {
		int x = 10;
		System.out.println(factorial(x));
	}
	
	public static int factorial(int x){
		if(x == 1){
			return x;
		}
		if(x == 0){
			return x;
		}
		return x * factorial(x-1);
	}

}

