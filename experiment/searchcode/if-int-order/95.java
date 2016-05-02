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
import org.hsqldb.index.RowIterator;
import org.hsqldb.lib.HashSet;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.store.ValuePool;

// fredt@users 20020215 - patch 1.7.0 by fredt
// to preserve column size etc. when SELECT INTO TABLE is used
// tony_lai@users 20021020 - patch 1.7.2 - improved aggregates and HAVING
// fredt@users 20021112 - patch 1.7.2 by Nitin Chauhan - use of switch
// rewrite of the majority of multiple if(){}else{} chains with switch(){}
// vorburger@users 20021229 - patch 1.7.2 - null handling
// boucherb@users 200307?? - patch 1.7.2 - resolve param nodes
// boucherb@users 200307?? - patch 1.7.2 - compress constant expr during resolve
// boucherb@users 200307?? - patch 1.7.2 - eager pmd and rsmd
// boucherb@users 20031005 - patch 1.7.2 - optimised LIKE
// boucherb@users 20031005 - patch 1.7.2 - improved IN value lists
// fredt@users 20031012 - patch 1.7.2 - better OUTER JOIN implementation
// thomasm@users 20041001 - patch 1.7.3 - BOOLEAN undefined handling
// fredt@users 200412xx - patch 1.7.2 - evaluation of time functions
// boucherb@users 20050516 - patch 1.8.0 - remove DITypeInfo usage for faster
//                                         statement compilation

/**
 * Expression class.
 *
 * The core functionality of this class was inherited from HypersonicSQL and
 * extensively rewritten and extended in successive versions of HSQLDB.
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @version    1.8.0
 * @since Hypersonic SQL
 */

/** @todo - fredt - constant TRUE and FALSE type expressions have valueData of
  * type BOOLEAN, while computed expressions have no valueData; this should be
  * normalised in future
  */
public class Expression {

    // leaf types
    static final int VALUE     = 1,
                     COLUMN    = 2,
                     QUERY     = 3,
                     TRUE      = 4,
                     FALSE     = -4,    // arbitrary
                     VALUELIST = 5,
                     ASTERISK  = 6,
                     FUNCTION  = 7,
                     LIMIT     = 8,
                     ROW       = 9;

// boucherb@users 20020410 - parametric compiled statements
    // new leaf type
    static final int PARAM = 9;

// --
    // operations
    static final int NEGATE   = 10,
                     ADD      = 11,
                     SUBTRACT = 12,
                     MULTIPLY = 13,
                     DIVIDE   = 14,
                     CONCAT   = 15;

    // logical operations
    static final int NOT           = 20,
                     EQUAL         = 21,
                     BIGGER_EQUAL  = 22,
                     BIGGER        = 23,
                     SMALLER       = 24,
                     SMALLER_EQUAL = 25,
                     NOT_EQUAL     = 26,
                     LIKE          = 27,
                     AND           = 28,
                     OR            = 29,
                     IN            = 30,
                     EXISTS        = 31,
                     ALL           = 32,
                     ANY           = 33,
                     IS_NULL       = 34;

    // aggregate functions
    static final int COUNT       = 40,
                     SUM         = 41,
                     MIN         = 42,
                     MAX         = 43,
                     AVG         = 44,
                     EVERY       = 45,
                     SOME        = 46,
                     STDDEV_POP  = 47,
                     STDDEV_SAMP = 48,
                     VAR_POP     = 49,
                     VAR_SAMP    = 50;

    // system functions
    static final int IFNULL      = 60,
                     CONVERT     = 61,
                     CASEWHEN    = 62,
                     EXTRACT     = 63,
                     POSITION    = 64,
                     TRIM        = 65,
                     SUBSTRING   = 66,
                     NULLIF      = 67,
                     CASE        = 68,
                     COALESCE    = 69,
                     ALTERNATIVE = 70,
                     SEQUENCE    = 71;

    // temporary used during parsing
    static final int PLUS     = 100,
                     OPEN     = 101,
                     CLOSE    = 102,
                     SELECT   = 103,
                     COMMA    = 104,
                     BETWEEN  = 106,
                     CAST     = 107,
                     END      = 108,
                     IS       = 109,
                     WHEN     = 110,
                     THEN     = 111,
                     ELSE     = 112,
                     ENDWHEN  = 113,
                     DISTINCT = 114,
                     VIEW     = 115;

    // used inside brackets for system functions
    static final int     AS                      = 122,
                         FOR                     = 123,
                         FROM                    = 124,
                         BOTH                    = 125,
                         LEADING                 = 126,
                         TRAILING                = 127,
                         YEAR                    = 128,
                         MONTH                   = 129,
                         DAY                     = 130,
                         HOUR                    = 131,
                         MINUTE                  = 132,
                         SECOND                  = 133,
                         TIMEZONE_HOUR           = 134,
                         T_TIMEZONE_MINUTE       = 135,
                         DOW                     = 136;
    static final HashSet SQL_EXTRACT_FIELD_NAMES = new HashSet();
    static final HashSet SQL_TRIM_SPECIFICATION  = new HashSet();

    static {
        SQL_EXTRACT_FIELD_NAMES.addAll(new Object[] {
            Token.T_YEAR, Token.T_MONTH, Token.T_DAY, Token.T_HOUR,
            Token.T_MINUTE, Token.T_SECOND, Token.T_TIMEZONE_HOUR,
            Token.T_TIMEZONE_MINUTE, Token.T_DOW
        });
        SQL_TRIM_SPECIFICATION.addAll(new Object[] {
            Token.T_LEADING, Token.T_TRAILING, Token.T_BOTH
        });
    }

    private static final int AGGREGATE_SELF     = -1;
    private static final int AGGREGATE_NONE     = 0;
    private static final int AGGREGATE_LEFT     = 1;
    private static final int AGGREGATE_RIGHT    = 2;
    private static final int AGGREGATE_BOTH     = 3;
    private static final int AGGREGATE_FUNCTION = 4;

    // type
    int         exprType;
    private int aggregateSpec = AGGREGATE_NONE;

    // nodes
    Expression eArg, eArg2;

    // VALUE
    Object      valueData;
    private int dataType;

    // VALUE LIST NEW
    HashSet         hList;
    Expression[]    valueList;
    private boolean isFixedConstantValueList;

    // QUERY - in single value selects, IN or EXISTS predicates
    SubQuery subQuery;
    boolean  isQueryCorrelated;

    // FUNCTION
    Function function;

