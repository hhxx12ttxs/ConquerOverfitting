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

import tools.util.NameValuePairs;
import tools.util.NameValue;
import tools.util.EZArrayList;
//import tools.util.StringUtil;
import tools.util.Crypto;
import tools.util.StringUtil;
import tools.util.SharedMethods;
import java.io.*;
import java.util.*;
import java.sql.SQLException;
import org.jiql.db.objs.*;
import org.jiql.db.*;
import java.sql.Types;
import java.text.DateFormat;
import org.jiql.jdbc.jiqlConnection;
import org.jiql.db.select.SelectParser;
import org.jiql.db.insert.InsertParser;
import org.jiql.db.jdbc.stat.StatementProcessor;
import tools.util.AlphaNumeric;
import org.jiql.db.SpecialCommandParser;

public  class SQLParser implements java.io.Serializable
{
	boolean special = false;
	GroupBy groupby = new GroupBy();
	LoadData loadtable = null;
	InsertData inserttable = null;
	SelectParser selectP = null;
	CreateParser createP = null;
	InsertParser insertP = null;
	StringFunctions sfunctions = new StringFunctions();
	String tok = null;
	String action = null;
	Hashtable hash = new Hashtable();
	Vector selectList = new Vector();
	Vector origSelectL = new Vector();
	Vector includealllist  = new Vector();
	Vector eitheroralllist  = new Vector();
	Vector sortList = new Vector();
	Vector results = new Vector();
	Hashtable resultsT = new Hashtable();
	boolean desc = false;
	NameValuePairs aliases = new NameValuePairs();
	
	NameValuePairs notquoted = new NameValuePairs();
	boolean distinct = false;
	
	Vector updatelist =  new Vector();
	String table = null;
	
	boolean count = false;
	
	Hashtable selectAS = new Hashtable();
   	Hashtable<String,String> selectAS2 = new Hashtable<String,String>();
   	static Vector<String> reserved  = new Vector<String>();
   	static Vector<String> sreserved  = new Vector<String>();
   	
   	String prefixValue = "jiql";
boolean prefix = true;
StatementProcessor spro = null;

Hashtable attributes = new Hashtable();
public void setAttribute(String n,Object v){
	attributes.put(n,v);
}

public Object getAttribute(String n){
	return attributes.get(n);
}

public StatementProcessor getStatementProcessor(){
	return spro;
}
public void setStatementProcessor(StatementProcessor sp){
	spro = sp;
}
public boolean hasPrefix(){
	return prefix;
}
public boolean isSpecial(){
	return special;
}

public String getPrefixValue(){
	return prefixValue;
}
boolean tableleafs = true;
public boolean hasTableleafs(){
	return tableleafs;
}

	static {
		reserved.add("where");
		reserved.add("order");
		sreserved.add("from");
		reserved.add("group");
	}
public static Vector<String> getReserved(){
	return reserved;
}
public DateFormat getDateFormat(){
			return getConnection().getDateFormat();

	}
	public String getDatePattern(){
			return getConnection().getDatePattern();

	}
	
	public LoadData getLoadTable(){
		if (loadtable == null)
			loadtable = new LoadData(this,tok);
		return loadtable;
	}
	public InsertData getInsertIntoTable(){
		if (inserttable == null)
			inserttable = new InsertData(this,tok);
		return inserttable;
	}
	public InsertParser getInsertParser(){
		if (insertP == null)
			insertP = new InsertParser(this);
		return insertP;
	}

	public SelectParser getSelectParser(){
		if (selectP == null)
			selectP = new SelectParser(this);
		return selectP;
	}
	
	public CreateParser getCreateParser(){
		if (createP == null)
			createP = new CreateParser(this);
		return createP;
	}
		Union union = null;
		public Union getUnion(){
			if (union == null)
				union = new Union(this);
			return union;
		}
		void updateSelectAS(String o,String n)throws SQLException{
		String itm = (String)selectAS.remove(o);
		if (itm != null && StringUtil.isRealString(n))
			selectAS.put(n,itm);
	}
	
	public Vector getOriginalSelectList(){
		return origSelectL;
	}
	
	public void setOriginalSelectList(Vector v2){
		
		String l = null;
		int i = 0;
		for (int ct = 0;ct < v2.size();ct++)
		{
			l = v2.elementAt(ct).toString();
			i =  l.indexOf(" ");
			if (i > 0)
			{
				l = l.substring(0,i);
			}
				l = StringUtil.getTrimmedValue(l);
			if (!origSelectL.contains(l))
				origSelectL.add(l);
		}
		
		
	}
	public void addAlias(String a,String t){
		aliases.put(a, t);
	}
		public Hashtable getSelectAS(){
	return selectAS;
	}

		public Hashtable<String,String> getSelectAS2(){
	return selectAS2;
	}
	
	String getSAlias(String n){
		
		String a = (String)selectAS.get(n);
		if (a == null)
			a = selectAS2.get(n);
		if (a != null)return a;
		return null;
	}
	
		public String getTAlias(String n){
		Enumeration en = aliases.keys();
		String a = null;
		while (en.hasMoreElements())
		{
			a = en.nextElement().toString();
			if (aliases.get(a).equals(n))
				return a;
		}
		
		return null;
	}
	public void updateSelects()throws SQLException{
		String itm = null;
		int i = 0;
		for (int ct = 0;ct < selectList.size();ct++)
		{
			itm = selectList.elementAt(ct).toString();
			String itm2 = parseSelectAlias(itm);
			//(itm + " parse AS2  " + itm2);

			itm2 = sfunctions.parse(itm2);

			if (!itm2.equals(itm))
			{
				itm = itm2;
				selectList.setElementAt(itm,ct);
				
			}
			if (itm.startsWith(table + ".")){
			itm = StringUtil.getTrimmedValue(itm.substring(table.length() + 1,itm.length()));
			if (StringUtil.isRealString(itm))
			selectList.setElementAt(getSelectParser().parseFunctions(new StringBuffer(itm),null).toString(),ct);
			}
			//itm =  (itm);
			i =  itm.toLowerCase().indexOf(" as ");
			if (i > 0){
			if (StringUtil.isRealString(StringUtil.getTrimmedValue(itm.substring(0,i)))){
			StringBuffer un = new StringBuffer(StringUtil.getTrimmedValue(itm.substring(0,i)));
			StringBuffer uv = new StringBuffer(StringUtil.getTrimmedValue(itm.substring(i + 3,itm.length())));
			getSelectParser().parseFunctions(un,uv.toString());

		//	 List.setElementAt(un.toString(),ct);
	//		 ( List.elementAt(ct),uv.toString());
	//		 (uv.toString(), List.elementAt(ct).toString());
			selectList.setElementAt(uv.toString(),ct);
			selectAS.put(selectList.elementAt(ct),un.toString());
			//(ct + " ADD TO   LIST " + uv.toString() + ":" + un.toString() +  AS);


			// (un.toString(), List.elementAt(ct));

			// (uv.toString(), List.elementAt(ct).toString());


			}
			}
		}
	}
	
	public StringFunctions getSFunctions(){
		return sfunctions;
	}
	public void parseSelectAS()throws SQLException{
		String itm = null;
		int i = 0;
		Union union = getUnion();
   Vector<String> rmv  = new Vector<String>();	
		for (int ct = 0;ct < selectList.size();ct++)
		{
			itm = selectList.elementAt(ct).toString();
			itm = sfunctions.parse(itm);

			String itm2 = parseSelectAlias(itm);
			//(itm + " parse AS1  " + itm2);
			if (!itm2.equals(itm))
			{
				itm = itm2;
				selectList.setElementAt(itm,ct);
				
			}

			if (union.addToSelectList(itm)){

				rmv.add(itm);
			}
		}
		
		while (rmv.size() > 0){
			selectList.remove(rmv.elementAt(0));
			rmv.removeElementAt(0);
		}
		
		updateSelects();
	}
	
	/*public static   get(String t,Properties u)throws SQLException{
		return new  (t,u);
	}*/
	public static SQLParser get(String t,jiqlConnection c)throws SQLException{
		return new SQLParser(t,c);
	}
	
		public boolean isNotQuoted(String c){
		return notquoted.getBoolean(c);
	}
	
		public boolean isCount(){
					//(getSelectParser().getSQLFunctionParser().hasFunction("count") + " SC 2" + this);

		return ((count || getSelectParser().getSQLFunctionParser().hasFunction("count") ) && (getSelectList().size() < 2));
	}
	
	public Vector getSortList(){
	return sortList;
	}
	
	
	public Hashtable getResultsTable(){
	return resultsT;
	}
	
		public void setResultsTable(Hashtable r){
	 resultsT = r;
	}
	
	
	public Vector getResults(){
	return results;
	}
	
	public void setResults(Vector r)throws SQLException{
		setResults(r,true);
	}
	
