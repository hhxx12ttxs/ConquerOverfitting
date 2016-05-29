@Override
public void sort(int[] array, int startIndex, int endIndex) {

if (startIndex >= endIndex) {
return;
}

int mid = (startIndex + endIndex) >> 1;
sort(array, startIndex, mid);
sort(array, mid + 1, endIndex);

