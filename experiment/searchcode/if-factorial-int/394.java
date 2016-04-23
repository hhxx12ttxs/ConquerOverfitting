import java.io.*;
import java.util.*;

public class FactorialRecursion{
	public static void main(String args[]){
		int fact = factorial(5);
		System.out.println("The factorial value is : "+fact);
	}

	public static int factorial(int n){
		if(n == 1)
			return 1;
		else
			return n * factorial(n-1);
	}
}