	public boolean isJoin(){
		if (union == null)
		return false;
		return union.isJoin();
}
		public void setResults(Vector r,boolean u)throws SQLException{
				if (u)
				if (union != null)
				{
					r = union.join(r);
					//union = null;
					
				}
	 results = r;
	}

	public boolean isDescending(){
		return desc;
	}

	
	public boolean isDistinct(){
		return distinct;
	}

	public NameValuePairs getAliases(){
	return aliases;
	}
	public void mergeAliases(Hashtable h){
		aliases.merge(h);	
	}

	public Vector getEitherOrAllList(){
	return eitheroralllist;
	}
	
	
	
	public Vector getIncludeAllList(){
	return includealllist;
	}
	
	public EZArrayList getPrimaryKeys(){
	return primaryKeys;
	}
	
		
	public Vector getUpdateList(){
	return updatelist;
	}
	Properties properties = null;
	private String toko = null;
	public Properties getProperties(){
		return properties;
	}
	transient jiqlConnection connection = null;
	public SQLParser(String t,jiqlConnection c)throws SQLException{
		connection = c;

		setSQLParser(t,c.getProperties());
	}
	public jiqlConnection getConnection(){
		return connection;
	}
	public void setConnection(jiqlConnection c){
		connection = c;
		connection.setIdentity(getInsertParser().getAutoIncrementValue());
		String tbl = (String)getAttribute("removeMetaCache");
		if (tbl != null)
	CacheMgr.removeMetaCache(connection.getProperties().getProperty("baseUrl"),tbl);

	}
	String osql = null;
	
	public  String getOriginalSQL(){
		return osql;
	}
	public void setSQLParser(String t,Properties u)throws SQLException{
		properties = u;
		if (osql == null)
			osql = t;
		tok = t;
		toko = tok;
		parse();
	}
	public SQLParser(Properties u)throws SQLException{
		properties = u;
	}
	
	public String toString(){
		return "jiql.SQLParser [ORIGINAL SQL:" + osql + " ] table:" + table + ";aliases:" + aliases  + ";values:" + hash + ";selects:" + selectList + ";selectAS:" + selectAS + ";selectAS2:" + selectAS2 +  ";includealllist:" +  includealllist + ";eitheroralllist:" + eitheroralllist + ";getOriginalSelectList:" + getOriginalSelectList()+ ";groupby:" + groupby + ";union:" + union;
	}

		/*public String toString(){
		return "jiql.  table:" + table + "; s:" +    ;
	}*/
	
	public String getStatement(){
		return toko;
	}
	public String getAction(){
		return action;
	}
	public String getTable(){
		return table;
	}
	public void setTable(String t){
		t = StringUtil.replaceSubstring(t,"\"","");
		t = StringUtil.replaceSubstring(t,"'","");
		t = StringUtil.replaceSubstring(t,"`","");
		if (t.startsWith("jiql."))
			t = t.substring(5,t.length());
		table = t;
	
		int i = table.indexOf(" ");
	if (i > 0)
	{
		t = table;
		String a = t.substring(i + 1,t.length());
		t = t.substring(0,i);
		table = t;
		addAlias(t,a);
	}
	
	
		if (connection == null || !connection.isRemote())
		try{
		
		TableInfo ti = Gateway.get(getProperties()).readTableInfo(getTable());
		setTableInfo(ti);
		}catch (Exception e){
			tools.util.LogMgr.err(t + " setTable.setTableInfo " + tbi + ":" +e.toString());
			e.printStackTrace();
		}

	}
	
	TableInfo tbi = null;

	public TableInfo getTableInfo(){
		return tbi;
	}


	jiqlTableInfo ti = null;

	public jiqlTableInfo getJiqlTableInfo()throws java.sql.SQLException{
		if (ti == null)
			ti = jiqlDBMgr.get(getProperties()).getTableInfo(getTable());
			return ti;
	}
	
		public jiqlTableInfo getJiqlTableInfo(boolean tf)throws java.sql.SQLException{
		if (ti == null)
			ti = jiqlDBMgr.get(getProperties()).getTableInfo(getTable(),tf);
			return ti;
	}
	
	public void setTableInfo(TableInfo ti){
		tbi = ti;
	}
	
	public Object convert(Object value,String c){
		
		String cn = getRealColName(c);
		if (cn != null){
			TableInfo ti = getTableInfo();
			if (ti != null){
			
			ColumnInfo ci = ti.getColumnInfo(cn);
			if (ci != null){
				int cit = -34;
				try{
				
				cit = ci.getColumnType();
				Object o = jiqlCellValue.getObj(value,cit,this);
				return o;
				/*if (cit ==  ){
					//(c + " convert: " + ":" +  value + ":" +  value.getClass().getName());
	
					return new  (value.toString());
				}
				else if (cit == Types.DATE)
					return new Date(value.toString());
				else if (cit == Types.FLOAT)
					return new Double(value.toString());*/
				}catch (Exception e){
					tools.util.LogMgr.err(value + ":" + c  + ":" + Types.DATE + ":" + cit + " CRITERIA.getValue " + e.toString());
					//ByteArrayOutputStre
					//e.printStackTrace();
					JGUtil.olog(e);
				}
			}
		}
		}
		
		
		return value;
		
	}
	
	/*public String getRealColName2(String n,String t){
					if (t != null)
			try{

			jiqlTableInfo jti = null;
			String tn = null;

			jti =  jiqlDBMgr.get(getProperties()).getTableInfo( t);
			if (jti == null)return n;
			Vector v1 = jti.getFieldList();
						//(n + " ********NEW TTT " + v1.contains(n) + ":" + t + ":" + v1);

			if (v1.contains(n))return n;


			//getJiqlTableInfo();
			Hashtable ch = null;
			if (jti != null)
			ch = jti.getConstraints();
			jiqlConstraint pk = null;
			//if (ch != null)
			//	pk = (jiqlConstraint)ch.get(n);
			
			if (ch != null)
			{
			
			Enumeration en = ch.elements();
			while(en.hasMoreElements())
			{
			
			pk = (jiqlConstraint)en.nextElement();
			if (pk == null || pk.size() < 1)continue;
			if (jiqlConstraint.FOREIGNKEY == pk.getType())
			{

			String n1 = null;
			String v = null;
			jiqlFunction ref = pk.getReference();
			for (int ct = 0;ct < pk.size();ct++)
			{
			n1 = pk.elementAt(ct).toString();
		 	v = ref.elementAt(ct).toString();
			//( t + ":" + pk.getName() + ":" + ref.getName() +  " ***log getRealColName 1a " +  ":" + n + ":" + aliases + ":" + ref + ":" + pk + ":" + n1 + ":" + v + getTable());

			////( rn1 + ":" + pk.getName() + ":" + ref.getName() +  " log getRealColName 1a " +  ":" + n + ":" + aliases + ":" + ref + ":" + pk + ":" + n1 + ":" + v + getTable());

			}
			}
			}
			}
			
			////(  " log getRealColName 2 " +  ":" + n + ":" + aliases + ":" + ch + ":" + pk);
			}catch (Exception e){
				tools.util.LogMgr.err(n + " ERROR getRealName2 " + e.toString());
			}
			return n;
	}*/
	public String getRealColName(String n){
		String rn1 = n;
					int i2 = 0;
					//String oldn = n;
					
					i2 = n.indexOf(".");
					/*if (i2 > 0){
						n = n.substring(i2 +1 ,n.length());
					}*/
					String rn = (String)selectAS.get(n);
					//(rn +  " RN " + n + ":" +  AS);
					//if (rn == null)
					//	rn =  AS2.get(n);
				//( rn + "  ********** getRealColName SHESHERE:" + n + ":" + aliases + ":" + getTable());

					if (rn != null)n = rn;
					i2 = n.indexOf(".");
	
							//(  " log getRealColName 333 " + rn + ":" + n + ":" + table + ":" +  aliases);
//the_role:the_role:realm_userrole1:{role=realm_userrole1}
//the_role RN role.realm_rolename:{role.realm_rolename=the_role}


			if (i2 > 0){
				String al = n.substring(0,i2);
				al = al.trim();
				String ta = (String)aliases.get(al);

				if (ta !=  null || al.equals(table)){
					n = n.substring(i2+1,n.length());
					//( al + " log getRealColName aa " + ta + ":" + n + ":" + table + ":" +  aliases);
//feature log getRealColName aa null:feature_type_id:feature:{}
					//return getRealColName2(n,table);
					return n;
					
				}
			}
			

			
			n =  getSelectASRealName(n);
				 i2 = n.indexOf(".");

						if (i2 > 0){
				String al = n.substring(0,i2);
				al = al.trim();
				String ta = (String)aliases.get(al);

				if (ta !=  null){
					n = n.substring(i2+1,n.length());
				//( al + " log getRealColName gg2 " + ta + ":" + n + ":" + aliases);

					return n;
					
				}
			}

			return n;
		
	}
	
	
		public NameValue parseUpdate(String tok)throws SQLException{
		tok = StringUtil.getTrimmedValue(tok);
		int i = tok.indexOf("=");
		if (i > 0){
			NameValue nv = new NameValue();
			nv.name = tok.substring(0,i).trim();
			nv.value = StringUtil.getTrimmedValue(tok.substring(i + 1,tok.length()).trim());
			nv.value = decode(nv.value);

			//va = StringUtil.getTrimmedValue(va);
			//va = decode(va);
			//hash.put(n,convert(va,n));


			return nv;
			
		}
		
		String tok2 = tok;
		i = tok2.indexOf(" in ");
		if (i > 0){
			String n = tok.substring(0,i);
			String v = tok.substring(i +" in ".length(),tok.length());
			v = v.trim();
			if (v.startsWith("("))
				v = v.substring(1,v.length());
			if (v.endsWith(")"))
				v = v.substring(0,v.length() -1);
			v = v.trim();	
			NameValue nv = new NameValue();
			nv.name = n;
			nv.value = v;
			return nv;
		}
		return null;
	}
	
