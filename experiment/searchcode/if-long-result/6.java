public static long[][] generateBinomialCoefficients(int n, long module) {
long[][] result = new long[n + 1][n + 1];
if (module == 1)
return result;
for (int i = 0; i <= n; i++) {
result[i][j] -= module;
}
}
return result;
}

public static long power(long base, long exponent, long mod) {
if (exponent == 0)

