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
 * @(#)AttributeBasedWindow.java
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */

package com.sun.jbi.engine.iep.core.derby;

import com.sun.jbi.engine.iep.core.runtime.operator.OperatorConstants;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author Bing Lu
 */
public class AttributeBasedWindow implements OperatorConstants {
    public static void operate(String sName, String rName, String colNames, String attribute, String attributeType, double size, Timestamp ts0, Timestamp ts1) throws Exception {
        Connection con = null;
        PreparedStatement q0 = null, q1 = null;
        ResultSet rs0 = null, rs1 = null;
        PreparedStatement i0 = null, i1 = null;
        try {
            con = DriverManager.getConnection("jdbc:default:connection");
            String[] cols = Util.getTokens(colNames, DELIM);
            
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT ");
            for (int i = 0; i < cols.length; i++) {
                sb.append(cols[i]);
                sb.append(",");
            }
            sb.append(COL_SEQID + "," + COL_TIMESTAMP + " FROM ");
            sb.append(sName + " u1");
            sb.append(" WHERE ? < u1." + COL_TIMESTAMP + " AND u1." + COL_TIMESTAMP + " <= ? AND ");
            sb.append("NOT EXISTS (SELECT 'x' FROM " + sName + " u2");
            sb.append(" WHERE u1." + COL_TIMESTAMP + " = u2." + COL_TIMESTAMP + " AND ");
            if (attributeType.equals(SQL_TYPE_TIMESTAMP)) {
                sb.append(size + " <= DIFF_IN_MILLISECONDS(u2." + attribute + ", u1." + attribute + "))");
            } else if (attributeType.equals(SQL_TYPE_DATE)) {
                sb.append(size + " <= DIFF_IN_DAYS(u2." + attribute + ", u1." + attribute + "))");
            } else {
                sb.append(size + " <= (u2." + attribute + " - u1." + attribute + "))");
            }
            String sqlStr = sb.toString();
            q0 = con.prepareStatement(sqlStr);
            q0.setTimestamp(1, ts0);
            q0.setTimestamp(2, ts1);
            rs0 = q0.executeQuery();

            sb = new StringBuffer();
            int colTotal = cols.length + 2;
            sb.append("INSERT INTO " + rName + " VALUES (");
            for (int i = 0; i < colTotal; i++) {
                sb.append("?,");
            }
            sb.append("'+')");
            sqlStr = sb.toString();
            i0 = con.prepareStatement(sqlStr);
            while (rs0.next()) {
                for (int i = 1; i <= colTotal; i++) {
                    i0.setObject(i, rs0.getObject(i));
                }
                i0.addBatch();
            }
            i0.executeBatch();

            sb = new StringBuffer();
            sb.append("SELECT ");
            for (String col : cols) {
                sb.append("t1." + col + ", ");
            }
            sb.append("t1." + COL_SEQID + ", MIN(t2." + COL_TIMESTAMP + ") FROM " + rName + " t1, " + rName + " t2");
            sb.append(" WHERE t1." + COL_TAG + " = '+' AND");
            sb.append(" t2." + COL_TAG + " = '+' AND ");
            sb.append("? < t2." + COL_TIMESTAMP + " AND t2." + COL_TIMESTAMP + " <= ? AND ");
            if (attributeType.equals(SQL_TYPE_TIMESTAMP)) {
                sb.append(size + " <= DIFF_IN_MILLISECONDS(t2." + attribute + ", t1." + attribute + ") ");
            } else if (attributeType.equals(SQL_TYPE_DATE)) {
                sb.append(size + " <= DIFF_IN_DAYS(t2." + attribute + ", t1." + attribute + ") ");
            } else {
                sb.append(size + " <= (t2." + attribute + " - t1." + attribute + ") ");
            }    
            sb.append(" GROUP BY ");
            for (String col : cols) {
                sb.append("t1." + col + ", ");
            }
            sb.append("t1." + COL_SEQID);
            sb.append(" EXCEPT ");
            sb.append("SELECT ");
            for (int i = 0; i < cols.length; i++) {
                sb.append("t1." + cols[i] + ",");
            }
            sb.append("t1." + COL_SEQID + ", MIN(t2." + COL_TIMESTAMP + ") FROM " + rName + " t1, " + rName + " t2");
            sb.append(" WHERE t1." + COL_TAG + " = '-' AND");
            sb.append(" t2." + COL_TAG + " = '+' AND ");
            sb.append("? < t2." + COL_TIMESTAMP + " AND t2." + COL_TIMESTAMP + " <= ? AND ");
            if (attributeType.equals(SQL_TYPE_TIMESTAMP)) {
                sb.append(size + " <= DIFF_IN_MILLISECONDS(t2." + attribute + ", t1." + attribute + ") ");
            } else if (attributeType.equals(SQL_TYPE_DATE)) {
                sb.append(size + " <= DIFF_IN_DAYS(t2." + attribute + ", t1." + attribute + ") ");
            } else {
                sb.append(size + " <= (t2." + attribute + " - t1." + attribute + ") ");
            }    
            sb.append(" GROUP BY ");
            for (String col : cols) {
                sb.append("t1." + col + ",");
            }
            sb.append("t1." + COL_SEQID);
            sqlStr = sb.toString();
            q1 = con.prepareStatement(sqlStr);
            q1.setTimestamp(1, ts0);
            q1.setTimestamp(2, ts1);
            q1.setTimestamp(3, ts0);
            q1.setTimestamp(4, ts1);
            rs1 = q1.executeQuery();
            
            sb = new StringBuffer();
            sb.append("INSERT INTO " + rName + " VALUES (");
            for (int i = 0; i < colTotal; i++) {
                sb.append("?,");
            }
            sb.append("'-')");
            sqlStr = sb.toString();
            i1 = con.prepareStatement(sqlStr);
            while (rs1.next()) {
                for (int i = 1; i <= colTotal; i++) {
                    i1.setObject(i, rs1.getObject(i));
                }
                i1.addBatch();
            }
            i1.executeBatch();
        } catch (Exception e) {
            throw e;
        } finally {
            Util.close(q0);
            Util.close(q1);
            Util.close(rs0);
            Util.close(rs1);
            Util.close(i0);
            Util.close(i1);
            Util.close(con);
        }
    }
}

