private static int[] chain = {30, 35, 15, 5, 10, 20, 25};

public static int[][][] getOptimalOrder(int[] chain) {
if (chain == null) chain = MatrixChainOrder.chain;
private static void printOptimalOrder(int[][][] r, int i, int j) {
if (i == j) return;
else {
int k = r[1][i][j];

