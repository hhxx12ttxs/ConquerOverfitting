package howto;

import howto.constrainedOptimization.Settings;
import howto.database.DatabaseInterface;
import howto.parser.Parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public final class Utilities {
	

	/**
	 * Looks for a string in a list of strings
	 * @param strList	the list of strings
	 * @param str		the string to look for
	 * @return			the index of the string in the array, or -1 if it is not found
	 */
	public static int findStringInArray(String[] strList, String str){
		if (str == null)
			return -1;
		
		for(int i = 0; i < strList.length; i++){
			if(strList[i].equals(str))
				return i;
		}
		return -1;
	}
	
	/**
	 * Looks for a string in a list of strings that could be in complex form, e.g. "x" appears in "x + 5"
	 * Only looks for parts of arithmetic expressions, not substrings.
	 * @param strList	the list of strings
	 * @param str		the string to look for
	 * @return			the index of the string in the array, or -1 if it is not found
	 */
	public static int findStringInArrayComplex(String[] strList, String str){
		if(str == null)
			return -1;
		
		for(int i = 0; i < strList.length; i++){
			if(isArithmeticExpression(strList[i])){
				String[] elem = getArithmeticElements(strList[i]);
				for(int j = 0; j < elem.length; j++){
					if(elem[j].equals(str))
						return i;
				}
			}
			else{
				if(strList[i].equals(str))
					return i;
			}
		}
		return -1;
	}
	
	public static int findInArray(int[] array, int num){
		if(array == null)
			return -1;
		
		for(int i = 0; i < array.length; i++){
			if(array[i] == num)
				return i;
		}
		
		return -1;
	}
	
	/**
	 * Computes the intersection of two arrays
	 * @param s1	first array
	 * @param s2	second array
	 * @return		the common elements
	 */
	public static String[] intersection(String[] s1, String[] s2){
		if(s1 == null || s2 == null)
			return null;
		
		String[] intersect = new String[0];
		for(int i = 0; i < s1.length; i++){
			if(findStringInArray(s2, s1[i]) >= 0)
				intersect = addToArray(intersect, s1[i]);
		}
		
		return intersect;
	}
	
	/**
	 * Computes the intersection of two arrays
	 * @param s1	first array
	 * @param s2	second array
	 * @return		the common elements
	 */
	public static int[] intersection(int[] s1, int[] s2){
		if(s1 == null || s2 == null)
			return null;
		
		int[] intersect = new int[0];
		for(int i = 0; i < s1.length; i++){
			if(findInArray(s2, s1[i]) >= 0)
				intersect = addToArray(intersect, s1[i]);
		}
		
		return intersect;
	}
	
	/**
	 * Returns the elements of s1 that do not appear in s2
	 * @param s1	
	 * @param s2
	 * @return		s1-s2
	 */
	public static String[] difference(String[] s1, String[] s2){
		if(s1 == null)
			return null;
		
		if(s2 == null)
			return s1;
		
		String[] diff = new String[0];
		for(int i = 0; i < s1.length; i++){
			if(!(findStringInArray(s2, s1[i]) >= 0))
				diff = addToArray(diff, s1[i]);
		}
		
		return diff;
	}
	
	/**
	 * Returns the union of the 2 string arrays
	 * @param s1	
	 * @param s2
	 * @return		all elements in s1 and s2
	 */
	public static String[] union(String[] s1, String[] s2){
		if(s1 == null)
			return s2;
		
		
		String[] all = new String[s1.length];
		System.arraycopy(s1, 0, all, 0, s1.length);
		
		if(s2 != null){
			for(int i = 0; i < s2.length; i++){
				if(!(findStringInArray(s1, s2[i]) >= 0))
					all = addToArray(all, s2[i]);
			}
		}
		
		return all;
	}
	
	/**
	 * Looks for a string in a list of strings.
	 * REQUIRES: the list to be sorted
	 * @param strList	the list of strings (sorted)
	 * @param str		the string to look for
	 * @return			the index of the string in the array, or -1 if it is not found
	 */
	public static int findStringInSortedArray(String[] strList, String str){
		if (str == null)
			return -1;
		return java.util.Arrays.binarySearch(strList, str);
	}
	
	/**
	 * Replicates an integer array
	 * @param array1	the array to replicate
	 * @return			a copy of the array
	 */
	public static int[] replicateArray(int[] array1){
		int[] array2 = new int[array1.length];
		System.arraycopy(array1, 0, array2, 0, array1.length);
		return array2;
	}
	
	/**
	 * Replicates an array of double
	 * @param array1	the array to replicate
	 * @return			a copy of the array
	 */
	public static double[] replicateArray(double[] array1){
		double[] array2 = new double[array1.length];
		System.arraycopy(array1, 0, array2, 0, array1.length);
		return array2;
	}
	
	/**
	 * Replicates a String array
	 * @param array1	the array to replicate
	 * @return			a copy of the array
	 */
	public static String[] replicateArray(String[] array1){
		String[] array2 = new String[array1.length];
		System.arraycopy(array1, 0, array2, 0, array1.length);
		return array2;
	}
	
	/**
	 * Creates a string based on an integer array, separated by spaces
	 * @param out	an integer array
	 * @param delim	a delimiter to separate the elements
	 * @return		a space separated string with the integers of the array
	 */
	public static String printArray(int[] out, String delim){
		if(out == null)
			return "";
		
		String s = "";
		if(out.length < 1)
			return s;
		
		s = s + out[0];
		for(int i=1; i<out.length; i++){
			s = s + delim + out[i];
		}
		
		return s;		
	}
	
	/**
	 * Creates a string based on a double array, separated by spaces
	 * 
	 * @param out	a double array
	 * @param delim	a delimiter to separate the elements
	 * @return		a space separated string with the numbers of the array
	 */
	public static String printArray(double[] out, String delim){
		if(out == null)
			return "";
		
		String s = "";
		if(out.length < 1)
			return s;
		
		s = s + out[0];
		for(int i=1; i<out.length; i++){
			s = s + delim + out[i];
		}
		
		return s;		
	}
	
	/**
	 * Creates a single string based on an array of strings
	 * 
	 * @param out	a string array
	 * @param delim	a delimiter to separate the elements
	 * @return		a space separated string with the strings of the array
	 */
	public static String printArray(String[] out, String delim){
		if(out == null)
			return "";
		
		String s = "";
		if(out.length < 1)
			return s;
		
		s = s + out[0];
		for(int i=1; i<out.length; i++){
			s = s + delim + out[i];
		}
		
		return s;		
	}

	/**
	 * Adds a new element to an array of string
	 * @param strArray		the original array
	 * @param str			the string to add
	 * @return				the augmented array
	 */
	public static String[] addToArray(String[] strArray, String str){
		int currentSize;
		
		if(strArray == null)
			currentSize = 0;
		else
			currentSize = strArray.length;
			
		String[] newArray = new String[currentSize + 1];
		if(currentSize > 0)
			System.arraycopy(strArray, 0, newArray, 0, currentSize);
		
		newArray[currentSize] = str;
		
		return newArray;
	}
	
	/**
	 * Removes an element from an array
	 * @param strArray		the array of Strings
	 * @param index			the index of the element to remove
	 * @return				a new array without the element
	 */
	public static String[] removeFromArray(String[] strArray, int index){
		if(strArray == null || index < 0 || index >= strArray.length)
			return strArray;
		
		int currentSize = strArray.length;
		
		String[] newArray =  new String[currentSize - 1];
		System.arraycopy(strArray, 0, newArray, 0, index); //copies up to element index-1
		System.arraycopy(strArray, index + 1, newArray, index, currentSize - index - 1);
		
		return newArray;
	}
	
	/**
	 * Adds a new element to an array of integers
	 * @param intArray		the original array
	 * @param newInt			the string to add
	 * @return				the augmented array
	 */
	public static int[] addToArray(int[] intArray, int newInt){
		int currentSize;
		
		if(intArray == null)
			currentSize = 0;
		else
			currentSize = intArray.length;
			
		int[] newArray = new int[currentSize + 1];
		if(currentSize > 0)
			System.arraycopy(intArray, 0, newArray, 0, currentSize);
		
		newArray[currentSize] = newInt;
		
		return newArray;
	}
	
	/**
	 * Merges two sorted arrays of type double[] into one sorted array, without duplicating elements
	 * @param array1	first array to be merged (sorted)
	 * @param array2	second array to be merged (sorted)
	 * @return			a sorted array with the intersection of the elements of the two input arrays
	 */
	public static double[] mergeSortedArrays(double[] array1, double[] array2){
		
		if(array1 == null && array2 == null)
			return null;
		
		if(array1 == null)
			return replicateArray(array2);
		
		if(array2 == null)
			return replicateArray(array1);
		
		double[] tmpArray = new double[array1.length + array2.length];
		int tmpIndex = 0;
		int index1 = 0;
		
		for(int i = 0; i < array2.length; i++){
			while(index1 < array1.length && array1[index1] < array2[i]){
				tmpArray[tmpIndex] = array1[index1];
				tmpIndex++;
				index1++;
			}
			
			if(index1 >= array1.length){
				System.arraycopy(array2, i, tmpArray, tmpIndex, array2.length - i);
				tmpIndex = tmpIndex + array2.length - i;
				break;
			}
			
			if(array1[index1] == array2[i]){
				continue;
			}
			else{
				tmpArray[tmpIndex] = array2[i];
				tmpIndex++;
			}
		}
		
		if(index1 < array1.length){
			System.arraycopy(array1, index1, tmpArray, tmpIndex, array1.length - index1);
			tmpIndex = tmpIndex + array1.length - index1;
		}
		double[] finalArray = new double[tmpIndex];
		System.arraycopy(tmpArray, 0, finalArray, 0, tmpIndex);
		
		return finalArray;
	}
	
	/**
	 * Merges two sorted arrays of type int[] into one sorted array, without duplicating elements
	 * @param array1	first array to be merged (sorted)
	 * @param array2	second array to be merged (sorted)
	 * @return			a sorted array with the intersection of the elements of the two input arrays
	 */
	public static int[] mergeSortedArrays(int[] array1, int[] array2){
		
		if(array1 == null && array2 == null)
			return null;
		
		if(array1 == null)
			return replicateArray(array2);
		
		if(array2 == null)
			return replicateArray(array1);
		
		int[] tmpArray = new int[array1.length + array2.length];
		int tmpIndex = 0;
		int index1 = 0;
		
		for(int i = 0; i < array2.length; i++){
			while(index1 < array1.length && array1[index1] < array2[i]){
				tmpArray[tmpIndex] = array1[index1];
				tmpIndex++;
				index1++;
			}
			
			if(index1 >= array1.length){
				System.arraycopy(array2, i, tmpArray, tmpIndex, array2.length - i);
				tmpIndex = tmpIndex + array2.length - i;
				break;
			}
			
			if(array1[index1] == array2[i]){
				continue;
			}
			else{
				tmpArray[tmpIndex] = array2[i];
				tmpIndex++;
			}
				
		}
		
		if(index1 < array1.length){
			System.arraycopy(array1, index1, tmpArray, tmpIndex, array1.length - index1);
			tmpIndex = tmpIndex + array1.length - index1;
		}
		int[] finalArray = new int[tmpIndex];
		System.arraycopy(tmpArray, 0, finalArray, 0, tmpIndex);
		
		return finalArray;
	}
	
	/**
	 * Merges two sorted arrays of type String[] into one sorted array, without duplicating elements
	 * @param array1	first array to be merged (sorted)
	 * @param array2	second array to be merged (sorted)
	 * @return			a sorted array with the intersection of the elements of the two input arrays
	 */
	public static String[] mergeSortedArrays(String[] array1, String[] array2){
		if(array1 == null && array2 == null)
			return null;
		
		if(array1 == null)
			return replicateArray(array2);
		
		if(array2 == null)
			return replicateArray(array1);
		
		String[] tmpArray = new String[array1.length + array2.length];
		int tmpIndex = 0;
		int index1 = 0;
		
		for(int i = 0; i < array2.length; i++){
			while(index1 < array1.length && array1[index1].compareTo(array2[i]) < 0){
				tmpArray[tmpIndex] = array1[index1];
				tmpIndex++;
				index1++;
			}
			
			if(index1 >= array1.length){
				System.arraycopy(array2, i, tmpArray, tmpIndex, array2.length - i);
				tmpIndex = tmpIndex + array2.length - i;
				break;
			}
			
			if(array1[index1] == array2[i]){
				continue;
			}
			else{
				tmpArray[tmpIndex] = array2[i];
				tmpIndex++;
			}
				
		}
		
		if(index1 < array1.length){
			System.arraycopy(array1, index1, tmpArray, tmpIndex, array1.length - index1);
			tmpIndex = tmpIndex + array1.length - index1;
		}
		String[] finalArray = new String[tmpIndex];
		System.arraycopy(tmpArray, 0, finalArray, 0, tmpIndex);
		
		return finalArray;
	}
	
	/**
	 * Checks if a string is a number.
	 * @param inputData	the string to check.
	 * @return			true if the input is a number, false otherwise.
	 */
	public static boolean isNumeric(String inputData) {
		  return inputData.matches("[-+]?\\d+(\\.\\d+)?");
		}
	
	/**
	 * Checks if a string represents a string constant, i.e. is surrounded by single quotes
	 * @param input		the string to check
	 * @return			true if the input is a string constant, false otherwise
	 */
	public static boolean isStringConstant(String input){
		if(input.startsWith("'") && input.endsWith("'"))
			return true;
		else return false;
	}
	
	/**
	 * Checks if a given string contains an arithmetic expression
	 * @param input			
	 * @return			true if the string contains +,-,*, or /, false otherwise
	 */
	public static boolean isArithmeticExpression(String input){
		if(input.contains("+") || input.contains("-") || input.contains("*") || input.contains("/"))
			return true;
		else
			return false;
	}
	
	
	/**
	 * Retrieves the list of operators in a given arithmetic expression
	 * @param input			an arithmetic expression (e.g. "x+z-y")
	 * @return				a list of operators (e.g {"+","-"}
	 */
	public static String[] getArithmeticOperators(String input){
		Scanner sc = new Scanner(input);
		sc.useDelimiter("\\w*\\b\\s*");			//delimiters are words
		
		String[] op = new String[0];
		
		while(sc.hasNext()){
			op = addToArray(op, sc.next());
		}
		
		return op;
	}
	
	/**
	 * Retrieves the list of elements in a given arithmetic expression
	 * @param input			an arithmetic expression (e.g. "x+z-y")
	 * @return				a list of operators (e.g {"x","z", "y"}
	 */
	public static String[] getArithmeticElements(String input){
		Scanner sc = new Scanner(input);
		sc.useDelimiter("\\s*\\W\\s*");			//delimiters are words
		
		String[] op = new String[0];
		
		while(sc.hasNext()){
			op = addToArray(op, sc.next());
		}
		
		return op;
	}
	
	/**
	 * Returns the elements that correspond to a given set of indexes
	 * @param strArray			the original array of all elements
	 * @param indexes			a set of indexes
	 * @return					the elements that correspond to the indices
	 */
	public static String[] getElements(String[] strArray, int[] indexes){
		if(strArray == null || indexes == null)
			return null;
		
		String[] res = new String[indexes.length];
		
		for(int i = 0; i < indexes.length; i++){
			if(indexes[i] >= strArray.length)
				continue;
			res[i] = strArray[indexes[i]];
		}
		
		return res;
	}
	
	/**
	 * Computes the number of elements that 2 arrays have in common
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static int overlap(String[] s1, String[] s2){
		String[] inter = intersection(s1,s2);
		if(inter == null || inter.length < 1)
			return 0;
		
		else
			return inter.length;
	}
	

	
	/** 
	 * Finds the indexes of a set of strings in an array
	 * @param strArray
	 * @param elem
	 * @return
	 */
	public static int[] getElementIndexes(String[] strArray, String[] elem){
		if(strArray == null || elem == null)
			return null;
		
		int[] indexes = new int[elem.length];
		
		for(int i = 0; i < elem.length; i++){
			indexes[i] = findStringInArray(strArray, elem[i]);
		}
		
		return indexes;
	}
	
	/**
	 * Find the indexes of all occurrences of a string in an array of strings
	 * @param strArray		The array of strings
	 * @param elem			The string to look for
	 * @return				The list of indices of all occurrences. Returns null if any of the inputs are null. Returns 
	 * 						an empty array if the element is not found
	 */
	public static int[] getAllOccurencies(String[] strArray, String elem){
		if(strArray == null || elem == null)
			return null;
		
		String[] str = Utilities.replicateArray(strArray);
		
		int index = findStringInArray(str, elem);
		int[] indexArray = new int[0];
		
		while(index >= 0){
			indexArray = Utilities.addToArray(indexArray, index);
			str[index] = "randomString167514";
			index = findStringInArray(str, elem);
		}
		
		return indexArray;
	}
	
	/**
	 * Writes a string to a file. If the file exists, it will be overwritten
	 * @param filename	The name of the file
	 * @param s			The string to write to the file
	 */
	public static void writeToFile(String filename, String s){
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			out.write(s);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Appends a string to a file. It will write to the end of the file, or create it if it doesn't exist.
	 * @param filename		The name of the file
	 * @param s				The string to write
	 */
	public static void appendToFile(String filename, String s){
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename, true));
			out.write(s);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void setProblemSize(String filename, int size){
		String inFile = filename + "_template";
		String outFile = filename;
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(inFile));
			BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
			
			String currentLine;
			
			while((currentLine = in.readLine()) != null){
				String tmpS = currentLine.replace("[SIZE]", "" + size);
				out.write(tmpS + "\n");
			}
			
			out.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Appends a file to the end of the first file
	 * @param filename
	 * @param filename2
	 */
	public static void appendFileToFile(String filename, String filename2){
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename2));
			String currentLine;
			
			while ((currentLine = in.readLine()) != null) {
				appendToFile(filename, currentLine + "\n");
			}

			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Delete a file with a given name
	 * @param filename
	 */
	public static void deleteFile(String filename){
		File f1 = new File(filename);
		f1.delete();
	}
	
	public static void println(String s){
		if(!Settings.suppressOutput)
			System.out.println(s);
	}
	
	public static void print(String s){
		if(!Settings.suppressOutput)
			System.out.print(s);
	}
	
	/**
	 * Pulls the appropriate data from the "log" table in the database, to modify a customizable constraint
	 * @param filename		The name of the file where the customizable constraint is
	 */
	public static void updateCustomizedStatement(String filename){
		DatabaseInterface db = new DatabaseInterface();
		ResultSet rSet = db.executeQuery("select * from log");	//The name of the constraint table is hard-coded
						
		try {
			String defaultName = null;
			
			while(rSet.next()){
				//The attribute names of the constraint table are hard-coded
				String name = rSet.getString("name");
				String symbol = rSet.getString("sign");
				String value = rSet.getString("value");
				String version = rSet.getString("version");
				
				
				
				if(version.equals("0")){
					defaultName = name;
				}
				
				//Version 0 denotes the default constraint if it exists
				if((!version.equals("0")) && (!name.equals(defaultName))){
					name = "\'" + name +"\'";			//put quotes if it is a constant
				}
				
				System.out.println("( "+ name +", " + symbol + ", "+ value + ", " + version + " )");
				
				String newStmt = Parser.getRefinement(filename, name, symbol, value);
				appendToFile(Settings.customizedFile, newStmt + "\n");
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Customizes an input file, based on a list of placeholder names and a list of values. The method
	 * replaces each placeholder name with the corresponding value, and stores the output in the out file.
	 * 
	 * @param inFile		the input file
	 * @param outFile		the output file
	 * @param varNames		the patterns to be changed
	 * @param varValues		the values to replace each pattern
	 */
	public static void customizeFile(String inFile, String outFile, String[] varNames, String[] varValues){
		if(varNames == null || varValues == null)
			return;
		
		if(varNames.length != varValues.length)
			return;

		
		try {
			BufferedReader in = new BufferedReader(new FileReader(inFile));
			
			String currentLine;
			
			writeToFile(outFile, "");
			
			while((currentLine  = in.readLine()) != null){
				for(int i = 0; i < varNames.length; i++){
					String pattern = varNames[i];
					String value = varValues[i];
					currentLine = currentLine.replace(pattern, value);
				}
				
				appendToFile(outFile, currentLine + "\n");
			}
			
			in.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

					

	}
	
	/**
	 * Returns the first line in a file as a string.
	 * @param filename		the name of the file
	 * @return				the first line in the file
	 */
	public static String readLineFromFile(String filename){
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String currentLine = in.readLine();

			in.close();
			return currentLine;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}

