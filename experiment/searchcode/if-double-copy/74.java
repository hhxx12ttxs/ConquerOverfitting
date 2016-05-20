package cgeo.geocaching;

import cgeo.geocaching.enumerations.CacheSize;
import cgeo.geocaching.enumerations.CacheType;
import cgeo.geocaching.enumerations.LoadFlags;
import cgeo.geocaching.enumerations.LoadFlags.LoadFlag;
import cgeo.geocaching.enumerations.LoadFlags.RemoveFlag;
import cgeo.geocaching.enumerations.LoadFlags.SaveFlag;
import cgeo.geocaching.enumerations.LogType;
import cgeo.geocaching.enumerations.WaypointType;
import cgeo.geocaching.files.LocalStorage;
import cgeo.geocaching.geopoint.Geopoint;
import cgeo.geocaching.geopoint.Viewport;
import cgeo.geocaching.utils.Log;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

public class cgData {

    public enum StorageLocation {
        HEAP,
        CACHE,
        DATABASE,
    }

    /** The list of fields needed for mapping. */
    private static final String[] CACHE_COLUMNS = new String[] {
            "_id", "updated", "reason", "detailed", "detailedupdate", "visiteddate", "geocode", "cacheid", "guid", "type", "name", "own", "owner", "owner_real", "hidden", "hint", "size",
            "difficulty", "distance", "direction", "terrain", "latlon", "location", "latitude", "longitude", "elevation", "shortdesc",
            "favourite_cnt", "rating", "votes", "myvote", "disabled", "archived", "members", "found", "favourite", "inventorycoins", "inventorytags",
            "inventoryunknown", "onWatchlist", "personal_note", "reliable_latlon", "coordsChanged", "finalDefined"
            // reason is replaced by listId in cgCache
    };
    /** The list of fields needed for mapping. */
    private static final String[] WAYPOINT_COLUMNS = new String[] { "_id", "geocode", "updated", "type", "prefix", "lookup", "name", "latlon", "latitude", "longitude", "note", "own" };

    /** Number of days (as ms) after temporarily saved caches are deleted */
    private final static long DAYS_AFTER_CACHE_IS_DELETED = 3 * 24 * 60 * 60 * 1000;

    /**
     * holds the column indexes of the cache table to avoid lookups
     */
    private static int[] cacheColumnIndex;
    private CacheCache cacheCache = new CacheCache();
    private SQLiteDatabase database = null;
    private static final int dbVersion = 64;
    public static final int customListIdOffset = 10;
    private static final String dbName = "data";
    private static final String dbTableCaches = "cg_caches";
    private static final String dbTableLists = "cg_lists";
    private static final String dbTableAttributes = "cg_attributes";
    private static final String dbTableWaypoints = "cg_waypoints";
    private static final String dbTableSpoilers = "cg_spoilers";
    private static final String dbTableLogs = "cg_logs";
    private static final String dbTableLogCount = "cg_logCount";
    private static final String dbTableLogImages = "cg_logImages";
    private static final String dbTableLogsOffline = "cg_logs_offline";
    private static final String dbTableTrackables = "cg_trackables";
    private static final String dbTableSearchDestionationHistory = "cg_search_destination_history";
    private static final String dbCreateCaches = ""
            + "create table " + dbTableCaches + " ("
            + "_id integer primary key autoincrement, "
            + "updated long not null, "
            + "detailed integer not null default 0, "
            + "detailedupdate long, "
            + "visiteddate long, "
            + "geocode text unique not null, "
            + "reason integer not null default 0, " // cached, favourite...
            + "cacheid text, "
            + "guid text, "
            + "type text, "
            + "name text, "
            + "own integer not null default 0, "
            + "owner text, "
            + "owner_real text, "
            + "hidden long, "
            + "hint text, "
            + "size text, "
            + "difficulty float, "
            + "terrain float, "
            + "latlon text, "
            + "location text, "
            + "direction double, "
            + "distance double, "
            + "latitude double, "
            + "longitude double, "
            + "reliable_latlon integer, "
            + "elevation double, "
            + "personal_note text, "
            + "shortdesc text, "
            + "description text, "
            + "favourite_cnt integer, "
            + "rating float, "
            + "votes integer, "
            + "myvote float, "
            + "disabled integer not null default 0, "
            + "archived integer not null default 0, "
            + "members integer not null default 0, "
            + "found integer not null default 0, "
            + "favourite integer not null default 0, "
            + "inventorycoins integer default 0, "
            + "inventorytags integer default 0, "
            + "inventoryunknown integer default 0, "
            + "onWatchlist integer default 0, "
            + "coordsChanged integer default 0, "
            + "finalDefined integer default 0"
            + "); ";
    private static final String dbCreateLists = ""
            + "create table " + dbTableLists + " ("
            + "_id integer primary key autoincrement, "
            + "title text not null, "
            + "updated long not null, "
            + "latitude double, "
            + "longitude double "
            + "); ";
    private static final String dbCreateAttributes = ""
            + "create table " + dbTableAttributes + " ("
            + "_id integer primary key autoincrement, "
            + "geocode text not null, "
            + "updated long not null, " // date of save
            + "attribute text "
            + "); ";

    private static final String dbCreateWaypoints = ""
            + "create table " + dbTableWaypoints + " ("
            + "_id integer primary key autoincrement, "
            + "geocode text not null, "
            + "updated long not null, " // date of save
            + "type text not null default 'waypoint', "
            + "prefix text, "
            + "lookup text, "
            + "name text, "
            + "latlon text, "
            + "latitude double, "
            + "longitude double, "
            + "note text, "
            + "own integer default 0"
            + "); ";
    private static final String dbCreateSpoilers = ""
            + "create table " + dbTableSpoilers + " ("
            + "_id integer primary key autoincrement, "
            + "geocode text not null, "
            + "updated long not null, " // date of save
            + "url text, "
            + "title text, "
            + "description text "
            + "); ";
    private static final String dbCreateLogs = ""
            + "create table " + dbTableLogs + " ("
            + "_id integer primary key autoincrement, "
            + "geocode text not null, "
            + "updated long not null, " // date of save
            + "type integer not null default 4, "
            + "author text, "
            + "log text, "
            + "date long, "
            + "found integer not null default 0, "
            + "friend integer "
            + "); ";

