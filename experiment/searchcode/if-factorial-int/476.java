//////////////////////////////////////////////////////////////////////////////////////////////////
//Exercise 7.4. Section 6.8 presents a recursive method that computes the factorial function.	// 
//Write an iterative version of factorial.														//
//////////////////////////////////////////////////////////////////////////////////////////////////

public class Factorial {
	public static int factorial(int n) {
    	if (n == 0) {		//factorial of 0 = 1.
      		return 1;		//return 1 in this case.
    	} 
    	else {
    		int result = 1;
      		for (int i=1; i <= n; i++) {
      			result *= i;		//calculation for factorial n.
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

/*public static int factorial(int n) {
    if (n == 0) {
      return 1;
    } 
    else {
      int recurse = factorial(n-1);
      int result = n * recurse;
      return result;
	} 
}*/
