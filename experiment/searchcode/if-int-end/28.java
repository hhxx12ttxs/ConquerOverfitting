int len = a.length;
int b[] = new int[len];
while(k < len){
int left = 0;
int right = k;
int end = Math.min(len, 2*k);
static void merge(int a[], int left, int right, int end, int[] b){

int i = left;
int j = right;
int k = 0;

