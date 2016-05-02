/*
 * Copyright 2011 The Apache Software Foundation
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
package org.apache.hadoop.hbase.regionserver;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.DoNotRetryIOException;
import org.apache.hadoop.hbase.DroppedSnapshotException;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HConstants.OperationStatusCode;
import org.apache.hadoop.hbase.HDFSBlocksDistribution;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.NotServingRegionException;
import org.apache.hadoop.hbase.UnknownScannerException;
import org.apache.hadoop.hbase.client.Append;
import org.apache.hadoop.hbase.client.RowMutation;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.IsolationLevel;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.client.RowLock;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.coprocessor.Exec;
import org.apache.hadoop.hbase.client.coprocessor.ExecResult;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.IncompatibleFilterException;
import org.apache.hadoop.hbase.filter.WritableByteArrayComparable;
import org.apache.hadoop.hbase.io.HeapSize;
import org.apache.hadoop.hbase.io.TimeRange;
import org.apache.hadoop.hbase.io.hfile.BlockCache;
import org.apache.hadoop.hbase.io.hfile.CacheConfig;
import org.apache.hadoop.hbase.ipc.CoprocessorProtocol;
import org.apache.hadoop.hbase.ipc.HBaseRPC;
import org.apache.hadoop.hbase.monitoring.MonitoredTask;
import org.apache.hadoop.hbase.monitoring.TaskMonitor;
import org.apache.hadoop.hbase.regionserver.compactions.CompactionRequest;
import org.apache.hadoop.hbase.regionserver.metrics.SchemaMetrics;
import org.apache.hadoop.hbase.regionserver.wal.HLog;
import org.apache.hadoop.hbase.regionserver.wal.HLogKey;
import org.apache.hadoop.hbase.regionserver.wal.WALEdit;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.CancelableProgressable;
import org.apache.hadoop.hbase.util.ClassSize;
import org.apache.hadoop.hbase.util.CompressionTest;
import org.apache.hadoop.hbase.util.EnvironmentEdgeManager;
import org.apache.hadoop.hbase.util.FSUtils;
import org.apache.hadoop.hbase.util.HashedBytes;
import org.apache.hadoop.hbase.util.Pair;
import org.apache.hadoop.hbase.util.Threads;
import org.apache.hadoop.hbase.util.Writables;
import org.apache.hadoop.io.MultipleIOException;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.util.StringUtils;
import org.cliffc.high_scale_lib.Counter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.MutableClassToInstanceMap;

/**
 * HRegion stores data for a certain region of a table.  It stores all columns
 * for each row. A given table consists of one or more HRegions.
 *
 * <p>We maintain multiple HStores for a single HRegion.
 *
 * <p>An Store is a set of rows with some column data; together,
 * they make up all the data for the rows.
 *
 * <p>Each HRegion has a 'startKey' and 'endKey'.
 * <p>The first is inclusive, the second is exclusive (except for
 * the final region)  The endKey of region 0 is the same as
 * startKey for region 1 (if it exists).  The startKey for the
 * first region is null. The endKey for the final region is null.
 *
 * <p>Locking at the HRegion level serves only one purpose: preventing the
 * region from being closed (and consequently split) while other operations
 * are ongoing. Each row level operation obtains both a row lock and a region
 * read lock for the duration of the operation. While a scanner is being
 * constructed, getScanner holds a read lock. If the scanner is successfully
 * constructed, it holds a read lock until it is closed. A close takes out a
 * write lock and consequently will block for ongoing operations and will block
 * new operations from starting while the close is in progress.
 *
 * <p>An HRegion is defined by its table and its key extent.
 *
 * <p>It consists of at least one Store.  The number of Stores should be
 * configurable, so that data which is accessed together is stored in the same
 * Store.  Right now, we approximate that by building a single Store for
 * each column family.  (This config info will be communicated via the
 * tabledesc.)
 *
 * <p>The HTableDescriptor contains metainfo about the HRegion's table.
 * regionName is a unique identifier for this HRegion. (startKey, endKey]
 * defines the keyspace for this HRegion.
 */
public class HRegion implements HeapSize { // , Writable{
  public static final Log LOG = LogFactory.getLog(HRegion.class);
  static final String MERGEDIR = "merges";

  final AtomicBoolean closed = new AtomicBoolean(false);
  /* Closing can take some time; use the closing flag if there is stuff we don't
   * want to do while in closing state; e.g. like offer this region up to the
   * master as a region to close if the carrying regionserver is overloaded.
   * Once set, it is never cleared.
   */
  final AtomicBoolean closing = new AtomicBoolean(false);

  //////////////////////////////////////////////////////////////////////////////
  // Members
  //////////////////////////////////////////////////////////////////////////////

  private final ConcurrentHashMap<HashedBytes, CountDownLatch> lockedRows =
    new ConcurrentHashMap<HashedBytes, CountDownLatch>();
  private final ConcurrentHashMap<Integer, HashedBytes> lockIds =
    new ConcurrentHashMap<Integer, HashedBytes>();
  private final AtomicInteger lockIdGenerator = new AtomicInteger(1);
  static private Random rand = new Random();

  protected final Map<byte [], Store> stores =
    new ConcurrentSkipListMap<byte [], Store>(Bytes.BYTES_RAWCOMPARATOR);

  // Registered region protocol handlers
  private ClassToInstanceMap<CoprocessorProtocol>
      protocolHandlers = MutableClassToInstanceMap.create();
  
  private Map<String, Class<? extends CoprocessorProtocol>>
      protocolHandlerNames = Maps.newHashMap();

  /**
   * Temporary subdirectory of the region directory used for compaction output.
   */
  public static final String REGION_TEMP_SUBDIR = ".tmp";

  //These variable are just used for getting data out of the region, to test on
  //client side
  // private int numStores = 0;
  // private int [] storeSize = null;
  // private byte [] name = null;

  final AtomicLong memstoreSize = new AtomicLong(0);

  final Counter readRequestsCount = new Counter();
  final Counter writeRequestsCount = new Counter();

  /**
   * The directory for the table this region is part of.
   * This directory contains the directory for this region.
   */
  final Path tableDir;

  final HLog log;
  final FileSystem fs;
  final Configuration conf;
  final int rowLockWaitDuration;
  static final int DEFAULT_ROWLOCK_WAIT_DURATION = 30000;
  final HRegionInfo regionInfo;
  final Path regiondir;
  KeyValue.KVComparator comparator;

  private ConcurrentHashMap<RegionScanner, Long> scannerReadPoints;

  /*
   * @return The smallest mvcc readPoint across all the scanners in this
   * region. Writes older than this readPoint, are included  in every
   * read operation.
   */
  public long getSmallestReadPoint() {
    long minimumReadPoint;
    // We need to ensure that while we are calculating the smallestReadPoint
    // no new RegionScanners can grab a readPoint that we are unaware of.
    // We achieve this by synchronizing on the scannerReadPoints object.
    synchronized(scannerReadPoints) {
      minimumReadPoint = mvcc.memstoreReadPoint();

      for (Long readPoint: this.scannerReadPoints.values()) {
        if (readPoint < minimumReadPoint) {
          minimumReadPoint = readPoint;
        }
      }
    }
    return minimumReadPoint;
  }
  /*
   * Data structure of write state flags used coordinating flushes,
   * compactions and closes.
   */
  static class WriteState {
    // Set while a memstore flush is happening.
    volatile boolean flushing = false;
    // Set when a flush has been requested.
    volatile boolean flushRequested = false;
    // Number of compactions running.
    volatile int compacting = 0;
    // Gets set in close. If set, cannot compact or flush again.
    volatile boolean writesEnabled = true;
    // Set if region is read-only
    volatile boolean readOnly = false;

