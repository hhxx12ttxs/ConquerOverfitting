package rosita.linkage.util;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

//Encryption imports
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

import rosita.linkage.deprecated.Record;


/**
 * Utility class to help with 
 *  - Reading Record data from Databases & Files
 *  - Reading DB credentials from a properties file
 *  - Writing Record data to Database, File, and STDOUT
 *  - Time calculations
 *  - String Formatting
 * @author Brandon Abbott
 *
 */
public class MyUtilities 
{

	/** 
	 * Suppress default constructor for non-instantiability
	 */
	private MyUtilities() 
	{
		throw new AssertionError();
	}

	// Column Names References
	public static final String LASTNAME = "LASTNAME";
	public static final String FIRSTNAME = "FIRSTNAME";
	public static final String SSN = "SSN";
	public static final String DOB = "DOB";
	public static final String DOBHASH = "DOBHASH";
	public static final String GENDER = "GENDER";
	public static final String ADDR_1 = "ADDR_1";
	public static final String CITY = "CITY";
	public static final String STATE = "STATE";
	public static final String ZIP = "ZIP";
	public static final String PHONE = "PHONE";
	public static final String KEYWORD_EXTRA = "EXTRA";

	// The set of keys to use for the HMAC SHA1
	private static String[] HMACkeys = populateKeys();
	// The Algorithm to use for the HMAC_SHA1 computation
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

