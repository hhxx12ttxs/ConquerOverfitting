package parallelzo.max;
import java.util.concurrent.RecursiveTask;

/**
 * MaxDoubleTask is a task for {@link java.util.concurrent.ForkJoinPool ForkJoinPool}
 * that searches for the greatest value in a double array, and returns it. <br />
 * The search is parallel if the array length is greater then 10,000 elements. <br />
 * Recommended use: when array length is greater then 500,000 elements. 
 * 
 * @author Oz Elentok <oz.elen@gmail.com>
 * @version 1.0
 */
class MaxDoubleTask extends RecursiveTask<Double> {
		private static final long serialVersionUID = 1L;
		/**
		 * Minimum length of array to make new tasks
		 */
		private static final int MINLEN = 10000;
		/**
		 * The array to search in
		 */
		private final double[] array;
		/**
		 * Starting index of searching range
		 */
		private final int start;
		/**
		 * Ending index of searching range
		 */
		private final int end;
		
		/**
		 * Creates a searching task for the greatest value in a double array. <br />
		 * Searches the array from <code>start</code> up to <code>end</code>.
		 * @param array		The Array to sort	
		 * @param start		Starting index of searching range
		 * @param end		Ending index of searching range
		 */
		public MaxDoubleTask(double[] array, int start, int end) {
			this.array = array;
			this.start = start;
			this.end = end;
		}
		/**
		 * Creates a searching task for the greatest value in a double array.
		 * @param array		The Array to sort	
		 */
		public MaxDoubleTask(double[] array) {
			this.array = array;
			this.start = 0;
			this.end = array.length - 1;
		}
		/**
		 * Searches for the greatest value in a double array. <br />
		 * the length of the search range decides whether to make new tasks(adding them to the thread pool),
		 * or to search in them on the current thread.
		 * @return	Greatest element between <code>start</code> and </end> in the array 
		 */
		public Double max() {
			int len = end - start + 1;
			if(len < MINLEN) {
				double max = array[start];
				for(int i = start + 1; i <= end; i++) {
					if(max < array[i]) {
						max = array[i];
					}
				}
				return new Double(max);
			}
			else {
				int mid = (start + end) / 2;
				Double leftMax, rightMax;
				MaxDoubleTask left = new MaxDoubleTask(array, start, mid);
				MaxDoubleTask right = new MaxDoubleTask(array, mid + 1, end);
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
		protected Double compute() {
			return max();
		}
	}

