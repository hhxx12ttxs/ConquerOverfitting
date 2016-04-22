
public class FactorialV2 {

	public static void main(String[] args) {
		
		int n;
		if (args.length > 0) {
		        n = Integer.parseInt(args[0]);
		        int result = Factorial(n);
		        System.out.println(result);
		}
	}
	
	public static int Factorial(int n)
	{
		if (n == 0)
			return 1;
		else
			return n * Factorial(n-1);
	}


}

