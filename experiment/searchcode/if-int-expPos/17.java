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

import org.hsqldb.HsqlNameManager.HsqlName;
import org.hsqldb.lib.ArrayUtil;
import org.hsqldb.lib.HashMap;
import org.hsqldb.lib.HashMappedList;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.IntKeyHashMap;
import org.hsqldb.lib.IntValueHashMap;
import org.hsqldb.lib.Iterator;
import org.hsqldb.store.ValuePool;

// fredt@users 20020130 - patch 497872 by Nitin Chauhan - reordering for speed
// fredt@users 20020215 - patch 1.7.0 by fredt - support GROUP BY with more than one column
// fredt@users 20020215 - patch 1.7.0 by fredt - SQL standard quoted identifiers
// fredt@users 20020218 - patch 1.7.0 by fredt - DEFAULT keyword
// fredt@users 20020221 - patch 513005 by sqlbob@users - SELECT INTO types
// fredt@users 20020425 - patch 548182 by skitt@users - DEFAULT enhancement
// thertz@users 20020320 - patch 473613 by thertz - outer join condition bug
// fredt@users 20021229 - patch 1.7.2 by fredt - new solution for above
// fredt@users 20020420 - patch 523880 by leptipre@users - VIEW support
// fredt@users 20020525 - patch 559914 by fredt@users - SELECT INTO logging
// tony_lai@users 20021020 - patch 1.7.2 - improved aggregates and HAVING
// aggregate functions can now be used in expressions - HAVING supported
// kloska@users 20021030 - patch 1.7.2 - ON UPDATE CASCADE
// fredt@users 20021112 - patch 1.7.2 by Nitin Chauhan - use of switch
// rewrite of the majority of multiple if(){}else{} chains with switch(){}
// boucherb@users 20030705 - patch 1.7.2 - prepared statement support
// fredt@users 20030819 - patch 1.7.2 - EXTRACT({YEAR | MONTH | DAY | HOUR | MINUTE | SECOND } FROM datetime)
// fredt@users 20030820 - patch 1.7.2 - CHAR_LENGTH | CHARACTER_LENGTH | OCTET_LENGTH(string)
// fredt@users 20030820 - patch 1.7.2 - POSITION(string IN string)
// fredt@users 20030820 - patch 1.7.2 - SUBSTRING(string FROM pos [FOR length])
// fredt@users 20030820 - patch 1.7.2 - TRIM({LEADING | TRAILING | BOTH} [<character>] FROM <string expression>)
// fredt@users 20030820 - patch 1.7.2 - CASE [expr] WHEN ... THEN ... [ELSE ...] END and its variants
// fredt@users 20030820 - patch 1.7.2 - NULLIF(expr,expr)
// fredt@users 20030820 - patch 1.7.2 - COALESCE(expr,expr,...)
// fredt@users 20031012 - patch 1.7.2 - improved scoping for column names in all areas
// boucherb@users 200403xx - patch 1.7.2 - added support for prepared SELECT INTO
// boucherb@users 200403xx - doc 1.7.2 - some
// thomasm@users 20041001 - patch 1.7.3 - BOOLEAN undefined handling
// fredt@users 20050220 - patch 1.8.0 - CAST with precision / scale
/* todo: fredt - implement remaining numeric value functions (SQL92 6.6)
 *
 * EXTRACT({TIMEZONE_HOUR | TIMEZONE_MINUTE} FROM {datetime | interval})
 */

/**
 * Responsible for parsing non-DDL statements.
 *
 * Extensively rewritten and extended in successive versions of HSQLDB.
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @version 1.8.0
 * @since Hypersonic SQL
 */
class Parser {

    private Database  database;
    private Tokenizer tokenizer;
    private Session   session;
    private String    sSchema;
    private String    sTable;
    private String    sToken;
    private boolean   wasQuoted;
    private Object    oData;
    private int       iType;
    private int       iToken;
    private boolean   compilingView;

    //
    private int           subQueryLevel;
    private HsqlArrayList subQueryList = new HsqlArrayList();

    /**
     *  Constructs a new Parser object with the given context.
     *
     * @param  db the Database instance against which to resolve named
     *      database object references
     * @param  t the token source from which to parse commands
     * @param  session the connected context
     */
    Parser(Session session, Database db, Tokenizer t) {

        database     = db;
        tokenizer    = t;
        this.session = session;
    }

    /**
     *  sets a flag indicating the parser is used for compiling a view
     */
    void setCompilingView() {
        compilingView = true;
    }

    /**
     *  determines whether the parser is used for compiling a view
     */
    boolean isCompilingView() {
        return compilingView;
    }

    /**
     *  Resets this parse context with the given SQL character sequence.
     *
     * Internal structures are reset as though a new parser were created
     * with the given sql and the originally specified database and session
     *
     * @param a new SQL character sequence to replace the current one
     */
    void reset(String sql) {

        sTable = null;
        sToken = null;
        oData  = null;

        tokenizer.reset(sql);
        subQueryList.clear();

        subQueryLevel = 0;

        parameters.clear();
    }

    /**
     * Tests whether the parsing session has the given write access on the
     * given Table object. <p>
     *
     * @param table the Table object to check
     * @param userRight the numeric code of the right to check
     * @throws HsqlException if the session user does not have the right
     *      or the given Table object is simply not writable (e.g. is a
     *      non-updateable View)
     */
    void checkTableWriteAccess(Table table,
                               int userRight) throws HsqlException {

        // session level user rights
        session.checkReadWrite();

        // object level user rights
        session.check(table.getName(), userRight);

        // object type
        if (table.isView()) {
            throw Trace.error(Trace.NOT_A_TABLE, table.getName().name);
        }

        // object readonly
        table.checkDataReadOnly();
    }

    /**
     * Parses a comma-separated, right-bracket terminated list of column
     * names. <p>
     *
     * @param db the Database instance whose name manager is to provide the
     *      resulting HsqlName objects, when the full argument is true
     * @param t the tokenizer representing the character sequence to be parsed
     * @param full if true, generate a list of HsqlNames, else a list of
     *  String objects
     */
    static HsqlArrayList getColumnNames(Database db, Table table, Tokenizer t,
                                        boolean full) throws HsqlException {

        HsqlArrayList columns = new HsqlArrayList();

        while (true) {
            if (full) {
                String   token  = t.getSimpleName();
                boolean  quoted = t.wasQuotedIdentifier();
                HsqlName name   = db.nameManager.newHsqlName(t.sTokenOrig/*token*/, quoted);

                columns.add(name);
            } else {
            	String   token  = t.getName();
                columns.add(t.sTokenOrig);

                if (t.wasLongName()
                        && !t.getLongNameFirst().equals(
                            table.getName().name)) {
                    throw (Trace.error(Trace.TABLE_NOT_FOUND,
                                       t.getLongNameFirst()));
                }
            }

            String token = t.getSimpleToken();

            if (token.equals(Token.T_COMMA)) {
                continue;
            }

            if (token.equals(Token.T_CLOSEBRACKET)) {
                break;
            }

            t.throwUnexpected();
        }

        return columns;
    }

