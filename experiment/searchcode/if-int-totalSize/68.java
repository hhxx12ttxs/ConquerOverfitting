/*
* Copyright 2010 The Apache Software Foundation
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.bizosys.hsearch.index;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.bizosys.hsearch.common.RecordScalar;
import com.bizosys.hsearch.common.Storable;
import com.bizosys.hsearch.hbase.HReader;
import com.bizosys.hsearch.hbase.HWriter;
import com.bizosys.hsearch.hbase.NV;
import com.bizosys.hsearch.schema.IOConstants;
import com.bizosys.oneline.ApplicationFault;
import com.bizosys.oneline.SystemFault;
import com.bizosys.oneline.services.batch.BatchTask;

/**
 * Defines various term types.
 * To save indexing space, each term type is stored as a byte code.
 * So totally 256 diffierent codes can be defined in the existing index.  
 * @author karan
 *
 */
public class TermType implements BatchTask {
	
	public static String TYPE_KEY = "TERM_TYPE";
	public static byte[] TYPE_KEY_BYTES = TYPE_KEY.getBytes();
	
	public static final String NONE = "NONE";
	public static final Byte NONE_TYPECODE = Byte.MIN_VALUE;
	
	public static final String KEYWORD = "KEYWORD";
	public static final String BODY = "BODY";
	public static final String TITLE = "TITLE";
	public static final String URL_OR_ID = "URL";

	
	public static TermType instance = null;
	public static TermType getInstance() throws SystemFault {
		if ( null != instance ) return instance;
		synchronized (TermType.class) {
			if ( null != instance ) return instance;
			instance = new TermType();
			return instance;
		}
	}
	
	public Map<String, Byte> types = new HashMap<String, Byte>();
	
	private TermType() throws SystemFault {
		/**
		 * Add Reserved Types
		 */
		types.put(NONE, (byte) -125);
		types.put(URL_OR_ID, (byte) -124);
		types.put(TITLE, (byte) -123);
		types.put(BODY, (byte) -122);
		types.put(KEYWORD, (byte) -121);
		this.process();
	}
	
	public byte getTypeCode(String type) throws ApplicationFault {
		if (this.types.containsKey(type))
			return this.types.get(type);
		throw new ApplicationFault("Term Type " + type + " is unknown");
	}
	
	public void persist() throws IOException {
		
		try {
			int totalSize = 0;
			for (String type : types.keySet()) {
				totalSize = totalSize + 
					1 /** Type char length */  + type.length() + 1 /** Reserved for byte mapping */;  
			}
			if ( 0 == totalSize ) return;
			
			byte[] bytes = new byte[totalSize];
			
			int pos = 0;
			int len = 0;
			for (String type : types.keySet()) {
				len =  type.length();
				bytes[pos++] = (byte)len;
				System.arraycopy(type.getBytes(), 0, bytes, pos,len);
				pos = pos + len;
				bytes[pos++] = types.get(type);
			}
			NV nv = new NV(IOConstants.NAME_VALUE_BYTES, 
				IOConstants.NAME_VALUE_BYTES, new Storable(bytes));
			RecordScalar record = new RecordScalar(new Storable(TYPE_KEY_BYTES), nv);
			HWriter.insertScalar(IOConstants.TABLE_CONFIG, record);
			
		} catch (IOException e) {
			e.printStackTrace(System.out);
			throw e;
		}
	}

	public String getJobName() {
		return "TermType";
	}

	public Object process() throws SystemFault {
		NV nv = new NV(IOConstants.NAME_VALUE_BYTES, IOConstants.NAME_VALUE_BYTES);
		
		RecordScalar scalar = new RecordScalar(TYPE_KEY_BYTES, nv);
		HReader.getScalar(IOConstants.TABLE_CONFIG, scalar);
		if ( null == nv.data) return 1;
		
		byte[] bytes = nv.data.toBytes();
		int total = bytes.length;
		
		int pos = 0;
		byte len = 0;
		
		Map<String, Byte> newTypes = new HashMap<String, Byte>();
		while ( pos < total) {
			len =  bytes[pos++];
			byte[] typeB = new byte[len];
			System.arraycopy(bytes, pos, typeB, 0,len);
			pos = pos + len;
			byte typeCode = bytes[pos++];
			newTypes.put(new String(typeB), typeCode);
		}
		Map<String, Byte> temp = this.types;
		this.types = newTypes;
		temp.clear();
		temp = null;
		return 0;
	}

	public void setJobName(String jobName) {
	}
	
}

