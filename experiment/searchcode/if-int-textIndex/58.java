package com.mutineer.kissel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;

import com.mutineer.kissel.api.JuickMessage;
import com.mutineer.kissel.api.JuickUser;

public class DBHelper extends SQLiteOpenHelper {
    static private final int DB_VERSION = 7;
    static         final String DB_NAME = "juick_messages.db";
    static         final String HOMELINE_TABLE = "homeline";
    static         final String USERS_TABLE = "users";
    static         final String REPLIES_TABLE = "replies";
    static         final String HOMELINE_APPEARANCE_INDEX = "homeline_app_ind";
    static         final String C_ID = BaseColumns._ID;
    static         final String C_RID = "rid";
    static         final String C_USER = "user";
    static         final String C_CREATED_AT = "created_at";
    static         final String C_APPEARANCE = "appearance";
    static         final String C_TEXT = "text";
    static         final String C_TAGS = "tags";
    static         final String C_PHOTO = "photo";
    static         final String C_LINKS = "links";
    static         final String C_NOT_IN_FRIENDS = "not_in_firends";
    static private final int MIN_LIMIT = 60;
    static private final int MAX_LIMIT = 100;
    private Context context;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " + HOMELINE_TABLE + " (" + C_ID + " integer primary key, " +
                      C_CREATED_AT + " integer, " + C_APPEARANCE + " integer, " + C_USER + " text, " + C_TEXT + " text, " +
                      C_NOT_IN_FRIENDS + " integer, " + C_PHOTO + " text, " + C_LINKS + " text, " + C_TAGS + " text)";
        
        db.execSQL(sql);
        
        sql = "create table " + USERS_TABLE + " (" + C_ID + " integer primary key, " + C_USER + " text)";
        db.execSQL(sql);
        
        sql = "create index " + HOMELINE_APPEARANCE_INDEX + " on " + HOMELINE_TABLE + "(" + C_APPEARANCE + " DESC)";
        db.execSQL(sql);
        
        sql = "create table " + REPLIES_TABLE + " (" + C_ID + " integer, " + C_RID + " integer, " +
               C_CREATED_AT + " integer, " + C_USER + " text, " + C_TEXT + " text, " + C_NOT_IN_FRIENDS + " integer, " +
               C_PHOTO + " text, " + C_LINKS + " text, primary key (" + C_ID + ", " + C_RID + "))";
        db.execSQL(sql);
        Log.d("Database", "Create");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + HOMELINE_TABLE);
        db.execSQL("drop table if exists " + USERS_TABLE);
        db.execSQL("drop table if exists " + REPLIES_TABLE);
        db.execSQL("drop index if exists " + HOMELINE_APPEARANCE_INDEX);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Editor  edit = sp.edit();
        edit.remove(HomeUpdateService.FRIENDS_UPDATE_KEY);
        edit.commit();

