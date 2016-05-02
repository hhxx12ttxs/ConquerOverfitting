/* Copyright (c) 1995-2000, The Hypersonic SQL Group.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the Hypersonic SQL Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE HYPERSONIC SQL GROUP,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Hypersonic SQL Group.
 *
 *
 * For work added by the HSQL Development Group:
 *
 * Copyright (c) 2001-2008, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb;

import java.io.IOException;

import org.hsqldb.HsqlNameManager.HsqlName;
import org.hsqldb.index.RowIterator;
import org.hsqldb.lib.ArrayUtil;
import org.hsqldb.lib.HashMappedList;
import org.hsqldb.lib.HashSet;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.Iterator;
import org.hsqldb.lib.StringUtil;
import org.hsqldb.persist.CachedObject;
import org.hsqldb.persist.DataFileCache;
import org.hsqldb.persist.PersistentStore;
import org.hsqldb.rowio.RowInputBinary;
import org.hsqldb.rowio.RowInputInterface;
import org.hsqldb.store.ValuePool;

import j2me.math.Number;

// fredt@users 20020130 - patch 491987 by jimbag@users - made optional
// fredt@users 20020405 - patch 1.7.0 by fredt - quoted identifiers
// for sql standard quoted identifiers for column and table names and aliases
// applied to different places
// fredt@users 20020225 - patch 1.7.0 - restructuring
// some methods moved from Database.java, some rewritten
// changes to several methods
// fredt@users 20020225 - patch 1.7.0 - ON DELETE CASCADE
// fredt@users 20020225 - patch 1.7.0 - named constraints
// boucherb@users 20020225 - patch 1.7.0 - multi-column primary keys
// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// tony_lai@users 20020820 - patch 595099 - user defined PK name
// tony_lai@users 20020820 - patch 595172 - drop constraint fix
// kloska@users 20021030 - patch 1.7.2 - ON UPDATE CASCADE | SET NULL | SET DEFAULT
// kloska@users 20021112 - patch 1.7.2 - ON DELETE SET NULL | SET DEFAULT
// fredt@users 20021210 - patch 1.7.2 - better ADD / DROP INDEX for non-CACHED tables
// fredt@users 20030901 - patch 1.7.2 - allow multiple nulls for UNIQUE columns
// fredt@users 20030901 - patch 1.7.2 - reworked IDENTITY support
// achnettest@users 20040130 - patch 878288 - bug fix for new indexes in memory tables by Arne Christensen
// boucherb@users 20040327 - doc 1.7.2 - javadoc updates
// boucherb@users 200404xx - patch 1.7.2 - proper uri for getCatalogName
// fredt@users 20050000 - 1.8.0 updates in several areas
// fredt@users 20050220 - patch 1.8.0 enforcement of DECIMAL precision/scale

/**
 *  Holds the data structures and methods for creation of a database table.
 *
 *
 * Extensively rewritten and extended in successive versions of HSQLDB.
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @version 1.8.0
 * @since Hypersonic SQL
 */
public class Table extends BaseTable {

    // types of table
    public static final int SYSTEM_TABLE    = 0;
    public static final int SYSTEM_SUBQUERY = 1;
    public static final int TEMP_TABLE      = 2;
    public static final int MEMORY_TABLE    = 3;
    public static final int CACHED_TABLE    = 4;
    public static final int TEMP_TEXT_TABLE = 5;
    public static final int TEXT_TABLE      = 6;
    public static final int VIEW            = 7;

// boucherb@users - for future implementation of SQL standard INFORMATION_SCHEMA
    static final int SYSTEM_VIEW = 8;

    // main properties
// boucherb@users - access changed in support of metadata 1.7.2
    public HashMappedList columnList;                 // columns in table
    private int[]         primaryKeyCols;             // column numbers for primary key
    private int[]         primaryKeyTypes;            // types for primary key
    private int[]         primaryKeyColsSequence;     // {0,1,2,...}
    int[]                 bestRowIdentifierCols;      // column set for best index
    boolean               bestRowIdentifierStrict;    // true if it has no nullable column
    int[]                 bestIndexForColumn;         // index of the 'best' index for each column
    Index                 bestIndex;                  // the best index overall - null if there is no user-defined index
    int            identityColumn;                    // -1 means no such row
    NumberSequence identitySequence;                  // next value of identity column
    NumberSequence rowIdSequence;                     // next value of optional rowid

// -----------------------------------------------------------------------
    Constraint[]      constraintList;                 // constrainst for the table
    HsqlArrayList[]   triggerLists;                   // array of trigger lists
    private int[]     colTypes;                       // fredt - types of columns
    private int[]     colSizes;                       // fredt - copy of SIZE values for columns
    private int[]     colScales;                      // fredt - copy of SCALE values for columns
    private boolean[] colNullable;                    // fredt - modified copy of isNullable() values
    private Expression[] colDefaults;                 // fredt - expressions of DEFAULT values
    private int[]        defaultColumnMap;            // fred - holding 0,1,2,3,...
    private boolean      hasDefaultValues;            //fredt - shortcut for above
    boolean              sqlEnforceSize;              // inherited from the database -

    // properties for subclasses
    protected int           columnCount;              // inclusive the hidden primary key
    public Database         database;
    protected DataFileCache cache;
    protected HsqlName      tableName;                // SQL name
    private int             tableType;
    protected boolean       isReadOnly;
    protected boolean       isTemp;
    protected boolean       isCached;
    protected boolean       isText;
    protected boolean       isMemory;
    private boolean         isView;
    protected boolean       isLogged;
    protected int           indexType;                // fredt - type of index used
    protected boolean       onCommitPreserve;         // for temp tables

    //
    PersistentStore rowStore;
    Index[]         indexList;                        // vIndex(0) is the primary key index

