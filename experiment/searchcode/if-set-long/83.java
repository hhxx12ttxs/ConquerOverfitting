/*
 * Copyright (C) 2006 The Android Open Source Project
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

package android.database.sqlite;

import android.app.AppGlobals;
import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.DatabaseUtils;
import android.database.DefaultDatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDebug.DbStats;
import android.os.Debug;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;
import android.util.LruCache;
import android.util.Pair;
import dalvik.system.BlockGuard;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

/**
 * Exposes methods to manage a SQLite database.
 * <p>SQLiteDatabase has methods to create, delete, execute SQL commands, and
 * perform other common database management tasks.
 * <p>See the Notepad sample application in the SDK for an example of creating
 * and managing a database.
 * <p> Database names must be unique within an application, not across all
 * applications.
 *
 * <h3>Localized Collation - ORDER BY</h3>
 * <p>In addition to SQLite's default <code>BINARY</code> collator, Android supplies
 * two more, <code>LOCALIZED</code>, which changes with the system's current locale
 * if you wire it up correctly (XXX a link needed!), and <code>UNICODE</code>, which
 * is the Unicode Collation Algorithm and not tailored to the current locale.
 */
public class SQLiteDatabase extends SQLiteClosable {
    private static final String TAG = "SQLiteDatabase";
    private static final boolean ENABLE_DB_SAMPLE = false; // true to enable stats in event log
    private static final int EVENT_DB_OPERATION = 52000;
    private static final int EVENT_DB_CORRUPT = 75004;

    /**
     * Algorithms used in ON CONFLICT clause
     * http://www.sqlite.org/lang_conflict.html
     */
    /**
     *  When a constraint violation occurs, an immediate ROLLBACK occurs,
     * thus ending the current transaction, and the command aborts with a
     * return code of SQLITE_CONSTRAINT. If no transaction is active
     * (other than the implied transaction that is created on every command)
     *  then this algorithm works the same as ABORT.
     */
    public static final int CONFLICT_ROLLBACK = 1;

    /**
     * When a constraint violation occurs,no ROLLBACK is executed
     * so changes from prior commands within the same transaction
     * are preserved. This is the default behavior.
     */
    public static final int CONFLICT_ABORT = 2;

    /**
     * When a constraint violation occurs, the command aborts with a return
     * code SQLITE_CONSTRAINT. But any changes to the database that
     * the command made prior to encountering the constraint violation
     * are preserved and are not backed out.
     */
    public static final int CONFLICT_FAIL = 3;

    /**
     * When a constraint violation occurs, the one row that contains
     * the constraint violation is not inserted or changed.
     * But the command continues executing normally. Other rows before and
     * after the row that contained the constraint violation continue to be
     * inserted or updated normally. No error is returned.
     */
    public static final int CONFLICT_IGNORE = 4;

    /**
     * When a UNIQUE constraint violation occurs, the pre-existing rows that
     * are causing the constraint violation are removed prior to inserting
     * or updating the current row. Thus the insert or update always occurs.
     * The command continues executing normally. No error is returned.
     * If a NOT NULL constraint violation occurs, the NULL value is replaced
     * by the default value for that column. If the column has no default
     * value, then the ABORT algorithm is used. If a CHECK constraint
     * violation occurs then the IGNORE algorithm is used. When this conflict
     * resolution strategy deletes rows in order to satisfy a constraint,
     * it does not invoke delete triggers on those rows.
     *  This behavior might change in a future release.
     */
    public static final int CONFLICT_REPLACE = 5;

    /**
     * use the following when no conflict action is specified.
     */
    public static final int CONFLICT_NONE = 0;
    private static final String[] CONFLICT_VALUES = new String[]
            {"", " OR ROLLBACK ", " OR ABORT ", " OR FAIL ", " OR IGNORE ", " OR REPLACE "};

    /**
     * Maximum Length Of A LIKE Or GLOB Pattern
     * The pattern matching algorithm used in the default LIKE and GLOB implementation
     * of SQLite can exhibit O(N^2) performance (where N is the number of characters in
     * the pattern) for certain pathological cases. To avoid denial-of-service attacks
     * the length of the LIKE or GLOB pattern is limited to SQLITE_MAX_LIKE_PATTERN_LENGTH bytes.
     * The default value of this limit is 50000. A modern workstation can evaluate
     * even a pathological LIKE or GLOB pattern of 50000 bytes relatively quickly.
     * The denial of service problem only comes into play when the pattern length gets
     * into millions of bytes. Nevertheless, since most useful LIKE or GLOB patterns
     * are at most a few dozen bytes in length, paranoid application developers may
     * want to reduce this parameter to something in the range of a few hundred
     * if they know that external users are able to generate arbitrary patterns.
     */
    public static final int SQLITE_MAX_LIKE_PATTERN_LENGTH = 50000;

    /**
     * Flag for {@link #openDatabase} to open the database for reading and writing.
     * If the disk is full, this may fail even before you actually write anything.
     *
     * {@more} Note that the value of this flag is 0, so it is the default.
     */
    public static final int OPEN_READWRITE = 0x00000000;          // update native code if changing

    /**
     * Flag for {@link #openDatabase} to open the database for reading only.
     * This is the only reliable way to open a database if the disk may be full.
     */
    public static final int OPEN_READONLY = 0x00000001;           // update native code if changing

    private static final int OPEN_READ_MASK = 0x00000001;         // update native code if changing

    /**
     * Flag for {@link #openDatabase} to open the database without support for localized collators.
     *
     * {@more} This causes the collator <code>LOCALIZED</code> not to be created.
     * You must be consistent when using this flag to use the setting the database was
     * created with.  If this is set, {@link #setLocale} will do nothing.
     */
    public static final int NO_LOCALIZED_COLLATORS = 0x00000010;  // update native code if changing

    /**
     * Flag for {@link #openDatabase} to create the database file if it does not already exist.
     */
    public static final int CREATE_IF_NECESSARY = 0x10000000;     // update native code if changing

    /**
     * Indicates whether the most-recently started transaction has been marked as successful.
     */
    private boolean mInnerTransactionIsSuccessful;

    /**
     * Valid during the life of a transaction, and indicates whether the entire transaction (the
     * outer one and all of the inner ones) so far has been successful.
     */
    private boolean mTransactionIsSuccessful;

    /**
     * Valid during the life of a transaction.
     */
    private SQLiteTransactionListener mTransactionListener;

    /**
     * this member is set if {@link #execSQL(String)} is used to begin and end transactions.
     */
    private boolean mTransactionUsingExecSql;

    /** Synchronize on this when accessing the database */
    private final DatabaseReentrantLock mLock = new DatabaseReentrantLock(true);

    private long mLockAcquiredWallTime = 0L;
    private long mLockAcquiredThreadTime = 0L;