    // LIKE
    private Like likeObject;

    // COLUMN
    private String      catalog;
    private String      schema;
    private String      tableName;
    private String      columnName;
    public String       columnNameOrig;
    private TableFilter tableFilter;                // null if not yet resolved
    TableFilter         outerFilter;                // defined if this is part of an OUTER JOIN condition tree

    // COLUMN
    private int     columnIndex;
    private boolean columnQuoted;
    private int     precision;
    private int     scale;
    private String  columnAlias;                    // if it is a column of a select column list
    private boolean aliasQuoted;

    //
    private boolean isDescending;                   // if it is a column in a order by
    int             joinedTableColumnIndex = -1;    // >= 0 when it is used for order by
    boolean         isDistinctAggregate;

    // PARAM
    private boolean isParam;

    // does Expression stem from a JOIN <table> ON <expression>
    boolean isInJoin;

    //
    static final Integer INTEGER_0 = ValuePool.getInt(0);
    static final Integer INTEGER_1 = ValuePool.getInt(1);

    /**
     * Creates a new boolean expression
     * @param b boolean constant
     */
    Expression(boolean b) {
        exprType = b ? TRUE
                     : FALSE;
    }

    /**
     * Creates a new FUNCTION expression
     * @param f function
     */
    Expression(Function f) {

        exprType = FUNCTION;
        function = f;

        if (f.hasAggregate) {
            aggregateSpec = AGGREGATE_FUNCTION;
        }
    }

    /**
     * Creates a new SEQUENCE expression
     * @param sequence number sequence
     */
    Expression(NumberSequence sequence) {

        exprType  = SEQUENCE;
        valueData = sequence;
        dataType  = sequence.getType();
    }

    /**
     * Copy Constructor. Used by TableFilter to move a condition to a filter.
     * @param e source expression
     */
    Expression(Expression e) {

        exprType = e.exprType;
        dataType = e.dataType;
        eArg     = e.eArg;
        eArg2    = e.eArg2;
        isInJoin = e.isInJoin;

        //
        likeObject = e.likeObject;
        subQuery   = e.subQuery;
        function   = e.function;

        checkAggregate();
    }

    /**
     * Creates a new QUERY expression
     * @param sq subquery
     */
    Expression(SubQuery sq) {
        exprType = QUERY;
        subQuery = sq;
    }

    /**
     * Creates a new VALUELIST expression
     * @param valueList array of Expression
     */
    Expression(Expression[] valueList) {
        exprType       = VALUELIST;
        this.valueList = valueList;
    }

    /**
     * Creates a new binary (or unary) operation expression
     *
     * @param type operator type
     * @param e operand 1
     * @param e2 operand 2
     */
    Expression(int type, Expression e, Expression e2) {

        exprType = type;
        eArg     = e;
        eArg2    = e2;

        checkAggregate();
    }

    /**
     * creates a CONVERT expression
     */
    Expression(Expression e, int dataType, int precision, int scale) {

        this.exprType    = CONVERT;
        this.eArg        = e;
        this.dataType    = dataType;
        this.precision   = precision;
        this.scale       = scale;
        this.columnAlias = e.columnAlias;
        this.aliasQuoted = e.aliasQuoted;

        checkAggregate();
    }

    /**
     * Creates a new LIKE expression
     *
     * @param e operand 1
     * @param e2 operand 2
     * @param escape escape character
     */
    Expression(Expression e, Expression e2, Character escape,
               boolean hasCollation) {

        exprType   = LIKE;
        eArg       = e;
        eArg2      = e2;
        likeObject = new Like(escape, hasCollation);

        checkAggregate();
    }

    /**
     * Creates a new ASTERISK or COLUMN expression
     * @param table table
     * @param column column
     */
    Expression(String schema, String table, String column) {

        this.schema = schema;
        tableName   = table;

        if (column == null) {
            exprType = ASTERISK;
        } else {
            exprType   = COLUMN;
            columnName = column;
        }
    }

    /**
     * Creates a new ASTERIX or possibly quoted COLUMN expression
     * @param table table
     * @param column column name
     * @param isquoted boolean
     */
    Expression(String table, String column, boolean isquoted) {

        tableName = table;

        if (column == null) {
            exprType = ASTERISK;
        } else {
            exprType     = COLUMN;
            columnName   = column;
            columnQuoted = isquoted;
        }
    }

    Expression(TableFilter filter, Column column) {

        schema    = filter.filterTable.tableName.schema.name;
        tableName = filter.getName();

        if (column == null) {
            exprType = ASTERISK;
        } else {
            exprType     = COLUMN;
            columnName   = column.columnName.name;
            columnQuoted = column.columnName.isNameQuoted;
            dataType     = column.getType();
        }
    }

    /**
     * Creates a new VALUE expression
     *
     * @param datatype data type
     * @param o data
     */
    Expression(int datatype, Object o) {

        exprType  = VALUE;
        dataType  = datatype;
        valueData = o;
    }

    /**
     * Creates a new (possibly PARAM) VALUE expression
     *
     * @param datatype initial datatype
     * @param o initial value
     * @param isParam true if this is to be a PARAM VALUE expression
     */
    Expression(int datatype, Object o, boolean isParam) {

        this(datatype, o);

        this.isParam = isParam;

        if (isParam) {
            paramMode = PARAM_IN;
        }
    }

    boolean isTypeEqual(Expression other) {
        return dataType == other.dataType && precision == other.precision
               && scale == other.scale;
    }

    private void checkAggregate() {

        if (isAggregate(exprType)) {
            aggregateSpec = AGGREGATE_SELF;
        } else {
            aggregateSpec = AGGREGATE_NONE;

            if ((eArg != null) && eArg.isAggregate()) {
                aggregateSpec += AGGREGATE_LEFT;
            }

            if ((eArg2 != null) && eArg2.isAggregate()) {
                aggregateSpec += AGGREGATE_RIGHT;
            }
        }
    }

    public String describe(Session session) {
        return describe(session, 0);
    }

    static String getContextDDL(Expression expression) throws HsqlException {

        String ddl = expression.getDDL();

        if (expression.exprType != VALUE && expression.exprType != COLUMN
                && expression.exprType != FUNCTION
                && expression.exprType != ALTERNATIVE
                && expression.exprType != CASEWHEN
                && expression.exprType != CONVERT) {
            StringBuffer temp = new StringBuffer();

            ddl = temp.append('(').append(ddl).append(')').toString();
        }

        return ddl;
    }

