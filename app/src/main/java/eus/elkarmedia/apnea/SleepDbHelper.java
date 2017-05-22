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

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "Sleep.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + Sleep.TABLE_NAME + " (" +
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
                    Sleep.SYNC + " INTEGER)";

    private static final String SQL_DELETE_ONE_ENTRY = "DELETE FROM " + Sleep.TABLE_NAME + " WHERE ID = ";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + Sleep.TABLE_NAME;

    public SleepDbHelper(Context context) {
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            try {
                db.execSQL("ALTER TABLE " + Sleep.TABLE_NAME + " add column " + Sleep.SYNC + " INTEGER");
                db.execSQL("UPDATE " + Sleep.TABLE_NAME + " SET SYNC = 0");
            } catch (Exception e) {
                db.execSQL(SQL_DELETE_ENTRIES);
                this.onCreate(db);
            }

        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void deleteSleeps(SQLiteDatabase db) {
        db.delete(Sleep.TABLE_NAME, null, null);
    }

    public void deleteSleep(Sleep sleep) {
        this.getWritableDatabase().execSQL(SQL_DELETE_ONE_ENTRY + sleep.getId());
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
                Sleep.SYNC
        };

        String sortOrder =
                Sleep._ID + " ASC";

        Cursor cursor = db.query(
                Sleep.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        while(cursor.moveToNext()) {
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
                    cursor.getInt(cursor.getColumnIndexOrThrow(Sleep.SYNC))
            );
            sleeps.add(sleep);
        }
        cursor.close();
        return sleeps;
    }
}
