int[][] matrix = new int[n][n];
if(n <= 0) return matrix;

int start = 0;
int finish = n-1;
int ctr= 1;
while(start < finish){
int x = start, y = start;
while(y < finish)  matrix[x][y++] = ctr++;