    /**
     *  returns the complete name of the column represented by the expression
     *
     *  If an alias is known for the column's table, this alias will precede the
     *  column name, except it's "SYSTEM_SUBQUERY".
     *  If no alias is known, the column's table will be asked for its
     *  statementName, which then will precede the column name.
     */
    String getColumnDDL() throws HsqlException {

        Trace.doAssert(exprType == COLUMN);

        StringBuffer buf   = new StringBuffer();
        Table        table = tableFilter.getTable();

        if (!tableName.equals("SYSTEM_SUBQUERY")) {
            if (!table.getName().name.equals(tableName)) {
                buf.append('"').append(tableName).append('"').append('.');
            } else {
                buf.append(table.tableName.statementName).append('.');
            }
        }

        buf.append(table.getColumn(columnIndex).columnName.statementName);

        return buf.toString();
    }

    /**
     * For use with CHECK constraints. Under development.
     *
     * Currently supports a subset of expressions and is suitable for CHECK
     * search conditions that refer only to the inserted/updated row.
     *
     * For full DDL reporting of VIEW select queries and CHECK search
     * conditions, future improvements here are dependent upon improvements to
     * SELECT query parsing, so that it is performed in a number of passes.
     * An early pass should result in the query turned into an Expression tree
     * that contains the information in the original SQL without any
     * alterations, and with tables and columns all resolved. This Expression
     * can then be preserved for future use. Table and column names that
     * are not user-defined aliases should be kept as the HsqlName structures
     * so that table or column renaming is reflected in the precompiled
     * query.
     *
     * @return DDL
     * @throws HsqlException
     */
    String getDDL() throws HsqlException {

        StringBuffer buf   = new StringBuffer(64);
        String       left  = null;
        String       right = null;

        if (eArg != null) {
            left = Expression.getContextDDL(eArg);
        }

        if (eArg2 != null) {
            right = Expression.getContextDDL(eArg2);
        }

        switch (exprType) {

            case FUNCTION :
                return function.getDLL();

            case VALUE :
                try {
                    return isParam ? Token.T_QUESTION
                                   : Column.createSQLString(valueData,
                                   dataType);
                } catch (HsqlException e) {}

                return buf.toString();

            case COLUMN :

                // this is a limited solution
                Table table = tableFilter.getTable();

                if (tableName != null) {
                    buf.append(table.tableName.statementName);
                    buf.append('.');
                }

                buf.append(
                    table.getColumn(columnIndex).columnName.statementName);

                return buf.toString();

            case TRUE :
                return Token.T_TRUE;

            case FALSE :
                return Token.T_FALSE;

            case VALUELIST :
                for (int i = 0; i < valueList.length; i++) {
                    buf.append(valueList[i].getDDL());

                    if (i < valueList.length - 1) {
                        buf.append(',');
                    }
                }

                return buf.toString();

            case ASTERISK :
                buf.append('*');

                return buf.toString();

            case NEGATE :
                buf.append('-').append(left);

                return buf.toString();

            case ADD :
                buf.append(left).append('+').append(right);

                return buf.toString();

            case SUBTRACT :
                buf.append(left).append('-').append(right);

                return buf.toString();

            case MULTIPLY :
                buf.append(left).append('*').append(right);

                return buf.toString();

            case DIVIDE :
                buf.append(left).append('/').append(right);

                return buf.toString();

            case CONCAT :
                buf.append(left).append("||").append(right);

                return buf.toString();

            case NOT :
                if (eArg.exprType == IS_NULL) {
                    buf.append(getContextDDL(eArg.eArg)).append(' ').append(
                        Token.T_IS).append(' ').append(Token.T_NOT).append(
                        ' ').append(Token.T_NULL);

                    return buf.toString();
                }

                buf.append(Token.T_NOT).append(' ').append(left);

                return buf.toString();

            case EQUAL :
                buf.append(left).append('=').append(right);

                return buf.toString();

            case BIGGER_EQUAL :
                buf.append(left).append(">=").append(right);

                return buf.toString();

            case BIGGER :
                buf.append(left).append('>').append(right);

                return buf.toString();

            case SMALLER :
                buf.append(left).append('<').append(right);

                return buf.toString();

            case SMALLER_EQUAL :
                buf.append(left).append("<=").append(right);

                return buf.toString();

            case NOT_EQUAL :
                if (Token.T_NULL.equals(right)) {
                    buf.append(left).append(" IS NOT ").append(right);
                } else {
                    buf.append(left).append("!=").append(right);
                }

                return buf.toString();

            case LIKE :
                buf.append(left).append(' ').append(Token.T_LIKE).append(' ');
                buf.append(right);

                /** @todo fredt - scripting of non-ascii escapes needs changes to general script logging */
                if (likeObject.escapeChar != null) {
                    buf.append(' ').append(Token.T_ESCAPE).append(' ').append(
                        '\'');
                    buf.append(likeObject.escapeChar.toString()).append('\'');
                    buf.append(' ');
                }

                return buf.toString();

            case AND :
                buf.append(left).append(' ').append(Token.T_AND).append(
                    ' ').append(right);

                return buf.toString();

            case OR :
                buf.append(left).append(' ').append(Token.T_OR).append(
                    ' ').append(right);

                return buf.toString();

            case ALL :
                buf.append(left).append(' ').append(Token.T_ALL).append(
                    ' ').append(right);

                return buf.toString();

            case ANY :
                buf.append(left).append(' ').append(Token.T_ANY).append(
                    ' ').append(right);

                return buf.toString();

            case IN :
                buf.append(left).append(' ').append(Token.T_IN).append(
                    ' ').append(right);

                return buf.toString();

            case CONVERT :
                buf.append(' ').append(Token.T_CONVERT).append('(');
                buf.append(left).append(',');
                buf.append(Types.getTypeString(dataType, precision, scale));
                buf.append(')');

                return buf.toString();

            case CASEWHEN :
                buf.append(' ').append(Token.T_CASEWHEN).append('(');
                buf.append(left).append(',').append(right).append(')');

                return buf.toString();

            case IS_NULL :
                buf.append(left).append(' ').append(Token.T_IS).append(
                    ' ').append(Token.T_NULL);

                return buf.toString();

            case ALTERNATIVE :
                buf.append(left).append(',').append(right);

                return buf.toString();

            case QUERY :
/*
                buf.append('(');
                buf.append(subSelect.getDDL());
                buf.append(')');
*/
                break;

            case EXISTS :
                buf.append(' ').append(Token.T_EXISTS).append(' ');
                break;

            case COUNT :
                buf.append(' ').append(Token.T_COUNT).append('(');
                break;

            case SUM :
                buf.append(' ').append(Token.T_SUM).append('(');
                buf.append(left).append(')');
                break;

            case MIN :
                buf.append(' ').append(Token.T_MIN).append('(');
                buf.append(left).append(')');
                break;

            case MAX :
                buf.append(' ').append(Token.T_MAX).append('(');
                buf.append(left).append(')');
                break;

            case AVG :
                buf.append(' ').append(Token.T_AVG).append('(');
                buf.append(left).append(')');
                break;

            case EVERY :
                buf.append(' ').append(Token.T_EVERY).append('(');
                buf.append(left).append(')');
                break;

            case SOME :
                buf.append(' ').append(Token.T_SOME).append('(');
                buf.append(left).append(')');
                break;

            case STDDEV_POP :
                buf.append(' ').append(Token.T_STDDEV_POP).append('(');
                buf.append(left).append(')');
                break;

            case STDDEV_SAMP :
                buf.append(' ').append(Token.T_STDDEV_SAMP).append('(');
                buf.append(left).append(')');
                break;

            case VAR_POP :
                buf.append(' ').append(Token.T_VAR_POP).append('(');
                buf.append(left).append(')');
                break;

            case VAR_SAMP :
                buf.append(' ').append(Token.T_VAR_SAMP).append('(');
                buf.append(left).append(')');
                break;
        }

        throw Trace.error(Trace.EXPRESSION_NOT_SUPPORTED);
    }

