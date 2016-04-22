package lesson3;

public class HW4_factorial {

	public static void main(String[] args) {
		System.out.println(factorial(0));
		System.out.println(factorial(1));
		System.out.println(factorial(2));
		System.out.println(factorial(3));
		System.out.println(factorial(4));
		System.out.println(factorial(5));
		System.out.println(factorial(6));
		System.out.println(factorial(7));
		System.out.println(factorial(8));
		System.out.println(factorial(9));
		System.out.println(factorial(10));
		System.out.println(factorialR(10));
		System.out.println(factorialR(11));
		
	}
	
	static long factorial(int i) {
		if ( i <= 1 ) {
			return 1;
		}
		
		int res = 1;
		for ( int n = 1; n <= i; n++ ) {
			res *= n;
		}
		
		return res;
	}
	
	static long factorialR(int i) { //Maximum q-ty recursive calls should be not more 256 
		if ( i <= 1 ) {
			return 1;
		}
		
		return i * factorialR(i - 1);
	}

}

