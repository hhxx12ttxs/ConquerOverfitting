for (int i = 0; i < n; i++){
int minPos = array[i];
for (int j = i+1 ; j < n; j++){
if (array[minPos] > array[j])
minPos = j;
}
int temp = array[i];

