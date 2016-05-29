public static int numTrees(int n) {
double result = factor(2 * n) / (factor(n + 1) * factor(n));
return (int)result;
}

private static double factor(int n) {
if (n <= 1)
return 1;
return n * factor(n - 1);
}

}

