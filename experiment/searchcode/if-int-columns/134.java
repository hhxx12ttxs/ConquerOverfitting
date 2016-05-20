/*
 * Ahsan Zaman
 * CSC 201 - Unit 2
 * Temperature 
 * 
 * Algorithm:
 * 1. Declare and alocate Temperature two dimensional array with 2 rows and 12 columns
 * 2. Declare and initialize integer variable index to 0
 * 3. Ask user for High and Low temperatures for each individual months in the year
 * 4. After getting the input, calculate the average high and average low with their respective indexes
 * 5. Display the Average high and average low for the year.
 * 6. Find the highest temperature and its index  in the array by looking at row 1 which have the high temperatures
 * 7. Display the highest temperature and its index
 * 8. Find the lowest temperature and its index in the array by looking only at the indexes with low temperatures which is row 2
 * 9. Display the lowest temperature and its index
 * END
 */

import java.util.Scanner;
public class Source {
	public static Scanner input = new Scanner(System.in);
	public static final int ROWS = 2;
	public static final int COLUMNS = 12;
	public static int[][] arrayTemperature = new int[ROWS][COLUMNS];	// Each alternating index has high and low of a month
																		// For storing index returned by methods for highest and lowest temperatures

	
	public static void main( String[] args ){

		System.out.println( "\t\t**Temperature**\nInput high and low temperature for each month of the year and find out the average high and low.\n" );
		arrayTemperature = inputTempforYear();							// Returns an multidimensional array for all high and low temperatures in the year
		System.out.println( "\nAverage high: "+calculateAverageHigh( arrayTemperature ) );
		System.out.println( "Average low: "+calculateAverageLow( arrayTemperature ) );
		
		System.out.println( "Index for highest temperature: "+findHighestTemp( arrayTemperature ) );
		System.out.println( "Index for lowest temperature: "+findLowestTemp( arrayTemperature ) );
		System.out.println( "\n\n**END PROGRAM** " );
		
		input.close();
	}
	
	public static void inputTempforMonth( int[][] arrayTemperature, int month ){
		
		System.out.println( "Enter the high and low temperature for month #"+(month+1) );
		for( int i=0; i<ROWS ;i++ ){									// index 0 stores high temperatures and 1 stores low temperatures
			arrayTemperature[i][month] = input.nextInt();
		}
	}
	
	public static int[][] inputTempforYear(){							// Calls inputTempforMonth() to store high and low temperatures for each month
		int[][] temperature = new int[ROWS][COLUMNS];
		
		for( int i=0; i<COLUMNS ;i++ ){
			inputTempforMonth( temperature, i );
		}
		return temperature;
	}
	
	public static int calculateAverageHigh( int[][] arrayTemperature ){
		int temp=0;
		for( int i=0; i<COLUMNS ;i++ ){
			temp+=arrayTemperature[0][i];								// Sum of all high temperatures
		}
		return temp/12;													// Taking average of high temperatures
	}
	
	public static int calculateAverageLow( int[][] arrayTemperature ){
		int temp=0;
		for( int i=0; i<COLUMNS ;i++ ){
			temp+=arrayTemperature[1][i];								// Summing up all low temperatures
		}
		return temp/12;													// Taking average of low temperatures
	}
	
	public static int findHighestTemp( int[][] arrayTemperature ){
		int highestIndex=0;												// highestIndex will hold the index for the highest temperature
		for( int i=1; i<COLUMNS ;i++ ){									// Will only traverse through the high temperatures
			if( arrayTemperature[0][highestIndex]<arrayTemperature[0][i] )	
				highestIndex = i;										// Replacing index with new high's index
		}
		return highestIndex;
	}
	
	public static int findLowestTemp( int[][] arrayTemperature ){
		int lowestIndex=0;
		for( int i=1; i<COLUMNS ;i++ ){
			if( arrayTemperature[1][lowestIndex]>arrayTemperature[1][i] )
					lowestIndex = i;									// subsequently, the index is changed to the new low's index
		}
		return lowestIndex;
	}
	
}

