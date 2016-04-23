/*
* 
* Author: Judith Fuog / Pascal Zaugg
* Matrikelnr.: 09-926-809 / 05-299-425
* Excercice 6-2b
* Last modified: 09.12.2010
* 
*/

public class Factorial {

	public static void main(String[] args) {
		long fac = factorial(Integer.parseInt(args[0]));
		System.out.println(fac);
	}
	
	public static long factorial (int n) {
		if (n == 0 || n == 1) {
			return 1;
		}
		else {
			return n*factorial(n-1);
		}
	}	
}

