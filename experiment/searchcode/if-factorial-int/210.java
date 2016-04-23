public class Factorial
{
	public static void main(String args[])
	{
		System.out.println(factorial(Integer.parseInt(args[0])));

	}
	public static int factorial(int n)
	{
		if(n<=1)
			return 1;
		else
			return n*factorial(n-1);
	}

}

