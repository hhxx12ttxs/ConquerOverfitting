public int uniquePaths1(int m, int n) {

int[][] result = new int[m][n];

for(int i = 0; i < m; i++){
for(int j = 0; j < n; j++){
if(i == 0 || j == 0){