    private String describe(Session session, int blanks) {

        int          lIType;
        StringBuffer buf = new StringBuffer(64);

        buf.append('\n');

        for (int i = 0; i < blanks; i++) {
            buf.append(' ');
        }

        if (oldIType != -1) {
            buf.append("SET TRUE, WAS: ");
        }

        lIType = oldIType == -1 ? exprType
                                : oldIType;

        switch (lIType) {

            case FUNCTION :
                buf.append("FUNCTION ");
                buf.append(function.describe(session));

                return buf.toString();

            case VALUE :
                if (isParam) {
                    buf.append("PARAM ");
                }

                buf.append("VALUE = ").append(valueData);
                buf.append(", TYPE = ").append(Types.getTypeString(dataType));

                return buf.toString();

            case COLUMN :
                buf.append("COLUMN ");

                if (tableName != null) {
                    buf.append(tableName);
                    buf.append('.');
                }

                buf.append(columnName);

                return buf.toString();

            case QUERY :
                buf.append("QUERY ");
                buf.append(subQuery.select.describe(session));

                return buf.toString();

            case TRUE :
                buf.append("TRUE ");
                break;

            case FALSE :
                buf.append("FALSE ");
                break;

            case VALUELIST :
                buf.append("VALUELIST ");
                buf.append(" TYPE = ").append(Types.getTypeString(dataType));

                if (valueList != null) {
                    for (int i = 0; i < valueList.length; i++) {
                        buf.append(valueList[i].describe(session,
                                                         blanks + blanks));
                        buf.append(' ');
                    }
                }
                break;

            case ASTERISK :
                buf.append("* ");
                break;

            case NEGATE :
                buf.append("NEGATE ");
                break;

            case ADD :
                buf.append("ADD ");
                break;

            case SUBTRACT :
                buf.append("SUBTRACT ");
                break;

            case MULTIPLY :
                buf.append("MULTIPLY ");
                break;

            case DIVIDE :
                buf.append("DIVIDE ");
                break;

            case CONCAT :
                buf.append("CONCAT ");
                break;

            case NOT :
                buf.append("NOT ");
                break;

            case EQUAL :
                buf.append("EQUAL ");
                break;

            case BIGGER_EQUAL :
                buf.append("BIGGER_EQUAL ");
                break;

            case BIGGER :
                buf.append("BIGGER ");
                break;

            case SMALLER :
                buf.append("SMALLER ");
                break;

            case SMALLER_EQUAL :
                buf.append("SMALLER_EQUAL ");
                break;

            case NOT_EQUAL :
                buf.append("NOT_EQUAL ");
                break;

            case LIKE :
                buf.append("LIKE ");
                buf.append(likeObject.describe(session));
                break;

            case AND :
                buf.append("AND ");
                break;

            case OR :
                buf.append("OR ");
                break;

            case ALL :
                buf.append("ALL ");
                break;

            case ANY :
                buf.append("ANY ");
                break;

            case IN :
                buf.append("IN ");
                break;

            case IS_NULL :
                buf.append("IS_NULL ");
                break;

            case EXISTS :
                buf.append("EXISTS ");
                break;

            case COUNT :
                buf.append("COUNT ");
                break;

            case SUM :
                buf.append("SUM ");
                break;

            case MIN :
                buf.append("MIN ");
                break;

            case MAX :
                buf.append("MAX ");
                break;

            case AVG :
                buf.append("AVG ");
                break;

            case EVERY :
                buf.append(Token.T_EVERY).append(' ');
                break;

            case SOME :
                buf.append(Token.T_SOME).append(' ');
                break;

            case STDDEV_POP :
                buf.append(Token.T_STDDEV_POP).append(' ');
                break;

            case STDDEV_SAMP :
                buf.append(Token.T_STDDEV_SAMP).append(' ');
                break;

            case VAR_POP :
                buf.append(Token.T_VAR_POP).append(' ');
                break;

            case VAR_SAMP :
                buf.append(Token.T_VAR_SAMP).append(' ');
                break;

            case CONVERT :
                buf.append("CONVERT ");
                buf.append(Types.getTypeString(dataType, precision, scale));
                buf.append(' ');
                break;

            case CASEWHEN :
                buf.append("CASEWHEN ");
                break;
        }

        if (isInJoin) {
            buf.append(" join");
        }

        if (eArg != null) {
            buf.append(" arg1=[");
            buf.append(eArg.describe(session, blanks + 1));
            buf.append(']');
        }

        if (eArg2 != null) {
            buf.append(" arg2=[");
            buf.append(eArg2.describe(session, blanks + 1));
            buf.append(']');
        }

        return buf.toString();
    }

    /**
     * Set the data type
     *
     *
     * @param type data type
     */
    void setDataType(int type) {
        dataType = type;
    }

    int oldIType = -1;

