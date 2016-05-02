import java.util.*;
import java.io.*;

public class euler18
{
	public static void main (String [] args)
	{
		final String 	IN_FILE_NAME		= "triangle2.txt";
		final int		TRIANGLE_HEIGHT 	= 100;
		int [][] triangle;
		int [][] greatestSums;
		int index;
		int rr;
		int cc;
		Scanner inFile;
		
		// Initialize Scanner Object
		try
		{
			inFile = new Scanner(new File(IN_FILE_NAME));
		}
		catch(Exception ee)
		{
			inFile = null;
			System.out.printf("Problem initiating inFile\n");
			ee.getMessage();
			System.exit(100);
		}
		
		// Instantiate Triangle array
		triangle = new int[TRIANGLE_HEIGHT][];

		for (index = 0; index < TRIANGLE_HEIGHT; index++)
		{
			triangle[index] = new int[index + 1];
		}
		// Instantiate greatestSums array
		greatestSums = new int[TRIANGLE_HEIGHT - 1][];

		for (index = 0; index < TRIANGLE_HEIGHT - 1; index++)
		{
			greatestSums[index] = new int[index + 1];
		}
		
		//Fill Triangle Array
		for( rr = 0; rr < TRIANGLE_HEIGHT; rr++)
		{
			for (cc = 0; cc < triangle[rr].length; cc++ )
			{
				if( inFile.hasNextInt())
				{
					triangle[rr][cc] = inFile.nextInt();
				}
				else
				{
					System.out.printf("Insufficient integers in row %d stopped at cc = %d",rr,cc);
				}
			}
		}

		//Fill greatestSums array
		greatestSums[greatestSums.length -1] = FillRowGreatSum(triangle[triangle.length-1], triangle[triangle.length-2]);
		for( rr = greatestSums.length - 2; rr >= 0; rr--)
		{	
			greatestSums[rr] = FillRowGreatSum(greatestSums[rr + 1], triangle[rr]);
		}
	
		System.out.println(greatestSums[0][0]);
		

//		Print2DArray(greatestSums);
	}
	public static void Print2DArray(int [][] array)
	{
		int rr;
		int cc;

		for (rr = 0; rr < array.length; rr++)
		{
//			System.out.printf("Row %3d", rr);
			for (cc = 0; cc < array[rr].length; cc++)
			{
				System.out.printf(" %3d ", array[rr][cc]);
			}
			System.out.printf("\n");
		}
	}

	private static int [] FillRowGreatSum(int [] baseRow, int [] superRow)
	{
		int index;
		int [] fillRow;

		fillRow = new int[superRow.length];
		if (fillRow.length != baseRow.length - 1)
		{
			System.err.printf("baseRow is inconsistent with other row length");
			System.exit(101);
		}

		for(index = 0; index < fillRow.length; index++)
		{
			fillRow[index] = greatest(baseRow[index],baseRow[index+1]);
			fillRow[index] += superRow[index];
		}
		return fillRow;
	}

	private static int greatest(int a, int b)
	{
		if (a>b)
			return a;
		else
			return b;
	}
}

