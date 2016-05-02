/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.server;

import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.AppGlobals;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.app.IBackupAgent;
import android.app.PendingIntent;
import android.app.backup.BackupAgent;
import android.app.backup.BackupDataOutput;
import android.app.backup.FullBackup;
import android.app.backup.RestoreSet;
import android.app.backup.IBackupManager;
import android.app.backup.IFullBackupRestoreObserver;
import android.app.backup.IRestoreObserver;
import android.app.backup.IRestoreSession;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.WorkSource;
import android.os.storage.IMountService;
import android.provider.Settings;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.StringBuilderPrinter;

import com.android.internal.backup.BackupConstants;
import com.android.internal.backup.IBackupTransport;
import com.android.internal.backup.LocalTransport;
import com.android.server.PackageManagerBackupAgent.Metadata;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

class BackupManagerService extends IBackupManager.Stub {
    private static final String TAG = "BackupManagerService";
    private static final boolean DEBUG = true;
    private static final boolean MORE_DEBUG = false;

    // Name and current contents version of the full-backup manifest file
    static final String BACKUP_MANIFEST_FILENAME = "_manifest";
    static final int BACKUP_MANIFEST_VERSION = 1;
    static final String BACKUP_FILE_HEADER_MAGIC = "ANDROID BACKUP\n";
    static final int BACKUP_FILE_VERSION = 1;
    static final boolean COMPRESS_FULL_BACKUPS = true; // should be true in production

    // How often we perform a backup pass.  Privileged external callers can
    // trigger an immediate pass.
    private static final long BACKUP_INTERVAL = AlarmManager.INTERVAL_HOUR;

    // Random variation in backup scheduling time to avoid server load spikes
    private static final int FUZZ_MILLIS = 5 * 60 * 1000;

    // The amount of time between the initial provisioning of the device and
    // the first backup pass.
    private static final long FIRST_BACKUP_INTERVAL = 12 * AlarmManager.INTERVAL_HOUR;

    private static final String RUN_BACKUP_ACTION = "android.app.backup.intent.RUN";
    private static final String RUN_INITIALIZE_ACTION = "android.app.backup.intent.INIT";
    private static final String RUN_CLEAR_ACTION = "android.app.backup.intent.CLEAR";
    private static final int MSG_RUN_BACKUP = 1;
    private static final int MSG_RUN_FULL_BACKUP = 2;
    private static final int MSG_RUN_RESTORE = 3;
    private static final int MSG_RUN_CLEAR = 4;
    private static final int MSG_RUN_INITIALIZE = 5;
    private static final int MSG_RUN_GET_RESTORE_SETS = 6;
    private static final int MSG_TIMEOUT = 7;
    private static final int MSG_RESTORE_TIMEOUT = 8;
    private static final int MSG_FULL_CONFIRMATION_TIMEOUT = 9;
    private static final int MSG_RUN_FULL_RESTORE = 10;

    // backup task state machine tick
    static final int MSG_BACKUP_RESTORE_STEP = 20;
    static final int MSG_OP_COMPLETE = 21;

    // Timeout interval for deciding that a bind or clear-data has taken too long
    static final long TIMEOUT_INTERVAL = 10 * 1000;

    // Timeout intervals for agent backup & restore operations
    static final long TIMEOUT_BACKUP_INTERVAL = 30 * 1000;
    static final long TIMEOUT_FULL_BACKUP_INTERVAL = 5 * 60 * 1000;
    static final long TIMEOUT_SHARED_BACKUP_INTERVAL = 30 * 60 * 1000;
    static final long TIMEOUT_RESTORE_INTERVAL = 60 * 1000;

    // User confirmation timeout for a full backup/restore operation.  It's this long in
    // order to give them time to enter the backup password.
    static final long TIMEOUT_FULL_CONFIRMATION = 60 * 1000;

    private Context mContext;
    private PackageManager mPackageManager;
    IPackageManager mPackageManagerBinder;
    private IActivityManager mActivityManager;
    private PowerManager mPowerManager;
    private AlarmManager mAlarmManager;
    private IMountService mMountService;
    IBackupManager mBackupManagerBinder;

    boolean mEnabled;   // access to this is synchronized on 'this'
    boolean mProvisioned;
    boolean mAutoRestore;
    PowerManager.WakeLock mWakelock;
    HandlerThread mHandlerThread;
    BackupHandler mBackupHandler;
    PendingIntent mRunBackupIntent, mRunInitIntent;
    BroadcastReceiver mRunBackupReceiver, mRunInitReceiver;
    // map UIDs to the set of backup client services within that UID's app set
    final SparseArray<HashSet<ApplicationInfo>> mBackupParticipants
        = new SparseArray<HashSet<ApplicationInfo>>();
    // set of backup services that have pending changes
    class BackupRequest {
        public String packageName;

        BackupRequest(String pkgName) {
            packageName = pkgName;
        }

        public String toString() {
            return "BackupRequest{pkg=" + packageName + "}";
        }
    }
    // Backups that we haven't started yet.  Keys are package names.
    HashMap<String,BackupRequest> mPendingBackups
            = new HashMap<String,BackupRequest>();

    // Pseudoname that we use for the Package Manager metadata "package"
    static final String PACKAGE_MANAGER_SENTINEL = "@pm@";

    // locking around the pending-backup management
    final Object mQueueLock = new Object();

    // The thread performing the sequence of queued backups binds to each app's agent
    // in succession.  Bind notifications are asynchronously delivered through the
    // Activity Manager; use this lock object to signal when a requested binding has
    // completed.
    final Object mAgentConnectLock = new Object();
    IBackupAgent mConnectedAgent;
    volatile boolean mBackupRunning;
    volatile boolean mConnecting;
    volatile long mLastBackupPass;
    volatile long mNextBackupPass;

    // A similar synchronization mechanism around clearing apps' data for restore
    final Object mClearDataLock = new Object();
    volatile boolean mClearingData;

    // Transport bookkeeping
    final HashMap<String,IBackupTransport> mTransports
            = new HashMap<String,IBackupTransport>();
    String mCurrentTransport;
    IBackupTransport mLocalTransport, mGoogleTransport;
    ActiveRestoreSession mActiveRestoreSession;

    class RestoreGetSetsParams {
        public IBackupTransport transport;
        public ActiveRestoreSession session;
        public IRestoreObserver observer;

