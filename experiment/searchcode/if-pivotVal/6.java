package parallelzo.qsort;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.RecursiveAction;
/**
 * QSortArrayListTask is a task for {@link java.util.concurrent.ForkJoinPool ForkJoinPool}
 * that sorts using Parallel Quick Sort a generic ArrayList
 * of a type that implements the {@link java.lang.Comparable Comparable} Interface. <br />
 * Recommended use: when array length is greater then 20,000 elements. 
 * 
 * @author Oz Elentok <oz.elen@gmail.com>
 * @version 1.0
 */
public class QSortArrayListTask<T extends Comparable<T>> extends RecursiveAction {
	
	private static final long serialVersionUID = 1L;
	/**
	 * Minimum length of an ArrayList required to make new tasks
	 */
	private static final int MINLEN = 1000;
	/**
	 * Pivot Index Generator
	 */
	private final Random rand;
	/**
	 * The ArrayList to sort
	 */
	private final ArrayList<T> array;
	/**
	 * Starting index of sorting range
	 */
	private final int start;
	/**
	 * Ending index of sorting range
	 */
	private final int end;
	
	/**
	 * Creates a sort task for a generic ArrayList of
	 * a type that implements the {@link java.lang.Comparable Comparable} Interface. <br />
	 * Sorts the list from <code>start</code> up to <code>end</code>.
	 * @param array		The ArrayList to sort	
	 * @param start		Starting index of sorting range
	 * @param end		Ending index of sorting range
	 */
	public QSortArrayListTask(ArrayList<T> array , int start, int end) {
		this.array = array;
		this.start = start;
		this.end = end;
		this.rand = new Random();
	}
	
	/**
	 * Creates a sort task for a generic ArrayList of
	 * a type that implements the {@link java.lang.Comparable Comparable} Interface.
	 * @param array		the ArrayList to sort	
	 */
	public QSortArrayListTask(ArrayList<T> array) {
		this.array = array;
		this.start = 0;
		this.end = array.size() - 1;
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
			invokeAll(new QSortArrayListTask<T>(array, start, pivot - 1),
			new QSortArrayListTask<T>(array, pivot + 1, end));
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
		T pivotVal = array.get(pivot);
		T temp;
		array.set(pivot, array.get(end));
		array.set(end, pivotVal);
		pivot = start;
		for (int i = start; i < end; i++) {
			if (array.get(i).compareTo(pivotVal) < 0) {
				temp = array.get(i);
				array.set(i, array.get(pivot));
				array.set(pivot, temp);
				pivot++;
			}
		}
		array.set(end, array.get(pivot));
		array.set(pivot, pivotVal);
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

