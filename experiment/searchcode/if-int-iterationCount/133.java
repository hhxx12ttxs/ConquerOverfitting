/**
 * 
 */
package com.yewell.assessment.utils;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * @author Ginger
 * 
 */
public class EncryptDecryptUtils {

	Cipher ecipher;
	Cipher dcipher;

	String key;
	String encPass;

	String algo;

	// 8-byte Salt
	byte[] salt = { (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32, (byte) 0x56, (byte) 0x35,
			(byte) 0xE3, (byte) 0x03 };

	// Iteration count
	int iterationCount = 19;

	public EncryptDecryptUtils(String passPhrase) throws Exception {
		init(passPhrase);
	}

	private void init(String passPhrase) throws Exception {
		// This method is implemented in e194 Listing All Available
		// Cryptographic Services
		// String[] names = getCryptoImpls("Cipher");may be:
		// Blowfish,DESede,PBEWithMD5AndTripleDES,TripleDES,DES,PBEWithMD5AndDES
		try {
			// Create the key
			KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt, iterationCount);
			SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES")
					.generateSecret(keySpec);
			ecipher = Cipher.getInstance(key.getAlgorithm());
			dcipher = Cipher.getInstance(key.getAlgorithm());

			// Prepare the parameter to the ciphers
			AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);

			// Create the ciphers
			ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
			dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
		} catch (java.security.InvalidAlgorithmParameterException e) {
			throw e;
		} catch (java.security.spec.InvalidKeySpecException e) {
			throw e;
		} catch (javax.crypto.NoSuchPaddingException e) {
			throw e;
		} catch (java.security.NoSuchAlgorithmException e) {
			throw e;
		} catch (java.security.InvalidKeyException e) {
			throw e;
		}

	}

	// encrypt str if and only if it's not encrypted before.
	public String encrypt(String str) {
		// if str has been encrypted,do not encrypted again
		// logic: decrypt a plain text will return the same plain text;
		String decrypted = decrypt(str);
		if (!str.equals(decrypted)) {// is not plain text,already encrypted.
			return str;
		}

		try {
			// Encode the string into bytes using utf-8
			byte[] utf8 = str.getBytes("UTF8");

			// Encrypt
			byte[] enc = ecipher.doFinal(utf8);

			// Encode bytes to base64 to get a string
			return new sun.misc.BASE64Encoder().encode(enc);

		} catch (Exception e) {
		}
		return str;
	}

	// decrypt a plain text will do nothing
	public String decrypt(String str) {
		try {
			// Decode base64 to get bytes
			byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str);

			// Decrypt
			byte[] utf8 = dcipher.doFinal(dec);

			// Decode using utf-8
			return new String(utf8, "UTF8");

		} catch (Exception e) {

		}
		return str;
	}

	private static void prnln(String msg) {
		System.out.println(msg);
	}

	private boolean isGoodPassword(String pass) {
		// allows !@#$%-_ also
		if (pass.matches("[\\!\\@\\#\\$\\%a-zA-Z0-9_-]{4,}"))
			return true;
		return false;
	}

	public static void main(String[] args) throws Exception {
		String phaseCode = "g12345678";// use this to encrypt/decrypt your
		// message
		EncryptDecryptUtils encdec = new EncryptDecryptUtils(phaseCode);
		String passwd = "mypasswd";
		String encPasswd = encdec.encrypt(passwd);
		prnln("Encrypted result for plain password '" + passwd + "':" + encPasswd);
		prnln("Decrypted result for encrypted password '" + encPasswd + "':"
				+ encdec.decrypt(encPasswd));
	}

}

