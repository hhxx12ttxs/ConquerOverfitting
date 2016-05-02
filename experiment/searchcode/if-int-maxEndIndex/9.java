package main;

public class MaxContiguousSubset {
	
	
	public void calcMaxContiguoussubset(int[] arr){
		int maxStartIndex = 0, maxEndIndex = 0;
		int maxEndingHere = arr[0];
		int maxSum = arr[0];
		for (int i=1; i<arr.length; i++){
			if(maxEndingHere < 0){
				maxEndingHere = arr[i];
				maxStartIndex = i;
			}else{
				maxEndingHere += arr[i];
			}
			if(maxEndingHere > maxSum){
				maxSum = maxEndingHere;
				maxEndIndex = i;
			}
		}
		
		System.out.println("Maximum sum is :" + maxSum);
		for(int i=maxStartIndex; i<=maxEndIndex; i++){
			System.out.print(arr[i] + ",");
		}
				
	}
		
	public static void main(String[] args){
		int[] arr = {4, -1, 5, 6, -13, 2};
		new MaxContiguousSubset().calcMaxContiguoussubset(arr);
	}

}

