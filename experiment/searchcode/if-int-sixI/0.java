static int dp[][][][][][][] = new int[40][5][5][5][5][5][5];

static int dfs(int sum, int one, int two, int three, int four, int five,
int six) {
if (dp[sum][one][two][three][four][five][six] > -1)
for (int fivei = 0; fivei <= 4; fivei++)
for (int sixi = 0; sixi <= 4; sixi++)
dp[i][onei][twoi][threei][fouri][fivei][sixi] = -1;

