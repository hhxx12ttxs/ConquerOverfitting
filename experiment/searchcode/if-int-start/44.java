int search(int[] A, int start, int end, int target){
if(end==start){
if(target>A[start]) return start+1
return start;
} else{
int mid=(start+end)/2;
if(target==A[mid]) return mid;