    /**
     * The SubQuery objects are added to the end of subquery list.
     *
     * When parsing the SELECT for a view, optional HsqlName[] array is used
     * for view column aliases.
     *
     */
    SubQuery parseSubquery(int brackets, HsqlName[] colNames,
                           boolean resolveAll,
                           int predicateType) throws HsqlException {

        SubQuery sq;

        sq = new SubQuery();

        subQueryLevel++;

        boolean canHaveOrder = predicateType == Expression.VIEW
                               || predicateType == Expression.SELECT;
        boolean canHaveLimit = predicateType == Expression.SELECT
                               || predicateType == Expression.VIEW
                               || predicateType == Expression.QUERY;
        boolean limitWithOrder = predicateType == Expression.IN
                                 || predicateType == Expression.ALL
                                 || predicateType == Expression.ANY;
        Select s = parseSelect(brackets, canHaveOrder, canHaveLimit,
                               limitWithOrder, true);

        sq.level = subQueryLevel;

        subQueryLevel--;

        boolean isResolved = s.resolveAll(session, resolveAll);

        sq.select     = s;
        sq.isResolved = isResolved;

        // it's not a problem that this table has not a unique name
        HsqlName sqtablename =
            database.nameManager.newHsqlName("SYSTEM_SUBQUERY", false);

        sqtablename.schema = SchemaManager.SYSTEM_SCHEMA_HSQLNAME;

        Table table = new Table(database, sqtablename, Table.SYSTEM_SUBQUERY);

        if (colNames != null) {
            if (colNames.length != s.iResultLen) {
                throw Trace.error(Trace.COLUMN_COUNT_DOES_NOT_MATCH);
            }

            for (int i = 0; i < s.iResultLen; i++) {
                HsqlName name = colNames[i];

                s.exprColumns[i].setAlias(name.name, name.isNameQuoted);
            }
        } else {
            for (int i = 0; i < s.iResultLen; i++) {
                String colname = s.exprColumns[i].getAlias();

                if (colname == null || colname.length() == 0) {

                    // fredt - this does not guarantee the uniqueness of column
                    // names but addColumns() will throw if names are not unique.
                    colname = "COL_" + String.valueOf(i + 1);

                    s.exprColumns[i].setAlias(colname, false);
                }
            }
        }

        table.addColumns(s);

        boolean uniqueValues = predicateType == Expression.EXISTS
                               || predicateType == Expression.IN
                               || predicateType == Expression.ALL
                               || predicateType == Expression.ANY;
        int[] pcol = null;

        if (uniqueValues) {
            pcol = new int[s.iResultLen];

            ArrayUtil.fillSequence(pcol);
        }

        table.createPrimaryKey(pcol);

        sq.table      = table;
        sq.uniqueRows = uniqueValues;

        subQueryList.add(sq);

        return sq;
    }

    SubQuery getViewSubquery(View v) {

        SubQuery sq = v.viewSubQuery;

        for (int i = 0; i < v.viewSubqueries.length; i++) {
            subQueryList.add(v.viewSubqueries[i]);
        }

        return sq;
    }

