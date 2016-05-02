/**
 * Copyright (c) 2003 Daffodil Software Ltd all rights reserved.
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of version 2 of the GNU General Public License as
 * published by the Free Software Foundation.
 * There are special exceptions to the terms and conditions of the GPL
 * as it is applied to this software. See the GNU General Public License for more details.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.daffodilwoods.replication.xml;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

import com.daffodilwoods.replication.*;
import com.daffodilwoods.replication.DBHandler.*;
import com.daffodilwoods.replication.zip.*;
import org.apache.log4j.Logger;
import java.math.BigDecimal;

/**
 * This class was implemented for handling all the issues that will occure at
 * the time of creating XML file for synchronization purpose.
 */

public class SyncXMLCreator {

  String local_pub_sub_name, shadowTable, remoteServerName;
//  Connection pub_sub_Connection;
  ConnectionPool connectionPool;
  AbstractDataBaseHandler dbDataypeHandler;
  private ArrayList viewedIds;
  private String[] primaryColNames;
  private int[] primaryColumnTypes;
  private PreparedStatement primaryPreparedStatement,commonPreparedStatement;
  private Statement commonStatement = null;

//  OutputStreamWriter os;
//  BufferedWriter bw;
  public ServerSocket serverSocket = null;
  RepTable repTable;
  String filterClause;
  String tableName;
  private String[] parameters;
  private boolean USE_getLastRecord = true;
  protected static Logger log = Logger.getLogger(SyncXMLCreator.class.getName());

  /**
   * Creates an XML file for synchronizing data between server and client i.e. Subscriber and publisher.
   * @param pub_sub_name0
   * @param pub_sub_Connection0
   * @param dbDataTypeHandler0
   */
  public SyncXMLCreator(String pub_sub_name0, ConnectionPool connectionPool0,
                        AbstractDataBaseHandler dbDataypeHandler0) {
    local_pub_sub_name = pub_sub_name0;
    connectionPool = connectionPool0;
    dbDataypeHandler = dbDataypeHandler0;
  }

