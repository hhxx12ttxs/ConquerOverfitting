public static long dp[][];
public static long sumFreq[][];

public static long optBSTDP(int[] arr, int[] freq) {
int n;
int r;
long minSum;
long sum;

if (i > j)
return 0;

if (i == j)
return freq[i];

if (dp[i][j] != -1)

