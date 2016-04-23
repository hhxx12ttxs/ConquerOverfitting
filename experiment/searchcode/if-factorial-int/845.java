package mum;

public class Factorial {

	public static int factorial(int x){
		
		if(x==0 )
			return 1;
		else{
			return x*factorial(x-1);
		}
		
	}
	
	public static void main(String[] args) {
	
		int number = 4;
		
		System.out.println(factorial(number));
			
			
		}

	}



