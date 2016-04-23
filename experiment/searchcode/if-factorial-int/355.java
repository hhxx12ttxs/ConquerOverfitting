package com.ch.programs;

public class Factorial 
{
public static void main(String[] args) {
	System.out.println(fact(5));
}
static int fact(int j)
{
	int factorial = 1;
	if(j==0)
	{
		return j;
	}
	else
	{
		for(int k=1;k<j;j--)
		{
			factorial=factorial*j;
		}
	return factorial;	
		
	}
	//return fin;
	
}
}

