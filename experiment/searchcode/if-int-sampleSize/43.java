public int[][] table = new int[SAMPLESIZE+1][SAMPLESIZE+1];

// recall metrices
public double recall12 = 0;
for (int i = 1; i <= SAMPLESIZE; i++) {
for (int j = 1; j <= SAMPLESIZE; j++) {
int type = table[i][j];

