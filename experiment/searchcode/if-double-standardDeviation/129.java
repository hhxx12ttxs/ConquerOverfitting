package util;

import java.util.Random;

import main.Main;

/**
 * A series of utility/external support methods
 * @author pedro
 *
 */
public final class Util 
{
	public static final int IS_WINDOWS = 1;
	public static final int IS_MAC = 2;
	public static final int IS_LINUX = 3;
	
	/**
	 * Compares two given double arrays by value
	 * @param arr1 - First array
	 * @param arr2 - Second array
	 * @return true if every element in arr1 matches its arr2 counterpart, false o.w.
	 */
	public static boolean compareArraysByValue(double[] arr1, double[] arr2)
	{
		if (arr1 == null || arr2 == null || arr1.length != arr2.length)
		{
			return false;
		}
		
		for(int x=0; x<arr1.length; x++)
		{
			if (arr1[x] != arr2[x])
				return false;
		}
		
		return true;
	}
	
	/**
	 * Compares two given Comparable arrays by value
	 * 
	 * @param arr1 - First array
	 * @param arr2 - Second array
	 * @return true iff every element in arr1 matches its arr2 counterpart , false o.w.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean compareArraysByValue( Comparable[] arr1, Comparable[] arr2)
	{
		if (arr1 == null || arr2 == null || arr1.length != arr2.length)
		{
			return false;
		}
		
		for (int x = 0; x < arr1.length; x++)
		{
			if (arr1[x].compareTo(arr2[x]) != 0)
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Attempts to cast an Object into a double value without throwing an exception
	 * 
	 * @param obj - the Object to be cast
	 * @return the double value of the Object if it is of numerical type, o.w. Double.NaN
	 */
	public static double attemptValueCast(Object obj)
	{
		double d = Double.NaN;
		
		try{
			d = (Double) obj;
		}
		catch(Exception e){}
		try{
			d = (Integer) obj;
		}
		catch(Exception e){}
		try{
			d = (Float) obj;
		}
		catch(Exception e){}
		
		return d;
	}

	/**
	 * Truncates value to number of decimal places
	 * @param value
	 * @param numDecimalPlaces
	 * @return
	 */
	public static double truncate(double value, short numDecimalPlaces)
	{
		  return Math.floor(value * Math.pow(10, numDecimalPlaces))/Math.pow(10, numDecimalPlaces);
	}

	/**
	 * Generates random value b.w. two numbers(inclusive), with number of decimals
	 * 
	 * @param min - Minimum value (inclusive)
	 * @param max - Maximum value (inclusive)
	 * @param numDecimalPlaces - Number of decimal places
	 * @return Pseudorandom value truncated to number of decimal places
	 */
	public static double randomScaledValue(double min, double max, short numDecimalPlaces) 
	{
		double scale = Math.pow(10, numDecimalPlaces);
		min *= scale; max *= scale;
		double value = min + (int)(Math.random() * ((max - min) + 1));
		
		value *= (1/scale);
		
		return truncate(value, numDecimalPlaces);
	}
	
	//TODO add javadoc
	public static double[] stringArrayToDouble(String[] arr)
	{
		if (arr == null || arr.length == 0) return null;
		
		double[] new_arr = new double[arr.length];
		
		for (int x = 0; x < arr.length; x++)
		{
			new_arr[x] = Double.parseDouble(arr[x]);
		}
		
		return new_arr;
	}
	
	/**
	 * Returns the equivalent String printed by Util.printArray
	 * 
	 * Printed string is of the form:
	 * [val1 , val2 , val3 , {...} , valN]
	 * 
	 * @param arr - the array to be "Stringed"
	 * @return standard String representation of arr and its elements
	 */
	public static <T> String arrayToString(T[] arr)
	{
		if ( arr == null || arr.length == 0) return "[ ]";
		
		String output = "[";
		
		for (int x=0; x<arr.length; x++)
		{
			output += ""+arr[x];
			
			if (x+1 < arr.length)
			{
				output += ",";
			}
			else
			{
				output += "]";
			}
		}
		
		return output;
	}
	
	/**
	 * Returns the equivalent String printed by Util.printArray
	 * 
	 * Printed string is of the form:
	 * [val1 , val2 , val3 , {...} , valN]
	 * 
	 * @param arr - the array to be "Stringed"
	 * @return standard String representation of arr and its elements
	 */
	public static String arrayToString(double[] arr) 
	{
		if ( arr == null || arr.length == 0) return "[ ]";
		
		String output = "[";
		
		for (int x=0; x<arr.length; x++)
		{
			output += ""+arr[x];
			
			if (x+1 < arr.length)
			{
				output += ",";
			}
			else
			{
				output += "]";
			}
		}
		
		return output;
	}
	
	/**
	 * Same as getGaussianValue(mean, stdev), but only returns positive values.
	 * May produce statistically-skewed deviations.
	 * 
	 * @param mean
	 * @param stdev
	 * @return
	 */
	public static double getPositiveGaussianValue(double mean, double stdev)
	{
		double value;
		
		do
		{
			value = Gaussian.getGaussianValue(mean, stdev);
			
		}while(value <= 0);
		
		return value;
	}
	
