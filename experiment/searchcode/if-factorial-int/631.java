package IntegerAlgorithm;

public class Factorial {

	public int recursiveFactorial(int n){
		if(n <= 1)
		   return 1;
		else
		   return n * recursiveFactorial(n-1);
	}
	
	public int factorial(int n){
		int product = 1;
		if(n <=1)
		   return 1;
		else {
			for(int i=1; i<=n; i++)
			    product *= i;
			return product;
		}
		   	
	}
}

