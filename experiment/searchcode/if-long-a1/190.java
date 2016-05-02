/*
 * Copyright (c) 2011.  The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hbase.hbql;

import org.apache.expreval.expr.ExpressionTree;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.HConnection;
import org.apache.hadoop.hbase.hbql.client.HConnectionManager;
import org.apache.hadoop.hbase.hbql.impl.InvalidFunctionException;
import org.apache.hadoop.hbase.hbql.impl.InvalidTypeException;
import org.apache.hadoop.hbase.hbql.statement.select.SelectExpressionContext;
import org.apache.hadoop.hbase.hbql.util.TestSupport;
import org.apache.yaoql.ObjectAllTypes;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

public class WhereExpressionsTest extends TestSupport {

    @Test
    public void booleanExpressions() throws HBqlException {
        assertEvalTrue("TRUE");
        assertEvalTrue("TRUE == TRUE");
        assertEvalTrue("TRUE != FALSE");
        assertEvalFalse("NOT TRUE");
        assertEvalFalse("NOT TRUE");
        assertEvalFalse("NOT TRUE");
        assertEvalFalse("NOT(((((TRUE)))))");
        assertEvalTrue("((TRUE))");
        assertEvalTrue("(((((TRUE)))))");
        assertEvalFalse("NOT((NOT(((NOT TRUE)))))");
        assertEvalFalse("FALSE");
        assertEvalTrue("TRUE OR TRUE");
        assertEvalTrue("TRUE OR TRUE OR TRUE");
        assertEvalFalse("FALSE OR FALSE OR FALSE");
        assertEvalFalse("(FALSE OR FALSE OR FALSE)");
        assertEvalFalse("((((FALSE OR FALSE OR FALSE))))" + " OR " + "((((FALSE OR FALSE OR FALSE))))");
        assertEvalTrue("TRUE OR FALSE");
        assertEvalFalse("FALSE OR FALSE");
        assertEvalTrue("TRUE AND TRUE");
        assertEvalFalse("TRUE AND FALSE");
        assertEvalTrue("TRUE OR ((true) or true) OR FALSE");
        assertEvalFalse("(false AND ((true) OR true)) AND TRUE");
        assertEvalTrue("(false AND ((true) OR true)) OR TRUE");
    }

    @Test
    public void booleanParamExpressions() throws HBqlException {

        ExpressionTree tree;

        tree = parseExpr(":test");
        tree.setParameter(":test", Boolean.TRUE);
        assertExpressionEvalTrue(tree);
        tree.setParameter(":test", Boolean.FALSE);
        assertExpressionEvalFalse(tree);

        tree = parseExpr(":test AND :test");
        tree.setParameter(":test", Boolean.TRUE);
        assertExpressionEvalTrue(tree);
        tree.setParameter(":test", Boolean.FALSE);
        assertExpressionEvalFalse(tree);

        tree = parseExpr(":test1 OR :test2");
        tree.setParameter(":test1", Boolean.TRUE);
        tree.setParameter(":test2", Boolean.FALSE);
        assertExpressionEvalTrue(tree);
        tree.setParameter(":test1", Boolean.FALSE);
        assertExpressionEvalFalse(tree);

        tree = parseExpr(":test1");
        assertHasException(tree, InvalidTypeException.class);

        tree = parseExpr(":b1 == :b2");
        tree.setParameter("b1", Boolean.TRUE);
        tree.setParameter("b2", Boolean.TRUE);
        assertExpressionEvalTrue(tree);
        tree.setParameter("b2", Boolean.FALSE);
        assertExpressionEvalFalse(tree);

        tree = parseExpr(":b1 != :b2");
        tree.setParameter("b1", Boolean.TRUE);
        tree.setParameter("b2", Boolean.FALSE);
        assertExpressionEvalTrue(tree);
        tree.setParameter("b2", Boolean.TRUE);
        assertExpressionEvalFalse(tree);

        tree = parseExpr("((((:b1 OR :b1 OR :b1))))" + " OR " + "((((:b1 OR :b1 OR :b1))))");
        tree.setParameter("b1", Boolean.FALSE);
        assertExpressionEvalFalse(tree);

        tree = parseExpr(":b1 OR ((:b1) or :b1) OR :b2");
        tree.setParameter("b1", Boolean.TRUE);
        tree.setParameter("b2", Boolean.FALSE);
        assertExpressionEvalTrue(tree);
    }

    @Test
    public void miscFunctions() throws HBqlException {

        assertEvalTrue("RANDOMDATE() != NOW()");
        assertEvalTrue("RANDOMINTEGER() != 23");
        assertEvalTrue("RANDOMLONG() != 23L");
        assertEvalTrue("RANDOMFLOAT() != 23.9");
        assertEvalTrue("RANDOMDOUBLE() != 23.9");

        assertEvalTrue("substring('val', 0, 2) == 'va'");

        assertEvalTrue("abs(-2) == 2");
        assertEvalTrue("-abs(-2) == -2");
        assertEvalTrue("abs(2) == 2");
        assertEvalTrue("-abs(2) == -2");

        assertTrue(parseNumberExpr("2L").longValue() == 2L);
        assertTrue(parseNumberExpr("2l").longValue() == 2l);
        assertTrue(parseNumberExpr("2.0").doubleValue() == 2.0);
        assertTrue(parseNumberExpr("2.0D").doubleValue() == 2.0D);
        assertTrue(parseNumberExpr("2.0F").floatValue() == 2.0F);
        assertTrue(parseNumberExpr("2.0d").doubleValue() == 2.0d);
        assertTrue(parseNumberExpr("2.0f").floatValue() == 2.0f);

        assertEvalTrue("abs(-2L) == 2L");
        assertEvalTrue("abs(-2.0) == 2.0");
        assertEvalTrue("abs(-2D) == 2D");
        assertEvalTrue("abs(-2D) == 2.0D");
        assertEvalTrue("abs(-2D) == double('2')");

        assertTrue(parseNumberExpr("datetolong(DATE('12/10/94', 'mm/dd/yy'))").longValue() == 758189520000L);

        assertTrue(parseStringExpr("repeat('abc', 3)").equals("abcabcabc"));

        assertTrue(parseNumberExpr("lesser(4, 3)").intValue() == 3);
        assertTrue(parseNumberExpr("lesser(4.0, 3.0)").doubleValue() == 3.0);

        assertTrue(parseNumberExpr("greater(4, 3)").intValue() == 4);
        assertTrue(parseNumberExpr("greater(4.0, 3.0)").doubleValue() == 4.0);
    }

    @Test
    public void numericCompares() throws HBqlException {

        assertEvalTrue("4 < 5");
        assertEvalFalse("4 = 5");
        assertEvalFalse("4 == 5");
        assertEvalTrue("4 != 5");
        assertEvalTrue("4 <> 5");
        assertEvalTrue("4 <= 5");
        assertEvalFalse("4 > 5");
        assertEvalFalse("4 >= 5");
    }

    @Test
    public void numericParamExpressions() throws HBqlException {

        ExpressionTree tree;

        tree = parseExpr(":val1 < :val2");
        tree.setParameter("val1", 4);
        tree.setParameter("val2", 5);
        assertExpressionEvalTrue(tree);
        tree.setParameter(":val2", 3);
        assertExpressionEvalFalse(tree);
    }


    @Test
    public void dateCompares() throws HBqlException {
        assertEvalTrue("NOW() = NOW()");
        assertEvalTrue("NOW() != NOW()-HOUR(1)");
        assertEvalTrue("NOW() > NOW()-DAY(1)");
        assertEvalTrue("NOW()-DAY(1) < NOW()");
        assertEvalTrue("NOW() <= NOW()+DAY(1)");
        assertEvalTrue("NOW()+DAY(1) >= NOW()");
        assertEvalTrue("NOW() < Date('12/21/2020', 'mm/dd/yyyy')");
        assertEvalTrue("NOW() BETWEEN NOW()-DAY(1) AND NOW()+DAY(1)");
        assertEvalTrue("NOW() IN (NOW()-DAY(1), NOW(), NOW()+DAY(1), Date('12/21/2020', 'mm/dd/yyyy'))");
        assertEvalFalse("NOW() IN (NOW()-DAY(1), NOW()+DAY(1), Date('12/21/2020', 'mm/dd/yyyy'))");
        assertEvalTrue("DATE('10/31/94', 'mm/dd/yy') - DAY(1) = DATE('10/30/94', 'mm/dd/yy')");
        assertEvalTrue("DATE('10/31/94', 'mm/dd/yy') - DAY(2) < DATE('10/30/94', 'mm/dd/yy')");
        assertEvalFalse("DATE('10/31/94', 'mm/dd/yy') - DAY(1) < DATE('10/30/94', 'mm/dd/yy')");
        assertEvalTrue("DATE('10/31/1994', 'mm/dd/yyyy') - DAY(1) - MILLI(1) < DATE('10/31/1994', 'mm/dd/yyyy')  - DAY(1) ");
        assertEvalTrue("DATE('10/31/1994', 'mm/dd/yyyy') - DAY(1) - MILLI(1) = DATE('10/30/1994', 'mm/dd/yyyy')  - MILLI(1) ");

        assertEvalTrue("NOW()+DAY(3) = NOW()+DAY(6)-DAY(3)");

        assertEvalTrue("NOW() < MAXDATE()");
        assertEvalTrue("NOW() > MINDATE()");
        assertEvalTrue("NOW() BETWEEN MINDATE() AND MAXDATE()");
    }

    @Test
    public void dateParamCompares() throws HBqlException {
        ExpressionTree tree;

        tree = parseExpr("NOW() - DAY(1) = :d1");
        tree.setParameter("d1", new Date());
        assertExpressionEvalFalse(tree);

        tree = parseExpr("NOW() - DAY(1) < :d1");
        tree.setParameter("d1", new Date());
        assertExpressionEvalTrue(tree);
    }

    @Test
    public void stringCompares() throws HBqlException {

        assertEvalTrue("'aaa' = 'aaa'");
        assertEvalFalse("'aaa' != 'aaa'");
        assertEvalFalse("'aaa' <> 'aaa'");
        assertEvalFalse("'aaa' = 'bbb'");
        assertEvalTrue("'aaa' <= 'bbb'");
        assertEvalTrue("'bbb' <= 'bbb'");
        assertEvalFalse("'bbb' <= 'aaa'");
        assertEvalFalse("'bbb' > 'bbb'");
        assertEvalTrue("'bbb' > 'aaa'");
        assertEvalTrue("'bbb' >= 'aaa'");
        assertEvalTrue("'aaa' >= 'aaa'");
    }

    @Test
    public void stringParamCompares() throws HBqlException {

        ExpressionTree tree;

        tree = parseExpr("'aaa' = 'a'+:s1");
        tree.setParameter("s1", "aa");
        assertExpressionEvalTrue(tree);

        tree = parseExpr("'aaa' = 'a'+:s1");
        tree.setParameter("s1", 1);
        assertHasException(tree, InvalidTypeException.class);
    }

    @Test
    public void nullCompares() throws HBqlException {

        assertEvalTrue("NULL IS NULL");
        assertEvalFalse("NULL IS NOT NULL");
    }

    @Test
    public void nullParamCompares() throws HBqlException {

        ExpressionTree tree;

        tree = parseExpr(":a IS NULL");
        tree.setParameter("a", null);
        assertExpressionEvalTrue(tree);
        tree.setParameter("a", "val");
        assertExpressionEvalFalse(tree);

        tree = parseExpr(":a IS NOT NULL");
        tree.setParameter("a", "vall");
        assertExpressionEvalTrue(tree);
        tree.setParameter("a", 3);
        assertExpressionEvalTrue(tree);
        tree.setParameter("a", new Date());
        assertExpressionEvalTrue(tree);
        tree.setParameter("a", Boolean.TRUE);
        assertExpressionEvalTrue(tree);
        tree.setParameter("a", (Boolean)null);
        assertExpressionEvalFalse(tree);
        tree.setParameter("a", null);
        assertExpressionEvalFalse(tree);
    }

    @Test
    public void numericCalculations() throws HBqlException {

        assertEvalTrue("-9 = -9");
        assertEvalFalse("-9 = -8");
        assertEvalTrue("9 = 9");
        assertEvalTrue("((4 + 5) = 9)");
        assertEvalTrue("(9) = 9");
        assertEvalTrue("(4 + 5) = 9");
        assertEvalFalse("(4 + 5) = 8");
        assertEvalTrue("(4 + 5 + 10 + 10 - 20) = 9");
        assertEvalFalse("(4 + 5 + 10 + 10 - 20) != 9");

        assertEvalTrue("(4 * 5) = 20");
        assertEvalTrue("(40 % 6) = 4");
        assertEvalFalse("(40 % 6) = 3");

        assertTrue(parseNumberExpr("1-2-3-4").intValue() == (1 - 2 - 3 - 4));
        assertTrue(parseNumberExpr("(2-2)-2").intValue() == ((2 - 2) - 2));
        assertTrue(parseNumberExpr("2-(2-2)").intValue() == (2 - (2 - 2)));
        assertTrue(parseNumberExpr("2-2-2").intValue() == (2 - 2 - 2));
        assertTrue(parseNumberExpr("2*3-4*(((20/5)))+2-(2-3-4*3)").intValue()
                   == (2 * 3 - 4 * (((20 / 5))) + 2 - (2 - 3 - 4 * 3)));
        assertTrue(parseNumberExpr("2-(-4)").intValue() == (2 - (-4)));
        assertTrue(parseNumberExpr("(7-4)+2").intValue() == ((7 - 4) + 2));
        assertTrue(parseNumberExpr("7-(4+2)").intValue() == (7 - (4 + 2)));

        assertTrue(parseNumberExpr("((((4+3)*(2-1))*(3/1))-((2+5)*(1+1)))").intValue()
                   == (((4 + 3) * (2 - 1)) * (3 / 1)) - ((2 + 5) * (1 + 1)));

        assertTrue(parseNumberExpr("((2+4)*(9-2))").intValue() == ((2 + 4) * (9 - 2)));
        assertTrue(parseNumberExpr("(((4+3)*(2-1))*(3/1))").intValue() == (((4 + 3) * (2 - 1)) * (3 / 1)));

        assertEvalTrue("DOUBLE('23.0') > 12.3");
    }

    @Test
    public void numericParamCalculations() throws HBqlException {

        ExpressionTree tree;

        tree = parseExpr(":a = :b");
        tree.setParameter("a", 8);
        tree.setParameter("b", 8);
        assertExpressionEvalTrue(tree);

        tree = parseExpr("(-1*:a) = :b");
        tree.setParameter("a", 8);
        tree.setParameter("b", -8);
        assertExpressionEvalTrue(tree);

        tree = parseExpr("(-1*-1*:a) = :b");
        tree.setParameter("a", 8);
        tree.setParameter("b", 8);
        assertExpressionEvalTrue(tree);

        tree = parseExpr("(:a + :a + :a + :a - :a) = :b");
        tree.setParameter("a", 5);
        tree.setParameter("b", 15);
        assertExpressionEvalTrue(tree);

        tree = parseExpr("(:a % :b) = :c");
        tree.setParameter("a", 40);
        tree.setParameter("b", 6);
        tree.setParameter("c", 4);
        assertExpressionEvalTrue(tree);
    }


    @Test
    public void booleanFunctions() throws HBqlException {

        assertEvalTrue("'abc' CONTAINS 'b'");
        assertEvalFalse("'abc' CONTAINS 'n'");

        assertEvalTrue("'a' IN ('a', 'b')");
        assertEvalFalse("'a' NOT IN ('a', 'b')");
        assertEvalTrue("NOT 'a' NOT IN ('a', 'b')");
        assertEvalFalse("'a' IN ('d', 'b')");
        assertEvalTrue("'a' NOT IN ('d', 'b')");

        HConnection connection = HConnectionManager.newConnection();
        final ObjectAllTypes obj = new ObjectAllTypes("aaabbb", 3, "aaab");

        assertReflectionEvalTrue(obj, "keyval CONTAINS 'ab'");
        assertReflectionEvalFalse(obj, "keyval CONTAINS 'ba'");
        assertReflectionEvalFalse(obj, "'asasas' CONTAINS stringValue");
        assertReflectionEvalTrue(obj, "'xxaaabxx' CONTAINS stringValue");
        assertReflectionEvalTrue(obj, "keyval CONTAINS stringValue");
        assertReflectionEvalTrue(obj, "keyval+'zz' CONTAINS stringValue+'bbz'");
        assertReflectionEvalFalse(obj, "NOT(keyval+'zz' CONTAINS stringValue+'bbz')");
        assertReflectionEvalFalse(obj, "NOT ((('asasas' NOT CONTAINS stringValue)))");
        assertReflectionEvalTrue(obj, "NOT(keyval+'zz' NOT CONTAINS stringValue+'bbz')");

        // Defines Mappings
        AnnotationAllTypesTest.setupMapping(connection);

        final AnnotatedAllTypes annoObj = new AnnotatedAllTypes("aaabbb", 3, "aaab");

        assertAnnotationEvalTrue(connection, annoObj, "keyval CONTAINS 'ab'");
        assertAnnotationEvalFalse(connection, annoObj, "keyval CONTAINS 'ba'");
        assertAnnotationEvalFalse(connection, annoObj, "'asasas' CONTAINS stringValue");
        assertAnnotationEvalTrue(connection, annoObj, "'xxaaabxx' CONTAINS stringValue");
        assertAnnotationEvalTrue(connection, annoObj, "keyval CONTAINS stringValue");
        assertAnnotationEvalTrue(connection, annoObj, "keyval+'zz' CONTAINS stringValue+'bbz'");
        assertAnnotationEvalFalse(connection, annoObj, "NOT(keyval+'zz' CONTAINS stringValue+'bbz')");
    }

    @Test
    public void booleanParamFunctions() throws HBqlException {

        HConnection connection = HConnectionManager.newConnection();
        ExpressionTree tree;

        tree = parseExpr(":a CONTAINS :b");
        tree.setParameter("a", "abc");
        tree.setParameter("b", "b");
        assertExpressionEvalTrue(tree);

        tree.setParameter("b", "z");
        assertExpressionEvalFalse(tree);

        final ObjectAllTypes obj = new ObjectAllTypes("aaabbb", 3, "aaab");

        tree = parseReflectionExpr(obj, "keyval CONTAINS :a");
        tree.setParameter("a", "ab");
        assertExpressionEvalTrue(obj, tree);
        tree.setParameter("a", "ba");
        assertEvalFalse(obj, tree);

        tree = parseExpr(":a IN (:b, :c)");
        tree.setParameter("a", "a");
        tree.setParameter("b", "b");
        tree.setParameter("c", "a");
        assertExpressionEvalTrue(tree);

        tree = parseExpr(":a IN (:b)");
        tree.setParameter("a", "a");
        tree.setParameter("b", Arrays.asList("a", "b", "c"));
        assertExpressionEvalTrue(tree);

        // Test for list where scalar is required
        tree.setParameter("a", Arrays.asList("a", "b", "c"));
        assertHasException(tree, InvalidTypeException.class);
    }

    @Test
    public void invalidFunction() throws HBqlException {
        assertHasException("zzz('dd') == 'dd'", InvalidFunctionException.class);
    }

    @Test
    public void caseTests() throws HBqlException {

        assertEvalTrue("CASE WHEN TRUE THEN TRUE END");
        assertEvalTrue("CASE WHEN 1=3 THEN FALSE ELSE TRUE END");
        assertEvalTrue("CASE " +
                       "WHEN 1=3 THEN FALSE " +
                       "WHEN 1=3 THEN FALSE " +
                       "WHEN 3=3 THEN TRUE " +
                       "END");
        assertEvalTrue("CASE " +
                       "WHEN 1=3 THEN FALSE " +
                       "WHEN 1=3 THEN FALSE " +
                       "WHEN 3=3 THEN TRUE " +
                       "ELSE TRUE END");

        assertHasException("CASE WHEN 3 THEN TRUE END", InvalidTypeException.class);

        assertEvalTrue("4 = CASE WHEN TRUE THEN 4 END");
        assertEvalTrue("'4' = CASE WHEN TRUE THEN '4' END");
        assertEvalTrue("TRUE = CASE WHEN TRUE THEN TRUE END");
        assertEvalTrue("NOW() = CASE WHEN TRUE THEN NOW() END");
    }

    @Test
    public void numericFunctions() throws HBqlException {

        assertEvalTrue("3 between 2 AND 5");
        assertEvalTrue("3 between (1+1) AND (3+2)");
        assertEvalTrue("3 between (1+1) AND (3+2)");

        assertEvalTrue("3 in (2,3,4)");
        assertEvalFalse("3 in (1+1,1+3,4)");
        assertEvalTrue("3 in (1+1,1+2,4)");
        assertEvalFalse("3 NOT in (1+1,1+2,4)");
        assertEvalFalse("3 NOT in (1+1,1+2,4)");
        assertEvalTrue("3 = IF true THEN 3 ELSE 2 END");
        assertEvalFalse("3 = IF false THEN 3 else 2 END");
        assertEvalTrue("2 = IF false THEN 3 else 2 END");

        assertEvalTrue("LENGTH('abc') = 3");
        assertEvalTrue("LENGTH('') = 0");

        assertEvalTrue("INDEXOF('abc', 'b') = 1");
        assertEvalTrue("INDEXOF('abc', 'v') = (-1)");

        assertEvalTrue("REPLACE('abc', 'a', 'bb') = 'bb'+'b'+'c'");
    }

    @Test
    public void stringFunctions() throws HBqlException {

        assertEvalTrue("'bbb' between 'aaa' AND 'ccc'");
        assertEvalTrue("'bbb' between 'aaa' AND 'ccc'");
        assertEvalTrue("'bbb' between 'bbb' AND 'ccc'");
        assertEvalFalse("'bbb' between 'ccc' AND 'ddd'");
        assertEvalTrue("('bbb' between 'bbb' AND 'ccc') AND ('fff' between 'eee' AND 'ggg')");
        assertEvalTrue("('bbb' between 'bbb' AND 'ccc') OR ('fff' between 'eee' AND 'ggg')");
        assertEvalFalse("('bbb' not between 'bbb' AND 'ccc') AND ('fff' between 'eee' AND 'ggg')");
        assertEvalTrue("'bbb' = LOWER('BBB')");
        assertEvalTrue("'ABABAB' = UPPER(CONCAT('aba', 'bab'))");
        assertEvalTrue("'bbb' = SUBSTRING('BBBbbbAAA', 3, 3)");
        assertEvalTrue("'AAA' = 'A' + 'A' + 'A'");
        assertEvalTrue("'aaa' = LOWER('A' + 'A' + 'A')");

        assertEvalTrue("'000004' = zeropad(4, 6)");
    }

    @Test
    public void stringParamFunctions() throws HBqlException {

        ExpressionTree tree;

        tree = parseExpr(":a between :b AND :c");
        tree.setParameter("a", "bbb");
        tree.setParameter("b", "aaa");
        tree.setParameter("c", "ccc");
        assertExpressionEvalTrue(tree);
    }

    @Test
    public void regexFunctions() throws HBqlException {

        HConnection connection = HConnectionManager.newConnection();
        AnnotationAllTypesTest.setupMapping(connection);
        final AnnotatedAllTypes obj = new AnnotatedAllTypes("aaa", 3, "aaab");

        assertEvalTrue("'abc' like 'abc'");
        assertEvalFalse("'abc' not like 'abc'");
        assertEvalTrue("'aaaaab' like 'a*b'");
        assertEvalTrue("'aaaaa' + 'b' like 'a*b'");
        assertEvalTrue("('aaaaa' + 'b') like 'a*b'");
        assertAnnotationEvalTrue(connection, obj, "stringValue + 'b' like 'a*bb'");
        assertAnnotationEvalTrue(connection, obj, "(((((stringValue + 'b'))))) like 'a*bb'");
    }

    @Test
    public void objectFunctions() throws HBqlException {

        HConnection connection = HConnectionManager.newConnection();
        AnnotationAllTypesTest.setupMapping(connection);
        final AnnotatedAllTypes obj = new AnnotatedAllTypes("aaa", 3, "bbb");

        assertAnnotationEvalTrue(connection, obj, "stringValue between 'aaa' AND 'ccc'");
        assertAnnotationEvalTrue(connection, obj, "stringValue between 'aaa' AND 'ccc' AND stringValue between 'aaa' AND 'ccc'");
        assertAnnotationEvalTrue(connection, obj, "stringValue between 'bbb' AND 'ccc'");
        assertAnnotationEvalFalse(connection, obj, "stringValue between 'ccc' AND 'ddd'");
        assertAnnotationEvalTrue(connection, obj, "('bbb' between stringValue AND 'ccc') AND ('fff' between 'eee' AND 'ggg')");

        assertAnnotationEvalTrue(connection, obj, "intValue between 2 AND 5");
        assertAnnotationEvalTrue(connection, obj, "intValue between (1+1) AND (intValue+2)");
        assertAnnotationEvalFalse(connection, obj, "stringValue IN ('v2', 'v0', 'v999')");
        assertAnnotationEvalTrue(connection, obj, "'v19' = 'v19'");
        assertAnnotationEvalFalse(connection, obj, "'v19'= stringValue");
        assertAnnotationEvalFalse(connection, obj, "stringValue = 'v19'");
        assertAnnotationEvalFalse(connection, obj, "stringValue = 'v19' OR stringValue IN ('v2', 'v0', 'v999')");
        assertAnnotationEvalTrue(connection, obj, "stringValue IS NOT NULL");
        assertAnnotationEvalFalse(connection, obj, "stringValue IS NULL");
    }

    @Test
    public void columnLookups() throws HBqlException {

        // assertExprColumnsMatchTrue("{fam1:col1 int, fam2:col2 int} TRUE");
        // assertExprColumnsMatchFalse("TRUE", "intValue");
        assertExprColumnsMatchTrue("{intValue  int, int2  integer} intValue between 2 AND 5", "intValue");
        assertInvalidInput("{xintValue  int} xintValue between 2 AND 5", "intValue");
        assertExprColumnsMatchTrue("{a1 date,a2  date} a1 < a2", "a1", "a2");
        assertExprColumnsMatchFalse("{a1 int, a2 int, d1 int, k3 int} a1 < a2 OR d1 > k3", "a1", "a2");
        assertExprColumnsMatchTrue("{a1 date, a2 date, d1 date, k3  date} a1 < a2 OR d1 > k3", "a1", "a2", "d1", "k3");
    }

    @Test
    public void intervalExpressions() throws HBqlException {
        assertEvalTrue("NOW() < NOW()+YEAR(1)");
        assertEvalTrue("NOW() = NOW()+YEAR(1)-YEAR(1)");
        assertEvalTrue("NOW()+YEAR(2) > NOW()+YEAR(1)");

        assertEvalTrue("NOW() < NOW()+WEEK(1)");
        assertEvalTrue("YEAR(2) = WEEK(52*2)");
        assertEvalTrue("NOW()+YEAR(2) = NOW()+WEEK(52*2)");

        assertEvalTrue("NOW()+YEAR(2) = NOW()+WEEK(52)+DAY(364)");

        assertEvalTrue("NOW() BETWEEN NOW()-DAY(1) AND NOW()+DAY(1)");
        assertEvalTrue("NOW() between NOW()-DAY(1) AND NOW()+DAY(1)");
        assertEvalFalse("NOW() BETWEEN NOW()+DAY(1) AND NOW()+DAY(1)");
    }

    @Test
    public void intervalParamExpressions() throws HBqlException {

        ExpressionTree tree;

        tree = parseExpr("NOW() < NOW()+MINUTE(:a)");
        tree.setParameter("a", 1);
        assertExpressionEvalTrue(tree);

        tree = parseExpr("NOW()+YEAR(:a) = NOW()+WEEK(:b)+DAY(:c)");
        tree.setParameter("a", 2);
        tree.setParameter("b", 52);
        tree.setParameter("c", 364);
        assertExpressionEvalTrue(tree);
    }

    @Test
    public void dateTypes() throws HBqlException {

        assertEvalTrue("DATETOLONG(NOW()) + DATETOLONG(DAY(1)) = DATETOLONG(NOW()+DAY(1))");
        assertEvalTrue("LONGTODATE(DATETOLONG(NOW()) + DATETOLONG(DAY(1))) = LONGTODATE(DATETOLONG(NOW()+DAY(1)))");
        assertEvalTrue("NOW() < LONGTODATE(DATETOLONG(NOW()) + DATETOLONG(MINUTE(1)))");
        assertEvalTrue("NOW() < NOW()+ MINUTE(1)");
    }

    @Test
    public void numericTypes() throws HBqlException {

        assertTypeAndValue("4", Integer.class, 4);
        assertTypeAndValue("4L", Long.class, 4L);
        assertTypeAndValue("4.33", Double.class, 4.33);

        assertTypeAndValue("SHORT('4')", Short.class, (short)4);
        assertTypeAndValue("INTEGER('4')", Integer.class, 4);
        assertTypeAndValue("LONG('4')", Long.class, 4L);
        assertTypeAndValue("FLOAT('4.32')", Float.class, (float)4.32);
        assertTypeAndValue("DOUBLE('4.32')", Double.class, 4.32);

        assertTypeAndValue("SHORT('4') + SHORT('5')", Short.class, Short.valueOf("9"));
        assertTypeAndValue("LONG('4') + LONG('5')", Long.class, 9L);
        assertTypeAndValue("SHORT('4') + LONG('5')", Long.class, 9L);

        assertTypeAndValue("FLOAT('1.0') * IF (TRUE) THEN SHORT('4') + LONG('5') ELSE 10 END", Float.class, (float)9.0);
        assertTypeAndValue("FLOAT('1.0') * IF (FALSE) THEN SHORT('4') + LONG('5') ELSE Integer('10') END", Float.class, (float)10.0);

        assertTypeAndValue("5.0+ FLOAT('4.0') + LONG('5')", Double.class, 14.0);

        SelectExpressionContext elem = parseSelectElement(":a + :b");

        elem.setParameter("a", (short)2);
        elem.setParameter("b", (short)4);
        assertTypeAndValue(elem, Short.class, (short)6);

        elem.setParameter("a", (short)2);
        elem.setParameter("b", (int)4);
        assertTypeAndValue(elem, Integer.class, (int)6);

        elem.setParameter("a", (int)2);
        elem.setParameter("b", (int)4);
        assertTypeAndValue(elem, Integer.class, (int)6);

        elem.setParameter("a", (long)2);
        elem.setParameter("b", (long)4);
        assertTypeAndValue(elem, Long.class, (long)6);

        elem.setParameter("a", (long)2);
        elem.setParameter("b", (float)4);
        assertTypeAndValue(elem, Float.class, (float)6);

        elem.setParameter("a", (float)2.0);
        elem.setParameter("b", (float)4.0);
        assertTypeAndValue(elem, Float.class, (float)6.0);

        elem.setParameter("a", (double)2.0);
        elem.setParameter("b", (double)4.0);
        assertTypeAndValue(elem, Double.class, (double)6.0);

        elem.setParameter("a", "aa");
        elem.setParameter("b", "bb");
        assertTypeAndValue(elem, String.class, "aabb");
    }
}


