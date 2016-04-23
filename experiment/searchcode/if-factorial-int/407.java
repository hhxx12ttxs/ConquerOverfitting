package other;

public class Factorial {
	
	public int factorial(int num) {
		if (num ==1 || num == 0) {
			return 1;
		} else {
			return num*factorial(num-1);
		}
		
	}
	
	public static void main(String[] args) {
		System.out.println(new Factorial().factorial(5));
	}

}

