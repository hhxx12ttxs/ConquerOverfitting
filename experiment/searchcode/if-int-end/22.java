public static void Sort(int[] A, int start, int end){
if(end <= start || start < 0 || end >= A.length){
return;
}
int q = partion(A, start, end);

