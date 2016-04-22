public static int numPaths(int x, int y) {
	int xy = x + y;
	int xfat = factorial(x);
	int yfat = factorial(y);
	int xyfat = factorial(xy);
	
	return (xyfat / (xfat * yfat));
}

private static int factorial(int n) {
	if(n == 1) return 1;
	return n * factorial(n-1);
}

public static int numPathsDP(int x, int y) {
	int[] memo = new int[100];
	int xy = x + y;
	int xfat = factorialDP(x, memo);
	int yfat = factorialDP(y, memo);
	int xyfat = factorialDP(xy, memo);

	return (xyfat / (xfat * yfat));	
}

private static int factorialDP(int n, int[] memo) {
	if(n == 1) return memo[n] = 1;
	if(memo[n] > 0) return memo[n];
	return memo[n] = n * factorialDP(n - 1, memo);
}

