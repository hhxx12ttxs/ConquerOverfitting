package parallelzo.qsort;
import java.util.Random;
import java.util.concurrent.RecursiveAction;
/**
 * QSortIntTask is a task for {@link java.util.concurrent.ForkJoinPool ForkJoinPool}
 * that sorts using Parallel Quick Sort an int Array. <br />
 * Recommended use: when array length is greater then 20,000 elements. 
 * 
 * @author Oz Elentok <oz.elen@gmail.com>
 * @version 1.0
 */
public class QSortIntTask extends RecursiveAction {
	private static final long serialVersionUID = 1L;
	/**
	 * Minimum length of an array required to make new tasks
	 */
	private static final int MINLEN = 1000;
	/**
	 * Pivot Index Generator
	 */
	private final Random rand;
	/**
	 * The Array to sort
	 */
	private final int[] array;
	/**
	 * Starting index of sorting range
	 */
	private final int start;
	/**
	 * Ending index of sorting range
	 */
	private final int end;
	
	/**
	 * Creates a sort task for an int Array. <br />
	 * Sorts the array from <code>start</code> up to <code>end</code>.
	 * @param array		The Array to sort	
	 * @param start		Starting index of sorting range
	 * @param end		Ending index of sorting range
	 */
	public QSortIntTask(int[] array , int start, int end) {
		this.array = array;
		this.start = start;
		this.end = end;
		this.rand = new Random();
	}
	
	/**
	 * Creates a sort task for an int Array
	 * @param array		The Array to sort	
	 */
	public QSortIntTask(int[] array) {
		this.array = array;
		this.start = 0;
		this.end = array.length - 1;
		this.rand = new Random();
	}
	
	/**
	 * Sorts the array from start index to end index using Quick Sort. <br />
	 * The array at the sorting range gets partitioned. <br />
	 * the length of the sorting range decides whether to make new tasks(adding them to the thread pool),
	 * or to sort them on the current thread.
	 * @param start		starting index of sorting range
	 * @param end		ending index of sorting range
	 */
	private void quickSort(int start, int end) {
		if (start >= end) {
			return;
		}
		int pivot = rand.nextInt(end - start) + start + 1;
		pivot = partition(start, end, pivot);
		
		if(end - start + 1 < MINLEN) {
			quickSort(start, pivot - 1);
			quickSort(pivot + 1, end);
		}
		else {
			invokeAll(new QSortIntTask(array, start, pivot - 1),
			new QSortIntTask(array, pivot + 1, end));
		}
	}
	
	/**
	 * Partitions the array at the sorting range
	 * @param start		starting index of sorting range
	 * @param end		ending index of sorting range
	 * @param pivot		index of selected pivot
	 * @return		new index of the selected pivot 
	 */
	private int partition(int start, int end, int pivot) {
		int pivotVal = array[pivot];
		int temp;
		array[pivot] =  array[end];
		array[end] = pivotVal;
		pivot = start;
		for (int i = start; i < end; i++) {
			if (array[i] < pivotVal) {
				temp = array[i];
				array[i] = array[pivot];
				array[pivot] = temp;
				pivot++;
			}
		}
		array[end] = array[pivot];
		array[pivot] = pivotVal;
		return pivot;
	}
	
	/**
	 * Starts the sorting process.
	 */
	@Override
	protected void compute() {
		quickSort(start, end);
	}

}

