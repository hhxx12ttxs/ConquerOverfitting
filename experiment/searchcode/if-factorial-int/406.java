package test.algorithms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class Factorial {

	@Test
	public void Test()
	{
		int temp = 10;
		int factorial = factorial(temp);
		System.out.println("Factorial recursive " +  temp + " = " + factorial);
		factorial = factorial2(temp);
		System.out.println("Factorial iterative " +  temp + " = " + factorial);	
	}
	
	public int factorial(int n)
	{
		if(n <= 1)
		{
			return 1;
		}
		return n * factorial(n - 1);
	}
	
	public int factorial2(int n)
	{
		int s = 1;
		for(int i = n; i > 0; i--)
		{
			s *= i;
		}
		return s;
	}
}