    private static final String dbCreateLogCount = ""
            + "create table " + dbTableLogCount + " ("
            + "_id integer primary key autoincrement, "
            + "geocode text not null, "
            + "updated long not null, " // date of save
            + "type integer not null default 4, "
            + "count integer not null default 0 "
            + "); ";
    private static final String dbCreateLogImages = ""
            + "create table " + dbTableLogImages + " ("
            + "_id integer primary key autoincrement, "
            + "log_id integer not null, "
            + "title text not null, "
            + "url text not null"
            + "); ";
    private static final String dbCreateLogsOffline = ""
            + "create table " + dbTableLogsOffline + " ("
            + "_id integer primary key autoincrement, "
            + "geocode text not null, "
            + "updated long not null, " // date of save
            + "type integer not null default 4, "
            + "log text, "
            + "date long "
            + "); ";
    private static final String dbCreateTrackables = ""
            + "create table " + dbTableTrackables + " ("
            + "_id integer primary key autoincrement, "
            + "updated long not null, " // date of save
            + "tbcode text not null, "
            + "guid text, "
            + "title text, "
            + "owner text, "
            + "released long, "
            + "goal text, "
            + "description text, "
            + "geocode text "
            + "); ";

    private static final String dbCreateSearchDestinationHistory = ""
            + "create table " + dbTableSearchDestionationHistory + " ("
            + "_id integer primary key autoincrement, "
            + "date long not null, "
            + "latitude double, "
            + "longitude double "
            + "); ";

    private HashMap<String, SQLiteStatement> statements = new HashMap<String, SQLiteStatement>();
    private static boolean newlyCreatedDatabase = false;

    public synchronized void init() {
        if (database != null) {
            return;
        }

        try {
            final DbHelper dbHelper = new DbHelper(new DBContext(cgeoapplication.getInstance()));
            database = dbHelper.getWritableDatabase();
        } catch (Exception e) {
            Log.e("cgData.init: unable to open database for R/W", e);
        }
    }

    public void closeDb() {
        if (database == null) {
            return;
        }

        cacheCache.removeAllFromCache();
        clearPreparedStatements();
        database.close();
        database = null;
    }

    private void clearPreparedStatements() {
        for (SQLiteStatement statement : statements.values()) {
            statement.close();
        }
        statements.clear();
    }

    private static File backupFile() {
        return new File(LocalStorage.getStorage(), "cgeo.sqlite");
    }

    public String backupDatabase() {
        if (!LocalStorage.isExternalStorageAvailable()) {
            Log.w("Database wasn't backed up: no external memory");
            return null;
        }

        final File target = backupFile();
        closeDb();
        final boolean backupDone = LocalStorage.copy(databasePath(), target);
        init();

        if (!backupDone) {
            Log.e("Database could not be copied to " + target);
            return null;
        }

        Log.i("Database was copied to " + target);
        return target.getPath();
    }

    public boolean moveDatabase() {
        if (!LocalStorage.isExternalStorageAvailable()) {
            Log.w("Database was not moved: external memory not available");
            return false;
        }

        closeDb();

        final File source = databasePath();
        final File target = databaseAlternatePath();

        if (!LocalStorage.copy(source, target)) {
            Log.e("Database could not be moved to " + target);
            init();
            return false;
        }

        source.delete();
        Settings.setDbOnSDCard(!Settings.isDbOnSDCard());
        Log.i("Database was moved to " + target);
        init();
        return true;
    }

    private static File databasePath(final boolean internal) {
        return new File(internal ? LocalStorage.getInternalDbDirectory() : LocalStorage.getExternalDbDirectory(), dbName);
    }

    private static File databasePath() {
        return databasePath(!Settings.isDbOnSDCard());
    }

    private static File databaseAlternatePath() {
        return databasePath(Settings.isDbOnSDCard());
    }

    public static File isRestoreFile() {
        final File fileSourceFile = backupFile();
        return fileSourceFile.exists() ? fileSourceFile : null;
    }

    public boolean restoreDatabase() {
        if (!LocalStorage.isExternalStorageAvailable()) {
            Log.w("Database wasn't restored: no external memory");
            return false;
        }

        final File sourceFile = backupFile();
        closeDb();
        final boolean restoreDone = LocalStorage.copy(sourceFile, databasePath());
        init();

        if (restoreDone) {
            Log.i("Database succesfully restored from " + sourceFile.getPath());
        } else {
            Log.e("Could not restore database from " + sourceFile.getPath());
        }

        return restoreDone;
    }

    private static class DBContext extends ContextWrapper {

        public DBContext(Context base) {
            super(base);
        }

        /**
         * We override the default open/create as it doesn't work on OS 1.6 and
         * causes issues on other devices too.
         */
        @Override
        public SQLiteDatabase openOrCreateDatabase(String name, int mode,
                CursorFactory factory) {
            final File file = new File(name);
            file.getParentFile().mkdirs();
            return SQLiteDatabase.openOrCreateDatabase(file, factory);
        }

    }

    private static class DbHelper extends SQLiteOpenHelper {

        DbHelper(Context context) {
            super(context, databasePath().getPath(), null, dbVersion);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            newlyCreatedDatabase = true;
            db.execSQL(dbCreateCaches);
            db.execSQL(dbCreateLists);
            db.execSQL(dbCreateAttributes);
            db.execSQL(dbCreateWaypoints);
            db.execSQL(dbCreateSpoilers);
            db.execSQL(dbCreateLogs);
            db.execSQL(dbCreateLogCount);
            db.execSQL(dbCreateLogImages);
            db.execSQL(dbCreateLogsOffline);
            db.execSQL(dbCreateTrackables);
            db.execSQL(dbCreateSearchDestinationHistory);

            createIndices(db);
        }

