package org.jbs.happysad;

import static android.provider.BaseColumns._ID;
import static org.jbs.happysad.Constants.EMO;
import static org.jbs.happysad.Constants.LAT;
import static org.jbs.happysad.Constants.LONG;
import static org.jbs.happysad.Constants.SYNC;
import static org.jbs.happysad.Constants.MSG;
import static org.jbs.happysad.Constants.TABLE_NAME;
import static org.jbs.happysad.Constants.TIME;
import static org.jbs.happysad.Constants.UID;
import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.jbs.happysad.HappyBottle;
import android.util.Log;
/**
 * helper class that serves as a layer of abstraction between the user and the database.
 * @author HappyTrack
 */
public class HappyData {
	//for debugging purposes, delete when no longer needed
	private static final String TAG = "HappyData";

	//fields
	private HappyDB h;
	//private static final long MyUserID = 1; //this should not be hardcoded to 1
	private UIDhelper UIDh =  new UIDhelper();


	long myID = -1; 
	private static String[] FROM = { _ID, UID, LAT, LONG, EMO, MSG, TIME, SYNC };
	private static String ORDER_BY = TIME + " DESC";
	private NetHelper net = new NetHelper();

	public HappyData(Context ctx){
		h = new HappyDB(ctx);
		//mainThread = new Handler();
		myID = UIDh.getUID();
	}

	//Return true/false if the application can add HappyBottle b
	protected boolean addBottle(HappyBottle b){
		return addBottle(b, false);
	}

	/**
	 * Tries to add a bottle to the local db. Return true/false if it was successful or not.
	 * @param b HappyBottle
	 * @param isSynced Has this bottle been uploaded to the server?
	 * @return
	 */
	public synchronized boolean addBottle(HappyBottle b, boolean isSynced){
		boolean toreturn = false;
		//get the db
		SQLiteDatabase db = h.getWritableDatabase();
		ContentValues values = b.getAll();
		int z = isSynced?1:0;
		values.put(SYNC, z);
		//add a bottle. 
		try {
			db.insertOrThrow(TABLE_NAME, null, values);
			toreturn = true;	
		}
		catch(Exception e){
			Log.e(TAG, e.toString());
			toreturn = false;
		}
		finally{
			h.close();
		}	
		return toreturn;
	}


	//for each entry that isn't synced, send to database. 
	protected void syncUp(){
		SQLiteDatabase db = h.getReadableDatabase();
		//now we check each row in the database to see if it's been synced yet. For those that haven't, and that belong to you:
		Cursor cursor = db.query(TABLE_NAME, null, UID+"="+myID+" AND " + SYNC + "=0", null, null, null, null);
		//iterate through the database
		while (cursor.moveToNext() ){
			//if we hit a bottle that has not been synced, and is ours, we upload it.
			HappyBottle b = createBottle(cursor);
			boolean result = net.upload(b);
			//we just uploaded it.
			if (result){
				SQLiteDatabase dbwrite = h.getWritableDatabase();
				ContentValues c = new ContentValues();
				int rowid = cursor.getInt(0);
				c.put(SYNC, 1);
				Log.d(TAG, "update query is: "+ _ID+"="+rowid);
				Log.d(TAG, "for msg: " + cursor.getString(5));
				//with rowid, we find the unique identifier for this row
				//then with update, we change SYNC to 1, but only for the column with our id.
				dbwrite.update(TABLE_NAME, c, _ID+"="+rowid , null);
				dbwrite.close();}
			else{ 
				//the update didn't go through. Probably because we don't have a network connection. That's fine.
				//So don't update the row saying that it is synced. This lets us try to update later when we have a network connection.
			}
		}
		cursor.close();
		db.close(); 
	}

	//syncs down everything. Temp - get rid of it.
	protected void syncDown(){
		//syncMyDown();
		syncAllDown();
		Log.d(TAG, "synced down");

	}

	//same as syncAllDown, but just for my bottles
	protected void syncMyDown(){
		ArrayList<HappyBottle> b = net.download(Task.GETMINE);
		addAvoidDupes(b);
	}

	//sync all down, then add the bottles to the database, avoiding duplicates
	private void syncAllDown(){
		ArrayList<HappyBottle> b = net.download(Task.GETALL);
		addAvoidDupes(b);
	}


	//add bottles to the database but only if they are not dupes
	private void addAvoidDupes(ArrayList<HappyBottle> a){
		for (HappyBottle b : a){
			long time = b.getTime();
			long userId = b.getUID();
			SQLiteDatabase db = h.getReadableDatabase();
			String[] columns = {_ID, MSG};
			//if there is nothing with the same uid and time as the bottle in your localdb add it to the db
			Cursor c = db.query(TABLE_NAME, columns, UID+"=\'"+userId+"\' AND "+TIME+"="+time, null, null, null, null);
			if (c.getCount() == 0){

				Log.d(TAG, "adding bottle fromthe internets!");
				this.addBottle(b, true); //was true
			}
			c.close();
			db.close();
		}
	}

	/**
	 * This method creates an ArrayList of HappyBottles that id == MyUserID form the local database
	 * @return the ArrayList of HappyBottles
	 */
	public ArrayList<HappyBottle> getMyHistory(){
		SQLiteDatabase db = h.getReadableDatabase();
		Cursor cursor = getCursor(db);
		ArrayList<HappyBottle> a = new ArrayList<HappyBottle>();
		//call the database 
		while (cursor.moveToNext() ){
			long id = cursor.getLong(1);
			if (id == myID) {
				//for each entry in the database, where the user id == MyUserID
				HappyBottle b = createBottle(cursor);
				a.add(b);
			}
		}
		cursor.close();
		db.close();
		return a;
	}