    /**
     * Set flags that make this region read-only.
     *
     * @param onOff flip value for region r/o setting
     */
    synchronized void setReadOnly(final boolean onOff) {
      this.writesEnabled = !onOff;
      this.readOnly = onOff;
    }

    boolean isReadOnly() {
      return this.readOnly;
    }

    boolean isFlushRequested() {
      return this.flushRequested;
    }

    static final long HEAP_SIZE = ClassSize.align(
        ClassSize.OBJECT + 5 * Bytes.SIZEOF_BOOLEAN);
  }

  final WriteState writestate = new WriteState();

  long memstoreFlushSize;
  final long timestampSlop;
  private volatile long lastFlushTime;
  final RegionServerServices rsServices;
  private List<Pair<Long, Long>> recentFlushes = new ArrayList<Pair<Long,Long>>();
  private long blockingMemStoreSize;
  final long threadWakeFrequency;
  // Used to guard closes
  final ReentrantReadWriteLock lock =
    new ReentrantReadWriteLock();

  // Stop updates lock
  private final ReentrantReadWriteLock updatesLock =
    new ReentrantReadWriteLock();
  private boolean splitRequest;
  private byte[] explicitSplitPoint = null;

  private final MultiVersionConsistencyControl mvcc =
      new MultiVersionConsistencyControl();

  // Coprocessor host
  private RegionCoprocessorHost coprocessorHost;

  /**
   * Name of the region info file that resides just under the region directory.
   */
  public final static String REGIONINFO_FILE = ".regioninfo";
  private HTableDescriptor htableDescriptor = null;
  private RegionSplitPolicy splitPolicy;

  // for simple numeric metrics (# of blocks read from block cache)
  public static final ConcurrentMap<String, AtomicLong> numericMetrics = new ConcurrentHashMap<String, AtomicLong>();

  // for simple numeric metrics (current block cache size)
  // These ones are not reset to zero when queried, unlike the previous.
  public static final ConcurrentMap<String, AtomicLong> numericPersistentMetrics = new ConcurrentHashMap<String, AtomicLong>();

  /**
   * Used for metrics where we want track a metrics (such as latency) over a
   * number of operations.
   */
  public static final ConcurrentMap<String, Pair<AtomicLong, AtomicInteger>>
      timeVaryingMetrics = new ConcurrentHashMap<String, 
          Pair<AtomicLong, AtomicInteger>>();

  public static void incrNumericMetric(String key, long amount) {
    AtomicLong oldVal = numericMetrics.get(key);
    if (oldVal == null) {
      oldVal = numericMetrics.putIfAbsent(key, new AtomicLong(amount));
      if (oldVal == null)
        return;
    }
    oldVal.addAndGet(amount);
  }

  public static void setNumericMetric(String key, long amount) {
    numericMetrics.put(key, new AtomicLong(amount));
  }

  public static void incrTimeVaryingMetric(String key, long amount) {
    Pair<AtomicLong, AtomicInteger> oldVal = timeVaryingMetrics.get(key);
    if (oldVal == null) {
      oldVal = timeVaryingMetrics.putIfAbsent(key,
          new Pair<AtomicLong, AtomicInteger>(new AtomicLong(amount),
              new AtomicInteger(1)));
      if (oldVal == null)
        return;
    }
    oldVal.getFirst().addAndGet(amount); // total time
    oldVal.getSecond().incrementAndGet(); // increment ops by 1
  }

  public static void incrNumericPersistentMetric(String key, long amount) {
    AtomicLong oldVal = numericPersistentMetrics.get(key);
    if (oldVal == null) {
      oldVal = numericPersistentMetrics
          .putIfAbsent(key, new AtomicLong(amount));
      if (oldVal == null)
        return;
    }
    oldVal.addAndGet(amount);
  }

  public static long getNumericMetric(String key) {
    AtomicLong m = numericMetrics.get(key);
    if (m == null)
      return 0;
    return m.get();
  }

  public static Pair<Long, Integer> getTimeVaryingMetric(String key) {
    Pair<AtomicLong, AtomicInteger> pair = timeVaryingMetrics.get(key);
    if (pair == null) {
      return new Pair<Long, Integer>(0L, 0);
    }

    return new Pair<Long, Integer>(pair.getFirst().get(),
        pair.getSecond().get());
  }

  static long getNumericPersistentMetric(String key) {
    AtomicLong m = numericPersistentMetrics.get(key);
    if (m == null)
      return 0;
    return m.get();
  }

  /**
   * Should only be used for testing purposes
   */
  public HRegion(){
    this.tableDir = null;
    this.blockingMemStoreSize = 0L;
    this.conf = null;
    this.rowLockWaitDuration = DEFAULT_ROWLOCK_WAIT_DURATION;
    this.rsServices = null;
    this.fs = null;
    this.timestampSlop = HConstants.LATEST_TIMESTAMP;
    this.memstoreFlushSize = 0L;
    this.log = null;
    this.regiondir = null;
    this.regionInfo = null;
    this.htableDescriptor = null;
    this.threadWakeFrequency = 0L;
    this.coprocessorHost = null;
    this.scannerReadPoints = new ConcurrentHashMap<RegionScanner, Long>();
  }

  /**
   * HRegion constructor.  his constructor should only be used for testing and
   * extensions.  Instances of HRegion should be instantiated with the
   * {@link HRegion#newHRegion(Path, HLog, FileSystem, Configuration, HRegionInfo, HTableDescriptor, RegionServerServices)} method.
   *
   *
   * @param tableDir qualified path of directory where region should be located,
   * usually the table directory.
   * @param log The HLog is the outbound log for any updates to the HRegion
   * (There's a single HLog for all the HRegions on a single HRegionServer.)
   * The log file is a logfile from the previous execution that's
   * custom-computed for this HRegion. The HRegionServer computes and sorts the
   * appropriate log info for this HRegion. If there is a previous log file
   * (implying that the HRegion has been written-to before), then read it from
   * the supplied path.
   * @param fs is the filesystem.
   * @param conf is global configuration settings.
   * @param regionInfo - HRegionInfo that describes the region
   * is new), then read them from the supplied path.
   * @param rsServices reference to {@link RegionServerServices} or null
   *
   * @see HRegion#newHRegion(Path, HLog, FileSystem, Configuration, HRegionInfo, HTableDescriptor, RegionServerServices)
   */
  public HRegion(Path tableDir, HLog log, FileSystem fs, Configuration conf,
    final HRegionInfo regionInfo, final HTableDescriptor htd,
      RegionServerServices rsServices) {
    this.tableDir = tableDir;
    this.comparator = regionInfo.getComparator();
    this.log = log;
    this.fs = fs;
    this.conf = conf;
    this.rowLockWaitDuration = conf.getInt("hbase.rowlock.wait.duration",
                    DEFAULT_ROWLOCK_WAIT_DURATION);
    this.regionInfo = regionInfo;
    this.htableDescriptor = htd;
    this.rsServices = rsServices;
    this.threadWakeFrequency = conf.getLong(HConstants.THREAD_WAKE_FREQUENCY,
        10 * 1000);
    String encodedNameStr = this.regionInfo.getEncodedName();
    setHTableSpecificConf();
    this.regiondir = getRegionDir(this.tableDir, encodedNameStr);
    this.scannerReadPoints = new ConcurrentHashMap<RegionScanner, Long>();

    /*
     * timestamp.slop provides a server-side constraint on the timestamp. This
     * assumes that you base your TS around currentTimeMillis(). In this case,
     * throw an error to the user if the user-specified TS is newer than now +
     * slop. LATEST_TIMESTAMP == don't use this functionality
     */
    this.timestampSlop = conf.getLong(
        "hbase.hregion.keyvalue.timestamp.slop.millisecs",
        HConstants.LATEST_TIMESTAMP);

    // don't initialize coprocessors if not running within a regionserver
    // TODO: revisit if coprocessors should load in other cases
    if (rsServices != null) {
      this.coprocessorHost = new RegionCoprocessorHost(this, rsServices, conf);
    }
    if (LOG.isDebugEnabled()) {
      // Write out region name as string and its encoded name.
      LOG.debug("Instantiated " + this);
    }
  }

