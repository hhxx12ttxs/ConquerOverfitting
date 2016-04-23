public class Factorial {

	public static int factorial(int n)
	{
	if(n == 1)
	{	
		return 1;
	 }
	else
	{ 
		return n * factorial(n-1); 
	}
	}
	
	
	public static int rig(int n)
	{
	if ( (n == 0) )
	{
	return 5;
	}
	else if ( n == 1)
	{
	return 8;
	}
	else
	{
	return rig(n - 1) - rig(n - 2);
	}
	}
}

