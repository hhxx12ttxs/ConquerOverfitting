/**
 * A library to interact with Virtual Worlds such as OpenSim
 * Copyright (C) 2012  Jitendra Chauhan, Email: jitendra.chauhan@gmail.com
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.ngt.jopenmetaverse.shared.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import org.junit.Assert;
import org.junit.Test;

public class UtilsTest {

	@Test
	public void MD5PasswordTest() {
		try 
		{
			Assert.assertTrue(Utils.MD5("Hello World!").equals("ed076287532e86365e841e92bfc50d8c"));
			Assert.assertTrue(Utils.MD5("").equals("d41d8cd98f00b204e9800998ecf8427e"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void uintToBytesTests()
	{
		long a1 = 763435512;
		byte[] a1bytes = Utils.uintToBytes(a1);
		Assert.assertEquals(new BigInteger(a1bytes).longValue(), a1);
		System.out.println(new BigInteger(a1bytes).longValue() + " HEX: " 
		+ Utils.bytesToHexDebugString(a1bytes, "") + " " + Utils.bytesToHexDebugString(new BigInteger("763435512").toByteArray(), ""));
		long a2 = 4162421037L;
		byte[] a2bytes = Utils.uintToBytes(a2);
		System.out.println(" HEX: " + Utils.bytesToHexDebugString(a2bytes, ""));
		System.out.println(" HEX: " + Utils.bytesToHexDebugString(Utils.uintToBytes(Utils.bytesToUInt(a2bytes)), ""));
		Assert.assertEquals(a2, Utils.bytesToUInt(a2bytes));
	}
	
	
	@Test 
	public void ubyteToIntTests()
	{
		byte a1 = -5;
		int i1 = Utils.ubyteToInt(a1);
		System.out.println(Utils.bytesToHexDebugString(new byte[]{a1}, ""));
		Assert.assertEquals(251, i1);
	}
	
	@Test
	public void longToUintTests()
	{
		long[] uints1 = new long[2];
		Utils.longToUInts(1099511628032000L, uints1);
		Assert.assertEquals(1099511628032000L >> 32, uints1[0]);
		Assert.assertEquals(1099511628032000L & 0x00000000ffffffffL, uints1[1]);
//		System.out.println(String.format("%d --> %d --> %s --> %s", 1099511628032000L >> 32, 
//				1099511628032000L & 0x00000000ffffffffL, 
//				Utils.bytesToHexDebugString(Utils.int64ToBytes(uints1[0]), "")
//				,Utils.bytesToHexDebugString(Utils.int64ToBytes(uints1[1]), "")));
	}
	
	@Test
	public void stringtoBytesTests()
	{
		System.out.println(String.format("22.660995 get converted to \n%s", 
				Utils.bytesToHexDebugString(Utils.stringToBytes(Double.toString(22.660995)), "")));
		System.out.println(String.format("20.1538619995117 gets converted to\n%s", 
				Utils.bytesToHexDebugString(Utils.stringToBytes("20.1538619995117"), "")));
	}
	
	@Test
	public void stringToBytesWithTrailingNullByteTests()
	{
		System.out.println(String.format("22.660995 get converted to \n%s", 
				Utils.bytesToHexDebugString(Utils.stringToBytesWithTrailingNullByte(Double.toString(22.660995)), "")));
		System.out.println(String.format("20.1538619995117 gets converted to\n%s", 
				Utils.bytesToHexDebugString(Utils.stringToBytesWithTrailingNullByte("20.1538619995117"), "")));
		
		Assert.assertEquals(Utils.bytesToHexDebugString(Utils.stringToBytesWithTrailingNullByte(Double.toString(22.660995)), ""), 
				"32 32 2E 36 36 30 39 39 35 00");
	}
	
	
	@Test
	public void bytesToStringWithTrailingNullByteTests()
	{
		try {
			byte[] bytes1 = new byte[]{0x32, 0x32, 0x2E, 0x36, 0x36, 0x30, 0x39, 0x39, 0x35, 0x00};
			String string1 = "22.660995";
			Assert.assertEquals(Utils.bytesWithTrailingNullByteToString(bytes1), string1);			

			byte[] bytes2 = new byte[]{0x32, 0x32, 0x2E, 0x36, 0x36, 0x30, 0x39, 0x39, 0x35};
			String string2 = "22.660995";
			Assert.assertEquals(Utils.bytesWithTrailingNullByteToString(bytes2), string2);
			
		} catch (UnsupportedEncodingException e) {
			Assert.fail("Failed with exception" + Utils.getExceptionStackTraceAsString(e));
			e.printStackTrace();
		} 
	}	
	
	
	@Test
	public void uintToHexStringTests()
	{
		long uint1 = 0x78fe8945ee6ea2b9L;
		String str1 = Utils.uintToHexString(uint1);
		Assert.assertEquals("ee6ea2b9", str1);
	}
	
	@Test
	public void hexStringToBytes()
	{
		try{
			String hex1 = "7fffffff";
			byte[] bytes1 = Utils.hexStringToBytes(hex1, false);
			System.out.println(Utils.bytesToHexDebugString(bytes1, ""));
			bytes1 = Utils.hexStringToBytes(hex1, false);
			System.out.println(Utils.bytesToHexDebugString(bytes1, ""));			
		}
		catch(Exception e)
		{ Assert.fail(Utils.getExceptionStackTraceAsString(e));}
	}
	
	@Test
	public void byteToFloatTests()
	{
		byte b1 = (byte)0xfe;
		float f1 = Utils.byteToFloat(b1, 0.0f, 1.0f);
		Assert.assertEquals(0xfe/(float)255, f1, .01);
		
		b1 = 0;
		f1 = Utils.byteToFloat(b1, 0.0f, 1.0f);
		Assert.assertEquals(0/(float)255, f1, 0.0);

		b1 = (byte)255;
		f1 = Utils.byteToFloat(b1, 0.0f, 1.0f);
		Assert.assertEquals(1.0, f1, 0.0);
	}
	
	@Test
	public void floatToByteTests()
	{
		float val1 = (float)0.5;
		byte b1 = Utils.floatToByte(val1, 0, 1);
		Assert.assertEquals(((short)(0.5*Utils.UByteMaxValue)) , b1);

		val1 = (float)1.0;
		b1 = Utils.floatToByte(val1, 0, 1);
		Assert.assertEquals((byte)0xff , b1);
		
		val1 = (float)0.0;
		b1 = Utils.floatToByte(val1, 0, 1);
		Assert.assertEquals((byte)0x00 , b1);
	}
	
}