  void setHTableSpecificConf() {
    if (this.htableDescriptor == null) return;
    LOG.info("Setting up tabledescriptor config now ...");
    long flushSize = this.htableDescriptor.getMemStoreFlushSize();

    if (flushSize == HTableDescriptor.DEFAULT_MEMSTORE_FLUSH_SIZE) {
      flushSize = conf.getLong(HConstants.HREGION_MEMSTORE_FLUSH_SIZE,
         HTableDescriptor.DEFAULT_MEMSTORE_FLUSH_SIZE);
    }
    this.memstoreFlushSize = flushSize;
    this.blockingMemStoreSize = this.memstoreFlushSize *
        conf.getLong("hbase.hregion.memstore.block.multiplier", 2);
  }

  /**
   * Initialize this region.
   * @return What the next sequence (edit) id should be.
   * @throws IOException e
   */
  public long initialize() throws IOException {
    return initialize(null);
  }

  /**
   * Initialize this region.
   *
   * @param reporter Tickle every so often if initialize is taking a while.
   * @return What the next sequence (edit) id should be.
   * @throws IOException e
   */
  public long initialize(final CancelableProgressable reporter)
  throws IOException {

    MonitoredTask status = TaskMonitor.get().createStatus(
        "Initializing region " + this);

    if (coprocessorHost != null) {
      status.setStatus("Running coprocessor pre-open hook");
      coprocessorHost.preOpen();
    }

    // Write HRI to a file in case we need to recover .META.
    status.setStatus("Writing region info on filesystem");
    checkRegioninfoOnFilesystem();

    // Remove temporary data left over from old regions
    status.setStatus("Cleaning up temporary data from old regions");
    cleanupTmpDir();

    // Load in all the HStores.
    // Get minimum of the maxSeqId across all the store.
    //
    // Context: During replay we want to ensure that we do not lose any data. So, we
    // have to be conservative in how we replay logs. For each store, we calculate
    // the maxSeqId up to which the store was flushed. But, since different stores
    // could have a different maxSeqId, we choose the
    // minimum across all the stores.
    // This could potentially result in duplication of data for stores that are ahead
    // of others. ColumnTrackers in the ScanQueryMatchers do the de-duplication, so we
    // do not have to worry.
    // TODO: If there is a store that was never flushed in a long time, we could replay
    // a lot of data. Currently, this is not a problem because we flush all the stores at
    // the same time. If we move to per-cf flushing, we might want to revisit this and send
    // in a vector of maxSeqIds instead of sending in a single number, which has to be the
    // min across all the max.
    long minSeqId = -1;
    long maxSeqId = -1;
    // initialized to -1 so that we pick up MemstoreTS from column families
    long maxMemstoreTS = -1;

    if (this.htableDescriptor != null &&
        !htableDescriptor.getFamilies().isEmpty()) {
      // initialize the thread pool for opening stores in parallel.
      ThreadPoolExecutor storeOpenerThreadPool =
        getStoreOpenAndCloseThreadPool(
          "StoreOpenerThread-" + this.regionInfo.getRegionNameAsString());
      CompletionService<Store> completionService =
        new ExecutorCompletionService<Store>(storeOpenerThreadPool);

      // initialize each store in parallel
      for (final HColumnDescriptor family : htableDescriptor.getFamilies()) {
        status.setStatus("Instantiating store for column family " + family);
        completionService.submit(new Callable<Store>() {
          public Store call() throws IOException {
            return instantiateHStore(tableDir, family);
          }
        });
      }
      try {
        for (int i = 0; i < htableDescriptor.getFamilies().size(); i++) {
          Future<Store> future = completionService.take();
          Store store = future.get();

          this.stores.put(store.getColumnFamilyName().getBytes(), store);
          long storeSeqId = store.getMaxSequenceId();
          if (minSeqId == -1 || storeSeqId < minSeqId) {
            minSeqId = storeSeqId;
          }
          if (maxSeqId == -1 || storeSeqId > maxSeqId) {
            maxSeqId = storeSeqId;
          }
          long maxStoreMemstoreTS = store.getMaxMemstoreTS();
          if (maxStoreMemstoreTS > maxMemstoreTS) {
            maxMemstoreTS = maxStoreMemstoreTS;
          }
        }
      } catch (InterruptedException e) {
        throw new IOException(e);
      } catch (ExecutionException e) {
        throw new IOException(e.getCause());
      } finally {
        storeOpenerThreadPool.shutdownNow();
      }
    }
    mvcc.initialize(maxMemstoreTS + 1);
    // Recover any edits if available.
    maxSeqId = Math.max(maxSeqId, replayRecoveredEditsIfAny(
        this.regiondir, minSeqId, reporter, status));

    status.setStatus("Cleaning up detritus from prior splits");
    // Get rid of any splits or merges that were lost in-progress.  Clean out
    // these directories here on open.  We may be opening a region that was
    // being split but we crashed in the middle of it all.
    SplitTransaction.cleanupAnySplitDetritus(this);
    FSUtils.deleteDirectory(this.fs, new Path(regiondir, MERGEDIR));

    this.writestate.setReadOnly(this.htableDescriptor.isReadOnly());

    this.writestate.flushRequested = false;
    this.writestate.compacting = 0;

    // Initialize split policy
    this.splitPolicy = RegionSplitPolicy.create(this, conf);

    this.lastFlushTime = EnvironmentEdgeManager.currentTimeMillis();
    // Use maximum of log sequenceid or that which was found in stores
    // (particularly if no recovered edits, seqid will be -1).
    long nextSeqid = maxSeqId + 1;
    LOG.info("Onlined " + this.toString() + "; next sequenceid=" + nextSeqid);

    // A region can be reopened if failed a split; reset flags
    this.closing.set(false);
    this.closed.set(false);

    if (coprocessorHost != null) {
      status.setStatus("Running coprocessor post-open hooks");
      coprocessorHost.postOpen();
    }

    status.markComplete("Region opened successfully");
    return nextSeqid;
  }

  /*
   * Move any passed HStore files into place (if any).  Used to pick up split
   * files and any merges from splits and merges dirs.
   * @param initialFiles
   * @throws IOException
   */
  static void moveInitialFilesIntoPlace(final FileSystem fs,
    final Path initialFiles, final Path regiondir)
  throws IOException {
    if (initialFiles != null && fs.exists(initialFiles)) {
      if (!fs.rename(initialFiles, regiondir)) {
        LOG.warn("Unable to rename " + initialFiles + " to " + regiondir);
      }
    }
  }

  /**
   * @return True if this region has references.
   */
  public boolean hasReferences() {
    for (Store store : this.stores.values()) {
      for (StoreFile sf : store.getStorefiles()) {
        // Found a reference, return.
        if (sf.isReference()) return true;
      }
    }
    return false;
  }

