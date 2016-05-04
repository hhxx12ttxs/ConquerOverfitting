/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.adempiere.exceptions.DBException;
import org.compiere.util.CLogMgt;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Ini;
import org.compiere.util.MSort;
import org.compiere.util.SecureEngine;
import org.compiere.util.Trx;
import org.compiere.util.ValueNamePair;

/**
 *	Grid Table Model for JDBC access including buffering.
 *  <pre>
 *		The following data types are handled
 *			Integer		for all IDs
 *			BigDecimal	for all Numbers
 *			Timestamp	for all Dates
 *			String		for all others
 *  The data is read via r/o resultset and cached in m_buffer. Writes/updates
 *  are via dynamically constructed SQL INSERT/UPDATE statements. The record
 *  is re-read via the resultset to get results of triggers.
 *
 *  </pre>
 *  The model maintains and fires the requires TableModelEvent changes,
 *  the DataChanged events (loading, changed, etc.)
 *  as well as Vetoable Change event "RowChange"
 *  (for row changes initiated by moving the row in the table grid).
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: GridTable.java,v 1.9 2006/08/09 16:38:25 jjanke Exp $
 * 
 * @author Teo Sarca, SC ARHIPAC SERVICE SRL
 * 			<li>BF [ 1901192 ] LogMigrationScripts: GridTable.dataSave: manual update
 *			<li>BF [ 1943682 ] Copy Record should not copy IsApproved and IsGenerated
 *			<li>BF [ 1949543 ] Window freeze if there is a severe exception
 *			<li>BF [ 1984310 ] GridTable.getClientOrg() doesn't work for AD_Client/AD_Org
 *  @author victor.perez@e-evolution.com,www.e-evolution.com
 *  		<li>BF [ 2910358 ] Error in context when a field is found in different tabs.
 *  			https://sourceforge.net/tracker/?func=detail&aid=2910358&group_id=176962&atid=879332
 *     		<li>BF [ 2910368 ] Error in context when IsActive field is found in different
 *  			https://sourceforge.net/tracker/?func=detail&aid=2910368&group_id=176962&atid=879332
 */
