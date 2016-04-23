package InterviewSamples;

class Factorial
{
	static int factorial(int fact)
	{
		int result;

		System.out.println("Fact = " + fact);

		if (fact == 1)
			return 1;

		result = fact * factorial(fact -1);

		return result;
	}

	public static void main(String[] args)
	{
		factorial(4);
	}
}
