double solve(double[] r, int remain, int k){
if(r.length == k + remain){
double sum = 0;
for(double x:r)
for(int i = 0 ; i <= remain ; i++){
double[] tmp = mt(r, k+i);
double result = solve(tmp, remain - i, k);

