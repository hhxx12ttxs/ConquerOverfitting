package redone;

public class FactorialIte {

	
	public static int computeFactorial(int number){
		
		if(number==0){
			return 1;
		}
		else
			return number*computeFactorial(number-1);
	}
	
	public static void main(String args[]){
		int fact = computeFactorial(10);
		
		System.out.println(fact);
	}
}

