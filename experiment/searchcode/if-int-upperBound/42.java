private <T extends Comparable<T>> void quickSort(T[] a,int lowerBound,int upperBound){
if(lowerBound <= upperBound){
int pivot = getPivotPosition(a, lowerBound, upperBound);
quickSort(a, lowerBound, pivot - 1);

