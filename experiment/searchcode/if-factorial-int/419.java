public class factorial
{
	public static int factorial(int factor)
	{
		System.out.println("Factorial called with " + factor);
		if (factor != 1)
		{

			return (factor * factorial(factor-1));
		}
		else
		{
			return (1);
		}
	}
	public static void main (String[] args)
	{


		System.out.println(factorial(6));
	}

}
