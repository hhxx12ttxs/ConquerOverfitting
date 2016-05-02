/**
 ******************************************************************************
 *
 * Project        EPA CMS
 * File           DCTM_Utilities.java
 * Description    Documentum related utility functions
 * Created on     August 9, 2001
 * Tab width      3
 *
 ******************************************************************************
 *
 * PVCS Maintained Data
 *
 * Revision       $Revision: 1.1 $
 * Modified on    $Date: 2006/04/06 20:02:05 $
 *
 * Log at EOF
 *
 ******************************************************************************
 */
package com.custom.library.utility;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;
import com.documentum.web.common.*;

import java.io.*;
import java.util.*;

public class DCTM_Utilities {

	// retrieve workitems on selected epa_case performed by from user
	public static String getInstallOwner(IDfSession sess)
	{
		String str = "";
		IDfQuery query = new DfQuery();
		query.setDQL("select r_install_owner from dm_server_config");
		IDfCollection col = null;

		try {
			col = query.execute(sess, DfQuery.DF_READ_QUERY);
			if(col!=null && col.next())
			{
				str = col.getString("r_install_owner");
				if(str == null || str.length() == 0) str = "";
			}
			if(col != null) col.close();
		} catch(Exception ee) {}
		finally { try { if(col != null) col.close(); } catch(Exception ex) {}}
		return str;
	}

        public static String getAltName(IDfSession sess, String s){
            String queryString = "select alt_name from dm_dbo.org_tree where node_name = '" + s + "'";
            String org = "";
            IDfQuery query = new DfQuery();
            query.setDQL(queryString);
            IDfCollection col = null;
            try {
                col = query.execute(sess, DfQuery.DF_READ_QUERY);
		if(col != null && col.next())
		{
                    String str = col.getString("alt_name");
                    if(str != null && str.trim().length() > 0)
                    {
                       org = str;
			}
		}
            }catch(Exception ee) { System.out.println("Error retrieving org_tree info for " + s + ": " + ee.toString()); return null; }
		finally { try {col.close(); } catch(Exception ex) {} }
		return org;
        }
	public static OrgTreeEntry getOrgTreeEntry(IDfSession sess, String strType)
	{
		String s = "";
		try {
			s = sess.getLoginUserName();
			if(strType.equalsIgnoreCase("group"))
			{
				IDfUser user = sess.getUser(s);
				s = user.getUserGroupName();
				if(s.endsWith("group")) s = s.substring(0, s.lastIndexOf(" "));
				s = s.trim();
			}
		}catch(Exception ee) { System.out.println("Error retrieving user info: " + ee.toString()); return null; }

		StringBuffer sb = new StringBuffer("SELECT node_name, node_type, parent_node, node_level, alt_name, scan_id, is_active, ro_groups ");
		sb.append("FROM dm_dbo.org_tree ");
		sb.append("WHERE LOWER(node_name)='"+Utilities.getOracleInsertText(s.toLowerCase())+"'");

		IDfQuery query = new DfQuery();
		query.setDQL(sb.toString());
		sb.delete(0, sb.length());
		IDfCollection col = null;

		OrgTreeEntry org = new OrgTreeEntry();
		try {
			col = query.execute(sess, DfQuery.DF_READ_QUERY);
			if(col != null && col.next())
			{
				String str = col.getString("node_name");
				if(str != null && str.trim().length() > 0)
				{
					org.setNodeName(str);
					org.setNodeType(getString(col.getString("node_type")));
					org.setParentNode(getString(col.getString("parent_node")));
					org.setNodeLevel(col.getInt("node_level"));
					org.setAltName(getString(col.getString("alt_name")));
					org.setScanId(getString(col.getString("scan_id")));
					org.setIsActive(col.getInt("is_active"));
					org.setROGroups(getString(col.getString("ro_groups")));
				}
			}
		} catch(Exception ee) { System.out.println("Error retrieving org_tree info for " + s + ": " + ee.toString()); return null; }
		finally { try {col.close(); } catch(Exception ex) {} }
		return org;
	}

