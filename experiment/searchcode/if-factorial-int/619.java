
public class computeFactorialTailRecursion {
	/**return the factorial for a specified number*/
	public static long factorial(int n) {
		return factorial(n, 1); // call auxiliary method
		
	}
	/**tail recursive method for factorial**/
	private static long factorial(int n, int result){
		if (n==0)
			return result; // base case
		else
			return factorial(n-1, n*result); // recursive call 
	}

}

