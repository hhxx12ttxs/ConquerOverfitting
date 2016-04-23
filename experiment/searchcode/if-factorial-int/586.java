package binomialCoefficient;

public class BinCoefficient {
	
	public int binCoefficient(int a, int b){
		if(a>=b){
			return factorial(a)/(factorial(a-b)*factorial(b)); 
		} else {
			return -1;
		}
	}
	
	public int factorial(int n){
		int result = 1;
		for (int i = 1; i<=n; i++){
			result *= i;
		}
		return result;
	}

}