    /**
     *  Constructs and returns a Select object.
     *
     * @param canHaveOrder whether the SELECT being parsed can have an ORDER BY
     * @param canHaveLimit whether LIMIT without ORDER BY is allowed
     * @param limitWithOrder whether LIMIT is allowed only with ORDER BY
     * @param isMain whether the SELECT being parsed is the first
     * select statement in the set
     * @return a new Select object
     * @throws  HsqlException if a parsing error occurs
     */
    Select parseSelect(int brackets, boolean canHaveOrder,
                       boolean canHaveLimit, boolean limitWithOrder,
                       boolean isMain) throws HsqlException {

        Select select = new Select();
        String token  = tokenizer.getString();

        if (canHaveLimit || limitWithOrder) {
            if (tokenizer.wasThis(Token.T_LIMIT)
                    || tokenizer.wasThis(Token.T_TOP)) {
                parseLimit(token, select, false);

                token = tokenizer.getString();
            }
        }

        if (tokenizer.wasThis(Token.T_DISTINCT)) {
            select.isDistinctSelect = true;
        } else if (tokenizer.wasThis(Token.T_ALL)) {}
        else {
            tokenizer.back();
        }

        // parse column list
        HsqlArrayList vcolumn = new HsqlArrayList();

        do {
            int        expPos = tokenizer.getPosition();
            Expression e      = parseExpression();

            if (isCompilingView()) {
                if (e.getType() == Expression.ASTERISK) {
                    if (select.asteriskPositions == null) {
                        select.asteriskPositions = new IntKeyHashMap();
                    }

                    // remember the position of the asterisk. For the moment, just
                    // remember the expression, so it can later be found and replaced
                    // with the concrete column list
                    select.asteriskPositions.put(expPos, e);
                }
            }

            token = tokenizer.getString();

            if (tokenizer.wasThis(Token.T_AS)) {
                e.setAlias(tokenizer.getString(),//SimpleName(),
                           tokenizer.wasQuotedIdentifier());

                token = tokenizer.getString();
            } else if (tokenizer.wasSimpleName()) {
                e.setAlias(token, tokenizer.wasQuotedIdentifier());

                token = tokenizer.getString();
            }

            vcolumn.add(e);
        } while (tokenizer.wasThis(Token.T_COMMA));

        if (token.equals(Token.T_INTO)) {
            boolean getname = true;

            token           = tokenizer.getString();
            select.intoType = database.getDefaultTableType();

            if (tokenizer.wasSimpleToken()) {
                switch (Token.get(token)) {

                    case Token.CACHED :
                        select.intoType = Table.CACHED_TABLE;
                        break;

                    case Token.TEMP :
                        select.intoType = Table.TEMP_TABLE;
                        break;

                    case Token.TEXT :
                        select.intoType = Table.TEXT_TABLE;
                        break;

                    case Token.MEMORY :
                        select.intoType = Table.MEMORY_TABLE;
                        break;

                    default :
                        getname = false;
                        break;
                }

                if (getname) {
                    token = tokenizer.getName();
                }
            }

            if (!tokenizer.wasName()) {
                tokenizer.throwUnexpected();
            }

            select.sIntoTable = database.nameManager.newHsqlName(token,
                    tokenizer.wasQuotedIdentifier());
            select.sIntoTable.schema =
                session.getSchemaHsqlName(tokenizer.getLongNameFirst());
            token = tokenizer.getString();
        }

        tokenizer.matchThis(Token.T_FROM);

        Expression condition = null;

        // parse table list
        HsqlArrayList vfilter = new HsqlArrayList();

        vfilter.add(parseTableFilter(false));

        while (true) {
            token = tokenizer.getString();

            boolean cross = false;

            if (tokenizer.wasThis(Token.T_INNER)) {
                tokenizer.getThis(Token.T_JOIN);

                token = Token.T_JOIN;
            } else if (tokenizer.wasThis(Token.T_CROSS)) {
                tokenizer.getThis(Token.T_JOIN);

                token = Token.T_JOIN;
                cross = true;
            }

            if (token.equals(Token.T_LEFT)
                    && !tokenizer.wasQuotedIdentifier()) {
                tokenizer.isGetThis(Token.T_OUTER);
                tokenizer.getThis(Token.T_JOIN);

                TableFilter tf = parseTableFilter(true);

                vfilter.add(tf);
                tokenizer.getThis(Token.T_ON);

                Expression newcondition = parseExpression();

                newcondition.checkTables(vfilter);

                condition = addJoinCondition(condition, newcondition, tf,
                                             true);

                // MarcH HuugO RIGHT JOIN SUPPORT
            } else if (token.equals(Token.T_RIGHT)
                       && !tokenizer.wasQuotedIdentifier()) {
                tokenizer.isGetThis(Token.T_OUTER);
                tokenizer.getThis(Token.T_JOIN);

                // this object is not an outerjoin, the next object is an outerjoin
                TableFilter tf = parseTableFilter(false);

                // insert new condition as first element in a new vfilter (nvfilter), copy the content of vfilter and rename nvfilter back to vfilter.
                HsqlArrayList nvfilter = new HsqlArrayList();

                nvfilter.add(tf);
                nvfilter.addAll(vfilter);

                vfilter = nvfilter;

                // set isOuterJoin correct
                ((TableFilter) vfilter.get(1)).isOuterJoin = true;

                tokenizer.getThis(Token.T_ON);

                Expression newcondition = parseExpression();

                newcondition.checkTables(vfilter);

                condition = addJoinCondition(condition, newcondition,
                                             ((TableFilter) vfilter.get(1)),
                                             true);
            } else if (tokenizer.wasThis(Token.T_JOIN)) {
                vfilter.add(parseTableFilter(false));

                if (!cross) {
                    tokenizer.getThis(Token.T_ON);

                    Expression newcondition = parseExpression();

                    newcondition.checkTables(vfilter);

                    condition = addJoinCondition(condition, newcondition,
                                                 null, false);
                }
            } else if (tokenizer.wasThis(Token.T_COMMA)) {
                vfilter.add(parseTableFilter(false));
            } else {
                tokenizer.back();

                break;
            }
        }

        resolveSelectTableFilter(select, vcolumn, vfilter);

        // where
        token = tokenizer.getString();

        if (tokenizer.wasThis(Token.T_WHERE)) {
            Expression newcondition = parseExpression();

            condition = addCondition(condition, newcondition);
            token     = tokenizer.getString();
        }

        select.queryCondition = condition;

        // group by
        if (tokenizer.wasThis(Token.T_GROUP)) {
            tokenizer.getThis(Token.T_BY);

            int len = 0;

            do {
                Expression e = parseExpression();

                vcolumn.add(e);

                token = tokenizer.getString();

                len++;
            } while (tokenizer.wasThis(Token.T_COMMA));

            select.iGroupLen = len;
        }

        // having
        if (tokenizer.wasThis(Token.T_HAVING)) {
            select.iHavingLen      = 1;
            select.havingCondition = parseExpression();
            token                  = tokenizer.getString();

            vcolumn.add(select.havingCondition);
        }

        if (isMain || limitWithOrder) {
            if (tokenizer.wasThis(Token.T_ORDER)) {
                tokenizer.getThis(Token.T_BY);
                parseOrderBy(select, vcolumn);

                token = tokenizer.getString();
            }

            if (tokenizer.wasThis(Token.T_LIMIT)) {
                parseLimit(token, select, true);

                token = tokenizer.getString();
            }
        }

        boolean closebrackets = false;

        if (brackets > 0 && token.equals(Token.T_CLOSEBRACKET)) {
            closebrackets = true;
            brackets      -= parseCloseBrackets(brackets - 1) + 1;
            token         = tokenizer.getString();
        }

        select.unionDepth = brackets;

        // checks for ORDER and LIMIT
        if (!(isMain || closebrackets)) {
            limitWithOrder = false;
        }

        boolean hasOrder = select.iOrderLen != 0;
        boolean hasLimit = select.limitCondition != null;

        if (limitWithOrder) {
            if (hasLimit && !hasOrder) {
                throw Trace.error(Trace.ORDER_LIMIT_REQUIRED);
            }
        } else {
            if (hasOrder && !canHaveOrder) {
                throw Trace.error(Trace.INVALID_ORDER_BY);
            }

            if (hasLimit && !canHaveLimit) {
                throw Trace.error(Trace.INVALID_LIMIT);
            }
        }

        int unionType = parseUnion(token);

        if (unionType != Select.NOUNION) {
            boolean openbracket = false;

            select.unionType = unionType;

            if (tokenizer.isGetThis(Token.T_OPENBRACKET)) {
                openbracket = true;
                brackets    += parseOpenBrackets() + 1;
            }

            tokenizer.getThis(Token.T_SELECT);

            // accept ORDRY BY with LIMIT when in brackets
            select.unionSelect = parseSelect(brackets, false, false,
                                             openbracket, false);
            token = tokenizer.getString();
        }

        if (isMain && (canHaveOrder || limitWithOrder)
                && select.iOrderLen == 0) {
            if (tokenizer.wasThis(Token.T_ORDER)) {
                tokenizer.getThis(Token.T_BY);
                parseOrderBy(select, vcolumn);

                token            = tokenizer.getString();
                select.sortUnion = true;
            }

            if (tokenizer.wasThis(Token.T_LIMIT)) {
                parseLimit(token, select, true);

                token = tokenizer.getString();
            }
        }

        tokenizer.back();

        if (isMain) {
            select.prepareUnions();
        }

        int len = vcolumn.size();

        select.exprColumns = new Expression[len];

        vcolumn.toArray(select.exprColumns);

        return select;
    }