  /**
   * This function will return the HDFS blocks distribution based on the data
   * captured when HFile is created
   * @return The HDFS blocks distribution for the region.
   */
  public HDFSBlocksDistribution getHDFSBlocksDistribution() {
    HDFSBlocksDistribution hdfsBlocksDistribution =
      new HDFSBlocksDistribution();
    synchronized (this.stores) {
      for (Store store : this.stores.values()) {
        for (StoreFile sf : store.getStorefiles()) {
          HDFSBlocksDistribution storeFileBlocksDistribution =
            sf.getHDFSBlockDistribution();
          hdfsBlocksDistribution.add(storeFileBlocksDistribution);
        }
      }
    }
    return hdfsBlocksDistribution;
  }

  /**
   * This is a helper function to compute HDFS block distribution on demand
   * @param conf configuration
   * @param tableDescriptor HTableDescriptor of the table
   * @param regionEncodedName encoded name of the region
   * @return The HDFS blocks distribution for the given region.
 * @throws IOException
   */
  static public HDFSBlocksDistribution computeHDFSBlocksDistribution(
    Configuration conf, HTableDescriptor tableDescriptor,
    String regionEncodedName) throws IOException {
    HDFSBlocksDistribution hdfsBlocksDistribution =
      new HDFSBlocksDistribution();
    Path tablePath = FSUtils.getTablePath(FSUtils.getRootDir(conf),
      tableDescriptor.getName());
    FileSystem fs = tablePath.getFileSystem(conf);

    for (HColumnDescriptor family: tableDescriptor.getFamilies()) {
      Path storeHomeDir = Store.getStoreHomedir(tablePath, regionEncodedName,
      family.getName());
      if (!fs.exists(storeHomeDir))continue;

      FileStatus[] hfilesStatus = null;
      hfilesStatus = fs.listStatus(storeHomeDir);

      for (FileStatus hfileStatus : hfilesStatus) {
        HDFSBlocksDistribution storeFileBlocksDistribution =
          FSUtils.computeHDFSBlocksDistribution(fs, hfileStatus, 0,
          hfileStatus.getLen());
        hdfsBlocksDistribution.add(storeFileBlocksDistribution);
      }
    }
    return hdfsBlocksDistribution;
  }

  public AtomicLong getMemstoreSize() {
    return memstoreSize;
  }

  /**
   * Increase the size of mem store in this region and the size of global mem
   * store
   * @param memStoreSize
   * @return the size of memstore in this region
   */
  public long addAndGetGlobalMemstoreSize(long memStoreSize) {
    if (this.rsServices != null) {
      RegionServerAccounting rsAccounting =
        this.rsServices.getRegionServerAccounting();

      if (rsAccounting != null) {
        rsAccounting.addAndGetGlobalMemstoreSize(memStoreSize);
      }
    }
    return this.memstoreSize.getAndAdd(memStoreSize);
  }

  /*
   * Write out an info file under the region directory.  Useful recovering
   * mangled regions.
   * @throws IOException
   */
  private void checkRegioninfoOnFilesystem() throws IOException {
    Path regioninfoPath = new Path(this.regiondir, REGIONINFO_FILE);
    if (this.fs.exists(regioninfoPath) &&
        this.fs.getFileStatus(regioninfoPath).getLen() > 0) {
      return;
    }
    // Create in tmpdir and then move into place in case we crash after
    // create but before close.  If we don't successfully close the file,
    // subsequent region reopens will fail the below because create is
    // registered in NN.
    Path tmpPath = new Path(getTmpDir(), REGIONINFO_FILE);
    FSDataOutputStream out = this.fs.create(tmpPath, true);
    try {
      this.regionInfo.write(out);
      out.write('\n');
      out.write('\n');
      out.write(Bytes.toBytes(this.regionInfo.toString()));
    } finally {
      out.close();
    }
    if (!fs.rename(tmpPath, regioninfoPath)) {
      throw new IOException("Unable to rename " + tmpPath + " to " +
        regioninfoPath);
    }
  }

  /** @return a HRegionInfo object for this region */
  public HRegionInfo getRegionInfo() {
    return this.regionInfo;
  }

  /** @return requestsCount for this region */
  public long getRequestsCount() {
    return this.readRequestsCount.get() + this.writeRequestsCount.get();
  }

  /** @return readRequestsCount for this region */
  public long getReadRequestsCount() {
    return this.readRequestsCount.get();
  }

  /** @return writeRequestsCount for this region */
  public long getWriteRequestsCount() {
    return this.writeRequestsCount.get();
  }

  /** @return true if region is closed */
  public boolean isClosed() {
    return this.closed.get();
  }

  /**
   * @return True if closing process has started.
   */
  public boolean isClosing() {
    return this.closing.get();
  }

  boolean areWritesEnabled() {
    synchronized(this.writestate) {
      return this.writestate.writesEnabled;
    }
  }

   public MultiVersionConsistencyControl getMVCC() {
     return mvcc;
   }

  /**
   * Close down this HRegion.  Flush the cache, shut down each HStore, don't
   * service any more calls.
   *
   * <p>This method could take some time to execute, so don't call it from a
   * time-sensitive thread.
   *
   * @return Vector of all the storage files that the HRegion's component
   * HStores make use of.  It's a list of all HStoreFile objects. Returns empty
   * vector if already closed and null if judged that it should not close.
   *
   * @throws IOException e
   */
  public List<StoreFile> close() throws IOException {
    return close(false);
  }

  private final Object closeLock = new Object();

  /**
   * Close down this HRegion.  Flush the cache unless abort parameter is true,
   * Shut down each HStore, don't service any more calls.
   *
   * This method could take some time to execute, so don't call it from a
   * time-sensitive thread.
   *
   * @param abort true if server is aborting (only during testing)
   * @return Vector of all the storage files that the HRegion's component
   * HStores make use of.  It's a list of HStoreFile objects.  Can be null if
   * we are not to close at this time or we are already closed.
   *
   * @throws IOException e
   */
  public List<StoreFile> close(final boolean abort) throws IOException {
    // Only allow one thread to close at a time. Serialize them so dual
    // threads attempting to close will run up against each other.
    MonitoredTask status = TaskMonitor.get().createStatus(
        "Closing region " + this +
        (abort ? " due to abort" : ""));

    status.setStatus("Waiting for close lock");
    try {
      synchronized (closeLock) {
        return doClose(abort, status);
      }
    } finally {
      status.cleanup();
    }
  }

