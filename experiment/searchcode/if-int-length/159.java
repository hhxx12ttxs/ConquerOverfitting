package librfid.utils;

public class ByteUtils {

	public static boolean sameContents(byte[] a, byte[] b) {
		if (a.length != b.length)
			return false;
		for (int i = 0 ; i < a.length ; i++) {
			if (a[i] != b[i])
				return false;
		}
		return true;
	}
	
	public static byte[] reverseEvery(byte[] a, int i) {
		byte[] result = new byte[a.length];
		int new_index;
		for (int j = 0 ; j < a.length ; j++) {
			// def tst(i) ; (i/3)*3 + (2 - (i%3)) ; end
			new_index = ((j/i) * i) + (i - 1 - (j % i));
			result[new_index] = a[j];
		}
		return result;
	}
	
	public static byte[] skipEvery(byte[] a, int i) {
		byte[] result = new byte[a.length];
		int new_index;
		for (int j = 0 ; j < a.length ; j++) {
			new_index = ((j/i) * i) + (i - 1 - (j % i));
			result[new_index] = a[j];
		}
		return result;
	}
	
	public static byte[] cons(byte b, byte[] a) {
		byte[] result = new byte[a.length + 1];
		result[0] = b;
		System.arraycopy(a, 0, result, 1, a.length);
		return result;
	}
	
	public static byte[] consTo(byte[] a, byte ... bytes) {
		byte[] result = new byte[a.length + bytes.length];
		for (int i = 0 ; i < bytes.length ; i++) {
			result[i] = bytes[i];
		}
		System.arraycopy(a, 0, result, bytes.length, a.length);
		return result;
	}
	
	public static byte[] appendTo(byte[] a, byte ... bytes) {
		byte[] result = new byte[a.length + bytes.length];
		System.arraycopy(a, 0, result, 0, a.length);
		int offset = a.length;
		for (int i = 0 ; i < bytes.length ; i++) {
			result[offset] = bytes[i];
			offset++;
		}
		return result;
	}
	
	public static byte[] cons(int b, byte[] a) {
		return cons((byte)b, a);
	}
	
	public static byte[] range(int from, int to, byte[] a) {
		if (to < 0 || to > a.length)
			to = a.length - 1;
		if (to < from)
			return new byte[0];
		int new_size = to - from + 1;
		byte[] result = new byte[new_size];
		System.arraycopy(a, from, result, 0, new_size);
		return result;
	}
	
	public static byte[] concat(byte[] a, byte[] b) {
		byte[] result = new byte[a.length + b.length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}
	
	public static byte[] concat(byte[] ... byte_arrays) {
		int size = 0;
		for (int i = 0 ; i < byte_arrays.length ; i++){
			size += byte_arrays.length;
		}
		byte[] result = new byte[size];
		int offset = 0;
		for (int i = 0 ; i < byte_arrays.length ; i++){
			System.arraycopy(byte_arrays[i], 0, result, offset, byte_arrays[i].length);
			offset += byte_arrays[i].length;
		}
		return result;
	}

	public static int unsignedByteToInt(byte b) {
		return (int) b & 0xFF;
	}

	public static String byteToHex(byte b){
		int i = b & 0xFF;
		return Integer.toHexString(i).toUpperCase();
	}

	public static String byteArrayToHex(byte[] a) {
		String result = "[";
		for (int i = 0 ; i < a.length-1 ; i++) {
			result += byteToHex(a[i]) + ", ";
		}
		if(a.length > 0)
			result += byteToHex(a[a.length-1]);
		result += "]";
		return result;
	}
	
	public static int mergeBytes(byte ... bytes) {
		int result = 0;
		int b;
		for (int i = 0 ; i < bytes.length ; i++) {
			b = unsignedByteToInt(bytes[i]);
			result += (b << ((bytes.length - i -1) * 8));
		}
		return result;
	}

}
