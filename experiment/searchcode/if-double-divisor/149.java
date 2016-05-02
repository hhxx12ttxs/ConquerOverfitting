import java.lang.Math;
public class primes
{
	public static void main(String args[] )
	{
//		System.out.println(getProperDivisorCount(10));
//		System.out.println(getProperDivisorCount(15));
//		System.out.println(getProperDivisorCount(12));
//		System.out.println(getProperDivisorCount(16));
//		int index;
//		int [] testMe;
//		testMe = getProperDivisors(100);
//
//		System.out.printf("The Proper Divisors of 100 are:\n");
//		for (index = 0; index < testMe.length; index++)
//		{
//			System.out.printf("%d, ", testMe[index]);
//		}
//		System.out.printf("\n");
//		System.out.printf("\n");
//		System.out.printf("The sum of these proper divisors is: \n%d\n", getProperDivisorSum(100));
//		System.out.printf("Is it an amicable pair member? %b", isMemberAmicablePair(100));
//		System.out.printf("\n");
//		System.out.printf("The sum of 284's proper divisors is: \n%d\n", getProperDivisorSum(284));
//		System.out.printf("Is it an amicable pair member? %b", isMemberAmicablePair(284));
//		System.out.printf("\n");
	}
	public static int getProperDivisorSum(int number)
	{
		int sum = 0;
		int index;
		int [] divisors;
		divisors = getProperDivisors(number);

		for(index = 0; index < divisors.length; index++)
		{
			sum += divisors[index];
		}

		return sum;
	}

	public static boolean isMemberAmicablePair(int number)
	{
		int partner;
		boolean output;

		partner = getProperDivisorSum(number);
		if (getProperDivisorSum(partner) == number)
		{
			if (partner == number)
			{
				output = false;
			}
			else
			{
				output = true;
			}
		}
		else
		{
			output = false;
		}
		return output;
	}

	private class node
	{
		node link;
		int data;

		public node()
		{
			this.data = 0;
			this.link = null;
		}
	}

	public static int [] getProperDivisors(int number)
	{
		int divisor;
		int [] divisors = null;
		int count = 0;

		divisors = new int[getDivisorCount(number) - 1];

		for (divisor = 1; divisor < number; divisor++)
		{
			if ((number % divisor) == 0)
			{
				divisors[count] = divisor;
				count++;
			}
		}
		return divisors;
	}

	public static int getDivisorCount(int number)
	{
		int divisor;
		int count = 0;
		
		for (divisor = 1; divisor < Math.sqrt(number); divisor++)
		{
			if ((number % divisor) == 0)
			{
				count++;
			}
		}
		count *= 2;							//double count because we stopped at sqrt(number)
		if (Math.pow(Math.sqrt(number),2) == number)
		{
			count++;
		}

		return count;
	}
}

