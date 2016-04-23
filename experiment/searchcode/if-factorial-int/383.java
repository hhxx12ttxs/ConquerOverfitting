package com.marcos.factorial;

public class TestFactorial {
	public static void main(String[] args) {
		int n = 9;
		
		System.out.println(factorial(n));
	}
	
	static int factorial(int n){
		if(n == 0)
			return 1;
		else
			return n * factorial(n - 1);
	}
}

//ESTE ARCHIVO PUEDE BORRARSE ES DE PRACTICA
