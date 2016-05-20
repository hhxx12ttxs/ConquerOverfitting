/*
 * Gasdroid 
 * Copyright (C) 2012  Andrea Antonello (www.hydrologis.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gasdroide.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import eu.geopaparazzi.library.util.DynamicDoubleArray;
import eu.geopaparazzi.library.util.debug.Debug;
import eu.geopaparazzi.library.util.debug.Logger;

/**
 * The handler of the data.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public class DaoData {

    public static final String TABLE_DATASET = "dataset";
    public static final String TABLE_DATA = "data";

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_LON = "lon";
    private static final String COLUMN_LAT = "lat";
    private static final String COLUMN_ALTIM = "altim";
    private static final String COLUMN_P1X = "p1x";
    private static final String COLUMN_P1Y = "p1y";
    private static final String COLUMN_P2X = "p2x";
    private static final String COLUMN_P2Y = "p2y";
    private static final String COLUMN_TS = "ts";
    private static final String COLUMN_VALUE = "value";
    private static final String COLUMN_DATASET_ID = "dataset_id";
    private static final String COLUMN_TITLE = "title";

    /**
     * Adds a new dataset to the db.
     * 
     * @param context
     * @param title
     * @param lon
     * @param lat
     * @param altim
     * @param p1
     * @param p2
     * @param timeStamps
     * @param dataValues
     * @throws IOException
     */
    public static void addDataset( Context context, String title, Double lon, Double lat, Double altim, double[] p1, double[] p2,
            DynamicDoubleArray timeStamps, DynamicDoubleArray dataValues ) throws IOException {
        SQLiteDatabase sqliteDatabase = DatabaseManager.getInstance().getDatabase(context);
        sqliteDatabase.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TS, new java.util.Date().getTime());
            values.put(COLUMN_TITLE, title);
            if (lon != null)
                values.put(COLUMN_LON, lon);
            if (lat != null)
                values.put(COLUMN_LAT, lat);
            if (altim != null)
                values.put(COLUMN_ALTIM, altim);
            if (p1 != null) {
                values.put(COLUMN_P1X, p1[0]);
                values.put(COLUMN_P1Y, p1[1]);
            }
            if (p2 != null) {
                values.put(COLUMN_P2X, p2[0]);
                values.put(COLUMN_P2Y, p2[1]);
            }
            long datasetId = sqliteDatabase.insertOrThrow(TABLE_DATASET, null, values);

            double[] timeArray = timeStamps.getInternalArray();
            double[] valuesArray = dataValues.getInternalArray();
            for( int i = 0; i < timeStamps.size(); i++ ) {
                values = new ContentValues();
                values.put(COLUMN_DATASET_ID, datasetId);
                values.put(COLUMN_TS, timeArray[i]);
                values.put(COLUMN_VALUE, valuesArray[i]);
                sqliteDatabase.insertOrThrow(TABLE_DATA, null, values);
            }

            sqliteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            Logger.e("DAODATA", e.getLocalizedMessage(), e);
            throw new IOException(e.getLocalizedMessage());
        } finally {
            sqliteDatabase.endTransaction();
        }
    }

    public static void addDataset_List( Context context, String title, Double lon, Double lat, Double altim, double[] p1,
            double[] p2, List<Float> timeStamps, List<Float> dataValues ) throws IOException {
        SQLiteDatabase sqliteDatabase = DatabaseManager.getInstance().getDatabase(context);
        sqliteDatabase.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TS, new java.util.Date().getTime());
            values.put(COLUMN_TITLE, title);
            if (lon != null)
                values.put(COLUMN_LON, lon);
            if (lat != null)
                values.put(COLUMN_LAT, lat);
            if (altim != null)
                values.put(COLUMN_ALTIM, altim);
            if (p1 != null) {
                values.put(COLUMN_P1X, p1[0]);
                values.put(COLUMN_P1Y, p1[1]);
            }
            if (p2 != null) {
                values.put(COLUMN_P2X, p2[0]);
                values.put(COLUMN_P2Y, p2[1]);
            }
            long datasetId = sqliteDatabase.insertOrThrow(TABLE_DATASET, null, values);

            // double[] timeArray = timeStamps.getInternalArray();
            // double[] valuesArray = dataValues.getInternalArray();
            for( int i = 0; i < timeStamps.size(); i++ ) {
                values = new ContentValues();
                values.put(COLUMN_DATASET_ID, datasetId);
                values.put(COLUMN_TS, timeStamps.get(i));
                values.put(COLUMN_VALUE, dataValues.get(i));
                sqliteDatabase.insertOrThrow(TABLE_DATA, null, values);
            }

            sqliteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            Logger.e("DAODATA", e.getLocalizedMessage(), e);
            throw new IOException(e.getLocalizedMessage());
        } finally {
            sqliteDatabase.endTransaction();
        }
    }

    // public static void deleteDataset( Context context, long id ) throws IOException {
    // SQLiteDatabase sqliteDatabase = DatabaseManager.getInstance().getDatabase(context);
    // sqliteDatabase.beginTransaction();
    // try {
    // // delete note
    // String query = "delete from " + TABLE_NOTES + " where " + COLUMN_ID + " = " + id;
    // SQLiteStatement sqlUpdate = sqliteDatabase.compileStatement(query);
    // sqlUpdate.execute();
    //
    // sqliteDatabase.setTransactionSuccessful();
    // } catch (Exception e) {
    // Logger.e("DAODATA", e.getLocalizedMessage(), e);
    // throw new IOException(e.getLocalizedMessage());
    // } finally {
    // sqliteDatabase.endTransaction();
    // }
    // }

    /**
     * Get the list of {@link Dataset}s from the db.
     * 
     * @param context the {@link Context} to use.
     * @return list of datasets.
     * @throws IOException
     */
    public static List<Dataset> getDatasetsList( Context context ) throws IOException {
        SQLiteDatabase sqliteDatabase = DatabaseManager.getInstance().getDatabase(context);
        List<Dataset> datasetList = new ArrayList<Dataset>();
        String asColumnsToReturn[] = {COLUMN_ID, COLUMN_TS, COLUMN_TITLE, COLUMN_LAT, COLUMN_LON, COLUMN_P1X, COLUMN_P1Y,
                COLUMN_P2X, COLUMN_P2Y};
        String strSortOrder = "_id ASC";
        Cursor c = sqliteDatabase.query(TABLE_DATASET, asColumnsToReturn, null, null, null, null, strSortOrder);
        c.moveToFirst();
        while( !c.isAfterLast() ) {
            long id = c.getLong(0);
            long ts = c.getLong(1);
            String title = c.getString(2);
            Double lat = null;
            if (!c.isNull(3))
                lat = c.getDouble(3);
            Double lon = null;
            if (!c.isNull(4))
                lon = c.getDouble(4);
            double[] p1 = null;
            if (!c.isNull(5) && !c.isNull(6)) {
                double p1x = c.getDouble(5);
                double p1y = c.getDouble(6);
                p1 = new double[]{p1x, p1y};
            }
            double[] p2 = null;
            if (!c.isNull(7) && !c.isNull(8)) {
                double p2x = c.getDouble(7);
                double p2y = c.getDouble(8);
                p2 = new double[]{p2x, p2y};
            }

            Dataset dataset = new Dataset(id, title, ts, lat, lon, p1, p2);
            datasetList.add(dataset);
            c.moveToNext();
        }
        c.close();
        return datasetList;
    }

    /**
     * Get the {@link Data} of a certain dataset.
     * 
     * @param context the {@link Context} to use.
     * @param dataset the parent dataset.
     * @return the data.
     * @throws IOException
     */
    public static Data getData4Dataset( Context context, Dataset dataset ) throws IOException {
        SQLiteDatabase sqliteDatabase = DatabaseManager.getInstance().getDatabase(context);

        DynamicDoubleArray timeArray = new DynamicDoubleArray(100);
        DynamicDoubleArray valuesArray = new DynamicDoubleArray(100);

        String asColumnsToReturn[] = {COLUMN_TS, COLUMN_VALUE};
        String strSortOrder = COLUMN_TS + " ASC";
        String strWhere = COLUMN_DATASET_ID + "=" + dataset.getId();
        Cursor c = null;
        try {
            c = sqliteDatabase.query(TABLE_DATA, asColumnsToReturn, strWhere, null, null, null, strSortOrder);
            c.moveToFirst();
            while( !c.isAfterLast() ) {
                long time = c.getLong(0);
                double value = c.getDouble(1);
                timeArray.add(time);
                valuesArray.add(value);
                c.moveToNext();
            }

            Data data = new Data(dataset, timeArray, valuesArray);
            return data;
        } finally {
            if (c != null)
                c.close();
        }
    }

    public static void createTables( Context context ) throws IOException {
        StringBuilder sB = new StringBuilder();

        sB = new StringBuilder();
        sB.append("CREATE TABLE ");
        sB.append(TABLE_DATA);
        sB.append(" (");
        sB.append(COLUMN_ID);
        sB.append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sB.append(COLUMN_TS).append(" LONG NOT NULL,");
        sB.append(COLUMN_VALUE).append(" REAL NOT NULL, ");
        sB.append(COLUMN_DATASET_ID).append(" INTEGER NOT NULL ");
        sB.append(");");
        String CREATE_TABLE_DATA = sB.toString();

        sB = new StringBuilder();
        sB.append("CREATE INDEX data_ts_idx ON ");
        sB.append(TABLE_DATA);
        sB.append(" ( ");
        sB.append(COLUMN_TS);
        sB.append(" );");
        String CREATE_INDEX_DATA_TS = sB.toString();

        sB = new StringBuilder();
        sB.append("CREATE INDEX data_dataset_id_idx ON ");
        sB.append(TABLE_DATA);
        sB.append(" ( ");
        sB.append(COLUMN_DATASET_ID);
        sB.append(" );");
        String CREATE_INDEX_DATASET_ID = sB.toString();

        /*
         * dataset table
         */
        sB = new StringBuilder();
        sB.append("CREATE TABLE ");
        sB.append(TABLE_DATASET);
        sB.append(" (");
        sB.append(COLUMN_ID);
        sB.append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sB.append(COLUMN_TS).append(" LONG NOT NULL,");
        sB.append(COLUMN_LON).append(" REAL, ");
        sB.append(COLUMN_LAT).append(" REAL,");
        sB.append(COLUMN_ALTIM).append(" REAL,");
        sB.append(COLUMN_P1X).append(" REAL,");
        sB.append(COLUMN_P1Y).append(" REAL,");
        sB.append(COLUMN_P2X).append(" REAL,");
        sB.append(COLUMN_P2Y).append(" REAL,");
        sB.append(COLUMN_TITLE).append(" TEXT NOT NULL ");
        sB.append(");");
        String CREATE_TABLE_DATASET = sB.toString();

        SQLiteDatabase sqliteDatabase = DatabaseManager.getInstance().getDatabase(context);
        if (Debug.D)
            Logger.i("DAODATA", "Create the data and dataset tables.");

        sqliteDatabase.beginTransaction();
        try {
            sqliteDatabase.execSQL(CREATE_TABLE_DATASET);
            sqliteDatabase.execSQL(CREATE_TABLE_DATA);
            sqliteDatabase.execSQL(CREATE_INDEX_DATA_TS);
            sqliteDatabase.execSQL(CREATE_INDEX_DATASET_ID);

            sqliteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            Logger.e("DAODATA", e.getLocalizedMessage(), e);
            throw new IOException(e.getLocalizedMessage());
        } finally {
            sqliteDatabase.endTransaction();
        }
    }

}

