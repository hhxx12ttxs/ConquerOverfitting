package org.bluemedialabs.vincoli;

import org.bluemedialabs.mpid.NodeLabel;

public class SortArrayMulti 
{
	public void Stampa(double[][] a) {
	

     //	int [] a = {7,5,11,2,16,4,18,14,12,30};
   /*	int[][] a = new int[5][2];
		
 		a[0][0] = 1;
 		a[0][1] = 7;
 		a[1][0] = 2;
 		a[1][1] = 5;
 		a[2][0] = 3;
 		a[2][1] = 11;
 		a[3][0] = 4;
 		a[3][1] = 2;
 		a[4][0] = 5;
 		a[4][1] = 16;    */
 		
		int index , indexOfNextSmallest;
		int i;
		for (index = 0; a[index][0] != 0 ;index++)
		  {
			indexOfNextSmallest = indexOfSmallest (index,a);
			interchange(index,indexOfNextSmallest,a);
			
		  }
		
		System.out.println ("RSV ORDINATI");
		System.out.println ("NODE NUMBER    PESO W");
		
		for (int row = 0; row < 10 ;row++)
		 {
			for (int col = 0; col < 2; col++)
			 {
		 	   System.out.print (a[row][col] + "            ");
			 }
			System.out.println ("  "); 
		 }	
	}
	
	
	private static void interchange (int i, int j, double[][] a)
	{
	  double temp;
	  temp = a[i][1];
	  a[i][1]= a[j][1];
	  a[j][1] = temp;
	  
	  temp = a[i][0];
	  a[i][0]= a[j][0];
	  a[j][0] = temp;
	}

	private static int indexOfSmallest (int startIndex,double[][] a)
	{
	  double max = a[startIndex][1];
	  int indexOfMax = startIndex;
	  int index;
	  for (index = startIndex +1; index < 10 ;index++)
	  {
		if (a[index][1] > max)
		 {
		   max = a[index][1];
		   indexOfMax = index;
	     }	
	  }
	 return indexOfMax;
	}


}