        static private void createIndices(final SQLiteDatabase db) {
            db.execSQL("create index if not exists in_caches_geo on " + dbTableCaches + " (geocode)");
            db.execSQL("create index if not exists in_caches_guid on " + dbTableCaches + " (guid)");
            db.execSQL("create index if not exists in_caches_lat on " + dbTableCaches + " (latitude)");
            db.execSQL("create index if not exists in_caches_lon on " + dbTableCaches + " (longitude)");
            db.execSQL("create index if not exists in_caches_reason on " + dbTableCaches + " (reason)");
            db.execSQL("create index if not exists in_caches_detailed on " + dbTableCaches + " (detailed)");
            db.execSQL("create index if not exists in_caches_type on " + dbTableCaches + " (type)");
            db.execSQL("create index if not exists in_caches_visit_detail on " + dbTableCaches + " (visiteddate, detailedupdate)");
            db.execSQL("create index if not exists in_attr_geo on " + dbTableAttributes + " (geocode)");
            db.execSQL("create index if not exists in_wpts_geo on " + dbTableWaypoints + " (geocode)");
            db.execSQL("create index if not exists in_wpts_geo_type on " + dbTableWaypoints + " (geocode, type)");
            db.execSQL("create index if not exists in_spoil_geo on " + dbTableSpoilers + " (geocode)");
            db.execSQL("create index if not exists in_logs_geo on " + dbTableLogs + " (geocode)");
            db.execSQL("create index if not exists in_logcount_geo on " + dbTableLogCount + " (geocode)");
            db.execSQL("create index if not exists in_logsoff_geo on " + dbTableLogsOffline + " (geocode)");
            db.execSQL("create index if not exists in_trck_geo on " + dbTableTrackables + " (geocode)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i("Upgrade database from ver. " + oldVersion + " to ver. " + newVersion + ": start");

            try {
                if (db.isReadOnly()) {
                    return;
                }

                db.beginTransaction();

                if (oldVersion <= 0) { // new table
                    dropDatabase(db);
                    onCreate(db);

                    Log.i("Database structure created.");
                }

                if (oldVersion > 0) {
                    db.execSQL("delete from " + dbTableCaches + " where reason = 0");

                    if (oldVersion < 52) { // upgrade to 52
                        try {
                            db.execSQL(dbCreateSearchDestinationHistory);

                            Log.i("Added table " + dbTableSearchDestionationHistory + ".");
                        } catch (Exception e) {
                            Log.e("Failed to upgrade to ver. 52", e);
                        }
                    }

                    if (oldVersion < 53) { // upgrade to 53
                        try {
                            db.execSQL("alter table " + dbTableCaches + " add column onWatchlist integer");

                            Log.i("Column onWatchlist added to " + dbTableCaches + ".");
                        } catch (Exception e) {
                            Log.e("Failed to upgrade to ver. 53", e);
                        }
                    }

                    if (oldVersion < 54) { // update to 54
                        try {
                            db.execSQL(dbCreateLogImages);
                        } catch (Exception e) {
                            Log.e("Failed to upgrade to ver. 54: " + e.toString());

                        }
                    }

                    if (oldVersion < 55) { // update to 55
                        try {
                            db.execSQL("alter table " + dbTableCaches + " add column personal_note text");
                        } catch (Exception e) {
                            Log.e("Failed to upgrade to ver. 55: " + e.toString());
                        }
                    }

                    // make all internal attribute names lowercase
                    // @see issue #299
                    if (oldVersion < 56) { // update to 56
                        try {
                            db.execSQL("update " + dbTableAttributes + " set attribute = " +
                                    "lower(attribute) where attribute like \"%_yes\" " +
                                    "or attribute like \"%_no\"");
                        } catch (Exception e) {
                            Log.e("Failed to upgrade to ver. 56: " + e.toString());
                        }
                    }

                    // Create missing indices. See issue #435
                    if (oldVersion < 57) { // update to 57
                        try {
                            db.execSQL("drop index in_a");
                            db.execSQL("drop index in_b");
                            db.execSQL("drop index in_c");
                            db.execSQL("drop index in_d");
                            db.execSQL("drop index in_e");
                            db.execSQL("drop index in_f");
                            createIndices(db);
                        } catch (Exception e) {
                            Log.e("Failed to upgrade to ver. 57: " + e.toString());
                        }
                    }

                    if (oldVersion < 58) { // upgrade to 58
                        try {
                            db.beginTransaction();

                            final String dbTableCachesTemp = dbTableCaches + "_temp";
                            final String dbCreateCachesTemp = ""
                                    + "create table " + dbTableCachesTemp + " ("
                                    + "_id integer primary key autoincrement, "
                                    + "updated long not null, "
                                    + "detailed integer not null default 0, "
                                    + "detailedupdate long, "
                                    + "visiteddate long, "
                                    + "geocode text unique not null, "
                                    + "reason integer not null default 0, "
                                    + "cacheid text, "
                                    + "guid text, "
                                    + "type text, "
                                    + "name text, "
                                    + "own integer not null default 0, "
                                    + "owner text, "
                                    + "owner_real text, "
                                    + "hidden long, "
                                    + "hint text, "
                                    + "size text, "
                                    + "difficulty float, "
                                    + "terrain float, "
                                    + "latlon text, "
                                    + "location text, "
                                    + "direction double, "
                                    + "distance double, "
                                    + "latitude double, "
                                    + "longitude double, "
                                    + "reliable_latlon integer, "
                                    + "elevation double, "
                                    + "personal_note text, "
                                    + "shortdesc text, "
                                    + "description text, "
                                    + "favourite_cnt integer, "
                                    + "rating float, "
                                    + "votes integer, "
                                    + "myvote float, "
                                    + "disabled integer not null default 0, "
                                    + "archived integer not null default 0, "
                                    + "members integer not null default 0, "
                                    + "found integer not null default 0, "
                                    + "favourite integer not null default 0, "
                                    + "inventorycoins integer default 0, "
                                    + "inventorytags integer default 0, "
                                    + "inventoryunknown integer default 0, "
                                    + "onWatchlist integer default 0 "
                                    + "); ";

                            db.execSQL(dbCreateCachesTemp);
                            db.execSQL("insert into " + dbTableCachesTemp + " select _id,updated,detailed,detailedupdate,visiteddate,geocode,reason,cacheid,guid,type,name,own,owner,owner_real," +
                                    "hidden,hint,size,difficulty,terrain,latlon,location,direction,distance,latitude,longitude, 0,elevation," +
                                    "personal_note,shortdesc,description,favourite_cnt,rating,votes,myvote,disabled,archived,members,found,favourite,inventorycoins," +
                                    "inventorytags,inventoryunknown,onWatchlist from " + dbTableCaches);
                            db.execSQL("drop table " + dbTableCaches);
                            db.execSQL("alter table " + dbTableCachesTemp + " rename to " + dbTableCaches);

                            final String dbTableWaypointsTemp = dbTableWaypoints + "_temp";
                            final String dbCreateWaypointsTemp = ""
                                    + "create table " + dbTableWaypointsTemp + " ("
                                    + "_id integer primary key autoincrement, "
                                    + "geocode text not null, "
                                    + "updated long not null, " // date of save
                                    + "type text not null default 'waypoint', "
                                    + "prefix text, "
                                    + "lookup text, "
                                    + "name text, "
                                    + "latlon text, "
                                    + "latitude double, "
                                    + "longitude double, "
                                    + "note text "
                                    + "); ";
                            db.execSQL(dbCreateWaypointsTemp);
                            db.execSQL("insert into " + dbTableWaypointsTemp + " select _id, geocode, updated, type, prefix, lookup, name, latlon, latitude, longitude, note from " + dbTableWaypoints);
                            db.execSQL("drop table " + dbTableWaypoints);
                            db.execSQL("alter table " + dbTableWaypointsTemp + " rename to " + dbTableWaypoints);

                            createIndices(db);

                            db.setTransactionSuccessful();

                            Log.i("Removed latitude_string and longitude_string columns");
                        } catch (Exception e) {
                            Log.e("Failed to upgrade to ver. 58", e);
                        } finally {
                            db.endTransaction();
                        }
                    }

                    if (oldVersion < 59) {
                        try {
                            // Add new indices and remove obsolete cache files
                            createIndices(db);
                            removeObsoleteCacheDirectories(db);
                        } catch (Exception e) {
                            Log.e("Failed to upgrade to ver. 59", e);
                        }
                    }

                    if (oldVersion < 60) {
                        try {
                            removeSecEmptyDirs();
                        } catch (Exception e) {
                            Log.e("Failed to upgrade to ver. 60", e);
                        }
                    }
                    if (oldVersion < 61) {
                        try {
                            db.execSQL("alter table " + dbTableLogs + " add column friend integer");
                            db.execSQL("alter table " + dbTableCaches + " add column coordsChanged integer default 0");
                        } catch (Exception e) {
                            Log.e("Failed to upgrade to ver. 61: " + e.toString());

                        }
                    }
                    // Introduces finalDefined on caches and own on waypoints
                    if (oldVersion < 62) {
                        try {
                            db.execSQL("alter table " + dbTableCaches + " add column finalDefined integer default 0");
                            db.execSQL("alter table " + dbTableWaypoints + " add column own integer default 0");
                            db.execSQL("update " + dbTableWaypoints + " set own = 1 where type = 'own'");
                        } catch (Exception e) {
                            Log.e("Failed to upgrade to ver. 62: " + e.toString());

                        }
                    }
                    if (oldVersion < 63) {
                        try {
                            removeDoubleUnderscoreMapFiles();
                        } catch (Exception e) {
                            Log.e("Failed to upgrade to ver. 63: " + e.toString());

                        }
                    }

                    if (oldVersion < 64) {
                        try {
                            // No cache should ever be stored into the ALL_CACHES list. Here we use hardcoded list ids
                            // rather than symbolic ones because the fix must be applied with the values at the time
                            // of the problem. The problem was introduced in release 2012.06.01.
                            db.execSQL("update " + dbTableCaches + " set reason=1 where reason=2");
                        } catch (Exception e) {
                            Log.e("Failed to upgrade to ver. 64", e);
                        }
                    }
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            Log.i("Upgrade database from ver. " + oldVersion + " to ver. " + newVersion + ": completed");
        }

        /**
         * Method to remove static map files with double underscore due to issue#1670
         * introduced with release on 2012-05-24.
         */
        private static void removeDoubleUnderscoreMapFiles() {
            File[] geocodeDirs = LocalStorage.getStorage().listFiles();
            final FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return (filename.startsWith("map_") && filename.contains("__"));
                }
            };
            for (File dir : geocodeDirs) {
                File[] wrongFiles = dir.listFiles(filter);
                for (File wrongFile : wrongFiles) {
                    wrongFile.delete();
                }
            }
        }
    }