    /**
     *  Constructor
     *
     * @param  db
     * @param  name
     * @param  type
     * @param  sessionid
     * @exception  HsqlException
     */
    Table(Database db, HsqlName name, int type) throws HsqlException {

        database         = db;
        sqlEnforceSize   = db.sqlEnforceStrictSize;
        identitySequence = new NumberSequence(null, 0, 1, Types.BIGINT);
        rowIdSequence    = new NumberSequence(null, 0, 1, Types.BIGINT);

        switch (type) {

            case SYSTEM_SUBQUERY :
                isTemp   = true;
                isMemory = true;
                break;

            case SYSTEM_TABLE :
                isMemory = true;
                break;

            case CACHED_TABLE :
                if (DatabaseURL.isFileBasedDatabaseType(db.getType())) {
                    cache     = db.logger.getCache();
                    isCached  = true;
                    isLogged  = !database.isFilesReadOnly();
                    indexType = Index.DISK_INDEX;
                    rowStore  = new RowStore();

                    break;
                }

                type = MEMORY_TABLE;
            case MEMORY_TABLE :
                isMemory = true;
                isLogged = !database.isFilesReadOnly();
                break;

            case TEMP_TABLE :
                isMemory = true;
                isTemp   = true;
                break;

            case TEMP_TEXT_TABLE :
                if (!DatabaseURL.isFileBasedDatabaseType(db.getType())) {
                    throw Trace.error(Trace.DATABASE_IS_MEMORY_ONLY);
                }

                isTemp     = true;
                isText     = true;
                isReadOnly = true;
                indexType  = Index.POINTER_INDEX;
                rowStore   = new RowStore();
                break;

            case TEXT_TABLE :
                if (!DatabaseURL.isFileBasedDatabaseType(db.getType())) {
                    throw Trace.error(Trace.DATABASE_IS_MEMORY_ONLY);
                }

                isText    = true;
                indexType = Index.POINTER_INDEX;
                rowStore  = new RowStore();
                break;

            case VIEW :
            case SYSTEM_VIEW :
                isView = true;
                break;
        }

        // type may have changed above for CACHED tables
        tableType       = type;
        tableName       = name;
        primaryKeyCols  = null;
        primaryKeyTypes = null;
        identityColumn  = -1;
        columnList      = new HashMappedList();
        indexList       = new Index[0];
        constraintList  = new Constraint[0];
        triggerLists    = new HsqlArrayList[TriggerDef.NUM_TRIGS];

// ----------------------------------------------------------------------------
// akede@users - 1.7.2 patch Files readonly
        // Changing the mode of the table if necessary
        if (db.isFilesReadOnly() && isFileBased()) {
            setIsReadOnly(true);
        }

// ----------------------------------------------------------------------------
    }

    boolean equals(Session session, String name) {

/*
        if (isTemp && (session != null
                       && session.getId() != ownerSessionId)) {
            return false;
        }
*/
        return (tableName.name.equals(name));
    }

    boolean equals(String name) {
        return (tableName.name.equals(name));
    }

    boolean equals(HsqlName name) {
        return (tableName.equals(name));
    }

    public final boolean isText() {
        return isText;
    }

    public final boolean isTemp() {
        return isTemp;
    }

    public final boolean isReadOnly() {
        return isDataReadOnly();
    }

    final boolean isView() {
        return isView;
    }

    final int getIndexType() {
        return indexType;
    }

    public final int getTableType() {
        return tableType;
    }

    public boolean isDataReadOnly() {
        return isReadOnly;
    }

    /**
     * sets the isReadOnly flag, and invalidates the database's system tables as needed
     */
    protected void setIsReadOnly(boolean newReadOnly) {

        isReadOnly = newReadOnly;

        database.setMetaDirty(true);
    }

    /**
     * Used by INSERT, DELETE, UPDATE operations
     */
    void checkDataReadOnly() throws HsqlException {

        if (isDataReadOnly()) {
            throw Trace.error(Trace.DATA_IS_READONLY);
        }
    }

// ----------------------------------------------------------------------------
// akede@users - 1.7.2 patch Files readonly
    void setDataReadOnly(boolean value) throws HsqlException {

        // Changing the Read-Only mode for the table is only allowed if
        // the database can realize it.
        if (!value && database.isFilesReadOnly() && isFileBased()) {
            throw Trace.error(Trace.DATA_IS_READONLY);
        }

        isReadOnly = value;
    }

    /**
     * Text or Cached Tables are normally file based
     */
    boolean isFileBased() {
        return isCached || isText;
    }

    /**
     * For text tables
     */
    protected void setDataSource(Session s, String source, boolean isDesc,
                                 boolean newFile) throws HsqlException {
        throw (Trace.error(Trace.TABLE_NOT_FOUND));
    }

    /**
     * For text tables
     */
    protected String getDataSource() {
        return null;
    }

    /**
     * For text tables.
     */
    protected boolean isDescDataSource() {
        return false;
    }

    /**
     * For text tables.
     */
    public void setHeader(String header) throws HsqlException {
        throw Trace.error(Trace.TEXT_TABLE_HEADER);
    }

    /**
     * For text tables.
     */
    public String getHeader() {
        return null;
    }

    /**
     * determines whether the table is actually connected to the underlying data source.
     *
     *  <p>This method is available for text tables only.</p>
     *
     *  @see setDataSource
     *  @see disconnect
     *  @see isConnected
     */
    public boolean isConnected() {
        return true;
    }

    /**
     * connects the table to the underlying data source.
     *
     *  <p>This method is available for text tables only.</p>
     *
     *  @param session
     *      denotes the current session. Might be <code>null</code>.
     *
     *  @see setDataSource
     *  @see disconnect
     *  @see isConnected
     */
    public void connect(Session session) throws HsqlException {
        throw Trace.error(Trace.CANNOT_CONNECT_TABLE);
    }

    /**
     * disconnects the table from the underlying data source.
     *
     *  <p>This method is available for text tables only.</p>
     *
     *  @param session
     *      denotes the current session. Might be <code>null</code>.
     *
     *  @see setDataSource
     *  @see connect
     *  @see isConnected
     */
    public void disconnect(Session session) throws HsqlException {
        throw Trace.error(Trace.CANNOT_CONNECT_TABLE);
    }

    /**
     *  Adds a constraint.
     */
    void addConstraint(Constraint c) {

        constraintList =
            (Constraint[]) ArrayUtil.toAdjustedArray(constraintList, c,
                constraintList.length, 1);
    }

    /**
     *  Returns the list of constraints.
     */
    Constraint[] getConstraints() {
        return constraintList;
    }

    /**
     *  Returns the primary constraint.
     */
    Constraint getPrimaryConstraint() {
        return primaryKeyCols.length == 0 ? null
                                          : constraintList[0];
    }

/** @todo fredt - this can be improved to ignore order of columns in
     * multi-column indexes */

    /**
     *  Returns the index supporting a constraint with the given column signature.
     *  Only Unique constraints are considered.
     */
    Index getUniqueConstraintIndexForColumns(int[] col) {

        if (ArrayUtil.areEqual(getPrimaryIndex().getColumns(), col,
                               col.length, true)) {
            return getPrimaryIndex();
        }

        for (int i = 0, size = constraintList.length; i < size; i++) {
            Constraint c = constraintList[i];

            if (c.getType() != Constraint.UNIQUE) {
                continue;
            }

            if (ArrayUtil.areEqual(c.getMainColumns(), col, col.length,
                                   true)) {
                return c.getMainIndex();
            }
        }

        return null;
    }

    /**
     *  Returns any foreign key constraint equivalent to the column sets
     */
    Constraint getConstraintForColumns(Table tablemain, int[] colmain,
                                       int[] colref) {

        for (int i = 0, size = constraintList.length; i < size; i++) {
            Constraint c = constraintList[i];

            if (c.isEquivalent(tablemain, colmain, this, colref)) {
                return c;
            }
        }

        return null;
    }

