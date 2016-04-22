public class Factorial {
	public Factorial () {}
	
	public int getFactorial(int x) {
		if (x == 1) {
			return 1;
		} else {
			return x * getFactorial(x-1);
		}
	}
	
	public static void main(String[] args) {
		Factorial num = new Factorial();
		
		System.out.println(num.getFactorial(5));
	}
}

