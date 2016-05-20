/*
 *  Copyright (C) 2001 David Hoag
 *  ObjectWave Corporation
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *  For a full copy of the license see:
 *  http://www.opensource.org/licenses/lgpl-license.html
 */
package com.objectwave.persist.sqlConstruction;
import com.objectwave.persist.*;
import com.objectwave.utility.StringManipulator;
import com.objectwave.logging.MessageLog;
import java.util.*;
/**
 *  A class that handles the creation of SQL Select statements. For example:
 *  <pre>
 *
 *  SQLSelect sql = new SQLSelect("TABLENAME");</pre>
 *
 * @author  Dave Hoag
 * @version  $Date: 2005/02/13 19:09:22 $ $Revision: 2.8 $
 * @see  com.objectwave.persist.sqlConstruction.SQLAssembler
 */
public class SQLSelect extends SQLObject implements SQLAssembler
{
	/**
	 */
	public static char nextAlias = '0';
	/**
	 */
	public ArrayList columnList;
	/**
	 */
	public StringBuffer columnListBuffer;

	ArrayList table;
	StringBuffer reusableStatementBuffer;
	Vector whereClause;
	boolean count;
	boolean distinct;
	boolean forUpdateInd;
	boolean orderBy;
	StringBuffer orderByList;
	HashMap pathToAlias = new HashMap();