    /**
     * Parses the given token and any further tokens in tokenizer to return
     * any UNION or other set operation ID.
     */
    int parseUnion(String token) throws HsqlException {

        int unionType = Select.NOUNION;

        if (tokenizer.wasSimpleToken()) {
            switch (Token.get(token)) {

                case Token.UNION :
                    token = tokenizer.getSimpleToken();

                    if (token.equals(Token.T_ALL)) {
                        unionType = Select.UNIONALL;
                    } else if (token.equals(Token.T_DISTINCT)) {
                        unionType = Select.UNION;
                    } else {
                        unionType = Select.UNION;

                        tokenizer.back();
                    }
                    break;

                case Token.INTERSECT :
                    tokenizer.isGetThis(Token.T_DISTINCT);

                    unionType = Select.INTERSECT;
                    break;

                case Token.EXCEPT :
                case Token.MINUS :
                    tokenizer.isGetThis(Token.T_DISTINCT);

                    unionType = Select.EXCEPT;
                    break;

                default :
                    break;
            }
        }

        return unionType;
    }

// fredt@users 20011010 - patch 471710 by fredt - LIMIT rewritten
// SELECT LIMIT n m DISTINCT ... queries and error message
// "SELECT LIMIT n m ..." creates the result set for the SELECT statement then
// discards the first n rows and returns m rows of the remaining result set
// "SELECT LIMIT 0 m" is equivalent to "SELECT TOP m" or "SELECT FIRST m"
// in other RDBMS's
// "SELECT LIMIT n 0" discards the first n rows and returns the remaining rows
// fredt@users 20020225 - patch 456679 by hiep256 - TOP keyword
    private void parseLimit(String token, Select select,
                            boolean isEnd) throws HsqlException {

        if (select.limitCondition != null) {
            return;
        }

        Expression e1 = null;
        Expression e2;
        boolean    islimit = false;

        if (isEnd) {
            if (token.equals(Token.T_LIMIT)) {
                islimit = true;

                read();

                e2 = readTerm();

                if (sToken.equals(Token.T_OFFSET)) {
                    read();

                    e1 = readTerm();
                }

                tokenizer.back();
            } else {
                return;
            }
        } else if (token.equals(Token.T_LIMIT)) {
            read();

            e1      = readTerm();
            e2      = readTerm();
            islimit = true;

            tokenizer.back();
        } else if (token.equals(Token.T_TOP)) {
            read();

            e2 = readTerm();

            tokenizer.back();
        } else {
            return;
        }

        if (e1 == null) {
            e1 = new Expression(Types.INTEGER, ValuePool.getInt(0));
        }

        if (e1.isParam()
                || (e1.getType() == Expression.VALUE
                    && e1.getDataType() == Types.INTEGER
                    && e1.getValue(null) != null
                    && ((Integer) e1.getValue(null)).intValue() >= 0)) {
            if (e2.isParam()
                    || (e2.getType() == Expression.VALUE
                        && e2.getDataType() == Types.INTEGER
                        && e2.getValue(null) != null
                        && ((Integer) e2.getValue(null)).intValue() >= 0)) {

                // necessary for params
                e1.setDataType(Types.INTEGER);
                e2.setDataType(Types.INTEGER);

                select.limitCondition = new Expression(Expression.LIMIT, e1,
                                                       e2);

                return;
            }
        }

        int messageid = islimit ? Trace.INVALID_LIMIT_EXPRESSION
                                : Trace.INVALID_TOP_EXPRESSION;

        throw Trace.error(Trace.WRONG_DATA_TYPE, messageid);
    }

    private void parseOrderBy(Select select,
                              HsqlArrayList vcolumn) throws HsqlException {

        String token;
        int    len = 0;

        do {
            Expression e = parseExpression();

            e     = resolveOrderByExpression(e, select, vcolumn);
            token = tokenizer.getString();

            if (token.equals(Token.T_DESC)) {
                e.setDescending();

                token = tokenizer.getString();
            } else if (token.equals(Token.T_ASC)) {
                token = tokenizer.getString();
            }

            vcolumn.add(e);

            len++;
        } while (token.equals(Token.T_COMMA));

        tokenizer.back();

        select.iOrderLen = len;
    }

    private void resolveSelectTableFilter(Select select,
                                          HsqlArrayList vcolumn,
                                          HsqlArrayList vfilter)
                                          throws HsqlException {

        int           colcount;
        TableFilter[] filters = new TableFilter[vfilter.size()];

        vfilter.toArray(filters);

        select.tFilter = filters;

        // expand [table.]* columns
        colcount = vcolumn.size();

        for (int pos = 0; pos < colcount; ) {
            Expression e = (Expression) (vcolumn.get(pos));

            if (e.getType() == Expression.ASTERISK) {
                vcolumn.remove(pos);

                colcount = vcolumn.size();

                String tablename = e.getTableName();
                int    oldPos    = pos;

                if (tablename == null) {
                    for (int i = 0; i < filters.length; i++) {
                        pos      = addFilterColumns(filters[i], vcolumn, pos);
                        colcount = vcolumn.size();
                    }
                } else {
                    TableFilter f = e.findTableFilter(filters);

                    if (f == null) {
                        throw Trace.error(Trace.TABLE_NOT_FOUND, tablename);
                    }

                    pos      = addFilterColumns(f, vcolumn, pos);
                    colcount = vcolumn.size();
                }

                if (isCompilingView()) {

                    // find this expression's position in the Select's asterisk list
                    boolean foundAsteriskPos = false;
                    Iterator expSearch =
                        select.asteriskPositions.keySet().iterator();

                    while (expSearch.hasNext()) {
                        int expPos = expSearch.nextInt();

                        if (e == select.asteriskPositions.get(expPos)) {

                            // compile the complete column list which later is to replace the asterisk
                            StringBuffer completeColList = new StringBuffer();

                            for (int col = oldPos; col < pos; ++col) {
                                Expression resolvedColExpr =
                                    (Expression) (vcolumn.get(col));

                                completeColList.append(
                                    resolvedColExpr.getColumnDDL());

                                if (col < pos - 1) {
                                    completeColList.append(",");
                                }
                            }

                            select.asteriskPositions.put(
                                expPos, completeColList.toString());

                            foundAsteriskPos = true;

                            break;
                        }
                    }

                    Trace.doAssert(foundAsteriskPos);
                }
            } else {
                if (e.getFilter() == null) {
                    for (int i = 0; i < filters.length; i++) {
                        e.resolveTables(filters[i]);
                    }
                }

                pos++;
            }
        }

        for (int i = 0; i < colcount; i++) {
            Expression e = (Expression) (vcolumn.get(i));

            e.resolveTypes(session);
        }

        select.iResultLen = colcount;
    }

    /**
     * Add all columns of a table filter to list of columns
     */
    int addFilterColumns(TableFilter filter, HsqlArrayList columnList,
                         int position) throws HsqlException {

        Table table = filter.getTable();
        int   count = table.getColumnCount();

        for (int i = 0; i < count; i++) {
            Expression e = new Expression(filter, table.getColumn(i));

            if (isCompilingView()) {
                e.resolveTables(filter);
            }

            columnList.add(position++, e);
        }

        return position;
    }

