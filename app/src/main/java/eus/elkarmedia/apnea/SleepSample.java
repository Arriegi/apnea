package eus.elkarmedia.apnea;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

/**
 * Represents a single data point sampled during a sleep session.
 *
 * Each sample captures the body position and sound level at a specific moment,
 * enabling intra-session correlation between posture and snoring intensity.
 *
 * Follows the Single Responsibility Principle: only holds and persists sample
 * data.
 */
public class SleepSample {

    public static final String TABLE_NAME = "sleep_sample";
    public static final String _ID = "id";
    public static final String SLEEP_ID = "sleep_id";
    public static final String TIMESTAMP = "timestamp";
    public static final String POSITION = "position";
    public static final String DECIBELS = "decibels";

    private long id;
    private long sleepId;
    private long timestamp;
    private int position;
    private double decibels;

    public SleepSample(long sleepId, long timestamp, int position, double decibels) {
        this.sleepId = sleepId;
        this.timestamp = timestamp;
        this.position = position;
        this.decibels = decibels;
    }

    public SleepSample(long id, long sleepId, long timestamp, int position, double decibels) {
        this.id = id;
        this.sleepId = sleepId;
        this.timestamp = timestamp;
        this.position = position;
        this.decibels = decibels;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSleepId() {
        return sleepId;
    }

    public void setSleepId(long sleepId) {
        this.sleepId = sleepId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public double getDecibels() {
        return decibels;
    }

    public void setDecibels(double decibels) {
        this.decibels = decibels;
    }

    public long storeOnDB(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(SLEEP_ID, sleepId);
        values.put(TIMESTAMP, timestamp);
        values.put(POSITION, position);
        values.put(DECIBELS, decibels);
        return db.insert(TABLE_NAME, null, values);
    }
}
