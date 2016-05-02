package subString;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SubArrayFinder {

	/**
	 * Given an array containing only 0s and 1s,
	 *  find the minimum size of subarray containing exactly k-zeros.
	 *  Ex. 11010100011 --  for k=4, result = 5(01000)
	
	 */

	public int[] getShortestSubArray(int[] array, int k){
		int minLength = Integer.MAX_VALUE;
		int minBegin = 0;
		int minEnd = 0;
		Boolean bool = false;
		
		int count =0;
		Map<Integer, Integer> found = new HashMap<Integer, Integer>();
		
		for(int i=0; i<array.length ;i ++){
			if(array[i] == 0){
				count ++;
				found.put(count, i);
				bool = true;
			}
			if(count >= k && bool){
				int begin = found.get(count - k+1);
				int end = found.get(count);
				int length = end - begin + 1;
				if(length < minLength){
					minLength = length;
					minBegin = begin;
					minEnd = end;
				}
				bool = false;
			}
			
		}
		
		int[] subArray = null ;
		if(minLength < Integer.MAX_VALUE){
			subArray= Arrays.copyOfRange(array, minBegin, minEnd+1);
		}
		
		return subArray;
	}
	
	
	public void printArray(int[] array){
		System.out.println();
		
		if(array != null){
			for(int i=0; i<array.length ; i++){
				System.out.print(array[i]+" ");
			}
		} else{
			System.out.println("Null array");
		}
		
	}
	
	
}

