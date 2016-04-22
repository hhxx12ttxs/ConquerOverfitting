package factorials;

public  class Factorial {
	
	public int result= 1;
		public Factorial(int factor) {
				if (factor == 0) {
				result = 1; 
				}
				else 
					for (int i = 1 ; i<=factor; i++)  
					result = result * i;	
				
				System.out.println("factorial = "  +result);
				}
				

	
	
	}	
	
	

