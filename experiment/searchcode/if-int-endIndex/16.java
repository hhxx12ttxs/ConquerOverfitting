public static void sort(Sortable[] array, int startIndex, int endIndex){
if(startIndex >= endIndex){
return;
}
int q = partition(array, startIndex, endIndex);

