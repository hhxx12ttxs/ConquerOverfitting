// FactorialRec - calculating the Factorial with a recursive algorithm

public class FactorialRec {
	public static void main(String[] args) { // Frame for reading a value 
		if (args.length > 0) {			// from the command line
			FactorialRec f = new FactorialRec(Integer.parseInt(args[0]));
		}
		else
			System.out.println("use syntax: java FactorialRec n");
		}
	public FactorialRec(int n) { // Printing out the result
			System.out.println(n + "! = " + factorial(n));
	}
	private int factorial(int n) { // recursive algorithm
			if (n==1) return 1;
			else 
 			  return n * factorial(n-1);
		}	
}

