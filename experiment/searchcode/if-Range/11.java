public static int[] searchRange(int[] A, int target) {
int[] range={-1,-1};
if(A.length==0 || target>A[A.length-1]||target<A[0]){
public static void searchRange(int[] A,int target, int start, int end, int[] range){
if(start>end){
return;
}
int mid=(start+end)/2;

