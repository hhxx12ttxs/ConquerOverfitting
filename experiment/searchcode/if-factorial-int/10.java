package tests.pojos;

import java.math.BigInteger;

public class Factorial {

	
	public static void main(String[] args){	
		int x = factorial(2);
		System.out.println(x);
	}
	
	public static int factorial(int i){
		return foo(i, 1);
	}
	
	public static int bar(int i, int acc){
		if(i == 1){
			return acc;
		}else{
			return foo(i - 1, acc * i);
		}
	}
	
	public static int foo(int i, int acc){
		if(i == 1){
			return acc;
		}else{
			return bar(i - 1, acc * i);
		}
	}
	
}

