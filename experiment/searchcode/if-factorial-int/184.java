class fact
{
	public
	static int factorial(int n)
	{
		if(n == 1)
			return 1;
		else if(n == 0)
			return 1;
		else
			return (n * factorial(n - 1));
	}
	public static void main(String args[])
	{
		//fact 
		int n;
		n = 5;
		n = factorial(n);
		System.out.println("Factorial: " + n);
	}
}

