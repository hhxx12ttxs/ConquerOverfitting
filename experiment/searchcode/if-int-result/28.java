/*
 * Copyright (c) 2008-2011 Simon Ritchie.
 * All rights reserved. 
 * 
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published 
 * by the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program.  If not, see http://www.gnu.org/licenses/>.
 */
package org.rimudb.generic;

import static org.junit.Assert.*;

import java.sql.*;

import org.junit.*;

import org.rimudb.*;
import org.rimudb.exception.*;
import org.rimudb.testdb.*;


public class GenericUpdateTests {
	private CompoundDatabase cdb;

	private class TestGenericUpdateQuery extends GenericUpdate {

		public TestGenericUpdateQuery(Database database) throws RimuDBException {
			super(database);
			setSQL("update {OrderTransaction} set \"value\" = ? where \"orderNr\" = ? and \"name\" = ?");
		}

	}

    @Before
    public void setUp() throws Exception {
		// Connect to the database
		cdb = new CompoundDatabase("/testconfig/genericupdatetests-jdbcconfig.xml", true);
		cdb.connect("dbid-1");
		
		// Create the table
		cdb.createTable(OrderTransactionDO.class, true);
		
		// Clear the table in case it was not removed
		OrderTransactionFinder finder = new OrderTransactionFinder(cdb);
		finder.deleteAll();
		
		OrderTransactionDO orderTransaction = new OrderTransactionDO(cdb);
		orderTransaction.setOrderNr(12345);
		orderTransaction.setName("test1name");
		orderTransaction.setValue("test1value");
		orderTransaction.commit();
		
		orderTransaction = new OrderTransactionDO(cdb);
		orderTransaction.setOrderNr(12345);
		orderTransaction.setName("test2name");
		orderTransaction.setValue("test2value");
		orderTransaction.commit();
		
		orderTransaction = new OrderTransactionDO(cdb);
		orderTransaction.setOrderNr(12345);
		orderTransaction.setName("test3name");
		orderTransaction.setValue("test3value");
		orderTransaction.commit();
		
		orderTransaction = new OrderTransactionDO(cdb);
		orderTransaction.setOrderNr(67890);
		orderTransaction.setName("test3name");
		orderTransaction.setValue("test3value");
		orderTransaction.commit();
		
		orderTransaction = new OrderTransactionDO(cdb);
		orderTransaction.setOrderNr(67890);
		orderTransaction.setName("test4name");
		orderTransaction.setValue("test4value");
		orderTransaction.commit();
		
	}

    @After
    public void tearDown() throws Exception {
		cdb.disconnectAll();
//		Thread.sleep(1000);
	}

    @Test
    public void testQuery() throws Exception {
		TestGenericUpdateQuery query = new TestGenericUpdateQuery(cdb.getDatabase(OrderTransactionDO.class));
		
		Object parameters[] = new Object[3];
		parameters[0] = "test3valueUPDATED";
		parameters[1] = "12345";
		parameters[2] = "test3name";
		int result = query.executeUpdate(parameters);
		assertEquals("Wrong number of records updated", 1, result);
		
		OrderTransactionFinder orderTransactionFinder = new OrderTransactionFinder(cdb);
		WhereList whereList = new WhereList();
		whereList.add_AND_EQ(OrderTransactionDO.F_ORDER_NR, 12345);
		whereList.add_AND_EQ(OrderTransactionDO.F_NAME, "test3name");
		OrderTransactionDO[] transactionDOs = orderTransactionFinder.select(whereList, null, AbstractFinder.ALL_RECORDS);
		assertNotNull("transactionDOs is null", transactionDOs);
		assertEquals("Wrong number of records returned", 1, transactionDOs.length);
		assertEquals("Wrong number of records returned", "test3valueUPDATED", transactionDOs[0].getValue());
	}