	/**
	 * This method creates an ArrayList of HappyBottles to create AllHistory from the local database
	 * @return an ArrayList of HappyBottles
	 */
	public ArrayList<HappyBottle> getAllHistory(){
		//see getMyHistory. Same idea.
		SQLiteDatabase db = h.getReadableDatabase();
		Cursor cursor = getCursor(db);
		ArrayList<HappyBottle> a = new ArrayList<HappyBottle>();
		while (cursor.moveToNext() ){
			HappyBottle b = createBottle(cursor);
			a.add(b);
		}
		cursor.close();
		db.close();
		return a;
	}

	/* 
	 * Given the cursor that is pointing to a row in the database
	 * Pull the info from that row and create a bottle object
	 */
	private HappyBottle createBottle(Cursor cursor){
		long uid = cursor.getLong(1);
		int latitude = cursor.getInt(2);
		int longitude = cursor.getInt(3);
		short emo = cursor.getShort(4);
		String msg = cursor.getString(5);
		long time = cursor.getLong(6);
		short priv;
		try{
			Log.d(TAG, "cursor = " + cursor.getString(8));
			
			priv = cursor.getShort(8);
		}
		catch(Exception e){
			priv = 0;
			Log.e(TAG, "Error in createBottle");
			//sometimes the error happens when you download from the internet. No idea why.
		}
		boolean privacy = priv>0?true:false;
		HappyBottle b = new HappyBottle(uid, latitude, longitude, emo, msg, time);
		b.setPrivacy(privacy);
		return b;
	}

	//Provides random read-write access to the result set returned by a database query.
	private Cursor getCursor(SQLiteDatabase db){
		Cursor cursor = db.query(TABLE_NAME, FROM, null, null, null,
				null, ORDER_BY);
		return cursor;
	}
	
	protected ArrayList<HappyBottle> getMyLocalRecent(int minLat, int maxLat, int minLong, int maxLong, int limit){
		//Bottle.where(" lat > ? and lat < ? and long > ? and long < ? ",params[:lat1],params[:lat2],params[:long1],params[:long2]).order("time DESC").limit(params[:recent])
		SQLiteDatabase db = h.getReadableDatabase();
		String[] args = {Integer.toString(minLat), Integer.toString(maxLat), Integer.toString(minLong), Integer.toString(maxLong), Long.toString(myID)};
		Cursor cursor = db.query(TABLE_NAME, null, "lat > ? and lat < ? and long > ? and long < ? and " + UID +"= ?" , args, null, null, TIME + " DESC", Integer.toString(limit));
		ArrayList<HappyBottle> a = new ArrayList<HappyBottle>();
		while (cursor.moveToNext() ){
			HappyBottle b = createBottle(cursor);
			a.add(b);
		}
		cursor.close();
		db.close();
		return a;
	}
	
	protected ArrayList<HappyBottle> getMyLocalAfter(int minLat, int maxLat, int minLong, int maxLong, int limit, long timeafter){
		SQLiteDatabase db = h.getReadableDatabase();
		String[] args = {Integer.toString(minLat), Integer.toString(maxLat), Integer.toString(minLong), Integer.toString(maxLong), Long.toString(timeafter), Long.toString(myID)};
		Cursor cursor = db.query(TABLE_NAME, null, "lat > ? and lat < ? and long > ? and long < ? and time > ? and " + UID +"= ?" , args, null, null, TIME + " ASC", Integer.toString(limit));
		ArrayList<HappyBottle> a = new ArrayList<HappyBottle>();
		while (cursor.moveToNext() ){
			HappyBottle b = createBottle(cursor);
			a.add(b);
		}
		cursor.close();
		db.close();
		return a;
	}
	
	protected ArrayList<HappyBottle> getMyLocalBefore(int minLat, int maxLat, int minLong, int maxLong, int limit, long timebefore){
		String[] args = {Integer.toString(minLat), Integer.toString(maxLat), Integer.toString(minLong), Integer.toString(maxLong), Long.toString(timebefore), Long.toString(myID)};
		SQLiteDatabase db = h.getReadableDatabase();
		ArrayList<HappyBottle> a = new ArrayList<HappyBottle>();
		
		try{
			Cursor cursor = db.query(TABLE_NAME, null, "lat > ? and lat < ? and long > ? and long < ? and time < ? and " + UID +"= ?" , args, null, null, TIME + " DESC", Integer.toString(limit));
			
			while (cursor.moveToNext() ){
				HappyBottle b = createBottle(cursor);
				a.add(b);
			}
			cursor.close();
			db.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}finally{
			db.close();
		}
		
		return a;
	}
	
	
	
	protected ArrayList<HappyBottle> getLocalRecent(int minLat, int maxLat, int minLong, int maxLong, int limit){
		return net.downloadLocal(minLat, maxLat, minLong, maxLong, limit);
	}
	
	protected ArrayList<HappyBottle> getLocalAfter(int minLat, int maxLat, int minLong, int maxLong, int limit, long timeafter){
		return net.downloadLocalAfter(minLat, maxLat, minLong, maxLong, limit, timeafter);
	}

	protected ArrayList<HappyBottle> getLocalBefore(int minLat, int maxLat, int minLong, int maxLong, int limit, long time){
		return net.downloadLocalBefore(minLat, maxLat, minLong, maxLong, limit, time);

	}
}