	String getSelectASRealName(String n){
		/*
		String rn = (String) AS.get(n);
			//(rn + ":" +   + "  AS " + n + ":" + aliases);
			if (rn == null)
			rn = (String) AS2.get(n);
			if (rn != null) n = rn;
		
		Enumeration en =  AS.keys();
		while (en.hasMoreElements())
		{
			rn = en.nextElement().toString();
			if ( AS.get(rn).equals(n))
			{
			
				n = rn;
				break;
			}
		}
*/
		
		return n;
		
	}
	public Criteria parseCriteria(String tok)throws SQLException{
		tok = StringUtil.getTrimmedValue(tok);
		if (tok.startsWith("("))
		{
			tok = tok.substring(1,tok.length() - 1);
		}
		int i = tok.indexOf(">=");
				if (i > 0){

			String v = tok.substring(i + 2,tok.length());
			
			String n = tok.substring(0,i);
			n = getSelectASRealName(n);

			int i2 = n.indexOf(".");
	
				//(n + " NEW COL b " + v);

			if (i2 > 0){
				String al = n.substring(0,i2);
				al = al.trim();
				String ta = (String)aliases.get(al);


				if (ta !=  null){
					n = n.substring(i2+1,n.length());

			return new Criteria(StringUtil.getTrimmedValue(n),StringUtil.getTrimmedValue(decode(v)),">=",ta,this);

				}
			}
			return new Criteria(StringUtil.getTrimmedValue(n),StringUtil.getTrimmedValue(decode(v)),">=",table,this);
			
		}

		i = tok.indexOf("<=");
				if (i > 0){

			String v = tok.substring(i + 2,tok.length());
			
			String n = tok.substring(0,i);
			n = getSelectASRealName(n);

			int i2 = n.indexOf(".");
	
				//(n + " NEW COL b " + v);

			if (i2 > 0){
				String al = n.substring(0,i2);
				al = al.trim();
				String ta = (String)aliases.get(al);


				if (ta !=  null){
					n = n.substring(i2+1,n.length());

			return new Criteria(StringUtil.getTrimmedValue(n),StringUtil.getTrimmedValue(decode(v)),"<=",ta,this);

				}
			}
			return new Criteria(StringUtil.getTrimmedValue(n),StringUtil.getTrimmedValue(decode(v)),"<=",table,this);
			
		}

			i = tok.indexOf("!=");
				if (i > 0){

			String v = tok.substring(i + 2,tok.length());
			
			String n = tok.substring(0,i);
			n = getSelectASRealName(n);


			int i2 = n.indexOf(".");
	
				//(n + " NEW COL b " + v);

			if (i2 > 0){
				String al = n.substring(0,i2);
				al = al.trim();
				String ta = (String)aliases.get(al);


				if (ta !=  null){
					n = n.substring(i2+1,n.length());

			return new Criteria(StringUtil.getTrimmedValue(n),StringUtil.getTrimmedValue(decode(v)),"!=",ta,this);

				}
			}
			return new Criteria(StringUtil.getTrimmedValue(n),StringUtil.getTrimmedValue(decode(v)),"!=",table,this);
			
		}
		
		i = tok.indexOf("=");	
		if (i > 0){

			String v = tok.substring(i + 1,tok.length());
			
			String n = tok.substring(0,i);
			n = getSelectASRealName(n);

			int i2 = n.indexOf(".");
	
				//(n + " NEW COL b " + v);

			if (i2 > 0){
				String al = n.substring(0,i2);
				al = al.trim();
				String ta = (String)aliases.get(al);


				if (ta !=  null){
					n = n.substring(i2+1,n.length());

			return new Criteria(StringUtil.getTrimmedValue(n),StringUtil.getTrimmedValue(decode(v)),"=",ta,this);

				}
			}
			return new Criteria(StringUtil.getTrimmedValue(n),StringUtil.getTrimmedValue(decode(v)),"=",table,this);
			
		}
		
		i = tok.indexOf(">");
				if (i > 0){

			String v = tok.substring(i + 1,tok.length());
			
			String n = tok.substring(0,i);
			n = getSelectASRealName(n);

			int i2 = n.indexOf(".");
	
				//(n + " NEW COL b " + v);

			if (i2 > 0){
				String al = n.substring(0,i2);
				al = al.trim();
				String ta = (String)aliases.get(al);


				if (ta !=  null){
					n = n.substring(i2+1,n.length());

			return new Criteria(StringUtil.getTrimmedValue(n),StringUtil.getTrimmedValue(decode(v)),">",ta,this);

				}
			}
			return new Criteria(StringUtil.getTrimmedValue(n),StringUtil.getTrimmedValue(decode(v)),">",table,this);
			
		}
		
		i = tok.indexOf("<");
				if (i > 0){

			String v = tok.substring(i + 1,tok.length());
			
			String n = tok.substring(0,i);
			n = getSelectASRealName(n);

			int i2 = n.indexOf(".");
	
				//(n + " NEW COL b " + v);

			if (i2 > 0){
				String al = n.substring(0,i2);
				al = al.trim();
				String ta = (String)aliases.get(al);


				if (ta !=  null){
					n = n.substring(i2+1,n.length());

			return new Criteria(StringUtil.getTrimmedValue(n),StringUtil.getTrimmedValue(decode(v)),"<",ta,this);

				}
			}
			return new Criteria(StringUtil.getTrimmedValue(n),StringUtil.getTrimmedValue(decode(v)),"<",table,this);
			
		}
		
		String tok2 = tok;
		i = tok2.indexOf(" in ");
		if (i < 0)
			i = tok2.indexOf(" IN ");
		if (i > 0){
			String n = tok.substring(0,i);
			n = getSelectASRealName(n);

			String v = tok.substring(i +" in ".length(),tok.length());
			v = v.trim();
			if (v.startsWith("("))
				v = v.substring(1,v.length());
			if (v.endsWith(")"))
				v = v.substring(0,v.length() -1);
			v = v.trim();	
			
			int i2 = n.indexOf(".");
			if (i2 > 0){
				String al = n.substring(0,i2);
				al = al.trim();
				String ta = (String)aliases.get(al);
				if (ta !=  null){
					n = n.substring(i2+1,n.length());
			return new Criteria(StringUtil.getTrimmedValue(n),StringUtil.getTrimmedValue(decode(v)),"in",ta,this);

				}
			}
			return new Criteria(StringUtil.getTrimmedValue(n),StringUtil.getTrimmedValue(decode(v)),"in",table,this);
			
		
		
		}
		
		i = tok2.indexOf(" is ");
		if (i > 0){
			String n = tok.substring(0,i);
			n = getSelectASRealName(n);

			String v = tok.substring(i +" is ".length(),tok.length());
			v = v.trim();
			int i2 = n.indexOf(".");
			if (i2 > 0){
				String al = n.substring(0,i2);
				al = al.trim();
				String ta = (String)aliases.get(al);
				if (ta !=  null){
					n = n.substring(i2+1,n.length());
			return new Criteria(StringUtil.getTrimmedValue(n),StringUtil.getTrimmedValue(decode(v)),"is",ta,this);

				}
			}
			return new Criteria(StringUtil.getTrimmedValue(n),StringUtil.getTrimmedValue(decode(v)),"is",table,this);
			
		
		
		}
		
		
		return null;
	}
	
	String parseTable(String table)throws SQLException{
		int ji = table.indexOf(",");
		//(ji + " PSTBL1 : " + table);
		if (ji > 0){
			getUnion().parseTables(table.substring(ji + 1,table.length()).trim());
			table = table.substring(0,ji).trim();
		}
		table = table.trim();
		return table;		
		
	}
	static String parseTableAlias(String tok){
int i3 = tok.indexOf(" ");
			if (i3 > 0){
				String tok1 = tok.substring(0,i3 + 1);
				String tok2 = tok.substring(i3 + 1,tok.length());
				tok2 = StringUtil.getTrimmedValue(tok2);
				String tok3 = tok2;
				i3 = tok3.indexOf(" ");
				if (i3 > 0)
					tok3 = StringUtil.getTrimmedValue(tok3.substring(0,i3));
				i3 = tok3.indexOf(",");
				if (i3 > 0)
					tok3 = StringUtil.getTrimmedValue(tok3.substring(0,i3));

				tok3 = tok3.toLowerCase();
				if (!reserved.contains(tok3) && !tok3.equals("as") && StringUtil.isRealString(tok2) && !tok2.startsWith(","))
					tok = tok1 + "as " + tok2;
				
			}
			//("  1 " + tok);
			return tok;		
	}
	