    // limit the frequency of complaints about each database to one within 20 sec
    // unless run command adb shell setprop log.tag.Database VERBOSE
    private static final int LOCK_WARNING_WINDOW_IN_MS = 20000;
    /** If the lock is held this long then a warning will be printed when it is released. */
    private static final int LOCK_ACQUIRED_WARNING_TIME_IN_MS = 300;
    private static final int LOCK_ACQUIRED_WARNING_THREAD_TIME_IN_MS = 100;
    private static final int LOCK_ACQUIRED_WARNING_TIME_IN_MS_ALWAYS_PRINT = 2000;

    private static final int SLEEP_AFTER_YIELD_QUANTUM = 1000;

    // The pattern we remove from database filenames before
    // potentially logging them.
    private static final Pattern EMAIL_IN_DB_PATTERN = Pattern.compile("[\\w\\.\\-]+@[\\w\\.\\-]+");

    private long mLastLockMessageTime = 0L;

    // Things related to query logging/sampling for debugging
    // slow/frequent queries during development.  Always log queries
    // which take (by default) 500ms+; shorter queries are sampled
    // accordingly.  Commit statements, which are typically slow, are
    // logged together with the most recently executed SQL statement,
    // for disambiguation.  The 500ms value is configurable via a
    // SystemProperty, but developers actively debugging database I/O
    // should probably use the regular log tunable,
    // LOG_SLOW_QUERIES_PROPERTY, defined below.
    private static int sQueryLogTimeInMillis = 0;  // lazily initialized
    private static final int QUERY_LOG_SQL_LENGTH = 64;
    private static final String COMMIT_SQL = "COMMIT;";
    private static final String BEGIN_SQL = "BEGIN;";
    private final Random mRandom = new Random();
    /** the last non-commit/rollback sql statement in a transaction */
    // guarded by 'this'
    private String mLastSqlStatement = null;

    synchronized String getLastSqlStatement() {
        return mLastSqlStatement;
    }

    synchronized void setLastSqlStatement(String sql) {
        mLastSqlStatement = sql;
    }

    /** guarded by {@link #mLock} */
    private long mTransStartTime;

    // String prefix for slow database query EventLog records that show
    // lock acquistions of the database.
    /* package */ static final String GET_LOCK_LOG_PREFIX = "GETLOCK:";

    /** Used by native code, do not rename. make it volatile, so it is thread-safe. */
    /* package */ volatile int mNativeHandle = 0;

    /**
     * The size, in bytes, of a block on "/data". This corresponds to the Unix
     * statfs.f_bsize field. note that this field is lazily initialized.
     */
    private static int sBlockSize = 0;

    /** The path for the database file */
    private final String mPath;

    /** The anonymized path for the database file for logging purposes */
    private String mPathForLogs = null;  // lazily populated

    /** The flags passed to open/create */
    private final int mFlags;

    /** The optional factory to use when creating new Cursors */
    private final CursorFactory mFactory;

    private final WeakHashMap<SQLiteClosable, Object> mPrograms;

    /** Default statement-cache size per database connection ( = instance of this class) */
    private static final int DEFAULT_SQL_CACHE_SIZE = 25;

    /**
     * for each instance of this class, a LRU cache is maintained to store
     * the compiled query statement ids returned by sqlite database.
     *     key = SQL statement with "?" for bind args
     *     value = {@link SQLiteCompiledSql}
     * If an application opens the database and keeps it open during its entire life, then
     * there will not be an overhead of compilation of SQL statements by sqlite.
     *
     * why is this cache NOT static? because sqlite attaches compiledsql statements to the
     * struct created when {@link SQLiteDatabase#openDatabase(String, CursorFactory, int)} is
     * invoked.
     *
     * this cache's max size is settable by calling the method
     * (@link #setMaxSqlCacheSize(int)}.
     */
    // guarded by this
    private LruCache<String, SQLiteCompiledSql> mCompiledQueries;

    /**
     * absolute max value that can be set by {@link #setMaxSqlCacheSize(int)}
     * size of each prepared-statement is between 1K - 6K, depending on the complexity of the
     * SQL statement & schema.
     */
    public static final int MAX_SQL_CACHE_SIZE = 100;
    private boolean mCacheFullWarning;

    /** Used to find out where this object was created in case it never got closed. */
    private final Throwable mStackTrace;

    /** stores the list of statement ids that need to be finalized by sqlite */
    private final ArrayList<Integer> mClosedStatementIds = new ArrayList<Integer>();

    /** {@link DatabaseErrorHandler} to be used when SQLite returns any of the following errors
     *    Corruption
     * */
    private final DatabaseErrorHandler mErrorHandler;

    /** The Database connection pool {@link DatabaseConnectionPool}.
     * Visibility is package-private for testing purposes. otherwise, private visibility is enough.
     */
    /* package */ volatile DatabaseConnectionPool mConnectionPool = null;

    /** Each database connection handle in the pool is assigned a number 1..N, where N is the
     * size of the connection pool.
     * The main connection handle to which the pool is attached is assigned a value of 0.
     */
    /* package */ final short mConnectionNum;

    /** on pooled database connections, this member points to the parent ( = main)
     * database connection handle.
     * package visibility only for testing purposes
     */
    /* package */ SQLiteDatabase mParentConnObj = null;

    private static final String MEMORY_DB_PATH = ":memory:";

    /** set to true if the database has attached databases */
    private volatile boolean mHasAttachedDbs = false;

    /** stores reference to all databases opened in the current process. */
    private static ArrayList<WeakReference<SQLiteDatabase>> mActiveDatabases =
            new ArrayList<WeakReference<SQLiteDatabase>>();

    synchronized void addSQLiteClosable(SQLiteClosable closable) {
        // mPrograms is per instance of SQLiteDatabase and it doesn't actually touch the database
        // itself. so, there is no need to lock().
        mPrograms.put(closable, null);
    }

    synchronized void removeSQLiteClosable(SQLiteClosable closable) {
        mPrograms.remove(closable);
    }

    @Override
    protected void onAllReferencesReleased() {
        if (isOpen()) {
            // close the database which will close all pending statements to be finalized also
            close();
        }
    }

    /**
     * Attempts to release memory that SQLite holds but does not require to
     * operate properly. Typically this memory will come from the page cache.
     *
     * @return the number of bytes actually released
     */
    static public native int releaseMemory();

    /**
     * Control whether or not the SQLiteDatabase is made thread-safe by using locks
     * around critical sections. This is pretty expensive, so if you know that your
     * DB will only be used by a single thread then you should set this to false.
     * The default is true.
     * @param lockingEnabled set to true to enable locks, false otherwise
     */
    public void setLockingEnabled(boolean lockingEnabled) {
        mLockingEnabled = lockingEnabled;
    }

    /**
     * If set then the SQLiteDatabase is made thread-safe by using locks
     * around critical sections
     */
    private boolean mLockingEnabled = true;

    /* package */ void onCorruption() {
        EventLog.writeEvent(EVENT_DB_CORRUPT, mPath);
        mErrorHandler.onCorruption(this);
    }