	/**
	 * This for loop converts the byte array digest of the function
	 * into a long
	 * @param inArray -input
	 * @return - input converted to long
	 */
	public static long byteArrayToLong(byte[] inArray){
		long h = 0;
		for (int i = 0; i < 4; i++) {
			h <<= 8;
			h |= ((int) inArray[i]) & 0xFF;
		}
		return h;
	}
	/**
	 * This for loop converts the byte array digest of the function
	 * into a String
	 * @param inArray -input
	 * @return - input converted to String
	 */
	public static String byteArrayToString(byte[] inArray){
		try {
			return new String(inArray, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * Converts String into Bi-grams as an ArrayList of size 2 char[] arrays
	 * @param input - The string to be converted
	 * @return - The array list of bi-grams
	 */
	public static ArrayList<char[]> convertToBigrams(String input)
	{
		// TODO: make sure this function is efficient 
		ArrayList<char[]> charList = new ArrayList<char[]>();
		char left;
		char right;
		input = " " + input + " ";
		for(int i = 0; i < input.length()-1; i++){
			left  = input.charAt(i);
			right = input.charAt(i+1);
			char[] temp = {left, right };
			charList.add(temp);
		}
		return charList;
	}

	// TODO: function description goes here
	public static String convertToHex(byte[] data) { 
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) { 
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do { 
				if ((0 <= halfbyte) && (halfbyte <= 9)) 
					buf.append((char) ('0' + halfbyte));
				else 
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while(two_halfs++ < 1);
		} 
		return buf.toString();
	} 

	// TODO: function desc
	public static String convertStringArrayToCSV(String[] s)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length; i++) {

			if (!s[i].equalsIgnoreCase("PK")){
				sb.append(s[i]);
			}

			if (i != s.length - 1)
				if (!s[i+1].equalsIgnoreCase("PK"))
					sb.append(",");

		}

		return sb.toString();
	}

	/**
	 * End the program with a message.
	 * @param message - The message to display before exit.
	 */
	public static void end(String message)
	{
		System.err.println(message);
		System.exit(1);
	}

	
	/**
	 * This is a HMAC algorithm that has been verified by using the HASHCALC program
	 * @param data String that needs to be Encrypted
	 * @return String of encryption
	 */
	public static byte[] HMAC_SHA1(String data, int n){
		byte[] hexBytes = null;
		try {
			// Get an hmac_sha1 key from the raw key bytes
			byte[] keyBytes = HMACkeys[n].getBytes();			
			SecretKeySpec signingKey = new SecretKeySpec(keyBytes, HMAC_SHA1_ALGORITHM);
			// Get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);
			// Compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(data.getBytes());
			// Convert raw bytes to Hex
			hexBytes = new Hex().encode(rawHmac);
			//Covert array of Hex bytes to a String
			//String result = new String(hexBytes, "ISO-8859-1");
			//System.out.println("MAC : " + result);
		} 
		catch (Exception e) {
			System.err.println(("Failed to generate HMAC : " + e.getMessage()));
		}
		return hexBytes;
	}	


	//TODO: provide description for this method
	public static long multiHMAC(String input, int index, int iter, int incre)
	{
		for(int i = 0; i < iter; i++){
			input = byteArrayToString(HMAC_SHA1(input, index));
			index = index + incre;
		}
		return byteArrayToLong(input.getBytes());
	}

	/**
	 * Print the progress of i reaching n, stepping by some percentage
	 * 
	 * @param i The current location of the iterator
	 * @param n The maximum the iterator can reach
	 * @param percent what percentage to step by
	 * @param c the character to print upon reaching each percent
	 */
	public static void PrintPercentProgress(int i, int n, int percent, char c)
	{


		percent = 100 / percent;
		int x = (int)((double)n / (double)percent);
		if(x == 0) return;

		if (i == 0 )
		{
			System.out.print('|');
			for(int j = 0; j < percent-1; j++)
				System.out.print('=');
			System.out.print("|\n|");
		}


		// Determine progress
		if (i != 0 && i % x == 0 )
		{
			System.out.print(c);
			System.out.flush();
		}
		if (i == n-1)
		{
			System.out.println(" 100% Complete");
		}
	}



	/**
	 * Given some array of columnNames, this function matches columnNameReferences
	 * to columnNames.  
	 * The result is a HashMap of Strings(ColumnNames) to Integers (Indices) of where
	 * each columnName reside.
	 * This allows one to know which values in a row of data belong to which columns. 
	 * @param columnName - A String array of columnNames
	 * @return - A HashMap mapping columnNames to column Indices
	 */
	// TODO: update function description
	public static HashMap<String, Integer> returnMap(String[] columnName)
	{
		HashMap<String, Integer> myMap = new HashMap<String, Integer>();
		int extraCount = 0;

		// Iterate through each column name
		for (int i = 0; i < columnName.length; i++)
		{
			String s = columnName[i];

			// PERSON Headers || filea_5000_encrypted Headers || PERSON_ENCRYPTED || FieldWeights.
			// TODO: it could make more sense to use regex's or to pull these from file.
			// TODO: determine which checks are not being used! yikes!

			// Last Name
			if (s.equalsIgnoreCase("Person_Last") || 
					s.equalsIgnoreCase("LASTNAME") ||
					s.equalsIgnoreCase("PE_LAST") ||
					s.equalsIgnoreCase("LN") ||
					s.equalsIgnoreCase("PAT_LAST_NAME")
			)
				myMap.put(MyUtilities.LASTNAME, i);
			// First Name
			else if (s.equalsIgnoreCase("Person_First") || 
					s.equalsIgnoreCase("FIRSTNAME") || 
					s.equalsIgnoreCase("PE_First") ||
					s.equalsIgnoreCase("FN") ||
					s.equalsIgnoreCase("PAT_FIRST_NAME"))
				myMap.put(MyUtilities.FIRSTNAME, i);
			// Social Security Number
			else if (s.equalsIgnoreCase("Person_SSN") || 
					s.equalsIgnoreCase("SSN") || 
					s.equalsIgnoreCase("PE_SSN"))
				myMap.put(MyUtilities.SSN, i);
			// Date of Birth
			else if ( s.equalsIgnoreCase("Date_of_Birth") || 
					s.equalsIgnoreCase("DOB") ||
					s.equalsIgnoreCase("BDATE") ||
					s.equalsIgnoreCase("BIRTH_DATE"))
				myMap.put(MyUtilities.DOB, i);
			// Gender
			else if ( s.equalsIgnoreCase("SOURCE_GENDER_CODE") || 
					s.equalsIgnoreCase("SEX") || 
					s.equalsIgnoreCase("PE_GENDER_CODE"))
				myMap.put(MyUtilities.GENDER, i);
			// Address Line 1
			else if ( s.equalsIgnoreCase("Person_Address_1") || 
					s.equalsIgnoreCase("ADDR") || 
					s.equalsIgnoreCase("PE_Address_1") ||
					s.equalsIgnoreCase("ADD_LINE_1"))
				myMap.put(MyUtilities.ADDR_1, i);
			// City
			else if (s.equalsIgnoreCase("Person_City") || 
					s.equalsIgnoreCase("CITY") || 
					s.equalsIgnoreCase("PE_CITY"))
				myMap.put(MyUtilities.CITY, i);
			// State
			else if (s.equalsIgnoreCase("STATE"))
				myMap.put(MyUtilities.STATE, i);
			// Zip Code
			else if (s.equalsIgnoreCase("ZIP"))
				myMap.put(MyUtilities.ZIP, i);
			// Phone
			else if (s.equalsIgnoreCase("PHONE"))
				myMap.put(MyUtilities.PHONE, i);
			// DOB HASH
			else if (s.equalsIgnoreCase("DOBHASH"))
				myMap.put(MyUtilities.DOBHASH, i);
			else 
				myMap.put(KEYWORD_EXTRA + extraCount++, i);
		}
		return myMap;
	}


	/**
	 * Give the location of some file, return a BufferedReader of the file
	 * @param file_location - The path to the file
	 * @return - A BufferedReader pointing to the file
	 */
	public static BufferedReader ReadFile (String file_location)
	{
		// Try to set up the reader for the field weights
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file_location));
		} catch (FileNotFoundException e1) {
			System.err.println("Could not find file: " + file_location);
			e1.printStackTrace();
		} 
		return br;
	}

	/**
	 * Given the location of some properties file,
	 * return the contents of the file as a Properties object.
	 * 
	 * @param file_location - The path to the file
	 * @return - A properties object containing all the info from the file
	 */
	public static Properties readProperties(String file_location)
	{
		Properties props = new Properties();
		try 
		{
			// Create reading stream, read properties, and close.
			BufferedReader br = ReadFile(file_location);
			props.load(br);
			br.close();
		} catch (IOException e) {
			System.err.println("Error: Unknown IO Exception.");
			e.printStackTrace();
		}
		return props;
	}

	/**
	 * Returns a SHA1 of the input as a string
	 * @param text - input
	 * @return - returns the SHA1 as a string
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static String SHA1(String text){ 
		MessageDigest md;
		byte[] sha1hash = new byte[40];
		try {
			md = MessageDigest.getInstance("SHA-1");
			md.update(text.getBytes("iso-8859-1"), 0, text.length());
			sha1hash = md.digest();
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Error with SHA1 encryption");
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			System.err.println("Error Encoding the SHA1");
			e.printStackTrace();
		}

		return MyUtilities.convertToHex(sha1hash);
	}

	/**
	 * Return the difference in time between two dates
	 * @param d1  The start date
	 * @param d2  The ending date
	 * @return String: (d2 - d1) + " milliseconds"
	 */
	public static String subtractTime(Date d1, Date d2)
	{
		long time = Math.abs((d2.getTime() - d1.getTime()));
		return time  + " milliseconds";
	}
	/**
	 * Return the difference in time between two dates
	 * @param d1  The start date
	 * @param d2  The ending date
	 * @return double: (d2 - d1) (in seconds);
	 */
	public static double subtractTimeDouble(Date d1, Date d2)
	{
		return Math.abs((d2.getTime() - d1.getTime()) / 1000.0);
	}

	/**
	 * Truncates some double value to only have a certain number of decimals
	 * E.g. (1.23456, 2) returns (1.23)
	 * @param input - The double value that needs decimals removed from
	 * @param numDecimals - The number of decimals desired on the output
	 * @return - The same double value, but with less decimal values.
	 */
	public static double toDecimals(double input, int numDecimals)
	{
		numDecimals = (int)Math.pow(10, numDecimals);
		return (  ( (double) ((int)(numDecimals*input)) ) / numDecimals  );
	}


	/**
	 * Given an ArrayList of Records, write each record, using it's toString() function 
	 * to some output location
	 * @param records - An ArrayList of initialized Records
	 * @param output_location - The path to the file to be written to
	 */
	public static void WriteRecordsToFile (ArrayList<Record> records, String output_location)
	{
		// Try to Set up writing stream
		PrintWriter writerStream = null;
		try {
			writerStream = new PrintWriter(new BufferedWriter(new FileWriter(output_location)));
		} catch (IOException e1) {
			System.out.println("Failed to open writer stream. Check the filename?");
			e1.printStackTrace();
		}

		// Write encrypted data to file
		for (Record r : records)
			writerStream.write(r.toString() + "\n");

		// Close the writing connection
		writerStream.close();
	}


	/**
	 * Given an ArrayList of Records, use the toString function of each
	 * and print the results to STDOUT
	 * @param records - The ArrayList of Records to print
	 */
	public static void WriteRecordsToStdOut(ArrayList<Record> records)
	{
		for (Record r : records)
			System.out.println(r.toString());
	}



	//---------------------------------------------------------------------------------

	/**
	 * The set of keys to use for the HMAC_SHA1 algorithm
	 * @return
	 */
	private static String[] populateKeys() 
	{
		String[] temp = {
				"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"
		};
		return temp;
	}

}

