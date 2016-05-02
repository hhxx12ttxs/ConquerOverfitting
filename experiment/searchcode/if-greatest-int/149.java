package parallelzo.max;
import java.util.concurrent.RecursiveTask;

/**
 * MaxArrayTask is a task for {@link java.util.concurrent.ForkJoinPool ForkJoinPool}
 * that searches for the greatest value in a generic array, and returns it. <br />
 * The search is parallel if the array length is greater then 10,000 elements. <br />
 * Recommended use: when array length is greater then 500,000 elements. 
 * 
 * @author Oz Elentok <oz.elen@gmail.com>
 * @version 1.0
 */
class MaxArrayTask<T extends Comparable<T>> extends RecursiveTask<T> {
		private static final long serialVersionUID = 1L;
		/**
		 * Minimum length of array to make new tasks
		 */
		private static final int MINLEN = 10000;
		/**
		 * The array to search in
		 */
		private final T[] array;
		/**
		 * Starting index of searching range
		 */
		private final int start;
		/**
		 * Ending index of searching range
		 */
		private final int end;
		
		/**
		 * Creates a searching task for the greatest value in a generic array. <br />
		 * Searches the array from <code>start</code> up to <code>end</code>.
		 * @param array		The Array to sort	
		 * @param start		Starting index of searching range
		 * @param end		Ending index of searching range
		 */
		public MaxArrayTask(T[] array, int start, int end) {
			this.array = array;
			this.start = start;
			this.end = end;
		}
		/**
		 * Creates a searching task for the greatest value in a generic array.
		 * @param array		The Array to sort	
		 */
		public MaxArrayTask(T[] array) {
			this.array = array;
			this.start = 0;
			this.end = array.length - 1;
		}
		/**
		 * Searches for the greatest value in a generic array. <br />
		 * the length of the search range decides whether to make new tasks(adding them to the thread pool),
		 * or to search in them on the current thread.
		 * @return	Greatest element between <code>start</code> and </end> in the array 
		 */
		public T max() {
			int len = end - start + 1;
			if(len < MINLEN) {
				T max = array[start];
				for(int i = start + 1; i <= end; i++) {
					if(max.compareTo(array[i]) < 0) {
						max = array[i];
					}
				}
				return max;
			}
			else {
				int mid = (start + end) / 2;
				T leftMax, rightMax;
				MaxArrayTask<T> left = new MaxArrayTask<T>(array, start, mid);
				MaxArrayTask<T> right = new MaxArrayTask<T>(array, mid + 1, end);
				left.fork();
				rightMax = right.compute();
				leftMax = left.join();
				return (leftMax.compareTo(rightMax) >= 0) ? leftMax : rightMax;
			}
		}
		/**
		 * Starts the search process.
		 * @return Greatest element between <code>start</code> and </end> in the array
		 */
		@Override
		protected T compute() {
			return max();
		}
	}

