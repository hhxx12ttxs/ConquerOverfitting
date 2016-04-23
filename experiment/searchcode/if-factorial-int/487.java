//Exercise 7.4. Section 6.8 presents a recursive method that 
//computes the factorial function. Write an iterative version of factorial.


public class Exercise7_4InterationFactorial {
	
	
	public static int factorial(int n) {
	    if (n == 0) {				//factorial 0 = 1
	      return 1;					//return 1
	    } else {
	    	int result = 1;
	    	for (int i = 1;i <= n; i++)	{
	    		result *= i;		//calculate factorial n
	    	}
	    	return result;
	    }
	}

	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++)  {
		System.out.println(factorial(Integer.parseInt(args[i])));
		}
	}
}
	