  /**
      if operation is I , insert the record
      if operation is U check for its last record and also make extra tag for primary key of the initial record
      and also make attribute for columns which are updated.
      if operation id D then mark it for deletion.

      case1.
      =====
       If a user isnert a new record then to delete it. It does not considered
       and we do not write it in XML file.

       If a new record is inserted and after that it is updated in that case it
       considered as a new inserted record not a updated record
   */
  private Object[] createXMLFile(String xmlFileURL, String zipFileURL,
                                 String xmlFileName, String remote_Pub_Sub_Name,
                                 ArrayList pubRepTables,
                                 String clientServerName, Socket socket,
                                 int noOfTables, boolean DeleteXML,
                                 String local_pub_subName) throws RepException {
    ResultSet rows = null;
    try {

      // must be true
      boolean toWriteInsertElement = true;
      // must be false
      boolean doNotWriteInsertElement = false;

      //must be true
      boolean toWriteUpdateElement = true;
      //must be false
      boolean doNotWriteUpdateElement = false;

      //must be false
      boolean toWriteDeleElement = false;

      //must be true
      boolean doNotWriteDeleElement = true;

      ArrayList EnCodedcols;
      FileOutputStream fos = new FileOutputStream(xmlFileURL);
      OutputStreamWriter os = new OutputStreamWriter(fos);
      BufferedWriter bw = new BufferedWriter(os);
      Connection pub_sub_Connection = connectionPool.getConnection(
          local_pub_sub_name);
      XMLWriter xmlWriter = new XMLWriter(bw, dbDataypeHandler,
                                          pub_sub_Connection);
      bw.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
      bw.write("<root>");
      ArrayList usedActualTables = new ArrayList();
      // Must be uncommented
//      String tableName;
      String[] primarycols;
      remoteServerName = clientServerName;
      Object[] lastIdArray = new Object[noOfTables];

      for (int i = 0; i < noOfTables; i++) {
        repTable = ( (RepTable) pubRepTables.get(i));
        if (repTable.getCreateShadowTable().equalsIgnoreCase(RepConstants.NO))
          continue;
        tableName = repTable.getSchemaQualifiedName().toString();
        shadowTable = RepConstants.shadow_Table(repTable.getSchemaQualifiedName().
                                                toString());
        lastIdArray[i] = getLastUIDFromShadowTable(shadowTable);
      }
      for (int i = noOfTables - 1; i >= 0; i--) {
        try {
          repTable = ( (RepTable) pubRepTables.get(i));
          if (repTable.getCreateShadowTable().equalsIgnoreCase(RepConstants.NO))
            continue;
          tableName = repTable.getSchemaQualifiedName().toString();
          EnCodedcols = PathHandler.getEncodedColumns(tableName);
          shadowTable = RepConstants.shadow_Table(repTable.
                                                  getSchemaQualifiedName().
                                                  toString());

          viewedIds = new ArrayList();
          long lastId = getLastSyncId(remote_Pub_Sub_Name, tableName);
          primaryColNames = repTable.getPrimaryColumns();
          xmlWriter.setNoOFPrimaryColumnNumber(primaryColNames.length);

          primaryPreparedStatement = dbDataypeHandler.
              makePrimaryPreperedStatement(primaryColNames, shadowTable,
                                           local_pub_sub_name);
          commonPreparedStatement = makeCommonPreparedStatement(shadowTable);
          //statement whose resulset is set in tracer in getlastrecord in update case
          commonStatement = pub_sub_Connection.createStatement();

          // deleting records from shadow table with primaryColumns of Main Table as NULL
          deleteRecordsFromShadowTableWithNullPk();
          // String Buffer should be used
          String query = "Select * from " + shadowTable + " where " +
              RepConstants.shadow_sync_id1 +
              " > " + lastId + " and " + RepConstants.shadow_serverName_n +
              " != '" + remoteServerName + "' order by " +
              RepConstants.shadow_sync_id1;
          log.debug(query);
          filterClause = repTable.getFilterClause();
          rows = getResultSet(query);
          primaryColumnTypes = new int[primaryColNames.length];
          ResultSetMetaData rsmt = rows.getMetaData();
          int noOfColumns = rsmt.getColumnCount();
          String operation;
          if (rows.next()) {
            bw.write("<tableName>");
            bw.write(tableName);
            usedActualTables.add(tableName);
            do {
//            int icount =0;
//            long time = System.currentTimeMillis();
              operation = rows.getString(RepConstants.shadow_operation3);
              if (operation.equalsIgnoreCase(RepConstants.insert_operation)) {
                // Do not write the insert element in XML file
                makeInsertElement(bw, operation, noOfColumns, xmlWriter, rows,
                                  rsmt, remoteServerName,
                                  doNotWriteInsertElement, EnCodedcols);
              }
              else
              if (operation.equalsIgnoreCase(RepConstants.update_operation)) {
                // Do not write the update element in XML file.
                makeUpdateElement(bw, noOfColumns, rsmt, xmlWriter, rows,
                                  shadowTable, remoteServerName,
                                  doNotWriteUpdateElement, EnCodedcols);
              }
              else if (operation.equalsIgnoreCase(RepConstants.delete_operation)) {
                //  Write the delete element in XML file.
                makeDeleteElement(bw, noOfColumns, xmlWriter, rows,
                                  toWriteDeleElement, EnCodedcols);
              }
//            System.out.println((i++)+"  time taken in insert  "+ (System.currentTimeMillis()-time));
            }
            while (rows.next());
            bw.write("</tableName>\r\n");
          }

          // change last_sync id in bookmarks table
//        updateBookMarkLastSyncId(shadowTable, tableName, remote_Pub_Sub_Name);
        }
        catch (Exception ex) {
          RepConstants.writeERROR_FILE(ex);
          RepException rep = null;
          if (ex instanceof RepException) {
            throw (RepException) ex;
          }
          else {
            rep = new RepException("REP087", new Object[] {ex.getMessage()});
            rep.setStackTrace(ex.getStackTrace());
            throw rep;
          }
        }
        finally {
          try {
            if (rows != null) {
              Statement st = rows.getStatement();
              rows.close();
              st.close();

            }
            if (primaryPreparedStatement != null) {
              primaryPreparedStatement.close();
            }
            if (commonPreparedStatement != null) {
              commonPreparedStatement.close();
            }
            if (commonStatement != null) {
              commonStatement.close();
            }
          }
          catch (SQLException ex1) {
           //ignore SQLException
          }
        }

      }

      for (int i = 0; i < noOfTables; i++) {
        try {
          repTable = ( (RepTable) pubRepTables.get(i));
          if (repTable.getCreateShadowTable().equalsIgnoreCase(RepConstants.NO))
            continue;
          tableName = repTable.getSchemaQualifiedName().toString();
          EnCodedcols = PathHandler.getEncodedColumns(tableName);
          shadowTable = RepConstants.shadow_Table(repTable.
                                                  getSchemaQualifiedName().
                                                  toString());
          viewedIds = new ArrayList();
          long lastId = getLastSyncId(remote_Pub_Sub_Name, tableName);
          primaryColNames = repTable.getPrimaryColumns();
          xmlWriter.setNoOFPrimaryColumnNumber(primaryColNames.length);
          primaryPreparedStatement = dbDataypeHandler.
              makePrimaryPreperedStatement(primaryColNames, shadowTable,
                                           local_pub_sub_name);
          commonPreparedStatement = makeCommonPreparedStatement(shadowTable);
          //statement whose resulset is set in tracer in getlastrecord in update case
          commonStatement = pub_sub_Connection.createStatement();

          // deleting records from shadow table with primaryColumns of Main Table as NULL
//         deleteRecordsFromShadowTableWithNullPk();
          String query = "Select * from " + shadowTable + " where " +
              RepConstants.shadow_sync_id1 +
              " > " + lastId + " and " + RepConstants.shadow_serverName_n +
              " != '" + remoteServerName + "' order by " +
              RepConstants.shadow_sync_id1;

          filterClause = repTable.getFilterClause();
          rows = getResultSet(query);
          primaryColumnTypes = new int[primaryColNames.length];
          ResultSetMetaData rsmt = rows.getMetaData();
          int noOfColumns = rsmt.getColumnCount();
          String operation;
          if (rows.next()) {
            bw.write("<tableName>");
            bw.write(tableName);
            if (usedActualTables.contains(tableName))
              usedActualTables.add(tableName);
            do {
//            int icount =0;
//            long time = System.currentTimeMillis();
              operation = rows.getString(RepConstants.shadow_operation3);
              if (operation.equalsIgnoreCase(RepConstants.insert_operation)) {
                // Write the insert element in XML file.
                makeInsertElement(bw, operation, noOfColumns, xmlWriter, rows,
                                  rsmt, remoteServerName, toWriteInsertElement,
                                  EnCodedcols);
              }

              else if (operation.equalsIgnoreCase(RepConstants.update_operation)) {
                // Write the update element in XML file.
                makeUpdateElement(bw, noOfColumns, rsmt, xmlWriter, rows,
                                  shadowTable, remoteServerName,
                                  toWriteUpdateElement, EnCodedcols);
              }
//            System.out.println((i++)+"  time taken in insert  "+ (System.currentTimeMillis()-time));
            }
            while (rows.next());
            bw.write("</tableName>\r\n");
          }

          // change last_sync id in bookmarks table
//                   updateBookMarkLastSyncId(shadowTable, tableName, remote_Pub_Sub_Name);
        }
        finally {
          try {
            if (rows != null) {
              Statement st = rows.getStatement();
              rows.close();
              st.close();
              if (primaryPreparedStatement != null) {
                primaryPreparedStatement.close();
              }
              if (commonPreparedStatement != null) {
                commonPreparedStatement.close();
              }
              if (commonStatement != null) {
                commonStatement.close();
              }
            }
          }
          catch (SQLException ex1) {
            //Ignore Exception
          }
        }

      }
//commented By Nancy on 29-03-2005
//to avoid record skipping during realTime Scheduling
      /* for (int i = 0; i < noOfTables; i++) {
        repTable = ( (RepTable) pubRepTables.get(i));
        tableName = repTable.getSchemaQualifiedName().toString();
         shadowTable = RepConstants.shadow_Table(repTable.getSchemaQualifiedName().toString());
         updateBookMarkLastSyncId(shadowTable, tableName, remote_Pub_Sub_Name,lastIdArray[i]);
       }*/

      bw.write("</root>");
      bw.close();
      fos.close();

      // making zip file from xml file
      ZipHandler.makeZip(zipFileURL, xmlFileURL, xmlFileName);
      // writing zip file on socket
//      writeZIPFileOnClientSocket(socket, zipFileURL);
      new WriteOnSocket(socket, zipFileURL, xmlFileURL, DeleteXML).start();
      return new Object[] {
          usedActualTables, lastIdArray};
    }
    catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      RepException rep = new RepException("REP057", new Object[] {ex.getMessage()});
      rep.setStackTrace(ex.getStackTrace());
      throw rep;
    }
  }

  /**
   * prepared statement for getting common record for the copmmon id
   * @param shadowTable
   * @return
   * @throws SQLException
   */
  private PreparedStatement makeCommonPreparedStatement(String shadowTable) throws
      SQLException, RepException {
    StringBuffer query = new StringBuffer();
    query.append(" select * from ");
    query.append(shadowTable);
    query.append(" where ");
    query.append(RepConstants.shadow_sync_id1);
    query.append(" > ");
    query.append("? ");
    query.append(" and ");
    query.append(RepConstants.shadow_common_id2);
    query.append(" = ");
    query.append("? ");
    query.append(" and ");
    query.append(RepConstants.shadow_status4);
    query.append(" = '");
    query.append(RepConstants.afterUpdate);
    query.append("' ");
    Connection pub_sub_Connection = connectionPool.getConnection(
        local_pub_sub_name);
    //check (dbDataypeHandler.getvendorName() == Utility.DataBase_DB2) added by nancy to handle blob clob case
//System.out.println("commonPreapredStatement::"+query.toString());
    if (dbDataypeHandler.getvendorName() == Utility.DataBase_PostgreSQL ||
        dbDataypeHandler.getvendorName() == Utility.DataBase_DB2) {
      return pub_sub_Connection.prepareStatement(query.toString());
    }
    return pub_sub_Connection.prepareStatement(query.toString(),
                                               ResultSet.
                                               TYPE_SCROLL_INSENSITIVE,
                                               ResultSet.CONCUR_UPDATABLE);

  }

  /**
   * returns last synchronization id from bookmark TableName
   * @param remote_Pub_Sub_subName
   * @param tableName
   * @return
   */
  private long getLastSyncId(String remote_Pub_Sub_subName, String tableName) {
    ResultSet rs = null;
    try {
      StringBuffer query = new StringBuffer();
      query.append(" Select ").append(RepConstants.bookmark_lastSyncId4).append(
          " from ")
          .append(dbDataypeHandler.getBookMarkTableName()).append(" where ").
          append(RepConstants.bookmark_LocalName1)
          .append(" = '").append(local_pub_sub_name).append("' and ").append(
          RepConstants.bookmark_RemoteName2)
          .append(" = '").append(remote_Pub_Sub_subName).append("'")
          .append(" and ").append(RepConstants.bookmark_TableName3).append(
          "= '")
          .append(tableName).append("'");
//      ResultSet rs = getResultSet(query.toString());
      log.debug(query.toString());

    rs = getResultSet(query.toString());

//      Long lastId = null;
      Object lastIdObj = null;
      if (rs.next()) {
        // It may rise class cast exception and require casting after check object type
        // use insetanceof java feature for multiple handling
        lastIdObj = rs.getObject(RepConstants.bookmark_lastSyncId4);
      }
      long lastId = 0;
      if (lastIdObj instanceof Long) {
        lastId = lastIdObj == null ? 0 : ( (Long) lastIdObj).longValue();
      }
      else if (lastIdObj instanceof BigDecimal) {
        lastId = lastIdObj == null ? 0 : ( (BigDecimal) lastIdObj).longValue();
      }
      else if (lastIdObj instanceof Integer) {
        lastId = lastIdObj == null ? 0 : ( (Integer) lastIdObj).longValue();
      }
      else if (lastIdObj instanceof Double) {
        lastId = lastIdObj == null ? 0 : ( (Double) lastIdObj).longValue();
      }
      return lastId;
//      boolean flag = rs.next();
//      long lastId = rs.getLong(RepConstants.bookmark_lastSyncId4);
//      return flag ?
//          (new Long(lastId) == null ? new Long(0).longValue() : lastId) :
//          new Long(0).longValue();

    }
    catch (Exception ex) {
      return 0;
    }
    finally {
      try {
        if (rs != null) {
          Statement st = rs.getStatement();
          rs.close();
          if (st != null)
            st.close();
        }
      }
      catch (SQLException ex1) {
      }
    }
  }

  private ResultSet getResultSet(String nonPreparedQuery) throws Exception {
    Connection pub_sub_Connection = connectionPool.getConnection(
        local_pub_sub_name);
    Statement st;
    if (dbDataypeHandler.getvendorName() == Utility.DataBase_PostgreSQL ||
        dbDataypeHandler.getvendorName() == Utility.DataBase_Cloudscape ||
        dbDataypeHandler.getvendorName() == Utility.DataBase_DB2) {
      st = pub_sub_Connection.createStatement();
    }
    else {
      st = pub_sub_Connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                                              ResultSet.CONCUR_READ_ONLY);
    }
    log.debug(nonPreparedQuery);
    return st.executeQuery(nonPreparedQuery);
