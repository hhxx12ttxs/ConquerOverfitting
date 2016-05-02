package ecs160.project.locationtask;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DataSource 
{
	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;

	public DataSource(Context context) 
	{
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException 
	{
		database = dbHelper.getWritableDatabase();
	}

	public void close() 
	{
		dbHelper.close();
	}
	
	public int getTotalNumberMessages(String user)
	{
		String args[] = { user, String.valueOf(1) };
		Cursor cursor = database.query(MySQLiteHelper.COMMUNICATION_TABLE, null, MySQLiteHelper.USER_ID + " = ?" + " and " +
																	       MySQLiteHelper.TYPE + " = ?", 
																		   args, null, null, null);
		return cursor.getCount();
	}
	
	public int getTotalNumberMessagesNotRead(String user)
	{
		String args[] = { user, String.valueOf(1), String.valueOf(0) };
		Cursor cursor = database.query(MySQLiteHelper.COMMUNICATION_TABLE, null, MySQLiteHelper.USER_ID + " = ?" + " and " +
																	       MySQLiteHelper.TYPE + " = ?" + " and " + 
																	       MySQLiteHelper.HAS_READ + " = ?", 
																		   args, null, null, null);
		
		return cursor.getCount();
	}
	
	public int getTotalNumberTasks(String user)
	{
		String args[] = { user, String.valueOf(2) };
		Cursor cursor = database.query(MySQLiteHelper.COMMUNICATION_TABLE, null, MySQLiteHelper.USER_ID + " = ?" + " and " +
																	       MySQLiteHelper.TYPE + " = ?", 
																		   args, null, null, null);
		return cursor.getCount();
	}
	
	public int getTotalNumberTasksActive(String user)
	{
		String args[] = { user, String.valueOf(2),String.valueOf(1) };
		Cursor cursor = database.query(MySQLiteHelper.COMMUNICATION_TABLE, null, MySQLiteHelper.USER_ID + " = ?" + " and " +
																			MySQLiteHelper.TYPE + " = ?" + " and " + 
																		    MySQLiteHelper.COMMUNICATION_ACTIVE + " = ?", 
																			args, null, null, null);
		return cursor.getCount();
	}
	
	public int getTotalNumberQueries(String user)
	{
		String args[] = { user, String.valueOf(3) };
		Cursor cursor = database.query(MySQLiteHelper.COMMUNICATION_TABLE, null, MySQLiteHelper.USER_ID + " = ?" + " and " +
																	       MySQLiteHelper.TYPE + " = ?", 
																		   args, null, null, null);
		return cursor.getCount();
	}
	
	public int getTotalNumberQueriesActive(String user)
	{
		String args[] = { user, String.valueOf(3),String.valueOf(1) };
		Cursor cursor = database.query(MySQLiteHelper.COMMUNICATION_TABLE, null, MySQLiteHelper.USER_ID + " = ?" + " and " +
																			MySQLiteHelper.TYPE + " = ?" + " and " + 
																		    MySQLiteHelper.COMMUNICATION_ACTIVE + " = ?", 
																			args, null, null, null);
		return cursor.getCount();
	}
	
	public int getTotalNumberGeoMsg(String user)
	{
		String args[] = { user, String.valueOf(4) };
		Cursor cursor = database.query(MySQLiteHelper.COMMUNICATION_TABLE, null, MySQLiteHelper.USER_ID + " = ?" + " and " +
																	       MySQLiteHelper.TYPE + " = ?", 
																		   args, null, null, null);
		return cursor.getCount();
	}
	
	public int getNumberMessagesAtLocation(String user, Location loc)
	{
		String args[] = { user, String.valueOf(1), String.valueOf(loc.getLatitude()), String.valueOf(loc.getLongitude()) };
		Cursor cursor = database.query(MySQLiteHelper.COMMUNICATION_TABLE, null, MySQLiteHelper.USER_ID + " = ?" + " and " +
																	       MySQLiteHelper.TYPE + " = ?" + " and " +
																		   MySQLiteHelper.LATITUDE + " = ?" + " and " +
																		   MySQLiteHelper.LONGITUDE	+ " = ?", 
																		   args, null, null, null);
		return cursor.getCount();
	}
	
	public int getNumberTasksAtLocation(String user, Location loc)
	{
		String args[] = { user, String.valueOf(2), String.valueOf(loc.getLatitude()), String.valueOf(loc.getLongitude()) };
		Cursor cursor = database.query(MySQLiteHelper.COMMUNICATION_TABLE, null, MySQLiteHelper.USER_ID + " = ?" + " and " +
																	       MySQLiteHelper.TYPE + " = ?" + " and " +
																		   MySQLiteHelper.LATITUDE + " = ?" + " and " +
																		   MySQLiteHelper.LONGITUDE	+ " = ?", 
																		   args, null, null, null);
		return cursor.getCount();
	}
	
	public int getNumberQueriesAtLocation(String user, Location loc)
	{
		String args[] = { user, String.valueOf(3), String.valueOf(loc.getLatitude()), String.valueOf(loc.getLongitude()) };
		Cursor cursor = database.query(MySQLiteHelper.COMMUNICATION_TABLE, null, MySQLiteHelper.USER_ID + " = ?" + " and " +
																	       MySQLiteHelper.TYPE + " = ?" + " and " +
																		   MySQLiteHelper.LATITUDE + " = ?" + " and " +
																		   MySQLiteHelper.LONGITUDE	+ " = ?", 
																		   args, null, null, null);
		return cursor.getCount();
	}
	
	public int getNumberGeoMsgAtLocation(String user, Location loc)
	{
		String args[] = { user, String.valueOf(4), String.valueOf(loc.getLatitude()), String.valueOf(loc.getLongitude()) };
		Cursor cursor = database.query(MySQLiteHelper.COMMUNICATION_TABLE, null, MySQLiteHelper.USER_ID + " = ?" + " and " +
																	       MySQLiteHelper.TYPE + " = ?" + " and " +
																		   MySQLiteHelper.LATITUDE + " = ?" + " and " +
																		   MySQLiteHelper.LONGITUDE	+ " = ?", 
																		   args, null, null, null);
		return cursor.getCount();
	}
	
	public void updateMessage(String id, Message m) 
	{		
		String text = m.getTextData();
		double lo = m.getLocation().getLongitude();
		double la = m.getLocation().getLatitude();
		boolean b = m.getMessageRead();
		String args[] = { text, String.valueOf(lo), String.valueOf(la), id, m.getSender(), String.valueOf(1) };
		ContentValues cv = new ContentValues();
        cv.put(MySQLiteHelper.HAS_READ, b);    
        database.update(MySQLiteHelper.COMMUNICATION_TABLE, cv, MySQLiteHelper.MESSAGE + "=? and " +
        														MySQLiteHelper.LONGITUDE + "=? and " + 
        														MySQLiteHelper.LATITUDE + "=? and " + 
        														MySQLiteHelper.USER_ID + "=? and " +
        														MySQLiteHelper.SENDER_ID + "=? and " +
        														MySQLiteHelper.TYPE + "=?", args);
    }
	
	public void updateTask(String user, Task t) 
	{		
		String text = t.getTextData();
		double lo = t.getLocation().getLongitude();
		double la = t.getLocation().getLatitude();
		boolean active = t.getTaskActive();
		int sYr = t.getTaskBegin().getYear();
		int sMonth = t.getTaskBegin().getMonth();
		int sD = t.getTaskBegin().getDay();
		int eYr = t.getTaskEnd().getYear();
		int eMonth = t.getTaskBegin().getMonth();
		int eD = t.getTaskBegin().getDay();
		int sHr = t.getTaskBegin().getHour();
		int sMin = t.getTaskBegin().getMinute();
		int sSec = t.getTaskBegin().getSecond();
		int eHr = t.getTaskEnd().getHour();
		int eMin = t.getTaskEnd().getMinute();
		int eSec = t.getTaskEnd().getSecond();
		String args[] = { text, String.valueOf(lo), String.valueOf(la), user, t.getSender(), String.valueOf(1)
				          ,String.valueOf(sYr), String.valueOf(sMonth),String.valueOf(sD),String.valueOf(sHr),String.valueOf(sMin),String.valueOf(sSec)
				          ,String.valueOf(eYr), String.valueOf(eMonth),String.valueOf(eD),String.valueOf(eHr),String.valueOf(eMin),String.valueOf(eSec)};
		ContentValues cv = new ContentValues();
        cv.put(MySQLiteHelper.COMMUNICATION_ACTIVE, active);    
        database.update(MySQLiteHelper.COMMUNICATION_TABLE, cv, MySQLiteHelper.MESSAGE + "=? and " +
        														MySQLiteHelper.LONGITUDE + "=? and " + 
        														MySQLiteHelper.LATITUDE + "=? and " + 
        														MySQLiteHelper.USER_ID + "=? and " +
        														MySQLiteHelper.SENDER_ID + "=? and " +
        														MySQLiteHelper.TYPE + "=? and " +
        														MySQLiteHelper.BEGIN_YEAR + "=? and " +
        														MySQLiteHelper.BEGIN_MON + "=? and " +
        														MySQLiteHelper.BEGIN_DAY + "=? and " +
        														MySQLiteHelper.BEGIN_HOUR + "=? and " +
        														MySQLiteHelper.BEGIN_MIN + "=? and " +
        														MySQLiteHelper.BEGIN_SEC + "=? and " +
        														MySQLiteHelper.END_YEAR + "=? and " +
        														MySQLiteHelper.END_MON + "=? and " +
        														MySQLiteHelper.END_DAY + "=? and " +
        														MySQLiteHelper.END_HOUR + "=? and " +
        														MySQLiteHelper.END_MIN + "=? and " +
        														MySQLiteHelper.END_SEC + "=?"
        														, args);
    }
	
	public void updateQuery(String user, Query q) 
	{		
		String text = q.getTextData();
		double lo = q.getLocation().getLongitude();
		double la = q.getLocation().getLatitude();
		boolean active = q.getQueryActive();
		int sYr = q.getQueryBegin().getYear();
		int sMonth = q.getQueryBegin().getMonth();
		int sD = q.getQueryBegin().getDay();
		int eYr = q.getQueryEnd().getYear();
		int eMonth = q.getQueryEnd().getMonth();
		int eD = q.getQueryEnd().getDay();
		int sHr = q.getQueryBegin().getHour();
		int sMin = q.getQueryBegin().getMinute();
		int sSec = q.getQueryBegin().getSecond();
		int eHr = q.getQueryEnd().getHour();
		int eMin = q.getQueryEnd().getMinute();
		int eSec = q.getQueryEnd().getSecond();
		String args[] = { text, String.valueOf(lo), String.valueOf(la), user, q.getSender(), String.valueOf(1)
				          ,String.valueOf(sYr), String.valueOf(sMonth),String.valueOf(sD),String.valueOf(sHr),String.valueOf(sMin),String.valueOf(sSec)
				          ,String.valueOf(eYr), String.valueOf(eMonth),String.valueOf(eD),String.valueOf(eHr),String.valueOf(eMin),String.valueOf(eSec)};
		ContentValues cv = new ContentValues();
        cv.put(MySQLiteHelper.COMMUNICATION_ACTIVE, active);    
        database.update(MySQLiteHelper.COMMUNICATION_TABLE, cv, MySQLiteHelper.MESSAGE + "=? and " +
        														MySQLiteHelper.LONGITUDE + "=? and " + 
        														MySQLiteHelper.LATITUDE + "=? and " + 
        														MySQLiteHelper.USER_ID + "=? and " +
        														MySQLiteHelper.SENDER_ID + "=? and " +
        														MySQLiteHelper.TYPE + "=? and " +
        														MySQLiteHelper.BEGIN_YEAR + "=? and " +
        														MySQLiteHelper.BEGIN_MON + "=? and " +
        														MySQLiteHelper.BEGIN_DAY + "=? and " +
        														MySQLiteHelper.BEGIN_HOUR + "=? and " +
        														MySQLiteHelper.BEGIN_MIN + "=? and " +
        														MySQLiteHelper.BEGIN_SEC + "=? and " +
        														MySQLiteHelper.END_YEAR + "=? and " +
        														MySQLiteHelper.END_MON + "=? and " +
        														MySQLiteHelper.END_DAY + "=? and " +
        														MySQLiteHelper.END_HOUR + "=? and " +
        														MySQLiteHelper.END_MIN + "=? and " +
        														MySQLiteHelper.END_SEC + "=?"
        														, args);
    }

	public void storeMessage(String user, Message message)//(String user, String msg, double lat, double lon, boolean read) 
	{ 
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.USER_ID, user);
		values.put(MySQLiteHelper.SENDER_ID, message.getSender());
		values.put(MySQLiteHelper.TYPE, 1);
		values.put(MySQLiteHelper.MESSAGE, message.getTextData());
		values.put(MySQLiteHelper.LATITUDE, message.getLocation().getLatitude());
		values.put(MySQLiteHelper.LONGITUDE, message.getLocation().getLongitude());
		values.put(MySQLiteHelper.HAS_READ, message.getMessageRead());
	    database.insert(MySQLiteHelper.COMMUNICATION_TABLE, null, values);
	}
	
	public void storeTask(String user, Task task)//(String user, String msg, double lat, double lon, boolean read) 
	{ 
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.USER_ID, user);
		values.put(MySQLiteHelper.SENDER_ID, task.getSender());
		values.put(MySQLiteHelper.TYPE, 2);
		values.put(MySQLiteHelper.MESSAGE, task.getTextData());
		values.put(MySQLiteHelper.LATITUDE, task.getLocation().getLatitude());
		values.put(MySQLiteHelper.LONGITUDE, task.getLocation().getLongitude());
		
		values.put(MySQLiteHelper.COMMUNICATION_ACTIVE, task.getTaskActive());
		values.put(MySQLiteHelper.BEGIN_YEAR, task.getTaskBegin().getYear());
		values.put(MySQLiteHelper.BEGIN_MON, task.getTaskBegin().getMonth());
		values.put(MySQLiteHelper.BEGIN_DAY, task.getTaskBegin().getDay());
		values.put(MySQLiteHelper.BEGIN_HOUR, task.getTaskBegin().getHour());
		values.put(MySQLiteHelper.BEGIN_MIN, task.getTaskBegin().getMinute());
		values.put(MySQLiteHelper.BEGIN_SEC, task.getTaskBegin().getSecond());
		values.put(MySQLiteHelper.END_YEAR, task.getTaskEnd().getYear());
		values.put(MySQLiteHelper.END_MON, task.getTaskEnd().getMonth());
		values.put(MySQLiteHelper.END_DAY, task.getTaskEnd().getDay());
		values.put(MySQLiteHelper.END_HOUR, task.getTaskEnd().getHour());
		values.put(MySQLiteHelper.END_MIN, task.getTaskEnd().getMinute());
		values.put(MySQLiteHelper.END_SEC, task.getTaskEnd().getSecond());
		
		database.insert(MySQLiteHelper.COMMUNICATION_TABLE, null, values);
	}
	
	public void storeQuery(String user, Query query) 
	{ 
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.USER_ID, user);
		values.put(MySQLiteHelper.SENDER_ID, query.getSender());
		values.put(MySQLiteHelper.TYPE, 3);
		values.put(MySQLiteHelper.MESSAGE, query.getTextData());
		values.put(MySQLiteHelper.LATITUDE, query.getLocation().getLatitude());
		values.put(MySQLiteHelper.LONGITUDE, query.getLocation().getLongitude());
		
		values.put(MySQLiteHelper.COMMUNICATION_ACTIVE, query.getQueryActive());
		values.put(MySQLiteHelper.QUERY_REPLY, query.getQueryReply());
		values.put(MySQLiteHelper.QUERY_REPLIED, query.getQueryReplied());
		values.put(MySQLiteHelper.BEGIN_YEAR, query.getQueryBegin().getYear());
		values.put(MySQLiteHelper.BEGIN_MON, query.getQueryBegin().getMonth());
		values.put(MySQLiteHelper.BEGIN_DAY, query.getQueryBegin().getDay());
		values.put(MySQLiteHelper.BEGIN_HOUR, query.getQueryBegin().getHour());
		values.put(MySQLiteHelper.BEGIN_MIN, query.getQueryBegin().getMinute());
		values.put(MySQLiteHelper.BEGIN_SEC, query.getQueryBegin().getSecond());
		values.put(MySQLiteHelper.END_YEAR, query.getQueryEnd().getYear());
		values.put(MySQLiteHelper.END_MON, query.getQueryEnd().getMonth());
		values.put(MySQLiteHelper.END_DAY, query.getQueryEnd().getDay());
		values.put(MySQLiteHelper.END_HOUR, query.getQueryEnd().getHour());
		values.put(MySQLiteHelper.END_MIN, query.getQueryEnd().getMinute());
		values.put(MySQLiteHelper.END_SEC, query.getQueryEnd().getSecond());
		
		database.insert(MySQLiteHelper.COMMUNICATION_TABLE, null, values);
	}

	public void storeGeoMsg(String user, GeoMessage geomessage)//(String user, String msg, double lat, double lon, boolean read) 
	{ 
		Log.i("saveGEO", "In storeGeoMsg");
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.USER_ID, user);
		values.put(MySQLiteHelper.SENDER_ID, geomessage.getSender());
		values.put(MySQLiteHelper.TYPE, 4);
		values.put(MySQLiteHelper.MESSAGE, geomessage.getTextdata());
		values.put(MySQLiteHelper.LATITUDE, geomessage.getGeoLocation().getLatitude());
		values.put(MySQLiteHelper.LONGITUDE, geomessage.getGeoLocation().getLongitude());
		values.put(MySQLiteHelper.IMAGE_URL, geomessage.getPathURL());
		database.insert(MySQLiteHelper.COMMUNICATION_TABLE, null, values);
		Log.i("saveGEO", "After storeGeoMsg");
	}
	
	public ArrayList<Message> getAllMessagesAtLocation(String user, Location loc)
	{
		ArrayList<Message> messages = new ArrayList<Message>();
	
		String args[] = { user, String.valueOf(1), String.valueOf(loc.getLatitude()), String.valueOf(loc.getLongitude()) };
		Cursor cursor = database.query(MySQLiteHelper.COMMUNICATION_TABLE, null, MySQLiteHelper.USER_ID + " = ?" + " and " +
																	       MySQLiteHelper.TYPE + " = ?" + " and " +
																		   MySQLiteHelper.LATITUDE + " = ?" + " and " +
																		   MySQLiteHelper.LONGITUDE	+ " = ?", 
																		   args, null, null, null);
		cursor.moveToFirst(); //moves cursor to the first row
		while(!cursor.isAfterLast())
		{
			messages.add(cursorToMessage(cursor)); //convert each query row to a message and store it in the list
			cursor.moveToNext(); //advance the cursor to the next row
		}
		cursor.close();
		return messages;
	}
	
	public ArrayList<Message> getAllMessages(String user)
	{
		ArrayList<Message> messages = new ArrayList<Message>();
	
		String args[] = { user, String.valueOf(1)};
		Cursor cursor = database.query(MySQLiteHelper.COMMUNICATION_TABLE, null, MySQLiteHelper.USER_ID + " = ?" + " and " +
																	       MySQLiteHelper.TYPE + " = ?" , 
																		   args, null, null, null);
		cursor.moveToFirst(); //moves cursor to the first row
		while(!cursor.isAfterLast())
		{
			messages.add(cursorToMessage(cursor)); //convert each query row to a message and store it in the list
			cursor.moveToNext(); //advance the cursor to the next row
		}
		cursor.close();
		return messages;
	}
	
	public ArrayList<Message> getAllMessagesNotRead(String user)
	{
		ArrayList<Message> messages = new ArrayList<Message>();
	
		String args[] = { user, String.valueOf(1),String.valueOf(0)};
		Cursor cursor = database.query(MySQLiteHelper.COMMUNICATION_TABLE, null, MySQLiteHelper.USER_ID + " = ?" + " and " +
																	       MySQLiteHelper.TYPE + " = ?" + " and " +
																	       MySQLiteHelper.HAS_READ + " = ?",
																		   args, null, null, null);
		cursor.moveToFirst(); //moves cursor to the first row
		while(!cursor.isAfterLast())
		{
			messages.add(cursorToMessage(cursor)); //convert each query row to a message and store it in the list
			cursor.moveToNext(); //advance the cursor to the next row
		}
		cursor.close();
		return messages;
	}
	
	public ArrayList<Task> getAllTasksAtLocation(String user, Location loc)
	{
		ArrayList<Task> tasks = new ArrayList<Task>();
	
		String args[] = { user, String.valueOf(2), String.valueOf(loc.getLatitude()), String.valueOf(loc.getLongitude()) };
		Cursor cursor = database.query(MySQLiteHelper.COMMUNICATION_TABLE, null, MySQLiteHelper.USER_ID + " = ?" + " and " +
																	       MySQLiteHelper.TYPE + " = ?" + " and " +
																		   MySQLiteHelper.LATITUDE + " = ?" + " and " +
																		   MySQLiteHelper.LONGITUDE	+ " = ?", 
																		   args, null, null, null);
		cursor.moveToFirst(); //moves cursor to the first row
		while(!cursor.isAfterLast())
		{
			tasks.add(cursorToTask(cursor)); //convert each query row to a message and store it in the list
			cursor.moveToNext(); //advance the cursor to the next row
		}
		cursor.close();
		return tasks;
	}
	
	public ArrayList<Task> getAllTasks(String user)
	{
		ArrayList<Task> tasks = new ArrayList<Task>();
	
		String args[] = { user, String.valueOf(2)};
		Cursor cursor = database.query(MySQLiteHelper.COMMUNICATION_TABLE, null, MySQLiteHelper.USER_ID + " = ?" + " and " +
																	       MySQLiteHelper.TYPE + " = ?", 
																		   args, null, null, null);
		cursor.moveToFirst(); //moves cursor to the first row
		while(!cursor.isAfterLast())
		{
			tasks.add(cursorToTask(cursor)); //convert each query row to a message and store it in the list
			cursor.moveToNext(); //advance the cursor to the next row
		}
		cursor.close();
		return tasks;
	}
	
	public ArrayList<Task> getAllTasksActive(String user)
	{
		ArrayList<Task> tasks = new ArrayList<Task>();
	
		String args[] = { user, String.valueOf(2),String.valueOf(1)};
		Cursor cursor = database.query(MySQLiteHelper.COMMUNICATION_TABLE, null, MySQLiteHelper.USER_ID + " = ?" + " and " +
																				 MySQLiteHelper.TYPE + " = ?"+ " and " +
																				 MySQLiteHelper.COMMUNICATION_ACTIVE + " = ?", 
																		   args, null, null, null);
		cursor.moveToFirst(); //moves cursor to the first row
		while(!cursor.isAfterLast())
		{
			tasks.add(cursorToTask(cursor)); //convert each query row to a message and store it in the list
			cursor.moveToNext(); //advance the cursor to the next row
		}
		cursor.close();
		return tasks;
	}

	public ArrayList<Query> getAllQueriesAtLocation(String user, Location loc)
	{
		ArrayList<Query> queries = new ArrayList<Query>();
	
		String args[] = { user, String.valueOf(3), String.valueOf(loc.getLatitude()), String.valueOf(loc.getLongitude()) };
		Cursor cursor = database.query(MySQLiteHelper.COMMUNICATION_TABLE, null, MySQLiteHelper.USER_ID + " = ?" + " and " +
																	       MySQLiteHelper.TYPE + " = ?" + " and " +
																		   MySQLiteHelper.LATITUDE + " = ?" + " and " +
																		   MySQLiteHelper.LONGITUDE	+ " = ?", 
																		   args, null, null, null);
		cursor.moveToFirst(); //moves cursor to the first row
		while(!cursor.isAfterLast())
		{
			queries.add(cursorToQuery(cursor)); //convert each query row to a message and store it in the list
			cursor.moveToNext(); //advance the cursor to the next row
		}
		cursor.close();
		return queries;
	}
	
	public ArrayList<Query> getAllQueries(String user)
	{
		ArrayList<Query> queries = new ArrayList<Query>();
	
		String args[] = { user, String.valueOf(3)};
		Cursor cursor = database.query(MySQLiteHelper.COMMUNICATION_TABLE, null, MySQLiteHelper.USER_ID + " = ?" + " and " +
																	       MySQLiteHelper.TYPE + " = ?", 
																		   args, null, null, null);
		cursor.moveToFirst(); //moves cursor to the first row
		while(!cursor.isAfterLast())
		{
			queries.add(cursorToQuery(cursor)); //convert each query row to a message and store it in the list
			cursor.moveToNext(); //advance the cursor to the next row
		}
		cursor.close();
		return queries;
	}
	
	public ArrayList<Query> getAllQueriesActive(String user)
	{
		ArrayList<Query> queries = new ArrayList<Query>();
	
		String args[] = { user, String.valueOf(3),String.valueOf(1)};
		Cursor cursor = database.query(MySQLiteHelper.COMMUNICATION_TABLE, null, MySQLiteHelper.USER_ID + " = ?" + " and " +
																				 MySQLiteHelper.TYPE + " = ?"+ " and " +
																				 MySQLiteHelper.COMMUNICATION_ACTIVE + " = ?",
																				 args, null, null, null);
		cursor.moveToFirst(); //moves cursor to the first row
		while(!cursor.isAfterLast())
		{
			queries.add(cursorToQuery(cursor)); //convert each query row to a message and store it in the list
			cursor.moveToNext(); //advance the cursor to the next row
		}
		cursor.close();
		return queries;
	}
	
	public ArrayList<GeoMessage> getAllGeoMsgAtLocation(String user, Location loc)
	{
		ArrayList<GeoMessage> geomsg = new ArrayList<GeoMessage>();
	
		String args[] = { user, String.valueOf(4), String.valueOf(loc.getLatitude()), String.valueOf(loc.getLongitude()) };
		Cursor cursor = database.query(MySQLiteHelper.COMMUNICATION_TABLE, null, MySQLiteHelper.USER_ID + " = ?" + " and " +
																	       MySQLiteHelper.TYPE + " = ?" + " and " +
																		   MySQLiteHelper.LATITUDE + " = ?" + " and " +
																		   MySQLiteHelper.LONGITUDE	+ " = ?", 
																		   args, null, null, null);
		cursor.moveToFirst(); //moves cursor to the first row
		while(!cursor.isAfterLast())
		{
			geomsg.add(cursorToGeoMessage(cursor)); //convert each query row to a message and store it in the list
			cursor.moveToNext(); //advance the cursor to the next row
		}
		cursor.close();
		return geomsg;
	}
	
	public ArrayList<GeoMessage> getAllGeoMsg(String user)
	{
		ArrayList<GeoMessage> geomsg = new ArrayList<GeoMessage>();
	
		String args[] = { user, String.valueOf(4)};
		Cursor cursor = database.query(MySQLiteHelper.COMMUNICATION_TABLE, null, MySQLiteHelper.USER_ID + " = ?" + " and " +
																	       MySQLiteHelper.TYPE + " = ?", 
																		   args, null, null, null);
		cursor.moveToFirst(); //moves cursor to the first row
		while(!cursor.isAfterLast())
		{
			geomsg.add(cursorToGeoMessage(cursor)); //convert each query row to a message and store it in the list
			cursor.moveToNext(); //advance the cursor to the next row
		}
		cursor.close();
		return geomsg;
	}
	
	public void deleteMessage(String user, Message msg) 
	{
		String args[] = { user, msg.getSender(), String.valueOf(1), msg.getTextData(), String.valueOf(msg.getLocation().getLatitude()), String.valueOf(msg.getLocation().getLongitude()) };
		
		int count = database.delete(MySQLiteHelper.COMMUNICATION_TABLE, MySQLiteHelper.USER_ID + " = ? and " +
													  MySQLiteHelper.SENDER_ID + " = ? and " +
													  MySQLiteHelper.TYPE + " = ? and " +
													  MySQLiteHelper.MESSAGE + " = ?  and " +
													  MySQLiteHelper.LATITUDE + " = ? and " + 
													  MySQLiteHelper.LONGITUDE + " = ?"
													  , args);
		
		Log.w("C2DM","num of row deleted "+ count);
	}
	
	public void deleteTask(String user, Task tsk) 
	{
		String args[] = { user, tsk.getSender(), String.valueOf(2), tsk.getTextData(), 
						  String.valueOf(tsk.getLocation().getLatitude()), String.valueOf(tsk.getLocation().getLongitude()), 
						  String.valueOf(tsk.getTaskBegin().getYear()), String.valueOf(tsk.getTaskBegin().getMonth()), 
						  String.valueOf(tsk.getTaskBegin().getDay()), String.valueOf(tsk.getTaskBegin().getHour()), 
						  String.valueOf(tsk.getTaskBegin().getMinute()), String.valueOf(tsk.getTaskBegin().getSecond()),
						  String.valueOf(tsk.getTaskEnd().getYear()), String.valueOf(tsk.getTaskEnd().getMonth()), 
						  String.valueOf(tsk.getTaskEnd().getDay()), String.valueOf(tsk.getTaskEnd().getHour()), 
						  String.valueOf(tsk.getTaskEnd().getMinute()), String.valueOf(tsk.getTaskEnd().getSecond()) };
		
		database.delete(MySQLiteHelper.COMMUNICATION_TABLE, MySQLiteHelper.USER_ID + " = ? and " +
													  MySQLiteHelper.SENDER_ID + " = ? and " +
													  MySQLiteHelper.TYPE + " = ? and " +
													  MySQLiteHelper.MESSAGE + " = ? and " +
													  MySQLiteHelper.LATITUDE + " = ? and " + 
													  MySQLiteHelper.LONGITUDE + " = ? and " + 
													  MySQLiteHelper.BEGIN_YEAR + " = ? and " +
													  MySQLiteHelper.BEGIN_MON + " = ? and " +
													  MySQLiteHelper.BEGIN_DAY + " = ? and " +
													  MySQLiteHelper.BEGIN_HOUR + " = ? and " +
													  MySQLiteHelper.BEGIN_MIN + " = ? and " +
													  MySQLiteHelper.BEGIN_SEC + " = ? and " +
													  MySQLiteHelper.END_YEAR + " = ? and " +
													  MySQLiteHelper.END_MON + " = ? and " +
													  MySQLiteHelper.END_DAY + " = ? and " +
													  MySQLiteHelper.END_HOUR + " = ? and " +
													  MySQLiteHelper.END_MIN + " = ? and " +
													  MySQLiteHelper.END_SEC + " = ?", args);
	}
	
	public void deleteQuery(String user, Query q) 
	{
		String args[] = { user, q.getSender(), String.valueOf(3), q.getTextData(), String.valueOf(q.getLocation().getLatitude()),
				String.valueOf(q.getLocation().getLongitude()),
				
				String.valueOf(q.getQueryBegin().getYear()), String.valueOf(q.getQueryBegin().getMonth()), 
				String.valueOf(q.getQueryBegin().getDay()), String.valueOf(q.getQueryBegin().getHour()), 
				String.valueOf(q.getQueryBegin().getMinute()), String.valueOf(q.getQueryBegin().getSecond()),
				String.valueOf(q.getQueryEnd().getYear()), String.valueOf(q.getQueryEnd().getMonth()), 
				String.valueOf(q.getQueryEnd().getDay()), String.valueOf(q.getQueryEnd().getHour()), 
				String.valueOf(q.getQueryEnd().getMinute()), String.valueOf(q.getQueryEnd().getSecond())};
		
		database.delete(MySQLiteHelper.COMMUNICATION_TABLE, MySQLiteHelper.USER_ID + " = ? and " +
													  MySQLiteHelper.SENDER_ID + " = ? and " +
													  MySQLiteHelper.TYPE + " = ? and " +
													  MySQLiteHelper.MESSAGE + " = ? and " +
													  MySQLiteHelper.LATITUDE + " = ? and " + 
													  MySQLiteHelper.LONGITUDE + " = ? and " +
													  MySQLiteHelper.BEGIN_YEAR + " = ? and " +
													  MySQLiteHelper.BEGIN_MON + " = ? and " +
													  MySQLiteHelper.BEGIN_DAY + " = ? and " +
													  MySQLiteHelper.BEGIN_HOUR + " = ? and " +
													  MySQLiteHelper.BEGIN_MIN + " = ? and " +
													  MySQLiteHelper.BEGIN_SEC + " = ? and " +
													  MySQLiteHelper.END_YEAR + " = ? and " +
													  MySQLiteHelper.END_MON + " = ? and " +
													  MySQLiteHelper.END_DAY + " = ? and " +
													  MySQLiteHelper.END_HOUR + " = ? and " +
													  MySQLiteHelper.END_MIN + " = ? and " +
													  MySQLiteHelper.END_SEC + " = ?", args);
	}

	public void deleteGeoMessage(String user, GeoMessage msg) 
	{
		String args[] = { user, msg.getSender(), String.valueOf(4), msg.getTextdata(), String.valueOf(msg.getGeoLocation().getLatitude()), String.valueOf(msg.getGeoLocation().getLongitude()), msg.getPathURL() };
		
		int count = database.delete(MySQLiteHelper.COMMUNICATION_TABLE, MySQLiteHelper.USER_ID + " = ? and " +
													  MySQLiteHelper.SENDER_ID + " = ? and " +
													  MySQLiteHelper.TYPE + " = ? and " +
													  MySQLiteHelper.MESSAGE + " = ?  and " +
													  MySQLiteHelper.LATITUDE + " = ? and " + 
													  MySQLiteHelper.LONGITUDE + " = ? and " +
													  MySQLiteHelper.IMAGE_URL + " = ?"
													  , args);
		
		Log.w("C2DM","GeoMSG num of row deleted "+ count);
	}
	
	private Message cursorToMessage(Cursor cursor) 
	{
		Message message = new Message();
		message.setSender(cursor.getString(1));
		message.setTextData(cursor.getString(3));
		message.setLocation(new Location(cursor.getDouble(4), cursor.getDouble(5)));
		if(cursor.getInt(6) == 1)
			message.setMessageRead(true);
		else
			message.setMessageRead(false);
		return message;
	}
	
	private Task cursorToTask(Cursor cursor) 
	{
		Task task = new Task();
		task.setSender(cursor.getString(1));
		task.setTextData(cursor.getString(3));
		task.setLocation(new Location(cursor.getDouble(4), cursor.getDouble(5)));
		if(cursor.getInt(7) == 1)
			task.setTaskActive(true);
		else
			task.setTaskActive(false);
		task.setTaskBegin(new Time(cursor.getInt(8), 
								   cursor.getInt(9), 
								   cursor.getInt(10),
								   cursor.getInt(11),
								   cursor.getInt(12),
								   cursor.getInt(13)));
		task.setTaskEnd(new Time(cursor.getInt(14), 
							     cursor.getInt(15), 
							     cursor.getInt(16),
							     cursor.getInt(17),
							     cursor.getInt(18),
							     cursor.getInt(19)));
		return task;
	}
	
	private Query cursorToQuery(Cursor cursor) 
	{
		Query query = new Query();
		query.setSender(cursor.getString(1));
		query.setTextData(cursor.getString(3));
		query.setLocation(new Location(cursor.getDouble(4), cursor.getDouble(5)));
		if(cursor.getInt(7) == 1)
			query.setQueryActive(true);
		else
			query.setQueryActive(false);
		if(cursor.getInt(21) == 1)
			query.setQueryReplied(true);
		else
			query.setQueryReplied(false);
		query.setQueryBegin(new Time(cursor.getInt(8), 
				   					 cursor.getInt(9), 
				   					 cursor.getInt(10),
				   					 cursor.getInt(11),
				   					 cursor.getInt(12),
				   					 cursor.getInt(13)));
		query.setQueryEnd(new Time(cursor.getInt(14), 
			     				   cursor.getInt(15), 
			     				   cursor.getInt(16),
			     				   cursor.getInt(17),
			     				   cursor.getInt(18),
			     				   cursor.getInt(19)));
		query.replyToQuery(cursor.getString(20));
		return query;
	}
	
	private GeoMessage cursorToGeoMessage(Cursor cursor) 
	{
		GeoMessage geomsg = new GeoMessage();
		geomsg.setSender(cursor.getString(1));
		geomsg.setTextdata(cursor.getString(3));
		geomsg.setGeoLocation(new Location(cursor.getDouble(4), cursor.getDouble(5)));
		geomsg.setPathURL(cursor.getString(22));
		return geomsg;
	}
}
