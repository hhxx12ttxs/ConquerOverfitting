package cn.xhmao.libs.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

public class Exporter {
    private static final String TAG = Exporter.class.getSimpleName();
    private static final String TABLE_MASTER = "sqlite_master";

    public static void dump(SQLiteDatabase db, Writer writer, int types[]) throws IOException {
        ArrayList<String> tables = new ArrayList<String>();

        Cursor c = null;
        try {
            c = db.rawQuery("SELECT * FROM " + TABLE_MASTER, null);
            int columnCount = c.getColumnCount();
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                String name = c.getString(c.getColumnIndex("name"));
                if ("android_metadata".equals(name)) {
                    continue;
                }
                LogUtils.d(TAG, "dump table: " + name);
                tables.add(name);

                for (int i = 0; i < columnCount; i++) {
                    LogUtils.d(TAG, c.getColumnName(i) + ": " + c.getString(i));
                }
            }
        } catch (SQLiteException e) {
            LogUtils.e(TAG, "Failed to query table list: " + e);
            throw e;
        } finally {
            if (c != null) {
                c.close();
            }
        }

        writer.write("# version: " + db.getVersion() + "\n");

        for (String table : tables) {
            try {
                dump(db, table, writer, types);
            } catch (IOException e) {
                LogUtils.e(TAG, "Failed to dump table(" + table + "):" + e);
            }
        }
    }

    public static void dump(SQLiteDatabase db, String tableName,
                            Writer writer, int types[]) throws IOException {
        Cursor c = null;
        try {
            c = db.query(TABLE_MASTER, new String[]{"sql"}, "name=?", new String[]{tableName}, null, null, null);
            if (c.moveToFirst()) {
                // writer.write("DROP TABLE " + tableName + ";\n");
                writer.write(c.getString(0));
                writer.write(";\n");

                dumpRows(db, tableName, writer, types);
            } else {
                LogUtils.e(TAG, "no such table: " + tableName);
            }
        } catch (SQLiteException e) {
            LogUtils.e(TAG, "Failed to dump table(" + tableName + "): " + e);
            throw e;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private static void dumpRows(SQLiteDatabase db, String tableName,
                                 Writer writer, int types[]) throws IOException {
        Cursor c = null;
        try {
            c = db.rawQuery("SELECT * FROM " + tableName, null);
            int columnCount = c.getColumnCount();
            if (columnCount > 0) {
                dumpRows(tableName, writer, c, columnCount, types);
            }
        } catch (SQLiteException e) {
            LogUtils.e(TAG, "Failed to dump rows of table(" + tableName + "): " + e);
            throw e;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private static void dumpRows(String tableName, Writer writer, Cursor c,
                                 int columnCount, int types[]) throws IOException {
        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append("INSERT INTO '" + tableName + "' (");
        for (int i = 0; i < columnCount; i++) {
            if (i > 0) {
                headerBuilder.append(", ");
            }
            headerBuilder.append('\'');
            headerBuilder.append(c.getColumnName(i));
            headerBuilder.append('\'');
        }
        headerBuilder.append(") VALUES (");
        String insertHeader = headerBuilder.toString();

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            if (types == null) {
                types = new int[columnCount];
                for (int i = 0; i < columnCount; i++) {
//                    types[i] = c.getType(i);
                    LogUtils.d(TAG, "type[" + i + "]=" + types[i]);
                }
            }
            writer.write(insertHeader);

            for (int i = 0; i < columnCount; i++) {
                if (i > 0) {
                    writer.write(", ");
                }
                dumpColumn(writer, c, i, types[i]);
            }

            writer.write(");\n");
        }
    }

    private static void dumpColumn(Writer writer, Cursor c, int col, int type) throws IOException {
        switch (type) {
            case /*Cursor.FIELD_TYPE_BLOB*/ 4:
                writer.write(dumpBlob(c.getBlob(col)));
                break;
            case /*Cursor.FIELD_TYPE_FLOAT*/ 2:
                writer.write(String.valueOf(c.getFloat(col)));
                break;
            case /*Cursor.FIELD_TYPE_INTEGER*/ 1:
                writer.write(String.valueOf(c.getLong(col)));
                break;
            case /*Cursor.FIELD_TYPE_STRING*/ 3:
            default:
                writer.write("'" + c.getString(col) + "'");
                break;
        }
    }

    private static String dumpBlob(byte[] blob) {
        StringBuilder builder = new StringBuilder(blob.length * 2 + 3);
        builder.append("X'");
        for (byte b : blob) {
            builder.append(String.format("%02X", b & 0xff));
        }
        builder.append('\'');
        return builder.toString();
    }
}

