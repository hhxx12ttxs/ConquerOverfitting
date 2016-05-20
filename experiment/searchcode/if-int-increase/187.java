package com.rothconsulting.android.radiorec.sqlitedb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.rothconsulting.android.common.Utils;

public class DBHelper extends SQLiteOpenHelper {

	private static final String TAG = "DBHelper";

	public static final String DATABASE_NAME = "RadioRecPlus.db";

	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table " + DbAdapter.T_STATION + " (" + DbAdapter.KEY_ROWID + " integer primary key autoincrement, "
			+ DbAdapter.KEY_STATION_NAME + " text not null);";

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		Utils.log(TAG, "executeSQL. database=" + database);
		database.execSQL(DATABASE_CREATE);
	}

	// Method is called during an upgrade of the database, e.g. if you increase
	// the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(DBHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + DbAdapter.T_STATION);
		onCreate(database);
	}
}