		public  static String parseSelectAlias(String tok){
int i3 = tok.indexOf(" ");
			if (i3 > 0){
				String tok1 = tok.substring(0,i3 + 1);
				String tok2 = tok.substring(i3 + 1,tok.length());
				tok2 = StringUtil.getTrimmedValue(tok2);
				String tok3 = tok2;
				i3 = tok3.indexOf(" ");
				if (i3 > 0)
					tok3 = StringUtil.getTrimmedValue(tok3.substring(0,i3));
				i3 = tok3.indexOf(",");
				if (i3 > 0)
					tok3 = StringUtil.getTrimmedValue(tok3.substring(0,i3));

				tok3 = tok3.toLowerCase();
				if (!sreserved.contains(tok3) && !tok3.equals("as")&& StringUtil.isRealString(tok2)&& !tok2.startsWith(","))
					tok = tok1 + "as " + tok2;
				
			}
						//("  2 " + tok);

			return tok;		
	}
	
		public String getToken(){
			return tok;
		}
		public void setSpecial(boolean tf){
			special = tf;
		}
		public void setAction(String a){
			action = a;
		}
		public boolean showTables(){
			return action.equals("showTables");
		}
		public void parse()throws SQLException{
		tok = tok.trim();
		if (tok.endsWith(";"))
		{
			tok = tok.substring(0,tok.length() - 1);
			tok = tok.trim();
		}
		tok = encode(tok);

		tok = StringUtil.replaceSubstring(tok, " join ",",");
		tok  = StringUtil.replaceSubstring(tok, " on "," where ");
		tok = StringUtil.replaceSubstring(tok, " JOIN ",",");
		tok = StringUtil.replaceSubstring(tok, " ON "," where ");

//("SELECt ME " + tok);
		
		String tok2 = tok.toLowerCase();
		String firstToken = tok2;
		int ti = firstToken.indexOf(" ");
		if (ti > 0)
			firstToken = firstToken.substring(0,ti);
		if (tok2.startsWith("select ")){
		action = "select";
		tok = tok.substring("select ".length(),tok.length());
		tok = tok.trim();
			StringBuffer tok2b = new StringBuffer (tok);
			tok2b = getSelectParser().parse(tok2b);
			tok = tok2b.toString();


		tok2 = tok.toLowerCase();
		
			
		//
		if (tok2.startsWith("@@identity")){
		special = true;
		setAction("getIdentity");
		
		return;

		}

		else if (tok2.startsWith("found_rows()")){
		special = true;
		setAction("getFoundRows");
		
		return;

		}
		else if (tok2.startsWith("distinct ")){
		distinct = true;
		tok = tok.substring("distinct ".length(),tok.length());
		tok = tok.trim();
		tok2 = tok.toLowerCase();
		}


		int si = tok2.indexOf(" from ");
		if (si < 0)
		{
			if (getSelectParser().parseNoFrom(tok2))
				return;
			//throw  ("SELECT STATEMENT MISSING FROM CLAUSE " + tok);
			throw JGException.get("missing_from_clause","SELECT STATEMENT MISSING FROM CLAUSE " + tok);

		}
		String sels = tok.substring(0,si);
		sels = sels.trim();
		StringBuffer selsb = new StringBuffer(sels);
		selsb = SharedMethods.replaceSubstringBuffer(selsb,"\"","");
		selsb = SharedMethods.replaceSubstringBuffer(selsb,"'","");
		selsb = SharedMethods.replaceSubstringBuffer(selsb,"`","");

		selectList = new EZArrayList(new StringTokenizer(selsb.toString(),","));
		////();
		if (sels.toLowerCase().startsWith("count(") && sels.endsWith(")"))
		{
			count = true;
			selectList = new EZArrayList();
			selectList.add("*");
			//("SC 1" + this);
		}
		//origSelectL = (Vector)selectList.clone();
		setOriginalSelectList(selectList);

		tok = tok.substring(si + " from ".length(),tok.length());
		tok = tok.trim();
		tok2 = tok.toLowerCase();

		int i = tok.indexOf(" ");
		if (i < 0)
		{

			table = tok.substring(0,tok.length());
			table = parseTable(table);
			setTable(table);
			parseSelectAS();

			return;
		}

		String jtl = "";
		while (true){
			tok = parseTableAlias(tok);
			
			int i3 = tok.toLowerCase().indexOf(" as ");
			if (i3 > 0){
				jtl = jtl + tok.substring(0,i3 + " as ".length());

				tok = tok.substring(i3 + " as ".length(),tok.length());
				tok = StringUtil.getTrimmedValue(tok);
				
			}
			i = tok.indexOf(" ");
			
			int i2 = tok.indexOf(" ,");
			if (i2 > 0 && i2 <= i)
				i = i+ 1;
			i2 = tok.indexOf(",");

			if (i2 > 0 && i2 < i)
				i = i2;
				if (i < 1)
				{
					jtl = jtl + tok;
					break;
				}
				table = tok.substring(0,i + 1);
				jtl = jtl + table;
					int ji = table.indexOf(",");
			if (ji > 0){
							//jtl = jtl + table;

				tok = tok.substring(i + 1,tok.length());
				tok = tok.trim();
			}
			else break;
		}
		table = jtl;
		
		int ji = table.indexOf(",");
		//(ji + " PSTBL2 : " + table);

		if (ji > 0){
			getUnion().parseTables(table.substring(ji + 1,table.length()).trim());
			table = table.substring(0,ji).trim();
		}
		table = table.trim();
		table = parseTableAlias(table);
		int tas = table.toLowerCase().indexOf(" as ");
		if (tas > 0)
		{
			String al = table.substring(tas + " as ".length(),table.length());
			table = table.substring(0,tas);
			al = StringUtil.getTrimmedValue(al);
			table = StringUtil.getTrimmedValue(table);

			if (StringUtil.isRealString(al))
			aliases.put(al,table);
			
		}
		setTable(table);
		parseSelectAS();

		tok = tok.substring(i + 1,tok.length());
		tok = tok.trim();
		tok = groupby.parse(tok);
		tok2 = tok.toLowerCase();
		//i = tok2.indexOf("   ");
		//("OBY b " + tok2);

		//if (i > -1)
		i = tok2.indexOf(" order by ");
		if (i < 0)
			i = tok2.indexOf("order by ");

		if (i > -1)
		{
		String oby = tok.substring(i + "order by ".length(),tok.length() );
		tok = tok.substring(0,i);
		tok = tok.trim();
		tok2 = oby.toLowerCase();

		i = tok2.indexOf(" desc");
		if (i > 0){
			
			oby = oby.substring(0,i);
			desc = true;
		}

		i = tok2.indexOf(" asc");
		if (i > 0){
			
			oby = oby.substring(0,i);
		}
		oby = oby.trim();
		//("OBY a " + oby);
		sortList = new EZArrayList(new StringTokenizer(oby,","));



		tok2 = tok.toLowerCase();

		}
		if (tok2.startsWith("where "))
		{
			tok2 = " " + tok2;
			tok = " " + tok;
		}
		//("HELLO " + tok);
		i = tok2.indexOf(" where ");
		if (i > -1){
		String al = tok.substring(0,i);
		al = al.trim();
		if (al.toLowerCase().startsWith("as "))
		{
			al = al.substring(3,al.length());
			al = al.trim();
		}
		ji = al.indexOf(",");
			//(ji + " PSTBL3 : " + al);

		if (ji > 0){
			getUnion().parseTables(al.substring(ji + 1,al.length()).trim());
			al = al.substring(0,ji).trim();
		}
		if (StringUtil.isRealString(al))
		aliases.put(al,table);
		tok = tok.substring(i + 1,tok.length());
		tok = tok.trim();
		tok2 = tok.toLowerCase();

		if (selectList.size() > 0)
			for (int ct = 0;ct < selectList.size();ct++)
			{
				String se = selectList.elementAt(ct).toString();
				if (se.equals(al))
					selectList.setElementAt("*",ct);
				else if (se.startsWith(al + ".") && StringUtil.isRealString(se.substring(al.length() + 1,se.length()))){
					String cn = se.substring(al.length() + 1,se.length());
					//(se + " SELECTSSS " + cn);
					updateSelectAS(se,cn);
					selectList.setElementAt(cn,ct);
				}
					
			}

			
		}else if (tok2.trim().length() > 0)
		{
		tok = tok.trim();
		String al = tok;
		al = al.trim();
		if (al.toLowerCase().startsWith("as "))
		{
			al = al.substring(3,al.length());
			al = al.trim();
		}
		if (StringUtil.isRealString(al))
		aliases.put(al,table);			
		}
		
		i = tok2.indexOf("where ");
		if (i > -1){
		tok = tok.substring(i + "where ".length(),tok.length());
		tok = tok.trim();
		tok2 = tok.toLowerCase();
		String lc = null;
		//("SEL 1 " + tok);
			while (true){
				
				if (tok.length() < 1)break;
				i = tok2.indexOf(" and ");
				if (i > -1){
		tok2 = tok.substring(0,i);
		tok2 = tok2.trim();
		Criteria cr = parseCriteria(tok2);
		if (lc == null || lc.equals("and")){
		if (!getUnion().addToIncludeList(table,getTAlias(table),getSAlias(cr.getName()),getSAlias(cr.getValueString()),cr))
		includealllist.add(cr);
		}
		else{
		if (!getUnion().addToEitherOrList(table,getTAlias(table),getSAlias(cr.getName()),getSAlias(cr.getValueString()),cr))
		eitheroralllist.add(cr);
		}
		tok = tok.substring(i + " and ".length(),tok.length());
		tok = tok.trim();
		tok2 = tok.toLowerCase();
		lc = "and";

				}

				else if( tok2.indexOf(" or ") > -1){
i = tok2.indexOf(" or ");
		tok2 = tok.substring(0,i);
		tok2 = tok2.trim();
		//eitheroralllist.add(parseCriteria(tok2));
		Criteria cr = parseCriteria(tok2);

		if (lc == null || lc.equals("or"))
		{
		if (!getUnion().addToEitherOrList(table,getTAlias(table),getSAlias(cr.getName()),getSAlias(cr.getValueString()),cr))
		eitheroralllist.add(cr);
		}		else
		{
		if (!getUnion().addToIncludeList(table,getTAlias(table),getSAlias(cr.getName()),getSAlias(cr.getValueString()),cr))
		includealllist.add(cr);
		}					
		tok = tok.substring(i + " or ".length(),tok.length());
		tok = tok.trim();
		tok2 = tok.toLowerCase();
		lc = "or";


				}
				else{
				Criteria cr = parseCriteria(tok);
			
					if (lc == null || lc.equals("and"))
		{
		if (!getUnion().addToIncludeList(table,getTAlias(table),getSAlias(cr.getName()),getSAlias(cr.getValueString()),cr))
		includealllist.add(cr);
		}					else
		{
		if (!getUnion().addToEitherOrList(table,getTAlias(table),getSAlias(cr.getName()),getSAlias(cr.getValueString()),cr))
		eitheroralllist.add(cr);
		}					break;
				}
				
			}
			
		}



		return;
		}
		else if (tok2.startsWith("delete ")){
		action = "delete";
		tok = tok.substring("delete ".length(),tok.length());
		tok = tok.trim();
		tok2 = tok.toLowerCase();

		int si = tok2.indexOf("from ");
		tok = tok.substring(si + "from ".length(),tok.length());
		tok = tok.trim();
		tok2 = tok.toLowerCase();

		int i = tok.indexOf(" ");
				if (i < 0)
		{
			table = tok.substring(0,tok.length());
			setTable(table);
			return;
		}

		table = tok.substring(0,i);
		setTable(table);
		tok = tok.substring(i + 1,tok.length());
		tok = tok.trim();
		tok2 = tok.toLowerCase();
		
		i = tok2.indexOf("where ");
		if (i > -1){
		tok = tok.substring(i + "where ".length(),tok.length());
		tok = tok.trim();
		tok2 = tok.toLowerCase();
			while (true){
				
				if (tok.length() < 1)break;
				i = tok2.indexOf(" and ");
				if (i > -1){
		tok2 = tok.substring(0,i);
		tok2 = tok2.trim();
		Criteria cr = parseCriteria(tok2);
		if (!getUnion().addToIncludeList(table,getTAlias(table),getSAlias(cr.getName()),getSAlias(cr.getValueString()),cr))
			includealllist.add(cr);
		tok = tok.substring(i + " and ".length(),tok.length());
		tok = tok.trim();
		tok2 = tok.toLowerCase();

				}
				else if( tok2.indexOf(" or ") > -1){
				}
				else{
					
		Criteria cr = parseCriteria(tok);
		if (!getUnion().addToIncludeList(table,getTAlias(table),getSAlias(cr.getName()),getSAlias(cr.getValueString()),cr))
			includealllist.add(cr);
								break;
				}
				
			}
			
		}



		return;
		}
		
		
		else if (tok2.startsWith("update ")){

	
		action = "update";
		tok = tok.substring("update ".length(),tok.length());
		tok = tok.trim();
		tok2 = tok.toLowerCase();


		int i = tok.indexOf(" ");
				if (i < 0)
			i = tok.length();

		table = tok.substring(0,i);
		setTable(table);
		tok = tok.substring(i + 1,tok.length());
		tok = tok.trim();
		tok2 = tok.toLowerCase();

		tok = tok.substring("set ".length(),tok.length());
		tok = tok.trim();
		tok2 = tok.toLowerCase();

		
		int si = tok2.indexOf(" where ");
		if (si < 0)
			si = tok2.length();
		String ups = tok.substring(0,si);
		ups = ups.trim();

		//tok = encode(ups);
		EZArrayList uv = new EZArrayList(new StringTokenizer(ups,","));
			String upstr = null;
			for (int ct = 0;ct < uv.size();ct++){
			upstr = uv.elementAt(ct).toString();
			updatelist.add(parseUpdate(upstr));

				
			}
	


si = tok2.indexOf(" where ");
if (si < 0)return;

		tok = tok.substring(si + " where ".length(),tok.length());
		tok = tok.trim();
		tok2 = tok.toLowerCase();

		/*int i = tok.indexOf(" ");
		table = tok.substring(0,i);
		tok = tok.substring(i + 1,tok.length());
		tok = tok.trim();
		tok2 = tok.toLowerCase();*/
		
	//	i = tok2.indexOf("where ");
	//	if (i > -1){
		//tok = tok.substring(i + "where ".length(),tok.length());
		//tok = tok.trim();
		//tok2 = tok.toLowerCase();
			while (true){
				i = tok2.indexOf(" and ");
				if (i > -1){
			tok2 = tok.substring(0,i);
		tok2 = tok2.trim();
		Criteria cr = parseCriteria(tok2);
		if (!getUnion().addToIncludeList(table,getTAlias(table),getSAlias(cr.getName()),getSAlias(cr.getValueString()),cr))
			includealllist.add(cr);
		tok = tok.substring(i + " and ".length(),tok.length());
		tok = tok.trim();
		tok2 = tok.toLowerCase();

				}
				else if( tok2.indexOf(" or ") > -1){
				}
				else{
					
		Criteria cr = parseCriteria(tok);
		if (!getUnion().addToIncludeList(table,getTAlias(table),getSAlias(cr.getName()),getSAlias(cr.getValueString()),cr))
			includealllist.add(cr);
								break;
				}
				
			}
			
	//	}



		return;
		}else if (SpecialCommandParser.parse(this,firstToken)){
			return;
		} else if (tok2.startsWith("show tables")){
			action = "showTables";
			return;
		}
		else if (tok2.startsWith("describe ")){
			action = "describeTable";
			tok2 = tok.substring("describe ".length(),tok2.length()).trim();
			setTable(tok2);
			return;
		}
		else if (tok2.startsWith("getcolumns ")){
			action = "getColumns";
			tok2 = tok.substring("getcolumns ".length(),tok2.length()).trim();
			setTable(tok2);
			return;
		}
		else if (tok2.startsWith("getprimarykeys ")){
			action = "getPrimaryKeys";
			tok2 = tok.substring("getprimarykeys ".length(),tok2.length()).trim();
			setTable(tok2);
			return;
		}//special = true;
		else if (tok2.startsWith("getindex ")){
			action = "getIndex";
			tok2 = tok.substring("getindex ".length(),tok2.length()).trim();
			setTable(tok2);
			special = true;
			return;
		}
		else if (tok2.startsWith("getexportedkeys ")){
			action = "getExportedKeys";
			tok2 = tok.substring("getexportedkeys ".length(),tok2.length()).trim();
			setTable(tok2);
			return;
		}
				else if (tok2.startsWith("gettypeinfo")){
		special = true;
		setAction("getTypeInfo");
		//local = true;
		return;

		}
		else if (tok2.startsWith("getimportedkeys ")){
			action = "getImportedKeys";
			tok2 = tok.substring("getimportedkeys ".length(),tok2.length()).trim();
			setTable(tok2);
			return;
		}
		else if (tok2.startsWith("jiqldescribe ")){
			action = "jiqldescribeTable";
			tok2 = tok.substring("jiqldescribe ".length(),tok2.length()).trim();
			setTable(tok2);
			return;
		}
		ti = tok2.indexOf(" index ");

		if (tok2.startsWith("create ") && ti > 0)
		{
			action = "createIndex";
			return;
		}

		ti = tok2.indexOf(" table ");
		if (tok2.startsWith("create ") && ti > 0){
			action = "createTable";
			tok = tok.substring(ti + " table ".length(),tok.length());
			tok = tok.trim();
			if (tok.toUpperCase().startsWith("IF NOT EXISTS"))
			{
				tok = tok.substring(14,tok.length());
				tok = tok.trim();
				getCreateParser().setIfNotExists(true);
				
			}
			int i = tok.indexOf(" ");
					if (i < 0)
			i = tok.length();

			table = tok.substring(0,i);
			setTable(table);
			if (!AlphaNumeric.isAlphaNumeric(table,"_"))
				throw JGException.get("invalid_table_name", table + " Invalid Create Table name");
			tok = tok.substring(i + 1,tok.length());
			tok = tok.trim();
			//(tok + ":" + table + ":REDBEAN");

			if (tok.startsWith("("))
			{
				tok = tok.substring(1,tok.length());
				tok = tok.trim();

			if (tok.endsWith(")"))
			{
				tok = tok.substring(0,tok.length() - 1);
				tok = tok.trim();
			}
			}
			parseCreateTableParams();
			return;
		}
		else if (tok2.startsWith("drop ") && ti > 0){
			action = "dropTable";
			tok = tok.substring(ti + " table ".length(),tok.length());
			tok = tok.trim();
			int i = tok.indexOf(" ");
					if (i < 0)
			i = tok.length();

			table = tok.substring(0,i);
			setTable(table);
			return;
		}
		else if (tok2.startsWith("set "))
		{
			action = "setOperation";
			return;
		}
		else if (tok2.startsWith("load ")){
			action = "loadTable";
			getLoadTable();
			return;
		}
		else if (tok2.startsWith("insert ")){
			action = "sqlInsert";
			getInsertIntoTable();
			return;
		}
		else if (tok2.startsWith("alter ") && ti > 0){
			parseAlter(ti);
			/*action = "dropTable";
			tok = tok.substring(ti + " table ".length(),tok.length());
			tok = tok.trim();
			int i = tok.indexOf(" ");
					if (i < 0)
			i = tok.length();

			table = tok.substring(0,i);
			return;*/
		}
		ti = tok2.indexOf(" user ");
				if (tok2.startsWith("drop ") && ti > 0)
			{
			action = "dropUser";
			tok = tok.substring(ti + " user ".length(),tok.length());
			tok = tok.trim();

			return;
			}
		else if (tok2.startsWith("create ") && ti > 0)
			{
			/*action = "createUser";
			tok = tok.substring(ti + " user ".length(),tok.length());
			tok = tok.trim();
			//create user DB_NAME identified by PASSWORD role user|admin

			int i = tok.indexOf(" ");
			String nuser = tok.substring(0,i);
			tok = tok.substring(i + 1,tok.length());
			tok = tok.trim();
			//(tok + ":" + nuser + ":create suser");
			 ("nuser",nuser);
			//identified by PASSWORD role user|admin
			//PASSWORD role user|admin

				tok = tok.substring("identified".length(),tok.length());
				tok = tok.trim();
				tok = tok.substring("by".length(),tok.length());
				tok = tok.trim();
				String d = JGProperties.get().getProperty("digest");

			 ("nupassword",tok);
	    	if (StringUtil.isRealString(d) && !d.equals("PLAIN"))
				 ("nupassword",Crypto.digest(tok,d));
			i = tok.indexOf(" ");
			if (i > 0)
			{
	    	if (StringUtil.isRealString(d) && !d.equals("PLAIN"))
				 ("nupassword",Crypto.digest(tok.substring(0,i).trim(),d));
				else
				 ("nupassword",tok.substring(0,i).trim());


				tok = tok.substring(i+1,tok.length());
				tok = tok.trim();
				if (tok.startsWith("role "))
				{
				tok = tok.substring("role ".length(),tok.length());
				tok = tok.trim();
				if (!tok.equalsIgnoreCase("admin") && !tok.equalsIgnoreCase("user"))
					throw JGException.get("invalid_role Invalid Role! Should be admin or user");
				 ("role",tok);
				}


			}
			return;*/
			}

		ti = tok2.indexOf(" into ");
		if (tok2.startsWith("jiqlinsert ") && ti > 0)
			{
			action = "writeTableRow";
			tok = tok.substring(ti + " into ".length(),tok.length());
			tok = tok.trim();
			int i = tok.indexOf(" ");
				int i2 = tok.indexOf("(");
			if (i > i2)
				i = i2;
					if (i < 0)
			i = tok.length();

			table = tok.substring(0,i);
			setTable(table);
			if (i == i2)
			tok = tok.substring(i,tok.length());
			else
			tok = tok.substring(i + 1,tok.length());
			tok = tok.trim();
			boolean checkfl = false;

			if (tok.toLowerCase().startsWith("valjiqlues"))
			{
			checkfl = true;
			StringBuffer sbf = new StringBuffer("(");
			EZArrayList fv = getJiqlTableInfo().getFieldList();
			sbf.append(fv.toDelimitedString(","));
			sbf.append(") values ");
			//tok = tok.substring("values".length(),tok.length());
			//tok = tok.trim();
			tok = sbf.toString() + tok.substring(10,tok.length());

			}
			if (tok.toLowerCase().startsWith("values"))
			{
			/*checkfl = true;
			StringBuffer sbf = new StringBuffer("(");
			EZArrayList fv = getJiqlTableInfo().getFieldList();
			sbf.append(fv.toDelimitedString(","));
			sbf.append(") ");*/
			tok = tok.substring("values".length(),tok.length());
			tok = tok.trim();
			//tok = sbf.toString() + tok;

			}
			//("INSERT 1 " + tok);
			tok = tok.substring(1,tok.length());
			tok = tok.trim();
			//tok = encode(tok);
			i = tok.indexOf(")");
			String tokf = tok.substring(0,i);

			tok = tok.substring(i+ 1,tok.length());
			tok = tok.trim();
			
			Vector v = null;
			if (tok.toLowerCase().startsWith("values"))
			{
			tokf = tokf.trim();
			tok = tok.substring("values".length(),tok.length());
			tok = tok.trim();
			tok = tok.substring(1,tok.length());
			tok = tok.trim();
			tok = tok.substring(0,tok.length() -1);
			tok = tok.trim();
		 	v = new EZArrayList(new StringTokenizer(tokf,","));
		
			}
			else
			{
				tok = tokf;
				v = getJiqlTableInfo().getFieldList();

    	/*Vector r = Gateway.get(getProperties()).describeTable(this);
    	v = new Vector();
    	NameValuePairs row= null;
    	for (int ct = 0;ct < r.size();ct++){
    		row = (NameValuePairs)r.elementAt(ct);
    		v.add(row.getString("Field"));
    	}*/

			}
			


		EZArrayList vv = new EZArrayList(new StringTokenizer(tok,","));
		String n = null;
		String va = null;
		for (int ct = 0;ct < v.size();ct++)
		{
			n = v.elementAt(ct).toString();
			n = n.trim();
			n= StringUtil.getTrimmedValue(n);
			try{
			
			va = vv.elementAt(ct).toString();
			}catch (ArrayIndexOutOfBoundsException e){
			throw JGException.get("missing_column_value",n + " Missing Column Value! ");
			
			}
					//(n + "   TOKO b " + va);

			va = va.trim();
					//(n + "   TOKO c " + va);

			//va = decode(va);
			if (!(va.startsWith("'") && va.endsWith("'")))
				notquoted.put(n,true);
			va = StringUtil.getTrimmedValue(va);
			va = decode(va);
			//(n + "  E " + va);
			hash.put(n,convert(va,n));
		}
		return;

			}

			return;
		}




