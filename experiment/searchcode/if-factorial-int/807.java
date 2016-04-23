package com.factorial.main;


/**
 * This is a small utility class to show two different ways to work out the factorial of a number.
 * It could be easily change to show a single numbers factorial, but for this demonstration i showed the first n numbers. 
 * @author David McFadden
 *
 */
public class Factorial 
{

	
	public static void main(String[] args) 	{
		int maxInputNumber = 10;
		for(int inputNumber = 1;inputNumber <= maxInputNumber; inputNumber++){
			int iterativeFactorialNumber = iterativeFactorial(inputNumber);
			int recursiveFactorialNumber = recursiveFactorial(inputNumber);
			
			//print the 2 factorial numbers retuned from the appropriate methods
			System.out.println("Iterative Factorial of " + inputNumber + " is " + iterativeFactorialNumber);
			System.out.println("Recusive Factorial of " + inputNumber + " is " + recursiveFactorialNumber);
		}
		
    }

	/**
	 * Recursive method to work out the factorial of all numbers to a max of n
	 * @param iNumber
	 * @return integer
	 */
	private static int  recursiveFactorial(int iNumber) {
		int factorial = 1;

		if(iNumber==factorial){
		return factorial;
		}

		factorial = recursiveFactorial(iNumber-1) * iNumber;
		return factorial;
	}

	/**
	 * Iterative method to work out the factorial of all numbers to a max of n
	 * @param iNumber
	 * @return integer
	 */
	private static int iterativeFactorial(int inputNumber) {
        int factorial = 1;
        for(int iNumber = inputNumber ;iNumber>0;iNumber--)   {
          factorial = factorial * iNumber;
        }
		return factorial;
		
	}

}