//   return st.getResultSet();
  }

  /**
   * creates and XML Insert Element
   * @param operation
   * @param noOfColumns
   * @param xmlWriter
   * @param rows_I
   * @param rsmt
   * @param remoteServerName
   * @throws SQLException
   * @throws IOException
   * @throws RepException
   */
  private void makeInsertElement(Writer bw, String operation, int noOfColumns,
                                 XMLWriter xmlWriter, ResultSet rows_I,
                                 ResultSetMetaData rsmt,
                                 String remoteServerName, boolean willInclude,
                                 ArrayList encodedCols) throws SQLException,
      IOException, RepException {
    Object Uid = rows_I.getObject(RepConstants.shadow_sync_id1);

    if (viewedIds.contains(Uid)) {
      viewedIds.remove(Uid);
      return;
    }

    Object[] primaryColValues = new Object[primaryColNames.length];

    for (int i = 0; i < primaryColNames.length; i++) {
      primaryColValues[i] = rows_I.getObject(primaryColNames[i]);
    }

    if (USE_getLastRecord) { // can comment this code if no error comes
      makeInsertElement_GetLastRecord(bw, operation, noOfColumns, xmlWriter,
                                      rows_I, rsmt, remoteServerName,
                                      primaryColValues, willInclude,
                                      encodedCols);
    }
    else {
      if (filterResultSet(rows_I)) {
        writeInsertElement(bw, operation, noOfColumns, xmlWriter, rows_I, rsmt,
                           primaryColValues, willInclude, encodedCols);
      }
    }
  }

  /**
   * creates and XML Insert Element
   * iF USED with getLast Records for shadow Table
   * @param operation
   * @param noOfColumns
   * @param xmlWriter
   * @param rows_I
   * @param rsmt
   * @param remoteServerName
   * @param primaryColValues
   * @throws IOException
   * @throws RepException
   * @throws SQLException
   */
  private void makeInsertElement_GetLastRecord(Writer bw, String operation,
                                               int noOfColumns,
                                               XMLWriter xmlWriter,
                                               ResultSet rows_I,
                                               ResultSetMetaData rsmt,
                                               String remoteServerName,
                                               Object[] primaryColValues,
                                               boolean willInclude,
                                               ArrayList encodedCols) throws
      IOException, RepException, SQLException {
    Object UId = rows_I.getObject(RepConstants.shadow_sync_id1);
    Tracer tracer = new Tracer();
    getLastRecord(primaryColValues, UId, tracer, remoteServerName);
    if (!tracer.recordFound) {
      if (filterResultSet(rows_I)) {
        writeInsertElement(bw, operation, noOfColumns, xmlWriter, rows_I, rsmt,
                           primaryColValues, willInclude, encodedCols);
      }
    }
    else if (tracer.type.equalsIgnoreCase(RepConstants.update_operation)) {
      ResultSet rsTracer = tracer.rs;
      if (filterResultSet(rsTracer)) {
        writeInsertElement(bw, operation, noOfColumns, xmlWriter, rsTracer,
                           rsTracer.getMetaData(), tracer.primaryKeyValues,
                           willInclude, encodedCols);
      }
    }
//    else if (tracer.type.equalsIgnoreCase(RepConstants.delete_operation)) {
//    }
  }

  /**
   * creates a XML element for Insert
   * @param operation
   * @param noOfColumns
   * @param xmlWriter
   * @param rows_I
   * @param rsmt
   * @param primaryColValues
   * @throws SQLException
   * @throws IOException
   * @throws RepException
   */
  private void writeInsertElement(Writer bw, String operation, int noOfColumns,
                                  XMLWriter xmlWriter, ResultSet rows_I,
                                  ResultSetMetaData rsmt,
                                  Object[] primaryColValues,
                                  boolean willInclude, ArrayList encodedCols) throws
      SQLException, IOException, RepException {
    /*
          // just an example not mandat implementation.
          <operation>Insert
     <row>
       <columnName name="c1">
          <![CDATA[ value value value ]]>
      </columnName>

      <columnName name="c2"> // suppose column is of blob/clob type
         <start> start position index</start>
         <length> number of characters</length>
      </columnName>
     </row>
     <primary>
       <primaryColumnNames name="p1">
              <![CDATA[ value value value ]]>
       </primaryColumnNames>
     </primary>
          </operation>
     */

    if (willInclude) {
      bw.write("<operation>");
      bw.write(operation);
      xmlWriter.writeRowElement(noOfColumns, rows_I, rsmt, primaryColNames,
                                primaryColValues, tableName, encodedCols);
      xmlWriter.writePrimaryKeyElement(primaryColNames, primaryColValues,
                                       encodedCols);
      bw.write("</operation>\r\n");
    }
  }

  /**
   * Returns a resutSet for the other common record corresponding to the common Id passed.
   * @param rs
   * @param tracer
   * @return
   * @throws SQLException
   */
  private ResultSet getOtherCommonRecord(ResultSet rs, Tracer tracer) throws
      SQLException, RepException {
    // so that if the method is called recursively then result set created is not closed.
//    commonPreparedStatement = makeCommonPreparedStatement(shadowTable);
    Object commonId = rs.getObject(RepConstants.shadow_common_id2);
    String serverName = rs.getString(RepConstants.shadow_serverName_n);
    Object UId = rs.getObject(RepConstants.shadow_sync_id1);
    if (!serverName.equalsIgnoreCase(remoteServerName)) {
      viewedIds.add(UId);
    }
    commonPreparedStatement.setObject(1, UId);
    commonPreparedStatement.setObject(2, commonId);

     ResultSet resultSet = commonPreparedStatement.executeQuery();

    return resultSet;
  }

  /**
   * creates and XML Element for Update
   * @param noOfColumns
   * @param rsmt
   * @param xmlWriter
   * @param rows_U
   * @param shadowTableName
   * @param remoteServerName
   * @throws java.lang.Exception
   */
  private void makeUpdateElement(Writer bw, int noOfColumns,
                                 ResultSetMetaData rsmt
                                 , XMLWriter xmlWriter, ResultSet rows_U,
                                 String shadowTableName,
                                 String remoteServerName, boolean willInclude,
                                 ArrayList encodedCols) throws
      Exception {
    Tracer tracer = new Tracer();
    ResultSet rs = null;
    try {
      Object Uid = rows_U.getObject(RepConstants.shadow_sync_id1);
      if (viewedIds.contains(Uid)) {
        viewedIds.remove(Uid);
        return;
      }
      if (rows_U.getString(RepConstants.shadow_status4).equalsIgnoreCase(
          RepConstants.afterUpdate)) {
        throw new Exception(" NOT POSSIBLE ");
      }

      Object[] oldPrimaryColValues = new Object[primaryColNames.length];
      for (int i = 0; i < primaryColNames.length; i++) {
        oldPrimaryColValues[i] = rows_U.getObject(primaryColNames[i]);
      }
      rs = getOtherCommonRecord(rows_U, tracer);
      boolean recordPresent = rs.next();
      Object[] primaryColValues = new Object[primaryColNames.length];
      for (int i = 0; i < primaryColNames.length; i++) {
        primaryColValues[i] = rs.getObject(primaryColNames[i]);
      }
      String serverName = rs.getString(RepConstants.shadow_serverName_n);
      Object currentUId = rs.getObject(RepConstants.shadow_sync_id1);
      if (!serverName.equalsIgnoreCase(remoteServerName)) {
        viewedIds.add(currentUId);
      }

      if (USE_getLastRecord) {
        makeUpdateElement_getLastRecord(bw, noOfColumns, xmlWriter, rows_U,
                                        remoteServerName, oldPrimaryColValues,
                                        tracer, rs, primaryColValues,
                                        currentUId, willInclude, encodedCols);
      }
      else {
        makeUpdateEment_ForFilter(bw, noOfColumns, xmlWriter, rows_U,
                                  oldPrimaryColValues, rs, primaryColValues,
                                  tracer, willInclude, encodedCols);
      }
    }
    finally {
      if (tracer.rs != null) {
        ResultSet rstemp = tracer.rs;
//        Statement stmt = rstemp.getStatement();
        rstemp.close();
//        stmt.close();
      }
      if (rs != null) {
//        Statement st = rs.getStatement();
        rs.close();
//        st.close();

      }
    }

  }

  /**
   * creates an XML element for update With filter clause
   * @param noOfColumns
   * @param xmlWriter
   * @param rows_U
   * @param oldPrimaryColValues
   * @param newRs
   * @param primaryColValues
   * @throws java.lang.Exception
   */
  private void makeUpdateEment_ForFilter(Writer bw, int noOfColumns,
                                         XMLWriter xmlWriter,
                                         ResultSet rows_U,
                                         Object[] oldPrimaryColValues,
                                         ResultSet newRs,
                                         Object[] primaryColValues,
                                         Tracer tracer, boolean willInclude,
                                         ArrayList encodedCols) throws
      Exception {
    boolean oldFilterResult = filterResultSet(rows_U);
    boolean newFilterResult = filterResultSet(newRs);
    if (oldFilterResult && newFilterResult) {
      writeUpdateElement(bw, noOfColumns, xmlWriter, newRs, newRs.getMetaData(),
                         oldPrimaryColValues, primaryColValues, rows_U, tracer,
                         willInclude, encodedCols);
    }
    else if (oldFilterResult && (!newFilterResult)) {
      writeDeleteElement(bw, xmlWriter, rows_U, oldPrimaryColValues,
                         willInclude, encodedCols);
    }
    else if ( (!oldFilterResult) && newFilterResult) {
      writeInsertElement(bw, RepConstants.insert_operation,
                         newRs.getMetaData().getColumnCount(), xmlWriter, newRs,
                         newRs.getMetaData(), primaryColValues, willInclude,
                         encodedCols);
    }
  }

  /**
   * creates an XML element for Update is Get Last record from shadow Table is Used.
   * @param noOfColumns
   * @param xmlWriter
   * @param rows_U
   * @param remoteServerName
   * @param oldPrimaryColValues
   * @param tracer
   * @param rs
   * @param primaryColValues
   * @param currentUId
   * @throws java.lang.Exception
   */
  private void makeUpdateElement_getLastRecord(Writer bw, int noOfColumns,
                                               XMLWriter xmlWriter,
                                               ResultSet rows_U,
                                               String remoteServerName,
                                               Object[] oldPrimaryColValues,
                                               Tracer tracer, ResultSet rs,
                                               Object[] primaryColValues,
                                               Object currentUId,
                                               boolean willInclude,
                                               ArrayList encodedCols) throws
      Exception {
    getLastRecord(primaryColValues, currentUId, tracer, remoteServerName);

    if (!tracer.recordFound) {
      makeUpdateEment_ForFilter(bw, noOfColumns, xmlWriter, rows_U,
                                oldPrimaryColValues, rs, primaryColValues,
                                tracer, willInclude, encodedCols);
    }
    else if (tracer.type.equalsIgnoreCase(RepConstants.delete_operation)) {
      if (filterResultSet(rows_U)) {
        writeDeleteElement(bw, xmlWriter, rows_U, oldPrimaryColValues,
                           willInclude, encodedCols);
      }
    }
    else if (tracer.type.equalsIgnoreCase(RepConstants.update_operation)) {
      ResultSet rsTracer = tracer.rs;
      makeUpdateEment_ForFilter(bw, noOfColumns, xmlWriter, rows_U,
                                oldPrimaryColValues, rsTracer,
                                primaryColValues, tracer, willInclude,
                                encodedCols);

    }
    else {
      ResultSet rsTracer = tracer.rs;
      if (rsTracer == null) {
        rsTracer = rs;
        tracer.primaryKeyValues = primaryColValues;
      }
      if (filterResultSet(rsTracer)) {
        writeInsertElement(bw, RepConstants.insert_operation, noOfColumns,
                           xmlWriter, rsTracer,
                           rsTracer.getMetaData(), tracer.primaryKeyValues,
                           willInclude, encodedCols);

      }
    }

  }

  // Update Operation Only
  private void writeUpdateElement(Writer bw, int noOfColumns,
                                  XMLWriter xmlWriter,
                                  ResultSet rs,
                                  ResultSetMetaData rsmt,
                                  Object[] primaryKeyValues,
                                  Object[] newprimaryKeyValues,
                                  ResultSet originalResultSet,
                                  Tracer tracer, boolean willInclude
                                  , ArrayList encodedCols
                                  ) throws Exception {
    if (willInclude) {
      bw.write("<operation>");
      bw.write(RepConstants.update_operation);
      if (tracer.primaryKeyValues != null) {
        xmlWriter.writeRowElementForUpdate(noOfColumns, rs, rsmt,
                                           originalResultSet, primaryColNames,
                                           tracer.primaryKeyValues, tableName,
                                           encodedCols);
      }
      else {
        xmlWriter.writeRowElementForUpdate(noOfColumns, rs, rsmt,
                                           originalResultSet, primaryColNames,
                                           newprimaryKeyValues, tableName,
                                           encodedCols);
      }

      xmlWriter.writePrimaryKeyElement(primaryColNames, primaryKeyValues,
                                       encodedCols);
      bw.write("</operation>\r\n");
    }
  }

  // Updated record is deleted
  private void writeDeleteElement(Writer bw, XMLWriter xmlWriter, ResultSet rs,
                                  Object[] primaryKeyValues,
                                  boolean willInclude, ArrayList encodedCols) throws
      Exception {
    if (!willInclude) {
      bw.write("<operation>");
      bw.write(RepConstants.delete_operation);
      xmlWriter.writePrimaryKeyElement(primaryColNames, primaryKeyValues,
                                       encodedCols);
      bw.write("</operation>\r\n");
    }
  }

