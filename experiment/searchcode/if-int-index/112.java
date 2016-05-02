package cc.creativecomputing.util;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * This class contains various methods for manipulating arrays (such as
 * sorting and searching).  This class also contains a static factory
 * that allows arrays to be viewed as lists.
 * @author info
 *
 */
public class CCArrayUtil{

	// ////////////////////////////////////////////////////////////

	// ARRAY UTILITIES

	/**
	 * Calls System.arraycopy(), included here so that we can avoid people
	 * needing to learn about the System object before they can just copy an
	 * array.
	 */
	static public void arraycopy(Object src, int srcPosition, Object dst, int dstPosition, int length){
		System.arraycopy(src, srcPosition, dst, dstPosition, length);
	}

	/**
	 * Convenience method for arraycopy(). Identical to <CODE>arraycopy(src, 0,
	 * dst, 0, length);</CODE>
	 */
	static public void arraycopy(Object src, Object dst, int length){
		System.arraycopy(src, 0, dst, 0, length);
	}

	/**
	 * Shortcut to copy the entire contents of the source into the destination
	 * array. Identical to <CODE>arraycopy(src, 0, dst, 0, src.length);</CODE>
	 */
	static public void arraycopy(Object src, Object dst){
		System.arraycopy(src, 0, dst, 0, Array.getLength(src));
	}

	/**
	 * @shortdesc Increases the size of an array.
	 * By default, this function doubles the size of the array, but the optional 
	 * newSize parameter provides precise control over the increase in size.
	 * When using an array of objects, the data returned from the function must 
	 * be cast to the object array's data type. 
	 * For example: SomeClass[] items = (SomeClass[]) expand(originalArray).
	 * @param theBooleanArray boolean array
	 * @return the expanded array
	 */
	static public boolean[] expand(final boolean theBooleanArray[]){
		return expand(theBooleanArray, theBooleanArray.length << 1);
	}

	/**
	 * 
	 * @param theNewSize new size for the array
	 */
	static public boolean[] expand(final boolean[] theBooleanArray, int theNewSize){
		boolean temp[] = new boolean[theNewSize];
		System.arraycopy(theBooleanArray, 0, temp, 0, Math.min(theNewSize, theBooleanArray.length));
		return temp;
	}

	/**
	 * 
	 * @param theByteArray byte array
	 */
	static public byte[] expand(final byte theByteArray[]){
		return expand(theByteArray, theByteArray.length << 1);
	}

	static public byte[] expand(final byte theByteArray[], final int theNewSize){
		byte temp[] = new byte[theNewSize];
		System.arraycopy(theByteArray, 0, temp, 0, Math.min(theNewSize, theByteArray.length));
		return temp;
	}

	/**
	 * @param theCharArray array of chars
	 */
	static public char[] expand(final char theCharArray[]){
		return expand(theCharArray, theCharArray.length << 1);
	}
	
	static public char[] expand(final char theCharArray[], final int theNewSize){
		char temp[] = new char[theNewSize];
		System.arraycopy(theCharArray, 0, temp, 0, Math.min(theNewSize, theCharArray.length));
		return temp;
	}

	static public int[] expand(int list[]){
		return expand(list, list.length << 1);
	}

	static public int[] expand(int list[], int newSize){
		int temp[] = new int[newSize];
		System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
		return temp;
	}

	static public float[] expand(float list[]){
		return expand(list, list.length << 1);
	}

	static public float[] expand(float list[], int newSize){
		float temp[] = new float[newSize];
		System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
		return temp;
	}

	static public double[] expand(double list[]){
		return expand(list, list.length << 1);
	}

	static public double[] expand(double list[], int newSize){
		double temp[] = new double[newSize];
		System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
		return temp;
	}

	static public String[] expand(String list[]){
		return expand(list, list.length << 1);
	}

	static public String[] expand(String list[], int newSize){
		String temp[] = new String[newSize];
		// in case the new size is smaller than list.length
		System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
		return temp;
	}

	static public <ObjectType> ObjectType[]  expand(ObjectType[] array){
		return expand(array, Array.getLength(array) << 1);
	}

