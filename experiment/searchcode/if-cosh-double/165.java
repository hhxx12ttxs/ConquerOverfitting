/*
 * Copyright 2010 Spolecne s.r.o. (www.spoledge.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.spoledge.audao.parser.gql;

import java.util.Arrays;
import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import static com.spoledge.audao.parser.gql.impl.ParserUtils.argDouble;
import com.spoledge.audao.parser.gql.impl.soft.SoftFunction;
import com.spoledge.audao.parser.gql.impl.soft.SoftFunctionFactory;

public class GqlExtDynamicTest extends AbstractSelectDynamicTest {

    private GqlExtDynamic gqld;

    @Before
    public void setUp() {
        super.setUp();
        gqld = new GqlExtDynamic( ds );
    }

    ////////////////////////////////////////////////////////////////////////////
    // Tests
    ////////////////////////////////////////////////////////////////////////////

    @Test 
    public void testExtGeneral03() {
        Entity empVaclav = first( test( 1, "SELECT * FROM kind('Employee') WHERE fullName = 'Vaclav Bartacek'" ));
        test( 1, "SELECT * FROM com.foo.Employee" );
        test( 0, "SELECT * FROM kind('Order')" );
        test( 0, "SELECT * FROM kind('Order') WHERE prop('limit')=1" );
        test( 1, "SELECT * FROM Employee WHERE prop('limit')=1" );
        test( 1, "SELECT * FROM Employee WHERE prop('com.foo')=true" );

        //// not working - why ? bug on devel ?
        //test( 1, "SELECT * FROM kind() WHERE __key__ = :1", empVaclav.getKey());
        //test( 3, "SELECT * FROM kind() WHERE ANCESTOR IS :1", empVaclav.getKey());
        //test( 9, "SELECT * FROM kind()");
    }

    ////////////////////////////////////////////////////////////////////////////
    // Tests
    ////////////////////////////////////////////////////////////////////////////

    @Test 
    public void testExt02() {
        for (Entity ent : executeQuery( "SELECT fullName,position FROM Employee WHERE employeeNumber=3" )) {
            log.debug("testGeneral02(): " + ent);
        };
    }

    @Test 
    public void testExtColumnExpr() {
        for (Entity ent : executeQuery( "SELECT fullName+'_suffix' 'fullName',employeeNumber+1000 'en' FROM Employee WHERE employeeNumber=3" )) {
            log.debug("testExtColumnExpr(): " + ent);
            assertTrue( "suffix", ((String)ent.getProperty( "fullName" )).endsWith( "_suffix" ));
            assertTrue( "pos", ((Number)ent.getProperty( "en" )).intValue() > 1000);
        };
    }

    /**
     * Found a Bug in distro.
     */
    @Test 
    public void testExtColumnExpr02() {
        executeQuery( "SELECT cos(:1*3.1415/180) as 'cos' FROM dual", 30);
    }


    @Test 
    public void testExtSoftWhere() {
        test( 3, "SELECT * FROM Employee WHERE SOFT employeeNumber=employeeNumber");
        test( 0, "SELECT * FROM Employee WHERE SOFT employeeNumber=employeeNumber+1");
        test( 3, "SELECT * FROM Employee WHERE SOFT employeeNumber=employeeNumber+1-1");
        test( 1, "SELECT * FROM Employee WHERE SOFT SIGN(employeeNumber)=employeeNumber-1");
        test( 3, "SELECT * FROM Preference WHERE SOFT ANCESTOR IS KEY('Employee', 'vasek')");
    }


    ////////////////////////////////////////////////////////////////////////////
    // Tests - DUAL
    ////////////////////////////////////////////////////////////////////////////

    @Test 
    public void testExtDual() { 
        Entity ent = first( test( 1, "SELECT * FROM dual"));
        ent = first( test( 1, "SELECT 1 AS val FROM dual"));
        testProp( ent, "val", 1L );
    }

    ////////////////////////////////////////////////////////////////////////////
    // Tests - functions
    ////////////////////////////////////////////////////////////////////////////

    @Test(expected=RuntimeException.class) 
    public void testExtFuncNonExistent() { 
        test( 0, "SELECT UNEXISTENT(-1) AS val FROM dual");
    }

    @Test 
    public void testExtFuncABS() { 
        Entity ent = first( test( 1, "SELECT ABS(-1) AS val, ABS(-1.1) AS dval FROM dual"));
        testProp( ent, "val", 1L );
        testProp( ent, "dval", 1.1d );
    }

    @Test(expected=RuntimeException.class) 
    public void testExtFuncABSBadCard() { 
        Entity ent = first( test( 1, "SELECT ABS(1,2) AS val FROM dual"));
    }

    @Test(expected=RuntimeException.class) 
    public void testExtFuncABSBadValue() { 
        Entity ent = first( test( 1, "SELECT ABS('bad') AS val FROM dual"));
    }

    @Test 
    public void testExtFuncACOS() { 
        Entity ent = first( test( 1, "SELECT ACOS(1) AS val, ACOS(0.5) AS val2 FROM dual"));
        testProp( ent, "val", Math.acos(1) );
        testProp( ent, "val2", Math.acos(0.5) );
    }

    @Test 
    public void testExtFuncASIN() { 
        Entity ent = first( test( 1, "SELECT ASIN(1) AS val, ASIN(0.5) AS val2 FROM dual"));
        testProp( ent, "val", Math.asin(1) );
        testProp( ent, "val2", Math.asin(0.5) );
    }

    @Test 
    public void testExtFuncATAN() { 
        Entity ent = first( test( 1, "SELECT ATAN(1) AS val, ATAN(0.5) AS val2 FROM dual"));
        testProp( ent, "val", Math.atan(1) );
        testProp( ent, "val2", Math.atan(0.5) );
    }

    @Test 
    public void testExtFuncATAN2() { 
        Entity ent = first( test( 1, "SELECT ATAN2(10,13) AS val, ATAN2(13,10) AS val2 FROM dual"));
        testProp( ent, "val", Math.atan2(10,13) );
        testProp( ent, "val2", Math.atan2(13,10) );
    }

    @Test 
    public void testExtFuncCEIL() { 
        Entity ent = first( test( 1, "SELECT CEIL(1.1) AS val, CEIL(-1.1) AS val2 FROM dual"));
        testProp( ent, "val", (long) Math.ceil(1.1) );
        testProp( ent, "val2", (long) Math.ceil(-1.1) );
    }

    @Test 
    public void testExtFuncCOS() { 
        Entity ent = first( test( 1, "SELECT COS(0) AS val, COS(3.1415/2) AS val2 FROM dual"));
        testProp( ent, "val", Math.cos(0) );
        testProp( ent, "val2", Math.cos(3.1415/2) );
    }

    @Test 
    public void testExtFuncCOSH() { 
        Entity ent = first( test( 1, "SELECT COSH(1) AS val, COSH(0.5) AS val2 FROM dual"));
        testProp( ent, "val", Math.cosh(1) );
        testProp( ent, "val2", Math.cosh(0.5) );
    }

    @Test 
    public void testExtFuncDECODE() { 
        Entity ent = first( test( 1, "SELECT DECODE(1, 2/2, 3, 4) AS val, DECODE(1,2,3) AS nullval FROM dual"));
        testProp( ent, "val", 3L );
        testProp( ent, "nullval", null );
    }

    @Test 
    public void testExtFuncDECODE2() { 
        for (Entity ent : test( 3, "SELECT DECODE(employeeNumber,1,'Vaclav Balcar',2,'Tomas Straka',3,'Vaclav Bartacek') AS val,fullName FROM Employee")) {
            testProp( ent, "val", ent.getProperty("fullName"));
        }
    }

    @Test 
    public void testExtFuncDIV() { 
        Entity ent = first( test( 1, "SELECT 6/4 AS val, 6.0/4 AS val2, 6/4.0 AS val3 FROM dual"));
        testProp( ent, "val", 6L/4 );
        testProp( ent, "val2", 6.0/4);
        testProp( ent, "val3", 6/4.0);
    }

    @Test 
    public void testExtFuncEXP() { 
        Entity ent = first( test( 1, "SELECT EXP(1) AS val, EXP(2) AS val2 FROM dual"));
        testProp( ent, "val", Math.exp(1) );
        testProp( ent, "val2", Math.exp(2) );
    }

    @Test 
    public void testExtFuncFLOOR() { 
        Entity ent = first( test( 1, "SELECT FLOOR(1.1) AS val, FLOOR(-1.1) AS val2 FROM dual"));
        testProp( ent, "val", (long) Math.floor(1.1) );
        testProp( ent, "val2",(long) Math.floor(-1.1) );
    }

    @Test 
    public void testExtFuncGEOPT_LAT() { 
        Entity ent = first( test( 1, "SELECT GEOPT_LAT(geopt(1.234,2.345)) AS val FROM dual"));
        testProp( ent, "val", new Double(1.234f) );
    }

    @Test 
    public void testExtFuncGEOPT_LNG() { 
        Entity ent = first( test( 1, "SELECT GEOPT_LNG(geopt(1.234,2.345)) AS val FROM dual"));
        testProp( ent, "val", new Double(2.345f) );
    }

    @Test 
    public void testExtFuncINSTR() { 
        Entity ent = first( test( 1, "SELECT INSTR('abrakadabra', 'br') AS val, INSTR('abrakadabra', 'br', 2) AS val2, INSTR('abrakadabra', 'br', 2, 2) as val3 FROM dual"));
        testProp( ent, "val", 2L);
        testProp( ent, "val2", 2L);
        testProp( ent, "val3", 9L);
    }

    @Test 
    public void testExtFuncINSTRback() { 
        Entity ent = first( test( 1, "SELECT INSTR('abrakadabra', 'br', -1) AS val, INSTR('abrakadabra', 'br', -1, 2) as val2 FROM dual"));
        testProp( ent, "val", 9L);
        testProp( ent, "val2", 2L);
    }

    @Test 
    public void testExtFuncKEY_ID() { 
        Entity ent = first( test( 1, "SELECT KEY_ID(KEY('test',123)) AS val FROM dual"));
        testProp( ent, "val", 123L );
    }

    @Test 
    public void testExtFuncKEY_NAME() { 
        Entity ent = first( test( 1, "SELECT KEY_NAME(KEY('test','key.name')) AS val FROM dual"));
        testProp( ent, "val", "key.name" );
    }

    @Test 
    public void testExtFuncKEY_PARENT() { 
        Entity ent = first( test( 1, "SELECT KEY_ID(KEY_PARENT(KEY('parent', 456, 'test',123))) AS val FROM dual"));
        testProp( ent, "val", 456L );
    }

    @Test 
    public void testExtFuncKEY_VALUE() { 
        Entity ent = first( test( 1, "SELECT KEY_VALUE(KEY('test',123)) AS val, KEY_VALUE(KEY('test','key.name')) AS sval FROM dual"));
        testProp( ent, "val", 123L );
        testProp( ent, "sval", "key.name" );
    }

    @Test 
    public void testExtFuncLENGTH() { 
        Entity ent = first( test( 1, "SELECT LENGTH('test') AS val, LENGTH(null) AS nullval FROM dual"));
        testProp( ent, "val", 4L );
        testProp( ent, "nullval", null );
    }

    @Test 
    public void testExtFuncLIST() { 
        Entity ent = first( test( 1, "SELECT 3*(4-1) AS val, (1,2) AS val2, LIST() as val3, LIST(1) as val4, LIST(3,4) as val5 FROM dual"));
        testProp( ent, "val", 9L );
        testProp( ent, "val2", Arrays.asList( 1L, 2L ));
        testProp( ent, "val3", Arrays.asList());
        testProp( ent, "val4", Arrays.asList( 1L ));
        testProp( ent, "val5", Arrays.asList( 3L, 4L ));
    }

    @Test 
    public void testExtFuncLIST_JOIN() { 
        Entity ent = first( test( 1, "SELECT LIST_JOIN('|',LIST(1,2,3)) AS val"
                                        + ", LIST_JOIN('|',LIST(1)) AS val2"
                                        + ", LIST_JOIN('|',LIST()) AS val3"
                                        + ", LIST_JOIN('|',null) AS val4"
                                        + ", LIST_JOIN(null,LIST(1,2)) AS val5 FROM dual"));
        testProp( ent, "val", "1|2|3" );
        testProp( ent, "val2", "1" );
        testProp( ent, "val3", "" );
        testProp( ent, "val4", null );
        testProp( ent, "val5", "12" );
    }

    @Test 
    public void testExtFuncLN() { 
        Entity ent = first( test( 1, "SELECT LN(1) AS val, LN(10) AS val2 FROM dual"));
        testProp( ent, "val", Math.log(1) );
        testProp( ent, "val2", Math.log(10) );
    }

    @Test 
    public void testExtFuncLOWER() { 
        Entity ent = first( test( 1, "SELECT LOWER('ABCd') AS val, LOWER(null) AS val2 FROM dual"));
        testProp( ent, "val", "abcd");
        testProp( ent, "val2", null );
    }

    @Test 
    public void testExtFuncMINUS() { 
        Entity ent = first( test( 1, "SELECT 6-4 AS val, 6.0-4 AS val2, 6-4.0 AS val3, -4 AS val4 FROM dual"));
        testProp( ent, "val", 6L-4 );
        testProp( ent, "val2", 6.0-4);
        testProp( ent, "val3", 6-4.0);
        testProp( ent, "val4", -4L);
    }

    @Test 
    public void testExtFuncMINUSDate() { 
        java.util.Date now = new java.util.Date();
        long dayMillis = 24*3600000L;
        Entity ent = first( test( 1, "SELECT :1 - 1 AS val, :1 - 0.5 AS val2 FROM dual", now ));
        testProp( ent, "val", new java.util.Date( now.getTime() - dayMillis));
        testProp( ent, "val2", new java.util.Date( now.getTime() - (long) (0.5*dayMillis)));
    }

    @Test 
    public void testExtFuncMINUSDateDate() { 
        java.util.Date now = new java.util.Date();
        long dayMillis = 24*3600000L;
        java.util.Date before = new java.util.Date(now.getTime()-dayMillis/2);
        Entity ent = first( test( 1, "SELECT :1 - :2 AS val FROM dual", now, before ));
        testProp( ent, "val", (now.getTime() - before.getTime()) / (double) dayMillis);
    }

    @Test 
    public void testExtFuncMOD() { 
        Entity ent = first( test( 1, "SELECT MOD(7,5) AS val FROM dual"));
        testProp( ent, "val", 2L );
    }

    @Test 
    public void testExtFuncMUL() { 
        Entity ent = first( test( 1, "SELECT 6*4 AS val, 6.0*4 AS val2, 6*4.0 AS val3 FROM dual"));
        testProp( ent, "val", 6L*4 );
        testProp( ent, "val2", 6.0*4);
        testProp( ent, "val3", 6*4.0);
    }

    @Test 
    public void testExtFuncMULString() { 
        Entity ent = first( test( 1, "SELECT 'test'*3 AS val FROM dual"));
        testProp( ent, "val", "testtesttest" );
    }

    @Test 
    public void testExtFuncNVL() { 
        Entity ent = first( test( 1, "SELECT NVL(1, 2) AS val, NVL(null,true) AS nullval FROM dual"));
        testProp( ent, "val", 1L );
        testProp( ent, "nullval", true );
    }

    @Test 
    public void testExtFuncNVL2() { 
        Entity ent = first( test( 1, "SELECT NVL2(1, 2, 3) AS val, NVL2(null,true,false) AS nullval FROM dual"));
        testProp( ent, "val", 2L );
        testProp( ent, "nullval", false );
    }

    @Test 
    public void testExtFuncPLUS() { 
        Entity ent = first( test( 1, "SELECT 6+4 AS val, 6.0+4 AS val2, 6+4.0 AS val3 FROM dual"));
        testProp( ent, "val", 6L+4 );
        testProp( ent, "val2", 6.0+4);
        testProp( ent, "val3", 6+4.0);
    }

    @Test 
    public void testExtFuncPLUSString() { 
        Entity ent = first( test( 1, "SELECT 'test-'+true AS val FROM dual"));
        testProp( ent, "val", "test-true" );
    }

    @Test 
    public void testExtFuncPLUSDate() { 
        java.util.Date now = new java.util.Date();
        long dayMillis = 24*3600000L;
        Entity ent = first( test( 1, "SELECT :1 + 1 AS val, :1 + 0.5 AS val2 FROM dual", now ));
        testProp( ent, "val", new java.util.Date( now.getTime() + dayMillis));
        testProp( ent, "val2", new java.util.Date( now.getTime() + (long) (0.5*dayMillis)));
    }

    @Test 
    public void testExtFuncPOWER() { 
        Entity ent = first( test( 1, "SELECT POWER(2,4) AS val, POWER(3.1,4.2) AS val2 FROM dual"));
        testProp( ent, "val", Math.pow(2,4) );
        testProp( ent, "val2", Math.pow(3.1,4.2) );
    }

    @Test 
    public void testExtFuncRAND() { 
        test( 1, "SELECT RAND() AS val FROM dual");
    }

    @Test 
    public void testExtFuncSIGN() { 
        Entity ent = first( test( 1, "SELECT SIGN(-10) AS negval, SIGN(-1.1) AS negdval, SIGN(0) AS val, SIGN(0.0) AS dval, SIGN(10) AS posval, SIGN(1.1) AS posdval FROM dual"));
        testProp( ent, "negval", -1L );
        testProp( ent, "negdval", -1L );
        testProp( ent, "val", 0L );
        testProp( ent, "dval", 0L );
        testProp( ent, "posval", 1L );
        testProp( ent, "posdval", 1L );
    }

    @Test 
    public void testExtFuncSIN() { 
        Entity ent = first( test( 1, "SELECT SIN(0) AS val, SIN(3.1415/2) AS val2 FROM dual"));
        testProp( ent, "val", Math.sin(0) );
        testProp( ent, "val2", Math.sin(3.1415/2) );
    }

    @Test 
    public void testExtFuncSINH() { 
        Entity ent = first( test( 1, "SELECT SINH(0) AS val, SINH(3.1415/2) AS val2 FROM dual"));
        testProp( ent, "val", Math.sinh(0) );
        testProp( ent, "val2", Math.sinh(3.1415/2) );
    }

    @Test 
    public void testExtFuncSQRT() { 
        Entity ent = first( test( 1, "SELECT SQRT(9) AS val, SQRT(999.9) AS val2 FROM dual"));
        testProp( ent, "val", Math.sqrt(9) );
        testProp( ent, "val2", Math.sqrt(999.9) );
    }

    @Test 
    public void testExtFuncSUBSTR() { 
        Entity ent = first( test( 1, "SELECT SUBSTR('ABCDEFG',3,4) AS val, SUBSTR('ABCDEFG',-5,4) AS bval, SUBSTR('ABCDEFG',2) AS cval FROM dual"));
        testProp( ent, "val", "CDEF" );
        testProp( ent, "bval", "CDEF" );
        testProp( ent, "cval", "BCDEFG" );
    }

    @Test 
    public void testExtFuncSYSDATE() { 
        Entity ent = first( test( 1, "SELECT SYSDATE() AS val FROM dual"));
        assertTrue( Math.abs(System.currentTimeMillis() - ((java.util.Date)ent.getProperty("val")).getTime()) < 100);
    }

    @Test 
    public void testExtFuncTAN() { 
        Entity ent = first( test( 1, "SELECT TAN(0) AS val, TAN(3.1415/4) AS val2 FROM dual"));
        testProp( ent, "val", Math.tan(0) );
        testProp( ent, "val2", Math.tan(3.1415/4) );
    }

    @Test 
    public void testExtFuncTANH() { 
        Entity ent = first( test( 1, "SELECT TANH(0) AS val, TANH(3.1415/4) AS val2 FROM dual"));
        testProp( ent, "val", Math.tanh(0) );
        testProp( ent, "val2", Math.tanh(3.1415/4) );
    }

    @Test 
    public void testExtFuncTO_CHAR_date() { 
        Entity ent = first( test( 1, "SELECT TO_CHAR(:1) AS val, TO_CHAR(:1,'yyyy-MM-dd') AS val2 FROM dual", datetime("2010-06-13 16:52:08")));
        testProp( ent, "val", "2010-06-13 16:52:08");
        testProp( ent, "val2", "2010-06-13" );
    }

    @Test 
    public void testExtFuncTO_CHAR_number() { 
        Entity ent = first( test( 1, "SELECT TO_CHAR(:1) AS val, TO_CHAR(:1,'00.00') AS val2 FROM dual", 3.14159265));
        testProp( ent, "val", "3.1416");
        testProp( ent, "val2", "03.14" );
    }

    @Test 
    public void testExtFuncUPPER() { 
        Entity ent = first( test( 1, "SELECT UPPER('Abcd') AS val, UPPER(null) AS val2 FROM dual"));
        testProp( ent, "val", "ABCD");
        testProp( ent, "val2", null );
    }

    ////////////////////////////////////////////////////////////////////////////
    // Tests - SoftFunctionFactory
    ////////////////////////////////////////////////////////////////////////////

    @Test 
    public void testExtFuncMINE() { 
        SoftFunctionFactory.getDefaultFactory().defineSoftFunction( "MINE", new SoftFunction() {
            public Object getFunctionValue( List<Object> args ) {
                double x =  argDouble( args.get(0));
                double y =  argDouble( args.get(1));
                return Math.sqrt( x*x + y*y );
            }
        });
        Entity ent = first( test( 1, "SELECT MINE(3,4) AS val FROM dual"));
        testProp( ent, "val", Math.sqrt( 3d*3 + 4d*4));
    }

    ////////////////////////////////////////////////////////////////////////////
    // Tests - nested queries
    ////////////////////////////////////////////////////////////////////////////

    @Test 
    public void testNested() {
        test( 1, "SELECT * FROM (SELECT * FROM Employee WHERE employeeNumber=1) WHERE SOFT employeeNumber=employeeNumber");
    }

    @Test 
    public void testNestedLimit() {
        test( 2, "SELECT * FROM (SELECT * FROM Employee) WHERE SOFT employeeNumber=employeeNumber LIMIT 2");
    }

    @Test 
    public void testNestedOffset() {
        test( 1, "SELECT * FROM (SELECT * FROM Employee) WHERE SOFT employeeNumber=employeeNumber OFFSET 2");
    }

    ////////////////////////////////////////////////////////////////////////////
    // Tests - IN nested queries
    ////////////////////////////////////////////////////////////////////////////

    @Test 
    public void testInNested() {
        test( 2, "SELECT * FROM Employee WHERE employeeNumber IN (SELECT value/5 'val' FROM Preference WHERE name='limit')");
    }

    ////////////////////////////////////////////////////////////////////////////
    // Tests - IN SOFT nested queries
    ////////////////////////////////////////////////////////////////////////////

    @Test 
    public void testInSoftNested() {
        test( 2, "SELECT * FROM Employee WHERE SOFT employeeNumber IN (SELECT value/5 'val' FROM Preference WHERE name='limit')");
    }

    ////////////////////////////////////////////////////////////////////////////
    // Tests - column names
    ////////////////////////////////////////////////////////////////////////////

    @Test
    public void testColumnNames() {
        testColumnNames( "SELECT * FROM Ble", null );
        testColumnNames( "SELECT __key__ FROM Ble", Arrays.asList("__key__"));
        testColumnNames( "SELECT alfa FROM Ble", Arrays.asList("alfa"));
        testColumnNames( "SELECT alfa,beta FROM Ble", Arrays.asList("alfa", "beta"));

        // __key__ is not in the list:
        testColumnNames( "SELECT alfa,beta,__key__ FROM Ble", Arrays.asList("alfa", "beta"));
        testColumnNames( "SELECT __key__,alfa,beta FROM Ble", Arrays.asList("alfa", "beta"));

        testColumnNames( "SELECT alfa,beta,* FROM Ble", null );
        testColumnNames( "SELECT *,alfa,beta FROM Ble", null );

        testColumnNames( "SELECT * FROM (SELECT alfa,beta FROM Ble)", Arrays.asList("alfa", "beta"));
    }


    ////////////////////////////////////////////////////////////////////////////
    // Tests - insert
    ////////////////////////////////////////////////////////////////////////////

    @Test
    public void testInsertAutoKey() {
        executeUpdate( 1, "INSERT INTO TestInsert (name,value,flag) VALUES ('test_name',234,true)");

        Entity ent = first( test( 1, "SELECT * FROM TestInsert" ));
        assertNotNull( ent );

        testProp( ent, "name", "test_name" );
        testProp( ent, "value", 234L );
        testProp( ent, "flag", true );
    }

    @Test
    public void testInsertManuKey() {
        executeUpdate( 1, "INSERT INTO kind() (__key__,name,value,flag) VALUES (KEY('TestInsert', 'first'),'test_name',234,true)");

        Entity ent = first( test( 1, "SELECT * FROM TestInsert" ));
        assertNotNull( ent );

        assertEquals( "first", ent.getKey().getName());

        testProp( ent, "name", "test_name" );
        testProp( ent, "value", 234L );
        testProp( ent, "flag", true );
    }

    @Test
    public void testInsertUnindexed() {
        executeUpdate( 1, "INSERT INTO TestInsert (UNINDEXED('name'),UNINDEXED('value')) VALUES ('test_name',null)");

        Entity ent = first( test( 1, "SELECT * FROM TestInsert" ));
        assertNotNull( ent );

        testProp( ent, "name", "test_name", true );
        testPropEmpty( ent, "value" );
    }

    @Test
    public void testInsertEmpty() {
        executeUpdate( 1, "INSERT INTO TestInsert (EMPTY('name'),EMPTY('value')) VALUES ('test_name',null)");

        Entity ent = first( test( 1, "SELECT * FROM TestInsert" ));
        assertNotNull( ent );

        testProp( ent, "name", "test_name" );
        testPropEmpty( ent, "value" );
    }


    @Test(expected=RuntimeException.class)
    public void testInsertBadColVals() {
        executeUpdate( 1, "INSERT INTO TestInsert (name,value,flag) VALUES ('test_name',234)");
        fail();
    }

    @Test(expected=RuntimeException.class)
    public void testInsertBadExpr() {
        executeUpdate( 1, "INSERT INTO TestInsert (name,value,flag) VALUES (propName,234,true)");
        fail();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Tests - delete
    ////////////////////////////////////////////////////////////////////////////

    @Test
    public void testDelete01() {
        executeUpdate( 1, "DELETE FROM Employee WHERE employeeNumber=3" );
        test( 2, "SELECT * FROM Employee" );
        executeUpdate( 0, "DELETE FROM Employee WHERE employeeNumber=4" );
        executeUpdate( 2, "DELETE FROM Employee" );
        test( 0, "SELECT * FROM Employee" );
    }


    ////////////////////////////////////////////////////////////////////////////
    // Tests - update
    ////////////////////////////////////////////////////////////////////////////

    @Test
    public void testUpdate01() {
        test( 0, "SELECT * FROM Employee WHERE employeeNumber > 100" );
        executeUpdate( 3, "UPDATE Employee SET employeeNumber=employeeNumber+100" );
        test( 3, "SELECT * FROM Employee WHERE employeeNumber > 100" );
    }

    @Test
    public void testUpdateUnindexed() {
        executeUpdate( 1, "UPDATE Employee SET UNINDEXED('new_prop')=employeeNumber WHERE employeeNumber=1" );
        Entity ent = first(test( 1, "SELECT * FROM Employee WHERE employeeNumber=1" ));
        testProp( ent, "new_prop", ent.getProperty("employeeNumber"), true );
        testProp( ent, "employeeNumber", ent.getProperty("employeeNumber"), false );

        executeUpdate( 3, "UPDATE Employee SET UNINDEXED('employeeNumber')=employeeNumber" );
        ent = first(test( 3, "SELECT * FROM Employee" ));
        testProp( ent, "employeeNumber", ent.getProperty("employeeNumber"), true );

        executeUpdate( 3, "UPDATE Employee SET employeeNumber=employeeNumber" );
        ent = first(test( 3, "SELECT * FROM Employee" ));
        testProp( ent, "employeeNumber", ent.getProperty("employeeNumber"), false );
    }

    @Test
    public void testUpdateEmpty() {
        executeUpdate( 1, "UPDATE Employee SET employeeNumber=null WHERE employeeNumber=1" );
        Entity ent = first(test( 1, "SELECT * FROM Employee WHERE employeeNumber=null" ));
        testProp( ent, "employeeNumber", null );

        executeUpdate( 3, "UPDATE Employee SET EMPTY('employeeNumber')=null" );
        ent = first(test( 3, "SELECT * FROM Employee" ));
        testPropEmpty( ent, "employeeNumber" );
    }


    ////////////////////////////////////////////////////////////////////////////
    // Private
    ////////////////////////////////////////////////////////////////////////////

    protected Iterable<Entity> executeQuery( String gql, Object... params) {
        return gqld.prepare( gql ).executeQuery( params );
    }

    protected void executeUpdate( int expectedCount, String gql, Object... params) {
        assertEquals( "update records count", expectedCount, gqld.prepare( gql ).executeUpdate( params ));
    }


    ////////////////////////////////////////////////////////////////////////////
    // Private
    ////////////////////////////////////////////////////////////////////////////

    private Entity first( Iterable<Entity> iterable ) {
        return iterable.iterator().next();
    }


    private void testPropEmpty( Entity ent, String propName ) {
        assertTrue( "property " + propName + " empty", !ent.hasProperty( propName ));
    }


    private void testProp( Entity ent, String propName, Object expectedVal ) {
        testProp( ent, propName, expectedVal, false );
    }


    private void testProp( Entity ent, String propName, Object expectedVal, boolean isUnindexed ) {
        assertEquals( "property " + propName, expectedVal, ent.getProperty( propName ));
        assertEquals( "property " + propName + " unindexed", isUnindexed, ent.isUnindexedProperty( propName ));
    }

    private void testColumnNames( String gql, List<String> expected ) {
        PreparedGql pgql = gqld.prepare( gql );
        pgql.executeQuery();
        String[] columns = pgql.getColumnNames();

        if (expected == null) { assertEquals( "null", expected, columns ); return; }

        assertNotNull( "not-null", columns );
        assertEquals( "size", expected.size(), columns.length );

        for (int i=0; i < columns.length; i++) {
            assertEquals( "column " + (i+1), expected.get(i), columns[i]);
        }
    }

}