    /**
     * Resolves an ORDER BY Expression, returning the column Expression object
     * to which it refers if it is an alias or column index. <p>
     *
     * If select is a SET QUERY, then only column indexes or names in the first
     * query are allowed.
     *
     * @param  e                          search column expression
     * @param  vcolumn                    list of columns
     * @param  union                      is select a union
     * @return                            new or the same expression
     * @throws HsqlException if an ambiguous reference to an alias or
     *      non-integer column index is encountered
     */
    private static Expression resolveOrderByExpression(Expression e,
            Select select, HsqlArrayList vcolumn) throws HsqlException {

        int     visiblecols = select.iResultLen;
        boolean union       = select.unionSelect != null;

        if (e.getType() == Expression.VALUE) {
            return resolveOrderByColumnIndex(e, vcolumn, visiblecols);
        }

        if (e.getType() != Expression.COLUMN) {
            if (union) {
                throw Trace.error(Trace.INVALID_ORDER_BY);
            }

            return e;
        }

        String ecolname   = e.getColumnName();
        String etablename = e.getTableName();

        for (int i = 0, size = visiblecols; i < size; i++) {
            Expression colexpr    = (Expression) vcolumn.get(i);
            String     colalias   = colexpr.getDefinedAlias();
            String     colname    = colexpr.getColumnName();
            String     tablename  = colexpr.getTableName();
            String     filtername = colexpr.getFilterTableName();

            if ((ecolname.equalsIgnoreCase(colalias) || ecolname.equalsIgnoreCase(colname))
                    && (etablename == null || etablename.equals(tablename)
                        || etablename.equals(filtername))) {
                colexpr.joinedTableColumnIndex = i;

                return colexpr;
            }
        }

        if (union) {
            throw Trace.error(Trace.INVALID_ORDER_BY, ecolname);
        }

        return e;
    }

    private static Expression resolveOrderByColumnIndex(Expression e,
            HsqlArrayList vcolumn, int visiblecols) throws HsqlException {

        // order by 1,2,3
        if (e.getDataType() == Types.INTEGER) {
            int i = ((Integer) e.getValue(null)).intValue();

            if (0 < i && i <= visiblecols) {
                Expression colexpr = (Expression) vcolumn.get(i - 1);

                colexpr.joinedTableColumnIndex = i - 1;

                return colexpr;
            }
        }

        throw Trace.error(Trace.INVALID_ORDER_BY);
    }

    private TableFilter parseSimpleTableFilter(int type) throws HsqlException {

        String alias  = null;
        String token  = tokenizer.getName();
        String schema = session.getSchemaName(tokenizer.getLongNameFirst());
        Table  table = database.schemaManager.getTable(session, token, schema);

        checkTableWriteAccess(table, type);

//
        token = tokenizer.getString();

        if (token.equals(Token.T_AS)) {
            alias = tokenizer.getSimpleName();
        } else if (tokenizer.wasSimpleName()) {
            alias = token;
        } else {
            tokenizer.back();
        }

        return new TableFilter(table, alias, null, false);
    }

    /**
     * Retrieves a TableFilter object newly constructed from the current
     * parse context. <p>
     *
     * @param  outerjoin if the filter is to back an outer join
     * @return a newly constructed TableFilter object
     * @throws  HsqlException if a parsing error occurs
     */
    private TableFilter parseTableFilter(boolean outerjoin)
    throws HsqlException {

        Table          t          = null;
        SubQuery       sq         = null;
        String         sAlias     = null;
        HashMappedList columnList = null;

        if (tokenizer.isGetThis(Token.T_OPENBRACKET)) {
            int brackets = parseOpenBrackets();

            tokenizer.getThis(Token.T_SELECT);

            // fredt - not correlated - a joined subquery table must resolve fully
            sq = parseSubquery(brackets, null, true, Expression.QUERY);

            tokenizer.getThis(Token.T_CLOSEBRACKET);

            t = sq.table;
        } else {
            String token = tokenizer.getName();
            String schema =
                session.getSchemaName(tokenizer.getLongNameFirst());

            t = database.schemaManager.getTable(session, token, schema);

            session.check(t.getName(), UserManager.SELECT);

            if (t.isView()) {
                sq        = getViewSubquery((View) t);
                sq.select = ((View) t).viewSelect;
                t         = sq.table;
                sAlias    = token;
            }
        }

        // fredt - we removed LEFT from the list of reserved words in Tokenizer
        // to allow LEFT() to work. Thus wasName() will return true for LEFT
        // and we check separately for this token
        String token = tokenizer.getString();

        if (tokenizer.wasLongName()) {
            tokenizer.throwUnexpected();
        }

        if ((token.equals(Token.T_LEFT) || token.equals(Token.T_RIGHT))
                && !tokenizer.wasQuotedIdentifier()) {
            tokenizer.back();
        } else if (token.equals(Token.T_AS)
                   && !tokenizer.wasQuotedIdentifier()) {
            sAlias = tokenizer.getSimpleName();

            if (tokenizer.isGetThis(Token.T_OPENBRACKET)) {
                tokenizer.back();

                columnList = parseColumnList();
            }
        } else if (tokenizer.wasSimpleName()) {
            sAlias = token;

            if (tokenizer.isGetThis(Token.T_OPENBRACKET)) {
                tokenizer.back();

                columnList = parseColumnList();
            }
        } else {
            tokenizer.back();
        }

        if (columnList != null && t.getColumnCount() != columnList.size()) {
            throw Trace.error(Trace.COLUMN_COUNT_DOES_NOT_MATCH);
        }

        return new TableFilter(t, sAlias, columnList, outerjoin);
    }

    /**
     *  Add a condition from the WHERE clause.
     *
     * @param  e1
     * @param  e2
     * @return
     */
    private static Expression addCondition(Expression e1, Expression e2) {

        if (e1 == null) {
            return e2;
        } else if (e2 == null) {
            return e1;
        } else {
            return new Expression(Expression.AND, e1, e2);
        }
    }

    /**
     *  Conjuntively adds a condition from the JOIN table ON clause.
     *
     * @param  e1 an existing condition with which e2 is to be combined
     *      in order to form a new conjunction
     * @param  e2 the new condition
     * @param tf the table filter that should become e2's join
     *      table filter
     * @param outer true if join is outer
     * @throws HsqlException if e2 responds that it cannot participate
     *      in the join
     * @return a new Expression object; the conjunction of e1 and e2
     */
    private static Expression addJoinCondition(Expression e1, Expression e2,
            TableFilter tf, boolean outer) throws HsqlException {

        if (!e2.setForJoin(tf, outer)) {
            throw Trace.error(Trace.OUTER_JOIN_CONDITION);
        }

        return addCondition(e1, e2);
    }

