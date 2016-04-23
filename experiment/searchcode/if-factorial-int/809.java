package com;

public class Factorial {

	public static void main(String[] args){
		int n=6;
		factorial(n);
	}

	public static void factorial(int n){
		int fact = 1;
		if (n>0){
			for(int i=1 ; i<=n; i++){
				fact = fact*i;
			}
			System.out.println("Factorial of "+ n + " is:" + fact);
		}
	}
}

