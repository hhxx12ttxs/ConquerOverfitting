int i = n;
int j = n;
int temp;
if(n <= 0){
return num;
}

if(num[n] > num[n - 1]){
num[n] = num[n-1] - num[n];
return num;
}

while (num[i] <= num[i - 1]){
--i;
if(i == 0){
for(int k = 0;k < (n+1)/2; k++){
num[k] = num[n-k] - num[k];