        RestoreGetSetsParams(IBackupTransport _transport, ActiveRestoreSession _session,
                IRestoreObserver _observer) {
            transport = _transport;
            session = _session;
            observer = _observer;
        }
    }

    class RestoreParams {
        public IBackupTransport transport;
        public IRestoreObserver observer;
        public long token;
        public PackageInfo pkgInfo;
        public int pmToken; // in post-install restore, the PM's token for this transaction
        public boolean needFullBackup;
        public String[] filterSet;

        RestoreParams(IBackupTransport _transport, IRestoreObserver _obs,
                long _token, PackageInfo _pkg, int _pmToken, boolean _needFullBackup) {
            transport = _transport;
            observer = _obs;
            token = _token;
            pkgInfo = _pkg;
            pmToken = _pmToken;
            needFullBackup = _needFullBackup;
            filterSet = null;
        }

        RestoreParams(IBackupTransport _transport, IRestoreObserver _obs, long _token,
                boolean _needFullBackup) {
            transport = _transport;
            observer = _obs;
            token = _token;
            pkgInfo = null;
            pmToken = 0;
            needFullBackup = _needFullBackup;
            filterSet = null;
        }

        RestoreParams(IBackupTransport _transport, IRestoreObserver _obs, long _token,
                String[] _filterSet, boolean _needFullBackup) {
            transport = _transport;
            observer = _obs;
            token = _token;
            pkgInfo = null;
            pmToken = 0;
            needFullBackup = _needFullBackup;
            filterSet = _filterSet;
        }
    }

    class ClearParams {
        public IBackupTransport transport;
        public PackageInfo packageInfo;

        ClearParams(IBackupTransport _transport, PackageInfo _info) {
            transport = _transport;
            packageInfo = _info;
        }
    }

    class FullParams {
        public ParcelFileDescriptor fd;
        public final AtomicBoolean latch;
        public IFullBackupRestoreObserver observer;
        public String curPassword;     // filled in by the confirmation step
        public String encryptPassword;

        FullParams() {
            latch = new AtomicBoolean(false);
        }
    }

    class FullBackupParams extends FullParams {
        public boolean includeApks;
        public boolean includeShared;
        public boolean allApps;
        public boolean includeSystem;
        public String[] packages;

        FullBackupParams(ParcelFileDescriptor output, boolean saveApks, boolean saveShared,
                boolean doAllApps, boolean doSystem, String[] pkgList) {
            fd = output;
            includeApks = saveApks;
            includeShared = saveShared;
            allApps = doAllApps;
            includeSystem = doSystem;
            packages = pkgList;
        }
    }

    class FullRestoreParams extends FullParams {
        FullRestoreParams(ParcelFileDescriptor input) {
            fd = input;
        }
    }

    // Bookkeeping of in-flight operations for timeout etc. purposes.  The operation
    // token is the index of the entry in the pending-operations list.
    static final int OP_PENDING = 0;
    static final int OP_ACKNOWLEDGED = 1;
    static final int OP_TIMEOUT = -1;

    class Operation {
        public int state;
        public BackupRestoreTask callback;

        Operation(int initialState, BackupRestoreTask callbackObj) {
            state = initialState;
            callback = callbackObj;
        }
    }
    final SparseArray<Operation> mCurrentOperations = new SparseArray<Operation>();
    final Object mCurrentOpLock = new Object();
    final Random mTokenGenerator = new Random();

    final SparseArray<FullParams> mFullConfirmations = new SparseArray<FullParams>();

    // Where we keep our journal files and other bookkeeping
    File mBaseStateDir;
    File mDataDir;
    File mJournalDir;
    File mJournal;

    // Backup password, if any, and the file where it's saved.  What is stored is not the
    // password text itself; it's the result of a PBKDF2 hash with a randomly chosen (but
    // persisted) salt.  Validation is performed by running the challenge text through the
    // same PBKDF2 cycle with the persisted salt; if the resulting derived key string matches
    // the saved hash string, then the challenge text matches the originally supplied
    // password text.
    private final SecureRandom mRng = new SecureRandom();
    private String mPasswordHash;
    private File mPasswordHashFile;
    private byte[] mPasswordSalt;

    // Configuration of PBKDF2 that we use for generating pw hashes and intermediate keys
    static final int PBKDF2_HASH_ROUNDS = 10000;
    static final int PBKDF2_KEY_SIZE = 256;     // bits
    static final int PBKDF2_SALT_SIZE = 512;    // bits
    static final String ENCRYPTION_ALGORITHM_NAME = "AES-256";

    // Keep a log of all the apps we've ever backed up, and what the
    // dataset tokens are for both the current backup dataset and
    // the ancestral dataset.
    private File mEverStored;
    HashSet<String> mEverStoredApps = new HashSet<String>();

    static final int CURRENT_ANCESTRAL_RECORD_VERSION = 1;  // increment when the schema changes
    File mTokenFile;
    Set<String> mAncestralPackages = null;
    long mAncestralToken = 0;
    long mCurrentToken = 0;

    // Persistently track the need to do a full init
    static final String INIT_SENTINEL_FILE_NAME = "_need_init_";
    HashSet<String> mPendingInits = new HashSet<String>();  // transport names

    // Utility: build a new random integer token
    int generateToken() {
        int token;
        do {
            synchronized (mTokenGenerator) {
                token = mTokenGenerator.nextInt();
            }
        } while (token < 0);
        return token;
    }

    // ----- Asynchronous backup/restore handler thread -----

