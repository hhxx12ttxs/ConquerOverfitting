double[] linkCounts = new double[n];

//Lij = 1 if j links to i
//for each node (j), calculate how many outgoing links
for(int i = 0; i < n; i++) {
for(int j = 0; j < n; j++) {
//aij is only 1/sum if j links to i, so if Lij == 1
if(L[i][j] == 1) {

