int[] ans = new int[a.length + b.length];
int stepA = 0;
int stepB = 0;
for(int i = 0; i<ans.length; i++){
if(stepA < a.length &amp;&amp; stepB < b.length){
if(a[stepA] >= b[stepB]){
ans[i] = b[stepB];

