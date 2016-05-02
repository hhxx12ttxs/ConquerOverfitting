/*
 * Copyright 2003-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.rowset;

import java.sql.*;
import javax.sql.*;
import javax.naming.*;
import java.io.*;
import java.math.*;
import java.util.*;

import javax.sql.rowset.*;

/**
 * The standard implementation of the <code>JoinRowSet</code>
 * interface providing an SQL <code>JOIN</code> between <code>RowSet</code>
 * objects.
 * <P>
 * The implementation provides an ANSI-style <code>JOIN</code> providing an
 * inner join between two tables. Any unmatched rows in either table of the
 * join are  discarded.
 * <p>
 * Typically, a <code>JoinRowSet</code> implementation is leveraged by
 * <code>RowSet</code> instances that are in a disconnected environment and
 * thus do not have the luxury of an open connection to the data source to
 * establish logical relationships between themselves. In other words, it is
 * largely <code>CachedRowSet</code> objects and implementations derived from
 * the <code>CachedRowSet</code> interface that will use the <code>JoinRowSetImpl</code>
 * implementation.
 *
 * @author Amit Handa, Jonathan Bruce
 */
public class JoinRowSetImpl extends WebRowSetImpl implements JoinRowSet {
    /**
     * A <code>Vector</code> object that contains the <code>RowSet</code> objects
     * that have been added to this <code>JoinRowSet</code> object.
         */
    private Vector vecRowSetsInJOIN;

    /**
     * The <code>CachedRowSet</code> object that encapsulates this
     * <code>JoinRowSet</code> object.
     * When <code>RowSet</code> objects are added to this <code>JoinRowSet</code>
     * object, they are also added to <i>crsInternal</i> to form the same kind of
     * SQL <code>JOIN</code>.  As a result, methods for making updates to this
     * <code>JoinRowSet</code> object can use <i>crsInternal</i> methods in their
     * implementations.
     */
    private CachedRowSetImpl crsInternal;

    /**
     * A <code>Vector</code> object containing the types of join that have been set
     * for this <code>JoinRowSet</code> object.
     * The last join type set forms the basis of succeeding joins.
     */
    private Vector vecJoinType;

    /**
     * A <code>Vector</code> object containing the names of all the tables entering
     * the join.
     */
    private Vector vecTableNames;

    /**
     * An <code>int</code> that indicates the column index of the match column.
     */
    private int iMatchKey;

    /**
     * A <code>String</code> object that stores the name of the match column.
     */
    private String strMatchKey ;

    /**
     * An array of <code>boolean</code> values indicating the types of joins supported
     * by this <code>JoinRowSet</code> implementation.
     */
    boolean[] supportedJOINs;

    /**
     * The <code>WebRowSet</code> object that encapsulates this <code>JoinRowSet</code>
     * object. This <code>WebRowSet</code> object allows this <code>JoinRowSet</code>
     * object to leverage the properties and methods of a <code>WebRowSet</code>
     * object.
     */
    private WebRowSet wrs;


    /**
     * Constructor for <code>JoinRowSetImpl</code> class. Configures various internal data
     * structures to provide mechanisms required for <code>JoinRowSet</code> interface
     * implementation.
     *
     * @throws SQLException if an error occurs in instantiating an instance of
     * <code>JoinRowSetImpl</code>
     */
    public JoinRowSetImpl() throws SQLException {

        vecRowSetsInJOIN = new Vector();
        crsInternal = new CachedRowSetImpl();
        vecJoinType = new Vector();
        vecTableNames = new Vector();
        iMatchKey = -1;
        strMatchKey = null;
        supportedJOINs =
              new boolean[] {false, true, false, false, false};

    }

    /**
     * Adds the given <code>RowSet</code> object to this
     * <code>JoinRowSet</code> object.  If this
     * rowset is the first to be added to the <code>JoinRowSet</code>
     * object, it forms the basis for the <code>JOIN</code>
     * relationships to be formed.
     * <p>
     * This method should be used when the given <code>RowSet</code> object
     * already has a match column set.
     *
     * @param rowset the <code>RowSet</code> object that implements the
     *         <code>Joinable</code> interface and is to be added
     *         to this <code>JoinRowSet</code> object
     * @throws SQLException if an empty <code>RowSet</code> is added to the to the
     *         <code>JoinRowSet</code>; if a match column is not set; or if an
     *         additional <code>RowSet</code> violates the active <code>JOIN</code>
     * @see CachedRowSet#setMatchColumn
     */
    public void addRowSet(Joinable rowset) throws SQLException {
        boolean boolColId, boolColName;

        boolColId = false;
        boolColName = false;
        CachedRowSetImpl cRowset;

        if(!(rowset instanceof RowSet)) {
            throw new SQLException(resBundle.handleGetObject("joinrowsetimpl.notinstance").toString());
        }

        if(rowset instanceof JdbcRowSetImpl ) {
            cRowset = new CachedRowSetImpl();
            cRowset.populate((RowSet)rowset);
            if(cRowset.size() == 0){
                throw new SQLException(resBundle.handleGetObject("joinrowsetimpl.emptyrowset").toString());
            }


            try {
                int matchColumnCount = 0;
                for(int i=0; i< rowset.getMatchColumnIndexes().length; i++) {
                    if(rowset.getMatchColumnIndexes()[i] != -1)
                        ++ matchColumnCount;
                    else
                        break;
                }
                int[] pCol = new int[matchColumnCount];
                for(int i=0; i<matchColumnCount; i++)
                   pCol[i] = rowset.getMatchColumnIndexes()[i];
                cRowset.setMatchColumn(pCol);
            } catch(SQLException sqle) {

            }

        } else {
             cRowset = (CachedRowSetImpl)rowset;
             if(cRowset.size() == 0){
                 throw new SQLException(resBundle.handleGetObject("joinrowsetimpl.emptyrowset").toString());
             }
        }

        // Either column id or column name will be set
        // If both not set throw exception.

        try {
             iMatchKey = (cRowset.getMatchColumnIndexes())[0];
        } catch(SQLException sqle) {
           //if not set catch the exception but do nothing now.
             boolColId = true;
        }

        try {
             strMatchKey = (cRowset.getMatchColumnNames())[0];
        } catch(SQLException sqle) {
           //if not set catch the exception but do nothing now.
           boolColName = true;
        }

        if(boolColId && boolColName) {
           // neither setter methods have been used to set
           throw new SQLException(resBundle.handleGetObject("joinrowsetimpl.matchnotset").toString());
        } else {
           //if(boolColId || boolColName)
           // either of the setter methods have been set.
           if(boolColId){
              //
              ArrayList indices = new ArrayList();
              for(int i=0;i<cRowset.getMatchColumnNames().length;i++) {
                  if( (strMatchKey = (cRowset.getMatchColumnNames())[i]) != null) {
                      iMatchKey = cRowset.findColumn(strMatchKey);
                      indices.add(iMatchKey);
                  }
                  else
                      break;
              }
              int[] indexes = new int[indices.size()];
              for(int i=0; i<indices.size();i++)
                  indexes[i] = ((Integer)indices.get(i)).intValue();
              cRowset.setMatchColumn(indexes);
              // Set the match column here because join will be
              // based on columnId,
              // (nested for loop in initJOIN() checks for equality
              //  based on columnIndex)
           } else {
              //do nothing, iMatchKey is set.
           }
           // Now both iMatchKey and strMatchKey have been set pointing
           // to the same column
        }

        // Till first rowset setJoinType may not be set because
        // default type is JoinRowSet.INNER_JOIN which should
        // be set and for subsequent additions of rowset, if not set
        // keep on adding join type as JoinRowSet.INNER_JOIN
        // to vecJoinType.

        initJOIN(cRowset);
    }

