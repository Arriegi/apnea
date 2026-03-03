package eus.elkarmedia.apnea;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 27/12/2016.
 */

public class SleepDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "Sleep.db";

    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + Sleep.TABLE_NAME + " (" +
            Sleep._ID + " INTEGER PRIMARY KEY NOT NULL," +
            Sleep.LEFT_TITLE + " INTEGER," +
            Sleep.LEFT_COUNT_TITLE + " INTEGER," +
            Sleep.RIGHT_TITLE + " INTEGER," +
            Sleep.RIGHT_COUNT_TITLE + " INTEGER," +
            Sleep.BACK_TITLE + " INTEGER," +
            Sleep.BACK_COUNT_TITLE + " INTEGER," +
            Sleep.STOMACH_TITLE + " INTEGER," +
            Sleep.STOMACH_COUNT_TITLE + " INTEGER," +
            Sleep.UP_TITLE + " INTEGER," +
            Sleep.UP_COUNT_TITLE + " INTEGER," +
            Sleep.TOTAL_TITLE + " INTEGER," +
            Sleep.TOTAL_COUNT_TITLE + " INTEGER," +
            Sleep.AVG_DECIBELS + " REAL DEFAULT 0," +
            Sleep.MAX_DECIBELS + " REAL DEFAULT 0," +
            Sleep.SOUND_SAMPLE_COUNT + " INTEGER DEFAULT 0," +
            Sleep.SYNC + " INTEGER)";

    private static final String SQL_DELETE_ONE_ENTRY = "DELETE FROM " + Sleep.TABLE_NAME + " WHERE ID = ";
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + Sleep.TABLE_NAME;
    private static final String SQL_DELETE_SAMPLE_ENTRIES = "DROP TABLE IF EXISTS " + SleepSample.TABLE_NAME;

    private static final String SQL_CREATE_SAMPLE_TABLE = "CREATE TABLE " + SleepSample.TABLE_NAME + " (" +
            SleepSample._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            SleepSample.SLEEP_ID + " INTEGER NOT NULL," +
            SleepSample.TIMESTAMP + " INTEGER," +
            SleepSample.POSITION + " INTEGER," +
            SleepSample.DECIBELS + " REAL," +
            "FOREIGN KEY(" + SleepSample.SLEEP_ID + ") REFERENCES " +
            Sleep.TABLE_NAME + "(" + Sleep._ID + "))";

    public SleepDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_SAMPLE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            if (oldVersion < 2) {
                upgradeToVersion2(db);
            }
            if (oldVersion < 3) {
                upgradeToVersion3(db);
            }
            if (oldVersion < 4) {
                upgradeToVersion4(db);
            }
        } catch (Exception e) {
            db.execSQL(SQL_DELETE_SAMPLE_ENTRIES);
            db.execSQL(SQL_DELETE_ENTRIES);
            this.onCreate(db);
        }
    }

    private void upgradeToVersion2(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + Sleep.TABLE_NAME + " ADD COLUMN " + Sleep.SYNC + " INTEGER");
        db.execSQL("UPDATE " + Sleep.TABLE_NAME + " SET SYNC = 0");
    }

    private void upgradeToVersion3(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + Sleep.TABLE_NAME + " ADD COLUMN " + Sleep.AVG_DECIBELS + " REAL DEFAULT 0");
        db.execSQL("ALTER TABLE " + Sleep.TABLE_NAME + " ADD COLUMN " + Sleep.MAX_DECIBELS + " REAL DEFAULT 0");
        db.execSQL(
                "ALTER TABLE " + Sleep.TABLE_NAME + " ADD COLUMN " + Sleep.SOUND_SAMPLE_COUNT + " INTEGER DEFAULT 0");
    }

    private void upgradeToVersion4(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_SAMPLE_TABLE);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void deleteSleeps(SQLiteDatabase db) {
        db.delete(SleepSample.TABLE_NAME, null, null);
        db.delete(Sleep.TABLE_NAME, null, null);
    }

    public void deleteSleep(Sleep sleep) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(SleepSample.TABLE_NAME, SleepSample.SLEEP_ID + " = " + sleep.getId(), null);
        db.execSQL(SQL_DELETE_ONE_ENTRY + sleep.getId());
    }

    public List<Sleep> getSleeps(SQLiteDatabase db) {
        List<Sleep> sleeps = new ArrayList<>();
        String[] projection = {
                Sleep._ID,
                Sleep.LEFT_TITLE,
                Sleep.LEFT_COUNT_TITLE,
                Sleep.RIGHT_TITLE,
                Sleep.RIGHT_COUNT_TITLE,
                Sleep.BACK_TITLE,
                Sleep.BACK_COUNT_TITLE,
                Sleep.STOMACH_TITLE,
                Sleep.STOMACH_COUNT_TITLE,
                Sleep.UP_TITLE,
                Sleep.UP_COUNT_TITLE,
                Sleep.TOTAL_TITLE,
                Sleep.TOTAL_COUNT_TITLE,
                Sleep.AVG_DECIBELS,
                Sleep.MAX_DECIBELS,
                Sleep.SOUND_SAMPLE_COUNT,
                Sleep.SYNC
        };

        String sortOrder = Sleep._ID + " ASC";

        Cursor cursor = db.query(
                Sleep.TABLE_NAME, // The table to query
                projection, // The columns to return
                null, // The columns for the WHERE clause
                null, // The values for the WHERE clause
                null, // don't group the rows
                null, // don't filter by row groups
                sortOrder // The sort order
        );

        while (cursor.moveToNext()) {
            Sleep sleep = new Sleep(
                    cursor.getLong(cursor.getColumnIndexOrThrow(Sleep._ID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(Sleep.LEFT_TITLE)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(Sleep.LEFT_COUNT_TITLE)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(Sleep.RIGHT_TITLE)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(Sleep.RIGHT_COUNT_TITLE)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(Sleep.BACK_TITLE)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(Sleep.BACK_COUNT_TITLE)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(Sleep.STOMACH_TITLE)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(Sleep.STOMACH_COUNT_TITLE)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(Sleep.UP_TITLE)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(Sleep.UP_COUNT_TITLE)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(Sleep.TOTAL_TITLE)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(Sleep.TOTAL_COUNT_TITLE)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(Sleep.AVG_DECIBELS)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(Sleep.MAX_DECIBELS)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(Sleep.SOUND_SAMPLE_COUNT)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(Sleep.SYNC)));
            sleeps.add(sleep);
        }
        cursor.close();
        return sleeps;
    }

    public List<SleepSample> getSamplesForSleep(SQLiteDatabase db, long sleepId) {
        List<SleepSample> samples = new ArrayList<>();
        String[] projection = {
                SleepSample._ID,
                SleepSample.SLEEP_ID,
                SleepSample.TIMESTAMP,
                SleepSample.POSITION,
                SleepSample.DECIBELS
        };

        String selection = SleepSample.SLEEP_ID + " = ?";
        String[] selectionArgs = { String.valueOf(sleepId) };
        String sortOrder = SleepSample.TIMESTAMP + " ASC";

        Cursor cursor = db.query(
                SleepSample.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null, null,
                sortOrder);

        while (cursor.moveToNext()) {
            SleepSample sample = new SleepSample(
                    cursor.getLong(cursor.getColumnIndexOrThrow(SleepSample._ID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(SleepSample.SLEEP_ID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(SleepSample.TIMESTAMP)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(SleepSample.POSITION)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(SleepSample.DECIBELS)));
            samples.add(sample);
        }
        cursor.close();
        return samples;
    }
}
