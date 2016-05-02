package com.androidfuzzer.constants;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import android.util.Log;

import com.example.ss.WifiManagerFuzzer;

public class FuzzerConstants {

	public short smax = Short.MAX_VALUE;
	public int imax = Integer.MAX_VALUE;
	public long lmax = Long.MAX_VALUE;
	public float fmax = Float.MAX_VALUE;
	public double dmax = Double.MAX_VALUE;
	public char cmax = Character.MAX_VALUE;
	public byte bmax = Byte.MAX_VALUE;
	public double dinfinity = Double.MAX_VALUE + Double.MAX_VALUE;
	// this is just for completeness
	public boolean boo_max = Boolean.TRUE;
	public String max_string = getARandomString();
	public int max_string_length = max_string.length();

	public short smin = Short.MIN_VALUE;
	public int imin = Integer.MIN_VALUE;
	public long lmin = Long.MIN_VALUE;
	public float fmin = Float.MIN_VALUE;
	public double dmin = Double.MIN_VALUE;
	public char cmin = Character.MIN_VALUE;
	public byte bmin = Byte.MIN_VALUE;
	// this is just for completeness
	public boolean boo_min = Boolean.FALSE;
	public int min_string_length = 0;
	public String min_string = "";

	public HashMap<Class<?>, List<Object>> max_preDefObjects = null;
	public HashMap<Class<?>, List<Object>> min_preDefObjects = null;

	public FuzzerConstants() {
		max_preDefObjects = new HashMap<Class<?>, List<Object>>();
		min_preDefObjects = new HashMap<Class<?>, List<Object>>();
	}

	public void addToMaxPredefObjects(Class<?> cls, List<Object> list) {
		max_preDefObjects.put(cls, list);
	}

	public void addToMinPredefObjects(Class<?> cls, List<Object> list) {
		min_preDefObjects.put(cls, list);
	}

	/**
	 * Used to create an object of the FuzzerConstant class with max and min
	 * objects of specified classes. The objects in this constructors will
	 * override the default objects if there are any classes in common. It is
	 * the responsibility of the user of this class to make sure that the
	 * classes contained in both the max and min hashmaps are identical. Else
	 * the output is unpredictable.
	 * 
	 * @param max_objts
	 *            the hashmap of class and their corresponding objects with max
	 *            values
	 * @param min_objts
	 *            the hashmap of class and their corresponding objects with min
	 *            values
	 */
	public FuzzerConstants(HashMap<Class<?>, List<Object>> max_objts, HashMap<Class<?>, List<Object>> min_objts) {
		max_preDefObjects = max_objts;
		min_preDefObjects = min_objts;
	}

	/**
	 * Used to create an object of the FuzzerConstant class with max and min
	 * objects of specified classes. The objects in this constructors will
	 * override the default objects if there are any classes in common. This
	 * constructor is useful when there needs to be no distinction between max
	 * and min values.
	 * 
	 * @param objts
	 *            the hashmap of class and their corresponding objects with
	 *            values
	 */
	public FuzzerConstants(HashMap<Class<?>, List<Object>> objts) {
		max_preDefObjects = objts;
		min_preDefObjects = objts;
	}

	/**
	 * Gets the value of the field who's class is the class specified as the
	 * argument.
	 * 
	 * @param cls
	 *            the class of the field who's value is required
	 * @param max
	 *            boolean flag representing whether the max or min value should
	 *            be returned
	 * @return the value of the field whose class is specified
	 */
	public Object getValueOfType(Class<?> cls, boolean max) {

		// Check if hasmap of max, min is not null and if not null check if
		// there is an object for the class in the hashmap
		if (max_preDefObjects.size() != 0 && min_preDefObjects.size() != 0) {
			Random rand = new Random();
			List<Object> predef_objs;
			if (max) {
				if (max_preDefObjects.containsKey(cls)) {
					predef_objs = max_preDefObjects.get(cls);
					return predef_objs.get(rand.nextInt(predef_objs.size()));
				}
			}
			else {
				if (min_preDefObjects.containsKey(cls)) {
					predef_objs = min_preDefObjects.get(cls);
					return predef_objs.get(rand.nextInt(predef_objs.size()));
				}
			}
		}

		Object returnObject = null;
		Field[] fields = this.getClass().getFields();
		String maxOrMin = "min";
		if (max) {
			maxOrMin = "max";
		}
		try {
			for (Field f : fields) {
				if (f.getType() == cls && f.getName().contains(maxOrMin)) {
					returnObject = f.get(this);
					break;
				}
			}
			/* Return an instance of the class if no predefined value is found */
			/*if(returnObject == null){
				return cls.newInstance();
			}*/
		} catch(Exception e){
			Log.e(WifiManagerFuzzer.FUZZER_TAG,"Got exception:"+returnObject);
			returnObject = null;
		}	
		return returnObject;
	}