    /**
     *  Returns any unique constraint equivalent to the column set
     */
    Constraint getUniqueConstraintForColumns(int[] cols) {

        for (int i = 0, size = constraintList.length; i < size; i++) {
            Constraint c = constraintList[i];

            if (c.isEquivalent(cols, Constraint.UNIQUE)) {
                return c;
            }
        }

        return null;
    }

    /**
     *  Returns any unique Constraint using this index
     *
     * @param  index
     * @return
     */
    Constraint getUniqueOrPKConstraintForIndex(Index index) {

        for (int i = 0, size = constraintList.length; i < size; i++) {
            Constraint c = constraintList[i];

            if (c.getMainIndex() == index
                    && (c.getType() == Constraint.UNIQUE
                        || c.getType() == Constraint.PRIMARY_KEY)) {
                return c;
            }
        }

        return null;
    }

    /**
     *  Returns the next constraint of a given type
     *
     * @param  from
     * @param  type
     * @return
     */
    int getNextConstraintIndex(int from, int type) {

        for (int i = from, size = constraintList.length; i < size; i++) {
            Constraint c = constraintList[i];

            if (c.getType() == type) {
                return i;
            }
        }

        return -1;
    }

// fredt@users 20020220 - patch 475199 - duplicate column

    /**
     *  Performs the table level checks and adds a column to the table at the
     *  DDL level. Only used at table creation, not at alter column.
     */
    void addColumn(Column column) throws HsqlException {

        if (findColumn(column.columnName.name) >= 0) {
            throw Trace.error(Trace.COLUMN_ALREADY_EXISTS,
                              column.columnName.name);
        }

        if (column.isIdentity()) {
            Trace.check(
                column.getType() == Types.INTEGER
                || column.getType() == Types.BIGINT, Trace.WRONG_DATA_TYPE,
                    column.columnName.name);
            Trace.check(identityColumn == -1, Trace.SECOND_PRIMARY_KEY,
                        column.columnName.name);

            identityColumn = columnCount;
        }

        if (primaryKeyCols != null) {
            Trace.doAssert(false, "Table.addColumn");
        }

        columnList.add(column.columnName.name, column);

        columnCount++;
    }

    /**
     *  Add a set of columns based on a ResultMetaData
     */
    void addColumns(Result.ResultMetaData metadata,
                    int count) throws HsqlException {

        for (int i = 0; i < count; i++) {
            Column column = new Column(
                database.nameManager.newHsqlName(
                    metadata.colLabels[i], metadata.isLabelQuoted[i]), true,
                        metadata.colTypes[i], metadata.colSizes[i],
                        metadata.colScales[i], false, null);

            addColumn(column);
        }
    }

    /**
     *  Adds a set of columns based on a compiled Select
     */
    void addColumns(Select select) throws HsqlException {

        int colCount = select.iResultLen;

        for (int i = 0; i < colCount; i++) {
            Expression e = select.exprColumns[i];
            Column column = new Column(
                database.nameManager.newHsqlName(
                    e.getAlias(), e.isAliasQuoted()), true, e.getDataType(),
                        e.getColumnSize(), e.getColumnScale(), false, null);

            addColumn(column);
        }
    }

    /**
     *  Returns the HsqlName object fo the table
     */
    public HsqlName getName() {
        return tableName;
    }

    public int getId() {
        return tableName.hashCode();
    }

    /**
     * Changes table name. Used by 'alter table rename to'.
     * Essential to use the existing HsqlName as this is is referenced by
     * intances of Constraint etc.
     */
    void rename(Session session, String newname,
                boolean isquoted) throws HsqlException {

        String oldname = tableName.name;

        tableName.rename(newname, isquoted);
        renameTableInCheckConstraints(session, oldname, newname);
    }

    /**
     *  Returns total column counts, including hidden ones.
     */
    int getInternalColumnCount() {
        return columnCount;
    }

    /**
     * returns a basic duplicate of the table without the data structures.
     */
    protected Table duplicate() throws HsqlException {

        Table t = new Table(database, tableName, tableType);

        t.onCommitPreserve = onCommitPreserve;

        return t;
    }

    /**
     * Match two columns arrays for length and type of columns
     *
     * @param col column array from this Table
     * @param other the other Table object
     * @param othercol column array from the other Table
     * @throws HsqlException if there is a mismatch
     */
    void checkColumnsMatch(int[] col, Table other,
                           int[] othercol) throws HsqlException {

        if (col.length != othercol.length) {
            throw Trace.error(Trace.COLUMN_COUNT_DOES_NOT_MATCH);
        }

        for (int i = 0; i < col.length; i++) {

            // integrity check - should not throw in normal operation
            if (col[i] >= columnCount || othercol[i] >= other.columnCount) {
                throw Trace.error(Trace.COLUMN_COUNT_DOES_NOT_MATCH);
            }

            if (getColumn(col[i]).getType()
                    != other.getColumn(othercol[i]).getType()) {
                throw Trace.error(Trace.COLUMN_TYPE_MISMATCH);
            }
        }
    }

// fredt@users 20020405 - patch 1.7.0 by fredt - DROP and CREATE INDEX bug

    /**
     * Constraints that need removing are removed outside this method.<br>
     * removeIndex is the index of an index to be removed, in which case
     * no change is made to columns <br>
     * When withoutindex is null,  adjust {-1 | 0 | +1} indicates if a
     * column is {removed | replaced | added}
     *
     */
    Table moveDefinition(int[] removeIndex, Column newColumn, int colIndex,
                         int adjust) throws HsqlException {

        Table tn = duplicate();

        // loop beyond the end in order to be able to add a column to the end
        // of the list
        for (int i = 0; i < columnCount + 1; i++) {
            if (i == colIndex) {
                if (adjust == 0) {
                    if (newColumn != null) {
                        tn.addColumn(newColumn);

                        continue;
                    }
                } else if (adjust > 0) {
                    tn.addColumn(newColumn);
                } else if (adjust < 0) {
                    continue;
                }
            }

            if (i == columnCount) {
                break;
            }

            tn.addColumn(getColumn(i));
        }

        // treat it the same as new table creation and
        int[] primarykey = primaryKeyCols.length == 0 ? null
                                                      : primaryKeyCols;

        if (primarykey != null) {
            int[] newpk = ArrayUtil.toAdjustedColumnArray(primarykey,
                colIndex, adjust);

            if (primarykey.length != newpk.length) {
                throw Trace.error(Trace.DROP_PRIMARY_KEY);
            } else {
                primarykey = newpk;
            }
        }

        tn.createPrimaryKey(getIndex(0).getName(), primarykey, false);

        tn.constraintList = constraintList;

        Index idx = null;

        if (removeIndex != null) {
            idx = getIndex(removeIndex, colIndex);
        }

        if (idx != null) {
            if (idx.isConstraint()) {
                throw Trace.error(Trace.COLUMN_IS_IN_CONSTRAINT);
            } else {
                throw Trace.error(Trace.COLUMN_IS_IN_INDEX);
            }
        }

        for (int i = 1; i < indexList.length; i++) {
            if (removeIndex != null && ArrayUtil.find(removeIndex, i) != -1) {
                continue;
            }

            tn.createAdjustedIndex(indexList[i], colIndex, adjust);
        }

        tn.triggerLists = triggerLists;

        return tn;
    }

