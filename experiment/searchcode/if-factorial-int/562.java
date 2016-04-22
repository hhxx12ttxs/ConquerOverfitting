public class Euler53 
{
	public static void main(String[] args)
	{
		int counter = 0;

/*		
		for(int r = 1; r <= 100; r++)
		{
			for(int n = 1; n <= 100; n++)
			{
				if(n == r)
				{
					n++;
				}
				System.out.println(n + " " + r);
				int value = ((Euler53.factorial(n))/((Euler53.factorial(r)) * (Euler53.factorial(n - r))));
				if(value >= 1000000)
				{
					counter++;
				}
			}
		}
*/
		
		//System.out.println(counter);
		//System.out.println(((Euler53.factorial(35))/((Euler53.factorial(1)) * (Euler53.factorial(34)))));
		System.out.println(((Euler53.factorial(35))));
		System.out.println(((Euler53.factorial(1))));
		System.out.println((Euler53.factorial(100)));
	}
	
	public static long factorial(int n)
	{
		long value = 1;
		
		for(int i = 1; i <= n; i++)
		{
			value = value * i;
		}
		
		return value;
	}
}