    private class BackupHandler extends Handler {
        public BackupHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {

            switch (msg.what) {
            case MSG_RUN_BACKUP:
            {
                mLastBackupPass = System.currentTimeMillis();
                mNextBackupPass = mLastBackupPass + BACKUP_INTERVAL;

                IBackupTransport transport = getTransport(mCurrentTransport);
                if (transport == null) {
                    Slog.v(TAG, "Backup requested but no transport available");
                    synchronized (mQueueLock) {
                        mBackupRunning = false;
                    }
                    mWakelock.release();
                    break;
                }

                // snapshot the pending-backup set and work on that
                ArrayList<BackupRequest> queue = new ArrayList<BackupRequest>();
                File oldJournal = mJournal;
                synchronized (mQueueLock) {
                    // Do we have any work to do?  Construct the work queue
                    // then release the synchronization lock to actually run
                    // the backup.
                    if (mPendingBackups.size() > 0) {
                        for (BackupRequest b: mPendingBackups.values()) {
                            queue.add(b);
                        }
                        if (DEBUG) Slog.v(TAG, "clearing pending backups");
                        mPendingBackups.clear();

                        // Start a new backup-queue journal file too
                        mJournal = null;

                    }
                }

                // At this point, we have started a new journal file, and the old
                // file identity is being passed to the backup processing task.
                // When it completes successfully, that old journal file will be
                // deleted.  If we crash prior to that, the old journal is parsed
                // at next boot and the journaled requests fulfilled.
                if (queue.size() > 0) {
                    // Spin up a backup state sequence and set it running
                    PerformBackupTask pbt = new PerformBackupTask(transport, queue, oldJournal);
                    Message pbtMessage = obtainMessage(MSG_BACKUP_RESTORE_STEP, pbt);
                    sendMessage(pbtMessage);
                } else {
                    Slog.v(TAG, "Backup requested but nothing pending");
                    synchronized (mQueueLock) {
                        mBackupRunning = false;
                    }
                    mWakelock.release();
                }
                break;
            }

            case MSG_BACKUP_RESTORE_STEP:
            {
                try {
                    BackupRestoreTask task = (BackupRestoreTask) msg.obj;
                    if (MORE_DEBUG) Slog.v(TAG, "Got next step for " + task + ", executing");
                    task.execute();
                } catch (ClassCastException e) {
                    Slog.e(TAG, "Invalid backup task in flight, obj=" + msg.obj);
                }
                break;
            }

            case MSG_OP_COMPLETE:
            {
                try {
                    BackupRestoreTask task = (BackupRestoreTask) msg.obj;
                    task.operationComplete();
                } catch (ClassCastException e) {
                    Slog.e(TAG, "Invalid completion in flight, obj=" + msg.obj);
                }
                break;
            }

            case MSG_RUN_FULL_BACKUP:
            {
                // TODO: refactor full backup to be a looper-based state machine
                // similar to normal backup/restore.
                FullBackupParams params = (FullBackupParams)msg.obj;
                PerformFullBackupTask task = new PerformFullBackupTask(params.fd,
                        params.observer, params.includeApks,
                        params.includeShared, params.curPassword, params.encryptPassword,
                        params.allApps, params.includeSystem, params.packages, params.latch);
                (new Thread(task)).start();
                break;
            }

            case MSG_RUN_RESTORE:
            {
                RestoreParams params = (RestoreParams)msg.obj;
                Slog.d(TAG, "MSG_RUN_RESTORE observer=" + params.observer);
                PerformRestoreTask task = new PerformRestoreTask(
                        params.transport, params.observer,
                        params.token, params.pkgInfo, params.pmToken,
                        params.needFullBackup, params.filterSet);
                Message restoreMsg = obtainMessage(MSG_BACKUP_RESTORE_STEP, task);
                sendMessage(restoreMsg);
                break;
            }

            case MSG_RUN_FULL_RESTORE:
            {
                // TODO: refactor full restore to be a looper-based state machine
                // similar to normal backup/restore.
                FullRestoreParams params = (FullRestoreParams)msg.obj;
                PerformFullRestoreTask task = new PerformFullRestoreTask(params.fd,
                        params.curPassword, params.encryptPassword,
                        params.observer, params.latch);
                (new Thread(task)).start();
                break;
            }

            case MSG_RUN_CLEAR:
            {
                ClearParams params = (ClearParams)msg.obj;
                (new PerformClearTask(params.transport, params.packageInfo)).run();
                break;
            }

            case MSG_RUN_INITIALIZE:
            {
                HashSet<String> queue;

                // Snapshot the pending-init queue and work on that
                synchronized (mQueueLock) {
                    queue = new HashSet<String>(mPendingInits);
                    mPendingInits.clear();
                }

                (new PerformInitializeTask(queue)).run();
                break;
            }

            case MSG_RUN_GET_RESTORE_SETS:
            {
                // Like other async operations, this is entered with the wakelock held
                RestoreSet[] sets = null;
                RestoreGetSetsParams params = (RestoreGetSetsParams)msg.obj;
                try {
                    sets = params.transport.getAvailableRestoreSets();
                    // cache the result in the active session
                    synchronized (params.session) {
                        params.session.mRestoreSets = sets;
                    }
                    if (sets == null) EventLog.writeEvent(EventLogTags.RESTORE_TRANSPORT_FAILURE);
                } catch (Exception e) {
                    Slog.e(TAG, "Error from transport getting set list");
                } finally {
                    if (params.observer != null) {
                        try {
                            params.observer.restoreSetsAvailable(sets);
                        } catch (RemoteException re) {
                            Slog.e(TAG, "Unable to report listing to observer");
                        } catch (Exception e) {
                            Slog.e(TAG, "Restore observer threw", e);
                        }
                    }

                    // Done: reset the session timeout clock
                    removeMessages(MSG_RESTORE_TIMEOUT);
                    sendEmptyMessageDelayed(MSG_RESTORE_TIMEOUT, TIMEOUT_RESTORE_INTERVAL);

                    mWakelock.release();
                }
                break;
            }

            case MSG_TIMEOUT:
            {
                handleTimeout(msg.arg1, msg.obj);
                break;
            }

            case MSG_RESTORE_TIMEOUT:
            {
                synchronized (BackupManagerService.this) {
                    if (mActiveRestoreSession != null) {
                        // Client app left the restore session dangling.  We know that it
                        // can't be in the middle of an actual restore operation because
                        // the timeout is suspended while a restore is in progress.  Clean
                        // up now.
                        Slog.w(TAG, "Restore session timed out; aborting");
                        post(mActiveRestoreSession.new EndRestoreRunnable(
                                BackupManagerService.this, mActiveRestoreSession));
                    }
                }
            }

            case MSG_FULL_CONFIRMATION_TIMEOUT:
            {
                synchronized (mFullConfirmations) {
                    FullParams params = mFullConfirmations.get(msg.arg1);
                    if (params != null) {
                        Slog.i(TAG, "Full backup/restore timed out waiting for user confirmation");

                        // Release the waiter; timeout == completion
                        signalFullBackupRestoreCompletion(params);

                        // Remove the token from the set
                        mFullConfirmations.delete(msg.arg1);

                        // Report a timeout to the observer, if any
                        if (params.observer != null) {
                            try {
                                params.observer.onTimeout();
                            } catch (RemoteException e) {
                                /* don't care if the app has gone away */
                            }
                        }
                    } else {
                        Slog.d(TAG, "couldn't find params for token " + msg.arg1);
                    }
                }
                break;
            }
            }
        }
    }

