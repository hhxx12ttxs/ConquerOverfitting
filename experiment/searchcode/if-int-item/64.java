static int subsearch(int[] a, int left, int right, int item){
int n = a.length;
if(right<left || left<0 || right>=n) return -1;

int mid = left+(right-left)/2;
if(a[mid] == item) return mid;