	/**
	 * Get a random Gaussian value between 0.0 and 1.0
	 * @return
	 */
	public static double getPositiveGaussianValue()
	{
		Random r = new Random();
		double value;
		
		do
		{
			 value = r.nextGaussian();
			
		}while(value <= 0);
		
		return value;
	}
	
	/**
	 * Calculates the variance of a given population
	 * 
	 * @param population - double[] of population values
	 * @return the variance of the population
	 */
	public static double variance(double[] population) 
	{
        long n = 0;
        double mean = 0;
        double s = 0.0;
 
        for (double x : population) {
                n++;
                double delta = x - mean;
                mean += delta / n;
                s += delta * (x - mean);
        }
        // if you want to calculate std deviation
        // of a sample change this to (s/(n-1))
        return (s / n);
	}
 
	/**
	 * Calculates the standard deviation of a population
	 * 
	 * @param population an array, the population
	 * @return 	the standard deviation of the population
	 * 				 OR 
	 * 			NaN if the population is empty
	 */
	public static double standardDeviation(double[] population) 
	{
		return (population == null || population.length == 0)? Double.NaN:Math.sqrt(variance(population));
	}

	/**
	 * Calculates the sum of an array's value, even if certain elements are missing
	 * 
	 * @param array - the array to be summed over
	 * @return the sum of array's values
	 */
	public static double arraySum(double[] array)
	{
		double total = 0;
		
		for(double d : array)
			total += d;
		
		return total;
	}
	
	public static double magnitude(double[] vector)
	{
		double sum = 0;
		
		for (double d : vector)
		{
			sum += Math.pow(d, 2);
		}
		
		return Math.sqrt(sum);
	}
	
	/**
	 * Calculates mean of an array.
	 * @param array
	 * @return the mean of the array if it exists, o.w. NaN
	 */
	public static double arrayMean(double[] array)
	{
		if (array == null)
			return Double.NaN;
		else if (array.length == 0)
			return 0;
		
		double mean, sum;
		
		sum = Util.arraySum(array);
		mean = sum / array.length;
		
		return mean;
	}

