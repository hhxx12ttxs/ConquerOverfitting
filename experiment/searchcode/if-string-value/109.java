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

import org.junit.*;

import org.rimudb.*;
import org.rimudb.exception.*;
import org.rimudb.testdb.*;


public class GenericQueryToObjectTests {
	private CompoundDatabase cdb;

    @Before
    public void setUp() throws Exception {
		// Connect to the database
		cdb = new CompoundDatabase("/testconfig/genericsinglevaluequerytests-jdbcconfig.xml", true);
		cdb.connect("dbid-1");
		
		// Create the table
		cdb.createTable(OrderTransactionDO.class, true);

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
	}

	/**
	 * Test a query that returns a single object.
	 * 
	 * @throws Exception
	 */
    @Test
    public void testQuery() throws Exception {
		// Create the query
		Database db = cdb.getDatabase(OrderTransactionDO.class);
		String sql = "select \"value\" from {OrderTransaction} where \"orderNr\" = ? and \"name\" = ?";
		GenericQuery query = new GenericQuery(db);
		query.setSQL(sql);
		
		Object parameters[] = new Object[2];
		parameters[0] = new Integer(67890);
		parameters[1] = "test3name";
		String value = (String)query.executeQueryToObject(parameters);
		assertNotNull("value is null", value);
		assertEquals("Incorrect value", "test3value", value);
	}

	/**
	 * Test a query that returns a single object and passes the SQL on the constructor.
	 * 
	 * @throws Exception
	 */
    @Test
    public void testQuerySQLOnConstructor() throws Exception {
		Database db = cdb.getDatabase(OrderTransactionDO.class);
		String sql = "select \"value\" from {OrderTransaction} where \"orderNr\" = ? and \"name\" = ?";
		GenericQuery query = new GenericQuery(db, sql);

		Object parameters[] = new Object[2];
		parameters[0] = new Integer(67890);
		parameters[1] = "test3name";
		String value = (String)query.executeQueryToObject(parameters);
		assertNotNull("value is null", value);
		assertEquals("Incorrect value", "test3value", value);
	}

	/**
	 * Test a query that does not produce any result.
	 * 
	 * @throws Exception
	 */
    @Test
    public void testNoResultQuery() throws Exception {
		// Create the query
		Database db = cdb.getDatabase(OrderTransactionDO.class);
		String sql = "select \"value\" from {OrderTransaction} where \"orderNr\" = ? and \"name\" = ?";
		GenericQuery query = new GenericQuery(db, sql);
		String value = (String)query.executeQueryToObject(9999999, "unknown");
		assertNull("value is not null", value);
	}

	/**
	 * Test passing an invalid SQL statement. A RimuDBException should be thrown.
	 * 
	 * @throws Exception
	 */
    @Test
    public void testInvalidSQLQuery() throws Exception {
		Database db = cdb.getDatabase(OrderTransactionDO.class);
		String sql = "select from from from from where \"orderNr\" = ? and \"name\" = ?";	// Purposely invalid SQL statement
		GenericQuery inlineQuery = new GenericQuery(db, sql);

		Object parameters[] = new Object[2];
		parameters[0] = new Integer(67890);
		parameters[1] = "test3name";
		try {
			String value = (String)inlineQuery.executeQueryToObject(parameters);
			fail("Expected RimuDBException due to invalid SQL statement");
		} catch (RimuDBException e) {
			// Expected
		}
	}

	/**
	 * Test passing a null to the sql parameter. An IllegalArgumentException should be thrown.
	 * 
	 * @throws Exception
	 */
    @Test
    public void testQueryNullSQLOnConstructor() throws Exception {
		Database db = cdb.getDatabase(OrderTransactionDO.class);
		GenericQuery inlineQuery = new GenericQuery(db, null);

		try {
			String value = (String)inlineQuery.executeQueryToObject(67890, "test3name");
			fail("RimuDBException expected");
		} catch (RimuDBException e) {
			assertEquals("SQL statement has not been set", e.getMessage());
		}
	}

	/**
	 * Test a query where the SQL string has not been set
	 * @throws Exception
	 */
    @Test
    public void testQueryUnsetSQL() throws Exception {
		Database db = cdb.getDatabase(OrderTransactionDO.class);
		GenericQuery inlineQuery = new GenericQuery(db);

		try {
			String value = (String)inlineQuery.executeQueryToObject("rs1", "rs2");
			fail("RimuDBException expected");
		} catch (RimuDBException e) {
			assertEquals("SQL statement has not been set", e.getMessage());
		}
	}

}