    /**
     * When an Expression is assigned to a TableFilter, a copy is made for use
     * there and the original is set to Expression.TRUE
     *
     */
    void setTrue() {

        if (oldIType == -1) {
            oldIType = exprType;
        }

        exprType = TRUE;
    }

    void setNull() {

        isParam   = false;
        exprType  = VALUE;
        dataType  = Types.NULL;
        valueData = null;
        eArg      = null;
        eArg2     = null;
    }

    /**
     * Check if the given expression defines similar operation as this
     * expression. This method is used for ensuring an expression in
     * the ORDER BY clause has a matching column in the SELECT list. This check
     * is necessary with a SELECT DISTINCT query.<br>
     *
     * In the future we may perform the test when evaluating the search
     * condition to get a more accurate match.
     *
     * @param exp expression
     * @return boolean
     */
    public boolean similarTo(Expression exp) {

        if (exp == null) {
            return false;
        }

        if (exp == this) {
            return true;
        }

        /** @todo fredt - equals() method for valueList, subSelect and function are needed */
        return exprType == exp.exprType && dataType == exp.dataType
               && equals(valueData, exp.valueData)
               && equals(valueList, exp.valueList)
               && equals(subQuery, exp.subQuery)
               && equals(function, exp.function)
               && equals(tableName, exp.tableName)
               && equals(columnName, exp.columnName)
               && similarTo(eArg, exp.eArg) && similarTo(eArg2, exp.eArg2);
    }

    static boolean equals(Object o1, Object o2) {
        return (o1 == null) ? o2 == null
                            : o1.equals(o2);
    }

    static boolean equals(Expression[] ae1, Expression[] ae2) {

        if (ae1 == ae2) {
            return true;
        }

        if (ae1.length != ae2.length) {
            return false;
        }

        int     len    = ae1.length;
        boolean equals = true;

        for (int i = 0; i < len; i++) {
            Expression e1 = ae1[i];
            Expression e2 = ae2[i];

            equals = (e1 == null) ? e2 == null
                                  : e1.equals(e2);
        }

        return equals;
    }

    static boolean similarTo(Expression e1, Expression e2) {
        return (e1 == null) ? e2 == null
                            : e1.similarTo(e2);
    }

/** @todo fredt - workaround for functions in ORDER BY and GROUP BY needs
 *  checking the argument of the function to ensure they are valid. */

    /**
     * Check if this expression can be included in a group by clause.
     * <p>
     * It can, if itself is a column expression, and it is not an aggregate
     * expression.
     *
     * @return boolean
     */
    boolean canBeInGroupBy() {

        if (exprType == FUNCTION) {
            return true;
        }

        return isColumn() && (!(isAggregate()));
    }

    /**
     * Check if this expression can be included in an order by clause.
     * <p>
     * It can, if itself is a column expression.
     *
     * @return boolean
     */
    boolean canBeInOrderBy() {
        return exprType == FUNCTION || joinedTableColumnIndex != -1
               || isColumn() || isAggregate();
    }

    /**
     * Check if this expression defines at least one column.
     * <p>
     * It is, if itself is a column expression, or any the argument
     * expressions is a column expression.
     *
     * @return boolean
     */
    private boolean isColumn() {

        switch (exprType) {

            case COLUMN :
                return true;

            case NEGATE :
                return eArg.isColumn();

            case ADD :
            case SUBTRACT :
            case MULTIPLY :
            case DIVIDE :
            case CONCAT :
                return eArg.isColumn() || eArg2.isColumn();
        }

        return false;
    }

    /**
     * Collect column name used in this expression.
     *
     * @param columnNames set to be filled
     * @return true if a column name is used in this expression
     */
    boolean collectColumnName(HashSet columnNames) {

        boolean result = exprType == COLUMN;

        if (result) {
            columnNames.add(columnName);
        }

        return result;
    }

    /**
     * Collect all column names used in this expression or any of nested
     * expression.
     *
     * @param columnNames set to be filled
     */
    void collectAllColumnNames(HashSet columnNames) {

        if (!collectColumnName(columnNames)) {
            if (eArg != null) {
                eArg.collectAllColumnNames(columnNames);
            }

            if (eArg2 != null) {
                eArg2.collectAllColumnNames(columnNames);
            }
        }
    }

    /**
     * Check if this expression defines a constant value.
     * <p>
     * It does, if it is a constant value expression, or all the argument
     * expressions define constant values.
     *
     * @return boolean
     */
    boolean isConstant() {

        switch (exprType) {

            case VALUE :
                return true;

            case NEGATE :
                return eArg.isConstant();

            case ADD :
            case SUBTRACT :
            case MULTIPLY :
            case DIVIDE :
            case CONCAT :
                return eArg.isConstant() && eArg2.isConstant();
        }

        return false;
    }

    /**
     * Check if this expression can be included as a result column in an
     * aggregated select statement.
     * <p>
     * It can, if itself is an aggregate expression, or it results a constant
     * value.
     *
     * @return boolean
     */
    boolean canBeInAggregate() {
        return isAggregate() || isConstant();
    }

    /**
     *  Is this (indirectly) an aggregate expression
     *
     *  @return boolean
     */
    boolean isAggregate() {
        return aggregateSpec != AGGREGATE_NONE;
    }

    /**
     *  Is this directly an aggregate expression
     *
     *
     *  @return boolean
     */
    boolean isSelfAggregate() {
        return aggregateSpec == AGGREGATE_SELF;
    }

    static boolean isAggregate(int type) {

        switch (type) {

            case COUNT :
            case MAX :
            case MIN :
            case SUM :
            case AVG :
            case EVERY :
            case SOME :
            case STDDEV_POP :
            case STDDEV_SAMP :
            case VAR_POP :
            case VAR_SAMP :
                return true;
        }

        return false;
    }

// tony_lai@users having

    /**
     *  Checks for conditional expression.
     *
     *
     *  @return boolean
     */
    boolean isConditional() {

        switch (exprType) {

            case TRUE :
            case FALSE :
            case EQUAL :
            case BIGGER_EQUAL :
            case BIGGER :
            case SMALLER :
            case SMALLER_EQUAL :
            case NOT_EQUAL :
            case LIKE :
            case IN :
            case EXISTS :
            case IS_NULL :
                return true;

            case NOT :
                return eArg.isConditional();

            case AND :
            case OR :
                return eArg.isConditional() && eArg2.isConditional();

            default :
                return false;
        }
    }

