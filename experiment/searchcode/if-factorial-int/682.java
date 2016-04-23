public class FactorialPrime
{

	public FactorialPrime()
	{
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] Args)

	{

		System.out.println("2 is a rpime number" + isPrime(2));
		System.out.println("The factorial  of3 is " + factorial(3) + "");

		System.out.println("The factorial prime of3 is " + isFactorialPrime(3)
				+ "");
		System.out.println("The factorial prime of 7 is " + isFactorialPrime(7)
				+ "");
		System.out.println("The factorial prime of 11 is "
				+ isFactorialPrime(11) + "");
		// 2 3 7 8 11 721
	}

	public static boolean isPrime(int n)
	{
		// negatives out of the way
		if (n <= 0)
		{
			return false;
		}

		// only divisible by 1 or itself
		for (int i = n - 1; i > 1; i--)
		{
			if (n % i == 0)
			{
				return false;
			}
		}

		// if all else fails
		return true;
	}

	public static int isFactorialPrime(int n)
	{
		/*
		 * 1 if its argument is a factorial prime number, otherwise it returns 0
		 */

		// if not prime argument return 0

		if (isPrime(n) == false)
		{
			return 0;
		}

		int i = 0;
		// now check if n -1 is factorial of something
		while (factorial(i) < n - 1)
		{
			i++;
		}

		if (factorial(i) == n - 1)
		{

			return 1;
		} else
		{
			return 0;
		}

	}

	public static int factorial(int n)
	{
		if (n == 0)
		{
			return 1;
		}
		return n * factorial(n - 1);
	}

}