    /**
     * Adds the given <code>RowSet</code> object to the <code>JOIN</code> relation
     * and sets the designated column as the match column.
     * If the given <code>RowSet</code>
     * object is the first to be added to this <code>JoinRowSet</code>
     * object, it forms the basis of the <code>JOIN</code> relationship to be formed
     * when other <code>RowSet</code> objects are added .
     * <P>
     * This method should be used when the given <code>RowSet</code> object
     * does not already have a match column set.
     *
     * @param rowset a <code>RowSet</code> object to be added to
     *         the <code>JOIN</code> relation; must implement the <code>Joinable</code>
     *         interface
     * @param columnIdx an <code>int</code> giving the index of the column to be set as
     *         the match column
     * @throws SQLException if (1) an empty <code>RowSet</code> object is added to this
     *         <code>JoinRowSet</code> object, (2) a match column has not been set,
     *         or (3) the <code>RowSet</code> object being added violates the active
     *         <code>JOIN</code>
     * @see CachedRowSet#unsetMatchColumn
     */
    public void addRowSet(RowSet rowset, int columnIdx) throws SQLException {
        //passing the rowset as well as the columnIdx to form the joinrowset.

        ((CachedRowSetImpl)rowset).setMatchColumn(columnIdx);

        addRowSet((Joinable)rowset);
    }

    /**
     * Adds the given <code>RowSet</code> object to the <code>JOIN</code> relationship
     * and sets the designated column as the match column. If the given
     * <code>RowSet</code>
     * object is the first to be added to this <code>JoinRowSet</code>
     * object, it forms the basis of the <code>JOIN</code> relationship to be formed
     * when other <code>RowSet</code> objects are added .
     * <P>
     * This method should be used when the given <code>RowSet</code> object
     * does not already have a match column set.
     *
     * @param rowset a <code>RowSet</code> object to be added to
     *         the <code>JOIN</code> relation
     * @param columnName a <code>String</code> object giving the name of the column
     *        to be set as the match column; must implement the <code>Joinable</code>
     *        interface
     * @throws SQLException if (1) an empty <code>RowSet</code> object is added to this
     *         <code>JoinRowSet</code> object, (2) a match column has not been set,
     *         or (3) the <code>RowSet</code> object being added violates the active
     *         <code>JOIN</code>
     */
    public void addRowSet(RowSet rowset, String columnName) throws SQLException {
        //passing the rowset as well as the columnIdx to form the joinrowset.
        ((CachedRowSetImpl)rowset).setMatchColumn(columnName);
        addRowSet((Joinable)rowset);
    }

    /**
     * Adds the given <code>RowSet</code> objects to the <code>JOIN</code> relationship
     * and sets the designated columns as the match columns. If the first
     * <code>RowSet</code> object in the array of <code>RowSet</code> objects
     * is the first to be added to this <code>JoinRowSet</code>
     * object, it forms the basis of the <code>JOIN</code> relationship to be formed
     * when other <code>RowSet</code> objects are added.
     * <P>
     * The first <code>int</code>
     * in <i>columnIdx</i> is used to set the match column for the first
     * <code>RowSet</code> object in <i>rowset</i>, the second <code>int</code>
     * in <i>columnIdx</i> is used to set the match column for the second
     * <code>RowSet</code> object in <i>rowset</i>, and so on.
     * <P>
     * This method should be used when the given <code>RowSet</code> objects
     * do not already have match columns set.
     *
     * @param rowset an array of <code>RowSet</code> objects to be added to
     *         the <code>JOIN</code> relation; each <code>RowSet</code> object must
     *         implement the <code>Joinable</code> interface
     * @param columnIdx an array of <code>int</code> values designating the columns
     *        to be set as the
     *        match columns for the <code>RowSet</code> objects in <i>rowset</i>
     * @throws SQLException if the number of <code>RowSet</code> objects in
     *         <i>rowset</i> is not equal to the number of <code>int</code> values
     *         in <i>columnIdx</i>
     */
    public void addRowSet(RowSet[] rowset,
                          int[] columnIdx) throws SQLException {
    //validate if length of rowset array is same as length of int array.
     if(rowset.length != columnIdx.length) {
        throw new SQLException
             (resBundle.handleGetObject("joinrowsetimpl.numnotequal").toString());
     } else {
        for(int i=0; i< rowset.length; i++) {
           ((CachedRowSetImpl)rowset[i]).setMatchColumn(columnIdx[i]);
           addRowSet((Joinable)rowset[i]);
        } //end for
     } //end if

   }