        Log.d("Database", "Upgrade");
        onCreate(db);
    }

    public int addMessages(Vector<JuickMessage> messages) {
        int nNewMessages = 0;
        if(messages == null || messages.isEmpty())
            return nNewMessages;
        
        SQLiteDatabase db = getWritableDatabase();
        
        ContentValues values = new ContentValues();
        for(JuickMessage message : messages)
        {
            values.clear();
            values.put(C_ID, message.MID);
            values.put(C_CREATED_AT, message.Timestamp.getTime());
            values.put(C_APPEARANCE, message.Appearance.getTime());
            values.put(C_USER, message.UserName);
            values.put(C_TEXT, message.Text);
            values.put(C_TAGS, message.Tags);
            values.put(C_PHOTO, message.Photo);
            values.put(C_LINKS, message.Links);
            values.put(C_NOT_IN_FRIENDS, message.notFromFriends);
            
            long res = -1;
            try {
                res = db.insertOrThrow(HOMELINE_TABLE, null, values);
            } catch (Exception e) {
            }
            if(res != -1)
                nNewMessages++;
        }
        
        db.close();
        return nNewMessages;
    }
    
    public void saveFriendsList(Vector<JuickUser> users) {
        if(users == null)
            return;
        
        SQLiteDatabase db = getWritableDatabase();
        db.delete(USERS_TABLE, null, null);
        
        ContentValues values = new ContentValues();
        for(JuickUser user : users) {
            values.clear();
            values.put(C_ID, user.UID);
            values.put(C_USER, user.UName);
            
            db.insert(USERS_TABLE, null, values);
        }
        
        db.close();
    }
    
    public void deleteOldMessages() {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            int numMessages = numMessages(db);
            Log.d("Database", "messages in database: " + String.valueOf(numMessages));
            if(numMessages > MAX_LIMIT) {
                long oldestAppearance;
                Cursor cur = db.query(HOMELINE_TABLE, new String[]{C_ID, C_APPEARANCE}, null, null, null, null, C_APPEARANCE + " DESC", String.valueOf(MIN_LIMIT));
                cur.moveToLast();
                oldestAppearance = cur.getLong(cur.getColumnIndex(C_APPEARANCE));
                cur.close();
                
                //deleting old messages
                db.delete(HOMELINE_TABLE, C_APPEARANCE + " < ?", new String[]{String.valueOf(oldestAppearance)});
                Log.d("Database", "After deleting: " + String.valueOf(numMessages(db)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(db != null)
            db.close();
    }
    
    boolean isUserInFriends(SQLiteDatabase db, String uname) {
        int nNum = 0;
        
        Cursor cur = db.query(USERS_TABLE, new String[]{C_USER}, C_USER + " = ?", new String[]{uname}, null, null, null);
        nNum = cur.getCount();
        cur.close();
        
        return nNum == 0;
    }
    
    HashSet<Integer> getFriendsIDs() {
        HashSet<Integer> IDs = null;
        SQLiteDatabase db = null;
        
        try {
            db = getReadableDatabase();
            Cursor cur = db.query(USERS_TABLE, new String[]{C_ID}, null, null, null, null, null);
            IDs = new HashSet<Integer>(cur.getCount());
            int idIndex = cur.getColumnIndex(C_ID);
            if(cur.moveToFirst()) {
                while(!cur.isAfterLast()) {
                    IDs.add(cur.getInt(idIndex));
                    cur.moveToNext();
                }
            }
            cur.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if(db != null)
            db.close();
        if(IDs == null)
            IDs = new HashSet<Integer>();
        return IDs;
    }
    
    public ArrayList<JuickMessage> getReplies(int mid) {
        ArrayList<JuickMessage> replies = new ArrayList<JuickMessage>();
        SQLiteDatabase db = null;
        Cursor cur = null;
        
        try {
            db = getReadableDatabase();
            String columns[] = new String[]{C_ID, C_RID, C_CREATED_AT, C_USER, C_TEXT, C_NOT_IN_FRIENDS, C_PHOTO, C_LINKS};
            cur = db.query(REPLIES_TABLE, columns, C_ID + " = ?", new String[]{String.valueOf(mid)}, null, null, C_RID + " ASC");
            
            if (cur.moveToFirst()) {
                int idIndex           = cur.getColumnIndex(C_ID);
                int ridIndex          = cur.getColumnIndex(C_RID);
                int timeIndex         = cur.getColumnIndex(C_CREATED_AT);
                int userIndex         = cur.getColumnIndex(C_USER);
                int textIndex         = cur.getColumnIndex(C_TEXT);
                int notInFriendsIndex = cur.getColumnIndex(C_NOT_IN_FRIENDS);
                int photoIndex        = cur.getColumnIndex(C_PHOTO);
                int linksIndex        = cur.getColumnIndex(C_LINKS);
                JuickMessage msg = null;
                
                while(!cur.isAfterLast()) {
                    msg = new JuickMessage();
                    msg.MID            = cur.getInt(idIndex);
                    msg.RID            = cur.getInt(ridIndex);
                    msg.Timestamp      = new Date(cur.getLong(timeIndex));
                    msg.UserName       = cur.getString(userIndex);
                    msg.Text           = cur.getString(textIndex);
                    msg.notFromFriends = (cur.getInt(notInFriendsIndex) != 0);
                    msg.Photo          = cur.getString(photoIndex);
                    msg.Links          = cur.getString(linksIndex);
                    replies.add(msg);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            replies.clear();
        } finally {
            if(cur != null)
                cur.close();
            if(db != null)
                db.close();
        }
        
        return replies;
    }
    
    private int numMessages(SQLiteDatabase db) {
        int nRes = 0;
        Cursor cur = db.query(HOMELINE_TABLE, new String[]{"count(" + C_ID + ")"}, null, null, null, null, null);
        cur.moveToFirst();
        nRes = cur.getInt(0);
        cur.close();
        return nRes;
    }
    
    static JuickMessage getMessage(Cursor cursor) {
        JuickMessage message = new JuickMessage();
        message.MID               = cursor.getInt(    cursor.getColumnIndex(C_ID)    );
        message.UserName          = cursor.getString( cursor.getColumnIndex(C_USER)  );
        message.Tags              = cursor.getString( cursor.getColumnIndex(C_TAGS)  );
        message.Text              = cursor.getString( cursor.getColumnIndex(C_TEXT)  );
        message.Photo             = cursor.getString( cursor.getColumnIndex(C_PHOTO) );
        message.Links             = cursor.getString( cursor.getColumnIndex(C_LINKS) );
        message.notFromFriends = (cursor.getInt(cursor.getColumnIndex(C_NOT_IN_FRIENDS)) != 0);
        return message;
    }
}