    /**
     * Locks the database for exclusive access. The database lock must be held when
     * touch the native sqlite3* object since it is single threaded and uses
     * a polling lock contention algorithm. The lock is recursive, and may be acquired
     * multiple times by the same thread. This is a no-op if mLockingEnabled is false.
     *
     * @see #unlock()
     */
    /* package */ void lock(String sql) {
        lock(sql, false);
    }

    /* pachage */ void lock() {
        lock(null, false);
    }

    private static final long LOCK_WAIT_PERIOD = 30L;
    private void lock(String sql, boolean forced) {
        // make sure this method is NOT being called from a 'synchronized' method
        if (Thread.holdsLock(this)) {
            Log.w(TAG, "don't lock() while in a synchronized method");
        }
        verifyDbIsOpen();
        if (!forced && !mLockingEnabled) return;
        boolean done = false;
        long timeStart = SystemClock.uptimeMillis();
        while (!done) {
            try {
                // wait for 30sec to acquire the lock
                done = mLock.tryLock(LOCK_WAIT_PERIOD, TimeUnit.SECONDS);
                if (!done) {
                    // lock not acquired in NSec. print a message and stacktrace saying the lock
                    // has not been available for 30sec.
                    Log.w(TAG, "database lock has not been available for " + LOCK_WAIT_PERIOD +
                            " sec. Current Owner of the lock is " + mLock.getOwnerDescription() +
                            ". Continuing to wait in thread: " + Thread.currentThread().getId());
                }
            } catch (InterruptedException e) {
                // ignore the interruption
            }
        }
        if (SQLiteDebug.DEBUG_LOCK_TIME_TRACKING) {
            if (mLock.getHoldCount() == 1) {
                // Use elapsed real-time since the CPU may sleep when waiting for IO
                mLockAcquiredWallTime = SystemClock.elapsedRealtime();
                mLockAcquiredThreadTime = Debug.threadCpuTimeNanos();
            }
        }
        if (sql != null) {
            if (ENABLE_DB_SAMPLE)  {
                logTimeStat(sql, timeStart, GET_LOCK_LOG_PREFIX);
            }
        }
    }
    private static class DatabaseReentrantLock extends ReentrantLock {
        DatabaseReentrantLock(boolean fair) {
            super(fair);
        }
        @Override
        public Thread getOwner() {
            return super.getOwner();
        }
        public String getOwnerDescription() {
            Thread t = getOwner();
            return (t== null) ? "none" : String.valueOf(t.getId());
        }
    }

    /**
     * Locks the database for exclusive access. The database lock must be held when
     * touch the native sqlite3* object since it is single threaded and uses
     * a polling lock contention algorithm. The lock is recursive, and may be acquired
     * multiple times by the same thread.
     *
     * @see #unlockForced()
     */
    private void lockForced() {
        lock(null, true);
    }

    private void lockForced(String sql) {
        lock(sql, true);
    }

    /**
     * Releases the database lock. This is a no-op if mLockingEnabled is false.
     *
     * @see #unlock()
     */
    /* package */ void unlock() {
        if (!mLockingEnabled) return;
        if (SQLiteDebug.DEBUG_LOCK_TIME_TRACKING) {
            if (mLock.getHoldCount() == 1) {
                checkLockHoldTime();
            }
        }
        mLock.unlock();
    }

    /**
     * Releases the database lock.
     *
     * @see #unlockForced()
     */
    private void unlockForced() {
        if (SQLiteDebug.DEBUG_LOCK_TIME_TRACKING) {
            if (mLock.getHoldCount() == 1) {
                checkLockHoldTime();
            }
        }
        mLock.unlock();
    }

    private void checkLockHoldTime() {
        // Use elapsed real-time since the CPU may sleep when waiting for IO
        long elapsedTime = SystemClock.elapsedRealtime();
        long lockedTime = elapsedTime - mLockAcquiredWallTime;
        if (lockedTime < LOCK_ACQUIRED_WARNING_TIME_IN_MS_ALWAYS_PRINT &&
                !Log.isLoggable(TAG, Log.VERBOSE) &&
                (elapsedTime - mLastLockMessageTime) < LOCK_WARNING_WINDOW_IN_MS) {
            return;
        }
        if (lockedTime > LOCK_ACQUIRED_WARNING_TIME_IN_MS) {
            int threadTime = (int)
                    ((Debug.threadCpuTimeNanos() - mLockAcquiredThreadTime) / 1000000);
            if (threadTime > LOCK_ACQUIRED_WARNING_THREAD_TIME_IN_MS ||
                    lockedTime > LOCK_ACQUIRED_WARNING_TIME_IN_MS_ALWAYS_PRINT) {
                mLastLockMessageTime = elapsedTime;
                String msg = "lock held on " + mPath + " for " + lockedTime + "ms. Thread time was "
                        + threadTime + "ms";
                if (SQLiteDebug.DEBUG_LOCK_TIME_TRACKING_STACK_TRACE) {
                    Log.d(TAG, msg, new Exception());
                } else {
                    Log.d(TAG, msg);
                }
            }
        }
    }

    /**
     * Begins a transaction in EXCLUSIVE mode.
     * <p>
     * Transactions can be nested.
     * When the outer transaction is ended all of
     * the work done in that transaction and all of the nested transactions will be committed or
     * rolled back. The changes will be rolled back if any transaction is ended without being
     * marked as clean (by calling setTransactionSuccessful). Otherwise they will be committed.
     * </p>
     * <p>Here is the standard idiom for transactions:
     *
     * <pre>
     *   db.beginTransaction();
     *   try {
     *     ...
     *     db.setTransactionSuccessful();
     *   } finally {
     *     db.endTransaction();
     *   }
     * </pre>
     */
    public void beginTransaction() {
        beginTransaction(null /* transactionStatusCallback */, true);
    }

    /**
     * Begins a transaction in IMMEDIATE mode. Transactions can be nested. When
     * the outer transaction is ended all of the work done in that transaction
     * and all of the nested transactions will be committed or rolled back. The
     * changes will be rolled back if any transaction is ended without being
     * marked as clean (by calling setTransactionSuccessful). Otherwise they
     * will be committed.
     * <p>
     * Here is the standard idiom for transactions:
     *
     * <pre>
     *   db.beginTransactionNonExclusive();
     *   try {
     *     ...
     *     db.setTransactionSuccessful();
     *   } finally {
     *     db.endTransaction();
     *   }
     * </pre>
     */
    public void beginTransactionNonExclusive() {
        beginTransaction(null /* transactionStatusCallback */, false);
    }

