public void sort(int startIndex, int endIndex) {

if(startIndex >= endIndex) return;

int pivotIndex = divide(startIndex, endIndex);
sort(pivotIndex + 1, endIndex);

}

private int divide(int startIndex, int endIndex) {

int optimalIndex = getOptimalIndex(startIndex, endIndex);

