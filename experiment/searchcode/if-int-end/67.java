@Slf4j
public class QuickSort {

public int[] sort(int[] a) {
int start = 0, end = a.length - 1;
private void quickSort(int[] a, int start, int end) {
if (start < end) {
int partition = partition(a, start, end);

