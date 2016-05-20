/*
 * BEGIN_HEADER - DO NOT EDIT
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */

/*
 * @(#)XMLCharUtil.java 
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */

package org.glassfish.openesb.databasebc.util;

import java.util.ArrayList;

/**
 * @author M.S.Veerendra
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public final class XMLCharUtil {
	
	public static final char[] specialChars = { '~','!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '+', '|', '}', '{', '"', ':', '?', '>', '<', '`', '-', '=', '\\', ']', '[', '\'', ';', '/', '.',','};
	public static final String[] replaceChars={ "_T_","_E_","_R_","_ASH_","_D_","_P_","_C_","_A_","_S_","_OP_","_CL_","_PL_","_OR_","_CB_","_OB_","_DQ_","_CLN_","_Q_","_GT_","_LT_","_QO_","_MI_","_EQ_","_BS_","_BC_","_BO_","_AP_","_SCL_","_FS_","_DOT_","_COM_"};
	public static final String[] appendCols={"_C","_CN","_FD","_FS","_MF","_M","_MD","_MR","_Q","_RS","_RC","_RT","_R","_RSA","_T","_UC","_W"};
	public static final String[] reservedCols={"Concurrency","CursorName","FetchDirection","FetchSize","MaxFiledSize","MaxRows","MetaData","MoreResults","QueryTimeout","ResultSet","ResultSetConcurrency","ResultSetType","Row","RSAgent","Type","UpdateCount","Warnings"};
	
	public static String getReplacement(char c)
	{
		
	    for (int i=0;i<specialChars.length;i++)
		{
			System.out.println(specialChars[i]+"  " + replaceChars[i]);
		}

		for (int i=0;i<specialChars.length;i++)
		{
			if(specialChars[i]==c)
			{
				return replaceChars[i];
			}
		}

		System.out.println(specialChars.length);
		System.out.println(specialChars.length);
		
		return null;
		
	}

	
	public static String makeValidNCName(String name) {
		StringBuffer nCName = new StringBuffer();
		if (name == null)
			name = "";

		name = name.trim();
		int size = name.length();

		char ncChars[] = name.toCharArray();

		int i = 0;

		for (i = 0; i < size; i++) {

			char ch = ncChars[i];

			if (((i == 0)
					&& !(Character.isJavaIdentifierStart(ch) && (ch != '$')) && !Character
					.isDigit(ch))
					|| ((i > 0) && !(Character.isJavaIdentifierPart(ch) && (ch != '$')))) {
				String replace = getReplacement(ch);
				if (replace != null) {
					nCName.insert(nCName.length(), replace);

				} else {
					nCName.insert(nCName.length(), "_Z_");

				}
			} else {
				nCName.append(ncChars[i]);
			}

		}

		if ((i > 0) && Character.isDigit(nCName.charAt(0))) {
			nCName.insert(0, "X_");
		}
		if ((i > 0) && nCName.charAt(0) == '_') {
			nCName.insert(0, "X");
		}

		return nCName.toString();

	}

	public static boolean isJavaName(String name) {
        if (name == null) {
            name = "";
        }
        name = name.trim();

        int   size      = name.length();
        char  ncChars[] = name.toCharArray();
        int   i         = 0;

        for (i = 1; i < size; i++) {
            char ch = ncChars[i];
            char ch0=ncChars[0];
            if ( !Character.isJavaIdentifierStart(ch0)
            		|| Character.isDigit(ch0)
					|| !Character.isJavaIdentifierPart(ch)){
        					return false;
	 				}
        }
        return true;
	 }
	public static String validColName(String name){

		StringBuffer nCName = new StringBuffer();
		nCName=nCName.insert(0,name);
		for (int k=0;k<reservedCols.length;k++)
		{
			String temp=reservedCols[k];
			if (temp.equals(name))
			{
				nCName = nCName.insert(nCName.length(),appendCols[k]);
				break;
			}
		}
		return nCName.toString();
	}

}

