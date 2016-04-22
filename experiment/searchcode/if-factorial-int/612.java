package com.Practice.puzzle;

public class Factorial {
	
	public static int getFactorial( int num){
		
		if( num == 0)
			return 1;
		else
			return (num * getFactorial(num - 1));
	}

}

