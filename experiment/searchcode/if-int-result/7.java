/*****************************************************************************
 *  Copyright (C) 2011 by Thomas Goossens - thomasgoossens.be				**
 *  																		**
 *  This program is free software: you can redistribute it and/or modify	**	
 *  it under the terms of the GNU General Public License as published by	**
 *  the Free Software Foundation, either version 3 of the License, or		**
 *  (at your option) any later version.										**
 *  																		**
 *  This program is distributed in the hope that it will be useful,			**
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of			**
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the			**
 *  GNU General Public License for more details.							**
 *																			**
 * You should have received a copy of the GNU General Public License		**
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>. 	**
 *******************************************************************************/

import java.util.Random;


/**
 * Can generate arrays of a given size with a certain property
 * -already sorted
 * -completely random
 * -nearly sorted
 * -few unique keys
 * -reversed
 * 
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class ArrayGenerator
{
	
	
    public final static int ARRAY_RANDOM = 0;
    public final static int ARRAY_REVERSED = 1;
    public final static int ARRAY_SORTED = 2;
    public final static int ARRAY_FEWUNIQUE = 3;
  
	/**
     * Constructor for objects of class ArrayGenerator
     */
    public ArrayGenerator()
    {
   
    }

   /**
    * Random array with maximum value = 1000 , minvalue=0
    * @param size
    * @return
    */
   public static int[] generateRandomArray(int size){
    	return generateRandomArray(size,0, size);
   }
   
   /**
    * Random array
    * @param size
    * @param minvalue
    * @param maxvalue
    * @return
    */
    public static int[] generateRandomArray(int size,int minvalue, int maxvalue){
     	 int[] a = new int[size];
       	 Random generator = new Random();
       	 for (int i = 0; i < size; i++)
       	 {
       	      a[i] = generator.nextInt(maxvalue-minvalue)+minvalue;
       	      
       	  }
       	  return a;
    }
    
    /**
     * Generate a sorted array of given size
     * @param size
     * @return
     */
    public static int[] generateSortedArray(int size){
    	int[] a= new int[size];
    	for(int i=0;i<a.length;i++)
    	{
    		a[i]=i;
    	}
    	
    	
    	return a;
    }
    
    /**
     * Generate a reversed (sorted) array of given size
     * @param size
     * @return
     */
    public static int[] generateReversedArray(int size){
    	int[] a= new int[size];
    	for(int i=0;i<size;i++)
    	{
    		a[i]=size-i;
    	}

    	return a;
    }

    /**
     * Generate an array with few unique Keys
     * @param size
     * @return
     */
    public static int[] generateFewUniqueKeysArray(int size){
    	int[] a = new int[size];
    	a=generateRandomArray(size, 0, 20);
    	return a;
    }

	public static int[] generateArray(int size, int arrayType) {
		int[] result = null;
		switch(arrayType){
		case ARRAY_RANDOM:
			result = generateRandomArray(size);
			break;
		case ARRAY_REVERSED:
			result = generateReversedArray(size);
			break;
		case ARRAY_SORTED:
			result = generateSortedArray(size);
			break;
		case ARRAY_FEWUNIQUE:
			result = generateFewUniqueKeysArray(size);
			break;
		}
		
		return result;
	}
    
	public static String giveName(int arrayType) {
		String result = null;
		switch(arrayType){
		case ARRAY_RANDOM:
			result = "random";
			break;
		case ARRAY_REVERSED:
			result = "reversed";
			break;
		case ARRAY_SORTED:
			result = "sorted";
			break;
		case ARRAY_FEWUNIQUE:
			result = "fewunique";
			break;
		}
		
		return result;
	}
    
	
    
}

