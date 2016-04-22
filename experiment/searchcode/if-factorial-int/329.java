package samplePrograms;

public class Factorial {

	static int n=6;
	public static void main(String arg[])
	{
		System.out.println(factorial(n)) ;
		System.out.println("Sum of factorial :"+sumOfFactorial(n));
	}


	public static int  factorial(int n )
	{
		if(n==0)
			return 1;
		else
		return n* factorial(n-1);
	}
	
	public static int sumOfFactorial(int n)
	{
		if(n==0)
			return 0;
		else
		return n+ sumOfFactorial(n-1);
	}

}

