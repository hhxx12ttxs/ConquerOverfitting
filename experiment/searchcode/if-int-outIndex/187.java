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

public class Base64Encoder
{
	public String encodedString = null;

	public Base64Encoder(){
	
	}

	public void setStringToEncode(String tok){
		encodedString = new String(toBase64(tok.getBytes()));
	}

	private static char[] alphabet = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' }; 

	public static byte[] toBase64(byte[] octetStr) 
{ 
	int bits24, bits6; 
	int outIndex = 0;
byte[] out = new byte[((octetStr.length-1)/3+1)*4]; 
	int i = 0; 
	PrintStream pout = System.out;
	for (i = 0; (i+3) <= octetStr.length; ) 
	{ 
	// store the octets 
	bits24 = (octetStr[i++] << 16); 
	bits24 += (octetStr[i++] << 8); 
	bits24 += octetStr[i++]; 
	bits6 = (bits24 & 0xfc0000) >> 18; 
	out[outIndex++] = (byte)alphabet[bits6]; 
	bits6 = (bits24 & 0x3f000) >> 12; 
	out[outIndex++]= (byte)alphabet[bits6]; 
	bits6 = (bits24 & 0xfc0) >> 6;
	out[outIndex++] = (byte)alphabet[bits6]; 
	bits6 = (bits24 & 0x3f); 
	out[outIndex++] = (byte)alphabet[bits6];
	 } 
	 if (octetStr.length-i == 2) { // store the octets 
	 bits24 = octetStr[i] << 16; 
bits24 += octetStr[i+1] << 8; 
bits6 = (bits24 & 0xfc0000) >> 18; 
out[outIndex++] = (byte)alphabet[bits6]; 
bits6 = (bits24 & 0x3f000) >> 12; 
out[outIndex++] = (byte)alphabet[bits6]; 
bits6 = (bits24 & 0xfc0) >> 6; 
out[outIndex++] = (byte)alphabet[bits6]; // padding
out[outIndex++] = (byte)'='; 
} 
else if (octetStr.length-i == 1) { // store the octets 
bits24 = octetStr[i] << 16; 
bits6 = (bits24 & 0xfc0000) >> 18; 
out[outIndex++] = (byte)alphabet[bits6]; 
bits6 = (bits24 & 0x3f000) >> 12; 
out[outIndex++] = (byte)alphabet[bits6]; 
	// padding 
	out[outIndex++] = (byte)'=';
out[outIndex++] = (byte)'='; 
} 
	return out; 
} 
	


}