  private List<StoreFile> doClose(
      final boolean abort, MonitoredTask status)
  throws IOException {
    if (isClosed()) {
      LOG.warn("Region " + this + " already closed");
      return null;
    }

    if (coprocessorHost != null) {
      status.setStatus("Running coprocessor pre-close hooks");
      this.coprocessorHost.preClose(abort);
    }

    status.setStatus("Disabling compacts and flushes for region");
    boolean wasFlushing = false;
    synchronized (writestate) {
      // Disable compacting and flushing by background threads for this
      // region.
      writestate.writesEnabled = false;
      wasFlushing = writestate.flushing;
      LOG.debug("Closing " + this + ": disabling compactions & flushes");
      while (writestate.compacting > 0 || writestate.flushing) {
        LOG.debug("waiting for " + writestate.compacting + " compactions" +
            (writestate.flushing ? " & cache flush" : "") +
            " to complete for region " + this);
        try {
          writestate.wait();
        } catch (InterruptedException iex) {
          // continue
        }
      }
    }
    // If we were not just flushing, is it worth doing a preflush...one
    // that will clear out of the bulk of the memstore before we put up
    // the close flag?
    if (!abort && !wasFlushing && worthPreFlushing()) {
      status.setStatus("Pre-flushing region before close");
      LOG.info("Running close preflush of " + this.getRegionNameAsString());
      internalFlushcache(status);
    }

    this.closing.set(true);
    status.setStatus("Disabling writes for close");
    lock.writeLock().lock();
    try {
      if (this.isClosed()) {
        status.abort("Already got closed by another process");
        // SplitTransaction handles the null
        return null;
      }
      LOG.debug("Updates disabled for region " + this);
      // Don't flush the cache if we are aborting
      if (!abort) {
        internalFlushcache(status);
      }

      List<StoreFile> result = new ArrayList<StoreFile>();
      if (!stores.isEmpty()) {
        // initialize the thread pool for closing stores in parallel.
        ThreadPoolExecutor storeCloserThreadPool =
          getStoreOpenAndCloseThreadPool("StoreCloserThread-"
            + this.regionInfo.getRegionNameAsString());
        CompletionService<ImmutableList<StoreFile>> completionService =
          new ExecutorCompletionService<ImmutableList<StoreFile>>(
            storeCloserThreadPool);
      
        // close each store in parallel
        for (final Store store : stores.values()) {
          completionService
              .submit(new Callable<ImmutableList<StoreFile>>() {
                public ImmutableList<StoreFile> call() throws IOException {
                  return store.close();
                }
              });
        }
        try {
          for (int i = 0; i < stores.size(); i++) {
            Future<ImmutableList<StoreFile>> future = completionService
                .take();
            ImmutableList<StoreFile> storeFileList = future.get();
            result.addAll(storeFileList);
          }
        } catch (InterruptedException e) {
          throw new IOException(e);
        } catch (ExecutionException e) {
          throw new IOException(e.getCause());
        } finally {
          storeCloserThreadPool.shutdownNow();
        }
      }
      this.closed.set(true);

      if (coprocessorHost != null) {
        status.setStatus("Running coprocessor post-close hooks");
        this.coprocessorHost.postClose(abort);
      }
      status.markComplete("Closed");
      LOG.info("Closed " + this);
      return result;
    } finally {
      lock.writeLock().unlock();
    }
  }

  protected ThreadPoolExecutor getStoreOpenAndCloseThreadPool(
      final String threadNamePrefix) {
    int numStores = Math.max(1, this.htableDescriptor.getFamilies().size());
    int maxThreads = Math.min(numStores,
        conf.getInt(HConstants.HSTORE_OPEN_AND_CLOSE_THREADS_MAX,
            HConstants.DEFAULT_HSTORE_OPEN_AND_CLOSE_THREADS_MAX));
    return getOpenAndCloseThreadPool(maxThreads, threadNamePrefix);
  }

  protected ThreadPoolExecutor getStoreFileOpenAndCloseThreadPool(
      final String threadNamePrefix) {
    int numStores = Math.max(1, this.htableDescriptor.getFamilies().size());
    int maxThreads = Math.max(1,
        conf.getInt(HConstants.HSTORE_OPEN_AND_CLOSE_THREADS_MAX,
            HConstants.DEFAULT_HSTORE_OPEN_AND_CLOSE_THREADS_MAX)
            / numStores);
    return getOpenAndCloseThreadPool(maxThreads, threadNamePrefix);
  }

  private ThreadPoolExecutor getOpenAndCloseThreadPool(int maxThreads,
      final String threadNamePrefix) {
    ThreadPoolExecutor openAndCloseThreadPool = Threads
        .getBoundedCachedThreadPool(maxThreads, 30L, TimeUnit.SECONDS,
            new ThreadFactory() {
              private int count = 1;

              public Thread newThread(Runnable r) {
                Thread t = new Thread(r, threadNamePrefix + "-" + count++);
                return t;
              }
            });
    return openAndCloseThreadPool;
  }

   /**
    * @return True if its worth doing a flush before we put up the close flag.
    */
  private boolean worthPreFlushing() {
    return this.memstoreSize.get() >
      this.conf.getLong("hbase.hregion.preclose.flush.size", 1024 * 1024 * 5);
  }

  //////////////////////////////////////////////////////////////////////////////
  // HRegion accessors
  //////////////////////////////////////////////////////////////////////////////

  /** @return start key for region */
  public byte [] getStartKey() {
    return this.regionInfo.getStartKey();
  }

  /** @return end key for region */
  public byte [] getEndKey() {
    return this.regionInfo.getEndKey();
  }

  /** @return region id */
  public long getRegionId() {
    return this.regionInfo.getRegionId();
  }

  /** @return region name */
  public byte [] getRegionName() {
    return this.regionInfo.getRegionName();
  }

  /** @return region name as string for logging */
  public String getRegionNameAsString() {
    return this.regionInfo.getRegionNameAsString();
  }

  /** @return HTableDescriptor for this region */
  public HTableDescriptor getTableDesc() {
    return this.htableDescriptor;
  }

  /** @return HLog in use for this region */
  public HLog getLog() {
    return this.log;
  }

  /** @return Configuration object */
  public Configuration getConf() {
    return this.conf;
  }

  /** @return region directory Path */
  public Path getRegionDir() {
    return this.regiondir;
  }

  /**
   * Computes the Path of the HRegion
   *
   * @param tabledir qualified path for table
   * @param name ENCODED region name
   * @return Path of HRegion directory
   */
  public static Path getRegionDir(final Path tabledir, final String name) {
    return new Path(tabledir, name);
  }

  /** @return FileSystem being used by this region */
  public FileSystem getFilesystem() {
    return this.fs;
  }

  /** @return the last time the region was flushed */
  public long getLastFlushTime() {
    return this.lastFlushTime;
  }

  /** @return info about the last flushes <time, size> */
  public List<Pair<Long,Long>> getRecentFlushInfo() {
    this.lock.readLock().lock();
    List<Pair<Long,Long>> ret = this.recentFlushes;
    this.recentFlushes = new ArrayList<Pair<Long,Long>>();
    this.lock.readLock().unlock();
    return ret;
  }

  //////////////////////////////////////////////////////////////////////////////
  // HRegion maintenance.
  //
  // These methods are meant to be called periodically by the HRegionServer for
  // upkeep.
  //////////////////////////////////////////////////////////////////////////////

  /** @return returns size of largest HStore. */
  public long getLargestHStoreSize() {
    long size = 0;
    for (Store h: stores.values()) {
      long storeSize = h.getSize();
      if (storeSize > size) {
        size = storeSize;
      }
    }
    return size;
  }

  /*
   * Do preparation for pending compaction.
   * @throws IOException
   */
  void doRegionCompactionPrep() throws IOException {
  }

  /*
   * Removes the temporary directory for this Store.
   */
  private void cleanupTmpDir() throws IOException {
    FSUtils.deleteDirectory(this.fs, getTmpDir());
  }

  /**
   * Get the temporary directory for this region. This directory
   * will have its contents removed when the region is reopened.
   */
  Path getTmpDir() {
    return new Path(getRegionDir(), REGION_TEMP_SUBDIR);
  }

  void triggerMajorCompaction() {
    for (Store h: stores.values()) {
      h.triggerMajorCompaction();
    }
  }

