import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


/**
 * This class represents the I/O for getting stages
 * @author Vinh Doan
 *
 */
public class Stages {
	
	
	/**
	 * Returns int 2D array of stage based on filname 
	 * @param name - file name of stage
	 * @return an int array of the stage
	 */
	public static int[][] getStage(String name)
	{	
		return readFile(name);
	}
	
	private static int[][] readFile(String filename)
	{
		int[][] data = null;
		ArrayList<int[]> list;
		int cols;
		int rows;
		int count = 0;
		Scanner scan = null;
		try
		{
			FileReader reader = new FileReader(filename);
			scan = new Scanner(reader);
			String nl = System.getProperty("line.separator");
						
			String[] test = scan.nextLine().split(",");
			cols = test.length;
			list = new ArrayList<int[]>();
			int[] col1 = new int[cols];
			for(int i = 0; i < test.length; i++)
			{
				col1[i] = Integer.parseInt(test[i]);
			}
			list.add(col1);			
			while(scan.hasNextLine())
			{
				String[] numsInRow = scan.nextLine().split(",");
				int[] col = new int[cols];
				for(int i = 0; i < numsInRow.length; i++)
				{
					col[i] = Integer.parseInt(numsInRow[i]);
				}
				list.add(col);
			}
			scan.close();
			
			rows = list.size();
			data = new int[rows][cols];
			for(int i = 0; i < rows; i++)
			{
				data[i] = list.get(i);
			}
			
		}
		catch(FileNotFoundException ex)
		{
			System.out.println("File not found in file system.");
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
		catch (NumberFormatException ex) {
			System.out.println("File is in the wrong format.");
			return null;
		}
		finally
		{	if (scan != null)
				scan.close();
		}
		return data;
	}
	
	
}

