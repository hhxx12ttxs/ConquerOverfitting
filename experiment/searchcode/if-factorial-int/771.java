package factorial;

public class Factorial {
	
	// RECURSIVE

	/**
	 * 			1 	if n == 1 
	 * n! =		
	 * 			n * ( n - 1 )!	otherwise.
	 * 
	 * 
	 * @param n
	 * @return
	 */
	
	public static int factorial( int n){

		// BASE 
		if( n <= 0 )
			throw new IllegalArgumentException() ;
		
		if( n == 1 )
			return 1 ;
		
		// INDUCTIVE
		return ( n * factorial( n -1 ));	// Re-composing solutions to subproblems
	}
	
	
}