    /**
     *  Method declaration
     *
     * @return the Expression resulting from the parse
     * @throws  HsqlException
     */
    Expression parseExpression() throws HsqlException {

        read();

        Expression r = readOr();

        tokenizer.back();

        return r;
    }

    private Expression readAggregate() throws HsqlException {

        boolean distinct = false;
        boolean all      = false;
        int     type     = iToken;

        read();

        String token = tokenizer.getString();

        if (token.equals(Token.T_DISTINCT)) {
            distinct = true;
        } else if (token.equals(Token.T_ALL)) {
            all = true;
        } else {
            tokenizer.back();
        }

        readThis(Expression.OPEN);

        Expression s = readOr();

        readThis(Expression.CLOSE);

        if ((all || distinct)
                && (type == Expression.STDDEV_POP
                    || type == Expression.STDDEV_SAMP
                    || type == Expression.VAR_POP
                    || type == Expression.VAR_SAMP)) {
            throw Trace.error(Trace.INVALID_FUNCTION_ARGUMENT);
        }

        Expression aggregateExp = new Expression(type, s, null);

        aggregateExp.setDistinctAggregate(distinct);

        return aggregateExp;
    }

    /**
     *  Method declaration
     *
     * @return a disjuntion, possibly degenerate
     * @throws  HsqlException
     */
    private Expression readOr() throws HsqlException {

        Expression r = readAnd();

        while (iToken == Expression.OR) {
            int        type = iToken;
            Expression a    = r;

            read();

            r = new Expression(type, a, readAnd());
        }

        return r;
    }

    /**
     *  Method declaration
     *
     * @return a conjunction, possibly degenerate
     * @throws  HsqlException
     */
    private Expression readAnd() throws HsqlException {

        Expression r = readCondition();

        while (iToken == Expression.AND) {
            int        type = iToken;
            Expression a    = r;

            read();

            r = new Expression(type, a, readCondition());
        }

        return r;
    }

    /**
     *  Method declaration
     *
     * @return a predicate, possibly composite
     * @throws  HsqlException
     */
    private Expression readCondition() throws HsqlException {

        switch (iToken) {

            case Expression.NOT : {
                int type = iToken;

                read();

                return new Expression(type, readCondition(), null);
            }
            case Expression.EXISTS : {
                int type = iToken;

                read();
                readThis(Expression.OPEN);

                int brackets = 0;

                if (iToken == Expression.OPEN) {
                    brackets += parseOpenBrackets() + 1;

                    read();
                }

                Trace.check(iToken == Expression.SELECT,
                            Trace.UNEXPECTED_TOKEN);

                SubQuery sq = parseSubquery(brackets, null, false,
                                            Expression.EXISTS);
                Expression s = new Expression(sq);

                read();
                readThis(Expression.CLOSE);

                return new Expression(type, s, null);
            }
            default : {
                Expression a = readConcat();

                if (iToken == Expression.IS) {
                    read();

                    boolean not;

                    if (iToken == Expression.NOT) {
                        not = true;

                        read();
                    } else {
                        not = false;
                    }

                    Trace.check(iToken == Expression.VALUE && oData == null,
                                Trace.UNEXPECTED_TOKEN);
                    read();

                    // TODO: the TableFilter needs a right hand side to avoid null pointer exceptions...
                    a = new Expression(Expression.IS_NULL, a,
                                       new Expression(Types.NULL, null));

                    if (not) {
                        a = new Expression(Expression.NOT, a, null);
                    }

                    return a;
                }

                boolean not = false;

                if (iToken == Expression.NOT) {
                    not = true;

                    read();
                }

                switch (iToken) {

                    case Expression.LIKE : {
                        a = parseLikePredicate(a);

                        break;
                    }
                    case Expression.BETWEEN : {
                        a = parseBetweenPredicate(a);

                        break;
                    }
                    case Expression.IN : {
                        a = this.parseInPredicate(a);

                        break;
                    }
                    default : {
                        Trace.check(!not, Trace.UNEXPECTED_TOKEN);

                        if (Expression.isCompare(iToken)) {
                            int type = iToken;

                            read();

                            return new Expression(type, a, readConcat());
                        }

                        return a;
                    }
                }

                if (not) {
                    a = new Expression(Expression.NOT, a, null);
                }

                return a;
            }
        }
    }

    private Expression parseLikePredicate(Expression a) throws HsqlException {

        read();

        Expression b      = readConcat();
        Character  escape = null;

        if (sToken.equals(Token.T_ESCAPE)) {
            read();

            Expression c = readTerm();

            Trace.check(c.getType() == Expression.VALUE, Trace.INVALID_ESCAPE);

            String s = (String) c.getValue(session, Types.VARCHAR);

            // boucherb@users 2003-09-25
            // CHECKME:
            // Assert s.length() == 1 for xxxchar comparisons?
            // TODO:
            // SQL200n says binary escape can be 1 or more octets.
            // Maybe we need to retain s and check this in
            // Expression.resolve()?
            if (s == null || s.length() < 1) {
                throw Trace.error(Trace.INVALID_ESCAPE, s);
            }

            escape = new Character(s.charAt(0));
        }

        boolean hasCollation = database.collation.name != null;

        a = new Expression(a, b, escape, hasCollation);

        return a;
    }

    private Expression parseBetweenPredicate(Expression a)
    throws HsqlException {

        read();

        Expression l = new Expression(Expression.BIGGER_EQUAL, a,
                                      readConcat());

        readThis(Expression.AND);

        Expression h = new Expression(Expression.SMALLER_EQUAL, a,
                                      readConcat());

        if (l.getArg().isParam() && l.getArg2().isParam()) {
            throw Trace.error(Trace.UNRESOLVED_PARAMETER_TYPE,
                              Trace.Parser_ambiguous_between1);
        }

        if (h.getArg().isParam() && h.getArg2().isParam()) {
            throw Trace.error(Trace.UNRESOLVED_PARAMETER_TYPE,
                              Trace.Parser_ambiguous_between1);
        }

        return new Expression(Expression.AND, l, h);
    }

    private Expression parseInPredicate(Expression a) throws HsqlException {

        int type = iToken;

        read();
        readThis(Expression.OPEN);

        Expression b        = null;
        int        brackets = 0;

        if (iToken == Expression.OPEN) {
            brackets += parseOpenBrackets() + 1;

            read();
        }

        if (iToken == Expression.SELECT) {
            SubQuery sq = parseSubquery(brackets, null, false, Expression.IN);

            // until we support rows in IN predicates
            Trace.check(sq.select.iResultLen == 1,
                        Trace.SINGLE_COLUMN_EXPECTED);

            b = new Expression(sq);

            read();
        } else {
            tokenizer.back();

            HsqlArrayList v = new HsqlArrayList();

            while (true) {
                Expression value = parseExpression();

                if (value.exprType == Expression.VALUE
                        && value.valueData == null && !value.isParam()) {
                    throw Trace.error(Trace.NULL_IN_VALUE_LIST);
                }

                v.add(value);
                read();

                if (iToken != Expression.COMMA) {
                    break;
                }
            }

            Expression[] valueList;

            valueList = (Expression[]) v.toArray(new Expression[v.size()]);
            b         = new Expression(valueList);
        }

        readThis(Expression.CLOSE);

        return new Expression(type, a, b);
    }

