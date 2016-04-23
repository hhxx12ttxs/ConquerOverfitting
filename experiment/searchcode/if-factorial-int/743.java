package assignment2;
import acm.program.*;

public class Factorial extends ConsoleProgram {
	
	public void run() {
		int n = readInt("Enter a Number: ");
		println("n! = " + nFactorial(n));	
	}
	
	private int nFactorial(int n) {
		if (n == 0) return(1);
		else return(n * nFactorial(n-1));
	}
}

