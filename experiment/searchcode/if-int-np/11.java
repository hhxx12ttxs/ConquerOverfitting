int x = sc.nextInt();
int n = sc.nextInt();
int e = sc.nextInt();
if((x|n|e)==0)break;
int[] v = new int[x];
for(int k=0;k<x;k++){
int np = Math.min(n, i+v[k]);
int nm = j;
if(ev[np][0]==1){