	/**
	 * Create a String whose length is specified by the size arguments.
	 * 
	 * @param size
	 *            the size of the string to be created
	 * @param character
	 *            the character to used to generate the string
	 * @return the generated string
	 */
	public String getLargeString(int size, String character) {
		StringBuilder sb = new StringBuilder(size);
		for (int i = 0; i < size; i++) {
			sb.append(character);
		}
		return sb.toString();
	}

	/**
	 * 
	 * @return returns a string that is randomly generated with random length
	 */
	public String getARandomString() {
		Random rand = new Random();
		String spaceString = getLargeString(rand.nextInt(1024), " ");
		String formatString = getStringWithFormatSpecifiers(rand.nextInt(1024));
		String largeString = max_string;
		String specialString = getStringWithRandomCharacters(rand.nextInt(1024));
		String validIpString = getValidIpString();
		String invalidIpString = getInvalidIpString();

		switch (rand.nextInt(6)) {
		case 0:
			return spaceString;
		case 1:
			return formatString;
		case 2:
			return largeString;
		case 3:
			return specialString;
		case 4:
			return validIpString;
		case 5:
			return invalidIpString;
		}

		return null;
	}

	/**
	 * Creates a string with only format specifiers
	 * 
	 * @param size
	 *            the size of the string to be generated.
	 * @return the constructed string
	 */
	public String getStringWithFormatSpecifiers(int size) {
		String[] formatSpecifiers = { "%s", "%d", "%f", "%c", "%x", "%t" };
		StringBuilder sb = new StringBuilder(size);
		Random rand = new Random(System.currentTimeMillis());
		for (int i = 0; i < size; i++) {
			sb.append(formatSpecifiers[rand.nextInt(formatSpecifiers.length)]);
		}

		return sb.toString();
	}

	/**
	 * Creates a string with random characters
	 * 
	 * @param size
	 *            the size of the string to be constructed
	 * @return the string of the specified size containing random characters
	 */
	public String getStringWithRandomCharacters(int size) {
		Random rand = new Random(System.currentTimeMillis());
		StringBuilder sb = new StringBuilder(size);
		for (int i = 0; i < size; i++) {
			sb.append((char) rand.nextInt(256));
		}

		return sb.toString();

	}
	
	/**
	 * Function to create a valid ipv4 address
	 * @return
	 */
	public String getValidIpString(){
		Random rand = new Random(System.currentTimeMillis());
		String[] ipFields = new String[4];
		ipFields[0] = Integer.toString(rand.nextInt(256));
		ipFields[1] = Integer.toString(rand.nextInt(256));
		ipFields[2] = Integer.toString(rand.nextInt(256));
		ipFields[3] = Integer.toString(rand.nextInt(256));
		
		return ipFields[0] + "." + ipFields[1] + "." + ipFields[2] + "." + ipFields[3];
	}
	
	/**
	 * Function to create a valid ipv4 address
	 * @return
	 */
	public String getInvalidIpString(){
		Random rand = new Random(System.currentTimeMillis());
		int invalidIpField = 256 + rand.nextInt(10000);
		String[] ipFields = new String[4];
		ipFields[0] = Integer.toString(rand.nextInt(256));
		ipFields[1] = Integer.toString(rand.nextInt(256));
		ipFields[2] = Integer.toString(rand.nextInt(256));
		ipFields[3] = Integer.toString(rand.nextInt(256));
		
		// set one of the four fields to an invalid ip number
		ipFields[rand.nextInt(4)] = Integer.toString(invalidIpField);
		
		return ipFields[0] + "." + ipFields[1] + "." + ipFields[2] + "." + ipFields[3];
	}
}