	char alias;
	int rowLimit;
	boolean isDelete;
	/**
	 */
	public SQLSelect()
	{
	}
	/**
	 *  Create a SQLSelect to find information from the specified table.
	 *
	 * @param  tableName The name of the table from which this statement is
	 *      selecting data.
	 */
	public SQLSelect(String tableName)
	{
		initializeAttributes(tableName, false);
	}
	/**
	 * @param  tableName The name of the table from which this statement is
	 *      selecting data.
	 * @param  exclusiveReadLock Lock the table using database level locking.
	 */
	public SQLSelect(String tableName, boolean exclusiveReadLock)
	{
		initializeAttributes(tableName, exclusiveReadLock);
	}
	/**
	 *  Table aliasing allows for the joining of tables with out worry of column
	 *  name overlap.
	 *
	 * @param  aValue The new NextAlias value
	 */
	public static void setNextAlias(char aValue)
	{
		nextAlias = aValue;
	}
	/**
	 *  Use the static member 'nextAlias' to keep track of the next available
	 *  alias.
	 *
	 * @return  A char of the available alias. (A --> Z).
	 */
	protected static char getNextAlias()
	{
		if(nextAlias == '0')
		{
			refreshNextAlias();
		}
		return nextAlias;
	}
	/**
	 *  Create new alias and return it. Get the next alias from the static data
	 *  member, update the next alias value, and return the available alias.
	 *
	 * @return  char That is to represent a table alias.
	 */
	protected static synchronized char generateNextAlias()
	{
		char next = getNextAlias();
		refreshNextAlias();
		return next;
	}
	/**
	 *  Update the static member nextAlias to contain the next available table
	 *  alias. We start with A and when we get to Z we just start over again. This
	 *  obviously limits the number of tables in one select statement to 26.
	 */
	protected static synchronized void refreshNextAlias()
	{
		if(nextAlias == '0' || nextAlias == 'Z')
		{
			setNextAlias('A');
		}
		else
		{
			char c = (char) ((int) nextAlias + 1);
			//The following should be impossible!
			if(!Character.isJavaIdentifierStart(c))
			{
				MessageLog.warn(null, "Alias is not a javaIdentifierStart " + c);
			}
			setNextAlias(c);
		}
	}
	/**
	 *  A sql select could span multiple tables.
	 *
	 * @param  tableName The new TableName value
	 */
	public void setTableName(final String tableName)
	{
		initializeAttributes(tableName, false);
	}
	/**
	 *  Change this query to a delete statement.
	 *
	 * @param  value true would have this statement start with 'delete' instead of
	 *      'select'.
	 */
	public void setAsDelete(boolean value)
	{
		isDelete = value;
		if(isDelete)
		{
			int size = table.size();
			for(int i = 0; i < size; i++)
			{
				String tableName = (String) table.get(i);
				int idx = tableName.lastIndexOf(" " + getAlias());
				if(idx > 0)
				{
					tableName = tableName.substring(0, idx);
					table.set(i, tableName);
				}
			}
			size = getWhereClause().size();
			for(int i = 0; i < size; i++)
			{
				String clause = (String) whereClause.get(i);
				clause = StringManipulator.replaceAllWith(clause, "" + getAlias() + '.', "");
				whereClause.set(i, clause);
			}
		}
	}
	/**
	 *  Limit the result set the provided number of rows.
	 *
	 * @param  limit The number of rows to limit in the result set.
	 */
	public void setRowLimit(int limit)
	{
		rowLimit = limit;
	}
	/**
	 *  Alias' are used to prevent name collisions with columns from multiple
	 *  tables.
	 *
	 * @param  c The new Alias value
	 * @see  #getAlias()
	 */
	public void setAlias(char c)
	{
		alias = c;
	}
	/**
	 *  A state variable that indicates that this select is merely doing a count of
	 *  matching elements.
	 *
	 * @param  value boolean true if we are to find only a count of elements that
	 *      would match our constraints.
	 */
	public void setCount(boolean value)
	{
		count = value;
	}
	/**
	 *  Sets the Distinct attribute of the SQLSelect object
	 *
	 * @param  value The new Distinct value
	 */
	public void setDistinct(boolean value)
	{
		distinct = value;
	}
	/**
	 *  A state variable that indicates if there are order by constraints on this
	 *  select.
	 *
	 * @param  value true if this select statement is to include order by
	 *      constraints.
	 */
	public void setOrderBy(boolean value)
	{
		orderBy = value;
	}
	/**
	 *  Alias are used to eliminate problems when the same database column name is
	 *  found on two tables in a single select statement. The table is given an
	 *  alias and that alias is used when providing the column names.
	 *
	 * @return  The Alias value
	 */
	public char getAlias()
	{
		return alias;
	}
	/**
	 * @return  java.lang.StringBuffer containing legal sql code.
	 */
	public StringBuffer getSqlStatement()
	{
		if(reusableStatementBuffer == null)
		{
			reusableStatementBuffer = new StringBuffer();
		}
		formatSelectList(reusableStatementBuffer);
		formatTableList(reusableStatementBuffer);
		formatWhereClause(reusableStatementBuffer);
		formatForUpdateClause(reusableStatementBuffer);
		formatOrderBy(reusableStatementBuffer);

		return reusableStatementBuffer;
	}
	/**
	 *  Get the elements that will make up the where clause.
	 *
	 * @return  The WhereClause value
	 */
	public Vector getWhereClause()
	{
		return whereClause;
	}
	/**
	 *  The result set is limited to the result of this method.
	 *
	 * @return  int The row limit for this select.
	 */
	protected int getRowLimit()
	{
		return rowLimit;
	}
	/**
	 *  The number of columns that are found by this select statement.
	 *
	 * @return  The RecordSize value
	 */
	protected int getRecordSize()
	{
		return columnList.size();
	}
	/**
	 *  Find out what the alias is for the provided table and append it to the
	 *  'colName'.
	 *
	 * @param  tableName Hopefully the name of one of the tables in the sqlObj.
	 * @param  colName The column name without any alias.
	 * @return  The ColumnName value
	 */
	protected String getColumnName(final String tableName, final String colName)
	{
		String alias = null;
		for(int i = 0; i < table.size(); i++)
		{
			final String str = (String) table.get(i);
			int idx = str.indexOf(' ');
			final String tabName = str.substring(0, idx);
			if(tabName.equals(tableName))
			{
				alias = str.substring(idx, str.length());
				break;
			}
		}
		if(alias != null)
		{
			return alias += '.' + colName;
		}
		return colName;
	}
	/**
	 *  When returning the object to the ObjectPoolImpl, the object must be 'clean'.
	 */
	public void clean()
	{
		super.clean();
		//If table is not null, then we have values for all of these
		if(table != null)
		{
			table.clear();
			columnList.clear();
			columnListBuffer.setLength(0);
			whereClause.clear();
		}
		if(reusableStatementBuffer != null)
		{
			reusableStatementBuffer.setLength(0);
		}
		count = false;
		distinct = false;
		forUpdateInd = false;
		orderBy = false;
		orderByList = null;

		alias = 0;
		rowLimit = 0;
		isDelete = false;
		pathToAlias.clear();
	}
	/**
	 *  Used for adding one element May update the instance variable 'first' to
	 *  indicate that at least one column has been added to the column list buffer.
	 *
	 * @param  aVal The feature to be added to the ColumnList attribute
	 */
	public void addColumnList(String aVal)
	{
		boolean first = columnListBuffer.length() == 0;
		if(first)
		{
			first = false;
		}
		else
		{
			columnListBuffer.append(", ");
		}

		columnListBuffer.append(aVal);
		columnList.add(aVal);
	}
	/**
	 *  Add the clause to our list of where clauses. The clause is expected to be
	 *  complete, meaning the column name contains the appropriate alias and both
	 *  sides of the comparison operator are provided.
	 *
	 * @param  clause String of the clause. Ex. "a.tableField = 'aValue'"
	 */
	public void addWhereClause(String clause)
	{
		whereClause.addElement(clause);
	}
	/**
	 *  Empty the list of columns.
	 */
	public void clearColumnList()
	{
		columnList = new ArrayList();
		columnListBuffer = new StringBuffer();
	}
	/**
	 *  This can not be called until the SQL is completely built...other than the
	 *  Order by clause. Since this is not done until the end, concerns about joins
	 *  and the such are not an issue.
	 *
	 * @param  source Persistence This is usually the persistent object for which
	 *      we are querying
	 * @param  paths The feature to be added to the OrderBy attribute
	 */
	public void addOrderBy(Vector paths, Persistence source)
	{
		Enumeration e = paths.elements();
		while(e.hasMoreElements())
		{
			String path = (String) e.nextElement();
			boolean descend = false;
			if(path.charAt(0) == '!')
			{
				path = path.substring(1, path.length());
				descend = true;
			}
			final String columnName = findColumnName(source, path);
			addOrderByImpl(columnName, descend);
		}
	}
	/**
	 *  Special support for dealing with 'any of (values,...)' clauses.
	 *
	 * @param  column The column name. The left hand side of the comparison.
	 * @param  values java.util.Vector The objects to place in the any of clause.
	 * @param  compareWith
	 */
	public void insertAnyOfClause(final String compareWith, final String column, final Vector values)
	{
		if(values == null || values.size() == 0)
		{
			return;
			//No values in the any of list. Ignore this where clause.
		}
		StringBuffer colName = new StringBuffer();
		colName.append(getAlias());
		colName.append('.');
		colName.append(column);

		StringBuffer clause = new StringBuffer(colName.toString() + ' ' + compareWith + " (");
		Enumeration e = values.elements();
		//Build the list of any of elements.
		while(e.hasMoreElements())
		{
			formatValue(e.nextElement(), clause);
			if(e.hasMoreElements())
			{
				clause.append(", ");
			}
		}
		clause.append(')');
		addWhereClause(clause.toString());
	}
	/**
	 *  The default where clause will do an '=' comparison.
	 *
	 * @param  column The column name. The left hand side of the comparison.
	 * @param  value The right hand side of the comparison.
	 */
	public void insertWhereClause(final String column, final Object value)
	{
		insertWhereClause("=", column, value);
	}
	/**
	 *  Add a where clause to the current select statement. The clause will add the
	 *  table alias, the parameter column, the comparsion operator and finally a
	 *  formatted version of the value. Ex. insertWhereClause("=", "aColumn",
	 *  "aValue") would add the following where clause where a. is the table alias.
	 *  a.aColumn = 'aValue'
	 *
	 * @param  compareWith The comparison operator. Ex. '=', ' <=', etc...
	 * @param  column The column name. The left hand side of the comparison.
	 * @param  value The right hand side of the comparison.
	 */
	public void insertWhereClause(final String compareWith, final String column, final Object value)
	{
		StringBuffer clause = createWhereClause(compareWith, column);
		clause.append(' ');
		//The following if would break the sql statement even if the 'formatValue' method
		//was not called.
//		if (value != null && !(value instanceof String && ((String)value).length()==0))
		formatValue(value, clause);

		addWhereClause(clause.toString());
	}
	/**
	 * @param  compareWith
	 * @param  column
	 * @todo  Document
	 */
	public void insertWhereClause(final String compareWith, final String column)
	{
		StringBuffer clause = createWhereClause(compareWith, column);
		addWhereClause(clause.toString());
	}
	/**
	 * Add the alias to the column name & append the compare statement.
	 * ex. "A.columnName != 9" <br>
	 *  The value to be compared is a part of the compareStatement parameter.
	 *
	 * @param  compareStatment String A string like "!= 9".
	 * @param  columnName The column name. The left hand side of the comparison.
	 * @return  A string buffer containing the new elements.
	 */
	public final StringBuffer createWhereClause(final String compareStatment, final String columnName)
	{
		StringBuffer clause = new StringBuffer();
		clause.append(getAlias());
		clause.append('.');
		clause.append(columnName);
		clause.append(' ');
		clause.append(compareStatment);

		return clause;
	}
	/**
	 *  Very similar to insertWhereClause except the value has already been
	 *  formatted. The value is part of the compareStatement parameter.
	 *
	 * @param  columnName The column name. The left hand side of the comparison.
	 * @param  compareStatement
	 */
	public void insertConstraintWhereClause(final String compareStatement, final String columnName)
	{
		StringBuffer clause = createWhereClause(compareStatement, columnName);

		addWhereClause(clause.toString());
	}
	/**
	 *  Join with another formatted sql statement.
	 *
	 * @param  sqlObj Another sql select representing the table to which this is
	 *      joining.
	 * @param  sourceCol A column on this SQLSelect that will join with a column on
	 *      the parameter SQLSelect.
	 * @param  withCol A column on the parameter SQLSelect that will join with a
	 *      column on this SQLSelect.
	 * @param  attribLink
	 */
	public void joinWith(final String attribLink, final SQLSelect sqlObj, final String sourceCol, final String withCol)
	{
		joinWith(attribLink, sqlObj, sourceCol, withCol, "=");
	}

