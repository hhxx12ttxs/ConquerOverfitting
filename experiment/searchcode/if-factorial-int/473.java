package com.classes;
public class Factorial {
	public static int res = 1;
	public static int result  = 0;
	
	public int factorial(int n){
		if(n>1){
			res = res*(n);
			factorial(n-1);
		}
		result = res;
		res = 1;
		return result;
	}
}

