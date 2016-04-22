package com.bayamp.homework;

public class Factorial {
	
	public static int getFactorial(int number){
		int factorial = 1;
	for(int i=number; i>0 ; i--){
		factorial = factorial*i;
	}	
		System.out.println("print factorial " + number);
		return factorial;
	}

	
	public static int getFactorialUsingRecursion(int number){
		if(number==0)
			return 1;
		return number*getFactorialUsingRecursion(number-1);
		
		
	}
	
	
}

