private <T extends Comparable<T>> void mergeSort(T[] a,int lowerBound,int upperBound){
if(lowerBound < upperBound){
int mid = (lowerBound + upperBound)/2;
merge(a,lowerBound,mid,upperBound);
}
}
private <T extends Comparable<T>> void merge(T[] a,int lowerBound,int midPoint,int upperBound){

