<<<<<<< HEAD
/*
 * Copyright 1999-2011 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.druid.sql.visitor;

import static com.alibaba.druid.sql.visitor.SQLEvalVisitor.EVAL_EXPR;
import static com.alibaba.druid.sql.visitor.SQLEvalVisitor.EVAL_VALUE;
import static com.alibaba.druid.sql.visitor.SQLEvalVisitor.EVAL_VALUE_NULL;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.alibaba.druid.DruidRuntimeException;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLBetweenExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLNullExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumericLiteralExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLUnaryExpr;
import com.alibaba.druid.sql.ast.expr.SQLUnaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.dialect.db2.visitor.DB2EvalVisitor;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlEvalVisitorImpl;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleEvalVisitor;
import com.alibaba.druid.sql.dialect.postgresql.visitor.PGEvalVisitor;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerEvalVisitor;
import com.alibaba.druid.sql.visitor.functions.Ascii;
import com.alibaba.druid.sql.visitor.functions.Bin;
import com.alibaba.druid.sql.visitor.functions.BitLength;
import com.alibaba.druid.sql.visitor.functions.Char;
import com.alibaba.druid.sql.visitor.functions.Concat;
import com.alibaba.druid.sql.visitor.functions.Elt;
import com.alibaba.druid.sql.visitor.functions.Function;
import com.alibaba.druid.sql.visitor.functions.Greatest;
import com.alibaba.druid.sql.visitor.functions.Hex;
import com.alibaba.druid.sql.visitor.functions.If;
import com.alibaba.druid.sql.visitor.functions.Insert;
import com.alibaba.druid.sql.visitor.functions.Instr;
import com.alibaba.druid.sql.visitor.functions.Isnull;
import com.alibaba.druid.sql.visitor.functions.Lcase;
import com.alibaba.druid.sql.visitor.functions.Least;
import com.alibaba.druid.sql.visitor.functions.Left;
import com.alibaba.druid.sql.visitor.functions.Length;
import com.alibaba.druid.sql.visitor.functions.Locate;
import com.alibaba.druid.sql.visitor.functions.Lpad;
import com.alibaba.druid.sql.visitor.functions.Ltrim;
import com.alibaba.druid.sql.visitor.functions.Now;
import com.alibaba.druid.sql.visitor.functions.Reverse;
import com.alibaba.druid.sql.visitor.functions.Right;
import com.alibaba.druid.sql.visitor.functions.Substring;
import com.alibaba.druid.sql.visitor.functions.Trim;
import com.alibaba.druid.sql.visitor.functions.Ucase;
import com.alibaba.druid.sql.visitor.functions.Unhex;
import com.alibaba.druid.util.JdbcUtils;
import com.alibaba.druid.wall.spi.WallVisitorUtils;
import com.alibaba.druid.wall.spi.WallVisitorUtils.WallConditionContext;
import com.alibaba.druid.wall.spi.WallVisitorUtils.WallSelectQueryContext;

public class SQLEvalVisitorUtils {

    private static Map<String, Function> functions = new HashMap<String, Function>();

    static {
        registerBaseFunctions();
    }

    public static Object evalExpr(String dbType, String expr, Object... parameters) {
        SQLExpr sqlExpr = SQLUtils.toSQLExpr(expr, dbType);
        return eval(dbType, sqlExpr, parameters);
    }

    public static Object evalExpr(String dbType, String expr, List<Object> parameters) {
        SQLExpr sqlExpr = SQLUtils.toSQLExpr(expr);
        return eval(dbType, sqlExpr, parameters);
    }

    public static Object eval(String dbType, SQLObject sqlObject, Object... parameters) {
        Object value = eval(dbType, sqlObject, Arrays.asList(parameters));

        if (value == EVAL_VALUE_NULL) {
            value = null;
        }

        return value;
    }

    public static Object getValue(SQLObject sqlObject) {
        if (sqlObject instanceof SQLNumericLiteralExpr) {
            return ((SQLNumericLiteralExpr) sqlObject).getNumber();
        }

        return sqlObject.getAttributes().get(EVAL_VALUE);
    }

    public static Object eval(String dbType, SQLObject sqlObject, List<Object> parameters) {
        return eval(dbType, sqlObject, parameters, true);
    }

    public static Object eval(String dbType, SQLObject sqlObject, List<Object> parameters, boolean throwError) {
        SQLEvalVisitor visitor = createEvalVisitor(dbType);
        visitor.setParameters(parameters);
        sqlObject.accept(visitor);

        Object value = getValue(sqlObject);
        if (value == null) {
            if (throwError && !sqlObject.getAttributes().containsKey(EVAL_VALUE)) {
                throw new DruidRuntimeException("eval error : " + SQLUtils.toSQLString(sqlObject, dbType));
            }
        }

        return value;
    }

    public static SQLEvalVisitor createEvalVisitor(String dbType) {
        if (JdbcUtils.MYSQL.equals(dbType)) {
            return new MySqlEvalVisitorImpl();
        }

        if (JdbcUtils.MARIADB.equals(dbType)) {
            return new MySqlEvalVisitorImpl();
        }

        if (JdbcUtils.H2.equals(dbType)) {
            return new MySqlEvalVisitorImpl();
        }

        if (JdbcUtils.ORACLE.equals(dbType) || JdbcUtils.ALI_ORACLE.equals(dbType)) {
            return new OracleEvalVisitor();
        }

        if (JdbcUtils.POSTGRESQL.equals(dbType)) {
            return new PGEvalVisitor();
        }

        if (JdbcUtils.SQL_SERVER.equals(dbType) || JdbcUtils.JTDS.equals(dbType)) {
            return new SQLServerEvalVisitor();
        }

        if (JdbcUtils.DB2.equals(dbType)) {
            return new DB2EvalVisitor();
        }

        return new SQLEvalVisitorImpl();
    }

    static void registerBaseFunctions() {
        functions.put("now", Now.instance);
        functions.put("concat", Concat.instance);
        functions.put("concat_ws", Concat.instance);
        functions.put("ascii", Ascii.instance);
        functions.put("bin", Bin.instance);
        functions.put("bit_length", BitLength.instance);
        functions.put("insert", Insert.instance);
        functions.put("instr", Instr.instance);
        functions.put("char", Char.instance);
        functions.put("elt", Elt.instance);
        functions.put("left", Left.instance);
        functions.put("locate", Locate.instance);
        functions.put("lpad", Lpad.instance);
        functions.put("ltrim", Ltrim.instance);
        functions.put("mid", Substring.instance);
        functions.put("substring", Substring.instance);
        functions.put("right", Right.instance);
        functions.put("reverse", Reverse.instance);
        functions.put("len", Length.instance);
        functions.put("length", Length.instance);
        functions.put("char_length", Length.instance);
        functions.put("character_length", Length.instance);
        functions.put("trim", Trim.instance);
        functions.put("ucase", Ucase.instance);
        functions.put("upper", Ucase.instance);
        functions.put("ucase", Lcase.instance);
        functions.put("lower", Lcase.instance);
        functions.put("hex", Hex.instance);
        functions.put("unhex", Unhex.instance);
        functions.put("greatest", Greatest.instance);
        functions.put("least", Least.instance);
        functions.put("isnull", Isnull.instance);
        functions.put("if", If.instance);
    }

    public static boolean visit(SQLEvalVisitor visitor, SQLMethodInvokeExpr x) {
        String methodName = x.getMethodName().toLowerCase();

        Function function = functions.get(methodName);

        if (function != null) {
            Object result = function.eval(visitor, x);

            if (result != SQLEvalVisitor.EVAL_ERROR) {
                x.getAttributes().put(EVAL_VALUE, result);
            }
            return false;
        }

        if ("mod".equals(methodName)) {
            if (x.getParameters().size() != 2) {
                return false;
            }

            SQLExpr param0 = x.getParameters().get(0);
            SQLExpr param1 = x.getParameters().get(1);
            param0.accept(visitor);
            param1.accept(visitor);

            Object param0Value = param0.getAttributes().get(EVAL_VALUE);
            Object param1Value = param1.getAttributes().get(EVAL_VALUE);
            if (param0Value == null || param1Value == null) {
                return false;
            }

            int intValue0 = castToInteger(param0Value);
            int intValue1 = castToInteger(param1Value);

            int result = intValue0 % intValue1;

            x.putAttribute(EVAL_VALUE, result);
        } else if ("abs".equals(methodName)) {
            if (x.getParameters().size() != 1) {
                return false;
            }

            SQLExpr param0 = x.getParameters().get(0);
            param0.accept(visitor);

            Object paramValue = param0.getAttributes().get(EVAL_VALUE);
            if (paramValue == null) {
                return false;
            }

            Object result;
            if (paramValue instanceof Integer) {
                result = Math.abs(((Integer) paramValue).intValue());
            } else if (paramValue instanceof Long) {
                result = Math.abs(((Long) paramValue).longValue());
            } else {
                result = castToDecimal(paramValue).abs();
            }

            x.putAttribute(EVAL_VALUE, result);
        } else if ("acos".equals(methodName)) {
            if (x.getParameters().size() != 1) {
                return false;
            }

            SQLExpr param0 = x.getParameters().get(0);
            param0.accept(visitor);

            Object paramValue = param0.getAttributes().get(EVAL_VALUE);
            if (paramValue == null) {
                return false;
            }

            double doubleValue = castToDouble(paramValue);
            double result = Math.acos(doubleValue);

            if (Double.isNaN(result)) {
                x.putAttribute(EVAL_VALUE, null);
            } else {
                x.putAttribute(EVAL_VALUE, result);
            }
        } else if ("asin".equals(methodName)) {
            if (x.getParameters().size() != 1) {
                return false;
            }

            SQLExpr param0 = x.getParameters().get(0);
            param0.accept(visitor);

            Object paramValue = param0.getAttributes().get(EVAL_VALUE);
            if (paramValue == null) {
                return false;
            }

            double doubleValue = castToDouble(paramValue);
            double result = Math.asin(doubleValue);

            if (Double.isNaN(result)) {
                x.putAttribute(EVAL_VALUE, null);
            } else {
                x.putAttribute(EVAL_VALUE, result);
            }
        } else if ("atan".equals(methodName)) {
            if (x.getParameters().size() != 1) {
                return false;
            }

            SQLExpr param0 = x.getParameters().get(0);
            param0.accept(visitor);

            Object paramValue = param0.getAttributes().get(EVAL_VALUE);
            if (paramValue == null) {
                return false;
            }

            double doubleValue = castToDouble(paramValue);
            double result = Math.atan(doubleValue);

            if (Double.isNaN(result)) {
                x.putAttribute(EVAL_VALUE, null);
            } else {
                x.putAttribute(EVAL_VALUE, result);
            }
        } else if ("atan2".equals(methodName)) {
            if (x.getParameters().size() != 2) {
                return false;
            }

            SQLExpr param0 = x.getParameters().get(0);
            SQLExpr param1 = x.getParameters().get(1);
            param0.accept(visitor);
            param1.accept(visitor);

            Object param0Value = param0.getAttributes().get(EVAL_VALUE);
            Object param1Value = param1.getAttributes().get(EVAL_VALUE);
            if (param0Value == null || param1Value == null) {
                return false;
            }

            double doubleValue0 = castToDouble(param0Value);
            double doubleValue1 = castToDouble(param1Value);
            double result = Math.atan2(doubleValue0, doubleValue1);

            if (Double.isNaN(result)) {
                x.putAttribute(EVAL_VALUE, null);
            } else {
                x.putAttribute(EVAL_VALUE, result);
            }
        } else if ("ceil".equals(methodName) || "ceiling".equals(methodName)) {
            if (x.getParameters().size() != 1) {
                return false;
            }

            SQLExpr param0 = x.getParameters().get(0);
            param0.accept(visitor);

            Object paramValue = param0.getAttributes().get(EVAL_VALUE);
            if (paramValue == null) {
                return false;
            }

            double doubleValue = castToDouble(paramValue);
            int result = (int) Math.ceil(doubleValue);

            if (Double.isNaN(result)) {
                x.putAttribute(EVAL_VALUE, null);
            } else {
                x.putAttribute(EVAL_VALUE, result);
            }
        } else if ("cos".equals(methodName)) {
            if (x.getParameters().size() != 1) {
                return false;
            }

            SQLExpr param0 = x.getParameters().get(0);
            param0.accept(visitor);

            Object paramValue = param0.getAttributes().get(EVAL_VALUE);
            if (paramValue == null) {
                return false;
            }

            double doubleValue = castToDouble(paramValue);
            double result = Math.cos(doubleValue);

            if (Double.isNaN(result)) {
                x.putAttribute(EVAL_VALUE, null);
            } else {
                x.putAttribute(EVAL_VALUE, result);
            }
        } else if ("sin".equals(methodName)) {
            if (x.getParameters().size() != 1) {
                return false;
            }

            SQLExpr param0 = x.getParameters().get(0);
            param0.accept(visitor);

            Object paramValue = param0.getAttributes().get(EVAL_VALUE);
            if (paramValue == null) {
                return false;
            }

            double doubleValue = castToDouble(paramValue);
            double result = Math.sin(doubleValue);

            if (Double.isNaN(result)) {
                x.putAttribute(EVAL_VALUE, null);
            } else {
                x.putAttribute(EVAL_VALUE, result);
            }
        } else if ("log".equals(methodName)) {
            if (x.getParameters().size() != 1) {
                return false;
            }

            SQLExpr param0 = x.getParameters().get(0);
            param0.accept(visitor);

            Object paramValue = param0.getAttributes().get(EVAL_VALUE);
            if (paramValue == null) {
                return false;
            }

            double doubleValue = castToDouble(paramValue);
            double result = Math.log(doubleValue);

            if (Double.isNaN(result)) {
                x.putAttribute(EVAL_VALUE, null);
            } else {
                x.putAttribute(EVAL_VALUE, result);
            }
        } else if ("log10".equals(methodName)) {
            if (x.getParameters().size() != 1) {
                return false;
            }

            SQLExpr param0 = x.getParameters().get(0);
            param0.accept(visitor);

            Object paramValue = param0.getAttributes().get(EVAL_VALUE);
            if (paramValue == null) {
                return false;
            }

            double doubleValue = castToDouble(paramValue);
            double result = Math.log10(doubleValue);

            if (Double.isNaN(result)) {
                x.putAttribute(EVAL_VALUE, null);
            } else {
                x.putAttribute(EVAL_VALUE, result);
            }
        } else if ("tan".equals(methodName)) {
            if (x.getParameters().size() != 1) {
                return false;
            }

            SQLExpr param0 = x.getParameters().get(0);
            param0.accept(visitor);

            Object paramValue = param0.getAttributes().get(EVAL_VALUE);
            if (paramValue == null) {
                return false;
            }

            double doubleValue = castToDouble(paramValue);
            double result = Math.tan(doubleValue);

            if (Double.isNaN(result)) {
                x.putAttribute(EVAL_VALUE, null);
            } else {
                x.putAttribute(EVAL_VALUE, result);
            }
        } else if ("sqrt".equals(methodName)) {
            if (x.getParameters().size() != 1) {
                return false;
            }

            SQLExpr param0 = x.getParameters().get(0);
            param0.accept(visitor);

            Object paramValue = param0.getAttributes().get(EVAL_VALUE);
            if (paramValue == null) {
                return false;
            }

            double doubleValue = castToDouble(paramValue);
            double result = Math.sqrt(doubleValue);

            if (Double.isNaN(result)) {
                x.putAttribute(EVAL_VALUE, null);
            } else {
                x.putAttribute(EVAL_VALUE, result);
            }
        } else if ("power".equals(methodName) || "pow".equals(methodName)) {
            if (x.getParameters().size() != 2) {
                return false;
            }

            SQLExpr param0 = x.getParameters().get(0);
            SQLExpr param1 = x.getParameters().get(1);
            param0.accept(visitor);
            param1.accept(visitor);

            Object param0Value = param0.getAttributes().get(EVAL_VALUE);
            Object param1Value = param1.getAttributes().get(EVAL_VALUE);
            if (param0Value == null || param1Value == null) {
                return false;
            }

            double doubleValue0 = castToDouble(param0Value);
            double doubleValue1 = castToDouble(param1Value);
            double result = Math.pow(doubleValue0, doubleValue1);

            if (Double.isNaN(result)) {
                x.putAttribute(EVAL_VALUE, null);
            } else {
                x.putAttribute(EVAL_VALUE, result);
            }
        } else if ("pi".equals(methodName)) {
            x.putAttribute(EVAL_VALUE, Math.PI);
        } else if ("rand".equals(methodName)) {
            x.putAttribute(EVAL_VALUE, Math.random());
        } else if ("chr".equals(methodName) && x.getParameters().size() == 1) {
            SQLExpr first = x.getParameters().get(0);
            Object firstResult = getValue(first);
            if (firstResult instanceof Number) {
                int intValue = ((Number) firstResult).intValue();
                char ch = (char) intValue;
                x.putAttribute(EVAL_VALUE, Character.toString(ch));
            }
        } else if ("CURRENT_USER".equals(methodName)) {
            x.putAttribute(EVAL_VALUE, "CURRENT_USER");
        }
        return false;
    }

    public static boolean visit(SQLEvalVisitor visitor, SQLCharExpr x) {
        x.putAttribute(EVAL_VALUE, x.getText());
        return true;
    }

    public static boolean visit(SQLEvalVisitor visitor, SQLBetweenExpr x) {
        x.getTestExpr().accept(visitor);

        if (!x.getTestExpr().getAttributes().containsKey(EVAL_VALUE)) {
            return false;
        }

        Object value = x.getTestExpr().getAttribute(EVAL_VALUE);

        x.getBeginExpr().accept(visitor);
        if (!x.getBeginExpr().getAttributes().containsKey(EVAL_VALUE)) {
            return false;
        }

        Object begin = x.getBeginExpr().getAttribute(EVAL_VALUE);

        if (lt(value, begin)) {
            x.getAttributes().put(EVAL_VALUE, x.isNot() ? true : false);
            return false;
        }

        x.getEndExpr().accept(visitor);
        if (!x.getEndExpr().getAttributes().containsKey(EVAL_VALUE)) {
            return false;
        }

        Object end = x.getEndExpr().getAttribute(EVAL_VALUE);

        if (gt(value, end)) {
            x.getAttributes().put(EVAL_VALUE, x.isNot() ? true : false);
            return false;
        }

        x.getAttributes().put(EVAL_VALUE, x.isNot() ? false : true);
        return false;
    }

    public static boolean visit(SQLEvalVisitor visitor, SQLNullExpr x) {
        x.getAttributes().put(EVAL_VALUE, EVAL_VALUE_NULL);
        return false;
    }

    public static boolean visit(SQLEvalVisitor visitor, SQLCaseExpr x) {
        Object value;
        if (x.getValueExpr() != null) {
            x.getValueExpr().accept(visitor);

            if (!x.getValueExpr().getAttributes().containsKey(EVAL_VALUE)) {
                return false;
            }

            value = x.getValueExpr().getAttribute(EVAL_VALUE);
        } else {
            value = null;
        }

        for (SQLCaseExpr.Item item : x.getItems()) {
            item.getConditionExpr().accept(visitor);

            if (!item.getConditionExpr().getAttributes().containsKey(EVAL_VALUE)) {
                return false;
            }

            Object conditionValue = item.getConditionExpr().getAttribute(EVAL_VALUE);

            if (eq(value, conditionValue)) {
                item.getValueExpr().accept(visitor);

                if (item.getValueExpr().getAttributes().containsKey(EVAL_VALUE)) {
                    x.getAttributes().put(EVAL_VALUE, item.getValueExpr().getAttribute(EVAL_VALUE));
                }

                return false;
            }
        }

        if (x.getElseExpr() != null) {
            x.getElseExpr().accept(visitor);

            if (x.getElseExpr().getAttributes().containsKey(EVAL_VALUE)) {
                x.getAttributes().put(EVAL_VALUE, x.getElseExpr().getAttribute(EVAL_VALUE));
            }
        }

        return false;
    }

    public static boolean visit(SQLEvalVisitor visitor, SQLInListExpr x) {
        SQLExpr valueExpr = x.getExpr();
        valueExpr.accept(visitor);
        if (!valueExpr.getAttributes().containsKey(EVAL_VALUE)) {
            return false;
        }
        Object value = valueExpr.getAttribute(EVAL_VALUE);

        for (SQLExpr item : x.getTargetList()) {
            item.accept(visitor);
            if (!item.getAttributes().containsKey(EVAL_VALUE)) {
                return false;
            }
            Object itemValue = item.getAttribute(EVAL_VALUE);
            if (eq(value, itemValue)) {
                x.getAttributes().put(EVAL_VALUE, x.isNot() ? false : true);
                return false;
            }
        }

        x.getAttributes().put(EVAL_VALUE, x.isNot() ? true : false);
        return false;
    }

    public static boolean visit(SQLEvalVisitor visitor, SQLQueryExpr x) {
        if (WallVisitorUtils.isSimpleCountTableSource(null, ((SQLQueryExpr) x).getSubQuery())) {
            x.putAttribute(EVAL_VALUE, 1);
            return false;
        }

        if (x.getSubQuery().getQuery() instanceof SQLSelectQueryBlock) {
            SQLSelectQueryBlock queryBlock = (SQLSelectQueryBlock) x.getSubQuery().getQuery();

            boolean nullFrom = false;
            if (queryBlock.getFrom() == null) {
                nullFrom = true;
            } else if (queryBlock.getFrom() instanceof SQLExprTableSource) {
                SQLExpr expr = ((SQLExprTableSource) queryBlock.getFrom()).getExpr();
                if (expr instanceof SQLIdentifierExpr) {
                    if ("dual".equalsIgnoreCase(((SQLIdentifierExpr) expr).getName())) {
                        nullFrom = true;
                    }
                }
            }

            if (nullFrom) {
                List<Object> row = new ArrayList<Object>(queryBlock.getSelectList().size());
                for (int i = 0; i < queryBlock.getSelectList().size(); ++i) {
                    SQLSelectItem item = queryBlock.getSelectList().get(i);
                    item.getExpr().accept(visitor);
                    Object cell = item.getExpr().getAttribute(EVAL_VALUE);
                    row.add(cell);
                }
                List<List<Object>> rows = new ArrayList<List<Object>>(1);
                rows.add(row);

                Object result = rows;
                queryBlock.putAttribute(EVAL_VALUE, result);
                x.getSubQuery().putAttribute(EVAL_VALUE, result);
                x.putAttribute(EVAL_VALUE, result);

                return false;
            }
        }

        return false;
    }

    public static boolean visit(SQLEvalVisitor visitor, SQLUnaryExpr x) {
        final WallConditionContext wallConditionContext = WallVisitorUtils.getWallConditionContext();
        if (x.getOperator() == SQLUnaryOperator.Compl && wallConditionContext != null) {
            wallConditionContext.setBitwise(true);
        }

        return false;
    }

    public static boolean visit(SQLEvalVisitor visitor, SQLBinaryOpExpr x) {
        SQLExpr left = x.getLeft();
        SQLExpr right = x.getRight();

        // final WallConditionContext old = wallConditionContextLocal.get();

        left.accept(visitor);
        right.accept(visitor);

        final WallConditionContext wallConditionContext = WallVisitorUtils.getWallConditionContext();
        if (x.getOperator() == SQLBinaryOperator.BooleanOr) {
            if (wallConditionContext != null) {
                if (left.getAttribute(EVAL_VALUE) == Boolean.TRUE || right.getAttribute(EVAL_VALUE) == Boolean.TRUE) {
                    wallConditionContext.setPartAlwayTrue(true);
                }
            }
        } else if (x.getOperator() == SQLBinaryOperator.BooleanXor) {
            if (wallConditionContext != null) {
                wallConditionContext.setXor(true);
            }
        } else if (x.getOperator() == SQLBinaryOperator.BitwiseAnd //
                   || x.getOperator() == SQLBinaryOperator.BitwiseNot //
                   || x.getOperator() == SQLBinaryOperator.BitwiseOr //
                   || x.getOperator() == SQLBinaryOperator.BitwiseXor) {
            if (wallConditionContext != null) {
                wallConditionContext.setBitwise(true);
            }
        }

        Object leftValue = left.getAttribute(EVAL_VALUE);
        Object rightValue = right.getAttributes().get(EVAL_VALUE);

        if (x.getOperator() == SQLBinaryOperator.Like) {
            if (isAlwayTrueLikePattern(x.getRight())) {
                final WallSelectQueryContext wallSelectQueryContext = WallVisitorUtils.getWallSelectQueryContext();
                if (wallSelectQueryContext != null) {
                    wallSelectQueryContext.setTrueLike(Boolean.TRUE);
                }
                x.putAttribute(EVAL_VALUE, Boolean.TRUE);
                return false;
            }
        }

        if (x.getOperator() == SQLBinaryOperator.NotLike) {
            if (isAlwayTrueLikePattern(x)) {
                x.putAttribute(EVAL_VALUE, Boolean.FALSE);
                return false;
            }
        }

        boolean leftHasValue = left.getAttributes().containsKey(EVAL_VALUE);
        boolean rightHasValue = right.getAttributes().containsKey(EVAL_VALUE);

        if ((!leftHasValue) && !rightHasValue) {
            SQLExpr leftEvalExpr = (SQLExpr) left.getAttribute(EVAL_EXPR);
            SQLExpr rightEvalExpr = (SQLExpr) right.getAttribute(EVAL_EXPR);

            if (leftEvalExpr != null && leftEvalExpr.equals(rightEvalExpr)) {
                switch (x.getOperator()) {
                    case Like:
                    case Equality:
                    case GreaterThanOrEqual:
                    case LessThanOrEqual:
                    case NotLessThan:
                    case NotGreaterThan:
                        x.putAttribute(EVAL_VALUE, Boolean.TRUE);
                        return false;
                    case NotEqual:
                    case NotLike:
                    case GreaterThan:
                    case LessThan:
                        x.putAttribute(EVAL_VALUE, Boolean.FALSE);
                        return false;
                    default:
                        break;
                }
            }
        }

        if (!leftHasValue) {
            return false;
        }

        if (!rightHasValue) {
            return false;
        }

        if (wallConditionContext != null) {
            wallConditionContext.setConstArithmetic(true);
        }

        leftValue = processValue(leftValue);
        rightValue = processValue(rightValue);

        if (leftValue == null || rightValue == null) {
            return false;
        }

        Object value = null;
        switch (x.getOperator()) {
            case Add:
                value = add(leftValue, rightValue);
                x.putAttribute(EVAL_VALUE, value);
                break;
            case Subtract:
                value = sub(leftValue, rightValue);
                x.putAttribute(EVAL_VALUE, value);
                break;
            case Multiply:
                value = multi(leftValue, rightValue);
                x.putAttribute(EVAL_VALUE, value);
                break;
            case Divide:
                value = div(leftValue, rightValue);
                x.putAttribute(EVAL_VALUE, value);
                break;
            case GreaterThan:
                value = gt(leftValue, rightValue);
                x.putAttribute(EVAL_VALUE, value);
                break;
            case GreaterThanOrEqual:
                value = gteq(leftValue, rightValue);
                x.putAttribute(EVAL_VALUE, value);
                break;
            case LessThan:
                value = lt(leftValue, rightValue);
                x.putAttribute(EVAL_VALUE, value);
                break;
            case LessThanOrEqual:
                value = lteq(leftValue, rightValue);
                x.putAttribute(EVAL_VALUE, value);
                break;
            case Is:
                if (rightValue == EVAL_VALUE_NULL) {
                    if (leftValue != null) {
                        value = (leftValue == EVAL_VALUE_NULL);
                        x.putAttribute(EVAL_VALUE, value);
                        break;
                    }
                }
                break;
            case Equality:
                value = eq(leftValue, rightValue);
                x.putAttribute(EVAL_VALUE, value);
                break;
            case NotEqual:
                value = !eq(leftValue, rightValue);
                x.putAttribute(EVAL_VALUE, value);
                break;
            case IsNot:
                value = !eq(leftValue, rightValue);
                x.putAttribute(EVAL_VALUE, value);
                break;
            case RegExp:
            case RLike: {
                String pattern = castToString(rightValue);
                String input = castToString(left.getAttributes().get(EVAL_VALUE));
                boolean matchResult = Pattern.matches(pattern, input);
                x.putAttribute(EVAL_VALUE, matchResult);
                break;
            }
            case NotRegExp:
            case NotRLike: {
                String pattern = castToString(rightValue);
                String input = castToString(left.getAttributes().get(EVAL_VALUE));
                boolean matchResult = !Pattern.matches(pattern, input);
                x.putAttribute(EVAL_VALUE, matchResult);
                break;
            }
            case Like: {
                String pattern = castToString(rightValue);
                String input = castToString(left.getAttributes().get(EVAL_VALUE));
                boolean matchResult = like(input, pattern);
                x.putAttribute(EVAL_VALUE, matchResult);
                break;
            }
            case NotLike: {
                String pattern = castToString(rightValue);
                String input = castToString(left.getAttributes().get(EVAL_VALUE));
                boolean matchResult = !like(input, pattern);
                x.putAttribute(EVAL_VALUE, matchResult);
                break;
            }
            case Concat: {
                String result = leftValue.toString() + rightValue.toString();
                x.putAttribute(EVAL_VALUE, result);
                break;
            }
            default:
                break;
        }

        return false;
    }

    @SuppressWarnings("rawtypes")
    private static Object processValue(Object value) {
        if (value instanceof List) {
            List list = (List) value;
            if (list.size() == 1) {
                return processValue(list.get(0));
            }
        }
        return value;
    }

    private static boolean isAlwayTrueLikePattern(SQLExpr x) {
        if (x instanceof SQLCharExpr) {
            String text = ((SQLCharExpr) x).getText();

            if (text.length() >= 0) {
                for (char ch : text.toCharArray()) {
                    if (ch != '%') {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static boolean visit(SQLEvalVisitor visitor, SQLNumericLiteralExpr x) {
        x.getAttributes().put(EVAL_VALUE, x.getNumber());
        return false;
    }

    public static boolean visit(SQLEvalVisitor visitor, SQLVariantRefExpr x) {
        if (!"?".equals(x.getName())) {
            return false;
        }

        Map<String, Object> attributes = x.getAttributes();

        int varIndex = x.getIndex();

        if (varIndex != -1 && visitor.getParameters().size() > varIndex) {
            boolean containsValue = attributes.containsKey(EVAL_VALUE);
            if (!containsValue) {
                Object value = visitor.getParameters().get(varIndex);
                if (value == null) {
                    value = EVAL_VALUE_NULL;
                }
                attributes.put(EVAL_VALUE, value);
            }
        }

        return false;
    }

    public static Boolean castToBoolean(Object val) {
        if (val == null) {
            return null;
        }

        if (val instanceof Boolean) {
            return (Boolean) val;
        }

        if (val instanceof Number) {
            return ((Number) val).intValue() == 1;
        }

        throw new IllegalArgumentException(val.getClass() + " not supported.");
    }

    public static String castToString(Object val) {
        Object value = val;

        if (value == null) {
            return null;
        }

        return value.toString();
    }

    public static Byte castToByte(Object val) {
        if (val == null) {
            return null;
        }

        if (val instanceof Byte) {
            return (Byte) val;
        }

        if (val instanceof String) {
            return Byte.parseByte((String) val);
        }

        return ((Number) val).byteValue();
    }

    public static Short castToShort(Object val) {
        if (val == null || val == EVAL_VALUE_NULL) {
            return null;
        }

        if (val instanceof Short) {
            return (Short) val;
        }

        if (val instanceof String) {
            return Short.parseShort((String) val);
        }

        return ((Number) val).shortValue();
    }

    @SuppressWarnings("rawtypes")
    public static Integer castToInteger(Object val) {
        if (val == null) {
            return null;
        }

        if (val instanceof Integer) {
            return (Integer) val;
        }

        if (val instanceof String) {
            return Integer.parseInt((String) val);
        }

        if (val instanceof List) {
            List list = (List) val;
            if (list.size() == 1) {
                return castToInteger(list.get(0));
            }
        }

        if (val instanceof Boolean) {
            if (((Boolean) val).booleanValue()) {
                return 1;
            } else {
                return 0;
            }
        }

        return ((Number) val).intValue();
    }

    @SuppressWarnings("rawtypes")
    public static Long castToLong(Object val) {
        if (val == null) {
            return null;
        }

        if (val instanceof Long) {
            return (Long) val;
        }

        if (val instanceof String) {
            return Long.parseLong((String) val);
        }

        if (val instanceof List) {
            List list = (List) val;
            if (list.size() == 1) {
                return castToLong(list.get(0));
            }
        }

        return ((Number) val).longValue();
    }

    public static Float castToFloat(Object val) {
        if (val == null || val == EVAL_VALUE_NULL) {
            return null;
        }

        if (val instanceof Float) {
            return (Float) val;
        }

        return ((Number) val).floatValue();
    }

    public static Double castToDouble(Object val) {
        if (val == null || val == EVAL_VALUE_NULL) {
            return null;
        }

        if (val instanceof Double) {
            return (Double) val;
        }

        return ((Number) val).doubleValue();
    }

    public static BigInteger castToBigInteger(Object val) {
        if (val == null) {
            return null;
        }

        if (val instanceof BigInteger) {
            return (BigInteger) val;
        }

        if (val instanceof String) {
            return new BigInteger((String) val);
        }

        return BigInteger.valueOf(((Number) val).longValue());
    }

    public static Date castToDate(Object val) {
        if (val == null) {
            return null;
        }

        if (val instanceof Date) {
            return (Date) val;
        }

        if (val instanceof Number) {
            return new Date(((Number) val).longValue());
        }

        if (val instanceof String) {
            return castToDate((String) val);
        }

        throw new DruidRuntimeException("can cast to date");
    }

    public static Date castToDate(String text) {
        if (text == null || text.length() == 0) {
            return null;
        }

        String format;

        if (text.length() == "yyyy-MM-dd".length()) {
            format = "yyyy-MM-dd";
        } else {
            format = "yyyy-MM-dd HH:mm:ss";
        }

        try {
            return new SimpleDateFormat(format).parse(text);
        } catch (ParseException e) {
            throw new DruidRuntimeException("format : " + format + ", value : " + text, e);
        }
    }

    public static BigDecimal castToDecimal(Object val) {
        if (val == null) {
            return null;
        }

        if (val instanceof BigDecimal) {
            return (BigDecimal) val;
        }

        if (val instanceof String) {
            return new BigDecimal((String) val);
        }

        if (val instanceof Float) {
            return new BigDecimal((Float) val);
        }

        if (val instanceof Double) {
            return new BigDecimal((Double) val);
        }

        return BigDecimal.valueOf(((Number) val).longValue());
    }

    public static Object div(Object a, Object b) {
        if (a == null || b == null) {
            return null;
        }

        if (a instanceof BigDecimal || b instanceof BigDecimal) {
            return castToDecimal(a).divide(castToDecimal(b));
        }

        if (a instanceof Double || b instanceof Double) {
            Double doubleA = castToDouble(a);
            Double doubleB = castToDouble(b);
            if (doubleA == null || doubleB == null) {
                return null;
            }
            return doubleA / doubleB;
        }

        if (a instanceof Float || b instanceof Float) {
            Float floatA = castToFloat(a);
            Float floatB = castToFloat(b);
            if (floatA == null || floatB == null) {
                return null;
            }
            return floatA / floatB;
        }

        if (a instanceof BigInteger || b instanceof BigInteger) {
            return castToBigInteger(a).divide(castToBigInteger(b));
        }

        if (a instanceof Long || b instanceof Long) {
            return castToLong(a) / castToLong(b);
        }

        if (a instanceof Integer || b instanceof Integer) {
            return castToInteger(a) / castToInteger(b);
        }

        if (a instanceof Short || b instanceof Short) {
            return castToShort(a) / castToShort(b);
        }

        if (a instanceof Byte || b instanceof Byte) {
            return castToByte(a) / castToByte(b);
        }

        throw new IllegalArgumentException(a.getClass() + " and " + b.getClass() + " not supported.");
    }

    public static boolean gt(Object a, Object b) {
        if (a == null) {
            return false;
        }

        if (b == null) {
            return true;
        }

        if (a instanceof String || b instanceof String) {
            return castToString(a).compareTo(castToString(b)) > 0;
        }

        if (a instanceof BigDecimal || b instanceof BigDecimal) {
            return castToDecimal(a).compareTo(castToDecimal(b)) > 0;
        }

        if (a instanceof BigInteger || b instanceof BigInteger) {
            return castToBigInteger(a).compareTo(castToBigInteger(b)) > 0;
        }

        if (a instanceof Long || b instanceof Long) {
            return castToLong(a) > castToLong(b);
        }

        if (a instanceof Integer || b instanceof Integer) {
            return castToInteger(a) > castToInteger(b);
        }

        if (a instanceof Short || b instanceof Short) {
            return castToShort(a) > castToShort(b);
        }

        if (a instanceof Byte || b instanceof Byte) {
            return castToByte(a) > castToByte(b);
        }

        if (a instanceof Date || b instanceof Date) {
            Date d1 = castToDate(a);
            Date d2 = castToDate(b);

            if (d1 == d2) {
                return false;
            }

            if (d1 == null) {
                return false;
            }

            if (d2 == null) {
                return true;
            }

            return d1.compareTo(d2) > 0;
        }

        throw new IllegalArgumentException(a.getClass() + " and " + b.getClass() + " not supported.");
    }

    public static boolean gteq(Object a, Object b) {
        if (eq(a, b)) {
            return true;
        }

        return gt(a, b);
    }

    public static boolean lt(Object a, Object b) {
        if (a == null) {
            return true;
        }

        if (b == null) {
            return false;
        }

        if (a instanceof String || b instanceof String) {
            return (castToString(a)).compareTo(castToString(b)) < 0;
        }

        if (a instanceof BigDecimal || b instanceof BigDecimal) {
            return castToDecimal(a).compareTo(castToDecimal(b)) < 0;
        }

        if (a instanceof BigInteger || b instanceof BigInteger) {
            return castToBigInteger(a).compareTo(castToBigInteger(b)) < 0;
        }

        if (a instanceof Long || b instanceof Long) {
            return castToLong(a) < castToLong(b);
        }

        if (a instanceof Integer || b instanceof Integer) {
            return castToInteger(a) < castToInteger(b);
        }

        if (a instanceof Short || b instanceof Short) {
            return castToShort(a) < castToShort(b);
        }

        if (a instanceof Byte || b instanceof Byte) {
            return castToByte(a) < castToByte(b);
        }

        if (a instanceof Date || b instanceof Date) {
            Date d1 = castToDate(a);
            Date d2 = castToDate(b);

            if (d1 == d2) {
                return false;
            }

            if (d1 == null) {
                return true;
            }

            if (d2 == null) {
                return false;
            }

            return d1.compareTo(d2) < 0;
        }

        throw new IllegalArgumentException(a.getClass() + " and " + b.getClass() + " not supported.");
    }

    public static boolean lteq(Object a, Object b) {
        if (eq(a, b)) {
            return true;
        }

        return lt(a, b);
    }

    public static boolean eq(Object a, Object b) {
        if (a == b) {
            return true;
        }

        if (a == null || b == null) {
            return false;
        }

        if (a.equals(b)) {
            return true;
        }

        if (a instanceof String || b instanceof String) {
            return castToString(a).equals(castToString(b));
        }

        if (a instanceof BigDecimal || b instanceof BigDecimal) {
            return castToDecimal(a).compareTo(castToDecimal(b)) == 0;
        }

        if (a instanceof BigInteger || b instanceof BigInteger) {
            return castToBigInteger(a).compareTo(castToBigInteger(b)) == 0;
        }

        if (a instanceof Long || b instanceof Long) {
            return castToLong(a).equals(castToLong(b));
        }

        if (a instanceof Integer || b instanceof Integer) {
            return castToInteger(a).equals(castToInteger(b));
        }

        if (a instanceof Short || b instanceof Short) {
            return castToShort(a).equals(castToShort(b));
        }

        if (a instanceof Boolean || b instanceof Boolean) {
            return castToBoolean(a).equals(castToBoolean(b));
        }

        if (a instanceof Byte || b instanceof Byte) {
            return castToByte(a).equals(castToByte(b));
        }

        if (a instanceof Date || b instanceof Date) {
            Date d1 = castToDate(a);
            Date d2 = castToDate(b);

            if (d1 == d2) {
                return true;
            }

            if (d1 == null || d2 == null) {
                return false;
            }

            return d1.equals(d2);
        }

        throw new IllegalArgumentException(a.getClass() + " and " + b.getClass() + " not supported.");
    }

    public static Object add(Object a, Object b) {
        if (a == null) {
            return b;
        }

        if (b == null) {
            return a;
        }

        if (a == EVAL_VALUE_NULL || b == EVAL_VALUE_NULL) {
            return EVAL_VALUE_NULL;
        }

        if (a instanceof BigDecimal || b instanceof BigDecimal) {
            return castToDecimal(a).add(castToDecimal(b));
        }

        if (a instanceof BigInteger || b instanceof BigInteger) {
            return castToBigInteger(a).add(castToBigInteger(b));
        }

        if (a instanceof Double || b instanceof Double) {
            return castToDouble(a) + castToDouble(b);
        }

        if (a instanceof Float || b instanceof Float) {
            return castToFloat(a) + castToFloat(b);
        }

        if (a instanceof Long || b instanceof Long) {
            return castToLong(a) + castToLong(b);
        }

        if (a instanceof Integer || b instanceof Integer) {
            return castToInteger(a) + castToInteger(b);
        }

        if (a instanceof Short || b instanceof Short) {
            return castToShort(a) + castToShort(b);
        }

        if (a instanceof Boolean || b instanceof Boolean) {
            int aI = 0, bI = 0;
            if (castToBoolean(a)) aI = 1;
            if (castToBoolean(b)) bI = 1;
            return aI + bI;
        }

        if (a instanceof Byte || b instanceof Byte) {
            return castToByte(a) + castToByte(b);
        }

        if (a instanceof String || b instanceof String) {
            return castToString(a) + castToString(b);
        }

        throw new IllegalArgumentException(a.getClass() + " and " + b.getClass() + " not supported.");
    }

    public static Object sub(Object a, Object b) {
        if (a == null) {
            return null;
        }

        if (b == null) {
            return a;
        }

        if (a instanceof Date || b instanceof Date) {
            return SQLEvalVisitor.EVAL_ERROR;
        }

        if (a instanceof BigDecimal || b instanceof BigDecimal) {
            return castToDecimal(a).subtract(castToDecimal(b));
        }

        if (a instanceof BigInteger || b instanceof BigInteger) {
            return castToBigInteger(a).subtract(castToBigInteger(b));
        }

        if (a instanceof Double || b instanceof Double) {
            return castToDouble(a) - castToDouble(b);
        }

        if (a instanceof Float || b instanceof Float) {
            return castToFloat(a) - castToFloat(b);
        }

        if (a instanceof Long || b instanceof Long) {
            return castToLong(a) - castToLong(b);
        }

        if (a instanceof Integer || b instanceof Integer) {
            return castToInteger(a) - castToInteger(b);
        }

        if (a instanceof Short || b instanceof Short) {
            return castToShort(a) - castToShort(b);
        }

        if (a instanceof Boolean || b instanceof Boolean) {
            int aI = 0, bI = 0;
            if (castToBoolean(a)) aI = 1;
            if (castToBoolean(b)) bI = 1;
            return aI - bI;
        }

        if (a instanceof Byte || b instanceof Byte) {
            return castToByte(a) - castToByte(b);
        }

        if (a instanceof String && b instanceof String) {
            return castToLong(a) - castToLong(b);
        }

        // return SQLEvalVisitor.EVAL_ERROR;
        throw new IllegalArgumentException(a.getClass() + " and " + b.getClass() + " not supported.");
    }

    public static Object multi(Object a, Object b) {
        if (a == null || b == null) {
            return null;
        }

        if (a instanceof BigDecimal || b instanceof BigDecimal) {
            return castToDecimal(a).multiply(castToDecimal(b));
        }

        if (a instanceof BigInteger || b instanceof BigInteger) {
            return castToBigInteger(a).multiply(castToBigInteger(b));
        }

        if (a instanceof Long || b instanceof Long) {
            return castToLong(a) * castToLong(b);
        }

        if (a instanceof Integer || b instanceof Integer) {
            return castToInteger(a) * castToInteger(b);
        }

        if (a instanceof Short || b instanceof Short) {
            Short shortA = castToShort(a);
            Short shortB = castToShort(b);

            if (shortA == null || shortB == null) {
                return null;
            }

            return shortA * shortB;
        }

        if (a instanceof Byte || b instanceof Byte) {
            return castToByte(a) * castToByte(b);
        }

        throw new IllegalArgumentException(a.getClass() + " and " + b.getClass() + " not supported.");
    }

    public static boolean like(String input, String pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("pattern is null");
        }

        StringBuilder regexprBuilder = new StringBuilder(pattern.length() + 4);

        final int STAT_NOTSET = 0;
        final int STAT_RANGE = 1;
        final int STAT_LITERAL = 2;

        int stat = STAT_NOTSET;

        int blockStart = -1;
        for (int i = 0; i < pattern.length(); ++i) {
            char ch = pattern.charAt(i);

            if (stat == STAT_LITERAL //
                && (ch == '%' || ch == '_' || ch == '[')) {
                String block = pattern.substring(blockStart, i);
                regexprBuilder.append("\\Q");
                regexprBuilder.append(block);
                regexprBuilder.append("\\E");
                blockStart = -1;
                stat = STAT_NOTSET;
            }

            if (ch == '%') {
                regexprBuilder.append(".*");
            } else if (ch == '_') {
                regexprBuilder.append('.');
            } else if (ch == '[') {
                if (stat == STAT_RANGE) {
                    throw new IllegalArgumentException("illegal pattern : " + pattern);
                }
                stat = STAT_RANGE;
                blockStart = i;
            } else if (ch == ']') {
                if (stat != STAT_RANGE) {
                    throw new IllegalArgumentException("illegal pattern : " + pattern);
                }
                String block = pattern.substring(blockStart, i + 1);
                regexprBuilder.append(block);

                blockStart = -1;
            } else {
                if (stat == STAT_NOTSET) {
                    stat = STAT_LITERAL;
                    blockStart = i;
                }

                if (stat == STAT_LITERAL && i == pattern.length() - 1) {
                    String block = pattern.substring(blockStart, i + 1);
                    regexprBuilder.append("\\Q");
                    regexprBuilder.append(block);
                    regexprBuilder.append("\\E");
                }
            }
        }
        if ("%".equals(pattern) || "%%".equals(pattern)) {
            return true;
        }

        String regexpr = regexprBuilder.toString();
        return Pattern.matches(regexpr, input);
    }

    public static boolean visit(SQLEvalVisitor visitor, SQLIdentifierExpr x) {
        x.putAttribute(EVAL_EXPR, x);
        return false;
    }
}
=======
/**
 * Autogenerated by Thrift
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package com.flaptor.indextank.rpc;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.thrift.*;
import org.apache.thrift.async.*;
import org.apache.thrift.meta_data.*;
import org.apache.thrift.transport.*;
import org.apache.thrift.protocol.*;

@SuppressWarnings("all") public class WorkerLoadStats implements TBase<WorkerLoadStats, WorkerLoadStats._Fields>, java.io.Serializable, Cloneable {
  private static final TStruct STRUCT_DESC = new TStruct("WorkerLoadStats");

  private static final TField ONE_MINUTE_LOAD_FIELD_DESC = new TField("one_minute_load", TType.DOUBLE, (short)1);
  private static final TField FIVE_MINUTES_LOAD_FIELD_DESC = new TField("five_minutes_load", TType.DOUBLE, (short)2);
  private static final TField FIFTEEN_MINUTES_LOAD_FIELD_DESC = new TField("fifteen_minutes_load", TType.DOUBLE, (short)3);

  private double one_minute_load;
  private double five_minutes_load;
  private double fifteen_minutes_load;

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements TFieldIdEnum {
    ONE_MINUTE_LOAD((short)1, "one_minute_load"),
    FIVE_MINUTES_LOAD((short)2, "five_minutes_load"),
    FIFTEEN_MINUTES_LOAD((short)3, "fifteen_minutes_load");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // ONE_MINUTE_LOAD
          return ONE_MINUTE_LOAD;
        case 2: // FIVE_MINUTES_LOAD
          return FIVE_MINUTES_LOAD;
        case 3: // FIFTEEN_MINUTES_LOAD
          return FIFTEEN_MINUTES_LOAD;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __ONE_MINUTE_LOAD_ISSET_ID = 0;
  private static final int __FIVE_MINUTES_LOAD_ISSET_ID = 1;
  private static final int __FIFTEEN_MINUTES_LOAD_ISSET_ID = 2;
  private BitSet __isset_bit_vector = new BitSet(3);

  public static final Map<_Fields, FieldMetaData> metaDataMap;
  static {
    Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.ONE_MINUTE_LOAD, new FieldMetaData("one_minute_load", TFieldRequirementType.DEFAULT, 
        new FieldValueMetaData(TType.DOUBLE)));
    tmpMap.put(_Fields.FIVE_MINUTES_LOAD, new FieldMetaData("five_minutes_load", TFieldRequirementType.DEFAULT, 
        new FieldValueMetaData(TType.DOUBLE)));
    tmpMap.put(_Fields.FIFTEEN_MINUTES_LOAD, new FieldMetaData("fifteen_minutes_load", TFieldRequirementType.DEFAULT, 
        new FieldValueMetaData(TType.DOUBLE)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    FieldMetaData.addStructMetaDataMap(WorkerLoadStats.class, metaDataMap);
  }

  public WorkerLoadStats() {
  }

  public WorkerLoadStats(
    double one_minute_load,
    double five_minutes_load,
    double fifteen_minutes_load)
  {
    this();
    this.one_minute_load = one_minute_load;
    set_one_minute_load_isSet(true);
    this.five_minutes_load = five_minutes_load;
    set_five_minutes_load_isSet(true);
    this.fifteen_minutes_load = fifteen_minutes_load;
    set_fifteen_minutes_load_isSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public WorkerLoadStats(WorkerLoadStats other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    this.one_minute_load = other.one_minute_load;
    this.five_minutes_load = other.five_minutes_load;
    this.fifteen_minutes_load = other.fifteen_minutes_load;
  }

  public WorkerLoadStats deepCopy() {
    return new WorkerLoadStats(this);
  }

  @Override
  public void clear() {
    set_one_minute_load_isSet(false);
    this.one_minute_load = 0.0;
    set_five_minutes_load_isSet(false);
    this.five_minutes_load = 0.0;
    set_fifteen_minutes_load_isSet(false);
    this.fifteen_minutes_load = 0.0;
  }

  public double get_one_minute_load() {
    return this.one_minute_load;
  }

  public WorkerLoadStats set_one_minute_load(double one_minute_load) {
    this.one_minute_load = one_minute_load;
    set_one_minute_load_isSet(true);
    return this;
  }

  public void unset_one_minute_load() {
    __isset_bit_vector.clear(__ONE_MINUTE_LOAD_ISSET_ID);
  }

  /** Returns true if field one_minute_load is set (has been asigned a value) and false otherwise */
  public boolean is_set_one_minute_load() {
    return __isset_bit_vector.get(__ONE_MINUTE_LOAD_ISSET_ID);
  }

  public void set_one_minute_load_isSet(boolean value) {
    __isset_bit_vector.set(__ONE_MINUTE_LOAD_ISSET_ID, value);
  }

  public double get_five_minutes_load() {
    return this.five_minutes_load;
  }

  public WorkerLoadStats set_five_minutes_load(double five_minutes_load) {
    this.five_minutes_load = five_minutes_load;
    set_five_minutes_load_isSet(true);
    return this;
  }

  public void unset_five_minutes_load() {
    __isset_bit_vector.clear(__FIVE_MINUTES_LOAD_ISSET_ID);
  }

  /** Returns true if field five_minutes_load is set (has been asigned a value) and false otherwise */
  public boolean is_set_five_minutes_load() {
    return __isset_bit_vector.get(__FIVE_MINUTES_LOAD_ISSET_ID);
  }

  public void set_five_minutes_load_isSet(boolean value) {
    __isset_bit_vector.set(__FIVE_MINUTES_LOAD_ISSET_ID, value);
  }

  public double get_fifteen_minutes_load() {
    return this.fifteen_minutes_load;
  }

  public WorkerLoadStats set_fifteen_minutes_load(double fifteen_minutes_load) {
    this.fifteen_minutes_load = fifteen_minutes_load;
    set_fifteen_minutes_load_isSet(true);
    return this;
  }

  public void unset_fifteen_minutes_load() {
    __isset_bit_vector.clear(__FIFTEEN_MINUTES_LOAD_ISSET_ID);
  }

  /** Returns true if field fifteen_minutes_load is set (has been asigned a value) and false otherwise */
  public boolean is_set_fifteen_minutes_load() {
    return __isset_bit_vector.get(__FIFTEEN_MINUTES_LOAD_ISSET_ID);
  }

  public void set_fifteen_minutes_load_isSet(boolean value) {
    __isset_bit_vector.set(__FIFTEEN_MINUTES_LOAD_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case ONE_MINUTE_LOAD:
      if (value == null) {
        unset_one_minute_load();
      } else {
        set_one_minute_load((Double)value);
      }
      break;

    case FIVE_MINUTES_LOAD:
      if (value == null) {
        unset_five_minutes_load();
      } else {
        set_five_minutes_load((Double)value);
      }
      break;

    case FIFTEEN_MINUTES_LOAD:
      if (value == null) {
        unset_fifteen_minutes_load();
      } else {
        set_fifteen_minutes_load((Double)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case ONE_MINUTE_LOAD:
      return new Double(get_one_minute_load());

    case FIVE_MINUTES_LOAD:
      return new Double(get_five_minutes_load());

    case FIFTEEN_MINUTES_LOAD:
      return new Double(get_fifteen_minutes_load());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been asigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case ONE_MINUTE_LOAD:
      return is_set_one_minute_load();
    case FIVE_MINUTES_LOAD:
      return is_set_five_minutes_load();
    case FIFTEEN_MINUTES_LOAD:
      return is_set_fifteen_minutes_load();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof WorkerLoadStats)
      return this.equals((WorkerLoadStats)that);
    return false;
  }

  public boolean equals(WorkerLoadStats that) {
    if (that == null)
      return false;

    boolean this_present_one_minute_load = true;
    boolean that_present_one_minute_load = true;
    if (this_present_one_minute_load || that_present_one_minute_load) {
      if (!(this_present_one_minute_load && that_present_one_minute_load))
        return false;
      if (this.one_minute_load != that.one_minute_load)
        return false;
    }

    boolean this_present_five_minutes_load = true;
    boolean that_present_five_minutes_load = true;
    if (this_present_five_minutes_load || that_present_five_minutes_load) {
      if (!(this_present_five_minutes_load && that_present_five_minutes_load))
        return false;
      if (this.five_minutes_load != that.five_minutes_load)
        return false;
    }

    boolean this_present_fifteen_minutes_load = true;
    boolean that_present_fifteen_minutes_load = true;
    if (this_present_fifteen_minutes_load || that_present_fifteen_minutes_load) {
      if (!(this_present_fifteen_minutes_load && that_present_fifteen_minutes_load))
        return false;
      if (this.fifteen_minutes_load != that.fifteen_minutes_load)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(WorkerLoadStats other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    WorkerLoadStats typedOther = (WorkerLoadStats)other;

    lastComparison = Boolean.valueOf(is_set_one_minute_load()).compareTo(typedOther.is_set_one_minute_load());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_one_minute_load()) {
      lastComparison = TBaseHelper.compareTo(this.one_minute_load, typedOther.one_minute_load);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(is_set_five_minutes_load()).compareTo(typedOther.is_set_five_minutes_load());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_five_minutes_load()) {
      lastComparison = TBaseHelper.compareTo(this.five_minutes_load, typedOther.five_minutes_load);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(is_set_fifteen_minutes_load()).compareTo(typedOther.is_set_fifteen_minutes_load());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_fifteen_minutes_load()) {
      lastComparison = TBaseHelper.compareTo(this.fifteen_minutes_load, typedOther.fifteen_minutes_load);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(TProtocol iprot) throws TException {
    TField field;
    iprot.readStructBegin();
    while (true)
    {
      field = iprot.readFieldBegin();
      if (field.type == TType.STOP) { 
        break;
      }
      switch (field.id) {
        case 1: // ONE_MINUTE_LOAD
          if (field.type == TType.DOUBLE) {
            this.one_minute_load = iprot.readDouble();
            set_one_minute_load_isSet(true);
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 2: // FIVE_MINUTES_LOAD
          if (field.type == TType.DOUBLE) {
            this.five_minutes_load = iprot.readDouble();
            set_five_minutes_load_isSet(true);
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 3: // FIFTEEN_MINUTES_LOAD
          if (field.type == TType.DOUBLE) {
            this.fifteen_minutes_load = iprot.readDouble();
            set_fifteen_minutes_load_isSet(true);
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        default:
          TProtocolUtil.skip(iprot, field.type);
      }
      iprot.readFieldEnd();
    }
    iprot.readStructEnd();

    // check for required fields of primitive type, which can't be checked in the validate method
    validate();
  }

  public void write(TProtocol oprot) throws TException {
    validate();

    oprot.writeStructBegin(STRUCT_DESC);
    oprot.writeFieldBegin(ONE_MINUTE_LOAD_FIELD_DESC);
    oprot.writeDouble(this.one_minute_load);
    oprot.writeFieldEnd();
    oprot.writeFieldBegin(FIVE_MINUTES_LOAD_FIELD_DESC);
    oprot.writeDouble(this.five_minutes_load);
    oprot.writeFieldEnd();
    oprot.writeFieldBegin(FIFTEEN_MINUTES_LOAD_FIELD_DESC);
    oprot.writeDouble(this.fifteen_minutes_load);
    oprot.writeFieldEnd();
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("WorkerLoadStats(");
    boolean first = true;

    sb.append("one_minute_load:");
    sb.append(this.one_minute_load);
    first = false;
    if (!first) sb.append(", ");
    sb.append("five_minutes_load:");
    sb.append(this.five_minutes_load);
    first = false;
    if (!first) sb.append(", ");
    sb.append("fifteen_minutes_load:");
    sb.append(this.fifteen_minutes_load);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws TException {
    // check for required fields
  }

}

>>>>>>> 76aa07461566a5976980e6696204781271955163

