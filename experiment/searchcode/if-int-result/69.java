int[][] result = new int[n][n];

int start = 1, x = 0, y = 0;

for(int i = n; i > 0; i -= 2){
if(i == 1){
result[x][y] = start;