    /**
     * Remove obsolete cache directories in c:geo private storage.
     *
     * @param db
     *            the read-write database to use
     */
    private static void removeObsoleteCacheDirectories(final SQLiteDatabase db) {
        final Pattern oldFilePattern = Pattern.compile("^[GC|TB|O][A-Z0-9]{4,7}$");
        final SQLiteStatement select = db.compileStatement("select count(*) from " + dbTableCaches + " where geocode = ?");
        final File[] files = LocalStorage.getStorage().listFiles();
        final ArrayList<File> toRemove = new ArrayList<File>(files.length);
        for (final File file : files) {
            if (file.isDirectory()) {
                final String geocode = file.getName();
                if (oldFilePattern.matcher(geocode).find()) {
                    select.bindString(1, geocode);
                    if (select.simpleQueryForLong() == 0) {
                        toRemove.add(file);
                    }
                }
            }
        }

        // Use a background thread for the real removal to avoid keeping the database locked
        // if we are called from within a transaction.
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (final File dir : toRemove) {
                    Log.i("Removing obsolete cache directory for " + dir.getName());
                    LocalStorage.deleteDirectory(dir);
                }
            }
        }).start();
    }

    /*
     * Remove empty directories created in the secondary storage area.
     */
    private static void removeSecEmptyDirs() {
        for (final File file : LocalStorage.getStorageSec().listFiles()) {
            if (file.isDirectory()) {
                // This will silently fail if the directory is not empty.
                file.delete();
            }
        }
    }

    private static void dropDatabase(SQLiteDatabase db) {
        db.execSQL("drop table if exists " + dbTableCaches);
        db.execSQL("drop table if exists " + dbTableAttributes);
        db.execSQL("drop table if exists " + dbTableWaypoints);
        db.execSQL("drop table if exists " + dbTableSpoilers);
        db.execSQL("drop table if exists " + dbTableLogs);
        db.execSQL("drop table if exists " + dbTableLogCount);
        db.execSQL("drop table if exists " + dbTableLogsOffline);
        db.execSQL("drop table if exists " + dbTableTrackables);
    }

    public String[] allDetailedThere() {
        init();

        Cursor cursor = null;
        List<String> list = new ArrayList<String>();

        try {
            long timestamp = System.currentTimeMillis() - DAYS_AFTER_CACHE_IS_DELETED;
            cursor = database.query(
                    dbTableCaches,
                    new String[]{"geocode"},
                    "(detailed = 1 and detailedupdate > ?) or reason > 0",
                    new String[]{Long.toString(timestamp)},
                    null,
                    null,
                    "detailedupdate desc",
                    "100");

            if (cursor != null) {
                int index;

                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    index = cursor.getColumnIndex("geocode");

                    do {
                        list.add(cursor.getString(index));
                    } while (cursor.moveToNext());
                } else {
                    cursor.close();
                    return null;
                }
            }
        } catch (Exception e) {
            Log.e("cgData.allDetailedThere: " + e.toString());
        }

        if (cursor != null) {
            cursor.close();
        }

        return list.toArray(new String[list.size()]);
    }

    public boolean isThere(String geocode, String guid, boolean detailed, boolean checkTime) {
        init();

        Cursor cursor = null;

        int cnt = 0;
        long dataUpdated = 0;
        long dataDetailedUpdate = 0;
        int dataDetailed = 0;

        try {
            if (StringUtils.isNotBlank(geocode)) {
                cursor = database.query(
                        dbTableCaches,
                        new String[]{"detailed", "detailedupdate", "updated"},
                        "geocode = ?",
                        new String[]{geocode},
                        null,
                        null,
                        null,
                        "1");
            } else if (StringUtils.isNotBlank(guid)) {
                cursor = database.query(
                        dbTableCaches,
                        new String[]{"detailed", "detailedupdate", "updated"},
                        "guid = ?",
                        new String[]{guid},
                        null,
                        null,
                        null,
                        "1");
            } else {
                return false;
            }

            if (cursor != null) {
                int index;
                cnt = cursor.getCount();

                if (cnt > 0) {
                    cursor.moveToFirst();

                    index = cursor.getColumnIndex("updated");
                    dataUpdated = cursor.getLong(index);
                    index = cursor.getColumnIndex("detailedupdate");
                    dataDetailedUpdate = cursor.getLong(index);
                    index = cursor.getColumnIndex("detailed");
                    dataDetailed = cursor.getInt(index);
                }
            }
        } catch (Exception e) {
            Log.e("cgData.isThere: " + e.toString());
        }

        if (cursor != null) {
            cursor.close();
        }

        if (cnt > 0) {
            if (detailed && dataDetailed == 0) {
                // we want details, but these are not stored
                return false;
            }

            if (checkTime && detailed && dataDetailedUpdate < (System.currentTimeMillis() - DAYS_AFTER_CACHE_IS_DELETED)) {
                // we want to check time for detailed cache, but data are older than 3 hours
                return false;
            }

            if (checkTime && !detailed && dataUpdated < (System.currentTimeMillis() - DAYS_AFTER_CACHE_IS_DELETED)) {
                // we want to check time for short cache, but data are older than 3 hours
                return false;
            }

            // we have some cache
            return true;
        }

        // we have no such cache stored in cache
        return false;
    }

    /** is cache stored in one of the lists (not only temporary) */
    public boolean isOffline(String geocode, String guid) {
        if (StringUtils.isBlank(geocode) && StringUtils.isBlank(guid)) {
            return false;
        }
        init();

        try {
            final SQLiteStatement listId;
            final String value;
            if (StringUtils.isNotBlank(geocode)) {
                listId = getStatementListIdFromGeocode();
                value = geocode;
            }
            else {
                listId = getStatementListIdFromGuid();
                value = guid;
            }
            synchronized (listId) {
                listId.bindString(1, value);
                return listId.simpleQueryForLong() != StoredList.TEMPORARY_LIST_ID;
            }
        } catch (SQLiteDoneException e) {
            // Do nothing, it only means we have no information on the cache
        } catch (Exception e) {
            Log.e("cgData.isOffline", e);
        }

        return false;
    }

    public String getGeocodeForGuid(String guid) {
        if (StringUtils.isBlank(guid)) {
            return null;
        }
        init();

        try {
            final SQLiteStatement description = getStatementGeocode();
            synchronized (description) {
                description.bindString(1, guid);
                return description.simpleQueryForString();
            }
        } catch (SQLiteDoneException e) {
            // Do nothing, it only means we have no information on the cache
        } catch (Exception e) {
            Log.e("cgData.getGeocodeForGuid", e);
        }

        return null;
    }

    public String getCacheidForGeocode(String geocode) {
        if (StringUtils.isBlank(geocode)) {
            return null;
        }
        init();

        try {
            final SQLiteStatement description = getStatementCacheId();
            synchronized (description) {
                description.bindString(1, geocode);
                return description.simpleQueryForString();
            }
        } catch (SQLiteDoneException e) {
            // Do nothing, it only means we have no information on the cache
        } catch (Exception e) {
            Log.e("cgData.getCacheidForGeocode", e);
        }

        return null;
    }

    /**
     * Save/store a cache to the CacheCache
     *
     * @param cache
     *            the Cache to save in the CacheCache/DB
     * @param saveFlags
     *
     * @return true = cache saved successfully to the CacheCache/DB
     */
    public boolean saveCache(cgCache cache, EnumSet<LoadFlags.SaveFlag> saveFlags) {
        if (cache == null) {
            throw new IllegalArgumentException("cache must not be null");
        }

        // merge always with data already stored in the CacheCache or DB
        if (saveFlags.contains(SaveFlag.SAVE_CACHE)) {
            cache.gatherMissingFrom(cacheCache.getCacheFromCache(cache.getGeocode()));
            cacheCache.putCacheInCache(cache);
        }

        if (!saveFlags.contains(SaveFlag.SAVE_DB)) {
            return true;
        }
        boolean updateRequired = !cache.gatherMissingFrom(loadCache(cache.getGeocode(), LoadFlags.LOAD_ALL_DB_ONLY));

        // only save a cache to the database if
        // - the cache is detailed
        // - there are changes
        // - the cache is only stored in the CacheCache so far
        if ((!updateRequired || !cache.isDetailed()) && cache.getStorageLocation().contains(StorageLocation.DATABASE)) {
            return false;
        }

        cache.addStorageLocation(StorageLocation.DATABASE);
        cacheCache.putCacheInCache(cache);
        Log.d("Saving " + cache.toString() + " (" + cache.getListId() + ") to DB");

        ContentValues values = new ContentValues();

        if (cache.getUpdated() == 0) {
            values.put("updated", System.currentTimeMillis());
        } else {
            values.put("updated", cache.getUpdated());
        }
        values.put("reason", cache.getListId());
        values.put("detailed", cache.isDetailed() ? 1 : 0);
        values.put("detailedupdate", cache.getDetailedUpdate());
        values.put("visiteddate", cache.getVisitedDate());
        values.put("geocode", cache.getGeocode());
        values.put("cacheid", cache.getCacheId());
        values.put("guid", cache.getGuid());
        values.put("type", cache.getType().id);
        values.put("name", cache.getName());
        values.put("own", cache.isOwn() ? 1 : 0);
        values.put("owner", cache.getOwnerDisplayName());
        values.put("owner_real", cache.getOwnerUserId());
        if (cache.getHiddenDate() == null) {
            values.put("hidden", 0);
        } else {
            values.put("hidden", cache.getHiddenDate().getTime());
        }
        values.put("hint", cache.getHint());
        values.put("size", cache.getSize() == null ? "" : cache.getSize().id);
        values.put("difficulty", cache.getDifficulty());
        values.put("terrain", cache.getTerrain());
        values.put("latlon", cache.getLatlon());
        values.put("location", cache.getLocation());
        values.put("distance", cache.getDistance());
        values.put("direction", cache.getDirection());
        putCoords(values, cache.getCoords());
        values.put("reliable_latlon", cache.isReliableLatLon() ? 1 : 0);
        values.put("elevation", cache.getElevation());
        values.put("shortdesc", cache.getShortdesc());
        values.put("personal_note", cache.getPersonalNote());
        values.put("description", cache.getDescription());
        values.put("favourite_cnt", cache.getFavoritePoints());
        values.put("rating", cache.getRating());
        values.put("votes", cache.getVotes());
        values.put("myvote", cache.getMyVote());
        values.put("disabled", cache.isDisabled() ? 1 : 0);
        values.put("archived", cache.isArchived() ? 1 : 0);
        values.put("members", cache.isPremiumMembersOnly() ? 1 : 0);
        values.put("found", cache.isFound() ? 1 : 0);
        values.put("favourite", cache.isFavorite() ? 1 : 0);
        values.put("inventoryunknown", cache.getInventoryItems());
        values.put("onWatchlist", cache.isOnWatchlist() ? 1 : 0);
        values.put("coordsChanged", cache.hasUserModifiedCoords() ? 1 : 0);
        values.put("finalDefined", cache.hasFinalDefined() ? 1 : 0);

        boolean result = false;
        init();

        //try to update record else insert fresh..
        database.beginTransaction();

        try {
            saveAttributesWithoutTransaction(cache);
            saveOriginalWaypointsWithoutTransaction(cache);
            saveSpoilersWithoutTransaction(cache);
            saveLogsWithoutTransaction(cache.getGeocode(), cache.getLogs());
            saveLogCountsWithoutTransaction(cache);
            saveInventoryWithoutTransaction(cache.getGeocode(), cache.getInventory());

            int rows = database.update(dbTableCaches, values, "geocode = ?", new String[] { cache.getGeocode() });
            if (rows == 0) {
                // cache is not in the DB, insert it
                /* long id = */
                database.insert(dbTableCaches, null, values);
            }
            database.setTransactionSuccessful();
            result = true;
        } catch (Exception e) {
            Log.e("SaveCache", e);
        } finally {
            database.endTransaction();
        }

        return result;
    }

    private void saveAttributesWithoutTransaction(final cgCache cache) {
        String geocode = cache.getGeocode();
        database.delete(dbTableAttributes, "geocode = ?", new String[]{geocode});

        if (cache.getAttributes().isEmpty()) {
            return;
        }
        SQLiteStatement statement = getStatementInsertAttribute();
        long timeStamp = System.currentTimeMillis();
        for (String attribute : cache.getAttributes()) {
            statement.bindString(1, geocode);
            statement.bindLong(2, timeStamp);
            statement.bindString(3, attribute);

            statement.executeInsert();
        }
    }

    /**
     * Persists the given <code>destination</code> into the database.
     *
     * @param destination
     *            a destination to save
     */
    public void saveSearchedDestination(final Destination destination) {
        init();

        database.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put("date", destination.getDate());
            putCoords(values, destination.getCoords());
            database.insert(dbTableSearchDestionationHistory, null, values);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("Updating searchedDestinations db failed", e);
        } finally {
            database.endTransaction();
        }
    }

    public boolean saveWaypoints(final cgCache cache) {
        boolean result = false;
        init();
        database.beginTransaction();

        try {
            saveOriginalWaypointsWithoutTransaction(cache);
            database.setTransactionSuccessful();
            result = true;
        } catch (Exception e) {
            Log.e("saveWaypoints", e);
        } finally {
            database.endTransaction();
        }
        return result;
    }

    private void saveOriginalWaypointsWithoutTransaction(final cgCache cache) {
        String geocode = cache.getGeocode();
        database.delete(dbTableWaypoints, "geocode = ? and type <> ? and own = 0", new String[]{geocode, "own"});

        List<cgWaypoint> waypoints = cache.getWaypoints();
        if (CollectionUtils.isNotEmpty(waypoints)) {
            ContentValues values = new ContentValues();
            long timeStamp = System.currentTimeMillis();
            for (cgWaypoint oneWaypoint : waypoints) {
                if (oneWaypoint.isUserDefined()) {
                    continue;
                }

                values.clear();
                values.put("geocode", geocode);
                values.put("updated", timeStamp);
                values.put("type", oneWaypoint.getWaypointType() != null ? oneWaypoint.getWaypointType().id : null);
                values.put("prefix", oneWaypoint.getPrefix());
                values.put("lookup", oneWaypoint.getLookup());
                values.put("name", oneWaypoint.getName());
                values.put("latlon", oneWaypoint.getLatlon());
                putCoords(values, oneWaypoint.getCoords());
                values.put("note", oneWaypoint.getNote());
                values.put("own", oneWaypoint.isUserDefined() ? 1 : 0);

                final long rowId = database.insert(dbTableWaypoints, null, values);
                oneWaypoint.setId((int) rowId);
            }
        }
    }

    /**
     * Save coordinates into a ContentValues
     *
     * @param values
     *            a ContentValues to save coordinates in
     * @param oneWaypoint
     *            coordinates to save, or null to save empty coordinates
     */
    private static void putCoords(final ContentValues values, final Geopoint coords) {
        values.put("latitude", coords == null ? null : coords.getLatitude());
        values.put("longitude", coords == null ? null : coords.getLongitude());
    }

    /**
     * Retrieve coordinates from a Cursor
     *
     * @param cursor
     *            a Cursor representing a row in the database
     * @param indexLat
     *            index of the latitude column
     * @param indexLon
     *            index of the longitude column
     * @return the coordinates, or null if latitude or longitude is null or the coordinates are invalid
     */
    private static Geopoint getCoords(final Cursor cursor, final int indexLat, final int indexLon) {
        if (cursor.isNull(indexLat) || cursor.isNull(indexLon)) {
            return null;
        }

        return new Geopoint(cursor.getDouble(indexLat), cursor.getDouble(indexLon));
    }

    public boolean saveWaypoint(int id, String geocode, cgWaypoint waypoint) {
        if ((StringUtils.isBlank(geocode) && id <= 0) || waypoint == null) {
            return false;
        }

        init();

        boolean ok = false;
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("geocode", geocode);
            values.put("updated", System.currentTimeMillis());
            values.put("type", waypoint.getWaypointType() != null ? waypoint.getWaypointType().id : null);
            values.put("prefix", waypoint.getPrefix());
            values.put("lookup", waypoint.getLookup());
            values.put("name", waypoint.getName());
            values.put("latlon", waypoint.getLatlon());
            putCoords(values, waypoint.getCoords());
            values.put("note", waypoint.getNote());
            values.put("own", waypoint.isUserDefined() ? 1 : 0);

            if (id <= 0) {
                final long rowId = database.insert(dbTableWaypoints, null, values);
                waypoint.setId((int) rowId);
                ok = true;
            } else {
                final int rows = database.update(dbTableWaypoints, values, "_id = " + id, null);
                ok = rows > 0;
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        return ok;
    }

    public boolean deleteWaypoint(int id) {
        if (id == 0) {
            return false;
        }

        init();

        return database.delete(dbTableWaypoints, "_id = " + id, null) > 0;
    }

    private void saveSpoilersWithoutTransaction(final cgCache cache) {
        String geocode = cache.getGeocode();
        database.delete(dbTableSpoilers, "geocode = ?", new String[]{geocode});

        List<cgImage> spoilers = cache.getSpoilers();
        if (CollectionUtils.isNotEmpty(spoilers)) {
            ContentValues values = new ContentValues();
            long timeStamp = System.currentTimeMillis();
            for (cgImage spoiler : spoilers) {
                values.clear();
                values.put("geocode", geocode);
                values.put("updated", timeStamp);
                values.put("url", spoiler.getUrl());
                values.put("title", spoiler.getTitle());
                values.put("description", spoiler.getDescription());

                database.insert(dbTableSpoilers, null, values);
            }
        }
    }

    private void saveLogsWithoutTransaction(final String geocode, final Iterable<LogEntry> logs) {
        // TODO delete logimages referring these logs
        database.delete(dbTableLogs, "geocode = ?", new String[]{geocode});

        if (!logs.iterator().hasNext()) {
            return;
        }

        SQLiteStatement statement = getStatementInsertLog();
        long timeStamp = System.currentTimeMillis();
        for (LogEntry log : logs) {
            statement.bindString(1, geocode);
            statement.bindLong(2, timeStamp);
            statement.bindLong(3, log.type.id);
            statement.bindString(4, log.author);
            statement.bindString(5, log.log);
            statement.bindLong(6, log.date);
            statement.bindLong(7, log.found);
            statement.bindLong(8, log.friend ? 1 : 0);
            long logId = statement.executeInsert();
            if (log.hasLogImages()) {
                ContentValues values = new ContentValues();
                for (cgImage img : log.getLogImages()) {
                    values.clear();
                    values.put("log_id", logId);
                    values.put("title", img.getTitle());
                    values.put("url", img.getUrl());
                    database.insert(dbTableLogImages, null, values);
                }
            }
        }
    }

    private void saveLogCountsWithoutTransaction(final cgCache cache) {
        String geocode = cache.getGeocode();
        database.delete(dbTableLogCount, "geocode = ?", new String[]{geocode});

        Map<LogType, Integer> logCounts = cache.getLogCounts();
        if (MapUtils.isNotEmpty(logCounts)) {
            ContentValues values = new ContentValues();

            Set<Entry<LogType, Integer>> logCountsItems = logCounts.entrySet();
            long timeStamp = System.currentTimeMillis();
            for (Entry<LogType, Integer> pair : logCountsItems) {
                values.clear();
                values.put("geocode", geocode);
                values.put("updated", timeStamp);
                values.put("type", pair.getKey().id);
                values.put("count", pair.getValue());

                database.insert(dbTableLogCount, null, values);
            }
        }
    }

    public boolean saveTrackable(final cgTrackable trackable) {
        init();

        database.beginTransaction();
        try {
            saveInventoryWithoutTransaction(null, Collections.singletonList(trackable));
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        return true;
    }

    private void saveInventoryWithoutTransaction(final String geocode, final List<cgTrackable> trackables) {
        if (geocode != null) {
            database.delete(dbTableTrackables, "geocode = ?", new String[]{geocode});
        }

        if (CollectionUtils.isNotEmpty(trackables)) {
            ContentValues values = new ContentValues();
            long timeStamp = System.currentTimeMillis();
            for (cgTrackable trackable : trackables) {
                final String tbCode = trackable.getGeocode();
                if (StringUtils.isNotBlank(tbCode)) {
                    database.delete(dbTableTrackables, "tbcode = ?", new String[] { tbCode });
                }
                values.clear();
                if (geocode != null) {
                    values.put("geocode", geocode);
                }
                values.put("updated", timeStamp);
                values.put("tbcode", tbCode);
                values.put("guid", trackable.getGuid());
                values.put("title", trackable.getName());
                values.put("owner", trackable.getOwner());
                if (trackable.getReleased() != null) {
                    values.put("released", trackable.getReleased().getTime());
                } else {
                    values.put("released", 0L);
                }
                values.put("goal", trackable.getGoal());
                values.put("description", trackable.getDetails());

                database.insert(dbTableTrackables, null, values);

                saveLogsWithoutTransaction(tbCode, trackable.getLogs());
            }
        }
    }

    public Viewport getBounds(final Set<String> geocodes) {
        if (CollectionUtils.isEmpty(geocodes)) {
            return null;
        }

        final Set<cgCache> caches = loadCaches(geocodes, LoadFlags.LOAD_CACHE_OR_DB);
        return Viewport.containing(caches);
    }

    /**
     * Load a single Cache.
     *
     * @param geocode
     *            The Geocode GCXXXX
     * @return the loaded cache (if found). Can be null
     */
    public cgCache loadCache(final String geocode, final EnumSet<LoadFlag> loadFlags) {
        if (StringUtils.isBlank(geocode)) {
            throw new IllegalArgumentException("geocode must not be empty");
        }

        final Set<cgCache> caches = loadCaches(Collections.singleton(geocode), loadFlags);
        return caches.isEmpty() ? null : caches.iterator().next();
    }

    /**
     * Load caches.
     *
     * @param geocodes
     * @return Set of loaded caches. Never null.
     */
    public Set<cgCache> loadCaches(final Set<String> geocodes, final EnumSet<LoadFlag> loadFlags) {
        if (CollectionUtils.isEmpty(geocodes)) {
            return new HashSet<cgCache>();
        }

        Set<cgCache> result = new HashSet<cgCache>();
        Set<String> remaining = new HashSet<String>(geocodes);

        if (loadFlags.contains(LoadFlag.LOAD_CACHE_BEFORE)) {
            for (String geocode : new HashSet<String>(remaining)) {
                cgCache cache = cacheCache.getCacheFromCache(geocode);
                if (cache != null) {
                    result.add(cache);
                    remaining.remove(cache.getGeocode());
                }
            }
        }

        if (loadFlags.contains(LoadFlag.LOAD_DB_MINIMAL) ||
                loadFlags.contains(LoadFlag.LOAD_ATTRIBUTES) ||
                loadFlags.contains(LoadFlag.LOAD_WAYPOINTS) ||
                loadFlags.contains(LoadFlag.LOAD_SPOILERS) ||
                loadFlags.contains(LoadFlag.LOAD_LOGS) ||
                loadFlags.contains(LoadFlag.LOAD_INVENTORY) ||
                loadFlags.contains(LoadFlag.LOAD_OFFLINE_LOG)) {

            final Set<cgCache> cachesFromDB = loadCachesFromGeocodes(remaining, loadFlags);
            result.addAll(cachesFromDB);
            for (final cgCache cache : cachesFromDB) {
                remaining.remove(cache.getGeocode());
            }
        }

        if (loadFlags.contains(LoadFlag.LOAD_CACHE_AFTER)) {
            for (String geocode : new HashSet<String>(remaining)) {
                cgCache cache = cacheCache.getCacheFromCache(geocode);
                if (cache != null) {
                    result.add(cache);
                    remaining.remove(cache.getGeocode());
                }
            }
        }

        if (remaining.size() >= 1) {
            Log.e("cgData.loadCaches(" + remaining.toString() + ") failed");
        }
        return result;
    }

    /**
     * Load caches.
     *
     * @param geocodes
     * @param loadFlags
     * @return Set of loaded caches. Never null.
     */
    private Set<cgCache> loadCachesFromGeocodes(final Set<String> geocodes, final EnumSet<LoadFlag> loadFlags) {
        if (CollectionUtils.isEmpty(geocodes)) {
            return Collections.emptySet();
        }


        Log.d("cgData.loadCachesFromGeocodes(" + geocodes.toString() + ") from DB");

        init();

        final StringBuilder query = new StringBuilder("SELECT ");
        for (int i = 0; i < CACHE_COLUMNS.length; i++) {
            query.append(i > 0 ? ", " : "").append(dbTableCaches).append('.').append(CACHE_COLUMNS[i]).append(' ');
        }
        if (loadFlags.contains(LoadFlag.LOAD_OFFLINE_LOG)) {
            query.append(',').append(dbTableLogsOffline).append(".log");
        }

        query.append(" FROM ").append(dbTableCaches);
        if (loadFlags.contains(LoadFlag.LOAD_OFFLINE_LOG)) {
            query.append(" LEFT OUTER JOIN ").append(dbTableLogsOffline).append(" ON ( ").append(dbTableCaches).append(".geocode == ").append(dbTableLogsOffline).append(".geocode) ");
        }

        query.append(" WHERE ").append(dbTableCaches).append('.');
        query.append(cgData.whereGeocodeIn(geocodes));

        Cursor cursor = database.rawQuery(query.toString(), null);
        try {
            if (!cursor.moveToFirst()) {
                return Collections.emptySet();
            }

            final Set<cgCache> caches = new HashSet<cgCache>();
            int logIndex = -1;
            do {
                //Extracted Method = LOADDBMINIMAL
                cgCache cache = cgData.createCacheFromDatabaseContent(cursor);

                if (loadFlags.contains(LoadFlag.LOAD_ATTRIBUTES)) {
                    cache.setAttributes(loadAttributes(cache.getGeocode()));
                }

                if (loadFlags.contains(LoadFlag.LOAD_WAYPOINTS)) {
                    final List<cgWaypoint> waypoints = loadWaypoints(cache.getGeocode());
                    if (CollectionUtils.isNotEmpty(waypoints)) {
                        cache.setWaypoints(waypoints, false);
                    }
                }

                if (loadFlags.contains(LoadFlag.LOAD_SPOILERS)) {
                    final List<cgImage> spoilers = loadSpoilers(cache.getGeocode());
                    cache.setSpoilers(spoilers);
                }

                if (loadFlags.contains(LoadFlag.LOAD_LOGS)) {
                    cache.setLogs(loadLogs(cache.getGeocode()));
                    final Map<LogType, Integer> logCounts = loadLogCounts(cache.getGeocode());
                    if (MapUtils.isNotEmpty(logCounts)) {
                        cache.getLogCounts().clear();
                        cache.getLogCounts().putAll(logCounts);
                    }
                }

                if (loadFlags.contains(LoadFlag.LOAD_INVENTORY)) {
                    final List<cgTrackable> inventory = loadInventory(cache.getGeocode());
                    if (CollectionUtils.isNotEmpty(inventory)) {
                        if (cache.getInventory() == null) {
                            cache.setInventory(new ArrayList<cgTrackable>());
                        } else {
                            cache.getInventory().clear();
                        }
                        cache.getInventory().addAll(inventory);
                    }
                }

                if (loadFlags.contains(LoadFlag.LOAD_OFFLINE_LOG)) {
                    if (logIndex < 0) {
                        logIndex = cursor.getColumnIndex("log");
                    }
                    cache.setLogOffline(!cursor.isNull(logIndex));
                }
                cache.addStorageLocation(StorageLocation.DATABASE);
                cacheCache.putCacheInCache(cache);

                caches.add(cache);
            } while (cursor.moveToNext());
            return caches;
        } finally {
            cursor.close();
        }
    }


    /**
     * Builds a where for a viewport with the size enhanced by 50%.
     *
     * @param dbTable
     * @param viewport
     * @return
     */

    private static String buildCoordinateWhere(final String dbTable, final Viewport viewport) {
        return viewport.resize(1.5).sqlWhere(dbTable);
    }

    /**
     * creates a Cache from the cursor. Doesn't next.
     *
     * @param cursor
     * @return Cache from DB
     */
    private static cgCache createCacheFromDatabaseContent(Cursor cursor) {
        int index;
        cgCache cache = new cgCache();

        if (cacheColumnIndex == null) {
            int[] local_cci = new int[41]; // use a local variable to avoid having the not yet fully initialized array be visible to other threads
            local_cci[0] = cursor.getColumnIndex("updated");
            local_cci[1] = cursor.getColumnIndex("reason");
            local_cci[2] = cursor.getColumnIndex("detailed");
            local_cci[3] = cursor.getColumnIndex("detailedupdate");
            local_cci[4] = cursor.getColumnIndex("visiteddate");
            local_cci[5] = cursor.getColumnIndex("geocode");
            local_cci[6] = cursor.getColumnIndex("cacheid");
            local_cci[7] = cursor.getColumnIndex("guid");
            local_cci[8] = cursor.getColumnIndex("type");
            local_cci[9] = cursor.getColumnIndex("name");
            local_cci[10] = cursor.getColumnIndex("own");
            local_cci[11] = cursor.getColumnIndex("owner");
            local_cci[12] = cursor.getColumnIndex("owner_real");
            local_cci[13] = cursor.getColumnIndex("hidden");
            local_cci[14] = cursor.getColumnIndex("hint");
            local_cci[15] = cursor.getColumnIndex("size");
            local_cci[16] = cursor.getColumnIndex("difficulty");
            local_cci[17] = cursor.getColumnIndex("direction");
            local_cci[18] = cursor.getColumnIndex("distance");
            local_cci[19] = cursor.getColumnIndex("terrain");
            local_cci[20] = cursor.getColumnIndex("latlon");
            local_cci[21] = cursor.getColumnIndex("location");
            local_cci[22] = cursor.getColumnIndex("elevation");
            local_cci[23] = cursor.getColumnIndex("personal_note");
            local_cci[24] = cursor.getColumnIndex("shortdesc");
            local_cci[25] = cursor.getColumnIndex("favourite_cnt");
            local_cci[26] = cursor.getColumnIndex("rating");
            local_cci[27] = cursor.getColumnIndex("votes");
            local_cci[28] = cursor.getColumnIndex("myvote");
            local_cci[29] = cursor.getColumnIndex("disabled");
            local_cci[30] = cursor.getColumnIndex("archived");
            local_cci[31] = cursor.getColumnIndex("members");
            local_cci[32] = cursor.getColumnIndex("found");
            local_cci[33] = cursor.getColumnIndex("favourite");
            local_cci[34] = cursor.getColumnIndex("inventoryunknown");
            local_cci[35] = cursor.getColumnIndex("onWatchlist");
            local_cci[36] = cursor.getColumnIndex("reliable_latlon");
            local_cci[37] = cursor.getColumnIndex("coordsChanged");
            local_cci[38] = cursor.getColumnIndex("latitude");
            local_cci[39] = cursor.getColumnIndex("longitude");
            local_cci[4