    private Expression parseAllAnyPredicate() throws HsqlException {

        int type = iToken;

        read();
        readThis(Expression.OPEN);

        Expression b        = null;
        int        brackets = 0;

        if (iToken == Expression.OPEN) {
            brackets += parseOpenBrackets() + 1;

            read();
        }

        if (iToken != Expression.SELECT) {
            throw Trace.error(Trace.INVALID_IDENTIFIER);
        }

        SubQuery sq     = parseSubquery(brackets, null, false, type);
        Select   select = sq.select;

        // until we support rows
        Trace.check(sq.select.iResultLen == 1, Trace.SINGLE_COLUMN_EXPECTED);

        b = new Expression(sq);

        read();
        readThis(Expression.CLOSE);

        return new Expression(type, b, null);
    }

    /**
     *  Method declaration
     *
     * @param  type
     * @throws  HsqlException
     */
    private void readThis(int type) throws HsqlException {
        Trace.check(iToken == type, Trace.UNEXPECTED_TOKEN);
        read();
    }

    /**
     *  Method declaration
     *
     * @return a concatenation, possibly degenerate
     * @throws  HsqlException
     */
    private Expression readConcat() throws HsqlException {

        Expression r = readSum();

        while (iToken == Expression.CONCAT) {
            int        type = Expression.CONCAT;
            Expression a    = r;

            read();

            r = new Expression(type, a, readSum());
        }

        return r;
    }

    static HashMap simpleFunctions = new HashMap();

    static {
        simpleFunctions.put(Token.T_CURRENT_DATE,
                            "org.hsqldb.Library.curdate");
        simpleFunctions.put(Token.T_CURRENT_TIME,
                            "org.hsqldb.Library.curtime");
        simpleFunctions.put(Token.T_CURRENT_TIMESTAMP,
                            "org.hsqldb.Library.now");
        simpleFunctions.put(Token.T_CURRENT_USER, "org.hsqldb.Library.user");
        simpleFunctions.put(Token.T_SYSDATE, "org.hsqldb.Library.curdate");
        simpleFunctions.put(Token.T_NOW, "org.hsqldb.Library.now");
        simpleFunctions.put(Token.T_TODAY, "org.hsqldb.Library.curdate");
    }

    /**
     *  Method declaration
     *
     * @return  a summation, possibly degenerate
     * @throws  HsqlException
     */
    private Expression readSum() throws HsqlException {

        Expression r = readFactor();

        while (true) {
            int type;

            if (iToken == Expression.PLUS) {
                type = Expression.ADD;
            } else if (iToken == Expression.NEGATE) {
                type = Expression.SUBTRACT;
            } else {
                break;
            }

            Expression a = r;

            read();

            r = new Expression(type, a, readFactor());
        }

        return r;
    }

    /**
     *  Method declaration
     *
     * @return  a product, possibly degenerate
     * @throws  HsqlException
     */
    private Expression readFactor() throws HsqlException {

        Expression r = readTerm();

        while (iToken == Expression.MULTIPLY || iToken == Expression.DIVIDE) {
            int        type = iToken;
            Expression a    = r;

            read();

            r = new Expression(type, a, readTerm());
        }

        return r;
    }

    /**
     *  Method declaration
     *
     * @return  a term, possibly composite
     * @throws  HsqlException
     */
    private Expression readTerm() throws HsqlException {

        Expression r = null;

        switch (iToken) {

            case Expression.COLUMN : {
                r = readColumnExpression();

                break;
            }
            case Expression.NEGATE : {
                int type = iToken;

                read();

                r = new Expression(type, readTerm(), null);

                Trace.check(!r.getArg().isParam(),
                            Trace.Expression_resolveTypes1);

                break;
            }
            case Expression.PLUS : {
                read();

                r = readTerm();

                Trace.check(!r.isParam(), Trace.UNRESOLVED_PARAMETER_TYPE,
                            Trace.getMessage(Trace.Expression_resolveTypes1));

                break;
            }
            case Expression.OPEN : {
                read();

                r = readOr();

                if (iToken != Expression.CLOSE) {
                    throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
                }

                read();

                break;
            }
            case Expression.VALUE : {
                r = new Expression(iType, oData);

                read();

                break;
            }
            case Expression.PARAM : {
                r = new Expression(Types.NULL, null, true);

                parameters.add(r);
                read();

                break;
            }
            case Expression.SELECT : {
                SubQuery sq = parseSubquery(0, null, false, Expression.SELECT);

                r = new Expression(sq);

                read();

                break;
            }
            case Expression.ANY :
            case Expression.ALL : {
                r = parseAllAnyPredicate();

//                read();
                break;
            }
            case Expression.MULTIPLY : {
                r = new Expression(sSchema, sTable, (String) null);

                read();

                break;
            }
            case Expression.CASEWHEN :
                return readCaseWhenExpression();

            case Expression.CASE :
                return readCaseExpression();

            case Expression.NULLIF :
                return readNullIfExpression();

            case Expression.COALESCE :
            case Expression.IFNULL :
                return readCoalesceExpression();

            case Expression.SEQUENCE :
                return readSequenceExpression();

            case Expression.CAST :
            case Expression.CONVERT :
                return readCastExpression();

            case Expression.EXTRACT :
                return readExtractExpression();

            case Expression.TRIM :
                return readTrimExpression();

            case Expression.POSITION :
                return readPositionExpression();

            case Expression.SUBSTRING :
                return readSubstringExpression();

            default :
                if (Expression.isAggregate(iToken)) {
                    return readAggregate();
                } else {
                    throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
                }
        }

        return r;
    }

    /**
     * reads a CASE .. WHEN expression
     */
    Expression readCaseExpression() throws HsqlException {

        int        type      = Expression.CASEWHEN;
        Expression r         = null;
        Expression predicand = null;

        read();

        if (iToken != Expression.WHEN) {
            predicand = readOr();
        }

        Expression leaf = null;

        while (true) {
            Expression casewhen = parseCaseWhen(predicand);

            if (r == null) {
                r = casewhen;
            } else {
                leaf.setRightExpression(casewhen);
            }

            leaf = casewhen.getRightExpression();

            if (iToken != Expression.WHEN) {
                break;
            }
        }

        if (iToken == Expression.ELSE) {
            readThis(Expression.ELSE);

            Expression elsexpr = readOr();

            leaf.setRightExpression(elsexpr);
        }

        readThis(Expression.ENDWHEN);

        return r;
    }