// writes a n delete Element in XML file
  private void makeDeleteElement(Writer bw, int noOfColumns,
                                 XMLWriter xmlWriter, ResultSet rows_D,
                                 boolean willInclude, ArrayList encodedCols) throws
      Exception {
    Object Uid = rows_D.getObject(RepConstants.shadow_sync_id1);
    if (viewedIds.contains(Uid)) {
      viewedIds.remove(Uid);
      return;
    }

    Object[] primaryColValues = new Object[primaryColNames.length];
    for (int i = 0; i < primaryColNames.length; i++) {
      primaryColValues[i] = rows_D.getObject(primaryColNames[i]);
    }

    if (filterResultSet(rows_D)) {
      writeDeleteElement(bw, xmlWriter, rows_D, primaryColValues, willInclude,
                         encodedCols);
    }
  }

  /**
   * get last unique id from the corresponding the shadow Table
   * @param shadowTable
   * @return
   * @throws java.lang.Exception
   */
  /*  private Object getLastUIDFromShadowTable(String shadowTable) throws Exception {
      StringBuffer query = new StringBuffer();
      ResultSet rs = null;
      try {
   query.append(" Select max(").append(RepConstants.shadow_sync_id1).append(
            ")").append(" from ").append(shadowTable);
        Connection pub_sub_Connection = connectionPool.getConnection(local_pub_sub_name);
   rs = pub_sub_Connection.createStatement().executeQuery(query.toString());
        boolean flag = rs.next();
        Object lastId = rs.getObject(1);
        return flag ? (lastId == null ? new Long(0) : lastId) : new Long(0);
      }
      finally {
        Statement st = rs.getStatement();
        rs.close();
        st.close();
      }
    }
   */

  private Object getLastUIDFromShadowTable(String shadowTable) throws Exception {
    StringBuffer query = new StringBuffer();
    ResultSet rs = null;
    boolean flag = false;
    Object lastId = null;
    try {
//RepConstants.writeMessage_FILE(" pub_sub_Connection ="+pub_sub_Connection);
      query.append(" Select max(").append(RepConstants.shadow_sync_id1).append(
          ")").append(" from ").append(shadowTable);
      Connection pub_sub_Connection = connectionPool.getConnection(
          local_pub_sub_name);

      rs = pub_sub_Connection.createStatement().executeQuery(query.toString());

//RepConstants.writeMessage_FILE("getLastUIDFromShadowTable rs :: "+rs);
      flag = rs.next();
      lastId = rs.getObject(1);
    }
    catch (Exception ex) {
      RepConstants.writeERROR_FILE(ex);
    }
    finally {
      if (rs != null) {
// RepConstants.writeMessage_FILE(" finally rs : " + rs);
        Statement st = rs.getStatement();
//RepConstants.writeMessage_FILE(" finally st : " + st);
        rs.close();
        if (st != null)
          st.close();
      }
    }
    return flag ? (lastId == null ? new Long(0) : lastId) : new Long(0);
  }

  /**
   * update the bookmarks table after XML file for synchronisation is ready with the last UniqueID.
   * @param shadowTable
   * @param tableName
   * @param remote_Pub_Sub_Name
   * @throws java.lang.Exception
   */
  private void updateBookMarkLastSyncId(String shadowTable, String tableName,
                                        String remote_Pub_Sub_Name,
                                        Object lastId) throws Exception {

    Statement stmt = null;
    try {
//        Object lastId = getLastUIDFromShadowTable(shadowTable);
      String updateQuery = "update " + dbDataypeHandler.getBookMarkTableName() +
          " set " +
          RepConstants.bookmark_lastSyncId4 + "=" + lastId + " where  " +
          RepConstants.bookmark_LocalName1 + " = '" + local_pub_sub_name +
          "' and " +
          RepConstants.bookmark_RemoteName2 + " = '" + remote_Pub_Sub_Name +
          "' and " +
          RepConstants.bookmark_TableName3 + " = '" + tableName + "' ";
      Connection pub_sub_Connection = connectionPool.getConnection(
          local_pub_sub_name);
      stmt = pub_sub_Connection.createStatement();
      int updateNumber = stmt.executeUpdate(updateQuery);
    }
    finally {
      if (stmt != null)
        stmt.close();
    }
  }

  /**
   * writes the zip file on the other nodes socket.
   * @param s
   * @param xmlFilePath
   * @throws IOException
   */
  private void writeZIPFileOnClientSocket(Socket s, String xmlFilePath) throws
      IOException {
    FileInputStream fis = new FileInputStream(xmlFilePath);
    OutputStream sos = s.getOutputStream();
    s.setSendBufferSize(Integer.MAX_VALUE);
    BufferedOutputStream bos = new BufferedOutputStream(sos);

    byte[] buf = new byte[1024];
    int len = 0;
    int kb = 0;
    while ( (len = fis.read(buf)) > 0) {
      bos.write(buf, 0, len);
    }
    bos.flush();
    bos.close();
    sos.close();
    fis.close();
  }

  public Object[] createXMLFile(String xmlFileURL, String zipFielURL,
                                String xmlFileName, String remote_Pub_Sub_Name,
                                ArrayList subRepTables
                                , String clientServerName, String address,
                                int portNo, int noOfTables, boolean DeleteXML,
                                String local_pub_sub_name) throws
      RepException {
    Socket socket = null;
    try {
      socket = new Socket(address, portNo);
    }
    catch (IOException ex) {
      log.error(ex.getMessage(), ex);
      throw new RuntimeException(ex.getMessage());
    }
    return createXMLFile(xmlFileURL, zipFielURL, xmlFileName,
                         remote_Pub_Sub_Name, subRepTables, clientServerName,
                         socket, noOfTables, DeleteXML, local_pub_sub_name);
  }

  /**
   * deletes all records from the shadow tablw having null primary key set corresponding to the actual table.
   * @throws SQLException
   */
  private void deleteRecordsFromShadowTableWithNullPk() throws SQLException,
      RepException {
    StringBuffer query = new StringBuffer();
    query.append(" delete from ").append(shadowTable).append(" where ");
    for (int i = 0; i < primaryColNames.length; i++) {
      if (i > 0) {
        query.append(" and ");
      }
      query.append(primaryColNames[i]).append(" is null ");
    }
    Connection pub_sub_Connection = connectionPool.getConnection(
        local_pub_sub_name);
    Statement st = null;
    try {
      st = pub_sub_Connection.createStatement();
      st.executeUpdate(query.toString());
    }
    finally {
      if (st != null)
        st.close();
    }
  }

  /**
   * filters the result set with the filter clause given.
   * @param rs
   * @return
   * @throws SQLException
   * @throws SQLException
   */
  private boolean filterResultSet(ResultSet rs) throws SQLException,
      SQLException, RepException {
    StringBuffer query = new StringBuffer();
    if ( (filterClause != null) && (!filterClause.trim().equals(""))) {
      Object Uid = rs.getObject(RepConstants.shadow_sync_id1);
      query.append("Select * from ").append(shadowTable).append(" where ").
          append(
          RepConstants.shadow_sync_id1)
          .append(" = ").append(Uid).append(
          " and ").append(RepConstants.shadow_serverName_n).append(" != '")
          .append(remoteServerName).append("' and ").append(filterClause);
      PreparedStatement pstmt = null;
      ResultSet filteredSet = null;
      try {
        Connection pub_sub_Connection = connectionPool.getConnection(
            local_pub_sub_name);
        pstmt = pub_sub_Connection.prepareStatement(query.toString());
        if (parameters != null) {
          for (int i = 0, size = parameters.length; i < size; i++) {
            pstmt.setString(i + 1, parameters[0]);
          }
        }
        filteredSet = pstmt.executeQuery();
        boolean f1 = filteredSet.next();
        return f1;
      }
      finally {
        if (filteredSet != null)
          filteredSet.close();
        if (pstmt != null)
          pstmt.close();
      }
    }
    else {
      return true;
    }
  }

  /**
   * used for debugging. shows values stored in a resultSet.
   * @param rs
   * @throws SQLException
   */
