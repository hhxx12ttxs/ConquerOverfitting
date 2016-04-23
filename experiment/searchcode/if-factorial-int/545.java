package com.sean.tailcall;

public class Factorial {
	
	public static TailCall<Integer> factorialTailRec(final int factorial, final int number){
		if(number == 1)
			return TailCalls.done(factorial);
		else
			return TailCalls.call(() -> factorialTailRec(factorial * number, number - 1));
	}
	
	public static void main(String[] args){
		int f = factorialTailRec(1, 10).invoke();
		System.out.println(f);
	}
}

