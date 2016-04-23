
public class Factorial {

	
public static int computeFactorial(int number){
		
		if(number==0){
			return 1;
		}
		return number*computeFactorial(number-1);
	}

	public static void main(String args[]){
		
		int number =10;
		int fact = computeFactorial(number);
		System.out.println(fact);
	}

	
	
	
}