		jiqlFunction parseFunction(String tok)
				{
				//	rolleruser ( id )
				if (tok == null)return null;
						tok = tok.trim();
					int i = tok.indexOf("(");
					String n = tok.substring(0,i);
					n = n.trim();
					tok = 	tok.substring(i + 1,tok.length() -1);
					tok = tok.trim();
					
					EZArrayList v = new EZArrayList(new StringTokenizer(tok,","));
					jiqlFunction p = new jiqlFunction();
					for (int ct = 0;ct < v.size(); ct++)
					{
						tok = v.elementAt(ct).toString();
						if (tok.indexOf("(") > 0 && tok.indexOf(")") > 0)
							tok = tok.substring(0,tok.indexOf("("));
						p.add(tok);
					}
					p.setName(n);
					return p;
				}




public EZArrayList getNotNulls(){
	return notnulls;
}


public Hashtable getDefaultValues(){
	return defaultValues;
}


	EZArrayList primaryKeys = new EZArrayList();
	EZArrayList notnulls = new EZArrayList();
	Hashtable defaultValues = new Hashtable();

	protected void parseAlter(int ti)throws SQLException{

			tok = tok.substring(ti + " table ".length(),tok.length());
			tok = tok.trim();
			int i = tok.indexOf(" ");
					if (i < 0)
			i = tok.length();

			table = tok.substring(0,i);
			setTable(table);
			tok = tok.substring(i + 1,tok.length());
			tok = tok.trim();
			String tok2 = tok.toLowerCase();
			if (tok2.startsWith("add "))
			{
				tok = tok.substring("add ".length(),tok.length());
				tok = tok.trim();
				tok2 = tok.toLowerCase();
				String references = null;
				if (tok2.startsWith("constraint "))
				{
					tok = tok.substring("constraint ".length(),tok.length());
					tok = tok.trim();
					tok2 = tok.toLowerCase();
					action = "addConstraint";
					
					i = tok.indexOf(" ");
					String cn = tok.substring(0,i);
					cn = cn.trim();
					tok = tok.substring(i + 1,tok.length());
					tok = tok.trim();
					i = tok.indexOf(" ");
					String ty = tok.substring(0,i);
					ty = ty.trim();
					tok = tok.substring(i + 1,tok.length());
					tok = tok.trim();
					if (ty.equalsIgnoreCase("foreign"))
					{
					  i = tok.indexOf(" ");
					  ty = ty + tok.substring(0,i);
					  ty = ty.trim();
					  tok = tok.substring(i + 1,tok.length());
					  if (ty.equalsIgnoreCase("foreignkey"))
					  {
					  	//alter table website add constraint ws_userid_fk foreign key ( userid ) references rolleruser ( id )  ;
					  	tok2 = tok.toLowerCase();
					  	i = tok2.indexOf(" references ");
					  	references = tok.substring(i + " references ".length(),tok.length());
					  	tok = tok.substring(0,i);
						tok = tok.trim();
						references = references.trim();
					  	
					  }
						
					}
					tok = tok.substring(1,tok.length() -1);
					tok = tok.trim();
					EZArrayList v = new EZArrayList(new StringTokenizer(tok,","));
					jConstraint = new jiqlConstraint();
					for (int ct = 0;ct < v.size(); ct++)
					{
						tok = v.elementAt(ct).toString();
						if (tok.indexOf("(") > 0 && tok.indexOf(")") > 0)
							tok = tok.substring(0,tok.indexOf("("));
						jConstraint.add(tok);
					}
					jConstraint.setType(ty);
					jConstraint.setName(cn);
					jiqlFunction jref =  parseFunction(references);
					jConstraint.setReference(jref);
					//(table + " aLTER 5 " +   + ":" + cn + ":" + ty  +":" + jref);
					
				}
				
				else {
					 org.jiql.db.alter.AlterParser.get().parse(tok,this);
				}

	
			}
		
		
	}
	
public  jiqlConstraint getConstraint(){
	return jConstraint;
}
	public String  decode(String s){
		s = StringUtil.replaceSubstring(s,"jiql_replace_comma",",");
		s = StringUtil.replaceSubstring(s,"jiql_replace_openbracket","(");
		s = StringUtil.replaceSubstring(s,"jiql_replace_closebracket",")");
		s = StringUtil.replaceSubstring(s,"jiql_replace_dna","and");
		s = StringUtil.replaceSubstring(s,"jiql_replace_ro","or");
		s = StringUtil.replaceSubstring(s,"jiql_replace_erehw","where");

		s = StringUtil.replaceSubstring(s,"jiql_replace_DNA","AND");
		s = StringUtil.replaceSubstring(s,"jiql_replace_RO","OR");
		s = StringUtil.replaceSubstring(s,"jiql_replace_EREHW","WHERE");
		s = StringUtil.replaceSubstring(s,"jiql_replace_is_null","is null");
		s = StringUtil.replaceSubstring(s,"jiql_replace_not_null","not null");
		s = StringUtil.replaceSubstring(s,"jiql_replace_primary_key","primary key");
		s = StringUtil.replaceSubstring(s,"jiql_replace_NOT_NULL","NOT NULL");
		s = StringUtil.replaceSubstring(s,"jiql_replace_PRIMARY_KEY","PRIMARY KEY");
		s = StringUtil.replaceSubstring(s,"jiql_replace_ob","order by");
		s = StringUtil.replaceSubstring(s,"jiql_replace_OB","ORDER BY");

		s = StringUtil.replaceSubstring(s,"jiql_replace_gb","group by");
		s = StringUtil.replaceSubstring(s,"jiql_replace_GB","GROUP BY");
		
		s = StringUtil.replaceSubstring(s,"jiql_replace_IS_NULL","IS NULL");
		
		StringBuffer sb = new StringBuffer(s);
		sb = SharedMethods.replaceSubstringBuffer(sb,"jiql_replace_auto-increment","auto_increment");
		sb = SharedMethods.replaceSubstringBuffer(sb,"jiql_replace_AUTO-INCREMENT","AUTO_INCREMENT");

		sb = SharedMethods.replaceSubstringBuffer(sb,"jiql_replace_unsiigned","unsigned");
		sb = SharedMethods.replaceSubstringBuffer(sb,"jiql_replace_UNSIIGNED","UNSIGNED");

		sb = SharedMethods.replaceSubstringBuffer(sb,"jiql_replace_limiit","limit");
		sb = SharedMethods.replaceSubstringBuffer(sb,"jiql_replace_LIMIIT","LIMIT");

		sb = SharedMethods.replaceSubstringBuffer(sb,"jiql_replace_inner-join","inner join");
		sb = SharedMethods.replaceSubstringBuffer(sb,"jiql_replace_INNER-JOIN","INNER JOIN");
		sb = SharedMethods.replaceSubstringBuffer(sb,"jiql_replace_o-n","on");
		sb = SharedMethods.replaceSubstringBuffer(sb,"jiql_replace_O-N","ON");

		sb = SharedMethods.replaceSubstringBuffer(sb,"jiql_replace_joi-n","join");
		sb = SharedMethods.replaceSubstringBuffer(sb,"jiql_replace_JOI-N","JOIN");


		sb = SharedMethods.replaceSubstringBuffer(sb,"\\\"","\"");
		sb = SharedMethods.replaceSubstringBuffer(sb,"\\'","'");

		
		return sb.toString();

	}

