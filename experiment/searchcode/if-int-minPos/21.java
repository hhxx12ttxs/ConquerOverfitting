
/**
   This class sorts an array, using the selection sort algorithm.
 */
public class SelectionSorter extends Sorter
{
	public SelectionSorter(int[] anArray)
	{
		super(anArray);
	}

	
	/**
      Sorts the array managed by this selection sorter.
	 */
   public void sort() 
         throws InterruptedException
   {  
      for (int i = 0; i < a.length - 1; i++)
      {  
         int minPos = minimumPosition(i);
         //sortStateLock.lock();
         try
         {
            swap(minPos, i);
            // For animation
            alreadySorted = i;
         }
         finally
         {
            //sortStateLock.unlock();
         }
         pause(2);
      }
   }

	/**
      Finds the smallest element in a tail range of the array
      @param from the first position in a to compare
      @return the position of the smallest element in the
      range a[from]...a[a.length - 1]
	 */
	private int minimumPosition(int from)
	throws InterruptedException
	{  
		int minPos = from;
		for (int i = from + 1; i < a.length; i++)
		{
			//sortStateLock.lock();
			try
			{
				if (a[i] < a[minPos]) minPos = i;
				// For animation
				markedPosition = i;
			}
			finally
			{
				//sortStateLock.unlock();
			}
			pause(2);
		}
		return minPos;
	}

	/**
      Swaps two entries of the array.
      @param i the first position to swap
      @param j the second position to swap
	 */
	private void swap(int i, int j)
	{
		int temp = a[i];
		a[i] = a[j];
		a[j] = temp;
	}
}
