int lower = 0;
int upper = length-1;
int[] result = new int[]{-1, -1};

while(lower<upper) {
int mid = (lower+upper)/2;
if(A[mid]<target) {
lower = mid+1;
} else {

