public static int[] Partition(int[] arr, int low, int high){
int i = low;
int j = high;

int pivotVal = GetLeftNumber(arr[low + (high-low)/2]);
while(GetLeftNumber(arr[j]) > pivotVal){
j--;
}

if(i <= j){
Swap(arr, i, j);