    Index getIndex(int[] exclude, int colIndex) {

        for (int i = 1; i < indexList.length; i++) {
            if (exclude != null && ArrayUtil.find(exclude, i) != -1) {
                continue;
            }

            Index idx  = indexList[i];
            int[] cols = idx.getColumns();

            if (ArrayUtil.find(cols, colIndex) != -1) {
                return idx;
            }
        }

        return null;
    }

    private void copyIndexes(Table tn, int removeIndex, int colIndex,
                             int adjust) throws HsqlException {

        for (int i = 1; i < getIndexCount(); i++) {
            Index idx = indexList[i];

            if (removeIndex == i) {
                continue;
            }

            Index newidx = tn.createAdjustedIndex(idx, colIndex, adjust);

            if (newidx == null) {

                // column to remove is part of an index
                throw Trace.error(Trace.COLUMN_IS_IN_INDEX);
            }
        }
    }

    /**
     * cols == null means drop
     */
    Table moveDefinitionPK(int[] pkCols,
                           boolean withIdentity) throws HsqlException {

        // some checks
        if ((hasPrimaryKey() && pkCols != null)
                || (!hasPrimaryKey() && pkCols == null)) {
            throw Trace.error(Trace.DROP_PRIMARY_KEY);
        }

        Table tn = duplicate();

        for (int i = 0; i < columnCount; i++) {
            tn.addColumn(getColumn(i).duplicate(withIdentity));
        }

        tn.createPrimaryKey(getIndex(0).getName(), pkCols, true);

        tn.constraintList = constraintList;

        for (int i = 1; i < getIndexCount(); i++) {
            Index idx = getIndex(i);

            tn.createAdjustedIndex(idx, -1, 0);
        }

        tn.triggerLists = triggerLists;

        return tn;
    }

    /**
     * Updates the constraint and replaces references to the old table with
     * the new one, adjusting column index arrays by the given amount.
     */
    void updateConstraintsTables(Session session, Table old, int colindex,
                                 int adjust) throws HsqlException {

        for (int i = 0, size = constraintList.length; i < size; i++) {
            Constraint c = constraintList[i];

            c.replaceTable(old, this, colindex, adjust);

            if (c.constType == Constraint.CHECK) {
                recompileCheckConstraint(session, c);
            }
        }
    }

    private void recompileCheckConstraints(Session session)
    throws HsqlException {

        for (int i = 0, size = constraintList.length; i < size; i++) {
            Constraint c = constraintList[i];

            if (c.constType == Constraint.CHECK) {
                recompileCheckConstraint(session, c);
            }
        }
    }

    /**
     * Used after adding columns or indexes to the table.
     */
    private void recompileCheckConstraint(Session session,
                                          Constraint c) throws HsqlException {

        String     ddl       = c.core.check.getDDL();
        Tokenizer  tokenizer = new Tokenizer(ddl);
        Parser     parser    = new Parser(session, database, tokenizer);
        Expression condition = parser.parseExpression();

        c.core.check = condition;

        // this workaround is here to stop LIKE optimisation (for proper scripting)
        condition.setLikeOptimised();

        Select s = Expression.getCheckSelect(session, this, condition);

        c.core.checkFilter = s.tFilter[0];

        c.core.checkFilter.setAsCheckFilter();

        c.core.mainTable = this;
    }

    /**
     * Used for drop column.
     */
    void checkColumnInCheckConstraint(String colname) throws HsqlException {

        for (int i = 0, size = constraintList.length; i < size; i++) {
            Constraint c = constraintList[i];

            if (c.constType == Constraint.CHECK) {
                if (c.hasColumn(this, colname)) {
                    throw Trace.error(Trace.COLUMN_IS_REFERENCED, c.getName());
                }
            }
        }
    }

    /**
     * Used for retype column. Checks whether column is in an FK or is
     * referenced by a FK
     * @param colIndex index
     */
    void checkColumnInFKConstraint(int colIndex) throws HsqlException {

        for (int i = 0, size = constraintList.length; i < size; i++) {
            Constraint c = constraintList[i];

            if (c.hasColumn(colIndex)
                    && (c.getType() == Constraint.MAIN
                        || c.getType() == Constraint.FOREIGN_KEY)) {
                throw Trace.error(Trace.COLUMN_IS_REFERENCED,
                                  c.getName().name);
            }
        }
    }

    /**
     * Used for column defaults and nullability. Checks whether column is in an FK.
     * @param colIndex index of column
     * @param refOnly only check FK columns, not referenced columns
     */
    void checkColumnInFKConstraint(int colIndex,
                                   int actionType) throws HsqlException {

        for (int i = 0, size = constraintList.length; i < size; i++) {
            Constraint c = constraintList[i];

            if (c.hasColumn(colIndex)) {
                if (c.getType() == Constraint.FOREIGN_KEY
                        && (actionType == c.getUpdateAction()
                            || actionType == c.getDeleteAction())) {
                    throw Trace.error(Trace.COLUMN_IS_REFERENCED,
                                      c.getName().name);
                }
            }
        }
    }

    /**
     * Used for rename column.
     */
    private void renameColumnInCheckConstraints(String oldname,
            String newname, boolean isquoted) throws HsqlException {

        for (int i = 0, size = constraintList.length; i < size; i++) {
            Constraint c = constraintList[i];

            if (c.constType == Constraint.CHECK) {
                Expression.Collector coll = new Expression.Collector();

                coll.addAll(c.core.check, Expression.COLUMN);

                Iterator it = coll.iterator();

                for (; it.hasNext(); ) {
                    Expression e = (Expression) it.next();

                    if (e.getColumnName() == oldname) {
                        e.setColumnName(newname, isquoted);
                    }
                }
            }
        }
    }

    /**
     * Used for drop column.
     */
    private void renameTableInCheckConstraints(Session session,
            String oldname, String newname) throws HsqlException {

        for (int i = 0, size = constraintList.length; i < size; i++) {
            Constraint c = constraintList[i];

            if (c.constType == Constraint.CHECK) {
                Expression.Collector coll = new Expression.Collector();

                coll.addAll(c.core.check, Expression.COLUMN);

                Iterator it = coll.iterator();

                for (; it.hasNext(); ) {
                    Expression e = (Expression) it.next();

                    if (e.getTableName() == oldname) {
                        e.setTableName(newname);
                    }
                }
            }
        }

        recompileCheckConstraints(session);
    }

    /**
     *  Returns the count of user defined columns.
     */
    public int getColumnCount() {
        return columnCount;
    }

    /**
     *  Returns the count of indexes on this table.
     */
    public int getIndexCount() {
        return indexList.length;
    }

