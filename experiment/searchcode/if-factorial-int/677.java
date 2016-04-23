/* 
 * Compute factorial
 */

// Time: O(n)
// Space: O(n)
public static long factorialRecursive(int n){
	if(n < 0) return -1;
	if(n == 0) return 1;
	return n * factorial(n-1);
}

// Time: O(n)
// Space: O(1)
public static long factorial(int n){
	if(n < 0) return -1;
	long result = 1;
	for(int i=2; i<=n; i++){
		result *= i;
	}
	return result;
}
