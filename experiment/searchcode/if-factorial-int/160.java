package test1;
public class Factorial
{
	public static int factorial(int n)
	{
		if (n < 1)
			return 1;
		else
			return factorial(n - 1) * n;
	}

	public static void main(String[] args)
	{
		for (int i = 1; i < 6; i++)
		{
			System.out.println(factorial(i));
		}
	}

}