    /**
     *  Returns the identity column or null.
     */
    int getIdentityColumn() {
        return identityColumn;
    }

    /**
     *  Returns the index of given column name or throws if not found
     */
    int getColumnNr(String c) throws HsqlException {

        int i = findColumn(c);
        if (i == -1)
        	i = findColumn(c.toLowerCase());
        
        if (i == -1) {
            throw Trace.error(Trace.COLUMN_NOT_FOUND, c);
        }

        return i;
    }

    /**
     *  Returns the index of given column name or -1 if not found.
     */
    int findColumn(String c) {

        int index = columnList.getIndex(c);

        return index;
    }

    /**
     *  Returns the primary index (user defined or system defined)
     */
    public Index getPrimaryIndex() {
        return getIndex(0);
    }

    /**
     *  Return the user defined primary key column indexes, or empty array for system PK's.
     */
    public int[] getPrimaryKey() {
        return primaryKeyCols;
    }

    public int[] getPrimaryKeyTypes() {
        return primaryKeyTypes;
    }

    public boolean hasPrimaryKey() {
        return !(primaryKeyCols.length == 0);
    }

    int[] getBestRowIdentifiers() {
        return bestRowIdentifierCols;
    }

    boolean isBestRowIdentifiersStrict() {
        return bestRowIdentifierStrict;
    }

    /**
     * This method is called whenever there is a change to table structure and
     * serves two porposes: (a) to reset the best set of columns that identify
     * the rows of the table (b) to reset the best index that can be used
     * to find rows of the table given a column value.
     *
     * (a) gives most weight to a primary key index, followed by a unique
     * address with the lowest count of nullable columns. Otherwise there is
     * no best row identifier.
     *
     * (b) finds for each column an index with a corresponding first column.
     * It uses any type of visible index and accepts the first one (it doesn't
     * make any difference to performance).
     *
     * bestIndex is the user defined, primary key, the first unique index, or
     * the first non-unique index. NULL if there is no user-defined index.
     *
     */
    void setBestRowIdentifiers() {

        int[]   briCols      = null;
        int     briColsCount = 0;
        boolean isStrict     = false;
        int     nNullCount   = 0;

        // ignore if called prior to completion of primary key construction
        if (colNullable == null) {
            return;
        }

        bestIndex          = null;
        bestIndexForColumn = new int[columnList.size()];

        ArrayUtil.fillArray(bestIndexForColumn, -1);

        for (int i = 0; i < indexList.length; i++) {
            Index index     = indexList[i];
            int[] cols      = index.getColumns();
            int   colsCount = index.getVisibleColumns();

            if (i == 0) {

                // ignore system primary keys
                if (hasPrimaryKey()) {
                    isStrict = true;
                } else {
                    continue;
                }
            }

            if (bestIndexForColumn[cols[0]] == -1) {
                bestIndexForColumn[cols[0]] = i;
            }

            if (!index.isUnique()) {
                if (bestIndex == null) {
                    bestIndex = index;
                }

                continue;
            }

            int nnullc = 0;

            for (int j = 0; j < colsCount; j++) {
                if (!colNullable[cols[j]]) {
                    nnullc++;
                }
            }

            if (bestIndex != null) {
                bestIndex = index;
            }

            if (nnullc == colsCount) {
                if (briCols == null || briColsCount != nNullCount
                        || colsCount < briColsCount) {

                    //  nothing found before ||
                    //  found but has null columns ||
                    //  found but has more columns than this index
                    briCols      = cols;
                    briColsCount = colsCount;
                    nNullCount   = colsCount;
                    isStrict     = true;
                }

                continue;
            } else if (isStrict) {
                continue;
            } else if (briCols == null || colsCount < briColsCount
                       || nnullc > nNullCount) {

                //  nothing found before ||
                //  found but has more columns than this index||
                //  found but has fewer not null columns than this index
                briCols      = cols;
                briColsCount = colsCount;
                nNullCount   = nnullc;
            }
        }

        // remove rowID column from bestRowIdentiferCols
        bestRowIdentifierCols = briCols == null
                                || briColsCount == briCols.length ? briCols
                                                                  : ArrayUtil
                                                                  .arraySlice(briCols,
                                                                      0, briColsCount);
        bestRowIdentifierStrict = isStrict;

        if (hasPrimaryKey()) {
            bestIndex = getPrimaryIndex();
        }
    }

    /**
     * Sets the SQL default value for a columm.
     */
    void setDefaultExpression(int columnIndex, Expression def) {

        Column column = getColumn(columnIndex);

        column.setDefaultExpression(def);

        colDefaults[columnIndex] = column.getDefaultExpression();

        resetDefaultsFlag();
    }

    /**
     * sets the flag for the presence of any default expression
     */
    void resetDefaultsFlag() {

        hasDefaultValues = false;

        for (int i = 0; i < columnCount; i++) {
            hasDefaultValues = hasDefaultValues || colDefaults[i] != null;
        }
    }

    DataFileCache getCache() {
        return cache;
    }

    /**
     *  Used in TableFilter to get an index for the column.
     *  An index is created automatically for system tables or subqueries.
     */
    Index getIndexForColumn(Session session, int column) {

        int i = bestIndexForColumn[column];

        if (i == -1
                && (tableType == Table.SYSTEM_SUBQUERY
                    || tableType == Table.SYSTEM_TABLE)) {
            try {
                HsqlName indexName = database.nameManager.newAutoName("IDX");

                createIndex(session, new int[]{ column }, indexName, false,
                            false, false);

                i = bestIndexForColumn[column];
            } catch (Exception e) {}
        }

        return i == -1 ? null
                       : getIndex(i);
    }

    /**
     *  Used for TableFilter to get an index for the columns
     */
    Index getIndexForColumns(boolean[] columnCheck) {

        Index indexChoice = null;
        int   colCount    = 0;

        for (int i = 0; i < indexList.length; i++) {
            Index index = indexList[i];
            boolean result = ArrayUtil.containsAllTrueElements(columnCheck,
                index.colCheck);

            if (result && index.getVisibleColumns() > colCount) {
                colCount    = index.getVisibleColumns();
                indexChoice = index;
            }
        }

        return indexChoice;
    }

    /**
     *  Finds an existing index for a foreign key column group
     */
    Index getIndexForColumns(int[] col, boolean unique) throws HsqlException {

        for (int i = 0, count = getIndexCount(); i < count; i++) {
            Index currentindex = getIndex(i);
            int[] indexcol     = currentindex.getColumns();

            if (ArrayUtil.haveEqualArrays(indexcol, col, col.length)) {
                if (!unique || currentindex.isUnique()) {
                    return currentindex;
                }
            }
        }

        return null;
    }

    /**
     *  Return the list of file pointers to root nodes for this table's
     *  indexes.
     */
    public int[] getIndexRootsArray() {

        int[] roots = new int[getIndexCount()];

        for (int i = 0; i < getIndexCount(); i++) {
            roots[i] = indexList[i].getRoot();
        }

        return roots;
    }