    @Test
    public void testInlineQuery() throws Exception {
		Database database = cdb.getDatabase(OrderTransactionDO.class);
		String sql = "update {OrderTransaction} set \"value\" = ? where \"orderNr\" = ? and \"name\" = ?";
		GenericUpdate query = new GenericUpdate(database, sql);
		
		Object parameters[] = new Object[3];
		parameters[0] = "test3valueUPDATED";
		parameters[1] = "12345";
		parameters[2] = "test3name";
		int result = query.executeUpdate(parameters);
		assertEquals("Wrong number of records updated", 1, result);
		
		OrderTransactionFinder orderTransactionFinder = new OrderTransactionFinder(cdb);
		WhereList whereList = new WhereList();
		whereList.add_AND_EQ(OrderTransactionDO.F_ORDER_NR, 12345);
		whereList.add_AND_EQ(OrderTransactionDO.F_NAME, "test3name");
		OrderTransactionDO[] transactionDOs = orderTransactionFinder.select(whereList, null);
		assertNotNull("transactionDOs is null", transactionDOs);
		assertEquals("Wrong number of records returned", 1, transactionDOs.length);
		assertEquals("Wrong number of records returned", "test3valueUPDATED", transactionDOs[0].getValue());
	}

    @Test
    public void testNullSQLQuery() throws Exception {
		Database database = cdb.getDatabase(OrderTransactionDO.class);
		String sql = null;
		GenericUpdate query = new GenericUpdate(database, sql);
		
		Object parameters[] = new Object[3];
		parameters[0] = "test3valueUPDATED";
		parameters[1] = "12345";
		parameters[2] = "test3name";
		int result;
		try {
			result = query.executeUpdate(parameters);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertEquals("SQL statement was not provided on constructor", e.getMessage());
		}
	}

    @Test
    public void testInlineExecuteNullSQLQuery() throws Exception {
		Database db = cdb.getDatabase(OrderTransactionDO.class);
		String sql = null;
		GenericUpdate inlineQuery = new GenericUpdate(db);

		Object parameters[] = null;
		
		Object rsParameters[] = new Object[2];
		rsParameters[0] = "rs1";
		rsParameters[1] = "rs2";
		
		try {
			int result = inlineQuery.executeUpdate(parameters, rsParameters);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
			assertEquals("SQL statement was not provided on constructor", e.getMessage());
		}
	}
	/**
	 * Test passing an invalid SQL statement. A RimuDBException should be thrown.
	 * 
	 * @throws Exception
	 */
    @Test
    public void testInvalidSQLQuery() throws Exception {
		Database db = cdb.getDatabase(OrderTransactionDO.class);
		String sql = "update from from from from where \"orderNr\" = ? and \"name\" = ?";	// Purposely invalid SQL statement
		GenericUpdate inlineQuery = new GenericUpdate(db, sql);

		Object parameters[] = new Object[2];
		parameters[0] = new Integer(67890);
		parameters[1] = "test3name";
		try {
			int result = inlineQuery.executeUpdate(parameters);
			fail("Expected RimuDBException due to invalid SQL statement");
		} catch (RimuDBException e) {
			// Expected
		}
	}

    @Test
    public void testInlineExecuteSQLQuery() throws Exception {
		Database db = cdb.getDatabase(OrderTransactionDO.class);
		String tableName = db.getTable(OrderTransactionDO.class).getTableName();
		String sql = "update {OrderTransaction} set \"value\" = ? where \"orderNr\" = ? and \"name\" = ?";
		GenericUpdate inlineQuery = new GenericUpdate(db, sql);

		Object parameters[] = new Object[3];
		parameters[0] = "test3valueUPDATED";
		parameters[1] = "12345";
		parameters[2] = "test3name";
		int result = inlineQuery.executeUpdate(parameters);
		assertEquals("Wrong number of records updated", 1, result);
		
		OrderTransactionFinder orderTransactionFinder = new OrderTransactionFinder(cdb);
		WhereList whereList = new WhereList();
		whereList.add_AND_EQ(OrderTransactionDO.F_ORDER_NR, 12345);
		whereList.add_AND_EQ(OrderTransactionDO.F_NAME, "test3name");
		OrderTransactionDO[] transactionDOs = orderTransactionFinder.select(whereList, null, AbstractFinder.ALL_RECORDS);
		assertNotNull("transactionDOs is null", transactionDOs);
		assertEquals("Wrong number of records returned", 1, transactionDOs.length);
		assertEquals("Wrong number of records returned", "test3valueUPDATED", transactionDOs[0].getValue());
	}

}

