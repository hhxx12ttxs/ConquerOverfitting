/**
 * Copyright 2009 The Apache Software Foundation
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
package org.apache.hadoop.hbase.client;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HServerAddress;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.LargeTests;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.client.HTable.DaemonThreadFactory;
import org.apache.hadoop.hbase.client.metrics.ScanMetrics;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.KeyOnlyFilter;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.WhileMatchFilter;
import org.apache.hadoop.hbase.io.hfile.BlockCache;
import org.apache.hadoop.hbase.io.hfile.CacheConfig;
import org.apache.hadoop.hbase.regionserver.HRegion;
import org.apache.hadoop.hbase.regionserver.HRegionServer;
import org.apache.hadoop.hbase.regionserver.Store;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.DataInputBuffer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Run tests that use the HBase clients; {@link HTable} and {@link HTablePool}.
 * Sets up the HBase mini cluster once at start and runs through all client tests.
 * Each creates a table named for the method and does its stuff against that.
 */
@Category(LargeTests.class)
public class TestFromClientSide {
  final Log LOG = LogFactory.getLog(getClass());
  private final static HBaseTestingUtility TEST_UTIL = new HBaseTestingUtility();
  private static byte [] ROW = Bytes.toBytes("testRow");
  private static byte [] FAMILY = Bytes.toBytes("testFamily");
  private static byte [] QUALIFIER = Bytes.toBytes("testQualifier");
  private static byte [] VALUE = Bytes.toBytes("testValue");
  private static int SLAVES = 3;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    // We need more than one region server in this test
    TEST_UTIL.startMiniCluster(SLAVES);
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    TEST_UTIL.shutdownMiniCluster();
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    // Nothing to do.
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    // Nothing to do.
  }

  /**
   * Basic client side validation of HBASE-4536
   */
   @Test
   public void testKeepDeletedCells() throws Exception {
     final byte[] TABLENAME = Bytes.toBytes("testKeepDeletesCells");
     final byte[] FAMILY = Bytes.toBytes("family");
     final byte[] C0 = Bytes.toBytes("c0");

     final byte[] T1 = Bytes.toBytes("T1");
     final byte[] T2 = Bytes.toBytes("T2");
     final byte[] T3 = Bytes.toBytes("T3");
     HColumnDescriptor hcd = new HColumnDescriptor(FAMILY,
         HColumnDescriptor.DEFAULT_MIN_VERSIONS,
         HColumnDescriptor.DEFAULT_VERSIONS,
         true,
         HColumnDescriptor.DEFAULT_COMPRESSION,
         HColumnDescriptor.DEFAULT_ENCODE_ON_DISK,
         HColumnDescriptor.DEFAULT_DATA_BLOCK_ENCODING,
         HColumnDescriptor.DEFAULT_IN_MEMORY,
         HColumnDescriptor.DEFAULT_BLOCKCACHE,
         HColumnDescriptor.DEFAULT_BLOCKSIZE,
         HColumnDescriptor.DEFAULT_TTL,
         HColumnDescriptor.DEFAULT_BLOOMFILTER,
         HColumnDescriptor.DEFAULT_REPLICATION_SCOPE);

     HTableDescriptor desc = new HTableDescriptor(TABLENAME);
     desc.addFamily(hcd);
     TEST_UTIL.getHBaseAdmin().createTable(desc);
     Configuration c = TEST_UTIL.getConfiguration();
     HTable h = new HTable(c, TABLENAME);

     long ts = System.currentTimeMillis();
     Put p = new Put(T1, ts);
     p.add(FAMILY, C0, T1);
     h.put(p);
     p = new Put(T1, ts+2);
     p.add(FAMILY, C0, T2);
     h.put(p);
     p = new Put(T1, ts+4);
     p.add(FAMILY, C0, T3);
     h.put(p);

     Delete d = new Delete(T1, ts+2, null);
     h.delete(d);

     d = new Delete(T1, ts+3, null);
     d.deleteColumns(FAMILY, C0, ts+3);
     h.delete(d);

     Get g = new Get(T1);
     // does *not* include the delete
     g.setTimeRange(0, ts+3);
     Result r = h.get(g);
     assertArrayEquals(T2, r.getValue(FAMILY, C0));

     Scan s = new Scan(T1);
     s.setTimeRange(0, ts+3);
     s.setMaxVersions();
     ResultScanner scanner = h.getScanner(s);
     KeyValue[] kvs = scanner.next().raw();
     assertArrayEquals(T2, kvs[0].getValue());
     assertArrayEquals(T1, kvs[1].getValue());
     scanner.close();

     s = new Scan(T1);
     s.setRaw(true);
     s.setMaxVersions();
     scanner = h.getScanner(s);
     kvs = scanner.next().raw();
     assertTrue(kvs[0].isDeleteFamily());
     assertArrayEquals(T3, kvs[1].getValue());
     assertTrue(kvs[2].isDelete());
     assertArrayEquals(T2, kvs[3].getValue());
     assertArrayEquals(T1, kvs[4].getValue());
     scanner.close();
     h.close();
   }

   /**
   * HBASE-2468 use case 1 and 2: region info de/serialization
   */
   @Test
   public void testRegionCacheDeSerialization() throws Exception {
     // 1. test serialization.
     LOG.info("Starting testRegionCacheDeSerialization");
     final byte[] TABLENAME = Bytes.toBytes("testCachePrewarm2");
     final byte[] FAMILY = Bytes.toBytes("family");
     Configuration conf = TEST_UTIL.getConfiguration();
     TEST_UTIL.createTable(TABLENAME, FAMILY);

     // Set up test table:
     // Create table:
     HTable table = new HTable(conf, TABLENAME);

     // Create multiple regions for this table
     TEST_UTIL.createMultiRegions(table, FAMILY);
     Scan s = new Scan();
     ResultScanner scanner = table.getScanner(s);
     while (scanner.next() != null) continue;

     Path tempPath = new Path(TEST_UTIL.getDataTestDir(), "regions.dat");

     final String tempFileName = tempPath.toString();

     FileOutputStream fos = new FileOutputStream(tempFileName);
     DataOutputStream dos = new DataOutputStream(fos);

     // serialize the region info and output to a local file.
     table.serializeRegionInfo(dos);
     dos.flush();
     dos.close();

     // read a local file and deserialize the region info from it.
     FileInputStream fis = new FileInputStream(tempFileName);
     DataInputStream dis = new DataInputStream(fis);

     Map<HRegionInfo, HServerAddress> deserRegions =
       table.deserializeRegionInfo(dis);
     dis.close();

     // regions obtained from meta scanner.
     Map<HRegionInfo, HServerAddress> loadedRegions =
       table.getRegionsInfo();

     // set the deserialized regions to the global cache.
     table.getConnection().clearRegionCache();

     table.getConnection().prewarmRegionCache(table.getTableName(),
         deserRegions);

     // verify whether the 2 maps are identical or not.
     assertEquals("Number of cached region is incorrect",
         HConnectionManager.getCachedRegionCount(conf, TABLENAME),
         loadedRegions.size());

     // verify each region is prefetched or not.
     for (Map.Entry<HRegionInfo, HServerAddress> e: loadedRegions.entrySet()) {
       HRegionInfo hri = e.getKey();
       assertTrue(HConnectionManager.isRegionCached(conf,
           hri.getTableName(), hri.getStartKey()));
     }

     // delete the temp file
     File f = new java.io.File(tempFileName);
     f.delete();
     LOG.info("Finishing testRegionCacheDeSerialization");
   }

  /**
   * HBASE-2468 use case 3:
   */
  @Test
  public void testRegionCachePreWarm() throws Exception {
    LOG.info("Starting testRegionCachePreWarm");
    final byte [] TABLENAME = Bytes.toBytes("testCachePrewarm");
    Configuration conf = TEST_UTIL.getConfiguration();

    // Set up test table:
    // Create table:
    TEST_UTIL.createTable(TABLENAME, FAMILY);

    // disable region cache for the table.
    HTable.setRegionCachePrefetch(conf, TABLENAME, false);
    assertFalse("The table is disabled for region cache prefetch",
        HTable.getRegionCachePrefetch(conf, TABLENAME));

    HTable table = new HTable(conf, TABLENAME);

    // create many regions for the table.
    TEST_UTIL.createMultiRegions(table, FAMILY);
    // This count effectively waits until the regions have been
    // fully assigned
    TEST_UTIL.countRows(table);
    table.getConnection().clearRegionCache();
    assertEquals("Clearing cache should have 0 cached ", 0,
        HConnectionManager.getCachedRegionCount(conf, TABLENAME));

    // A Get is suppose to do a region lookup request
    Get g = new Get(Bytes.toBytes("aaa"));
    table.get(g);

    // only one region should be cached if the cache prefetch is disabled.
    assertEquals("Number of cached region is incorrect ", 1,
        HConnectionManager.getCachedRegionCount(conf, TABLENAME));

    // now we enable cached prefetch.
    HTable.setRegionCachePrefetch(conf, TABLENAME, true);
    assertTrue("The table is enabled for region cache prefetch",
        HTable.getRegionCachePrefetch(conf, TABLENAME));

    HTable.setRegionCachePrefetch(conf, TABLENAME, false);
    assertFalse("The table is disabled for region cache prefetch",
        HTable.getRegionCachePrefetch(conf, TABLENAME));

    HTable.setRegionCachePrefetch(conf, TABLENAME, true);
    assertTrue("The table is enabled for region cache prefetch",
        HTable.getRegionCachePrefetch(conf, TABLENAME));

    table.getConnection().clearRegionCache();

    assertEquals("Number of cached region is incorrect ", 0,
        HConnectionManager.getCachedRegionCount(conf, TABLENAME));

    // if there is a cache miss, some additional regions should be prefetched.
    Get g2 = new Get(Bytes.toBytes("bbb"));
    table.get(g2);

    // Get the configured number of cache read-ahead regions.
    int prefetchRegionNumber = conf.getInt("hbase.client.prefetch.limit", 10);

    // the total number of cached regions == region('aaa") + prefeched regions.
    LOG.info("Testing how many regions cached");
    assertEquals("Number of cached region is incorrect ", prefetchRegionNumber,
        HConnectionManager.getCachedRegionCount(conf, TABLENAME));

    table.getConnection().clearRegionCache();

    Get g3 = new Get(Bytes.toBytes("abc"));
    table.get(g3);
    assertEquals("Number of cached region is incorrect ", prefetchRegionNumber,
        HConnectionManager.getCachedRegionCount(conf, TABLENAME));

    LOG.info("Finishing testRegionCachePreWarm");
  }


  /**
   * Verifies that getConfiguration returns the same Configuration object used
   * to create the HTable instance.
   */
  @Test
  public void testGetConfiguration() throws Exception {
    byte[] TABLE = Bytes.toBytes("testGetConfiguration");
    byte[][] FAMILIES = new byte[][] { Bytes.toBytes("foo") };
    Configuration conf = TEST_UTIL.getConfiguration();
    HTable table = TEST_UTIL.createTable(TABLE, FAMILIES, conf);
    assertSame(conf, table.getConfiguration());
  }

  /**
   * Test from client side of an involved filter against a multi family that
   * involves deletes.
   *
   * @throws Exception
   */
  @Test
  public void testWeirdCacheBehaviour() throws Exception {
    byte [] TABLE = Bytes.toBytes("testWeirdCacheBehaviour");
    byte [][] FAMILIES = new byte[][] { Bytes.toBytes("trans-blob"),
        Bytes.toBytes("trans-type"), Bytes.toBytes("trans-date"),
        Bytes.toBytes("trans-tags"), Bytes.toBytes("trans-group") };
    HTable ht = TEST_UTIL.createTable(TABLE, FAMILIES);
    String value = "this is the value";
    String value2 = "this is some other value";
    String keyPrefix1 = UUID.randomUUID().toString();
    String keyPrefix2 = UUID.randomUUID().toString();
    String keyPrefix3 = UUID.randomUUID().toString();
    putRows(ht, 3, value, keyPrefix1);
    putRows(ht, 3, value, keyPrefix2);
    putRows(ht, 3, value, keyPrefix3);
    ht.flushCommits();
    putRows(ht, 3, value2, keyPrefix1);
    putRows(ht, 3, value2, keyPrefix2);
    putRows(ht, 3, value2, keyPrefix3);
    HTable table = new HTable(TEST_UTIL.getConfiguration(), TABLE);
    System.out.println("Checking values for key: " + keyPrefix1);
    assertEquals("Got back incorrect number of rows from scan", 3,
        getNumberOfRows(keyPrefix1, value2, table));
    System.out.println("Checking values for key: " + keyPrefix2);
    assertEquals("Got back incorrect number of rows from scan", 3,
        getNumberOfRows(keyPrefix2, value2, table));
    System.out.println("Checking values for key: " + keyPrefix3);
    assertEquals("Got back incorrect number of rows from scan", 3,
        getNumberOfRows(keyPrefix3, value2, table));
    deleteColumns(ht, value2, keyPrefix1);
    deleteColumns(ht, value2, keyPrefix2);
    deleteColumns(ht, value2, keyPrefix3);
    System.out.println("Starting important checks.....");
    assertEquals("Got back incorrect number of rows from scan: " + keyPrefix1,
      0, getNumberOfRows(keyPrefix1, value2, table));
    assertEquals("Got back incorrect number of rows from scan: " + keyPrefix2,
      0, getNumberOfRows(keyPrefix2, value2, table));
    assertEquals("Got back incorrect number of rows from scan: " + keyPrefix3,
      0, getNumberOfRows(keyPrefix3, value2, table));
    ht.setScannerCaching(0);
    assertEquals("Got back incorrect number of rows from scan", 0,
      getNumberOfRows(keyPrefix1, value2, table)); ht.setScannerCaching(100);
    assertEquals("Got back incorrect number of rows from scan", 0,
      getNumberOfRows(keyPrefix2, value2, table));
  }

  private void deleteColumns(HTable ht, String value, String keyPrefix)
  throws IOException {
    ResultScanner scanner = buildScanner(keyPrefix, value, ht);
    Iterator<Result> it = scanner.iterator();
    int count = 0;
    while (it.hasNext()) {
      Result result = it.next();
      Delete delete = new Delete(result.getRow());
      delete.deleteColumn(Bytes.toBytes("trans-tags"), Bytes.toBytes("qual2"));
      ht.delete(delete);
      count++;
    }
    assertEquals("Did not perform correct number of deletes", 3, count);
  }

  private int getNumberOfRows(String keyPrefix, String value, HTable ht)
      throws Exception {
    ResultScanner resultScanner = buildScanner(keyPrefix, value, ht);
    Iterator<Result> scanner = resultScanner.iterator();
    int numberOfResults = 0;
    while (scanner.hasNext()) {
      Result result = scanner.next();
      System.out.println("Got back key: " + Bytes.toString(result.getRow()));
      for (KeyValue kv : result.raw()) {
        System.out.println("kv=" + kv.toString() + ", "
            + Bytes.toString(kv.getValue()));
      }
      numberOfResults++;
    }
    return numberOfResults;
  }

  private ResultScanner buildScanner(String keyPrefix, String value, HTable ht)
      throws IOException {
    // OurFilterList allFilters = new OurFilterList();
    FilterList allFilters = new FilterList(/* FilterList.Operator.MUST_PASS_ALL */);
    allFilters.addFilter(new PrefixFilter(Bytes.toBytes(keyPrefix)));
    SingleColumnValueFilter filter = new SingleColumnValueFilter(Bytes
        .toBytes("trans-tags"), Bytes.toBytes("qual2"), CompareOp.EQUAL, Bytes
        .toBytes(value));
    filter.setFilterIfMissing(true);
    allFilters.addFilter(filter);

    // allFilters.addFilter(new
    // RowExcludingSingleColumnValueFilter(Bytes.toBytes("trans-tags"),
    // Bytes.toBytes("qual2"), CompareOp.EQUAL, Bytes.toBytes(value)));

    Scan scan = new Scan();
    scan.addFamily(Bytes.toBytes("trans-blob"));
    scan.addFamily(Bytes.toBytes("trans-type"));
    scan.addFamily(Bytes.toBytes("trans-date"));
    scan.addFamily(Bytes.toBytes("trans-tags"));
    scan.addFamily(Bytes.toBytes("trans-group"));
    scan.setFilter(allFilters);

    return ht.getScanner(scan);
  }

  private void putRows(HTable ht, int numRows, String value, String key)
      throws IOException {
    for (int i = 0; i < numRows; i++) {
      String row = key + "_" + UUID.randomUUID().toString();
      System.out.println(String.format("Saving row: %s, with value %s", row,
          value));
      Put put = new Put(Bytes.toBytes(row));
      put.setWriteToWAL(false);
      put.add(Bytes.toBytes("trans-blob"), null, Bytes
          .toBytes("value for blob"));
      put.add(Bytes.toBytes("trans-type"), null, Bytes.toBytes("statement"));
      put.add(Bytes.toBytes("trans-date"), null, Bytes
          .toBytes("20090921010101999"));
      put.add(Bytes.toBytes("trans-tags"), Bytes.toBytes("qual2"), Bytes
          .toBytes(value));
      put.add(Bytes.toBytes("trans-group"), null, Bytes
          .toBytes("adhocTransactionGroupId"));
      ht.put(put);
    }
  }

  /**
   * Test filters when multiple regions.  It does counts.  Needs eye-balling of
   * logs to ensure that we're not scanning more regions that we're supposed to.
   * Related to the TestFilterAcrossRegions over in the o.a.h.h.filter package.
   * @throws IOException
   * @throws InterruptedException
   */
  @Test
  public void testFilterAcrossMultipleRegions()
  throws IOException, InterruptedException {
    byte [] name = Bytes.toBytes("testFilterAcrossMutlipleRegions");
    HTable t = TEST_UTIL.createTable(name, FAMILY);
    int rowCount = TEST_UTIL.loadTable(t, FAMILY);
    assertRowCount(t, rowCount);
    // Split the table.  Should split on a reasonable key; 'lqj'
    Map<HRegionInfo, HServerAddress> regions  = splitTable(t);
    assertRowCount(t, rowCount);
    // Get end key of first region.
    byte [] endKey = regions.keySet().iterator().next().getEndKey();
    // Count rows with a filter that stops us before passed 'endKey'.
    // Should be count of rows in first region.
    int endKeyCount = countRows(t, createScanWithRowFilter(endKey));
    assertTrue(endKeyCount < rowCount);

    // How do I know I did not got to second region?  Thats tough.  Can't really
    // do that in client-side region test.  I verified by tracing in debugger.
    // I changed the messages that come out when set to DEBUG so should see
    // when scanner is done. Says "Finished with scanning..." with region name.
    // Check that its finished in right region.

    // New test.  Make it so scan goes into next region by one and then two.
    // Make sure count comes out right.
    byte [] key = new byte [] {endKey[0], endKey[1], (byte)(endKey[2] + 1)};
    int plusOneCount = countRows(t, createScanWithRowFilter(key));
    assertEquals(endKeyCount + 1, plusOneCount);
    key = new byte [] {endKey[0], endKey[1], (byte)(endKey[2] + 2)};
    int plusTwoCount = countRows(t, createScanWithRowFilter(key));
    assertEquals(endKeyCount + 2, plusTwoCount);

    // New test.  Make it so I scan one less than endkey.
    key = new byte [] {endKey[0], endKey[1], (byte)(endKey[2] - 1)};
    int minusOneCount = countRows(t, createScanWithRowFilter(key));
    assertEquals(endKeyCount - 1, minusOneCount);
    // For above test... study logs.  Make sure we do "Finished with scanning.."
    // in first region and that we do not fall into the next region.

    key = new byte [] {'a', 'a', 'a'};
    int countBBB = countRows(t,
      createScanWithRowFilter(key, null, CompareFilter.CompareOp.EQUAL));
    assertEquals(1, countBBB);

    int countGreater = countRows(t, createScanWithRowFilter(endKey, null,
      CompareFilter.CompareOp.GREATER_OR_EQUAL));
    // Because started at start of table.
    assertEquals(0, countGreater);
    countGreater = countRows(t, createScanWithRowFilter(endKey, endKey,
      CompareFilter.CompareOp.GREATER_OR_EQUAL));
    assertEquals(rowCount - endKeyCount, countGreater);
  }

  /*
   * @param key
   * @return Scan with RowFilter that does LESS than passed key.
   */
  private Scan createScanWithRowFilter(final byte [] key) {
    return createScanWithRowFilter(key, null, CompareFilter.CompareOp.LESS);
  }

  /*
   * @param key
   * @param op
   * @param startRow
   * @return Scan with RowFilter that does CompareOp op on passed key.
   */
  private Scan createScanWithRowFilter(final byte [] key,
      final byte [] startRow, CompareFilter.CompareOp op) {
    // Make sure key is of some substance... non-null and > than first key.
    assertTrue(key != null && key.length > 0 &&
      Bytes.BYTES_COMPARATOR.compare(key, new byte [] {'a', 'a', 'a'}) >= 0);
    LOG.info("Key=" + Bytes.toString(key));
    Scan s = startRow == null? new Scan(): new Scan(startRow);
    Filter f = new RowFilter(op, new BinaryComparator(key));
    f = new WhileMatchFilter(f);
    s.setFilter(f);
    return s;
  }

  /*
   * @param t
   * @param s
   * @return Count of rows in table.
   * @throws IOException
   */
  private int countRows(final HTable t, final Scan s)
  throws IOException {
    // Assert all rows in table.
    ResultScanner scanner = t.getScanner(s);
    int count = 0;
    for (Result result: scanner) {
      count++;
      assertTrue(result.size() > 0);
      // LOG.info("Count=" + count + ", row=" + Bytes.toString(result.getRow()));
    }
    return count;
  }

  private void assertRowCount(final HTable t, final int expected)
  throws IOException {
    assertEquals(expected, countRows(t, new Scan()));
  }

  /*
   * Split table into multiple regions.
   * @param t Table to split.
   * @return Map of regions to servers.
   * @throws IOException
   */
  private Map<HRegionInfo, HServerAddress> splitTable(final HTable t)
  throws IOException, InterruptedException {
    // Split this table in two.
    HBaseAdmin admin = new HBaseAdmin(TEST_UTIL.getConfiguration());
    admin.split(t.getTableName());
    Map<HRegionInfo, HServerAddress> regions = waitOnSplit(t);
    assertTrue(regions.size() > 1);
    return regions;
  }

  /*
   * Wait on table split.  May return because we waited long enough on the split
   * and it didn't happen.  Caller should check.
   * @param t
   * @return Map of table regions; caller needs to check table actually split.
   */
  private Map<HRegionInfo, HServerAddress> waitOnSplit(final HTable t)
  throws IOException {
    Map<HRegionInfo, HServerAddress> regions = t.getRegionsInfo();
    int originalCount = regions.size();
    for (int i = 0; i < TEST_UTIL.getConfiguration().getInt("hbase.test.retries", 30); i++) {
      Thread.currentThread();
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      regions = t.getRegionsInfo();
      if (regions.size() > originalCount) break;
    }
    return regions;
  }

  @Test
  public void testSuperSimple() throws Exception {
    byte [] TABLE = Bytes.toBytes("testSuperSimple");
    HTable ht = TEST_UTIL.createTable(TABLE, FAMILY);
    Put put = new Put(ROW);
    put.add(FAMILY, QUALIFIER, VALUE);
    ht.put(put);
    Scan scan = new Scan();
    scan.addColumn(FAMILY, TABLE);
    ResultScanner scanner = ht.getScanner(scan);
    Result result = scanner.next();
    assertTrue("Expected null result", result == null);
    scanner.close();
  }

  @Test
  public void testMaxKeyValueSize() throws Exception {
    byte [] TABLE = Bytes.toBytes("testMaxKeyValueSize");
    Configuration conf = TEST_UTIL.getConfiguration();
    String oldMaxSize = conf.get("hbase.client.keyvalue.maxsize");
    HTable ht = TEST_UTIL.createTable(TABLE, FAMILY);
    byte[] value = new byte[4 * 1024 * 1024];
    Put put = new Put(ROW);
    put.add(FAMILY, QUALIFIER, value);
    ht.put(put);
    try {
      conf.setInt("hbase.client.keyvalue.maxsize", 2 * 1024 * 1024);
      TABLE = Bytes.toBytes("testMaxKeyValueSize2");
      ht = TEST_UTIL.createTable(TABLE, FAMILY);
      put = new Put(ROW);
      put.add(FAMILY, QUALIFIER, value);
      ht.put(put);
      fail("Inserting a too large KeyValue worked, should throw exception");
    } catch(Exception e) {}
    conf.set("hbase.client.keyvalue.maxsize", oldMaxSize);
  }

  @Test
  public void testFilters() throws Exception {
    byte [] TABLE = Bytes.toBytes("testFilters");
    HTable ht = TEST_UTIL.createTable(TABLE, FAMILY);
    byte [][] ROWS = makeN(ROW, 10);
    byte [][] QUALIFIERS = {
        Bytes.toBytes("col0-<d2v1>-<d3v2>"), Bytes.toBytes("col1-<d2v1>-<d3v2>"),
        Bytes.toBytes("col2-<d2v1>-<d3v2>"), Bytes.toBytes("col3-<d2v1>-<d3v2>"),
        Bytes.toBytes("col4-<d2v1>-<d3v2>"), Bytes.toBytes("col5-<d2v1>-<d3v2>"),
        Bytes.toBytes("col6-<d2v1>-<d3v2>"), Bytes.toBytes("col7-<d2v1>-<d3v2>"),
        Bytes.toBytes("col8-<d2v1>-<d3v2>"), Bytes.toBytes("col9-<d2v1>-<d3v2>")
    };
    for(int i=0;i<10;i++) {
      Put put = new Put(ROWS[i]);
      put.setWriteToWAL(false);
      put.add(FAMILY, QUALIFIERS[i], VALUE);
      ht.put(put);
    }
    Scan scan = new Scan();
    scan.addFamily(FAMILY);
    Filter filter = new QualifierFilter(CompareOp.EQUAL,
      new RegexStringComparator("col[1-5]"));
    scan.setFilter(filter);
    ResultScanner scanner = ht.getScanner(scan);
    int expectedIndex = 1;
    for(Result result : ht.getScanner(scan)) {
      assertEquals(result.size(), 1);
      assertTrue(Bytes.equals(result.raw()[0].getRow(), ROWS[expectedIndex]));
      assertTrue(Bytes.equals(result.raw()[0].getQualifier(),
          QUALIFIERS[expectedIndex]));
      expectedIndex++;
    }
    assertEquals(expectedIndex, 6);
    scanner.close();
  }

  @Test
  public void testKeyOnlyFilter() throws Exception {
    byte [] TABLE = Bytes.toBytes("testKeyOnlyFilter");
    HTable ht = TEST_UTIL.createTable(TABLE, FAMILY);
    byte [][] ROWS = makeN(ROW, 10);
    byte [][] QUALIFIERS = {
        Bytes.toBytes("col0-<d2v1>-<d3v2>"), Bytes.toBytes("col1-<d2v1>-<d3v2>"),
        Bytes.toBytes("col2-<d2v1>-<d3v2>"), Bytes.toBytes("col3-<d2v1>-<d3v2>"),
        Bytes.toBytes("col4-<d2v1>-<d3v2>"), Bytes.toBytes("col5-<d2v1>-<d3v2>"),
        Bytes.toBytes("col6-<d2v1>-<d3v2>"), Bytes.toBytes("col7-<d2v1>-<d3v2>"),
        Bytes.toBytes("col8-<d2v1>-<d3v2>"), Bytes.toBytes("col9-<d2v1>-<d3v2>")
    };
    for(int i=0;i<10;i++) {
      Put put = new Put(ROWS[i]);
      put.setWriteToWAL(false);
      put.add(FAMILY, QUALIFIERS[i], VALUE);
      ht.put(put);
    }
    Scan scan = new Scan();
    scan.addFamily(FAMILY);
    Filter filter = new KeyOnlyFilter(true);
    scan.setFilter(filter);
    ResultScanner scanner = ht.getScanner(scan);
    int count = 0;
    for(Result result : ht.getScanner(scan)) {
      assertEquals(result.size(), 1);
      assertEquals(result.raw()[0].getValueLength(), Bytes.SIZEOF_INT);
      assertEquals(Bytes.toInt(result.raw()[0].getValue()), VALUE.length);
      count++;
    }
    assertEquals(count, 10);
    scanner.close();
  }

  /**
   * Test simple table and non-existent row cases.
   */
  @Test
  public void testSimpleMissing() throws Exception {
    byte [] TABLE = Bytes.toBytes("testSimpleMissing");
    HTable ht = TEST_UTIL.createTable(TABLE, FAMILY);
    byte [][] ROWS = makeN(ROW, 4);

    // Try to get a row on an empty table
    Get get = new Get(ROWS[0]);
    Result result = ht.get(get);
    assertEmptyResult(result);

    get = new Get(ROWS[0]);
    get.addFamily(FAMILY);
    result = ht.get(get);
    assertEmptyResult(result);

    get = new Get(ROWS[0]);
    get.addColumn(FAMILY, QUALIFIER);
    result = ht.get(get);
    assertEmptyResult(result);

    Scan scan = new Scan();
    result = getSingleScanResult(ht, scan);
    assertNullResult(result);


    scan = new Scan(ROWS[0]);
    result = getSingleScanResult(ht, scan);
    assertNullResult(result);

    scan = new Scan(ROWS[0],ROWS[1]);
    result = getSingleScanResult(ht, scan);
    assertNullResult(result);

    scan = new Scan();
    scan.addFamily(FAMILY);
    result = getSingleScanResult(ht, scan);
    assertNullResult(result);

    scan = new Scan();
    scan.addColumn(FAMILY, QUALIFIER);
    result = getSingleScanResult(ht, scan);
    assertNullResult(result);

    // Insert a row

    Put put = new Put(ROWS[2]);
    put.add(FAMILY, QUALIFIER, VALUE);
    ht.put(put);

    // Try to get empty rows around it

    get = new Get(ROWS[1]);
    result = ht.get(get);
    assertEmptyResult(result);

    get = new Get(ROWS[0]);
    get.addFamily(FAMILY);
    result = ht.get(get);
    assertEmptyResult(result);

    get = new Get(ROWS[3]);
    get.addColumn(FAMILY, QUALIFIER);
    result = ht.get(get);
    assertEmptyResult(result);

    // Try to scan empty rows around it

    scan = new Scan(ROWS[3]);
    result = getSingleScanResult(ht, scan);
    assertNullResult(result);

    scan = new Scan(ROWS[0],ROWS[2]);
    result = getSingleScanResult(ht, scan);
    assertNullResult(result);

    // Make sure we can actually get the row

    get = new Get(ROWS[2]);
    result = ht.get(get);
    assertSingleResult(result, ROWS[2], FAMILY, QUALIFIER, VALUE);

    get = new Get(ROWS[2]);
    get.addFamily(FAMILY);
    result = ht.get(get);
    assertSingleResult(result, ROWS[2], FAMILY, QUALIFIER, VALUE);

    get = new Get(ROWS[2]);
    get.addColumn(FAMILY, QUALIFIER);
    result = ht.get(get);
    assertSingleResult(result, ROWS[2], FAMILY, QUALIFIER, VALUE);

    // Make sure we can scan the row

    scan = new Scan();
    result = getSingleScanResult(ht, scan);
    assertSingleResult(result, ROWS[2], FAMILY, QUALIFIER, VALUE);

    scan = new Scan(ROWS[0],ROWS[3]);
    result = getSingleScanResult(ht, scan);
    assertSingleResult(result, ROWS[2], FAMILY, QUALIFIER, VALUE);

    scan = new Scan(ROWS[2],ROWS[3]);
    result = getSingleScanResult(ht, scan);
    assertSingleResult(result, ROWS[2], FAMILY, QUALIFIER, VALUE);
  }

  /**
   * Test basic puts, gets, scans, and deletes for a single row
   * in a multiple family table.
   */
  @Test
  public void testSingleRowMultipleFamily() throws Exception {
    byte [] TABLE = Bytes.toBytes("testSingleRowMultipleFamily");
    byte [][] ROWS = makeN(ROW, 3);
    byte [][] FAMILIES = makeNAscii(FAMILY, 10);
    byte [][] QUALIFIERS = makeN(QUALIFIER, 10);
    byte [][] VALUES = makeN(VALUE, 10);

    HTable ht = TEST_UTIL.createTable(TABLE, FAMILIES);

    Get get;
    Scan scan;
    Delete delete;
    Put put;
    Result result;

    ////////////////////////////////////////////////////////////////////////////
    // Insert one column to one family
    ////////////////////////////////////////////////////////////////////////////

    put = new Put(ROWS[0]);
    put.add(FAMILIES[4], QUALIFIERS[0], VALUES[0]);
    ht.put(put);

    // Get the single column
    getVerifySingleColumn(ht, ROWS, 0, FAMILIES, 4, QUALIFIERS, 0, VALUES, 0);

    // Scan the single column
    scanVerifySingleColumn(ht, ROWS, 0, FAMILIES, 4, QUALIFIERS, 0, VALUES, 0);

    // Get empty results around inserted column
    getVerifySingleEmpty(ht, ROWS, 0, FAMILIES, 4, QUALIFIERS, 0);

    // Scan empty results around inserted column
    scanVerifySingleEmpty(ht, ROWS, 0, FAMILIES, 4, QUALIFIERS, 0);

    ////////////////////////////////////////////////////////////////////////////
    // Flush memstore and run same tests from storefiles
    ////////////////////////////////////////////////////////////////////////////

    TEST_UTIL.flush();

    // Redo get and scan tests from storefile
    getVerifySingleColumn(ht, ROWS, 0, FAMILIES, 4, QUALIFIERS, 0, VALUES, 0);
    scanVerifySingleColumn(ht, ROWS, 0, FAMILIES, 4, QUALIFIERS, 0, VALUES, 0);
    getVerifySingleEmpty(ht, ROWS, 0, FAMILIES, 4, QUALIFIERS, 0);
    scanVerifySingleEmpty(ht, ROWS, 0, FAMILIES, 4, QUALIFIERS, 0);

    ////////////////////////////////////////////////////////////////////////////
    // Now, Test reading from memstore and storefiles at once
    ////////////////////////////////////////////////////////////////////////////

    // Insert multiple columns to two other families
    put = new Put(ROWS[0]);
    put.add(FAMILIES[2], QUALIFIERS[2], VALUES[2]);
    put.add(FAMILIES[2], QUALIFIERS[4], VALUES[4]);
    put.add(FAMILIES[4], QUALIFIERS[4], VALUES[4]);
    put.add(FAMILIES[6], QUALIFIERS[6], VALUES[6]);
    put.add(FAMILIES[6], QUALIFIERS[7], VALUES[7]);
    put.add(FAMILIES[7], QUALIFIERS[7], VALUES[7]);
    put.add(FAMILIES[9], QUALIFIERS[0], VALUES[0]);
    ht.put(put);

    // Get multiple columns across multiple families and get empties around it
    singleRowGetTest(ht, ROWS, FAMILIES, QUALIFIERS, VALUES);

    // Scan multiple columns across multiple families and scan empties around it
    singleRowScanTest(ht, ROWS, FAMILIES, QUALIFIERS, VALUES);

    ////////////////////////////////////////////////////////////////////////////
    // Flush the table again
    ////////////////////////////////////////////////////////////////////////////

    TEST_UTIL.flush();

    // Redo tests again
    singleRowGetTest(ht, ROWS, FAMILIES, QUALIFIERS, VALUES);
    singleRowScanTest(ht, ROWS, FAMILIES, QUALIFIERS, VALUES);

    // Insert more data to memstore
    put = new Put(ROWS[0]);
    put.add(FAMILIES[6], QUALIFIERS[5], VALUES[5]);
    put.add(FAMILIES[6], QUALIFIERS[8], VALUES[8]);
    put.add(FAMILIES[6], QUALIFIERS[9], VALUES[9]);
    put.add(FAMILIES[4], QUALIFIERS[3], VALUES[3]);
    ht.put(put);

    ////////////////////////////////////////////////////////////////////////////
    // Delete a storefile column
    ////////////////////////////////////////////////////////////////////////////
    delete = new Delete(ROWS[0]);
    delete.deleteColumns(FAMILIES[6], QUALIFIERS[7]);
    ht.delete(delete);

    // Try to get deleted column
    get = new Get(ROWS[0]);
    get.addColumn(FAMILIES[6], QUALIFIERS[7]);
    result = ht.get(get);
    assertEmptyResult(result);

    // Try to scan deleted column
    scan = new Scan();
    scan.addColumn(FAMILIES[6], QUALIFIERS[7]);
    result = getSingleScanResult(ht, scan);
    assertNullResult(result);

    // Make sure we can still get a column before it and after it
    get = new Get(ROWS[0]);
    get.addColumn(FAMILIES[6], QUALIFIERS[6]);
    result = ht.get(get);
    assertSingleResult(result, ROWS[0], FAMILIES[6], QUALIFIERS[6], VALUES[6]);

    get = new Get(ROWS[0]);
    get.addColumn(FAMILIES[6], QUALIFIERS[8]);
    result = ht.get(get);
    assertSingleResult(result, ROWS[0], FAMILIES[6], QUALIFIERS[8], VALUES[8]);

    // Make sure we can still scan a column before it and after it
    scan = new Scan();
    scan.addColumn(FAMILIES[6], QUALIFIERS[6]);
    result = getSingleScanResult(ht, scan);
    assertSingleResult(result, ROWS[0], FAMILIES[6], QUALIFIERS[6], VALUES[6]);

    scan = new Scan();
    scan.addColumn(FAMILIES[6], QUALIFIERS[8]);
    result = getSingleScanResult(ht, scan);
    assertSingleResult(result, ROWS[0], FAMILIES[6], QUALIFIERS[8], VALUES[8]);

    ////////////////////////////////////////////////////////////////////////////
    // Delete a memstore column
    ////////////////////////////////////////////////////////////////////////////
    delete = new Delete(ROWS[0]);
    delete.deleteColumns(FAMILIES[6], QUALIFIERS[8]);
    ht.delete(delete);

    // Try to get deleted column
    get = new Get(ROWS[0]);
    get.addColumn(FAMILIES[6], QUALIFIERS[8]);
    result = ht.get(get);
    assertEmptyResult(result);

    // Try to scan deleted column
    scan = new Scan();
    scan.addColumn(FAMILIES[6], QUALIFIERS[8]);
    result = getSingleScanResult(ht, scan);
    assertNullResult(result);

    // Make sure we can still get a column before it and after it
    get = new Get(ROWS[0]);
    get.addColumn(FAMILIES[6], QUALIFIERS[6]);
    result = ht.get(get);
    assertSingleResult(result, ROWS[0], FAMILIES[6], QUALIFIERS[6], VALUES[6]);

    get = new Get(ROWS[0]);
    get.addColumn(FAMILIES[6], QUALIFIERS[9]);
    result = ht.get(get);
    assertSingleResult(result, ROWS[0], FAMILIES[6], QUALIFIERS[9], VALUES[9]);

    // Make sure we can still scan a column before it and after it
    scan = new Scan();
    scan.addColumn(FAMILIES[6], QUALIFIERS[6]);
    result = getSingleScanResult(ht, scan);
    assertSingleResult(result, ROWS[0], FAMILIES[6], QUALIFIERS[6], VALUES[6]);

    scan = new Scan();
    scan.addColumn(FAMILIES[6], QUALIFIERS[9]);
    result = getSingleScanResult(ht, scan);
    assertSingleResult(result, ROWS[0], FAMILIES[6], QUALIFIERS[9], VALUES[9]);

    ////////////////////////////////////////////////////////////////////////////
    // Delete joint storefile/memstore family
    ////////////////////////////////////////////////////////////////////////////

    delete = new Delete(ROWS[0]);
    delete.deleteFamily(FAMILIES[4]);
    ht.delete(delete);

    // Try to get storefile column in deleted family
    get = new Get(ROWS[0]);
    get.addColumn(FAMILIES[4], QUALIFIERS[4]);
    result = ht.get(get);
    assertEmptyResult(result);

    // Try to get memstore column in deleted family
    get = new Get(ROWS[0]);
    get.addColumn(FAMILIES[4], QUALIFIERS[3]);
    result = ht.get(get);
    assertEmptyResult(result);

    // Try to get deleted family
    get = new Get(ROWS[0]);
    get.addFamily(FAMILIES[4]);
    result = ht.get(get);
    assertEmptyResult(result);

    // Try to scan storefile column in deleted family
    scan = new Scan();
    scan.addColumn(FAMILIES[4], QUALIFIERS[4]);
    result = getSingleScanResult(ht, scan);
    assertNullResult(result);

    // Try to scan memstore column in deleted family
    scan = new Scan();
    scan.addColumn(FAMILIES[4], QUALIFIERS[3]);
    result = getSingleScanResult(ht, scan);
    assertNullResult(result);

    // Try to scan deleted family
    scan = new Scan();
    scan.addFamily(FAMILIES[4]);
    result = getSingleScanResult(ht, scan);
    assertNullResult(result);

    // Make sure we can still get another family
    get = new Get(ROWS[0]);
    get.addColumn(FAMILIES[2], QUALIFIERS[2]);
    result = ht.get(get);
    assertSingleResult(result, ROWS[0], FAMILIES[2], QUALIFIERS[2], VALUES[2]);

    get = new Get(ROWS[0]);
    get.addColumn(FAMILIES[6], QUALIFIERS[9]);
    result = ht.get(get);
    assertSingleResult(result, ROWS[0], FAMILIES[6], QUALIFIERS[9], VALUES[9]);

    // Make sure we can still scan another family
    scan = new Scan();
    scan.addColumn(FAMILIES[6], QUALIFIERS[6]);
    result = getSingleScanResult(ht, scan);
    assertSingleResult(result, ROWS[0], FAMILIES[6], QUALIFIERS[6], VALUES[6]);

    scan = new Scan();
    scan.addColumn(FAMILIES[6], QUALIFIERS[9]);
    result = getSingleScanResult(ht, scan);
    assertSingleResult(result, ROWS[0], FAMILIES[6], QUALIFIERS[9], VALUES[9]);

    ////////////////////////////////////////////////////////////////////////////
    // Flush everything and rerun delete tests
    ////////////////////////////////////////////////////////////////////////////

    TEST_UTIL.flush();

    // Try to get storefile column in deleted family
    get = new Get(ROWS[0]);
    get.addColumn(FAMILIES[4], QUALIFIERS[4]);
    result = ht.get(get);
    assertEmptyResult(result);

    // Try to get memstore column in deleted family
    get = new Get(ROWS[0]);
    get.addColumn(FAMILIES[4], QUALIFIERS[3]);
    result = ht.get(get);
    assertEmptyResult(result);

    // Try to get deleted family
    get = new Get(ROWS[0]);
    get.addFamily(FAMILIES[4]);
    result = ht.get(get);
    assertEmptyResult(result);

    // Try to scan storefile column in deleted family
    scan = new Scan();
    scan.addColumn(FAMILIES[4], QUALIFIERS[4]);
    result = getSingleScanResult(ht, scan);
    assertNullResult(result);

    // Try to scan memstore column in deleted family
    scan = new Scan();
    scan.addColumn(FAMILIES[4], QUALIFIERS[3]);
    result = getSingleScanResult(ht, scan);
    assertNullResult(result);

    // Try to scan deleted family
    scan = new Scan();
    scan.addFamily(FAMILIES[4]);
    result = getSingleScanResult(ht, scan);
    assertNullResult(result);

    // Make sure we can still get another family
    get = new Get(ROWS[0]);
    get.addColumn(FAMILIES[2], QUALIFIERS[2]);
    result = ht.get(get);
    assertSingleResult(result, ROWS[0], FAMILIES[2], QUALIFIERS[2], VALUES[2]);

    get = new Get(ROWS[0]);
    get.addColumn(FAMILIES[6], QUALIFIERS[9]);
    result = ht.get(get);
    assertSingleResult(result, ROWS[0], FAMILIES[6], QUALIFIERS[9], VALUES[9]);

    // Make sure we can still scan another family
    scan = new Scan();
    scan.addColumn(FAMILIES[6], QUALIFIERS[6]);
    result = getSingleScanResult(ht, scan);
    assertSingleResult(result, ROWS[0], FAMILIES[6], QUALIFIERS[6], VALUES[6]);

    scan = new Scan();
    scan.addColumn(FAMILIES[6], QUALIFIERS[9]);
    result = getSingleScanResult(ht, scan);
    assertSingleResult(result, ROWS[0], FAMILIES[6], QUALIFIERS[9], VALUES[9]);

  }

  @Test
  public void testNull() throws Exception {
    byte [] TABLE = Bytes.toBytes("testNull");

    // Null table name (should NOT work)
    try {
      TEST_UTIL.createTable(null, FAMILY);
      fail("Creating a table with null name passed, should have failed");
    } catch(Exception e) {}

    // Null family (should NOT work)
    try {
      TEST_UTIL.createTable(TABLE, (byte[])null);
      fail("Creating a table with a null family passed, should fail");
    } catch(Exception e) {}

    HTable ht = TEST_UTIL.createTable(TABLE, FAMILY);

    // Null row (should NOT work)
    try {
      Put put = new Put((byte[])null);
      put.add(FAMILY, QUALIFIER, VALUE);
      ht.put(put);
      fail("Inserting a null row worked, should throw exception");
    } catch(Exception e) {}

    // Null qualifier (should work)
    {
      Put put = new Put(ROW);
      put.add(FAMILY, null, VALUE);
      ht.put(put);

      getTestNull(ht, ROW, FAMILY, VALUE);

      scanTestNull(ht, ROW, FAMILY, VALUE);

      Delete delete = new Delete(ROW);
      delete.deleteColumns(FAMILY, null);
      ht.delete(delete);

      Get get = new Get(ROW);
      Result result = ht.get(get);
      assertEmptyResult(result);
    }

    // Use a new table
    byte [] TABLE2 = Bytes.toBytes("testNull2");
    ht = TEST_UTIL.createTable(TABLE2, FAMILY);

    // Empty qualifier, byte[0] instead of null (should work)
    try {
      Put put = new Put(ROW);
      put.add(FAMILY, HConstants.EMPTY_BYTE_ARRAY, VALUE);
      ht.put(put);

      getTestNull(ht, ROW, FAMILY, VALUE);

      scanTestNull(ht, ROW, FAMILY, VALUE);

      // Flush and try again

      TEST_UTIL.flush();

      getTestNull(ht, ROW, FAMILY, VALUE);

      scanTestNull(ht, ROW, FAMILY, VALUE);

      Delete delete = new Delete(ROW);
      delete.deleteColumns(FAMILY, HConstants.EMPTY_BYTE_ARRAY);
      ht.delete(delete);

      Get get = new Get(ROW);
      Result result = ht.get(get);
      assertEmptyResult(result);

    } catch(Exception e) {
      throw new IOException("Using a row with null qualifier threw exception, should ");
    }

    // Null value
    try {
      Put put = new Put(ROW);
      put.add(FAMILY, QUALIFIER, null);
      ht.put(put);

      Get get = new Get(ROW);
      get.addColumn(FAMILY, QUALIFIER);
      Result result = ht.get(get);
      assertSingleResult(result, ROW, FAMILY, QUALIFIER, null);

      Scan scan = new Scan();
      scan.addColumn(FAMILY, QUALIFIER);
      result = getSingleScanResult(ht, scan);
      assertSingleResult(result, ROW, FAMILY, QUALIFIER, null);

      Delete delete = new Delete(ROW);
      delete.deleteColumns(FAMILY, QUALIFIER);
      ht.delete(delete);

      get = new Get(ROW);
      result = ht.get(get);
      assertEmptyResult(result);

    } catch(Exception e) {
      throw new IOException("Null values should be allowed, but threw exception");
    }
  }

  @Test
  public void testVersions() throws Exception {
    byte [] TABLE = Bytes.toBytes("testVersions");

    long [] STAMPS = makeStamps(20);
    byte [][] VALUES = makeNAscii(VALUE, 20);

    HTable ht = TEST_UTIL.createTable(TABLE, FAMILY, 10);

    // Insert 4 versions of same column
    Put put = new Put(ROW);
    put.add(FAMILY, QUALIFIER, STAMPS[1], VALUES[1]);
    put.add(FAMILY, QUALIFIER, STAMPS[2], VALUES[2]);
    put.add(FAMILY, QUALIFIER, STAMPS[4], VALUES[4]);
    put.add(FAMILY, QUALIFIER, STAMPS[5], VALUES[5]);
    ht.put(put);

    // Verify we can get each one properly
    getVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[1], VALUES[1]);
    getVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[2], VALUES[2]);
    getVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[4], VALUES[4]);
    getVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[5], VALUES[5]);
    scanVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[1], VALUES[1]);
    scanVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[2], VALUES[2]);
    scanVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[4], VALUES[4]);
    scanVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[5], VALUES[5]);

    // Verify we don't accidentally get others
    getVersionAndVerifyMissing(ht, ROW, FAMILY, QUALIFIER, STAMPS[0]);
    getVersionAndVerifyMissing(ht, ROW, FAMILY, QUALIFIER, STAMPS[3]);
    getVersionAndVerifyMissing(ht, ROW, FAMILY, QUALIFIER, STAMPS[6]);
    scanVersionAndVerifyMissing(ht, ROW, FAMILY, QUALIFIER, STAMPS[0]);
    scanVersionAndVerifyMissing(ht, ROW, FAMILY, QUALIFIER, STAMPS[3]);
    scanVersionAndVerifyMissing(ht, ROW, FAMILY, QUALIFIER, STAMPS[6]);

    // Ensure maxVersions in query is respected
    Get get = new Get(ROW);
    get.addColumn(FAMILY, QUALIFIER);
    get.setMaxVersions(2);
    Result result = ht.get(get);
    assertNResult(result, ROW, FAMILY, QUALIFIER,
        new long [] {STAMPS[4], STAMPS[5]},
        new byte[][] {VALUES[4], VALUES[5]},
        0, 1);

    Scan scan = new Scan(ROW);
    scan.addColumn(FAMILY, QUALIFIER);
    scan.setMaxVersions(2);
    result = getSingleScanResult(ht, scan);
    assertNResult(result, ROW, FAMILY, QUALIFIER,
        new long [] {STAMPS[4], STAMPS[5]},
        new byte[][] {VALUES[4], VALUES[5]},
        0, 1);

    // Flush and redo

    TEST_UTIL.flush();

    // Verify we can get each one properly
    getVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[1], VALUES[1]);
    getVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[2], VALUES[2]);
    getVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[4], VALUES[4]);
    getVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[5], VALUES[5]);
    scanVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[1], VALUES[1]);
    scanVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[2], VALUES[2]);
    scanVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[4], VALUES[4]);
    scanVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[5], VALUES[5]);

    // Verify we don't accidentally get others
    getVersionAndVerifyMissing(ht, ROW, FAMILY, QUALIFIER, STAMPS[0]);
    getVersionAndVerifyMissing(ht, ROW, FAMILY, QUALIFIER, STAMPS[3]);
    getVersionAndVerifyMissing(ht, ROW, FAMILY, QUALIFIER, STAMPS[6]);
    scanVersionAndVerifyMissing(ht, ROW, FAMILY, QUALIFIER, STAMPS[0]);
    scanVersionAndVerifyMissing(ht, ROW, FAMILY, QUALIFIER, STAMPS[3]);
    scanVersionAndVerifyMissing(ht, ROW, FAMILY, QUALIFIER, STAMPS[6]);

    // Ensure maxVersions in query is respected
    get = new Get(ROW);
    get.addColumn(FAMILY, QUALIFIER);
    get.setMaxVersions(2);
    result = ht.get(get);
    assertNResult(result, ROW, FAMILY, QUALIFIER,
        new long [] {STAMPS[4], STAMPS[5]},
        new byte[][] {VALUES[4], VALUES[5]},
        0, 1);

    scan = new Scan(ROW);
    scan.addColumn(FAMILY, QUALIFIER);
    scan.setMaxVersions(2);
    result = getSingleScanResult(ht, scan);
    assertNResult(result, ROW, FAMILY, QUALIFIER,
        new long [] {STAMPS[4], STAMPS[5]},
        new byte[][] {VALUES[4], VALUES[5]},
        0, 1);


    // Add some memstore and retest

    // Insert 4 more versions of same column and a dupe
    put = new Put(ROW);
    put.add(FAMILY, QUALIFIER, STAMPS[3], VALUES[3]);
    put.add(FAMILY, QUALIFIER, STAMPS[6], VALUES[6]);
    put.add(FAMILY, QUALIFIER, STAMPS[7], VALUES[7]);
    put.add(FAMILY, QUALIFIER, STAMPS[8], VALUES[8]);
    ht.put(put);

    // Ensure maxVersions in query is respected
    get = new Get(ROW);
    get.addColumn(FAMILY, QUALIFIER);
    get.setMaxVersions();
    result = ht.get(get);
    assertNResult(result, ROW, FAMILY, QUALIFIER,
        new long [] {STAMPS[1], STAMPS[2], STAMPS[3], STAMPS[4], STAMPS[5], STAMPS[6], STAMPS[7], STAMPS[8]},
        new byte[][] {VALUES[1], VALUES[2], VALUES[3], VALUES[4], VALUES[5], VALUES[6], VALUES[7], VALUES[8]},
        0, 7);

    scan = new Scan(ROW);
    scan.addColumn(FAMILY, QUALIFIER);
    scan.setMaxVersions();
    result = getSingleScanResult(ht, scan);
    assertNResult(result, ROW, FAMILY, QUALIFIER,
        new long [] {STAMPS[1], STAMPS[2], STAMPS[3], STAMPS[4], STAMPS[5], STAMPS[6], STAMPS[7], STAMPS[8]},
        new byte[][] {VALUES[1], VALUES[2], VALUES[3], VALUES[4], VALUES[5], VALUES[6], VALUES[7], VALUES[8]},
        0, 7);

    get = new Get(ROW);
    get.setMaxVersions();
    result = ht.get(get);
    assertNResult(result, ROW, FAMILY, QUALIFIER,
        new long [] {STAMPS[1], STAMPS[2], STAMPS[3], STAMPS[4], STAMPS[5], STAMPS[6], STAMPS[7], STAMPS[8]},
        new byte[][] {VALUES[1], VALUES[2], VALUES[3], VALUES[4], VALUES[5], VALUES[6], VALUES[7], VALUES[8]},
        0, 7);

    scan = new Scan(ROW);
    scan.setMaxVersions();
    result = getSingleScanResult(ht, scan);
    assertNResult(result, ROW, FAMILY, QUALIFIER,
        new long [] {STAMPS[1], STAMPS[2], STAMPS[3], STAMPS[4], STAMPS[5], STAMPS[6], STAMPS[7], STAMPS[8]},
        new byte[][] {VALUES[1], VALUES[2], VALUES[3], VALUES[4], VALUES[5], VALUES[6], VALUES[7], VALUES[8]},
        0, 7);

    // Verify we can get each one properly
    getVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[1], VALUES[1]);
    getVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[2], VALUES[2]);
    getVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[4], VALUES[4]);
    getVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[7], VALUES[7]);
    scanVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[1], VALUES[1]);
    scanVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[2], VALUES[2]);
    scanVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[4], VALUES[4]);
    scanVersionAndVerify(ht, ROW, FAMILY, QUALIFIER, STAMPS[7], VALUES[7]);

    // Verify we don't accidentally get others
    getVersionAndVerifyMissing(ht, ROW, FAMILY, QUALIFIER, STAMPS[0]);
    getVersionAndVerifyMissing(ht, ROW, FAMILY, QUALIFIER, STAMPS[9]);
    scanVersionAndVerifyMissing(ht, ROW, FAMILY, QUALIFIER, STAMPS[0]);
    scanVersionAndVerifyMissing(ht, ROW, FAMILY, QUALIFIER, STAMPS[9]);

    // Ensure maxVersions of table is respected

    TEST_UTIL.flush();

    // Insert 4 more versions of same column and a dupe
    put = new Put(ROW);
    put.add(FAMILY, QUALIFIER, STAMPS[9], VALUES[9]);
    put.add(FAMILY, QUALIFIER, STAMPS[11], VALUES[11]);
    put.add(FAMILY, QUALIFIER, STAMPS[13], VALUES[13]);
    put.add(FAMILY, QUALIFIER, STAMPS[15], VALUES[15]);
    ht.put(put);

    get = new Get(ROW);
    get.addColumn(FAMILY, QUALIFIER);
    get.setMaxVersions(Integer.MAX_VALUE);
    result = ht.get(get);
    assertNResult(result, ROW, FAMILY, QUALIFIER,
        new long [] {STAMPS[3], STAMPS[4], STAMPS[5], STAMPS[6], STAMPS[7], STAMPS[8], STAMPS[9], STAMPS[11], STAMPS[13], STAMPS[15]},
        new byte[][] {VALUES[3], VALUES[4], VALUES[5], VALUES[6], VALUES[7], VALUES[8], VALUES[9], VALUES[11], VALUES[13], VALUES[15]},
        0, 9);

    scan = new Scan(ROW);
    scan.addColumn(FAMILY, QUALIFIER);
    scan.setMaxVersions(Integer.MAX_VALUE);
    result = getSingleScanResult(ht, scan);
    assertNResult(result, ROW, FAMILY, QUALIFIER,
        new long [] {STAMPS[3], STAMPS[4], STAMPS[5], STAMPS[6], STAMPS[7], STAMPS[8], STAMPS[9], STAMPS[11], STAMPS[13], STAMPS[15]},
        new byte[][] {VALUES[3], VALUES[4], VALUES[5], VALUES[6], VALUES[7], VALUES[8], VALUES[9], VALUES[11], VALUES[13], VALUES[15]},
        0, 9);

    // Delete a version in the memstore and a version in a storefile
    Delete delete = new Delete(ROW);
    delete.deleteColumn(FAMILY, QUALIFIER, STAMPS[11]);
    delete.deleteColumn(FAMILY, QUALIFIER, STAMPS[7]);
    ht.delete(delete);

    // Test that it's gone
    get = new Get(ROW);
    get.addColumn(FAMILY, QUALIFIER);
    get.setMaxVersions(Integer.MAX_VALUE);
    result = ht.get(get);
    assertNResult(result, ROW, FAMILY, QUALIFIER,
        new long [] {STAMPS[1], STAMPS[2], STAMPS[3], STAMPS[4], STAMPS[5], STAMPS[6], STAMPS[8], STAMPS[9], STAMPS[13], STAMPS[15]},
        new byte[][] {VALUES[1], VALUES[2], VALUES[3], VALUES[4], VALUES[5], VALUES[6], VALUES[8], VALUES[9], VALUES[13], VALUES[15]},
        0, 9);

    scan = new Scan(ROW);
    scan.addColumn(FAMILY, QUALIFIER);
    scan.setMaxVersions(Integer.MAX_VALUE);
    result = getSingleScanResult(ht, scan);
    assertNResult(result, ROW, FAMILY, QUALIFIER,
        new long [] {STAMPS[1], STAMPS[2], STAMPS[3], STAMPS[4], STAMPS[5], STAMPS[6], STAMPS[8], STAMPS[9], STAMPS[13], STAMPS[15]},
        new byte[][] {VALUES[1], VALUES[2], VALUES[3], VALUES[4], VALUES[5], VALUES[6], VALUES[8], VALUES[9], VALUES[13], VALUES[15]},
        0, 9);

  }

  @Test
  public void testVersionLimits() throws Exception {
    byte [] TABLE = Bytes.toBytes("testVersionLimits");
    byte [][] FAMILIES = makeNAscii(FAMILY, 3);
    int [] LIMITS = {1,3,5};
    long [] STAMPS = makeStamps(10);
    byte [][] VALUES = makeNAscii(VALUE, 10);
    HTable ht = TEST_UTIL.createTable(TABLE, FAMILIES, LIMITS);

    // Insert limit + 1 on each family
    Put put = new Put(ROW);
    put.add(FAMILIES[0], QUALIFIER, STAMPS[0], VALUES[0]);
    put.add(FAMILIES[0], QUALIFIER, STAMPS[1], VALUES[1]);
    put.add(FAMILIES[1], QUALIFIER, STAMPS[0], VALUES[0]);
    put.add(FAMILIES[1], QUALIFIER, STAMPS[1], VALUES[1]);
    put.add(FAMILIES[1], QUALIFIER, STAMPS[2], VALUES[2]);
    put.add(FAMILIES[1], QUALIFIER, STAMPS[3], VALUES[3]);
    put.add(FAMILIES[2], QUALIFIER, STAMPS[0], VALUES[0]);
    put.add(FAMILIES[2], QUALIFIER, STAMPS[1], VALUES[1]);
    put.add(FAMILIES[2], QUALIFIER, STAMPS[2], VALUES[2]);
    put.add(FAMILIES[2], QUALIFIER, STAMPS[3], VALUES[3]);
    put.add(FAMILIES[2], QUALIFIER, STAMPS[4], VALUES[4]);
    put.add(FAMILIES[2], QUALIFIER, STAMPS[5], VALUES[5]);
    put.add(FAMILIES[2], QUALIFIER, STAMPS[6], VALUES[6]);
    ht.put(put);

    // Verify we only get the right number out of each

    // Family0

    Get get = new Get(ROW);
    get.addColumn(FAMILIES[0], QUALIFIER);
    get.setMaxVersions(Integer.MAX_VALUE);
    Result result = ht.get(get);
    assertNResult(result, ROW, FAMILIES[0], QUALIFIER,
        new long [] {STAMPS[1]},
        new byte[][] {VALUES[1]},
        0, 0);

    get = new Get(ROW);
    get.addFamily(FAMILIES[0]);
    get.setMaxVersions(Integer.MAX_VALUE);
    result = ht.get(get);
    assertNResult(result, ROW, FAMILIES[0], QUALIFIER,
        new long [] {STAMPS[1]},
        new byte[][] {VALUES[1]},
        0, 0);

    Scan scan = new Scan(ROW);
    scan.addColumn(FAMILIES[0], QUALIFIER);
    scan.setMaxVersions(Integer.MAX_VALUE);
    result = getSingleScanResult(ht, scan);
    assertNResult(result, ROW, FAMILIES[0], QUALIFIER,
        new long [] {STAMPS[1]},
        new byte[][] {VALUES[1]},
        0, 0);

    scan = new Scan(ROW);
    scan.addFamily(FAMILIES[0]);
    scan.setMaxVersions(Integer.MAX_VALUE);
    result = getSingleScanResult(ht, scan);
    assertNResult(result, ROW, FAMILIES[0], QUALIFIER,
        new long [] {STAMPS[1]},
        new byte[][] {VALUES[1]},
        0, 0);

    // Family1

    get = new Get(ROW);
    get.addColumn(FAMILIES[1], QUALIFIER);
    get.setMaxVersions(Integer.MAX_VALUE);
    result = ht.get(get);
    assertNResult(result, ROW, FAMILIES[1], QUALIFIER,
        new long [] {STAMPS[1], STAMPS[2], STAMPS[3]},
        new byte[][] {VALUES[1], VALUES[2], VALUES[3]},
        0, 2);

    get = new Get(ROW);
    get.addFamily(FAMILIES[1]);
    get.setMaxVersions(Integer.MAX_VALUE);
    result = ht.get(get);
    assertNResult(result, ROW, FAMILIES[1], QUALIFIER,
        new long [] {STAMPS[1], STAMPS[2], STAMPS[3]},
        new byte[][] {VALUES[1], VALUES[2], VALUES[3]},
        0, 2);

    scan = new Scan(ROW);
    scan.addColumn(FAMILIES[1], QUALIFIER);
    scan.setMaxVersions(Integer.MAX_VALUE);
    result = getSingleScanResult(ht, scan);
    assertNResult(result, ROW, FAMILIES[1], QUALIFIER,
        new long [] {STAMPS[1], STAMPS[2], STAMPS[3]},
        new byte[][] {VALUES[1], VALUES[2], VALUES[3]},
        0, 2);

    scan = new Scan(ROW);
    scan.addFamily(FAMILIES[1]);
    scan.setMaxVersions(Integer.MAX_VALUE);
    result = getSingleScanResult(ht, scan);
    assertNResult(result, ROW, FAMILIES[1], QUALIFIER,
        new long [] {STAMPS[1], STAMPS[2], STAMPS[3]},
        new byte[][] {VALUES[1], VALUES[2], VALUES[3]},
        0, 2);

    // Family2

    get = new Get(ROW);
    get.addColumn(FAMILIES[2], QUALIFIER);
    get.setMaxVersions(Integer.MAX_VALUE);
    result = ht.get(get);
    assertNResult(result, ROW, FAMILIES[2], QUALIFIER,
        new long [] {STAMPS[2], STAMPS[3], STAMPS[4], STAMPS[5], STAMPS[6]},
        new byte[][] {VALUES[2], VALUES[3], VALUES[4], VALUES[5], VALUES[6]},
        0, 4);

    get = new Get(ROW);
    get.addFamily(FAMILIES[2]);
    get.setMaxVersions(Integer.MAX_VALUE);
    result = ht.get(get);
    assertNResult(result, ROW, FAMILIES[2], QUALIFIER,
        new long [] {STAMPS[2], STAMPS[3], STAMPS[4], STAMPS[5], STAMPS[6]},
        new byte[][] {VALUES[2], VALUES[3], VALUES[4], VALUES[5], VALUES[6]},
        0, 4);

    scan = new Scan(ROW);
    scan.addColumn(FAMILIES[2], QUALIFIER);
    scan.setMaxVersions(Integer.MAX_VALUE);
    result = getSingleScanResult(ht, scan);
    assertNResult(result, ROW, FAMILIES[2], QUALIFIER,
        new long [] {STAMPS[2], STAMPS[3], STAMPS[4], STAMPS[5], STAMPS[6]},
        new byte[][] {VALUES[2], VALUES[3], VALUES[4], VALUES[5], VALUES[6]},
        0, 4);

    scan = new Scan(ROW);
    scan.addFamily(FAMILIES[2]);
    scan.setMaxVersions(Integer.MAX_VALUE);
    result = getSingleScanResult(ht, scan);
    assertNResult(result, ROW, FAMILIES[2], QUALIFIER,
        new long [] {STAMPS[2], STAMPS[3], STAMPS[4], STAMPS[5], STAMPS[6]},
        new byte[][] {VALUES[2], VALUES[3], VALUES[4], VALUES[5], VALUES[6]},
        0, 4);

    // Try all families

    get = new Get(ROW);
    get.setMaxVersions(Integer.MAX_VALUE);
    result = ht.get(get);
    assertTrue("Expected 9 keys but received " + result.size(),
        result.size() == 9);

    get = new Get(ROW);
    get.addFamily(FAMILIES[0]);
    get.addFamily(FAMILIES[1]);
    get.addFamily(FAMILIES[2]);
    get.setMaxVersions(Integer.MAX_VALUE);
    result = ht.get(get);
    assertTrue("Expected 9 keys but received " + result.size(),
        result.size() == 9);

    get = new Get(ROW);
    get.addColumn(FAMILIES[0], QUALIFIER);
    get.addColumn(FAMILIES[1], QUALIFIER);
    get.addColumn(FAMILIES[2], QUALIFIER);
    get.setMaxVersions(Integer.MAX_VALUE);
    result = ht.get(get);
    assertTrue("Expected 9 keys but received " + result.size(),
        result.size() == 9);

    scan = new Scan(ROW);
    scan.setMaxVersions(Integer.MAX_VALUE);
    result = getSingleScanResult(ht, scan);
    assertTrue("Expected 9 keys but received " + result.size(),
        result.size() == 9);

    scan = new Scan(ROW);
    scan.setMaxVersions(Integer.MAX_VALUE);
    scan.addFamily(FAMILIES[0]);
    scan.addFamily(FAMILIES[1]);
    scan.addFamily(FAMILIES[2]);
    result = getSingleScanResult(ht, scan);
    assertTrue("Expected 9 keys but received " + result.size(),
        result.size() == 9);

    scan = new Scan(ROW);
    scan.setMaxVersions(Integer.MAX_VALUE);
    scan.addColumn(FAMILIES[0], QUALIFIER);
    scan.addColumn(FAMILIES[1], QUALIFIER);
    scan.addColumn(FAMILIES[2], QUALIFIER);
    result = getSingleScanResult(ht, scan);
    assertTrue("Expected 9 keys but received " + result.size(),
        result.size() == 9);

  }

  @Test
  public void testDeletes() throws Exception {
    byte [] TABLE = Bytes.toBytes("testDeletes");

    byte [][] ROWS = makeNAscii(ROW, 6);
    byte [][] FAMILIES = makeNAscii(FAMILY, 3);
    byte [][] VALUES = makeN(VALUE, 5);
    long [] ts = {1000, 2000, 3000, 4000, 5000};

    HTable ht = TEST_UTIL.createTable(TABLE, FAMILIES);

    Put put = new Put(ROW);
    put.add(FAMILIES[0], QUALIFIER, ts[0], VALUES[0]);
    put.add(FAMILIES[0], QUALIFIER, ts[1], VALUES[1]);
    ht.put(put);

    Delete delete = new Delete(ROW);
    delete.deleteFamily(FAMILIES[0], ts[0]);
    ht.delete(delete);

    Get get = new Get(ROW);
    get.addFamily(FAMILIES[0]);
    get.setMaxVersions(Integer.MAX_VALUE);
    Result result = ht.get(get);
    assertNResult(result, ROW, FAMILIES[0], QUALIFIER,
        new long [] {ts[1]},
        new byte[][] {VALUES[1]},
        0, 0);

    Scan scan = new Scan(ROW);
    scan.addFamily(FAMILIES[0]);
    scan.setMaxVersions(Integer.MAX_VALUE);
    result = getSingleScanResult(ht, scan);
    assertNResult(result, ROW, FAMILIES[0], QUALIFIER,
        new long [] {ts[1]},
        new byte[][] {VALUES[1]},
        0, 0);

    // Test delete latest version
    put = new Put(ROW);
    put.add(FAMILIES[0], QUALIFIER, ts[4], VALUES[4]);
    put.add(FAMILIES[0], QUALIFIER, ts[2], VALUES[2]);
    put.add(FAMILIES[0], QUALIFIER, ts[3], VALUES[3]);
    put.add(FAMILIES[0], null, ts[4], VALUES[4]);
    put.add(FAMILIES[0], null, ts[2], VALUES[2]);
    put.add(FAMILIES[0], null, ts[3], VALUES[3]);
    ht.put(put);

    delete = new Delete(ROW);
    delete.deleteColumn(FAMILIES[0], QUALIFIER); // ts[4]
    ht.delete(delete);

    get = new Get(ROW);
    get.addColumn(FAMILIES[0], QUALIFIER);
    get.setMaxVersions(Integer.MAX_VALUE);
    result = ht.get(get);
    assertNResult(result, ROW, FAMILIES[0], QUALIFIER,
        new long [] {ts[1], ts[2], ts[3]},
        new byte[][] {VALUES[1], VALUES[2], VALUES[3]},
        0, 2);

    scan = new Scan(ROW);
    scan.addColumn(FAMILIES[0], QUALIFIER);
    scan.setMaxVersions(Integer.MAX_VALUE);
    result = getSingleScanResult(ht, scan);
    assertNResult(result, ROW, FAMILIES[0], QUALIFIER,
        new long [] {ts[1], ts[2], ts[3]},
        new byte[][] {VALUES[1], VALUES[2], VALUES[3]},
        0, 2);

    // Test for HBASE-1847
    delete = new Delete(ROW);
    delete.deleteColumn(FAMILIES[0], null);
    ht.delete(delete);

    // Cleanup null qualifier
    delete = new Delete(ROW);
    delete.deleteColumns(FAMILIES[0], null);
    ht.delete(delete);

    // Expected client behavior might be that you can re-put deleted values
    // But alas, this is not to be.  We can't put them back in either case.

    put = new Put(ROW);
    put.add(FAMILIES[0], QUALIFIER, ts[0], VALUES[0]); // 1000
    put.add(FAMILIES[0], QUALIFIER, ts[4], VALUES[4]); // 5000
    ht.put(put);


    // It used to be due to the internal implementation of Get, that
    // the Get() call would return ts[4] UNLIKE