	/**
	 *  Joins the receiver with sqlObj. A join condition is added to the
	 *  whereClause based on the column maps if provided. Adds all of the columns
	 *  in the parameter SQLSelect to this SQLSelect. Adds all of the tables in the
	 *  parameter SQLSelect to this SQLSelect.
	 *
	 * @param  sqlObj Another sql select representing the table to which this is
	 *      joining.
	 * @param  sourceCol A column on this SQLSelect that will join with a column on
	 *      the parameter SQLSelect.
	 * @param  withCol A column on the parameter SQLSelect that will join with a
	 *      column on this SQLSelect.
	 * @param  attribLink
	 * @param  compareOperator
	 */
	public void joinWith(final String attribLink, final SQLSelect sqlObj, final String sourceCol, final String withCol, final String compareOperator)
	{

		pathToAlias.put(attribLink, sqlObj.table.get(0));
		Set set = sqlObj.pathToAlias.entrySet();
		Iterator iter = set.iterator();
		while(iter.hasNext())
		{
			Map.Entry entry = (Map.Entry) iter.next();
			pathToAlias.put(attribLink + '.' + entry.getKey(), entry.getValue());
		}

		//Add the columns.
		ArrayList v = sqlObj.columnList;
		int sz = v.size();
		for(int i = 0; i < sz; i++)
		{
			columnList.add(v.get(i));
			//Already has the alias on the column
		}

		if(columnListBuffer.length() != 0 && sqlObj.columnListBuffer.length() != 0)
		{
			columnListBuffer.append(", ");
		}
		columnListBuffer.append(sqlObj.columnListBuffer);

		//Add the tables.
		sz = sqlObj.table.size();
		for(int i = 0; i < sz; i++)
		{
			table.add(sqlObj.table.get(i));
		}

		//Add the joining where clause.
		if(sourceCol != null && withCol != null)
		{
			String constraint = compareOperator + ' ' + sqlObj.getAlias() + '.' + withCol;
			StringBuffer clause = createWhereClause(constraint, sourceCol);
			addWhereClause(clause.toString());
			//addWhereClause(String.valueOf(alias) + '.' + sourceCol + compareOperator + sqlObj.getAlias() + '.' + withCol);
		}
		sz = sqlObj.whereClause.size();
		for(int i = 0; i < sz; i++)
		{
			whereClause.addElement(sqlObj.whereClause.elementAt(i));
		}
	}