	protected String  encode(String s){
		String si = s;
		int i = si.indexOf("'");
		if (i < 0)return s;
		String ns = "";
		String smiddle = "";
		while(true){
		
		i = si.indexOf("'");
		while(i > 0 && si.substring(i - 1, i).equals("\\"))
			i = si.indexOf("'", i + 1);
			
		if (i < 0)break;
		ns = ns + si.substring(0,i);
		si = si.substring(i +1,si.length());
		i = si.indexOf("'");
		while(i > 0 && si.substring(i - 1, i).equals("\\"))
			i = si.indexOf("'", i + 1);
		
		if (i < 0)break;
		smiddle = si.substring(0,i);
		smiddle = StringUtil.replaceSubstring(smiddle,"order by","jiql_replace_ob");
		smiddle = StringUtil.replaceSubstring(smiddle,"ORDER BY","jiql_replace_OB");

		smiddle = StringUtil.replaceSubstring(smiddle,"group by","jiql_replace_gb");
		smiddle = StringUtil.replaceSubstring(smiddle,"GROUP BY","jiql_replace_GB");


		smiddle = StringUtil.replaceSubstring(smiddle,",","jiql_replace_comma");
		smiddle = StringUtil.replaceSubstring(smiddle,"(","jiql_replace_openbracket");
		smiddle = StringUtil.replaceSubstring(smiddle,")","jiql_replace_closebracket");
		smiddle = StringUtil.replaceSubstring(smiddle,"and","jiql_replace_dna");
		smiddle = StringUtil.replaceSubstring(smiddle,"or","jiql_replace_ro");
		smiddle = StringUtil.replaceSubstring(smiddle,"where","jiql_replace_erehw");

		smiddle = StringUtil.replaceSubstring(smiddle,"AND","jiql_replace_DNA");
		smiddle = StringUtil.replaceSubstring(smiddle,"OR","jiql_replace_RO");
		smiddle = StringUtil.replaceSubstring(smiddle,"WHERE","jiql_replace_EREHW");
		smiddle = StringUtil.replaceSubstring(smiddle,"is null","jiql_replace_is_null");
		smiddle = StringUtil.replaceSubstring(smiddle,"not null","jiql_replace_not_null");
		smiddle = StringUtil.replaceSubstring(smiddle,"primary key","jiql_replace_primary_key");
		smiddle = StringUtil.replaceSubstring(smiddle,"NOT NULL","jiql_replace_NOT_NULL");
		smiddle = StringUtil.replaceSubstring(smiddle,"PRIMARY KEY","jiql_replace_PRIMARY_KEY");
		smiddle = StringUtil.replaceSubstring(smiddle,"IS NULL","jiql_replace_IS_NULL");		
	
			StringBuffer sb = new StringBuffer(smiddle);
		sb = SharedMethods.replaceSubstringBuffer(sb,"auto_increment","jiql_replace_auto-increment");
		sb = SharedMethods.replaceSubstringBuffer(sb,"AUTO_INCREMENT","jiql_replace_AUTO-INCREMENT");

		sb = SharedMethods.replaceSubstringBuffer(sb,"unsigned","jiql_replace_unsiigned");
		sb = SharedMethods.replaceSubstringBuffer(sb,"UNSIGNED","jiql_replace_UNSIIGNED");
		sb = SharedMethods.replaceSubstringBuffer(sb,"limit","jiql_replace_limiit");
		sb = SharedMethods.replaceSubstringBuffer(sb,"LIMIT","jiql_replace_LIMIIT");

		sb = SharedMethods.replaceSubstringBuffer(sb,"inner join","jiql_replace_inner-join");
		sb = SharedMethods.replaceSubstringBuffer(sb,"INNER JOIN","jiql_replace_INNER-JOIN");
		sb = SharedMethods.replaceSubstringBuffer(sb,"on","jiql_replace_o-n");
		sb = SharedMethods.replaceSubstringBuffer(sb,"ON","jiql_replace_O-N");
		
		sb = SharedMethods.replaceSubstringBuffer(sb,"join","jiql_replace_joi-n");
		sb = SharedMethods.replaceSubstringBuffer(sb,"JOIN","jiql_replace_JOI-N");
	  
	  	//DOES NOTHING
	  	

		si = si.substring(i +1,si.length());
		ns = ns + "'" + sb.toString() + "'";
		if (si.length() < 1)break;
		i = si.indexOf("'");
		if (i < 0){
			ns = ns + si;
			break;
		}

		//si = StringUtil.replaceFirstSubstring(si,"'","jiql_replace_closequote");
		}
		return ns;
		//return s;
		
	}