    /**
     * Reads part of a CASE .. WHEN  expression
     */
    private Expression parseCaseWhen(Expression r) throws HsqlException {

        readThis(Expression.WHEN);

        Expression condition;

        if (r == null) {
            condition = readOr();
        } else {
            condition = new Expression(Expression.EQUAL, r, readOr());
        }

        readThis(Expression.THEN);

        Expression current = readOr();
        Expression alternatives = new Expression(Expression.ALTERNATIVE,
            current, new Expression(Types.NULL, null));
        Expression casewhen = new Expression(Expression.CASEWHEN, condition,
                                             alternatives);

        return casewhen;
    }

    /**
     * reads a CASEWHEN expression
     */
    private Expression readCaseWhenExpression() throws HsqlException {

        int        type = iToken;
        Expression r    = null;

        read();
        readThis(Expression.OPEN);

        r = readOr();

        readThis(Expression.COMMA);

        Expression thenelse = readOr();

        readThis(Expression.COMMA);

        // thenelse part is never evaluated; only init
        thenelse = new Expression(Expression.ALTERNATIVE, thenelse, readOr());
        r        = new Expression(type, r, thenelse);

        readThis(Expression.CLOSE);

        return r;
    }

    /**
     * Reads a CAST or CONVERT expression
     */
    private Expression readCastExpression() throws HsqlException {

        boolean isConvert = iToken == Expression.CONVERT;

        read();
        readThis(Expression.OPEN);

        Expression r = readOr();

        if (isConvert) {
            readThis(Expression.COMMA);
        } else {
            readThis(Expression.AS);
        }

        int     typeNr    = Types.getTypeNr(sToken);
        int     length    = 0;
        int     scale     = 0;
        boolean hasLength = false;

        if (Types.acceptsPrecisionCreateParam(typeNr)
                && tokenizer.isGetThis(Token.T_OPENBRACKET)) {
            length    = tokenizer.getInt();
            hasLength = true;

            if (Types.acceptsScaleCreateParam(typeNr)
                    && tokenizer.isGetThis(Token.T_COMMA)) {
                scale = tokenizer.getInt();
            }

            tokenizer.getThis(Token.T_CLOSEBRACKET);
        }

        if (typeNr == Types.FLOAT && length > 53) {
            throw Trace.error(Trace.NUMERIC_VALUE_OUT_OF_RANGE);
        }

        if (typeNr == Types.TIMESTAMP) {
            if (!hasLength) {
                length = 6;
            } else if (length != 0 && length != 6) {
                throw Trace.error(Trace.NUMERIC_VALUE_OUT_OF_RANGE);
            }
        }

        if (r.isParam()) {
            r.setDataType(typeNr);
        }

        r = new Expression(r, typeNr, length, scale);

        read();
        readThis(Expression.CLOSE);

        return r;
    }

    /**
     * reads a Column or Function expression
     */
    private Expression readColumnExpression() throws HsqlException {

        String     name = sToken;
        Expression r    = new Expression(sTable, name, wasQuoted);
        r.columnNameOrig = tokenizer.sTokenOrig;

        read();

        if (iToken == Expression.OPEN) {
            String   javaName = database.getJavaName(name);
            Function f        = new Function(name, javaName, false);

            session.check(javaName);

            //int len = f.getArgCount();
            int i   = 0;

            read();

            if (iToken != Expression.CLOSE) {
                while (true) {
                    f.setArgument(i++, readOr());

                    if (iToken != Expression.COMMA) {
                        break;
                    }

                    read();
                }
            }

            readThis(Expression.CLOSE);

            r = new Expression(f);
        } else {
            String javaName = (String) simpleFunctions.get(name);

            if (javaName != null) {
                Function f = new Function(name, javaName, true);

                r = new Expression(f);
            }
        }

        return r;
    }

    /**
     * reads a CONCAT expression
     */
    private Expression readConcatExpression() throws HsqlException {

        int type = iToken;

        read();
        readThis(Expression.OPEN);

        Expression r = readOr();

        readThis(Expression.COMMA);

        r = new Expression(type, r, readOr());

        readThis(Expression.CLOSE);

        return r;
    }

    /**
     * Reads a NULLIF expression
     */
    private Expression readNullIfExpression() throws HsqlException {

        // turn into a CASEWHEN
        read();
        readThis(Expression.OPEN);

        Expression r = readOr();

        readThis(Expression.COMMA);

        Expression thenelse = new Expression(Expression.ALTERNATIVE,
                                             new Expression(Types.NULL, null),
                                             r);

        r = new Expression(Expression.EQUAL, r, readOr());
        r = new Expression(Expression.CASEWHEN, r, thenelse);

        readThis(Expression.CLOSE);

        return r;
    }

    /**
     * Reads a COALESE or IFNULL expression
     */
    private Expression readCoalesceExpression() throws HsqlException {

        Expression r = null;

        // turn into a CASEWHEN
        read();
        readThis(Expression.OPEN);

        Expression leaf = null;

        while (true) {
            Expression current = readOr();

            if (leaf != null && iToken == Expression.CLOSE) {
                readThis(Expression.CLOSE);
                leaf.setLeftExpression(current);

                break;
            }

            Expression condition = new Expression(Expression.IS_NULL, current,
                                                  null);
            Expression alternatives = new Expression(Expression.ALTERNATIVE,
                new Expression(Types.NULL, null), current);
            Expression casewhen = new Expression(Expression.CASEWHEN,
                                                 condition, alternatives);

            if (r == null) {
                r = casewhen;
            } else {
                leaf.setLeftExpression(casewhen);
            }

            leaf = alternatives;

            readThis(Expression.COMMA);
        }

        return r;
    }

    /**
     * Reads an EXTRACT expression
     */
    private Expression readExtractExpression() throws HsqlException {

        read();
        readThis(Expression.OPEN);

        String name = sToken;

        // must be an accepted identifier
        if (!Expression.SQL_EXTRACT_FIELD_NAMES.contains(name)) {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
        }

        readToken();
        readThis(Expression.FROM);

        // the name argument is DAY, MONTH etc.  - OK for now for CHECK constraints
        Function f = new Function(name, database.getJavaName(name), false);

        f.setArgument(0, readOr());
        readThis(Expression.CLOSE);

        return new Expression(f);
    }

    /**
     * Reads a POSITION expression
     */
    private Expression readPositionExpression() throws HsqlException {

        read();
        readThis(Expression.OPEN);

        Function f = new Function(Token.T_POSITION,
                                  "org.hsqldb.Library.position", false);

        f.setArgument(0, readTerm());
        readThis(Expression.IN);
        f.setArgument(1, readOr());
        readThis(Expre
