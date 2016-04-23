package com.calypso.training.map;

public class RecursionExample {

	public static int factorial(int n){
		
		int result=0;
		
		
		
		if(n==1){
			
			return 1;
			
			
		}
		result=factorial(n-1)*n;
		
		
		return result;
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		System.out.println("Factorial of 4::"+ factorial(4));
		
		
		
	}

}