	public static OrgTreeEntry getOrgTreeEntry(IDfSession sess, String strNodeName, String strType)
	{
		StringBuffer sb = new StringBuffer("SELECT node_name, node_type, parent_node, node_level, alt_name, scan_id, is_active, ro_groups ");
		sb.append("FROM dm_dbo.org_tree ");
		sb.append("WHERE LOWER(node_name)='"+Utilities.getOracleInsertText(strNodeName.toLowerCase())+"' ");
		if(strType.length() > 0)
		{
			sb.append(" AND LOWER(node_type) ");
			if(strType.equalsIgnoreCase("group")) sb.append("=");
			else sb.append("<>");
			sb.append("'group'");
		}

		IDfQuery query = new DfQuery();
		query.setDQL(sb.toString());
		System.out.println(query.getDQL());
		//System.out.println("null session:" + (sess==null));
		sb.delete(0, sb.length());
		IDfCollection col = null;

		OrgTreeEntry org = new OrgTreeEntry();
		try {
			col = query.execute(sess, DfQuery.DF_READ_QUERY);
			if(col != null && col.next())
			{
				String str = col.getString("node_name");
				if(str != null && str.trim().length() > 0)
				{
					org.setNodeName(str);
					org.setNodeType(getString(col.getString("node_type")));
					org.setParentNode(getString(col.getString("parent_node")));
					//org.setOrgLevel(getString(col.getString("org_level")));
					org.setNodeLevel(col.getInt("node_level"));
					org.setAltName(getString(col.getString("alt_name")));
					org.setScanId(getString(col.getString("scan_id")));
					org.setIsActive(col.getInt("is_active"));
					org.setROGroups(getString(col.getString("ro_groups")));
				}
			}
		} catch(Exception ee) { System.out.println("Error retrieving org_tree info for " + strNodeName + ": " + ee.toString()); return null; }
		finally { try {col.close(); } catch(Exception ex) {} }
		return org;
	}

	// retrieve information in org_tree table with the input where clause and order by
	// clause. Create an OrgTreeEntry object from sql result and add it to hashtable
	public static Hashtable getOrgTreeInfo(IDfSession sess, String strKeyField, String strWhere, String strOrder)
	{
		return getOrgTreeInfo(sess, strKeyField, "", strWhere, strOrder);
	}

	public static Hashtable getOrgTreeInfo(IDfSession sess, String strKeyField, String strKeyCase, String strWhere, String strOrder)
	{
		String str = "SELECT node_name, node_type, parent_node, node_level, alt_name, scan_id, is_active, ro_groups FROM dm_dbo.org_tree " + strWhere + strOrder;
		IDfQuery query = new DfQuery();
		query.setDQL(str);
		IDfCollection col = null;

		Hashtable hash = new Hashtable();

		try {
			col = query.execute(sess, DfQuery.DF_READ_QUERY);
			while(col != null && col.next())
			{
				OrgTreeEntry org = new OrgTreeEntry();
				str = col.getString("node_name");
				if(str != null && str.trim().length() > 0)
				{
					org.setNodeName(str);
					org.setNodeType(getString(col.getString("node_type")));
					org.setParentNode(getString(col.getString("parent_node")));
					org.setNodeLevel(col.getInt("node_level"));
					org.setAltName(getString(col.getString("alt_name")));
					org.setScanId(getString(col.getString("scan_id")));
					org.setIsActive(col.getInt("is_active"));
					org.setROGroups(getString(col.getString("ro_groups")));

					str = getString(col.getString(strKeyField));
					if(strKeyCase.equalsIgnoreCase("upper")) str = str.toUpperCase();
					else if(strKeyCase.equalsIgnoreCase("lower")) str = str.toLowerCase();
					hash.put(str, org);
				}
			}
			if(col != null) col.close();
		} catch(Exception ee) { System.out.println("FAILURE DURING RETRIEING ORG_TREE INFO: " + ee.toString()); }
		finally {
			try { col.close(); } catch(Exception ex){}
		}
		return hash;
	}

	// retrieve information in org_tree table with the input where clause and order by
	// clause. Create an OrgTreeEntry object from sql result and add it to hashtable
	public static ArrayList getOrgTreeInfo(IDfSession sess, String strWhere, String strOrder)
	{
		String str = "SELECT node_name, node_type, parent_node, node_level, alt_name, scan_id, is_active, ro_groups FROM dm_dbo.org_tree " + strWhere + strOrder;
		//System.out.println(str);
		IDfQuery query = new DfQuery();
		query.setDQL(str);
		IDfCollection col = null;

		ArrayList list = new ArrayList();
		try {
			col = query.execute(sess, DfQuery.DF_READ_QUERY);
			while(col != null && col.next())
			{
				OrgTreeEntry org = new OrgTreeEntry();
				str = col.getString("node_name");
				if(str != null && str.trim().length() > 0)
				{
					org.setNodeName(str);
					org.setNodeType(getString(col.getString("node_type")));
					org.setParentNode(getString(col.getString("parent_node")));
					org.setNodeLevel(col.getInt("node_level"));
					org.setAltName(getString(col.getString("alt_name")));
					org.setScanId(getString(col.getString("scan_id")));
					org.setIsActive(col.getInt("is_active"));
					org.setROGroups(getString(col.getString("ro_groups")));
					list.add(org);
				}
			}
			if(col != null) col.close();
		} catch(Exception ee) { System.out.println("FAILURE DURING RETRIEING ORG_TREE INFO: " + ee.toString()); }
		finally {
			try { col.close(); } catch(Exception ex){}
		}
		return list;
	}

