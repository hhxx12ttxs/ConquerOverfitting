package FactorialRecursion.RichHarkins;

public class FactorialRecursion {
	
	public int CalculateFactorial(int Factorial)
	{
		// Prints value of Factorial variable as it is recursively decremented
		System.out.println(Factorial);
		// Recursion base case - this case ends the processing of the CalculateFactorial function
		if (Factorial == 1) return 1;
		// Reduction step - this step decrements the Factorial variable and recursively calls the CalculateFactorial function
		return Factorial * CalculateFactorial(Factorial-1);
	}

}

