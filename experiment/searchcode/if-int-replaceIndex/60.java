/*
 * Copyright (C) 2010 The Android Open Source Project
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

import com.android.internal.os.storage.ExternalStorageFormatter;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.JournaledFile;
import com.android.internal.util.XmlUtils;
import com.android.internal.widget.LockPatternUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.AppGlobals;
import android.app.PendingIntent;
import android.app.admin.DeviceAdminInfo;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.app.admin.IDevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.PowerManager;
import android.os.Process;
import android.os.RecoverySystem;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import android.view.IWindowManager;
import android.view.WindowManagerPolicy;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Implementation of the device policy APIs.
 */
public class DevicePolicyManagerService extends IDevicePolicyManager.Stub {
    private static final String DEVICE_POLICIES_XML = "device_policies.xml";

    private static final String TAG = "DevicePolicyManagerService";

    private static final int REQUEST_EXPIRE_PASSWORD = 5571;

    private static final long MS_PER_DAY = 86400 * 1000;

    private static final long EXPIRATION_GRACE_PERIOD_MS = 5 * MS_PER_DAY; // 5 days, in ms

    protected static final String ACTION_EXPIRED_PASSWORD_NOTIFICATION
            = "com.android.server.ACTION_EXPIRED_PASSWORD_NOTIFICATION";

    private static final boolean DBG = false;

    final Context mContext;
    final PowerManager.WakeLock mWakeLock;

    IPowerManager mIPowerManager;
    IWindowManager mIWindowManager;

    public static class DevicePolicyData {
        int mActivePasswordQuality = DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED;
        int mActivePasswordLength = 0;
        int mActivePasswordUpperCase = 0;
        int mActivePasswordLowerCase = 0;
        int mActivePasswordLetters = 0;
        int mActivePasswordNumeric = 0;
        int mActivePasswordSymbols = 0;
        int mActivePasswordNonLetter = 0;
        int mFailedPasswordAttempts = 0;

        int mUserHandle;;
        int mPasswordOwner = -1;
        long mLastMaximumTimeToLock = -1;

        final HashMap<ComponentName, ActiveAdmin> mAdminMap
                = new HashMap<ComponentName, ActiveAdmin>();
        final ArrayList<ActiveAdmin> mAdminList
                = new ArrayList<ActiveAdmin>();

        public DevicePolicyData(int userHandle) {
            mUserHandle = userHandle;
        }
    }

    final SparseArray<DevicePolicyData> mUserData = new SparseArray<DevicePolicyData>();

