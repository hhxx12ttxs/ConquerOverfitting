public class Factorial {
	public static int[] factorial(int n) {
		int[] result = new int[n==0?1:n];
		doFactorial(n, result, 0);
		return result;
	}
	public static int doFactorial(int n, int[] result, int level) {
		if(n>1) {
			result[level] = n*doFactorial(n-1, result, level+1);
			return result[level];
		} else {
			result[level] = 1;
			return result[level];
		}
	}
	public static void main(String[] args) {
		int[] arr = factorial(5);
		for(int i = 0; i < arr.length; i++) {
			System.out.print(arr[i] + " ");
		}
	}
}