    /**
     * Adds the given <code>RowSet</code> objects to the <code>JOIN</code> relationship
     * and sets the designated columns as the match columns. If the first
     * <code>RowSet</code> object in the array of <code>RowSet</code> objects
     * is the first to be added to this <code>JoinRowSet</code>
     * object, it forms the basis of the <code>JOIN</code> relationship to be formed
     * when other <code>RowSet</code> objects are added.
     * <P>
     * The first <code>String</code> object
     * in <i>columnName</i> is used to set the match column for the first
     * <code>RowSet</code> object in <i>rowset</i>, the second <code>String</code>
     * object in <i>columnName</i> is used to set the match column for the second
     * <code>RowSet</code> object in <i>rowset</i>, and so on.
     * <P>
     * This method should be used when the given <code>RowSet</code> objects
     * do not already have match columns set.
     *
     * @param rowset an array of <code>RowSet</code> objects to be added to
     *         the <code>JOIN</code> relation; each <code>RowSet</code> object must
     *         implement the <code>Joinable</code> interface
     * @param columnName an array of <code>String</code> objects designating the columns
     *        to be set as the
     *        match columns for the <code>RowSet</code> objects in <i>rowset</i>
     * @throws SQLException if the number of <code>RowSet</code> objects in
     *         <i>rowset</i> is not equal to the number of <code>String</code> objects
     *         in <i>columnName</i>, an empty <code>JdbcRowSet</code> is added to the
     *         <code>JoinRowSet</code>, if a match column is not set,
     *         or one or the <code>RowSet</code> objects in <i>rowset</i> violates the
     *         active <code>JOIN</code>
     */
    public void addRowSet(RowSet[] rowset,
                          String[] columnName) throws SQLException {
    //validate if length of rowset array is same as length of int array.

     if(rowset.length != columnName.length) {
        throw new SQLException
                 (resBundle.handleGetObject("joinrowsetimpl.numnotequal").toString());
     } else {
        for(int i=0; i< rowset.length; i++) {
           ((CachedRowSetImpl)rowset[i]).setMatchColumn(columnName[i]);
           addRowSet((Joinable)rowset[i]);
        } //end for
     } //end if

    }

    /**
     * Returns a Collection of the <code>RowSet</code> object instances
     * currently residing with the instance of the <code>JoinRowSet</code>
     * object instance. This should return the 'n' number of RowSet contained
     * within the JOIN and maintain any updates that have occoured while in
     * this union.
     *
     * @return A <code>Collection</code> of the added <code>RowSet</code>
     * object instances
     * @throws SQLException if an error occours generating a collection
     * of the originating RowSets contained within the JOIN.
     */
    public Collection getRowSets() throws SQLException {
        return vecRowSetsInJOIN;
    }

    /**
     * Returns a string array of the RowSet names currently residing
     * with the <code>JoinRowSet</code> object instance.
     *
     * @return a string array of the RowSet names
     * @throws SQLException if an error occours retrieving the RowSet names
     * @see CachedRowSet#setTableName
     */
    public String[] getRowSetNames() throws SQLException {
        Object [] arr = vecTableNames.toArray();
        String []strArr = new String[arr.length];

        for( int i = 0;i < arr.length; i++) {
           strArr[i] = arr[i].toString();
        }

        return strArr;
    }

    /**
     * Creates a separate <code>CachedRowSet</code> object that contains the data
     * in this <code>JoinRowSet</code> object.
     * <P>
     * If any updates or modifications have been applied to this <code>JoinRowSet</code>
     * object, the <code>CachedRowSet</code> object returned by this method will
     * not be able to persist
     * the changes back to the originating rows and tables in the
     * data source because the data may be from different tables. The
     * <code>CachedRowSet</code> instance returned should not
     * contain modification data, such as whether a row has been updated or what the
     * original values are.  Also, the <code>CachedRowSet</code> object should clear
     * its  properties pertaining to
     * its originating SQL statement. An application should reset the
     * SQL statement using the <code>RowSet.setCommand</code> method.
     * <p>
     * To persist changes back to the data source, the <code>JoinRowSet</code> object
     * calls the method <code>acceptChanges</code>. Implementations
     * can leverage the internal data and update tracking in their
     * implementations to interact with the <code>SyncProvider</code> to persist any
     * changes.
     *
     * @return a <code>CachedRowSet</code> object containing the contents of this
     *         <code>JoinRowSet</code> object
     * @throws SQLException if an error occurs assembling the <code>CachedRowSet</code>
     *         object
     * @see javax.sql.RowSet
     * @see javax.sql.rowset.CachedRowSet
     * @see javax.sql.rowset.spi.SyncProvider
     */
    public CachedRowSet toCachedRowSet() throws SQLException {
        return crsInternal;
    }

    /**
     * Returns <code>true</code> if this <code>JoinRowSet</code> object supports
     * an SQL <code>CROSS_JOIN</code> and <code>false</code> if it does not.
     *
     * @return <code>true</code> if the CROSS_JOIN is supported; <code>false</code>
     *         otherwise
     */
    public boolean supportsCrossJoin() {
        return supportedJOINs[JoinRowSet.CROSS_JOIN];
    }

    /**
     * Returns <code>true</code> if this <code>JoinRowSet</code> object supports
     * an SQL <code>INNER_JOIN</code> and <code>false</code> if it does not.
     *
     * @return true is the INNER_JOIN is supported; false otherwise
     */
    public boolean supportsInnerJoin() {
        return supportedJOINs[JoinRowSet.INNER_JOIN];
    }

    /**
     * Returns <code>true</code> if this <code>JoinRowSet</code> object supports
     * an SQL <code>LEFT_OUTER_JOIN</code> and <code>false</code> if it does not.
     *
     * @return true is the LEFT_OUTER_JOIN is supported; false otherwise
     */
    public boolean supportsLeftOuterJoin() {
        return supportedJOINs[JoinRowSet.LEFT_OUTER_JOIN];
    }

    /**
     * Returns <code>true</code> if this <code>JoinRowSet</code> object supports
     * an SQL <code>RIGHT_OUTER_JOIN</code> and <code>false</code> if it does not.
     *
     * @return true is the RIGHT_OUTER_JOIN is supported; false otherwise
     */
    public boolean supportsRightOuterJoin() {
        return supportedJOINs[JoinRowSet.RIGHT_OUTER_JOIN];
    }

    /**
     * Returns <code>true</code> if this <code>JoinRowSet</code> object supports
     * an SQL <code>FULL_JOIN</code> and <code>false</code> if it does not.
     *
     * @return true is the FULL_JOIN is supported; false otherwise
     */
    public boolean supportsFullJoin() {
        return supportedJOINs[JoinRowSet.FULL_JOIN];

    }

    /**
     * Sets the type of SQL <code>JOIN</code> that this <code>JoinRowSet</code>
     * object will use. This method
     * allows an application to adjust the type of <code>JOIN</code> imposed
     * on tables contained within this <code>JoinRowSet</code> object and to do it
     * on the fly. The last <code>JOIN</code> type set determines the type of
     * <code>JOIN</code> to be performed.
     * <P>
     * Implementations should throw an <code>SQLException</code> if they do
     * not support the given <code>JOIN</code> type.
     *
     * @param type one of the standard <code>JoinRowSet</code> constants
     *        indicating the type of <code>JOIN</code>.  Must be one of the
     *        following:
     *            <code>JoinRowSet.CROSS_JOIN</code>
     *            <code>JoinRowSet.INNER_JOIN</code>
     *            <code>JoinRowSet.LEFT_OUTER_JOIN</code>
     *            <code>JoinRowSet.RIGHT_OUTER_JOIN</code>, or
     *            <code>JoinRowSet.FULL_JOIN</code>
     * @throws SQLException if an unsupported <code>JOIN</code> type is set
     */
    public void setJoinType(int type) throws SQLException {
        // The join which governs the join of two rowsets is the last
        // join set, using setJoinType

       if (type >= JoinRowSet.CROSS_JOIN && type <= JoinRowSet.FULL_JOIN) {
           if (type != JoinRowSet.INNER_JOIN) {
               // This 'if' will be removed after all joins are implemented.
               throw new SQLException(resBundle.handleGetObject("joinrowsetimpl.notsupported").toString());
           } else {
              Integer Intgr = new Integer(JoinRowSet.INNER_JOIN);
              vecJoinType.add(Intgr);
           }
       } else {
          throw new SQLException(resBundle.handleGetObject("joinrowsetimpl.notdefined").toString());
       }  //end if
    }