  /**
   * This is a helper function that compact all the stores synchronously
   * It is used by utilities and testing
   *
   * @param majorCompaction True to force a major compaction regardless of thresholds
   * @throws IOException e
   */
  void compactStores(final boolean majorCompaction)
  throws IOException {
    if (majorCompaction) {
      this.triggerMajorCompaction();
    }
    compactStores();
  }

  /**
   * This is a helper function that compact all the stores synchronously
   * It is used by utilities and testing
   *
   * @throws IOException e
   */
  public void compactStores() throws IOException {
    for(Store s : getStores().values()) {
      CompactionRequest cr = s.requestCompaction();
      if(cr != null) {
        try {
          compact(cr);
        } finally {
          s.finishRequest(cr);
        }
      }
    }
  }

  /*
   * Called by compaction thread and after region is opened to compact the
   * HStores if necessary.
   *
   * <p>This operation could block for a long time, so don't call it from a
   * time-sensitive thread.
   *
   * Note that no locking is necessary at this level because compaction only
   * conflicts with a region split, and that cannot happen because the region
   * server does them sequentially and not in parallel.
   *
   * @param cr Compaction details, obtained by requestCompaction()
   * @return whether the compaction completed
   * @throws IOException e
   */
  public boolean compact(CompactionRequest cr)
  throws IOException {
    if (cr == null) {
      return false;
    }
    if (this.closing.get() || this.closed.get()) {
      LOG.debug("Skipping compaction on " + this + " because closing/closed");
      return false;
    }
    Preconditions.checkArgument(cr.getHRegion().equals(this));
    lock.readLock().lock();
    MonitoredTask status = TaskMonitor.get().createStatus(
        "Compacting " + cr.getStore() + " in " + this);
    try {
      if (this.closed.get()) {
        LOG.debug("Skipping compaction on " + this + " because closed");
        return false;
      }
      boolean decr = true;
      try {
        synchronized (writestate) {
          if (writestate.writesEnabled) {
            ++writestate.compacting;
          } else {
            String msg = "NOT compacting region " + this + ". Writes disabled.";
            LOG.info(msg);
            status.abort(msg);
            decr = false;
            return false;
          }
        }
        LOG.info("Starting compaction on " + cr.getStore() + " in region "
            + this + (cr.getCompactSelection().isOffPeakCompaction()?" as an off-peak compaction":""));
        doRegionCompactionPrep();
        try {
          status.setStatus("Compacting store " + cr.getStore());
          cr.getStore().compact(cr);
        } catch (InterruptedIOException iioe) {
          String msg = "compaction interrupted by user";
          LOG.info(msg, iioe);
          status.abort(msg);
          return false;
        }
      } finally {
        if (decr) {
          synchronized (writestate) {
            --writestate.compacting;
            if (writestate.compacting <= 0) {
              writestate.notifyAll();
            }
          }
        }
      }
      status.markComplete("Compaction complete");
      return true;
    } finally {
      status.cleanup();
      lock.readLock().unlock();
    }
  }

  /**
   * Flush the cache.
   *
   * When this method is called the cache will be flushed unless:
   * <ol>
   *   <li>the cache is empty</li>
   *   <li>the region is closed.</li>
   *   <li>a flush is already in progress</li>
   *   <li>writes are disabled</li>
   * </ol>
   *
   * <p>This method may block for some time, so it should not be called from a
   * time-sensitive thread.
   *
   * @return true if cache was flushed
   *
   * @throws IOException general io exceptions
   * @throws DroppedSnapshotException Thrown when replay of hlog is required
   * because a Snapshot was not properly persisted.
   */
  public boolean flushcache() throws IOException {
    // fail-fast instead of waiting on the lock
    if (this.closing.get()) {
      LOG.debug("Skipping flush on " + this + " because closing");
      return false;
    }
    MonitoredTask status = TaskMonitor.get().createStatus("Flushing " + this);
    status.setStatus("Acquiring readlock on region");
    lock.readLock().lock();
    try {
      if (this.closed.get()) {
        LOG.debug("Skipping flush on " + this + " because closed");
        status.abort("Skipped: closed");
        return false;
      }
      if (coprocessorHost != null) {
        status.setStatus("Running coprocessor pre-flush hooks");
        coprocessorHost.preFlush();
      }
      try {
        synchronized (writestate) {
          if (!writestate.flushing && writestate.writesEnabled) {
            this.writestate.flushing = true;
          } else {
            if (LOG.isDebugEnabled()) {
              LOG.debug("NOT flushing memstore for region " + this +
                  ", flushing=" +
                  writestate.flushing + ", writesEnabled=" +
                  writestate.writesEnabled);
            }
            status.abort("Not flushing since " +
                (writestate.flushing ? "already flushing" : "writes not enabled"));
            return false;
          }
        }
        boolean result = internalFlushcache(status);

        if (coprocessorHost != null) {
          status.setStatus("Running post-flush coprocessor hooks");
          coprocessorHost.postFlush();
        }

        status.markComplete("Flush successful");
        return result;
      } finally {
        synchronized (writestate) {
          writestate.flushing = false;
          this.writestate.flushRequested = false;
          writestate.notifyAll();
        }
      }
    } finally {
      lock.readLock().unlock();
      status.cleanup();
    }
  }

  /**
   * Flush the memstore.
   *
   * Flushing the memstore is a little tricky. We have a lot of updates in the
   * memstore, all of which have also been written to the log. We need to
   * write those updates in the memstore out to disk, while being able to
   * process reads/writes as much as possible during the flush operation. Also,
   * the log has to state clearly the point in time at which the memstore was
   * flushed. (That way, during recovery, we know when we can rely on the
   * on-disk flushed structures and when we have to recover the memstore from
   * the log.)
   *
   * <p>So, we have a three-step process:
   *
   * <ul><li>A. Flush the memstore to the on-disk stores, noting the current
   * sequence ID for the log.<li>
   *
   * <li>B. Write a FLUSHCACHE-COMPLETE message to the log, using the sequence
   * ID that was current at the time of memstore-flush.</li>
   *
   * <li>C. Get rid of the memstore structures that are now redundant, as
   * they've been flushed to the on-disk HStores.</li>
   * </ul>
   * <p>This method is protected, but can be accessed via several public
   * routes.
   *
   * <p> This method may block for some time.
   * @param status
   *
   * @return true if the region needs compacting
   *
   * @throws IOException general io exceptions
   * @throws DroppedSnapshotException Thrown when replay of hlog is required
   * because a Snapshot was not properly persisted.
   */
  protected boolean internalFlushcache(MonitoredTask status)
      throws IOException {
    return internalFlushcache(this.log, -1, status);
  }

