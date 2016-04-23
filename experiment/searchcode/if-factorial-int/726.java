package com.example;

public class FactorialDemo {

	 public static void main(String[] args) {
//	       Scanner scanner = new Scanner(System.in);
//	       System.out.print("Enter the number whose factorial is to be found: ");
//	       int n = scanner.nextInt();
	       // int result = factorial(5);
	       int result = factorialRecursive(5);
	       System.out.println("The factorial of 10 is " + result);
	   }

	   public static int factorial(int n) {
	       int result = 1;
	       for (int i = 1; i <= n; i++) {
	           result = result * i;
	       }
	       return result;
	   }
	   
	   public static int factorialRecursive(int n) {
	       if (n == 0) {
	           return 1;
	       } else {
	           return n * factorial(n - 1);
	       }
	   }
}

