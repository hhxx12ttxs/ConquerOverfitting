/*
 Copyright (c) 2007-2009 WebAppShowCase DBA Appcloem (http://www.appcloem.com). All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in
     the documentation and/or other materials provided with the distribution.

  3. The names of the authors may not be used to endorse or promote products
     derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL WebAppShowCase
OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package tools.util;
 import java.util.*;
 import java.io.*;

public class Base64Decoder
{
	public String decodedString = "";

	public Base64Decoder(){
	
	}

	public void setStringToDecode(String tok){
		decodedString = new String(Base64Decoder.fromBase64(tok.getBytes()));
	}

	private static byte[] unalphabet = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63,
52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28,
29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1,
1, -1, -1, -1 }; 

	public static byte[] fromBase64(byte[] octetStr) 
	{ 
		byte[] out; 
		int bits24, bits8; 
		int outIndex; int i; 
		if ((octetStr.length % 4) != 0) 
		{ 
			System.out.println("error, must be a multiple of 4 bytes" + (octetStr.length % 4)); 
			return null; 
		} 
		outIndex = 0; // allocate correct space 
		if (octetStr[octetStr.length-2] == '=') 
			out = new byte[octetStr.length/4*3-2]; 
		else if (octetStr[octetStr.length-1] == '=') 
			out = new byte[octetStr.length/4*3-1]; 
		else 
			out = new byte[octetStr.length/4*3]; 
		for (i = 0; (octetStr.length-i) > 4; ) 
		{ 
			bits24 = (unalphabet[octetStr[i++]] << 18); 
			bits24 += (unalphabet[octetStr[i++]] << 12); 
			bits24 += (unalphabet[octetStr[i++]] << 6); 
			bits24 += unalphabet[octetStr[i++]]; 
			bits8 = (bits24 & 0xff0000) >> 16; 
			out[outIndex++] = (byte)bits8; 
			bits8 = (bits24 & 0x00ff00) >> 8;
			//if (outIndex < out.length -2)
				out[outIndex++] = (byte)bits8;
			bits8 = bits24 & 0x0000ff;
			//if (outIndex < out.length -2)
				out[outIndex++] = (byte)bits8; 
		}
		bits24 = (unalphabet[octetStr[i++]] << 18);
		bits24 += (unalphabet[octetStr[i++]] << 12);
		bits8 = (bits24 & 0xff0000) >> 16; 
		out[outIndex++] = (byte)bits8; 
		if (octetStr[i] != '=') 
		{ 
			bits24 += (unalphabet[octetStr[i++]] << 6); 
			bits8 = (bits24 & 0x00ff00) >> 8; 
			out[outIndex++] = (byte)bits8; 
			if (octetStr[i] != '=') 
			{ 
				bits24 += unalphabet[octetStr[i++]]; 
				bits8 = bits24 & 0x0000ff; 
				out[outIndex++] = (byte)bits8; 
			} 
	} 
		
		return out; 

} 




}
