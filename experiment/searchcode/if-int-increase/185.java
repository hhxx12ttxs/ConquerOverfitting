package com.example.android.notepad.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NotepadDatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "nbtable.db";
	private static final int DATABASE_VERSION = 1;

	public NotepadDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		NotepadTable.onCreate(database);
	}

	// Method is called during an upgrade of the database,
	// e.g. if you increase the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		NotepadTable.onUpgrade(database, oldVersion, newVersion);
	}

}

