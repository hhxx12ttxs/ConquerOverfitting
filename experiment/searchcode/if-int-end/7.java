public class QuickSort {

public void sort(int a[], int start, int end) {

if (end <= start) {
return;
}

int partition = partition(a, start, end);
sort(a, start, partition-1);

