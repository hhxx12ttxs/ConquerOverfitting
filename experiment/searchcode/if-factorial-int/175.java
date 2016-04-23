package edu.neumont.csc250;

public class Factorial {	
	
	public int findFactorialIterative(int num) {
		
		int factorial = 1;
		
		for (int i= 1; i <= num; i++){
			factorial *= i;
		}
		
		return factorial;
	}
	
	public int findFactorialRecursive(int num){
		
		if(num < 2){
			return 1;
		}
		
		return num * findFactorialRecursive(num - 1);
	}
}

