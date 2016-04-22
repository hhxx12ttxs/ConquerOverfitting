public class RecursiveFactorial{

	
	public static void main(String[] args){
	
		Factorial  f = new Factorial();
	System.out.println(	f.factorial(4));
	
	
	
	}

	public int factorial(int number){
		if (number>=1){
		return number*factorial(number-1);
		}
		else
			return 1;
	}
	
	}

