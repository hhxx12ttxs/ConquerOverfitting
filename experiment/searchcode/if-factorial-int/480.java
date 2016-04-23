package edu.wbqa.ut;

public class Factorial {
	
	public static int getFactorial(int i)
	{
		int returnValue = 0;
		
		if(i <= 1)
		{
			returnValue = i;
		}
		else
		{
			returnValue = i * getFactorial(i-1);
		}
		
		
		return returnValue;
	}

}

