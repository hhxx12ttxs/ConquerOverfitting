package DieRoll;

public class Factorial
{
	public static int factorial(int n)
	{
		int result = 1;
		for (int i = 1; i<=n; i++)
		{
			result = result * i;
		}
		
		return result;
	}
	public static int fact(int n)
    {
        int result;

       if(n==1)
         return 1;

       result = fact(n-1) * n;
       return result;
    }
	public static double power(double a, double n)
    {
		
		Math.pow(a, n)
        double result;

       if(n==0 || a==1)
         return 1;

       result = power(a*((Math.pow(a, n-1))));
       return result;
    }
	
	public int fibo(int n)
	{
		return n;
		
	}
	public static void main(String[] args)
	{
		System.out.println(factorial(5));
		System.out.println(fact(5));
	}
}
