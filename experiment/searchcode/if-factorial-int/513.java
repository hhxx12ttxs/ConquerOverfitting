/*solving the factorial using recursion
*/

public class FactorialR
{
	public static void main(String [] args)
	{
		System.out.println(factorial(Integer.parseInt(args[0])));
	}
	// n!
	private static int factorial(int n)
	{
		// return variable that accumulates factorial sum
		int f = 1;
		
		// base case
		if(n==0 || n==1) return f;
		// continues calling itself until base case found
		else f = n*factorial(n-1);
		// return factorial sum
		return f;
	}
}