	static public <ObjectType> ObjectType[] expand(ObjectType[] list, int newSize){
		return copyOf(list, newSize);
	}
	
	/**
     * Copies the specified array, truncating or padding with nulls (if necessary)
     * so the copy has the specified length.  For all indices that are
     * valid in both the original array and the copy, the two arrays will
     * contain identical values.  For any indices that are valid in the
     * copy but not the original, the copy will contain <tt>null</tt>.
     * Such indices will exist if and only if the specified length
     * is greater than that of the original array.
     * The resulting array is of exactly the same class as the original array.
     *
     * @param original the array to be copied
     * @param newLength the length of the copy to be returned
     * @return a copy of the original array, truncated or padded with nulls
     *     to obtain the specified length
     * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static <T> T[] copyOf(T[] original, int newLength) {
        return (T[]) copyOf(original, newLength, original.getClass());
    }
    
    /**
     * Copies the specified array, truncating or padding with nulls (if necessary)
     * so the copy has the specified length.  For all indices that are
     * valid in both the original array and the copy, the two arrays will
     * contain identical values.  For any indices that are valid in the
     * copy but not the original, the copy will contain <tt>null</tt>.
     * Such indices will exist if and only if the specified length
     * is greater than that of the original array.
     * The resulting array is of the class <tt>newType</tt>.
     *
     * @param original the array to be copied
     * @param newLength the length of the copy to be returned
     * @param newType the class of the copy to be returned
     * @return a copy of the original array, truncated or padded with nulls
     *     to obtain the specified length
     * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
     * @throws NullPointerException if <tt>original</tt> is null
     * @throws ArrayStoreException if an element copied from
     *     <tt>original</tt> is not of a runtime type that can be stored in
     *     an array of class <tt>newType</tt>
     * @since 1.6
     */
    public static <T,U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
        T[] copy = ((Object)newType == (Object)Object[].class) ? (T[]) new Object[newLength] : (T[]) Array.newInstance(newType.getComponentType(), newLength);
        System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
        return copy;
    }


	//

	static public boolean[] contract(boolean list[], int newSize){
		return expand(list, newSize);
	}

	static public byte[] contract(byte list[], int newSize){
		return expand(list, newSize);
	}

	static public char[] contract(char list[], int newSize){
		return expand(list, newSize);
	}

	static public int[] contract(int list[], int newSize){
		return expand(list, newSize);
	}

	static public float[] contract(float list[], int newSize){
		return expand(list, newSize);
	}

	static public String[] contract(String list[], int newSize){
		return expand(list, newSize);
	}

	static public <ObjectType> ObjectType[] contract(ObjectType[] list, int newSize){
		return expand(list, newSize);
	}

	//

	static public byte[] append(byte b[], byte value){
		b = expand(b, b.length + 1);
		b[b.length - 1] = value;
		return b;
	}

	static public char[] append(char b[], char value){
		b = expand(b, b.length + 1);
		b[b.length - 1] = value;
		return b;
	}

	static public int[] append(int b[], int value){
		b = expand(b, b.length + 1);
		b[b.length - 1] = value;
		return b;
	}

	static public float[] append(float b[], float value){
		b = expand(b, b.length + 1);
		b[b.length - 1] = value;
		return b;
	}

	static public String[] append(String b[], String value){
		b = expand(b, b.length + 1);
		b[b.length - 1] = value;
		return b;
	}

	static public <ObjectType> ObjectType[] append(ObjectType[] b, Object value){
		int length = Array.getLength(b);
		b = expand(b, length + 1);
		Array.set(b, length, value);
		return b;
	}

	//

	static public boolean[] shorten(boolean list[]){
		return contract(list, list.length - 1);
	}

	static public byte[] shorten(byte list[]){
		return contract(list, list.length - 1);
	}

	static public char[] shorten(char list[]){
		return contract(list, list.length - 1);
	}

	static public int[] shorten(int list[]){
		return contract(list, list.length - 1);
	}

	static public float[] shorten(float list[]){
		return contract(list, list.length - 1);
	}

	static public String[] shorten(String list[]){
		return contract(list, list.length - 1);
	}

	static public <ObjectType> ObjectType[] shorten(ObjectType[] list){
		int length = Array.getLength(list);
		return contract(list, length - 1);
	}

	//

	static final public boolean[] splice(boolean list[], boolean v, int index){
		boolean outgoing[] = new boolean[list.length + 1];
		System.arraycopy(list, 0, outgoing, 0, index);
		outgoing[index] = v;
		System.arraycopy(list, index, outgoing, index + 1, list.length - index);
		return outgoing;
	}

	static final public boolean[] splice(boolean list[], boolean v[], int index){
		boolean outgoing[] = new boolean[list.length + v.length];
		System.arraycopy(list, 0, outgoing, 0, index);
		System.arraycopy(v, 0, outgoing, index, v.length);
		System.arraycopy(list, index, outgoing, index + v.length, list.length - index);
		return outgoing;
	}

	static final public byte[] splice(byte list[], byte v, int index){
		byte outgoing[] = new byte[list.length + 1];
		System.arraycopy(list, 0, outgoing, 0, index);
		outgoing[index] = v;
		System.arraycopy(list, index, outgoing, index + 1, list.length - index);
		return outgoing;
	}

	static final public byte[] splice(byte list[], byte v[], int index){
		byte outgoing[] = new byte[list.length + v.length];
		System.arraycopy(list, 0, outgoing, 0, index);
		System.arraycopy(v, 0, outgoing, index, v.length);
		System.arraycopy(list, index, outgoing, index + v.length, list.length - index);
		return outgoing;
	}

	static final public char[] splice(char list[], char v, int index){
		char outgoing[] = new char[list.length + 1];
		System.arraycopy(list, 0, outgoing, 0, index);
		outgoing[index] = v;
		System.arraycopy(list, index, outgoing, index + 1, list.length - index);
		return outgoing;
	}

	static final public char[] splice(char list[], char v[], int index){
		char outgoing[] = new char[list.length + v.length];
		System.arraycopy(list, 0, outgoing, 0, index);
		System.arraycopy(v, 0, outgoing, index, v.length);
		System.arraycopy(list, index, outgoing, index + v.length, list.length - index);
		return outgoing;
	}

	static final public int[] splice(int list[], int v, int index){
		int outgoing[] = new int[list.length + 1];
		System.arraycopy(list, 0, outgoing, 0, index);
		outgoing[index] = v;
		System.arraycopy(list, index, outgoing, index + 1, list.length - index);
		return outgoing;
	}

	static final public int[] splice(int list[], int v[], int index){
		int outgoing[] = new int[list.length + v.length];
		System.arraycopy(list, 0, outgoing, 0, index);
		System.arraycopy(v, 0, outgoing, index, v.length);
		System.arraycopy(list, index, outgoing, index + v.length, list.length - index);
		return outgoing;
	}

	static final public float[] splice(float list[], float v, int index){
		float outgoing[] = new float[list.length + 1];
		System.arraycopy(list, 0, outgoing, 0, index);
		outgoing[index] = v;
		System.arraycopy(list, index, outgoing, index + 1, list.length - index);
		return outgoing;
	}

	static final public float[] splice(float list[], float v[], int index){
		float outgoing[] = new float[list.length + v.length];
		System.arraycopy(list, 0, outgoing, 0, index);
		System.arraycopy(v, 0, outgoing, index, v.length);
		System.arraycopy(list, index, outgoing, index + v.length, list.length - index);
		return outgoing;
	}

	static final public String[] splice(String list[], String v, int index){
		String outgoing[] = new String[list.length + 1];
		System.arraycopy(list, 0, outgoing, 0, index);
		outgoing[index] = v;
		System.arraycopy(list, index, outgoing, index + 1, list.length - index);
		return outgoing;
	}

	static final public String[] splice(String list[], String v[], int index){
		String outgoing[] = new String[list.length + v.length];
		System.arraycopy(list, 0, outgoing, 0, index);
		System.arraycopy(v, 0, outgoing, index, v.length);
		System.arraycopy(list, index, outgoing, index + v.length, list.length - index);
		return outgoing;
	}

	static final public Object splice(Object list, Object v, int index){
		Object[] outgoing = null;
		int length = Array.getLength(list);

		// check whether is an array or not, and if so, treat as such
		if (list.getClass().getName().charAt(0) == '['){
			int vlength = Array.getLength(v);
			outgoing = new Object[length + vlength];
			System.arraycopy(list, 0, outgoing, 0, index);
			System.arraycopy(v, 0, outgoing, index, vlength);
			System.arraycopy(list, index, outgoing, index + vlength, length - index);

		}else{
			outgoing = new Object[length + 1];
			System.arraycopy(list, 0, outgoing, 0, index);
			Array.set(outgoing, index, v);
			System.arraycopy(list, index, outgoing, index + 1, length - index);
		}
		return outgoing;
	}

	//

	static public boolean[] subset(boolean list[], int start){
		return subset(list, start, list.length - start);
	}

	static public boolean[] subset(boolean list[], int start, int count){
		boolean output[] = new boolean[count];
		System.arraycopy(list, start, output, 0, count);
		return output;
	}

	static public byte[] subset(byte list[], int start){
		return subset(list, start, list.length - start);
	}

	static public byte[] subset(byte list[], int start, int count){
		byte output[] = new byte[count];
		System.arraycopy(list, start, output, 0, count);
		return output;
	}

	static public char[] subset(char list[], int start){
		return subset(list, start, list.length - start);
	}

	static public char[] subset(char list[], int start, int count){
		char output[] = new char[count];
		System.arraycopy(list, start, output, 0, count);
		return output;
	}

	static public int[] subset(int list[], int start){
		return subset(list, start, list.length - start);
	}

	static public int[] subset(int list[], int start, int count){
		int output[] = new int[count];
		System.arraycopy(list, start, output, 0, count);
		return output;
	}

	static public float[] subset(float list[], int start){
		return subset(list, start, list.length - start);
	}

	static public float[] subset(float list[], int start, int count){
		float output[] = new float[count];
		System.arraycopy(list, start, output, 0, count);
		return output;
	}

	static public String[] subset(String list[], int start){
		return subset(list, start, list.length - start);
	}

	static public String[] subset(String list[], int start, int count){
		String output[] = new String[count];
		System.arraycopy(list, start, output, 0, count);
		return output;
	}

	static public Object subset(Object list, int start){
		int length = Array.getLength(list);
		int count = length - start;
		Class<?> type = list.getClass().getComponentType();
		Object outgoing = Array.newInstance(type, count);
		System.arraycopy(list, 0, outgoing, 0, count);
		return outgoing;
	}

	static public Object subset(Object list, int start, int count){
		// int length = Array.getLength(list);
		Class<?> type = list.getClass().getComponentType();
		Object outgoing = Array.newInstance(type, count);
		System.arraycopy(list, start, outgoing, 0, count);
		return outgoing;
	}

	//

	static public boolean[] concat(boolean a[], boolean b[]){
		boolean c[] = new boolean[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	static public byte[] concat(byte a[], byte b[]){
		byte c[] = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	static public char[] concat(char a[], char b[]){
		char c[] = new char[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	static public int[] concat(int a[], int b[]){
		int c[] = new int[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	static public float[] concat(float a[], float b[]){
		float c[] = new float[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	static public String[] concat(String a[], String b[]){
		String c[] = new String[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	static public Object concat(Object a, Object b){
		Class<?> type = a.getClass().getComponentType();
		int alength = Array.getLength(a);
		int blength = Array.getLength(b);
		Object outgoing = Array.newInstance(type, alength + blength);
		System.arraycopy(a, 0, outgoing, 0, alength);
		System.arraycopy(b, 0, outgoing, alength, blength);
		return outgoing;
	}

	//

	static public boolean[] reverse(boolean list[]){
		boolean outgoing[] = new boolean[list.length];
		int length1 = list.length - 1;
		for (int i = 0; i < list.length; i++){
			outgoing[i] = list[length1 - i];
		}
		return outgoing;
	}

	static public byte[] reverse(byte list[]){
		byte outgoing[] = new byte[list.length];
		int length1 = list.length - 1;
		for (int i = 0; i < list.length; i++){
			outgoing[i] = list[length1 - i];
		}
		return outgoing;
	}

	static public char[] reverse(char list[]){
		char outgoing[] = new char[list.length];
		int length1 = list.length - 1;
		for (int i = 0; i < list.length; i++){
			outgoing[i] = list[length1 - i];
		}
		return outgoing;
	}

	static public int[] reverse(int list[]){
		int outgoing[] = new int[list.length];
		int length1 = list.length - 1;
		for (int i = 0; i < list.length; i++){
			outgoing[i] = list[length1 - i];
		}
		return outgoing;
	}

	static public float[] reverse(float list[]){
		float outgoing[] = new float[list.length];
		int length1 = list.length - 1;
		for (int i = 0; i < list.length; i++){
			outgoing[i] = list[length1 - i];
		}
		return outgoing;
	}

	static public String[] reverse(String list[]){
		String outgoing[] = new String[list.length];
		int length1 = list.length - 1;
		for (int i = 0; i < list.length; i++){
			outgoing[i] = list[length1 - i];
		}
		return outgoing;
	}

	static public Object reverse(Object list){
		Class<?> type = list.getClass().getComponentType();
		int length = Array.getLength(list);
		Object outgoing = Array.newInstance(type, length);
		for (int i = 0; i < length; i++){
			Array.set(outgoing, i, Array.get(list, (length - 1) - i));
		}
		return outgoing;
	}
	
//////////////////////////////////////////////////////////////

	// SORT

	public byte[] sort(final byte[] theByteArray){
		Arrays.sort(theByteArray);
		return theByteArray;
	}
	
	public byte[] sort(final byte[] theByteArray, final int theCount){
		return sort(theByteArray,0,theCount);
	}
	
	public byte[] sort(final byte[] theByteArray, final int theStart, final int theCount){
		if (theCount == 0)return null;
		final byte[] myResult = new byte[theCount];
		System.arraycopy(theByteArray, theStart, myResult, 0, theCount);
		Arrays.sort(myResult);
		return myResult;
	}

	public char[] sort(final char[] theCharArray){
		Arrays.sort(theCharArray);
		return theCharArray;
	}
	
	public char[] sort(final char[] theCharArray, final int theCount){
		return sort(theCharArray,0,theCount);
	}
	
	public char[] sort(final char[] theCharArray, final int theStart, final int theCount){
		if (theCount == 0)return null;
		final char[] myResult = new char[theCount];
		System.arraycopy(theCharArray, theStart, myResult, 0, theCount);
		Arrays.sort(myResult);
		return myResult;
	}

	public int[] sort(final int[] theIntArray){
		Arrays.sort(theIntArray);
		return theIntArray;
	}
	
	public int[] sort(final int[] theIntArray, final int theCount){
		return sort(theIntArray,0,theCount);
	}
	
	public int[] sort(final int[] theIntArray, final int theStart, final int theCount){
		if (theCount == 0)return null;
		final int[] myResult = new int[theCount];
		System.arraycopy(theIntArray, theStart, myResult, 0, theCount);
		Arrays.sort(myResult);
		return myResult;
	}

	public float[] sort(final float[] theFloatArray){
		Arrays.sort(theFloatArray);
		return theFloatArray;
	}
	
	public float[] sort(final float[] theFloatArray, final int theCount){
		return sort(theFloatArray,0,theCount);
	}
	
	public float[] sort(final float[] theFloatArray, final int theStart, final int theCount){
		if (theCount == 0)return null;
		final float[] myResult = new float[theCount];
		System.arraycopy(theFloatArray, theStart, myResult, 0, theCount);
		Arrays.sort(myResult);
		return myResult;
	}

	public String[] sort(final String[] theStringArray){
		Arrays.sort(theStringArray);
		return theStringArray;
	}
	
	public String[] sort(final String[] theStringArray, final int theCount){
		return sort(theStringArray,0,theCount);
	}
	
	public String[] sort(final String[] theStringArray, final int theStart, final int theCount){
		if (theCount == 0)return null;
		final String[] myResult = new String[theCount];
		System.arraycopy(theStringArray, theStart, myResult, 0, theCount);
		Arrays.sort(myResult);
		return myResult;
	}
}