    /**
     * This checks for a match column for
     * whether it exists or not.
     *
     * @param <code>CachedRowSet</code> object whose match column needs to be checked.
     * @throws SQLException if MatchColumn is not set.
     */
    private boolean checkforMatchColumn(Joinable rs) throws SQLException {
        int[] i = rs.getMatchColumnIndexes();
        if (i.length <= 0) {
            return false;
        }
        return true;
    }

    /**
     * Internal initialization of <code>JoinRowSet</code>.
     */
    private void initJOIN(CachedRowSet rowset) throws SQLException {
        try {

            CachedRowSetImpl cRowset = (CachedRowSetImpl)rowset;
            // Create a new CachedRowSet object local to this function.
            CachedRowSetImpl crsTemp = new CachedRowSetImpl();
            RowSetMetaDataImpl rsmd = new RowSetMetaDataImpl();

            /* The following 'if block' seems to be always going true.
               commenting this out for present

            if (!supportedJOINs[1]) {
                throw new SQLException(resBundle.handleGetObject("joinrowsetimpl.notsupported").toString());
            }

            */

            if (vecRowSetsInJOIN.isEmpty() ) {

                // implies first cRowset to be added to the Join
                // simply add this as a CachedRowSet.
                // Also add it to the class variable of type vector
                // do not need to check "type" of Join but it should be set.
                crsInternal = (CachedRowSetImpl)rowset.createCopy();
                crsInternal.setMetaData((RowSetMetaDataImpl)cRowset.getMetaData());
                // metadata will also set the MatchColumn.

                vecRowSetsInJOIN.add(cRowset);

            } else {
                // At this point we are ready to add another rowset to 'this' object
                // Check the size of vecJoinType and vecRowSetsInJoin

                // If nothing is being set, internally call setJoinType()
                // to set to JoinRowSet.INNER_JOIN.

                // For two rowsets one (valid) entry should be there in vecJoinType
                // For three rowsets two (valid) entries should be there in vecJoinType

                // Maintain vecRowSetsInJoin = vecJoinType + 1


                if( (vecRowSetsInJOIN.size() - vecJoinType.size() ) == 2 ) {
                   // we are going to add next rowset and setJoinType has not been set
                   // recently, so set it to setJoinType() to JoinRowSet.INNER_JOIN.
                   // the default join type

                        setJoinType(JoinRowSet.INNER_JOIN);
                } else if( (vecRowSetsInJOIN.size() - vecJoinType.size() ) == 1  ) {
                   // do nothing setjoinType() has been set by programmer
                }

                // Add the table names to the class variable of type vector.
                vecTableNames.add(crsInternal.getTableName());
                vecTableNames.add(cRowset.getTableName());
                // Now we have two rowsets crsInternal and cRowset which need
                // to be INNER JOIN'ED to form a new rowset
                // Compare table1.MatchColumn1.value1 == { table2.MatchColumn2.value1
                //                              ... upto table2.MatchColumn2.valueN }
                //     ...
                // Compare table1.MatchColumn1.valueM == { table2.MatchColumn2.value1
                //                              ... upto table2.MatchColumn2.valueN }
                //
                // Assuming first rowset has M rows and second N rows.

                int rowCount2 = cRowset.size();
                int rowCount1 = crsInternal.size();

                // total columns in the new CachedRowSet will be sum of both -1
                // (common column)
                int matchColumnCount = 0;
                for(int i=0; i< crsInternal.getMatchColumnIndexes().length; i++) {
                    if(crsInternal.getMatchColumnIndexes()[i] != -1)
                        ++ matchColumnCount;
                    else
                        break;
                }

                rsmd.setColumnCount
                    (crsInternal.getMetaData().getColumnCount() +
                     cRowset.getMetaData().getColumnCount() - matchColumnCount);

                crsTemp.setMetaData(rsmd);
                crsInternal.beforeFirst();
                cRowset.beforeFirst();
                for (int i = 1 ; i <= rowCount1 ; i++) {
                  if(crsInternal.isAfterLast() ) {
                    break;
                  }
                  if(crsInternal.next()) {
                    cRowset.beforeFirst();
                    for(int j = 1 ; j <= rowCount2 ; j++) {
                         if( cRowset.isAfterLast()) {
                            break;
                         }
                         if(cRowset.next()) {
                             boolean match = true;
                             for(int k=0; k<matchColumnCount; k++) {
                                 if (!crsInternal.getObject( crsInternal.getMatchColumnIndexes()[k]).equals
                                         (cRowset.getObject(cRowset.getMatchColumnIndexes()[k]))) {
                                     match = false;
                                     break;
                                 }
                             }
                             if (match) {

                                int p;
                                int colc = 0;   // reset this variable everytime you loop
                                // re create a JoinRowSet in crsTemp object
                                crsTemp.moveToInsertRow();

                                // create a new rowset crsTemp with data from first rowset
                            for( p=1;
                                p<=crsInternal.getMetaData().getColumnCount();p++) {

                                match = false;
                                for(int k=0; k<matchColumnCount; k++) {
                                 if (p == crsInternal.getMatchColumnIndexes()[k] ) {
                                     match = true;
                                     break;
                                 }
                                }
                                    if ( !match ) {

                                    crsTemp.updateObject(++colc, crsInternal.getObject(p));
                                    // column type also needs to be passed.

                                    rsmd.setColumnName
                                        (colc, crsInternal.getMetaData().getColumnName(p));
                                    rsmd.setTableName(colc, crsInternal.getTableName());

                                    rsmd.setColumnType(p, crsInternal.getMetaData().getColumnType(p));
                                    rsmd.setAutoIncrement(p, crsInternal.getMetaData().isAutoIncrement(p));
                                    rsmd.setCaseSensitive(p, crsInternal.getMetaData().isCaseSensitive(p));
                                    rsmd.setCatalogName(p, crsInternal.getMetaData().getCatalogName(p));
                                    rsmd.setColumnDisplaySize(p, crsInternal.getMetaData().getColumnDisplaySize(p));
                                    rsmd.setColumnLabel(p, crsInternal.getMetaData().getColumnLabel(p));
                                    rsmd.setColumnType(p, crsInternal.getMetaData().getColumnType(p));
                                    rsmd.setColumnTypeName(p, crsInternal.getMetaData().getColumnTypeName(p));
                                    rsmd.setCurrency(p,crsInternal.getMetaData().isCurrency(p) );
                                    rsmd.setNullable(p, crsInternal.getMetaData().isNullable(p));
                                    rsmd.setPrecision(p, crsInternal.getMetaData().getPrecision(p));
                                    rsmd.setScale(p, crsInternal.getMetaData().getScale(p));
                                    rsmd.setSchemaName(p, crsInternal.getMetaData().getSchemaName(p));
                                    rsmd.setSearchable(p, crsInternal.getMetaData().isSearchable(p));
                                    rsmd.setSigned(p, crsInternal.getMetaData().isSigned(p));

                                } else {
                                    // will happen only once, for that  merged column pass
                                    // the types as OBJECT, if types not equal

                                    crsTemp.updateObject(++colc, crsInternal.getObject(p));

                                    rsmd.setColumnName(colc, crsInternal.getMetaData().getColumnName(p));
                                    rsmd.setTableName
                                        (colc, crsInternal.getTableName()+
                                         "#"+
                                         cRowset.getTableName());


                                    rsmd.setColumnType(p, crsInternal.getMetaData().getColumnType(p));
                                    rsmd.setAutoIncrement(p, crsInternal.getMetaData().isAutoIncrement(p));
                                    rsmd.setCaseSensitive(p, crsInternal.getMetaData().isCaseSensitive(p));
                                    rsmd.setCatalogName(p, crsInternal.getMetaData().getCatalogName(p));
                                    rsmd.setColumnDisplaySize(p, crsInternal.getMetaData().getColumnDisplaySize(p));
                                    rsmd.setColumnLabel(p, crsInternal.getMetaData().getColumnLabel(p));
                                    rsmd.setColumnType(p, crsInternal.getMetaData().getColumnType(p));
                                    rsmd.setColumnTypeName(p, crsInternal.getMetaData().getColumnTypeName(p));
                                    rsmd.setCurrency(p,crsInternal.getMetaData().isCurrency(p) );
                                    rsmd.setNullable(p, crsInternal.getMetaData().isNullable(p));
                                    rsmd.setPrecision(p, crsInternal.getMetaData().getPrecision(p));
                                    rsmd.setScale(p, crsInternal.getMetaData().getScale(p));
                                    rsmd.setSchemaName(p, crsInternal.getMetaData().getSchemaName(p));
                                    rsmd.setSearchable(p, crsInternal.getMetaData().isSearchable(p));
                                    rsmd.setSigned(p, crsInternal.getMetaData().isSigned(p));

                                    //don't do ++colc in the above statement
                                } //end if
                            } //end for


                            // append the rowset crsTemp, with data from second rowset
                            for(int q=1;
                                q<= cRowset.getMetaData().getColumnCount();q++) {

                                match = false;
                                for(int k=0; k<matchColumnCount; k++) {
                                 if (q == cRowset.getMatchColumnIndexes()[k] ) {
                                     match = true;
                                     break;
                                 }
                                }
                                    if ( !match ) {

                                    crsTemp.updateObject(++colc, cRowset.getObject(q));

                                    rsmd.setColumnName
                                        (colc, cRowset.getMetaData().getColumnName(q));
                                    rsmd.setTableName(colc, cRowset.getTableName());

                                    /**
                                      * This will happen for a special case scenario. The value of 'p'
                                      * will always be one more than the number of columns in the first
                                      * rowset in the join. So, for a value of 'q' which is the number of
                                      * columns in the second rowset that participates in the join.
                                      * So decrement value of 'p' by 1 else `p+q-1` will be out of range.
                                      **/

                                    //if((p+q-1) > ((crsInternal.getMetaData().getColumnCount()) +
                                      //            (cRowset.getMetaData().getColumnCount())     - 1)) {
                                      // --p;
                                    //}
                                    rsmd.setColumnType(p+q-1, cRowset.getMetaData().getColumnType(q));
                                    rsmd.setAutoIncrement(p+q-1, cRowset.getMetaData().isAutoIncrement(q));
                                    rsmd.setCaseSensitive(p+q-1, cRowset.getMetaData().isCaseSensitive(q));
                                    rsmd.setCatalogName(p+q-1, cRowset.getMetaData().getCatalogName(q));
                                    rsmd.setColumnDisplaySize(p+q-1, cRowset.getMetaData().getColumnDisplaySize(q));
                                    rsmd.setColumnLabel(p+q-1, cRowset.getMetaData().getColumnLabel(q));
                                    rsmd.setColumnType(p+q-1, cRowset.getMetaData().getColumnType(q));
                                    rsmd.setColumnTypeName(p+q-1, cRowset.getMetaData().getColumnTypeName(q));
                                    rsmd.setCurrency(p+q-1,cRowset.getMetaData().isCurrency(q) );
                                    rsmd.setNullable(p+q-1, cRowset.getMetaData().isNullable(q));
                                    rsmd.setPrecision(p+q-1, cRowset.getMetaData().getPrecision(q));
                                    rsmd.setScale(p+q-1, cRowset.getMetaData().getScale(q));
                                    rsmd.setSchemaName(p+q-1, cRowset.getMetaData().getSchemaName(q));
                                    rsmd.setSearchable(p+q-1, cRowset.getMetaData().isSearchable(q));
                                    rsmd.setSigned(p+q-1, cRowset.getMetaData().isSigned(q));
                                }
                                else {
                                    --p;
                                }
                            }
                            crsTemp.insertRow();
                            crsTemp.moveToCurrentRow();

                        } else {
                            // since not equa12
                            // so do nothing
                        } //end if
                         // bool1 = cRowset.next();
                         }

                    } // end inner for
                     //bool2 = crsInternal.next();
                   }

                } //end outer for
                crsTemp.setMetaData(rsmd);
                crsTemp.setOriginal();

                // Now the join is done.
               // Make crsInternal = crsTemp, to be ready for next merge, if at all.

                int[] pCol = new int[matchColumnCount];
                for(int i=0; i<matchColumnCount; i++)
                   pCol[i] = crsInternal.getMatchColumnIndexes()[i];

                crsInternal = (CachedRowSetImpl)crsTemp.createCopy();

                // Because we add the first rowset as crsInternal to the
                // merged rowset, so pCol will point to the Match column.
                // until reset, am not sure we should set this or not(?)
                // if this is not set next inner join won't happen
                // if we explicitly do not set a set MatchColumn of
                // the new crsInternal.

                crsInternal.setMatchColumn(pCol);
                // Add the merged rowset to the class variable of type vector.
                crsInternal.setMetaData(rsmd);
                vecRowSetsInJOIN.add(cRowset);
            } //end if
        } catch(SQLException sqle) {
            // %%% Exception should not dump here:
            sqle.printStackTrace();
            throw new SQLException(resBundle.handleGetObject("joinrowsetimpl.initerror").toString() + sqle);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException(resBundle.handleGetObject("joinrowsetimpl.genericerr").toString() + e);
        }
    }