public class GridTable extends AbstractTableModel
	implements Serializable
{
	/**
	 * generated
	 */
	private static final long serialVersionUID = 7799823493936826600L;
	
	public static final String DATA_REFRESH_MESSAGE = "Refreshed";

	/**
	 *	JDBC Based Buffered Table
	 *
	 *  @param ctx Properties
	 *  @param AD_Table_ID table id
	 *  @param TableName table name
	 *  @param WindowNo window no
	 *  @param TabNo tab no
	 *  @param withAccessControl    if true adds AD_Client/Org restrictuins
	 */
	public GridTable(Properties ctx, int AD_Table_ID, String TableName, int WindowNo, int TabNo,
		boolean withAccessControl)
	{
		this(ctx, AD_Table_ID, TableName, WindowNo, TabNo, withAccessControl, false);
	}

	/**
	 *	JDBC Based Buffered Table
	 *
	 *  @param ctx Properties
	 *  @param AD_Table_ID table id
	 *  @param TableName table name
	 *  @param WindowNo window no
	 *  @param TabNo tab no
	 *  @param withAccessControl    if true adds AD_Client/Org restrictuins
	 *  @param virtual use virtual table mode if table is mark as high volume
	 */
	public GridTable(Properties ctx, int AD_Table_ID, String TableName, int WindowNo, int TabNo,
		boolean withAccessControl, boolean virtual)
	{
		super();
		log.info(TableName);
		m_ctx = ctx;
		m_AD_Table_ID = AD_Table_ID;
		setTableName(TableName);
		m_WindowNo = WindowNo;
		m_TabNo = TabNo;
		m_withAccessControl = withAccessControl;
		m_virtual = virtual;
	}	//	MTable

	private static CLogger		log = CLogger.getCLogger(GridTable.class.getName());
	private Properties          m_ctx;
	private int					m_AD_Table_ID;
	private String 		        m_tableName = "";
	private int				    m_WindowNo;
	/** Tab No 0..				*/
	private int				    m_TabNo;
	private boolean			    m_withAccessControl;
	private boolean			    m_readOnly = true;
	private boolean			    m_deleteable = true;
	//virtual table state variables
	private boolean				m_virtual;
	public static final String CTX_KeyColumnName = "KeyColumnName";
	//

	/**	Rowcount                    */
	private int				    m_rowCount = 0;
	/**	Has Data changed?           */
	private boolean			    m_changed = false;
	/** Index of changed row via SetValueAt */
	private int				    m_rowChanged = -1;
	/** Insert mode active          */
	private boolean			    m_inserting = false;
	/** Inserted Row number         */
	private int                 m_newRow = -1;
	
	/**	Is the Resultset open?      */
	private boolean			    m_open = false;
	/**	Compare to DB before save	*/
	private boolean				m_compareDB = true;		//	set to true after every save

	//	The buffer for all data
	private volatile ArrayList<Object[]>	m_buffer = new ArrayList<Object[]>(100);
	private volatile ArrayList<MSort>		m_sort = new ArrayList<MSort>(100);
	private volatile Map<Integer, Object[]> m_virtualBuffer = new HashMap<Integer, Object[]>(100);
	/** Original row data               */
	private Object[]			m_rowData = null;
	/** Original data [row,col,data]    */
	private Object[]            m_oldValue = null;
	//
	private Loader		        m_loader = null;

	/**	Columns                 		*/
	private ArrayList<GridField>	m_fields = new ArrayList<GridField>(30);
	private ArrayList<Object>	m_parameterSELECT = new ArrayList<Object>(5);
	private ArrayList<Object>	m_parameterWHERE = new ArrayList<Object>(5);

	/** Complete SQL statement          */
	private String 		        m_SQL;
	/** SQL Statement for Row Count     */
	private String 		        m_SQL_Count;
	/** The SELECT clause with FROM     */
	private String 		        m_SQL_Select;
	/** The static where clause         */
	private String 		        m_whereClause = "";
	/** Show only Processed='N' and last 24h records    */
	private boolean		        m_onlyCurrentRows = false;
	/** Show only Not processed and x days				*/
	private int					m_onlyCurrentDays = 1;
	/** Static ORDER BY clause          */
	private String		        m_orderClause = "";
	/** Max Rows to query or 0 for all	*/
	private int					m_maxRows = 0;

	/** Index of Key Column                 */
	private int			        m_indexKeyColumn = -1;
	/** Index of Color Column               */
	private int			        m_indexColorColumn = -1;
	/** Index of Processed Column           */
	private int                 m_indexProcessedColumn = -1;
	/** Index of IsActive Column            */
	private int                 m_indexActiveColumn = -1;
	/** Index of AD_Client_ID Column        */
	private int					m_indexClientColumn = -1;
	/** Index of AD_Org_ID Column           */
	private int					m_indexOrgColumn = -1;

	/** Vetoable Change Bean support    */
	private VetoableChangeSupport   m_vetoableChangeSupport = new VetoableChangeSupport(this);
	private Thread m_loaderThread;
	/** Property of Vetoable Bean support "RowChange" */
	public static final String  PROPERTY = "MTable-RowSave";

	private final static Integer NEW_ROW_ID = Integer.valueOf(-1);
	private static final int DEFAULT_FETCH_SIZE = 200;

	/**
	 *	Set Table Name
	 *  @param newTableName table name
	 */
	public void setTableName(String newTableName)
	{
		if (m_open)
		{
			log.log(Level.SEVERE, "Table already open - ignored");
			return;
		}
		if (newTableName == null || newTableName.length() == 0)
			return;
		m_tableName = newTableName;
	}	//	setTableName

	/**
	 *	Get Table Name
	 *  @return table name
	 */
	public String getTableName()
	{
		return m_tableName;
	}	//	getTableName

	/**
	 *	Set Where Clause (w/o the WHERE and w/o History).
	 *  @param newWhereClause sql where clause
	 *  @param onlyCurrentRows only current rows
	 *  @param onlyCurrentDays how many days back for current
	 *	@return true if where clase set
	 */
	public boolean setSelectWhereClause(String newWhereClause, boolean onlyCurrentRows, int onlyCurrentDays)
	{
		if (m_open)
		{
			log.log(Level.SEVERE, "Table already open - ignored");
			return false;
		}
		//
		m_whereClause = newWhereClause;
		m_onlyCurrentRows = onlyCurrentRows;
		m_onlyCurrentDays = onlyCurrentDays;
		if (m_whereClause == null)
			m_whereClause = "";
		return true;
	}	//	setWhereClause

	/**
	 *	Get record set Where Clause (w/o the WHERE and w/o History)
	 *  @return where clause
	 */
	public String getSelectWhereClause()
	{
		return m_whereClause;
	}	//	getWhereClause

	/**
	 *	Is History displayed
	 *  @return true if history displayed
	 */
	public boolean isOnlyCurrentRowsDisplayed()
	{
		return !m_onlyCurrentRows;
	}	//	isHistoryDisplayed

	/**
	 *	Set Order Clause (w/o the ORDER BY)
	 *  @param newOrderClause sql order by clause
	 */
	public void setOrderClause(String newOrderClause)
	{
		m_orderClause = newOrderClause;
		if (m_orderClause == null)
			m_orderClause = "";
	}	//	setOrderClause

	/**
	 *	Get Order Clause (w/o the ORDER BY)
	 *  @return order by clause
	 */
	public String getOrderClause()
	{
		return m_orderClause;
	}	//	getOrderClause

	/**
	 *	Assemble & store
	 *	m_SQL and m_countSQL
	 *  @return m_SQL
	 */
	private String createSelectSql()
	{
		if (m_fields.size() == 0 || m_tableName == null || m_tableName.equals(""))
			return "";

		//	Create SELECT Part
		StringBuffer select = new StringBuffer("SELECT ");
		for (int i = 0; i < m_fields.size(); i++)
		{
			if (i > 0)
				select.append(",");
			GridField field = (GridField)m_fields.get(i);
			select.append(field.getColumnSQL(true));	//	ColumnName or Virtual Column
		}
		//
		select.append(" FROM ").append(m_tableName);
		m_SQL_Select = select.toString();
		m_SQL_Count = "SELECT COUNT(*) FROM " + m_tableName;
		//BF [ 2910358 ] 
		//Restore the Original Value for Key Column Name based in Tab Context Value
		int parentTabNo = getParentTabNo();
		String parentKey = Env.getContext(m_ctx, m_WindowNo, parentTabNo, GridTab.CTX_KeyColumnName, true);
		String valueKey = null;
		String currKey = null;
		if (parentKey != null && parentKey.length() > 0) {
			valueKey = Env.getContext(m_ctx, m_WindowNo, parentTabNo, parentKey, true);
			currKey = Env.getContext(m_ctx, m_WindowNo, parentKey);
			if (currKey == null)
				currKey = new String("");
			if (valueKey != null && valueKey.length() > 0 && parentKey != null && parentKey.length() > 0 && ! currKey.equals(valueKey))
			{
				Env.setContext(m_ctx, m_WindowNo,  parentKey, valueKey);
			}	
		}
		
		StringBuffer where = new StringBuffer("");
		//	WHERE
		if (m_whereClause.length() > 0)
		{
			where.append(" WHERE ");
			if (m_whereClause.indexOf('@') == -1)
				where.append(m_whereClause);
			else    //  replace variables
			{
				String context = Env.parseContext(m_ctx, m_WindowNo, m_whereClause, false);
				if(context != null && context.trim().length() > 0)
				{
					where.append(context);
				}
				else
				{
					log.log(Level.WARNING, "Failed to parse where clause. whereClause="+m_whereClause);
					where.append(" 1 = 2 ");
				}
			}
		}
		if (m_onlyCurrentRows && m_TabNo == 0)
		{
			if (where.toString().indexOf(" WHERE ") == -1)
				where.append(" WHERE ");
			else
				where.append(" AND ");
			//	Show only unprocessed or the one updated within x days
			where.append("(Processed='N' OR Updated>");
			where.append("SysDate-1");
			where.append(")");
		}

		//	RO/RW Access
		m_SQL = m_SQL_Select + where.toString();
		m_SQL_Count += where.toString();
		if (m_withAccessControl)
		{
			boolean ro = MRole.SQL_RO;
		//	if (!m_readOnly)
		//		ro = MRole.SQL_RW;
			m_SQL = MRole.getDefault(m_ctx, false).addAccessSQL(m_SQL, 
				m_tableName, MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO);
			m_SQL_Count = MRole.getDefault(m_ctx, false).addAccessSQL(m_SQL_Count, 
				m_tableName, MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO);
		}

		//	ORDER BY
		if (!m_orderClause.equals(""))
		{
			m_SQL += " ORDER BY " + m_orderClause;
		}
		//
		log.fine(m_SQL_Count);
		Env.setContext(m_ctx, m_WindowNo, m_TabNo, GridTab.CTX_SQL, m_SQL);
		return m_SQL;
	}	//	createSelectSql

	/**
	 *	Add Field to Table
	 *  @param field field
	 */
	public void addField (GridField field)
	{
		log.fine("(" + m_tableName + ") - " + field.getColumnName());
		if (m_open)
		{
			log.log(Level.SEVERE, "Table already open - ignored: " + field.getColumnName());
			return;
		}
		if (!MRole.getDefault(m_ctx, false).isColumnAccess (m_AD_Table_ID, field.getAD_Column_ID(), true))
		{
			log.fine("No Column Access " + field.getColumnName());
			return;			
		}
		//  Set Index for Key column
		if (field.isKey())
			m_indexKeyColumn = m_fields.size();
		//  Set Index of other standard columns
		if (field.getColumnName().equals("IsActive"))
			m_indexActiveColumn = m_fields.size();
		else if (field.getColumnName().equals("Processed"))
			m_indexProcessedColumn = m_fields.size();
		else if (field.getColumnName().equals("AD_Client_ID"))
			m_indexClientColumn = m_fields.size();
		else if (field.getColumnName().equals("AD_Org_ID"))
			m_indexOrgColumn = m_fields.size();
		//
		m_fields.add(field);
	}	//	addColumn

	/**
	 *  Returns database column name
	 *
	 *  @param index  the column being queried
	 *  @return column name
	 */
	public String getColumnName (int index)
	{
		if (index < 0 || index > m_fields.size())
		{
			log.log(Level.SEVERE, "Invalid index=" + index);
			return "";
		}
		//
		GridField field = (GridField)m_fields.get(index);
		return field.getColumnName();
	}   //  getColumnName

	/**
	 * Returns a column given its name.
	 *
	 * @param columnName string containing name of column to be located
	 * @return the column index with <code>columnName</code>, or -1 if not found
	 */
	public int findColumn (String columnName)
	{
		for (int i = 0; i < m_fields.size(); i++)
		{
			GridField field = (GridField)m_fields.get(i);
			if (columnName.equals(field.getColumnName()))
				return i;
		}
		return -1;
	}   //  findColumn

	/**
	 *  Returns Class of database column/field
	 *
	 *  @param index  the column being queried
	 *  @return the class
	 */
	public Class<?> getColumnClass (int index)
	{
		if (index < 0 || index >= m_fields.size())
		{
			log.log(Level.SEVERE, "Invalid index=" + index);
			return null;
		}
		GridField field = (GridField)m_fields.get(index);
		return DisplayType.getClass(field.getDisplayType(), false);
	}   //  getColumnClass

	/**
	 *	Set Select Clause Parameter.
	 *	Assumes that you set parameters starting from index zero
	 *  @param index index
	 *  @param parameter parameter
	 */
	public void setParameterSELECT (int index, Object parameter)
	{
		if (index >= m_parameterSELECT.size())
			m_parameterSELECT.add(parameter);
		else
			m_parameterSELECT.set(index, parameter);
	}	//	setParameterSELECT

	/**
	 *	Set Where Clause Parameter.
	 *	Assumes that you set parameters starting from index zero
	 *  @param index index
	 *  @param parameter parameter
	 */
	public void setParameterWHERE (int index, Object parameter)
	{
		if (index >= m_parameterWHERE.size())
			m_parameterWHERE.add(parameter);
		else
			m_parameterWHERE.set(index, parameter);
	}	//	setParameterWHERE


	/**
	 *	Get Column at index
	 *  @param index index
	 *  @return MField
	 */
	protected GridField getField (int index)
	{
		if (index < 0 || index >= m_fields.size())
			return null;
		return (GridField)m_fields.get(index);
	}	//	getColumn

	/**
	 *	Return Columns with Indentifier (ColumnName)
	 *  @param identifier column name
	 *  @return MField
	 */
	protected GridField getField (String identifier)
	{
		if (identifier == null || identifier.length() == 0)
			return null;
		int cols = m_fields.size();
		for (int i = 0; i < cols; i++)
		{
			GridField field = (GridField)m_fields.get(i);
			if (identifier.equalsIgnoreCase(field.getColumnName()))
				return field;
		}
	//	log.log(Level.WARNING, "Not found: '" + identifier + "'");
		return null;
	}	//	getField

	/**
	 *  Get all Fields
	 *  @return MFields
	 */
	public GridField[] getFields ()
	{
		GridField[] retValue = new GridField[m_fields.size()];
		m_fields.toArray(retValue);
		return retValue;
	}   //  getField

	
	/**************************************************************************
	 *	Open Database.
	 *  if already opened, data is refreshed
	 *	@param maxRows maximum number of rows or 0 for all
	 *	@return true if success
	 */
	public boolean open (int maxRows)
	{
		log.info("MaxRows=" + maxRows);
		m_maxRows = maxRows;
		if (m_open)
		{
			log.fine("already open");
			dataRefreshAll();
			return true;
		}

		if (m_virtual)
		{
			verifyVirtual();
		}

		//	create m_SQL and m_countSQL
		createSelectSql();
		if (m_SQL == null || m_SQL.equals(""))
		{
			log.log(Level.SEVERE, "No SQL");
			return false;
		}

		if (!m_open)
		{
			m_open = true;
		}

		//	Start Loading
		m_loader = new Loader();
		m_rowCount = m_loader.open(maxRows);
		if (m_virtual)
		{
			m_buffer = null;
			m_virtualBuffer = new HashMap<Integer, Object[]>(210);
		}
		else
		{
			m_buffer = new ArrayList<Object[]>(m_rowCount+10);
		}
		m_sort = new ArrayList<MSort>(m_rowCount+10);
		if (m_rowCount > 0)
		{
			if (m_rowCount < 1000)
				m_loader.run();
			else
			{
				m_loaderThread = new Thread(m_loader, "TLoader");
				m_loaderThread.start();
			}
		}
		else
			m_loader.close();
		//
		m_changed = false;
		m_rowChanged = -1;
		m_inserting = false;
		return true;
	}	//	open

	private void verifyVirtual()
	{
		if (m_indexKeyColumn == -1)
		{
			m_virtual = false;
			return;
		}
		GridField[] fields = getFields();
		for(int i = 0; i < fields.length; i++)
		{
			if (fields[i].isKey() && i != m_indexKeyColumn)
			{
				m_virtual = false;
				return;
			}
		}
	}

	/**
	 *  Wait until async loader of Table and Lookup Fields is complete
	 *  Used for performance tests
	 */
	public void loadComplete()
	{
		//  Wait for loader
		if (m_loaderThread != null)
		{
			if (m_loaderThread.isAlive())
			{
				try
				{
					m_loaderThread.join();
				}
				catch (InterruptedException ie)
				{
					log.log(Level.SEVERE, "Join interrupted", ie);
				}
			}
		}
		//  wait for field lookup loaders
		for (int i = 0; i < m_fields.size(); i++)
		{
			GridField field = (GridField)m_fields.get(i);
			field.lookupLoadComplete();
		}
	}   //  loadComplete

	/**
	 *  Is Loading
	 *  @return true if loading
	 */
	public boolean isLoading()
	{
		if (m_loaderThread != null && m_loaderThread.isAlive())
			return true;
		return false;
	}   //  isLoading

	/**
	 *	Is it open?
	 *  @return true if opened
	 */
	public boolean isOpen()
	{
		return m_open;
	}	//	isOpen

	/**
	 *	Close Resultset
	 *  @param finalCall final call
	 */
	public void close (boolean finalCall)
	{
		if (!m_open)
			return;
		log.fine("final=" + finalCall);

		//  remove listeners
		if (finalCall)
		{
			DataStatusListener evl[] = (DataStatusListener[])listenerList.getListeners(DataStatusListener.class);
			for (int i = 0; i < evl.length; i++)
				listenerList.remove(DataStatusListener.class, evl[i]);
			TableModelListener ev2[] = (TableModelListener[])listenerList.getListeners(TableModelListener.class);
			for (int i = 0; i < ev2.length; i++)
				listenerList.remove(TableModelListener.class, ev2[i]);
			VetoableChangeListener vcl[] = m_vetoableChangeSupport.getVetoableChangeListeners();
			for (int i = 0; i < vcl.length; i++)
				m_vetoableChangeSupport.removeVetoableChangeListener(vcl[i]);
		}

		//	Stop loader
		while (m_loaderThread != null && m_loaderThread.isAlive())
		{
			log.fine("Interrupting Loader ...");
			m_loaderThread.interrupt();
			try
			{
				Thread.sleep(200);		//	.2 second
			}
			catch (InterruptedException ie)
			{}
		}

		if (!m_inserting)
			dataSave(false);	//	not manual

		if (m_buffer != null)
		{
			m_buffer.clear();
			m_buffer = null;
		}
		if (m_sort != null)
		{
			m_sort.clear();
			m_sort = null;
		}
		if (m_virtualBuffer != null)
		{
			m_virtualBuffer.clear();
			m_virtualBuffer = null;
		}

		if (finalCall)
			dispose();

		//  Fields are disposed from MTab
		log.fine("");
		m_open = false;
	}	//	close

	/**
	 *  Dispose MTable.
	 *  Called by close-final
	 */
	private void dispose()
	{
		//  MFields
		for (int i = 0; i < m_fields.size(); i++)
			((GridField)m_fields.get(i)).dispose();
		m_fields.clear();
		m_fields = null;
		//
		m_vetoableChangeSupport = null;
		//
		m_parameterSELECT.clear();
		m_parameterSELECT = null;
		m_parameterWHERE.clear();
		m_parameterWHERE = null;
		//  clear data arrays
		m_buffer = null;
		m_virtualBuffer = null;
		m_sort = null;
		m_rowData = null;
		m_oldValue = null;
		m_loader = null;
		m_loaderThread = null;
	}   //  dispose

	/**
	 *	Get total database column count (displayed and not displayed)
	 *  @return column count
	 */
	public int getColumnCount()
	{
		return m_fields.size();
	}	//	getColumnCount

	/**
	 *	Get (displayed) field count
	 *  @return field count
	 */
	public int getFieldCount()
	{
		return m_fields.size();
	}	//	getFieldCount

	/**
	 *  Return number of rows
	 *  @return Number of rows or 0 if not opened
	 */
	public int getRowCount()
	{
		return m_rowCount;
	}	//	getRowCount

	/**
	 *	Set the Column to determine the color of the row
	 *  @param columnName column name
	 */
	public void setColorColumn (String columnName)
	{
		m_indexColorColumn = findColumn(columnName);
	}	//  setColorColumn

	/**
	 *	Get ColorCode for Row.
	 *  <pre>
	 *	If numerical value in compare column is
	 *		negative = -1,
	 *      positive = 1,
	 *      otherwise = 0
	 *  </pre>
	 *  @see #setColorColumn
	 *  @param row row
	 *  @return color code
	 */
	public int getColorCode (int row)
	{
		if (m_indexColorColumn  == -1)
			return 0;
		Object data = getValueAt(row, m_indexColorColumn);
		//	We need to have a Number
		if (data == null || !(data instanceof BigDecimal))
			return 0;
		BigDecimal bd = (BigDecimal)data;
		return bd.signum();
	}	//	getColorCode


	/**
	 *	Sort Entries by Column.
	 *  actually the rows are not sorted, just the access pointer ArrayList
	 *  with the same size as m_buffer with MSort entities
	 *  @param col col
	 *  @param ascending ascending
	 */
	@SuppressWarnings("unchecked")
	public void sort (int col, boolean ascending)
	{
		log.info("#" + col + " " + ascending);
		if (col < 0) {
			return;
		}
		if (getRowCount() == 0)
			return;

		//cache changed row
		Object[] changedRow = m_rowChanged >= 0 ? getDataAtRow(m_rowChanged) : null;

		GridField field = getField (col);
		//	RowIDs are not sorted
		if (field.getDisplayType() == DisplayType.RowID)
			return;
		boolean isLookup = DisplayType.isLookup(field.getDisplayType());
		boolean isASI = DisplayType.PAttribute == field.getDisplayType();

		//	fill MSort entities with data entity
		for (int i = 0; i < m_sort.size(); i++)
		{
			MSort sort = (MSort)m_sort.get(i);
			Object[] rowData = getDataAtRow(i);
			if (rowData[col] == null)
				sort.data = null;
			else if (isLookup || isASI)
				sort.data = field.getLookup().getDisplay(rowData[col]);	//	lookup
			else
				sort.data = rowData[col];								//	data
		}
		log.info(field.toString() + " #" + m_sort.size());

		//	sort it
		MSort sort = new MSort(0, null);
		sort.setSortAsc(ascending);
		Collections.sort(m_sort, sort);
		if (m_virtual)
		{
			Object[] newRow = m_virtualBuffer.get(NEW_ROW_ID);
			m_virtualBuffer.clear();
			if (newRow != null && newRow.length > 0)
				m_virtualBuffer.put(NEW_ROW_ID, newRow);

			if (changedRow != null && changedRow.length > 0)
			{
				if (changedRow[m_indexKeyColumn] != null && (Integer)changedRow[m_indexKeyColumn] > 0)
				{
					m_virtualBuffer.put((Integer)changedRow[m_indexKeyColumn], changedRow);
					for(int i = 0; i < m_sort.size(); i++)
					{
						if (m_sort.get(i).index == (Integer)changedRow[m_indexKeyColumn])
						{
							m_rowChanged = i;
							break;
						}
					}
				}
			}

			//release sort memory
			for (int i = 0; i < m_sort.size(); i++)
			{
				m_sort.get(i).data = null;
			}
		}
		//	update UI
		fireTableDataChanged();
		//  Info detected by MTab.dataStatusChanged and current row set to 0
		fireDataStatusIEvent("Sorted", "#" + m_sort.size());
	}	//	sort

	/**
	 *	Get Key ID or -1 of none
	 *  @param row row
	 *  @return ID or -1
	 */
	public int getKeyID (int row)
	{
	//	Log.info("MTable.getKeyID - row=" + row + ", keyColIdx=" + m_indexKeyColumn);
		if (m_indexKeyColumn != -1)
		{
			try
			{
				Integer ii = (Integer)getValueAt(row, m_indexKeyColumn);
				if (ii == null)
					return -1;
				return ii.intValue();
			}
			catch (Exception e)     //  Alpha Key
			{
				return -1;
			}
		}
		return -1;
	}	//	getKeyID

	/**
	 *	Get Key ColumnName
	 *  @return key column name
	 */
	public String getKeyColumnName()
	{
		if (m_indexKeyColumn != -1)
			return getColumnName(m_indexKeyColumn);
		return "";
	}	//	getKeyColumnName


	/**************************************************************************
	 * 	Get Value in Resultset
	 *  @param row row
	 *  @param col col
	 *  @return Object of that row/column
	 */
	public Object getValueAt (int row, int col)
	{
	//	log.config( "MTable.getValueAt r=" + row + " c=" + col);
		if (!m_open || row < 0 || col < 0 || row >= m_rowCount)
		{
		//	log.fine( "Out of bounds - Open=" + m_open + ", RowCount=" + m_rowCount);
			return null;
		}

		//	need to wait for data read into buffer
		int loops = 0;
		while (row >= m_sort.size() && m_loaderThread != null && m_loaderThread.isAlive() && loops < 15)
		{
			log.fine("Waiting for loader row=" + row + ", size=" + m_sort.size());
			try
			{
				Thread.sleep(500);		//	1/2 second
			}
			catch (InterruptedException ie)
			{}
			loops++;
		}

		//	empty buffer
		if (row >= m_sort.size())
		{
		//	log.fine( "Empty buffer");
			return null;
		}

		//	return Data item
		Object[] rowData = getDataAtRow(row);
		//	out of bounds
		if (rowData == null || col > rowData.length)
		{
		//	log.fine( "No data or Column out of bounds");
			return null;
		}
		return rowData[col];
	}	//	getValueAt

	private Object[] getDataAtRow(int row)
	{
		return getDataAtRow(row, true);
	}

	private Object[] getDataAtRow(int row, boolean fetchIfNotFound)
	{
		MSort sort = (MSort)m_sort.get(row);
		Object[] rowData = null;
		if (m_virtual)
		{
			if (sort.index != NEW_ROW_ID && !(m_virtualBuffer.containsKey(sort.index)) && fetchIfNotFound)
			{
				fillBuffer(row, DEFAULT_FETCH_SIZE);
			}
			rowData = (Object[])m_virtualBuffer.get(sort.index);
		}
		else
		{
			rowData = (Object[])m_buffer.get(sort.index);
		}
		return rowData;
	}

	private void setDataAtRow(int row, Object[] rowData) {
		MSort sort = m_sort.get(row);
		if (m_virtual)
		{
			if (sort.index != NEW_ROW_ID && !(m_virtualBuffer.containsKey(sort.index)))
			{
				fillBuffer(row, DEFAULT_FETCH_SIZE);
			}
			m_virtualBuffer.put(sort.index, rowData);
		}
		else
		{
			m_buffer.set(sort.index, rowData);
		}

	}

	private void fillBuffer(int start, int fetchSize)
	{
		//adjust start if needed
		if (start > 0)
		{
			if (start + fetchSize >= m_sort.size())
			{
				start = start - (fetchSize - ( m_sort.size() - start ));
				if (start < 0)
					start = 0;
			}
		}
		StringBuffer sql = new StringBuffer();
		sql.append(m_SQL_Select)
			.append(" WHERE ")
			.append(getKeyColumnName())
			.append(" IN (");
		Map<Integer, Integer>rowmap = new LinkedHashMap<Integer, Integer>(DEFAULT_FETCH_SIZE);
		for(int i = start; i < start+fetchSize && i < m_sort.size(); i++)
		{
			if(i > start)
				sql.append(",");
			sql.append(m_sort.get(i).index);
			rowmap.put(m_sort.get(i).index, i);
		}
		sql.append(")");

		Object[] newRow = m_virtualBuffer.get(NEW_ROW_ID);
		//cache changed row
		Object[] changedRow = m_rowChanged >= 0 ? getDataAtRow(m_rowChanged, false) : null;
		m_virtualBuffer = new HashMap<Integer, Object[]>(210);
		if (newRow != null && newRow.length > 0)
			m_virtualBuffer.put(NEW_ROW_ID, newRow);
		if (changedRow != null && changedRow.length > 0)
		{
			if (changedRow[m_indexKeyColumn] != null && (Integer)changedRow[m_indexKeyColumn] > 0)
			{
				m_virtualBuffer.put((Integer)changedRow[m_indexKeyColumn], changedRow);
			}
		}

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = DB.prepareStatement(sql.toString(), null);
			rs = stmt.executeQuery();
			while(rs.next())
			{
				Object[] data = readData(rs);
				rowmap.remove(data[m_indexKeyColumn]);
				m_virtualBuffer.put((Integer)data[m_indexKeyColumn], data);
			}
			if (!rowmap.isEmpty())
			{
				List<Integer> toremove = new ArrayList<Integer>();
				for(Map.Entry<Integer, Integer> entry : rowmap.entrySet())
				{
					toremove.add(entry.getValue());
				}
				Collections.reverse(toremove);
				for(Integer row : toremove)
				{
					m_sort.remove(row);
				}
			}
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		finally
		{
			DB.close(rs, stmt);
		}
	}
	
	/**
	 *	Indicate that there will be a change
	 *  @param changed changed
	 */
	public void setChanged (boolean changed)
	{
		//	Can we edit?
		if (!m_open || m_readOnly)
			return;

		//	Indicate Change
		m_changed = changed;
		if (!changed)
			m_rowChanged = -1;
		//if (changed)
		//	fireDataStatusIEvent("", "");
	}	//	setChanged

	/**
	 * 	Set Value in data and update MField.
	 *  (called directly or from JTable.editingStopped())
	 *
	 *  @param  value value to assign to cell
	 *  @param  row row index of cell
	 *  @param  col column index of cell
	 */
	public final void setValueAt (Object value, int row, int col)
	{
		setValueAt (value, row, col, false);
	}	//	setValueAt

	/**
	 * 	Set Value in data and update MField.
	 *  (called directly or from JTable.editingStopped())
	 *
	 *  @param  value value to assign to cell
	 *  @param  row row index of cell
	 *  @param  col column index of cell
	 * 	@param	force force setting new value
	 */
	public final void setValueAt (Object value, int row, int col, boolean force)
	{
		//	Can we edit?
		if (!m_open || m_readOnly       //  not accessible
				|| row < 0 || col < 0   //  invalid index
				|| m_rowCount == 0	//  no rows
				|| row >= m_rowCount )     //invalid row
		{
			log.finest("r=" + row + " c=" + col + " - R/O=" + m_readOnly + ", Rows=" + m_rowCount + " - Ignored");
			return;
		}

		dataSave(row, false);

		//	Has anything changed?
		Object oldValue = getValueAt(row, col);
		if (!force && !isValueChanged(oldValue, value) )
		{
			log.finest("r=" + row + " c=" + col + " - New=" + value + "==Old=" + oldValue + " - Ignored");
			return;
		}

		log.fine("r=" + row + " c=" + col + " = " + value + " (" + oldValue + ")");

		//  Save old value
		m_oldValue = new Object[3];
		m_oldValue[0] = new Integer(row);
		m_oldValue[1] = new Integer(col);
		m_oldValue[2] = oldValue;

		//	Set Data item
		
		Object[] rowData = getDataAtRow(row);
		m_rowChanged = row;

		/**	Selection
		if (col == 0)
		{
			rowData[col] = value;
			m_buffer.set(sort.index, rowData);
			return;
		}	**/

		//	save original value - shallow copy
		if (m_rowData == null)
		{
			int size = m_fields.size();
			m_rowData = new Object[size];
			for (int i = 0; i < size; i++)
				m_rowData[i] = rowData[i];
		}

		//	save & update
		rowData[col] = value;
		setDataAtRow(row, rowData);
		//  update Table
		fireTableCellUpdated(row, col);
		//  update MField
		GridField field = getField(col);
		field.setValue(value, m_inserting);
		//  inform
		DataStatusEvent evt = createDSE();
		evt.setChangedColumn(col, field.getColumnName());
		fireDataStatusChanged(evt);
	}	//	setValueAt

	/**
	 *  Get Old Value
	 *  @param row row
	 *  @param col col
	 *  @return old value
	 */
	public Object getOldValue (int row, int col)
	{
		if (m_oldValue == null)
			return null;
		if (((Integer)m_oldValue[0]).intValue() == row
				&& ((Integer)m_oldValue[1]).intValue() == col)
			return m_oldValue[2];
		return null;
	}   // getOldValue

	/**
	 *	Check if the current row needs to be saved.
	 *  @param  onlyRealChange if true the value of a field was actually changed
	 *  (e.g. for new records, which have not been changed) - default false
	 *	@return true it needs to be saved
	 */
	public boolean needSave(boolean onlyRealChange)
	{
		return needSave(m_rowChanged, onlyRealChange);
	}   //  needSave

	/**
	 *	Check if the row needs to be saved.
	 *  - only if nothing was changed
	 *	@return true it needs to be saved
	 */
	public boolean needSave()
	{
		return needSave(m_rowChanged, false);
	}   //  needSave

	/**
	 *	Check if the row needs to be saved.
	 *  - only when row changed
	 *  - only if nothing was changed
	 *	@param	newRow to check
	 *	@return true it needs to be saved
	 */
	public boolean needSave(int newRow)
	{
		return needSave(newRow, false);
	}   //  needSave

	/**
	 *	Check if the row needs to be saved.
	 *  - only when row changed
	 *  - only if nothing was changed
	 *	@param	newRow to check
	 *  @param  onlyRealChange if true the value of a field was actually changed
	 *  (e.g. for new records, which have not been changed) - default false
	 *	@return true it needs to be saved
	 */
	public boolean needSave(int newRow, boolean onlyRealChange)
	{
		log.fine("Row=" + newRow +
			", Changed=" + m_rowChanged + "/" + m_changed);  //  m_rowChanged set in setValueAt
		//  nothing done
		if (!m_changed && m_rowChanged == -1)
			return false;
		//  E.g. New unchanged records
		if (m_changed && m_rowChanged == -1 && onlyRealChange)
			return false;
		//  same row
		if (newRow == m_rowChanged)
			return false;

		return true;
	}	//	needSave

	/*************************************************************************/

	/** Save OK - O		*/
	public static final char	SAVE_OK = 'O';			//	the only OK condition
	/** Save Error - E	*/
	public static final char	SAVE_ERROR = 'E';
	/** Save Access Error - A	*/
	public static final char	SAVE_ACCESS = 'A';
	/** Save Mandatory Error - M	*/
	public static final char	SAVE_MANDATORY = 'M';
	/** Save Abort Error - U	*/
	public static final char	SAVE_ABORT = 'U';

	/**
	 *	Check if it needs to be saved and save it.
	 *  @param newRow row
	 *  @param manualCmd manual command to save
	 *	@return true if not needed to be saved or successful saved
	 */
	public boolean dataSave (int newRow, boolean manualCmd)
	{
		log.fine("Row=" + newRow +
			", Changed=" + m_rowChanged + "/" + m_changed);  //  m_rowChanged set in setValueAt
		//  nothing done
		if (!m_changed && m_rowChanged == -1)
			return true;
		//  same row, don't save yet
		if (newRow == m_rowChanged)
			return true;

		return (dataSave(manualCmd) == SAVE_OK);
	}   //  dataSave

	/**
	 *	Save unconditional.
	 *  @param manualCmd if true, no vetoable PropertyChange will be fired for save confirmation
	 *	@return OK Or Error condition
	 *  Error info (Access*, FillMandatory, SaveErrorNotUnique,
	 *  SaveErrorRowNotFound, SaveErrorDataChanged) is saved in the log
	 */
	public char dataSave (boolean manualCmd)
	{
		//	cannot save
		if (!m_open)
		{
			log.warning ("Error - Open=" + m_open);
			return SAVE_ERROR;
		}
		//	no need - not changed - row not positioned - no Value changed
		if (m_rowChanged == -1)
		{
			log.config("NoNeed - Changed=" + m_changed + ", Row=" + m_rowChanged);
		//	return SAVE_ERROR;
			if (!manualCmd)
				return SAVE_OK;
		}
		//  Value not changed
		if (m_rowData == null)
		{
			//reset out of sync variable
			m_rowChanged = -1;
			log.fine("No Changes");
			return SAVE_ERROR;
		}

		if (m_readOnly)
		//	If Processed - not editable (Find always editable)  -> ok for changing payment terms, etc.
		{
			log.warning("IsReadOnly - ignored");
			dataIgnore();
			return SAVE_ACCESS;
		}

		//	row not positioned - no Value changed
		if (m_rowChanged == -1)
		{
			if (m_newRow != -1)     //  new row and nothing changed - might be OK
				m_rowChanged = m_newRow;
			else
			{
				fireDataStatusEEvent("SaveErrorNoChange", "", true);
				return SAVE_ERROR;
			}
		}

		//	Can we change?
		int[] co = getClientOrg(m_rowChanged);
		int AD_Client_ID = co[0]; 
		int AD_Org_ID = co[1];
		if (!MRole.getDefault(m_ctx, false).canUpdate(AD_Client_ID, AD_Org_ID, m_AD_Table_ID, 0, true))
		{
			fireDataStatusEEvent(CLogger.retrieveError());
			dataIgnore();
			return SAVE_ACCESS;
		}

		log.info("Row=" + m_rowChanged);

		//  inform about data save action, if not manually initiated
		try
		{
			if (!manualCmd)
				m_vetoableChangeSupport.fireVetoableChange(PROPERTY, -1, m_rowChanged);
		}
		catch (PropertyVetoException pve)
		{
			log.warning(pve.getMessage());
			//[ 2696732 ] Save changes dialog's cancel button shouldn't reset status
			//https://sourceforge.net/tracker/index.php?func=detail&aid=2696732&group_id=176962&atid=879332
			//dataIgnore();
			return SAVE_ABORT;
		}

		//	get updated row data
		Object[] rowData = getDataAtRow(m_rowChanged);

		// CarlosRuiz - globalqss - fix [1722226] - Usability - Record_ID = 0 on 9 tables can't be modified
		boolean specialZeroUpdate = false;
		if (!m_inserting // not inserting, updating a record 
			&& manualCmd   // in a manual way (pushing the save button)
			&& (Env.getAD_User_ID(m_ctx) == 0 || Env.getAD_User_ID(m_ctx) == 100)  // user must know what is doing -> just allowed to System or SuperUser (Hardcoded)
			&& getKeyID(m_rowChanged) == 0) { // the record being changed has ID = 0
			String tablename = getTableName(); // just the allowed tables (HardCoded)
			if (tablename.equals("AD_Org") ||
				tablename.equals("AD_ReportView") ||
				tablename.equals("AD_Role") ||
				tablename.equals("AD_System") ||
				tablename.equals("AD_User") ||
				tablename.equals("C_DocType") ||
				tablename.equals("GL_Category") ||
				tablename.equals("M_AttributeSet") ||
				tablename.equals("M_AttributeSetInstance")) {
				specialZeroUpdate = true;
			}
		}

		//	Check Mandatory
		String missingColumns = getMandatory(rowData);
		if (missingColumns.length() != 0)
		{
		//	Trace.printStack(false, false);
			fireDataStatusEEvent("FillMandatory", missingColumns + "\n", true);
			return SAVE_MANDATORY;
		}

		/**
		 *	Update row *****
		 */
		int Record_ID = 0;
		if (!m_inserting)
			Record_ID = getKeyID(m_rowChanged);
		try
		{
			if (!m_tableName.endsWith("_Trl") && !specialZeroUpdate)	//	translation tables have no model
				return dataSavePO (Record_ID);
		}
		catch (Throwable e)
		{
			if (e instanceof ClassNotFoundException)
				log.warning(m_tableName + " - " + e.getLocalizedMessage());
			else
			{
				log.log(Level.SEVERE, "Persistency Issue - " 
					+ m_tableName + ": " + e.getLocalizedMessage(), e);
				return SAVE_ERROR;
			}
		}
		
		/**	Manual Update of Row (i.e. not via PO class)	**/
		log.info("NonPO");
		
		boolean error = false;
		lobReset();
		//
		String is = null;
		final String ERROR = "ERROR: ";
		final String INFO  = "Info: ";

		//	Update SQL with specific where clause
		StringBuffer select = new StringBuffer("SELECT ");
		for (int i = 0, addedColumns = 0; i < m_fields.size(); i++)
		{
			GridField field = (GridField)m_fields.get(i);
			if (m_inserting && field.isVirtualColumn())
				continue;
			// Add "," if it is not the first added column - teo_sarca [ 1735618 ]
			if (addedColumns++ > 0)
				select.append(",");
			select.append(field.getColumnSQL(true));	//	ColumnName or Virtual Column
		}
		//
		select.append(" FROM ").append(m_tableName);
		StringBuffer singleRowWHERE = new StringBuffer();
		StringBuffer multiRowWHERE = new StringBuffer();
		//	Create SQL	& RowID
		if (m_inserting)
			select.append(" WHERE 1=2");
		else	//  FOR UPDATE causes  -  ORA-01002 fetch out of sequence
			select.append(" WHERE ").append(getWhereClause(rowData));
		PreparedStatement pstmt = null;
		ResultSet rs =  null;
		try
		{
			pstmt = DB.prepareStatement (select.toString(), 
				ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE, null);
			rs = pstmt.executeQuery();
			//	only one row
			if (!(m_inserting || rs.next()))
			{
				fireDataStatusEEvent("SaveErrorRowNotFound", "", true);
				dataRefresh(m_rowChanged);
				return SAVE_ERROR;
			}

			Object[] rowDataDB = null;
			//	Prepare
			boolean manualUpdate = ResultSet.CONCUR_READ_ONLY == rs.getConcurrency();
			// Manual update if log migration scripts is enabled - teo_sarca BF [ 1901192 ]
			if(!manualUpdate && Ini.isPropertyBool(Ini.P_LOGMIGRATIONSCRIPT))
				manualUpdate = true;
			if (manualUpdate)
				createUpdateSqlReset();
			if (m_inserting)
			{
				if (manualUpdate)
					log.fine("Prepare inserting ... manual");
				else
				{
					log.fine("Prepare inserting ... RowSet");
					rs.moveToInsertRow ();
				}
			}
			else
			{
				log.fine("Prepare updating ... manual=" + manualUpdate);
				//	get current Data in DB
				rowDataDB = readData (rs);
			}

			/**	Data:
			 *		m_rowData	= original Data
			 *		rowData 	= updated Data
			 *		rowDataDB	= current Data in DB
			 *	1) Difference between original & updated Data?	N:next
			 *	2) Difference between original & current Data?	Y:don't update
			 *	3) Update current Data
			 *	4) Refresh to get last Data (changed by trigger, ...)
			 */

			//	Constants for Created/Updated(By)
			Timestamp now = new Timestamp(System.currentTimeMillis());
			int user = Env.getContextAsInt(m_ctx, "#AD_User_ID");

			/**
			 *	for every column
			 */
			int size = m_fields.size();
			int colRs = 1;
			for (int col = 0; col < size; col++)
			{
				GridField field = (GridField)m_fields.get (col);
				if (field.isVirtualColumn())
				{
					if (!m_inserting)
						colRs++;
					continue;
				}
				String columnName = field.getColumnName ();
			//	log.fine(columnName + "= " + m_rowData[col] + " <> DB: " + rowDataDB[col] + " -> " + rowData[col]);

				//	RowID, Virtual Column
				if (field.getDisplayType () == DisplayType.RowID
					|| field.isVirtualColumn())
					; //	ignore

				//	New Key
				else if (field.isKey () && m_inserting)
				{
					if (columnName.endsWith ("_ID") || columnName.toUpperCase().endsWith ("_ID"))
					{
						int insertID = DB.getNextID (m_ctx, m_tableName, null);	//	no trx
						if (manualUpdate)
							createUpdateSql (columnName, String.valueOf (insertID));
						else
							rs.updateInt (colRs, insertID); 						// ***
						singleRowWHERE.append (columnName).append ("=").append (insertID);
						//
						is = INFO + columnName + " -> " + insertID + " (Key)";
					}
					else //	Key with String value
					{
						String str = rowData[col].toString ();
						if (manualUpdate)
							createUpdateSql (columnName, DB.TO_STRING (str));
						else
							rs.updateString (colRs, str); 						// ***
						singleRowWHERE = new StringBuffer();	//	overwrite
						singleRowWHERE.append (columnName).append ("=").append (DB.TO_STRING(str));
						//
						is = INFO + columnName + " -> " + str + " (StringKey)";
					}
					log.fine(is);
				} //	New Key

				//	New DocumentNo
				else if (columnName.equals ("DocumentNo"))
				{
					boolean newDocNo = false;
					String docNo = (String)rowData[col];
					//  we need to have a doc number
					if (docNo == null || docNo.length () == 0)
						newDocNo = true;
						//  Preliminary ID from CalloutSystem
					else if (docNo.startsWith ("<") && docNo.endsWith (">"))
						newDocNo = true;

					if (newDocNo || m_inserting)
					{
						String insertDoc = null;
						//  always overwrite if insering with mandatory DocType DocNo
						if (m_inserting)
							insertDoc = DB.getDocumentNo (m_ctx, m_WindowNo, 
								m_tableName, true, null);	//	only doc type - no trx
						log.fine("DocumentNo entered=" + docNo + ", DocTypeInsert=" + insertDoc + ", newDocNo=" + newDocNo);
						// can we use entered DocNo?
						if (insertDoc == null || insertDoc.length () == 0)
						{
							if (!newDocNo && docNo != null && docNo.length () > 0)
								insertDoc = docNo;
							else //  get a number from DocType or Table
								insertDoc = DB.getDocumentNo (m_ctx, m_WindowNo, 
									m_tableName, false, null);	//	no trx
						}
						//	There might not be an automatic document no for this document
						if (insertDoc == null || insertDoc.length () == 0)
						{
							//  in case DB function did not return a value
							if (docNo != null && docNo.length () != 0)
								insertDoc = (String)rowData[col];
							else
							{
								error = true;
								is = ERROR + field.getColumnName () + "= " + rowData[col] + " NO DocumentNo";
								log.fine(is);
								break;
							}
						}
						//
						if (manualUpdate)
							createUpdateSql (columnName, DB.TO_STRING (insertDoc));
						else
							rs.updateString (colRs, insertDoc);					//	***
							//
						is = INFO + columnName + " -> " + insertDoc + " (DocNo)";
						log.fine(is);
					}
				}	//	New DocumentNo

				//  New Value(key)
				else if (columnName.equals ("Value") && m_inserting)
				{
					String value = (String)rowData[col];
					//  Get from Sequence, if not entered
					if (value == null || value.length () == 0)
					{
						value = DB.getDocumentNo (m_ctx, m_WindowNo, m_tableName, false, null);
						//  No Value
						if (value == null || value.length () == 0)
						{
							error = true;
							is = ERROR + field.getColumnName () + "= " + rowData[col]
								 + " No Value";
							log.fine(is);
							break;
						}
					}
					if (manualUpdate)
						createUpdateSql (columnName, DB.TO_STRING (value));
					else
						rs.updateString (colRs, value); 							//	***
						//
					is = INFO + columnName + " -> " + value + " (Value)";
					log.fine(is);
				}	//	New Value(key)

				//	Updated		- check database
				else if (columnName.equals ("Updated"))
				{
					if (m_compareDB && !m_inserting && !m_rowData[col].equals (rowDataDB[col]))	//	changed
					{
						error = true;
						is = ERROR + field.getColumnName () + "= " + m_rowData[col]
							 + " != DB: " + rowDataDB[col];
						log.fine(is);
						break;
					}
					if (manualUpdate)
						createUpdateSql (columnName, DB.TO_DATE (now, false));
					else
						rs.updateTimestamp (colRs, now); 							//	***
						//
					is = INFO + "Updated/By -> " + now + " - " + user;
					log.fine(is);
				} //	Updated

				//	UpdatedBy	- update
				else if (columnName.equals ("UpdatedBy"))
				{
					if (manualUpdate)
						createUpdateSql (columnName, String.valueOf (user));
					else
						rs.updateInt (colRs, user); 								//	***
				} //	UpdatedBy

				//	Created
				else if (m_inserting && columnName.equals ("Created"))
				{
					if (manualUpdate)
						createUpdateSql (columnName, DB.TO_DATE (now, false));
					else
						rs.updateTimestamp (colRs, now); 							//	***
				} //	Created

				//	CreatedBy
				else if (m_inserting && columnName.equals ("CreatedBy"))
				{
					if (manualUpdate)
						createUpdateSql (columnName, String.valueOf (user));
					else
						rs.updateInt (colRs, user); 								//	***
				} //	CreatedBy

				//	Nothing changed & null
				else if (m_rowData[col] == null && rowData[col] == null)
				{
					if (m_inserting)
					{
						if (manualUpdate)
							createUpdateSql (columnName, "NULL");
						else
							rs.updateNull (colRs); 								//	***
						is = INFO + columnName + "= NULL";
						log.fine(is);
					}
				}

				//	***	Data changed ***
				else if (m_inserting
				  || (m_rowData[col] == null && rowData[col] != null)
				  || (m_rowData[col] != null && rowData[col] == null)
				  || !m_rowData[col].equals (rowData[col])) 			//	changed
				{
					//	Original == DB
					if (m_inserting || !m_compareDB
					  || (m_rowData[col] == null && rowDataDB[col] == null)
					  || (m_rowData[col] != null && m_rowData[col].equals (rowDataDB[col])))
					{
						if (CLogMgt.isLevelFinest())
							log.fine(columnName + "=" + rowData[col]
								+ " " + (rowData[col]==null ? "" : rowData[col].getClass().getName()));
						//
						boolean encrypted = field.isEncryptedColumn();
						//
						String type = "String";
						if (rowData[col] == null)
						{
							if (manualUpdate)
								createUpdateSql (columnName, "NULL");
							else
								rs.updateNull (colRs); 							//	***
						}
						
						//	ID - int
						else if (DisplayType.isID (field.getDisplayType()) 
							|| field.getDisplayType() == DisplayType.Integer)
						{
							try
							{
								Object dd = rowData[col];
								Integer iii = null;
								if (dd instanceof Integer)
									iii = (Integer)dd;
								else
									iii = new Integer(dd.toString());
								if (encrypted)
									iii = (Integer)encrypt(iii);
								if (manualUpdate)
									createUpdateSql (columnName, String.valueOf (iii));
								else
									rs.updateInt (colRs, iii.intValue()); 		// 	***
							}
							catch (Exception e) //  could also be a String (AD_Language, AD_Message)
							{
								if (manualUpdate)
									createUpdateSql (columnName, DB.TO_STRING (rowData[col].toString ()));
								else
									rs.updateString (colRs, rowData[col].toString ()); //	***
							}
							type = "Int";
						}
						//	Numeric - BigDecimal
						else if (DisplayType.isNumeric (field.getDisplayType ()))
						{
							BigDecimal bd = (BigDecimal)rowData[col];
							if (encrypted)
								bd = (BigDecimal)encrypt(bd);
							if (manualUpdate)
								createUpdateSql (columnName, bd.toString ());
							else
								rs.updateBigDecimal (colRs, bd); 				//	***
							type = "Number";
						}
						//	Date - Timestamp
						else if (DisplayType.isDate (field.getDisplayType ()))
						{
							Timestamp ts = (Timestamp)rowData[col];
							if (encrypted)
								ts = (Timestamp)encrypt(ts);
							if (manualUpdate)
								createUpdateSql (columnName, DB.TO_DATE (ts, false));
							else
								rs.updateTimestamp (colRs, ts); 				//	***
							type = "Date";
						}
						//	LOB
						else if (field.getDisplayType() == DisplayType.TextLong)
						{
							PO_LOB lob = new PO_LOB (getTableName(), columnName, 
								null, field.getDisplayType(), rowData[col]);
							lobAdd(lob);
							type = "CLOB";
						}
						//	Boolean
						else if (field.getDisplayType() == DisplayType.YesNo)
						{
							String yn = null;
							if (rowData[col] instanceof Boolean)
							{
								Boolean bb = (Boolean)rowData[col];
								yn = bb.booleanValue() ? "Y" : "N";
							}
							else
								yn = "Y".equals(rowData[col]) ? "Y" : "N"; 
							if (encrypted)
								yn = (String)yn;
							if (manualUpdate)
								createUpdateSql (columnName, DB.TO_STRING (yn));
							else
								rs.updateString (colRs, yn); 					//	***
						}
						//	String and others
						else	
						{
							String str = rowData[col].toString ();
							if (encrypted)
								str = (String)encrypt(str);
							if (manualUpdate)
								createUpdateSql (columnName, DB.TO_STRING (str));
							else
								rs.updateString (colRs, str); 					//	***
						}
						//
						is = INFO + columnName + "= " + m_rowData[col]
							 + " -> " + rowData[col] + " (" + type + ")";
						if (encrypted)
							is += " encrypted";
						log.fine(is);
					}
					//	Original != DB
					else
					{
						error = true;
						is = ERROR + field.getColumnName () + "= " + m_rowData[col]
							 + " != DB: " + rowDataDB[col] + " -> " + rowData[col];
						log.fine(is);
					}
				}	//	Data changed

				//	Single Key - retrieval sql
				if (field.isKey() && !m_inserting)
				{
					if (rowData[col] == null)
						throw new RuntimeException("Key is NULL - " + columnName);
					if (columnName.endsWith ("_ID"))
						singleRowWHERE.append (columnName).append ("=").append (rowData[col]);
					else
					{
						singleRowWHERE = new StringBuffer();	//	overwrite
						singleRowWHERE.append (columnName).append ("=").append (DB.TO_STRING(rowData[col].toString()));
					}
				}
				//	MultiKey Inserting - retrieval sql
				if (field.isParentColumn())
				{
					if (rowData[col] == null)
						throw new RuntimeException("MultiKey Parent is NULL - " + columnName);
					if (multiRowWHERE.length() != 0)
						multiRowWHERE.append(" AND ");
					if (columnName.endsWith ("_ID"))
						multiRowWHERE.append (columnName).append ("=").append (rowData[col]);
					else
						multiRowWHERE.append (columnName).append ("=").append (DB.TO_STRING(rowData[col].toString()));
				}
				//
				colRs++;
			}	//	for every column

			if (error)
			{
				if (manualUpdate)
					createUpdateSqlReset();
				else
					rs.cancelRowUpdates();
				fireDataStatusEEvent("SaveErrorDataChanged", "", true);
				dataRefresh(m_rowChanged);
				return SAVE_ERROR;
			}

			/**
			 *	Save to Database
			 */
			//
			String whereClause = singleRowWHERE.toString();
			if (whereClause.length() == 0)
				whereClause = multiRowWHERE.toString();
			if (m_inserting)
			{
				log.fine("Inserting ...");
				if (manualUpdate)
				{
					String sql = createUpdateSql(true, null);
					int no = DB.executeUpdateEx (sql, null);	//	no Trx
					if (no != 1)
						log.log(Level.SEVERE, "Insert #=" + no + " - " + sql);
				}
				else
					rs.insertRow();
			}
			else
			{
				log.fine("Updating ... " + whereClause);
				if (manualUpdate)
				{
					String sql = createUpdateSql(false, whereClause);
					int no = DB.executeUpdateEx (sql, null);	//	no Trx
					if (no != 1)
						log.log(Level.SEVERE, "Update #=" + no + " - " + sql);
				}
				else
					rs.updateRow();
			}

			log.fine("Committing ...");
			DB.commit(true, null);	//	no Trx
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
			//
			lobSave(whereClause);
			
			//	Need to re-read row to get ROWID, Key, DocumentNo, Trigger, virtual columns
			log.fine("Reading ... " + whereClause);
			StringBuffer refreshSQL = new StringBuffer(m_SQL_Select)
				.append(" WHERE ").append(whereClause);
			pstmt = DB.prepareStatement(refreshSQL.toString(), null);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				rowDataDB = readData(rs);
				//	update buffer
				setDataAtRow(m_rowChanged, rowDataDB);
				if (m_virtual)
				{
					MSort sort = m_sort.get(m_rowChanged);
					int oldId = sort.index;
					int newId = getKeyID(m_rowChanged);
					if (newId != oldId)
					{
						sort.index = newId;
						Object[] data = m_virtualBuffer.remove(oldId);
						m_virtualBuffer.put(newId, data);
					}
				}
				fireTableRowsUpdated(m_rowChanged, m_rowChanged);
			}
			else
				log.log(Level.SEVERE, "Inserted row not found");
			//
		}
		catch (Exception e)
		{

			String msg = "SaveError";
			if (DBException.isUniqueContraintError(e))		//	Unique Constraint
			{
				log.log(Level.SEVERE, "Key Not Unique", e);
				msg = "SaveErrorNotUnique";
			}
			else
				log.log(Level.SEVERE, select.toString(), e);
			fireDataStatusEEvent(msg, e.getLocalizedMessage(), true);
			return SAVE_ERROR;
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; 
			pstmt = null;
		}
		//	everything ok
		m_rowData = null;
		m_changed = false;
		m_compareDB = true;
		m_rowChanged = -1;
		m_newRow = -1;
		m_inserting = false;
		fireDataStatusIEvent("Saved", "");
		//
		log.info("fini");
		return SAVE_OK;
	}	//	dataSave

	/**
	 * 	Save via PO
	 *	@param Record_ID
	 *	@return SAVE_ERROR or SAVE_OK
	 *	@throws Exception
	 */
	private char dataSavePO (int Record_ID) throws Exception
	{
		log.fine("ID=" + Record_ID);
		//
		Object[] rowData = getDataAtRow(m_rowChanged);
		//
		MTable table = MTable.get (m_ctx, m_AD_Table_ID);
		PO po = null;
		if (Record_ID != -1)
			po = table.getPO(Record_ID, null);
		else	//	Multi - Key
			po = table.getPO(getWhereClause(rowData), null);
		//	No Persistent Object
		if (po == null)
			throw new ClassNotFoundException ("No Persistent Object");
		
		int size = m_fields.size();
		for (int col = 0; col < size; col++)
		{
			GridField field = (GridField)m_fields.get (col);
			if (field.isVirtualColumn())
				continue;
			String columnName = field.getColumnName ();
			Object value = rowData[col];
			Object oldValue = m_rowData[col];
			//	RowID
			if (field.getDisplayType() == DisplayType.RowID)
				; 	//	ignore

			//	Nothing changed & null
			else if (oldValue == null && value == null)
				;	//	ignore
			
			//	***	Data changed ***
			else if (m_inserting || isValueChanged(oldValue, value) )
			{
				//	Check existence
				int poIndex = po.get_ColumnIndex(columnName);
				if (poIndex < 0)
				{
					//	Custom Fields not in PO
					po.set_CustomColumn(columnName, value);
				//	log.log(Level.SEVERE, "Column not found: " + columnName);
					continue;
				}
				
				Object dbValue = po.get_Value(poIndex);
				if (m_inserting 
					|| !m_compareDB
					//	Original == DB
					|| (oldValue == null && dbValue == null)
					|| (oldValue != null && oldValue.equals (dbValue))
					//	Target == DB (changed by trigger to new value already)
					|| (value == null && dbValue == null)
					|| (value != null && value.equals (dbValue)) )
				{
					po.set_ValueNoCheck (columnName, value);
				}
				//	Original != DB
				else
				{
					String msg = columnName 
						+ "= " + oldValue 
							+ (oldValue==null ? "" : "(" + oldValue.getClass().getName() + ")")
						+ " != DB: " + dbValue 
							+ (dbValue==null ? "" : "(" + dbValue.getClass().getName() + ")")
						+ " -> New: " + value 
							+ (value==null ? "" : "(" + value.getClass().getName() + ")");
				//	CLogMgt.setLevel(Level.FINEST);
				//	po.dump();
					fireDataStatusEEvent("SaveErrorDataChanged", msg, true);
					dataRefresh(m_rowChanged);
					return SAVE_ERROR;
				}
			}	//	Data changed

		}	//	for every column

		if (!po.save())
		{
			String msg = "SaveError";
			String info = "";
			ValueNamePair ppE = CLogger.retrieveError();
			if (ppE != null)
			{
				msg = ppE.getValue();
				info = ppE.getName();
				//	Unique Constraint
				if (DBException.isUniqueContraintError(CLogger.retrieveException()))
					msg = "SaveErrorNotUnique";
			}
			fireDataStatusEEvent(msg, info, true);
			return SAVE_ERROR;
		}
		else if (m_virtual && po.get_ID() > 0)
		{
			//update ID
			MSort sort = m_sort.get(m_rowChanged);
			int oldid = sort.index;
			if (oldid != po.get_ID())
			{
				sort.index = po.get_ID();
				Object[] data = m_virtualBuffer.remove(oldid);
				data[m_indexKeyColumn] = sort.index;
				m_virtualBuffer.put(sort.index, data);
			}
		}
		
		//	Refresh - update buffer
		String whereClause = po.get_WhereClause(true);
		log.fine("Reading ... " + whereClause);
		StringBuffer refreshSQL = new StringBuffer(m_SQL_Select)
			.append(" WHERE ").append(whereClause);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(refreshSQL.toString(), null);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				Object[] rowDataDB = readData(rs);
				//	update buffer
				setDataAtRow(m_rowChanged, rowDataDB);
				fireTableRowsUpdated(m_rowChanged, m_rowChanged);
			}
		}
		catch (SQLException e)
		{
			String msg = "SaveError";
			log.log(Level.SEVERE, refreshSQL.toString(), e);
			fireDataStatusEEvent(msg, e.getLocalizedMessage(), true);
			return SAVE_ERROR;
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}

		//	everything ok
		m_rowData = null;
		m_changed = false;
		m_compareDB = true;
		m_rowChanged = -1;
		m_newRow = -1;
		m_inserting = false;
		//
		ValueNamePair pp = CLogger.retrieveWarning();
		if (pp != null)
		{
			String msg = pp.getValue();
			String info = pp.getName();
			fireDataStatusEEvent(msg, info, false);
		}
		else
		{
			pp = CLogger.retrieveInfo();
			String msg = "Saved";
			String info = "";
			if (pp != null)
			{
				msg = pp.getValue();
				info = pp.getName();
			}
			fireDataStatusIEvent(msg, info);
		}
		//
		log.config("fini");
		return SAVE_OK;
	}	//	dataSavePO
	
	/**
	 * 	Get Record Where Clause from data (single key or multi-parent)
	 *	@param rowData data
	 *	@return where clause or null
	 */
	private String getWhereClause (Object[] rowData)
	{
		int size = m_fields.size();
		StringBuffer singleRowWHERE = null;
		StringBuffer multiRowWHERE = null;
		for (int col = 0; col < size; col++)
		{
			GridField field = (GridField)m_fields.get (col);
			if (field.isKey())
			{
				String columnName = field.getColumnName();
				Object value = rowData[col]; 
				if (value == null)
				{
					log.log(Level.WARNING, "PK data is null - " + columnName);
					return null;
				}
				if (columnName.endsWith ("_ID"))
					singleRowWHERE = new StringBuffer(columnName)
						.append ("=").append (value);
				else
					singleRowWHERE = new StringBuffer(columnName)
						.append ("=").append (DB.TO_STRING(value.toString()));
			}
			else if (field.isParentColumn())
			{
				String columnName = field.getColumnName();
				Object value = rowData[col]; 
				if (value == null)
				{
					log.log(Level.INFO, "FK data is null - " + columnName);
					continue;
				}
				if (multiRowWHERE == null)
					multiRowWHERE = new StringBuffer();
				else
					multiRowWHERE.append(" AND ");
				if (columnName.endsWith ("_ID"))
					multiRowWHERE.append (columnName)
						.append ("=").append (value);
				else
					multiRowWHERE.append (columnName)
						.append ("=").append (DB.TO_STRING(value.toString()));
			}
		}	//	for all columns
		if (singleRowWHERE != null)
			return singleRowWHERE.toString();
		if (multiRowWHERE != null)
			return multiRowWHERE.toString();
		log.log(Level.WARNING, "No key Found");
		return null;
	}	//	getWhereClause
	
	/*************************************************************************/

	private ArrayList<String>	m_createSqlColumn = new ArrayList<String>();
	private ArrayList<String>	m_createSqlValue = new ArrayList<String>();

	/**
	 * 	Prepare SQL creation
	 * 	@param columnName column name
	 * 	@param value value
	 */
	private void createUpdateSql (String columnName, String value)
	{
		m_createSqlColumn.add(columnName);
		m_createSqlValue.add(value);
		log.finest("#" + m_createSqlColumn.size()
				+ " - " + columnName + "=" + value);
	}	//	createUpdateSQL

	/**
	 * 	Create update/insert SQL
	 * 	@para