    /**
     * Returns the string consisting of file pointers to roots of indexes
     * plus the next identity value (hidden or user defined). This is used
     * with CACHED tables.
     */
    String getIndexRoots() {

        String       roots = StringUtil.getList(getIndexRootsArray(), " ", "");
        StringBuffer s     = new StringBuffer(roots);

        s.append(' ');
        s.append(identitySequence.peek());

        return s.toString();
    }

    /**
     *  Sets the index roots of a cached/text table to specified file
     *  pointers. If a
     *  file pointer is -1 then the particular index root is null. A null index
     *  root signifies an empty table. Accordingly, all index roots should be
     *  null or all should be a valid file pointer/reference.
     */
    public void setIndexRoots(int[] roots) throws HsqlException {

        Trace.check(isCached, Trace.TABLE_NOT_FOUND);

        for (int i = 0; i < getIndexCount(); i++) {
            int p = roots[i];
            Row r = null;

            if (p != -1) {
                r = (CachedRow) rowStore.get(p);
            }

            Node f = null;

            if (r != null) {
                f = r.getNode(i);
            }

            indexList[i].setRoot(null, f);
        }
    }

    /**
     *  Sets the index roots and next identity.
     */
    void setIndexRoots(String s) throws HsqlException {

        // the user may try to set this; this is not only internal problem
        Trace.check(isCached, Trace.TABLE_NOT_FOUND);

        Tokenizer t     = new Tokenizer(s);
        int[]     roots = new int[getIndexCount()];

        for (int i = 0; i < getIndexCount(); i++) {
            int v = t.getInt();

            roots[i] = v;
        }

        setIndexRoots(roots);

        long v = t.getBigint();

        identitySequence.reset(v);
    }

    /**
     *  Shortcut for creating system table PK's.
     */
    void createPrimaryKey(int[] cols) throws HsqlException {
        createPrimaryKey(null, cols, false);
    }

    /**
     *  Shortcut for creating default PK's.
     */
    void createPrimaryKey() throws HsqlException {
        createPrimaryKey(null, null, false);
    }

    /**
     *  Creates a single or multi-column primary key and index. sets the
     *  colTypes array. Finalises the creation of the table. (fredt@users)
     */

// tony_lai@users 20020820 - patch 595099
    void createPrimaryKey(HsqlName indexName, int[] columns,
                          boolean columnsNotNull) throws HsqlException {

        if (primaryKeyCols != null) {
            Trace.doAssert(false, "Table.createPrimaryKey(column)");
        }

        if (columns == null) {
            columns = new int[0];
        } else {
            for (int i = 0; i < columns.length; i++) {
                if (columnsNotNull) {
                    getColumn(columns[i]).setNullable(false);
                }

                getColumn(columns[i]).setPrimaryKey(true);
            }
        }

        primaryKeyCols   = columns;
        colTypes         = new int[columnCount];
        colDefaults      = new Expression[columnCount];
        colSizes         = new int[columnCount];
        colScales        = new int[columnCount];
        colNullable      = new boolean[columnCount];
        defaultColumnMap = new int[columnCount];

        for (int i = 0; i < columnCount; i++) {
            setColumnTypeVars(i);
        }

        primaryKeyTypes = new int[primaryKeyCols.length];

        ArrayUtil.copyColumnValues(colTypes, primaryKeyCols, primaryKeyTypes);

        primaryKeyColsSequence = new int[primaryKeyCols.length];

        ArrayUtil.fillSequence(primaryKeyColsSequence);
        resetDefaultsFlag();

        // tony_lai@users 20020820 - patch 595099
        HsqlName name = indexName != null ? indexName
                                          : database.nameManager.newAutoName(
                                              "IDX");

        createPrimaryIndex(columns, name);
        setBestRowIdentifiers();
    }

    void setColumnTypeVars(int i) {

        Column column = getColumn(i);

        colTypes[i]         = column.getType();
        colSizes[i]         = column.getSize();
        colScales[i]        = column.getScale();
        colNullable[i]      = column.isNullable();
        defaultColumnMap[i] = i;

        if (column.isIdentity()) {
            identitySequence.reset(column.identityStart,
                                   column.identityIncrement);
        }

        colDefaults[i] = column.getDefaultExpression();
    }

    HsqlName makeSysPKName() throws HsqlException {
        return database.nameManager.newAutoName("PK");
    }

    void createPrimaryIndex(int[] pkcols, HsqlName name) throws HsqlException {

        int[] pkcoltypes = new int[pkcols.length];

        for (int j = 0; j < pkcols.length; j++) {
            pkcoltypes[j] = colTypes[pkcols[j]];
        }

        Index newindex = new Index(database, name, this, pkcols, pkcoltypes,
                                   true, true, true, false, pkcols,
                                   pkcoltypes, isTemp);

        addIndex(newindex);
    }

    /**
     *  Create new index taking into account removal or addition of a column
     *  to the table.
     */
    private Index createAdjustedIndex(Index index, int colindex,
                                      int adjust) throws HsqlException {

        int[] indexcolumns = (int[]) ArrayUtil.resizeArray(index.getColumns(),
            index.getVisibleColumns());
        int[] colarr = ArrayUtil.toAdjustedColumnArray(indexcolumns, colindex,
            adjust);

        // if a column to remove is one of the Index columns
        if (colarr.length != index.getVisibleColumns()) {
            return null;
        }

        return createIndexStructure(colarr, index.getName(), index.isUnique(),
                                    index.isConstraint, index.isForward);
    }

    /**
     *  Create new memory-resident index. For MEMORY and TEXT tables.
     */
    Index createIndex(Session session, int[] column, HsqlName name,
                      boolean unique, boolean constraint,
                      boolean forward) throws HsqlException {

        int newindexNo = createIndexStructureGetNo(column, name, unique,
            constraint, forward);
        Index         newindex     = indexList[newindexNo];
        Index         primaryindex = getPrimaryIndex();
        RowIterator   it           = primaryindex.firstRow(session);
        int           rowCount     = 0;
        HsqlException error        = null;

        try {
            while (it.hasNext()) {
                Row  row      = it.next();
                Node backnode = row.getNode(newindexNo - 1);
                Node newnode  = Node.newNode(row, newindexNo, this);

                newnode.nNext  = backnode.nNext;
                backnode.nNext = newnode;

                // count before inserting
                rowCount++;

                newindex.insert(session, row, newindexNo);
            }

            return newindex;
        } catch (java.lang.OutOfMemoryError e) {
            error = Trace.error(Trace.OUT_OF_MEMORY);
        } catch (HsqlException e) {
            error = e;
        }

        // backtrack on error
        // rowCount rows have been modified
        it = primaryindex.firstRow(session);

        for (int i = 0; i < rowCount; i++) {
            Row  row      = it.next();
            Node backnode = row.getNode(0);
            int  j        = newindexNo;

            while (--j > 0) {
                backnode = backnode.nNext;
            }

            backnode.nNext = backnode.nNext.nNext;
        }

        indexList = (Index[]) ArrayUtil.toAdjustedArray(indexList, null,
                newindexNo, -1);

        setBestRowIdentifiers();

        throw error;
    }