    /**
     * Begins a transaction in EXCLUSIVE mode.
     * <p>
     * Transactions can be nested.
     * When the outer transaction is ended all of
     * the work done in that transaction and all of the nested transactions will be committed or
     * rolled back. The changes will be rolled back if any transaction is ended without being
     * marked as clean (by calling setTransactionSuccessful). Otherwise they will be committed.
     * </p>
     * <p>Here is the standard idiom for transactions:
     *
     * <pre>
     *   db.beginTransactionWithListener(listener);
     *   try {
     *     ...
     *     db.setTransactionSuccessful();
     *   } finally {
     *     db.endTransaction();
     *   }
     * </pre>
     *
     * @param transactionListener listener that should be notified when the transaction begins,
     * commits, or is rolled back, either explicitly or by a call to
     * {@link #yieldIfContendedSafely}.
     */
    public void beginTransactionWithListener(SQLiteTransactionListener transactionListener) {
        beginTransaction(transactionListener, true);
    }

    /**
     * Begins a transaction in IMMEDIATE mode. Transactions can be nested. When
     * the outer transaction is ended all of the work done in that transaction
     * and all of the nested transactions will be committed or rolled back. The
     * changes will be rolled back if any transaction is ended without being
     * marked as clean (by calling setTransactionSuccessful). Otherwise they
     * will be committed.
     * <p>
     * Here is the standard idiom for transactions:
     *
     * <pre>
     *   db.beginTransactionWithListenerNonExclusive(listener);
     *   try {
     *     ...
     *     db.setTransactionSuccessful();
     *   } finally {
     *     db.endTransaction();
     *   }
     * </pre>
     *
     * @param transactionListener listener that should be notified when the
     *            transaction begins, commits, or is rolled back, either
     *            explicitly or by a call to {@link #yieldIfContendedSafely}.
     */
    public void beginTransactionWithListenerNonExclusive(
            SQLiteTransactionListener transactionListener) {
        beginTransaction(transactionListener, false);
    }

    private void beginTransaction(SQLiteTransactionListener transactionListener,
            boolean exclusive) {
        verifyDbIsOpen();
        lockForced(BEGIN_SQL);
        boolean ok = false;
        try {
            // If this thread already had the lock then get out
            if (mLock.getHoldCount() > 1) {
                if (mInnerTransactionIsSuccessful) {
                    String msg = "Cannot call beginTransaction between "
                            + "calling setTransactionSuccessful and endTransaction";
                    IllegalStateException e = new IllegalStateException(msg);
                    Log.e(TAG, "beginTransaction() failed", e);
                    throw e;
                }
                ok = true;
                return;
            }

            // This thread didn't already have the lock, so begin a database
            // transaction now.
            if (exclusive && mConnectionPool == null) {
                execSQL("BEGIN EXCLUSIVE;");
            } else {
                execSQL("BEGIN IMMEDIATE;");
            }
            mTransStartTime = SystemClock.uptimeMillis();
            mTransactionListener = transactionListener;
            mTransactionIsSuccessful = true;
            mInnerTransactionIsSuccessful = false;
            if (transactionListener != null) {
                try {
                    transactionListener.onBegin();
                } catch (RuntimeException e) {
                    execSQL("ROLLBACK;");
                    throw e;
                }
            }
            ok = true;
        } finally {
            if (!ok) {
                // beginTransaction is called before the try block so we must release the lock in
                // the case of failure.
                unlockForced();
            }
        }
    }

    /**
     * End a transaction. See beginTransaction for notes about how to use this and when transactions
     * are committed and rolled back.
     */
    public void endTransaction() {
        verifyLockOwner();
        try {
            if (mInnerTransactionIsSuccessful) {
                mInnerTransactionIsSuccessful = false;
            } else {
                mTransactionIsSuccessful = false;
            }
            if (mLock.getHoldCount() != 1) {
                return;
            }
            RuntimeException savedException = null;
            if (mTransactionListener != null) {
                try {
                    if (mTransactionIsSuccessful) {
                        mTransactionListener.onCommit();
                    } else {
                        mTransactionListener.onRollback();
                    }
                } catch (RuntimeException e) {
                    savedException = e;
                    mTransactionIsSuccessful = false;
                }
            }
            if (mTransactionIsSuccessful) {
                execSQL(COMMIT_SQL);
                // if write-ahead logging is used, we have to take care of checkpoint.
                // TODO: should applications be given the flexibility of choosing when to
                // trigger checkpoint?
                // for now, do checkpoint after every COMMIT because that is the fastest
                // way to guarantee that readers will see latest data.
                // but this is the slowest way to run sqlite with in write-ahead logging mode.
                if (this.mConnectionPool != null) {
                    execSQL("PRAGMA wal_checkpoint;");
                    if (SQLiteDebug.DEBUG_SQL_STATEMENTS) {
                        Log.i(TAG, "PRAGMA wal_Checkpoint done");
                    }
                }
                // log the transaction time to the Eventlog.
                if (ENABLE_DB_SAMPLE) {
                    logTimeStat(getLastSqlStatement(), mTransStartTime, COMMIT_SQL);
                }
            } else {
                try {
                    execSQL("ROLLBACK;");
                    if (savedException != null) {
                        throw savedException;
                    }
                } catch (SQLException e) {
                    if (false) {
                        Log.d(TAG, "exception during rollback, maybe the DB previously "
                                + "performed an auto-rollback");
                    }
                }
            }
        } finally {
            mTransactionListener = null;
            unlockForced();
            if (false) {
                Log.v(TAG, "unlocked " + Thread.currentThread()
                        + ", holdCount is " + mLock.getHoldCount());
            }
        }
    }

    /**
     * Marks the current transaction as successful. Do not do any more database work between
     * calling this and calling endTransaction. Do as little non-database work as possible in that
     * situation too. If any errors are encountered between this and endTransaction the transaction
     * will still be committed.
     *
     * @throws IllegalStateException if the current thread is not in a transaction or the
     * transaction is already marked as successful.
     */
    public void setTransactionSuccessful() {
        verifyDbIsOpen();
        if (!mLock.isHeldByCurrentThread()) {
            throw new IllegalStateException("no transaction pending");
        }
        if (mInnerTransactionIsSuccessful) {
            throw new IllegalStateException(
                    "setTransactionSuccessful may only be called once per call to beginTransaction");
        }
        mInnerTransactionIsSuccessful = true;
    }

    /**
     * return true if there is a transaction pending
     */
    public boolean inTransaction() {
        return mLock.getHoldCount() > 0 || mTransactionUsingExecSql;
    }

