package com.interlan.test.problem;

public class FactorialRecursion {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(FactorialRecursion.factorial(4));
	}
	
	public static int factorial(int number){
		if(number == 0 || number == 1)
			return 1;
		return number * factorial(number-1);
	}

}

