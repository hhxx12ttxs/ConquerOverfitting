Integer[] result = Utils.arrayFullCopy(n, array);

for(int i=0; i<n; i++){
int minPos = findMinPosition(result, i);
private Integer findMinPosition(Integer[] array, int start){
int min = array[start];
int minPos = start;

