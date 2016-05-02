package com.orientor.website.tools;



import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class MD5EncodeTool {
	private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5",
		"6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

public static String encodeBase64ForString(String s) {
	BASE64Encoder base64encoder = new BASE64Encoder();
	return base64encoder.encode(s.getBytes());
}

public static String decodeBase64ForString(String s) throws Exception {
	byte abyte0[];
	BASE64Decoder base64decoder = new BASE64Decoder();
	abyte0 = base64decoder.decodeBuffer(s);
	return new String(abyte0);
}


public static String byteArrayToHexString(byte[] b) {
	StringBuffer resultSb = new StringBuffer();
	for (int i = 0; i < b.length; i++) {
		resultSb.append(byteToHexString(b[i]));
	}
	return resultSb.toString();
}

private static String byteToHexString(byte b) {
	int n = b;
	if (n < 0)
		n = 256 + n;
	int d1 = n / 16;
	int d2 = n % 16;
	return hexDigits[d1] + hexDigits[d2];
}

public static String encode(String origin) {
	String resultString = null;
	try {
		resultString = new String(origin);
		java.security.MessageDigest md = java.security.MessageDigest
				.getInstance("MD5");
		resultString = byteArrayToHexString(md.digest(resultString
				.getBytes()));
	} catch (Exception ex) {
		ex.printStackTrace();
	}
	return resultString;
}
	  
	  
	  
public static void main(String[] s){
	System.out.print(MD5EncodeTool.encode("123456"));
}
	
}