    /* package */ synchronized void setTransactionUsingExecSqlFlag() {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.i(TAG, "found execSQL('begin transaction')");
        }
        mTransactionUsingExecSql = true;
    }

    /* package */ synchronized void resetTransactionUsingExecSqlFlag() {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            if (mTransactionUsingExecSql) {
                Log.i(TAG, "found execSQL('commit or end or rollback')");
            }
        }
        mTransactionUsingExecSql = false;
    }

    /**
     * Returns true if the caller is considered part of the current transaction, if any.
     * <p>
     * Caller is part of the current transaction if either of the following is true
     * <ol>
     *   <li>If transaction is started by calling beginTransaction() methods AND if the caller is
     *   in the same thread as the thread that started the transaction.
     *   </li>
     *   <li>If the transaction is started by calling {@link #execSQL(String)} like this:
     *   execSQL("BEGIN transaction"). In this case, every thread in the process is considered
     *   part of the current transaction.</li>
     * </ol>
     *
     * @return true if the caller is considered part of the current transaction, if any.
     */
    /* package */ synchronized boolean amIInTransaction() {
        // always do this test on the main database connection - NOT on pooled database connection
        // since transactions always occur on the main database connections only.
        SQLiteDatabase db = (isPooledConnection()) ? mParentConnObj : this;
        boolean b = (!db.inTransaction()) ? false :
                db.mTransactionUsingExecSql || db.mLock.isHeldByCurrentThread();
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.i(TAG, "amIinTransaction: " + b);
        }
        return b;
    }

    /**
     * Checks if the database lock is held by this thread.
     *
     * @return true, if this thread is holding the database lock.
     */
    public boolean isDbLockedByCurrentThread() {
        return mLock.isHeldByCurrentThread();
    }

    /**
     * Checks if the database is locked by another thread. This is
     * just an estimate, since this status can change at any time,
     * including after the call is made but before the result has
     * been acted upon.
     *
     * @return true, if the database is locked by another thread
     */
    public boolean isDbLockedByOtherThreads() {
        return !mLock.isHeldByCurrentThread() && mLock.isLocked();
    }

    /**
     * Temporarily end the transaction to let other threads run. The transaction is assumed to be
     * successful so far. Do not call setTransactionSuccessful before calling this. When this
     * returns a new transaction will have been created but not marked as successful.
     * @return true if the transaction was yielded
     * @deprecated if the db is locked more than once (becuase of nested transactions) then the lock
     *   will not be yielded. Use yieldIfContendedSafely instead.
     */
    @Deprecated
    public boolean yieldIfContended() {
        return yieldIfContendedHelper(false /* do not check yielding */,
                -1 /* sleepAfterYieldDelay */);
    }

    /**
     * Temporarily end the transaction to let other threads run. The transaction is assumed to be
     * successful so far. Do not call setTransactionSuccessful before calling this. When this
     * returns a new transaction will have been created but not marked as successful. This assumes
     * that there are no nested transactions (beginTransaction has only been called once) and will
     * throw an exception if that is not the case.
     * @return true if the transaction was yielded
     */
    public boolean yieldIfContendedSafely() {
        return yieldIfContendedHelper(true /* check yielding */, -1 /* sleepAfterYieldDelay*/);
    }

    /**
     * Temporarily end the transaction to let other threads run. The transaction is assumed to be
     * successful so far. Do not call setTransactionSuccessful before calling this. When this
     * returns a new transaction will have been created but not marked as successful. This assumes
     * that there are no nested transactions (beginTransaction has only been called once) and will
     * throw an exception if that is not the case.
     * @param sleepAfterYieldDelay if > 0, sleep this long before starting a new transaction if
     *   the lock was actually yielded. This will allow other background threads to make some
     *   more progress than they would if we started the transaction immediately.
     * @return true if the transaction was yielded
     */
    public boolean yieldIfContendedSafely(long sleepAfterYieldDelay) {
        return yieldIfContendedHelper(true /* check yielding */, sleepAfterYieldDelay);
    }

    private boolean yieldIfContendedHelper(boolean checkFullyYielded, long sleepAfterYieldDelay) {
        if (mLock.getQueueLength() == 0) {
            // Reset the lock acquire time since we know that the thread was willing to yield
            // the lock at this time.
            mLockAcquiredWallTime = SystemClock.elapsedRealtime();
            mLockAcquiredThreadTime = Debug.threadCpuTimeNanos();
            return false;
        }
        setTransactionSuccessful();
        SQLiteTransactionListener transactionListener = mTransactionListener;
        endTransaction();
        if (checkFullyYielded) {
            if (this.isDbLockedByCurrentThread()) {
                throw new IllegalStateException(
                        "Db locked more than once. yielfIfContended cannot yield");
            }
        }
        if (sleepAfterYieldDelay > 0) {
            // Sleep for up to sleepAfterYieldDelay milliseconds, waking up periodically to
            // check if anyone is using the database.  If the database is not contended,
            // retake the lock and return.
            long remainingDelay = sleepAfterYieldDelay;
            while (remainingDelay > 0) {
                try {
                    Thread.sleep(remainingDelay < SLEEP_AFTER_YIELD_QUANTUM ?
                            remainingDelay : SLEEP_AFTER_YIELD_QUANTUM);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
                remainingDelay -= SLEEP_AFTER_YIELD_QUANTUM;
                if (mLock.getQueueLength() == 0) {
                    break;
                }
            }
        }
        beginTransactionWithListener(transactionListener);
        return true;
    }

    /**
     * @deprecated This method no longer serves any useful purpose and has been deprecated.
     */
    @Deprecated
    public Map<String, String> getSyncedTables() {
        return new HashMap<String, String>(0);
    }

    /**
     * Used to allow returning sub-classes of {@link Cursor} when calling query.
     */
    public interface CursorFactory {
        /**
         * See
         * {@link SQLiteCursor#SQLiteCursor(SQLiteCursorDriver, String, SQLiteQuery)}.
         */
        public Cursor newCursor(SQLiteDatabase db,
                SQLiteCursorDriver masterQuery, String editTable,
                SQLiteQuery query);
    }

    /**
     * Open the database according to the flags {@link #OPEN_READWRITE}
     * {@link #OPEN_READONLY} {@link #CREATE_IF_NECESSARY} and/or {@link #NO_LOCALIZED_COLLATORS}.
     *
     * <p>Sets the locale of the database to the  the system's current locale.
     * Call {@link #setLocale} if you would like something else.</p>
     *
     * @param path to database file to open and/or create
     * @param factory an optional factory class that is called to instantiate a
     *            cursor when query is called, or null for default
     * @param flags to control database access mode
     * @return the newly opened database
     * @throws SQLiteException if the database cannot be opened
     */
    public static SQLiteDatabase openDatabase(String path, CursorFactory factory, int flags) {
        return openDatabase(path, factory, flags, new DefaultDatabaseErrorHandler());
    }

    /**
     * Open the database according to the flags {@link #OPEN_READWRITE}
     * {@link #OPEN_READONLY} {@link #CREATE_IF_NECESSARY} and/or {@link #NO_LOCALIZED_COLLATORS}.
     *
     * <p>Sets the locale of the database to the  the system's current locale.
     * Call {@link #setLocale} if you would like something else.</p>
     *
     * <p>Accepts input param: a concrete instance of {@link DatabaseErrorHandler} to be
     * used to handle corruption when sqlite reports database corruption.</p>
     *
     * @param path to database file to open and/or create
     * @param factory an optional factory class that is called to instantiate a
     *            cursor when query is called, or null for default
     * @param flags to control database access mode
     * @param errorHandler the {@link DatabaseErrorHandler} obj to be used to handle corruption
     * when sqlite reports database corruption
     * @return the newly opened database
     * @throws SQLiteException if the database cannot be opened
     */
    public static SQLiteDatabase openDatabase(String path, CursorFactory factory, int flags,
            DatabaseErrorHandler errorHandler) {
        SQLiteDatabase sqliteDatabase = openDatabase(path, factory, flags, errorHandler,
                (short) 0 /* the main connection handle */);

        // set sqlite pagesize to mBlockSize
        if (sBlockSize == 0) {
            // TODO: "/data" should be a static final String constant somewhere. it is hardcoded
            // in several places right now.
            sBlockSize = new StatFs("/data").getBlockSize();
        }
        sqliteDatabase.setPageSize(sBlockSize);
        sqliteDatabase.setJournalMode(path, "TRUNCATE");

        // add this database to the list of databases opened in this process
        synchronized(mActiveDatabases) {
            mActiveDatabases.add(new WeakReference<SQLiteDatabase>(sqliteDatabase));
        }
        return sqliteDatabase;
    }

    private static SQLiteDatabase openDatabase(String path, CursorFactory factory, int flags,
            DatabaseErrorHandler errorHandler, short connectionNum) {
        SQLiteDatabase db = new SQLiteDatabase(path, factory, flags, errorHandler, connectionNum);
        try {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.i(TAG, "opening the db : " + path);
            }
            // Open the database.
            db.dbopen(path, flags);
            db.setLocale(Locale.getDefault());
            if (SQLiteDebug.DEBUG_SQL_STATEMENTS) {
                db.enableSqlTracing(path, connectionNum);
            }
            if (SQLiteDebug.DEBUG_SQL_TIME) {
                db.enableSqlProfiling(path, connectionNum);
            }
            return db;
        } catch (SQLiteDatabaseCorruptException e) {
            db.mErrorHandler.onCorruption(db);
            return SQLiteDatabase.openDatabase(path, factory, flags, errorHandler);
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to open the database. closing it.", e);
            db.close();
            throw e;
        }
    }

    /**
     * Equivalent to openDatabase(file.getPath(), factory, CREATE_IF_NECESSARY).
     */
    public static SQLiteDatabase openOrCreateDatabase(File file, CursorFactory factory) {
        return openOrCreateDatabase(file.getPath(), factory);
    }

    /**
     * Equivalent to openDatabase(path, factory, CREATE_IF_NECESSARY).
     */
    public static SQLiteDatabase openOrCreateDatabase(String path, CursorFactory factory) {
        return openDatabase(path, factory, CREATE_IF_NECESSARY);
    }

    /**
     * Equivalent to openDatabase(path, factory, CREATE_IF_NECESSARY, errorHandler).
     */
    public static SQLiteDatabase openOrCreateDatabase(String path, CursorFactory factory,
            DatabaseErrorHandler errorHandler) {
        return openDatabase(path, factory, CREATE_IF_NECESSARY, errorHandler);
    }

    private void setJournalMode(final String dbPath, final String mode) {
        // journal mode can be set only for non-memory databases
        // AND can't be set for readonly databases
        if (dbPath.equalsIgnoreCase(MEMORY_DB_PATH) || isReadOnly()) {
            return;
        }
        String s = DatabaseUtils.stringForQuery(this, "PRAGMA journal_mode=" + mode, null);
        if (!s.equalsIgnoreCase(mode)) {
            Log.e(TAG, "setting journal_mode to " + mode + " failed for db: " + dbPath +
                    " (on pragma set journal_mode, sqlite returned:" + s);
        }
    }

    /**
     * Create a memory backed SQLite database.  Its contents will be destroyed
     * when the database is closed.
     *
     * <p>Sets the locale of the database to the  the system's current locale.
     * Call {@link #setLocale} if you would like something else.</p>
     *
     * @param factory an optional factory class that is called to instantiate a
     *            cursor when query is called
     * @return a SQLiteDatabase object, or null if the database can't be created
     */
    public static SQLiteDatabase create(CursorFactory factory) {
        // This is a magic string with special meaning for SQLite.
        return openDatabase(MEMORY_DB_PATH, factory, CREATE_IF_NECESSARY);
    }

    /**
     * Close the database.
     */
    public void close() {
        if (!isOpen()) {
            return;
        }
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.i(TAG, "closing db: " + mPath + " (connection # " + mConnectionNum);
        }
        lock();
        try {
            // some other thread could have closed this database while I was waiting for lock.
            // check the database state
            if (!isOpen()) {
                return;
            }
            closeClosable();
            // finalize ALL statements queued up so far
            closePendingStatements();
            releaseCustomFunctions();
            // close this database instance - regardless of its reference count value
            closeDatabase();
            if (mConnectionPool != null) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    assert mConnectionPool != null;
                    Log.i(TAG, mConnectionPool.toString());
                }
                mConnectionPool.close();
            }
        } finally {
            unlock();
        }
    }

    private void closeClosable() {
        /* deallocate all compiled SQL statement objects from mCompiledQueries cache.
         * this should be done before de-referencing all {@link SQLiteClosable} objects
         * from this database object because calling
         * {@link SQLiteClosable#onAllReferencesReleasedFromContainer()} could cause the database
         * to be closed. sqlite doesn't let a database close if there are
         * any unfinalized statements - such as the compiled-sql objects in mCompiledQueries.
         */
        deallocCachedSqlStatements();

        Iterator<Map.Entry<SQLiteClosable, Object>> iter = mPrograms.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<SQLiteClosable, Object> entry = iter.next();
            SQLiteClosable program = entry.getKey();
            if (program != null) {
                program.onAllReferencesReleasedFromContainer();
            }
        }
    }

    /**
     * package level access for testing purposes
     */
    /* package */ void closeDatabase() throws SQLiteException {
        try {
            dbclose();
        } catch (SQLiteUnfinalizedObjectsException e)  {
            String msg = e.getMessage();
            String[] tokens = msg.split(",", 2);
            int stmtId = Integer.parseInt(tokens[0]);
            // get extra info about this statement, if it is still to be released by closeClosable()
            Iterator<Map.Entry<SQLiteClosable, Object>> iter = mPrograms.entrySet().iterator();
            boolean found = false;
            while (iter.hasNext()) {
                Map.Entry<SQLiteClosable, Object> entry = iter.next();
                SQLiteClosable program = entry.getKey();
                if (program != null && program instanceof SQLiteProgram) {
                    SQLiteCompiledSql compiledSql = ((SQLiteProgram)program).mCompiledSql;
                    if (compiledSql.nStatement == stmtId) {
                        msg = compiledSql.toString();
                        found = true;
                    }
                }
            }
            if (!found) {
                // the statement is already released by closeClosable(). is it waiting to be
                // finalized?
                if (mClosedStatementIds.contains(stmtId)) {
                    Log.w(TAG, "this shouldn't happen. finalizing the statement now: ");
                    closePendingStatements();
                    // try to close the database again
                    closeDatabase();
                }
            } else {
                // the statement is not yet closed. most probably programming error in the app.
                throw new SQLiteUnfinalizedObjectsException(
                        "close() on database: " + getPath() +
                        " failed due to un-close()d SQL statements: " + msg);
            }
        }
    }

    /**
     * Native call to close the database.
     */
    private native void dbclose();

    /**
     * A callback interface for a custom sqlite3 function.
     * This can be used to create a function that can be called from
     * sqlite3 database triggers.
     * @hide
     */
    public interface CustomFunction {
        public void callback(String[] args);
    }

    /**
     * Registers a CustomFunction callback as a function that can be called from
     * sqlite3 database triggers.
     * @param name the name of the sqlite3 function
     * @param numArgs the number of arguments for the function
     * @param function callback to call when the function is executed
     * @hide
     */
    public void addCustomFunction(String name, int numArgs, CustomFunction function) {
        verifyDbIsOpen();
        synchronized (mCustomFunctions) {
            int ref = native_addCustomFunction(name, numArgs, function);
            if (ref != 0) {
                // save a reference to the function for cleanup later
                mCustomFunctions.add(new Integer(ref));
            } else {
                throw new SQLiteException("failed to add custom function " + name);
            }
        }
    }

    private void releaseCustomFunctions() {
        synchronized (mCustomFunctions) {
            for (int i = 0; i < mCustomFunctions.size(); i++) {
                Integer function = mCustomFunctions.get(i);
                native_releaseCustomFunction(function.intValue());
            }
            mCustomFunctions.clear();
        }
    }

    // list of CustomFunction references so we can clean up when the database closes
    private final ArrayList<Integer> mCustomFunctions =
            new ArrayList<Integer>();

    private native int native_addCustomFunction(String name, int numArgs, CustomFunction function);
    private native void native_releaseCustomFunction(int function);

    /**
     * Gets the database version.
     *
     * @return the database version
     */
    public int getVersion() {
        return ((Long) DatabaseUtils.longForQuery(this, "PRAGMA user_version;", null)).intValue();
    }

    /**
     * Sets the database version.
     *
     * @param version the new database version
     */
    public void setVersion(int version) {
        execSQL("PRAGMA user_version = " + version);
    }

    /**
     * Returns the maximum size the database may grow to.
     *
     * @return the new maximum database size
     */
    public long getMaximumSize() {
        long pageCount = DatabaseUtils.longForQuery(this, "PRAGMA max_page_count;", null);
        return pageCount * getPageSize();
    }

    /**
     * Sets the maximum size the database will grow to. The maximum size cannot
     * be set below the current size.
     *
     * @param numBytes the maximum database size, in bytes
     * @return the new maximum database size
     */
    public long setMaximumSize(long numBytes) {
        long pageSize = getPageSize();
        long numPages = numBytes / pageSize;
        // If numBytes isn't a multiple of pageSize, bump up a page
        if ((numBytes % pageSize) != 0) {
            numPages++;
        }
        long newPageCount = DatabaseUtils.longForQuery(this, "PRAGMA max_page_count = " + numPages,
                null);
        return newPageCount * pageSize;
    }

    /**
     * Returns the current database page size, in bytes.
     *
     * @return the database page size, in bytes
     */
    public long getPageSize() {
        return DatabaseUtils.longForQuery(this, "PRAGMA page_size;", null);
    }

    /**
     * Sets the database page size. The page size must be a power of two. This
     * method does not work if any data has been written to the database file,
     * and must be called right after the database has been created.
     *
     * @param numBytes the database page size, in bytes
     */
    public void setPageSize(long numBytes) {
        execSQL("PRAGMA page_size = " + numBytes);
    }

    /**
     * Mark this table as syncable. When an update occurs in this table the
     * _sync_dirty field will be set to ensure proper syncing operation.
     *
     * @param table the table to mark as syncable
     * @param deletedTable The deleted table that corresponds to the
     *          syncable table
     * @deprecated This method no longer serves any useful purpose and has been deprecated.
     */
    @Deprecated
    public void markTableSyncable(String table, String deletedTable) {
    }

    /**
     * Mark this table as syncable, with the _sync_dirty residing in another
     * table. When an update occurs in this table the _sync_dirty field of the
     * row in updateTable with the _id in foreignKey will be set to
     * ensure proper syncing operation.
     *
     * @param table an update on this table will trigger a sync time removal
     * @param foreignKey this is the column in table whose value is an _id in
     *          updateTable
     * @param updateTable this is the table that will have its _sync_dirty
     * @deprecated This method no longer serves any useful purpose and has been deprecated.
     */
    @Deprecated
    public void markTableSyncable(String table, String foreignKey, String updateTable) {
    }

    /**
     * Finds the name of the first table, which is editable.
     *
     * @param tables a list of tables
     * @return the first table listed
     */
    public static String findEditTable(String tables) {
        if (!TextUtils.isEmpty(tables)) {
            // find the first word terminated by either a space or a comma
            int spacepos = tables.indexOf(' ');
            int commapos = tables.indexOf(',');

            if (spacepos > 0 && (spacepos < commapos || commapos < 0)) {
                return tables.substring(0, spacepos);
            } else if (commapos > 0 && (commapos < spacepos || spacepos < 0) ) {
                return tables.substring(0, commapos);
            }
            return tables;
        } else {
            throw new IllegalStateException("Invalid tables");
        }
    }

    /**
     * Compiles an SQL statement into a reusable pre-compiled statement object.
     * The parameters are identical to {@link #execSQL(String)}. You may put ?s in the
     * statement and fill in those values with {@link SQLiteProgram#bindString}
     * and {@link SQLiteProgram#bindLong} each time you want to run the
     * statement. Statements may not return result sets larger than 1x1.
     *<p>
     * No two threads should be using the same {@link SQLiteStatement} at the same time.
     *
     * @param sql The raw SQL statement, may contain ? for unknown values to be
     *            bound later.
     * @return A pre-compiled {@link SQLiteStatement} object. Note that
     * {@link SQLiteStatement}s are not synchronized, see the documentation for more details.
     */
    public SQLiteStatement compileStatement(String sql) throws SQLException {
        verifyDbIsOpen();
        return new SQLiteStatement(this, sql, null);
    }

    /**
     * Query the given URL, returning a {@link Cursor} over the result set.
     *
     * @param distinct true if you want each row to be unique, false otherwise.
     * @param table The table name to compile the query against.
     * @param columns A list of which columns to return. Passing null will
     *            return all columns, which is discouraged to prevent reading
     *            data from storage that isn't going to be used.
     * @param selection A filter declaring which rows to return, formatted as an
     *            SQL WHERE clause (excluding the WHERE itself). Passing null
     *            will return all rows for the given table.
     * @param selectionArgs You may include ?s in selection, which will be
     *         replaced by the values from selectionArgs, in order that they
     *         appear in the selection. The values will be bound as Strings.
     * @param groupBy A filter declaring how to group rows, formatted as an SQL
     *            GROUP BY clause (excluding the GROUP BY itself). Passing null
     *            will cause the rows to not be grouped.
     * @param having A filter declare which row groups to include in the cursor,
     *            if row grouping is being used, formatted as an SQL HAVING
     *            clause (excluding the HAVING itself). Passing null will cause
     *            all row groups to be included, and is required when row
     *            grouping is not being used.
     * @param orderBy How to order the rows, formatted as an SQL ORDER BY clause
     *            (excluding the ORDER BY itself). Passing null will use the
     *            default sort order, which may be unordered.
     * @param limit Limits the number of rows returned by the query,
     *            formatted as LIMIT clause. Passing null denotes no LIMIT clause.
     * @return A {@link Cursor} object, which is positioned before the first entry. Note that
     * {@link Cursor}s are not synchronized, see the documentation for more details.
     * @see Cursor
     */
    public Cursor query(boolean distinct, String table, String[] columns,
            String selection, String[] selectionArgs, String groupBy,
            String having, String orderBy, String limit) {
        return queryWithFactory(null, distinct, table, columns, selection, selectionArgs,
                groupBy, having, orderBy, limit);
    }

    /**
     * Query the given URL, returning a {@link Cursor} over the result set.
     *
     * @param cursorFactory the cursor factory to use, or null for the default factory
     * @param distinct true if you want each row to be unique, false otherwise.
     * @param table The table name to compile the query against.
     * @param columns A list of which columns to return. Passing null will
     *            return all columns, which is discouraged to prevent reading
     *            data from storage that isn't going to be used.
     * @param selection A filter declaring which rows to return, formatted as an
     *            SQL WHERE clause (excluding the WHERE itself). Passing null
     *            will return all rows for the given table.
     * @param selectionArgs You may include ?s in selection, which will be
     *         replaced by the values from selectionArgs, in order that they
     *         appear in the selection. The values will be bound as Strings.
     * @param groupBy A filter declaring how to group rows, formatted as an SQL
     *            GROUP BY clause (excluding the GROUP BY itself). Passing null
     *            will cause the rows to not be grouped.
     * @param having A filter declare which row groups to include in the cursor,
     *            if row grouping is being used, formatted as an SQL HAVING
     *            clause (excluding the HAVING itself). Passing null will cause
     *            all row groups to be included, and is required when row
     *            grouping is not being used.
     * @param orderBy How to order the rows, formatted as an SQL ORDER BY clause
     *            (excluding the ORDER BY itself). Passing null will use the
     *            default sort order, which may be unordered.
     * @param limit Limits the number of rows returned by the query,
     *            formatted as LIMIT clause. Passing null denotes no LIMIT clause.
     * @return A {@link Cursor} object, which is positioned before the first entry. Note that
     * {@link Cursor}s are not synchronized, see the documentation for more details.
     * @see Cursor
     */
    public Cursor queryWithFactory(CursorFactory cursorFactory,
            boolean distinct, String table, String[] columns,
            String selection, String[] selectionArgs, String groupBy,
            String having, String orderBy, String limit) {
        verifyDbIsOpen();
        String sql = SQLiteQueryBuilder.buildQueryString(
                distinct, table, columns, selection, groupBy, having, orderBy, limit);

        return rawQueryWithFactory(
                cursorFactory, sql, selectionArgs, findEditTable(table));
    }

    /**
     * Query the given table, returning a {@link Cursor} over the result set.
     *
     * @param table The table name to compile the query against.
     * @param columns A list of which columns to return. Passing null will
     *            return all columns, which is discouraged to prevent reading
     *            data from storage that isn't going to be used.
     * @param selection A filter declaring which rows to return, formatted as an
     *            SQL WHERE clause (excluding the WHERE itself). Passing null
     *            will return all rows for the given table.
     * @param selectionArgs You may include ?s in selection, which will be
     *         replaced by the values from selectionArgs, in order that they
     *         appear in the selection. The values will be bound as Strings.
     * @param groupBy A filter declaring how to group rows, formatted as an SQL
     *            GROUP BY clause (excluding the GROUP BY itself). Passing null
     *            will cause the rows to not be grouped.
     * @param having A filter declare which row groups to include in the cursor,
     *            if row grouping is being used, formatted as an SQL HAVING
     *            clause (excluding the HAVING itself). Passing null will cause
     *            all row groups to be included, and is required when row
     *            grouping is not being used.
     * @param orderBy How to order the rows, formatted as an SQL ORDER BY clause
     *            (excluding the ORDER BY itself). Passing null will use the
     *            default sort order, which may be unordered.
     * @return A {@link Cursor} object, which is positioned before the first entry. Note that
     * {@link Cursor}s are not synchronized, see the documentation for more details.
     * @see Cursor
     */
    public Cursor query(String table, String[] columns, String selection,
            String[] selectionArgs, String groupBy, String having,
            String orderBy) {

        return query(false, table, columns, selection, selectionArgs, groupBy,
                having, orderBy, null /* limit */);
    }

    /**
     * Query the given table, returning a {@link Cursor} over the result set.
     *
     * @param table The table name to compile the query against.
     * @param columns A list of which columns to return. Passing null will
     *            return all columns, which is discouraged to prevent reading
     *            data from storage that isn't going to be used.
     * @param selection A filter declaring which rows to return, formatted as an
     *            SQL WHERE clause (excluding the WHERE itself). Passing null
     *            will return all rows for the given table.
     * @param selectionArgs You may include ?s in selection, which will be
     *         replaced by the values from selectionArgs, in order that they
     *         appear in the selection. The values will be bound as Strings.
     * @param groupBy A filter declaring how to group rows, formatted as an SQL
     *            GROUP BY clause (excluding the GROUP BY itself). Passing null
     *            will cause the rows to not be grouped.
     * @param having A filter declare which row groups to include in the cursor,
     *            if row grouping is being used, formatted as an SQL HAVING
     *            clause (excluding the HAVING itself). Passing null will cause
     *            all row groups to be included, and is required when row
     *            grouping is not being used.
     * @param orderBy How to order the rows, formatted as an SQL ORDER BY clause
     *            (excluding the ORDER BY itself). Passing null will use the
     *            default sort order, which may be unordered.
     * @param limit Limits the number of rows returned by the query,
     *            formatted as LIMIT clause. Passing null denotes no LIMIT clause.
     * @return A {@link Cursor} object, which is positioned before the first entry. Note that
     * {@link Cursor}s are not synchronized, see the documentation for more details.
     * @see Cursor
     */
    public Cursor query(String table, String[] columns, String selection,
            String[] selectionArgs, String groupBy, String having,
            String orderBy, String limit) {

        return query(false, table, columns, selection, selectionArgs, groupBy,
                having, orderBy, limit);
    }

    /**
     * Runs the provided SQL and returns a {@link Cursor} over the result set.
     *
     * @param sql the SQL query. The SQL string must not be ; terminated
     * @param selectionArgs You may include ?s in where clause in the query,
     *     which will be replaced by the val