    // ----- Main service implementation -----

    public BackupManagerService(Context context) {
        mContext = context;
        mPackageManager = context.getPackageManager();
        mPackageManagerBinder = AppGlobals.getPackageManager();
        mActivityManager = ActivityManagerNative.getDefault();

        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mMountService = IMountService.Stub.asInterface(ServiceManager.getService("mount"));

        mBackupManagerBinder = asInterface(asBinder());

        // spin up the backup/restore handler thread
        mHandlerThread = new HandlerThread("backup", Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        mBackupHandler = new BackupHandler(mHandlerThread.getLooper());

        // Set up our bookkeeping
        boolean areEnabled = Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.BACKUP_ENABLED, 0) != 0;
        mProvisioned = Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.BACKUP_PROVISIONED, 0) != 0;
        mAutoRestore = Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.BACKUP_AUTO_RESTORE, 1) != 0;
        // If Encrypted file systems is enabled or disabled, this call will return the
        // correct directory.
        mBaseStateDir = new File(Environment.getSecureDataDirectory(), "backup");
        mBaseStateDir.mkdirs();
        mDataDir = Environment.getDownloadCacheDirectory();

        mPasswordHashFile = new File(mBaseStateDir, "pwhash");
        if (mPasswordHashFile.exists()) {
            FileInputStream fin = null;
            DataInputStream in = null;
            try {
                fin = new FileInputStream(mPasswordHashFile);
                in = new DataInputStream(new BufferedInputStream(fin));
                // integer length of the salt array, followed by the salt,
                // then the hex pw hash string
                int saltLen = in.readInt();
                byte[] salt = new byte[saltLen];
                in.readFully(salt);
                mPasswordHash = in.readUTF();
                mPasswordSalt = salt;
            } catch (IOException e) {
                Slog.e(TAG, "Unable to read saved backup pw hash");
            } finally {
                try {
                    if (in != null) in.close();
                    if (fin != null) fin.close();
                } catch (IOException e) {
                    Slog.w(TAG, "Unable to close streams");
                }
            }
        }

        // Alarm receivers for scheduled backups & initialization operations
        mRunBackupReceiver = new RunBackupReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(RUN_BACKUP_ACTION);
        context.registerReceiver(mRunBackupReceiver, filter,
                android.Manifest.permission.BACKUP, null);

        mRunInitReceiver = new RunInitializeReceiver();
        filter = new IntentFilter();
        filter.addAction(RUN_INITIALIZE_ACTION);
        context.registerReceiver(mRunInitReceiver, filter,
                android.Manifest.permission.BACKUP, null);

        Intent backupIntent = new Intent(RUN_BACKUP_ACTION);
        backupIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        mRunBackupIntent = PendingIntent.getBroadcast(context, MSG_RUN_BACKUP, backupIntent, 0);

        Intent initIntent = new Intent(RUN_INITIALIZE_ACTION);
        backupIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        mRunInitIntent = PendingIntent.getBroadcast(context, MSG_RUN_INITIALIZE, initIntent, 0);

        // Set up the backup-request journaling
        mJournalDir = new File(mBaseStateDir, "pending");
        mJournalDir.mkdirs();   // creates mBaseStateDir along the way
        mJournal = null;        // will be created on first use

        // Set up the various sorts of package tracking we do
        initPackageTracking();

        // Build our mapping of uid to backup client services.  This implicitly
        // schedules a backup pass on the Package Manager metadata the first
        // time anything needs to be backed up.
        synchronized (mBackupParticipants) {
            addPackageParticipantsLocked(null);
        }

        // Set up our transport options and initialize the default transport
        // TODO: Have transports register themselves somehow?
        // TODO: Don't create transports that we don't need to?
        mLocalTransport = new LocalTransport(context);  // This is actually pretty cheap
        ComponentName localName = new ComponentName(context, LocalTransport.class);
        registerTransport(localName.flattenToShortString(), mLocalTransport);

        mGoogleTransport = null;
        mCurrentTransport = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.BACKUP_TRANSPORT);
        if ("".equals(mCurrentTransport)) {
            mCurrentTransport = null;
        }
        if (DEBUG) Slog.v(TAG, "Starting with transport " + mCurrentTransport);

        // Attach to the Google backup transport.  When this comes up, it will set
        // itself as the current transport because we explicitly reset mCurrentTransport
        // to null.
        ComponentName transportComponent = new ComponentName("com.google.android.backup",
                "com.google.android.backup.BackupTransportService");
        try {
            // If there's something out there that is supposed to be the Google
            // backup transport, make sure it's legitimately part of the OS build
            // and not an app lying about its package name.
            ApplicationInfo info = mPackageManager.getApplicationInfo(
                    transportComponent.getPackageName(), 0);
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                if (DEBUG) Slog.v(TAG, "Binding to Google transport");
                Intent intent = new Intent().setComponent(transportComponent);
                context.bindService(intent, mGoogleConnection, Context.BIND_AUTO_CREATE);
            } else {
                Slog.w(TAG, "Possible Google transport spoof: ignoring " + info);
            }
        } catch (PackageManager.NameNotFoundException nnf) {
            // No such package?  No binding.
            if (DEBUG) Slog.v(TAG, "Google transport not present");
        }

        // Now that we know about valid backup participants, parse any
        // leftover journal files into the pending backup set
        parseLeftoverJournals();

        // Power management
        mWakelock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "*backup*");

        // Start the backup passes going
        setBackupEnabled(areEnabled);
    }

    private class RunBackupReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (RUN_BACKUP_ACTION.equals(intent.getAction())) {
                synchronized (mQueueLock) {
                    if (mPendingInits.size() > 0) {
                        // If there are pending init operations, we process those
                        // and then settle into the usual periodic backup schedule.
                        if (DEBUG) Slog.v(TAG, "Init pending at scheduled backup");
                        try {
                            mAlarmManager.cancel(mRunInitIntent);
                            mRunInitIntent.send();
                        } catch (PendingIntent.CanceledException ce) {
                            Slog.e(TAG, "Run init intent cancelled");
                            // can't really do more than bail here
                        }
                    } else {
                        // Don't run backups now if we're disabled or not yet
                        // fully set up.
                        if (mEnabled && mProvisioned) {
                            if (!mBackupRunning) {
                                if (DEBUG) Slog.v(TAG, "Running a backup pass");

                                // Acquire the wakelock and pass it to the backup thread.  it will
                                // be released once backup concludes.
                                mBackupRunning = true;
                                mWakelock.acquire();

                                Message msg = mBackupHandler.obtainMessage(MSG_RUN_BACKUP);
                                mBackupHandler.sendMessage(msg);
                            } else {
                                Slog.i(TAG, "Backup time but one already running");
                            }
                        } else {
                            Slog.w(TAG, "Backup pass but e=" + mEnabled + " p=" + mProvisioned);
                        }
                    }
                }
            }
        }
    }

    private class RunInitializeReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (RUN_INITIALIZE_ACTION.equals(intent.getAction())) {
                synchronized (mQueueLock) {
                    if (DEBUG) Slog.v(TAG, "Running a device init");

                    // Acquire the wakelock and pass it to the init thread.  it will
                    // be released once init concludes.
                    mWakelock.acquire();

                    Message msg = mBackupHandler.obtainMessage(MSG_RUN_INITIALIZE);
                    mBackupHandler.sendMessage(msg);
                }
            }
        }
    }

    private void initPackageTracking() {
        if (DEBUG) Slog.v(TAG, "Initializing package tracking");

        // Remember our ancestral dataset
        mTokenFile = new File(mBaseStateDir, "ancestral");
        try {
            RandomAccessFile tf = new RandomAccessFile(mTokenFile, "r");
            int version = tf.readInt();
            if (version == CURRENT_ANCESTRAL_RECORD_VERSION) {
                mAncestralToken = tf.readLong();
                mCurrentToken = tf.readLong();

                int numPackages = tf.readInt();
                if (numPackages >= 0) {
                    mAncestralPackages = new HashSet<String>();
                    for (int i = 0; i < numPackages; i++) {
                        String pkgName = tf.readUTF();
                        mAncestralPackages.add(pkgName);
                    }
                }
            }
            tf.close();
        } catch (FileNotFoundException fnf) {
            // Probably innocuous
            Slog.v(TAG, "No ancestral data");
        } catch (IOException e) {
            Slog.w(TAG, "Unable to read token file", e);
        }

        // Keep a log of what apps we've ever backed up.  Because we might have
        // rebooted in the middle of an operation that was removing something from
        // this log, we sanity-check its contents here and reconstruct it.
        mEverStored = new File(mBaseStateDir, "processed");
        File tempProcessedFile = new File(mBaseStateDir, "processed.new");

        // If we were in the middle of removing something from the ever-backed-up
        // file, there might be a transient "processed.new" file still present.
        // Ignore it -- we'll validate "processed" against the current package set.
        if (tempProcessedFile.exists()) {
            tempProcessedFile.delete();
        }

        // If there are previous contents, parse them out then start a new
        // file to continue the recordkeeping.
        if (mEverStored.exists()) {
            RandomAccessFile temp = null;
            RandomAccessFile in = null;

            try {
                temp = new RandomAccessFile(tempProcessedFile, "rws");
                in = new RandomAccessFile(mEverStored, "r");

                while (true) {
                    PackageInfo info;
                    String pkg = in.readUTF();
                    try {
                        info = mPackageManager.getPackageInfo(pkg, 0);
                        mEverStoredApps.add(pkg);
                        temp.writeUTF(pkg);
                        if (MORE_DEBUG) Slog.v(TAG, "   + " + pkg);
                    } catch (NameNotFoundException e) {
                        // nope, this package was uninstalled; don't include it
                        if (MORE_DEBUG) Slog.v(TAG, "   - " + pkg);
                    }
                }
            } catch (EOFException e) {
                // Once we've rewritten the backup history log, atomically replace the
                // old one with the new one then reopen the file for continuing use.
                if (!tempProcessedFile.renameTo(mEverStored)) {
                    Slog.e(TAG, "Error renaming " + tempProcessedFile + " to " + mEverStored);
                }
            } catch (IOException e) {
                Slog.e(TAG, "Error in processed file", e);
            } finally {
                try { if (temp != null) temp.close(); } catch (IOException e) {}
                try { if (in != null) in.close(); } catch (IOException e) {}
            }
        }

        // Register for broadcasts about package install, etc., so we can
        // update the provider list.
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addDataScheme("package");
        mContext.registerReceiver(mBroadcastReceiver, filter);
        // Register for events related to sdcard installation.
        IntentFilter sdFilter = new IntentFilter();
        sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        mContext.registerReceiver(mBroadcastReceiver, sdFilter);
    }

    private void parseLeftoverJournals() {
        for (File f : mJournalDir.listFiles()) {
            if (mJournal == null || f.compareTo(mJournal) != 0) {
                // This isn't the current journal, so it must be a leftover.  Read
                // out the package names mentioned there and schedule them for
                // backup.
                RandomAccessFile in = null;
                try {
                    Slog.i(TAG, "Found stale backup journal, scheduling");
                    in = new RandomAccessFile(f, "r");
                    while (true) {
                        String packageName = in.readUTF();
                        Slog.i(TAG, "  " + packageName);
                        dataChangedImpl(packageName);
                    }
                } catch (EOFException e) {
                    // no more data; we're done
                } catch (Exception e) {
                    Slog.e(TAG, "Can't read " + f, e);
                } finally {
                    // close/delete the file
                    try { if (in != null) in.close(); } catch (IOException e) {}
                    f.delete();
                }
            }
        }
    }

    private SecretKey buildPasswordKey(String pw, byte[] salt, int rounds) {
        return buildCharArrayKey(pw.toCharArray(), salt, rounds);
    }

    private SecretKey buildCharArrayKey(char[] pwArray, byte[] salt, int rounds) {
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec ks = new PBEKeySpec(pwArray, salt, rounds, PBKDF2_KEY_SIZE);
            return keyFactory.generateSecret(ks);
        } catch (InvalidKeySpecException e) {
            Slog.e(TAG, "Invalid key spec for PBKDF2!");
        } catch (NoSuchAlgorithmException e) {
            Slog.e(TAG, "PBKDF2 unavailable!");
        }
        return null;
    }

    private String buildPasswordHash(String pw, byte[] salt, int rounds) {
        SecretKey key = buildPasswordKey(pw, salt, rounds);
        if (key != null) {
            return byteArrayToHex(key.getEncoded());
        }
        return null;
    }

    private String byteArrayToHex(byte[] data) {
        StringBuilder buf = new StringBuilder(data.length * 2);
        for (int i = 0; i < data.length; i++) {
            buf.append(Byte.toHexString(data[i], true));
        }
        return buf.toString();
    }

    private byte[] hexToByteArray(String digits) {
        final int bytes = digits.length() / 2;
        if (2*bytes != digits.length()) {
            throw new IllegalArgumentException("Hex string must have an even number of digits");
        }

        byte[] result = new byte[bytes];
        for (int i = 0; i < digits.length(); i += 2) {
            result[i/2] = (byte) Integer.parseInt(digits.substring(i, i+2), 16);
        }
        return result;
    }

    private byte[] makeKeyChecksum(byte[] pwBytes, byte[] salt, int rounds) {
        char[] mkAsChar = new char[pwBytes.length];
        for (int i = 0; i < pwBytes.length; i++) {
            mkAsChar[i] = (char) pwBytes[i];
        }

        Key checksum = buildCharArrayKey(mkAsChar, salt, rounds);
        return checksum.getEncoded();
    }

    // Used for generating random salts or passwords
    private byte[] randomBytes(int bits) {
        byte[] array = new byte[bits / 8];
        mRng.nextBytes(array);
        return array;
    }

    // Backup password management
    boolean passwordMatchesSaved(String candidatePw, int rounds) {
        // First, on an encrypted device we require matching the device pw
        final boolean isEncrypted;
        try {
            isEncrypted = (mMountService.getEncryptionState() != MountService.ENCRYPTION_STATE_NONE);
            if (isEncrypted) {
                if (DEBUG) {
                    Slog.i(TAG, "Device encrypted; verifying against device data pw");
                }
                // 0 means the password validated
                // -2 means device not encrypted
                // Any other result is either password failure or an error condition,
                // so we refuse the match
                final int result = mMountService.verifyEncryptionPassword(candidatePw);
                if (result == 0) {
                    if (MORE_DEBUG) Slog.d(TAG, "Pw verifies");
                    return true;
                } else if (result != -2) {
                    if (MORE_DEBUG) Slog.d(TAG, "Pw mismatch");
                    return false;
                } else {
                    // ...else the device is supposedly not encrypted.  HOWEVER, the
                    // query about the encryption state said that the device *is*
                    // encrypted, so ... we may have a problem.  Log it and refuse
                    // the backup.
                    Slog.e(TAG, "verified encryption state mismatch against query; no match allowed");
                    return false;
                }
            }
        } catch (Exception e) {
            // Something went wrong talking to the mount service.  This is very bad;
            // assume that we fail password validation.
            return false;
        }

        if (mPasswordHash == null) {
            // no current password case -- require that 'currentPw' be null or empty
            if (candidatePw == null || "".equals(candidatePw)) {
                return true;
            } // else the non-empty candidate does not match the empty stored pw
        } else {
            // hash the stated current pw and compare to the stored one
            if (candidatePw != null && candidatePw.length() > 0) {
                String currentPwHash = buildPasswordHash(candidatePw, mPasswordSalt, rounds);
                if (mPasswordHash.equalsIgnoreCase(currentPwHash)) {
                    // candidate hash matches the stored hash -- the password matches
                    return true;
                }
            } // else the stored pw is nonempty but the candidate is empty; no match
        }
        return false;
    }

    @Override
    public boolean setBackupPassword(String currentPw, String newPw) {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.BACKUP,
                "setBackupPassword");

        // If the supplied pw doesn't hash to the the saved one, fail
        if (!passwordMatchesSaved(currentPw, PBKDF2_HASH_ROUNDS)) {
            return false;
        }

        // Clearing the password is okay
        if (newPw == null || newPw.isEmpty()) {
            if (mPasswordHashFile.exists()) {
                if (!mPasswordHashFile.delete()) {
                    // Unable to delete the old pw file, so fail
                    Slog.e(TAG, "Unable to clear backup password");
                    return false;
                }
            }
            mPasswordHash = null;
            mPasswordSalt = null;
            return true;
        }

        try {
            // Okay, build the hash of the new backup password
            byte[] salt = randomBytes(PBKDF2_SALT_SIZE);
            String newPwHash = buildPasswordHash(newPw, salt, PBKDF2_HASH_ROUNDS);

            OutputStream pwf = null, buffer = null;
            DataOutputStream out = null;
            try {
                pwf = new FileOutputStream(mPasswordHashFile);
                buffer = new BufferedOutputStream(pwf);
                out = new DataOutputStream(buffer);
                // integer length of the salt array, followed by the salt,
                // then the hex pw hash string
                out.writeInt(salt.length);
                out.write(salt);
                out.writeUTF(newPwHash);
                out.flush();
                mPasswordHash = newPwHash;
                mPasswordSalt = salt;
                return true;
            } finally {
                if (out != null) out.close();
                if (buffer != null) buffer.close();
                if (pwf != null) pwf.close();
            }
        } catch (IOException e) {
            Slog.e(TAG, "Unable to set backup password");
        }
        return false;
    }

    @Override
    public boolean hasBackupPassword() {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.BACKUP,
                "hasBackupPassword");

        try {
            return (mMountService.getEncryptionState() != IMountService.ENCRYPTION_STATE_NONE)
                || (mPasswordHash != null && mPasswordHash.length() > 0);
        } catch (Exception e) {
            // If we can't talk to the mount service we have a serious problem; fail
            // "secure" i.e. assuming that we require a password
            return true;
        }
    }

    // Maintain persistent state around whether need to do an initialize operation.
    // Must be called with the queue lock held.
    void recordInitPendingLocked(boolean isPending, String transportName) {
        if (DEBUG) Slog.i(TAG, "recordInitPendingLocked: " + isPending
                + " on transport " + transportName);
        try {
            IBackupTransport transport = getTransport(transportName);
            String transportDirName = transport.transportDirName();
            File stateDir = new File(mBaseStateDir, transportDirName);
            File initPendingFile = new File(stateDir, INIT_SENTINEL_FILE_NAME);

            if (isPending) {
                // We need an init before we can proceed with sending backup data.
                // Record that with an entry in our set of pending inits, as well as
                // journaling it via creation of a sentinel file.
                mPendingInits.add(transportName);
                try {
                    (new FileOutputStream(initPendingFile)).close();
                } catch (IOException ioe) {
                    // Something is badly wrong with our permissions; just try to move on
                }
            } else {
                // No more initialization needed; wipe the journal and reset our state.
                initPendingFile.delete();
                mPendingInits.remove(transportName);
            }
        } catch (RemoteException e) {
            // can't happen; the transport is local
        }
    }

    // Reset all of our bookkeeping, in response to having been told that
    // the backend data has been wiped [due to idle expiry, for example],
    // so we must re-upload all saved settings.
    void resetBackupState(File stateFileDir) {
        synchronized (mQueueLock) {
            // Wipe the "what we've ever backed up" tracking
            mEverStoredApps.clear();
            mEverStored.delete();

            mCurrentToken = 0;
            writeRestoreTokens();

            // Remove all the state files
            for (File sf : stateFileDir.listFiles()) {
                // ... but don't touch the needs-init sentinel
                if (!sf.getName().equals(INIT_SENTINEL_FILE_NAME)) {
                    sf.delete();
                }
            }
        }

        // Enqueue a new backup of every participant
        synchronized (mBackupParticipants) {
            int N = mBackupParticipants.size();
            for (int i=0; i<N; i++) {
                int uid = mBackupParticipants.keyAt(i);
                HashSet<ApplicationInfo> participants = mBackupParticipants.valueAt(i);
                for (ApplicationInfo app: participants) {
                    dataChangedImpl(app.packageName);
                }
            }
        }
    }

    // Add a transport to our set of available backends.  If 'transport' is null, this
    // is an unregistration, and the transport's entry is removed from our bookkeeping.
    private void registerTransport(String name, IBackupTransport transport) {
        synchronized (mTransports) {
            if (DEBUG) Slog.v(TAG, "Registering transport " + name + " = " + transport);
            if (transport != null) {
                mTransports.put(name, transport);
            } else {
                mTransports.remove(name);
                if ((mCurrentTransport != null) && mCurrentTransport.equals(name)) {
                    mCurrentTransport = null;
                }
                // Nothing further to do in the unregistration case
                return;
            }
        }

        // If the init sentinel file exists, we need to be sure to perform the init
        // as soon as practical.  We also create the state directory at registration
        // time to ensure it's present from the outset.
        try {
            String transportName = transport.transportDirName();
            File stateDir = new File(mBaseStateDir, transportName);
            stateDir.mkdirs();

            File initSentinel = new File(stateDir, INIT_SENTINEL_FILE_NAME);
            if (initSentinel.exists()) {
                synchronized (mQueueLock) {
                    mPendingInits.add(transportName);

                    // TODO: pick a better starting time than now + 1 minute
                    long delay = 1000 * 60; // one minute, in milliseconds
                    mAlarmManager.set(AlarmManager.RTC_WAKEUP,
                            System.currentTimeMillis() + delay, mRunInitIntent);
                }
            }
        } catch (RemoteException e) {
            // can't happen, the transport is local
        }
    }

    // ----- Track installation/removal of packages -----
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Slog.d(TAG, "Received broadcast " + intent);

            String action = intent.getAction();
            boolean replacing = false;
            boolean added = false;
            Bundle extras = intent.getExtras();
            String pkgList[] = null;
            if (Intent.ACTION_PACKAGE_ADDED.equals(action) ||
                    Intent.ACTION_PACKAGE_REMOVED.equals(action) ||
                    Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
                Uri uri = intent.getData();
                if (uri == null) {
                    return;
                }
                String pkgName = uri.getSchemeSpecificPart();
                if (pkgName != null) {
                    pkgList = new String[] { pkgName };
                }
                if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
                    // use the existing "add with replacement" logic
                    if (MORE_DEBUG) Slog.d(TAG, "PACKAGE_REPLACED, updating package " + pkgName);
                    added = replacing = true;
                } else {
                    added = Intent.ACTION_PACKAGE_ADDED.equals(action);
                    replacing = extras.getBoolean(Intent.EXTRA_REPLACING, false);
                }
            } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
                added = true;
                pkgList = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {
                added = false;
                pkgList = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            }

            if (pkgList == null || pkgList.length == 0) {
                return;
            }
            if (added) {
                synchronized (mBackupParticipants) {
                    if (replacing) {
                        updatePackageParticipantsLocked(pkgList);
                    } else {
                        addPackageParticipantsLocked(pkgList);
                    }
                }
            } else {
                if (replacing) {
                    // The package is being updated.  We'll receive a PACKAGE_ADDED shortly.
                } else {
                    synchronized (mBackupParticipants) {
                        removePackageParticipantsLocked(pkgList);
                    }
                }
            }
        }
    };

    // ----- Track connection to GoogleBackupTransport service -----
    ServiceConnection mGoogleConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (DEBUG) Slog.v(TAG, "Connected to Google transport");
            mGoogleTransport = IBackupTransport.Stub.asInterface(service);
            registerTransport(name.flattenToShortString(), mGoogleTransport);
        }

        public void onServiceDisconnected(ComponentName name) {
            if (DEBUG) Slog.v(TAG, "Disconnected from Google transport");
            mGoogleTransport = null;
            registerTransport(name.flattenToShortString(), null);
        }
    };

    // Add the backup agents in the given packages to our set of known backup participants.
    // If 'packageNames' is null, adds all backup agents in the whole system.
    void addPackageParticipantsLocked(String[] packageNames) {
        // Look for apps that define the android:backupAgent attribute
        List<PackageInfo> targetApps = allAgentPackages();
        if (packageNames != null) {
            if (DEBUG) Slog.v(TAG, "addPackageParticipantsLocked: #" + packageNames.length);
            for (String packageName : packageNames) {
                addPackageParticipantsLockedInner(packageName, targetApps);
            }
        } else {
            if (DEBUG) Slog.v(TAG, "addPackageParticipantsLocked: all");
            addPackageParticipantsLockedInner(null, targetApps);
        }
    }

    private void addPackageParticipantsLockedInner(String packageName,
            List<PackageInfo> targetPkgs) {
        if (MORE_DEBUG) {
            Slog.v(TAG, "Examining " + packageName + " for backup agent");
        }

        for (PackageInfo pkg : targetPkgs) {
            if (packageName == null || pkg.packageName.equals(packageName)) {
                int uid = pkg.applicationInfo.uid;
                HashSet<ApplicationInfo> set = mBackupParticipants.get(uid);
                if (set == null) {
                    set = new HashSet<ApplicationInfo>();
                    mBackupParticipants.put(uid, set);
                }
                set.add(pkg.applicationInfo);
                if (MORE_DEBUG) Slog.v(TAG, "Agent found; added");

                // If we've never seen this app before, schedule a backup for it
                if (!mEverStoredApps.contains(pkg.packageName)) {
                    if (DEBUG) Slog.i(TAG, "New app " + pkg.packageName
                            + " never backed up; scheduling");
                    dataChangedImpl(pkg.packageName);
                }
            }
        }
    }

    // Remove the given packages' entries from our known active set.
    void removePackageParticipantsLocked(String[] packageNames) {
        if (packageNames == null) {
            Slog.w(TAG, "removePackageParticipants with null list");
            return;
        }

        if (DEBUG) Slog.v(TAG, "removePackageParticipantsLocked: #" + packageNames.length);
        List<PackageInfo> knownPackages = allAgentPackages();
        for (String pkg : packageNames) {
            removePackageParticipantsLockedInner(pkg, knownPackages);
        }
    }

    private void removePackageParticipantsLockedInner(String packageName,
            List<PackageInfo> allPackages) {
        if (MORE_DEBUG) {
            Slog.v(TAG, "removePackageParticipantsLockedInner (" + packageName
                    + ") removing from " + allPackages.size() + " entries");
            for (PackageInfo p : allPackages) {
                Slog.v(TAG, "    - " + p.packageName);
            }
        }
        for (PackageInfo pkg : allPackages) {
            if (packageName == null || pkg.packageName.equals(packageName)) {
                /*
                int uid = -1;
                try {
                    PackageInfo info = mPackageManager.getPackageInfo(packageName, 0);
                    uid = info.applicationInfo.uid;
                } catch (NameNotFoundException e) {
                    // we don't know this package name, so just skip it for now
                    continue;
                }
                */
                final int uid = pkg.applicationInfo.uid;
                if (MORE_DEBUG) Slog.i(TAG, "   found pkg " + packageName + " uid=" + uid);

                HashSet<ApplicationInfo> set = mBackupParticipants.get(uid);
                if (set != null) {
                    // Find the existing entry with the same package name, and remove it.
                    // We can't just remove(app) because the instances are different.
                    for (ApplicationInfo entry: set) {
                        if (MORE_DEBUG) Slog.i(TAG, "      checking against " + entry.packageName);
                        if (entry.packageName.equals(pkg)) {
                            if (MORE_DEBUG) Slog.v(TAG, "  removing participant " + pkg);
                            set.remove(entry);
                            removeEverBackedUp(pkg.packageName);
                            break;
                        }
                    }
                    if (set.size() == 0) {
                        mBackupParticipants.delete(uid);
                    }
                } else {
                    if (MORE_DEBUG) Slog.i(TAG, "   ... not found in uid mapping");
                }
            }
        }
    }

    // Returns the set of all applications that define an android:backupAgent attribute
    List<PackageInfo> allAgentPackages() {
        // !!! TODO: cache this and regenerate only when necessary
        int flags = PackageManager.GET_SIGNATURES;
        List<PackageInfo> packages = mPackageManager.getInstalledPackages(flags);
        int N = packages.size();
        for (int a = N-1; a >= 0; a--) {
            PackageInfo pkg = packages.get(a);
            try {
                ApplicationInfo app = pkg.applicationInfo;
                if (((app.flags&ApplicationInfo.FLAG_ALLOW_BACKUP) == 0)
                        || app.backupAgentName == null) {
                    packages.remove(a);
                }
                else {
                    // we will need the shared library path, so look that up and store it here
                    app = mPackageManager.getApplicationInfo(pkg.packageName,
                            PackageManager.GET_SHARED_LIBRARY_FILES);
                    pkg.applicationInfo.sharedLibraryFiles = app.sharedLibraryFiles;
                }
            } catch (NameNotFoundException e) {
                packages.remove(a);
            }
        }
        return packages;
    }

    // Reset the given package's known backup participants.  Unlike add/remove, the update
    // action cannot be passed a null package name.
    void updatePackageParticipantsLocked(String[] packageNames) {
        if (packageNames == null) {
            Slog.e(TAG, "updatePackageParticipants called with null package list");
            return;
        }
        if (DEBUG) Slog.v(TAG, "updatePackageParticipantsLocked: #" + packageNames.length);

        if (packageNames.length > 0) {
            List<PackageInfo> allApps = allAgentPackages();
            for (String packageName : packageNames) {
                removePackageParticipantsLockedInner(packageName, allApps);
                addPackageParticipantsLockedInner(packageName, allApps);
            }
        }
    }

    // Called from the backup task: record that the given app has been successfully
    // backed up at least once
    void logBackupComplete(String packageName) {
        if (packageName.equals(PACKAGE_MANAGER_SENTINEL)) return;

        synchronized (mEverStoredApps) {
            if (!mEverStoredApps.add(packageName)) return;

            RandomAccessFile out = null;
            try {
                out = new RandomAccessFile(mEverStored, "rws");
                out.seek(out.length());
                out.writeUTF(packageName);
            } catch (IOException e) {
                Slog.e(TAG, "Can't log backup of " + packageName + " to " + mEverStored);
            } finally {
                try { if (out != null) out.close(); } catch (IOException e) {}
            }
        }
    }

    // Remove our awareness of having ever backed up the given package
    void removeEverBackedUp(String packageName) {
        if (DEBUG) Slog.v(TAG, "Removing backed-up knowledge of " + packageName);
        if (MORE_DEBUG) Slog.v(TAG, "New set:");

        synchronized (mEverStoredApps) {
            // Rewrite the file and rename to overwrite.  If we reboot in the middle,
            // we'll recognize on initialization time that the package no longer
            // exists and fix it up then.
            File tempKnownFile = new File(mBaseStateDir, "processed.new");
            RandomAccessFile known = null;
            try {
                known = new RandomAccessFile(tempKnownFile, "rws");
                mEverStoredApps.remove(packageName);
                for (String s : mEverStoredApps) {
                    known.writeUTF(s);
                    if (MORE_DEBUG) Slog.v(TAG, "    " + s);
                }
                known.close();
                known = null;
                if (!tempKnownFile.renameTo(mEverStored)) {
                    throw new IOException("Can't rename " + tempKnownFile + " to " + mEverStored);
                }
            } catch (IOException e) {
                // Bad: we couldn't create the new copy.  For safety's sake we
                // abandon the whole process and remove all what's-backed-up
                // state entirely, meaning we'll force a backup pass for every
                // participant on the next boot or [re]install.
                Slog.w(TAG, "Error rewriting " + mEverStored, e);
                mEverSt
