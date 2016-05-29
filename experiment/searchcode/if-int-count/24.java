int s = S.length(), t = T.length();
int[][] count = new int[s + 1][t + 1];
count[0][0] = 1;

for(int i = 1; i <= s; i++)
count[i][0] = 1;

for(int j = 1; j <= t; j++){