    /**
     * Collects all expressions that must be in the GROUP BY clause, for a
     * grouped select statement.
     *
     * @param colExps expression list
     */
    void collectInGroupByExpressions(HsqlArrayList colExps) {

        if (!(isConstant() || isSelfAggregate())) {
            if (isColumn()) {
                colExps.add(this);
            } else if (exprType == FUNCTION) {

//                function.collectInGroupByExpressions(colExps);
            } else if (exprType == CASEWHEN) {
                eArg2.collectInGroupByExpressions(colExps);
            } else {
                if (eArg != null) {
                    eArg.collectInGroupByExpressions(colExps);
                }

                if (eArg2 != null) {
                    eArg2.collectInGroupByExpressions(colExps);
                }
            }
        }
    }

    /**
     * Set an ORDER BY column expression DESC
     *
     */
    void setDescending() {
        isDescending = true;
    }

    /**
     * Is an ORDER BY column expression DESC
     *
     *
     * @return boolean
     */
    boolean isDescending() {
        return isDescending;
    }

    /**
     * Set the column alias and whether the name is quoted
     *
     * @param s alias
     * @param isquoted boolean
     */
    void setAlias(String s, boolean isquoted) {
        columnAlias = s;
        aliasQuoted = isquoted;
    }

    /**
     * Change the column name
     *
     * @param newname name
     * @param isquoted quoted
     */
    void setColumnName(String newname, boolean isquoted) {
        columnName   = newname;
        columnQuoted = isquoted;
    }

    /**
     * Change the table name
     *
     * @param newname table name for column expression
     */
    void setTableName(String newname) {
        tableName = newname;
    }

    /**
     * Return the user defined alias or null if none
     *
     * @return alias
     */
    String getDefinedAlias() {
        return columnAlias;
    }

    /**
     * Get the column alias
     *
     *
     * @return alias
     */
    String getAlias() {

        if (columnAlias != null) {
            return columnAlias;
        }

        if (exprType == COLUMN) {
            return columnName;
        }

        return "";
    }

    /**
     * Is a column alias quoted
     *
     * @return boolean
     */
    boolean isAliasQuoted() {

        if (columnAlias != null) {
            return aliasQuoted;
        }

        if (exprType == COLUMN) {
            return columnQuoted;
        }

        return false;
    }

    /**
     * Returns the type of expression
     *
     *
     * @return type
     */
    int getType() {
        return exprType;
    }

    /**
     * Returns the left node
     *
     *
     * @return argument
     */
    Expression getArg() {
        return eArg;
    }

    /**
     * Returns the right node
     *
     *
     * @return argument
     */
    Expression getArg2() {
        return eArg2;
    }

    /**
     * Returns the table filter for a COLUMN expression
     *
     * @return table filter
     */
    TableFilter getFilter() {
        return tableFilter;
    }

    /**
     * Final check for all expressions.
     *
     * @param check boolean
     * @return boolean
     * @throws HsqlException
     */
    boolean checkResolved(boolean check) throws HsqlException {

        boolean result = true;

        if (eArg != null) {
            result = result && eArg.checkResolved(check);
        }

        if (eArg2 != null) {
            result = result && eArg2.checkResolved(check);
        }

        if (subQuery != null && subQuery.select != null) {
            result = result && subQuery.select.checkResolved(check);
        }

        if (function != null) {
            result = result && function.checkResolved(check);
        }

        if (valueList != null) {
            for (int i = 0; i < valueList.length; i++) {
                result = result && valueList[i].checkResolved(check);
            }
        }

        if (exprType == COLUMN) {
            if (tableFilter == null) {

                // if an order by column alias
                result = joinedTableColumnIndex != -1;

                if (!result && check) {
                    String err = tableName == null ? columnName
                                                   : tableName + "."
                                                     + columnName;

                    throw Trace.error(Trace.COLUMN_NOT_FOUND, err);
                }
            } else {
                tableFilter.usedColumns[this.columnIndex] = true;
            }
        }

        return result;
    }

    /**
     * Resolve the table names for columns and throws if a column remains
     * unresolved.
     *
     * @param filters list of filters
     *
     * @throws HsqlException
     */
    void checkTables(HsqlArrayList filters) throws HsqlException {

        if (filters == null || exprType == Expression.VALUE) {
            return;
        }

        if (eArg != null) {
            eArg.checkTables(filters);
        }

        if (eArg2 != null) {
            eArg2.checkTables(filters);
        }

        switch (exprType) {

            case COLUMN :
                boolean found = false;
                int     len   = filters.size();

                for (int j = 0; j < len; j++) {
                    TableFilter filter     = (TableFilter) filters.get(j);
                    String      filterName = filter.getName();

                    if (tableName == null || filterName.equalsIgnoreCase(tableName)) {
                        Table table = filter.getTable();
                        int   i     = table.findColumn(columnName);
                        if (i == -1)
                        	i = table.findColumn(columnNameOrig);

                        if (i != -1) {
                            if (tableName == null) {
                                if (found) {
                                    throw Trace.error(
                                        Trace.AMBIGUOUS_COLUMN_REFERENCE,
                                        columnName);
                                }

                                //
                                found = true;
                            } else {
                                return;
                            }
                        }
                    }
                }

                if (found) {
                    return;
                }

                throw Trace.error(Trace.COLUMN_NOT_FOUND, columnName);
            case QUERY :

                // fredt - subquery in join condition !
                break;

            case FUNCTION :
                if (function != null) {
                    function.checkTables(filters);
                }
                break;

            case ALL :
            case ANY :
                break;

            case IN :
                if (eArg2.exprType != QUERY) {
                    Expression[] vl = eArg2.valueList;

                    for (int i = 0; i < vl.length; i++) {
                        vl[i].checkTables(filters);
                    }
                }
                break;

            default :
        }
    }

    /**
     * return the expression for an aliases
     */
    Expression getExpressionForAlias(Expression[] columns, int length) {

        for (int i = 0; i < length; i++) {
            if (columnName.equals(columns[i].columnAlias)
                    && (tableName == null
                        || tableName.equals(columns[i].tableName))) {
                return columns[i];
            }
        }

        return this;
    }

