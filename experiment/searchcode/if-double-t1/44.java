/**
 * Copyright 2011, Pancake Technology, LLC
 * All Rights Reserved.
 */
package com.pancaketech.stardog.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.clarkparsia.stardog.StardogException;
import com.clarkparsia.stardog.api.Connection;
import com.clarkparsia.stardog.api.ConnectionConfiguration;
import com.clarkparsia.stardog.api.Query;
import com.clarkparsia.stardog.security.SecurityUtil;

/**
 * @author Robert Butler
 * 
 */
public class StardogConnectionPoolTest {

	@BeforeClass
	public static void securitySetup() {
		SecurityUtil.setupSingletonSecurityManager();
	}

	/**
	 * Test method for
	 * {@link com.pancaketech.stardog.api.StardogConnectionPool#StardogConnectionPool()}
	 * .
	 */
	@Test
	public void testStardogConnectionPool() {
		StardogConnectionPool scp = new StardogConnectionPool();
		try {
			assertEquals(50, scp.getMaxPoolSize());
			assertEquals(10000L, scp.getPoolAggressiveness());
		} finally {
			scp.terminatePool();
		}
	}

	/**
	 * Test method for
	 * {@link com.pancaketech.stardog.api.StardogConnectionPool#connect()}.
	 * 
	 * @throws StardogException
	 * @throws InterruptedException
	 */
	@Test
	public void testConnect() throws StardogException, InterruptedException {
		/*
		 * Note: One connection is kept open during open/query/close tests to
		 * make sure it doesn't cause errors and to make sure that any cached
		 * info is warmed up by the first connection (if Stardog has any).
		 */
		pool = new StardogConnectionPool();
		try {
			final ConnectionConfiguration configuration = ConnectionConfiguration
					.to("testDB");
			pool.setConnConfig(configuration);

			// warm cache
			for (int i = 0; i < 10; i++) {
				Connection c = configuration.connect();
				Query q = c.query("ASK { ?s ?p ?o }");
				assertTrue(q.executeAsk());
				c.close();
			}

			int connCount = 500;
			// Test without connection pool
			Connection conn = configuration.connect();
			long t0 = System.currentTimeMillis();
			for (int i = 0; i < connCount; i++) {
				Connection c = configuration.connect();
				Query q = c.query("ASK { ?s ?p ?o }");
				assertTrue(q.executeAsk());
				c.close();
			}
			long t1 = System.currentTimeMillis();
			System.out.println("Connections without pool (" + connCount + "): "
					+ (t1 - t0) + "ms. (" + (((double) (t1 - t0)) / connCount)
					+ "ms per connection)");
			conn.close();

			// Open first connection outside of timed loop to initialize
			conn = pool.connect();
			t0 = System.currentTimeMillis();
			for (int i = 0; i < connCount; i++) {
				connectAskClose();
			}
			t1 = System.currentTimeMillis();
			System.out.println("Serial connections (" + connCount + "): "
					+ (t1 - t0) + "ms. (" + (((double) (t1 - t0)) / connCount)
					+ "ms per connection)");
			t0 = System.currentTimeMillis();

			LinkedList<Connection> conns = new LinkedList<Connection>();
			for (int i = 0; i < connCount; i++) {
				Connection c = pool.connect();
				conns.add(c);
				Query q = c.query("ASK { ?s ?p ?o }");
				assertTrue(q.executeAsk());
				if (conns.size() == 49) {
					conns.removeFirst().close();
				}
			}
			conn.close();

			t1 = System.currentTimeMillis();
			System.out
					.println("Pooling and serial connections (" + connCount
							+ "): " + (t1 - t0) + "ms. ("
							+ (((double) (t1 - t0)) / connCount)
							+ "ms per connection)");
			t0 = System.currentTimeMillis();

			// multi-threaded test

			final int threadedConnCount = 500;
			int threadCount = 100;
			List<Thread> threads = new LinkedList<Thread>();
			for (int i = 0; i < threadCount; i++) {
				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						for (int i = 0; i < threadedConnCount; i++) {
							try {
								Connection c = configuration.connect();
								Query q = c.query("ASK { ?s ?p ?o }");
								assertTrue(q.executeAsk());
								c.close();
							} catch (Throwable e) {
								errors.add(e);
								return;
							}
						}
					}
				});
				t.setDaemon(true);
				t.start();
				threads.add(t);
			}

			for (Thread t : threads) {
				t.join();
			}

			t1 = System.currentTimeMillis();
			double ratio = (((double) (t1 - t0)) / ((double) (threadCount * threadedConnCount)));
			System.out.println("Threaded without pool (" + threadCount + " - "
					+ threadedConnCount + "): " + (t1 - t0) + "ms. (" + ratio
					+ "ms per connection)");

			for (Throwable t : errors)
				t.printStackTrace();
			assertEquals(0, errors.size());
			t0 = System.currentTimeMillis();

			// multi-threaded test

			for (int i = 0; i < threadCount; i++) {
				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						for (int i = 0; i < threadedConnCount; i++) {
							try {
								connectAskClose();
							} catch (Throwable e) {
								errors.add(e);
							}
						}
					}
				});
				t.setDaemon(true);
				t.start();
				threads.add(t);
			}

			for (Thread t : threads) {
				t.join();
			}

			t1 = System.currentTimeMillis();
			ratio = (((double) (t1 - t0)) / ((double) (threadCount * threadedConnCount)));
			System.out.println("Threaded with serial pool (" + threadCount
					+ " - " + threadedConnCount + "): " + (t1 - t0) + "ms. ("
					+ ratio + "ms per connection)");

			for (Throwable t : errors)
				t.printStackTrace();
			assertEquals(0, errors.size());
			t0 = System.currentTimeMillis();

			// multi-threaded test
			pool.setMaxPoolSize(400);
			for (int i = 0; i < threadCount; i++) {
				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							LinkedList<Connection> conns = new LinkedList<Connection>();
							for (int i = 0; i < threadedConnCount; i++) {
								Connection c = pool.connect();
								conns.add(c);
								Query q = c.query("ASK { ?s ?p ?o }");
								assertTrue(q.executeAsk());
								if (conns.size() == 4) {
									conns.removeFirst().close();
								}
							}
						} catch (Throwable e) {
							errors.add(e);
						}
					}
				});
				t.setDaemon(true);
				t.start();
				threads.add(t);
			}

			for (Thread t : threads) {
				t.join();
			}

			t1 = System.currentTimeMillis();
			ratio = (((double) (t1 - t0)) / ((double) (threadCount * threadedConnCount)));
			System.out.println("Threaded with multi pool (" + threadCount
					+ " - " + threadedConnCount + "): " + (t1 - t0) + "ms. ("
					+ ratio + "ms per connection)");

			for (Throwable t : errors)
				t.printStackTrace();
			assertEquals(0, errors.size());
		} finally {
			pool.terminatePool();
		}
	}

	private StardogConnectionPool pool;
	private List<Throwable> errors = Collections.synchronizedList(new LinkedList<Throwable>());

	private void connectAskClose() throws StardogException {
		try {
			Connection c = pool.connect();
			Query q = c.query("ASK { ?s ?p ?o }");
			assertTrue(q.executeAsk());
			c.close();
		} catch (StardogException e) {
			synchronized (errors) {
				errors.add(e);
			}
			throw e;
		}
	}

	/**
	 * Test method for
	 * {@link com.pancaketech.stardog.api.StardogConnectionPool#terminatePool()}
	 * .
	 * 
	 * @throws StardogException
	 */
	@Test
	public void testTerminatePool() throws StardogException {
		ConnectionConfiguration config = ConnectionConfiguration.to("testDB");
		StardogConnectionPool scp = new StardogConnectionPool();
		scp.setConnConfig(config);
		scp.terminatePool();
		assertEquals(0, scp.getPoolCount());
		assertNull(scp.connect());
	}

	/**
	 * Test method for
	 * {@link com.pancaketech.stardog.api.StardogConnectionPool#getMaxPoolSize()}
	 * .
	 * 
	 * @throws StardogException
	 * @throws InterruptedException
	 */
	@Test
	public void testSetMaxPoolSize() throws StardogException,
			InterruptedException {
		ConnectionConfiguration config = ConnectionConfiguration.to("testDB");
		pool = new StardogConnectionPool();
		try {
			pool.setConnConfig(config);

			IllegalArgumentException iae = null;
			try {
				pool.setMaxPoolSize(-1);
			} catch (IllegalArgumentException e) {
				iae = e;
			}
			assertNotNull(iae);

			pool.setMaxPoolSize(1);
			assertEquals(1, pool.getMaxPoolSize());
			assertEquals(0, pool.getPoolCount());
			Connection conn = pool.connect();
			assertEquals(1, pool.getMaxPoolSize());
			assertEquals(1, pool.getPoolCount());
			errors.clear();
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						connectAskClose();
					} catch (StardogException e) {
						e.printStackTrace();
					}
				}
			});
			t.setDaemon(true);
			t.start();
			for (int i = 0; i < 11; i++) {
				assertEquals(1, pool.getPoolCount());
				Thread.sleep(10);
			}
			assertEquals(1, pool.getPoolCount());
			assertTrue(t.isAlive());
			conn.close();
			assertEquals(1, pool.getPoolCount());
			t.join();
			assertEquals(0, errors.size());
		} finally {
			pool.terminatePool();
			pool = null;
		}
	}

	/**
	 * Test method for
	 * {@link com.pancaketech.stardog.api.StardogConnectionPool#setConnConfig(com.clarkparsia.stardog.api.ConnectionConfiguration)}
	 * .
	 */
	@Test
	public void testSetConnConfig() {
		ConnectionConfiguration config = ConnectionConfiguration.to("testDB");
		StardogConnectionPool scp = new StardogConnectionPool();
		scp.setConnConfig(config);
		assertSame(config, scp.getConnConfig());
	}

	/**
	 * Test method for
	 * {@link com.pancaketech.stardog.api.StardogConnectionPool#setPoolAggressiveness(long)}
	 * .
	 * 
	 * @throws StardogException
	 * @throws InterruptedException
	 */
	@Test
	public void testSetPoolAggressiveness() throws StardogException,
			InterruptedException {
		StardogConnectionPool scp = new StardogConnectionPool();
		try {
			scp.setConnConfig(ConnectionConfiguration.to("testDB"));

			IllegalArgumentException iae = null;
			try {
				scp.setPoolAggressiveness(-1);
			} catch (IllegalArgumentException e) {
				iae = e;
			}
			assertNotNull(iae);

			iae = null;
			try {
				scp.setPoolAggressiveness(99);
			} catch (IllegalArgumentException e) {
				iae = e;
			}
			assertNotNull(iae);

			scp.setPoolAggressiveness(5000000);
			assertEquals(5000000, scp.getPoolAggressiveness());
			scp.setPoolAggressiveness(100);
			assertEquals(100, scp.getPoolAggressiveness());

			Connection c = scp.connect();
			assertEquals(1, scp.getPoolCount());
			c.close();
			assertEquals(1, scp.getPoolCount());
			Thread.sleep(150);
			assertEquals(0, scp.getPoolCount());
		} finally {
			scp.terminatePool();
		}

	}

}

