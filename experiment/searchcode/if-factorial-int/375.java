package com.apcompsci.factorial;

public class Factorial {
	public static void main(String[] args){
		System.out.println(factorialRecursion(5));
	}
	public static int factorialRecursion(int n){
		if(n == 0 || n == 1){
			return 1;
		}
		else {
			
		}
		return n*factorialRecursion(n);
	}


}
