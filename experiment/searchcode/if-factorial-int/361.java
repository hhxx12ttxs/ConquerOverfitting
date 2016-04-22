package algorithms.recursionAlgorithms;

public class FactorialCalculator {
	public int factorial(int n){
		if(n == 1 || n == 0){
			return 1;
		}
		
		return n*factorial(n-1);
	}
	
	public static void main(String[] args){
		System.out.println(new FactorialCalculator().factorial(5));
	}
}

