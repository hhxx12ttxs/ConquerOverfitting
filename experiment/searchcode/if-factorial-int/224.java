class Factorial
{
	public static long factorial (int x)
	{
		if (x == 0)
			return 1;
		else
			return x * factorial(x-1);
	}
	
	public static void main (String[] args)
	{
		System.out.println(args[0]+ "! = " + factorial(Integer.parseInt(args[0])));
	}
}