	public static String doesNodeExist(IDfSession sess,String strName)
	{
		String str = "SELECT node_name FROM dm_dbo.org_tree WHERE UPPER(node_name)='"+Utilities.getOracleInsertText(strName.toUpperCase())+"'";
		IDfQuery query = new DfQuery();
		query.setDQL(str);
		IDfCollection col = null;

		// return value
		String strNodeName = "";

		try {
			col = query.execute(sess, IDfQuery.DF_READ_QUERY);
			while(col!=null&&col.next())
			{
				str = col.getString("node_name");
				if(str!=null&&str.length()>0)
				{
					strNodeName = str;
					break;
				}
			}
			col.close();
		} catch(Exception ee) {
			System.out.println("Error validate new org name: "+ee.toString());
			return null;
		} finally {
			try { if(col!=null) col.close(); } catch(Exception ex) {}
		}
		return strNodeName;
	}

	private static String getString(String str)
	{
		if(str == null || str.length() == 0) return "";
		return str.trim();
	}

	// add trace to epa_case
	public static void addTrace(IDfSysObject doc, Properties prop) throws Exception
	{
		IDfSession sess = doc.getSession();

		String strTrace = prop.getProperty("trace");
		String strTracePerformer = prop.getProperty("trace_performer");
		IDfTime time = new DfTime();

		String strTraceOrg = prop.getProperty("trace_org");
		if(strTraceOrg == null || strTraceOrg.length() == 0)
		{
			IDfUser user = sess.getUser(strTracePerformer);
			if(!user.isGroup()) strTraceOrg = user.getUserGroupName();
		}
		if(strTraceOrg.endsWith(" group")) strTraceOrg = strTraceOrg.substring(0, strTraceOrg.lastIndexOf(" "));
		strTraceOrg = strTraceOrg.toUpperCase().trim();

		doc.appendString("trace_performer", strTracePerformer);
		doc.appendString("trace_org", strTraceOrg);
		doc.appendTime("trace_time", new DfTime());
		doc.appendString("trace", strTrace);
		doc.save();
	}

	public static String getLoginGroup(IDfSession sess) throws Exception
	{
		IDfUser user = sess.getUser(sess.getLoginUserName());
		String strGroup = user.getUserGroupName();
		if(strGroup != null)
		{
			if(strGroup.endsWith(" group")) strGroup = strGroup.substring(0, strGroup.lastIndexOf(" "));
			strGroup = strGroup.trim().toUpperCase();
		}
		return strGroup;
	}

	private static int getMax(int n1, int n2)
	{
		if(n1 > n2) return n1;
		return n2;
	}

	private static int getMax(int n1, int n2, int n3)
	{
		return getMax(getMax(n1,n2), n3);
	}

	public static StringBuffer getContent(IDfSession sess, String strId, String strBreak) throws Exception
	{
		// use object id to obtain dm_note object
		IDfSysObject note = (IDfSysObject)sess.getObject(new DfId(strId));

		// read in dm_note content and add it to string buffer
		ByteArrayInputStream is = note.getContent();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String strLine = "";
		StringBuffer sb = new StringBuffer();
		while((strLine=br.readLine()) != null)
		{
			if(strLine.trim().length() > 0)
			{
				sb.append(strLine);
				sb.append(strBreak);
			}
		}
		br.close();
		return sb;
	}

	// retrieve constituent organization
	public static String getConstituentOrganization(IDfSession sess, String strID)
	{
		String str = "SELECT organization FROM constituent WHERE r_object_id='"+strID+"'";
		IDfQuery query = new DfQuery();
		query.setDQL(str);
		str = "";
		IDfCollection col = null;

		try {
			col = query.execute(sess, DfQuery.DF_READ_QUERY);
			if(col != null && col.next())
			{
				str = col.getString("organization");
				if(str == null || str.trim().length() == 0) str = "";
			}
			col.close();
		} catch(Exception ee) {}
		finally { try { if(col != null) col.close(); } catch(Exception ex) {}}
		return str;
	}

