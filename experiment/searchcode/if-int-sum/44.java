int cnt = 0;
long sum = 0;
for(int i = 0;i < nums.length; i++){
while(sum+1 < nums[i] &amp;&amp; sum < n){
cnt++;
sum += sum+1;
}
sum+= nums[i];
if(sum >= n){

