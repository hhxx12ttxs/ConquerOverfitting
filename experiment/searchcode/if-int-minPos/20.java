private static int minimumPosition(int[] a,int from){
int minPos = from;
for(int i = from + 1;i<a.length;i++){
if(a[i] < a[minPos]) {
minPos = i;

