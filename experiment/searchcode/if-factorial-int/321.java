package cis232.examples;

public class FactorialExample {

	public static void main(String[] args) {
		System.out.println(factorial(10));
		System.out.println(factorialLoop(10));
	}
	
	public static int factorial(int n){
		//Base case
		if(n == 0){
			return 1;
		}else{
			//Recursive case
			return n * factorial(n - 1);
		}
	}
	
	//The same solution without recursion
	public static int factorialLoop(int n){
		int factorial = 1;
		for(int i = 1; i <= n; i++){
			factorial *= i;
		}
		return factorial;
	}

}