    /**
     * Creates the internal structures for an index.
     */
    Index createIndexStructure(int[] columns, HsqlName name, boolean unique,
                               boolean constraint,
                               boolean forward) throws HsqlException {

        int i = createIndexStructureGetNo(columns, name, unique, constraint,
                                          forward);

        return indexList[i];
    }

    int createIndexStructureGetNo(int[] column, HsqlName name, boolean unique,
                                  boolean constraint,
                                  boolean forward) throws HsqlException {

        if (primaryKeyCols == null) {
            Trace.doAssert(false, "createIndex");
        }

        int   s    = column.length;
        int[] col  = new int[s];
        int[] type = new int[s];

        for (int j = 0; j < s; j++) {
            col[j]  = column[j];
            type[j] = colTypes[col[j]];
        }

        int[] pkcols  = getPrimaryKey();
        int[] pktypes = getPrimaryKeyTypes();
        Index newindex = new Index(database, name, this, col, type, false,
                                   unique, constraint, forward, pkcols,
                                   pktypes, isTemp);
        int indexNo = addIndex(newindex);

        setBestRowIdentifiers();

        return indexNo;
    }

    private int addIndex(Index index) {

        int i = 0;

        for (; i < indexList.length; i++) {
            Index current = indexList[i];
            int order = index.getIndexOrderValue()
                        - current.getIndexOrderValue();

            if (order < 0) {
                break;
            }
        }

        indexList = (Index[]) ArrayUtil.toAdjustedArray(indexList, index, i,
                1);

        return i;
    }

    /**
     * returns false if the table has to be recreated in order to add / drop
     * indexes. Only CACHED tables return false.
     */
    boolean isIndexingMutable() {
        return !isIndexCached();
    }

    /**
     *  Checks for use of a named index in table constraints,
     *  while ignorring a given set of constraints.
     * @throws  HsqlException if index is used in a constraint
     */
    void checkDropIndex(String indexname, HashSet ignore,
                        boolean dropPK) throws HsqlException {

        Index index = this.getIndex(indexname);

        if (index == null) {
            throw Trace.error(Trace.INDEX_NOT_FOUND, indexname);
        }

        if (!dropPK && index.equals(getIndex(0))) {
            throw Trace.error(Trace.DROP_PRIMARY_KEY, indexname);
        }

        for (int i = 0, size = constraintList.length; i < size; i++) {
            Constraint c = constraintList[i];

            if (ignore != null && ignore.contains(c)) {
                continue;
            }

            if (c.isIndexFK(index)) {
                throw Trace.error(Trace.DROP_FK_INDEX, indexname);
            }

            if (c.isIndexUnique(index)) {
                throw Trace.error(Trace.SYSTEM_INDEX, indexname);
            }
        }

        return;
    }

    /**
     *  Returns true if the table has any rows at all.
     */
    public boolean isEmpty(Session session) {

        if (getIndexCount() == 0) {
            return true;
        }

        return getIndex(0).isEmpty(session);
    }

    /**
     * Returns direct mapping array.
     */
    int[] getColumnMap() {
        return defaultColumnMap;
    }

    /**
     * Returns empty mapping array.
     */
    int[] getNewColumnMap() {
        return new int[columnCount];
    }

    /**
     * Returns empty boolean array.
     */
    boolean[] getNewColumnCheckList() {
        return new boolean[columnCount];
    }

    /**
     * Returns empty Object array for a new row.
     */
    public Object[] getEmptyRowData() {
        return new Object[columnCount];
    }

    /**
     * Returns array for a new row with SQL DEFAULT value for each column n
     * where exists[n] is false. This provides default values only where
     * required and avoids evaluating these values where they will be
     * overwritten.
     */
    Object[] getNewRowData(Session session,
                           boolean[] exists) throws HsqlException {

        Object[] data = new Object[columnCount];
        int      i;

        if (exists != null && hasDefaultValues) {
            for (i = 0; i < columnCount; i++) {
                Expression def = colDefaults[i];

                if (exists[i] == false && def != null) {
                    data[i] = def.getValue(session, colTypes[i]);
                }
            }
        }

        return data;
    }

    /**
     *  Performs Table structure modification and changes to the index nodes
     *  to remove a given index from a MEMORY or TEXT table. Not for PK index.
     *
     */
    void dropIndex(Session session, String indexname) throws HsqlException {

        // find the array index for indexname and remove
        int todrop = getIndexIndex(indexname);

        indexList = (Index[]) ArrayUtil.toAdjustedArray(indexList, null,
                todrop, -1);

        setBestRowIdentifiers();
        dropIndexFromRows(session, todrop);
    }

    void dropIndexFromRows(Session session, int index) throws HsqlException {

        RowIterator it = getPrimaryIndex().firstRow(session);

        while (it.hasNext()) {
            Row  row      = it.next();
            int  i        = index - 1;
            Node backnode = row.getNode(0);

            while (i-- > 0) {
                backnode = backnode.nNext;
            }

            backnode.nNext = backnode.nNext.nNext;
        }
    }

    /**
     * Moves the data from table to table.
     * The colindex argument is the index of the column that was
     * added or removed. The adjust argument is {-1 | 0 | +1}
     */
    void moveData(Session session, Table from, int colindex,
                  int adjust) throws HsqlException {

        Object colvalue = null;
        Column column   = null;

        if (adjust >= 0 && colindex != -1) {
            column   = getColumn(colindex);
            colvalue = column.getDefaultValue(session);
        }

        RowIterator it = from.getPrimaryIndex().firstRow(session);

        while (it.hasNext()) {
            Row      row  = it.next();
            Object[] o    = row.getData();
            Object[] data = getEmptyRowData();

            if (adjust == 0 && colindex != -1) {
                colvalue = Column.convertObject(session, o[colindex],
                                                column.getType(),
                                                column.getSize(),
                                                column.getScale());
            }

            ArrayUtil.copyAdjustArray(o, data, colvalue, colindex, adjust);
            setIdentityColumn(session, data);
            enforceNullConstraints(data);

            Row newrow = newRow(data);

            indexRow(session, newrow);
        }

        from.drop();
    }

    /**
     *  Highest level multiple row insert method. Corresponds to an SQL
     *  INSERT INTO ... SELECT ... statement.
     */
    int insert(Session session, Result ins) throws HsqlException {

        Record ni    = ins.rRoot;
        int    count = 0;

        fireAll(session, Trigger.INSERT_BEFORE);

        while (ni != null) {
            insertRow(session, ni.data);

            ni = ni.next;

            count++;
        }

        fireAll(session, Trigger.INSERT_AFTER);

        return count;
    }

