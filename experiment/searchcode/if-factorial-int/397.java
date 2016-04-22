/**
* Recursivly calculates the factorial of a given number. 
* E.g 4 factorial = 4 X 3 X 2 X 1
* Day 5 Question 2a
*/
public class Factorial {
	public int factorialGenerator(int x) {
		if (x == 0) {
			return 1;
		}  else {
			int result = factorialGenerator(x - 1) ;
			result = x * result;
			return result;
		}
	}

	public static void main (String[] args) {
		Factorial a = new Factorial();
		System.out.println(a.factorialGenerator(4));
	}
}