	public static String getCitizenAddress(IDfSession sess, String strId, String strDelim) throws Exception
	{
		StringBuffer sb = new StringBuffer();
		IDfSysObject con = (IDfSysObject)sess.getObject(new DfId(strId));

		String str = con.getString("street_address_1");
		if(str!=null && str.trim().length()>0) sb.append(str + ", ");
		str = con.getString("city");
		if(str!=null && str.trim().length()>0) sb.append(strDelim+str + ", ");
		str = con.getString("states");
		if(str!=null && str.trim().length()>0) sb.append(strDelim+str + " ");
		str = con.getString("zip");
		if(str!=null && str.trim().length()>0) sb.append(str);
		str = con.getString("country");
		if(str!=null && str.trim().length()>0)
		{
			if(strDelim.length() > 0) sb.append(strDelim);
			else sb.append(", ");
			sb.append(str);
		}
		return sb.toString();
	}

	public static String doMethod(IDfSession sess, String strMethodName, String strArgument) throws Exception
	{
		System.out.println("Running method " + strMethodName+"...");
		String strMessage = "";

		// use API to apply method, set save_results to true in case error happends
		StringBuffer sb = new StringBuffer();
		sb.append("NULL,DO_METHOD,METHOD,S,"+strMethodName+",SAVE_RESULTS,B,T,");
		sb.append("ARGUMENTS,S, ");
		sb.append(strArgument);

		System.out.println(sb.toString());
		String strResult = sess.apiGet("apply", sb.toString());

		// process result, use "next,c,q0" to process
		if(strResult != null && strResult.length() > 0)
		{
			boolean b = sess.apiExec("next", strResult);
			if(b)
			{
				// "get,c,q0,method_return_val" to retrieve return value from method
				String str = sess.apiGet("get", strResult + ",method_return_val");
				if(str != null && !str.equals("0"))
				{
					// "get,c,q0,result_doc_id" to get the method's saved result document
					str = sess.apiGet("get", strResult + ",result_doc_id");
					strMessage = "METHOD "+strMethodName+" FAILED, RESULT_DOCUMENT_ID=" + str;
				}
			}
		}
		sess.apiExec("close", strResult);

		return strMessage;
	}

	public static String getArguments(ArgumentList args)
	{
		if(args == null) return "Null ArgumentList.";
		if(args.isEmpty()) return "ArgumentList is empty.";

		Iterator it = args.nameIterator();
		StringBuffer sb = new StringBuffer();
		while(it.hasNext())
		{
			String s = (String)it.next();
			sb.append(s + "=" + args.get(s) + "\n");
		}
		return sb.toString();
	}

	public static String executeQuery(IDfSession sess, String strQuery)
	{
		if(strQuery == null || strQuery.length() == 0) return "";
		IDfQuery query = new DfQuery();
		query.setDQL(strQuery);
		IDfCollection col = null;

		try {
			col = query.execute(sess, IDfQuery.DF_EXEC_QUERY);
			col.close();
		} catch(Exception ee)
		{
			return ee.toString();
		}
		finally {
			try{ if(col != null) col.close(); } catch(Exception ex) {}
		}
		return "";
	}


	// retrieve process object id
	public static String getProcessId(IDfSession sess, String strProcess)
	{
		IDfQuery query = new DfQuery();
		query.setDQL("SELECT r_object_id FROM dm_process WHERE object_name='"+strProcess+"'");
		strProcess = "";
		IDfCollection col = null;

		try {
			col = query.execute(sess, IDfQuery.DF_READ_QUERY);
			while(col != null && col.next())
			{
				strProcess = col.getString("r_object_id");
				if(strProcess != null && strProcess.trim().length() > 0) break;
			}
			col.close();
		} catch(Exception ee) {
			System.out.println("Error retrieving process id for "+strProcess+": " + ee.toString());
		}
		finally { try { if(col != null) col.close(); } catch(Exception ex){} }
		return strProcess;
	}

	// time difference between time 1 and 2
	public static long getDateDiff(IDfTime time1, IDfTime time2)
	{
		long l1 = 0;
		long l2 = 0;

		if(time1!=null && !time1.isNullDate()) l1 = time1.getDate().getTime();
		if(time2!=null && !time2.isNullDate()) l2 = time2.getDate().getTime();

		long milsecperday = 1000 * 24 * 60 * 60;
		return (l2 - l1)/milsecperday;
	}
}