  /**
   * @param wal Null if we're NOT to go via hlog/wal.
   * @param myseqid The seqid to use if <code>wal</code> is null writing out
   * flush file.
   * @param status
   * @return true if the region needs compacting
   * @throws IOException
   * @see #internalFlushcache(MonitoredTask)
   */
  protected boolean internalFlushcache(
      final HLog wal, final long myseqid, MonitoredTask status)
  throws IOException {
    final long startTime = EnvironmentEdgeManager.currentTimeMillis();
    // Clear flush flag.
    // Record latest flush time
    this.lastFlushTime = startTime;
    // If nothing to flush, return and avoid logging start/stop flush.
    if (this.memstoreSize.get() <= 0) {
      return false;
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("Started memstore flush for " + this +
        ", current region memstore size " +
        StringUtils.humanReadableInt(this.memstoreSize.get()) +
        ((wal != null)? "": "; wal is null, using passed sequenceid=" + myseqid));
    }

    // Stop updates while we snapshot the memstore of all stores. We only have
    // to do this for a moment.  Its quick.  The subsequent sequence id that
    // goes into the HLog after we've flushed all these snapshots also goes
    // into the info file that sits beside the flushed files.
    // We also set the memstore size to zero here before we allow updates
    // again so its value will represent the size of the updates received
    // during the flush
    long sequenceId = -1L;
    long completeSequenceId = -1L;
    MultiVersionConsistencyControl.WriteEntry w = null;

    // We have to take a write lock during snapshot, or else a write could
    // end up in both snapshot and memstore (makes it difficult to do atomic
    // rows then)
    status.setStatus("Obtaining lock to block concurrent updates");
    this.updatesLock.writeLock().lock();
    long flushsize = this.memstoreSize.get();
    status.setStatus("Preparing to flush by snapshotting stores");
    List<StoreFlusher> storeFlushers = new ArrayList<StoreFlusher>(stores.size());
    try {
      // Record the mvcc for all transactions in progress.
      w = mvcc.beginMemstoreInsert();
      mvcc.advanceMemstore(w);

      sequenceId = (wal == null)? myseqid:
        wal.startCacheFlush(this.regionInfo.getEncodedNameAsBytes());
      completeSequenceId = this.getCompleteCacheFlushSequenceId(sequenceId);

      for (Store s : stores.values()) {
        storeFlushers.add(s.getStoreFlusher(completeSequenceId));
      }

      // prepare flush (take a snapshot)
      for (StoreFlusher flusher : storeFlushers) {
        flusher.prepare();
      }
    } finally {
      this.updatesLock.writeLock().unlock();
    }
    String s = "Finished snapshotting " + this +
      ", commencing wait for mvcc, flushsize=" + flushsize;
    status.setStatus(s);
    LOG.debug(s);

    // wait for all in-progress transactions to commit to HLog before
    // we can start the flush. This prevents
    // uncommitted transactions from being written into HFiles.
    // We have to block before we start the flush, otherwise keys that
    // were removed via a rollbackMemstore could be written to Hfiles.
    mvcc.waitForRead(w);

    status.setStatus("Flushing stores");
    LOG.debug("Finished snapshotting, commencing flushing stores");

    // Any failure from here on out will be catastrophic requiring server
    // restart so hlog content can be replayed and put back into the memstore.
    // Otherwise, the snapshot content while backed up in the hlog, it will not
    // be part of the current running servers state.
    boolean compactionRequested = false;
    try {
      // A.  Flush memstore to all the HStores.
      // Keep running vector of all store files that includes both old and the
      // just-made new flush store file. The new flushed file is still in the
      // tmp directory.

      for (StoreFlusher flusher : storeFlushers) {
        flusher.flushCache(status);
      }

      // Switch snapshot (in memstore) -> new hfile (thus causing
      // all the store scanners to reset/reseek).
      for (StoreFlusher flusher : storeFlushers) {
        boolean needsCompaction = flusher.commit(status);
        if (needsCompaction) {
          compactionRequested = true;
        }
      }
      storeFlushers.clear();

      // Set down the memstore size by amount of flush.
      this.addAndGetGlobalMemstoreSize(-flushsize);
    } catch (Throwable t) {
      // An exception here means that the snapshot was not persisted.
      // The hlog needs to be replayed so its content is restored to memstore.
      // Currently, only a server restart will do this.
      // We used to only catch IOEs but its possible that we'd get other
      // exceptions -- e.g. HBASE-659 was about an NPE -- so now we catch
      // all and sundry.
      if (wal != null) {
        wal.abortCacheFlush(this.regionInfo.getEncodedNameAsBytes());
      }
      DroppedSnapshotException dse = new DroppedSnapshotException("region: " +
          Bytes.toStringBinary(getRegionName()));
      dse.initCause(t);
      status.abort("Flush failed: " + StringUtils.stringifyException(t));
      throw dse;
    }

    // If we get to here, the HStores have been written. If we get an
    // error in completeCacheFlush it will release the lock it is holding

    // B.  Write a FLUSHCACHE-COMPLETE message to the log.
    //     This tells future readers that the HStores were emitted correctly,
    //     and that all updates to the log for this regionName that have lower
    //     log-sequence-ids can be safely ignored.
    if (wal != null) {
      wal.completeCacheFlush(this.regionInfo.getEncodedNameAsBytes(),
        regionInfo.getTableName(), completeSequenceId,
        this.getRegionInfo().isMetaRegion());
    }

    // C. Finally notify anyone waiting on memstore to clear:
    // e.g. checkResources().
    synchronized (this) {
      notifyAll(); // FindBugs NN_NAKED_NOTIFY
    }

    long time = EnvironmentEdgeManager.currentTimeMillis() - startTime;
    long memstoresize = this.memstoreSize.get();
    String msg = "Finished memstore flush of ~" +
      StringUtils.humanReadableInt(flushsize) + "/" + flushsize +
      ", currentsize=" +
      StringUtils.humanReadableInt(memstoresize) + "/" + memstoresize +
      " for region " + this + " in " + time + "ms, sequenceid=" + sequenceId +
      ", compaction requested=" + compactionRequested +
      ((wal == null)? "; wal=null": "");
    LOG.info(msg);
    status.setStatus(msg);
    this.recentFlushes.add(new Pair<Long,Long>(time/1000, flushsize));

    return compactionRequested;
  }

   /**
   * Get the sequence number to be associated with this cache flush. Used by
   * TransactionalRegion to not complete pending transactions.
   *
   *
   * @param currentSequenceId
   * @return sequence id to complete the cache flush with
   */
  protected long getCompleteCacheFlushSequenceId(long currentSequenceId) {
    return currentSequenceId;
  }

  //////////////////////////////////////////////////////////////////////////////
  // get() methods for client use.
  //////////////////////////////////////////////////////////////////////////////
  /**
   * Return all the data for the row that matches <i>row</i> exactly,
   * or the one that immediately preceeds it, at or immediately before
   * <i>ts</i>.
   *
   * @param row row key
   * @return map of values
   * @throws IOException
   */
  Result getClosestRowBefore(final byte [] row)
  throws IOException{
    return getClosestRowBefore(row, HConstants.CATALOG_FAMILY);
  }

  /**
   * Return all the data for the row that matches <i>row</i> exactly,
   * or the one that immediately preceeds it, at or immediately before
   * <i>ts</i>.
   *
   * @param row row key
   * @param family column family to find on
   * @return map of values
   * @throws IOException read exceptions
   */
  public Result getClosestRowBefore(final byte [] row, final byte [] family)
  throws IOException {
    if (coprocessorHost != null) {
      Result result = new Result();
      if (coprocessorHost.preGetClosestRowBefore(row, family, result)) {
        return result;
      }
    }
    // look across all the HStores for this region and determine what the
    // closest key is across all column families, since the data may be sparse
    checkRow(row, "getClosestRowBefore");
    startRegionOperation();
    this.readRequestsCount.increment();
    try {
      Store store = getStore(family);
      // get the closest key. (HStore.getRowKeyAtOrBefore can return null)
      KeyValue key = store.getRowKeyAtOrBefore(row);
      Result result = null;
      if (key != null) {
        Get get = new Get(key.getRow());
        get.addFamily(family);
        result = get(get, null);
      }
      if (coprocessorHost != null) {
        coprocessorHost.postGetClosestRowBefore(row, family, result);
      }
      return result;
    } finally {
      closeRegionOperation();
    }
  }

