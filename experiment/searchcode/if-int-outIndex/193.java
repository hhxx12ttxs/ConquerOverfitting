/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "xml-axis-wsil" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.wsil;


/**
 * Converts to Base 64.
 *
 */
class Base64Converter 
{
  /**
   * Constants for conversion.
   *
   */
  public static final char alphabet[] = {
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 
    'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 
    'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 
    'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 
    'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 
    'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', 
    '8', '9', '+', '/'
  };

  /**
   * Creates a new <code>Base64Converter</code> instance.
   *
   */
  Base64Converter() 
  {
  }

  /**
   * Encodes the passed string to Base64.
   *
   * @param s a <code>String</code> value
   * @return a <code>String</code> value
   */
  public static String encode(String s) 
  {
    return encode(s.getBytes());
  }

  public static String encode(byte octetString[]) 
  {
    char out[] = new char[((octetString.length - 1) / 3 + 1) * 4];
    int outIndex = 0;
    int i;
  
    for(i = 0; i + 3 <= octetString.length;) 
    {
      int bits24 = (octetString[i++] & 0xff) << 16;
      bits24 |= (octetString[i++] & 0xff) << 8;
      bits24 |= (octetString[i++] & 0xff) << 0;
      int bits6 = (bits24 & 0xfc0000) >> 18;
      out[outIndex++] = alphabet[bits6];
      bits6 = (bits24 & 0x3f000) >> 12;
      out[outIndex++] = alphabet[bits6];
      bits6 = (bits24 & 0xfc0) >> 6;
      out[outIndex++] = alphabet[bits6];
      bits6 = bits24 & 0x3f;
      out[outIndex++] = alphabet[bits6];
    }

    if(octetString.length - i == 2) 
    {
      int bits24 = (octetString[i] & 0xff) << 16;
      bits24 |= (octetString[i + 1] & 0xff) << 8;
      int bits6 = (bits24 & 0xfc0000) >> 18;
      out[outIndex++] = alphabet[bits6];
      bits6 = (bits24 & 0x3f000) >> 12;
      out[outIndex++] = alphabet[bits6];
      bits6 = (bits24 & 0xfc0) >> 6;
      out[outIndex++] = alphabet[bits6];
      out[outIndex++] = '=';
    } 
    else
      if(octetString.length - i == 1) 
      {
        int bits24 = (octetString[i] & 0xff) << 16;
        int bits6 = (bits24 & 0xfc0000) >> 18;
        out[outIndex++] = alphabet[bits6];
        bits6 = (bits24 & 0x3f000) >> 12;
        out[outIndex++] = alphabet[bits6];
        out[outIndex++] = '=';
        out[outIndex++] = '=';
      }

    return new String(out);
  }
}