    /**
     * Return a SQL-like description of the <code>WHERE</code> clause being used
     * in a <code>JoinRowSet</code> object instance. An implementation can describe
     * the <code>WHERE</code> clause of the SQL <code>JOIN</code> by supplying a <code>SQL</code>
     * strings description of <code>JOIN</code> or provide a textual description to assist
     * applications using a <code>JoinRowSet</code>.
     *
     * @return whereClause a textual or SQL descripition of the logical
     * <code>WHERE</code> cluase used in the <code>JoinRowSet</code> instance
     * @throws SQLException if an error occurs in generating a representation
     * of the <code>WHERE</code> clause.
     */
    public String getWhereClause() throws SQLException {

       String strWhereClause = "Select ";
       String whereClause;
       String tabName= null;
       String strTabName = null;
       int sz,cols;
       int j;
       CachedRowSetImpl crs;

       // get all the column(s) names from each rowset.
       // append them with their tablenames i.e. tableName.columnName
       // Select tableName1.columnName1,..., tableNameX.columnNameY
       // from tableName1,...tableNameX where
       // tableName1.(rowset1.getMatchColumnName()) ==
       // tableName2.(rowset2.getMatchColumnName()) + "and" +
       // tableNameX.(rowsetX.getMatchColumnName()) ==
       // tableNameZ.(rowsetZ.getMatchColumnName()));

       tabName = new String();
       strTabName  = new String();
       sz = vecRowSetsInJOIN.size();
       for(int i=0;i<sz; i++) {
          crs = (CachedRowSetImpl)vecRowSetsInJOIN.get(i);
          cols = crs.getMetaData().getColumnCount();
          tabName = tabName.concat(crs.getTableName());
          strTabName = strTabName.concat(tabName+", ");
          j = 1;
          while(j<cols) {

            strWhereClause = strWhereClause.concat
                (tabName+"."+crs.getMetaData().getColumnName(j++));
            strWhereClause = strWhereClause.concat(", ");
          } //end while
        } //end for


        // now remove the last ","
        strWhereClause = strWhereClause.substring
             (0, strWhereClause.lastIndexOf(","));

        // Add from clause
        strWhereClause = strWhereClause.concat(" from ");

        // Add the table names.
        strWhereClause = strWhereClause.concat(strTabName);

        //Remove the last ","
        strWhereClause = strWhereClause.substring
             (0, strWhereClause.lastIndexOf(","));

        // Add the where clause
        strWhereClause = strWhereClause.concat(" where ");

        // Get the match columns
        // rowset1.getMatchColumnName() == rowset2.getMatchColumnName()
         for(int i=0;i<sz; i++) {
             strWhereClause = strWhereClause.concat(
               ((CachedRowSetImpl)vecRowSetsInJOIN.get(i)).getMatchColumnNames()[0]);
             if(i%2!=0) {
               strWhereClause = strWhereClause.concat("=");
             }  else {
               strWhereClause = strWhereClause.concat(" and");
             }
          strWhereClause = strWhereClause.concat(" ");
         }

        return strWhereClause;
    }