  /**
   * Return an iterator that scans over the HRegion, returning the indicated
   * columns and rows specified by the {@link Scan}.
   * <p>
   * This Iterator must be closed by the caller.
   *
   * @param scan configured {@link Scan}
   * @return RegionScanner
   * @throws IOException read exceptions
   */
  public RegionScanner getScanner(Scan scan) throws IOException {
   return getScanner(scan, null);
  }

  void prepareScanner(Scan scan) throws IOException {
    if(!scan.hasFamilies()) {
      // Adding all families to scanner
      for(byte[] family: this.htableDescriptor.getFamiliesKeys()){
        scan.addFamily(family);
      }
    }
  }

  protected RegionScanner getScanner(Scan scan,
      List<KeyValueScanner> additionalScanners) throws IOException {
    startRegionOperation();
    this.readRequestsCount.increment();
    try {
      // Verify families are all valid
      prepareScanner(scan);
      if(scan.hasFamilies()) {
        for(byte [] family : scan.getFamilyMap().keySet()) {
          checkFamily(family);
        }
      }
      return instantiateRegionScanner(scan, additionalScanners);
    } finally {
      closeRegionOperation();
    }
  }

  protected RegionScanner instantiateRegionScanner(Scan scan,
      List<KeyValueScanner> additionalScanners) throws IOException {
    return new RegionScannerImpl(scan, additionalScanners);
  }

  /*
   * @param delete The passed delete is modified by this method. WARNING!
   */
  private void prepareDelete(Delete delete) throws IOException {
    // Check to see if this is a deleteRow insert
    if(delete.getFamilyMap().isEmpty()){
      for(byte [] family : this.htableDescriptor.getFamiliesKeys()){
        // Don't eat the timestamp
        delete.deleteFamily(family, delete.getTimeStamp());
      }
    } else {
      for(byte [] family : delete.getFamilyMap().keySet()) {
        if(family == null) {
          throw new NoSuchColumnFamilyException("Empty family is invalid");
        }
        checkFamily(family);
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  // set() methods for client use.
  //////////////////////////////////////////////////////////////////////////////
  /**
   * @param delete delete object
   * @param lockid existing lock id, or null for grab a lock
   * @param writeToWAL append to the write ahead lock or not
   * @throws IOException read exceptions
   */
  public void delete(Delete delete, Integer lockid, boolean writeToWAL)
  throws IOException {
    checkReadOnly();
    checkResources();
    Integer lid = null;
    startRegionOperation();
    this.writeRequestsCount.increment();
    try {
      byte [] row = delete.getRow();
      // If we did not pass an existing row lock, obtain a new one
      lid = getLock(lockid, row, true);

      try {
        // All edits for the given row (across all column families) must happen atomically.
        prepareDelete(delete);
        internalDelete(delete, delete.getClusterId(), writeToWAL);
      } finally {
        if(lockid == null) releaseRowLock(lid);
      }
    } finally {
      closeRegionOperation();
    }
  }

  /**
   * This is used only by unit tests. Not required to be a public API.
   * @param familyMap map of family to edits for the given family.
   * @param writeToWAL
   * @throws IOException
   */
  void delete(Map<byte[], List<KeyValue>> familyMap, UUID clusterId,
      boolean writeToWAL) throws IOException {
    Delete delete = new Delete();
    delete.setFamilyMap(familyMap);
    delete.setClusterId(clusterId);
    delete.setWriteToWAL(writeToWAL);
    internalDelete(delete, clusterId, writeToWAL);
  }

  /**
   * Setup a Delete object with correct timestamps.
   * Caller should the row and region locks.
   * @param delete
   * @param now
   * @throws IOException
   */
  private void prepareDeleteTimestamps(Delete delete, byte[] byteNow)
      throws IOException {
    Map<byte[], List<KeyValue>> familyMap = delete.getFamilyMap();
    for (Map.Entry<byte[], List<KeyValue>> e : familyMap.entrySet()) {

      byte[] family = e.getKey();
      List<KeyValue> kvs = e.getValue();
      Map<byte[], Integer> kvCount = new TreeMap<byte[], Integer>(Bytes.BYTES_COMPARATOR);

      for (KeyValue kv: kvs) {
        //  Check if time is LATEST, change to time of most recent addition if so
        //  This is expensive.
        if (kv.isLatestTimestamp() && kv.isDeleteType()) {
          byte[] qual = kv.getQualifier();
          if (qual == null) qual = HConstants.EMPTY_BYTE_ARRAY;

          Integer count = kvCount.get(qual);
          if (count == null) {
            kvCount.put(qual, 1);
          } else {
            kvCount.put(qual, count + 1);
          }
          count = kvCount.get(qual);

          Get get = new Get(kv.getRow());
          get.setMaxVersions(count);
          get.addColumn(family, qual);

          List<KeyValue> result = get(get, false);

          if (result.size() < count) {
            // Nothing to delete
            kv.updateLatestStamp(byteNow);
            continue;
          }
          if (result.size() > count) {
            throw new RuntimeException("Unexpected size: " + result.size());
          }
          KeyValue getkv = result.get(count - 1);
          Bytes.putBytes(kv.getBuffer(), kv.getTimestampOffset(),
              getkv.getBuffer(), getkv.getTimestampOffset(), Bytes.SIZEOF_LONG);
        } else {
          kv.updateLatestStamp(byteNow);
        }
      }
    }
  }

  /**
   * @param delete The Delete command
   * @param clusterId UUID of the originating cluster (for replication).
   * @param writeToWAL
   * @throws IOException
   */
  private void internalDelete(Delete delete, UUID clusterId,
      boolean writeToWAL) throws IOException {
    Map<byte[], List<KeyValue>> familyMap = delete.getFamilyMap();
    WALEdit walEdit = new WALEdit();
    /* Run coprocessor pre hook outside of locks to avoid deadlock */
    if (coprocessorHost != null) {
      if (coprocessorHost.preDelete(delete, walEdit, writeToWAL)) {
        return;
      }
    }

    long now = EnvironmentEdgeManager.currentTimeMillis();
    byte [] byteNow = Bytes.toBytes(now);
    boolean flush = false;

    updatesLock.readLock().lock();
    try {
      prepareDeleteTimestamps(delete, byteNow);

      if (writeToWAL) {
        // write/sync to WAL should happen before we touch memstore.
        //
        // If order is reversed, i.e. we write to memstore first, and
        // for some reason fail to write/sync to commit log, the memstore
        // will contain uncommitted transactions.
        //
        // bunch up all edits across all column families into a
        // single WALEdit.
        addFamilyMapToWALEdit(familyMap, walEdit);
        this.log.append(regionInfo, this.htableDescriptor.getName(),
            walEdit, clusterId, now, this.htableDescriptor);
      }

      // Now make changes to the memstore.
      long addedSize = applyFamilyMapToMemstore(familyMap, null);
      flush = isFlushSize(this.addAndGetGlobalMemstoreSize(addedSize));

    } finally {
      this.updatesLock.readLock().unlock();
    }
    // do after lock
    if (coprocessorHost != null) {
      coprocessorHost.postDelete(delete, walEdit, writeToWAL);
    }
    final long after = EnvironmentEdgeManager.currentTimeMillis();
    final String metricPrefix = SchemaMetrics.generateSchemaMetricsPrefix(
        getTableDesc().getNameAsString(), familyMap.keySet());
    if (!metricPrefix.isEmpty()) {
      HRegion.incrTimeVaryingMetric(metricPrefix + "delete_", after - now);
    }

    if (flush) {
      // Request a cache flush.  Do it outside update lock.
      requestFlush();
    }
  }

  /**
   * @param put
   * @throws IOExcept
