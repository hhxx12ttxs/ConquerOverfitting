package com.java.factorial;


public class Factorial {

	public static void main(String args[])
	{
		Factorial factorial=new Factorial();
		int result=factorial.fact(5);
		System.out.println("Factorial.main():::"+result);	
	}
	public int fact(int n){
		int result=0;
		if(n==0)
	         return 1;
		
		System.out.println("Factorial.fact()::before::"+n);
		result=n * fact(n-1);
		
		System.out.println("Thread:::");
		System.out.println("Factorial.fact()::After::"+n);
		System.out.println("Factorial.fact()::"+result);
		return result;
	}
}

