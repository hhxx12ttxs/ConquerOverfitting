public static int partition(Comparable[] li,int low_idx,int high_idx){
int length = high_idx-low_idx+1;
if (length==1){
return low_idx;
}
else{
Comparable pivot = li[high_idx];
return thresh_idx-1;

}
}
public static void qsort(Comparable[] li,int low_idx,int high_idx){
if(high_idx<=low_idx) return;