    /**
     *  Highest level method for inserting a single row. Corresponds to an
     *  SQL INSERT INTO .... VALUES(,,) statement.
     *  fires triggers.
     */
    void insert(Session session, Object[] data) throws HsqlException {

        fireAll(session, Trigger.INSERT_BEFORE);
        insertRow(session, data);
        fireAll(session, Trigger.INSERT_AFTER);
    }

    /**
     *  Mid level method for inserting rows. Performs constraint checks and
     *  fires row level triggers.
     */
    private void insertRow(Session session,
                           Object[] data) throws HsqlException {

        if (triggerLists[Trigger.INSERT_BEFORE_ROW] != null) {
            fireAll(session, Trigger.INSERT_BEFORE_ROW, null, data);
        }
        
        setIdentityColumn(session, data);
        checkRowDataInsert(session, data);
        Row row = insertNoCheck(session, data);

        if (triggerLists[Trigger.INSERT_AFTER_ROW] != null) {
            fireAll(session, Trigger.INSERT_AFTER_ROW, null, data);
            checkRowDataInsert(session, data);
        }
        session.onAfterInsert(this, row);
    }

    /**
     * Multi-row insert method. Used for SELECT ... INTO tablename queries.
     * These tables are new, empty tables, with no constraints, triggers
     * column default values, column size enforcement whatsoever.
     *
     * Not used for INSERT INTO .... SELECT ... FROM queries
     */
    void insertIntoTable(Session session, Result result) throws HsqlException {

        insertResult(session, result);

        if (!isLogged) {
            return;
        }

        Record r = result.rRoot;

        while (r != null) {
            database.logger.writeInsertStatement(session, this, r.data);

            r = r.next;
        }
    }

    /**
     *  Low level method for row insert.
     *  UNIQUE or PRIMARY constraints are enforced by attempting to
     *  add the row to the indexes.
     */
    private Row insertNoCheck(Session session,
                               Object[] data) throws HsqlException {

        Row row = newRow(data);

        // this handles the UNIQUE constraints
        indexRow(session, row);

        if (session != null) {
            session.addInsertAction(this, row);
        }

        if (isLogged) {
            database.logger.writeInsertStatement(session, this, data);
        }
        
        return row;
    }

    /**
     *
     */
    public void insertNoCheckFromLog(Session session,
                                     Object[] data) throws HsqlException {

        Row r = newRow(data);

        updateIdentityValue(data);
        indexRow(session, r);

        if (session != null) {
            session.addInsertAction(this, r);
        }
    }

    /**
     *  Low level method for restoring deleted rows
     */
    void insertNoCheckRollback(Session session, Row row,
                               boolean log) throws HsqlException {

        Row newrow = restoreRow(row);

        // instead of new row, use new routine so that the row does not use
        // rowstore.add(), which will allocate new space and different pos
        indexRow(session, newrow);

        if (log && isLogged) {
            database.logger.writeInsertStatement(session, this, row.getData());
        }
    }

    /**
     * Used for system table inserts. No checks. No identity
     * columns.
     */
    int insertSys(Result ins) throws HsqlException {

        Record ni    = ins.rRoot;
        int    count = 0;

        while (ni != null) {
            insertData(null, ni.data);

            ni = ni.next;

            count++;
        }

        return count;
    }

    /**
     * Used for subquery inserts. No checks. No identity
     * columns.
     */
    int insertResult(Session session, Result ins) throws HsqlException {

        Record ni    = ins.rRoot;
        int    count = 0;

        while (ni != null) {
            Object[] newData =
                (Object[]) ArrayUtil.resizeArrayIfDifferent(ni.data,
                    columnCount);

            insertData(session, newData);

            ni = ni.next;

            count++;
        }

        return count;
    }

    /**
     * Not for general use.
     * Used by ScriptReader to unconditionally insert a row into
     * the table when the .script file is read.
     */
    public void insertFromScript(Object[] data) throws HsqlException {
        updateIdentityValue(data);
        insertData(null, data);
    }

    /**
     * Used by the methods above.
     */
    public void insertData(Session session,
                           Object[] data) throws HsqlException {

        Row row = newRow(data);

        indexRow(session, row);
        commitRowToStore(row);
    }

    /**
     * Used by the system tables
     */
    public void insertSys(Object[] data) throws HsqlException {

        Row row = newRow(data);

        indexRow(null, row);
    }

    /**
     * Used by TextCache to insert a row into the indexes when the source
     * file is first read.
     */
    protected void insertFromTextSource(CachedRow row) throws HsqlException {

        Object[] data = row.getData();

        updateIdentityValue(data);
        enforceFieldValueLimits(data, defaultColumnMap);
        enforceNullConstraints(data);
        indexRow(null, row);
    }

    /**
     * Checks a row against NOT NULL constraints on columns.
     */
    protected void enforceNullConstraints(Object[] data) throws HsqlException {

        for (int i = 0; i < columnCount; i++) {
            if (data[i] == null && !colNullable[i]) {
                Trace.throwerror(Trace.TRY_TO_INSERT_NULL,
                                 "column: " + getColumn(i).columnName.name
                                 + " table: " + tableName.name);
            }
        }
    }

    /**
     * If there is an identity column (visible or hidden) on the table, sets
     * the value and/or adjusts the iIdentiy value for the table.
     */
    protected void setIdentityColumn(Session session,
                                     Object[] data) throws HsqlException {

        if (identityColumn != -1) {
            Object id = data[identityColumn];

            if (id == null) {
                if (colTypes[identityColumn] == Types.INTEGER) {
                    id = ValuePool.getInt((int) identitySequence.getValue());
                } else {
                    id = ValuePool.getLong(identitySequence.getValue());
                }

                data[identityColumn] = id;
            } else {
                identitySequence.getValue( Number.longValue(id));
            }

            if (session != null) {
                session.setLastIdentity( id );
            }
        }
    }

    /**
     * If there is an identity column (visible or hidden) on the table, sets
     * the max identity value.
     */
    protected void updateIdentityValue(Object[] data) throws HsqlException {

        if (identityColumn != -1) {
            Object id = data[identityColumn];

            if (id != null) {
                identitySequence.getValue(Number.longValue(id));
            }
        }
    }

    /**
     *  Enforce max field sizes according to SQL column definition.
     *  SQL92 13.8
     */
    void enforceFieldValueLimits(Object[] data,
                                 int[] cols) throws HsqlException {

        int i;
        int colindex;

        if (sqlEnforceSize) {
            if (cols == null) {
                cols = defaultColumnMap;
            }

            for (i = 0; i < cols.length; i++) {
                colindex = cols[i];

                if ((colTypes[colindex] == Types.TIMESTAMP || colSizes[colindex] != 0)
                        && data[colindex] != null) {
                    data[colindex] = Column.enforceSize(data[colindex],
                                                        colTypes[colindex],
           
