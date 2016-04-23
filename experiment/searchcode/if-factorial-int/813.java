package jrout.tutorial.corejava.recursion;

public class Factorial {

	public static void main(String[] args) {
		System.out.println(factorial(5));
	}
	
	public static int factorial(int val){
		int factValue = 0;
		if(val == 1) {
			return 1;	
		} else{
			factValue = val * factorial(val-1); // 3 * 2 * 1
		}
		return factValue;
	}
}
