int temp = list[i];
int minPos = -1;
int min = Integer.MAX_VALUE;
for(int j = i; j < N; j++) {
if(min > list[j]) {
minPos = j;
min = list[j];

