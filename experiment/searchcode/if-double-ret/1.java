package com.babytree.apps.comm.util;

import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public final class SharedPreferencesUtil {

	private SharedPreferencesUtil() {
	}

	private static final int DB_VERSION = 1;
	private static final String DB_NAME = "setting.db";
	private static final String TABLE_NAME = "setting_info";

	private static MySqlLiteHelper sqlLiteHelper = null;
	private static SQLiteDatabase db = null;

	private static class MySqlLiteHelper extends SQLiteOpenHelper {

		public MySqlLiteHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("create table if not exists " + TABLE_NAME + "("
					+ "key text primary key," + "value text," + "text1 text,"
					+ "text2 text," + "text3 text," + "text4 text,"
					+ "text5 text)");

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}

	}

	public static String getStringValue(Context context, String key) {
		return getStringValue(context, key, null);
	}

	public static int getIntValue(Context context, String key) {
		return getIntValue(context, key, -1);
	}

	public static float getFlaotValue(Context context, String key) {
		return getFloatValue(context, key, -1);
	}

	public static boolean getBooleanValue(Context context, String key) {
		return getBooleanValue(context, key, false);
	}

	public static long getLongValue(Context context, String key) {
		return getLongValue(context, key, (long) -1);
	}

	private static synchronized void setValuePrivate(Context context,
			String key, Object value) {
		if (sqlLiteHelper == null) {
			sqlLiteHelper = new MySqlLiteHelper(
					context.getApplicationContext(), DB_NAME, null, DB_VERSION);
			db = sqlLiteHelper.getWritableDatabase();
		}

		try {
			String sql = "select count(*) from " + TABLE_NAME
					+ " where key = ?";
			if (value == null) {
				sql = "delete from " + TABLE_NAME + " where key = ?";
				db.execSQL(sql, new Object[] { key });
			} else {
				Cursor cursor = db.rawQuery(sql, new String[] { key });
				int count = 0;
				if (cursor.moveToFirst()) {
					count = cursor.getInt(0);
				}
				cursor.close();
				if (count != 0) {
					sql = "update " + TABLE_NAME + " set value=? where key=?";
					db.execSQL(sql, new Object[] { value, key });
				} else {
					sql = "insert into " + TABLE_NAME
							+ " (key, value) values (?,?)";
					db.execSQL(sql, new Object[] { key, value });
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private static synchronized void removeValuePrivate(Context context,
			String... key) {
		if (sqlLiteHelper == null) {
			sqlLiteHelper = new MySqlLiteHelper(
					context.getApplicationContext(), DB_NAME, null, DB_VERSION);
			db = sqlLiteHelper.getWritableDatabase();
		}

		try {
			String keyTmp = "";
			for (String string : key) {
				keyTmp += "'" + string + "',";
			}
			if (keyTmp != "") {
				keyTmp = keyTmp.substring(0, keyTmp.length() - 1);
			}
			String sql = "delete from " + TABLE_NAME + " where key in("
					+ keyTmp + ")";
			db.execSQL(sql);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * 是否为正确内容(暂时不用)
	 * 
	 * @author wangshuaibo
	 * @param obj
	 * @return
	 */
	private static boolean isReal(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj.toString().equalsIgnoreCase("null")
				|| obj.toString().equalsIgnoreCase("false")) {
			return false;
		}
		return true;
	}

	private static Object getValue(Context context, String key, Class<?> clazz,
			Object defValue) {
		Object ret = getValue(context, key, clazz);

		if (clazz == Integer.class) {
			if ((Integer) ret == -1) {
				ret = defValue;
			} else if (!isReal(ret)) {
				ret = -1;
			}
		} else if (clazz == Long.class) {
			if ((Long) ret == -1L) {
				ret = defValue;
			} else if (!isReal(ret)) {
				ret = -1;
			}
		} else if (clazz == Double.class) {
			if ((Double) ret == -1D) {
				ret = defValue;
			} else if (!isReal(ret)) {
				ret = -1;
			}
		} else if (clazz == Float.class) {
			if ((Float) ret == -1F) {
				ret = defValue;
			} else if (!isReal(ret)) {
				ret = -1;
			}
		} else if (clazz == String.class) {
			if (ret == null) {
				ret = defValue;
			}
		} else if (clazz == Boolean.class) {
			if (ret == null) {
				ret = defValue;
			}
		}

		return ret;
	}

	private static Object getValue(Context context, String key, Class<?> clazz) {
		if (sqlLiteHelper == null) {
			sqlLiteHelper = new MySqlLiteHelper(context, DB_NAME, null,
					DB_VERSION);
			db = sqlLiteHelper.getWritableDatabase();
		}
		Object ret = null;
		if (clazz == Integer.class) {
			ret = -1;
		} else if (clazz == Long.class) {
			ret = -1L;
		} else if (clazz == Double.class) {
			ret = -1D;
		} else if (clazz == Float.class) {
			ret = -1F;
		} else if (clazz == String.class) {
			ret = null;
		}

		String sql = "select value from " + TABLE_NAME + " where key=?";
		try {
			Cursor cursor = db.rawQuery(sql, new String[] { key });
			if (cursor.getCount() > 0 && cursor.moveToFirst()) {
				if (clazz == Integer.class) {
					ret = cursor.getInt(0);
				} else if (clazz == Float.class) {
					ret = cursor.getFloat(0);
				} else if (clazz == Long.class) {
					ret = cursor.getLong(0);
				} else if (clazz == Double.class) {
					ret = cursor.getDouble(0);
				} else if (clazz == Boolean.class) {
					ret = cursor.getString(0).equals("no") ? false : true;
				} else if (clazz == String.class) {
					ret = cursor.getString(0);
				}
			}
			cursor.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}

	public static long getLongValue(Context context, String key, Long defValue) {
		return (Long) getValue(context, key, Long.class, defValue);
	}

	public static String getStringValue(Context context, String key,
			String defValue) {
		return (String) getValue(context, key, String.class, defValue);
	}

	public static int getIntValue(Context context, String key, int defValue) {
		return (Integer) getValue(context, key, Integer.class, defValue);
	}

	public static float getFloatValue(Context context, String key,
			float defValue) {
		return (Float) getValue(context, key, Float.class, defValue);
	}

	public static boolean getBooleanValue(Context context, String key,
			boolean defValue) {
		return (Boolean) getValue(context, key, Boolean.class, defValue);
	}

	public static void setValue(Context context, String key, String value) {
		setValuePrivate(context, key, value);
	}

	public static void setValue(Context context, String key, int value) {
		setValuePrivate(context, key, value);
	}

	public static void setValue(Context context, String key, float value) {
		setValuePrivate(context, key, value);
	}

	public static void setValue(Context context, String key, boolean value) {
		if (value == true)
			setValuePrivate(context, key, "yes");
		else
			setValuePrivate(context, key, "no");
	}

	public static void setValue(Context context, String key, long value) {
		setValuePrivate(context, key, value);
	}

	public static void removeKey(Context context, String key) {
		setValuePrivate(context, key, null);
	}

	public static void removeKeyArray(Context context, String... keyArray) {
		removeValuePrivate(context, keyArray);
	}

	/**
	 * 批量增加配置信息
	 * 
	 * @author wangshuaibo
	 * @param values
	 */
	public static void setValue(Context context, HashMap<String, Object> map) {
		if (sqlLiteHelper == null) {
			sqlLiteHelper = new MySqlLiteHelper(
					context.getApplicationContext(), DB_NAME, null, DB_VERSION);
			db = sqlLiteHelper.getWritableDatabase();
		}
		try {
			db.beginTransaction();

			for (String key : map.keySet()) {
				Object value = map.get(key);
				setValuePrivate(context, key, value);
			}

			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}
}