	public static int spaceIndex(String n){
				int	i = n.indexOf(" ");
			if (i < 0)
				i = n.indexOf("	");
	return i;	
	}
jiqlConstraint jConstraint = null;
	protected void parseCreateTableParams() throws SQLException{
		//tok = encode(tok);
		//("CRE TOK " + tok);
					jiqlTableInfo ti = getJiqlTableInfo(true);

		if (tok.indexOf("prefix=false") > -1){
			prefix=false;
			tok = StringUtil.replaceSubstring(tok,"prefix=false","");
		}
		else
			tok = StringUtil.replaceSubstring(tok,"prefix=true","");

		if (tok.indexOf("tableleafs=false") > -1){
			tableleafs=false;
			tok = StringUtil.replaceSubstring(tok,"tableleafs=false","");
		}
		else
			tok = StringUtil.replaceSubstring(tok,"tableleafs=true","");
		int pvi = tok.indexOf("prefix_value=");
		if (pvi > -1){
			String tok1 = tok.substring(0,pvi);
			String tok2 = tok.substring(pvi + "prefix_value=".length(),tok.length());
			pvi = tok2.indexOf(" ");
			if (pvi > 0){
				prefixValue = tok2.substring(0,pvi).trim();
				tok = tok1 + tok2.substring(pvi + 1,tok2.length());
			}
			else{
				tok = tok1;
				prefixValue=tok2.trim();
			}
			
		}

		EZArrayList v = new EZArrayList(new StringTokenizer(tok,","));
		String n = null;
		String va = null;
		int i = 0;
		Enumeration en = v.elements();
		//for (int ct = 0;ct < v.size();ct++)
		while (en.hasMoreElements())
		{

			n = en.nextElement().toString();
			//v.elementAt(ct).toString();
		n = StringUtil.replaceSubstring(n,"\"","");
		n = StringUtil.replaceSubstring(n,"'","");
		n = StringUtil.replaceSubstring(n,"`","");
			if (getCreateParser().parseParams(n,en))
				continue;
			n = n.trim();
			i = n.indexOf(" ");
			if (i < 0)
				i = n.indexOf("\t");

			//(i + ":" +  n + ":PC1\t:" + va);

			if (i < 0)
				i = n.indexOf("	");
			
			va = n.substring(i + 1,n.length());
			va = va.trim();
			//if (i < 0)
			//("***********" + n + ":PC:" + va);
			n =n.substring(0,i);
			n = n.trim();
			
			StringBuffer tok2b = new StringBuffer (va);
			tok2b = getCreateParser().parse(n,tok2b);
			va = tok2b.toString();
			
			
			i = va.indexOf(" ");
			//(n + " PARS1 " + va);

			if (n.toLowerCase().equals("primary") && va.toLowerCase().startsWith("key"))
			{
				va = va.substring("key".length(),va.length());
				va = va.trim();
				va = va.substring( 1,va.length());
				va = va.trim();
				//("VA VAL  1" + va);
				while (!va.endsWith(")")){
					//ct = ct + 1;
					//va = va + "," + v.elementAt(ct).toString();
					va = va + "," + en.nextElement().toString();

				}
				va = va.trim();

				va = va.substring( 0,va.length() -1);
				va = va.trim();
				primaryKeys = new EZArrayList(new StringTokenizer(va,","),true);
				//("  VAL " +  );


			}
			
			else if (i > 0)
			{
				
				
				tok = va.substring(i,va.length());
				va = va.substring(0,i);
				tok = tok.trim();
				va = va.trim();
	
					//(tok + " PARS2 " + va);

				if (va.endsWith("("))
				{
					i = tok.indexOf(")");
					va = va + tok.substring(0,i + 1);
					tok = tok.substring(i + 1,tok.length());
					tok = tok.trim();
					va = va.trim();
				}
				else if (tok.startsWith("("))
				{
					i = tok.indexOf(")");
					va = va + tok.substring(0,i + 1);
					tok = tok.substring(i + 1,tok.length());
					tok = tok.trim();
					va = va.trim();
				}
					//(tok + " PARS3 " + va);

				String tok2 = tok.toLowerCase();
				i = tok2.indexOf("not ");
				if (i > -1 && tok2.indexOf(" null") > i)
				{
					if (!notnulls.contains(n)){

						notnulls.add(n);
					}
				}

				if (tok2.indexOf(" unique") > i)
				{
					jiqlConstraint jc = new jiqlConstraint();
					jc.add(n);
					jc.setName(n);
					jc.setType(jiqlConstraint.UNIQUE);
					ti.addConstraint(jc);

				}

				i = tok2.indexOf("primary ");
					if (i > -1 && tok2.indexOf(" key") > i)
				{
					n = StringUtil.trimQuotes(new StringBuffer(n)).toString();
					if (!primaryKeys.contains(n))
						primaryKeys.add(n);
				}
				//StringBuffer tok2b = new StringBuffer (tok2);
				//tok2b = getCreateParser().parse(n,tok2b);
				//tok2 = tok2b.toString();
				i = tok2.indexOf("default");
					if (i > -1 )
				{
					//(n + " defaultValues -1 " + tok2);	

					if (tok2.endsWith("default")){
					defaultValues.put(n,"");
					//(n + " defaultValues 0 " + tok2);	

					}
					else
					{
						i = tok2.indexOf("default ");
					if (i > -1 )
				{			
					tok = tok.substring(i + "default ".length(),tok.length());
					tok = tok.trim();
					
					if (tok.startsWith("'"))
					{
					tok = tok.substring(1,tok.length());
					i = tok.indexOf("'");
					tok2 = tok.substring(0,i);
					tok = tok.substring(i + 1,tok.length());
					tok = tok.trim();
					tok2 = decode(tok2);
					defaultValues.put(n,tok2);
					//(n + " defaultValues 1 " + tok2);	
					}
					else{
					
					//i = tok.indexOf(" ");
					//if (i > 0)
					//	tok = tok.substring(0,i);
					tok = tok.trim();
					tok = StringUtil.getTrimmedValue(tok);
					tok = decode(tok);
					defaultValues.put(n,tok);
					//(n + " defaultValues 2 " + tok);	

					}
				}
					}	
				}
				if (!JGUtil.validFieldType(va,this))
								throw JGException.get("invalid_field_type",n + " --> " + va + " Invalid Field Type on Table " + getTable());

					//throw   (n + " --> " + va + " Invalid Field Type on Table " + getTable());
				hash.put(n,convert(va,n));
				ti.addFieldList(n);
			}	
				//not null primary key

			else{
				if (!JGUtil.validFieldType(va,this))
						throw JGException.get("invalid_field_type",n + " --> " + va + " Invalid Field Type on Table " + getTable());

					//throw   (n + " --> " + va + " Invalid Field Type on Table " + getTable());
			
			hash.put(n,convert(va,n));
			ti.addFieldList(n);
			}


		}
	}
	public Hashtable getHash(){
		return hash;
	}

	public Vector getSelectList(){
		return selectList;
	}



}
