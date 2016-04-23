class factorial
{
	public static void main(String[] args)
	{
		int i = 20;
		for(int x = 0; x<i; x++ ){
			System.out.println(factorial(x) + ",");
		}
		

	}

	public static int factorial(int n)
	{
		if(n > 1)
		{
			return n * factorial(n-1);
		}
		else if(n >= 0)
		{
			return 1;
		}
		return -1;
		
	}
}

