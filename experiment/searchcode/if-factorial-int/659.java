package codingbat.recursion1;

public class Factorial
{
	public static void main(String[] args) 
	{
	}

	/**
	 * Given n of 1 or more, return the factorial of n,
	 * which is n * (n-1) * (n-2) ... 1. 
	 * Compute the result recursively (without loops).
	 *
	 * factorial(1) → 1
	 * factorial(2) → 2
	 * factorial(3) → 6
	 */
	public int factorial(int n) 
	{
		if (1 == n || 0 == n)
		{
			return 1;
		}
		else
		{
			return n*factorial(n-1);  
		}
	}
}

