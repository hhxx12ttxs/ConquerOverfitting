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

package org.jiql.util;
import tools.util.EZArrayList;
import tools.util.StringUtil;
import java.util.Date;
import java.util.*;
import org.jiql.db.*;
//import java.sql.Types;
public class Criteria implements java.io.Serializable
{

String compareoperator = null;
String name = null;
String value = null;
String table = null;
EZArrayList inclause = new EZArrayList();
SQLParser sqp = null;
	public Criteria(){
	}

	public Criteria(String n,String v,String c,SQLParser p){
			this(n,v,c);
			sqp = p;
	
	}
	public void setSQLParser(SQLParser p){
		sqp = p;
	}
	public Criteria(String n,String v,String c,String t,SQLParser p){
			this(n,v,c,t);
			sqp = p;
	
	}

	public Criteria(String n,String v,String c,String t){
		this(n,v,c);
		table = t;
	}

	public Criteria(String n,String v,String c){
	compareoperator = c;
	name = n;
	value = v;
	if (compareoperator.equals("in"))
	{
		inclause = new EZArrayList(new StringTokenizer(value,","));
	}
	}

public EZArrayList getInClause(){
return inclause;
}

public String getCompareOperator(){
return compareoperator;
}

public String getValueString(){
	return value;
}
public Object getValue(){
	if (sqp != null){
		String cn = sqp.getRealColName(name);
		return sqp.convert(value,cn);
		/*if (cn != null){
			TableInfo ti = sqp.getTableInfo();
			ColumnInfo ci = ti.getColumnInfo(cn);
			if (ci != null){
				try{
				
				int cit = ci.getColumnType();
				if (cit ==  )
					return new  (value);
				else if (cit == Types.DATE)
					return new Date(value);
				else if (cit == Types.FLOAT)
					return new Double(value);
				}catch (Exception e){
					tools.util.LogMgr.err("CRhITERIA.getValue " + e.toString());
				}
			}
		}*/

	}
return value;
}

public String getName(){
return name;
}

public String getRealName(){
	if (sqp != null)
		return sqp.getRealColName(name);

return name;
}


public String toString(){
	return "SQLCriteria:" + name + compareoperator + value;
}

}
