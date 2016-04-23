package util;

public class Factorial {

	public static long factorial(int n) throws NumberFormatException
	{
		if(n < 0)
		{
			throw new NumberFormatException();
		}
		return n == 0 ? 1 : n * factorial(n - 1);
	}
}