    Handler mHandler = new Handler();

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final int userHandle = intent.getIntExtra(Intent.EXTRA_USER_HANDLE,
                    getSendingUserId());
            if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                    || ACTION_EXPIRED_PASSWORD_NOTIFICATION.equals(action)) {
                Slog.v(TAG, "Sending password expiration notifications for action " + action
                        + " for user " + userHandle);
                mHandler.post(new Runnable() {
                    public void run() {
                        handlePasswordExpirationNotification(getUserData(userHandle));
                    }
                });
            } else if (Intent.ACTION_USER_REMOVED.equals(action)) {
                removeUserData(userHandle);
            } else if (Intent.ACTION_USER_STARTED.equals(action)
                    || Intent.ACTION_PACKAGE_CHANGED.equals(action)
                    || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                    || Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {

                if (Intent.ACTION_USER_STARTED.equals(action)) {
                    // Reset the policy data
                    synchronized (DevicePolicyManagerService.this) {
                        mUserData.remove(userHandle);
                    }
                }

                handlePackagesChanged(userHandle);
            }
        }
    };

    static class ActiveAdmin {
        final DeviceAdminInfo info;

        int passwordQuality = DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED;

        static final int DEF_MINIMUM_PASSWORD_LENGTH = 0;
        int minimumPasswordLength = DEF_MINIMUM_PASSWORD_LENGTH;

        static final int DEF_PASSWORD_HISTORY_LENGTH = 0;
        int passwordHistoryLength = DEF_PASSWORD_HISTORY_LENGTH;

        static final int DEF_MINIMUM_PASSWORD_UPPER_CASE = 0;
        int minimumPasswordUpperCase = DEF_MINIMUM_PASSWORD_UPPER_CASE;

        static final int DEF_MINIMUM_PASSWORD_LOWER_CASE = 0;
        int minimumPasswordLowerCase = DEF_MINIMUM_PASSWORD_LOWER_CASE;

        static final int DEF_MINIMUM_PASSWORD_LETTERS = 1;
        int minimumPasswordLetters = DEF_MINIMUM_PASSWORD_LETTERS;

        static final int DEF_MINIMUM_PASSWORD_NUMERIC = 1;
        int minimumPasswordNumeric = DEF_MINIMUM_PASSWORD_NUMERIC;

        static final int DEF_MINIMUM_PASSWORD_SYMBOLS = 1;
        int minimumPasswordSymbols = DEF_MINIMUM_PASSWORD_SYMBOLS;

        static final int DEF_MINIMUM_PASSWORD_NON_LETTER = 0;
        int minimumPasswordNonLetter = DEF_MINIMUM_PASSWORD_NON_LETTER;

        static final long DEF_MAXIMUM_TIME_TO_UNLOCK = 0;
        long maximumTimeToUnlock = DEF_MAXIMUM_TIME_TO_UNLOCK;

        static final int DEF_MAXIMUM_FAILED_PASSWORDS_FOR_WIPE = 0;
        int maximumFailedPasswordsForWipe = DEF_MAXIMUM_FAILED_PASSWORDS_FOR_WIPE;

        static final long DEF_PASSWORD_EXPIRATION_TIMEOUT = 0;
        long passwordExpirationTimeout = DEF_PASSWORD_EXPIRATION_TIMEOUT;

        static final long DEF_PASSWORD_EXPIRATION_DATE = 0;
        long passwordExpirationDate = DEF_PASSWORD_EXPIRATION_DATE;

        static final int DEF_KEYGUARD_FEATURES_DISABLED = 0; // none
        int disabledKeyguardFeatures = DEF_KEYGUARD_FEATURES_DISABLED;

        boolean encryptionRequested = false;
        boolean disableCamera = false;

        // TODO: review implementation decisions with frameworks team
        boolean specifiesGlobalProxy = false;
        String globalProxySpec = null;
        String globalProxyExclusionList = null;

        ActiveAdmin(DeviceAdminInfo _info) {
            info = _info;
        }

        int getUid() { return info.getActivityInfo().applicationInfo.uid; }

        public UserHandle getUserHandle() {
            return new UserHandle(UserHandle.getUserId(info.getActivityInfo().applicationInfo.uid));
        }

        void writeToXml(XmlSerializer out)
                throws IllegalArgumentException, IllegalStateException, IOException {
            out.startTag(null, "policies");
            info.writePoliciesToXml(out);
            out.endTag(null, "policies");
            if (passwordQuality != DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED) {
                out.startTag(null, "password-quality");
                out.attribute(null, "value", Integer.toString(passwordQuality));
                out.endTag(null, "password-quality");
                if (minimumPasswordLength != DEF_MINIMUM_PASSWORD_LENGTH) {
                    out.startTag(null, "min-password-length");
                    out.attribute(null, "value", Integer.toString(minimumPasswordLength));
                    out.endTag(null, "min-password-length");
                }
                if(passwordHistoryLength != DEF_PASSWORD_HISTORY_LENGTH) {
                    out.startTag(null, "password-history-length");
                    out.attribute(null, "value", Integer.toString(passwordHistoryLength));
                    out.endTag(null, "password-history-length");
                }
                if (minimumPasswordUpperCase != DEF_MINIMUM_PASSWORD_UPPER_CASE) {
                    out.startTag(null, "min-password-uppercase");
                    out.attribute(null, "value", Integer.toString(minimumPasswordUpperCase));
                    out.endTag(null, "min-password-uppercase");
                }
                if (minimumPasswordLowerCase != DEF_MINIMUM_PASSWORD_LOWER_CASE) {
                    out.startTag(null, "min-password-lowercase");
                    out.attribute(null, "value", Integer.toString(minimumPasswordLowerCase));
                    out.endTag(null, "min-password-lowercase");
                }
                if (minimumPasswordLetters != DEF_MINIMUM_PASSWORD_LETTERS) {
                    out.startTag(null, "min-password-letters");
                    out.attribute(null, "value", Integer.toString(minimumPasswordLetters));
                    out.endTag(null, "min-password-letters");
                }
                if (minimumPasswordNumeric != DEF_MINIMUM_PASSWORD_NUMERIC) {
                    out.startTag(null, "min-password-numeric");
                    out.attribute(null, "value", Integer.toString(minimumPasswordNumeric));
                    out.endTag(null, "min-password-numeric");
                }
                if (minimumPasswordSymbols != DEF_MINIMUM_PASSWORD_SYMBOLS) {
                    out.startTag(null, "min-password-symbols");
                    out.attribute(null, "value", Integer.toString(minimumPasswordSymbols));
                    out.endTag(null, "min-password-symbols");
                }
                if (minimumPasswordNonLetter > DEF_MINIMUM_PASSWORD_NON_LETTER) {
                    out.startTag(null, "min-password-nonletter");
                    out.attribute(null, "value", Integer.toString(minimumPasswordNonLetter));
                    out.endTag(null, "min-password-nonletter");
                }
            }
            if (maximumTimeToUnlock != DEF_MAXIMUM_TIME_TO_UNLOCK) {
                out.startTag(null, "max-time-to-unlock");
                out.attribute(null, "value", Long.toString(maximumTimeToUnlock));
                out.endTag(null, "max-time-to-unlock");
            }
            if (maximumFailedPasswordsForWipe != DEF_MAXIMUM_FAILED_PASSWORDS_FOR_WIPE) {
                out.startTag(null, "max-failed-password-wipe");
                out.attribute(null, "value", Integer.toString(maximumFailedPasswordsForWipe));
                out.endTag(null, "max-failed-password-wipe");
            }
            if (specifiesGlobalProxy) {
                out.startTag(null, "specifies-global-proxy");
                out.attribute(null, "value", Boolean.toString(specifiesGlobalProxy));
                out.endTag(null, "specifies_global_proxy");
                if (globalProxySpec != null) {
                    out.startTag(null, "global-proxy-spec");
                    out.attribute(null, "value", globalProxySpec);
                    out.endTag(null, "global-proxy-spec");
                }
                if (globalProxyExclusionList != null) {
                    out.startTag(null, "global-proxy-exclusion-list");
                    out.attribute(null, "value", globalProxyExclusionList);
                    out.endTag(null, "global-proxy-exclusion-list");
                }
            }
            if (passwordExpirationTimeout != DEF_PASSWORD_EXPIRATION_TIMEOUT) {
                out.startTag(null, "password-expiration-timeout");
                out.attribute(null, "value", Long.toString(passwordExpirationTimeout));
                out.endTag(null, "password-expiration-timeout");
            }
            if (passwordExpirationDate != DEF_PASSWORD_EXPIRATION_DATE) {
                out.startTag(null, "password-expiration-date");
                out.attribute(null, "value", Long.toString(passwordExpirationDate));
                out.endTag(null, "password-expiration-date");
            }
            if (encryptionRequested) {
                out.startTag(null, "encryption-requested");
                out.attribute(null, "value", Boolean.toString(encryptionRequested));
                out.endTag(null, "encryption-requested");
            }
            if (disableCamera) {
                out.startTag(null, "disable-camera");
                out.attribute(null, "value", Boolean.toString(disableCamera));
                out.endTag(null, "disable-camera");
            }
            if (disabledKeyguardFeatures != DEF_KEYGUARD_FEATURES_DISABLED) {
                out.startTag(null, "disable-keyguard-features");
                out.attribute(null, "value", Integer.toString(disabledKeyguardFeatures));
                out.endTag(null, "disable-keyguard-features");
            }
        }

        void readFromXml(XmlPullParser parser)
                throws XmlPullParserException, IOException {
            int outerDepth = parser.getDepth();
            int type;
            while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
                   && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }
                String tag = parser.getName();
                if ("policies".equals(tag)) {
                    info.readPoliciesFromXml(parser);
                } else if ("password-quality".equals(tag)) {
                    passwordQuality = Integer.parseInt(
                            parser.getAttributeValue(null, "value"));
                } else if ("min-password-length".equals(tag)) {
                    minimumPasswordLength = Integer.parseInt(
                            parser.getAttributeValue(null, "value"));
                } else if ("password-history-length".equals(tag)) {
                    passwordHistoryLength = Integer.parseInt(
                            parser.getAttributeValue(null, "value"));
                } else if ("min-password-uppercase".equals(tag)) {
                    minimumPasswordUpperCase = Integer.parseInt(
                            parser.getAttributeValue(null, "value"));
                } else if ("min-password-lowercase".equals(tag)) {
                    minimumPasswordLowerCase = Integer.parseInt(
                            parser.getAttributeValue(null, "value"));
                } else if ("min-password-letters".equals(tag)) {
                    minimumPasswordLetters = Integer.parseInt(
                            parser.getAttributeValue(null, "value"));
                } else if ("min-password-numeric".equals(tag)) {
                    minimumPasswordNumeric = Integer.parseInt(
                            parser.getAttributeValue(null, "value"));
                } else if ("min-password-symbols".equals(tag)) {
                    minimumPasswordSymbols = Integer.parseInt(
                            parser.getAttributeValue(null, "value"));
                } else if ("min-password-nonletter".equals(tag)) {
                    minimumPasswordNonLetter = Integer.parseInt(
                            parser.getAttributeValue(null, "value"));
                } else if ("max-time-to-unlock".equals(tag)) {
                    maximumTimeToUnlock = Long.parseLong(
                            parser.getAttributeValue(null, "value"));
                } else if ("max-failed-password-wipe".equals(tag)) {
                    maximumFailedPasswordsForWipe = Integer.parseInt(
                            parser.getAttributeValue(null, "value"));
                } else if ("specifies-global-proxy".equals(tag)) {
                    specifiesGlobalProxy = Boolean.parseBoolean(
                            parser.getAttributeValue(null, "value"));
                } else if ("global-proxy-spec".equals(tag)) {
                    globalProxySpec =
                        parser.getAttributeValue(null, "value");
                } else if ("global-proxy-exclusion-list".equals(tag)) {
                    globalProxyExclusionList =
                        parser.getAttributeValue(null, "value");
                } else if ("password-expiration-timeout".equals(tag)) {
                    passwordExpirationTimeout = Long.parseLong(
                            parser.getAttributeValue(null, "value"));
                } else if ("password-expiration-date".equals(tag)) {
                    passwordExpirationDate = Long.parseLong(
                            parser.getAttributeValue(null, "value"));
                } else if ("encryption-requested".equals(tag)) {
                    encryptionRequested = Boolean.parseBoolean(
                            parser.getAttributeValue(null, "value"));
                } else if ("disable-camera".equals(tag)) {
                    disableCamera = Boolean.parseBoolean(
                            parser.getAttributeValue(null, "value"));
                } else if ("disable-keyguard-features".equals(tag)) {
                    disabledKeyguardFeatures = Integer.parseInt(
                            parser.getAttributeValue(null, "value"));
                } else {
                    Slog.w(TAG, "Unknown admin tag: " + tag);
                }
                XmlUtils.skipCurrentTag(parser);
            }
        }

        void dump(String prefix, PrintWriter pw) {
            pw.print(prefix); pw.print("uid="); pw.println(getUid());
            pw.print(prefix); pw.println("policies:");
            ArrayList<DeviceAdminInfo.PolicyInfo> pols = info.getUsedPolicies();
            if (pols != null) {
                for (int i=0; i<pols.size(); i++) {
                    pw.print(prefix); pw.print("  "); pw.println(pols.get(i).tag);
                }
            }
            pw.print(prefix); pw.print("passwordQuality=0x");
                    pw.println(Integer.toHexString(passwordQuality));
            pw.print(prefix); pw.print("minimumPasswordLength=");
                    pw.println(minimumPasswordLength);
            pw.print(prefix); pw.print("passwordHistoryLength=");
                    pw.println(passwordHistoryLength);
            pw.print(prefix); pw.print("minimumPasswordUpperCase=");
                    pw.println(minimumPasswordUpperCase);
            pw.print(prefix); pw.print("minimumPasswordLowerCase=");
                    pw.println(minimumPasswordLowerCase);
            pw.print(prefix); pw.print("minimumPasswordLetters=");
                    pw.println(minimumPasswordLetters);
            pw.print(prefix); pw.print("minimumPasswordNumeric=");
                    pw.println(minimumPasswordNumeric);
            pw.print(prefix); pw.print("minimumPasswordSymbols=");
                    pw.println(minimumPasswordSymbols);
            pw.print(prefix); pw.print("minimumPasswordNonLetter=");
                    pw.println(minimumPasswordNonLetter);
            pw.print(prefix); pw.print("maximumTimeToUnlock=");
                    pw.println(maximumTimeToUnlock);
            pw.print(prefix); pw.print("maximumFailedPasswordsForWipe=");
                    pw.println(maximumFailedPasswordsForWipe);
            pw.print(prefix); pw.print("specifiesGlobalProxy=");
                    pw.println(specifiesGlobalProxy);
            pw.print(prefix); pw.print("passwordExpirationTimeout=");
                    pw.println(passwordExpirationTimeout);
            pw.print(prefix); pw.print("passwordExpirationDate=");
                    pw.println(passwordExpirationDate);
            if (globalProxySpec != null) {
                pw.print(prefix); pw.print("globalProxySpec=");
                        pw.println(globalProxySpec);
            }
            if (globalProxyExclusionList != null) {
                pw.print(prefix); pw.print("globalProxyEclusionList=");
                        pw.println(globalProxyExclusionList);
            }
            pw.print(prefix); pw.print("encryptionRequested=");
                    pw.println(encryptionRequested);
            pw.print(prefix); pw.print("disableCamera=");
                    pw.println(disableCamera);
            pw.print(prefix); pw.print("disabledKeyguardFeatures=");
                    pw.println(disabledKeyguardFeatures);
        }
    }

    private void handlePackagesChanged(int userHandle) {
        boolean removed = false;
        Slog.d(TAG, "Handling package changes for user " + userHandle);
        DevicePolicyData policy = getUserData(userHandle);
        IPackageManager pm = AppGlobals.getPackageManager();
        for (int i = policy.mAdminList.size() - 1; i >= 0; i--) {
            ActiveAdmin aa = policy.mAdminList.get(i);
            try {
                if (pm.getPackageInfo(aa.info.getPackageName(), 0, userHandle) == null
                        || pm.getReceiverInfo(aa.info.getComponent(), 0, userHandle) == null) {
                    removed = true;
                    policy.mAdminList.remove(i);
                }
            } catch (RemoteException re) {
                // Shouldn't happen
            }
        }
        if (removed) {
            validatePasswordOwnerLocked(policy);
            syncDeviceCapabilitiesLocked(policy);
            saveSettingsLocked(policy.mUserHandle);
        }
    }

    /**
     * Instantiates the service.
     */
    public DevicePolicyManagerService(Context context) {
        mContext = context;
        mWakeLock = ((PowerManager)context.getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DPM");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        filter.addAction(ACTION_EXPIRED_PASSWORD_NOTIFICATION);
        filter.addAction(Intent.ACTION_USER_REMOVED);
        filter.addAction(Intent.ACTION_USER_STARTED);
        context.registerReceiverAsUser(mReceiver, UserHandle.ALL, filter, null, mHandler);
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        filter.addDataScheme("package");
        context.registerReceiverAsUser(mReceiver, UserHandle.ALL, filter, null, mHandler);
    }

    /**
     * Creates and loads the policy data from xml.
     * @param userHandle the user for whom to load the policy data
     * @return
     */
    DevicePolicyData getUserData(int userHandle) {
        synchronized (this) {
            DevicePolicyData policy = mUserData.get(userHandle);
            if (policy == null) {
                policy = new DevicePolicyData(userHandle);
                mUserData.append(userHandle, policy);
                loadSettingsLocked(policy, userHandle);
            }
            return policy;
        }
    }

    void removeUserData(int userHandle) {
        synchronized (this) {
            if (userHandle == UserHandle.USER_OWNER) {
                Slog.w(TAG, "Tried to remove device policy file for user 0! Ignoring.");
                return;
            }
            DevicePolicyData policy = mUserData.get(userHandle);
            if (policy != null) {
                mUserData.remove(userHandle);
            }
            File policyFile = new File(Environment.getUserSystemDirectory(userHandle),
                    DEVICE_POLICIES_XML);
            policyFile.delete();
            Slog.i(TAG, "Removed device policy file " + policyFile.getAbsolutePath());
        }
    }

    /**
     * Set an alarm for an upcoming event - expiration warning, expiration, or post-expiration
     * reminders.  Clears alarm if no expirations are configured.
     */
    protected void setExpirationAlarmCheckLocked(Context context, DevicePolicyData policy) {
        final long expiration = getPasswordExpirationLocked(null, policy.mUserHandle);
        final long now = System.currentTimeMillis();
        final long timeToExpire = expiration - now;
        final long alarmTime;
        if (expiration == 0) {
            // No expirations are currently configured:  Cancel alarm.
            alarmTime = 0;
        } else if (timeToExpire <= 0) {
            // The password has already expired:  Repeat every 24 hours.
            alarmTime = now + MS_PER_DAY;
        } else {
            // Selecting the next alarm time:  Roll forward to the next 24 hour multiple before
            // the expiration time.
            long alarmInterval = timeToExpire % MS_PER_DAY;
            if (alarmInterval == 0) {
                alarmInterval = MS_PER_DAY;
            }
            alarmTime = now + alarmInterval;
        }

        long token = Binder.clearCallingIdentity();
        try {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pi = PendingIntent.getBroadcastAsUser(context, REQUEST_EXPIRE_PASSWORD,
                    new Intent(ACTION_EXPIRED_PASSWORD_NOTIFICATION),
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT,
                    new UserHandle(policy.mUserHandle));
            am.cancel(pi);
            if (alarmTime != 0) {
                am.set(AlarmManager.RTC, alarmTime, pi);
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private IPowerManager getIPowerManager() {
        if (mIPowerManager == null) {
            IBinder b = ServiceManager.getService(Context.POWER_SERVICE);
            mIPowerManager = IPowerManager.Stub.asInterface(b);
        }
        return mIPowerManager;
    }

    private IWindowManager getWindowManager() {
        if (mIWindowManager == null) {
            IBinder b = ServiceManager.getService(Context.WINDOW_SERVICE);
            mIWindowManager = IWindowManager.Stub.asInterface(b);
        }
        return mIWindowManager;
    }

    ActiveAdmin getActiveAdminUncheckedLocked(ComponentName who, int userHandle) {
        ActiveAdmin admin = getUserData(userHandle).mAdminMap.get(who);
        if (admin != null
                && who.getPackageName().equals(admin.info.getActivityInfo().packageName)
                && who.getClassName().equals(admin.info.getActivityInfo().name)) {
            return admin;
        }
        return null;
    }

    ActiveAdmin getActiveAdminForCallerLocked(ComponentName who, int reqPolicy)
            throws SecurityException {
        final int callingUid = Binder.getCallingUid();
        final int userHandle = UserHandle.getUserId(callingUid);
        final DevicePolicyData policy = getUserData(userHandle);
        if (who != null) {
            ActiveAdmin admin = policy.mAdminMap.get(who);
            if (admin == null) {
                throw new SecurityException("No active admin " + who);
            }
            if (admin.getUid() != callingUid) {
                throw new SecurityException("Admin " + who + " is not owned by uid "
                        + Binder.getCallingUid());
            }
            if (!admin.info.usesPolicy(reqPolicy)) {
                throw new SecurityException("Admin " + admin.info.getComponent()
                        + " did not specify uses-policy for: "
                        + admin.info.getTagForPolicy(reqPolicy));
            }
            return admin;
        } else {
            final int N = policy.mAdminList.size();
            for (int i=0; i<N; i++) {
                ActiveAdmin admin = policy.mAdminList.get(i);
                if (admin.getUid() == callingUid && admin.info.usesPolicy(reqPolicy)) {
                    return admin;
                }
            }
            throw new SecurityException("No active admin owned by uid "
                    + Binder.getCallingUid() + " for policy #" + reqPolicy);
        }
    }

    void sendAdminCommandLocked(ActiveAdmin admin, String action) {
        sendAdminCommandLocked(admin, action, null);
    }

    void sendAdminCommandLocked(ActiveAdmin admin, String action, BroadcastReceiver result) {
        Intent intent = new Intent(action);
        intent.setComponent(admin.info.getComponent());
        if (action.equals(DeviceAdminReceiver.ACTION_PASSWORD_EXPIRING)) {
            intent.putExtra("expiration", admin.passwordExpirationDate);
        }
        if (result != null) {
            mContext.sendOrderedBroadcastAsUser(intent, admin.getUserHandle(),
                    null, result, mHandler, Activity.RESULT_OK, null, null);
        } else {
            mContext.sendBroadcastAsUser(intent, UserHandle.OWNER);
        }
    }

    void sendAdminCommandLocked(String action, int reqPolicy, int userHandle) {
        final DevicePolicyData policy = getUserData(userHandle);
        final int count = policy.mAdminList.size();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                ActiveAdmin admin = policy.mAdminList.get(i);
                if (admin.info.usesPolicy(reqPolicy)) {
                    sendAdminCommandLocked(admin, action);
                }
            }
        }
    }

    void removeActiveAdminLocked(final ComponentName adminReceiver, int userHandle) {
        final ActiveAdmin admin = getActiveAdminUncheckedLocked(adminReceiver, userHandle);
        if (admin != null) {
            sendAdminCommandLocked(admin,
                    DeviceAdminReceiver.ACTION_DEVICE_ADMIN_DISABLED,
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            synchronized (DevicePolicyManagerService.this) {
                                int userHandle = admin.getUserHandle().getIdentifier();
                                DevicePolicyData policy = getUserData(userHandle);
                                boolean doProxyCleanup = admin.info.usesPolicy(
                                        DeviceAdminInfo.USES_POLICY_SETS_GLOBAL_PROXY);
                                policy.mAdminList.remove(admin);
                                policy.mAdminMap.remove(adminReceiver);
                                validatePasswordOwnerLocked(policy);
                                syncDeviceCapabilitiesLocked(policy);
                                if (doProxyCleanup) {
                                    resetGlobalProxyLocked(getUserData(userHandle));
                                }
                                saveSettingsLocked(userHandle);
                                updateMaximumTimeToLockLocked(policy);
                            }
                        }
            });
        }
    }

    public DeviceAdminInfo findAdmin(ComponentName adminName, int userHandle) {
        enforceCrossUserPermission(userHandle);
        Intent resolveIntent = new Intent();
        resolveIntent.setComponent(adminName);
        List<ResolveInfo> infos = mContext.getPackageManager().queryBroadcastReceivers(
                resolveIntent, PackageManager.GET_META_DATA, userHandle);
        if (infos == null || infos.size() <= 0) {
            throw new IllegalArgumentException("Unknown admin: " + adminName);
        }

        try {
            return new DeviceAdminInfo(mContext, infos.get(0));
        } catch (XmlPullParserException e) {
            Slog.w(TAG, "Bad device admin requested for user=" + userHandle + ": " + adminName, e);
            return null;
        } catch (IOException e) {
            Slog.w(TAG, "Bad device admin requested for user=" + userHandle + ": " + adminName, e);
            return null;
        }
    }

    private static JournaledFile makeJournaledFile(int userHandle) {
        final String base = userHandle == 0
                ? "/data/system/" + DEVICE_POLICIES_XML
                : new File(Environment.getUserSystemDirectory(userHandle), DEVICE_POLICIES_XML)
                        .getAbsolutePath();
        return new JournaledFile(new File(base), new File(base + ".tmp"));
    }

    private void saveSettingsLocked(int userHandle) {
        DevicePolicyData policy = getUserData(userHandle);
        JournaledFile journal = makeJournaledFile(userHandle);
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(journal.chooseForWrite(), false);
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(stream, "utf-8");
            out.startDocument(null, true);

            out.startTag(null, "policies");

            final int N = policy.mAdminList.size();
            for (int i=0; i<N; i++) {
                ActiveAdmin ap = policy.mAdminList.get(i);
                if (ap != null) {
                    out.startTag(null, "admin");
                    out.attribute(null, "name", ap.info.getComponent().flattenToString());
                    ap.writeToXml(out);
                    out.endTag(null, "admin");
                }
            }

            if (policy.mPasswordOwner >= 0) {
                out.startTag(null, "password-owner");
                out.attribute(null, "value", Integer.toString(policy.mPasswordOwner));
                out.endTag(null, "password-owner");
            }

            if (policy.mFailedPasswordAttempts != 0) {
                out.startTag(null, "failed-password-attempts");
                out.attribute(null, "value", Integer.toString(policy.mFailedPasswordAttempts));
                out.endTag(null, "failed-password-attempts");
            }

            if (policy.mActivePasswordQuality != 0 || policy.mActivePasswordLength != 0
                    || policy.mActivePasswordUpperCase != 0 || policy.mActivePasswordLowerCase != 0
                    || policy.mActivePasswordLetters != 0 || policy.mActivePasswordNumeric != 0
                    || policy.mActivePasswordSymbols != 0 || policy.mActivePasswordNonLetter != 0) {
                out.startTag(null, "active-password");
                out.attribute(null, "quality", Integer.toString(policy.mActivePasswordQuality));
                out.attribute(null, "length", Integer.toString(policy.mActivePasswordLength));
                out.attribute(null, "uppercase", Integer.toString(policy.mActivePasswordUpperCase));
                out.attribute(null, "lowercase", Integer.toString(policy.mActivePasswordLowerCase));
                out.attribute(null, "letters", Integer.toString(policy.mActivePasswordLetters));
                out.attribute(null, "numeric", Integer
                        .toString(policy.mActivePasswordNumeric));
                out.attribute(null, "symbols", Integer.toString(policy.mActivePasswordSymbols));
                out.attribute(null, "nonletter", Integer.toString(policy.mActivePasswordNonLetter));
                out.endTag(null, "active-password");
            }

            out.endTag(null, "policies");

            out.endDocument();
            stream.close();
            journal.commit();
            sendChangedNotification(userHandle);
        } catch (IOException e) {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException ex) {
                // Ignore
            }
            journal.rollback();
        }
    }

    private void sendChangedNotification(int userHandle) {
        Intent intent = new Intent(DevicePolicyManager.ACTION_DEVICE_POLICY_MANAGER_STATE_CHANGED);
        intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        long ident = Binder.clearCallingIdentity();
        try {
            mContext.sendBroadcastAsUser(intent, new UserHandle(userHandle));
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void loadSettingsLocked(DevicePolicyData policy, int userHandle) {
        JournaledFile journal = makeJournaledFile(userHandle);
        FileInputStream stream = null;
        File file = journal.chooseForRead();
        try {
            stream = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);

            int type;
            while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
                    && type != XmlPullParser.START_TAG) {
            }
            String tag = parser.getName();
            if (!"policies".equals(tag)) {
                throw new XmlPullParserException(
                        "Settings do not start with policies tag: found " + tag);
            }
            type = parser.next();
            int outerDepth = parser.getDepth();
            while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
                   && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }
                tag = parser.getName();
                if ("admin".equals(tag)) {
                    String name = parser.getAttributeValue(null, "name");
                    try {
                        DeviceAdminInfo dai = findAdmin(
                                ComponentName.unflattenFromString(name), userHandle);
                        if (DBG && (UserHandle.getUserId(dai.getActivityInfo().applicationInfo.uid)
                                != userHandle)) {
                            Slog.w(TAG, "findAdmin returned an incorrect uid "
                                    + dai.getActivityInfo().applicationInfo.uid + " for user "
                                    + userHandle);
                        }
                        if (dai != null) {
                            ActiveAdmin ap = new ActiveAdmin(dai);
                            ap.readFromXml(parser);
                            policy.mAdminMap.put(ap.info.getComponent(), ap);
                            policy.mAdminList.add(ap);
                        }
                    } catch (RuntimeException e) {
                        Slog.w(TAG, "Failed loading admin " + name, e);
                    }
                } else if ("failed-password-attempts".equals(tag)) {
                    policy.mFailedPasswordAttempts = Integer.parseInt(
                            parser.getAttributeValue(null, "value"));
                    XmlUtils.skipCurrentTag(parser);
                } else if ("password-owner".equals(tag)) {
                    policy.mPasswordOwner = Integer.parseInt(
                            parser.getAttributeValue(null, "value"));
                    XmlUtils.skipCurrentTag(parser);
                } else if ("active-password".equals(tag)) {
                    policy.mActivePasswordQuality = Integer.parseInt(
                            parser.getAttributeValue(null, "quality"));
                    policy.mActivePasswordLength = Integer.parseInt(
                            parser.getAttributeValue(null, "length"));
                    policy.mActivePasswordUpperCase = Integer.parseInt(
                            parser.getAttributeValue(null, "uppercase"));
                    policy.mActivePasswordLowerCase = Integer.parseInt(
                            parser.getAttributeValue(null, "lowercase"));
                    policy.mActivePasswordLetters = Integer.parseInt(
                            parser.getAttributeValue(null, "letters"));
                    policy.mActivePasswordNumeric = Integer.parseInt(
                            parser.getAttributeValue(null, "numeric"));
                    policy.mActivePasswordSymbols = Integer.parseInt(
                            parser.getAttributeValue(null, "symbols"));
                    policy.mActivePasswordNonLetter = Integer.parseInt(
                            parser.getAttributeValue(null, "nonletter"));
                    XmlUtils.skipCurrentTag(parser);
                } else {
                    Slog.w(TAG, "Unknown tag: " + tag);
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        } catch (NullPointerException e) {
            Slog.w(TAG, "failed parsing " + file + " " + e);
        } catch (NumberFormatException e) {
            Slog.w(TAG, "failed parsing " + file + " " + e);
        } catch (XmlPullParserException e) {
            Slog.w(TAG, "failed parsing " + file + " " + e);
        } catch (FileNotFoundException e) {
            // Don't be noisy, this is normal if we haven't defined any policies.
        } catch (IOException e) {
            Slog.w(TAG, "failed parsing " + file + " " + e);
        } catch (IndexOutOfBoundsException e) {
            Slog.w(TAG, "failed parsing " + file + " " + e);
        }
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e) {
            // Ignore
        }

        // Validate that what we stored for the password quality matches
        // sufficiently what is currently set.  Note that this is only
        // a sanity check in case the two get out of sync; this should
        // never normally happen.
        LockPatternUtils utils = new LockPatternUtils(mContext);
        if (utils.getActivePasswordQuality() < policy.mActivePasswordQuality) {
            Slog.w(TAG, "Active password quality 0x"
                    + Integer.toHexString(policy.mActivePasswordQuality)
                    + " does not match actual quality 0x"
                    + Integer.toHexString(utils.getActivePasswordQuality()));
            policy.mActivePasswordQuality = DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED;
            policy.mActivePasswordLength = 0;
            policy.mActivePasswordUpperCase = 0;
            policy.mActivePasswordLowerCase = 0;
            policy.mActivePasswordLetters = 0;
            policy.mActivePasswordNumeric = 0;
            policy.mActivePasswordSymbols = 0;
            policy.mActivePasswordNonLetter = 0;
        }

        validatePasswordOwnerLocked(policy);
        syncDeviceCapabilitiesLocked(policy);
        updateMaximumTimeToLockLocked(policy);
    }

    static void validateQualityConstant(int quality) {
        switch (quality) {
            case DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED:
            case DevicePolicyManager.PASSWORD_QUALITY_BIOMETRIC_WEAK:
            case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING:
            case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC:
            case DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC:
            case DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC:
            case DevicePolicyManager.PASSWORD_QUALITY_COMPLEX:
                return;
        }
        throw new IllegalArgumentException("Invalid quality constant: 0x"
                + Integer.toHexString(quality));
    }

    void validatePasswordOwnerLocked(DevicePolicyData policy) {
        if (policy.mPasswordOwner >= 0) {
            boolean haveOwner = false;
            for (int i = policy.mAdminList.size() - 1; i >= 0; i--) {
                if (policy.mAdminList.get(i).getUid() == policy.mPasswordOwner) {
                    haveOwner = true;
                    break;
                }
            }
            if (!haveOwner) {
                Slog.w(TAG, "Previous password owner " + policy.mPasswordOwner
                        + " no longer active; disabling");
                policy.mPasswordOwner = -1;
            }
        }
    }

    /**
     * Pushes down policy information to the system for any policies related to general device
     * capabilities that need to be enforced by lower level services (e.g. Camera services).
     */
    void syncDeviceCapabilitiesLocked(DevicePolicyData policy) {
        // Ensure the status of the camera is synced down to the system. Interested native services
        // should monitor this value and act accordingly.
        boolean systemState = SystemProperties.getBoolean(SYSTEM_PROP_DISABLE_CAMERA, false);
        boolean cameraDisabled = getCameraDisabled(null, policy.mUserHandle);
        if (cameraDisabled != systemState) {
            long token = Binder.clearCallingIdentity();
            try {
                String value = cameraDisabled ? "1" : "0";
                Slog.v(TAG, "Change in camera state ["
                        + SYSTEM_PROP_DISABLE_CAMERA + "] = " + value);
                SystemProperties.set(SYSTEM_PROP_DISABLE_CAMERA, value);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    public void systemReady() {
        synchronized (this) {
            loadSettingsLocked(getUserData(UserHandle.USER_OWNER), UserHandle.USER_OWNER);
        }
    }

    private void handlePasswordExpirationNotification(DevicePolicyData policy) {
        synchronized (this) {
            final long now = System.currentTimeMillis();
            final int N = policy.mAdminList.size();
            if (N <= 0) {
                return;
            }
            for (int i=0; i < N; i++) {
                ActiveAdmin admin = policy.mAdminList.get(i);
                if (admin.info.usesPolicy(DeviceAdminInfo.USES_POLICY_EXPIRE_PASSWORD)
                        && admin.passwordExpirationTimeout > 0L
                        && admin.passwordExpirationDate > 0L
                        && now >= admin.passwordExpirationDate - EXPIRATION_GRACE_PERIOD_MS) {
                    sendAdminCommandLocked(admin, DeviceAdminReceiver.ACTION_PASSWORD_EXPIRING);
                }
            }
            setExpirationAlarmCheckLocked(mContext, policy);
        }
    }

    /**
     * @param adminReceiver The admin to add
     * @param refreshing true = update an active admin, no error
     */
    public void setActiveAdmin(ComponentName adminReceiver, boolean refreshing, int userHandle) {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.BIND_DEVICE_ADMIN, null);
        enforceCrossUserPermission(userHandle);

        DevicePolicyData policy = getUserData(userHandle);
        DeviceAdminInfo info = findAdmin(adminReceiver, userHandle);
        if (info == null) {
            throw new IllegalArgumentException("Bad admin: " + adminReceiver);
        }
        synchronized (this) {
            long ident = Binder.clearCallingIdentity();
            try {
                if (!refreshing && getActiveAdminUncheckedLocked(adminReceiver, userHandle) != null) {
                    throw new IllegalArgumentException("Admin is already added");
                }
                ActiveAdmin newAdmin = new ActiveAdmin(info);
                policy.mAdminMap.put(adminReceiver, newAdmin);
                int replaceIndex = -1;
                if (refreshing) {
                    final int N = policy.mAdminList.size();
                    for (int i=0; i < N; i++) {
                        ActiveAdmin oldAdmin = policy.mAdminList.get(i);
                        if (oldAdmin.info.getComponent().equals(adminReceiver)) {
                            replaceIndex = i;
                            break;
                        }
                    }
                }
                if (replaceIndex == -1) {
                    policy.mAdminList.add(newAdmin);
                } else {
                    policy.mAdminList.set(replaceIndex, newAdmin);
                }
                saveSettingsLocked(userHandle);
                sendAdminCommandLocked(newAdmin, DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public boolean isAdminActive(ComponentName adminReceiver, int userHandle) {
        enforceCrossUserPermission(userHandle);
        synchronized (this) {
            return getActiveAdminUncheckedLocked(adminReceiver, userHandle) != null;
        }
    }

    public boolean hasGrantedPolicy(ComponentName adminReceiver, int policyId, int userHandle) {
        enforceCrossUserPermission(userHandle);
        synchronized (this) {
            ActiveAdmin administrator = getActiveAdminUncheckedLocked(adminReceiver, userHandle);
            if (administrator == null) {
                throw new SecurityException("No active admin " + adminReceiver);
            }
            return administrator.info.usesPolicy(policyId);
        }
    }

    public List<ComponentName> getActiveAdmins(int userHandle) {
        enforceCrossUserPermission(userHandle);
        synchronized (this) {
            DevicePolicyData policy = getUserData(userHandle);
            final int N = policy.mAdminList.size();
            if (N <= 0) {
                return null;
            }
            ArrayList<ComponentName> res = new ArrayList<ComponentName>(N);
            for (int i=0; i<N; i++) {
                res.add(policy.mAdminList.get(i).info.getComponent());
            }
            return res;
        }
    }

    public boolean packageHasActiveAdmins(String packageName, int userHandle) {
        enforceCrossUserPermission(userHandle);
        synchronized (this) {
            DevicePolicyData policy = getUserData(userHandle);
            final int N = policy.mAdminList.size();
            for (int i=0; i<N; i++) {
                if (policy.mAdminList.get(i).info.getPackageName().equals(packageName)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void removeActiveAdmin(ComponentName adminReceiver, int userHandle) {
        enforceCrossUserPermission(userHandle);
        synchronized (this) {
            ActiveAdmin admin = getActiveAdminUncheckedLocked(adminReceiver, userHandle);
            if (admin == null) {
                return;
            }
            if (admin.getUid() != Binder.getCallingUid()) {
                mContext.enforceCallingOrSelfPermission(
                        android.Manifest.permission.BIND_DEVICE_ADMIN, null);
            }
            long ident = Binder.clearCallingIdentity();
            try {
                removeActiveAdminLocked(adminReceiver, userHandle);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void setPasswordQuality(ComponentName who, int quality, int userHandle) {
        validateQualityConstant(quality);
        enforceCrossUserPermission(userHandle);

        synchronized (this) {
            if (who == null) {
                throw new NullPointerException("ComponentName is null");
            }
            ActiveAdmin ap = getActiveAdminForCallerLocked(who,
                    DeviceAdminInfo.USES_POLICY_LIMIT_PASSWORD);
            if (ap.passwordQuality != quality) {
                ap.passwordQuality = quality;
                saveSettingsLocked(userHandle);
            }
        }
    }

    public int getPasswordQuality(ComponentName who, int userHandle) {
        enforceCrossUserPermission(userHandle);
        synchronized (this) {
            int mode = DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED;
            DevicePolicyData policy = getUserData(userHandle);

            if (who != null) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                return admin != null ? admin.passwordQuality : mode;
            }

            final int N = policy.mAdminList.size();
            for  (int i=0; i<N; i++) {
                ActiveAdmin admin = policy.mAdminList.get(i);
                if (mode < admin.passwordQuality) {
                    mode = admin.passwordQuality;
                }
            }
            return mode;
        }
    }

    public void setPasswordMinimumLength(ComponentName who, int length, int userHandle) {
        enforceCrossUserPermission(userHandle);
        synchronized (this) {
            if (who == null) {
                throw new NullPointerException("ComponentName is null");
            }
            ActiveAdmin ap = getActiveAdminForCallerLocked(who,
                    DeviceAdminInfo.USES_POLICY_LIMIT_PASSWORD);
            if (ap.minimumPasswordLength != length) {
                ap.minimumPasswordLength = length;
                saveSettingsLocked(userHandle);
            }
        }
    }

    public int getPasswordMinimumLength(ComponentName who, int userHandle) {
        enforceCrossUserPermission(userHandle);
        synchronized (this) {
            DevicePolicyData policy = getUserData(userHandle);
            int length = 0;

            if (who != null) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                return admin != null ? admin.minimumPasswordLength : length;
            }

            final int N = policy.mAdminList.size();
            for  (int i=0; i<N; i++) {
                ActiveAdmin admin = policy.mAdminList.get(i);
                if (length < admin.minimumPasswordLength) {
                    length = admin.minimumPasswordLength;
                }
            }
            return length;
        }
    }

    public void setPasswordHistoryLength(ComponentName who, int length, int userHandle) {
        enforceCrossUserPermission(userHandle);
        synchronized (this) {
            if (who == null) {
                throw new NullPointerException("ComponentName is null");
            }
            ActiveAdmin ap = getActiveAdminForCallerLocked(who,
                    DeviceAdminInfo.USES_POLICY_LIMIT_PASSWORD);
            if (ap.passwordHistoryLength != length) {
                ap.passwordHistoryLength = length;
                saveSettingsLocked(userHandle);
            }
        }
    }

    public int getPasswordHistoryLength(ComponentName who, int userHandle) {
        enforceCrossUserPermission(userHandle);
        synchronized (this) {
            DevicePolicyData policy = getUserData(userHandle);
            int length = 0;

            if (who != null) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                return admin != null ? admin.passwordHistoryLength : length;
            }

            final int N = policy.mAdminList.size();
            for (int i = 0; i < N; i++) {
                ActiveAdmin admin = policy.mAdminList.get(i);
                if (length < admin.passwordHistoryLength) {
                    length = admin.passwordHistoryLength;
                }
            }
            return length;
        }
    }

    public void setPasswordExpirationTimeout(ComponentName who, long timeout, int userHandle) {
        enforceCrossUserPermission(userHandle);
        synchronized (this) {
            if (who == null) {
                throw new NullPointerException("ComponentName is null");
            }
            if (timeout < 0) {
                throw new IllegalArgumentException("Timeout must be >= 0 ms");
            }
            ActiveAdmin ap = getActiveAdminForCallerLocked(who,
                    DeviceAdminInfo.USES_POLICY_EXPIRE_PASSWORD);
            // Calling this API automatically bumps the expiration date
            final long expiration = timeout > 0L ? (timeout + System.currentTimeMillis()) : 0L;
            ap.passwordExpirationDate = expiration;
            ap.passwordExpirationTimeout = timeout;
            if (timeout > 0L) {
                Slog.w(TAG, "setPasswordExpiration(): password will expire on "
                        + DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT)
                        .format(new Date(expiration)));
            }
            saveSettingsLocked(userHandle);
            // in case this is the first one
            setExpirationAlarmCheckLocked(mContext, getUserData(userHandle));
        }
    }

    /**
     * Return a single admin's expiration cycle time, or the min of all cycle times.
     * Returns 0 if not configured.
     */
    public long getPasswordExpirationTimeout(ComponentName who, int userHandle) {
        enforceCrossUserPermission(userHandle);
        synchronized (this) {
            if (who != null) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                return admin != null ? admin.passwordExpirationTimeout : 0L;
            }

            long timeout = 0L;
            DevicePolicyData policy = getUserData(userHandle);
            final int N = policy.mAdminList.size();
            for (int i = 0; i < N; i++) {
                ActiveAdmin admin = policy.mAdminList.get(i);
                if (timeout == 0L || (admin.passwordExpirationTimeout != 0L
                        && timeout > admin.passwordExpirationTimeout)) {
                    timeout = admin.passwordExpirationTimeout;
                }
            }
            return timeout;
        }
    }

    /**
     * Return a single admin's expiration date/time, or the min (soonest) for all admins.
     * Returns 0 if not configured.
     */
    private long getPasswordExpirationLocked(ComponentName who, int userHandle) {
        if (who != null) {
            ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
            return admin != null ? admin.passwordExpirationDate : 0L;
        }

        long timeout = 0L;
        DevicePolicyData policy = getUserData(userHandle);
        final int N = policy.mAdminList.size();
        for (int i = 0; i < N; i++) {
            ActiveAdmin admin = policy.mAdminList.get(i);
            if (timeout == 0L || (admin.passwordExpirationDate != 0
                    && timeout > admin.passwordExpirationDate)) {
                timeout = admin.passwordExpirationDate;
            }
        }
        return timeout;
    }

    public long getPasswordExpiration(ComponentName who, int userHandle) {
        enforceCrossUserPermission(userHandle);
        synchronized (this) {
            return getPasswordExpirationLocked(who, userHandle);
        }
    }

    public void setPasswordMinimumUpperCase(ComponentName who, int length, int userHandle) {
        enforceCrossUserPermission(userHandle);
        synchronized (this) {
            if (who == null) {
                throw new NullPointerException("ComponentName is null");
            }
            ActiveAdmin ap = getActiveAdminForCallerLocked(who,
                    DeviceAdminInfo.USES_POLICY_LIMIT_PASSWORD);
            if (ap.minimumPasswordUpperCase != length) {
                ap.minimumPasswordUpperCase = length;
                saveSettingsLocked(userHandle);
            }
        }
    }

    public int getPasswordMinimumUpperCase(ComponentName who, int userHandle) {
        enforceCrossUserPermission(userHandle);
        synchronized (this) {
            int length = 0;

            if (who != null) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                return admin != null ? admin.minimumPasswordUpperCase : length;
            }

            DevicePolicyData policy = getUserData(userHandle);
            final int N = policy.mAdminList.size();
            for (int i=0; i<N; i++) {
                ActiveAdmin admin = policy.mAdminList.get(i);
                if (length < admin.minimumPasswordUpperCase) {
                    length = admin.minimumPasswordUpperCase;
                }
            }
            return length;
        }
    }

    public void setPasswordMinimumLowerCase(ComponentName who, int length, int userHandle) {
        enforceCrossUserPermission(userHandle);
        synchronized (this) {
            if (who == null) {
                throw new NullPointerException("ComponentName is null");
            }
            ActiveAdmin ap = getActiveAdminForCallerLocked(who,
                    DeviceAdminInfo.USES_POLICY_LIMIT_PASSWORD);
            if (ap.minimumPasswordLowerCase != length) {
                ap.minimumPasswordLowerCase = length;
                saveSettingsLocked(userHandle);
            }
        }
    }

    public int getPasswordMinimumLowerCase(ComponentName who, int userHandle) {
        enforceCrossUserPermission(userHandle);
        synchronized (this) {
            int length = 0;

            if (who != null) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                return admin != null ? admin.minimumPasswordLowerCase : length;
            }

            DevicePolicyData policy = getUserData(userHandle);
            final int N = policy.mAdminList.size();
            for (int i=0; i<N; i++) {
                ActiveAdmin admin = policy.mAdminList.get(i);
                if (length < admin.minimumPasswordLowerCase) {
                    length = admin.minimumPasswordLowerCase;
                }
            }
            return length;
        }
    }

    public void setPasswordMinimumLetters(ComponentName who, int length, int userHandle) {
        enforceCrossUserPermission(userHandle);
        synchronized (this) {
            if (who == null) {
                throw new NullPointerException("ComponentName is null");
            }
            ActiveAdmin ap = getActiveAdminForCallerLocked(who,
                    DeviceAdminInfo.USES_POLICY_LIMIT_PASSWORD);
            if (ap.minimumPasswordLetters != length) {
                ap.minimumPasswordLetters = length;
                saveSettingsLocked(userHandle);
            }
        }
    }

    public int getPasswordMinimumLetters(ComponentName who, int userHandle) {
        enforceCrossUserPermission(userHandle);
        synchronized (this) {
            int length = 0;

            if (who != null) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                return admin != null ? admin.minimumPasswordLetters : length;
            }

            DevicePolicyData policy = getUserData(userHandle);
            final int N = policy.mAdminList.size();
            for (int i=0; i<N; i++) {
                ActiveAdmin admin = policy.mAdminList.get(i);
                if (length < admin.minimumPasswordLetters) {
                    length = admin.minimumPasswordLetters;
                }
            }
            return length;
        }
    }

    public void setPasswordMinimumNumeric(ComponentName who, int length, int userHandle) {
        enforceCrossUserPermission(userHandle);
        synchronized (this) {
            if (who == null) {
                throw new NullPointerException("ComponentName is null");
            }
            ActiveAdmin ap = getActiveAdminForCallerLocked(who,
                    DeviceAdminInfo.USES_POLICY_LIMIT_PASSWORD);
            if (ap.minimumPasswordNumeric != length) {
                ap.minimumPasswordNumeric = length;
                saveSettingsLocked(userHandle);
            }
        }
    }

    public int getPasswordMinimumNumeric(ComponentName who, int userHandle) {
        enforceCrossUserPermission(userHandle);
        synchronized (this) {
            int length = 0;

            if (who != null) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                return admin != null ? admin.minimumPasswordNumeric : length;
            }

            DevicePolicyData policy = getUserData(userHandle);
            final int N = policy.mAdminList.size();
            for (int i = 0; i < N; i++) {
                ActiveAdmin admin = policy.mAdminList.get(i);
                if (length < admin.minimumPasswordNumeric) {
                    length = admin.minimumPasswordNumeric;
                }
            }
            return length;
        }
    }

    public void setPasswordMinimumSymbols(ComponentName who, int length, int userHandle) {
        enforceCrossUserPermissio