	/**
	 *  Method used to add additional join columns for multiple primary key support.
	 *  Joins the receiver with sqlObj. A join condition is added to the
	 *  whereClause based on the column maps if provided. Adds all of the columns
	 *  in the parameter SQLSelect to this SQLSelect. Adds all of the tables in the
	 *  parameter SQLSelect to this SQLSelect.
	 *
	 * @param  sourceCol A column on this SQLSelect that will join with a column on
	 *      the parameter SQLSelect.
	 * @param  withCol A column on the parameter SQLSelect that will join with a
	 *      column on this SQLSelect.
	 * @param  compareOperator
	 * @param  sqlObj
	 */
	public void joinWith(final SQLSelect sqlObj, final String sourceCol, final String withCol, final String compareOperator)
	{
		//Add the joining where clause.
		if(sourceCol != null && withCol != null)
		{
			String constraint = compareOperator + ' ' + sqlObj.getAlias() + '.' + withCol;
			StringBuffer clause = createWhereClause(constraint, sourceCol);
			addWhereClause(clause.toString());
			//addWhereClause(String.valueOf(alias) + '.' + sourceCol + compareOperator + sqlObj.getAlias() + '.' + withCol);
		}
	}

	/**
	 *  Find the aliased column name that matchs the path.
	 *
	 * @param  p The source object from which the path statement will begin it's
	 *      search.
	 * @param  path
	 * @return  The columnName to use in this query that will correspond to the
	 *      'path'.
	 */
	public String findColumnName(final Persistence p, final String path)
	{
		int idx2 = path.lastIndexOf('.');
		String alias = "";
		String tableName = (String) table.get(0);
		if(idx2 > -1)
		{
			String hash = path.substring(0, idx2);
			tableName = (String) pathToAlias.get(hash);
		}
		int idx3 = tableName.indexOf(' ');
		alias = tableName.substring(idx3, tableName.length()) + ".";
		return findColumnName(p, path, alias);
	}
	/**
	 *  Find the aliased column name that matchs the path.
	 *
	 * @param  p The source object from which the path statement will begin it's
	 *      search.
	 * @param  path
	 * @param  alias
	 * @return  The columnName to use in this query that will correspond to the
	 *      'path'.
	 */
	public String findColumnName(Persistence p, String path, final String alias)
	{
		RDBPersistence pObj = null;
		if(p == null)
		{
			return null;
		}

		if(p.usesAdapter())
		{
			pObj = (RDBPersistence) p.getAdapter();
		}
		else
		{
			pObj = (RDBPersistence) p;
		}

		Vector v = pObj.getClassDescription();
		String compare = path;
		int idx = path.indexOf('.');
		if(idx > -1)
		{
			compare = path.substring(0, idx);
			path = path.substring(idx + 1, path.length());
		}
		else
		{
			path = null;
		}

		AttributeTypeColumn col = null;
		for(int i = 0; i < v.size(); i++)
		{
			col = (AttributeTypeColumn) v.elementAt(i);
			String fieldName = col.getField().getName();
			if(fieldName.equals(compare))
			{
				break;
			}
			col = null;
		}
		if(col == null)
		{
			System.out.println("RDBBroker>>findColumnName>>Failed on " + compare);
			return null;
		}
		if(path == null)
		{
			//Match found.
			return alias + col.getColumnName();
		}
		p = (Persistence) col.getValue(p);
		return findColumnName(p, path, alias);
	}
	/**
	 *  Initialize the instance variables to their default values.
	 *
	 * @param  tableName The name of the table on which this select will be
	 *      centered.
	 * @param  exclusiveReadLock
	 */
	protected void initializeAttributes(final String tableName, final boolean exclusiveReadLock)
	{
		if(table == null)
		{
			table = new ArrayList(5);
			columnList = new ArrayList();
			whereClause = new Vector(5);
			columnListBuffer = new StringBuffer();
		}
		insertTable(tableName, exclusiveReadLock);
	}
	/**
	 *  Add a list of columns to our list of columns.
	 *
	 * @param  columns The column names include in the select statement.
	 */
	protected void addColumnList(String[] columns)
	{
		char c = getAlias();
		boolean first = columnListBuffer.length() == 0;
		for(int i = 0; i < columns.length; i++)
		{
			if(first)
			{
				first = false;
			}
			else
			{
				columnListBuffer.append(", ");
			}

			columnListBuffer.append(c);
			columnListBuffer.append('.');
			columnListBuffer.append(columns[i]);

			columnList.add(columns[i]);
		}
	}
	/**
	 * @param  buf The buffer containing the statment being built.
	 */
	protected void formatForUpdateClause(StringBuffer buf)
	{
		if(forUpdateInd)
		{
			buf.append(" FOR UPDATE");
		}
	}
	/**
	 *  Assemble a string buffer that will represent the order by part of a Sql
	 *  statement. The parameter value must have the alias set before calling this
	 *  method.
	 *
	 * @param  orderByColumn A database column that is part of the query.
	 * @param  descend The feature to be added to the OrderByImpl attribute
	 */
	protected void addOrderByImpl(final String orderByColumn, final boolean descend)
	{
		String columnName;

		if(descend)
		{
			columnName = orderByColumn + " DESC";
		}
		else
		{
			columnName = orderByColumn;
		}
		if(orderByList == null)
		{
			orderByList = new StringBuffer(columnName);
		}
		else
		{
			orderByList.append(',');
			orderByList.append(columnName);
		}
	}
	/**
	 *  The point at which the order by portion of the Sql statement is added to
	 *  our statement.
	 *
	 * @param  buf The buffer containing the statment being built.
	 */
	protected void formatOrderBy(final StringBuffer buf)
	{
		if(orderBy)
		{
			if(orderByList == null || orderByList.length() == 0)
			{
				buf.append(" ORDER BY " + columnList.get(0));
			}
			else
			{
				buf.append(" ORDER BY " + orderByList);
			}
		}
	}
	/**
	 *  Format the beginning of the select statement.
	 *
	 * @param  buf The buffer containing the statment being built.
	 * @return
	 */
	protected StringBuffer formatSelectList(StringBuffer buf)
	{
		if(isDelete)
		{
			buf.append("DELETE ");
		}
		else
		{
			buf.append("SELECT ");
			if(count)
			{
				return buf.append("COUNT(*)");
			}
			if(distinct)
			{
				buf.append("DISTINCT ");
			}
			/*
			 *  If there is no columnList just return
			 *  the wildcard to select all.
			 */
			int size = getRecordSize();
			if(size == 0)
			{
				return buf.append("* ");
			}
			buf.append(columnListBuffer);
		}
		return buf;
	}
	/**
	 *  Format the list of tables that will be participating in this query.
	 *
	 * @param  buf The buffer containing the statment being built.
	 */
	protected void formatTableList(StringBuffer buf)
	{
		buf.append(" FROM ");
		int size = table.size();
		for(int i = 0; i < size; i++)
		{
			buf.append(table.get(i));
			if(i < size - 1)
			{
				buf.append(" ,");
			}
		}
	}
	/**
	 *  Constrain the result set with the where clause elements.
	 *
	 * @param  buf The buffer containing the statment being built.
	 */
	protected void formatWhereClause(StringBuffer buf)
	{
		if(rowLimit > 0)
		{
			addWhereClause("ROWNUM<=" + rowLimit);
		}
		int size = whereClause.size();
		if(size == 0)
		{
			return;
		}
		buf.append(" WHERE ");
		for(int i = 0; i < size - 1; i++)
		{
			buf.append(whereClause.elementAt(i));
			if(i < size - 1)
			{
				buf.append(" AND ");
			}
			// FIXME: DAH - AND as default?  Query Object?
		}
		buf.append(whereClause.elementAt(size - 1));
	}
	/**
	 *  Create new alias, update this object to know of this new alias, and return
	 *  the char alias value.
	 *
	 * @return  char That is to represent a table alias for this instance of
	 *      SQLSelect.
	 */
	protected char genNextAlias()
	{
		setAlias(generateNextAlias());
		return alias;
	}
	/**
	 *  Add a table to our list of tables. An alias is assigned to each table.
	 *
	 * @param  exclusiveReadLock boolean Indicating if the table should be locked.
	 * @param  tableName String Name of the table.
	 */
	protected void insertTable(final String tableName, final boolean exclusiveReadLock)
	{
		char c = genNextAlias();
		String tabName = tableName + ' ' + c;
		//I know that this string works for SQLServer.  I'm not sure it would work for
		//all databases.
		if(exclusiveReadLock)
		{
			tabName += " (TABLOCKX)";
		}
		table.add(tabName);
	}

}