    /**
     * Moves the cursor down one row from its current position and
     * returns <code>true</code> if the new cursor position is a
     * valid row.
     * The cursor for a new <code>ResultSet</code> object is initially
     * positioned before the first row. The first call to the method
     * <code>next</code> moves the cursor to the first row, making it
     * the current row; the second call makes the second row the
     * current row, and so on.
     *
     * <P>If an input stream from the previous row is open, it is
     * implicitly closed. The <code>ResultSet</code> object's warning
     * chain is cleared when a new row is read.
     *
     * @return <code>true</code> if the new current row is valid;
     *         <code>false</code> if there are no more rows
     * @throws SQLException if an error occurs or
     *            the cursor is not positioned in the rowset, before
     *            the first row, or after the last row
     */
    public boolean next() throws SQLException {
        return crsInternal.next();
    }


    /**
     * Releases the current contents of this rowset, discarding  outstanding
     * updates.  The rowset contains no rows after the method
     * <code>release</code> is called. This method sends a
     * <code>RowSetChangedEvent</code> object to all registered listeners prior
     * to returning.
     *
     * @throws SQLException if an error occurs
     */
    public void close() throws SQLException {
        crsInternal.close();
    }


    /**
     * Reports whether the last column read was SQL <code>NULL</code>.
     * Note that you must first call the method <code>getXXX</code>
     * on a column to try to read its value and then call the method
     * <code>wasNull</code> to determine whether the value was
     * SQL <code>NULL</code>.
     *
     * @return <code>true</code> if the value in the last column read
     *         was SQL <code>NULL</code>; <code>false</code> otherwise
     * @throws SQLException if an error occurs
     */
    public boolean wasNull() throws SQLException {
        return crsInternal.wasNull();
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>JoinRowSetImpl</code> object as a
     * <code>String</code> object.
     *
     * @param columnIndex the first column is <code>1</code>, the second
     *        is <code>2</code>, and so on; must be <code>1</code> or larger
     *        and equal to or less than the number of columns in the rowset
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         result is <code>null</code>
     * @throws SQLException if the given column index is out of bounds or
     *            the cursor is not on a valid row
     */
    public String getString(int columnIndex) throws SQLException {
        return crsInternal.getString(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>JoinRowSetImpl</code> object as a
     * <code>boolean</code> value.
     *
     * @param columnIndex the first column is <code>1</code>, the second
     *        is <code>2</code>, and so on; must be <code>1</code> or larger
     *        and equal to or less than the number of columns in the rowset
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         result is <code>false</code>
     * @throws SQLException if the given column index is out of bounds,
     *            the cursor is not on a valid row, or this method fails
     */
    public boolean getBoolean(int columnIndex) throws SQLException {
        return crsInternal.getBoolean(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>JoinRowSetImpl</code> object as a
     * <code>byte</code> value.
     *
     * @param columnIndex the first column is <code>1</code>, the second
     *        is <code>2</code>, and so on; must be <code>1</code> or larger
     *        and equal to or less than the number of columns in the rowset
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         result is <code>0</code>
     * @throws SQLException if the given column index is out of bounds,
     *            the cursor is not on a valid row, or this method fails
     */
    public byte getByte(int columnIndex) throws SQLException {
        return crsInternal.getByte(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>JoinRowSetImpl</code> object as a
             * <code>short</code> value.
             *
     * @param columnIndex the first column is <code>1</code>, the second
     *        is <code>2</code>, and so on; must be <code>1</code> or larger
     *        and equal to or less than the number of columns in the rowset
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         result is <code>0</code>
     * @throws SQLException if the given column index is out of bounds,
     *            the cursor is not on a valid row, or this method fails
     */
    public short getShort(int columnIndex) throws SQLException {
        return crsInternal.getShort(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>JoinRowSetImpl</code> object as a
     * <code>short</code> value.
     *
     * @param columnIndex the first column is <code>1</code>, the second
     *        is <code>2</code>, and so on; must be <code>1</code> or larger
     *        and equal to or less than the number of columns in the rowset
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         result is <code>0</code>
     * @throws SQLException if the given column index is out of bounds,
     *            the cursor is not on a valid row, or this method fails
     */
    public int getInt(int columnIndex) throws SQLException {
        return crsInternal.getInt(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>JoinRowSetImpl</code> object as a
     * <code>long</code> value.
     *
     * @param columnIndex the first column is <code>1</code>, the second
     *        is <code>2</code>, and so on; must be <code>1</code> or larger
     *        and equal to or less than the number of columns in the rowset
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         result is <code>0</code>
     * @throws SQLException if the given column index is out of bounds,
     *            the cursor is not on a valid row, or this method fails
     */
    public long getLong(int columnIndex) throws SQLException {
        return crsInternal.getLong(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>JoinRowSetImpl</code> object as a
     * <code>float</code> value.
     *
     * @param columnIndex the first column is <code>1</code>, the second
     *        is <code>2</code>, and so on; must be <code>1</code> or larger
     *        and equal to or less than the number of columns in the rowset
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         result is <code>0</code>
     * @throws SQLException if the given column index is out of bounds,
     *            the cursor is not on a valid row, or this method fails
     */
    public float getFloat(int columnIndex) throws SQLException {
        return crsInternal.getFloat(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>JoinRowSetImpl</code> object as a
     * <code>double</code> value.
     *
     * @param columnIndex the first column is <code>1</code>, the second
     *        is <code>2</code>, and so on; must be <code>1</code> or larger
     *        and equal to or less than the number of columns in the rowset
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         result is <code>0</code>
     * @throws SQLException if the given column index is out of bounds,
     *            the cursor is not on a valid row, or this method fails
     */
    public double getDouble(int columnIndex) throws SQLException {
        return crsInternal.getDouble(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>JoinRowSetImpl</code> object as a
     * <code>java.math.BigDecimal</code> object.
     * <P>
     * This method is deprecated; use the version of <code>getBigDecimal</code>
     * that does not take a scale parameter and returns a value with full
     * precision.
     *
     * @param columnIndex the first column is <code>1</code>, the second
     *        is <code>2</code>, and so on; must be <code>1</code> or larger
     *        and equal to or less than the number of columns in the rowset
     * @param scale the number of digits to the right of the decimal point in the
     *        value returned
     * @return the column value with the specified number of digits to the right
     *         of the decimal point; if the value is SQL <code>NULL</code>, the
     *         result is <code>null</code>
     * @throws SQLException if the given column index is out of bounds,
     *            the cursor is not on a valid row, or this method fails
     * @deprecated
     */
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return crsInternal.getBigDecimal(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>JoinRowSetImpl</code> object as a
     * <code>byte array</code> value.
     *
     * @param columnIndex the first column is <code>1</code>, the second
     *        is <code>2</code>, and so on; must be <code>1</code> or larger
     *        and equal to or less than the number of columns in the rowset
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         result is <code>null</code>
     * @throws SQLException if the given column index is out of bounds,
     *            the cursor is not on a valid row, or the the value to be
     *            retrieved is not binary
     */
    public byte[] getBytes(int columnIndex) throws SQLException {
        return crsInternal.getBytes(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>JoinRowSetImpl</code> object as a
     * <code>java.sql.Date</code> object.
     *
     * @param columnIndex the first column is <code>1</code>, the second
     *        is <code>2</code>, and so on; must be <code>1</code> or larger
     *        and equal to or less than the number of columns in the rowset
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         result is <code>null</code>
     * @throws SQLException if the given column index is out of bounds,
     *            the cursor is not on a valid row, or this method fails
     */
    public java.sql.Date getDate(int columnIndex) throws SQLException {
        return crsInternal.getDate(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>JoinRowSetImpl</code> object as a
     * <code>java.sql.Time</code> object.
     *
     * @param columnIndex the first column is <code>1</code>, the second
     *        is <code>2</code>, and so on; must be <code>1</code> or larger
     *        and equal to or less than the number of columns in the rowset
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         result is <code>null</code>
     * @throws SQLException if the given column index is out of bounds,
     *            the cursor is not on a valid row, or this method fails
     */
    public java.sql.Time getTime(int columnIndex) throws SQLException {
        return crsInternal.getTime(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>JoinRowSetImpl</code> object as a
     * <code>java.sql.Timestamp</code> object.
     *
     * @param columnIndex the first column is <code>1</code>, the second
     *        is <code>2</code>, and so on; must be <code>1</code> or larger
     *        and equal to or less than the number of columns in the rowset
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         result is <code>null</code>
     * @throws SQLException if the given column index is out of bounds,
     *            the cursor is not on a valid row, or this method fails
     */
    public java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException {
        return crsInternal.getTimestamp(columnIndex);
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>JoinRowSetImpl</code> object as a
     * <code>java.sql.Timestamp</code> object.
     *
     * @param columnIndex the first column is <code>1</code>, the second
     *        is <code>2</code>, and so on; must be <code>1</code> or larger
     *        and equal to or less than the number of columns in the rowset
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         result is <code>null</code>
     * @throws SQLException if the given column index is out of bounds,
     *            the cursor is not on a valid row, or this method fails
     */
    public java.io.InputStream getAsciiStream(int columnIndex) throws SQLException {
        return crsInternal.getAsciiStream(columnIndex);
    }

    /**
     * A column value can be retrieved as a stream of Unicode characters
     * and then read in chunks from the stream.  This method is particularly
     * suitable for retrieving large LONGVARCHAR values.  The JDBC driver will
     * do any necessary conversion from the database format into Unicode.
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a get method implicitly closes the stream. . Also, a
     * stream may return 0 for available() whether there is data
     * available or not.
     *
     * @param columnIndex the first column is <code>1</code>, the second
     *        is <code>2</code>, and so on; must be <code>1</code> or larger
     *        and equal to or less than the number of columns in this rowset
     * @return a Java input stream that delivers the database column value
     * as a stream of two byte Unicode characters.  If the value is SQL NULL
     * then the result is null.
     * @throws SQLException if an error occurs
     * @deprecated
     */
    public java.io.InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return crsInternal.getUnicodeStream(columnIndex);
    }

    /**
     * A column value can be retrieved as a stream of uninterpreted bytes
     * and then read in chunks from the stream.  This method is particularly
     * suitable for retrieving large LONGVARBINARY values.
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a get method implicitly closes the stream. Also, a
     * stream may return 0 for available() whether there is data
     * available or not.
     *
     * @param columnIndex the first column is <code>1</code>, the second
     *        is <code>2</code>, and so on; must be <code>1</code> or larger
     *        and equal to or less than the number of columns in the rowset
     * @return a Java input stream that delivers the database column value
     * as a stream of uninterpreted bytes.  If the value is SQL NULL
     * then the result is null.
     * @throws SQLException if an error occurs
     */
    public java.io.InputStream getBinaryStream(int columnIndex) throws SQLException {
        return crsInternal.getBinaryStream(columnIndex);
    }

    // ColumnName methods

    /**
     * Retrieves the value stored in the designated column
     * of the current row as a <code>String</code> object.
     *
     * @param columnName a <code>String</code> object giving the SQL name of
     *        a column in this <code>JoinRowSetImpl</code> object
     * @return the column value; if the value is SQL <code>NULL</code>,
     *         the result is <code>null</code>
     * @throws SQLException if the given column name does not match one of
     *            this rowset's column names or the cursor is not on one of
     *            this rowset's rows or its insert row
     */
    public String getString(String columnName) throws SQLException {
        return crsInternal.getString(columnName);
    }

    /**
     * Retrieves the value stored in the designated column
     * of the current row as a <code>boolean</code> value.
     *
     * @param columnName a <code>String</code> object giving the SQL name of
     *        a column in this <code>JoinRowSetImpl</code> object
     * @return the column value; if the value is SQL <code>NULL</code>,
     *         the result is <code>false</code>
     * @throws SQLException if the given column name does not match one of
     *            this rowset's column names or the cursor is not on one of
     *            this rowset's rows or its insert row
     */
    public boolean getBoolean(String columnName) throws SQLException {
        return crsInternal.getBoolean(columnName);
    }

    /**
     * Retrieves the value stored in the designated column
     * of the current row as a <code>byte</code> value.
     *
     * @param columnName a <code>String</code> object giving the SQL name of
     *        a column in this <code>JoinRowSetImpl</code> object
     * @return the column value; if the value is SQL <code>NULL</code>,
     *         the result is <code>0</code>
     * @throws SQLException if the given column name does not match one of
     *            this rowset's column names or the cursor is not on one of
     *            this rowset's rows or its insert row
     */
    public byte getByte(String columnName) throws SQLException {
        return crsInternal.getByte(columnName);
    }

    /**
     * Retrieves the value stored in the designated column
     * of the current row as a <code>short</code> value.
     *
     * @param columnName a <code>String</code> object giving the SQL name of
     *        a column in this <code>JoinRowSetImpl</code> object
     * @return the column value; if the value is SQL <code>NULL</code>,
     *         the result is <code>0</code>
     * @throws SQLException if the given column name does not match one of
     *            this rowset's column names or the cursor is not on one of
     *            this rowset's rows or its insert row
     */
    public short getShort(String columnName) throws SQLException {
        return crsInternal.getShort(columnName);
    }

    /**
     * Retrieves the value stored in the designated column
     * of the current row as an <code>int</code> value.
     *
     * @param columnName a <code>String</code> object giving the SQL name of
     *        a column in this <code>JoinRowSetImpl</code> object
     * @return the column value; if the value is SQL <code>NULL</code>,
     *         the result is <code>0</code>
     * @throws SQLException if the given column name does not match one of
     *            this rowset's column names or the cursor is not on one of
     *            this rowset's rows or its insert row
     */
    public int getInt(String columnName) throws SQLException {
        return crsInternal.getInt(columnName);
    }

    /**
     * Retrieves the value stored in the designated column
     * of the current row as a <code>long</code> value.
     *
     * @param columnName a <code>String</code> object giving the SQL name of
     *        a column in this <code>JoinRowSetImpl</code> object
     * @return the column value; if the value is SQL <code>NULL</code>,
     *         the result is <code>0</code>
     * @throws SQLException if the given column name does not match one of
     *            this rowset's column names or the cursor is not on one of
     *            this rowset's rows or its insert row
     */
    public long getLong(String columnName) throws SQLException {
        return crsInternal.getLong(columnName);
    }

    /**
     * Retrieves the value stored in the designated column
     * of the current row as a <code>float</code> value.
     *
     * @param columnName a <code>String</code> object giving the SQL name of
     *        a column in this <code>JoinRowSetImpl</code> object
     * @return the column value; if the value is SQL <code>NULL</code>,
     *         the result is <code>0</code>
     * @throws SQLException if the given column name does not match one of
     *            this rowset's column names or the cursor is not on one of
     *            this rowset's rows or its insert row
     */
    public float getFloat(String columnName) throws SQLException {
        return crsInternal.getFloat(columnName);
    }

    /**
     * Retrieves the value stored in the designated column
     * of the current row as a <code>double</code> value.
     *
     * @param columnName a <code>String</code> object giving the SQL name of
     *        a column in this <code>JoinRowSetImpl</code> object
     * @return the column value; if the value is SQL <code>NULL</code>,
     *         the result is <code>0</code>
     * @throws SQLException if the given column name does not match one of
     *            this rowset's column names or the cursor is not on one of
     *            this rowset's rows or its insert row
     */
    public double getDouble(String columnName) throws SQLException {
        return crsInternal.getDouble(columnName);
    }

    /**
     * Retrieves the value stored in the designated column
     * of the current row as a <code>java.math.BigDecimal</code> object.
     *
     * @param columnName a <code>String</code> object giving the SQL name of
     *        a column in this <code>JoinRowSetImpl</code> object
     * @param scale the number of digits to the right of the decimal point
     * @return the column value; if the value is SQL <code>NULL</code>,
     *         the result is <code>null</code>
     * @thro