	/**
	 * Checks to see if given array of type E contains given value
	 * 
	 * @param <E> -     array and value type
	 * @param arr -     array of type E
	 * @param value -   value of type E
	 * @return true if value in arr, false otherwise
	 */
	public static <E> boolean arrayContains(E[] arr, E value)
	{
		for(E element : arr)
		{
			if(element.equals(value))
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * Checks to see if given double[] array contains a given value
	 * 
	 * @param arr
	 * @param value
	 * @return true if value in arr, false otherwise
	 */
	public static boolean arrayContains(double[] arr, double val)
	{
		for(double element : arr)
		{
			if (element == val) return true;
		}
		
		return false;
	}
	
	/**
	 * Checks to see if given double[] array contains at least one instance
	 * of the given values each
	 * 
	 * @param arr
	 * @param value
	 * @return true if all value occur at least once in arr, false otherwise
	 */
	public static boolean arrayContains(double[] arr, double[] values)
	{
		for(double val : values)
		{
			if ( !Util.arrayContains(arr, val)) return false;
		}
		
		return true;
	}
	
	/**
	 * Standard method for printing an array (i.e. [6,42,24]) of values
	 * @param arr
	 */
	public static void printArray(double[] arr)
	{
		System.out.println();
		System.out.print("[");
		
		for (int x=0; x<arr.length; x++)
		{
			System.out.print(arr[x]);
			
			if (x+1 < arr.length)
			{
				System.out.print(",");
			}
			else
			{
				System.out.print("]");
			}
		}
		
		System.out.println();
	}
	
	//TODO add javadoc to arrayToCSV
	public static String arrayToCSV(double[] arr)
	{
		if ( arr == null || arr.length == 0) return null;
		
		String output = "";
		
		for (int x=0; x<arr.length; x++)
		{
			output += ""+arr[x];
			
			if (x+1 < arr.length)
			{
				output += ",";
			}
			else
			{
				output += "";
			}
		}
		
		return output;
	}

	/**
	 * Same as generateRandomSummedArray, but using Gaussian distribution with given params.  
	 * Guaranteed to be non-negative!
	 * 
	 * @param size - Number of array elements
	 * @param totalSum - Expected sum of all values
	 * @param mean - the mean of the expected gaussian
	 * @param stdev - the standard deviation of the expected gaussian
	 * @return shuffled, gaussian array
	 * @deprecated
	 * 	 */
	public static double[] summedGaussianArray(int size, double totalSum, double mean, double stdev)
	{
		double[] result = new double[size];
	    
	    double sum = 0;
	    
	    for (int i = 0; i < size; i++) 
	    {
	        result[i] = Util.getPositiveGaussianValue(mean, stdev);
	        sum+=result[i];
	    }
	    
	    for (int i = 0; i < size; i++) 
	    {
	        result[i] /= sum;
	        result[i] *= totalSum;
	    }
	    
	    return Util.shuffle(result);
	}
	
	/**
	 * Same as generateRandomSummedArray, but using Gaussian distribution. 
	 * Guaranteed to be non-negative!
	 * @param size - Number of array elements
	 * @param totalSum - Expected sum of all values
	 * @return shuffled, gaussian array
	 * 	 */
	public static double[] summedGaussianArray(int size, double totalSum) 
	{
	    double[] result = new double[size];
	    
	    double sum = 0;
	    
	    for (int i = 0; i < size; i++) 
	    {
	        result[i] = Math.sin((i+1)/((double)size+1) * Math.PI);
	        sum+=result[i];
	    }
	    
	    for (int i = 0; i < size; i++) 
	    {
	        result[i] /= sum;
	        result[i] *= totalSum;
	    }
	    
	    return Util.shuffle(result);
	}

	/**
	 * Shuffles given array randomly by value
	 * @param array to be shuffled
	 * @return reference to shuffled array
	 */
	public static double[] shuffle(double[] array)
	{
		Random r = new Random();
		
		for (int i=0; i<array.length; i++) 
		{
		    int randomPosition = r.nextInt(array.length);
		    double temp = array[i];
		    array[i] = array[randomPosition];
		    array[randomPosition] = temp;
		}
		
		return array;
	}

	/**
	 * Returns an array of a specified # of elements adding up to a sum
	 * NOT GUARANTEED TO BE NON-NEGATIVE
	 * @deprecated
	 */
	public static double[] generateRandomSummedArray(int numElements, int totalSum)
	{
		double[] result = new double[numElements];
		Random r = new Random();
		
		double sum = result[0] = r.nextInt(totalSum);
		
		for(int x=1; x < result.length-1; x++)
		{
			int max = (int)(sum - result[x-1]);
			result[x] = randomScaledValue(1, max, Main.PRECISION);
			sum = arraySum(result);
			
			if(sum > totalSum)	
			{
				return null;
			}
			
		}
		
		result[numElements-1] = totalSum - sum;
		
		return shuffle(result);
	}
	
	/**
	 * Detects which OS NeuroBaldr is running from
	 * @return
	 */
	public static int getOS()
	{
		if (isWindows())
		{
			return IS_WINDOWS;
		}
		else if (isMac())
		{
			return IS_MAC;
		}
		else if (isUnix())
		{
			return IS_LINUX;
		}
		else
		{
			return -1;
		}
	}
	
	/**
	 * Returns array of type [1,2,3,...,n] given length n
	 * @param length
	 * @return array of type [1,2,3,...,length]
	 */
	public static double[] getCountedArray(int length)
	{
		double[] arr = new double[length];
		
		for (int x = 0; x<length; x++)
			arr[x] = x+1;
		
		return arr;
	}
	
	/**
	 * Calculates the dot-product of two double-valued vectors.
	 * 
	 * @param arr1 - the first vector
	 * @param arr2 - the second vector
	 * @return arr1*arr2, if they are of the same dimension, o.w. NaN
	 */
	public static double dotProduct(double[] arr1, double[] arr2)
	{
		if (arr1 == null || arr2 == null || arr1.length != arr2.length)
			return Double.NaN;
		else if (arr1.length == 0)
			return 0;
		
		double sum = 0;
		
		for (int x = 0; x < arr1.length; x++)
		{
			sum += arr1[x] * arr2[x];
		}
		
		return sum;
	} 
	
	public static double angleBetweenVectors(double[] arr1, double[] arr2)
	{
		if (arr1 == null || arr2 == null || arr1.length != arr2.length)
			return Double.NaN;
		else if (arr1.length == 0)
			return 0;
		
		double angle = Double.NaN;
		
		angle = Math.acos( (Util.dotProduct(arr1, arr2)) / (Util.vectorMagnitude(arr1)*Util.vectorMagnitude(arr2)) );
		
		return angle;
	}
	
	public static double vectorMagnitude(double[] v)
	{
		if (v == null || v.length == 0) return Double.NaN;
		
		double magnitude = 0;
	
		for (double d : v)
		{
			magnitude += d*d;
		}
		
		magnitude = Math.sqrt(magnitude);
		
		return magnitude;
	}
	
	
	public static String[] truncateLinesAtChar(String[] arr, char c)
	{
		for (int x = 0; x < arr.length; x++)
		{
			arr[x] = arr[x].substring(arr[x].indexOf(c)+1);
			
			arr[x] = arr[x].trim();
		}
		
		return arr;
	}
	

	public static String removeChar(String s, char c) 
	{
		String r = "";

		for (int i = 0; i < s.length(); i ++) 
		{
			if (s.charAt(i) != c) r += s.charAt(i);
   		}

   		return r;
	}
	
	private static boolean isWindows(){
		 
		String os = System.getProperty("os.name").toLowerCase();
		//windows
	    return (os.indexOf( "win" ) >= 0); 
 
	}
 
	private static boolean isMac(){
 
		String os = System.getProperty("os.name").toLowerCase();
		//Mac
	    return (os.indexOf( "mac" ) >= 0); 
 
	}
 
	private static boolean isUnix(){
 
		String os = System.getProperty("os.name").toLowerCase();
		//linux or unix
	    return (os.indexOf( "nix") >=0 || os.indexOf( "nux") >=0);
 
	}
}