//  public static void showResultSet(ResultSet rs) throws SQLException {
//    ResultSetMetaData metaData = rs.getMetaData();
//    int columnCount = metaData.getColumnCount();
//    Object[] displayColumn = new Object[columnCount];
//    for (int i = 1; i <= columnCount; i++)
//      displayColumn[i - 1] = metaData.getColumnName(i);
//    System.out.println(Arrays.asList(displayColumn));
//    while (rs.next()) {
//      Object[] columnValues = new Object[columnCount];
//      for (int i = 1; i <= columnCount; i++)
//        columnValues[i - 1] = rs.getObject(i);
//      System.out.println(Arrays.asList(columnValues));
//    }
//  }
// changed by neeraj sir as stackoverflow was coming
//  when a single row was updated more than 2500 times(ANZ case) now getlastrecord not called recursively
// but executed in infinite loop

// ********************
  private boolean getLastRecord(Object[] primaryColValues0, Object UId0,
                                Tracer tracer, String remoteServerName) throws
      RepException, SQLException {
    boolean recordFound = false;
    Object[] primaryColValues = primaryColValues0;
    Object[] oldPrimaryColValues = primaryColValues0;
    Object UId = UId0;
    String returnOperation = "";
    while (true) {
      primaryPreparedStatement.setObject(1, UId);
      for (int i = 0; i < primaryColValues.length; i++) {
        primaryPreparedStatement.setObject(i + 2, primaryColValues[i]);
      }
      ResultSet rs = primaryPreparedStatement.executeQuery();
      //        execute prepared statement and check the record in result set
      if (!rs.next()) {
        rs.close();
        break;
      }
      else {
        String operation = rs.getString(RepConstants.shadow_operation3);
        String serverName = rs.getString(RepConstants.shadow_serverName_n);
        if (operation.equalsIgnoreCase(RepConstants.delete_operation)) {
                 // Delete Operation
          if (!serverName.equalsIgnoreCase(remoteServerName)) {
            Object currentUId = rs.getObject(RepConstants.shadow_sync_id1);
            viewedIds.add(currentUId);
            recordFound = true;
            returnOperation = RepConstants.delete_operation;
          }
          break;
        }
        else if (operation.equalsIgnoreCase(RepConstants.update_operation)) {
          ResultSet resultSet = getOtherCommonRecord(rs, tracer);
          resultSet.next();
          oldPrimaryColValues = primaryColValues;
          primaryColValues = new Object[primaryColNames.length];
          for (int i = 0; i < primaryColNames.length; i++) {
            primaryColValues[i] = resultSet.getObject(primaryColNames[i]);
          }
          serverName = resultSet.getString(RepConstants.shadow_serverName_n);
          UId = resultSet.getObject(RepConstants.shadow_sync_id1);
          if (!serverName.equalsIgnoreCase(remoteServerName)) {
            viewedIds.add(UId);
          }
          recordFound = true;
          returnOperation = RepConstants.update_operation;
          resultSet.close();
        }
        else {
          // for insert type operation case produced by yana's table having triggers       .
          primaryColValues = new Object[primaryColNames.length];
          for (int i = 0; i < primaryColNames.length; i++) {
            primaryColValues[i] = rs.getObject(primaryColNames[i]);
          }
          serverName = rs.getString(RepConstants.shadow_serverName_n);
          UId = rs.getObject(RepConstants.shadow_sync_id1);
          if (!serverName.equalsIgnoreCase(remoteServerName)) {
            viewedIds.add(UId);
          }
          recordFound = true;
          returnOperation = RepConstants.insert_operation;
        }
      }
    }
    if (!recordFound)
      return recordFound;
    tracer.recordFound = recordFound;
    tracer.type = returnOperation;
    if (returnOperation.equalsIgnoreCase(RepConstants.update_operation)) {
      tracer.primaryKeyValues = primaryColValues;
      String finalResultSetQuery = "select * from " + shadowTable + " where " +
          RepConstants.shadow_sync_id1 + " = " + UId;
      ResultSet resultSet = commonStatement.executeQuery(finalResultSetQuery);
      resultSet.next();
      tracer.rs = resultSet;
    }
    return recordFound;
  }




  }

