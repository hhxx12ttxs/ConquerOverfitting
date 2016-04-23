package factorial.controller;

public class FactorialController
{
	private double factorial(int currentNumber)
	{
		double factorialValue =0;
		if(currentNumber >= 0)
		{
			if(currentNumber <= 1)
			{
				return 1;
			}
			else
			{
				return factorial(currentNumber-1)*currentNumber;
			}
		}
		return factorialValue;
	}
	
	public void start()
	{
		for(int count = 0; count < 200; count++)
		{
			System.out.println(factorial(count));
		}
		
	}

}

