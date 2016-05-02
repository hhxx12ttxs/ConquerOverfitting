/*
 Copyright (c) 2007-2009 WebAppShowCase DBA Appcloem (http://www.appcloem.com). All rights reserved.
Apache Software License 2.0
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

package org.jiql.jdbc;
import java.util.*;
import java.sql.*;
import java.io.*;
import tools.util.StringUtil;
import tools.util.SEXParser;
import tools.util.StreamUtil;
import org.jiql.util.*;

public class jiqlBlob implements java.sql.Blob,java.io.Serializable
{
	byte[] b = null;
	
	public jiqlBlob(byte[] b){
		this.b = b;
	}
	
	public void free()throws SQLException{
	} 

 public InputStream getBinaryStream()throws SQLException{
 	return new ByteArrayInputStream(b);
	}  

 public InputStream getBinaryStream(long pos, long length)throws SQLException{
 	
 	return new ByteArrayInputStream(getBytes(pos,new Long(length).intValue()));

	} 
		
 public byte[] getBytes(long pos, int length)throws SQLException{
 	byte[] rec = new byte[length];
 	StreamUtil.readStream(getBinaryStream(), rec,new Long(pos).intValue(),length);
 	return rec;

	}  

 public long length()throws SQLException{
 	return b.length;
	}  

 public long position(Blob pattern, long start)throws SQLException{
	return -1;
	}  

 public long position(byte[] pattern, long start)throws SQLException{
	return -1;

	}  

 public OutputStream setBinaryStream(long pos)throws SQLException{
	        throw JGException.get("not_supported","Not Supported");

	}  

 public int setBytes(long pos, byte[] bytes)throws SQLException{
        throw JGException.get("not_supported","Not Supported");

	}  

 public int setBytes(long pos, byte[] bytes, int offset, int len)throws SQLException{
        throw JGException.get("not_supported","Not Supported");

	}  

 public void truncate(long len)throws SQLException{
        throw JGException.get("not_supported","Not Supported");

	}  
 public String toString(){
 	return b.toString();
 }

}