    /**
     * Replace aliases with expression trees
     */
    void replaceAliases(Expression[] columns,
                        int length) throws HsqlException {

        if (eArg != null) {
            if (eArg.exprType == Expression.COLUMN) {
                eArg = eArg.getExpressionForAlias(columns, length);
            } else {
                eArg.replaceAliases(columns, length);
            }
        }

        if (eArg2 != null) {
            if (eArg2.exprType == Expression.COLUMN) {
                eArg2 = eArg2.getExpressionForAlias(columns, length);
            } else {
                eArg2.replaceAliases(columns, length);
            }
        }

        switch (exprType) {

            case QUERY :
                break;

            case FUNCTION :
                if (function != null) {
                    function.replaceAliases(columns, length);
                }
                break;

            case ALL :
            case ANY :
                break;

            case IN :
                if (eArg2.exprType != QUERY) {
                    Expression[] vl = eArg2.valueList;

                    for (int i = 0; i < vl.length; i++) {
                        if (vl[i].exprType == Expression.COLUMN) {
                            vl[i] = vl[i].getExpressionForAlias(columns,
                                                                length);
                        } else {
                            vl[i].replaceAliases(columns, length);
                        }
                    }
                }
                break;

            default :
        }
    }

    /**
     * Workaround for CHECK constraints. We don't want optimisation so we
     * flag all LIKE expressions as already optimised.
     *
     * @throws HsqlException
     */
    void setLikeOptimised() throws HsqlException {

        if (eArg != null) {
            eArg.setLikeOptimised();
        }

        if (eArg2 != null) {
            eArg2.setLikeOptimised();
        }

        if (exprType == LIKE) {
            likeObject.optimised = true;
        }
    }

    /**
     * Removes table filter resolution from an Expression tree.
     */
/*
    void removeFilters() throws HsqlException {

        if (eArg != null) {
            eArg.removeFilters();
        }

        if (eArg2 != null) {
            eArg2.removeFilters();
        }

        switch (exprType) {

            case COLUMN :
                tableFilter = null;

                return;

            case QUERY :
                if (subSelect != null) {
                    subSelect.removeFilters();
                }
                break;

            case FUNCTION :
                if (function != null) {
                    function.removeFilters();
                }
                break;

            case IN :
                if (eArg2.exprType != QUERY) {
                    Expression[] vl = eArg2.valueList;

                    for (int i = 0; i < vl.length; i++) {
                        vl[i].removeFilters();
                    }
                }
                break;

            default :
        }
    }
*/

    /**
     * set boolean flags and expressions for columns in a join
     *
     * @param filter target table filter
     * @param columns boolean array
     * @param elist expression list
     */
    void getEquiJoinColumns(TableFilter filter, boolean[] columns,
                            Expression[] elist) {

        if (eArg != null) {
            eArg.getEquiJoinColumns(filter, columns, elist);
        }

        if (eArg2 != null) {
            eArg2.getEquiJoinColumns(filter, columns, elist);
        }

        if (exprType == EQUAL) {
            if (eArg.tableFilter == eArg2.tableFilter) {
                return;
            }

            // an elist element may be set more than once - OK
            if (eArg.tableFilter == filter) {
                if (eArg2.exprType == COLUMN || eArg2.exprType == VALUE) {
                    columns[eArg.columnIndex] = true;
                    elist[eArg.columnIndex]   = eArg2;
                }

                return;
            }

            if (eArg2.tableFilter == filter) {
                if (eArg.exprType == COLUMN || eArg.exprType == VALUE) {
                    columns[eArg2.columnIndex] = true;
                    elist[eArg2.columnIndex]   = eArg;
                }
            }
        }
    }

    /**
     * Find a table filter with the given table alias
     */
    TableFilter findTableFilter(TableFilter[] list) {

        for (int t = 0; t < list.length; t++) {
            TableFilter f = list[t];

            if (schema == null
                    || f.filterTable.getSchemaName().equals(schema)) {
                if (f.getName().equals(tableName)) {
                    return f;
                }
            }
        }

        return null;
    }

    /**
     * Resolve the table names for columns
     *
     * @param f table filter
     *
     * @throws HsqlException
     */
    void resolveTables(TableFilter f) throws HsqlException {

        if (isParam || f == null || exprType == Expression.VALUE) {
            return;
        }

        if (eArg != null) {
            eArg.resolveTables(f);
        }

        if (eArg2 != null) {
            eArg2.resolveTables(f);
        }

        switch (exprType) {

            case COLUMN :
                if (tableFilter != null) {
                    break;
                }

                String filterName = f.getName();

                if (tableName == null || tableName.equalsIgnoreCase(filterName)) {
                    Table table = f.getTable();
                    int   i     = table.findColumn(columnName);
                    if (i == -1)
                    	i = table.findColumn(columnNameOrig);
                    
                    if (i != -1) {
                        tableFilter = f;
                        columnIndex = i;
                        tableName   = filterName;

                        setTableColumnAttributes(table, i);

                        // COLUMN is leaf; we are done
                        return;
                    }
                }
                break;

            case QUERY :

                // we now (1_7_2_ALPHA_R) resolve independently first, then
                // resolve in the enclosing context
                if (subQuery != null) {
                    subQuery.select.resolveTablesUnion(f);
                }
                break;

            case FUNCTION :
                if (function != null) {
                    function.resolveTables(f);
                }
                break;

            case ALL :
            case ANY :
                break;

            case IN :
                if (eArg2.exprType != QUERY) {
                    Expression[] vl = eArg2.valueList;

                    for (int i = 0; i < vl.length; i++) {
                        vl[i].resolveTables(f);
                    }
                }
                break;

            default :
        }
    }

    /**
     * For CASE WHEN and its special cases section 9.3 of the SQL standard
     * on type aggregation should be implemented.
     */
    int getCaseWhenType(Session session) throws HsqlException {

        /*
            find data type in condition
            int type = eArg.eArg.getDataType();
            then recurse on eArg2

        */
        return eArg2.dataType;
    }

