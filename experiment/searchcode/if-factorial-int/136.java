package lesson_4;

public class frame_4_10 {

	public static void main(String[] args) {
		
		int a = 1, b = 2, c = 0, d = 4, e = 5, i = -2, f = 10, g = 15;
		
		System.out.println(a + "!" + " = " + factorial(a));
		System.out.println(b + "!" + " = " + factorial(b));
		System.out.println(c + "!" + " = " + factorial(c));
		System.out.println(d + "!" + " = " + factorial(d));
		System.out.println(e + "!" + " = " + factorial(e));
		System.out.println(i + "!" + " = " + factorial(i));
		System.out.println(f + "!" + " = " + factorial(f));
		System.out.println(g + "!" + " = " + factorial(g));

	}
	static long factorial (int number){
		if (error(number) == true) {
		long factorial = 1;
		for (int i = 1; i<=number; i++) {
			
			factorial = factorial * i;
		}
		return factorial;}
		else return -1;
	}
	
	static boolean error (int number) {
		if (number >= 0) {
			return true;
		}
		return false;
	}
}
