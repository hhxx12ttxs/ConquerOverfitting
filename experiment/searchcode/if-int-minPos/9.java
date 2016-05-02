package chapter_20;

public class SmallestDiff 
{
	public void findSmallestDiff(int[] a, int[] b)
	{
		int[] temp_a = a.clone(), 
				temp_b = b.clone();
		
		if(a.length > b.length)
		{
			temp_a = b.clone();
			temp_b = a.clone();
		}
		
		int[] minPos = new int[3],
				result_minPos = new int[3];
		
		minPos[0] = Integer.MAX_VALUE;
		minPos[1] = 0;
		minPos[2] = 0;
		result_minPos = minPos.clone();
		
		//For each element in array temp_a, use binary search for array temp_b to find the minimum difference
		for(int i = 0; i < temp_a.length; i++)
		{
			//Binary search for array temp_b
			minPos = checkArr(temp_b, temp_a[i], minPos[0], 0, temp_b.length - 1, minPos);
			
			//Store the minimum value
			if(result_minPos[0] > minPos[0])
			{
				result_minPos[0] = minPos[0];
				result_minPos[1] = minPos[1];
				result_minPos[2] = i;
			}
		}
		
		System.out.println("The smallest difference is " + result_minPos[0] + " between " + temp_b[result_minPos[1]] + " and " + temp_a[result_minPos[2]]);
	}
	
	private int[] checkArr(int[] a, int val, int min, int from, int to, int[] minPos)
	{
		int mid = (to + from) / 2,
			result = val - a[mid];
		
		if(Math.abs(result) < min)
		{
			min = Math.abs(result);
			minPos[0] = min;
			minPos[1] = mid;
		}
		
		//Return the position of the value
		if(to == from)
			return minPos;
		else if(result > 0)
			return checkArr(a, val, min, mid + 1, to, minPos);
		else
			return checkArr(a, val, min, from, mid, minPos);
	}
	
	public static void main(String[] args)
	{
		SmallestDiff sd = new SmallestDiff();
		
		int[] a = {2, 5, 9, 10};
		int[] b = {4, 12, 16, 21, 22};
		
		sd.findSmallestDiff(a, b);
	}
}