    void resolveTypes(Session session) throws HsqlException {

        if (isParam) {
            return;
        }

        if (eArg != null) {
            eArg.resolveTypes(session);
        }

        if (eArg2 != null) {
            eArg2.resolveTypes(session);
        }

        switch (exprType) {

            case VALUE :
                if (dataType == Types.BOOLEAN && valueData != null) {
                    exprType = ((Boolean) valueData).booleanValue() ? TRUE
                                                                    : FALSE;
                }
                break;

            case COLUMN :
                break;

            case FUNCTION :
                function.resolveType(session);

                dataType = function.getReturnType();
                break;

            case QUERY : {
                subQuery.select.resolveTypes(session);

                dataType = subQuery.select.exprColumns[0].dataType;

                break;
            }
            case NEGATE :
                if (eArg.isParam) {
                    throw Trace.error(Trace.UNRESOLVED_PARAMETER_TYPE,
                                      Trace.Expression_resolveTypes1);
                }

                dataType = eArg.dataType;

                if (isFixedConstant()) {
                    valueData = getValue(session, dataType);
                    eArg      = null;
                    exprType  = VALUE;
                }
                break;

            case ADD :

                // concat using + operator
                // non-standard concat operator to be deprecated
                if (Types.isCharacterType(eArg.dataType)
                        || Types.isCharacterType(eArg2.dataType)) {
                    exprType = Expression.CONCAT;
                    dataType = Types.VARCHAR;

                    if (isFixedConstant()) {
                        valueData = getValue(session, dataType);
                        eArg      = null;
                        eArg2     = null;
                        exprType  = VALUE;
                    } else {
                        if (eArg.isParam) {
                            eArg.dataType = Types.VARCHAR;
                        }

                        if (eArg2.isParam) {
                            eArg2.dataType = Types.VARCHAR;
                        }
                    }

                    break;
                }
            case SUBTRACT :
            case MULTIPLY :
            case DIVIDE :
                if (eArg.isParam && eArg2.isParam) {
                    throw Trace.error(Trace.UNRESOLVED_PARAMETER_TYPE,
                                      Trace.Expression_resolveTypes2);
                }

                if (isFixedConstant()) {
                    dataType = Column.getCombinedNumberType(eArg.dataType,
                            eArg2.dataType, exprType);
                    valueData = getValue(session, dataType);
                    eArg      = null;
                    eArg2     = null;
                    exprType  = VALUE;
                } else {
                    if (eArg.isParam) {
                        eArg.dataType = eArg2.dataType;
                    } else if (eArg2.isParam) {
                        eArg2.dataType = eArg.dataType;
                    }

                    // fredt@users 20011010 - patch 442993 by fredt
                    dataType = Column.getCombinedNumberType(eArg.dataType,
                            eArg2.dataType, exprType);
                }
                break;

            case CONCAT :
                dataType = Types.VARCHAR;

                if (isFixedConstant()) {
                    valueData = getValue(session, dataType);
                    eArg      = null;
                    eArg2     = null;
                    exprType  = VALUE;
                } else {
                    if (eArg.isParam) {
                        eArg.dataType = Types.VARCHAR;
                    }

                    if (eArg2.isParam) {
                        eArg2.dataType = Types.VARCHAR;
                    }
                }
                break;

            case EQUAL :
            case BIGGER_EQUAL :
            case BIGGER :
            case SMALLER :
            case SMALLER_EQUAL :
            case NOT_EQUAL :
                if (eArg.isParam && eArg2.isParam) {
                    throw Trace.error(Trace.UNRESOLVED_PARAMETER_TYPE,
                                      Trace.Expression_resolveTypes3);
                }

                if (isFixedConditional()) {
                    Boolean result = test(session);

                    if (result == null) {
                        setNull();
                    } else if (result.booleanValue()) {
                        exprType = TRUE;
                    } else {
                        exprType = FALSE;
                    }

                    eArg  = null;
                    eArg2 = null;
                } else if (eArg.isParam) {
                    eArg.dataType = eArg2.dataType == Types.NULL
                                    ? Types.VARCHAR
                                    : eArg2.dataType;

                    if (eArg2.exprType == COLUMN) {
                        eArg.setTableColumnAttributes(eArg2);
                    }
                } else if (eArg2.isParam) {
                    eArg2.dataType = eArg.dataType == Types.NULL
                                     ? Types.VARCHAR
                                     : eArg.dataType;

                    if (eArg.exprType == COLUMN) {
                        eArg2.setTableColumnAttributes(eArg);
                    }
                }

                dataType = Types.BOOLEAN;
                break;

            case LIKE :
                resolveTypeForLike(session);

                dataType = Types.BOOLEAN;
                break;

            case AND : {
                boolean argFixed  = eArg.isFixedConditional();
                boolean arg2Fixed = eArg2.isFixedConditional();
                Boolean arg       = argFixed ? (eArg.test(session))
                                             : null;
                Boolean arg2      = arg2Fixed ? eArg2.test(session)
                                              : null;

                if (argFixed && arg2Fixed) {
                    if (arg == null || arg2 == null) {
                        setNull();
                    } else {
                        exprType = arg.booleanValue() && arg2.booleanValue()
                                   ? TRUE
                                   : FALSE;
                        eArg  = null;
                        eArg2 = null;
                    }
                } else if ((argFixed && !Boolean.TRUE.equals(arg))
                           || (arg2Fixed && !Boolean.TRUE.equals(arg2))) {
                    exprType = FALSE;
                    eArg     = null;
                    eArg2    = null;
                } else {
                    if (eArg.isParam) {
                        eArg.dataType = Types.BOOLEAN;
                    }

                    if (eArg2.isParam) {
                        eArg2.dataType = Types.BOOLEAN;
                    }
                }

                dataType = Types.BOOLEAN;

                break;
            }
            case OR : {
                boolean argFixed  = eArg.isFixedConditional();
                boolean arg2Fixed = eArg2.isFixedConditional();
                Boolean arg       = argFixed ? (eArg.test(session))
                                             : null;
                Boolean arg2      = arg2Fixed ? eArg2.test(session)
                                              : null;

                if (argFixed && arg2Fixed) {
                    if (arg == null || arg2 == null) {
                        setNull();
                    } else {
                        exprType = arg.booleanValue() || arg2.booleanValue()
                                   ? TRUE
                                   : FALSE;
                        eArg  = null;
                        eArg2 = null;
                    }
                } else if ((argFixed && Boolean.TRUE.equals(arg))
                           || (arg2Fixed && Boolean.TRUE.equals(arg2))) {
                    exprType = TRUE;
                    eArg     = null;
                    eArg2    = null;
                } else {
                    if (eArg.isParam) {
                        eArg.dataType = Types.BOOLEAN;
                    }

                    if (eArg2.isParam) {
                        eArg2.dataType = Types.BOOLEAN;
                    }
                }

                dataType = Types.BOOLEAN;

                break;
            }
            case IS_NULL :
                if (isFixedConditional()) {
                    exprType = Boolean.TRUE.equals(test(session)) ? TRUE
                                                                  : FALSE;
                    eArg     = null;
                } else if (eArg.dataType == Types.NULL) {
                    eArg.dataType = Types.VARCHAR;
                }

                dataType = Types.BOOLEAN;
                break;

            case NOT :
                if (isFixedConditional()) {
                   
