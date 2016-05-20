package com.ecinv.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptographyUtil {

	private static int iterationCount = 10000;
	private static int saltLength = 16; // bytes; 128 bits
	private static int keyLength = 128;
	private static Cipher cipher = null;
	
	static{
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException e) {
			// need to do something here but wt??
		} catch (NoSuchPaddingException e) {
			// need to do something here but wt??
		}
	}


	public static void main(String[] args) throws Exception {
		CryptographyUtil mc = new CryptographyUtil();
		String encryptedData = mc.encrypt("1234");
		if(mc.checkForAuthentication(encryptedData, "1234")){	
		}
	}

	public CryptographyUtil() throws NoSuchAlgorithmException,
			NoSuchPaddingException {
		cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	}

	public static String encrypt(String text) throws Exception {
		if (text == null || text.length() == 0)
			throw new Exception("Empty string");

		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[saltLength];
		random.nextBytes(salt);

		byte[] iv = new byte[cipher.getBlockSize()];
		random.nextBytes(iv);

		String encryptedStr = justEncrypt(text, salt, iv);

		StringBuffer strBuf = new StringBuffer();
		strBuf.append(encryptedStr);
		strBuf.append("]");
		String saltStr = Base64.encodeBytes(salt);
		strBuf.append(saltStr);
		strBuf.append("]");
		String ivStr = Base64.encodeBytes(iv);
		strBuf.append(ivStr);

		return new String(Base64.encodeBytes(strBuf.toString().getBytes()));
	}
	
	public byte[] decrypt(String code, String pwd) throws Exception {
		if (code == null || code.length() == 0)
		throw new Exception("Empty string");

		String[] fields = new String(Base64.decode(code)).split("]");
		byte[] cipherBytes = Base64.decode(fields[0]);
		byte[] salt =  Base64.decode(fields[1]);
		byte[] iv =  Base64.decode(fields[2]);

		KeySpec keySpec = new PBEKeySpec(pwd.toCharArray(), salt,
		iterationCount, keyLength);
		SecretKeyFactory keyFactory = SecretKeyFactory
		.getInstance("PBKDF2WithHmacSHA1");
		byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
		SecretKey key = new SecretKeySpec(keyBytes, "AES");

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		IvParameterSpec ivParams = new IvParameterSpec(iv);

		byte[] decrypted = null;
		cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
		decrypted = cipher.doFinal(cipherBytes);

		return decrypted;
		}

	public static boolean checkForAuthentication(String code, String pwd) throws Exception {

		if (code == null || code.length() == 0)
			throw new Exception("Empty string");

		String[] fields = new String(Base64.decode(code)).split("]");
		byte[] salt = Base64.decode(fields[1]);
		byte[] iv = Base64.decode(fields[2]);
		
		String currentData = justEncrypt(pwd, salt, iv);
		
		if(fields[0].equals(currentData)){
			return true;
		}else{
			return false;
		}

	}

	private static String justEncrypt(String pwd, byte[] salt, byte[] iv)
			throws Exception {

		KeySpec keySpec = new PBEKeySpec(pwd.toCharArray(), salt,
				iterationCount, keyLength);
		SecretKeyFactory keyFactory = SecretKeyFactory
				.getInstance("PBKDF2WithHmacSHA1");
		byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
		SecretKey key = new SecretKeySpec(keyBytes, "AES");

		IvParameterSpec ivParams = new IvParameterSpec(iv);

		byte[] encrypted = null;
		cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
		encrypted = cipher.doFinal(pwd.getBytes("UTF-8"));

		return Base64.encodeBytes(encrypted);
	}
}
