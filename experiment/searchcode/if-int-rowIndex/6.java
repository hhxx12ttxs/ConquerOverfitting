ArrayList<Integer> res = new ArrayList<Integer>();

if(rowIndex < 0) return res;

int[] parent = new int[rowIndex + 1];
child[0] = 1;
parent[0] = child[0];

if(rowIndex >= 1){
for(int i = 2; i <= rowIndex + 1; i++){

